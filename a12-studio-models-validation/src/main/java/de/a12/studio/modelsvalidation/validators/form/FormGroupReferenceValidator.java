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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Every Repeat in the Screens tree, and every group configuration entry, must reference a group that still
 * exists in one of the referenced Document Models, mirroring {@link FormFieldReferenceValidator} for {@link
 * GroupConfigEntry#getGroupRef()} - most Repeats have no {@link GroupConfigEntry} at all, so the tree itself
 * has to be scanned for dangling {@code groupRef}s, not just {@code groupConfiguration.group}.
 */
public final class FormGroupReferenceValidator implements ModelValidator {

  // Fallback elementId for a dangling GroupConfigEntry with no Repeat left in the tree that still references
  // it - same reasoning as FormFieldReferenceValidator.ELEMENT_ID.
  public static final String ELEMENT_ID = "content/groupConfiguration";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }

    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      // Without a resolvable document model there is nothing to check against
      // (FormDocumentModelReferenceValidator already reports the missing reference).
      return List.of();
    }

    Map<String, List<AbstractRepeat>> referencingNodesByRef = new LinkedHashMap<>();
    visitAll(formModel, (ref, node) -> referencingNodesByRef.computeIfAbsent(ref, k -> new ArrayList<>()).add(node));

    Set<String> groupRefs = new LinkedHashSet<>(referencingNodesByRef.keySet());
    if (formModel.getContent().getGroupConfiguration() != null) {
      for (GroupConfigEntry entry : formModel.getContent().getGroupConfiguration().getGroup()) {
        if (entry.getGroupRef() != null && !entry.getGroupRef().isBlank()) {
          groupRefs.add(entry.getGroupRef());
        }
      }
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (String groupRef : groupRefs) {
      if (indexes.stream().anyMatch(index -> index.isResolvable(groupRef))) {
        continue;
      }
      String message = ValidationMessages.get("validation.formGroupReference.missing", groupRef);
      List<AbstractRepeat> referencingNodes = referencingNodesByRef.getOrDefault(groupRef, List.of());
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
    visitAll(formModel, (ref, node) -> {
      if (groupRef.equals(ref)) {
        matches.add(node);
      }
    });
    return matches;
  }

  /** Visits every Repeat in the Screen tree that carries a non-blank {@code groupRef}. */
  private static void visitAll(FormModel formModel, BiConsumer<String, AbstractRepeat> onRef) {
    for (Screen screen : formModel.getContent().getScreens()) {
      visit(screen.getScreenElements(), onRef);
    }
  }

  private static void visit(List<ScreenElement> elements, BiConsumer<String, AbstractRepeat> onRef) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      visit(element, onRef);
    }
  }

  private static void visit(ScreenElement element, BiConsumer<String, AbstractRepeat> onRef) {
    if (element instanceof Section section) {
      visit(section.getScreenElements(), onRef);
    }
    else if (element instanceof MultiColumnSection section) {
      visit(section.getScreenElements(), onRef);
    }
    else if (element instanceof AbstractRepeat repeat) {
      if (repeat.getGroupRef() != null && !repeat.getGroupRef().isBlank()) {
        onRef.accept(repeat.getGroupRef(), repeat);
      }
      if (repeat instanceof DetachedRepeat detachedRepeat && detachedRepeat.getDetailScreen() != null) {
        visit(detachedRepeat.getDetailScreen().getScreenElements(), onRef);
      }
    }
  }
}
