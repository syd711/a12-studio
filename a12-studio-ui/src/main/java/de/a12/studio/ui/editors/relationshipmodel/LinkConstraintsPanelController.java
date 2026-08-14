package de.a12.studio.ui.editors.relationshipmodel;

import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.LinkConstraints;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits an {@link EntityCharacteristic}'s {@link LinkConstraints}: whether the multiplicity is unbounded, and
 * (only relevant when it isn't) the numeric upper limit. Embedded alongside {@link
 * EntityCharacteristicsPanelController} in the entity add/edit dialog opened from {@link
 * RelatedEntitiesPanelController}; see that class for why this follows the model-header binding pattern.
 */
public class LinkConstraintsPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox unboundedField;

  @FXML
  private Spinner<Integer> upperLimitField;

  private EntityCharacteristic entity;

  // Set while fields are being repopulated from the entity, so those programmatic updates aren't mistaken for
  // user edits and don't trigger a save.
  private boolean updatingFromModel;

  // Notified after every field edit, so the owning dialog can re-run its OK-button validation.
  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    upperLimitField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));
    WidgetFactory.restrictToNumericInput(upperLimitField.getEditor());

    unboundedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || entity == null) {
        return;
      }
      Multiplicity multiplicity = multiplicity();
      multiplicity.setUnbounded(newValue);
      multiplicity.setUpperLimit(newValue ? null : upperLimitField.getValue());
      upperLimitField.setDisable(newValue);
      onChange.run();
      commitHeaderChange();
    });

    upperLimitField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || entity == null) {
        return;
      }
      Multiplicity multiplicity = multiplicity();
      if (!Boolean.TRUE.equals(multiplicity.getUnbounded())) {
        multiplicity.setUpperLimit(newValue);
        onChange.run();
        commitHeaderChange();
      }
    });
  }

  public void setEntity(@NonNull EntityCharacteristic entity) {
    this.entity = entity;
    updatingFromModel = true;
    try {
      Multiplicity multiplicity = multiplicity();
      boolean unbounded = Boolean.TRUE.equals(multiplicity.getUnbounded());
      unboundedField.setSelected(unbounded);
      upperLimitField.setDisable(unbounded);
      upperLimitField.getValueFactory().setValue(multiplicity.getUpperLimit() != null ? multiplicity.getUpperLimit() : 1);
    }
    finally {
      updatingFromModel = false;
    }
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  private Multiplicity multiplicity() {
    if (entity.getLinkConstraints() == null) {
      entity.setLinkConstraints(new LinkConstraints());
    }
    if (entity.getLinkConstraints().getMultiplicity() == null) {
      entity.getLinkConstraints().setMultiplicity(new Multiplicity());
    }
    return entity.getLinkConstraints().getMultiplicity();
  }
}
