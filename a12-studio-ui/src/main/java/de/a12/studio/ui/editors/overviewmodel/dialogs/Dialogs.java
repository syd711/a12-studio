package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.overviewmodel.FilterSection;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.util.FXResizeHelper;
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

    FXResizeHelper.install(stage, 30, 6, WidgetFactory.DIALOG_SHADOW_MARGIN);
    // root's minWidth/minHeight (see overview-column-dialog.fxml) are 700/760; the stage must
    // allow at least that plus the shadowWrapper's padding on both sides, or the root can't lay
    // out at its real minimum and the header/footer get clipped.
    stage.setMinWidth(700 + 2 * WidgetFactory.DIALOG_SHADOW_MARGIN);
    stage.setMinHeight(760 + 2 * WidgetFactory.DIALOG_SHADOW_MARGIN);

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
    Stage stage = WidgetFactory.createDialogStage(null, fxmlLoader, owner, title);
    SectionDataDialogController controller = (SectionDataDialogController) stage.getUserData();
    controller.initDialog(stage, documentModelIndex, section);
    stage.setOnHidden(event -> controller.destroy());

    FXResizeHelper.install(stage, 30, 6, WidgetFactory.DIALOG_SHADOW_MARGIN);
    stage.setMinWidth(800);
    stage.setMinHeight(600);
    stage.setOnHidden(event -> controller.destroy());

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
    stage.showAndWait();
    return controller.isConfirmed();
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }
}
