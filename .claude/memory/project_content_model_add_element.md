---
name: content-model-add-element
description: Content Model "Add child" opens an Add Element dialog filtered by SME's parent/child rules (built 2026-09-26); where the rules come from, how they are ported, what is not ported
metadata:
  type: project
---

"Add child" in `ContentModelEditorController` no longer inserts a Box: it opens `editors/contentmodel/dialogs/InsertElementDialogController` (tiles per category, nothing preselected, double click or Add confirms) listing only the types SME would offer. Rules live in `a12-studio-models/.../contentmodel`: `ContentElementLibrary` (52 types with label/category/parent+child rule), `ContentRuleEvaluator`, `ContentInsertion` (simulates the child list after the insert, Group/Conditional looked through except in the table body), `ContentElementFactory` (SME's `propertiesCreator`s: default props + the parts a type needs, table rows get one cell per table column). Details: "Content Model "Add child"" in `docs/sme-reference-comparison.md`.

**Source of the rules:** `@com.mgmtp.a12.contentengine/contentengine-editor` `src/internal/core/default-editor-elements/*/*.module.tsx` (`parentRules`/`childRules`) and `store/selectors/model-state.ts` (`insertableNodeTypes`), form elements from `@com.mgmtp.a12.formengine/formengine-content-elements-editor` (SME pins 38.3.0, 38.4.3 read); download like [[a12-community-npm-and-maven-sources]]. Extends [[content-model-property-panels]].

**Traps:** `ContentModule::id` is ambiguous with a static overload (static is `moduleId`); form elements live in namespace `com.mgmtp.a12.formengine`, an element built with the wrong namespace is an "unknown type" that takes no children; `ContentModelEditorPanelsTest` has 5 failures that exist at HEAD (element panel include commented out + heading text row) - not caused by this work; `onAddChild` is split into the dialog part and package-private `addChild(parent, module)` so tests can insert without the modal dialog.

**Not built:** paste/cut/drag still accept any target (SME has paste rules), "Insert above/below" menu entries (the engine supports both positions), an Add Element flow for element types from plugin libraries.
