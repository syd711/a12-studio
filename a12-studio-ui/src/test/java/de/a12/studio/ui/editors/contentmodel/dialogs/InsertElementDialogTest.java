package de.a12.studio.ui.editors.contentmodel.dialogs;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentInsertion;
import de.a12.studio.models.contentmodel.ContentModule;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** The "Add Element" dialog of the Content Model editor, loaded from its real FXML. */
class InsertElementDialogTest {

  private FxTestSupport.Loaded<InsertElementDialogController> loaded;

  @BeforeEach
  void load() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    loaded = FxTestSupport.load("/de/a12/studio/ui/editors/contentmodel/dialogs/insert-element-dialog.fxml");
  }

  private static List<ContentModule> insertableIntoABox() {
    ContentElement box = new ContentElement();
    box.setNamespace(ContentElementLibrary.NAMESPACE);
    box.setType("Box");
    return ContentInsertion.insertableModules(box, box, ContentInsertion.Position.AS_CHILD);
  }

  private void init(List<ContentModule> modules) throws Exception {
    FxTestSupport.onFx(() -> loaded.controller().init(new Stage(), "Box", modules));
  }

  @Test
  void offersOneTilePerTypeGroupedByCategoryAndNothingIsPreselected() throws Exception {
    List<ContentModule> modules = insertableIntoABox();

    init(modules);

    VBox categories = FxTestSupport.field(loaded.controller(), "categoriesBox");
    List<String> headings = new ArrayList<>();
    int tiles = 0;
    for (var node : categories.getChildren()) {
      VBox group = (VBox) node;
      headings.add(((Label) group.getChildren().get(0)).getText());
      tiles += ((FlowPane) group.getChildren().get(1)).getChildren().size();
    }
    assertEquals(4, headings.size(), headings.toString());
    assertEquals(modules.size(), tiles);
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");
    assertTrue(ok.isDisabled());
    assertNull(loaded.controller().getSelected());
  }

  @Test
  void confirmingReturnsTheSelectedType() throws Exception {
    init(insertableIntoABox());

    Button ok = FxTestSupport.field(loaded.controller(), "okButton");
    FxTestSupport.onFx(() -> {
      ToggleButton table = loaded.controller().tileOf("Table");
      assertNotNull(table);
      table.setSelected(true);
      ok.fire();
    });

    assertTrue(loaded.controller().isConfirmed());
    assertEquals("Table", loaded.controller().getSelected().type());
  }

  @Test
  void addIsOnlyEnabledWhileATileIsSelected() throws Exception {
    init(insertableIntoABox());
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");

    FxTestSupport.onFx(() -> loaded.controller().tileOf("Box").setSelected(true));
    assertFalse(ok.isDisabled());
    assertEquals("Box", loaded.controller().getSelected().type());

    FxTestSupport.onFx(() -> loaded.controller().tileOf("Box").setSelected(false));
    assertTrue(ok.isDisabled());
    assertNull(loaded.controller().getSelected());
  }

  @Test
  void cancellingSelectsNothing() throws Exception {
    init(insertableIntoABox());

    FxTestSupport.onFx(() -> loaded.controller().onDialogCancel());

    assertFalse(loaded.controller().isConfirmed());
  }

  @Test
  void withNothingToOfferTheDialogSaysSoAndCannotBeConfirmed() throws Exception {
    init(List.of());

    Label empty = FxTestSupport.field(loaded.controller(), "emptyLabel");
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");
    assertTrue(empty.isVisible());
    assertTrue(ok.isDisabled());
    assertNull(loaded.controller().getSelected());
  }
}
