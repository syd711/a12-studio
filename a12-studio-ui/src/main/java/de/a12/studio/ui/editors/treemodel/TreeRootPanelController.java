package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits a {@link TreeModel}'s {@link TreeConfiguration#getRootRef()} (SME's "Root"): which relationship yields
 * the tree's root nodes. Like SME, the choices are the child relationship configurations defined on the node
 * types (a node's {@code childRelationshipConfigurations}), each shown as "&lt;node Document Model&gt; →
 * &lt;relationship model&gt;" - the value stored is the configuration's id. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern. The owning editor calls
 * {@link #refresh()} whenever the node types change, so the choices never go stale; a reference that no longer
 * resolves is kept (and reported in the error container) rather than silently dropped.
 */
public class TreeRootPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> rootField;

  private TreeModel model;

  // Set while rootField is being repopulated from the model, so that repopulation isn't mistaken for a user
  // edit and doesn't trigger a save.
  private boolean updatingFromModel;

  // Display text per child relationship configuration id, rebuilt by refresh(); read by the combo's converter.
  private Map<String, String> optionLabels = new LinkedHashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    rootField.setPromptText(StudioBundle.get("select_a_relationship"));
    rootField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String configurationId) {
        return configurationId == null ? null : optionLabels.getOrDefault(configurationId, configurationId);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    rootField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setRootRef(newValue);
      commitHeaderChange();
      refreshError();
    });
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;
    refresh();
  }

  /** Re-reads the choices from the nodes' child relationship configurations and re-selects the current root. */
  public void refresh() {
    if (model == null) {
      return;
    }
    optionLabels = new LinkedHashMap<>();
    for (TreeNode node : model.getContent().getNodes()) {
      for (Object configuration : node.getChildRelationshipConfigurations()) {
        if (configuration instanceof Map<?, ?> map && map.get("id") instanceof String id) {
          optionLabels.put(id, describe(node, map.get("relationshipModelRef")));
        }
      }
    }

    updatingFromModel = true;
    try {
      rootField.getItems().setAll(optionLabels.keySet());
      rootField.setValue(getRootRef());
    }
    finally {
      updatingFromModel = false;
    }
    refreshError();
  }

  /** The ids of the child relationship configurations defined on {@code node}. */
  static List<String> childRelationshipConfigurationIds(@NonNull TreeNode node) {
    List<String> ids = new ArrayList<>();
    for (Object configuration : node.getChildRelationshipConfigurations()) {
      if (configuration instanceof Map<?, ?> map && map.get("id") instanceof String id) {
        ids.add(id);
      }
    }
    return ids;
  }

  private static String describe(TreeNode node, Object relationshipModelRef) {
    String owner = node.getDocumentModelRef() != null ? node.getDocumentModelRef() : node.getId();
    return owner + " → " + (relationshipModelRef != null ? relationshipModelRef : "?");
  }

  private void refreshError() {
    String rootRef = getRootRef();
    if (rootRef != null && !optionLabels.containsKey(rootRef)) {
      showError("ERROR", StudioBundle.get("tree_root_invalid_reference", rootRef));
    }
    else {
      hideError();
    }
  }

  private String getRootRef() {
    return model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getRootRef() : null;
  }

  private TreeConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
