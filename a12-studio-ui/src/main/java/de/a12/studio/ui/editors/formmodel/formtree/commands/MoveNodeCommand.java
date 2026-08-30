package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Moves a node from one sibling list to another (or reorders it within the same list), used for the Form Model
 * tree's drag-and-drop. Mirrors {@link de.a12.studio.ui.editors.documentmodel.commands.MoveNodeCommand}, untyped
 * ({@code List<Object>}) for the same reason as {@link AddNodeCommand}.
 */
public class MoveNodeCommand implements Command {

  private final List<Object> sourceSiblings;

  private final List<Object> targetSiblings;

  private final Object node;

  private final int targetIndex;

  private int sourceIndex;

  public MoveNodeCommand(@NonNull List<Object> sourceSiblings, @NonNull List<Object> targetSiblings,
                          @NonNull Object node, int targetIndex) {
    this.sourceSiblings = sourceSiblings;
    this.targetSiblings = targetSiblings;
    this.node = node;
    this.targetIndex = targetIndex;
  }

  @Override
  public void execute() {
    sourceIndex = sourceSiblings.indexOf(node);
    sourceSiblings.remove(node);

    int index = targetIndex;
    if (sourceSiblings == targetSiblings && sourceIndex < index) {
      index--;
    }
    targetSiblings.add(Math.max(0, Math.min(index, targetSiblings.size())), node);
  }

  @Override
  public void undo() {
    targetSiblings.remove(node);
    sourceSiblings.add(Math.max(0, Math.min(sourceIndex, sourceSiblings.size())), node);
  }
}
