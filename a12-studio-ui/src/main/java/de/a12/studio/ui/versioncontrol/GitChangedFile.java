package de.a12.studio.ui.versioncontrol;

import java.io.File;

/**
 * A single project file with an outstanding git change.
 *
 * @param file         the absolute file (may not exist on disk for a {@link ChangeStatus#DELETED} file)
 * @param relativePath path relative to the opened project's folder, forward-slash normalized
 * @param status       the simplified change classification
 */
public record GitChangedFile(File file, String relativePath, ChangeStatus status) {
}
