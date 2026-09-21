package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModel;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.DocumentModelIdRef;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelContent;
import de.a12.studio.models.combineddocumentmodel.SelectionModelIdRef;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.RuleConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.mappingmodel.MappingModelContent;
import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.models.mappingmodel.MappingTarget;
import de.a12.studio.models.mappingmodel.SortField;
import de.a12.studio.models.mappingmodel.SortInfo;
import de.a12.studio.models.mappingmodel.StructuralMappingModelRef;
import de.a12.studio.models.printmodel.FieldRef;
import de.a12.studio.models.printmodel.Calculation;
import de.a12.studio.models.printmodel.ComputationStep;
import de.a12.studio.models.printmodel.PrintCalculationElement;
import de.a12.studio.models.printmodel.PrintFieldElement;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.printmodel.PrintModelContent;
import de.a12.studio.models.querymodel.QueryAggregation;
import de.a12.studio.models.querymodel.QueryAggregationEntry;
import de.a12.studio.models.querymodel.QueryAggregationGroup;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.operator.AndOperator;
import de.a12.studio.models.querymodel.operator.DoubleRangeOperator;
import de.a12.studio.models.querymodel.operator.ExactMatchOperator;
import de.a12.studio.models.querymodel.operator.HasOperator;
import de.a12.studio.models.querymodel.operator.NotOperator;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.models.selectionmodel.PathSpecification;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.models.selectionmodel.SelectionModelContent;
import de.a12.studio.models.structuralmappingmodel.FieldMapping;
import de.a12.studio.models.structuralmappingmodel.GroupToClearOnFirstFill;
import de.a12.studio.models.structuralmappingmodel.MappingBlock;
import de.a12.studio.models.structuralmappingmodel.ResolutionStrategy;
import de.a12.studio.models.structuralmappingmodel.Slice;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModelContent;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.ProjectReferenceRefactoring.ModelEdits;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the rewriting of the references other project models hold on a Document Model when one of its elements is
 * renamed or moved. The changed model, {@code Person_DM}:
 * <pre>
 * /Person
 *   Name           (field)
 *   Address
 *     Street       (field)
 *     City         (field)
 * </pre>
 */
class ProjectReferenceRefactoringTest {

  private final FieldElement name = field("Name");
  private final FieldElement street = field("Street");
  private final FieldElement city = field("City");
  private final GroupElement address = group("Address", street, city);
  private final GroupElement person = group("Person", name, address);
  private final DocumentModel personModel = documentModel("Person_DM", person);

  // ---- the path rewriter -------------------------------------------------------------------------------------

  @Test
  void aRenamedFieldIsFollowedInAbsolutePathsAndTheirEndings() {
    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(personModel);
    street.setName("Road");
    DocumentModelRefactoring.PathRewriter rewriter = plan.pathRewriter();

    assertEquals("/Person/Address/Road", rewriter.rewriteAbsolute("/Person/Address/Street"));
    assertEquals("/Person/Address/Road", rewriter.rewriteAbsolute("/Person/Address/Street"));
    assertEquals("/Person/Address/City", rewriter.rewriteAbsolute("/Person/Address/City"));
    assertEquals("/Person/Name", rewriter.rewriteAbsolute("/Person/Name"));
  }

  @Test
  void aRenamedGroupIsFollowedForEverythingBelowItAndItsEndingsSurvive() {
    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(personModel);
    address.setName("Home");
    DocumentModelRefactoring.PathRewriter rewriter = plan.pathRewriter();

    assertEquals("/Person/Home/Street", rewriter.rewriteAbsolute("/Person/Address/Street"));
    assertEquals("/Person/Home/", rewriter.rewriteAbsolute("/Person/Address/"));
    assertEquals("/Person/Home/*", rewriter.rewriteAbsolute("/Person/Address/*"));
    assertEquals("/Person/Home", rewriter.rewriteAbsolute("/Person/Address"));
  }

  @Test
  void aMovedFieldIsFollowedAndPathsThatDontResolveAreLeftAlone() {
    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(personModel);
    children(address).remove(street);
    children(person).add(street);
    DocumentModelRefactoring.PathRewriter rewriter = plan.pathRewriter();

    assertEquals("/Person/Street", rewriter.rewriteAbsolute("/Person/Address/Street"));
    assertEquals("/Nowhere/Street", rewriter.rewriteAbsolute("/Nowhere/Street"));
    assertEquals("Person/Address/Street", rewriter.rewriteAbsolute("Person/Address/Street"));
    assertEquals("", rewriter.rewriteAbsolute(""));
    assertNull(rewriter.rewriteAbsolute(null));
  }

  // ---- other Document Models that include the changed one ---------------------------------------------------

  @Test
  void aDocumentModelThatIncludesTheChangedOneFollowsARenameOfAnIncludedField() {
    RuleElement rule = rule("StreetRule", "../Person/Street", "[Person/Street] < 0 And [Total] > 0");
    GroupElement includeGroup = includeGroup("Person", "Person_DM");
    GroupElement shop = group("Shop", includeGroup, field("Total"), rule);
    DocumentModel shopModel = documentModel("Shop_DM", shop);
    // Person_DM mounts the children of its root group, so "Person/Street" would be Street directly below the root;
    // put the field there for this case.
    children(person).add(street);

    List<ModelEdits> edits = renameAndCompute(street, "Road", shopModel);

    assertEquals(1, edits.size());
    assertEquals("../Person/Road", rule.getRule().getErrorEntityRelPath());
    assertEquals("[Person/Road] < 0 And [Total] > 0", rule.getRule().getErrorCondition());
  }

