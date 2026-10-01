package de.a12.studio.models.contentmodel;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContentPropertyFormatsTest {

  @Test
  void splitsKeywordsAndStripsUnitDefaults() {
    ContentPropertyFormats.Format height = ContentPropertyFormats.require("style.height");
    assertEquals(List.of("auto", "fit-content"), height.keywordList());
    assertEquals(List.of("px", "%"), height.unitNames());
    assertEquals("px:400,%:100", height.units());
  }

  @Test
  void emptySpecsGiveEmptyLists() {
    assertEquals(List.of(), ContentPropertyFormats.require("style.padding").keywordList());
    assertEquals(List.of(), ContentPropertyFormats.require("style.overflow").unitNames());
  }

  @Test
  void unknownPath() {
    assertNull(ContentPropertyFormats.forPath("style.color"));
    assertNull(ContentPropertyFormats.forPath(null));
    assertThrows(IllegalArgumentException.class, () -> ContentPropertyFormats.require("style.color"));
  }
}
