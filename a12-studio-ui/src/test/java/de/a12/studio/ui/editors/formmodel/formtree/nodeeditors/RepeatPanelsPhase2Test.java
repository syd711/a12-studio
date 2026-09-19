package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Alignment;
import de.a12.studio.models.formmodel.DefaultRowAction;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.ExpressionRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.RowAction;
import de.a12.studio.models.formmodel.RowActionGroup;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.modelsvalidation.validators.form.DefaultRowActionSupport;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Panels added for the attachment settings, the remaining column fields and the default row action, driven
// against real FXML on the JavaFX thread.
class RepeatPanelsPhase2Test {

  private static final String DIR = "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/";

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private static RowAction action(String event) {
    RowAction action = new RowAction();
    action.setEvent(event);
    return action;
  }

  private static <T extends AbstractRepeat> T withActions(T repeat, String... events) {
    repeat.setId("repeat1");
    RowActionGroup group = new RowActionGroup();
    for (String event : events) {
      group.getAction().add(action(event));
    }
    repeat.setRowActionGroup(group);
    return repeat;
  }

  // ---- attachment settings ----

  @Test
  void attachmentSettingsWriteOnlyWhatWasChangedAndPruneWhenBackToDefault() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    AttachmentSettingsPanelController panel =
        FxTestSupport.<AttachmentSettingsPanelController>load(DIR + "attachment-settings-panel.fxml").controller();
    FieldConfigEntry entry = new FieldConfigEntry();
    entry.setElementRef("group_attachments");
    FxTestSupport.onFx(() -> panel.setEntry(entry));
    ComboBox<String> icon = FxTestSupport.field(panel, "placeholderIconCombo");
    ComboBox<String> action = FxTestSupport.field(panel, "defaultActionCombo");
    TextField accept = FxTestSupport.field(panel, "acceptField");

    assertNull(entry.getAttachmentConfig(), "opening the panel must not create an attachmentConfig");

    FxTestSupport.onFx(() -> icon.setValue("pdf"));
    FxTestSupport.onFx(() -> action.setValue("download"));
    FxTestSupport.onFx(() -> accept.setText(" image/jpeg, video/* "));
    assertEquals("pdf", entry.getAttachmentConfig().getPlaceholderIcon());
    assertEquals("download", entry.getAttachmentConfig().getDefaultAction());
    assertEquals("image/jpeg, video/*", entry.getAttachmentConfig().getAccept());

