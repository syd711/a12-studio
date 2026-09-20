package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * A Document Model's Type Definition "mode": it either owns type definitions itself ({@link #LOCAL}, which also
 * covers ones it merely includes), or imports a Type Definition Model ({@link #IMPORT}), or has neither ({@link
 * #NONE}). The two ways of introducing type definitions are mutually exclusive in one model (see {@code
 * TypeDefinitionTableController#updateAddImportAvailability()}), mirroring SME's {@code resolveTDMode}. Shared by
 * {@link IncludeTypeDefinitionModeValidator} and the "insert from another Document Model" planner.
 */
public enum TypeDefinitionMode {
  NONE, LOCAL, IMPORT;

  public static TypeDefinitionMode modeOf(@NonNull DocumentModel documentModel) {
    List<ModelReference> references = documentModel.getModelReferences();
    boolean hasImport = references != null
        && references.stream().anyMatch(reference -> ModelReference.PURPOSE_TYPE_DEFINITIONS.equals(reference.getPurpose()));
    if (hasImport) {
      return IMPORT;
    }
    List<?> typeDefinitions = documentModel.getContent() == null ? null : documentModel.getContent().getTypeDefinitions();
    return typeDefinitions != null && !typeDefinitions.isEmpty() ? LOCAL : NONE;
  }

  /** Whether a model in mode {@code this} may Include/take over content from one in mode {@code other}. */
  public boolean isCompatibleWith(@NonNull TypeDefinitionMode other) {
    return this == NONE || other == NONE || this == other;
  }
}
