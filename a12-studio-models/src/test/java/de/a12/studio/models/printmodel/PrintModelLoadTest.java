package de.a12.studio.models.printmodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.ModelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrintModelLoadTest {

  @Test
  void loadsEmptyPrintModelSkeleton() throws Exception {
    PrintModel model = ModelRoundTrip.load(getClass(), "/printmodel/PrintModel.json", PrintModel.class);

    assertEquals("PrintModel", model.getId());
    assertEquals(ModelType.PRINT, model.getModelType());
    assertEquals("4.0.0", model.getModelVersion());

    PrintModelContent content = model.getContent();
    assertEquals("PRINT_MODEL_CONTENT", content.getId());
    assertEquals("\"PrintModel\"", content.getGeneral().getMetadata().getTitleComputation().get(0).getOperation());
    assertEquals(12, content.getGeneral().getSegmentDefaults().getFontSize());
    assertTrue(content.getSegments().getDefinitions().isEmpty());
    assertTrue(content.getElementDefinitions().isEmpty());
  }

  @Test
  void loadsPopulatedExamplePrintModel() throws Exception {
    PrintModel model = ModelRoundTrip.load(getClass(), "/printmodel/Example_PM.json", PrintModel.class);

    PrintModelContent content = model.getContent();
    assertEquals(2, content.getGeneral().getStructure().size());
    assertEquals("0pq4GN6X48qvRvcxtJlwa", content.getGeneral().getStructure().get(0).getId());

    assertEquals(2, content.getSegments().getDefinitions().size());
    PrintSegmentDefinition firstSegment = content.getSegments().getDefinitions().get(0);
    assertEquals("First Segment", firstSegment.getTitle());
    assertEquals("Default", firstSegment.getType());
    assertEquals("Portrait", firstSegment.getDefaultSegment().getPageOrientation());
    assertEquals(2, firstSegment.getElementReferences().size());

    PrintElementReference reference = firstSegment.getElementReferences().get(0);
    assertEquals("4ZTShELl9YK0UU79fz5uV", reference.getRefId());
    assertEquals(new java.math.BigDecimal("74"), reference.getPosition().getX().getValue());
    assertEquals("Millimeter", reference.getPosition().getX().getUnit());
    assertEquals(new java.math.BigDecimal("6"), reference.getDimensions().getMinHeight().getValue());
    assertEquals("DEFAULT", reference.getPageBreakBehavior().getSource());

    assertEquals(6, content.getElementDefinitions().size());
    PrintTextElement text = assertInstanceOf(PrintTextElement.class, content.getElementDefinitions().get(0));
    assertEquals("Text", text.getType());
    assertTrue(text.getText().getText().contains("First Segment"));
    assertEquals("INPUT", text.getTextProperties().getTextStyleId().getSource());
    assertEquals("no-text-style-fallback-id", text.getTextProperties().getTextStyleId().getValue());

    // The second text element embeds a Field and a Calculation entity.
    PrintTextElement entityText = assertInstanceOf(PrintTextElement.class, content.getElementDefinitions().get(1));
    assertEquals(2, entityText.getText().getEntities().size());
    assertEquals("OspND3O0c2JAcjZHvPkut", entityText.getText().getEntities().get(0).getRefId());

    PrintFieldElement field = assertInstanceOf(PrintFieldElement.class, content.getElementDefinitions().get(2));
    assertEquals("Example_DM", field.getField().getModel());
    assertEquals("/example/stringField", field.getField().getPath());

    PrintCalculationElement calculation = assertInstanceOf(PrintCalculationElement.class, content.getElementDefinitions().get(3));
    assertEquals("Calculation", calculation.getCalculation().getName());
    assertEquals("[Example_DM/example/numberField]", calculation.getCalculation().getComputationAlternatives().get(0).getOperation());
    assertNotNull(calculation.getCalculation().getModel());
  }

  @Test
  void roundTripsEmptyPrintModel() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/printmodel/PrintModel.json", PrintModel.class);
  }

  @Test
  void roundTripsExamplePrintModel() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/printmodel/Example_PM.json", PrintModel.class);
  }
}
