package de.a12.studio.dataservices.services.documentmodel.computationrule;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.OriginInComputationFragment;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;
import de.a12.studio.dataservices.services.support.ProblemReporter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ComputationRuleService {

  public ComputationResultDto validate(DocumentModel documentModel, String computationId) {
    ProblemReporter pr = new ProblemReporter();
    Computation computation = DocumentModelSupport.getComputationById(documentModel, computationId);
    if (computation.getComputedField().getDocumentModelObject().isEmpty()) {
      return new ComputationResultDto(
          Collections.emptyList(), Collections.emptyList(), computation.getComputationAlternatives());
    }
    String path = new DocumentModelService().getPath(computation);
    new DocumentModelService().isValidComputation(path, documentModel, pr);

    List<String> semanticErrors =
        pr.getProblems().stream()
            .filter(p -> OriginInComputationFragment.of(p).isEmpty())
            .map(p -> p.getMessage())
            .toList();

    List<ComputationValidationResult> parserErrors =
        pr.getProblems().stream()
            .map(p -> {
              Optional<OriginInComputationFragment> fragment = OriginInComputationFragment.of(p);
              return fragment.isPresent() ? convertToValidationResult(p.getMessage(), fragment.get()) : null;
            })
            .filter(java.util.Objects::nonNull)
            .toList();

    return new ComputationResultDto(semanticErrors, parserErrors, computation.getComputationAlternatives());
  }

  private ComputationValidationResult convertToValidationResult(String message, OriginInComputationFragment fragment) {
    String type = determineConditionType(fragment);
    int index = fragment.getComputationAlternativeIndex();
    int line = fragment.getStartInFragment().getLineAndColumn().getLeft();
    int start = fragment.getStartInFragment().getLineAndColumn().getRight();
    int end = fragment.getEndInFragment().getLineAndColumn().getRight();

    return new ComputationValidationResult(type, index, line, start, end, message);
  }

  private String determineConditionType(OriginInComputationFragment fragment) {
    // check if it is a commonPrecondition must be done first, because isPrecondition also returns true
    if (fragment.isCommonPrecondition()) {
      return "commonPrecondition";
    } else if (fragment.isPrecondition()) {
      return "precondition";
    } else {
      return "operation";
    }
  }

  public String formatCommonPrecondition(DocumentModel documentModel, String computationId) {
    Computation computation = DocumentModelSupport.getComputationById(documentModel, computationId);
    if (computation.getCommonPrecondition().isEmpty()) {
      return "";
    }

    return formatComputationCondition(computation.getCommonPrecondition().get(), computation, documentModel);
  }

  public String formatPrecondition(DocumentModel documentModel, String computationId, int alternativeIndex) {
    Computation computation = DocumentModelSupport.getComputationById(documentModel, computationId);
    var precondition = computation.getComputationAlternatives().get(alternativeIndex).getPrecondition();

    if (precondition.isEmpty()) {
      return "";
    }

    return formatComputationCondition(precondition.get(), computation, documentModel);
  }

  public String formatCalculation(DocumentModel documentModel, String computationId, int alternativeIndex) {
    Computation computation = DocumentModelSupport.getComputationById(documentModel, computationId);
    String operation = computation.getComputationAlternatives().get(alternativeIndex).getOperation();
    if (operation.isEmpty()) {
      return "";
    }

    DocumentModelService service = new DocumentModelService();
    String path = service.getPath(computation);
    ProblemReporter pr = new ProblemReporter();
    return service.formatComputationOperation(operation, path, documentModel, pr);
  }

  private String formatComputationCondition(String condition, Computation computation, DocumentModel documentModel) {
    DocumentModelService service = new DocumentModelService();
    String path = service.getPath(computation);
    ProblemReporter pr = new ProblemReporter();
    return service.formatComputationCondition(condition, path, documentModel, pr);
  }
}
