package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Replaces a node with a different node at the same position in a sibling list (e.g. converting a repeat to a
 * different {@link de.a12.studio.models.formmodel.AbstractRepeat} subtype), undoing by putting the original
 * node back in its place. Unlike {@link AddNodeCommand}/{@link DeleteNodeCommand}, both nodes stay resolvable
 * by identity ({@link List#set}) rather than by {@code indexOf}, so it also works when the list contains
 * duplicate-looking entries.
 */
public class ReplaceNodeCommand implements Command {

  private final List<Object> siblings;

  private final Object oldNode;

  private final Object newNode;

  private int index;

  public ReplaceNodeCommand(@NonNull List<Object> siblings, @NonNull Object oldNode, @NonNull Object newNode) {
    this.siblings = siblings;
    this.oldNode = oldNode;
    this.newNode = newNode;
  }

  @Override
  public void execute() {
    index = siblings.indexOf(oldNode);
    siblings.set(index, newNode);
  }

  @Override
  public void undo() {
    siblings.set(index, oldNode);
  }
}
