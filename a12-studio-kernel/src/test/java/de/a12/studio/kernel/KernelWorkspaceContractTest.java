package de.a12.studio.kernel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Contract test over whole sample projects: every Document Model, Type Definition Model and Combination Model of
 * the workspace must expand with the kernel without errors and the expansion must pass the kernel's own consistency
 * check. Complements {@link KernelDocumentModelCheckerContractTest}, which pins single models, by catching a
 * kernel bump that starts to reject something real projects contain.
 */
class KernelWorkspaceContractTest {

  private static List<String> expandableModelIds(String workspace) throws IOException {
    List<String> ids = new ArrayList<>();
    try (Stream<Path> files = Files.walk(TestWorkspaces.findRoot().resolve("testing/workspaces").resolve(workspace))) {
      files.map(f -> f.getFileName().toString())
          // data documents are named Model+n_...json and are not models
          .filter(name -> name.matches("[^+]*_(DM|TDM|Cm)[.]json"))
          .map(name -> name.replaceFirst("[.]json$", ""))
          .forEach(ids::add);
    }
    return ids;
  }

  private void assertWorkspaceExpandsAndIsConsistent(String workspace) throws IOException {
    List<String> ids = expandableModelIds(workspace);
    assertFalse(ids.isEmpty(), "no models found in " + workspace);
    List<String> problems = new ArrayList<>();
    for (String id : ids) {
      try {
        KernelExpansion expansion = new KernelDocumentModelExpander().expand(id, TestWorkspaces.source(workspace));
        if (expansion.hasErrors()) {
          problems.add(id + " expansion: " + expansion.findings());
          continue;
        }
        List<KernelFinding> findings = new KernelDocumentModelChecker().check(expansion.expandedJson());
        if (findings.stream().anyMatch(KernelFinding::isError)) {
          problems.add(id + " check: " + findings);
        }
      }
      catch (KernelException e) {
        problems.add(id + ": " + e.getMessage());
      }
    }
    assertTrue(problems.isEmpty(), () -> workspace + ": " + String.join("\n", problems));
  }

  @Test
  void theBasicWorkspaceExpandsAndIsConsistent() throws IOException {
    assertWorkspaceExpandsAndIsConsistent("basic");
  }

  @Test
  void theECommerceWorkspaceExpandsAndIsConsistent() throws IOException {
    assertWorkspaceExpandsAndIsConsistent("e-commerce");
  }

  @Test
  void theAdvancedWorkspaceExpandsAndIsConsistent() throws IOException {
    assertWorkspaceExpandsAndIsConsistent("advanced_new");
  }
}
