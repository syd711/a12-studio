package de.a12.studio.modelsvalidation.formincludes;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelElements;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Form Model bound to a Combined Document Model as the source of an include. {@code PersonEmployee_Fm} of the
 * advanced workspace is bound to {@code PersonEmployee_Cm} (base {@code Person_Dc} + the additive {@code
 * PersonEmployee_Ad}); the expander follows it through the stand-in {@link CombinedDocumentModelElements} makes of a
 * combination - a Document Model carrying the combination's own id, with the additive elements under their
 * {@code md5(additiveId)_} ids - which the caller adds to the Document Models it hands in. The host is made up: a
 * Document Model that includes the combination below {@code /Host/employee}.
 */
class FormIncludeCombinedSourceTest {

  private static final String ADDITIVE_PREFIX = "3ebb47b738ad9c6e3c36113ff04df00d_";

  private static final String HOST_DM = """
      {"header":{"id":"Host_DM","modelType":"document","modelVersion":"29.4.0","locales":[{"code":"en"}]},
       "content":{"modelInfo":{"name":"Host_DM"},"modelRoot":{"rootGroups":[
         {"type":"Group","id":"group_host","name":"Host","Group":{"repeatability":1,"elements":[
           {"type":"Group","id":"include_emp","name":"employee","Group":{"repeatability":1,
             "includeConfig":{"reference":"PersonEmployee_Cm"}}}]}}]}}}
      """;

  private static final String HOST_FORM = """
      {"header":{"id":"Host_FM","modelType":"form","modelVersion":"37.3.0","locales":[{"code":"en"}],
         "modelReferences":[{"alias":"Host_DM","modelType":"document","purpose":"data binding","reference":"Host_DM"}]},
       "content":{"screens":[{"id":"screen_1","name":"Screen 1","screenElements":[]}],
         "fieldConfiguration":{},"groupConfiguration":{},"defaults":{}}}
      """;

  private static Path workspace() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("advanced_new");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/advanced_new'");
  }

  private static void collect(ProjectItem item, List<A12Model<?>> models) {
    if (item.isFolder()) {
      item.getChildren().forEach(child -> collect(child, models));
    }
    else if (item.getModel() != null) {
      models.add(item.getModel());
    }
  }

  private final ProjectItem project = new ProjectItem(workspace().toFile());
  private final DocumentModel hostDm = JsonSettings.objectMapper.readValue(HOST_DM, DocumentModel.class);
  private final FormModel hostForm = JsonSettings.objectMapper.readValue(HOST_FORM, FormModel.class);
  private final List<DocumentModel> documentModels = documentModels();
  private final FormModel source = (FormModel) project.findByModelId("PersonEmployee_Fm").getModel();

  private List<DocumentModel> documentModels() {
    List<A12Model<?>> models = new ArrayList<>();
    collect(project, models);
    List<DocumentModel> result = new ArrayList<>();
    models.stream().filter(DocumentModel.class::isInstance).map(DocumentModel.class::cast).forEach(result::add);
    result.add(hostDm);
    result.add(CombinedDocumentModelElements.resolveForFieldReferences(project, "PersonEmployee_Cm"));
    return result;
  }

  @Test
  void theCombinationIsOnlyKnownThroughItsStandIn() {
    assertFalse(documentModels.stream().filter(model -> "PersonEmployee_Cm".equals(model.getId()))
        .findFirst().isEmpty(), "the stand-in carries the combination's id");
    assertEquals("PersonEmployee_Cm", FormIncludeExpander.documentModelIdOf(source));
    assertTrue(FormIncludeExpander.documentModelOf(source, documentModels).isPresent());
    assertEquals(List.of("/Host/employee"), FormIncludeExpander.candidateHostPaths(hostDm,
        FormIncludeExpander.documentModelOf(source, documentModels).orElseThrow(), documentModels));
  }

  @Test
  void aFormBoundToACombinationCanBeIncludedAndItsBaseAndAdditiveReferencesAllResolveInTheHost() {
    FormIncludeExpander.Expansion expansion = new FormIncludeExpander(documentModels)
        .expand(source, hostForm, "/Host/employee", "include-1", null);

    assertEquals(source.getContent().getScreens().get(0).getScreenElements().size(), expansion.elements().size());
    ElementIndex hostIndex = new ElementIndex(hostDm, documentModels);
    List<Control> controls = FormModelWalker.find(expansion.elements(), Control.class, node -> true);
    assertFalse(controls.isEmpty());
    for (Control control : controls) {
      assertTrue(control.getElementRef().startsWith("include_emp_"), control.getElementRef());
      assertTrue(hostIndex.isResolvable(control.getElementRef()), "does not resolve in the host: " + control.getElementRef());
    }
    assertTrue(controls.stream().anyMatch(control -> control.getElementRef().startsWith("include_emp_" + ADDITIVE_PREFIX)),
        "a field of the additive model keeps its md5 prefix below the include group");
    assertTrue(controls.stream().anyMatch(control -> !control.getElementRef().startsWith("include_emp_" + ADDITIVE_PREFIX)),
        "a field of the base model is there as well");
  }
}
