package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits a plain {@code name} field shared by several Form Model structural node types ({@link
 * de.a12.studio.models.formmodel.Row}, {@link de.a12.studio.models.formmodel.ScreenElement}, {@link
 * de.a12.studio.models.formmodel.Screen}) - none of which is a single {@code Element}, so it follows the
 * model-header pattern ({@code reader}/{@code writer} suppliers, mirroring {@link StylesPanelController#setCustom}),
 * bound fresh via {@link #setCustom} whenever the Form Model tree's selection changes to a node of that type. Kept
 * in this package (rather than the shared {@code propertyeditors} package) since every current and anticipated
 * caller belongs to the same Form Model editor, per the package-placement convention in the project's CLAUDE.md.
 */
public class NamePanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;

  private Consumer<String> writer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindTextField(nameField, (el, value) -> writer.accept(value.isEmpty() ? null : value));
  }

  public void setCustom(@NonNull Supplier<String> reader, @NonNull Consumer<String> writer) {
    this.writer = writer;
    setFieldValue(nameField, reader.get());
  }
}
