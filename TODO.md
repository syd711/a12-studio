# TODO

Rewritten 2026-09-30: everything fixed so far was removed - what was done is in `git log` and, per feature, in
`docs/sme-reference-comparison.md`. Everything below is open. Conventions are in `CLAUDE.md`.

## Tags

Each item carries one tag so you can pick what to continue in a cloud session (which has only this repo and
`docs/sme-reference-comparison.md`, not `C:\workspace\sme`, `C:\workspace\a12\2606-06-doc`, `C:\workspace\RichTextFX`
or an A12 installation):

- **[SME]** - needs a fresh read of the SME source (`C:\workspace\sme`) and/or the BA doc to pin down exact wire
  values, labels or rules. **Do locally.**
- **[LOCAL]** - needs the A12 installation (`SmeInstallation`/installed client bundle) or a real display. **Do locally.**
- **[OWNER]** - needs a decision from the owner first.
- **[CLOUD]** - can be done with this repo alone.

Cloud build note: the cloud egress policy may block `services.gradle.org`, `plugins.gradle.org` and Maven Central
(seen 2026-10-01) - then nothing builds and only fixture/doc items are doable; Java changes wait for a session with
a working build.

Cloud test note: the JavaFX UI tests run headed under Xvfb (`Xvfb :99 -screen 0 1280x1024x24 &`, `export DISPLAY=:99`,
then `./gradlew ... test`). Without `DISPLAY` every FX-toolkit test is silently skipped, so a green run proves nothing.

## Open decisions

1. **[OWNER] Kernel dependency: may a12-studio rely on `internal`/`a12internal` kernel classes?** The 2026-09-19 spike found kernel `31.1.1` viable in-process (condition validation, DM expansion, additive join; TDG is enterprise-only and stays blocked), but nearly everything it uses has no stability guarantee. Needs mgm's answer, plus contract tests if yes. Blocks: semantic condition validation, real DM expansion / additive join, `BindingRepeat`'s deeper heterogeneous-relationship/multiplicity checks, the kernel-backed Combination Model preview. Details: "Kernel dependency spike" in `docs/sme-reference-comparison.md`.
2. **[OWNER] Which model types to finish next.** Only Print is `enabled: false` in `model-versions.json`. Mapping has target + sources only and Structural Mapping is a stub, yet both are enabled. Transformer, Model Graph Diagram, Link/Document do not exist. Comparison doc's ranking: structural mapping -> mapping -> additive overlay editing -> print. Decide the order, and whether the two near-empty enabled editors should be switched off until usable.

## Open issues

