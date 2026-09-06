package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.HideCondition;
import de.a12.studio.models.formmodel.HideConditionCase;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits the "Hide Condition" property of a form node (Section, Row, ControlGrid, Repeat, Control):
 * a master Document Model field plus one or more trigger values, stored as {@link HideCondition}
 * ({@code masterField} + a list of {@link HideConditionCase}). The node is hidden whenever the master
 * field's current value matches any of the checked cases.
 * <ul>
 *   <li>{@code fieldCombo} — the id of a master field, offered from that model's Boolean/Confirm/Enumeration
 *       fields that are actually reachable from this node's position (see {@link MasterFieldScope}), mirroring
 *       the SME reference's {@code getMasterFields}/{@code collectCompatibleFields}.</li>
 *   <li>{@code valueList} — a checklist of the values that are actually possible for the selected master
 *       field's type: {@code "true"} + "(no value)" for Boolean/Confirm, or every declared enum literal +
 *       "(no value)" for an Enumeration field. Checking an item adds/removes a {@link HideConditionCase} with
 *       that value.</li>
 * </ul>
 * <p>
 * Does not extend {@link de.a12.studio.ui.editors.AbstractPropertyEditor} because it edits a
 * form-tree node (Section / Row / ControlGrid / Repeat / Control) rather than a document-model {@link Element}.
 * Saves are performed directly via {@link Studio#getSelectedProjectItem()} and
 * {@link StudioEventManager#fireModelSavedEvent}, matching the pattern used by
 * non-element panels such as {@link de.a12.studio.ui.editors.formmodel.StylesPanelController}.
 * <p>
 * Call {@link #configure} once per node selection to bind this panel to the node's hide-condition
 * getter/setter and repopulate the field combo for the given {@link MasterFieldScope}.
 */
public class HideConditionPanelController implements Initializable {

  /** Display label for the synthetic "no value" case (stored as a {@code null} masterValue). */
  public static final String DISPLAY_NO_VALUE = "(no value)";
  public static final String DISPLAY_TRUE = "true";

  @FXML
  private ComboBox<String> fieldCombo;

  @FXML
  private ListView<String> valueList;

  // Guards programmatic repopulation in configure()/rebuildValueList() from being treated as user edits.
  private boolean updatingFromModel;

  private Supplier<HideCondition> getter;
  private Consumer<HideCondition> setter;
  private @Nullable ElementIndex elementIndex;

  // display value -> stored masterValue (null for "no value")
  private final Map<String, String> displayToStoredValue = new LinkedHashMap<>();
  private final Map<String, BooleanProperty> checkedProperties = new LinkedHashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    valueList.setCellFactory(CheckBoxListCell.forListView(this::checkedPropertyFor));

    fieldCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel || setter == null) {
        return;
      }
      String fieldValue = (newVal == null || newVal.isBlank()) ? null : newVal;
      if (fieldValue == null) {
        setter.accept(null);
      } else {
        HideCondition condition = new HideCondition();
        condition.setMasterField(fieldValue);
        setter.accept(condition);
      }
      commitChange();
      rebuildValueList();
    });
  }

  /**
   * Binds this panel to the hide-condition property of a form node and repopulates the field combo from
   * {@code scope}. Must be called every time a new node is selected in the form tree.
   *
   * @param getter       reads the node's {@link HideCondition} (may be {@code null})
   * @param setter       writes the node's {@link HideCondition} (may be called with {@code null})
   * @param elementIndex index over the Document Model linked to the form, or {@code null} if none is linked
   * @param scope        where in the Document Model to look for candidate master fields, see {@link MasterFieldScope}
   */
  public void configure(
      @NonNull Supplier<HideCondition> getter,
      @NonNull Consumer<HideCondition> setter,
      @Nullable ElementIndex elementIndex,
      @NonNull MasterFieldScope scope) {

    this.getter = getter;
    this.setter = setter;
    this.elementIndex = elementIndex;

    List<String> masterFieldIds = collectMasterFieldIds(elementIndex, scope, HideConditionPanelController::isCompatibleMasterFieldType);

    updatingFromModel = true;
    try {
      fieldCombo.getItems().setAll(masterFieldIds);
      HideCondition current = getter.get();
      fieldCombo.setValue(current == null ? null : current.getMasterField());
    } finally {
      updatingFromModel = false;
    }
    rebuildValueList();
  }

  private BooleanProperty checkedPropertyFor(String display) {
    return checkedProperties.computeIfAbsent(display, d -> {
      BooleanProperty property = new SimpleBooleanProperty(isChecked(d));
      property.addListener((obs, oldVal, newVal) -> {
        if (!updatingFromModel) {
          onValueToggled(d, newVal);
        }
      });
      return property;
    });
  }

  private boolean isChecked(String display) {
    HideCondition condition = getter == null ? null : getter.get();
    if (condition == null) {
      return false;
    }
    String stored = displayToStoredValue.get(display);
    for (HideConditionCase c : condition.getCases()) {
      if (valuesEqual(c.getMasterValue(), stored)) {
        return true;
      }
    }
    return false;
  }

  private void onValueToggled(String display, boolean checked) {
    HideCondition condition = getter.get();
    if (condition == null) {
      // Should not normally happen (a value can only be checked once a master field is selected), but guard
      // against it defensively by creating one anchored to the currently selected field.
      condition = new HideCondition();
      condition.setMasterField(fieldCombo.getValue());
      setter.accept(condition);
    }
    String stored = displayToStoredValue.get(display);
    if (checked) {
      boolean alreadyPresent = condition.getCases().stream()
          .anyMatch(c -> valuesEqual(c.getMasterValue(), stored));
      if (!alreadyPresent) {
        HideConditionCase newCase = new HideConditionCase();
        newCase.setMasterValue(stored);
        condition.getCases().add(newCase);
      }
    } else {
      condition.getCases().removeIf(c -> valuesEqual(c.getMasterValue(), stored));
    }
    commitChange();
  }

  private static boolean valuesEqual(@Nullable String a, @Nullable String b) {
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  /** Repopulates {@code valueList} with the values possible for the currently selected master field's type. */
  private void rebuildValueList() {
    displayToStoredValue.clear();
    checkedProperties.clear();

    HideCondition current = getter == null ? null : getter.get();
    String masterFieldId = current == null ? null : current.getMasterField();

    if (masterFieldId != null && elementIndex != null) {
      ElementIndex index = elementIndex;
      index.resolveElement(masterFieldId)
          .filter(FieldElement.class::isInstance)
          .map(FieldElement.class::cast)
          .ifPresent(field -> populateDisplayValues(field, index));
    }

    updatingFromModel = true;
    try {
      valueList.getItems().setAll(displayToStoredValue.keySet());
    } finally {
      updatingFromModel = false;
    }
  }

  private void populateDisplayValues(@NonNull FieldElement field, @NonNull ElementIndex index) {
    if (field.getField() == null) {
      return;
    }
    FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
    if (effectiveType instanceof EnumerationFieldType enumType) {
      if (enumType.getEnumerationType() != null) {
        for (EnumerationValue value : enumType.getEnumerationType().getValues()) {
          displayToStoredValue.put(value.getValue(), value.getValue());
        }
      }
      displayToStoredValue.put(DISPLAY_NO_VALUE, null);
    } else if (effectiveType instanceof BooleanFieldType || effectiveType instanceof ConfirmFieldType) {
      displayToStoredValue.put(DISPLAY_TRUE, "true");
      displayToStoredValue.put(DISPLAY_NO_VALUE, null);
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

  /**
   * Collects the ids of every Document Model field reachable from {@code scope} whose effective type matches
   * {@code typeFilter}, ordered nearest-first. Shared with other master-field pickers (e.g.
   * {@link DependentEnumerationPanelController}) that need the same reachability algorithm but a different
   * set of acceptable field types.
   */
  static List<String> collectMasterFieldIds(@Nullable ElementIndex elementIndex, @NonNull MasterFieldScope scope,
      @NonNull BiPredicate<ElementIndex, FieldElement> typeFilter) {
    if (elementIndex == null || scope.unbound) {
      return List.of();
    }
    List<Element> anchorPath = ancestorChainIncludingSelf(scope.anchor, elementIndex);

    List<CandidateField> candidates = new ArrayList<>();
    for (Element element : elementIndex.allElements()) {
      if (element == scope.anchor || !(element instanceof FieldElement field) || field.getField() == null
          || !typeFilter.test(elementIndex, field)) {
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
