package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.documentmodel.commands.AddNodeCommand;
import de.a12.studio.ui.editors.documentmodel.commands.DeleteNodeCommand;
import de.a12.studio.ui.editors.documentmodel.dialogs.CreateOverviewModelDialogController.FieldOption;
import de.a12.studio.ui.editors.documentmodel.dialogs.CreateOverviewModelDialogController.Result;
import de.a12.studio.ui.editors.documentmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.documentmodel.dialogs.IncludeDialogController;
import de.a12.studio.ui.editors.documentmodel.dialogs.MoveGroupDialogController;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.commandstack.Command;
import de.a12.studio.ui.util.commandstack.CommandStack;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import de.a12.studio.ui.util.StudioBundle;

/**
 * Builds the document model tree's context menu and carries out its actions: element creation, deletion, and
 * Cut/Copy/Paste ({@link #cutSelection()}/{@link #copySelection()}/{@link #pasteSelection()}, also reused by
 * {@link DocumentModelElementsTreeController} for the toolbar's Cut/Copy/Paste buttons), plus {@link
 * #createAddMenuItems()}, which is also reused by that controller for the toolbar "Add" button.
 */
@Slf4j
public class DocumentModelActions {

  // Static so Copy/Cut in one Document Model tab and Paste in another (or a later reopen of the same tab)
  // work, mirroring how a system clipboard behaves. Holds JSON snapshots (one per top-level copied element)
  // rather than the live objects so repeated pastes each get their own fresh clone with fresh ids (see
  // #pasteSelection), and so multi-selection Copy/Cut carries every selected top-level element.
  private static List<String> clipboardJson = List.of();

  private final ProjectItem projectItem;
  private final ModelRoot modelRoot;
  private final CommandStack commandStack;
  private final TreeTableView<ElementViewModel> elementsTreeTable;
  private final Consumer<Element> onModelChanged;

  // Field/Validation Rule/Computation Rule can only be inserted into a selected group, unlike
  // Group/Attachment/Multi-Select/Include which can also land at the model root (see #resolveInsertionPointForAdd) -
  // tracked here so #updateAddMenuItemsState can grey them out independently of the toolbar's single Add button.
  private MenuItem fieldMenuItem;
  private MenuItem ruleMenuItem;
  private MenuItem computationMenuItem;

  /**
   * Triggered when a rename is requested for an element; wired by the tree controller to the active cell.
   */
  private Runnable startRenameCallback;

  /**
   * Sets the callback that triggers inline rename on the currently selected cell.
   */
  public void setStartRenameCallback(@NonNull Runnable callback) {
    this.startRenameCallback = callback;
  }

  /**
   * Invokes the inline-rename callback if one is registered.
   */
  public void startRename() {
    if (startRenameCallback != null) {
      startRenameCallback.run();
    }
  }

  public DocumentModelActions(@NonNull ProjectItem projectItem, @NonNull ModelRoot modelRoot,
                              @NonNull CommandStack commandStack, @NonNull TreeTableView<ElementViewModel> elementsTreeTable,
                              @NonNull Consumer<Element> onModelChanged) {
    this.projectItem = projectItem;
    this.modelRoot = modelRoot;
    this.commandStack = commandStack;
    this.elementsTreeTable = elementsTreeTable;
    this.onModelChanged = onModelChanged;
  }

