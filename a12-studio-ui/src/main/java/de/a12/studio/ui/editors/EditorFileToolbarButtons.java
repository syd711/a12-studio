package de.a12.studio.ui.editors;

import de.a12.studio.ui.util.SystemUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

/**
 * Reusable toolbar component providing the "Edit File" and "Open Model Folder" buttons
 * with their preceding separator. Embed in any editor toolbar via {@code fx:include}.
 *
 * <p>After loading, call {@link #setFileSupplier(Supplier)} so the component knows
 * which file to act on — typically {@code () -> projectItem.getFile()}.
 */
@Slf4j
public class EditorFileToolbarButtons extends VBox {

  private Supplier<File> fileSupplier;

  public EditorFileToolbarButtons() {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/de/a12/studio/ui/editors/editor-file-toolbar-buttons.fxml"));
    loader.setRoot(this);
    loader.setController(this);
    try {
      loader.load();
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Provide the file this component should open/edit.
   * Call this after the owning controller's {@code projectItem} is available.
   */
  public void setFileSupplier(Supplier<File> fileSupplier) {
    this.fileSupplier = fileSupplier;
  }

  @FXML
  private void onFileEdit(ActionEvent e) {
    if (fileSupplier != null) {
      SystemUtil.editFile(fileSupplier.get());
    }
  }

  @FXML
  private void onFileOpen(ActionEvent e) {
    if (fileSupplier != null) {
      SystemUtil.openFile(fileSupplier.get());
    }
  }
}
