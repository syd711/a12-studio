package de.a12.studio.ui.editors.propertyeditors;

import javafx.scene.Node;

import java.util.Optional;

/**
 * Supplies {@link RuleEditorController}'s hover popup: the documentation of the token under the mouse. Each
 * expression language decides what its tokens mean, so this is a pluggable strategy like {@link SuggestionProvider}.
 */
@FunctionalInterface
public interface HoverProvider {

  /**
   * @param text      the editor's full current plain text
   * @param charIndex the index of the character under the mouse
   * @return the popup's content, or empty to show no popup
   */
  Optional<Node> hover(String text, int charIndex);
}
