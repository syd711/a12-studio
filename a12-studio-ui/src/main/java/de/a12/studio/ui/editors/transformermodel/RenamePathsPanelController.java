package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.transformermodel.RenamePath;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerElementIds;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * The "Rename Paths" section of the Element Selection tab ({@code content.RenamePaths}): the element at an original
 * path of the generated Document Model gets a new name. One row per entry - the original path (suggested from the
 * XSD's element paths, any text accepted) and the new element name. SME's {@code RenamePaths_Repeat}.
 */
public class RenamePathsPanelController extends TransformerModelPanelController {

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
    return TransformerElementIds.RENAME_PATHS;
  }

  @FXML
  private void onAdd() {
    content().getOrCreateRenamePaths().add(new RenamePath());
    structuralChange();
  }

  @Override
  protected void rebuild() {
    rows.getChildren().clear();
    List<RenamePath> renames = content().getRenamePathsOrEmpty();
    emptyLabel.setVisible(renames.isEmpty());
    emptyLabel.setManaged(renames.isEmpty());

    for (int index = 0; index < renames.size(); index++) {
      int rowIndex = index;
      RenamePath rename = renames.get(index);

      var original = suggestionCombo(rename.getOriginalPath(), () -> elementPaths, rename::setOriginalPath, "original-" + index,
          StudioBundle.get("transformer_model.rename_paths.original_path"));
      TextField newName = textField(rename.getNewElementName(), rename::setNewElementName, "new-" + index,
          StudioBundle.get("transformer_model.rename_paths.new_element_name"));
      registerErrorTarget(TransformerElementIds.renamePath(index, "OriginalPath"), original);
      registerErrorTarget(TransformerElementIds.renamePath(index, "NewElementName"), newName);

      var arrow = new Label("→");
      var moveButtons = RowFactory.createMoveButtonsBox(rowIndex, renames.size(), (from, to) -> {
        Collections.swap(content().getOrCreateRenamePaths(), from, to);
        structuralChange();
      });
      var delete = actionButton(Icons.TRASH, "row_action.delete", () -> {
        content().getOrCreateRenamePaths().remove(rowIndex);
        structuralChange();
      });
      rows.getChildren().add(row(grow(original), arrow, grow(newName), moveButtons, delete));
    }
  }
}
