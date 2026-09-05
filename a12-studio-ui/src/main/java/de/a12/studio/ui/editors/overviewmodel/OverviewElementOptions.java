package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DateFragmentFieldType;
import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TimeFieldType;
import de.a12.studio.models.Label;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.overview.OverviewElementResolution;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
   * When an Overview Model is bound through a Query Model rather than directly through a Document Model,
   * every field-reference picker should only offer fields the Query Model actually projects ({@code
   * QueryModelContent.fields}), mirroring SME's {@code getExtendedGetDmCandidates}. Rather than threading an
   * "allowed ids" parameter through every picker call site (the Columns/Sorting/Accessibility/Custom
   * Selection Of Fields/Section Data/Custom Filter Configuration panels, plus the Column dialog), the
   * restriction is attached to the specific {@link ElementIndex} instance it applies to - set once in {@code
   * OverviewModelEditorController#refreshDocumentModelIndex()} - and consulted transparently by {@link
   * #elementIds}. A {@link WeakHashMap} means a freshly rebuilt index (e.g. after switching Document
   * Models/Query Models, which always constructs a new {@link ElementIndex}) starts unrestricted with no
   * explicit clearing required, and old entries are collected once their index is no longer referenced.
   */
  private static final Map<ElementIndex, Set<String>> allowedFieldIdsByIndex = new WeakHashMap<>();

  /** Restricts every field-reference picker backed by {@code index} to {@code allowedIds}; {@code null} (or
   * an empty set) removes any restriction, so every element in {@code index} is offered again. */
  public static void restrictFieldIds(ElementIndex index, Set<String> allowedIds) {
    if (index == null) {
      return;
    }
    if (allowedIds == null || allowedIds.isEmpty()) {
      allowedFieldIdsByIndex.remove(index);
    }
    else {
      allowedFieldIdsByIndex.put(index, allowedIds);
    }
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
    Set<String> allowedIds = allowedFieldIdsByIndex.get(index);
    return index.allElements().stream()
        .filter(element -> element.getId() != null)
        .filter(element -> allowedIds == null || allowedIds.contains(element.getId()))
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

  /** The {@code fieldId}'s multilingual label, i.e. a {@link de.a12.studio.models.documentmodel.FieldElement}'s
   * own {@code Field.label} - used by the Custom Filter Configuration editor's "Generate from document fields"
   * actions to seed a generated {@link de.a12.studio.models.overviewmodel.FilterGroup}'s label. Empty if {@code
   * index} is {@code null}, {@code fieldId} doesn't resolve, or the resolved element isn't a field. */
  public static List<Label> fieldLabel(ElementIndex index, String fieldId) {
    if (index == null || fieldId == null) {
      return List.of();
    }
    return index.allElements().stream()
        .filter(element -> fieldId.equals(element.getId()) && element instanceof FieldElement fieldElement && fieldElement.getField() != null)
        .findFirst()
        .map(element -> ((FieldElement) element).getField().getLabel())
        .orElse(List.of());
  }

  /** A short, stable type name derived from {@code fieldId}'s {@link FieldType} (e.g. {@code "string"}, {@code
   * "number"}), mirroring the field-type groupings the platform docs use for Custom Filter Configuration's
   * per-field-type Filter Item options ("Filter Items"). {@code null} if {@code index} is {@code null}, {@code
   * fieldId} doesn't resolve to a field, or the field has no recognized type yet. */
  public static String filterItemFieldType(ElementIndex index, String fieldId) {
    if (index == null || fieldId == null) {
      return null;
    }
    return index.allElements().stream()
        .filter(element -> fieldId.equals(element.getId()) && element instanceof FieldElement fieldElement && fieldElement.getField() != null)
        .findFirst()
        .map(element -> filterItemFieldType(((FieldElement) element).getField().getFieldType()))
        .orElse(null);
  }

  /** Every element id in {@code index} whose field type is Enumeration - used to restrict a reference
   * column's dynamic-suffix field picker ({@code suffixRef}) to fields that can actually supply a per-row
   * suffix value. */
  public static List<String> enumerationElementIds(ElementIndex index) {
    if (index == null) {
      return List.of();
    }
    return elementIds(index).stream()
        .filter(id -> "enumeration".equals(filterItemFieldType(index, id)))
        .toList();
  }

  /** A reference column's element-specific behavior depends on what kind of Document-Model element its
   * {@code elementRef} resolves to - mirrors SME's own {@code elementType} derivation ({@code
   * updateColumnByElementRef.ts}), which drives which of the column's type-specific fields ({@code
   * attachmentDisplayMode}, {@code multiSelectDisplayMode}, {@code suffix}/{@code suffixRef}/{@code summary})
   * are shown at all. Like SME's, this is derived at edit time from the resolved element and never itself
   * persisted to the model. */
  public enum ElementKind {
    ATTACHMENT, NUMBER, MULTI_SELECT, PLAIN
  }

  /** {@link ElementKind#PLAIN} if {@code index} is {@code null} or {@code elementRef} doesn't resolve. */
  public static ElementKind elementKind(ElementIndex index, String elementRef) {
    if (index == null) {
      return ElementKind.PLAIN;
    }
    Element element = OverviewElementResolution.resolve(index, elementRef);
    if (element == null) {
      return ElementKind.PLAIN;
    }
    if (OverviewElementResolution.isAttachment(index, element)) {
      return ElementKind.ATTACHMENT;
    }
    if (OverviewElementResolution.isMultiSelect(index, element)) {
      return ElementKind.MULTI_SELECT;
    }
    if (element instanceof FieldElement fieldElement && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof NumberFieldType) {
      return ElementKind.NUMBER;
    }
    return ElementKind.PLAIN;
  }

  private static String filterItemFieldType(FieldType fieldType) {
    if (fieldType instanceof StringFieldType) {
      return "string";
    }
    if (fieldType instanceof NumberFieldType) {
      return "number";
    }
    if (fieldType instanceof BooleanFieldType) {
      return "boolean";
    }
    if (fieldType instanceof ConfirmFieldType) {
      return "confirm";
    }
    if (fieldType instanceof EnumerationFieldType) {
      return "enumeration";
    }
    if (fieldType instanceof DateTimeFieldType) {
      return "datetime";
    }
    if (fieldType instanceof DateRangeFieldType) {
      return "daterange";
    }
    if (fieldType instanceof DateFragmentFieldType) {
      return "datefragment";
    }
    if (fieldType instanceof DateFieldType) {
      return "date";
    }
    if (fieldType instanceof TimeFieldType) {
      return "time";
    }
    return null;
  }
}
