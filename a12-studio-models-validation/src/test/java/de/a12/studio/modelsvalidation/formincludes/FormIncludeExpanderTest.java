package de.a12.studio.modelsvalidation.formincludes;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.formincludes.FormIncludeExpander.Expansion;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The fixtures are SME's own include example ({@code client/resources/input/models/fmm/workspace}): a host form
 * bound to {@code A-for-host}, whose Person/address group includes {@code B-for-include}, and the form
 * {@code IncludedModel} bound to {@code B-for-include}. {@code HostModel_expanded} is what the Form Engine's batch
 * expansion made of it. The Document Models were converted to a12-studio's {@code includeConfig} wire shape (SME's
 * copy predates it) and got a second include group, {@code billing}.
 */
class FormIncludeExpanderTest {

  private final DocumentModel hostDm = TestModels.load("/formincludes/A-for-host.json", DocumentModel.class);
  private final DocumentModel includedDm = TestModels.load("/formincludes/B-for-include.json", DocumentModel.class);
  private final FormModel includedForm = load("IncludedModel");
  private final FormIncludeExpander expander = new FormIncludeExpander(List.of(hostDm, includedDm));

  private static FormModel load(String name) {
    return TestModels.load("/formincludes/" + name + ".json", FormModel.class);
  }

  private static String json(Object value) {
    return JsonSettings.objectMapper.writeValueAsString(value);
  }

  /** The host as it was before the expansion: SME's result with the address section emptied. */
  private static FormModel unexpandedHost() {
    FormModel host = load("HostModel_expanded");
    addressSection(host).getScreenElements().clear();
    return host;
  }

  private static Section addressSection(FormModel host) {
    return host.getContent().getScreens().get(0).getScreenElements().stream()
        .filter(element -> "address-section".equals(element.getName()))
        .map(Section.class::cast).findFirst().orElseThrow();
  }

  @Test
  void expandingReproducesWhatTheFormEngineMadeOfSmesFixture() {
    FormModel host = unexpandedHost();

    Expansion expansion = expander.expand(includedForm, host, "/Person/address", "controlgrid_130cc", "address-cg");
    addressSection(host).getScreenElements().addAll(expansion.elements());

    assertEquals(json(load("HostModel_expanded").getContent()), json(host.getContent()));
    assertTrue(expansion.warnings().isEmpty());
    assertTrue(expansion.fieldEntries().isEmpty());
    assertTrue(expansion.groupEntries().isEmpty());
  }

  @Test
  void provenanceIsOnTheTopLevelElementsOnly() {
    Expansion expansion = expander.expand(includedForm, unexpandedHost(), "/Person/address", "controlgrid_130cc", null);

    ScreenElement grid = expansion.elements().get(0);
    assertEquals("controlgrid_130cc", grid.getIncludeId());
    assertEquals("IncludedModel", grid.getFormModelRef());
    assertEquals("/Person/address", grid.getHostDocumentModelPath());
    assertEquals("address-cg", grid.getName(), "without a requested name the source's name stays");
    List<ScreenElement> withProvenance = FormModelWalker.find(expansion.elements(), ScreenElement.class, node -> true).stream()
        .filter(element -> element.getIncludeId() != null).toList();
    assertEquals(1, withProvenance.size());
  }

  @Test
  void aSecondIncludeOfTheSameFormGetsItsOwnIdsAndBindsToTheOtherGroup() {
    Expansion expansion = expander.expand(includedForm, unexpandedHost(), "/Person/billing", "controlgrid_2", "billing-cg");

    ControlGrid grid = (ControlGrid) expansion.elements().get(0);
    assertEquals("controlgrid_2_controlgrid_248a7", grid.getId());
    assertEquals("billing-cg", grid.getName());
    Control first = (Control) grid.getRow().get(0).getCell().get(0);
    assertEquals("controlgrid_2_control_e03ed", first.getId());
    assertEquals("include_5c0d1_field_b0b4d", first.getElementRef());
  }

