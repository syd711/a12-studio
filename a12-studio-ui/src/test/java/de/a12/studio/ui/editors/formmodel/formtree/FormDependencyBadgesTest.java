package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** SME's "D"/"T" decoration: a control with {@code dependentControls} triggers screen elements, which depend on it. */
class FormDependencyBadgesTest {

  private static Control.DependentControls.Entry entry(String idref) {
    Control.DependentControls.Entry entry = new Control.DependentControls.Entry();
    entry.setIdref(idref);
    return entry;
  }

  @Test
  void aTriggeringControlAndItsDependentElementsAreMarked() {
    Screen screen = new Screen();
    screen.setId("screen1");
    screen.setName("S");
    Section address = new Section();
    address.setId("sec1");
    address.setName("Address");
    Section other = new Section();
    other.setId("sec2");
    other.setName("Other");
    Control control = new Control();
    control.setId("control1");
    control.setDependentControls(new Control.DependentControls());
    control.getDependentControls().getScreenElement().addAll(List.of(entry("sec1"), entry("sec1"), entry("gone")));

    TreeItem<FormElementViewModel> root = new TreeItem<>();
    TreeItem<FormElementViewModel> screenItem = new TreeItem<>(new FormElementViewModel(screen, null, null));
    TreeItem<FormElementViewModel> addressItem = new TreeItem<>(new FormElementViewModel(address, screen, null));
    TreeItem<FormElementViewModel> otherItem = new TreeItem<>(new FormElementViewModel(other, screen, null));
    TreeItem<FormElementViewModel> controlItem = new TreeItem<>(new FormElementViewModel(control, screen, null));
    screenItem.getChildren().addAll(addressItem, otherItem, controlItem);
    root.getChildren().add(screenItem);

    FormDependencyBadges.apply(root);

    // The control triggers the (once-listed) section and one element that no longer exists.
    List<FormDependencyBadges.Entry> masterOf = controlItem.getValue().getMasterOf();
    assertEquals(2, masterOf.size());
    assertEquals("S/Address", masterOf.get(0).path());
    assertEquals(FormDependencyBadges.UNKNOWN_PATH, masterOf.get(1).path());
    assertTrue(controlItem.getValue().getDependentOn().isEmpty(), "a control is never a dependent");

    // The section depends on the control; an unrelated one is unmarked.
    assertEquals(1, addressItem.getValue().getDependentOn().size());
    assertTrue(addressItem.getValue().getDependentOn().get(0).path().startsWith("S/"));
    assertTrue(addressItem.getValue().getMasterOf().isEmpty());
    assertTrue(otherItem.getValue().getDependentOn().isEmpty() && otherItem.getValue().getMasterOf().isEmpty());
  }

  @Test
  void marksAreResetWhenTheDependencyIsRemoved() {
    Section section = new Section();
    section.setId("sec1");
    Control control = new Control();
    control.setId("control1");
    control.setDependentControls(new Control.DependentControls());
    control.getDependentControls().getScreenElement().add(entry("sec1"));
    TreeItem<FormElementViewModel> root = new TreeItem<>();
    TreeItem<FormElementViewModel> sectionItem = new TreeItem<>(new FormElementViewModel(section, null, null));
    TreeItem<FormElementViewModel> controlItem = new TreeItem<>(new FormElementViewModel(control, null, null));
    root.getChildren().addAll(sectionItem, controlItem);
    FormDependencyBadges.apply(root);
    assertEquals(1, sectionItem.getValue().getDependentOn().size());

    control.setDependentControls(null);
    FormDependencyBadges.apply(root);

    assertTrue(sectionItem.getValue().getDependentOn().isEmpty());
    assertTrue(controlItem.getValue().getMasterOf().isEmpty());
  }
}
