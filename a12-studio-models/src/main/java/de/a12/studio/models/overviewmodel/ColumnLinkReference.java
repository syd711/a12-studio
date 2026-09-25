package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
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

  /**
   * The id of the Document Model the column's {@code elementRef} lives in, resolved against {@code
   * relationshipContent}: for {@link #TYPE_LINK} the relationship's own {@code linkDocumentModel} (the fields
   * attached to the link itself, e.g. {@code TeamPerson_LinkFields_Dc}), otherwise - {@link #TYPE_CHILD} - the
   * Document Model of the {@code targetRole} entity (e.g. {@code Team_Dc}). Mirrors how a Query Model link
   * splits its projection into {@code fields} (target document) and {@code linkDocumentFields} (link
   * document). {@code null} if it can't be determined.
   */
  public String resolveDocumentModelId(RelationshipModelContent relationshipContent) {
    if (relationshipContent == null) {
      return null;
    }
    if (TYPE_LINK.equals(type)) {
      return relationshipContent.getLinkDocumentModelValue();
    }
    if (targetRole == null || relationshipContent.getEntityCharacteristics() == null) {
      return null;
    }
    return relationshipContent.getEntityCharacteristics().stream()
        .filter(entity -> targetRole.equals(entity.getRole()))
        .map(EntityCharacteristic::getDocumentModel)
        .findFirst()
        .orElse(null);
  }
}
