package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentUniquenessCriterion;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Edits a {@link DocumentModel}'s {@link ModelConfig#getUniquenessCriteria()}: a list of named "Document
 * Uniqueness Criteria", each specifying a set of Fields whose combined values must be unique across all
 * existing documents. Only shown for Document Models (see {@link #setVisible}). Rows here only summarize each
 * criterion; the Fields selection and per-locale Error Messages are edited in a dedicated dialog (see {@link
 * Dialogs#showUniquenessCriterion}), opened via Add/Edit.
 */
public class DocumentUniquenessCriteriaPanelController extends AbstractPropertyEditor {

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
    Dialogs.showUniquenessCriterion(Studio.stage, model, null, usedNames(null)).ifPresent(criterion -> {
      getCriteria().add(criterion);
      rebuildRows();
      commitChange();
    });
  }

  private List<DocumentUniquenessCriterion> getCriteria() {
    ModelConfig modelConfig = getModelConfig(model);
    return modelConfig != null ? modelConfig.getUniquenessCriteria() : List.of();
  }

  private void rebuildRows() {
    criteriaRows.getChildren().clear();

    List<DocumentUniquenessCriterion> criteria = getCriteria();
    boolean empty = criteria.isEmpty();
    criteriaColumnHeaders.setVisible(!empty);
    criteriaColumnHeaders.setManaged(!empty);
    criteriaEmptyLabel.setVisible(empty);
    criteriaEmptyLabel.setManaged(empty);

    for (int index = 0; index < criteria.size(); index++) {
      criteriaRows.getChildren().add(createRow(criteria.get(index), index, criteria.size()));
    }
  }

  private HBox createRow(DocumentUniquenessCriterion criterion, int index, int rowCount) {
    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(criterion.getName());
    nameLabel.setId("uniquenessCriterionName-" + index);
    nameLabel.setPrefWidth(160.0);

    javafx.scene.control.Label fieldsLabel = new javafx.scene.control.Label(fieldsSummary(criterion));
    fieldsLabel.setId("uniquenessCriterionFields-" + index);
    fieldsLabel.setWrapText(true);
    fieldsLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(fieldsLabel, Priority.ALWAYS);

    HBox row = new HBox(10.0, nameLabel, fieldsLabel, createActionsBox(criterion, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  /** Every field's path (see {@link ElementIndex#getPath}), joined for a compact row summary. */
  private String fieldsSummary(DocumentUniquenessCriterion criterion) {
    if (criterion.getFields().isEmpty()) {
      return "";
    }
    ElementIndex index = new ElementIndex(model);
    return criterion.getFields().stream()
        .map(fieldId -> resolvePath(index, fieldId))
        .collect(Collectors.joining(", "));
  }

  private static String resolvePath(ElementIndex index, String fieldId) {
    return index.allElements().stream()
        .filter(candidate -> fieldId.equals(candidate.getId()))
        .findFirst()
        .map(index::getPath)
        .orElse(fieldId);
  }

  private HBox createActionsBox(DocumentUniquenessCriterion criterion, int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(index, rowCount);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () ->
        Dialogs.showUniquenessCriterion(Studio.stage, model, criterion, usedNames(criterion)).ifPresent(edited -> {
          criterion.setName(edited.getName());
          criterion.setFields(edited.getFields());
          criterion.setErrorMessage(edited.getErrorMessage());
          rebuildRows();
          commitChange();
        }));

    Button copyButton = createActionButton(Icons.COPY, "Copy", () -> {
      DocumentUniquenessCriterion copy = new DocumentUniquenessCriterion();
      copy.setName(uniqueCopyName(criterion.getName()));
      copy.setFields(new ArrayList<>(criterion.getFields()));
      for (Label label : criterion.getErrorMessage()) {
        Label labelCopy = new Label();
        labelCopy.setLocale(label.getLocale());
        labelCopy.setText(label.getText());
        copy.getErrorMessage().add(labelCopy);
      }
      List<DocumentUniquenessCriterion> criteria = getCriteria();
      criteria.add(criteria.indexOf(criterion) + 1, copy);
      rebuildRows();
      commitChange();
    });

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this uniqueness criterion?", null, null, "Delete");
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

  // Move up/down stacked in a VBox instead of side by side in the HBox: each button is half-height (see the
  // "move-button" style class), so the pair together takes up the same width/height as a single normal button.
  private VBox createMoveButtonsBox(int index, int rowCount) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", () -> moveRow(index, index - 1));
    moveUpButton.setDisable(index == 0);
    moveUpButton.getStyleClass().addAll("move-button", "move-button-top");

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", () -> moveRow(index, index + 1));
    moveDownButton.setDisable(index == rowCount - 1);
    moveDownButton.getStyleClass().addAll("move-button", "move-button-bottom");

    return new VBox(1, moveUpButton, moveDownButton);
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getCriteria(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }

  /** Every criterion name already in use, excluding {@code editing} itself so it doesn't collide with its own name. */
  private Set<String> usedNames(DocumentUniquenessCriterion editing) {
    Set<String> names = new HashSet<>();
    for (DocumentUniquenessCriterion criterion : getCriteria()) {
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

  private static Button createActionButton(String iconLiteral, String tooltip, Runnable action) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(new Tooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }
}
