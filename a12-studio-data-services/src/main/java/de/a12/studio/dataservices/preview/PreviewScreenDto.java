package de.a12.studio.dataservices.preview;

import java.util.List;

public record PreviewScreenDto(String id, String name, String title, List<PreviewScreenElementDto> elements) {
}