  @Test
  void aDocumentModelThatIncludesTheChangedOneFollowsAMoveOfAnIncludedField() {
    RuleElement rule = rule("StreetRule", "../Person/Address/Street", "[Person/Address/Street] < 0");
    GroupElement shop = group("Shop", includeGroup("Person", "Person_DM"), rule);
    DocumentModel shopModel = documentModel("Shop_DM", shop);

    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(personModel);
    children(address).remove(street);
    children(person).add(street);
    List<ModelEdits> edits = ProjectReferenceRefactoring.computeEdits(personModel, plan,
        List.of(personModel, shopModel));
    apply(edits);

    assertEquals(1, edits.size());
    assertEquals("../Person/Street", rule.getRule().getErrorEntityRelPath());
    assertEquals("[Person/Street] < 0", rule.getRule().getErrorCondition());
  }

  @Test
  void renamingTheIncludedModelsRootGroupChangesNothingInTheIncludingModel() {
    RuleElement rule = rule("StreetRule", "../Person/Address/Street", "[Person/Address/Street] < 0");
    GroupElement shop = group("Shop", includeGroup("Person", "Person_DM"), rule);
    DocumentModel shopModel = documentModel("Shop_DM", shop);

    assertEquals(List.of(), renameAndCompute(person, "Human", shopModel));
    assertEquals("[Person/Address/Street] < 0", rule.getRule().getErrorCondition());
  }

  @Test
  void aDocumentModelThatDoesNotIncludeTheChangedOneIsNotTouched() {
    RuleElement rule = rule("StreetRule", "../Person/Address/Street", "[Person/Address/Street] < 0");
    GroupElement shop = group("Shop", includeGroup("Person", "Other_DM"), rule);
    DocumentModel shopModel = documentModel("Shop_DM", shop);

    assertEquals(List.of(), renameAndCompute(street, "Road", shopModel));
    assertEquals("[Person/Address/Street] < 0", rule.getRule().getErrorCondition());
  }

  // A includes B, B includes the changed Person_DM: a path of A ends in Person_DM's elements two Includes further on.
  @Test
  void aDocumentModelThatReachesTheChangedOneThroughAChainOfIncludesFollowsARename() {
    RuleElement inMid = rule("MidRule", "../Person/Address/Street", "[Person/Address/Street] < 0");
    DocumentModel mid = documentModel("Mid_DM", group("Mid", includeGroup("Person", "Person_DM"), inMid));
    RuleElement inShop = rule("ShopRule", "../Mid/Person/Address/Street", "[Mid/Person/Address/Street] < 0 And [Mid/Note] > 0");
    DocumentModel shop = documentModel("Shop_DM", group("Shop", includeGroup("Mid", "Mid_DM"), inShop));

    List<ModelEdits> edits = renameAndCompute(street, "Road", shop, mid);

    assertEquals(2, edits.size());
    assertEquals("[Person/Address/Road] < 0", inMid.getRule().getErrorCondition());
    assertEquals("../Mid/Person/Address/Road", inShop.getRule().getErrorEntityRelPath());
    assertEquals("[Mid/Person/Address/Road] < 0 And [Mid/Note] > 0", inShop.getRule().getErrorCondition());

    revert(edits);
    assertEquals("[Mid/Person/Address/Street] < 0 And [Mid/Note] > 0", inShop.getRule().getErrorCondition());
    assertEquals("[Person/Address/Street] < 0", inMid.getRule().getErrorCondition());
  }

  @Test
  void aChainOfIncludesFollowsAMoveAndLeavesPathsThatEndInTheIntermediateModelAlone() {
    RuleElement rule = rule("ShopRule", "../Mid/Person/Address/Street", "[Mid/Person/Address/Street] < 0 And [Mid/Extra] > 0");
    DocumentModel mid = documentModel("Mid_DM", group("Mid", includeGroup("Person", "Person_DM"), field("Extra")));
    DocumentModel shop = documentModel("Shop_DM", group("Shop", includeGroup("Mid", "Mid_DM"), rule));

    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(personModel);
    children(address).remove(street);
    children(person).add(street);
    apply(ProjectReferenceRefactoring.computeEdits(personModel, plan, List.of(personModel, mid, shop)));

    assertEquals("../Mid/Person/Street", rule.getRule().getErrorEntityRelPath());
    assertEquals("[Mid/Person/Street] < 0 And [Mid/Extra] > 0", rule.getRule().getErrorCondition());
  }

  @Test
  void aChainThatDoesNotLeadToTheChangedModelIsNotTouched() {
    DocumentModel mid = documentModel("Mid_DM", group("Mid", includeGroup("Person", "Other_DM")));
    RuleElement rule = rule("ShopRule", "../Mid/Person/Address/Street", "[Mid/Person/Address/Street] < 0");
    DocumentModel shop = documentModel("Shop_DM", group("Shop", includeGroup("Mid", "Mid_DM"), rule));

    assertEquals(List.of(), renameAndCompute(street, "Road", shop, mid));
    assertEquals("[Mid/Person/Address/Street] < 0", rule.getRule().getErrorCondition());
  }

  @Test
  void includeCyclesInTheProjectDoNotSendTheChainWalkAroundForever() {
    DocumentModel first = documentModel("First_DM", group("First", includeGroup("Second", "Second_DM")));
    DocumentModel second = documentModel("Second_DM", group("Second", includeGroup("First", "First_DM")));

    assertEquals(List.of(), renameAndCompute(street, "Road", first, second));
  }

  // ---- Additive Document Model over the changed one ---------------------------------------------------------

