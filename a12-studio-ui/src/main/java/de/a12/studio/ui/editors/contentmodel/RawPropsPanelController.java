package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The element's whole {@code props} as JSON, for what the typed panels do not cover: the Lexical {@code tree}/
 * {@code html} text of paragraphs and headings (SME edits those inline on its canvas), keys of element types the
 * studio has no panel for, and anything unusual already in a file. Invalid JSON is flagged and never applied; the
 * text is applied when the field loses focus.
 */
public class RawPropsPanelController extends AbstractContentSettingsPanel {

  @FXML
  private TextArea propsField;

  @FXML
  private Label styleInfoIcon;

  private boolean populating;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(styleInfoIcon, StudioBundle.get("content_settings.raw_props_style_info"));
    propsField.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
      if (!hasFocus) {
        commit();
      }
    });
  }

  @Override
  protected boolean appliesTo(String type) {
    return true;
  }

  @Override
  protected void populate(@NonNull ContentElement element) {
    populating = true;
    try {
      propsField.setText(pretty(element));
      hideError();
    }
    finally {
      populating = false;
    }
  }

  /** Re-renders the JSON after another panel changed the props (no-op while the user is typing here). */
  public void refresh() {
    ContentElement element = currentElement();
    if (element != null && !propsField.isFocused()) {
      populate(element);
    }
  }

  private static String pretty(ContentElement element) {
    if (element.getProps() == null) {
      return "{}";
    }
    return JsonSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(element.getProps());
  }

  private void commit() {
    ContentElement element = currentElement();
    if (element == null || populating) {
      return;
    }
    String text = propsField.getText();
    try {
      JsonNode node = JsonSettings.objectMapper.readTree(text == null || text.isBlank() ? "{}" : text);
      if (!node.isObject()) {
        showError("ERROR", StudioBundle.get("content_settings.raw_props_not_object"));
        return;
      }
      @SuppressWarnings("unchecked")
      Map<String, Object> props = JsonSettings.objectMapper.treeToValue(node, LinkedHashMap.class);
      if (props.equals(element.getProps()) || (element.getProps() == null && props.isEmpty())) {
        hideError();
        return;
      }
      element.setProps(props);
      hideError();
      changed();
    }
    catch (RuntimeException e) {
      showError("ERROR", StudioBundle.get("content_settings.raw_props_invalid", e.getMessage()));
    }
  }
}
