package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.form.InitiallyFocusedElementSupport;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * "Initially Focused Element" property editor of a {@link Screen}: the Control that gets the keyboard focus when
 * the form opens ({@link Screen#getInitiallyFocusedElementId()}). Like SME it only offers the choice on the first
 * screen ({@link InitiallyFocusedElementSupport#isFirstScreen}), with the editable Controls outside of repeats
 * as candidates ({@link InitiallyFocusedElementSupport#focusableControls}); on any other screen the panel is
 * hidden, unless a value is already there (e.g. a screen moved behind the first one) so it can still be seen and
 * cleared. The candidates show their Document Model path, the stored value is the Control's id.
 * <p>
 * Not tied to a document-model {@code Element}, so it follows the model-header pattern. The problem of the
 * current value ({@link InitiallyFocusedElementSupport#problem}) is shown in the panel's own error container;
 * that is why the combo box is wired by hand with {@link #commitHeaderChange()} instead of {@link
 * #bindComboBox}, whose commit would hide the error again.
 */
public class InitiallyFocusedElementPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private final Debouncer debouncer = new Debouncer();
  // Control id -> what the combo box shows for it. A value that is not a candidate (a stale id) shows as itself.
  private final Map<String, String> displayById = new HashMap<>();

  @FXML
  private Label infoIcon;
  @FXML
  private ComboBox<String> focusedElementField;
  @FXML
  private Button clearButton;

  private Screen screen;
  private FormModelContent content;
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(infoIcon, StudioBundle.get("initially_focused_element_tooltip"));
    focusedElementField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String id) {
        return id == null ? "" : displayById.getOrDefault(id, id);
      }

      @Override
      public String fromString(String text) {
        return null;
      }
    });
    clearButton.disableProperty().bind(focusedElementField.valueProperty().isNull());
    focusedElementField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel && screen != null) {
        screen.setInitiallyFocusedElementId(newValue == null || newValue.isBlank() ? null : newValue);
        showProblem();
        debouncer.debounce(focusedElementField.getId(), this::commitHeaderChange, COMMIT_DEBOUNCE_MS, true);
      }
    });
  }

  @FXML
  private void onClear(ActionEvent event) {
    focusedElementField.setValue(null);
  }

  /** Shows or hides the whole panel, see the class comment. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /**
   * Binds the panel to {@code screen}; {@code content} says which screen is the first one, {@code elementIndex}
   * (may be {@code null}) resolves the Controls' Document Model paths for display.
   */
  public void setScreen(@NonNull Screen screen, @Nullable FormModelContent content, @Nullable ElementIndex elementIndex) {
    this.screen = screen;
    this.content = content;
    boolean first = InitiallyFocusedElementSupport.isFirstScreen(content, screen);
    setVisible(first || screen.getInitiallyFocusedElementId() != null);

    displayById.clear();
    List<String> candidateIds = List.of();
    if (first) {
      List<Control> candidates = InitiallyFocusedElementSupport.focusableControls(screen);
      candidates.forEach(control -> displayById.put(control.getId(), display(control, elementIndex)));
      candidateIds = candidates.stream().map(Control::getId).toList();
    }
    updatingFromModel = true;
    try {
      focusedElementField.getItems().setAll(candidateIds);
      focusedElementField.setValue(screen.getInitiallyFocusedElementId());
    }
    finally {
      updatingFromModel = false;
    }
    showProblem();
  }

  private static String display(Control control, @Nullable ElementIndex elementIndex) {
    String path = elementIndex != null && control.getElementRef() != null
        ? elementIndex.resolveDisplayPath(control.getElementRef()) : control.getElementRef();
    return (path == null || path.isBlank() ? control.getId() : path) + " (" + control.getId() + ")";
  }

  private void showProblem() {
    InitiallyFocusedElementSupport.problem(content, screen)
        .ifPresentOrElse(problem -> showError("ERROR", InitiallyFocusedElementSupport.message(problem, screen)), this::hideError);
  }
}
