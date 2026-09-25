package de.a12.studio.ui.editors.typesettingmodel;

import de.a12.studio.models.typesettingmodel.PreventLineBreakRule;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRules;
import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.models.typesettingmodel.TypesettingModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.typesetting.TypesettingElementIds;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared behavior of the three "Prevent Line Break Rule" tables of a {@link TypesettingModel}
 * ({@link CharacterSequenceRulesPanelController}, {@link NumberUnitRulesPanelController}, {@link
 * SpecialPatternRulesPanelController}): all three edit one list, {@code content.preventLineBreakRules}, each
 * showing the rules of its own {@link PreventLineBreakRuleType} (told apart by {@link
 * PreventLineBreakRules#classify}). Every row is one value control (a text field, or a choice for the special
 * patterns) plus a delete button; "Add" appends a still-empty rule of this panel's kind.
 *
 * <p>Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so this follows the model-header
 * pattern of {@code SelectionCategoryPanelController}: plain listeners and {@link #commitHeaderChange()}
 * instead of the element-bound {@code bind*} helpers (whose commit would hide this panel's own error container).
 * Since the panels share the list, a row is looked up by its rule object rather than by a cached list position,
 * which shifts whenever another panel deletes a rule.
 */
public abstract class AbstractRulesPanelController extends AbstractPropertyEditor {

  private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

  private static final int COMMIT_DEBOUNCE_MS = 150;

  @FXML
  private GridPane rulesGrid;

  @FXML
  private Label emptyLabel;

  private final Debouncer debouncer = new Debouncer();

  // The value control of every row currently shown, in row order.
  private final Map<PreventLineBreakRule, Node> valueControls = new LinkedHashMap<>();

  private TypesettingModel model;

  @Override
  public void destroy() {
    debouncer.shutdown();
    super.destroy();
  }

  /** The kind of rule this panel lists and adds. */
  protected abstract PreventLineBreakRuleType ruleType();

  /**
   * Builds the control that edits {@code rule}'s value. It must show the rule's current value without
   * reporting that as an edit, and report every user edit through {@link #onValueEdited}.
   */
  protected abstract Node createValueControl(@NonNull PreventLineBreakRule rule);

  public void setModel(@NonNull TypesettingModel model) {
    this.model = model;
    rebuildRows();
    refreshValidation();
  }

  @FXML
  private void onAdd() {
    getRules().add(new PreventLineBreakRule(ruleType().emptyPattern()));
    changed();
  }

  /** Applies {@code value}, as edited in {@code rule}'s row, to the rule and saves (debounced while typing). */
  protected final void onValueEdited(@NonNull PreventLineBreakRule rule, String value) {
    rule.setPattern(PreventLineBreakRules.toPattern(ruleType(), value));
    debouncer.debounce(getClass().getName() + "#" + System.identityHashCode(rule), this::commitAndRefresh, COMMIT_DEBOUNCE_MS, true);
  }

  private List<PreventLineBreakRule> getRules() {
    TypesettingModelContent content = model.getContent();
    if (content.getPreventLineBreakRules() == null) {
      content.setPreventLineBreakRules(new ArrayList<>());
    }
    return content.getPreventLineBreakRules();
  }

  private void rebuildRows() {
    rulesGrid.getChildren().clear();
    valueControls.clear();

    int row = 0;
    for (PreventLineBreakRule rule : getRules()) {
      if (PreventLineBreakRules.classify(rule.getPattern()) != ruleType()) {
        continue;
      }
      Node valueControl = createValueControl(rule);
      valueControl.setId(getClass().getSimpleName() + "-row-" + row);
      valueControls.put(rule, valueControl);

      Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
            StudioBundle.get("typesetting_model.delete_this_rule"), null, null, "Delete");
        if (result.isPresent() && result.get() == ButtonType.OK) {
          getRules().remove(rule);
          changed();
        }
      });
      HBox actionsBox = new HBox(4.0, deleteButton);
      actionsBox.setAlignment(Pos.CENTER_LEFT);

      rulesGrid.addRow(row++, valueControl, actionsBox);
    }

    boolean empty = row == 0;
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    rulesGrid.setVisible(!empty);
    rulesGrid.setManaged(!empty);
  }

  private void changed() {
    rebuildRows();
    commitAndRefresh();
  }

  private void commitAndRefresh() {
    commitHeaderChange();
    refreshValidation();
  }

  /**
   * Re-checks the model's typesetting validators and shows the first problem of this panel's rules in its own
   * error container, marking every offending row - same convention as {@code
   * SelectionCategoryPanelController#refreshValidation}.
   */
  private void refreshValidation() {
    if (model == null || Studio.getSelectedProjectItem() == null) {
      hideError();
      return;
    }
    List<ModelValidationError> errors = Studio.getValidationService().validate(model);
    List<PreventLineBreakRule> rules = getRules();

    List<ModelValidationError> panelErrors = new ArrayList<>();
    valueControls.forEach((rule, valueControl) -> {
      String elementId = TypesettingElementIds.rule(indexOf(rules, rule));
      List<ModelValidationError> ruleErrors = errors.stream().filter(error -> elementId.equals(error.elementId())).toList();
      markError(valueControl, !ruleErrors.isEmpty());
      panelErrors.addAll(ruleErrors);
    });

    if (panelErrors.isEmpty()) {
      hideError();
    }
    else {
      ModelValidationError first = panelErrors.get(0);
      showError(first.severity(), first.message());
    }
  }

  // By identity (the rules have no equals), see the class doc.
  private static int indexOf(List<PreventLineBreakRule> rules, PreventLineBreakRule rule) {
    for (int index = 0; index < rules.size(); index++) {
      if (rules.get(index) == rule) {
        return index;
      }
    }
    return -1;
  }

  private static void markError(Node valueControl, boolean error) {
    valueControl.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
    if (valueControl instanceof ComboBox<?>) {
      // combo boxes are styled through a style class instead of the pseudo-class, see stylesheet-combobox.css
      valueControl.getStyleClass().remove("validation-error");
      if (error) {
        valueControl.getStyleClass().add("validation-error");
      }
    }
  }
}
