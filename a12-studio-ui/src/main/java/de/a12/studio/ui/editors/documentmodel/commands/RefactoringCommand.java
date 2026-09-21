package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.ProjectReferenceRefactoring;
import de.a12.studio.modelsvalidation.refactoring.ProjectReferenceRefactoring.ModelEdits;
import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Wraps a structural change to a Document Model's element tree - a rename ({@link RenameElementCommand}) or a move
 * ({@link MoveNodeCommand}) - so that the path references it would otherwise break (rule and computation
 * conditions, error messages, {@code errorEntityRelPath}/{@code computedFieldRelPath}, index fields, uniqueness
 * criteria - see {@link DocumentModelRefactoring}) are rewritten in the same step: one {@link
 * de.a12.studio.ui.util.commandstack.CommandStack#execute}, one undo, one redo.
 *
 * <p>With a {@link ProjectModelStore} it also rewrites the references <em>other models of the project</em> hold on
 * this one (Print/Query/Mapping/Selection/Structural Mapping paths, rules of Document Models that include it - see
 * {@link ProjectReferenceRefactoring}) as part of the same step: those models are edited in place, saved and announced
 * on execute, and restored, saved and announced again on undo. Editors save on every edit, so no model has unsaved
 * changes that this could clash with, and open editors of those models share the instance that is edited.
 *
 * <p>If the reference analysis itself fails, the structural change still happens (as it would without this
 * wrapper) and the failure is logged - refactoring is a convenience on top of the edit, never a reason to refuse it.
 */
public class RefactoringCommand implements Command {

  private static final Logger log = LoggerFactory.getLogger(RefactoringCommand.class);

  private final DocumentModel model;

  private final Command structuralChange;

  // Null when only the model itself is to be kept consistent.
  private final ProjectModelStore store;

  // Null until the first execute(); redo then reuses the same edits, which are still exactly right because the
  // model is back in the shape they were computed against.
  private List<Edit> edits;

  private List<ModelEdits> externalEdits = Collections.emptyList();

  /**
   * {@code structuralChange} wrapped for the Document Model {@code item} holds, so that a rename or move made in its
   * editor also rewrites the references it would break (see the class comment); for any other model - which has no such
   * references - {@code structuralChange} itself.
   */
  public static Command around(ProjectItem item, @NonNull Command structuralChange) {
    return item != null && item.getModel() instanceof DocumentModel documentModel
        ? new RefactoringCommand(documentModel, structuralChange, ProjectModelStore.of(item))
        : structuralChange;
  }

  public RefactoringCommand(@NonNull DocumentModel model, @NonNull Command structuralChange) {
    this(model, structuralChange, null);
  }

  public RefactoringCommand(@NonNull DocumentModel model, @NonNull Command structuralChange,
      ProjectModelStore store) {
    this.model = model;
    this.structuralChange = structuralChange;
    this.store = store;
  }

  @Override
  public void execute() {
    if (edits == null) {
      executeFirstTime();
    }
    else {
      structuralChange.execute();
    }
    edits.forEach(Edit::apply);
    for (ModelEdits external : externalEdits) {
      if (isCurrent(external)) {
        external.edits().forEach(Edit::apply);
        store.changed(external.model());
      }
    }
  }

  private void executeFirstTime() {
    DocumentModelRefactoring.Plan plan = null;
    try {
      plan = DocumentModelRefactoring.prepare(model);
    }
    catch (RuntimeException e) {
      log.warn("Could not analyse the references of {} before a structural change; they won't be updated: {}",
          model.getId(), e.getMessage(), e);
    }

    structuralChange.execute();

    edits = Collections.emptyList();
    if (plan != null) {
      try {
        edits = plan.computeEdits();
        externalEdits = computeExternalEdits(plan);
        if (plan.skippedSites() > 0) {
          log.warn("{} rule/computation text(s) of {} could not be parsed and keep their old paths",
              plan.skippedSites(), model.getId());
        }
      }
      catch (RuntimeException e) {
        log.warn("Could not update the references of {} after a structural change: {}", model.getId(), e.getMessage(), e);
      }
    }
  }

  private List<ModelEdits> computeExternalEdits(DocumentModelRefactoring.Plan plan) {
    if (store == null) {
      return Collections.emptyList();
    }
    try {
      return ProjectReferenceRefactoring.computeEdits(model, plan, store.models());
    }
    catch (RuntimeException e) {
      log.warn("Could not update the references other models hold on {}: {}", model.getId(), e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  // An edit prepared against a model instance that has since been replaced (reloaded from disk) would land in an
  // object nobody reads any more; skipping it is the only safe answer.
  private boolean isCurrent(ModelEdits external) {
    if (store.isCurrent(external.model())) {
      return true;
    }
    log.warn("{} was reloaded since its references to {} were updated; leaving it as it is now",
        external.model().getId(), model.getId());
    return false;
  }

  @Override
  public void undo() {
    for (int i = externalEdits.size() - 1; i >= 0; i--) {
      ModelEdits external = externalEdits.get(i);
      if (isCurrent(external)) {
        List<Edit> modelEdits = external.edits();
        for (int j = modelEdits.size() - 1; j >= 0; j--) {
          modelEdits.get(j).revert();
        }
        store.changed(external.model());
      }
    }
    for (int i = edits.size() - 1; i >= 0; i--) {
      edits.get(i).revert();
    }
    structuralChange.undo();
  }
}
