package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Optional;

public class Dialogs {

  public static Optional<Menu> showForAdd(Stage owner) {
    Menu menu = new Menu();
    return show(owner, "Add Child Menu Entry", menu, null) ? Optional.of(menu) : Optional.empty();
  }

  public static boolean showForEdit(Stage owner, Menu menu) {
    return show(owner, "Edit Child Menu Entry", menu, new MenuSnapshot(menu));
  }

  private static boolean show(Stage owner, String title, Menu menu, MenuSnapshot snapshot) {
    FXMLLoader fxmlLoader = new FXMLLoader(ChildMenuDialogController.class.getResource("child-menu-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("dialog-children-menu", fxmlLoader, owner, title);
    ChildMenuDialogController controller = (ChildMenuDialogController) stage.getUserData();
    controller.init(stage, menu, snapshot);

    stage.setOnHidden(event -> controller.destroy());
    stage.showAndWait();
    return controller.isConfirmed();
  }

  public static Optional<String> showModuleForAdd(Stage owner) {
    return showModule(owner, "Add Module", "");
  }

  public static Optional<String> showModuleForEdit(Stage owner, String currentName) {
    return showModule(owner, "Edit Module", currentName);
  }

  private static Optional<String> showModule(Stage owner, String title, String initialName) {
    FXMLLoader fxmlLoader = new FXMLLoader(ModuleDialogController.class.getResource("module-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("dialog-module", fxmlLoader, owner, title);
    ModuleDialogController controller = (ModuleDialogController) stage.getUserData();
    controller.init(stage, initialName);
    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<String> showSubregionForAdd(Stage owner) {
    return showSubregion(owner, "Add Subregion", "");
  }

  public static Optional<String> showSubregionForEdit(Stage owner, String currentName) {
    return showSubregion(owner, "Edit Subregion", currentName);
  }

  private static Optional<String> showSubregion(Stage owner, String title, String initialName) {
    FXMLLoader fxmlLoader = new FXMLLoader(SubregionDialogController.class.getResource("subregion-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("dialog-subregion", fxmlLoader, owner, title);
    SubregionDialogController controller = (SubregionDialogController) stage.getUserData();
    controller.init(stage, initialName);
    stage.showAndWait();
    return controller.getResult();
  }
}
