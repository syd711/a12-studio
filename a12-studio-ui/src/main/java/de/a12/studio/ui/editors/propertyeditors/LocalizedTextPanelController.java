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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
 *
 * <p>{@link LocalizedTextAreaPanelController} subclasses this to edit the same per-locale texts with a {@link
 * javafx.scene.control.TextArea} per locale instead of a {@link javafx.scene.control.TextField}, overriding
 * only {@link #createLocaleField}.
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

  // Generic escape hatch for owners that don't warrant their own dedicated field/setXxx pair below (see
  // configureCustom/setCustom): a plain List<Label> living somewhere on an arbitrary POJO, read and written via
  // a caller-supplied Supplier rather than a typed accessor function. customTextsSupplier must be side-effect-free
  // (used to repopulate the fields on every setCustom/localesChanged, including when nothing has actually been
  // typed yet); customTextsWriteSupplier is only ever invoked once the user actually edits a field, so it's the
  // one allowed to lazily materialize a parent object the read path found absent. Both point at the same supplier
  // when the caller passes a single one (the common case: an owner, e.g. a FilterGroup, that already exists and
  // whose List<Label> field is safe to read without creating anything).
  private Supplier<List<Label>> customTextsSupplier;
  private Supplier<List<Label>> customTextsWriteSupplier;

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

  private final Map<String, TextInputControl> textFieldsByLocale = new LinkedHashMap<>();

  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

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
   * Configures this panel to edit an arbitrary {@code List<Label>}, read/written via {@code textsSupplier}
   * (e.g. {@code filterGroup::getLabel}), for an owner type that doesn't warrant its own dedicated {@code
   * setXxx} pair above. Follow with {@link #setCustom}.
   */
  public void configureCustom(@NonNull String fieldKey, @NonNull String title) {
    this.fieldKey = fieldKey;
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  /**
   * Configures this panel to edit a single entry of {@link Defaults#getButtonLabels()} (e.g. {@code "ADD"},
   * {@code "CANCEL"}) - one of the repeat-widget default button label overrides shown, one action per panel
   * instance, in {@link de.a12.studio.ui.editors.formmodel.FormModelEditorController}.
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
    this.customTextsSupplier = null;
    this.customTextsWriteSupplier = null;
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
    this.customTextsSupplier = null;
    this.customTextsWriteSupplier = null;
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
    this.customTextsSupplier = null;
    this.customTextsWriteSupplier = null;
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
    this.customTextsSupplier = null;
    this.customTextsWriteSupplier = null;
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
    this.customTextsSupplier = null;
    this.customTextsWriteSupplier = null;
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
    this.customTextsSupplier = null;
    this.customTextsWriteSupplier = null;
    this.confirmation = confirmation;
    this.projectItem = Studio.getSelectedProjectItem();
    buildLocaleFields();
    populateLocaleFields();
  }

  /**
   * Binds this panel (after {@link #configureCustom}) directly to a caller-supplied {@code List<Label>}, e.g.
   * {@code filterGroup::getLabel}. Used for owners not otherwise represented above (see {@link
   * de.a12.studio.models.overviewmodel.FilterGroup}, {@link de.a12.studio.models.overviewmodel.FilterItem}).
   * {@code textsSupplier} is used for both reading (to repopulate the fields, including right away as this
   * method runs) and writing, so it must be safe to call before the user has typed anything - i.e. the owner
   * (e.g. the {@code FilterGroup}) must already exist. For an owner whose parent container may not exist yet
   * and would otherwise have to be lazily created just to answer a read, use {@link #setCustom(Supplier,
   * Supplier)} instead.
   */
  public void setCustom(@NonNull Supplier<List<Label>> textsSupplier) {
    setCustom(textsSupplier, textsSupplier);
  }

  /**
   * Same as {@link #setCustom(Supplier)}, but for an owner whose parent container may not exist yet on the
   * model (e.g. {@link de.a12.studio.models.overviewmodel.FilterSelectorConfig#getHeaderSubtitle()}, {@link
   * de.a12.studio.models.overviewmodel.FilterTriggerValue#getLabel()} - both nested under {@code
   * newFilterConfiguration}, which a model with no custom filter configured at all may never have). {@code
   * reader} must be side-effect-free - it's called to repopulate the fields on every {@link #setCustom}/{@link
   * #localesChanged}, including merely opening the panel, so it must tolerate a missing parent by returning
   * {@link List#of()} rather than creating one. {@code writer} is only ever invoked once the user actually
   * types into a locale field (see {@link #setLocaleText}), so it's the one allowed to lazily materialize
   * whatever parent chain is missing - mirrors {@link #setDefaults}'s {@code getWriteTexts()} handling of a
   * missing {@link Defaults#getButtonLabels()} entry.
   */
  public void setCustom(@NonNull Supplier<List<Label>> reader, @NonNull Supplier<List<Label>> writer) {
    this.element = null;
    this.model = null;
    this.module = null;
    this.sceneCase = null;
    this.column = null;
    this.confirmation = null;
    this.defaults = null;
    this.customTextsSupplier = reader;
    this.customTextsWriteSupplier = writer;
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
    this.customTextsSupplier = null;
    this.customTextsWriteSupplier = null;
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
    if (customTextsSupplier != null) {
      return customTextsSupplier.get();
    }
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
      TextInputControl field = createLocaleField(fieldKey + "-" + locale.getCode());
      field.setMaxWidth(Double.MAX_VALUE);
      bindLocaleField(field, (el, value) -> setLocaleText(locale.getCode(), value));

      localesGrid.addRow(row, localeLabel, field);
      textFieldsByLocale.put(locale.getCode(), field);
      row++;
    }
  }

  /**
   * Creates the control used for a single locale's row. Overridden by {@link LocalizedTextAreaPanelController}
   * to use a {@link TextArea} instead of a {@link TextField}.
   */
  protected TextInputControl createLocaleField(@NonNull String id) {
    TextField textField = new TextField();
    textField.setId(id);
    return textField;
  }

  /**
   * Wires {@code field} (as created by {@link #createLocaleField}) up to {@code setter} via {@link
   * #bindTextField}/{@link #bindTextArea}, whichever matches its actual type.
   */
  private void bindLocaleField(@NonNull TextInputControl field, @NonNull BiConsumer<Element, String> setter) {
    if (field instanceof TextArea textArea) {
      bindTextArea(textArea, setter);
    } else if (field instanceof TextField textField) {
      bindTextField(textField, setter);
    }
  }

  private void populateLocaleFields() {
    List<Label> texts = getTexts();
    textFieldsByLocale.forEach((localeCode, field) -> {
      String text = texts.stream()
          .filter(label -> localeCode.equals(label.getLocale()))
          .findFirst()
          .map(Label::getText)
          .orElse("");
      setLocaleFieldValue(field, text);
    });
  }

  /**
   * Sets {@code field}'s value without triggering the save/validation cycle, dispatching to whichever of
   * {@link #setFieldValue(TextField, String)}/{@link #setFieldValue(TextArea, String)} matches its actual type.
   */
  private void setLocaleFieldValue(@NonNull TextInputControl field, String value) {
    if (field instanceof TextArea textArea) {
      setFieldValue(textArea, value);
    } else if (field instanceof TextField textField) {
      setFieldValue(textField, value);
    }
  }

  private List<Label> getWriteTexts() {
    if (customTextsWriteSupplier != null) {
      return customTextsWriteSupplier.get();
    }
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
