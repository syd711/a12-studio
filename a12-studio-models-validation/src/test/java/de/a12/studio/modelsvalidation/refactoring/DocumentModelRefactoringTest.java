package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.ComputationAlternative;
import de.a12.studio.models.documentmodel.ComputationConfig;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.ContentUniquenessCriterion;
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
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Plan;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the rewriting of path references when an element is renamed or moved. The model under test:
 * <pre>
 * /Shop
 *   Address            (Include)
 *   Order
 *     Total            (field)
 *     Customer
 *       Name           (field)
 *     Products*        (repeatable)
 *       Quantity, Price, Subtotal   (fields)
 *       PriceRule      (rule)   - errorEntityRelPath "../Price"
 *     Discounts*       (repeatable)
 *       Percent        (field)
 *       DiscountRule   (rule)   - errorEntityRelPath "../Percent"
 *       DiscountCalc   (computation)
 * </pre>
 */
class DocumentModelRefactoringTest {

  private final GroupElement shop = group("Shop", 1);
  private final GroupElement include = includeGroup("Address", "Address_DM");
  private final GroupElement order = group("Order", 1);
  private final FieldElement total = field("Total");
  private final GroupElement customer = group("Customer", 1);
  private final FieldElement customerName = field("Name");
  private final GroupElement products = group("Products", 10);
  private final FieldElement quantity = field("Quantity");
  private final FieldElement price = field("Price");
  private final FieldElement subtotal = field("Subtotal");
  private final RuleElement priceRule = rule("PriceRule", "../Price", "[Price] < 0");
  private final GroupElement discounts = group("Discounts", 10);
  private final FieldElement percent = field("Percent");
  private final RuleElement discountRule = rule("DiscountRule", "../Percent", "[Percent] > 100");
  private final ComputationElement discountCalc = computation("DiscountCalc", "../Percent");
  private final DocumentModel model = buildModel();

  private DocumentModel buildModel() {
    children(shop).addAll(List.of(include, order));
    children(order).addAll(List.of(total, customer, products, discounts));
    children(customer).add(customerName);
    children(products).addAll(List.of(quantity, price, subtotal, priceRule));
    children(discounts).addAll(List.of(percent, discountRule, discountCalc));

    ModelRoot root = new ModelRoot();
    root.setRootGroups(new ArrayList<>(List.of(shop)));
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(root);
    DocumentModel documentModel = new DocumentModel();
    documentModel.setId("Shop_DM");
    documentModel.setContent(content);
    return documentModel;
  }

  // ---- renames --------------------------------------------------------------------------------------------------

  @Test
  void renamingAFieldRewritesConditionRelPathAndMessageParameters() {
    discountRule.getRule().setErrorCondition("FieldFilled(Percent) And [Percent] > 100");
    discountRule.getRule().setErrorMessage(messages(
        "Discount $Percent$ of $Percent.value$ costs $$5 (item $#Discounts$) - $Percent->Category$"));

    rename(percent, "Rate");

    assertEquals("../Rate", discountRule.getRule().getErrorEntityRelPath());
    assertEquals("FieldFilled(Rate) And [Rate] > 100", discountRule.getRule().getErrorCondition());
    assertEquals("Discount $Rate$ of $Rate.value$ costs $$5 (item $#Discounts$) - $Rate->Category$",
        discountRule.getRule().getErrorMessage().get(0).getText());
    assertEquals("../Rate", discountCalc.getComputation().getComputedFieldRelPath());
  }

  @Test
  void renamingARepeatableGroupKeepsTheRepetitionMarkerAndCoversHavingClauses() {
    discountCalc.getComputation().setCommonPrecondition("FieldFilled(Percent)");
    discountCalc.getComputation().getComputationAlternatives().add(alternative(
        "FieldFilled(Percent)",
        "[Percent] * Sum(../Products*/Quantity Having [../Products/Price] == [$Percent])"));

    rename(products, "Items");

    ComputationAlternative alternative = discountCalc.getComputation().getComputationAlternatives().get(0);
    assertEquals("FieldFilled(Percent)", discountCalc.getComputation().getCommonPrecondition());
    assertEquals("FieldFilled(Percent)", alternative.getPrecondition());
    assertEquals("[Percent] * Sum(../Items*/Quantity Having [../Items/Price] == [$Percent])", alternative.getOperation());
  }

  @Test
  void renamingAnAncestorUpdatesAbsolutePathsButLeavesTheirStyle() {
    discountRule.getRule().setErrorCondition("[/Shop/Order/Products/Price] > [Percent]");

    rename(order, "Purchase");

    assertEquals("[/Shop/Purchase/Products/Price] > [Percent]", discountRule.getRule().getErrorCondition());
  }

