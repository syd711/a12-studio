package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per tree model validator, each loading a fixture that contains exactly that error. */
class TreeValidatorsTest {

  private TreeModel load(String name) {
    return TestModels.load("/treemodel/" + name + ".json", TreeModel.class);
  }

  private DocumentModel refDm() {
    return TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
  }

  @Test
  void nodesNotEmptyValidatorReportsEmptyNodes() {
    TreeModel model = load("TreeNodesNotEmptyValidator_invalid");
    List<ModelValidationError> errors = new TreeNodesNotEmptyValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Node types must not be empty"));
  }

  @Test
  void columnsNotEmptyValidatorReportsEmptyColumns() {
    TreeModel model = load("TreeColumnsNotEmptyValidator_invalid");
    List<ModelValidationError> errors = new TreeColumnsNotEmptyValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Columns must not be empty"));
  }

  @Test
  void uniqueNodeValidatorReportsDuplicateIdAndDocumentModel() {
    TreeModel model = load("TreeUniqueNodeValidator_invalid");
    List<ModelValidationError> errors = new TreeUniqueNodeValidator().validate(model, TestModels.context(model));

    // Same node id AND same document model on both nodes.
    assertEquals(2, errors.size());
  }

  @Test
  void documentModelReferenceValidatorReportsMissingModel() {
    TreeModel model = load("TreeDocumentModelReferenceValidator_invalid");
    List<ModelValidationError> errors = new TreeDocumentModelReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Missing_DM"));
  }

  @Test
  void columnFieldValidatorReportsUnknownColumnMissingFieldAndUnindexedField() {
    TreeModel model = load("TreeColumnFieldValidator_invalid");
    List<ModelValidationError> errors = new TreeColumnFieldValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm()));

    // Unknown column ref + unresolvable element ref on the first mapping, indexed=false field on the second.
    assertEquals(3, errors.size());
  }

  @Test
  void hierarchicalColumnRefValidatorReportsUnknownColumn() {
    TreeModel model = load("TreeHierarchicalColumnRefValidator_invalid");
    List<ModelValidationError> errors = new TreeHierarchicalColumnRefValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("column_unknown"));
  }
}
