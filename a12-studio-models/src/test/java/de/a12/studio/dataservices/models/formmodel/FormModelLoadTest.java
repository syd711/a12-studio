package de.a12.studio.dataservices.models.formmodel;

import de.a12.studio.dataservices.models.ModelType;
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

  private FormModel load(String resource) throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream(resource)) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    return MAPPER.readValue(json, FormModel.class);
  }
}
