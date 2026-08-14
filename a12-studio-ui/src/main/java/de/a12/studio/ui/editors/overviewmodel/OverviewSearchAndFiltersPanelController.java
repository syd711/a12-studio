package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@link OverviewModel}'s "Search and Filters": full-text search and filter enablement, living on {@link
 * OverviewConfiguration} rather than a single {@link de.a12.studio.models.documentmodel.Element}, so it follows
 * the model-header pattern used by e.g. {@link de.a12.studio.ui.editors.maindetailmodel.FormWidthPanelController}.
 * {@code showFilterBar}/{@code showFilterButton}/{@code filterMode} live one level deeper, on {@link
 * FilterConfiguration} (matching SME's {@code omDocument.FilterConfiguration}), same nesting {@link
 * CustomSelectionOfFieldsPanelController} and {@link OverviewSectionDataPanelController} already edit. {@code
 * filterMode}'s five values (SME's {@code FilterMode} enum, plus {@link
 * FilterConfiguration#FILTER_MODE_CUSTOM_FILTER} - the platform docs' fifth option with no SME equivalent, see
 * {@link CustomFilterConfigurationPanelController}) are rendered via their {@code filter_mode_<value>} bundle
 * keys while the raw string is kept as the combo's value, matching {@link OverviewColumnOptions}'s
 * id-vs-display-label pattern. {@code filterMode} also governs which of the sibling Custom Selection Of
 * Fields/Section Data/Custom Filter Configuration panels are relevant, via {@link #setOnFilterModeChange}/{@link
 * #getFilterMode}, driven from {@link OverviewModelEditorController}. Row count lives on the Columns panel
 * instead ({@link OverviewColumnsPanelController}), next to the column list it's displayed alongside. Paging
 * (Behaviour/Size) is delegated to {@link PagingBehaviourPanelController}.
 */
public class OverviewSearchAndFiltersPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox showFullTextSearchField;

  @FXML
  private CheckBox enableFilterField;

  @FXML
  private CheckBox showFilterBarField;

  @FXML
  private CheckBox showFilterButtonField;

  @FXML
  private Label showFilterButtonInfoIcon;

  @FXML
  private ComboBox<String> filterModeField;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  // Notified after every change (including the initial setModel()) to filterMode, so the parent editor can
  // show/hide the sibling Custom Selection Of Fields/Section Data/Custom Filter Configuration panels that
  // filterMode governs the relevance of. See getFilterMode() for the value it should re-read.
  private Runnable onFilterModeChange = () -> { };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(showFilterButtonInfoIcon,
        StudioBundle.get("choosing_to_hide_this_button_only_applies_to_desktop_mode_the"));

    filterModeField.getItems().setAll(List.of(FilterConfiguration.FILTER_MODE_ALL_COLUMNS, FilterConfiguration.FILTER_MODE_ALL,
        FilterConfiguration.FILTER_MODE_ALL_WITH_META, FilterConfiguration.FILTER_MODE_CUSTOM_LIST, FilterConfiguration.FILTER_MODE_CUSTOM_FILTER));
    filterModeField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String filterMode) {
        return filterMode == null ? "" : StudioBundle.get("filter_mode_" + filterMode);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });

    showFullTextSearchField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setShowFullTextSearch(newValue);
      commitHeaderChange();
    });

    enableFilterField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setEnableFilter(newValue);
      commitHeaderChange();
    });

    showFilterBarField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureFilterConfiguration().setShowFilterBar(newValue);
      commitHeaderChange();
    });

    showFilterButtonField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureFilterConfiguration().setShowFilterButton(newValue);
      commitHeaderChange();
    });

    filterModeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureFilterConfiguration().setFilterMode(newValue);
      commitHeaderChange();
      onFilterModeChange.run();
    });
  }

  /**
   * @see #onFilterModeChange
   */
  public void setOnFilterModeChange(@NonNull Runnable onFilterModeChange) {
    this.onFilterModeChange = onFilterModeChange;
  }

  /**
   * The currently selected filter mode, defaulted to {@link FilterConfiguration#FILTER_MODE_ALL_COLUMNS} (the
   * combo's first entry) whenever the model has none set yet - see {@link #setModel}.
   */
  public String getFilterMode() {
    return filterModeField.getValue();
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      OverviewConfiguration configuration = model.getContent().getConfiguration();
      showFullTextSearchField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getShowFullTextSearch()));
      enableFilterField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getEnableFilter()));

      FilterConfiguration filterConfiguration = configuration != null ? configuration.getFilterConfiguration() : null;
      showFilterBarField.setSelected(filterConfiguration != null && Boolean.TRUE.equals(filterConfiguration.getShowFilterBar()));
      showFilterButtonField.setSelected(filterConfiguration != null && Boolean.TRUE.equals(filterConfiguration.getShowFilterButton()));
      String filterMode = filterConfiguration != null ? filterConfiguration.getFilterMode() : null;
      // Defaults the combo to its first entry when the model has no filterMode set yet, without writing that
      // default back until the user actually changes something themselves (updatingFromModel suppresses the
      // valueProperty listener above).
      filterModeField.setValue(filterMode != null ? filterMode : FilterConfiguration.FILTER_MODE_ALL_COLUMNS);
    }
    finally {
      updatingFromModel = false;
    }
    onFilterModeChange.run();
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
