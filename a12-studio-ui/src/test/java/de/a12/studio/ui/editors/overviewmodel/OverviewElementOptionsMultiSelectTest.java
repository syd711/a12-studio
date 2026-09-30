package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Gap 16 of "Overview Model: gap review" - a String multi-select group is not offered by the Column dialog's
 * Element Reference picker or the Custom Selection Of Fields/Section Data pickers (SME's {@code
 * isMultiSelect(element) ? isEnumerationMultiSelect(element) : true} and {@code isFieldLike(element) ||
 * isEnumerationMultiSelect(element, documentModel)} respectively); an Enumeration multi-select group is
 * offered by both, and a plain (non-multi-select) Group is offered by the Column picker but not by Custom
 * Selection Of Fields/Section Data.
 */
class OverviewElementOptionsMultiSelectTest {

  private static ElementIndex indexWith(GroupElement... extraGroups) {
    DocumentModel documentModel = new DocumentModel();
    documentModel.setId("Person_Dc");
    DocumentModelContent content = new DocumentModelContent();
    ModelRoot modelRoot = new ModelRoot();
    GroupElement root = new GroupElement();
    root.setId("group_root");
    root.setName("Person");
    GroupConfig rootConfig = new GroupConfig();
    rootConfig.setRepeatability(1);
    List<Object> elements = new ArrayList<>();
    elements.add(field("field_plain", "Name", new StringFieldType()));
    elements.addAll(List.of(extraGroups));
    rootConfig.setElements(cast(elements));
    root.setGroup(rootConfig);
    modelRoot.setRootGroups(List.of(root));
    content.setModelRoot(modelRoot);
    documentModel.setContent(content);
    return OverviewElementOptions.indexOf(documentModel, List.of(documentModel));
  }

  @SuppressWarnings("unchecked")
  private static <T> List<T> cast(List<?> list) {
    return (List<T>) list;
  }

  private static FieldElement field(String id, String name, de.a12.studio.models.documentmodel.FieldType type) {
    FieldElement field = new FieldElement();
    field.setId(id);
    field.setName(name);
    FieldConfig fieldConfig = new FieldConfig();
    fieldConfig.setFieldType(type);
    field.setField(fieldConfig);
    return field;
  }

  private static GroupElement multiSelectGroup(String id, de.a12.studio.models.documentmodel.FieldType valueType) {
    GroupElement group = new GroupElement();
    group.setId(id);
    group.setName(id);
    GroupConfig config = new GroupConfig();
    config.setRepeatability(999999);
    config.setUsageType(GroupConfig.USAGE_TYPE_MULTI_SELECT);
    config.setElements(List.of(field(id + "_value", "Value", valueType)));
    group.setGroup(config);
    return group;
  }

  private static GroupElement plainGroup(String id) {
    GroupElement group = new GroupElement();
    group.setId(id);
    group.setName(id);
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    config.setElements(List.of(field(id + "_child", "Child", new StringFieldType())));
    group.setGroup(config);
    return group;
  }

  @Test
  void enumerationMultiSelectIsOfferedByBothPickers() {
    GroupElement enumMultiSelect = multiSelectGroup("group_enum", new EnumerationFieldType());
    ElementIndex index = indexWith(enumMultiSelect);

    assertTrue(OverviewElementOptions.columnElementIds(index).contains("group_enum"));
    assertTrue(OverviewElementOptions.customSelectionFieldIds(index).contains("group_enum"));
  }

  @Test
  void stringMultiSelectIsOfferedByNeitherPicker() {
    GroupElement stringMultiSelect = multiSelectGroup("group_string", new StringFieldType());
    ElementIndex index = indexWith(stringMultiSelect);

    assertFalse(OverviewElementOptions.columnElementIds(index).contains("group_string"), "String multi-select");
    assertFalse(OverviewElementOptions.customSelectionFieldIds(index).contains("group_string"), "String multi-select");
  }

  @Test
  void aPlainGroupIsOfferedByTheColumnPickerButNotByCustomSelectionOfFields() {
    GroupElement plain = plainGroup("group_plain");
    ElementIndex index = indexWith(plain);

    assertTrue(OverviewElementOptions.columnElementIds(index).contains("group_plain"), "the Column picker allows any non-multi-select element");
    assertFalse(OverviewElementOptions.customSelectionFieldIds(index).contains("group_plain"), "not a field, not an enumeration multi-select");
  }

  @Test
  void aPlainFieldIsOfferedByBothPickers() {
    ElementIndex index = indexWith();

    assertTrue(OverviewElementOptions.columnElementIds(index).contains("field_plain"));
    assertTrue(OverviewElementOptions.customSelectionFieldIds(index).contains("field_plain"));
  }
}
