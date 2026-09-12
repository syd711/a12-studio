package de.a12.studio.ui.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Live "Name Convention" check shared by every dialog/field that lets the user type a model name or an
 * element name (a document/type-definition model's field, group, include, rule, computation, or type):
 * per the SME reference documentation, only letters, digits, hyphens, underscores and periods are
 * allowed, the name must not start with "xml" (in any casing) and must be at most 100 characters long.
 * Mirrors the pattern enforced at save time by {@code NameConventionValidator} against a model's {@code
 * header.id}, so the same violation surfaces immediately instead of only after the fact.
 */
public final class NameConventionValidation {

  public static final Pattern NAME_PATTERN = Pattern.compile("^[_a-zA-Z][-_.a-zA-Z0-9]{0,99}$");

  private NameConventionValidation() {
  }

  /**
   * Returns the error message to show for {@code name}, using {@code label} (e.g. "Model name", "Field
   * name") to name the offending field per the "validator messages must name the field" convention;
   * empty if {@code name} satisfies every rule.
   */
  public static Optional<String> validate(@NonNull String label, @Nullable String name) {
    if (name == null || name.isBlank()) {
      return Optional.of(StudioBundle.get("validation.name_convention.empty", label));
    }
    if (!NAME_PATTERN.matcher(name).matches()) {
      return Optional.of(StudioBundle.get("validation.name_convention.invalid_characters", label));
    }
    if (name.toLowerCase(Locale.ROOT).startsWith("xml")) {
      return Optional.of(StudioBundle.get("validation.name_convention.xml_prefix", label));
    }
    return Optional.empty();
  }

  /**
   * Plain boolean form of {@link #validate}, for call sites that only need to gate an OK button and
   * surface the message some other way (or not at all).
   */
  public static boolean isValid(@Nullable String name) {
    return name != null && NAME_PATTERN.matcher(name).matches() && !name.toLowerCase(Locale.ROOT).startsWith("xml");
  }
}
