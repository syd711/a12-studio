package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.auth.AuthFileType;
import de.a12.studio.models.auth.Role;
import de.a12.studio.models.auth.RolesDocument;
import de.a12.studio.models.util.YamlSettings;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The rules the kernel's meta-models put on a model's {@code roles} header annotation (a comma-separated
 * list, e.g. {@code "admin,guest"}), ported from the {@code header/annotations} rules of the
 * {@code DomainTypesettingMetaModel} in the {@code print-typesetting} library (SME's other meta-models carry the
 * same set):
 * <ul>
 *   <li>{@code mustHaveValidRoleValues} (error): a role only consists of letters, digits, hyphens,
 *       underscores and periods and starts with a letter or underscore. SME's own regex has a character range
 *       ({@code ,-_}) that is looser than its message, this checks what the message says.</li>
 *   <li>{@code rolesNotUnique} (error): no role twice.</li>
 *   <li>{@code shouldNotHaveEmptyRoles} (error): no blank role.</li>
 *   <li>{@code roleIsNotPartOfRolesModel} (warning): every role is in the workspace's roles file.</li>
 *   <li>{@code mustHaveRolesIfRolesModelPresent} (warning): a workspace with a roles file wants roles on every
 *       model.</li>
 *   <li>{@code shouldHaveRolesModelIfSpecifyingRoles} (warning): roles need a roles file to be checked against.</li>
 * </ul>
 * The workspace checks are skipped when the model belongs to no (opened) project. Every problem is reported against the
 * one {@link #ELEMENT_ID}, so a model settings dialog/badge can list them all.
 */
public final class HeaderRolesValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/annotations/roles";

  private static final String ROLES_ANNOTATION_NAME = "roles";

  private static final String AUTH_FOLDER_NAME = "auth";

  private static final Pattern ROLE_NAME = Pattern.compile("^[_a-zA-Z][-_.a-zA-Z0-9]*$");

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    File projectFolder = context.project() == null ? null : context.project().getFolder();
    File rolesFile = projectFolder == null ? null : new File(new File(projectFolder, AUTH_FOLDER_NAME), AuthFileType.ROLES.getFileName());
    boolean checkWorkspace = rolesFile != null;
    boolean workspaceHasRolesFile = checkWorkspace && rolesFile.isFile();

    Annotation annotation = findRolesAnnotation(model);
    if (annotation == null) {
      return workspaceHasRolesFile ? List.of(error(model, Severity.WARNING, "validation.roles.requiredByRolesFile")) : List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    String value = annotation.getValue();
    if (value == null || value.isBlank()) {
      errors.add(error(model, Severity.ERROR, "validation.roles.empty"));
      return errors;
    }

    Set<String> declaredRoles = workspaceHasRolesFile ? loadRoleNames(rolesFile) : null;
    Set<String> seen = new HashSet<>();
    Set<String> reportedDuplicates = new HashSet<>();
    boolean reportedEmpty = false;
    for (String segment : value.split(",", -1)) {
      String role = segment.trim();
      if (role.isEmpty()) {
        if (!reportedEmpty) {
          errors.add(error(model, Severity.ERROR, "validation.roles.empty"));
          reportedEmpty = true;
        }
        continue;
      }
      if (!ROLE_NAME.matcher(role).matches()) {
        errors.add(error(model, Severity.ERROR, "validation.roles.invalid", role));
      }
      if (!seen.add(role) && reportedDuplicates.add(role)) {
        errors.add(error(model, Severity.ERROR, "validation.roles.duplicate", role));
      }
      if (declaredRoles != null && !declaredRoles.contains(role)) {
        errors.add(error(model, Severity.WARNING, "validation.roles.notInRolesFile", role));
      }
    }
    if (checkWorkspace && !workspaceHasRolesFile) {
      errors.add(error(model, Severity.WARNING, "validation.roles.missingRolesFile"));
    }
    return errors;
  }

  private static ModelValidationError error(A12Model<?> model, Severity severity, String messageKey, Object... args) {
    return new ModelValidationError(model, ELEMENT_ID, ValidationMessages.get(messageKey, args), severity.name());
  }

  private static Annotation findRolesAnnotation(A12Model<?> model) {
    for (Annotation annotation : model.getAnnotations()) {
      if (ROLES_ANNOTATION_NAME.equals(annotation.getName())) {
        return annotation;
      }
    }
    return null;
  }

  /** The role names declared in {@code rolesFile}; {@code null} (no check possible) if it cannot be read. */
  private static Set<String> loadRoleNames(File rolesFile) {
    try {
      RolesDocument document = YamlSettings.objectMapper.readValue(rolesFile, RolesDocument.class);
      Set<String> names = new HashSet<>();
      for (Role role : document.getRoles()) {
        if (role.getName() != null) {
          names.add(role.getName());
        }
      }
      return names;
    }
    catch (Exception e) {
      return null;
    }
  }
}
