package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnStyles;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.overview.OverviewStylesValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits {@link de.a12.studio.models.overviewmodel.OverviewModelContent#getStyles()}: a list of CSS style
 * class names, each reorderable (drag handle or move up/down), copyable and deletable, with a directly
 * editable text field per row that must not be blank (see {@link OverviewStylesValidator}). Not bound to a
 * single Element (styles live on the model's content), so it follows the model-header pattern used by e.g.
 * {@link de.a12.studio.ui.editors.applicationmodel.ModulesPanelController}. Also reused, via {@link #setColumn}, for a single {@link Column}'s header-
 * cell and content-cell style lists ({@link #configureColumnHeaderStyles} / {@link #configureColumnContentStyles}),
 * and, via {@link #setCustom}, for an arbitrary owner's own {@code List<String>} styles field (e.g. a {@link
 * de.a12.studio.models.overviewmodel.Button}/{@link de.a12.studio.models.overviewmodel.ButtonElement}'s {@code
 * styles}, edited by {@link de.a12.studio.ui.editors.propertyeditors.dialogs.EventButtonDialogController}) -
 * neither of which have a dedicated validator (that only exists for the model-level list), so {@link
 * #refreshStylesError} is a no-op in those modes.
 */
public class StylesPanelController extends AbstractPropertyEditor {

  private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

  private static final int COMMIT_DEBOUNCE_MS = 150;

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getStyles().
  private static final DataFormat STYLE_INDEX = new DataFormat("application/x-a12-style-index");

  @FXML
  private VBox stylesList;

  @FXML
  private Label stylesEmptyLabel;

  private final Debouncer debouncer = new Debouncer();

  private OverviewModel model;

  private Column column;

  private Function<Column, List<String>> columnStylesAccessor;

  // Set (instead of model/column) by setCustom(), for a plain List<String> living on neither the model's own
  // content nor a Column - e.g. an overviewmodel.Button/ButtonElement's own styles, edited from a modal dialog
  // (see EventButtonDialogController) rather than the currently selected project item's own editor. Behaves
  // like column mode: refreshStylesError() is a no-op, since there's no dedicated validator for this list either.
  private Supplier<List<String>> customStylesSupplier;

  /**
   * Binds this panel to an arbitrary owner's {@code List<String>} styles field via a getter, e.g. {@code
   * button::getStyles}. {@code stylesSupplier} is used for both reading and writing, so the owner (e.g. the
   * {@code Button}) must already exist.
   */
  public void setCustom(@NonNull Supplier<List<String>> stylesSupplier) {
    this.model = null;
    this.column = null;
    this.customStylesSupplier = stylesSupplier;
    rebuildRows();
  }

  public void configureColumnHeaderStyles() {
    this.columnStylesAccessor = column -> ensureColumnStyles(column).getHeader();
    setTitle(StudioBundle.get("style_for_header_cells"));
    setSettingsKeySuffix(".headerStyles");
  }

  public void configureColumnContentStyles() {
    this.columnStylesAccessor = column -> ensureColumnStyles(column).getContent();
    setTitle(StudioBundle.get("style_for_content_cells"));
    setSettingsKeySuffix(".contentStyles");
  }

  public void setModel(@NonNull OverviewModel model) {
    this.column = null;
    this.customStylesSupplier = null;
    this.model = model;
    rebuildRows();
  }

  /** {@link #configureColumnHeaderStyles} or {@link #configureColumnContentStyles} must be called first. */
  public void setColumn(@NonNull Column column) {
    this.model = null;
    this.customStylesSupplier = null;
    this.column = column;
    rebuildRows();
  }

  private static ColumnStyles ensureColumnStyles(Column column) {
    if (column.getStyles() == null) {
      column.setStyles(new ColumnStyles());
    }
    return column.getStyles();
  }

  @FXML
  private void onAdd() {
    getStyles().add("");
    rebuildRows();
    commitChange();
  }

  private List<String> getStyles() {
    if (customStylesSupplier != null) {
      return customStylesSupplier.get();
    }
    return column != null ? columnStylesAccessor.apply(column) : model.getContent().getStyles();
  }

  private void rebuildRows() {
    refreshStylesError();
    stylesList.getChildren().clear();

    List<String> styles = getStyles();
    boolean empty = styles.isEmpty();
    stylesEmptyLabel.setVisible(empty);
    stylesEmptyLabel.setManaged(empty);

    for (int index = 0; index < styles.size(); index++) {
      stylesList.getChildren().add(createRow(index, styles.size()));
    }
  }

  /**
   * Not bound to an {@link de.a12.studio.models.documentmodel.Element}, so the base class's element-keyed
   * validation plumbing never runs for this panel; queries {@link OverviewStylesValidator}'s dedicated
   * element id directly instead. Called from {@link #rebuildRows} (itself called by every structural
   * mutation here, plus {@link #setModel}) and after every debounced text commit, so this always reflects
   * the list as currently shown.
   */
  private void refreshStylesError() {
    if (model == null) {
      return;
    }
    List<ModelValidationError> errors = Studio.getValidationService().validateElement(model, OverviewStylesValidator.ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    }
    else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private HBox createRow(int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    TextField styleField = new TextField();
    styleField.setId("style-" + index);
    styleField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(styleField, Priority.ALWAYS);
    String initialValue = getStyles().get(index);
    setFieldValue(styleField, initialValue);
    styleField.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, initialValue == null || initialValue.isBlank());
    styleField.textProperty().addListener((observable, oldValue, newValue) -> {
      getStyles().set(index, newValue);
      styleField.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, newValue == null || newValue.isBlank());
      debouncer.debounce(styleField.getId(), () -> {
        refreshStylesError();
        commitChange();
      }, COMMIT_DEBOUNCE_MS, true);
    });

    HBox row = new HBox(10.0, dragHandle, styleField, createActionsBox(index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, STYLE_INDEX, index, this::moveStyle);
    return row;
  }

  private void moveStyle(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getStyles(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitChange();
    }
  }

  private HBox createActionsBox(int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button copyButton = RowFactory.createActionButton(Icons.COPY, StudioBundle.get("copy"), () -> {
      List<String> styles = getStyles();
      styles.add(index + 1, styles.get(index));
      rebuildRows();
      commitChange();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_style"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getStyles().remove(index);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getStyles(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }
}
