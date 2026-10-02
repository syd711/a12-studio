package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.querymodel.ql.QLLexer;
import de.a12.studio.modelsvalidation.validators.query.QlFunctionDocs.Doc;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.Arg;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FieldInfo;
import de.a12.studio.modelsvalidation.validators.query.QlTypes.FunctionType;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterCompletion.Frame;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker.Models;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hover documentation for a Query Language filter - the Java counterpart of SME's {@code moduleSupport/qmm}
 * {@code information-provider.ts}: what the token under the mouse is. A callee, an operator or {@code and}/{@code
 * or}/{@code !} shows the function's documentation, a field reference shows what is known about the field, and
 * the relationship / role string of a {@code Has(...)} shows that relationship's entity characteristics.
 *
 * <p>Instances cache one {@link QueryFilterCompletion} (and so its element indexes), so create one per editor load.
 */
public final class QueryFilterHover {

  /** A block of the hover: an optional heading and either prose lines or code lines. */
  public record Section(String heading, List<String> lines, boolean code) {
  }

  /** The hover of the text range {@code [start, end)}. */
  public record Hover(int start, int end, String title, String subtitle, List<Section> sections) {
  }

  private final Models models;
  private final QueryFilterCompletion completion;

  public QueryFilterHover(Models models) {
    this.models = models;
    this.completion = new QueryFilterCompletion(models);
  }

