package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryAggregation;
import de.a12.studio.models.querymodel.QueryAggregationEntry;
import de.a12.studio.models.querymodel.QueryAggregationGroup;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.query.QueryAggregationSupport;
import de.a12.studio.modelsvalidation.validators.query.QueryAggregationValidator;
import de.a12.studio.modelsvalidation.validators.query.QueryElementResolution;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.components.ErrorContainerController;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Edits a {@link QueryModel}'s {@code content.aggregation} on the Post Processing tab: the "Aggregate Results"
 * switch, the fields to group by, and the aggregations (function + field + optional alias) computed per group -
 * SME's {@code QMPostProcessingForm} "Aggregation" section, see docs/sme-reference-comparison.md "Query Model".
 * Not bound to a single {@link Element}, so it follows the model-header pattern of {@link
 * QuerySortingPanelController}: manual listeners, {@link #commitHeaderChange()}.
 *
 * <p>The switch <em>is</em> the presence of {@link QueryModelContent#getAggregation()}, as in SME (whose {@code
 * technical_useAggregation} is editor-only). Like SME, switching it off keeps the configuration for the rest of the
 * editing session, so switching it on again restores it instead of starting empty; only the switched-on state is
 * ever written to the file.
 *
 * <p>The field combos offer only what {@link QueryAggregationSupport} accepts - non-repeatable fields that are not
 * {@code indexed = false} - and an aggregation's field combo only those the chosen function is available for. A
 * stored value that is not acceptable any more (the field was removed or changed type, or the function was changed
 * to one that does not fit) stays in its combo, marked as an error, and the findings of {@link
 * QueryAggregationValidator} - the same ones the project-wide validation reports - are listed below the switch.
 */
public class QueryAggregationPanelController extends AbstractPropertyEditor {

  private static final int COMMIT_DEBOUNCE_MS = 150;
  private static final String ALIAS_DEBOUNCE_KEY = "query-aggregation-alias";
  private static final String ERROR_STYLE_CLASS = "validation-error";
  private static final double FUNCTION_WIDTH = 140.0;
  private static final double FIELD_WIDTH = 320.0;
  private static final double ALIAS_WIDTH = 160.0;

  @FXML
  private CheckBox aggregateResultsField;
  @FXML
  private ErrorContainerController problemsContainerController;
  @FXML
  private VBox aggregationBox;
  @FXML
  private VBox groupRows;
  @FXML
  private VBox aggregationRows;
  @FXML
  private Label groupEmptyLabel;
  @FXML
  private Label aggregationsEmptyLabel;
  @FXML
  private HBox aggregationsHeaderRow;

  private final Debouncer debouncer = new Debouncer();

  private QueryModel model;
  // The target Document Model's elements, null while the target does not resolve.
  private ElementIndex index;
  // Every field of the target Document Model an aggregation may use, with its effective type (null if unknown).
  private final Map<String, FieldType> candidates = new LinkedHashMap<>();
  // The aggregation of a query whose switch was turned off, see the class doc.
  private QueryAggregation switchedOff;

  // Set while a combo's items are replaced or the switch is repopulated, so those programmatic value changes are
  // not mistaken for user edits.
  private boolean updatingFromModel;

