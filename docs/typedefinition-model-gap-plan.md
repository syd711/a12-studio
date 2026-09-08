# Type Definition Model — SME Gap Closure Plan

Source: deep comparison of `a12-studio`'s Type Definition Model (TdM) editor against SME's
reference implementation (`C:\workspace\sme\client\src\modules\typeDefinitionModel`,
`commonDocumentModel/api/editor/typeDefs`, `DomainTypedef.json`/`DomainField.json`, and
`backend/.../documentModel/features/{expansion,validation}`), done 2026-09-08.

Status legend: `[ ]` not started, `[~]` in progress, `[x]` done.

## Background / key finding

`NumberFieldValueLimitValidator`, `StringPatternErrorMessageValidator`, `BasicConsistencyValidator`,
and `EnumerationValuesValidator` all walk `ElementIndex.allElements()` (the `modelRoot` tree) and
resolve `TypeDefFieldType` references through `effectiveFieldType()`. They never iterate
`content.typeDefinitions` directly. Since a Type Definition Model's `modelRoot` is always empty,
opening a TdM and configuring an invalid field (e.g. String `minLength > maxLength`) currently
produces **zero validation errors** until some other model's field references it. Phase 1 fixes the
iteration source once, which unlocks correctness for every base type.

## Phase 1 — Validator iteration fix + missing per-basetype rules — DONE (2026-09-08)

- [x] Dual-loop fix applied directly in each touched validator (matches the pattern
      `EnumerationValuesValidator` already used: a second explicit loop over
      `documentModel.getContent().getTypeDefinitions()` after the existing `allElements()` loop,
      extracted into a shared private `checkXxxType(...)` method called from both loops). No new
      shared `ElementIndex` helper was introduced — the existing per-validator dual-loop shape was
      simpler and matched established convention (three similar lines > premature abstraction).
- [x] `NumberFieldValueLimitValidator`, `StringPatternErrorMessageValidator`,
      `BasicConsistencyValidator` (enum duplicate-value/category checks only — the `TypeDefType`
      blank-id check stays field-only, since a type definition's own `fieldType` can never itself be
      a `TypeDefType` reference) now also validate a model's own `TypeDefinition`s directly.
- [x] New `StringTypeConfigValidator`: pattern regex-syntax validity, `minLength > maxLength`,
      `lineBreaksPermitted` + `maxLength == 1`, `lineBreaksPermitted` + `alphabeticalSorting`
