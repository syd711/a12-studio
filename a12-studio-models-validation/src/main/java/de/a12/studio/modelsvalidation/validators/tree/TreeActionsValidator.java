package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.RowActivation;
import de.a12.studio.models.treemodel.TreeEvents;
import de.a12.studio.models.treemodel.TreeHeterogeneity;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.models.treemodel.TreeNodeActionGroup;
import de.a12.studio.models.treemodel.TreeNodeContextMenu;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeValidationSupport.Kind;
import de.a12.studio.modelsvalidation.validators.tree.TreeValidationSupport.Site;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * The rules on every button and action of a tree (SME's {@code TM_NodeAction}, {@code OMTM_Button} and {@code
 * OMTM_Subheader_Element} meta models plus their custom conditions), for the node types' row actions and context menus,
 * the Virtual Root, the row activation, the Subheader, the Footer and the multi-selection buttons:
 * <ul>
 *   <li>an event action needs an Event, an insert action a Position (not on the Virtual Root, it always inserts as
 *       child) and, when it names a Document Model, one that is among the candidates for its position;</li>
 *   <li>a button needs an Event;</li>
 *   <li>the copy/paste events are not available while a relationship has a link Document Model;</li>
 *   <li>annotation names are unique per action or button;</li>
 *   <li>a context menu group needs a name and an action, and a group of type "add" holds insert actions only;</li>
 *   <li>a row activation of type event needs an event, one of type insert a position.</li>
 * </ul>
 * Not ported: "Priority is required" (SME fills it on load, an unset {@code primary} here just means secondary) and
 * "Invalid Event" (SME's own candidate list always contains the current value, so it never fires).
 */
public final class TreeActionsValidator implements ModelValidator {

  public static final String ACTIONS_ELEMENT_ID = "content/nodes/actions";
  public static final String CONTEXT_MENU_ELEMENT_ID = "content/nodes/contextMenu/groups";
  public static final String ROW_ACTIVATION_ELEMENT_ID = "content/nodes/rowActivation";
  public static final String VIRTUAL_ROOT_ELEMENT_ID = "content/configuration/virtualRoot";
  public static final String SUBHEADER_ELEMENT_ID = "content/subHeaderBox";
  public static final String FOOTER_ELEMENT_ID = "content/footerBox";
  public static final String MULTI_SELECTION_BUTTONS_ELEMENT_ID = "content/configuration/multiSelection/buttons";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    boolean linkDocumentModel = TreeValidationSupport.hasLinkDocumentModel(treeModel, context);
    List<A12Model<?>> documentModels = TreeValidationSupport.heterogeneityModels(context);
    Function<String, RelationshipModel> relationships = TreeValidationSupport.relationships(context);
    List<ModelValidationError> errors = new ArrayList<>();

    for (Site site : TreeValidationSupport.sites(treeModel)) {
      String elementId = elementId(site.kind());
      if (site.kind().isAction()) {
        checkAction(model, errors, treeModel, site, elementId, linkDocumentModel, documentModels, relationships);
      }
      else {
        checkButton(model, errors, site, elementId, linkDocumentModel);
      }
      checkAnnotations(model, errors, site, elementId);
    }

    for (TreeNode node : treeModel.getContent().getNodes()) {
      checkContextMenu(model, errors, node.getContextMenu(), ValidationMessages.get("validation.treeOwner.node", TreeValidationSupport.name(node)),
          CONTEXT_MENU_ELEMENT_ID, false);
      checkRowActivation(model, errors, treeModel, node, linkDocumentModel, documentModels, relationships);
    }
    if (treeModel.getContent().getConfiguration() != null && treeModel.getContent().getConfiguration().getVirtualRoot() != null) {
      checkContextMenu(model, errors, treeModel.getContent().getConfiguration().getVirtualRoot().getContextMenu(),
          ValidationMessages.get("validation.treeOwner.virtualRoot"), VIRTUAL_ROOT_ELEMENT_ID, true);
    }
    return errors;
  }

  private static String elementId(Kind kind) {
    return switch (kind) {
      case ROW_ACTION -> ACTIONS_ELEMENT_ID;
      case CONTEXT_MENU_ACTION -> CONTEXT_MENU_ELEMENT_ID;
      case VIRTUAL_ROOT_ACTION, VIRTUAL_ROOT_CONTEXT_MENU_ACTION -> VIRTUAL_ROOT_ELEMENT_ID;
      case SUBHEADER_ELEMENT -> SUBHEADER_ELEMENT_ID;
      case FOOTER_BUTTON -> FOOTER_ELEMENT_ID;
      case MULTI_SELECTION_BUTTON -> MULTI_SELECTION_BUTTONS_ELEMENT_ID;
    };
  }

  private static void checkAction(A12Model<?> model, List<ModelValidationError> errors, TreeModel treeModel, Site site, String elementId,
      boolean linkDocumentModel, List<A12Model<?>> documentModels, Function<String, RelationshipModel> relationships) {
    TreeNodeAction action = (TreeNodeAction) site.button();
    if (action.getType() == null || action.getType().isBlank()) {
      errors.add(error(model, elementId, "validation.treeActions.typeMissing", site.owner()));
      return;
    }
    if (action.isInsert()) {
      String position = action.getPosition();
      boolean positionSet = position != null && !position.isBlank();
      if (!positionSet && !site.kind().isVirtualRoot()) {
        errors.add(error(model, elementId, "validation.treeActions.positionMissing", site.owner()));
      }
      String documentModelRef = action.getDocumentModelRef();
      if (documentModelRef != null && !documentModelRef.isBlank() && (positionSet || site.kind().isVirtualRoot())) {
        List<String> candidates = site.kind().isVirtualRoot()
            ? TreeHeterogeneity.rootInsertCandidates(treeModel, documentModels)
            : TreeHeterogeneity.insertCandidates(treeModel, site.node(), position, relationships, documentModels);
        if (!candidates.contains(documentModelRef)) {
          errors.add(error(model, elementId, "validation.treeActions.documentModelInvalid", documentModelRef, site.owner()));
        }
      }
    }
    else if (action.getEvent() == null || action.getEvent().isBlank()) {
      errors.add(error(model, elementId, "validation.treeActions.eventMissing", site.owner()));
    }
    checkCopyPaste(model, errors, action.getEvent(), site, elementId, linkDocumentModel);
  }

  private static void checkButton(A12Model<?> model, List<ModelValidationError> errors, Site site, String elementId, boolean linkDocumentModel) {
    if (!TreeValidationSupport.isButton(site)) {
      return;
    }
    String event = site.button().getEvent();
    if (event == null || event.isBlank()) {
      errors.add(error(model, elementId, "validation.treeActions.buttonEventMissing", site.owner()));
    }
    checkCopyPaste(model, errors, event, site, elementId, linkDocumentModel);
  }

  private static void checkCopyPaste(A12Model<?> model, List<ModelValidationError> errors, String event, Site site, String elementId,
      boolean linkDocumentModel) {
    if (linkDocumentModel && TreeEvents.isCopyPaste(site.kind().eventContext(), event)) {
      errors.add(error(model, elementId, "validation.treeActions.copyPasteWithLinkDocumentModel", event, site.owner()));
    }
  }

  private static void checkAnnotations(A12Model<?> model, List<ModelValidationError> errors, Site site, String elementId) {
    Set<String> seen = new HashSet<>();
    for (Annotation annotation : site.button().getAnnotations()) {
      if (annotation.getName() != null && !annotation.getName().isBlank() && !seen.add(annotation.getName())) {
        errors.add(error(model, elementId, "validation.treeActions.annotationTwice", annotation.getName(), site.owner()));
      }
    }
  }

  private static void checkContextMenu(A12Model<?> model, List<ModelValidationError> errors, TreeNodeContextMenu menu, String owner,
      String elementId, boolean virtualRoot) {
    if (menu == null) {
      return;
    }
    for (TreeNodeActionGroup group : menu.getGroups()) {
      boolean named = group.getName() != null && !group.getName().isBlank();
      boolean add = TreeNodeActionGroup.TYPE_ADD.equals(group.getType());
      if (!named) {
        errors.add(error(model, elementId, "validation.treeActions.groupNameMissing", owner));
      }
      else if (group.getActions().isEmpty() && !(virtualRoot && add)) {
        errors.add(error(model, elementId, "validation.treeActions.groupEmpty", group.getName(), owner));
      }
      if (add && group.getActions().stream().anyMatch(action -> !action.isInsert())) {
        errors.add(error(model, elementId, "validation.treeActions.addGroupOnlyInsert", group.getName(), owner));
      }
    }
  }

  private static void checkRowActivation(A12Model<?> model, List<ModelValidationError> errors, TreeModel treeModel, TreeNode node,
      boolean linkDocumentModel, List<A12Model<?>> documentModels, Function<String, RelationshipModel> relationships) {
    RowActivation activation = node.getRowActivation();
    if (activation == null || activation.getType() == null) {
      return;
    }
    String owner = ValidationMessages.get("validation.treeOwner.node", TreeValidationSupport.name(node));
    if (RowActivation.TYPE_EVENT.equals(activation.getType()) && (activation.getEvent() == null || activation.getEvent().isBlank())) {
      errors.add(error(model, ROW_ACTIVATION_ELEMENT_ID, "validation.treeActions.rowActivationEventMissing", owner));
    }
    if (RowActivation.TYPE_INSERT.equals(activation.getType())) {
      String position = activation.getPosition();
      if (position == null || position.isBlank()) {
        errors.add(error(model, ROW_ACTIVATION_ELEMENT_ID, "validation.treeActions.rowActivationPositionMissing", owner));
      }
      else if (activation.getDocumentModelRef() != null && !activation.getDocumentModelRef().isBlank()
          && !TreeHeterogeneity.insertCandidates(treeModel, node, position, relationships, documentModels).contains(activation.getDocumentModelRef())) {
        errors.add(error(model, ROW_ACTIVATION_ELEMENT_ID, "validation.treeActions.documentModelInvalid", activation.getDocumentModelRef(), owner));
      }
    }
    if (linkDocumentModel && TreeEvents.isCopyPaste(TreeEvents.Context.ROW_ACTIVATION, activation.getEvent())) {
      errors.add(error(model, ROW_ACTIVATION_ELEMENT_ID, "validation.treeActions.copyPasteWithLinkDocumentModel", activation.getEvent(), owner));
    }
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String key, Object... arguments) {
    return new ModelValidationError(model, elementId, ValidationMessages.get(key, arguments), Severity.ERROR.name());
  }
}
