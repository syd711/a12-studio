package de.a12.studio.ui.editors.formmodel.modelsettings;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.formmodel.AmountSuffix;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits {@link FormModel}'s "General Settings" (SME's {@code FormModelFrame-form.json} {@code
 * controlgrid-64f1b}): the Document Model the form binds its Controls' data to, the global Amount Suffix,
 * the model-wide Readonly Presentation, and whether required Fields are marked with an asterisk. Not bound to
 * a single {@link Element} (these fields live on the model's header/content), so only {@link #setModel} is
 * used, following the model-header pattern (e.g. {@link
 * de.a12.studio.ui.editors.applicationmodel.LayoutPanelController}).
 * <p>
 * The Document Model field isn't a {@link FormModelContent} property at all - like SME's own {@code
 * technicalField_documentModel}, it's synthesized from/written back to the header's {@link ModelReference}
 * with {@link ModelReference#PURPOSE_DATA_BINDING}, which is why this panel takes over from the generic
 * {@code ModelReferencesPanelController} for Form Models (see {@code ModelSettingsDialog}).
 */
public class GeneralSettingsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final Map<String, String> AMOUNT_SUFFIX_TYPE_LABELS = new LinkedHashMap<>();
  private static final Map<String, String> READONLY_PRESENTATION_LABELS = new LinkedHashMap<>();
  private static final Map<String, String> MARKING_OF_REQUIRED_FIELDS_LABELS = new LinkedHashMap<>();

  private static final String AMOUNT_SUFFIX_TYPE_DYNAMIC = "dynamic";

  /** Default {@link AmountSuffix#getType()} behavior when the field is unset. */
  private static final String AMOUNT_SUFFIX_TYPE_DEFAULT = "static";

  /** Default {@link FormModelContent#getReadonlyPresentation()} behavior when the field is unset. */
  private static final String READONLY_PRESENTATION_DEFAULT = "INPUT";

  /** Default {@link FormModelContent#getMarkingOfRequiredFields()} behavior when the field is unset. */
  private static final String MARKING_OF_REQUIRED_FIELDS_DEFAULT = "REQUIRED";

  static {
    AMOUNT_SUFFIX_TYPE_LABELS.put(AMOUNT_SUFFIX_TYPE_DEFAULT, "Static");
    AMOUNT_SUFFIX_TYPE_LABELS.put(AMOUNT_SUFFIX_TYPE_DYNAMIC, "Dynamic");

    READONLY_PRESENTATION_LABELS.put(READONLY_PRESENTATION_DEFAULT, "input");
    READONLY_PRESENTATION_LABELS.put("TEXT", "text output");

    MARKING_OF_REQUIRED_FIELDS_LABELS.put(MARKING_OF_REQUIRED_FIELDS_DEFAULT, "If required");
    MARKING_OF_REQUIRED_FIELDS_LABELS.put("NONE", "Never");
    MARKING_OF_REQUIRED_FIELDS_LABELS.put("ALWAYS", "Always");
  }

  @FXML
  private ComboBox<String> documentModelCombo;

  @FXML
  private Button openDocumentModelButton;

  @FXML
  private ComboBox<String> amountSuffixTypeCombo;

  @FXML
  private TextField amountSuffixValueField;

  @FXML
  private ComboBox<String> amountSuffixFieldRefCombo;

  @FXML
  private ComboBox<String> readonlyPresentationCombo;

  @FXML
  private ComboBox<String> markingOfRequiredFieldsCombo;

  private FormModel model;

  private List<DocumentModel> documentModels = List.of();

  private ElementIndex fieldIndex;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    applyLabelConverter(amountSuffixTypeCombo, AMOUNT_SUFFIX_TYPE_LABELS);
    applyLabelConverter(readonlyPresentationCombo, READONLY_PRESENTATION_LABELS);
    applyLabelConverter(markingOfRequiredFieldsCombo, MARKING_OF_REQUIRED_FIELDS_LABELS);
    amountSuffixTypeCombo.getItems().addAll(AMOUNT_SUFFIX_TYPE_LABELS.keySet());
    readonlyPresentationCombo.getItems().addAll(READONLY_PRESENTATION_LABELS.keySet());
    markingOfRequiredFieldsCombo.getItems().addAll(MARKING_OF_REQUIRED_FIELDS_LABELS.keySet());
    applyFieldRefConverter();

    bindComboBox(documentModelCombo, (element, value) -> {
      applyDocumentModelReference(value);
      refreshFieldRefItems();
    });
    openDocumentModelButton.disableProperty().bind(documentModelCombo.valueProperty().isNull());

    bindComboBox(amountSuffixTypeCombo, (element, value) -> {
      getOrCreateAmountSuffix().setType(value);
      if (AMOUNT_SUFFIX_TYPE_DYNAMIC.equals(value)) {
        getOrCreateAmountSuffix().setValue(null);
      } else {
        getOrCreateAmountSuffix().setFieldRef(null);
      }
      updateAmountSuffixVisibility();
    });
    bindTextField(amountSuffixValueField, (element, value) -> getOrCreateAmountSuffix().setValue(value));
    bindComboBox(amountSuffixFieldRefCombo, (element, value) -> getOrCreateAmountSuffix().setFieldRef(value));

    bindComboBox(readonlyPresentationCombo, (element, value) -> getContent().setReadonlyPresentation(value));
    bindComboBox(markingOfRequiredFieldsCombo, (element, value) -> getContent().setMarkingOfRequiredFields(value));
  }

  @FXML
  private void onOpenDocumentModel() {
    String reference = documentModelCombo.getValue();
    if (reference != null) {
      ProjectDocumentModels.openModelInEditor(reference);
    }
  }

  /** Hides this panel entirely for model types other than {@link FormModel}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull FormModel model, @NonNull List<DocumentModel> documentModels) {
    this.model = model;
    this.documentModels = documentModels;

    setComboBoxItems(documentModelCombo, documentModels.stream()
        .map(DocumentModel::getId)
        .sorted(Comparator.naturalOrder())
        .toList());
    setFieldValue(documentModelCombo, currentDocumentModelId());
    refreshFieldRefItems();

    AmountSuffix amountSuffix = getContent().getAmountSuffix();
    String amountSuffixType = amountSuffix != null ? amountSuffix.getType() : null;
    setFieldValue(amountSuffixTypeCombo, amountSuffixType == null ? AMOUNT_SUFFIX_TYPE_DEFAULT : amountSuffixType);
    setFieldValue(amountSuffixValueField, amountSuffix != null ? amountSuffix.getValue() : null);
    setFieldValue(amountSuffixFieldRefCombo, amountSuffix != null ? amountSuffix.getFieldRef() : null);
    updateAmountSuffixVisibility();

    String readonlyPresentation = getContent().getReadonlyPresentation();
    setFieldValue(readonlyPresentationCombo,
        readonlyPresentation == null ? READONLY_PRESENTATION_DEFAULT : readonlyPresentation);
    String markingOfRequiredFields = getContent().getMarkingOfRequiredFields();
    setFieldValue(markingOfRequiredFieldsCombo,
        markingOfRequiredFields == null ? MARKING_OF_REQUIRED_FIELDS_DEFAULT : markingOfRequiredFields);
  }

  private FormModelContent getContent() {
    FormModelContent content = model.getContent();
    if (content == null) {
      content = new FormModelContent();
      model.setContent(content);
    }
    return content;
  }

  private AmountSuffix getOrCreateAmountSuffix() {
    FormModelContent content = getContent();
    AmountSuffix amountSuffix = content.getAmountSuffix();
    if (amountSuffix == null) {
      amountSuffix = new AmountSuffix();
      content.setAmountSuffix(amountSuffix);
    }
    return amountSuffix;
  }

  private void updateAmountSuffixVisibility() {
    boolean dynamic = AMOUNT_SUFFIX_TYPE_DYNAMIC.equals(amountSuffixTypeCombo.getValue());
    amountSuffixValueField.setVisible(!dynamic);
    amountSuffixValueField.setManaged(!dynamic);
    amountSuffixFieldRefCombo.setVisible(dynamic);
    amountSuffixFieldRefCombo.setManaged(dynamic);
  }

  private String currentDocumentModelId() {
    if (model.getModelReferences() == null) {
      return null;
    }
    return model.getModelReferences().stream()
        .filter(this::isDataBindingReference)
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
  }

  private boolean isDataBindingReference(ModelReference reference) {
    return reference.getModelType() == ModelType.DOCUMENT && ModelReference.PURPOSE_DATA_BINDING.equals(reference.getPurpose());
  }

  /**
   * Rebuilds the header's {@link ModelReference#PURPOSE_DATA_BINDING} reference to match the selected
   * Document Model, matching the shape of an existing FM's reference (see e.g. {@code Invoice_FM.json}):
   * {@code alias} equal to {@code reference}, {@code modelType} DOCUMENT.
   */
  private void applyDocumentModelReference(String dmId) {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(this::isDataBindingReference);
    if (dmId != null && !dmId.isBlank()) {
      ModelReference reference = new ModelReference();
      reference.setModelType(ModelType.DOCUMENT);
      reference.setPurpose(ModelReference.PURPOSE_DATA_BINDING);
      reference.setAlias(dmId);
      reference.setReference(dmId);
      references.add(reference);
    }
  }

  private DocumentModel resolveSelectedDocumentModel() {
    String id = documentModelCombo.getValue();
    if (id == null) {
      return null;
    }
    return documentModels.stream().filter(candidate -> id.equals(candidate.getId())).findFirst().orElse(null);
  }

  /** Re-points {@link #amountSuffixFieldRefCombo} at the currently selected Document Model's Fields. */
  private void refreshFieldRefItems() {
    DocumentModel selected = resolveSelectedDocumentModel();
    fieldIndex = selected != null && selected.getContent() != null && selected.getContent().getModelRoot() != null
        ? new ElementIndex(selected)
        : null;

    List<String> fieldIds = fieldIndex == null ? List.of() : fieldIndex.allElements().stream()
        .filter(ElementIndex::isField)
        .filter(element -> element.getId() != null)
        .sorted(Comparator.comparing(fieldIndex::getPath))
        .map(Element::getId)
        .toList();
    setComboBoxItems(amountSuffixFieldRefCombo, fieldIds);
  }

  private void applyFieldRefConverter() {
    amountSuffixFieldRefCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String elementId) {
        return elementId == null ? "" : (fieldIndex != null ? fieldIndex.resolveDisplayPath(elementId) : elementId);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
  }

  private static void applyLabelConverter(ComboBox<String> comboBox, Map<String, String> labels) {
    comboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? "" : labels.getOrDefault(value, value);
      }

      @Override
      public String fromString(String displayName) {
        return labels.entrySet().stream()
            .filter(entry -> entry.getValue().equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
      }
    });
  }
}
