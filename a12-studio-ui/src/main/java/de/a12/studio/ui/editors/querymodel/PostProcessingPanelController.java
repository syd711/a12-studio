package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QueryPaging;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.QuerySortBy;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Tab 2 ("Post Processing") of the Query Model editor: Sorting (a reorderable row list, following {@code
 * OverviewSortingPanelController}'s pattern), Paging, and an Aggregation toggle - all editing {@link
 * QueryModelContent} directly, so one flat controller is simpler than three separate {@code
 * AbstractPropertyEditor} panels (none of the three sections is reused anywhere else).
 */
public class PostProcessingPanelController implements Initializable {

  private static final DataFormat SORT_INDEX = new DataFormat("application/x-a12-query-sort-index");

  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;

  @FXML
  private HBox sortingHeaderRow;
  @FXML
  private VBox sortingRows;
  @FXML
  private Label sortingEmptyLabel;

  @FXML
  private Spinner<Integer> pageNumberField;
  @FXML
  private Spinner<Integer> pageSizeField;

  @FXML
  private CheckBox aggregateResultsField;

  private ProjectItem projectItem;
  private QueryModel model;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger a save.
  private boolean updatingFromModel;

  private record TraversalOption(String relationshipModel, String targetRole) {
    static final TraversalOption NONE = new TraversalOption(null, null);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    pageNumberField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, DEFAULT_PAGE_NUMBER));
    WidgetFactory.restrictToNumericInput(pageNumberField.getEditor());
    pageSizeField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_PAGE_SIZE));
    WidgetFactory.restrictToNumericInput(pageSizeField.getEditor());

    pageNumberField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      ensurePaging().setPageNumber(newValue);
      commitChange();
    });
    pageSizeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      ensurePaging().setPageSize(newValue);
      commitChange();
    });

    aggregateResultsField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      content().setAggregateResults(newValue ? Boolean.TRUE : null);
      commitChange();
    });
  }

  public void load(@NonNull ProjectItem projectItem, @NonNull QueryModel model) {
    this.projectItem = projectItem;
    this.model = model;

    updatingFromModel = true;
    try {
      QueryPaging paging = content().getPaging();
      pageNumberField.getValueFactory().setValue(paging != null && paging.getPageNumber() != null ? paging.getPageNumber() : DEFAULT_PAGE_NUMBER);
      pageSizeField.getValueFactory().setValue(paging != null && paging.getPageSize() != null ? paging.getPageSize() : DEFAULT_PAGE_SIZE);
      aggregateResultsField.setSelected(Boolean.TRUE.equals(content().getAggregateResults()));
    }
    finally {
      updatingFromModel = false;
    }

    rebuildSortingRows();
  }

  private QueryModelContent content() {
    return model.getContent();
  }

  private QueryPaging ensurePaging() {
    if (content().getPaging() == null) {
      content().setPaging(new QueryPaging());
    }
    return content().getPaging();
  }

  private List<QuerySort> getSort() {
    return content().getSort();
  }

  @FXML
  private void onAddSort() {
    QuerySort sort = new QuerySort();
    sort.getSortBy().setDirection(QuerySortBy.DIRECTION_ASC);
    getSort().add(sort);
    rebuildSortingRows();
    commitChange();
  }

  private void rebuildSortingRows() {
    sortingRows.getChildren().clear();

    List<QuerySort> sort = getSort();
    boolean empty = sort.isEmpty();
    sortingHeaderRow.setVisible(!empty);
    sortingHeaderRow.setManaged(!empty);
    sortingEmptyLabel.setVisible(empty);
    sortingEmptyLabel.setManaged(empty);

    List<TraversalOption> traversalOptions = relationshipTraversalOptions();
    for (int index = 0; index < sort.size(); index++) {
      sortingRows.getChildren().add(createSortRow(sort.get(index), index, sort.size(), traversalOptions));
    }
  }

  /** Every relationship a sort entry can traverse before reaching the field it sorts by: {@link
   * TraversalOption#NONE} (sort a field on the target Document Model directly), plus one option per {@code
   * <RelationshipModel, role>} pair found across every {@link RelationshipModel} in the project. */
  private List<TraversalOption> relationshipTraversalOptions() {
    List<TraversalOption> options = new ArrayList<>();
    options.add(TraversalOption.NONE);
    for (A12Model<?> candidate : ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIP)) {
      if (candidate instanceof RelationshipModel relationshipModel && relationshipModel.getContent() != null) {
        for (EntityCharacteristic characteristic : relationshipModel.getContent().getEntityCharacteristics()) {
          if (characteristic.getRole() != null) {
            options.add(new TraversalOption(relationshipModel.getId(), characteristic.getRole()));
          }
        }
      }
    }
    return options;
  }

  private HBox createSortRow(@NonNull QuerySort sort, int index, int rowCount, @NonNull List<TraversalOption> traversalOptions) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    ComboBox<TraversalOption> traversalCombo = createTraversalCombo(sort, traversalOptions);
    traversalCombo.setPrefWidth(220.0);
    HBox.setHgrow(traversalCombo, Priority.ALWAYS);

    TextField fieldField = new TextField(sort.getSortBy().getField());
    fieldField.setPrefWidth(200.0);
    fieldField.setPromptText(StudioBundle.get("field"));
    fieldField.textProperty().addListener((observable, oldValue, newValue) -> {
      sort.getSortBy().setField(blankToNull(newValue));
      commitChange();
    });

    ComboBox<String> directionCombo = new ComboBox<>();
    directionCombo.setPrefWidth(110.0);
    directionCombo.getItems().setAll(QuerySortBy.DIRECTION_ASC, QuerySortBy.DIRECTION_DESC);
    directionCombo.setConverter(stringDisplayConverter(value -> QuerySortBy.DIRECTION_ASC.equals(value) ? StudioBundle.get("ascending") : StudioBundle.get("descending")));
    directionCombo.setValue(sort.getSortBy().getDirection() != null ? sort.getSortBy().getDirection() : QuerySortBy.DIRECTION_ASC);
    directionCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      sort.getSortBy().setDirection(newValue);
      commitChange();
    });

    ComboBox<String> nullHandlingCombo = new ComboBox<>();
    nullHandlingCombo.setPrefWidth(140.0);
    nullHandlingCombo.getItems().setAll(null, QuerySortBy.NULLS_FIRST, QuerySortBy.NULLS_LAST);
    nullHandlingCombo.setConverter(stringDisplayConverter(value -> {
      if (value == null) {
        return StudioBundle.get("null_handling_default");
      }
      return QuerySortBy.NULLS_FIRST.equals(value) ? StudioBundle.get("nulls_first") : StudioBundle.get("nulls_last");
    }));
    nullHandlingCombo.setValue(sort.getSortBy().getNullHandling());
    nullHandlingCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      sort.getSortBy().setNullHandling(newValue);
      commitChange();
    });

    CheckBox ignoreCaseCheckbox = new CheckBox();
    ignoreCaseCheckbox.setPrefWidth(90.0);
    ignoreCaseCheckbox.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("ignore_case")));
    ignoreCaseCheckbox.setSelected(Boolean.TRUE.equals(sort.getSortBy().getIgnoreCase()));
    ignoreCaseCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      sort.getSortBy().setIgnoreCase(newValue ? Boolean.TRUE : null);
      commitChange();
    });

    HBox row = new HBox(10.0, dragHandle, traversalCombo, fieldField, directionCombo, ignoreCaseCheckbox, nullHandlingCombo, createSortActionsBox(sort, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, SORT_INDEX, index, this::moveSort);
    return row;
  }

  private ComboBox<TraversalOption> createTraversalCombo(@NonNull QuerySort sort, @NonNull List<TraversalOption> traversalOptions) {
    ComboBox<TraversalOption> combo = new ComboBox<>();
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.setConverter(displayConverter(this::displayTraversal));

    TraversalOption current = new TraversalOption(sort.getRelationshipModel(), sort.getTargetRole());
    List<TraversalOption> items = new ArrayList<>(traversalOptions);
    boolean unresolved = !items.contains(current);
    if (unresolved) {
      items.add(current);
    }
    combo.getItems().setAll(items);
    combo.setValue(current);
    if (unresolved) {
      combo.getStyleClass().add("validation-error");
      combo.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("relationship_could_not_be_resolved")));
    }

    combo.valueProperty().addListener((observable, oldValue, newValue) -> {
      TraversalOption selected = newValue != null ? newValue : TraversalOption.NONE;
      sort.setRelationshipModel(selected.relationshipModel());
      sort.setTargetRole(selected.targetRole());
      combo.getStyleClass().remove("validation-error");
      combo.setTooltip(null);
      commitChange();
    });
    return combo;
  }

  private String displayTraversal(TraversalOption option) {
    if (option == null || option.equals(TraversalOption.NONE)) {
      return StudioBundle.get("sort_directly_on_target_document_model");
    }
    return option.relationshipModel() + " → " + option.targetRole();
  }

  private HBox createSortActionsBox(@NonNull QuerySort sort, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveSortRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_sorting_entry"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getSort().remove(sort);
        rebuildSortingRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveSort(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getSort(), fromIndex, insertBeforeIndex)) {
      rebuildSortingRows();
      commitChange();
    }
  }

  private void moveSortRow(int fromIndex, int toIndex) {
    Collections.swap(getSort(), fromIndex, toIndex);
    rebuildSortingRows();
    commitChange();
  }

  private void commitChange() {
    if (updatingFromModel) {
      return;
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  private static StringConverter<TraversalOption> displayConverter(java.util.function.Function<TraversalOption, String> display) {
    return new StringConverter<>() {
      @Override
      public String toString(TraversalOption value) {
        return display.apply(value);
      }

      @Override
      public TraversalOption fromString(String string) {
        return null;
      }
    };
  }

  private static StringConverter<String> stringDisplayConverter(java.util.function.Function<String, String> display) {
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
