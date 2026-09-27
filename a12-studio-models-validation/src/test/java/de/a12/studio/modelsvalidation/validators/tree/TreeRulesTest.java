package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.Label;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.ExpandAllPopupElement;
import de.a12.studio.models.overviewmodel.Icon;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.overviewmodel.MultiSelectionElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.models.treemodel.RowActivation;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeModelContent;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.models.treemodel.TreeNodeActionGroup;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.models.treemodel.TreeNodeContextMenu;
import de.a12.studio.models.treemodel.TreeNodeInheritance;
import de.a12.studio.models.treemodel.TreeVirtualRoot;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One test group per tree validator added for the gaps of "Tree Model: full gap review against SME 13.0.2": each builds the
 * small tree, Document Models and relationships it needs and checks what SME's rule reports for it. Person is abstract with
 * the sub types Employee and Freelancer; Team and Person are related by TeamPerson (Team role "Team", Person role
 * "Member").
 */
class TreeRulesTest {

  private static final String ITEM_DM = "{\"header\": {\"id\": \"Item_DM\", \"modelType\": \"document\", \"modelVersion\": \"29.4.0\", "
      + "\"locales\": [{\"code\": \"en\"}]}, \"content\": {\"modelInfo\": {\"name\": \"Item_DM\", \"immutable\": false}, "
      + "\"modelConfig\": {\"timeZone\": \"UTC\", \"decimalSeparator\": \".\", \"conditionLanguage\": {\"code\": \"en_US\"}}, "
      + "\"modelRoot\": {\"rootGroups\": [{\"type\": \"Group\", \"id\": \"g_root\", \"name\": \"Root\", \"Group\": {\"repeatability\": 1, "
      + "\"elements\": [{\"type\": \"Field\", \"id\": \"f_name\", \"name\": \"Name\", \"Field\": {\"fieldType\": {\"type\": \"StringType\"}}}, "
      + "{\"type\": \"Field\", \"id\": \"f_hidden\", \"name\": \"Hidden\", \"annotations\": [{\"name\": \"indexed\", \"value\": \"false\"}], "
      + "\"Field\": {\"fieldType\": {\"type\": \"StringType\"}}}, "
      + "{\"type\": \"Group\", \"id\": \"g_rep\", \"name\": \"Rep\", \"Group\": {\"repeatability\": 9999, \"elements\": ["
      + "{\"type\": \"Field\", \"id\": \"f_rep\", \"name\": \"InRep\", \"Field\": {\"fieldType\": {\"type\": \"StringType\"}}}]}}]}}]}}}";

  // ---------------------------------------------------------------- root / screen reader column, virtual scrolling

  @Test
  void theRootAndTheScreenReaderColumnMustExist() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    team.getChildRelationshipConfigurations().add(configuration("crc-1", "TeamPerson_Re", "Team"));
    tree.getContent().getNodes().add(team);
    tree.getContent().getColumns().add(column("column-1", "Name"));
    tree.getContent().getConfiguration().setRootRef("crc-1");
    tree.getContent().getConfiguration().setScreenReaderColumnRef("column-1");
    assertEquals(0, run(new TreeRootRefValidator(), tree).size());

