package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.util.Icons;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * One row of {@link QueryModelTreeController}'s tree table: either the synthetic "Model Tree" header row, the
 * synthetic row for the query's target Document Model itself (the only row the "Filter Definition" column
 * applies to - see {@link QueryModelTreeController}'s class doc), or a Document Model element (Field or Group)
 * wrapped via {@link ElementViewModel}. {@link #descendantFieldPaths} is populated bottom-up as the tree is
 * built (see {@code QueryModelTreeController#buildTreeItem}), so the "In Result" checkbox can show the
 * tri-state aggregate for a Group/target-DM row without re-walking the subtree on every render.
 */
class QueryTreeRow {

  enum Kind {ROOT_LABEL, TARGET_DOCUMENT_MODEL, ELEMENT}

  /** Tri-state "In Result" aggregate for a Group/target-Document-Model row's checkbox. {@code NOT_APPLICABLE}
   * (distinct from {@code NONE}) means no field lives under the row at all, so the checkbox should be hidden
   * rather than rendered unchecked. */
  enum InResultState {ALL, NONE, MIXED, NOT_APPLICABLE}

  private final Kind kind;
  private final ElementViewModel elementViewModel;
  private final String name;
  private final String path;
  private List<String> descendantFieldPaths = List.of();

  private QueryTreeRow(Kind kind, ElementViewModel elementViewModel, String name, String path) {
    this.kind = kind;
    this.elementViewModel = elementViewModel;
    this.name = name;
    this.path = path;
  }

  static QueryTreeRow rootLabel(@NonNull String label) {
    return new QueryTreeRow(Kind.ROOT_LABEL, null, label, null);
  }

  static QueryTreeRow targetDocumentModel(@NonNull String documentModelId) {
    return new QueryTreeRow(Kind.TARGET_DOCUMENT_MODEL, null, documentModelId, null);
  }

  static QueryTreeRow element(@NonNull ElementViewModel elementViewModel, @NonNull String path) {
    return new QueryTreeRow(Kind.ELEMENT, elementViewModel, elementViewModel.getName(), path);
  }

  Kind getKind() {
    return kind;
  }

  String getName() {
    return name;
  }

  /** {@code null} for {@link Kind#ROOT_LABEL} and {@link Kind#TARGET_DOCUMENT_MODEL} rows - only a Field or
   * Group element row has a single path of its own; those two synthetic rows instead expose the whole
   * projection's paths via {@link #descendantFieldPaths}. */
  @Nullable
  String getPath() {
    return path;
  }

  boolean isField() {
    return kind == Kind.ELEMENT && !elementViewModel.isGroup();
  }

  boolean isGroup() {
    return kind == Kind.ELEMENT && elementViewModel.isGroup();
  }

  /** Whether this row shows an "In Result" checkbox at all: every row except the plain "Model Tree" label. */
  boolean hasInResultCheckbox() {
    return kind != Kind.ROOT_LABEL;
  }

  /** Whether this row shows the "Filter Definition" cell: only the target Document Model's own row (see class
   * doc on {@link QueryModelTreeController} for why it's collapsed to one filter per query rather than one per
   * node, unlike SME). */
  boolean hasFilterDefinition() {
    return kind == Kind.TARGET_DOCUMENT_MODEL;
  }

  String getIcon() {
    return switch (kind) {
      case ROOT_LABEL -> null;
      case TARGET_DOCUMENT_MODEL -> Icons.PNG_MODEL_DOCUMENT;
      case ELEMENT -> elementViewModel.getIcon();
    };
  }

  void setDescendantFieldPaths(@NonNull List<String> descendantFieldPaths) {
    this.descendantFieldPaths = descendantFieldPaths;
  }

  /** Every field path reachable under this row: itself for a field row, the recursive union of children for a
   * Group or the target-Document-Model row. Empty for a field-less Group (no field to project). */
  List<String> getDescendantFieldPaths() {
    return descendantFieldPaths;
  }

  /** Aggregate "In Result" state used for the checkbox's checked/indeterminate rendering, mirroring SME's
   * {@code allChidrenOfGroupAreInResult}. */
  InResultState inResultState(@NonNull List<String> selectedFields) {
    if (isField()) {
      return selectedFields.contains(path) ? InResultState.ALL : InResultState.NONE;
    }
    List<String> fields = descendantFieldPaths;
    if (fields.isEmpty()) {
      return InResultState.NOT_APPLICABLE;
    }
    boolean allSelected = true;
    boolean noneSelected = true;
    for (String field : fields) {
      if (selectedFields.contains(field)) {
        noneSelected = false;
      }
      else {
        allSelected = false;
      }
    }
    if (allSelected) {
      return InResultState.ALL;
    }
    if (noneSelected) {
      return InResultState.NONE;
    }
    return InResultState.MIXED;
  }

  /** Every {@link FieldElement} reachable under {@code elementViewModel} (itself, if it is one; recursively
   * through every {@link GroupElement} descendant otherwise). Rule/Computation elements aren't projectable
   * query fields, so they - and anything nested only under them - are silently skipped. */
  static List<String> collectDescendantFieldPaths(@NonNull ElementViewModel elementViewModel, @NonNull Function<ElementViewModel, String> pathResolver) {
    List<String> paths = new ArrayList<>();
    collectDescendantFieldPaths(elementViewModel, pathResolver, paths);
    return paths;
  }

  private static void collectDescendantFieldPaths(ElementViewModel elementViewModel, Function<ElementViewModel, String> pathResolver, List<String> out) {
    if (elementViewModel.getElement() instanceof FieldElement) {
      out.add(pathResolver.apply(elementViewModel));
      return;
    }
    if (elementViewModel.getElement() instanceof GroupElement) {
      for (ElementViewModel child : elementViewModel.getChildren()) {
        collectDescendantFieldPaths(child, pathResolver, out);
      }
    }
  }
}
