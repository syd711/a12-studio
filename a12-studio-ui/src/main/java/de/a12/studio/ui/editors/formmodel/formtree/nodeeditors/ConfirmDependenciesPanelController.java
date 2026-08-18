package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DependentCase;
import de.a12.studio.models.formmodel.DependentConfig;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FieldConfiguration;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Dependencies" tab for the confirm-field Control node editor. Two trees (one per confirm value:
 * {@code "true"} and {@code null} / "no value") let the user mark form nodes as hidden when the confirm
 * field has that value. Checked node IDs are stored in {@link DependentCase#getNotRelevantNodes()} inside
 * the {@link FieldConfigEntry#getDependentField()} for the bound field's elementRef.
 *
 * <h3>Tree structure</h3>
 * <ul>
 *   <li>{@link Screen} — non-selectable root per screen.</li>
 *   <li>Direct {@link ScreenElement} children of a screen — selectable (Section, ControlGrid,
 *       MultiColumnSection, Repeat, …).</li>
 *   <li>If a direct child is a {@link Section}, its {@link ControlGrid} children are also shown as
 *       selectable children of that section node.</li>
 *   <li>Nothing deeper is shown.</li>
 * </ul>
 */
public class ConfirmDependenciesPanelController implements Initializable {

  /** masterValue stored in JSON for the "true" confirm case. */
  public static final String VALUE_TRUE = "true";

  @FXML private TreeView<FormNodeItem> trueTree;
  @FXML private TreeView<FormNodeItem> noValueTree;

  private Control control;
  private FormModelContent content;

  /** Prevents programmatic tree population from triggering save listeners. */
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    configureCellFactory(trueTree);
    configureCellFactory(noValueTree);
  }

  private static void configureCellFactory(@NonNull TreeView<FormNodeItem> tree) {
    tree.setCellFactory(tv -> new CheckBoxTreeCell<>() {
      @Override
      public void updateItem(FormNodeItem item, boolean empty) {
        super.updateItem(item, empty);
        setText((!empty && item != null) ? item.displayName() : null);
      }
    });
  }

  /**
   * Binds this panel to the given control and form model content. Rebuilds both trees and restores
   * the stored selection from {@link DependentCase#getNotRelevantNodes()}.
   */
  public void setControl(@NonNull Control control, @Nullable FormModelContent content) {
    this.control = control;
    this.content = content;

    List<String> trueIds   = loadNodeIds(VALUE_TRUE);
    List<String> noValueIds = loadNodeIds(null);

    updatingFromModel = true;
    try {
      buildTree(trueTree,    content, trueIds);
      buildTree(noValueTree, content, noValueIds);
    } finally {
      updatingFromModel = false;
    }

    installListeners(trueTree,    VALUE_TRUE);
    installListeners(noValueTree, null);
  }

  // ── Tree building ─────────────────────────────────────────────────────────

  private void buildTree(@NonNull TreeView<FormNodeItem> tree,
      @Nullable FormModelContent content,
      @NonNull List<String> selectedIds) {

    CheckBoxTreeItem<FormNodeItem> invisibleRoot = new CheckBoxTreeItem<>();
    invisibleRoot.setExpanded(true);
    tree.setRoot(invisibleRoot);
    tree.setShowRoot(false);

    if (content == null) {
      return;
    }

    for (Screen screen : content.getScreens()) {
      CheckBoxTreeItem<FormNodeItem> screenItem =
          screenNode(new FormNodeItem(screen.getId(), nameOf(screen)));
      screenItem.setExpanded(true);

      for (ScreenElement child : screen.getScreenElements()) {
        CheckBoxTreeItem<FormNodeItem> childItem =
            selectableNode(new FormNodeItem(child.getId(), nameOf(child)),
                selectedIds.contains(child.getId()));

        // Show ControlGrid children of Sections, but nothing deeper.
        if (child instanceof Section section) {
          for (ScreenElement grandChild : section.getScreenElements()) {
            if (grandChild instanceof ControlGrid) {
              childItem.getChildren().add(
                  selectableNode(new FormNodeItem(grandChild.getId(), nameOf(grandChild)),
                      selectedIds.contains(grandChild.getId())));
            }
          }
          if (!childItem.getChildren().isEmpty()) {
            childItem.setExpanded(true);
          }
        }
        screenItem.getChildren().add(childItem);
      }
      invisibleRoot.getChildren().add(screenItem);
    }
  }

  /** Screen node: rendered with a non-functional checkbox (always unchecked, independent=false). */
  private static CheckBoxTreeItem<FormNodeItem> screenNode(@NonNull FormNodeItem item) {
    CheckBoxTreeItem<FormNodeItem> node = new CheckBoxTreeItem<>(item);
    node.setIndependent(false);
    return node;
  }

  /** Normal selectable node with a functional checkbox. */
  private static CheckBoxTreeItem<FormNodeItem> selectableNode(@NonNull FormNodeItem item,
      boolean selected) {
    CheckBoxTreeItem<FormNodeItem> node = new CheckBoxTreeItem<>(item);
    node.setIndependent(true);
    node.setSelected(selected);
    return node;
  }

  // ── Listener wiring ───────────────────────────────────────────────────────

  /**
   * Walks the tree (skipping the invisible root and the Screen-level nodes, which are not selectable)
   * and attaches a save listener to every selectable {@link CheckBoxTreeItem}.
   */
  private void installListeners(@NonNull TreeView<FormNodeItem> tree,
      @Nullable String masterValue) {
    if (tree.getRoot() == null) return;
    // Invisible root → screen items → selectable items
    for (TreeItem<FormNodeItem> screenItem : tree.getRoot().getChildren()) {
      for (TreeItem<FormNodeItem> child : screenItem.getChildren()) {
        attachListener(child, masterValue);
        // ControlGrid children of Sections
        for (TreeItem<FormNodeItem> grandChild : child.getChildren()) {
          attachListener(grandChild, masterValue);
        }
      }
    }
  }

  private void attachListener(@NonNull TreeItem<FormNodeItem> item,
      @Nullable String masterValue) {
    if (item instanceof CheckBoxTreeItem<FormNodeItem> cb && cb.isIndependent()) {
      cb.selectedProperty().addListener((obs, old, selected) -> {
        if (updatingFromModel) return;
        onSelectionChanged(masterValue, cb.getValue().id(), selected);
      });
    }
  }

  private void onSelectionChanged(@Nullable String masterValue,
      @NonNull String nodeId, boolean selected) {
    DependentCase dependentCase = findOrCreateCase(masterValue);
    List<String> nodes = dependentCase.getNotRelevantNodes();
    if (selected) {
      if (!nodes.contains(nodeId)) {
        nodes.add(nodeId);
      }
    } else {
      nodes.remove(nodeId);
    }
    commitChange();
  }

  // ── Model helpers ─────────────────────────────────────────────────────────

  @NonNull
  private List<String> loadNodeIds(@Nullable String masterValue) {
    if (content == null || control.getElementRef() == null) return List.of();
    FieldConfigEntry entry = findEntry();
    if (entry == null || entry.getDependentField() == null) return List.of();
    for (DependentCase c : entry.getDependentField().getCases()) {
      if (masterValuesEqual(c.getMasterValue(), masterValue)) {
        return new ArrayList<>(c.getNotRelevantNodes());
      }
    }
    return List.of();
  }

  @NonNull
  private DependentCase findOrCreateCase(@Nullable String masterValue) {
    if (content == null) return new DependentCase();
    if (content.getFieldConfiguration() == null) {
      content.setFieldConfiguration(new FieldConfiguration());
    }
    FieldConfigEntry entry = findOrCreateEntry();
    if (entry.getDependentField() == null) {
      DependentConfig cfg = new DependentConfig();
      cfg.setMasterField(control.getElementRef());
      entry.setDependentField(cfg);
    }
    for (DependentCase c : entry.getDependentField().getCases()) {
      if (masterValuesEqual(c.getMasterValue(), masterValue)) return c;
    }
    DependentCase newCase = new DependentCase();
    newCase.setMasterValue(masterValue);
    entry.getDependentField().getCases().add(newCase);
    return newCase;
  }

  @Nullable
  private FieldConfigEntry findEntry() {
    if (content == null || content.getFieldConfiguration() == null
        || control.getElementRef() == null) return null;
    for (FieldConfigEntry e : content.getFieldConfiguration().getField()) {
      if (control.getElementRef().equals(e.getElementRef())) return e;
    }
    return null;
  }

  @NonNull
  private FieldConfigEntry findOrCreateEntry() {
    return FieldConfigEntryHelper.findOrCreate(control, content);
  }

  private static boolean masterValuesEqual(@Nullable String a, @Nullable String b) {
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  private void commitChange() {
    ProjectItem item = Studio.getSelectedProjectItem();
    if (item == null) return;
    item.save();
    StudioEventManager.getInstance().fireModelSavedEvent(item);
  }

  // ── Display helpers ───────────────────────────────────────────────────────

  private static String nameOf(@NonNull Screen screen) {
    String n = screen.getName();
    return (n != null && !n.isBlank()) ? n : screen.getId();
  }

  private static String nameOf(@NonNull ScreenElement el) {
    String n = el.getName();
    return (n != null && !n.isBlank()) ? n : el.getId();
  }

  /**
   * Value object carried by each tree node: the node's id (stored in the model) and its display name.
   */
  public record FormNodeItem(String id, String displayName) {
    @Override public String toString() { return displayName; }
  }
}
