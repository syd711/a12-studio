package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reproduces the include chain in testing/basic/models: Company_DM includes Invoice_DM (via a Group's
 * includeConfig), which itself includes Order_DM, which owns the "ProductType" type definition. Company_DM's
 * own typeDefinitions is empty, so without walking the Include chain that type definition would be invisible
 * from Company_DM's Type Definitions dialog even though fields inherited from Order_DM depend on it.
 */
class TransitiveTypeDefinitionsTest {

  @Test
  void resolvesATypeDefinitionInheritedTwoIncludesDeep() {
    DocumentModel order = modelWithTypeDefinitions("Order_DM", typeDef("typedef_1", "ProductType"));
    DocumentModel invoice = modelWithIncludes("Invoice_DM", "Order_DM");
    DocumentModel company = modelWithIncludes("Company_DM", "Invoice_DM");

    List<TransitiveTypeDefinitions.Entry> resolved = TransitiveTypeDefinitions.resolve(company, List.of(invoice, order));

    assertEquals(1, resolved.size());
    assertEquals("ProductType", resolved.get(0).typeDefinition().getName());
    assertEquals("Invoice_DM > Order_DM", resolved.get(0).sourcePath());
  }

  @Test
  void ignoresAnIncludeWhoseReferenceDoesNotResolve() {
    DocumentModel company = modelWithIncludes("Company_DM", "DoesNotExist_DM");

    List<TransitiveTypeDefinitions.Entry> resolved = TransitiveTypeDefinitions.resolve(company, List.of());

    assertTrue(resolved.isEmpty());
  }

  @Test
  void stopsFollowingAnIncludeCycleInsteadOfRecursingForever() {
    DocumentModel modelB = modelWithIncludes("B_DM", "A_DM");
    modelB.getContent().getTypeDefinitions().add(typeDef("typedef_b", "FromB"));
    DocumentModel modelA = modelWithIncludes("A_DM", "B_DM");

    List<TransitiveTypeDefinitions.Entry> resolved = TransitiveTypeDefinitions.resolve(modelA, List.of(modelA, modelB));

    assertEquals(1, resolved.size());
    assertEquals("FromB", resolved.get(0).typeDefinition().getName());
  }

  @Test
  void deduplicatesATypeDefinitionReachableThroughTwoDifferentIncludePaths() {
    DocumentModel shared = modelWithTypeDefinitions("Shared_DM", typeDef("typedef_common", "Common"));
    DocumentModel left = modelWithIncludes("Left_DM", "Shared_DM");
    DocumentModel right = modelWithIncludes("Right_DM", "Shared_DM");

    DocumentModel root = new DocumentModel();
    root.setId("Root_DM");
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(List.of(includeGroup("include_left", "Left_DM"), includeGroup("include_right", "Right_DM")));
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(modelRoot);
    root.setContent(content);

    List<TransitiveTypeDefinitions.Entry> resolved =
        TransitiveTypeDefinitions.resolve(root, List.of(left, right, shared));

    assertEquals(1, resolved.size());
    assertEquals("Common", resolved.get(0).typeDefinition().getName());
  }

  @Test
  void resolvesATypeDefinitionImportedFromATypeDefinitionModel() {
    DocumentModel tdm = tdmWithTypeDefinitions("Currency_TDM", typeDef("typedef_currency", "Currency"));
    DocumentModel model = modelWithImports("Invoice_DM", "Currency_TDM");

    List<TransitiveTypeDefinitions.Entry> resolved = TransitiveTypeDefinitions.resolve(model, List.of(tdm));

    assertEquals(1, resolved.size());
    assertEquals("Currency", resolved.get(0).typeDefinition().getName());
    assertEquals("Currency_TDM", resolved.get(0).sourcePath());
    assertTrue(resolved.get(0).imported());
  }

  @Test
  void resolvesATypeDefinitionImportedThroughATdmThatItselfImportsAnotherTdm() {
    DocumentModel tdmA = tdmWithTypeDefinitions("A_TDM", typeDef("typedef_base", "Base"));
    DocumentModel tdmB = modelWithImports("B_TDM", "A_TDM");
    DocumentModel model = modelWithImports("Invoice_DM", "B_TDM");

    List<TransitiveTypeDefinitions.Entry> resolved = TransitiveTypeDefinitions.resolve(model, List.of(tdmA, tdmB));

    assertEquals(1, resolved.size());
    assertEquals("Base", resolved.get(0).typeDefinition().getName());
    assertEquals("B_TDM > A_TDM", resolved.get(0).sourcePath());
    assertTrue(resolved.get(0).imported());
  }

  @Test
  void importedModelIdsFindsTransitiveImportsForCycleDetectionInThePicker() {
    DocumentModel tdmA = tdmWithTypeDefinitions("A_TDM");
    DocumentModel tdmB = modelWithImports("B_TDM", "A_TDM");

    assertEquals(Set.of("A_TDM"), TransitiveTypeDefinitions.importedModelIds(tdmB, List.of(tdmA)));
    assertTrue(TransitiveTypeDefinitions.importedModelIds(tdmB, List.of(tdmA)).contains("A_TDM"));
    assertFalse(TransitiveTypeDefinitions.importedModelIds(tdmA, List.of(tdmB)).contains("B_TDM"));
  }

  private static DocumentModel modelWithImports(String id, String... references) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(new ModelRoot());
    model.setContent(content);
    for (String reference : references) {
      ModelReference modelReference = new ModelReference();
      modelReference.setAlias(reference);
      modelReference.setModelType(ModelType.DOCUMENT);
      modelReference.setPurpose(ModelReference.PURPOSE_TYPE_DEFINITIONS);
      modelReference.setReference(reference);
      model.getModelReferences().add(modelReference);
    }
    return model;
  }

  private static DocumentModel tdmWithTypeDefinitions(String id, TypeDefinition... typeDefinitions) {
    TypeDefinitionModel model = new TypeDefinitionModel();
    model.setId(id);
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(new ModelRoot());
    content.setTypeDefinitions(new java.util.ArrayList<>(List.of(typeDefinitions)));
    model.setContent(content);
    return model;
  }

  private static DocumentModel modelWithIncludes(String id, String... references) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(java.util.Arrays.stream(references)
        .map(reference -> includeGroup("include_" + reference, reference))
        .toList());
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(modelRoot);
    model.setContent(content);
    return model;
  }

  private static DocumentModel modelWithTypeDefinitions(String id, TypeDefinition... typeDefinitions) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(new ModelRoot());
    content.setTypeDefinitions(new java.util.ArrayList<>(List.of(typeDefinitions)));
    model.setContent(content);
    return model;
  }

  private static GroupElement includeGroup(String id, String reference) {
    GroupElement group = new GroupElement();
    group.setId(id);
    group.setName(id);
    GroupConfig config = new GroupConfig();
    IncludeConfig includeConfig = new IncludeConfig();
    includeConfig.setReference(reference);
    config.setIncludeConfig(includeConfig);
    group.setGroup(config);
    return group;
  }

  private static TypeDefinition typeDef(String id, String name) {
    TypeDefinition typeDefinition = new TypeDefinition();
    typeDefinition.setId(id);
    typeDefinition.setName(name);
    typeDefinition.setFieldType(new StringFieldType());
    return typeDefinition;
  }
}
