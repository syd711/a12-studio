package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.Category;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationTypeOptions;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.dialogs.CategoryDialogController;
import de.a12.studio.ui.events.LocalesChangedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits an {@link EnumerationFieldType}'s {@link EnumerationTypeOptions}: the list of {@link Category
 * categories} (each edited via a modal dialog for its name/description), the list of {@link EnumerationValue
 * enumeration values} (with a per-locale text and, for every category, an editable combobox holding that
 * value's category value), and the alphabetical-sorting flag. Row/column structure (categories, locales,
 * values) is rebuilt from scratch on every structural change; individual text/combobox edits are committed via
 * the usual {@link AbstractPropertyEditor#bindTextField}/{@link AbstractPropertyEditor#bindComboBox} debounced
 * save. Pagination of the enumeration values list is intentionally not implemented.
 */
public class DataTypeEnumerationConfigurationPanelController extends AbstractPropertyEditor implements Initializable, StudioEventListener {

  @FXML
  private VBox categoryRows;

  @FXML
  private GridPane enumerationValuesGrid;

  @FXML
  private javafx.scene.control.Label enumerationValuesEmptyLabel;

  @FXML
  private HBox buttonPanel;

  @FXML
  private CheckBox alphabeticalSortingCheckBox;

  private ProjectItem projectItem;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);
    StudioEventManager.getInstance().addListener(this);
    bindCheckBox(alphabeticalSortingCheckBox, (element, value) -> withEnumerationTypeOptions(element, options -> options.setAlphabeticalSorting(value ? true : null)));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    this.projectItem = Studio.getSelectedProjectItem();

    EnumerationTypeOptions options = getEnumerationFieldType(element).map(EnumerationFieldType::getEnumerationType).orElse(null);
    setFieldValue(alphabeticalSortingCheckBox, options != null && Boolean.TRUE.equals(options.getAlphabeticalSorting()));
    rebuildCategoryRows();
    rebuildEnumerationValuesGrid();
  }

  @Override
  public void localesChanged(@NonNull LocalesChangedEvent event) {
    if (event.getItem().equals(projectItem)) {
      rebuildEnumerationValuesGrid();
    }
  }

  @Override
  public void destroy() {
    StudioEventManager.getInstance().removeListener(this);
  }

  // ----- Category -----

  @FXML
  private void onAddCategory() {
    openCategoryDialog(null);
  }

  private List<Category> getCategories() {
    EnumerationTypeOptions options = getOptions();
    return options != null ? options.getCategories() : List.of();
  }

  private void rebuildCategoryRows() {
    categoryRows.getChildren().clear();

    List<Category> categories = getCategories();
    for (int index = 0; index < categories.size(); index++) {
      categoryRows.getChildren().add(createCategoryRow(categories.get(index), index, categories.size()));
    }
  }

  private HBox createCategoryRow(Category category, int index, int rowCount) {
    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(category.getName() == null ? "" : category.getName());
    nameLabel.setId("category-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    HBox row = new HBox(10.0, nameLabel, createCategoryActionsBox(category, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    if (category.getDescription() != null && !category.getDescription().isEmpty()) {
      Tooltip.install(row, new Tooltip(category.getDescription()));
    }
    nameLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        onEditCategory(category);
      }
    });
    return row;
  }

  private HBox createCategoryActionsBox(Category category, int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(
        () -> moveCategory(index, index - 1), index == 0,
        () -> moveCategory(index, index + 1), index == rowCount - 1);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () -> onEditCategory(category));
    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> onDeleteCategory(category));

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void onEditCategory(Category category) {
    openCategoryDialog(category);
  }

  /**
   * Opens the Add/Edit Category modal. {@code existing} is {@code null} when adding a new category, otherwise
   * the category being edited in place.
   */
  private void openCategoryDialog(Category existing) {
    String title = existing == null ? "Add Category" : "Edit Category";
    FXMLLoader fxmlLoader = new FXMLLoader(CategoryDialogController.class.getResource("category-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("category-dialog", fxmlLoader, Studio.stage, title);
    CategoryDialogController controller = (CategoryDialogController) stage.getUserData();
    controller.initDialog(stage, existing == null ? null : existing.getName(), existing == null ? null : existing.getDescription());
    stage.showAndWait();

    if (controller.getResult().isEmpty() || controller.getResult().get() != ButtonType.OK) {
      return;
    }
    String name = controller.getName();
    if (name == null || name.isBlank()) {
      return;
    }
    String description = controller.getDescription();

    if (existing == null) {
      Category category = new Category();
      category.setName(name);
      category.setDescription(description == null || description.isEmpty() ? null : description);
      withEnumerationTypeOptions(element, options -> options.getCategories().add(category));
    } else {
      existing.setName(name);
      existing.setDescription(description == null || description.isEmpty() ? null : description);
    }
    rebuildCategoryRows();
    rebuildEnumerationValuesGrid();
    commitChange();
  }

  private void onDeleteCategory(Category category) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this category?", null, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    getCategories().remove(category);
    rebuildCategoryRows();
    rebuildEnumerationValuesGrid();
    commitChange();
  }

  private void moveCategory(int fromIndex, int toIndex) {
    Collections.swap(getCategories(), fromIndex, toIndex);
    rebuildCategoryRows();
    rebuildEnumerationValuesGrid();
    commitChange();
  }

  // ----- Enumeration Values -----

  @FXML
  private void onAddValue() {
    withEnumerationTypeOptions(element, options -> options.getValues().add(new EnumerationValue()));
    rebuildEnumerationValuesGrid();
    commitChange();
  }

  @FXML
  private void onFillEmptyTexts() {
    List<Locale> locales = getModelLocales();
    for (EnumerationValue value : getEnumerationValues()) {
      if (value.getValue() == null || value.getValue().isEmpty()) {
        continue;
      }
      for (Locale locale : locales) {
        if (getLabelText(value, locale.getCode()).isEmpty()) {
          setLabelText(value, locale.getCode(), value.getValue());
        }
      }
    }
    rebuildEnumerationValuesGrid();
    commitChange();
  }

  private void onDeleteValue(EnumerationValue value) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this enumeration value?", null, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    int index = getEnumerationValues().indexOf(value);
    getEnumerationValues().remove(value);
    if (index >= 0) {
      // category values are aligned by index with the enumeration values list, so the removed value's slot
      // has to be removed from every category as well to keep the remaining entries aligned.
      for (Category category : getCategories()) {
        if (index < category.getValues().size()) {
          category.getValues().remove(index);
        }
      }
    }
    rebuildEnumerationValuesGrid();
    commitChange();
  }

  private List<EnumerationValue> getEnumerationValues() {
    EnumerationTypeOptions options = getOptions();
    return options != null ? options.getValues() : List.of();
  }

  private void rebuildEnumerationValuesGrid() {
    enumerationValuesGrid.getChildren().clear();
    enumerationValuesGrid.getColumnConstraints().clear();

    List<EnumerationValue> values = getEnumerationValues();
    List<Category> categories = getCategories();
    List<Locale> locales = getModelLocales();

    boolean empty = values.isEmpty();
    enumerationValuesGrid.setVisible(!empty);
    enumerationValuesGrid.setManaged(!empty);
    enumerationValuesEmptyLabel.setVisible(empty);
    enumerationValuesEmptyLabel.setManaged(empty);
    buttonPanel.setVisible(!empty);
    buttonPanel.setManaged(!empty);

    if (empty) {
      return;
    }

    int column = 0;
    addGridHeader(column, "Value");
    addGrowingColumnConstraint();
    column++;

    for (Locale locale : locales) {
      addGridHeader(column, "Text (" + locale.getCode() + ")");
      addGrowingColumnConstraint();
      column++;
    }

    for (Category category : categories) {
      addGridHeader(column, category.getName() == null ? "" : category.getName());
      addGrowingColumnConstraint();
      column++;
    }

    ColumnConstraints deleteColumnConstraints = new ColumnConstraints();
    deleteColumnConstraints.setPrefWidth(40.0);
    enumerationValuesGrid.getColumnConstraints().add(deleteColumnConstraints);

    for (int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
      EnumerationValue value = values.get(rowIndex);
      int gridRow = rowIndex + 1;
      int currentColumn = 0;
      // captured by the combobox listeners below, since the loop variable itself isn't effectively final
      final int valueIndex = rowIndex;

      TextField valueField = new TextField(value.getValue() == null ? "" : value.getValue());
      valueField.setId("enumvalue-" + rowIndex);
      valueField.setMaxWidth(Double.MAX_VALUE);
      bindTextField(valueField, (element, newValue) -> value.setValue(newValue.isEmpty() ? null : newValue));
      enumerationValuesGrid.add(valueField, currentColumn, gridRow);
      currentColumn++;

      for (Locale locale : locales) {
        TextField textField = new TextField(getLabelText(value, locale.getCode()));
        textField.setId("enumtext-" + rowIndex + "-" + locale.getCode());
        textField.setMaxWidth(Double.MAX_VALUE);
        bindTextField(textField, (element, newValue) -> setLabelText(value, locale.getCode(), newValue));
        enumerationValuesGrid.add(textField, currentColumn, gridRow);
        currentColumn++;
      }

      for (Category category : categories) {
        ComboBox<String> categoryValueCombo = new ComboBox<>();
        categoryValueCombo.setId("enumcategory-" + rowIndex + "-" + currentColumn);
        categoryValueCombo.setEditable(true);
        categoryValueCombo.setMaxWidth(Double.MAX_VALUE);
        setComboBoxItems(categoryValueCombo, getDistinctCategoryValues(category));
        setFieldValue(categoryValueCombo, getCategoryValue(category, valueIndex));
        bindComboBox(categoryValueCombo, (element, newValue) -> setCategoryValue(category, valueIndex, newValue));
        enumerationValuesGrid.add(categoryValueCombo, currentColumn, gridRow);
        currentColumn++;
      }

      Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> onDeleteValue(value));
      GridPane.setHalignment(deleteButton, HPos.CENTER);
      enumerationValuesGrid.add(deleteButton, currentColumn, gridRow);
    }
  }

  private void addGridHeader(int column, String text) {
    javafx.scene.control.Label header = new javafx.scene.control.Label(text);
    header.getStyleClass().add("field-label");
    enumerationValuesGrid.add(header, column, 0);
  }

  private void addGrowingColumnConstraint() {
    ColumnConstraints constraints = new ColumnConstraints();
    constraints.setHgrow(Priority.ALWAYS);
    enumerationValuesGrid.getColumnConstraints().add(constraints);
  }

  /**
   * A category's {@code values} are aligned by index with the enumeration values list (i.e. entry {@code i}
   * is the category value assigned to the {@code i}-th enumeration value), matching the SME reference
   * implementation. Reads out of bounds (e.g. a value row added after the category) as an empty string.
   */
  private static String getCategoryValue(Category category, int valueIndex) {
    List<String> values = category.getValues();
    return valueIndex < values.size() ? values.get(valueIndex) : "";
  }

  private static void setCategoryValue(Category category, int valueIndex, String newValue) {
    List<String> values = category.getValues();
    while (values.size() <= valueIndex) {
      values.add("");
    }
    values.set(valueIndex, newValue == null ? "" : newValue);
  }

  /**
   * Items offered by a category value combobox: every distinct, non-blank value already entered for that
   * category, matching the SME reference implementation's "Drop-Down Selection ... contains all already
   * inserted enumeration values for this category".
   */
  private static List<String> getDistinctCategoryValues(Category category) {
    return category.getValues().stream()
        .filter(value -> value != null && !value.isEmpty())
        .distinct()
        .sorted()
        .toList();
  }

  private static String getLabelText(EnumerationValue value, String localeCode) {
    return value.getLabel().stream()
        .filter(label -> localeCode.equals(label.getLocale()))
        .findFirst()
        .map(Label::getText)
        .orElse("");
  }

  private static void setLabelText(EnumerationValue value, String localeCode, String text) {
    Optional<Label> existing = value.getLabel().stream()
        .filter(label -> localeCode.equals(label.getLocale()))
        .findFirst();
    if (existing.isPresent()) {
      existing.get().setText(text.isEmpty() ? null : text);
    } else if (!text.isEmpty()) {
      Label label = new Label();
      label.setLocale(localeCode);
      label.setText(text);
      value.getLabel().add(label);
    }
  }

  private List<Locale> getModelLocales() {
    if (projectItem == null || projectItem.getModel() == null) {
      return List.of();
    }
    return projectItem.getModel().getLocales();
  }

  private EnumerationTypeOptions getOptions() {
    return getEnumerationFieldType(element).map(EnumerationFieldType::getEnumerationType).orElse(null);
  }

  private static void withEnumerationTypeOptions(Element element, Consumer<EnumerationTypeOptions> mutator) {
    getEnumerationFieldType(element).ifPresent(enumerationFieldType -> {
      EnumerationTypeOptions options = enumerationFieldType.getEnumerationType();
      if (options == null) {
        options = new EnumerationTypeOptions();
        enumerationFieldType.setEnumerationType(options);
      }
      mutator.accept(options);
    });
  }

  private static Optional<EnumerationFieldType> getEnumerationFieldType(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof EnumerationFieldType enumerationFieldType) {
      return Optional.of(enumerationFieldType);
    }
    return Optional.empty();
  }

  private static VBox createMoveButtonsBox(Runnable moveUp, boolean upDisabled, Runnable moveDown, boolean downDisabled) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", moveUp);
    moveUpButton.setDisable(upDisabled);
    moveUpButton.getStyleClass().addAll("move-button", "move-button-top");

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", moveDown);
    moveDownButton.setDisable(downDisabled);
    moveDownButton.getStyleClass().addAll("move-button", "move-button-bottom");

    return new VBox(1, moveUpButton, moveDownButton);
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
