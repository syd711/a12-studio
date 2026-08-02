package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.auth.AuthFileType;
import de.a12.studio.models.auth.Role;
import de.a12.studio.models.auth.RolesDocument;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.YamlSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shared row-editing behavior for the "comma-separated roles list" panels: a grid of role rows, each an
 * editable combo box (suggesting the role names declared in the workspace's {@code auth/roles.yaml} via
 * {@link #createRoleComboBox}) plus a delete button, and an "Add" button that appends a blank row. Subclasses
 * differ only in the combo box's id prefix and where the joined roles string is persisted
 * ({@link #commitRolesChange}).
 */
@Slf4j
public abstract class AbstractRolesPanelController extends AbstractPropertyEditor {

  protected static final int COMMIT_DEBOUNCE_MS = 150;

  private static final String NO_ROLES_CONFIGURED_MESSAGE = "No roles configured.";

  private static final String AUTH_FOLDER_NAME = "auth";

  @FXML
  private GridPane rolesGrid;

  @FXML
  private Label roleHeaderLabel;

  @FXML
  private Button editRolesButton;

  protected final Debouncer debouncer = new Debouncer();
  protected final List<String> roles = new ArrayList<>();

  @FXML
  protected void onAdd() {
    roles.add("");
    rebuildRows();
  }

  /**
   * Opens the workspace's {@code auth/roles.yaml} in an editor tab, selecting its tab instead if it's already
   * open (see {@code TabPaneController#modelOpened}). Only wired to a visible button (see
   * {@link #updateEditRolesButtonVisibility}), so the file is known to exist when this runs.
   */
  @FXML
  private void onEditRoles() {
    File rolesFile = resolveWorkspaceRolesFile();
    if (rolesFile == null || !rolesFile.isFile()) {
      return;
    }

    ProjectItem item = new ProjectItem(rolesFile);
    Project project = Studio.getCurrentProject();
    if (project != null) {
      project.getSettings().getUISettings().addOpenedFile(item.getPath());
      project.getSettings().getUISettings().save();
    }
    StudioEventManager.getInstance().fireModelOpenEvent(item);
  }

  /**
   * Hidden when embedded in a dialog ({@link #isEmbeddedInDialog()}): opening the roles file in an editor tab
   * from there would abandon whatever the dialog's own Save/Cancel flow was about, so the action doesn't make
   * sense in that context.
   */
  private void updateEditRolesButtonVisibility() {
    File rolesFile = resolveWorkspaceRolesFile();
    boolean available = rolesFile != null && rolesFile.isFile() && !isEmbeddedInDialog();
    editRolesButton.setVisible(available);
    editRolesButton.setManaged(available);
  }

  protected void rebuildRows() {
    updateEditRolesButtonVisibility();

    rolesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    roleHeaderLabel.setVisible(!roles.isEmpty());
    roleHeaderLabel.setManaged(!roles.isEmpty());
    if (roles.isEmpty()) {
      Label emptyLabel = new Label(NO_ROLES_CONFIGURED_MESSAGE);
      emptyLabel.getStyleClass().add("placeholder-label");
      rolesGrid.addRow(1, emptyLabel);
      return;
    }

    for (int index = 0; index < roles.size(); index++) {
      rolesGrid.addRow(index + 1, createRoleField(index), createActionsBox(index));
    }
  }

  /**
   * Builds the editable combo box for the role at {@code index}, with {@code idPrefix} distinguishing this
   * panel's field ids (e.g. {@code "role-0"} vs {@code "module-role-0"}) from another roles panel's.
   */
  protected ComboBox<String> createRoleComboBox(int index, String idPrefix) {
    ComboBox<String> comboBox = new ComboBox<>();
    comboBox.setId(idPrefix + index);
    comboBox.setEditable(true);
    comboBox.setMaxWidth(Double.MAX_VALUE);
    comboBox.setPromptText("Role name");
    comboBox.getItems().setAll(loadWorkspaceRoleNames());
    comboBox.setValue(roles.get(index));
    comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      roles.set(index, newValue);
      debouncer.debounce(comboBox.getId(), this::commitRolesChange, COMMIT_DEBOUNCE_MS, true);
    });
    return comboBox;
  }

  /**
   * Builds the editable control for the role at {@code index}.
   */
  protected abstract Node createRoleField(int index);

  protected HBox createActionsBox(int index) {
    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this role?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        roles.remove(index);
        rebuildRows();
        commitRolesChange();
      }
    });

    HBox actionsBox = new HBox(4.0, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  /**
   * Persists the current {@link #roles} list to whatever backs this panel (a model header annotation, a
   * module's menu permission, ...).
   */
  protected abstract void commitRolesChange();

  protected static List<String> parseRoles(String commaSeparated) {
    if (commaSeparated == null || commaSeparated.isBlank()) {
      return List.of();
    }
    return Arrays.stream(commaSeparated.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(Collectors.toList());
  }

  protected String joinRoles() {
    return roles.stream()
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(Collectors.joining(","));
  }

  /**
   * The role names declared in the current workspace's {@code auth/roles.yaml}, sorted alphabetically. Empty
   * if no project is open, the file doesn't exist, or it fails to parse.
   */
  protected static List<String> loadWorkspaceRoleNames() {
    File rolesFile = resolveWorkspaceRolesFile();
    if (rolesFile == null || !rolesFile.isFile()) {
      return List.of();
    }
    try {
      RolesDocument rolesDocument = YamlSettings.objectMapper.readValue(rolesFile, RolesDocument.class);
      return rolesDocument.getRoles().stream()
          .map(Role::getName)
          .filter(name -> name != null && !name.isBlank())
          .distinct()
          .sorted()
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.warn("Failed to load workspace roles from '{}': {}", rolesFile, e.getMessage(), e);
      return List.of();
    }
  }

  /**
   * The workspace's {@code auth/roles.yaml}, or {@code null} if no project is currently open.
   */
  protected static File resolveWorkspaceRolesFile() {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return null;
    }
    return new File(new File(project.getFolder(), AUTH_FOLDER_NAME), AuthFileType.ROLES.getFileName());
  }

  protected static Button createActionButton(String iconLiteral, String tooltip, Runnable action) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(WidgetFactory.createTooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }
}
