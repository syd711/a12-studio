package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Every group configuration entry must reference a group that still exists in one of the referenced Document
 * Models, mirroring {@link FormFieldReferenceValidator} for {@link GroupConfigEntry#getGroupRef()}.
 */
public final class FormGroupReferenceValidator implements ModelValidator {

  // Fallback elementId for a dangling GroupConfigEntry with no Repeat left in the tree that still references
  // it - same reasoning as FormFieldReferenceValidator.ELEMENT_ID.
  public static final String ELEMENT_ID = "content/groupConfiguration";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel)
        || formModel.getContent() == null
        || formModel.getContent().getGroupConfiguration() == null) {
      return List.of();
    }

    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      // Without a resolvable document model there is nothing to check against
      // (FormDocumentModelReferenceValidator already reports the missing reference).
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (GroupConfigEntry entry : formModel.getContent().getGroupConfiguration().getGroup()) {
      if (entry.getGroupRef() == null || entry.getGroupRef().isBlank()
          || indexes.stream().anyMatch(index -> index.isResolvable(entry.getGroupRef()))) {
        continue;
      }
      String message = ValidationMessages.get("validation.formGroupReference.missing", entry.getGroupRef());
      List<AbstractRepeat> referencingNodes = findReferencingNodes(formModel, entry.getGroupRef());
      if (referencingNodes.isEmpty()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID, message, Severity.ERROR.name()));
      }
      else {
        for (AbstractRepeat node : referencingNodes) {
          errors.add(new ModelValidationError(model, node.getId(), message, Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }

  /** Every Repeat in the Screen tree whose {@code groupRef} equals {@code groupRef}. */
  static List<AbstractRepeat> findReferencingNodes(FormModel formModel, String groupRef) {
    List<AbstractRepeat> matches = new ArrayList<>();
    for (Screen screen : formModel.getContent().getScreens()) {
      visit(screen.getScreenElements(), groupRef, matches);
    }
    return matches;
  }

  private static void visit(List<ScreenElement> elements, String groupRef, List<AbstractRepeat> matches) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      visit(element, groupRef, matches);
    }
  }

  private static void visit(ScreenElement element, String groupRef, List<AbstractRepeat> matches) {
    if (element instanceof Section section) {
      visit(section.getScreenElements(), groupRef, matches);
    }
    else if (element instanceof MultiColumnSection section) {
      visit(section.getScreenElements(), groupRef, matches);
    }
    else if (element instanceof AbstractRepeat repeat) {
      if (groupRef.equals(repeat.getGroupRef())) {
        matches.add(repeat);
      }
      if (repeat instanceof DetachedRepeat detachedRepeat && detachedRepeat.getDetailScreen() != null) {
        visit(detachedRepeat.getDetailScreen().getScreenElements(), groupRef, matches);
      }
    }
  }
}
