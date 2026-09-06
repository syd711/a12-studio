package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Additional Settings" property editor for a selected {@link AbstractRepeat} node: toggles and a
 * combo for the behavioural flags shared by all repeat variants:
 * <ul>
 *   <li>{@link AbstractRepeat#getReadonly()} — check box</li>
 *   <li>{@link AbstractRepeat#getEnableAdd()} — check box</li>
 *   <li>{@link AbstractRepeat#getEnableRemove()} — check box</li>
 *   <li>{@link AbstractRepeat#getEnableReorder()} — check box</li>
 *   <li>{@link AbstractRepeat#getEnableCopy()} — check box</li>
 *   <li>{@link AbstractRepeat#getEnableColumnsResize()} — check box</li>
 *   <li>{@link AbstractRepeat#getInfiniteScrolling()} — check box</li>
 *   <li>{@link AbstractRepeat#getReadonlyPresentation()} — combo ("TEXT" or "INPUT")</li>
 * </ul>
 */
public class RepeatAdditionalSettingsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> READONLY_PRESENTATION_VALUES = List.of("TEXT", "INPUT");

  @FXML private CheckBox readonlyCheckBox;
  @FXML private CheckBox enableAddCheckBox;
  @FXML private CheckBox enableRemoveCheckBox;
  @FXML private CheckBox enableReorderCheckBox;
  @FXML private CheckBox enableCopyCheckBox;
  @FXML private CheckBox enableColumnsResizeCheckBox;
  @FXML private CheckBox infiniteScrollingCheckBox;
  @FXML private ComboBox<String> readonlyPresentationCombo;
  @FXML private CheckBox titleHiddenCheckBox;
  @FXML private TextField filterExpressionField;
  @FXML private TextField initialSortingField;

  private AbstractRepeat repeat;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    readonlyPresentationCombo.getItems().setAll(READONLY_PRESENTATION_VALUES);

    bindCheckBox(readonlyCheckBox, (el, val) -> repeat.setReadonly(val ? Boolean.TRUE : null));
    bindCheckBox(enableAddCheckBox, (el, val) -> repeat.setEnableAdd(val ? Boolean.TRUE : null));
    bindCheckBox(enableRemoveCheckBox, (el, val) -> repeat.setEnableRemove(val ? Boolean.TRUE : null));
    bindCheckBox(enableReorderCheckBox, (el, val) -> repeat.setEnableReorder(val ? Boolean.TRUE : null));
    bindCheckBox(enableCopyCheckBox, (el, val) -> repeat.setEnableCopy(val ? Boolean.TRUE : null));
    bindCheckBox(enableColumnsResizeCheckBox, (el, val) -> repeat.setEnableColumnsResize(val ? Boolean.TRUE : null));
    bindCheckBox(infiniteScrollingCheckBox, (el, val) -> repeat.setInfiniteScrolling(val ? Boolean.TRUE : null));
    bindComboBox(readonlyPresentationCombo, (el, val) -> repeat.setReadonlyPresentation(val == null || val.isBlank() ? null : val));
    bindCheckBox(titleHiddenCheckBox, (el, val) -> repeat.setTitleHidden(val ? Boolean.TRUE : null));
    bindTextField(filterExpressionField, (el, val) -> repeat.setFilterExpression(val == null || val.isBlank() ? null : val));
    bindTextField(initialSortingField, (el, val) -> repeat.setInitialSorting(val == null || val.isBlank() ? null : val));
  }

  public void setRepeat(@NonNull AbstractRepeat repeat) {
    this.repeat = repeat;
    setFieldValue(readonlyCheckBox, Boolean.TRUE.equals(repeat.getReadonly()));
    setFieldValue(enableAddCheckBox, Boolean.TRUE.equals(repeat.getEnableAdd()));
    setFieldValue(enableRemoveCheckBox, Boolean.TRUE.equals(repeat.getEnableRemove()));
    setFieldValue(enableReorderCheckBox, Boolean.TRUE.equals(repeat.getEnableReorder()));
    setFieldValue(enableCopyCheckBox, Boolean.TRUE.equals(repeat.getEnableCopy()));
    setFieldValue(enableColumnsResizeCheckBox, Boolean.TRUE.equals(repeat.getEnableColumnsResize()));
    setFieldValue(infiniteScrollingCheckBox, Boolean.TRUE.equals(repeat.getInfiniteScrolling()));
    setFieldValue(readonlyPresentationCombo, repeat.getReadonlyPresentation());
    setFieldValue(titleHiddenCheckBox, Boolean.TRUE.equals(repeat.getTitleHidden()));
    setFieldValue(filterExpressionField, repeat.getFilterExpression());
    setFieldValue(initialSortingField, repeat.getInitialSorting());
  }
}
