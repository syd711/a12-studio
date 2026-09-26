package de.a12.studio.ui.editors.contentmodel.commands;

import de.a12.studio.models.contentmodel.ContentElement;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.function.Consumer;

/** Inserts an element into a parent's children (add child, paste, duplicate); undo removes it again. */
public class InsertElementCommand extends ContentCommand {

  private final ContentElement parent;
  private final ContentElement element;
  private final int index;
  // "children" absent and "children": [] are different on disk, so undo puts back the absent key if this created the list.
  private boolean createdChildren;

  /** @param index position among the parent's children; anything past the end appends */
  public InsertElementCommand(@NonNull ContentElement parent, @NonNull ContentElement element, int index,
      @NonNull Consumer<ContentElement> selection) {
    super(selection);
    this.parent = parent;
    this.element = element;
    this.index = index;
  }

  @Override
  public void execute() {
    createdChildren = parent.getChildren() == null;
    if (createdChildren) {
      parent.setChildren(new ArrayList<>());
    }
    parent.getChildren().add(Math.max(0, Math.min(index, parent.getChildren().size())), element);
    select(element);
  }

  @Override
  public void undo() {
    parent.getChildren().remove(element);
    if (createdChildren && parent.getChildren().isEmpty()) {
      parent.setChildren(null);
    }
    select(parent);
  }
}
