package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

/** Renames an {@link Element}; undo restores its previous name. */
public class RenameElementCommand implements Command {

  private final Element element;

  private final String newName;

  private String oldName;

  public RenameElementCommand(@NonNull Element element, @NonNull String newName) {
    this.element = element;
    this.newName = newName;
  }

  @Override
  public void execute() {
    oldName = element.getName();
    element.setName(newName);
  }

  @Override
  public void undo() {
    element.setName(oldName);
  }
}