  /**
   * Called fresh for every right-click (see {@link DocumentModelElementsTreeController}'s row factory, which
   * builds a new menu per {@code ContextMenuEvent} instead of caching one via {@code TreeTableRow.setContextMenu}
   * in {@code updateItem}), so the "Create Overview Model from Selection" entry below always reflects the
   * tree's actual selection at click time.
   */
  public ContextMenu createContextMenu(@NonNull Element element) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().addAll(createElementMenuItems(element));
    return contextMenu;
  }

  /**
   * Creates a context menu for the root node of the document model tree.
   *
   * <p>Only the four group-producing variants (Group, Attachment, Multi-Select, Include) are
   * offered here, since Field / Validation Rule / Computation Rule require a parent group to
   * land inside and cannot be top-level root children.
   */
  public ContextMenu createRootContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    contextMenu.getItems().add(createAddMenuItem(createMenuItem(StudioBundle.get("document_model_tree.add_group"), createGroupIcon()),
        siblings -> DocumentModelElementFactory.newGroupElement(siblings, modelRoot)));
    contextMenu.getItems().add(createAddMenuItem(createMenuItem(StudioBundle.get("document_model_tree.add_attachment"), Icons.ELEMENT_ATTACHMENT),
        siblings -> DocumentModelElementFactory.newAttachmentElement(siblings, modelRoot)));
    contextMenu.getItems().add(createAddMenuItem(createMenuItem(StudioBundle.get("document_model_tree.add_multi_select"), Icons.ELEMENT_MULTI_SELECT),
        siblings -> DocumentModelElementFactory.newMultiSelectElement(siblings, modelRoot)));

    MenuItem includeItem = createMenuItem(StudioBundle.get("document_model_tree.add_include"), Icons.ELEMENT_INCLUDE);
    includeItem.setOnAction(event -> onAddInclude());
    contextMenu.getItems().add(includeItem);

    return contextMenu;
  }

  private List<MenuItem> createElementMenuItems(@NonNull Element element) {
    List<MenuItem> items = new ArrayList<>();
    if (!new ElementViewModel(element).hasFixedChildren()) {
      items.addAll(createAddMenuItems());
      List<TreeItem<ElementViewModel>> selection = new ArrayList<>(elementsTreeTable.getSelectionModel().getSelectedItems());
      items.add(new SeparatorMenuItem());
      MenuItem overviewModelItem = createMenuItem(StudioBundle.get("document_model_tree.create_overview_model_menu_item"),
          WidgetFactory.createModelIcon(Icons.PNG_MODEL_OVERVIEW));
      overviewModelItem.setOnAction(event -> onCreateOverviewModelFromSelection(selection));
      items.add(overviewModelItem);
      items.add(new SeparatorMenuItem());
    }
    // "Move..." is only offered for plain groups (GroupElements that don't have fixed children,
    // already guaranteed by the outer hasFixedChildren guard). Attachments, multi-selects, and
    // includes are also GroupElements but are excluded by that guard above.
    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null
        && groupElement.getGroup().getIncludeConfig() == null
        && groupElement.getGroup().getUsageType() == null) {
      MenuItem moveItem = createMenuItem(StudioBundle.get("document_model_tree.move"), Icons.ARROW_UP);
      moveItem.setOnAction(event -> onMoveGroup(groupElement));
      items.add(moveItem);
      items.add(new SeparatorMenuItem());
    }
    MenuItem cutItem = createMenuItem(StudioBundle.get("document_model_tree.cut"), Icons.CUT);
    cutItem.setOnAction(event -> cutSelection());
    items.add(cutItem);

    MenuItem copyItem = createMenuItem(StudioBundle.get("document_model_tree.copy"), Icons.COPY);
    copyItem.setOnAction(event -> copySelection());
    items.add(copyItem);

    MenuItem pasteItem = createMenuItem(StudioBundle.get("document_model_tree.paste"), Icons.PASTE);
    pasteItem.setDisable(!hasClipboardContent());
    pasteItem.setOnAction(event -> pasteSelection());
    items.add(pasteItem);
    items.add(new SeparatorMenuItem());

    MenuItem renameItem = createMenuItem(StudioBundle.get("rename"), Icons.PENCIL);
    renameItem.setOnAction(event -> startRename());
    items.add(renameItem);
    items.add(new SeparatorMenuItem());

    MenuItem deleteItem = createMenuItem(StudioBundle.get("delete"), Icons.TRASH);
    deleteItem.setOnAction(event -> confirmAndDeleteSelection());
    items.add(deleteItem);
    return items;
  }

  public List<MenuItem> createAddMenuItems() {
    List<MenuItem> items = new ArrayList<>();
    items.add(createAddMenuItem(createMenuItem(StudioBundle.get("document_model_tree.add_group"), createGroupIcon()),
        siblings -> DocumentModelElementFactory.newGroupElement(siblings, modelRoot)));
    fieldMenuItem = createAddMenuItem(createMenuItem(StudioBundle.get("document_model_tree.add_field"), Icons.ELEMENT_FIELD),
        siblings -> DocumentModelElementFactory.newFieldElement(siblings, modelRoot));
    items.add(fieldMenuItem);
    ruleMenuItem = createAddMenuItem(createMenuItem(StudioBundle.get("document_model_tree.add_validation_rule"), Icons.ELEMENT_VALIDATION_RULE),
        siblings -> DocumentModelElementFactory.newRuleElement(siblings, modelRoot));
    items.add(ruleMenuItem);
    computationMenuItem = createAddMenuItem(createMenuItem(StudioBundle.get("document_model_tree.add_computation_rule"), Icons.ELEMENT_COMPUTATION),
        siblings -> DocumentModelElementFactory.newComputationElement(siblings, modelRoot));
    items.add(computationMenuItem);
    items.add(createAddMenuItem(createMenuItem(StudioBundle.get("document_model_tree.add_attachment"), Icons.ELEMENT_ATTACHMENT),
        siblings -> DocumentModelElementFactory.newAttachmentElement(siblings, modelRoot)));
    items.add(createAddMenuItem(createMenuItem(StudioBundle.get("document_model_tree.add_multi_select"), Icons.ELEMENT_MULTI_SELECT),
        siblings -> DocumentModelElementFactory.newMultiSelectElement(siblings, modelRoot)));

    MenuItem includeItem = createMenuItem(StudioBundle.get("document_model_tree.add_include"), Icons.ELEMENT_INCLUDE);
    includeItem.setOnAction(event -> onAddInclude());
    items.add(includeItem);

    updateAddMenuItemsState();
    return items;
  }

  /**
   * Greys out Field/Validation Rule/Computation Rule whenever nothing is selected - unlike
   * Group/Attachment/Multi-Select/Include (see {@link #resolveInsertionPointForAdd}), they have no valid
   * target to insert into when the tree is empty. Called after building the menu and on every selection
   * change (see {@code DocumentModelElementsTreeController#updateEditingButtonsState}).
   */
  public void updateAddMenuItemsState() {
    boolean hasSelection = hasSelection();
    fieldMenuItem.setDisable(!hasSelection);
    ruleMenuItem.setDisable(!hasSelection);
    computationMenuItem.setDisable(!hasSelection);
  }

  private boolean hasSelection() {
    TreeItem<ElementViewModel> selectedItem = elementsTreeTable.getSelectionModel().getSelectedItem();
    return selectedItem != null && selectedItem.getValue() != null;
  }

  private MenuItem createAddMenuItem(@NonNull MenuItem menuItem, @NonNull Function<List<Element>, Element> elementFactory) {
    menuItem.setOnAction(event -> onAddElement(elementFactory));
    return menuItem;
  }

  private void onAddElement(@NonNull Function<List<Element>, Element> elementFactory) {
    InsertionPoint insertionPoint = resolveInsertionPointForAdd();
    if (insertionPoint == null) {
      return;
    }

    Element newElement = elementFactory.apply(insertionPoint.siblings());
    String name = promptElementName(newElement.getName());
    if (name == null) {
      return;
    }
    newElement.setName(name);

    commandStack.execute(new AddNodeCommand<>(insertionPoint.siblings(), newElement, insertionPoint.index()));
    onModelChanged.accept(newElement);
  }

  /**
   * Unlike every other element type (added via {@link #onAddElement}, then optionally renamed), a new
   * Include must have its referenced {@link de.a12.studio.models.documentmodel.DocumentModel} picked up
   * front: the {@link IncludeDialogController} dialog requires both a valid name and a reference selection
   * before it can be submitted, so the Include this creates is never left in the "Missing Include Reference"
   * state that an unset reference would otherwise cause (see {@link
   * de.a12.studio.modelsvalidation.validators.MissingReferenceValidator}).
   */
  private void onAddInclude() {
    InsertionPoint insertionPoint = resolveInsertionPointForAdd();
    if (insertionPoint == null) {
      return;
    }

    Element newElement = DocumentModelElementFactory.newIncludeElement(insertionPoint.siblings(), modelRoot);

    Project project = Studio.getCurrentProject();
    Optional<IncludeDialogController.IncludeInput> input = Dialogs.showInclude(
        Studio.stage, project, (DocumentModel) projectItem.getModel(), newElement.getName());
    if (input.isEmpty()) {
      return;
    }

    newElement.setName(input.get().name());
    ((GroupElement) newElement).getGroup().getIncludeConfig().setReference(input.get().reference());

    commandStack.execute(new AddNodeCommand<>(insertionPoint.siblings(), newElement, insertionPoint.index()));
    onModelChanged.accept(newElement);
  }

  /**
   * Opens the Move Group dialog and, once confirmed, performs the move:
   * <ol>
   *   <li>Removes the {@code group} from this model's element list (root groups or its parent group's children).</li>
   *   <li>Adds an include element in its place, referencing the target Document Model.</li>
   *   <li>Appends the group at the end of the target Document Model's root groups.</li>
   *   <li>Saves both models and fires save events so the tree refreshes.</li>
   * </ol>
   * When the user opts to create a new Document Model, the new model is created on disk first via
   * {@link NewModelFactory#createModel}, then the group is transferred into its root.
   */
  @SuppressWarnings("unchecked")
  private void onMoveGroup(@NonNull GroupElement group) {
    Optional<MoveGroupDialogController.MoveTarget> targetOpt =
        MoveGroupDialogController.show(Studio.stage, projectItem, group.getName());
    if (targetOpt.isEmpty()) {
      return;
    }

    MoveGroupDialogController.MoveTarget target = targetOpt.get();

    // Resolve or create the target Document Model and its backing ProjectItem.
    ProjectItem targetProjectItem;
    DocumentModel targetDocumentModel;
    try {
      if (target instanceof MoveGroupDialogController.MoveTarget.ExistingModel existing) {
        targetDocumentModel = existing.documentModel();
        targetProjectItem = ProjectDocumentModels
            .findProjectItemByModelId(targetDocumentModel.getId())
            .orElse(null);
        if (targetProjectItem == null) {
          WidgetFactory.showAlert(Studio.stage,
              StudioBundle.get("move_group_dialog.could_not_find_target_model"));
          return;
        }
      } else {
        MoveGroupDialogController.MoveTarget.NewModel newModel =
            (MoveGroupDialogController.MoveTarget.NewModel) target;
        targetProjectItem = NewModelFactory.createModel(
            newModel.folder(), ModelType.DOCUMENT, newModel.modelName());
        targetDocumentModel = (DocumentModel) targetProjectItem.getModel();
        if (!newModel.locales().isEmpty()) {
          targetDocumentModel.setLocales(newModel.locales());
        }
        if (!newModel.roles().isEmpty()) {
          RolesEditorPanelController.applyRoles(targetDocumentModel, newModel.roles());
        }
      }
    } catch (IOException e) {
      WidgetFactory.showAlert(Studio.stage,
          StudioBundle.get("move_group_dialog.could_not_create_model"), e.getMessage());
      return;
    }

    // 1. Remove the group from the source model's element list.
    TreeItem<ElementViewModel> groupTreeItem = findTreeItemForGroup(group);
    List<? extends Element> sourceList = resolveSourceList(groupTreeItem);
    if (sourceList == null) {
      WidgetFactory.showAlert(Studio.stage,
          StudioBundle.get("move_group_dialog.could_not_determine_source"));
      return;
    }
    ((List<Element>) sourceList).remove(group);

    // 2. Insert an include element in place of the removed group (appended after remaining siblings).
    // Cross-model operations are not undoable, so we skip the command stack here.
    GroupElement include = buildIncludeFor(group.getName(), targetDocumentModel.getId());
    ((List<Element>) sourceList).add(include);

    // 3. Append the group to the target model's root groups.
    DocumentModelElementFactory.regenerateIds(group, targetDocumentModel.getContent().getModelRoot());
    targetDocumentModel.getContent().getModelRoot().getRootGroups().add(group);

    // 4. Save both models and fire events.
    targetProjectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(targetProjectItem);

    onModelChanged.accept(null);
  }

  /**
   * Finds the tree item in the current tree that holds {@code group}, or {@code null} if not found.
   */
  private TreeItem<ElementViewModel> findTreeItemForGroup(@NonNull GroupElement group) {
    return findTreeItemById(elementsTreeTable.getRoot(), group.getId());
  }

  private TreeItem<ElementViewModel> findTreeItemById(TreeItem<ElementViewModel> node, @NonNull String id) {
    if (node == null) return null;
    if (node.getValue() != null && id.equals(node.getValue().getElement().getId())) return node;
    for (TreeItem<ElementViewModel> child : node.getChildren()) {
      TreeItem<ElementViewModel> found = findTreeItemById(child, id);
      if (found != null) return found;
    }
    return null;
  }

  /**
   * Returns the list that directly contains the group's element: either {@link ModelRoot#getRootGroups()}
   * when the group is a top-level root group, or its parent group's element list otherwise.
   */
  private List<? extends Element> resolveSourceList(TreeItem<ElementViewModel> groupItem) {
    if (groupItem == null) return null;
    TreeItem<ElementViewModel> parentItem = groupItem.getParent();
    if (parentItem == null || parentItem.getValue() == null) {
      return modelRoot.getRootGroups();
    }
    Element parentElement = parentItem.getValue().getElement();
    if (parentElement instanceof GroupElement parentGroup && parentGroup.getGroup() != null) {
      return parentGroup.getGroup().getElements();
    }
    return null;
  }

  /**
   * Creates a new include {@link GroupElement} referencing {@code targetModelId}, using {@code name}
   * as its display name so the tree shows the original group's name after the move.
   */
  private GroupElement buildIncludeFor(@NonNull String name, @NonNull String targetModelId) {
    GroupElement include = new GroupElement();
    include.setId("ig-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
    include.setName(name);
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    IncludeConfig includeConfig = new IncludeConfig();
    includeConfig.setReference(targetModelId);
    config.setIncludeConfig(includeConfig);
    include.setGroup(config);
    return include;
  }

  /**
   * Opens {@link Dialogs#showCreateOverviewModel} for {@code selection} (the tree's current multi-selection,
   * required by the caller to have more than one item) and, once confirmed, creates a new Overview Model in
   * this Document Model's own folder with one Column per checked field, in {@link #collectFields}'s order.
   */
  private void onCreateOverviewModelFromSelection(@NonNull List<TreeItem<ElementViewModel>> selection) {
    DocumentModel documentModel = (DocumentModel) projectItem.getModel();
    ElementIndex index = new ElementIndex(documentModel);
    List<FieldElement> fields = collectFields(selection);
    List<FieldOption> fieldOptions = fields.stream().map(field -> new FieldOption(field.getId(), index.getPath(field))).toList();

    ProjectItem targetFolder = projectItem.getParent();
    Optional<Result> input = Dialogs.showCreateOverviewModel(Studio.stage, targetFolder, documentModel, fieldOptions,
        defaultOverviewModelName(documentModel.getId()));
    if (input.isEmpty()) {
      return;
    }

    try {
      ProjectItem selectedFolder = input.get().folder();
      ProjectItem newItem = NewModelFactory.createModel(selectedFolder, ModelType.OVERVIEW, input.get().name(), documentModel.getId());
      OverviewModel overviewModel = (OverviewModel) newItem.getModel();
      for (String fieldId : input.get().selectedFieldIds()) {
        Column column = new Column();
        column.setId("column-" + shortId());
        column.setWidth(1.0);
        column.setElementRef(fieldId);
        overviewModel.getContent().getColumns().add(column);
      }
      if (!input.get().locales().isEmpty()) {
        overviewModel.setLocales(input.get().locales());
      }
      if (!input.get().roles().isEmpty()) {
        RolesEditorPanelController.applyRoles(overviewModel, input.get().roles());
      }
      newItem.save();
      StudioEventManager.getInstance().fireModelSavedEvent(newItem);
    }
    catch (IOException e) {
      WidgetFactory.showAlert(Studio.stage, StudioBundle.get("could_not_create_item", input.get().name()), e.getMessage());
    }
  }

  /**
   * Every {@link FieldElement} reachable from {@code selection}: a directly selected field is included as-is,
   * a directly selected {@link GroupElement} contributes every field nested under it (recursively, so a
   * sub-group's fields are included too), and everything else (rules, computations, attachments/multi-select
   * as a whole) contributes nothing. Selections are deduplicated by id and first collapsed to their top-level
   * items via {@link #topLevelSelection} so a field selected both directly and as part of a selected ancestor
   * group isn't counted twice.
   */
  private List<FieldElement> collectFields(@NonNull List<TreeItem<ElementViewModel>> selection) {
    List<FieldElement> fields = new ArrayList<>();
    Set<String> seenIds = new LinkedHashSet<>();
    for (TreeItem<ElementViewModel> item : topLevelSelection(selection)) {
      collectFields(item.getValue().getElement(), fields, seenIds);
    }
    return fields;
  }

  private void collectFields(@NonNull Element element, @NonNull List<FieldElement> fields, @NonNull Set<String> seenIds) {
    if (element instanceof FieldElement fieldElement) {
      if (seenIds.add(fieldElement.getId())) {
        fields.add(fieldElement);
      }
    }
    else if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      for (Element child : groupElement.getGroup().getElements()) {
        collectFields(child, fields, seenIds);
      }
    }
  }

  // Mirrors the "<Base>_DM" -> "<Base>_OM" naming convention used across testing/basic's fixture models
  // (e.g. Company_DM.json / Company_OM.json).
  private static String defaultOverviewModelName(@NonNull String documentModelId) {
    String base = documentModelId.endsWith("_DM") ? documentModelId.substring(0, documentModelId.length() - 3) : documentModelId;
    return base + "_OM";
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }

  /**
   * Asks for the new element's name, pre-filled with the factory's auto-generated name, looping
   * until the entered name is a valid whitespace-free filename or the user cancels.
   */
  private String promptElementName(@NonNull String defaultName) {
    String name = defaultName;
    while (true) {
      name = WidgetFactory.showInputDialog(Studio.stage, "New Element", "Name", null, null, name);
      if (name == null) {
        return null;
      }
      name = name.trim();
      if (FileUtils.isValidWindowsFilename(name)) {
        return name;
      }
      WidgetFactory.showAlert(Studio.stage, StudioBundle.get("please_enter_a_valid_name_without_whitespace"));
    }
  }

  /**
   * Where a new element should land: as the last child of the selected group, or as a sibling
   * directly after the selected element if a non-group (leaf) element is selected.
   */
  private record InsertionPoint(List<Element> siblings, int index) {

  }

  /**
   * Like {@link #resolveInsertionPoint}, but falls back to the end of {@link ModelRoot#getRootGroups()} when
   * nothing is selected - which happens whenever the tree is empty, since there is then nothing to select.
   * Only reachable for Group/Attachment/Multi-Select/Include (see {@link #updateAddMenuItemsState}, which
   * disables Field/Validation Rule/Computation Rule in that state), all of which produce a {@link GroupElement}
   * valid in the root list.
   */
  private InsertionPoint resolveInsertionPointForAdd() {
    TreeItem<ElementViewModel> selectedItem = elementsTreeTable.getSelectionModel().getSelectedItem();
    if (selectedItem == null || selectedItem.getValue() == null) {
      return rootInsertionPoint();
    }
    return resolveInsertionPoint(selectedItem);
  }

  @SuppressWarnings("unchecked")
  private InsertionPoint rootInsertionPoint() {
    // Same List<GroupElement> -> List<Element> unchecked widening as MoveNodeCommand's constructor, for the
    // same reason: ModelRoot#getRootGroups() is typed narrower than the generic siblings list AddNodeCommand needs.
    List<Element> rootGroups = (List<Element>) (List<?>) modelRoot.getRootGroups();
    return new InsertionPoint(rootGroups, rootGroups.size());
  }

  private InsertionPoint resolveInsertionPoint(@NonNull TreeItem<ElementViewModel> selectedItem) {
    Element selected = selectedItem.getValue().getElement();
    if (selected instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      List<Element> siblings = groupElement.getGroup().getElements();
      return new InsertionPoint(siblings, siblings.size());
    }

    TreeItem<ElementViewModel> parentItem = selectedItem.getParent();
    if (parentItem == null || parentItem.getValue() == null) {
      return null;
    }
    Element parentElement = parentItem.getValue().getElement();
    if (parentElement instanceof GroupElement parentGroup && parentGroup.getGroup() != null) {
      List<Element> siblings = parentGroup.getGroup().getElements();
      return new InsertionPoint(siblings, siblings.indexOf(selected) + 1);
    }
    return null;
  }

  /**
   * Whether {@link #pasteSelection()} currently has anything to paste, for the toolbar Paste button's disabled state.
   */
  public boolean hasClipboardContent() {
    return !clipboardJson.isEmpty();
  }

  /**
   * Copies the current top-level selection to the clipboard (see {@link #selectionForClipboard()}), leaving
   * the tree unchanged.
   */
  public void copySelection() {
    List<Element> elements = selectionForClipboard();
    if (elements.isEmpty()) {
      return;
    }
    copyToClipboard(elements);
  }

  /**
   * Copies the current top-level selection to the clipboard, then deletes it the same way {@link
   * #confirmAndDeleteSelection()} does - but without a confirmation prompt, matching standard Cut behavior
   * (the removed elements are still recoverable via Paste or Undo).
   */
  public void cutSelection() {
    List<Element> elements = selectionForClipboard();
    if (elements.isEmpty()) {
      return;
    }
    copyToClipboard(elements);
    onDeleteModelItem();
  }

  /**
   * Pastes every clipboard entry as a fresh clone - with a regenerated id (recursively, see {@link
   * DocumentModelElementFactory#regenerateIds}) and a name made unique within the destination siblings (see
   * {@link DocumentModelElementFactory#uniqueName}, so pasting back into the group it was copied from renames
   * rather than collides) - at the same insertion point {@link #onAddElement} would use for a new element:
   * as the last child of a selected group, or as a sibling directly after a selected leaf element. Each pasted
   * top-level element becomes its own undo step, mirroring how {@link #onDeleteModelItem()} handles a
   * multi-element deletion.
   */
  public void pasteSelection() {
    if (clipboardJson.isEmpty()) {
      return;
    }
    TreeItem<ElementViewModel> selectedItem = elementsTreeTable.getSelectionModel().getSelectedItem();
    if (selectedItem == null || selectedItem.getValue() == null) {
      return;
    }
    InsertionPoint insertionPoint = resolveInsertionPoint(selectedItem);
    if (insertionPoint == null) {
      return;
    }

    Element firstPasted = null;
    int index = insertionPoint.index();
    for (String json : clipboardJson) {
      Element clone = cloneFromClipboard(json);
      if (clone == null) {
        continue;
      }
      DocumentModelElementFactory.regenerateIds(clone, modelRoot);
      clone.setName(DocumentModelElementFactory.uniqueName(clone.getName(), insertionPoint.siblings()));
      commandStack.execute(new AddNodeCommand<>(insertionPoint.siblings(), clone, index));
      index++;
      if (firstPasted == null) {
        firstPasted = clone;
      }
    }
    if (firstPasted != null) {
      onModelChanged.accept(firstPasted);
    }
  }

  /**
   * The tree's current top-level selection (see {@link #topLevelSelection}), skipping any element within a
   * fixed-children group (attachment/multi-select/include) the same way {@link #onDeleteModelItem()} does -
   * those belong to a fixed set of children or, for an Include, to another Document Model's own element list.
   */
  private List<Element> selectionForClipboard() {
    List<TreeItem<ElementViewModel>> selection =
        new ArrayList<>(elementsTreeTable.getSelectionModel().getSelectedItems());
    selection.removeIf(this::hasFixedChildrenAncestor);
    List<Element> elements = new ArrayList<>();
    for (TreeItem<ElementViewModel> treeItem : topLevelSelection(selection)) {
      elements.add(treeItem.getValue().getElement());
    }
    return elements;
  }

  private static void copyToClipboard(@NonNull List<Element> elements) {
    try {
      List<String> json = new ArrayList<>();
      for (Element element : elements) {
        json.add(JsonSettings.objectMapper.writeValueAsString(element));
      }
      clipboardJson = json;
    }
    catch (Exception e) {
      log.warn("Failed to copy document model element(s) to clipboard: {}", e.getMessage(), e);
    }
  }

  private static Element cloneFromClipboard(@NonNull String json) {
    try {
      return JsonSettings.objectMapper.readValue(json, Element.class);
    }
    catch (Exception e) {
      log.warn("Failed to paste document model element: {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * Always confirms before deleting, regardless of the entry point (toolbar button, context menu,
   * Delete key), warning separately when child elements would be removed along with the selection.
   */
  public void confirmAndDeleteSelection() {
    List<TreeItem<ElementViewModel>> selection =
        new ArrayList<>(elementsTreeTable.getSelectionModel().getSelectedItems());
    if (selection.isEmpty()) {
      return;
    }

    boolean hasChildren = topLevelSelection(selection).stream().anyMatch(treeItem -> !treeItem.getChildren().isEmpty());
    String help = hasChildren ? "Child elements will be deleted as well." : null;
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        StudioBundle.get("delete_the_selected_element_s_confirm"), help, null, StudioBundle.get("delete"));
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }

    onDeleteModelItem();
  }

  private void onDeleteModelItem() {
    List<TreeItem<ElementViewModel>> selection =
        new ArrayList<>(elementsTreeTable.getSelectionModel().getSelectedItems());
    // An Include's resolved children (see ElementViewModel#getChildren) belong to the referenced Document
    // Model's own element lists, not this model's - deleting one would silently mutate that other model's
    // in-memory graph instead of this one, so they're skipped here regardless of how the deletion was
    // triggered (toolbar button, Delete key, context menu).
    selection.removeIf(this::hasFixedChildrenAncestor);
    for (TreeItem<ElementViewModel> treeItem : topLevelSelection(selection)) {
      Command command = createDeleteCommand(treeItem);
      if (command != null) {
        commandStack.execute(command);
      }
    }

    onModelChanged.accept(null);
  }

  private boolean hasFixedChildrenAncestor(@NonNull TreeItem<ElementViewModel> treeItem) {
    TreeItem<ElementViewModel> parent = treeItem.getParent();
    while (parent != null && parent.getValue() != null) {
      if (new ElementViewModel(parent.getValue().getElement()).hasFixedChildren()) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }

  private List<TreeItem<ElementViewModel>> topLevelSelection(@NonNull List<TreeItem<ElementViewModel>> selection) {
    List<TreeItem<ElementViewModel>> result = new ArrayList<>();
    for (TreeItem<ElementViewModel> treeItem : selection) {
      if (treeItem != null && !hasSelectedAncestor(treeItem, selection)) {
        result.add(treeItem);
      }
    }
    return result;
  }

  private boolean hasSelectedAncestor(@NonNull TreeItem<ElementViewModel> treeItem,
                                      @NonNull List<TreeItem<ElementViewModel>> selection) {
    TreeItem<ElementViewModel> ancestor = treeItem.getParent();
    while (ancestor != null) {
      if (selection.contains(ancestor)) {
        return true;
      }
      ancestor = ancestor.getParent();
    }
    return false;
  }

  private Command createDeleteCommand(@NonNull TreeItem<ElementViewModel> treeItem) {
    Element element = treeItem.getValue().getElement();
    TreeItem<ElementViewModel> parentItem = treeItem.getParent();
    if (parentItem == null || parentItem.getValue() == null) {
      return new DeleteNodeCommand<>(modelRoot.getRootGroups(), (GroupElement) element);
    }

    Element parentElement = parentItem.getValue().getElement();
    if (parentElement instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      return new DeleteNodeCommand<>(groupElement.getGroup().getElements(), element);
    }
    return null;
  }

  private MenuItem createMenuItem(@NonNull String text, @NonNull String icon) {
    MenuItem menuItem = new MenuItem(text);
    FontIcon fontIcon = WidgetFactory.createIcon(icon);
    fontIcon.getStyleClass().add("menu-icon");
    menuItem.setGraphic(fontIcon);
    return menuItem;
  }

  private MenuItem createMenuItem(@NonNull String text, @NonNull Node icon) {
    MenuItem menuItem = new MenuItem(text);
    icon.getStyleClass().add("menu-icon");
    menuItem.setGraphic(icon);
    return menuItem;
  }

  private Node createGroupIcon() {
    return WidgetFactory.createIcon(Icons.ELEMENT_GROUP);
  }
}
