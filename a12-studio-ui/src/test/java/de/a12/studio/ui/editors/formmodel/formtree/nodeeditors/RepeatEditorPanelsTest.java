package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.ExpressionRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.MultiFileUploadOptions;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TitledPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Drives the Form Model repeat editor panels added for the "modeled but no UI" fields against real FXML, on the
// JavaFX thread: what a user's click in the panel does to the repeat / column it edits.
class RepeatEditorPanelsTest {

  private static final String DOCUMENT_MODEL = """
      {
        "header": {"id": "Upload_DM", "modelType": "document", "modelVersion": "29.4.0"},
        "content": {
          "modelInfo": {"name": "Upload_DM", "immutable": false},
          "modelConfig": {"timeZone": "UTC", "decimalSeparator": ".", "conditionLanguage": {"code": "en_US"}},
          "modelRoot": {"rootGroups": [{
            "type": "Group", "id": "group_root", "name": "Root",
            "Group": {"repeatability": 1, "elements": [
              {"type": "Group", "id": "group_docs", "name": "Documents",
               "Group": {"repeatability": 10, "elements": [
                 {"type": "Group", "id": "group_att", "name": "attachments", "Group": {"repeatability": 1, "usageType": "attachment"}}]}},
              {"type": "Group", "id": "group_plain", "name": "Plain",
               "Group": {"repeatability": 10, "elements": [
                 {"type": "Field", "id": "field_x", "name": "X", "Field": {"fieldType": {"type": "StringType"}}}]}}
            ]}
          }]}
        }
      }
      """;

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private static ElementIndex index() {
    return new ElementIndex(JsonSettings.objectMapper.readValue(DOCUMENT_MODEL, DocumentModel.class));
  }

  private static InlineRepeat inlineRepeat(String groupRef) {
    InlineRepeat repeat = new InlineRepeat();
    repeat.setId("repeat1");
    repeat.setGroupRef(groupRef);
    return repeat;
  }

  @Test
  void enablingMultiFileUploadBindsTheSingleAttachmentGroup() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RepeatMultiFileUploadPanelController panel = FxTestSupport.<RepeatMultiFileUploadPanelController>load(
        "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/repeat-multi-file-upload-panel.fxml").controller();
    InlineRepeat repeat = inlineRepeat("group_docs");
    FxTestSupport.onFx(() -> panel.setRepeat(repeat, index()));
    CheckBox enable = FxTestSupport.field(panel, "enableCheckBox");

    FxTestSupport.onFx(() -> enable.setSelected(true));

    assertEquals(Boolean.TRUE, repeat.getMultiFileUpload());
    assertNotNull(repeat.getMultiFileUploadOptions());
    assertEquals("group_att", repeat.getMultiFileUploadOptions().getElementRef());

    // Editing an option keeps the options object; disabling only clears the switch.
    CheckBox download = FxTestSupport.field(panel, "enableDownloadCheckBox");
    FxTestSupport.onFx(() -> download.setSelected(true));
    assertEquals(Boolean.TRUE, repeat.getMultiFileUploadOptions().getEnableDownload());

    FxTestSupport.onFx(() -> enable.setSelected(false));
    assertNull(repeat.getMultiFileUpload());
    MultiFileUploadOptions kept = repeat.getMultiFileUploadOptions();
    assertNotNull(kept);
    assertEquals(Boolean.TRUE, kept.getEnableDownload());
  }

  @Test
  void multiFileUploadIsRefusedWithoutASingleAttachmentGroup() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RepeatMultiFileUploadPanelController panel = FxTestSupport.<RepeatMultiFileUploadPanelController>load(
        "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/repeat-multi-file-upload-panel.fxml").controller();
    InlineRepeat repeat = inlineRepeat("group_plain");
    FxTestSupport.onFx(() -> panel.setRepeat(repeat, index()));
    CheckBox enable = FxTestSupport.field(panel, "enableCheckBox");

    FxTestSupport.onFx(() -> enable.setSelected(true));

    assertFalse(enable.isSelected(), "the switch must fall back to off");
    assertNull(repeat.getMultiFileUpload());
    assertNull(repeat.getMultiFileUploadOptions(), "no options pointing at no attachment group may be written");
  }

  @Test
  void multiFileUploadPanelIsHiddenForDetachedRepeats() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RepeatMultiFileUploadPanelController panel = FxTestSupport.<RepeatMultiFileUploadPanelController>load(
        "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/repeat-multi-file-upload-panel.fxml").controller();
    TitledPane root = FxTestSupport.field(panel, "root");

    FxTestSupport.onFx(() -> panel.setRepeat(new DetachedRepeat(), index()));
    assertFalse(root.isVisible());
    assertFalse(root.isManaged());

    FxTestSupport.onFx(() -> panel.setRepeat(inlineRepeat("group_docs"), index()));
    assertTrue(root.isVisible());
  }

  @Test
  void pinDirectionPanelWritesAndClearsThePinDirection() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RepeatColumnPinDirectionPanelController panel = FxTestSupport.<RepeatColumnPinDirectionPanelController>load(
        "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/repeat-column-pin-direction-panel.fxml").controller();
    FieldBasedRepeatOverviewColumn column = new FieldBasedRepeatOverviewColumn();
    column.setPinDirection("RIGHT");
    FxTestSupport.onFx(() -> panel.setColumn(column));
    ComboBox<String> combo = FxTestSupport.field(panel, "pinDirectionCombo");

    assertEquals("RIGHT", combo.getValue());

    FxTestSupport.onFx(() -> combo.setValue("LEFT"));
    assertEquals("LEFT", column.getPinDirection());

    FxTestSupport.onFx(() -> combo.setValue(null));
    assertNull(column.getPinDirection());
  }

  @Test
  void columnEditorBindsBothColumnTypes() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormNodeEditorRepeatOverviewColumnPanelController editor = FxTestSupport.<FormNodeEditorRepeatOverviewColumnPanelController>load(
        "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/formnode-editor-repeat-overview-column-panel.fxml").controller();

    FieldBasedRepeatOverviewColumn fieldColumn = new FieldBasedRepeatOverviewColumn();
    fieldColumn.setId("column1");
    fieldColumn.setPinDirection("LEFT");
    ExpressionRepeatOverviewColumn expressionColumn = new ExpressionRepeatOverviewColumn();
    expressionColumn.setId("column2");
    expressionColumn.setExpression("[A] + [B]");

    FxTestSupport.onFx(() -> editor.setColumn(fieldColumn, null, HideConditionPanelController.MasterFieldScope.root()));
    FxTestSupport.onFx(() -> editor.setColumn(expressionColumn, null, HideConditionPanelController.MasterFieldScope.root()));

    RepeatColumnPinDirectionPanelController pin = FxTestSupport.field(editor, "pinDirectionController");
    ComboBox<String> pinCombo = FxTestSupport.field(pin, "pinDirectionCombo");
    // Rebinding to the expression column must show its (absent) pin direction, not leak the previous column's.
    assertNull(pinCombo.getValue());
    assertEquals("LEFT", fieldColumn.getPinDirection());
    assertEquals("[A] + [B]", expressionColumn.getExpression());
  }
}
