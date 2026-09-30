# TODO

Cleaned up 2026-09-27 (this pass): fixed items from the previous pass (rewritten 2026-09-20) were removed —
what was done is in `git log` and, per feature, in `docs/sme-reference-comparison.md`. Everything below is open.
Conventions are in `CLAUDE.md`.

**Sandbox note (2026-09-27):** this pass ran in a cloud sandbox with no access to `C:\workspace\sme`,
`C:\workspace\a12\2606-06-doc` or `C:\workspace\RichTextFX` (all Windows paths outside the container) - only this
repo and `docs/sme-reference-comparison.md`'s own already-fact-checked text. Gaps below that would need a fresh
read of the SME source or BA doc to pin down an exact wire-value/label/candidate-rule (e.g. Form Model gap 8's
enum wire values, Overview gap 16's "dynamic-suffix fields excluded" rule, the Type Definition Model per-row
"invalid" indicator's exact trigger) were deliberately left alone rather than guessed at - a session with that
access should tackle those next.

## Open decisions (need the owner)

1. **Kernel dependency: may a12-studio rely on `internal`/`a12internal` kernel classes?** The 2026-09-19 spike found kernel `31.1.1` viable in-process (condition validation, DM expansion, additive join; TDG is enterprise-only and stays blocked), but nearly everything it uses has no stability guarantee. Needs mgm's answer, plus contract tests if yes. Until then only slices that need the reference graph alone are built clean-room (as the loop detection was). Blocks: semantic condition validation, real DM expansion / additive join, `BindingRepeat`'s deeper heterogeneous-relationship/multiplicity checks (Composed Document Models section below). Details: "Kernel dependency spike" in `docs/sme-reference-comparison.md`.
2. **Which model types to finish next.** Checked 2026-09-26: only Print is `enabled: false` in `model-versions.json` (opening it shows "not supported yet"); Mapping still has target + sources only and Structural Mapping is still a stub, so those two are enabled but hardly usable. Transformer, Model Graph Diagram, Link/Document do not exist. The comparison doc's ranking is structural mapping → mapping → additive overlay editing → print (typesetting is done). Decide the order, and whether the two near-empty enabled editors should be switched off again until they are usable.

## Open issues (defects and unverified behaviour)

Fixed 2026-09-27 (this pass): Print Typesetting Model's Model Settings dialog no longer shows Name/Description —
`ModelSettingsDialog.initialize()` had regressed so `modelSettingsNameController.setModel/.focusNameField()` ran
unconditionally instead of being guarded (and hidden) by the roles-only flag for a Typesetting Model; the two calls
are back inside the `generalAndRolesOnly` guard and `modelSettingsNameController.setVisible(false)` is now also set
alongside the dialog's other Typesetting-only hides. `TypesettingModelEditorTest.theModelSettingsDialogOffersNothingButTheRoles`
is green again (verified under Xvfb — this container can run the JavaFX UI test suite headed via
`Xvfb :99 -screen 0 1280x1024x24 &`, `export DISPLAY=:99`, then plain `./gradlew ... test`; a bare `./gradlew test`
with no `DISPLAY` silently skips every FX-toolkit test instead of failing, so a green run without `DISPLAY` set proves
nothing).

Fixed 2026-09-27 (this pass, found while re-running the full suite, unrelated to the item above): a stale fixture,
`testing/workspaces/advanced_new/models/80_Cities/City_Fm.json`, had a `groupConfiguration` dependent-group entry
hiding `group_c28f9` when Confirm field `field_69593` ("HelperDistrict") equals `"false"` — a Confirm field only
ever has `(no value)`/`"true"` (`DependentControlSupport.masterValues`, ConfirmFieldType has no false state), so this
case could never fire and `FormDependencyDriftValidator` correctly flagged it as drift. Removed the dead
`dependentGroup` entry (`groupConfiguration` is now `{}`, matching what a save already produces for an empty one -
round-trip-checked). `FixtureWorkspacesFormValidatorsTest.realFormModelsHaveNoDriftAgainstTheirDocumentModels` and
`AdvancedNewProjectModelsRoundTripTest`/`ProjectsFolderModelsRoundTripTest` are green again.

Fixed 2026-09-27 (this pass): the cross-cutting "`HeaderRolesValidator` not wired into every model type's
validation service" gap (tracked separately under several model-type sections below) is now closed for **Query
Model** and **Master Detail Model** — both services register `HeaderRolesValidator`, pinned by new
`QueryModelValidationServiceTest`/`MasterDetailModelValidationServiceTest`. Every other model-type service already
had it (see the git log entries for Application/Relationship/Combined Document/Content/Form/Tree/Typesetting/
Relationship UI Models). No model type is known to still be missing it.

**Correction, fixed 2026-09-29 (this pass):** the line above was wrong - `DocumentModelValidationService`,
`OverviewModelValidationService`, `SelectionModelValidationService` and `PrintModelValidationService` were still
missing `HeaderRolesValidator` (confirmed by direct inspection of all 15 `*ValidationService` classes, not just
re-trusting the earlier claim). All four now register it, matching every other model type; pinned by four new
tests (`DocumentModelValidationServiceTest`/`OverviewModelValidationServiceTest`/
`SelectionModelValidationServiceTest`/`PrintModelValidationServiceTest`, mirroring
`MasterDetailModelValidationServiceTest`'s shape). Full `a12-studio-models-validation` suite and the
`documentmodel`/`overviewmodel`/`selectionmodel`/`printmodel` slices of `a12-studio-ui`'s suite stay green - no
real fixture's roles annotation triggers a new finding. **No model type is now actually missing it** (verified
by grepping every `*ValidationService` file for the registration, not just by memory of prior passes).

Manual checks (no known defect, just not yet verified):
- Check the SME for references in where in error messages the `$path$` notation is used. (Rename/move rewriting for these is unit-tested; what is left is checking it in the UI.)
- Drag and drop in general; error handling when dropping from a repeatable group into a regular group; dnd of sections with multi-select.
- Trigger and dependency icons on tree rows: SME's T/D flags are not ported, so check what is shown and add them.
- Merge the Settings and Control tabs of the field editor; note that dependencies are only shown for fields that have values.
- Every combo box should offer an empty value so a selection can be reset.
- When a rule is created, pre-fill its name from the field or group it targets.

**Environment note (2026-09-27):** four pre-existing `a12-studio-ui` JavaFX tests fail under this sandbox's Xvfb
even on a clean checkout with no code changes — `StudioTabPaneTest` ("rule 4 below its tabs, not over them"),
`TabPaneControllerTest` ("JavaFX thread did not finish in time"), `ContentModelEditorPanelsTest` (NPEs on a
drag-and-drop `target`, a heading-wrapping assertion) and `TypeDefinitionTableControllerTest` (NPEs on
`DocumentModelContent.getModelRoot()`). All four are layout-size/timing-sensitive (drag-and-drop simulation, text
wrapping, thread-join timeouts) and reproduce identically with or without any of this pass's changes - confirmed by
stashing and re-running. Left open as an environment limitation, not a code defect; re-verify on a real display
before trusting a "fixed" claim for anything touching these four classes.

## Open todos

### Form Models
Full review 2026-09-27 against SME's Form-Model meta-model (`FormModelFrame.json`'s graph-level `Rule`s and the
shared `I_Label`/`I_ScreenElementBase`/`I_RepeatOverviewColumnBase`/`I_ButtonStyling` mixins — SME's own ground
truth, not just fixtures/`.tsx`), the 21 `FMCustomConditions` and the BA doc: "Form Model: gap review
(2026-09-27)" in `docs/sme-reference-comparison.md` (9 numbered gaps, a table and the suggested order; the numbers
below are its gap numbers). Gaps 1-7 and 9 are done (see git log/doc); gap 8 is still open.
- **Gap 8, only when needed:** "Preprocessing Settings" (`FormModelContent.openNewDocumentPreProcessing`/
  `openExistingDocumentPreProcessing`) has fields on the Java model but zero editor panel - the shape is fully known
  from the meta-model, no fixture currently needs a non-default value.
