package de.a12.studio.models.transformermodel;

import org.jspecify.annotations.Nullable;

/**
 * The A12 data types an XSD simple type can be mapped to ({@code TypeMapping.a12Type}, SME's {@code a12Type}
 * enumeration), each with its {@code superType} - the kernel grouping SME's type-mapping rules compare against
 * ({@code [a12Type->superType] == "String"} etc., the map embedded in the installed
 * {@code TransformerConfigModel.validation.js}).
 */
public enum A12DataType {

  STRING("StringType", "String"),
  NUMBER("NumberType", "Number"),
  DATE("DateType", "Date"),
  DATE_TIME("DateTimeType", "Date"),
  TIME("TimeType", "Date"),
  CONFIRM("ConfirmType", "Boolean"),
  BOOLEAN("BooleanType", "Boolean"),
  ENUMERATION("EnumerationType", "Enumeration"),
  STRING_WITH_XS_PATTERN("StringWithXsPatternType", "String"),
  ENUM_FOR_BOOLEAN("EnumForBooleanType", "Enumeration"),
  ENUM_FOR_STRING("EnumForStringType", "Enumeration");

  public static final String SUPER_TYPE_STRING = "String";
  public static final String SUPER_TYPE_NUMBER = "Number";
  public static final String SUPER_TYPE_ENUMERATION = "Enumeration";

  private final String value;
  private final String superType;

  A12DataType(String value, String superType) {
    this.value = value;
    this.superType = superType;
  }

  /** The value as written to {@code TypeMapping.a12Type}, e.g. {@code "StringType"}. */
  public String getValue() {
    return value;
  }

  public String getSuperType() {
    return superType;
  }

  /** Whether the {@code StringType} configuration group may be filled for this type (SME's superType check). */
  public boolean acceptsStringConfig() {
    return SUPER_TYPE_STRING.equals(superType);
  }

  public boolean acceptsNumberConfig() {
    return SUPER_TYPE_NUMBER.equals(superType);
  }

  public boolean acceptsEnumerationConfig() {
    return SUPER_TYPE_ENUMERATION.equals(superType);
  }

  /**
   * Whether SME's editor offers the String configuration fields (min/max length, pattern, line breaks, sorting) -
   * only for plain {@code StringType}, not for {@code StringWithXsPatternType} (see the dependent-field cases of
   * {@code TransformerModelTransformationEditor.json}).
   */
  public boolean showsStringConfig() {
    return this == STRING;
  }

  public boolean showsNumberConfig() {
    return this == NUMBER;
  }

  /** SME offers the enumeration sorting checkbox for {@code EnumerationType} and {@code EnumForStringType}. */
  public boolean showsEnumerationConfig() {
    return this == ENUMERATION || this == ENUM_FOR_STRING;
  }

  /** The type for a stored {@code a12Type} value, or null for none/unknown. */
  @Nullable
  public static A12DataType fromValue(@Nullable String value) {
    for (A12DataType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return null;
  }
}
