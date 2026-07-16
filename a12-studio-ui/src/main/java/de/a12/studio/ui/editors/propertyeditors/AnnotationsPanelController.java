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

import java.util.List;
import java.util.Optional;

public class AnnotationsPanelController extends AbstractPropertyEditor {

  @FXML
  private GridPane annotationsGrid;

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

    List<String> suggestedNames = resolveSuggestedNames();
    List<Annotation> annotations = element.getAnnotations();
    for (int index = 0; index < annotations.size(); index++) {
      addRow(annotations.get(index), index, annotations.size(), suggestedNames);
    }
  }

  /**
   * Annotation names previously used elsewhere in the project for the same model type and field type as the
   * element currently being edited (see {@link AnnotationFieldRegistry}), offered as suggestions in the Name
   * combo box.
   */
  private List<String> resolveSuggestedNames() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    ModelType modelType = projectItem == null || projectItem.getModel() == null ? null : projectItem.getModel().getModelType();
    return AnnotationFieldRegistry.getInstance().getNames(modelType, AnnotationFieldRegistry.resolveFieldType(element));
  }

  private void addRow(Annotation annotation, int index, int rowCount, List<String> suggestedNames) {
    ComboBox<String> nameField = new ComboBox<>();
    nameField.setId("annotationName-" + index);
    nameField.setEditable(true);
    nameField.setMaxWidth(Double.MAX_VALUE);
    nameField.getItems().setAll(suggestedNames);
    setFieldValue(nameField, annotation.getName());
    bindComboBox(nameField, (element, value) -> annotation.setName(value));

    TextField valueField = new TextField();
    valueField.setId("annotationValue-" + index);
    valueField.setMaxWidth(Double.MAX_VALUE);
    setFieldValue(valueField, annotation.getValue());
    bindTextField(valueField, (element, value) -> annotation.setValue(value));

    annotationsGrid.addRow(index + 1, nameField, valueField, createActionsBox(annotation, index, rowCount));
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
      rebuildRows();
      commitChange();
    });

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this annotation?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        element.getAnnotations().remove(index);
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
