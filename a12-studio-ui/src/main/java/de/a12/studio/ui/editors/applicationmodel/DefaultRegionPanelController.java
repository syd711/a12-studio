package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.application.ApplicationSceneGraphValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Debouncer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@link ApplicationModelContent#getDefaultRegion()}, the comma-separated region path applied when a
 * directive doesn't specify its own {@code region} (see {@link
 * de.a12.studio.models.applicationmodel.Directive#getRegion()}'s javadoc). Not bound to a single {@link
 * Element} (the field lives on the model's content), so it follows the model-header pattern used by e.g.
 * {@link RegionPanelController}. The field needs its own reference error, which {@link #bindTextField}
 * would clobber on every commit (it always clears the error container for header panels, see {@link
 * AbstractPropertyEditor#commitChange(javafx.scene.Node)}), so {@link #defaultRegionField} is wired
 * manually instead, following {@link RegionPanelController}.
 */
public class DefaultRegionPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TextField defaultRegionField;

  private ApplicationModel model;

  // Set while setModel() is repopulating defaultRegionField from the model, so the listener below doesn't
  // mistake that programmatic change for a user edit and write it straight back.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    defaultRegionField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      getContent().setDefaultRegion(splitRegion(newValue));
      refreshReferenceError();
      debouncer.debounce(defaultRegionField.getId(), this::commitHeaderChange, COMMIT_DEBOUNCE_MS, true);
    });
  }

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    updatingFromModel = true;
    try {
      defaultRegionField.setText(joinRegion(getContent().getDefaultRegion()));
    } finally {
      updatingFromModel = false;
    }
    refreshReferenceError();
  }

  private void refreshReferenceError() {
    List<ModelValidationError> errors = Studio.getValidationService()
        .validateElement(model, ApplicationSceneGraphValidator.DEFAULT_REGION_ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    } else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private ApplicationModelContent getContent() {
    ApplicationModelContent content = model.getContent();
    if (content == null) {
      content = new ApplicationModelContent();
      model.setContent(content);
    }
    return content;
  }

  private static String joinRegion(List<String> region) {
    return region == null ? "" : String.join(", ", region);
  }

  private static List<String> splitRegion(String text) {
    List<String> region = new ArrayList<>();
    if (text == null || text.isBlank()) {
      return region;
    }
    for (String part : text.split(",")) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        region.add(trimmed);
      }
    }
    return region;
  }
}
