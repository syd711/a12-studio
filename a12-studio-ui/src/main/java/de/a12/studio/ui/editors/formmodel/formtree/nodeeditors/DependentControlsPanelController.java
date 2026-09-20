package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.DependentCase;
import de.a12.studio.models.formmodel.DependentConfig;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.form.DependentControlSupport;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * "Dependencies" tab of a Control editor whose field can be a dependent-controls master - a Boolean, Confirm or
 * Enumeration field (SME's {@code isPossibleDependentControlMaster}). One section per value the master can take
 * ("(no value)" first, then e.g. {@code false}/{@code true} or the enumeration's values), each with a checkable
 * tree of the screen the Control is on; the blocks checked for a value are hidden while the master has it.
 *
 * <h3>Stored as</h3>
 * SME's wire shape, {@link Control#getDependentControls()}: {@code dependentControls.screenElement[]} of
 * {@code {idref, masterValue}} on the master Control itself, so a form authored here and one authored in SME are
 * interchangeable. Confirm controls used to be edited through a12-studio's own
 * {@link DependentCase#getNotRelevantNodes()} inside the field's {@code dependentField} (a shape SME and the Form
 * Engine do not know); such entries are shown here too and moved into {@code dependentControls} the first time
 * the selection is changed.
 *
 * <h3>Tree</h3>
 * The tree resembles the top-level screen; only Sections, Multi-Column Sections, Control Grids and Custom Screen
 * Elements that {@link DependentControlSupport#isCandidate} accepts get a checkbox, the branches around them are
 * shown without one, and branches without any candidate are left out (SME's {@code DependentControlsCandidatesCollector}).
 * Changing a selection rewrites the whole block from what is checked, which also drops entries that are no longer
 * selectable (a deleted element, an incompatible data context) - until then a warning lists them.
 */
public class DependentControlsPanelController {

  @FXML private VBox root;
  @FXML private Label staleLabel;
  @FXML private VBox sections;

  private Control control;
  private FormModelContent content;
  private ElementIndex elementIndex;

  /** Master value (null = "(no value)") -> the tree showing it; in the order the values are shown. */
  private final Map<String, TreeView<TreeNode>> treesByValue = new LinkedHashMap<>();

  /** Prevents programmatic tree population from triggering save listeners. */
  private boolean updatingFromModel;

  /**
   * Binds this panel to the given master control: rebuilds one section per possible master value and restores
   * the stored selection. Shows nothing if the Control's field cannot be a master.
   */
  public void setControl(@NonNull Control control, @Nullable FormModelContent content, @Nullable ElementIndex elementIndex) {
    this.control = control;
    this.content = content;
    this.elementIndex = elementIndex;

    sections.getChildren().clear();
    treesByValue.clear();
    List<String> masterValues = DependentControlSupport.masterValues(control, elementIndex);
    Screen screen = content == null ? null : DependentControlSupport.topLevelScreen(control, content).orElse(null);
    if (masterValues.isEmpty() || screen == null) {
      staleLabel.setVisible(false);
      staleLabel.setManaged(false);
      return;
    }

    Map<String, Set<String>> stored = storedSelection(masterValues);
    List<ScreenElement> candidates = DependentControlSupport.candidates(control, screen, content, elementIndex);
    Set<String> candidateIds = new LinkedHashSet<>();
    candidates.forEach(candidate -> candidateIds.add(candidate.getId()));

    updatingFromModel = true;
    try {
      for (String masterValue : masterValues) {
        Set<String> selectedIds = stored.get(masterValue);
        TreeView<TreeNode> tree = createTree(screen, candidateIds, selectedIds);
        treesByValue.put(masterValue, tree);
        TitledPane pane = new TitledPane(labelOf(masterValue), tree);
        pane.setExpanded(!selectedIds.isEmpty() || masterValues.size() <= 3);
        pane.setMaxWidth(Double.MAX_VALUE);
        sections.getChildren().add(pane);
      }
    }
    finally {
      updatingFromModel = false;
    }

    List<String> stale = staleEntries(stored, candidateIds);
    staleLabel.setText(StudioBundle.get("dependent_controls.stale_entries", String.join(", ", stale)));
    staleLabel.setVisible(!stale.isEmpty());
    staleLabel.setManaged(!stale.isEmpty());
  }

  // ── Stored state ──────────────────────────────────────────────────────────

  /**
   * What the Control has stored per master value: its {@code dependentControls} entries plus the legacy Confirm
   * {@code notRelevantNodes}. Entries for a value the field no longer has are left out (SME ignores them as well).
   */
  private Map<String, Set<String>> storedSelection(@NonNull List<String> masterValues) {
    Map<String, Set<String>> stored = new LinkedHashMap<>();
    masterValues.forEach(value -> stored.put(value, new LinkedHashSet<>()));
    if (control.getDependentControls() != null) {
      for (Control.DependentControls.Entry entry : control.getDependentControls().getScreenElement()) {
        Set<String> ids = stored.get(entry.getMasterValue());
        if (ids != null && entry.getIdref() != null) {
          ids.add(entry.getIdref());
        }
      }
    }
    DependentConfig legacy = legacyConfig();
    if (legacy != null) {
      for (DependentCase dependentCase : legacy.getCases()) {
        Set<String> ids = stored.get(dependentCase.getMasterValue());
        if (ids != null) {
          ids.addAll(dependentCase.getNotRelevantNodes());
        }
      }
    }
    return stored;
  }

  private static List<String> staleEntries(@NonNull Map<String, Set<String>> stored, @NonNull Set<String> candidateIds) {
    List<String> stale = new ArrayList<>();
    stored.values().forEach(ids -> ids.stream().filter(id -> !candidateIds.contains(id)).forEach(id -> {
      if (!stale.contains(id)) {
        stale.add(id);
      }
    }));
    return stale;
  }

  /** The {@code dependentField} of this Control's own field when it is the a12-studio-only Confirm dependency, else null. */
  @Nullable
  private DependentConfig legacyConfig() {
    FieldConfigEntry entry = findEntry();
    if (entry == null || entry.getDependentField() == null
        || !Objects.equals(control.getElementRef(), entry.getDependentField().getMasterField())) {
      return null;
    }
    boolean hasNodes = entry.getDependentField().getCases().stream().anyMatch(c -> !c.getNotRelevantNodes().isEmpty());
    return hasNodes ? entry.getDependentField() : null;
  }

  @Nullable
  private FieldConfigEntry findEntry() {
    if (content == null || content.getFieldConfiguration() == null || control.getElementRef() == null) {
      return null;
    }
    for (FieldConfigEntry entry : content.getFieldConfiguration().getField()) {
      if (control.getElementRef().equals(entry.getElementRef())) {
        return entry;
      }
    }
    return null;
  }

  // ── Tree building ─────────────────────────────────────────────────────────

  private TreeView<TreeNode> createTree(@NonNull Screen screen, @NonNull Set<String> candidateIds,
      @NonNull Set<String> selectedIds) {
    TreeView<TreeNode> tree = new TreeView<>();
    tree.setPrefHeight(200.0);
    tree.setShowRoot(false);
    tree.setCellFactory(tv -> new CheckBoxTreeCell<>() {
      @Override
      public void updateItem(TreeNode item, boolean empty) {
        super.updateItem(item, empty);
        setText(!empty && item != null ? item.displayName() : null);
        if (empty || item == null || !item.candidate()) {
          setGraphic(null);
        }
      }
    });

    CheckBoxTreeItem<TreeNode> invisibleRoot = new CheckBoxTreeItem<>(new TreeNode(null, null, false));
    invisibleRoot.setExpanded(true);
    tree.setRoot(invisibleRoot);
    CheckBoxTreeItem<TreeNode> screenItem = buildItem(screen, screen.getId(), nameOf(screen.getName(), screen.getId()),
        candidateIds, selectedIds);
    if (screenItem != null) {
      invisibleRoot.getChildren().add(screenItem);
    }
    return tree;
  }

  /** The item for {@code node} with everything below it, or null if neither it nor anything below is a candidate. */
  @Nullable
  private CheckBoxTreeItem<TreeNode> buildItem(@NonNull Object node, String id, String name,
      @NonNull Set<String> candidateIds, @NonNull Set<String> selectedIds) {
    boolean candidate = id != null && candidateIds.contains(id);
    CheckBoxTreeItem<TreeNode> item = new CheckBoxTreeItem<>(new TreeNode(id, name, candidate));
    item.setIndependent(true);
    item.setSelected(candidate && selectedIds.contains(id));
    for (ScreenElement child : childrenOf(node)) {
      CheckBoxTreeItem<TreeNode> childItem = buildItem(child, child.getId(), nameOf(child.getName(), child.getId()),
          candidateIds, selectedIds);
      if (childItem != null) {
        item.getChildren().add(childItem);
      }
    }
    if (!candidate && item.getChildren().isEmpty()) {
      return null;
    }
    item.setExpanded(true);
    if (candidate) {
      item.selectedProperty().addListener((obs, old, selected) -> {
        if (!updatingFromModel) {
          onSelectionChanged();
        }
      });
    }
    return item;
  }

  /**
   * The screen elements directly below {@code node} that can contain a candidate. Control Grids are candidates
   * themselves but hold only Controls, so the walk stops there.
   */
  private static List<ScreenElement> childrenOf(@NonNull Object node) {
    List<ScreenElement> children = new ArrayList<>();
    if (node instanceof Screen screen) {
      children.addAll(screen.getScreenElements());
    }
    else if (node instanceof Section section) {
      children.addAll(section.getScreenElements());
    }
    else if (node instanceof MultiColumnSection section) {
      children.addAll(section.getScreenElements());
    }
    else if (node instanceof EmbeddedRepeat repeat && repeat.getControlGrid() != null) {
      children.add(repeat.getControlGrid());
    }
    else if (node instanceof DetachedRepeat repeat && repeat.getDetailScreen() != null) {
      children.addAll(repeat.getDetailScreen().getScreenElements());
    }
    return children;
  }

  // ── Saving ────────────────────────────────────────────────────────────────

  /** Rewrites {@code dependentControls} from what is checked in every tree and drops the legacy Confirm nodes. */
  private void onSelectionChanged() {
    List<Control.DependentControls.Entry> entries = new ArrayList<>();
    for (Map.Entry<String, TreeView<TreeNode>> tree : treesByValue.entrySet()) {
      for (String id : checkedIds(tree.getValue().getRoot())) {
        Control.DependentControls.Entry entry = new Control.DependentControls.Entry();
        entry.setIdref(id);
        entry.setMasterValue(tree.getKey());
        entries.add(entry);
      }
    }
    if (entries.isEmpty()) {
      control.setDependentControls(null);
    }
    else {
      Control.DependentControls dependentControls = new Control.DependentControls();
      dependentControls.setScreenElement(entries);
      control.setDependentControls(dependentControls);
    }
    dropLegacyNodes();
    commitChange();
  }

  private static List<String> checkedIds(@Nullable TreeItem<TreeNode> item) {
    List<String> ids = new ArrayList<>();
    if (item == null) {
      return ids;
    }
    if (item instanceof CheckBoxTreeItem<TreeNode> checkBoxItem && checkBoxItem.isSelected()
        && item.getValue() != null && item.getValue().candidate() && !ids.contains(item.getValue().id())) {
      ids.add(item.getValue().id());
    }
    for (TreeItem<TreeNode> child : item.getChildren()) {
      checkedIds(child).stream().filter(id -> !ids.contains(id)).forEach(ids::add);
    }
    return ids;
  }

  /**
   * The Confirm tab used to keep its selection in the field's own {@code dependentField}. Everything it held is
   * now in {@code dependentControls}; leaving it would resurrect a deselected block on the next load. A case that
   * did nothing else, and the {@code dependentField} block this tab created around it, go with it.
   */
  private void dropLegacyNodes() {
    DependentConfig legacy = legacyConfig();
    FieldConfigEntry entry = findEntry();
    if (legacy == null || entry == null) {
      return;
    }
    for (DependentCase dependentCase : new ArrayList<>(legacy.getCases())) {
      dependentCase.getNotRelevantNodes().clear();
      if (!dependentCase.hasAction()) {
        legacy.getCases().remove(dependentCase);
      }
    }
    if (legacy.getCases().isEmpty()) {
      entry.setDependentField(null);
    }
  }

  private void commitChange() {
    ProjectItem item = Studio.getSelectedProjectItem();
    if (item == null) {
      return;
    }
    item.save();
    StudioEventManager.getInstance().fireModelSavedEvent(item);
  }

  // ── Display helpers ───────────────────────────────────────────────────────

  private static String labelOf(@Nullable String masterValue) {
    return masterValue == null ? StudioBundle.get("dependent_controls.no_value") : masterValue;
  }

  private static String nameOf(@Nullable String name, String id) {
    return name != null && !name.isBlank() ? name : id;
  }

  /**
   * Value object carried by each tree node: the element's id (stored in the model), its display name and whether
   * it can be checked. The invisible root has neither.
   */
  public record TreeNode(String id, String displayName, boolean candidate) {
    @Override
    public String toString() {
      return displayName;
    }
  }
}
