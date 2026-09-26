package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The hints SME's element modules give while editing (severity warning, so they never make a model invalid): a Box
 * without children can render with a wrong height, a Media Query needs a Box as the root of the model to measure, a
 * Table's screen reader column should not be an action column, a vertical Button needs both a label and an icon, and a
 * Message Group Display shows nothing outside a Message Group Container.
 */
public final class ContentWarningsValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ContentModel contentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    ContentElement root = contentModel.getContent() != null ? contentModel.getContent().getRoot() : null;
    ContentTree.forEach(contentModel, (element, ancestors) -> {
      if (element.getId() == null || element.getType() == null) {
        return;
      }
      ContentProps props = new ContentProps(element);
      boolean contentEngine = ContentElementLibrary.NAMESPACE.equals(ContentNodeWalker.namespace(element));
      boolean formEngine = ContentElementLibrary.FORM_ELEMENTS_NAMESPACE.equals(ContentNodeWalker.namespace(element));
      if (contentEngine) {
        switch (element.getType()) {
          case "Box" -> {
            if (element.getChildren() == null || element.getChildren().isEmpty()) {
              errors.add(ContentTree.finding(contentModel, element, Severity.WARNING, "validation.contentWarning.boxWithoutChildren"));
            }
          }
          case "MediaQuery" -> {
            if (root != null && !("Box".equals(root.getType()) && ContentElementLibrary.NAMESPACE.equals(ContentNodeWalker.namespace(root)))) {
              errors.add(ContentTree.finding(contentModel, element, Severity.WARNING, "validation.contentWarning.mediaQueryRoot"));
            }
          }
          case "Table" -> {
            if (isActionColumn(props)) {
              errors.add(ContentTree.finding(contentModel, element, Severity.WARNING, "validation.contentWarning.screenReaderActionColumn"));
            }
          }
          case "Button" -> {
            if (props.getBoolean("vertical", false) && (isBlank(props.get("icon")) || isBlank(props.get("label"))
                || props.getBoolean("labelHidden", false))) {
              errors.add(ContentTree.finding(contentModel, element, Severity.WARNING, "validation.contentWarning.verticalButton"));
            }
          }
          default -> {
          }
        }
      }
      if (formEngine && "MessageGroupDisplay".equals(element.getType()) && ancestors.stream().noneMatch(
          ancestor -> "MessageGroupContainer".equals(ancestor.getType()))) {
        errors.add(ContentTree.finding(contentModel, element, Severity.WARNING, "validation.contentWarning.messageGroupDisplay"));
      }
    });
    return errors;
  }

  /** The table's screen reader column ({@code screenReaderColumnRef}) is one of its columns that is an action column. */
  private static boolean isActionColumn(ContentProps props) {
    String reference = props.getString("screenReaderColumnRef");
    if (reference == null || !(props.get("columns") instanceof List<?> columns)) {
      return false;
    }
    return columns.stream().anyMatch(column -> column instanceof Map<?, ?> map && reference.equals(map.get("id"))
        && Boolean.TRUE.equals(map.get("actionColumn")));
  }

  private static boolean isBlank(Object value) {
    return value == null || (value instanceof String string && string.isBlank());
  }
}
