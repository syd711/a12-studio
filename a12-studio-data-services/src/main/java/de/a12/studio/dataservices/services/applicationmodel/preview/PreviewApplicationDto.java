package de.a12.studio.dataservices.services.applicationmodel.preview;

import java.util.List;
import java.util.Map;

public record PreviewApplicationDto(List<PreviewModuleDto> modules, PreviewRegionDto regionTree, Map<String, String> initialActivity) {
}
