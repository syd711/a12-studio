package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.overview.OverviewElementResolution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Gap 15 of the "Overview Model: gap review" - the kernel-injected {@code __meta} fields can be picked as a
 * Column's Element Reference and as a Custom Selection Of Fields entry. */
class OverviewElementOptionsMetaFieldsTest {

  private static final String CREATOR_ID = OverviewElementResolution.META_FIELDS.stream()
      .filter(field -> "Creator".equals(field.displayName())).findFirst().orElseThrow().id();

  private static ElementIndex plainIndex() {
    DocumentModel documentModel = new DocumentModel();
    documentModel.setId("Person_Dc");
    DocumentModelContent content = new DocumentModelContent();
    ModelRoot modelRoot = new ModelRoot();
    GroupElement root = new GroupElement();
    root.setId("group_root");
    root.setName("Person");
    GroupConfig rootConfig = new GroupConfig();
    rootConfig.setRepeatability(1);
    FieldElement field = new FieldElement();
    field.setId("field_first");
    field.setName("FirstName");
    FieldConfig fieldConfig = new FieldConfig();
    fieldConfig.setFieldType(new StringFieldType());
    field.setField(fieldConfig);
    rootConfig.setElements(List.of(field));
    root.setGroup(rootConfig);
    modelRoot.setRootGroups(List.of(root));
    content.setModelRoot(modelRoot);
    documentModel.setContent(content);
    return OverviewElementOptions.indexOf(documentModel, List.of(documentModel));
  }

  @Test
  void columnElementIdsIncludesTheSevenMetaFields() {
    ElementIndex index = plainIndex();
    List<String> ids = OverviewElementOptions.columnElementIds(index);

    assertTrue(ids.contains("field_first"));
    assertEquals(7, ids.stream().filter(OverviewElementResolution::isMetaFieldId).count());
    assertTrue(ids.contains(CREATOR_ID));
  }

  @Test
  void customSelectionFieldIdsIncludesTheSevenMetaFields() {
    List<String> ids = OverviewElementOptions.customSelectionFieldIds(plainIndex());

    assertEquals(7, ids.stream().filter(OverviewElementResolution::isMetaFieldId).count());
  }

  @Test
  void aMetaFieldDisplaysItsFriendlyNameAndIsNeverUnresolved() {
    ElementIndex index = plainIndex();

    assertEquals("__meta/Creator", OverviewElementOptions.displayPath(index, CREATOR_ID));
    assertTrue(OverviewElementOptions.isResolved(index, CREATOR_ID));
    assertTrue(OverviewElementOptions.isResolved(null, CREATOR_ID));
    assertFalse(OverviewElementOptions.isResolved(index, "field_does_not_exist"), "a real dangling ref is still flagged");
  }
}
