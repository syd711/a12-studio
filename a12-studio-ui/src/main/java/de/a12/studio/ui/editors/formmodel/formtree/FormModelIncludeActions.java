package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.formincludes.FormIncludeException;
import de.a12.studio.modelsvalidation.formincludes.FormIncludeExpander;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.formmodel.formtree.commands.ExpandIncludeCommand;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.commandstack.CommandStack;
import javafx.scene.control.ButtonType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The tree's include actions: <em>Include Form Model</em> copies the first screen of another Form Model in, and
 * <em>Refresh Include</em> replaces such a copy by a fresh one (see {@link FormIncludeExpander}). Both are one undo
 * step ({@link ExpandIncludeCommand}). Kept apart from {@link FormModelActions}, which delegates here.
 */
final class FormModelIncludeActions {

  private final FormModelContent content;
  private final CommandStack commandStack;
  private final Consumer<Object> onModelChanged;
  private final ProjectItem projectItem;
  private final @Nullable DocumentModel documentModel;

  FormModelIncludeActions(@NonNull FormModelContent content, @NonNull CommandStack commandStack, @NonNull Consumer<Object> onModelChanged,
      @NonNull ProjectItem projectItem, @Nullable DocumentModel documentModel) {
    this.content = content;
    this.commandStack = commandStack;
    this.onModelChanged = onModelChanged;
    this.projectItem = projectItem;
    this.documentModel = documentModel;
  }

  /** Whether elements can be included into {@code node}: it holds screen elements, and the form has a Document Model. */
  boolean canIncludeInto(@NonNull Object node) {
    return documentModel != null && projectItem.getModel() instanceof FormModel
        && (node instanceof Screen || node instanceof Section || node instanceof MultiColumnSection || node instanceof EmbeddedRepeat);
  }

  /** Whether {@code node} is (the first element of) something a form was expanded from, so it can be refreshed. */
  boolean isRefreshable(@NonNull Object node) {
    return node instanceof ScreenElement element && documentModel != null && projectItem.getModel() instanceof FormModel
        && notEmpty(element.getIncludeId()) && notEmpty(element.getFormModelRef()) && element.getHostDocumentModelPath() != null;
  }

  void includeInto(@NonNull FormElementViewModel target) {
    if (!(projectItem.getModel() instanceof FormModel host) || documentModel == null) {
      return;
    }
    List<DocumentModel> documentModels = ProjectDocumentModels.getOtherDocumentModelsWithCombinations(projectItem);
    List<FormModel> sources = otherForms().stream()
        .filter(form -> FormIncludeExpander.documentModelOf(form, documentModels).isPresent()).toList();
    String includeId = newIncludeId();
    EmbeddedRepeat repeat = target.getNode() instanceof EmbeddedRepeat embeddedRepeat ? embeddedRepeat : null;

    Optional<FormIncludeExpander.Expansion> expansion =
        Dialogs.showIncludeFormModel(Studio.stage, host, documentModel, sources, documentModels, includeId, repeat != null);
    if (expansion.isEmpty()) {
      return;
    }
    if (repeat != null) {
      if (hasContent(repeat.getControlGrid()) && !confirmReplacingGrid()) {
        return;
      }
      commandStack.execute(new ExpandIncludeCommand(content, repeat, expansion.get()));
    }
    else {
      List<Object> siblings = FormModelActions.childListOf(target.getNode());
      commandStack.execute(new ExpandIncludeCommand(content, siblings, siblings.size(), 0, expansion.get()));
    }
    onModelChanged.accept(expansion.get().elements().get(0));
  }

  private static boolean hasContent(@Nullable ControlGrid grid) {
    return grid != null && grid.getRow().stream().anyMatch(row -> !row.getCell().isEmpty());
  }

  private static boolean confirmReplacingGrid() {
    Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("form_model_tree.include_replaces_grid"),
        StudioBundle.get("form_model_tree.include_replaces_grid_help"), null, StudioBundle.get("form_model_tree.refresh_include_button"));
    return confirmation.isPresent() && confirmation.get() == ButtonType.OK;
  }

  /**
   * Replaces the include {@code item} belongs to (it and its neighbors with the same include id) by a fresh copy.
   * {@code siblings} is the list it lives in, or null when it is the grid of an Embedded Repeat.
   */
  void refresh(@NonNull FormElementViewModel item, @Nullable List<Object> siblings) {
    if (!(projectItem.getModel() instanceof FormModel host) || !(item.getNode() instanceof ScreenElement element)) {
      return;
    }
    Optional<FormModel> source = otherForms().stream().filter(form -> form.getId().equals(element.getFormModelRef())).findFirst();
    if (source.isEmpty()) {
      WidgetFactory.showAlert(Studio.stage, StudioBundle.get("form_model_tree.refresh_include_missing", element.getFormModelRef()));
      return;
    }
    EmbeddedRepeat repeat = siblings == null && item.getParentNode() instanceof EmbeddedRepeat embeddedRepeat ? embeddedRepeat : null;
    if (siblings == null && repeat == null) {
      return;
    }
    int[] run = siblings == null ? new int[] {0, 0} : FormIncludeExpander.includeRun(siblings, siblings.indexOf(element));
    // Only a single element has a name of its own; with several, each is named after the include again.
    String name = run[0] == run[1] ? element.getName() : null;
    FormIncludeExpander.Expansion expansion;
    try {
      expansion = new FormIncludeExpander(ProjectDocumentModels.getOtherDocumentModelsWithCombinations(projectItem))
          .expand(source.get(), host, element.getHostDocumentModelPath(), element.getIncludeId(), name);
    }
    catch (FormIncludeException e) {
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
      return;
    }
    if (repeat != null && !expansion.isSingleControlGrid()) {
      WidgetFactory.showAlert(Studio.stage, StudioBundle.get("include_form_model.grid_required", element.getFormModelRef()));
      return;
    }
    Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(Studio.stage,
        StudioBundle.get("form_model_tree.refresh_include_confirm", run[1] - run[0] + 1, element.getFormModelRef()),
        StudioBundle.get("form_model_tree.refresh_include_help"), null, StudioBundle.get("form_model_tree.refresh_include_button"));
    if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
      return;
    }
    commandStack.execute(repeat != null
        ? new ExpandIncludeCommand(content, repeat, expansion)
        : new ExpandIncludeCommand(content, siblings, run[0], run[1] - run[0] + 1, expansion));
    onModelChanged.accept(expansion.elements().get(0));
  }

  private List<FormModel> otherForms() {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.FORM).stream()
        .filter(FormModel.class::isInstance).map(FormModel.class::cast).toList();
  }

  private String newIncludeId() {
    String id;
    do {
      id = FormModelElementFactory.generateId("include");
    }
    while (!FormIncludeExpander.isIncludeIdFree(content, id));
    return id;
  }

  private static boolean notEmpty(@Nullable String value) {
    return value != null && !value.isEmpty();
  }
}
