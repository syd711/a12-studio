package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.documentmodel.TypeDefinition;
import org.jspecify.annotations.NonNull;

/**
 * One row of {@link TypeDefinitionTableController}'s table: either one of the model's own type definitions
 * (editable, {@code source} blank) or one inherited transitively through the model's Include chain (see
 * {@link TransitiveTypeDefinitions}), shown read-only with {@code source} naming the models it travelled
 * through so users can tell where it actually lives.
 */
public record TypeDefinitionRow(@NonNull TypeDefinition typeDefinition, @NonNull String source, boolean editable) {

  public static TypeDefinitionRow own(@NonNull TypeDefinition typeDefinition) {
    return new TypeDefinitionRow(typeDefinition, "", true);
  }

  public static TypeDefinitionRow included(TransitiveTypeDefinitions.@NonNull Entry entry) {
    return new TypeDefinitionRow(entry.typeDefinition(), entry.sourcePath(), false);
  }
}
