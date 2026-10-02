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

1. **[CLOUD] Kernel dependency: decided 2026-10-02, build the facade module.** Owner decision: a12-studio relies on the kernel (`31.1.1`, including `internal`/`a12internal` classes) and puts a facade in front of every kernel access; if a future A12 major release breaks something, it is fixed then, in the facade only. No answer from mgm is awaited (informing them at `a12-license@mgm-tp.com` is optional, not blocking). Done 2026-10-02: module `a12-studio-kernel` (`KernelDocumentModelChecker` over the public `checkConsistency`, `KernelFinding`; 4 contract tests green: consistent model, corrupted condition -> `MVK_UNEXPECTED_TOKEN`, unexpanded includes refused, version pin). Also done 2026-10-02: DM and Combination Model expansion (`KernelDocumentModelExpander`, also covers `advanced_new`'s three `*_Cm` models; 8 contract tests green), the project adapter `ProjectKernelModels` in `a12-studio-models-validation` (feeds the kernel from the in-memory `ProjectItem` tree, so unsaved edits count; empty result = caller falls back), and `shadowJar` builds with the kernel inside (3280 kernel entries, 14 merged service files; a real app start with a JavaFX window was not tried). Remaining: further facade slices (additive join with a diff against SME's output, per-rule validation with line/column, migration), contract tests over `e-commerce`. Then wire the consumers it unblocks: semantic condition validation, real DM expansion / additive join, `BindingRepeat`'s deeper heterogeneous-relationship/multiplicity checks, the kernel-backed Combination Model preview. The registry is now `gitlab.geta12.com` (`settings.gradle`, repository `mgm-gitlabel`, anonymous today, optional `a12User`/`a12Token`); a cloud session needs egress to it. Details: "Kernel dependency spike" in `docs/sme-reference-comparison.md`.
2. **[OWNER] Which model types to finish next.** Only Print is `enabled: false` in `model-versions.json`. Mapping has target + sources only and Structural Mapping is a stub, yet both are enabled. Transformer, Model Graph Diagram, Link/Document do not exist. Comparison doc's ranking: structural mapping -> mapping -> additive overlay editing -> print. Decide the order, and whether the two near-empty enabled editors should be switched off until usable.

## Open issues

- **[LOCAL] `$path$` notation in error messages:** only a visual check of the UI is left (SME has no `$path$` handling of its own; rename/move rewriting is unit-tested).
- **Manual UI checks (no known defect, not yet verified) - [LOCAL]** (same list applied to the Form and Document Model editors):
  - Drag and drop in general; error handling when dropping from a repeatable group into a regular group; dnd of sections with multi-select.
  - D/T dependency marks on the Form Model tree (`FormDependencyBadges`) and on the Document Model source tree of the Form editor (`DocumentSourceTreeController#refreshDependencyMarks`, no unit test): not looked at on a display.
  - Merge the Settings and Control tabs of the field editor; dependencies are only shown for fields that have values.
  - Every combo box should offer an empty value so a selection can be reset.
  - When a rule is created, pre-fill its name from the field or group it targets.

## Open todos

### Form Model
Gap review: "Form Model: gap review (2026-09-27)" in `docs/sme-reference-comparison.md`. All gaps done; `PreprocessingSettingsPanelController` not yet looked at on a real display.
- **Interactive Commit/Edit/Delete refactoring dialog:** the delete confirmation already lists the references it cleans up. Open: SME's per-reference choice (keep/edit instead of delete) and references from *other* models (e.g. Application Model entries pointing at a deleted Screen/Control) - a12-studio only reports those afterwards as validation errors. Cross-cutting with the Application Model, needs a design decision.

