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
 */
public final class MarkdownRenderer {

  private static final Parser PARSER = Parser.builder()
      .extensions(List.of(TablesExtension.create()))
      .build();

  private static final HtmlRenderer RENDERER = HtmlRenderer.builder()
      .extensions(List.of(TablesExtension.create()))
      .build();

  private MarkdownRenderer() {}

  /**
   * Renders {@code markdown} to a complete HTML document with embedded CSS
   * that matches the a12-studio dark theme.
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
        <style>
          body {
            font-family: -apple-system, 'Segoe UI', Roboto, sans-serif;
            font-size: 13px;
            line-height: 1.6;
            color: #c9d1d9;
            background: transparent;
            margin: 0;
            padding: 4px 2px;
          }
          h1, h2, h3, h4 {
            color: #e6edf3;
            font-weight: 600;
            margin-top: 16px;
            margin-bottom: 6px;
            border-bottom: 1px solid #30363d;
            padding-bottom: 4px;
          }
          h1 { font-size: 1.4em; }
          h2 { font-size: 1.2em; }
          h3 { font-size: 1.0em; }
          a { color: #58a6ff; }
          code {
            background: #161b22;
            border: 1px solid #30363d;
            border-radius: 4px;
            padding: 1px 5px;
            font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
            font-size: 0.9em;
          }
          pre {
            background: #161b22;
            border: 1px solid #30363d;
            border-radius: 6px;
            padding: 12px;
            overflow-x: auto;
          }
          pre code {
            background: none;
            border: none;
            padding: 0;
          }
          blockquote {
            border-left: 3px solid #30363d;
            color: #8b949e;
            margin-left: 0;
            padding-left: 12px;
          }
          img {
            max-width: 100%;
            border-radius: 6px;
            margin: 8px 0;
          }
          table {
            border-collapse: collapse;
            width: 100%;
            margin: 8px 0;
          }
          th, td {
            border: 1px solid #30363d;
            padding: 6px 12px;
          }
          th { background: #21262d; color: #e6edf3; }
          tr:nth-child(even) { background: #161b22; }
          ul, ol { padding-left: 20px; }
          li { margin: 3px 0; }
          strong { color: #e6edf3; }
          em { color: #adbac7; }
        </style>
        </head>
        <body>
        """ + body + """
        </body>
        </html>
        """;
  }
}
