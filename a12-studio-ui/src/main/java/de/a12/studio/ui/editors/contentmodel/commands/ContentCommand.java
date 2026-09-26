package de.a12.studio.ui.editors.contentmodel.commands;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * A change to the Content Model's element tree. The element tree view is rebuilt from the model after every
 * execute/undo/redo, so a command only touches the model and reports, through {@code selection}, which element the
 * editor should select afterwards.
 */
abstract class ContentCommand implements Command {

  private final Consumer<ContentElement> selection;

  ContentCommand(@NonNull Consumer<ContentElement> selection) {
    this.selection = selection;
  }

  final void select(ContentElement element) {
    selection.accept(element);
  }
}
