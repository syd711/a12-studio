package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiFileUploadSupportTest {

  private final ElementIndex index = new ElementIndex(TestModels.load("/documentmodel/MultiFileUpload_DM.json", DocumentModel.class));

  private static List<String> ids(List<GroupElement> groups) {
    return groups.stream().map(GroupElement::getId).toList();
  }

  @Test
  void findsTheSingleNonRepeatableAttachmentGroup() {
    assertEquals(List.of("group_single_attachment"), ids(MultiFileUploadSupport.attachmentGroupCandidates(index, "group_single")));
    GroupElement single = MultiFileUploadSupport.singleAttachmentGroup(index, "group_single");
    assertNotNull(single);
    assertEquals("group_single_attachment", single.getId());
  }

  @Test
  void reportsNoCandidateWithoutAttachmentGroup() {
    assertTrue(MultiFileUploadSupport.attachmentGroupCandidates(index, "group_none").isEmpty());
    assertNull(MultiFileUploadSupport.singleAttachmentGroup(index, "group_none"));
  }

  @Test
  void reportsAllCandidatesWhenThereAreSeveral() {
    assertEquals(List.of("group_two_first", "group_two_second"), ids(MultiFileUploadSupport.attachmentGroupCandidates(index, "group_two")));
    assertNull(MultiFileUploadSupport.singleAttachmentGroup(index, "group_two"));
  }

  @Test
  void ignoresAttachmentGroupNestedInAnotherRepeatableGroup() {
    assertTrue(MultiFileUploadSupport.attachmentGroupCandidates(index, "group_nested").isEmpty());
  }

  @Test
  void ignoresRepeatableAttachmentGroup() {
    assertTrue(MultiFileUploadSupport.attachmentGroupCandidates(index, "group_repeatable_attachment").isEmpty());
  }

  @Test
  void toleratesMissingIndexOrUnresolvableGroup() {
    assertTrue(MultiFileUploadSupport.attachmentGroupCandidates(null, "group_single").isEmpty());
    assertTrue(MultiFileUploadSupport.attachmentGroupCandidates(index, null).isEmpty());
    assertTrue(MultiFileUploadSupport.attachmentGroupCandidates(index, "no_such_group").isEmpty());
    // A field is not a group.
    assertTrue(MultiFileUploadSupport.attachmentGroupCandidates(index, "field_title").isEmpty());
  }
}
