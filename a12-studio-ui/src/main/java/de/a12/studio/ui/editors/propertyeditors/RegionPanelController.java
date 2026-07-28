package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.application.ApplicationUniqueNamesValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Debouncer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@link ApplicationModelContent#getRegion()}'s name, the application model's top-level region. Not bound
 * to a single {@link Element} (the region lives on the model's content), so it follows the model-header pattern
 * used by e.g. {@link LayoutPanelController}. The name needs its own uniqueness error, which {@link
 * #bindComboBox} would clobber on every commit (it always clears the error container for header panels, see
 * {@link AbstractPropertyEditor#commitChange(javafx.scene.Node)}), so {@link #regionCombo} is wired manually
 * instead, following {@link SupportedCharactersPanelController}.
 */
public class RegionPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> REGIONS = List.of("APP", "CONTENT", "SIDEBAR", "MODAL");

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<String> regionCombo;

  private ApplicationModel model;

  // Set while setModel() is repopulating regionCombo from the model, so the listener below doesn't mistake
  // that programmatic change for a user edit and write it straight back.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    regionCombo.getItems().addAll(REGIONS);

    regionCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      getOrCreateRegion().setName(newValue);
      refreshNameUniquenessError();
      debouncer.debounce(regionCombo.getId(), this::commitChange, COMMIT_DEBOUNCE_MS, true);
    });
  }

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    Region region = getRegion();
    updatingFromModel = true;
    try {
      regionCombo.setValue(region != null ? region.getName() : null);
    } finally {
      updatingFromModel = false;
    }
    refreshNameUniquenessError();
  }

  /**
   * Not bound to an {@link Element}, so the base class's element-keyed validation plumbing (which needs
   * {@code this.element} to be set) never runs for this panel; queries {@link
   * ApplicationUniqueNamesValidator}'s dedicated region-name element id directly instead. That check runs
   * against the whole region tree (root and every subregion share one uniqueness scope), so this can also
   * surface a clash between two subregions unrelated to the root region's own name.
   */
  private void refreshNameUniquenessError() {
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, ApplicationUniqueNamesValidator.REGION_ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    } else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private Region getRegion() {
    return model == null || model.getContent() == null ? null : model.getContent().getRegion();
  }

  private Region getOrCreateRegion() {
    ApplicationModelContent content = model.getContent();
    if (content == null) {
      content = new ApplicationModelContent();
      model.setContent(content);
    }
    Region region = content.getRegion();
    if (region == null) {
      region = new Region();
      content.setRegion(region);
    }
    return region;
  }
}
