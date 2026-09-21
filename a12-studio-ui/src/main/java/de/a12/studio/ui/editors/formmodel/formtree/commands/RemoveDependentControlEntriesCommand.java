package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Drops the entries of every {@link Control#getDependentControls()} block that point at one of {@code removedIds}
 * (the screen elements a delete is about to take out of the model), the way SME's delete refactoring does; a
 * block left without any entry is dropped too, since {@code DependentControlsAtLeastOneOptionValidator} rejects
 * an empty one. Undo puts back exactly what was removed - the entries at their old positions and the block
 * itself - so it belongs in the same {@link de.a12.studio.ui.util.commandstack.CompositeCommand} as the detach:
 * run it <em>after</em> the detach (a Control inside the deleted subtree is then no longer visited, it goes away
 * with its own block anyway) and it is undone <em>before</em> the node is re-attached.
 */
public class RemoveDependentControlEntriesCommand implements Command {

  private record Removal(Control control, Control.DependentControls block, List<Control.DependentControls.Entry> entriesBefore) {
  }

  private final FormModelContent content;

  private final Set<String> removedIds;

  private final List<Removal> removals = new ArrayList<>();

  public RemoveDependentControlEntriesCommand(@NonNull FormModelContent content, @NonNull Set<String> removedIds) {
    this.content = content;
    this.removedIds = removedIds;
  }

  /** Whether any Control in {@code content} still lists one of {@code ids}, i.e. whether this command has work to do. */
  public static boolean isNeeded(@NonNull FormModelContent content, @NonNull Set<String> ids) {
    return !ids.isEmpty() && FormModelWalker.find(content, Control.class).stream()
        .anyMatch(control -> control.getDependentControls() != null && control.getDependentControls().getScreenElement().stream()
            .anyMatch(entry -> ids.contains(entry.getIdref())));
  }

  @Override
  public void execute() {
    removals.clear();
    for (Control control : FormModelWalker.find(content, Control.class)) {
      Control.DependentControls block = control.getDependentControls();
      if (block == null || block.getScreenElement().stream().noneMatch(entry -> removedIds.contains(entry.getIdref()))) {
        continue;
      }
      removals.add(new Removal(control, block, new ArrayList<>(block.getScreenElement())));
      block.getScreenElement().removeIf(entry -> removedIds.contains(entry.getIdref()));
      if (block.getScreenElement().isEmpty()) {
        control.setDependentControls(null);
      }
    }
  }

  @Override
  public void undo() {
    for (Removal removal : removals) {
      removal.block().getScreenElement().clear();
      removal.block().getScreenElement().addAll(removal.entriesBefore());
      removal.control().setDependentControls(removal.block());
    }
  }
}
