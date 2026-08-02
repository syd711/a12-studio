package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.InitialActivity;
import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edits {@link InitialActivity#getDescriptor()}. Same row-based Name/Value layout as {@link
 * AnnotationsPanelController}, except the name column offers a fixed set of descriptor keys instead of
 * suggestions sourced from a registry. Not bound to a single Element (the descriptor lives on either the
 * model's {@link ApplicationModelContent} via {@link #setModel}, or a {@link Module}'s {@link Menu} via
 * {@link #setModule}; the two are mutually exclusive), so it follows the model-header pattern used by e.g.
 * {@link TimezonePanelController}.
 */
public class ActivityPanelController extends AbstractPropertyEditor {

  private static final List<String> DESCRIPTOR_KEYS = List.of("instance", "model", "module", "engine", "menuEnty");

  @FXML
  private GridPane descriptorGrid;

  @FXML
  private CheckBox skipDataLoadingCheckbox;

  private ApplicationModel model;

  private Module module;

  private final List<DescriptorEntry> entries = new ArrayList<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindCheckBox(skipDataLoadingCheckbox, (el, value) -> {
      if (model != null || module != null) {
        getOrCreateInitialActivity().setWithoutData(value ? true : null);
      }
    });
  }

  public void setModel(@NonNull ApplicationModel model) {
    this.module = null;
    this.model = model;
    rebuildRows();
    setFieldValue(skipDataLoadingCheckbox, isWithoutData());
  }

  public void setModule(@NonNull Module module) {
    this.model = null;
    this.module = module;
    rebuildRows();
    setFieldValue(skipDataLoadingCheckbox, isWithoutData());
  }

  @FXML
  private void onAdd() {
    entries.add(new DescriptorEntry("", ""));
    syncDescriptorToModel();
    rebuildRows();
    commitChange();
  }

  private void rebuildRows() {
    descriptorGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    entries.clear();
    Map<String, String> descriptor = getDescriptor();
    if (descriptor != null) {
      descriptor.forEach((key, value) -> entries.add(new DescriptorEntry(key, value)));
    }

    for (int index = 0; index < entries.size(); index++) {
      addRow(entries.get(index), index, entries.size());
    }
  }

  private void addRow(DescriptorEntry entry, int index, int rowCount) {
    ComboBox<String> nameField = new ComboBox<>();
    nameField.setId("activityName-" + index);
    nameField.setEditable(true);
    nameField.setMaxWidth(Double.MAX_VALUE);
    nameField.getItems().setAll(DESCRIPTOR_KEYS);
    setFieldValue(nameField, entry.key);

    TextField valueField = new TextField();
    valueField.setId("activityValue-" + index);
    valueField.setMaxWidth(Double.MAX_VALUE);
    setFieldValue(valueField, entry.value);

    bindTextField(valueField, (el, value) -> {
      entry.value = value;
      syncDescriptorToModel();
    });
    bindComboBox(nameField, (el, value) -> {
      entry.key = value;
      syncDescriptorToModel();
    });

    descriptorGrid.addRow(index + 1, nameField, valueField, createActionsBox(entry, index, rowCount));
  }

  private HBox createActionsBox(DescriptorEntry entry, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button copyButton = RowFactory.createActionButton(Icons.COPY, "Copy", () -> {
      entries.add(entries.indexOf(entry) + 1, new DescriptorEntry(entry.key, entry.value));
      syncDescriptorToModel();
      rebuildRows();
      commitChange();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this entry?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        entries.remove(entry);
        syncDescriptorToModel();
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(entries, fromIndex, toIndex);
    syncDescriptorToModel();
    rebuildRows();
    commitChange();
  }

  private Map<String, String> getDescriptor() {
    InitialActivity initialActivity = getInitialActivity();
    return initialActivity != null ? initialActivity.getDescriptor() : null;
  }

  private boolean isWithoutData() {
    InitialActivity initialActivity = getInitialActivity();
    return initialActivity != null && Boolean.TRUE.equals(initialActivity.getWithoutData());
  }

  private InitialActivity getInitialActivity() {
    if (module != null) {
      Menu menu = module.getMenu();
      return menu != null ? menu.getInitialActivity() : null;
    }
    if (model == null || model.getContent() == null) {
      return null;
    }
    return model.getContent().getInitialActivity();
  }

  private void syncDescriptorToModel() {
    if (model == null && module == null) {
      return;
    }
    Map<String, String> descriptor = getOrCreateInitialActivity().getDescriptor();
    descriptor.clear();
    for (DescriptorEntry entry : entries) {
      descriptor.put(entry.key, entry.value);
    }
  }

  private InitialActivity getOrCreateInitialActivity() {
    if (module != null) {
      return getOrCreateInitialActivity(module.getOrCreateMenu());
    }

    ApplicationModelContent content = model.getContent();
    if (content == null) {
      content = new ApplicationModelContent();
      model.setContent(content);
    }
    return getOrCreateInitialActivity(content);
  }

  private static InitialActivity getOrCreateInitialActivity(Menu menu) {
    InitialActivity initialActivity = menu.getInitialActivity();
    if (initialActivity == null) {
      initialActivity = new InitialActivity();
      menu.setInitialActivity(initialActivity);
    }
    return initialActivity;
  }

  private static InitialActivity getOrCreateInitialActivity(ApplicationModelContent content) {
    InitialActivity initialActivity = content.getInitialActivity();
    if (initialActivity == null) {
      initialActivity = new InitialActivity();
      content.setInitialActivity(initialActivity);
    }
    return initialActivity;
  }

  private static final class DescriptorEntry {

    private String key;
    private String value;

    private DescriptorEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
