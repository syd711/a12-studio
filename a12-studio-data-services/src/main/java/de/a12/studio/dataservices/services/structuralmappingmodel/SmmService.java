package de.a12.studio.dataservices.services.structuralmappingmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingModelService;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class SmmService {

  public List<RankedNotification> validateWithContext(StructuralMappingModelWithContext context) {
    try {
      List<RankedNotification> problems = new ArrayList<>();
      var smmService = StructuralMappingModelService.create(context.getSourceDM(), context.getTargetDM());
      smmService.checkConsistencyFull(context.getSmm(), problems::add, Locale.US);
      return removeFirstDuplicationProblem(problems);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public List<RankedNotification> validateStandalone(MM_StructuralMappingModel_1 smm) {
    try {
      List<RankedNotification> problems = new ArrayList<>();
      StructuralMappingModelService.checkConsistencyStandalone(smm, problems::add, Locale.US);
      return removeFirstDuplicationProblem(problems);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public JsonNode addFieldMapping(AddFieldMappingDto dto) {
    var sourceModel = DocumentModelSupport.deserialize(dto.getSourceModel().toString());
    var targetModel = DocumentModelSupport.deserialize(dto.getTargetModel().toString());
    var oldSmm = StructuralMappingModelWithContext.deserializeSMM(dto.getStructuralMappingModel().toString());
    var service = StructuralMappingModelService.create(sourceModel, targetModel);
    var newSmm = service.addFieldMapping(oldSmm, dto.getSourceFieldPath(), dto.getTargetFieldPath());
    String serializedNewSmm = StructuralMappingModelWithContext.serializeSMM(newSmm);
    try {
      return new ObjectMapper().readTree(serializedNewSmm);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public List<StructuralMappingModelService.OptionForAddFieldMapping> getAvailableMoveTargets(
      StructuralMappingModelWithContext context, String sourceFieldFullName, String targetFieldFullName) {
    var smmService = StructuralMappingModelService.create(context.getSourceDM(), context.getTargetDM());
    return smmService.optionsForAddFieldMapping(context.getSmm(), sourceFieldFullName, targetFieldFullName);
  }

  public Set<String> getAvailableSourceGroups(StructuralMappingModelWithContext context, String resolutionStrategyPointer) {
    var smmService = StructuralMappingModelService.create(context.getSourceDM(), context.getTargetDM());
    var resolutionStrategy = DocumentPointer.of(resolutionStrategyPointer);
    return smmService.findValidSourceGroups(context.getSmm(), resolutionStrategy);
  }

  public Set<String> getAvailableSliceSourceFields(StructuralMappingModelWithContext context, String resolutionStrategyPointer) {
    var smmService = StructuralMappingModelService.create(context.getSourceDM(), context.getTargetDM());
    var resolutionStrategy = DocumentPointer.of(resolutionStrategyPointer);
    return smmService.findValidSliceSourceFields(context.getSmm(), resolutionStrategy);
  }

  public Set<String> getAvailableSliceTargetFields(StructuralMappingModelWithContext context, String resolutionStrategyPointer) {
    var smmService = StructuralMappingModelService.create(context.getSourceDM(), context.getTargetDM());
    var resolutionStrategy = DocumentPointer.of(resolutionStrategyPointer);
    return smmService.findValidSliceTargetFields(context.getSmm(), resolutionStrategy);
  }

  public Set<String> getAvailableResolutionStrategyTypes(StructuralMappingModelWithContext context, String resolutionStrategyPointer) {
    var smmService = StructuralMappingModelService.create(context.getSourceDM(), context.getTargetDM());
    var resolutionStrategy = DocumentPointer.of(resolutionStrategyPointer);
    var types = smmService.findValidResolutionStrategyTypes(context.getSmm(), resolutionStrategy);
    return types.stream().map(Enum::name).collect(Collectors.toSet());
  }

  private static List<RankedNotification> removeFirstDuplicationProblem(List<RankedNotification> problems) {
    Set<RankedNotification> duplicatedProblems = new LinkedHashSet<>();
    Set<String> visited = new LinkedHashSet<>();

    for (RankedNotification problem : problems) {
      if (!visited.contains(problem.getMessage()) && isDuplicationProblem(problem)) {
        visited.add(problem.getMessage());
        duplicatedProblems.add(problem);
      }
    }

    return problems.stream().filter(p -> !duplicatedProblems.contains(p)).toList();
  }

  private static boolean isDuplicationProblem(RankedNotification problem) {
    return problem.getMessage().endsWith("[RESOLUTION_STRATEGY_DUPLICATE]") || problem.getMessage().endsWith("[FIELD_MAPPING_DUPLICATE]");
  }
}
