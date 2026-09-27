package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions.ElementKind;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
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
    populate(grid, noColumnsLabel, columns, fieldOptions, null, mappings, onChange);
  }

  /** As above, showing each field by its path in {@code elementIndex} (see {@link #elementIndexFor}) instead of its id. */
  public static void populate(@NonNull GridPane grid, @NonNull Label noColumnsLabel, @NonNull List<TreeColumn> columns,
      @NonNull List<String> fieldOptions, ElementIndex elementIndex, @NonNull List<TreeNodeColumn> mappings, @NonNull Runnable onChange) {
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
      String mapped = mappedElementRef(mappings, column.getId());
      // A mapping whose field is no longer offered stays visible (and is kept), as documented on the class.
      List<String> items = new ArrayList<>(fieldOptions);
      if (mapped != null && !items.contains(mapped)) {
        items.add(mapped);
      }
      OverviewElementOptions.applyElementRefConverter(elementField, elementIndex);
      elementField.getItems().setAll(items);
      elementField.setValue(mapped);
      elementField.valueProperty().addListener((observable, oldValue, newValue) -> {
        setMappedElementRef(mappings, column.getId(), newValue, elementIndex);
        // The display mode combo depends on the kind of the chosen field, so the grid is rebuilt.
        populate(grid, noColumnsLabel, columns, fieldOptions, elementIndex, mappings, onChange);
        onChange.run();
      });

      grid.addRow(row, columnLabel, elementField);
      ElementKind kind = OverviewElementOptions.elementKind(elementIndex, mapped);
      TreeNodeColumn mapping = mapped != null ? mappings.stream().filter(candidate -> column.getId() != null
          && column.getId().equals(candidate.getColumnRef())).findFirst().orElse(null) : null;
      if (mapping != null && (kind == ElementKind.ATTACHMENT || kind == ElementKind.MULTI_SELECT)) {
        grid.add(displayModeCombo(mapping, kind, onChange), 2, row);
      }
      row++;
    }
  }

  /** The combo choosing how an attachment / multi-select field is displayed in {@code mapping}'s column. */
  public static ComboBox<String> displayModeCombo(@NonNull TreeNodeColumn mapping, @NonNull ElementKind kind, @NonNull Runnable onChange) {
    ComboBox<String> combo = new ComboBox<>();
    combo.setId("displayMode-" + mapping.getColumnRef());
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.getItems().setAll(displayModes(kind));
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return humanize(value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    combo.setValue(displayMode(mapping, kind));
    combo.valueProperty().addListener((observable, oldValue, newValue) -> {
      setDisplayMode(mapping, kind, newValue);
      onChange.run();
    });
    return combo;
  }

  /** The display modes SME offers for a field of {@code kind}; none for any other kind. */
  public static List<String> displayModes(ElementKind kind) {
    return switch (kind) {
      case ATTACHMENT -> List.of(Column.ATTACHMENT_DISPLAY_MODE_PREVIEW, Column.ATTACHMENT_DISPLAY_MODE_ICON_WITH_FILE_NAME,
          Column.ATTACHMENT_DISPLAY_MODE_ICON, Column.ATTACHMENT_DISPLAY_MODE_FILE_NAME);
      case MULTI_SELECT -> List.of(Column.MULTI_SELECT_DISPLAY_MODE_DEFAULT, Column.MULTI_SELECT_DISPLAY_MODE_COMMA_SEPARATED);
      default -> List.of();
    };
  }

  /** {@code mapping}'s display mode for a field of {@code kind}: the stored one, or SME's default (preview / default). */
  public static String displayMode(TreeNodeColumn mapping, ElementKind kind) {
    TreeNodeColumn.Configuration configuration = mapping.getConfiguration();
    String stored = configuration == null ? null
        : kind == ElementKind.ATTACHMENT ? configuration.getAttachmentDisplayMode() : configuration.getMultiSelectDisplayMode();
    return stored != null && displayModes(kind).contains(stored) ? stored : displayModes(kind).get(0);
  }

  /** Stores {@code mode} as the display mode for a field of {@code kind}, leaving the other kind's key unset. */
  public static void setDisplayMode(TreeNodeColumn mapping, ElementKind kind, String mode) {
    TreeNodeColumn.Configuration configuration = new TreeNodeColumn.Configuration();
    if (kind == ElementKind.ATTACHMENT) {
      configuration.setAttachmentDisplayMode(mode);
    }
    else {
      configuration.setMultiSelectDisplayMode(mode);
    }
    mapping.setConfiguration(configuration);
  }

  /**
   * Keeps {@code mapping}'s display mode in step with its field, as SME's {@code handleColumnDisplayMode} does: an
   * attachment / multi-select field gets a (valid) display mode - the default when it has none - any other field none.
   */
  public static void syncDisplayMode(TreeNodeColumn mapping, ElementKind kind) {
    if (kind == ElementKind.ATTACHMENT || kind == ElementKind.MULTI_SELECT) {
      setDisplayMode(mapping, kind, displayMode(mapping, kind));
    }
    else {
      mapping.setConfiguration(null);
    }
  }

  /** {@code value} with its underscores as spaces, capitalized: {@code icon_with_file_name} becomes "Icon With File Name". */
  static String humanize(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    StringBuilder result = new StringBuilder();
    for (String word : value.split("_")) {
      if (!result.isEmpty()) {
        result.append(' ');
      }
      result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
    }
    return result.toString();
  }

  public static String mappedElementRef(List<TreeNodeColumn> mappings, String columnId) {
    return mappings.stream()
        .filter(mapping -> columnId != null && columnId.equals(mapping.getColumnRef()))
        .map(TreeNodeColumn::getElementRef)
        .findFirst()
        .orElse(null);
  }

  /**
   * As {@link #setMappedElementRef(List, String, String)}, then keeps the mapping's display mode in step with the new field
   * (see {@link #syncDisplayMode}); {@code elementIndex} resolves the field's kind, without one nothing is touched.
   */
  public static void setMappedElementRef(List<TreeNodeColumn> mappings, String columnId, String elementRef, ElementIndex elementIndex) {
    setMappedElementRef(mappings, columnId, elementRef);
    if (elementIndex == null || columnId == null) {
      return;
    }
    mappings.stream().filter(mapping -> columnId.equals(mapping.getColumnRef())).findFirst()
        .ifPresent(mapping -> syncDisplayMode(mapping, OverviewElementOptions.elementKind(elementIndex, elementRef)));
  }

  public static void setMappedElementRef(List<TreeNodeColumn> mappings, String columnId, String elementRef) {
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

  /**
   * The ids of all elements of the given Document Model that a column can show - fields and also groups (e.g. an
   * attachment group), included models' elements too - sorted by path; empty if the model is unknown.
   */
  public static List<String> fieldOptionsFor(@NonNull ProjectItem projectItem, String documentModelId) {
    return OverviewElementOptions.elementIds(elementIndexFor(projectItem, documentModelId));
  }

  /**
   * The element index of {@code documentModelId} (a Document Model, or a Combination Model standing in for one), used to
   * show a mapped field id as its path; {@code null} if it is unknown, so callers fall back to the raw id.
   */
  public static ElementIndex elementIndexFor(@NonNull ProjectItem projectItem, String documentModelId) {
    if (documentModelId == null) {
      return null;
    }
    DocumentModel documentModel = ProjectDocumentModels.resolveDocumentModelForFieldReferences(documentModelId);
    return OverviewElementOptions.indexOf(documentModel, ProjectDocumentModels.getOtherDocumentModels(projectItem));
  }

  /** {@code elementId}'s path in {@code elementIndex}, or the id itself when there is no index or it doesn't resolve. */
  public static String displayPath(ElementIndex elementIndex, String elementId) {
    return OverviewElementOptions.displayPath(elementIndex, elementId);
  }

  /** The ids of all Document Models in the project, for a combo. */
  public static List<String> documentModelIds(@NonNull ProjectItem projectItem) {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.DOCUMENT).stream()
        .map(A12Model::getId)
        .sorted()
        .toList();
  }
}
