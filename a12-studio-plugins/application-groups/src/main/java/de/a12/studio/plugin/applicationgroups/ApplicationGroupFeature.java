package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.features.A12StudioFeatureException;
import de.a12.studio.models.features.A12StudioProjectFeature;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.ModelReferenceRewriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Renames every model file of a project with the project's application group name as a
 * "{@code <group>_}" prefix, and stamps a matching {@code applicationGroup} header annotation on
 * every model. Re-running with a different group name strips the previously applied prefix (found
 * via the annotation, not by guessing from the filename) before applying the new one, and every
 * in-project reference to a renamed model - both the structured {@code header.modelReferences} list
 * and the typed content fields the model validators already treat as real references - is rewritten
 * to keep the project internally consistent.
 */
public class ApplicationGroupFeature implements A12StudioProjectFeature<ApplicationGroupResult> {

  public static final String ANNOTATION_NAME = "applicationGroup";

  private static final Pattern GROUP_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");



  public static boolean isValidGroupName(String name) {
    return name != null && GROUP_NAME_PATTERN.matcher(name).matches();
  }

  @Override
  public ApplicationGroupResult apply(Project project) throws A12StudioFeatureException {
    String groupName = ApplicationGroupsSettings.load(project.getFolder()).getApplicationGroupName();
    if (!isValidGroupName(groupName)) {
      throw new A12StudioFeatureException("\"" + groupName + "\" is not a valid application group name.");
    }

    List<ProjectItem> items = new ArrayList<>();
    collectModelItems(project.getRoot(), items);

    Map<ProjectItem, String> newIds = new LinkedHashMap<>();
    Map<String, String> idMap = new HashMap<>();
    for (ProjectItem item : items) {
      A12Model<?> model = item.getModel();
      String newId = groupName + "_" + stripExistingGroupPrefix(model);
      newIds.put(item, newId);
      if (!newId.equals(model.getId())) {
        idMap.put(model.getId(), newId);
      }
    }

    int renamedCount = 0;
    for (ProjectItem item : items) {
      A12Model<?> model = item.getModel();
      String newId = newIds.get(item);
      boolean idChanged = !newId.equals(model.getId());
      setApplicationGroupAnnotation(model, groupName);
      if (idChanged) {
        try {
          item.renameTo(newId + ".json");
        }
        catch (IOException e) {
          throw new A12StudioFeatureException(
              "Failed to rename \"" + item.getName() + "\" to \"" + newId + ".json\": " + e.getMessage(), e);
        }
        renamedCount++;
      }
      else {
        item.save();
      }
    }

    int referencesUpdatedCount = 0;
    if (!idMap.isEmpty()) {
      for (ProjectItem item : items) {
        if (rewriteReferences(item.getModel(), idMap)) {
          item.save();
          referencesUpdatedCount++;
        }
      }
    }

    return new ApplicationGroupResult(groupName, renamedCount, referencesUpdatedCount);
  }

  private static void collectModelItems(ProjectItem item, List<ProjectItem> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectModelItems(child, result);
      }
    }
    else if (item.getModel() != null) {
      result.add(item);
    }
  }

  static String stripExistingGroupPrefix(A12Model<?> model) {
    String id = model.getId();
    String existingGroup = findAnnotationValue(model, ANNOTATION_NAME);
    if (existingGroup != null && id.startsWith(existingGroup + "_")) {
      return id.substring(existingGroup.length() + 1);
    }
    return id;
  }

  static String findAnnotationValue(A12Model<?> model, String name) {
    for (Annotation annotation : model.getAnnotations()) {
      if (name.equals(annotation.getName())) {
        return annotation.getValue();
      }
    }
    return null;
  }

  static void setApplicationGroupAnnotation(A12Model<?> model, String groupName) {
    for (Annotation annotation : model.getAnnotations()) {
      if (ANNOTATION_NAME.equals(annotation.getName())) {
        annotation.setValue(groupName);
        return;
      }
    }
    Annotation annotation = new Annotation();
    annotation.setName(ANNOTATION_NAME);
    annotation.setValue(groupName);
    model.getAnnotations().add(annotation);
  }

  private static <C> boolean rewriteReferences(A12Model<C> model, Map<String, String> idMap) {
    return ModelReferenceRewriter.rewriteReferences(model, idMap);
  }
}
