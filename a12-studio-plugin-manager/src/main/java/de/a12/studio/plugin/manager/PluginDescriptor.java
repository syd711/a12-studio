package de.a12.studio.plugin.manager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Java representation of a {@code plugin.json} file bundled inside a plugin JAR.
 *
 * <p>Example:
 * <pre>
 * {
 *   "name": "Create from Excel",
 *   "description": "Imports a Document Model from an Excel spreadsheet.",
 *   "pluginVersion": "1.0.0",
 *   "a12Version": "2606",
 *   "icon": "&lt;base64-encoded PNG&gt;",
 *   "downloadUrl": "https://example.com/plugins/create-from-excel.jar",
 *   "extensionPoints": [
 *     { "name": "createMenu", "class": "de.a12.studio.plugin.excel.ExcelCreateMenuEntry" }
 *   ]
 * }
 * </pre>
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginDescriptor {

  /** Human-readable plugin name. */
  @JsonProperty("name")
  private String name;

  /** Short description shown in the plugin manager UI. */
  @JsonProperty("description")
  private String description;

  /** Plugin version string (e.g. {@code "1.0.0"}). */
  @JsonProperty("pluginVersion")
  private String pluginVersion;

  /** Minimum compatible a12-studio version (e.g. {@code "2606"}). */
  @JsonProperty("a12Version")
  private String a12Version;

  /** Base64-encoded PNG icon (may be {@code null} if no icon is provided). */
  @JsonProperty("icon")
  @Nullable
  private String icon;

  /** URL from which this plugin JAR can be downloaded or updated. */
  @JsonProperty("downloadUrl")
  @Nullable
  private String downloadUrl;

  /** Extension points contributed by this plugin. */
  @JsonProperty("extensionPoints")
  private List<ExtensionPoint> extensionPoints = new ArrayList<>();

  // ---------------------------------------------------------------------------
  // Nested types
  // ---------------------------------------------------------------------------

  /**
   * A single extension-point registration inside {@code plugin.json}.
   */
  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ExtensionPoint {

    /**
     * Well-known extension point name.
     * Currently supported values:
     * <ul>
     *   <li>{@code "createMenu"} – contributes a {@link ICreateItemMenuEntry}</li>
     * </ul>
     */
    @JsonProperty("name")
    private String name;

    /** Fully-qualified class name that implements the extension point interface. */
    @JsonProperty("class")
    private String className;
  }
}
