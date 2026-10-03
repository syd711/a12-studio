# TODO

Regenerated 2026-10-02: everything fixed so far was removed - what was done is in `git log` and, per feature, in
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

1. **[OWNER] Which model types to finish next.** Only Print is `enabled: false` in `model-versions.json`. Mapping has target, sources and the Structural Mapping Model link but no precomputation fragment (the Structural Mapping editor itself was built 2026-10-03). Model Graph Diagram, Link/Document do not exist (the Transformer Model editor was built 2026-10-03, see below). Comparison doc's ranking: (structural mapping done) -> mapping -> additive overlay editing -> print. Decide the order.

## Open issues

- **[CLOUD] Kernel facade (`a12-studio-kernel`, decided 2026-10-02, details in "Kernel dependency spike" in `docs/sme-reference-comparison.md`).** The facade, DM/Combination expansion, additive join, rule and computation validation (also while typing in the rule, computation options and computation alternative editors, `ComputationKernelCheck`), the project adapter, the in-service condition validation (`KernelConditionValidator`, cached per model on a content fingerprint; reports kernel findings of rules and computations next to the grammar check) and the kernel-backed Combination Model preview are done. Still open:
  - Combination parity is complete (2026-10-02): several steps, an additive model that redefines a reference field and `DecorationForGroups` are pinned in `KernelCombinationParityTest`.
  - `shadowJar` (kernel inside): the fat jar's kernel check and expansion were run headless from the jar alone (2026-10-02, `Company_DM`: 0 findings, expansion ok); a real app start with a JavaFX window was not tried **[LOCAL]**.
  - **[OWNER]** Data-services' `DocumentModelFieldResolver` still uses the approximate addition-only merge. Technically it could call `ProjectKernelModels` (no Gradle cycle: `a12-studio-models-validation` does not depend on data-services), but `a12-studio-server` depends on data-services, so the kernel (3280 jar entries) would ship in the server too. Decide whether that is wanted.
  - Optional: inform mgm at `a12-license@mgm-tp.com` (not blocking).
- **[LOCAL] `$path$` notation in error messages:** only a visual check of the UI is left (SME has no `$path$` handling of its own; rename/move rewriting is unit-tested).
- **Manual UI checks (no known defect, not yet verified) - [LOCAL]** (same list applied to the Form and Document Model editors):
  - Drag and drop in general; error handling when dropping from a repeatable group into a regular group; dnd of sections with multi-select.
  - D/T dependency marks on the Form Model tree (`FormDependencyBadges`) and on the Document Model source tree of the Form editor (`DocumentSourceTreeController#refreshDependencyMarks`, no unit test).
  - Merge the Settings and Control tabs of the field editor; dependencies are only shown for fields that have values.
  - Every combo box should offer an empty value so a selection can be reset.

## Open todos

### Form Model
- **[LOCAL]** `PreprocessingSettingsPanelController` not yet looked at on a real display.
- **Interactive Commit/Edit/Delete refactoring dialog:** the delete confirmation already lists the references it cleans up. Open: SME's per-reference choice (keep/edit instead of delete) and references from *other* models (e.g. Application Model entries pointing at a deleted Screen/Control) - a12-studio only reports those afterwards as validation errors. Cross-cutting with the Application Model, needs a design decision.

### Structural Mapping Model
- **[LOCAL]** Not looked at on a real display: drag and drop of a source field onto a target row, the tag columns' layout and context menu, the resolution-strategy and move-field-mapping dialogs, the Clear column. Needs a project whose Mapping Model has a source, a target and this model (kernel-backed, so the trees stay empty without one).

