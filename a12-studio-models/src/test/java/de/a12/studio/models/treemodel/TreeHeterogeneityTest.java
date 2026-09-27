package de.a12.studio.models.treemodel;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Person is abstract with the sub types Employee and Freelancer (Employee has a sub type Manager); Team and Person are
 * related by TeamPerson (Team role "Team", Person role "Member"), A and B by AB/BA.
 */
class TreeHeterogeneityTest {

  private final List<DocumentModel> models = List.of(
      documentModel("Team", null, false),
      documentModel("Person", null, true),
      documentModel("Employee", "Person", false),
      documentModel("Freelancer", "Person", false),
      documentModel("Manager", "Employee", false),
      documentModel("A", null, false),
      documentModel("B", null, false));

  private final Map<String, RelationshipModel> relationships = Map.of(
      "TeamPerson_Re", relationship(entity("Team", "Team"), entity("Member", "Person")),
      "AB_Re", relationship(entity("Parent", "A"), entity("Child", "B")),
      "BA_Re", relationship(entity("Parent", "B"), entity("Child", "A")));

  private final Function<String, RelationshipModel> lookup = relationships::get;

  @Test
  void subTypesAreDirectAndTheSubTypeTestIsRecursive() {
    assertEquals(List.of("Employee", "Freelancer"), TreeHeterogeneity.info(models, "Person").subTypes());
    assertTrue(TreeHeterogeneity.info(models, "Person").isAbstract());
    assertFalse(TreeHeterogeneity.info(models, "Employee").isAbstract());
    assertNull(TreeHeterogeneity.info(models, "Unknown"));

    assertTrue(TreeHeterogeneity.isSubTypeOf(models, "Manager", "Person"));
    assertTrue(TreeHeterogeneity.isSubTypeOf(models, "Employee", "Person"));
    assertFalse(TreeHeterogeneity.isSubTypeOf(models, "Person", "Employee"));
    assertFalse(TreeHeterogeneity.isSubTypeOf(models, "Team", "Person"));
  }

  @Test
  void aChildConfigurationLeadsToTheEntityThatIsNotTheParentRole() {
    TreeHeterogeneity.SubTypesInfo info = TreeHeterogeneity.childInfo(configuration("TeamPerson_Re", "Team"), lookup, models);
    assertEquals("Person", info.superType());
    assertEquals("Team", TreeHeterogeneity.childInfo(configuration("TeamPerson_Re", "Member"), lookup, models).superType());
    assertNull(TreeHeterogeneity.childInfo(configuration("Unknown_Re", "Team"), lookup, models));
    assertNull(TreeHeterogeneity.childInfo(configuration(null, null), lookup, models));
  }

  @Test
  void allDocumentsListsTheWholeSubTypeTreeAndCanLeaveOutTheAbstractOnes() {
    TreeHeterogeneity.SubTypesInfo person = TreeHeterogeneity.info(models, "Person");

    assertEquals(List.of("Person", "Employee", "Manager", "Freelancer"), TreeHeterogeneity.allDocuments(models, person, false));
    assertEquals(List.of("Employee", "Manager", "Freelancer"), TreeHeterogeneity.allDocuments(models, person, true));
  }

  @Test
  void aChildDocumentNeedsANodeTypeForItselfOrItsSubTypes() {
    assertTrue(TreeHeterogeneity.noNodeTypeIsAdded(models, "Person", List.of(node("Team"))));
    assertFalse(TreeHeterogeneity.noNodeTypeIsAdded(models, "Person", List.of(node("Team"), node("Manager"))),
        "a node type for a sub type counts, however deep");

    TreeHeterogeneity.SubTypesInfo person = TreeHeterogeneity.info(models, "Person");
    assertFalse(TreeHeterogeneity.isMissingNodeType(models, person, List.of(node("Person"))), "the super type itself");
    assertTrue(TreeHeterogeneity.isMissingNodeType(models, person, List.of(node("Employee"))), "Freelancer has none");
    assertFalse(TreeHeterogeneity.isMissingNodeType(models, person, List.of(node("Employee"), node("Freelancer"))));
    assertTrue(TreeHeterogeneity.isMissingNodeType(models, TreeHeterogeneity.info(models, "Team"), List.of(node("A"))),
        "a concrete Document Model needs its own node type");
    assertFalse(TreeHeterogeneity.isMissingNodeType(models, TreeHeterogeneity.info(models, "Team"), List.of(node("Team"))));
  }

