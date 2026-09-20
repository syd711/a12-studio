package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.CustomFieldFieldType;
import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DateFragmentFieldType;
import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.RequirednessConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TimeFieldType;
import org.jspecify.annotations.NonNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

/**
 * What the Document Model tree currently shows: a search text (matched against name, id or label) plus the
 * narrowing filters of SME's tree filter panel (see its {@code tree/filter/filters.ts}) - annotated elements
 * only, element types, field types and requiredness. Pure state and predicates, no JavaFX; {@link
 * DocumentModelElementsTreeController} applies them while it builds the tree.
 * <p>
 * The rules follow SME's {@code useData.ts}: an element whose type is hidden disappears together with everything
 * below it; a group stays while anything below it is visible (or its own name/id/label matches the search text,
 * in which case only the matching part of its subtree is shown); any other element must match the search text,
 * the annotation filter and - for a field - the field-type and requiredness filters. With nothing set, the whole
 * tree is shown, empty groups included.
 * <p>
 * Two deliberate differences from SME: the two requiredness filters are the two requiredness modes of a field
 * (always / only if the parent group is filled), combined as alternatives, where SME ANDs two independent flags
 * one of which implies the other; and switching a requiredness filter on hides every element that is not a field
 * (SME keeps all rules and computations in the list next to the required fields).
 */
public final class DocumentModelTreeFilter {

  /** Which property of an element the search text is compared against. */
  public enum SearchIn {
    NAME, ID, LABEL
  }

  /** The element types that can be switched off; plain groups cannot (they only follow their content). */
  public enum ElementKind {
    VALIDATION_RULES, COMPUTATION_RULES, ATTACHMENTS, MULTI_SELECTS, INCLUDES, FIELDS
  }

  /** The base field types that can be switched off; a field whose type is a type definition counts as its base type. */
  public enum FieldKind {
    STRING, NUMBER, DATE, DATE_TIME, TIME, DATE_FRAGMENT, DATE_RANGE, CONFIRM, BOOLEAN, CUSTOM, ENUMERATION
  }

  private String searchText = "";
  private SearchIn searchIn = SearchIn.NAME;
  private boolean onlyAnnotated;
  private boolean alwaysRequired;
  private boolean requiredIfParentFilled;
  private final Set<ElementKind> hiddenElementKinds = EnumSet.noneOf(ElementKind.class);
  private final Set<FieldKind> hiddenFieldKinds = EnumSet.noneOf(FieldKind.class);

  // Resolves a field's declared type to the type it effectively has (a type definition reference to the type it
  // stands for); identity unless set, see #setEffectiveTypeResolver.
  private Function<FieldType, FieldType> effectiveTypeResolver = Function.identity();

  public String getSearchText() {
    return searchText;
  }

  public void setSearchText(String searchText) {
    this.searchText = searchText == null ? "" : searchText.trim();
  }

  public SearchIn getSearchIn() {
    return searchIn;
  }

  public void setSearchIn(@NonNull SearchIn searchIn) {
    this.searchIn = searchIn;
  }

  public boolean isOnlyAnnotated() {
    return onlyAnnotated;
  }

  public void setOnlyAnnotated(boolean onlyAnnotated) {
    this.onlyAnnotated = onlyAnnotated;
  }

  /** Only fields required in every case (SME: "Always Required"). */
  public boolean isAlwaysRequired() {
    return alwaysRequired;
  }

  public void setAlwaysRequired(boolean alwaysRequired) {
    this.alwaysRequired = alwaysRequired;
  }

  /** Only fields required only if their parent group is filled (SME: "Required If Parent Group Filled"). */
  public boolean isRequiredIfParentFilled() {
    return requiredIfParentFilled;
  }

  public void setRequiredIfParentFilled(boolean requiredIfParentFilled) {
    this.requiredIfParentFilled = requiredIfParentFilled;
  }

  public boolean isShown(@NonNull ElementKind kind) {
    return !hiddenElementKinds.contains(kind);
  }

  public void setShown(@NonNull ElementKind kind, boolean shown) {
    if (shown) {
      hiddenElementKinds.remove(kind);
    }
    else {
      hiddenElementKinds.add(kind);
    }
  }

  public boolean isShown(@NonNull FieldKind kind) {
    return !hiddenFieldKinds.contains(kind);
  }

  public void setShown(@NonNull FieldKind kind, boolean shown) {
    if (shown) {
      hiddenFieldKinds.remove(kind);
    }
    else {
      hiddenFieldKinds.add(kind);
    }
  }

  /**
   * How to look through a field's type definition reference to the base type it stands for, needed for the
   * field-type filter to see a type-definition field as the String/Number/... it really is. Without one (or when
   * the resolver returns {@code null}, i.e. the type definition is missing) such a field is not hidden by type.
   */
  public void setEffectiveTypeResolver(@NonNull Function<FieldType, FieldType> effectiveTypeResolver) {
    this.effectiveTypeResolver = effectiveTypeResolver;
  }

  /** Whether the text search alone is in use. */
  public boolean hasSearchText() {
    return !searchText.isEmpty();
  }

  /** Whether any narrowing filter apart from the search text is set - what the toolbar's filter button flags. */
  public boolean hasNarrowingFilters() {
    return onlyAnnotated || alwaysRequired || requiredIfParentFilled
        || !hiddenElementKinds.isEmpty() || !hiddenFieldKinds.isEmpty();
  }

