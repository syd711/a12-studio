package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits the "Hide Condition" property of a form node (Section, Row, ControlGrid, Screen):
 * a pair of fields that together specify when the node should be hidden.
 * <ul>
 *   <li>{@code hideConditionField} — the id of a boolean field from the linked Document Model,
 *       offered as a combo-box populated from that model's boolean fields.</li>
 *   <li>{@code hideConditionValue} — {@code "true"} (hide when the field is true) or
 *       {@code null} / "no value" (hide when the field has no value), offered as a fixed
 *       two-item combo-box.</li>
 * </ul>
 * <p>
 * Does not extend {@link de.a12.studio.ui.editors.AbstractPropertyEditor} because it edits a
 * form-tree node (Section / Row / ControlGrid / Screen) rather than a document-model {@link Element}.
 * Saves are performed directly via {@link Studio#getSelectedProjectItem()} and
 * {@link StudioEventManager#fireModelSavedEvent}, matching the pattern used by
 * non-element panels such as {@link de.a12.studio.ui.editors.formmodel.StylesPanelController}.
 * <p>
 * Call {@link #configure} once per node selection to bind this panel to the node's getters/setters
 * and populate the field combo from the linked Document Model's boolean fields.
 */
public class HideConditionPanelController implements Initializable {

  /** Displayed in the value combo to represent a stored {@code null} condition value. */
  public static final String DISPLAY_NO_VALUE = "no value";
  public static final String DISPLAY_TRUE = "true";

  @FXML
  private ComboBox<String> fieldCombo;

  @FXML
  private ComboBox<String> valueCombo;

  // Guards programmatic repopulation in configure() from being treated as user edits.
  private boolean updatingFromModel;

  // Getter/setter pair for hideConditionField on the currently bound form node.
  private Supplier<String> fieldGetter;
  private Consumer<String> fieldSetter;

  // Getter/setter pair for hideConditionValue on the currently bound form node.
  private Supplier<String> valueGetter;
  private Consumer<String> valueSetter;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    valueCombo.getItems().setAll(DISPLAY_TRUE, DISPLAY_NO_VALUE);

    // Wire fieldCombo: a user selection writes through to the model and saves.
    // Clearing the field also clears the value, since a value without a field is meaningless.
    fieldCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel || fieldSetter == null) {
        return;
      }
      String fieldValue = (newVal == null || newVal.isBlank()) ? null : newVal;
      fieldSetter.accept(fieldValue);
      if (fieldValue == null) {
        // Clear value silently so its listener does not fire an extra save.
        updatingFromModel = true;
        try {
          valueCombo.setValue(null);
        } finally {
          updatingFromModel = false;
        }
        if (valueSetter != null) {
          valueSetter.accept(null);
        }
      }
      commitChange();
    });

    // Wire valueCombo: "no value" display → null stored; "true" display → "true" stored.
    valueCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel || valueSetter == null) {
        return;
      }
      valueSetter.accept(DISPLAY_NO_VALUE.equals(newVal) ? null : newVal);
      commitChange();
    });
  }

  /**
   * Binds this panel to the hide-condition properties of a form node and repopulates both combo
   * boxes from the given node and Document Model. Must be called every time a new node is
   * selected in the form tree.
   *
   * @param fieldGetter   reads {@code hideConditionField} from the node
   * @param fieldSetter   writes {@code hideConditionField} to the node
   * @param valueGetter   reads {@code hideConditionValue} from the node
   * @param valueSetter   writes {@code hideConditionValue} to the node
   * @param documentModel the Document Model linked to the form, or {@code null} if none is linked
   */
  public void configure(
      @NonNull Supplier<String> fieldGetter,
      @NonNull Consumer<String> fieldSetter,
      @NonNull Supplier<String> valueGetter,
      @NonNull Consumer<String> valueSetter,
      @Nullable DocumentModel documentModel) {

    this.fieldGetter = fieldGetter;
    this.fieldSetter = fieldSetter;
    this.valueGetter = valueGetter;
    this.valueSetter = valueSetter;

    List<String> booleanFieldIds = collectBooleanFieldIds(documentModel);

    updatingFromModel = true;
    try {
      fieldCombo.getItems().setAll(booleanFieldIds);
      fieldCombo.setValue(fieldGetter.get());
      String storedValue = valueGetter.get();
      // Stored null → no selection; stored "true" → display "true".
      // Any other stored value is also shown as DISPLAY_NO_VALUE for graceful degradation.
      valueCombo.setValue(storedValue == null ? null
          : (storedValue.equals("true") ? DISPLAY_TRUE : DISPLAY_NO_VALUE));
    } finally {
      updatingFromModel = false;
    }
  }

  private void commitChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  // Collects the ids of all boolean fields reachable from the document model's root groups.
  private static List<String> collectBooleanFieldIds(@Nullable DocumentModel documentModel) {
    if (documentModel == null
        || documentModel.getContent() == null
        || documentModel.getContent().getModelRoot() == null) {
      return List.of();
    }
    List<String> ids = new ArrayList<>();
    for (GroupElement group : documentModel.getContent().getModelRoot().getRootGroups()) {
      collectBooleanFields(group, ids);
    }
    return ids;
  }

  private static void collectBooleanFields(@NonNull Element element, @NonNull List<String> ids) {
    if (element instanceof FieldElement field
        && field.getField() != null
        && field.getField().getFieldType() instanceof BooleanFieldType) {
      ids.add(element.getId());
    }
    if (element instanceof GroupElement group && group.getGroup() != null) {
      for (Element child : group.getGroup().getElements()) {
        collectBooleanFields(child, ids);
      }
    }
  }
}
