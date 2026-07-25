package de.a12.studio.ui.preview;

import de.a12.studio.dataservices.preview.PreviewApplicationDto;
import de.a12.studio.dataservices.preview.PreviewSceneDto;
import org.jspecify.annotations.Nullable;

/**
 * UI-layer response shape for the {@code /preview/{modelId}/data} endpoint: the application-wide preview data
 * (module list, initial activity) plus the currently selected scene's resolved region tree, if any was
 * requested. Kept out of {@code a12-studio-data-services} since it's just a wire-format aggregation of that
 * module's two DTOs, not a data transformation of its own.
 */
public record PreviewDataResponse(PreviewApplicationDto application, @Nullable PreviewSceneDto scene) {
}
