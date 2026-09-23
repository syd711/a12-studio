package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnLinkReference;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.operator.AndOperator;
import de.a12.studio.models.querymodel.operator.HasOperator;
import de.a12.studio.models.querymodel.operator.NotOperator;
import de.a12.studio.models.querymodel.operator.Operator;
import de.a12.studio.models.querymodel.operator.OrOperator;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.ProjectReferenceRefactoring.ModelEdits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Updates all cross-model references that hold a role name when a role in a Relationship Model is renamed.
 *
 * <p>A role rename in a Relationship Model must be reflected wherever other models reference it by name:
 * <ul>
 *   <li><b>Query Models</b>: {@code QueryLink.targetRole}, {@code QuerySort.targetRole}, and
 *       {@code HasOperator.targetRole} - every reference that names {@code relationshipModelId} and
 *       {@code oldRole} is updated to {@code newRole}.</li>
 *   <li><b>Form Models</b>: {@code BindingDetails.targetRole} for bindings whose
 *       {@code relationshipName} matches {@code relationshipModelId}.</li>
 *   <li><b>Relationship UI Models</b>: {@code RelationshipUiModelContent.targetRole} whose
 *       {@code relationshipName} matches {@code relationshipModelId}.</li>
 *   <li><b>Overview Models</b>: {@code ColumnLinkReference.targetRole} whose
 *       {@code relationship} matches {@code relationshipModelId}.</li>
 * </ul>
 *
 * <p>Only references that unambiguously point to {@code relationshipModelId}/{@code oldRole} are updated;
 * anything that cannot be resolved is left alone and logged.
 */
public final class RoleRenameRefactoring {

  private static final Logger log = LoggerFactory.getLogger(RoleRenameRefactoring.class);

  private RoleRenameRefactoring() {
  }

  /**
   * Computes the edits that bring all other models in {@code projectModels} up to date after
   * {@code oldRole} in {@code relationshipModelId} was renamed to {@code newRole}.
   * Models with no affected reference yield nothing.
   */
  public static List<ModelEdits> computeEdits(String relationshipModelId, String oldRole, String newRole,
      Collection<? extends A12Model<?>> projectModels) {
    if (relationshipModelId == null || oldRole == null || newRole == null || oldRole.equals(newRole)) {
      return List.of();
    }
    List<ModelEdits> result = new ArrayList<>();
    for (A12Model<?> model : projectModels) {
      if (model == null) {
        continue;
      }
      try {
        List<Edit> edits = editsFor(model, relationshipModelId, oldRole, newRole);
        if (!edits.isEmpty()) {
          result.add(new ModelEdits(model, edits));
        }
      }
      catch (RuntimeException e) {
        log.warn("Could not update role references in {}: {}", model.getId(), e.getMessage(), e);
      }
    }
    return result;
  }

  private static List<Edit> editsFor(A12Model<?> model, String relationshipModelId,
      String oldRole, String newRole) {
    List<Edit> edits = new ArrayList<>();
    if (model instanceof QueryModel query) {
      queryEdits(query, relationshipModelId, oldRole, newRole, edits);
    }
    else if (model instanceof FormModel form) {
      formEdits(form, relationshipModelId, oldRole, newRole, edits);
    }
    else if (model instanceof RelationshipUiModel rum) {
      rumEdits(rum, relationshipModelId, oldRole, newRole, edits);
    }
    else if (model instanceof OverviewModel overview) {
      overviewEdits(overview, relationshipModelId, oldRole, newRole, edits);
    }
    return edits;
  }

  // ---- Query Model ------------------------------------------------------------------------------------------

  private static void queryEdits(QueryModel query, String relationshipModelId,
      String oldRole, String newRole, List<Edit> edits) {
    if (query.getContent() == null) {
      return;
    }
    for (QuerySort sort : query.getContent().getSort()) {
      if (relationshipModelId.equals(sort.getRelationshipModel()) && oldRole.equals(sort.getTargetRole())) {
        edits.add(new Edit(sort::setTargetRole, oldRole, newRole));
      }
    }
    links(query.getContent().getLinks(), relationshipModelId, oldRole, newRole, edits);
    operator(query.getContent().getConstraint(), relationshipModelId, oldRole, newRole, edits);
  }

