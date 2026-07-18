package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.ComputationConfig;
import de.a12.studio.dataservices.models.documentmodel.ComputationElement;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.EnumerationFieldType;
import de.a12.studio.dataservices.models.documentmodel.EnumerationTypeOptions;
import de.a12.studio.dataservices.models.documentmodel.EnumerationValue;
import de.a12.studio.dataservices.models.documentmodel.FieldConfig;
import de.a12.studio.dataservices.models.documentmodel.FieldElement;
import de.a12.studio.dataservices.models.documentmodel.GroupConfig;
import de.a12.studio.dataservices.models.documentmodel.GroupElement;
import de.a12.studio.dataservices.models.documentmodel.ModelRoot;
import de.a12.studio.dataservices.models.documentmodel.NumberFieldType;
import de.a12.studio.dataservices.models.documentmodel.RequirednessConfig;
import de.a12.studio.dataservices.models.documentmodel.RuleConfig;
import de.a12.studio.dataservices.models.documentmodel.RuleElement;
import de.a12.studio.dataservices.models.documentmodel.StringFieldType;
import de.a12.studio.commons.components.SearchFieldController;
import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.commons.util.localsettings.BaseTableSettings;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.editors.documentmodel.commands.AddNodeCommand;
import de.a12.studio.ui.editors.documentmodel.commands.DeleteNodeCommand;
import de.a12.studio.ui.editors.util.commandstack.Command;
import de.a12.studio.ui.editors.util.commandstack.CommandStack;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.ElementValidatedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class DocumentModelElementsTreeController implements Initializable, StudioEventListener {

  private static final String TABLE_SETTINGS_ID = ModelType.DOCUMENT.getValue();

  private static final String NAME_COLUMN_ID = "name";

  private static final String TYPE_COLUMN_ID = "type";

  private static final String ID_PREFIX_GROUP = "group";
  private static final String ID_PREFIX_FIELD = "field";
  private static final String ID_PREFIX_RULE = "rule";
  private static final String ID_PREFIX_COMPUTATION = "computation";
  private static final String ID_PREFIX_ATTACHMENT = "attachment";
  private static final String ID_PREFIX_MULTI_SELECT = "multi-select";
  private static final String ID_PREFIX_MULTI_SELECT_CHILD = "multiSelectChild";
  private static final String ID_PREFIX_INCLUDE = "include";

  private static final Random ID_RANDOM = new SecureRandom();

  @FXML
  private ToolBar modelTreeToolbarBar;

  @FXML
  private Button undoButton;

  @FXML
  private Button redoButton;

  @FXML
  private MenuButton modelTreeAddButton;

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
    expandAll(root);
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

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    modelTreeAddButton.setDisable(true);
    updateUndoRedoState();
    searchController.setOnSearch(this::applyFilter);

    elementsTreeTable.setShowRoot(false);
    elementsTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    elementsTreeTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ElementViewModel>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<ElementViewModel>> observable, TreeItem<ElementViewModel> oldValue, TreeItem<ElementViewModel> newValue) {
        modelTreeAddButton.setDisable(newValue == null || isWithinFixedChildrenGroup(newValue.getValue().getElement()));
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
        setContextMenu(empty || item == null || isWithinFixedChildrenGroup(item.getElement()) ? null : createContextMenu());
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
      type = type.replaceAll("Type", "");
      if (type.equalsIgnoreCase("Rule")) {
        type = "Validation Rule";
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

  private ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().addAll(createElementMenuItems());
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

  private List<MenuItem> createElementMenuItems() {
    List<MenuItem> items = new ArrayList<>();
    items.addAll(createAddMenuItems());
    items.add(new SeparatorMenuItem());
    items.add(createMenuItem("_Cut", Icons.CUT));
    items.add(createMenuItem("Cop_y", Icons.COPY));
    items.add(createMenuItem("_Paste", Icons.PASTE));
    items.add(new SeparatorMenuItem());
    MenuItem deleteItem = createMenuItem("_Delete", Icons.TRASH);
    deleteItem.setOnAction(event -> onDeleteModelItem());
    items.add(deleteItem);
    return items;
  }

  private void onDeleteKeyPressed() {
    List<TreeItem<ElementViewModel>> selection =
        new ArrayList<>(elementsTreeTable.getSelectionModel().getSelectedItems());
    if (selection.isEmpty()) {
      return;
    }

    boolean hasChildren = topLevelSelection(selection).stream().anyMatch(treeItem -> !treeItem.getChildren().isEmpty());
    if (hasChildren) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
          "Delete the selected element(s)?", "Child elements will be deleted as well.", null, "Delete");
      if (result.isEmpty() || result.get() != ButtonType.OK) {
        return;
      }
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
    items.add(createAddMenuItem(createMenuItem("_Group", createGroupIcon()), this::newGroupElement));
    items.add(createAddMenuItem(createMenuItem("_Field", Icons.ELEMENT_FIELD), this::newFieldElement));
    items.add(createAddMenuItem(createMenuItem("_Validation Rule", Icons.ELEMENT_VALIDATION_RULE), this::newRuleElement));
    items.add(createAddMenuItem(createMenuItem("Co_mputation Rule", Icons.ELEMENT_COMPUTATION), this::newComputationElement));
    items.add(createAddMenuItem(createMenuItem("_Attachment", Icons.ELEMENT_ATTACHMENT), this::newAttachmentElement));
    items.add(createAddMenuItem(createMenuItem("Multi-_Select", Icons.ELEMENT_MULTI_SELECT), this::newMultiSelectElement));
    items.add(createAddMenuItem(createMenuItem("_Include", Icons.ELEMENT_INCLUDE), this::newIncludeElement));
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
    commandStack.execute(new AddNodeCommand<>(insertionPoint.siblings(), newElement, insertionPoint.index()));

    updateUndoRedoState();
    applyFilter(searchController.getText());
    selectElement(newElement);
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
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

  private Element newGroupElement(@NonNull List<Element> siblings) {
    GroupElement group = new GroupElement();
    group.setId(generateId(ID_PREFIX_GROUP));
    group.setName(uniqueName("Group", siblings));
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    group.setGroup(config);
    return group;
  }

  private Element newFieldElement(@NonNull List<Element> siblings) {
    FieldElement field = new FieldElement();
    field.setId(generateId(ID_PREFIX_FIELD));
    field.setName(uniqueName("Field", siblings));
    field.setField(newStringFieldConfig());
    return field;
  }

  private Element newRuleElement(@NonNull List<Element> siblings) {
    RuleElement rule = new RuleElement();
    String id = generateId(ID_PREFIX_RULE);
    rule.setId(id);
    rule.setName(uniqueName("ValidationRule", siblings));
    RuleConfig config = new RuleConfig();
    config.setErrorCode("Error " + id);
    rule.setRule(config);
    return rule;
  }

  private Element newComputationElement(@NonNull List<Element> siblings) {
    ComputationElement computation = new ComputationElement();
    computation.setId(generateId(ID_PREFIX_COMPUTATION));
    computation.setName(uniqueName("ComputationRule", siblings));
    computation.setComputation(new ComputationConfig());
    return computation;
  }

  /**
   * A group with a fixed set of children (attachment). Field/rule names, error conditions and
   * messages mirror what the kernel expects for the "attachment" usage type.
   */
  private Element newAttachmentElement(@NonNull List<Element> siblings) {
    GroupElement attachment = new GroupElement();
    attachment.setId(generateId(ID_PREFIX_ATTACHMENT));
    attachment.setName(uniqueName("Attachment", siblings));
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    config.setUsageType(GroupConfig.USAGE_TYPE_ATTACHMENT);
    config.getElements().addAll(createAttachmentFixedChildren());
    attachment.setGroup(config);
    return attachment;
  }

  private List<Element> createAttachmentFixedChildren() {
    List<Element> children = new ArrayList<>();
    children.add(newFixedField("original_filename", newStringFieldConfig()));
    children.add(newFixedField("internal_filename", newStringFieldConfig()));
    children.add(newFixedField("content", newStringFieldConfig()));
    children.add(newFixedField("attachment_id", newStringFieldConfig()));
    children.add(newFixedField("size", newNumberFieldConfig()));
    children.add(newFixedField("mime_type", newStringFieldConfig()));
    children.add(newFixedField("category", newStringFieldConfig()));
    children.add(newFixedField("description", newStringFieldConfig()));
    children.add(newAttachmentRule("AttachmentInternalFilenameRequired", "../internal_filename",
        "GroupFilled(RuleGroup) and FieldNotFilled(internal_filename)",
        "Internal Error: Field $internal_filename$ of customType attachment is not filled."));
    children.add(newAttachmentRule("AttachmentMimeTypeRequired", "../mime_type",
        "GroupFilled(RuleGroup) and FieldNotFilled(mime_type)",
        "Internal Error: Field $mime_type$ of customType attachment is not filled."));
    children.add(newAttachmentRule("AttachmentIdOrContentFilled", "../content",
        "GroupFilled(RuleGroup) and NotExactlyOneFieldFilled(attachment_id, content)",
        "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both."));
    children.add(newAttachmentRule("SizeOfContentFilled", "../content",
        "FieldFilled(content) and FieldNotFilled(size)",
        "Internal Error: If the content is filled, the size must be also filled."));
    return children;
  }

  private RuleElement newAttachmentRule(@NonNull String name, @NonNull String errorEntityRelPath,
                                         @NonNull String errorCondition, @NonNull String errorMessageText) {
    RuleElement rule = new RuleElement();
    String id = generateId(ID_PREFIX_RULE);
    rule.setId(id);
    rule.setName(name);
    RuleConfig config = new RuleConfig();
    config.setErrorEntityRelPath(errorEntityRelPath);
    config.setErrorCode("Error " + id);
    config.setErrorCondition(errorCondition);
    config.setSeverity("ERROR");
    config.getErrorMessage().add(newLabel("en", errorMessageText));
    config.getErrorMessage().add(newLabel("de", errorMessageText));
    rule.setRule(config);
    return rule;
  }

  /**
   * A group with a fixed single "value" enumeration child, forced to the maximum repeatability
   * (999999) that the kernel treats as "unbounded" for the "multi-select" usage type.
   */
  private Element newMultiSelectElement(@NonNull List<Element> siblings) {
    GroupElement multiSelect = new GroupElement();
    multiSelect.setId(generateId(ID_PREFIX_MULTI_SELECT));
    multiSelect.setName(uniqueName("New Multi-Select", siblings));
    GroupConfig config = new GroupConfig();
    config.setRepeatability(999_999);
    config.setUsageType(GroupConfig.USAGE_TYPE_MULTI_SELECT);
    config.getElements().add(newMultiSelectFixedChild());
    multiSelect.setGroup(config);
    return multiSelect;
  }

  private FieldElement newMultiSelectFixedChild() {
    FieldElement field = new FieldElement();
    field.setId(generateId(ID_PREFIX_MULTI_SELECT_CHILD));
    field.setName("value");
    FieldConfig config = new FieldConfig();
    RequirednessConfig requirednessConfig = new RequirednessConfig();
    requirednessConfig.setMode(RequirednessConfig.MODE_REQUIRED);
    config.setRequirednessConfig(requirednessConfig);
    EnumerationTypeOptions options = new EnumerationTypeOptions();
    options.getValues().add(newEnumerationValue("key1"));
    options.getValues().add(newEnumerationValue("key2"));
    EnumerationFieldType fieldType = new EnumerationFieldType();
    fieldType.setEnumerationType(options);
    config.setFieldType(fieldType);
    field.setField(config);
    return field;
  }

  private EnumerationValue newEnumerationValue(@NonNull String value) {
    EnumerationValue enumerationValue = new EnumerationValue();
    enumerationValue.setValue(value);
    return enumerationValue;
  }

  /**
   * A group referencing another Document Model. The reference itself ({@code modelAlias}) has no
   * picker in the UI yet, so it's left unset here, same as a plain group, until that reference can
   * be assigned (surfaces as a "Missing Include Reference" validation error until then).
   */
  private Element newIncludeElement(@NonNull List<Element> siblings) {
    GroupElement include = new GroupElement();
    include.setId(generateId(ID_PREFIX_INCLUDE));
    include.setName(uniqueName("New Include", siblings));
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    include.setGroup(config);
    return include;
  }

  private FieldElement newFixedField(@NonNull String name, @NonNull FieldConfig config) {
    FieldElement field = new FieldElement();
    field.setId(generateId(ID_PREFIX_FIELD));
    field.setName(name);
    field.setField(config);
    return field;
  }

  private FieldConfig newStringFieldConfig() {
    FieldConfig config = new FieldConfig();
    config.setFieldType(new StringFieldType());
    return config;
  }

  private FieldConfig newNumberFieldConfig() {
    FieldConfig config = new FieldConfig();
    config.setFieldType(new NumberFieldType());
    return config;
  }

  private de.a12.studio.dataservices.models.Label newLabel(@NonNull String locale, @NonNull String text) {
    de.a12.studio.dataservices.models.Label label = new de.a12.studio.dataservices.models.Label();
    label.setLocale(locale);
    label.setText(text);
    return label;
  }

  private String uniqueName(@NonNull String baseName, @NonNull List<Element> siblings) {
    Set<String> usedNames = new HashSet<>();
    for (Element sibling : siblings) {
      usedNames.add(sibling.getName());
    }
    if (!usedNames.contains(baseName)) {
      return baseName;
    }
    int suffix = 2;
    while (usedNames.contains(baseName + "_" + suffix)) {
      suffix++;
    }
    return baseName + "_" + suffix;
  }

  private String generateId(@NonNull String prefix) {
    Set<String> usedIds = collectIds();
    String id;
    do {
      id = prefix + "_" + String.format("%05x", ID_RANDOM.nextInt(0x100000));
    } while (usedIds.contains(id));
    return id;
  }

  private Set<String> collectIds() {
    Set<String> ids = new HashSet<>();
    for (GroupElement rootGroup : modelRoot.getRootGroups()) {
      collectIds(rootGroup, ids);
    }
    return ids;
  }

  private void collectIds(@NonNull Element element, @NonNull Set<String> ids) {
    ids.add(element.getId());
    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      for (Element child : groupElement.getGroup().getElements()) {
        collectIds(child, ids);
      }
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

  private Node createPngIcon(@NonNull String path) {
    Image image = new Image(getClass().getResourceAsStream(path),
        WidgetFactory.DEFAULT_ICON_SIZE, WidgetFactory.DEFAULT_ICON_SIZE, true, true);
    return new ImageView(image);
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
}
