package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.FilterSection;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Edits an {@link OverviewModel}'s {@code content.configuration.filterConfiguration.sectionData}: one
 * non-reorderable row per {@link FilterSection}, summarizing its Label (the first localized text) and Fields
 * (the referenced Document Model elements, resolved via {@link OverviewElementOptions}). Not bound to a single
 * {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by e.g.
 * {@link OverviewMultiSelectionPanelController}. Clicking a row (or its Edit button) opens {@link
 * Dialogs#showSectionForEdit}, which edits the section's Label and Fields.
 */
public class OverviewSectionDataPanelController extends AbstractPropertyEditor {

  // Matches the fixed-width spacer reserved after the "Fields" header in overview-section-data-panel.fxml, so
  // rows' edit/delete buttons line up under it instead of stealing space from the Label/Fields columns.
  private static final double ACTIONS_BOX_WIDTH = 70.0;

  @FXML
  private HBox sectionColumnHeaders;

  @FXML
  private VBox sectionRows;

  @FXML
  private Label sectionsEmptyLabel;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    rebuildRows();
  }

  /** Irrelevant for {@link FilterConfiguration#FILTER_MODE_CUSTOM_FILTER}, which uses Filter Groups instead -
   * hidden only for that filter mode, see {@link OverviewModelEditorController}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /** Re-points every row's Fields summary at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex) {
    this.documentModelIndex = documentModelIndex;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    Dialogs.showSectionForAdd(Studio.stage, documentModelIndex).ifPresent(section -> {
      getSections().add(section);
      rebuildRows();
      commitHeaderChange();
    });
  }

  private List<FilterSection> getSections() {
    return ensureFilterConfiguration().getSectionData();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    sectionRows.getChildren().clear();

    List<FilterSection> sections = currentSections();
    boolean empty = sections.isEmpty();
    sectionColumnHeaders.setVisible(!empty);
    sectionColumnHeaders.setManaged(!empty);
    sectionsEmptyLabel.setVisible(empty);
    sectionsEmptyLabel.setManaged(empty);

    for (int index = 0; index < sections.size(); index++) {
      sectionRows.getChildren().add(createRow(sections.get(index), index));
    }
  }

  private List<FilterSection> currentSections() {
    FilterConfiguration filterConfiguration = currentFilterConfiguration();
    return filterConfiguration != null ? filterConfiguration.getSectionData() : List.of();
  }

  private HBox createRow(FilterSection section, int index) {
    Label labelCell = new Label(labelSummary(section));
    labelCell.setId("sectionLabel-" + index);
    labelCell.setMaxWidth(Double.MAX_VALUE);
    labelCell.setAlignment(Pos.CENTER_LEFT);
    makeClickableToEdit(labelCell, section);

    VBox fieldsCell = createFieldsCell(section, index);

    GridPane contentGrid = new GridPane();
    contentGrid.setHgap(10.0);
    contentGrid.setMaxWidth(Double.MAX_VALUE);
    ColumnConstraints labelColumn = new ColumnConstraints();
    labelColumn.setPercentWidth(33.33);
    ColumnConstraints fieldsColumn = new ColumnConstraints();
    fieldsColumn.setPercentWidth(66.67);
    contentGrid.getColumnConstraints().addAll(labelColumn, fieldsColumn);
    contentGrid.add(labelCell, 0, 0);
    contentGrid.add(fieldsCell, 1, 0);
    HBox.setHgrow(contentGrid, Priority.ALWAYS);

    HBox actionsBox = createActionsBox(section);
    actionsBox.setPrefWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMinWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMaxWidth(ACTIONS_BOX_WIDTH);
    HBox.setHgrow(actionsBox, Priority.NEVER);

    HBox row = new HBox(10.0, contentGrid, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  /** One left-aligned path per field, stacked vertically, mirroring {@code labelCell}'s click-to-edit. */
  private VBox createFieldsCell(FilterSection section, int index) {
    VBox fieldsCell = new VBox(4.0);
    fieldsCell.setId("sectionFields-" + index);
    fieldsCell.setAlignment(Pos.CENTER_LEFT);
    fieldsCell.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(fieldsCell, Priority.ALWAYS);
    for (String path : fieldPaths(section)) {
      Label pathLabel = new Label(path);
      pathLabel.getStyleClass().add("path-chip");
      pathLabel.setWrapText(true);
      pathLabel.setMaxWidth(Double.MAX_VALUE);
      fieldsCell.getChildren().add(pathLabel);
    }
    makeClickableToEdit(fieldsCell, section);
    return fieldsCell;
  }

  private void makeClickableToEdit(Node node, FilterSection section) {
    node.setCursor(Cursor.HAND);
    node.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(section);
      }
    });
  }

  /** The first localized Label text, mirroring SME's own row summary (see {@code omEditor.tsx}'s {@code
   * firstLabel}). */
  private static String labelSummary(FilterSection section) {
    return section.getLabel().stream()
        .map(de.a12.studio.models.Label::getText)
        .filter(text -> text != null && !text.isBlank())
        .findFirst()
        .orElse("");
  }

  /** Every field's path (see {@link ElementIndex#getPath}), one per row. */
  private List<String> fieldPaths(FilterSection section) {
    return section.getFields().stream()
        .map(FieldRef::getFieldId)
        .filter(fieldId -> fieldId != null)
        .map(fieldId -> OverviewElementOptions.displayPath(documentModelIndex, fieldId))
        .collect(Collectors.toList());
  }

  private void openEditDialog(FilterSection section) {
    if (Dialogs.showSectionForEdit(Studio.stage, documentModelIndex, section)) {
      rebuildRows();
      commitHeaderChange();
    }
  }

  private HBox createActionsBox(FilterSection section) {
    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(section));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_section"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getSections().remove(section);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private FilterConfiguration ensureFilterConfiguration() {
    OverviewConfiguration configuration = ensureConfiguration();
    if (configuration.getFilterConfiguration() == null) {
      configuration.setFilterConfiguration(new FilterConfiguration());
    }
    return configuration.getFilterConfiguration();
  }

  private FilterConfiguration currentFilterConfiguration() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    return configuration != null ? configuration.getFilterConfiguration() : null;
  }
}
