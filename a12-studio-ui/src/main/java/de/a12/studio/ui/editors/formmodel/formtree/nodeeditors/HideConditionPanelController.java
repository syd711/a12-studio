package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits the "Hide Condition" property of a form node (Section, Row, ControlGrid, Repeat, Control):
 * a pair of fields that together specify when the node should be hidden.
 * <ul>
 *   <li>{@code hideConditionField} — the id of a master field from the linked Document Model, offered as a
 *       combo-box populated from that model's Boolean/Confirm/Enumeration fields that are actually reachable
 *       from this node's position (see {@link MasterFieldScope}), mirroring the SME reference's
 *       {@code getMasterFields}/{@code collectCompatibleFields}.</li>
 *   <li>{@code hideConditionValue} — {@code "true"} (hide when the field is true) or
 *       {@code null} / "no value" (hide when the field has no value), offered as a fixed
 *       two-item combo-box.</li>
 * </ul>
 * <p>
 * Does not extend {@link de.a12.studio.ui.editors.AbstractPropertyEditor} because it edits a
 * form-tree node (Section / Row / ControlGrid / Repeat / Control) rather than a document-model {@link Element}.
 * Saves are performed directly via {@link Studio#getSelectedProjectItem()} and
 * {@link StudioEventManager#fireModelSavedEvent}, matching the pattern used by
 * non-element panels such as {@link de.a12.studio.ui.editors.formmodel.StylesPanelController}.
 * <p>
 * Call {@link #configure} once per node selection to bind this panel to the node's getters/setters
 * and repopulate the field combo for the given {@link MasterFieldScope}.
 */
public class HideConditionPanelController implements Initializable {

  /** Displayed in the value combo to represent a stored {@code null} condition value. */
  public static final String DISPLAY_NO_VALUE = "no value";
  public static final String DISPLAY_TRUE = "true";

  @FXML
  private ComboBox<String> fieldCombo;

  @FXML
  private ComboBox<String> valueCombo;

  // Guards programmatic repopulation in configure() from being treated as user edits.
  private boolean updatingFromModel;

  // Getter/setter pair for hideConditionField on the currently bound form node.
  private Supplier<String> fieldGetter;
  private Consumer<String> fieldSetter;

  // Getter/setter pair for hideConditionValue on the currently bound form node.
  private Supplier<String> valueGetter;
  private Consumer<String> valueSetter;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    valueCombo.getItems().setAll(DISPLAY_TRUE, DISPLAY_NO_VALUE);

    // Wire fieldCombo: a user selection writes through to the model and saves.
    // Clearing the field also clears the value, since a value without a field is meaningless.
    fieldCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel || fieldSetter == null) {
        return;
      }
      String fieldValue = (newVal == null || newVal.isBlank()) ? null : newVal;
      fieldSetter.accept(fieldValue);
      if (fieldValue == null) {
        // Clear value silently so its listener does not fire an extra save.
        updatingFromModel = true;
        try {
          valueCombo.setValue(null);
        } finally {
          updatingFromModel = false;
        }
        if (valueSetter != null) {
          valueSetter.accept(null);
        }
      }
      commitChange();
    });

    // Wire valueCombo: "no value" display → null stored; "true" display → "true" stored.
    valueCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel || valueSetter == null) {
        return;
      }
      valueSetter.accept(DISPLAY_NO_VALUE.equals(newVal) ? null : newVal);
      commitChange();
    });
  }

  /**
   * Binds this panel to the hide-condition properties of a form node and repopulates the field combo from
   * {@code scope}. Must be called every time a new node is selected in the form tree.
   *
   * @param fieldGetter   reads {@code hideConditionField} from the node
   * @param fieldSetter   writes {@code hideConditionField} to the node
   * @param valueGetter   reads {@code hideConditionValue} from the node
   * @param valueSetter   writes {@code hideConditionValue} to the node
   * @param elementIndex  index over the Document Model linked to the form, or {@code null} if none is linked
   * @param scope         where in the Document Model to look for candidate master fields, see {@link MasterFieldScope}
   */
  public void configure(
      @NonNull Supplier<String> fieldGetter,
      @NonNull Consumer<String> fieldSetter,
      @NonNull Supplier<String> valueGetter,
      @NonNull Consumer<String> valueSetter,
      @Nullable ElementIndex elementIndex,
      @NonNull MasterFieldScope scope) {

    this.fieldGetter = fieldGetter;
    this.fieldSetter = fieldSetter;
    this.valueGetter = valueGetter;
    this.valueSetter = valueSetter;

    List<String> masterFieldIds = collectCompatibleMasterFieldIds(elementIndex, scope);

    updatingFromModel = true;
    try {
      fieldCombo.getItems().setAll(masterFieldIds);
      fieldCombo.setValue(fieldGetter.get());
      String storedValue = valueGetter.get();
      // Stored null → no selection; stored "true" → display "true".
      // Any other stored value is also shown as DISPLAY_NO_VALUE for graceful degradation.
      valueCombo.setValue(storedValue == null ? null
          : (storedValue.equals("true") ? DISPLAY_TRUE : DISPLAY_NO_VALUE));
    } finally {
      updatingFromModel = false;
    }
  }

  private void commitChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  /**
   * Where in the Document Model to look for hide-condition master fields, mirroring the three outcomes of
   * the SME reference's {@code resolveDmElementForFmElement}/{@code getMasterFields}:
   * <ul>
   *   <li>{@link #root()} — no ancestor Repeat contains this node (a Section/Row/ControlGrid/Repeat directly
   *       under a Screen): candidates are collected from the whole Document Model, but only fields reachable
   *       through non-repeatable groups (fields nested in an unrelated repeatable group are ambiguous outside
   *       a repeat instance, so they're excluded) - matches SME's {@code getHiddenDmRootDocument()} fallback.</li>
   *   <li>{@link #anchoredOrUnbound(String, ElementIndex)} with a resolvable id — anchored at that Document
   *       Model element: for a Section/Row/ControlGrid/Repeat this is the group the nearest ancestor Repeat
   *       iterates over (its {@code groupRef}); for a Control this is the field it's bound to
   *       ({@code elementRef}). Candidates are the anchor's own fields, fields in non-repeatable subgroups
   *       anywhere, and fields along the anchor's own ancestor chain - not fields from unrelated repeat
   *       groups.</li>
   *   <li>{@link #anchoredOrUnbound(String, ElementIndex)} with a blank/dangling/unresolvable id (an
   *       unbound Control, or an ancestor Repeat whose {@code groupRef} doesn't resolve) - no candidates at
   *       all, matching SME's {@code if (!startElement) return [];}.</li>
   * </ul>
   */
  public static final class MasterFieldScope {

    private static final MasterFieldScope ROOT = new MasterFieldScope(null, false);
    private static final MasterFieldScope UNBOUND = new MasterFieldScope(null, true);

    private final @Nullable Element anchor;
    private final boolean unbound;

    private MasterFieldScope(@Nullable Element anchor, boolean unbound) {
      this.anchor = anchor;
      this.unbound = unbound;
    }

    public static MasterFieldScope root() {
      return ROOT;
    }

    public static MasterFieldScope anchoredOrUnbound(@Nullable String referencedElementId, @Nullable ElementIndex elementIndex) {
      if (referencedElementId == null || referencedElementId.isBlank() || elementIndex == null) {
        return UNBOUND;
      }
      return elementIndex.resolveElement(referencedElementId)
          .<MasterFieldScope>map(element -> new MasterFieldScope(element, false))
          .orElse(UNBOUND);
    }
  }

  private record CandidateField(FieldElement field, int distance) {
  }

  private static List<String> collectCompatibleMasterFieldIds(@Nullable ElementIndex elementIndex, @NonNull MasterFieldScope scope) {
    if (elementIndex == null || scope.unbound) {
      return List.of();
    }
    List<Element> anchorPath = ancestorChainIncludingSelf(scope.anchor, elementIndex);

    List<CandidateField> candidates = new ArrayList<>();
    for (Element element : elementIndex.allElements()) {
      if (element == scope.anchor || !(element instanceof FieldElement field) || field.getField() == null
          || !isCompatibleMasterFieldType(elementIndex, field)) {
        continue;
      }
      List<Element> fieldPath = ancestorChainIncludingSelf(field, elementIndex);
      if (!allAncestorsReachable(fieldPath, anchorPath)) {
        continue;
      }
      candidates.add(new CandidateField(field, calculateDistance(fieldPath, anchorPath)));
    }
    candidates.sort(Comparator.comparingInt(CandidateField::distance));

    List<String> ids = new ArrayList<>();
    for (CandidateField candidate : candidates) {
      ids.add(candidate.field.getId());
    }
    return ids;
  }

  // Boolean, Confirm and Enumeration fields, resolving TypeDefType indirection - mirrors SME's isCompatibleMasterField.
  private static boolean isCompatibleMasterFieldType(@NonNull ElementIndex elementIndex, @NonNull FieldElement field) {
    FieldType effectiveType = elementIndex.effectiveFieldType(field.getField().getFieldType());
    return effectiveType instanceof BooleanFieldType
        || effectiveType instanceof ConfirmFieldType
        || effectiveType instanceof EnumerationFieldType;
  }

  // A field is reachable from the anchor when every ancestor group on its path is either non-repeatable (always
  // traversable, since a non-repeating group has no ambiguous "which instance" question) or lies on the anchor's
  // own ancestor path (i.e. the field sits inside the same repeat context the anchor is in) - mirrors SME's
  // collectCompatibleFields recursion, which can only reach a field by successfully descending through every one
  // of its ancestor groups under that same rule.
  private static boolean allAncestorsReachable(@NonNull List<Element> fieldPath, @NonNull List<Element> anchorPath) {
    for (int i = 0; i < fieldPath.size() - 1; i++) {
      if (!(fieldPath.get(i) instanceof GroupElement group)) {
        continue;
      }
      boolean repeatable = group.getGroup() != null && group.getGroup().getRepeatability() != null
          && group.getGroup().getRepeatability() > 1;
      if (repeatable && !anchorPath.contains(group)) {
        return false;
      }
    }
    return true;
  }

  // Root-to-self chain of ancestor Elements, or an empty list for a null anchor (root scope).
  private static List<Element> ancestorChainIncludingSelf(@Nullable Element element, @NonNull ElementIndex elementIndex) {
    if (element == null) {
      return List.of();
    }
    Deque<Element> chain = new ArrayDeque<>();
    for (Element current = element; current != null; current = elementIndex.parentOf(current)) {
      chain.addFirst(current);
    }
    return new ArrayList<>(chain);
  }

  // Mirrors SME's calculateDistance: index of first divergence between the two root-to-self paths, scored so
  // that a shared ancestor closer to the field earns a smaller (nearer) distance.
  private static int calculateDistance(@NonNull List<Element> path1, @NonNull List<Element> path2) {
    for (int idx = 0; idx < path1.size(); idx++) {
      Element other = idx < path2.size() ? path2.get(idx) : null;
      if (path1.get(idx) != other) {
        return path1.size() + path2.size() - 2 * idx;
      }
    }
    return 0;
  }
}
