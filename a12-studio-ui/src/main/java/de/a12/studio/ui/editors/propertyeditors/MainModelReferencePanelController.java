package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link MasterDetailModel}'s master list: the choice between an {@link
 * de.a12.studio.models.overviewmodel.OverviewModel} and a {@link de.a12.studio.models.treemodel.TreeModel},
 * plus the actual reference to whichever kind is selected. Mirrors {@link OverviewReferencePanelController}'s
 * radio-group-above-combobox pattern. Isn't wired through {@link de.a12.studio.ui.editors.AbstractPropertyEditor}
 * for the same reason as that panel: it edits {@code content.type}/{@code content.overviewModel}/{@code
 * content.treeModel} directly, not a document-model {@link de.a12.studio.models.documentmodel.Element}, and
 * its owning editor already has its own updatingFromModel/commitChange save cycle to fold this into.
 */
public class MainModelReferencePanelController implements Initializable {

  @FXML
  private RadioButton overviewTypeField;
  @FXML
  private RadioButton treeTypeField;
  @FXML
  private ComboBox<String> mainModelField;
  @FXML
  private Button editMainModelButton;

  @FXML
  private ErrorContainerController errorContainerController;

  private MasterDetailModel model;
  private List<String> overviewModelIds = List.of();
  private List<String> treeModelIds = List.of();

  // Set while fields are being repopulated from the model, so that programmatic updates aren't mistaken
  // for user edits and don't trigger setOnChange's callback.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    overviewTypeField.selectedProperty().addListener((observable, oldValue, isSelected) -> {
      if (isSelected) {
        onTypeChanged();
      }
    });
    treeTypeField.selectedProperty().addListener((observable, oldValue, isSelected) -> {
      if (isSelected) {
        onTypeChanged();
      }
    });
    mainModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      validate();
      if (updatingFromModel || model == null) {
        return;
      }
      applySelection();
      onChange.run();
    });
    editMainModelButton.disableProperty().bind(mainModelField.valueProperty().isNull());
  }

  /**
   * Opens the Document Model referenced by the combo box in an editor tab, selecting its tab instead if it's
   * already open (see {@code TabPaneController#modelOpened}). Mirrors {@link
   * IncludePropertiesPanelController#onEditReference}.
   */
  @FXML
  private void onEditMainModel() {
    String reference = mainModelField.getValue();
    if (reference == null) {
      return;
    }

    ProjectDocumentModels.findProjectItemByModelId(reference).ifPresent(item -> {
      Project project = Studio.getCurrentProject();
      if (project != null) {
        project.getSettings().getUISettings().addOpenedFile(item.getPath());
        project.getSettings().getUISettings().save();
      }
      StudioEventManager.getInstance().fireModelOpenEvent(item);
    });
  }

  /**
   * Invoked after every user-driven type switch or reference selection (not while {@link #load} is
   * repopulating the fields), so the owning editor can rebuild the Form Mapping section and save.
   */
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void load(@NonNull MasterDetailModel model, @NonNull List<String> overviewModelIds, @NonNull List<String> treeModelIds) {
    this.model = model;
    this.overviewModelIds = overviewModelIds;
    this.treeModelIds = treeModelIds;

    updatingFromModel = true;
    try {
      boolean treeMode = "tree".equals(model.getContent().getType());
      overviewTypeField.setSelected(!treeMode);
      treeTypeField.setSelected(treeMode);
      populateCombo(treeMode, treeMode ? model.getContent().getTreeModel() : model.getContent().getOverviewModel());
    }
    finally {
      updatingFromModel = false;
    }
    validate();
  }

  /** Toggling the radio group switches the combobox between Tree Models and Overview Models. */
  private void onTypeChanged() {
    if (updatingFromModel || model == null) {
      return;
    }
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      populateCombo(treeTypeField.isSelected(), null);
    }
    finally {
      updatingFromModel = wasUpdating;
    }
    applySelection();
    onChange.run();
  }

  private void populateCombo(boolean treeMode, String valueToSelect) {
    mainModelField.getItems().setAll(treeMode ? treeModelIds : overviewModelIds);
    mainModelField.setValue(valueToSelect);
  }

  /** Writes the current radio/combo selection back into {@code content}, clearing the other reference field. */
  private void applySelection() {
    boolean treeMode = treeTypeField.isSelected();
    String selectedId = mainModelField.getValue();
    model.getContent().setType(treeMode ? "tree" : "overview");
    model.getContent().setTreeModel(treeMode ? selectedId : null);
    model.getContent().setOverviewModel(treeMode ? null : selectedId);
  }

  /** The combo box reference is required regardless of which radio button is selected. */
  private void validate() {
    if (mainModelField.getValue() == null) {
      errorContainerController.show("ERROR", "This field is required.");
    }
    else {
      errorContainerController.hide();
    }
  }
}
