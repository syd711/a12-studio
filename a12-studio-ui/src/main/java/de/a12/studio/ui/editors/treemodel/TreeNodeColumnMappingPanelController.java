package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.treemodel.dialogs.ColumnMappingEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits the selected node type's {@code columns}: one row per tree column, each choosing the field of the node's
 * Document Model that column shows. Edits the node's own mapping list in place and saves on every change. Not bound
 * to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern. Call
 * {@link #setNode} again after the node's Document Model or the tree's columns changed.
 */
public class TreeNodeColumnMappingPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private GridPane columnMappingGrid;

  @FXML
  private Label noColumnsLabel;

  private TreeModel model;
  private ProjectItem projectItem;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
  }

  public void setModel(@NonNull TreeModel model, @NonNull ProjectItem projectItem) {
    this.model = model;
    this.projectItem = projectItem;
  }

  /** Binds the panel to {@code node}, or to nothing ({@code null}). */
  public void setNode(TreeNode node) {
    if (node == null || model == null) {
      ColumnMappingEditor.populate(columnMappingGrid, noColumnsLabel, List.of(), List.of(), List.of(), () -> {
      });
      return;
    }
    ColumnMappingEditor.populate(columnMappingGrid, noColumnsLabel, model.getContent().getColumns(),
        ColumnMappingEditor.fieldOptionsFor(projectItem, node.getDocumentModelRef()), node.getColumns(), this::commitHeaderChange);
  }
}
