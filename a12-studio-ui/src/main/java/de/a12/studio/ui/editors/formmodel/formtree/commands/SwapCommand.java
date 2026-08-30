package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

/** Swaps two positions in a sibling list (used for "Move Up"/"Move Down") - its own inverse, so undo re-swaps them. */
public class SwapCommand implements Command {

  private final List<Object> list;

  private final int indexA;

  private final int indexB;

  public SwapCommand(@NonNull List<Object> list, int indexA, int indexB) {
    this.list = list;
    this.indexA = indexA;
    this.indexB = indexB;
  }

  @Override
  public void execute() {
    Collections.swap(list, indexA, indexB);
  }

  @Override
  public void undo() {
    Collections.swap(list, indexA, indexB);
  }
}
