package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.ComputationConfig;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link ComputationElement}'s "Allow Differing Decimal Places" and "Common Precondition" settings
 * (SME's Computation Rule Editor, see docs/modules/documentModel/05_detail-dialogs.adoc): "Allow Differing
 * Decimal Places" maps to the presence of the kernel error code {@value #DIFFERING_DECIMAL_PLACES_ERROR_CODE}
 * in {@link ComputationConfig#getErrorCodesToSuppress()}; "Common Precondition" is a plain UI toggle for
 * whether {@link ComputationConfig#getCommonPrecondition()} is set at all - checking it creates an (initially
 * empty) common precondition and reveals the text area to edit it, unchecking it clears the field again.
 */
public class ComputationOptionsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String DIFFERING_DECIMAL_PLACES_ERROR_CODE = "MVK_INVALID_COMPARE_DEC_PLACES";

  @FXML
  private CheckBox allowDifferingDecimalPlacesCheckBox;

  @FXML
  private CheckBox commonPreconditionCheckBox;

  @FXML
  private TextArea commonPreconditionTextArea;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    bindCheckBox(allowDifferingDecimalPlacesCheckBox, (element, value) -> {
      List<String> errorCodesToSuppress = getComputation(element).getErrorCodesToSuppress();
      if (value) {
        if (!errorCodesToSuppress.contains(DIFFERING_DECIMAL_PLACES_ERROR_CODE)) {
          errorCodesToSuppress.add(DIFFERING_DECIMAL_PLACES_ERROR_CODE);
        }
      } else {
        errorCodesToSuppress.remove(DIFFERING_DECIMAL_PLACES_ERROR_CODE);
      }
    });

    bindCheckBox(commonPreconditionCheckBox, (element, value) ->
        getComputation(element).setCommonPrecondition(value ? "" : null));
    commonPreconditionCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateCommonPreconditionVisibility());

    bindTextArea(commonPreconditionTextArea, (element, value) -> getComputation(element).setCommonPrecondition(value));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    ComputationConfig computation = getComputation(element);
    setFieldValue(allowDifferingDecimalPlacesCheckBox, computation.getErrorCodesToSuppress().contains(DIFFERING_DECIMAL_PLACES_ERROR_CODE));
    setFieldValue(commonPreconditionCheckBox, computation.getCommonPrecondition() != null);
    setFieldValue(commonPreconditionTextArea, computation.getCommonPrecondition());
    updateCommonPreconditionVisibility();
  }

  private void updateCommonPreconditionVisibility() {
    boolean visible = commonPreconditionCheckBox.isSelected();
    commonPreconditionTextArea.setVisible(visible);
    commonPreconditionTextArea.setManaged(visible);
  }

  private static ComputationConfig getComputation(Element element) {
    return ((ComputationElement) element).getComputation();
  }
}
