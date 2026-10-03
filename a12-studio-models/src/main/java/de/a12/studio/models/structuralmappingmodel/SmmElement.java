package de.a12.studio.models.structuralmappingmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A group or field of the source or target Document Model of a Structural Mapping Model, as the two trees of its
 * editor show them. Rules and computations are not elements a mapping can refer to and are left out.
 *
 * @param name         the element name
 * @param fullName     the full name the SMM refers to the element by, e.g. {@code /Person/Addresses/City}
 * @param group        whether this is a group (otherwise a field)
 * @param repeatability how often a group can repeat; {@code null} for fields and groups without a limit set
 * @param dataType     the field type without the {@code Type} suffix, e.g. {@code String}; empty for groups
 * @param children     the groups and fields of a group
 */
public record SmmElement(String name, String fullName, boolean group, @Nullable Integer repeatability, String dataType,
    List<SmmElement> children) {

  /**
   * Reads the root groups of a Document Model json (the content the kernel computed for the SMM: includes and
   * type definitions are already expanded).
   *
   * @throws IllegalArgumentException if the json is not a Document Model
   */
  public static List<SmmElement> parseRoots(String documentModelJson) {
    DocumentModel model;
    try {
      model = JsonSettings.objectMapper.readValue(documentModelJson, DocumentModel.class);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Not a document model: " + e.getMessage(), e);
    }
    List<SmmElement> roots = new ArrayList<>();
    if (model.getContent() != null && model.getContent().getModelRoot() != null) {
      for (GroupElement group : model.getContent().getModelRoot().getRootGroups()) {
        roots.add(of(group, ""));
      }
    }
    return roots;
  }

  private static SmmElement of(Element element, String parentPath) {
    String fullName = parentPath + "/" + element.getName();
    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      List<SmmElement> children = new ArrayList<>();
      for (Element child : groupElement.getGroup().getElements()) {
        if (child instanceof GroupElement || child instanceof FieldElement) {
          children.add(of(child, fullName));
        }
      }
      return new SmmElement(element.getName(), fullName, true, groupElement.getGroup().getRepeatability(), "", children);
    }
    String dataType = "";
    if (element instanceof FieldElement fieldElement && fieldElement.getField() != null && fieldElement.getField().getFieldType() != null) {
      String type = fieldElement.getField().getFieldType().getType();
      dataType = type != null && type.endsWith("Type") ? type.substring(0, type.length() - "Type".length()) : String.valueOf(type);
    }
    return new SmmElement(element.getName(), fullName, element instanceof GroupElement, null, dataType, List.of());
  }

  /** The element with the given full name in {@code roots}, or {@code null}. */
  public static @Nullable SmmElement find(List<SmmElement> roots, String fullName) {
    for (SmmElement root : roots) {
      SmmElement found = root.find(fullName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private @Nullable SmmElement find(String path) {
    if (fullName.equals(path)) {
      return this;
    }
    if (!path.startsWith(fullName + "/")) {
      return null;
    }
    for (SmmElement child : children) {
      SmmElement found = child.find(path);
      if (found != null) {
        return found;
      }
    }
    return null;
  }
}
