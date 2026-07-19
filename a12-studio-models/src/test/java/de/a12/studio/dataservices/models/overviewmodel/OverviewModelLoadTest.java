package de.a12.studio.dataservices.models.overviewmodel;

import de.a12.studio.dataservices.models.Label;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.overviewmodel.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverviewModelLoadTest {

  private static final JsonMapper MAPPER = JsonMapper.builder()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  @Test
  void loadsCompanyOverviewModel() throws Exception {
    OverviewModel model = load("/overviewmodel/Company_OM.json");

    assertEquals("Company_OM", model.getId());
    assertEquals(ModelType.OVERVIEW, model.getModelType());
    assertEquals("38.3.0", model.getModelVersion());
    assertEquals("Companies", model.getLabels().get(0).getText());
    assertEquals("document-model-for-overview", model.getModelReferences().get(0).getPurpose());
    assertEquals("Company_DM", model.getModelReferences().get(0).getReference());

    OverviewModelContent content = model.getContent();
    assertNotNull(content);
    assertTrue(content.getConfiguration().getEnableFilter());
    assertEquals(10, content.getConfiguration().getPagingSize());

    FilterConfiguration filterConfiguration = content.getConfiguration().getFilterConfiguration();
    assertEquals(FilterConfiguration.FILTER_MODE_CUSTOM_LIST, filterConfiguration.getFilterMode());
    assertEquals(5, filterConfiguration.getFields().size());
    assertEquals("field_67684", filterConfiguration.getFields().get(0).getFieldId());
    assertEquals(3, filterConfiguration.getSectionData().size());
    assertEquals("Name", filterConfiguration.getSectionData().get(0).getLabel().get(0).getText());

    assertEquals(2, content.getSubHeaderBox().getMajorElements().size());
    assertInstanceOf(SearchElement.class, content.getSubHeaderBox().getMajorElements().get(0));
    assertInstanceOf(FilterElement.class, content.getSubHeaderBox().getMajorElements().get(1));

    ButtonElement newButton = assertInstanceOf(ButtonElement.class, content.getSubHeaderBox().getMinorElements().get(0));
    assertEquals(BoxElementType.BUTTON, newButton.getType());
    assertEquals("add", newButton.getEvent());
    assertEquals("add", newButton.getIcon().getName());
    assertEquals("New", newButton.getLabel().get(0).getText());

    assertEquals(3, content.getColumns().size());
    Column logoColumn = content.getColumns().get(0);
    assertEquals("Logo", logoColumn.getLabel().get(0).getText());
    assertEquals("group_8e00a", logoColumn.getElementRef());
    Column nameColumn = content.getColumns().get(1);
    assertEquals("field_67684", nameColumn.getElementRef());
    assertTrue(nameColumn.getSortable());
    assertEquals(Column.PREFERRED_SORTING_ASC, nameColumn.getPreferredSorting());

    assertEquals(1, content.getRowActionGroup().getActions().size());
    Button deleteAction = content.getRowActionGroup().getActions().get(0);
    assertEquals("event_delete", deleteAction.getEvent());
    Label confirmationTitle = deleteAction.getConfirmation().getTitle().get(0);
    assertEquals("Warning", confirmationTitle.getText());
  }

  @Test
  void loadsInvoiceOverviewModelWithMultiSelectionAndExpressionColumn() throws Exception {
    OverviewModel model = load("/overviewmodel/Invoice_OM.json");

    OverviewModelContent content = model.getContent();
    assertFalse(content.getConfiguration().getFilterConfiguration().getShowFilterButton() == null);
    assertEquals(FilterConfiguration.FILTER_MODE_ALL_COLUMNS, content.getConfiguration().getFilterConfiguration().getFilterMode());

    MultiSelectionConfig multiSelection = content.getConfiguration().getMultiSelection();
    assertEquals(MultiSelectionConfig.COLLAPSE_OPTION_COLLAPSIBLE_COLLAPSED, multiSelection.getCollapseOption());
    assertEquals(MultiSelectionConfig.COUNTER_OPTION_SIMPLE, multiSelection.getCounterOption());
    assertEquals(MultiSelectionConfig.SELECTION_AREA_CHECKBOX_AND_ROW, multiSelection.getSelectionArea());
    assertTrue(multiSelection.getClearConfirmation().getEnabled());
    Button deleteSelected = multiSelection.getButtons().get(0);
    assertEquals("delete_selected", deleteSelected.getEvent());
    assertTrue(deleteSelected.getDestructive());

    assertInstanceOf(MultiSelectionElement.class, content.getSubHeaderBox().getMinorElements().get(1));

    Column expressionColumn = content.getColumns().stream()
        .filter(c -> "ExpressionColumn".equals(c.getName()))
        .findFirst().orElseThrow();
    assertNotNull(expressionColumn.getExpression());
    assertEquals(Column.PIN_DIRECTION_RIGHT, expressionColumn.getPinDirection());

    Column finalPriceColumn = content.getColumns().stream()
        .filter(c -> "include_d7e3e_field_0680a".equals(c.getElementRef()))
        .findFirst().orElseThrow();
    assertEquals("€", finalPriceColumn.getSuffix().get(0).getText());
    assertEquals(SummaryConfig.OPERATION_SUM, finalPriceColumn.getSummary().get(0).getOperation());
  }

  @Test
  void loadsPersonOverviewModelAlignmentAndAttachmentColumn() throws Exception {
    OverviewModel model = load("/overviewmodel/Person_OM.json");

    OverviewModelContent content = model.getContent();
    assertEquals(6, content.getConfiguration().getFilterConfiguration().getSectionData().size());

    Column pictureColumn = content.getColumns().get(0);
    assertEquals("group_05909", pictureColumn.getElementRef());
    assertEquals(Column.ATTACHMENT_DISPLAY_MODE_PREVIEW, pictureColumn.getAttachmentDisplayMode());
    assertTrue(pictureColumn.getFixedWidth());

    Column nameColumn = content.getColumns().get(1);
    assertEquals("center", nameColumn.getAlignment().getHeader().getHorizontal());
    assertEquals("bottom", nameColumn.getAlignment().getContent().getVertical());
  }

  private OverviewModel load(String resourcePath) throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    return MAPPER.readValue(json, OverviewModel.class);
  }
}
