package de.a12.studio.ui.editors.formmodel;

import javafx.fxml.FXML;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Loads the Form Model editor FXML files that were extended with new property editors and checks that every
 * {@code @FXML} field of the controllers (and of the controllers of everything they {@code fx:include}) was
 * actually injected. A mismatch between an {@code fx:id} and the controller field it should fill isn't a load
 * error - the field just stays {@code null} and blows up later in the running app - so a plain "did it load"
 * check wouldn't catch it. Skipped when no JavaFX toolkit can be started (headless build machine).
 */
class FormEditorFxmlLoadTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @Test
  void rowActionDialogInjectsAllFields() throws Exception {
    assertAllFieldsInjected("dialogs/row-action-dialog.fxml");
  }

  @Test
  void formButtonDialogInjectsAllFields() throws Exception {
    assertAllFieldsInjected("dialogs/form-button-dialog.fxml");
  }

  @Test
  void repeatEditorInjectsAllFields() throws Exception {
    assertAllFieldsInjected("formtree/nodeeditors/formnode-editor-repeat-panel.fxml");
  }

  @Test
  void repeatOverviewColumnEditorInjectsAllFields() throws Exception {
    assertAllFieldsInjected("formtree/nodeeditors/formnode-editor-repeat-overview-column-panel.fxml");
  }

  @Test
  void controlEditorInjectsAllFields() throws Exception {
    assertAllFieldsInjected("formtree/nodeeditors/formnode-editor-control-panel.fxml");
  }

  @Test
  void confirmControlEditorInjectsAllFields() throws Exception {
    assertAllFieldsInjected("formtree/nodeeditors/formnode-editor-confirm-control-panel.fxml");
  }

  @Test
  void screenEditorInjectsAllFields() throws Exception {
    assertAllFieldsInjected("formtree/nodeeditors/formnode-editor-screen-panel.fxml");
  }

  @Test
  void customScreenElementEditorInjectsAllFields() throws Exception {
    assertAllFieldsInjected("formtree/nodeeditors/formnode-editor-custom-screen-element-panel.fxml");
  }

  @Test
  void multiColumnSectionEditorInjectsAllFields() throws Exception {
    assertAllFieldsInjected("multi-column-section-editor-panel.fxml");
  }

  @Test
  void dataConfigurationTabInjectsAllFields() throws Exception {
    assertAllFieldsInjected("data-configuration-panel.fxml");
  }

  @Test
  void modelSettingsDialogInjectsAllFields() throws Exception {
    assertAllFieldsInjected("/de/a12/studio/ui/editors/dialogs/document-model-settings-dialog.fxml");
  }

  private void assertAllFieldsInjected(String fxml) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    Object controller = FxTestSupport.load(fxml.startsWith("/") ? fxml : "/de/a12/studio/ui/editors/formmodel/" + fxml).controller();

    List<String> missing = new ArrayList<>();
    collectMissingFields(controller, controller.getClass().getSimpleName(), missing, new ArrayList<>());
    assertEquals(List.of(), missing, "@FXML fields left null in " + fxml);
  }

  // Walks the controller graph: a field holding another controller of this application is checked as well.
  private static void collectMissingFields(Object controller, String path, List<String> missing, List<Object> visited) throws Exception {
    if (controller == null || visited.stream().anyMatch(other -> other == controller)) {
      return;
    }
    visited.add(controller);
    for (Class<?> type = controller.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
      for (Field field : type.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        field.setAccessible(true);
        Object value = field.get(controller);
        if (field.isAnnotationPresent(FXML.class) && value == null) {
          missing.add(path + "." + field.getName());
        }
        else if (value != null && value.getClass().getName().startsWith("de.a12.studio.ui.") && field.isAnnotationPresent(FXML.class)
            && value.getClass().getSimpleName().endsWith("Controller")) {
          collectMissingFields(value, path + "." + field.getName(), missing, visited);
        }
      }
    }
  }
}
