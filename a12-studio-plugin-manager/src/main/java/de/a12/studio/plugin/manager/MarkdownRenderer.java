package de.a12.studio.plugin.manager;

import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

/**
 * Converts Markdown text to a self-contained HTML page suitable for display
 * in a JavaFX {@code WebView}.
 *
 * <p>Uses the CommonMark reference implementation with the GFM-tables extension.
 * Styling lives in {@code markdown-renderer.css} (see {@link #getStylesheetUrl()})
 * rather than being embedded in the generated HTML; apply it to the target
 * {@code WebView} via {@code WebEngine.setUserStyleSheetLocation(...)}.
 */
public final class MarkdownRenderer {

  private static final String STYLESHEET_RESOURCE = "markdown-renderer.css";

  private static final Parser PARSER = Parser.builder()
      .extensions(List.of(TablesExtension.create()))
      .build();

  private static final HtmlRenderer RENDERER = HtmlRenderer.builder()
      .extensions(List.of(TablesExtension.create()))
      .build();

  private MarkdownRenderer() {}

  /**
   * Returns the classpath URL of the CSS stylesheet matching the a12-studio
   * dark theme, for use with {@code WebEngine.setUserStyleSheetLocation(...)}.
   */
  public static String getStylesheetUrl() {
    return MarkdownRenderer.class.getResource(STYLESHEET_RESOURCE).toExternalForm();
  }

  /**
   * Renders {@code markdown} to a complete HTML document body.
   *
   * @param markdown source Markdown text (may be {@code null} or blank)
   * @return full HTML string ready for {@code WebView.getEngine().loadContent()}
   */
  public static String toHtml(String markdown) {
    String body;
    if (markdown == null || markdown.isBlank()) {
      body = "<p><em>No description available.</em></p>";
    } else {
      Node document = PARSER.parse(markdown);
      body = RENDERER.render(document);
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8"/>
        </head>
        <body>
        """ + body + """
        </body>
        </html>
        """;
  }
}
