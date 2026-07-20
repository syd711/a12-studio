package de.a12.studio.dataservices.services.applicationmodel.preview;

import java.util.List;

public record PreviewRegionDto(String name, String layout, List<String> views, List<PreviewRegionDto> subRegions) {
}
