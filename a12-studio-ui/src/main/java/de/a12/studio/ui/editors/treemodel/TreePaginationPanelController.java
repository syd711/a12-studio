package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits {@link ExpansionStrategy#getPageSize()} - SME's "Pagination" section: the Tree Engine loads that many child
 * nodes per "Load more" instead of all of them. Only for the "Level by level" strategy (the owning editor toggles that
 * through {@link #setStrategyVisible}). Like SME, "Enable Pagination" isn't stored: it is on exactly when a page size is
 * present, and unchecking it drops the page size. The page size is prefilled with 10. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreePaginationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int DEFAULT_PAGE_SIZE = 10;

  @FXML
  private CheckBox paginationField;

  @FXML
  private Label infoIcon;

  @FXML
  private VBox pageSizeBox;

  @FXML
  private Spinner<Integer> pageSizeField;

  private TreeModel model;

  // Set while the fields are being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(infoIcon, StudioBundle.get("tree_pagination_panel.info"));
    pageSizeField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_PAGE_SIZE));
    WidgetFactory.restrictToNumericInput(pageSizeField.getEditor());

    paginationField.selectedProperty().addListener((observable, oldValue, enabled) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureStrategy().setPageSize(enabled ? Integer.valueOf(DEFAULT_PAGE_SIZE) : null);
      refresh();
      commitHeaderChange();
    });
    pageSizeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null || newValue == null || !paginationField.isSelected()) {
        return;
      }
      ensureStrategy().setPageSize(newValue);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;
    refresh();
  }

  /** Shows or hides the whole panel; it only applies to the "Level by level" expansion strategy. */
  public void setStrategyVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /** Re-reads the page size from the model, e.g. after the strategy changed. */
  public void refresh() {
    if (model == null) {
      return;
    }
    updatingFromModel = true;
    try {
      TreeConfiguration configuration = model.getContent().getConfiguration();
      Integer pageSize = configuration != null && configuration.getExpansionStrategy() != null
          ? configuration.getExpansionStrategy().getPageSize() : null;
      paginationField.setSelected(pageSize != null);
      pageSizeField.getValueFactory().setValue(pageSize != null ? pageSize : DEFAULT_PAGE_SIZE);
      pageSizeBox.setVisible(pageSize != null);
      pageSizeBox.setManaged(pageSize != null);
    }
    finally {
      updatingFromModel = false;
    }
  }

  private ExpansionStrategy ensureStrategy() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    ExpansionStrategy strategy = model.getContent().getConfiguration().getOrCreateExpansionStrategy();
    if (strategy.getType() == null) {
      // The strategy shown for a tree without one; this panel only exists for it.
      strategy.setType(ExpansionStrategy.LEVEL_BY_LEVEL);
    }
    return strategy;
  }
}
