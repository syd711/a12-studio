# widgets widgets showcase docs

Widgets
Introduction
Our A12 Widgets mission is to provide a wide range of web components that follow a consistent
and attractive design and interaction concept (Plasma) to support business applications running on
desktop, tablet and smartphone supporting keyboard, mouse and touch input. The components
provide an easy to use, well documented, strongly typed API and are extendable and customizable.
The widgets showcase serves not only as a showcase of examples but also as full user
documentation.
Click here for the Widgets showcase.
Migration Instructions
Migration Notes
39.0.2
No breaking changes in 39.0.2.
39.0.1
DataTable, DataTreeTable & TreeView moved to /experimental
The recently introduced DataTable, DataTreeTable and TreeView components (and their related types
and helpers) are no longer exported from the package root. They now live on a dedicated entry
point: @com.mgmtp.a12.widgets/widgets-core/experimental.
This keeps @atlaskit/pragmatic-drag-and-drop — the drag-and-drop engine behind these
components — out of the main module graph. Its package exports subpaths fail to resolve as
directory imports under Node ESM in some consumer build environments (e.g. Directory import
'.../pragmatic-drag-and-drop/element/adapter' is not supported). Importing the package root no
longer pulls that dependency in.
// Before
import { DataTable, DataTreeTable, TreeView } from "@com.mgmtp.a12.widgets/widgets-
core";
// After
import { DataTable, DataTreeTable, TreeView } from "@com.mgmtp.a12.widgets/widgets-
core/experimental";
1

-- 1 of 55 --

