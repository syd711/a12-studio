package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.ActionGroup;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.overviewmodel.FilterSection;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public class Dialogs {

  private Dialogs() {
  }

  public static Optional<Column> showColumnForAdd(Stage owner, ElementIndex documentModelIndex, String documentModelId) {
    Column column = new Column();
    column.setId("column-" + shortId());
    column.setWidth(1.0);
    return showColumn(owner, StudioBundle.get("add_column_title"), documentModelIndex, documentModelId, column)
        ? Optional.of(column) : Optional.empty();
  }

  public static boolean showColumnForEdit(Stage owner, ElementIndex documentModelIndex, String documentModelId, Column column) {
    return showColumn(owner, StudioBundle.get("edit_column_title"), documentModelIndex, documentModelId, column);
  }

  /**
   * Opens the column editor for {@code column}, editing it live so a Cancel can undo the changes.
   */
  private static boolean showColumn(Stage owner, String title, ElementIndex documentModelIndex, String documentModelId, Column column) {
    FXMLLoader fxmlLoader = new FXMLLoader(OverviewColumnDialogController.class.getResource("overview-column-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("overview-column-dialog", fxmlLoader, owner, title);
    OverviewColumnDialogController controller = (OverviewColumnDialogController) stage.getUserData();
    controller.init(stage, documentModelIndex, documentModelId, column);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }

  public static Optional<Button> showMultiSelectionActionForAdd(Stage owner) {
    Button button = new Button();
    return showMultiSelectionAction(owner, StudioBundle.get("add_action_title"), button) ? Optional.of(button) : Optional.empty();
  }

  public static boolean showMultiSelectionActionForEdit(Stage owner, Button button) {
    return showMultiSelectionAction(owner, StudioBundle.get("edit_action_title"), button);
  }

  private static boolean showMultiSelectionAction(Stage owner, String title, Button button) {
    FXMLLoader fxmlLoader = new FXMLLoader(MultiSelectionActionDialogController.class.getResource("overview-multi-selection-action-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("overview-multi-selection-action-dialog", fxmlLoader, owner, title);
    MultiSelectionActionDialogController controller = (MultiSelectionActionDialogController) stage.getUserData();
    controller.initDialog(stage, button);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }

  public static Optional<FilterSection> showSectionForAdd(Stage owner, ElementIndex documentModelIndex) {
    FilterSection section = new FilterSection();
    section.setId("section-" + shortId());
    return showSection(owner, StudioBundle.get("add_section_title"), documentModelIndex, section) ? Optional.of(section) : Optional.empty();
  }

  public static boolean showSectionForEdit(Stage owner, ElementIndex documentModelIndex, FilterSection section) {
    return showSection(owner, StudioBundle.get("edit_section_title"), documentModelIndex, section);
  }

  private static boolean showSection(Stage owner, String title, ElementIndex documentModelIndex, FilterSection section) {
    FXMLLoader fxmlLoader = new FXMLLoader(SectionDataDialogController.class.getResource("overview-section-data-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("overview-section-data-dialog", fxmlLoader, owner, title);
    SectionDataDialogController controller = (SectionDataDialogController) stage.getUserData();
    controller.initDialog(stage, documentModelIndex, section);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }

  public static Optional<FilterGroup> showFilterGroupForAdd(Stage owner, ElementIndex documentModelIndex) {
    FilterGroup group = new FilterGroup();
    group.setId("filter-group-" + shortId());
    return showFilterGroup(owner, StudioBundle.get("add_filter_group_title"), documentModelIndex, group)
        ? Optional.of(group) : Optional.empty();
  }

  public static boolean showFilterGroupForEdit(Stage owner, FilterGroup group, ElementIndex documentModelIndex) {
    return showFilterGroup(owner, StudioBundle.get("edit_filter_group_title"), documentModelIndex, group);
  }

  private static boolean showFilterGroup(Stage owner, String title, ElementIndex documentModelIndex, @NonNull FilterGroup group) {
    FXMLLoader fxmlLoader = new FXMLLoader(FilterGroupDialogController.class.getResource("overview-filter-group-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("overview-filter-group-dialog", fxmlLoader, owner, title);
    FilterGroupDialogController controller = (FilterGroupDialogController) stage.getUserData();
    controller.init(stage, documentModelIndex, group);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }

  public static boolean showFilterItemForAdd(Stage owner, ElementIndex documentModelIndex, FilterItem item) {
    return showFilterItem(owner, StudioBundle.get("add_filter_item_title"), documentModelIndex, item);
  }

  public static boolean showFilterItemForEdit(Stage owner, ElementIndex documentModelIndex, FilterItem item) {
    return showFilterItem(owner, StudioBundle.get("edit_filter_item_title"), documentModelIndex, item);
  }

  private static boolean showFilterItem(Stage owner, String title, ElementIndex documentModelIndex, @NonNull FilterItem item) {
    FXMLLoader fxmlLoader = new FXMLLoader(FilterItemDialogController.class.getResource("overview-filter-item-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("overview-filter-item-dialog", fxmlLoader, owner, title);
    FilterItemDialogController controller = (FilterItemDialogController) stage.getUserData();
    controller.init(stage, documentModelIndex, item);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }

  public static Optional<ActionGroup> showContextMenuGroupForAdd(Stage owner, OverviewModel model) {
    ActionGroup group = new ActionGroup();
    return showContextMenuGroup(owner, StudioBundle.get("add_context_menu_group_title"), model, group)
        ? Optional.of(group) : Optional.empty();
  }

  public static boolean showContextMenuGroupForEdit(Stage owner, OverviewModel model, ActionGroup group) {
    return showContextMenuGroup(owner, StudioBundle.get("edit_context_menu_group_title"), model, group);
  }

  private static boolean showContextMenuGroup(Stage owner, String title, OverviewModel model, @NonNull ActionGroup group) {
    FXMLLoader fxmlLoader = new FXMLLoader(ContextMenuGroupDialogController.class.getResource("context-menu-group-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("context-menu-group-dialog", fxmlLoader, owner, title);
    ContextMenuGroupDialogController controller = (ContextMenuGroupDialogController) stage.getUserData();
    controller.init(stage, model, group);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }

  /**
   * Opens the Add dialog for a brand-new, unattached context-menu action - the caller only adds it to its
   * owning {@link ActionGroup#getActions()} once present, mirroring {@link
   * de.a12.studio.ui.editors.overviewmodel.dialogs.FilterItemsPanelController}'s Add flow.
   */
  public static Optional<Button> showContextMenuActionForAdd(Stage owner) {
    return showContextMenuAction(owner, StudioBundle.get("add_context_menu_action_title"), new Button());
  }

  /**
   * Opens the Edit dialog for a working copy of {@code action}, so a Cancel leaves the real, attached action
   * untouched - mirrors {@link de.a12.studio.ui.editors.propertyeditors.dialogs.Dialogs#showEventButtonForEdit}.
   * The caller only replaces the original action with the returned one once present.
   */
  public static Optional<Button> showContextMenuActionForEdit(Stage owner, @NonNull Button action) {
    return showContextMenuAction(owner, StudioBundle.get("edit_context_menu_action_title"), cloneAction(action));
  }

  private static Optional<Button> showContextMenuAction(Stage owner, String title, Button action) {
    FXMLLoader fxmlLoader = new FXMLLoader(ContextMenuActionDialogController.class.getResource("context-menu-action-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("context-menu-action-dialog", fxmlLoader, owner, title);
    ContextMenuActionDialogController controller = (ContextMenuActionDialogController) stage.getUserData();
    controller.init(stage, action);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();

    if (!controller.isConfirmed() || action.getEvent() == null || action.getEvent().isBlank()) {
      return Optional.empty();
    }
    return Optional.of(action);
  }

  private static Button cloneAction(Button action) {
    String json = JsonSettings.objectMapper.writeValueAsString(action);
    return JsonSettings.objectMapper.readValue(json, Button.class);
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }
}
