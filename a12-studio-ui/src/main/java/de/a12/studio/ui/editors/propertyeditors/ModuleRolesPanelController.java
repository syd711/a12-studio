package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Edits the comma-separated {@code permission} (roles) of a single {@link Module}'s {@link Menu}, e.g.
 * {@code "tester,reviewer"}. Not bound to a single Element (roles live on the module's menu), so it follows the
 * model-header pattern used by {@link RolesEditorPanelController}, minus the parts specific to editing a whole
 * model's header roles annotation (suggestions, the roles-file warning).
 */
public class ModuleRolesPanelController extends AbstractPropertyEditor {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private static final String NO_ROLES_CONFIGURED_MESSAGE = "No roles configured.";

  @FXML
  private GridPane rolesGrid;

  @FXML
  private Label roleHeaderLabel;

  private final Debouncer debouncer = new Debouncer();

  private Module module;
  private final List<String> roles = new ArrayList<>();

  public void setModule(@NonNull Module module) {
    this.module = module;
    roles.clear();
    roles.addAll(parseRoles(module));
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    roles.add("");
    rebuildRows();
  }

  private void rebuildRows() {
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
      rolesGrid.addRow(index + 1, createTextField(index), createActionsBox(index));
    }
  }

  private TextField createTextField(int index) {
    TextField textField = new TextField(roles.get(index));
    textField.setId("module-role-" + index);
    textField.setMaxWidth(Double.MAX_VALUE);
    textField.setPromptText("Role name");
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
      roles.set(index, newValue);
      debouncer.debounce(textField.getId(), this::commitRolesChange, COMMIT_DEBOUNCE_MS, true);
    });
    return textField;
  }

  private HBox createActionsBox(int index) {
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

  private void commitRolesChange() {
    if (module == null) {
      return;
    }

    String joined = roles.stream()
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(Collectors.joining(","));

    Menu menu = module.getOrCreateMenu();
    menu.setPermission(joined.isEmpty() ? null : joined);

    commitChange();
  }

  private static List<String> parseRoles(Module module) {
    Menu menu = module.getMenu();
    String permission = menu != null ? menu.getPermission() : null;
    if (permission == null || permission.isBlank()) {
      return List.of();
    }
    return Arrays.stream(permission.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .collect(Collectors.toList());
  }

  private static Button createActionButton(String iconLiteral, String tooltip, Runnable action) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(new Tooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }
}
