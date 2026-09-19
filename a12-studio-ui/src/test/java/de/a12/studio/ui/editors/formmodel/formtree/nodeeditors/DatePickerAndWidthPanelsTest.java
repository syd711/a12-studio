package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.DatePickerConfig;
import de.a12.studio.models.formmodel.ExpressionRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The date picker range panel and the fractional column width, against real FXML on the JavaFX thread.
class DatePickerAndWidthPanelsTest {

  private static final String DIR = "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/";

  private static final String DOCUMENT_MODEL = """
      {
        "header": {"id": "Dates_DM", "modelType": "document", "modelVersion": "29.4.0"},
        "content": {
          "modelInfo": {"name": "Dates_DM", "immutable": false},
          "modelConfig": {"timeZone": "UTC", "decimalSeparator": ".", "conditionLanguage": {"code": "en_US"}},
          "modelRoot": {"rootGroups": [{
            "type": "Group", "id": "group_root", "name": "Root",
            "Group": {"repeatability": 1, "elements": [
              {"type": "Field", "id": "field_date", "name": "Birthday", "Field": {"fieldType": {"type": "DateType"}}},
              {"type": "Field", "id": "field_text", "name": "Name", "Field": {"fieldType": {"type": "StringType"}}}
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

  private static DatePickerConfigPanelController loadPanel(DatePickerConfig[] holder) throws Exception {
    DatePickerConfigPanelController panel =
        FxTestSupport.<DatePickerConfigPanelController>load(DIR + "date-picker-config-panel.fxml").controller();
    FxTestSupport.onFx(() -> panel.setConfig(() -> holder[0], config -> holder[0] = config));
    return panel;
  }

  // ---- date picker panel ----

  @Test
  void theConfigIsCreatedOnTheFirstEditAndDroppedWhenItCarriesNothing() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    DatePickerConfig[] holder = new DatePickerConfig[1];
    DatePickerConfigPanelController panel = loadPanel(holder);
    TextField min = FxTestSupport.field(panel, "minYearField");
    TextField max = FxTestSupport.field(panel, "maxYearField");
    TextField preselection = FxTestSupport.field(panel, "preselectionYearField");
    CheckBox absolute = FxTestSupport.field(panel, "absoluteCheckBox");

    assertNull(holder[0], "opening the panel creates nothing");

    FxTestSupport.onFx(() -> min.setText("-70"));
    FxTestSupport.onFx(() -> max.setText("-18"));
    FxTestSupport.onFx(() -> preselection.setText("-34"));
    assertEquals(-70, holder[0].getMinYear());
    assertEquals(-18, holder[0].getMaxYear());
    assertEquals(-34, holder[0].getPreselectionYear());
    assertNull(holder[0].getAbsolute(), "unchecked is written as absent");
    assertFalse(panel.errorProperty().get());

    FxTestSupport.onFx(() -> absolute.setSelected(true));
    assertEquals(Boolean.TRUE, holder[0].getAbsolute());

    FxTestSupport.onFx(() -> {
      min.setText("");
      max.setText("");
      preselection.setText("");
      absolute.setSelected(false);
    });
    assertNull(holder[0], "nothing left: the empty datePickerConfig is dropped");
  }

  @Test
  void anExplicitAbsoluteFalseIsNotRewrittenByOpeningThePanel() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    DatePickerConfig config = new DatePickerConfig();
    config.setMinYear(-7);
    config.setAbsolute(false);
    DatePickerConfig[] holder = {config};

    DatePickerConfigPanelController panel = loadPanel(holder);
    CheckBox absolute = FxTestSupport.field(panel, "absoluteCheckBox");
    TextField min = FxTestSupport.field(panel, "minYearField");

    assertFalse(absolute.isSelected());
    assertEquals("-7", min.getText());
    assertEquals(Boolean.FALSE, holder[0].getAbsolute());
  }

  @Test
  void theRangeRulesAreShownAsYouType() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    DatePickerConfig[] holder = new DatePickerConfig[1];
    DatePickerConfigPanelController panel = loadPanel(holder);
    TextField min = FxTestSupport.field(panel, "minYearField");
    TextField max = FxTestSupport.field(panel, "maxYearField");

    FxTestSupport.onFx(() -> max.setText("2"));
    FxTestSupport.onFx(() -> min.setText("5"));

    assertTrue(panel.errorProperty().get(), "min above max");
    assertEquals(5, holder[0].getMinYear(), "the value is still written - the validator reports it too");

    FxTestSupport.onFx(() -> min.setText("1"));
    assertFalse(panel.errorProperty().get());
  }

  @Test
  void aNonNumericYearIsReportedAndNotWritten() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    DatePickerConfig[] holder = new DatePickerConfig[1];
    DatePickerConfigPanelController panel = loadPanel(holder);
    TextField min = FxTestSupport.field(panel, "minYearField");

    FxTestSupport.onFx(() -> min.setText("abc"));

    assertTrue(panel.errorProperty().get());
    assertNull(holder[0]);
  }

  // ---- visibility in the Control and column editors ----

  @Test
  void theControlEditorOffersTheDatePickerOnlyForDateFields() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormNodeEditorControlPanelController editor =
        FxTestSupport.<FormNodeEditorControlPanelController>load(DIR + "formnode-editor-control-panel.fxml").controller();
    DatePickerConfigPanelController panel = FxTestSupport.field(editor, "datePickerController");
    TitledPane root = FxTestSupport.field(panel, "root");
    Control dateControl = new Control();
    dateControl.setId("control_date");
    dateControl.setElementRef("field_date");
    Control textControl = new Control();
    textControl.setId("control_text");
    textControl.setElementRef("field_text");

    FxTestSupport.onFx(() -> editor.setControl(dateControl, null, index(), new FormModelContent()));
    assertTrue(root.isVisible());

    FxTestSupport.onFx(() -> editor.setControl(textControl, null, index(), new FormModelContent()));
    assertFalse(root.isVisible());
  }

  @Test
  void theColumnEditorOffersTheDatePickerOnlyForDateColumns() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormNodeEditorRepeatOverviewColumnPanelController editor = FxTestSupport.<FormNodeEditorRepeatOverviewColumnPanelController>load(
        DIR + "formnode-editor-repeat-overview-column-panel.fxml").controller();
    DatePickerConfigPanelController panel = FxTestSupport.field(editor, "datePickerController");
    TitledPane root = FxTestSupport.field(panel, "root");
    FieldBasedRepeatOverviewColumn dateColumn = new FieldBasedRepeatOverviewColumn();
    dateColumn.setId("c1");
    dateColumn.setElementRef("field_date");
    FieldBasedRepeatOverviewColumn textColumn = new FieldBasedRepeatOverviewColumn();
    textColumn.setId("c2");
    textColumn.setElementRef("field_text");
    ExpressionRepeatOverviewColumn expressionColumn = new ExpressionRepeatOverviewColumn();
    expressionColumn.setId("c3");
    HideConditionPanelController.MasterFieldScope scope = HideConditionPanelController.MasterFieldScope.root();

    FxTestSupport.onFx(() -> editor.setColumn(dateColumn, index(), scope));
    assertTrue(root.isVisible());
    FxTestSupport.onFx(() -> editor.setColumn(textColumn, index(), scope));
    assertFalse(root.isVisible());
    FxTestSupport.onFx(() -> editor.setColumn(expressionColumn, index(), scope));
    assertFalse(root.isVisible());

    // and the panel edits the column it was shown for
    FxTestSupport.onFx(() -> editor.setColumn(dateColumn, index(), scope));
    TextField min = FxTestSupport.field(panel, "minYearField");
    FxTestSupport.onFx(() -> min.setText("-5"));
    assertNotNull(dateColumn.getDatePickerConfig());
    assertEquals(-5, dateColumn.getDatePickerConfig().getMinYear());
    assertNull(textColumn.getDatePickerConfig());
  }

  // ---- fractional column width ----

  @Test
  void theWidthAcceptsOneDecimalAndReportsEverythingElse() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormNodeEditorRepeatOverviewColumnPanelController editor = FxTestSupport.<FormNodeEditorRepeatOverviewColumnPanelController>load(
        DIR + "formnode-editor-repeat-overview-column-panel.fxml").controller();
    ExpressionRepeatOverviewColumn column = new ExpressionRepeatOverviewColumn();
    column.setId("c1");
    column.setWidth(0.8);
    FxTestSupport.onFx(() -> editor.setColumn(column, null, HideConditionPanelController.MasterFieldScope.root()));
    TextField widthField = FxTestSupport.field(editor, "widthField");

    assertEquals("0.8", widthField.getText(), "a fractional width is shown as stored, not truncated to 0");

    FxTestSupport.onFx(() -> widthField.setText("1,5"));
    assertEquals(1.5, column.getWidth(), "a decimal comma is accepted");
    assertFalse(editor.errorProperty().get());

    FxTestSupport.onFx(() -> widthField.setText("2"));
    assertEquals(2.0, column.getWidth());

    FxTestSupport.onFx(() -> widthField.setText("0.2"));
    assertEquals(2.0, column.getWidth(), "below 0.3: not written");
    assertTrue(editor.errorProperty().get());

    FxTestSupport.onFx(() -> widthField.setText("0.85"));
    assertEquals(2.0, column.getWidth(), "two decimals: not written");
    assertTrue(editor.errorProperty().get());

    FxTestSupport.onFx(() -> widthField.setText("wide"));
    assertEquals(2.0, column.getWidth());
    assertTrue(editor.errorProperty().get());

    FxTestSupport.onFx(() -> widthField.setText("0.3"));
    assertEquals(0.3, column.getWidth());
    assertFalse(editor.errorProperty().get());

    FxTestSupport.onFx(() -> widthField.setText(""));
    assertNull(column.getWidth(), "blank clears it - the default is 1.0");
    assertFalse(editor.errorProperty().get());
  }
}
