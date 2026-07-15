package de.a12.studio.dataservices.services.documentmodel.features.validation;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.model.notification.Severity;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;
import de.a12.studio.dataservices.services.support.InMemoryDocumentModelReferenceResolver;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DMValidationService {

  public List<DocumentModelErrors> validate(DocumentModel model, List<DocumentModel> otherModels) {
    List<DocumentModelErrors> elementErrorsThatKernelDoesNotFind = checkMissingErrors(model, otherModels);
    try {
      List<DocumentModelErrors> kernelElementErrors =
          DocumentModelSupport.getElementProblems(model).stream()
              .map(p -> new DocumentModelErrors(((Element) p.getSource()).getId(), p.getMessage(), p.getSeverity()))
              .toList();
      List<DocumentModelErrors> combined = new ArrayList<>(elementErrorsThatKernelDoesNotFind);
      combined.addAll(kernelElementErrors);
      return combined;
    } catch (Exception e) {
      return elementErrorsThatKernelDoesNotFind;
    }
  }

  /**
   * Checks for Errors that the Kernel validation does not find.
   * We use simple generic Error Messages instead of the Document Model ValidationRule Error Messages so that changes in
   * the Validation Error Messages do not need to be integrated into this code.
   */
  private static List<DocumentModelErrors> checkMissingErrors(DocumentModel model, List<DocumentModel> otherModels) {
    DocumentModelService documentModelService = new DocumentModelService();
    List<DocumentModelErrors> result = new ArrayList<>();
    List<Element> unfixableElements = new ArrayList<>();
    DocumentModelReferenceResolver resolver = new InMemoryDocumentModelReferenceResolver(otherModels);
    DocumentModelVisitor visitor =
        new DocumentModelVisitor() {
          @Override
          public VisitProcess visitGroup(Group group) {
            String groupPath = documentModelService.getPath(group);
            if (hasMissingIncludeReference(group, resolver)) {
              result.add(
                  new DocumentModelErrors(group.getId(), "Include with path '" + groupPath + "': Missing Include Reference", Severity.ERROR));
              unfixableElements.add(group);
            }
            if (hasMissingIndexField(group)) {
              String elementType = DocumentModelSupport.isInclude(group) ? "Include" : "Group";
              result.add(
                  new DocumentModelErrors(group.getId(), elementType + " with path '" + groupPath + "': Missing Index Field", Severity.ERROR));
            }
            Set<Element> duplicatedElements = getElementsWithDuplicatedNames(group);
            for (Element element : duplicatedElements) {
              String elementPath = documentModelService.getPath(group);
              result.add(
                  new DocumentModelErrors(
                      element.getId(), "Element with path '" + elementPath + "': Multiple Elements with same path", Severity.ERROR));
            }
            return VisitProcess.CONTINUE_TRAVERSAL;
          }

          @Override
          public VisitProcess visitComputation(Computation computation) {
            if (hasMissingComputedField(computation)) {
              String path = documentModelService.getPath(computation);
              result.add(
                  new DocumentModelErrors(computation.getId(), "Computation with path '" + path + "': Missing Computed Field", Severity.ERROR));
              unfixableElements.add(computation);
            }
            return VisitProcess.CONTINUE_TRAVERSAL;
          }

          @Override
          public VisitProcess visitField(Field field) {
            String path = documentModelService.getPath(field);
            if (hasTooFewEnumValues(field)) {
              result.add(
                  new DocumentModelErrors(
                      field.getId(), "Field with path '" + path + "': Enumeration must have at least two values", Severity.ERROR));
            }
            if (hasMissingTypeDef(field)) {
              result.add(new DocumentModelErrors(field.getId(), "Field with path '" + path + "': Missing Type Definition", Severity.ERROR));
              unfixableElements.add(field);
            }
            return VisitProcess.CONTINUE_TRAVERSAL;
          }
        };

    new DocumentModelWalker().acceptDocumentModel(model, visitor);
    for (Element element : unfixableElements) {
      element.getParent().removeElement(element);
    }
    return result;
  }

  private static boolean hasMissingIncludeReference(Group group, DocumentModelReferenceResolver resolver) {
    return DocumentModelSupport.isInclude(group)
        && resolver.getDocumentModel(group.getIncludeDetails().get().getModelReference().getReference()) == null;
  }

  private static Set<Element> getElementsWithDuplicatedNames(Group group) {
    Set<Element> result = new LinkedHashSet<>();
    var nameMap = group.getElements().stream().collect(Collectors.groupingBy(Element::getName));
    nameMap.values().stream().filter(v -> v.size() > 1).forEach(result::addAll);
    return result;
  }

  private static boolean hasMissingIndexField(Group group) {
    boolean indexFieldUndefined = group.getIndexField().isPresent() && group.getIndexField().get().getDocumentModelObject().isEmpty();
    if (indexFieldUndefined) {
      group.setIndexField(null);
    }
    return indexFieldUndefined;
  }

  private static boolean hasMissingComputedField(Computation computation) {
    return computation.getComputedField().getDocumentModelObject().isEmpty();
  }

  private static boolean hasMissingTypeDef(Field field) {
    return field.getFieldType() instanceof TypeDefType typeDefType && typeDefType.getTypeDefinition().isEmpty();
  }

  private static boolean hasTooFewEnumValues(Field field) {
    if ("multi-select".equals(field.getParent().getUsageType().orElse(null))) {
      var enumValues = getEnumValues(field);
      return enumValues != null && enumValues.size() < 2;
    }
    return false;
  }

  private static List<EnumerationType.EnumValue> getEnumValues(Field field) {
    if (field.getFieldType() instanceof EnumerationType enumerationType) {
      return enumerationType.getValues();
    } else if (field.getFieldType() instanceof TypeDefType typeDefType
        && typeDefType.getTypeDefinition().isPresent()
        && typeDefType.getTypeDefinition().get().getFieldType() instanceof EnumerationType referencedEnumType) {
      return referencedEnumType.getValues();
    } else {
      return null;
    }
  }
}
