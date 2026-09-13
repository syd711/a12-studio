package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * One entry of {@link Column#getLinkReferences()}: marks a column as sourced through a relationship hop
 * (a Relationship UI Model's Available/Selected Items overview), naming the relationship, the role the
 * *other* document plays in it, and whether that document is the relationship's own {@code CHILD} or a
 * plain {@code LINK}ed one. No editor UI yet - mapped purely for lossless round-tripping.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ColumnLinkReference {

  public static final String TYPE_CHILD = "CHILD";
  public static final String TYPE_LINK = "LINK";

  private String relationship;
  private String targetRole;
  private String type;
}
