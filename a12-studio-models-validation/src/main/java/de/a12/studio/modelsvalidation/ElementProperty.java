package de.a12.studio.modelsvalidation;

/**
 * Tags for {@link ModelValidationError#property()} identifying which property editor panel a document model
 * element-level error concerns, so panels that share the same bound {@code Element} (e.g. a field's General
 * Information, Type Definition and Data Type Configuration panels) can each show only their own errors. Shared
 * between the validators that assign these tags and the UI panels that filter by them.
 */
public final class ElementProperty {

  /** The element's name/id, or a structural problem with the element that has no more specific home. */
  public static final String GENERAL = "general";

  /** The element's field type (including a Type Definition reference) and required/global/transient flags. */
  public static final String TYPE = "type";

  /** The data-type-specific configuration for the element's current field type (limits, enumeration values, pattern). */
  public static final String DATA_TYPE = "dataType";

  /** The localized error text shown when a string field's pattern doesn't match. */
  public static final String ERROR_MESSAGE = "errorMessage";

  /** A group's repeatability and index field. */
  public static final String GROUP_PROPERTIES = "groupProperties";

  /** An Include group's referenced document model. */
  public static final String INCLUDE_REFERENCE = "includeReference";

  /** A validation Rule's error entity/condition/code/severity. */
  public static final String RULE_PROPERTIES = "ruleProperties";

  /** A Computation's computed field and alternatives (precondition/operation). */
  public static final String COMPUTATION_PROPERTIES = "computationProperties";

  private ElementProperty() {
  }
}
