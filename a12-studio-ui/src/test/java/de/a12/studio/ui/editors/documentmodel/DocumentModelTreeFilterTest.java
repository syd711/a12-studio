package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.RequirednessConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.ui.editors.documentmodel.DocumentModelTreeFilter.ElementKind;
import de.a12.studio.ui.editors.documentmodel.DocumentModelTreeFilter.FieldKind;
import de.a12.studio.ui.editors.documentmodel.DocumentModelTreeFilter.SearchIn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentModelTreeFilterTest {

  private final DocumentModelTreeFilter filter = new DocumentModelTreeFilter();

  // ---- search -----------------------------------------------------------------------------------------------

  @Test
  void aFilterWithNothingSetIsInactiveAndShowsEverything() {
    assertFalse(filter.isActive());
    assertFalse(filter.hasNarrowingFilters());
    assertTrue(filter.isLeafShown(field("Anything", new StringFieldType())));
    assertFalse(filter.isHiddenByType(field("Anything", new StringFieldType())));
  }

  @Test
  void nameSearchIsCaseInsensitiveAndIgnoresSurroundingBlanks() {
    filter.setSearchText("  postal ");

    assertTrue(filter.isActive());
    assertTrue(filter.hasSearchText());
    assertFalse(filter.hasNarrowingFilters(), "a search text is not a narrowing filter");
    assertTrue(filter.matchesSearch(field("PostalCode", new StringFieldType())));
    assertFalse(filter.matchesSearch(field("City", new StringFieldType())));
  }

  @Test
  void idSearchComparesTheIdNotTheName() {
    FieldElement city = field("City", new StringFieldType());
    city.setId("field_ab12c");
    filter.setSearchIn(SearchIn.ID);

    filter.setSearchText("AB12");
    assertTrue(filter.matchesSearch(city));
    filter.setSearchText("city");
    assertFalse(filter.matchesSearch(city));
  }

  @Test
  void labelSearchLooksAtEveryLocaleOfAGroupOrFieldAndNothingElse() {
    FieldElement city = field("City", new StringFieldType());
    city.getField().getLabel().add(label("en", "Town"));
    city.getField().getLabel().add(label("de", "Stadt"));
    GroupElement address = group("Address");
    address.getGroup().getLabel().add(label("en", "Postal address"));
    RuleElement rule = new RuleElement();
    rule.setName("Town");
    filter.setSearchIn(SearchIn.LABEL);

    filter.setSearchText("stadt");
    assertTrue(filter.matchesSearch(city));
    filter.setSearchText("postal");
    assertTrue(filter.matchesSearch(address));
    filter.setSearchText("town");
    assertFalse(filter.matchesSearch(rule), "a rule has no label, its name does not count");
  }

  // ---- element types ----------------------------------------------------------------------------------------

  @Test
  void everyElementKindIsRecognisedAndPlainGroupsAreNotHideable() {
    assertEquals(ElementKind.FIELDS, DocumentModelTreeFilter.kindOf(field("F", new StringFieldType())));
    assertEquals(ElementKind.VALIDATION_RULES, DocumentModelTreeFilter.kindOf(new RuleElement()));
    assertEquals(ElementKind.COMPUTATION_RULES, DocumentModelTreeFilter.kindOf(new ComputationElement()));
    assertEquals(ElementKind.INCLUDES, DocumentModelTreeFilter.kindOf(include("I")));
    GroupElement attachment = group("A");
    attachment.getGroup().setUsageType(GroupConfig.USAGE_TYPE_ATTACHMENT);
    assertEquals(ElementKind.ATTACHMENTS, DocumentModelTreeFilter.kindOf(attachment));
    GroupElement multiSelect = group("M");
    multiSelect.getGroup().setUsageType(GroupConfig.USAGE_TYPE_MULTI_SELECT);
    assertEquals(ElementKind.MULTI_SELECTS, DocumentModelTreeFilter.kindOf(multiSelect));
    assertNull(DocumentModelTreeFilter.kindOf(group("Plain")));
  }

  @Test
  void switchingAnElementKindOffHidesItsElementsAndOnlyThose() {
    filter.setShown(ElementKind.INCLUDES, false);
    filter.setShown(ElementKind.VALIDATION_RULES, false);

    assertTrue(filter.hasNarrowingFilters());
    assertTrue(filter.isHiddenByType(include("I")));
    assertTrue(filter.isHiddenByType(new RuleElement()));
    assertFalse(filter.isHiddenByType(new ComputationElement()));
    assertFalse(filter.isHiddenByType(field("F", new StringFieldType())));
    assertFalse(filter.isHiddenByType(group("Plain")));

    filter.setShown(ElementKind.INCLUDES, true);
    assertFalse(filter.isHiddenByType(include("I")));
  }

  // ---- annotations ------------------------------------------------------------------------------------------

  @Test
  void annotatedOnlyKeepsLeavesWithAtLeastOneAnnotation() {
    FieldElement annotated = field("A", new StringFieldType());
    annotated.getAnnotations().add(new Annotation());
    FieldElement plain = field("B", new StringFieldType());
    RuleElement plainRule = new RuleElement();
    filter.setOnlyAnnotated(true);

    assertTrue(filter.isLeafShown(annotated));
    assertFalse(filter.isLeafShown(plain));
    assertFalse(filter.isLeafShown(plainRule));
  }

  // ---- field types ------------------------------------------------------------------------------------------

  @Test
  void aHiddenFieldTypeHidesFieldsOfThatBaseTypeOnly() {
    filter.setShown(FieldKind.STRING, false);

    assertFalse(filter.isLeafShown(field("S", new StringFieldType())));
    assertTrue(filter.isLeafShown(field("N", new NumberFieldType())));
    assertTrue(filter.isLeafShown(new RuleElement()), "field-type filters do not concern other elements");
    assertTrue(filter.isLeafShown(new ComputationElement()));
  }

  @Test
  void aTypeDefinitionFieldCountsAsTheTypeItResolvesTo() {
    FieldElement kind = field("Kind", typeDef("typedef_kind"));
    filter.setEffectiveTypeResolver(declared -> declared instanceof TypeDefFieldType ? new EnumerationFieldType() : declared);

    filter.setShown(FieldKind.STRING, false);
    assertTrue(filter.isLeafShown(kind));
    filter.setShown(FieldKind.ENUMERATION, false);
    assertFalse(filter.isLeafShown(kind));
  }

  @Test
  void aTypeDefinitionThatDoesNotResolveIsNeverHiddenByType() {
    FieldElement kind = field("Kind", typeDef("typedef_gone"));
    filter.setEffectiveTypeResolver(declared -> declared instanceof TypeDefFieldType ? null : declared);
    for (FieldKind fieldKind : FieldKind.values()) {
      filter.setShown(fieldKind, false);
    }

    assertTrue(filter.isLeafShown(kind));
  }

  @Test
  void everyBaseFieldTypeMapsToItsKind() {
    assertEquals(FieldKind.BOOLEAN, DocumentModelTreeFilter.fieldKindOf(new BooleanFieldType()));
    assertEquals(FieldKind.STRING, DocumentModelTreeFilter.fieldKindOf(new StringFieldType()));
    assertEquals(FieldKind.NUMBER, DocumentModelTreeFilter.fieldKindOf(new NumberFieldType()));
    assertEquals(FieldKind.ENUMERATION, DocumentModelTreeFilter.fieldKindOf(new EnumerationFieldType()));
    assertNull(DocumentModelTreeFilter.fieldKindOf(typeDef("typedef_unresolved")));
    assertNull(DocumentModelTreeFilter.fieldKindOf(null));
  }

  // ---- requiredness -----------------------------------------------------------------------------------------

  @Test
  void requirednessFiltersMatchTheirModeAndCombineAsAlternatives() {
    FieldElement always = required("Always", RequirednessConfig.MODE_REQUIRED);
    FieldElement ifParent = required("IfParent", RequirednessConfig.MODE_REQUIRED_IF_PARENT_FILLED);
    FieldElement optional = field("Optional", new StringFieldType());

    filter.setAlwaysRequired(true);
    assertTrue(filter.isLeafShown(always));
    assertFalse(filter.isLeafShown(ifParent));
    assertFalse(filter.isLeafShown(optional));

    filter.setAlwaysRequired(false);
    filter.setRequiredIfParentFilled(true);
    assertFalse(filter.isLeafShown(always));
    assertTrue(filter.isLeafShown(ifParent));

    filter.setAlwaysRequired(true);
    assertTrue(filter.isLeafShown(always));
    assertTrue(filter.isLeafShown(ifParent));
    assertFalse(filter.isLeafShown(optional));
  }

  @Test
  void aRequirednessFilterLeavesOnlyFieldsBehind() {
    assertTrue(filter.isLeafShown(new RuleElement()));

    filter.setAlwaysRequired(true);

    assertFalse(filter.isLeafShown(new RuleElement()));
    assertFalse(filter.isLeafShown(new ComputationElement()));
  }

  // ---- reset ------------------------------------------------------------------------------------------------

  @Test
  void resettingClearsTheNarrowingFiltersButKeepsTheSearch() {
    filter.setSearchText("city");
    filter.setSearchIn(SearchIn.ID);
    filter.setOnlyAnnotated(true);
    filter.setAlwaysRequired(true);
    filter.setRequiredIfParentFilled(true);
    filter.setShown(ElementKind.FIELDS, false);
    filter.setShown(FieldKind.DATE, false);

    filter.resetNarrowingFilters();

    assertFalse(filter.hasNarrowingFilters());
    assertEquals("city", filter.getSearchText());
    assertEquals(SearchIn.ID, filter.getSearchIn());
    assertTrue(filter.isShown(ElementKind.FIELDS));
    assertTrue(filter.isShown(FieldKind.DATE));
  }

  // ---- builders ---------------------------------------------------------------------------------------------

  private static FieldElement field(String name, FieldType type) {
    FieldElement field = new FieldElement();
    field.setId("field_" + name);
    field.setName(name);
    FieldConfig config = new FieldConfig();
    config.setFieldType(type);
    field.setField(config);
    return field;
  }

  private static FieldElement required(String name, String mode) {
    FieldElement field = field(name, new StringFieldType());
    RequirednessConfig requiredness = new RequirednessConfig();
    requiredness.setMode(mode);
    field.getField().setRequirednessConfig(requiredness);
    return field;
  }

  private static TypeDefFieldType typeDef(String id) {
    TypeDefFieldType type = new TypeDefFieldType();
    type.getTypeDefType().setTypeDefinitionId(id);
    return type;
  }

  private static GroupElement group(String name) {
    GroupElement group = new GroupElement();
    group.setId("group_" + name);
    group.setName(name);
    group.setGroup(new GroupConfig());
    return group;
  }

  private static GroupElement include(String name) {
    GroupElement group = group(name);
    IncludeConfig config = new IncludeConfig();
    config.setReference("Other_DM");
    group.getGroup().setIncludeConfig(config);
    return group;
  }

  private static Label label(String locale, String text) {
    Label label = new Label();
    label.setLocale(locale);
    label.setText(text);
    return label;
  }
}
