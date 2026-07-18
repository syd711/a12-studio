package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.commons.fx.Debouncer;
import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.dataservices.models.Annotation;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.AnnotationHeaderRegistry;
import de.a12.studio.ui.util.Icons;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Edits the comma-separated {@code roles} header annotation (see {@code /header/annotations} in a model's
 * json), e.g. {@code "tester,reviewer"}. Not bound to a single {@link de.a12.studio.dataservices.models.documentmodel.Element}
 * (roles live on the model header), so {@link #setElement} is never called and only {@link #setModel} is used.
 */
public class RoleEditorPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String ROLES_ANNOTATION_NAME = "roles";
  private static final int COMMIT_DEBOUNCE_MS = 150;

  @FXML
  private GridPane rolesGrid;

  private final Debouncer debouncer = new Debouncer();

  private A12Model model;
  private ModelType currentModelType;
  private final List<String> roles = new ArrayList<>();

  // Rows added via onAdd() during the current editing session, rendered as a plain text field rather than a
  // combobox until the panel is reloaded from the model (setModel), at which point they become regular rows.
  private final Set<Integer> newRowIndices = new HashSet<>();

  public void setModel(@NonNull A12Model model) {
    this.model = model;
    this.currentModelType = model.getModelType();
    roles.clear();
    roles.addAll(parseRoles(model));
    newRowIndices.clear();
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    roles.add("");
    newRowIndices.add(roles.size() - 1);
    rebuildRows();
  }

  private void rebuildRows() {
    rolesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    for (int index = 0; index < roles.size(); index++) {
      Node roleField = newRowIndices.contains(index) ? createTextField(index) : createComboBox(index);
      rolesGrid.addRow(index + 1, roleField, createActionsBox(index));
    }
  }

  private TextField createTextField(int index) {
    TextField textField = new TextField(roles.get(index));
    textField.setId("role-" + index);
    textField.setMaxWidth(Double.MAX_VALUE);
    textField.setPromptText("New role");
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
      roles.set(index, newValue);
      debouncer.debounce(textField.getId(), this::commitRolesChange, COMMIT_DEBOUNCE_MS, true);
    });
    return textField;
  }

  private ComboBox<String> createComboBox(int index) {
    ComboBox<String> comboBox = new ComboBox<>();
    comboBox.setId("role-" + index);
    comboBox.setEditable(true);
    comboBox.setMaxWidth(Double.MAX_VALUE);
    comboBox.getItems().setAll(getSuggestedRoles());
    comboBox.setValue(roles.get(index));
    comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      roles.set(index, newValue);
      debouncer.debounce(comboBox.getId(), this::commitRolesChange, COMMIT_DEBOUNCE_MS, true);
    });
    return comboBox;
  }

  /**
   * Suggested role names for this model type, sourced from {@link AnnotationHeaderRegistry}'s most recently
   * seen value for the {@code roles} annotation (which is itself the comma-separated list this panel writes).
   */
  private List<String> getSuggestedRoles() {
    String value = AnnotationHeaderRegistry.getInstance().getValue(currentModelType, ROLES_ANNOTATION_NAME);
    if (value == null || value.isBlank()) {
      return List.of();
    }
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(v -> !v.isEmpty())
        .distinct()
        .collect(Collectors.toList());
  }

  private HBox createActionsBox(int index) {
    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this role?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        roles.remove(index);
        shiftNewRowIndices(index);
        rebuildRows();
        commitRolesChange();
      }
    });

    HBox actionsBox = new HBox(4.0, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void shiftNewRowIndices(int removedIndex) {
    Set<Integer> shifted = new HashSet<>();
    for (int existingIndex : newRowIndices) {
      if (existingIndex < removedIndex) {
        shifted.add(existingIndex);
      } else if (existingIndex > removedIndex) {
        shifted.add(existingIndex - 1);
      }
    }
    newRowIndices.clear();
    newRowIndices.addAll(shifted);
  }

  private void commitRolesChange() {
    if (model == null) {
      return;
    }

    String joined = roles.stream()
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(Collectors.joining(","));

    Annotation rolesAnnotation = findRolesAnnotation(model);
    if (joined.isEmpty()) {
      if (rolesAnnotation != null) {
        model.getAnnotations().remove(rolesAnnotation);
        AnnotationHeaderRegistry.getInstance().removeName(currentModelType, ROLES_ANNOTATION_NAME);
      }
    } else if (rolesAnnotation != null) {
      rolesAnnotation.setValue(joined);
      AnnotationHeaderRegistry.getInstance().setValue(currentModelType, ROLES_ANNOTATION_NAME, joined);
    } else {
      Annotation annotation = new Annotation();
      annotation.setName(ROLES_ANNOTATION_NAME);
      annotation.setValue(joined);
      model.getAnnotations().add(annotation);
      AnnotationHeaderRegistry.getInstance().addName(currentModelType, ROLES_ANNOTATION_NAME, joined);
    }

    commitChange();
  }

  private static Annotation findRolesAnnotation(A12Model model) {
    for (Annotation annotation : model.getAnnotations()) {
      if (ROLES_ANNOTATION_NAME.equals(annotation.getName())) {
        return annotation;
      }
    }
    return null;
  }

  private static List<String> parseRoles(A12Model model) {
    Annotation rolesAnnotation = findRolesAnnotation(model);
    if (rolesAnnotation == null || rolesAnnotation.getValue() == null || rolesAnnotation.getValue().isBlank()) {
      return List.of();
    }
    return Arrays.stream(rolesAnnotation.getValue().split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(Collectors.toList());
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
