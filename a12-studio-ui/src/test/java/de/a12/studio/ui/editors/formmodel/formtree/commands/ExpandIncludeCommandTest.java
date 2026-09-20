package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.formincludes.FormIncludeExpander;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Inserting/refreshing an include as one undo step, on SME's include example (see FormIncludeExpanderTest).
class ExpandIncludeCommandTest {

  private final DocumentModel hostDm = load("A-for-host", DocumentModel.class);
  private final DocumentModel includedDm = load("B-for-include", DocumentModel.class);
  private final FormModel source = load("IncludedModel", FormModel.class);
  private final FormModel host = load("HostModel_expanded", FormModel.class);
  private final FormIncludeExpander expander = new FormIncludeExpander(List.of(hostDm, includedDm));

  private static <T> T load(String name, Class<T> type) {
    try (InputStream in = ExpandIncludeCommandTest.class.getResourceAsStream("/formincludes/" + name + ".json")) {
      return JsonSettings.objectMapper.readValue(in, type);
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private FormIncludeExpander.Expansion expand(String includeId, String path) {
    return expander.expand(source, host, path, includeId, null);
  }

  @Test
  void executeInsertsTheElementsAndUndoTakesThemOutAgain() {
    List<Object> siblings = new ArrayList<>(List.of("first"));
    FormIncludeExpander.Expansion expansion = expand("inc", "/Person/address");
    ExpandIncludeCommand command = new ExpandIncludeCommand(host.getContent(), siblings, 1, 0, expansion);

    command.execute();

    assertEquals(2, siblings.size());
    assertSame(expansion.elements().get(0), siblings.get(1));

    command.undo();

    assertEquals(List.of("first"), siblings);
  }

  @Test
  void refreshingReplacesTheRunAndUndoBringsBackTheSameInstances() {
    List<Object> siblings = new ArrayList<>(List.of("before"));
    FormIncludeExpander.Expansion first = expand("inc", "/Person/address");
    new ExpandIncludeCommand(host.getContent(), siblings, 1, 0, first).execute();
    ScreenElement original = first.elements().get(0);

    FormIncludeExpander.Expansion second = expand("inc", "/Person/address");
    ExpandIncludeCommand refresh = new ExpandIncludeCommand(host.getContent(), siblings, 1, 1, second);
    refresh.execute();

    assertEquals(2, siblings.size());
    assertSame(second.elements().get(0), siblings.get(1));

    refresh.undo();

    assertEquals(2, siblings.size());
    assertSame(original, siblings.get(1));
  }

  @Test
  void anEntryTheHostAlreadyHasIsMergedNotReplacedAndUndoPutsTheOriginalBack() {
    FieldConfigEntry existing = new FieldConfigEntry();
    existing.setElementRef("include_8a4ea_field_b0b4d");
    existing.setInitialValue("mine");
    host.getContent().getFieldConfiguration().getField().add(existing);
    FieldConfigEntry inSource = new FieldConfigEntry();
    inSource.setElementRef("field_b0b4d");
    inSource.setInitialValue("theirs");
    inSource.setReadonly(true);
    source.getContent().getFieldConfiguration().getField().add(inSource);
    FieldConfigEntry newOne = new FieldConfigEntry();
    newOne.setElementRef("field_dcc51");
    source.getContent().getFieldConfiguration().getField().add(newOne);
    List<FieldConfigEntry> entries = host.getContent().getFieldConfiguration().getField();

    ExpandIncludeCommand command = new ExpandIncludeCommand(host.getContent(), new ArrayList<>(), 0, 0,
        expand("inc", "/Person/address"));
    command.execute();

    assertEquals(2, entries.size());
    assertEquals("mine", entries.get(0).getInitialValue(), "what the host has stays");
    assertEquals(Boolean.TRUE, entries.get(0).getReadonly(), "what it lacks is taken over");
    assertEquals("include_8a4ea_field_dcc51", entries.get(1).getElementRef());

    command.undo();

    assertEquals(1, entries.size());
    assertSame(existing, entries.get(0));
    assertTrue(entries.get(0).getReadonly() == null);
  }

  @Test
  void anEmbeddedRepeatsGridIsReplacedByTheIncludedOneAndUndoPutsItBack() {
    EmbeddedRepeat repeat = new EmbeddedRepeat();
    ControlGrid old = new ControlGrid();
    repeat.setControlGrid(old);
    FormIncludeExpander.Expansion expansion = expand("inc", "/Person/address");
    ExpandIncludeCommand command = new ExpandIncludeCommand(host.getContent(), repeat, expansion);

    command.execute();

    assertSame(expansion.elements().get(0), repeat.getControlGrid());

    command.undo();

    assertSame(old, repeat.getControlGrid());
  }

  @Test
  void anEmbeddedRepeatOnlyTakesASingleControlGrid() {
    Section wrapper = new Section();
    wrapper.getScreenElements().addAll(source.getContent().getScreens().get(0).getScreenElements());
    source.getContent().getScreens().get(0).getScreenElements().clear();
    source.getContent().getScreens().get(0).getScreenElements().add(wrapper);
    FormIncludeExpander.Expansion expansion = expand("inc", "/Person/address");

    assertFalse(expansion.isSingleControlGrid());
    assertThrows(IllegalArgumentException.class, () -> new ExpandIncludeCommand(host.getContent(), new EmbeddedRepeat(), expansion));
  }

  @Test
  void redoAfterUndoDoesTheSameAgain() {
    List<Object> siblings = new ArrayList<>();
    ExpandIncludeCommand command = new ExpandIncludeCommand(host.getContent(), siblings, 0, 0, expand("inc", "/Person/address"));

    command.execute();
    command.undo();
    command.execute();

    assertEquals(1, siblings.size());
  }
}
