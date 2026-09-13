package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.ConfirmationText;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DefaultRowAction;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.HideCondition;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.LocalizedText;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.RowActionGroup;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.formmodel.Style;
import de.a12.studio.models.formmodel.TableStyle;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.models.util.JsonSettings;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a Form Model repeat (Inline, Embedded or Detached) into a different repeat type, mirroring the SME
 * reference's {@code repeatConverter.ts}: every field the two types share (everything on {@link AbstractRepeat}
 * and {@link ScreenElement}, inherited from the common base rather than split per subtype as in SME) carries
 * over unchanged, and the type-specific detail-edit structure is rebuilt for the target type:
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
 * The common-field copy deep-clones every nested mutable field (e.g. {@code tableStyle}, {@code label}) via a
 * JSON round trip through the same {@link JsonSettings#objectMapper} used by {@link FormModelActions#cloneNode}
 * rather than aliasing the source's objects - {@link FormModelActions}'s undo command puts the original
 * {@code source} node back on Undo, which must come back untouched.
 * <p>
 * Not replicated here (no equivalent infrastructure exists in a12-studio yet): SME additionally forces
 * attachment-backed field columns to read-only/"TEXT" presentation when converting into Inline/Embedded (and
 * clears that override when converting into Detached, whose separate screen can present them normally).
 */
@Slf4j
final class RepeatConverter {

  private RepeatConverter() {
  }

  /** The repeat types a repeat may be converted to/from. */
  enum RepeatKind {
    INLINE, EMBEDDED, DETACHED
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
    AbstractRepeat target = switch (targetKind) {
      case INLINE -> FormModelElementFactory.newInlineRepeat();
      case EMBEDDED -> FormModelElementFactory.newEmbeddedRepeat();
      case DETACHED -> FormModelElementFactory.newDetachedRepeat();
    };
    copyCommonFields(source, target);
    if (target instanceof EmbeddedRepeat embedded && embedded.getTableStyle() != null) {
      // Embedded repeats don't scroll a fixed-height table the way Inline/Detached ones can.
      embedded.getTableStyle().setTableHeight(null);
    }
    applyStructuralConversion(source, target);
    return target;
  }

  private static void copyCommonFields(@NonNull AbstractRepeat source, @NonNull AbstractRepeat target) {
    // ScreenElement fields (id and type stay as freshly generated by the factory).
    target.setName(source.getName());
    target.setTitle(deepClone(source.getTitle(), LocalizedText.class));
    target.setStyle(deepCloneList(source.getStyle(), Style.class));
    target.setAnnotations(deepCloneList(source.getAnnotations(), Annotation.class));
    target.setHideCondition(deepClone(source.getHideCondition(), HideCondition.class));
    target.setIncludeId(source.getIncludeId());
    target.setFormModelRef(source.getFormModelRef());
    target.setHostDocumentModelPath(source.getHostDocumentModelPath());

    // AbstractRepeat fields.
    target.setReadonly(source.getReadonly());
    target.setRepeatOverviewColumn(deepCloneList(source.getRepeatOverviewColumn(), RepeatOverviewColumn.class));
    target.setGroupRef(source.getGroupRef());
    target.setEnableAdd(source.getEnableAdd());
    target.setEnableRemove(source.getEnableRemove());
    target.setEnableReorder(source.getEnableReorder());
    target.setEnableCopy(source.getEnableCopy());
    target.setEnableColumnsResize(source.getEnableColumnsResize());
    target.setInfiniteScrolling(source.getInfiniteScrolling());
    target.setReadonlyPresentation(source.getReadonlyPresentation());
    target.setTableStyle(deepClone(source.getTableStyle(), TableStyle.class));
    target.setDefaultRowAction(deepClone(source.getDefaultRowAction(), DefaultRowAction.class));
    target.setRowActionGroup(deepClone(source.getRowActionGroup(), RowActionGroup.class));
    target.setFilterExpression(source.getFilterExpression());
    target.setInitialSorting(source.getInitialSorting());
    target.setTitleHidden(source.getTitleHidden());
    target.setConfirmationTexts(deepCloneConfirmationTexts(source.getConfirmationTexts()));
    target.setLabel(deepClone(source.getLabel(), LocalizedText.class));
    target.setHint(deepClone(source.getHint(), TextContainer.class));
    target.setPlaceholder(deepClone(source.getPlaceholder(), TextContainer.class));
    target.setDefaultHorizontalAlignment(source.getDefaultHorizontalAlignment());
    target.setHeaderStyle(deepCloneList(source.getHeaderStyle(), Style.class));
  }

  private static <T> @Nullable T deepClone(@Nullable T value, @NonNull Class<T> type) {
    if (value == null) {
      return null;
    }
    try {
      String json = JsonSettings.objectMapper.writeValueAsString(value);
      return JsonSettings.objectMapper.readValue(json, type);
    }
    catch (Exception e) {
      log.warn("Failed to deep-clone a {} while converting a repeat: {}", type.getSimpleName(), e.getMessage(), e);
      return null;
    }
  }

  private static <T> List<T> deepCloneList(@NonNull List<T> values, @NonNull Class<T> type) {
    List<T> result = new ArrayList<>();
    for (T value : values) {
      T clone = deepClone(value, type);
      if (clone != null) {
        result.add(clone);
      }
    }
    return result;
  }

  private static Map<String, ConfirmationText> deepCloneConfirmationTexts(@NonNull Map<String, ConfirmationText> source) {
    Map<String, ConfirmationText> result = new LinkedHashMap<>();
    for (Map.Entry<String, ConfirmationText> entry : source.entrySet()) {
      ConfirmationText clone = deepClone(entry.getValue(), ConfirmationText.class);
      if (clone != null) {
        result.put(entry.getKey(), clone);
      }
    }
    return result;
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
