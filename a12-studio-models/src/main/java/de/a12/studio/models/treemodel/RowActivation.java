package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What a click on a row of a node type does (SME "Row Activation", tree model version 11.0.0, replacing the former
 * {@code defaultRowAction}). Absent from the node = the Tree Engine's own behavior (select the node for view/edit).
 * <ul>
 *   <li>{@link #TYPE_EVENT}: fires {@link #getEvent()} - a built-in event behaves like the row action button of the same
 *       event, any other name is handed to the application as a custom row click.</li>
 *   <li>{@link #TYPE_INSERT}: triggers an insert at {@link #getPosition()} (as child when absent), optionally of {@link
 *       #getDocumentModelRef()}.</li>
 *   <li>{@link #TYPE_NON_INTERACTIVE}: the row does nothing when clicked and is skipped by the keyboard.</li>
 * </ul>
 */
@Getter
@Setter
public class RowActivation {

  public static final String TYPE_NON_INTERACTIVE = "non_interactive";
  public static final String TYPE_EVENT = "event";
  public static final String TYPE_INSERT = "insert";

  private String type;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String event;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String position;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String documentModelRef;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }
}
