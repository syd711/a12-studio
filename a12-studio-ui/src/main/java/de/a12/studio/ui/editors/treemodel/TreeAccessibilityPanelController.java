package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a {@link TreeModel}'s {@link TreeConfiguration#getLabelHidden()} (SME's "Hide Label"): the model label
 * is always announced to screen readers, and this only decides whether it is also shown in the tree's header.
 * Written as {@code true} or omitted, never {@code false}, like SME. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreeAccessibilityPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox hideLabelField;

  private TreeModel model;

  // Set while hideLabelField is being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    hideLabelField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (model.getContent().getConfiguration() == null) {
        model.getContent().setConfiguration(new TreeConfiguration());
      }
      model.getContent().getConfiguration().setLabelHidden(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      hideLabelField.setSelected(model.getContent().getConfiguration() != null
          && Boolean.TRUE.equals(model.getContent().getConfiguration().getLabelHidden()));
    }
    finally {
      updatingFromModel = false;
    }
  }
}
