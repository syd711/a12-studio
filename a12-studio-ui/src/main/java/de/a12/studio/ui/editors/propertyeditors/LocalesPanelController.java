package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.Locale;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Edits {@link A12Model#getLocales()}. Not bound to a single {@link Element}
 * (locales live on the model header), so {@link #setElement} is never called and only {@link #setModel} is used;
 * validation is therefore driven manually via {@link #updateValidation} rather than the element-based
 * validation in {@link AbstractPropertyEditor#commitChange(javafx.scene.Node)}.
 */
public class LocalesPanelController extends AbstractPropertyEditor implements Initializable {

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

  private final Debouncer debouncer = new Debouncer();

  // Used only while this panel is not yet bound to a model (see #initializeLocales), for
  // de.a12.studio.ui.projecttree.dialogs.NewModelDialogController, which only creates the model once its
  // dialog is confirmed.
  private final List<Locale> standaloneLocales = new ArrayList<>();

  private A12Model<?> model;

  public void setModel(@NonNull A12Model<?> model) {
    this.model = model;
    rebuildRows();
    updateValidation();
  }

  /**
   * Seeds this panel's rows without binding to a model, for {@link
   * de.a12.studio.ui.projecttree.dialogs.NewModelDialogController}. Edits made here are read back via
   * {@link #getLocales()} on submit -- {@link #commitLocalesChange} skips the save/event machinery the
   * whole time since {@link #model} is never set.
   */
  public void initializeLocales(@NonNull List<Locale> initialLocales) {
    standaloneLocales.clear();
    for (Locale initialLocale : initialLocales) {
      Locale copy = new Locale();
      copy.setCode(initialLocale.getCode());
      standaloneLocales.add(copy);
    }
    rebuildRows();
    updateValidation();
  }

  /**
   * The current locales -- the counterpart to {@link #initializeLocales} for callers that never bind this
   * panel to a model via {@link #setModel}.
   */
  public List<Locale> getLocales() {
    return List.copyOf(standaloneLocales);
  }

  private List<Locale> currentLocales() {
    return model != null ? model.getLocales() : standaloneLocales;
  }

  @FXML
  private void onAdd() {
    currentLocales().add(new Locale());
    rebuildRows();
    updateValidation();
  }

  private void rebuildRows() {
    localesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<Locale> locales = currentLocales();
    for (int index = 0; index < locales.size(); index++) {
      localesGrid.addRow(index + 1, createLocaleComboBox(index), createActionsBox(index));
    }
  }

  private ComboBox<java.util.Locale> createLocaleComboBox(int index) {
    Locale locale = currentLocales().get(index);
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
    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_locale"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        currentLocales().remove(index);
        rebuildRows();
        commitLocalesChange();
      }
    });

    HBox actionsBox = new HBox(4.0, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void commitLocalesChange() {
    if (model == null) {
      // Not bound to a model yet (NewModelDialogController) -- nothing to save or revalidate via
      // ValidationService, just keep the panel's own error container in sync.
      updateValidation();
      return;
    }

    commitChange();
    updateValidation();

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      StudioEventManager.getInstance().fireLocalesChangedEvent(projectItem);
    }
  }

  private void updateValidation() {
    if (model == null) {
      if (standaloneLocales.isEmpty()) {
        showError("ERROR", StudioBundle.get("validation.please_add_at_least_one_locale"));
      } else {
        hideError();
      }
      return;
    }
    Studio.getValidationService().getMissingLocaleError(model)
        .ifPresentOrElse(message -> showError("ERROR", message), this::hideError);
  }
}
