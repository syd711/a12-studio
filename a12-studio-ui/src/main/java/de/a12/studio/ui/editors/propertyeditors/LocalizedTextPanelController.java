package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Locale;
import de.a12.studio.models.Label;
import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.StringTypeOptions;
import de.a12.studio.models.formmodel.Defaults;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.events.LocalesChangedEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Edits a per-locale text (a list of {@link Label}). Reused for the label,
 * internal description, external description and helper text of a single {@link Element} (via {@link
 * #setElement}, distinguished via {@link #configureLabel} / {@link #configureInternal} / {@link
 * #configureExternal} / {@link #configureHelperText}), for a model's own header labels (via {@link
 * #setModel} after {@link #configureModelLabels}), for an application model {@link Module}'s menu label
 * (via {@link #setModule} after {@link #configureModuleMenuLabel}), for a {@link Case}'s label (via
 * {@link #setCase} after {@link #configureCaseLabel}), and for a {@link Confirmation}'s title or message
 * (via {@link #setConfirmation} after {@link #configureConfirmationTitle} / {@link
 * #configureConfirmationMessage}), and for an {@link de.a12.studio.models.overviewmodel.Column}'s label (via
 * {@link #setColumn} after {@link #configureColumnLabel}). Exactly one configure method must be called once
 * after this controller is loaded from FXML, before setElement/setModel/setModule/setCase/setConfirmation/setColumn;
 * {@code element}, {@link #model}, {@link #module}, {@link #sceneCase}, {@link #confirmation} and {@link
 * #column} are mutually exclusive.
 */
public class LocalizedTextPanelController extends AbstractPropertyEditor {

  // The fieldKey passed to configure() by configureErrorMessages(), reused in validationProperty() so only
  // an instance actually configured as the error-messages panel claims that error, not every reuse of this
  // controller (label, descriptions, helper text) that happens to share the bound element.
  private static final String FIELD_KEY_ERROR_MESSAGES = "errorMessages";

  @FXML
  private GridPane localesGrid;

  private A12Model<?> model;

  private Module module;

  private Case sceneCase;

  private Confirmation confirmation;

  private Column column;

  private Defaults defaults;

  private String buttonLabelAction;

  // Captured whenever setElement/setModel is called, i.e. whenever this panel is (re)bound to whichever
  // project item is currently selected. Used to tell apart a locales-changed event meant for this panel's own
  // model from one fired for a different, unrelated model open in another tab.
  private ProjectItem projectItem;

  private Function<Element, List<Label>> elementTextsAccessor = Element::getExternalDescription;

  private Function<Element, List<Label>> elementTextsWriteAccessor = elementTextsAccessor;

  private Function<A12Model<?>, List<Label>> modelTextsAccessor = A12Model::getLabels;

  private Function<Module, List<Label>> moduleTextsAccessor = module -> module.getOrCreateMenu().getLabel();

  private Function<Confirmation, List<Label>> confirmationTextsAccessor = Confirmation::getTitle;

  private String fieldKey = "external";

  private final Map<String, TextField> textFieldsByLocale = new LinkedHashMap<>();

  public void configureInternal() {
    configure(Element::getInternalDescription, "internal", "DESCRIPTION (INTERNAL)");
  }

  public void configureErrorMessages() {
    configure(LocalizedTextPanelController::getErrorMessages, LocalizedTextPanelController::getOrCreateErrorMessages,
        FIELD_KEY_ERROR_MESSAGES, "ERROR MESSAGES");
  }

  public void configureExternal() {
    configure(Element::getExternalDescription, "external", "DESCRIPTION (EXTERNAL)");
  }

  public void configureHelperText() {
    configure(LocalizedTextPanelController::getHelperText, "helperText", "HELPER TEXT");
  }

  public void configureLabel() {
    configure(LocalizedTextPanelController::getLabel, "label", "LABEL");
  }

  public void configureModelLabels() {
    this.modelTextsAccessor = A12Model::getLabels;
    this.fieldKey = "labels";
    setTitle("LABELS");
    setSettingsKeySuffix("." + fieldKey);
  }

  public void configureModuleMenuLabel() {
    this.fieldKey = "label";
    setTitle("LABEL");
    setSettingsKeySuffix("." + fieldKey);
  }

  public void configureCaseLabel() {
    this.fieldKey = "label";
    setTitle("LABEL");
    setSettingsKeySuffix("." + fieldKey);
  }

  public void configureColumnLabel() {
    this.fieldKey = "label";
    setTitle("LABEL");
    setSettingsKeySuffix("." + fieldKey);
  }

  /**
   * Configures this panel to edit a single entry of {@link Defaults#getButtonLabels()} (e.g. {@code "ADD"},
   * {@code "CANCEL"}) - one of the repeat-widget default button label overrides shown, one action per panel
   * instance, in {@link de.a12.studio.ui.editors.formmodel.dialogs.RepeatDefaultButtonLabelsDialogController}.
   */
  public void configureButtonLabel(@NonNull String action, @NonNull String title) {
    this.buttonLabelAction = action;
    this.fieldKey = "buttonLabel-" + action;
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  public void configureConfirmationTitle() {
    configureConfirmation(Confirmation::getTitle, "confirmationTitle", "CONFIRMATION TITLE");
  }

  public void configureConfirmationMessage() {
    configureConfirmation(Confirmation::getMessage, "confirmationMessage", "CONFIRMATION MESSAGE");
  }

  private void configureConfirmation(Function<Confirmation, List<Label>> textsAccessor, String fieldKey, String title) {
    this.confirmationTextsAccessor = textsAccessor;
    this.fieldKey = fieldKey;
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  private void configure(Function<Element, List<Label>> textsAccessor, String fieldKey, String title) {
    configure(textsAccessor, textsAccessor, fieldKey, title);
  }

  private void configure(Function<Element, List<Label>> readAccessor,
                         Function<Element, List<Label>> writeAccessor, String fieldKey, String title) {
    this.elementTextsAccessor = readAccessor;
    this.elementTextsWriteAccessor = writeAccessor;
    this.fieldKey = fieldKey;
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  @Override
  public void setElement(@NonNull Element element) {
    this.model = null;
    this.module = null;
    this.sceneCase = null;
    this.confirmation = null;
    this.column = null;
    this.defaults = null;
    this.projectItem = Studio.getSelectedProjectItem();
    super.setElement(element);
    buildLocaleFields();
    populateLocaleFields();
  }

  @Override
  protected String validationProperty() {
    return FIELD_KEY_ERROR_MESSAGES.equals(fieldKey) ? ElementProperty.ERROR_MESSAGE : null;
  }

  public void setModel(@NonNull A12Model<?> model) {
    this.element = null;
    this.module = null;
    this.sceneCase = null;
    this.confirmation = null;
    this.column = null;
    this.defaults = null;
    this.model = model;
    this.projectItem = Studio.getSelectedProjectItem();
    buildLocaleFields();
    populateLocaleFields();
  }

  public void setModule(@NonNull Module module) {
    this.element = null;
    this.model = null;
    this.sceneCase = null;
    this.confirmation = null;
    this.column = null;
    this.defaults = null;
    this.module = module;
    this.projectItem = Studio.getSelectedProjectItem();
    buildLocaleFields();
    populateLocaleFields();
  }

  public void setCase(@NonNull Case sceneCase) {
    this.element = null;
    this.model = null;
    this.module = null;
    this.confirmation = null;
    this.column = null;
    this.defaults = null;
    this.sceneCase = sceneCase;
    this.projectItem = Studio.getSelectedProjectItem();
    buildLocaleFields();
    populateLocaleFields();
  }

  public void setColumn(@NonNull Column column) {
    this.element = null;
    this.model = null;
    this.module = null;
    this.sceneCase = null;
    this.confirmation = null;
    this.defaults = null;
    this.column = column;
    this.projectItem = Studio.getSelectedProjectItem();
    buildLocaleFields();
    populateLocaleFields();
  }

  public void setConfirmation(@NonNull Confirmation confirmation) {
    this.element = null;
    this.model = null;
    this.module = null;
    this.sceneCase = null;
    this.column = null;
    this.defaults = null;
    this.confirmation = confirmation;
    this.projectItem = Studio.getSelectedProjectItem();
    buildLocaleFields();
    populateLocaleFields();
  }

  /**
   * Binds this panel (after {@link #configureButtonLabel}) to the currently selected form model's {@link
   * Defaults}. Reading tolerates a missing {@link Defaults#getButtonLabels()} entry for this panel's action
   * (returns an empty list rather than creating one); writing lazily creates that entry - via {@link
   * #getWriteTexts()} - the first time a locale is actually typed into, so merely opening the dialog doesn't
   * add empty {@code "ACTION": {"text": []}} noise to the saved model.
   */
  public void setDefaults(@NonNull Defaults defaults) {
    this.element = null;
    this.model = null;
    this.module = null;
    this.sceneCase = null;
    this.confirmation = null;
    this.column = null;
    this.defaults = defaults;
    this.projectItem = Studio.getSelectedProjectItem();
    buildLocaleFields();
    populateLocaleFields();
  }

  @Override
  public void localesChanged(@NonNull LocalesChangedEvent event) {
    if (event.getItem().equals(projectItem)) {
      buildLocaleFields();
      populateLocaleFields();
    }
  }

  private List<Label> getTexts() {
    if (confirmation != null) {
      return confirmationTextsAccessor.apply(confirmation);
    }
    if (sceneCase != null) {
      return sceneCase.getLabel();
    }
    if (module != null) {
      return moduleTextsAccessor.apply(module);
    }
    if (column != null) {
      return column.getLabel();
    }
    if (defaults != null) {
      TextContainer container = defaults.getButtonLabels().get(buttonLabelAction);
      return container != null ? container.getText() : List.of();
    }
    return model != null ? modelTextsAccessor.apply(model) : elementTextsAccessor.apply(element);
  }

  private void buildLocaleFields() {
    localesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });
    textFieldsByLocale.clear();

    int row = 1;
    for (Locale locale : getModelLocales()) {
      javafx.scene.control.Label localeLabel = new javafx.scene.control.Label(locale.getCode());
      TextField textField = new TextField();
      textField.setId(fieldKey + "-" + locale.getCode());
      textField.setMaxWidth(Double.MAX_VALUE);
      bindTextField(textField, (el, value) -> setLocaleText(locale.getCode(), value));

      localesGrid.addRow(row, localeLabel, textField);
      textFieldsByLocale.put(locale.getCode(), textField);
      row++;
    }
  }

  private void populateLocaleFields() {
    List<Label> texts = getTexts();
    textFieldsByLocale.forEach((localeCode, textField) -> {
      String text = texts.stream()
          .filter(label -> localeCode.equals(label.getLocale()))
          .findFirst()
          .map(Label::getText)
          .orElse("");
      setFieldValue(textField, text);
    });
  }

  private List<Label> getWriteTexts() {
    if (confirmation != null) {
      return confirmationTextsAccessor.apply(confirmation);
    }
    if (sceneCase != null) {
      return sceneCase.getLabel();
    }
    if (module != null) {
      return moduleTextsAccessor.apply(module);
    }
    if (column != null) {
      return column.getLabel();
    }
    if (defaults != null) {
      return defaults.getButtonLabels().computeIfAbsent(buttonLabelAction, action -> new TextContainer()).getText();
    }
    return model != null ? modelTextsAccessor.apply(model) : elementTextsWriteAccessor.apply(element);
  }

  private void setLocaleText(String localeCode, String value) {
    List<Label> texts = getWriteTexts();
    Optional<Label> existing = texts.stream()
        .filter(label -> localeCode.equals(label.getLocale()))
        .findFirst();
    if (existing.isPresent()) {
      existing.get().setText(value);
    } else {
      Label label = new Label();
      label.setLocale(localeCode);
      label.setText(value);
      texts.add(label);
    }
  }

  private static List<Label> getHelperText(Element element) {
    if (element instanceof FieldElement fieldElement && fieldElement.getField() != null) {
      return fieldElement.getField().getHelperText();
    }
    return List.of();
  }

  private static List<Label> getLabel(Element element) {
    if (element instanceof FieldElement fieldElement && fieldElement.getField() != null) {
      return fieldElement.getField().getLabel();
    }
    return List.of();
  }

  private static List<Label> getErrorMessages(Element element) {
    StringTypeOptions options = getStringTypeOptions(element).orElse(null);
    return options != null ? options.getErrorMessage() : List.of();
  }

  private static List<Label> getOrCreateErrorMessages(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof StringFieldType stringFieldType) {
      StringTypeOptions options = stringFieldType.getStringType();
      if (options == null) {
        options = new StringTypeOptions();
        stringFieldType.setStringType(options);
      }
      return options.getErrorMessage();
    }
    return List.of();
  }

  private static Optional<StringTypeOptions> getStringTypeOptions(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof StringFieldType stringFieldType) {
      return Optional.ofNullable(stringFieldType.getStringType());
    }
    return Optional.empty();
  }

  private List<Locale> getModelLocales() {
    if (projectItem == null || projectItem.getModel() == null) {
      return List.of();
    }
    return projectItem.getModel().getLocales();
  }
}