  // The overlay mirrors Person_DM's root group "Person" and adds a field there; its rule also reads what the base
  // provides below it, which the overlay's own file does not contain.
  @Test
  void anAdditiveDocumentModelFollowsARenameInItsBaseModel() {
    RuleElement rule = rule("NickRule", "../Nick", "[Address/Street] < 0 And [Nick] > 0");
    AdditiveDocumentModel overlay = additiveModel("Person_Ad", group("Person", field("Nick"), rule));
    CombinedDocumentModel combination = additionCombination("Person_Cm", "Person_DM", "Person_Ad");

    List<ModelEdits> edits = renameAndCompute(street, "Road", overlay, combination);

    assertEquals(1, edits.size());
    assertEquals("[Address/Road] < 0 And [Nick] > 0", rule.getRule().getErrorCondition());
    assertEquals("../Nick", rule.getRule().getErrorEntityRelPath());

    revert(edits);
    assertEquals("[Address/Street] < 0 And [Nick] > 0", rule.getRule().getErrorCondition());
  }

  @Test
  void anAdditiveDocumentModelFollowsAMoveOfABaseFieldOutOfItsGroup() {
    RuleElement rule = rule("NickRule", "../Nick", "[Address/Street] < 0");
    AdditiveDocumentModel overlay = additiveModel("Person_Ad", group("Person", field("Nick"), rule));
    CombinedDocumentModel combination = additionCombination("Person_Cm", "Person_DM", "Person_Ad");

    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(personModel);
    children(address).remove(street);
    children(person).add(street);
    apply(ProjectReferenceRefactoring.computeEdits(personModel, plan, List.of(personModel, overlay, combination)));

    assertEquals("[Street] < 0", rule.getRule().getErrorCondition());
  }

  @Test
  void anAdditiveDocumentModelWhoseBaseIsAnotherModelIsNotTouched() {
    RuleElement rule = rule("NickRule", "../Nick", "[Address/Street] < 0");
    AdditiveDocumentModel overlay = additiveModel("Person_Ad", group("Person", field("Nick"), rule));
    CombinedDocumentModel combination = additionCombination("Other_Cm", "Other_DM", "Person_Ad");

    assertEquals(List.of(), renameAndCompute(street, "Road", overlay, combination));
    assertEquals("[Address/Street] < 0", rule.getRule().getErrorCondition());
  }

  @Test
  void aMirroredGroupThatTheBaseRenamesIsNotFollowedByTheOverlayPaths() {
    RuleElement rule = rule("NickRule", "../Nick", "[Address/Street] < 0");
    AdditiveDocumentModel overlay = additiveModel("Person_Ad", group("Person", field("Nick"), rule));
    CombinedDocumentModel combination = additionCombination("Person_Cm", "Person_DM", "Person_Ad");

    assertEquals(List.of(), renameAndCompute(person, "Human", overlay, combination));
    assertEquals("[Address/Street] < 0", rule.getRule().getErrorCondition());
  }

  // ---- Print Model ------------------------------------------------------------------------------------------

  @Test
  void aPrintFieldFollowsTheRenameOnlyWhenItReadsTheChangedModel() {
    FieldRef mine = fieldRef("Person_DM", "/Person/Address/Street");
    FieldRef foreign = fieldRef("Other_DM", "/Person/Address/Street");
    PrintModel print = printModel("Letter_Pt", mine, foreign);

    List<ModelEdits> edits = renameAndCompute(street, "Road", print);

    assertEquals(1, edits.size());
    assertEquals("/Person/Address/Road", mine.getPath());
    assertEquals("/Person/Address/Street", foreign.getPath());
  }

  @Test
  void aPrintCalculationStepFollowsTheRenameForTheReferencesToTheChangedModelOnly() {
    ComputationStep step = step("[Person_DM/Person/Address/Street] + [Other_DM/Person/Address/Street] + [Person_DM/Person/Name]");
    ComputationStep untouched = step("[Other_DM/Person/Address/Street]");
    ComputationStep blank = step(null);
    PrintModel print = printModel("Letter_Pt");
    print.getContent().getElementDefinitions().add(calculation(step, untouched, blank));

    List<ModelEdits> edits = renameAndCompute(street, "Road", print);

    assertEquals(1, edits.size());
    assertEquals("[Person_DM/Person/Address/Road] + [Other_DM/Person/Address/Street] + [Person_DM/Person/Name]",
        step.getOperation());
    assertEquals("[Other_DM/Person/Address/Street]", untouched.getOperation());
    assertNull(blank.getOperation());
  }

  @Test
  void aPrintCalculationStepIsRestoredByUndoingTheEdit() {
    ComputationStep step = step("[Person_DM/Person/Address/Street]");
    PrintModel print = printModel("Letter_Pt");
    print.getContent().getElementDefinitions().add(calculation(step));

    List<ModelEdits> edits = renameAndCompute(street, "Road", print);
    edits.get(0).edits().forEach(DocumentModelRefactoring.Edit::revert);

    assertEquals("[Person_DM/Person/Address/Street]", step.getOperation());
  }

  // ---- Query Model ------------------------------------------------------------------------------------------