39.0.0
New baseTheme replaces flat / flat-compact
getBaseTheme() provides a clean 3-layer theme (Application → Semantic → Widget) as the
recommended A12 base theme, replacing getFlatTheme() and getFlatCompactTheme(). Its visual
appearance matches the existing flat theme, so migrating from flat is a like-for-like swap.
default / compact themes are being retired. getDefaultTheme / defaultTheme and getCompactTheme /
compactTheme are deprecated and will be removed in a future release with no replacement —
their visual style is not carried forward. getBaseTheme() adopts the flat look and does not reproduce
the default/compact appearance. Projects still on those themes can adopt getBaseTheme() to stay
supported, but should expect a visual change.
Migrating from flat / flat-compact
getBaseTheme() is the successor to the flat factories and createTheme(). It defaults to 12px base
spacing, so pass spacing: { base: 16 } to keep the previous non-compact density:
Deprecated Replacement
getFlatTheme() / flatTheme getBaseTheme({ spacing: { base: 16 } })
getFlatCompactTheme() / flatCompactTheme getBaseTheme()
createTheme({ ... }) getBaseTheme({ ... })
// Before
import { getFlatTheme } from "@com.mgmtp.a12.widgets/widgets-core";
const theme = getFlatTheme();
// After — base: 16 preserves the previous (non-compact) spacing
import { getBaseTheme } from "@com.mgmtp.a12.widgets/widgets-core";
const theme = getBaseTheme({ spacing: { base: 16 } });
createTheme’s options shape differs slightly. `getBaseTheme exposes the override layers (colors,
semantic, spacing, typography, components); see core/src/theme/base-theme/schema.ts for the full
BaseThemeOptions type. The codemod does not silently drop unknown options — migrate anything
outside that shape manually.
For overriding colors, semantic tokens, spacing, fonts, and per-component configs, see the Base
Theme guide.
New tokens
All existing flat/legacy semantic token names are preserved and keep working — colors.text.*,
colors.background.*, colors.divider.color, the colors.interaction.* states (hover / active / selected
/ focus / disabled), and colors.variant.* are unchanged. The base theme adds the following tokens
for surfaces that were previously hardcoded:
2

-- 2 of 55 --

New token Replaces
colors.text.titleColor hardcoded card / dialog heading
colors.text.placeholderColor hardcoded placeholder color
colors.background.navigationBackground hardcoded sidebar background
colors.background.navigationAccent hardcoded menu decoration
colors.background.overlayLight hardcoded scrim / overlay light
colors.divider.colorMuted hardcoded subtle divider
colors.interaction.color, colorDark neutral interactive tint
colors.interaction.colorBG, colorBGLight —
colors.interaction.hover.colorLight —
colors.interaction.touchOverlay rgba(0,0,0,0.2) inverted button overlay
colors.interaction.touchOverlayDark rgba(0,0,0,0.35) activated inverted overlay
colors.shadow.overlayFaint rgba(0,0,0,0.04) glow
colors.shadow.overlaySoft rgba(0,0,0,0.1) card shadow
colors.shadow.overlayMid rgba(0,0,0,0.2) popover
colors.shadow.overlayDark rgba(0,0,0,0.3) modal scrim
colors.shadow.overlayDeep rgba(0,0,0,0.5) overlay
Top-level border, motion, opacity, and hoverStyles keys are also new. See core/src/theme/base-
theme/schema.ts for the exact shape of BaseThemeConfig.
Quick theming
For applications that only need to recolor the theme (without overriding component configs), use
getQuickTheme():
import { getQuickTheme } from "@com.mgmtp.a12.widgets/widgets-core";
const theme = getQuickTheme({
palette: {
primary: "#005FAD",
surface: "#ffffff",
pageBackground: "#f7f8fa",
border: "#d9dde3",
textPrimary: "#1f2937",
success: "#1f9d55",
warning: "#d97706",
error: "#dc2626",
info: "#2563eb"
}
});
3

-- 3 of 55 --

Missing palette variants (primaryHover, primaryActive, primaryLight, primaryTint) are derived
automatically. Pass buildQuickThemeOptions(input) instead when you want to combine the quick
theme palette with further getBaseTheme overrides.
Styled-components DefaultTheme augmentation
This only matters if your own typed styled-components read base-theme-specific tokens through
the theme prop (e.g. theme.colors.shadow, theme.hoverStyles). If they don’t, your existing
augmentation keeps compiling unchanged. To get autocomplete and type-checking for the new
tokens, augment DefaultTheme with BaseThemeConfig:
// src/@types/styled-components.d.ts
import "styled-components";
import type { BaseThemeConfig } from "@com.mgmtp.a12.widgets/widgets-core";
declare module "styled-components" {
export interface DefaultTheme extends BaseThemeConfig {}
}
New DataTable, DataTreeTable & TreeView (experimental)
39.0.0 ships three new, opt-in components built on a shared headless model: DataTable,
DataTreeTable and TreeView. They are additive and @experimental — nothing breaks, and no
migration is required. The production Table / TreeTable are not deprecated; only the legacy Tree
template and its behavior HOCs are.
Because this is an opt-in adoption (not a forced change), the details — supported subset, the
componentRenderers → slots mapping, the virtualization-engine swap, tree drag-and-drop — live on
their own page: see New Table & Tree Components.
Year Selector
Changed default rendering mode
Previously, <YearSelector /> was always rendered as a native <select> element. Starting from
version 39.0.0, a variant property (textbox | select | autocomplete) has been introduced to allow
customization of how the selector is displayed. If the variant property is not explicitly provided, the
component will resolve the variant based on the presence of yearRange property:
Condition Resolved variant
yearRange provided autocomplete (text input + dropdown)
no yearRange textbox (plain numeric text input)
The same applies to the embedded year selector in <YearMonthSelector /> when yearSelectorVariant
is not set.
Migration — add an explicit variant/yearSelectorVariant to keep the old <select> behaviour:
4

-- 4 of 55 --

// Before (implicitly rendered as <select>)
<YearSelector year={2024} yearRange={{ start: 2020, end: 2030 }} />
<YearMonthSelector year={2024} month={3} yearRange={{ start: 2020, end: 2030 }} />
// After (explicit, preserves previous behaviour)
<YearSelector year={2024} yearRange={{ start: 2020, end: 2030 }} variant="select" />
<YearMonthSelector year={2024} month={3} yearRange={{ start: 2020, end: 2030 }}
yearSelectorVariant="select" />
YearRange fields are now optional
YearRange.start and YearRange.end are now typed as number | undefined to support single-bound
ranges. Code that reads either field without a null-check may encounter undefined.
// Before — safe without a guard (fields were always numbers)
const label = `${yearRange.start} – ${yearRange.end}`;
// After — guard required
const label = `${yearRange.start ?? ""} – ${yearRange.end ?? ""}`;
Enforcement of top-level package exports
Automatic migration (required): run the enforce-top-level-exports codemod — see the recipe for
the full list of changes it applies.
Starting with 39.0.0, widgets-core and widgets-utils enforce strict exports fields in their
package.json. Deep imports through lib/ paths are no longer allowed and will fail at build time. The
codemod rewrites the deep lib/ import paths and the CSS entry-point paths for you; the type-
augmentation paths below are not handled by the codemod and must be updated by hand.
Type augmentations
Type augmentations (e.g. the styled-components DefaultTheme augmentation) are now exposed via
./types/* subpath exports instead of deep lib/@types/ paths. Update your tsconfig.json or
reference directives:
Option 1: tsconfig.json types array
// Before
{
"compilerOptions": {
"types": ["@com.mgmtp.a12.widgets/widgets-core/lib/@types/styled-components"]
}
}
// After
{
"compilerOptions": {
5

-- 5 of 55 --

"types": ["@com.mgmtp.a12.widgets/widgets-core/types/styled-components"]
}
}
Option 2: Triple-slash reference directive
// Before
/// <reference path="../node_modules/@com.mgmtp.a12.widgets/widgets-
core/lib/@types/styled-components/index.d.ts" />
// After
/// <reference types="@com.mgmtp.a12.widgets/widgets-core/types/styled-components" />
Available type augmentations: styled-components, react, react-lines-ellipsis, assets.
Renamed TextLine to TextField
Automatic migration (required): run the enforce-top-level-exports codemod — see the recipe for
the full list of changes it applies.
The TextLine naming is dropped in favour of TextField across the public API — the component and
its props, the DataRoles key, the data-role values, and the theme configuration. The codemod
rewrites all of these for you.
Note: the data-role change affects test selectors and CSS attribute selectors in your own code —
update any that match the old "textline-*" values.
Removed deprecated APIs
Automatic migration (required): run the enforce-top-level-exports codemod — see the recipe for
the full list of changes it applies.
The deprecated aliases introduced in 38.2.0 have been removed. They were originally created to
avoid naming collisions when moving from deep lib/ imports to the top-level barrel export; with
the enforcement of top-level exports in 39.0.0 the old names are no longer needed and have been
removed to keep the public API clean. The codemod renames each removed alias to its replacement
automatically.
Migration from Material Icons to Material Symbols
The icon system has been migrated from Material Icons (legacy static fonts) to Material Symbols
(variable fonts).
What changed
• The font files have been replaced: the three static fonts (Material Icons, Material Icons
Outlined, Material Icons Round) are replaced by two variable fonts (Material Symbols Outlined,
Material Symbols Rounded). The variable fonts are optimized via axis pinning (unused axes
GRAD, opsz, wght are fixed), resulting in a combined size of ~954 KB — comparable to the
6

-- 6 of 55 --

previous ~450 KB of static fonts, but with access to ~3840 icons and variable FILL support.
• The filled theme now uses Material Symbols Outlined with font-variation-settings: 'FILL' 1
instead of the separate Material Icons font.
• The outlined theme uses Material Symbols Outlined with FILL 0.
• The rounded theme uses Material Symbols Rounded.
• The MATERIAL_ICONS data list has been expanded from ~1500 to ~3840 icons.
What hasn’t changed
• The IconTheme type ("filled" | "outlined" | "rounded" | "custom") is unchanged.
• All existing icon ligature names (e.g., close, check_circle, arrow_drop_down) continue to work.
• The Icon component API is fully backward-compatible.
• Custom icons (iconTheme="custom") are unaffected.
Action required for consumers
• If you self-host fonts: Replace the old Material Icons font files with the new Material Symbols
variable font files from basic.css. The new @font-face declarations reference
fonts/materialsymbols/material-symbols-outlined.woff2 and fonts/materialsymbols/material-
symbols-rounded.woff2.
• If you use font-family: "Material Icons" directly in your CSS: Update to font-family:
"Material Symbols Outlined" with font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0,
'opsz' 24.
• If you use snapshot testing: Snapshots that include icon styling will need to be updated due to
the changed font-family values and new font-variation-settings property.
Removed useCustomView from NativeSelectProps and CustomSelectProps
The redundant useCustomView property has been removed from NativeSelectProps and
CustomSelectProps. Since the type itself already encodes which rendering mode is used.
If you are passing useCustomView to a CustomSelect or NativeSelect, simply remove it:
// Before
<CustomSelect useCustomView={true} ... />
<NativeSelect useCustomView={false} ... />
// After
<CustomSelect ... />
<NativeSelect ... />
If you need to switch between rendering modes dynamically, use the top-level Select component
and its useCustomView property from SelectProps:
import { Select } from "@com.mgmtp.a12.widgets/widgets-core";
7

-- 7 of 55 --

import { useState } from "react";
const [isUseCustomView, setIsUseCustomView] = useState(true);
<Select useCustomView={isUseCustomView} ... />
Tooltip: Wrapper Element Renamed and DataRoles Changed
The Tooltip component has three main elements:
• Trigger Wrapper — the element wrapping the trigger element of Tooltip, always in the DOM.
• Container — the floating tooltip bubble, rendered in a portal and anchored to the trigger
element when the tooltip is visible.
• Content — the element inside the bubble that contains the actual tooltip text.
Previously, the trigger wrapper was named StyledTooltipWrapper. That gave no hint about its
purpose, making it easy to confuse with the tooltip bubble itself (StyledTooltipContainer). On top of
that, both elements shared data-role="tooltip", which made element selection by data-role
ambiguous.
To address this, the wrapper’s name and data-role has been updated.
• Styled component rename
Element Before After
Tooltip trigger wrapper StyledTooltipWrapper StyledTooltipTriggerWrapper
Tooltip container StyledTooltipContainer unchanged
Tooltip content StyledTooltipContent unchanged
• DataRoles changes
Element Before After
Tooltip trigger wrapper DataRoles.Tooltip DataRoles.Tooltip.TriggerWrapp
er
Tooltip container DataRoles.Tooltip unchanged
Tooltip content DataRoles.Tooltip.Content unchanged
This affects test selectors and CSS attribute selectors to the trigger wrapper element:
// Before
getByDataRole(container, "tooltip");
// After
getByDataRole(container, "tooltip-trigger-wrapper");
8

-- 8 of 55 --

And also affects direct imports of the trigger wrapper’s styled component:
// Before
import { StyledTooltipWrapper } from "@com.mgmtp.a12.widgets/widgets-core";
// After
import { StyledTooltipTriggerWrapper } from "@com.mgmtp.a12.widgets/widgets-core";
Switch Thumb Icon Color Changes
The switch thumb icon color theme configuration has been updated from
thumb.uncheckedBackground to thumb.checkedIconColor and thumb.uncheckedIconColor.
NOTE: For warning, error, disabled, and readonly variants, the unchecked thumb icon still
matches the corresponding variant’s color to maintain visual consistency with the state styling.
Checked State Variant Previous Theme Key Current Theme Key
On All variants thumb.color thumb.checkedIconColor
(new key)
Off Default thumb.uncheckedBackgro
und
thumb.uncheckedIconCol
or (new key)
Off Warning thumb.uncheckedBackgro
und
thumb.warningColor
Off Error thumb.uncheckedBackgro
und
thumb.errorColor
Off Disabled thumb.uncheckedBackgro
und
thumb.disabled.color
Off Readonly thumb.uncheckedBackgro
und
thumb.readonly.color
Automatic migration
The enforce-top-level-exports codemod is required for this release — deep lib/ imports fail at
build time. Run it against your source directory:
npx @com.mgmtp.a12.widgets/widgets-codemod enforce-top-level-exports <your-source-
directory-containing-ts-config-json-file>
For the complete list of change categories this recipe applies — with before/after examples to verify
a codemod diff against — see the enforce-top-level-exports recipe.
The type-augmentation path move (lib/@types/* → types/*) is not rewritten by the codemod and
must be updated by hand — see "Enforcement of top-level package exports" above. All other
breaking changes in this release not marked Automatic migration (required) are manual.
9

-- 9 of 55 --

38.3.4
No breaking changes in 38.3.4.
38.3.3
No breaking changes in 38.3.3.
38.3.2
No breaking changes in 38.3.2.
38.3.1
No breaking changes in 38.3.1.
38.3.0
No breaking changes in 38.3.0.
38.2.2
No breaking changes in 38.2.2.
38.2.1
No breaking changes in 38.2.1.
38.2.0
Deprecation of nested imports
Nested imports are deprecated in favor of top-level imports to avoid unnecessary breaking changes
caused by moving or renaming internal files. This makes the code more resilient to internal
refactoring, provides a single consistent import path, and reduces ongoing maintenance effort.
Run the codemod command below to migrate automatically:
npx @com.mgmtp.a12.widgets/widgets-codemod prefer-top-level-imports <your-source-
directory-containing-ts-config-json-file>
The codemod will update all nested imports to use the package root:
// Before
import { Button } from "@com.mgmtp.a12.widgets/widgets-core/lib/button/index.js";
// After
10

-- 10 of 55 --

import { Button } from "@com.mgmtp.a12.widgets/widgets-core";
Some entities are deprecated to avoid duplicate export names in the top-level index. Use the
recommended replacements:
Deprecated Use Instead Deprecated Path
ResizeEventHandler ColumnResizeEventHandler @com.mgmtp.a12.widgets/widgets
-core/lib/table/new
-api/table.api.js
IconPicker IconPickerTitles @com.mgmtp.a12.widgets/widgets
-core/lib/common/main/a11y
-localization/a11y-key
-definition.api.js
TooltipProps TooltipPluginProps @com.mgmtp.a12.widgets/widgets
-core/lib/rich-text
-editor/main/plugins/tooltip
-plugin/view/tooltip.api.js
Tooltip TooltipPlugin @com.mgmtp.a12.widgets/widgets
-core/lib/rich-text
-editor/main/plugins/tooltip
-plugin/view/tooltip.view.js
TooltipWrapperProps TooltipPluginWrapperProps @com.mgmtp.a12.widgets/widgets
-core/lib/rich-text
-editor/main/plugins/tooltip
-plugin/view/tooltip.api.js
commonTileConfigs commonInteractiveTileFlatConfi
gs
@com.mgmtp.a12.widgets/widgets
-core/lib/theme/flat/config/co
mponents/interactive-
tile.config.js
BodyContent TreeTableBodyContent @com.mgmtp.a12.widgets/widgets
-core/lib/tree-table/main/tree
-table.view.js
BodyCell TreeTableBodyCell @com.mgmtp.a12.widgets/widgets
-core/lib/tree-table/main/tree
-table.view.js
walk walkTreeNode @com.mgmtp.a12.widgets/widgets
-core/lib/tree/main/behavior/t
ree.behavior.api.js
Deprecation of Relation Node
The entire relation-node module has been deprecated, including all its components and interfaces.
This module should no longer be used in new projects and existing usages should be migrated to
the newer model-graph-diagram module components.
Deprecated Components and Interfaces
All entities from the relation-node module are deprecated:
• NodeTpl namespace and all its sub-components (NodeTpl.Node, NodeTpl.NodeTitle, etc.)
11

-- 11 of 55 --

• NodeTplProps namespace and all its interfaces
• createPort higher-order component (HOC)
• PortProps interface
Migration Guide
Use DiagramNode and DiagramPort from the model-graph-diagram module instead:
// Before
import { NodeTpl, createPort, PortProps } from "@com.mgmtp.a12.widgets/widgets-core";
// After
import { DiagramNode, DiagramPort } from "@com.mgmtp.a12.widgets/widgets-core";
Component Migration Examples
Node Component:
// Before
<NodeTpl.Node>
<NodeTpl.NodeTitle>My Node</NodeTpl.NodeTitle>
</NodeTpl.Node>
// After
<DiagramNode>My Node</DiagramNode>
Port Component:
// Before
const MyPort = createPort<MyProps, MyPortProps>({
// port configuration
});
// After
<DiagramPort />
The new DiagramNode and DiagramPort components provide improved functionality with better type
safety and simpler API. Refer to the Diagram Shapes documentation for detailed usage examples.
38.1.1
Deprecation of Chart Widgets
Widgets has deprecated the Chart Widgets, which was previously a wrapper around the Recharts
library. The Chart Widgets will be removed in a future release, so users are advised to transition to
using Recharts directly. For the detailed migration guide, please refer to Chart Widgets to Recharts.
12

-- 12 of 55 --

draft-js-editor Package Discontinued
@com.mgmtp.a12.widgets/widgets-draft-js-editor is no longer available. Please migrate to Rich Text
Editor to continue using a supported editor with ongoing updates. The detailed migration guide is
available in Draft-js to Lexical Editor.
38.0.0
React 19 upgrade
Widgets now supports React 19. The upgrade includes the following changes:
• Updated peer dependencies to only support React 19. This allows us to use the latest features
and improvements of React 19.
• Due to this upgrade, some breaking changes has been introduced since React breaks some APIs
and features. Please refer to the React 19 release notes for more information. Most notable
changes:
◦ Removed: propTypes and defaultProps for functions
◦ Removed: ReactDOM.render, use ReactDOM.createRoot instead
◦ Removed: ReactDOM.findDOMNode
◦ ref cleanups required
◦ useRef requires an argument
◦ The JSX namespace in TypeScript
◦ All changes are documented in React upgrade guide, and many include code mods to help
with the migration.
react-day-picker v9 update
• To be compatible with React 19, react-day-picker was updated to major version 9. Since the
Date(Time)Picker inherits and exports react-day-picker props, please refer to react-day-
picker migration notes.
• locale property has been removed from all Date(Time)Picker. Instead of passing the locale to
each picker, it can now be configured in the DateTimeContext.Provider centrally. The locale
should be imported from date-fns library, as this is required by the react-day-picker.
DateTimeContext.Provider is particularly useful when you need to set a specific picker in a
different locale from the others. However, for internationalized applications, this approach has
to be applied globally to ensure all picker components consistently use the same localization
settings.
// BEFORE
<DateInput datePickerProps={{ locale: "de" }} />;
// NOW
import { de } from "date-fns/locale/de";
13

-- 13 of 55 --

<DateTimeContext.Provider value={{ locale: de }}>
<DateInput />
</DateTimeContext.Provider>;
Please refer to Date Picker with Additional Properties for a demonstration.
styled-components v6 upgrade
IMPORTANT:
• Since widgets declares styled-components as peer dependency, please update the version in
package.json to ^6.1.18. Besides, TypeScript definition is now included, therefore please
remove @types/styled-components from package.json.
Other changes:
Breaking Change: disableVendorPrefixes removed.
• In Styled Components v5, the <StyleSheetManager> component supported the
disableVendorPrefixes property:
<StyleSheetManager disableVendorPrefixes>
• In Styled Components v6:
◦ disableVendorPrefixes has been removed.
◦ Vendor prefixes are now disabled by default.
◦ To enable vendor prefixes, set enableVendorPrefixes to true in the StyleSheetManager.
<StyleSheetManager enableVendorPrefixes={true}>
New Requirement: Defining shouldForwardProp
• Styled Components v6 no longer performs automatic prop validation. Instead, it recommends
using transient props ($prop) to pass style-only props to components. If you cannot use
transient props, you must define a shouldForwardProp function to filter props.
◦ You can import the built-in shouldForwardProp function from widgets-core:
import { shouldForwardProp } from "@com.mgmtp.a12.widgets/widgets-
core/lib/common/main/should-forward-prop";
◦ Alternatively, you can define your own shouldForwardProp function.
◦ Applying shouldForwardProp in <StyleSheetManager>
14

-- 14 of 55 --

<StyleSheetManager shouldForwardProp={shouldForwardProp}>
All changes are documented in the styled-components migration notes.
Plugin Editor
Deprecated draft-js based text editor widgets is separated into their own package. The new package
@com.mgmtp.a12.widgets/widgets-draft-js-editor is now available for use. As a result, the
@com.mgmtp.a12.widgets/widgets-core/lib/editor folder is removed. Please update your import from
"@com.mgmtp.a12.widgets/widgets-core/lib/editor" to the new package.
Migration to ESM
The npm artifacts @com.mgmtp.a12.widgets/widgets-core, @com.mgmtp.a12.widgets/widgets-utils, and
@com.mgmtp.a12.widgets/widgets-draft-js-editor were migrated from CommonJS to ESM. When
using Node 22.12+ and modern build tools, there should be no changes necessary to your bundler
setup. Migrating your own application to ESM is not required, but recommended. Consult the
documentation of your bundler for specifics.
Applying Patch for Third-Party Libraries
• Some third-party libraries are not fully compatible with ESM-based build processes due to
incorrect or missing module exports, such as:
◦ @draft-js-plugins/editor
◦ react-dnd
• This causes issues when building or bundling your project. To fix this, patches are included in
this repository to correct the export behavior.
• For detailed instructions on applying the patch, please refer to the Patch Instruction.
Updating to ES2024
The javascript output of the npm artifacts was updated from ES2020 to ES2024 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
Other breaking changes
• Master Detail: Master detail view width is restricted from 1 to 12 columns. This should not
have any impact on existing implementations, but is documented for reference purposes.
• Date Picker:
◦ Before: The selected day maintains the same appearance in both normal and interactive
states.
◦ Now: The selected day has been updated to enhance visualization.
▪ Remove the day.selected.interactiveColor configuration.
▪ Introduce new configuration keys for each interaction state:
15

-- 15 of 55 --

▪ day.selected.interaction.active { background, border, color }
▪ day.selected.interaction.focus { background, border, color }
▪ day.selected.interaction.hover { background, border, color }
• Popup Menu: The icon size in the trigger element has been changed.
◦ Remove the plasmaIconFontSize configuration key.
◦ By default, the icon matches the font size of the Icon Button. When a custom trigger element
is defined (such as a Button with the label and icon), it follows the styles defined by that
custom element.
• Select: If you are using the following properties of SelectItem: hideLabel, secondaryText,
selected, tabIndex, title, and ariaChecked, please remove them. They were wrongly inherit from
DropdownItem, but do not have meaning for a SelectItem, and were removed.
• Action Content Box: The headingElements property in the ActionContentboxProps interface
has been changed from required to optional. It’s necessary to check for undefined value.
// BEFORE: Checking only for null
if (props.headingElements !== null) {
// Render heading elements
renderHeading(props.headingElements);
}
// AFTER: Checking for both null and undefined
if (props.headingElements !== null && props.headingElements !== undefined) {
// Render heading elements
renderHeading(props.headingElements);
}
• Content Box: The variable BASE_CONTENTBOX_DATA_ROLE has been removed. Use
DataRoles.Contentbox instead for better maintainability and consistency.
// BEFORE
import { BASE_CONTENTBOX_CLASS_NAME } from "@com.mgmtp.a12.widgets/widgets-
core/lib/contentbox/main/template/elements/config.js";
<div data-role={BASE_CONTENTBOX_DATA_ROLE} />;
// AFTER
import { DataRoles } from "@com.mgmtp.a12.widgets/widgets-
core/lib/common/main/data-roles.js";
<div data-role={DataRoles.Contentbox} />;
• Supporting Panes Layout:
◦ Type Change: The type of configuration keys transitionDuration has been updated from
string to Duration. This ensures that only valid transition durations (${number}${"ms" |
16

-- 16 of 55 --

"s"}, "initial", or "0") are accepted, allowing for more precise and consistent animation
timing values.
◦ Valid Values: transitionDuration can now only be one of the following:
▪ ${number}ms (e.g., "500ms", "200ms")
▪ ${number}s (e.g., "0.5s", "1s")
▪ "initial"
▪ "0"
◦ No More Loose Strings: Any value like "0.5" or "500" will now trigger a type error, reducing
potential bugs.
• To have better separation of dependencies, namespace TimeUtils in lib/common/main/utils has
been moved to @com.mgmtp.a12.widgets/widgets-core/lib/common/main/date-time/time-utils.js.
Please update your import statement accordingly. For example:
// BEFORE
import { TimeUtils } from "@com.mgmtp.a12.widgets/widgets-
core/lib/common/main/utils";
// AFTER
import { TimeUtils } from "@com.mgmtp.a12.widgets/widgets-
core/lib/common/main/date-time/time-utils";
• Similarly, namespace DateTimeUtils in lib/common/main/utils has been moved to
@com.mgmtp.a12.widgets/widgets-core/lib/common/main/date-time/date-utils.js. Please update
your import statement accordingly.
• The Date/Time utils now operate with the locale object from date-fns instead of the locale
string. For example:
// BEFORE
import { DateTimeUtils } from "@com.mgmtp.a12.widgets/widgets-
core/lib/common/main/utils";
const date = new Date();
const locale = "de";
const format = "DD/MM/YYYY";
DateTimeUtils.formatDateTime(date, locale, format);
// AFTER
import { DateTimeUtils } from "@com.mgmtp.a12.widgets/widgets-
core/lib/common/main/date-time/date-utils";
import { de } from "date-fns/locale/de";
const date = new Date();
const format = "DD/MM/YYYY";
17

-- 17 of 55 --

DateTimeUtils.formatDateTime(date, de, format);
• dayjs is removed from the widgets-core package, as well as the corresponding utility. Please
install it separately if you are using it in your project, or use date-fns instead.
37.2.3
Interaction Hint
• The hint is deactivated by default for interactive elements. To enable the hint, use the
InteractionHintConfigProvider configuration.
import { InteractionHintConfigProvider } from "@com.mgmtp.a12.widgets/widgets-
core/lib/interaction-hint/main/interaction-hint-context";
<InteractionHintConfigProvider
enableInteractionHint>...</InteractionHintConfigProvider>;
37.2.2
Interaction Hint
• In some situations, displaying hints for interactive elements may be unnecessary, particularly
when the default browser tooltips from the title attribute are sufficient. To manage this, you can
disable the hint feature in your application by using InteractionHintConfigProvider. This will
ensure that any interactive element with a title attribute will only show the browser’s default
tooltip, omitting any additional hints.
import { InteractionHintConfigProvider } from "@com.mgmtp.a12.widgets/widgets-
core/lib/interaction-hint/main/interaction-hint-context";
<InteractionHintConfigProvider
enableInteractionHint={false}>...</InteractionHintConfigProvider>;
37.2.0
Accessibility Enhancement
• By default, an element uses the title attribute to provide additional information. When a user
hovers over that element, a tooltip displaying title text appears. However, this information
disappears when the element is focused and is not accessible to screen readers, making it
difficult to convey the purpose of an interactive element. To support a better accessibility, a new
Interaction Hint is introduced. This component will replace the browser’s tooltip with a look-
like Widgets’s Tooltip. It has two main properties:
◦ title: The text that will be shown in the hint.
18

-- 18 of 55 --

◦ referenceElementRef: The reference to the element that the hint will be attached to.
The example below shows how to use the InteractionHint component:
// BEFORE
import * as React from "react";
function ButtonExample(): React.ReactElement {
return <input role="button" type="button" title="This is a submit button"
value="Submit" />;
}
// AFTER
import * as React from "react";
import { InteractionHint } from "@com.mgmtp.a12.widgets/widgets-
core/lib/interaction-hint/main/interaction-hint.view";
function ButtonExample(): React.ReactElement {
const inputRef = React.useRef<HTMLInputElement | null>(null);
return (
<>
<input role="button" type="button" value="Submit" ref={inputRef} />
<InteractionHint referenceElementRef={inputRef} title="This is a
submit button" />
</>
);
}
• Interaction Hint is automatically applied to various Widget components:
◦ Button, Toggle Button.
◦ File Upload.
◦ Flyout Menu.
◦ External Link, Mailto Link.
◦ Interactive Counter.
◦ Interactive Tile.
◦ Rich Text Editor.
◦ Wizard.
Depend on each component, there are different ways to integrate the Interaction Hint. The
common aspect is that the title property is still available for the components that are listed
above. However, instead of being used as a title attribute in the DOM, it is passed to aria-
label or HiddenText. As a result, the title attribute is set to empty, or entirely removed from
the element. For example, we have a Button Widget:
19

-- 19 of 55 --

<Button label="Default" title="This is a button" />
The difference in the DOM will be:
// BEFORE: `title` property's value was passed to `title` attribute.
<button type="button" title="This is a button" data-role="button">
<span data-role="button-label">Default</span>
</button>
// NOW:
// - `title` attribute is set to empty.
// - `title` property's value is passed to `aria-label` attribute.
<button aria-label="Default, This is a button" title="" type="button" data-
role="button">
<span data-role="button-label">Default</span>
</button>
We have an Interactive Counter Widget:
<Counter interactive title="This is an Interactive Counter" value="9" />
The difference in the DOM will be:
// BEFORE: `title` property's value was passed to `title` attribute.
<span title="This is an Interactive Counter" data-role="counter" tabindex="0">
<span data-role="hidden-text">9 Entries</span>
<span aria-hidden="true" role="presentation">
<span>9</span>
</span>
</span>
// NOW:
// - `role="button"` is added.
// - `title` attribute is removed.
// - `title` property's value is passed to the new added `HiddenText` (data-
role="hidden-text").
<span data-role="counter" role="button" tabindex="0" >
<span data-role="hidden-text">9 Entries</span>
<span data-role="hidden-text">, This is an Interactive Counter</span>
<span aria-hidden="true" role="presentation">
<span>9</span>
</span>
</span>
20

-- 20 of 55 --

Theming Customization
• Interactive Tile: To enhance border styling flexibility, the general border configuration key has
been deprecated. Instead, alongside the existing secondary.border, a new primary.border
configuration key has been introduced for the primary Tile.
// BEFORE
import { ThemeProvider, useTheme } from "styled-components";
import { createTheme } from "@com.mgmtp.a12.widgets/widgets-
core/lib/theme/create-theme";
const customTheme = () => {
const interactiveTileConfigs: DeepPartial<InteractiveTileConfigType> = {
border: "1px solid red",
secondary: {
border: "1px solid blue",
}
};
return createTheme({ components: { interactiveTile: interactiveTileConfig } });
}
<ThemeProvider theme={customTheme}>
<InteractiveTile primary />
<InteractiveTile secondary />
</ThemeProvider>
```
```tsx
// AFTER
import { ThemeProvider, useTheme } from "styled-components";
import { createTheme } from "@com.mgmtp.a12.widgets/widgets-
core/lib/theme/create-theme";
const customTheme = () => {
const interactiveTileConfigs: DeepPartial<InteractiveTileConfigType> = {
primary: {
border: "1px solid red",
},
secondary: {
border: "1px solid blue",
}
};
return createTheme({ components: { interactiveTile: interactiveTileConfig } });
}
<ThemeProvider theme={customTheme}>
<InteractiveTile primary />
<InteractiveTile secondary />
</ThemeProvider>
21

-- 21 of 55 --

```
37.0.0
React 18 upgrade changes
• To render a DOM element for calculating its children’s size for responsive behavior such as
FlyoutMenu, ButtonGroupContainer, the newest React 18’s API React.createRoot is used as a
replacement of ReactDOM.render. However, to facilitate the migration effort to React 18, Widgets
still support React 16 & React 17 whereas ReactDOM.render is used in mentioned components. To
enable the fallback behavior, setting A12_ENABLE_REACT_18_SUPPORT to false will ensure the
compatibility. This environment variable can be configured via webpack like below:
plugins: [
webpack.DefinePlugin({
A12_ENABLE_REACT_18_SUPPORT: false
})
];
• Portal has been heavily refactored to be compatible with React 18 strict mode. One of the
fundamental changes is that the portal now relies on React context to find the parent portal
instead of using the DOM API. Each portal will render an additional placeholder DIV element to
accommodate child portals. If you have DOM snapshot test, please be aware that the following
markup may appear in the DOM where the portal is rendered:
<div data-role="portal-placeholder"></div>
Besides, the wrapper property is no longer needed and is removed since Portal will automatically
find its parent element.
• withSizeDetector HOC is difficult to use, and because it combines the props needed for window
resize detection as well as element resize detector, the resulting API is confusing. We also don’t
see the need for a component to combine those behaviors, except in the case of a component
library like Widgets itself. It has now been removed in favor of new hooks: useWindowSize and
useElementSizeDetector
◦ The hooks return the current breakpoint directly, so there is no need to write a callback with
additional state update.
◦ There are also 2 React components to support class component: WindowSizeDetector and
ElementSizeDetector.
Example of the code using withSizeDetector:
const AppFrameWithSizeDetect = withSizeDetector(ApplicationFrame);
const AppView = () => {
22

-- 22 of 55 --

const [windowSize, setWindowSize] =
React.useState<SizeDetectorProps.Size>("lg");
const handleWindowSizeChange = React.useCallback((breakPoint:
SizeDetectorProps.BreakPoint) => {
setWindowSize(breakPoint.size);
}, []);
return (
<AppFrameWithSizeDetect window={true}
onSizeChange={handleWindowSizeChange}>
{windowSize}
</AppFrameWithSizeDetect>
);
};
New code with useWindowSize hook which is much simpler and doesn’t require wrapping of
component:
const AppView = () => {
const { breakPoint } = useWindowSize();
return <ApplicationFrame>{breakPoint.size}</ApplicationFrame>;
};
Similar code can be written for the new useElementSizeDetector hook, but a targetRef
property is needed pointing to the element to listen for the size change.
• Switch to the original react-virtualized that now supports React 18. Therefore,
@com.mgmtp.a12.widgets/react-virtualized-fork@10.0.0 is no longer needed.
// BEFORE
import { InfiniteLoader, List as ReactVirtualizedList } from
"@com.mgmtp.a12.widgets/react-virtualized-fork";
// AFTER
import { InfiniteLoader, List as ReactVirtualizedList } from "react-virtualized";
Other breaking changes
• Table, Tree Table:
◦ During drag and drop event, the dragging row rendered by the new preview layer is
rendered as the direct child of the Table Body.
▪ For example, assuming MyCustomBodyRow read values from MyContextProvider.
// BEFORE
<TableBody>
23

