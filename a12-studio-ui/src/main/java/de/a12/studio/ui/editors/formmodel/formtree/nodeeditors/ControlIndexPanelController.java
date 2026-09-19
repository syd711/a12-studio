package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlIndex;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.form.ControlIndexSupport;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * "Control Index" property editor of a {@link de.a12.studio.models.formmodel.Control} placed outside the repeat
 * of its repeatable group ({@link ControlIndexSupport#isIndexable}): which repetition of that group the Control shows, given as a type
 * (numeric: the number of the row, semantic: the value of the group's index field) and a value. Like SME
 * the value can only be entered once a type is chosen and is emptied when the type changes; unlike SME the index
 * can be removed again (with the clear button) instead of staying set once chosen. SME applies no checks to the
 * value, neither does this panel.
 * <p>
 * The {@link ControlIndex} is created on the first edit and dropped again when the type is cleared, so a Control
 * without index never carries an empty {@code index} object. Not tied to a document-model {@code Element}, so
 * it follows the model-header pattern (getter/setter pair instead of a typed owner).
 */
public class ControlIndexPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> TYPES = List.of(ControlIndex.TYPE_SEMANTIC, ControlIndex.TYPE_NUMERIC);

  @FXML
  private Label infoIcon;
  @FXML
  private ComboBox<String> typeField;
  @FXML
  private TextField valueField;
  @FXML
  private Button clearButton;

  private Supplier<ControlIndex> getter;
  private Consumer<ControlIndex> setter;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(infoIcon, StudioBundle.get("control_index_tooltip"));
    typeField.getItems().setAll(TYPES);
    typeField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String type) {
        return type == null ? "" : typeLabel(type);
      }

      @Override
      public String fromString(String text) {
        return null;
      }
    });
    clearButton.disableProperty().bind(typeField.valueProperty().isNull());

    bindComboBox(typeField, (el, type) -> editType(type));
    bindTextField(valueField, (el, value) -> editValue(value));
  }

  @FXML
  private void onClear(ActionEvent event) {
    typeField.setValue(null);
  }

  /**
   * Binds the panel to {@code control}, and shows it only when the Control needs an index ({@link
   * ControlIndexSupport#isIndexable}) - or already has one, so a stale index can still be seen and removed.
   */
  public void setControl(@NonNull Control control, @Nullable FormModelContent content, @Nullable ElementIndex elementIndex) {
    boolean visible = control.getIndex() != null || ControlIndexSupport.isIndexable(control, content, elementIndex);
    setEditorVisible(visible);
    if (visible) {
      setIndex(control::getIndex, control::setIndex);
    }
  }

  /** Binds the panel to a Control's index that may not exist yet: {@code getter} may return {@code null}. */
  void setIndex(@NonNull Supplier<ControlIndex> getter, @NonNull Consumer<ControlIndex> setter) {
    this.getter = getter;
    this.setter = setter;
    ControlIndex index = getter.get();
    String type = index == null ? null : index.getType();
    setFieldValue(typeField, type);
    setFieldValue(valueField, index == null || index.getValue() == null ? "" : index.getValue());
    showType(type);
  }

  private void editType(@Nullable String type) {
    if (type == null || type.isBlank()) {
      setter.accept(null);
    }
    else {
      ControlIndex index = getter.get();
      if (index == null) {
        index = new ControlIndex();
        setter.accept(index);
      }
      // A value means something different for each type, so the old one is dropped.
      if (!type.equals(index.getType())) {
        index.setValue(null);
      }
      index.setType(type);
    }
    setFieldValue(valueField, "");
    showType(type);
  }

  private void editValue(@Nullable String value) {
    ControlIndex index = getter.get();
    if (index != null) {
      index.setValue(value == null || value.isBlank() ? null : value);
    }
  }

  private void showType(@Nullable String type) {
    valueField.setDisable(type == null);
    valueField.setPromptText(ControlIndex.TYPE_NUMERIC.equals(type) ? StudioBundle.get("control_index_value_numeric_prompt")
        : ControlIndex.TYPE_SEMANTIC.equals(type) ? StudioBundle.get("control_index_value_semantic_prompt") : "");
  }

  private static String typeLabel(String type) {
    return switch (type) {
      case ControlIndex.TYPE_SEMANTIC -> StudioBundle.get("control_index_type_semantic");
      case ControlIndex.TYPE_NUMERIC -> StudioBundle.get("control_index_type_numeric");
      default -> type;
    };
  }
}