- **[LOCAL] Regression: `testing/workspaces/basic/models/Company_OM.json`** was edited on disk after the last fix and the invalid `enumeratedStringFilter` block (no `fields`, `enableFilter: true`) is back, so `FixtureWorkspacesOverviewValidatorsTest` is red. The validator is right (SME's `fieldIdsMustBeFilled`). Owner of the fixture decides: add fields to the "Filter String Fields with Multi-Select" list, or drop the block.
- **[LOCAL] Four JavaFX tests fail under Xvfb even on a clean checkout:** `StudioTabPaneTest`, `TabPaneControllerTest`, `ContentModelEditorPanelsTest`, `TypeDefinitionTableControllerTest` (layout/timing-sensitive). Re-verify on a real display before trusting any "fixed" claim touching these classes.
- **[LOCAL] `$path$` notation in error messages:** checked 2026-10-01 - SME's own source has no `$path$` handling (the only `$path` hits are Kotlin string templates); the `$...$` parameters are kernel-side. Rename/move rewriting is unit-tested. What is left is only a visual check of the UI.
- **Manual UI checks (no known defect, not yet verified) - [LOCAL]** (same list applied to the Form and Document Model editors):
  - Drag and drop in general; error handling when dropping from a repeatable group into a regular group; dnd of sections with multi-select.
  - Trigger and dependency icons on tree rows: SME's T/D flags are not ported - check what is shown and add them. (**[SME]** for the exact flag semantics.)
  - Merge the Settings and Control tabs of the field editor; dependencies are only shown for fields that have values.
  - Every combo box should offer an empty value so a selection can be reset.
  - When a rule is created, pre-fill its name from the field or group it targets.

## Open todos

### Form Model
Gap review: "Form Model: gap review (2026-09-27)" in `docs/sme-reference-comparison.md`. Gaps 1-9 done (gap 8: `PreprocessingSettingsPanelController`, 2026-10-01; compiled and tests green, but not looked at on a real display).
- **[SME] Interactive Commit/Edit/Delete refactoring dialog** SME shows when deleting a Screen/Control that is referenced elsewhere. a12-studio only reports the dangling reference afterwards as a validation error. Cross-cutting (same gap for the Application Model) - fix together, not as a Form-Model-only patch.

### Document Model
Gap review: "Document Model: gap review (2026-09-27)" in `docs/sme-reference-comparison.md`. Gaps 1-6 and 9-11 done (gap 10's `OPTIONAL_DATE_*` rules do not port - a12-studio has no `optionalDateType`; `INTERPRETATION_OF_YEAR_INVALID` done 2026-10-01 with `ModelInfo.baseYear`; `_MISSING` not ported, its legacy `DD.MM-DD.MM` format name can't occur).
- **[OWNER] Number `minFractionalDigits`/`maxFractionalDigits` required-ness** (SME's `MIN_FRACT_DIGITS_MISSING`/`MAX_FRACT_DIGITS_MISSING`): deliberately not ported - a12-studio makes them optional behind a "has decimal places" checkbox, and ~43% of real Number fields in the fixtures have none. Only port after deciding to make them mandatory.
- **[LOCAL] New fixture failure:** `FixtureWorkspacesDocumentValidatorsTest` reports `e-commerce/ProductMovie_DM.json` (`IncludeTypeDefinitionModeValidator`: included `Product_Common_DM` has a different Type Definition mode). Not caused by recent validator work; owner of the fixture decides.

### Type Definition Model
Nothing open.

### Relationship Models
Nothing open.

### Composed Document Models
- **[OWNER] Blocked on Open Decision #1:** `BindingRepeat`'s deeper heterogeneous-relationship/repetition-vs-multiplicity checks (`DescendantOfHeterogeneous(ToMany)Relationship`, `InvalidBindingRepeatRepetitionAndMultiplicity`) need kernel-backed DM expansion.
- Decided 2026-09-26: SME's diagram-driven authoring is intentionally not ported.

### Combined Document Model
- **[OWNER] Kernel-backed preview:** the new Preview tab shows only the addition-only merge (`CombinedDocumentModelElements.resolveForFieldReferences`). Selection/Decoration steps and real semantic join are not reflected - needs Open Decision #1.

### Query Model
- **Filter expression type/enum-value checking: done 2026-10-01** (`QueryFilterTypeChecker`, golden-tested against SME's own snapshots - see "Query Model" in `docs/sme-reference-comparison.md`). Open: nothing in the checker; the editor helpers of SME's qmm package (completion, inlay hints, hover docs) are not ported.
- **[LOCAL] Wire shape of Query `aggregation` and of a `Has(...)` call inside `filterDefinition`** never checked against a real SME file (see Blocked).
- Note: the Model Tree tab's root DM is picked in the Settings tab, not through an ER-diagram picker like SME (by design).

### Form Engine preview (see "Form Engine preview" in `docs/sme-reference-comparison.md`)
- **[LOCAL] Verify Ad Hoc Testing of an Additive Document Model end to end** against a real A12 installation. Built 2026-09-30; only the id-mapping logic is unit-tested (`AdHocTestPreviewSessionAdditiveIdMappingTest`) - the `expandCombination`/`generateAdHocTestInput` round trip with the real SME backend has not been run.
- **[OWNER] + [LOCAL] Theme and Data menus of the preview are empty:** offer the project's `.theme` files (`request-theme` -> `send-theme`) and sample documents (`request-document` -> `send-document`). No code reads `.theme` or `data/documents/*.json` today, no `.theme` fixture exists to verify the wire format, and whether edits made in the preview (`create-document`/`update-document`) may be saved is an open product decision.
- **[LOCAL] `FormScreenGenerator` vs. SME's `form-model-generator`** (npm `@com.mgmtp.a12.formengine/form-model-generator`): compare on a larger model and align labels/grouping.
- **[OWNER]** Without a configured A12 installation the Form Model button falls back to the old wireframe and Ad Hoc Testing shows an error page - decide whether the fallback should say so in the studio.

### Overview Model
Gap review: "Overview Model: gap review" in `docs/sme-reference-comparison.md`. Gaps 1-16 done, 17 partial.
- **[SME] Gap 17 remainder:** refactoring-dialog behaviors not done - deleting a filter field, and event/model reference cascades. `overviewRefactoring.ts` only handles cross-model rename, so there is no known recipe; do not guess semantics.
- **[SME] `bindingConfiguration` wire-shape gap:** real but currently causes no validator misbehavior - see the doc.

### Content Model
Gap review: "Content Model: gap review" in `docs/sme-reference-comparison.md`. Gap 4 (migration) is Won't Do. Only gap 9 is partly open.
- **[LOCAL] Gap 9, `ColorRow`/`ShadowRow` setting validation** (`color-panel.fxml`, `shadow-panel.fxml`): needs CSS color/shadow syntax read out of the installed client bundle to avoid false positives.

### Application Model
Nothing open.

### Master Detail Model
All five gaps from the 2026-09-27 review are done. Nothing open.

## Blocked (waiting for an input)

- **Overview filter items:** Boolean/Confirm criteria-based configuration and Enumeration/Multi-select Initial Criteria, Pinned Values and join behaviour are not modeled: no fixture (including the `A12 Tools - 2026.06` sample workspaces) has an example, the BA doc only has screenshots, and SME's `overviewModel` TS types have no such concept. Implement once a real example JSON turns up.
- **Overview DateFragment/DateRange periods:** `OverviewElementOptions.defaultPeriods` reuses Date's subset ({date, year, yearMonth, month}) by analogy. Verify against a real example.
- **Wire shapes never checked against a real SME file** (shapes come from SME's meta model and the Data Services docs): `Control.index`, Query `aggregation` (`alias`), a `Has(...)` call inside a Query `filterDefinition`, and the Query filter reference validator (no real `filterDefinition` with references to sweep for false positives). Verify when a sample appears.

## Parked (do not start unless asked)

- Nothing parked at the moment.

## Won't do (decided 2026-09-19)

- Relationship Model `EntityCharacteristic.navigable`/`candidateConstraints` and `Multiplicity.lowerLimit` (decided
  2026-10-01): Relationship Model 4.0.0, the version a12-studio writes, removed them (Data Services docs,
  "Relationship Model Version 4.0.0 - Unused Properties Removed"), so dropping them on load is correct.

- AI-assisted Document Model generation (SME `documentModel/ai/*`).
- Model diff/compare editor.
- Model migration (decided 2026-09-26): a12-studio does not migrate models written for older versions of a model type or of the Content Engine (SME's `DefaultMigrator` / `namespaceVersions` steps). Older files are not normalized on load, no migration action is offered, and `Studio.checkModelVersions` stays header-based. A Content Model with an older `namespaceVersions` entry only gets the validator's warning. Do not add migration code unless the owner reverses this.
