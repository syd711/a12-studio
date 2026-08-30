package de.a12.studio.ui.util.commandstack;

import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Bundles several {@link Command}s so a single {@link CommandStack#execute(Command)} call - and later a single
 * {@link CommandStack#undo()}/{@link CommandStack#redo()} - covers all of them as one atomic step. Executes its
 * children in order; undoes them in reverse order, mirroring how the inverse of "do A then B" is "undo B then
 * undo A". Used e.g. to combine a detach-from-old-parent command with an attach-to-new-parent command into one
 * drag-and-drop move.
 */
public class CompositeCommand implements Command {

  private final List<Command> commands;

  public CompositeCommand(@NonNull Command... commands) {
    this.commands = List.of(commands);
  }

  @Override
  public void execute() {
    for (Command command : commands) {
      command.execute();
    }
  }

  @Override
  public void undo() {
    for (int i = commands.size() - 1; i >= 0; i--) {
      commands.get(i).undo();
    }
  }
}
