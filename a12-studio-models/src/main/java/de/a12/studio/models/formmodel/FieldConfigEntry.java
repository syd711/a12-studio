package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Annotation;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Model-wide configuration for a Document Model field, applying to every Control/column referencing it
// (as opposed to per-Control settings, which take precedence).
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FieldConfigEntry {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText label;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer placeholder;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer hint;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String initialValue;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer suffix;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String exposition;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DependentConfig dependentField;
  // Constrains which of this field's own Enumeration values are offered, based on a master field's value.
  // Distinct from dependentField, which only affects visibility/readonly, not the set of allowed values.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DependentEnumeration dependentEnumeration;
  // Sources this field's enumeration options from an external URL instead of the Document Model's own enum.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ExternalEnumeration externalEnumeration;
  // "AREA" is the only value the reference platform currently defines; modeled as a free string since more
  // may exist server-side, matching the pattern used for exposition/readonlyPresentation above.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String formatting;
  // Password-style masking for this field's input.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean secret;
  // "Select all" toggle offered on a multi-select control for this field.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableSelectAll;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Annotation> annotations = new ArrayList<>();
  private String elementRef;
  // Attachment-field display configuration, e.g. the icon shown before a file is uploaded. Lives on the entry
  // whose elementRef is the attachment *group* (usageType "attachment"), see SME's "Attachment Settings".
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private AttachmentConfig attachmentConfig;

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  public static class AttachmentConfig {
    // "default", "image", "text", "spreadsheet", "pdf", "video", "sound" or "none"; absent = "default".
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String placeholderIcon;
    // Comma-separated MIME types suggested by the file picker (e.g. "image/jpeg, video/*"); not enforced.
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String accept;
    // What a click on an already uploaded attachment does: "replace" (absent = default) or "download".
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String defaultAction;

    /** Whether no attachment setting is set, i.e. this object would serialize as an empty {@code {}}. */
    @JsonIgnore
    public boolean isBlank() {
      return isEmpty(placeholderIcon) && isEmpty(accept) && isEmpty(defaultAction);
    }

    private static boolean isEmpty(String value) {
      return value == null || value.isEmpty();
    }
  }
}
