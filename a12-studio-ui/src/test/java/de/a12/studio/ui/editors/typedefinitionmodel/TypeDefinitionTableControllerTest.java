package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.StringTypeOptions;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins Gap 1 of the 2026-09-27 Type Definition Model review: unlike a plain Document Model (which must pick
 * either local-only or imported-only type definitions), a {@link TypeDefinitionModel} itself may hold both at
 * once - SME's {@code selectTypeDefinitionMode()} returns {@code "combined"} exactly for this case, so neither
 * the Add nor the Import button is ever disabled on a TDM. Also pins the "low priority" per-row "invalid"
 * indicator gap (2026-09-28): a type definition with a validation problem - its own, or (for an included/
 * imported row) its actual owning model's - is marked in {@code errorMessagesByTypeDefinitionId}, matching
 * SME's {@code TypedefOverview} "invalidTypeDefs" column.
 */
class TypeDefinitionTableControllerTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
    if (toolkitAvailable) {
      // TypeDefinitionTableController.refreshValidationState() calls Studio.getValidationService(); its
      // ProjectModels walk needs a real (even empty) folder - an unloaded Project's root has no File and NPEs.
      Project validationProject = new Project();
      validationProject.load(Files.createTempDirectory("typedefinition-validation").toFile());
      FxTestSupport.setValidationServiceForProject(validationProject);
    }
  }

  @AfterAll
  static void stopValidationService() throws Exception {
    if (toolkitAvailable) {
      FxTestSupport.clearValidationService();
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    FxTestSupport.onFx(() -> {
    });
  }

  @Test
  void addAndImportStayEnabledOnATypeDefinitionModelWithBoth() throws Exception {
    Assumptions.assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    TypeDefinitionModel tdm = new TypeDefinitionModel();
    tdm.setId("Combined_TDM");
    DocumentModelContent content = newContent();
    TypeDefinition local = new TypeDefinition();
    local.setId("typedef_local");
    local.setName("Local");
    local.setFieldType(new StringFieldType());
    content.getTypeDefinitions().add(local);
    tdm.setContent(content);

    ModelReference importReference = new ModelReference();
    importReference.setPurpose(ModelReference.PURPOSE_TYPE_DEFINITIONS);
    importReference.setReference("Other_TDM");
    tdm.getModelReferences().add(importReference);

    DocumentModel other = new TypeDefinitionModel();
    other.setId("Other_TDM");
    other.setContent(newContent());

    FxTestSupport.Loaded<TypeDefinitionTableController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/typedefinitionmodel/typedefinition-table.fxml");
    TypeDefinitionTableController controller = loaded.controller();

    FxTestSupport.onFx(() -> controller.load(tdm, List.of(other)));

    Button addButton = FxTestSupport.field(controller, "addButton");
    Button importButton = FxTestSupport.field(controller, "importButton");
    assertFalse(addButton.isDisabled(), "Add must stay enabled on a TDM that already imports another TDM");
    assertFalse(importButton.isDisabled(), "Import must stay enabled on a TDM that already owns a local type definition");
  }

  @Test
  void addAndImportStayMutuallyExclusiveOnAPlainDocumentModel() throws Exception {
    Assumptions.assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    DocumentModel plain = new DocumentModel();
    plain.setId("Plain_DM");
    DocumentModelContent content = newContent();
    TypeDefinition local = new TypeDefinition();
    local.setId("typedef_local");
    local.setName("Local");
    local.setFieldType(new StringFieldType());
    content.getTypeDefinitions().add(local);
    plain.setContent(content);

    FxTestSupport.Loaded<TypeDefinitionTableController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/typedefinitionmodel/typedefinition-table.fxml");
    TypeDefinitionTableController controller = loaded.controller();

    FxTestSupport.onFx(() -> controller.load(plain, List.of()));

    Button importButton = FxTestSupport.field(controller, "importButton");
    assertTrue(importButton.isDisabled(), "Import must stay disabled once a plain Document Model owns a local type definition");
  }

  @Test
  void marksAnOwnTypeDefinitionWithAnInvalidStringConfigAsInvalid() throws Exception {
    Assumptions.assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    DocumentModel plain = new DocumentModel();
    plain.setId("Plain_DM");
    DocumentModelContent content = newContent();
    content.getTypeDefinitions().add(invalidStringTypeDefinition("typedef_invalid"));
    TypeDefinition valid = new TypeDefinition();
    valid.setId("typedef_valid");
    valid.setName("Valid");
    valid.setFieldType(new StringFieldType());
    content.getTypeDefinitions().add(valid);
    plain.setContent(content);

    FxTestSupport.Loaded<TypeDefinitionTableController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/typedefinitionmodel/typedefinition-table.fxml");
    TypeDefinitionTableController controller = loaded.controller();

    FxTestSupport.onFx(() -> controller.load(plain, List.of()));

    Map<String, List<String>> errors = FxTestSupport.field(controller, "errorMessagesByTypeDefinitionId");
    assertTrue(errors.containsKey("typedef_invalid"), "A type definition with minLength > maxLength must be marked invalid");
    assertFalse(errors.containsKey("typedef_valid"), "An unconfigured type definition must not be marked invalid");
  }

  @Test
  void marksAnImportedTypeDefinitionInvalidWhenItsOwningModelReportsAProblem() throws Exception {
    Assumptions.assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    TypeDefinitionModel importer = new TypeDefinitionModel();
    importer.setId("Importer_TDM");
    importer.setContent(newContent());
    ModelReference importReference = new ModelReference();
    importReference.setPurpose(ModelReference.PURPOSE_TYPE_DEFINITIONS);
    importReference.setReference("Owner_TDM");
    importer.getModelReferences().add(importReference);

    TypeDefinitionModel owner = new TypeDefinitionModel();
    owner.setId("Owner_TDM");
    DocumentModelContent ownerContent = newContent();
    ownerContent.getTypeDefinitions().add(invalidStringTypeDefinition("typedef_owner_invalid"));
    owner.setContent(ownerContent);

    FxTestSupport.Loaded<TypeDefinitionTableController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/typedefinitionmodel/typedefinition-table.fxml");
    TypeDefinitionTableController controller = loaded.controller();

    FxTestSupport.onFx(() -> controller.load(importer, List.of(owner)));

    Map<String, List<String>> errors = FxTestSupport.field(controller, "errorMessagesByTypeDefinitionId");
    assertTrue(errors.containsKey("typedef_owner_invalid"),
        "An imported type definition invalid in its owning model must still be marked invalid here");
  }

  /** Validation's {@code ElementIndex} requires a non-null {@code modelRoot} - a TDM's own "stays empty" per
   * CLAUDE.md, but empty is still a real, non-null {@link ModelRoot}, not a missing one. */
  private static DocumentModelContent newContent() {
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(new ModelRoot());
    return content;
  }

  private static TypeDefinition invalidStringTypeDefinition(String id) {
    TypeDefinition invalid = new TypeDefinition();
    invalid.setId(id);
    invalid.setName("Invalid");
    StringFieldType invalidType = new StringFieldType();
    StringTypeOptions options = new StringTypeOptions();
    options.setMinLength(10);
    options.setMaxLength(5);
    invalidType.setStringType(options);
    invalid.setFieldType(invalidType);
    return invalid;
  }
}
