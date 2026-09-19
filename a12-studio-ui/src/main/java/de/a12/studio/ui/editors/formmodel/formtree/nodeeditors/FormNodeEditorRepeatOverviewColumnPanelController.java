package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.ExpressionRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.form.DatePickerSupport;
import de.a12.studio.modelsvalidation.validators.form.FormColumnWidthValidator;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.editors.propertyeditors.RuleEditorController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Editor for a selected {@link RepeatOverviewColumn} node (a {@link FieldBasedRepeatOverviewColumn} or
 * {@link ExpressionRepeatOverviewColumn} inside a Repeat's overview table). Previously these could only be
 * added via the tree/drag-drop with no editor pane at all - existing columns rendered read-only and new ones
 * couldn't be authored through the UI.
 * <p>
 * Covers the fields shared by both column types (label, width, sortable, filterable, preferred sorting, and
 * - the field-based variant only - the read-only bound element, readonly and message position) plus the
 * expression variant's own expression text, and embeds one panel each for the display options - hide label,
 * fixed width, filter exposition ({@link RepeatColumnDisplayPanelController}) - the pin direction ({@link
 * RepeatColumnPinDirectionPanelController}), the header icon ({@link RepeatColumnIconPanelController}), the
 * header/content alignment overrides ({@link RepeatColumnAlignmentPanelController}), the per-column hide
 * condition ({@link HideConditionPanelController}), the header styles ({@link
 * RepeatColumnHeaderStylesPanelController}), the annotations and - for a field-based column bound to a date
 * field - the date picker's year range ({@link DatePickerConfigPanelController}).
 * <p>
 * The width is a number of at least {@value FormColumnWidthValidator#MIN_WIDTH} with one decimal place at most
 * (1.0 is about 150px); anything else is reported in the panel's error container and not written.
 */
public class FormNodeEditorRepeatOverviewColumnPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private LocalizedTextTypePanelController labelController;
  @FXML
  private TextField widthField;
  @FXML
  private CheckBox sortableCheckBox;
  @FXML
  private CheckBox filterableCheckBox;
  @FXML
  private ComboBox<String> preferredSortingCombo;
  @FXML
  private Node fieldBasedSection;
  @FXML
  private Label elementRefValueLabel;
  @FXML
  private CheckBox readonlyCheckBox;
  @FXML
  private ComboBox<String> messageExpositionCombo;
  @FXML
  private Node expressionSection;
  @FXML
  private TextField expressionNameField;
  @FXML
  private RuleEditorController expressionController;
  @FXML
  private RepeatColumnDisplayPanelController displayController;
  @FXML
  private DatePickerConfigPanelController datePickerController;
  @FXML
  private RepeatColumnPinDirectionPanelController pinDirectionController;
  @FXML
  private RepeatColumnAlignmentPanelController alignmentController;
  @FXML
  private RepeatColumnHeaderStylesPanelController headerStylesController;
  @FXML
  private AnnotationsPanelController annotationsController;
  @FXML
  private RepeatColumnIconPanelController iconController;
  @FXML
  private HideConditionPanelController hideConditionController;

  private RepeatOverviewColumn column;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    labelController.configureCustom("repeatOverviewColumnLabel", StudioBundle.get("label"));
    preferredSortingCombo.getItems().setAll(null, "ASC", "DESC");
    messageExpositionCombo.getItems().setAll(null, "TOOLTIP");

    bindTextField(widthField, (el, value) -> editWidth(value));
    bindCheckBox(sortableCheckBox, (el, value) -> column.setSortable(value ? Boolean.TRUE : null));
    bindCheckBox(filterableCheckBox, (el, value) -> {
      column.setFilterable(value ? Boolean.TRUE : null);
      displayController.refreshFilterable();
    });
    bindComboBox(preferredSortingCombo, (el, value) -> column.setPreferredSorting(value));
    bindCheckBox(readonlyCheckBox, (el, value) -> asFieldBased().setReadonly(value ? Boolean.TRUE : null));
    bindComboBox(messageExpositionCombo, (el, value) -> asFieldBased().setMessageExposition(value));
    bindTextField(expressionNameField, (el, value) -> asExpressionBased().setName(value));
    expressionController.configureCustom("repeatColumnExpression", StudioBundle.get("expression_cell_expression"));
  }

  /**
   * @param hideConditionScope where the per-column hide condition looks for master fields - the column's own
   *                           field for a field-based column, the closest enclosing repeat's group (or the root)
   *                           for an expression column, mirroring SME's {@code resolveDmElementForFmElement}
   */
  public void setColumn(@NonNull RepeatOverviewColumn column, @Nullable ElementIndex elementIndex,
      HideConditionPanelController.@NonNull MasterFieldScope hideConditionScope) {
    this.column = column;
    displayController.setColumn(column);
    pinDirectionController.setColumn(column);
    alignmentController.setColumn(column);
    headerStylesController.setColumn(column);
    annotationsController.setCustom(column::getAnnotations);
    iconController.setColumn(column);
    hideConditionController.configure(column.getId(), column::getHideCondition, column::setHideCondition,
        elementIndex, hideConditionScope);
    labelController.setCustom(column::getLabel, column::setLabel);
    labelController.setFieldSuggestionSource(elementIndex);
    hideError();
    setFieldValue(widthField, formatWidth(column.getWidth()));
    setFieldValue(sortableCheckBox, Boolean.TRUE.equals(column.getSortable()));
    setFieldValue(filterableCheckBox, Boolean.TRUE.equals(column.getFilterable()));
    setFieldValue(preferredSortingCombo, column.getPreferredSorting());

    boolean fieldBased = column instanceof FieldBasedRepeatOverviewColumn;
    fieldBasedSection.setVisible(fieldBased);
    fieldBasedSection.setManaged(fieldBased);
    expressionSection.setVisible(!fieldBased);
    expressionSection.setManaged(!fieldBased);

    boolean datePicker = fieldBased
        && DatePickerSupport.isSupportedElement(elementIndex, ((FieldBasedRepeatOverviewColumn) column).getElementRef());
    datePickerController.setVisible(datePicker);
    if (datePicker) {
      FieldBasedRepeatOverviewColumn dateColumn = (FieldBasedRepeatOverviewColumn) column;
      datePickerController.setConfig(dateColumn::getDatePickerConfig, dateColumn::setDatePickerConfig);
    }

    if (fieldBased) {
      FieldBasedRepeatOverviewColumn fieldColumn = (FieldBasedRepeatOverviewColumn) column;
      elementRefValueLabel.setText(displayName(fieldColumn.getElementRef(), elementIndex));
      setFieldValue(readonlyCheckBox, Boolean.TRUE.equals(fieldColumn.getReadonly()));
      setFieldValue(messageExpositionCombo, fieldColumn.getMessageExposition());
    }
    else {
      ExpressionRepeatOverviewColumn expressionColumn = (ExpressionRepeatOverviewColumn) column;
      setFieldValue(expressionNameField, expressionColumn.getName());
      expressionController.setCustom(expressionColumn::getExpression, expressionColumn::setExpression);
    }
  }

  private FieldBasedRepeatOverviewColumn asFieldBased() {
    return (FieldBasedRepeatOverviewColumn) column;
  }

  private ExpressionRepeatOverviewColumn asExpressionBased() {
    return (ExpressionRepeatOverviewColumn) column;
  }

  /** Writes the typed width; a blank clears it (the default is 1.0), an invalid one is reported and not written. */
  private void editWidth(String text) {
    String trimmed = text == null ? "" : text.strip().replace(',', '.');
    if (trimmed.isEmpty()) {
      column.setWidth(null);
      hideError();
      return;
    }
    Double width = null;
    try {
      width = Double.valueOf(trimmed);
    }
    catch (NumberFormatException e) {
      // reported below
    }
    if (width == null || !FormColumnWidthValidator.isValid(width)) {
      showError("ERROR", StudioBundle.get("repeat_column_width_invalid", FormColumnWidthValidator.MIN_WIDTH));
      return;
    }
    column.setWidth(width);
    hideError();
  }

  // 1 and 0.8, not 1.0 and 0.8000000000000000444: the shortest exact form of the stored number.
  private static String formatWidth(Double width) {
    return width == null ? "" : BigDecimal.valueOf(width).stripTrailingZeros().toPlainString();
  }

  private static String displayName(String elementRef, ElementIndex elementIndex) {
    if (elementRef == null || elementRef.isBlank()) {
      return "";
    }
    if (elementIndex == null) {
      return elementRef;
    }
    String path = elementIndex.resolveDisplayPath(elementRef);
    return path != null ? path : elementRef;
  }
}
