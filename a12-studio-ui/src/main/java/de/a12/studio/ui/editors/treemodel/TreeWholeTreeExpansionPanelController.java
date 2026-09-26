package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a {@link TreeModel}'s {@link TreeConfiguration#getWholeTreeExpansion()} (SME "Enable Expand/Collapse The Whole
 * Tree"): the Tree Engine then shows a popup with Expand All and Collapse All in the subheader, placed by the Tree
 * Model's Expand All PopUp element. Written as {@code true} or omitted, never {@code false}, like SME. Not bound to a
 * single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreeWholeTreeExpansionPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox wholeTreeExpansionField;

  @FXML
  private Label infoIcon;

  private TreeModel model;

  // Set while the checkbox is being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(infoIcon, StudioBundle.get("tree_whole_tree_expansion_panel.info"));
    wholeTreeExpansionField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (model.getContent().getConfiguration() == null) {
        model.getContent().setConfiguration(new TreeConfiguration());
      }
      model.getContent().getConfiguration().setWholeTreeExpansion(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      wholeTreeExpansionField.setSelected(model.getContent().getConfiguration() != null
          && Boolean.TRUE.equals(model.getContent().getConfiguration().getWholeTreeExpansion()));
    }
    finally {
      updatingFromModel = false;
    }
  }
}
