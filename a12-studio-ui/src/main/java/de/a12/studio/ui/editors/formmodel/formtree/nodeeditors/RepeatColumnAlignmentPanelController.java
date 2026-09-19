package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Alignment;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * "Alignment" property editor for a selected {@link RepeatOverviewColumn}: independent horizontal
 * ({@code left}/{@code center}/{@code right}) and vertical ({@code top}/{@code middle}/{@code bottom})
 * alignment overrides for the column's header and content cells ({@link
 * RepeatOverviewColumn#getSpecificHorizontalAlignment()}/{@link
 * RepeatOverviewColumn#getSpecificVerticalAlignment()}). "Default" - written as an absent key - lets the
 * platform choose from the data type, repeat type and cell type; an override with neither side set is removed
 * again, so the column doesn't gain an empty {@code {}}. Not tied to a document-model {@code Element}, so it
 * follows the model-header pattern.
 */
public class RepeatColumnAlignmentPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> headHorizontalCombo;
  @FXML
  private ComboBox<String> bodyHorizontalCombo;
  @FXML
  private ComboBox<String> headVerticalCombo;
  @FXML
  private ComboBox<String> bodyVerticalCombo;

  private RepeatOverviewColumn column;

  // Which of the column's two alignment properties an edit goes to.
  private record Axis(Function<RepeatOverviewColumn, Alignment> getter, BiConsumer<RepeatOverviewColumn, Alignment> setter) {
  }

  private static final Axis HORIZONTAL =
      new Axis(RepeatOverviewColumn::getSpecificHorizontalAlignment, RepeatOverviewColumn::setSpecificHorizontalAlignment);
  private static final Axis VERTICAL =
      new Axis(RepeatOverviewColumn::getSpecificVerticalAlignment, RepeatOverviewColumn::setSpecificVerticalAlignment);

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    configure(headHorizontalCombo, "left", "center", "right");
    configure(bodyHorizontalCombo, "left", "center", "right");
    configure(headVerticalCombo, "top", "middle", "bottom");
    configure(bodyVerticalCombo, "top", "middle", "bottom");

    bindComboBox(headHorizontalCombo, (el, value) -> update(HORIZONTAL, alignment -> alignment.setHead(value)));
    bindComboBox(bodyHorizontalCombo, (el, value) -> update(HORIZONTAL, alignment -> alignment.setBody(value)));
    bindComboBox(headVerticalCombo, (el, value) -> update(VERTICAL, alignment -> alignment.setHead(value)));
    bindComboBox(bodyVerticalCombo, (el, value) -> update(VERTICAL, alignment -> alignment.setBody(value)));
  }

  public void setColumn(@NonNull RepeatOverviewColumn column) {
    this.column = column;
    Alignment horizontal = column.getSpecificHorizontalAlignment();
    Alignment vertical = column.getSpecificVerticalAlignment();
    setFieldValue(headHorizontalCombo, horizontal == null ? null : horizontal.getHead());
    setFieldValue(bodyHorizontalCombo, horizontal == null ? null : horizontal.getBody());
    setFieldValue(headVerticalCombo, vertical == null ? null : vertical.getHead());
    setFieldValue(bodyVerticalCombo, vertical == null ? null : vertical.getBody());
  }

  private void update(Axis axis, Consumer<Alignment> change) {
    Alignment alignment = axis.getter().apply(column);
    if (alignment == null) {
      alignment = new Alignment();
      axis.setter().accept(column, alignment);
    }
    change.accept(alignment);
    if (alignment.isBlank()) {
      axis.setter().accept(column, null);
    }
  }

  private static void configure(ComboBox<String> combo, String... values) {
    combo.getItems().add(null);
    combo.getItems().addAll(values);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return StudioBundle.get("repeat_column_alignment." + (value == null ? "default" : value));
      }

      @Override
      public String fromString(String displayName) {
        return null;
      }
    });
  }
}
