package de.a12.studio.ui.preferences;

import de.a12.studio.models.Locale;
import de.a12.studio.models.projects.settings.GeneralSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edits {@link GeneralSettings#getLocales()} directly, the project-wide master locale list stored in the
 * project-root {@code settings.json}. This is a dedicated copy of {@link
 * de.a12.studio.ui.editors.propertyeditors.LocalesPanelController} (which edits a single document model's own
 * locale list, and is bound to an {@link de.a12.studio.models.A12Model}/{@link
 * de.a12.studio.models.projects.ProjectItem}): General Settings isn't a document model or project item, so this
 * copy loads/saves straight through {@link GeneralSettings} and {@link ProjectRootSettings#save()} instead of
 * reusing that controller via a fake model adapter.
 */
public class GeneralLocalesPanelController implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private static final List<java.util.Locale> AVAILABLE_LOCALES = Arrays.stream(java.util.Locale.getAvailableLocales())
      .filter(locale -> !locale.toLanguageTag().isEmpty() && !locale.toLanguageTag().equals("und"))
      .sorted(Comparator.comparing(java.util.Locale::getDisplayName))
      .toList();

  private static final StringConverter<java.util.Locale> LOCALE_CONVERTER = new StringConverter<>() {
    @Override
    public String toString(java.util.Locale locale) {
      return locale == null ? "" : locale.getDisplayName() + " (" + locale.toLanguageTag() + ")";
    }

    @Override
    public java.util.Locale fromString(String text) {
      if (text == null || text.isBlank()) {
        return null;
      }
      String trimmed = text.trim();
      return AVAILABLE_LOCALES.stream()
          .filter(locale -> locale.toLanguageTag().equalsIgnoreCase(trimmed) || toString(locale).equalsIgnoreCase(trimmed))
          .findFirst()
          .orElseGet(() -> java.util.Locale.forLanguageTag(trimmed));
    }
  };

  @FXML
  private GridPane localesGrid;

  @FXML
  private Label codeHeaderLabel;

  @FXML
  private ErrorContainerController errorContainerController;

  private final Debouncer debouncer = new Debouncer();

  private GeneralSettings generalSettings;

  private ProjectRootSettings rootSettings;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // no-op: setup happens in setGeneralSettings, once the caller has a project loaded
  }

  public void setGeneralSettings(@NonNull GeneralSettings generalSettings, @NonNull ProjectRootSettings rootSettings) {
    this.generalSettings = generalSettings;
    this.rootSettings = rootSettings;
    rebuildRows();
    updateValidation();
  }

  @FXML
  private void onAdd() {
    generalSettings.getLocales().add(new Locale());
    rebuildRows();
    commitLocalesChange();
  }

  private void rebuildRows() {
    localesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<Locale> locales = generalSettings.getLocales();
    codeHeaderLabel.setVisible(!locales.isEmpty());
    codeHeaderLabel.setManaged(!locales.isEmpty());
    if (locales.isEmpty()) {
      Label emptyLabel = new Label("No locales configured.");
      emptyLabel.getStyleClass().add("placeholder-label");
      localesGrid.addRow(1, emptyLabel);
      return;
    }

    for (int index = 0; index < locales.size(); index++) {
      localesGrid.addRow(index + 1, createLocaleComboBox(index), createActionsBox(index));
    }
  }

  private ComboBox<java.util.Locale> createLocaleComboBox(int index) {
    Locale locale = generalSettings.getLocales().get(index);
    ComboBox<java.util.Locale> comboBox = new ComboBox<>(FXCollections.observableArrayList(AVAILABLE_LOCALES));
    comboBox.setId("locale-" + index);
    comboBox.setEditable(true);
    comboBox.setMaxWidth(Double.MAX_VALUE);
    comboBox.setConverter(LOCALE_CONVERTER);
    comboBox.setValue(LOCALE_CONVERTER.fromString(locale.getCode()));
    comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      locale.setCode(newValue == null ? null : newValue.toLanguageTag());
      debouncer.debounce(comboBox.getId(), this::commitLocalesChange, COMMIT_DEBOUNCE_MS, true);
    });
    return comboBox;
  }

  private HBox createActionsBox(int index) {
    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_locale"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        generalSettings.getLocales().remove(index);
        rebuildRows();
        commitLocalesChange();
      }
    });

    HBox actionsBox = new HBox(4.0, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void commitLocalesChange() {
    rootSettings.save();
    updateValidation();
  }

  private void updateValidation() {
    if (generalSettings.getLocales() == null || generalSettings.getLocales().isEmpty()) {
      errorContainerController.show("ERROR", "Please add at least one locale.");
    } else {
      errorContainerController.hide();
    }
  }

  private static Button createActionButton(String iconLiteral, String tooltip, Runnable action) {
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
