package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.overviewmodel.RowAction;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits the selected node type's {@code defaultRowAction} (SME "Default Row Action"): what a click on one of its
 * rows does. Without a custom row action the Tree Engine's own view/edit behavior applies and the key is absent;
 * with "Custom Row Action" it is {@code {"custom": true, "event": ...}}, the row firing that event - or, with no
 * event, not being interactive at all, like the Overview Model's row activation. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreeNodeRowActivationPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox customField;

  @FXML
  private Label customInfoIcon;

  @FXML
  private TextField eventField;

  private TreeNode node;

  // Set while the fields are being repopulated from the node, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(customInfoIcon, StudioBundle.get("tree_node_row_activation_panel.custom_info"));
    eventField.disableProperty().bind(customField.selectedProperty().not());

    customField.selectedProperty().addListener((observable, oldValue, custom) -> {
      if (updatingFromModel || node == null) {
        return;
      }
      if (custom) {
        RowAction rowAction = new RowAction();
        rowAction.setCustom(true);
        rowAction.setEvent(blankToNull(eventField.getText()));
        node.setDefaultRowAction(rowAction);
      }
      else {
        node.setDefaultRowAction(null);
      }
      commitHeaderChange();
    });

    eventField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || node == null || node.getDefaultRowAction() == null) {
        return;
      }
      node.getDefaultRowAction().setEvent(blankToNull(newValue));
      commitHeaderChange();
    });
  }

  /** Binds the panel to {@code node}, or to nothing ({@code null}). */
  public void setNode(TreeNode node) {
    this.node = node;
    updatingFromModel = true;
    try {
      RowAction rowAction = node != null ? node.getDefaultRowAction() : null;
      customField.setSelected(rowAction != null && Boolean.TRUE.equals(rowAction.getCustom()));
      eventField.setText(rowAction != null && rowAction.getEvent() != null ? rowAction.getEvent() : "");
    }
    finally {
      updatingFromModel = false;
    }
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
