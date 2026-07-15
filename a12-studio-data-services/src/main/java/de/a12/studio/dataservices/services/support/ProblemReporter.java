package de.a12.studio.dataservices.services.support;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

import java.util.ArrayList;
import java.util.List;

public class ProblemReporter implements IProblemReporter {

  private final List<IProblem> problems = new ArrayList<>();

  @Override
  public void reportProblem(IProblem problem) {
    problems.add(problem);
  }

  public List<IProblem> getProblems() {
    return problems;
  }

  public boolean hasProblemOccurred() {
    return !problems.isEmpty();
  }
}
