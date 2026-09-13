package de.a12.studio.ui.editors.selectionmodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.selectionmodel.PathSpecification;
import de.a12.studio.models.selectionmodel.SelectionCategory;
import de.a12.studio.models.selectionmodel.SelectionDefault;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edits one of a {@link SelectionModel}'s three identically-shaped sections (Data/Computation/Validation,
 * see {@link SelectionCategory}): reused three times by {@link SelectionModelEditorController} (one
 * {@code fx:include} per section, each calling {@link #setCategory} with a different backing object and
 * title - the same runtime-retitled-shared-controller pattern as e.g. {@code
 * DataTypeDateConfigurationPanelController}), instead of three near-duplicate FXML/controller pairs.
 *
 * <p>Not bound to a single {@link de.a12.studio.models.documentmodel.Element} - a Selection Model has no
 * element graph of its own - so every field here is wired with a plain listener + a local {@link
 * #updatingFromModel} guard and saved via {@link #commitHeaderChange()}, following {@code
 * LayoutPanelController}/{@code ModelSettingsNamePanelController} rather than the inherited {@code
 * bindTextField}/{@code bindComboBox} helpers: those always call the inherited (element-bound)
 * {@code commitChange()}, which - since this panel has no {@link #element} - unconditionally hides this
 * panel's error container on every debounced commit (see {@code LayoutPanelController}'s javadoc for the
 * same reasoning), clobbering the validation error this panel actually wants to show.
 */
public class SelectionCategoryPanelController extends AbstractPropertyEditor implements Initializable {

  private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

  private static final String SELECTED_LIST_NAME = "Selected";
  private static final String UNSELECTED_LIST_NAME = "Unselected";

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<String> defaultComboBox;

  @FXML
  private GridPane selectedGrid;

  @FXML
  private Label selectedEmptyLabel;

  @FXML
  private GridPane unselectedGrid;

  @FXML
  private Label unselectedEmptyLabel;

  private SelectionModel model;
  private String jsonCategoryName;
  private SelectionCategory category;

  // Set while defaultComboBox is being repopulated programmatically (setCategory()), so its listener below
  // doesn't mistake that for a user edit and write it straight back - same guard LayoutPanelController uses
  // for its own manually-wired settingsArea.
  private boolean updatingFromModel;

  // Every path TextField currently shown, keyed by list position (matching this category's
  // ModelValidationError#elementId() suffix), so refreshValidation() can toggle the error pseudo-class on
  // exactly the offending row without rebuilding the whole grid.
  private final List<TextField> selectedFields = new ArrayList<>();
  private final List<TextField> unselectedFields = new ArrayList<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    defaultComboBox.getItems().setAll(SelectionDefault.SELECTED.getValue(), SelectionDefault.UNSELECTED.getValue());
    defaultComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || newValue == null || category == null) {
        return;
      }
      category.setDefaultValue(SelectionDefault.fromValue(newValue));
      commitAndRefresh();
    });
  }

  public void setCategory(@NonNull SelectionModel model, @NonNull String jsonCategoryName, @NonNull SelectionCategory category) {
    this.model = model;
    this.jsonCategoryName = jsonCategoryName;
    this.category = category;
    // Three instances of this same controller class share the editor - without this, their expanded/
    // collapsed state (persisted per-class by AbstractPropertyEditor) would collide on one settings key.
    setSettingsKeySuffix(jsonCategoryName);
    setTitle(StudioBundle.get("selection_model." + jsonCategoryName.toLowerCase(Locale.ROOT) + "_section"));

    updatingFromModel = true;
    try {
      defaultComboBox.setValue(category.getDefaultValue() != null ? category.getDefaultValue().getValue() : null);
      rebuildRows();
    } finally {
      updatingFromModel = false;
    }
    refreshValidation();
  }

  @FXML
  private void onAddSelected() {
    getSelected().add(new PathSpecification());
    changed();
  }

  @FXML
  private void onAddUnselected() {
    getUnselected().add(new PathSpecification());
    changed();
  }

  private List<PathSpecification> getSelected() {
    if (category.getSelected() == null) {
      category.setSelected(new ArrayList<>());
    }
    return category.getSelected();
  }

  private List<PathSpecification> getUnselected() {
    if (category.getUnselected() == null) {
      category.setUnselected(new ArrayList<>());
    }
    return category.getUnselected();
  }

  private void rebuildRows() {
    rebuildList(selectedGrid, selectedEmptyLabel, selectedFields, SELECTED_LIST_NAME, category.getSelected());
    rebuildList(unselectedGrid, unselectedEmptyLabel, unselectedFields, UNSELECTED_LIST_NAME, category.getUnselected());
  }

  private void rebuildList(GridPane grid, Label emptyLabel, List<TextField> fields, String listName, List<PathSpecification> paths) {
    grid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });
    fields.clear();

    boolean empty = paths == null || paths.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    grid.setVisible(!empty);
    grid.setManaged(!empty);
    if (empty) {
      return;
    }

    for (int index = 0; index < paths.size(); index++) {
      addRow(grid, fields, listName, paths, index);
    }
  }

  private void addRow(GridPane grid, List<TextField> fields, String listName, List<PathSpecification> paths, int index) {
    PathSpecification path = paths.get(index);

    // Set the initial value before attaching the listener below, so repopulating it here is never mistaken
    // for a user edit - simpler than a shared guard flag since every row is a freshly constructed TextField.
    TextField pathField = new TextField(path.getPath());
    pathField.setId(jsonCategoryName + listName + "Path-" + index);
    pathField.setMaxWidth(Double.MAX_VALUE);
    pathField.textProperty().addListener((observable, oldValue, newValue) -> {
      path.setPath(newValue.isBlank() ? null : newValue);
      debouncer.debounce(pathField.getId(), this::commitAndRefresh, COMMIT_DEBOUNCE_MS, true);
    });
    fields.add(pathField);

    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, paths.size(), (fromIndex, toIndex) -> {
      Collections.swap(paths, fromIndex, toIndex);
      rebuildRows();
      commitAndRefresh();
    });
    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
          StudioBundle.get("selection_model.delete_this_path"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        paths.remove(path);
        changed();
      }
    });
    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);

    grid.addRow(index, pathField, actionsBox);
  }

  private void changed() {
    rebuildRows();
    commitAndRefresh();
  }

  private void commitAndRefresh() {
    commitHeaderChange();
    refreshValidation();
  }

  /**
   * Re-checks this category's validators (see {@code de.a12.studio.modelsvalidation.validators.selection})
   * and shows the first hit, if any, in this panel's own error container - same convention as {@link
   * de.a12.studio.ui.editors.combineddocumentmodel.CombinationStepsPanelController#refreshValidation()}.
   * Also toggles the "error" pseudo-class on whichever path row(s) a problem is actually about, so a
   * duplicate/invalid-pattern/both-lists error is visible on the offending row even though only the first
   * message is shown in the shared error container.
   */
  private void refreshValidation() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (model == null || projectItem == null) {
      hideError();
      return;
    }
    String prefix = "content/" + jsonCategoryName + "/";
    List<ModelValidationError> categoryErrors = Studio.getValidationService().validate(model).stream()
        .filter(error -> error.elementId() != null && error.elementId().startsWith(prefix))
        .toList();

    applyRowErrors(selectedFields, prefix + SELECTED_LIST_NAME + "/", categoryErrors);
    applyRowErrors(unselectedFields, prefix + UNSELECTED_LIST_NAME + "/", categoryErrors);

    if (categoryErrors.isEmpty()) {
      hideError();
    }
    else {
      ModelValidationError first = categoryErrors.get(0);
      showError(first.severity(), first.message());
    }
  }

  private static void applyRowErrors(List<TextField> fields, String listPrefix, List<ModelValidationError> errors) {
    for (int index = 0; index < fields.size(); index++) {
      String elementId = listPrefix + index;
      boolean hasError = errors.stream().anyMatch(error -> elementId.equals(error.elementId()));
      fields.get(index).pseudoClassStateChanged(ERROR_PSEUDO_CLASS, hasError);
    }
  }
}
