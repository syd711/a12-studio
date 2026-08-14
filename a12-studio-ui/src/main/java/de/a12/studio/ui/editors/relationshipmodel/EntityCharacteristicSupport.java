package de.a12.studio.ui.editors.relationshipmodel;

import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Reads an {@link EntityCharacteristic}'s {@link Multiplicity} (tolerating a missing {@code linkConstraints}/
 * {@code multiplicity} chain) and derives the human-readable Upper Limit / Upper Limit Description summaries
 * shown by {@link RelatedEntitiesPanelController}'s row and validated against by {@link
 * de.a12.studio.ui.editors.relationshipmodel.dialogs.EntityCharacteristicDialogController}. Mirrors SME's own
 * {@code RelationshipModelEditor.json} overview columns, which derive the same text from an expression over
 * {@code role}/{@code linkConstraints.multiplicity} rather than a stored field - there is no such field on
 * {@link EntityCharacteristic} or {@link Multiplicity} to persist it in.
 */
public final class EntityCharacteristicSupport {

  private EntityCharacteristicSupport() {
  }

  public static Multiplicity getMultiplicity(EntityCharacteristic entity) {
    return entity.getLinkConstraints() != null ? entity.getLinkConstraints().getMultiplicity() : null;
  }

  public static boolean isUnbounded(EntityCharacteristic entity) {
    Multiplicity multiplicity = getMultiplicity(entity);
    return multiplicity != null && Boolean.TRUE.equals(multiplicity.getUnbounded());
  }

  public static Integer getUpperLimit(EntityCharacteristic entity) {
    Multiplicity multiplicity = getMultiplicity(entity);
    return multiplicity != null ? multiplicity.getUpperLimit() : null;
  }

  public static String describeUpperLimit(EntityCharacteristic entity) {
    if (isUnbounded(entity)) {
      return StudioBundle.get("unbounded");
    }
    Integer upperLimit = getUpperLimit(entity);
    return upperLimit != null ? String.valueOf(upperLimit) : "";
  }

  public static String describeUpperLimitExplanation(EntityCharacteristic entity) {
    String role = entity.getRole() != null && !entity.getRole().isBlank() ? entity.getRole() : StudioBundle.get("this_role");
    if (isUnbounded(entity)) {
      return StudioBundle.get("upper_limit_description_unbounded", role);
    }
    Integer upperLimit = getUpperLimit(entity);
    if (upperLimit == null) {
      return StudioBundle.get("upper_limit_description_unspecified", role);
    }
    return StudioBundle.get("upper_limit_description_bounded", role, upperLimit);
  }
}
