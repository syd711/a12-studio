package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.projects.settings.annotations.AnnotationFieldRegistry;
import de.a12.studio.models.projects.settings.annotations.AnnotationHeaderRegistry;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.events.PreferencesOpenRequestedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import javafx.fxml.FXML;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Edits either a single {@link Element}'s annotations (via {@link #setElement}, suggestions sourced from
 * {@link AnnotationFieldRegistry}) or a model's header annotations (via {@link #setModel}, suggestions sourced
 * from {@link AnnotationHeaderRegistry}). Which mode is active is determined by which of {@code element}
 * (inherited from {@link AbstractPropertyEditor}) / {@link #model} is currently set; the two are mutually
 * exclusive.
 */
public class AnnotationsPanelController extends AbstractPropertyEditor {

  // Managed separately by RoleEditorPanelController; hidden here so it isn't shown/edited twice.
  private static final String ROLES_ANNOTATION_NAME = "roles";

  @FXML
  private GridPane annotationsGrid;

  @FXML
  private Label emptyLabel;

  @FXML
  private Button annotationDatasetsButton;

  private A12Model<?> model;

  // The model type of the element currently being edited, i.e. the key under which suggested names are
  // looked up in and reported to the AnnotationFieldRegistry / AnnotationHeaderRegistry. Recomputed on every
  // rebuildRows().
  private ModelType currentModelType;

  // The name combo boxes for the rows currently displayed, kept around so a name edit (which doesn't rebuild
  // the rows) can still refresh every row's suggestions once the registry changes.
  private final List<ComboBox<String>> nameFields = new ArrayList<>();

  // Editing an annotation revalidates the whole element, so any error surfaced here would typically be
  // about some unrelated field rather than the annotation itself; showing it in this panel is misleading.
  @Override
  protected boolean suppressErrorContainer() {
    return true;
  }

  @Override
  public void setElement(@NonNull Element element) {
    this.model = null;
    super.setElement(element);
    rebuildRows();
  }

  public void setModel(@NonNull A12Model<?> model) {
    this.element = null;
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    getBackingAnnotations().add(new Annotation());
    rebuildRows();
    commitChange();
  }

  @FXML
  private void onAnnotationDatasets() {
    StudioEventManager.getInstance().firePreferencesOpenRequestedEvent(PreferencesOpenRequestedEvent.Section.ANNOTATION_SETS);
  }

  // Used when this panel is embedded in a modal dialog (e.g. ModelSettingsDialog), where opening the
  // Preferences window on top of it would be confusing.
  public void hideAnnotationDatasetsButton() {
    annotationDatasetsButton.setVisible(false);
    annotationDatasetsButton.setManaged(false);
  }

  private List<Annotation> getBackingAnnotations() {
    return model != null ? model.getAnnotations() : element.getAnnotations();
  }

  // Excludes the "roles" annotation in header mode; it is edited via RoleEditorPanelController instead.
  private List<Annotation> getAnnotations() {
    List<Annotation> backing = getBackingAnnotations();
    if (model == null) {
      return backing;
    }
    List<Annotation> visible = new ArrayList<>();
    for (Annotation annotation : backing) {
      if (!ROLES_ANNOTATION_NAME.equals(annotation.getName())) {
        visible.add(annotation);
      }
    }
    return visible;
  }

  private void rebuildRows() {
    annotationsGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });
    nameFields.clear();

    List<String> suggestedNames;
    if (model != null) {
      currentModelType = model.getModelType();
      suggestedNames = Studio.getCurrentProject().getAnnotationHeaderRegistry().getNames(currentModelType);
    } else {
      ProjectItem projectItem = Studio.getSelectedProjectItem();
      currentModelType = projectItem == null || projectItem.getModel() == null ? null : projectItem.getModel().getModelType();
      suggestedNames = Studio.getCurrentProject().getAnnotationFieldRegistry().getNames(currentModelType);
    }

    List<Annotation> annotations = getAnnotations();
    boolean empty = annotations.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    annotationsGrid.setVisible(!empty);
    annotationsGrid.setManaged(!empty);
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
    bindTextField(valueField, (el, value) -> {
      annotation.setValue(value);
      setSuggestionValue(annotation.getName(), value);
    });

    bindComboBox(nameField, (el, value) -> {
      String oldName = annotation.getName();
      annotation.setName(value);
      onAnnotationNameChanged(annotation, valueField, oldName, value);
    });
    nameFields.add(nameField);

    annotationsGrid.addRow(index + 1, nameField, valueField, createActionsBox(annotation, index, rowCount));
  }

  /**
   * Keeps the suggestion registry in sync as an annotation's name is typed/selected, then refreshes every
   * visible row's suggestions so the new name is immediately offered elsewhere (and a name that's no longer
   * used anywhere stops being suggested). When {@code newName} is a previously used name, also prefills
   * {@code valueField} with the value it was last used with.
   */
  private void onAnnotationNameChanged(Annotation annotation, TextField valueField, String oldName, String newName) {
    if (Objects.equals(oldName, newName)) {
      return;
    }
    String suggestedValue = getSuggestionValue(newName);
    if (suggestedValue != null && !suggestedValue.equals(annotation.getValue())) {
      annotation.setValue(suggestedValue);
      setFieldValue(valueField, suggestedValue);
    }
    removeSuggestionName(oldName);
    addSuggestionName(newName, annotation.getValue());
    refreshNameSuggestions();
  }

  private void refreshNameSuggestions() {
    List<String> suggestedNames = model != null
        ? Studio.getCurrentProject().getAnnotationHeaderRegistry().getNames(currentModelType)
        : Studio.getCurrentProject().getAnnotationFieldRegistry().getNames(currentModelType);
    for (ComboBox<String> nameField : nameFields) {
      nameField.getItems().setAll(suggestedNames);
    }
  }

  private String getSuggestionValue(String name) {
    return model != null
        ? Studio.getCurrentProject().getAnnotationHeaderRegistry().getValue(currentModelType, name)
        : Studio.getCurrentProject().getAnnotationFieldRegistry().getValue(currentModelType, name);
  }

  private void setSuggestionValue(String name, String value) {
    if (model != null) {
      Studio.getCurrentProject().getAnnotationHeaderRegistry().setValue(currentModelType, name, value);
    } else {
      Studio.getCurrentProject().getAnnotationFieldRegistry().setValue(currentModelType, name, value);
    }
  }

  private void addSuggestionName(String name, String value) {
    if (model != null) {
      Studio.getCurrentProject().getAnnotationHeaderRegistry().addName(currentModelType, name, value);
    } else {
      Studio.getCurrentProject().getAnnotationFieldRegistry().addName(currentModelType, name, value);
    }
  }

  private void removeSuggestionName(String name) {
    if (model != null) {
      Studio.getCurrentProject().getAnnotationHeaderRegistry().removeName(currentModelType, name);
    } else {
      Studio.getCurrentProject().getAnnotationFieldRegistry().removeName(currentModelType, name);
    }
  }

  private HBox createActionsBox(Annotation annotation, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button copyButton = RowFactory.createActionButton(Icons.COPY, "Copy", () -> {
      Annotation copy = new Annotation();
      copy.setName(annotation.getName());
      copy.setValue(annotation.getValue());
      List<Annotation> backing = getBackingAnnotations();
      backing.add(backing.indexOf(annotation) + 1, copy);
      addSuggestionName(copy.getName(), copy.getValue());
      rebuildRows();
      commitChange();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this annotation?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getBackingAnnotations().remove(annotation);
        removeSuggestionName(annotation.getName());
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    List<Annotation> visible = getAnnotations();
    Annotation moved = visible.get(fromIndex);
    Annotation neighbor = visible.get(toIndex);
    List<Annotation> backing = getBackingAnnotations();
    Collections.swap(backing, backing.indexOf(moved), backing.indexOf(neighbor));
    rebuildRows();
    commitChange();
  }
}