  @Test
  void renamingTheRootGroupUpdatesAbsolutePathsInUniquenessCriteriaToo() {
    ContentUniquenessCriterion criterion = new ContentUniquenessCriterion();
    ContentUniquenessCriterion.Field criterionField = new ContentUniquenessCriterion.Field();
    criterionField.setFullName("/Shop/Order/Total");
    criterion.getFields().add(criterionField);
    model.getContent().getDocumentUniquenessCriteria().add(criterion);

    rename(shop, "Store");

    assertEquals("/Store/Order/Total", criterionField.getFullName());
  }

  @Test
  void aGroupsIndexFieldNameFollowsItsChildsRename() {
    discounts.getGroup().setIndexFieldName("Percent");

    rename(percent, "Rate");

    assertEquals("Rate", discounts.getGroup().getIndexFieldName());
  }

  @Test
  void aPathThroughAnIncludeKeepsTheIncludedTailAndFollowsTheIncludeGroup() {
    discountRule.getRule().setErrorCondition("FieldFilled(../../Address/Street) And FieldFilled(/Shop/Address/City)");

    rename(include, "HomeAddress");

    assertEquals("FieldFilled(../../HomeAddress/Street) And FieldFilled(/Shop/HomeAddress/City)",
        discountRule.getRule().getErrorCondition());
  }

  @Test
  void aNameThatCollidesWithAKeywordIsQuoted() {
    discountRule.getRule().setErrorCondition("[Percent] > 1");

    rename(percent, "Having");

    assertEquals("['Having'] > 1", discountRule.getRule().getErrorCondition());
  }

  // ---- moves ----------------------------------------------------------------------------------------------------

  @Test
  void movingAFieldRewritesReferencesFromOtherGroups() {
    discountRule.getRule().setErrorCondition("[../Products/Price] > [Percent]");
    priceRule.getRule().setErrorCondition("[Price] < 0 And FieldFilled(Subtotal)");

    move(price, order);

    assertEquals("[../Price] > [Percent]", discountRule.getRule().getErrorCondition());
    assertEquals("[../Price] < 0 And FieldFilled(Subtotal)", priceRule.getRule().getErrorCondition());
    // The rule sits in Products, its target field now in Order, one level up.
    assertEquals("../../Price", priceRule.getRule().getErrorEntityRelPath());
  }

  @Test
  void movingARuleReMeasuresItsRelativePathsFromTheNewGroup() {
    discountRule.getRule().setErrorCondition("[Percent] > [../Products/Price] And [$Percent] > 1");

    move(discountRule, order);

    // Order is now the Rule Group: Percent lives one group further down, Products directly below.
    assertEquals("[Discounts/Percent] > [Products/Price] And [$Discounts/Percent] > 1",
        discountRule.getRule().getErrorCondition());
    assertEquals("../Discounts/Percent", discountRule.getRule().getErrorEntityRelPath());
  }

  @Test
  void movingAGroupLeavesReferencesInsideItAlone() {
    discountRule.getRule().setErrorCondition("[Percent] > [../Products/Price]");

    move(discounts, customer);

    // Rule and Percent moved together, only the reference to the group they left changes.
    assertEquals("[Percent] > [../../Products/Price]", discountRule.getRule().getErrorCondition());
    assertEquals("../Percent", discountRule.getRule().getErrorEntityRelPath());
  }

  @Test
  void aTurningGroupNameIsKeptWhileTheReferenceStillGoesUp() {
    discountRule.getRule().setErrorCondition("[..Order/Total] > 1");

    move(total, customer);

    assertEquals("[..Order/Customer/Total] > 1", discountRule.getRule().getErrorCondition());
  }

  @Test
  void aTurningGroupNameIsDroppedWhenTheReferenceNoLongerGoesUp() {
    discountRule.getRule().setErrorCondition("[..Order/Total] > 1");

    move(discountRule, order);

    assertEquals("[Total] > 1", discountRule.getRule().getErrorCondition());
  }

  @Test
  void aRoundAboutPathToTheRuleGroupItselfIsRewrittenWhenTheGroupIsRenamed() {
    // "../Discounts" from inside Discounts is the rule group again, spelled by its own name - renaming it would
    // leave that dangling, and the only spelling left for "myself" is RuleGroup.
    discountRule.getRule().setErrorCondition("GroupFilled(../Discounts) And GroupFilled(RuleGroup)");

    rename(discounts, "Rebates");

    assertEquals("GroupFilled(RuleGroup) And GroupFilled(RuleGroup)", discountRule.getRule().getErrorCondition());
  }

  @Test
  void aPathToTheRuleGroupBecomesAnOrdinaryPathWhenTheRuleLeavesIt() {
    discountRule.getRule().setErrorCondition("GroupFilled(../Discounts) And GroupFilled(RuleGroup)");

    move(discountRule, order);

    // RuleGroup is by definition the rule's own group, so it now means Order; only the real path is rewritten.
    assertEquals("GroupFilled(Discounts) And GroupFilled(RuleGroup)", discountRule.getRule().getErrorCondition());
  }

