package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Locale;
import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.StringTypeOptions;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.events.LocalesChangedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * Edits a per-locale text (a list of {@link Label}). Reused for the label,
 * internal description, external description and helper text of a single {@link Element} (via {@link
 * #setElement}, distinguished via {@link #configureLabel} / {@link #configureInternal} / {@link
 * #configureExternal} / {@link #configureHelperText}), as well as for a model's own header labels (via {@link
 * #setModel} after {@link #configureModelLabels}). Exactly one configure method must be called once after this
 * controller is loaded from FXML, before setElement/setModel; {@code element} and {@link #model} are mutually
 * exclusive.
 */
public class LocalizedTextPanelController extends AbstractPropertyEditor implements StudioEventListener {

  @FXML
  private GridPane localesGrid;

  private A12Model<?> model;

  // Captured whenever setElement/setModel is called, i.e. whenever this panel is (re)bound to whichever
  // project item is currently selected. Used to tell apart a locales-changed event meant for this panel's own
  // model from one fired for a different, unrelated model open in another tab.
  private ProjectItem projectItem;

  private Function<Element, List<Label>> elementTextsAccessor = Element::getExternalDescription;

  private Function<Element, List<Label>> elementTextsWriteAccessor = elementTextsAccessor;

  private Function<A12Model<?>, List<Label>> modelTextsAccessor = A12Model::getLabels;

  private String fieldKey = "external";

  private final Map<String, TextField> textFieldsByLocale = new LinkedHashMap<>();

  public void configureInternal() {
    configure(Element::getInternalDescription, "internal", "DESCRIPTION (INTERNAL)");
  }

  public void configureErrorMessages() {
    configure(LocalizedTextPanelController::getErrorMessages, LocalizedTextPanelController::getOrCreateErrorMessages, "errorMessages",
        "ERROR MESSAGES");
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
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    StudioEventManager.getInstance().addListener(this);
  }

  @Override
  public void setElement(@NonNull Element element) {
    this.model = null;
    this.projectItem = Studio.getSelectedProjectItem();
    super.setElement(element);
    buildLocaleFields();
    populateLocaleFields();
  }

  public void setModel(@NonNull A12Model<?> model) {
    this.element = null;
    this.model = model;
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
