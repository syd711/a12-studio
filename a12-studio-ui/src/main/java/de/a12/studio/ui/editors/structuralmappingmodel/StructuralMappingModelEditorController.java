package de.a12.studio.ui.editors.structuralmappingmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.structuralmappingmodel.FieldMapping;
import de.a12.studio.models.structuralmappingmodel.ResolutionStrategy;
import de.a12.studio.models.structuralmappingmodel.SmmBlockTree;
import de.a12.studio.models.structuralmappingmodel.SmmElement;
import de.a12.studio.models.structuralmappingmodel.SmmNode;
import de.a12.studio.models.structuralmappingmodel.SmmOperations;
import de.a12.studio.models.structuralmappingmodel.SmmPath;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModelContent;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.kernel.StructuralMappingContext;
import de.a12.studio.modelsvalidation.kernel.StructuralMappingContext.Finding;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.structuralmappingmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.structuralmappingmodel.dialogs.MoveFieldMappingDialogController;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Edits a {@link StructuralMappingModel}: which source field fills which target field, and how the repetitions of
 * repeating groups are matched up - SME's Structural Mapping Model editor.
 *
 * <p>Like SME's it shows two trees, the source model on the left and the target model on the right, and a column
 * per mapping block in the target tree that holds the mappings as tags in the row of the target element they fill.
 * A mapping is made by dragging a source field onto a target field (or by "Map Selected Source Field Here"); the
 * kernel decides which resolution strategies (Fold, Slice) the new field mapping needs and where it goes. Tags
 * are edited, moved to another mapping block and deleted from their context menu, the "Clear" column marks the
 * groups cleared before they are filled the first time.
 *
 * <p>The model is not edited in isolation: the groups and fields the mappings refer to are the source and target
 * model of the Mapping Model that uses it ({@link StructuralMappingContext}). Without one - no Mapping Model uses
 * the model, or the kernel cannot combine its Document Models - the trees stay empty and say why. The kernel's
 * check (also off the UI thread) marks the tags and tree rows with problems.
 */
@Slf4j
public class StructuralMappingModelEditorController extends AbstractEditorController implements SmmTagsCell.Host {

  // Identifies a source field being dragged; the dragboard content is its full name.
  private static final DataFormat SOURCE_FIELD = new DataFormat("application/x-a12-smm-source-field");

  @FXML
  private Label contextLabel;

  @FXML
  private Label problemsLabel;

  @FXML
  private Button removeInvalidButton;

  @FXML
  private SmmTreePaneController sourcePaneController;

  @FXML
  private SmmTreePaneController targetPaneController;

  private StructuralMappingModel model;

  // null while there is none (not yet computed, no Mapping Model, kernel failure).
  private StructuralMappingContext context;

  private List<SmmBlockTree> trees = List.of();

  // The kernel's findings by element pointer (SmmPointers form).
  private Map<String, List<Finding>> findings = Map.of();

  // Which target elements have an error on a mapping that ends in them, for the row badge.
  private Set<String> errorTargetPaths = Set.of();

  // Invalidates results of earlier loads/checks that arrive after a later one was started.
  private int loadGeneration;

  private int checkGeneration;

  private TreeTableColumn<SmmElement, SmmElement> clearColumn;

  @FXML
  private void initialize() {
    sourcePaneController.setTitle(StudioBundle.get("structural_mapping.source_title"));
    sourcePaneController.setMappedPaths(() -> model == null ? Set.of() : SmmOperations.mappedSourceElements(model.getContent()));
    sourcePaneController.setNameSuffix(this::sourceUsageSuffix);
    addTypeColumn(sourcePaneController.getTree());
    installSourceDrag(sourcePaneController.getTree());

    targetPaneController.setTitle(StudioBundle.get("structural_mapping.target_title"));
    targetPaneController.setMappedPaths(() -> model == null ? Set.of() : SmmOperations.mappedTargetElements(model.getContent()));
    targetPaneController.setBadge(this::targetBadge);
    installTargetRows(targetPaneController.getTree());
    clearColumn = createClearColumn();
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    this.model = (StructuralMappingModel) model;
    reload();
    updateSettingsErrorBadge();
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.STRUCTURALMAPPING;
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    super.modelSaved(event);
    // The Mapping Model that uses this model decides what the trees show.
    if (projectItem != null && !event.getItem().equals(projectItem) && event.getItem().getModel() instanceof MappingModel) {
      reload();
    }
  }

