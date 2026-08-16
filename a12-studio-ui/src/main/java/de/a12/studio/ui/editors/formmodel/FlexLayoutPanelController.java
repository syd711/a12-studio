package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.ColumnLayout;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
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
 * Edits a {@link MultiColumnSection}'s {@code layout.lg} (SME's per-breakpoint {@link ColumnLayout} - only the
 * "lg" breakpoint is exposed here, matching the Multi-Column Section editor's scope for now). Not tied to a
 * single {@code Element}, so it follows the model-header pattern.
 */
public class FlexLayoutPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private Label layoutLgInfoIcon;

  @FXML
  private TextField layoutLgField;

  private MultiColumnSection section;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(layoutLgInfoIcon, StudioBundle.get("layout_lg_tooltip"));
    bindTextField(layoutLgField, (el, value) -> getOrCreateLayout().setLg(value.isEmpty() ? null : value));
  }

  public void setSection(@NonNull MultiColumnSection section) {
    this.section = section;
    ColumnLayout layout = section.getLayout();
    setFieldValue(layoutLgField, layout != null ? layout.getLg() : null);
  }

  private ColumnLayout getOrCreateLayout() {
    ColumnLayout layout = section.getLayout();
    if (layout == null) {
      layout = new ColumnLayout();
      section.setLayout(layout);
    }
    return layout;
  }
}
