package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Section;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a {@link Section}'s {@code name} and {@code collapsible} flag - the Form Model tree's Section node
 * editor's first panel ({@link FormNodeEditorSectionPanelController}). Not tied to a single {@code Element},
 * so it follows the model-header pattern (a plain {@link #setSection} entry point, {@code bindTextField}/
 * {@code bindCheckBox}'s built-in commit) rather than {@link #setElement}, mirroring {@link
 * de.a12.studio.ui.editors.formmodel.ButtonGeneralPanelController}.
 */
public class SectionNamePanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;
  @FXML
  private CheckBox collapsibleField;

  private Section section;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindTextField(nameField, (el, value) -> section.setName(value.isEmpty() ? null : value));
    bindCheckBox(collapsibleField, (el, value) -> section.setCollapsible(value ? Boolean.TRUE : null));
  }

  public void setSection(@NonNull Section section) {
    this.section = section;
    setFieldValue(nameField, section.getName());
    setFieldValue(collapsibleField, Boolean.TRUE.equals(section.getCollapsible()));
  }
}
