package de.a12.studio.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Contract test for the kernel facade: pins the behavior of kernel {@value KernelDocumentModelChecker#KERNEL_VERSION}
 * that a12-studio relies on, so a kernel bump that changes it fails here instead of shipping silently. The
 * kernel's {@code internal} surface has no stability guarantee (owner decision 2026-10-02: rely on it, but only
 * behind this module).
 */
class KernelDocumentModelCheckerContractTest {

  private final KernelDocumentModelChecker checker = new KernelDocumentModelChecker();

  @Test
  void consistentModelHasNoErrors() throws IOException {
    List<KernelFinding> findings = checker.check(TestWorkspaces.read("basic/models/Company_DM.json"));

    assertTrue(findings.stream().noneMatch(KernelFinding::isError), () -> "unexpected findings: " + findings);
  }

  @Test
  void corruptedRuleConditionIsReportedWithKernelErrorCode() throws IOException {
    String original = TestWorkspaces.read("basic/models/Company_DM.json");
    String condition = "\"errorCondition\": \"GroupFilled(RuleGroup) and FieldNotFilled(internal_filename)\"";
    assertTrue(original.contains(condition), "fixture changed, adjust the test");

    List<KernelFinding> findings = checker.check(original.replace(condition, "\"errorCondition\": \"nonsense nonsense\""));

    assertFalse(findings.isEmpty());
    assertTrue(findings.stream().anyMatch(f -> f.isError() && f.message().contains("MVK_UNEXPECTED_TOKEN")),
        () -> "expected an MVK_UNEXPECTED_TOKEN error, got: " + findings);
  }

  @Test
  void modelWithUnexpandedIncludesIsRefused() throws IOException {
    List<KernelFinding> findings = checker.check(TestWorkspaces.read("basic/models/Invoice_DM.json"));

    assertTrue(findings.stream().anyMatch(f -> f.isError() && f.message().contains("Unexpanded include")),
        () -> "expected the kernel to refuse unexpanded includes, got: " + findings);
  }

  @Test
  void kernelVersionConstantMatchesBuildFile() throws IOException {
    String build = Files.readString(TestWorkspaces.findRoot().resolve("a12-studio-kernel/build.gradle"));
    assertTrue(build.contains("kernel-md-facade:" + KernelDocumentModelChecker.KERNEL_VERSION + "'"));
    assertEquals("31.1.1", KernelDocumentModelChecker.KERNEL_VERSION);
  }


  @Test
  void expansionResolvesIncludesAndTheExpandedModelPassesTheConsistencyCheck() throws IOException {
    KernelExpansion expansion = new KernelDocumentModelExpander().expand("Invoice_DM", TestWorkspaces.source("basic"));

    assertFalse(expansion.hasErrors(), () -> "expansion findings: " + expansion.findings());
    assertFalse(expansion.expandedJson().contains("\"includeConfig\""), "includes must be resolved");
    assertTrue(expansion.expandedJson().length() > TestWorkspaces.read("basic/models/Invoice_DM.json").length(), "model must have grown");
    List<KernelFinding> findings = checker.check(expansion.expandedJson());
    assertTrue(findings.stream().noneMatch(KernelFinding::isError), () -> "unexpected findings: " + findings);
  }

  @Test
  void combinationModelsExpandToTheirJoinedDocumentModel() throws IOException {
    for (String cm : List.of("PersonEmployee_Cm", "PersonFreelancer_Cm", "PersonSkills_LinkFields_Cm")) {
      KernelExpansion expansion = new KernelDocumentModelExpander().expand(cm, TestWorkspaces.source("advanced_new"));

      assertFalse(expansion.hasErrors(), () -> cm + " expansion findings: " + expansion.findings());
      assertTrue(expansion.expandedJson().contains("\"modelRoot\""), () -> cm + " did not produce a Document Model");
    }
  }

  @Test
  void expandingAModelWithoutIncludesKeepsItConsistent() throws IOException {
    KernelExpansion expansion = new KernelDocumentModelExpander().expand("Company_DM", TestWorkspaces.source("basic"));

    assertFalse(expansion.hasErrors(), () -> "expansion findings: " + expansion.findings());
  }

  @Test
  void expandingAnUnknownModelFailsWithKernelException() {
    assertThrows(KernelException.class, () -> new KernelDocumentModelExpander().expand("Missing_DM", TestWorkspaces.source("basic")));
  }
}
