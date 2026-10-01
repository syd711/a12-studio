package de.a12.studio.models.contentmodel;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The keywords and units each CSS length/spacing setting of a Content Model element accepts, keyed by the
 * setting's props path. Single source for both the editor rows ({@code LengthRow}/{@code SpacingRow} in
 * {@code a12-studio-ui/.../editors/contentmodel/fields}, which look their format up by {@code path} instead of
 * carrying {@code keywords}/{@code units} attributes in the panel FXML) and the model-layer check
 * ({@code ContentPropertyFormatRules}/{@code ContentSettingValueValidator} in {@code a12-studio-models-validation}).
 * <p>
 * {@code units} uses the FXML spec form {@code "px:400,%:100"}: unit name and the number the editor fills in when
 * switching to that unit. The validator only needs the names ({@link Format#unitNames()}).
 */
public final class ContentPropertyFormats {

  /** Comma separated {@code keywords} and {@code units} spec of one setting; either may be empty. */
  public record Format(String keywords, String units) {

    public List<String> keywordList() {
      return csv(keywords);
    }

    public List<String> unitNames() {
      return csv(units).stream().map(entry -> entry.split(":")[0].trim()).toList();
    }

    private static List<String> csv(String spec) {
      return spec == null || spec.isBlank()
          ? List.of()
          : Arrays.stream(spec.split(",")).map(String::trim).filter(entry -> !entry.isEmpty()).toList();
    }
  }

  private static final Map<String, Format> FORMATS = Map.ofEntries(
      // dimensions-panel.fxml
      Map.entry("style.width", new Format("auto", "px:400,%:100")),
      Map.entry("style.height", new Format("auto,fit-content", "px:400,%:100")),
      Map.entry("style.padding", new Format("", "px:0,%:0,rem:0")),
      Map.entry("style.margin", new Format("auto", "px:0,%:0,rem:0")),

      // appearance-panel.fxml
      Map.entry("size", new Format("medium,big", "px:16")),

      // background-image-panel.fxml
      Map.entry("style.backgroundSize", new Format("auto,cover,contain", "px:512,%:100")),
      Map.entry("style.backgroundPositionX", new Format("left,center,right", "px:512,%:100")),
      Map.entry("style.backgroundPositionY", new Format("top,center,bottom", "px:512,%:100")),

      // border-panel.fxml
      Map.entry("style.borderWidth", new Format("auto", "px:1")),
      Map.entry("style.borderRadius", new Format("", "px:0,%:0,rem:0")),

      // icons-panel.fxml
      Map.entry("icons.size", new Format("medium,big", "px:16")),

      // layout-panel.fxml
      Map.entry("style.justifyContent", new Format("start,center,end,space-between,space-around,space-evenly", "")),
      Map.entry("style.gap", new Format("", "px:0,%:0")),
      Map.entry("style.overflow", new Format("visible,hidden,scroll,auto", "")));

  /** The format of the setting at {@code path}, or {@code null} if it is not a length/spacing setting. */
  public static @Nullable Format forPath(@Nullable String path) {
    return path == null ? null : FORMATS.get(path);
  }

  /** Like {@link #forPath(String)}, but fails for a path that has no format - for the validator's rule table. */
  public static Format require(String path) {
    Format format = FORMATS.get(path);
    if (format == null) {
      throw new IllegalArgumentException("No content property format for path " + path);
    }
    return format;
  }

  private ContentPropertyFormats() {
  }
}
