package de.a12.studio.models.features;

/** Outcome of an {@link ApplicationGroupFeature} run, for display to the user. */
public record ApplicationGroupResult(String groupName, int renamedCount, int referencesUpdatedCount) {
}
