package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.OverviewModelContent;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Gap 4 of the "Overview Model: gap review" - selecting a Query Model as the Overview Reference must write
 * only the {@code query-model-for-overview} header reference, not a redundant {@code
 * document-model-for-overview} one alongside it (SME, and all real Query-bound overview fixtures, write only
 * the former). */
class OverviewReferencePanelControllerTest {

  private static final String FXML = "/de/a12/studio/ui/editors/overviewmodel/overview-reference-panel.fxml";

  @Test
  void selectingAQueryModelWritesOnlyTheQueryModelReference() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    FxTestSupport.Loaded<OverviewReferencePanelController> loaded = FxTestSupport.load(FXML);
    OverviewReferencePanelController controller = loaded.controller();

    OverviewModel model = new OverviewModel();
    model.setId("Team_Ov");
    model.setContent(new OverviewModelContent());

    DocumentModel documentModel = new DocumentModel();
    documentModel.setId("Team_Dc");
    documentModel.setContent(new DocumentModelContent());

    QueryModel queryModel = new QueryModel();
    queryModel.setId("Team_Qe");
    QueryModelContent queryModelContent = new QueryModelContent();
    queryModelContent.setTargetDocumentModel("Team_Dc");
    queryModel.setContent(queryModelContent);

    FxTestSupport.onFx(() -> controller.load(model, List.of(documentModel), List.of(queryModel)));

    RadioButton queryModelReferenceField = FxTestSupport.field(controller, "queryModelReferenceField");
    ComboBox<String> overviewReferenceField = FxTestSupport.field(controller, "overviewReferenceField");

    FxTestSupport.onFx(() -> queryModelReferenceField.setSelected(true));
    FxTestSupport.onFx(() -> overviewReferenceField.setValue("Team_Qe"));

    List<ModelReference> references = model.getModelReferences();
    assertEquals(1, references.size(), "only the query-model-for-overview reference should be written");
    assertEquals(ModelReference.PURPOSE_QUERY_MODEL_FOR_OVERVIEW, references.get(0).getPurpose());
    assertEquals("Team_Qe", references.get(0).getReference());
    assertFalse(references.stream().anyMatch(reference -> ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW.equals(reference.getPurpose())),
        "no document-model-for-overview reference should be added alongside it");

    // Switching back to Document Model mode drops the query reference and writes a plain document one.
    RadioButton documentModelReferenceField = FxTestSupport.field(controller, "documentModelReferenceField");
    FxTestSupport.onFx(() -> documentModelReferenceField.setSelected(true));
    FxTestSupport.onFx(() -> overviewReferenceField.setValue("Team_Dc"));

    assertEquals(1, model.getModelReferences().size());
    assertEquals(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW, model.getModelReferences().get(0).getPurpose());
    assertTrue(model.getModelReferences().stream().noneMatch(reference -> ModelReference.PURPOSE_QUERY_MODEL_FOR_OVERVIEW.equals(reference.getPurpose())));
  }
}
