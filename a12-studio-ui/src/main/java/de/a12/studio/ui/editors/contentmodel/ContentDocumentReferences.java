package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.content.ContentDataContext;
import de.a12.studio.modelsvalidation.validators.content.ContentFormElementTypes;
import de.a12.studio.modelsvalidation.validators.content.DocumentStructure;
import de.a12.studio.ui.util.ProjectDocumentModels;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link ContentReferences} over the Document Model the edited Content Model is bound to. The Document Model with its
 * Includes expanded is built on first use and kept until {@link #invalidate()}, which the editor calls whenever something
 * the answer depends on may have changed (the binding, the base group, another Document Model that was saved).
 */
final class ContentDocumentReferences implements ContentReferences {

  /** The types of field a date picker can show, see {@code ContentFormElementTypes}. */
  private static final Set<String> DATE_TYPES = Set.of("DateType", "DateTimeType", "DateRangeType");

  private final ContentModel model;
  private final ProjectItem projectItem;
  private DocumentStructure structure;
  private boolean built;

  ContentDocumentReferences(@NonNull ContentModel model, @NonNull ProjectItem projectItem) {
    this.model = model;
    this.projectItem = projectItem;
  }

  void invalidate() {
    built = false;
    structure = null;
  }

  private @Nullable DocumentStructure structure() {
    if (!built) {
      built = true;
      String id = model.getDocumentModelId();
      if (id != null) {
        List<DocumentModel> documentModels = ProjectDocumentModels.getOtherDocumentModelsWithCombinations(projectItem);
        DocumentModel documentModel = documentModels.stream().filter(candidate -> id.equals(candidate.getId())).findFirst().orElse(null);
        structure = documentModel != null ? new DocumentStructure(documentModel, documentModels) : null;
      }
    }
    return structure;
  }

  @Override
  public boolean isBound() {
    return structure() != null;
  }

  private DocumentStructure.@Nullable Node contextOf(ContentElement element) {
    DocumentStructure structure = structure();
    return structure == null ? null : ContentDataContext.of(model, structure, element);
  }

  @Override
  public List<Choice> groups(@NonNull ContentElement element, boolean repeatedOnly) {
    DocumentStructure structure = structure();
    if (structure == null) {
      return List.of();
    }
    return structure.candidateGroups(contextOf(element)).stream()
        .filter(node -> !repeatedOnly || node.isRepeated())
        .map(ContentDocumentReferences::choice)
        .toList();
  }

  @Override
  public List<Choice> attachmentGroups(@NonNull ContentElement element) {
    DocumentStructure structure = structure();
    if (structure == null) {
      return List.of();
    }
    return structure.candidateGroups(contextOf(element)).stream()
        .filter(node -> GroupConfig.USAGE_TYPE_ATTACHMENT.equals(node.usageType()))
        .map(ContentDocumentReferences::choice)
        .toList();
  }

  @Override
  public List<Choice> indexGroups(@NonNull ContentElement element) {
    DocumentStructure structure = structure();
    return structure == null ? List.of()
        : structure.candidateIndexGroups(contextOf(element)).stream().map(ContentDocumentReferences::choice).toList();
  }

  @Override
  public List<Choice> listableFields(@NonNull ContentElement element) {
    DocumentStructure structure = structure();
    return structure == null ? List.of()
        : structure.candidateFieldsThroughRepeatedGroups(contextOf(element)).stream().map(ContentDocumentReferences::choice).toList();
  }

  @Override
  public List<Choice> listableGroups(@NonNull ContentElement element) {
    // Groups are listed the same way whether they repeat or not: everything below the data context.
    return groups(element, false);
  }

  @Override
  public Collected autoCollected(@NonNull ContentElement container) {
    DocumentStructure structure = structure();
    Map<String, Choice> fields = new LinkedHashMap<>();
    Map<String, Choice> groups = new LinkedHashMap<>();
    if (structure != null && container.getChildren() != null) {
      collect(container.getChildren(), structure, fields, groups);
    }
    return new Collected(List.copyOf(fields.values()), List.copyOf(groups.values()));
  }

  private static void collect(List<ContentElement> elements, DocumentStructure structure, Map<String, Choice> fields, Map<String, Choice> groups) {
    for (ContentElement element : elements) {
      if ("MessageGroupContainer".equals(element.getType()) || "MessageGroupDisplay".equals(element.getType())) {
        continue;
      }
      boolean engine = ContentElementLibrary.FORM_ELEMENTS_NAMESPACE.equals(element.getNamespace());
      if (engine && element.getProps() != null && element.getProps().get("elementId") instanceof String id) {
        DocumentStructure.Node node = structure.find(id);
        if (node != null && node.isGroup()) {
          groups.putIfAbsent(node.path(), choice(node));
        }
        else if (node != null && node.isField()) {
          fields.putIfAbsent(node.path(), choice(node));
        }
      }
      if (element.getChildren() != null) {
        collect(element.getChildren(), structure, fields, groups);
      }
    }
  }

  @Override
  public boolean isDateField(@NonNull String id) {
    DocumentStructure structure = structure();
    DocumentStructure.Node node = structure == null ? null : structure.find(id);
    FieldType type = node != null && node.isField() ? node.fieldType() : null;
    return type != null && DATE_TYPES.contains(type.getType());
  }

  @Override
  public List<Choice> fields(@NonNull ContentElement element) {
    DocumentStructure structure = structure();
    return structure == null ? List.of() : structure.candidateFields(contextOf(element)).stream().map(ContentDocumentReferences::choice).toList();
  }

  @Override
  public List<Choice> formElements(@NonNull ContentElement element) {
    DocumentStructure structure = structure();
    if (structure == null || !ContentFormElementTypes.isInputElement(element.getType())) {
      return List.of();
    }
    return ContentFormElementTypes.candidates(element.getType(), structure, contextOf(element)).stream()
        .map(ContentDocumentReferences::choice).toList();
  }

  @Override
  public @Nullable FieldInfo fieldInfo(@NonNull String id) {
    DocumentStructure structure = structure();
    DocumentStructure.Node node = structure == null ? null : structure.find(id);
    if (node == null || !node.isField()) {
      return null;
    }
    FieldType type = node.fieldType();
    String name = type != null ? type.getType() : null;
    if (name == null) {
      return new FieldInfo(ValueKind.OTHER, List.of());
    }
    return switch (name) {
      case "BooleanType" -> new FieldInfo(ValueKind.BOOLEAN, List.of());
      case "ConfirmType" -> new FieldInfo(ValueKind.CONFIRM, List.of());
      case "NumberType" -> new FieldInfo(ValueKind.NUMBER, List.of());
      case "StringType" -> new FieldInfo(ValueKind.TEXT, List.of());
      case "EnumerationType" -> new FieldInfo(ValueKind.ENUMERATION, enumerationValues(type));
      default -> new FieldInfo(ValueKind.OTHER, List.of());
    };
  }

  private static List<String> enumerationValues(FieldType type) {
    if (type instanceof EnumerationFieldType enumeration && enumeration.getEnumerationType() != null
        && enumeration.getEnumerationType().getValues() != null) {
      return enumeration.getEnumerationType().getValues().stream().map(EnumerationValue::getValue).toList();
    }
    return List.of();
  }

  @Override
  public String labelOf(@NonNull String id) {
    DocumentStructure structure = structure();
    DocumentStructure.Node node = structure == null ? null : structure.find(id);
    return node != null ? node.path() : id;
  }

  private static Choice choice(DocumentStructure.Node node) {
    return new Choice(node.id, node.path());
  }
}
