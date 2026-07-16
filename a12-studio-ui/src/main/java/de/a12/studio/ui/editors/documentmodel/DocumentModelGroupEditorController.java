package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelGroupEditorController implements ElementEditorController, Initializable {
  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private LocalizedTextPanelController descriptionInternalController;

  @FXML
  private LocalizedTextPanelController descriptionExternalController;

  private Element element;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    labelController.configureLabel();
    descriptionInternalController.configureInternal();
    descriptionExternalController.configureExternal();
  }

  @Override
  public void setElement(@NonNull Element element, @NonNull List<Element> ancestors) {
    this.element = element;
  }
}
