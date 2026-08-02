package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.application.ApplicationUniqueNamesValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Edits {@link ApplicationModelContent#getModules()}: a list of module names, each reorderable (move up/down),
 * copyable and deletable, with a full editor (name, menu label, roles) opened inline via {@link #onEditModule}
 * (see {@link #setOnEditModule}) when a row's "Edit" button is pressed or a row is double-clicked. Not bound to
 * a single Element (modules live on the model's content), so it follows the model-header pattern used by e.g.
 * {@link ActivityPanelController}.
 */
public class ModulesPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getModules().
  private static final DataFormat MODULE_INDEX = new DataFormat("application/x-a12-module-index");

  @FXML
  private VBox modulesList;

  private ApplicationModel model;

  // Notified with the module to open in the inline editor, e.g. by ApplicationModelEditorController to show
  // it in its editorContainer. Set via setOnEditModule once this panel is loaded from FXML.
  private Consumer<Module> onEditModule;

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    rebuildRows();
  }

  public void setOnEditModule(@NonNull Consumer<Module> onEditModule) {
    this.onEditModule = onEditModule;
  }

  @FXML
  private void onAdd() {
    Dialogs.showModuleForAdd(Studio.stage).ifPresent(name -> {
      Module module = new Module();
      module.setName(name);
      getModules().add(module);
      rebuildRows();
      commitChange();
    });
  }

  private List<Module> getModules() {
    return model.getContent().getModules();
  }

  private void rebuildRows() {
    refreshNameUniquenessError();
    modulesList.getChildren().clear();

    List<Module> modules = getModules();
    if (modules.isEmpty()) {
      Label emptyLabel = new Label("No modules found.");
      emptyLabel.getStyleClass().add("placeholder-label");
      modulesList.getChildren().add(emptyLabel);
      return;
    }

    for (int index = 0; index < modules.size(); index++) {
      modulesList.getChildren().add(createRow(modules.get(index), index, modules.size()));
    }
  }

  /**
   * Not bound to an {@link de.a12.studio.models.documentmodel.Element}, so the base class's element-keyed
   * validation plumbing never runs for this panel; queries {@link ApplicationUniqueNamesValidator}'s
   * dedicated module-name element id directly instead. Called from {@link #rebuildRows} (itself called by
   * every mutation here, plus {@link #setModel}), so this always reflects the list as currently shown.
   */
  private void refreshNameUniquenessError() {
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, ApplicationUniqueNamesValidator.MODULES_ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    } else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private HBox createRow(Module module, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label nameLabel = new Label(module.getName());
    nameLabel.setId("module-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    nameLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);
    nameLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        onEditModule.accept(module);
      }
    });

    HBox row = new HBox(10.0, dragHandle, nameLabel, createActionsBox(module, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    row.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        onEditModule.accept(module);
      }
    });
    RowFactory.setupRowDragAndDrop(row, dragHandle, MODULE_INDEX, index, this::moveModule);
    return row;
  }

  private void moveModule(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getModules(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitChange();
    }
  }

  private HBox createActionsBox(Module module, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> onEditModule.accept(module));

    Button copyButton = RowFactory.createActionButton(Icons.COPY, "Copy", () -> {
      Module copy = new Module();
      copy.setName(module.getName());
      copy.setMenu(module.getMenu());
      copy.setFlows(new ArrayList<>(module.getFlows()));
      getModules().add(getModules().indexOf(module) + 1, copy);
      rebuildRows();
      commitChange();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this module?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getModules().remove(module);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getModules(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }
}
