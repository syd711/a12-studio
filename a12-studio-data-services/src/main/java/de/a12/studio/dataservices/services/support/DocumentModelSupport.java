package de.a12.studio.dataservices.services.support;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.AdditionOrigins;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.services.DocumentModelExpansionException;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.model.notification.RankedNotification;
import de.a12.studio.dataservices.services.support.additivemodel.AdditiveModelSupport;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipInputStream;

public final class DocumentModelSupport {

  private static final String REFERENCE_DOCUMENT = "reference-document";
  private static final String TYPE_DEFINITIONS = "typeDefinitions";
  private static final String INCLUDE = "include";
  private static final String TD_ONLY = "tdonly";

  private DocumentModelSupport() {
  }

  public static DocumentModel deserialize(String documentModelContent) {
    try {
      return new DocumentModelSerializer().deserialize(new StringReader(documentModelContent));
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static DocumentModel deserialize(JsonNode documentModel) {
    try {
      return new DocumentModelSerializer().deserialize(documentModel);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String serialize(DocumentModel documentModel) {
    return serialize(documentModel, new ProblemReporter());
  }

  public static String serialize(DocumentModel documentModel, IProblemReporter problemReporter) {
    StringWriter result = new StringWriter();
    try {
      new DocumentModelSerializer().serialize(documentModel, result, problemReporter);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
    return result.toString();
  }

  public static DocumentModel expand(String documentModelId, List<DocumentModel> documentModels) {
    return expand(documentModelId, documentModels, new ProblemReporter(), new AdditionOrigins(), null);
  }

  public static DocumentModel expand(
      String documentModelId,
      List<DocumentModel> documentModels,
      IProblemReporter problemReporter,
      AdditionOrigins additionOrigins,
      JsonNode contextData) {
    DocumentModelReferenceResolver resolver =
        new de.a12.studio.dataservices.services.support.InMemoryDocumentModelReferenceResolver(
            removeMetaData(documentModels));
    DocumentModel documentModel =
        documentModels.stream().filter(dm -> dm.getHeader().getId().equals(documentModelId)).findFirst().orElse(null);
    if (documentModel == null) {
      throw new IllegalStateException("Cannot expand Document Model with id '" + documentModelId + "': Model not found");
    }

    if (isTypeDefinitionModel(documentModel)) {
      expandTDOnlyModel(documentModel, resolver, problemReporter);
    } else if (isAdditiveDocumentModel(documentModel)) {
      return expandAdditiveDocumentModel(documentModelId, documentModels, problemReporter, additionOrigins, contextData);
    } else if (hasTypeDefinitionImport(documentModel)) {
      expandDMWithImportedTypeDefinitions(documentModel, resolver, problemReporter);
    } else if (hasInclude(documentModel)) {
      new DocumentModelService().expand(documentModel, resolver, problemReporter);
    }

    return documentModel;
  }

  private static boolean hasAnnotation(DocumentModel documentModel, String name) {
    return documentModel.getHeader().getAnnotations() != null
        && documentModel.getHeader().getAnnotations().stream().anyMatch(a -> a.getName().equals(name));
  }

  private static boolean hasModelReference(DocumentModel documentModel, String purpose) {
    return documentModel.getHeader().getModelReferences() != null
        && documentModel.getHeader().getModelReferences().stream().anyMatch(r -> r.getPurpose().equals(purpose));
  }

  private static boolean isTypeDefinitionModel(DocumentModel documentModel) {
    return hasAnnotation(documentModel, TD_ONLY);
  }

  private static boolean isAdditiveDocumentModel(DocumentModel documentModel) {
    return hasAnnotation(documentModel, "additive-document") && hasModelReference(documentModel, REFERENCE_DOCUMENT);
  }

  public static boolean isComposedDocumentModel(DocumentModel documentModel) {
    return hasAnnotation(documentModel, "cdm.queryRoot");
  }

  private static boolean hasTypeDefinitionImport(DocumentModel documentModel) {
    return hasModelReference(documentModel, TYPE_DEFINITIONS);
  }

  private static boolean hasInclude(DocumentModel documentModel) {
    return hasModelReference(documentModel, INCLUDE);
  }

  public static DocumentModel expandAdditiveDocumentModel(
      String documentModelId,
      List<DocumentModel> documentModels,
      IProblemReporter problemReporter,
      AdditionOrigins additionOrigins,
      JsonNode contextData) {
    DocumentModel documentModel =
        documentModels.stream().filter(dm -> dm.getHeader().getId().equals(documentModelId)).findFirst().orElse(null);
    if (documentModel == null) {
      throw new IllegalStateException("Document model not found: " + documentModelId);
    }
    if (contextData == null) {
      throw new IllegalStateException("Context data is required to expand an additive document model");
    }
    AdditiveModelSupport.AdditionExpansionContextData expansionContext;
    try {
      expansionContext = new ObjectMapper().treeToValue(contextData, AdditiveModelSupport.AdditionExpansionContextData.class);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    String referenceModelId =
        documentModel.getHeader().getModelReferences().stream()
            .filter(r -> r.getPurpose().equals(REFERENCE_DOCUMENT))
            .findFirst()
            .orElseThrow()
            .getReference();
    DocumentModelReferenceResolver resolver =
        new de.a12.studio.dataservices.services.support.InMemoryDocumentModelReferenceResolver(
            removeMetaData(documentModels));
    // reference model is already in expanded state
    DocumentModel referenceModel = resolver.getDocumentModel(referenceModelId);
    if (referenceModel == null) {
      throw new IllegalStateException("Reference model not found: " + referenceModelId);
    }

    documentModel.getHeader().getModelReferences().removeIf(r -> r.getPurpose().equals(REFERENCE_DOCUMENT));
    DocumentModel expandedAdditiveModel = expand(documentModel.getHeader().getId(), documentModels, problemReporter, new AdditionOrigins(), null);

    Optional<DocumentModel> joinedModel =
        AdditiveModelSupport.doJoinWithAdm(
            referenceModel,
            expandedAdditiveModel,
            java.util.Locale.US,
            new ProblemReporter(),
            problemReporter,
            additionOrigins);

    DocumentModel dm = joinedModel.get();

    Map<String, String> elementOrigins = AdditiveModelSupport.elementOriginsAsMap(additionOrigins);
    AdditiveModelSupport.restoreIds(elementOrigins, additionOrigins, expansionContext, dm);

    return dm;
  }

  // Workaround until A12K-1997 is fixed. Temporarily remove local typedefs when expanding td only models.
  public static void expandTDOnlyModel(
      DocumentModel documentModel, DocumentModelReferenceResolver resolver, IProblemReporter problemReporter) {
    List<FieldTypeDefinition> typeDefinitions = new ArrayList<>(documentModel.getContent().getTypeDefinitions());
    documentModel.getContent().getTypeDefinitions().clear();

    new DocumentModelService().expand(documentModel, resolver, problemReporter);

    typeDefinitions.addAll(documentModel.getContent().getTypeDefinitions());
    documentModel.getContent().setTypeDefinitions(typeDefinitions);
  }

  // Workaround until A12K-1997 is fixed. DocumentModelService.collapse should be used as preparation for the expand step.
  public static void expandDMWithImportedTypeDefinitions(
      DocumentModel documentModel, DocumentModelReferenceResolver resolver, IProblemReporter problemReporter) {
    documentModel.getContent().getTypeDefinitions().clear();
    new DocumentModelService().expand(documentModel, resolver, problemReporter);
  }

  public static List<FieldTypeDefinition> extractImportedTypeDefinitions(
      String id, String modelReference, List<DocumentModel> documentModels) {
    DocumentModelService dmService = new DocumentModelService();
    de.a12.studio.dataservices.services.support.InMemoryDocumentModelReferenceResolver resolver =
        new de.a12.studio.dataservices.services.support.InMemoryDocumentModelReferenceResolver(documentModels);
    DocumentModel model = resolver.getDocumentModel(id);
    if (model == null) {
      throw new IllegalStateException("Cannot extract type definitions for Document Model with id '" + id + "': Model not found");
    }

    var locales = model.getHeader().getLocales();

    var ref =
        model.getHeader().getModelReferences().stream()
            .filter(r -> r.getPurpose().equals(TYPE_DEFINITIONS))
            .filter(r -> r.getAlias().equals(modelReference))
            .findFirst()
            .orElseThrow(() -> new DocumentModelExpansionException("Model reference does not exist in given model."));

    List<FieldTypeDefinition> extracted = dmService.extractImportedTypeDefinitions(ref, resolver, locales);
    List<FieldTypeDefinition> distinct = new ArrayList<>();
    var seenIds = new java.util.HashSet<String>();
    for (FieldTypeDefinition td : extracted) {
      if (seenIds.add(td.getId())) {
        distinct.add(td);
      }
    }
    return distinct;
  }

  public static Map<String, TypeDefinitionInfo> importedTypeDefinitions(List<FieldTypeDefinition> typeDefinitions) {
    List<FieldTypeDefinition> tdsWithPurpose =
        typeDefinitions.stream()
            .filter(td -> !td.getAllModelReferencePaths().isEmpty()
                && td.getAllModelReferencePaths().stream().allMatch(refList ->
                    !refList.isEmpty() && refList.stream().allMatch(ref -> ref.getPurpose().equals(TYPE_DEFINITIONS))))
            .toList();

    Map<String, TypeDefinitionInfo> result = new HashMap<>();
    for (FieldTypeDefinition td : tdsWithPurpose) {
      var firstPath = td.getAllModelReferencePaths().stream().findFirst().orElseThrow();
      result.put(td.getId(), new TypeDefinitionInfo(firstPath.stream().map(r -> r.getAlias()).toList()));
    }
    return result;
  }

  public static Map<String, TypeDefinitionInfo> includedImportedTypeDefinitions(List<FieldTypeDefinition> typeDefinitions) {
    List<FieldTypeDefinition> tdsWithPurpose =
        typeDefinitions.stream()
            .filter(td -> !td.getAllModelReferencePaths().isEmpty()
                && td.getAllModelReferencePaths().stream().anyMatch(refList ->
                    refList.get(0).getPurpose().equals(INCLUDE) && refList.get(refList.size() - 1).getPurpose().equals(TYPE_DEFINITIONS)))
            .toList();

    Map<String, TypeDefinitionInfo> result = new HashMap<>();
    for (FieldTypeDefinition td : tdsWithPurpose) {
      var includedReferencePath =
          td.getAllModelReferencePaths().stream().filter(refList -> refList.get(0).getPurpose().equals(INCLUDE)).findFirst().orElseThrow();
      result.put(td.getId(), new TypeDefinitionInfo(includedReferencePath.stream().map(r -> r.getAlias()).toList()));
    }
    return result;
  }

  public static Map<String, TypeDefinitionInfo> includedTypeDefinitions(List<FieldTypeDefinition> typeDefinitions) {
    List<FieldTypeDefinition> tdsWithPurpose =
        typeDefinitions.stream()
            .filter(td -> !td.getAllModelReferencePaths().isEmpty()
                && td.getAllModelReferencePaths().stream().allMatch(refList -> refList.stream().allMatch(r -> r.getPurpose().equals(INCLUDE))))
            .toList();

    Map<String, TypeDefinitionInfo> result = new HashMap<>();
    for (FieldTypeDefinition td : tdsWithPurpose) {
      var firstPath = td.getAllModelReferencePaths().stream().findFirst().orElseThrow();
      result.put(td.getId(), new TypeDefinitionInfo(firstPath.stream().map(r -> r.getAlias()).toList()));
    }
    return result;
  }

  public static String generateValidationCode(DocumentModel expandedDM) {
    var externalDocument = new DocumentModelService().convertToExternal(expandedDM);

    List<RankedNotification> list = new ArrayList<>();
    var dmService = new DocumentModelServiceFactory().createDocumentModelService();
    byte[] byteCode = dmService.generateValidationCode(externalDocument, new JSCodeGeneratorConfig(), null, list::add);

    if (!list.isEmpty()) {
      StringBuilder message = new StringBuilder();
      for (int i = 0; i < list.size(); i++) {
        if (i > 0) {
          message.append(", ");
        }
        message.append(list.get(i));
      }
      throw new RuntimeException(message.toString());
    }
    return unzipToString(byteCode);
  }

  public static String unzipToString(byte[] zipContent) {
    StringBuilder sb = new StringBuilder();
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipContent))) {
      while (zis.getNextEntry() != null) {
        sb.append(IOUtils.toString(zis, StandardCharsets.UTF_8));
      }
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
    return sb.toString();
  }

  private static final class JSCodeGeneratorConfig implements IValidationCodeGeneratorConfig {
    @Override
    public ProgrammingLanguage getProgrammingLanguage() {
      return ProgrammingLanguage.JAVASCRIPT;
    }

    @Override
    public Optional<String> getVariant() {
      return Optional.empty();
    }

    @Override
    public Optional<String> getPackageName() {
      return Optional.empty();
    }
  }

  public static Rule getRuleById(DocumentModel documentModel, String ruleId) {
    Element element = getElementById(documentModel, ruleId);
    if (!(element instanceof Rule rule)) {
      throw new IllegalStateException("No rule with id " + ruleId + " found");
    }
    return rule;
  }

  public static Field getFieldById(DocumentModel documentModel, String fieldId) {
    Element element = getElementById(documentModel, fieldId);
    if (!(element instanceof Field field)) {
      throw new IllegalStateException("No field with id " + fieldId + " found");
    }
    return field;
  }

  public static Computation getComputationById(DocumentModel documentModel, String computationId) {
    Element element = getElementById(documentModel, computationId);
    if (!(element instanceof Computation computation)) {
      throw new IllegalStateException("No computation with id " + computationId + " found");
    }
    return computation;
  }

  public static Element getElementById(DocumentModel documentModel, String elementId) {
    DocumentModelSearchService searchService = new DocumentModelSearchService(documentModel);
    Optional<Element> element = searchService.getById(elementId);
    return element.orElse(null);
  }

  public static List<IProblem> getElementProblems(DocumentModel documentModel) {
    List<IProblem> problems = new ArrayList<>();
    new DocumentModelService().checkConsistency(documentModel, problems::add);

    return problems.stream()
        // cycle problems are handled by rule contradictions
        .filter(p -> !p.getMessage().contains("MVK_ERROR_CALC_CYCLE"))
        .filter(p -> !(p.getMessage().startsWith("Missing rules [") && p.getMessage().contains("for attachment with group")))
        .filter(p -> p.getSource() instanceof Element)
        .toList();
  }

  public static boolean isInclude(Group group) {
    return group.getIncludeDetails().isPresent();
  }

  public static List<Rule> getAllRules(DocumentModel documentModel) {
    List<Rule> rules = new ArrayList<>();
    DocumentModelVisitor visitor =
        new DocumentModelVisitor() {
          @Override
          public VisitProcess visitRule(Rule rule) {
            if (rule != null) {
              rules.add(rule);
            }
            return VisitProcess.CONTINUE_TRAVERSAL;
          }
        };
    new DocumentModelWalker().acceptDocumentModel(documentModel, visitor);
    return rules;
  }

  public static List<Computation> getAllComputations(DocumentModel documentModel) {
    List<Computation> computations = new ArrayList<>();
    DocumentModelVisitor visitor =
        new DocumentModelVisitor() {
          @Override
          public VisitProcess visitComputation(Computation computation) {
            if (computation != null) {
              computations.add(computation);
            }
            return VisitProcess.CONTINUE_TRAVERSAL;
          }
        };
    new DocumentModelWalker().acceptDocumentModel(documentModel, visitor);
    return computations;
  }

  public static List<JsonNode> serializeElements(List<Element> elements, DocumentModel documentModel) {
    StringWriter writer = new StringWriter();
    ProblemReporter pr = new ProblemReporter();
    try {
      new DocumentModelSerializer().serializeElements(documentModel, elements, writer, pr);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
    JsonNode elementsJson = toJson(writer.toString());
    if (elementsJson == null) {
      throw new IllegalStateException("Failed to serialize elements");
    }

    List<JsonNode> result = new ArrayList<>();
    elementsJson.forEach(result::add);
    return result;
  }

  public static List<JsonNode> serializeTypeDefs(List<FieldTypeDefinition> typeDefinitions, DocumentModel documentModel) {
    StringWriter writer = new StringWriter();
    ProblemReporter pr = new ProblemReporter();
    try {
      new DocumentModelSerializer().serializeFieldTypeDefinitions(documentModel, typeDefinitions, writer, pr);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
    JsonNode typeDefsJson = toJson(writer.toString());
    if (typeDefsJson == null) {
      throw new IllegalStateException("Failed to serialize type definitions");
    }

    List<JsonNode> result = new ArrayList<>();
    typeDefsJson.forEach(result::add);
    return result;
  }

  public static JsonNode toJson(String string) {
    if (string == null) {
      return null;
    }
    try {
      return new ObjectMapper().readTree(string);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonNode stringifyRankedNotification(RankedNotification rn) {
    return new ObjectMapper().createObjectNode().put("severity", rn.getSeverity().name()).put("message", rn.getMessage());
  }

  public static JsonNode convertRankedNotifications(List<RankedNotification> notifications) {
    var result = new ObjectMapper().createObjectNode();
    for (RankedNotification notification : notifications) {
      if (notification.getSource() == null) {
        // TODO: This was found for "Content is empty" (i.e. no mappings were specified). It should be reported to the
        // user that empty smm's are not allowed
        continue;
      }
      String source = convertDocumentPointerToEntityInstancePath(notification.getSource().toString());
      var entry = new ObjectMapper().createArrayNode();
      if (result.has(source)) {
        entry.addAll((ArrayNode) result.get(source));
      }
      entry.add(stringifyRankedNotification(notification));
      result.set(source, entry);
    }
    return result;
  }

  public static String convertDocumentPointerToEntityInstancePath(String documentPointer) {
    var parts = com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer.of(documentPointer).getPathParts();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.size(); i++) {
      if (i > 0) {
        sb.append("/");
      }
      sb.append(parts.get(i).name()).append("[").append(parts.get(i).repetitionIndex()).append("]");
    }
    return sb.toString();
  }

  // --- metadata helpers (formerly DocumentModelMetaDataHelper.kt) ---

  public static DocumentModel enrichWithMetaData(DocumentModel documentModel) {
    DocumentModel metaDataModel = readMetaDataDm();
    if (documentModel.getHeader().getId().endsWith("____generated") || isComposedDocumentModel(documentModel)) {
      return addMetaDataWithDataServicesInjector(documentModel, metaDataModel);
    } else {
      // TODO: we should use Data Services Injector, but at the moment it is too slow. https://jira.mgm-tp.com/jira/browse/A12S-6668
      String metaDataIdPrefix = calculateHash(metaDataModel.getHeader().getId());
      prefixElementIds(metaDataIdPrefix, metaDataModel);
      addMetaDataElements(documentModel.getContent().getModelRoot(), metaDataModel.getContent().getModelRoot().getElements());
      return documentModel;
    }
  }

  public static DocumentModel addMetaDataWithDataServicesInjector(DocumentModel documentModel, DocumentModel metaDataModel) {
    DocumentModelService dmService = new DocumentModelService();
    var externalModel = dmService.convertToExternal(documentModel);
    var externalMetaDataModel = dmService.convertToExternal(metaDataModel);

    var injector =
        new com.mgmtp.a12.dataservices.model.metadata.DocumentModelMetadataInjectorFactory(
                new com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService(),
                new com.mgmtp.a12.kernel.md.document.internal.service.DocumentFactoryImpl(),
                dmService)
            .getInstance(externalModel, java.util.Locale.US);

    var externalEnrichedModel = injector.getDocumentModelWithMetadata(externalMetaDataModel, null);
    DocumentModel enrichedModel = dmService.convertFromExternal(externalEnrichedModel);
    enrichedModel.getContent().getModelInfo().setJoinedModelsInfo(null);
    return enrichedModel;
  }

  // Due to performance issues we are not using Data Services MetadataInjector
  public static void addMetaDataElements(Group group, List<Element> metaDataElements) {
    for (Element metaDataElement : metaDataElements) {
      // MetaData can be customized by Projects, thus we only add the elements that don't already exist
      boolean elementExistsByName = group.getElements().stream().anyMatch(e -> e.getName().equals(metaDataElement.getName()));
      if (!elementExistsByName) {
        group.addElement(metaDataElement);
      }
      if (metaDataElement instanceof Group metaDataGroup) {
        Group groupInModel =
            (Group) group.getElements().stream().filter(e -> e.getName().equals(metaDataElement.getName())).findFirst().orElse(null);
        if (groupInModel == null) {
          throw new IllegalStateException("Group not found for meta data element " + metaDataElement.getName());
        }
        addMetaDataElements(groupInModel, metaDataGroup.getElements());
      }
    }
  }

  public static List<DocumentModel> removeMetaData(List<DocumentModel> documentModels) {
    DocumentModel metaDataModel = readMetaDataDm();
    String metaDataIdPrefix = calculateHash(metaDataModel.getHeader().getId());
    prefixElementIds(metaDataIdPrefix, metaDataModel);
    for (DocumentModel dm : documentModels) {
      removeMetaData(dm.getContent().getModelRoot(), metaDataModel.getContent().getModelRoot().getElements());
    }
    return documentModels;
  }

  public static void removeMetaData(Group group, List<Element> metaDataElements) {
    for (Element metaDataElement : metaDataElements) {
      Element elementById =
          group.getElements().stream().filter(e -> e.getId().equals(metaDataElement.getId())).findFirst().orElse(null);
      if (elementById != null) {
        group.removeElement(elementById);
      } else {
        Element elementByName =
            group.getElements().stream().filter(e -> e.getName().equals(metaDataElement.getName())).findFirst().orElse(null);
        if (elementByName instanceof Group elementByNameGroup && metaDataElement instanceof Group metaDataGroup) {
          removeMetaData(elementByNameGroup, metaDataGroup.getElements());
        }
      }
    }
  }

  private static DocumentModel readMetaDataDm() {
    try (var metaDataDmReader =
        new java.io.BufferedReader(
            new java.io.InputStreamReader(
                DocumentModelSupport.class.getResourceAsStream("/com/mgmtp/a12/platform/model/document-meta-data.json"),
                StandardCharsets.UTF_8))) {
      return new DocumentModelSerializer().deserialize(metaDataDmReader);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void prefixElementIds(String idPrefix, DocumentModel documentModel) {
    DocumentModelVisitor visitor =
        new DocumentModelVisitor() {
          @Override
          public VisitProcess visitElement(Element element) {
            element.setId(idPrefix + "_" + element.getId());
            return super.visitElement(element);
          }
        };
    new DocumentModelWalker().acceptDocumentModel(documentModel, visitor);
  }

  // Copy pasted from Kernel https://bitbucket.mgm-tp.com/projects/A12/repos/kernel_internal_os_staging/browse/kernel-md/kernel-md-join/src/main/java/com/mgmtp/a12/kernel/md/model/internal/services/join/util/IdUtil.java#25
  private static String calculateHash(String string) {
    try {
      java.security.MessageDigest messageDigest = java.security.MessageDigest.getInstance("MD5");
      messageDigest.reset();
      messageDigest.update(string.getBytes());
      byte[] digest = messageDigest.digest();
      java.math.BigInteger bigInt = new java.math.BigInteger(1, digest);
      return bigInt.toString(16);
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
