package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link TreeModel}'s accessibility settings: {@link TreeConfiguration#getLabelHidden()} (SME's "Hide Label": the
 * model label is always announced to screen readers, and this only decides whether it is also shown in the tree's header;
 * written as {@code true} or omitted, never {@code false}, like SME) and {@link TreeConfiguration#getScreenReaderColumnRef()}
 * (SME's "Screen Reader Column": the column whose text identifies a row for screen readers). Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern; call {@link #refresh()} when the
 * columns changed.
 */
public class TreeAccessibilityPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox hideLabelField;

  @FXML
  private ComboBox<String> screenReaderColumnField;

  private TreeModel model;

  // Set while hideLabelField is being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    hideLabelField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (model.getContent().getConfiguration() == null) {
        model.getContent().setConfiguration(new TreeConfiguration());
      }
      model.getContent().getConfiguration().setLabelHidden(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });

    // The items are column ids (the names may repeat); the converter shows the names.
    screenReaderColumnField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String columnId) {
        return columnId == null ? StudioBundle.get("none") : columnName(columnId);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    screenReaderColumnField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (model.getContent().getConfiguration() == null) {
        model.getContent().setConfiguration(new TreeConfiguration());
      }
      model.getContent().getConfiguration().setScreenReaderColumnRef(newValue);
      commitHeaderChange();
    });
  }

  /** Re-reads the columns to choose from, e.g. after one was added, renamed or removed. */
  public void refresh() {
    if (model != null) {
      setModel(model);
    }
  }

  private String columnName(String columnId) {
    return model.getContent().getColumns().stream()
        .filter(column -> columnId.equals(column.getId()))
        .map(column -> column.getName() != null ? column.getName() : columnId)
        .findFirst()
        .orElse(columnId);
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      hideLabelField.setSelected(model.getContent().getConfiguration() != null
          && Boolean.TRUE.equals(model.getContent().getConfiguration().getLabelHidden()));

      List<String> columnIds = new ArrayList<>();
      columnIds.add(null);
      model.getContent().getColumns().forEach(column -> columnIds.add(column.getId()));
      String current = model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getScreenReaderColumnRef() : null;
      if (current != null && !columnIds.contains(current)) {
        columnIds.add(current);
      }
      screenReaderColumnField.getItems().setAll(columnIds);
      screenReaderColumnField.setValue(current);
    }
    finally {
      updatingFromModel = false;
    }
  }
}
