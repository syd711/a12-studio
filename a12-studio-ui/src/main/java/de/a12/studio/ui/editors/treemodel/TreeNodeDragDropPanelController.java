package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

/**
 * Edits the selected node type's {@code configuration.dnd} flag: whether its nodes may be dragged and dropped. The
 * key is only written once the user changes the checkbox, so an absent key stays absent. Not bound to a single
 * {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern; the owner learns of
 * an edit through {@link #setOnChange}, since the node types list shows the flag.
 */
public class TreeNodeDragDropPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String DND = "dnd";

  @FXML
  private CheckBox dragDropField;

  private TreeNode node;

  // Set while the field is being repopulated from the node, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    dragDropField.selectedProperty().addListener((observable, oldValue, enabled) -> {
      if (updatingFromModel || node == null) {
        return;
      }
      if (node.getConfiguration() == null) {
        node.setConfiguration(new LinkedHashMap<>());
      }
      node.getConfiguration().put(DND, enabled);
      commitHeaderChange();
      onChange.run();
    });
  }

  /** Called after the user toggled the flag. */
  public void setOnChange(Runnable onChange) {
    this.onChange = onChange;
  }

  /** Binds the panel to {@code node}, or to nothing ({@code null}). */
  public void setNode(TreeNode node) {
    this.node = node;
    updatingFromModel = true;
    try {
      dragDropField.setSelected(node != null && node.getConfiguration() != null && Boolean.TRUE.equals(node.getConfiguration().get(DND)));
    }
    finally {
      updatingFromModel = false;
    }
  }
}
