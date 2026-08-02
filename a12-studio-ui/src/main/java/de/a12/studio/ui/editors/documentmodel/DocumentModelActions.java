package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.documentmodel.commands.AddNodeCommand;
import de.a12.studio.ui.editors.documentmodel.commands.DeleteNodeCommand;
import de.a12.studio.ui.editors.documentmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.documentmodel.dialogs.IncludeDialogController;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.commandstack.Command;
import de.a12.studio.ui.util.commandstack.CommandStack;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builds the document model tree's context menu and carries out its actions (element creation,
 * deletion; Cut/Copy/Paste are still unwired placeholders), plus {@link #createAddMenuItems()},
 * which is also reused by {@link DocumentModelElementsTreeController} for the toolbar "Add" button.
 */
public class DocumentModelActions {

  private final ProjectItem projectItem;
  private final ModelRoot modelRoot;
  private final CommandStack commandStack;
  private final TreeTableView<ElementViewModel> elementsTreeTable;
  private final Consumer<Element> onModelChanged;

  public DocumentModelActions(@NonNull ProjectItem projectItem, @NonNull ModelRoot modelRoot,
                               @NonNull CommandStack commandStack, @NonNull TreeTableView<ElementViewModel> elementsTreeTable,
                               @NonNull Consumer<Element> onModelChanged) {
    this.projectItem = projectItem;
    this.modelRoot = modelRoot;
    this.commandStack = commandStack;
    this.elementsTreeTable = elementsTreeTable;
    this.onModelChanged = onModelChanged;
  }

  public ContextMenu createContextMenu(@NonNull Element element) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().addAll(createElementMenuItems(element));
    return contextMenu;
  }

  private List<MenuItem> createElementMenuItems(@NonNull Element element) {
    List<MenuItem> items = new ArrayList<>();
    if (!new ElementViewModel(element).hasFixedChildren()) {
      Menu createMenu = new Menu("_Create...");
      createMenu.getItems().addAll(createAddMenuItems());
      items.add(createMenu);
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

  public List<MenuItem> createAddMenuItems() {
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

    MenuItem includeItem = createMenuItem("_Include", Icons.ELEMENT_INCLUDE);
    includeItem.setOnAction(event -> onAddInclude());
    items.add(includeItem);

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
    TreeItem<ElementViewModel> selectedItem = elementsTreeTable.getSelectionModel().getSelectedItem();
    if (selectedItem == null || selectedItem.getValue() == null) {
      return;
    }

    InsertionPoint insertionPoint = resolveInsertionPoint(selectedItem);
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

    onModelChanged.accept(null);
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
