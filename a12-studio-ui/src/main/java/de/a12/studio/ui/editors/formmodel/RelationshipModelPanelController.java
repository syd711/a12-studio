package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The "Relationships" panel of the Form Model editor's Overview tab ({@link FormModelEditorController#loadOverview}):
 * lists every {@link RelationshipModel} in the project connected to the Form Model's bound Document Model (via
 * {@link ProjectDocumentModels#getRelationshipModelsConnectedTo}), with drag-and-drop support so a row can be
 * dropped onto {@link FormModelTreeController}'s tree to create a {@code Binding} element pre-wired with that
 * relationship, mirroring the SME reference's relationship-model-list panel.
 */
public class RelationshipModelPanelController implements Initializable {

  // Carries the dragged RelationshipModel's id; FormModelTreeController resolves it back to a RelationshipModel
  // via the same project-wide lookup this panel itself uses to populate its list.
  public static final DataFormat RELATIONSHIP_DRAG_FORMAT = new DataFormat("application/x-a12-form-model-relationship");

  @FXML
  private StackPane listContainer;

  @FXML
  private ListView<RelationshipModel> list;

  private Label placeholderLabel;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    list.setCellFactory(view -> {
      RelationshipModelListCell cell = new RelationshipModelListCell();
      setupDragSource(cell);
      return cell;
    });
  }

  public void load(@Nullable DocumentModel documentModel, @NonNull ProjectItem projectItem) {
    List<RelationshipModel> relationshipModels = documentModel == null
        ? List.of()
        : ProjectDocumentModels.getRelationshipModelsConnectedTo(projectItem, documentModel.getId());
    list.getItems().setAll(relationshipModels);
    boolean hasAny = !relationshipModels.isEmpty();
    list.setVisible(hasAny);
    list.setManaged(hasAny);
    if (hasAny) {
      hidePlaceholder();
    }
    else {
      showPlaceholder();
    }
  }

  private void showPlaceholder() {
    if (placeholderLabel == null) {
      placeholderLabel = new Label(StudioBundle.get("form_model_relationship_panel.no_related_relationship_model"));
      placeholderLabel.setWrapText(true);
      placeholderLabel.getStyleClass().add("placeholder-label");
      placeholderLabel.setMaxWidth(220);
    }
    if (!listContainer.getChildren().contains(placeholderLabel)) {
      listContainer.getChildren().add(placeholderLabel);
    }
  }

  private void hidePlaceholder() {
    if (placeholderLabel != null) {
      listContainer.getChildren().remove(placeholderLabel);
    }
  }

  private static void setupDragSource(@NonNull RelationshipModelListCell cell) {
    cell.setOnDragDetected(event -> {
      if (cell.isEmpty() || cell.getItem() == null) {
        return;
      }
      Dragboard dragboard = cell.startDragAndDrop(TransferMode.COPY);
      ClipboardContent content = new ClipboardContent();
      content.put(RELATIONSHIP_DRAG_FORMAT, cell.getItem().getId());
      dragboard.setContent(content);
      event.consume();
    });
    cell.setOnMouseClicked(event -> {
      if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !cell.isEmpty() && cell.getItem() != null) {
        ProjectDocumentModels.openModelInEditor(cell.getItem().getId());
      }
    });
  }

  /** Read-only rendering of one {@link RelationshipModel}: icon + id, same idiom as {@code FormSourceElementTreeCell}. */
  private static class RelationshipModelListCell extends ListCell<RelationshipModel> {
    @Override
    protected void updateItem(RelationshipModel item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setGraphic(null);
        return;
      }
      Node icon = WidgetFactory.createIcon(Icons.FORM_BINDING);
      icon.getStyleClass().add("tree-icon");
      Label nameLabel = new Label(item.getId());
      nameLabel.getStyleClass().add("tree-cell-name-label");
      HBox graphic = new HBox(4, icon, nameLabel);
      graphic.setAlignment(Pos.CENTER_LEFT);
      setText(null);
      setGraphic(graphic);
    }
  }
}
