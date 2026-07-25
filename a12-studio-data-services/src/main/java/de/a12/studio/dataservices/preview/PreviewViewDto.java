package de.a12.studio.dataservices.preview;

import java.util.List;

public record PreviewViewDto(String name, String modelName, String modelType, List<PreviewFieldDto> fields) {
}
