package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Fields included in Result Set" panel embedded in {@link QueryDocumentNodePanelController}: the "All Fields
 * of the Document Model" checkbox ({@link QueryFilterableNode#getUseAllFields()}) and, when it's off, an
 * add/remove-able list of the paths in {@link QueryFilterableNode#getFields()} - mirrors SME's
 * {@code QMDocumentModelData}'s {@code useAllFields} + {@code fields} group (see
 * docs/sme-reference-comparison.md "Query Model" section). Model-header style (not tied to a single {@link
 * Element}, see {@link de.a12.studio.ui.editors.applicationmodel.LayoutPanelController}'s own doc for the same
 * pattern): manual listener + a local {@link #updatingFromModel} guard, committed via {@link
 * #commitHeaderChange()}.
 *
 * <p>The "Add" combo only ever offers paths that actually resolve to a {@link FieldElement} in the bound
 * Document Model and aren't already in {@code fields} - so, unlike SME's free-text {@code fieldPath} (validated
 * after the fact by a custom condition), an invalid field-path projection simply isn't reachable through this
 * UI at all; {@link #suppressErrorContainer()} reflects that there is nothing left for this panel to validate.
 */
public class QueryFieldsProjectionPanelController extends AbstractPropertyEditor {

  @FXML
  private CheckBox useAllFieldsCheckBox;

  @FXML
  private Label emptyLabel;

  @FXML
  private VBox fieldsBox;

  @FXML
  private ComboBox<String> addFieldCombo;

  @FXML
  private Button addButton;

  private QueryFilterableNode node;
  private DocumentModel targetDocumentModel;
  private Runnable onChange = () -> {
  };

  // Set while setNode() is repopulating useAllFieldsCheckBox from the model - see class doc.
  private boolean updatingFromModel;

  @Override
  protected boolean suppressErrorContainer() {
    return true;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    useAllFieldsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || node == null) {
        return;
      }
      node.setUseAllFields(newValue ? Boolean.TRUE : null);
      updateFieldsVisibility();
      commitHeaderChange();
      onChange.run();
    });
    addButton.setOnAction(event -> onAddField());
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void setNode(@NonNull QueryFilterableNode node, DocumentModel targetDocumentModel) {
    this.node = node;
    this.targetDocumentModel = targetDocumentModel;
    updatingFromModel = true;
    try {
      useAllFieldsCheckBox.setSelected(Boolean.TRUE.equals(node.getUseAllFields()));
    } finally {
      updatingFromModel = false;
    }
    refresh();
  }

  /** Re-renders the field-row list and the "Add" combo's offered paths against {@link #node}'s current {@code
   * fields} - called both from {@link #setNode} and whenever this panel needs to reflect a change made
   * elsewhere (the tree's own "In Result" checkboxes read/write the very same list - see
   * {@code QueryModelTreeController#toggleInResult}). */
  public void refresh() {
    if (node == null) {
      return;
    }
    rebuildFieldRows();
    rebuildAddCombo();
    updateFieldsVisibility();
  }

  private void updateFieldsVisibility() {
    boolean useAllFields = node != null && Boolean.TRUE.equals(node.getUseAllFields());
    fieldsBox.setVisible(!useAllFields);
    fieldsBox.setManaged(!useAllFields);
    addFieldCombo.setVisible(!useAllFields);
    addFieldCombo.setManaged(!useAllFields);
    addButton.setVisible(!useAllFields);
    addButton.setManaged(!useAllFields);
    boolean showEmptyLabel = !useAllFields && node != null && node.getFields().isEmpty();
    emptyLabel.setVisible(showEmptyLabel);
    emptyLabel.setManaged(showEmptyLabel);
  }

  private void rebuildFieldRows() {
    fieldsBox.getChildren().clear();
    for (String path : node.getFields()) {
      fieldsBox.getChildren().add(createFieldRow(path));
    }
  }

  private HBox createFieldRow(@NonNull String path) {
    Label label = new Label(path);
    label.getStyleClass().add("path-text");
    label.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(label, Priority.ALWAYS);

    Button removeButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("query_fields_projection.remove_field"), () -> {
      node.getFields().remove(path);
      refresh();
      commitHeaderChange();
      onChange.run();
    });

    HBox row = new HBox(8.0, label, removeButton);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
  }

  private void rebuildAddCombo() {
    List<String> available = new ArrayList<>();
    if (targetDocumentModel != null && targetDocumentModel.getContent() != null
        && targetDocumentModel.getContent().getModelRoot() != null) {
      ElementIndex index = new ElementIndex(targetDocumentModel);
      for (Element element : index.allElements()) {
        if (element instanceof FieldElement) {
          String path = index.getPath(element);
          if (!node.getFields().contains(path)) {
            available.add(path);
          }
        }
      }
    }
    addFieldCombo.getItems().setAll(available);
    addFieldCombo.setValue(null);
    addButton.setDisable(available.isEmpty());
  }

  private void onAddField() {
    String selected = addFieldCombo.getValue();
    if (node == null || selected == null || selected.isBlank()) {
      return;
    }
    node.getFields().add(selected);
    refresh();
    commitHeaderChange();
    onChange.run();
  }
}
