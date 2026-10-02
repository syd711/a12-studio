package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModelResolver;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelHeterogeneity;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Port of SME's {@code DescendantOfHeterogeneousRelationship}, {@code DescendantOfHeterogeneousToManyRelationship}
 * and {@code InitialValueAndDescendantOfHeterogeneousToManyRelationship} custom conditions. In a Form Model bound
 * to a {@link ComposedDocumentModel}, a field behind a relationship whose target Document Model is heterogeneous
 * (it has non-abstract sub types, so the linked type is unknown on initialization) must not have an initial value;
 * for a Control with an index it is already an error when that relationship is to-many. Fields of the CDM's
 * own root ("cdd document") are never affected.
 * <p>
 * SME evaluates these on the expanded CDM; this walks the CDM's groups and follows {@code Include}s into the
 * referenced Document Models itself, so it needs no kernel expansion.
 */
public final class FormInitialValueHeterogeneousRelationshipValidator implements ModelValidator {

  private static final int MAX_DEPTH = 40;

  /** One ancestor of a field in the CDM: a relationship group, an include, or any other group. */
  private record Node(GroupElement group, boolean relationship, boolean include) {
  }

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null
        || formModel.getContent().getFieldConfiguration() == null
        || !(context.findOtherDocumentModel(FormBindingRepeatCdmRequiredValidator.dataBindingDocumentModelId(formModel)) instanceof ComposedDocumentModel cdm)) {
      return List.of();
    }
    List<FieldConfigEntry> entries = formModel.getContent().getFieldConfiguration().getField();
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> reportedFields = new HashSet<>();
    Set<String> fieldsWithInitialValue = new HashSet<>();
    for (FieldConfigEntry entry : entries) {
      if (entry.getInitialValue() == null || entry.getInitialValue().isEmpty() || entry.getElementRef() == null) {
        continue;
      }
      fieldsWithInitialValue.add(entry.getElementRef());
      List<Node> path = findPath(cdm, entry.getElementRef(), context);
      if (path != null && !isFieldOfCddDocument(path) && path.stream().anyMatch(node -> isHeterogeneous(node, context))) {
        reportedFields.add(entry.getElementRef());
        errors.add(new ModelValidationError(model, FormFieldReferenceValidator.ELEMENT_ID,
            ValidationMessages.get("validation.formInitialValueHeterogeneous.relationship", entry.getElementRef()), Severity.ERROR.name()));
      }
    }
    for (Control control : FormModelWalker.find(formModel.getContent(), Control.class)) {
      if (control.getIndex() == null || control.getIndex().getValue() == null || control.getIndex().getValue().isEmpty()
          || !fieldsWithInitialValue.contains(control.getElementRef()) || reportedFields.contains(control.getElementRef())) {
        continue;
      }
      List<Node> path = findPath(cdm, control.getElementRef(), context);
      if (path != null && !isFieldOfCddDocument(path)
          && path.stream().anyMatch(node -> node.relationship() && isToMany(node, context) && isHeterogeneous(node, context))) {
        errors.add(new ModelValidationError(model, control.getId(),
            ValidationMessages.get("validation.formInitialValueHeterogeneous.toManyWithIndex", control.getElementRef()), Severity.ERROR.name()));
      }
    }
    return errors;
  }

  // The ancestors of the element with the given id, outermost first, or null if the CDM does not contain it.
  private static List<Node> findPath(DocumentModel model, String elementId, ValidationContext context) {
    if (model.getContent() == null || model.getContent().getModelRoot() == null) {
      return null;
    }
    for (GroupElement root : model.getContent().getModelRoot().getRootGroups()) {
      List<Node> path = findIn(root, elementId, context, new ArrayList<>(), model);
      if (path != null) {
        return path;
      }
    }
    return null;
  }

  private static List<Node> findIn(Element element, String elementId, ValidationContext context, List<Node> ancestors, DocumentModel cdm) {
    if (elementId.equals(expandedPrefix(ancestors) + element.getId())) {
      return new ArrayList<>(ancestors);
    }
    if (!(element instanceof GroupElement groupElement) || groupElement.getGroup() == null || ancestors.size() > MAX_DEPTH) {
      return null;
    }
    GroupConfig group = groupElement.getGroup();
    String includedId = includedModelId(group, cdm);
    boolean include = includedId != null;
    boolean relationship = annotation(groupElement, ComposedDocumentModelResolver.RELATIONSHIP_ANNOTATION) != null;
    ancestors.add(new Node(groupElement, relationship, include));
    try {
      for (Element child : group.getElements()) {
        List<Node> found = findIn(child, elementId, context, ancestors, cdm);
        if (found != null) {
          return found;
        }
      }
      if (include && context.findOtherDocumentModel(includedId) instanceof DocumentModel included
          && included.getContent() != null && included.getContent().getModelRoot() != null) {
        for (GroupElement root : included.getContent().getModelRoot().getRootGroups()) {
          List<Node> found = findIn(root, elementId, context, ancestors, cdm);
          if (found != null) {
            return found;
          }
        }
      }
      return null;
    }
    finally {
      ancestors.remove(ancestors.size() - 1);
    }
  }

  // The kernel prefixes every element of an included model with the include group's id (nested includes chain), so
  // a Form Model bound to a CDM references e.g. "include_725f7_field_bbe8f" and only its own fields unprefixed.
  private static String expandedPrefix(List<Node> ancestors) {
    StringBuilder prefix = new StringBuilder();
    for (Node node : ancestors) {
      if (node.include()) {
        prefix.append(node.group().getId()).append('_');
      }
    }
    return prefix.toString();
  }

  // The id of the model an include group points at: its includeConfig, or - as SME writes them into a CDM - its
  // modelAlias resolved through the CDM's header modelReferences.
  private static String includedModelId(GroupConfig group, DocumentModel cdm) {
    if (group.getIncludeConfig() != null && group.getIncludeConfig().getReference() != null) {
      return group.getIncludeConfig().getReference();
    }
    if (group.getModelAlias() == null || group.getModelAlias().isBlank()) {
      return null;
    }
    return cdm.getModelReferences().stream()
        .filter(reference -> group.getModelAlias().equals(reference.getAlias()))
        .map(ModelReference::getReference).findFirst().orElse(group.getModelAlias());
  }

  // SME's isFieldOfCddDocument: no relationship group and no include on the path, or the last relationship group
  // lies below the last include.
  private static boolean isFieldOfCddDocument(List<Node> path) {
    int lastRelationship = -1;
    int lastInclude = -1;
    for (int i = 0; i < path.size(); i++) {
      if (path.get(i).relationship()) {
        lastRelationship = i;
      }
      if (path.get(i).include()) {
        lastInclude = i;
      }
    }
    return (lastRelationship == -1 && lastInclude == -1) || lastRelationship > lastInclude;
  }

  private static boolean isHeterogeneous(Node node, ValidationContext context) {
    if (!node.relationship()) {
      return false;
    }
    String targetId = annotation(node.group(), ComposedDocumentModelResolver.TARGET_DOCUMENT_MODEL_ANNOTATION);
    DocumentModel target = context.findOtherDocumentModel(targetId);
    if (target == null) {
      return false;
    }
    long nonAbstractSubTypes = DocumentModelHeterogeneity.recursiveSubTypes(context.otherDocumentModels(), targetId).stream()
        .map(context::findOtherDocumentModel)
        .filter(subType -> subType != null && !DocumentModelHeterogeneity.isAbstract(subType))
        .count();
    return DocumentModelHeterogeneity.isAbstract(target) ? nonAbstractSubTypes > 1 : nonAbstractSubTypes >= 1;
  }

  private static boolean isToMany(Node node, ValidationContext context) {
    String targetRole = annotation(node.group(), ComposedDocumentModelResolver.TARGET_ROLE_ANNOTATION);
    String relationshipName = annotation(node.group(), ComposedDocumentModelResolver.RELATIONSHIP_ANNOTATION);
    if (targetRole == null || !(context.findOtherModel(relationshipName) instanceof RelationshipModel relationshipModel)
        || relationshipModel.getContent() == null) {
      return false;
    }
    for (EntityCharacteristic characteristic : relationshipModel.getContent().getEntityCharacteristics()) {
      if (targetRole.equals(characteristic.getRole()) && characteristic.getLinkConstraints() != null
          && characteristic.getLinkConstraints().getMultiplicity() != null) {
        Multiplicity multiplicity = characteristic.getLinkConstraints().getMultiplicity();
        Integer upperLimit = multiplicity.getUpperLimit();
        return Boolean.TRUE.equals(multiplicity.getUnbounded()) || (upperLimit != null && upperLimit > 1);
      }
    }
    return false;
  }

  private static String annotation(Element element, String name) {
    return element.getAnnotations().stream().filter(a -> name.equals(a.getName())).map(Annotation::getValue).findFirst().orElse(null);
  }
}
