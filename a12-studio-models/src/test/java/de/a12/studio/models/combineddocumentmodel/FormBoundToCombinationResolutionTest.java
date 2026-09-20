package de.a12.studio.models.combineddocumentmodel;

import de.a12.studio.models.TestHelper;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code PersonEmployee_Fm}'s "data binding" reference names {@code PersonEmployee_Cm}, a Combination Model
 * (base {@code Person_Dc} + additive {@code PersonEmployee_Ad}), not a plain Document Model. The Form Model
 * editor has to resolve that reference through {@link CombinedDocumentModelElements} - a lookup limited to
 * {@link DocumentModel}s finds nothing, and the editor then shows no linked Document Model and can't resolve
 * any of the form tree's {@code elementRef}s/{@code groupRef}s.
 */
class FormBoundToCombinationResolutionTest {

  private static final String COMBINATION_ID = "PersonEmployee_Cm";

  private final Path workspace = TestHelper.resolveTestingAdvancedNewDir();
  private final ProjectItem root = new ProjectItem(workspace.toFile());

  @Test
  void theBoundModelIsACombinationThatAPlainDocumentModelLookupCannotSee() {
    ProjectItem item = root.findByModelId(COMBINATION_ID);

    assertNotNull(item);
    assertFalse(item.getModel() instanceof DocumentModel);
  }

  @Test
  void everyElementAndGroupRefOfTheFormResolvesThroughTheCombination() throws IOException {
    DocumentModel resolved = CombinedDocumentModelElements.resolveForFieldReferences(root, COMBINATION_ID);

    assertNotNull(resolved);
    assertEquals(COMBINATION_ID, resolved.getId());

    Set<String> ids = new HashSet<>();
    collectIds(resolved.getContent().getModelRoot().getRootGroups(), ids);

    Set<String> refs = new HashSet<>();
    collectRefs(JsonSettings.objectMapper.readTree(Files.readString(
        workspace.resolve("models/10_People/PersonEmployee_Fm.json"))), refs);
    assertFalse(refs.isEmpty());

    List<String> unresolved = new ArrayList<>(refs);
    unresolved.removeAll(ids);
    assertTrue(unresolved.isEmpty(), "Refs that do not resolve against " + COMBINATION_ID + ": " + unresolved);
  }

  private static void collectIds(List<? extends Element> elements, Set<String> ids) {
    for (Element element : elements) {
      ids.add(element.getId());
      if (element instanceof GroupElement group && group.getGroup() != null && group.getGroup().getElements() != null) {
        collectIds(group.getGroup().getElements(), ids);
      }
    }
  }

  private static void collectRefs(JsonNode node, Set<String> refs) {
    if (node.isObject()) {
      for (Map.Entry<String, JsonNode> property : node.properties()) {
        if (("elementRef".equals(property.getKey()) || "groupRef".equals(property.getKey())) && property.getValue().isString()) {
          refs.add(property.getValue().asString());
        }
        else {
          collectRefs(property.getValue(), refs);
        }
      }
    }
    else if (node.isArray()) {
      for (JsonNode child : node) {
        collectRefs(child, refs);
      }
    }
  }
}
