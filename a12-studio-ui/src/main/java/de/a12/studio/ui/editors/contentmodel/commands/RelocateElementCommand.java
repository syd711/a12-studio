package de.a12.studio.ui.editors.contentmodel.commands;

import de.a12.studio.models.contentmodel.ContentElement;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Moves an element, with everything below it, to another place of the tree - another parent or another position among
 * the same parent's children (drag and drop); undo puts it back where it was.
 */
public class RelocateElementCommand extends ContentCommand {

  private final ContentElement oldParent;
  private final ContentElement element;
  private final ContentElement newParent;
  private final int newIndex;
  private int oldIndex;
  // "children" absent and "children": [] are different on disk, so undo puts back the absent key if this created the list.
  private boolean createdChildren;

  /**
   * @param newIndex position among {@code newParent}'s children <em>after</em> the element has been taken out of {@code
   *                 oldParent}; anything past the end appends
   */
  public RelocateElementCommand(@NonNull ContentElement oldParent, @NonNull ContentElement element,
      @NonNull ContentElement newParent, int newIndex, @NonNull Consumer<ContentElement> selection) {
    super(selection);
    this.oldParent = oldParent;
    this.element = element;
    this.newParent = newParent;
    this.newIndex = newIndex;
  }

  @Override
  public void execute() {
    oldIndex = oldParent.getChildren().indexOf(element);
    oldParent.getChildren().remove(oldIndex);
    createdChildren = newParent.getChildren() == null;
    if (createdChildren) {
      newParent.setChildren(new ArrayList<>());
    }
    newParent.getChildren().add(Math.max(0, Math.min(newIndex, newParent.getChildren().size())), element);
    select(element);
  }

  @Override
  public void undo() {
    newParent.getChildren().remove(element);
    if (createdChildren && newParent.getChildren().isEmpty()) {
      newParent.setChildren(null);
    }
    oldParent.getChildren().add(oldIndex, element);
    select(element);
  }
}
