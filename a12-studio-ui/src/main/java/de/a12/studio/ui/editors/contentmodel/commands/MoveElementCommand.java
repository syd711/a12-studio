package de.a12.studio.ui.editors.contentmodel.commands;

import de.a12.studio.models.contentmodel.ContentElement;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.function.Consumer;

/** Moves an element one position up or down among its siblings; undo swaps it back. */
public class MoveElementCommand extends ContentCommand {

  private final ContentElement parent;
  private final ContentElement element;
  private final int delta;
  private int from;
  private int to;

  /** @param delta {@code -1} to move up, {@code 1} to move down */
  public MoveElementCommand(@NonNull ContentElement parent, @NonNull ContentElement element, int delta,
      @NonNull Consumer<ContentElement> selection) {
    super(selection);
    this.parent = parent;
    this.element = element;
    this.delta = delta;
  }

  @Override
  public void execute() {
    from = parent.getChildren().indexOf(element);
    to = from + delta;
    Collections.swap(parent.getChildren(), from, to);
    select(element);
  }

  @Override
  public void undo() {
    Collections.swap(parent.getChildren(), from, to);
    select(element);
  }
}
