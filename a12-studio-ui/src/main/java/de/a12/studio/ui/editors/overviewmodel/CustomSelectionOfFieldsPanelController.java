package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelHeterogeneity;
import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.overview.OverviewFilterCustomFieldsValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Edits an {@link OverviewModel}'s {@code content.configuration.filterConfiguration.fields}: one draggable,
 * reorderable row per {@link FieldRef}, matching the reference metamodel's "Custom Selection Of Fields" group
 * (whose validation message is literally "Please define list Custom selection of fields.") - the {@code fields}
 * group sits right before {@code sectionData} there, so this panel is placed the same way relative to {@link
 * OverviewSectionDataPanelController} in {@code overview-model-editor.fxml}. Each row picks a Document Model
 * field via an inline combo box (see {@link OverviewElementOptions}), mirroring {@link
 * OverviewSortingPanelController}'s column picker. The row's "Subtype" combo (gap 5 of "Overview Model: gap
 * review") offers the recursive sub-types of the referenced Document Model ({@link
 * DocumentModelHeterogeneity#recursiveSubTypes}, disabled when there are none); picking one re-points that
 * row's own field picker at the sub-type's elements instead ({@link FieldRef#getSubModel()}, matching SME).
 */
public class CustomSelectionOfFieldsPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getFields().
  private static final DataFormat FIELD_INDEX = new DataFormat("application/x-a12-overview-custom-field-index");

  // Matches the fixed-width spacer reserved after the "Field" header in custom-selection-of-fields-panel.fxml,
  // so rows' move/delete buttons line up under it instead of stealing space from the Subtype/Field columns.
  private static final double ACTIONS_BOX_WIDTH = 70.0;

  @FXML
  private Label subtypeInfoIcon;

  @FXML
  private HBox fieldColumnHeaders;

  @FXML
  private VBox fieldRows;

  @FXML
  private Label fieldsEmptyLabel;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  private List<DocumentModel> otherDocumentModels = List.of();

  // Set while a row's combo box is being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(subtypeInfoIcon, StudioBundle.get("for_heterogeneous_data_please_specify_a_subtype_to_add_field_"));
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    rebuildRows();
  }

  /** Only relevant for {@link FilterConfiguration#FILTER_MODE_CUSTOM_LIST} - hidden for every other filter
   * mode, see {@link OverviewModelEditorController}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /** Re-points every row's field picker at the currently referenced Document Model, and every row's Subtype
   * picker at that model's own recursive sub-types (from {@code otherDocumentModels}, the whole project's
   * Document Models - needed for the heterogeneity graph, unlike {@code documentModelIndex} alone). */
  public void setDocumentModelIndex(ElementIndex documentModelIndex, List<DocumentModel> otherDocumentModels) {
    this.documentModelIndex = documentModelIndex;
    this.otherDocumentModels = otherDocumentModels != null ? otherDocumentModels : List.of();
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    getFields().add(new FieldRef());
    rebuildRows();
    commitHeaderChange();
  }

  private List<FieldRef> getFields() {
    return ensureFilterConfiguration().getFields();
  }

  /**
   * Reflects any {@link OverviewFilterCustomFieldsValidator} problem still present among {@link #getFields()}
   * in this panel's own error container - the per-row inline styling in {@link #updateFieldValidationState}
   * flags an unresolved reference on its own combo box, but only this also drives {@link
   * de.a12.studio.ui.util.TabErrorBadge}, so switching to another tab doesn't hide the fact that a custom
   * field is still unresolved (or the selection is empty/has a duplicate).
   */
  private void refreshValidationError() {
    List<ModelValidationError> errors = Studio.getValidationService().validate(model);
    errors.stream()
        .filter(error -> OverviewFilterCustomFieldsValidator.ELEMENT_ID.equals(error.elementId()))
        .findFirst()
        .ifPresentOrElse(error -> showError(error.severity(), error.message()), this::hideError);
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    refreshValidationError();
    fieldRows.getChildren().clear();

    List<FieldRef> fields = currentFields();
    boolean empty = fields.isEmpty();
    fieldsEmptyLabel.setVisible(empty);
    fieldsEmptyLabel.setManaged(empty);
    fieldColumnHeaders.setVisible(!empty);
    fieldColumnHeaders.setManaged(!empty);

    for (int index = 0; index < fields.size(); index++) {
      fieldRows.getChildren().add(createRow(fields.get(index), index, fields.size()));
    }
  }

  private List<FieldRef> currentFields() {
    FilterConfiguration filterConfiguration = currentFilterConfiguration();
    return filterConfiguration != null ? filterConfiguration.getFields() : List.of();
  }

  private HBox createRow(FieldRef fieldRef, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    List<String> subTypes = baseModelId() != null
        ? DocumentModelHeterogeneity.recursiveSubTypes(otherDocumentModels, baseModelId()) : List.of();
    ComboBox<String> subtypeField = new ComboBox<>();
    subtypeField.setId("customFieldSubtype-" + index);
    subtypeField.setPromptText(StudioBundle.get("select_a_field"));
    subtypeField.setMaxWidth(Double.MAX_VALUE);
    subtypeField.setDisable(subTypes.isEmpty());
    HBox.setHgrow(subtypeField, Priority.ALWAYS);
    subtypeField.getItems().setAll(subTypes);

    ComboBox<String> fieldField = new ComboBox<>();
    fieldField.setId("customFieldField-" + index);
    fieldField.setPromptText(StudioBundle.get("select_a_field"));
    fieldField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(fieldField, Priority.ALWAYS);

    updatingFromModel = true;
    try {
      subtypeField.setValue(fieldRef.getSubModel());
      populateFieldCombo(fieldField, fieldRef);
      fieldField.setValue(fieldRef.getFieldId());
    }
    finally {
      updatingFromModel = false;
    }
    updateFieldValidationState(fieldField, fieldRef);

    subtypeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      fieldRef.setSubModel(newValue);
      // The field a row's own elementRef pointed at before switching Subtype almost certainly doesn't exist
      // on the new one (a different Document Model's element ids), so it's cleared rather than kept dangling.
      fieldRef.setFieldId(null);
      updatingFromModel = true;
      try {
        populateFieldCombo(fieldField, fieldRef);
        fieldField.setValue(null);
      }
      finally {
        updatingFromModel = false;
      }
      commitHeaderChange();
      updateFieldValidationState(fieldField, fieldRef);
      refreshValidationError();
    });

    fieldField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      fieldRef.setFieldId(newValue);
      commitHeaderChange();
      updateFieldValidationState(fieldField, fieldRef);
      refreshValidationError();
    });

    GridPane contentGrid = new GridPane();
    contentGrid.setHgap(10.0);
    contentGrid.setMaxWidth(Double.MAX_VALUE);
    ColumnConstraints subtypeColumn = new ColumnConstraints();
    subtypeColumn.setPercentWidth(33.33);
    ColumnConstraints fieldColumn = new ColumnConstraints();
    fieldColumn.setPercentWidth(66.67);
    contentGrid.getColumnConstraints().addAll(subtypeColumn, fieldColumn);
    contentGrid.add(subtypeField, 0, 0);
    contentGrid.add(fieldField, 1, 0);
    HBox.setHgrow(contentGrid, Priority.ALWAYS);

    HBox actionsBox = createActionsBox(fieldRef, index, rowCount);
    actionsBox.setPrefWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMinWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMaxWidth(ACTIONS_BOX_WIDTH);
    HBox.setHgrow(actionsBox, Priority.NEVER);

    HBox row = new HBox(10.0, dragHandle, contentGrid, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, FIELD_INDEX, index, this::moveField);
    return row;
  }

  /** Flags {@code fieldField} with a red border and an explanatory tooltip when {@code fieldRef}'s field id is
   * set but doesn't resolve against {@link #effectiveIndexFor} - a dangling reference. Same "unresolved"
   * semantics as {@link OverviewColumnOptions#isUnresolvedElementRef}, whose {@link
   * OverviewColumnsPanelController} counterpart flags it on a plain summary {@code Label} instead, since here
   * the field is picked via a combo box rather than rendered as text. */
  private void updateFieldValidationState(ComboBox<String> fieldField, FieldRef fieldRef) {
    ElementIndex index = effectiveIndexFor(fieldRef);
    String fieldId = fieldRef.getFieldId();
    boolean unresolved = fieldId != null && !fieldId.isBlank() && !OverviewElementOptions.isResolved(index, fieldId);
    if (unresolved) {
      if (!fieldField.getStyleClass().contains("validation-error")) {
        fieldField.getStyleClass().add("validation-error");
      }
      fieldField.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("path_could_not_be_resolved", OverviewElementOptions.displayPath(index, fieldId))));
    }
    else {
      fieldField.getStyleClass().remove("validation-error");
      fieldField.setTooltip(null);
    }
  }

  private void populateFieldCombo(ComboBox<String> fieldField, FieldRef fieldRef) {
    ElementIndex index = effectiveIndexFor(fieldRef);
    fieldField.getItems().setAll(candidateFieldIds(index, fieldRef));
    OverviewElementOptions.applyElementRefConverter(fieldField, index);
    // Other rows and the Columns list change while this combo is already built, so refresh on every open.
    fieldField.setOnShowing(event -> {
      String current = fieldField.getValue();
      boolean wasUpdating = updatingFromModel;
      updatingFromModel = true;
      try {
        fieldField.getItems().setAll(candidateFieldIds(effectiveIndexFor(fieldRef), fieldRef));
        fieldField.setValue(current);
      }
      finally {
        updatingFromModel = wasUpdating;
      }
    });
  }

  /** SME's {@code getCustomFilterValues}: Fields and enumeration multi-selects, minus any column's dynamic
   * suffix field, minus the fields another row already selects (this row's own selection stays offered). */
  private List<String> candidateFieldIds(ElementIndex index, FieldRef fieldRef) {
    Set<String> alreadyUsed = getFields().stream()
        .filter(other -> other != fieldRef)
        .map(FieldRef::getFieldId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    return OverviewElementOptions.customSelectionFieldIds(index, OverviewElementOptions.dynamicSuffixPaths(documentModelIndex, model)).stream()
        .filter(id -> !alreadyUsed.contains(id) || id.equals(fieldRef.getFieldId()))
        .toList();
  }

  /** {@code documentModelIndex}, or - if {@code fieldRef} has a Subtype set and it resolves to a real project
   * Document Model - an index over that sub-type instead, so the field picker offers its own elements. */
  private ElementIndex effectiveIndexFor(FieldRef fieldRef) {
    String subModelId = fieldRef.getSubModel();
    if (subModelId == null || subModelId.isBlank()) {
      return documentModelIndex;
    }
    DocumentModel subModel = otherDocumentModels.stream()
        .filter(candidate -> subModelId.equals(candidate.getId()))
        .findFirst()
        .orElse(null);
    ElementIndex subModelIndex = OverviewElementOptions.indexOf(subModel, otherDocumentModels);
    return subModelIndex != null ? subModelIndex : documentModelIndex;
  }

  private String baseModelId() {
    return documentModelIndex != null && documentModelIndex.getModel() != null ? documentModelIndex.getModel().getId() : null;
  }

  private void moveField(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getFields(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitHeaderChange();
    }
  }

  private HBox createActionsBox(FieldRef fieldRef, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_field"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getFields().remove(fieldRef);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getFields(), fromIndex, toIndex);
    rebuildRows();
    commitHeaderChange();
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private FilterConfiguration ensureFilterConfiguration() {
    OverviewConfiguration configuration = ensureConfiguration();
    if (configuration.getFilterConfiguration() == null) {
      configuration.setFilterConfiguration(new FilterConfiguration());
    }
    return configuration.getFilterConfiguration();
  }

  private FilterConfiguration currentFilterConfiguration() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    return configuration != null ? configuration.getFilterConfiguration() : null;
  }
}
