package de.a12.studio.models.util;

import de.a12.studio.models.overviewmodel.OverviewModel;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Gap 5 of "Overview Model: gap review" - renaming a Document Model used as a Custom Selection Of Fields
 * entry's Subtype ({@code FieldRef.subModel}) must follow along, like every other model reference {@link
 * ModelReferenceRewriter} rewrites. */
class ModelReferenceRewriterOverviewSubModelTest {

  private static final String OVERVIEW = """
      {"header": {"id": "Product_OM", "modelType": "overview", "modelVersion": "39.0.0", "modelReferences": [
         {"purpose": "document-model-for-overview", "modelType": "document", "alias": "DM1", "reference": "Product_DM"}]},
       "content": {
         "configuration": {"filterConfiguration": {"filterMode": "custom_list",
           "fields": [{"fieldId": "field-1", "subModel": "ProductBook_DM"}, {"fieldId": "field-2"}]}},
         "columns": [{"id": "column-1", "elementRef": "field-1", "width": 1}]}}
      """;

  @Test
  void aRenamedSubModelIsFollowedInTheCustomSelectionOfFields() {
    OverviewModel overview = JsonSettings.objectMapper.readValue(OVERVIEW, OverviewModel.class);

    boolean changed = ModelReferenceRewriter.rewriteReferences(overview, Map.of("ProductBook_DM", "ProductNovel_DM"));

    assertTrue(changed);
    assertEquals("ProductNovel_DM", overview.getContent().getConfiguration().getFilterConfiguration().getFields().get(0).getSubModel());
    assertEquals("field-2", overview.getContent().getConfiguration().getFilterConfiguration().getFields().get(1).getFieldId());
    assertEquals("Product_DM", overview.getModelReferences().get(0).getReference(), "the base Document Model reference is unaffected");
  }

  @Test
  void anUnrelatedRenameChangesNothing() {
    OverviewModel overview = JsonSettings.objectMapper.readValue(OVERVIEW, OverviewModel.class);

    assertFalse(ModelReferenceRewriter.rewriteReferences(overview, Map.of("Other_DM", "Renamed_DM")));
  }
}
