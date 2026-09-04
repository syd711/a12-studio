package de.a12.studio.models.util;

import de.a12.studio.models.Label;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.masterdetailmodel.MasterDetailModelContent;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// None of the applicationmodel/masterdetailmodel POJOs override equals()/hashCode(), so generated
// structures are compared as JSON trees (which do have proper deep equality) rather than via assertEquals
// on the objects themselves. The expected shapes mirror the hand-authored CompanyModule/PersonModule
// entries in testing/workspaces/basic/models/PreviewApp_AM.json, which follow the same scene-graph
// convention this generator reproduces (see MasterDetailModuleGenerator's class javadoc).
class MasterDetailModuleGeneratorTest {

  @Test
  void generatesModuleForOverviewTypeWithOneFormMapping() {
    MasterDetailModel masterDetailModel = overviewTypeModel(
        List.of(label("en", "My Shopping List"), label("de", "Meine Einkaufsliste")),
        List.of(formMapping("Product_DM", "Product_FM")),
        null);

    Module module = MasterDetailModuleGenerator.createModule("Product_MDM", masterDetailModel);

    assertJsonEquals("""
        {
          "name": "Product_MDMModule",
          "menu": {
            "name": "Product_MDM",
            "label": [
              { "locale": "en", "text": "My Shopping List" },
              { "locale": "de", "text": "Meine Einkaufsliste" }
            ],
            "initialActivity": {
              "descriptor": { "module": "Product_MDMModule", "engine": "overview" }
            }
          },
          "flows": [
            {
              "name": "Product_MDMFlow",
              "scenes": [
                {
                  "name": "Product_MDMOverview",
                  "description": "Overview type: overview",
                  "matchConditions": [
                    { "key": "engine", "mustEqual": "overview" },
                    { "key": "module", "mustEqual": "Product_MDMModule" },
                    { "key": "instance", "isSet": false }
                  ],
                  "sceneChange": {
                    "onEnter": [
                      { "type": "REGION_CLEAR", "layout": { "name": "MasterDetail" } },
                      {
                        "type": "VIEW_ADD",
                        "name": "OverviewEngine",
                        "models": [ { "modelType": "overview", "name": "Product_OM" } ]
                      }
                    ]
                  }
                },
                {
                  "name": "Product_MDM_Detail_Product_DM",
                  "priorScene": "Product_MDMOverview",
                  "matchConditions": [
                    { "key": "engine", "mustEqual": "overview" },
                    { "key": "instance", "isSet": true },
                    { "key": "linkForm", "isSet": false },
                    { "key": "model", "mustEqual": "Product_DM" }
                  ],
                  "sceneChange": {
                    "onEnter": [
                      {
                        "type": "VIEW_ADD",
                        "name": "FormEngine",
                        "constraints": { "type": "MasterDetail" },
                        "models": [ { "modelType": "form", "name": "Product_FM", "documentModel": "Product_DM" } ]
                      }
                    ]
                  }
                }
              ]
            }
          ]
        }
        """, module);
  }

  @Test
  void generatesOneDetailScenePerFormMappingAndAppliesPreferredWidth() {
    MasterDetailModel masterDetailModel = overviewTypeModel(List.of(),
        List.of(formMapping("Company_DM", "Company_FM"), formMapping("Person_DM", "Person_FM")), 8);

    Module module = MasterDetailModuleGenerator.createModule("MDM", masterDetailModel);

    JsonNode scenes = JsonSettings.objectMapper.valueToTree(module).get("flows").get(0).get("scenes");
    assertEquals(3, scenes.size());
    assertEquals("MDM_Detail_Company_DM", scenes.get(1).get("name").asString());
    assertEquals("MDM_Detail_Person_DM", scenes.get(2).get("name").asString());
    assertEquals(8, scenes.get(1).get("sceneChange").get("onEnter").get(0).get("constraints").get("preferredWidth").asInt());
  }

  @Test
  void generatesTreeEngineViewForTreeType() {
    MasterDetailModelContent content = new MasterDetailModelContent();
    content.setType("tree");
    content.setTreeModel("Product_TrM");
    content.setFormMapping(List.of());

    MasterDetailModel masterDetailModel = new MasterDetailModel();
    masterDetailModel.setContent(content);

    Module module = MasterDetailModuleGenerator.createModule("Product_MDM", masterDetailModel);

    JsonNode masterView = JsonSettings.objectMapper.valueToTree(module)
        .get("flows").get(0).get("scenes").get(0).get("sceneChange").get("onEnter").get(1);
    assertEquals("TreeEngine", masterView.get("name").asString());
    assertEquals("tree", masterView.get("models").get(0).get("modelType").asString());
    assertEquals("Product_TrM", masterView.get("models").get(0).get("name").asString());
  }

  private static MasterDetailModel overviewTypeModel(List<Label> labels, List<FormMapping> formMapping, Integer formWidth) {
    MasterDetailModelContent content = new MasterDetailModelContent();
    content.setType("overview");
    content.setOverviewModel("Product_OM");
    content.setFormMapping(formMapping);
    content.setFormWidth(formWidth);

    MasterDetailModel model = new MasterDetailModel();
    model.setContent(content);
    model.setLabels(labels);
    return model;
  }

  private static FormMapping formMapping(String documentModel, String formModel) {
    FormMapping mapping = new FormMapping();
    mapping.setDocumentModel(documentModel);
    mapping.setFormModel(formModel);
    return mapping;
  }

  private static Label label(String locale, String text) {
    Label label = new Label();
    label.setLocale(locale);
    label.setText(text);
    return label;
  }

  private static void assertJsonEquals(String expectedJson, Object actual) {
    JsonNode expected = JsonSettings.objectMapper.readTree(expectedJson);
    JsonNode actualTree = JsonSettings.objectMapper.valueToTree(actual);
    assertEquals(expected, actualTree);
  }
}
