package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A check made on every element of a Content Model together with its data context (see {@link ContentNodeWalker}); the
 * subclasses only decide what is wrong with one element. Findings are reported against the element's id, so an editor
 * can mark the element (event nodes report against the element that holds them).
 */
abstract class AbstractContentNodeValidator implements ModelValidator {

  @Override
  public final List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ContentModel contentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    ContentNodeWalker.walk(contentModel, ContentDocument.of(contentModel, context), info -> {
      if (info.elementId() != null) {
        check(info, new Findings(info, errors));
      }
    });
    return errors;
  }

  abstract void check(ContentNodeWalker.NodeInfo info, Findings findings);

  /** Collects the findings for one element. */
  static final class Findings {

    private final ContentNodeWalker.NodeInfo info;
    private final List<ModelValidationError> errors;

    private Findings(ContentNodeWalker.NodeInfo info, List<ModelValidationError> errors) {
      this.info = info;
      this.errors = errors;
    }

    /** An error described by {@code messageKey}; its arguments are the element's label and id, then {@code arguments}. */
    void error(String messageKey, Object... arguments) {
      add(Severity.ERROR, messageKey, arguments);
    }

    void warning(String messageKey, Object... arguments) {
      add(Severity.WARNING, messageKey, arguments);
    }

    private void add(Severity severity, String messageKey, Object[] arguments) {
      Object[] all = new Object[arguments.length + 2];
      all[0] = info.label();
      all[1] = info.elementId();
      System.arraycopy(arguments, 0, all, 2, arguments.length);
      errors.add(new ModelValidationError(info.model(), info.elementId(), ValidationMessages.get(messageKey, all), severity.name()));
    }
  }
}
