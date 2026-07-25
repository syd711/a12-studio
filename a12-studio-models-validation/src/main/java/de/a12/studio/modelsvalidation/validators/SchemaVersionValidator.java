package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port of the kernel's DocumentSchemaVersionRule / DocumentSchemaVersionPatternRule (decompiled from
 * kernel-md-model, EUPL-1.2 dual-licensed). Model-sourced (elementId == null): {@code ValidatorRunner} drops
 * these, mirroring how the kernel's getElementProblems only ever surfaced element-sourced problems to the UI.
 * Kept for completeness.
 */
public final class SchemaVersionValidator implements ModelValidator {

  private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+\\.\\d+)");
  private static final String COMPATIBLE_SCHEMA_VERSION = "28.4.0";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    String version = documentModel.getModelVersion();
    if (version == null || !VERSION_PATTERN.matcher(version).matches()) {
      errors.add(error(model,
          "Document model version " + version + " does not match proper version schema. Expected version is " + COMPATIBLE_SCHEMA_VERSION
              + "."));
      return errors;
    }
    Matcher actual = VERSION_PATTERN.matcher(version);
    Matcher compatible = VERSION_PATTERN.matcher(COMPATIBLE_SCHEMA_VERSION);
    actual.matches();
    compatible.matches();
    boolean compatibleVersion = Integer.parseInt(actual.group(1)) == Integer.parseInt(compatible.group(1))
        && Double.parseDouble(compatible.group(2)) >= Double.parseDouble(actual.group(2));
    if (!compatibleVersion) {
      errors.add(error(model, "Version mismatch: Document model version is " + version + " and application version is "
          + COMPATIBLE_SCHEMA_VERSION + "."));
    }
    return errors;
  }

  private static ModelValidationError error(A12Model<?> model, String message) {
    return new ModelValidationError(model, null, message, Severity.ERROR.name());
  }
}
