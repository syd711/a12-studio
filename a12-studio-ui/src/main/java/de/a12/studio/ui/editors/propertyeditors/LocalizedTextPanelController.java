package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.dataservices.models.Locale;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.FieldElement;
import de.a12.studio.dataservices.models.documentmodel.StringFieldType;
import de.a12.studio.dataservices.models.documentmodel.StringTypeOptions;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
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
 * Edits a per-locale text (a list of {@link de.a12.studio.dataservices.models.Label}). Reused for the label,
 * internal description, external description and helper text of a single {@link Element} (via {@link
 * #setElement}, distinguished via {@link #configureLabel} / {@link #configureInternal} / {@link
 * #configureExternal} / {@link #configureHelperText}), as well as for a model's own header labels (via {@link
 * #setModel} after {@link #configureModelLabels}). Exactly one configure method must be called once after this
 * controller is loaded from FXML, before setElement/setModel; {@code element} and {@link #model} are mutually
 * exclusive.
 */
public class LocalizedTextPanelController extends AbstractPropertyEditor {

  @FXML
  private GridPane localesGrid;

  private A12Model model;

  private Function<Element, List<de.a12.studio.dataservices.models.Label>> elementTextsAccessor = Element::getExternalDescription;

  private Function<Element, List<de.a12.studio.dataservices.models.Label>> elementTextsWriteAccessor = elementTextsAccessor;

  private Function<A12Model, List<de.a12.studio.dataservices.models.Label>> modelTextsAccessor = A12Model::getLabels;

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

  private void configure(Function<Element, List<de.a12.studio.dataservices.models.Label>> textsAccessor, String fieldKey, String title) {
    configure(textsAccessor, textsAccessor, fieldKey, title);
  }

  private void configure(Function<Element, List<de.a12.studio.dataservices.models.Label>> readAccessor,
      Function<Element, List<de.a12.studio.dataservices.models.Label>> writeAccessor, String fieldKey, String title) {
    this.elementTextsAccessor = readAccessor;
    this.elementTextsWriteAccessor = writeAccessor;
    this.fieldKey = fieldKey;
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  @Override
  public void setElement(@NonNull Element element) {
    this.model = null;
    super.setElement(element);
    buildLocaleFields();
    populateLocaleFields();
  }

  public void setModel(@NonNull A12Model model) {
    this.element = null;
    this.model = model;
    buildLocaleFields();
    populateLocaleFields();
  }

  private List<de.a12.studio.dataservices.models.Label> getTexts() {
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
    List<de.a12.studio.dataservices.models.Label> texts = getTexts();
    textFieldsByLocale.forEach((localeCode, textField) -> {
      String text = texts.stream()
          .filter(label -> localeCode.equals(label.getLocale()))
          .findFirst()
          .map(de.a12.studio.dataservices.models.Label::getText)
          .orElse("");
      setFieldValue(textField, text);
    });
  }

  private List<de.a12.studio.dataservices.models.Label> getWriteTexts() {
    return model != null ? modelTextsAccessor.apply(model) : elementTextsWriteAccessor.apply(element);
  }

  private void setLocaleText(String localeCode, String value) {
    List<de.a12.studio.dataservices.models.Label> texts = getWriteTexts();
    Optional<de.a12.studio.dataservices.models.Label> existing = texts.stream()
        .filter(label -> localeCode.equals(label.getLocale()))
        .findFirst();
    if (existing.isPresent()) {
      existing.get().setText(value);
    } else {
      de.a12.studio.dataservices.models.Label label = new de.a12.studio.dataservices.models.Label();
      label.setLocale(localeCode);
      label.setText(value);
      texts.add(label);
    }
  }

  private static List<de.a12.studio.dataservices.models.Label> getHelperText(Element element) {
    if (element instanceof FieldElement fieldElement && fieldElement.getField() != null) {
      return fieldElement.getField().getHelperText();
    }
    return List.of();
  }

  private static List<de.a12.studio.dataservices.models.Label> getLabel(Element element) {
    if (element instanceof FieldElement fieldElement && fieldElement.getField() != null) {
      return fieldElement.getField().getLabel();
    }
    return List.of();
  }

  private static List<de.a12.studio.dataservices.models.Label> getErrorMessages(Element element) {
    StringTypeOptions options = getStringTypeOptions(element).orElse(null);
    return options != null ? options.getErrorMessage() : List.of();
  }

  private static List<de.a12.studio.dataservices.models.Label> getOrCreateErrorMessages(Element element) {
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
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null || projectItem.getModel() == null) {
      return List.of();
    }
    return projectItem.getModel().getLocales();
  }
}
