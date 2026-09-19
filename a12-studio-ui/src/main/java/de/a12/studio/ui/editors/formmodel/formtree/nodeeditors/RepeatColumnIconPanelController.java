package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Icon;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.ui.components.IconComboController;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * "Icon" property editor for a selected {@link RepeatOverviewColumn}: the icon shown in the column header
 * ({@link RepeatOverviewColumn#getIcon()}, wire shape {@code {"name": "face"}}). Not tied to a document-model
 * {@code Element}, so it follows the model-header pattern: a plain {@link #setColumn} entry point and {@link
 * #commitHeaderChange()}.
 */
public class RepeatColumnIconPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private IconComboController iconComboController;

  private RepeatOverviewColumn column;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    iconComboController.setOnChange(name -> {
      if (name == null) {
        column.setIcon(null);
      }
      else {
        Icon icon = column.getIcon();
        if (icon == null) {
          icon = new Icon();
          column.setIcon(icon);
        }
        icon.setName(name);
      }
      commitHeaderChange();
    });
  }

  public void setColumn(@NonNull RepeatOverviewColumn column) {
    this.column = column;
    iconComboController.setValue(column.getIcon() != null ? column.getIcon().getName() : null);
  }
}