  @Override
  protected boolean suppressErrorContainer() {
    return true;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    problemsContainerController.hide();
    aggregateResultsField.selectedProperty().addListener((observable, oldValue, selected) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (selected) {
        content().setAggregation(switchedOff != null ? switchedOff : new QueryAggregation());
        switchedOff = null;
      }
      else {
        switchedOff = content().getAggregation();
        content().setAggregation(null);
      }
      rebuild();
      changed();
    });
  }

  @Override
  public void destroy() {
    debouncer.shutdown();
    super.destroy();
  }

  public void load(@NonNull ProjectItem projectItem, @NonNull QueryModel model) {
    if (this.model != model) {
      switchedOff = null;
    }
    this.model = model;
    loadCandidates(projectItem);

    updatingFromModel = true;
    try {
      aggregateResultsField.setSelected(content().getAggregation() != null);
    }
    finally {
      updatingFromModel = false;
    }
    rebuild();
  }

  // ---- candidates -------------------------------------------------------------------------------------------

  private void loadCandidates(ProjectItem projectItem) {
    candidates.clear();
    index = null;
    String targetId = content().getTargetDocumentModel();
    List<DocumentModel> documentModels = ProjectDocumentModels.getOtherDocumentModels(projectItem);
    DocumentModel target = documentModels.stream().filter(dm -> dm.getId().equals(targetId)).findFirst().orElse(null);
    if (target == null || target.getContent() == null || target.getContent().getModelRoot() == null) {
      return;
    }
    index = new ElementIndex(target, documentModels);
    for (String path : QueryAggregationSupport.candidatePaths(index)) {
      Element element = index.resolveAbsolutePath(path).orElse(null);
      candidates.put(path, element == null ? null : QueryAggregationSupport.effectiveType(index, element));
    }
  }

  /** The candidate paths {@code function} can be applied to; every candidate while no function is chosen. */
  private List<String> fieldItemsFor(String function) {
    List<String> items = new ArrayList<>();
    candidates.forEach((path, type) -> {
      if (QueryAggregationSupport.isFunctionAvailable(function, type)) {
        items.add(path);
      }
    });
    return items;
  }

  /** Whether {@code path} is acceptable for {@code function} (null for a group field) - the validator's own rule.
   * Nothing is flagged that cannot be checked: an empty value, a meta path, or a target that does not resolve. */
  private boolean isAcceptable(String path, String function) {
    if (index == null || path == null || path.isBlank() || QueryElementResolution.isMetaPath(path)) {
      return true;
    }
    QueryAggregationSupport.Check check = QueryAggregationSupport.check(index, path);
    return check.ok() && QueryAggregationSupport.isFunctionAvailable(function, QueryAggregationSupport.effectiveType(index, check.element()));
  }

  // ---- rows -------------------------------------------------------------------------------------------------

  @FXML
  private void onAddGroup() {
    QueryAggregation aggregation = content().getAggregation();
    if (aggregation == null) {
      return;
    }
    aggregation.getGroup().add(new QueryAggregationGroup());
    rebuild();
    changed();
  }

  @FXML
  private void onAddAggregation() {
    QueryAggregation aggregation = content().getAggregation();
    if (aggregation == null) {
      return;
    }
    QueryAggregationEntry entry = new QueryAggregationEntry();
    entry.setFunction(QueryAggregationEntry.FUNCTION_COUNT);
    aggregation.getAggregations().add(entry);
    rebuild();
    changed();
  }

  private void rebuild() {
    groupRows.getChildren().clear();
    aggregationRows.getChildren().clear();

    QueryAggregation aggregation = content().getAggregation();
    boolean on = aggregation != null;
    setVisibleAndManaged(aggregationBox, on);
    if (!on) {
      showProblems(List.of());
      return;
    }

    for (QueryAggregationGroup group : aggregation.getGroup()) {
      groupRows.getChildren().add(createGroupRow(aggregation, group));
    }
    for (QueryAggregationEntry entry : aggregation.getAggregations()) {
      aggregationRows.getChildren().add(createEntryRow(aggregation, entry));
    }
    setVisibleAndManaged(groupEmptyLabel, aggregation.getGroup().isEmpty());
    setVisibleAndManaged(aggregationsEmptyLabel, aggregation.getAggregations().isEmpty());
    setVisibleAndManaged(aggregationsHeaderRow, !aggregation.getAggregations().isEmpty());
    showProblems(currentProblems());
  }

  private HBox createGroupRow(QueryAggregation aggregation, QueryAggregationGroup group) {
    ComboBox<String> fieldCombo = createFieldCombo(group.getField(), fieldItemsFor(null));
    flag(fieldCombo, !isAcceptable(group.getField(), null));
    fieldCombo.valueProperty().addListener((observable, oldValue, value) -> {
      if (!updatingFromModel) {
        group.setField(value);
        flag(fieldCombo, !isAcceptable(value, null));
        changed();
      }
    });
    Button remove = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("query_aggregation.remove_group"), () -> {
      aggregation.getGroup().remove(group);
      rebuild();
      changed();
    });
    return createRow(fieldCombo, remove);
  }

  private HBox createEntryRow(QueryAggregation aggregation, QueryAggregationEntry entry) {
    ComboBox<String> functionCombo = createFunctionCombo(entry.getFunction());
    ComboBox<String> fieldCombo = createFieldCombo(entry.getField(), fieldItemsFor(entry.getFunction()));
    flag(fieldCombo, !isAcceptable(entry.getField(), entry.getFunction()));
    TextField aliasField = new TextField(entry.getAlias());
    aliasField.setPrefWidth(ALIAS_WIDTH);

    functionCombo.valueProperty().addListener((observable, oldValue, value) -> {
      if (updatingFromModel) {
        return;
      }
      entry.setFunction(value);
      // The field stays as it is even when the new function is not available for it - that is flagged, not undone.
      setFieldItems(fieldCombo, entry.getField(), fieldItemsFor(value));
      flag(fieldCombo, !isAcceptable(entry.getField(), value));
      changed();
    });
    fieldCombo.valueProperty().addListener((observable, oldValue, value) -> {
      if (!updatingFromModel) {
        entry.setField(value);
        flag(fieldCombo, !isAcceptable(value, entry.getFunction()));
        changed();
      }
    });
    aliasField.textProperty().addListener((observable, oldValue, text) -> {
      entry.setAlias(text == null || text.isBlank() ? null : text);
      debouncer.debounce(ALIAS_DEBOUNCE_KEY, this::changed, COMMIT_DEBOUNCE_MS, true);
    });

    Button remove = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("query_aggregation.remove_aggregation"), () -> {
      aggregation.getAggregations().remove(entry);
      rebuild();
      changed();
    });
    return createRow(functionCombo, fieldCombo, aliasField, remove);
  }

  private static HBox createRow(Node... children) {
    HBox row = new HBox(10.0, children);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  private ComboBox<String> createFunctionCombo(String current) {
    ComboBox<String> combo = new ComboBox<>();
    combo.setPrefWidth(FUNCTION_WIDTH);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String function) {
        return function == null || !QueryAggregationEntry.FUNCTIONS.contains(function)
            ? function : StudioBundle.get("query_aggregation.function." + function);
      }

      @Override
      public String fromString(String text) {
        return text;
      }
    });
    List<String> items = new ArrayList<>(QueryAggregationEntry.FUNCTIONS);
    if (current != null && !current.isBlank() && !items.contains(current)) {
      items.add(current); // an unknown function of a hand-edited file stays visible (and is reported)
    }
    setItems(combo, items, current);
    flag(combo, current != null && !current.isBlank() && !QueryAggregationEntry.FUNCTIONS.contains(current));
    return combo;
  }

  private ComboBox<String> createFieldCombo(String current, List<String> items) {
    ComboBox<String> combo = new ComboBox<>();
    combo.setPrefWidth(FIELD_WIDTH);
    combo.setPromptText(StudioBundle.get("query_aggregation.select_field"));
    setFieldItems(combo, current, items);
    return combo;
  }

  /** Replaces {@code combo}'s items without losing {@code current}, even when it is not among {@code items}. */
  private void setFieldItems(ComboBox<String> combo, String current, List<String> items) {
    List<String> shown = new ArrayList<>(items);
    if (current != null && !current.isBlank() && !shown.contains(current)) {
      shown.add(0, current);
    }
    setItems(combo, shown, current == null || current.isBlank() ? null : current);
  }

  private void setItems(ComboBox<String> combo, List<String> items, String value) {
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      combo.getItems().setAll(items);
      combo.setValue(value);
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  private static void flag(ComboBox<String> combo, boolean error) {
    combo.getStyleClass().remove(ERROR_STYLE_CLASS);
    if (error) {
      combo.getStyleClass().add(ERROR_STYLE_CLASS);
    }
  }

  private static void setVisibleAndManaged(Node node, boolean visible) {
    node.setVisible(visible);
    node.setManaged(visible);
  }

  // ---- saving and problems ----------------------------------------------------------------------------------

  private void changed() {
    commitHeaderChange();
    showProblems(currentProblems());
  }

  private QueryModelContent content() {
    return model.getContent();
  }

  /** What the validator reports about the aggregation as it is now: errors first, then warnings. */
  private List<ModelValidationError> currentProblems() {
    if (content().getAggregation() == null || Studio.getValidationService() == null) {
      return List.of();
    }
    return Studio.getValidationService().validate(model).stream()
        .filter(error -> error.elementId() != null && error.elementId().startsWith(QueryAggregationValidator.ELEMENT_ID))
        .sorted((a, b) -> Boolean.compare(isWarning(a), isWarning(b)))
        .toList();
  }

  private void showProblems(List<ModelValidationError> problems) {
    if (problems.isEmpty()) {
      problemsContainerController.hide();
      return;
    }
    boolean onlyWarnings = problems.stream().allMatch(QueryAggregationPanelController::isWarning);
    problemsContainerController.show(onlyWarnings ? Severity.WARNING.name() : Severity.ERROR.name(),
        problems.stream().map(ModelValidationError::message).collect(Collectors.joining("\n")));
  }

  private static boolean isWarning(ModelValidationError error) {
    return Severity.WARNING.name().equals(error.severity());
  }
}
