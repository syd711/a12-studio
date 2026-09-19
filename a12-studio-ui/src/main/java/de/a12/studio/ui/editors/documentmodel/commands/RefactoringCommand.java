package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
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
 * <p>If the reference analysis itself fails, the structural change still happens (as it would without this
 * wrapper) and the failure is logged - refactoring is a convenience on top of the edit, never a reason to refuse it.
 */
public class RefactoringCommand implements Command {

  private static final Logger log = LoggerFactory.getLogger(RefactoringCommand.class);

  private final DocumentModel model;

  private final Command structuralChange;

  // Null until the first execute(); redo then reuses the same edits, which are still exactly right because the
  // model is back in the shape they were computed against.
  private List<Edit> edits;

  public RefactoringCommand(@NonNull DocumentModel model, @NonNull Command structuralChange) {
    this.model = model;
    this.structuralChange = structuralChange;
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

  @Override
  public void undo() {
    for (int i = edits.size() - 1; i >= 0; i--) {
      edits.get(i).revert();
    }
    structuralChange.undo();
  }
}
