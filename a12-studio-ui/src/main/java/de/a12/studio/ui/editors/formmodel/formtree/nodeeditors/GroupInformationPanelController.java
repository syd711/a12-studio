package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only "Field Information" panel for a selected {@link AbstractRepeat} node (Inline, Embedded or
 * Detached Repeat). Shows metadata from the Document Model group that the repeat binds to via
 * {@link AbstractRepeat#getGroupRef()}:
 * <ul>
 *   <li><b>Group ID</b> — the raw {@code groupRef} value.</li>
 *   <li><b>Repeatability</b> — the group's {@link GroupConfig#getRepeatability()} value, or "–" when absent.</li>
 *   <li><b>Document Model Path</b> — slash-separated ancestor path resolved by walking the Document Model's
 *       group tree, or just the id when unresolvable.</li>
 *   <li><b>Internal Field Description</b> — the group element's {@code internalDescription} per-locale texts,
 *       shown via the shared {@link LocalizedTextPanelController} in read-only mode.</li>
 * </ul>
 * All content is read-only; no editing takes place here.
 */
@Slf4j
public class GroupInformationPanelController {

  @FXML
  private Label groupIdLabel;

  @FXML
  private Label repeatabilityLabel;

  @FXML
  private Label documentModelPathLabel;

  @FXML
  private LocalizedTextPanelController internalDescriptionController;

  @FXML
  private void initialize() {
    internalDescriptionController.configureCustom("internalDescription",
        StudioBundle.get("internal_field_description"));
    internalDescriptionController.setCollapsed();
  }

  public void setRepeat(@NonNull AbstractRepeat repeat, @Nullable DocumentModel documentModel) {
    String groupRef = repeat.getGroupRef();

    if (groupRef == null || groupRef.isBlank() || documentModel == null
        || documentModel.getContent() == null
        || documentModel.getContent().getModelRoot() == null) {
      clearAll(groupRef);
      return;
    }

    List<GroupElement> roots = documentModel.getContent().getModelRoot().getRootGroups();
    GroupLookupResult result = findGroup(groupRef, roots);

    groupIdLabel.setText(groupRef);

    if (result == null) {
      repeatabilityLabel.setText("–");
      documentModelPathLabel.setText(groupRef);
      internalDescriptionController.setCustom(List::of);
      return;
    }

    GroupConfig cfg = result.group().getGroup();
    if (cfg != null && cfg.getRepeatability() != null) {
      repeatabilityLabel.setText(String.valueOf(cfg.getRepeatability()));
    } else {
      repeatabilityLabel.setText("–");
    }

    StringBuilder path = new StringBuilder();
    for (String segment : result.path()) {
      if (!path.isEmpty()) {
        path.append(" / ");
      }
      path.append(segment);
    }
    documentModelPathLabel.setText(path.toString());

    internalDescriptionController.setCustom(() -> result.group().getInternalDescription());
  }

  private void clearAll(@Nullable String groupRef) {
    groupIdLabel.setText(groupRef != null ? groupRef : "–");
    repeatabilityLabel.setText("–");
    documentModelPathLabel.setText("–");
    internalDescriptionController.setCustom(List::of);
  }

  @Nullable
  private static GroupLookupResult findGroup(@NonNull String groupId,
      @NonNull List<GroupElement> roots) {
    for (GroupElement root : roots) {
      GroupLookupResult result = searchGroup(groupId, root, new ArrayList<>());
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Nullable
  private static GroupLookupResult searchGroup(@NonNull String groupId,
      @NonNull GroupElement group, @NonNull List<String> ancestorPath) {
    List<String> currentPath = new ArrayList<>(ancestorPath);
    String name = group.getName() != null ? group.getName() : group.getId();
    currentPath.add(name != null ? name : "?");

    if (groupId.equals(group.getId())) {
      return new GroupLookupResult(group, currentPath);
    }

    if (group.getGroup() != null && group.getGroup().getElements() != null) {
      for (Element child : group.getGroup().getElements()) {
        if (child instanceof GroupElement childGroup) {
          GroupLookupResult result = searchGroup(groupId, childGroup, currentPath);
          if (result != null) {
            return result;
          }
        }
      }
    }
    return null;
  }

  private record GroupLookupResult(GroupElement group, List<String> path) {}
}
