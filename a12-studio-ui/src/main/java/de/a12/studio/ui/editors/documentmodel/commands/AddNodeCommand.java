package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.ui.editors.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class AddNodeCommand<T extends Element> implements Command {

  private final List<T> siblings;

  private final T element;

  private final int index;

  public AddNodeCommand(@NonNull List<T> siblings, @NonNull T element, int index) {
    this.siblings = siblings;
    this.element = element;
    this.index = index;
  }

  @Override
  public void execute() {
    siblings.add(Math.min(index, siblings.size()), element);
  }

  @Override
  public void undo() {
    siblings.remove(element);
  }
}
