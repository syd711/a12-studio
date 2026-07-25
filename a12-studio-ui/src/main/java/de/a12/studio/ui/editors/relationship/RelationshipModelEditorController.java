package de.a12.studio.ui.editors.relationship;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.dialogs.EditorDialogs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RelationshipModelEditorController extends AbstractEditorController implements Initializable {
  private static final String DEFAULT_SETTINGS_TOOLTIP = "Model Settings";

  @FXML
  private Tooltip settingsButtonTooltip;

  @FXML
  private Circle settingsErrorBadge;


  @FXML
  public void onSettings(ActionEvent e) {
    EditorDialogs.openSettings();
    updateSettingsErrorBadge();
  }

  public void loadModel(@NonNull A12Model<?> model) {
    load((OverviewModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull OverviewModel documentModel) {

  }

  private void updateSettingsErrorBadge() {
    List<String> issues = projectItem.getModel() != null
        ? Studio.getValidationService().getSettingsIssueMessages(projectItem.getModel())
        : List.of();

    settingsErrorBadge.setVisible(!issues.isEmpty());
    settingsButtonTooltip.setText(issues.isEmpty() ? DEFAULT_SETTINGS_TOOLTIP : String.join("\n\n", issues));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.RELATIONSHIP;
  }
}