-- 23 of 55 --

// ... Few levels below
<MyContextProvider value={myValue}>
...
<MyCustomBodyRow></MyCustomBodyRow>
</MyContextProvider>
</TableBody>
▪ The context provider now should be moved up, preferably mounted as the parent of
TableBody, which make sure the dragging row always have access to the context.
// AFTER
<MyContextProvider value={myValue}>
<TableBody>
...
<MyCustomBodyRow></MyCustomBodyRow>
</TableBody>
</MyContextProvider>
• TabSandbox: referenceElementContainer is removed, since it has no use in the new refactoring.
• Menu:
◦ menu/main/template/menu.tpl.view.tsx: MainMenu component has been renamed to
MainMenuTpl
• Popup Menu:
◦ To support accessibility, the popup menu now features a new design that includes a visible
close button for improving navigation with screen readers on mobiles and tablets.
◦ To revert to the previous design for all popup menus, wrap the application under the
PopupMenuConfigContext. It is not recommended to wrap the context around a specific popup
menu for consistency reasons, but it is possible.
<PopupMenuConfigContext.Provider value={{ enableA11YMobileDesign: false
}}>...</PopupMenuConfigContext.Provider>
• Text Output: By default, the Text Output content is now wrapped by paragraph tags for
improved semantics. A disableParagraphWrapping property has also been introduced for
situations where this default behavior may not be desired (such as when working with block
level elements).
• Button: A significant upgrade has been made to the invert icon button for a better look.
◦ The withBackground property is no longer needed because the inverted icon button’s
appearance now varies depending on its type (regular, primary, secondary, and active).
Therefore, the set of theme configurations button.invertIcon.withBackground is completely
eliminated.
To achieve the same light background as before, use these configuration keys: *
invertIcon.background * invertIcon.activated.background * invertPrimary.background *
24

