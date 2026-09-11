package de.a12.studio.ui.versioncontrol;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.VersionControlSettings;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.JFXFuture;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * The commit/revert flow (message/confirmation dialog, the actual git call, the follow-up events)
 * shared by the per-tab Commit/Revert toolbar buttons ({@link
 * de.a12.studio.ui.editors.EditorFileToolbarButtonsController#onCommit}/{@code onRevert}) and the
 * Ctrl+Shift+C / Ctrl+Shift+Z shortcuts ({@link de.a12.studio.ui.StudioKeyEventHandler}), so the two
 * entry points can't drift apart.
 */
@Slf4j
public class VersionControlActions {

  private VersionControlActions() {
  }

  public static void commit(@NonNull Stage stage, @NonNull Project project, @NonNull GitService gitService,
      @NonNull GitChangedFile file) {
    VersionControlSettings settings = VersionControlSettings.load();
    String defaultMessage = settings.getLastCommitMessages().getOrDefault(project.getFolder().getAbsolutePath(), "");
    String message = WidgetFactory.showTextAreaInputDialog(stage, StudioBundle.get("versioncontrol_commit"),
        StudioBundle.get("versioncontrol_commit_message_prompt"), StudioBundle.get("commit_file_description", file.relativePath()),
        defaultMessage);
    if (message == null || message.isBlank()) {
      return;
    }
    String trimmedMessage = message.trim();
    settings.getLastCommitMessages().put(project.getFolder().getAbsolutePath(), trimmedMessage);
    settings.save();

    JFXFuture.runAsync(() -> {
          try {
            gitService.stageAndCommit(List.of(file), trimmedMessage);
          }
          catch (GitAPIException ex) {
            throw new RuntimeException(ex);
          }
        })
        .thenLater(() -> StudioEventManager.getInstance().fireGitStatusChangedEvent())
        .onErrorLater(ex -> {
          log.error("Failed to commit '{}'", file.file(), ex);
          WidgetFactory.showAlert(stage, StudioBundle.get("versioncontrol_commit_failed"), ex.getMessage());
        });
  }

  public static void revert(@NonNull Stage stage, @NonNull GitService gitService, @NonNull ProjectItem item,
      @NonNull GitChangedFile file) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage,
        StudioBundle.get("confirm_revert_file", file.relativePath()), null, null, StudioBundle.get("versioncontrol_revert"));
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }

    JFXFuture.runAsync(() -> {
          try {
            gitService.revert(List.of(file));
          }
          catch (GitAPIException ex) {
            throw new RuntimeException(ex);
          }
        })
        .thenLater(() -> onRevertCompleted(item, file))
        .onErrorLater(ex -> {
          log.error("Failed to revert '{}'", file.file(), ex);
          WidgetFactory.showAlert(stage, StudioBundle.get("versioncontrol_revert_failed"), ex.getMessage());
        });
  }

  /**
   * Mirrors {@link de.a12.studio.ui.versioncontrol.VersioncontrolPanelController}'s own post-revert
   * handling: a {@link ChangeStatus#NEW} file was deleted by the revert, so it's treated like any
   * other deletion (closes the tab, if open); every other file was checked out from HEAD, so {@link
   * ProjectItem#reload()} picks up its new content and {@link StudioEventManager#fireModelRevertedEvent}
   * lets an open tab's editor rebuild from it.
   */
  private static void onRevertCompleted(@NonNull ProjectItem item, @NonNull GitChangedFile file) {
    if (file.status() == ChangeStatus.NEW) {
      StudioEventManager.getInstance().fireModelDeletedEvent(item);
    }
    else {
      item.reload();
      StudioEventManager.getInstance().fireModelRevertedEvent(item);
    }
    StudioEventManager.getInstance().fireGitStatusChangedEvent();
  }
}
