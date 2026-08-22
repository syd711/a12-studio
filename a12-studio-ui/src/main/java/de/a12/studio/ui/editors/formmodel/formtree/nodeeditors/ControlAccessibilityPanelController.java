package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * "Accessibility" property editor for a selected {@link Control} node: a single checkbox toggling {@link
 * Control#getLabelHiddenButRead()} — the control's label stays mandatory for screen readers but can be hidden
 * visually on screen. Not tied to a single {@code Element}, so it follows the model-header pattern (a plain
 * {@link #setControl} entry point, {@code bindCheckBox} ignoring its unused {@code Element} argument),
 * mirroring {@link SectionNamePanelController}.
 */
public class ControlAccessibilityPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox hideLabelCheckBox;

  @FXML
  private Label hideLabelInfoIcon;

  private Control control;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(hideLabelInfoIcon, StudioBundle.get("hide_label_hint"));
    bindCheckBox(hideLabelCheckBox, (el, value) -> control.setLabelHiddenButRead(value ? Boolean.TRUE : null));
  }

  public void setControl(@NonNull Control control) {
    this.control = control;
    setFieldValue(hideLabelCheckBox, Boolean.TRUE.equals(control.getLabelHiddenButRead()));
  }
}
