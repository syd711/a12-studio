package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.application.ApplicationUniqueNamesValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.applicationmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits a {@link Scene}'s {@link Case} list: a non-inline-editable row list (row = Name summary, edited via a
 * modal dialog), matching the SME reference's "Cases" table. Follows the same row-list pattern as {@link
 * SceneChangePanelController}'s onEnter/onExit tables. Isn't wired through {@link
 * de.a12.studio.ui.editors.AbstractPropertyEditor} for the same reason as {@link
 * MatchConditionsPanelController}, but still gets its own {@code errorContainer} (queried directly via {@link
 * de.a12.studio.modelsvalidation.ValidationService#validateElement}) to surface {@link
 * ApplicationUniqueNamesValidator}'s case-name-uniqueness check, scoped to this panel's {@link #scene}.
 */
public class CasesPanelController {

  @FXML
  private HBox headerRow;

  @FXML
  private VBox rows;

  @FXML
  private Label emptyLabel;

  @FXML
  private ErrorContainerController errorContainerController;

  private Scene scene;

  private Runnable onChange = () -> {
  };

  public void setScene(@NonNull Scene scene) {
    this.scene = scene;
    rebuildRows();
  }

  /**
   * Invoked after every add/remove/reorder/edit, so the owning Scene dialog can keep its Default Case
   * dropdown's items in sync with the current case names.
   */
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  @FXML
  private void onAdd() {
    Dialogs.showCaseForAdd(Studio.stage).ifPresent(newCase -> {
      scene.getCases().add(newCase);
      rebuildRows();
      onChange.run();
    });
  }

  private void rebuildRows() {
    refreshNameUniquenessError();
    rows.getChildren().clear();

    List<Case> cases = scene.getCases();
    boolean empty = cases.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    headerRow.setVisible(!empty);
    headerRow.setManaged(!empty);

    for (int index = 0; index < cases.size(); index++) {
      rows.getChildren().add(createRow(cases.get(index), index, cases.size()));
    }
  }

  /**
   * Queries {@link ApplicationUniqueNamesValidator}'s dedicated case-name element id, scoped to this panel's
   * {@link #scene}, directly against the current project's model, since this panel isn't an {@link
   * de.a12.studio.ui.editors.AbstractPropertyEditor} and so has no access to its element-keyed validation
   * plumbing. Called from {@link #rebuildRows} (itself called by every mutation here, plus {@link #setScene}).
   */
  private void refreshNameUniquenessError() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    A12Model<?> model = projectItem == null ? null : projectItem.getModel();
    if (model == null) {
      errorContainerController.hide();
      return;
    }
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, ApplicationUniqueNamesValidator.casesElementId(scene.getName()));
    if (errors.isEmpty()) {
      errorContainerController.hide();
    } else {
      ModelValidationError error = errors.get(0);
      errorContainerController.show(error.severity(), error.message());
    }
  }

  private HBox createRow(Case caseObj, int index, int rowCount) {
    Label nameLabel = new Label(caseObj.getName() == null ? "" : caseObj.getName());
    nameLabel.setId("case-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    nameLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    HBox row = new HBox(10.0, nameLabel, createActionsBox(caseObj, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    nameLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        onEditCase(caseObj);
      }
    });
    return row;
  }

  private HBox createActionsBox(Case caseObj, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveCase);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> onEditCase(caseObj));
    Button copyButton = RowFactory.createActionButton(Icons.COPY, StudioBundle.get("duplicate"), () -> {
      scene.getCases().add(index + 1, cloneCase(caseObj));
      rebuildRows();
      onChange.run();
    });
    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> onDeleteCase(caseObj));

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void onEditCase(Case caseObj) {
    if (Dialogs.showCaseForEdit(Studio.stage, caseObj)) {
      rebuildRows();
      onChange.run();
    }
  }

  private void onDeleteCase(Case caseObj) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_case"), null, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    scene.getCases().remove(caseObj);
    rebuildRows();
    onChange.run();
  }

  private void moveCase(int fromIndex, int toIndex) {
    Collections.swap(scene.getCases(), fromIndex, toIndex);
    rebuildRows();
    onChange.run();
  }

  private static Case cloneCase(@NonNull Case caseObj) {
    String json = JsonSettings.objectMapper.writeValueAsString(caseObj);
    return JsonSettings.objectMapper.readValue(json, Case.class);
  }
}