-- 24 of 55 --

invertSecondary.background
◦ Some other new configuration keys are added for the button:
▪ invertIcon
▪ activated.borderRadius
▪ activated.interaction.focus.outline
▪ interaction.focus.outline
▪ iconButton, primary, secondary, vertical, invertPrimary, invertSecondary:
▪ interaction.focus.outline
Changes in other affected widgets:
◦ Content Box:
▪ The onBackButtonClicked property of the BackButton and the onCloseButtonClicked
property of the CloseButton have been deprecated. Instead, use the onClick property
from the Button widget directly.
▪ Introduce new elements ActionButton and HeadingActionButton for additional actions.
▪ The built-in elements below will ensure the buttons displayed in the Content Box’s
header match the expected contrast:
▪ ContentBoxElements.CloseButton to display a close button.
▪ ContentBoxElements.BackButton to display a navigation button.
▪ ActionButton or HeadingActionButton to display an additional action button in the
Content Box’s header. The difference between them is the ActionButton is a single
button, meanwhile the HeadingActionButton is an addon that contains the
ActionButton.
◦ Date Picker, Time Picker, Date Time Picker:
▪ The built-in element PickerHeaderButton is recommended to render an additional action
button in the picker header. Some theme configuration keys are added for the
customization (dateTimePicker.headerActionButton):
▪ background
▪ color
▪ interaction
▪ active, hover
▪ background, border, borderColor, color
▪ focus
▪ background, border, borderColor, color, outline
▪ Some configuration keys of the datePicker.navButton are removed (active, focus, hover:
background). Use the keys of dateTimePicker.headerActionButton listed above as an
alternative.
▪ Besides, the PickerHeaderCloseButton and PickerHeaderNavButton are recommended to use
25