  private static void links(List<QueryLink> links, String relationshipModelId,
      String oldRole, String newRole, List<Edit> edits) {
    if (links == null) {
      return;
    }
    for (QueryLink link : links) {
      if (relationshipModelId.equals(link.getRelationshipModel()) && oldRole.equals(link.getTargetRole())) {
        edits.add(new Edit(link::setTargetRole, oldRole, newRole));
      }
      links(link.getLinks(), relationshipModelId, oldRole, newRole, edits);
      operator(link.getConstraint(), relationshipModelId, oldRole, newRole, edits);
    }
  }

  private static void operator(Operator operator, String relationshipModelId,
      String oldRole, String newRole, List<Edit> edits) {
    if (operator == null) {
      return;
    }
    if (operator instanceof AndOperator and && and.getOperands() != null) {
      and.getOperands().forEach(o -> operator(o, relationshipModelId, oldRole, newRole, edits));
    }
    else if (operator instanceof OrOperator or && or.getOperands() != null) {
      or.getOperands().forEach(o -> operator(o, relationshipModelId, oldRole, newRole, edits));
    }
    else if (operator instanceof NotOperator not) {
      operator(not.getOperand(), relationshipModelId, oldRole, newRole, edits);
    }
    else if (operator instanceof HasOperator has) {
      if (relationshipModelId.equals(has.getRelationshipModel()) && oldRole.equals(has.getTargetRole())) {
        edits.add(new Edit(has::setTargetRole, oldRole, newRole));
      }
      operator(has.getConstraint(), relationshipModelId, oldRole, newRole, edits);
      operator(has.getLinkDocumentConstraint(), relationshipModelId, oldRole, newRole, edits);
    }
  }

  // ---- Form Model -------------------------------------------------------------------------------------------

  private static void formEdits(FormModel form, String relationshipModelId,
      String oldRole, String newRole, List<Edit> edits) {
    if (form.getContent() == null) {
      return;
    }
    for (ScreenElement element : FormModelWalker.find(form.getContent(), ScreenElement.class)) {
      if (element instanceof Binding binding
          && binding.getBinding() != null
          && binding.getBinding().getDetails() != null) {
        bindingEdit(binding.getBinding().getDetails(), relationshipModelId, oldRole, newRole, edits);
      }
      else if (element instanceof BindingRepeat bindingRepeat
          && bindingRepeat.getBinding() != null
          && bindingRepeat.getBinding().getDetails() != null) {
        bindingEdit(bindingRepeat.getBinding().getDetails(), relationshipModelId, oldRole, newRole, edits);
      }
    }
  }

  private static void bindingEdit(BindingDetails details, String relationshipModelId,
      String oldRole, String newRole, List<Edit> edits) {
    if (relationshipModelId.equals(details.getRelationshipName()) && oldRole.equals(details.getTargetRole())) {
      edits.add(new Edit(details::setTargetRole, oldRole, newRole));
    }
  }

  // ---- Relationship UI Model --------------------------------------------------------------------------------

  private static void rumEdits(RelationshipUiModel rum, String relationshipModelId,
      String oldRole, String newRole, List<Edit> edits) {
    if (rum.getContent() == null) {
      return;
    }
    var content = rum.getContent();
    if (relationshipModelId.equals(content.getRelationshipName())
        && oldRole.equals(content.getTargetRole())) {
      edits.add(new Edit(content::setTargetRole, oldRole, newRole));
    }
  }

  // ---- Overview Model ---------------------------------------------------------------------------------------

  private static void overviewEdits(OverviewModel overview, String relationshipModelId,
      String oldRole, String newRole, List<Edit> edits) {
    if (overview.getContent() == null || overview.getContent().getColumns() == null) {
      return;
    }
    for (Column column : overview.getContent().getColumns()) {
      for (ColumnLinkReference ref : column.getLinkReferences()) {
        if (relationshipModelId.equals(ref.getRelationship()) && oldRole.equals(ref.getTargetRole())) {
          edits.add(new Edit(ref::setTargetRole, oldRole, newRole));
        }
      }
    }
  }
}
