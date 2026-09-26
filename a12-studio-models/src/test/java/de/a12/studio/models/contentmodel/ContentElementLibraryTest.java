package de.a12.studio.models.contentmodel;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentElementLibraryTest {

  @Test
  void theLibraryHoldsTheDefaultElementsAndTheTwelveFormElements() {
    long defaults = ContentElementLibrary.modules().stream().filter(m -> ContentElementLibrary.NAMESPACE.equals(m.namespace())).count();
    long forms = ContentElementLibrary.modules().stream().filter(m -> ContentElementLibrary.FORM_ELEMENTS_NAMESPACE.equals(m.namespace())).count();

    assertEquals(40, defaults);
    assertEquals(12, forms);
  }

  @Test
  void everyModuleIsUniqueAndEveryRuleRefersToAKnownModule() {
    var ids = new HashSet<String>();
    for (ContentModule module : ContentElementLibrary.modules()) {
      assertTrue(ids.add(module.id()), "duplicate " + module.id());
    }
    for (ContentModule module : ContentElementLibrary.modules()) {
      module.parentRule().ids().forEach(id -> assertTrue(id.equals(ContentModule.ANY) || ids.contains(id), module.id() + " parent " + id));
      referencedIds(module.childRule()).forEach(id -> assertTrue(id.equals(ContentModule.ANY) || ids.contains(id), module.id() + " child " + id));
    }
  }

  private static java.util.List<String> referencedIds(ContentModule.ChildRule rule) {
    var result = new java.util.ArrayList<String>();
    switch (rule) {
      case ContentModule.NoneOf noneOf -> result.addAll(noneOf.ids());
      case ContentModule.AnyOf anyOf -> anyOf.rules().forEach(r -> collect(r, result));
      case ContentModule.Sequence sequence -> sequence.rules().forEach(r -> collect(r, result));
      case ContentModule.AnyOfSequences sequences -> sequences.sequences().forEach(s -> s.rules().forEach(r -> collect(r, result)));
    }
    return result;
  }

  private static void collect(ContentModule.ModuleRule rule, java.util.List<String> result) {
    result.add(rule.id());
    if (rule.childrenRule() != null) {
      result.addAll(referencedIds(rule.childrenRule()));
    }
  }
}
