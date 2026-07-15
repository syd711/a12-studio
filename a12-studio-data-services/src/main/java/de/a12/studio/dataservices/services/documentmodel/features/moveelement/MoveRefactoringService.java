package de.a12.studio.dataservices.services.documentmodel.features.moveelement;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.MoveSupportDM;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MoveRefactoringService {

  public RefactorConditionsResult refactorConditionTexts(DocumentModel documentModel, Map<String, String> changes) {
    DocumentModel copy = DocumentModelCopyService.copy(documentModel);
    MoveSupportDM.changePathReferencesForMove(copy, changes);
    List<Rule> oldRules = DocumentModelSupport.getAllRules(documentModel);
    List<Computation> oldComputations = DocumentModelSupport.getAllComputations(documentModel);
    List<Rule> rules = DocumentModelSupport.getAllRules(copy);
    List<Computation> computations = DocumentModelSupport.getAllComputations(copy);

    List<Rule> changedRules = filterChangedRules(oldRules, rules);
    List<Computation> changedComputations = filterChangedComputations(oldComputations, computations);

    List<JsonNode> rulesJson = DocumentModelSupport.serializeElements(List.copyOf(changedRules), copy);
    List<JsonNode> computationsJson = DocumentModelSupport.serializeElements(List.copyOf(changedComputations), copy);

    return new RefactorConditionsResult(rulesJson, computationsJson);
  }

  public List<Rule> filterChangedRules(List<Rule> oldRules, List<Rule> newRules) {
    Map<String, Rule> oldRulesMap = oldRules.stream().collect(Collectors.toMap(Rule::getId, Function.identity()));
    return newRules.stream()
        .filter(rule -> {
          Rule oldRule = oldRulesMap.get(rule.getId());
          if (oldRule == null) {
            throw new IllegalStateException("Old rule not found for id " + rule.getId());
          }

          return !rule.getErrorEntity().getRelativePath().equals(oldRule.getErrorEntity().getRelativePath())
              || !rule.getErrorCondition().equals(oldRule.getErrorCondition())
              || rule.getErrorMessage().entrySet().stream()
                  .anyMatch(entry -> !entry.getValue().equals(oldRule.getErrorMessage().get(entry.getKey())));
        })
        .toList();
  }

  public List<Computation> filterChangedComputations(List<Computation> oldComputations, List<Computation> newComputations) {
    Map<String, Computation> oldComputationsMap = oldComputations.stream().collect(Collectors.toMap(Computation::getId, Function.identity()));
    return newComputations.stream()
        .filter(computation -> {
          Computation oldComputation = oldComputationsMap.get(computation.getId());
          if (oldComputation == null) {
            throw new IllegalStateException("Old computation not found for id " + computation.getId());
          }

          if (!computation.getComputedField().getRelativePath().equals(oldComputation.getComputedField().getRelativePath())
              || !computation.getCommonPrecondition().equals(oldComputation.getCommonPrecondition())) {
            return true;
          }

          var oldAlternatives = oldComputation.getComputationAlternatives();
          for (int i = 0; i < computation.getComputationAlternatives().size(); i++) {
            var alt = computation.getComputationAlternatives().get(i);
            var oldAlternative = oldAlternatives.get(i);
            if (!alt.getPrecondition().equals(oldAlternative.getPrecondition())
                || !alt.getOperation().equals(oldAlternative.getOperation())) {
              return true;
            }
          }
          return false;
        })
        .toList();
  }
}
