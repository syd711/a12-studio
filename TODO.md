# Commons
- Why are not all rows editable for C:\workspace\a12-studio\a12-studio-ui\src\main\resources\de\a12\studio\ui\editors\overviewmodel\subheader-slot-panel.fxml. For C:\workspace\a12-studio\testing\workspaces\basic\models\Company_OM.json all rows are editable in the SME.

# Overview Model
- `FilterItemDialogController`'s Filter Item editor (`overview-filter-item-dialog.fxml`) now covers String (matching + viewMode), Enumeration (viewMode), Number (ranges), and Date/DateTime/Time/DateFragment/DateRange (ranges/periods) - all fixture-evidenced (see `FilterItemOptions`'s class doc). Still not modeled, because no fixture anywhere on disk (including the broader `A12 Tools - 2026.06` sample workspaces) has an example and the BA doc only shows screenshots: Boolean/Confirm criteria-based configuration, and Enumeration/Multi-select's Initial Criteria, Pinned Values and join behavior. Implement once a real example JSON is found.
- DateFragment/DateRange's Periods row set (`OverviewElementOptions.defaultPeriods`) reuses Date's subset ({date, year, yearMonth, month}) by analogy - not fixture-confirmed. Verify against a real example once found and adjust if the actual subset differs.


# Git-Support:
Hide the version control view and button when no .git folder is found above the project folder.

# Relationship-UI:


# Shortcuts:

# Rule Editor

# Testing
I've created AdvancedNewProjectModelsRoundTripTest (mirroring the basic one, via a new TestHelper.resolveTestingAdvancedNewDir() helper) and run it.
**[Stale as of 2026-09-19: the Advanced-new round-trip is now fully green — 96 tests, 0 failures, 3 skipped for disabled model types. Findings below are historical.]**
Result: 62 of 96 model files fail the round-trip (vs. the basic workspace, which is fully green). This isn't one bug — advanced_new exercises far more model-type features than basic does, and it's surfacing several distinct root causes:
- Confirmed pre-existing known gap (documented in CLAUDE.md): header.labels missing @JsonInclude(NON_EMPTY) → 28 files get a spurious "labels":[].                                                                                                                                                                                                                                
- Query Model: content.sort, content.fields, links[].fields, links[].linkDocumentFields appearing as [] when absent in source (same class of bug); content.constraint for has-type constraints dropped entirely.
- Relationship UI Model / Tree Model: columns[].linkReferences, columns[].icon, columns[].pinDirection, columns[].styles, nodes[].actions[] fields not round-tripping — looks like missing model fields, not just annotation gaps.
- Overview Model: filter configuration (newFilterConfiguration.filterGroups[].filterItems[].options/description, filterSelector.*) structurally mismatched — this overlaps with the FilterItemOptions/FilterOptionToggle work you have in progress right now (uncommitted), so may already be mid-fix.
- Form Model: buttonStyling.icon.theme, screenElements[].reference, hideCondition, dependentControls dropped.
- Selection Model: content.Data/Computation/Validation entirely missing after save — looks like a real content-mapping bug, not cosmetic.
- Document Model: documentUniquenessCriteria, Computation.errorCodesToSuppress dropped.
- Combination Model: modelReferences[].modelType becomes null for additive-document refs.

# Selection Model



# Form Models:
- check detached and embedded repeats
- check dnd behaviour
- check error handling when dropping from repeatable in regular group
- check dnd of sections with multi select
- check if trigger and dependency icons are visible
- merge tabs of settings and control, note that only fields with values shows deps
- check combobox for empty values so that these can be resetted
- for rules, initialize the name field based on the field or group 

# Performance:


# Document Model:
- check field init for new validation rules
- check white space in rule names and other name fields
- check references in error messages using the $$ notation
- Check the tree update after moving groups or creating validation rules
- Check validation rules for repeatable groups and field not filled. kcp3

# Application Model


