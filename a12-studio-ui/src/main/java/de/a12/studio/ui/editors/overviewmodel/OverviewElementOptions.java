package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.Comparator;
import java.util.List;

/**
 * Builds the "element reference" option list used by every field-picker in the Overview tab (a column's
 * Element Reference, the filter's custom field list, filter section fields): every element with an id in
 * the referenced Document Model, not just {@code Field}s - a column can also reference a {@code Group}
 * (e.g. an attachment group, see {@code Company_OM.json}'s "Logo" column) - displayed by its full path
 * (see {@link ElementIndex#getPath}) so ids from included models (e.g. {@code include_7c34e_field_0b84d})
 * are disambiguated, mirroring {@code DocumentUniquenessCriterionDialogController}'s field picker.
 */
public final class OverviewElementOptions {

  private OverviewElementOptions() {
  }

  /**
   * {@code null} if {@code documentModel} is {@code null} or has no model root yet. {@code otherModels} is
   * every other Document Model in the project, needed to follow an Include's reference when resolving a
   * field that lives inside an included model (see {@link ElementIndex#resolveDisplayPath}).
   */
  public static ElementIndex indexOf(DocumentModel documentModel, List<DocumentModel> otherModels) {
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return null;
    }
    return new ElementIndex(documentModel, otherModels);
  }

  public static List<String> elementIds(ElementIndex index) {
    if (index == null) {
      return List.of();
    }
    return index.allElements().stream()
        .filter(element -> element.getId() != null)
        .sorted(Comparator.comparing(index::getPath))
        .map(Element::getId)
        .toList();
  }

  public static String displayPath(ElementIndex index, String elementId) {
    if (index == null || elementId == null) {
      return elementId;
    }
    return index.resolveDisplayPath(elementId);
  }

  /** {@code false} for a dangling {@code elementId} - i.e. one that {@link #displayPath} can only echo back
   * as-is rather than resolve to an actual path. {@code true} when {@code index} is {@code null} (nothing to
   * flag yet, e.g. no Document Model selected), so callers only render the "unresolved" state once there's an
   * index to have actually failed against. */
  public static boolean isResolved(ElementIndex index, String elementId) {
    return index == null || index.isResolvable(elementId);
  }

  /** Renders ids as their display path in a {@code ComboBox<String>} while keeping the id as the stored value. */
  public static void applyElementRefConverter(ComboBox<String> comboBox, ElementIndex index) {
    StringConverter<String> converter = new StringConverter<>() {
      @Override
      public String toString(String elementId) {
        return elementId == null ? "" : displayPath(index, elementId);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
    comboBox.setConverter(converter);
    applyMonospaceCells(comboBox, converter);
  }

  /** Renders both the combo box's collapsed display and its popup rows in the shared monospace "path" font
   * (see {@code .path-text} in {@code stylesheet.css}) - the same font used everywhere else a field/column path
   * is shown, but without {@code .path-chip}'s background/border decoration, which is reserved for the Section
   * Data panel's field list. Applied on the {@link ListCell}s themselves (not via a CSS descendant selector on
   * the combo box) because the popup's cells aren't scene-graph descendants of the combo box, so a style class
   * added only to the combo box wouldn't reach them. Shared by {@link OverviewColumnOptions#applyColumnConverter}. */
  static void applyMonospaceCells(ComboBox<String> comboBox, StringConverter<String> converter) {
    comboBox.setCellFactory(listView -> createPathCell(converter));
    comboBox.setButtonCell(createPathCell(converter));
  }

  private static ListCell<String> createPathCell(StringConverter<String> converter) {
    ListCell<String> cell = new ListCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : converter.toString(item));
      }
    };
    cell.getStyleClass().add("path-text");
    return cell;
  }
}
