package de.a12.studio.ui.editors.querymodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.querymodel.QueryTraversalOption;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Picks one {@code <RelationshipModel, role>} pair to add as a new {@link
 * de.a12.studio.models.querymodel.QueryLink} child of the Model Tree node the user right-clicked - either the
 * target Document Model row itself, or an existing relationship-link row (for a multi-hop traversal). A plain
 * combo box rather than SME's ER-diagram element picker, per an explicit scope decision for this editor.
 */
public class QueryAddRelationshipDialogController implements DialogController {

  @FXML
  private ComboBox<QueryTraversalOption> relationshipCombo;

  @FXML
  private Button okButton;
  @FXML
  private Button cancelButton;

  private Stage stage;
  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    relationshipCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(QueryTraversalOption value) {
        return value == null ? "" : value.display();
      }

      @Override
      public QueryTraversalOption fromString(String string) {
        return null;
      }
    });
    relationshipCombo.valueProperty().addListener((observable, oldValue, newValue) -> okButton.setDisable(newValue == null));
  }

  void init(@NonNull Stage stage, @NonNull ProjectItem projectItem, @NonNull String sourceDocumentModelId) {
    this.stage = stage;
    List<QueryTraversalOption> options = QueryTraversalOption.optionsConnectedTo(projectItem, sourceDocumentModelId);
    relationshipCombo.setItems(FXCollections.observableArrayList(options));
    okButton.setDisable(true);
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  QueryTraversalOption getValue() {
    return relationshipCombo.getValue();
  }
}
