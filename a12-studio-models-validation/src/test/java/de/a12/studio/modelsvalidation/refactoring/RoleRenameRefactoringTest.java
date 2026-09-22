package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnLinkReference;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.OverviewModelContent;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.operator.HasOperator;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModelContent;
import de.a12.studio.modelsvalidation.refactoring.ProjectReferenceRefactoring.ModelEdits;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins that renaming a role ("Owner" -&gt; "Buyer") in relationship model {@code Owns} is followed into every
 * other project model that names that role: {@link QuerySort}/{@link QueryLink}/{@link HasOperator}, Form Model
 * {@code Binding}s, Relationship UI Models and Overview {@link ColumnLinkReference}s. Mirrors the style of
 * {@link ProjectReferenceRefactoringTest}.
 */
class RoleRenameRefactoringTest {

  private static final String RELATIONSHIP = "Owns";
  private static final String OLD_ROLE = "Owner";
  private static final String NEW_ROLE = "Buyer";

  @Test
  void aQuerySortFollowsTheRenameOfItsRelationshipRole() {
    QuerySort matching = sort(RELATIONSHIP, OLD_ROLE);
    QuerySort otherRole = sort(RELATIONSHIP, "Order");
    QuerySort otherRelationship = sort("Gone", OLD_ROLE);
    QueryModel query = query("Q", matching, otherRole, otherRelationship);

    List<ModelEdits> edits = renameAndApply(query);

    assertEquals(1, edits.size());
    assertEquals(NEW_ROLE, matching.getTargetRole());
    assertEquals("Order", otherRole.getTargetRole());
    assertEquals(OLD_ROLE, otherRelationship.getTargetRole());
  }

  @Test
  void aQueryLinkAndItsNestedLinksAndHasConstraintFollowTheRename() {
    QueryLink nested = link(RELATIONSHIP, OLD_ROLE);
    QueryLink top = link(RELATIONSHIP, OLD_ROLE);
    top.getLinks().add(nested);

    HasOperator has = new HasOperator();
    has.setRelationshipModel(RELATIONSHIP);
    has.setTargetRole(OLD_ROLE);
    top.setConstraint(has);

    QueryModelContent content = new QueryModelContent();
    content.getLinks().add(top);
    QueryModel query = new QueryModel();
    query.setId("Q");
    query.setContent(content);

    List<ModelEdits> edits = renameAndApply(query);

    assertEquals(1, edits.size());
    assertEquals(NEW_ROLE, top.getTargetRole());
    assertEquals(NEW_ROLE, nested.getTargetRole());
    assertEquals(NEW_ROLE, has.getTargetRole());
  }

  @Test
  void aFormBindingFollowsTheRenameOnlyWhenItsRelationshipAndRoleMatch() {
    Binding matching = binding(RELATIONSHIP, OLD_ROLE);
    Binding otherRole = binding(RELATIONSHIP, "Order");
    Binding otherRelationship = binding("Gone", OLD_ROLE);

    Screen screen = new Screen();
    screen.getScreenElements().addAll(List.of(matching, otherRole, otherRelationship));
    FormModelContent content = new FormModelContent();
    content.getScreens().add(screen);
    FormModel form = new FormModel();
    form.setId("F");
    form.setContent(content);

    List<ModelEdits> edits = renameAndApply(form);

    assertEquals(1, edits.size());
    assertEquals(NEW_ROLE, matching.getBinding().getDetails().getTargetRole());
    assertEquals("Order", otherRole.getBinding().getDetails().getTargetRole());
    assertEquals(OLD_ROLE, otherRelationship.getBinding().getDetails().getTargetRole());
  }

  @Test
  void aRelationshipUiModelFollowsTheRename() {
    RelationshipUiModelContent content = new RelationshipUiModelContent();
    content.setRelationshipName(RELATIONSHIP);
    content.setTargetRole(OLD_ROLE);
    RelationshipUiModel rum = new RelationshipUiModel();
    rum.setId("Ru");
    rum.setContent(content);

    List<ModelEdits> edits = renameAndApply(rum);

    assertEquals(1, edits.size());
    assertEquals(NEW_ROLE, content.getTargetRole());
  }

