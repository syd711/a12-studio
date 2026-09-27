package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.ContentUniquenessCriterion;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Modal dialog for creating/editing a single {@link ContentUniquenessCriterion}: its {@link
 * ContentUniquenessCriterion#getName() Name}, the {@link ContentUniquenessCriterion#getFields() Fields} it
 * covers (a checklist of every {@link FieldElement} in {@code model}, labeled/keyed by its full path - see
 * {@link ElementIndex#getPath} - since this criterion addresses Fields by path rather than by {@code Element}
 * id, unlike {@link DocumentUniquenessCriterionDialogController}), and a per-locale {@link
 * ContentUniquenessCriterion#getErrorMessage() Error Message}. Edits a private working copy throughout, only
 * exposed via {@link #getResult()} once OK is pressed.
 */
public class ContentUniquenessCriterionDialogController implements DialogController {

  @FXML
  private TextField nameField;

  @FXML
  private VBox fieldsBox;

  @FXML
  private javafx.scene.control.Label noFieldsLabel;

  @FXML
  private GridPane errorMessagesGrid;

  @FXML
  private Button okButton;

  private Stage stage;

  private Set<String> usedNames = Set.of();

  private final List<CheckBox> fieldCheckBoxes = new ArrayList<>();

  private final List<Label> errorMessages = new ArrayList<>();

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  public void initDialog(Stage stage, @NonNull DocumentModel model, ContentUniquenessCriterion criterion, @NonNull Set<String> usedNames) {
    this.stage = stage;
    this.usedNames = usedNames;

    nameField.setText(criterion != null ? criterion.getName() : "");
    nameField.textProperty().addListener((observable, oldValue, newValue) -> updateOkState());

    errorMessages.clear();
    if (criterion != null) {
      for (Label label : criterion.getErrorMessage()) {
        Label copy = new Label();
        copy.setLocale(label.getLocale());
        copy.setText(label.getText());
        errorMessages.add(copy);
      }
    }

    Set<String> preselectedFullNames = criterion != null
        ? criterion.getFields().stream().map(ContentUniquenessCriterion.Field::getFullName).collect(java.util.stream.Collectors.toSet())
        : Set.of();
    buildFieldCheckboxes(model, preselectedFullNames);
    buildErrorMessageRows(model);

    updateOkState();
    nameField.requestFocus();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  public Optional<ContentUniquenessCriterion> getResult() {
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return Optional.empty();
    }
    String name = nameField.getText();
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }

    ContentUniquenessCriterion criterion = new ContentUniquenessCriterion();
    criterion.setName(name.trim());
    List<ContentUniquenessCriterion.Field> fields = new ArrayList<>();
    for (CheckBox checkBox : fieldCheckBoxes) {
      if (checkBox.isSelected()) {
        ContentUniquenessCriterion.Field field = new ContentUniquenessCriterion.Field();
        field.setFullName((String) checkBox.getUserData());
        fields.add(field);
      }
    }
    criterion.setFields(fields);
    criterion.setErrorMessage(new ArrayList<>(errorMessages));
    return Optional.of(criterion);
  }

  /**
   * A checkbox per {@link FieldElement} in {@code model}, keyed and labeled by its full path (see {@link
   * ElementIndex#getPath}) - the same string {@link ContentUniquenessCriterion.Field#getFullName()} stores. A
   * field already referenced by {@code preselectedFullNames} that no longer resolves (e.g. renamed/deleted
   * since) is kept in the list (checked, labeled accordingly) so re-opening this dialog for an existing
   * criterion never silently drops data.
   */
  private void buildFieldCheckboxes(DocumentModel model, Set<String> preselectedFullNames) {
    fieldsBox.getChildren().clear();
    fieldCheckBoxes.clear();

    ElementIndex index = new ElementIndex(model);
    List<String> paths = new ArrayList<>();
    Set<String> resolvedPaths = new HashSet<>();
    for (Element element : index.allElements()) {
      if (element instanceof FieldElement) {
        String path = index.getPath(element);
        paths.add(path);
        resolvedPaths.add(path);
      }
    }
    for (String fullName : preselectedFullNames) {
      if (!resolvedPaths.contains(fullName)) {
        paths.add(fullName);
      }
    }
    paths.sort(Comparator.naturalOrder());

    boolean empty = paths.isEmpty();
    fieldsBox.setVisible(!empty);
    fieldsBox.setManaged(!empty);
    noFieldsLabel.setVisible(empty);
    noFieldsLabel.setManaged(empty);

    for (String path : paths) {
      boolean resolvable = resolvedPaths.contains(path);
      CheckBox checkBox = new CheckBox(resolvable ? path : path + " (no longer resolvable)");
      checkBox.setId("contentUniquenessCriterionField-" + path);
      checkBox.setUserData(path);
      checkBox.setSelected(preselectedFullNames.contains(path));
      checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOkState());
      fieldCheckBoxes.add(checkBox);
      fieldsBox.getChildren().add(checkBox);
    }
  }

  private void buildErrorMessageRows(DocumentModel model) {
    errorMessagesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    int row = 1;
    for (Locale locale : model.getLocales()) {
      javafx.scene.control.Label localeLabel = new javafx.scene.control.Label(locale.getCode());
      TextField textField = new TextField(findErrorMessageText(locale.getCode()));
      textField.setId("contentUniquenessCriterionErrorMessage-" + locale.getCode());
      textField.setMaxWidth(Double.MAX_VALUE);
      textField.textProperty().addListener((observable, oldValue, newValue) -> setErrorMessageText(locale.getCode(), newValue));
      errorMessagesGrid.addRow(row, localeLabel, textField);
      row++;
    }
  }

  private String findErrorMessageText(String localeCode) {
    return errorMessages.stream()
        .filter(label -> localeCode.equals(label.getLocale()))
        .findFirst()
        .map(Label::getText)
        .orElse("");
  }

  private void setErrorMessageText(String localeCode, String value) {
    Optional<Label> existing = errorMessages.stream().filter(label -> localeCode.equals(label.getLocale())).findFirst();
    if (value == null || value.isBlank()) {
      existing.ifPresent(errorMessages::remove);
      return;
    }
    if (existing.isPresent()) {
      existing.get().setText(value);
    } else {
      Label label = new Label();
      label.setLocale(localeCode);
      label.setText(value);
      errorMessages.add(label);
    }
  }

  private void updateOkState() {
    String name = nameField.getText();
    boolean nameValid = name != null && !name.isBlank() && !usedNames.contains(name.trim());
    boolean hasFieldSelected = fieldCheckBoxes.stream().anyMatch(CheckBox::isSelected);
    okButton.setDisable(!nameValid || !hasFieldSelected);
  }
}
