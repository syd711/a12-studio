package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.a12.studio.modelsvalidation.validators.content.ContentReferenceValidatorsTest.element;
import static de.a12.studio.modelsvalidation.validators.content.ContentReferenceValidatorsTest.group;
import static de.a12.studio.modelsvalidation.validators.content.ContentReferenceValidatorsTest.page;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the editor's pickers may offer, which is what the validators accept: the groups and fields reachable from a data
 * context, the Document Model elements a form element can show, and the data context of an element of the tree. Uses the
 * ids of {@link ContentReferenceValidatorsTest}'s Product model.
 */
class ContentReferenceCandidatesTest {

  private static final String P = "include_common_";
  private static final String VARIANTS = P + "group_082cd";
  private static final String PRICING = P + "group_7b560";
  private static final String ATTRIBUTES = P + "group_8767d";
  private static final String NAME = P + "field_4217e";
  private static final String VARIANT_NAME = P + "field_df210";
  private static final String HIGHEST_PRICE = P + "field_d2f50";
  private static final String NUMBER_OF_VARIANTS = P + "field_12ee1";
  private static final String IS_ACTIVE = P + "field_64375";

  private static DocumentStructure structure;
  private static List<DocumentModel> documentModels;

  @BeforeAll
  static void load() throws IOException {
    Path products = locate().resolve("e-commerce").resolve("models").resolve("01_Products");
    documentModels = new ArrayList<>();
    try (Stream<Path> files = Files.list(products)) {
      for (Path file : files.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
        A12Model<?> model = new ProjectItem(file.toFile()).getModel();
        if (model instanceof DocumentModel documentModel) {
          documentModels.add(documentModel);
        }
      }
    }
    DocumentModel product = documentModels.stream().filter(model -> "Product_DM".equals(model.getId())).findFirst().orElseThrow();
    structure = new DocumentStructure(product, documentModels);
  }

  @Test
  void theIncludesAreExpandedUnderTheirPrefixedIds() {
    assertTrue(structure.find(VARIANTS) != null && structure.find(VARIANTS).isRepeated());
    assertTrue(structure.find(VARIANTS).path().startsWith("/Product/") && structure.find(VARIANTS).path().endsWith("/Variants"),
        structure.find(VARIANTS).path());
    assertTrue(structure.find(NAME).isField());
    assertNull(structure.find("nope"));
  }

  @Test
  void anElementThatOnlyListsFieldsMayListThoseInRepeatedGroupsToo() {
    List<String> fromTop = structure.candidateFieldsThroughRepeatedGroups(null).stream().map(node -> node.id).toList();
    List<String> fromVariants = structure.candidateFieldsThroughRepeatedGroups(structure.find(VARIANTS)).stream().map(node -> node.id).toList();

    assertTrue(fromTop.containsAll(List.of(NAME, VARIANT_NAME, HIGHEST_PRICE)), "repeated groups are traversed");
    assertEquals(fromTop, fromVariants, "the whole tree lies below the topmost group above any context");
    assertTrue(structure.candidateFieldsThroughRepeatedGroups(null).stream().allMatch(node -> node.isField()));
  }

  @Test
  void atTheTopEveryGroupButOnlyFieldsThatAreNotInRepeatedGroupsAreCandidates() {
    List<String> groups = structure.candidateGroups(null).stream().map(node -> node.id).toList();
    List<String> fields = structure.candidateFields(null).stream().map(node -> node.id).toList();

    assertTrue(groups.containsAll(List.of(VARIANTS, PRICING, ATTRIBUTES)));
    assertTrue(fields.containsAll(List.of(NAME, NUMBER_OF_VARIANTS)));
    assertFalse(fields.contains(VARIANT_NAME), "Variants is repeated");
    assertFalse(fields.contains(HIGHEST_PRICE), "Pricing is repeated");
  }

  @Test
  void insideARepeatedGroupItsOwnFieldsAndTheGroupsBelowItAreCandidates() {
    DocumentStructure.Node variants = structure.find(VARIANTS);

    List<String> groups = structure.candidateGroups(variants).stream().map(node -> node.id).toList();
    List<String> fields = structure.candidateFields(variants).stream().map(node -> node.id).toList();

    assertEquals(List.of(P + "group_259c2", ATTRIBUTES), groups.stream().filter(id -> List.of(P + "group_259c2", ATTRIBUTES).contains(id)).toList());
    assertFalse(groups.contains(PRICING));
    assertFalse(groups.contains(VARIANTS), "not below itself");
    assertTrue(fields.containsAll(List.of(VARIANT_NAME, NAME, NUMBER_OF_VARIANTS)), "its own and those of the groups above");
    assertFalse(fields.contains(HIGHEST_PRICE));
  }

  @Test
  void aFormElementIsOfferedTheElementsItCanShowAtItsPosition() {
    List<String> textLines = ContentFormElementTypes.candidates("TextLine", structure, null).stream().map(node -> node.id).toList();
    List<String> checkboxes = ContentFormElementTypes.candidates("Checkbox", structure, null).stream().map(node -> node.id).toList();
    List<String> insideVariants = ContentFormElementTypes.candidates("TextLine", structure, structure.find(VARIANTS)).stream()
        .map(node -> node.id).toList();

    assertTrue(textLines.contains(NAME) && textLines.contains(NUMBER_OF_VARIANTS));
    assertFalse(textLines.contains(IS_ACTIVE), "a boolean is not text");
    assertFalse(textLines.contains(VARIANT_NAME), "repeated more often than the top");
    assertTrue(checkboxes.contains(IS_ACTIVE));
    assertFalse(checkboxes.contains(NAME));
    assertTrue(insideVariants.contains(VARIANT_NAME));
    assertFalse(insideVariants.contains(P + "field_5bafa"), "an attribute is one repeat deeper than Variants");
  }

  @Test
  void multiSelectElementsAreOfferedMultiSelectGroupsOnly() {
    assertEquals(List.of(), ContentFormElementTypes.candidates("MultiSelect", structure, null),
        "the Product model has no multi-select group");
  }

  @Test
  void theDataContextOfAnElementIsTheClosestEnclosingRepeatableGroup() {
    ContentElement inner = element("inner", "FieldOutput", "fieldId", NAME);
    ContentElement attributes = group("attributes", ATTRIBUTES, inner);
    ContentElement variants = group("variants", VARIANTS, attributes);
    ContentElement top = element("top", "Paragraph");
    ContentModel model = page("Product_DM", top, variants);

    assertNull(ContentDataContext.of(model, structure, top));
    assertNull(ContentDataContext.of(model, structure, variants), "a group's own reference is relative to what encloses it");
    assertEquals(VARIANTS, ContentDataContext.of(model, structure, attributes).id);
    assertEquals(ATTRIBUTES, ContentDataContext.of(model, structure, inner).id);
  }

  @Test
  void theBaseGroupIsTheContextAtTheTop() {
    ContentElement top = element("top", "Paragraph");
    ContentModel model = page("Product_DM", top);
    model.getContent().getConfiguration().setBaseGroupId(PRICING);

    assertEquals(PRICING, ContentDataContext.of(model, structure, top).id);

    model.getContent().getConfiguration().setBaseGroupId("nope");
    assertNull(ContentDataContext.of(model, structure, top));
  }

  private static Path locate() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate testing/workspaces");
  }
}
