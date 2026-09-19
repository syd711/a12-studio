package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.ColumnLayout;
import de.a12.studio.models.formmodel.ControlGrid;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits the {@code layout.md}/{@code layout.sm} responsive breakpoint overrides of a {@link ControlGrid} or a
 * {@link MultiColumnSection} (SME's shared "layout" mixin; the {@code lg} breakpoint itself is edited separately,
 * by the shared {@link de.a12.studio.ui.editors.propertyeditors.ColumnLayoutPanelController} for a grid and by
 * {@link FlexLayoutPanelController} for a section). Not tied to a single {@code Element}, so it follows the
 * model-header pattern, mirroring {@link FlexLayoutPanelController}; the owner's {@link ColumnLayout} is reached
 * through a getter/setter pair because it is created on the first edit.
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

  private Supplier<ColumnLayout> layoutGetter;
  private Consumer<ColumnLayout> layoutSetter;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(layoutMdInfoIcon, StudioBundle.get("layout_md_tooltip"));
    WidgetFactory.createHelpIcon(layoutSmInfoIcon, StudioBundle.get("layout_sm_tooltip"));
    bindTextField(layoutMdField, (el, value) -> getOrCreateLayout().setMd(value.isEmpty() ? null : value));
    bindTextField(layoutSmField, (el, value) -> getOrCreateLayout().setSm(value.isEmpty() ? null : value));
  }

  public void setControlGrid(@NonNull ControlGrid grid) {
    setLayoutOwner(grid::getLayout, grid::setLayout);
  }

  public void setSection(@NonNull MultiColumnSection section) {
    setLayoutOwner(section::getLayout, section::setLayout);
  }

  private void setLayoutOwner(@NonNull Supplier<ColumnLayout> getter, @NonNull Consumer<ColumnLayout> setter) {
    this.layoutGetter = getter;
    this.layoutSetter = setter;
    ColumnLayout layout = getter.get();
    setFieldValue(layoutMdField, layout != null ? layout.getMd() : null);
    setFieldValue(layoutSmField, layout != null ? layout.getSm() : null);
  }

  private ColumnLayout getOrCreateLayout() {
    ColumnLayout layout = layoutGetter.get();
    if (layout == null) {
      layout = new ColumnLayout();
      layoutSetter.accept(layout);
    }
    return layout;
  }
}
