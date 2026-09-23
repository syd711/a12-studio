package de.a12.studio.models.composeddocumentmodel;

import de.a12.studio.models.documentmodel.DocumentModel;

// A "Composed Document Model" (CdM) is not a distinct JSON schema: it is a DocumentModel whose header carries
// the "cdm.queryRoot" annotation (value = the id of the root Document Model the CDM queries from), plus a chain
// of "cdm.relationship"/"cdm.sourceRole"/"cdm.targetRole"/"cdm.targetDocumentModel" annotations describing the
// relationships it traverses - see ComposedDocumentModelResolver. This marker subclass mirrors
// AdditiveDocumentModel: it lets the UI dispatch on a distinct type (settings panel, tree) instead of
// re-checking the annotation everywhere; ModelFactory is what actually decides which of the two classes to
// instantiate.
public class ComposedDocumentModel extends DocumentModel {
}
