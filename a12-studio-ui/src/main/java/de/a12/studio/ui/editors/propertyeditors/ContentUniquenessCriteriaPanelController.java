package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.ContentUniquenessCriterion;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Edits a {@link DocumentModel}'s {@link de.a12.studio.models.documentmodel.DocumentModelContent#getDocumentUniquenessCriteria()}
 * ({@link ContentUniquenessCriterion}) - a set of named uniqueness checks distinct from {@link
 * de.a12.studio.models.documentmodel.ModelConfig#getUniquenessCriteria()} (edited by {@link
 * DocumentUniquenessCriteriaPanelController}): this one addresses its Fields by full path string ({@link
 * ContentUniquenessCriterion.Field#getFullName()}, e.g. {@code "/Person/PersonID"}) rather than by {@code
 * Element} id. Only shown for Composed Document Models (see {@link #setVisible}). Rows here only summarize each
 * criterion; the Fields selection and per-locale Error Messages are edited in a dedicated dialog (see {@link
 * Dialogs#showContentUniquenessCriterion}), opened via Add/Edit - the same structure as {@link
 * DocumentUniquenessCriteriaPanelController}, just addressing Fields differently.
 */
public class ContentUniquenessCriteriaPanelController extends AbstractPropertyEditor {

  @FXML
  private HBox criteriaColumnHeaders;

  @FXML
  private VBox criteriaRows;

  @FXML
  private javafx.scene.control.Label criteriaEmptyLabel;

  private DocumentModel model;

  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull DocumentModel model) {
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    Dialogs.showContentUniquenessCriterion(Studio.stage, model, null, usedNames(null)).ifPresent(criterion -> {
      getCriteria().add(criterion);
      rebuildRows();
      commitChange();
    });
  }

  private List<ContentUniquenessCriterion> getCriteria() {
    return model.getContent().getDocumentUniquenessCriteria();
  }

  private void rebuildRows() {
    criteriaRows.getChildren().clear();

    List<ContentUniquenessCriterion> criteria = getCriteria();
    boolean empty = criteria.isEmpty();
    criteriaColumnHeaders.setVisible(!empty);
    criteriaColumnHeaders.setManaged(!empty);
    criteriaEmptyLabel.setVisible(empty);
    criteriaEmptyLabel.setManaged(empty);

    for (int index = 0; index < criteria.size(); index++) {
      criteriaRows.getChildren().add(createRow(criteria.get(index), index, criteria.size()));
    }
  }

  private HBox createRow(ContentUniquenessCriterion criterion, int index, int rowCount) {
    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(criterion.getName());
    nameLabel.setId("contentUniquenessCriterionName-" + index);
    nameLabel.setPrefWidth(160.0);
    makeClickableToEdit(nameLabel, criterion);

    javafx.scene.control.Label fieldsLabel = new javafx.scene.control.Label(fieldsSummary(criterion));
    fieldsLabel.setId("contentUniquenessCriterionFields-" + index);
    fieldsLabel.setWrapText(true);
    fieldsLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(fieldsLabel, Priority.ALWAYS);
    makeClickableToEdit(fieldsLabel, criterion);

    HBox row = new HBox(10.0, nameLabel, fieldsLabel, createActionsBox(criterion, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  private void makeClickableToEdit(javafx.scene.control.Label label, ContentUniquenessCriterion criterion) {
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(criterion);
      }
    });
  }

  private String fieldsSummary(ContentUniquenessCriterion criterion) {
    return criterion.getFields().stream()
        .map(ContentUniquenessCriterion.Field::getFullName)
        .collect(Collectors.joining(", "));
  }

  private void openEditDialog(ContentUniquenessCriterion criterion) {
    Dialogs.showContentUniquenessCriterion(Studio.stage, model, criterion, usedNames(criterion)).ifPresent(edited -> {
      criterion.setName(edited.getName());
      criterion.setFields(edited.getFields());
      criterion.setErrorMessage(edited.getErrorMessage());
      rebuildRows();
      commitChange();
    });
  }

  private HBox createActionsBox(ContentUniquenessCriterion criterion, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(criterion));

    Button copyButton = RowFactory.createActionButton(Icons.COPY, StudioBundle.get("copy"), () -> {
      ContentUniquenessCriterion copy = new ContentUniquenessCriterion();
      copy.setName(uniqueCopyName(criterion.getName()));
      List<ContentUniquenessCriterion.Field> fields = new ArrayList<>();
      for (ContentUniquenessCriterion.Field field : criterion.getFields()) {
        ContentUniquenessCriterion.Field fieldCopy = new ContentUniquenessCriterion.Field();
        fieldCopy.setFullName(field.getFullName());
        fields.add(fieldCopy);
      }
      copy.setFields(fields);
      for (Label label : criterion.getErrorMessage()) {
        Label labelCopy = new Label();
        labelCopy.setLocale(label.getLocale());
        labelCopy.setText(label.getText());
        copy.getErrorMessage().add(labelCopy);
      }
      List<ContentUniquenessCriterion> criteria = getCriteria();
      criteria.add(criteria.indexOf(criterion) + 1, copy);
      rebuildRows();
      commitChange();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_uniqueness_criterion"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getCriteria().remove(criterion);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getCriteria(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }

  /** Every criterion name already in use, excluding {@code editing} itself so it doesn't collide with its own name. */
  private Set<String> usedNames(ContentUniquenessCriterion editing) {
    Set<String> names = new HashSet<>();
    for (ContentUniquenessCriterion criterion : getCriteria()) {
      if (criterion != editing) {
        names.add(criterion.getName());
      }
    }
    return names;
  }

  private String uniqueCopyName(String baseName) {
    Set<String> usedNames = usedNames(null);
    String candidate = baseName + "_copy";
    int suffix = 2;
    while (usedNames.contains(candidate)) {
      candidate = baseName + "_copy" + suffix;
      suffix++;
    }
    return candidate;
  }
}
