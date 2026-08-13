package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.formmodel.dialogs.Dialogs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

public class FormModelEditorController extends AbstractEditorController implements Initializable {

  public void loadModel(@NonNull A12Model<?> model) {
    load((FormModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull FormModel formModel) {

  }

  @FXML
  private void onRepeatDefaultButtonLabels(ActionEvent event) {
    Dialogs.openRepeatDefaultButtonLabels((FormModel) projectItem.getModel());
  }

  @FXML
  private void onSubheaderAndFooter(ActionEvent event) {
    Dialogs.openSubheaderAndFooter((FormModel) projectItem.getModel());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.FORM;
  }
}