# Additive Document Model:
There's no dedicated ADDITIVE model type — additive models referenced in CombinationStep.additiveModel are plain DocumentModels (via DocumentModelIdRef). The superTypes annotation can be placed on those to declare their relationship. So "additive document model" in the CDM context just means a regular DM used as an additive step — the heterogeneity panel already covers it since it shows on DocumentModel. No separate handling needed; what's missing is just a TODO note to verify the filter also covers those DMs if someone opens their settings.

One note on the "additive document model" concern: there's no separate model type for it — a CDM's additive steps reference plain DocumentModels via DocumentModelIdRef. The superTypes annotation can legitimately live on those DMs (declaring upward parentage), and the filter in AnnotationsPanelController already covers all DocumentModel instances. The TODO captures the one thing worth manually verifying: that when such a DM is opened standalone in its own editor tab, the raw annotations panel correctly hides the heterogeneity annotations there too — which it should, since the filter keys off model instanceof DocumentModel, not off how the model is being used in a CDM.

The Context only needs to be selected if the Additive Document Model is referenced more than once in your workspace.
- Add and remove from ADM


# Relationship Models:

- The labels which can be maintained for the Relationship Model are currently not used in the default UI for Relationships. => Hide them in the settings.
- 

# Composed Document Models
- Add separate property editor for cdm.queryRoot property


# Richtext:
@..\..\mnt\c\workspace\a12-studio\a12-studio-ui\src\main\java\de\a12\studio\ui\editors\propertyeditors\RichtextEditorController.java you know how to customize the richtext for syntax highlighting and autocompletition. I want the same autocomplete for this editor the SME is using. take your time create a plan.                                                            


# SME Gap Backlog
Source: `docs/sme-reference-comparison.md` (analysis of 2026-09-19). Each `##` entry below is a self-contained prompt: copy the paragraph(s) under it as-is. Entries are in recommended order; "Depends on" notes say what has to land first. Every prompt assumes the conventions in `CLAUDE.md` and that `docs/sme-reference-comparison.md` gets updated when a gap is closed.

## 1. Header round-trip fidelity (labels / locales / modelReferences) — DONE (verified 2026-09-19)
Already fixed by commit 332d4654 (2026-09-13): `A12Model` keeps absent-vs-explicit-`[]` via a null-backed `Header` DTO + `*Explicit` flags. Verified 2026-09-19: `AdvancedNewProjectModelsRoundTripTest` 96 tests / 0 failures / 3 skipped (was 62 failing), Basic 21/0, Commerce 92/0/3 skipped; added `A12ModelHeaderRoundTripTest`; CLAUDE.md and the comparison doc updated. Original prompt kept below for reference.

`A12Model.Header` re-serializes `labels`, `locales` and `modelReferences` as `[]` when the source file omits the key, so every save churns files of any model type whose header lacks them (e.g. `PersonSkills_NumberConversion_Se.json`). A plain `@JsonInclude(NON_EMPTY)` is not an option: other fixtures (`RelationshipModel.json`, `TreeModel.json`, `TypeDefinition.json`) rely on an explicit `[]` round-tripping. Fix it with the same absent-vs-explicit-empty-aware `JsonNode`-backed approach already used for `overviewmodel.Column.width` and `RelationshipModelContent.linkDocumentModel` (see the "Known issues" section in CLAUDE.md), keeping the existing `getLabels()`/`setLabels()` style accessors working for calling UI code. Then run `AdvancedNewProjectModelsRoundTripTest` and `BasicProjectModelsRoundTripTest`: report how many of the 28 spurious-`labels` failures (see "Testing" above) are gone and whether any new ones appeared. Update CLAUDE.md's "Still-open, same-shaped gap" paragraph and the "Known round-trip gap" note in the Selection Model section of the comparison doc.

## 2. Document Model: move/rename refactoring — DONE for one model (2026-09-19); cross-model half open, see 2b
Implemented as `DocumentModelRefactoring` (`a12-studio-models-validation`, package `refactoring`) + `RefactoringCommand` in the UI; tests `DocumentModelRefactoringTest` (19), `DocumentModelRefactoringFixturesTest` (renames/moves every element of every fixture DM and asserts each reference still resolves to the same element, 2608 operations), `RefactoringCommandTest`. Reference inventory and the covered/not-covered lists are in `docs/sme-reference-comparison.md` (Document Model gap list, "Move/rename refactoring"). Original prompt kept below for reference.

