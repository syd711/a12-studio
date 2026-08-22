package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.LocalizedText;
import de.a12.studio.models.formmodel.MultilingualText;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextReadonlyPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Label" property editor for a selected {@link Control} node:
 * <ul>
 *   <li><b>Document Model</b> — the {@link FieldElement#getField()}'s {@code label} on the Document Model
 *       element referenced by {@link Control#getElementRef()}, shown read-only here via {@link
 *       LocalizedTextReadonlyPanelController} for reference; it isn't editable from this panel.</li>
 *   <li><b>Field Configuration</b> — the model-wide {@link FieldConfigEntry#getLabel()} for the bound field,
 *       stored in {@link FormModelContent#getFieldConfiguration()}, applying to every Control that references
 *       this field unless a Control-level override is set. Shown read-only here via {@link
 *       LocalizedTextReadonlyPanelController} for reference; it isn't editable from this panel.</li>
 *   <li><b>Control</b> — the per-Control {@link Control#getLabel()} override, which takes precedence, editable
 *       via {@link LocalizedTextTypePanelController} (per-locale text or expression).</li>
 * </ul>
 */
public class ControlLabelPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private LocalizedTextReadonlyPanelController documentModelLabelController;

  @FXML
  private LocalizedTextReadonlyPanelController fieldConfigLabelController;

  @FXML
  private LocalizedTextTypePanelController controlLabelController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    documentModelLabelController.configureCustom("documentModelLabel", "Document Model");
    fieldConfigLabelController.configureCustom("fieldConfigLabel", "Field Configuration");
    controlLabelController.configureCustom("controlLabel", "Control");
  }

  public void setControl(@NonNull Control control, @Nullable DocumentModel documentModel, @Nullable FormModelContent content) {
    FieldConfigEntry entry = FieldConfigEntryHelper.findOrCreate(control, content);
    documentModelLabelController.setCustom(() -> findFieldLabel(control.getElementRef(), documentModel));
    fieldConfigLabelController.setCustom(() -> textsOf(entry.getLabel()));
    controlLabelController.setCustom(control::getLabel, control::setLabel);
  }

  private static List<Label> textsOf(LocalizedText value) {
    if (value instanceof MultilingualText multilingualText && multilingualText.getMultilingualText() != null) {
      return multilingualText.getMultilingualText().getText();
    }
    return List.of();
  }

  // Resolves the Document Model field element referenced by elementRef and returns its Label texts,
  // or an empty list when no Document Model is linked or the reference cannot be resolved.
  private static List<Label> findFieldLabel(@Nullable String elementRef, @Nullable DocumentModel documentModel) {
    if (elementRef == null || elementRef.isBlank()
        || documentModel == null
        || documentModel.getContent() == null
        || documentModel.getContent().getModelRoot() == null) {
      return List.of();
    }
    for (GroupElement root : documentModel.getContent().getModelRoot().getRootGroups()) {
      List<Label> label = findFieldLabel(elementRef, root);
      if (label != null) {
        return label;
      }
    }
    return List.of();
  }

  @Nullable
  private static List<Label> findFieldLabel(@NonNull String elementRef, @NonNull Element element) {
    if (elementRef.equals(element.getId())) {
      if (element instanceof FieldElement field && field.getField() != null) {
        return field.getField().getLabel();
      }
      return List.of();
    }
    if (element instanceof GroupElement group && group.getGroup() != null) {
      for (Element child : group.getGroup().getElements()) {
        List<Label> label = findFieldLabel(elementRef, child);
        if (label != null) {
          return label;
        }
      }
    }
    return null;
  }
}
