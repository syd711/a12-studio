package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * "Display" property editor for a selected {@link RepeatOverviewColumn}: {@code labelHidden} (only the icon is
 * shown in the table head; the label stays available to accessibility tools), {@code fixedWidth} (the column
 * does not stretch beyond its width) and {@code filterExposition} - how the filter input of a filterable
 * Enumeration/External Enumeration column is displayed ({@code FULL} = checkbox group, {@code STRING} = text
 * input; absent = the type's default, FULL for an Enumeration and STRING for an External Enumeration). The
 * exposition only matters for a filterable column, so its combo is disabled otherwise (see {@link
 * #refreshFilterable()}). Not tied to a document-model {@code Element}, so it follows the model-header pattern.
 */
public class RepeatColumnDisplayPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox labelHiddenCheckBox;
  @FXML
  private CheckBox fixedWidthCheckBox;
  @FXML
  private ComboBox<String> filterExpositionCombo;

  private RepeatOverviewColumn column;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    filterExpositionCombo.getItems().setAll(null, "FULL", "STRING");
    filterExpositionCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return StudioBundle.get("repeat_column_filter_exposition." + (value == null ? "default" : value.toLowerCase()));
      }

      @Override
      public String fromString(String displayName) {
        return null;
      }
    });

    bindCheckBox(labelHiddenCheckBox, (el, value) -> column.setLabelHidden(value ? Boolean.TRUE : null));
    bindCheckBox(fixedWidthCheckBox, (el, value) -> column.setFixedWidth(value ? Boolean.TRUE : null));
    bindComboBox(filterExpositionCombo, (el, value) -> column.setFilterExposition(value));
  }

  public void setColumn(@NonNull RepeatOverviewColumn column) {
    this.column = column;
    setFieldValue(labelHiddenCheckBox, Boolean.TRUE.equals(column.getLabelHidden()));
    setFieldValue(fixedWidthCheckBox, Boolean.TRUE.equals(column.getFixedWidth()));
    setFieldValue(filterExpositionCombo, column.getFilterExposition());
    refreshFilterable();
  }

  /** Re-evaluates whether the filter exposition applies, after the column's Filterable switch changed. */
  public void refreshFilterable() {
    filterExpositionCombo.setDisable(column == null || !Boolean.TRUE.equals(column.getFilterable()));
  }
}
