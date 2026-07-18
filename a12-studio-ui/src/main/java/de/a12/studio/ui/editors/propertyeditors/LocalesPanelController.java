package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.commons.fx.Debouncer;
import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.dataservices.models.Locale;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.util.Icons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edits {@link A12Model#getLocales()}. Not an {@link de.a12.studio.ui.editors.AbstractPropertyEditor}
 * since locales live on the model header rather than a single {@link de.a12.studio.dataservices.models.documentmodel.Element}.
 */
public class LocalesPanelController implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  @FXML
  private TitledPane root;

  @FXML
  private GridPane localesGrid;

  private final Debouncer debouncer = new Debouncer();

  private A12Model model;

  // Immediate by default; switched to a shared PropertyEditorSaveMode.Deferred by dialogs with their own
  // Save button, see setSaveMode().
  private PropertyEditorSaveMode saveMode = PropertyEditorSaveMode.IMMEDIATE;

  public void setSaveMode(@NonNull PropertyEditorSaveMode saveMode) {
    this.saveMode = saveMode;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Platform.runLater(() -> {
      String settingsKey = getExpandedSettingsKey();
      if (settingsKey != null) {
        boolean animated = root.isAnimated();
        root.setAnimated(false);
        root.setExpanded(LocalUISettings.getBoolean(settingsKey));
        root.setAnimated(animated);
        root.expandedProperty().addListener((observable, oldValue, newValue) ->
            LocalUISettings.saveProperty(settingsKey, String.valueOf(newValue)));
      }
    });
  }

  public void setModel(@NonNull A12Model model) {
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    model.getLocales().add(new Locale());
    rebuildRows();
  }

  private void rebuildRows() {
    localesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<Locale> locales = model.getLocales();
    for (int index = 0; index < locales.size(); index++) {
      localesGrid.addRow(index + 1, createTextField(index), createActionsBox(index));
    }
  }

  private TextField createTextField(int index) {
    Locale locale = model.getLocales().get(index);
    TextField textField = new TextField(locale.getCode());
    textField.setId("locale-" + index);
    textField.setMaxWidth(Double.MAX_VALUE);
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
      locale.setCode(newValue);
      debouncer.debounce(textField.getId(), this::commitChange, COMMIT_DEBOUNCE_MS, true);
    });
    return textField;
  }

  private HBox createActionsBox(int index) {
    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this locale?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        model.getLocales().remove(index);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void commitChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      saveMode.commit(projectItem);
    }
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

  private String getExpandedSettingsKey() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null || projectItem.getModel() == null || projectItem.getModel().getModelType() == null) {
      return null;
    }
    return projectItem.getModel().getModelType().getValue() + "." + getClass().getSimpleName() + ".expanded";
  }
}
