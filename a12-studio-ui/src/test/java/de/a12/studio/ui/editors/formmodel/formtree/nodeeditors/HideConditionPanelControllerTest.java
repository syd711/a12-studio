package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.HideCondition;
import de.a12.studio.models.formmodel.HideConditionCase;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Copy/Paste Hide Condition: SME's tree-level Ctrl+H/Ctrl+B actions, ported as panel buttons since a12-studio
// already funnels every node type's Hide Condition editing through this one shared panel.
class HideConditionPanelControllerTest {

  private static final String DIR = "/de/a12/studio/ui/editors/formmodel/formtree/nodeeditors/";

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private static HideConditionPanelController load() throws Exception {
    return FxTestSupport.<HideConditionPanelController>load(DIR + "hide-condition-panel.fxml").controller();
  }

  private static HideCondition condition(String masterField, String... masterValues) {
    HideCondition condition = new HideCondition();
    condition.setMasterField(masterField);
    for (String value : masterValues) {
      HideConditionCase c = new HideConditionCase();
      c.setMasterValue(value);
      condition.getCases().add(c);
    }
    return condition;
  }

  private static void configure(HideConditionPanelController panel, String nodeId, Supplier<HideCondition> getter,
      Consumer<HideCondition> setter) throws Exception {
    FxTestSupport.onFx(() -> panel.configure(nodeId, getter, setter, null, HideConditionPanelController.MasterFieldScope.root()));
  }

  @Test
  void copyButtonIsDisabledUntilTheNodeHasAHideCondition() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    HideConditionPanelController panel = load();
    HideCondition[] holder = {null};

    configure(panel, "node1", () -> holder[0], hc -> holder[0] = hc);
    Button copyButton = FxTestSupport.field(panel, "copyHideConditionButton");
    assertTrue(copyButton.isDisabled());

    holder[0] = condition("field_a", "x");
    configure(panel, "node1", () -> holder[0], hc -> holder[0] = hc);
    assertFalse(copyButton.isDisabled());
  }

  // copiedHideConditionSource is static (a cross-panel, cross-tab "clipboard", like FormModelActions'
  // clipboardJson), so these assertions only ever compare states within one self-contained scenario rather
  // than assuming a pristine "nothing copied yet" starting point - a source may already be set by an earlier
  // test or a real earlier Copy click elsewhere in the running application.
  @Test
  void pasteButtonTracksWhetherTheCopySourceCurrentlyHasAHideCondition() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    HideConditionPanelController source = load();
    HideCondition[] sourceHolder = {condition("field_a", "x")};
    configure(source, "source", () -> sourceHolder[0], hc -> sourceHolder[0] = hc);
    Button copyButton = FxTestSupport.field(source, "copyHideConditionButton");
    FxTestSupport.onFx(copyButton::fire);

    HideConditionPanelController target = load();
    HideCondition[] targetHolder = {null};
    configure(target, "target", () -> targetHolder[0], hc -> targetHolder[0] = hc);
    Button pasteButton = FxTestSupport.field(target, "pasteHideConditionButton");
    assertFalse(pasteButton.isDisabled(), "a source was just copied");

    // The copied-from node's hide condition is cleared elsewhere before the paste happens.
    sourceHolder[0] = null;
    configure(target, "target", () -> targetHolder[0], hc -> targetHolder[0] = hc);
    assertTrue(pasteButton.isDisabled(), "the copy source no longer has a hide condition to paste");
  }

  @Test
  void pasteWritesAnIndependentCloneOntoTheTargetNode() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    HideConditionPanelController source = load();
    HideCondition[] sourceHolder = {condition("field_a", "x")};
    configure(source, "source", () -> sourceHolder[0], hc -> sourceHolder[0] = hc);
    Button copyButton = FxTestSupport.field(source, "copyHideConditionButton");
    FxTestSupport.onFx(copyButton::fire);

    HideConditionPanelController target = load();
    HideCondition[] targetHolder = {null};
    configure(target, "target", () -> targetHolder[0], hc -> targetHolder[0] = hc);
    Button pasteButton = FxTestSupport.field(target, "pasteHideConditionButton");
    FxTestSupport.onFx(pasteButton::fire);

    assertNotNull(targetHolder[0]);
    assertEquals("field_a", targetHolder[0].getMasterField());
    assertEquals(List.of("x"), targetHolder[0].getCases().stream().map(HideConditionCase::getMasterValue).toList());
    assertNotSame(sourceHolder[0].getCases(), targetHolder[0].getCases(), "paste must clone, not alias, the source's cases list");

    // Mutating the pasted copy must not reach back into the source.
    targetHolder[0].getCases().clear();
    assertEquals(1, sourceHolder[0].getCases().size());
  }

  @Test
  void pasteReadsTheSourceSCurrentValueNotACopyTimeSnapshot() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    HideConditionPanelController source = load();
    HideCondition[] sourceHolder = {condition("field_a", "x")};
    configure(source, "source", () -> sourceHolder[0], hc -> sourceHolder[0] = hc);
    Button copyButton = FxTestSupport.field(source, "copyHideConditionButton");
    FxTestSupport.onFx(copyButton::fire);

    // The source is edited again after Copy, before Paste happens.
    sourceHolder[0] = condition("field_b", "y", "z");

    HideConditionPanelController target = load();
    HideCondition[] targetHolder = {null};
    configure(target, "target", () -> targetHolder[0], hc -> targetHolder[0] = hc);
    Button pasteButton = FxTestSupport.field(target, "pasteHideConditionButton");
    FxTestSupport.onFx(pasteButton::fire);

    assertEquals("field_b", targetHolder[0].getMasterField());
    assertEquals(List.of("y", "z"), targetHolder[0].getCases().stream().map(HideConditionCase::getMasterValue).toList());
  }

  @Test
  void pasteWithNoExistingTargetHideConditionNeedsNoConfirmation() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    HideConditionPanelController source = load();
    HideCondition[] sourceHolder = {condition("field_a", "x")};
    configure(source, "source", () -> sourceHolder[0], hc -> sourceHolder[0] = hc);
    Button copyButton = FxTestSupport.field(source, "copyHideConditionButton");
    FxTestSupport.onFx(copyButton::fire);

    HideConditionPanelController target = load();
    HideCondition[] targetHolder = {null};
    configure(target, "target", () -> targetHolder[0], hc -> targetHolder[0] = hc);
    Button pasteButton = FxTestSupport.field(target, "pasteHideConditionButton");

    // Would hang on a modal showAndWait() if a confirmation were (incorrectly) shown for a blank target.
    FxTestSupport.onFx(pasteButton::fire);

    assertNotNull(targetHolder[0]);
    assertEquals("field_a", targetHolder[0].getMasterField());
  }
}
