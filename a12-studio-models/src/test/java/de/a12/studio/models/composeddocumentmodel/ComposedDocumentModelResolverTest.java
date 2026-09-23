package de.a12.studio.models.composeddocumentmodel;

import de.a12.studio.models.documentmodel.DocumentModelContent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComposedDocumentModelResolverTest {

  private static ComposedDocumentModel model() {
    ComposedDocumentModel model = new ComposedDocumentModel();
    model.setId("Order_CdM");
    model.setContent(new DocumentModelContent());
    return model;
  }

  @Test
  void queryRootRoundTripsThroughTheAnnotationList() {
    ComposedDocumentModel model = model();
    assertEquals(Optional.empty(), ComposedDocumentModelResolver.getQueryRootId(model));

    ComposedDocumentModelResolver.setQueryRootId(model, "Order_DM");
    assertEquals(Optional.of("Order_DM"), ComposedDocumentModelResolver.getQueryRootId(model));
    assertEquals(1, model.getAnnotations().size());
    assertEquals("cdm.queryRoot", model.getAnnotations().get(0).getName());

    ComposedDocumentModelResolver.setQueryRootId(model, null);
    assertEquals(Optional.empty(), ComposedDocumentModelResolver.getQueryRootId(model));
    assertTrue(model.getAnnotations().isEmpty());
  }

  @Test
  void firstStepUsesUnsuffixedAnnotationNames() {
    ComposedDocumentModel model = model();
    ComposedDocumentModelResolver.setRelationshipSteps(model,
        List.of(new CdmRelationshipStep("OrderPosition_ReM", "order", "position", "Position_DM")));

    assertEquals("OrderPosition_ReM", findAnnotationValue(model, "cdm.relationship"));
    assertEquals("order", findAnnotationValue(model, "cdm.sourceRole"));
    assertEquals("position", findAnnotationValue(model, "cdm.targetRole"));
    assertEquals("Position_DM", findAnnotationValue(model, "cdm.targetDocumentModel"));
  }

  @Test
  void additionalStepsAreSuffixed() {
    ComposedDocumentModel model = model();
    ComposedDocumentModelResolver.setRelationshipSteps(model, List.of(
        new CdmRelationshipStep("OrderPosition_ReM", "order", "position", "Position_DM"),
        new CdmRelationshipStep("PositionProduct_ReM", "position", "product", "Product_DM")));

    assertEquals("PositionProduct_ReM", findAnnotationValue(model, "cdm.relationship.1"));
    assertEquals("product", findAnnotationValue(model, "cdm.targetRole.1"));

    List<CdmRelationshipStep> steps = ComposedDocumentModelResolver.getRelationshipSteps(model);
    assertEquals(2, steps.size());
    assertEquals("OrderPosition_ReM", steps.get(0).getRelationshipName());
    assertEquals("PositionProduct_ReM", steps.get(1).getRelationshipName());
  }

  @Test
  void settingStepsReplacesThePreviousChain() {
    ComposedDocumentModel model = model();
    ComposedDocumentModelResolver.setRelationshipSteps(model, List.of(
        new CdmRelationshipStep("OrderPosition_ReM", "order", "position", "Position_DM"),
        new CdmRelationshipStep("PositionProduct_ReM", "position", "product", "Product_DM")));

    ComposedDocumentModelResolver.setRelationshipSteps(model,
        List.of(new CdmRelationshipStep("OrderCustomer_ReM", "order", "customer", "Customer_DM")));

    List<CdmRelationshipStep> steps = ComposedDocumentModelResolver.getRelationshipSteps(model);
    assertEquals(1, steps.size());
    assertEquals("OrderCustomer_ReM", steps.get(0).getRelationshipName());
  }

  private static String findAnnotationValue(ComposedDocumentModel model, String name) {
    return model.getAnnotations().stream()
        .filter(annotation -> name.equals(annotation.getName()))
        .findFirst()
        .map(de.a12.studio.models.Annotation::getValue)
        .orElse(null);
  }
}
