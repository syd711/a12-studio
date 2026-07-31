package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentUniquenessCriterion;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.RequirednessConfig;
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
 * Modal dialog for creating/editing a single {@link DocumentUniquenessCriterion}: its {@link
 * DocumentUniquenessCriterion#getName() Name}, the {@link DocumentUniquenessCriterion#getFields() Fields} it
 * covers (a checklist of every {@link FieldElement} in {@code model} eligible per the a12 platform's Document
 * Uniqueness Criteria rules, see {@link #isEligibleField}), and a per-locale {@link
 * DocumentUniquenessCriterion#getErrorMessage() Error Message}. Edits a private working copy throughout, only
 * exposed via {@link #getResult()} once OK is pressed - the caller (see {@link
 * de.a12.studio.ui.editors.propertyeditors.DocumentUniquenessCriteriaPanelController}) decides whether/how to
 * apply it to the real list.
 */
public class DocumentUniquenessCriterionDialogController implements DialogController {

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

  public void initDialog(Stage stage, @NonNull DocumentModel model, DocumentUniquenessCriterion criterion, @NonNull Set<String> usedNames) {
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

    Set<String> preselectedIds = criterion != null ? new HashSet<>(criterion.getFields()) : Set.of();
    buildFieldCheckboxes(model, preselectedIds);
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

  public Optional<DocumentUniquenessCriterion> getResult() {
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return Optional.empty();
    }
    String name = nameField.getText();
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }

    DocumentUniquenessCriterion criterion = new DocumentUniquenessCriterion();
    criterion.setName(name.trim());
    List<String> fields = new ArrayList<>();
    for (CheckBox checkBox : fieldCheckBoxes) {
      if (checkBox.isSelected()) {
        fields.add((String) checkBox.getUserData());
      }
    }
    criterion.setFields(fields);
    criterion.setErrorMessage(new ArrayList<>(errorMessages));
    return Optional.of(criterion);
  }

  /**
   * A checkbox per eligible {@link FieldElement} in {@code model}, labeled with its full path (see {@link
   * ElementIndex#getPath}) for disambiguation. Fields already referenced by {@code preselectedIds} are kept in
   * the list (and checked) even if they no longer pass {@link #isEligibleField} - e.g. a Field that has since
   * become repeatable - so re-opening this dialog for an existing criterion never silently drops data; such a
   * row is labeled accordingly instead.
   */
  private void buildFieldCheckboxes(DocumentModel model, Set<String> preselectedIds) {
    fieldsBox.getChildren().clear();
    fieldCheckBoxes.clear();

    ElementIndex index = new ElementIndex(model);
    Set<String> eligibleIds = new HashSet<>();
    List<Element> candidates = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (isEligibleField(index, element)) {
        candidates.add(element);
        eligibleIds.add(element.getId());
      }
    }
    for (String id : preselectedIds) {
      if (!eligibleIds.contains(id)) {
        index.allElements().stream()
            .filter(candidate -> id.equals(candidate.getId()))
            .findFirst()
            .ifPresent(candidates::add);
      }
    }
    candidates.sort(Comparator.comparing(index::getPath));

    boolean empty = candidates.isEmpty();
    fieldsBox.setVisible(!empty);
    fieldsBox.setManaged(!empty);
    noFieldsLabel.setVisible(empty);
    noFieldsLabel.setManaged(empty);

    for (Element element : candidates) {
      boolean eligible = eligibleIds.contains(element.getId());
      CheckBox checkBox = new CheckBox(eligible ? index.getPath(element) : index.getPath(element) + " (no longer eligible)");
      checkBox.setId("uniquenessCriterionField-" + element.getId());
      checkBox.setUserData(element.getId());
      checkBox.setSelected(preselectedIds.contains(element.getId()));
      checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOkState());
      fieldCheckBoxes.add(checkBox);
      fieldsBox.getChildren().add(checkBox);
    }
  }

  /**
   * Mirrors the a12 platform's Document Uniqueness Criteria field prerequisites: a Field must be Required
   * (with "Only if Parent Group Filled" set to false, i.e. {@link RequirednessConfig#MODE_REQUIRED}), not
   * inside a repeatable Group, and not transient.
   */
  private static boolean isEligibleField(ElementIndex index, Element element) {
    if (!(element instanceof FieldElement fieldElement) || fieldElement.getField() == null) {
      return false;
    }
    FieldConfig field = fieldElement.getField();
    RequirednessConfig requirednessConfig = field.getRequirednessConfig();
    if (requirednessConfig == null || !RequirednessConfig.MODE_REQUIRED.equals(requirednessConfig.getMode())) {
      return false;
    }
    if (Boolean.TRUE.equals(field.getTransientField())) {
      return false;
    }
    return !isInRepeatableGroup(index, element);
  }

  /** True when any ancestor group (not the root group itself) has a repeatability above 1. */
  private static boolean isInRepeatableGroup(ElementIndex index, Element element) {
    GroupElement parent = index.parentOf(element);
    while (parent != null) {
      if (parent.getGroup() != null && parent.getGroup().getRepeatability() != null
          && parent.getGroup().getRepeatability() > 1 && index.parentOf(parent) != null) {
        return true;
      }
      parent = index.parentOf(parent);
    }
    return false;
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
      textField.setId("uniquenessCriterionErrorMessage-" + locale.getCode());
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
