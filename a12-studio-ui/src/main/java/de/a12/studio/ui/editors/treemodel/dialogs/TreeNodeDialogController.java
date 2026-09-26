package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Add/edit dialog for a single {@link TreeNode} (node type), opened from {@link
 * de.a12.studio.ui.editors.treemodel.TreeNodeTypesPanelController} by its Add button or a node type row's edit
 * button. Only asks for the Document Model, like SME; drag &amp; drop and the column mapping are edited in the
 * configuration panel below the node types list. On OK, builds a brand new {@link TreeNode} carrying just the
 * chosen Document Model (see {@link #getResult()}) rather than mutating the one passed to {@link #init},
 * mirroring {@link TreeColumnDialogController}; the caller copies it onto the existing node (edit, so everything
 * else on it stays untouched) or gives the new node an id and appends it (add).
 */
public class TreeNodeDialogController implements DialogController {

  @FXML
  private ComboBox<String> documentModelField;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private TreeNode built;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    okButton.disableProperty().bind(documentModelField.valueProperty().isNull());
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    TreeNode node = new TreeNode();
    node.setDocumentModelRef(documentModelField.getValue());

    built = node;
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  void init(Stage stage, ProjectItem projectItem, TreeNode existing) {
    this.stage = stage;

    documentModelField.getItems().setAll(ColumnMappingEditor.documentModelIds(projectItem));
    documentModelField.setValue(existing != null ? existing.getDocumentModelRef() : null);
  }

  Optional<TreeNode> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      return Optional.ofNullable(built);
    }
    return Optional.empty();
  }
}