-- 25 of 55 --

if needed.
◦ Depending on themes, the type of the icon button should be adjusted if it is used externally
with the widget that has a dark or a light background.
Below is how the Collapsible Panel adapts to the new change:
// BEFORE
<CollapsiblePanel
addons={
<CollapsiblePanelElements.Addon>
<Button
invert // inverted button in all themes
icon={<Icon>get_app</Icon>}
/>
</CollapsiblePanelElements.Addon>
}
>
Content
</CollapsiblePanel>
// AFTER
<CollapsiblePanel
addons={
<CollapsiblePanelElements.Addon>
<Button
invert={isDefaultTheme} // inverted button only in the default theme.
In flat theme, it is a normal button
icon={<Icon>get_app</Icon>}
/>
</CollapsiblePanelElements.Addon>
}
>
Content
</CollapsiblePanel>
• Message Color:
◦ The appearance of the warning variant is adjusted to improve contrast. This includes changes
to the color and type of the warning icon. These adjustments are primarily done by widgets.
However, some widgets require an icon to be passed in, therefore, the warning icon shown
in the code below is recommended for use.
This is an example to get the desired warning Status that meets the contrast:
// BEFORE
<Status variant="warning" icon={<Icon>warning</Icon>} />
// AFTER
<Status variant="warning" icon={<Icon iconTheme="outlined">warning_amber</Icon>}
26

-- 26 of 55 --

/>
◦ Additional Changes:
▪ Besides the regular and light colors, a dark color has been introduced for each variant.
This change is intended for the warning variant, with its dark color being darker than
the regular color, while the other variants' dark color remains the same as the original
color.
▪ variant.errorColorDark
▪ variant.infoColorDark
▪ variant.successColorDark
▪ variant.warningColorDark
▪ Each variant now has its own text color:
▪ variant.text.error
▪ variant.text.info
▪ variant.text.success
▪ variant.text.warning
▪ The theme configuration keys of some components have been removed:
▪ components
▪ badge: color
▪ globalMessageBox: graphic.color, text.color
▪ modalNotification: closeButton.color, errorBG, infoBG, successBG, warningBG,
icon.color, titleColor
▪ toast: variantIcon.color
▪ validationBar:
▪ background
▪ graphic: background, color
▪ mobile: graphic.color, icon.color, overview.background, overview.right.color
▪ title.color
▪ status:
▪ variant: color, lightBackground, lightColor
▪ Several new variants have been added, enabling customization of the element according
to specific variants:
▪ components
▪ chat:
▪ notification.content.variant.text: error, info, success, warning
▪ fileUpload:
27

-- 27 of 55 --

▪ uploaded.borderColor: error, info, warning
▪ icon.variant: error, info, warning
▪ icon.additional.variant:
▪ error, info, warning
▪ text: error, info, warning
▪ item.horizontal.badge.backgroundColor.warning
▪ globalMessageBox:
▪ variant.text: error, info, success, warning
▪ modalNotification:
▪ closeButton.color: error, info, success, warning
▪ variant: error, info, success, warning
▪ variant.text: error, info, success, warning
▪ toast:
▪ variantIcon: error, info, success, warning
▪ tooltip: warning.contentColor
▪ validationBar
▪ variant:
▪ error, info, warning
▪ text: error, info, warning
• Status: The deprecated light property has been removed. The alternative way to customize the
Status is by using the theme variables.
import { ThemeProvider, useTheme } from "styled-components";
import { createTheme } from "@com.mgmtp.a12.widgets/widgets-core/lib/theme/create-
theme";
const customTheme = () => {
const statusConfigs: DeepPartial<StatusConfigType> = {
text: {
color: { warning: colors.text.color }
},
icon: {
color: { warning: colors.variant.warningColorDark }
}
};
return createTheme({
components: { status: statusConfigs }
});
};
28

-- 28 of 55 --

<ThemeProvider theme={customTheme}>
<Status variant="warning" icon={<Icon>warning</Icon>}>
Warning
</Status>
</ThemeProvider>;
Codemod Instructions
A command-line tool for running automated code transformations (codemods) on TypeScript
projects.
Codemods assist with codebase migrations by automatically applying breaking changes,
deprecations, and API updates—reducing manual effort and minimizing human error during
upgrades.
Usage
The codemod supports two primary modes of operation:
1. Recipe-based execution — Run a specific codemod recipe by its identifier
2. Version-based migration — Run all applicable recipes for a target library version
Run the codemod using either npx or pnpm dlx:
npx @com.mgmtp.a12.widgets/widgets-codemod@latest <recipe-id-or-version> <tsconfig-
path> [options]
pnpm dlx @com.mgmtp.a12.widgets/widgets-codemod@latest <recipe-id-or-version>
<tsconfig-path> [options]
Running a Specific Recipe
To execute a single codemod recipe, provide the recipe identifier and the path to your TypeScript
configuration:
npx @com.mgmtp.a12.widgets/widgets-codemod@latest prefer-top-level-imports
./tsconfig.json
Migrating to a Target Version
To run all codemods applicable for migrating to a specific library version, provide the target
version number instead of a recipe identifier:
npx @com.mgmtp.a12.widgets/widgets-codemod@latest 38.0.0 ./tsconfig.json
29

-- 29 of 55 --

The tool automatically identifies and executes all recipes whose supported version range includes
the specified target version.
Interactive Mode
For guided execution, use interactive mode to select recipes or specify the target version through
prompts:
npx @com.mgmtp.a12.widgets/widgets-codemod@latest --interactive
Arguments
• <recipe-id-or-version>
Either the identifier of a specific codemod recipe to execute, or a target version number (e.g.,
1.2.0, 38.0.0) to run all applicable recipes. Use --list to view available recipes and their
supported versions.
• <tsconfig-path>
Path to a tsconfig.json file or a directory containing one. Accepts both absolute and relative
paths (relative to the current working directory).
Options
• --list, -l (default: false)
List all available codemod recipes along with their supported version ranges and descriptions.
npx @com.mgmtp.a12.widgets/widgets-codemod@latest --list
• --interactive, -i (default: false)
Run in interactive mode, allowing you to select a recipe or specify a target version through
guided prompts.
npx @com.mgmtp.a12.widgets/widgets-codemod@latest -i
• --git-check (default: true)
Verify that the git working directory is clean before execution. If uncommitted changes are
detected, you will be prompted to confirm before proceeding. Use --no-git-check to disable this
check.
npx @com.mgmtp.a12.widgets/widgets-codemod@latest 38.0.0 ./tsconfig.json --no-git
-check
30

-- 30 of 55 --

