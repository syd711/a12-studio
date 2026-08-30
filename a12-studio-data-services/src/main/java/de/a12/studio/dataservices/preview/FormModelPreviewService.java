package de.a12.studio.dataservices.preview;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.LocalizedText;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.MultilingualText;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.projects.ProjectItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@link FormModel} into wireframe-preview DTOs: one {@link PreviewScreenDto} per screen, with its
 * {@code Section}/{@code MultiColumnSection} nesting preserved and its {@code Control}/repeat-column fields
 * resolved (via {@link DocumentModelFieldResolver}) against the Document Model bound via {@link
 * ModelReference#PURPOSE_DATA_BINDING} - the same elementRef-lookup approach {@link
 * ApplicationModelPreviewService} uses for Overview Model columns. Pure data transformation over {@code
 * a12-studio-models} types - no JavaFX, no HTTP.
 */
public class FormModelPreviewService {

  public PreviewFormDto buildPreview(ProjectItem projectItem) {
    FormModel model = (FormModel) projectItem.getModel();
    FormModelContent content = model.getContent();

    DocumentModel documentModel = DocumentModelFieldResolver.resolveReferencedDocumentModel(
        model, ModelReference.PURPOSE_DATA_BINDING, projectItem);
    Map<String, Element> elementsById = DocumentModelFieldResolver.index(documentModel);
    Map<String, FieldConfigEntry> fieldConfigByRef = indexFieldConfig(content);

    List<PreviewScreenDto> screens = content.getScreens().stream()
        .map(screen -> toScreenDto(screen, elementsById, fieldConfigByRef))
        .toList();
    return new PreviewFormDto(screens);
  }

  private PreviewScreenDto toScreenDto(Screen screen, Map<String, Element> elementsById, Map<String, FieldConfigEntry> fieldConfigByRef) {
    String title = localizedText(screen.getTitle());
    List<PreviewScreenElementDto> elements = toElementDtos(screen.getScreenElements(), elementsById, fieldConfigByRef);
    return new PreviewScreenDto(screen.getId(), screen.getName(), title, elements);
  }

  private List<PreviewScreenElementDto> toElementDtos(List<ScreenElement> screenElements, Map<String, Element> elementsById,
      Map<String, FieldConfigEntry> fieldConfigByRef) {
    return screenElements.stream().map(element -> toElementDto(element, elementsById, fieldConfigByRef)).toList();
  }

  private PreviewScreenElementDto toElementDto(ScreenElement element, Map<String, Element> elementsById,
      Map<String, FieldConfigEntry> fieldConfigByRef) {
    String title = localizedText(element.getTitle());

    if (element instanceof Section section) {
      return new PreviewScreenElementDto("Section", title, List.of(),
          toElementDtos(section.getScreenElements(), elementsById, fieldConfigByRef));
    }
    if (element instanceof MultiColumnSection multiColumnSection) {
      return new PreviewScreenElementDto("MultiColumnSection", title, List.of(),
          toElementDtos(multiColumnSection.getScreenElements(), elementsById, fieldConfigByRef));
    }
    if (element instanceof ControlGrid controlGrid) {
      return new PreviewScreenElementDto("ControlGrid", title, resolveControlGridFields(controlGrid, elementsById, fieldConfigByRef), List.of());
    }
    if (element instanceof AbstractRepeat repeat) {
      String repeatTitle = title != null ? title : localizedText(repeat.getLabel());
      return new PreviewScreenElementDto(element.getClass().getSimpleName(), repeatTitle,
          resolveRepeatColumnFields(repeat, elementsById), List.of());
    }
    return new PreviewScreenElementDto(element.getClass().getSimpleName(), title, List.of(), List.of());
  }

  private List<PreviewFieldDto> resolveControlGridFields(ControlGrid controlGrid, Map<String, Element> elementsById,
      Map<String, FieldConfigEntry> fieldConfigByRef) {
    List<PreviewFieldDto> fields = new ArrayList<>();
    for (Row row : controlGrid.getRow()) {
      for (Cell cell : row.getCell()) {
        fields.add(toFieldDto(cell, elementsById, fieldConfigByRef));
      }
    }
    return fields;
  }

  private PreviewFieldDto toFieldDto(Cell cell, Map<String, Element> elementsById, Map<String, FieldConfigEntry> fieldConfigByRef) {
    if (cell instanceof Control control) {
      Element element = elementsById.get(control.getElementRef());
      String label = resolveControlLabel(control, fieldConfigByRef.get(control.getElementRef()), element);
      return new PreviewFieldDto(label, DocumentModelFieldResolver.fieldType(element));
    }
    String label = cell.getName() != null ? cell.getName() : cell.getId();
    return new PreviewFieldDto(label, cell.getClass().getSimpleName());
  }

  private String resolveControlLabel(Control control, FieldConfigEntry fieldConfig, Element element) {
    String label = localizedText(control.getLabel());
    if (label != null) {
      return label;
    }
    if (fieldConfig != null) {
      label = localizedText(fieldConfig.getLabel());
      if (label != null) {
        return label;
      }
    }
    label = DocumentModelFieldResolver.fieldLabel(element);
    if (label != null) {
      return label;
    }
    if (element != null) {
      return element.getName();
    }
    return control.getElementRef();
  }

  private List<PreviewFieldDto> resolveRepeatColumnFields(AbstractRepeat repeat, Map<String, Element> elementsById) {
    List<PreviewFieldDto> fields = new ArrayList<>();
    for (RepeatOverviewColumn column : repeat.getRepeatOverviewColumn()) {
      if (column instanceof FieldBasedRepeatOverviewColumn fieldColumn) {
        Element element = elementsById.get(fieldColumn.getElementRef());
        String label = DocumentModelFieldResolver.fieldLabel(element);
        if (label == null) {
          label = element != null ? element.getName() : fieldColumn.getElementRef();
        }
        fields.add(new PreviewFieldDto(label, DocumentModelFieldResolver.fieldType(element)));
      }
      else {
        fields.add(new PreviewFieldDto(column.getId(), column.getClass().getSimpleName()));
      }
    }
    return fields;
  }

  private Map<String, FieldConfigEntry> indexFieldConfig(FormModelContent content) {
    Map<String, FieldConfigEntry> byRef = new HashMap<>();
    if (content.getFieldConfiguration() != null) {
      content.getFieldConfiguration().getField().forEach(entry -> byRef.put(entry.getElementRef(), entry));
    }
    return byRef;
  }

  private String localizedText(LocalizedText localizedText) {
    if (localizedText instanceof MultilingualText multilingualText && multilingualText.getMultilingualText() != null) {
      return DocumentModelFieldResolver.firstLabelText(multilingualText.getMultilingualText().getText());
    }
    return null;
  }
}
