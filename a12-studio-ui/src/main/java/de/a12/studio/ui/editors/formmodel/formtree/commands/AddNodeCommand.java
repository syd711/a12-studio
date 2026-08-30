package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Inserts a node into a sibling list, undoing by removing it again. Untyped ({@code List<Object>}) since Form
 * Model node types ({@link de.a12.studio.models.formmodel.Screen}, {@link de.a12.studio.models.formmodel.Row},
 * {@link de.a12.studio.models.formmodel.Cell}, ...) share no common Java supertype - mirrors {@link
 * de.a12.studio.ui.editors.documentmodel.commands.AddNodeCommand}, which can stay generic over {@code Element}.
 */
public class AddNodeCommand implements Command {

  private final List<Object> siblings;

  private final Object node;

  private final int index;

  public AddNodeCommand(@NonNull List<Object> siblings, @NonNull Object node, int index) {
    this.siblings = siblings;
    this.node = node;
    this.index = index;
  }

  @Override
  public void execute() {
    siblings.add(Math.max(0, Math.min(index, siblings.size())), node);
  }

  @Override
  public void undo() {
    siblings.remove(node);
  }
}