  @Test
  void aPathOfSlashKeepsTheSourcePathAndOnlyPrefixesTheIds() {
    FormModel host = load("IncludedModel");
    host.setId("Other");

    Expansion expansion = expander.expand(includedForm, host, "/", "inc", null);

    Control first = (Control) ((ControlGrid) expansion.elements().get(0)).getRow().get(0).getCell().get(0);
    assertEquals("inc_control_e03ed", first.getId());
    assertEquals("field_b0b4d", first.getElementRef());
  }

  @Test
  void neitherTheSourceNorTheHostIsModified() {
    FormModel host = unexpandedHost();
    String sourceBefore = json(includedForm);
    String hostBefore = json(host);

    expander.expand(includedForm, host, "/Person/address", "controlgrid_130cc", null);

    assertEquals(sourceBefore, json(includedForm));
    assertEquals(hostBefore, json(host));
  }

  @Test
  void aPathThatDoesNotExistInTheHostDocumentModelFailsAndNamesIt() {
    FormIncludeException failure = assertThrows(FormIncludeException.class,
        () -> expander.expand(includedForm, unexpandedHost(), "/Person/nowhere", "inc", null));

    assertTrue(failure.getMessage().contains("/Person/nowhere/"), failure.getMessage());
  }

  @Test
  void aFormWithoutAScreenCannotBeIncluded() {
    FormModel empty = load("IncludedModel");
    empty.getContent().getScreens().clear();

    FormIncludeException failure = assertThrows(FormIncludeException.class,
        () -> expander.expand(empty, unexpandedHost(), "/Person/address", "inc", null));

    assertTrue(failure.getMessage().contains("IncludedModel"), failure.getMessage());
  }

  @Test
  void configurationOfWhatTheCopiedElementsBindToComesAlongRekeyed() {
    FormModel source = load("IncludedModel");
    FieldConfigEntry entry = new FieldConfigEntry();
    entry.setElementRef("field_b0b4d");
    entry.setReadonly(true);
    source.getContent().getFieldConfiguration().getField().add(entry);
    FieldConfigEntry unused = new FieldConfigEntry();
    unused.setElementRef("field_unused");
    source.getContent().getFieldConfiguration().getField().add(unused);

    Expansion expansion = expander.expand(source, unexpandedHost(), "/Person/address", "inc", null);

    assertEquals(1, expansion.fieldEntries().size());
    assertEquals("include_8a4ea_field_b0b4d", expansion.fieldEntries().get(0).getElementRef());
    assertEquals(Boolean.TRUE, expansion.fieldEntries().get(0).getReadonly());
    assertEquals("field_b0b4d", entry.getElementRef(), "the source's entry is a copy");
  }

  @Test
  void aDependentControlInsideTheCopyFollowsItsTargetAndOneOutsideIsDroppedWithAWarning() {
    FormModel source = load("IncludedModel");
    Control control = (Control) ((ControlGrid) source.getContent().getScreens().get(0).getScreenElements().get(0))
        .getRow().get(0).getCell().get(0);
    Control.DependentControls dependentControls = new Control.DependentControls();
    dependentControls.getScreenElement().add(entry("controlgrid_248a7"));
    dependentControls.getScreenElement().add(entry("somewhere_else"));
    control.setDependentControls(dependentControls);

    Expansion expansion = expander.expand(source, unexpandedHost(), "/Person/address", "inc", null);

    Control copy = (Control) ((ControlGrid) expansion.elements().get(0)).getRow().get(0).getCell().get(0);
    List<Control.DependentControls.Entry> entries = copy.getDependentControls().getScreenElement();
    assertEquals(1, entries.size());
    assertEquals("inc_controlgrid_248a7", entries.get(0).getIdref());
    assertEquals(1, expansion.warnings().size());
    assertTrue(expansion.warnings().get(0).contains("somewhere_else"), expansion.warnings().get(0));
  }

