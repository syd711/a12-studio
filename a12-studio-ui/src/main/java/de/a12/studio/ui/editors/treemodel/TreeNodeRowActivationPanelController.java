package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.RowActivation;
import de.a12.studio.models.treemodel.TreeEvents;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits the selected node type's {@code rowActivation} (SME "Row Activation", tree model 11.0.0): what a click on one of
 * its rows does. Without one the Tree Engine's own view/edit behavior applies and the key is absent ("Default"); the rows
 * can be non interactive, fire an Event (a built-in one behaves like the row action button, any other name is handed to
 * the application), or trigger an insert at a Position of a Document Model. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreeNodeRowActivationPanelController extends AbstractPropertyEditor implements Initializable {

  // The absent rowActivation; SME's own values are the RowActivation types.
  private static final String DEFAULT = "";

  private static final List<String> TYPES = Arrays.asList(DEFAULT, RowActivation.TYPE_NON_INTERACTIVE, RowActivation.TYPE_EVENT,
      RowActivation.TYPE_INSERT);
  private static final List<String> POSITIONS = List.of(TreeNodeAction.POSITION_AS_CHILD, TreeNodeAction.POSITION_ABOVE,
      TreeNodeAction.POSITION_BELOW);

  @FXML
  private ComboBox<String> typeField;

  @FXML
  private Label typeInfoIcon;

  @FXML
  private VBox eventBox;

  @FXML
  private ComboBox<String> eventField;

  @FXML
  private VBox positionBox;

  @FXML
  private ComboBox<String> positionField;

  @FXML
  private VBox documentModelBox;

  @FXML
  private ComboBox<String> documentModelField;

  private TreeModel model;
  private ProjectItem projectItem;
  private TreeNode node;

  // Set while the fields are being repopulated from the node, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(typeInfoIcon, StudioBundle.get("tree_node_row_activation_panel.type_info"));
    typeField.getItems().setAll(TYPES);
    typeField.setConverter(bundleConverter("tree_node_row_activation_panel.type_"));
    positionField.getItems().setAll(POSITIONS);
    positionField.setConverter(bundleConverter("tree_node_action.position_"));
    eventField.setEditable(true);

    typeField.valueProperty().addListener((observable, oldValue, type) -> {
      if (updatingFromModel || node == null || type == null) {
        return;
      }
      applyType(type);
      updateVisibility(type);
      commitHeaderChange();
    });
    eventField.getEditor().textProperty().addListener((observable, oldValue, text) -> {
      if (updatingFromModel || node == null || node.getRowActivation() == null) {
        return;
      }
      node.getRowActivation().setEvent(blankToNull(text));
      commitHeaderChange();
    });
    positionField.valueProperty().addListener((observable, oldValue, position) -> {
      if (updatingFromModel || node == null || node.getRowActivation() == null || position == null) {
        return;
      }
      node.getRowActivation().setPosition(position);
      refreshDocumentModelChoices();
      commitHeaderChange();
    });
    documentModelField.valueProperty().addListener((observable, oldValue, documentModel) -> {
      if (updatingFromModel || node == null || node.getRowActivation() == null) {
        return;
      }
      node.getRowActivation().setDocumentModelRef(blankToNull(documentModel));
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model, @NonNull ProjectItem projectItem) {
    this.model = model;
    this.projectItem = projectItem;
  }

  /** Binds the panel to {@code node}, or to nothing ({@code null}). */
  public void setNode(TreeNode node) {
    this.node = node;
    updatingFromModel = true;
    try {
      RowActivation activation = node != null ? node.getRowActivation() : null;
      String type = activation != null && activation.getType() != null ? activation.getType() : DEFAULT;
      typeField.setValue(type);

      List<String> events = new ArrayList<>(model != null && projectItem != null
          ? TreeEvents.candidates(TreeEvents.Context.ROW_ACTIVATION, TreeProjectModels.hasLinkDocumentModel(model, projectItem))
          : TreeEvents.ROW_ACTIVATION_EVENTS);
      if (activation != null && activation.getEvent() != null && !events.contains(activation.getEvent())) {
        events.add(activation.getEvent());
      }
      eventField.getItems().setAll(events);
      eventField.getEditor().setText(activation != null ? activation.getEvent() : null);
      positionField.setValue(activation != null ? activation.getPosition() : null);
      refreshDocumentModelChoices();
      updateVisibility(type);
    }
    finally {
      updatingFromModel = false;
    }
  }

  /** Switching the type starts from a fresh activation, so no stale event / insert settings are written. */
  private void applyType(String type) {
    if (DEFAULT.equals(type)) {
      node.setRowActivation(null);
      return;
    }
    RowActivation activation = new RowActivation();
    activation.setType(type);
    if (RowActivation.TYPE_EVENT.equals(type)) {
      activation.setEvent(blankToNull(eventField.getEditor().getText()));
    }
    node.setRowActivation(activation);
    if (RowActivation.TYPE_INSERT.equals(type)) {
      // SME requires a position for an insert; as child is the Tree Engine's default.
      activation.setPosition(TreeNodeAction.POSITION_AS_CHILD);
      updatingFromModel = true;
      try {
        positionField.setValue(TreeNodeAction.POSITION_AS_CHILD);
        refreshDocumentModelChoices();
      }
      finally {
        updatingFromModel = false;
      }
    }
  }

  private void updateVisibility(String type) {
    setShown(eventBox, RowActivation.TYPE_EVENT.equals(type));
    setShown(positionBox, RowActivation.TYPE_INSERT.equals(type));
    setShown(documentModelBox, RowActivation.TYPE_INSERT.equals(type));
  }

  private static void setShown(VBox box, boolean shown) {
    box.setVisible(shown);
    box.setManaged(shown);
  }

  /**
   * The Document Models an insert can create at the current Position - an empty choice for "the default one", and a stored
   * value that is no longer a candidate, so it stays visible and the validator reports it.
   */
  private void refreshDocumentModelChoices() {
    List<String> documentModels = new ArrayList<>();
    documentModels.add(null);
    RowActivation activation = node != null ? node.getRowActivation() : null;
    if (node != null && model != null && projectItem != null && activation != null) {
      documentModels.addAll(TreeActionContext.forNode(model, node).documentModelCandidates(projectItem, activation.getPosition()));
    }
    String current = activation != null ? activation.getDocumentModelRef() : null;
    if (current != null && !documentModels.contains(current)) {
      documentModels.add(current);
    }
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      documentModelField.getItems().setAll(documentModels);
      documentModelField.setValue(current);
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  private static StringConverter<String> bundleConverter(String keyPrefix) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? null : StudioBundle.get(keyPrefix + (value.isEmpty() ? "default" : value));
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
