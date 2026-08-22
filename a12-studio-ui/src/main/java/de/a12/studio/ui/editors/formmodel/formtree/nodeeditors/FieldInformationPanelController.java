package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextReadonlyPanelController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only "Field Information" panel for a selected {@link Control} node in the Form Model tree.
 * Shows metadata from the Document Model field that the Control binds to via {@link Control#getElementRef()}:
 * <ul>
 *   <li><b>Field ID</b> — the raw {@code elementRef} value.</li>
 *   <li><b>Data Type</b> — the field's {@link FieldType} name (e.g. {@code "StringType"}), or "–" for groups.</li>
 *   <li><b>Document Model Path</b> — the slash-separated ancestor path resolved by walking the Document
 *       Model's group tree, or just the id when unresolvable (dangling reference, no Document Model).</li>
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
   * Populates the panel from the Document Model element referenced by {@code control.elementRef}.
   * All fields are cleared when no Document Model is linked or the reference cannot be resolved.
   */
  public void setControl(@NonNull Control control, @Nullable DocumentModel documentModel) {
    String elementRef = control.getElementRef();

    if (elementRef == null || elementRef.isBlank() || documentModel == null
        || documentModel.getContent() == null
        || documentModel.getContent().getModelRoot() == null) {
      clearAll(elementRef);
      return;
    }

    // Walk the document model to find the element and its ancestor path.
    List<GroupElement> roots = documentModel.getContent().getModelRoot().getRootGroups();
    ElementLookupResult result = findElement(elementRef, roots);

    fieldIdLabel.setText(elementRef);

    if (result == null) {
      // Dangling reference — show the raw id but nothing else.
      dataTypeLabel.setText("–");
      documentModelPathLabel.setText(elementRef);
      internalDescriptionController.setCustom(List::of);
      return;
    }

    // Data type
    if (result.element() instanceof FieldElement field && field.getField() != null
        && field.getField().getFieldType() != null) {
      dataTypeLabel.setText(field.getField().getFieldType().getType());
    } else {
      dataTypeLabel.setText("–");
    }

    // Document Model Path — slash-joined ancestor names + element name
    StringBuilder path = new StringBuilder();
    for (String segment : result.path()) {
      if (!path.isEmpty()) {
        path.append(" / ");
      }
      path.append(segment);
    }
    documentModelPathLabel.setText(path.toString());

    // Internal description (read-only via setCustom with write supplier pointing to the same list)
    if (result.element() instanceof FieldElement field) {
      internalDescriptionController.setCustom(() -> field.getInternalDescription());
    } else {
      internalDescriptionController.setCustom(List::of);
    }
  }

  private void clearAll(@Nullable String elementRef) {
    fieldIdLabel.setText(elementRef != null ? elementRef : "–");
    dataTypeLabel.setText("–");
    documentModelPathLabel.setText("–");
    internalDescriptionController.setCustom(List::of);
  }

  // Recursively searches the group tree for the element with the given id.
  // Returns the element and its full path (list of names from root to the element itself),
  // or null when not found.
  @Nullable
  private static ElementLookupResult findElement(
      @NonNull String elementId,
      @NonNull List<GroupElement> roots) {
    for (GroupElement root : roots) {
      ElementLookupResult result = searchGroup(elementId, root, new ArrayList<>());
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Nullable
  private static ElementLookupResult searchGroup(
      @NonNull String elementId,
      @NonNull GroupElement group,
      @NonNull List<String> ancestorPath) {

    List<String> currentPath = new ArrayList<>(ancestorPath);
    String groupName = group.getName() != null ? group.getName() : group.getId();
    currentPath.add(groupName != null ? groupName : "?");

    if (elementId.equals(group.getId())) {
      return new ElementLookupResult(group, currentPath);
    }

    if (group.getGroup() != null && group.getGroup().getElements() != null) {
      for (Element child : group.getGroup().getElements()) {
        String childName = child.getName() != null ? child.getName() : child.getId();
        if (elementId.equals(child.getId())) {
          List<String> childPath = new ArrayList<>(currentPath);
          childPath.add(childName != null ? childName : "?");
          return new ElementLookupResult(child, childPath);
        }
        if (child instanceof GroupElement childGroup) {
          ElementLookupResult result = searchGroup(elementId, childGroup, currentPath);
          if (result != null) {
            return result;
          }
        }
      }
    }
    return null;
  }

  private record ElementLookupResult(Element element, List<String> path) {}
}
