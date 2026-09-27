package de.a12.studio.models.treemodel;

import de.a12.studio.models.relationshipmodel.RelationshipModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The events the Tree Engine knows, by where they can be used (the lists of SME's tree editor, {@code tmEvents.ts}
 * plus what the 13.0.2 client adds). The copy/paste events are only offered - and only valid - while none of the tree's
 * relationships has a link Document Model.
 */
public final class TreeEvents {

  /** Where an event is attached; each place has its own list. */
  public enum Context {
    /** A node's row actions and its context menu actions. */
    ROW,
    /** The Subheader, the Footer and the Virtual Root's actions. */
    HEADER,
    /** The buttons of the multi-selection panel. */
    MULTI_SELECTION,
    /** A node's row activation (what a click on the row does). */
    ROW_ACTIVATION
  }

  public static final List<String> ROW_EVENTS = List.of("event_add_link", "event_delete_link", "event_delete_node",
      "event_expand_sub_tree", "event_collapse_sub_tree", "event_open_node");
  public static final List<String> ROW_COPY_PASTE_EVENTS = List.of("event_copy_node", "event_copy_node_and_children",
      "event_cut_node", "event_paste", "event_paste_above", "event_paste_below");

  public static final List<String> HEADER_EVENTS = List.of("event_add_root_node", "event_expand_whole_tree",
      "event_collapse_whole_tree");
  public static final List<String> HEADER_COPY_PASTE_EVENTS = List.of("event_paste");

  public static final List<String> MULTI_SELECTION_EVENTS = List.of("event_delete_nodes");
  public static final List<String> MULTI_SELECTION_COPY_PASTE_EVENTS = List.of("event_copy_nodes", "event_cut_nodes",
      "event_paste");

  /** What only a row activation can fire, besides the row events. */
  public static final List<String> ROW_ACTIVATION_EVENTS = List.of("event_open_node", "event_toggle_expansion");

  private TreeEvents() {
  }

  /** The events offered for {@code context}; the copy/paste ones only when the tree has no link Document Model. */
  public static List<String> candidates(Context context, boolean hasLinkDocumentModel) {
    List<String> events = new ArrayList<>(plain(context));
    if (!hasLinkDocumentModel) {
      events.addAll(copyPaste(context));
    }
    if (context == Context.ROW_ACTIVATION && !events.contains("event_toggle_expansion")) {
      events.add("event_toggle_expansion");
    }
    return events;
  }

  /** Whether {@code event} is one of the copy/paste events of {@code context} (not allowed with a link Document Model). */
  public static boolean isCopyPaste(Context context, String event) {
    return event != null && copyPaste(context).contains(event);
  }

  /** Whether {@code event} is one of the built-in events of {@code context}, with or without copy/paste. */
  public static boolean isKnown(Context context, String event) {
    return event != null && (plain(context).contains(event) || copyPaste(context).contains(event)
        || (context == Context.ROW_ACTIVATION && ROW_ACTIVATION_EVENTS.contains(event)));
  }

  /**
   * Whether one of the relationships used by the tree's node types has a link Document Model. {@code relationships}
   * resolves a relationship model id to the model (or null).
   */
  public static boolean hasLinkDocumentModel(TreeModel model, Function<String, RelationshipModel> relationships) {
    if (model == null || model.getContent() == null) {
      return false;
    }
    for (TreeNode node : model.getContent().getNodes()) {
      for (TreeChildRelationshipConfiguration configuration : node.getChildRelationshipConfigurations()) {
        String ref = configuration.getRelationshipModelRef();
        RelationshipModel relationship = ref == null || ref.isBlank() ? null : relationships.apply(ref);
        if (relationship != null && relationship.getContent() != null
            && relationship.getContent().getLinkDocumentModelValue() != null) {
          return true;
        }
      }
    }
    return false;
  }

  private static List<String> plain(Context context) {
    return switch (context) {
      case ROW, ROW_ACTIVATION -> ROW_EVENTS;
      case HEADER -> HEADER_EVENTS;
      case MULTI_SELECTION -> MULTI_SELECTION_EVENTS;
    };
  }

  private static List<String> copyPaste(Context context) {
    return switch (context) {
      case ROW, ROW_ACTIVATION -> ROW_COPY_PASTE_EVENTS;
      case HEADER -> HEADER_COPY_PASTE_EVENTS;
      case MULTI_SELECTION -> MULTI_SELECTION_COPY_PASTE_EVENTS;
    };
  }
}
