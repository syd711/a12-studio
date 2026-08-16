package de.a12.studio.ui.editors.propertyeditors;

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
 * Edits a single {@code lg} (base/default breakpoint) column layout string, e.g. {@link
 * de.a12.studio.models.formmodel.ColumnLayout#getLg()}. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element} - the value is read/written via a caller-supplied {@code
 * Supplier}/{@code Consumer} pair (see {@link #setCustom}), mirroring {@link
 * de.a12.studio.ui.editors.formmodel.NamePanelController}. Shared across model editors (per the project's
 * package-placement convention), unlike {@link de.a12.studio.ui.editors.formmodel.FlexLayoutPanelController}
 * (which edits the same kind of field but is bound directly to a {@code MultiColumnSection} and carries a
 * "required" tooltip, whereas this field defaults to "12" when left empty).
 */
public class ColumnLayoutPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField layoutField;

  @FXML
  private Label layoutInfoIcon;

  private Consumer<String> writer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(layoutInfoIcon, StudioBundle.get("control_grid_layout_tooltip"));
    bindTextField(layoutField, (el, value) -> writer.accept(value.isEmpty() ? null : value));
  }

  public void setCustom(@NonNull Supplier<String> reader, @NonNull Consumer<String> writer) {
    this.writer = writer;
    setFieldValue(layoutField, reader.get());
  }
}
