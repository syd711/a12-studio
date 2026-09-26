package de.a12.studio.models.contentmodel;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * One element type of the Content Engine editor's element library: what SME's {@code EditorElementModule} declares
 * about it besides the settings panel. The parent and child rules decide where the type may be inserted, see
 * {@link ContentInsertion}.
 *
 * @param label                 name shown to the user
 * @param category              group heading in the insert dialog ("Layout", "Content", "General", "Form Elements")
 * @param keepTransitiveChildren the Repeatable Group / Conditional children of this type are matched by the child
 *                              rule as themselves instead of being looked through (SME's
 *                              {@code hasRelationshipWithTransitiveNode}, only the table body)
 */
public record ContentModule(@NonNull String namespace, @NonNull String type, @NonNull String label,
                            @NonNull String category, @NonNull ParentRule parentRule, @NonNull ChildRule childRule,
                            boolean keepTransitiveChildren) {

  /** Matches every module in a rule. */
  public static final String ANY = "*";

  /** The id rules refer to a module by, e.g. {@code com.mgmtp.a12.contentengine/Box}. */
  public String id() {
    return moduleId(namespace, type);
  }

  public static String moduleId(String namespace, String type) {
    return namespace + "/" + type;
  }

  /** Which parents the type may have: only the listed ones ({@code allow}), or any but the listed ones. */
  public record ParentRule(boolean allow, @NonNull List<String> ids) {

    public static ParentRule anyOf(String... ids) {
      return new ParentRule(true, List.of(ids));
    }

    public static ParentRule noneOf(String... ids) {
      return new ParentRule(false, List.of(ids));
    }

    boolean matches(String parentId) {
      boolean listed = ids.contains(ANY) || ids.contains(parentId);
      return allow == listed;
    }
  }

  /** A rule for one child type, with optional instance limits and a rule for that child's own children. */
  public record ModuleRule(@NonNull String id, @Nullable Integer min, @Nullable Integer max,
                           @Nullable ChildRule childrenRule) {

    public static ModuleRule of(String id) {
      return new ModuleRule(id, null, null, null);
    }

    public static ModuleRule of(String id, Integer min, Integer max) {
      return new ModuleRule(id, min, max, null);
    }
  }

  /** Which children the type may have. */
  public sealed interface ChildRule {

    /** Any number of the listed child types; the single rule {@link #ANY} allows every type. */
    static ChildRule anyOf(ModuleRule... rules) {
      return new AnyOf(List.of(rules));
    }

    /** Any one of several sequences. */
    static ChildRule anyOfSequences(Sequence... sequences) {
      return new AnyOfSequences(List.of(sequences));
    }

    static ChildRule sequence(ModuleRule... rules) {
      return new Sequence(List.of(rules));
    }

    /** No children of the listed types; {@link #ANY} forbids children altogether. */
    static ChildRule noneOf(String... ids) {
      return new NoneOf(List.of(ids));
    }

    static ChildRule noChildren() {
      return noneOf(ANY);
    }

    static ChildRule anyChildren() {
      return anyOf(ModuleRule.of(ANY));
    }
  }

  public record AnyOf(@NonNull List<ModuleRule> rules) implements ChildRule {
  }

  public record AnyOfSequences(@NonNull List<Sequence> sequences) implements ChildRule {
  }

  /** The children must be runs of the listed types in exactly this order. */
  public record Sequence(@NonNull List<ModuleRule> rules) implements ChildRule {
  }

  public record NoneOf(@NonNull List<String> ids) implements ChildRule {
  }
}
