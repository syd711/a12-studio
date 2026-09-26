---
name: tree-node-configuration
description: Tree Model Node Types tab - radio row selection + per-node configuration panels below the list (built 2026-09-26); design, SME source, traps
metadata:
  type: project
---

**Built 2026-09-26.** `TreeNodeTypesPanelController` rows start with a `RadioButton` (one `ToggleGroup`; click anywhere on the row selects, pencil / double click opens the old `TreeNodeDialogController`). `setOnSelectionChange` feeds `TreeNodeConfigurationPanelController` (a plain VBox container, not an `AbstractPropertyEditor`) which binds the sub panels to the selected `TreeNode` via `setNode`: `TreeNodeInheritPanelController`, `IconPanelController`, `TreeChildRelationshipsPanelController`, `TreeNodeActionsPanelController`, `TreeNodeContextMenuPanelController`, `TreeNodeRowActivationPanelController`, Row Title (`LocalizedTextPanelController.configureCustom/setCustom`), Styles (`StylesPanelController.setCustom`). Dialogs live in `treemodel/dialogs/` (`TreeChildRelationshipDialogController`, `TreeNodeActionDialogController`, `TreeNodeContextMenuGroupDialogController`, shared `ColumnMappingEditor`).

**SME source:** the node detail screen is in `C:\workspace\sme\client\resources\models\treeModel\TreeModelEditor.json` (487 KB - don't Read it whole; walk it with a Python script) plus `TreeMetaModel.json`; the action form is `treeModel/button/TM_NodeAction*.json`; inheritance rules are `client/src/modules/treeModel/middlewares/handleInheritedNodes.ts` (ported as `models/.../TreeNodeInheritance`). Events list: `treeModel/references/tmEvents.ts`.

**Design decisions:** `TreeNodeAction` implements `OverviewButtonLike` (overviewmodel `Icon`/`Confirmation`) so the overview's Priority/Icon/Annotations/Localized panels are reused inside the action dialog. Dialogs edit a JSON clone and the caller splices it in only on OK. New *row* actions get `primary=false, destructive=false` (SME requires a priority); context-menu actions get none (SME fixtures have none). A part that is inherited is cleared (after a confirm if it had content) and its panel hidden. `contextMenu` is dropped when its last group goes; `crc.columns` stays `null` (absent) unless something is mapped (fixtures mix absent and `[]`).

**Traps:**
- `Studio.currentProject` is static shared test state; other test classes leave `new Project()` behind whose root has a null file, so `ProjectDocumentModels.getOtherDocumentModels` throws `this.file is null`. New tests restore the previous project in `@AfterAll`; the container guards `projectItem == null`.
- `AbstractPropertyEditor.setSettingsKeySuffix` after `initialize()` has no effect on the persisted expanded state (key is computed in `initialize`), so the Actions panel inline and in the group dialog share one expanded flag.
- Header/row alignment: two `HBox.hgrow=ALWAYS` labels only line up with their header when both have the same `prefWidth` (`growEqually`).
- 7 tests fail on a clean HEAD, unrelated to trees: `ContentModelEditorPanelsTest` (5), `TypesettingModelEditorTest` (1), `FixtureWorkspacesFormValidatorsTest` (City_Fm drift) - checked on a `git archive HEAD` export 2026-09-26. [[test-harness-notes]] claims all-green; that is stale.
- Visual check trick: a throwaway JUnit test in the mirror that does `new Scene(root, w, h).snapshot(null)` + `ImageIO.write` renders any editor tab to a PNG (Read can show it).

**Still open:** Document Model / drag & drop / column mapping stay in the node dialog (not inline); no Virtual Root; no per-node validators (circular relationships, missing node type for child role, ...). Listed in `docs/sme-reference-comparison.md`.
