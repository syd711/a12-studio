package de.a12.studio.ui.editors.maindetailmodel;

import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits {@link MasterDetailModel}'s preferred detail form width (1 to 11 of the 12 layout grid slots). Not
 * bound to a single {@link de.a12.studio.models.documentmodel.Element} (the width lives on the model's
 * content), so it follows the model-header pattern used by e.g. {@link
 * de.a12.studio.ui.editors.applicationmodel.LayoutPanelController}.
 */
public class FormWidthPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int DEFAULT_FORM_WIDTH = 6;
  private static final int MAX_FORM_WIDTH = 11;

  @FXML
  private Spinner<Integer> formWidthField;

  private MasterDetailModel model;

  // Set while formWidthField is being repopulated from the model, so that programmatic updates aren't
  // mistaken for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    formWidthField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_FORM_WIDTH, DEFAULT_FORM_WIDTH));
    WidgetFactory.restrictToNumericInput(formWidthField.getEditor());

    formWidthField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      model.getContent().setFormWidth(newValue);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull MasterDetailModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      formWidthField.getValueFactory().setValue(
          model.getContent().getFormWidth() != null ? model.getContent().getFormWidth() : DEFAULT_FORM_WIDTH);
    } finally {
      updatingFromModel = false;
    }
  }
}
