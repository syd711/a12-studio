package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import javafx.scene.control.ComboBox;
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

  /** {@code null} if {@code documentModel} is {@code null} or has no model root yet. */
  public static ElementIndex indexOf(DocumentModel documentModel) {
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return null;
    }
    return new ElementIndex(documentModel);
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
    return index.allElements().stream()
        .filter(element -> elementId.equals(element.getId()))
        .findFirst()
        .map(index::getPath)
        .orElse(elementId);
  }

  /** Renders ids as their display path in a {@code ComboBox<String>} while keeping the id as the stored value. */
  public static void applyElementRefConverter(ComboBox<String> comboBox, ElementIndex index) {
    comboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(String elementId) {
        return elementId == null ? "" : displayPath(index, elementId);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
  }
}
