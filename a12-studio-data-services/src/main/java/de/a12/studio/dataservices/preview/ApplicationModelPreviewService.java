package de.a12.studio.dataservices.preview;

import de.a12.studio.models.Label;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.ModelDescriptor;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.applicationmodel.RegionClearDirective;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.applicationmodel.ViewAddDirective;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.ProjectItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts an {@link ApplicationModel} into wireframe-preview DTOs: the module/menu navigation and, given a
 * selected module/scene, the region/layout tree with boxes labeled by the resolved {@code VIEW_ADD} directive.
 * Where a view's bound model is an {@link OverviewModel}, its columns are further resolved against the
 * Document Model referenced via {@code document-model-for-overview} (see {@link #resolveOverviewFields}) so
 * the preview can render actual field labels/types rather than just the model's name. Pure data transformation
 * over {@code a12-studio-models} types - no JavaFX, no HTTP.
 */
public class ApplicationModelPreviewService {

  private static final String PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW = "document-model-for-overview";

  public PreviewApplicationDto buildPreview(ProjectItem projectItem) {
    ApplicationModel model = (ApplicationModel) projectItem.getModel();
    ApplicationModelContent content = model.getContent();
    List<PreviewModuleDto> modules = content.getModules().stream().map(this::toModuleDto).toList();
    PreviewRegionDto regionTree = toRegionDto(content.getRegion(), Map.of(), Map.of());
    Map<String, String> initialActivity = content.getInitialActivity() != null
        ? content.getInitialActivity().getDescriptor()
        : Map.of();
    return new PreviewApplicationDto(modules, regionTree, initialActivity);
  }

  /**
   * Resolves the given scene's {@code onEnter} directives (in order) onto the model's static region tree:
   * {@code REGION_CLEAR} sets a region's layout label and clears any views previously accumulated for it in
   * this pass, {@code VIEW_ADD} appends a view (with its bound model's fields resolved, if any) to its target
   * region(s). A directive with no explicit region targets {@link ApplicationModelContent#getDefaultRegion()}.
   * Directives that are neither (i.e. an unrecognized/future type, deserialized as {@code GenericDirective})
   * are skipped.
   *
   * @throws IllegalArgumentException if the module or scene name doesn't exist in the model
   */
  public PreviewSceneDto resolveScene(ProjectItem projectItem, String moduleName, String sceneName) {
    ApplicationModel model = (ApplicationModel) projectItem.getModel();
    ApplicationModelContent content = model.getContent();
    Module module = content.getModules().stream()
        .filter(candidate -> moduleName.equals(candidate.getName()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No such module: " + moduleName));

    Scene scene = module.getFlows().stream()
        .flatMap(flow -> flow.getScenes().stream())
        .filter(candidate -> sceneName.equals(candidate.getName()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No such scene: " + sceneName));

    List<String> defaultRegion = content.getDefaultRegion();
    Map<String, String> layoutOverrides = new HashMap<>();
    Map<String, List<PreviewViewDto>> viewsByRegion = new HashMap<>();

    List<Directive> onEnter = scene.getSceneChange() != null ? scene.getSceneChange().getOnEnter() : List.of();
    for (Directive directive : onEnter) {
      List<String> targetRegions = directive.getRegion().isEmpty() ? defaultRegion : directive.getRegion();
      if (directive instanceof RegionClearDirective clear) {
        String layoutName = clear.getLayout() != null ? clear.getLayout().getName() : null;
        for (String regionName : targetRegions) {
          layoutOverrides.put(regionName, layoutName);
          viewsByRegion.put(regionName, new ArrayList<>());
        }
      }
      else if (directive instanceof ViewAddDirective viewAdd) {
        PreviewViewDto view = toViewDto(viewAdd, projectItem);
        for (String regionName : targetRegions) {
          viewsByRegion.computeIfAbsent(regionName, key -> new ArrayList<>()).add(view);
        }
      }
    }

    PreviewRegionDto regionTree = toRegionDto(content.getRegion(), layoutOverrides, viewsByRegion);
    return new PreviewSceneDto(moduleName, sceneName, regionTree);
  }

  private PreviewModuleDto toModuleDto(Module module) {
    List<PreviewLabelDto> labels = module.getMenu() != null
        ? module.getMenu().getLabel().stream().map(label -> new PreviewLabelDto(label.getLocale(), label.getText())).toList()
        : List.of();
    String defaultSceneName = module.getFlows().stream()
        .flatMap(flow -> flow.getScenes().stream())
        .map(Scene::getName)
        .findFirst()
        .orElse(null);
    return new PreviewModuleDto(module.getName(), labels, defaultSceneName);
  }

  private PreviewRegionDto toRegionDto(Region region, Map<String, String> layoutOverrides,
      Map<String, List<PreviewViewDto>> viewsByRegion) {
    String layout = layoutOverrides.containsKey(region.getName())
        ? layoutOverrides.get(region.getName())
        : (region.getLayout() != null ? region.getLayout().getName() : null);
    List<PreviewViewDto> views = viewsByRegion.getOrDefault(region.getName(), List.of());
    List<PreviewRegionDto> subRegions = region.getSubRegions().stream()
        .map(subRegion -> toRegionDto(subRegion, layoutOverrides, viewsByRegion))
        .toList();
    return new PreviewRegionDto(region.getName(), layout, views, subRegions);
  }

  private PreviewViewDto toViewDto(ViewAddDirective viewAdd, ProjectItem contextItem) {
    List<ModelDescriptor> models = viewAdd.getModels();
    if (models.isEmpty()) {
      return new PreviewViewDto(viewAdd.getName(), null, null, List.of());
    }

    ModelDescriptor descriptor = models.get(0);
    List<PreviewFieldDto> fields = resolveFields(descriptor, contextItem);
    String modelType = descriptor.getModelType() != null ? descriptor.getModelType().getValue() : null;
    return new PreviewViewDto(viewAdd.getName(), descriptor.getName(), modelType, fields);
  }

  /**
   * Resolves a view's bound model into its field list. Only {@link OverviewModel} is currently supported (see
   * {@link #resolveOverviewFields}); other model types (e.g. Form Models) fall back to an empty field list, so
   * the view still shows its name/model label without erroring.
   */
  private List<PreviewFieldDto> resolveFields(ModelDescriptor descriptor, ProjectItem contextItem) {
    ProjectItem modelItem = contextItem.findByModelId(descriptor.getName());
    if (modelItem != null && modelItem.getModel() instanceof OverviewModel overviewModel) {
      return resolveOverviewFields(overviewModel, contextItem);
    }
    return List.of();
  }

  /**
   * Mirrors the reference implementation's field resolution: each Overview Model column has an
   * {@code elementRef} pointing into the {@code document-model-for-overview}-referenced Document Model's
   * element tree ({@code modelRoot.rootGroups}, searched recursively through groups). The effective label is
   * the column's own label if set, else the resolved Document Model field's label, else its name.
   */
  private List<PreviewFieldDto> resolveOverviewFields(OverviewModel overviewModel, ProjectItem contextItem) {
    String documentModelId = overviewModel.getModelReferences().stream()
        .filter(reference -> PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
    if (documentModelId == null) {
      return List.of();
    }

    ProjectItem documentItem = contextItem.findByModelId(documentModelId);
    if (documentItem == null || !(documentItem.getModel() instanceof DocumentModel documentModel)) {
      return List.of();
    }

    Map<String, Element> elementsById = new HashMap<>();
    indexElements(documentModel.getContent().getModelRoot().getRootGroups(), elementsById);

    List<PreviewFieldDto> fields = new ArrayList<>();
    for (Column column : overviewModel.getContent().getColumns()) {
      Element element = elementsById.get(column.getElementRef());
      fields.add(new PreviewFieldDto(resolveColumnLabel(column, element), resolveFieldType(element)));
    }
    return fields;
  }

  private void indexElements(List<? extends Element> elements, Map<String, Element> target) {
    for (Element element : elements) {
      target.put(element.getId(), element);
      if (element instanceof GroupElement groupElement) {
        indexElements(groupElement.getGroup().getElements(), target);
      }
    }
  }

  private String resolveColumnLabel(Column column, Element element) {
    String label = firstLabelText(column.getLabel());
    if (label != null) {
      return label;
    }
    if (element instanceof FieldElement fieldElement) {
      label = firstLabelText(fieldElement.getField().getLabel());
      if (label != null) {
        return label;
      }
    }
    if (element != null) {
      return element.getName();
    }
    return column.getElementRef();
  }

  private String resolveFieldType(Element element) {
    if (element instanceof FieldElement fieldElement && fieldElement.getField().getFieldType() != null) {
      return fieldElement.getField().getFieldType().getType();
    }
    return null;
  }

  private String firstLabelText(List<Label> labels) {
    return labels != null && !labels.isEmpty() ? labels.get(0).getText() : null;
  }
}
