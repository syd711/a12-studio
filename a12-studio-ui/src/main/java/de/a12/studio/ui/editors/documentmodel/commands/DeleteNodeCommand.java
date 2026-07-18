package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class DeleteNodeCommand<T extends Element> implements Command {

  private final List<T> siblings;

  private final T element;

  private int index;

  public DeleteNodeCommand(@NonNull List<T> siblings, @NonNull T element) {
    this.siblings = siblings;
    this.element = element;
  }

  @Override
  public void execute() {
    index = siblings.indexOf(element);
    siblings.remove(element);
  }

  @Override
  public void undo() {
    siblings.add(Math.min(index, siblings.size()), element);
  }
}
