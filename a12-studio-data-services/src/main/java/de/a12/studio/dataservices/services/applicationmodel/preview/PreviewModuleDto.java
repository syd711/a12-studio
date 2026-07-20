package de.a12.studio.dataservices.services.applicationmodel.preview;

import java.util.List;

/**
 * {@code defaultSceneName} is the first flow's first scene, used by the preview frontend to pick a scene to
 * render as soon as a module is selected, without needing its own flow/scene picker in v1.
 */
public record PreviewModuleDto(String name, List<PreviewLabelDto> label, String defaultSceneName) {
}
