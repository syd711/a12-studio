package de.a12.studio.ui.util;

import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loads Google's "Material Icons" font, bundled under {@code /de/a12/studio/ui/fonts} (from
 * https://github.com/google/material-design-icons, Apache 2.0), together with its ligature-name-to-codepoint
 * mapping. This is the same name list shown at https://fonts.google.com/icons, letting property editors that
 * store an icon by name (e.g. {@link de.a12.studio.ui.editors.propertyeditors.IconPanelController}) offer a
 * searchable suggestion list with a live glyph preview instead of a free-text field.
 */
@Slf4j
public final class MaterialIcons {

  private static final String FONT_RESOURCE = "/de/a12/studio/ui/fonts/MaterialIcons-Regular.ttf";
  private static final String CODEPOINTS_RESOURCE = "/de/a12/studio/ui/fonts/MaterialIcons-Regular.codepoints";

  private static final String FONT_FAMILY = loadFontFamily();
  private static final Map<String, Character> GLYPHS_BY_NAME = loadGlyphs();

  private MaterialIcons() {
  }

  /**
   * Every icon name known to the bundled font, alphabetically sorted (matching the ligature names used at
   * fonts.google.com/icons, e.g. {@code "home"}, {@code "arrow_back"}).
   */
  public static List<String> iconNames() {
    return List.copyOf(GLYPHS_BY_NAME.keySet());
  }

  /**
   * The glyph character to render (in a {@link #font} instance) to preview the given icon name, or {@code
   * null} if it isn't one of {@link #iconNames()}.
   */
  public static Character glyph(String iconName) {
    return iconName == null ? null : GLYPHS_BY_NAME.get(iconName);
  }

  public static Font font(double size) {
    return Font.font(FONT_FAMILY, size);
  }

  private static String loadFontFamily() {
    try (InputStream in = MaterialIcons.class.getResourceAsStream(FONT_RESOURCE)) {
      Font font = in == null ? null : Font.loadFont(in, 16);
      if (font == null) {
        throw new IOException("JavaFX could not load \"" + FONT_RESOURCE + "\"");
      }
      return font.getFamily();
    }
    catch (IOException e) {
      log.error("Failed to load bundled Material Icons font: {}", e.getMessage(), e);
      return Font.getDefault().getFamily();
    }
  }

  private static Map<String, Character> loadGlyphs() {
    Map<String, Character> glyphs = new TreeMap<>();
    try (InputStream in = MaterialIcons.class.getResourceAsStream(CODEPOINTS_RESOURCE)) {
      if (in == null) {
        throw new IOException("Resource \"" + CODEPOINTS_RESOURCE + "\" not found");
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 2) {
          glyphs.put(parts[0], (char) Integer.parseInt(parts[1], 16));
        }
      }
    }
    catch (IOException e) {
      log.error("Failed to load bundled Material Icons codepoints: {}", e.getMessage(), e);
    }
    return glyphs;
  }
}
