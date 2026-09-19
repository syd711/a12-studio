package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.ComputationAlternative;
import de.a12.studio.models.documentmodel.ComputationConfig;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.ContentUniquenessCriterion;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.RuleConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.modelsvalidation.refactoring.PathLocator.Region;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Keeps the path-based references <em>inside one Document Model</em> valid while an element is renamed or moved -
 * the a12-studio counterpart of SME's {@code moveElementApi.ts} / backend {@code MoveRefactoringService}, which
 * delegates to the kernel's {@code MoveSupportDM.changePathReferencesForMove}.
 *
 * <p>Usage is two-phase, around whatever performs the structural change (see the UI's {@code RefactoringCommand}):
 * <pre>
 *   Plan plan = DocumentModelRefactoring.prepare(model);   // tree still in its OLD shape
 *   ... rename / move the element ...
 *   List&lt;Edit&gt; edits = plan.computeEdits();               // tree now in its NEW shape
 *   edits.forEach(Edit::apply);                            // and Edit::revert to undo
 * </pre>
 *
 * <p>Unlike SME's "old path to new path" map, {@link #prepare} resolves every reference to the <em>element</em> it
 * points at while the old tree is still intact, and {@link Plan#computeEdits} re-checks it against the new tree.
 * That is what makes a moved <em>rule</em> work too: its relative paths are measured from its own (new) position,
 * so they have to change even though the fields they point at didn't move.
 *
 * <p>A reference is rewritten <em>only if its original spelling no longer resolves to the same element</em>, so
 * whatever is still correct - including unusual but valid spellings - is never reformatted. A reference that does
 * need rewriting keeps its style: absolute stays absolute, a {@code ..Group} turning-group name and the {@code *}
 * repetition markers survive.
 *
 * <p><b>Covered references</b> (all in the same model): Rule {@code errorEntityRelPath}, {@code errorCondition} and
 * the paths in its {@code errorMessage} {@code $...$} parameters; Computation {@code computedFieldRelPath},
 * {@code commonPrecondition}, every alternative's {@code precondition}/{@code operation} and its {@code errorMessage};
 * a Group's {@code indexFieldName}; the {@code fullName}s of {@code documentUniquenessCriteria}. References by
 * element id (uniqueness criteria on {@code ModelConfig}, and every reference from Form/Overview/Tree/... models)
 * are immune to renames and moves by construction.
 *
 * <p><b>Other models</b>: the references other project models hold on this one are handled by {@link
 * ProjectReferenceRefactoring}, which builds on {@link Plan#pathRewriter()} (absolute paths in Print/Query/Mapping/
 * Selection/Structural Mapping models) and {@link Plan#computeEdits(IncludedModelChange)} (rules and computations of a
 * Document Model that includes the changed one).
 *
 * <p><b>Not covered</b>: a path that reaches an element only through an Additive base model, or through a chain of
 * more than one Include; a condition that doesn't parse (skipped, see {@link Plan#skippedSites()}).
 */
public final class DocumentModelRefactoring {

  private static final Logger log = LoggerFactory.getLogger(DocumentModelRefactoring.class);

  private static final Set<String> CONSTANTS = Set.of("RuleGroup", "BaseYear", "FirstDay", "LastDay");

  private DocumentModelRefactoring() {
  }

  /**
   * How a Document Model that <em>includes</em> another one has to follow a change made in that other model: an
   * Include group mounts the children of the included model's root group, so a condition path such as {@code
   * Address/Street} in the including model ends in the included model's own element names, which the change may
   * have renamed or moved. {@code modelId} is the id of the included (changed) model.
   */
  public record IncludedModelChange(String modelId, PathRewriter rewriter) {
  }

  /** One reversible text replacement; {@link #apply} sets the new value, {@link #revert} restores the old one. */
  public record Edit(Consumer<String> setter, String oldValue, String newValue) {

    public void apply() {
      setter.accept(newValue);
    }

    public void revert() {
      setter.accept(oldValue);
    }
  }

  /**
   * Captures every reference of {@code model}'s <em>current</em> tree. Call before the structural change; then
   * call {@link Plan#computeEdits()} after it.
   */
  public static Plan prepare(DocumentModel model) {
    Tree before = new Tree(model);
    List<Site> sites = new ArrayList<>();

    for (Element element : before.index.allElements()) {
      if (element instanceof RuleElement rule && rule.getRule() != null) {
        collectRule(before, sites, rule);
      }
      else if (element instanceof ComputationElement computation && computation.getComputation() != null) {
        collectComputation(before, sites, computation);
      }
      else if (element instanceof GroupElement group && group.getGroup() != null) {
        addSite(before, sites, Mode.RELATIVE_PATH, group, group.getGroup()::getIndexFieldName,
            group.getGroup()::setIndexFieldName);
      }
    }
    if (model.getContent() != null && model.getContent().getDocumentUniquenessCriteria() != null) {
      for (ContentUniquenessCriterion criterion : model.getContent().getDocumentUniquenessCriteria()) {
        if (criterion.getFields() == null) {
          continue;
        }
        for (ContentUniquenessCriterion.Field field : criterion.getFields()) {
          addSite(before, sites, Mode.ABSOLUTE_PATH, null, field::getFullName, field::setFullName);
        }
      }
    }
    return new Plan(model, sites, before);
  }

  private static void collectRule(Tree tree, List<Site> sites, RuleElement rule) {
    RuleConfig config = rule.getRule();
    addSite(tree, sites, Mode.RELATIVE_PATH, rule, config::getErrorEntityRelPath, config::setErrorEntityRelPath);
    addSite(tree, sites, Mode.CONDITION, rule, config::getErrorCondition, config::setErrorCondition);
    addMessageSites(tree, sites, rule, config.getErrorMessage());
  }

  private static void collectComputation(Tree tree, List<Site> sites, ComputationElement computation) {
    ComputationConfig config = computation.getComputation();
    addSite(tree, sites, Mode.RELATIVE_PATH, computation, config::getComputedFieldRelPath, config::setComputedFieldRelPath);
    addSite(tree, sites, Mode.CONDITION, computation, config::getCommonPrecondition, config::setCommonPrecondition);
    if (config.getComputationAlternatives() != null) {
      for (ComputationAlternative alternative : config.getComputationAlternatives()) {
        addSite(tree, sites, Mode.CONDITION, computation, alternative::getPrecondition, alternative::setPrecondition);
        addSite(tree, sites, Mode.CONDITION, computation, alternative::getOperation, alternative::setOperation);
      }
    }
    addMessageSites(tree, sites, computation, config.getErrorMessage());
  }

  private static void addMessageSites(Tree tree, List<Site> sites, Element owner, List<Label> messages) {
    if (messages != null) {
      for (Label label : messages) {
        addSite(tree, sites, Mode.MESSAGE, owner, label::getText, label::setText);
      }
    }
  }

  private static void addSite(Tree tree, List<Site> sites, Mode mode, Element owner, Supplier<String> getter,
      Consumer<String> setter) {
    String text = getter.get();
    if (text == null || text.isBlank()) {
      return;
    }
    List<Ref> refs = locate(tree, mode, baseOf(mode, owner, tree), text);
    if (refs == null) {
      log.warn("Reference refactoring skips a {} it cannot parse: {}", mode, text);
    }
    sites.add(new Site(mode, owner, getter, setter, text, refs));
  }

  /**
   * The element {@code mode}'s relative paths start from, in {@code tree}. Derived from the owner each time rather
   * than remembered: when the owning rule is what moves, its Rule Group is a different one afterwards.
   */
  private static Element baseOf(Mode mode, Element owner, Tree tree) {
    return switch (mode) {
      case CONDITION, MESSAGE -> tree.index.parentOf(owner);
      case RELATIVE_PATH -> owner;
      case ABSOLUTE_PATH -> null;
    };
  }

  /** The paths of {@code text} as they resolve in {@code tree}; null if {@code text} can't be parsed. */
  private static List<Ref> locate(Tree tree, Mode mode, Element base, String text) {
    List<Region> regions;
    switch (mode) {
      case CONDITION -> {
        Optional<List<Region>> located = PathLocator.inCondition(text);
        if (located.isEmpty()) {
          return null;
        }
        regions = located.get();
      }
      case MESSAGE -> regions = PathLocator.inMessage(text);
      default -> regions = List.of(new Region(0, text.length()));
    }
    List<Ref> refs = new ArrayList<>();
    for (Region region : regions) {
      String pathText = text.substring(region.start(), region.end());
      Optional<PathText> path = PathText.parse(pathText);
      if (path.isEmpty()) {
        continue;
      }
      if (CONSTANTS.contains(pathText.strip())) {
        if (base != null) {
          refs.add(new Ref(region, path.get(), new Resolution(base, List.of(), null, Map.of()), true));
        }
        continue;
      }
      resolve(path.get(), base, tree).ifPresent(resolution -> refs.add(new Ref(region, path.get(), resolution, false)));
    }
    return refs;
  }

  /**
   * Resolves {@code path} - relative to {@code base}, or from the model root if it is absolute - to the deepest
   * element of {@code tree} it reaches. Empty if it doesn't even reach a root group, or climbs above the root.
   */
  private static Optional<Resolution> resolve(PathText path, Element base, Tree tree) {
    // The path as a stack of names from the model root, popped by ".." and pushed by every named segment.
    List<Step> stack = new ArrayList<>();
    if (!path.absolute()) {
      if (base == null) {
        return Optional.empty();
      }
      for (String name : tree.index.getPath(base).substring(1).split("/")) {
        stack.add(new Step(name, false, name));
      }
    }
    String turningGroupName = null;
    for (PathText.Segment segment : path.segments()) {
      if (segment.up()) {
        if (stack.isEmpty()) {
          return Optional.empty();
        }
        stack.remove(stack.size() - 1);
        turningGroupName = segment.name();
      }
      else {
        stack.add(new Step(segment.name(), segment.star(), segment.text()));
      }
    }

    // The deepest prefix that is an element of this model; the rest (e.g. the part that lives inside an Include
    // group's own model) is carried along unchanged.
    for (int depth = stack.size(); depth >= 1; depth--) {
      Element target = tree.byPath.get(pathOf(stack, depth));
      if (target == null) {
        continue;
      }
      Map<Element, Boolean> starred = new IdentityHashMap<>();
      for (int i = 1; i <= depth; i++) {
        if (stack.get(i - 1).star()) {
          starred.put(tree.byPath.get(pathOf(stack, i)), true);
        }
      }
      List<String> tail = new ArrayList<>();
      for (int i = depth; i < stack.size(); i++) {
        tail.add(stack.get(i).text());
      }
      return Optional.of(new Resolution(target, tail, turningGroupName, starred));
    }
    return Optional.empty();
  }

  private static String pathOf(List<Step> stack, int depth) {
    StringBuilder path = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      path.append('/').append(stack.get(i).name());
    }
    return path.toString();
  }

  /**
   * Writes the reference that was {@code old} the way it is written in {@code tree}, keeping its style; null if
   * that isn't possible (the target or base isn't in that tree, or the path would come out empty).
   */
  private static String render(Mode mode, Element base, PathText path, Resolution old, Tree tree) {
    ElementIndex index = tree.index;
    if (!tree.present.contains(old.target())) {
      return null;
    }
    if (path.absolute()) {
      return join(List.of(index.getPath(old.target())), old.tail());
    }
    if (base == null || !tree.present.contains(base)) {
      return null;
    }

    String relative = index.relativePathTo(base, old.target());
    List<String> parts = relative.isEmpty() ? List.of() : List.of(relative.split("/"));
    int ups = 0;
    while (ups < parts.size() && parts.get(ups).equals("..")) {
      ups++;
    }
    List<String> rendered = new ArrayList<>(parts.subList(0, ups));
    if (old.turningGroupName() != null && ups > 0) {
      Element reached = base;
      for (int i = 0; i < ups && reached != null; i++) {
        reached = index.parentOf(reached);
      }
      if (reached != null) {
        rendered.set(ups - 1, PathText.Segment.up(reached.getName()).text());
      }
    }

    // The downward part names the chain of ancestors of the target, so each name can keep its own "*" marker.
    List<Element> chain = new ArrayList<>();
    for (Element element = old.target(); element != null; element = index.parentOf(element)) {
      chain.add(element);
    }
    Collections.reverse(chain);
    for (Element element : chain.subList(chain.size() - (parts.size() - ups), chain.size())) {
      boolean star = old.starred().getOrDefault(element, false);
      rendered.add(PathText.Segment.down(element.getName(), false, star).text());
    }

    if (rendered.isEmpty() && old.tail().isEmpty()) {
      // The path names the base itself. The condition language (and the "$#RuleGroup$" message parameter) has a
      // spelling for the Rule Group; a relative-path property measured from the element itself does not.
      return mode == Mode.CONDITION || mode == Mode.MESSAGE ? "RuleGroup" : null;
    }
    return join(rendered, old.tail());
  }

  private static String join(List<String> head, List<String> tail) {
    List<String> all = new ArrayList<>(head);
    all.addAll(tail);
    return String.join("/", all);
  }

  /** How a text is interpreted, and so where its relative paths start from. */
  private enum Mode {
    /** A Rule/Computation condition: paths start at the rule's parent group ("Rule Group"). */
    CONDITION,
    /** An error message: {@code $...$} parameters, paths start at the Rule Group like in the condition. */
    MESSAGE,
    /** The whole text is one relative path measured from {@code base} itself (e.g. {@code ../fieldName}). */
    RELATIVE_PATH,
    /** The whole text is one absolute path ({@code /Root/Group/Field}). */
    ABSOLUTE_PATH
  }

  /** One state of the model's element tree, indexed for path lookups. */
  private static final class Tree {

    final ElementIndex index;
    final Map<String, Element> byPath = new HashMap<>();
    // Each element's path as it was when this tree was taken: ElementIndex.getPath() reads the element names as they
    // are now, so it can't tell an old tree's paths from a new one's once something has been renamed.
    final Map<Element, String> pathOf = new IdentityHashMap<>();
    final Set<Element> present = Collections.newSetFromMap(new IdentityHashMap<>());

    Tree(DocumentModel model) {
      this.index = new ElementIndex(model);
      for (Element element : index.allElements()) {
        String path = index.getPath(element);
        byPath.put(path, element);
        pathOf.put(element, path);
        present.add(element);
      }
    }
  }

  private record Step(String name, boolean star, String text) {
  }

  /**
   * What a path spelling points at in one particular tree: the deepest element of this model it reaches
   * ({@code target}), whatever trailing segments didn't resolve ({@code tail} - e.g. the part inside an Include
   * group's own model), and how it was spelled ({@code turningGroupName}, {@code starred} repetition markers).
   */
  private record Resolution(Element target, List<String> tail, String turningGroupName, Map<Element, Boolean> starred) {

    boolean samePointAs(Resolution other) {
      return target == other.target && tail.equals(other.tail);
    }
  }

  /**
   * A path written down somewhere: where in its text, how it was spelled, and what it pointed at in the old tree.
   * {@code constant} marks the condition language's {@code RuleGroup} (and the like), which always means "my own
   * Rule Group" and so is never rewritten - it just follows the rule around.
   */
  private record Ref(Region region, PathText path, Resolution old, boolean constant) {
  }

  /**
   * A property that holds text with paths in it, with the paths it contained when {@link #prepare} ran. {@code
   * owner} is the element that carries the property (the rule, computation or group; null for a model-level one).
   */
  private record Site(Mode mode, Element owner, Supplier<String> getter, Consumer<String> setter, String text,
                      List<Ref> refs) {
  }

  /** One reference as seen by {@link Plan#resolvedTargets()}; {@code constant} is a {@code RuleGroup}-style keyword. */
  record Entry(Element target, List<String> tail, boolean constant) {
  }

  /**
   * Whether {@code after} holds the same references as {@code before}: each one still points at the same element
   * (and tail), except that a constant such as {@code RuleGroup} has to stay one - it follows its rule to a new group
   * by design - and that a path which now reaches the rule's own group is legitimately spelled {@code RuleGroup}.
   */
  static boolean sameReferences(Map<String, List<Entry>> before, Map<String, List<Entry>> after) {
    if (!before.keySet().equals(after.keySet())) {
      return false;
    }
    for (Map.Entry<String, List<Entry>> site : before.entrySet()) {
      List<Entry> was = site.getValue();
      List<Entry> is = after.get(site.getKey());
      if (was.size() != is.size()) {
        return false;
      }
      for (int i = 0; i < was.size(); i++) {
        boolean same = was.get(i).constant()
            ? is.get(i).constant()
            : was.get(i).target() == is.get(i).target() && was.get(i).tail().equals(is.get(i).tail());
        if (!same) {
          return false;
        }
      }
    }
    return true;
  }

  /** The references of one model, ready to be re-checked once the model's structure has changed. */
  public static final class Plan {

    private final DocumentModel model;
    private final List<Site> sites;
    private final Tree before;
    private int skippedSites;

    private Plan(DocumentModel model, List<Site> sites, Tree before) {
      this.model = model;
      this.sites = sites;
      this.before = before;
    }

    /**
     * Rewrites paths <em>into this model</em> that are held elsewhere, as they read after the structural change
     * that just happened - see {@link PathRewriter}. Call after the change.
     */
    public PathRewriter pathRewriter() {
      return new PathRewriter(before, new Tree(model));
    }

    /**
     * The edits that bring every captured reference in line with the model's current (post-change) structure. A
     * reference that still resolves to what it did, or whose target or base is gone, yields no edit.
     */
    public List<Edit> computeEdits() {
      return computeEdits(null);
    }

    /**
     * {@link #computeEdits()} for a model that includes another one which has just changed: a reference that ends in
     * elements of the included model is expected to follow their new names and positions, see {@link
     * IncludedModelChange}. {@code included} may be null.
     */
    public List<Edit> computeEdits(IncludedModelChange included) {
      Tree after = new Tree(model);
      List<Edit> edits = new ArrayList<>();
      skippedSites = 0;

      for (Site site : sites) {
        if (site.refs() == null) {
          skippedSites++;
          continue;
        }
        if (!site.text().equals(site.getter().get())) {
          continue;
        }
        Element base = baseOf(site.mode(), site.owner(), after);
        Map<Region, String> replacements = new HashMap<>();
        for (Ref ref : site.refs()) {
          if (ref.constant()) {
            continue;
          }
          Resolution expected = followIncludedChange(ref.old(), included);
          Optional<Resolution> now = resolve(ref.path(), base, after);
          if (now.isPresent() && now.get().samePointAs(expected)) {
            continue;
          }
          String rendered = render(site.mode(), base, ref.path(), expected, after);
          if (rendered != null) {
            replacements.put(ref.region(), rendered);
          }
        }
        if (replacements.isEmpty()) {
          continue;
        }
        String updated = replace(site.text(), replacements);
        if (!updated.equals(site.text())) {
          edits.add(new Edit(site.setter(), site.text(), updated));
        }
      }
      if (!edits.isEmpty()) {
        log.debug("Reference refactoring rewrites {} text(s) of {}", edits.size(), model.getId());
      }
      return edits;
    }

    /** How many texts holding paths couldn't be parsed, hence weren't rewritten; valid after {@link #computeEdits}. */
    public int skippedSites() {
      return skippedSites;
    }

    /**
     * What every captured reference points at, keyed by the owning element's id, the kind of text and its occurrence
     * within that owner - so the result doesn't depend on where in the tree an owner sits. Compare two snapshots
     * with {@link #sameReferences}.
     */
    Map<String, List<Entry>> resolvedTargets() {
      Map<String, List<Entry>> targets = new HashMap<>();
      Map<String, Integer> occurrences = new HashMap<>();
      for (Site site : sites) {
        String kind = (site.owner() == null ? "model" : site.owner().getId()) + "|" + site.mode();
        String key = kind + "|" + occurrences.merge(kind, 1, Integer::sum);
        List<Entry> entries = new ArrayList<>();
        if (site.refs() == null) {
          entries.add(new Entry(null, List.of(), false));
        }
        else {
          for (Ref ref : site.refs()) {
            entries.add(new Entry(ref.old().target(), ref.old().tail(), ref.constant()));
          }
        }
        targets.put(key, entries);
      }
      return targets;
    }

    /** {@code old}, with the part of its tail that lies inside the changed included model brought up to date. */
    private static Resolution followIncludedChange(Resolution old, IncludedModelChange included) {
      if (included == null || old.tail().isEmpty()
          || !(old.target() instanceof GroupElement group) || group.getGroup() == null
          || group.getGroup().getIncludeConfig() == null
          || !included.modelId().equals(group.getGroup().getIncludeConfig().getReference())) {
        return old;
      }
      List<String> tail = included.rewriter().rewriteIncludedTail(old.tail());
      return tail.equals(old.tail()) ? old : new Resolution(old.target(), tail, old.turningGroupName(), old.starred());
    }

    private static String replace(String text, Map<Region, String> replacements) {
      List<Region> regions = new ArrayList<>(replacements.keySet());
      regions.sort(Comparator.comparingInt(Region::start).reversed());
      StringBuilder result = new StringBuilder(text);
      for (Region region : regions) {
        result.replace(region.start(), region.end(), replacements.get(region));
      }
      return result.toString();
    }
  }

  /**
   * Maps a path into the model as it was <em>before</em> a structural change to how it reads <em>after</em> it:
   * every path is resolved to the element it named in the old tree and written again from that element's new
   * position, so a rename or move of the element - or of any group above it - is followed. A path that still reads
   * the same, doesn't resolve, or can't be parsed is returned untouched (never reformatted).
   */
  public static final class PathRewriter {

    private final Tree before;
    private final Tree after;

    private PathRewriter(Tree before, Tree after) {
      this.before = before;
      this.after = after;
    }

    /**
     * {@code path} in the form the JSON models hold it - absolute ({@code /Root/Group/Field}), optionally ending in
     * {@code /} (a group and everything below it) or {@code /*} (a wildcard segment); that ending is kept. Any part
     * of the path that lies beyond this model's own elements (inside an Include's model) is carried along unchanged.
     */
    public String rewriteAbsolute(String path) {
      if (path == null || path.isBlank()) {
        return path;
      }
      String suffix = "";
      String core = path;
      if (core.endsWith("/*")) {
        suffix = "/*";
        core = core.substring(0, core.length() - 2);
      }
      else if (core.endsWith("/")) {
        suffix = "/";
        core = core.substring(0, core.length() - 1);
      }
      Optional<PathText> parsed = PathText.parse(core);
      if (parsed.isEmpty() || !parsed.get().absolute()) {
        return path;
      }
      Optional<Resolution> resolution = resolve(parsed.get(), null, before);
      if (resolution.isEmpty() || !after.present.contains(resolution.get().target())) {
        return path;
      }
      Element target = resolution.get().target();
      String newPath = after.pathOf.get(target);
      if (newPath.equals(before.pathOf.get(target))) {
        return path;
      }
      return join(List.of(newPath), resolution.get().tail()) + suffix;
    }

    /**
     * {@code tail} - path segments below an Include group that name elements of the included model, that is
     * children of one of its root groups - as they read after the change. The root group's own name is not part of
     * such a path (the Include group stands in for it), so renaming the root leaves every tail alone.
     */
    List<String> rewriteIncludedTail(List<String> tail) {
      List<PathText.Segment> segments = new ArrayList<>();
      for (String text : tail) {
        Optional<PathText> single = PathText.parse(text);
        if (single.isEmpty() || single.get().absolute() || single.get().segments().size() != 1
            || single.get().segments().get(0).up()) {
          return tail;
        }
        segments.add(single.get().segments().get(0));
      }
      List<GroupElement> rootGroups = before.index.getModel().getContent().getModelRoot().getRootGroups();
      if (rootGroups == null) {
        return tail;
      }
      for (GroupElement root : rootGroups) {
        List<Step> stack = new ArrayList<>();
        stack.add(new Step(root.getName(), false, root.getName()));
        for (PathText.Segment segment : segments) {
          stack.add(new Step(segment.name(), segment.star(), segment.text()));
        }
        for (int depth = stack.size(); depth >= 2; depth--) {
          Element target = before.byPath.get(pathOf(stack, depth));
          if (target == null) {
            continue;
          }
          if (!after.present.contains(target) || after.pathOf.get(target).equals(before.pathOf.get(target))) {
            return tail;
          }
          // How each element on the way to the target was spelled (quotes, "*"), to spell it the same way again.
          Map<Element, PathText.Segment> written = new IdentityHashMap<>();
          Element cursor = target;
          for (int i = depth - 1; i >= 1; i--) {
            written.put(cursor, segments.get(i - 1));
            cursor = before.index.parentOf(cursor);
          }
          List<Element> chain = new ArrayList<>();
          for (Element element = target; element != null; element = after.index.parentOf(element)) {
            chain.add(element);
          }
          Collections.reverse(chain);
          List<String> rewritten = new ArrayList<>();
          for (Element element : chain.subList(1, chain.size())) {
            PathText.Segment old = written.get(element);
            rewritten.add(PathText.Segment.down(element.getName(), old != null && old.quoted(), old != null && old.star())
                .text());
          }
          for (int i = depth - 1; i < segments.size(); i++) {
            rewritten.add(segments.get(i).text());
          }
          return rewritten;
        }
      }
      return tail;
    }
  }
}