• --help
Display CLI help information including usage syntax, available options, and examples.
npx @com.mgmtp.a12.widgets/widgets-codemod@latest --help
Post-Execution Recommendations
After running codemods, it is recommended to:
1. Review the changes — Codemods apply transformations based on pattern matching and may
not cover all edge cases. Carefully review the generated diff before committing.
2. Run linters and formatters — Codemods do not automatically apply code formatting. Run
your project’s linter (e.g., ESLint) and formatter (e.g., Prettier) to ensure code style consistency.
3. Execute tests — Run your test suite to verify that the transformations did not introduce
regressions.
4. Commit incrementally — If running multiple recipes or migrating across versions, consider
committing after each successful transformation for easier rollback if issues arise.
Recipes
The sections below document each recipe and the exact change categories it applies, with one
before/after example per category. Use them as a reference when reviewing a codemod diff —
every hunk a recipe produces should map to one of its listed categories.
Recipe: enforce-top-level-exports
Supported versions: ^39.0.0. Migrates deep lib/ imports to the top-level barrel, moves CSS imports
to ./styles/, and applies the TextLine → TextField rename and the removed-alias renames.
npx @com.mgmtp.a12.widgets/widgets-codemod@latest enforce-top-level-exports
./tsconfig.json
Rewrite import paths
Any deep lib/** import of widgets-core or widgets-utils collapses to the package root.
// Before
import { Button } from "@com.mgmtp.a12.widgets/widgets-core/lib/button/index.js";
import { SourceCodeSection } from "@com.mgmtp.a12.widgets/widgets-utils/lib/code-
example/index.js";
// After
import { Button } from "@com.mgmtp.a12.widgets/widgets-core";
import { SourceCodeSection } from "@com.mgmtp.a12.widgets/widgets-utils";
31

-- 31 of 55 --

Rewrite CSS import paths
The two CSS entry points move from lib/ to styles/.
Before After
…/widgets-core/lib/theme/basic.css …/widgets-core/styles/basic.css
…/widgets-core/lib/rich-text-
editor/main/themes/rich-text-editor.css
…/widgets-core/styles/rich-text-editor.css
Rename symbols
The removed deprecated aliases are rewritten to their replacements throughout your code.
Renamed export (before) Replacement (after) What this export is
IconPicker IconPickerTitles the a11y localization key for the
icon picker, not the component
Tooltip TooltipPlugin the Rich Text Editor tooltip
plugin, not the general Tooltip
widget
TooltipProps TooltipPluginProps props for the Rich Text Editor
tooltip plugin
TooltipWrapperProps TooltipPluginWrapperProps wrapper props for the Rich Text
Editor tooltip plugin
commonTileConfigs commonInteractiveTileFlatConfi
gs
the shared interactive-tile
config in the flat theme
BodyCell TreeTableBodyCell the tree-table body cell
component
BodyContent TreeTableBodyContent the tree-table body content
component
walk walkTreeNode the tree-node traversal helper
Rename TextLine to TextField
The TextLine naming is dropped in favour of TextField. The recipe applies plain text rewrites, so the
rules below match by name pattern rather than by usage — review the diff for false positives (see
the note after the table).
Rewrite (before) Replacement (after) What is matched
TextLineStateless TextField the component export — in
imports and JSX
TextLineStatelessProps TextFieldProps the props interface — in type
annotations
DataRoles.Textline DataRoles.TextField the DataRoles.Textline property
access
32

-- 32 of 55 --

Rewrite (before) Replacement (after) What is matched
TextLineConfigType TextFieldConfigType the theme config type
textLineConfig textFieldConfig the theme config object
any "textline…" string literal "text-field…" any string/template literal
starting with textline — data-
role values in markup, tests,
and CSS selectors, but also any
other such string
any textLine identifier textField any identifier named textLine
— the theme config key,
destructured names,
createTheme overrides, and
anything else by that name
The last two rules are pattern-based and intentionally broad — a literal
beginning with textline or an identifier named textLine is rewritten
regardless of whether it is widgets-related. Review the diff for unintended
matches in your own code (e.g. unrelated strings or variables).
Not covered
The type-augmentation path move (lib/@types/* → types/*) is not rewritten correctly by this recipe
and must be updated by hand.
Recipe: prefer-top-level-imports
Supported versions: ^38.2.0. Migrates deep widgets-core imports to the top-level barrel and
renames the entities that were deprecated to avoid duplicate export names.
npx @com.mgmtp.a12.widgets/widgets-codemod@latest prefer-top-level-imports
./tsconfig.json
Rewrite import paths
Any deep widgets-core/lib/** import collapses to the package root.
// Before
import { Button } from "@com.mgmtp.a12.widgets/widgets-core/lib/button/index.js";
// After
import { Button } from "@com.mgmtp.a12.widgets/widgets-core";
33

-- 33 of 55 --

Rename symbols
The deprecated entities are rewritten to their replacements.
Renamed export (before) Replacement (after) What this export is
ResizeEventHandler ColumnResizeEventHandler the column-resize event
handler in the table new API
IconPicker IconPickerTitles the a11y localization key for the
icon picker, not the component
Tooltip TooltipPlugin the Rich Text Editor tooltip
plugin, not the general Tooltip
widget
TooltipProps TooltipPluginProps props for the Rich Text Editor
tooltip plugin
TooltipWrapperProps TooltipPluginWrapperProps wrapper props for the Rich Text
Editor tooltip plugin
commonTileConfigs commonInteractiveTileFlatConfi
gs
the shared interactive-tile
config in the flat theme
BodyCell TreeTableBodyCell the tree-table body cell
component
BodyContent TreeTableBodyContent the tree-table body content
component
walk walkTreeNode the tree-node traversal helper
Patch Instructions
Why patching is needed
Some third-party packages may not work correctly due to missing files, incorrect types/import
statements, or other minor issues. While waiting for the maintainers to release a fixed version,
applying patch files is a way to keep the project stable and unblocked, avoiding forking and making
it easy to remove the patch once the official fix becomes available.
Therefore, our published artifacts may include patch files when necessary. These patches will be
removed once we upgrade to a version that includes the official fix.
Available patch file
Below is the patch file included in the published artifact, which can be applied to temporarily fix
known issues in a specific third-party package.
Artifact Third-party package
@com.mgmtp.a12.widgets/widgets-core react-dnd
34

-- 34 of 55 --

How to apply patch files
For each of our published package, if patches are needed, a patches folder will be present at the
same level as the package.json file.
Inside this folder, there are two versions of each patch: one in the pnpm subfolder for native pnpm
patching feature, and one in the npm subfolder for projects that do not use pnpm, for example:
/node_modules
/@com.mgmtp.a12.widgets
/widgets-core
package.json
/patches
/npm ← for npm (or yarn) + patch-package users
/pnpm ← for pnpm users (native support)
Each patch file is named based on the tool that created it, following the general format: <package-
name><separator><version>.patch. The separator is @ for patches created by pnpm, and + for those
created by other tools like patch-package.
The following section explains how projects can apply our patch files depending on the package
manager in use.
For projects using pnpm
1. Copy the patch from its location inside the Widgets package to the root-level patches folder
(create the folder if it doesn’t exist). Example: To patch react-dnd, copy the patch file from:
<project-root>/node_modules/@com.mgmtp.a12.widgets/widgets-core/patches/pnpm/react-
dnd@16.0.1.patch
to your root-level patches folder:
<project-root>/patches/react-dnd@16.0.1.patch
2. Add to your package.json located at the root level (the same level as pnpm-workspace.yaml):
{
"pnpm": {
"patchedDependencies": {
"<package-name>@<version>": "<relative-path-to-patch-file>"
}
}
}
Important: The <relative-path-to-patch-file> must be relative to the project root and
35

-- 35 of 55 --

should point to the patch file inside the patches folder at the root. Replace <package-
name>@<version> with the actual package name and version.
+ Example: To patch react-dnd, add this section to the project root package.json file:
+
{
"pnpm": {
"patchedDependencies": {
"react-dnd@16.0.1": "./patches/react-dnd@16.0.1.patch"
}
}
}
3. Reinstall dependencies
pnpm install
The patch will be applied automatically.
For projects using npm or yarn
1. Install the patch-package package as a development dependency:
npm install patch-package --save-dev
2. Add the following script to the project root package.json file (the one has the same level as
package-lock.json):
{
"scripts": {
"postinstall": "patch-package"
}
}
3. Copy the patch from its location inside the Widgets package to the root-level patches folder
(create the folder if it doesn’t exist).
Example: To patch react-dnd, copy the patch file from:
<project-root>/node_modules/@com.mgmtp.a12.widgets/widgets-core/patches/npm/react-
dnd+16.0.1.patch
to your root-level patches folder:
36

-- 36 of 55 --

<project-root>/patches/react-dnd+16.0.1.patch
4. Reinstall dependencies
npm install
The patch will be applied automatically after installation.
Rich Text Editor Migration Notes
Overview
• The Plugin Editor widget, previously built on top of the draft-js library, has been officially
deprecated. That’s the reason to create a new Rich Text Editor to replace it using Lexical, a
more modern, lightweight, and extensible framework. This new editor provides:
◦ Comparable feature support to the old editor
◦ Enhanced functionality and extensibility
◦ A more performant and modern architecture
Migration Details
Installation
• In your project, install Lexical by the following command:
npm i lexical
DefaultRichTextEditor
• The DefaultRichTextEditor is more accessible for beginners to work with because it’s built on
top of the RichTextEditor and takes care of the most essential nodes and plugins for you. Below
is the steps to guide you how to migration from DefaultEditor to DefaultRichTextEditor:
1. Update import
// BEFORE
import { BoldButton, ItalicButton } from "@com.mgmtp.a12.widgets/widgets-draft-
js-editor";
// AFTER
import { BoldButton, ItalicButton } from "@com.mgmtp.a12.widgets/widgets-core";
2. Update Configuration
37

-- 37 of 55 --

// BEFORE
import type { DefaultEditorProps, MarkTextButtonProps } from
"@com.mgmtp.a12.widgets/widgets-draft-js-editor";
const toolbarPluginConfig: DefaultEditorProps.StaticToolbarPluginConfig = {
structure: [BoldButton, ItalicButton]
};
// AFTER
const BUTTONS = [BoldButton, ItalicButton];
3. Update Component Usage
• Replace all instances of DefaultEditor with DefaultRichTextEditor. Update the props to match
the new component’s API.
• Key Prop Changes:
◦ toolbarPluginConfig → staticToolbarButtons
◦ readOnly → readonly
// BEFORE
<DefaultEditor
id="default-editor"
label="Default Editor with Toolbar"
toolbarPluginConfig={toolbarPluginConfig}
readOnly={selectedValue === "readonly"}
placeholder="Enter anything..."
/>
// AFTER
<DefaultRichTextEditor
id="default-editor"
initialConfig={{ namespace: "Default Rich Text Editor" }}
label="Default Editor with Toolbar"
staticToolbarButtons={BUTTONS}
readonly={selectedValue === "readonly"}
placeholder="Enter anything..."
/>
1. CSS and Styling:
• To use the standard theme for Rich Text Editor, you need to import the CSS file:
import "@com.mgmtp.a12.widgets/widgets-core/styles/rich-text-editor.css";
Plugin
38

-- 38 of 55 --

Mention Plugin
1. Update import
// BEFORE
import { createMentionPlugin, Editor, Mention } from
"@com.mgmtp.a12.widgets/widgets-core/lib/editor";
// AFTER
import { MentionNode, MentionPlugin, RichTextEditor } from
"@com.mgmtp.a12.widgets/widgets-core";
2. Update component usage
// BEFORE
const mentionPlugin = createMentionPlugin();
const { MentionSuggestions } = mentionPlugin;
return (
<div>
<Editor
id="basic-editor"
editorState={this.state.editorState}
onChange={(editorState) => this.onChange(editorState)}
plugins={[mentionPlugin]}
placeholder="Enter @ character"
/>
<MentionSuggestions
suggestions={[
{ name: "A12W", value: "Widgets" },
{ name: "A12P", value: "Plasma" },
{ name: "mgm", value: "mgm-tp" }
]}
onSearchChange={this.onSearchChange}
/>
</div>
);
// AFTER
return (
<RichTextEditor
initialConfig={{
namespace: "Mention Plugin",
nodes: [MentionNode]
}}
id="mention-plugin-editor"
labelGraphic={<Icon>info</Icon>}
placeholder="Enter the @ character"
>
<MentionPlugin
39

-- 39 of 55 --

suggestions={[
{ name: "A12W", value: "Widgets" },
{ name: "A12P", value: "Plasma" },
{ name: "mgm", value: "mgm-tp" }
]}
/>
</RichTextEditor>
);
Link Plugin
1. Installation
◦ To use Link Plugin, you need to install @lexical/link by following this command:
npm i @lexical/link
2. Update import
// BEFORE
import { createLinkPlugin, Editor } from "@com.mgmtp.a12.widgets/widgets-
core/lib/editor";
import { EditorState, ContentState } from "draft-js";
// AFTER
import { $createParagraphNode, $createTextNode, $getRoot } from "lexical";
import { AutoLinkNode, LinkNode } from "@lexical/link";
import { AutoLinkPlugin, createFollowLinkPopupPlugin, RichTextEditor } from
"@com.mgmtp.a12.widgets/widgets-core";
3. Update configuration
// BEFORE
const linkPlugin = createLinkPlugin({
target: "_blank",
customTerms: [
{
regex: /\bA12W-\d+\b/g,
getUrl: (text) => `https://example.com/${text}`
}
],
popupDelayRender: 500
});
const { FollowLinkPopup } = linkPlugin;
// AFTER
const { FollowLinkPopupPlugin, FollowLinkPopup } = createFollowLinkPopupPlugin({
40

-- 40 of 55 --

render: (link) => {
return (
<Button
label="Follow this link"
onClick={(): void => {
if (link.target === "_self") {
window.location.href = link.href;
} else if (link.target === "_blank") {
window.open(link.href);
}
}}
/>
);
}
});
4. Component Usage
// BEFORE
const LinkPlugin = () => {
const [editorState, setEditorState] = useState(
EditorState.createWithContent(ContentState.createFromText(initialContent))
);
const onChange = (newEditorState: EditorState): void => {
setEditorState(newEditorState);
};
return (
<div className="-u-width-full">
<Editor
editorState={editorState}
onChange={onChange}
plugins={[linkPlugin]}
placeholder="Enter some link here"
/>
<FollowLinkPopup render={(link) => <Button label="Follow this link"
onClick={} />} />
</div>
);
};
// AFTER
const LinkPlugin = () => {
return (
<div className="-u-width-full">
<RichTextEditor
initialConfig={{
namespace: "Link Plugin",
nodes: [AutoLinkNode, LinkNode]
41

-- 41 of 55 --

}}
id="link-plugin-editor"
labelGraphic={<Icon>info</Icon>}
placeholder="Type anything..."
>
<AutoLinkPlugin
customTerms={[
{
regex: /\bA12W-\d+\b/g,
getUrl: (text: string) => `https://example.com/${text}`
}
]}
target="_blank"
/>
<FollowLinkPopupPlugin>
<FollowLinkPopup />
</FollowLinkPopupPlugin>
</RichTextEditor>
</div>
);
};
Spell Check
1. Update import
// BEFORE
import { EditorState } from "draft-js";
import {
createSpellCheckPlugin,
Editor,
SpellCheckPlugin,
SpellCheckResult
} from "@com.mgmtp.a12.widgets/widgets-core/lib/editor";
import {
StyledPopupMenuItem,
StyledPopupMenuWrapper
} from "@com.mgmtp.a12.widgets/widgets-core/lib/pop-up-menu/main/popup-
menu.styled";
// AFTER
import { createSpellCheckPlugin, RichTextEditor } from
"@com.mgmtp.a12.widgets/widgets-core";
2. Update Component Usage
const SpellCheckPluginEditor = () => {
const [editorState, setEditorState] = useState(EditorState.createEmpty());
42

-- 42 of 55 --

const [dictionary, setDictionary] = useState<string[]>([]);
// ref to store and access the plugin instance
const spellCheckPluginRef = useRef<SpellCheckPlugin | null>(null);
const closePopupHandlerRef = useRef<() => void>(() => {});
const handleOnClick = (text: string) => {
// Your logic here.
};
const { SpellCheckPopup } = spellCheckPluginRef.current;
return (
<div className="-u-width-full">
<Editor
editorState={editorState}
onChange={setEditorState}
plugins={[spellCheckPluginRef.current]}
placeholder="Enter developr"
/>
<SpellCheckPopup
close={(callback) => {
closePopupHandlerRef.current = callback;
}}
render={(text) => (
<StyledPopupMenuWrapper as="ul">
<StyledPopupMenuItem>
<Button label="Add to dictionary"
onClick={handleOnClick} />
</StyledPopupMenuItem>
</StyledPopupMenuWrapper>
)}
/>
</div>
);
};
// AFTER
export const SpellCheckPluginEditor: FC = () => {
const [dictionary, setDictionary] = useState<string[]>([]);
const handleSpellCheck = (dictionary: string[]): TextMatcher[] => {
// Your logic
};
const { SpellCheckPopup, SpellCheckPlugin } = createSpellCheckPlugin({
spellCheck: handleSpellCheck(dictionary)
});
return (
<div className="-u-width-full">
43

-- 43 of 55 --

<RichTextEditor
initialConfig={{
namespace: "Spell Check Plugin Editor"
}}
id="spell-check-plugin-editor"
placeholder="Enter developr"
>
<SpellCheckPlugin>
<SpellCheckPopup
render={(text) => (
<Button
primary
className="h_blueBG"
label="Add to dictionary"
onClick={(): void => setDictionary([...dictionary,
text])}
/>
)}
/>
</SpellCheckPlugin>
</RichTextEditor>
</div>
);
};
Tooltip Plugin
1. Update import
// BEFORE
import { EditorState } from "draft-js";
import { createTooltipPlugin, Editor } from "@com.mgmtp.a12.widgets/widgets-
core/lib/editor";
// AFTER
import { createTooltipPlugin, RichTextEditor } from "@com.mgmtp.a12.widgets/widgets-
core";
1. Update configuration
// BEFORE
const tooltipPlugin = createTooltipPlugin({
customTerms: [
{
regex: /\bexample\b/g,
render: () => (
<ExternalLink target="_blank" href="https://www.example.com/">
Go to the example homepage
</ExternalLink>
44

-- 44 of 55 --

)
}
],
triggerMode: "focus"
});
// AFTER
const { TooltipPopup, TooltipPlugin } = createTooltipPlugin({
customTerms: [
{
regex: /\bexample\b/g,
render: () => (
<ExternalLink target="_blank" href="https://www.example.com/">
Go to the example homepage
</ExternalLink>
)
}
],
triggerMode: "focus"
});
1. Update component usage
// BEFORE
const TooltipPluginEditor = () => {
const [editorState, setEditorState] = useState(EditorState.createEmpty());
const { Tooltip } = tooltipPlugin;
return (
<div>
<Editor
editorState={editorState}
onChange={setEditorState}
plugins={[tooltipPlugin]}
placeholder="Enter 'mgm-tp'."
/>
<Tooltip />
</div>
);
};
// AFTER
const TooltipPluginEditor = () => {
return (
<RichTextEditor
initialConfig={{
namespace: "Tooltip Plugin Editor"
}}
id="tooltip-plugin-editor"
45

-- 45 of 55 --

placeholder="Enter 'mgm-tp'."
>
<TooltipPlugin>
<TooltipPopup />
</TooltipPlugin>
</RichTextEditor>
);
};
Plugin Creation
1. Installation
◦ If you want to create a custom plugin that modifies the editor’s behavior (e.g., adding a
custom node or handling specific commands), useLexicalComposerContext is essential for
accessing the editor instance and performing these operations. To use this hook, you need to
install @lexical/react package:
npm i @lexical/react
2. Usage
◦ This is the way to use RichTextEditor to build your own custom editor:
// BEFORE
import { EditorState, Modifier, SelectionState } from "draft-js";
import type { EditorPlugin, PluginFunctions } from
"@com.mgmtp.a12.widgets/widgets-draft-js-editor";
import { Editor, EditorUtils, BlockUtils } from "@com.mgmtp.a12.widgets/widgets-
draft-js-editor";
function createCustomPlugin(): EditorPlugin {
// Your logic here
}
const customPlugin = createCustomPlugin();
export function EditorWithCreatedPlugin(): ReactElement {
const [editorState, setEditorState] = useState(EditorState.createEmpty());
return <Editor editorState={editorState} onChange={setEditorState}
plugins={[customPlugin]} />;
}
// AFTER
import { useLexicalComposerContext } from
"@lexical/react/LexicalComposerContext";
import { $isTextNode } from "lexical";
46

-- 46 of 55 --

import "@com.mgmtp.a12.widgets/widgets-core/styles/rich-text-editor.css";
import { InlineStyleTextNode, RichTextEditor } from
"@com.mgmtp.a12.widgets/widgets-core";
export const CustomPluginEditor = () => {
return (
<RichTextEditor
initialConfig={{
namespace: "Custom Plugin Editor"
}}
id="custom-plugin-editor"
>
<CustomPlugin />
</RichTextEditor>
);
};
Chart Widget to Recharts
Chart Widget to Recharts: Overview
We initially introduced Chart Widgets based on Recharts with the goal of customizing them to align
with Plasma’s theming concepts. However, this approach required significant effort and was never
fully completed, we also did not achieve the desired accessibility standards for charts.
Meanwhile, Recharts has evolved significantly and now provides a mature, comprehensive set of
chart components that extend far beyond the basic charts we had originally adopted. Therefore,
from version 38.1.1, we have decided to deprecate our custom Chart Widgets and recommend using
Recharts directly for any charting needs.
This guide helps you migrate from the deprecated Charts Widget to direct Recharts usage.
Installation
First, ensure you have Recharts installed in your project:
// By npm
npm install recharts
// By pnpm
pnpm install recharts
Chart Migration Guides
After that, you can start migrating your existing Chart Widgets to Recharts components. We have
created specific migration guides to assist you in this transition. Please refer to the following guides
based on the chart type you are using:
47

-- 47 of 55 --

• Bar Chart
• Line Chart
• Pie Chart
Key Differences
1. Component Structure
• Before: Single component with properties
<BarChart showLegend xAxisDataKey="name" />
• After: Composed components with children
<BarChart>
<Legend />
<XAxis dataKey="name" />
</BarChart>
2. Styling
• Before: Custom properties for styling
<BarChart barPropsMap={{ value1: { fill: "blue" } }} />
• After: Component-based styling with individual properties
<BarChart>
<Bar dataKey="value1" fill="blue" />
</BarChart>
3. Event Handling
• Before: Custom event handlers
<BarChart onLegendClick={handleClick} />
• After: Recharts native event handlers
<BarChart>
<Legend onClick={handleClick} />
</BarChart>
48

-- 48 of 55 --

4. Accessibility
• Before: Limited accessibility features.
• After: Recharts provides better support for accessibility. Refer to the Recharts Storybook > API >
Accesibility.
5. Additional Features
Recharts provides a broad set of features and customization options that were not available in the
deprecated Chart Widgets. To make the most of its capabilities, explore the Recharts Storybook.
Lastly, you can find examples of common use cases in the Recharts Examples.
Bar Chart Migration
When migrating from the Bar Chart Widget to direct usage of Recharts, certain deprecated APIs
need to be replaced with their corresponding Recharts components and properties.
Deprecated APIs and Their Recharts Equivalents
Below is a mapping of deprecated APIs to their Recharts equivalents for Bar Chart.
Deprecated API Recharts Equivalent
cartesianGridProps <CartesianGrid />
xAxisProps <XAxis />
xAxisDataKey <XAxis dataKey />
xAxisLabel <XAxis label />
xAxisLabelProps <Label />
yAxisProps <YAxis />
yAxisLabel <YAxis label />
yAxisLabelProps <Label />
tooltipProps <Tooltip />
barPropsMap <Bar />
cellPropsList <Cell />
labelKey <Legend />
thresholdProps <Area /> and wrapped by <ComposedChart />
aboveThresholdStyle <Cell />
belowThresholdStyle <Cell />
showLegend <Legend />
showTooltip <Tooltip />
onLegendClick <Legend onClick />
49

-- 49 of 55 --

For the complete list and documentation of all Recharts Bar Chart components, please refer to the
BarChart API.
Deprecated Types and Their Recharts Equivalents
And below is a mapping of deprecated types to their Recharts equivalents for Bar Chart.
Deprecated Type Recharts Equivalent
Layout <Bar layout />
VerticalAlign <Legend verticalAlign />
Align <Legend align />
NOTE: Some Widgets' custom elements, such as Bar Chart template’s Legend and Item are also
deprecated. Therefore, relying solely on Recharts may not provide the same result. If you still wish
to implement these functionalities, please refer to the Widgets core’s existing implementation to
apply the necessary customizations in your project.
Migration Example
This is a practical example of how to migrate from the legacy Bar Chart Widget.
• Before: Single component with properties
import { ResponsiveChartContainer, BarChart } from "@com.mgmtp.a12.widgets/widgets-
core";
const DATA = [
{ product: "Apple", sale: 120 },
{ product: "Peach", sale: 150 },
{ product: "Grapes", sale: 100 },
{ product: "Strawberry", sale: 90 },
{ product: "Blueberry", sale: 140 }
];
const BAR_PROPS_MAP = {
sale: {
dataKey: "sale",
color: "#0088FE"
}
};
<ResponsiveChartContainer aspect={1} maxHeight={400}>
<BarChart
barSize={40}
data={DATA}
labelKey="product"
xAxisProps={{ dataKey: "product" }}
xAxisLabel="Product"
xAxisLabelProps={{ position: "insideBottom", offset: -5 }}
50

-- 50 of 55 --

yAxisLabel="Sale"
barPropsMap={BAR_PROPS_MAP}
cartesianGridProps={{
horizontal: true,
vertical: true
}}
/>
</ResponsiveChartContainer>;
• After: Composed components with children from Recharts directly
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis }
from "recharts";
const DATA = [
{ product: "Apple", sale: 120 },
{ product: "Peach", sale: 150 },
{ product: "Grapes", sale: 100 },
{ product: "Strawberry", sale: 90 },
{ product: "Blueberry", sale: 140 }
];
<ResponsiveContainer aspect={1} maxHeight={400}>
<BarChart data={DATA} barSize={40}>
<CartesianGrid stroke="#f1f2f4" strokeDasharray="2 4" strokeWidth={2} />
<XAxis dataKey="product" label={{ value: "Product", position:
"insideBottom", offset: -5 }} />
<YAxis label={{ value: "Sale", angle: -90, position: "insideLeft" }} />
<Tooltip />
<Bar dataKey="sale" fill="#0088FE" />
</BarChart>
</ResponsiveContainer>;
Line Chart Migration
When migrating from the Line Chart Widget to direct usage of Recharts, certain deprecated APIs
and types need to be replaced with their corresponding Recharts components and properties.
Deprecated APIs and Their Recharts Equivalents
Below is a mapping of deprecated APIs to their Recharts equivalents for Line Chart.
Deprecated API Recharts Equivalent
cartesianGridProps <CartesianGrid />
xAxisProps <XAxis />
xAxisDataKey <XAxis dataKey />
xAxisLabel <XAxis label />
51

-- 51 of 55 --

Deprecated API Recharts Equivalent
yAxisProps <YAxis />
yAxisLabel <YAxis label />
tooltipProps <Tooltip />
linePropsMap <Line />
legendProps <Legend />
thresholdLineProps <Area /> and wrapped by <ComposedChart />
comparableAreaProps <Area /> and wrapped by <ComposedChart />
showLegend <Legend />
showAndHideLines <Line />
onLegendClick <Legend onClick />
onDotClick <Line activeDot />
For the complete list and documentation of all Recharts Line Chart components, please refer to the
LineChart API.
Deprecated Types and Their Recharts Equivalents
And below is a mapping of deprecated type to its Recharts equivalents for Line Chart.
Deprecated type Recharts Equivalent
LegendProps <Legend />
NOTE: Some Widgets' custom elements, such as Line Chart template’s Legend and Item are also
deprecated. Therefore, relying solely on Recharts may not provide the same result. If you still wish
to implement these functionalities, please refer to the Widgets core’s existing implementation to
apply the necessary customizations in your project.
Migration Example
This is a practical example of how to migrate from the legacy Line Chart Widget.
• Before: Single component with properties
import { ResponsiveContainer, LineChart } from "@com.mgmtp.a12.widgets/widgets-
core";
const DATA = [
{ name: "A", desktop: 170 },
{ name: "B", desktop: 150 },
{ name: "C", desktop: 140 },
{ name: "D", desktop: 125 },
{ name: "E", desktop: 100 }
];
52

-- 52 of 55 --

const LINE_PROPS_MAP = {
desktop: {
dataKey: "desktop",
stroke: "#0088FE",
strokeWidth: 2
}
};
<ResponsiveChartContainer aspect={0.5} maxHeight={300}>
<LineChart data={DATA} xAxisProps={{ dataKey: "name", tick: false }}
linePropsMap={LINE_PROPS_MAP} />
</ResponsiveChartContainer>;
• After: Composed components with children from Recharts directly
import { ResponsiveContainer, LineChart, XAxis, YAxis, Line, Tooltip } from
"recharts";
const DATA = [
{ name: "A", desktop: 170 },
{ name: "B", desktop: 150 },
{ name: "C", desktop: 140 },
{ name: "D", desktop: 125 },
{ name: "E", desktop: 100 }
];
<ResponsiveContainer aspect={0.5} maxHeight={300}>
<LineChart data={DATA}>
<XAxis dataKey="name" tick={false} />
<YAxis />
<Tooltip />
<Line dataKey="desktop" stroke="#0088FE" strokeWidth={2} />
</LineChart>
</ResponsiveContainer>;
Pie Chart Migration
When migrating from the Pie Chart Widget to direct usage of Recharts, certain deprecated APIs and
types need to be replaced with their corresponding Recharts components and properties.
Deprecated APIs and Their Recharts Equivalents
Below is a mapping of deprecated APIs to their Recharts equivalents for Pie Chart.
Deprecated API Recharts Equivalent
legendProps <Legend />
tooltipProps <Tooltip />
53

-- 53 of 55 --

Deprecated API Recharts Equivalent
data <Pie data />
pieProps <Pie />
label <Pie label />
rotation <Pie startAngle /> and <Pie endAngle />
For the complete list and documentation of all Recharts Pie Chart components, please refer to the
PieChart API.
Deprecated Types and Their Recharts Equivalents
And below is a mapping of deprecated type to its Recharts equivalents for Pie Chart.
Deprecated type Recharts Equivalent
Rotation <Pie startAngle /> and <Pie endAngle />
NOTE: Some Widgets' custom elements, such as Pie Chart template’s Legend and Item are also
deprecated. Therefore, relying solely on Recharts may not provide the same result. If you still wish
to implement these functionalities, please refer to the Widgets core’s existing implementation to
apply the necessary customizations in your project.
Migration Example
This is a practical example of how to migrate from the legacy Pie Chart Widget.
• Before: Single component with properties
import { ResponsiveChartContainer, PieChart } from "@com.mgmtp.a12.widgets/widgets-
core";
const DATA = [
{ name: "Europe", value: 54, color: "#9c1616" },
{ name: "Asia", value: 44, color: "#f56600" },
{ name: "North America", value: 23, color: "#056294" },
{ name: "Oceania", value: 14, color: "#196719" },
{ name: "South America", value: 12, color: "#b5e4fd" }
];
<ResponsiveChartContainer aspect={0.5} maxHeight={300}>
<PieChart innerRadius="50%" outerRadius="100%" data={data} />
</ResponsiveChartContainer>;
• After: Composed components with children from Recharts directly
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
54

-- 54 of 55 --

const DATA = [
{ name: "Europe", value: 54, color: "#9c1616" },
{ name: "Asia", value: 44, color: "#f56600" },
{ name: "North America", value: 23, color: "#056294" },
{ name: "Oceania", value: 14, color: "#196719" },
{ name: "South America", value: 12, color: "#b5e4fd" }
];
<ResponsiveContainer aspect={0.5} maxHeight={300}>
<PieChart>
<Pie
data={data}
innerRadius="50%"
outerRadius="100%"
dataKey="value"
startAngle={90}
endAngle={-270}
isAnimationActive
>
{data.map((entry, index) => (
<Cell key={`cell-${index}`} fill={entry.color} />
))}
</Pie>
<Tooltip />
</PieChart>
</ResponsiveContainer>;
55

-- 55 of 55 --