The Document Model editor only has `DeleteNodeCommand`; renaming or moving a field/group silently breaks rules (`errorEntityRelPath`, `errorCondition`), computations (`computedFieldRelPath`, `precondition`, `operation`), and references from Form/Overview/Query/other models, and the validators then only flag the damage. Read SME's reference first: `C:\workspace\sme\client\src\modules\documentModel` `moveElementApi.ts` and the backend `MoveRefactoringService` (endpoint `/move-element-with-refactoring`), and the kernel condition-language path syntax in `C:\workspace\a12\2606-06-doc`. Then design and implement a refactoring command on the existing `CommandStack` that, on move/rename of an element, rewrites relative-path references in rule/computation condition text within the same Document Model (use `ElementIndex.relativePathTo` for path computation), and updates cross-model references (Form `FieldConfigEntry.elementRef`/`FormReferences`, Overview columns, Query paths, Mapping etc. - inventory which model types hold path/id references to DM elements first and list them before coding). Make it undoable as one step. Start with a plan and a list of which reference kinds you will and won't cover in the first pass; add tests for path rewriting (including relative paths across repeatable groups and `$$` references in error messages).

## 2b. Move/rename refactoring: references held by other model files — DONE for the covered kinds (2026-09-19)
Implemented as `ProjectReferenceRefactoring` (`a12-studio-models-validation`, `refactoring` package) on top of `DocumentModelRefactoring.Plan#pathRewriter()`/`computeEdits(IncludedModelChange)`, wired into the UI's `RefactoringCommand` through `ProjectModelStore` (edits other models in place, saves + fires `ModelSaveEvent`, one undo step). Covered: Print `FieldRef.path`, Query `fields`/`sort`/top-level constraints/`filterDefinition`, Mapping `SortField`, Structural Mapping `*FullName`, Selection `PathSpecification.path`, rules/computations of Document Models that include the changed one. NOT covered (see `docs/sme-reference-comparison.md`, "Move/rename refactoring" row): Query `links[]`/constraints below `has`, Print calculation steps, Form `hostDocumentModelPath`, Include chains/Additive base paths, live redraw of open non-DM editors. Tests: `ProjectReferenceRefactoringTest` (18), `ProjectReferenceRefactoringFixturesTest` (sweep over every element of every fixture DM, renames + moves), `RefactoringCommandTest`. Note: Print `OverridableValue.path` was in the original list but is a path into the print model's *own* content (see `City_Pt.json`), not a DM path, so it is deliberately not rewritten. Original prompt kept below for reference.

Depends on: #2 (done). Read `DocumentModelRefactoring` and `RefactoringCommand` first, and the "Move/rename refactoring" row in `docs/sme-reference-comparison.md`.

#2 keeps path references *inside one Document Model* valid when an element is renamed or moved. References from Form/Overview/Tree/RelationshipUI models are by element id (`elementRef`, `fieldId`, `groupRef`, `columnRef`) and cannot break, but these path-based ones in other model files still can: Print `FieldRef.path`/`OverridableValue.path` (+ `model`), Selection `PathSpecification.path` (absolute, may end in `/` or `*`), Structural Mapping `FieldMapping`/`Slice`/`ResolutionStrategy`/`GroupToClearOnFirstFill` `*FullName` (source *and* target DM), Mapping `SortField.sortFieldFullName`, Query `fields`/`sort`/`links[].fields` and the `filterDefinition` QL text (`QueryLanguageEmitter.fieldPath`), Form `ScreenElement.hostDocumentModelPath`; plus rules/computations in *other Document Models that include the changed one* (an Include group mounts the included model's root, so a path like `Address/Street` changes when `Street` is renamed in the included model - SME handles this in `dmRuleRefactoringHelper.ts` `calculateIncludedNameChanges`/`calculateIncludedPathChanges`). Design the plumbing first: the engine already yields old-path -> element resolutions, but a rename/move in one model now has to find every project model that references it (header `modelReferences`), edit and save those files, and stay undoable as one step - decide how that interacts with those models being open (and possibly dirty) in other editors, and with the `CommandStack` being per-editor, before coding. Start with a plan and a list of which of the above you will and won't cover in the first pass; add tests using the fixtures under `testing/workspaces`.

