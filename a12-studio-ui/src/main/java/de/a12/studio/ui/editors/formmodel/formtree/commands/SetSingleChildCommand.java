package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Sets a single-slot child reference (e.g. {@link de.a12.studio.models.formmodel.EmbeddedRepeat#setControlGrid},
 * {@link de.a12.studio.models.formmodel.DetachedRepeat#setDetailScreen}) to a new value, undoing by restoring
 * whatever the slot held before - {@code null} for a plain add/remove, or the previous occupant for a
 * replacement. Covers the single-slot parents {@link AddNodeCommand}/{@link DeleteNodeCommand} can't (they
 * assume a real sibling list).
 */
public class SetSingleChildCommand<T> implements Command {

  private final Consumer<T> setter;

  private final @Nullable T newValue;

  private final @Nullable T oldValue;

  public SetSingleChildCommand(Consumer<T> setter, @Nullable T newValue, @Nullable T oldValue) {
    this.setter = setter;
    this.newValue = newValue;
    this.oldValue = oldValue;
  }

  @Override
  public void execute() {
    setter.accept(newValue);
  }

  @Override
  public void undo() {
    setter.accept(oldValue);
  }
}
