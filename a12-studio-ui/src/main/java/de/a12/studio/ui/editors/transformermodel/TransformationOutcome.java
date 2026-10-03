package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * What one run of a Transformer Model's transformation brought back (see {@link TransformerRun}): the generated
 * Document Model, the issues the transformer reported while generating it, and the XSD discovery the editor's
 * suggestion lists come from. In SME this is the {@code EditorTransformerModel} (last transformation successful,
 * its issues, the generated document).
 *
 * @param state         whether the transformer was reached at all
 * @param message       why it was not ({@link State#INCOMPLETE}, {@link State#UNAVAILABLE}), otherwise null
 * @param success       the transformer's own success flag - false when no Document Model could be generated
 * @param documentModel the generated Document Model, null when none was generated
 * @param issues        everything the transformer reported, in order
 * @param discovery     what the XSD offers; {@link Discovery#EMPTY} when the discovery failed
 */
public record TransformationOutcome(@NonNull State state, @Nullable String message, boolean success,
    @Nullable DocumentModel documentModel, @NonNull List<Issue> issues, @NonNull Discovery discovery) {

  public enum State {
    /** The model does not say what to transform yet (no main XSD), or the XSD cannot be found: nothing was run. */
    INCOMPLETE,
    /** The Simple Model Editor backend that runs the transformer could not be started or reached. */
    UNAVAILABLE,
    /** The transformer answered (successfully or not). */
    DONE
  }

  public static final String SEVERITY_ERROR = "ERROR";
  public static final String SEVERITY_WARNING = "WARNING";
  public static final String SEVERITY_INFO = "INFO";

  /** One message of the transformer. {@code source} is the part of the configuration it is about, e.g. {@code /content/TypeMapping}. */
  public record Issue(@NonNull String severity, @NonNull String message, @Nullable String source) {
  }

  /** An XSD simple type with the A12 data type the transformer maps it to by default. */
  public record SimpleType(@NonNull String name, @Nullable String xsType, @Nullable String defaultMapping) {
  }

  /** An enumeration value of the XSD, the paths of the fields it occurs in and the type definitions it belongs to. */
  public record EnumValue(@NonNull String value, @NonNull List<String> fieldPaths, @NonNull List<String> typeDefinitionIds) {
  }

  /** The information SME's editor offers as suggestions (the {@code xsd:*} sources of its autocomplete fields). */
  public record Discovery(@NonNull List<String> rootElements, @NonNull List<SimpleType> simpleTypes, @NonNull List<String> elementPaths,
      @NonNull Map<String, List<String>> patternFields, @NonNull Map<String, EnumValue> enumValues) {

    public static final Discovery EMPTY = new Discovery(List.of(), List.of(), List.of(), Map.of(), Map.of());

    /** The patterns of the XSD's String fields (the regex strings the {@code PatternErrors} entries refer to). */
    public List<String> patterns() {
      return List.copyOf(patternFields.keySet());
    }
  }

  public static TransformationOutcome incomplete(@NonNull String message) {
    return new TransformationOutcome(State.INCOMPLETE, message, false, null, List.of(), Discovery.EMPTY);
  }

  public static TransformationOutcome unavailable(@NonNull String message) {
    return new TransformationOutcome(State.UNAVAILABLE, message, false, null, List.of(), Discovery.EMPTY);
  }

  public boolean hasDocumentModel() {
    return documentModel != null;
  }

  public long count(@NonNull String severity) {
    return issues.stream().filter(issue -> severity.equals(issue.severity())).count();
  }
}
