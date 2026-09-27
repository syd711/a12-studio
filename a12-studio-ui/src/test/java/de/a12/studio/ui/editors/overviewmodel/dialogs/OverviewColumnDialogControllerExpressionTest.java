package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Gap 8 ("Overview Model: gap review") - the Column dialog's OK button used to enable for an expression
 * column with a blank Name/Expression, and its {@link de.a12.studio.ui.editors.propertyeditors.RuleEditorController}
 * had no syntax validator at all.
 */
class OverviewColumnDialogControllerExpressionTest {

  private static final String FXML = "/de/a12/studio/ui/editors/overviewmodel/dialogs/overview-column-dialog.fxml";

  @Test
  void okStaysDisabledUntilNameAndAValidExpressionAreBothFilled() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    FxTestSupport.Loaded<OverviewColumnDialogController> loaded = FxTestSupport.load(FXML);
    OverviewColumnDialogController controller = loaded.controller();
    Stage stage = FxTestSupport.onFx(() -> new Stage());

    Column column = new Column();
    column.setId("column_1");
    column.setWidth(1.0);
    FxTestSupport.onFx(() -> controller.init(stage, null, null, column, reference -> null));

    Button okButton = FxTestSupport.field(controller, "okButton");
    ComboBox<String> columnTypeCombo = FxTestSupport.field(controller, "columnTypeCombo");
    TextField nameField = FxTestSupport.field(controller, "nameField");
    de.a12.studio.ui.editors.propertyeditors.RuleEditorController expressionPanelController =
        FxTestSupport.field(controller, "expressionPanelController");
    CodeArea codeArea = FxTestSupport.field(expressionPanelController, "codeArea");

    // Reference type with no elementRef picked yet: OK is disabled (pre-existing behavior).
    assertTrue(FxTestSupport.onFx(okButton::isDisabled));

    FxTestSupport.onFx(() -> columnTypeCombo.setValue("expression"));
    assertTrue(FxTestSupport.onFx(okButton::isDisabled), "blank Name and Expression must not enable OK");

    FxTestSupport.onFx(() -> nameField.setText("MyExpression"));
    assertTrue(FxTestSupport.onFx(okButton::isDisabled), "Expression is still blank");

    FxTestSupport.onFx(() -> codeArea.replaceText("kontext(City){"));
    Thread.sleep(300); // the expression is written/validated debounced (RuleEditorController.SAVE_DEBOUNCE)
    assertTrue(FxTestSupport.onFx(okButton::isDisabled), "the expression has a syntax error");

    FxTestSupport.onFx(() -> codeArea.replaceText("[City]"));
    Thread.sleep(300);
    assertFalse(FxTestSupport.onFx(okButton::isDisabled), "Name and a syntactically valid Expression are set");

    FxTestSupport.onFx(controller::destroy);
  }
}
