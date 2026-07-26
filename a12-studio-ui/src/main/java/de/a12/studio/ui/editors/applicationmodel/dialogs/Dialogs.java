package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Optional;

public class Dialogs {

  private static final String FLOW_NAME_TOOLTIP = "The name of the flow which acts as an identifier and must be unique among siblings.";

  public static Optional<Menu> showChildMenuForAdd(Stage owner) {
    Menu menu = new Menu();
    return showChildMenu(owner, "Add Child Menu Entry", menu, null) ? Optional.of(menu) : Optional.empty();
  }

  public static boolean showChildMenuForEdit(Stage owner, Menu menu) {
    return showChildMenu(owner, "Edit Child Menu Entry", menu, new MenuSnapshot(menu));
  }

  private static boolean showChildMenu(Stage owner, String title, Menu menu, MenuSnapshot snapshot) {
    FXMLLoader fxmlLoader = new FXMLLoader(ChildMenuDialogController.class.getResource("child-menu-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("dialog-children-menu", fxmlLoader, owner, title);
    ChildMenuDialogController controller = (ChildMenuDialogController) stage.getUserData();
    controller.init(stage, menu, snapshot);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(800);
    stage.setMinHeight(600);
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

  public static Optional<Flow> showFlowForAdd(Stage owner) {
    String name = WidgetFactory.showInputDialog(owner, "Add Flow", "Name", null, null, "New Flow", FLOW_NAME_TOOLTIP);
    if (name == null) {
      return Optional.empty();
    }
    Flow flow = new Flow();
    flow.setName(name);
    return Optional.of(flow);
  }

  public static boolean showFlowForEdit(Stage owner, Flow flow) {
    String name = WidgetFactory.showInputDialog(owner, "Edit Flow", "Name", null, null, flow.getName(), FLOW_NAME_TOOLTIP);
    if (name == null) {
      return false;
    }
    flow.setName(name);
    return true;
  }

  public static Optional<Scene> showSceneForAdd(Stage owner) {
    Scene scene = new Scene();
    scene.setName("New Scene");
    return showScene(owner, "Add Scene") ? Optional.of(scene) : Optional.empty();
  }

  public static boolean showSceneForEdit(Stage owner, Scene scene) {
    return showScene(owner, "Edit Scene");
  }

  private static boolean showScene(Stage owner, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(SceneDialogController.class.getResource("scene-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("dialog-scene", fxmlLoader, owner, title);
    SceneDialogController controller = (SceneDialogController) stage.getUserData();
    controller.init(stage);
    stage.showAndWait();
    return controller.isConfirmed();
  }
}