### Transformer Model
Built 2026-10-03 (four tabs + the Model Settings dialog, validators, runs the installed SME backend out of process; details and deviations in "Transformer Model: built" in `docs/sme-reference-comparison.md`). Open:
- **[CLOUD] Offer the generated Document Model to the other editors** (the main gap). SME lets a Transformer Model be chosen wherever a Document Model is (Form, Overview, Tree, Content, Query, Mapping, Combination base model, ...); `ProjectDocumentModels` only knows `DocumentModel`s and Combination stand-ins, and the Document Model exists only after a transformation (`TransformerRun`, needs the installed SME). Needs the last generated model cached or regenerated on demand behind that class, then the pickers and the "not found" validators of those editors (see the Tree/Query/Relationship gap reviews) follow. Decide first whether an editor may depend on the installed SME just to list fields **[OWNER]**.
- **[LOCAL]** Only rendered offscreen and driven through the FXML, not looked at in a running app: the "Add XSD files" file chooser, typing in the suggestion combo boxes while a run is in progress, the issue list with many messages.
- Not done: kernel validation of the generated Document Model (SME's `isDocumentModelValid`), an element editor in the Preview tab, `/api/transformer/document/from-source` and `/to-source` of the installed backend (XML <-> A12 document), SME's New Model modal asking for the XSD up front.

### Composed Document Models
- **[LOCAL]** Still unverified: a real SME Form Model bound to a CDM with a heterogeneous relationship (SME's own spec for `DescendantOfHeterogeneous*` / `InitialValueAndDescendantOfHeterogeneousToManyRelationship` is mocked).

### Combined Document Model
- **[LOCAL]** Look at the kernel-backed Preview tab in a running app.

### Query Model
- **[LOCAL]** Filter completion (`QueryFilterCompletion`/`QueryFilterSuggestionProvider`) and hover docs (`QueryFilterHover`/`QueryFilterHoverProvider`, popup in `RuleEditorController`) are not looked at on a display. Not ported from SME's qmm editor: inlay hints, call-stack breadcrumb.
- **[LOCAL] Wire shape of Query `aggregation` and of a `Has(...)` call inside `filterDefinition`** never checked against a real SME file (see Blocked). Re-swept 2026-10-02: neither the repo fixtures nor the A12 2026.06 sample workspaces contain one.
- Note: the Model Tree tab's root DM is picked in the Settings tab, not through an ER-diagram picker like SME (by design).

### Form Engine preview (see "Form Engine preview" in `docs/sme-reference-comparison.md`)
- **[LOCAL] Ad Hoc Testing of an Additive Document Model:** backend round trip verified; still open is looking at the result in the actual preview window on a display.
- **[OWNER] + [LOCAL] Theme and Data menus of the preview are empty:** offer the project's `.theme` files (`request-theme` -> `send-theme`) and sample documents (`request-document` -> `send-document`). No code reads `.theme` or `data/documents/*.json` today, no `.theme` fixture exists to verify the wire format, and whether edits made in the preview (`create-document`/`update-document`) may be saved is an open product decision.
- **[OWNER]** Without a configured A12 installation the Form Model button falls back to the old wireframe and Ad Hoc Testing shows an error page - decide whether the fallback should say so in the studio.

## Blocked (waiting for an input)

- **Overview filter items:** Boolean/Confirm criteria-based configuration and Enumeration/Multi-select Initial Criteria, Pinned Values and join behaviour are not modeled: no fixture (including the `A12 Tools - 2026.06` sample workspaces) has an example, the BA doc only has screenshots, and SME's `overviewModel` TS types have no such concept. Implement once a real example JSON turns up.
- **Overview DateFragment/DateRange periods:** `OverviewElementOptions.defaultPeriods` reuses Date's subset ({date, year, yearMonth, month}) by analogy. Verify against a real example.
- **Wire shapes never checked against a real SME file** (shapes come from SME's meta model and the Data Services docs): `Control.index`, Query `aggregation` (`alias`), a `Has(...)` call inside a Query `filterDefinition`, and the Query filter reference validator (no real `filterDefinition` with references to sweep for false positives). Verify when a sample appears.

## Won't do (decided 2026-09-19)

- Relationship Model `EntityCharacteristic.navigable`/`candidateConstraints` and `Multiplicity.lowerLimit` (decided
  2026-10-01): Relationship Model 4.0.0, the version a12-studio writes, removed them (Data Services docs,
  "Relationship Model Version 4.0.0 - Unused Properties Removed"), so dropping them on load is correct.
- Document Model Number `minFractionalDigits`/`maxFractionalDigits` required-ness (decided 2026-10-02): SME's `MIN_FRACT_DIGITS_MISSING`/`MAX_FRACT_DIGITS_MISSING` are not ported. The fields stay optional behind the "has decimal places" checkbox (~43% of real Number fields in the fixtures have none). Do not add these rules unless the owner reverses this.
- SME's diagram-driven CDM authoring (decided 2026-09-26).
- AI-assisted Document Model generation (SME `documentModel/ai/*`).
- Model diff/compare editor.
- Model migration (decided 2026-09-26): a12-studio does not migrate models written for older versions of a model type or of the Content Engine (SME's `DefaultMigrator` / `namespaceVersions` steps). Older files are not normalized on load, no migration action is offered, and `Studio.checkModelVersions` stays header-based. A Content Model with an older `namespaceVersions` entry only gets the validator's warning. Do not add migration code unless the owner reverses this.
- Content Model migration (gap 4 of its gap review).
