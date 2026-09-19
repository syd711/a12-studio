package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormStyleReferences;
import de.a12.studio.models.formmodel.Style;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Edits a {@code List<Style>} (each entry just a CSS style class {@code name}, see {@link Style}) - SME's
 * {@code stylable_mixin}/{@code style} field, e.g. {@link de.a12.studio.models.formmodel.ButtonStyling#getStyle()}
 * or {@link de.a12.studio.models.formmodel.Control#getStyle()}. Not tied to a single {@code Element}, so it
 * follows the read/write-supplier pattern used by {@link
 * de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController#setCustom(Supplier, Supplier)}: {@code
 * reader} must tolerate the owner's style list not existing yet (returning an empty list rather than creating
 * one), {@code writer} may lazily create it. First used by {@link
 * de.a12.studio.ui.editors.formmodel.dialogs.FormButtonDialogController} for a {@link
 * de.a12.studio.models.formmodel.Button}'s {@code buttonStyling.style}. Distinct from {@link
 * de.a12.studio.ui.editors.overviewmodel.StylesPanelController}, which edits a plain {@code List<String>}
 * instead (Overview Model's model-level style class list has no per-entry object).
 * <p>
 * Also serves the Form Model's own model-level style list ({@code FormModelContent.styles}, see {@link
 * #configureModelStyles()}), shown in the Model Settings dialog, where each name is typed.
 * <p>
 * Everywhere else in a Form Model the panel edits which of those model-level styles ("style presets") an
 * element uses, like SME's reference picker: each entry is picked from a combo box of the defined styles, and
 * with none defined "Add" is disabled and the empty hint says where to define them. A name that isn't (or is
 * no longer) defined is still shown, so it can be seen and replaced; {@code FormStyleReferenceValidator}
 * reports it. Outside a Form Model (no selected form) the panel falls back to typing the name.
 */
public class StylesPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; unique per panel instance, mirroring EventButtonsPanelController's approach
  // (a plain static DataFormat would collide if this panel is ever embedded twice in the same window).
  private static final DataFormat STYLE_INDEX = new DataFormat("application/x-a12-form-style-index");

  @FXML
  private VBox stylesList;

  @FXML
  private Label stylesEmptyLabel;

  @FXML
  private Button addButton;

  // True for the model-level list itself, whose names are typed rather than picked from that same list.
  private boolean modelLevel;

  private Supplier<List<Style>> reader;

  private Supplier<List<Style>> writer;

  /**
   * Retitles this panel for the Form Model's model-level style list ({@code FormModelContent.styles}) - SME's
   * "styles" section of {@code FormModelFrame-form.json} - and gives it its own persisted expanded state,
   * separate from the per-node instances. Call once after loading from FXML, before {@link #setCustom}.
   */
  public void configureModelStyles() {
    setTitle(StudioBundle.get("model_styles"));
    setSettingsKeySuffix(".model");
    modelLevel = true;
  }

  /** Shows or hides the whole panel, e.g. for the Model Settings dialog's non-Form model types. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setCustom(@NonNull Supplier<List<Style>> reader, @NonNull Supplier<List<Style>> writer) {
    this.reader = reader;
    this.writer = writer;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    writer.get().add(new Style());
    rebuildRows();
    commitChange();
  }

  private void rebuildRows() {
    stylesList.getChildren().clear();

    List<Style> styles = reader.get();
    List<String> presets = presetNames();
    boolean noPresets = presets != null && presets.isEmpty();
    boolean empty = styles.isEmpty();
    stylesEmptyLabel.setText(StudioBundle.get(noPresets ? "no_styles_defined_in_model_settings" : "no_styles_defined"));
    stylesEmptyLabel.setVisible(empty);
    stylesEmptyLabel.setManaged(empty);
    // Nothing to pick from yet: an added entry could not get a valid name.
    addButton.setDisable(noPresets);

    for (int index = 0; index < styles.size(); index++) {
      stylesList.getChildren().add(createRow(styles.get(index), index, styles.size(), presets));
    }
  }

  /**
   * The names of the styles the open Form Model defines, or {@code null} when names are typed instead: this is
   * the model-level list itself, or no Form Model is selected.
   */
  private @Nullable List<String> presetNames() {
    if (modelLevel) {
      return null;
    }
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null || !(projectItem.getModel() instanceof FormModel formModel) || formModel.getContent() == null) {
      return null;
    }
    return new ArrayList<>(FormStyleReferences.definedNames(formModel.getContent()));
  }

  private HBox createRow(Style style, int index, int rowCount, @Nullable List<String> presets) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    javafx.scene.Node nameControl;
    if (presets == null) {
      TextField nameField = new TextField();
      nameField.setId("formStyleName-" + index);
      setFieldValue(nameField, style.getName());
      bindTextField(nameField, (el, value) -> style.setName(value.isEmpty() ? null : value));
      nameControl = nameField;
    }
    else {
      ComboBox<String> nameCombo = new ComboBox<>();
      nameCombo.setId("formStyleName-" + index);
      List<String> items = new ArrayList<>(presets);
      if (style.getName() != null && !style.getName().isBlank() && !items.contains(style.getName())) {
        items.add(style.getName());
      }
      nameCombo.getItems().setAll(items);
      setFieldValue(nameCombo, style.getName());
      bindComboBox(nameCombo, (el, value) -> style.setName(value));
      nameControl = nameCombo;
    }
    ((javafx.scene.layout.Region) nameControl).setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameControl, Priority.ALWAYS);

    HBox row = new HBox(10.0, dragHandle, nameControl, createActionsBox(style, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, STYLE_INDEX, index, this::moveStyleViaDrag);
    return row;
  }

  private void moveStyleViaDrag(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(writer.get(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitChange();
    }
  }

  private HBox createActionsBox(Style style, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button copyButton = RowFactory.createActionButton(Icons.COPY, StudioBundle.get("copy"), () -> {
      Style copy = new Style();
      copy.setName(style.getName());
      List<Style> styles = writer.get();
      styles.add(styles.indexOf(style) + 1, copy);
      rebuildRows();
      commitChange();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_style"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        writer.get().remove(style);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(writer.get(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }
}
