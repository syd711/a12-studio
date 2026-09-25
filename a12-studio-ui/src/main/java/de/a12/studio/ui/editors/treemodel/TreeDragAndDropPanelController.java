package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits a {@link TreeModel}'s {@code content.configuration.dnd} (SME "Drag And Drop" under Features). The key
 * is absent while drag and drop is disabled and {@code {"onDrag": {"expandHoveredNode": <bool>}}} otherwise;
 * "Expand On Hover" is that flag and is only editable while drag and drop is enabled (SME then defaults it to
 * on). Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header
 * pattern. Any other keys found in the {@code dnd} object are preserved.
 */
public class TreeDragAndDropPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String ON_DRAG = "onDrag";
  private static final String EXPAND_HOVERED_NODE = "expandHoveredNode";

  @FXML
  private CheckBox dragAndDropEnabledField;

  @FXML
  private CheckBox expandOnHoverField;

  @FXML
  private Label expandOnHoverInfoIcon;

  private TreeModel model;

  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(expandOnHoverInfoIcon, StudioBundle.get("tree_drag_and_drop_panel.expand_on_hover_info"));
    expandOnHoverField.disableProperty().bind(dragAndDropEnabledField.selectedProperty().not());

    dragAndDropEnabledField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (newValue) {
        Map<String, Object> dnd = new LinkedHashMap<>();
        dnd.put(ON_DRAG, new LinkedHashMap<>(Map.of(EXPAND_HOVERED_NODE, true)));
        ensureConfiguration().setDnd(dnd);
        updatingFromModel = true;
        try {
          expandOnHoverField.setSelected(true);
        }
        finally {
          updatingFromModel = false;
        }
      }
      else {
        ensureConfiguration().setDnd(null);
      }
      commitHeaderChange();
    });

    expandOnHoverField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null || ensureConfiguration().getDnd() == null) {
        return;
      }
      onDrag(ensureConfiguration().getDnd()).put(EXPAND_HOVERED_NODE, newValue);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      Map<String, Object> dnd = model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getDnd() : null;
      dragAndDropEnabledField.setSelected(dnd != null);
      // SME defaults Expand On Hover to on when the flag is missing.
      expandOnHoverField.setSelected(dnd != null && !Boolean.FALSE.equals(onDrag(dnd).get(EXPAND_HOVERED_NODE)));
    }
    finally {
      updatingFromModel = false;
    }
  }

  /** The {@code onDrag} object of {@code dnd}, created (and attached) if it isn't there yet. */
  @SuppressWarnings("unchecked")
  private static Map<String, Object> onDrag(Map<String, Object> dnd) {
    Object existing = dnd.get(ON_DRAG);
    if (existing instanceof Map<?, ?>) {
      return (Map<String, Object>) existing;
    }
    Map<String, Object> created = new LinkedHashMap<>();
    dnd.put(ON_DRAG, created);
    return created;
  }

  private TreeConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
