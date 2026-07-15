package de.a12.studio.dataservices.services.documentmodel.validationrule;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IMVK_Service;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;
import de.a12.studio.dataservices.services.support.ProblemReporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ValidationRuleService {

  public List<ValidationResult> validateCondition(DocumentModel documentModel, String ruleId) {
    Rule rule = DocumentModelSupport.getRuleById(documentModel, ruleId);
    if (rule.getErrorEntity().getDocumentModelObject().isEmpty()) {
      return Collections.emptyList();
    }

    return validateCondition(documentModel, rule);
  }

  private List<ValidationResult> validateCondition(DocumentModel documentModel, Rule rule) {
    var mvkService = new DocumentModelService().getMvkServiceForModel(documentModel, null);
    ProblemReporter problemReporter = new ProblemReporter();
    new DocumentModelService().hasValidConditionText(mvkService, rule, problemReporter);

    return problemReporter.getProblems().stream()
        .map(p -> new ValidationResult(p.getLine(), p.getSourceStart(), p.getSourceEnd(), p.getMessage()))
        .toList();
  }

  public List<String> validateErrorMessage(DocumentModel documentModel, String ruleId, int index) {
    Rule rule = DocumentModelSupport.getRuleById(documentModel, ruleId);
    if (rule.getErrorEntity().getDocumentModelObject().isEmpty()) {
      return Collections.emptyList();
    }
    var mvkService = new DocumentModelService().getMvkServiceForModel(documentModel, null);

    ProblemReporter pr = new ProblemReporter();
    Locale locale = new ArrayList<>(rule.getErrorMessage().keySet()).get(index);
    new DocumentModelService().hasValidErrorText(mvkService, rule, List.of(locale), false, pr);

    return pr.getProblems().stream().map(p -> p.getMessage()).toList();
  }

  public String formatCondition(DocumentModel documentModel, String ruleId) {
    var mvkService = new DocumentModelService().getMvkServiceForModel(documentModel, null);
    Rule rule = DocumentModelSupport.getRuleById(documentModel, ruleId);
    IRule iRule = convertToIRule(rule, mvkService);

    return format(iRule, mvkService);
  }

  private IRule convertToIRule(Rule rule, IMVK_Service mvkService) {
    String errorFieldPath = new DocumentModelService().getPath((Element) rule.getErrorEntity().getDocumentModelObject().get());
    IEntity errorField = mvkService.getIEC().get(errorFieldPath);
    String rulePath = new DocumentModelService().getPath(rule);

    return generateRule(rulePath, errorField, rule.getErrorCondition(), rule.getErrorMessage());
  }

  private IRule generateRule(String fullName, IEntity errorField, String conditionText, Map<Locale, String> errorMessages) {
    return new IRule() {
      @Override
      public String getFullName() {
        return fullName;
      }

      @Override
      public String getErrorConditionText() {
        return conditionText;
      }

      @Override
      public String getErrorMessage(Locale loc) {
        return "";
      }

      @Override
      public Map<Locale, String> getErrorMessages() {
        return errorMessages;
      }

      @Override
      public IEntity getErrorEntity() {
        return errorField;
      }

      @Override
      public String getErrorCode() {
        return "";
      }

      @Override
      public RuleSeverityType getSeverityType() {
        return RuleSeverityType.ERROR;
      }
    };
  }

  private String format(IRule rule, IMVK_Service mvkService) {
    ProblemReporter pr = new ProblemReporter();
    String result = mvkService.formatConditionText(rule, pr);
    return (result == null || result.isBlank()) ? rule.getErrorConditionText() : result;
  }
}
