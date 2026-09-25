package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementDefaults;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewLauncher;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link ContentModel}: the element tree on the left (add/remove/reorder), the live preview in the middle,
 * and on the right, for the selected element, the same settings SME's setting panel offers for its type, as a stack
 * of {@link ContentSettingsPanel}s (each shows itself only for the types it has settings for). The Lexical {@code
 * tree}/{@code html} payloads inside props are deliberately edited as opaque JSON in the "Raw properties" panel —
 * the studio does not reinterpret them.
 *
 * <p>The center shows the Content Model rendered by the real Content Engine, as SME's preview window does: the
 * installed Simple Model Editor client is loaded into a {@code WebView} and fed the live model by the {@link
 * de.a12.studio.ui.preview.PreviewServer} (see {@link de.a12.studio.ui.preview.ContentModelPreviewSession}).
 */
public class ContentModelEditorController extends AbstractEditorController implements Initializable {

  private static final int TREE_ICON_SIZE = 14;
  // Edits made in the panels are applied to the model at once; the file is written once typing pauses.
  private static final int SAVE_DEBOUNCE_MS = 300;
  private static final String SAVE_KEY = "save";

  @FXML
  private TreeView<ContentElement> elementsTree;

  @FXML
  private VBox settingsBox;

  @FXML
  private ElementPanelController elementPanelController;

  @FXML
  private RawPropsPanelController rawPropsPanelController;

  @FXML
  private WebView previewWebView;

  @FXML
  private Label previewUnavailableLabel;

