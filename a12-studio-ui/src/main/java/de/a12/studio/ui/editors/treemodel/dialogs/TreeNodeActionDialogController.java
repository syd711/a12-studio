package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.Label;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.overviewmodel.StylesPanelController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.PriorityPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Modal dialog for creating/editing one {@link TreeNodeAction}, opened from {@link
 * de.a12.studio.ui.editors.treemodel.TreeNodeActionsPanelController} for a node type's row actions and for the
 * actions of a context-menu group. SME's "Button Functions" (Type: an event action fires an Event, an insert action
 * inserts a node at a Position, optionally of a given Document Model, and can take its label, title and icon from
 * that model), Confirmation Text, "Visual Settings" (Priority, Destructive, Icon, Hide Label, Label, Description,
 * Styles) and Annotations. Which Events make sense is open-ended, so the field is an editable combo with the
 * events SME's Tree Engine knows as suggestions.
 * <p>
 * The action starts out unattached (Add) or as a JSON clone of the real one (Edit, see {@link
 * Dialogs#showActionForEdit}) - the caller only splices it into its list once {@link #isConfirmed()} is true.
 * Like {@link de.a12.studio.ui.editors.propertyeditors.dialogs.EventButtonDialogController}, every embedded panel
 * shares a deferred save mode so nothing is persisted while the dialog is open.
 */
public class TreeNodeActionDialogController implements DialogController {

  // The row events (incl. copy/paste) SME's Tree Engine handles itself, see its tmEvents.ts.
  private static final List<String> EVENT_SUGGESTIONS = List.of("event_add_link", "event_delete_link", "event_delete_node",
      "event_expand_sub_tree", "event_collapse_sub_tree", "event_copy_node", "event_copy_node_and_children", "event_cut_node",
      "event_paste", "event_paste_above", "event_paste_below");

  private static final List<String> TYPES = List.of(TreeNodeAction.TYPE_EVENT, TreeNodeAction.TYPE_INSERT);
  private static final List<String> POSITIONS = List.of(TreeNodeAction.POSITION_AS_CHILD, TreeNodeAction.POSITION_ABOVE, TreeNodeAction.POSITION_BELOW);

  @FXML
  private ComboBox<String> typeField;

  @FXML
  private VBox eventBox;

  @FXML
  private ComboBox<String> eventField;

  @FXML
  private VBox insertBox;

  @FXML
  private ComboBox<String> positionField;

  @FXML
  private ComboBox<String> documentModelField;

  @FXML
  private CheckBox useLabelFromDocumentModelField;

  @FXML
  private CheckBox useTitleFromDocumentModelField;

  @FXML
  private CheckBox useGlobalIconField;

  @FXML
  private LocalizedTextPanelController confirmationTitleController;

  @FXML
  private LocalizedTextPanelController confirmationMessageController;

  @FXML
  private PriorityPanelController priorityController;

  @FXML
  private IconPanelController iconController;

  @FXML
  private CheckBox hideLabelField;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private LocalizedTextPanelController descriptionController;

  @FXML
  private StylesPanelController stylesController;

  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private Button okButton;

  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private TreeNodeAction action;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Set while the fields are being repopulated from the action, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @FXML
  private void initialize() {
    confirmationTitleController.setSaveMode(saveMode);
    confirmationMessageController.setSaveMode(saveMode);
    priorityController.setSaveMode(saveMode);
    iconController.setSaveMode(saveMode);
    labelController.setSaveMode(saveMode);
    descriptionController.setSaveMode(saveMode);
    stylesController.setSaveMode(saveMode);
    annotationsController.setSaveMode(saveMode);

    confirmationTitleController.configureCustom("confirmationTitle", StudioBundle.get("confirmation_title"));
    confirmationMessageController.configureCustom("confirmationMessage", StudioBundle.get("confirmation_message"));
    labelController.configureCustom("label", StudioBundle.get("label"));
    descriptionController.configureCustom("description", StudioBundle.get("description"));
    annotationsController.hideAnnotationDatasetsButton();

    typeField.getItems().setAll(TYPES);
    typeField.setConverter(bundleConverter("tree_node_action.type_"));
    typeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel && newValue != null) {
        applyType(newValue);
      }
    });

    eventField.setEditable(true);
    eventField.getItems().setAll(EVENT_SUGGESTIONS);
    eventField.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        action.setEvent(blankToNull(newValue));
      }
    });
    okButton.disableProperty().bind(typeField.valueProperty().isEqualTo(TreeNodeAction.TYPE_EVENT)
        .and(eventField.getEditor().textProperty().isEmpty()));

    positionField.getItems().setAll(POSITIONS);
    positionField.setConverter(bundleConverter("tree_node_action.position_"));
    positionField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel && newValue != null) {
        action.setPosition(newValue);
      }
    });

    documentModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      action.setDocumentModelRef(blankToNull(newValue));
      refreshDocumentModelDependents();
    });
    bindFlag(useLabelFromDocumentModelField, TreeNodeAction::setUseLabelFromDocumentModel);
    bindFlag(useTitleFromDocumentModelField, TreeNodeAction::setUseTitleFromDocumentModel);
    bindFlag(useGlobalIconField, TreeNodeAction::setUseGlobalIcon);
  }

  /** {@code true} when checked, absent when not - SME never writes {@code false} for these flags. */
  private void bindFlag(CheckBox checkBox, BiConsumer<TreeNodeAction, Boolean> setter) {
    checkBox.selectedProperty().addListener((observable, oldValue, selected) -> {
      if (!updatingFromModel) {
        setter.accept(action, selected ? Boolean.TRUE : null);
      }
    });
  }

  /**
   * @param insertOnly whether the action can only be an insert action, as in a context menu's "add" group; the Type is
   *                   then fixed.
   */
  void init(@NonNull Stage stage, @NonNull ProjectItem projectItem, @NonNull TreeNodeAction action, boolean insertOnly) {
    this.stage = stage;
    this.action = action;

    if (insertOnly && !action.isInsert()) {
      action.setType(TreeNodeAction.TYPE_INSERT);
      action.setEvent(null);
    }
    if (action.getType() == null) {
      action.setType(TreeNodeAction.TYPE_EVENT);
    }
    if (action.isInsert() && action.getPosition() == null) {
      action.setPosition(TreeNodeAction.POSITION_AS_CHILD);
    }

    updatingFromModel = true;
    try {
      typeField.setValue(action.getType());
      typeField.setDisable(insertOnly);
      eventField.getEditor().setText(action.getEvent());
      positionField.setValue(action.getPosition());

      List<String> documentModels = new ArrayList<>();
      documentModels.add(null);
      documentModels.addAll(ColumnMappingEditor.documentModelIds(projectItem));
      if (action.getDocumentModelRef() != null && !documentModels.contains(action.getDocumentModelRef())) {
        documentModels.add(action.getDocumentModelRef());
      }
      documentModelField.getItems().setAll(documentModels);
      documentModelField.setValue(action.getDocumentModelRef());
      documentModelField.setPromptText(StudioBundle.get("tree_node_action.default_document_model"));

      useLabelFromDocumentModelField.setSelected(Boolean.TRUE.equals(action.getUseLabelFromDocumentModel()));
      useTitleFromDocumentModelField.setSelected(Boolean.TRUE.equals(action.getUseTitleFromDocumentModel()));
      useGlobalIconField.setSelected(Boolean.TRUE.equals(action.getUseGlobalIcon()));
      hideLabelField.setSelected(Boolean.TRUE.equals(action.getLabelHidden()));
    }
    finally {
      updatingFromModel = false;
    }
    hideLabelField.selectedProperty().addListener((observable, oldValue, newValue) ->
        action.setLabelHidden(newValue ? Boolean.TRUE : null));
    refreshTypeDependents();

    confirmationTitleController.setCustom(this::currentConfirmationTitle, this::writeConfirmationTitle);
    confirmationMessageController.setCustom(this::currentConfirmationMessage, this::writeConfirmationMessage);
    priorityController.setButton(action);
    iconController.setCustom(action::getIcon, action::setIcon);
    labelController.setCustom(action::getLabel);
    descriptionController.setCustom(action::getDescription);
    stylesController.setCustom(action::getStyles);
    annotationsController.setCustom(action::getAnnotations);
  }

  /** Switching the Type drops what only the other one uses, so no stale event/insert settings are written. */
  private void applyType(String type) {
    action.setType(type);
    if (TreeNodeAction.TYPE_INSERT.equals(type)) {
      action.setEvent(null);
      updatingFromModel = true;
      try {
        eventField.getEditor().setText(null);
        positionField.setValue(action.getPosition() != null ? action.getPosition() : TreeNodeAction.POSITION_AS_CHILD);
      }
      finally {
        updatingFromModel = false;
      }
      action.setPosition(positionField.getValue());
    }
    else {
      action.setPosition(null);
      action.setDocumentModelRef(null);
      action.setHasDocumentModelRef(null);
      action.setUseLabelFromDocumentModel(null);
      action.setUseTitleFromDocumentModel(null);
      action.setUseGlobalIcon(null);
      updatingFromModel = true;
      try {
        positionField.setValue(null);
        documentModelField.setValue(null);
        useLabelFromDocumentModelField.setSelected(false);
        useTitleFromDocumentModelField.setSelected(false);
        useGlobalIconField.setSelected(false);
      }
      finally {
        updatingFromModel = false;
      }
    }
    refreshTypeDependents();
  }

  private void refreshTypeDependents() {
    boolean insert = TreeNodeAction.TYPE_INSERT.equals(typeField.getValue());
    eventBox.setVisible(!insert);
    eventBox.setManaged(!insert);
    insertBox.setVisible(insert);
    insertBox.setManaged(insert);
    refreshDocumentModelDependents();
  }

  /** Taking label, title or icon from the Document Model only makes sense once one has been picked. */
  private void refreshDocumentModelDependents() {
    boolean hasDocumentModel = documentModelField.getValue() != null;
    for (CheckBox flag : List.of(useLabelFromDocumentModelField, useTitleFromDocumentModelField, useGlobalIconField)) {
      flag.setVisible(hasDocumentModel);
      flag.setManaged(hasDocumentModel);
    }
  }

  /** Unregisters the embedded panels once this dialog is closed - see {@link Dialogs}. */
  void destroy() {
    confirmationTitleController.destroy();
    confirmationMessageController.destroy();
    priorityController.destroy();
    iconController.destroy();
    labelController.destroy();
    descriptionController.destroy();
    stylesController.destroy();
    annotationsController.destroy();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  TreeNodeAction getAction() {
    return action;
  }

  private static StringConverter<String> bundleConverter(String keyPrefix) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? null : StudioBundle.get(keyPrefix + value);
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

  private List<Label> currentConfirmationTitle() {
    Confirmation confirmation = action.getConfirmation();
    return confirmation != null ? confirmation.getTitle() : List.of();
  }

  private List<Label> writeConfirmationTitle() {
    return action.getOrCreateConfirmation().getTitle();
  }

  private List<Label> currentConfirmationMessage() {
    Confirmation confirmation = action.getConfirmation();
    return confirmation != null ? confirmation.getMessage() : List.of();
  }

  private List<Label> writeConfirmationMessage() {
    return action.getOrCreateConfirmation().getMessage();
  }
}
