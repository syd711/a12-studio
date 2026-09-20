package de.a12.studio.modelsvalidation.formincludes;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.ProjectReferenceRefactoring;
import de.a12.studio.modelsvalidation.refactoring.ProjectReferenceRefactoring.ModelEdits;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A rename or move in the host's Document Model has to carry the {@code hostDocumentModelPath} of the includes of a
 * form bound to it along (SME's include example, see {@link FormIncludeExpanderTest}: the include group {@code address}
 * at {@code /Person/address}).
 */
class FormIncludeRefactoringTest {

  private final DocumentModel hostDm = TestModels.load("/formincludes/A-for-host.json", DocumentModel.class);
  private final DocumentModel includedDm = TestModels.load("/formincludes/B-for-include.json", DocumentModel.class);
  private final FormModel hostForm = TestModels.load("/formincludes/HostModel_expanded.json", FormModel.class);
  private final FormModel includedForm = TestModels.load("/formincludes/IncludedModel.json", FormModel.class);

  private GroupElement person() {
    return hostDm.getContent().getModelRoot().getRootGroups().get(0);
  }

  private Element personChild(String name) {
    return person().getGroup().getElements().stream().filter(element -> name.equals(element.getName())).findFirst().orElseThrow();
  }

  private ScreenElement includedElement() {
    return FormModelWalker.find(hostForm.getContent(), ScreenElement.class).stream()
        .filter(element -> element.getIncludeId() != null).findFirst().orElseThrow();
  }

  /** Runs {@code change} on the host DM and returns (without applying) the edits it makes to the project's forms. */
  private List<ModelEdits> edits(Runnable change) {
    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(hostDm);
    change.run();
    return ProjectReferenceRefactoring.computeEdits(hostDm, plan, List.<A12Model<?>>of(hostDm, includedDm, hostForm, includedForm));
  }

  @Test
  void renamingTheIncludeGroupCarriesTheIncludesPathAlong() {
    List<ModelEdits> edits = edits(() -> personChild("address").setName("location"));

    assertEquals(1, edits.size());
    assertEquals(hostForm, edits.get(0).model());
    edits.get(0).edits().forEach(Edit::apply);
    assertEquals("/Person/location", includedElement().getHostDocumentModelPath());
  }

  @Test
  void renamingAGroupAboveTheIncludeGroupCarriesItAlongToo() {
    List<ModelEdits> edits = edits(() -> person().setName("Human"));

    edits.forEach(modelEdits -> modelEdits.edits().forEach(Edit::apply));

    assertEquals("/Human/address", includedElement().getHostDocumentModelPath());
  }

  @Test
  void movingTheIncludeGroupCarriesItAlong() {
    List<ModelEdits> edits = edits(() -> {
      GroupElement contacts = new GroupElement();
      contacts.setId("group_contacts");
      contacts.setName("Contacts");
      GroupConfig config = new GroupConfig();
      config.setRepeatability(1);
      config.setElements(new ArrayList<>());
      contacts.setGroup(config);
      Element address = personChild("address");
      person().getGroup().getElements().remove(address);
      config.getElements().add(address);
      person().getGroup().getElements().add(contacts);
    });

    edits.forEach(modelEdits -> modelEdits.edits().forEach(Edit::apply));

    assertEquals("/Person/Contacts/address", includedElement().getHostDocumentModelPath());
  }

  @Test
  void theOtherIncludeGroupAndTheElementReferencesStayAsTheyAre() {
    String elementRefBefore = JsonSettings.objectMapper.writeValueAsString(hostForm.getContent().getScreens());

    List<ModelEdits> edits = edits(() -> personChild("billing").setName("invoice"));

    assertEquals(List.of(), edits, "the form's include is at /Person/address, not below billing");
    assertEquals(elementRefBefore, JsonSettings.objectMapper.writeValueAsString(hostForm.getContent().getScreens()));
  }

  @Test
  void aChangeInTheIncludedModelDoesNotTouchAFormBoundToTheHostModel() {
    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(includedDm);
    includedDm.getContent().getModelRoot().getRootGroups().get(0).setName("Location");

    List<ModelEdits> edits = ProjectReferenceRefactoring.computeEdits(includedDm, plan,
        List.<A12Model<?>>of(hostDm, includedDm, hostForm, includedForm));

    assertTrue(edits.stream().noneMatch(modelEdits -> modelEdits.model() == hostForm), "hostForm is bound to A-for-host");
  }

  @Test
  void revertingPutsTheOldPathBack() {
    List<ModelEdits> edits = edits(() -> personChild("address").setName("location"));
    edits.forEach(modelEdits -> modelEdits.edits().forEach(Edit::apply));

    edits.forEach(modelEdits -> modelEdits.edits().forEach(Edit::revert));

    assertEquals("/Person/address", includedElement().getHostDocumentModelPath());
  }

  @Test
  void aFormWithoutIncludesIsNotTouched() {
    ScreenElement element = includedElement();
    element.setIncludeId(null);
    element.setFormModelRef(null);
    element.setHostDocumentModelPath(null);

    assertEquals(List.of(), edits(() -> personChild("address").setName("location")));
  }
}
