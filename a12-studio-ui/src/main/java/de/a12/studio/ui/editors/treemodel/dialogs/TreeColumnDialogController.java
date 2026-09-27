package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.overviewmodel.Alignment;
import de.a12.studio.models.overviewmodel.ColumnAlignment;
import de.a12.studio.models.overviewmodel.ColumnStyles;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions.ElementKind;
import de.a12.studio.ui.editors.overviewmodel.StylesPanelController;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Add/edit dialog for a single {@link TreeColumn}, opened from {@link
 * de.a12.studio.ui.editors.treemodel.TreeColumnsPanelController} by clicking a column row or its Add button. Edits what SME's
 * column detail screen edits: Name, the header (multilingual Label, Hide Label, Icon), Width (0.3 or more, one decimal), Fixed
 * Width, Pin Direction, the Alignment of header and content and their Styles. It works on a copy of the column (see {@link
 * #getResult()}), which the caller copies back once the dialog is confirmed - the same way for an edit as for an Add. When
 * opened for a node type's column mapping (see {@link #init(Stage, TreeColumn, List, ElementIndex, TreeNodeColumn)}) it also
 * offers the node's Document Model field shown in the column and, for an attachment or multi-select field, its display mode,
 * returned with the column by {@link #getMappingResult()}.
 */
public class TreeColumnDialogController implements DialogController {

  private static final List<String> PIN_DIRECTIONS = Arrays.asList(null, TreeColumn.PIN_DIRECTION_LEFT, TreeColumn.PIN_DIRECTION_RIGHT);
  private static final List<String> HORIZONTAL_ALIGNMENTS = Arrays.asList(null, "left", "center", "right");
  private static final List<String> VERTICAL_ALIGNMENTS = Arrays.asList(null, "top", "middle", "bottom");

  @FXML
  private TextField nameField;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private CheckBox hideLabelField;

  @FXML
  private IconPanelController iconPanelController;

  @FXML
  private TextField widthField;

  @FXML
  private CheckBox fixedWidthField;

  @FXML
  private ComboBox<String> pinDirectionCombo;

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
  private VBox fieldBox;

  @FXML
  private ComboBox<String> fieldCombo;

  @FXML
  private VBox displayModeBox;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  // Shared by the embedded panels so their commits aren't persisted while the dialog is open: the caller persists.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  // The working copy every field edits.
  private TreeColumn column;

  // Whether the styles object was only created to be edited: it is dropped again when nothing was put into it.
  private boolean stylesCreated;

  // Resolves the field ids offered by fieldCombo to their paths; null shows the raw ids.
  private ElementIndex elementIndex;

  // The node's mapping of this column while the dialog is in mapping mode: field and display mode.
  private TreeNodeColumn mapping;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Set while the fields are being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  /** The edited column together with the node's field for it and its display mode, as chosen in a column mapping dialog. */
  public record MappingResult(TreeColumn column, String field, TreeNodeColumn.Configuration configuration) {
  }

  @FXML
  private void initialize() {
    labelController.configureCustom("label", StudioBundle.get("label"));
    labelController.setSaveMode(saveMode);
    iconPanelController.setSaveMode(saveMode);
    stylesHeaderController.configureCustom(StudioBundle.get("style_for_header_cells"), ".treeColumnHeaderStyles");
    stylesHeaderController.setSaveMode(saveMode);
    stylesContentController.configureCustom(StudioBundle.get("style_for_content_cells"), ".treeColumnContentStyles");
    stylesContentController.setSaveMode(saveMode);

    pinDirectionCombo.setItems(FXCollections.observableArrayList(PIN_DIRECTIONS));
    pinDirectionCombo.setConverter(displayConverter(value -> value == null ? "(None)" : capitalize(value)));
    pinDirectionCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
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

    fieldCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? "(None)" : ColumnMappingEditor.displayPath(elementIndex, value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    fieldCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel && mapping != null) {
        mapping.setElementRef(newValue);
        ColumnMappingEditor.syncDisplayMode(mapping, kindOf(newValue));
        refreshDisplayMode();
      }
    });
    setVisible(fieldBox, false);
    setVisible(displayModeBox, false);

    WidgetFactory.restrictToDecimalInput(widthField);
    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        column.setName(newValue == null ? null : newValue.trim());
      }
    });
    widthField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        column.setWidth(parseWidth(newValue));
      }
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

    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> isBlank(nameField.getText()) || parseWidth(widthField.getText()) == null,
        nameField.textProperty(), widthField.textProperty()));
    nameField.requestFocus();
  }

  /** Edits a copy of {@code existing}, or a new column (width 1.0) when it is {@code null}. */
  void init(Stage stage, TreeColumn existing) {
    init(stage, existing, null, null, null);
  }

  /**
   * As {@link #init(Stage, TreeColumn)}, additionally offering the Document Model field of the node type's mapping of this
   * column ({@code mapping}, may be {@code null} for none) out of {@code fieldOptions}, shown by its path in {@code
   * elementIndex}; a {@code null} list hides the field. A field that is no longer offered stays selectable, so opening and
   * confirming the dialog doesn't silently drop it.
   */
  void init(Stage stage, TreeColumn existing, List<String> fieldOptions, ElementIndex elementIndex, TreeNodeColumn mapping) {
    this.stage = stage;
    this.elementIndex = elementIndex;
    this.column = existing != null ? Dialogs.copyOf(existing) : newColumn();
    this.stylesCreated = false;

    boolean mappingMode = fieldOptions != null;
    this.mapping = null;
    if (mappingMode) {
      this.mapping = new TreeNodeColumn();
      this.mapping.setColumnRef(column.getId());
      if (mapping != null) {
        this.mapping.setElementRef(mapping.getElementRef());
        this.mapping.setConfiguration(mapping.getConfiguration() != null ? ColumnMappingEditor.copyOf(mapping).getConfiguration() : null);
      }
    }

    updatingFromModel = true;
    try {
      if (mappingMode) {
        List<String> items = new ArrayList<>();
        items.add(null);
        items.addAll(fieldOptions);
        String field = this.mapping.getElementRef();
        if (field != null && !items.contains(field)) {
          items.add(field);
        }
        fieldCombo.setItems(FXCollections.observableArrayList(items));
        fieldCombo.setValue(field);
      }
      nameField.setText(column.getName() != null ? column.getName() : "");
      hideLabelField.setSelected(Boolean.TRUE.equals(column.getLabelHidden()));
      widthField.setText(column.getWidthText());
      fixedWidthField.setSelected(Boolean.TRUE.equals(column.getFixedWidth()));
      pinDirectionCombo.setValue(column.getPinDirection());
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
    setVisible(fieldBox, mappingMode);
    refreshDisplayMode();

    labelController.setCustom(column::getLabel);
    iconPanelController.setCustom(column::getIcon, column::setIcon);
    stylesHeaderController.setCustom(() -> ensureStyles().getHeader());
    stylesContentController.setCustom(() -> ensureStyles().getContent());
  }

  /** Unregisters the embedded panels once this dialog is closed, however (OK, Cancel, the window's close button). */
  void destroy() {
    labelController.destroy();
    iconPanelController.destroy();
    stylesHeaderController.destroy();
    stylesContentController.destroy();
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

  /** The edited copy once the dialog was confirmed; the caller copies it onto its column or adds it. */
  Optional<TreeColumn> getResult() {
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return Optional.empty();
    }
    if (stylesCreated && column.getStyles() != null && column.getStyles().getHeader().isEmpty() && column.getStyles().getContent().isEmpty()) {
      column.setStyles(null);
    }
    return Optional.of(column);
  }

  Optional<MappingResult> getMappingResult() {
    return getResult().map(edited -> new MappingResult(edited, mapping != null ? mapping.getElementRef() : null,
        mapping != null ? mapping.getConfiguration() : null));
  }

  /** Shows the display mode combo while the chosen field is an attachment or multi-select field. */
  private void refreshDisplayMode() {
    displayModeBox.getChildren().clear();
    ElementKind kind = mapping != null ? kindOf(mapping.getElementRef()) : ElementKind.PLAIN;
    boolean special = kind == ElementKind.ATTACHMENT || kind == ElementKind.MULTI_SELECT;
    if (special) {
      Label displayModeLabel = new Label(StudioBundle.get("tree_column_dialog.display_mode"));
      displayModeLabel.getStyleClass().add("field-label");
      displayModeBox.getChildren().addAll(displayModeLabel, ColumnMappingEditor.displayModeCombo(mapping, kind, () -> {
      }));
    }
    setVisible(displayModeBox, special);
  }

  private ElementKind kindOf(String field) {
    return OverviewElementOptions.elementKind(elementIndex, field);
  }

  private ColumnStyles ensureStyles() {
    if (column.getStyles() == null) {
      column.setStyles(new ColumnStyles());
      stylesCreated = true;
    }
    return column.getStyles();
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

  private static TreeColumn newColumn() {
    TreeColumn column = new TreeColumn();
    column.setWidth(TreeColumn.DEFAULT_WIDTH);
    return column;
  }

  private static void setVisible(VBox box, boolean visible) {
    box.setVisible(visible);
    box.setManaged(visible);
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

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String capitalize(String value) {
    return value == null || value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
  }

  /**
   * SME's column width: a number of at least {@link TreeColumn#MIN_WIDTH}, kept to one decimal (a comma is accepted as the
   * decimal separator); {@code null} for anything else.
   */
  static Double parseWidth(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      double value = Math.round(Double.parseDouble(text.trim().replace(',', '.')) * 10.0) / 10.0;
      return value >= TreeColumn.MIN_WIDTH ? value : null;
    }
    catch (NumberFormatException e) {
      return null;
    }
  }
}
