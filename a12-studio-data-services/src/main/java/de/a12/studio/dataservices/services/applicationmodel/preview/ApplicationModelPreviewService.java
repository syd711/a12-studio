package de.a12.studio.dataservices.services.applicationmodel.preview;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.ModelDescriptor;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.applicationmodel.RegionClearDirective;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.applicationmodel.ViewAddDirective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts an {@link ApplicationModel} into wireframe-preview DTOs: the module/menu navigation and, given a
 * selected module/scene, the region/layout tree with boxes labeled by the resolved {@code VIEW_ADD} directive.
 * Pure data transformation over {@code a12-studio-models} types - no JavaFX, no HTTP.
 */
public class ApplicationModelPreviewService {

  public PreviewApplicationDto buildPreview(ApplicationModel model) {
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
   * this pass, {@code VIEW_ADD} appends a "{name}: {model name}" box label to its target region(s). A
   * directive with no explicit region targets {@link ApplicationModelContent#getDefaultRegion()}. Directives
   * that are neither (i.e. an unrecognized/future type, deserialized as {@code GenericDirective}) are skipped.
   *
   * @throws IllegalArgumentException if the module or scene name doesn't exist in the model
   */
  public PreviewSceneDto resolveScene(ApplicationModel model, String moduleName, String sceneName) {
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
    Map<String, List<String>> viewsByRegion = new HashMap<>();

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
        String label = formatViewLabel(viewAdd);
        for (String regionName : targetRegions) {
          viewsByRegion.computeIfAbsent(regionName, key -> new ArrayList<>()).add(label);
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

  private PreviewRegionDto toRegionDto(Region region, Map<String, String> layoutOverrides, Map<String, List<String>> viewsByRegion) {
    String layout = layoutOverrides.containsKey(region.getName())
        ? layoutOverrides.get(region.getName())
        : (region.getLayout() != null ? region.getLayout().getName() : null);
    List<String> views = viewsByRegion.getOrDefault(region.getName(), List.of());
    List<PreviewRegionDto> subRegions = region.getSubRegions().stream()
        .map(subRegion -> toRegionDto(subRegion, layoutOverrides, viewsByRegion))
        .toList();
    return new PreviewRegionDto(region.getName(), layout, views, subRegions);
  }

  private String formatViewLabel(ViewAddDirective viewAdd) {
    List<ModelDescriptor> models = viewAdd.getModels();
    if (models.isEmpty()) {
      return viewAdd.getName();
    }
    return viewAdd.getName() + ": " + models.get(0).getName();
  }
}