  @Override
  protected void onDocumentModelChangedElsewhere() {
    reload();
  }

  // ---- context ------------------------------------------------------------------------------

  /** Finds the Mapping Model that uses the model and has the kernel compute the source and target model (off the UI thread). */
  private void reload() {
    int generation = ++loadGeneration;
    Project project = Studio.getCurrentProject();
    List<A12Model<?>> mappingModels = project == null ? List.of() : ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.MAPPING);
    List<MappingModel> owners = StructuralMappingContext.findOwners(model, mappingModels);
    if (project == null || owners.isEmpty()) {
      showNoContext(StudioBundle.get("structural_mapping.no_owner"));
      return;
    }
    MappingModel owner = owners.get(0);
    contextLabel.setText(StudioBundle.get("structural_mapping.loading"));

    CompletableFuture.supplyAsync(() -> {
      try {
        return StructuralMappingContext.resolve(project.getRoot(), owner);
      }
      catch (StructuralMappingContext.Unavailable e) {
        throw new java.util.concurrent.CompletionException(e);
      }
    }).whenComplete((resolved, failure) -> Platform.runLater(() -> {
      if (generation != loadGeneration) {
        return;
      }
      if (failure != null) {
        Throwable cause = failure.getCause() != null ? failure.getCause() : failure;
        showNoContext(StudioBundle.get("structural_mapping.no_context", cause.getMessage()));
        return;
      }
      boolean unchanged = resolved == context;
      context = resolved;
      contextLabel.setText(owners.size() == 1
          ? StudioBundle.get("structural_mapping.owner", owner.getId())
          : StudioBundle.get("structural_mapping.owner_ambiguous", owner.getId(),
              owners.stream().skip(1).map(MappingModel::getId).collect(Collectors.joining(", "))));
      if (!unchanged) {
        sourcePaneController.setRoots(context.sourceRoots());
        targetPaneController.setRoots(context.targetRoots());
      }
      rebuild();
    }));
  }

  private void showNoContext(String message) {
    context = null;
    trees = List.of();
    findings = Map.of();
    errorTargetPaths = Set.of();
    contextLabel.setText(message);
    problemsLabel.setText("");
    removeInvalidButton.setVisible(false);
    removeInvalidButton.setManaged(false);
    sourcePaneController.setRoots(List.of());
    targetPaneController.setRoots(List.of());
  }

  // ---- showing the model ------------------------------------------------------------------------

  /** Brings both trees up to date with the mappings, and has the kernel check them. */
  private void rebuild() {
    trees = SmmOperations.trees(model.getContent());
    updateInvalidElements();
    configureTargetColumns();
    sourcePaneController.refresh();
    targetPaneController.refresh();
    updateSettingsErrorBadge();
    runCheck();
  }

  private void runCheck() {
    if (context == null) {
      return;
    }
    int generation = ++checkGeneration;
    StructuralMappingContext checkContext = context;
    String snapshot;
    try {
      snapshot = StructuralMappingContext.snapshot(model);
    }
    catch (StructuralMappingContext.Unavailable e) {
      log.warn("Cannot check the structural mapping model: {}", e.getMessage());
      return;
    }
    CompletableFuture.supplyAsync(() -> {
      try {
        return checkContext.check(snapshot);
      }
      catch (StructuralMappingContext.Unavailable e) {
        log.warn("Cannot check the structural mapping model: {}", e.getMessage());
        return List.<Finding>of();
      }
    }).thenAccept(result -> Platform.runLater(() -> {
      if (generation == checkGeneration && checkContext == context) {
        applyFindings(result);
      }
    }));
  }

  private void applyFindings(List<Finding> result) {
    Map<String, List<Finding>> byPointer = new HashMap<>();
    result.forEach(finding -> byPointer.computeIfAbsent(finding.pointer(), pointer -> new ArrayList<>()).add(finding));
    findings = byPointer;

    Set<String> errorPaths = new HashSet<>();
    Set<String> nodePointers = new HashSet<>();
    for (SmmBlockTree tree : trees) {
      for (SmmNode node : tree.nodes()) {
        nodePointers.add(node.pointer());
        if (hasError(node.pointer())) {
          errorPaths.add(SmmPath.format(node.targetPath()));
        }
      }
    }
    errorTargetPaths = errorPaths;

    List<Finding> unrelated = result.stream().filter(finding -> !nodePointers.contains(finding.pointer())).toList();
    if (unrelated.isEmpty()) {
      problemsLabel.setText("");
      problemsLabel.setTooltip(null);
    }
    else {
      problemsLabel.setText(StudioBundle.get("structural_mapping.other_problems", unrelated.size()));
      problemsLabel.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("structural_mapping.other_problems_title") + ":\n"
          + unrelated.stream().map(finding -> "- " + finding.message()).collect(Collectors.joining("\n"))));
    }
    targetPaneController.getTree().refresh();
  }

  private boolean hasError(String pointer) {
    return findings.getOrDefault(pointer, List.of()).stream().anyMatch(finding -> finding.severity() == Severity.ERROR);
  }

  // ---- invalid elements ------------------------------------------------------------------------------

  /**
   * What the trees cannot show: mappings and cleared groups whose target group or field is not in the target model
   * (any more), e.g. after the target Document Model changed. SME offers to remove them when the model is opened;
   * here a button in the banner does.
   */
  private List<String> invalidElements() {
    List<String> invalid = new ArrayList<>();
    if (context == null) {
      return invalid;
    }
    for (SmmBlockTree tree : trees) {
      for (SmmNode node : tree.nodes()) {
        if (isInvalid(node)) {
          invalid.add(node + " (" + findings.getOrDefault(node.pointer(), List.of()).stream().map(Finding::message).findFirst()
              .orElse(SmmPath.format(node.targetPath())) + ")");
        }
      }
    }
    model.getContent().getGroupsToClearOnFirstFill().stream()
        .filter(group -> SmmElement.find(context.targetRoots(), group.getFullName()) == null)
        .forEach(group -> invalid.add(group.getFullName()));
    return invalid;
  }

  private boolean isInvalid(SmmNode node) {
    return SmmElement.find(context.targetRoots(), SmmPath.format(node.targetPath())) == null;
  }

  private void updateInvalidElements() {
    boolean any = !invalidElements().isEmpty();
    removeInvalidButton.setVisible(any);
    removeInvalidButton.setManaged(any);
  }

  @FXML
  private void onRemoveInvalidElements() {
    List<String> invalid = invalidElements();
    if (invalid.isEmpty()) {
      return;
    }
    String list = invalid.stream().limit(10).map(line -> "- " + line).collect(Collectors.joining("\n"))
        + (invalid.size() > 10 ? "\n" + StudioBundle.get("structural_mapping.remove_invalid_more", invalid.size() - 10) : "");
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("structural_mapping.remove_invalid_message"),
        list, StudioBundle.get("structural_mapping.remove_invalid_question"), StudioBundle.get("structural_mapping.remove_invalid_ok"));
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    StructuralMappingModelContent content = model.getContent();
    // Deleting can drop emptied strategies and blocks, which shifts the nodes' indexes: look again after every delete.
    boolean deleted = true;
    while (deleted) {
      deleted = false;
      for (SmmBlockTree tree : SmmOperations.trees(content)) {
        SmmNode invalidNode = tree.nodes().stream().filter(this::isInvalid).findFirst().orElse(null);
        if (invalidNode != null) {
          SmmOperations.delete(content, invalidNode);
          deleted = true;
          break;
        }
      }
    }
    content.getGroupsToClearOnFirstFill().removeIf(group -> SmmElement.find(context.targetRoots(), group.getFullName()) == null);
    commit();
  }

  private String sourceUsageSuffix(SmmElement element) {
    if (element.group() || model == null) {
      return "";
    }
    Integer usages = SmmOperations.sourceFieldUsages(model.getContent()).get(element.fullName());
    return usages != null ? " (" + usages + ")" : "";
  }

  private void addTypeColumn(TreeTableView<SmmElement> tree) {
    TreeTableColumn<SmmElement, String> typeColumn = new TreeTableColumn<>(StudioBundle.get("structural_mapping.data_type"));
    typeColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getValue().dataType()));
    typeColumn.setPrefWidth(110);
    typeColumn.setSortable(false);
    tree.getColumns().add(typeColumn);
  }

  // ---- target tree: columns, badge, rows -----------------------------------------------------------

  private void configureTargetColumns() {
    TreeTableView<SmmElement> tree = targetPaneController.getTree();
    List<TreeTableColumn<SmmElement, ?>> columns = new ArrayList<>();
    columns.add(tree.getColumns().get(0));
    columns.add(clearColumn);
    for (SmmBlockTree blockTree : trees) {
      TreeTableColumn<SmmElement, SmmElement> column = new TreeTableColumn<>(StudioBundle.get("structural_mapping.mapping_block", blockTree.blockIndex() + 1));
      column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getValue()));
      int blockIndex = blockTree.blockIndex();
      column.setCellFactory(c -> new SmmTagsCell(blockIndex, this));
      column.setSortable(false);
      column.setResizable(false);
      double width = blockTree.columnCount() * (SmmTagsCell.TAG_WIDTH + SmmTagsCell.TAG_SPACING) + 8;
      column.setMinWidth(width);
      column.setPrefWidth(width);
      column.setMaxWidth(width);
      columns.add(column);
    }
    tree.getColumns().setAll(columns);
  }

  private TreeTableColumn<SmmElement, SmmElement> createClearColumn() {
    TreeTableColumn<SmmElement, SmmElement> column = new TreeTableColumn<>(StudioBundle.get("structural_mapping.clear"));
    column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getValue()));
    column.setCellFactory(c -> new ClearCell());
    column.setSortable(false);
    column.setResizable(false);
    column.setPrefWidth(70);
    column.setMinWidth(70);
    column.setMaxWidth(70);
    Label info = new Label();
    WidgetFactory.createHelpIcon(info, StudioBundle.get("structural_mapping.clear_info"));
    column.setGraphic(info);
    return column;
  }

  /** Error and warning icons in the name cell of a target row. */
  private Node targetBadge(SmmElement element, boolean expanded) {
    if (context == null) {
      return null;
    }
    String path = element.fullName();
    if (errorTargetPaths.contains(path)) {
      return WidgetFactory.wrapIcon(WidgetFactory.createExclamationIcon(WidgetFactory.ERROR_COLOR), StudioBundle.get("structural_mapping.invalid_element"));
    }
    if (!expanded && errorTargetPaths.stream().anyMatch(errorPath -> errorPath.startsWith(path + "/"))) {
      return WidgetFactory.wrapIcon(WidgetFactory.createExclamationIcon(WidgetFactory.ERROR_COLOR), StudioBundle.get("structural_mapping.contains_invalid"));
    }
    return overflowWarning(element);
  }

  /** SME's warning: the source groups that fill a target group can repeat more often than the target group allows. */
  private Node overflowWarning(SmmElement element) {
    if (!element.group() || element.repeatability() == null) {
      return null;
    }
    List<String> target = SmmPath.parse(element.fullName());
    int sourceRepetitions = 0;
    boolean mapped = false;
    for (SmmBlockTree tree : trees) {
      for (SmmNode node : tree.nodes()) {
        if (node.isResolutionStrategy() && node.targetPath().equals(target)) {
          mapped = true;
          SmmElement sourceGroup = SmmElement.find(context.sourceRoots(), SmmPath.format(node.sourcePath()));
          sourceRepetitions += sourceGroup != null && sourceGroup.repeatability() != null ? sourceGroup.repeatability() : 0;
        }
      }
    }
    if (!mapped || element.repeatability() >= sourceRepetitions) {
      return null;
    }
    return WidgetFactory.wrapIcon(WidgetFactory.createWarningIcon(null),
        StudioBundle.get("structural_mapping.overflow_warning", sourceRepetitions, element.repeatability()));
  }

  private void installTargetRows(TreeTableView<SmmElement> tree) {
    tree.setRowFactory(table -> {
      TreeTableRow<SmmElement> row = new TreeTableRow<>();

      row.setOnDragOver(event -> {
        if (isFieldRow(row) && event.getDragboard().hasContent(SOURCE_FIELD)) {
          event.acceptTransferModes(TransferMode.COPY);
          if (!row.getStyleClass().contains("smm-drop-target")) {
            row.getStyleClass().add("smm-drop-target");
          }
        }
        event.consume();
      });
      row.setOnDragExited(event -> row.getStyleClass().remove("smm-drop-target"));
      row.setOnDragDropped(event -> {
        Dragboard dragboard = event.getDragboard();
        boolean accepted = isFieldRow(row) && dragboard.hasContent(SOURCE_FIELD);
        if (accepted) {
          addFieldMapping((String) dragboard.getContent(SOURCE_FIELD), row.getItem().fullName());
        }
        row.getStyleClass().remove("smm-drop-target");
        event.setDropCompleted(accepted);
        event.consume();
      });

      MenuItem mapSelected = new MenuItem(StudioBundle.get("structural_mapping.map_selected"));
      mapSelected.setMnemonicParsing(true);
      mapSelected.setOnAction(event -> {
        SmmElement source = sourcePaneController.getSelectedElement();
        if (source != null && !source.group() && isFieldRow(row)) {
          addFieldMapping(source.fullName(), row.getItem().fullName());
        }
      });
      ContextMenu menu = new ContextMenu(mapSelected);
      menu.setOnShowing(event -> {
        SmmElement source = sourcePaneController.getSelectedElement();
        mapSelected.setDisable(source == null || source.group() || !isFieldRow(row));
      });
      row.contextMenuProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(menu));
      return row;
    });
  }

  private static boolean isFieldRow(TreeTableRow<SmmElement> row) {
    return !row.isEmpty() && row.getItem() != null && !row.getItem().group();
  }

  private void installSourceDrag(TreeTableView<SmmElement> tree) {
    tree.setRowFactory(table -> {
      TreeTableRow<SmmElement> row = new TreeTableRow<>();
      row.setOnDragDetected(event -> {
        if (!isFieldRow(row)) {
          return;
        }
        Dragboard dragboard = row.startDragAndDrop(TransferMode.COPY);
        ClipboardContent content = new ClipboardContent();
        content.put(SOURCE_FIELD, row.getItem().fullName());
        dragboard.setContent(content);
        event.consume();
      });
      return row;
    });
  }

  /** The Clear column: whether a group is cleared before it is filled the first time (a cleared group clears the groups below it). */
  private final class ClearCell extends TreeTableCell<SmmElement, SmmElement> {

    @Override
    protected void updateItem(SmmElement element, boolean empty) {
      super.updateItem(element, empty);
      setText(null);
      if (empty || element == null || !element.group()) {
        setGraphic(null);
        return;
      }
      String path = element.fullName();
      boolean cleared = SmmOperations.isGroupCleared(model.getContent(), path);
      boolean byAncestor = SmmOperations.isClearedByAncestor(model.getContent(), path);
      CheckBox checkBox = new CheckBox();
      checkBox.setSelected(cleared || byAncestor);
      checkBox.setDisable(byAncestor);
      checkBox.setTooltip(WidgetFactory.createTooltip(StudioBundle.get(cleared ? "structural_mapping.clear_yes"
          : byAncestor ? "structural_mapping.clear_by_parent" : "structural_mapping.clear_no")));
      checkBox.setOnAction(event -> {
        SmmOperations.setGroupCleared(model.getContent(), path, checkBox.isSelected());
        commit();
      });
      HBox box = new HBox(checkBox);
      box.setAlignment(Pos.CENTER);
      setGraphic(box);
    }
  }

  // ---- editing ----------------------------------------------------------------------------------

  /** Saves the model after an edit and brings the trees up to date. */
  private void commit() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    rebuild();
  }

  private void addFieldMapping(String sourceField, String targetField) {
    if (context == null) {
      return;
    }
    try {
      StructuralMappingModelContent added = context.addFieldMapping(model, sourceField, targetField);
      StructuralMappingContext.takeMappingBlocks(model, added);
      commit();
    }
    catch (StructuralMappingContext.Unavailable e) {
      WidgetFactory.showAlert(Studio.stage, StudioBundle.get("structural_mapping.add_failed", e.getMessage()));
    }
  }

  // ---- SmmTagsCell.Host -----------------------------------------------------------------------------

  @Override
  public SmmBlockTree blockTree(int blockIndex) {
    return blockIndex >= 0 && blockIndex < trees.size() ? trees.get(blockIndex) : null;
  }

  @Override
  public String label(SmmNode node) {
    SmmNode parent = node.parent();
    String relative = SmmPath.relativeTo(parent != null ? parent.sourcePath() : List.of(), node.sourcePath());
    return relative + (node.children().isEmpty() ? "" : "/");
  }

  @Override
  public String sliceFieldLabel(SmmNode node) {
    return SmmPath.relativeTo(node.sourcePath(), SmmPath.parse(node.resolutionStrategy().getSlice().getSourceFieldFullName()));
  }

  @Override
  public String tooltip(SmmNode node) {
    String text;
    if (node.isFieldMapping()) {
      text = SmmPath.format(node.sourcePath());
    }
    else if (node.kind() == SmmNode.Kind.SLICE) {
      ResolutionStrategy strategy = node.resolutionStrategy();
      List<String> targetField = SmmPath.parse(strategy.getSlice().getTargetFieldFullName());
      text = StudioBundle.get("structural_mapping.tag_slice_tooltip", SmmPath.format(node.sourcePath()),
          strategy.getSlice().getTargetFieldFullName(), strategy.getSlice().getSourceFieldFullName(),
          targetField.isEmpty() ? "" : targetField.get(targetField.size() - 1));
    }
    else {
      text = StudioBundle.get("structural_mapping.tag_fold_tooltip", SmmPath.format(node.sourcePath()));
    }
    String problems = findings.getOrDefault(node.pointer(), List.of()).stream()
        .map(finding -> finding.severity() + " " + finding.message())
        .collect(Collectors.joining("\n"));
    return problems.isEmpty() ? text : problems + "\n\n" + text;
  }

  @Override
  public String sliceFieldTooltip(SmmNode node) {
    return node.resolutionStrategy().getSlice().getSourceFieldFullName();
  }

  @Override
  public boolean hasProblem(SmmNode node) {
    return !findings.getOrDefault(node.pointer(), List.of()).isEmpty();
  }

  @Override
  public void edit(SmmNode node) {
    if (context != null && node.isResolutionStrategy() && Dialogs.showResolutionStrategy(Studio.stage, context, model, node)) {
      commit();
    }
  }

  @Override
  public void move(SmmNode node) {
    if (context == null || !node.isFieldMapping()) {
      return;
    }
    FieldMapping fieldMapping = node.fieldMapping();
    int blockCount = model.getContent().getMappingBlocks().size();
    try {
      List<StructuralMappingContext.MoveOption> options = context.moveOptions(model, fieldMapping.getSourceFieldFullName(),
          fieldMapping.getTargetFieldFullName());

      List<MoveFieldMappingDialogController.Choice> choices = new ArrayList<>();
      MoveFieldMappingDialogController.Choice current = new MoveFieldMappingDialogController.Choice(blockLabel(node.blockIndex(), blockCount), null);
      Map<Integer, MoveFieldMappingDialogController.Choice> byBlock = new java.util.TreeMap<>();
      byBlock.put(node.blockIndex(), current);
      for (StructuralMappingContext.MoveOption option : options) {
        if (option.mappingBlock() != node.blockIndex()) {
          byBlock.put(option.mappingBlock(), new MoveFieldMappingDialogController.Choice(blockLabel(option.mappingBlock(), blockCount), option.modified()));
        }
      }
      choices.addAll(byBlock.values());

      String description = fieldMapping.getSourceFieldFullName() + " -> " + fieldMapping.getTargetFieldFullName();
      Optional<MoveFieldMappingDialogController.Choice> chosen = Dialogs.showMoveFieldMapping(Studio.stage, description, choices, current);
      if (chosen.isPresent() && chosen.get().modified() != null) {
        moveTo(node, chosen.get().modified());
      }
    }
    catch (StructuralMappingContext.Unavailable e) {
      WidgetFactory.showAlert(Studio.stage, StudioBundle.get("structural_mapping.move_failed", e.getMessage()));
    }
  }

  private static String blockLabel(int index, int blockCount) {
    return index < blockCount ? StudioBundle.get("structural_mapping.mapping_block", index + 1) : StudioBundle.get("structural_mapping.move_new_block");
  }

  /**
   * SME moves a field mapping by taking the model the kernel computed with the mapping added in the other block,
   * and deleting the mapping from the block it was in.
   */
  private void moveTo(SmmNode node, StructuralMappingModelContent withMappingAdded) {
    FieldMapping original = node.fieldMapping();
    StructuralMappingContext.takeMappingBlocks(model, withMappingAdded);
    List<SmmBlockTree> moved = SmmOperations.trees(model.getContent());
    if (node.blockIndex() < moved.size()) {
      moved.get(node.blockIndex()).nodes().stream()
          .filter(candidate -> candidate.isFieldMapping()
              && candidate.fieldMapping().getSourceFieldFullName().equals(original.getSourceFieldFullName())
              && candidate.fieldMapping().getTargetFieldFullName().equals(original.getTargetFieldFullName()))
          .findFirst()
          .ifPresent(copy -> SmmOperations.delete(model.getContent(), copy));
    }
    commit();
  }

  @Override
  public void delete(SmmNode node) {
    String message = StudioBundle.get(node.isFieldMapping() ? "structural_mapping.delete_field_mapping_confirm" : "structural_mapping.delete_strategy_confirm");
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, message, null, null, StudioBundle.get("structural_mapping.tag_delete").replace("_", ""));
    if (result.isPresent() && result.get() == ButtonType.OK) {
      SmmOperations.delete(model.getContent(), node);
      commit();
    }
  }
}
