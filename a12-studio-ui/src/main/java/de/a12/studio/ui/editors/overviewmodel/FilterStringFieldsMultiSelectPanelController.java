package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.EnumeratedStringFilter;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits {@link OverviewModel}'s {@code content.configuration.filterConfiguration.enumeratedStringFilter}: whether
 * string filter fields are shown as a paginated multi-select list instead of a plain text input, and (when
 * enabled) the page size of that list. Not bound to a single {@link de.a12.studio.models.documentmodel.Element},
 * so it follows the model-header pattern used by e.g. {@link PagingBehaviourPanelController}. The presence of
 * the {@code enumeratedStringFilter} object itself is the enabled flag, matching the reference implementation's
 * export behavior (an absent object means disabled; a present one is exported without its own {@code enabled}
 * key).
 */
public class FilterStringFieldsMultiSelectPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int DEFAULT_PAGING_SIZE = 10;

  @FXML
  private CheckBox enabledField;
  @FXML
  private VBox pagingSizeBox;
  @FXML
  private Spinner<Integer> pagingSizeField;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    pagingSizeField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_PAGING_SIZE));
    WidgetFactory.restrictToNumericInput(pagingSizeField.getEditor());

    pagingSizeBox.visibleProperty().bind(enabledField.selectedProperty());
    pagingSizeBox.managedProperty().bind(pagingSizeBox.visibleProperty());

    enabledField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (newValue) {
        EnumeratedStringFilter filter = new EnumeratedStringFilter();
        filter.setPagingSize(DEFAULT_PAGING_SIZE);
        ensureFilterConfiguration().setEnumeratedStringFilter(filter);
        updatingFromModel = true;
        try {
          pagingSizeField.getValueFactory().setValue(DEFAULT_PAGING_SIZE);
        }
        finally {
          updatingFromModel = false;
        }
      }
      else {
        ensureFilterConfiguration().setEnumeratedStringFilter(null);
      }
      commitHeaderChange();
    });
    pagingSizeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      EnumeratedStringFilter filter = ensureFilterConfiguration().getEnumeratedStringFilter();
      if (filter != null) {
        filter.setPagingSize(newValue);
        commitHeaderChange();
      }
    });
  }

  /** Irrelevant for {@link FilterConfiguration#FILTER_MODE_CUSTOM_FILTER} - hidden for that filter mode, see
   * {@link OverviewModelEditorController}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      EnumeratedStringFilter filter = currentFilter();
      boolean enabled = filter != null;
      enabledField.setSelected(enabled);
      pagingSizeField.getValueFactory().setValue(
          enabled && filter.getPagingSize() != null ? filter.getPagingSize() : DEFAULT_PAGING_SIZE);
    }
    finally {
      updatingFromModel = false;
    }
  }

  private EnumeratedStringFilter currentFilter() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    FilterConfiguration filterConfiguration = configuration != null ? configuration.getFilterConfiguration() : null;
    return filterConfiguration != null ? filterConfiguration.getEnumeratedStringFilter() : null;
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private FilterConfiguration ensureFilterConfiguration() {
    OverviewConfiguration configuration = ensureConfiguration();
    if (configuration.getFilterConfiguration() == null) {
      configuration.setFilterConfiguration(new FilterConfiguration());
    }
    return configuration.getFilterConfiguration();
  }
}
