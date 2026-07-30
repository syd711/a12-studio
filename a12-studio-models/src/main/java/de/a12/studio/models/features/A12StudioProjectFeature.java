package de.a12.studio.models.features;

import de.a12.studio.models.projects.Project;

/**
 * A project-wide bulk operation that reads and/or mutates every model in a {@link Project}.
 *
 * @param <R> the result type describing what the feature did, for display to the user
 */
public interface A12StudioProjectFeature<R> {

  R apply(Project project) throws A12StudioFeatureException;
}
