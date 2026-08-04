package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnRef;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.OverviewColumnOptions;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits an {@link OverviewModel}'s {@code content.configuration.screenReaderColumn}: a single combo box
 * picking one of the columns defined in the Columns panel, whose content is announced to screen readers for
 * each row. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the
 * model-header pattern used by e.g. {@link OverviewSortingPanelController}. Unlike that panel's dangling
 * references (left in place and flagged by a validator), {@link #refresh()} actively clears the selection
 * once its referenced column is deleted, since a single "the" screen-reader column silently pointing at
 * nothing would leave the table with no accessible column at all rather than merely one invalid row.
 */
public class OverviewAccessibilityPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> screenReaderColumnField;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  // Set while screenReaderColumnField is being repopulated from the model, so those programmatic updates
  // aren't mistaken for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    screenReaderColumnField.setPromptText(StudioBundle.get("select_a_column"));
    screenReaderColumnField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      setScreenReaderColumnId(newValue);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    populate();
  }

  /** Re-points the picker's column summaries at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex) {
    this.documentModelIndex = documentModelIndex;
    populate();
  }

  /** Called by the owning editor whenever the Columns panel changes: clears the selection if it referenced a
   * column that was just deleted, then re-syncs the picker's choices with the current column list. */
  public void refresh() {
    if (model == null) {
      return;
    }
    String currentId = getScreenReaderColumnId();
    if (currentId != null && getColumns().stream().noneMatch(column -> currentId.equals(column.getId()))) {
      setScreenReaderColumnId(null);
      commitHeaderChange();
    }
    populate();
  }

  private void populate() {
    if (model == null) {
      return;
    }
    List<Column> columns = getColumns();
    OverviewColumnOptions.applyColumnConverter(screenReaderColumnField, columns, documentModelIndex);

    updatingFromModel = true;
    try {
      screenReaderColumnField.getItems().setAll(OverviewColumnOptions.columnIds(columns));
      screenReaderColumnField.setValue(getScreenReaderColumnId());
    }
    finally {
      updatingFromModel = false;
    }
  }

  private List<Column> getColumns() {
    return model.getContent().getColumns();
  }

  private String getScreenReaderColumnId() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    return configuration != null && configuration.getScreenReaderColumn() != null
        ? configuration.getScreenReaderColumn().getIdref() : null;
  }

  private void setScreenReaderColumnId(String columnId) {
    if (columnId == null) {
      ensureConfiguration().setScreenReaderColumn(null);
      return;
    }
    ColumnRef ref = new ColumnRef();
    ref.setIdref(columnId);
    ensureConfiguration().setScreenReaderColumn(ref);
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
