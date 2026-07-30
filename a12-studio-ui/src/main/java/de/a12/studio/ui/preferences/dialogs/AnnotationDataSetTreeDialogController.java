package de.a12.studio.ui.preferences.dialogs;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.settings.annotations.AnnotationDataSet;
import de.a12.studio.models.projects.settings.annotations.AnnotationFieldSet;
import de.a12.studio.models.projects.settings.annotations.AnnotationHeaderSet;
import de.a12.studio.models.projects.settings.annotations.AnnotationModelSet;
import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared Edit/Export dialog for a single {@link AnnotationDataSet}: shows a tree of every header/content
 * annotation in the set, letting the user remove entries (and, when {@code editable}, rename the set).
 * Always operates on a working copy handed in by
 * {@link Dialogs#showAnnotationDataSetEditor(Stage, AnnotationDataSet, boolean)} - removals mutate that copy
 * directly, so Cancel simply discards it.
 */
public class AnnotationDataSetTreeDialogController implements DialogController {

  private static final String HEADER_ANNOTATIONS_LABEL = "Header Annotations";
  private static final String CONTENT_ANNOTATIONS_LABEL = "Content Annotations";
  private static final String NO_MODEL_TYPE_LABEL = "(none)";

  @FXML
  private TextField nameField;

  @FXML
  private TreeView<AnnotationTreeNode> annotationTree;

  @FXML
  private Button removeButton;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  // Leaf nodes carry a non-null onRemove that deletes that annotation name from the working copy's backing
  // maps; group nodes (Header/Content Annotations, model types) leave it null and can't be removed.
  private record AnnotationTreeNode(String label, Runnable onRemove) {
  }

  @FXML
  private void initialize() {
    annotationTree.setCellFactory(view -> new TreeCell<AnnotationTreeNode>() {
      @Override
      protected void updateItem(AnnotationTreeNode node, boolean empty) {
        super.updateItem(node, empty);
        setText(empty || node == null ? null : node.label());
      }
    });
    annotationTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        removeButton.setDisable(newValue == null || newValue.getValue().onRemove() == null));
    removeButton.setDisable(true);
    okButton.disableProperty().bind(nameField.textProperty().map(name -> name == null || name.isBlank()));
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

  @FXML
  private void onRemove() {
    TreeItem<AnnotationTreeNode> selected = annotationTree.getSelectionModel().getSelectedItem();
    if (selected == null || selected.getValue().onRemove() == null) {
      return;
    }
    selected.getValue().onRemove().run();
    removeFromParentPruningEmpty(selected);
  }

  void init(Stage stage, boolean editable, AnnotationDataSet workingCopy) {
    this.stage = stage;
    nameField.setText(workingCopy.getName());
    nameField.setEditable(editable);
    nameField.setDisable(!editable);
    okButton.setText(editable ? "Save" : "Export");
    annotationTree.setShowRoot(false);
    annotationTree.setRoot(buildRoot(workingCopy));
  }

  boolean applyResultTo(AnnotationDataSet target) {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      target.setName(nameField.getText().trim());
      return true;
    }
    return false;
  }

  private static TreeItem<AnnotationTreeNode> buildRoot(AnnotationDataSet dataSet) {
    TreeItem<AnnotationTreeNode> root = new TreeItem<>(new AnnotationTreeNode(dataSet.getName(), null));

    AnnotationHeaderSet headerSet = dataSet.getHeaderSet();
    TreeItem<AnnotationTreeNode> headerRoot = new TreeItem<>(new AnnotationTreeNode(HEADER_ANNOTATIONS_LABEL, null));
    for (Map.Entry<String, AnnotationModelSet> modelTypeEntry : sortedEntries(headerSet.getModelTypes())) {
      String modelTypeKey = modelTypeEntry.getKey();
      TreeItem<AnnotationTreeNode> modelTypeItem = new TreeItem<>(new AnnotationTreeNode(modelTypeLabelFor(modelTypeKey), null));
      for (String name : modelTypeEntry.getValue().getValues().keySet()) {
        modelTypeItem.getChildren().add(new TreeItem<>(new AnnotationTreeNode(name,
            () -> removeHeaderName(headerSet, modelTypeKey, name))));
      }
      if (!modelTypeItem.getChildren().isEmpty()) {
        headerRoot.getChildren().add(modelTypeItem);
      }
    }
    if (!headerRoot.getChildren().isEmpty()) {
      root.getChildren().add(headerRoot);
    }

    AnnotationFieldSet fieldSet = dataSet.getFieldSet();
    TreeItem<AnnotationTreeNode> contentRoot = new TreeItem<>(new AnnotationTreeNode(CONTENT_ANNOTATIONS_LABEL, null));
    for (Map.Entry<String, AnnotationModelSet> modelTypeEntry : sortedEntries(fieldSet.getModelTypes())) {
      String modelTypeKey = modelTypeEntry.getKey();
      TreeItem<AnnotationTreeNode> modelTypeItem = new TreeItem<>(new AnnotationTreeNode(modelTypeLabelFor(modelTypeKey), null));
      for (String name : modelTypeEntry.getValue().getValues().keySet()) {
        modelTypeItem.getChildren().add(new TreeItem<>(new AnnotationTreeNode(name,
            () -> removeFieldName(fieldSet, modelTypeKey, name))));
      }
      if (!modelTypeItem.getChildren().isEmpty()) {
        contentRoot.getChildren().add(modelTypeItem);
      }
    }
    if (!contentRoot.getChildren().isEmpty()) {
      root.getChildren().add(contentRoot);
    }

    expandAll(root);
    return root;
  }

  private static void removeHeaderName(AnnotationHeaderSet headerSet, String modelTypeKey, String name) {
    AnnotationModelSet modelSet = headerSet.getModelTypes().get(modelTypeKey);
    if (modelSet == null) {
      return;
    }
    modelSet.getValues().remove(name);
    if (modelSet.getValues().isEmpty()) {
      headerSet.getModelTypes().remove(modelTypeKey);
    }
  }

  private static void removeFieldName(AnnotationFieldSet fieldSet, String modelTypeKey, String name) {
    AnnotationModelSet modelSet = fieldSet.getModelTypes().get(modelTypeKey);
    if (modelSet == null) {
      return;
    }
    modelSet.getValues().remove(name);
    if (modelSet.getValues().isEmpty()) {
      fieldSet.getModelTypes().remove(modelTypeKey);
    }
  }

  private static void removeFromParentPruningEmpty(TreeItem<AnnotationTreeNode> item) {
    TreeItem<AnnotationTreeNode> parent = item.getParent();
    if (parent == null) {
      return;
    }
    parent.getChildren().remove(item);
    if (parent.getChildren().isEmpty() && parent.getParent() != null) {
      removeFromParentPruningEmpty(parent);
    }
  }

  private static <V> List<Map.Entry<String, V>> sortedEntries(Map<String, V> map) {
    return map.entrySet().stream()
        .sorted(Map.Entry.comparingByKey(Comparator.nullsFirst(Comparator.naturalOrder())))
        .toList();
  }

  // Model type keys are stored as ModelType#name() (see AnnotationHeaderRegistry/AnnotationFieldRegistry),
  // so resolve them through the enum to show the human-readable display name instead.
  private static String modelTypeLabelFor(String modelTypeKey) {
    if (modelTypeKey == null) {
      return NO_MODEL_TYPE_LABEL;
    }
    try {
      return ModelType.valueOf(modelTypeKey).getDisplayName();
    }
    catch (IllegalArgumentException e) {
      return modelTypeKey;
    }
  }

  private static void expandAll(TreeItem<AnnotationTreeNode> item) {
    item.setExpanded(true);
    for (TreeItem<AnnotationTreeNode> child : item.getChildren()) {
      expandAll(child);
    }
  }
}
