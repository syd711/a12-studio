package de.a12.studio.models.auth;

// Marker for the parsed content of a non-model auth YAML file (roles.yaml / users.yaml). Unlike
// A12Model, these files have no "header" object, so they intentionally sit outside the A12Model
// hierarchy - see ProjectItem.authDocument.
public interface AuthDocument {
}
