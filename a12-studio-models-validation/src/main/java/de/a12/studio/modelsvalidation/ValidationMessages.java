package de.a12.studio.modelsvalidation;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Central access point for the validators' resource bundle ({@code validation-messages.properties}), mirroring
 * {@code de.a12.studio.ui.util.StudioBundle} but kept in this module so validators stay independent of the UI
 * module (this module has no JavaFX/UI dependency and may run outside the desktop app, e.g. on the server).
 *
 * <p>The bundle is loaded once on first access using {@link Locale#getDefault()}; like {@code StudioBundle}, a
 * language switch takes effect on the next application start rather than retroactively.
 */
@Slf4j
public final class ValidationMessages {

  private static final String BUNDLE_BASE = "validation-messages";

  private static final ResourceBundle bundle = load();

  private ValidationMessages() {
  }

  /**
   * Looks up {@code key} in the bundle.
   * Falls back to the key itself if missing, so validation never crashes on a missing translation.
   */
  public static String get(String key) {
    try {
      return bundle.getString(key);
    }
    catch (MissingResourceException e) {
      log.warn("Missing validation message key: '{}'", key);
      return key;
    }
  }

  /**
   * Looks up {@code key} and formats it with {@link MessageFormat} using the supplied arguments.
   * Use {@code {0}}, {@code {1}} … placeholders in the property value.
   */
  public static String get(String key, Object... args) {
    String pattern = get(key);
    try {
      return MessageFormat.format(pattern, args);
    }
    catch (Exception e) {
      log.warn("Failed to format validation message key '{}' with args: {}", key, e.getMessage());
      return pattern;
    }
  }

  private static ResourceBundle load() {
    try {
      return ResourceBundle.getBundle(BUNDLE_BASE, Locale.getDefault(), ValidationMessages.class.getClassLoader());
    }
    catch (MissingResourceException e) {
      log.warn("Could not load resource bundle '{}' for locale '{}', falling back to root", BUNDLE_BASE, Locale.getDefault());
      return ResourceBundle.getBundle(BUNDLE_BASE, Locale.ROOT, ValidationMessages.class.getClassLoader());
    }
  }
}