  @Test
  void aQueryFollowsFieldsSortConstraintsAndFilterTextButNotLinkedModelsPaths() {
    QueryModel query = queryModel("Q", "Person_DM");
    QueryModelContent content = query.getContent();
    content.getFields().addAll(List.of("/Person/Name", "/Person/Address/Street"));

    QuerySort direct = new QuerySort();
    direct.getSortBy().setField("/Person/Address/Street");
    QuerySort throughRelationship = new QuerySort();
    throughRelationship.setRelationshipModel("Rel");
    throughRelationship.setTargetRole("Other");
    throughRelationship.getSortBy().setField("/Person/Address/Street");
    content.getSort().addAll(List.of(direct, throughRelationship));

    ExactMatchOperator exact = new ExactMatchOperator();
    exact.setField("/Person/Address/Street");
    DoubleRangeOperator range = new DoubleRangeOperator();
    range.setField("/Person/Name");
    ExactMatchOperator linked = new ExactMatchOperator();
    linked.setField("/Person/Address/Street");
    HasOperator has = new HasOperator();
    has.setConstraint(linked);
    NotOperator not = new NotOperator();
    not.setOperand(exact);
    AndOperator and = new AndOperator();
    and.setOperands(List.of(not, range, has));
    content.setConstraint(and);

    content.setFilterDefinition("[/Person/Address/Street] = \"[/Person/Address/Street]\" And [/Person/Name] > 1");

    List<ModelEdits> edits = renameAndCompute(street, "Road", query);

    assertEquals(1, edits.size());
    assertEquals(List.of("/Person/Name", "/Person/Address/Road"), content.getFields());
    assertEquals("/Person/Address/Road", direct.getSortBy().getField());
    assertEquals("/Person/Address/Street", throughRelationship.getSortBy().getField());
    assertEquals("/Person/Address/Road", exact.getField());
    assertEquals("/Person/Name", range.getField());
    assertEquals("/Person/Address/Street", linked.getField());
    assertEquals("[/Person/Address/Road] = \"[/Person/Address/Street]\" And [/Person/Name] > 1",
        content.getFilterDefinition());
  }

  @Test
  void aQueryAggregationFollowsTheRenameOfItsTargetModelsFieldsAndUndoRestoresThem() {
    QueryModel query = queryModel("Q", "Person_DM");
    QueryAggregation aggregation = new QueryAggregation();
    aggregation.getGroup().addAll(List.of(new QueryAggregationGroup("/Person/Address/Street"), new QueryAggregationGroup("/Person/Name")));
    aggregation.getAggregations().addAll(List.of(aggregationEntry("count", "/Person/Address/Street"), aggregationEntry("max", "/Person/Name")));
    query.getContent().setAggregation(aggregation);

    List<ModelEdits> edits = renameAndCompute(street, "Road", query);

    assertEquals(1, edits.size());
    assertEquals("/Person/Address/Road", aggregation.getGroup().get(0).getField());
    assertEquals("/Person/Name", aggregation.getGroup().get(1).getField());
    assertEquals("/Person/Address/Road", aggregation.getAggregations().get(0).getField());
    assertEquals("/Person/Name", aggregation.getAggregations().get(1).getField());

    revert(edits);
    assertEquals("/Person/Address/Street", aggregation.getGroup().get(0).getField());
    assertEquals("/Person/Address/Street", aggregation.getAggregations().get(0).getField());
  }

  @Test
  void aQueryAggregationOnAnotherModelIsNotTouched() {
    QueryModel query = queryModel("Q", "Other_DM");
    QueryAggregation aggregation = new QueryAggregation();
    aggregation.getGroup().add(new QueryAggregationGroup("/Person/Address/Street"));
    aggregation.getAggregations().add(aggregationEntry("count", "/Person/Address/Street"));
    query.getContent().setAggregation(aggregation);

    assertEquals(List.of(), renameAndCompute(street, "Road", query));
    assertEquals("/Person/Address/Street", aggregation.getGroup().get(0).getField());
    assertEquals("/Person/Address/Street", aggregation.getAggregations().get(0).getField());
  }

  private static QueryAggregationEntry aggregationEntry(String function, String field) {
    QueryAggregationEntry entry = new QueryAggregationEntry();
    entry.setFunction(function);
    entry.setField(field);
    return entry;
  }

  @Test
  void aQueryOnAnotherModelIsNotTouched() {
    QueryModel query = queryModel("Q", "Other_DM");
    query.getContent().getFields().add("/Person/Address/Street");
    query.getContent().setFilterDefinition("[/Person/Address/Street] = 1");
    QueryLink link = new QueryLink();
    link.getFields().add("/Person/Address/Street");
    query.getContent().getLinks().add(link);

    assertEquals(List.of(), renameAndCompute(street, "Road", query));
    assertEquals(List.of("/Person/Address/Street"), query.getContent().getFields());
  }

  // ---- Query Model: relationship hops, sorts through relationships, has ---------------------------------------
  //
  // "Owns" relates Order_DM (role "Order") to Person_DM (role "Owner"), with Link_DM as its link document.

  @Test
  void aQueryHopFollowsTheRenameWhenItsRolePlaysTheChangedModel() {
    QueryModel query = queryModel("Q", "Order_DM");
    QueryLink toPerson = hop("Owns", "Owner");
    toPerson.getFields().addAll(List.of("/Person/Name", "/Person/Address/Street"));
    QueryLink back = hop("Owns", "Order");
    back.getFields().add("/Person/Address/Street");
    query.getContent().getLinks().addAll(List.of(toPerson, back));

    List<ModelEdits> edits = renameAndCompute(street, "Road", query, owns());

    assertEquals(1, edits.size());
    assertEquals(List.of("/Person/Name", "/Person/Address/Road"), toPerson.getFields());
    assertEquals(List.of("/Person/Address/Street"), back.getFields());
  }

  @Test
  void aNestedQueryHopIsScopedByItsOwnRole() {
    QueryModel query = queryModel("Q", "Person_DM");
    query.getContent().getFields().add("/Person/Address/Street");
    QueryLink toOrder = hop("Owns", "Order");
    toOrder.getFields().add("/Person/Address/Street");
    QueryLink backToPerson = hop("Owns", "Owner");
    backToPerson.getFields().add("/Person/Address/Street");
    toOrder.getLinks().add(backToPerson);
    query.getContent().getLinks().add(toOrder);

    renameAndCompute(street, "Road", query, owns());

    assertEquals(List.of("/Person/Address/Road"), query.getContent().getFields());
    assertEquals(List.of("/Person/Address/Street"), toOrder.getFields());
    assertEquals(List.of("/Person/Address/Road"), backToPerson.getFields());
  }