    FxTestSupport.onFx(() -> icon.setValue(null));
    FxTestSupport.onFx(() -> action.setValue(null));
    assertNotNull(entry.getAttachmentConfig(), "accept is still set");
    FxTestSupport.onFx(() -> accept.setText(""));
    assertNull(entry.getAttachmentConfig(), "no setting left: the empty attachmentConfig is dropped");
  }

  @Test
  void attachmentSettingsShowTheDefaultForExplicitDefaultValues() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    AttachmentSettingsPanelController panel =
        FxTestSupport.<AttachmentSettingsPanelController>load(DIR + "attachment-settings-panel.fxml").controller();
    FieldConfigEntry entry = new FieldConfigEntry();
    FieldConfigEntry.AttachmentConfig config = new FieldConfigEntry.AttachmentConfig();
    config.setPlaceholderIcon("default");
    config.setDefaultAction("replace");
    entry.setAttachmentConfig(config);

    FxTestSupport.onFx(() -> panel.setEntry(entry));

    ComboBox<String> icon = FxTestSupport.field(panel, "placeholderIconCombo");
    ComboBox<String> action = FxTestSupport.field(panel, "defaultActionCombo");
    assertNull(icon.getValue());
    assertNull(action.getValue());
    assertEquals("default", entry.getAttachmentConfig().getPlaceholderIcon(), "only displaying must not rewrite the file");
  }

  // ---- column display / alignment ----

  @Test
  void displayPanelEditsTheFlagsAndOnlyEnablesFilterExpositionForFilterableColumns() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RepeatColumnDisplayPanelController panel =
        FxTestSupport.<RepeatColumnDisplayPanelController>load(DIR + "repeat-column-display-panel.fxml").controller();
    FieldBasedRepeatOverviewColumn column = new FieldBasedRepeatOverviewColumn();
    FxTestSupport.onFx(() -> panel.setColumn(column));
    CheckBox labelHidden = FxTestSupport.field(panel, "labelHiddenCheckBox");
    CheckBox fixedWidth = FxTestSupport.field(panel, "fixedWidthCheckBox");
    ComboBox<String> exposition = FxTestSupport.field(panel, "filterExpositionCombo");

    assertTrue(exposition.isDisabled(), "not filterable");

    FxTestSupport.onFx(() -> {
      labelHidden.setSelected(true);
      fixedWidth.setSelected(true);
    });
    assertEquals(Boolean.TRUE, column.getLabelHidden());
    assertEquals(Boolean.TRUE, column.getFixedWidth());

    column.setFilterable(true);
    FxTestSupport.onFx(panel::refreshFilterable);
    assertFalse(exposition.isDisabled());
    FxTestSupport.onFx(() -> exposition.setValue("STRING"));
    assertEquals("STRING", column.getFilterExposition());

    FxTestSupport.onFx(() -> {
      labelHidden.setSelected(false);
      exposition.setValue(null);
    });
    assertNull(column.getLabelHidden(), "unchecked means absent, not false");
    assertNull(column.getFilterExposition());
  }

  @Test
  void alignmentPanelWritesEachSideAndRemovesEmptyOverrides() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RepeatColumnAlignmentPanelController panel =
        FxTestSupport.<RepeatColumnAlignmentPanelController>load(DIR + "repeat-column-alignment-panel.fxml").controller();
    ExpressionRepeatOverviewColumn column = new ExpressionRepeatOverviewColumn();
    FxTestSupport.onFx(() -> panel.setColumn(column));
    ComboBox<String> headHorizontal = FxTestSupport.field(panel, "headHorizontalCombo");
    ComboBox<String> bodyHorizontal = FxTestSupport.field(panel, "bodyHorizontalCombo");
    ComboBox<String> bodyVertical = FxTestSupport.field(panel, "bodyVerticalCombo");

    assertNull(column.getSpecificHorizontalAlignment());

    FxTestSupport.onFx(() -> {
      headHorizontal.setValue("center");
      bodyVertical.setValue("top");
    });
    Alignment horizontal = column.getSpecificHorizontalAlignment();
    assertEquals("center", horizontal.getHead());
    assertNull(horizontal.getBody());
    assertEquals("top", column.getSpecificVerticalAlignment().getBody());

    FxTestSupport.onFx(() -> bodyHorizontal.setValue("right"));
    assertEquals("right", column.getSpecificHorizontalAlignment().getBody());

    FxTestSupport.onFx(() -> {
      headHorizontal.setValue(null);
      bodyHorizontal.setValue(null);
    });
    assertNull(column.getSpecificHorizontalAlignment(), "both sides default again: no empty {} left behind");
    assertNotNull(column.getSpecificVerticalAlignment());
  }

  // ---- default row action ----

  @Test
  void defaultRowActionPanelOffersTheCandidatesAndWritesTheWireShape() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RepeatDefaultRowActionPanelController panel =
        FxTestSupport.<RepeatDefaultRowActionPanelController>load(DIR + "repeat-default-row-action-panel.fxml").controller();
    DetachedRepeat repeat = withActions(new DetachedRepeat(), "event_a", "event_b");
    FxTestSupport.onFx(() -> panel.setRepeat(repeat));
    ComboBox<String> combo = FxTestSupport.field(panel, "eventCombo");
    CheckBox hideButton = FxTestSupport.field(panel, "hideButtonCheckBox");

    assertEquals(java.util.Arrays.asList(null, DefaultRowActionSupport.EDIT, "event_a", "event_b"), combo.getItems());
    assertTrue(hideButton.isDisabled(), "nothing to hide while there is no default");

    FxTestSupport.onFx(() -> combo.setValue("event_b"));
    assertEquals("event_b", repeat.getDefaultRowAction().getEvent());
    assertEquals(Boolean.TRUE, repeat.getDefaultRowAction().getCustom());

    FxTestSupport.onFx(() -> hideButton.setSelected(true));
    assertEquals(Boolean.TRUE, repeat.getDefaultRowAction().getHideButton());

    FxTestSupport.onFx(() -> combo.setValue(DefaultRowActionSupport.EDIT));
    assertEquals("edit", repeat.getDefaultRowAction().getEvent());
    assertNull(repeat.getDefaultRowAction().getCustom(), "a built-in action is not marked custom");
    assertEquals(Boolean.TRUE, repeat.getDefaultRowAction().getHideButton(), "switching the action keeps hide-button");

    FxTestSupport.onFx(() -> combo.setValue(null));
    assertNull(repeat.getDefaultRowAction());
  }

  @Test
  void defaultRowActionPanelIsHiddenForInlineRepeats() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RepeatDefaultRowActionPanelController panel =
        FxTestSupport.<RepeatDefaultRowActionPanelController>load(DIR + "repeat-default-row-action-panel.fxml").controller();
    TitledPane root = FxTestSupport.field(panel, "root");

    FxTestSupport.onFx(() -> panel.setRepeat(new InlineRepeat()));
    assertFalse(root.isVisible());

    FxTestSupport.onFx(() -> panel.setRepeat(new EmbeddedRepeat()));
    assertTrue(root.isVisible());
  }

  @Test
  void renamingOrDeletingTheDefaultsRowActionInTheRepeatEditorKeepsTheDefaultValid() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormNodeEditorRepeatPanelController editor =
        FxTestSupport.<FormNodeEditorRepeatPanelController>load(DIR + "formnode-editor-repeat-panel.fxml").controller();
    DetachedRepeat repeat = withActions(new DetachedRepeat(), "event_a", "event_b");
    DefaultRowAction defaultAction = DefaultRowActionSupport.fromTechnicalEvent("event_b", null);
    repeat.setDefaultRowAction(defaultAction);
    FxTestSupport.onFx(() -> editor.setRepeat(repeat, null, null, null,
        HideConditionPanelController.MasterFieldScope.root()));

    RepeatRowActionsPanelController rows = FxTestSupport.field(editor, "rowActionsController");
    RepeatDefaultRowActionPanelController defaults = FxTestSupport.field(editor, "defaultRowActionController");
    TableView<RowAction> table = FxTestSupport.field(rows, "actionsTable");
    TableColumn<RowAction, String> eventColumn = FxTestSupport.field(rows, "eventColumn");
    ComboBox<String> combo = FxTestSupport.field(defaults, "eventCombo");
    assertEquals("event_b", combo.getValue());

    // Rename event_b in the table: the default follows and the combo shows the new name.
    FxTestSupport.onFx(() -> commitEvent(table, eventColumn, 1, "event_renamed"));
    assertEquals("event_renamed", repeat.getDefaultRowAction().getEvent());
    assertEquals("event_renamed", combo.getValue());
    assertTrue(combo.getItems().contains("event_renamed"));

    // Delete that row action: the default is cleared.
    Button removeButton = FxTestSupport.field(rows, "removeButton");
    FxTestSupport.onFx(() -> {
      table.getSelectionModel().select(1);
      removeButton.fire();
    });
    assertNull(repeat.getDefaultRowAction());
    assertNull(combo.getValue());
    assertEquals(List.of(DefaultRowActionSupport.EDIT, "event_a"), combo.getItems().stream().filter(item -> item != null).toList());
  }

  private static void commitEvent(TableView<RowAction> table, TableColumn<RowAction, String> column, int row, String newValue) {
    TableColumn.CellEditEvent<RowAction, String> edit = new TableColumn.CellEditEvent<>(table,
        new javafx.scene.control.TablePosition<>(table, row, column), TableColumn.editCommitEvent(), newValue);
    column.getOnEditCommit().handle(edit);
  }
}
