package de.a12.studio.dataservices.preview;

import java.util.List;

public record PreviewRegionDto(String name, String layout, List<PreviewViewDto> views, List<PreviewRegionDto> subRegions) {
}
