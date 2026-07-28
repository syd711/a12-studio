package de.a12.studio.ui.editors.auth;

import de.a12.studio.models.auth.AuthDocument;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.editors.EditorFileToolbarButtonsController;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

// Sibling to AbstractEditorController, keyed on AuthDocument instead of A12Model<?> - auth YAML
// files (roles.yaml/users.yaml) have no "header" object, so they don't fit the A12Model hierarchy.
@Slf4j
abstract public class AbstractAuthFileEditorController implements StudioEventListener {

  protected ProjectItem projectItem;

  @FXML
  protected EditorFileToolbarButtonsController fileToolbarButtonsController;

  public void save() {
    projectItem.save();
  }

  public void load(@NonNull ProjectItem projectItem) {
    this.projectItem = projectItem;
    this.loadDocument(projectItem.getAuthDocument());

    if (fileToolbarButtonsController != null) {
      fileToolbarButtonsController.setFileSupplier(() -> projectItem.getFile());
    }

    StudioEventManager.getInstance().addListener(this);
  }

  @FXML
  private void onReload() {
    projectItem.reload();
    this.loadDocument(projectItem.getAuthDocument());
  }

  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    if (event.getItem().equals(projectItem)) {
      StudioEventManager.getInstance().removeListener(this);
    }
  }

  abstract public void loadDocument(@NonNull AuthDocument document);
}
