package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.editors.propertyeditors.AbstractMultiSelectionPanelController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Edits a {@link TreeModel}'s {@code content.configuration.multiSelection} (SME "Multi-Selection" under
 * Features): the shared options and actions of {@link AbstractMultiSelectionPanelController} plus the Tree's own
 * "Select parent node when all child nodes are selected" flag, which is written as {@code true} or omitted. Not
 * bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreeMultiSelectionPanelController extends AbstractMultiSelectionPanelController {

  @FXML
  private CheckBox selectParentField;

  private TreeModel model;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    selectParentField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelection().setSelectParent(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;
    loadFromModel();
  }

  @Override
  protected boolean isModelLoaded() {
    return model != null;
  }

  @Override
  protected MultiSelectionConfig getMultiSelection() {
    return model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getMultiSelection() : null;
  }

  @Override
  protected MultiSelectionConfig createMultiSelection() {
    MultiSelectionConfig config = new MultiSelectionConfig();
    ensureConfiguration().setMultiSelection(config);
    return config;
  }

  @Override
  protected void removeMultiSelection() {
    ensureConfiguration().setMultiSelection(null);
  }

  @Override
  protected void onActionCreated(@NonNull Button button) {
    button.setId("button-" + UUID.randomUUID().toString().replace("-", "").substring(0, 5));
  }

  @Override
  protected void populateExtraFields(MultiSelectionConfig config) {
    selectParentField.setSelected(config != null && Boolean.TRUE.equals(config.getSelectParent()));
  }

  private TreeConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
