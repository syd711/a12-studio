package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.Section;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FormModelActionsIncludeProvenanceTest {

  @Test
  void aCopyOfAnIncludedElementIsNoPartOfTheIncludeAnyMore() {
    Section section = new Section();
    section.setIncludeId("include-1");
    section.setFormModelRef("Other_FM");
    section.setHostDocumentModelPath("/Person/address");
    ControlGrid grid = new ControlGrid();
    grid.setIncludeId("include-1");
    section.getScreenElements().add(grid);

    FormModelActions.clearIncludeProvenance(section);

    assertNull(section.getIncludeId());
    assertNull(section.getFormModelRef());
    assertNull(section.getHostDocumentModelPath());
    assertNull(grid.getIncludeId());
  }

  @Test
  void anElementWithoutProvenanceStaysAsItIs() {
    Section section = new Section();
    section.setName("plain");

    FormModelActions.clearIncludeProvenance(section);

    assertEquals("plain", section.getName());
    assertNull(section.getIncludeId());
  }
}
