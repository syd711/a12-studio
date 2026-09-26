package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The "column mapping" grid shared by the node type dialog and the child relationship dialog: one row per tree
 * column, each with a combo choosing the Document Model field the column shows ({@link TreeNodeColumn#getElementRef}).
 * Edits a working list of {@link TreeNodeColumn}s in place; a row is created for a column on first choice, and a
 * mapping whose field is no longer offered stays visible (and is kept) instead of being silently dropped.
 */
public final class ColumnMappingEditor {

  private ColumnMappingEditor() {
  }

  /** Rebuilds {@code grid} with one row per column, editing {@code mappings}. */
  static void populate(@NonNull GridPane grid, @NonNull Label noColumnsLabel, @NonNull List<TreeColumn> columns,
      @NonNull List<String> fieldOptions, @NonNull List<TreeNodeColumn> mappings) {
    populate(grid, noColumnsLabel, columns, fieldOptions, mappings, () -> {
    });
  }

  /** As above, additionally running {@code onChange} after every user edit of a mapping. */
  public static void populate(@NonNull GridPane grid, @NonNull Label noColumnsLabel, @NonNull List<TreeColumn> columns,
      @NonNull List<String> fieldOptions, @NonNull List<TreeNodeColumn> mappings, @NonNull Runnable onChange) {
    grid.getChildren().clear();
    noColumnsLabel.setVisible(columns.isEmpty());
    noColumnsLabel.setManaged(columns.isEmpty());

    int row = 0;
    for (TreeColumn column : columns) {
      Label columnLabel = new Label(column.getName());
      columnLabel.getStyleClass().add("field-label");

      ComboBox<String> elementField = new ComboBox<>();
      elementField.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(elementField, Priority.ALWAYS);
      elementField.getItems().setAll(fieldOptions);
      elementField.setValue(mappedElementRef(mappings, column.getId()));
      elementField.valueProperty().addListener((observable, oldValue, newValue) -> {
        setMappedElementRef(mappings, column.getId(), newValue);
        onChange.run();
      });

      grid.addRow(row++, columnLabel, elementField);
    }
  }

  static String mappedElementRef(List<TreeNodeColumn> mappings, String columnId) {
    return mappings.stream()
        .filter(mapping -> columnId != null && columnId.equals(mapping.getColumnRef()))
        .map(TreeNodeColumn::getElementRef)
        .findFirst()
        .orElse(null);
  }

  static void setMappedElementRef(List<TreeNodeColumn> mappings, String columnId, String elementRef) {
    if (columnId == null) {
      return;
    }
    TreeNodeColumn mapping = mappings.stream()
        .filter(existingMapping -> columnId.equals(existingMapping.getColumnRef()))
        .findFirst()
        .orElse(null);
    if (mapping == null) {
      mapping = new TreeNodeColumn();
      mapping.setColumnRef(columnId);
      mappings.add(mapping);
    }
    mapping.setElementRef(elementRef);
  }

  /** Copies everything on the mapping, including its per-node display-mode override. */
  static TreeNodeColumn copyOf(TreeNodeColumn source) {
    TreeNodeColumn copy = new TreeNodeColumn();
    copy.setColumnRef(source.getColumnRef());
    copy.setElementRef(source.getElementRef());
    if (source.getConfiguration() != null) {
      TreeNodeColumn.Configuration configuration = new TreeNodeColumn.Configuration();
      configuration.setAttachmentDisplayMode(source.getConfiguration().getAttachmentDisplayMode());
      configuration.setMultiSelectDisplayMode(source.getConfiguration().getMultiSelectDisplayMode());
      copy.setConfiguration(configuration);
    }
    return copy;
  }

  /** All field element ids of the given Document Model, walking its root groups recursively; empty if unknown. */
  public static List<String> fieldOptionsFor(@NonNull ProjectItem projectItem, String documentModelId) {
    if (documentModelId == null) {
      return List.of();
    }
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.DOCUMENT).stream()
        .filter(documentModel -> documentModelId.equals(documentModel.getId()))
        .findFirst()
        .map(documentModel -> collectFieldIds((DocumentModel) documentModel))
        .orElse(List.of());
  }

  /** The ids of all Document Models in the project, for a combo. */
  public static List<String> documentModelIds(@NonNull ProjectItem projectItem) {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.DOCUMENT).stream()
        .map(A12Model::getId)
        .sorted()
        .toList();
  }

  private static List<String> collectFieldIds(DocumentModel documentModel) {
    List<String> ids = new ArrayList<>();
    if (documentModel.getContent() != null && documentModel.getContent().getModelRoot() != null
        && documentModel.getContent().getModelRoot().getRootGroups() != null) {
      for (GroupElement group : documentModel.getContent().getModelRoot().getRootGroups()) {
        collectFieldIds(group, ids);
      }
    }
    return ids;
  }

  private static void collectFieldIds(GroupElement group, List<String> ids) {
    if (group.getGroup() == null || group.getGroup().getElements() == null) {
      return;
    }
    for (Element child : group.getGroup().getElements()) {
      if (child instanceof FieldElement field && field.getId() != null) {
        ids.add(field.getId());
      }
      else if (child instanceof GroupElement childGroup) {
        collectFieldIds(childGroup, ids);
      }
    }
  }
}
