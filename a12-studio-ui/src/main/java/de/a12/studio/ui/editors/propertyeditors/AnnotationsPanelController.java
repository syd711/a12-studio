package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.dataservices.models.Annotation;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.AnnotationFieldRegistry;
import de.a12.studio.ui.events.PreferencesOpenRequestedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AnnotationsPanelController extends AbstractPropertyEditor {

  @FXML
  private GridPane annotationsGrid;

  // The model type / field type of the element currently being edited, i.e. the key under which suggested
  // names are looked up in and reported to the AnnotationFieldRegistry. Recomputed on every rebuildRows().
  private ModelType currentModelType;
  private String currentFieldType;

  // The name combo boxes for the rows currently displayed, kept around so a name edit (which doesn't rebuild
  // the rows) can still refresh every row's suggestions once the registry changes.
  private final List<ComboBox<String>> nameFields = new ArrayList<>();

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    element.getAnnotations().add(new Annotation());
    rebuildRows();
    commitChange();
  }

  @FXML
  private void onAnnotationDatasets() {
    StudioEventManager.getInstance().firePreferencesOpenRequestedEvent(PreferencesOpenRequestedEvent.Section.ANNOTATION_SETS);
  }

  private void rebuildRows() {
    annotationsGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });
    nameFields.clear();

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    currentModelType = projectItem == null || projectItem.getModel() == null ? null : projectItem.getModel().getModelType();
    currentFieldType = AnnotationFieldRegistry.resolveFieldType(element);

    List<String> suggestedNames = AnnotationFieldRegistry.getInstance().getNames(currentModelType, currentFieldType);
    List<Annotation> annotations = element.getAnnotations();
    for (int index = 0; index < annotations.size(); index++) {
      addRow(annotations.get(index), index, annotations.size(), suggestedNames);
    }
  }

  private void addRow(Annotation annotation, int index, int rowCount, List<String> suggestedNames) {
    ComboBox<String> nameField = new ComboBox<>();
    nameField.setId("annotationName-" + index);
    nameField.setEditable(true);
    nameField.setMaxWidth(Double.MAX_VALUE);
    nameField.getItems().setAll(suggestedNames);
    setFieldValue(nameField, annotation.getName());

    TextField valueField = new TextField();
    valueField.setId("annotationValue-" + index);
    valueField.setMaxWidth(Double.MAX_VALUE);
    setFieldValue(valueField, annotation.getValue());
    bindTextField(valueField, (element, value) -> {
      annotation.setValue(value);
      AnnotationFieldRegistry.getInstance().setValue(currentModelType, currentFieldType, annotation.getName(), value);
    });

    bindComboBox(nameField, (element, value) -> {
      String oldName = annotation.getName();
      annotation.setName(value);
      onAnnotationNameChanged(annotation, valueField, oldName, value);
    });
    nameFields.add(nameField);

    annotationsGrid.addRow(index + 1, nameField, valueField, createActionsBox(annotation, index, rowCount));
  }

  /**
   * Keeps the {@link AnnotationFieldRegistry} in sync as an annotation's name is typed/selected, then refreshes
   * every visible row's suggestions so the new name is immediately offered elsewhere (and a name that's no
   * longer used anywhere stops being suggested). When {@code newName} is a previously used name, also
   * prefills {@code valueField} with the value it was last used with.
   */
  private void onAnnotationNameChanged(Annotation annotation, TextField valueField, String oldName, String newName) {
    if (Objects.equals(oldName, newName)) {
      return;
    }
    AnnotationFieldRegistry registry = AnnotationFieldRegistry.getInstance();
    String suggestedValue = registry.getValue(currentModelType, currentFieldType, newName);
    if (suggestedValue != null && !suggestedValue.equals(annotation.getValue())) {
      annotation.setValue(suggestedValue);
      setFieldValue(valueField, suggestedValue);
    }
    registry.removeName(currentModelType, currentFieldType, oldName);
    registry.addName(currentModelType, currentFieldType, newName, annotation.getValue());
    refreshNameSuggestions();
  }

  private void refreshNameSuggestions() {
    List<String> suggestedNames = AnnotationFieldRegistry.getInstance().getNames(currentModelType, currentFieldType);
    for (ComboBox<String> nameField : nameFields) {
      nameField.getItems().setAll(suggestedNames);
    }
  }

  private HBox createActionsBox(Annotation annotation, int index, int rowCount) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", () -> moveRow(index, index - 1));
    moveUpButton.setDisable(index == 0);

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", () -> moveRow(index, index + 1));
    moveDownButton.setDisable(index == rowCount - 1);

    Button copyButton = createActionButton(Icons.COPY, "Copy", () -> {
      Annotation copy = new Annotation();
      copy.setName(annotation.getName());
      copy.setValue(annotation.getValue());
      element.getAnnotations().add(index + 1, copy);
      AnnotationFieldRegistry.getInstance().addName(currentModelType, currentFieldType, copy.getName(), copy.getValue());
      rebuildRows();
      commitChange();
    });

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this annotation?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        element.getAnnotations().remove(index);
        AnnotationFieldRegistry.getInstance().removeName(currentModelType, currentFieldType, annotation.getName());
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveUpButton, moveDownButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    List<Annotation> annotations = element.getAnnotations();
    annotations.add(toIndex, annotations.remove(fromIndex));
    rebuildRows();
    commitChange();
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
