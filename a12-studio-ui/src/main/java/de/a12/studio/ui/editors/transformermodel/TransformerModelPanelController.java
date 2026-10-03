package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.models.transformermodel.TransformerModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.events.LocalesChangedEvent;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Base of the Transformer Model editor's panels, every one of which edits one list (or group) of the model's content
 * rather than a single {@link de.a12.studio.models.documentmodel.Element}: wired with plain listeners and saved with
 * {@link #commitHeaderChange()}, following {@code SelectionCategoryPanelController} (the inherited
 * {@code bindTextField}/{@code commitChange} are element-bound and would hide this panel's error container on every
 * commit).
 *
 * <p>What the subclasses share, so each only builds its rows:
 * <ul>
 *   <li>the commit: {@link #structuralChange()} (a row added/removed/moved, a type switched - the rows are rebuilt) and
 *       {@link #textEdited(String)} (typing - debounced, the rows stay so the caret does not jump); both save, re-validate,
 *       and tell the editor ({@link #setOnChange}) so it can rerun the transformation</li>
 *   <li>the validation: the model's errors whose element id lies under {@link #errorIdPrefix()} are mapped onto the
 *       controls that were registered for that id ({@link #registerErrorTarget}) - the "error" pseudo-class and the
 *       message as tooltip - and the first one is shown in the panel's own error container</li>
 *   <li>suggestions: editable combo boxes ({@link #suggestionCombo}) whose proposals come from the XSD discovery and
 *       change after a transformation run ({@link #refreshSuggestions()}) without disturbing what is being typed</li>
 * </ul>
 */
public abstract class TransformerModelPanelController extends AbstractPropertyEditor {

  private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private final Debouncer commitDebouncer = new Debouncer();

  protected TransformerModel model;

  // Set while controls are being filled from the model or their items replaced, so that is not taken for an edit.
  protected boolean rebuilding;

  private Runnable onChange = () -> {
  };

  private final Map<String, List<Node>> errorTargets = new HashMap<>();

  private final Map<ComboBox<String>, Supplier<List<String>>> suggestionCombos = new LinkedHashMap<>();

  /** Binds the panel to {@code model} and builds its rows. */
  public void setModel(@NonNull TransformerModel model) {
    this.model = model;
    if (model.getContent() == null) {
      model.setContent(new TransformerModelContent());
    }
    rebuildAndValidate();
  }

  /** Called after every change this panel committed to the model (the editor reruns the transformation). */
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  /** Builds the rows from the model. Subclasses call {@link #registerErrorTarget} for every control that has an error id. */
  protected abstract void rebuild();

  /** The element id prefix of this panel's validation errors, see {@code TransformerElementIds}. */
  protected abstract String errorIdPrefix();

  protected TransformerModelContent content() {
    return model.getContent();
  }

  protected final void rebuildAndValidate() {
    errorTargets.clear();
    suggestionCombos.clear();
    rebuilding = true;
    try {
      rebuild();
    }
    finally {
      rebuilding = false;
    }
    refreshValidation();
  }

  /** A row was added, removed or moved, or a choice changed what the row consists of: rebuild, save, validate, notify. */
  protected final void structuralChange() {
    rebuildAndValidate();
    commitAndNotify();
  }

  /** A value was typed or picked in a row that stays as it is: save, validate, notify - debounced for typing. */
  protected final void textEdited(@NonNull String key) {
    if (rebuilding) {
      return;
    }
    commitDebouncer.debounce(key, this::commitAndNotify, COMMIT_DEBOUNCE_MS, true);
  }

  private void commitAndNotify() {
    commitHeaderChange();
    refreshValidation();
    onChange.run();
  }

  @Override
  public void destroy() {
    commitDebouncer.shutdown();
    super.destroy();
  }

  @Override
  public void localesChanged(@NonNull LocalesChangedEvent event) {
    if (model != null && event.getItem() != null && event.getItem().getModel() == model) {
      rebuildAndValidate();
    }
  }

  // ---- validation ----------------------------------------------------------------------------------------------

  protected final void registerErrorTarget(@NonNull String elementId, @NonNull Node node) {
    errorTargets.computeIfAbsent(elementId, id -> new ArrayList<>()).add(node);
  }

  /** Re-checks the model and shows what is wrong in this panel: on the rows' controls and as the first message. */
  public final void refreshValidation() {
    if (model == null || Studio.getValidationService() == null) {
      hideError();
      return;
    }
    List<ModelValidationError> errors = Studio.getValidationService().validate(model).stream()
        .filter(error -> error.elementId() != null && error.elementId().startsWith(errorIdPrefix()))
        .toList();

    for (Map.Entry<String, List<Node>> target : errorTargets.entrySet()) {
      ModelValidationError error = errors.stream().filter(candidate -> target.getKey().equals(candidate.elementId())).findFirst().orElse(null);
      for (Node node : target.getValue()) {
        node.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error != null);
        installTooltip(node, error == null ? null : error.message());
      }
    }

    if (errors.isEmpty()) {
      hideError();
    }
    else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private static void installTooltip(Node node, @Nullable String message) {
    if (node instanceof javafx.scene.control.Control control) {
      control.setTooltip(message == null ? null : WidgetFactory.createTooltip(message));
    }
  }

  // ---- suggestions -----------------------------------------------------------------------------------------------

  /** Replaces the proposals of every suggestion combo from its supplier, keeping the text typed into it. */
  public final void refreshSuggestions() {
    rebuilding = true;
    try {
      for (Map.Entry<ComboBox<String>, Supplier<List<String>>> entry : suggestionCombos.entrySet()) {
        applySuggestions(entry.getKey(), entry.getValue().get());
      }
    }
    finally {
      rebuilding = false;
    }
  }

  /**
   * Like {@link #refreshSuggestions()} for only the given combos - for the ones of a row whose proposals depend on
   * another control of that row (the field paths of the chosen enumeration value), so the control being typed
   * in is left alone.
   */
  @SafeVarargs
  protected final void reapplySuggestions(@NonNull ComboBox<String>... combos) {
    boolean previous = rebuilding;
    rebuilding = true;
    try {
      for (ComboBox<String> combo : combos) {
        Supplier<List<String>> supplier = suggestionCombos.get(combo);
        if (supplier != null) {
          applySuggestions(combo, supplier.get());
        }
      }
    }
    finally {
      rebuilding = previous;
    }
  }

  private static void applySuggestions(ComboBox<String> combo, List<String> suggestions) {
    String text = combo.getEditor().getText();
    combo.getItems().setAll(suggestions);
    combo.getEditor().setText(text);
  }

  /**
   * An editable combo box that proposes {@code suggestions} (from the XSD discovery, possibly empty) and accepts any
   * text. {@code setter} receives the text on every edit, {@code null} when it is blank.
   */
  protected final ComboBox<String> suggestionCombo(@Nullable String value, @NonNull Supplier<List<String>> suggestions,
      @NonNull Consumer<String> setter, @NonNull String key, @Nullable String prompt) {
    ComboBox<String> combo = new ComboBox<>();
    combo.setEditable(true);
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.setPromptText(prompt);
    applySuggestions(combo, suggestions.get());
    combo.getEditor().setText(value == null ? "" : value);
    combo.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      if (rebuilding) {
        return;
      }
      setter.accept(newValue == null || newValue.isBlank() ? null : newValue);
      textEdited(key);
    });
    suggestionCombos.put(combo, suggestions);
    return combo;
  }

  protected final TextField textField(@Nullable String value, @NonNull Consumer<String> setter, @NonNull String key, @Nullable String prompt) {
    TextField field = new TextField(value);
    field.setPromptText(prompt);
    field.setMaxWidth(Double.MAX_VALUE);
    field.textProperty().addListener((observable, oldValue, newValue) -> {
      if (rebuilding) {
        return;
      }
      setter.accept(newValue == null || newValue.isBlank() ? null : newValue);
      textEdited(key);
    });
    return field;
  }

  // ---- row building --------------------------------------------------------------------------------------------

  protected static HBox row(Node... nodes) {
    HBox box = new HBox(8, nodes);
    box.setAlignment(Pos.CENTER_LEFT);
    return box;
  }

  /** Lets {@code node} take the free width of its row. */
  protected static <T extends Node> T grow(T node) {
    HBox.setHgrow(node, Priority.ALWAYS);
    return node;
  }

  protected static javafx.scene.control.Label fieldLabel(@NonNull String bundleKey) {
    javafx.scene.control.Label label = new javafx.scene.control.Label(StudioBundle.get(bundleKey));
    label.getStyleClass().add("transformer-field-label");
    return label;
  }

  /** A caption above a control, for the fields of a card. */
  protected static VBox labelled(@NonNull String bundleKey, @NonNull Node control) {
    VBox box = new VBox(3, fieldLabel(bundleKey), control);
    box.setMinWidth(150);
    return box;
  }

  protected static Button actionButton(@NonNull String icon, @NonNull String tooltipKey, @NonNull Runnable action) {
    return de.a12.studio.ui.editors.propertyeditors.RowFactory.createActionButton(icon, StudioBundle.get(tooltipKey), action);
  }

  protected static void onAction(@NonNull Button button, @NonNull Runnable action) {
    button.setOnAction((ActionEvent event) -> action.run());
  }

  // ---- the localized texts of a list entry ---------------------------------------------------------------------

  /** The locales of the model, in order. */
  protected final List<String> modelLocales() {
    List<String> locales = new ArrayList<>();
    if (model != null) {
      model.getLocales().forEach(locale -> {
        if (locale.getCode() != null && !locale.getCode().isBlank()) {
          locales.add(locale.getCode());
        }
      });
    }
    return locales;
  }

  /**
   * One text row per locale of the model - the locale as a caption, an editable text - plus a row (with a delete button)
   * for every text of a locale the model does not have (or none), so such a stale text can be seen and removed.
   * Typing creates the text, emptying it removes it. SME's per-locale repeats ({@code errors}, {@code replacements}
   * have no add/remove buttons; one row per locale of the model).
   *
   * @param labels        the entry's texts, created on the first edit (and set back to null when the last one is gone)
   * @param localeErrorId the element id the validation reports a text's locale under, by position in the entry's texts
   */
  protected final VBox localeTextRows(@NonNull Supplier<List<Label>> labels, @NonNull Supplier<List<Label>> orCreate,
      @NonNull Runnable dropIfEmpty, @NonNull IntFunction<String> localeErrorId, @NonNull String keyPrefix) {
    VBox rows = new VBox(4);
    List<String> locales = modelLocales();
    List<Label> existing = labels.get();

    for (String locale : locales) {
      Label current = existing.stream().filter(label -> locale.equals(label.getLocale())).findFirst().orElse(null);
      javafx.scene.control.Label caption = new javafx.scene.control.Label(locale);
      caption.getStyleClass().add("transformer-locale-label");
      TextField text = new TextField(current == null ? "" : current.getText());
      text.setMaxWidth(Double.MAX_VALUE);
      text.textProperty().addListener((observable, oldValue, newValue) -> {
        if (rebuilding) {
          return;
        }
        List<Label> all = orCreate.get();
        Label target = all.stream().filter(label -> locale.equals(label.getLocale())).findFirst().orElse(null);
        if (newValue == null || newValue.isEmpty()) {
          if (target != null) {
            all.remove(target);
            dropIfEmpty.run();
          }
        }
        else {
          if (target == null) {
            target = new Label();
            target.setLocale(locale);
            all.add(target);
          }
          target.setText(newValue);
        }
        textEdited(keyPrefix + locale);
      });
      rows.getChildren().add(row(caption, grow(text)));
    }

    for (int index = 0; index < existing.size(); index++) {
      Label orphan = existing.get(index);
      if (orphan.getLocale() != null && locales.contains(orphan.getLocale())) {
        continue;
      }
      javafx.scene.control.Label caption = new javafx.scene.control.Label(orphan.getLocale() == null ? "?" : orphan.getLocale());
      caption.getStyleClass().addAll("transformer-locale-label", "transformer-locale-label-orphan");
      TextField text = new TextField(orphan.getText());
      text.setMaxWidth(Double.MAX_VALUE);
      text.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!rebuilding) {
          orphan.setText(newValue == null || newValue.isEmpty() ? null : newValue);
          textEdited(keyPrefix + "orphan" + System.identityHashCode(orphan));
        }
      });
      Button delete = actionButton(de.a12.studio.ui.util.Icons.TRASH, "row_action.delete", () -> {
        orCreate.get().remove(orphan);
        dropIfEmpty.run();
        structuralChange();
      });
      registerErrorTarget(localeErrorId.apply(index), caption);
      registerErrorTarget(localeErrorId.apply(index), text);
      rows.getChildren().add(row(caption, grow(text), delete));
    }
    return rows;
  }
}
