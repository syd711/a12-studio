package de.a12.studio.models.formmodel;

import de.a12.studio.models.ModelType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormModelLoadTest {

  private static final JsonMapper MAPPER = JsonMapper.builder()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  @Test
  void loadsCompanyFormModel() throws Exception {
    FormModel model = load("/formmodel/Company_FM.json");

    assertEquals("Company_FM", model.getId());
    assertEquals(ModelType.FORM, model.getModelType());
    assertEquals("37.4.0", model.getModelVersion());
    assertEquals(2, model.getAnnotations().size());
    assertEquals(3, model.getModelReferences().size());
    assertEquals("Company_DM", model.getModelReferences().get(0).getReference());

    assertNotNull(model.getContent());
    HeaderFooterBox subHeaderBox = model.getContent().getSubHeaderBox();
    assertEquals("subHeaderBox1", subHeaderBox.getId());
    Button navButton = subHeaderBox.getMajorButtons().getButton().get(0);
    NavigationButton navigationButton = assertInstanceOf(NavigationButton.class, navButton);
    assertEquals(ButtonType.NAVIGATION, navigationButton.getType());
    assertEquals("screen1", navigationButton.getTarget());
    MultilingualText label = assertInstanceOf(MultilingualText.class, navigationButton.getButtonStyling().getLabel());
    assertEquals("With Dualpane", label.getMultilingualText().getText().get(0).getText());

    Button saveButton = model.getContent().getFooterBox().getMajorButtons().getButton().get(1);
    EventButton eventButton = assertInstanceOf(EventButton.class, saveButton);
    assertEquals("event_submit", eventButton.getEvent());
    assertEquals("full", eventButton.getValidation());
    assertEquals("HIDDEN", eventButton.getEnablement());
    assertTrue(eventButton.getButtonStyling().getDestructive() == null || !eventButton.getButtonStyling().getDestructive());

    assertEquals(2, model.getContent().getScreens().size());
    Screen screen1 = model.getContent().getScreens().get(0);
    assertEquals("Screen1", screen1.getName());
    assertEquals(2, screen1.getScreenElements().size());

    Section companyData = (Section) screen1.getScreenElements().get(1);
    assertEquals("CompanyData", companyData.getName());
    assertEquals(4, companyData.getScreenElements().size());

    ControlGrid controlGrid = (ControlGrid) companyData.getScreenElements().get(0);
    assertEquals("6-6", controlGrid.getLayout().getLg());
    assertEquals("BOTTOM", controlGrid.getVerticalAlignment());
    assertEquals(2, controlGrid.getRow().size());
    Control control = (Control) controlGrid.getRow().get(0).getCell().get(0);
    assertEquals("field_67684", control.getElementRef());
    assertEquals(CellType.CONTROL, control.getType());

    CustomScreenElement customScreenElement = (CustomScreenElement) companyData.getScreenElements().get(1);
    assertEquals("PersonCompanyDualPane", customScreenElement.getName());

    Section addressSection = (Section) companyData.getScreenElements().get(2);
    InlineRepeat inlineRepeat = (InlineRepeat) addressSection.getScreenElements().get(0);
    assertEquals("group_1ec00", inlineRepeat.getGroupRef());
    assertEquals(5, inlineRepeat.getRepeatOverviewColumn().size());
    FieldBasedRepeatOverviewColumn column = (FieldBasedRepeatOverviewColumn) inlineRepeat.getRepeatOverviewColumn().get(0);
    assertEquals("field_94860", column.getElementRef());
    assertEquals("ASC", column.getPreferredSorting());
    assertTrue(inlineRepeat.getEnableAdd());
    assertTrue(inlineRepeat.getEnableRemove());

    // Re-serializing an already-loaded model must round-trip through the polymorphic types.
    String reserialized = MAPPER.writeValueAsString(model);
    FormModel reloaded = MAPPER.readValue(reserialized, FormModel.class);
    assertEquals("Company_FM", reloaded.getId());
    Control reloadedControl = (Control) ((ControlGrid) ((Section) reloaded.getContent().getScreens().get(0)
        .getScreenElements().get(1)).getScreenElements().get(0)).getRow().get(0).getCell().get(0);
    assertEquals("field_67684", reloadedControl.getElementRef());
  }

  @Test
  void loadsPersonFormModel() throws Exception {
    FormModel model = load("/formmodel/Person_FM.json");

    assertEquals("Person_FM", model.getId());
    Screen screen1 = model.getContent().getScreens().get(0);

    MultiColumnSection personalData = (MultiColumnSection) screen1.getScreenElements().get(0);
    assertEquals("PersonalData", personalData.getName());
    assertEquals("3-9", personalData.getLayout().getLg());
    assertEquals(2, personalData.getScreenElements().size());

    Section addressSection = (Section) screen1.getScreenElements().get(2);
    EmbeddedRepeat embeddedRepeat = (EmbeddedRepeat) addressSection.getScreenElements().get(0);
    assertEquals("G10", embeddedRepeat.getGroupRef());
    assertNotNull(embeddedRepeat.getControlGrid());
    assertEquals("6-6", embeddedRepeat.getControlGrid().getLayout().getLg());
    assertEquals("edit", embeddedRepeat.getDefaultRowAction().getEvent());

    Section educationSection = (Section) screen1.getScreenElements().get(4);
    DetachedRepeat detachedRepeat = (DetachedRepeat) educationSection.getScreenElements().get(0);
    assertEquals("G19", detachedRepeat.getGroupRef());
    assertNotNull(detachedRepeat.getDetailScreen());
    assertEquals("DetachedRepeatEducationDetailScreen", detachedRepeat.getDetailScreen().getName());

    Section membershipSection = (Section) screen1.getScreenElements().get(5);
    DetachedRepeat membershipRepeat = (DetachedRepeat) membershipSection.getScreenElements().get(0);
    assertEquals(150, membershipRepeat.getTableStyle().getTableHeight());
    assertTrue(membershipRepeat.getInfiniteScrolling());

    FieldConfigEntry pictureConfig = model.getContent().getFieldConfiguration().getField().get(0);
    assertEquals("group_05909", pictureConfig.getElementRef());
    MultilingualText pictureLabel = assertInstanceOf(MultilingualText.class, pictureConfig.getLabel());
    assertEquals("Profile picture", pictureLabel.getMultilingualText().getText().get(0).getText());

    GroupConfigEntry groupConfig = model.getContent().getGroupConfiguration().getGroup().get(0);
    assertEquals("G16", groupConfig.getGroupRef());
    assertEquals(2, groupConfig.getNumberOfInitialRows());

    assertEquals("Add", model.getContent().getDefaults().getButtonLabels().get("ADD").getText().get(0).getText());

    String reserialized = MAPPER.writeValueAsString(model);
    FormModel reloaded = MAPPER.readValue(reserialized, FormModel.class);
    assertEquals("Person_FM", reloaded.getId());
  }

  @Test
  void loadsInvoiceFormModel() throws Exception {
    FormModel model = load("/formmodel/Invoice_FM.json");

    assertEquals("static", model.getContent().getAmountSuffix().getType());
    assertEquals("€", model.getContent().getAmountSuffix().getValue());
    assertEquals(1, model.getContent().getStyles().size());
    assertEquals("h_rightAlign", model.getContent().getStyles().get(0).getName());
    assertEquals("INPUT", model.getContent().getReadonlyPresentation());

    Screen invoiceScreen = model.getContent().getScreens().get(0);
    assertEquals("headerfooter-e4974", invoiceScreen.getFooterBox().getId());
    Button summaryButton = invoiceScreen.getFooterBox().getMajorButtons().getButton().get(0);
    NavigationButton navigationButton = assertInstanceOf(NavigationButton.class, summaryButton);
    assertEquals("screen-14588", navigationButton.getTarget());
    assertEquals("list", navigationButton.getButtonStyling().getIcon().getName());

    // dependentField with an explicit null masterValue case must survive the round trip.
    FieldConfigEntry f107 = model.getContent().getFieldConfiguration().getField().get(0);
    assertEquals("F107", f107.getElementRef());
    assertNull(f107.getDependentField().getCases().get(0).getMasterValue());
    assertTrue(f107.getDependentField().getCases().get(0).getNotRelevant());
    assertEquals("invoice", f107.getDependentField().getCases().get(3).getMasterValue());
    assertTrue(f107.getDependentField().getCases().get(3).getReadonly());

    assertEquals("REMOVE", model.getContent().getDefaults().getConfirmationTexts().keySet().iterator().next());
    ConfirmationText removeConfirmation = model.getContent().getDefaults().getConfirmationTexts().get("REMOVE");
    assertEquals("Löschen bestätigen", removeConfirmation.getTitle().getText().get(0).getText());

    Row expressionRow = ((ControlGrid) model.getContent().getScreens().get(1).getScreenElements().stream()
        .filter(e -> "controlgrid_ef384".equals(e.getId()))
        .findFirst().orElseThrow()).getRow().get(0);
    ExpressionCell expressionCell = (ExpressionCell) expressionRow.getCell().get(0);
    assertTrue(expressionCell.getExpression().contains("kontext(Invoice)"));

    String reserialized = MAPPER.writeValueAsString(model);
    FormModel reloaded = MAPPER.readValue(reserialized, FormModel.class);
    assertNull(reloaded.getContent().getFieldConfiguration().getField().get(0).getDependentField().getCases().get(0).getMasterValue());
    assertEquals("static", reloaded.getContent().getAmountSuffix().getType());
  }

  @Test
  void loadsHideConditionAndDependentEnumerationFormModel() throws Exception {
    FormModel model = load("/formmodel/HideConditionAndDependentEnumeration_FM.json");

    Section section = (Section) model.getContent().getScreens().get(0).getScreenElements().get(0);
    HideCondition hideCondition = section.getHideCondition();
    assertEquals("field_master_enum", hideCondition.getMasterField());
    assertEquals(3, hideCondition.getCases().size());
    assertEquals("a", hideCondition.getCases().get(0).getMasterValue());
    assertEquals("b", hideCondition.getCases().get(1).getMasterValue());
    assertNull(hideCondition.getCases().get(2).getMasterValue());

    FieldConfigEntry dependentEnumEntry = model.getContent().getFieldConfiguration().getField().get(0);
    DependentEnumeration dependentEnumeration = dependentEnumEntry.getDependentEnumeration();
    assertEquals("field_master_enum", dependentEnumeration.getMasterField());
    assertEquals(2, dependentEnumeration.getConstraints().size());
    DependentEnumerationConstraint constraintA = dependentEnumeration.getConstraints().get(0);
    assertEquals("a", constraintA.getMasterValue());
    assertEquals("c", constraintA.getValueForMasterChange());
    assertEquals(3, constraintA.getConstraintValues().size());
    assertEquals("b", constraintA.getConstraintValues().get(0).getValue());
    assertNull(dependentEnumeration.getConstraints().get(1).getMasterValue());

    FieldConfigEntry externalEnumEntry = model.getContent().getFieldConfiguration().getField().get(1);
    ExternalEnumeration externalEnumeration = externalEnumEntry.getExternalEnumeration();
    assertEquals("https://invalid.example.com", externalEnumeration.getSrc());
    assertTrue(externalEnumeration.getCustomValuesAllowed());
    assertTrue(externalEnumeration.getCaseSensitive());

    FieldConfigEntry secretEntry = model.getContent().getFieldConfiguration().getField().get(2);
    assertTrue(secretEntry.getSecret());
    assertEquals("AREA", secretEntry.getFormatting());
    assertTrue(secretEntry.getEnableSelectAll());
    assertEquals(1, secretEntry.getAnnotations().size());

    // Multi-value hide conditions and dependent/external enumeration must survive the round trip, including
    // the explicit null masterValue cases.
    String reserialized = MAPPER.writeValueAsString(model);
    FormModel reloaded = MAPPER.readValue(reserialized, FormModel.class);
    Section reloadedSection = (Section) reloaded.getContent().getScreens().get(0).getScreenElements().get(0);
    assertEquals(3, reloadedSection.getHideCondition().getCases().size());
    assertNull(reloadedSection.getHideCondition().getCases().get(2).getMasterValue());
    assertNull(reloaded.getContent().getFieldConfiguration().getField().get(0)
        .getDependentEnumeration().getConstraints().get(1).getMasterValue());
  }

  @Test
  void loadsRepeatOverviewColumnsFormModel() throws Exception {
    FormModel model = load("/formmodel/RepeatOverviewColumns_FM.json");

    InlineRepeat repeat = (InlineRepeat) model.getContent().getScreens().get(0).getScreenElements().get(0);
    assertEquals(2, repeat.getRepeatOverviewColumn().size());

    ExpressionRepeatOverviewColumn expressionColumn =
        (ExpressionRepeatOverviewColumn) repeat.getRepeatOverviewColumn().get(0);
    assertEquals("expression1", expressionColumn.getName());
    assertEquals("\"Hallo\"", expressionColumn.getExpression());
    assertEquals(1, expressionColumn.getWidth());
    assertEquals("ASC", expressionColumn.getPreferredSorting());
    MultilingualText label = assertInstanceOf(MultilingualText.class, expressionColumn.getLabel());
    assertEquals("Expression", label.getMultilingualText().getText().get(0).getText());

    FieldBasedRepeatOverviewColumn fieldColumn =
        (FieldBasedRepeatOverviewColumn) repeat.getRepeatOverviewColumn().get(1);
    assertEquals("field_455f3", fieldColumn.getElementRef());
    assertEquals(1, fieldColumn.getWidth());

    String reserialized = MAPPER.writeValueAsString(model);
    FormModel reloaded = MAPPER.readValue(reserialized, FormModel.class);
    InlineRepeat reloadedRepeat = (InlineRepeat) reloaded.getContent().getScreens().get(0).getScreenElements().get(0);
    assertEquals("expression1",
        ((ExpressionRepeatOverviewColumn) reloadedRepeat.getRepeatOverviewColumn().get(0)).getName());
  }

  @Test
  void loadsRepeatFeaturesFormModel() throws Exception {
    FormModel model = load("/formmodel/RepeatFeatures_FM.json");

    InlineRepeat inlineRepeat = (InlineRepeat) model.getContent().getScreens().get(0).getScreenElements().get(0);
    assertEquals("field_status == \"active\"", inlineRepeat.getFilterExpression());
    assertEquals("column-1", inlineRepeat.getInitialSorting());
    assertTrue(inlineRepeat.getTitleHidden());
    assertEquals("Confirm removal",
        inlineRepeat.getConfirmationTexts().get("REMOVE").getTitle().getText().get(0).getText());
    assertEquals("group_attachments", inlineRepeat.getMultiFileUploadOptions().getElementRef());
    assertTrue(inlineRepeat.getMultiFileUploadOptions().getEnableDownload());

    RowAction rowAction = inlineRepeat.getRowActionGroup().getAction().get(0);
    assertEquals("custom", rowAction.getEvent());
    assertEquals("ALWAYS", rowAction.getScope());
    assertEquals("local_gas_station", rowAction.getButtonStyling().getIcon().getName());
    assertEquals("really?", rowAction.getConfirmation().getText().get(0).getText());

    EmbeddedRepeat embeddedRepeat = (EmbeddedRepeat) model.getContent().getScreens().get(0).getScreenElements().get(1);
    assertTrue(embeddedRepeat.getDefaultRowAction().getCustom());
    assertEquals("custom", embeddedRepeat.getDefaultRowAction().getEvent());

    String reserialized = MAPPER.writeValueAsString(model);
    FormModel reloaded = MAPPER.readValue(reserialized, FormModel.class);
    InlineRepeat reloadedRepeat = (InlineRepeat) reloaded.getContent().getScreens().get(0).getScreenElements().get(0);
    assertEquals("column-1", reloadedRepeat.getInitialSorting());
    assertEquals(1, reloadedRepeat.getRowActionGroup().getAction().size());
  }

  @Test
  void loadsButtonPanelAndCustomCellFormModel() throws Exception {
    FormModel model = load("/formmodel/ButtonPanelAndCustomCell_FM.json");

    ButtonPanel buttonPanel = (ButtonPanel) model.getContent().getScreens().get(0).getScreenElements().get(0);
    assertEquals("bp", buttonPanel.getName());
    assertEquals(1, buttonPanel.getButton().size());
    EventButton eventButton = assertInstanceOf(EventButton.class, buttonPanel.getButton().get(0));
    assertEquals("event_custom", eventButton.getEvent());

    ControlGrid grid = (ControlGrid) model.getContent().getScreens().get(0).getScreenElements().get(1);
    CustomCell customCell = (CustomCell) grid.getRow().get(0).getCell().get(0);
    assertEquals("custom-cell", customCell.getName());
    assertEquals(CellType.CUSTOM_CELL, customCell.getType());

    String reserialized = MAPPER.writeValueAsString(model);
    FormModel reloaded = MAPPER.readValue(reserialized, FormModel.class);
    ButtonPanel reloadedPanel = (ButtonPanel) reloaded.getContent().getScreens().get(0).getScreenElements().get(0);
    assertEquals(1, reloadedPanel.getButton().size());
    ControlGrid reloadedGrid = (ControlGrid) reloaded.getContent().getScreens().get(0).getScreenElements().get(1);
    assertInstanceOf(CustomCell.class, reloadedGrid.getRow().get(0).getCell().get(0));
  }

  @Test
  void loadsIncludesFormModel() throws Exception {
    FormModel model = load("/formmodel/Includes_FM.json");

    ControlGrid grid = (ControlGrid) model.getContent().getScreens().get(0).getScreenElements().get(0);
    assertEquals("controlgrid_130cc", grid.getIncludeId());
    assertEquals("IncludedModel", grid.getFormModelRef());
    assertEquals("/Person/address", grid.getHostDocumentModelPath());

    String reserialized = MAPPER.writeValueAsString(model);
    FormModel reloaded = MAPPER.readValue(reserialized, FormModel.class);
    ControlGrid reloadedGrid = (ControlGrid) reloaded.getContent().getScreens().get(0).getScreenElements().get(0);
    assertEquals("IncludedModel", reloadedGrid.getFormModelRef());
  }

  private FormModel load(String resource) throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream(resource)) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    return MAPPER.readValue(json, FormModel.class);
  }
}
