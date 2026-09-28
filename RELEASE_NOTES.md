# Release Notes 2026.06-ext0-0.1.2

## Highlights

- **Tree Model** and **Content Model** now have complete, enabled editors (previously a stub and a disabled editor, respectively). Mapping, Structural Mapping and Query Models were also switched on.
- A broad gap-closing pass against the SME reference implementation touched nearly every model type: Document, Form, Type Definition, Relationship, Combined Document, Query, Master Detail, Application, Overview and Print Typesetting Models.
- Roles validation (`HeaderRolesValidator`) is now wired into every model type's validation service, closing a gap that had been open across the board.

## Tree Model

- Built out and enabled: five tabs (Tree, Node Types, Configuration, Layout, Custom Actions).
- New Expansion Strategy editor: Initial Expansion, Pagination, "Enable Expand/Collapse The Whole Tree", and an Expansion Depths editor that only offers relationships without a depth and keeps unused strategy keys from lingering when switching strategy type.
- `Row Activation` replaces the old default row action; column widths are now decimal (matching Overview Model); every context (row, header, multi-selection, row activation) gets its real event list, hiding copy/paste while a relationship has a link Document Model.
- Node type and insert-action Document Model pickers now offer Combination Models and only the real per-position candidates (not every Document Model in the project).
- Added a Virtual Root panel (enable, Label, node actions, context menu).
- 17 new validators (root reference, virtual scrolling, multi-selection, styles, columns, node structure, child relationships, actions, roles).
- Rename/move refactoring now covers tree models (relationships, node types, columns).

## Content Model

- Built out and enabled: Document Model and Base Group binding via a proper settings panel (replacing the free-text Model References panel), with correct import/export semantics for the document reference.
- Structural rules are now enforced on every edit, not just insertion: move, cut, duplicate and paste are all blocked when they would produce an invalid tree; drag and drop was added, with drop zones for above/below/inside.
- 12 new validators: node shape, whole-tree structure, document model type, base group, group/field/image/conditional references (data-context aware), form elements, event nodes, settings, and warnings.
- New editors: Form Element settings, Localization, Additional Settings, Annotations, Conditions, Date Picker configuration, Message Group Container (with auto-collected field/group lists), Image dynamic source, and inline field/group references inside rich text.
- Validation results are now shown in the editor: per-row markers with tooltips, an error/warning summary under the tree, and the settings button badge.
- Roles validation is now applied to Content Models as well.

## Overview Model

- Enumerated String Filter now has an editable field list (previously invisible/uneditable).
- Action Column Width is now decimal, matching SME; a new model no longer serializes stray `null`s and seeds the sub-header/row-action-group the way SME does.
- Sub-header elements (Search, Filter, Multi-Selection) are now gated by their feature switches, and Overview Models used as a Form Model's Available/Selected Items binding are recognized and exempted from rules that don't apply to them.
- Expression columns are now validated, backed by a new Expression language grammar ported from the platform docs; column-level rules added for suffix references, preferred sorting, display mode, and style references.
- Added validation for Paging Size / Row Height / Action Column Width, Initial Sorting duplicates and non-sortable columns, Filter Section labels, Context Menu groups, and the `export_excel` Composed-Document-Model warning.
- Metadata fields (`__meta/createdAt` and friends) can now be picked in field/column pickers; Custom Selection of Fields gained Subtype support for heterogeneous Document Models.
- An "Add" button next to the Query Model reference now creates a new Query Model pre-filled from the Overview's own columns, paging and sorting.
- Structural refactoring: deleting a column now prunes Default Sorting, disabling Search/Filter/Multi-Selection now removes its sub-header element, and renaming/deleting a Style now cascades to every column that referenced it.

## Application Model

- `Constraints` now round-trips arbitrary keys (previously only `MasterDetail`/`preferredWidth` survived a save), with a raw-JSON area in the View Add dialog to author them.
- Fixed a validator bug that checked region-name uniqueness across the whole model instead of per parent, and another that accepted a reference of the wrong model type; added a `preferredWidth` (1-11) range check.
- Region fields (Default Region, Scene Change directives) are now breadcrumb-picker combo boxes sourced from the model's own region tree, instead of free text.
- Renaming or deleting a Region, Scene or Case now auto-rewrites every reference to it within the model (Default Region, directive regions, Prior Scene, Default Case).
- Roles validation is now applied to Application Models.

## Master Detail Model

- Binding Overview Models (an Overview Model that only backs a Form Model's Available/Selected Items widget) are now excluded from the master Overview Model combo.
- Roles validation is now applied to Master Detail Models.

## Document Model

- Fixed an over-strict Enumeration label validator that flagged perfectly valid unlabeled enumerations; added cross-validation for String's `noValueValidation`.
- Added the two missing Include checks (locale coverage, single non-repeatable root group), a Supported Characters validator, and name-pattern checks for Group/Field/Rule/Computation names.
- Base Model and Include pickers no longer offer candidates that would create an include loop.
- Document Uniqueness Criteria (the content-level, path-addressed uniqueness rule) now has an editor panel and a validator.

## Form Model

- Roles validation is now applied to Form Models.
- Added validators for Amount Suffix field references, Placeholder/Exposition conflicts, External Enumeration/Exposition conflicts, duplicate style names, and the reserved `bindingConfiguration` annotation name.
- Label-as-Expression fields are now validated with the same expression syntax checker built for Overview expression columns.
- Removed a stale, invalid dependent-group condition from a form fixture that Studio's own drift validator correctly flagged.

## Type Definition Model

- A Type Definition Model can now hold local type definitions and imported ones at the same time (previously Add/Import buttons incorrectly disabled each other, even on the Type Definition Model that is meant to combine both).
- A multi-select group's enumeration value field can no longer point at an imported or included type definition, matching SME.
- Added blank id/name validation for type definitions.

## Relationship Model

- The entity and Link Document Model pickers now offer Combination Models, and a validator bug that treated the two references inconsistently is fixed.
- Added role name pattern/length validation.
- Renaming a role now also rewrites Tree Model child-relationship references (previously only Query/Form/Relationship UI/Overview Models were updated).
- Roles validation is now applied to Relationship and Relationship UI Models.
- Relationship labels are hidden in the add/edit dialog, matching SME's read-only behavior there.

## Combined Document Model

- The Base Model and Decoration pickers now offer Combination Models; the Addition step's picker is now restricted to Additive Document Models.
- Added "Invalid Reference" validation for the base model and for additive/selection/decoration steps, reported directly on the offending step.
- Roles validation is now applied to Combined Document Models.

## Query Model

- A Combination Model can now be picked, and resolved, as a Query Model's target.
- Roles validation is now applied to Query Models.

## Print Typesetting Model

- Fixed a regression where Model Settings showed Name/Description fields that should be hidden for a Typesetting Model.
