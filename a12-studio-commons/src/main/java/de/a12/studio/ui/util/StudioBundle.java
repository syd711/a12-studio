package de.a12.studio.ui.util;

import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Central access point for the UI resource bundle ({@code messages.properties}).
 *
 * <p>The bundle is loaded once on first access using {@link Locale#getDefault()}.
 * Call {@link #reload()} after changing the default locale to pick up a different
 * language (takes effect for all subsequently loaded FXMLs and explicit {@link #get}
 * calls; already-constructed nodes are not retroactively updated — a restart is
 * required for a full language switch).
 *
 * <p>Usage:
 * <pre>
 *   // Simple lookup
 *   String label = StudioBundle.get("delete");
 *
 *   // Parameterised (MessageFormat patterns)
 *   String msg = StudioBundle.get("confirm_delete_role", roleName);
 * </pre>
 */
@Slf4j
public final class StudioBundle {

  private static final String BUNDLE_BASE = "messages";

  private static ResourceBundle bundle = load();

  private StudioBundle() {}

  /** Returns the current bundle (useful for passing to {@link javafx.fxml.FXMLLoader#setResources}). */
  public static ResourceBundle getBundle() {
    return bundle;
  }

  /**
   * Looks up {@code key} in the bundle.
   * Falls back to the key itself if missing, so the UI never crashes on a missing translation.
   */
  public static String get(String key) {
    try {
      return bundle.getString(key);
    }
    catch (MissingResourceException e) {
      log.warn("Missing bundle key: '{}'", key);
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
      log.warn("Failed to format bundle key '{}' with args: {}", key, e.getMessage());
      return pattern;
    }
  }

  /** Reloads the bundle from the current default locale. */
  public static void reload() {
    ResourceBundle.clearCache();
    bundle = load();
  }

  private static ResourceBundle load() {
    try {
      return ResourceBundle.getBundle(BUNDLE_BASE, Locale.getDefault(),
          StudioBundle.class.getClassLoader());
    }
    catch (MissingResourceException e) {
      log.warn("Could not load resource bundle '{}' for locale '{}', falling back to root",
          BUNDLE_BASE, Locale.getDefault());
      return ResourceBundle.getBundle(BUNDLE_BASE, Locale.ROOT,
          StudioBundle.class.getClassLoader());
    }
  }

  /**
   * Wraps {@code primary} so that lookups missing from it fall through to the main studio
   * bundle ({@link #getBundle()}). Intended for plugins that load their own message bundle
   * via {@link javafx.fxml.FXMLLoader#setResources} but whose FXML includes shared components
   * (e.g. {@code scene-dialog-header.fxml}) that reference core keys like {@code a12_studio} or
   * {@code close}, which don't exist in the plugin's own bundle.
   */
  public static ResourceBundle withFallback(ResourceBundle primary) {
    return new ResourceBundle() {
      @Override
      protected Object handleGetObject(String key) {
        if (primary.containsKey(key)) {
          return primary.getObject(key);
        }
        try {
          return bundle.getObject(key);
        }
        catch (MissingResourceException e) {
          return null;
        }
      }

      @Override
      public Enumeration<String> getKeys() {
        Set<String> keys = new HashSet<>(Collections.list(primary.getKeys()));
        keys.addAll(Collections.list(bundle.getKeys()));
        return Collections.enumeration(keys);
      }
    };
  }
}
