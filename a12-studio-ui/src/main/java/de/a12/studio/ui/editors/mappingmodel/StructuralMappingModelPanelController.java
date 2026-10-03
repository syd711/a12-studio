package de.a12.studio.ui.editors.mappingmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link de.a12.studio.models.mappingmodel.MappingModel}'s {@code content.StructuralMappingModel.id}: which
 * Structural Mapping Model holds the field mappings between its Source and Target models (SME's mapping model
 * requires one, "For the mapping model no structural mapping model is specified. This is not allowed."). A plain
 * combo box of the project's Structural Mapping Models with a button that opens the selected one in its editor,
 * where the mappings are made in the context of this Mapping Model.
 *
 * <p>Like {@link de.a12.studio.ui.editors.propertyeditors.TargetModelPanelController} it is not wired through
 * {@link de.a12.studio.ui.editors.AbstractPropertyEditor}: it edits a content field and a header {@link
 * de.a12.studio.models.ModelReference} directly, and its owning editor already has the save cycle to fold this into.
 * Only the Mapping Model editor uses it, so it lives next to it.
 */
public class StructuralMappingModelPanelController implements Initializable {

  @FXML
  private ComboBox<String> structuralMappingModelField;

  @FXML
  private Button editReferenceButton;

  @FXML
  private ErrorContainerController errorContainerController;

  // Set while the field is being repopulated from the model, so that programmatic updates are not mistaken
  // for user edits and do not trigger the change callback.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    structuralMappingModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      validate();
      if (updatingFromModel) {
        return;
      }
      onChange.run();
    });
    editReferenceButton.disableProperty().bind(structuralMappingModelField.valueProperty().isNull());
  }

  /** Opens the selected Structural Mapping Model in an editor tab, selecting its tab instead if it is already open. */
  @FXML
  private void onEditReference(ActionEvent event) {
    String reference = structuralMappingModelField.getValue();
    if (reference != null) {
      ProjectDocumentModels.openModelInEditor(reference);
    }
  }

  /** Invoked after every user-driven selection change (not while {@link #load} repopulates the field). */
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  /**
   * @param structuralMappingModels the Structural Mapping Models of the project
   * @param selectedId              the id the Mapping Model currently stores, or {@code null}
   */
  public void load(@NonNull List<? extends A12Model<?>> structuralMappingModels, String selectedId) {
    updatingFromModel = true;
    try {
      structuralMappingModelField.getItems().setAll(structuralMappingModels.stream()
          .map(A12Model::getId)
          .sorted(Comparator.naturalOrder())
          .toList());
      structuralMappingModelField.setValue(selectedId);
    }
    finally {
      updatingFromModel = false;
    }
    validate();
  }

  public String getValue() {
    return structuralMappingModelField.getValue();
  }

  private void validate() {
    String value = structuralMappingModelField.getValue();
    if (value == null) {
      errorContainerController.show("ERROR", StudioBundle.get("structural_mapping_panel.required"));
    }
    else if (!structuralMappingModelField.getItems().contains(value)) {
      // The combo box happily displays a stored id it has no item for (the model was deleted or renamed outside
      // the app), which looks exactly like a valid selection.
      errorContainerController.show("ERROR", StudioBundle.get("structural_mapping_panel.not_found", value));
    }
    else {
      errorContainerController.hide();
    }
  }
}
