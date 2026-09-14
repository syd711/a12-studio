package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Field-path autocomplete candidates for {@code a12-studio-ui}'s expression/condition editors (built entirely
 * on {@link ElementIndex}, no grammar/parser involved - see {@code docs/sme-reference-comparison.md} for why),
 * mirroring SME's {@code
 * getFieldNameSuggestor}/{@code getGroupSuggestor} (client/src/modules/commonDocumentModel/api/editor/elements
 * /editor/conditionWidget/elementSuggestor.ts) at a much simpler level: every {@link FieldElement} in the model,
 * as either a relative path (kernel {@code ElementPathUtils} convention, e.g. {@code "../fieldName"}) or an
 * absolute one (e.g. {@code "/Group/fieldName"}).
 * <p>
 * Lives next to {@link ElementIndex} (not in {@code a12-studio-ui}) so it stays usable from any module that
 * already depends on {@code a12-studio-models-validation} without pulling in JavaFX.
 */
public final class FieldPathSuggestions {

  private FieldPathSuggestions() {
  }

  /**
   * One candidate field: {@code path} is what gets inserted, {@code documentation} is a short human-readable
   * summary (name, type, first available label) shown alongside it.
   */
  public record Entry(String path, String documentation) {
  }

  /**
   * Every field in {@code index}, as the relative path {@code referencingElement} would use to reach it (via
   * {@link ElementIndex#relativePathTo}) - matching what {@code TargetFieldPanelController} already writes into
   * a rule/computation's {@code errorEntityRelPath}/{@code computedFieldRelPath}. Always relative, never
   * switching to an absolute path when
   * crossing a repeatable-group boundary (unlike SME's {@code calculateAbsoluteAndRelativePaths}) - both forms
   * are valid per the kernel's path convention; this is a deliberate v1 simplification.
   */
  public static List<Entry> relativePaths(ElementIndex index, Element referencingElement) {
    List<Entry> entries = new ArrayList<>();
    for (Element candidate : index.allElements()) {
      if (candidate instanceof FieldElement field && candidate != referencingElement) {
        entries.add(new Entry(index.relativePathTo(referencingElement, candidate), describe(field)));
      }
    }
    return entries;
  }

  /**
   * Every field in {@code index} as its absolute, "/"-rooted path (via {@link ElementIndex#getPath}) - matching
   * the {@code I_FIELD} token in {@code a12-studio-models/src/main/antlr/.../ql/QL.g4} ({@code
   * [/Absolute/Path]}), used inside a QL filter definition's square brackets.
   */
  public static List<Entry> absolutePaths(ElementIndex index) {
    List<Entry> entries = new ArrayList<>();
    for (Element candidate : index.allElements()) {
      if (candidate instanceof FieldElement field) {
        entries.add(new Entry(index.getPath(candidate), describe(field)));
      }
    }
    return entries;
  }

  /**
   * The bare names of {@code children} that are fields - the Overview/Form "Expression" language's {@code
   * [FieldName]} syntax (see {@code docs/2606-06-doc/expression-expression-docs.md}'s grammar), which only
   * allows direct fields of the current {@code kontext(...)} scope, never a path. Pair with {@link
   * ElementIndex#directChildren} to get {@code children} for the scope at some caret position.
   */
  public static List<Entry> fieldNames(List<Element> children) {
    List<Entry> entries = new ArrayList<>();
    for (Element child : children) {
      if (child instanceof FieldElement field) {
        entries.add(new Entry(field.getName(), describe(field)));
      }
    }
    return entries;
  }

  /**
   * The bare names of {@code children} that are groups - the Expression language's {@code
   * groupOperation: 'kontext' '(' fieldName ... ')'} production, which likewise only allows a direct child
   * group name of the current scope. Pair with {@link ElementIndex#directChildren} the same way as {@link
   * #fieldNames}.
   */
  public static List<Entry> groupNames(List<Element> children) {
    List<Entry> entries = new ArrayList<>();
    for (Element child : children) {
      if (child instanceof GroupElement group) {
        entries.add(new Entry(group.getName(), describeGroup(group)));
      }
    }
    return entries;
  }

  private static String describeGroup(GroupElement group) {
    StringBuilder text = new StringBuilder(group.getName());
    Integer repeatability = group.getGroup() != null ? group.getGroup().getRepeatability() : null;
    text.append("  (group").append(repeatability != null && repeatability > 1 ? ", repeatable" : "").append(')');
    String label = group.getGroup() != null ? firstLabelText(group.getGroup().getLabel()) : null;
    if (label != null) {
      text.append(" – ").append(label);
    }
    return text.toString();
  }

  private static String describe(FieldElement field) {
    StringBuilder text = new StringBuilder(field.getName());
    String type = typeName(field.getField() != null ? field.getField().getFieldType() : null);
    if (type != null) {
      text.append("  (").append(type).append(')');
    }
    String label = field.getField() != null ? firstLabelText(field.getField().getLabel()) : null;
    if (label != null) {
      text.append(" – ").append(label);
    }
    return text.toString();
  }

  private static String firstLabelText(List<Label> labels) {
    if (labels == null) {
      return null;
    }
    return labels.stream()
        .map(Label::getText)
        .filter(text -> text != null && !text.isBlank())
        .findFirst()
        .orElse(null);
  }

  private static String typeName(FieldType fieldType) {
    if (fieldType == null) {
      return null;
    }
    String simpleName = fieldType.getClass().getSimpleName();
    String suffix = "FieldType";
    String trimmed = simpleName.endsWith(suffix) ? simpleName.substring(0, simpleName.length() - suffix.length()) : simpleName;
    return trimmed.isEmpty() ? null : Character.toLowerCase(trimmed.charAt(0)) + trimmed.substring(1);
  }
}
