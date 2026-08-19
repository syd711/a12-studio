package de.a12.studio.ui.util.commandstack;

import org.jspecify.annotations.NonNull;

import java.util.Stack;

public class CommandStack {
  private Stack<Command> undoStack = new Stack<>();
  private Stack<Command> redoStack = new Stack<>();

  public void execute(@NonNull Command command) {
    command.execute();
    undoStack.push(command);
    redoStack.clear();
  }

  public void undo() {
    if (!canUndo()) {
      return;
    }
    Command command = undoStack.pop();
    command.undo();
    redoStack.push(command);
  }

  public void redo() {
    if (!canRedo()) {
      return;
    }
    Command command = redoStack.pop();
    command.execute();
    undoStack.push(command);
  }

  public boolean canUndo() {
    return !undoStack.isEmpty();
  }

  public boolean canRedo() {
    return !redoStack.isEmpty();
  }
}