## 3. Spike: kernel dependency vs. clean-room (decision, not implementation) — DONE (2026-09-19)
Decision: **hybrid, kernel pinned to `31.1.1`** behind one new Gradle module. Condition/computation validation, DM expansion and additive join all work in-process against the fixtures; TDG (rule contradiction, test data) is enterprise-only (401) and stays blocked. Main risk: nearly everything used is kernel `internal`/`a12internal` API with no stability guarantee, so it needs mgm's OK plus contract tests. SME's pinned 30.7.0 is unpublished and 30.8.x cannot read the studio's DM 29.4.0 (silently skips `includeConfig`). Full findings, versions, conflicts, licences and ranked unlocks: "Kernel dependency spike" in `docs/sme-reference-comparison.md`. Open decision for the owner: get mgm's answer on relying on `a12internal` classes. Original prompt kept below for reference.

Depends on: nothing. Produces a decision that gates rule contradiction, condition validation, DM expansion, additive join and test-data generation.

The comparison doc says a12-studio has no `com.mgmtp.a12.kernel:*` / `tdg` dependency and that Community artifacts (`kernel-md-facade`, `kernel-md-model`) are anonymously downloadable from `artifacts.geta12.com` under EUPL-1.2/commercial. Do a time-boxed spike, no production code: in a scratch Gradle module, try resolving `kernel-md-facade` (and whatever provides condition parsing/validation, `kernel-md-join`, and `tdg`), report transitive dependency size/conflicts with the current JavaFX/Jackson stack, license terms per artifact, and whether each of these works in-process against a12-studio's JSON models: (a) validating a rule `errorCondition`/computation `operation` string, (b) expanding a Document Model's includes, (c) TDG `checkModel` rule-contradiction, (d) additive join. Compare against SME's backend (`C:\workspace\sme\backend`: `ValidationRuleService`, `ComputationRuleService`, `ExpansionService`, `RuleContradictionCheckService`, `AdditiveModelController`). End with a recommendation (adopt kernel / stay clean-room / hybrid per capability) and a ranked list of what it would unlock. Write findings into the "Backend / kernel capability map" section of `docs/sme-reference-comparison.md`.

