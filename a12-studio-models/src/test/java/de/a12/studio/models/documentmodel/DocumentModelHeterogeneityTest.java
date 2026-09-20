package de.a12.studio.models.documentmodel;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.TestHelper;
import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DocumentModelHeterogeneityTest {

  private static DocumentModel model(String id, String superTypes, String subTypes) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    addAnnotation(model, DocumentModelHeterogeneity.SUPER_TYPES_ANNOTATION, superTypes);
    addAnnotation(model, DocumentModelHeterogeneity.SUB_TYPES_ANNOTATION, subTypes);
    return model;
  }

  private static void addAnnotation(DocumentModel model, String name, String value) {
    if (value != null) {
      Annotation annotation = new Annotation();
      annotation.setName(name);
      annotation.setValue(value);
      model.getAnnotations().add(annotation);
    }
  }

  @Test
  void followsSuperTypesTransitivelyAndExcludesTheSourceItself() {
    List<DocumentModel> models = List.of(model("C", "B", null), model("B", "A", null), model("A", null, null));

    assertEquals(List.of("B", "A"), DocumentModelHeterogeneity.reachableSuperTypes(models, "C"));
    assertEquals(List.of(), DocumentModelHeterogeneity.reachableSuperTypes(models, "A"));
  }

  @Test
  void aSubTypesEntryOnTheParentIsTheSameAsASuperTypesEntryOnTheChild() {
    List<DocumentModel> models = List.of(model("Parent", null, "Child1, Child2"), model("Child1", null, null),
        model("Child2", null, null));

    assertEquals(List.of("Parent"), DocumentModelHeterogeneity.reachableSuperTypes(models, "Child1"));
    assertEquals(List.of("Parent"), DocumentModelHeterogeneity.reachableSuperTypes(models, "Child2"));
  }

  @Test
  void survivesCyclesAndDuplicateDeclarations() {
    List<DocumentModel> models = List.of(model("A", "B", "B"), model("B", "A", null));

    assertEquals(List.of("B"), DocumentModelHeterogeneity.reachableSuperTypes(models, "A"));
  }

  @Test
  void aModelThatIsNotADocumentModelHasNoSuperTypes() {
    // How a Combination Model looks from here: its id is not among the Document Models, so it isn't a node,
    // whatever its own superTypes annotation says (same as SME's heterogeneity graph).
    List<DocumentModel> models = List.of(model("Person_Dc", null, null));

    assertEquals(List.of(), DocumentModelHeterogeneity.reachableSuperTypes(models, "PersonEmployee_Cm"));
    assertEquals(List.of(), DocumentModelHeterogeneity.reachableSuperTypes(models, null));
  }

  @Test
  void resolvesTheProductHierarchyOfTheECommerceWorkspace() {
    List<DocumentModel> models = new ArrayList<>();
    collectDocumentModels(new ProjectItem(TestHelper.resolveTestingCommerceDir().toFile()), models);

    assertEquals(List.of("ProductSingle_DM", "Product_DM"),
        DocumentModelHeterogeneity.reachableSuperTypes(models, "ProductBook_DM"));
    assertEquals(List.of("Product_DM"), DocumentModelHeterogeneity.reachableSuperTypes(models, "ProductBundle_DM"));
    assertEquals(List.of(), DocumentModelHeterogeneity.reachableSuperTypes(models, "Product_DM"));
  }

  private static void collectDocumentModels(ProjectItem item, List<DocumentModel> result) {
    if (item.isFolder()) {
      item.getChildren().forEach(child -> collectDocumentModels(child, result));
    }
    else if (item.getModel() instanceof DocumentModel documentModel) {
      result.add(documentModel);
    }
  }
}
