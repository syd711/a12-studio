package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Edits a {@link ContentModel}: the element tree on the left (add/remove/reorder), and per selected
 * element its type/namespace plus the raw {@code props} JSON. The Lexical {@code tree}/{@code html}
 * payloads inside props are deliberately edited as opaque JSON — the studio does not reinterpret them.
 */
public class ContentModelEditorController extends AbstractEditorController implements Initializable {

  // Component types of the content engine's core element library, offered when adding children.
  private static final List<String> KNOWN_TYPES = List.of(
      "Box", "Grid", "GridRow", "GridColumn", "Paragraph", "Heading", "UnorderedList", "ListItem",
      "Table", "TableHead", "TableHeadRow", "TableHeadCell", "TableBody", "TableBodyRow", "TableBodyCell",
      "TableFoot", "MessageBox", "Image");

  private static final String DEFAULT_NAMESPACE = "com.mgmtp.a12.contentengine";

  @FXML
  private TreeView<ContentElement> elementsTree;

  @FXML
  private TextField elementIdField;

  @FXML
  private javafx.scene.control.ComboBox<String> elementTypeField;

  @FXML
  private TextField elementNamespaceField;

  @FXML
  private TextArea elementPropsField;

  @FXML
  private Label propsErrorLabel;

  private ContentModel model;
  private boolean updatingFromModel;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    elementsTree.setCellFactory(tree -> new javafx.scene.control.TreeCell<>() {
      @Override
      protected void updateItem(ContentElement element, boolean empty) {
        super.updateItem(element, empty);
        setText(empty || element == null ? null : describe(element));
      }
    });
    elementsTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        showElement(newValue != null ? newValue.getValue() : null));

    elementTypeField.getItems().setAll(KNOWN_TYPES);
    elementTypeField.setEditable(true);
    elementTypeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      ContentElement selected = selectedElement();
      if (updatingFromModel || selected == null || newValue == null || newValue.isBlank()) {
        return;
      }
      selected.setType(newValue);
      elementsTree.refresh();
      commitChange();
    });

    elementPropsField.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
      if (!hasFocus) {
        commitProps();
      }
    });
  }

  private static String describe(ContentElement element) {
    String type = element.getType() != null ? element.getType() : "?";
    return element.getId() != null ? type + " (" + element.getId() + ")" : type;
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((ContentModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull ContentModel model) {
    this.model = model;
    TreeItem<ContentElement> rootItem = buildTreeItem(model.getContent().getRoot());
    rootItem.setExpanded(true);
    elementsTree.setRoot(rootItem);
    elementsTree.getSelectionModel().select(rootItem);
  }

  private TreeItem<ContentElement> buildTreeItem(ContentElement element) {
    TreeItem<ContentElement> item = new TreeItem<>(element);
    if (element.getChildren() != null) {
      for (ContentElement child : element.getChildren()) {
        item.getChildren().add(buildTreeItem(child));
      }
    }
    item.setExpanded(true);
    return item;
  }

  private ContentElement selectedElement() {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    return item != null ? item.getValue() : null;
  }

  private void showElement(ContentElement element) {
    updatingFromModel = true;
    try {
      propsErrorLabel.setVisible(false);
      if (element == null) {
        elementIdField.setText("");
        elementTypeField.setValue(null);
        elementNamespaceField.setText("");
        elementPropsField.setText("");
        return;
      }
      elementIdField.setText(element.getId() != null ? element.getId() : "");
      elementTypeField.setValue(element.getType());
      elementNamespaceField.setText(element.getNamespace() != null ? element.getNamespace() : "");
      elementPropsField.setText(prettyProps(element));
    }
    finally {
      updatingFromModel = false;
    }
  }

  private String prettyProps(ContentElement element) {
    if (element.getProps() == null) {
      return "{}";
    }
    return JsonSettings.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(element.getProps());
  }

  /** Parses the props text area back into the element; invalid JSON is rejected and flagged, not saved. */
  private void commitProps() {
    ContentElement element = selectedElement();
    if (element == null || updatingFromModel) {
      return;
    }
    String text = elementPropsField.getText();
    try {
      JsonNode node = JsonSettings.objectMapper.readTree(text == null || text.isBlank() ? "{}" : text);
      if (!node.isObject()) {
        showPropsError("Props must be a JSON object.");
        return;
      }
      @SuppressWarnings("unchecked")
      Map<String, Object> props = JsonSettings.objectMapper.treeToValue(node, LinkedHashMap.class);
      element.setProps(props);
      propsErrorLabel.setVisible(false);
      commitChange();
    }
    catch (RuntimeException e) {
      showPropsError("Invalid JSON: " + e.getMessage());
    }
  }

  private void showPropsError(String message) {
    propsErrorLabel.setText(message);
    propsErrorLabel.setVisible(true);
  }

  @FXML
  public void onAddChild(ActionEvent e) {
    TreeItem<ContentElement> parentItem = elementsTree.getSelectionModel().getSelectedItem();
    if (parentItem == null) {
      return;
    }
    ContentElement parent = parentItem.getValue();
    if (parent.getChildren() == null) {
      parent.setChildren(new ArrayList<>());
    }

    ContentElement child = new ContentElement();
    child.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
    child.setType("Box");
    child.setNamespace(parent.getNamespace() != null ? parent.getNamespace() : DEFAULT_NAMESPACE);
    child.setProps(new LinkedHashMap<>());
    parent.getChildren().add(child);

    TreeItem<ContentElement> childItem = new TreeItem<>(child);
    parentItem.getChildren().add(childItem);
    parentItem.setExpanded(true);
    elementsTree.getSelectionModel().select(childItem);
    commitChange();
  }

  @FXML
  public void onRemoveElement(ActionEvent e) {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    if (item == null || item.getParent() == null) {
      // The root element cannot be removed (mirrors the content engine's root element rules).
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "Remove element \"" + describe(item.getValue()) + "\" and all of its children?",
        ButtonType.OK, ButtonType.CANCEL);
    if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
      return;
    }

    TreeItem<ContentElement> parentItem = item.getParent();
    parentItem.getValue().getChildren().remove(item.getValue());
    parentItem.getChildren().remove(item);
    commitChange();
  }

  @FXML
  public void onMoveUp(ActionEvent e) {
    moveSelected(-1);
  }

  @FXML
  public void onMoveDown(ActionEvent e) {
    moveSelected(1);
  }

  private void moveSelected(int offset) {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    if (item == null || item.getParent() == null) {
      return;
    }
    TreeItem<ContentElement> parentItem = item.getParent();
    List<ContentElement> siblings = parentItem.getValue().getChildren();
    int index = siblings.indexOf(item.getValue());
    int target = index + offset;
    if (index < 0 || target < 0 || target >= siblings.size()) {
      return;
    }

    siblings.set(index, siblings.get(target));
    siblings.set(target, item.getValue());
    parentItem.getChildren().remove(item);
    parentItem.getChildren().add(target, item);
    elementsTree.getSelectionModel().select(item);
    commitChange();
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.CONTENT;
  }
}
