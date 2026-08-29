package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Edits an arbitrary {@code List<FieldRef>} - one row per {@link FieldRef}, each picking a Document Model
 * field via a combo box (see {@link OverviewElementOptions}), with Add/Delete but no reordering or Subtype
 * column, unlike {@link CustomSelectionOfFieldsPanelController} (which edits a different, unrelated {@code
 * filterConfiguration.fields} list and additionally supports drag-reordering and a Subtype column - not
 * reused here since this panel's owner has neither concept). Bound via {@link #setFields} with a
 * caller-supplied accessor, mirroring {@link
 * de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController#setCustom}, since an owner like
 * {@link de.a12.studio.models.overviewmodel.FilterSection} isn't a single {@link
 * de.a12.studio.models.documentmodel.Element}. First used for {@code FilterSection#getFields()} in {@link
 * de.a12.studio.ui.editors.overviewmodel.dialogs.SectionDataDialogController}.
 */
public class FieldReferencesPanelController extends AbstractPropertyEditor {

  @FXML
  private HBox fieldColumnHeaders;

  @FXML
  private VBox fieldRows;

  @FXML
  private Label fieldsEmptyLabel;

  @FXML
  private Button addButton;

  private Supplier<List<FieldRef>> fieldsSupplier;

  private ElementIndex documentModelIndex;

  // Set while a row's combo box is being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  public void setFields(@NonNull Supplier<List<FieldRef>> fieldsSupplier) {
    this.fieldsSupplier = fieldsSupplier;
    rebuildRows();
  }

  /** Re-points every row's field picker at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex) {
    this.documentModelIndex = documentModelIndex;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    FieldRef fieldRef = new FieldRef();
    fieldRef.setFieldId(firstUnselectedFieldId().orElse(null));
    getFields().add(fieldRef);
    rebuildRows();
    commitHeaderChange();
  }

  private List<FieldRef> getFields() {
    return fieldsSupplier.get();
  }

  /** The first field id (in {@link OverviewElementOptions#elementIds} order) not already used by an existing
   * row, so {@link #onAdd} pre-selects a sensible default instead of leaving the new row blank, and {@link
   * #rebuildRows} disables {@link #addButton} once this is empty - i.e. every available field is already
   * referenced. Empty (not just unresolved) whenever there's no index yet, since there's nothing to add. */
  private Optional<String> firstUnselectedFieldId() {
    List<String> usedIds = getFields().stream().map(FieldRef::getFieldId).toList();
    return OverviewElementOptions.elementIds(documentModelIndex).stream()
        .filter(id -> !usedIds.contains(id))
        .findFirst();
  }

  private void rebuildRows() {
    if (fieldsSupplier == null) {
      return;
    }
    fieldRows.getChildren().clear();

    List<FieldRef> fields = getFields();
    boolean empty = fields.isEmpty();
    fieldColumnHeaders.setVisible(!empty);
    fieldColumnHeaders.setManaged(!empty);
    fieldsEmptyLabel.setVisible(empty);
    fieldsEmptyLabel.setManaged(empty);

    for (int index = 0; index < fields.size(); index++) {
      fieldRows.getChildren().add(createRow(fields.get(index), index));
    }

    addButton.setDisable(firstUnselectedFieldId().isEmpty());
  }

  private HBox createRow(FieldRef fieldRef, int index) {
    ComboBox<String> fieldField = new ComboBox<>();
    fieldField.setId("sectionFieldField-" + index);
    fieldField.setPromptText(StudioBundle.get("select_a_field"));
    fieldField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(fieldField, Priority.ALWAYS);
    fieldField.getItems().setAll(OverviewElementOptions.elementIds(documentModelIndex));
    OverviewElementOptions.applyElementRefConverter(fieldField, documentModelIndex);

    updatingFromModel = true;
    try {
      fieldField.setValue(fieldRef.getFieldId());
    }
    finally {
      updatingFromModel = false;
    }
    updateFieldValidationState(fieldField, fieldRef);
    fieldField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      fieldRef.setFieldId(newValue);
      commitHeaderChange();
      updateFieldValidationState(fieldField, fieldRef);
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_field"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getFields().remove(fieldRef);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox row = new HBox(10.0, fieldField, deleteButton);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  /** Flags {@code fieldField} with a red border and an explanatory tooltip when {@code fieldRef}'s field id is
   * set but doesn't resolve against {@link #documentModelIndex} - a dangling reference. Mirrors {@link
   * CustomSelectionOfFieldsPanelController#updateFieldValidationState}. */
  private void updateFieldValidationState(ComboBox<String> fieldField, FieldRef fieldRef) {
    String fieldId = fieldRef.getFieldId();
    boolean unresolved = fieldId != null && !fieldId.isBlank() && !OverviewElementOptions.isResolved(documentModelIndex, fieldId);
    if (unresolved) {
      if (!fieldField.getStyleClass().contains("validation-error")) {
        fieldField.getStyleClass().add("validation-error");
      }
      fieldField.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("path_could_not_be_resolved", OverviewElementOptions.displayPath(documentModelIndex, fieldId))));
    }
    else {
      fieldField.getStyleClass().remove("validation-error");
      fieldField.setTooltip(null);
    }
  }
}
