package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * "Height" property editor of a {@link CustomScreenElement}: the fixed height in pixels of the embedded custom
 * component ({@link CustomScreenElement#getHeight()}, e.g. the scrollable area of a Relationship UI Model). Empty
 * means the component's own height. Only whole numbers can be entered; zero is taken over but flagged, like the
 * model validator does ({@code FormCustomScreenElementHeightValidator}, SME's {@code zeroNotAllowed}).
 * <p>
 * Not tied to a document-model {@code Element}, so it follows the model-header pattern. It shows its own
 * errors, which is why the text field is wired by hand with {@link #commitHeaderChange()} instead of {@link
 * #bindTextField}, whose commit would hide them again.
 */
public class CustomScreenElementHeightPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Label infoIcon;
  @FXML
  private TextField heightField;

  private CustomScreenElement customScreenElement;
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(infoIcon, StudioBundle.get("custom_element_height_tooltip"));
    heightField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel && customScreenElement != null) {
        onHeightEdited(newValue == null ? "" : newValue.strip());
      }
    });
  }

  public void setCustomScreenElement(@NonNull CustomScreenElement element) {
    this.customScreenElement = element;
    updatingFromModel = true;
    try {
      heightField.setText(element.getHeight() == null ? "" : element.getHeight().toString());
    }
    finally {
      updatingFromModel = false;
    }
    showProblem();
  }

  private void onHeightEdited(String text) {
    if (text.isEmpty()) {
      customScreenElement.setHeight(null);
    }
    else if (text.matches("\\d{1,9}")) {
      customScreenElement.setHeight(Integer.valueOf(text));
    }
    else {
      showError("ERROR", StudioBundle.get("custom_element_height_invalid", text));
      return;
    }
    showProblem();
    debouncer.debounce(heightField.getId(), this::commitHeaderChange, COMMIT_DEBOUNCE_MS, true);
  }

  private void showProblem() {
    if (customScreenElement.getHeight() != null && customScreenElement.getHeight() == 0) {
      String name = customScreenElement.getName() != null && !customScreenElement.getName().isBlank()
          ? customScreenElement.getName() : customScreenElement.getId();
      showError("ERROR", ValidationMessages.get("validation.customScreenElementHeight.zero", name));
    }
    else {
      hideError();
    }
  }
}
