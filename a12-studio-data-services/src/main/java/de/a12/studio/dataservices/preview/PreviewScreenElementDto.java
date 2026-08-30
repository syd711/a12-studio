package de.a12.studio.dataservices.preview;

import java.util.List;

/**
 * A node in a Form Model screen's element tree. {@code Section}/{@code MultiColumnSection} nest further
 * elements in {@code children} (fields empty); {@code ControlGrid} and the repeat element types resolve their
 * bound Document Model fields directly into {@code fields} (children empty). Any other/unrecognized element
 * type carries neither and renders as an opaque, unlabeled block of its {@code type}.
 */
public record PreviewScreenElementDto(String type, String title, List<PreviewFieldDto> fields, List<PreviewScreenElementDto> children) {
}
