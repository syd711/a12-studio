package de.a12.studio.plugin.manager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Root wrapper for {@code marketplace.json}, which aggregates every plugin's
 * {@link PluginDescriptor} together with its resolved download URL.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Marketplace {

  @JsonProperty("plugins")
  private List<MarketplaceEntry> plugins = new ArrayList<>();
}
