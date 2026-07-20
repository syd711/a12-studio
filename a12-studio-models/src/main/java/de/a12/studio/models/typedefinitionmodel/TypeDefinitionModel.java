package de.a12.studio.models.typedefinitionmodel;

import de.a12.studio.models.documentmodel.DocumentModel;

// A "Type Definition Model" is not a distinct JSON schema: it is a DocumentModel whose header carries
// the "tdonly" annotation and whose modelRoot stays empty, only typeDefinitions is used. This marker
// subclass lets the UI (e.g. EditorFactory) dispatch on a distinct type instead of re-checking the
// annotation everywhere; ModelFactory is what actually decides which of the two classes to instantiate.
public class TypeDefinitionModel extends DocumentModel {
}
