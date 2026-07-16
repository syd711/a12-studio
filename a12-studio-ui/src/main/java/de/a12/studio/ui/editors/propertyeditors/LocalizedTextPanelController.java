package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.dataservices.models.Locale;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.FieldElement;
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
 * Edits a per-locale text (a list of {@link de.a12.studio.dataservices.models.Label}). Reused for the label, internal
 * description, external description and helper text, distinguished via {@link #configureLabel} / {@link
 * #configureInternal} / {@link #configureExternal} / {@link #configureHelperText}, one of which must be called once
 * after this controller is loaded from FXML.
 */
public class LocalizedTextPanelController extends AbstractPropertyEditor {

  @FXML
  private GridPane localesGrid;

  private Function<Element, List<de.a12.studio.dataservices.models.Label>> textsAccessor = Element::getExternalDescription;

  private String fieldKey = "external";

  private final Map<String, TextField> textFieldsByLocale = new LinkedHashMap<>();

  public void configureInternal() {
    configure(Element::getInternalDescription, "internal", "DESCRIPTION (INTERNAL)");
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

  private void configure(Function<Element, List<de.a12.studio.dataservices.models.Label>> textsAccessor, String fieldKey, String title) {
    this.textsAccessor = textsAccessor;
    this.fieldKey = fieldKey;
    setTitle(title);
    setSettingsKeySuffix("." + fieldKey);
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    buildLocaleFields();
    populateLocaleFields();
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
      bindTextField(textField, (element, value) -> setLocaleText(element, locale.getCode(), value));

      localesGrid.addRow(row, localeLabel, textField);
      textFieldsByLocale.put(locale.getCode(), textField);
      row++;
    }
  }

  private void populateLocaleFields() {
    List<de.a12.studio.dataservices.models.Label> texts = textsAccessor.apply(element);
    textFieldsByLocale.forEach((localeCode, textField) -> {
      String text = texts.stream()
          .filter(label -> localeCode.equals(label.getLocale()))
          .findFirst()
          .map(de.a12.studio.dataservices.models.Label::getText)
          .orElse("");
      setFieldValue(textField, text);
    });
  }

  private void setLocaleText(Element element, String localeCode, String value) {
    List<de.a12.studio.dataservices.models.Label> texts = textsAccessor.apply(element);
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

  private List<Locale> getModelLocales() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null || projectItem.getModel() == null) {
      return List.of();
    }
    return projectItem.getModel().getLocales();
  }
}