- **Gap 9 fixed 2026-09-29 (this pass):** "Copy Hide Condition"/"Paste Hide Condition" (SME: tree-level actions
  with Ctrl+H/Ctrl+B) are now on `HideConditionPanelController` itself as two icon buttons, rather than as tree
  context-menu items - a12-studio already funnels every node type's (Section/Row/ControlGrid/Repeat/Control)
  Hide Condition editing through this one shared panel, unlike SME where it can also be done purely from the
  tree without opening an editor, so this is a translate-not-copy fit for the existing architecture rather than
  new tree/keyboard-shortcut plumbing. A static `copiedHideConditionSource` (the copied-from panel's own
  getter) mirrors SME's re-resolve-at-paste-time semantics: Paste always reads the source's *current* value,
  not a Copy-time snapshot, and clones it (JSON round-trip, matching `FormModelActions`' own clipboard clone
  pattern) so the target gets an independent copy. Pasting onto a node that already has a Hide Condition shows
  SME's own confirmation dialog before overwriting. Pinned by 5 new
  `HideConditionPanelControllerTest` cases (button enablement tracking, independent-clone paste, fresh-value-at-
  paste-time, no-confirmation-on-a-blank-target).
- Not re-listed as its own gap: the interactive Commit/Edit/Delete refactoring dialog SME shows when deleting a
  Screen/Control that's referenced elsewhere is the same cross-cutting missing feature as Application Model gap 6
  above (a12-studio only catches the dangling reference after the fact, as a validation error) - fix belongs with
  that follow-up, not a Form-Model-only patch.

Manual checks (no known defect, just not yet verified):
- Drag and drop in general; error handling when dropping from a repeatable group into a regular group; dnd of sections with multi-select.
- Trigger and dependency icons on tree rows: SME's T/D flags are not ported, so check what is shown and add them.
- Merge the Settings and Control tabs of the field editor; note that dependencies are only shown for fields that have values.
- Every combo box should offer an empty value so a selection can be reset.
- When a rule is created, pre-fill its name from the field or group it targets.

