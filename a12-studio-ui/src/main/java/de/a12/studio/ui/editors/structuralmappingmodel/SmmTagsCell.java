package de.a12.studio.ui.editors.structuralmappingmodel;

import de.a12.studio.models.structuralmappingmodel.SmmBlockTree;
import de.a12.studio.models.structuralmappingmodel.SmmElement;
import de.a12.studio.models.structuralmappingmodel.SmmNode;
import de.a12.studio.models.structuralmappingmodel.SmmPath;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

/**
 * The cell of one mapping block in a row of the target tree: the mapping tags of the block that end in that
 * element, side by side in the columns SME lays them out in (see {@link SmmBlockTree#tagsOfRow}). A field mapping
 * tag shows where the value comes from, a resolution strategy tag (with a plus for Fold, a magnifier for Slice)
 * how the repetition of the group is chosen, and the lookup field of a Slice carries a plain tag of its own.
 */
final class SmmTagsCell extends TreeTableCell<SmmElement, SmmElement> {

  /** What the cell needs from the editor. */
  interface Host {

    SmmBlockTree blockTree(int blockIndex);

    /** SME's label of a tag: the source path relative to that of the enclosing resolution strategy. */
    String label(SmmNode node);

    /** The label of the lookup field tag of a slice: the slice source field relative to the source group. */
    String sliceFieldLabel(SmmNode node);

    String tooltip(SmmNode node);

    String sliceFieldTooltip(SmmNode node);

    boolean hasProblem(SmmNode node);

    void edit(SmmNode node);

    void move(SmmNode node);

    void delete(SmmNode node);
  }

  static final double TAG_WIDTH = 200.0;

  static final double TAG_SPACING = 8.0;

  private final int blockIndex;

  private final Host host;

  SmmTagsCell(int blockIndex, Host host) {
    this.blockIndex = blockIndex;
    this.host = host;
  }

  @Override
  protected void updateItem(SmmElement element, boolean empty) {
    super.updateItem(element, empty);
    setText(null);
    if (empty || element == null) {
      setGraphic(null);
      return;
    }
    SmmBlockTree tree = host.blockTree(blockIndex);
    List<SmmNode> tags = tree == null ? List.of() : tree.tagsOfRow(SmmPath.parse(element.fullName()));
    if (tags.isEmpty()) {
      setGraphic(null);
      return;
    }
    HBox box = new HBox();
    box.setAlignment(Pos.CENTER_LEFT);
    for (SmmNode tag : tags) {
      box.getChildren().add(createTag(tag, element));
    }
    setGraphic(box);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
  }

  private Node createTag(SmmNode node, SmmElement row) {
    boolean sliceLookup = SmmBlockTree.isSliceLookupTarget(node, SmmPath.parse(row.fullName()));

    Label tag = new Label(sliceLookup ? host.sliceFieldLabel(node) : host.label(node));
    tag.getStyleClass().add("smm-tag");
    double width = node.span() * TAG_WIDTH + (node.span() - 1) * TAG_SPACING;
    tag.setMinWidth(width);
    tag.setPrefWidth(width);
    tag.setMaxWidth(width);
    HBox.setMargin(tag, new Insets(0, TAG_SPACING, 0, node.gap() * (TAG_WIDTH + TAG_SPACING)));

    if (sliceLookup) {
      tag.getStyleClass().add("smm-tag-slice-field");
      tag.setTooltip(WidgetFactory.createTooltip(host.sliceFieldTooltip(node)));
      tag.setOnMouseClicked(event -> {
        if (event.getButton() == MouseButton.PRIMARY) {
          host.edit(node);
        }
      });
      return tag;
    }

    if (node.isResolutionStrategy()) {
      tag.getStyleClass().add("smm-tag-strategy");
      FontIcon icon = WidgetFactory.createIcon(node.kind() == SmmNode.Kind.FOLD ? "mdi2p-plus-circle-outline" : "mdi2m-magnify");
      icon.getStyleClass().add("smm-tag-icon");
      tag.setGraphic(icon);
    }
    if (host.hasProblem(node)) {
      tag.getStyleClass().add("smm-tag-error");
    }
    tag.setTooltip(WidgetFactory.createTooltip(host.tooltip(node)));
    tag.setContextMenu(createMenu(node));
    if (node.isResolutionStrategy()) {
      tag.setOnMouseClicked(event -> {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
          host.edit(node);
        }
      });
    }
    return tag;
  }

  private ContextMenu createMenu(SmmNode node) {
    ContextMenu menu = new ContextMenu();
    MenuItem first = node.isFieldMapping()
        ? new MenuItem(StudioBundle.get("structural_mapping.tag_move"))
        : new MenuItem(StudioBundle.get("structural_mapping.tag_edit"));
    first.setMnemonicParsing(true);
    first.setOnAction(event -> {
      if (node.isFieldMapping()) {
        host.move(node);
      }
      else {
        host.edit(node);
      }
    });
    MenuItem delete = new MenuItem(StudioBundle.get("structural_mapping.tag_delete"));
    delete.setMnemonicParsing(true);
    delete.setOnAction(event -> host.delete(node));
    menu.getItems().addAll(first, delete);
    return menu;
  }
}
