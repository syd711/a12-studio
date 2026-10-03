package de.a12.studio.modelsvalidation.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.structuralmappingmodel.ResolutionStrategyType;
import de.a12.studio.models.structuralmappingmodel.SmmElement;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

/** The structural mapping context end to end, on a small project written out as files: a Mapping Model with its SMM and two Document Models. */
class StructuralMappingContextTest {

  private static String documentModel(String id, String rootGroup) {
    return """
        {"header":{"id":"%s","modelType":"document","modelVersion":"29.4.0","locales":[{"code":"en"}],"modelReferences":[]},
         "content":{"modelInfo":{"name":"%s","immutable":false},
           "modelConfig":{"timeZone":"UTC","decimalSeparator":".","conditionLanguage":{"code":"en_US"}},
           "modelRoot":{"rootGroups":[
             {"type":"Group","id":"G1","name":"%s","Group":{"repeatability":1,"elements":[
               {"type":"Field","id":"F1","name":"Name","Field":{"fieldType":{"type":"StringType"}}},
               {"type":"Group","id":"G2","name":"Addresses","Group":{"repeatability":5,"elements":[
                 {"type":"Field","id":"F2","name":"City","Field":{"fieldType":{"type":"StringType"}}}]}}]}}]}}}"""
        .formatted(id, id, rootGroup);
  }

  private static final String MAPPING_MODEL = """
      {"header":{"id":"Test_Ma","modelType":"mapping","modelVersion":"1.0.0","locales":[{"code":"en"}],"modelReferences":[
          {"modelType":"document","reference":"Source_DM"},{"modelType":"document","reference":"Target_DM"},
          {"modelType":"structuralmapping","reference":"Test_SMM"}]},
       "content":{"Source":[{"dmId":"Source_DM","name":"Src","maxRepeat":1,"includeLevel":"MODEL_ROOT"}],
         "Target":{"dmId":"Target_DM"},"StructuralMappingModel":{"id":"Test_SMM"}}}""";

  private static final String SMM = """
      {"header":{"id":"Test_SMM","modelType":"structuralmapping","modelVersion":"1.0.0","locales":[{"code":"en"}],"modelReferences":[]},
       "content":{"GroupsToClearOnFirstFill":[],"MappingBlocks":[{"ResolutionStrategies":[
           {"type":"Fold","sourceGroupFullName":"/Src/Person/Addresses","targetGroupFullName":"/Employee/Addresses"}],
         "FieldMappings":[
           {"sourceFieldFullName":"/Src/Person/Name","targetFieldFullName":"/Employee/Name"},
           {"sourceFieldFullName":"/Src/Person/Addresses/City","targetFieldFullName":"/Employee/Addresses/City"}]}]}}""";

  @TempDir
  Path dir;

  private ProjectItem root;
  private StructuralMappingModel smm;
  private MappingModel mapping;

  @BeforeEach
  void writeProject() throws IOException {
    Files.writeString(dir.resolve("Source_DM.json"), documentModel("Source_DM", "Person"));
    Files.writeString(dir.resolve("Target_DM.json"), documentModel("Target_DM", "Employee"));
    Files.writeString(dir.resolve("Test_Ma.json"), MAPPING_MODEL);
    Files.writeString(dir.resolve("Test_SMM.json"), SMM);
    root = new ProjectItem(dir.toFile());
    smm = (StructuralMappingModel) root.findByModelId("Test_SMM").getModel();
    mapping = (MappingModel) root.findByModelId("Test_Ma").getModel();
  }

  private List<A12Model<?>> allModels() {
    return List.of(root.findByModelId("Source_DM").getModel(), root.findByModelId("Target_DM").getModel(), mapping, smm);
  }

  @Test
  void findsTheMappingModelThatUsesTheSmm() {
    assertEquals(List.of(mapping), StructuralMappingContext.findOwners(smm, allModels()));
  }

  @Test
  void offersTheJoinedSourceAndTheTargetAsElementTrees() throws StructuralMappingContext.Unavailable {
    StructuralMappingContext context = StructuralMappingContext.resolve(root, mapping);

    SmmElement city = SmmElement.find(context.sourceRoots(), "/Src/Person/Addresses/City");
    assertNotNull(city, () -> "source roots: " + context.sourceRoots());
    assertEquals("String", city.dataType());
    SmmElement addresses = SmmElement.find(context.targetRoots(), "/Employee/Addresses");
    assertNotNull(addresses);
    assertTrue(addresses.group());
    assertEquals(5, addresses.repeatability());
  }

  @Test
  void aConsistentModelHasNoErrors() throws StructuralMappingContext.Unavailable {
    List<StructuralMappingContext.Finding> findings = StructuralMappingContext.resolve(root, mapping).check(smm);

    assertEquals(List.of(), findings.stream().filter(f -> f.severity() == Severity.ERROR).toList());
  }

  @Test
  void reportsAMappingOntoAnUnknownFieldAtItsPointer() throws StructuralMappingContext.Unavailable {
    smm.getContent().getMappingBlocks().get(0).getFieldMappings().get(0).setTargetFieldFullName("/Employee/Nope");

    List<StructuralMappingContext.Finding> findings = StructuralMappingContext.resolve(root, mapping).check(smm);

    assertTrue(findings.stream().anyMatch(f -> f.severity() == Severity.ERROR
        && f.pointer().equals("/content/MappingBlocks[1]/FieldMappings[1]")), findings::toString);
  }

  @Test
  void theValidatorUsesTheContextWhenThereIsOneAndOtherwiseTheStructuralCheck() {
    smm.getContent().getMappingBlocks().get(0).getFieldMappings().get(0).setTargetFieldFullName("/Employee/Nope");
    ProjectItem item = root.findByModelId("Test_SMM");

    List<ModelValidationError> withContext = new StructuralMappingKernelValidator()
        .validate(smm, new ValidationContext(null, item, List.of(), allModels(), smm));
    List<ModelValidationError> withoutContext = new StructuralMappingKernelValidator()
        .validate(smm, new ValidationContext(null, item, List.of(), List.of(smm), smm));

    assertTrue(withContext.stream().anyMatch(e -> e.elementId().equals("/content/MappingBlocks[1]/FieldMappings[1]")), withContext::toString);
    // Without a Mapping Model the unknown field cannot be known; the structure itself is fine.
    assertEquals(List.of(), withoutContext);
  }

  @Test
  void addsAFieldMappingAndOffersTheAlternativesOfAStrategy() throws StructuralMappingContext.Unavailable {
    StructuralMappingContext context = StructuralMappingContext.resolve(root, mapping);

    var content = context.addFieldMapping(smm, "/Src/Person/Name", "/Employee/Name");
    assertTrue(content.getMappingBlocks().stream().flatMap(b -> b.getFieldMappings().stream())
        .anyMatch(f -> "/Employee/Name".equals(f.getTargetFieldFullName())));

    String pointer = "/content/MappingBlocks[1]/ResolutionStrategies[1]";
    assertTrue(context.validSourceGroups(smm, pointer).contains("/Src/Person/Addresses"));
    assertEquals(Set.of(ResolutionStrategyType.FOLD, ResolutionStrategyType.SLICE), context.validTypes(smm, pointer));
  }

  @Test
  void aMappingModelWithoutATargetGivesNoContext() {
    mapping.getContent().getTarget().setDmId(null);

    org.junit.jupiter.api.Assertions.assertThrows(StructuralMappingContext.Unavailable.class,
        () -> StructuralMappingContext.resolve(root, mapping));
  }
}
