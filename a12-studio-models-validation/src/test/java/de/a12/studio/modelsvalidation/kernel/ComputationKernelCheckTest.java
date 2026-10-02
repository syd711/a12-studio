package de.a12.studio.modelsvalidation.kernel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.a12.studio.modelsvalidation.kernel.ComputationKernelCheck.TextPart;
import de.a12.studio.models.projects.ProjectItem;

class ComputationKernelCheckTest {

  private static ProjectItem workspace() {
    Path dir = Path.of("").toAbsolutePath();
    while (dir != null && !Files.isDirectory(dir.resolve("testing/workspaces/basic"))) {
      dir = dir.getParent();
    }
    return new ProjectItem(dir.resolve("testing/workspaces/basic").toFile());
  }

  private static ComputationKernelCheck check() {
    return new ComputationKernelCheck(workspace(), "Invoice_DM", "C110");
  }

  @Test
  void aValidPreconditionHasNoProblem() {
    assertNull(check().check(TextPart.PRECONDITION, 0, "FieldFilled(PaymentInfo/MethodOfPayment)"));
  }

  @Test
  void aSyntaxErrorInAnAlternativesOperationIsReportedWithItsPosition() {
    String message = check().check(TextPart.OPERATION, 1, "[PaymentInfo/MethodOfPayment] +");

    assertNotNull(message);
    assertTrue(message.matches("(?s).*[(]line 1, column [0-9]+[)]$"), message);
  }

  @Test
  void aNotYetStoredAlternativeCanBeChecked() {
    assertNull(check().check(TextPart.PRECONDITION, 3, "FieldFilled(PaymentInfo/MethodOfPayment)"));
  }

  @Test
  void anUnknownFieldInTheCommonPreconditionIsReported() {
    assertNotNull(check().check(TextPart.COMMON_PRECONDITION, 0, "FieldFilled(DoesNotExist)"));
  }
}
