package de.a12.studio.kernel;

import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/** Collects the problems the kernel reports (the counterpart of SME's {@code ProblemReporter}). */
final class ProblemCollector implements IProblemReporter {

  final List<IProblem> problems = new ArrayList<>();

  @Override
  public void reportProblem(IProblem problem) {
    problems.add(problem);
  }
}
