package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Accessibility" property editor for a selected {@link Control} node: edits {@link Control#getAccessibility()},
 * a per-locale text used as an alternative label for screen readers (analogous to an HTML {@code aria-label}).
 * Wraps a single plain {@link LocalizedTextPanelController} — no expression support, no Field Configuration
 * variant (this is a purely per-Control UI concern, not a field-level default).
 */
public class ControlAccessibilityPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private LocalizedTextPanelController accessibilityTextController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    accessibilityTextController.configureCustom("accessibilityText", "");
    accessibilityTextController.setCollapsed();
  }

  public void setControl(@NonNull Control control) {
    accessibilityTextController.setCustom(
        () -> texts(control.getAccessibility()),
        () -> ensureAccessibility(control).getText());
  }

  private static List<Label> texts(@Nullable TextContainer c) {
    return c != null ? c.getText() : List.of();
  }

  private static TextContainer ensureAccessibility(@NonNull Control control) {
    if (control.getAccessibility() == null) {
      control.setAccessibility(new TextContainer());
    }
    return control.getAccessibility();
  }
}
