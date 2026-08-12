package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.SceneChange;
import de.a12.studio.models.applicationmodel.ViewAddDirective;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.applicationmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits a {@link SceneChange}'s {@code onEnter} and {@code onExit} {@link Directive} lists: each is a
 * non-inline-editable, reorderable (drag handle plus move up/down) row list (row = Type/Region/Name summary),
 * matching the SME reference's "On Enter"/"On Exit" tables. Same row-based layout as {@link
 * SubregionsPanelController}. Reused by both {@link de.a12.studio.ui.editors.applicationmodel.dialogs.SceneDialogController}
 * (where both lists apply) and {@link de.a12.studio.ui.editors.applicationmodel.dialogs.CaseDialogController}
 * (where only {@code onEnter} applies, see {@link de.a12.studio.models.applicationmodel.SceneChange}'s own
 * {@code onExit} javadoc), via {@link #bind}. Isn't wired through {@link
 * de.a12.studio.ui.editors.AbstractPropertyEditor} for the same reason as {@link
 * MatchConditionsPanelController}.
 */
public class SceneChangePanelController {

  // Identifies a row-reorder drag; separate formats for onEnter/onExit so a drag started in one list can't be
  // dropped into the other (both lists otherwise carry the same "index into the source list" payload shape).
  private static final DataFormat ON_ENTER_DIRECTIVE_INDEX = new DataFormat("application/x-a12-scene-change-on-enter-index");
  private static final DataFormat ON_EXIT_DIRECTIVE_INDEX = new DataFormat("application/x-a12-scene-change-on-exit-index");

  @FXML
  private HBox onEnterColumnHeaders;

  @FXML
  private VBox onEnterRows;

  @FXML
  private Label onEnterEmptyLabel;

  @FXML
  private VBox onExitSection;

  @FXML
  private HBox onExitColumnHeaders;

  @FXML
  private VBox onExitRows;

  @FXML
  private Label onExitEmptyLabel;

  private Supplier<SceneChange> getter;

  private Consumer<SceneChange> setter;

  public void bind(@NonNull Supplier<SceneChange> getter, @NonNull Consumer<SceneChange> setter, boolean showOnExit) {
    this.getter = getter;
    this.setter = setter;
    onExitSection.setVisible(showOnExit);
    onExitSection.setManaged(showOnExit);
    rebuildAll();
  }

  @FXML
  private void onAddOnEnter() {
    addDirective(getOrCreateOnEnter());
  }

  @FXML
  private void onAddOnExit() {
    addDirective(getOrCreateOnExit());
  }

  private void addDirective(List<Directive> directives) {
    Dialogs.showDirectiveForAdd(Studio.stage).ifPresent(directive -> {
      directives.add(directive);
      rebuildAll();
    });
  }

  private List<Directive> getOnEnter() {
    SceneChange sceneChange = getter.get();
    return sceneChange != null ? sceneChange.getOnEnter() : List.of();
  }

  private List<Directive> getOnExit() {
    SceneChange sceneChange = getter.get();
    return sceneChange != null ? sceneChange.getOnExit() : List.of();
  }

  private List<Directive> getOrCreateOnEnter() {
    return getOrCreateSceneChange().getOnEnter();
  }

  private List<Directive> getOrCreateOnExit() {
    return getOrCreateSceneChange().getOnExit();
  }

  private SceneChange getOrCreateSceneChange() {
    SceneChange sceneChange = getter.get();
    if (sceneChange == null) {
      sceneChange = new SceneChange();
      setter.accept(sceneChange);
    }
    return sceneChange;
  }

  private void rebuildAll() {
    rebuildRows(onEnterColumnHeaders, onEnterRows, onEnterEmptyLabel, getOnEnter(), ON_ENTER_DIRECTIVE_INDEX);
    if (onExitSection.isVisible()) {
      rebuildRows(onExitColumnHeaders, onExitRows, onExitEmptyLabel, getOnExit(), ON_EXIT_DIRECTIVE_INDEX);
    }
  }

  private void rebuildRows(HBox columnHeaders, VBox rows, Label emptyLabel, List<Directive> directives, DataFormat indexFormat) {
    rows.getChildren().clear();

    boolean empty = directives.isEmpty();
    columnHeaders.setVisible(!empty);
    columnHeaders.setManaged(!empty);
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < directives.size(); index++) {
      rows.getChildren().add(createRow(directives, directives.get(index), index, indexFormat));
    }
  }

  private HBox createRow(List<Directive> directives, Directive directive, int index, DataFormat indexFormat) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label typeLabel = new Label(directive.getType() != null ? directive.getType().getValue() : "");
    typeLabel.setPrefWidth(110.0);
    typeLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        editDirective(directives, directive);
      }
    });

    Label regionLabel = new Label(String.join(", ", directive.getRegion()));
    regionLabel.setMaxWidth(Double.MAX_VALUE);
    regionLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(regionLabel, Priority.ALWAYS);
    regionLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        editDirective(directives, directive);
      }
    });

    Label nameLabel = new Label(directive instanceof ViewAddDirective viewAdd && viewAdd.getName() != null ? viewAdd.getName() : "");
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    nameLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);
    nameLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        editDirective(directives, directive);
      }
    });

    HBox row = new HBox(10.0, dragHandle, typeLabel, regionLabel, nameLabel, createActionsBox(directives, directive, index));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, indexFormat, index, (fromIndex, insertBeforeIndex) -> moveDirective(directives, fromIndex, insertBeforeIndex));
    return row;
  }

  private void moveDirective(List<Directive> directives, int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(directives, fromIndex, insertBeforeIndex)) {
      rebuildAll();
    }
  }

  private void editDirective(List<Directive> directives, Directive directive) {
    Dialogs.showDirectiveForEdit(Studio.stage, directive).ifPresent(updated -> {
      directives.set(directives.indexOf(directive), updated);
      rebuildAll();
    });
  }

  private HBox createActionsBox(List<Directive> directives, Directive directive, int index) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, directives.size(), (fromIndex, toIndex) -> moveRow(directives, fromIndex, toIndex));

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> editDirective(directives, directive));

    Button copyButton = RowFactory.createActionButton(Icons.COPY, StudioBundle.get("duplicate"), () -> {
      directives.add(index + 1, cloneDirective(directive));
      rebuildAll();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_directive"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        directives.remove(directive);
        rebuildAll();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(List<Directive> directives, int fromIndex, int toIndex) {
    Collections.swap(directives, fromIndex, toIndex);
    rebuildAll();
  }

  private static Directive cloneDirective(@NonNull Directive directive) {
    String json = JsonSettings.objectMapper.writeValueAsString(directive);
    return JsonSettings.objectMapper.readValue(json, Directive.class);
  }
}