  @Test
  void aQueryHopFollowsItsFilterAndConstraintButNotItsLinkFieldsWhichBelongToTheLinkModel() {
    QueryModel query = queryModel("Q", "Order_DM");
    QueryLink toPerson = hop("Owns", "Owner");
    toPerson.setFilterDefinition("[/Person/Address/Street] == \"a\"");
    ExactMatchOperator exact = new ExactMatchOperator();
    exact.setField("/Person/Address/Street");
    toPerson.setConstraint(exact);
    toPerson.getLinkDocumentFields().add("/Person/Address/Street");
    query.getContent().getLinks().add(toPerson);

    renameAndCompute(street, "Road", query, owns());

    assertEquals("[/Person/Address/Road] == \"a\"", toPerson.getFilterDefinition());
    assertEquals("/Person/Address/Road", exact.getField());
    assertEquals(List.of("/Person/Address/Street"), toPerson.getLinkDocumentFields());
  }

  @Test
  void aQuerySortThroughARelationshipFollowsTheModelOfItsRole() {
    QueryModel query = queryModel("Q", "Order_DM");
    QuerySort throughOwner = new QuerySort();
    throughOwner.setRelationshipModel("Owns");
    throughOwner.setTargetRole("Owner");
    throughOwner.getSortBy().setField("/Person/Address/Street");
    QuerySort throughOrder = new QuerySort();
    throughOrder.setRelationshipModel("Owns");
    throughOrder.setTargetRole("Order");
    throughOrder.getSortBy().setField("/Person/Address/Street");
    QuerySort direct = new QuerySort();
    direct.getSortBy().setField("/Person/Address/Street");
    query.getContent().getSort().addAll(List.of(throughOwner, throughOrder, direct));

    renameAndCompute(street, "Road", query, owns());

    assertEquals("/Person/Address/Road", throughOwner.getSortBy().getField());
    assertEquals("/Person/Address/Street", throughOrder.getSortBy().getField());
    assertEquals("/Person/Address/Street", direct.getSortBy().getField());
  }

  @Test
  void aHasOperatorConstraintFollowsTheModelOfItsRoleNotTheQueryTarget() {
    QueryModel query = queryModel("Q", "Order_DM");
    ExactMatchOperator inOwner = new ExactMatchOperator();
    inOwner.setField("/Person/Address/Street");
    ExactMatchOperator inLink = new ExactMatchOperator();
    inLink.setField("/Person/Address/Street");
    HasOperator has = new HasOperator();
    has.setRelationshipModel("Owns");
    has.setTargetRole("Owner");
    has.setConstraint(inOwner);
    has.setLinkDocumentConstraint(inLink);
    query.getContent().setConstraint(has);

    renameAndCompute(street, "Road", query, owns());

    assertEquals("/Person/Address/Road", inOwner.getField());
    assertEquals("/Person/Address/Street", inLink.getField());
  }

  @Test
  void aHasCallInTheFilterTextFollowsTheModelOfItsRoleNotTheEnclosingScope() {
    QueryModel query = queryModel("Q", "Person_DM");
    query.getContent().setFilterDefinition("[/Person/Address/Street] == \"a\" and "
        + "Has(\"Owns\", \"Order\", [/Person/Address/Street] == \"b\", Null)");
    QueryModel fromOrder = queryModel("Q2", "Order_DM");
    fromOrder.getContent().setFilterDefinition("[/Person/Address/Street] == \"a\" or "
        + "Has(\"Owns\", \"Owner\", [/Person/Address/Street] == \"b\", [/Person/Address/Street] == \"c\")");

    renameAndCompute(street, "Road", query, fromOrder, owns());

    assertEquals("[/Person/Address/Road] == \"a\" and "
        + "Has(\"Owns\", \"Order\", [/Person/Address/Street] == \"b\", Null)",
        query.getContent().getFilterDefinition());
    assertEquals("[/Person/Address/Street] == \"a\" or "
        + "Has(\"Owns\", \"Owner\", [/Person/Address/Road] == \"b\", [/Person/Address/Street] == \"c\")",
        fromOrder.getContent().getFilterDefinition());
  }

  @Test
  void aHopOrHasThatDoesNotResolveIsLeftAlone() {
    QueryModel query = queryModel("Q", "Order_DM");
    QueryLink unknownRelationship = hop("Gone", "Owner");
    unknownRelationship.getFields().add("/Person/Address/Street");
    QueryLink unknownRole = hop("Owns", "Nobody");
    unknownRole.getFields().add("/Person/Address/Street");
    query.getContent().getLinks().addAll(List.of(unknownRelationship, unknownRole));
    query.getContent().setFilterDefinition("Has(\"Gone\", \"Owner\", [/Person/Address/Street] == \"b\")");

    assertEquals(List.of(), renameAndCompute(street, "Road", query, owns()));
    assertEquals(List.of("/Person/Address/Street"), unknownRelationship.getFields());
    assertEquals(List.of("/Person/Address/Street"), unknownRole.getFields());
  }

  @Test
  void aFilterThatIsNotValidQueryLanguageStillFollowsUnlessItHasAHasCall() {
    QueryModel query = queryModel("Q", "Person_DM");
    query.getContent().setFilterDefinition("[/Person/Address/Street] = 1");
    QueryLink hop = hop("Owns", "Owner");
    hop.setFilterDefinition("Has(\"Owns\", \"Order\", [/Person/Address/Street] = 1");
    query.getContent().getLinks().add(hop);

    renameAndCompute(street, "Road", query, owns());

    assertEquals("[/Person/Address/Road] = 1", query.getContent().getFilterDefinition());
    assertEquals("Has(\"Owns\", \"Order\", [/Person/Address/Street] = 1", hop.getFilterDefinition());
  }

