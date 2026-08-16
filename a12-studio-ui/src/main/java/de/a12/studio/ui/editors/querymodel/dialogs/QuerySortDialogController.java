package de.a12.studio.ui.editors.querymodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.QuerySortBy;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.querymodel.QueryTraversalOption;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Add/edit dialog for a single {@link QuerySort}, opened from {@link
 * de.a12.studio.ui.editors.querymodel.QuerySortingPanelController} by clicking a sort row, its Edit button, or
 * the Add Sort Entry button. A standalone top-level dialog like {@link QueryFilterDefinitionDialogController}
 * (not one of the embedded-panel dialogs that defer saving to their caller), so {@link #onDialogSubmit} persists
 * itself; a {@link QuerySortSnapshot} taken before showing the dialog undoes live edits on Cancel.
 */
public class QuerySortDialogController implements DialogController {

  @FXML
  private ComboBox<QueryTraversalOption> traversalCombo;
  @FXML
  private TextField fieldField;
  @FXML
  private ComboBox<String> directionCombo;
  @FXML
  private ComboBox<String> nullHandlingCombo;
  @FXML
  private CheckBox ignoreCaseField;

  @FXML
  private Button okButton;
  @FXML
  private Button cancelButton;

  private Stage stage;
  private QuerySort sort;
  private QuerySortSnapshot snapshot;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits.
  private boolean updatingFromModel;

  @FXML
  private void initialize() {
    directionCombo.setItems(FXCollections.observableArrayList(QuerySortBy.DIRECTION_ASC, QuerySortBy.DIRECTION_DESC));
    directionCombo.setConverter(stringConverter(value -> QuerySortBy.DIRECTION_ASC.equals(value) ? StudioBundle.get("ascending") : StudioBundle.get("descending")));
    directionCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        sort.getSortBy().setDirection(newValue);
      }
    });

    nullHandlingCombo.setItems(FXCollections.observableArrayList((String) null, QuerySortBy.NULLS_FIRST, QuerySortBy.NULLS_LAST));
    nullHandlingCombo.setConverter(stringConverter(value -> {
      if (value == null) {
        return StudioBundle.get("null_handling_default");
      }
      return QuerySortBy.NULLS_FIRST.equals(value) ? StudioBundle.get("nulls_first") : StudioBundle.get("nulls_last");
    }));
    nullHandlingCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        sort.getSortBy().setNullHandling(newValue);
      }
    });

    traversalCombo.setConverter(traversalConverter());
    traversalCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      QueryTraversalOption selected = newValue != null ? newValue : QueryTraversalOption.NONE;
      sort.setRelationshipModel(selected.relationshipModel());
      sort.setTargetRole(selected.targetRole());
    });

    fieldField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        sort.getSortBy().setField(blankToNull(newValue));
      }
      validate();
    });
    ignoreCaseField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        sort.getSortBy().setIgnoreCase(newValue ? Boolean.TRUE : null);
      }
    });
  }

  void init(@NonNull Stage stage, @NonNull ProjectItem projectItem, @NonNull QuerySort sort) {
    this.stage = stage;
    this.sort = sort;
    this.snapshot = new QuerySortSnapshot(sort);

    List<QueryTraversalOption> traversalOptions = QueryTraversalOption.options(projectItem);
    QueryTraversalOption current = new QueryTraversalOption(sort.getRelationshipModel(), sort.getTargetRole());
    if (!traversalOptions.contains(current)) {
      traversalOptions = new ArrayList<>(traversalOptions);
      traversalOptions.add(current);
    }
    traversalCombo.setItems(FXCollections.observableArrayList(traversalOptions));

    updatingFromModel = true;
    try {
      traversalCombo.setValue(current);
      fieldField.setText(sort.getSortBy().getField());
      directionCombo.setValue(sort.getSortBy().getDirection() != null ? sort.getSortBy().getDirection() : QuerySortBy.DIRECTION_ASC);
      nullHandlingCombo.setValue(sort.getSortBy().getNullHandling());
      ignoreCaseField.setSelected(Boolean.TRUE.equals(sort.getSortBy().getIgnoreCase()));
    }
    finally {
      updatingFromModel = false;
    }

    validate();
  }

  @Override
  public void onDialogCancel() {
    snapshot.restore();
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      projectItem.save();
      StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    }
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  private void validate() {
    okButton.setDisable(fieldField.getText() == null || fieldField.getText().isBlank());
  }

  private static StringConverter<QueryTraversalOption> traversalConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(QueryTraversalOption value) {
        return value == null ? QueryTraversalOption.NONE.display() : value.display();
      }

      @Override
      public QueryTraversalOption fromString(String string) {
        return null;
      }
    };
  }

  private static StringConverter<String> stringConverter(Function<String, String> display) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return display.apply(value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
