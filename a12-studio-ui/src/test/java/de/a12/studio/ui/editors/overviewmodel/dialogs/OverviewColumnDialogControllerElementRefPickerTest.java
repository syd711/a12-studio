package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Gap 16 of the "Overview Model: gap review" - the Column dialog's Element Reference picker offers
 * non-repeatable fields only. */
class OverviewColumnDialogControllerElementRefPickerTest {

  private static final String FXML = "/de/a12/studio/ui/editors/overviewmodel/dialogs/overview-column-dialog.fxml";

  @Test
  void excludesARepeatableFieldButStillDisplaysOneAlreadySelected() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");

    DocumentModel documentModel = new DocumentModel();
    documentModel.setId("Person_Dc");
    DocumentModelContent content = new DocumentModelContent();
    ModelRoot modelRoot = new ModelRoot();

    GroupElement root = new GroupElement();
    root.setId("group_root");
    root.setName("Person");
    GroupConfig rootConfig = new GroupConfig();
    rootConfig.setRepeatability(1);

    FieldElement plainField = fieldElement("field_plain", "PlainField");
    GroupElement repeatableGroup = new GroupElement();
    repeatableGroup.setId("group_repeatable");
    repeatableGroup.setName("Repeatable");
    GroupConfig repeatableConfig = new GroupConfig();
    repeatableConfig.setRepeatability(999);
    FieldElement repeatableField = fieldElement("field_repeatable", "RepeatableField");
    repeatableConfig.setElements(List.of(repeatableField));
    repeatableGroup.setGroup(repeatableConfig);

    rootConfig.setElements(List.of(plainField, repeatableGroup));
    root.setGroup(rootConfig);
    modelRoot.setRootGroups(List.of(root));
    content.setModelRoot(modelRoot);
    documentModel.setContent(content);

    FxTestSupport.Loaded<OverviewColumnDialogController> loaded = FxTestSupport.load(FXML);
    OverviewColumnDialogController controller = loaded.controller();
    Stage stage = FxTestSupport.onFx(() -> new Stage());

    de.a12.studio.modelsvalidation.validators.ElementIndex index =
        OverviewElementOptions.indexOf(documentModel, List.of(documentModel));

    Column column = new Column();
    column.setId("column_1");
    column.setWidth(1.0);
    column.setElementRef("field_repeatable");
    FxTestSupport.onFx(() -> controller.init(stage, index, "Person_Dc", column, reference -> null));

    ComboBox<String> elementRefCombo = FxTestSupport.field(controller, "elementRefCombo");
    List<String> items = FxTestSupport.onFx(() -> List.copyOf(elementRefCombo.getItems()));

    assertTrue(items.contains("field_plain"));
    assertFalse(items.contains("field_repeatable"), "a repeatable field is excluded from the picker");
    assertTrue(FxTestSupport.onFx(() -> "field_repeatable".equals(elementRefCombo.getValue())),
        "the column's own already-repeatable value is still shown");
  }

  private static FieldElement fieldElement(String id, String name) {
    FieldElement fieldElement = new FieldElement();
    fieldElement.setId(id);
    fieldElement.setName(name);
    FieldConfig field = new FieldConfig();
    field.setFieldType(new StringFieldType());
    fieldElement.setField(field);
    return fieldElement;
  }
}
