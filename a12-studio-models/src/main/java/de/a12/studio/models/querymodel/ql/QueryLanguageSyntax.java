package de.a12.studio.models.querymodel.ql;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/** The one place a Query Language expression is lexed/parsed, shared by {@link QueryLanguageEmitter} (compile to
 * an operator tree) and {@link QueryLanguageReferences} (collect what the expression refers to). */
final class QueryLanguageSyntax {

  private QueryLanguageSyntax() {
  }

  /** @throws QueryLanguageException if {@code source} is not syntactically valid Query Language */
  static QLParser.ProgramContext parse(String source) {
    QLLexer lexer = new QLLexer(CharStreams.fromString(source));
    QLParser parser = new QLParser(new CommonTokenStream(lexer));

    List<String> syntaxErrors = new ArrayList<>();
    parser.removeErrorListeners();
    parser.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        syntaxErrors.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
    });

    QLParser.ProgramContext program = parser.program();
    if (!syntaxErrors.isEmpty()) {
      throw new QueryLanguageException("Invalid query language expression: " + String.join("; ", syntaxErrors));
    }
    return program;
  }
}
