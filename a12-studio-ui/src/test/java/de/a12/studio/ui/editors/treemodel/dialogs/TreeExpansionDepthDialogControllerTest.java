package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.treemodel.ExpansionDepth;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Expansion Depth dialog needs a Relationship before OK is enabled, defaults Max Depth to 1, offers a
 * dangling current reference alongside the valid choices, and returns a new {@link ExpansionDepth} instead
 * of touching the one it was opened for.
 */
class TreeExpansionDepthDialogControllerTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private static FxTestSupport.Loaded<TreeExpansionDepthDialogController> open(List<String> relationships, ExpansionDepth existing) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FxTestSupport.Loaded<TreeExpansionDepthDialogController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/treemodel/dialogs/tree-expansion-depth-dialog.fxml");
    FxTestSupport.onFx(() -> loaded.controller().init(new Stage(), relationships, existing));
    return loaded;
  }

  private static ExpansionDepth depth(String relationship, int maxDepth) {
    ExpansionDepth depth = new ExpansionDepth();
    depth.setRelationshipModel(relationship);
    depth.setMaxDepth(maxDepth);
    return depth;
  }

  @Test
  void addingRequiresARelationshipAndDefaultsMaxDepthToOne() throws Exception {
    FxTestSupport.Loaded<TreeExpansionDepthDialogController> loaded = open(List.of("TeamTeam_Re", "TeamPerson_Re"), null);
    ComboBox<String> relationship = FxTestSupport.field(loaded.controller(), "relationshipField");
    Spinner<Integer> maxDepth = FxTestSupport.field(loaded.controller(), "maxDepthField");
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");

    assertEquals(List.of("TeamTeam_Re", "TeamPerson_Re"), relationship.getItems());
    assertEquals(1, maxDepth.getValue());
    assertTrue(ok.isDisabled(), "no relationship chosen yet");

    FxTestSupport.onFx(() -> {
      relationship.setValue("TeamTeam_Re");
      maxDepth.getValueFactory().setValue(5);
    });
    assertFalse(ok.isDisabled());
    FxTestSupport.onFx(ok::fire);

    ExpansionDepth result = loaded.controller().getResult().orElseThrow();
    assertEquals("TeamTeam_Re", result.getRelationshipModel());
    assertEquals(5, result.getMaxDepth());
  }

  @Test
  void editingBuildsANewEntryAndKeepsADanglingRelationshipSelectable() throws Exception {
    ExpansionDepth existing = depth("Gone_Re", 3);
    FxTestSupport.Loaded<TreeExpansionDepthDialogController> loaded = open(List.of("TeamTeam_Re"), existing);
    ComboBox<String> relationship = FxTestSupport.field(loaded.controller(), "relationshipField");
    Spinner<Integer> maxDepth = FxTestSupport.field(loaded.controller(), "maxDepthField");
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");

    assertEquals(List.of("TeamTeam_Re", "Gone_Re"), relationship.getItems());
    assertEquals("Gone_Re", relationship.getValue());
    assertEquals(3, maxDepth.getValue());

    FxTestSupport.onFx(() -> maxDepth.getValueFactory().setValue(4));
    FxTestSupport.onFx(ok::fire);

    ExpansionDepth result = loaded.controller().getResult().orElseThrow();
    assertNotSame(existing, result);
    assertEquals("Gone_Re", result.getRelationshipModel());
    assertEquals(4, result.getMaxDepth());
    assertEquals(3, existing.getMaxDepth(), "the entry the dialog was opened for is untouched");
  }
}