### Document Model
Gap review: "Document Model: gap review (2026-09-27)" in `docs/sme-reference-comparison.md`. All gaps done or deliberately not ported.
- **[OWNER] Number `minFractionalDigits`/`maxFractionalDigits` required-ness** (SME's `MIN_FRACT_DIGITS_MISSING`/`MAX_FRACT_DIGITS_MISSING`): deliberately not ported - a12-studio makes them optional behind a "has decimal places" checkbox, and ~43% of real Number fields in the fixtures have none. Only port after deciding to make them mandatory.


### Type Definition Model
Nothing open.

### Relationship Models
Nothing open.

### Composed Document Models
- **[CLOUD] Unblocked (2026-10-02, kernel decision):** `BindingRepeat`'s deeper heterogeneous-relationship/repetition-vs-multiplicity checks (`DescendantOfHeterogeneous(ToMany)Relationship`, `InvalidBindingRepeatRepetitionAndMultiplicity`) need kernel-backed DM expansion, which now exists (`ProjectKernelModels.expand`). Not yet done: the rule semantics themselves (which expanded-model shapes trigger the two errors) still have to be read from SME's `I_BindingRepeat`-related checks and the installed A12 packages; the structural precondition is `FormBindingRepeatCdmRequiredValidator`.
- Decided 2026-09-26: SME's diagram-driven authoring is intentionally not ported.

### Combined Document Model
- **Kernel-backed preview: done 2026-10-02.** The Preview tab expands through the kernel (`ProjectKernelModels.expand`, Selection/Decoration steps included) and falls back to the addition-only merge (`CombinedDocumentModelElements.resolveForFieldReferences`) when the kernel cannot expand the model. Open: look at it in a running app (not done here), and field-reference pickers still use the approximate merge.

### Query Model
- Filter completion (`QueryFilterCompletion`/`QueryFilterSuggestionProvider`) is not looked at on a display. Hover docs (`QueryFilterHover`/`QueryFilterHoverProvider`, popup in `RuleEditorController`) are ported but, like completion, not looked at on a display. Not ported from SME's qmm editor: inlay hints, call-stack breadcrumb.
- **[LOCAL] Wire shape of Query `aggregation` and of a `Has(...)` call inside `filterDefinition`** never checked against a real SME file (see Blocked). Re-swept 2026-10-02: neither the repo fixtures nor the A12 2026.06 sample workspaces contain one; still unverifiable.
- Note: the Model Tree tab's root DM is picked in the Settings tab, not through an ER-diagram picker like SME (by design).

### Form Engine preview (see "Form Engine preview" in `docs/sme-reference-comparison.md`)
- **[LOCAL] Ad Hoc Testing of an Additive Document Model:** backend round trip verified; still open is looking at the result in the actual preview window on a display.
- **[OWNER] + [LOCAL] Theme and Data menus of the preview are empty:** offer the project's `.theme` files (`request-theme` -> `send-theme`) and sample documents (`request-document` -> `send-document`). No code reads `.theme` or `data/documents/*.json` today, no `.theme` fixture exists to verify the wire format, and whether edits made in the preview (`create-document`/`update-document`) may be saved is an open product decision.
- **[OWNER]** Without a configured A12 installation the Form Model button falls back to the old wireframe and Ad Hoc Testing shows an error page - decide whether the fallback should say so in the studio.

### Overview Model
Gap review: "Overview Model: gap review" in `docs/sme-reference-comparison.md`. All gaps closed (gap 17 closed 2026-10-02: SME's `overviewRefactoring.ts` only handles cross-model rename, which a12-studio already covers). Remaining items are under Blocked.

### Content Model
Gap review: "Content Model: gap review" in `docs/sme-reference-comparison.md`. Gap 4 (migration) is Won't Do. Only gap 9 is partly open.
- **[LOCAL] Gap 9, `ColorRow`/`ShadowRow` setting validation** (`color-panel.fxml`, `shadow-panel.fxml`): needs CSS color/shadow syntax read out of the installed client bundle to avoid false positives. The installed bundle has no color/shadow converter to read, and real files use `#333333`, `rgb(107, 107, 134)` and `rgb(0, 0, 0, 1)`, so a strict grammar would give false positives. Needs the content-engine editor package source, or leave unvalidated.

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
