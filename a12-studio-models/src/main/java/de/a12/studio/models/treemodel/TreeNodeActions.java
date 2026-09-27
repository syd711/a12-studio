package de.a12.studio.models.treemodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.overviewmodel.Icon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Ready-made {@link TreeNodeAction}s. */
public final class TreeNodeActions {

  // The texts SME ships for its default Delete action, by locale; every other model locale gets an empty text.
  private static final Map<String, String> DELETE_LABEL = Map.of("en", "Delete", "de", "Löschen");
  private static final Map<String, String> DELETE_DESCRIPTION = Map.of("en", "Delete Node", "de", "Knoten löschen");
  private static final Map<String, String> DELETE_CONFIRMATION_TITLE = Map.of("en", "Delete Node", "de", "Knoten löschen");
  private static final Map<String, String> DELETE_CONFIRMATION_MESSAGE = Map.of("en", "Do you want to delete this node?",
      "de", "Möchten Sie diesen Knoten löschen?");

  private TreeNodeActions() {
  }

  /**
   * The row action SME adds to every new node type (its {@code initializeNewNodeMiddleware}): an icon-only, destructive
   * "delete node" button with a confirmation, with a text for each of {@code localeCodes}.
   */
  public static TreeNodeAction newDeleteAction(List<String> localeCodes) {
    TreeNodeAction action = new TreeNodeAction();
    action.setType(TreeNodeAction.TYPE_EVENT);
    action.setEvent("event_delete_node");
    action.setPrimary(false);
    action.setDestructive(true);
    action.setLabelHidden(true);
    Icon icon = new Icon();
    icon.setName("delete_forever");
    icon.setTheme(Icon.THEME_FILLED);
    action.setIcon(icon);
    action.setLabel(texts(DELETE_LABEL, localeCodes));
    action.setDescription(texts(DELETE_DESCRIPTION, localeCodes));
    Confirmation confirmation = new Confirmation();
    confirmation.setTitle(texts(DELETE_CONFIRMATION_TITLE, localeCodes));
    confirmation.setMessage(texts(DELETE_CONFIRMATION_MESSAGE, localeCodes));
    action.setConfirmation(confirmation);
    return action;
  }

  private static List<Label> texts(Map<String, String> byLocale, List<String> localeCodes) {
    List<Label> labels = new ArrayList<>();
    for (String code : localeCodes) {
      Label label = new Label();
      label.setLocale(code);
      label.setText(byLocale.getOrDefault(code, ""));
      labels.add(label);
    }
    return labels;
  }
}
