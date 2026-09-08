package de.a12.studio.models.selectionmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Deliberately empty: a12-studio has no Selection Model editor yet (see {@code ModelType#SELECTION}), so this
 * only needs to round-trip enough for a {@link SelectionModel} to be discovered and referenced by id/type from
 * elsewhere in a project (e.g. a {@link de.a12.studio.models.combineddocumentmodel.CombinationStep}'s
 * SelectionModel). Every real content field is ignored rather than modeled, and never re-serialized by this app.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectionModelContent {
}
