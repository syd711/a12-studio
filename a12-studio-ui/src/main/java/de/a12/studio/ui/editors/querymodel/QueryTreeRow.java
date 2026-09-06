package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.util.Icons;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * One row of {@link QueryModelTreeController}'s tree table: the synthetic "Model Tree" header row, the synthetic
 * row for the query's target Document Model, a relationship-traversal hop ({@link QueryLink}, the tree's editable
 * "graph" part), or a Document Model element (Field or Group) wrapped via {@link ElementViewModel}. Every row
 * except {@code ROOT_LABEL} carries a {@link #fieldsScope}: the {@code fields} list its own "In Result" state and
 * every descendant field/group row's toggle read/write against - {@code content.fields} for the target Document
 * Model row and everything under it that isn't itself under a {@link QueryLink}, or that link's own {@code
 * fields} list otherwise (see {@link QueryLink#getFields()}). {@link #descendantFieldPaths} is populated
 * bottom-up as the tree is built (see {@code QueryModelTreeController#buildTreeItem}), so the "In Result"
 * checkbox can show the tri-state aggregate for a Group/target-DM/relationship-link row without re-walking the
 * subtree on every render.
 */
class QueryTreeRow {

  enum Kind {ROOT_LABEL, TARGET_DOCUMENT_MODEL, RELATIONSHIP_LINK, ELEMENT}

  /** Tri-state "In Result" aggregate for a Group/target-Document-Model/relationship-link row's checkbox.
   * {@code NOT_APPLICABLE} (distinct from {@code NONE}) means no field lives under the row at all, so the
   * checkbox should be hidden rather than rendered unchecked. */
  enum InResultState {ALL, NONE, MIXED, NOT_APPLICABLE}

  private final Kind kind;
  private final ElementViewModel elementViewModel;
  private final QueryLink link;
  private final String name;
  private final String path;
  private List<String> descendantFieldPaths = List.of();
  private List<String> fieldsScope = List.of();

  private QueryTreeRow(Kind kind, ElementViewModel elementViewModel, QueryLink link, String name, String path) {
    this.kind = kind;
    this.elementViewModel = elementViewModel;
    this.link = link;
    this.name = name;
    this.path = path;
  }

  static QueryTreeRow rootLabel(@NonNull String label) {
    return new QueryTreeRow(Kind.ROOT_LABEL, null, null, label, null);
  }

  static QueryTreeRow targetDocumentModel(@NonNull String documentModelId) {
    return new QueryTreeRow(Kind.TARGET_DOCUMENT_MODEL, null, null, documentModelId, null);
  }

  /** {@code targetDocumentModelId} is null when the link's relationship/role doesn't resolve, in which case
   * this row shows just the relationship/role text with no children and no "In Result" checkbox (nothing to
   * project until the reference is fixed) - see {@link #getName()}/{@link QueryModelTreeController}'s
   * unresolved-link handling. */
  static QueryTreeRow relationshipLink(@NonNull QueryLink link, String targetDocumentModelId) {
    String name = link.getRelationshipModel() + " → " + link.getTargetRole()
        + (targetDocumentModelId == null ? " (?)" : "");
    return new QueryTreeRow(Kind.RELATIONSHIP_LINK, null, link, name, null);
  }

  static QueryTreeRow element(@NonNull ElementViewModel elementViewModel, @NonNull String path) {
    return new QueryTreeRow(Kind.ELEMENT, elementViewModel, null, elementViewModel.getName(), path);
  }

  Kind getKind() {
    return kind;
  }

  String getName() {
    return name;
  }

  @Nullable
  QueryLink getLink() {
    return link;
  }

  /** {@code null} for {@link Kind#ROOT_LABEL} and {@link Kind#TARGET_DOCUMENT_MODEL}/{@link
   * Kind#RELATIONSHIP_LINK} rows - only a Field or Group element row has a single path of its own; those other
   * rows instead expose their whole subtree's paths via {@link #descendantFieldPaths}. */
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

  boolean isRelationshipLink() {
    return kind == Kind.RELATIONSHIP_LINK;
  }

  /** Whether this relationship-link row's relationship/role resolved to an actual Document Model - if not,
   * it has no subtree and no "In Result" checkbox until fixed. */
  boolean isUnresolvedRelationshipLink() {
    return kind == Kind.RELATIONSHIP_LINK && fieldsScope.isEmpty() && descendantFieldPaths.isEmpty() && link != null;
  }

  /** Whether this row shows an "In Result" checkbox at all: every row except the plain "Model Tree" label and
   * an unresolved relationship link (nothing to project until its reference is fixed). */
  boolean hasInResultCheckbox() {
    return kind != Kind.ROOT_LABEL && !isUnresolvedRelationshipLink();
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
      case RELATIONSHIP_LINK -> Icons.PNG_MODEL_RELATIONSHIP;
      case ELEMENT -> elementViewModel.getIcon();
    };
  }

  void setDescendantFieldPaths(@NonNull List<String> descendantFieldPaths) {
    this.descendantFieldPaths = descendantFieldPaths;
  }

  /** Every field path reachable under this row: itself for a field row, the recursive union of children for a
   * Group/target-Document-Model/relationship-link row. Empty for a field-less Group or an unresolved
   * relationship link. */
  List<String> getDescendantFieldPaths() {
    return descendantFieldPaths;
  }

  /** The {@code fields} list this row's (and every descendant's) "In Result" toggle reads/writes against - see
   * class doc. Must be set (via {@link #setFieldsScope}) before {@link #inResultState}/{@link
   * QueryModelTreeController#toggleInResult} are used. */
  void setFieldsScope(@NonNull List<String> fieldsScope) {
    this.fieldsScope = fieldsScope;
  }

  List<String> getFieldsScope() {
    return fieldsScope;
  }

  /** Aggregate "In Result" state used for the checkbox's checked/indeterminate rendering, mirroring SME's
   * {@code allChidrenOfGroupAreInResult}. */
  InResultState inResultState() {
    if (isField()) {
      return fieldsScope.contains(path) ? InResultState.ALL : InResultState.NONE;
    }
    List<String> fields = descendantFieldPaths;
    if (fields.isEmpty()) {
      return InResultState.NOT_APPLICABLE;
    }
    boolean allSelected = true;
    boolean noneSelected = true;
    for (String field : fields) {
      if (fieldsScope.contains(field)) {
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
