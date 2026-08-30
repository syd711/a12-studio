package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.List;

/** Removes a node from a sibling list, remembering its index so undo re-inserts it in the same spot. */
public class DeleteNodeCommand implements Command {

  private final List<Object> siblings;

  private final Object node;

  private int index;

  public DeleteNodeCommand(@NonNull List<Object> siblings, @NonNull Object node) {
    this.siblings = siblings;
    this.node = node;
  }

  @Override
  public void execute() {
    index = siblings.indexOf(node);
    siblings.remove(node);
  }

  @Override
  public void undo() {
    siblings.add(Math.max(0, Math.min(index, siblings.size())), node);
  }
}
