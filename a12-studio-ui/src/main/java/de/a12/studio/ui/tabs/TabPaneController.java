package de.a12.studio.ui.tabs;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.auth.RolesDocument;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.ui.EditorFactory;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.*;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class TabPaneController implements Initializable, StudioEventListener {

  @FXML
  private TabPane tabPane;

  private Project project;

  private boolean restoringSelection;

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    Project project = event.getProject();
    this.project = project;
    tabPane.getTabs().clear();
    restoringSelection = true;

    List<String> openedFiles = new ArrayList<>(project.getSettings().getUISettings().getOpenedFiles());
    String selectedFile = project.getSettings().getUISettings().getSelectedFile();
    restoreNextTab(project, openedFiles, 0, selectedFile);
  }

  /**
   * Restores one previously-open tab per FX pulse (via {@link Platform#runLater}) instead of looping
   * through all of them in a single call. {@link EditorFactory#create} builds a full Scene Graph (FXML +
   * controller {@code load()}) per tab, which - unlike the project/model loading that already happens on a
   * background thread before this fires - must run on the FX Application Thread. Chaining one
   * {@code runLater} per tab lets a pulse (and with it, e.g. the open-project progress dialog's
   * indeterminate animation) run between tabs instead of the FX thread being blocked solid for the whole
   * restore. Fires {@link TabsRestoredEvent} once done (or on error) so callers that need to know when the
   * restore actually finished - see {@link de.a12.studio.ui.OpenProjectProgressModel} - can wait for it
   * instead of assuming {@code projectOpened} dispatch means tabs are already showing.
   */
  private void restoreNextTab(@NonNull Project project, @NonNull List<String> openedFiles, int index, String selectedFile) {
    if (this.project != project) {
      // Project was switched/closed while restoring; abandon silently, but still signal completion so
      // nothing keeps waiting on the now-irrelevant restore.
      StudioEventManager.getInstance().fireTabsRestoredEvent(project);
      return;
    }

    if (index >= openedFiles.size()) {
      restoringSelection = false;
      StudioEventManager.getInstance().fireTabsRestoredEvent(project);
      return;
    }

    try {
      String path = openedFiles.get(index);
      File file = new File(path);
      // Resolved through the project's own tree (rather than a fresh `new ProjectItem(file)`) so this
      // shares the exact same ProjectItem/model instance as the rest of the UI, e.g. ProjectTreeController's
      // tree - which looks up nodes by model reference equality when revalidating after a save, and would
      // otherwise never find a match for edits made through a restored tab, permanently missing its
      // validation-error updates.
      ProjectItem item = file.exists() ? project.getRoot().findByPath(path) : null;
      if (item != null) {
        open(item);
        if (path.equals(selectedFile)) {
          tabPane.getSelectionModel().select(tabPane.getTabs().get(tabPane.getTabs().size() - 1));
        }
      }
    }
    catch (Exception e) {
      log.error("Failed to restore tab '{}': {}", openedFiles.get(index), e.getMessage(), e);
    }

    Platform.runLater(() -> restoreNextTab(project, openedFiles, index + 1, selectedFile));
  }

  @Override
  public void modelOpened(@NonNull ModelOpenedEvent event) {
    for (Tab existingTab : tabPane.getTabs()) {
      ProjectItem existingItem = (ProjectItem) existingTab.getUserData();
      if (existingItem != null && existingItem.getPath().equals(event.getItem().getPath())) {
        tabPane.getSelectionModel().select(existingTab);
        return;
      }
    }

    open(event.getItem());
  }

  @Override
  public void modelDeleted(@NonNull ModelDeletedEvent event) {
    String deletedPath = event.getItem().getPath();
    for (Tab tab : new ArrayList<>(tabPane.getTabs())) {
      ProjectItem tabItem = (ProjectItem) tab.getUserData();
      if (tabItem != null && isSameOrDescendant(tabItem.getPath(), deletedPath)) {
        closeTab(tab);
      }
    }
  }

  /**
   * Refreshes the tab of a renamed item in place (title + freshly rebuilt editor content) rather than
   * closing and reopening it, so the tab keeps its position and selection. The old editor is unregistered
   * via a synthetic {@link ModelClosedEvent} first - see {@link
   * de.a12.studio.ui.editors.AbstractEditorController#modelClosed} - since it would otherwise keep reacting
   * to events for a tab that visually no longer shows its content.
   */
  @Override
  public void modelRenamed(@NonNull ModelRenamedEvent event) {
    for (Tab tab : tabPane.getTabs()) {
      ProjectItem tabItem = (ProjectItem) tab.getUserData();
      if (tabItem != null && tabItem.getPath().equals(event.getOldPath())) {
        reloadTab(tab, event);
        return;
      }
    }
  }

  private void reloadTab(@NonNull Tab tab, @NonNull ModelRenamedEvent event) {
    ProjectItem item = event.getItem();
    StudioEventManager.getInstance().fireModelClosedEvent(item);

    // Set before EditorFactory.create() (not after): same reasoning as open() below - if this is the selected
    // tab, panels populated synchronously while the new controller's load() runs resolve their model via
    // Studio.getSelectedProjectItem(), which reads this tab's user data.
    tab.setText(item.getDisplayName());
    tab.setUserData(item);

    Parent content = EditorFactory.create(item);
    if (content != null) {
      tab.setContent(content);
    }

    if (project != null) {
      project.getSettings().getUISettings().removeOpenedFile(event.getOldPath());
      project.getSettings().getUISettings().addOpenedFile(item.getPath());
      if (event.getOldPath().equals(project.getSettings().getUISettings().getSelectedFile())) {
        project.getSettings().getUISettings().setSelectedFile(item.getPath());
      }
      project.getSettings().getUISettings().save();
    }
  }

  private boolean isSameOrDescendant(@NonNull String path, @NonNull String ancestorPath) {
    return path.equals(ancestorPath) || path.startsWith(ancestorPath + File.separator);
  }

  private void open(@NonNull ProjectItem item) {
    Tab tab = new Tab(item.getDisplayName());
    tab.setUserData(item);
    tab.setClosable(true);

    A12Model<?> model = item.getModel();
    if (model instanceof TypeDefinitionModel) {
      applyModelTabStyle(tab, ModelType.TYPEDEFINITION);
    }
    else if (model != null) {
      applyModelTabStyle(tab, model.getModelType());
    }
    else if (item.getAuthDocument() != null) {
      FontIcon icon = new FontIcon(item.getAuthDocument() instanceof RolesDocument
          ? Icons.ACCOUNT_KEY_OUTLINE : Icons.ACCOUNT_MULTIPLE_OUTLINE);
      icon.setIconSize(18);
      tab.setGraphic(icon);
    }
    tab.setContextMenu(createTabContextMenu(tab));
    tab.setOnClosed(closeEvent -> onTabClosed(tab));

    // Added and selected before the editor content is built (and only then handed its content below): several
    // property editor panels populate themselves synchronously while EditorFactory.create() -> controller.load()
    // runs, and resolve the model they should bind to via Studio.getSelectedProjectItem() (e.g.
    // LocalizedTextPanelController.buildLocaleFields(), used for model-header fields like
    // CustomFilterConfigurationPanelController's Header Subtitle/Filter Button Label). If this tab weren't
    // already selected by then, that lookup would still resolve to whichever tab was selected before, not `item`.
    Tab previousSelection = tabPane.getSelectionModel().getSelectedItem();
    tabPane.getTabs().add(tab);
    tabPane.getSelectionModel().select(tab);

    Parent content = EditorFactory.create(item);
    if (content == null) {
      tabPane.getTabs().remove(tab);
      if (previousSelection != null) {
        tabPane.getSelectionModel().select(previousSelection);
      }
      return;
    }
    tab.setContent(content);
    installDoubleClickHandler(tab);
  }

  /**
   * Sets the tab's icon and, for the "Enable Colorful Studio" preference (see
   * PreferenceAppGeneralPanelController / stylesheet-model-colors.css), a "model-tab-&lt;type&gt;" style
   * class (lowercase {@link ModelType#name()}) so the tab header can be tinted per model type. The
   * class is added unconditionally - stylesheet-model-colors.css only applies it while the TabPane
   * itself also carries "colorful-studio" (toggled by {@link #applyColorfulStudioSetting()}), so this
   * stays inert when the preference is off.
   */
  private void applyModelTabStyle(@NonNull Tab tab, @NonNull ModelType modelType) {
    tab.setGraphic(WidgetFactory.createModelIcon(Icons.forModelType(modelType)));
    tab.getStyleClass().add("model-tab-" + modelType.name().toLowerCase());
  }

  private void installDoubleClickHandler(@NonNull Tab tab) {
    Platform.runLater(() -> {
      Node tabNode = tab.getTabPane();
      if (tabNode != null) {
        tabNode.setOnMouseClicked(event -> {
          if (event.getClickCount() == 2) {
            ProjectItem projectItem = (ProjectItem) tab.getUserData();
            if (projectItem != null) {
              StudioEventManager.getInstance().fireModelFocusRequestedEvent(projectItem);
            }
          }
        });
      }
    });
  }

  private void onTabClosed(@NonNull Tab tab) {
    ProjectItem projectItem = (ProjectItem) tab.getUserData();
    if (project != null && projectItem != null) {
      project.getSettings().getUISettings().removeOpenedFile(projectItem.getPath());
      project.getSettings().getUISettings().save();
    }
    StudioEventManager.getInstance().fireModelClosedEvent(projectItem);
  }

  private ContextMenu createTabContextMenu(@NonNull Tab tab) {
    MenuItem close = new MenuItem(StudioBundle.get("close_tab"));
    close.setOnAction(event -> closeTab(tab));

    MenuItem closeAll = new MenuItem(StudioBundle.get("close_all"));
    closeAll.setOnAction(event -> {
      for (Tab t : new ArrayList<>(tabPane.getTabs())) {
        closeTab(t);
      }
    });

    MenuItem closeOthers = new MenuItem(StudioBundle.get("close_others"));
    closeOthers.setOnAction(event -> {
      for (Tab t : new ArrayList<>(tabPane.getTabs())) {
        if (t != tab) {
          closeTab(t);
        }
      }
    });

    return new ContextMenu(close, closeAll, closeOthers);
  }

  private void closeTab(@NonNull Tab tab) {
    tabPane.getTabs().remove(tab);
    onTabClosed(tab);
  }

  public ProjectItem getSelectedProjectItem() {
    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    return selectedTab == null ? null : (ProjectItem) selectedTab.getUserData();
  }

  public void closeSelectedTab() {
    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
      closeTab(selectedTab);
    }
  }

  public void selectNextTab() {
    int size = tabPane.getTabs().size();
    if (size < 2) {
      return;
    }
    int current = tabPane.getSelectionModel().getSelectedIndex();
    tabPane.getSelectionModel().select((current + 1) % size);
  }

  public void selectPreviousTab() {
    int size = tabPane.getTabs().size();
    if (size < 2) {
      return;
    }
    int current = tabPane.getSelectionModel().getSelectedIndex();
    tabPane.getSelectionModel().select((current - 1 + size) % size);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
    tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> onSelectionChanged(newTab));

    applyColorfulStudioSetting();
    LocalUISettings.addListener((key, value) -> {
      if (LocalUISettings.COLORFUL_STUDIO_ENABLED.equals(key)) {
        applyColorfulStudioSetting();
      }
    });
  }

  /** Toggles the "colorful-studio" style class that gates stylesheet-model-colors.css's per-tab tinting. */
  private void applyColorfulStudioSetting() {
    boolean enabled = LocalUISettings.getBoolean(LocalUISettings.COLORFUL_STUDIO_ENABLED, true);
    if (enabled) {
      if (!tabPane.getStyleClass().contains("colorful-studio")) {
        tabPane.getStyleClass().add("colorful-studio");
      }
    }
    else {
      tabPane.getStyleClass().remove("colorful-studio");
    }
  }

  private void onSelectionChanged(Tab newTab) {
    ProjectItem item = newTab == null ? null : (ProjectItem) newTab.getUserData();
    StudioEventManager.getInstance().fireTabSelectionChangedEvent(item);

    if (restoringSelection || project == null) {
      return;
    }
    project.getSettings().getUISettings().setSelectedFile(item == null ? null : item.getPath());
    project.getSettings().getUISettings().save();
  }
}
