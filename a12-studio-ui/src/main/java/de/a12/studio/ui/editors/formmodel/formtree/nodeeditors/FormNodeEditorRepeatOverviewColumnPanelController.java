package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.ExpressionRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
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
 * expression variant's own expression text, and embeds one panel each for the pin direction ({@link
 * RepeatColumnPinDirectionPanelController}), the header icon ({@link RepeatColumnIconPanelController}) and the
 * per-column hide condition ({@link HideConditionPanelController}). Deliberately does not (yet) expose
 * {@code filterExposition}/{@code labelHidden}/{@code headerStyle}/{@code fixedWidth}/{@code
 * specificHorizontalAlignment}/{@code specificVerticalAlignment}/{@code datePickerConfig}/{@code annotations};
 * the underlying data model already has all of these fields, so a future pass can add panels for them
 * without another data-model change.
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
  private RepeatColumnPinDirectionPanelController pinDirectionController;
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

    bindTextField(widthField, (el, value) -> column.setWidth(parseIntOrNull(value)));
    bindCheckBox(sortableCheckBox, (el, value) -> column.setSortable(value ? Boolean.TRUE : null));
    bindCheckBox(filterableCheckBox, (el, value) -> column.setFilterable(value ? Boolean.TRUE : null));
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
    pinDirectionController.setColumn(column);
    iconController.setColumn(column);
    hideConditionController.configure(column.getId(), column::getHideCondition, column::setHideCondition,
        elementIndex, hideConditionScope);
    labelController.setCustom(column::getLabel, column::setLabel);
    labelController.setFieldSuggestionSource(elementIndex);
    setFieldValue(widthField, column.getWidth() == null ? "" : column.getWidth().toString());
    setFieldValue(sortableCheckBox, Boolean.TRUE.equals(column.getSortable()));
    setFieldValue(filterableCheckBox, Boolean.TRUE.equals(column.getFilterable()));
    setFieldValue(preferredSortingCombo, column.getPreferredSorting());

    boolean fieldBased = column instanceof FieldBasedRepeatOverviewColumn;
    fieldBasedSection.setVisible(fieldBased);
    fieldBasedSection.setManaged(fieldBased);
    expressionSection.setVisible(!fieldBased);
    expressionSection.setManaged(!fieldBased);

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

  private static Integer parseIntOrNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Integer.parseInt(value.strip());
    }
    catch (NumberFormatException e) {
      return null;
    }
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
