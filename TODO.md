# TODO

Cleaned up 2026-09-27 (this pass): fixed items from the previous pass (rewritten 2026-09-20) were removed —
what was done is in `git log` and, per feature, in `docs/sme-reference-comparison.md`. Everything below is open.
Conventions are in `CLAUDE.md`.

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
below are its gap numbers). Gaps 1-7 are done (see git log/doc); gaps 8-9 are still open.
- **Gap 8, only when needed:** "Preprocessing Settings" (`FormModelContent.openNewDocumentPreProcessing`/
  `openExistingDocumentPreProcessing`) has fields on the Java model but zero editor panel - the shape is fully known
  from the meta-model, no fixture currently needs a non-default value.
- **Gap 9, last, pure UX:** no "Copy Hide Condition" / "Paste Hide Condition" context actions (SME has both, with
  keyboard shortcuts).
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
- **Gaps 10, 11, lower confidence:** Date/DateRange "expert" field cross-gating (conditions are now known, see the doc) and a few narrower Number/Custom validator checks that need a `DomainField.json` re-read before implementing, not a guess.
- Cross-cutting, not Document-Model-specific: no model type validates duplicate annotation names (SME's `ANNOTATION_DUPLICATE`) - fold into the same follow-up as the already-tracked cross-cutting `HeaderRolesValidator` gap (now closed for every model type, see "Open issues" above) rather than fixing here alone.

Manual checks:
- Check the SME for references in where in error messages the `$path$` notation is used. (Rename/move rewriting for these is unit-tested; what is left is checking it in the UI.)

### Type Definition Model
Gap review 2026-09-27 against SME's dedicated `typeDefinitionModel` module and the type-definition editing code it
shares with a plain Document Model's own Type Definitions tab — see "Type Definition Model" in
`docs/sme-reference-comparison.md` for the full write-up (a12-studio's port is already close to parity: every
field-type-config validator already walks `content.typeDefinitions`, name-uniqueness/missing-reference/broken-import
checks all match SME's real rules, and a12-studio's Delete/Remove-Import confirmation dialogs are stricter than
SME's, which has none at all). The real architectural gap (mixed local+imported type definitions in one TDM) is
already fixed (see git log).
- Low priority, still open: no per-row "invalid" indicator column in the Type Definitions table (SME has one);
  `testing/workspaces/advanced_new/models/CommonFieldDefinitions_Td.json` uses a non-conforming `_Td` suffix instead
  of `_TDM` (invisible today because that workspace disables suffix enforcement, and not worth a standalone rename
  since three other fixtures reference it by id).

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
- Low priority, still open: no cap on the number of Combination Steps (SME caps at 99).

### Query Model
- Filter expressions are only existence-checked; type and enum-value checking is not done, and there are no "did you mean" candidates.
- `QueryFieldReferenceValidator` (the `fields[]` projection) still accepts `indexed = false` fields, and the tree checkbox does not disable them.
- The Model Tree tab's root DM is picked in the Settings tab, not through an ER-diagram picker like SME.
- A role rename propagates to the structured `constraint`/`QueryLink`/`QuerySort` tree (`RoleRenameRefactoring`, since 2026-09-22) but not to a `Has("<relationship>", "<role>", ...)` call written as free-text `filterDefinition` — the two representations of the same filter disagree after a role rename until re-saved from the text side.
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
- **Gap 16, still open:** pickers offer every element instead of SME's candidate rules. Partly fixed as a side effect of gap 15's work (the Column dialog's Element Reference picker now excludes repeatable fields). Still open: the multi-select ("only when enumeration multi-select" - not modelled, Studio has no such distinction), filter-field, enumerated-string (done as part of gap 1), Section Data and Screen Reader Column candidate rules.
- **Gap 17, still open (partial):** 3 of SME's 5 refactoring-dialog behaviors are done (stale Default Sorting pruning, sub-header element pruning on toggle-off, Style rename/delete cascade). Not done: deleting a filter field, and event/model reference cascades.

### Application Model
Full review 2026-09-27 against SME's `appModel` module, its docs (`docs/modules/appModel/*.adoc`) and every real `*AppModel*.json` fixture: "Application Model: gap review" in `docs/sme-reference-comparison.md` (9 numbered gaps, a table and the suggested order; the numbers below are its gap numbers). Every SME editor screen already has a counterpart, including Model References and cross-model rename propagation. Gaps 1-8 are done (see git log/doc).
- **Gap 9, only if needed (still open):** nested subregions beyond one level have no UI (no real fixture needs it today).

### Master Detail Model
Full review 2026-09-27 against SME's `masterDetailModel` module, its self-hosted meta-model DM (`ModuleMasterDetail.json`) and the BA doc (`docs/modules/masterDetailModuleModel/index.adoc`): "Master Detail Model: gap review" in `docs/sme-reference-comparison.md` (5 numbered gaps, a table and the suggested order; the numbers below are its gap numbers). `MainDetailModelEditorController` and its five panels already mirror SME's `formMappingMiddleware`/`syncRelationshipEditors`/`syncLinkDocumentEditors`, both reference validators cover every rule in the meta-model DM, `MasterDetailModuleGenerator` is a faithful tested port of `masterDetailModule.ts`, and cross-model rename propagation needs no masterDetailModel-specific code - what is left:
- **Gap 3 fixed 2026-09-27 (this pass):** `HeaderRolesValidator` is now registered in `MasterDetailModelValidationService` (see "Open issues" above).
- **Gap 1, the real feature gap: no heterogeneous (abstract/subtype) or CDM expansion in the Form Mapping / Relationship Editors candidate lists.** SME's `resolveAndFilterAbstractDocuments` replaces a Composed Document Model reference with its query root and expands an abstract Document Model into its concrete subtypes, recursively - a documented, cypress-tested feature (BA doc's "Heterogeneous Overview Module"/"Tree Module" sections). a12-studio has the building blocks already (`ComposedDocumentModelResolver.getQueryRootId`, `TreeHeterogeneity.info`/`allDocuments` over `DocumentModelHeterogeneity`'s super/subtype graph, both used elsewhere) but `MainDetailModelEditorController.referencedDocumentModelIds`/`relationshipEditorDocumentModelIds` don't call them - a heterogeneous or CDM-backed master model currently shows one Form Mapping row for the abstract/CDM-member id itself, which usually can't be edited directly, instead of one row per concrete subtype/query-root.
- **Gap 2: Binding Overview Models aren't excluded from the Overview Model combo**, unlike SME (BA doc is explicit about this). `OverviewBindingPurpose.resolve` (built for the Overview Model's own gap review) already detects this but needs a `ValidationContext` built from a `ProjectItem` - not something any `a12-studio-ui` code does yet, so this is also the first call site for that constructor helper.
- **Gaps 4, 5, last:** low priority (missing-field validator gap only matters for hand-edited JSON; stale-panel-until-reopen only matters while this editor tab is open during an unrelated Overview/Tree Model save elsewhere).

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