- [ ] Not done: String — duplicate error-message locales (SME's `ERROR_MESSAGE_LANGUAGES_DUPLICATE`).
      Lower value than the others (a data-entry slip, not a broken config) — left for a follow-up.
- [x] New `NumberTypeConfigValidator`: `minValue > maxValue`, `minFractionalDigits > maxFractionalDigits`
- [ ] Not done: Number — amount trait + invalid fraction digits (SME's
      `A12_AMOUNT_AND_INVALID_FRACT_DIGITS`). Skipped: exact semantics (does "Amount" require
      `maxFractionalDigits == 2` specifically, or something else?) weren't confirmed against SME
      source before implementing, and guessing wrong would ship an incorrect rule. Needs a look at
      `DomainField.json`'s actual rule condition before implementing.
- [x] New `EnumerationTypeConfigValidator`: empty/blank value entry, label missing for a locale
      declared on the model, blank category name, blank category value entry
- [x] New `CustomFieldTypeConfigValidator`: codename (`CustomFieldTypeOptions.name`) required
- [x] New `DateFormatConfigValidator`: format required for Date/DateTime/Time/DateFragment/DateRange
- [x] Message keys added to `validation-messages.properties` + `_de.properties`, all naming "Field or
      Type Definition with id '{0}'" per CLAUDE.md's "validator messages must name the field" rule
- [x] Registered all 5 new validators in `DocumentModelValidationService`
- [x] Tests: `DocumentModelValidatorsTest` — one fixture-based test per new validator, all using a
      TdM-shaped fixture (`modelRoot.rootGroups: []`, only `content.typeDefinitions` populated) to
      prove the type-definition-only path fires; plus 3 regression tests
      (`*_typeDefinitionInvalid`/`BasicConsistencyValidator_typeDefinitionDuplicateEnum` fixtures)
      proving the dual-loop fix on the 3 extended pre-existing validators. Full
      `:a12-studio-models-validation:test` run green.

## Phase 2 — Enumeration custom error messages — DONE (2026-09-08)

- [x] `EnumerationTypeOptions`: added `useDefaultErrorMessages : Boolean` (`NON_NULL`) +
      `errorMessage : List<Label>` (`NON_EMPTY`), mirroring `StringTypeOptions.errorMessage`
- [x] `DataTypeEnumerationConfigurationPanelController` / its FXML: added
      `useDefaultErrorMessagesCheckBox` + a nested `fx:include` of `localized-text-panel.fxml`
      (`errorMessageController`), visible only while the checkbox is unchecked - same shape as
      `TypeDefinitionPanelController`'s existing `defaultErrorMessagesCheckbox` +
      `requirednessErrorMessageController` nesting, which was the established precedent for
      embedding one `AbstractPropertyEditor` inside another via `fx:include`.
- [x] `LocalizedTextPanelController`: new `configureEnumerationErrorMessage()` entry point
      (mirrors `configureErrorMessages()`), reading/writing `EnumerationTypeOptions.errorMessage`.
      Works transparently for both a Document Model field and a `TypeDefinitionFieldElement`
      (the TdM editor's adapter), since both are `FieldElement`s.
- [x] Discovered while implementing: the *existing* String pattern error-message editor
      (`errorMessagesController` in `DocumentModelFieldEditorController`) is only wired into the
      regular Document Model field editor, not `TypeDefinitionModelFieldEditorController` — so a
      String type definition's own pattern error message currently has **no UI** in the standalone
      TdM editor either (it can only be set by editing the field from a Document Model that
      references the type definition, or by hand-editing JSON). Not fixed here (out of the
      Enumeration-scoped ask); flagged for a follow-up. See "Additional gap found" below.
- [x] Validator: `EnumerationTypeConfigValidator.checkErrorMessage` — custom error message required
      per locale declared on the model, once `useDefaultErrorMessages` is set to `false` (mirrors
      `StringPatternErrorMessageValidator`'s per-locale check)
- [x] Tests: `EnumerationTypeConfigValidator_errorMessageMissingLocale` fixture +
      `enumerationTypeConfigValidatorReportsMissingLocaleForCustomErrorMessage`. Full
      `:a12-studio-models:test`, `:a12-studio-models-validation:test` green; `:a12-studio-ui:compileJava` green.

### Additional gap found and fixed during Phase 2

- [x] `TypeDefinitionModelFieldEditorController` had no error-message panel at all for String type
      definitions (only `DocumentModelFieldEditorController` wired one up, gated on
      `dataTypeConfigurationController.patternProperty()`). Fixed: added the same
      `errorMessages`/`errorMessagesController` (`configureErrorMessages()`) wiring to
      `TypeDefinitionModelFieldEditorController` and its FXML, gated on
      `dataTypeConfigurationController.patternProperty()` alone (no multi-select branch needed - a
      type definition is never itself inside a multi-select group).

## Phase 3 — Import/mixing integrity — DONE (2026-09-08)

- [x] `TypeDefinitionTableController.updateAddImportAvailability()`: Add disabled once any Import
      reference exists, Import disabled once any local type definition exists; tooltip text swaps to
      explain why (new keys `add_type_definition_disabled_import_present` /
      `import_type_definition_disabled_local_present` in `messages.properties`/`_de.properties`).
      Called from `load()` (via `refreshIncludedTypeDefinitions()`), `onAdd()`, `deleteTypeDefinitions()`,
      `onImport()`/`removeImport()` (via `refreshIncludedTypeDefinitions()`).
- [x] `importCandidates()`: added `hasCompatibleLocales()` filter - a candidate TDM must declare at
      least every locale the importing model declares. (Exact SME semantics for "incompatible
      locales" weren't found in the reference source; this is a defensible interpretation - a
      candidate missing a locale the importer needs would leave that locale's labels/error messages
      untranslated. Worth revisiting if SME's actual rule turns out narrower/wider.)
- [x] New `IncludeTypeDefinitionModeValidator`: flags an Include whose target has a different
      Type-Definition mode (local vs. import) than the current model - mirrors SME's
      `IncludeDifferentTypeDefinitionMode`. Registered in `DocumentModelValidationService`.
- [x] `TransitiveTypeDefinitions.Entry` gained a 4th field `includedImported` (chain starts with an
      Include, ends with an Import - an included model that itself imports a TDM), computed via an
      edge-kind `Deque<Boolean>` threaded alongside the existing model-id path deque through
      `collect`/`visit`.
- [x] `TypeDefinitionRow`/`TypeDefinitionTableController`: included-imported rows get their own
      `INCLUDED_IMPORTED_ROW` pseudo-class (grey background, `stylesheet.css`) and source label
      ("Included Import: ..."), and are excluded from `TypeDefinitionPanelController`'s "Use Custom
      Type" picker (`collectAvailableTypeDefinitionLabels`) - matching SME's "merely displayed...
      must import directly to use them".
- [x] Tests: `TransitiveTypeDefinitionsTest` (2 new/extended cases), `includeTypeDefinitionModeValidatorReportsMismatchedMode`
      in `DocumentModelValidatorsTest`. Full `:a12-studio-models-validation:test` and
      `:a12-studio-ui:compileJava` green.

## Phase 4 — Polish — DONE (2026-09-08)

- [x] "TD" badge in the Model Tree: `ElementViewModel.usesTypeDefinition()` +
      `Icons.ELEMENT_TYPE_DEFINITION_REFERENCE` (`mdi2a-alpha-t-box-outline`, following the same
      "alpha-X-box" convention as the existing Field/Annotation badges) + a new badge icon in
      `ElementNameTreeCell.showReadOnly` alongside the existing annotations/required badges, with
      tooltip key `field_uses_type_definition`.
- [x] De-duplicated the blank-`typeDefinitionId` double-report: removed
      `BasicConsistencyValidator`'s `TypeDefFieldType` check entirely (renamed
      `checkEnumerationOrTypeDef` stays but now only does the enumeration half) and removed its
      message key `validation.basicConsistency.invalidTypeDefReference`. `MissingReferenceValidator`
      now reports one of two distinct messages via a `TypeDefStatus` enum
      (`NOT_SPECIFIED`/`DOES_NOT_EXIST`/`OK`): `missingReference.missingTypeDefinition` ("must be
      specified") vs. new `missingReference.invalidTypeDefinition` ("does not exist") - mirrors
      SME's `A12_TYPE_DEFINITION_MISSING`/`A12_TYPE_DEFINITION_INVALID`.
- [x] Delete-confirmation wording in `TypeDefinitionTableController.deleteTypeDefinitions` now
      states fields using the type definition will become invalid (singular and plural), matching
      SME's own warning text. Deletion still isn't usage-blocked, same as SME.
- [x] Tests: `missingReferenceValidatorDistinguishesUnspecifiedFromUnresolvableTypeDefinition`
      (also asserts `BasicConsistencyValidator` no longer double-reports). Full
      `:a12-studio-models-validation:test`, `:a12-studio-commons:compileJava`,
      `:a12-studio-ui:compileJava` green.

## All four phases complete (2026-09-08)

Ran `:a12-studio-models:test`, `:a12-studio-models-validation:test`, `:a12-studio-commons:compileJava`,
`:a12-studio-ui:compileJava` green after every phase. A project-wide `./gradlew test` additionally hit an
unrelated Windows file-lock on a stale `a12-studio-ui/build/resources/main/.../Model-Overview.png` artifact
(`processResources` couldn't delete a file held open by another process) - not caused by any change here;
re-run it after closing whatever has that file open (a running app instance, most likely) if a full
cross-module regression pass is wanted.

## Bug found post-implementation: listener leak in the field editor panels (2026-09-08)

User reported an endless save loop in the Type Definition Model editor (repeating
`refresh() from TreeView` → `badge shown ERROR` → `Saved` roughly every 600ms, indefinitely).

Root cause investigation: `DataTypeConfigurationPanelController` (the dispatcher that swaps between the
7 per-basetype config sub-panels) loads all 7 sub-panel FXMLs once in `initialize()` and calls `setElement()`
on **all of them** on every selection, regardless of which one is actually shown - but never overrode
`destroy()` to tear them down, so only the dispatcher itself was ever unregistered from
`StudioEventManager`. Since `TypeDefintionModelEditorController` rebuilds the whole field-editor FXML tree
from scratch on every row selection (`loadEditor()`), and additionally reloads-and-reselects on every save
of its own model (`modelSaved()` → `projectItem.reload()` + `loadModel()`, a pattern unique to this editor,
called out in `AbstractEditorController`'s own javadoc), every such cycle leaked 7 stale
`AbstractPropertyEditor` listener instances. This was **pre-existing** (not introduced by this session's
work), but Phase 2's new nested `errorMessageController` inside `DataTypeEnumerationConfigurationPanelController`
added an 8th leaked instance per cycle, and `TypeDefinitionPanelController`'s pre-existing nested
`requirednessErrorMessageController` had the identical gap.

Could not conclusively prove via static analysis alone that this leak is *the* cause of the observed save
loop (a leaked panel's own `modelSaved()`/`elementValidated()` handlers are read-only - they call
`refreshValidationState()`, not `commitChange()` - so the leak explains unbounded listener/memory growth for
certain, but the exact mechanism turning that into a *save* loop wasn't nailed down with certainty).

Fixed regardless (a real bug on its own merits):
- [x] `DataTypeConfigurationPanelController.destroy()` now cascades to its 7 sub-panels
- [x] `DataTypeEnumerationConfigurationPanelController.destroy()` now cascades to `errorMessageController`
- [x] `TypeDefinitionPanelController.destroy()` now cascades to `requirednessErrorMessageController`
      (pre-existing gap, same class of bug, fixed for consistency)
- [x] Added `log.debug("[commitChange] ...")` diagnostics in `AbstractPropertyEditor.commitChange(Node)`/
      `commitChange()` naming the controller class + field id, so if the loop recurs after this fix, the
      next capture (enable debug logging) pinpoints the exact trigger instead of requiring another round of
      static-analysis guessing.

**If the loop still occurs after this fix**: enable debug logging and reproduce; the new `[commitChange]`
lines will show exactly which control/panel keeps re-firing on every cycle - that's the next concrete lead.

## Deferred / not attempted (candidates for a future session)

- String — duplicate error-message locales (SME's `ERROR_MESSAGE_LANGUAGES_DUPLICATE`)
- Number — amount trait + invalid fraction digits (SME's `A12_AMOUNT_AND_INVALID_FRACT_DIGITS`) -
  exact semantics unconfirmed against SME source, see Phase 1 note
- No fixture in `testing/workspaces/basic/models` exercises a standalone TdM (`tdonly`-equivalent)
  end-to-end through the actual app UI - all new-behavior tests are unit-level
  (`DocumentModelValidatorsTest`/`TransitiveTypeDefinitionsTest`), not run through the running app

## Carried over from prior session (uncommitted at plan time, verify still present)

- [x] `MissingReferenceValidator`: duplicate type-definition name within a model's own
      `typeDefinitions` (mirrors SME's `TYPE_DEF_NAME_DUPLICATED`)
- [x] `MissingReferenceValidator` + `TransitiveTypeDefinitions.hasUnresolvedImportChain`: flag an
      Import whose target's own import chain is broken further down
