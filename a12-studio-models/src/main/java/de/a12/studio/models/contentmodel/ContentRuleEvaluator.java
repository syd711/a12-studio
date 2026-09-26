package de.a12.studio.models.contentmodel;

import de.a12.studio.models.contentmodel.ContentModule.AnyOf;
import de.a12.studio.models.contentmodel.ContentModule.AnyOfSequences;
import de.a12.studio.models.contentmodel.ContentModule.ChildRule;
import de.a12.studio.models.contentmodel.ContentModule.ModuleRule;
import de.a12.studio.models.contentmodel.ContentModule.NoneOf;
import de.a12.studio.models.contentmodel.ContentModule.Sequence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks a list of children against a {@link ChildRule}, the way SME's {@code RelationshipRules.evaluateChildrenRule}
 * does. The children are {@link Node}s of a simplified tree that only knows each element's module id, so the check
 * can be made for an element that does not exist yet.
 */
final class ContentRuleEvaluator {

  /** An element reduced to its module id and (simplified) children. */
  record Node(String moduleId, List<Node> children) {
  }

  private ContentRuleEvaluator() {
  }

  static boolean matches(List<Node> children, ChildRule rule) {
    return switch (rule) {
      case Sequence sequence -> matchesSequence(children, sequence.rules());
      case NoneOf noneOf -> matchesNoneOf(children, noneOf);
      case AnyOf anyOf -> matchesAnyOf(children, anyOf);
      case AnyOfSequences anyOf -> anyOf.sequences().stream().anyMatch(sequence -> matchesSequence(children, sequence.rules()));
    };
  }

  private static boolean matchesNoneOf(List<Node> children, NoneOf rule) {
    if (rule.ids().contains(ContentModule.ANY)) {
      return children.isEmpty();
    }
    return children.stream().noneMatch(child -> rule.ids().contains(child.moduleId()));
  }

  private static boolean matchesAnyOf(List<Node> children, AnyOf rule) {
    for (ModuleRule moduleRule : rule.rules()) {
      if (ContentModule.ANY.equals(moduleRule.id())) {
        return matchesModuleRule(children, moduleRule);
      }
    }
    if (children.stream().anyMatch(child -> rule.rules().stream().noneMatch(r -> r.id().equals(child.moduleId())))) {
      return false;
    }
    Map<String, List<Node>> byModule = new LinkedHashMap<>();
    for (Node child : children) {
      byModule.computeIfAbsent(child.moduleId(), key -> new ArrayList<>()).add(child);
    }
    for (ModuleRule moduleRule : rule.rules()) {
      if (!matchesModuleRule(byModule.getOrDefault(moduleRule.id(), List.of()), moduleRule)) {
        return false;
      }
    }
    return true;
  }

  private static boolean matchesSequence(List<Node> children, List<ModuleRule> rules) {
    List<List<Node>> groups = chunk(children);
    List<List<Node>> pairedNodes = new ArrayList<>();
    List<ModuleRule> pairedRules = new ArrayList<>();
    int group = 0;
    int ruleIndex = 0;
    while (true) {
      if (group >= groups.size()) {
        // What is left of the rules is matched against no nodes at all (fails for a minimum above zero).
        for (; ruleIndex < rules.size(); ruleIndex++) {
          pairedNodes.add(List.of());
          pairedRules.add(rules.get(ruleIndex));
        }
        break;
      }
      if (ruleIndex >= rules.size()) {
        return false;
      }
      ModuleRule rule = rules.get(ruleIndex);
      if (groups.get(group).get(0).moduleId().equals(rule.id())) {
        pairedNodes.add(groups.get(group));
        pairedRules.add(rule);
        group++;
        ruleIndex++;
      }
      else if (rule.min() != null) {
        return false;
      }
      else {
        ruleIndex++;
      }
    }
    for (int i = 0; i < pairedRules.size(); i++) {
      if (!matchesModuleRule(pairedNodes.get(i), pairedRules.get(i))) {
        return false;
      }
    }
    return true;
  }

  /** The nodes are all of the rule's type: their own children, then the instance limits. */
  private static boolean matchesModuleRule(List<Node> nodes, ModuleRule rule) {
    if (rule.childrenRule() != null) {
      for (Node node : nodes) {
        if (!matches(node.children(), rule.childrenRule())) {
          return false;
        }
      }
    }
    if (rule.max() != null && nodes.size() > rule.max()) {
      return false;
    }
    return rule.min() == null || nodes.size() >= rule.min();
  }

  /** Splits into runs of neighbours with the same module id. */
  private static List<List<Node>> chunk(List<Node> nodes) {
    List<List<Node>> result = new ArrayList<>();
    List<Node> current = new ArrayList<>();
    for (Node node : nodes) {
      if (!current.isEmpty() && !current.get(0).moduleId().equals(node.moduleId())) {
        result.add(current);
        current = new ArrayList<>();
      }
      current.add(node);
    }
    if (!current.isEmpty()) {
      result.add(current);
    }
    return result;
  }
}
