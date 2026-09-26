---
name: tab-split-panes
description: Editor tabs can be split into several StudioTabPanes side by side (built 2026-09-26); the activePane concept in TabPaneController, DnD between panes, what is not persisted, and the Tab.getStyleableNode() trap
metadata:
  type: project
---

Built 2026-09-26: tab context menu "Split and Move Right" (`split_tab_right`) and drag and drop of tab headers between `StudioTabPane`s. `scene-tab-pane.fxml` now wraps the FXML's `tabPane` in a `SplitPane fx:id="splitPane"`; `TabPaneController` keeps `panes` (left to right) and `activePane`.

- **`activePane`** = last clicked/selected pane (a `MOUSE_PRESSED` filter per pane). New tabs open there; `getSelectedProjectItem()`, close/next/previous-tab shortcuts and the `TabSelectionChangedEvent` follow it. A selection change in a non-active pane is not published until that pane becomes active. `loadTabContent` makes the tab's pane active first, because editor panels resolve their model via `Studio.getSelectedProjectItem()`.
- An emptied pane is removed automatically (unless it is the only one); `projectOpened` collapses back to the single FXML pane (`resetToSinglePane`). Moving a tab is remove + add on the same `Tab` - editor and state survive, no `ModelClosedEvent`.
- Drag and drop lives in `StudioTabPane` (custom `DataFormat`, so editor text controls don't accept the drag; DRAG_OVER/DROPPED are *filters*); it only reports `setOnTabDropped`, the controller does the move. The static `draggedTab` carries the tab. Dropping on the same pane is ignored (no reordering).
- **Trap:** `Tab.getStyleableNode()` is `null` under the regular `TabPaneSkin`, so headers are mapped to tabs by sibling order there, and by a `TAB_HEADER_KEY` node property in `MultiRowTabPaneSkin`. The header lookup only accepts headers inside the pane's own `tab-header-area` (editors contain nested TabPanes with `.tab` headers of their own).
- **Not done:** the split layout is not persisted - on the next start all opened files are restored into one pane. The drag gesture itself has no automated test (a headless test can't start a DnD gesture); the move logic (`moveTab`) and the header lookup are tested in `TabPaneControllerTest`/`StudioTabPaneTest`.
- Close All / Close Others in the tab menu span all panes.

**Why:** requested as VS-Code-style editor groups. **How to apply:** before touching tab code, remember there is no single `tabPane` any more - go through `panes`/`activePane`/`paneOf(tab)`; the `tabPane` field is only the pane the FXML starts with.

Seven UI tests (ContentModelEditorPanelsTest x4, TreeModelEditorPanelsTest x2, TypesettingModelEditorTest x1) were already red on a clean HEAD export on 2026-09-26, unrelated to tabs - see [[test-harness-notes]], whose "suite is green" baseline is stale.