  @Test
  void aRelationshipUiModelForAnotherRoleIsNotTouched() {
    RelationshipUiModelContent content = new RelationshipUiModelContent();
    content.setRelationshipName(RELATIONSHIP);
    content.setTargetRole("Order");
    RelationshipUiModel rum = new RelationshipUiModel();
    rum.setId("Ru");
    rum.setContent(content);

    assertTrue(renameAndApply(rum).isEmpty());
    assertEquals("Order", content.getTargetRole());
  }

  @Test
  void anOverviewColumnLinkReferenceFollowsTheRename() {
    ColumnLinkReference matching = linkReference(RELATIONSHIP, OLD_ROLE);
    ColumnLinkReference otherRole = linkReference(RELATIONSHIP, "Order");
    Column column = new Column();
    column.getLinkReferences().addAll(List.of(matching, otherRole));
    OverviewModelContent content = new OverviewModelContent();
    content.getColumns().add(column);
    OverviewModel overview = new OverviewModel();
    overview.setId("Ov");
    overview.setContent(content);

    List<ModelEdits> edits = renameAndApply(overview);

    assertEquals(1, edits.size());
    assertEquals(NEW_ROLE, matching.getTargetRole());
    assertEquals("Order", otherRole.getTargetRole());
  }

  @Test
  void revertingTheEditsRestoresTheOldRole() {
    QuerySort sort = sort(RELATIONSHIP, OLD_ROLE);
    QueryModel query = query("Q", sort);

    List<ModelEdits> edits = RoleRenameRefactoring.computeEdits(RELATIONSHIP, OLD_ROLE, NEW_ROLE, List.of(query));
    edits.forEach(modelEdits -> modelEdits.edits().forEach(DocumentModelRefactoring.Edit::apply));
    assertEquals(NEW_ROLE, sort.getTargetRole());

    edits.forEach(modelEdits -> modelEdits.edits().forEach(DocumentModelRefactoring.Edit::revert));
    assertEquals(OLD_ROLE, sort.getTargetRole());
  }

  @Test
  void renamingToTheSameRoleYieldsNoEdits() {
    QueryModel query = query("Q", sort(RELATIONSHIP, OLD_ROLE));

    assertTrue(RoleRenameRefactoring.computeEdits(RELATIONSHIP, OLD_ROLE, OLD_ROLE, List.of(query)).isEmpty());
  }

  // ---- helpers ------------------------------------------------------------------------------------------------

  private static List<ModelEdits> renameAndApply(A12Model<?> model) {
    List<ModelEdits> edits = RoleRenameRefactoring.computeEdits(RELATIONSHIP, OLD_ROLE, NEW_ROLE, List.of(model));
    edits.forEach(modelEdits -> modelEdits.edits().forEach(DocumentModelRefactoring.Edit::apply));
    return edits;
  }

  private static QuerySort sort(String relationshipModel, String targetRole) {
    QuerySort sort = new QuerySort();
    sort.setRelationshipModel(relationshipModel);
    sort.setTargetRole(targetRole);
    return sort;
  }

  private static QueryLink link(String relationshipModel, String targetRole) {
    QueryLink link = new QueryLink();
    link.setRelationshipModel(relationshipModel);
    link.setTargetRole(targetRole);
    return link;
  }

  private static QueryModel query(String id, QuerySort... sorts) {
    QueryModelContent content = new QueryModelContent();
    content.getSort().addAll(List.of(sorts));
    QueryModel model = new QueryModel();
    model.setId(id);
    model.setContent(content);
    return model;
  }

  private static Binding binding(String relationshipName, String targetRole) {
    Binding binding = new Binding();
    binding.getBinding().getDetails().setRelationshipName(relationshipName);
    binding.getBinding().getDetails().setTargetRole(targetRole);
    return binding;
  }

  private static ColumnLinkReference linkReference(String relationship, String targetRole) {
    ColumnLinkReference reference = new ColumnLinkReference();
    reference.setRelationship(relationship);
    reference.setTargetRole(targetRole);
    return reference;
  }
}
