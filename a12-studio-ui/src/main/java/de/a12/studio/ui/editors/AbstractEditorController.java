package de.a12.studio.ui.editors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

@Slf4j
abstract public class AbstractEditorController implements StudioEventListener {

  protected ProjectItem projectItem;
  private BaseTableSettings baseTableSettings;

  public void save() {
    projectItem.save();
  }

  public void load(@NonNull ProjectItem projectItem) {
    this.projectItem = projectItem;
    this.loadModel(projectItem.getModel());

    StudioEventManager.getInstance().addListener(this);
  }

  protected BaseTableSettings getBaseTableSettings() {
    if (baseTableSettings == null) {
      baseTableSettings = LocalUISettings.getTablePreference(getModelType().getValue());
    }
    return baseTableSettings;
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    this.save();
  }

  @NonNull
  abstract public ModelType getModelType();

  abstract public void loadModel(@NonNull A12Model<?> model);
}
