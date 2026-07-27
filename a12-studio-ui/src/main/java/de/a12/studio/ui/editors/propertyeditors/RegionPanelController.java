package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.application.ApplicationUniqueNamesValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
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
 * used by e.g. {@link LayoutPanelController}.
 */
public class RegionPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> REGIONS = List.of("APP", "CONTENT", "SIDEBAR", "MODAL");

  @FXML
  private ComboBox<String> regionCombo;

  private ApplicationModel model;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    regionCombo.getItems().addAll(REGIONS);

    bindComboBox(regionCombo, (element, value) -> {
      getOrCreateRegion().setName(value);
      refreshNameUniquenessError();
    });
  }

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    Region region = getRegion();
    setFieldValue(regionCombo, region != null ? region.getName() : null);
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
