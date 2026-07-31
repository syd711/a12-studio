package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Moves an element from one sibling list to another (or reorders it within the same list), used for tree
 * drag-and-drop. {@code sourceSiblings}/{@code targetSiblings} are widened to {@code List<Element>} via an
 * unchecked cast: {@link de.a12.studio.models.documentmodel.ModelRoot#getRootGroups()} is typed as
 * {@code List<GroupElement>} while a nested {@link de.a12.studio.models.documentmodel.GroupConfig#getElements()}
 * is {@code List<Element>}, so a single generic type parameter can't cover both ends of a move between them.
 * Callers are responsible for only ever moving a {@code GroupElement} into or out of the root list.
 */
public class MoveNodeCommand implements Command {

  private final List<Element> sourceSiblings;

  private final List<Element> targetSiblings;

  private final Element element;

  private final int targetIndex;

  private int sourceIndex;

  @SuppressWarnings("unchecked")
  public MoveNodeCommand(@NonNull List<? extends Element> sourceSiblings, @NonNull List<? extends Element> targetSiblings,
                          @NonNull Element element, int targetIndex) {
    this.sourceSiblings = (List<Element>) sourceSiblings;
    this.targetSiblings = (List<Element>) targetSiblings;
    this.element = element;
    this.targetIndex = targetIndex;
  }

  @Override
  public void execute() {
    sourceIndex = sourceSiblings.indexOf(element);
    sourceSiblings.remove(element);

    int index = targetIndex;
    if (sourceSiblings == targetSiblings && sourceIndex < index) {
      index--;
    }
    targetSiblings.add(Math.min(index, targetSiblings.size()), element);
  }

  @Override
  public void undo() {
    targetSiblings.remove(element);
    sourceSiblings.add(Math.min(sourceIndex, sourceSiblings.size()), element);
  }
}
