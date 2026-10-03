package de.a12.studio.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Pins the kernel's structural mapping services as the facade exposes them, with a tiny source/target pair that is
 * written out inline: a repeatable {@code Addresses} group on both sides, so a field mapping into it needs a
 * resolution strategy.
 */
class KernelStructuralMappingContractTest {

  private static final String EMPTY_SMM = """
      {"header":{"id":"Test_SMM","modelType":"structuralmapping","modelVersion":"1.0.0",
        "locales":[{"code":"en"}],"modelReferences":[]},
       "content":{"GroupsToClearOnFirstFill":[],"MappingBlocks":[]}}""";

  private static final String MAPPING_MODEL = """
      {"header":{"id":"Test_Ma","modelType":"mapping","modelVersion":"1.0.0",
        "locales":[{"code":"en"}],"modelReferences":[
          {"modelType":"document","reference":"Source_DM"},{"modelType":"document","reference":"Target_DM"},
          {"modelType":"structuralmapping","reference":"Test_SMM"}]},
       "content":{"Source":[{"dmId":"Source_DM","name":"Src","maxRepeat":1,"includeLevel":"MODEL_ROOT"}],
         "Target":{"dmId":"Target_DM"},"StructuralMappingModel":{"id":"Test_SMM"}}}""";

  private final KernelStructuralMapping kernel = new KernelStructuralMapping();

  private static String documentModel(String id, String rootGroup) {
    return """
        {"header":{"id":"%s","modelType":"document","modelVersion":"29.4.0","locales":[{"code":"en"}],"modelReferences":[]},
         "content":{"modelInfo":{"name":"%s","immutable":false},
           "modelConfig":{"timeZone":"UTC","decimalSeparator":".","conditionLanguage":{"code":"en_US"}},
           "modelRoot":{"rootGroups":[
             {"type":"Group","id":"G1","name":"%s","Group":{"repeatability":1,"elements":[
               {"type":"Field","id":"F1","name":"Name","Field":{"fieldType":{"type":"StringType"}}},
               {"type":"Group","id":"G2","name":"Addresses","Group":{"repeatability":5,"elements":[
                 {"type":"Field","id":"F2","name":"City","Field":{"fieldType":{"type":"StringType"}}},
                 {"type":"Field","id":"F3","name":"Street","Field":{"fieldType":{"type":"StringType"}}}]}}]}}]}}}"""
        .formatted(id, id, rootGroup);
  }

  private static KernelModelSource source() {
    Map<String, String> models = Map.of(
        "Source_DM", documentModel("Source_DM", "Person"),
        "Target_DM", documentModel("Target_DM", "Employee"));
    return id -> {
      String json = models.get(id);
      if (json == null) {
        throw new UncheckedIOException(new FileNotFoundException(id));
      }
      return json;
    };
  }

  @Test
  void computesTheSourceAndTargetModelOfAMappingModel() {
    KernelStructuralMapping.Context context = kernel.computeContext(MAPPING_MODEL, source());

    // The source model is the Source Model joined under its Name; the target model is the Target as is.
    assertTrue(context.sourceDocumentModelJson().contains("\"Src\""), context.sourceDocumentModelJson());
    assertTrue(context.sourceDocumentModelJson().contains("\"Person\""), context.sourceDocumentModelJson());
    assertTrue(context.targetDocumentModelJson().contains("\"Employee\""), context.targetDocumentModelJson());
  }

  @Test
  void addsAFieldMappingWithItsResolutionStrategy() {
    KernelStructuralMapping.Context context = kernel.computeContext(MAPPING_MODEL, source());

    String withName = kernel.addFieldMapping(EMPTY_SMM, context, "/Src/Person/Name", "/Employee/Name");
    String withCity = kernel.addFieldMapping(withName, context, "/Src/Person/Addresses/City", "/Employee/Addresses/City");

    assertTrue(withName.contains("/Employee/Name"), withName);
    assertTrue(withCity.contains("ResolutionStrategies"), withCity);
    assertTrue(withCity.contains("/Employee/Addresses"), withCity);
    List<KernelFinding> findings = kernel.checkFull(withCity, context);
    assertEquals(List.of(), findings.stream().filter(KernelFinding::isError).toList());
  }

  @Test
  void reportsFindingsWithTheDocumentPointerOfTheElement() {
    KernelStructuralMapping.Context context = kernel.computeContext(MAPPING_MODEL, source());
    String smm = kernel.addFieldMapping(EMPTY_SMM, context, "/Src/Person/Name", "/Employee/Name")
        .replace("/Employee/Name", "/Employee/DoesNotExist");

    List<KernelFinding> findings = kernel.checkFull(smm, context);

    assertFalse(findings.isEmpty(), "a field mapping onto an unknown field must be reported");
    assertTrue(findings.stream().anyMatch(f -> f.isError() && "/content[1]/MappingBlocks[1]/FieldMappings[1]".equals(f.elementPath())),
        findings::toString);
  }

  @Test
  void knowsTheAlternativesOfAResolutionStrategy() {
    KernelStructuralMapping.Context context = kernel.computeContext(MAPPING_MODEL, source());
    String smm = kernel.addFieldMapping(EMPTY_SMM, context, "/Src/Person/Addresses/City", "/Employee/Addresses/City");
    String pointer = "/content/MappingBlocks[1]/ResolutionStrategies[1]";

    assertTrue(kernel.validSourceGroups(smm, context, pointer).contains("/Src/Person/Addresses"));
    assertEquals(Set.of("Fold", "Slice"), kernel.validResolutionStrategyTypes(smm, context, pointer));
  }

  @Test
  void checksStructureWithoutContext() {
    List<KernelFinding> findings = kernel.checkStandalone(EMPTY_SMM);

    assertTrue(findings.stream().noneMatch(f -> f.elementPath() != null), findings::toString);
  }
}
