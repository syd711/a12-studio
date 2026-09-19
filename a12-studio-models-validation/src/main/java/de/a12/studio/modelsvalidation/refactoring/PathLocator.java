package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.documentmodel.rulelang.RuleLangBaseListener;
import de.a12.studio.models.documentmodel.rulelang.RuleLangLexer;
import de.a12.studio.models.documentmodel.rulelang.RuleLangParser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Finds the element paths inside free text so {@link DocumentModelRefactoring} can rewrite exactly those
 * substrings and leave everything else - string literals, function names, comments, spacing - byte-for-byte alone.
 */
final class PathLocator {

  /** A path inside a text, as {@link String} indices (start inclusive, end exclusive). */
  record Region(int start, int end) {
  }

  private PathLocator() {
  }

  /**
   * The paths of a Rule/Computation condition, located through the RuleLang parse tree so a path is never confused
   * with a string literal, a call name or a {@code ;;} comment. Deliberately excludes what surrounds a path - the
   * {@code $} iteration prefix and any {@code ->category}/{@code For ...} accessor - since those don't move with it.
   * Empty (rather than an empty list) when the text doesn't parse: a half-edited condition can't be rewritten
   * reliably, and the caller reports that instead of guessing.
   */
  static Optional<List<Region>> inCondition(String condition) {
    if (condition == null || condition.isBlank()) {
      return Optional.of(List.of());
    }
    boolean[] failed = {false};
    BaseErrorListener errorListener = new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
          String msg, RecognitionException e) {
        failed[0] = true;
      }
    };
    RuleLangLexer lexer = new RuleLangLexer(CharStreams.fromString(condition));
    lexer.removeErrorListeners();
    lexer.addErrorListener(errorListener);
    RuleLangParser parser = new RuleLangParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    RuleLangParser.ProgramContext tree = parser.program();
    if (failed[0]) {
      return Optional.empty();
    }

    List<Region> regions = new ArrayList<>();
    ParseTreeWalker.DEFAULT.walk(new RuleLangBaseListener() {
      @Override
      public void enterPath(RuleLangParser.PathContext ctx) {
        RuleLangParser.SegmentContext first = ctx.segment(0);
        RuleLangParser.SegmentContext last = ctx.segment(ctx.segment().size() - 1);
        int start = first.getStart().getStartIndex();
        // A leading "/" (absolute path) belongs to the path; it's the only "/" that can precede the first segment.
        if (!ctx.S_SLASH().isEmpty()
            && ctx.S_SLASH(0).getSymbol().getTokenIndex() < first.getStart().getTokenIndex()) {
          start = ctx.S_SLASH(0).getSymbol().getStartIndex();
        }
        int end = last.getStop().getStopIndex() + 1;
        // ANTLR's CharStream indexes by code point, String by UTF-16 unit - only differs after a supplementary
        // character (e.g. an emoji in an earlier string literal), but then every later path would be off.
        regions.add(new Region(condition.offsetByCodePoints(0, start), condition.offsetByCodePoints(0, end)));
      }
    }, tree);
    return Optional.of(regions);
  }

  /**
   * The paths in the {@code $...$} parameters of a Rule/Computation error message: {@code $Field$},
   * {@code $Group/Field.value$}, {@code $Field->Category$}, {@code $#Group$} (a repetition counter) and
   * {@code $index(Field).value$}. {@code $$} is an escaped literal dollar sign and opens no parameter.
   */
  static List<Region> inMessage(String message) {
    List<Region> regions = new ArrayList<>();
    if (message == null) {
      return regions;
    }
    int i = 0;
    while (i < message.length()) {
      if (message.charAt(i) != '$') {
        i++;
        continue;
      }
      if (i + 1 < message.length() && message.charAt(i + 1) == '$') {
        i += 2;
        continue;
      }
      int close = message.indexOf('$', i + 1);
      if (close < 0) {
        break;
      }
      locateInParameter(message, i + 1, close).ifPresent(regions::add);
      i = close + 1;
    }
    return regions;
  }

  /** The path part of the parameter body {@code message[from, to)}, without its {@code #}/{@code index(}/{@code .value}/{@code ->} decoration. */
  private static Optional<Region> locateInParameter(String message, int from, int to) {
    int start = from;
    int end = to;
    if (start < end && message.charAt(start) == '#') {
      start++;
    }
    String body = message.substring(start, end);
    if (body.startsWith("index(")) {
      int closeParen = body.indexOf(')');
      if (closeParen < 0) {
        return Optional.empty();
      }
      start += "index(".length();
      end = start + (closeParen - "index(".length());
    }
    else {
      int arrow = body.indexOf("->");
      if (arrow >= 0) {
        end = start + arrow;
      }
      else if (body.endsWith(".value")) {
        end = end - ".value".length();
      }
    }
    return start < end ? Optional.of(new Region(start, end)) : Optional.empty();
  }
}
