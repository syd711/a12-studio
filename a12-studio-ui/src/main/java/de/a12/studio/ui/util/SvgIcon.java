package de.a12.studio.ui.util;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal loader for the single-path icon SVGs under de/a12/studio/ui/icons. Renders them as
 * scaled SVGPath nodes rather than pulling in a full SVG rendering library, since JavaFX has no
 * native SVG support.
 */
@Slf4j
public class SvgIcon {

  private static final String ICONS_PATH = "/de/a12/studio/ui/icons/";

  private static final Pattern VIEW_BOX = Pattern.compile("viewBox=\"([-\\d.\\s]+)\"");
  private static final Pattern STYLE_BLOCK = Pattern.compile("<style[^>]*>([\\s\\S]*?)</style>");
  private static final Pattern STYLE_RULE = Pattern.compile("\\.([\\w-]+)\\s*\\{([^}]*)}");
  private static final Pattern FILL = Pattern.compile("fill:\\s*([^;\\s]+)");
  private static final Pattern PATH_ELEMENT = Pattern.compile("<path\\b[^>]*/?>");
  private static final Pattern PATH_CLASS = Pattern.compile("class=\"([\\w-]+)\"");
  private static final Pattern PATH_D = Pattern.compile("d=\"([^\"]+)\"");
  private static final Pattern PATH_FILL = Pattern.compile("fill=\"([^\"]+)\"");

  private SvgIcon() {
  }

  public static Node load(@NonNull String resourceName, double size) {
    String svg = readResource(resourceName);
    if (svg == null) {
      return new Label();
    }

    double viewBoxSize = parseViewBoxSize(svg);
    double scale = viewBoxSize > 0 ? size / viewBoxSize : 1;
    Map<String, String> classFills = parseClassFills(svg);

    Pane pane = new Pane();
    pane.setPrefSize(size, size);
    pane.setMinSize(size, size);
    pane.setMaxSize(size, size);

    Matcher pathMatcher = PATH_ELEMENT.matcher(svg);
    while (pathMatcher.find()) {
      String pathElement = pathMatcher.group();
      Matcher dMatcher = PATH_D.matcher(pathElement);
      if (!dMatcher.find()) {
        continue;
      }

      String fill = resolveFill(pathElement, classFills);
      if ("none".equalsIgnoreCase(fill)) {
        continue;
      }

      SVGPath svgPath = new SVGPath();
      svgPath.setContent(dMatcher.group(1));
      svgPath.setFill(fill != null ? Color.web(fill) : Color.BLACK);
      svgPath.getTransforms().add(new Scale(scale, scale, 0, 0));
      pane.getChildren().add(svgPath);
    }

    return pane;
  }

  private static String resolveFill(@NonNull String pathElement, @NonNull Map<String, String> classFills) {
    Matcher inlineFill = PATH_FILL.matcher(pathElement);
    if (inlineFill.find()) {
      return inlineFill.group(1);
    }

    Matcher classMatcher = PATH_CLASS.matcher(pathElement);
    if (classMatcher.find()) {
      return classFills.get(classMatcher.group(1));
    }

    return null;
  }

  private static double parseViewBoxSize(@NonNull String svg) {
    Matcher matcher = VIEW_BOX.matcher(svg);
    if (!matcher.find()) {
      return 0;
    }
    String[] parts = matcher.group(1).trim().split("\\s+");
    return parts.length == 4 ? Double.parseDouble(parts[2]) : 0;
  }

  private static Map<String, String> parseClassFills(@NonNull String svg) {
    Map<String, String> fills = new HashMap<>();
    Matcher styleBlock = STYLE_BLOCK.matcher(svg);
    if (!styleBlock.find()) {
      return fills;
    }

    Matcher rule = STYLE_RULE.matcher(styleBlock.group(1));
    while (rule.find()) {
      Matcher fill = FILL.matcher(rule.group(2));
      if (fill.find()) {
        fills.put(rule.group(1), fill.group(1));
      }
    }
    return fills;
  }

  private static String readResource(@NonNull String resourceName) {
    try (InputStream stream = SvgIcon.class.getResourceAsStream(ICONS_PATH + resourceName)) {
      if (stream == null) {
        log.error("SVG icon not found: {}", resourceName);
        return null;
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      log.error("Error loading SVG icon {}: {}", resourceName, e.getMessage(), e);
      return null;
    }
  }
}
