package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.querymodel.ql.QueryLanguageTree.Node;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * One finding of {@link QueryFilterTypeChecker}, numbered like SME's {@code moduleSupport/qmm/resources/
 * diagnostics.json} (2000-2038). {@code args} are the already-formatted message parameters, in the order of the
 * {@code validation.queryFilterType.<code>} message; {@code start}/{@code stop} delimit the offending source range
 * (code-point indexes, {@code stop} inclusive).
 */
public record QlDiagnostic(int code, List<String> args, int start, int stop) {

  static QlDiagnostic of(int code, Node node, String... args) {
    return new QlDiagnostic(code, List.of(args), node.start(), node.stop());
  }

  public String message() {
    return ValidationMessages.get(key(code), args.toArray());
  }

  /** The message in the language of {@code bundle} (the validation messages bundle of some locale). */
  public String message(ResourceBundle bundle) {
    return new MessageFormat(bundle.getString(key(code))).format(args.toArray());
  }

  private static String key(int code) {
    return "validation.queryFilterType." + code;
  }
}
