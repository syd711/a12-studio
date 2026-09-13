package de.a12.studio.ui.editors.formmodel.formtree;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.util.JsonSettings;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a Form Model repeat (Inline, Embedded or Detached) into a different repeat type, mirroring the SME
 * reference's {@code repeatConverter.ts}: every field the two types share (everything on {@link AbstractRepeat},
 * inherited from the common base rather than split per subtype as in SME) carries over unchanged, and the
 * type-specific detail-edit structure is rebuilt for the target type:
 * <ul>
 *   <li>Embedded -&gt; Detached: the existing Control Grid is moved (same instance/ids) into a new detail Screen.</li>
 *   <li>Detached -&gt; Embedded: a new Control Grid is built from the detail Screen's Cells (one Row per Cell,
 *       skipping nested Inline/Embedded/Detached Repeats), matching {@code createControlGridFromDetachedRepeatScreen}.</li>
 *   <li>Inline -&gt; Embedded/Detached: a new Control Grid is built from the Inline Repeat's field-based
 *       {@code repeatOverviewColumn}s (one Row per column), matching {@code createControlGridFromInlineRepeatFieldColumns}
 *       - the original columns themselves are left untouched on the converted repeat (SME: "columns are preserved").</li>
 *   <li>Embedded/Detached -&gt; Inline: the Control Grid/detail Screen is simply dropped; the repeat's own
 *       {@code repeatOverviewColumn}s become the (already editable) inline row.</li>
 * </ul>
 * The common-field copy goes through a JSON round trip via the same {@link JsonSettings#objectMapper} used by
 * {@link FormModelActions#cloneNode} rather than manual getter/setter copying, so every nested mutable field
 * (e.g. {@code tableStyle}, {@code label}) is deep-copied - {@link FormModelActions}'s undo command puts the
 * original {@code source} node back on Undo, which must come back untouched.
 * <p>
 * Not replicated here (no equivalent infrastructure exists in a12-studio yet): SME additionally forces
 * attachment-backed field columns to read-only/"TEXT" presentation when converting into Inline/Embedded (and
 * clears that override when converting into Detached, whose separate screen can present them normally).
 */
@Slf4j
final class RepeatConverter {

  private RepeatConverter() {
  }

  /** The repeat types a repeat may be converted to/from - one {@link FormModelNodeTypes} descriptor per type. */
  enum RepeatKind {
    INLINE("InlineRepeat", "inlinerepeat", InlineRepeat.class),
    EMBEDDED("EmbeddedRepeat", "embeddedrepeat", EmbeddedRepeat.class),
    DETACHED("DetachedRepeat", "detachedrepeat", DetachedRepeat.class);

    final String typeName;
    final String idPrefix;
    final Class<? extends AbstractRepeat> resultClass;

    RepeatKind(String typeName, String idPrefix, Class<? extends AbstractRepeat> resultClass) {
      this.typeName = typeName;
      this.idPrefix = idPrefix;
      this.resultClass = resultClass;
    }
  }

  static @Nullable RepeatKind kindOf(@NonNull AbstractRepeat repeat) {
    if (repeat instanceof InlineRepeat) return RepeatKind.INLINE;
    if (repeat instanceof EmbeddedRepeat) return RepeatKind.EMBEDDED;
    if (repeat instanceof DetachedRepeat) return RepeatKind.DETACHED;
    return null;
  }

  /**
   * Converts {@code source} into a brand-new repeat of {@code targetKind}. {@code source} itself is never
   * mutated - the caller swaps it out for the returned node (see {@code FormModelActions#convertRepeat}).
   */
  static AbstractRepeat convert(@NonNull AbstractRepeat source, @NonNull RepeatKind targetKind) {
    try {
      ObjectNode node = (ObjectNode) JsonSettings.objectMapper.valueToTree(source);
      node.remove("controlGrid");
      node.remove("detailScreen");
      node.remove("multiFileUploadOptions");
      node.put("type", targetKind.typeName);
      node.put("id", FormModelElementFactory.generateId(targetKind.idPrefix));

      AbstractRepeat target = JsonSettings.objectMapper.treeToValue(node, targetKind.resultClass);
      if (target instanceof EmbeddedRepeat embedded && embedded.getTableStyle() != null) {
        // Embedded repeats don't scroll a fixed-height table the way Inline/Detached ones can.
        embedded.getTableStyle().setTableHeight(null);
      }
      applyStructuralConversion(source, target);
      return target;
    }
    catch (Exception e) {
      log.warn("Failed to convert {} to {}: {}", source.getClass().getSimpleName(), targetKind, e.getMessage(), e);
      throw new IllegalStateException("Failed to convert repeat to " + targetKind, e);
    }
  }

  private static void applyStructuralConversion(@NonNull AbstractRepeat source, @NonNull AbstractRepeat target) {
    if (source instanceof DetachedRepeat detached) {
      if (target instanceof EmbeddedRepeat embedded) {
        embedded.setControlGrid(controlGridFromScreenCells(detached.getDetailScreen()));
      }
      // Detached -> Inline: detail screen simply omitted, nothing else to do.
    }
    else if (source instanceof EmbeddedRepeat embedded) {
      if (target instanceof DetachedRepeat detached) {
        Screen screen = FormModelElementFactory.newScreen();
        if (embedded.getControlGrid() != null) {
          screen.getScreenElements().add(embedded.getControlGrid());
        }
        detached.setDetailScreen(screen);
      }
      else if (target instanceof InlineRepeat inline) {
        inline.setMultiFileUploadOptions(embedded.getMultiFileUploadOptions());
        // Embedded -> Inline: control grid simply dropped.
      }
    }
    else if (source instanceof InlineRepeat inline) {
      if (target instanceof EmbeddedRepeat embedded) {
        embedded.setControlGrid(controlGridFromColumns(target.getRepeatOverviewColumn()));
        embedded.setMultiFileUploadOptions(inline.getMultiFileUploadOptions());
      }
      else if (target instanceof DetachedRepeat detached) {
        Screen screen = FormModelElementFactory.newScreen();
        screen.getScreenElements().add(controlGridFromColumns(target.getRepeatOverviewColumn()));
        detached.setDetailScreen(screen);
      }
    }
  }

  /**
   * Builds a new Control Grid with one Row per field-based {@code repeatOverviewColumn}, each holding a single
   * Control derived from that column - the columns themselves are left untouched on the caller's repeat.
   */
  private static ControlGrid controlGridFromColumns(@NonNull List<RepeatOverviewColumn> columns) {
    ControlGrid grid = FormModelElementFactory.newControlGrid();
    for (RepeatOverviewColumn column : columns) {
      if (!(column instanceof FieldBasedRepeatOverviewColumn fieldColumn)) {
        continue;
      }
      Control control = FormModelElementFactory.newControl(fieldColumn.getElementRef());
      control.setReadonly(fieldColumn.getReadonly());
      control.setMessageExposition(fieldColumn.getMessageExposition());
      control.setDatePickerConfig(fieldColumn.getDatePickerConfig());
      Row row = FormModelElementFactory.newRow();
      row.getCell().add(control);
      grid.getRow().add(row);
    }
    return grid;
  }

  /**
   * Builds a new Control Grid with one Row per Cell found (recursively) in {@code detailScreen}, skipping any
   * nested Inline/Embedded/Detached Repeat subtree - its Cells can't be lifted out on their own, mirroring the
   * SME reference's {@code cellsInDetailScreen}.
   */
  private static ControlGrid controlGridFromScreenCells(@Nullable Screen detailScreen) {
    ControlGrid grid = FormModelElementFactory.newControlGrid();
    if (detailScreen == null) {
      return grid;
    }
    List<Cell> cells = new ArrayList<>();
    collectCells(detailScreen.getScreenElements(), cells);
    for (Cell cell : cells) {
      Row row = FormModelElementFactory.newRow();
      row.getCell().add(cell);
      grid.getRow().add(row);
    }
    return grid;
  }

  private static void collectCells(@NonNull List<ScreenElement> elements, @NonNull List<Cell> out) {
    for (ScreenElement element : elements) {
      if (element instanceof AbstractRepeat) {
        continue;
      }
      if (element instanceof ControlGrid grid) {
        for (Row row : grid.getRow()) {
          out.addAll(row.getCell());
        }
      }
      else if (element instanceof Section section) {
        collectCells(section.getScreenElements(), out);
      }
      else if (element instanceof MultiColumnSection section) {
        collectCells(section.getScreenElements(), out);
      }
    }
  }
}
