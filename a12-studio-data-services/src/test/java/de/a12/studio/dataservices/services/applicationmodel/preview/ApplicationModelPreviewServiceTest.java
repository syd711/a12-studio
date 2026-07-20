package de.a12.studio.dataservices.services.applicationmodel.preview;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.applicationmodel.RegionClearDirective;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.applicationmodel.SceneChange;
import de.a12.studio.models.applicationmodel.ViewAddDirective;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationModelPreviewServiceTest {

  private final ApplicationModelPreviewService service = new ApplicationModelPreviewService();

  private static ApplicationModel loadFixture() throws Exception {
    try (InputStream in = ApplicationModelPreviewServiceTest.class.getResourceAsStream("/applicationmodel/PreviewApp_AM.json")) {
      String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      return JsonSettings.objectMapper.readValue(json, ApplicationModel.class);
    }
  }

  @Test
  void buildPreviewListsModulesAndTopLevelRegionTree() throws Exception {
    ApplicationModel model = loadFixture();

    PreviewApplicationDto preview = service.buildPreview(model);

    assertEquals(4, preview.modules().size());
    PreviewModuleDto personModule = preview.modules().get(1);
    assertEquals("PersonModule", personModule.name());
    assertEquals("Persons", personModule.label().stream().filter(l -> "en".equals(l.locale())).findFirst().orElseThrow().text());
    assertEquals("PersonModuleOverview", personModule.defaultSceneName());

    assertEquals("APP", preview.regionTree().name());
    assertEquals("ApplicationFrame", preview.regionTree().layout());
    assertEquals(3, preview.regionTree().subRegions().size());
    assertEquals("CONTENT", preview.regionTree().subRegions().get(0).name());
    assertTrue(preview.regionTree().subRegions().get(0).views().isEmpty());

    assertEquals("content", preview.initialActivity().get("engine"));
    assertEquals("WelcomePage", preview.initialActivity().get("name"));
  }

  @Test
  void resolvesOverviewSceneRegionClearAndViewAddIntoDefaultRegion() throws Exception {
    ApplicationModel model = loadFixture();

    PreviewSceneDto scene = service.resolveScene(model, "PersonModule", "PersonModuleOverview");

    PreviewRegionDto content = findRegion(scene.regionTree(), "CONTENT");
    assertEquals("MasterDetail", content.layout());
    assertEquals(List.of("OverviewEngine: Person_OM"), content.views());
  }

  @Test
  void resolvesDetailSceneViewAddWithoutRegionClear() throws Exception {
    ApplicationModel model = loadFixture();

    PreviewSceneDto scene = service.resolveScene(model, "PersonModule", "PersonModule_Detail_Person_DM");

    PreviewRegionDto content = findRegion(scene.regionTree(), "CONTENT");
    assertEquals(List.of("FormEngine: Person_FM"), content.views());
  }

  @Test
  void unknownModuleOrSceneNameThrows() throws Exception {
    ApplicationModel model = loadFixture();

    assertThrows(IllegalArgumentException.class, () -> service.resolveScene(model, "NoSuchModule", "x"));
    assertThrows(IllegalArgumentException.class, () -> service.resolveScene(model, "PersonModule", "NoSuchScene"));
  }

  @Test
  void directiveWithExplicitRegionTargetsOnlyThatRegion() {
    ApplicationModel model = new ApplicationModel();
    ApplicationModelContent content = new ApplicationModelContent();
    model.setContent(content);

    Region contentRegion = new Region();
    contentRegion.setName("CONTENT");
    Region sidebarRegion = new Region();
    sidebarRegion.setName("SIDEBAR");
    Region appRegion = new Region();
    appRegion.setName("APP");
    appRegion.setSubRegions(List.of(contentRegion, sidebarRegion));
    content.setRegion(appRegion);
    content.setDefaultRegion(List.of("CONTENT"));

    ViewAddDirective viewAdd = new ViewAddDirective();
    viewAdd.setName("SidebarEngine");
    viewAdd.setRegion(List.of("SIDEBAR"));

    Scene scene = new Scene();
    scene.setName("SomeScene");
    SceneChange sceneChange = new SceneChange();
    sceneChange.setOnEnter(List.of(viewAdd));
    scene.setSceneChange(sceneChange);

    Flow flow = new Flow();
    flow.setName("SomeFlow");
    flow.setScenes(List.of(scene));

    Module module = new Module();
    module.setName("SomeModule");
    Menu menu = new Menu();
    menu.setName("SomeModule");
    module.setMenu(menu);
    module.setFlows(List.of(flow));

    content.setModules(List.of(module));

    PreviewSceneDto resolved = service.resolveScene(model, "SomeModule", "SomeScene");

    assertTrue(findRegion(resolved.regionTree(), "CONTENT").views().isEmpty());
    assertEquals(List.of("SidebarEngine"), findRegion(resolved.regionTree(), "SIDEBAR").views());
  }

  private static PreviewRegionDto findRegion(PreviewRegionDto root, String name) {
    if (root.name().equals(name)) {
      return root;
    }
    return root.subRegions().stream()
        .map(sub -> findRegion(sub, name))
        .filter(found -> found != null)
        .findFirst()
        .orElse(null);
  }
}
