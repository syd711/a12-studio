package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits {@link OverviewModel}'s "Features": full-text search, living on {@link OverviewConfiguration} rather
 * than a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern
 * used by e.g. {@link de.a12.studio.ui.editors.maindetailmodel.FormWidthPanelController}. Row count lives on
 * the Columns panel instead ({@link OverviewColumnsPanelController}), next to the column list it's displayed
 * alongside. Paging (Behaviour/Size) is delegated to {@link PagingBehaviourPanelController}.
 */
public class OverviewFeaturesPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox showFullTextSearchField;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    showFullTextSearchField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setShowFullTextSearch(newValue);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      OverviewConfiguration configuration = model.getContent().getConfiguration();
      showFullTextSearchField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getShowFullTextSearch()));
    }
    finally {
      updatingFromModel = false;
    }
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