  /** Whether the tree has to be narrowed at all; if not it is shown as is. */
  public boolean isActive() {
    return hasSearchText() || hasNarrowingFilters();
  }

  /** Clears everything except the search text and the property it searches in. */
  public void resetNarrowingFilters() {
    onlyAnnotated = false;
    alwaysRequired = false;
    requiredIfParentFilled = false;
    hiddenElementKinds.clear();
    hiddenFieldKinds.clear();
  }

  /**
   * Whether {@code element} and everything below it is switched off by an element-type filter. Plain groups
   * never are.
   */
  public boolean isHiddenByType(@NonNull Element element) {
    ElementKind kind = kindOf(element);
    return kind != null && hiddenElementKinds.contains(kind);
  }

  /** Whether {@code element}'s own name, id or label matches the search text (always true without one). */
  public boolean matchesSearch(@NonNull Element element) {
    if (searchText.isEmpty()) {
      return true;
    }
    String needle = searchText.toLowerCase(Locale.ROOT);
    return switch (searchIn) {
      case NAME -> contains(element.getName(), needle);
      case ID -> contains(element.getId(), needle);
      case LABEL -> labelsOf(element).stream().anyMatch(label -> contains(label.getText(), needle));
    };
  }

  /**
   * Whether a leaf element (anything but a group) is shown: it has to match the search text, the annotation
   * filter and, for a field, the field-type and requiredness filters.
   */
  public boolean isLeafShown(@NonNull Element element) {
    return matchesSearch(element) && passesAnnotationFilter(element) && passesFieldFilters(element);
  }

  private boolean passesAnnotationFilter(Element element) {
    return !onlyAnnotated || !element.getAnnotations().isEmpty();
  }

  private boolean passesFieldFilters(Element element) {
    if (!(element instanceof FieldElement field) || field.getField() == null) {
      // The field-type filter leaves other elements alone (switch those off by element type); the requiredness
      // filter asks for required fields, which nothing else is, so it narrows to fields only.
      return !(alwaysRequired || requiredIfParentFilled);
    }
    FieldConfig config = field.getField();

    if (!hiddenFieldKinds.isEmpty()) {
      FieldKind kind = fieldKindOf(effectiveType(config.getFieldType()));
      if (kind != null && hiddenFieldKinds.contains(kind)) {
        return false;
      }
    }

    if (alwaysRequired || requiredIfParentFilled) {
      RequirednessConfig requiredness = config.getRequirednessConfig();
      String mode = requiredness == null ? null : requiredness.getMode();
      return (alwaysRequired && RequirednessConfig.MODE_REQUIRED.equals(mode))
          || (requiredIfParentFilled && RequirednessConfig.MODE_REQUIRED_IF_PARENT_FILLED.equals(mode));
    }
    return true;
  }

  private FieldType effectiveType(FieldType declared) {
    return declared == null ? null : effectiveTypeResolver.apply(declared);
  }

  static ElementKind kindOf(Element element) {
    if (element instanceof RuleElement) {
      return ElementKind.VALIDATION_RULES;
    }
    if (element instanceof ComputationElement) {
      return ElementKind.COMPUTATION_RULES;
    }
    if (element instanceof FieldElement) {
      return ElementKind.FIELDS;
    }
    if (element instanceof GroupElement group && group.getGroup() != null) {
      GroupConfig config = group.getGroup();
      if (config.getIncludeConfig() != null) {
        return ElementKind.INCLUDES;
      }
      if (GroupConfig.USAGE_TYPE_ATTACHMENT.equals(config.getUsageType())) {
        return ElementKind.ATTACHMENTS;
      }
      if (GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(config.getUsageType())) {
        return ElementKind.MULTI_SELECTS;
      }
    }
    return null;
  }

  static FieldKind fieldKindOf(FieldType fieldType) {
    if (fieldType instanceof StringFieldType) {
      return FieldKind.STRING;
    }
    if (fieldType instanceof NumberFieldType) {
      return FieldKind.NUMBER;
    }
    if (fieldType instanceof DateFieldType) {
      return FieldKind.DATE;
    }
    if (fieldType instanceof DateTimeFieldType) {
      return FieldKind.DATE_TIME;
    }
    if (fieldType instanceof TimeFieldType) {
      return FieldKind.TIME;
    }
    if (fieldType instanceof DateFragmentFieldType) {
      return FieldKind.DATE_FRAGMENT;
    }
    if (fieldType instanceof DateRangeFieldType) {
      return FieldKind.DATE_RANGE;
    }
    if (fieldType instanceof ConfirmFieldType) {
      return FieldKind.CONFIRM;
    }
    if (fieldType instanceof BooleanFieldType) {
      return FieldKind.BOOLEAN;
    }
    if (fieldType instanceof CustomFieldFieldType) {
      return FieldKind.CUSTOM;
    }
    if (fieldType instanceof EnumerationFieldType) {
      return FieldKind.ENUMERATION;
    }
    return null;
  }

  /** The labels of a group or field in every locale, like SME's {@code getLabels}; other elements have none. */
  private static List<Label> labelsOf(Element element) {
    if (element instanceof GroupElement group && group.getGroup() != null) {
      return group.getGroup().getLabel();
    }
    if (element instanceof FieldElement field && field.getField() != null) {
      return field.getField().getLabel();
    }
    return List.of();
  }

  private static boolean contains(String value, String lowerCaseNeedle) {
    return value != null && value.toLowerCase(Locale.ROOT).contains(lowerCaseNeedle);
  }
}
