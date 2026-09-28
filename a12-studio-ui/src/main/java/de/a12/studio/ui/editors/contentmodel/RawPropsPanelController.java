package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.editors.propertyeditors.JsonCodeEditorController;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The element's whole {@code props} as JSON, for what the typed panels do not cover: the Lexical {@code tree}/
 * {@code html} text of paragraphs and headings (SME edits those inline on its canvas), keys of element types the
 * studio has no panel for, and anything unusual already in a file. Invalid JSON is flagged in this panel's error
 * container and never applied.
 */
public class RawPropsPanelController extends AbstractContentSettingsPanel {

  @FXML
  private JsonCodeEditorController propsFieldController;

  @FXML
  private Label styleInfoIcon;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(styleInfoIcon, StudioBundle.get("content_settings.raw_props_style_info"));
    propsFieldController.setValidator(this::checkIsObject);
    propsFieldController.errorProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        hideError();
      } else {
        showError("ERROR", newValue);
      }
    });
  }

  @Override
  public void destroy() {
    super.destroy();
    propsFieldController.destroy();
  }

  @Override
  protected boolean appliesTo(String type) {
    return true;
  }

  @Override
  protected void populate(@NonNull ContentElement element) {
    propsFieldController.setCustom(() -> pretty(element), text -> applyProps(element, text));
  }

  /** Re-renders the JSON after another panel changed the props (no-op while the user is typing here). */
  public void refresh() {
    ContentElement element = currentElement();
    if (element != null && !propsFieldController.isFocused()) {
      populate(element);
    }
  }

  private static String pretty(ContentElement element) {
    if (element.getProps() == null) {
      return "{}";
    }
    return JsonSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(element.getProps());
  }

  /** Only invoked once {@code text} has already parsed as valid JSON - checked by {@link JsonCodeEditorController} itself. */
  private String checkIsObject(String text) {
    JsonNode node = JsonSettings.objectMapper.readTree(text);
    return node.isObject() ? null : StudioBundle.get("content_settings.raw_props_not_object");
  }

  /** Applies {@code text} to {@code element} if it's valid JSON and an object; otherwise leaves the element
   * untouched, relying on {@link #propsFieldController}'s validation to have already flagged it. */
  private void applyProps(@NonNull ContentElement element, String text) {
    try {
      JsonNode node = JsonSettings.objectMapper.readTree(text == null || text.isBlank() ? "{}" : text);
      if (!node.isObject()) {
        return;
      }
      @SuppressWarnings("unchecked")
      Map<String, Object> props = JsonSettings.objectMapper.treeToValue(node, LinkedHashMap.class);
      if (props.equals(element.getProps()) || (element.getProps() == null && props.isEmpty())) {
        return;
      }
      element.setProps(props);
      changed();
    }
    catch (RuntimeException e) {
      // Invalid JSON - left unapplied; already flagged by propsFieldController's validation.
    }
  }
}
