package de.a12.studio.ui.editors.util.commandstack;

import org.jspecify.annotations.NonNull;

import java.util.Stack;

public class CommandStack {
  private Stack<Command> stack = new Stack<>();

  public void push(@NonNull Command command) {
    this.stack.push(command);
  }

  public Command pop() {
    return stack.pop();
  }
}
