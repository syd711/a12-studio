package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextReadonlyPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Read-only "Field Information" panel for a selected {@link Control} node in the Form Model tree.
 * Shows metadata from the Document Model field that the Control binds to via {@link Control#getElementRef()}:
 * <ul>
 *   <li><b>Field ID</b> — the raw {@code elementRef} value.</li>
 *   <li><b>Data Type</b> — the field's {@code FieldType} name (e.g. {@code "StringType"}), or "–" for groups.</li>
 *   <li><b>Document Model Path</b> — the slash-separated ancestor path resolved via {@link ElementIndex},
 *       which also follows compound {@code <includeGroupId>_<targetId>} references into an included Document
 *       Model (see {@link ElementIndex#resolveElement}) - or just the id when unresolvable (dangling
 *       reference, no Document Model).</li>
 *   <li><b>Internal Field Description</b> — the field element's {@code internalDescription} per-locale texts,
 *       shown via the shared {@link LocalizedTextReadonlyPanelController}.</li>
 * </ul>
 * All content is read-only; no editing takes place here.
 */
@Slf4j
public class FieldInformationPanelController {

  @FXML
  private Label fieldIdLabel;

  @FXML
  private Label dataTypeLabel;

  @FXML
  private Label documentModelPathLabel;

  @FXML
  private LocalizedTextReadonlyPanelController internalDescriptionController;

  @FXML
  private void initialize() {
    internalDescriptionController.configureCustom("internalDescription", StudioBundle.get("internal_field_description"));
  }

  /**
   * Populates the panel from the Document Model element referenced by {@code control.elementRef}, resolved
   * via {@code elementIndex} (built over the Form Model's linked Document Model, following compound
   * {@code <includeGroupId>_<targetId>} references into included Document Models along the way - see {@link
   * ElementIndex#resolveElement}). All fields are cleared when no Document Model is linked or the reference
   * cannot be resolved.
   */
  public void setControl(@NonNull Control control, @Nullable ElementIndex elementIndex) {
    String elementRef = control.getElementRef();

    if (elementRef == null || elementRef.isBlank() || elementIndex == null) {
      clearAll(elementRef);
      return;
    }

    fieldIdLabel.setText(elementRef);

    Optional<Element> resolved = elementIndex.resolveElement(elementRef);

    // Data type
    dataTypeLabel.setText(resolved.map(FieldInformationPanelController::describeDataType).orElse("–"));

    // Document Model Path — slash-separated, as resolved by ElementIndex (falls back to the raw id
    // when unresolvable, i.e. a dangling reference).
    String path = elementIndex.resolveDisplayPath(elementRef);
    documentModelPathLabel.setText(String.join(" / ",
        Arrays.stream(path.split("/")).filter(segment -> !segment.isEmpty()).toList()));

    // Internal description (read-only via setCustom with write supplier pointing to the same list)
    internalDescriptionController.setCustom(() -> resolved.map(Element::getInternalDescription).orElseGet(List::of));
  }

  /**
   * Mirrors {@code ElementViewModel.getType()}: a plain {@link FieldElement} shows its {@code FieldType},
   * while a {@link GroupElement} with {@code usageType == "multi-select"} (e.g. a "Pronouns" multi-select
   * group) is not a {@link FieldElement} at all, so it must be special-cased here rather than falling
   * through to "–".
   */
  private static String describeDataType(Element element) {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() != null) {
      return fieldElement.getField().getFieldType().getType();
    }
    if (element instanceof GroupElement groupElement
        && groupElement.getGroup() != null
        && GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(groupElement.getGroup().getUsageType())) {
      return "Multi-Select";
    }
    return "–";
  }

  private void clearAll(@Nullable String elementRef) {
    fieldIdLabel.setText(elementRef != null ? elementRef : "–");
    dataTypeLabel.setText("–");
    documentModelPathLabel.setText("–");
    internalDescriptionController.setCustom(List::of);
  }
}
