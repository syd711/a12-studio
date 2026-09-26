package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.contentmodel.ContentUrls;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The settings of an element that its property controller in SME rejects when they are stored ({@code Invalid setting for
 * property "..."}, the {@code errorExtractor} of the controller): the required ones (a Message Box's label, a Tooltip's
 * text, both icons of an Expandable), an Image without any source, and a URL that is not one of the known safe schemes.
 * The settings that are references (group, field, element ids) are checked by the reference validators; the numeric
 * settings (lengths, spacing, shadow) are not checked here yet.
 */
public final class ContentSettingsValidator implements ModelValidator {

  private static final Map<String, List<String>> REQUIRED = Map.of(
      "MessageBox", List.of("label"),
      "Tooltip", List.of("text"),
      "Expandable", List.of("icons.collapsedIcon", "icons.expandedIcon"));

  // The settings that hold a URL: the element type and the path of the setting.
  private static final Map<String, String> URLS = Map.of("Link", "href", "Video", "src");

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ContentModel contentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    ContentTree.forEach(contentModel, (element, ancestors) -> {
      if (element.getId() == null || element.getType() == null
          || !ContentElementLibrary.NAMESPACE.equals(ContentNodeWalker.namespace(element))) {
        return;
      }
      ContentProps props = new ContentProps(element);
      for (String path : REQUIRED.getOrDefault(element.getType(), List.of())) {
        if (isBlank(props.get(path))) {
          errors.add(ContentTree.finding(contentModel, element, Severity.ERROR, "validation.contentSetting.required", path));
        }
      }
      String urlPath = URLS.get(element.getType());
      if (urlPath != null) {
        checkUrl(contentModel, element, urlPath, props.get(urlPath), errors);
      }
      if ("Image".equals(element.getType())) {
        checkImageSource(contentModel, element, props, errors);
      }
    });
    return errors;
  }

  /** SME's image source setting: a static URL, a dynamic source (the group of an attachment field), or both; not none. */
  private static void checkImageSource(ContentModel model, ContentElement element, ContentProps props, List<ModelValidationError> errors) {
    Object source = props.get("src");
    Object staticUrl = source instanceof Map<?, ?> map ? map.get("static") : source;
    Object dynamic = source instanceof Map<?, ?> map ? map.get("dynamic") : null;
    if (isBlank(staticUrl) && isBlank(dynamic)) {
      errors.add(ContentTree.finding(model, element, Severity.ERROR, "validation.contentSetting.imageSource"));
    }
    checkUrl(model, element, "src.static", staticUrl, errors);
  }

  private static void checkUrl(ContentModel model, ContentElement element, String path, Object value, List<ModelValidationError> errors) {
    if (value instanceof String url && !url.isBlank() && !ContentUrls.isSafe(url)) {
      errors.add(ContentTree.finding(model, element, Severity.ERROR, "validation.contentSetting.unsafeUrl", path));
    }
  }

  private static boolean isBlank(Object value) {
    return value == null || (value instanceof String string && string.isBlank());
  }
}
