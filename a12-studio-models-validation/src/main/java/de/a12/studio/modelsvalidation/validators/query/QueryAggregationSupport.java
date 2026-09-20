package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.TimeFieldType;
import de.a12.studio.models.querymodel.QueryAggregationEntry;
import de.a12.studio.modelsvalidation.validators.ElementIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The rules for what an aggregation ({@code content.aggregation}) may refer to, shared by {@link
 * QueryAggregationValidator} and the Query editor's aggregation panel (which offers only what passes them).
 * <ul>
 *   <li>A group or aggregated field must be a <em>non-repeatable</em> field of the target Document Model that is not
 *       annotated {@code indexed = false} (SME's Query Model documentation, "Validation": "Sorting and Aggregation is
 *       only done on non-repeatable Fields ... that do not have Annotation indexed = false").</li>
 *   <li>Function availability per field type is Data Services': {@code count} works for every type, {@code sum} and
 *       {@code avg} for numbers only, {@code min} and {@code max} for numbers, dates, date-times and times ("Query
 *       API - Aggregations, Aggregation Functions").</li>
 * </ul>
 * The repeatability test uses {@link ElementIndex#granularity}, which resolves elements by id; an element that was
 * reached through an Include or an Additive base carries the id of <em>its own</em> model, so such a field is not
 * recognized as repeatable here (no false error, but no error either).
 */
public final class QueryAggregationSupport {

  /** Why a field cannot be used in an aggregation. */
  public enum Problem {
    /** No element at that path (or not a field). */
    UNKNOWN,
    /** The path resolves to a group, not a field. */
    NOT_A_FIELD,
    /** The field is annotated {@code indexed = false}. */
    NOT_INDEXED,
    /** The field sits in a repeatable group. */
    REPEATABLE
  }

  /**
   * @param element the resolved element, null for {@link Problem#UNKNOWN}
   * @param problem null when the field is usable
   * @param repeatableGroup for {@link Problem#REPEATABLE} the path of the innermost repeatable group
   */
  public record Check(Element element, Problem problem, String repeatableGroup) {

    public boolean ok() {
      return problem == null;
    }
  }

  private static final Set<String> NUMBER_ONLY = Set.of(QueryAggregationEntry.FUNCTION_SUM, QueryAggregationEntry.FUNCTION_AVG);
  private static final Set<String> ORDERED = Set.of(QueryAggregationEntry.FUNCTION_MIN, QueryAggregationEntry.FUNCTION_MAX);

  private QueryAggregationSupport() {
  }

  /** Whether {@code path} is a field that can be grouped by or aggregated in a query on {@code index}' model. */
  public static Check check(ElementIndex index, String path) {
    Element element = index.resolveAbsolutePath(path).orElse(null);
    if (element == null) {
      return new Check(null, Problem.UNKNOWN, null);
    }
    if (!(element instanceof FieldElement)) {
      return new Check(element, Problem.NOT_A_FIELD, null);
    }
    if (isNotIndexed(element)) {
      return new Check(element, Problem.NOT_INDEXED, null);
    }
    List<String> repeatableGroups = index.granularity(element.getId());
    if (!repeatableGroups.isEmpty()) {
      return new Check(element, Problem.REPEATABLE, repeatableGroups.get(repeatableGroups.size() - 1));
    }
    return new Check(element, null, null);
  }

  /** The paths of every field of {@code index}' own element tree that passes {@link #check}, in tree order. */
  public static List<String> candidatePaths(ElementIndex index) {
    List<String> paths = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (element instanceof FieldElement && !isNotIndexed(element) && index.granularity(element.getId()).isEmpty()) {
        paths.add(index.getPath(element));
      }
    }
    return paths;
  }

  /** The effective type of {@code element} (a type definition reference resolved), or null if it is not a field
   * or the type cannot be determined. */
  public static FieldType effectiveType(ElementIndex index, Element element) {
    if (!(element instanceof FieldElement field) || field.getField() == null) {
      return null;
    }
    return index.effectiveFieldType(field.getField().getFieldType());
  }

  /** Whether Data Services offers {@code function} for a field of {@code type}. An undeterminable type
   * (null) is given the benefit of the doubt. */
  public static boolean isFunctionAvailable(String function, FieldType type) {
    if (type == null || function == null) {
      return true;
    }
    boolean number = type instanceof NumberFieldType;
    if (NUMBER_ONLY.contains(function)) {
      return number;
    }
    if (ORDERED.contains(function)) {
      return number || type instanceof DateFieldType || type instanceof DateTimeFieldType || type instanceof TimeFieldType;
    }
    return true;
  }

  /** The field type kinds {@code function} is available for, for messages. */
  public static String availableFor(String function) {
    if (NUMBER_ONLY.contains(function)) {
      return "Number";
    }
    if (ORDERED.contains(function)) {
      return "Number, Date, Date-Time, Time";
    }
    return "every type";
  }

  /** Short type name for messages: {@code NumberType} -> {@code Number}. */
  public static String typeLabel(FieldType type) {
    if (type == null || type.getType() == null) {
      return "unknown";
    }
    String name = type.getType();
    if ("DateTimeType".equals(name)) {
      return "Date-Time";
    }
    return name.endsWith("Type") ? name.substring(0, name.length() - "Type".length()) : name;
  }

  /** The functions of {@link QueryAggregationEntry#FUNCTIONS} that can be applied to a field of {@code type}. */
  public static List<String> functionsFor(FieldType type) {
    return QueryAggregationEntry.FUNCTIONS.stream().filter(function -> isFunctionAvailable(function, type)).toList();
  }

  private static boolean isNotIndexed(Element element) {
    return element.getAnnotations().stream()
        .filter(annotation -> "indexed".equals(annotation.getName()))
        .map(Annotation::getValue)
        .anyMatch("false"::equals);
  }
}
