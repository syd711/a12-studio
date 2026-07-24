package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.services.documentmodel.features.validation.DMValidationService;
import de.a12.studio.dataservices.services.documentmodel.features.validation.ElementValidationError;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.documentmodel.commands.AddNodeCommand;
import de.a12.studio.ui.editors.documentmodel.commands.DeleteNodeCommand;
import de.a12.studio.ui.events.ElementValidatedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.commandstack.Command;
import de.a12.studio.ui.util.commandstack.CommandStack;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class DocumentModelElementsTreeController implements Initializable, StudioEventListener {

  private static final DMValidationService VALIDATION_SERVICE = new DMValidationService();

  private static final String TABLE_SETTINGS_ID = ModelType.DOCUMENT.getValue();

  private static final String NAME_COLUMN_ID = "name";

  private static final String TYPE_COLUMN_ID = "type";

  @FXML
  private ToolBar modelTreeToolbarBar;

  @FXML
  private Button undoButton;

  @FXML
  private Button redoButton;

  @FXML
  private MenuButton modelTreeAddButton;

  @FXML
  private Button deleteButton;

  @FXML
  private SearchFieldController searchController;

  @FXML
  private TreeTableView<ElementViewModel> elementsTreeTable;

  @FXML
  private TreeTableColumn<ElementViewModel, String> nameColumn;

  @FXML
  private TreeTableColumn<ElementViewModel, String> typeColumn;

  private ProjectItem projectItem;
  private ModelRoot modelRoot;

  private final CommandStack commandStack = new CommandStack();

  private Consumer<List<Element>> selectionListener;

  public void load(@NonNull DocumentModel model) {
    load(projectItem, model.getContent().getModelRoot());
  }

  public void setSelectionListener(Consumer<List<Element>> selectionListener) {
    this.selectionListener = selectionListener;
  }

  public void load(ProjectItem projectItem, @NonNull ModelRoot modelRoot) {
    this.projectItem = projectItem;
    this.modelRoot = modelRoot;
    applyFilter(searchController.getText());
    StudioEventManager.getInstance().addListener(this);
  }

  @Override
  public void elementValidated(@NonNull ElementValidatedEvent event) {
    TreeItem<ElementViewModel> treeItem = findTreeItem(elementsTreeTable.getRoot(), event.getElementId());
    if (treeItem == null) {
      return;
    }
    treeItem.getValue().setHasError(event.getError().isPresent());
    elementsTreeTable.refresh();
  }

  public List<Element> getAncestors(@NonNull Element element) {
    List<Element> ancestors = new ArrayList<>();
    TreeItem<ElementViewModel> treeItem = findTreeItem(elementsTreeTable.getRoot(), element.getId());
    if (treeItem == null) {
      return ancestors;
    }

    TreeItem<ElementViewModel> parent = treeItem.getParent();
    while (parent != null && parent.getValue() != null) {
      ancestors.add(0, parent.getValue().getElement());
      parent = parent.getParent();
    }
    return ancestors;
  }

  private TreeItem<ElementViewModel> findTreeItem(TreeItem<ElementViewModel> treeItem, @NonNull String elementId) {
    if (treeItem == null) {
      return null;
    }
    if (treeItem.getValue() != null && elementId.equals(treeItem.getValue().getElement().getId())) {
      return treeItem;
    }
    for (TreeItem<ElementViewModel> child : treeItem.getChildren()) {
      TreeItem<ElementViewModel> found = findTreeItem(child, elementId);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  @FXML
  private void onUndo() {
    commandStack.undo();
    updateUndoRedoState();
    applyFilter(searchController.getText());
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
  }

  @FXML
  private void onRedo() {
    commandStack.redo();
    updateUndoRedoState();
    applyFilter(searchController.getText());
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
  }

  private void updateUndoRedoState() {
    undoButton.setDisable(!commandStack.canUndo());
    redoButton.setDisable(!commandStack.canRedo());
  }

  private void notifySelectionChanged() {
    if (selectionListener == null) {
      return;
    }

    List<Element> selectedElements = new ArrayList<>();
    for (TreeItem<ElementViewModel> treeItem : elementsTreeTable.getSelectionModel().getSelectedItems()) {
      if (treeItem != null && treeItem.getValue() != null) {
        selectedElements.add(treeItem.getValue().getElement());
      }
    }
    selectionListener.accept(selectedElements);
  }

  private void applyFilter(String filter) {
    if (modelRoot == null) {
      return;
    }

    String term = filter == null ? "" : filter.trim().toLowerCase();
    TreeItem<ElementViewModel> root = new TreeItem<>();
    for (GroupElement group : modelRoot.getRootGroups()) {
      TreeItem<ElementViewModel> treeItem = term.isEmpty() ? toTreeItem(group) : toFilteredTreeItem(group, term);
      if (treeItem != null) {
        root.getChildren().add(treeItem);
      }
    }
    elementsTreeTable.setRoot(root);
    applyValidationState(root);
    expandAll(root);
  }

  /**
   * Re-validates the whole document and marks each errored element's tree row, so structural changes made
   * elsewhere in the tree (e.g. deleting a field another group uses as its index field, undo/redo, initial
   * load of a document that was already invalid on disk) are reflected immediately. This rebuilds every
   * {@link ElementViewModel} in the tree (see {@link #toTreeItem}), which would otherwise always start out
   * with {@code hasError == false} until the next individual field edit re-triggers {@link #elementValidated}.
   */
  private void applyValidationState(@NonNull TreeItem<ElementViewModel> root) {
    Set<String> erroredElementIds = erroredElementIds();
    markErrors(root, erroredElementIds);
  }

  private Set<String> erroredElementIds() {
    if (!(projectItem.getModel() instanceof DocumentModel documentModel)) {
      return Set.of();
    }
    try {
      List<ElementValidationError> errors =
          VALIDATION_SERVICE.validateDocument(documentModel, ProjectDocumentModels.getOtherDocumentModels(projectItem));
      Set<String> ids = new HashSet<>();
      for (ElementValidationError error : errors) {
        if (error.elementId() != null) {
          ids.add(error.elementId());
        }
      }
      return ids;
    }
    catch (Exception e) {
      log.warn("Failed to validate document '{}': {}", projectItem.getPath(), e.getMessage(), e);
      return Set.of();
    }
  }

  private void markErrors(@NonNull TreeItem<ElementViewModel> treeItem, @NonNull Set<String> erroredElementIds) {
    if (treeItem.getValue() != null) {
      treeItem.getValue().setHasError(erroredElementIds.contains(treeItem.getValue().getElement().getId()));
    }
    for (TreeItem<ElementViewModel> child : treeItem.getChildren()) {
      markErrors(child, erroredElementIds);
    }
  }

  private void expandAll(@NonNull TreeItem<ElementViewModel> treeItem) {
    treeItem.setExpanded(true);
    for (TreeItem<ElementViewModel> child : treeItem.getChildren()) {
      expandAll(child);
    }
  }

  private TreeItem<ElementViewModel> toTreeItem(@NonNull Element element) {
    ElementViewModel viewModel = new ElementViewModel(element);
    TreeItem<ElementViewModel> treeItem = new TreeItem<>(viewModel);
    for (ElementViewModel child : viewModel.getChildren()) {
      treeItem.getChildren().add(toTreeItem(child.getElement()));
    }
    return treeItem;
  }

  private TreeItem<ElementViewModel> toFilteredTreeItem(@NonNull Element element, @NonNull String term) {
    ElementViewModel viewModel = new ElementViewModel(element);
    List<TreeItem<ElementViewModel>> matchingChildren = new ArrayList<>();
    for (ElementViewModel child : viewModel.getChildren()) {
      TreeItem<ElementViewModel> filteredChild = toFilteredTreeItem(child.getElement(), term);
      if (filteredChild != null) {
        matchingChildren.add(filteredChild);
      }
    }

    boolean selfMatches = viewModel.getName() != null && viewModel.getName().toLowerCase().contains(term);
    if (!selfMatches && matchingChildren.isEmpty()) {
      return null;
    }

    TreeItem<ElementViewModel> treeItem = new TreeItem<>(viewModel);
    treeItem.getChildren().addAll(matchingChildren);
    return treeItem;
  }

  private ContextMenu createContextMenu(@NonNull Element element) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().addAll(createElementMenuItems(element));
    return contextMenu;
  }

  /**
   * Whether {@code element} is a group with fixed children (attachment, multi-select), or a descendant
   * of one. Such groups have a fixed set of children, so nothing may be added inside them.
   */
  private boolean isWithinFixedChildrenGroup(@NonNull Element element) {
    return new ElementViewModel(element).hasFixedChildren() || hasFixedChildrenAncestor(element);
  }

  /**
   * Whether any ancestor of {@code element} (not {@code element} itself) is a group with fixed
   * children (attachment, multi-select).
   */
  private boolean hasFixedChildrenAncestor(@NonNull Element element) {
    for (Element ancestor : getAncestors(element)) {
      if (new ElementViewModel(ancestor).hasFixedChildren()) {
        return true;
      }
    }
    return false;
  }

  private List<MenuItem> createElementMenuItems(@NonNull Element element) {
    List<MenuItem> items = new ArrayList<>();
    if (!new ElementViewModel(element).hasFixedChildren()) {
      items.addAll(createAddMenuItems());
      items.add(new SeparatorMenuItem());
    }
    items.add(createMenuItem("_Cut", Icons.CUT));
    items.add(createMenuItem("Cop_y", Icons.COPY));
    items.add(createMenuItem("_Paste", Icons.PASTE));
    items.add(new SeparatorMenuItem());
    MenuItem deleteItem = createMenuItem("_Delete", Icons.TRASH);
    deleteItem.setOnAction(event -> confirmAndDeleteSelection());
    items.add(deleteItem);
    return items;
  }

  private void onDeleteKeyPressed() {
    confirmAndDeleteSelection();
  }

  @FXML
  private void onDeleteButton() {
    confirmAndDeleteSelection();
  }

  /**
   * Always confirms before deleting, regardless of the entry point (toolbar button, context menu,
   * Delete key), warning separately when child elements would be removed along with the selection.
   */
  private void confirmAndDeleteSelection() {
    List<TreeItem<ElementViewModel>> selection =
        new ArrayList<>(elementsTreeTable.getSelectionModel().getSelectedItems());
    if (selection.isEmpty()) {
      return;
    }

    boolean hasChildren = topLevelSelection(selection).stream().anyMatch(treeItem -> !treeItem.getChildren().isEmpty());
    String help = hasChildren ? "Child elements will be deleted as well." : null;
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        "Delete the selected element(s)?", help, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }

    onDeleteModelItem();
  }

  private void onDeleteModelItem() {
    List<TreeItem<ElementViewModel>> selection =
        new ArrayList<>(elementsTreeTable.getSelectionModel().getSelectedItems());
    for (TreeItem<ElementViewModel> treeItem : topLevelSelection(selection)) {
      Command command = createDeleteCommand(treeItem);
      if (command != null) {
        commandStack.execute(command);
      }
    }

    updateUndoRedoState();
    applyFilter(searchController.getText());
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
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

  private List<MenuItem> createAddMenuItems() {
    List<MenuItem> items = new ArrayList<>();
    items.add(createAddMenuItem(createMenuItem("_Group", createGroupIcon()),
        siblings -> DocumentModelElementFactory.newGroupElement(siblings, modelRoot)));
    items.add(createAddMenuItem(createMenuItem("_Field", Icons.ELEMENT_FIELD),
        siblings -> DocumentModelElementFactory.newFieldElement(siblings, modelRoot)));
    items.add(createAddMenuItem(createMenuItem("_Validation Rule", Icons.ELEMENT_VALIDATION_RULE),
        siblings -> DocumentModelElementFactory.newRuleElement(siblings, modelRoot)));
    items.add(createAddMenuItem(createMenuItem("Co_mputation Rule", Icons.ELEMENT_COMPUTATION),
        siblings -> DocumentModelElementFactory.newComputationElement(siblings, modelRoot)));
    items.add(createAddMenuItem(createMenuItem("_Attachment", Icons.ELEMENT_ATTACHMENT),
        siblings -> DocumentModelElementFactory.newAttachmentElement(siblings, modelRoot)));
    items.add(createAddMenuItem(createMenuItem("Multi-_Select", Icons.ELEMENT_MULTI_SELECT),
        siblings -> DocumentModelElementFactory.newMultiSelectElement(siblings, modelRoot)));
    items.add(createAddMenuItem(createMenuItem("_Include", Icons.ELEMENT_INCLUDE),
        siblings -> DocumentModelElementFactory.newIncludeElement(siblings, modelRoot)));
    return items;
  }

  private MenuItem createAddMenuItem(@NonNull MenuItem menuItem, @NonNull Function<List<Element>, Element> elementFactory) {
    menuItem.setOnAction(event -> onAddElement(elementFactory));
    return menuItem;
  }

  private void onAddElement(@NonNull Function<List<Element>, Element> elementFactory) {
    TreeItem<ElementViewModel> selectedItem = elementsTreeTable.getSelectionModel().getSelectedItem();
    if (selectedItem == null || selectedItem.getValue() == null) {
      return;
    }

    InsertionPoint insertionPoint = resolveInsertionPoint(selectedItem);
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

    updateUndoRedoState();
    applyFilter(searchController.getText());
    selectElement(newElement);
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
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
      WidgetFactory.showAlert(Studio.stage, "Please enter a valid name without whitespace.");
    }
  }

  /**
   * Where a new element should land: as the last child of the selected group, or as a sibling
   * directly after the selected element if a non-group (leaf) element is selected.
   */
  private record InsertionPoint(List<Element> siblings, int index) {

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

  private void selectElement(@NonNull Element element) {
    TreeItem<ElementViewModel> treeItem = findTreeItem(elementsTreeTable.getRoot(), element.getId());
    if (treeItem == null) {
      return;
    }
    elementsTreeTable.getSelectionModel().clearSelection();
    elementsTreeTable.getSelectionModel().select(treeItem);
    int row = elementsTreeTable.getRow(treeItem);
    if (row >= 0) {
      elementsTreeTable.scrollTo(row);
    }
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

  private void applyColumnWidth(@NonNull TreeTableColumn<ElementViewModel, String> column,
                                BaseTableSettings tableSettings, @NonNull String columnId) {
    if (tableSettings == null) {
      return;
    }
    double width = tableSettings.getColumnWidth(columnId);
    if (width > 0) {
      column.setPrefWidth(width);
    }
  }

  private void saveColumnWidth(@NonNull String columnId, double width) {
    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    if (tableSettings == null) {
      return;
    }
    tableSettings.getColumnWith().put(columnId, width);
    tableSettings.save();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    modelTreeAddButton.setDisable(true);
    deleteButton.setDisable(true);
    updateUndoRedoState();
    searchController.setOnSearch(this::applyFilter);

    elementsTreeTable.setShowRoot(false);
    elementsTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    elementsTreeTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ElementViewModel>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<ElementViewModel>> observable, TreeItem<ElementViewModel> oldValue, TreeItem<ElementViewModel> newValue) {
        modelTreeAddButton.setDisable(newValue == null || isWithinFixedChildrenGroup(newValue.getValue().getElement()));
        deleteButton.setDisable(newValue == null);
      }
    });
    elementsTreeTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<ElementViewModel>>) change -> notifySelectionChanged());
    elementsTreeTable.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        onDeleteKeyPressed();
      }
    });
    elementsTreeTable.setRowFactory(treeTable -> new TreeTableRow<>() {
      @Override
      protected void updateItem(ElementViewModel item, boolean empty) {
        super.updateItem(item, empty);
        setContextMenu(empty || item == null || hasFixedChildrenAncestor(item.getElement())
            ? null : createContextMenu(item.getElement()));
        boolean fixedChildLeaf = !empty && item != null && hasFixedChildrenAncestor(item.getElement());
        if (fixedChildLeaf) {
          if (!getStyleClass().contains("fixed-child-row")) {
            getStyleClass().add("fixed-child-row");
          }
        }
        else {
          getStyleClass().remove("fixed-child-row");
        }
      }
    });
    nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getName()));
    nameColumn.setCellFactory(column -> new ElementNameTreeCell());


    typeColumn.setCellValueFactory(param -> {
      String type = param.getValue().getValue().getType();
      if (type != null) {
        type = type.replaceAll("Type", "");
        if (type.equalsIgnoreCase("Rule")) {
          type = "Validation Rule";
        }
      }

      return new ReadOnlyStringWrapper(type);
    });

    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    applyColumnWidth(nameColumn, tableSettings, NAME_COLUMN_ID);
    applyColumnWidth(typeColumn, tableSettings, TYPE_COLUMN_ID);

    nameColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(NAME_COLUMN_ID, newValue.doubleValue()));
    typeColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(TYPE_COLUMN_ID, newValue.doubleValue()));

    modelTreeAddButton.getItems().addAll(createAddMenuItems());
  }
}
