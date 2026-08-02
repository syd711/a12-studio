package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.applicationmodel.Constraints;
import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.DirectiveType;
import de.a12.studio.models.applicationmodel.GenericDirective;
import de.a12.studio.models.applicationmodel.Layout;
import de.a12.studio.models.applicationmodel.ModelDescriptor;
import de.a12.studio.models.applicationmodel.RegionClearDirective;
import de.a12.studio.models.applicationmodel.ViewAddDirective;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Add/edit dialog for a single {@link Directive} of a {@link de.a12.studio.models.applicationmodel.SceneChange}'s
 * {@code onEnter}/{@code onExit} list, opened from {@link
 * de.a12.studio.ui.editors.applicationmodel.SceneChangePanelController}. Since a directive's shape depends on its
 * {@link DirectiveType} (a {@link RegionClearDirective} carries a {@link Layout}, a {@link ViewAddDirective}
 * carries a component name/{@link Constraints}/model list/configuration), this dialog always builds and returns a
 * brand new {@link Directive} instance on OK (see {@link #getResult()}) rather than mutating the one passed to
 * {@link #init}; the caller is responsible for replacing (edit) or appending (add) it in the owning list. An
 * existing {@link GenericDirective} (an unrecognized/future directive type, see {@link DirectiveType#OTHER}) is
 * only offered region editing, since its free-form {@code config} isn't modeled here.
 */
public class DirectiveDialogController implements DialogController {

  private static final List<String> LAYOUTS = List.of("ApplicationFrame", "MasterDetail", "Dashboard", "Stash", "Null");

  private static final List<String> CONSTRAINTS_TYPES = List.of("MasterDetail");

  private static final String INVALID_JSON_MESSAGE = "Please enter a valid JSON object, e.g. {\"key\": \"value\"}.";

  @FXML
  private ComboBox<String> typeCombo;

  @FXML
  private TextField regionField;

  @FXML
  private Label genericNoticeLabel;

  @FXML
  private VBox regionClearSection;

  @FXML
  private ComboBox<String> layoutNameCombo;

  @FXML
  private TextArea layoutSettingsArea;

  @FXML
  private VBox viewAddSection;

  @FXML
  private TextField viewAddNameField;

  @FXML
  private ComboBox<String> constraintsTypeCombo;

  @FXML
  private TextField preferredWidthField;

  @FXML
  private GridPane modelsGrid;

  @FXML
  private Label modelsEmptyLabel;

  @FXML
  private TextArea configurationArea;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private GenericDirective genericDirective;

  private final List<ModelDescriptor> workingModels = new ArrayList<>();

  private Optional<Directive> result = Optional.empty();

  @FXML
  private void initialize() {
    typeCombo.getItems().addAll(DirectiveType.REGION_CLEAR.getValue(), DirectiveType.VIEW_ADD.getValue());
    typeCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateSections());
    layoutNameCombo.getItems().addAll(LAYOUTS);
    constraintsTypeCombo.getItems().addAll(CONSTRAINTS_TYPES);
    WidgetFactory.restrictToNumericInput(preferredWidthField);
  }

  void init(Stage stage, Directive existing) {
    this.stage = stage;

    if (existing instanceof GenericDirective generic) {
      this.genericDirective = generic;
      typeCombo.setValue(DirectiveType.OTHER.getValue());
      typeCombo.setDisable(true);
    } else {
      typeCombo.setValue(existing instanceof ViewAddDirective ? DirectiveType.VIEW_ADD.getValue() : DirectiveType.REGION_CLEAR.getValue());
    }

    regionField.setText(existing != null ? String.join(", ", existing.getRegion()) : "");

    if (existing instanceof RegionClearDirective regionClear && regionClear.getLayout() != null) {
      layoutNameCombo.setValue(regionClear.getLayout().getName());
      layoutSettingsArea.setText(toText(regionClear.getLayout().getSettings()));
    } else {
      layoutSettingsArea.setText("{}");
    }

    if (existing instanceof ViewAddDirective viewAdd) {
      viewAddNameField.setText(viewAdd.getName());
      if (viewAdd.getConstraints() != null) {
        constraintsTypeCombo.setValue(viewAdd.getConstraints().getType());
        preferredWidthField.setText(viewAdd.getConstraints().getPreferredWidth() != null ? viewAdd.getConstraints().getPreferredWidth().toString() : "");
      }
      workingModels.addAll(viewAdd.getModels());
      configurationArea.setText(toText(viewAdd.getConfiguration()));
    } else {
      configurationArea.setText("{}");
    }

    rebuildModelsRows();
    updateSections();
  }

  private void updateSections() {
    String type = typeCombo.getValue();
    boolean isGeneric = genericDirective != null;
    boolean isRegionClear = !isGeneric && DirectiveType.REGION_CLEAR.getValue().equals(type);
    boolean isViewAdd = !isGeneric && DirectiveType.VIEW_ADD.getValue().equals(type);

    genericNoticeLabel.setVisible(isGeneric);
    genericNoticeLabel.setManaged(isGeneric);
    regionClearSection.setVisible(isRegionClear);
    regionClearSection.setManaged(isRegionClear);
    viewAddSection.setVisible(isViewAdd);
    viewAddSection.setManaged(isViewAdd);
  }

  @FXML
  private void onAddModel() {
    workingModels.add(new ModelDescriptor());
    rebuildModelsRows();
  }

  private void rebuildModelsRows() {
    modelsGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    boolean empty = workingModels.isEmpty();
    modelsGrid.setVisible(!empty);
    modelsGrid.setManaged(!empty);
    modelsEmptyLabel.setVisible(empty);
    modelsEmptyLabel.setManaged(empty);

    for (int index = 0; index < workingModels.size(); index++) {
      addModelRow(workingModels.get(index), index, workingModels.size());
    }
  }

  private void addModelRow(ModelDescriptor descriptor, int index, int rowCount) {
    ComboBox<String> modelTypeField = new ComboBox<>();
    modelTypeField.setId("directiveModelType-" + index);
    modelTypeField.setMaxWidth(Double.MAX_VALUE);
    for (ModelType modelType : ModelType.values()) {
      modelTypeField.getItems().add(modelType.getDisplayName());
    }
    if (descriptor.getModelType() != null) {
      modelTypeField.setValue(descriptor.getModelType().getDisplayName());
    }
    modelTypeField.valueProperty().addListener((observable, oldValue, newValue) -> descriptor.setModelType(modelTypeByDisplayName(newValue)));

    TextField nameField = new TextField(descriptor.getName());
    nameField.setId("directiveModelName-" + index);
    nameField.setMaxWidth(Double.MAX_VALUE);
    nameField.textProperty().addListener((observable, oldValue, newValue) -> descriptor.setName(newValue.isEmpty() ? null : newValue));

    TextField documentModelField = new TextField(descriptor.getDocumentModel());
    documentModelField.setId("directiveModelDocumentModel-" + index);
    documentModelField.setMaxWidth(Double.MAX_VALUE);
    documentModelField.textProperty().addListener((observable, oldValue, newValue) -> descriptor.setDocumentModel(newValue.isEmpty() ? null : newValue));

    modelsGrid.addRow(index + 1, modelTypeField, nameField, documentModelField, createModelActionsBox(descriptor, index, rowCount));
  }

  private HBox createModelActionsBox(ModelDescriptor descriptor, int index, int rowCount) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", () -> moveModel(index, index - 1));
    moveUpButton.setDisable(index == 0);
    moveUpButton.getStyleClass().addAll("move-button", "move-button-top");

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", () -> moveModel(index, index + 1));
    moveDownButton.setDisable(index == rowCount - 1);
    moveDownButton.getStyleClass().addAll("move-button", "move-button-bottom");

    VBox moveButtonsBox = new VBox(1, moveUpButton, moveDownButton);

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      workingModels.remove(descriptor);
      rebuildModelsRows();
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveModel(int fromIndex, int toIndex) {
    Collections.swap(workingModels, fromIndex, toIndex);
    rebuildModelsRows();
  }

  private static Button createActionButton(String iconLiteral, String tooltip, Runnable action) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(WidgetFactory.createTooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }

  private static ModelType modelTypeByDisplayName(String displayName) {
    if (displayName == null) {
      return null;
    }
    for (ModelType modelType : ModelType.values()) {
      if (modelType.getDisplayName().equals(displayName)) {
        return modelType;
      }
    }
    return null;
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    Directive directive = buildDirective();
    if (directive == null) {
      return;
    }
    directive.setRegion(splitRegion(regionField.getText()));
    result = Optional.of(directive);
    stage.close();
  }

  private Directive buildDirective() {
    if (genericDirective != null) {
      return genericDirective;
    }

    if (DirectiveType.VIEW_ADD.getValue().equals(typeCombo.getValue())) {
      Map<String, Object> configuration = parseJsonObject(configurationArea.getText());
      if (configuration == null) {
        WidgetFactory.showAlert(Studio.stage, INVALID_JSON_MESSAGE);
        return null;
      }
      ViewAddDirective viewAdd = new ViewAddDirective();
      viewAdd.setName(viewAddNameField.getText());
      viewAdd.setConstraints(buildConstraints());
      viewAdd.getModels().addAll(workingModels);
      viewAdd.getConfiguration().putAll(configuration);
      return viewAdd;
    }

    Map<String, Object> settings = parseJsonObject(layoutSettingsArea.getText());
    if (settings == null) {
      WidgetFactory.showAlert(Studio.stage, INVALID_JSON_MESSAGE);
      return null;
    }
    RegionClearDirective regionClear = new RegionClearDirective();
    if (layoutNameCombo.getValue() != null || !settings.isEmpty()) {
      Layout layout = new Layout();
      layout.setName(layoutNameCombo.getValue());
      layout.setSettings(settings);
      regionClear.setLayout(layout);
    }
    return regionClear;
  }

  private Constraints buildConstraints() {
    String type = constraintsTypeCombo.getValue();
    String widthText = preferredWidthField.getText();
    if ((type == null || type.isEmpty()) && (widthText == null || widthText.isEmpty())) {
      return null;
    }
    Constraints constraints = new Constraints();
    constraints.setType(type);
    if (widthText != null && !widthText.isEmpty()) {
      constraints.setPreferredWidth(Integer.valueOf(widthText));
    }
    return constraints;
  }

  Optional<Directive> getResult() {
    return result;
  }

  private static String toText(Map<String, Object> settings) {
    if (settings == null || settings.isEmpty()) {
      return "{}";
    }
    return JsonSettings.objectMapper.writeValueAsString(settings);
  }

  private static Map<String, Object> parseJsonObject(String text) {
    if (text == null || text.isBlank()) {
      return new LinkedHashMap<>();
    }
    try {
      Map<?, ?> raw = JsonSettings.objectMapper.readValue(text, Map.class);
      Map<String, Object> result = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : raw.entrySet()) {
        if (!(entry.getKey() instanceof String key)) {
          return null;
        }
        result.put(key, entry.getValue());
      }
      return result;
    } catch (RuntimeException e) {
      return null;
    }
  }

  private static List<String> splitRegion(String text) {
    List<String> region = new ArrayList<>();
    if (text == null || text.isBlank()) {
      return region;
    }
    for (String part : text.split(",")) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        region.add(trimmed);
      }
    }
    return region;
  }
}