  private static Control.DependentControls.Entry entry(String idref) {
    Control.DependentControls.Entry entry = new Control.DependentControls.Entry();
    entry.setIdref(idref);
    entry.setMasterValue("true");
    return entry;
  }

  @Test
  void fillUnsetOnlyTakesOverWhatTheExistingEntryLacks() {
    FieldConfigEntry existing = new FieldConfigEntry();
    existing.setElementRef("a");
    existing.setInitialValue("mine");
    FieldConfigEntry added = new FieldConfigEntry();
    added.setElementRef("a");
    added.setInitialValue("theirs");
    added.setReadonly(true);

    FieldConfigEntry merged = FormIncludeExpander.fillUnset(existing, added, FieldConfigEntry.class);

    assertEquals("mine", merged.getInitialValue());
    assertEquals(Boolean.TRUE, merged.getReadonly());
    assertNull(existing.getReadonly(), "the existing entry is not modified");
  }

  @Test
  void anIncludeRunIsTheElementAndItsNeighborsWithTheSameIncludeId() {
    List<Object> siblings = new ArrayList<>(List.of(element(null), element("a"), element("a"), element("a"), element("b")));

    assertArrayEquals(new int[] {1, 3}, FormIncludeExpander.includeRun(siblings, 2));
    assertArrayEquals(new int[] {1, 3}, FormIncludeExpander.includeRun(siblings, 1));
    assertArrayEquals(new int[] {0, 0}, FormIncludeExpander.includeRun(siblings, 0));
    assertArrayEquals(new int[] {4, 4}, FormIncludeExpander.includeRun(siblings, 4));
  }

  private static ScreenElement element(String includeId) {
    Section section = new Section();
    section.setIncludeId(includeId);
    return section;
  }

  @Test
  void anIncludeIdIsFreeOnlyIfNoIdIsOrStartsWithIt() {
    FormModel host = load("HostModel_expanded");

    assertFalse(FormIncludeExpander.isIncludeIdFree(host.getContent(), "controlgrid_130cc"));
    assertFalse(FormIncludeExpander.isIncludeIdFree(host.getContent(), "section_18a7f"));
    assertTrue(FormIncludeExpander.isIncludeIdFree(host.getContent(), "controlgrid_130"));
    assertTrue(FormIncludeExpander.isIncludeIdFree(host.getContent(), "include_new"));
  }

  @Test
  void theIncludeGroupsOfTheSourceDocumentModelAreTheCandidatePaths() {
    List<String> paths = FormIncludeExpander.candidateHostPaths(hostDm, includedDm, List.of(hostDm, includedDm));

    assertEquals(List.of("/Person/address", "/Person/billing"), paths);
  }

  @Test
  void theDocumentModelOfAFormIsItsDataBindingReference() {
    Optional<DocumentModel> found = FormIncludeExpander.documentModelOf(includedForm, List.of(hostDm, includedDm));

    assertNotNull(found.orElse(null));
    assertEquals("B-for-include", found.get().getId());
  }

  @Test
  void elementIndexResolvesAPathToTheIdAFormUses() {
    ElementIndex index = new ElementIndex(hostDm, List.of(hostDm, includedDm));

    assertEquals(Optional.of("field_e3022"), index.resolveIdByPath("/Person/firstName"));
    assertEquals(Optional.of("include_8a4ea"), index.resolveIdByPath("/Person/address"));
    assertEquals(Optional.of("include_8a4ea_field_e2fea"), index.resolveIdByPath("/Person/address/street"));
    assertEquals(Optional.of("include_5c0d1_field_e2fea"), index.resolveIdByPath("/Person/billing/street"));
    assertEquals(Optional.empty(), index.resolveIdByPath("/Person/address/nope"));
    assertEquals(Optional.empty(), index.resolveIdByPath("/Person/firstName/deeper"));
    assertEquals(Optional.empty(), index.resolveIdByPath("/"));
  }
}