    tree.getContent().getConfiguration().setRootRef("crc-gone");
    tree.getContent().getConfiguration().setScreenReaderColumnRef("column-gone");
    List<ModelValidationError> errors = run(new TreeRootRefValidator(), tree);
    assertEquals(2, errors.size());
    assertTrue(errors.get(0).message().contains("crc-gone"));
    assertTrue(errors.get(1).message().contains("column-gone"));
  }

  @Test
  void virtualScrollingNeedsARowHeightAndAnActionColumnWidth() {
    TreeModel tree = tree();
    TreeConfiguration configuration = tree.getContent().getConfiguration();
    assertEquals(0, run(new TreeVirtualScrollingValidator(), tree).size(), "nothing is required while it is off");

    configuration.setEnableVirtualScroll(true);
    List<ModelValidationError> errors = run(new TreeVirtualScrollingValidator(), tree);
    assertEquals(2, errors.size());
    assertTrue(errors.get(0).message().contains("Row Height"));
    assertTrue(errors.get(1).message().contains("Action Column Width"));

    configuration.setRowHeight(49);
    configuration.setActionColumnWidth(1.0);
    assertEquals(0, run(new TreeVirtualScrollingValidator(), tree).size());

    configuration.setRowHeight(0);
    configuration.setActionColumnWidth(0.1);
    configuration.setEnableVirtualScroll(null);
    assertEquals(2, run(new TreeVirtualScrollingValidator(), tree).size(), "the minimums hold without virtual scrolling too");
  }

  // ---------------------------------------------------------------- multi-selection element

  @Test
  void multiSelectionAndItsSubheaderElementBelongTogether() {
    TreeModel tree = tree();
    tree.getContent().getSubHeaderBox().getRightSlot().add(new MultiSelectionElement());
    List<ModelValidationError> notEnabled = run(new TreeMultiSelectionElementValidator(), tree);
    assertEquals(1, notEnabled.size());
    assertTrue(notEnabled.get(0).message().contains("not allowed"));

    tree.getContent().getConfiguration().setMultiSelection(new MultiSelectionConfig());
    assertEquals(0, run(new TreeMultiSelectionElementValidator(), tree).size());

    tree.getContent().getSubHeaderBox().getLeftSlot().add(new MultiSelectionElement());
    assertTrue(run(new TreeMultiSelectionElementValidator(), tree).get(0).message().contains("Only one"));

    tree.getContent().getSubHeaderBox().getLeftSlot().clear();
    tree.getContent().getSubHeaderBox().getRightSlot().clear();
    assertTrue(run(new TreeMultiSelectionElementValidator(), tree).get(0).message().contains("No Multi-Selection"));
  }

  // ---------------------------------------------------------------- styles

  @Test
  void stylesAreUniqueAndEveryUsedStyleIsDefined() {
    TreeModel tree = tree();
    tree.getContent().getStyles().addAll(List.of("s1", "s2", "s1", " "));
    TreeNode node = node("node-1", "Team");
    node.getStyles().addAll(List.of("s1", "unknown", "s1"));
    tree.getContent().getNodes().add(node);
    TreeColumn column = column("column-1", "Name");
    column.setStyles(new de.a12.studio.models.overviewmodel.ColumnStyles());
    column.getStyles().getHeader().add("missing");
    tree.getContent().getColumns().add(column);
    ButtonElement button = new ButtonElement();
    button.getStyles().add("gone");
    tree.getContent().getFooterBox().getLeftSlot().add(button);

    List<String> messages = run(new TreeStylesValidator(), tree).stream().map(ModelValidationError::message).toList();

    assertTrue(messages.stream().anyMatch(message -> message.contains("must not be empty")), messages.toString());
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"s1\" is defined more than once")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"unknown\"") && message.contains("node type \"Team\"")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"s1\" is used more than once")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"missing\"") && message.contains("header styles of column \"Name\"")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"gone\"") && message.contains("footer")));
  }

  // ---------------------------------------------------------------- columns

  @Test
  void aColumnNeedsANameAWidthOfAtLeastPointThreeAndAHeader() {
    TreeModel tree = tree();
    TreeColumn nameless = column("column-1", null);
    nameless.setWidth(null);
    TreeColumn narrow = column("column-2", "Narrow");
    narrow.setWidth(0.2);
    TreeColumn hiddenLabel = column("column-3", "Hidden");
    hiddenLabel.setLabelHidden(true);
    TreeColumn withIcon = column("column-4", "Icon");
    withIcon.getLabel().clear();
    Icon icon = new Icon();
    icon.setName("group");
    withIcon.setIcon(icon);
    TreeColumn fine = column("column-5", "Fine");
    tree.getContent().getColumns().addAll(List.of(nameless, narrow, hiddenLabel, withIcon, fine));

    List<ModelValidationError> errors = run(new TreeColumnValidator(), tree);

    List<ModelValidationError> hard = errors.stream().filter(error -> error.severity().equals(Severity.ERROR.name())).toList();
    assertEquals(3, hard.size(), "name and width of the nameless column, the width of the narrow one: " + errors);
    assertTrue(hard.stream().anyMatch(error -> error.message().contains("Name of column \"column-1\"")));
    assertTrue(hard.stream().anyMatch(error -> error.message().contains("Width of column \"column-1\" is required")));
    assertTrue(hard.stream().anyMatch(error -> error.message().contains("Width of column \"Narrow\" must be at least")));
    List<ModelValidationError> warnings = errors.stream().filter(error -> error.severity().equals(Severity.WARNING.name())).toList();
    assertEquals(1, warnings.size(), "only the column whose label is hidden has an empty header: " + warnings);
    assertTrue(warnings.get(0).message().contains("\"Hidden\""));
    assertTrue(TreeColumnValidator.isHeaderEmpty(hiddenLabel));
    assertTrue(!TreeColumnValidator.isHeaderEmpty(fine));
    assertTrue(!TreeColumnValidator.isHeaderEmpty(withIcon));
  }

  // ---------------------------------------------------------------- column mapping

  @Test
  void columnMappingsAreCheckedAgainstTheNodesDocumentModel() throws Exception {
    DocumentModel item = documentModel(ITEM_DM);
    TreeModel tree = tree();
    tree.getContent().getColumns().addAll(List.of(column("column-1", "Name"), column("column-2", "Other"),
        column("column-3", "Third"), column("column-4", "Fourth")));
    TreeNode node = node("node-1", "Item_DM");
    node.getColumns().addAll(List.of(mapping("column-1", "f_name"), mapping("column-1", "f_rep"), mapping("column-2", "f_hidden"),
        mapping("column-9", "f_name"), mapping("column-3", "f_gone"), mapping("column-4", null)));
    tree.getContent().getNodes().add(node);

    List<String> messages = run(new TreeColumnFieldValidator(), tree, item).stream().map(ModelValidationError::message).toList();

    assertTrue(messages.stream().anyMatch(message -> message.contains("\"column-1\" is mapped more than once")), messages.toString());
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"InRep\"") && message.contains("repeatable")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"Hidden\"") && message.contains("indexed")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"column-9\"")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"f_gone\"")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("A field must be selected")));
    assertEquals(6, messages.size(), messages.toString());
  }

  @Test
  void childRelationshipColumnsAreCheckedAgainstTheLinkDocumentModel() throws Exception {
    DocumentModel item = documentModel(ITEM_DM);
    RelationshipModel link = relationship("TeamItem_Re", entity("Team", "Team"), entity("Item", "Person"));
    link.getContent().setLinkDocumentModelValue("Item_DM");
    TreeModel tree = tree();
    tree.getContent().getColumns().add(column("column-1", "Name"));
    TreeNode team = node("node-1", "Team");
    TreeChildRelationshipConfiguration configuration = configuration("crc-1", "TeamItem_Re", "Team");
    configuration.getOrCreateColumns().addAll(List.of(mapping("column-1", "f_name"), mapping("column-x", "f_rep")));
    team.getChildRelationshipConfigurations().add(configuration);
    tree.getContent().getNodes().add(team);

    List<String> messages = run(new TreeColumnFieldValidator(), tree, item, link).stream().map(ModelValidationError::message).toList();

    assertTrue(messages.stream().anyMatch(message -> message.contains("\"column-x\"") && message.contains("child relationship configuration")), messages.toString());
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"InRep\"") && message.contains("repeatable")));
    assertEquals(2, messages.size());
  }

  @Test
  void aCombinationModelIsAValidNodeDocumentModel() {
    TreeModel tree = tree();
    tree.getContent().getNodes().add(node("node-1", "Employee_Cm"));
    CombinedDocumentModel combination = combination("Employee_Cm", "Person");

    assertEquals(0, run(new TreeDocumentModelReferenceValidator(), tree, combination).size());
    assertEquals(1, run(new TreeDocumentModelReferenceValidator(), tree).size(), "still an error while it does not exist");
  }

  // ---------------------------------------------------------------- node structure

  @Test
  void aNodeTypeNeedsAColumnUnlessItInheritsOrAParentMapsSome() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    team.getChildRelationshipConfigurations().add(configuration("crc-1", "TeamPerson_Re", "Team"));
    TreeNode employee = node("node-2", "Employee");
    tree.getContent().getNodes().addAll(List.of(team, employee));
    tree.getContent().getColumns().add(column("column-1", "Name"));
    team.getColumns().add(mapping("column-1", "f"));

    List<ModelValidationError> errors = run(new TreeNodeStructureValidator(), tree, world());
    assertTrue(errors.stream().anyMatch(error -> error.message().contains("Node type \"Employee\" must have at least 1 column")), errors.toString());
    assertTrue(errors.stream().noneMatch(error -> error.message().contains("Node type \"Team\" must have")));

    // a parent's child relationship configuration that maps a column makes up for it
    team.getChildRelationshipConfigurations().get(0).getOrCreateColumns().add(mapping("column-1", "f"));
    assertTrue(run(new TreeNodeStructureValidator(), tree, world()).stream().noneMatch(error -> error.message().contains("must have at least 1 column")));

    // ... and so does inheriting the columns
    team.getChildRelationshipConfigurations().get(0).getColumns().clear();
    TreeNodeInheritance.setInherited(employee, TreeNodeInheritance.Part.COLUMNS, true);
    assertTrue(run(new TreeNodeStructureValidator(), tree, world()).stream().noneMatch(error -> error.message().contains("must have at least 1 column")));
  }

  @Test
  void inheritingNeedsASuperTypeNodeAndARelationshipIsUsedOnce() {
    TreeModel tree = tree();
    TreeNode person = node("node-1", "Person");
    TreeNode employee = node("node-2", "Employee");
    TreeNode team = node("node-3", "Team");
    TreeNodeInheritance.setInherited(employee, TreeNodeInheritance.Part.ICON, true);
    TreeNodeInheritance.setInherited(team, TreeNodeInheritance.Part.STYLES, true);
    team.getChildRelationshipConfigurations().add(configuration("crc-1", "TeamPerson_Re", "Team"));
    team.getChildRelationshipConfigurations().add(configuration("crc-2", "TeamPerson_Re", "Team"));
    tree.getContent().getNodes().addAll(List.of(person, employee, team));

    List<String> messages = run(new TreeNodeStructureValidator(), tree, world()).stream().map(ModelValidationError::message).toList();

    assertTrue(messages.stream().noneMatch(message -> message.contains("Employee") && message.contains("no parent")), "Person is a super type node");
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"Team\" has no parent node type")), messages.toString());
    assertTrue(messages.stream().anyMatch(message -> message.contains("relationship \"TeamPerson_Re\" already exists")));

    TreeNodeInheritance.setInherited(team, TreeNodeInheritance.Part.CHILD_RELATIONSHIP_CONFIGURATIONS, true);
    assertTrue(run(new TreeNodeStructureValidator(), tree, world()).stream().noneMatch(error -> error.message().contains("already exists")),
        "inherited relationships are not checked");
  }

  @Test
  void nodeTypesThatLeadBackToEachOtherWarnAndAChildMappingMayNotBeMappedAgain() {
    TreeModel tree = tree();
    TreeNode a = node("node-1", "A");
    a.getChildRelationshipConfigurations().add(configuration("crc-1", "AB_Re", "Parent"));
    TreeNode b = node("node-2", "B");
    b.getChildRelationshipConfigurations().add(configuration("crc-2", "BA_Re", "Parent"));
    tree.getContent().getNodes().addAll(List.of(a, b));
    tree.getContent().getColumns().add(column("column-1", "Name"));

    List<ModelValidationError> errors = run(new TreeNodeStructureValidator(), tree, world());
    assertEquals(2, errors.stream().filter(error -> error.severity().equals(Severity.WARNING.name()) && error.message().contains("circular")).count(), errors.toString());

    a.getChildRelationshipConfigurations().get(0).getOrCreateColumns().add(mapping("column-1", "f"));
    b.getColumns().add(mapping("column-1", "f"));
    assertTrue(run(new TreeNodeStructureValidator(), tree, world()).stream()
        .anyMatch(error -> error.message().contains("already defined in the column mapping of a child node type")));
  }

  // ---------------------------------------------------------------- child relationships

  @Test
  void aChildRelationshipMustExistFitTheNodeAndHaveAParentRole() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    team.getChildRelationshipConfigurations().add(configuration("crc-1", "Gone_Re", "Team"));
    team.getChildRelationshipConfigurations().add(configuration("crc-2", "AB_Re", "Parent"));
    team.getChildRelationshipConfigurations().add(configuration("crc-3", "TeamPerson_Re", "Wrong"));
    team.getChildRelationshipConfigurations().add(configuration("crc-4", "TeamPerson_Re", null));
    team.getChildRelationshipConfigurations().add(configuration("crc-5", null, null));
    tree.getContent().getNodes().addAll(List.of(team, node("node-2", "Person")));

    List<String> messages = run(new TreeChildRelationshipValidator(), tree, world()).stream().map(ModelValidationError::message).toList();

    assertTrue(messages.stream().anyMatch(message -> message.contains("\"Gone_Re\"") && message.contains("does not exist")), messages.toString());
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"AB_Re\"") && message.contains("no entity of the document model \"Team\"")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"Wrong\"") && message.contains("not a role")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("A parent role must be selected")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("A relationship must be selected")));
  }

  @Test
  void theChildSideNeedsNodeTypesAndPartialCoverageWarns() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    team.getChildRelationshipConfigurations().add(configuration("crc-1", "TeamPerson_Re", "Team"));
    tree.getContent().getNodes().add(team);

    List<ModelValidationError> none = run(new TreeChildRelationshipValidator(), tree, world());
    assertEquals(1, none.size(), none.toString());
    assertEquals(Severity.ERROR.name(), none.get(0).severity());
    assertTrue(none.get(0).message().contains("At least one node type"));

    tree.getContent().getNodes().add(node("node-2", "Employee"));
    List<ModelValidationError> partial = run(new TreeChildRelationshipValidator(), tree, world());
    assertEquals(1, partial.size(), partial.toString());
    assertEquals(Severity.WARNING.name(), partial.get(0).severity());
    assertTrue(partial.get(0).message().contains("Missing node type"));

    tree.getContent().getNodes().add(node("node-3", "Freelancer"));
    assertEquals(0, run(new TreeChildRelationshipValidator(), tree, world()).size());
  }

  @Test
  void aCombinationModelWithASuperTypeCoversItsSuperType() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    team.getChildRelationshipConfigurations().add(configuration("crc-1", "TeamPerson_Re", "Team"));
    tree.getContent().getNodes().addAll(List.of(team, node("node-2", "Employee"), node("node-3", "Freelancer_Cm")));
    List<A12Model<?>> models = new java.util.ArrayList<>(world());
    models.removeIf(model -> "Freelancer".equals(model.getId()));
    models.add(combination("Freelancer_Cm", "Person"));

    assertEquals(0, run(new TreeChildRelationshipValidator(), tree, models).size(),
        "the Combination Model is a sub type of Person, as SME's sub type graph has it");
    assertEquals(1, run(new TreeChildRelationshipValidator(), tree, world()).size(), "the Freelancer document model is still uncovered");
  }

  // ---------------------------------------------------------------- actions, buttons, row activation

  @Test
  void anActionNeedsItsEventOrPositionAndAKnownDocumentModel() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    team.getChildRelationshipConfigurations().add(configuration("crc-1", "TeamPerson_Re", "Team"));
    team.getActions().add(action(TreeNodeAction.TYPE_EVENT, null, null));
    team.getActions().add(action(TreeNodeAction.TYPE_INSERT, null, null));
    TreeNodeAction insert = action(TreeNodeAction.TYPE_INSERT, TreeNodeAction.POSITION_AS_CHILD, "Team");
    team.getActions().add(insert);
    TreeNodeAction validInsert = action(TreeNodeAction.TYPE_INSERT, TreeNodeAction.POSITION_AS_CHILD, "Employee");
    team.getActions().add(validInsert);
    TreeNodeAction untyped = new TreeNodeAction();
    team.getActions().add(untyped);
    tree.getContent().getNodes().add(team);

    List<String> messages = run(new TreeActionsValidator(), tree, world()).stream().map(ModelValidationError::message).toList();

    assertTrue(messages.stream().anyMatch(message -> message.contains("The Event of an event action")), messages.toString());
    assertTrue(messages.stream().anyMatch(message -> message.contains("The Position of an insert action")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"Team\" is not a valid choice")), "Team is no child of Team");
    assertTrue(messages.stream().anyMatch(message -> message.contains("The Type of an action")));
    assertEquals(4, messages.size(), "the insert of Employee is valid: " + messages);
  }

  @Test
  void copyAndPasteEventsAreNotAvailableWithALinkDocumentModel() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    team.getChildRelationshipConfigurations().add(configuration("crc-1", "TeamLink_Re", "Team"));
    team.getActions().add(action(TreeNodeAction.TYPE_EVENT, null, null));
    team.getActions().get(0).setEvent("event_paste_below");
    RowActivation activation = new RowActivation();
    activation.setType(RowActivation.TYPE_EVENT);
    activation.setEvent("event_cut_node");
    team.setRowActivation(activation);
    tree.getContent().getNodes().add(team);
    ButtonElement paste = new ButtonElement();
    paste.setEvent("event_paste");
    tree.getContent().getSubHeaderBox().getLeftSlot().add(paste);
    RelationshipModel link = relationship("TeamLink_Re", entity("Team", "Team"), entity("Member", "Person"));
    link.getContent().setLinkDocumentModelValue("Link_DM");
    List<A12Model<?>> models = new java.util.ArrayList<>(world());
    models.add(link);

    List<String> messages = run(new TreeActionsValidator(), tree, models.toArray(new A12Model<?>[0])).stream().map(ModelValidationError::message).toList();
    assertEquals(3, messages.stream().filter(message -> message.contains("link document model")).count(), messages.toString());

    assertEquals(0, run(new TreeActionsValidator(), tree, world().toArray(new A12Model<?>[0])).size(),
        "without a link document model the copy/paste events are fine");
  }

  @Test
  void aButtonNeedsAnEventAndAnnotationNamesAreUnique() {
    TreeModel tree = tree();
    ButtonElement noEvent = new ButtonElement();
    tree.getContent().getFooterBox().getRightSlot().add(noEvent);
    ButtonElement annotated = new ButtonElement();
    annotated.setEvent("event_add_root_node");
    annotated.getAnnotations().addAll(List.of(annotation("a"), annotation("a"), annotation("b")));
    tree.getContent().getSubHeaderBox().getLeftSlot().add(annotated);
    tree.getContent().getSubHeaderBox().getLeftSlot().add(new ExpandAllPopupElement());
    tree.getContent().getConfiguration().setMultiSelection(new MultiSelectionConfig());
    Button multi = new Button();
    tree.getContent().getConfiguration().getMultiSelection().getButtons().add(multi);

    List<String> messages = run(new TreeActionsValidator(), tree).stream().map(ModelValidationError::message).toList();

    assertTrue(messages.stream().anyMatch(message -> message.contains("button of the footer")), messages.toString());
    assertTrue(messages.stream().anyMatch(message -> message.contains("button of the multi-selection buttons")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("annotation \"a\"")));
    assertEquals(3, messages.size(), "the Expand All PopUp element needs no event: " + messages);
  }

  @Test
  void contextMenuGroupsNeedANameAnActionAndAnAddGroupOnlyInserts() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    TreeNodeContextMenu menu = new TreeNodeContextMenu();
    menu.getGroups().add(group("Empty", null));
    TreeNodeActionGroup add = group("Add", TreeNodeActionGroup.TYPE_ADD);
    add.getActions().add(action(TreeNodeAction.TYPE_EVENT, null, null));
    add.getActions().get(0).setEvent("event_delete_node");
    menu.getGroups().add(add);
    menu.getGroups().add(group(null, null));
    team.setContextMenu(menu);
    tree.getContent().getNodes().add(team);

    List<String> messages = run(new TreeActionsValidator(), tree, world()).stream().map(ModelValidationError::message).toList();

    assertTrue(messages.stream().anyMatch(message -> message.contains("group \"Empty\"") && message.contains("at least one action")), messages.toString());
    assertTrue(messages.stream().anyMatch(message -> message.contains("Only the type \"insert\"")));
    assertTrue(messages.stream().anyMatch(message -> message.contains("needs a Name")));
  }

  @Test
  void aRowActivationNeedsWhatItsTypeRequires() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    RowActivation activation = new RowActivation();
    activation.setType(RowActivation.TYPE_EVENT);
    team.setRowActivation(activation);
    TreeNode person = node("node-2", "Person");
    RowActivation insert = new RowActivation();
    insert.setType(RowActivation.TYPE_INSERT);
    person.setRowActivation(insert);
    TreeNode employee = node("node-3", "Employee");
    RowActivation nonInteractive = new RowActivation();
    nonInteractive.setType(RowActivation.TYPE_NON_INTERACTIVE);
    employee.setRowActivation(nonInteractive);
    tree.getContent().getNodes().addAll(List.of(team, person, employee));

    List<String> messages = run(new TreeActionsValidator(), tree, world()).stream().map(ModelValidationError::message).toList();

    assertTrue(messages.stream().anyMatch(message -> message.contains("Event of the row activation of node type \"Team\"")), messages.toString());
    assertTrue(messages.stream().anyMatch(message -> message.contains("Position of the row activation of node type \"Person\"")));
    assertEquals(2, messages.size(), "a non interactive row needs nothing: " + messages);
  }

  @Test
  void theVirtualRootNeedsLabelTextsAndItsInsertsCreateTheRootNodeType() {
    TreeModel tree = tree();
    TreeNode team = node("node-1", "Team");
    team.getChildRelationshipConfigurations().add(configuration("crc-1", "TeamPerson_Re", "Team"));
    tree.getContent().getNodes().add(team);
    tree.getContent().getConfiguration().setRootRef("crc-1");
    TreeVirtualRoot root = new TreeVirtualRoot();
    Label empty = new Label();
    empty.setLocale("en");
    root.getLabel().add(empty);
    root.getActions().add(action(TreeNodeAction.TYPE_INSERT, null, "Person"));
    root.getActions().add(action(TreeNodeAction.TYPE_INSERT, null, "Team"));
    tree.getContent().getConfiguration().setVirtualRoot(root);

    assertTrue(run(new TreeVirtualRootValidator(), tree).get(0).message().contains("locale \"en\""));
    List<String> messages = run(new TreeActionsValidator(), tree, world()).stream().map(ModelValidationError::message).toList();
    assertTrue(messages.stream().anyMatch(message -> message.contains("\"Person\"") && message.contains("Virtual Root")), messages.toString());
    assertEquals(1, messages.size(), "the root node type Team is a valid insert, no position is needed: " + messages);
  }

  // ---------------------------------------------------------------- helpers

  private List<A12Model<?>> world() {
    return new java.util.ArrayList<>(List.of(
        documentModel("Team", null, false), documentModel("Person", null, true), documentModel("Employee", "Person", false),
        documentModel("Freelancer", "Person", false), documentModel("A", null, false), documentModel("B", null, false),
        relationship("TeamPerson_Re", entity("Team", "Team"), entity("Member", "Person")),
        relationship("AB_Re", entity("Parent", "A"), entity("Child", "B")),
        relationship("BA_Re", entity("Parent", "B"), entity("Child", "A"))));
  }

  private static List<ModelValidationError> run(ModelValidator validator, TreeModel tree, A12Model<?>... others) {
    return validator.validate(tree, TestModels.contextWithOtherModels(tree, others));
  }

  private static List<ModelValidationError> run(ModelValidator validator, TreeModel tree, List<A12Model<?>> others) {
    return run(validator, tree, others.toArray(new A12Model<?>[0]));
  }

  private static TreeModel tree() {
    TreeModel tree = new TreeModel();
    tree.setId("Tree_Tr");
    TreeModelContent content = new TreeModelContent();
    content.setConfiguration(new TreeConfiguration());
    content.setSubHeaderBox(ElementBox.createEmpty());
    content.setFooterBox(ElementBox.createEmpty());
    tree.setContent(content);
    return tree;
  }

  private static TreeNode node(String id, String documentModel) {
    TreeNode node = new TreeNode();
    node.setId(id);
    node.setDocumentModelRef(documentModel);
    return node;
  }

  private static TreeColumn column(String id, String name) {
    TreeColumn column = new TreeColumn();
    column.setId(id);
    column.setName(name);
    column.setWidth(1.0);
    Label label = new Label();
    label.setLocale("en");
    label.setText(name == null ? "x" : name);
    column.getLabel().add(label);
    return column;
  }

  private static TreeNodeColumn mapping(String columnRef, String elementRef) {
    TreeNodeColumn mapping = new TreeNodeColumn();
    mapping.setColumnRef(columnRef);
    mapping.setElementRef(elementRef);
    return mapping;
  }

  private static TreeChildRelationshipConfiguration configuration(String id, String relationship, String parentRole) {
    TreeChildRelationshipConfiguration configuration = new TreeChildRelationshipConfiguration();
    configuration.setId(id);
    configuration.setRelationshipModelRef(relationship);
    configuration.setParentRole(parentRole);
    return configuration;
  }

  private static TreeNodeAction action(String type, String position, String documentModel) {
    TreeNodeAction action = new TreeNodeAction();
    action.setType(type);
    action.setPosition(position);
    action.setDocumentModelRef(documentModel);
    return action;
  }

  private static TreeNodeActionGroup group(String name, String type) {
    TreeNodeActionGroup group = new TreeNodeActionGroup();
    group.setName(name);
    group.setType(type);
    return group;
  }

  private static Annotation annotation(String name) {
    Annotation annotation = new Annotation();
    annotation.setName(name);
    annotation.setValue("v");
    return annotation;
  }

  private static DocumentModel documentModel(String json) throws Exception {
    return JsonSettings.objectMapper.readValue(json, DocumentModel.class);
  }

  private static DocumentModel documentModel(String id, String superTypes, boolean isAbstract) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    if (superTypes != null) {
      model.getAnnotations().add(annotation("superTypes", superTypes));
    }
    if (isAbstract) {
      model.getAnnotations().add(annotation("abstract", "true"));
    }
    return model;
  }

  private static CombinedDocumentModel combination(String id, String superTypes) {
    CombinedDocumentModel model = new CombinedDocumentModel();
    model.setId(id);
    model.getAnnotations().add(annotation("superTypes", superTypes));
    return model;
  }

  private static Annotation annotation(String name, String value) {
    Annotation annotation = new Annotation();
    annotation.setName(name);
    annotation.setValue(value);
    return annotation;
  }

  private static EntityCharacteristic entity(String role, String documentModel) {
    EntityCharacteristic entity = new EntityCharacteristic();
    entity.setRole(role);
    entity.setDocumentModel(documentModel);
    return entity;
  }

  private static RelationshipModel relationship(String id, EntityCharacteristic first, EntityCharacteristic second) {
    RelationshipModel model = new RelationshipModel();
    model.setId(id);
    model.setContent(new RelationshipModelContent());
    model.getContent().getEntityCharacteristics().add(first);
    model.getContent().getEntityCharacteristics().add(second);
    return model;
  }
}
