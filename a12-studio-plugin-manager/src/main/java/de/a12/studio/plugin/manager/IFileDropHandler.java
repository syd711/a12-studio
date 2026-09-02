package de.a12.studio.plugin.manager;

import de.a12.studio.models.projects.ProjectItem;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.List;

/**
 * Extension point interface for plugins that want to intercept files dropped onto the
 * a12-studio window.
 *
 * <p>When the user drops one or more files, the studio first checks whether the built-in
 * AI handler can process them. If not, it queries all registered {@code IFileDropHandler}
 * implementations in plugin-load order. The first plugin whose {@link #canHandle} returns
 * {@code true} for a given file takes ownership of it; the remaining plugins are not
 * consulted for that file.
 *
 * <p>Register this extension point in your {@code plugin.json} under the name
 * {@code "fileDrop"}:
 *
 * <pre>
 * {
 *   "extensionPoints": [
 *     { "name": "fileDrop", "class": "com.example.plugin.MyFileDropHandler" }
 *   ]
 * }
 * </pre>
 */
public interface IFileDropHandler {

  /**
   * Returns the MIME types (and/or file-extension pseudo-MIME patterns) that this handler
   * accepts, e.g. {@code "application/vnd.ms-access"}, {@code "application/vnd.ms-excel"},
   * or the extension-based patterns {@code "*.accdb"}, {@code "*.mdb"}, {@code "*.xlsx"},
   * {@code "*.xls"}.
   *
   * <p>The studio uses this list for a quick pre-filter; {@link #canHandle} is the
   * authoritative check.
   *
   * @return non-null, non-empty list of accepted MIME/extension patterns
   */
  @NonNull
  List<String> getAcceptedMimeTypes();

  /**
   * Returns {@code true} if this handler is willing to process the given file.
   *
   * <p>This method must be fast (no I/O) – it is called on the JavaFX application thread
   * during the drag-over phase. Use only the file name / extension for the decision.
   *
   * @param file the candidate file (existence is not guaranteed at this point)
   * @return {@code true} if this handler will accept {@code file}
   */
  boolean canHandle(@NonNull File file);

  /**
   * Processes the dropped file.
   *
   * <p>Called on the JavaFX application thread after the user completes the drop gesture
   * and this handler has been selected via {@link #canHandle}. Implementations typically
   * open an import dialog pre-populated with the dropped file.
   *
   * @param owner        the primary stage, used as owner for any modal dialogs
   * @param targetFolder the project-tree folder that was active when the file was dropped,
   *                     or the project root if no folder was selected
   * @param file         the dropped file
   */
  void handle(@NonNull Stage owner, @NonNull ProjectItem targetFolder, @NonNull File file);
}
