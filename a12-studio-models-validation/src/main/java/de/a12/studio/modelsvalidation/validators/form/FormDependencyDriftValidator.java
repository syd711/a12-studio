package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.DependentCase;
import de.a12.studio.models.formmodel.DependentConfig;
import de.a12.studio.models.formmodel.DependentEnumeration;
import de.a12.studio.models.formmodel.DependentEnumerationConstraint;
import de.a12.studio.models.formmodel.EnumerationConstraintValue;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Form-vs-Document-Model drift in the dependency configuration (SME's consistency check between a Form Model and
 * its possibly since-changed Document Model): a Form Model stores field ids and field <em>values</em> in its
 * dependent fields/groups/enumerations, hide conditions and control dependencies, and nothing keeps those in step
 * when the Document Model changes underneath. Reported, per configuration:
 * <ul>
 *   <li>the master field no longer exists, or is no longer a Boolean/Confirm/Enumeration field (an enumeration for
 *       a dependent enumeration, like SME's {@code determineDependentEnumState});</li>
 *   <li>a case / constraint / control dependency for a master value the field no longer has (the "(no value)"
 *       case is always possible);</li>
 *   <li>a dependent enumeration whose own field is no longer an enumeration or no longer has an offered value or
 *       the value to switch to;</li>
 *   <li>a dependent field case that forces an enumeration value the field no longer has, or copies from a field
 *       that no longer exists;</li>
 *   <li>a Control with a control dependency whose own field can no longer be a master.</li>
 * </ul>
 * The hide condition's master <em>values</em> are {@link HideConditionSupportedValuesValidator}'s. Anything that
 * cannot be resolved because the Document Model itself is not available is skipped, as there is nothing to compare.
 */
public final class FormDependencyDriftValidator implements ModelValidator {

  private enum Kind {
    DEPENDENT_FIELD("validation.dependency.kind.dependentField"),
    DEPENDENT_GROUP("validation.dependency.kind.dependentGroup"),
    DEPENDENT_ENUMERATION("validation.dependency.kind.dependentEnumeration"),
    HIDE_CONDITION("validation.dependency.kind.hideCondition"),
    CONTROL_DEPENDENCY("validation.dependency.kind.controlDependency");

    private final String messageKey;

    Kind(String messageKey) {
      this.messageKey = messageKey;
    }

    String label() {
      return ValidationMessages.get(messageKey);
    }
  }

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      return List.of();
    }
    Run run = new Run(formModel, indexes);
    FormModelContent content = formModel.getContent();
    if (content.getFieldConfiguration() != null) {
      content.getFieldConfiguration().getField().forEach(run::checkFieldEntry);
    }
    if (content.getGroupConfiguration() != null) {
      content.getGroupConfiguration().getGroup().forEach(run::checkGroupEntry);
    }
    HideConditionElements.collect(content).forEach(run::checkHideCondition);
    FormModelWalker.find(content, Control.class).forEach(run::checkControlDependency);
    return run.errors;
  }

  private static final class Run {

    private final FormModel model;
    private final List<ElementIndex> indexes;
    private final List<ModelValidationError> errors = new ArrayList<>();

    Run(FormModel model, List<ElementIndex> indexes) {
      this.model = model;
      this.indexes = indexes;
    }

    // ── Field / group configuration ────────────────────────────────────────

    void checkFieldEntry(FieldConfigEntry entry) {
      List<String> nodeIds = fieldNodeIds(entry.getElementRef());
      if (entry.getDependentField() != null) {
        DependentConfig config = entry.getDependentField();
        checkMaster(Kind.DEPENDENT_FIELD, entry.getElementRef(), config.getMasterField(), false,
            masterValues(config), nodeIds);
        for (DependentCase dependentCase : config.getCases()) {
          checkCaseTargets(entry, dependentCase, nodeIds);
        }
      }
      if (entry.getDependentEnumeration() != null) {
        checkDependentEnumeration(entry, entry.getDependentEnumeration(), nodeIds);
      }
    }

    void checkGroupEntry(GroupConfigEntry entry) {
      if (entry.getDependentGroup() != null) {
        checkMaster(Kind.DEPENDENT_GROUP, entry.getGroupRef(), entry.getDependentGroup().getMasterField(), false,
            masterValues(entry.getDependentGroup()), groupNodeIds(entry.getGroupRef()));
      }
    }

    private static List<String> masterValues(DependentConfig config) {
      return config.getCases().stream().map(DependentCase::getMasterValue).toList();
    }

    /** What a dependent field case does to the field: the value it forces and the field it copies from. */
    private void checkCaseTargets(FieldConfigEntry entry, DependentCase dependentCase, List<String> nodeIds) {
      if (dependentCase.getFieldRef() != null && !dependentCase.getFieldRef().isBlank()
          && indexes.stream().noneMatch(index -> index.isResolvable(dependentCase.getFieldRef()))) {
        report(nodeIds, "validation.dependentField.fieldRefMissing", entry.getElementRef(), dependentCase.getFieldRef());
      }
      String forced = dependentCase.getValue();
      if (forced != null && !forced.isEmpty()) {
        Optional<Set<String>> ownValues = enumerationValues(entry.getElementRef());
        if (ownValues.isPresent() && !ownValues.get().contains(forced)) {
          report(nodeIds, "validation.dependentField.valueMissing", entry.getElementRef(), forced);
        }
      }
    }

    private void checkDependentEnumeration(FieldConfigEntry entry, DependentEnumeration dependency, List<String> nodeIds) {
      Optional<Set<String>> ownValues = enumerationValues(entry.getElementRef());
      if (ownValues.isEmpty() && effectiveType(entry.getElementRef()).isPresent()) {
        report(nodeIds, "validation.dependentEnumeration.dependentNotEnumeration", entry.getElementRef());
      }
      checkMaster(Kind.DEPENDENT_ENUMERATION, entry.getElementRef(), dependency.getMasterField(), true,
          dependency.getConstraints().stream().map(DependentEnumerationConstraint::getMasterValue).toList(), nodeIds);
      if (ownValues.isEmpty()) {
        return;
      }
      for (DependentEnumerationConstraint constraint : dependency.getConstraints()) {
        for (EnumerationConstraintValue offered : constraint.getConstraintValues()) {
          if (!ownValues.get().contains(offered.getValue())) {
            report(nodeIds, "validation.dependentEnumeration.valueMissing", entry.getElementRef(), offered.getValue());
          }
        }
        String switchTo = constraint.getValueForMasterChange();
        if (switchTo != null && !switchTo.isEmpty() && !ownValues.get().contains(switchTo)) {
          report(nodeIds, "validation.dependentEnumeration.initialValueMissing", entry.getElementRef(), switchTo);
        }
      }
    }

    // ── Hide conditions / control dependencies ─────────────────────────────

    void checkHideCondition(HideConditionElements.Entry entry) {
      // Only the master field itself: which of its values can hide is HideConditionSupportedValuesValidator's.
      checkMaster(Kind.HIDE_CONDITION, entry.nodeId(), entry.hideCondition().getMasterField(), false, List.of(),
          List.of(entry.nodeId()));
    }

    void checkControlDependency(Control control) {
      if (control.getDependentControls() == null || control.getDependentControls().getScreenElement().isEmpty()
          || control.getElementRef() == null || control.getElementRef().isBlank()) {
        return;
      }
      String owner = DependentControlOptionsMustExistValidator.controlLabel(control);
      List<String> masterValues = new ArrayList<>();
      control.getDependentControls().getScreenElement().forEach(entry -> masterValues.add(entry.getMasterValue()));
      checkMaster(Kind.CONTROL_DEPENDENCY, owner, control.getElementRef(), false, masterValues, List.of(control.getId()));
    }

    // ── Shared ─────────────────────────────────────────────────────────────

    /**
     * The master field of one configuration: it must exist, be a Boolean/Confirm/Enumeration field (only an
     * enumeration when {@code enumerationOnly}) and have every value in {@code usedValues}.
     */
    private void checkMaster(Kind kind, String owner, String masterField, boolean enumerationOnly,
        List<String> usedValues, List<String> nodeIds) {
      if (masterField == null || masterField.isBlank()) {
        return; // the "...MasterRequired" validators report that
      }
      Optional<Element> element = findElement(masterField);
      if (element.isEmpty()) {
        report(nodeIds, "validation.dependency.masterMissing", kind.label(), owner, masterField);
        return;
      }
      if (!(element.get() instanceof FieldElement)) {
        report(nodeIds, "validation.dependency.masterInvalidType", kind.label(), owner, masterField);
        return;
      }
      Optional<FieldType> type = effectiveType(masterField);
      if (type.isEmpty()) {
        return; // its type definition is not available: nothing to compare against
      }
      List<String> possible = DependentControlSupport.masterValues(type.get());
      if (possible.isEmpty() || (enumerationOnly && !(type.get() instanceof EnumerationFieldType))) {
        report(nodeIds, enumerationOnly ? "validation.dependency.masterNotEnumeration" : "validation.dependency.masterInvalidType",
            kind.label(), owner, masterField);
        return;
      }
      Set<String> reported = new HashSet<>();
      for (String value : usedValues) {
        if (!possible.contains(value) && reported.add(value)) {
          report(nodeIds, "validation.dependency.masterValueMissing", kind.label(), owner, masterField, value);
        }
      }
    }

    private Optional<Element> findElement(String elementId) {
      for (ElementIndex index : indexes) {
        Optional<Element> element = index.resolveElement(elementId);
        if (element.isPresent()) {
          return element;
        }
      }
      return Optional.empty();
    }

    private Optional<FieldType> effectiveType(String elementId) {
      for (ElementIndex index : indexes) {
        Optional<FieldType> type = DependentControlSupport.effectiveFieldType(elementId, index);
        if (type.isPresent()) {
          return type;
        }
      }
      return Optional.empty();
    }

    /** The declared values of an enumeration field; empty for any other field (or an unresolved one). */
    private Optional<Set<String>> enumerationValues(String elementId) {
      Optional<FieldType> type = effectiveType(elementId);
      if (type.isPresent() && type.get() instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
        Set<String> values = new HashSet<>();
        for (EnumerationValue value : enumType.getEnumerationType().getValues()) {
          values.add(value.getValue());
        }
        return Optional.of(values);
      }
      return Optional.empty();
    }

    private List<String> fieldNodeIds(String elementRef) {
      List<String> ids = new ArrayList<>();
      for (Object node : FormFieldReferenceValidator.findReferencingNodes(model, elementRef)) {
        ids.add(FormFieldReferenceValidator.idOf(node));
      }
      return ids.isEmpty() ? List.of(FormFieldReferenceValidator.ELEMENT_ID) : ids;
    }

    private List<String> groupNodeIds(String groupRef) {
      List<String> ids = new ArrayList<>();
      for (AbstractRepeat repeat : FormGroupReferenceValidator.findReferencingNodes(model, groupRef)) {
        ids.add(repeat.getId());
      }
      return ids.isEmpty() ? List.of(FormGroupReferenceValidator.ELEMENT_ID) : ids;
    }

    private void report(List<String> nodeIds, String messageKey, Object... args) {
      String message = ValidationMessages.get(messageKey, args);
      for (String nodeId : nodeIds) {
        errors.add(new ModelValidationError(model, nodeId, message, Severity.ERROR.name()));
      }
    }
  }
}
