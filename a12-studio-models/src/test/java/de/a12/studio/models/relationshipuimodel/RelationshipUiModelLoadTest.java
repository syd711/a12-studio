package de.a12.studio.models.relationshipuimodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.ModelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Fixtures copied verbatim from real models under testing/workspaces/advanced_new/models (see each test's
// comment for the source file), covering every componentType and the optional-field variance seen across them.
class RelationshipUiModelLoadTest {

  @Test
  void loadsTableListWithEditModal() throws Exception {
    // Teammembers_Ru.json
    RelationshipUiModel model = ModelRoundTrip.load(getClass(), "/relationshipuimodel/TableListWithEditModal_Ru.json", RelationshipUiModel.class);

    assertEquals("Teammembers_Ru", model.getId());
    assertEquals(ModelType.RELATIONSHIPUI, model.getModelType());
    assertEquals("2.0.0", model.getModelVersion());
    assertEquals(4, model.getModelReferences().size());

    RelationshipUiModelContent content = model.getContent();
    assertEquals("TeamPerson_Re", content.getRelationshipName());
    assertEquals("Person", content.getTargetRole());

    TableListComponent component = assertInstanceOf(TableListComponent.class, content.getComponent());
    assertEquals("Teammembers_Ru_SelectedItems_Ov", component.getSelectedItemsOverviewModel());
    assertEquals("TeamPerson_LinkFields_Fm", component.getLinkFormModel());
    assertEquals("400", component.getHeight());
    assertEquals(3, component.getButtons().size());
    assertEquals("event_open_edit_modal", component.getButtons().get(0).getEvent());
    assertTrue(component.getButtons().get(0).getLabelHidden());
    assertNull(component.getButtons().get(0).getDestructive());
    assertTrue(component.getButtons().get(1).getDestructive());

    EditConfiguration editConfiguration = component.getEditConfiguration();
    assertEquals("Teammembers_Ru_SelectedItems_Ov", editConfiguration.getSelectedItemsOverviewModel());
    assertEquals("Teammembers_Ru_AvailableItems_Ov", editConfiguration.getAvailableItemsOverviewModel());
    assertEquals("90%", editConfiguration.getDialogWidth());
    assertEquals("1200", editConfiguration.getDialogMaxWidth());
    assertNull(editConfiguration.getDialogMaxHeight());
  }

  @Test
  void loadsTableListReadOnly() throws Exception {
    // PersonSkills_Person_Ru.json - the minimal TableList shape: only the required overview model reference.
    RelationshipUiModel model = ModelRoundTrip.load(getClass(), "/relationshipuimodel/TableListReadOnly_Ru.json", RelationshipUiModel.class);

    TableListComponent component = assertInstanceOf(TableListComponent.class, model.getContent().getComponent());
    assertEquals("PersonSkills_Person_Ru_SelectedItems_Ov", component.getSelectedItemsOverviewModel());
    assertNull(component.getLinkFormModel());
    assertNull(component.getHeight());
    assertTrue(component.getButtons().isEmpty());
    assertNull(component.getEditConfiguration());
  }

  @Test
  void loadsDualPaneWithLinkForm() throws Exception {
    // Teammembers_DualPane_Ru.json
    RelationshipUiModel model = ModelRoundTrip.load(getClass(), "/relationshipuimodel/DualPaneWithLinkForm_Ru.json", RelationshipUiModel.class);

    DualPaneSelectionComponent component = assertInstanceOf(DualPaneSelectionComponent.class, model.getContent().getComponent());
    assertEquals("Teammembers_Ru_SelectedItems_Ov", component.getSelectedItemsOverviewModel());
    assertEquals("Teammembers_Ru_AvailableItems_Ov", component.getAvailableItemsOverviewModel());
    assertEquals("TeamPerson_LinkFields_Fm", component.getLinkFormModel());
    assertEquals("400", component.getHeight());
    assertTrue(component.getButtons().isEmpty());
  }

  @Test
  void loadsDualPaneMinimal() throws Exception {
    // CountryCity_City_Ru.json - only the two required overview model references, no link form/height/buttons.
    RelationshipUiModel model = ModelRoundTrip.load(getClass(), "/relationshipuimodel/DualPaneMinimal_Ru.json", RelationshipUiModel.class);

    DualPaneSelectionComponent component = assertInstanceOf(DualPaneSelectionComponent.class, model.getContent().getComponent());
    assertEquals("CountryCity_City_Ru_AvailableItems_Ov", component.getAvailableItemsOverviewModel());
    assertEquals("CountryCity_City_Ru_SelectedItems_Ov", component.getSelectedItemsOverviewModel());
    assertNull(component.getLinkFormModel());
  }

  @Test
  void loadsDropDownSelection() throws Exception {
    // ParentTeam_Ru.json
    RelationshipUiModel model = ModelRoundTrip.load(getClass(), "/relationshipuimodel/DropDownSelection_Ru.json", RelationshipUiModel.class);

    DropDownSelectionComponent component = assertInstanceOf(DropDownSelectionComponent.class, model.getContent().getComponent());
    assertEquals("ParentTeam_AvailableItems_Qe", component.getAvailableItemsQueryModel());
    assertEquals("ParentTeam_SelectedItem_Qe", component.getSelectedItemQueryModel());
    assertEquals("field_c9ad3", component.getElementRef());
    assertEquals("TeamTeam_Re", model.getContent().getRelationshipName());
    assertEquals("Parent", model.getContent().getTargetRole());
  }

  @Test
  void loadsTableListEditModalWithMissingPrimaryFlags() throws Exception {
    // PersonTeamAssignment_TableWithEditModal_Ru.json - every button omits "primary" entirely.
    RelationshipUiModel model = ModelRoundTrip.load(getClass(), "/relationshipuimodel/TableListEditModalNoDestructive_Ru.json", RelationshipUiModel.class);

    TableListComponent component = assertInstanceOf(TableListComponent.class, model.getContent().getComponent());
    assertEquals(3, component.getButtons().size());
    for (Button button : component.getButtons()) {
      assertNull(button.getPrimary());
    }
    assertFalse(component.getButtons().get(0).getDestructive());
  }

  @Test
  void roundTripsTableListWithEditModal() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/relationshipuimodel/TableListWithEditModal_Ru.json", RelationshipUiModel.class);
  }

  @Test
  void roundTripsTableListReadOnly() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/relationshipuimodel/TableListReadOnly_Ru.json", RelationshipUiModel.class);
  }

  @Test
  void roundTripsDualPaneWithLinkForm() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/relationshipuimodel/DualPaneWithLinkForm_Ru.json", RelationshipUiModel.class);
  }

  @Test
  void roundTripsDualPaneMinimal() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/relationshipuimodel/DualPaneMinimal_Ru.json", RelationshipUiModel.class);
  }

  @Test
  void roundTripsDropDownSelection() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/relationshipuimodel/DropDownSelection_Ru.json", RelationshipUiModel.class);
  }

  @Test
  void roundTripsTableListEditModalNoDestructive() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/relationshipuimodel/TableListEditModalNoDestructive_Ru.json", RelationshipUiModel.class);
  }
}