  @Test
  void aFilterPathIsRewrittenAtTheRightOffsetWithNonBmpCharactersBeforeIt() {
    QueryModel query = queryModel("Q", "Person_DM");
    query.getContent().setFilterDefinition("Match([/Person/Name], \"😀\") or "
        + "[/Person/Address/Street] == \"😀\"");

    renameAndCompute(street, "Road", query);

    assertEquals("Match([/Person/Name], \"😀\") or [/Person/Address/Road] == \"😀\"",
        query.getContent().getFilterDefinition());
  }

  @Test
  void revertingAHopEditRestoresIt() {
    QueryModel query = queryModel("Q", "Order_DM");
    QueryLink toPerson = hop("Owns", "Owner");
    toPerson.getFields().add("/Person/Address/Street");
    toPerson.setFilterDefinition("[/Person/Address/Street] == \"a\"");
    query.getContent().getLinks().add(toPerson);

    List<ModelEdits> edits = renameAndCompute(street, "Road", query, owns());
    revert(edits);

    assertEquals(List.of("/Person/Address/Street"), toPerson.getFields());
    assertEquals("[/Person/Address/Street] == \"a\"", toPerson.getFilterDefinition());
  }

  // ---- Mapping Model / Structural Mapping Model -------------------------------------------------------------

  @Test
  void aMappingSortFieldFollowsTheModelOfItsOwnSourceOnly() {
    SortField mine = sortField("/Person/Address/Street");
    SortField foreign = sortField("/Person/Address/Street");
    MappingModel mapping = mappingModel("M", "Target_DM", null, source("Person_DM", mine), source("Other_DM", foreign));

    List<ModelEdits> edits = renameAndCompute(street, "Road", mapping);

    assertEquals(1, edits.size());
    assertEquals("/Person/Address/Road", mine.getSortFieldFullName());
    assertEquals("/Person/Address/Street", foreign.getSortFieldFullName());
  }

  @Test
  void aStructuralMappingFollowsTheSideThatIsTheChangedModel() {
    FieldMapping fieldMapping = new FieldMapping();
    fieldMapping.setSourceFieldFullName("/Person/Address/Street");
    fieldMapping.setTargetFieldFullName("/Person/Address/Street");
    ResolutionStrategy strategy = new ResolutionStrategy();
    strategy.setSourceGroupFullName("/Person/Address");
    strategy.setTargetGroupFullName("/Person/Address");
    Slice slice = new Slice();
    slice.setSourceFieldFullName("/Person/Address/Street");
    slice.setTargetFieldFullName("/Person/Address/Street");
    strategy.setSlice(slice);
    GroupToClearOnFirstFill clear = new GroupToClearOnFirstFill();
    clear.setFullName("/Person/Address");
    StructuralMappingModel structural = structuralMapping("S_SMM", clear, strategy, fieldMapping);
    MappingModel mapping = mappingModel("M", "Target_DM", "S_SMM", source("Person_DM"));

    List<ModelEdits> edits = renameAndCompute(street, "Road", structural, mapping);

    assertEquals(1, edits.size());
    assertEquals("/Person/Address/Road", fieldMapping.getSourceFieldFullName());
    assertEquals("/Person/Address/Street", fieldMapping.getTargetFieldFullName());
    assertEquals("/Person/Address/Road", slice.getSourceFieldFullName());
    assertEquals("/Person/Address/Street", slice.getTargetFieldFullName());
    assertEquals("/Person/Address", strategy.getSourceGroupFullName());
    assertEquals("/Person/Address", clear.getFullName());
  }

  @Test
  void aStructuralMappingFollowsTheTargetSideAndItsGroupsToClear() {
    FieldMapping fieldMapping = new FieldMapping();
    fieldMapping.setSourceFieldFullName("/Person/Address/Street");
    fieldMapping.setTargetFieldFullName("/Person/Address/Street");
    GroupToClearOnFirstFill clear = new GroupToClearOnFirstFill();
    clear.setFullName("/Person/Address");
    StructuralMappingModel structural = structuralMapping("S_SMM", clear, null, fieldMapping);
    MappingModel mapping = mappingModel("M", "Person_DM", "S_SMM", source("Other_DM"));

    renameAndCompute(address, "Home", structural, mapping);

    assertEquals("/Person/Address/Street", fieldMapping.getSourceFieldFullName());
    assertEquals("/Person/Home/Street", fieldMapping.getTargetFieldFullName());
    assertEquals("/Person/Home", clear.getFullName());
  }

  @Test
  void aStructuralMappingUsedByMappingsThatDisagreeAboutTheModelIsLeftAlone() {
    FieldMapping fieldMapping = new FieldMapping();
    fieldMapping.setSourceFieldFullName("/Person/Address/Street");
    fieldMapping.setTargetFieldFullName("/Person/Address/Street");
    StructuralMappingModel structural = structuralMapping("S_SMM", null, null, fieldMapping);
    MappingModel one = mappingModel("M1", "Target_DM", "S_SMM", source("Person_DM"));
    MappingModel two = mappingModel("M2", "Target_DM", "S_SMM", source("Other_DM"));
    MappingModel unrelated = mappingModel("M3", "Target_DM", null, source("Person_DM"));

    assertEquals(List.of(), renameAndCompute(street, "Road", structural, one, two, unrelated));
    assertEquals("/Person/Address/Street", fieldMapping.getSourceFieldFullName());
  }

  // ---- Selection Model --------------------------------------------------------------------------------------

