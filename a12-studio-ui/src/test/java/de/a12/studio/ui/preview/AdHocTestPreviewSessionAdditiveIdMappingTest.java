package de.a12.studio.ui.preview;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the pure id-mapping logic {@link AdHocTestPreviewSession} uses to test an {@code AdditiveDocumentModel}
 * (see its class javadoc): the rest of the class needs a real A12 installation ({@code SmeBackend}) to run at
 * all, which isn't available for an automated test, so this is the part that actually can be verified here.
 */
class AdHocTestPreviewSessionAdditiveIdMappingTest {

  @TempDir
  Path tempDir;

  @Test
  void additiveIdPrefixMatchesTheRealKernelsRewritingScheme() {
    // Independently verified in testing/workspaces/advanced_new/models/10_People/PersonEmployee_Ov.json:
    // elementRef "3ebb47b738ad9c6e3c36113ff04df00d_F7" for PersonEmployee_Ad's field F7 - see
    // CombinedDocumentModelElements's own javadoc in a12-studio-models for the same, independently-confirmed scheme.
    assertEquals("3ebb47b738ad9c6e3c36113ff04df00d_", AdHocTestPreviewSession.additiveIdPrefix("PersonEmployee_Ad"));
  }

  @Test
  void wholeModelElementIdsWalksEveryGroupAndField() {
    DocumentModel model = additiveModel();

    Set<String> ids = AdHocTestPreviewSession.wholeModelElementIds(model);

    assertEquals(Set.of("group_root", "field_first", "group_nested", "field_nested"), ids);
  }

  @Test
  void effectiveAdditiveElementIdsPrefixesAnExplicitSelection() {
    AdHocTestPreviewSession session = new AdHocTestPreviewSession(
        new ProjectItem(tempDir.toFile()), Set.of("field_first"), "SomeCombination_Cm");

    Set<String> effective = session.effectiveAdditiveElementIds(additiveModel());

    assertEquals(Set.of("3ebb47b738ad9c6e3c36113ff04df00d_field_first"), effective);
  }

  @Test
  void effectiveAdditiveElementIdsFallsBackToTheWholeAdditiveModelWhenNothingIsSelected() {
    AdHocTestPreviewSession session = new AdHocTestPreviewSession(
        new ProjectItem(tempDir.toFile()), Set.of(), "SomeCombination_Cm");

    Set<String> effective = session.effectiveAdditiveElementIds(additiveModel());

    String prefix = "3ebb47b738ad9c6e3c36113ff04df00d_";
    assertEquals(Set.of(prefix + "group_root", prefix + "field_first", prefix + "group_nested", prefix + "field_nested"), effective);
  }

  private static DocumentModel additiveModel() {
    DocumentModel model = new DocumentModel();
    model.setId("PersonEmployee_Ad");
    DocumentModelContent content = new DocumentModelContent();
    ModelRoot modelRoot = new ModelRoot();

    GroupElement nested = new GroupElement();
    nested.setId("group_nested");
    nested.setName("Nested");
    GroupConfig nestedConfig = new GroupConfig();
    nestedConfig.setRepeatability(1);
    nestedConfig.setElements(List.of(field("field_nested", "Nested Field")));
    nested.setGroup(nestedConfig);

    GroupElement root = new GroupElement();
    root.setId("group_root");
    root.setName("Root");
    GroupConfig rootConfig = new GroupConfig();
    rootConfig.setRepeatability(1);
    rootConfig.setElements(List.of(field("field_first", "First Field"), nested));
    root.setGroup(rootConfig);

    modelRoot.setRootGroups(List.of(root));
    content.setModelRoot(modelRoot);
    model.setContent(content);
    return model;
  }

  private static FieldElement field(String id, String name) {
    FieldElement field = new FieldElement();
    field.setId(id);
    field.setName(name);
    FieldConfig fieldConfig = new FieldConfig();
    fieldConfig.setFieldType(new StringFieldType());
    field.setField(fieldConfig);
    return field;
  }
}
