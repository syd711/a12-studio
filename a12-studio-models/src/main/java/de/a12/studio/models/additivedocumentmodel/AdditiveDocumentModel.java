package de.a12.studio.models.additivedocumentmodel;

import de.a12.studio.models.documentmodel.DocumentModel;

// An "Additive Document Model" is not a distinct JSON schema: it is a DocumentModel whose header carries
// the "additive-document" annotation (see the a12 platform's Combination Model Addition step). Its own
// content is a self-contained modelRoot with no reference to whichever Document Model it is combined
// onto - that link only exists on the consuming Combination Model (header purpose "combination-base" /
// "combination-addition"). This marker subclass mirrors TypeDefinitionModel: it lets the UI dispatch on a
// distinct type (icon, tree toolbar) instead of re-checking the annotation everywhere; ModelFactory is
// what actually decides which of the two classes to instantiate.
public class AdditiveDocumentModel extends DocumentModel {
}
