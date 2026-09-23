package de.a12.studio.ui.tabs;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModel;
import de.a12.studio.models.auth.RolesDocument;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.ui.EditorFactory;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ProgressDialog;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.*;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
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

  /**
   * Set for the duration of {@link #loadTabContent}'s own {@code select(tab)} call. That call can
   * synchronously re-enter {@link #onSelectionChanged} (selecting a tab that wasn't already selected fires
   * the selection listener inline, not on a later pulse), which would otherwise call back into
   * {@link #loadTabContentWithProgress} for the very tab already being loaded - building its content twice
   * and silently discarding the first (never-shown, never-cleaned-up) editor instance.
   */
  private boolean loadingTabContent;

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
   * through all of them in a single call. Each restored tab is added as an empty shell only - see
   * {@link #createTabShell} - without building its editor content: {@link EditorFactory#create} builds a
   * full Scene Graph (FXML + controller {@code load()}) per tab, which is the expensive part, so it is
   * deferred until the tab actually becomes selected (see {@link #loadTabContent}) instead of paid upfront
   * for every restored tab. Chaining one {@code runLater} per tab still lets a pulse (and with it, e.g. the
   * open-project progress dialog's indeterminate animation) run between tabs instead of the FX thread being
   * blocked solid for the whole restore. Fires {@link TabsRestoredEvent} once done (or on error) so callers
   * that need to know when the restore actually finished - see {@link de.a12.studio.ui.OpenProjectProgressModel}
   * - can wait for it instead of assuming {@code projectOpened} dispatch means tabs are already showing.
   */
  private void restoreNextTab(@NonNull Project project, @NonNull List<String> openedFiles, int index, String selectedFile) {
    if (this.project != project) {
      // Project was switched/closed while restoring; abandon silently, but still signal completion so
      // nothing keeps waiting on the now-irrelevant restore.
      StudioEventManager.getInstance().fireTabsRestoredEvent(project);
      return;
    }

    if (index >= openedFiles.size()) {
      // Selected once, here, rather than as each tab is added above: only the selected tab's editor
      // content is actually built (see selectRestoredTab/loadTabContent), so building it mid-loop would
      // just get thrown away as soon as a later tab in the loop became selected instead.
      selectRestoredTab(selectedFile);
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
      if (item != null && item.isModelSupported()) {
        tabPane.getTabs().add(createTabShell(item));
      }
    }
    catch (Exception e) {
      log.error("Failed to restore tab '{}': {}", openedFiles.get(index), e.getMessage(), e);
    }

    Platform.runLater(() -> restoreNextTab(project, openedFiles, index + 1, selectedFile));
  }

  /**
   * Selects the tab matching {@code selectedFile} (or, if none matches - e.g. the previously-active file
   * was closed/removed - whichever tab ended up selected by default, such as the first tab added in
   * {@link #restoreNextTab}) and, unlike every other tab added during restore, actually builds its editor
   * content right away via {@link #loadTabContent}. Called while {@link #restoringSelection} is still
   * {@code true}, so the general lazy-load-on-selection path in {@link #onSelectionChanged} deliberately
   * skips it and this is the one place that loads it.
   */
  private void selectRestoredTab(String selectedFile) {
    Tab target = findTabByPath(selectedFile);
    if (target == null) {
      target = tabPane.getSelectionModel().getSelectedItem();
    }
    if (target != null) {
      tabPane.getSelectionModel().select(target);
      loadTabContent(target, (ProjectItem) target.getUserData());
    }
  }

  private Tab findTabByPath(String path) {
    if (path == null) {
      return null;
    }
    for (Tab tab : tabPane.getTabs()) {
      ProjectItem item = (ProjectItem) tab.getUserData();
      if (item != null && item.getPath().equals(path)) {
        return tab;
      }
    }
    return null;
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

    // Set before EditorFactory.create() (not after): same reasoning as open() below - if this is the selected
    // tab, panels populated synchronously while the new controller's load() runs resolve their model via
    // Studio.getSelectedProjectItem(), which reads this tab's user data.
    tab.setText(item.getDisplayName());
    tab.setUserData(item);

    // Only rebuild content for a tab that was actually loaded - see loadTabContent. A still-lazy (never
    // selected) restored tab has no editor/controller registered to unregister, and rebuilding its content
    // now would wrongly build it against whatever tab actually is selected (EditorFactory.create()'s property
    // panels resolve their model via Studio.getSelectedProjectItem(), not the tab being rebuilt). It stays
    // lazy and gets built correctly - with itself selected - whenever it is eventually selected.
    if (tab.getContent() != null) {
      StudioEventManager.getInstance().fireModelClosedEvent(item);
      Parent content = EditorFactory.create(item);
      if (content != null) {
        tab.setContent(content);
      }
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

  /**
   * Refreshes the tab of a reverted item in place, so an editor left open against a file a
   * version-control revert just changed out from under it shows the reverted content instead of
   * stale edits. {@code event.getItem()} has already been {@link ProjectItem#reload() reloaded} by
   * the caller, so rebuilding from it picks up the reverted content.
   */
  @Override
  public void modelReverted(@NonNull ModelRevertedEvent event) {
    ProjectItem item = event.getItem();
    for (Tab tab : tabPane.getTabs()) {
      ProjectItem tabItem = (ProjectItem) tab.getUserData();
      if (tabItem != null && tabItem.getPath().equals(item.getPath())) {
        rebuildTab(tab, item);
        return;
      }
    }
  }

  /**
   * Rebuilds {@code tab}'s editor content from {@code item}'s freshly reloaded model, same as
   * {@link #reloadTab}'s rename handling: the old editor is unregistered first via a synthetic
   * {@link ModelClosedEvent}. Unlike a rename, the path doesn't change, so the tab's title and the
   * project's opened-files settings are left alone. If the editor can't be rebuilt in place (e.g.
   * {@link EditorFactory#create} finds nothing to show for the reverted content), the tab is closed
   * and a fresh one reopened instead of leaving stale content on screen.
   *
   * <p>If {@code tab} is still lazy (never selected, so never had content built - see
   * {@link #loadTabContent}), there is no editor to rebuild: just refresh its user data and leave it lazy,
   * same reasoning as {@link #reloadTab}.
   */
  private void rebuildTab(@NonNull Tab tab, @NonNull ProjectItem item) {
    if (tab.getContent() == null) {
      tab.setUserData(item);
      return;
    }

    StudioEventManager.getInstance().fireModelClosedEvent(item);
    tab.setUserData(item);

    Parent content = EditorFactory.create(item);
    if (content != null) {
      tab.setContent(content);
      return;
    }

    closeTab(tab);
    if (item.getFile().exists()) {
      if (project != null) {
        project.getSettings().getUISettings().addOpenedFile(item.getPath());
        project.getSettings().getUISettings().save();
      }
      open(item);
    }
  }

  /**
   * A refactoring in another model (see {@link ModelRefactoredEvent}) edited the model behind an open tab in place, so
   * its editor is rebuilt from that model like after a revert - otherwise it keeps showing the text it had before, until
   * the tab is closed and reopened. The rebuild resets what the editor had selected, which is the price for not needing
   * every editor type to know how to refresh itself.
   */
  @Override
  public void modelRefactored(@NonNull ModelRefactoredEvent event) {
    ProjectItem item = event.getItem();
    for (Tab tab : tabPane.getTabs()) {
      ProjectItem tabItem = (ProjectItem) tab.getUserData();
      if (tabItem != null && tabItem.getPath().equals(item.getPath())) {
        rebuildTab(tab, item);
        return;
      }
    }
  }

  private boolean isSameOrDescendant(@NonNull String path, @NonNull String ancestorPath) {
    return path.equals(ancestorPath) || path.startsWith(ancestorPath + File.separator);
  }

  private void open(@NonNull ProjectItem item) {
    Tab tab = createTabShell(item);
    tabPane.getTabs().add(tab);
    loadTabContentWithProgress(tab, item);
  }

  /**
   * Builds a tab's title/icon/context menu/close handler - everything needed to show it in the tab strip -
   * without building its (expensive) editor content, so it can be added to {@link #tabPane} up front while
   * restoring previously-open tabs (see {@link #restoreNextTab}) and have its content deferred until it is
   * actually selected (see {@link #loadTabContent}).
   */
  private Tab createTabShell(@NonNull ProjectItem item) {
    Tab tab = new Tab(item.getDisplayName());
    tab.setUserData(item);
    tab.setClosable(true);

    A12Model<?> model = item.getModel();
    if (model instanceof TypeDefinitionModel) {
      applyModelTabStyle(tab, ModelType.TYPEDEFINITION);
    }
    else if (model instanceof AdditiveDocumentModel) {
      applyModelTabStyle(tab, ModelType.DOCUMENT, Icons.PNG_MODEL_DOCUMENT_ADDITIVE);
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
    return tab;
  }

  /**
   * Builds {@code tab}'s editor content via {@link EditorFactory#create} if it hasn't been built yet
   * (a no-op otherwise - see the {@link #onSelectionChanged}/{@link #selectRestoredTab} callers, which may
   * call this for a tab that turns out to already be loaded). Selects {@code tab} first (and restores the
   * previous selection if building fails): several property editor panels populate themselves synchronously
   * while EditorFactory.create() -> controller.load() runs, and resolve the model they should bind to via
   * Studio.getSelectedProjectItem() (e.g. LocalizedTextPanelController.buildLocaleFields(), used for
   * model-header fields like CustomFilterConfigurationPanelController's Header Subtitle/Filter Button Label).
   * If {@code tab} weren't already selected by then, that lookup would still resolve to whichever tab was
   * selected before, not {@code item}.
   *
   * <p>{@link #loadingTabContent} guards the {@code select(tab)} call below: if {@code tab} wasn't already
   * selected, selecting it fires {@link #onSelectionChanged} synchronously (JavaFX selection listeners run
   * inline, not on a later pulse), which would otherwise re-enter this method for the same still-unloaded
   * tab and build its content a second time before this call gets a chance to.
   */
  private void loadTabContent(@NonNull Tab tab, @NonNull ProjectItem item) {
    if (tab.getContent() != null) {
      return;
    }

    Tab previousSelection = tabPane.getSelectionModel().getSelectedItem();
    boolean alreadyLoading = loadingTabContent;
    loadingTabContent = true;
    try {
      tabPane.getSelectionModel().select(tab);

      Parent content = EditorFactory.create(item);
      if (content == null) {
        tabPane.getTabs().remove(tab);
        if (previousSelection != null && previousSelection != tab) {
          tabPane.getSelectionModel().select(previousSelection);
        }
        return;
      }
      tab.setContent(content);
    }
    finally {
      loadingTabContent = alreadyLoading;
    }
  }

  /**
   * Same as {@link #loadTabContent}, but wrapped in its own progress dialog (see
   * {@link LoadTabProgressModel}) - used for the two "live" ways a not-yet-loaded tab's editor gets built
   * outside of project restore: {@link #open} (a brand-new tab) and {@link #onSelectionChanged} (switching
   * to a previously-restored tab that is still lazy). Restoring a project's own previously-selected tab
   * (see {@link #selectRestoredTab}) deliberately calls {@link #loadTabContent} directly instead - that
   * load is already covered by the "Restoring Tabs" dialog (see {@link de.a12.studio.ui.RestoreTabsProgressModel}),
   * so stacking a second dialog on top of it would be redundant.
   *
   * <p>Skips the dialog when {@link ProjectItem#isModelSupported()} is {@code false}: {@link
   * EditorFactory#create} won't build an editor for such a tab anyway - it just shows a "not supported yet"
   * alert and returns {@code null} (see that method) - so there is nothing worth putting a progress bar in
   * front of.
   */
  private void loadTabContentWithProgress(@NonNull Tab tab, @NonNull ProjectItem item) {
    if (tab.getContent() != null) {
      return;
    }
    if (!item.isModelSupported()) {
      loadTabContent(tab, item);
      return;
    }
    ProgressDialog.createProgressDialog(Studio.stage, new LoadTabProgressModel(() -> loadTabContent(tab, item)));
  }

  /**
   * Sets the tab's icon and, for the "Enable Colorful Studio" preference (see
   * PreferenceAppGeneralPanelController / stylesheet-model-colors.css), a "model-tab-&lt;type&gt;" style
   * class (lowercase {@link ModelType#name()}) so the tab header can be tinted per model type. The
   * class is added unconditionally - stylesheet-model-colors.css only applies it while the TabPane
   * itself also carries "colorful-studio" (toggled by {@link #applyColorfulStudioSetting(boolean)}), so this
   * stays inert when the preference is off.
   */
  private void applyModelTabStyle(@NonNull Tab tab, @NonNull ModelType modelType) {
    applyModelTabStyle(tab, modelType, Icons.forModelType(modelType));
  }

  /**
   * Same as {@link #applyModelTabStyle(Tab, ModelType)}, but with an explicit icon path instead of the one
   * {@link Icons#forModelType} derives from {@code modelType} alone - used for an {@link
   * AdditiveDocumentModel}, which is still a {@link ModelType#DOCUMENT} (so keeps that type's "colorful
   * studio" tint) but shows a distinct icon.
   */
  private void applyModelTabStyle(@NonNull Tab tab, @NonNull ModelType modelType, String iconPath) {
    tab.setGraphic(WidgetFactory.createModelIcon(iconPath));
    tab.getStyleClass().add("model-tab-" + modelType.name().toLowerCase());
  }

  /**
   * Double-clicking a tab's header (not its content - see {@link #isTabHeaderClick}) reveals that tab's
   * model in the project tree, same as the project tree toolbar's "select active model" button fires via
   * {@link StudioEventManager#fireModelFocusRequestedEvent}. Wired once on the shared
   * {@link #tabPane} rather than per-tab: a per-tab {@code setOnMouseClicked} on {@code tab.getTabPane()}
   * (the previous approach) targets the same underlying TabPane node for every tab, so each newly opened
   * tab silently overwrote the previous tab's handler and only the most-recently-opened tab's double-click
   * ever worked. By the time a double click's second click is dispatched, tab-header selection has already
   * switched to the clicked tab, so reading {@link #getSelectedProjectItem()} at that point always reflects
   * the tab actually under the pointer.
   */
  private void installTabHeaderDoubleClickHandler() {
    tabPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
      if (event.getClickCount() == 2 && isTabHeaderClick(event)) {
        ProjectItem projectItem = getSelectedProjectItem();
        if (projectItem != null) {
          StudioEventManager.getInstance().fireModelFocusRequestedEvent(projectItem);
        }
      }
    });
  }

  /**
   * True if {@code event} landed inside the TabPane's header strip (the row of tab labels) rather than the
   * selected tab's content below it - the default {@code TabPaneSkin} puts the header row in a node styled
   * "tab-header-area", a sibling of (not an ancestor shared with) the content area, so walking up from the
   * clicked node and finding that style class unambiguously means the header, not the editor, was clicked.
   */
  private boolean isTabHeaderClick(@NonNull MouseEvent event) {
    for (Node node = event.getPickResult().getIntersectedNode(); node != null; node = node.getParent()) {
      if (node.getStyleClass().contains("tab-header-area")) {
        return true;
      }
    }
    return false;
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
    tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> updateEmptyState());
    updateEmptyState();
    installTabHeaderDoubleClickHandler();

    boolean enabled = LocalUISettings.getBoolean(LocalUISettings.COLORFUL_STUDIO_ENABLED, true);
    applyColorfulStudioSetting(enabled);
    LocalUISettings.addListener((key, value) -> {
      Platform.runLater(() -> {
        if (LocalUISettings.COLORFUL_STUDIO_ENABLED.equals(key)) {
          boolean colorsEnabled = LocalUISettings.getBoolean(LocalUISettings.COLORFUL_STUDIO_ENABLED, true);
          applyColorfulStudioSetting(colorsEnabled);
        }
      });
    });
  }

  /**
   * Hides the (empty) TabPane so the "%no_tab_opened" placeholder {@link javafx.scene.control.Label}
   * behind it in scene-tab-pane.fxml's StackPane shows through - mirrors the mainSplitPane
   * visible/managed toggle in RootController used for the analogous "no project opened" placeholder.
   */
  private void updateEmptyState() {
    boolean empty = tabPane.getTabs().isEmpty();
    tabPane.setVisible(!empty);
    tabPane.setManaged(!empty);
  }

  /** Toggles the "colorful-studio" style class that gates stylesheet-model-colors.css's per-tab tinting. */
  private void applyColorfulStudioSetting(boolean colorsEnabled) {
    if (colorsEnabled) {
      if (!tabPane.getStyleClass().contains("colorful-studio")) {
        tabPane.getStyleClass().add("colorful-studio");
      }
    }
    else {
      tabPane.getStyleClass().remove("colorful-studio");
    }
  }

  /**
   * Besides notifying listeners and persisting the newly selected file, lazily builds {@code newTab}'s
   * editor content (behind its own progress dialog - see {@link #loadTabContentWithProgress}) if it doesn't
   * have any yet - i.e. a tab added lazily by {@link #restoreNextTab} that is only now being selected for
   * the first time. Skipped while {@link #restoringSelection} is set: during that window, {@link
   * #selectRestoredTab} is the one place responsible for loading the (single) tab that should end up with
   * content, and every other selection change firing here is just the transient default selection JavaFX
   * assigns as lazy tabs get added one by one - not a real user selection worth loading. Also skipped while
   * {@link #loadingTabContent} is set - see that field's javadoc - since this same selection change is what
   * a {@link #loadTabContent} call already in progress for {@code newTab} is itself triggering.
   */
  private void onSelectionChanged(Tab newTab) {
    ProjectItem item = newTab == null ? null : (ProjectItem) newTab.getUserData();

    if (newTab != null && item != null && !restoringSelection && !loadingTabContent) {
      loadTabContentWithProgress(newTab, item);
    }

    StudioEventManager.getInstance().fireTabSelectionChangedEvent(item);

    if (restoringSelection || project == null) {
      return;
    }
    project.getSettings().getUISettings().setSelectedFile(item == null ? null : item.getPath());
    project.getSettings().getUISettings().save();
  }
}