## 4. Form Model: finish "modeled but no UI" fields — DONE (2026-09-19)
All four parts have UI: (a) `RepeatMultiFileUploadPanelController` (Inline/Embedded repeats; auto-binds the single attachment group per the SME docs, refuses otherwise), (b) `RepeatColumnPinDirectionPanelController`, `RepeatColumnIconPanelController` and the per-column hide condition in the column editor (validators now traverse column hide conditions too), (c) a Row Action Edit dialog (`RowActionDialogController`; reuses the Button dialog's panels, `ButtonVisualSettingsPanelController` now edits any `ButtonStyling`), (d) the model-level `styles` panel in the Model Settings dialog. Tests: `FormModelEditableFieldsRoundTripTest` (wire shapes from real SME fixtures), `MultiFileUploadSupportTest`, the column hide-condition case in `FormValidatorsTest`, and JavaFX-thread tests `FormEditorFxmlLoadTest`/`RepeatEditorPanelsTest`/`RowActionDialogControllerTest`/`StylesPanelControllerTest` (skip themselves without a display). Also converted the expression column's `TextArea` to the rule editor. A second pass the same day closed everything that was left over: the style-preset picker with rename/delete refactoring (`FormStyleReferences`, `FormStyleReferenceValidator`), the default row action panel with SME's synchronisation (`DefaultRowActionSupport`, `FormDefaultRowActionValidator`), the remaining column fields (display/alignment/header styles/annotations panels) and the attachment settings (`AttachmentSettingsPanelController`, which also covers the `attachmentConfig` part of #5). A third pass closed the last two leftovers: the date picker year range (`DatePickerConfigPanelController` for Controls and columns, `DatePickerSupport`, `FormDatePickerConfigValidator`) and fractional column widths (`RepeatOverviewColumn.getWidth()` is a `Double`; `FormColumnWidthValidator`). Nothing from #4 is left open; the reflective `FormModelWalker` (find all objects of a type in a form) is available for further validators. Original prompt kept below for reference.

Depends on: nothing.

These are already in the data model (round-trip verified) but have no editor UI, so users can't configure them: (a) `MultiFileUploadOptions` on `InlineRepeat`/`EmbeddedRepeat` (download toggle, upload description/button/helper text), (b) `RepeatOverviewColumn` icon, pin direction and per-column hide condition, (c) per-action `buttonStyling` / `confirmation` / `confirmationDialogTitle` on `RowActionGroup` rows (the Row Actions table currently only edits event+scope), (d) `FormModelContent.styles` (model-level named style-class list) has no panel at all. Reference SME under `C:\workspace\sme\client\src\modules\formModel` and the "Form Model" section of `docs/sme-reference-comparison.md`. Follow the "Extract property editors" convention in CLAUDE.md (one `<name>-panel.fxml` + `<Name>PanelController` each, with `error-container.fxml`), reuse existing panels where shapes match (e.g. `ToolbarButtonsPanelController` for button styling), and use `RichtextEditorController` for any expression field. Verify wire shapes against real SME fixtures before adding fields; add round-trip tests.

## 5. Form Model: missing fields and small element gaps — DONE (2026-09-19), one item deliberately not built
Three of the five items were half-there (the data model already had `Screen.initiallyFocusedElementId` and `CustomScreenElement.height` "for round-tripping", and the `md`/`sm` model + column-count validator existed), so the work was mostly UI + validators: (a) `InitiallyFocusedElementPanelController` in the Screen tab (candidates = editable Controls of the first screen outside repeats, shared rule in `InitiallyFocusedElementSupport`; hidden on other screens unless a stale value must be cleared) + `FormInitiallyFocusedElementValidator` (only first screen; must reference a focusable Control), (b) `Control.index` (`ControlIndex`, `type` SEMANTIC/NUMERIC + `value`) + `ControlIndexPanelController`, shown only when SME's `isIndexableControl` holds (`ControlIndexSupport`, backed by the new `ElementIndex.granularity`), in the standard and the Confirm control editor, (c) `CustomScreenElementHeightPanelController` + `FormCustomScreenElementHeightValidator` (zero not allowed), (d) `md`/`sm` for the Multi-Column Section: `ResponsiveLayoutPanelController` (the Control Grid's md/sm panel) now takes a getter/setter pair and is included in the section editor. **`nameForTree` was not modeled on purpose**: SME computes it from the Document Model element name for its editor tree; it is not in the wire mapping and in no fixture (details in the comparison doc's `Control` row). Verification: wire shapes checked against real fixtures (`initiallyFocusedElementId` e-commerce/`Product_FM.json`, `height` all 16 occurrences are `CustomScreenElement`, MCS `md` `Company_FM.json`) - **`Control.index` has no fixture anywhere**, its shape comes from SME's mapping rule and editor model, so that one is unverified against a real file. Tests: `FormModelEditableFieldsRoundTripTest` (4th batch), `ElementIndexGranularityTest`, `ControlIndexSupportTest`, `InitialFocusAndCustomElementHeightValidatorsTest`, the new validators added to the fixture sweep in `FixtureWorkspacesFormValidatorsTest`, and JavaFX-thread `SmallElementGapPanelsTest` + four more editors in `FormEditorFxmlLoadTest`. Full run: 1183 tests, only the two known-red baseline tests fail. Still open near this item: SME's `mustHaveLayout` (a Multi-Column Section without `lg` raises no error here) and the backend consistency error for an indexable Control without index (belongs to #7b). Original prompt kept below for reference.

Depends on: nothing.

Add what SME has and a12-studio's data model lacks, each verified against a real SME fixture and round-trip tested: `Screen.initiallyFocusedElementId` (SME validates it's only settable on the first screen - add that validator too), `Control.index` (SEMANTIC/NUMERIC search-indexing config) and `nameForTree`, `CustomScreenElement.height`, and `md`/`sm` flex layout in `FlexLayoutPanelController` (currently only `lg`). (`FieldConfigEntry`/`attachmentConfig` - `placeholderIcon`, `accept`, `defaultAction` - was done under #4, 2026-09-19: model + `AttachmentSettingsPanelController`.) Reference: `C:\workspace\sme\client\src\modules\formModel` (`fmElements/types/*`).

## 6. Form Model: remaining validators — DONE (2026-09-19)
Depends on: nothing.

Ported from SME's `validation/customConditions/index.ts` into `a12-studio-models-validation/.../validators/form/`, all registered in `FormModelValidationService`: `DependentControlOptionsMustExistValidator` (each `Control.dependentControls.screenElement[].idref` must exist, be on the *same top-level screen* and be a Section/MultiColumnSection/ControlGrid/CustomScreenElement - SME's `isMissingOrIncorrect`, with a separate message per reason; also checks that the ids in a12-studio's Confirm-control `DependentCase.notRelevantNodes` still exist), `DependentControlsAtLeastOneOptionValidator` (an empty `dependentControls` block) and `DependentFieldAtLeastOneActionValidator` (`AtLeastOneActionPerCase` + `CaseValueIsUndefined`, via the new `DependentCase.hasAction()`). Tests: one fixture + case per validator in `FormValidatorsTest`, the previously missing `FormButtonScreenReferenceValidator` case, `DependentCaseTest`, and the three validators added to `FixtureWorkspacesFormValidatorsTest` (SME-authored models must not trip them). Decisions worth knowing: (1) the TODO/doc described dependent controls as living in `notRelevantNodes`, but the SME wire shape is `Control.dependentControls` (modeled, round-trips, **still no editor UI** - that is #7) while `notRelevantNodes` is an a12-studio-only field the Confirm Dependencies tab writes into `dependentField.case[]`; both are validated. (2) A case whose only content is `notRelevantNodes` counts as having an action, otherwise the Confirm tab's own output would be flagged; and the tab now drops a case (and the `dependentField` block it created) when its last node is unchecked (`ConfirmDependenciesPanelController.pruneIfWithoutAction`). (3) `DependentCase.value` is now `NON_NULL` instead of `NON_EMPTY`: SME treats `value: ""` as a valid action, and dropping it on save made a clean model report an error after reload. Not done: the `...Editor` variants of the SME conditions (they only exist because SME validates a form open in the editor separately from workspace validation; a12-studio has one validation path).

## 7. Form Model: dependent controls beyond Confirm, and form-vs-DM drift check - DONE (2026-09-20)
(a) The Confirm-only limit was **not** deliberate: nothing in git history, CLAUDE.md or memory says so, and the BA docs (`sme-sme-fm-ba-docs.md`, "Dependent Controls") name Boolean, Confirm and Enumeration as triggers. More importantly the Confirm tab wrote an a12-studio-only shape (`notRelevantNodes` inside the field's `dependentField`) that SME and the Form Engine do not know, so its data was silently lost outside a12-studio. The Dependencies tab is now `DependentControlsPanelController` (one titled tree per master value: "(no value)" + `false`/`true`, `true`, or the enumeration's values), shared by the Confirm editor and a new `FormNodeEditorDependentMasterControlPanelController` (standard control editor + Dependencies tab) that `FormModelTreeController` routes Boolean/Enumeration Controls to (Confirm via a type definition now gets the tab too). It writes SME's wire shape, `Control.dependentControls.screenElement[{idref, masterValue}]`; old `notRelevantNodes` are shown and moved over on the first change. The tree follows SME's `DependentControlsCandidatesCollector`: the whole top-level screen, checkbox only on allowed types (`DependentControlSupport`, port of `isPossibleDependentControlMaster`/`isAllowedDependentControlType`/`areControlAndScreenElementCompatible`), never on a container of the master, and only in a compatible data context. (b) Four validators against the Document Model, registered in `FormModelValidationService`: `FormDependencyDriftValidator` (master field of dependent field/group/enumeration, hide condition and control dependency: missing / no longer Boolean-Confirm-Enumeration / case value the field no longer has; dependent enumeration port of SME's `determineDependentEnumState` - own field no longer an enumeration, offered value and switch-to value gone; a dependent field case forcing a value or copying from a field that is gone), `FormDependentControlContextValidator` (hidden element contains the trigger, or is in a data context the trigger cannot control uniquely), `FormReferenceTypeDriftValidator` (a Control/column now pointing at a group other than attachment/multi-select, a Repeat now pointing at a field or a no longer repeatable group) and `FormControlIndexRequiredValidator` (the backend's "indexable Control without index", left over from #5). Tests: `FormValidatorsTest` (one fixture each + `DependencyDrift_DM`), `DependentControlsPanelTest` (JavaFX, 6), `FormEditorFxmlLoadTest`, and `FixtureWorkspacesFormValidatorsTest.realFormModelsHaveNoDriftAgainstTheirDocumentModels` (every SME-authored form of every fixture workspace against its Document Models; caught one real bug - attachment/multi-select groups - and one **genuinely stale fixture**: `advanced_new/.../City_Fm.json` has a dependent group case `"false"` on the Confirm field `HelperDistrict`, exempted by name in the test). Not done: deleting a screen element still leaves its id in `dependentControls` (SME's delete refactoring updates it) - the validator and the tab's stale-entry warning surface it; hide-condition value drift for Boolean stays with `HideConditionSupportedValuesValidator` (which allows only `true` for Boolean, unlike dependent controls' `false`/`true`). Original prompt kept below for reference.

Depends on: #6 (validators) is helpful but not required.

(a) `DependentCase.notRelevantNodes` (dependent controls) is only implemented for Confirm-type Controls via `ConfirmDependenciesPanelController`, but SME's `isPossibleDependentControlMaster` also allows Boolean and Enumeration masters. Decide whether the limitation is deliberate (check git history/CLAUDE.md/memory) and, if not, generalize the Dependencies tab to Boolean/Enumeration masters. (b) Add a structural consistency check between a Form Model and its (possibly since-changed) Document Model - SME produces a categorized `Problem[]` (INFO/WARNING/ERROR) - e.g. field type changed so a configured `dependentEnumeration`/hide-condition case value no longer exists, `elementRef` now points at a different element type. Surface it via the existing validation service, not a new architecture. Start with a plan listing which drift cases you'll detect.

## 8. Form Model: Includes copy-and-rewrite action
Depends on: nothing, but needs a design pass first.

Only the provenance fields (`includeId`/`formModelRef`/`hostDocumentModelPath`) exist on `ScreenElement` for round-trip fidelity. SME expands an include at author time: it copies the referenced Form Model's subtree into the host model's own `screens` with rewritten ids, keeping the three fields as provenance. Study the real fixture `C:\workspace\sme\client\resources\input\models\fmm\workspace\HostModel.json` and SME's include-insertion code, then write a short design (which node types can be included, id-rewrite rules, how `hostDocumentModelPath` is derived, how field/group config entries from the source are merged, conflict handling, how it's exposed in the tree context menu with localized `StudioBundle` labels) and get my sign-off before implementing.

## 9. Query Model: semantic filter validation
Depends on: nothing.

`filterDefinition` (root and per `QueryLink`) is checked for syntax only via `QueryLanguageEmitter`; field references inside the expression (`[/Path/To/Field]`) and relationship/role references are not resolved. Add a semantic check that resolves every bracketed path against the target (or link-target) Document Model and reports unknown fields with the field name in the message. Reuse `QueryElementResolution` (linear-scan "/"-path lookup) and `BracketedPathSuggestionProvider`'s path knowledge; add a new validator next to `QueryFilterDefinitionSyntaxValidator` and wire it into the `RichtextEditorController` per-keystroke validator hook used by `QueryDocumentNodePanelController`. Reference: SME `moduleSupport/qmm` `binder.ts`/`checker.ts`. Note: the emitter deliberately does no type checking; keep this to existence checks unless type checking is cheap. Add tests.

## 10. Query Model: reference/rename tracking
Depends on: #2 (Document Model move/rename refactoring) - reuse its mechanism rather than inventing a second one.

`QueryModelTreeController.resolveTargetDocumentModel()` silently yields an empty tree when the stored DM id no longer resolves, and renaming a DM/field never updates or flags queries. Hook Query Model references (target DM, relationship/role in links and sort, `fields[]`, sort field paths) into the refactoring mechanism from #2, and make an unresolvable target an explicit validation error surfaced in the UI rather than an empty tree. Reference: SME `qmModule.ts` `refactorDocument()`.

## 11. Query Model: aggregation (gated)
Depends on: #3 (kernel spike) - confirm the target runtime supports an aggregation-mode result first; otherwise record it as a documented non-goal in the comparison doc and stop.

`QueryModelContent.aggregateResults` is a dangling boolean with no config. SME's `aggregation` is `group: {field}[]` plus `aggregations: {function: count|sum|max|min|avg, field}[]`. If confirmed worthwhile: model it in `QueryModelContent`, add a panel (follow the Sort panel pattern), a validator (fields resolve, function/type compatibility), and round-trip fixtures from SME's `client/resources/input/models` examples.

## 12. Combined Document Model: loop detection
Depends on: nothing for the reference-graph part; full expansion depends on #3.

The doc says base/step loop detection (SME's `CombModelReferenceHelper`) isn't ported. First check whether loop detection needs DM expansion or only the reference graph (base model + step model references, following Additive/Selection/Decoration models' own references). If it works on references alone, implement it as a validator in `de.a12.studio.modelsvalidation.validators.combination` with tests including a self-reference and a two-model cycle. If it truly needs expansion, say so and stop.

## 13. Document Model: smaller SME features (verify first)
Depends on: nothing.

Several are marked "Not confirmed present" in the comparison doc, so first verify each in the code and report what actually exists, then propose which to build: tree filtering (by type, category, annotated-only - SME `dmEditorView` filters), multi-select bulk actions (bulk delete/cut/copy), "Insert from another Document Model" (SME resolves includes and copies/imports type defs), and ad hoc testing (SME generates a reduced test Document+Validation model for selected elements; needs #3 to be meaningful). Reference `C:\workspace\sme\client\src\modules\commonDocumentModel\api\editor\*` and `documentModel`. Don't start implementing before I pick from your list.

## 14. Refresh the comparison doc
Depends on: nothing; best done after a few of the above land.

`docs/sme-reference-comparison.md` was last fully analyzed 2026-07-17; the priority table and "Other model types" survey still list model types (structuralMapping, mapping, combination, additive, relationship, print family, etc.) whose editors may since have been built, and the Backend/kernel capability map has several "Not yet confirmed" rows. Re-verify against the code in `a12-studio-ui/.../editors`, `a12-studio-models`, `a12-studio-data-services` and against `C:\workspace\sme`, and update the stale sections, marking every changed statement with the date. Don't touch sections already dated after 2026-09-05 unless something contradicts the code.

## Won't do (decided 2026-09-19)
- AI-assisted Document Model generation (SME `documentModel/ai/*`).
- Model diff/compare editor.

## Parked (do not start unless I ask)
- Markdown report per element (`createMarkdownReport`).
- Form Model `Binding`/`BindingRepeat` - blocked on Relationship Model + Composed Document Model support.
- Query Model multi-target types (CDM/Transformer as target) and SME's structured-AST filter editor.
- Real Form Engine preview (current preview is a wireframe; see the project memory on real Form Model rendering).
