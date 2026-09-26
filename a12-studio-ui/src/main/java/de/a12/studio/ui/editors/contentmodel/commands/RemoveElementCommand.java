package de.a12.studio.ui.editors.contentmodel.commands;

import de.a12.studio.models.contentmodel.ContentElement;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

/** Removes an element, with all its children, from its parent (delete, cut); undo puts it back at its position. */
public class RemoveElementCommand extends ContentCommand {

  private final ContentElement parent;
  private final ContentElement element;
  private int index;

  public RemoveElementCommand(@NonNull ContentElement parent, @NonNull ContentElement element,
      @NonNull Consumer<ContentElement> selection) {
    super(selection);
    this.parent = parent;
    this.element = element;
  }

  @Override
  public void execute() {
    List<ContentElement> siblings = parent.getChildren();
    index = siblings.indexOf(element);
    siblings.remove(index);
    // The sibling that took its place, else the one before, else the parent.
    select(siblings.isEmpty() ? parent : siblings.get(Math.min(index, siblings.size() - 1)));
  }

  @Override
  public void undo() {
    parent.getChildren().add(index, element);
    select(element);
  }
}
