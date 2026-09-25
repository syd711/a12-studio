package de.a12.studio.dataservices.preview;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.ColumnLinkReference;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@code PersonSkills_Person_Ru_SelectedItems_Ov.json} (see {@code testing/workspaces/advanced_new}) is bound
 * only through a Query Model header reference (no {@code document-model-for-overview}), and half its columns
 * carry {@code linkReferences} projecting fields from the relationship's own link document model (itself a
 * Combination Model, {@code PersonSkills_LinkFields_Cm}) rather than the query's target Document Model.
 * {@link DocumentModelFieldResolver#resolveOverviewDocumentModel}/{@link
 * DocumentModelFieldResolver#resolveLinkDocumentModel} resolve each side so the wireframe preview shows real
 * field labels instead of an empty field list.
 */
class DocumentModelFieldResolverQueryModelLinkColumnsTest {

  @Test
  void resolvesTheQueryModelsTargetAndTheRelationshipsLinkDocumentModel(@TempDir Path tempDir) throws IOException {
    Path source = locateWorkspace();
    copy(source, tempDir, "10_People/Person_Dc.json");
    copy(source, tempDir, "30_Skills/Skill_Dc.json");
    copy(source, tempDir, "30_Skills/PersonSkills_LinkFields_Cm.json");
    copy(source, tempDir, "30_Skills/PersonSkills_LinkFields_Base_Dc.json");
    copy(source, tempDir, "30_Skills/PersonSkills/PersonSkills_Re.json");
    copy(source, tempDir, "30_Skills/PersonSkills/PersonSkills_Person_Ru_SelectedItems_Qe.json");
    copy(source, tempDir, "30_Skills/PersonSkills/PersonSkills_Person_Ru_SelectedItems_Ov.json");

    ProjectItem root = new ProjectItem(tempDir.toFile());
    ProjectItem overviewItem = root.getChildren().stream()
        .filter(item -> "PersonSkills_Person_Ru_SelectedItems_Ov.json".equals(item.getName()))
        .findFirst()
        .orElseThrow();
    OverviewModel overviewModel = (OverviewModel) overviewItem.getModel();

    DocumentModel primary = DocumentModelFieldResolver.resolveOverviewDocumentModel(overviewModel, overviewItem);
    assertNotNull(primary, "should fall back to the Query Model's own target Document Model");
    assertEquals("Person_Dc", primary.getId());
    Map<String, Element> primaryElements = DocumentModelFieldResolver.index(primary);
    assertEquals("First Name", DocumentModelFieldResolver.fieldLabel(primaryElements.get("field_99c3b")));

    ColumnLinkReference linkReference = new ColumnLinkReference();
    linkReference.setRelationship("PersonSkills_Re");
    linkReference.setTargetRole("Skill");
    linkReference.setType(ColumnLinkReference.TYPE_LINK);
    DocumentModel linkModel = DocumentModelFieldResolver.resolveLinkedDocumentModel(linkReference, overviewItem);
    assertNotNull(linkModel, "should follow the relationship's linkDocumentModel, through its Combination Model");
    Map<String, Element> linkElements = DocumentModelFieldResolver.index(linkModel);
    // field_42656's label lists "de" first in the fixture, so firstLabelText() (labels.get(0)) is "Level".
    assertEquals("Level", DocumentModelFieldResolver.fieldLabel(linkElements.get("field_42656")));

    // A CHILD reference resolves to the target role's own document model instead of the link document.
    linkReference.setType(ColumnLinkReference.TYPE_CHILD);
    DocumentModel skillModel = DocumentModelFieldResolver.resolveLinkedDocumentModel(linkReference, overviewItem);
    assertNotNull(skillModel, "should follow the target role's documentModel");
    assertEquals("Skill_Dc", skillModel.getId());
  }

  private static void copy(Path source, Path tempDir, String relativePath) throws IOException {
    Path from = source.resolve(relativePath.replace('/', java.io.File.separatorChar));
    Files.copy(from, tempDir.resolve(from.getFileName()));
  }

  private static Path locateWorkspace() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("advanced_new").resolve("models");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/advanced_new/models' above " + Path.of("").toAbsolutePath());
  }
}
