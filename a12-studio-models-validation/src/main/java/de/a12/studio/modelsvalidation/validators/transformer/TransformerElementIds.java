package de.a12.studio.modelsvalidation.validators.transformer;

/**
 * The {@link de.a12.studio.modelsvalidation.ModelValidationError#elementId()} scheme of the Transformer Model's
 * validators, shared with the editor's panels so each row can show the errors that are about it: a path into the
 * model's JSON, list entries addressed by position ({@code content/TypeMapping/2/a12Type}).
 */
public final class TransformerElementIds {

  public static final String MAIN_XSD = "content/Cmd/mainXsd";
  public static final String ROOT_ELEMENT = "content/Cmd/rootElement";

  public static final String SUPPORTED_CHARACTERS = "content/Configuration/supportedCharacters";

  public static final String CODE_LISTS_URI_VERSION_LIST = "content/CodeLists/UriVersionList";
  public static final String CODE_LISTS_ELEMENT_NAMES = "content/CodeLists/ElementNamesInXsd";

  public static final String TYPE_MAPPING = "content/TypeMapping";
  public static final String RENAME_PATHS = "content/RenamePaths";
  public static final String DELETE_PATHS = "content/DeletePaths";
  public static final String PATTERN_ERRORS = "content/PatternErrors";
  public static final String ENUM_LABELS = "content/EnumLabels";

  private TransformerElementIds() {
  }

  /** {@code field} may be nested, e.g. {@code StringType/minLength}. */
  public static String typeMapping(int index, String field) {
    return TYPE_MAPPING + "/" + index + "/" + field;
  }

  public static String renamePath(int index, String field) {
    return RENAME_PATHS + "/" + index + "/" + field;
  }

  public static String deletePath(int index) {
    return DELETE_PATHS + "/" + index + "/path";
  }

  public static String patternError(int index, String field) {
    return PATTERN_ERRORS + "/" + index + "/" + field;
  }

  /** The locale of the {@code errorIndex}-th error message of pattern error {@code index}. */
  public static String patternErrorLocale(int index, int errorIndex) {
    return PATTERN_ERRORS + "/" + index + "/errors/" + errorIndex + "/locale";
  }

  public static String enumLabel(int index, String field) {
    return ENUM_LABELS + "/" + index + "/" + field;
  }

  /** The locale of the {@code replacementIndex}-th display text of enumeration label {@code index}. */
  public static String enumLabelLocale(int index, int replacementIndex) {
    return ENUM_LABELS + "/" + index + "/replacements/" + replacementIndex + "/locale";
  }
}
