package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.dataservices.models.Locale;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.services.documentmodel.features.validation.DMValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Icons;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Edits {@link A12Model#getLocales()}. Not bound to a single {@link de.a12.studio.dataservices.models.documentmodel.Element}
 * (locales live on the model header), so {@link #setElement} is never called and only {@link #setModel} is used;
 * validation is therefore driven manually via {@link #updateValidation} rather than the element-based
 * validation in {@link AbstractPropertyEditor#commitChange(javafx.scene.Node)}.
 */
public class LocalesPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private static final DMValidationService VALIDATION_SERVICE = new DMValidationService();

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

  private final Debouncer debouncer = new Debouncer();

  private A12Model model;

  public void setModel(@NonNull A12Model model) {
    this.model = model;
    rebuildRows();
    updateValidation();
  }

  @FXML
  private void onAdd() {
    model.getLocales().add(new Locale());
    rebuildRows();
    updateValidation();
  }

  private void rebuildRows() {
    localesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<Locale> locales = model.getLocales();
    for (int index = 0; index < locales.size(); index++) {
      localesGrid.addRow(index + 1, createLocaleComboBox(index), createActionsBox(index));
    }
  }

  private ComboBox<java.util.Locale> createLocaleComboBox(int index) {
    Locale locale = model.getLocales().get(index);
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
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this locale?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        model.getLocales().remove(index);
        rebuildRows();
        commitLocalesChange();
      }
    });

    HBox actionsBox = new HBox(4.0, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void commitLocalesChange() {
    commitChange();
    updateValidation();
  }

  private void updateValidation() {
    if (model instanceof DocumentModel documentModel) {
      VALIDATION_SERVICE.getMissingLocaleError(documentModel)
          .ifPresentOrElse(message -> showError("ERROR", message), this::hideError);
    } else {
      hideError();
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
}