  @Test
  void aSelectionFollowsTheModelItsCombinationsAreBasedOn() {
    PathSpecification selected = path("/Person/Address/Street");
    PathSpecification unselected = path("/Person/Name");
    SelectionModel selection = selectionModel("Sel", List.of(selected), List.of(unselected));
    CombinedDocumentModel combined = combinationModel("C", "Person_DM", "Sel");

    List<ModelEdits> edits = renameAndCompute(address, "Home", selection, combined);

    assertEquals(1, edits.size());
    assertEquals("/Person/Home/Street", selected.getPath());
    assertEquals("/Person/Name", unselected.getPath());
  }

  @Test
  void aSelectionWithoutAnUnambiguousBaseModelIsLeftAlone() {
    PathSpecification selected = path("/Person/Address/Street");
    SelectionModel selection = selectionModel("Sel", List.of(selected), null);
    CombinedDocumentModel one = combinationModel("C1", "Person_DM", "Sel");
    CombinedDocumentModel two = combinationModel("C2", "Other_DM", "Sel");
    SelectionModel unused = selectionModel("Unused", List.of(path("/Person/Address/Street")), null);

    assertEquals(List.of(), renameAndCompute(street, "Road", selection, unused, one, two));
    assertEquals("/Person/Address/Street", selected.getPath());
  }

  // ---- undo -------------------------------------------------------------------------------------------------

  @Test
  void revertingTheEditsRestoresEveryModelAndApplyingThemAgainRedoesIt() {
    FieldRef ref = fieldRef("Person_DM", "/Person/Address/Street");
    PrintModel print = printModel("Letter_Pt", ref);
    QueryModel query = queryModel("Q", "Person_DM");
    query.getContent().getFields().add("/Person/Address/Street");

    List<ModelEdits> edits = renameAndCompute(street, "Road", print, query);
    assertEquals("/Person/Address/Road", ref.getPath());
    assertEquals(List.of("/Person/Address/Road"), query.getContent().getFields());

    revert(edits);
    assertEquals("/Person/Address/Street", ref.getPath());
    assertEquals(List.of("/Person/Address/Street"), query.getContent().getFields());

    apply(edits);
    assertEquals("/Person/Address/Road", ref.getPath());
    assertEquals(List.of("/Person/Address/Road"), query.getContent().getFields());
  }

  @Test
  void modelsThatHoldNoAffectedReferenceYieldNoEdits() {
    QueryModel query = queryModel("Q", "Person_DM");
    query.getContent().getFields().add("/Person/Name");

    List<ModelEdits> edits = renameAndCompute(street, "Road", query, personModel);

    assertTrue(edits.isEmpty());
  }

  // ---- helpers ----------------------------------------------------------------------------------------------

  /** Renames {@code element} of {@code Person_DM} and returns (and applies) the edits to {@code others}. */
  private List<ModelEdits> renameAndCompute(Element element, String newName, A12Model<?>... others) {
    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(personModel);
    element.setName(newName);
    List<A12Model<?>> project = new ArrayList<>(List.of(others));
    project.add(personModel);
    List<ModelEdits> edits = ProjectReferenceRefactoring.computeEdits(personModel, plan, project);
    apply(edits);
    return edits;
  }

  private static void apply(List<ModelEdits> edits) {
    edits.forEach(modelEdits -> modelEdits.edits().forEach(Edit::apply));
  }

  private static void revert(List<ModelEdits> edits) {
    for (int i = edits.size() - 1; i >= 0; i--) {
      List<Edit> modelEdits = edits.get(i).edits();
      for (int j = modelEdits.size() - 1; j >= 0; j--) {
        modelEdits.get(j).revert();
      }
    }
  }

  private static List<Element> children(GroupElement group) {
    return group.getGroup().getElements();
  }

  private static GroupElement group(String name, Element... elements) {
    GroupElement group = new GroupElement();
    group.setId("group_" + name);
    group.setName(name);
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    config.setElements(new ArrayList<>(List.of(elements)));
    group.setGroup(config);
    return group;
  }

  private static GroupElement includeGroup(String name, String referencedModelId) {
    GroupElement group = group(name);
    IncludeConfig includeConfig = new IncludeConfig();
    includeConfig.setReference(referencedModelId);
    group.getGroup().setIncludeConfig(includeConfig);
    return group;
  }

  private static FieldElement field(String name) {
    FieldElement field = new FieldElement();
    field.setId("field_" + name);
    field.setName(name);
    return field;
  }

  private static RuleElement rule(String name, String errorEntityRelPath, String condition) {
    RuleElement rule = new RuleElement();
    rule.setId("rule_" + name);
    rule.setName(name);
    RuleConfig config = new RuleConfig();
    config.setErrorEntityRelPath(errorEntityRelPath);
    config.setErrorCondition(condition);
    rule.setRule(config);
    return rule;
  }

  private static DocumentModel documentModel(String id, GroupElement... roots) {
    ModelRoot root = new ModelRoot();
    root.setRootGroups(new ArrayList<>(List.of(roots)));
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(root);
    DocumentModel model = new DocumentModel();
    model.setId(id);
    model.setContent(content);
    return model;
  }

  private static FieldRef fieldRef(String model, String path) {
    FieldRef ref = new FieldRef();
    ref.setModel(model);
    ref.setPath(path);
    return ref;
  }

  private static ComputationStep step(String operation) {
    ComputationStep step = new ComputationStep();
    step.setOperation(operation);
    return step;
  }

  private static PrintCalculationElement calculation(ComputationStep... steps) {
    Calculation calculation = new Calculation();
    calculation.setModel("Person_DM");
    calculation.setName("Calculation");
    calculation.getComputationAlternatives().addAll(List.of(steps));
    PrintCalculationElement element = new PrintCalculationElement();
    element.setType("Calculation");
    element.setCalculation(calculation);
    return element;
  }

