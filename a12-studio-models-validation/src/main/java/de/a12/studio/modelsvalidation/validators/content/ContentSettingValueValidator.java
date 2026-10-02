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
 * ones, per {@link ContentPropertyFormatRules}, and the color/shadow settings, ported from the editor's
 * controllers. SME itself only reports a shadow's unreadable offset/blur/spread numbers; a malformed color or
 * shadow shape makes its converter throw, so those are reported here too.
 * <p>
 * A settings row is checked whenever its path is present in the element's props, regardless of whether the row
 * editing it would currently be shown/enabled ({@code showWhen}/{@code enabledWhen} in the FXML) - SME's own
 * controller validates whatever is stored, not just what is currently visible.
 */
public final class ContentSettingValueValidator implements ModelValidator {

  /** What JS {@code parseFloat} accepts as a prefix (anything else is NaN, i.e. "Invalid number input"). */
  private static final Pattern PARSE_FLOAT = Pattern.compile("^[+-]?(?:\\d+\\.?\\d*|\\.\\d+|Infinity)");
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
      case COLOR -> raw instanceof String text && CssColor.isValid(text);
      case SHADOW -> raw instanceof String text && isValidShadow(text);
    };
  }

  /** Mirrors {@code createShadowController().converter}: 5 tokens, or 6 with a leading style word. */
  private static boolean isValidShadow(String text) {
    String[] elements = text.replaceAll("\\s+", " ").replaceAll(",\\s+", ",").split(" ", -1);
    if (elements.length != 5 && elements.length != 6) {
      return false;
    }
    int first = elements.length == 6 ? 1 : 0;
    for (int i = first; i < first + 4; i++) {
      if (!PARSE_FLOAT.matcher(elements[i]).find()) {
        return false;
      }
    }
    return CssColor.isValid(elements[first + 4]);
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
