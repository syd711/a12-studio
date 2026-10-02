package de.a12.studio.ui.versioncontrol;

import org.jspecify.annotations.NonNull;

import java.time.Instant;

/**
 * One entry of the history list.
 *
 * @param id          full commit id
 * @param shortId     abbreviated commit id
 * @param message     first line of the commit message
 * @param fullMessage complete commit message
 * @param author      author name
 * @param date        author timestamp
 */
public record GitCommitInfo(@NonNull String id, @NonNull String shortId, @NonNull String message,
                            @NonNull String fullMessage, @NonNull String author, @NonNull Instant date) {
}
