package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.transformermodel.DeletePath;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerElementIds;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * The "Delete Paths" section of the Element Selection tab ({@code content.DeletePaths}): fields and groups of the
 * generated Document Model that are left out. One row per path, suggested from the XSD's element paths (any text
 * accepted). SME's {@code DeletePaths_Repeat}.
 */
public class DeletePathsPanelController extends TransformerModelPanelController {

  @FXML
  private VBox rows;

  @FXML
  private Label emptyLabel;

  private List<String> elementPaths = List.of();

  /** The paths of the elements the transformer generates from the XSD (SME's {@code xsd:elementPaths}). */
  public void setElementPaths(@NonNull List<String> elementPaths) {
    this.elementPaths = elementPaths;
    refreshSuggestions();
  }

  @Override
  protected String errorIdPrefix() {
    return TransformerElementIds.DELETE_PATHS;
  }

  @FXML
  private void onAdd() {
    content().getOrCreateDeletePaths().add(new DeletePath());
    structuralChange();
  }

  @Override
  protected void rebuild() {
    rows.getChildren().clear();
    List<DeletePath> deletes = content().getDeletePathsOrEmpty();
    emptyLabel.setVisible(deletes.isEmpty());
    emptyLabel.setManaged(deletes.isEmpty());

    for (int index = 0; index < deletes.size(); index++) {
      int rowIndex = index;
      DeletePath delete = deletes.get(index);

      var path = suggestionCombo(delete.getPath(), () -> elementPaths, delete::setPath, "path-" + index,
          StudioBundle.get("transformer_model.delete_paths.path"));
      registerErrorTarget(TransformerElementIds.deletePath(index), path);

      var moveButtons = RowFactory.createMoveButtonsBox(rowIndex, deletes.size(), (from, to) -> {
        Collections.swap(content().getOrCreateDeletePaths(), from, to);
        structuralChange();
      });
      var remove = actionButton(Icons.TRASH, "row_action.delete", () -> {
        content().getOrCreateDeletePaths().remove(rowIndex);
        structuralChange();
      });
      rows.getChildren().add(row(grow(path), moveButtons, remove));
    }
  }
}
