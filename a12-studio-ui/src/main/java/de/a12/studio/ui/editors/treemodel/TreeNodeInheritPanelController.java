package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeInheritance;
import de.a12.studio.models.treemodel.TreeNodeInheritance.Part;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits which parts of the selected node type it inherits from its super type node (SME "Inherit From Supertype"):
 * one checkbox per {@link Part}. Only offered while there is something to inherit from, see {@link #setNode}. Like
 * SME, checking a part clears what the node defined itself for it - after asking, if that is anything - and {@link
 * #setOnInheritanceChange} lets the owner hide the editor of every inherited part. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreeNodeInheritPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox columnsField;
  @FXML
  private CheckBox childRelationshipConfigurationsField;
  @FXML
  private CheckBox iconField;
  @FXML
  private CheckBox actionsField;
  @FXML
  private CheckBox contextMenuField;
  @FXML
  private CheckBox defaultRowActionField;
  @FXML
  private CheckBox rowTitleField;
  @FXML
  private CheckBox stylesField;

  private final Map<Part, CheckBox> fields = new EnumMap<>(Part.class);

  private TreeNode node;

  // Set while the checkboxes are being repopulated from the node, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  // Told after a part was (un)marked as inherited and its content cleared.
  private Consumer<Part> onInheritanceChange = part -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    fields.put(Part.COLUMNS, columnsField);
    fields.put(Part.CHILD_RELATIONSHIP_CONFIGURATIONS, childRelationshipConfigurationsField);
    fields.put(Part.ICON, iconField);
    fields.put(Part.ACTIONS, actionsField);
    fields.put(Part.CONTEXT_MENU, contextMenuField);
    fields.put(Part.DEFAULT_ROW_ACTION, defaultRowActionField);
    fields.put(Part.ROW_TITLE, rowTitleField);
    fields.put(Part.STYLES, stylesField);
    fields.forEach((part, field) -> field.selectedProperty().addListener((observable, oldValue, inherited) -> {
      if (!updatingFromModel && node != null) {
        onToggle(part, field, inherited);
      }
    }));
  }

  public void setOnInheritanceChange(Consumer<Part> onInheritanceChange) {
    this.onInheritanceChange = onInheritanceChange;
  }

  /**
   * Binds the panel to {@code node}, or to nothing ({@code null}); {@code available} is whether the node has a super
   * type node to inherit from. A node that already inherits something keeps showing the panel either way, so that can
   * still be undone.
   */
  public void setNode(TreeNode node, boolean available) {
    this.node = node;
    updatingFromModel = true;
    try {
      for (Part part : Part.values()) {
        fields.get(part).setSelected(node != null && TreeNodeInheritance.isInherited(node, part));
      }
    }
    finally {
      updatingFromModel = false;
    }
    setEditorVisible(node != null && (available || TreeNodeInheritance.hasInheritedConfig(node)));
  }

  private void onToggle(Part part, CheckBox field, boolean inherited) {
    if (inherited && TreeNodeInheritance.hasContent(node, part)) {
      Optional<ButtonType> answer = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("tree_node_inherit_panel.confirm_clear", field.getText()),
          null, null, StudioBundle.get("ok"));
      if (answer.isEmpty() || answer.get() != ButtonType.OK) {
        updatingFromModel = true;
        try {
          field.setSelected(false);
        }
        finally {
          updatingFromModel = false;
        }
        return;
      }
    }
    TreeNodeInheritance.setInherited(node, part, inherited);
    if (inherited) {
      TreeNodeInheritance.clearContent(node, part);
    }
    commitHeaderChange();
    onInheritanceChange.accept(part);
  }
}
