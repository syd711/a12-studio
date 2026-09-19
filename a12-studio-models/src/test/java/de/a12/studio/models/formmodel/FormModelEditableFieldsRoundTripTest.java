package de.a12.studio.models.formmodel;

import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// The Form Model fields the editor grew UI for after they had already been modeled: multi file upload options,
// row action buttonStyling/confirmation, repeat column icon/pinDirection/hideCondition and the model-level
// styles. Wire shapes follow real SME fixtures (Contract_Fm.json's multiFileUploadOptions,
// DefaultRowActionExpectedAddForm.json's rowActionGroup, FmmMapperTestModel-form.json's column icon,
// BAPFormEngineTest-form.json's column pinDirection); the whole document must survive load-then-save unchanged.
class FormModelEditableFieldsRoundTripTest {

  private static final String JSON = """
      {
        "header": {"id": "Editable_FM", "modelType": "form", "modelVersion": "39.0.0"},
        "content": {
          "styles": [{"name": "-u-hidden"}, {"name": "highlight"}],
          "defaults": {}, "fieldConfiguration": {}, "groupConfiguration": {},
          "screens": [{
            "id": "screen1", "name": "Screen1",
            "screenElements": [{
              "type": "InlineRepeat", "id": "repeat1", "name": "Documents", "groupRef": "group_docs",
              "repeatOverviewColumn": [
                {"type": "FieldBasedRepeatOverviewColumn", "id": "column1", "elementRef": "field_a",
                 "pinDirection": "LEFT", "icon": {"name": "face"},
                 "hideCondition": {"masterField": "field_flag", "cases": [{"masterValue": "true"}]}},
                {"type": "ExpressionRepeatOverviewColumn", "id": "column2", "name": "sum",
                 "expression": "[A] + [B]", "pinDirection": "RIGHT", "icon": {"name": "view_column"}}
              ],
              "rowActionGroup": {"action": [{
                "buttonStyling": {
                  "label": {"type": "Multilingual", "multilingualText": {"text": [{"locale": "en", "text": "Remove"}]}},
                  "description": {"text": [{"locale": "en", "text": "Removes the row"}]},
                  "icon": {"name": "delete"},
                  "priority": "PRIMARY",
                  "destructive": true,
                  "labelHidden": true,
                  "style": [{"name": "highlight"}]
                },
                "event": "event_remove",
                "confirmation": {"text": [{"locale": "en", "text": "Really remove?"}]},
                "confirmationDialogTitle": {"text": [{"locale": "en", "text": "Warning"}]},
                "scope": "HIDDEN_IN_READONLY_MODE",
                "annotations": [{"name": "note", "value": "x"}]
              }]},
              "multiFileUpload": true,
              "multiFileUploadOptions": {
                "elementRef": "group_attachments",
                "enableDownload": true,
                "fileUploadDescription": {"text": [{"locale": "en", "text": "Drop files here"}]},
                "hideFileUploadDescription": true,
                "fileUploadButtonText": {"text": [{"locale": "en", "text": "Browse"}]},
                "hideFileUploadButtonText": true,
                "fileUploadHelperText": {"text": [{"locale": "en", "text": "Max. 10 MB"}]}
              }
            }]
          }]
        }
      }
      """;

  // Second batch: attachment settings (FieldConfigEntry.attachmentConfig, SME's AttachmentConfig type) and the
  // remaining column fields (alignment overrides as in Person_FM.json, display flags, header styles, annotations).
  private static final String SECOND_JSON = """
      {
        "header": {"id": "Editable2_FM", "modelType": "form", "modelVersion": "39.0.0"},
        "content": {
          "styles": [{"name": "narrow"}],
          "defaults": {}, "groupConfiguration": {},
          "fieldConfiguration": {"field": [
            {"elementRef": "group_attachments",
             "attachmentConfig": {"placeholderIcon": "pdf", "accept": "image/jpeg, video/*", "defaultAction": "download"}}]},
          "screens": [{
            "id": "screen1", "name": "Screen1",
            "screenElements": [{
              "type": "InlineRepeat", "id": "repeat1", "name": "Items", "groupRef": "group_items",
              "repeatOverviewColumn": [
                {"type": "FieldBasedRepeatOverviewColumn", "id": "column1", "elementRef": "field_a",
                 "filterable": true, "filterExposition": "STRING", "labelHidden": true, "fixedWidth": true,
                 "specificHorizontalAlignment": {"head": "left", "body": "right"},
                 "specificVerticalAlignment": {"head": "middle", "body": "top"},
                 "headerStyle": [{"name": "narrow"}],
                 "annotations": [{"name": "note", "value": "x"}]}
              ]
            }]
          }]
        }
      }
      """;

