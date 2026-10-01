package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.OverviewModelContent;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** SME's {@code getAllSuffixPaths}: a column's dynamic-suffix field is not offered by the filter-field pickers. */
class OverviewElementOptionsDynamicSuffixTest {

  private static FieldElement field(String id, String name) {
    FieldElement field = new FieldElement();
    field.setId(id);
    field.setName(name);
    FieldConfig config = new FieldConfig();
    config.setFieldType(new StringFieldType());
    field.setField(config);
    return field;
  }

  private static ElementIndex index() {
    GroupElement root = new GroupElement();
    root.setId("group_root");
    root.setName("Person");
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    config.setElements(new ArrayList<>(List.of(field("f_name", "Name"), field("f_unit", "Unit"))));
    root.setGroup(config);
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(List.of(root));
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(modelRoot);
    DocumentModel documentModel = new DocumentModel();
    documentModel.setId("Person_Dc");
    documentModel.setContent(content);
    return OverviewElementOptions.indexOf(documentModel, List.of(documentModel));
  }

  private static OverviewModel model(Boolean useDynamicSuffix, String suffixRef) {
    Column column = new Column();
    column.setUseDynamicSuffix(useDynamicSuffix);
    column.setSuffixRef(suffixRef);
    OverviewModel model = new OverviewModel();
    model.setContent(new OverviewModelContent());
    model.getContent().getColumns().add(column);
    return model;
  }

  @Test
  void aDynamicSuffixFieldIsExcluded() {
    ElementIndex index = index();
    Set<String> paths = OverviewElementOptions.dynamicSuffixPaths(index, model(true, "f_unit"));

    assertEquals(1, paths.size());
    List<String> ids = OverviewElementOptions.customSelectionFieldIds(index, paths);
    assertFalse(ids.contains("f_unit"));
    assertTrue(ids.contains("f_name"));
  }

  @Test
  void aSuffixRefWithoutTheDynamicFlagOrADanglingOneExcludesNothing() {
    ElementIndex index = index();

    assertTrue(OverviewElementOptions.dynamicSuffixPaths(index, model(false, "f_unit")).isEmpty());
    assertTrue(OverviewElementOptions.dynamicSuffixPaths(index, model(null, "f_unit")).isEmpty());
    assertTrue(OverviewElementOptions.dynamicSuffixPaths(index, model(true, "gone")).isEmpty());
    assertTrue(OverviewElementOptions.customSelectionFieldIds(index).contains("f_unit"));
  }
}
