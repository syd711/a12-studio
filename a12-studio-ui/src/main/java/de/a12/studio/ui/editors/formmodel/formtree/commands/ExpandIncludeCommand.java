package de.a12.studio.ui.editors.formmodel.formtree.commands;

import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FieldConfiguration;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.formmodel.GroupConfiguration;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.modelsvalidation.formincludes.FormIncludeExpander;
import de.a12.studio.ui.util.commandstack.Command;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Puts the result of a {@link FormIncludeExpander.Expansion} into a Form Model as one undo step: the copied
 * elements go where the include is (a list of siblings - replacing the elements of the include being refreshed, if
 * any - or the single grid slot of an {@link EmbeddedRepeat}), and the copied field/group configuration entries are
 * added to the model's configuration - or, where the host already has an entry for the same element, merged into a
 * copy of it ({@link FormIncludeExpander#fillUnset}), so undoing puts the original entry instance back.
 */
public class ExpandIncludeCommand implements Command {

  /** Where the copied elements go, and how to take them out again. */
  private interface Placement {

    void place(List<ScreenElement> elements);

    void restore(List<ScreenElement> elements);
  }

  /** A range of a list of siblings, possibly empty (a plain insert). */
  private static final class InList implements Placement {

    private final List<Object> siblings;
    private final int replaceFrom;
    private final int replaceCount;
    private List<Object> replaced = List.of();

    private InList(List<Object> siblings, int replaceFrom, int replaceCount) {
      this.siblings = siblings;
      this.replaceFrom = replaceFrom;
      this.replaceCount = replaceCount;
    }

    @Override
    public void place(List<ScreenElement> elements) {
      List<Object> range = siblings.subList(replaceFrom, replaceFrom + replaceCount);
      replaced = new ArrayList<>(range);
      range.clear();
      siblings.addAll(replaceFrom, elements);
    }

    @Override
    public void restore(List<ScreenElement> elements) {
      siblings.subList(replaceFrom, replaceFrom + elements.size()).clear();
      siblings.addAll(replaceFrom, replaced);
    }
  }

  /** The one grid an Embedded Repeat holds; the expansion has to consist of exactly one Control Grid. */
  private static final class InGridSlot implements Placement {

    private final EmbeddedRepeat repeat;
    private ControlGrid replaced;

    private InGridSlot(EmbeddedRepeat repeat) {
      this.repeat = repeat;
    }

    @Override
    public void place(List<ScreenElement> elements) {
      replaced = repeat.getControlGrid();
      repeat.setControlGrid((ControlGrid) elements.get(0));
    }

    @Override
    public void restore(List<ScreenElement> elements) {
      repeat.setControlGrid(replaced);
    }
  }

  private final FormModelContent content;

  private final Placement placement;

  private final FormIncludeExpander.Expansion expansion;

  private Runnable undoConfiguration = () -> {
  };

  /**
   * @param replaceFrom  index of the first element to replace, or where to insert when {@code replaceCount} is 0
   * @param replaceCount how many elements the expansion replaces (the run of an include that is refreshed)
   */
  public ExpandIncludeCommand(@NonNull FormModelContent content, @NonNull List<Object> siblings, int replaceFrom, int replaceCount,
      FormIncludeExpander.@NonNull Expansion expansion) {
    this(content, new InList(siblings, replaceFrom, replaceCount), expansion);
  }

  /**
   * The expansion becomes the grid of {@code repeat}, replacing the one it has. Only an expansion of exactly one
   * Control Grid fits ({@link FormIncludeExpander.Expansion#isSingleControlGrid()}).
   */
  public ExpandIncludeCommand(@NonNull FormModelContent content, @NonNull EmbeddedRepeat repeat,
      FormIncludeExpander.@NonNull Expansion expansion) {
    this(content, new InGridSlot(repeat), expansion);
    if (!expansion.isSingleControlGrid()) {
      throw new IllegalArgumentException("An Embedded Repeat holds exactly one Control Grid");
    }
  }

  private ExpandIncludeCommand(FormModelContent content, Placement placement, FormIncludeExpander.Expansion expansion) {
    this.content = content;
    this.placement = placement;
    this.expansion = expansion;
  }

  @Override
  public void execute() {
    placement.place(expansion.elements());

    if (content.getFieldConfiguration() == null) {
      content.setFieldConfiguration(new FieldConfiguration());
    }
    if (content.getGroupConfiguration() == null) {
      content.setGroupConfiguration(new GroupConfiguration());
    }
    Runnable undoFields = merge(content.getFieldConfiguration().getField(), expansion.fieldEntries(),
        FieldConfigEntry::getElementRef, FieldConfigEntry.class);
    Runnable undoGroups = merge(content.getGroupConfiguration().getGroup(), expansion.groupEntries(),
        GroupConfigEntry::getGroupRef, GroupConfigEntry.class);
    undoConfiguration = () -> {
      undoGroups.run();
      undoFields.run();
    };
  }

  @Override
  public void undo() {
    undoConfiguration.run();
    placement.restore(expansion.elements());
  }

  /** Adds or merges {@code entries} into {@code target}; returns what undoes exactly that. */
  private static <T> Runnable merge(List<T> target, List<T> entries, Function<T, String> key, Class<T> type) {
    List<T> added = new ArrayList<>();
    Map<Integer, T> replacedEntries = new LinkedHashMap<>();
    for (T entry : entries) {
      int index = indexOf(target, key.apply(entry), key);
      if (index < 0) {
        target.add(entry);
        added.add(entry);
      }
      else {
        replacedEntries.put(index, target.get(index));
        target.set(index, FormIncludeExpander.fillUnset(target.get(index), entry, type));
      }
    }
    return () -> {
      added.forEach(entry -> target.removeIf(candidate -> candidate == entry));
      replacedEntries.forEach(target::set);
    };
  }

  private static <T> int indexOf(List<T> entries, String ref, Function<T, String> key) {
    for (int i = 0; i < entries.size(); i++) {
      if (ref != null && ref.equals(key.apply(entries.get(i)))) {
        return i;
      }
    }
    return -1;
  }
}
