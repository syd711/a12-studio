package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.overviewmodel.ClearConfirmation;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.overviewmodel.Icon;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

/**
 * Edits an {@link OverviewModel}'s {@code content.configuration.multiSelection}: the collapse/counter/
 * selection-area options, the clear-selection confirmation, and one draggable-free {@link ListView} of
 * {@link de.a12.studio.models.overviewmodel.Button} entries with their own detail form. Not bound to a
 * single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by
 * e.g. {@link OverviewFeaturesPanelController}.
 */
public class OverviewMultiSelectionPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> COLLAPSE_OPTIONS = List.of("",
      MultiSelectionConfig.COLLAPSE_OPTION_COLLAPSIBLE_COLLAPSED, MultiSelectionConfig.COLLAPSE_OPTION_COLLAPSIBLE_EXPANDED,
      MultiSelectionConfig.COLLAPSE_OPTION_NON_COLLAPSIBLE);
  private static final List<String> COUNTER_OPTIONS = List.of("",
      MultiSelectionConfig.COUNTER_OPTION_SIMPLE, MultiSelectionConfig.COUNTER_OPTION_NONE);
  private static final List<String> SELECTION_AREA_OPTIONS = List.of("",
      MultiSelectionConfig.SELECTION_AREA_CHECKBOX, MultiSelectionConfig.SELECTION_AREA_CHECKBOX_AND_ROW);
  private static final List<String> ICON_THEME_OPTIONS = List.of("",
      Icon.THEME_FILLED, Icon.THEME_OUTLINED, Icon.THEME_ROUNDED, Icon.THEME_CUSTOM);

  @FXML
  private CheckBox enableMultiSelectionField;
  @FXML
  private VBox multiSelectionDetailsBox;
  @FXML
  private ComboBox<String> collapseOptionField;
  @FXML
  private ComboBox<String> counterOptionField;
  @FXML
  private ComboBox<String> selectionAreaField;
  @FXML
  private CheckBox clearConfirmationField;
  @FXML
  private ListView<de.a12.studio.models.overviewmodel.Button> multiSelectionButtonsList;
  @FXML
  private VBox multiSelectionButtonDetailBox;
  @FXML
  private TextField buttonEventField;
  @FXML
  private CheckBox buttonDestructiveField;
  @FXML
  private CheckBox buttonPrimaryField;
  @FXML
  private TextField buttonIconNameField;
  @FXML
  private ComboBox<String> buttonIconThemeField;
  @FXML
  private GridPane buttonLabelGrid;
  @FXML
  private GridPane buttonDescriptionGrid;
  @FXML
  private CheckBox buttonConfirmationField;
  @FXML
  private VBox buttonConfirmationDetailsBox;
  @FXML
  private GridPane buttonConfirmationTitleGrid;
  @FXML
  private GridPane buttonConfirmationMessageGrid;

  private OverviewModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  // Preserves multi-selection settings across an uncheck/recheck of "Enable Multi-Selection" within the
  // same session, since disabling it nulls configuration.multiSelection (matching SME's on-disk shape).
  private MultiSelectionConfig cachedMultiSelectionConfig;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    enableMultiSelectionField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      OverviewConfiguration configuration = ensureConfiguration();
      if (newValue) {
        configuration.setMultiSelection(configuration.getMultiSelection() != null ? configuration.getMultiSelection()
            : (cachedMultiSelectionConfig != null ? cachedMultiSelectionConfig : new MultiSelectionConfig()));
      }
      else {
        cachedMultiSelectionConfig = configuration.getMultiSelection();
        configuration.setMultiSelection(null);
      }
      multiSelectionDetailsBox.setVisible(newValue);
      multiSelectionDetailsBox.setManaged(newValue);
      boolean wasUpdating = updatingFromModel;
      updatingFromModel = true;
      try {
        populateMultiSelectionFields();
      }
      finally {
        updatingFromModel = wasUpdating;
      }
      commitHeaderChange();
    });

    collapseOptionField.getItems().setAll(COLLAPSE_OPTIONS);
    collapseOptionField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelectionConfig().setCollapseOption(newValue == null || newValue.isBlank() ? null : newValue);
      commitHeaderChange();
    });

    counterOptionField.getItems().setAll(COUNTER_OPTIONS);
    counterOptionField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelectionConfig().setCounterOption(newValue == null || newValue.isBlank() ? null : newValue);
      commitHeaderChange();
    });

    selectionAreaField.getItems().setAll(SELECTION_AREA_OPTIONS);
    selectionAreaField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelectionConfig().setSelectionArea(newValue == null || newValue.isBlank() ? null : newValue);
      commitHeaderChange();
    });

    clearConfirmationField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      MultiSelectionConfig config = ensureMultiSelectionConfig();
      if (newValue) {
        ClearConfirmation confirmation = config.getClearConfirmation();
        if (confirmation == null) {
          confirmation = new ClearConfirmation();
          config.setClearConfirmation(confirmation);
        }
        confirmation.setEnabled(true);
      }
      else {
        config.setClearConfirmation(null);
      }
      commitHeaderChange();
    });

    multiSelectionButtonsList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(de.a12.studio.models.overviewmodel.Button button, boolean empty) {
        super.updateItem(button, empty);
        setText(empty || button == null ? null : describeMultiSelectionButton(button));
      }
    });
    multiSelectionButtonsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showMultiSelectionButton(newValue));

    buttonEventField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button == null) {
        return;
      }
      button.setEvent(newValue == null || newValue.isBlank() ? null : newValue);
      multiSelectionButtonsList.refresh();
      commitHeaderChange();
    });

    buttonDestructiveField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button != null) {
        button.setDestructive(newValue);
        commitHeaderChange();
      }
    });
    buttonPrimaryField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button != null) {
        button.setPrimary(newValue);
        commitHeaderChange();
      }
    });

    buttonIconNameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button == null) {
        return;
      }
      setIconName(button, newValue);
      commitHeaderChange();
    });

    buttonIconThemeField.getItems().setAll(ICON_THEME_OPTIONS);
    buttonIconThemeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button == null || button.getIcon() == null) {
        return;
      }
      button.getIcon().setTheme(newValue == null || newValue.isBlank() ? null : newValue);
      commitHeaderChange();
    });

    buttonConfirmationField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button == null) {
        return;
      }
      if (newValue) {
        ensureConfirmation(button);
      }
      else {
        button.setConfirmation(null);
      }
      buttonConfirmationDetailsBox.setVisible(newValue);
      buttonConfirmationDetailsBox.setManaged(newValue);
      boolean wasUpdating = updatingFromModel;
      updatingFromModel = true;
      try {
        rebuildLocaleGrid(buttonConfirmationTitleGrid, newValue ? ensureConfirmation(button).getTitle() : List.of(),
            (code, text) -> setLabelText(ensureConfirmation(button).getTitle(), code, text));
        rebuildLocaleGrid(buttonConfirmationMessageGrid, newValue ? ensureConfirmation(button).getMessage() : List.of(),
            (code, text) -> setLabelText(ensureConfirmation(button).getMessage(), code, text));
      }
      finally {
        updatingFromModel = wasUpdating;
      }
      multiSelectionButtonsList.refresh();
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      OverviewConfiguration configuration = model.getContent().getConfiguration();
      boolean multiSelectionEnabled = configuration != null && configuration.getMultiSelection() != null;
      enableMultiSelectionField.setSelected(multiSelectionEnabled);
      multiSelectionDetailsBox.setVisible(multiSelectionEnabled);
      multiSelectionDetailsBox.setManaged(multiSelectionEnabled);
      populateMultiSelectionFields();
    }
    finally {
      updatingFromModel = false;
    }
  }

  private void populateMultiSelectionFields() {
    MultiSelectionConfig config = currentMultiSelectionConfig();
    collapseOptionField.setValue(config != null ? orEmpty(config.getCollapseOption()) : "");
    counterOptionField.setValue(config != null ? orEmpty(config.getCounterOption()) : "");
    selectionAreaField.setValue(config != null ? orEmpty(config.getSelectionArea()) : "");
    clearConfirmationField.setSelected(config != null && config.getClearConfirmation() != null
        && Boolean.TRUE.equals(config.getClearConfirmation().getEnabled()));
    refreshMultiSelectionButtonsList();
    showMultiSelectionButton(null);
  }

  private MultiSelectionConfig currentMultiSelectionConfig() {
    return model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getMultiSelection() : null;
  }

  private void refreshMultiSelectionButtonsList() {
    de.a12.studio.models.overviewmodel.Button selected = multiSelectionButtonsList.getSelectionModel().getSelectedItem();
    MultiSelectionConfig config = currentMultiSelectionConfig();
    List<de.a12.studio.models.overviewmodel.Button> buttons = config != null ? config.getButtons() : List.of();
    multiSelectionButtonsList.getItems().setAll(buttons);
    if (selected != null && buttons.contains(selected)) {
      multiSelectionButtonsList.getSelectionModel().select(selected);
    }
  }

  private de.a12.studio.models.overviewmodel.Button selectedMultiSelectionButton() {
    return multiSelectionButtonsList.getSelectionModel().getSelectedItem();
  }

  private void showMultiSelectionButton(de.a12.studio.models.overviewmodel.Button button) {
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      boolean present = button != null;
      multiSelectionButtonDetailBox.setVisible(present);
      multiSelectionButtonDetailBox.setManaged(present);
      if (!present) {
        return;
      }
      buttonEventField.setText(button.getEvent() != null ? button.getEvent() : "");
      buttonDestructiveField.setSelected(Boolean.TRUE.equals(button.getDestructive()));
      buttonPrimaryField.setSelected(Boolean.TRUE.equals(button.getPrimary()));
      buttonIconNameField.setText(button.getIcon() != null && button.getIcon().getName() != null ? button.getIcon().getName() : "");
      buttonIconThemeField.setValue(button.getIcon() != null ? orEmpty(button.getIcon().getTheme()) : "");
      rebuildLocaleGrid(buttonLabelGrid, button.getLabel(), (code, text) -> setLabelText(button.getLabel(), code, text));
      rebuildLocaleGrid(buttonDescriptionGrid, button.getDescription(), (code, text) -> setLabelText(button.getDescription(), code, text));

      boolean confirmationEnabled = button.getConfirmation() != null;
      buttonConfirmationField.setSelected(confirmationEnabled);
      buttonConfirmationDetailsBox.setVisible(confirmationEnabled);
      buttonConfirmationDetailsBox.setManaged(confirmationEnabled);
      Confirmation confirmation = button.getConfirmation();
      rebuildLocaleGrid(buttonConfirmationTitleGrid, confirmation != null ? confirmation.getTitle() : List.of(),
          (code, text) -> setLabelText(ensureConfirmation(button).getTitle(), code, text));
      rebuildLocaleGrid(buttonConfirmationMessageGrid, confirmation != null ? confirmation.getMessage() : List.of(),
          (code, text) -> setLabelText(ensureConfirmation(button).getMessage(), code, text));
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  @FXML
  public void onAddMultiSelectionButton() {
    MultiSelectionConfig config = ensureMultiSelectionConfig();
    de.a12.studio.models.overviewmodel.Button button = new de.a12.studio.models.overviewmodel.Button();
    button.setEvent("");
    config.getButtons().add(button);
    refreshMultiSelectionButtonsList();
    multiSelectionButtonsList.getSelectionModel().select(button);
    commitHeaderChange();
  }

  @FXML
  public void onRemoveMultiSelectionButton() {
    de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
    if (button == null) {
      return;
    }
    MultiSelectionConfig config = currentMultiSelectionConfig();
    if (config != null) {
      config.getButtons().remove(button);
    }
    refreshMultiSelectionButtonsList();
    commitHeaderChange();
  }

  private static void setIconName(de.a12.studio.models.overviewmodel.Button button, String value) {
    if (value == null || value.isBlank()) {
      if (button.getIcon() != null) {
        button.getIcon().setName(null);
      }
      return;
    }
    Icon icon = button.getIcon();
    if (icon == null) {
      icon = new Icon();
      button.setIcon(icon);
    }
    icon.setName(value);
  }

  private static Confirmation ensureConfirmation(de.a12.studio.models.overviewmodel.Button button) {
    if (button.getConfirmation() == null) {
      button.setConfirmation(new Confirmation());
    }
    return button.getConfirmation();
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private MultiSelectionConfig ensureMultiSelectionConfig() {
    OverviewConfiguration configuration = ensureConfiguration();
    if (configuration.getMultiSelection() == null) {
      configuration.setMultiSelection(cachedMultiSelectionConfig != null ? cachedMultiSelectionConfig : new MultiSelectionConfig());
    }
    return configuration.getMultiSelection();
  }

  /** One text field per model locale, in {@code grid}, calling {@code onTextChange} with (locale, text) on edit. */
  private void rebuildLocaleGrid(GridPane grid, List<Label> labels, BiConsumer<String, String> onTextChange) {
    grid.getChildren().clear();
    int row = 0;
    for (Locale locale : model.getLocales()) {
      String code = locale.getCode();
      javafx.scene.control.Label localeLabel = new javafx.scene.control.Label(code);
      localeLabel.getStyleClass().add("field-label");

      TextField textField = new TextField(labelText(labels, code));
      textField.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(textField, Priority.ALWAYS);
      textField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel) {
          return;
        }
        onTextChange.accept(code, newValue);
        commitHeaderChange();
      });

      grid.addRow(row++, localeLabel, textField);
    }
  }

  private static String labelText(List<Label> labels, String locale) {
    return labels.stream()
        .filter(label -> locale.equals(label.getLocale()))
        .map(Label::getText)
        .filter(text -> text != null)
        .findFirst()
        .orElse("");
  }

  private static void setLabelText(List<Label> labels, String locale, String text) {
    Label existing = labels.stream()
        .filter(label -> locale.equals(label.getLocale()))
        .findFirst()
        .orElse(null);
    if (existing == null) {
      existing = new Label();
      existing.setLocale(locale);
      labels.add(existing);
    }
    existing.setText(text == null || text.isBlank() ? null : text);
  }

  private static String firstNonBlankText(List<Label> labels) {
    return labels.stream()
        .map(Label::getText)
        .filter(text -> text != null && !text.isBlank())
        .findFirst()
        .orElse(null);
  }

  private String describeMultiSelectionButton(de.a12.studio.models.overviewmodel.Button button) {
    String label = firstNonBlankText(button.getLabel());
    if (label != null) {
      return label;
    }
    return button.getEvent() != null && !button.getEvent().isBlank() ? button.getEvent() : "(new button)";
  }

  private static String orEmpty(String value) {
    return value != null ? value : "";
  }
}