  @Test
  void attachmentSettingsAndRemainingColumnFieldsSurviveLoadThenSave() throws Exception {
    FormModel model = load(SECOND_JSON);

    FieldConfigEntry.AttachmentConfig config = model.getContent().getFieldConfiguration().getField().get(0).getAttachmentConfig();
    assertEquals("pdf", config.getPlaceholderIcon());
    assertEquals("image/jpeg, video/*", config.getAccept());
    assertEquals("download", config.getDefaultAction());
    assertFalse(config.isBlank());

    InlineRepeat repeat = assertInstanceOf(InlineRepeat.class, model.getContent().getScreens().get(0).getScreenElements().get(0));
    FieldBasedRepeatOverviewColumn column = assertInstanceOf(FieldBasedRepeatOverviewColumn.class, repeat.getRepeatOverviewColumn().get(0));
    assertEquals("STRING", column.getFilterExposition());
    assertEquals("right", column.getSpecificHorizontalAlignment().getBody());
    assertEquals("top", column.getSpecificVerticalAlignment().getBody());
    assertEquals("narrow", column.getHeaderStyle().get(0).getName());

    JsonNode expected = JsonSettings.objectMapper.readTree(SECOND_JSON);
    JsonNode actual = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model));
    assertEquals(expected.get("content"), actual.get("content"));
  }

  @Test
  void aBlankAttachmentConfigAndAlignmentAreDetected() {
    assertTrue(new FieldConfigEntry.AttachmentConfig().isBlank());
    assertTrue(new Alignment().isBlank());

    Alignment alignment = new Alignment();
    alignment.setHead("left");
    assertFalse(alignment.isBlank());
    alignment.setHead("");
    assertTrue(alignment.isBlank());
  }

  @Test
  void anAlignmentWithOnlyOneSideWritesOnlyThatSide() throws Exception {
    Alignment alignment = new Alignment();
    alignment.setBody("center");

    JsonNode json = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(alignment));

    assertFalse(json.has("head"), "an absent side stays absent instead of becoming null");
    assertEquals("center", json.get("body").asString());
  }

  @Test
  void loadsAllFields() throws Exception {
    FormModel model = load(JSON);

    assertEquals(2, model.getContent().getStyles().size());
    assertEquals("highlight", model.getContent().getStyles().get(1).getName());

    Screen screen = model.getContent().getScreens().get(0);
    InlineRepeat repeat = assertInstanceOf(InlineRepeat.class, screen.getScreenElements().get(0));

    FieldBasedRepeatOverviewColumn fieldColumn = assertInstanceOf(FieldBasedRepeatOverviewColumn.class, repeat.getRepeatOverviewColumn().get(0));
    assertEquals("LEFT", fieldColumn.getPinDirection());
    assertEquals("face", fieldColumn.getIcon().getName());
    assertEquals("field_flag", fieldColumn.getHideCondition().getMasterField());
    ExpressionRepeatOverviewColumn expressionColumn = assertInstanceOf(ExpressionRepeatOverviewColumn.class, repeat.getRepeatOverviewColumn().get(1));
    assertEquals("RIGHT", expressionColumn.getPinDirection());
    assertNull(expressionColumn.getHideCondition());

    RowAction action = repeat.getRowActionGroup().getAction().get(0);
    assertEquals("event_remove", action.getEvent());
    assertEquals("HIDDEN_IN_READONLY_MODE", action.getScope());
    assertEquals("delete", action.getButtonStyling().getIconName());
    assertEquals("PRIMARY", action.getButtonStyling().getPriority());
    assertEquals(Boolean.TRUE, action.getButtonStyling().getDestructive());
    assertEquals("Removes the row", action.getButtonStyling().getDescription().getText().get(0).getText());
    assertEquals("Really remove?", action.getConfirmation().getText().get(0).getText());
    assertEquals("Warning", action.getConfirmationDialogTitle().getText().get(0).getText());
    assertFalse(action.getButtonStyling().isBlank());

    assertEquals(Boolean.TRUE, repeat.getMultiFileUpload());
    MultiFileUploadOptions options = repeat.getMultiFileUploadOptions();
    assertEquals("group_attachments", options.getElementRef());
    assertEquals(Boolean.TRUE, options.getEnableDownload());
    assertEquals("Drop files here", options.getFileUploadDescription().getText().get(0).getText());
    assertEquals("Max. 10 MB", options.getFileUploadHelperText().getText().get(0).getText());
  }

  @Test
  void survivesLoadThenSaveUnchanged() throws Exception {
    JsonNode expected = JsonSettings.objectMapper.readTree(JSON);
    JsonNode actual = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(load(JSON)));

    assertEquals(expected.get("content"), actual.get("content"));
  }

  @Test
  void blankButtonStylingIsDetected() {
    ButtonStyling styling = new ButtonStyling();
    assertTrue(styling.isBlank());

    styling.setIconName("delete");
    assertFalse(styling.isBlank());
    assertEquals("delete", styling.getIconName());

    styling.setIconName("");
    assertNull(styling.getIcon());
    assertTrue(styling.isBlank());

    styling.setPriority("SECONDARY");
    assertFalse(styling.isBlank());
  }

  @Test
  void buttonDelegatesIconAccessorsToItsStyling() {
    EventButton button = new EventButton();
    assertNull(button.getIconName());

    button.setIconName("save");
    ButtonStyling styling = button.getButtonStyling();
    assertNotNull(styling);
    assertEquals("save", styling.getIconName());
    assertEquals("save", button.getIconName());

    button.setIconName(null);
    assertNull(button.getIconName());
  }

  @Test
  void multiFileUploadOptionsWithoutElementRefStayWithoutIt() throws Exception {
    MultiFileUploadOptions options = new MultiFileUploadOptions();
    options.setEnableDownload(true);

    JsonNode json = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(options));
    assertFalse(json.has("elementRef"));
    assertTrue(json.get("enableDownload").asBoolean());
  }

  private static FormModel load(String json) throws Exception {
    return JsonSettings.objectMapper.readValue(json, FormModel.class);
  }
}
