package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.validators.TransitiveTypeDefinitions;
import org.jspecify.annotations.NonNull;

/**
 * One row of {@link TypeDefinitionTableController}'s table: either one of the model's own type definitions
 * (editable, other fields blank/false) or one inherited transitively through the model's Include/Import graph
 * (see {@link TransitiveTypeDefinitions}), shown read-only with {@code source} naming the mechanism and models
 * it travelled through so users can tell where it actually lives, {@code ownerModelId} - the last model in
 * that chain, i.e. the one whose {@code typeDefinitions} it's actually declared in - so it can be opened
 * directly, {@code imported} distinguishing an Import (a Type Definition Model, removable only via
 * "Delete Import") from an Include (a regular Document Model, removable only by editing that Include), and
 * {@code includedImported} narrowing that further to the "merely informational" case (see {@link
 * TransitiveTypeDefinitions.Entry}) - an included model's own import, shown but not directly usable in the
 * current model until imported directly.
 */
public record TypeDefinitionRow(@NonNull TypeDefinition typeDefinition, @NonNull String source, @NonNull String ownerModelId,
                                 boolean imported, boolean includedImported, boolean editable) {

  public static TypeDefinitionRow own(@NonNull TypeDefinition typeDefinition) {
    return new TypeDefinitionRow(typeDefinition, "", "", false, false, true);
  }

  public static TypeDefinitionRow included(TransitiveTypeDefinitions.@NonNull Entry entry) {
    String label = (entry.includedImported() ? "Included Import: " : entry.imported() ? "Import: " : "Include: ") + entry.sourcePath();
    return new TypeDefinitionRow(entry.typeDefinition(), label, entry.ownerModelId(), entry.imported(), entry.includedImported(), false);
  }
}
