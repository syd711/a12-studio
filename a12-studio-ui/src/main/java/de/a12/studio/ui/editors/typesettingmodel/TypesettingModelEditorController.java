package de.a12.studio.ui.editors.typesettingmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.models.typesettingmodel.TypesettingModelContent;
import de.a12.studio.ui.editors.AbstractEditorController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

/**
 * Edits a {@link TypesettingModel}, mirroring SME's {@code TypesettingModelEditor} (the {@code print-typesetting}
 * library): the three "Prevent Line Break Rule" tables ({@link CharacterSequenceRulesPanelController}, {@link
 * NumberUnitRulesPanelController}, {@link SpecialPatternRulesPanelController}) and the orphan/widow limits
 * ({@link OrphanWidowPanelController}). SME's editor also lists the model's roles; here they live in the Model
 * Settings dialog, which shows nothing but the Roles panel for this model type (see {@code ModelSettingsDialog}).
 */
public class TypesettingModelEditorController extends AbstractEditorController {

  @FXML
  private CharacterSequenceRulesPanelController characterSequenceRulesPanelController;

  @FXML
  private NumberUnitRulesPanelController numberUnitRulesPanelController;

  @FXML
  private SpecialPatternRulesPanelController specialPatternRulesPanelController;

  @FXML
  private OrphanWidowPanelController orphanWidowPanelController;

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    TypesettingModel typesettingModel = (TypesettingModel) model;
    if (typesettingModel.getContent() == null) {
      typesettingModel.setContent(new TypesettingModelContent());
    }
    characterSequenceRulesPanelController.setModel(typesettingModel);
    numberUnitRulesPanelController.setModel(typesettingModel);
    specialPatternRulesPanelController.setModel(typesettingModel);
    orphanWidowPanelController.setModel(typesettingModel);
    updateSettingsErrorBadge();
  }

  @NonNull
  @Override
  public ModelType getModelType() {
    return ModelType.TYPESETTING;
  }
}