  @Test
  void nodeTypesThatLeadBackToEachOtherAreCircular() {
    TreeNode a = node("A");
    a.getChildRelationshipConfigurations().add(configuration("AB_Re", "Parent"));
    TreeNode b = node("B");
    b.getChildRelationshipConfigurations().add(configuration("BA_Re", "Parent"));

    assertTrue(TreeHeterogeneity.hasCircularRelationship(a, List.of(a, b), lookup, models));
    assertTrue(TreeHeterogeneity.hasCircularRelationship(b, List.of(a, b), lookup, models));

    b.getChildRelationshipConfigurations().clear();
    assertFalse(TreeHeterogeneity.hasCircularRelationship(a, List.of(a, b), lookup, models));
  }

  @Test
  void aRelationshipFitsTheNodesDocumentModelOrItsSuperTypes() {
    RelationshipModel teamPerson = relationships.get("TeamPerson_Re");

    assertTrue(TreeHeterogeneity.relationshipFits(teamPerson, "Team", models));
    assertTrue(TreeHeterogeneity.relationshipFits(teamPerson, "Manager", models), "Manager is a sub type of Person");
    assertFalse(TreeHeterogeneity.relationshipFits(teamPerson, "A", models));
    assertEquals(List.of("Member"), TreeHeterogeneity.fittingRoles(teamPerson, "Employee", models));
    assertEquals(List.of("Team"), TreeHeterogeneity.fittingRoles(teamPerson, "Team", models));
  }

  @Test
  void insertCandidatesFollowThePosition() {
    TreeNode team = node("Team");
    team.getChildRelationshipConfigurations().add(configuration("TeamPerson_Re", "Team"));
    TreeNode employee = node("Employee");
    TreeModel model = new TreeModel();
    model.setContent(new TreeModelContent());
    model.getContent().getNodes().addAll(List.of(team, employee));

    assertEquals(List.of("Employee", "Freelancer", "Manager"),
        TreeHeterogeneity.insertCandidates(model, team, TreeNodeAction.POSITION_AS_CHILD, lookup, models),
        "as child: the concrete Document Models of the child relationships");
    assertEquals(List.of("Employee", "Freelancer", "Manager"),
        TreeHeterogeneity.insertCandidates(model, employee, TreeNodeAction.POSITION_BELOW, lookup, models),
        "below: the node's own Document Model, its sub types and its siblings");
    assertEquals(List.of("Team"),
        TreeHeterogeneity.insertCandidates(model, team, TreeNodeAction.POSITION_ABOVE, lookup, models));
    assertTrue(TreeHeterogeneity.insertCandidates(model, team, null, lookup, models).isEmpty());
  }

  @Test
  void aNodeThatInheritsItsRelationshipsUsesItsSuperTypesOnes() {
    TreeNode person = node("Person");
    person.getChildRelationshipConfigurations().add(configuration("AB_Re", "Parent"));
    TreeNode employee = node("Employee");
    TreeNodeInheritance.setInherited(employee, TreeNodeInheritance.Part.CHILD_RELATIONSHIP_CONFIGURATIONS, true);
    TreeNode manager = node("Manager");
    TreeNodeInheritance.setInherited(manager, TreeNodeInheritance.Part.CHILD_RELATIONSHIP_CONFIGURATIONS, true);
    List<TreeNode> nodes = List.of(person, employee, manager);

    assertEquals(1, TreeHeterogeneity.effectiveChildRelationships(nodes, employee, models).size());
    assertEquals(1, TreeHeterogeneity.effectiveChildRelationships(nodes, manager, models).size(), "through Employee");
    assertNotNull(TreeHeterogeneity.effectiveChildRelationships(nodes, person, models));
    assertTrue(TreeHeterogeneity.effectiveChildRelationships(List.of(manager), manager, models).isEmpty());
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

  private static RelationshipModel relationship(EntityCharacteristic first, EntityCharacteristic second) {
    RelationshipModel model = new RelationshipModel();
    model.setContent(new RelationshipModelContent());
    model.getContent().getEntityCharacteristics().add(first);
    model.getContent().getEntityCharacteristics().add(second);
    return model;
  }

  private static TreeNode node(String documentModel) {
    TreeNode node = new TreeNode();
    node.setDocumentModelRef(documentModel);
    return node;
  }

  private static TreeChildRelationshipConfiguration configuration(String relationship, String parentRole) {
    TreeChildRelationshipConfiguration configuration = new TreeChildRelationshipConfiguration();
    configuration.setRelationshipModelRef(relationship);
    configuration.setParentRole(parentRole);
    return configuration;
  }
}
