package de.a12.studio.ui.versioncontrol;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.VersionControlSettings;
import de.a12.studio.ui.RootController;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ProgressDialog;
import de.a12.studio.ui.components.ProgressResultModel;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
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

    GitOperationProgressModel progressModel = new GitOperationProgressModel(StudioBundle.get("versioncontrol_committing"),
        () -> gitService.stageAndCommit(List.of(file), trimmedMessage));
    ProgressResultModel result = ProgressDialog.createProgressDialog(stage, progressModel);
    if (result.isSuccess()) {
      StudioEventManager.getInstance().fireGitStatusChangedEvent();
    }
    else if (!result.isCancelled()) {
      log.error("Failed to commit '{}'", file.file());
    }
  }

  public static void revert(@NonNull Stage stage, @NonNull GitService gitService, @NonNull GitChangedFile file) {
    Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(stage,
        StudioBundle.get("confirm_revert_file", file.relativePath()), null, null, StudioBundle.get("versioncontrol_revert"));
    if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
      return;
    }

    GitOperationProgressModel progressModel = new GitOperationProgressModel(StudioBundle.get("versioncontrol_reverting"),
        () -> gitService.revert(List.of(file)));
    ProgressResultModel result = ProgressDialog.createProgressDialog(stage, progressModel);
    if (result.isSuccess()) {
      onRevertCompleted();
    }
    else if (!result.isCancelled()) {
      log.error("Failed to revert '{}'", file.file());
    }
  }

  /** Replaces {@code file} with its version from {@code commit}, after confirmation. */
  public static void restoreVersion(@NonNull Stage stage, @NonNull GitService gitService, @NonNull File file,
      @NonNull GitCommitInfo commit) {
    Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(stage,
        StudioBundle.get("confirm_restore_version", file.getName(), commit.shortId()), null, null,
        StudioBundle.get("versioncontrol_restore_version"));
    if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
      return;
    }

    GitOperationProgressModel progressModel = new GitOperationProgressModel(StudioBundle.get("versioncontrol_restoring"),
        () -> gitService.restoreFromCommit(file, commit.id()));
    ProgressResultModel result = ProgressDialog.createProgressDialog(stage, progressModel);
    if (result.isSuccess()) {
      onRevertCompleted();
    }
    else if (!result.isCancelled()) {
      log.error("Failed to restore '{}' from commit {}", file, commit.id());
    }
  }

  /**
   * Mirrors {@link de.a12.studio.ui.versioncontrol.VersioncontrolPanelController}'s own post-revert
   * handling: a revert isn't limited to the one file that triggered it (a checkout can touch
   * whatever HEAD says that file's tree looked like), so rather than patching up just {@code file},
   * {@link de.a12.studio.ui.RootController#reloadProject()} reloads the whole project from disk and
   * rebuilds every open tab/detached window's editor content from it - closing any tab whose file
   * the revert deleted.
   */
  private static void onRevertCompleted() {
    RootController rootController = Studio.getRootController();
    if (rootController != null) {
      rootController.reloadProject();
    }
    StudioEventManager.getInstance().fireGitStatusChangedEvent();
  }
}
