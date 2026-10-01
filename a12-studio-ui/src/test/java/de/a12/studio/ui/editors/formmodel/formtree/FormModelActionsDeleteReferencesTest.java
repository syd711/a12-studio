package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.ButtonGroup;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.HeaderFooterBox;
import de.a12.studio.models.formmodel.NavigationButton;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.util.commandstack.CommandStack;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The confirmation of a delete lists what the delete cleans up besides the node itself. */
class FormModelActionsDeleteReferencesTest {

  private static FormModelActions actions(FormModelContent content) throws Exception {
    File file = new File(FormModelActionsDeleteReferencesTest.class.getResource("/formincludes/HostModel_expanded.json").toURI());
    return new FormModelActions(content, new CommandStack(), node -> {
    }, new ProjectItem(file), null);
  }

  @Test
  void aScreenListsTheNavigationButtonsAndDependencyEntriesThatGoWithIt() throws Exception {
    FormModelContent content = new FormModelContent();
    Screen first = new Screen();
    first.setId("screen1");
    Screen second = new Screen();
    second.setId("screen2");
    Section target = new Section();
    target.setId("sec1");
    second.getScreenElements().add(target);
    NavigationButton button = new NavigationButton();
    button.setId("button1");
    button.setName("Next");
    button.setTarget("screen2");
    ButtonGroup group = new ButtonGroup();
    group.getButton().add(button);
    HeaderFooterBox footer = new HeaderFooterBox();
    footer.setMajorButtons(group);
    first.setFooterBox(footer);
    Control trigger = new Control();
    trigger.setId("control1");
    trigger.setElementRef("field_a");
    trigger.setDependentControls(new Control.DependentControls());
    Control.DependentControls.Entry entry = new Control.DependentControls.Entry();
    entry.setIdref("sec1");
    trigger.getDependentControls().getScreenElement().add(entry);
    Section triggerSection = new Section();
    triggerSection.setId("sec0");
    first.getScreenElements().add(triggerSection);
    content.getScreens().addAll(List.of(first, second));
    // the trigger sits in the first screen (as a plain element list entry is enough for the walker's Control search)
    de.a12.studio.models.formmodel.ControlGrid grid = new de.a12.studio.models.formmodel.ControlGrid();
    grid.setId("grid1");
    de.a12.studio.models.formmodel.Row row = new de.a12.studio.models.formmodel.Row();
    row.getCell().add(trigger);
    grid.getRow().add(row);
    triggerSection.getScreenElements().add(grid);

    FormModelActions actions = actions(content);

    List<String> forSecondScreen = actions.affectedReferences(new FormElementViewModel(second, null, null));
    assertEquals(2, forSecondScreen.size(), forSecondScreen.toString());
    assertTrue(forSecondScreen.stream().anyMatch(line -> line.contains("Next")), forSecondScreen.toString());
    assertTrue(forSecondScreen.stream().anyMatch(line -> line.contains("field_a")), forSecondScreen.toString());

    assertEquals(List.of(), actions.affectedReferences(new FormElementViewModel(first, null, null)));
  }
}
