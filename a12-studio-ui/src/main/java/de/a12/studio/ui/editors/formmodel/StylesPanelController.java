package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.Style;
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
 */
public class StylesPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; unique per panel instance, mirroring EventButtonsPanelController's approach
  // (a plain static DataFormat would collide if this panel is ever embedded twice in the same window).
  private static final DataFormat STYLE_INDEX = new DataFormat("application/x-a12-form-style-index");

  @FXML
  private VBox stylesList;

  @FXML
  private Label stylesEmptyLabel;

  private Supplier<List<Style>> reader;

  private Supplier<List<Style>> writer;

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
    boolean empty = styles.isEmpty();
    stylesEmptyLabel.setVisible(empty);
    stylesEmptyLabel.setManaged(empty);

    for (int index = 0; index < styles.size(); index++) {
      stylesList.getChildren().add(createRow(styles.get(index), index, styles.size()));
    }
  }

  private HBox createRow(Style style, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    TextField nameField = new TextField();
    nameField.setId("formStyleName-" + index);
    nameField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameField, Priority.ALWAYS);
    setFieldValue(nameField, style.getName());
    bindTextField(nameField, (el, value) -> style.setName(value.isEmpty() ? null : value));

    HBox row = new HBox(10.0, dragHandle, nameField, createActionsBox(style, index, rowCount));
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