  // ---- leaving text alone ---------------------------------------------------------------------------------------

  @Test
  void textThatIsNotAPathIsNeverTouchedAndOnlyChangedReferencesAreRewritten() {
    String condition = "  ;; Percent is the discount, not /Shop/Order/Products/Price\n"
        + "[Percent]   >  100 And   [Percent] != \"Percent/Price\"\n"
        + " And [/Shop/Order/Customer/Name] == \"\" And FieldFilled( ../Products/Quantity )";
    discountRule.getRule().setErrorCondition(condition);

    rename(customerName, "FullName");

    assertEquals(condition.replace("/Shop/Order/Customer/Name", "/Shop/Order/Customer/FullName"),
        discountRule.getRule().getErrorCondition());
  }

  @Test
  void aReferenceThatIsStillCorrectIsNotReformatted() {
    String condition = "FieldFilled(   ..Order/Total )";
    discountRule.getRule().setErrorCondition(condition);

    rename(customerName, "FullName");

    assertEquals(condition, discountRule.getRule().getErrorCondition());
  }

  @Test
  void aConditionThatDoesNotParseIsLeftAloneAndReported() {
    String broken = "FieldFilled(Percent";
    discountRule.getRule().setErrorCondition(broken);

    Plan plan = DocumentModelRefactoring.prepare(model);
    percent.setName("Rate");
    plan.computeEdits().forEach(Edit::apply);

    assertEquals(broken, discountRule.getRule().getErrorCondition());
    assertEquals(1, plan.skippedSites());
  }

  @Test
  void aDanglingReferenceStaysDanglingInsteadOfBeingGuessedAt() {
    discountRule.getRule().setErrorCondition("[Typo] > 1");

    rename(percent, "Rate");

    assertEquals("[Typo] > 1", discountRule.getRule().getErrorCondition());
  }

  // ---- undo -----------------------------------------------------------------------------------------------------

  @Test
  void revertingTheEditsRestoresEveryText() {
    discountRule.getRule().setErrorCondition("[Percent] > [../Products/Price]");
    discountRule.getRule().setErrorMessage(messages("$Percent.value$"));

    Plan plan = DocumentModelRefactoring.prepare(model);
    percent.setName("Rate");
    List<Edit> edits = plan.computeEdits();
    edits.forEach(Edit::apply);
    assertTrue(edits.size() >= 4, "condition, message, errorEntityRelPath (twice: rule and computation)");

    percent.setName("Percent");
    edits.forEach(Edit::revert);

    assertEquals("[Percent] > [../Products/Price]", discountRule.getRule().getErrorCondition());
    assertEquals("$Percent.value$", discountRule.getRule().getErrorMessage().get(0).getText());
    assertEquals("../Percent", discountRule.getRule().getErrorEntityRelPath());
    assertEquals("../Percent", discountCalc.getComputation().getComputedFieldRelPath());
  }

  // ---- helpers --------------------------------------------------------------------------------------------------

  private void rename(Element element, String newName) {
    Plan plan = DocumentModelRefactoring.prepare(model);
    element.setName(newName);
    plan.computeEdits().forEach(Edit::apply);
  }

  private void move(Element element, GroupElement target) {
    Plan plan = DocumentModelRefactoring.prepare(model);
    for (GroupElement candidate : List.of(shop, order, customer, products, discounts)) {
      children(candidate).remove(element);
    }
    children(target).add(element);
    plan.computeEdits().forEach(Edit::apply);
  }

  private static List<Element> children(GroupElement group) {
    return group.getGroup().getElements();
  }

  private static GroupElement group(String name, int repeatability) {
    GroupElement group = new GroupElement();
    group.setId("group_" + name);
    group.setName(name);
    GroupConfig config = new GroupConfig();
    config.setRepeatability(repeatability);
    group.setGroup(config);
    return group;
  }

  private static GroupElement includeGroup(String name, String referencedModelId) {
    GroupElement group = group(name, 1);
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

  private static ComputationElement computation(String name, String computedFieldRelPath) {
    ComputationElement computation = new ComputationElement();
    computation.setId("computation_" + name);
    computation.setName(name);
    ComputationConfig config = new ComputationConfig();
    config.setComputedFieldRelPath(computedFieldRelPath);
    computation.setComputation(config);
    return computation;
  }

  private static ComputationAlternative alternative(String precondition, String operation) {
    ComputationAlternative alternative = new ComputationAlternative();
    alternative.setPrecondition(precondition);
    alternative.setOperation(operation);
    return alternative;
  }

  private static List<Label> messages(String text) {
    Label label = new Label();
    label.setLocale("en");
    label.setText(text);
    return new ArrayList<>(List.of(label));
  }
}
