package de.a12.studio.ui.versioncontrol;

import org.eclipse.jgit.api.errors.GitAPIException;

/** A failed git command (push, pull, stash, ...); the message is git's own output, shown to the user as-is. */
class GitPushException extends GitAPIException {

  GitPushException(String message) {
    super(message);
  }

  GitPushException(String message, Throwable cause) {
    super(message, cause);
  }
}
