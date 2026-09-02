package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Name cell for the document-model elements tree.
 *
 * <p>Displays the element name with its type icon and optional badge icons (annotations,
 * required). Supports inline renaming: calling {@link #startInlineEdit()} switches the
 * cell to a {@link TextField} in place of the read-only graphic. The rename is committed
 * on Enter or focus loss and cancelled on Escape, restoring the original name without
 * touching the model.
 *
 * <p>A {@link BiConsumer} rename callback must be supplied via
 * {@link #setRenameCallback(BiConsumer)} before inline editing is used; it receives the
 * element and the new (validated) name and is responsible for saving the model.
 */
class ElementNameTreeCell extends TreeTableCell<ElementViewModel, String> {

  /** Called when the user commits a rename: (element, newName) → save. */
  private BiConsumer<Element, String> renameCallback;

  private boolean editing = false;

  void setRenameCallback(BiConsumer<Element, String> renameCallback) {
    this.renameCallback = renameCallback;
  }

  /** Switches this cell into inline-edit mode immediately. */
  void startInlineEdit() {
    ElementViewModel viewModel = getViewModelOrNull();
    if (viewModel == null) {
      return;
    }
    editing = true;
    showTextField(viewModel.getName(), viewModel.getElement());
  }

  // -------------------------------------------------------------------------
  // TreeTableCell overrides
  // -------------------------------------------------------------------------

  @Override
  protected void updateItem(String name, boolean empty) {
    super.updateItem(name, empty);
    // If we are in the middle of an inline edit for this cell, don't overwrite the
    // text field with the read-only graphic.
    if (editing) {
      return;
    }
    if (empty || name == null) {
      setText(null);
      setGraphic(null);
      setTooltip(null);
      getStyleClass().remove("validation-error");
      return;
    }

    ElementViewModel viewModel = getViewModelOrNull();
    if (viewModel == null) {
      // Root pseudo-row: show plain text (no element backing it).
      setText(name);
      setGraphic(null);
      setTooltip(null);
      getStyleClass().remove("validation-error");
      return;
    }

    showReadOnly(name, viewModel);
  }

  // -------------------------------------------------------------------------
  // Read-only graphic
  // -------------------------------------------------------------------------

  private void showReadOnly(String name, ElementViewModel viewModel) {
    Node icon = WidgetFactory.createIcon(viewModel.getIcon());
    icon.getStyleClass().add("tree-icon");

    Label nameLabel = new Label(name);
    nameLabel.getStyleClass().add("tree-cell-name-label");
    HBox graphic = new HBox(4, icon, nameLabel);
    graphic.setAlignment(Pos.CENTER_LEFT);

    if (viewModel.hasAnnotations()) {
      Node annotationIcon = WidgetFactory.createIcon(Icons.ELEMENT_ANNOTATION);
      annotationIcon.getStyleClass().addAll("tree-icon", "tree-icon-badge");
      Tooltip.install(annotationIcon, WidgetFactory.createTooltip(StudioBundle.get("element_has_annotations")));
      graphic.getChildren().add(annotationIcon);
    }
    if (viewModel.isRequired()) {
      Node requiredIcon = WidgetFactory.createIcon(Icons.ELEMENT_REQUIRED);
      requiredIcon.getStyleClass().addAll("tree-icon", "tree-icon-badge");
      Tooltip.install(requiredIcon, WidgetFactory.createTooltip(StudioBundle.get("required_element")));
      graphic.getChildren().add(requiredIcon);
    }

    setText(null);
    setGraphic(graphic);

    if (viewModel.hasError()) {
      if (!getStyleClass().contains("validation-error")) {
        getStyleClass().add("validation-error");
      }
      nameLabel.getStyleClass().add("validation-error");
      String messages = viewModel.getErrorMessages().stream()
          .map(m -> "• " + m)
          .collect(Collectors.joining("\n"));
      setTooltip(WidgetFactory.createTooltip(messages));
    }
    else {
      getStyleClass().remove("validation-error");
      setTooltip(null);
    }
  }

  // -------------------------------------------------------------------------
  // Inline edit
  // -------------------------------------------------------------------------

  private void showTextField(String currentName, Element element) {
    TextField textField = new TextField(currentName);
    textField.getStyleClass().add("tree-cell-rename-field");
    textField.selectAll();

    setText(null);
    setGraphic(textField);

    Platform.runLater(textField::requestFocus);

    textField.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        commitEdit(textField.getText().trim(), element);
        event.consume();
      }
      else if (event.getCode() == KeyCode.ESCAPE) {
        cancelEdit(element);
        event.consume();
      }
    });

    textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
      if (!isFocused && editing) {
        commitEdit(textField.getText().trim(), element);
      }
    });
  }

  private void commitEdit(String newName, Element element) {
    if (!editing) {
      return;
    }
    editing = false;
    if (!newName.isEmpty() && !newName.equals(element.getName()) && renameCallback != null) {
      renameCallback.accept(element, newName);
    }
    else {
      // Restore read-only display without touching the model.
      updateItem(element.getName(), false);
    }
  }

  private void cancelEdit(Element element) {
    if (!editing) {
      return;
    }
    editing = false;
    updateItem(element.getName(), false);
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private ElementViewModel getViewModelOrNull() {
    if (getTableRow() == null) {
      return null;
    }
    return getTableRow().getItem();
  }
}
