# Release Notes 2026.06-ext0-0.1.3

## Highlights

- **A12 kernel (`31.1.1`) is now a dependency**, reached only through the new `a12-studio-kernel` facade module. It backs rule and computation validation, Document Model / Combination expansion, additive join and the Combination Model preview.
- **Structural Mapping Model** and **Transformer Model** editors were built. The Transformer Model runs the installed SME backend out of process.
- Many Form Model, Document Model, Overview and Query Model refinements.

## Kernel integration

- Rule and computation validation, also while typing in the rule, computation options and computation alternative editors.
- In-service condition validation reports kernel findings next to the grammar check, cached per model.
- Kernel-backed Combination Model preview; combination parity (several steps, additive models that redefine a reference field, `DecorationForGroups`) is pinned by tests.
- Contract tests over `testing/workspaces` guard the facade against kernel changes.

## Transformer Model

- New editor with four tabs and the Model Settings dialog, plus validators.
- The generated Document Model is validated with the kernel; an error fails the run so the model is not cached.
- The last successful result is cached (memory and `.a12-studio/generated/`) and offered in the Overview, Tree, Query and Mapping editors' Document Model pickers. A model that was never transformed is regenerated in the background when another editor asks for it.
- The Preview tab shows the selected element read-only.
- The New Model dialog asks for an optional main XSD, with an "Add XSD files..." entry.

## Structural Mapping Model

- New editor with source and target trees, tag columns, resolution-strategy and move-field-mapping dialogs and a Clear column; kernel-backed.

## Form Model

- Preprocessing Settings panel in the Model Settings dialog.
- D/T dependency badges on the Form Model tree and on the Document Model source tree of the editor.
- Delete confirmation lists the references the delete cleans up.
- Deleting a Screen that navigation buttons target now asks per button: clear the target, select another screen, or delete the button.
- Duplicate annotation names per node are validated.

## Document Model

- `baseYear` in Model Info with `interpretationOfYear` validation.
- `includeLevel` and `roundingMode` now round-trip.
- Duplicate annotation names per element are validated.

## Overview Model

- Custom filter field picker excludes dynamic-suffix and already-used fields.
- Numerous editor and validation refinements.

## Query Model

- Filter definitions are type-checked (port of SME's qmm binder/checker), golden-tested against SME snapshots.
- Filter completion and hover documentation in the rule editor.

## Application Model

- Nested subregions can be edited.

## Content Model

- Length/spacing keywords and units now come from a single source.

## Studio

- Version control: branch and unpushed commit count above the changes tree; Push button with a Force push option.
- Annotations panel shows the source (A12 or annotation data set) of each name suggestion.
- `CommonFieldDefinitions_Td` renamed to `CommonFieldDefinitions_TDM`; `ProductMovie_DM` defines the `Language` type inline for multi-select.

## Known open items

See `TODO.md`. Several of the new editors (Transformer, Structural Mapping, the Form delete dialog) have only been exercised headless, not on a display.
