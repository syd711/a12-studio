package de.a12.studio.dataservices.models.applicationmodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.applicationmodel.*;
import de.a12.studio.models.applicationmodel.Module;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationModelLoadTest {

  @Test
  void loadsPreviewAppApplicationModel() throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream("/applicationmodel/PreviewApp_AM.json")) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    JsonMapper mapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    ApplicationModel model = mapper.readValue(json, ApplicationModel.class);

    assertEquals("PreviewApp_AM", model.getId());
    assertEquals(ModelType.APPLICATION, model.getModelType());
    assertEquals("6.0.0", model.getModelVersion());
    assertEquals(2, model.getLocales().size());
    assertEquals("en", model.getLocales().get(0).getCode());
    assertEquals(2, model.getLabels().size());
    assertEquals("Basic", model.getLabels().get(0).getText());
    assertEquals(2, model.getAnnotations().size());
    assertEquals("workspaceName", model.getAnnotations().get(0).getName());
    assertTrue(model.getModelReferences().isEmpty());

    assertNotNull(model.getContent());
    assertEquals(4, model.getContent().getModules().size());
    assertEquals(1, model.getContent().getDefaultRegion().size());
    assertEquals("CONTENT", model.getContent().getDefaultRegion().get(0));

    assertEquals("content", model.getContent().getInitialActivity().getDescriptor().get("engine"));
    assertEquals("WelcomePage", model.getContent().getInitialActivity().getDescriptor().get("name"));
    assertEquals(Boolean.TRUE, model.getContent().getInitialActivity().getWithoutData());

    Region region = model.getContent().getRegion();
    assertEquals("APP", region.getName());
    assertEquals("ApplicationFrame", region.getLayout().getName());
    assertEquals(3, region.getSubRegions().size());
    assertEquals("CONTENT", region.getSubRegions().get(0).getName());
    assertEquals("MasterDetail", region.getSubRegions().get(0).getLayout().getName());

    Module personModule = model.getContent().getModules().get(1);
    assertEquals("PersonModule", personModule.getName());
    assertEquals("PersonModule", personModule.getMenu().getName());
    assertEquals("Persons", personModule.getMenu().getLabel().get(0).getText());
    assertEquals("PersonModule", personModule.getMenu().getInitialActivity().getDescriptor().get("module"));
    assertEquals("overview", personModule.getMenu().getInitialActivity().getDescriptor().get("engine"));

    assertEquals(1, personModule.getFlows().size());
    Flow personFlow = personModule.getFlows().get(0);
    assertEquals("PersonModuleFlow", personFlow.getName());
    assertEquals(2, personFlow.getScenes().size());

    Scene overviewScene = personFlow.getScenes().get(0);
    assertEquals("PersonModuleOverview", overviewScene.getName());
    assertEquals(3, overviewScene.getMatchConditions().size());
    MatchCondition instanceCondition = overviewScene.getMatchConditions().get(2);
    assertEquals("instance", instanceCondition.getKey());
    assertEquals(Boolean.FALSE, instanceCondition.getIsSet());

    assertEquals(2, overviewScene.getSceneChange().getOnEnter().size());
    Directive regionClear = overviewScene.getSceneChange().getOnEnter().get(0);
    assertEquals(DirectiveType.REGION_CLEAR, regionClear.getType());
    assertInstanceOf(RegionClearDirective.class, regionClear);
    assertEquals("MasterDetail", ((RegionClearDirective) regionClear).getLayout().getName());

    Directive viewAdd = overviewScene.getSceneChange().getOnEnter().get(1);
    assertInstanceOf(ViewAddDirective.class, viewAdd);
    ViewAddDirective viewAddDirective = (ViewAddDirective) viewAdd;
    assertEquals(DirectiveType.VIEW_ADD, viewAddDirective.getType());
    assertEquals("OverviewEngine", viewAddDirective.getName());
    assertEquals(1, viewAddDirective.getModels().size());
    assertEquals(ModelType.OVERVIEW, viewAddDirective.getModels().get(0).getModelType());
    assertEquals("Person_OM", viewAddDirective.getModels().get(0).getName());

    Scene detailScene = personFlow.getScenes().get(1);
    assertEquals("PersonModule_Detail_Person_DM", detailScene.getName());
    assertEquals("PersonModuleOverview", detailScene.getPriorScene());
    ViewAddDirective detailViewAdd = (ViewAddDirective) detailScene.getSceneChange().getOnEnter().get(0);
    assertEquals("MasterDetail", detailViewAdd.getConstraints().getType());
    assertEquals(ModelType.FORM, detailViewAdd.getModels().get(0).getModelType());
    assertEquals("Person_FM", detailViewAdd.getModels().get(0).getName());
    assertEquals("Person_DM", detailViewAdd.getModels().get(0).getDocumentModel());

    Module invoiceModule = model.getContent().getModules().get(3);
    Scene invoiceDetailScene = invoiceModule.getFlows().get(0).getScenes().get(1);
    ViewAddDirective invoiceViewAdd = (ViewAddDirective) invoiceDetailScene.getSceneChange().getOnEnter().get(0);
    assertEquals(8, invoiceViewAdd.getConstraints().getPreferredWidth());

    // Re-serializing an already-loaded model must round-trip through the polymorphic Directive types.
    String reserialized = mapper.writeValueAsString(model);
    ApplicationModel reloaded = mapper.readValue(reserialized, ApplicationModel.class);
    assertEquals("PreviewApp_AM", reloaded.getId());
    assertEquals(4, reloaded.getContent().getModules().size());
    ViewAddDirective reloadedViewAdd = (ViewAddDirective) reloaded.getContent().getModules().get(1).getFlows().get(0)
        .getScenes().get(0).getSceneChange().getOnEnter().get(1);
    assertEquals("OverviewEngine", reloadedViewAdd.getName());
  }
}
