package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.overview.OverviewMultiSelectionElementValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewSubHeaderElementValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.propertyeditors.AbstractMultiSelectionPanelController;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Edits an {@link OverviewModel}'s {@code content.configuration.multiSelection}; everything but where that
 * configuration lives is in {@link AbstractMultiSelectionPanelController}. Additionally shows the result of
 * {@link OverviewMultiSelectionElementValidator}, which also depends on whether a Multi-Selection element is
 * present in the Subheader.
 */
public class OverviewMultiSelectionPanelController extends AbstractMultiSelectionPanelController {

  private OverviewModel model;

  // Notified after a user toggle of the enabled checkbox (never from setModel()/loadFromModel() itself - see
  // OverviewSortingPanelController#refresh's own javadoc for why an initial load must not silently prune-and-
  // save), since disabling Multi-Selection can leave a now-disallowed Subheader element behind (gap 17 of
  // "Overview Model: gap review" - OverviewSubHeaderPruning removes it instead of merely flagging it).
  private Runnable onEnabledChange = () -> { };

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    loadFromModel();
    refreshValidationError();
  }

  /** Called by the owning editor whenever the Subheader panels change, since {@link
   * OverviewMultiSelectionElementValidator}'s result also depends on whether a Multi-Selection element is
   * present there. */
  public void refresh() {
    refreshValidationError();
  }

  /**
   * @see #onEnabledChange
   */
  public void setOnEnabledChange(@NonNull Runnable onEnabledChange) {
    this.onEnabledChange = onEnabledChange;
  }

  @Override
  protected boolean isModelLoaded() {
    return model != null;
  }

  @Override
  protected MultiSelectionConfig getMultiSelection() {
    return model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getMultiSelection() : null;
  }

  @Override
  protected MultiSelectionConfig createMultiSelection() {
    OverviewConfiguration configuration = ensureConfiguration();
    configuration.setMultiSelection(new MultiSelectionConfig());
    return configuration.getMultiSelection();
  }

  @Override
  protected void removeMultiSelection() {
    ensureConfiguration().setMultiSelection(null);
  }

  @Override
  protected void onEnabledChanged() {
    refreshValidationError();
    onEnabledChange.run();
  }

  private void refreshValidationError() {
    if (model == null) {
      return;
    }
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, OverviewMultiSelectionElementValidator.ELEMENT_ID);
    if (errors.isEmpty()) {
      errors = Studio.getValidationService().validateElement(model, OverviewSubHeaderElementValidator.MULTI_SELECTION_ELEMENT_ID);
    }
    if (errors.isEmpty()) {
      hideError();
    }
    else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
