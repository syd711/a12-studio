package de.a12.studio.ui.editors.structuralmappingmodel;

import de.a12.studio.models.structuralmappingmodel.SmmElement;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * One of the two trees of the Structural Mapping Model editor: the groups and fields of the source or the target
 * model, with SME's filters ("Mapped Elements", "Unmapped Elements") and a name search above it. What differs
 * between the two (extra columns, the badge in the name cell, the drag and drop behaviour) is set by the editor
 * through {@link #getTree()} and the setters. The tree is rebuilt on every {@link #refresh()}, keeping which groups
 * are expanded and what is selected.
 */
public class SmmTreePaneController {

  @FXML
  private VBox root;

  @FXML
  private Label titleLabel;

  @FXML
  private CheckBox mappedCheckBox;

  @FXML
  private CheckBox unmappedCheckBox;

  @FXML
  private TextField searchField;

  @FXML
  private TreeTableView<SmmElement> tree;

  @FXML
  private TreeTableColumn<SmmElement, SmmElement> nameColumn;

  private List<SmmElement> roots = List.of();

  private Supplier<Set<String>> mappedPaths = Set::of;

  // Text appended to an element name (the source tree shows how often a field is mapped).
  private Function<SmmElement, String> nameSuffix = element -> "";

  // A node shown after the name (the target tree shows error and warning icons); gets whether the row is expanded.
  private BiFunction<SmmElement, Boolean, Node> badge = (element, expanded) -> null;

  private Set<String> expandedPaths = new HashSet<>();

  private boolean firstBuild = true;

  @FXML
  private void initialize() {
    nameColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getValue()));
    nameColumn.setCellFactory(column -> new NameCell());
    mappedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> refresh());
    unmappedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> refresh());
    searchField.textProperty().addListener((observable, oldValue, newValue) -> refresh());
  }

  public VBox getRoot() {
    return root;
  }

  public TreeTableView<SmmElement> getTree() {
    return tree;
  }

  public void setTitle(@NonNull String title) {
    titleLabel.setText(title);
  }

  public void setMappedPaths(@NonNull Supplier<Set<String>> mappedPaths) {
    this.mappedPaths = mappedPaths;
  }

  public void setNameSuffix(@NonNull Function<SmmElement, String> nameSuffix) {
    this.nameSuffix = nameSuffix;
  }

  public void setBadge(@NonNull BiFunction<SmmElement, Boolean, Node> badge) {
    this.badge = badge;
  }

  /** Shows other groups and fields; everything is expanded the first time, later only what was expanded before. */
  public void setRoots(@NonNull List<SmmElement> roots) {
    this.roots = roots;
    firstBuild = true;
    expandedPaths.clear();
    refresh();
  }

  /** The element in the selected row, or {@code null}. */
  public SmmElement getSelectedElement() {
    TreeItem<SmmElement> selected = tree.getSelectionModel().getSelectedItem();
    return selected != null ? selected.getValue() : null;
  }

  /** Rebuilds the rows from the current mappings, filters and search, keeping expansion and selection. */
  public void refresh() {
    String selectedPath = getSelectedElement() != null ? getSelectedElement().fullName() : null;
    if (!firstBuild) {
      expandedPaths = collectExpanded(tree.getRoot());
    }

    TreeItem<SmmElement> newRoot = new TreeItem<>();
    Set<String> mapped = mappedPaths.get();
    String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
    for (SmmElement element : roots) {
      TreeItem<SmmElement> item = build(element, mapped, search);
      if (item != null) {
        newRoot.getChildren().add(item);
      }
    }
    newRoot.setExpanded(true);
    tree.setRoot(newRoot);
    firstBuild = false;

    if (selectedPath != null) {
      TreeItem<SmmElement> selected = find(newRoot, selectedPath);
      if (selected != null) {
        tree.getSelectionModel().select(selected);
      }
    }
  }

  private TreeItem<SmmElement> build(SmmElement element, Set<String> mapped, String search) {
    boolean matchesSearch = search.isEmpty() || element.name().toLowerCase().contains(search);
    boolean filterActive = !search.isEmpty() || !(mappedCheckBox.isSelected() && unmappedCheckBox.isSelected());
    if (!element.group()) {
      boolean stateShown = mapped.contains(element.fullName()) ? mappedCheckBox.isSelected() : unmappedCheckBox.isSelected();
      return stateShown && matchesSearch ? new TreeItem<>(element) : null;
    }

    TreeItem<SmmElement> item = new TreeItem<>(element);
    for (SmmElement child : element.children()) {
      TreeItem<SmmElement> childItem = build(child, mapped, search);
      if (childItem != null) {
        item.getChildren().add(childItem);
      }
    }
    boolean keepEmptyGroup = !filterActive || (!search.isEmpty() && matchesSearch && mappedCheckBox.isSelected() && unmappedCheckBox.isSelected());
    if (item.getChildren().isEmpty() && !keepEmptyGroup) {
      return null;
    }
    item.setExpanded(firstBuild || expandedPaths.contains(element.fullName()) || (!search.isEmpty() && !item.getChildren().isEmpty()));
    // The badge of a collapsed group sums up what is inside; it has to follow the expansion.
    item.expandedProperty().addListener((observable, oldValue, newValue) -> tree.refresh());
    return item;
  }

  private static Set<String> collectExpanded(TreeItem<SmmElement> item) {
    Set<String> expanded = new HashSet<>();
    if (item != null) {
      collectExpanded(item, expanded);
    }
    return expanded;
  }

  private static void collectExpanded(TreeItem<SmmElement> item, Set<String> expanded) {
    if (item.getValue() != null && item.isExpanded()) {
      expanded.add(item.getValue().fullName());
    }
    item.getChildren().forEach(child -> collectExpanded(child, expanded));
  }

  private static TreeItem<SmmElement> find(TreeItem<SmmElement> item, String fullName) {
    if (item.getValue() != null && item.getValue().fullName().equals(fullName)) {
      return item;
    }
    for (TreeItem<SmmElement> child : item.getChildren()) {
      TreeItem<SmmElement> found = find(child, fullName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /** Icon, name (with suffix) and badge of an element. */
  private final class NameCell extends TreeTableCell<SmmElement, SmmElement> {

    @Override
    protected void updateItem(SmmElement element, boolean empty) {
      super.updateItem(element, empty);
      if (empty || element == null) {
        setText(null);
        setGraphic(null);
        return;
      }
      Node icon = WidgetFactory.createIcon(element.group() ? Icons.ELEMENT_GROUP : Icons.ELEMENT_FIELD);
      icon.getStyleClass().add("tree-icon");
      Label name = new Label(element.name() + nameSuffix.apply(element));
      name.getStyleClass().add("tree-cell-name-label");
      HBox graphic = new HBox(4, icon, name);
      graphic.setAlignment(Pos.CENTER_LEFT);
      TreeItem<SmmElement> item = getTableRow() != null ? getTableRow().getTreeItem() : null;
      Node badgeNode = badge.apply(element, item == null || item.isExpanded() || item.isLeaf());
      if (badgeNode != null) {
        graphic.getChildren().add(badgeNode);
      }
      setText(null);
      setGraphic(graphic);
    }
  }
}
