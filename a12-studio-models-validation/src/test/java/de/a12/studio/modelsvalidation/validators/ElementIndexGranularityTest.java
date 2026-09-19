package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// SME's "granularity": the repeatable groups on the way to an element, attachment and multi-select groups
// excluded (documentModel/granularity.ts). Drives which Controls are "indexable" in the Form Model editor.
class ElementIndexGranularityTest {

  private static DocumentModel model(String id, String rootName, String elements) {
    return JsonSettings.objectMapper.readValue("""
        {"header": {"id": "%s", "modelType": "document", "modelVersion": "29.4.0"},
         "content": {"modelRoot": {"rootGroups": [
           {"type": "Group", "id": "root_%s", "name": "%s", "Group": {"repeatability": 1, "elements": [%s]}}
         ]}}}
        """.formatted(id, id, rootName, elements), DocumentModel.class);
  }

  private static String field(String id, String name) {
    return "{\"type\": \"Field\", \"id\": \"" + id + "\", \"name\": \"" + name
        + "\", \"Field\": {\"fieldType\": {\"type\": \"StringType\"}}}";
  }

  private static String group(String id, String name, int repeatability, String usageType, String... children) {
    return "{\"type\": \"Group\", \"id\": \"" + id + "\", \"name\": \"" + name + "\", \"Group\": {\"repeatability\": "
        + repeatability + (usageType == null ? "" : ", \"usageType\": \"" + usageType + "\"")
        + ", \"elements\": [" + String.join(",", children) + "]}}";
  }

  private static ElementIndex index() {
    DocumentModel included = model("Lines_DM", "Lines", group("g_items", "Items", 3, null, field("f_text", "Text")) + ","
        + field("f_total", "Total"));
    DocumentModel host = model("Company_DM", "Company",
        field("f_title", "Title") + ","
            + group("g_departments", "Departments", 10, null,
                field("f_dept", "Name"),
                group("g_employees", "Employees", 100, null, field("f_age", "Age"))) + ","
            + group("g_attachments", "Attachments", 10, "attachment", field("f_file", "File")) + ","
            + group("g_options", "Options", 5, "multi-select", field("f_option", "Option")) + ","
            + group("g_single", "Single", 1, null, field("f_single_field", "Field")) + ","
            + "{\"type\": \"Group\", \"id\": \"inc\", \"name\": \"Lines\", \"Group\": {\"repeatability\": 4, "
            + "\"includeConfig\": {\"reference\": \"Lines_DM\"}}}");
    return new ElementIndex(host, List.of(included));
  }

  @Test
  void aTopLevelFieldIsNotRepeated() {
    assertEquals(List.of(), index().granularity("f_title"));
  }

  @Test
  void aFieldInANonRepeatableGroupIsNotRepeated() {
    assertEquals(List.of(), index().granularity("f_single_field"));
  }

  @Test
  void aFieldCountsTheRepeatableGroupsAboveItOutermostFirst() {
    ElementIndex index = index();

    assertEquals(List.of("/Company/Departments"), index.granularity("f_dept"));
    assertEquals(List.of("/Company/Departments", "/Company/Departments/Employees"), index.granularity("f_age"));
  }

  @Test
  void aRepeatableGroupCountsItself() {
    assertEquals(List.of("/Company/Departments"), index().granularity("g_departments"));
    assertEquals(List.of("/Company/Departments", "/Company/Departments/Employees"), index().granularity("g_employees"));
  }

  @Test
  void attachmentAndMultiSelectGroupsAreOnlyTechnicallyRepeatable() {
    ElementIndex index = index();

    assertEquals(List.of(), index.granularity("f_file"));
    assertEquals(List.of(), index.granularity("g_attachments"));
    assertEquals(List.of(), index.granularity("f_option"));
  }

  @Test
  void anIncludeCountsAsTheRepeatableGroupItIsAndTheIncludedModelsOwnGroupsFollow() {
    ElementIndex index = index();

    assertEquals(List.of("/Company/Lines"), index.granularity("inc_f_total"));
    assertEquals(List.of("/Company/Lines", "/Company/Lines/Items"), index.granularity("inc_f_text"));
  }

  @Test
  void anUnknownOrMissingElementHasNoGranularity() {
    assertEquals(List.of(), index().granularity("nope"));
    assertEquals(List.of(), index().granularity(null));
  }
}
