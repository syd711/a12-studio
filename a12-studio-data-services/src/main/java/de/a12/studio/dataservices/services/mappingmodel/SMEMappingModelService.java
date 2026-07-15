package de.a12.studio.dataservices.services.mappingmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.MappingModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.Entity;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.SMMGeneratorConfig;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.util.SMMNotificationSource;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;
import de.a12.studio.dataservices.services.support.InMemoryIDocumentModelResolver;
import de.a12.studio.dataservices.services.support.TypeDefinitionInfo;
import de.a12.studio.dataservices.services.structuralmappingmodel.StructuralMappingModelWithContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SMEMappingModelService {

  public JsonNode validate(MM_MappingModel_2 mappingModel, List<DocumentModel> documentModels, MM_StructuralMappingModel_1 structuralMappingModel) {
    var dmResolver = new InMemoryIDocumentModelResolver(documentModels);
    List<RankedNotification> problems = new ArrayList<>();
    MappingModelService.create(dmResolver)
        .checkMappingModelConsistency(mappingModel, unusedId -> structuralMappingModel, problems::add);

    return convertRankedNotifications(problems);
  }

  public SourceAndTargetModel computeSourceAndTarget(MM_MappingModel_2 mappingModel, List<DocumentModel> documentModels) {
    var resolver = new InMemoryIDocumentModelResolver(documentModels);
    List<RankedNotification> problems = new ArrayList<>();
    var result = MappingModelService.create(resolver).createSourceAndTargetDMForSMM(mappingModel, problems::add, Locale.US);

    JsonNode sourceDm = DocumentModelSupport.toJson(DocumentModelSupport.serialize(result.sourceDM()));
    JsonNode targetDm = DocumentModelSupport.toJson(DocumentModelSupport.serialize(result.targetDM()));
    if (sourceDm == null || targetDm == null) {
      throw new IllegalStateException("Failed to serialize source/target document model");
    }
    return new SourceAndTargetModel(sourceDm, targetDm);
  }

  public BasePrecomputationModel computeBase(MM_MappingModel_2 mappingModel, List<DocumentModel> documentModels) {
    var resolver = new InMemoryIDocumentModelResolver(documentModels);
    List<RankedNotification> problems = new ArrayList<>();
    DocumentModel result = MappingModelService.create(resolver).createBaseDmForPrecomputationFragment(mappingModel, problems::add, Locale.US);

    JsonNode baseDm = DocumentModelSupport.toJson(DocumentModelSupport.serialize(result));
    if (baseDm == null) {
      throw new IllegalStateException("Failed to serialize base document model");
    }

    return new BasePrecomputationModel(
        baseDm,
        DocumentModelSupport.includedTypeDefinitions(result.getContent().getTypeDefinitions()),
        DocumentModelSupport.importedTypeDefinitions(result.getContent().getTypeDefinitions()),
        includedImportedTypeDefinitionsForBase(result.getContent().getTypeDefinitions()));
  }

  public JsonNode generateStructuralMappingModel(String smmId, MM_MappingModel_2 mappingModel, List<DocumentModel> documentModels) {
    var resolver = new InMemoryIDocumentModelResolver(documentModels);
    String precomputationModelId =
        mappingModel.content().preComputationFragment() != null ? mappingModel.content().preComputationFragment().dmId() : null;
    DocumentModel precomputationModel =
        precomputationModelId != null ? (DocumentModel) resolver.getDocumentModelById(precomputationModelId) : null;
    String suffix =
        precomputationModel != null
            ? precomputationModel.getHeader().getAnnotations().stream()
                .filter(a -> a.getName().equals("_SMM_GENERATOR_FIELD_NAME_SUFFIX"))
                .findFirst()
                .map(a -> a.getValue())
                .orElse(null)
            : null;
    var kernelConfig = SMMGeneratorConfig.of(smmId, "_NO_FIELD_MAPPING_GENERATION", suffix);
    List<RankedNotification> problems = new ArrayList<>();

    var smm = MappingModelService.create(resolver).generateStructuralMappingModel(mappingModel, kernelConfig, problems::add, Locale.US);
    JsonNode json = DocumentModelSupport.toJson(StructuralMappingModelWithContext.serializeSMM(smm));
    if (json == null) {
      throw new IllegalStateException("Failed to serialize generated structural mapping model");
    }
    return json;
  }

  // Temporary fix for root model type defs
  private static Map<String, TypeDefinitionInfo> includedImportedTypeDefinitionsForBase(List<FieldTypeDefinition> typeDefinitions) {
    List<FieldTypeDefinition> tdsWithPurpose =
        typeDefinitions.stream()
            .filter(td ->
                (!td.getAllModelReferencePaths().isEmpty()
                        && td.getAllModelReferencePaths().stream()
                            .anyMatch(refList -> refList.get(0).getPurpose().equals("include")
                                && refList.get(refList.size() - 1).getPurpose().equals("typeDefinitions")))
                    || (!td.getAllModelReferencePaths().isEmpty()
                        && td.getAllModelReferencePaths().stream()
                            .anyMatch(refList -> refList.stream().allMatch(ref -> ref.getPurpose().equals("typeDefinitions")))
                        && td.getAllModelReferencePaths().stream()
                            .anyMatch(refList -> refList.stream().allMatch(ref -> ref.getPurpose().equals("include")))))
            .toList();

    Map<String, TypeDefinitionInfo> result = new HashMap<>();
    for (FieldTypeDefinition td : tdsWithPurpose) {
      var includedReferencePath =
          td.getAllModelReferencePaths().stream().filter(refList -> refList.get(0).getPurpose().equals("include")).findFirst().orElseThrow();
      result.put(td.getId(), new TypeDefinitionInfo(includedReferencePath.stream().map(r -> r.getAlias()).toList()));
    }
    return result;
  }

  // TODO: should be imported from kernelExtensions instead
  private static JsonNode convertRankedNotifications(List<RankedNotification> notifications) {
    var result = new ObjectMapper().createObjectNode();
    for (RankedNotification notification : notifications) {
      String source;
      if (notification.getSource() instanceof SMMNotificationSource<?>) {
        source = MM_MappingModel_2._pointer().content().structuralMappingModel().id()._unwrap().toString();
        // If a Source Model Name starts with "xml" the group in the generated model is given as source. It cannot be
        // mapped accordingly by us for the editor.
      } else if (notification.getSource() instanceof Entity) {
        source = "unknown";
      } else if (notification.getSource() == null) {
        source = "unknown";
      } else {
        source = notification.getSource().toString();
      }

      var entry = new ObjectMapper().createArrayNode();
      if (result.has(source)) {
        entry.addAll((ArrayNode) result.get(source));
      }
      entry.add(DocumentModelSupport.stringifyRankedNotification(notification));
      result.set(source, entry);
    }
    return result;
  }
}
