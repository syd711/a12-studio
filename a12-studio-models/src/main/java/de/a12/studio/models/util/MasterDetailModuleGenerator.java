package de.a12.studio.models.util;

import de.a12.studio.models.Label;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.applicationmodel.Constraints;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.InitialActivity;
import de.a12.studio.models.applicationmodel.Layout;
import de.a12.studio.models.applicationmodel.MatchCondition;
import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.ModelDescriptor;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.RegionClearDirective;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.applicationmodel.SceneChange;
import de.a12.studio.models.applicationmodel.ViewAddDirective;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.masterdetailmodel.MasterDetailModelContent;

import java.util.ArrayList;
import java.util.List;

/**
 * Synthesizes an Application Model {@link Module} (a menu entry, an Overview/Tree scene and one Detail scene
 * per Form Mapping) from a referenced {@link MasterDetailModel}, mirroring SME's own generation logic
 * ({@code createModule} in {@code client/src/modules/appModel/document/masterDetailModule.ts}).
 *
 * <p>In SME, adding a {@code module-masterdetail} {@link de.a12.studio.models.ModelReference} to an
 * Application Model's header is enough to make that module appear in the deployed application: the client
 * runs this generation when producing the content actually uploaded to the server ({@code
 * toFileContentForUpload}), not when saving the edited model to disk. a12-studio mirrors that split -
 * {@code content.modules} on disk stays exactly what the user built by hand in the Modules panel (see {@code
 * ApplicationModelEditorController}), and this generator is applied only by {@code PreviewAppDeployer} while
 * building the zip uploaded to the Preview App server.
 */
public final class MasterDetailModuleGenerator {

  private static final String TYPE_TREE = "tree";
  private static final String LAYOUT_MASTER_DETAIL = "MasterDetail";

  private MasterDetailModuleGenerator() {
  }

  /**
   * @param id the id (filename) of {@code masterDetailModel}, i.e. the referencing header {@code
   *           ModelReference}'s {@code reference} - used as the generated module/flow/scene name prefix,
   *           matching SME's convention.
   */
  public static Module createModule(String id, MasterDetailModel masterDetailModel) {
    MasterDetailModelContent content = masterDetailModel.getContent();
    String type = content.getType();
    String masterModelName = TYPE_TREE.equals(type) ? content.getTreeModel() : content.getOverviewModel();

    Flow flow = new Flow();
    flow.setName(id + "Flow");
    flow.getScenes().add(createMasterScene(id, type, masterModelName));
    for (FormMapping mapping : content.getFormMapping()) {
      flow.getScenes().add(createDetailScene(id, type, mapping, content.getFormWidth()));
    }

    Module module = new Module();
    module.setName(id + "Module");
    module.setMenu(createMenu(id, type, masterDetailModel.getLabels()));
    module.setFlows(List.of(flow));
    return module;
  }

  private static Menu createMenu(String id, String type, List<Label> labels) {
    Menu menu = new Menu();
    menu.setName(id);
    menu.setLabel(new ArrayList<>(labels));

    InitialActivity initialActivity = new InitialActivity();
    initialActivity.getDescriptor().put("module", id + "Module");
    initialActivity.getDescriptor().put("engine", type);
    menu.setInitialActivity(initialActivity);
    return menu;
  }

  private static Scene createMasterScene(String id, String type, String masterModelName) {
    Scene scene = new Scene();
    scene.setName(id + "Overview");
    scene.setDescription("Overview type: " + type);
    scene.setMatchConditions(List.of(
        mustEqual("engine", type),
        mustEqual("module", id + "Module"),
        isSet("instance", false)));
    scene.setSceneChange(sceneChange(regionClear(), viewAdd(
        TYPE_TREE.equals(type) ? "TreeEngine" : "OverviewEngine", null, modelDescriptor(type, masterModelName, null))));
    return scene;
  }

  private static Scene createDetailScene(String id, String type, FormMapping mapping, Integer formWidth) {
    Constraints constraints = new Constraints();
    constraints.setType(LAYOUT_MASTER_DETAIL);
    constraints.setPreferredWidth(formWidth);

    Scene scene = new Scene();
    scene.setName(id + "_Detail_" + mapping.getDocumentModel());
    scene.setPriorScene(id + "Overview");
    scene.setMatchConditions(List.of(
        mustEqual("engine", type),
        isSet("instance", true),
        isSet("linkForm", false),
        mustEqual("model", mapping.getDocumentModel())));
    scene.setSceneChange(sceneChange(null,
        viewAdd("FormEngine", constraints, modelDescriptor("form", mapping.getFormModel(), mapping.getDocumentModel()))));
    return scene;
  }

  private static RegionClearDirective regionClear() {
    Layout layout = new Layout();
    layout.setName(LAYOUT_MASTER_DETAIL);
    RegionClearDirective directive = new RegionClearDirective();
    directive.setLayout(layout);
    return directive;
  }

  private static ViewAddDirective viewAdd(String name, Constraints constraints, ModelDescriptor descriptor) {
    ViewAddDirective directive = new ViewAddDirective();
    directive.setName(name);
    directive.setConstraints(constraints);
    directive.setModels(List.of(descriptor));
    return directive;
  }

  private static ModelDescriptor modelDescriptor(String modelType, String name, String documentModel) {
    ModelDescriptor descriptor = new ModelDescriptor();
    descriptor.setModelType(ModelType.fromValue(modelType));
    descriptor.setName(name);
    descriptor.setDocumentModel(documentModel);
    return descriptor;
  }

  private static SceneChange sceneChange(RegionClearDirective regionClear, ViewAddDirective viewAdd) {
    SceneChange sceneChange = new SceneChange();
    List<de.a12.studio.models.applicationmodel.Directive> onEnter = regionClear != null
        ? List.of(regionClear, viewAdd)
        : List.of(viewAdd);
    sceneChange.setOnEnter(onEnter);
    return sceneChange;
  }

  private static MatchCondition mustEqual(String key, String value) {
    MatchCondition condition = new MatchCondition();
    condition.setKey(key);
    condition.setMustEqual(value);
    return condition;
  }

  private static MatchCondition isSet(String key, boolean value) {
    MatchCondition condition = new MatchCondition();
    condition.setKey(key);
    condition.setIsSet(value);
    return condition;
  }
}
