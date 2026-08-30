package de.a12.studio.ui.preview;

import de.a12.studio.dataservices.preview.PreviewFormDto;

/**
 * UI-layer response shape for the Form Model preview's {@code /preview/{modelId}/data} endpoint. Kept out of
 * {@code a12-studio-data-services} for the same reason as {@link PreviewDataResponse}: it's just a wire-format
 * wrapper around {@link PreviewFormDto}, not a data transformation of its own.
 */
public record FormPreviewDataResponse(PreviewFormDto form) {
}
