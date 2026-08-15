package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Alignment;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnAlignment;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions;
import de.a12.studio.ui.editors.overviewmodel.StylesPanelController;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.RichtextEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Add/edit dialog for a single {@link Column}, opened from {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController} by clicking a column row or its Add
 * button. Edits the real {@link Column} live (there's no "add" mode of its own - {@code
 * Dialogs#showColumnForAdd} constructs a new, unattached column before opening this dialog, and the caller
 * only adds it to {@code content.columns} once {@link #isConfirmed()} is true), so a {@link ColumnSnapshot}
 * taken before showing the dialog can undo those edits on Cancel, mirroring {@link
 * de.a12.studio.ui.editors.applicationmodel.dialogs.CaseDialogController}.
 */
public class OverviewColumnDialogController implements DialogController {

  private static final String TYPE_REFERENCE = "reference";
  private static final String TYPE_EXPRESSION = "expression";

  private static final List<String> HORIZONTAL_ALIGNMENTS = Arrays.asList(null, "left", "center", "right");
  private static final List<String> VERTICAL_ALIGNMENTS = Arrays.asList(null, "top", "middle", "bottom");

  @FXML
  private ComboBox<String> columnTypeCombo;
  @FXML
  private TextField targetDocumentModelField;

  @FXML
  private VBox referenceSection;
  @FXML
  private ComboBox<String> elementRefCombo;
  @FXML
  private CheckBox sortableField;
  @FXML
  private VBox preferredSortingBox;
  @FXML
  private ComboBox<String> preferredSortingCombo;

  @FXML
  private VBox expressionSection;
  @FXML
  private TextField nameField;
  @FXML
  private RichtextEditorController expressionPanelController;

  @FXML
  private TextField idField;
  @FXML
  private IconPanelController iconPanelController;

  @FXML
  private ComboBox<String> pinDirectionCombo;
  @FXML
  private TextField widthField;
  @FXML
  private VBox fixedWidthBox;
  @FXML
  private CheckBox fixedWidthField;

  @FXML
  private LocalizedTextPanelController labelController;
  @FXML
  private CheckBox hideLabelField;

  @FXML
  private ComboBox<String> horizontalHeaderCombo;
  @FXML
  private ComboBox<String> horizontalContentCombo;
  @FXML
  private ComboBox<String> verticalHeaderCombo;
  @FXML
  private ComboBox<String> verticalContentCombo;

  @FXML
  private StylesPanelController stylesHeaderController;
  @FXML
  private StylesPanelController stylesContentController;

  @FXML
  private Button okButton;
  @FXML
  private Button cancelButton;

  // Shared by the embedded label/styles panels so their commits aren't persisted while the dialog is open: this
  // dialog persists everything itself, in one go, once OK is pressed.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private Column column;

  private ColumnSnapshot snapshot;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Set while fields are being repopulated from the model (initial load, or a field whose visibility toggle
  // shouldn't itself count as an edit), so those programmatic updates aren't mistaken for user edits.
  private boolean updatingFromModel;

  @FXML
  private void initialize() {
    labelController.configureColumnLabel();
    labelController.setSaveMode(saveMode);
    stylesHeaderController.configureColumnHeaderStyles();
    stylesHeaderController.setSaveMode(saveMode);
    stylesContentController.configureColumnContentStyles();
    stylesContentController.setSaveMode(saveMode);
    iconPanelController.setSaveMode(saveMode);
    expressionPanelController.setSaveMode(saveMode);

    columnTypeCombo.setItems(FXCollections.observableArrayList(TYPE_REFERENCE, TYPE_EXPRESSION));
    columnTypeCombo.setConverter(displayConverter(OverviewColumnDialogController::capitalize));
    columnTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      updateTypeVisibility();
      validate();
    });

    preferredSortingCombo.setItems(FXCollections.observableArrayList(Column.PREFERRED_SORTING_ASC, Column.PREFERRED_SORTING_DESC));
    preferredSortingCombo.setConverter(displayConverter(value -> Column.PREFERRED_SORTING_ASC.equals(value) ? "Ascending" : "Descending"));
    preferredSortingCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        column.setPreferredSorting(newValue);
      }
    });

    sortableField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      updateSortableVisibility();
      if (updatingFromModel) {
        return;
      }
      column.setSortable(newValue);
      if (newValue && column.getPreferredSorting() == null) {
        column.setPreferredSorting(Column.PREFERRED_SORTING_ASC);
        setComboValue(preferredSortingCombo, Column.PREFERRED_SORTING_ASC);
      }
    });

    pinDirectionCombo.setItems(FXCollections.observableArrayList(null, Column.PIN_DIRECTION_LEFT, Column.PIN_DIRECTION_RIGHT));
    pinDirectionCombo.setConverter(displayConverter(value -> value == null ? "(None)" : capitalize(value.toLowerCase())));
    pinDirectionCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      updateFixedWidthVisibility();
      if (!updatingFromModel) {
        column.setPinDirection(newValue);
      }
    });

    configureAlignmentCombo(horizontalHeaderCombo, HORIZONTAL_ALIGNMENTS);
    configureAlignmentCombo(horizontalContentCombo, HORIZONTAL_ALIGNMENTS);
    configureAlignmentCombo(verticalHeaderCombo, VERTICAL_ALIGNMENTS);
    configureAlignmentCombo(verticalContentCombo, VERTICAL_ALIGNMENTS);
    horizontalHeaderCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureHeaderAlignment().setHorizontal(newValue);
      }
    });
    horizontalContentCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureContentAlignment().setHorizontal(newValue);
      }
    });
    verticalHeaderCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureHeaderAlignment().setVertical(newValue);
      }
    });
    verticalContentCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        ensureContentAlignment().setVertical(newValue);
      }
    });

    WidgetFactory.restrictToDecimalInput(widthField);

    elementRefCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        column.setElementRef(newValue);
      }
      validate();
    });
    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        column.setName(blankToNull(newValue));
      }
    });
    widthField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        column.setWidth(parseWidth(newValue));
      }
      validate();
    });
    fixedWidthField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        column.setFixedWidth(newValue ? Boolean.TRUE : null);
      }
    });
    hideLabelField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        column.setLabelHidden(newValue ? Boolean.TRUE : null);
      }
    });
  }

  void init(Stage stage, ElementIndex documentModelIndex, String documentModelId, @NonNull Column column) {
    this.stage = stage;
    this.column = column;
    this.snapshot = new ColumnSnapshot(column);

    elementRefCombo.getItems().setAll(OverviewElementOptions.elementIds(documentModelIndex));
    OverviewElementOptions.applyElementRefConverter(elementRefCombo, documentModelIndex);

    updatingFromModel = true;
    try {
      targetDocumentModelField.setText(documentModelId);
      idField.setText(column.getId());

      columnTypeCombo.setValue(isExpressionType(column) ? TYPE_EXPRESSION : TYPE_REFERENCE);
      elementRefCombo.setValue(column.getElementRef());
      sortableField.setSelected(Boolean.TRUE.equals(column.getSortable()));
      preferredSortingCombo.setValue(column.getPreferredSorting());

      nameField.setText(column.getName());

      pinDirectionCombo.setValue(column.getPinDirection());
      widthField.setText(column.getWidth() != null ? String.valueOf(column.getWidth()) : "");
      fixedWidthField.setSelected(Boolean.TRUE.equals(column.getFixedWidth()));

      hideLabelField.setSelected(Boolean.TRUE.equals(column.getLabelHidden()));

      Alignment header = column.getAlignment() != null ? column.getAlignment().getHeader() : null;
      Alignment content = column.getAlignment() != null ? column.getAlignment().getContent() : null;
      horizontalHeaderCombo.setValue(header != null ? header.getHorizontal() : null);
      verticalHeaderCombo.setValue(header != null ? header.getVertical() : null);
      horizontalContentCombo.setValue(content != null ? content.getHorizontal() : null);
      verticalContentCombo.setValue(content != null ? content.getVertical() : null);
    }
    finally {
      updatingFromModel = false;
    }

    updateTypeVisibility();
    updateFixedWidthVisibility();

    labelController.setColumn(column);
    stylesHeaderController.setColumn(column);
    stylesContentController.setColumn(column);
    iconPanelController.setColumn(column);
    expressionPanelController.setCustom(column::getExpression, column::setExpression);

    validate();
  }

  /** Unregisters the embedded panels once this dialog is closed, regardless of how (OK, Cancel or the window's
   * own close button) - see {@link Dialogs#showColumn}, which calls this from the stage's {@code onHidden}
   * handler. */
  void destroy() {
    labelController.destroy();
    stylesHeaderController.destroy();
    stylesContentController.destroy();
    iconPanelController.destroy();
    expressionPanelController.destroy();
  }

  @Override
  public void onDialogCancel() {
    snapshot.restore();
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      projectItem.save();
      StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    }
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  private void updateTypeVisibility() {
    boolean expression = TYPE_EXPRESSION.equals(columnTypeCombo.getValue());
    referenceSection.setVisible(!expression);
    referenceSection.setManaged(!expression);
    expressionSection.setVisible(expression);
    expressionSection.setManaged(expression);
    updateSortableVisibility();
  }

  private void updateSortableVisibility() {
    boolean visible = !TYPE_EXPRESSION.equals(columnTypeCombo.getValue()) && sortableField.isSelected();
    preferredSortingBox.setVisible(visible);
    preferredSortingBox.setManaged(visible);
  }

  private void updateFixedWidthVisibility() {
    String pinDirection = pinDirectionCombo.getValue();
    boolean visible = !Column.PIN_DIRECTION_LEFT.equals(pinDirection) && !Column.PIN_DIRECTION_RIGHT.equals(pinDirection);
    fixedWidthBox.setVisible(visible);
    fixedWidthBox.setManaged(visible);
  }

  private void validate() {
    boolean expression = TYPE_EXPRESSION.equals(columnTypeCombo.getValue());
    boolean elementRefOk = expression || elementRefCombo.getValue() != null;
    boolean widthOk = parseWidth(widthField.getText()) != null;
    okButton.setDisable(!elementRefOk || !widthOk);
  }

  private ColumnAlignment ensureAlignment() {
    if (column.getAlignment() == null) {
      column.setAlignment(new ColumnAlignment());
    }
    return column.getAlignment();
  }

  private Alignment ensureHeaderAlignment() {
    ColumnAlignment alignment = ensureAlignment();
    if (alignment.getHeader() == null) {
      alignment.setHeader(new Alignment());
    }
    return alignment.getHeader();
  }

  private Alignment ensureContentAlignment() {
    ColumnAlignment alignment = ensureAlignment();
    if (alignment.getContent() == null) {
      alignment.setContent(new Alignment());
    }
    return alignment.getContent();
  }

  private void setComboValue(ComboBox<String> comboBox, String value) {
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      comboBox.setValue(value);
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  private static boolean isExpressionType(Column column) {
    return (column.getExpression() != null && !column.getExpression().isBlank())
        || (column.getName() != null && !column.getName().isBlank());
  }

  private static void configureAlignmentCombo(ComboBox<String> comboBox, List<String> values) {
    comboBox.setItems(FXCollections.observableArrayList(values));
    comboBox.setConverter(displayConverter(value -> value == null ? "Automatic" : capitalize(value)));
  }

  private static StringConverter<String> displayConverter(Function<String, String> display) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return display.apply(value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

  private static String capitalize(String value) {
    return value == null || value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private static Double parseWidth(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      return Double.valueOf(text);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }
}
