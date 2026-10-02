package de.a12.studio.kernel;

/**
 * A positioned problem inside one part of a computation.
 *
 * @param part             which text the position refers to
 * @param alternativeIndex index of the computation alternative the part belongs to, as the kernel reports it
 * @param line             1-based line within that part's text
 * @param startColumn      start column within the line
 * @param endColumn        end column within the line
 * @param message          the kernel's message
 */
public record KernelComputationProblem(Part part, int alternativeIndex, int line, int startColumn, int endColumn,
                                       String message) {

  /** The texts of a computation that carry conditions; SME calls them commonPrecondition, precondition and operation. */
  public enum Part {
    COMMON_PRECONDITION, PRECONDITION, OPERATION
  }
}
