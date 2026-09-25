package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits a {@link TreeModel}'s {@link TreeConfiguration#getExpansionStrategy()}. The Hierarchical Column picker
 * that used to sit alongside this on the Configuration tab now lives on {@link TreeColumnsPanelController}
 * instead, matching SME's own placement (see {@code docs/modules/treeModel/0203_tree_columns.adoc}: Hierarchical
 * Column is the last step of the Columns section, not a separate configuration step). Not bound to a single
 * {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreeConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> EXPANSION_STRATEGIES = List.of(ExpansionStrategy.LEVEL_BY_LEVEL, ExpansionStrategy.TREE);

  @FXML
  private ComboBox<String> expansionStrategyField;

  private TreeModel model;

  private boolean updatingFromModel;

  // Told the strategy type on load and on every change, so the owning editor can show the strategy-specific
  // panels (Expansion Depths for "tree") only while they apply.
  private Consumer<String> onStrategyChange = type -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    expansionStrategyField.getItems().setAll(EXPANSION_STRATEGIES);
    expansionStrategyField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String type) {
        if (type == null) {
          return null;
        }
        return switch (type) {
          case ExpansionStrategy.LEVEL_BY_LEVEL -> StudioBundle.get("tree_configuration_panel.strategy_level_by_level");
          case ExpansionStrategy.TREE -> StudioBundle.get("tree_configuration_panel.strategy_tree");
          default -> type;
        };
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    expansionStrategyField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null || newValue == null) {
        return;
      }
      if (ensureConfiguration().getExpansionStrategy() == null) {
        ensureConfiguration().setExpansionStrategy(new ExpansionStrategy());
      }
      ensureConfiguration().getExpansionStrategy().setType(newValue);
      commitHeaderChange();
      onStrategyChange.accept(newValue);
    });
  }

  public void setOnStrategyChange(@NonNull Consumer<String> onStrategyChange) {
    this.onStrategyChange = onStrategyChange;
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      expansionStrategyField.setValue(model.getContent().getConfiguration() != null
          && model.getContent().getConfiguration().getExpansionStrategy() != null
          ? model.getContent().getConfiguration().getExpansionStrategy().getType()
          : null);
    }
    finally {
      updatingFromModel = false;
    }
    onStrategyChange.accept(expansionStrategyField.getValue());
  }

  private TreeConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