  private static PrintModel printModel(String id, FieldRef... refs) {
    PrintModelContent content = new PrintModelContent();
    for (FieldRef ref : refs) {
      PrintFieldElement element = new PrintFieldElement();
      element.setType("Field");
      element.setField(ref);
      content.getElementDefinitions().add(element);
    }
    PrintModel model = new PrintModel();
    model.setId(id);
    model.setContent(content);
    return model;
  }

  private static QueryModel queryModel(String id, String targetDocumentModel) {
    QueryModelContent content = new QueryModelContent();
    content.setTargetDocumentModel(targetDocumentModel);
    QueryModel model = new QueryModel();
    model.setId(id);
    model.setContent(content);
    return model;
  }

  private static QueryLink hop(String relationshipModel, String targetRole) {
    QueryLink link = new QueryLink();
    link.setRelationshipModel(relationshipModel);
    link.setTargetRole(targetRole);
    return link;
  }

  /** {@code Owns}: Order_DM plays "Order", Person_DM plays "Owner"; Link_DM is the link document. */
  private static RelationshipModel owns() {
    RelationshipModelContent content = new RelationshipModelContent();
    content.setLinkDocumentModelValue("Link_DM");
    content.getEntityCharacteristics().add(entity("Order", "Order_DM"));
    content.getEntityCharacteristics().add(entity("Owner", "Person_DM"));
    RelationshipModel model = new RelationshipModel();
    model.setId("Owns");
    model.setContent(content);
    return model;
  }

  private static EntityCharacteristic entity(String role, String documentModel) {
    EntityCharacteristic entity = new EntityCharacteristic();
    entity.setRole(role);
    entity.setDocumentModel(documentModel);
    return entity;
  }

  private static SortField sortField(String path) {
    SortField sortField = new SortField();
    sortField.setSortFieldFullName(path);
    return sortField;
  }

  private static MappingSource source(String dmId, SortField... sortFields) {
    MappingSource source = new MappingSource();
    source.setDmId(dmId);
    if (sortFields.length > 0) {
      SortInfo sortInfo = new SortInfo();
      sortInfo.getSortFields().addAll(List.of(sortFields));
      source.setSortInfo(sortInfo);
    }
    return source;
  }

  private static MappingModel mappingModel(String id, String targetDmId, String structuralMappingId,
      MappingSource... sources) {
    MappingModelContent content = new MappingModelContent();
    content.getSource().addAll(List.of(sources));
    MappingTarget target = new MappingTarget();
    target.setDmId(targetDmId);
    content.setTarget(target);
    if (structuralMappingId != null) {
      StructuralMappingModelRef ref = new StructuralMappingModelRef();
      ref.setId(structuralMappingId);
      content.setStructuralMappingModel(ref);
    }
    MappingModel model = new MappingModel();
    model.setId(id);
    model.setContent(content);
    return model;
  }

  private static StructuralMappingModel structuralMapping(String id, GroupToClearOnFirstFill clear,
      ResolutionStrategy strategy, FieldMapping fieldMapping) {
    StructuralMappingModelContent content = new StructuralMappingModelContent();
    if (clear != null) {
      content.getGroupsToClearOnFirstFill().add(clear);
    }
    MappingBlock block = new MappingBlock();
    if (strategy != null) {
      block.getResolutionStrategies().add(strategy);
    }
    block.getFieldMappings().add(fieldMapping);
    content.getMappingBlocks().add(block);
    StructuralMappingModel model = new StructuralMappingModel();
    model.setId(id);
    model.setContent(content);
    return model;
  }

  private static PathSpecification path(String path) {
    PathSpecification specification = new PathSpecification();
    specification.setPath(path);
    return specification;
  }

  private static SelectionModel selectionModel(String id, List<PathSpecification> selected,
      List<PathSpecification> unselected) {
    SelectionModelContent content = new SelectionModelContent();
    content.getData().setSelected(selected == null ? null : new ArrayList<>(selected));
    content.getData().setUnselected(unselected == null ? null : new ArrayList<>(unselected));
    SelectionModel model = new SelectionModel();
    model.setId(id);
    model.setContent(content);
    return model;
  }

  private static AdditiveDocumentModel additiveModel(String id, GroupElement... roots) {
    DocumentModel plain = documentModel(id, roots);
    AdditiveDocumentModel model = new AdditiveDocumentModel();
    model.setId(id);
    model.setContent(plain.getContent());
    return model;
  }

  /** A Combination Model of {@code baseModelId} with one Addition step for {@code additiveModelId}. */
  private static CombinedDocumentModel additionCombination(String id, String baseModelId, String additiveModelId) {
    CombinationStep step = new CombinationStep();
    DocumentModelIdRef ref = new DocumentModelIdRef();
    ref.setDmId(additiveModelId);
    step.setAdditiveModel(ref);
    CombinedDocumentModelContent content = new CombinedDocumentModelContent();
    content.setBaseModelId(baseModelId);
    content.getCombinationSteps().add(step);
    CombinedDocumentModel model = new CombinedDocumentModel();
    model.setId(id);
    model.setContent(content);
    return model;
  }

  private static CombinedDocumentModel combinationModel(String id, String baseModelId, String selectionId) {
    CombinationStep step = new CombinationStep();
    SelectionModelIdRef ref = new SelectionModelIdRef();
    ref.setSmId(selectionId);
    step.setSelectionModel(ref);
    CombinedDocumentModelContent content = new CombinedDocumentModelContent();
    content.setBaseModelId(baseModelId);
    content.getCombinationSteps().add(step);
    CombinedDocumentModel model = new CombinedDocumentModel();
    model.setId(id);
    model.setContent(content);
    return model;
  }
}
