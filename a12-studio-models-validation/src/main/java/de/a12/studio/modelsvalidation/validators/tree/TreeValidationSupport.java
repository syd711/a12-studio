package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ConfigurableBoxElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.overviewmodel.OverviewButtonLike;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeEvents;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.models.treemodel.TreeNodeActionGroup;
import de.a12.studio.models.treemodel.TreeVirtualRoot;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.overview.OverviewElementResolution;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** What the Tree Model validators share: resolving referenced models and finding every button / action of a tree. */
public final class TreeValidationSupport {

  /** Where a button or action sits; decides which of SME's rules and which event list apply. */
  public enum Kind {
    /** A node type's row action. */
    ROW_ACTION(TreeEvents.Context.ROW),
    /** An action in a group of a node type's context menu. */
    CONTEXT_MENU_ACTION(TreeEvents.Context.ROW),
    /** An action of the Virtual Root's row. */
    VIRTUAL_ROOT_ACTION(TreeEvents.Context.HEADER),
    /** An action in a group of the Virtual Root's context menu. */
    VIRTUAL_ROOT_CONTEXT_MENU_ACTION(TreeEvents.Context.HEADER),
    /** An element of the Subheader (button, multi-selection or expand all popup). */
    SUBHEADER_ELEMENT(TreeEvents.Context.HEADER),
    /** A button of the Footer. */
    FOOTER_BUTTON(TreeEvents.Context.HEADER),
    /** A button of the multi-selection panel. */
    MULTI_SELECTION_BUTTON(TreeEvents.Context.MULTI_SELECTION);

    private final TreeEvents.Context eventContext;

    Kind(TreeEvents.Context eventContext) {
      this.eventContext = eventContext;
    }

    public TreeEvents.Context eventContext() {
      return eventContext;
    }

    /** Whether this is an action of a node type or the Virtual Root (insert / event actions), not a plain button. */
    public boolean isAction() {
      return this == ROW_ACTION || this == CONTEXT_MENU_ACTION || this == VIRTUAL_ROOT_ACTION || this == VIRTUAL_ROOT_CONTEXT_MENU_ACTION;
    }

    public boolean isVirtualRoot() {
      return this == VIRTUAL_ROOT_ACTION || this == VIRTUAL_ROOT_CONTEXT_MENU_ACTION;
    }
  }

  /** One button or action with a description of its owner for messages, and the node type it belongs to (or null). */
  public record Site(Kind kind, OverviewButtonLike button, String owner, TreeNode node) {
  }

  private TreeValidationSupport() {
  }

  /** {@code id} as a Document Model, or - if it names a Combination Model - the stand-in SME resolves fields against. */
  public static DocumentModel documentModel(String id, ValidationContext context) {
    return id == null || id.isBlank() ? null : OverviewElementResolution.resolveDocumentModelOrCombination(id, context);
  }

  /**
   * The models the super type / sub type graph is built from: Document Models and Combination Models (SME's {@code
   * getSubTypesInfo} works on every standalone Document Model type, and a Combination Model can name super types).
   */
  public static List<A12Model<?>> heterogeneityModels(ValidationContext context) {
    return context.otherModels().stream()
        .filter(model -> model instanceof DocumentModel || model instanceof CombinedDocumentModel)
        .<A12Model<?>>map(model -> model)
        .toList();
  }

  public static Function<String, RelationshipModel> relationships(ValidationContext context) {
    return id -> id != null && context.findOtherModel(id) instanceof RelationshipModel relationship ? relationship : null;
  }

  public static boolean hasLinkDocumentModel(TreeModel model, ValidationContext context) {
    return TreeEvents.hasLinkDocumentModel(model, relationships(context));
  }

  /** A node type as shown in messages: its Document Model, or its id while it has none. */
  public static String name(TreeNode node) {
    if (node.getDocumentModelRef() != null && !node.getDocumentModelRef().isBlank()) {
      return node.getDocumentModelRef();
    }
    return node.getId() != null ? node.getId() : "?";
  }

  /** Every button and action of {@code model}, each with a description of where it is. */
  public static List<Site> sites(TreeModel model) {
    List<Site> sites = new ArrayList<>();
    for (TreeNode node : model.getContent().getNodes()) {
      String owner = ValidationMessages.get("validation.treeOwner.nodeRowAction", name(node));
      node.getActions().forEach(action -> sites.add(new Site(Kind.ROW_ACTION, action, owner, node)));
      if (node.getContextMenu() != null) {
        for (TreeNodeActionGroup group : node.getContextMenu().getGroups()) {
          String groupOwner = ValidationMessages.get("validation.treeOwner.nodeContextMenu", group.getName(), name(node));
          group.getActions().forEach(action -> sites.add(new Site(Kind.CONTEXT_MENU_ACTION, action, groupOwner, node)));
        }
      }
    }
    TreeConfiguration configuration = model.getContent().getConfiguration();
    TreeVirtualRoot virtualRoot = configuration != null ? configuration.getVirtualRoot() : null;
    if (virtualRoot != null) {
      String owner = ValidationMessages.get("validation.treeOwner.virtualRootAction");
      virtualRoot.getActions().forEach(action -> sites.add(new Site(Kind.VIRTUAL_ROOT_ACTION, action, owner, null)));
      if (virtualRoot.getContextMenu() != null) {
        for (TreeNodeActionGroup group : virtualRoot.getContextMenu().getGroups()) {
          String groupOwner = ValidationMessages.get("validation.treeOwner.virtualRootContextMenu", group.getName());
          group.getActions().forEach(action -> sites.add(new Site(Kind.VIRTUAL_ROOT_CONTEXT_MENU_ACTION, action, groupOwner, null)));
        }
      }
    }
    addElements(sites, model.getContent().getSubHeaderBox(), Kind.SUBHEADER_ELEMENT, "validation.treeOwner.subheaderElement");
    addElements(sites, model.getContent().getFooterBox(), Kind.FOOTER_BUTTON, "validation.treeOwner.footerButton");
    MultiSelectionConfig multiSelection = configuration != null ? configuration.getMultiSelection() : null;
    if (multiSelection != null) {
      String owner = ValidationMessages.get("validation.treeOwner.multiSelectionButton");
      for (Button button : multiSelection.getButtons()) {
        sites.add(new Site(Kind.MULTI_SELECTION_BUTTON, button, owner, null));
      }
    }
    return sites;
  }

  /** Whether {@code site} is a real button (as opposed to a multi-selection / expand all marker in the Subheader). */
  public static boolean isButton(Site site) {
    if (site.kind() == Kind.SUBHEADER_ELEMENT) {
      return site.button() instanceof ConfigurableBoxElement element && element.getType() == BoxElementType.BUTTON;
    }
    return true;
  }

  private static void addElements(List<Site> sites, ElementBox box, Kind kind, String ownerKey) {
    if (box == null) {
      return;
    }
    String owner = ValidationMessages.get(ownerKey);
    for (List<BoxElement> slot : List.of(box.getLeftSlot(), box.getRightSlot())) {
      for (BoxElement element : slot) {
        if (element instanceof ConfigurableBoxElement configurable) {
          sites.add(new Site(kind, configurable, owner, null));
        }
      }
    }
  }
}
