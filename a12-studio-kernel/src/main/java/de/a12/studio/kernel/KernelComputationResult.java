package de.a12.studio.kernel;

import java.util.List;

/**
 * @param semanticErrors problems without a position in the text (e.g. unknown fields, type errors)
 * @param parserErrors   problems that can be pinned to a position in a precondition or operation
 */
public record KernelComputationResult(List<String> semanticErrors, List<KernelComputationProblem> parserErrors) {

  public boolean isValid() {
    return semanticErrors.isEmpty() && parserErrors.isEmpty();
  }
}
