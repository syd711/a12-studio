package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.form.FormReferences;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Cleanup" tab for the Form Model editor: flags {@link FieldConfigEntry}/{@link GroupConfigEntry} rows that
 * are either dangling (the referenced field/group no longer exists in the linked Document Model - already a
 * validation error via {@code FormFieldReferenceValidator}, but previously with no fix action) or orphaned
 * (the field/group still exists, but no Control/Repeat in the Screens tree references it any more - not an
 * error at all today, just dead config data), mirroring SME's Cleanup tab and its "Clean All" action.
 */
public class CleanupPanelController implements Initializable {

  private enum Reason {DANGLING, UNREFERENCED}

  private record Row(Object entry, String reference, boolean isField, Reason reason) {
  }

  @FXML
  private TableView<Row> table;
  @FXML
  private TableColumn<Row, String> elementColumn;
  @FXML
  private TableColumn<Row, String> reasonColumn;
  @FXML
  private Button cleanAllButton;
  @FXML
  private Label noneLabel;
  @FXML
  private Node tableContainer;

  private FormModel model;
  private List<ElementIndex> indexes = List.of();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    elementColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().reference()));
    reasonColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
        data.getValue().reason() == Reason.DANGLING
            ? StudioBundle.get("cleanup_reason_dangling")
            : StudioBundle.get("cleanup_reason_unreferenced")));
    cleanAllButton.setOnAction(e -> onCleanAll());
  }

  public void setModel(@NonNull FormModel model, @NonNull List<ElementIndex> indexes) {
    this.model = model;
    this.indexes = indexes;
    refresh();
  }

  private void refresh() {
    List<Row> rows = new ArrayList<>();
    if (model.getContent() != null) {
      if (model.getContent().getFieldConfiguration() != null) {
        for (FieldConfigEntry entry : model.getContent().getFieldConfiguration().getField()) {
          reasonFor(entry.getElementRef(), true).ifPresent(reason ->
              rows.add(new Row(entry, displayName(entry.getElementRef()), true, reason)));
        }
      }
      if (model.getContent().getGroupConfiguration() != null) {
        for (GroupConfigEntry entry : model.getContent().getGroupConfiguration().getGroup()) {
          reasonFor(entry.getGroupRef(), false).ifPresent(reason ->
              rows.add(new Row(entry, displayName(entry.getGroupRef()), false, reason)));
        }
      }
    }
    table.getItems().setAll(rows);
    boolean empty = rows.isEmpty();
    tableContainer.setVisible(!empty);
    tableContainer.setManaged(!empty);
    noneLabel.setVisible(empty);
    noneLabel.setManaged(empty);
    cleanAllButton.setDisable(empty);
  }

  private java.util.Optional<Reason> reasonFor(@Nullable String reference, boolean isField) {
    if (reference == null || reference.isBlank()) {
      return java.util.Optional.empty();
    }
    boolean resolvable = indexes.stream().anyMatch(index -> index.isResolvable(reference));
    if (!resolvable) {
      return java.util.Optional.of(Reason.DANGLING);
    }
    boolean referenced = isField ? FormReferences.isFieldReferenced(model, reference) : FormReferences.isGroupReferenced(model, reference);
    return referenced ? java.util.Optional.empty() : java.util.Optional.of(Reason.UNREFERENCED);
  }

  private String displayName(@Nullable String reference) {
    if (reference == null) {
      return "";
    }
    for (ElementIndex index : indexes) {
      String path = index.resolveDisplayPath(reference);
      if (path != null) {
        return path;
      }
    }
    return reference;
  }

  private void onCleanAll() {
    List<Row> rows = List.copyOf(table.getItems());
    for (Row row : rows) {
      if (row.isField() && model.getContent().getFieldConfiguration() != null) {
        model.getContent().getFieldConfiguration().getField().remove(row.entry());
      }
      else if (!row.isField() && model.getContent().getGroupConfiguration() != null) {
        model.getContent().getGroupConfiguration().getGroup().remove(row.entry());
      }
    }
    commitChange();
    refresh();
  }

  private void commitChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }
}
