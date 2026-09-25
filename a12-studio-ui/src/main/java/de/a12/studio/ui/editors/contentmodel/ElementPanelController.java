package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementDefaults;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * The identity of the selected element: id and namespace (read-only) and its type. SME picks the type when an
 * element is added from its component panel and never retypes it; the studio adds every child as a Box, so the type
 * stays editable here. Changing it fills in the default props of the new type that the element lacks (see {@link
 * ContentElementDefaults#applyMissing}) and tells the editor, which refreshes the tree and the other panels.
 */
public class ElementPanelController extends AbstractContentSettingsPanel {

  /** Component types of the content engine's core element library, offered as the element's type. */
  static final List<String> KNOWN_TYPES = List.of(
      "Box", "Grid", "GridRow", "GridColumn", "Paragraph", "Heading", "OrderedList", "UnorderedList", "ListItem",
      "Table", "TableHead", "TableHeadRow", "TableHeadCell", "TableBody", "TableBodyRow", "TableBodyCell",
      "TableFoot", "TableFootRow", "TableFootCell", "MessageBox", "Image", "Video", "Icon", "Link", "Button",
      "ButtonGroup", "ButtonGroupContainer", "Tooltip", "Expandable", "ExpandableTitle", "ExpandableContent",
      "ExpandableCollapsed", "ExpandableExpanded", "InteractiveList", "InteractiveListItem", "InteractiveTile",
      "Conditional", "MediaQuery", "Group", "FieldOutput", "AddRowAction", "DeleteRowAction", "CancelAction",
      "CommitAction", "SaveAction");

  @FXML
  private TextField elementIdField;

  @FXML
  private ComboBox<String> elementTypeField;

  @FXML
  private TextField elementNamespaceField;

  private Consumer<ContentElement> onTypeChange = element -> {
  };
  private boolean populating;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    elementTypeField.getItems().setAll(KNOWN_TYPES);
    elementTypeField.setEditable(true);
    elementTypeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      ContentElement element = currentElement();
      if (populating || element == null || newValue == null || newValue.isBlank() || newValue.equals(element.getType())) {
        return;
      }
      element.setType(newValue);
      ContentElementDefaults.applyMissing(element);
      onTypeChange.accept(element);
      changed();
    });
  }

  /** Called after the user retyped the element (its props may have gained defaults). */
  public void setOnTypeChange(@NonNull Consumer<ContentElement> onTypeChange) {
    this.onTypeChange = onTypeChange;
  }

  @Override
  protected boolean appliesTo(String type) {
    return true;
  }

  @Override
  protected void populate(@NonNull ContentElement element) {
    populating = true;
    try {
      elementIdField.setText(element.getId() != null ? element.getId() : "");
      elementTypeField.setValue(element.getType());
      elementNamespaceField.setText(element.getNamespace() != null ? element.getNamespace() : "");
    }
    finally {
      populating = false;
    }
  }
}
