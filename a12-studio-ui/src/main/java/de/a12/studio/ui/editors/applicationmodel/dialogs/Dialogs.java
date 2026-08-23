package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.ui.util.StudioBundle;

import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Optional;

public class Dialogs {

  private static final String FLOW_NAME_TOOLTIP = "The name of the flow which acts as an identifier and must be unique among siblings.";

  public static Optional<Menu> showChildMenuForAdd(Stage owner) {
    Menu menu = new Menu();
    return showChildMenu(owner, StudioBundle.get("add_child_menu_entry"), menu, null) ? Optional.of(menu) : Optional.empty();
  }

  public static boolean showChildMenuForEdit(Stage owner, Menu menu) {
    return showChildMenu(owner, StudioBundle.get("edit_child_menu_entry"), menu, new MenuSnapshot(menu));
  }

  private static boolean showChildMenu(Stage owner, String title, Menu menu, MenuSnapshot snapshot) {
    FXMLLoader fxmlLoader = new FXMLLoader(ChildMenuDialogController.class.getResource("child-menu-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("dialog-children-menu", fxmlLoader, owner, title);
    ChildMenuDialogController controller = (ChildMenuDialogController) stage.getUserData();
    controller.init(stage, menu, snapshot);
    WidgetFactory.installResizable(stage);
    stage.setOnHidden(event -> controller.destroy());

    stage.showAndWait();
    return controller.isConfirmed();
  }

  public static Optional<String> showModuleForAdd(Stage owner) {
    return showModule(owner, StudioBundle.get("add_module_title"), "");
  }

  public static Optional<String> showModuleForEdit(Stage owner, String currentName) {
    return showModule(owner, StudioBundle.get("edit_module_title"), currentName);
  }

  private static Optional<String> showModule(Stage owner, String title, String initialName) {
    FXMLLoader fxmlLoader = new FXMLLoader(ModuleDialogController.class.getResource("module-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("dialog-module", fxmlLoader, owner, title);
    ModuleDialogController controller = (ModuleDialogController) stage.getUserData();
    controller.init(stage, initialName);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<String> showSubregionForAdd(Stage owner) {
    return showSubregion(owner, StudioBundle.get("add_subregion_title"), "");
  }

  public static Optional<String> showSubregionForEdit(Stage owner, String currentName) {
    return showSubregion(owner, StudioBundle.get("edit_subregion_title"), currentName);
  }

  private static Optional<String> showSubregion(Stage owner, String title, String initialName) {
    FXMLLoader fxmlLoader = new FXMLLoader(SubregionDialogController.class.getResource("subregion-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("dialog-subregion", fxmlLoader, owner, title);
    SubregionDialogController controller = (SubregionDialogController) stage.getUserData();
    controller.init(stage, initialName);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<Flow> showFlowForAdd(Stage owner) {
    String name = WidgetFactory.showInputDialog(owner, StudioBundle.get("add_flow_title"), StudioBundle.get("name"), null, null, "New Flow", FLOW_NAME_TOOLTIP);
    if (name == null) {
      return Optional.empty();
    }
    Flow flow = new Flow();
    flow.setName(name);
    return Optional.of(flow);
  }

  public static boolean showFlowForEdit(Stage owner, Flow flow) {
    String name = WidgetFactory.showInputDialog(owner, StudioBundle.get("edit_flow_title"), StudioBundle.get("name"), null, null, flow.getName(), FLOW_NAME_TOOLTIP);
    if (name == null) {
      return false;
    }
    flow.setName(name);
    return true;
  }

  public static Optional<Scene> showSceneForAdd(Stage owner, Flow flow) {
    Scene scene = new Scene();
    scene.setName("New Scene");
    return showScene(owner, StudioBundle.get("add_scene_title"), flow, scene, null) ? Optional.of(scene) : Optional.empty();
  }

  public static boolean showSceneForEdit(Stage owner, Flow flow, Scene scene) {
    return showScene(owner, StudioBundle.get("edit_scene_title"), flow, scene, new SceneSnapshot(scene));
  }

  private static boolean showScene(Stage owner, String title, Flow flow, Scene scene, SceneSnapshot snapshot) {
    FXMLLoader fxmlLoader = new FXMLLoader(SceneDialogController.class.getResource("scene-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("dialogscene", fxmlLoader, owner, title);
    SceneDialogController controller = (SceneDialogController) stage.getUserData();
    controller.init(stage, flow, scene, snapshot);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }

  public static Optional<Case> showCaseForAdd(Stage owner) {
    Case caseObj = new Case();
    caseObj.setName("New Case");
    return showCase(owner, StudioBundle.get("add_case_title"), caseObj, null) ? Optional.of(caseObj) : Optional.empty();
  }

  public static boolean showCaseForEdit(Stage owner, Case caseObj) {
    return showCase(owner, StudioBundle.get("edit_case_title"), caseObj, new CaseSnapshot(caseObj));
  }

  private static boolean showCase(Stage owner, String title, Case caseObj, CaseSnapshot snapshot) {
    FXMLLoader fxmlLoader = new FXMLLoader(CaseDialogController.class.getResource("case-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("dialog-case", fxmlLoader, owner, title);
    CaseDialogController controller = (CaseDialogController) stage.getUserData();
    controller.init(stage, caseObj, snapshot);
    WidgetFactory.installResizable(stage);

    stage.setOnHidden(event -> controller.destroy());
    stage.showAndWait();
    return controller.isConfirmed();
  }

  public static Optional<Directive> showDirectiveForAdd(Stage owner) {
    return showDirective(owner, StudioBundle.get("add_directive_title"), null);
  }

  public static Optional<Directive> showDirectiveForEdit(Stage owner, Directive directive) {
    return showDirective(owner, StudioBundle.get("edit_directive_title"), directive);
  }

  private static Optional<Directive> showDirective(Stage owner, String title, Directive existing) {
    FXMLLoader fxmlLoader = new FXMLLoader(DirectiveDialogController.class.getResource("directive-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("dialog-directive", fxmlLoader, owner, title);
    DirectiveDialogController controller = (DirectiveDialogController) stage.getUserData();
    controller.init(stage, existing);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }
}
