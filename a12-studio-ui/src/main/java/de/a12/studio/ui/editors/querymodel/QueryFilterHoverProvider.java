package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterHover;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterHover.Hover;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterHover.Section;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker.Models;
import de.a12.studio.ui.editors.propertyeditors.HoverProvider;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Optional;

/**
 * Hover documentation for a Query filter definition (SME's qmm editor information provider): functions,
 * operators, field references and the relationship/role arguments of {@code Has}, from {@link QueryFilterHover}.
 */
final class QueryFilterHoverProvider implements HoverProvider {

  private static final double MAX_WIDTH = 520;

  private final QueryFilterHover hover;
  private final DocumentModel scope;

  QueryFilterHoverProvider(Models models, DocumentModel scope) {
    this.hover = new QueryFilterHover(models);
    this.scope = scope;
  }

  @Override
  public Optional<Node> hover(String text, int charIndex) {
    return hover.hover(text, charIndex, scope).map(QueryFilterHoverProvider::toNode);
  }

  private static Node toNode(Hover hover) {
    VBox box = new VBox();
    box.setMaxWidth(MAX_WIDTH);
    box.getChildren().add(label(hover.title(), "hover-doc-title"));
    if (hover.subtitle() != null && !hover.subtitle().isBlank()) {
      box.getChildren().add(label(hover.subtitle(), "hover-doc-text"));
    }
    for (Section section : hover.sections()) {
      if (section.heading() != null) {
        box.getChildren().add(label(section.heading(), "hover-doc-heading"));
      }
      for (String line : section.lines()) {
        box.getChildren().add(section.code() ? label(line, "hover-doc-code") : label("• " + line, "hover-doc-text"));
      }
    }
    return box;
  }

  private static Label label(String text, String styleClass) {
    Label label = new Label(text);
    label.setWrapText(true);
    label.setMaxWidth(MAX_WIDTH);
    label.getStyleClass().add(styleClass);
    return label;
  }
}
