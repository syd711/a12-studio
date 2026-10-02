package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.querymodel.ql.QLLexer;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Arg;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.BooleanType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FieldInfo;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FieldReferenceType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FunctionType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.NullType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.NumberType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.ObjectType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.QlType;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.StringType;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker.Models;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Context-aware completion for a Query Language filter - the Java counterpart of SME's {@code moduleSupport/qmm}
 * editor {@code proposer.ts}/{@code context-detector.ts}, minus the Monaco plumbing: what may come next at the
 * caret, from the tokens before it. Field paths inside {@code [...]} are not handled here (a12-studio's
 * {@code BracketedPathSuggestionProvider} does that).
 * <ul>
 *   <li>start of an expression ({@code and}/{@code or}, {@code (}, nothing yet): {@code Match(}, {@code Has(},
 *   {@code InRange(}, {@code !(}, a field reference;</li>
 *   <li>after a field reference: the binary operators whose signature accepts that field's type;</li>
 *   <li>after an operator: the value kinds its signature accepts for that field type ({@code Null}, {@code
 *   True}/{@code False}, a string, a number, a {@code Date(...)}/{@code Time(...)}/... constructor), and the
 *   enumeration's own values for {@code ==}/{@code !=} on an enumeration field;</li>
 *   <li>inside {@code Has(...)}: the relationship models connected to the current Document Model, then the roles
 *   reachable through the chosen one; the scope of a nested constraint is the role's (or link) Document Model;</li>
 *   <li>after a complete expression: {@code and}/{@code or}.</li>
 * </ul>
 * An {@code insertText} may contain one {@code $0}: where the caret belongs after the text is inserted.
 *
 * <p>Instances cache one {@link ElementIndex} per Document Model, so create one per editor load.
 */
public final class QueryFilterCompletion {

  /** One proposal; {@code insertText} replaces the {@link Result} range. */
  public record Proposal(String label, String insertText, String detail) {
  }

  public record Result(int replaceStart, int replaceEnd, List<Proposal> proposals) {
  }

  private static final String CARET = "$0";

  static final Map<String, String> OPERATOR_RAW = Map.of(
      "Equal", "==", "NotEqual", "!=", "SingleMatch", "~", "NotSingleMatch", "!~",
      "GreaterThanOrEqual", ">=", "LessThanOrEqual", "<=");

  private static final Set<Integer> WORD_TOKENS = Set.of(QLLexer.I_CALLEE, QLLexer.I_ARGUMENT_NAME, QLLexer.ALPHA,
      QLLexer.L_AND, QLLexer.L_OR, QLLexer.C_TRUE, QLLexer.C_FALSE, QLLexer.C_NULL);

  private static final Set<Integer> OPERATOR_TOKENS = Set.of(QLLexer.S_BINARY_OPERATOR, QLLexer.INVALID, QLLexer.L_NOT);

  private final Models models;
  private final Map<String, ElementIndex> indexes = new LinkedHashMap<>();

  public QueryFilterCompletion(Models models) {
    this.models = models;
  }

  /**
   * @param scope the Document Model the expression's own field paths are evaluated against; may be null
   * @return empty if nothing sensible can be proposed at {@code caret}
   */
  public Optional<Result> complete(String text, int caret, DocumentModel scope) {
    String before = text.substring(0, caret);
    if (before.isBlank()) {
      return Optional.empty();
    }
    int quoteStart = openQuote(before);
    if (quoteStart >= 0) {
      return completeInString(text, caret, before, quoteStart, scope);
    }

    List<Token> tokens = lex(before);
    boolean trailingSpace = !before.isEmpty() && Character.isWhitespace(before.charAt(before.length() - 1));
    int prefixStart = caret;
    String prefix = "";
    if (!trailingSpace && !tokens.isEmpty()) {
      Token last = tokens.getLast();
      Token previous = tokens.size() > 1 ? tokens.get(tokens.size() - 2) : null;
      boolean prefixToken = WORD_TOKENS.contains(last.getType())
          || OPERATOR_TOKENS.contains(last.getType()) && previous != null && previous.getType() == QLLexer.I_FIELD;
      if (last.getStopIndex() + 1 == before.length() && prefixToken) {
        prefix = last.getText();
        prefixStart = last.getStartIndex();
        tokens = tokens.subList(0, tokens.size() - 1);
      }
    }

    List<Proposal> proposals = propose(tokens, scope, false);
    return result(prefixStart, caret, prefix, proposals);
  }

  /** Inside an unterminated string literal: only names (Has arguments) and enumeration values make sense. */
  private Optional<Result> completeInString(String text, int caret, String before, int quoteStart, DocumentModel scope) {
    List<Token> tokens = lex(before.substring(0, quoteStart));
    List<Proposal> proposals = propose(tokens, scope, true);
    int end = caret < text.length() && text.charAt(caret) == '"' ? caret + 1 : caret;
    return result(quoteStart, end, before.substring(quoteStart + 1), proposals);
  }

  private static Optional<Result> result(int start, int end, String prefix, List<Proposal> proposals) {
    String filter = prefix.startsWith("\"") ? prefix.substring(1) : prefix;
    List<Proposal> matching = proposals.stream()
        .filter(proposal -> unquoted(proposal.label()).toLowerCase().startsWith(filter.toLowerCase()))
        .filter(proposal -> !proposal.insertText().trim().equals(prefix))
        .toList();
    return matching.isEmpty() ? Optional.empty() : Optional.of(new Result(start, end, matching));
  }

  private static String unquoted(String label) {
    return label.length() >= 2 && label.startsWith("\"") && label.endsWith("\"")
        ? label.substring(1, label.length() - 1) : label;
  }

  // ---------------------------------------------------------------------------------------------------------

  private List<Proposal> propose(List<Token> tokens, DocumentModel rootScope, boolean inString) {
    Token last = tokens.isEmpty() ? null : tokens.getLast();
    Token previous = tokens.size() > 1 ? tokens.get(tokens.size() - 2) : null;
    List<Frame> frames = frames(tokens);
    DocumentModel scope = scope(frames, tokens, rootScope);
    Frame call = frames.isEmpty() ? null : frames.getLast();

    if (last != null && last.getType() == QLLexer.S_BINARY_OPERATOR && previous != null
        && previous.getType() == QLLexer.I_FIELD) {
      return valueProposals(last.getText(), fieldInfo(previous.getText(), scope), inString);
    }
    if (inString) {
      return argumentProposals(call, tokens, scope, true);
    }
    if (last == null || last.getType() == QLLexer.L_AND || last.getType() == QLLexer.L_OR) {
      return topLevel();
    }
    if (last.getType() == QLLexer.B_OPEN_PAREN || last.getType() == QLLexer.S_COMMA) {
      if (call != null && call.callee != null) {
        return argumentProposals(call, tokens, scope, false);
      }
      return topLevel();
    }
    if (last.getType() == QLLexer.I_FIELD) {
      return operatorProposals(fieldInfo(last.getText(), scope));
    }
    boolean expressionComplete = last.getType() == QLLexer.B_CLOSE_PAREN || isLiteral(last);
    if (expressionComplete && (call == null || call.callee == null || "Has".equals(call.callee))) {
      return logicalProposals();
    }
    return List.of();
  }

  private static boolean isLiteral(Token token) {
    return Set.of(QLLexer.STRING_LITERAL, QLLexer.NUMBER_LITERAL, QLLexer.C_TRUE, QLLexer.C_FALSE, QLLexer.C_NULL)
        .contains(token.getType());
  }

  private static List<Proposal> topLevel() {
    return List.of(
        new Proposal("Match", "Match(" + CARET + ")", "Free-text search over fields"),
        new Proposal("Has", "Has(" + CARET + ")", "Condition on a related Document Model"),
        new Proposal("InRange", "InRange(" + CARET + ")", "Range condition on a field"),
        new Proposal("!", "!(" + CARET + ")", "Not operator"),
        new Proposal("[Field]", "[/", "Field reference"));
  }

  private static List<Proposal> logicalProposals() {
    return List.of(new Proposal("and", "and ", "And operator"), new Proposal("or", "or ", "Or operator"));
  }

  // ---- operators and values ----

  private static boolean accepts(FieldReferenceType parameter, FieldInfo field) {
    return parameter.fieldTypeName == null || field == null || field.type() == null
        || parameter.fieldTypeName.equals(field.type());
  }

  private static List<Proposal> operatorProposals(FieldInfo field) {
    List<Proposal> proposals = new ArrayList<>();
    for (Map.Entry<String, String> operator : OPERATOR_RAW.entrySet()) {
      FunctionType function = QlFunctions.find(operator.getKey());
      boolean applicable = function != null && function.signatures.stream()
          .anyMatch(signature -> signature.size() == 2 && signature.get(0).type() instanceof FieldReferenceType parameter
              && accepts(parameter, field));
      if (applicable) {
        proposals.add(new Proposal(operator.getValue(), operator.getValue() + " ", operator.getKey() + " operator"));
      }
    }
    proposals.sort(java.util.Comparator.comparing(Proposal::label));
    return proposals;
  }

  private static List<Proposal> valueProposals(String rawOperator, FieldInfo field, boolean inString) {
    String callee = OPERATOR_RAW.entrySet().stream().filter(entry -> entry.getValue().equals(rawOperator))
        .map(Map.Entry::getKey).findFirst().orElse(null);
    FunctionType function = callee == null ? null : QlFunctions.find(callee);
    if (function == null) {
      return List.of();
    }
    Map<String, Proposal> proposals = new LinkedHashMap<>();
    if (field != null && field.enumerationValues() != null && ("Equal".equals(callee) || "NotEqual".equals(callee))) {
      for (String value : field.enumerationValues()) {
        proposals.put("\"" + value + "\"", new Proposal("\"" + value + "\"", "\"" + value + "\"", "Enumeration value"));
      }
    }
    if (inString) {
      return new ArrayList<>(proposals.values());
    }
    for (List<Arg> signature : function.signatures) {
      if (signature.size() != 2 || !(signature.get(0).type() instanceof FieldReferenceType parameter)
          || !accepts(parameter, field)) {
        continue;
      }
      for (QlType type : signature.get(1).type().resolve()) {
        switch (type) {
          case NullType t -> proposals.putIfAbsent("Null", new Proposal("Null", "Null", "No value"));
          case BooleanType t -> {
            proposals.putIfAbsent("True", new Proposal("True", "True", "Boolean"));
            proposals.putIfAbsent("False", new Proposal("False", "False", "Boolean"));
          }
          case StringType t -> proposals.putIfAbsent("String", new Proposal("String", "\"" + CARET + "\"", "String"));
          case NumberType t -> proposals.putIfAbsent("Number", new Proposal("Number", "0", "Number"));
          case ObjectType t -> proposals.putIfAbsent(t.displayName(), constructor(t.displayName(), field));
          default -> {
          }
        }
      }
    }
    return new ArrayList<>(proposals.values());
  }

  private static Proposal constructor(String name, FieldInfo field) {
    String insert = switch (name) {
      case "DateTime" -> "DateTime(Date(" + CARET + "), Time())";
      case "DateFragment" -> "DateFragment(" + CARET + ")";
      default -> name + "(" + CARET + ")";
    };
    String detail = "DateFragment".equals(name) && field != null && field.formatOfFragment() != null
        ? "DateFragment, format " + field.formatOfFragment() : name;
    return new Proposal(name, insert, detail);
  }

  // ---- Has arguments ----

  private List<Proposal> argumentProposals(Frame call, List<Token> tokens, DocumentModel scope, boolean inString) {
    if (call == null || !"Has".equals(call.callee)) {
      return List.of();
    }
    int index = call.argStarts.size() - 1;
    if (index == 0) {
      List<Proposal> proposals = new ArrayList<>();
      for (RelationshipModel relationship : models.relationshipModels()) {
        if (relationship.getContent() == null) {
          continue;
        }
        boolean related = scope == null || relationship.getContent().getEntityCharacteristics().stream()
            .anyMatch(characteristic -> scope.getId().equals(characteristic.getDocumentModel()));
        if (related) {
          proposals.add(new Proposal("\"" + relationship.getId() + "\"", "\"" + relationship.getId() + "\"",
              "Relationship Model"));
        }
      }
      return proposals;
    }
    if (index == 1) {
      String relationshipId = stringArgument(tokens, call, 0);
      RelationshipModel relationship = relationshipId == null ? null : models.relationshipModel(relationshipId);
      if (relationship == null || relationship.getContent() == null) {
        return List.of();
      }
      List<EntityCharacteristic> characteristics = relationship.getContent().getEntityCharacteristics();
      List<String> roles = scope == null
          ? characteristics.stream().map(EntityCharacteristic::getRole).toList()
          : QueryFilterReferenceChecker.expectedTargetRoles(characteristics, scope.getId());
      return roles.stream()
          .map(role -> new Proposal("\"" + role + "\"", "\"" + role + "\"", "Role of " + relationshipId))
          .toList();
    }
    if (inString) {
      return List.of();
    }
    List<Proposal> proposals = new ArrayList<>(topLevel());
    proposals.add(new Proposal("Null", "Null", "No constraint"));
    return proposals;
  }

  // ---- token context ----

  /** An unclosed {@code (}: the callee in front of it (null for a plain group) and where its arguments start. */
  static final class Frame {

    final String callee;
    final List<Integer> argStarts = new ArrayList<>();

    Frame(String callee, int firstArgument) {
      this.callee = callee;
      this.argStarts.add(firstArgument);
    }
  }

  static List<Frame> frames(List<Token> tokens) {
    List<Frame> stack = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      switch (token.getType()) {
        case QLLexer.B_OPEN_PAREN -> {
          boolean call = i > 0 && tokens.get(i - 1).getType() == QLLexer.I_CALLEE;
          stack.add(new Frame(call ? tokens.get(i - 1).getText() : null, i + 1));
        }
        case QLLexer.B_CLOSE_PAREN -> {
          if (!stack.isEmpty()) {
            stack.removeLast();
          }
        }
        case QLLexer.S_COMMA -> {
          if (!stack.isEmpty()) {
            stack.getLast().argStarts.add(i + 1);
          }
        }
        default -> {
        }
      }
    }
    return stack;
  }

  /** The text of argument {@code index} of {@code call} if it is exactly one string literal, else null. */
  static String stringArgument(List<Token> tokens, Frame call, int index) {
    int start = call.argStarts.get(index);
    int end = index + 1 < call.argStarts.size() ? call.argStarts.get(index + 1) - 1 : tokens.size();
    if (end - start != 1 || tokens.get(start).getType() != QLLexer.STRING_LITERAL) {
      return null;
    }
    String literal = tokens.get(start).getText();
    return literal.substring(1, literal.length() - 1);
  }

  /** The Document Model the caret's expression is evaluated against: the root's, or - inside the constraint
   * arguments of enclosing {@code Has(...)} calls - the role's / the link's. */
  DocumentModel scope(List<Frame> frames, List<Token> tokens, DocumentModel root) {
    DocumentModel scope = root;
    for (Frame frame : frames) {
      if (!"Has".equals(frame.callee)) {
        continue;
      }
      int index = frame.argStarts.size() - 1;
      String relationshipId = index >= 2 ? stringArgument(tokens, frame, 0) : null;
      if (relationshipId == null) {
        continue;
      }
      if (index == 2) {
        String role = stringArgument(tokens, frame, 1);
        scope = role == null ? null : models.roleDocumentModel(relationshipId, role);
      }
      else {
        RelationshipModel relationship = models.relationshipModel(relationshipId);
        scope = relationship == null || relationship.getContent() == null ? null
            : models.documentModel(relationship.getContent().getLinkDocumentModelValue());
      }
    }
    return scope;
  }

  FieldInfo fieldInfo(String fieldToken, DocumentModel scope) {
    if (scope == null || scope.getContent() == null || scope.getContent().getModelRoot() == null
        || fieldToken.length() < 2) {
      return null;
    }
    ElementIndex index = indexes.computeIfAbsent(scope.getId(), id -> new ElementIndex(scope, models.documentModels()));
    Element element = index.resolveAbsolutePath(fieldToken.substring(1, fieldToken.length() - 1)).orElse(null);
    return element instanceof FieldElement field ? QueryFilterTypeChecker.fieldInfo(index, field) : null;
  }

  // ---- lexing ----

  /** The tokens of {@code source} on the default channel, whitespace already skipped by the lexer. */
  static List<Token> lex(String source) {
    QLLexer lexer = new QLLexer(CharStreams.fromString(source));
    lexer.removeErrorListeners();
    List<Token> tokens = new ArrayList<>(lexer.getAllTokens());
    return tokens;
  }

  /** The index of a string literal's opening quote if {@code before} ends inside one, else -1. */
  private static int openQuote(String before) {
    int open = -1;
    for (int i = 0; i < before.length(); i++) {
      char c = before.charAt(i);
      if (c == '\\' && open >= 0) {
        i++;
      }
      else if (c == '"') {
        open = open >= 0 ? -1 : i;
      }
    }
    return open;
  }
}
