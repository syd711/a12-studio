package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.ColumnLayout;
import de.a12.studio.models.formmodel.ControlGrid;
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
 * Edits a {@link ControlGrid}'s {@code layout.md}/{@code layout.lg} responsive breakpoint overrides (the {@code
 * lg} breakpoint itself is edited separately, by the shared {@link
 * de.a12.studio.ui.editors.propertyeditors.ColumnLayoutPanelController}). Not tied to a single {@code Element},
 * so it follows the model-header pattern, mirroring {@link FlexLayoutPanelController}.
 */
public class ResponsiveLayoutPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private Label layoutMdInfoIcon;
  @FXML
  private TextField layoutMdField;
  @FXML
  private Label layoutSmInfoIcon;
  @FXML
  private TextField layoutSmField;

  private ControlGrid grid;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(layoutMdInfoIcon, StudioBundle.get("layout_md_tooltip"));
    WidgetFactory.createHelpIcon(layoutSmInfoIcon, StudioBundle.get("layout_sm_tooltip"));
    bindTextField(layoutMdField, (el, value) -> getOrCreateLayout().setMd(value.isEmpty() ? null : value));
    bindTextField(layoutSmField, (el, value) -> getOrCreateLayout().setSm(value.isEmpty() ? null : value));
  }

  public void setControlGrid(@NonNull ControlGrid grid) {
    this.grid = grid;
    ColumnLayout layout = grid.getLayout();
    setFieldValue(layoutMdField, layout != null ? layout.getMd() : null);
    setFieldValue(layoutSmField, layout != null ? layout.getSm() : null);
  }

  private ColumnLayout getOrCreateLayout() {
    ColumnLayout layout = grid.getLayout();
    if (layout == null) {
      layout = new ColumnLayout();
      grid.setLayout(layout);
    }
    return layout;
  }
}
