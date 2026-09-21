package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveDependentControlEntriesCommandTest {

  private final FormModelContent content = new FormModelContent();

  private final Control master = new Control();

  private final Control onlyDependsOnGone = new Control();

  @Test
  void dropsTheEntriesOfRemovedElementsAndKeepsTheOthers() {
    master.setDependentControls(block("gone", "kept", "gone2"));
    setUp();

    new RemoveDependentControlEntriesCommand(content, Set.of("gone", "gone2")).execute();

    assertEquals(List.of("kept"), idrefs(master));
  }

  @Test
  void dropsABlockThatWouldBeLeftEmpty() {
    onlyDependsOnGone.setDependentControls(block("gone"));
    setUp();

    new RemoveDependentControlEntriesCommand(content, Set.of("gone")).execute();

    assertNull(onlyDependsOnGone.getDependentControls());
  }

  @Test
  void undoRestoresTheBlockAndTheEntryOrder() {
    master.setDependentControls(block("a", "gone", "b"));
    onlyDependsOnGone.setDependentControls(block("gone"));
    Control.DependentControls masterBlock = master.getDependentControls();
    Control.DependentControls otherBlock = onlyDependsOnGone.getDependentControls();
    setUp();
    RemoveDependentControlEntriesCommand command = new RemoveDependentControlEntriesCommand(content, Set.of("gone"));

    command.execute();
    command.undo();

    assertSame(masterBlock, master.getDependentControls());
    assertEquals(List.of("a", "gone", "b"), idrefs(master));
    assertSame(otherBlock, onlyDependsOnGone.getDependentControls());
    assertEquals(List.of("gone"), idrefs(onlyDependsOnGone));
  }

  @Test
  void isOnlyNeededWhenSomeControlStillListsARemovedId() {
    master.setDependentControls(block("a"));
    setUp();

    assertTrue(RemoveDependentControlEntriesCommand.isNeeded(content, Set.of("a")));
    assertFalse(RemoveDependentControlEntriesCommand.isNeeded(content, Set.of("b")));
    assertFalse(RemoveDependentControlEntriesCommand.isNeeded(content, Set.of()));
  }

  private void setUp() {
    master.setId("master");
    onlyDependsOnGone.setId("other");
    Row row = new Row();
    row.getCell().add(master);
    row.getCell().add(onlyDependsOnGone);
    ControlGrid grid = new ControlGrid();
    grid.getRow().add(row);
    Section section = new Section();
    section.getScreenElements().add(grid);
    Screen screen = new Screen();
    screen.getScreenElements().add(section);
    content.getScreens().add(screen);
  }

  private static Control.DependentControls block(String... idrefs) {
    Control.DependentControls block = new Control.DependentControls();
    for (String idref : idrefs) {
      Control.DependentControls.Entry entry = new Control.DependentControls.Entry();
      entry.setIdref(idref);
      block.getScreenElement().add(entry);
    }
    return block;
  }

  private static List<String> idrefs(Control control) {
    return control.getDependentControls().getScreenElement().stream().map(Control.DependentControls.Entry::getIdref).toList();
  }
}
