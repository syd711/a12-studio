package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.overviewmodel.FilterSection;
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

  /**
   * Opens the column editor for {@code column}, editing it live so a Cancel can undo the changes.
   */
  public static void showColumnDialog(Stage owner, ElementIndex documentModelIndex, String documentModelId, Column column) {
    FXMLLoader fxmlLoader = new FXMLLoader(OverviewColumnDialogController.class.getResource("overview-column-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage(null, fxmlLoader, owner, StudioBundle.get("edit_column_title"));
    OverviewColumnDialogController controller = (OverviewColumnDialogController) stage.getUserData();
    controller.init(stage, documentModelIndex, documentModelId, column);
    stage.setOnHidden(event -> controller.destroy());
    stage.showAndWait();
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

  public static Optional<FilterSection> showSectionForAdd(Stage owner) {
    FilterSection section = new FilterSection();
    section.setId("section-" + shortId());
    return showSection(owner, StudioBundle.get("add_section_title"), section) ? Optional.of(section) : Optional.empty();
  }

  public static boolean showSectionForEdit(Stage owner, FilterSection section) {
    return showSection(owner, StudioBundle.get("edit_section_title"), section);
  }

  private static boolean showSection(Stage owner, String title, FilterSection section) {
    FXMLLoader fxmlLoader = new FXMLLoader(SectionDataDialogController.class.getResource("overview-section-data-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("overview-section-data-dialog", fxmlLoader, owner, title);
    SectionDataDialogController controller = (SectionDataDialogController) stage.getUserData();
    controller.initDialog(stage, section);
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
