package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.MultiSelectionElement;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.SearchElement;
import de.a12.studio.models.relationshipuimodel.DualPaneSelectionComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Gap 6 of the "Overview Model: gap review" - the Filter/Search/Multi-Selection Subheader element rules,
 * checked directly against every real condition in {@code OverviewMetaModel.json} (see the class javadoc of
 * {@link OverviewSubHeaderElementValidator}). */
class OverviewSubHeaderElementValidatorTest {

  @Test
  void reportsAFilterElementWhileFilterIsOff() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSubHeaderElementValidator_filterNotAllowed_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewSubHeaderElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Filter element is not allowed"));
  }

  @Test
  void reportsNoFilterElementAddedWhileFilterIsOn() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSubHeaderElementValidator_filterMissing_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewSubHeaderElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("No filter element is added"));
  }

  @Test
  void reportsDuplicateFilterElements() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSubHeaderElementValidator_filterDuplicate_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewSubHeaderElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Only one filter"));
  }

  @Test
  void acceptsAFilterElementInCustomFilterMode() {
    // SME's meta model has no Custom Filter concept - per the BA doc, a Custom Filter overview doesn't need
    // the Subheader element at all, so this whole check is skipped once newFilterConfiguration has content.
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSubHeaderElementValidator_filterNotAllowed_invalid.json", OverviewModel.class);
    de.a12.studio.models.overviewmodel.NewFilterConfiguration newFilterConfiguration = new de.a12.studio.models.overviewmodel.NewFilterConfiguration();
    de.a12.studio.models.overviewmodel.BooleanUserAccessOption invert = new de.a12.studio.models.overviewmodel.BooleanUserAccessOption();
    invert.setValue(true);
    newFilterConfiguration.setInvert(invert);
    model.getContent().getConfiguration().setNewFilterConfiguration(newFilterConfiguration);
    List<ModelValidationError> errors = new OverviewSubHeaderElementValidator().validate(model, TestModels.context(model));

    assertEquals(0, errors.size());
  }

  @Test
  void reportsASearchElementWhileSearchIsOff() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSubHeaderElementValidator_searchNotAllowed_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewSubHeaderElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Search element is not allowed"));
  }

  @Test
  void warnsInsteadOfErrorsForASearchElementOnAnAvailableItemsBindingOverview() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSubHeaderElementValidator_searchNotAllowed_invalid.json", OverviewModel.class);
    RelationshipUiModel relationshipUiModel = bindingModel(model.getId(), null);
    List<ModelValidationError> errors = new OverviewSubHeaderElementValidator().validate(model,
        TestModels.contextWithOtherModels(model, relationshipUiModel));

    assertEquals(1, errors.size());
    assertEquals("WARNING", errors.get(0).severity());
    assertTrue(errors.get(0).message().contains("not supported in Available Items"));
  }

  @Test
  void noRuleFiresForASearchElementOnASelectedItemsBindingOverview() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSubHeaderElementValidator_searchNotAllowed_invalid.json", OverviewModel.class);
    RelationshipUiModel relationshipUiModel = bindingModel(null, "OverviewSubHeaderElementValidator_searchNotAllowed_invalid");
    List<ModelValidationError> errors = new OverviewSubHeaderElementValidator().validate(model,
        TestModels.contextWithOtherModels(model, relationshipUiModel));

    assertEquals(0, errors.size());
  }

  @Test
  void reportsAMultiSelectionElementWhileMultiSelectionIsOff() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewSubHeaderElementValidator_multiSelectionNotAllowed_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewSubHeaderElementValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Multi-Selection element is not allowed"));
  }

  @Test
  void multiSelectionElementValidatorSkipsABindingOverview() {
    OverviewModel model = new OverviewModel();
    model.setId("Teammembers_AvailableItems_Ov");
    model.setContent(new de.a12.studio.models.overviewmodel.OverviewModelContent());
    de.a12.studio.models.overviewmodel.ElementBox subHeaderBox = de.a12.studio.models.overviewmodel.ElementBox.createEmpty();
    subHeaderBox.getLeftSlot().add(new MultiSelectionElement());
    model.getContent().setSubHeaderBox(subHeaderBox);
    model.getContent().setConfiguration(new de.a12.studio.models.overviewmodel.OverviewConfiguration());
    RelationshipUiModel relationshipUiModel = bindingModel("Teammembers_AvailableItems_Ov", null);
    List<ModelValidationError> errors = new OverviewMultiSelectionElementValidator().validate(model,
        TestModels.contextWithOtherModels(model, relationshipUiModel));

    assertEquals(0, errors.size());
  }

  @Test
  void searchElementValidatorSkipsABindingOverview() {
    OverviewModel model = new OverviewModel();
    model.setId("Teammembers_AvailableItems_Ov");
    model.setContent(new de.a12.studio.models.overviewmodel.OverviewModelContent());
    model.getContent().getConfiguration().setShowFullTextSearch(true);
    RelationshipUiModel relationshipUiModel = bindingModel("Teammembers_AvailableItems_Ov", null);
    List<ModelValidationError> errors = new OverviewSearchElementValidator().validate(model,
        TestModels.contextWithOtherModels(model, relationshipUiModel));

    assertEquals(0, errors.size());
  }

  private static RelationshipUiModel bindingModel(String availableItemsOverviewModel, String selectedItemsOverviewModel) {
    RelationshipUiModel relationshipUiModel = new RelationshipUiModel();
    relationshipUiModel.setId("Teammembers_Ru");
    RelationshipUiModelContent content = new RelationshipUiModelContent();
    DualPaneSelectionComponent dualPane = new DualPaneSelectionComponent();
    dualPane.setAvailableItemsOverviewModel(availableItemsOverviewModel);
    dualPane.setSelectedItemsOverviewModel(selectedItemsOverviewModel);
    content.setComponent(dualPane);
    relationshipUiModel.setContent(content);
    return relationshipUiModel;
  }
}
