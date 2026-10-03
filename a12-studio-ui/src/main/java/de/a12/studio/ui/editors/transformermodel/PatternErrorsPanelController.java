package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.transformermodel.PatternError;
import de.a12.studio.models.transformermodel.PatternErrorAction;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerElementIds;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * The "String Pattern Error Messages" section of the Custom Texts tab ({@code content.PatternErrors}): for every
 * generated String field whose pattern equals the entry's pattern, what the transformer's pattern post-processing
 * does ({@link PatternErrorAction}: update the message, replace the pattern, or drop it) and the localized error
 * messages - one text per locale of the model. The pattern is suggested from the XSD's patterns (SME's
 * {@code xsd:patterns}), any text accepted; the replacement is only offered for {@code REPLACE}, and switching
 * to another action clears it (what SME's dependent field does).
 */
public class PatternErrorsPanelController extends TransformerModelPanelController {

  @FXML
  private VBox rows;

  @FXML
  private Label emptyLabel;

  private List<String> patterns = List.of();

  /** The regex strings of the XSD's String fields (SME's {@code xsd:patterns}). */
  public void setPatterns(@NonNull List<String> patterns) {
    this.patterns = patterns;
    refreshSuggestions();
  }

  @Override
  protected String errorIdPrefix() {
    return TransformerElementIds.PATTERN_ERRORS;
  }

  @FXML
  private void onAdd() {
    content().getOrCreatePatternErrors().add(new PatternError());
    structuralChange();
  }

  @Override
  protected void rebuild() {
    rows.getChildren().clear();
    List<PatternError> entries = content().getPatternErrorsOrEmpty();
    emptyLabel.setVisible(entries.isEmpty());
    emptyLabel.setManaged(entries.isEmpty());

    for (int index = 0; index < entries.size(); index++) {
      rows.getChildren().add(card(index, entries.get(index), entries.size()));
    }
  }

  private VBox card(int index, PatternError entry, int count) {
    var pattern = suggestionCombo(entry.getPattern(), () -> patterns, entry::setPattern, "pattern-" + index,
        StudioBundle.get("transformer_model.pattern_errors.pattern"));
    registerErrorTarget(TransformerElementIds.patternError(index, "pattern"), pattern);

    ComboBox<String> action = actionCombo(entry);
    action.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (rebuilding) {
        return;
      }
      entry.setAction(newValue);
      if (!PatternErrorAction.REPLACE.name().equals(newValue)) {
        entry.setReplacement(null);
      }
      structuralChange();
    });
    registerErrorTarget(TransformerElementIds.patternError(index, "action"), action);

    var moveButtons = RowFactory.createMoveButtonsBox(index, count, (from, to) -> {
      Collections.swap(content().getOrCreatePatternErrors(), from, to);
      structuralChange();
    });
    var delete = actionButton(Icons.TRASH, "row_action.delete", () -> {
      content().getOrCreatePatternErrors().remove(index);
      structuralChange();
    });

    VBox card = new VBox(8);
    card.getStyleClass().add("transformer-card");
    card.getChildren().add(row(grow(labelled("transformer_model.pattern_errors.pattern", pattern)),
        labelled("transformer_model.pattern_errors.action", action), moveButtons, delete));

    if (entry.getEffectiveAction() == PatternErrorAction.REPLACE) {
      TextField replacement = textField(entry.getReplacement(), entry::setReplacement, "replacement-" + index,
          StudioBundle.get("transformer_model.pattern_errors.replacement_prompt"));
      registerErrorTarget(TransformerElementIds.patternError(index, "replacement"), replacement);
      card.getChildren().add(labelled("transformer_model.pattern_errors.replacement", replacement));
    }

    card.getChildren().add(fieldLabel("transformer_model.pattern_errors.messages"));
    card.getChildren().add(localeTextRows(entry::getErrorsOrEmpty, entry::getOrCreateErrors,
        () -> {
          if (entry.getErrors() != null && entry.getErrors().isEmpty()) {
            entry.setErrors(null);
          }
        },
        errorIndex -> TransformerElementIds.patternErrorLocale(index, errorIndex), "message-" + index + "-"));
    return card;
  }

  private ComboBox<String> actionCombo(PatternError entry) {
    ComboBox<String> action = new ComboBox<>();
    action.getItems().setAll(PatternErrorAction.UPDATE_MESSAGE.name(), PatternErrorAction.REPLACE.name(), PatternErrorAction.SUPPRESS.name());
    action.setPromptText(label(PatternErrorAction.UPDATE_MESSAGE.name()) + " (" + StudioBundle.get("default") + ")");
    action.setConverter(new StringConverter<>() {
      @Override
      public String toString(@Nullable String value) {
        return value == null ? "" : label(value);
      }

      @Override
      public String fromString(String text) {
        return text;
      }
    });
    action.setValue(entry.getAction());
    return action;
  }

  private static String label(String action) {
    return StudioBundle.get("transformer_model.pattern_action." + action);
  }
}
