package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a {@link TreeModel}'s row height (in px) and action column width (SME "Row Height And Action Column
 * Width"), both living on {@link TreeConfiguration}. Virtual scrolling needs both, but SME keeps them editable
 * (and stored) while it is off, so neither field is tied to {@link TreeVirtualScrollingPanelController}'s
 * checkbox. Blank fields are stored as absent keys. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreeRowHeightActionColumnWidthPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField rowHeightField;

  @FXML
  private TextField actionColumnWidthField;

  @FXML
  private Label rowHeightInfoIcon;

  @FXML
  private Label actionColumnWidthInfoIcon;

  private TreeModel model;

  // Set while the fields are being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(rowHeightInfoIcon, StudioBundle.get("tree_row_height_panel.row_height_info"));
    WidgetFactory.createHelpIcon(actionColumnWidthInfoIcon,
        StudioBundle.get("specify_the_max_width_of_the_action_column_when_all_buttons_"));
    WidgetFactory.restrictToNumericInput(rowHeightField);
    WidgetFactory.restrictToDecimalInput(actionColumnWidthField);

    rowHeightField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      Integer height = null;
      if (newValue != null && !newValue.isBlank()) {
        try {
          height = Integer.valueOf(newValue);
        }
        catch (NumberFormatException e) {
          // More digits than an int holds - keep the stored value.
          return;
        }
      }
      ensureConfiguration().setRowHeight(height);
      commitHeaderChange();
    });
    actionColumnWidthField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      Double width = null;
      if (newValue != null && !newValue.isBlank()) {
        try {
          width = Double.valueOf(newValue);
        }
        catch (NumberFormatException e) {
          // Mid-typing input such as "-" or "." isn't a number yet - keep the stored value until it is.
          return;
        }
      }
      ensureConfiguration().setActionColumnWidth(width);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      TreeConfiguration configuration = model.getContent().getConfiguration();
      rowHeightField.setText(configuration != null && configuration.getRowHeight() != null
          ? String.valueOf(configuration.getRowHeight()) : "");
      actionColumnWidthField.setText(configuration != null && configuration.getActionColumnWidth() != null
          ? String.valueOf(configuration.getActionColumnWidth()) : "");
    }
    finally {
      updatingFromModel = false;
    }
  }

  private TreeConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    return model.getContent().getConfiguration();
  }
}