  private final List<ContentSettingsPanel> panels = new ArrayList<>();
  private final Debouncer saveDebouncer = new Debouncer();
  private boolean savePending;
  private ContentModel model;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    elementsTree.setCellFactory(tree -> new javafx.scene.control.TreeCell<>() {
      @Override
      protected void updateItem(ContentElement element, boolean empty) {
        super.updateItem(element, empty);
        setText(empty || element == null ? null : typeLabel(element));
        if (empty || element == null) {
          setGraphic(null);
          return;
        }
        // "tree-icon" lets the tree stylesheet switch the icon to the inverse color on the selected row.
        FontIcon icon = WidgetFactory.createIcon(iconFor(element.getType()), TREE_ICON_SIZE, null);
        icon.getStyleClass().add("tree-icon");
        setGraphic(icon);
      }
    });
    elementsTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        showElement(newValue != null ? newValue.getValue() : null));

    for (Node child : settingsBox.getChildren()) {
      if (child instanceof TitledPane pane && pane.getProperties().get(ContentSettingsPanel.PANEL_KEY) instanceof ContentSettingsPanel panel) {
        panels.add(panel);
        panel.setOnChange(() -> onPanelChanged(panel));
      }
    }
    ContentSettingsPanel.Context context = new ContentSettingsPanel.Context() {
      @Override
      public ContentElement parentOf(@NonNull ContentElement element) {
        TreeItem<ContentElement> item = findItem(elementsTree.getRoot(), element);
        return item != null && item.getParent() != null ? item.getParent().getValue() : null;
      }

      @Override
      public void structureChanged() {
        TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
        if (item != null) {
          item.getChildren().setAll(buildTreeItem(item.getValue()).getChildren());
        }
      }
    };
    panels.forEach(panel -> panel.setContext(context));
    // The element panel include is optional in the FXML; without it there is no type editor to wire up.
    if (elementPanelController != null) {
      elementPanelController.setOnTypeChange(element -> {
        elementsTree.refresh();
        panels.stream().filter(panel -> panel != elementPanelController).forEach(panel -> panel.showElement(element));
      });
    }
  }

  private static String typeLabel(ContentElement element) {
    return element.getType() != null ? element.getType() : "?";
  }

  /** Full description including the id, for messages that must identify one specific element. */
  private static String describe(ContentElement element) {
    return element.getId() != null ? typeLabel(element) + " (" + element.getId() + ")" : typeLabel(element);
  }

  private static String iconFor(String type) {
    if (type == null) {
      return "mdi2s-shape-outline";
    }
    return switch (type) {
      case "Box" -> "mdi2s-square-outline";
      case "Grid" -> "mdi2v-view-grid-outline";
      case "GridRow", "TableHeadRow", "TableBodyRow" -> "mdi2t-table-row";
      case "GridColumn", "TableBodyCell" -> "mdi2t-table-column";
      case "Paragraph" -> "mdi2f-format-paragraph";
      case "Heading" -> "mdi2f-format-header-1";
      case "UnorderedList" -> "mdi2f-format-list-bulleted";
      case "ListItem" -> "mdi2c-circle-small";
      case "Table" -> "mdi2t-table";
      case "TableHead" -> "mdi2t-table-arrow-up";
      case "TableHeadCell" -> "mdi2t-table-headers-eye";
      case "TableBody" -> "mdi2t-table-large";
      case "TableFoot" -> "mdi2t-table-arrow-down";
      case "MessageBox" -> "mdi2m-message-alert-outline";
      case "Image" -> "mdi2i-image-outline";
      default -> "mdi2s-shape-outline";
    };
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((ContentModel) model);
    updateSettingsErrorBadge();
    startPreview();
  }

  private void startPreview() {
    try {
      previewWebView.getEngine().load(PreviewLauncher.registerContentPreview(projectItem));
      previewUnavailableLabel.setVisible(false);
      previewUnavailableLabel.setManaged(false);
      previewWebView.setVisible(true);
    }
    catch (PreviewAppException e) {
      previewWebView.setVisible(false);
      previewUnavailableLabel.setText(StudioBundle.get("content_model_editor.preview_unavailable", e.getMessage()));
      previewUnavailableLabel.setVisible(true);
      previewUnavailableLabel.setManaged(true);
    }
  }

  /**
   * Stops the preview page (it polls the preview server) once the editor's tab is closed, writes an edit that is
   * still waiting for its debounced save, and releases the panels.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    if (event.getItem().equals(projectItem)) {
      previewWebView.getEngine().load("about:blank");
      saveDebouncer.shutdown();
      if (savePending) {
        savePending = false;
        commitChange();
      }
      panels.forEach(ContentSettingsPanel::destroy);
    }
    super.modelClosed(event);
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

  private static TreeItem<ContentElement> findItem(TreeItem<ContentElement> from, ContentElement element) {
    if (from == null) {
      return null;
    }
    if (from.getValue() == element) {
      return from;
    }
    for (TreeItem<ContentElement> child : from.getChildren()) {
      TreeItem<ContentElement> found = findItem(child, element);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private ContentElement selectedElement() {
    TreeItem<ContentElement> item = elementsTree.getSelectionModel().getSelectedItem();
    return item != null ? item.getValue() : null;
  }

  private void showElement(ContentElement element) {
    panels.forEach(panel -> panel.showElement(element));
  }

  /**
   * A panel applied an edit to the selected element's props. The panel that made it already shows the result; the
   * raw JSON view is brought up to date, or, when the raw JSON itself was edited, every other panel is.
   */
  private void onPanelChanged(ContentSettingsPanel source) {
    if (source == rawPropsPanelController) {
      ContentElement element = selectedElement();
      panels.stream().filter(panel -> panel != source).forEach(panel -> panel.showElement(element));
    }
    else {
      rawPropsPanelController.refresh();
    }
    scheduleSave();
  }

  private void scheduleSave() {
    savePending = true;
    saveDebouncer.debounce(SAVE_KEY, () -> {
      if (savePending) {
        savePending = false;
        commitChange();
      }
    }, SAVE_DEBOUNCE_MS, true);
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
    child.setId(ContentElementDefaults.newId());
    child.setType("Box");
    child.setNamespace(parent.getNamespace() != null ? parent.getNamespace() : ContentElementDefaults.DEFAULT_NAMESPACE);
    ContentElementDefaults.applyMissing(child);
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
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.CONTENT;
  }
}
