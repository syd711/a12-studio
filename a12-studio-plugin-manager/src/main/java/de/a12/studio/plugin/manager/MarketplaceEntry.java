package de.a12.studio.plugin.manager;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single entry in the marketplace – the full plugin descriptor plus the
 * resolved JAR download URL computed at build time by the {@code generateMarketplace}
 * Gradle task.
 */
public class MarketplaceEntry extends PluginDescriptor {

  /** Canonical URL of the {@code plugin.json} in the GitHub repository. */
  @JsonProperty("pluginJsonUrl")
  private String pluginJsonUrl;

  public String getPluginJsonUrl() {
    return pluginJsonUrl;
  }

  public void setPluginJsonUrl(String pluginJsonUrl) {
    this.pluginJsonUrl = pluginJsonUrl;
  }
}