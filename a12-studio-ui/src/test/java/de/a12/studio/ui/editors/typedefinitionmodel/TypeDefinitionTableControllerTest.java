package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins Gap 1 of the 2026-09-27 Type Definition Model review: unlike a plain Document Model (which must pick
 * either local-only or imported-only type definitions), a {@link TypeDefinitionModel} itself may hold both at
 * once - SME's {@code selectTypeDefinitionMode()} returns {@code "combined"} exactly for this case, so neither
 * the Add nor the Import button is ever disabled on a TDM.
 */
class TypeDefinitionTableControllerTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
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
    DocumentModelContent content = new DocumentModelContent();
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
    other.setContent(new DocumentModelContent());

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
    DocumentModelContent content = new DocumentModelContent();
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
}