### Document Model
Full review 2026-09-27 against SME's Document-Model meta-model (`Domain{Field,Group,Rule,Computation,...}.json` — SME's Document Model editor is itself generated at runtime from these, so they're ground truth for every field/rule it enforces) and `DMValidationService.kt`: "Document Model: gap review (2026-09-27)" in `docs/sme-reference-comparison.md` (11 numbered gaps, a table and the suggested order; the numbers below are its gap numbers). Gaps 1-6 and 9 are done (see git log/doc).
- **Gaps 7, 8: round-trip-only fields, no UI needed (SME has none either).** `IncludeConfig.includeLevel` and `ComputationConfig`/`ComputationAlternative.roundingMode` are both unmodeled in the Java classes and silently dropped on load; add only for lossless round-trip when a real fixture needs it.
- **Gap 10 partly fixed 2026-09-30 (this pass): `YOUNGER1900_CHECK_INVALID` only.** Re-read `DomainField.json` directly (`C:\workspace\sme`) rather than trusting the doc's prior "conditions are now known" note at face value, and found the other two rules don't actually port cleanly onto a12-studio's data model: `OPTIONAL_DATE_TYPE_INVALID`/`OPTIONAL_DATE_RANGE_INVALID` key off SME's `optionalDateType` field, which has no equivalent anywhere in a12-studio (`DateFragmentFieldType`, despite the name, models something else - see the doc's corrected gap 10 entry); `INTERPRETATION_OF_YEAR_INVALID`/`_MISSING` need a "is this model year-based" input (SME's `ModelInfo.baseYear`) that a12-studio's `ModelInfo` doesn't carry at all - a real, separate round-trip/UI gap, not something to guess a substitute for. New `DateYounger1900ConfigValidator` (`a12-studio-models-validation`, registered in `DocumentModelValidationService`) ports only `YOUNGER1900_CHECK_INVALID` (`youngerThan1900Check` requires a format containing a year), scoped to `DateFragmentFieldType`/`DateRangeFieldType` (the only two a12-studio types that actually expose this field) and applied to both fields and type definitions, matching `DateFormatConfigValidator`'s own pattern - including on `DateRangeFieldType`, which has no UI for this field at all (an SME "expert" property neither editor exposes) but can still carry it via a hand-edited/imported file, same defense-in-depth reasoning as `DateFormatConfigValidator`. Pinned by `DateYounger1900ConfigValidatorTest`-equivalent case in `DocumentModelValidatorsTest` (`dateYounger1900ConfigValidatorReportsYoungerThan1900CheckWithoutAYearInTheFormat`); full `a12-studio-models-validation` suite green (only the pre-existing, already-flagged `Company_OM.json` regression noted above remains). `optionalDateType`/`interpretationOfYear` left open - see the doc's gap 10 for what each actually needs.
- **Gap 11 fixed 2026-09-30 (this pass).** Re-read `DomainField.json` directly (`C:\workspace\sme`) for the exact
  conditions rather than guessing: the Number section's full rule set is `MIN_FRACT_DIGITS_MISSING`/`MAX_FRACT_DIGITS_MISSING`
  (required-ness, not attempted - see below), `FRACT_DIGITS_INVALID`/`MIN_VALUE_BIGGER_THAN_MAX_VALUE` (already
  covered), and `A12_AMOUNT_AND_INVALID_FRACT_DIGITS` (not covered - now fixed). **No SME rule for
  `positivesOnly`-vs-negative-`minValue` or `maxIntegerDigits`-vs-`maxValue`-digit-count exists at all** - both
  were speculative carry-overs from an earlier, less careful pass; dropped rather than implemented. `NumberTypeConfigValidator`
  now also checks `A12_AMOUNT_AND_INVALID_FRACT_DIGITS`: a `trait=amount` (a12-studio's wire value is lowercase,
  unlike SME's `Amount`, confirmed via real fixtures e.g. `Invoice-Includes/Order_DM.json`) field's `minFractionalDigits`/`maxFractionalDigits`
  must be equal and either both 0 or both 2. Separately, `CustomFieldTypeConfigValidator` gained the
  `MIN_LENGTH_BIGGER_MAX_LENGTH` check for `CustomFieldTypeOptions.minLength`/`maxLength` (String's own equivalent
  already existed; Custom's was the actual gap, confirmed against SME source). Number's own `minLength`/`maxLength`
  (string-representation length, a *different* SME rule pair scoped to `[/Data/Field/type->superType] ==
  "Number"`) has no a12-studio equivalent field at all - like gap 10's `optionalDateType`, nothing to port; not a
  gap. **`MIN_FRACT_DIGITS_MISSING`/`MAX_FRACT_DIGITS_MISSING` (required-ness) checked and confirmed not
  portable:** unlike Date's `format` (always defaulted by the panel, so `DateFormatConfigValidator` is pure
  defense-in-depth), `DataTypeNumberConfigurationPanelController` deliberately makes `minFractionalDigits`/
  `maxFractionalDigits` an optional pair behind its own "has decimal places" checkbox - a real, intentional
  a12-studio divergence from SME (which requires both, always, for any Number-superType field), not an oversight.
  Confirmed against the real fixture corpus: 29 of 68 real `NumberType` occurrences across `testing/workspaces`
  have no fractional digits at all. Porting this rule literally would flag ~43% of real, valid Number fields as
  errors - not attempted, and shouldn't be without a design decision to make fractional digits mandatory first.
  Pinned by 2 new `DocumentModelValidatorsTest`
  cases (`numberTypeConfigValidatorReportsInvalidAmountFractionalDigits`, 4 fixture entries covering mismatched/
  wrong-digit-count/valid/wrong-trait; `customFieldTypeConfigValidatorReportsMinLengthBiggerThanMaxLength`). Full
  `a12-studio-models-validation` suite green (only the pre-existing, already-flagged `Company_OM.json` regression
  remains).
- **Cross-cutting gap fixed 2026-09-29 (this pass):** no model type validated duplicate annotation names (SME's
  `annotationNamesNotUnique`, `core/ModelHeader.json`'s `RepetitionNotUnique(annotations/name)`) - not
  Document-Model-specific, so folded into the same follow-up as the cross-cutting `HeaderRolesValidator` gap
  rather than fixed here alone. New `AnnotationDuplicateValidator` (header annotations only - a Document Model
  `Element`'s or a Form Model `ScreenElement`'s own `annotations` list is a separate, larger gap, not covered)
  is now registered in all 14 model-type validation services, matching `HeaderRolesValidator`'s own rollout.
  Pinned by `AnnotationDuplicateValidatorTest`. Full `a12-studio-models-validation` suite green; no real fixture
  trips a new finding.

Manual checks:
- Check the SME for references in where in error messages the `$path$` notation is used. (Rename/move rewriting for these is unit-tested; what is left is checking it in the UI.)

**New 2026-09-29 (this pass): `FixtureWorkspacesDocumentValidatorsTest` built (real `Project.load()`, no flat
model list - see the Overview section for why that matters).** One real, ERROR-severity finding across the
whole fixture corpus, and it's accurate, not a bug: `e-commerce/models/01_Products/ProductMovie_DM.json`'s
"Languages" multi-select group points its value field at `CommonTypes_TDM`'s shared "Language" Type Definition
- `MultiSelectGroupValidator` correctly rejects this per the BA doc ("imported and included Type Definitions
are not allowed" for a multi-select value field), confirmed by tracing the type definition to its owning
model. Left as a known, named exclusion in the test (not silently dropped) rather than "fixed" by guessing at
the right correction (inline the type locally vs. drop multi-select) - flag for whoever owns fixture content
next.

**Same pass: fixture-based regression coverage extended to every remaining model type.** Following the same
pattern (real `Project.load()` per workspace; only ERROR severity asserted, not WARNING), built
`FixtureWorkspacesApplicationValidatorsTest`, `FixtureWorkspacesRelationshipValidatorsTest`,
`FixtureWorkspacesRelationshipUiValidatorsTest`, `FixtureWorkspacesMasterDetailValidatorsTest`,
`FixtureWorkspacesSelectionValidatorsTest`, `FixtureWorkspacesPrintValidatorsTest` and
`FixtureWorkspacesTypesettingValidatorsTest`. Combined with the pre-existing Form/Query/Tree/Content/Combination
tests and this pass's new Overview/Document ones, **all 14 model-type validation services now have a real-fixture
regression guard** against a rule becoming stricter than SME's own (Application/Relationship/RelationshipUI/
MasterDetail/Selection/Print/Typesetting: zero findings, nothing more to fix).

### Type Definition Model
Gap review 2026-09-27 against SME's dedicated `typeDefinitionModel` module and the type-definition editing code it
shares with a plain Document Model's own Type Definitions tab — see "Type Definition Model" in
`docs/sme-reference-comparison.md` for the full write-up (a12-studio's port is already close to parity: every
field-type-config validator already walks `content.typeDefinitions`, name-uniqueness/missing-reference/broken-import
checks all match SME's real rules, and a12-studio's Delete/Remove-Import confirmation dialogs are stricter than
SME's, which has none at all). The real architectural gap (mixed local+imported type definitions in one TDM) is
already fixed (see git log).
- **Fixed 2026-09-28 (this pass):** the per-row "invalid" indicator (SME's `TypedefOverview` `invalidTypeDefs`
  column) is now shown in `TypeDefinitionTableController` - a red name + tooltip listing the actual validation
  messages, matching this codebase's existing per-row convention (`ElementNameTreeCell`/`FormModelTreeCell`) rather
  than a separate icon column. `refreshValidationState()` validates this model plus, for every distinct owning model
  among the included/imported rows, that model too, so a problem on an inherited type definition (e.g. a duplicate
  name inside the model that actually owns it) still marks the row here. Pinned by two new
  `TypeDefinitionTableControllerTest` cases (own-model and owning-model invalidity).
- Still open, cosmetic: `testing/workspaces/advanced_new/models/CommonFieldDefinitions_Td.json` uses a non-conforming
  `_Td` suffix instead of `_TDM` (invisible today because that workspace disables suffix enforcement, and not worth
  a standalone rename since three other fixtures reference it by id).

### Relationship Models
Full gap review 2026-09-27 against SME's `relationshipModel` module — see "Relationship Model: gap review" in `docs/sme-reference-comparison.md` for the full write-up (SME's own implementation turns out to be a declarative Document+Form Model with no bespoke code at all, so most of a12-studio's hand-built editor is already at or above parity). The bulk of the gap list is already fixed (see git log).
- Lower priority, still open: `EntityCharacteristic.navigable`, `Multiplicity.lowerLimit`,
  `EntityCharacteristic.candidateConstraints` round-trip fields — unreachable from SME's own editor UI too, so
  unlikely to appear in real files.

### Composed Document Models
- Full diagram-driven authoring (SME's "Relationship Element"/"Link Relationship Element" diagram nodes) is intentionally **not** going to be re-implemented - a12-studio represents the relationship chain as a structured list editor instead. Decided 2026-09-26: no plan to port the SME diagram UX.
- `BindingRepeat`'s deeper heterogeneous-relationship/repetition-vs-multiplicity checks (SME's `DescendantOfHeterogeneous(ToMany)Relationship`/`InvalidBindingRepeatRepetitionAndMultiplicity`) need kernel-backed DM expansion - blocked on Open Decision #1, not on CDM itself any more.

### Combined Document Model
Gap review 2026-09-27 against SME's `combinationModel` module — see "Gap review (2026-09-27)" under "Combined Document Model" in `docs/sme-reference-comparison.md`. The bulk of the gap list is already fixed (see git log).
- Still open: no user-facing Preview of the expanded/merged Document Model (SME has one, kernel-backed); a partial
  Addition-only-merge preview is buildable today on the existing `CombinedDocumentModelElements` helper without the
  kernel dependency — worth considering as a scoped slice of the otherwise kernel-gated feature.
- **Fixed 2026-09-29 (this pass):** added a cap of 99 Combination Steps on a Combined Document Model, matching
  SME - new `CombinationStepsMaxCountValidator` (registered in `CombinationModelValidationService`), pinned by
  `CombinationValidatorsTest.stepsMaxCountValidatorReportsMoreThan99Steps`/`stepsMaxCountValidatorAllows99Steps`.

### Query Model
- Filter expressions are only existence-checked; type and enum-value checking is not done, and there are no "did you mean" candidates.
- **Fixed 2026-09-28 (this pass):** `QueryFieldReferenceValidator` now rejects an `indexed = false` field in `content.fields[]`
  (reusing `OverviewElementResolution.isIndexedFalse`/`validation.common.indexedAnnotationFalse`, matching the existing
  filter/aggregation validators' rule for the same annotation), and the tree's "In Result" checkbox
  (`QueryModelTreeController.InResultCell`) is now disabled for such a field (`QueryTreeRow.isIndexedFalseField()`).
  Group/document-node/relationship-link rows are unaffected (they aggregate descendants and still get flagged per-field
  by the validator). Pinned by `QueryValidatorsTest.fieldReferenceValidatorRejectsANotIndexedField`. Not run against a
  local Gradle build in this pass - the sandbox's egress policy blocks `plugins.gradle.org`/`services.gradle.org`
  needed to resolve the root build's license-report plugin (confirmed via `curl .../__agentproxy/status`, both classed
  as policy denials, not transient) - verify with CI/a full build once merged.
- The Model Tree tab's root DM is picked in the Settings tab, not through an ER-diagram picker like SME.
- **Fixed 2026-09-29 (this pass):** a role rename now also propagates to a `Has("<relationship>", "<role>", ...)`
  call written as free-text `filterDefinition` (root or any relationship hop, nested `Has(...)` inside a
  `constraint`/`linkConstraint` included), not just the structured `constraint`/`QueryLink`/`QuerySort` tree
  (`RoleRenameRefactoring`, since 2026-09-22) - the two representations of the same filter no longer disagree
  after a rename. Implemented by extending `QueryLanguageReferences.HasCall` with `targetRoleStart`/
  `targetRoleStop` (the role argument's source position, alongside the existing `relationshipStart`/
  `relationshipStop`) and reusing the same parse-tree-based rewrite approach
  `de.a12.studio.models.util.ModelReferenceRewriter` already uses for that call's relationship-id argument -
  text that isn't valid Query Language is left alone, matching that same behavior. Pinned by 4 new
  `RoleRenameRefactoringTest` cases (root filter, a matching-vs-non-matching link filter, nested `Has(...)`,
  invalid Query Language left alone).
- `HeaderRolesValidator` gap fixed 2026-09-27 (this pass) — see "Open issues" above.
- **Fixed 2026-09-27 (this pass):** a Combination Model can now be picked and resolved as a Query target -
  `QuerySettingsPanelController.documentModelOptions()` now offers combinations too
  (`ProjectDocumentModels.getOtherDocumentModelsWithCombinations`), `QueryModelTreeController.resolveTargetDocumentModel()`
  resolves through the same combination-aware helper for the Model Tree tab, and
  `QueryElementResolution.targetDocumentModel()` (shared by the field/sort/filter/aggregation validators) now falls
  back to `CombinedDocumentModelElements.resolveForFieldReferences` the same way Overview/Content Model already do.
  New `QueryModelCombinationTargetTest` pins it against the real `advanced_new/PersonEmployee_Cm` fixture.

### Form Engine preview (built 2026-09-25, see "Form Engine preview" in `docs/sme-reference-comparison.md`)
- Theme and Data menus of the preview are empty: offer the project's `.theme` files (`request-theme` -> `send-theme`) and sample documents (`request-document` -> `send-document`) from `form-engine-bootstrap.js`/`PreviewServer`; decide whether edits made in the preview (`create-document`/`update-document`) may be saved.
- Ad hoc testing of an Additive Document Model (needs its Combination Model as context, like SME's `contextData`); today the button is disabled.
- The generated ad hoc Form Model comes from `FormScreenGenerator`, not SME's `form-model-generator` (public npm package `@com.mgmtp.a12.formengine/form-model-generator`): compare on a larger model and align labels/grouping if they differ.
- The preview needs the Simple Model Editor of the configured A12 installation (`SmeInstallation`); without it the Form Model button falls back to the old wireframe and Ad Hoc Testing shows an error page. Decide whether that fallback should say so in the studio itself.

### Overview Model
Full review 2026-09-26 against SME's `overviewModel` module, its meta model (every rule) and the A12 2026.06 BA doc: "Overview Model: gap review" in `docs/sme-reference-comparison.md` (17 numbered gaps, a rule-by-rule coverage table and the suggested order; the numbers below are its gap numbers). Gaps 1-15 and 17 (partial) are done (see git log/doc).
- **Gap 16 fixed 2026-09-30 (multi-select/filter-field/Section Data):** the Column dialog's Element Reference picker, the Custom Selection Of Fields row picker and the Section Data field picker each offered every element with no restriction. Added `OverviewElementResolution.isEnumerationMultiSelect(ElementIndex, Element)` (mirrors SME's `DocumentModelApi.isEnumerationMultiSelect` - a multi-select group whose single value field is Enumeration-typed) and applied it in `OverviewElementOptions`: `columnElementIds()` now excludes a multi-select group unless it's an enumeration multi-select (`isMultiSelect(element) ? isEnumerationMultiSelect(element) : true`, matching SME); `customSelectionFieldIds()` (new method, replacing `elementIds()` at its one prior call site in `CustomSelectionOfFieldsPanelController`) restricts to Fields plus enumeration multi-select groups (`isFieldLike(element) || isEnumerationMultiSelect(element, documentModel)`), and is now also used by the Section Data picker (`FieldReferencesPanelController`, SME's own candidates for both are the same function - `getFilterValuesForSectionData` delegates to `getFilterValuesByFilterMode`). Verified against every real multi-select group in `testing/workspaces` (all Enumeration-typed, so nothing currently valid is excluded) and `ProductMovie_OM.json`'s `custom_list` field referencing its enum multi-select group directly by id (still resolves). New `OverviewElementOptionsMultiSelectTest` (4 cases: enum multi-select offered by both pickers, String multi-select offered by neither, a plain non-multi-select Group offered by the Column picker but not by Custom Selection Of Fields, a plain Field offered by both). Usability only, as with gap 15 - doesn't stop an already-invalid file's dangling/repeatable/String-multi-select reference from still displaying. Still open from gap 16: the "already-used fields excluded" and "dynamic-suffix fields excluded" sub-clauses of SME's filter-field candidate rule (found in SME source, not ported - keeping this pass bounded), and the Screen Reader Column candidate rule (no SME source recipe found yet).
- **Pre-existing test bug fixed 2026-09-30 (found while verifying gap 16, unrelated to it):** both
  `CustomSelectionOfFieldsPanelControllerTest` cases failed with an NPE (`Studio.getValidationService()` null)
  before ever reaching the picker logic under test - confirmed pre-existing on `main` (unmodified by gap 16's
  changes, reproduces identically after stashing them) rather than a regression. The test never called {@code
  FxTestSupport.setValidationServiceForProject}, unlike its sibling panel tests (e.g.
  `TreeExpansionStrategyPanelsTest`); added the same `@BeforeAll`/`@AfterAll` setup (an empty temp-dir-backed
  `Project`) both were already missing.
- **Gap 17, still open (partial):** 3 of SME's 5 refactoring-dialog behaviors are done (stale Default Sorting pruning, sub-header element pruning on toggle-off, Style rename/delete cascade). Not done: deleting a filter field, and event/model reference cascades. Checked SME source (`overviewRefactoring.ts`) 2026-09-29 looking for a concrete recipe to implement against, unlike Master Detail gap 1's - found no bespoke SME code matching either behavior (that file only handles cross-model rename, not within-model delete cascades), so implementing this now would mean guessing at exact semantics rather than porting a known rule; left open rather than guessed at.
- **New 2026-09-29 (this pass): `FixtureWorkspacesOverviewValidatorsTest` built and one real bug found/fixed.**
  No fixture-based regression test existed for the Overview validators (unlike Form/Query/Tree). Built one
  (real `Project.load()` per workspace, not a flat model list, so Combination Model link-document resolution
  works correctly) and used it to re-verify the "Re-checked with OverviewBindingPurpose in hand" note in the
  doc's gap review: its "link-column field resolution" false-positive claim turned out to be an artifact of
  that investigation's own flat-list test context (see doc), not a real bug - retracted. Its `bindingConfiguration`
  wire-shape gap is real but currently causes no actual validator misbehavior (also re-verified) - still open,
  see the doc. Separately found and fixed a genuine bug: `OverviewEnumeratedStringFilterValidator` fires
  unconditionally whenever `enumeratedStringFilter` is present, but SME's real rule
  (`OverviewMetaModel.json`'s `fieldIdsMustBeFilled`) also gates on the object's own `enabled` field, which
  a12-studio's `EnumeratedStringFilter` class doesn't model at all - `FilterStringFieldsMultiSelectPanelController`'s
  own javadoc already documents the deliberate design choice ("object present = enabled, matching SME's export
  behavior of never writing a present-but-disabled object"), so this wasn't a validator bug so much as a stale
  fixture: `testing/workspaces/basic/models/Company_OM.json` had an `enumeratedStringFilter: {pagingSize: 10}`
  block with no `fields` - impossible to produce through today's editor (which always includes the fields
  editor) and a leftover from before gap 1's `FilterStringFieldsMultiSelectPanelController` fields-editor was
  built. Removed the dead block (round-trip-checked, `BasicProjectModelsRoundTripTest` green). The 7 remaining
  `OverviewColumnHeaderLabelOrIconValidator` WARNINGs found across real fixtures (an attachment-Group-referencing
  column with neither icon nor label, e.g. "Photo"/"Flag"/"Picture") are checked against SME's own
  `elementRefHasNoLabel.ts` and are a faithful port, not over-strict - the new test only asserts zero ERRORs,
  not zero WARNINGs, for this reason (see the test's own javadoc).

**Regression, found 2026-09-30 (not fixed - flagging, not reverting):** `testing/workspaces/basic/models/Company_OM.json`
changed on disk since the fix above (external edit, not this session's) and the empty, invalid `enumeratedStringFilter`
block (no `fields`, `enableFilter: true`) is back - `FixtureWorkspacesOverviewValidatorsTest` is red again for
exactly the reason it was before. Per the same reasoning as the original fix, this is genuinely invalid per
SME's own rule, not a validator bug. Left the file as-is (the change looked like a real editing-session save,
not a targeted revert, and unrelated new columns were added alongside it) rather than silently re-removing it -
whoever owns this fixture should decide whether to add fields to the "Filter String Fields with Multi-Select"
list or drop the feature again.

### Content Model
Full review 2026-09-26 against SME's `contentModel` module and the installed client bundle (SME's own validation
isn't in the SME repo): "Content Model: gap review" in `docs/sme-reference-comparison.md` (17 numbered gaps, a
table, "What SME reports" and the suggested order - six rounds of "Status" updates the same day closed gaps
1, 2, 3, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16 and, for Content Models, 17; gap 4 (migration) is Won't Do by
decision (see below); only gap 9 was left "only partly" done.
- **Gap 9 mostly closed 2026-09-29/30 (this pass): the CSS length/spacing/numeric half of "per-property setting
  validation is not re-run on load/raw edit."** `ContentSettingsValidator` already covered required fields, the
  Image source and unsafe URLs; new `ContentSettingValueValidator` + `ContentPropertyFormatRules` (in
  `a12-studio-models-validation/.../validators/content`) cover CSS length (`LengthRow`), CSS shorthand spacing
  (`SpacingRow`) and whole-number (`NumberRow`) settings across the 9 `*-panel.fxml` files that use them
  (dimensions/appearance/background-image/border/icons/layout/date-picker) - each panel's own `path`/`types`/
  `keywords`/`units` FXML attributes are the source of truth the rule table transcribes, so this is a port, not
  a guess. A settings row is checked whenever its path is present, regardless of whether the editing row is
  currently shown (`showWhen`/`enabledWhen`), matching how SME's own controller validates whatever is stored.
  **Deliberately still open:** `ColorRow`/`ShadowRow` settings (`color-panel.fxml`, `shadow-panel.fxml`) -
  cloning CSS color/shadow syntax accurately enough to avoid false positives needs the same "read it out of the
  installed bundle" treatment the rest of gap 9 review used, not a guess (see `ContentPropertyFormatRules`'s
  javadoc); this is the only remaining piece of gap 9. The table currently duplicates the FXML rows' own
  `keywords`/`units` rather than the two sharing one source (the doc's original suggested order called for
  extracting them into `a12-studio-models` so the UI rows read from there too) - a manual-sync risk worth
  closing the next time either side is touched, not attempted this pass to keep scope bounded to
  `a12-studio-models-validation` alone. Pinned by 10 new `ContentSettingValueValidatorTest` cases; the real
  Content Model fixtures (`FixtureWorkspacesContentValidatorsTest`) trip no new finding.
- Not in TODO.md before this pass even though the gap review existed since 2026-09-26 - added this section so
  future passes don't have to rediscover the doc section to find out what's left.

### Application Model
Full review 2026-09-27 against SME's `appModel` module, its docs (`docs/modules/appModel/*.adoc`) and every real `*AppModel*.json` fixture: "Application Model: gap review" in `docs/sme-reference-comparison.md` (9 numbered gaps, a table and the suggested order; the numbers below are its gap numbers). Every SME editor screen already has a counterpart, including Model References and cross-model rename propagation. Gaps 1-8 are done (see git log/doc).
- **Gap 9, only if needed (still open):** nested subregions beyond one level have no UI (no real fixture needs it today).

### Master Detail Model
Full review 2026-09-27 against SME's `masterDetailModel` module, its self-hosted meta-model DM (`ModuleMasterDetail.json`) and the BA doc (`docs/modules/masterDetailModuleModel/index.adoc`): "Master Detail Model: gap review" in `docs/sme-reference-comparison.md` (5 numbered gaps, a table and the suggested order; the numbers below are its gap numbers). `MainDetailModelEditorController` and its five panels already mirror SME's `formMappingMiddleware`/`syncRelationshipEditors`/`syncLinkDocumentEditors`, both reference validators cover every rule in the meta-model DM, `MasterDetailModuleGenerator` is a faithful tested port of `masterDetailModule.ts`, and cross-model rename propagation needs no masterDetailModel-specific code - what is left:
- **Gap 3 fixed 2026-09-27 (this pass):** `HeaderRolesValidator` is now registered in `MasterDetailModelValidationService` (see "Open issues" above).
- **Gap 1 fixed 2026-09-29 (this pass), the real feature gap: no heterogeneous (abstract/subtype) or CDM
  expansion in the Form Mapping / Relationship Editors candidate lists.** SME's `resolveAndFilterAbstractDocuments`
  replaces a Composed Document Model reference with its query root and expands an abstract Document Model into
  its concrete subtypes, recursively - a documented, cypress-tested feature (BA doc's "Heterogeneous Overview
  Module"/"Tree Module" sections). New `MainDetailModelEditorController.resolveAndExpand` does the same, reusing
  the existing building blocks (`ComposedDocumentModelResolver.getQueryRootId`, `TreeHeterogeneity.info`/
  `allDocuments` over `DocumentModelHeterogeneity`'s super/subtype graph) rather than writing new expansion
  logic, and is applied to `referencedDocumentModelIds` (Form Mapping) and `relationshipEditorDocumentModelIds`
  (Relationship Editors) - not `linkDocumentEditorDocumentModelIds`, which SME's own `syncLinkDocumentEditors`
  doesn't expand either. A heterogeneous or CDM-backed master model now gets one row per concrete subtype/query
  root instead of one unusable row for the abstract/CDM-member id itself; a dangling reference is kept as-is so
  the existing reference validator still flags it. Pinned by 2 new `MainDetailModelEditorControllerTest` cases
  (abstract-to-concrete-subtypes expansion, CDM-to-query-root replacement) using in-memory fixtures (no real
  heterogeneous fixture exists under `testing/workspaces/**` yet - add one there too if this editor needs
  end-to-end manual verification).
- **Gap 2 fixed 2026-09-27 (this pass):** Binding Overview Models are now excluded from the Overview Model combo -
  `MainDetailModelEditorController.overviewModelOptions()` filters out any Overview Model id for which
  `OverviewBindingPurpose.resolve(id, otherModels)` returns non-null, gathering `otherModels` from
  `ProjectDocumentModels.getOtherModelsOfType` for `FORM` and `RELATIONSHIPUI` (the two model types
  `OverviewBindingPurpose` inspects) rather than the `ValidationContext`-only overload. No dedicated UI test was
  added - the `maindetailmodel` editor package has no FX test scaffolding at all yet (a pre-existing gap, not
  introduced here), while the filtering logic itself (`OverviewBindingPurpose.resolve`) is already covered by
  `OverviewBindingPurposeTest`. Build a proper `MainDetailModelEditorControllerTest` (mirroring e.g.
  `ContentModelSettingsDialogTest`'s FX harness) the next time this editor is touched, and pin this exclusion then.
- **Gap 4 fixed 2026-09-29 (this pass):** `MasterDetailReferenceValidator` now also rejects a blank `documentModel`/
  `formModel` on any `formMapping`/`relationshipEditors`/`linkDocumentEditors` row (previously only a non-blank,
  dangling reference was checked) - matters only for a hand-edited or imported file, since the editor itself
  always fills `documentModel` and blocks saving on a blank `formModel`. Pinned by
  `MasterDetailValidatorsTest.referenceValidatorReportsBlankDocumentAndFormModel`.
- **Gap 5 fixed 2026-09-29 (this pass):** `MainDetailModelEditorController` now overrides `modelSaved` (calling
  `super.modelSaved(event)` first, then also reacting when the saved model is an `OverviewModel`/`TreeModel`,
  not just a `DocumentModel`) so saving the Overview or Tree Model currently selected as this module's master
  list, in a different tab, refreshes the Form Mapping/Relationship Editors/Link Document Editors panels
  immediately instead of only once this tab is closed and reopened. Also built the FX test harness this section
  called for "the next time this editor is touched" - new `MainDetailModelEditorControllerTest` pins both gap 5
  (`savingTheSelectedOverviewModelElsewhereRefreshesTheFormMappingPanelImmediately`) and gap 2's previously
  untested Binding-Overview-Model exclusion (`theOverviewModelComboExcludesABindingOverviewModel`).

## Blocked (waiting for an input)

- **Overview filter items:** Boolean/Confirm criteria-based configuration and Enumeration/Multi-select Initial Criteria, Pinned Values and join behaviour are not modeled: no fixture on disk (including the `A12 Tools - 2026.06` sample workspaces) has an example and the BA doc only has screenshots. Re-checked 2026-09-22 (a12-studio fixtures, the upstream `A12 Tools - 2026.06` workspaces, and SME's `overviewModel` TS types, which are simpler than a12-studio's own model and have no `FilterItem`/`FilterGroup`/pinned/initial-criteria concept at all) - still no example anywhere. Implement once a real example JSON turns up.
- **Overview DateFragment/DateRange periods:** `OverviewElementOptions.defaultPeriods` reuses Date's subset ({date, year, yearMonth, month}) by analogy. Verify against a real example and adjust.
- **Wire shapes never checked against a real SME file** (no fixture has them; shapes come from SME's meta model and the Data Services docs): `Control.index`, Query `aggregation` (`alias` only from SME's transformer), a `Has(...)` call inside a Query `filterDefinition`, and the Query filter reference validator (no real `filterDefinition` with references to sweep for false positives). Verify when a sample appears.

## Parked (do not start unless asked)

- Nothing parked at the moment.

## Won't do (decided 2026-09-19)

- AI-assisted Document Model generation (SME `documentModel/ai/*`).
- Model diff/compare editor.
- Model migration (decided 2026-09-26): a12-studio does not migrate models written for older versions of a model type or of the Content Engine (SME's `DefaultMigrator` / `namespaceVersions` steps, e.g. Image `src` string -> `{static: ...}`). Older files are not normalized on load, no migration action is offered, and `Studio.checkModelVersions` stays header-based. A Content Model with an older `namespaceVersions` entry only gets the validator's warning. Do not add migration code unless the owner reverses this.
