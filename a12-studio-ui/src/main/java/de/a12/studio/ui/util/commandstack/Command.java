package de.a12.studio.ui.util.commandstack;

public interface Command {

  void execute();

  void undo();
}