  /**
   * @param offset the index of the character under the mouse
   * @param scope the Document Model the expression's own field paths are evaluated against; may be null
   * @return empty if the token at {@code offset} has nothing to say
   */
  public Optional<Hover> hover(String text, int offset, DocumentModel scope) {
    if (offset < 0 || offset >= text.length()) {
      return Optional.empty();
    }
    List<Token> tokens = QueryFilterCompletion.lex(text);
    int index = -1;
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (token.getStartIndex() <= offset && offset <= token.getStopIndex()) {
        index = i;
        break;
      }
    }
    if (index < 0) {
      return Optional.empty();
    }
    Token token = tokens.get(index);
    List<Token> before = tokens.subList(0, index);
    return switch (token.getType()) {
      case QLLexer.I_FIELD -> fieldHover(token, before, scope);
      case QLLexer.I_CALLEE -> functionHover(token, token.getText());
      case QLLexer.S_BINARY_OPERATOR -> functionHover(token, calleeOfOperator(token.getText()));
      case QLLexer.L_AND -> functionHover(token, "And");
      case QLLexer.L_OR -> functionHover(token, "Or");
      case QLLexer.L_NOT -> functionHover(token, "Not");
      case QLLexer.STRING_LITERAL -> hasArgumentHover(token, before);
      default -> Optional.empty();
    };
  }

  private static String calleeOfOperator(String raw) {
    return QueryFilterCompletion.OPERATOR_RAW.entrySet().stream().filter(entry -> entry.getValue().equals(raw))
        .map(Map.Entry::getKey).findFirst().orElse(null);
  }

  private static int end(Token token) {
    return token.getStopIndex() + 1;
  }

  // ---- functions and operators ----

  private static Optional<Hover> functionHover(Token token, String callee) {
    Doc doc = callee == null ? null : QlFunctionDocs.find(callee);
    if (doc == null) {
      return Optional.empty();
    }
    FunctionType function = QlFunctions.find(callee);
    boolean operator = function != null && function.kind == QlTypes.Kind.OPERATOR;
    List<Section> sections = new ArrayList<>();
    if (doc.signature() != null) {
      sections.add(new Section("Signature", List.of(doc.signature()), true));
    }
    else if (function != null && QueryFilterCompletion.OPERATOR_RAW.containsKey(callee)) {
      String raw = QueryFilterCompletion.OPERATOR_RAW.get(callee);
      sections.add(new Section("Signatures", function.signatures.stream()
          .filter(signature -> signature.size() == 2)
          .map(signature -> signature.get(0).type().displayName() + " " + raw + " "
              + signature.get(1).type().displayName())
          .toList(), true));
    }
    if (!doc.notes().isEmpty()) {
      sections.add(new Section("Notes", doc.notes(), false));
    }
    if (!doc.examples().isEmpty()) {
      sections.add(new Section("Examples", doc.examples(), true));
    }
    return Optional.of(new Hover(token.getStartIndex(), end(token), callee + (operator ? " Operator" : " Function"),
        doc.description(), sections));
  }

  // ---- fields ----

  private Optional<Hover> fieldHover(Token token, List<Token> before, DocumentModel rootScope) {
    DocumentModel scope = completion.scope(QueryFilterCompletion.frames(before), before, rootScope);
    if (scope == null) {
      return Optional.empty();
    }
    String path = token.getText().substring(1, token.getText().length() - 1);
    FieldInfo info = completion.fieldInfo(token.getText(), scope);
    List<String> lines = new ArrayList<>();
    lines.add("Document Model: " + scope.getId());
    if (info == null) {
      lines.add("The path does not resolve to a field.");
    }
    else {
      if (info.type() != null) {
        lines.add("Type: " + info.type());
      }
      if (info.formatOfFragment() != null) {
        lines.add("Format: " + info.formatOfFragment());
      }
      if (info.enumerationValues() != null && !info.enumerationValues().isEmpty()) {
        lines.add("Values: " + String.join(", ", info.enumerationValues()));
      }
    }
    return Optional.of(new Hover(token.getStartIndex(), end(token), path, null,
        List.of(new Section(null, lines, false))));
  }

  // ---- Has(relationship, role, ...) ----

  private Optional<Hover> hasArgumentHover(Token token, List<Token> before) {
    List<Frame> frames = QueryFilterCompletion.frames(before);
    Frame call = frames.isEmpty() ? null : frames.getLast();
    if (call == null || !"Has".equals(call.callee)) {
      return Optional.empty();
    }
    int argument = call.argStarts.size() - 1;
    if (argument > 1 || call.argStarts.get(argument) != before.size()) {
      return Optional.empty();
    }
    String literal = token.getText();
    String value = literal.substring(1, literal.length() - 1);
    if (argument == 0) {
      RelationshipModel relationship = models.relationshipModel(value);
      if (relationship == null || relationship.getContent() == null) {
        return Optional.empty();
      }
      List<Section> sections = new ArrayList<>();
      sections.add(new Section("Link Document Model",
          List.of(relationship.getContent().getLinkDocumentModelValue() == null ? "None"
              : relationship.getContent().getLinkDocumentModelValue()), false));
      List<EntityCharacteristic> characteristics = relationship.getContent().getEntityCharacteristics();
      for (int i = 0; i < characteristics.size(); i++) {
        sections.add(new Section("Entity Characteristic " + (i + 1), properties(characteristics.get(i)), false));
      }
      return Optional.of(new Hover(token.getStartIndex(), end(token), value + " Relationship Model",
          relationship.getDescription(), sections));
    }
    String relationshipId = QueryFilterCompletion.stringArgument(before, call, 0);
    RelationshipModel relationship = relationshipId == null ? null : models.relationshipModel(relationshipId);
    if (relationship == null || relationship.getContent() == null) {
      return Optional.empty();
    }
    return relationship.getContent().getEntityCharacteristics().stream()
        .filter(characteristic -> value.equals(characteristic.getRole())).findFirst()
        .map(characteristic -> new Hover(token.getStartIndex(), end(token), capitalize(value) + " Entity Characteristic",
            null, List.of(new Section("Properties", properties(characteristic), false))));
  }

  private static List<String> properties(EntityCharacteristic characteristic) {
    List<String> lines = new ArrayList<>();
    lines.add("Role: " + characteristic.getRole());
    lines.add("Document Model: " + characteristic.getDocumentModel());
    lines.add("Ordered: " + Boolean.TRUE.equals(characteristic.getOrdered()));
    if (characteristic.getLinkConstraints() != null && characteristic.getLinkConstraints().getMultiplicity() != null) {
      Multiplicity multiplicity = characteristic.getLinkConstraints().getMultiplicity();
      lines.add("Upper limit: " + multiplicity.getUpperLimit());
      lines.add("Unbounded: " + Boolean.TRUE.equals(multiplicity.getUnbounded()));
    }
    return lines;
  }

  private static String capitalize(String value) {
    return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
  }
}
