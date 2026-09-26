package de.a12.studio.ui.editors.contentmodel.commands;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * An edit made in the settings panels, which change an element's props (and, for table columns, its children) in
 * place. The panels have applied the edit already, so the command is recorded after the fact: the first {@link
 * #execute()} does nothing, and only undo/redo restore the element from its JSON snapshots.
 *
 * <p>Snapshots are whole-element JSON; restoring copies them back into the same {@link ContentElement} object, so
 * the element keeps its identity in the tree (and its parent's child list) across undo and redo.
 */
public class ElementStateCommand extends ContentCommand {

  private final ContentElement element;
  private final String before;
  private String after;
  private boolean applied = true;

  public ElementStateCommand(@NonNull ContentElement element, @NonNull String before, @NonNull String after,
      @NonNull Consumer<ContentElement> selection) {
    super(selection);
    this.element = element;
    this.before = before;
    this.after = after;
  }

  /** The element's current state as a snapshot for {@link ElementStateCommand}. */
  public static @NonNull String capture(@NonNull ContentElement element) {
    return JsonSettings.objectMapper.writeValueAsString(element);
  }

  public @NonNull ContentElement getElement() {
    return element;
  }

  /** Folds a further edit into this command, so one undo reverts a run of edits (typing, dragging a slider). */
  public void setAfter(@NonNull String after) {
    this.after = after;
  }

  @Override
  public void execute() {
    if (applied) {
      applied = false;
      return;
    }
    restore(after);
    select(element);
  }

  @Override
  public void undo() {
    restore(before);
    select(element);
  }

  private void restore(String json) {
    ContentElement source = JsonSettings.objectMapper.readValue(json, ContentElement.class);
    element.setId(source.getId());
    element.setType(source.getType());
    element.setNamespace(source.getNamespace());
    element.setProps(source.getProps());
    element.setChildren(source.getChildren());
    element.getExtras().clear();
    element.getExtras().putAll(source.getExtras());
  }
}
