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
import de.a12.studio.modelsvalidation.validators.content.ContentPropertyFormatRules.Kind;
import de.a12.studio.modelsvalidation.validators.content.ContentPropertyFormatRules.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The rest of gap 9 of "Content Model: gap review": SME's length/spacing/numeric property controllers reject a
 * value their converter cannot parse ({@code Invalid setting for property "..."}); {@link ContentSettingsValidator}
 * already covers the required/URL/image-source settings, this covers the CSS length/spacing and whole-number
 * ones, per {@link ContentPropertyFormatRules}. Color and shadow settings are deliberately not covered - see
 * that class's javadoc.
 * <p>
 * A settings row is checked whenever its path is present in the element's props, regardless of whether the row
 * editing it would currently be shown/enabled ({@code showWhen}/{@code enabledWhen} in the FXML) - SME's own
 * controller validates whatever is stored, not just what is currently visible.
 */
public final class ContentSettingValueValidator implements ModelValidator {

  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

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
      for (Rule rule : ContentPropertyFormatRules.RULES) {
        if (!rule.appliesTo(element.getType())) {
          continue;
        }
        Object raw = props.get(rule.path());
        if (raw == null || isValid(rule, raw)) {
          continue;
        }
        errors.add(ContentTree.finding(contentModel, element, Severity.ERROR, "validation.contentSetting.invalidValue",
            rule.path(), String.valueOf(raw)));
      }
    });
    return errors;
  }

  private static boolean isValid(Rule rule, Object raw) {
    return switch (rule.kind()) {
      case NUMBER -> isValidNumber(raw);
      case LENGTH -> raw instanceof String text && isValidLength(rule, text);
      case SPACING -> raw instanceof String text && isValidSpacing(rule, text);
    };
  }

  private static boolean isValidNumber(Object raw) {
    if (raw instanceof Number) {
      return true;
    }
    if (raw instanceof String text) {
      try {
        Long.parseLong(text.trim());
        return true;
      }
      catch (NumberFormatException e) {
        return false;
      }
    }
    return false;
  }

  private static boolean isValidSpacing(Rule rule, String text) {
    String trimmed = text.trim();
    if (trimmed.isEmpty()) {
      return false;
    }
    String[] tokens = WHITESPACE.split(trimmed);
    if (tokens.length < 1 || tokens.length > 4) {
      return false;
    }
    for (String token : tokens) {
      if (!isValidLengthToken(rule, token)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isValidLength(Rule rule, String text) {
    return isValidLengthToken(rule, text.trim());
  }

  /** Mirrors {@code LengthEditor#setValue}: a keyword, a number+unit, or the bare {@code 0} when units exist. */
  private static boolean isValidLengthToken(Rule rule, String token) {
    if (rule.keywords().contains(token)) {
      return true;
    }
    if (rule.units().isEmpty()) {
      return false;
    }
    if ("0".equals(token)) {
      return true;
    }
    String unitAlternatives = String.join("|", rule.units().stream().map(Pattern::quote).toList());
    return Pattern.compile("^(-?(?:\\d+\\.?\\d*|\\.\\d+))(" + unitAlternatives + ")$").matcher(token).matches();
  }
}
