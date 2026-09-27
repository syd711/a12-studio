package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.editors.treemodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Edits a list of {@link TreeNodeAction}s - a node type's row actions ({@code TreeNode.actions}) or the actions of
 * one context-menu group - as a compact table of Type, what the action does (its Event, or where it inserts what)
 * and Icon. Rows only summarize an action; a click on a row or its pencil button opens the full editor ({@link
 * Dialogs#showActionForEdit}), the Add button {@link Dialogs#showActionForAdd}. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern ({@link
 * #commitHeaderChange()}); {@link #configure} binds it to a list, so one panel class serves every place that holds
 * actions. The actions list is fetched through a supplier on every access, since an owner may create it lazily
 * (a node's context menu, say).
 */
public class TreeNodeActionsPanelController extends AbstractPropertyEditor {

  // javafx.scene.input.DataFormat registers its mime type in a process-wide static registry and throws if the same
  // string is registered twice, so every instance gets a counter of its own (see EventButtonsPanelController).
  private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();

  private static final double TYPE_COLUMN_WIDTH = 90.0;

  // The move up/down + edit + delete buttons at the end of each row (3 * 34px buttons + 2 * 4px spacing).
  private static final double ACTIONS_BOX_WIDTH = 110.0;

  @FXML
  private HBox actionsHeader;

  @FXML
  private VBox actionsList;

  @FXML
  private Label emptyLabel;

  private ProjectItem projectItem;
  private Supplier<List<TreeNodeAction>> actionsSupplier;
  private BooleanSupplier insertOnly = () -> false;
  private Supplier<TreeNodeAction> newAction = TreeNodeAction::new;
  private Supplier<TreeActionContext> context;
  private DataFormat indexFormat;

  // Notified after every change, so the owner can e.g. drop an owning object that became empty.
  private Runnable onChange = () -> {
  };

  /**
   * @param actionsSupplier the (mutable) list to edit; called on every access, so it may create the list lazily.
   * @param insertOnly      whether the actions can only be insert actions ({@code true} for an "add" context-menu
   *                        group); queried whenever a dialog opens, since the owner's type can change meanwhile.
   * @param newAction       creates the draft an Add starts from, so a list can seed the fields SME requires of its
   *                        actions (a row action needs a priority, a context-menu action has none).
   * @param context         where the actions are (the node type or the Virtual Root), which decides what the dialog's
   *                        pickers offer; called whenever a dialog opens, since the selected node type changes.
   */
  public void configure(@NonNull String title, @NonNull String settingsKeySuffix, @NonNull ProjectItem projectItem,
      @NonNull Supplier<List<TreeNodeAction>> actionsSupplier, @NonNull BooleanSupplier insertOnly,
      @NonNull Supplier<TreeNodeAction> newAction, @NonNull Supplier<TreeActionContext> context) {
    setTitle(title);
    setSettingsKeySuffix(settingsKeySuffix);
    this.projectItem = projectItem;
    this.actionsSupplier = actionsSupplier;
    this.insertOnly = insertOnly;
    this.newAction = newAction;
    this.context = context;
    if (indexFormat == null) {
      indexFormat = new DataFormat("application/x-a12-tree-node-action-index" + settingsKeySuffix + "-" + INSTANCE_COUNTER.incrementAndGet());
    }
    rebuildRows();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  /** Re-reads the list, e.g. after it changed from outside this panel. */
  public void refresh() {
    rebuildRows();
  }

  private List<TreeNodeAction> getActions() {
    return actionsSupplier.get();
  }

  @FXML
  private void onAdd() {
    Dialogs.showActionForAdd(Studio.stage, projectItem, newAction.get(), insertOnly.getAsBoolean(), context.get()).ifPresent(action -> {
      getActions().add(action);
      changed();
    });
  }

  private void openEditDialog(TreeNodeAction action) {
    Dialogs.showActionForEdit(Studio.stage, projectItem, action, insertOnly.getAsBoolean(), context.get()).ifPresent(edited -> {
      getActions().set(getActions().indexOf(action), edited);
      changed();
    });
  }

  private void changed() {
    rebuildRows();
    commitHeaderChange();
    onChange.run();
  }

  private void rebuildRows() {
    if (actionsSupplier == null) {
      return;
    }
    actionsList.getChildren().clear();

    List<TreeNodeAction> actions = getActions();
    boolean empty = actions.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    actionsHeader.setVisible(!empty);
    actionsHeader.setManaged(!empty);

    for (int index = 0; index < actions.size(); index++) {
      actionsList.getChildren().add(createRow(actions.get(index), index, actions.size()));
    }
  }

  private HBox createRow(TreeNodeAction action, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label typeLabel = new Label(StudioBundle.get("tree_node_action.type_" + (action.isInsert() ? TreeNodeAction.TYPE_INSERT : TreeNodeAction.TYPE_EVENT)));
    typeLabel.setId("treeNodeActionType-" + index);
    lockWidth(typeLabel, TYPE_COLUMN_WIDTH);
    makeClickableToEdit(typeLabel, action);

    Label whatLabel = new Label(describe(action));
    whatLabel.setId("treeNodeActionWhat-" + index);
    growEqually(whatLabel);
    makeClickableToEdit(whatLabel, action);

    Label iconLabel = new Label(action.getIconName());
    iconLabel.setId("treeNodeActionIcon-" + index);
    growEqually(iconLabel);
    makeClickableToEdit(iconLabel, action);

    HBox actionsBox = createActionsBox(action, index, rowCount);
    lockWidth(actionsBox, ACTIONS_BOX_WIDTH);

    HBox row = new HBox(10.0, dragHandle, typeLabel, whatLabel, iconLabel, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, indexFormat, index, this::moveViaDrag);
    return row;
  }

  /** The Event for an event action; for an insert action the Position, plus the Document Model if one is set. */
  static String describe(TreeNodeAction action) {
    if (!action.isInsert()) {
      return action.getEvent() != null ? action.getEvent() : "";
    }
    String position = action.getPosition() != null ? StudioBundle.get("tree_node_action.position_" + action.getPosition()) : "";
    return action.getDocumentModelRef() != null ? position + " · " + action.getDocumentModelRef() : position;
  }

  // Growing columns share the free width equally only if their preferred widths are equal, in the header as in the
  // rows (see the matching header in the panel's fxml); otherwise the header labels drift away from the values.
  private static void growEqually(Label label) {
    label.setMinWidth(0.0);
    label.setPrefWidth(100.0);
    label.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(label, Priority.ALWAYS);
  }

  private static void lockWidth(javafx.scene.layout.Region region, double width) {
    region.setMinWidth(width);
    region.setPrefWidth(width);
    region.setMaxWidth(width);
  }

  private void makeClickableToEdit(Label label, TreeNodeAction action) {
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(action);
      }
    });
  }

  private void moveViaDrag(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getActions(), fromIndex, insertBeforeIndex)) {
      changed();
    }
  }

  private HBox createActionsBox(TreeNodeAction action, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, StudioBundle.get("tree_node_action.edit"), () -> openEditDialog(action));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_action"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getActions().remove(action);
        changed();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getActions(), fromIndex, toIndex);
    changed();
  }
}
