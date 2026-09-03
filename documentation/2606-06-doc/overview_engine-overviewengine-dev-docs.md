# overview_engine overviewengine dev docs

Overview Engine
Introduction
Overview Engine includes model driven UI components based on Widgets library. It provides a
convenient way to set up a full-featured Overview view through models configurations and
programming interfaces.
This documentation describes in details about features and the integration of Overview Engine into
an existing product.
Getting Started
Installation
Overview Engine is provided as a npm package in ECMAScript modules (ESM) format. Run the
following command to install Overview Engine:
npm install @com.mgmtp.a12.overviewengine/overviewengine-core
Setup
Register Overview Engine module
The registration process of Overview Engine is quite simple, it is only required to Overview Engine
module and the Application sagas factory that can not be delivered by the module itself.
Setup the Overview Engine module
import { DirtyHandlingFactories } from "@com.mgmtp.a12.client/client-
core/dirtyHandling";
import { OverviewEngineFactories } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
import { ApplicationFactories, type ApplicationSetup, ModuleRegistryProvider } from
"@com.mgmtp.a12.client/client-core";
export function setup(): ApplicationSetup {
ModuleRegistryProvider.getInstance().addModule(OverviewEngineFactories.createModule())
;
return ApplicationFactories.createApplicationSetup({
...otherConfigurations,
overridePlatformSagas: [
...OverviewEngineFactories.createApplicationSagas(),
1

-- 1 of 114 --

...DirtyHandlingFactories.createSagas()
]
});
}
Register Overview Engine in non-modular way
However, in case the Overview engine module is not a preferable way to setup the application,
Overview Engine’s factories also allow registering in a non-modular way as below:
Setup the Overview Engine in non-modular way
import type { Middleware } from "redux";
import { DirtyHandlingFactories } from "@com.mgmtp.a12.client/client-
core/dirtyHandling";
import { OverviewEngineFactories } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
import {
type Module,
type DataLoader,
type DataHandler,
type DataProvider,
ApplicationFactories,
type ActivityReducers,
type ApplicationSetup,
ModuleRegistryProvider
} from "@com.mgmtp.a12.client/client-core";
export function setup(): ApplicationSetup {
projectModules.forEach((module) =>
ModuleRegistryProvider.getInstance().addModule(module));
const dataHandlers: DataHandler[] = [
...projectDataLoaders,
createEmptyDocumentDataProvider(),
RelationshipFactories.createRelationshipDataProvider(),
...OverviewEngineFactories.createDataProviders(),
new PlatformSingleDocumentDataLoader(localeProvider)
];
return ApplicationFactories.createApplicationSetup({
...otherConfigurations,
dataHandlers,
overridePlatformSagas: [
...OverviewEngineFactories.createApplicationSagas(),
...DirtyHandlingFactories.createSagas()
],
additionalMiddlewares: [...OverviewEngineFactories.createMiddlewares(),
2

-- 2 of 114 --

...otherMiddlewares],
dataReducers: [...OverviewEngineFactories.createDataReducers(),
...otherDataReducers]
});
}
Query Model integration
Overview Engine can obtain their data according to either
• a Document Model directly referenced in the Overview Model
• a Query Model that contains constraints, sorting information and a reference to a Document
Model
Both strategies can be configured in the Overview Model Editor, so switching between them
requires no client-side code changes.
Document Model Reference
Use this when the overview does not require additional logic beyond what the document model
already provides. The engine keeps the document reference as-is, so no extra constraints are
injected into the query request. The selector OverviewEngineSelectors.modelsState() resolves this
document model immediately and skips any Query Model lookup.
Query Model Reference
When the Overview Model references a Query Model, that Query Model becomes the authoritative
place to store Base Constraints.
The selector OverviewEngineSelectors.modelsState() can still resolve the document model for
compatibility, but now also augments the result with the referenced Query Model.
The Target Document Model specified in the Query Model will be taken as basis for the overview.
Most engine behavior remains unchanged. But the queries constructed by the engine will take the
Query Model into account. This is outlined in the following sections.
Fulltext Search & Filtering
The engine always merges queryModel.content.constraint with the respective constraints created
for the Fulltext Search and active Filters using an AND operator. This ensures base filtering defined
in the Query Model is always applied, regardless of user- or developer-defined Fulltext Searches or
Filters.
Field Filters & Enumerated String Loaders
Field Filters append their operands to the Query Model’s constraint with an AND operator, so ad-hoc
UI Filters are applied on top of the Base Query constraint.
For enumerated string filter, the search requests reuse the same merged logic as above, keeping
3

-- 3 of 114 --

candidate lists aligned with the Base Query constraint.
Field Projection
Regardless of the referencing strategy, the engine always uses the column definitions of the
Overview Model to compute the resulting Query’s field projection. This guarantees that only
necessary Fields are requested from the server.
If additional Fields beyond this default selection are needed, extend the request via the Custom
RequestSelectorMap.
Sorting & Pagination
The Sorting definition for the initial state of the Overview Engine is taken from the Overview
Model. If no Default Sorting is specified there, the respective settings of the Query Model is used. If
neither specifies a sorting, the engine will add a default sorting based on the __meta/createdAt
Field.
When the enduser interacts with the engine, this sorting is applied accordingly.
The Paging definition for the initial state of the Overview Engine is always determined from the
Overview Model. Either directly, if Pagination is chosen, or computed when Infinite Scrolling is
specified. In both cases, the setting of the Query Model is neglected.
Details
Overview Engine component
OverviewEngine is the main React component that is used to render an Overview Engine. The props
of OverviewEngine can belong to one of the following interfaces:
• OverviewEngine.PaginatedProps: is used to define paginated Overview Engines. See further
details here.
• OverviewEngine.InfiniteScrollProps: is used when infinite-scrolling is enabled. See further
details here.
The following snippet presents the above interfaces in details, specifying properties with their
corresponding functions:
OverviewEngine Props
export namespace OverviewEngine {
export type Props = PaginatedProps | InfiniteScrollProps;
export interface InfiniteScrollProps extends CommonProps {
/**
* The data that is rendered in overview engine.
* As this is for infinite-scroll mode, the data can be discontinuous, having
empty elements in the middle
4

-- 4 of 114 --

*/
readonly data: (JSONDocument | undefined)[];
/**
* To control infinite-scroll feature
*/
readonly infiniteScrollOptions: OverviewEngineApi.InfiniteScrollOptions;
}
export interface PaginatedProps extends CommonProps {
/**
* The data that is rendered in overview engine.
*/
readonly data: JSONDocument[];
}
export interface CommonProps extends Container {
/**
* The document model which overview model links to
*/
readonly documentModel: DocumentModel;
/**
* Sub document models which overview model links to
*/
readonly subDocumentModels?: DocumentModel[];
/**
* Resolved relationships across the document and sub-document models. Used by
* link/reference columns to traverse data without re-resolving relationships
per
* row.
*/
readonly modelGraph?: ModelGraph;
/**
* The UI model which is used to render overview engine
*/
readonly overviewModel: OverviewModel;
/**
* If given, the id document of current active row
*/
readonly activeRowId?: string;
/**
* This map is to define state for row actions
* @deprecated Use {@link rowActionStyling} instead.
*/
readonly rowActionState?: OverviewEngineApi.RowActionState;
5

-- 5 of 114 --

/**
* Callback variant of {@link OverviewEngineApi.RowActionState}.
* Called per row and per action; returns the action state for that specific
row.
*
* Prefer this over {@link OverviewEngineApi.RowActionState.rows} when rows
may share `id` (exclude-mode duplicates).
*
* @remarks Wrap with `useCallback` to avoid unnecessary re-renders.
*/
readonly rowActionStyling?: OverviewEngineApi.RowActionStyling;
/**
* The callback controls the style (e.g: interactive,...) of a row
*/
readonly rowStyling?: RowStyleGetter<JSONDocument>;
/**
* To specify aria-level for content box
*/
readonly ariaLevel?: number;
/**
* To enable the card view of the overview table. Useful on small screens.
*/
readonly cardView?: boolean;
/**
* To display overview engine as an embedded element.
*/
readonly embedded?: boolean;
/**
* Event handlers that is used in overview engine
*/
readonly eventHandlers?: OverviewEngineApi.EventHandlers;
/**
* A map of components is used to override the components in the overview
engine
* The components are expected to have rendering logic based on the overview
model, overview engine state and so on
* If not given, the {@link DefaultComponentMap} will be used
*/
readonly componentMap?: ComponentMap;
/**
* A map of Widgets components used in the overview engine
* These components are expected to focus on the UI, therefore, they are
recommended when some UI customizations need to be applied.
* If not given, the {@link DefaultWidgetMap} will be used
6

-- 6 of 114 --

*/
readonly widgetMap?: WidgetMap;
/**
* @experimental
*/
readonly selectorMap?: SelectorMap;
/**
* Filter state selectors. Defaults to {@link DefaultFilterStateSelectors}.
*
* @experimental until 40.0.0 - API may change without semver guarantees.
*/
readonly filterStateSelectors?: FilterStateSelectors;
/**
* The results of statistical operation for each column
*/
readonly summaryResult?: OverviewEngineApi.SummaryResult;
/**
* A property which defines the id prefix for Overview Engine component.
*/
readonly uiIdPrefix?: string;
/**
* A property which defines the thumbnail map
*/
readonly thumbnails?: Record<string, string>;
/**
* UI State
*/
readonly uiState?: UiState;
/**
* The total number of documents
*/
readonly totalDocumentsCount?: number;
/**
* The accessibility configurations
*/
readonly accessibilityConfigurations?:
OverviewEngineApi.AccessibilityConfigurations;
/**
* @experimental
* Reflect the loading state of a Client activity directly to the engine.
* This is an experimental feature, so use it with caution.
*/
7

-- 7 of 114 --

readonly loadingState?: "without" | "missing" | "loading" | "loaded" |
"error";
/**
* Resolved document links and their associated documents for reference
columns.
*/
readonly links?: Links;
}
}
Mostly the component customization will be done at the Client level via view providers by
extending/wrapping OverviewEngineFactories.ViewComponent component. In that case, it needs to
ensure that the handleProgressIndicator flag is handled properly. Consider the following example:
Re-setting handleProgressIndicator for custom views
export function CustomOverviewView(props: View) {
return <OverviewEngineFactories.ViewComponent {...props} uiIdPrefix="custom" />;
}
CustomOverviewView.handleProgressIndicator =
OverviewEngineFactories.ViewComponent.handleProgressIndicator;
const Views: { [name: string]: View.ViewComponent } = {
CustomOverviewView: CustomOverviewView,
OverviewCRUD: (props) => <CRUDViews.OverviewEngineView {...props} />,
FormCRUD: (props) => <CRUDViews.FormEngineView {...props} />
};
export function viewProvider(componentName: string): View.ViewComponent {
return Views[componentName];
}
Here the custom component re-uses the OverviewEngineFactories.ViewComponent component, which
already sets handleProgressIndicator to true internally. Therefore, it needs to also be set on the
wrapped (custom) component when the default behavior should be kept.
Overview Engine Actions
The engine actions are divided into two types: events and commands.
Events signal that something has happened in the UI, triggered by a user interaction. For example
click a button on a row. They are handled by middlewares and will never change the state directly.
They can be dispatched by users, for example Events.onFilterChanged to apply specific filters to the
Overview Engine. Developers are also encouraged to listen to them, for example to
Events.onEventButtonClicked to get notified about an event button being clicked.
Commands are used to directly modify the Redux state. They are dispatched by other
8

-- 8 of 114 --

Events/Commands and are usually implemented in reducers. Users are encouraged to dispatch
them, for example Commands.setDisabled to disable the UI, but are NOT encouraged to listen to them.
Which commands are dispatched in what order and by what user interaction is considered an
implementation detail and a change is not considered breaking.
Why are actions separated into events and commands?
This helps to understand the engine’s runtime behavior better, because you can rely on the effect of
actions only creating other actions or changing the state. This makes maintenance, customizing,
and debugging easier.
It also serves as a reminder not to listen to commands when you want to react to user interactions.
Which event is dispatched by what user interaction is fixed. Which commands are dispatched as a
result might change and should not be relied on.
Commands and Events
Events
All UI-Events trigger the dispatching of a Redux action. The behavior, which gets triggered by these
events, can be changed by registering custom middlewares. The following table describes what
each action does and what user interaction leads to the action.
For a more details: action’s payloads, please refer to the API documentation.
Event Description Dispatched by
onSearched trigger a full text search the full text search input
onInfiniteScrolled update infinite scroll state the scroll event of the table
onPageClicked change the current page to a
specific one
the pagination block to select a
specific page
onNextPageClicked change the current page to the
next one
the pagination block to select
the next page
onPreviousPageClicked change the current page to the
previous one
the pagination block to select
the previous page
onFilterChanged apply the filters the apply button of the filter
selector
onSorted change the sorting column click event on a column header
onMultiSelectionButtonClicked toggle the multi-selection state a click event on the
collapse/expand multi-selection
section
onOverallMultiSelectionButton
Clicked
select all rows of the current
page for multi-selection
a click event on checkbox in the
multi-selection column header
onMultiSelectionCleared deselect all multi-selected rows after successfully apply a new
filter or search
9

-- 9 of 114 --

Event Description Dispatched by
onRowsSelected select row(s) for multi-selection a click event on multi-selection
checkbox of a row
onScrollToRow programmatically scroll to a
row
application code dispatching
the event
onEventButtonClicked no usage, lets users react to
button clicks
a click event on a
subheader/footer’s event button
onEventButtonClickedRequest request a confirmation dialog if
configured by the button
a click event on a
subheader/footer’s event button
onRowButtonClicked no usage, lets users react to
button clicks
a click event on a row’s event
button
onRowButtonClickedRequest request a confirmation dialog if
configured by the button
a click event on a row’s event
button
onRowClicked no usage, lets users react to
button clicks
a click event on a row
onColumnWidthsChanged resize the columns width a resize event on resize handler
between column’s headers
onDialogClosed close the dialog a close button of the dialog
onDialogConfirmed confirm the dialog a confirm button of the dialog
onMobileSearchBarToggle toggle the mobile search bar
visibility
a click event on the mobile
search button
New Filter Events
The events above include onFilterChanged, which belongs to the legacy filter. The new filter (Filter
Selector and Filter Bar, see Customizing The New Filter) dispatches its own events under the
Events.NewFilter namespace. They are also exposed as eventHandlers.newFilter on OverviewEngine.
Event Description Dispatched by
onFilterSelectorOptionsChange
d
an active filter’s options
changed in the Filter Selector
before applying
editing a filter option in the
Filter Selector
onFilterOptionsChanged the shared top-level options
changed (joinOperator or
invert)
changing the join operator or
invert toggle
onFilterSelectorAllApplied apply all active filters of the
Filter Selector
the Apply button of the Filter
Selector
onFilterItemEditApplied apply a single Filter Bar item the Apply button of a Filter Bar
item
onFilterSelectorVisibilityChang
ed
show or hide the Filter Selector opening or closing the Filter
Selector
10

-- 10 of 114 --

Event Description Dispatched by
onFilterCollapsedChanged collapse or expand a filter
group
the collapse toggle of a filter
group
onFilterItemOptionsChanged a single filter item’s options
changed
editing a filter item
onFilterItemReset reset a single filter item the reset action of a filter item
onFilterSelectorReset reset the Filter Selector the reset action of the Filter
Selector
onFilterBarReset reset the Filter Bar the reset action of the Filter Bar
onFilterBarItemsOverflowed Filter Bar items overflowed the
available width
resizing the Filter Bar
onFilterItemEditStarted editing of a Filter Bar item
started
opening a Filter Bar item editor
onFilterItemEditCanceled editing of a Filter Bar item was
canceled
closing a Filter Bar item editor
without applying
onFilterItemSettingsOpened a filter item’s settings panel
opened
opening a filter item’s settings
onFilterItemSettingsClosed a filter item’s settings panel
closed
closing a filter item’s settings
Commands
All direct state changes are coming from these actions. However, it is not recommended to listen to
these actions directly. What Commands are dispatched at what time and in what order is
considered an implementation detail and may change any time in a non-breaking way.
It is possible to register custom reducers to take care about how information is stored. But be aware
that the engine components always need the store in a special structure to be able to render!
The following table shows an overview of all Commands.
For a detailed description, including the specific payloads, please refer to the API documentation.
Commands Description State Change
setDisabled set disabled state for the engine "disabled" in "uiState"
setDialog set the dialog information "dialog" in "uiState"
setColumnWidths set the width of each table’s
column
"columnWidths" in "uiState"
setRowState set the row state "rowState" in "uiState"
setExpandedMultiSelection set expansion state of multi-
selection panel
"expandedMultiSelection" in
"uiState"
11

-- 11 of 114 --

Commands Description State Change
setQueryParameters set query parameters "searchString", "pagination",
"scrolling", "sorting",
"activeFilters" in "uiState"
setMobileSearchBar set the mobile search bar
visibility
"showMobileSearchBar" in
"uiState"
setFilterState set the filter state "newFilter" in "uiState"
setFilterOptions set the options for a specific
filter by id
filter options within "newFilter"
in "uiState"
Client Actions
The commands and events are specific to the engine and are not aware of the context where it
belongs to. This can be a problem when multiple instances of an engine are required, resulting in
conflict between instances. Therefore, the OverviewEngineActions namespace exists to allow adding
context to the core’s events and commands, e.g. activityId and other Client’s related information.
The following table shows an overview of every action. For a detailed description, including the
specific payloads, please refer to the API documentation.
Actions Description
OverviewEngineActions.createActivity Action to create an Overview Engine activity
with option to include initial UI state
OverviewEngineActions.event A wrapper action for Events while also includes
the activityId
OverviewEngineActions.command A wrapper action for Commands while also
includes the activityId
OverviewEngineActions.createEnumeratedStrin
gDataHolder
Create a data holder for an enumerated string
filter option view
OverviewEngineActions.enumeratedStringQuer
yParametersChanged
Trigger when the search input changed of the
enumerated string filter option view
OverviewEngineActions.setEnumeratedStringCa
ndidates
Trigger after receiving the data for enumerated
string filter option view
Dispatch an Engine Action
In a usual A12 application, the engine runtime will NOT be able to react if only a bare event or
command action is dispatched. Those events or commands, which can be seen on Redux Devtool,
are usually nested in a client action. This internally allows some engine’s core features set to be
seperated from being dependent on the activity.
Therefore, to control the engine’s runtime behaviors, it is necessary to be aware of this extra layer
before dispatching an action.
12

-- 12 of 114 --

dispatching-actions.ts
import { Events, Commands } from "@com.mgmtp.a12.overviewengine/overviewengine-
core/lib/main/store";
import { OverviewEngineActions } from "@com.mgmtp.a12.overviewengine/overviewengine-
core/lib/main/client-extensions";
// Middleware
const customMiddleware: Middleware = (api) => (next) => (action) => {
// ...other logics
api.dispatch(OverviewEngineActions.event({
activityId: "MY_ACTIVITY_ID",
engineAction: Events.onFilterChanged({ activeFilters: [] })
}))
}
// Saga
function* customSagaHandler(): SagaGenerator<void> {
// ...other logics
yield put(OverviewEngineActions.command({
activityId: "MY_ACTIVITY_ID",
engineAction: Commands.setDisabled({ disabled: true })
}))
}
Listen to an Engine Action
Similarly, to handle an event dispatched by the engine, it is necessary to assert the client action
before checking the event, as follows:
handle-custom-row-action-saga.ts
function* rowClickSaga(): SagaGenerator<void> {
yield* takeLatest((action: unknown) => {
return OverviewEngineActions.event.match(action) &&
Events.onRowClicked.match(action.payload.engineAction);
}, handleRowButtonClick);
}
function* handleRowButtonClick(
action:
Action<OverviewEngineActions.EventPayload<Action<Events.RowClickedPayload>>>
): SagaGenerator<void> {
const { documentId } = action.payload.engineAction.payload;
yield* put(
NotificationActions.add({
severity: "info",
duration: 5000,
13

-- 13 of 114 --

title: { key: SHOWCASE_RESOURCE_KEYS.showcase.notifications.event.title },
message: {
key:
SHOWCASE_RESOURCE_KEYS.showcase.notifications.event.documentClickMessage,
args: {
instanceId: { type: "plain", value: documentId }
}
}
})
);
}
Row
Active Row
In OverviewEngine component, the activeRowId prop is used to set a row active. The value passed to it
should be id of the corresponding document.
The following example sets the row with id="0" to be active.
Code
<OverviewEngine {...otherProps} activeRowId="0" />
Result
Figure 1. Active row example
14

-- 14 of 114 --

Row State
Overview Engine supports applying certain styles including selected, disabled and
useSecondaryColor for certain rows, with optional per-link overrides for exclude-mode duplicate
rows via byLink.
API
rowState is used to control the visual state of rows in the table. This interface is a map that allows
setting the desired styles for certain rows as below:
RowState interface
export interface RowState {
readonly [docRef: string]: {
readonly selected?: boolean;
readonly useSecondaryColor?: boolean;
readonly disabled?: boolean;
/**
* Per-linkId flag overrides for exclude-mode duplicate rows.
* When present for a given `linkId`, these flags take precedence over the
* outer-level flags for that specific `(docRef, linkId)` row.
* Hosts that do not use exclude-mode duplicates can ignore this field.
*/
readonly byLink?: {
readonly [linkId: string]: {
readonly selected?: boolean;
readonly useSecondaryColor?: boolean;
readonly disabled?: boolean;
};
};
};
}
Each key in the map is the document reference (docRef) of the document corresponding to a specific
row. The value for the key is an object holding the styles applied to that row. The object interface is
as following:
• selected: specify whether the row should be selected or not. This is related to multi-selection
feature.
• useSecondaryColor: specify if the row should have secondary color or not.
• disabled: specify if the row should be disabled or not.
• byLink: per-link overrides for exclude-mode duplicate rows; each entry supports the same flags
and takes precedence over the outer-level values for that (docRef, linkId) pair. Can be ignored
if exclude-mode duplicates are not used.
The following example sets the rows with id values of 0, 1 and 2 to be selected, disabled and
useSecondaryColor, respectively.
15

-- 15 of 114 --

This is done by dispatching Commands.setRowState to update uiState.rowState.
Code:
export const CustomMiddleWare: Middleware = (api) => (next) => (action) => {
const result = next(action);
const activityId = ActivitySelectors.latestActivity()(api.getState())?.id;
if (conditionMatched && activityId) {
api.dispatch(
OverviewEngineActions.command({
activityId,
engineAction: Commands.setRowState({
rowState: {
"DomainProduct/selected-001": { selected: true },
"DomainProduct/disabled-002": { disabled: true },
"DomainProduct/secondary-003": { useSecondaryColor: true }
}
})
})
);
}
return result;
};
Result:
Figure 2. Row state example
16

-- 16 of 114 --

Event Handlers
There are two event handlers for rows that can be registered by using eventHandlers prop:
• onRowClick is used to handle when a row is clicked. This callback receives a params object with
the following fields:
◦ documentId — document id of the row that is clicked.
◦ linkId — the link id for exclude-mode duplicate rows; undefined otherwise.
◦ customEvent — the custom event name defined via rowActivation in the Overview Model (see
Row Activation and OverviewModel.EventRowActivation).
• onRowsSelect is used to control the multi-selection feature in Overview Engine. This is triggered
when one or multiple rows are selected or deselected. It will be called with a parameter, which
is an array of objects. Each object has the following elements:
◦ documentId — document id of the row that is clicked.
◦ linkId — the link id for exclude-mode duplicate rows; undefined otherwise.
◦ selected — the next expected selection state of this row.
Row Action
Row Action Styling
Overview Engine also provides a way to apply certain styles including hidden and disabled to row
action buttons.
API
The rowActionStyling prop on OverviewEngine accepts a callback of type
OverviewEngineApi.RowActionStyling. The callback is invoked once per row per visible action and
returns the desired state for that combination — or undefined to leave the action unstyled.
The returned RowActionStyling.IndividualRowActionState controls two flags:
• hidden — hides the row action button for that row.
• disabled — disables the row action button for that row.
Examples
Disable a specific action on all rows
The following example disables the delete action on every row:
const rowActionStylingAllRows: OverviewEngineApi.RowActionStyling = ({ button }) => {
if (button.event === "delete_event") {
return { disabled: true };
}
17

-- 17 of 114 --

return undefined;
};
export const RowActionStylingExample1: React.ComponentType = () => (
<OverviewEngine {...otherProps} rowActionStyling={rowActionStylingAllRows} />
);
Disable an action on specific rows
The following example disables the delete action only for rows with document ids "0" and "2", and
also disables the edit action on row "2":
const rowActionStylingPerRow: OverviewEngineApi.RowActionStyling = ({ row, button })
=> {
if (button.event === "delete_event" && ["0", "2"].includes(row.id)) {
return { disabled: true };
}
if (button.event === "edit" && row.id === "2") {
return { disabled: true };
}
return undefined;
};
export const RowActionStylingExample2: React.ComponentType = () => (
<OverviewEngine {...otherProps} rowActionStyling={rowActionStylingPerRow} />
);
Result (disable on all rows):
18

-- 18 of 114 --

Figure 3. Styling actions on all rows
Result (disable on specific rows):
Figure 4. Styling actions on specific rows
NOTE
Deprecated: rowActionState prop
rowActionState / OverviewEngineApi.RowActionState are deprecated since 2026.06 —
use rowActionStyling instead. They remain functional for backwards compatibility:
rowActionStyling takes precedence per flag; only when it returns undefined for a flag
does the engine fall back to rowActionState. The two props can coexist safely during
migration.
19

-- 19 of 114 --

Event Handlers
The on-click event of a row action can be registered by onRowButtonClick in eventHandlers of
OverviewEngine. The callback receives a params object with the following fields:
• documentId — document id of the row on which the action is clicked.
• linkId — the link id for exclude-mode duplicate rows; undefined otherwise.
• rowActionModel — configuration of the clicked row action, which has type Button.
Row Actions in Right-Click Context Menu
Overview Engine also provides a built-in feature that helps end users interact with row actions
easily by right-clicking on the row, instead of using row action buttons.
Figure 5. Context menu by right clicking
Button
Buttons, defined in an Overview Model, are displayed in the header, footer or multi-selection panel
of the Overview Engine. However, adding buttons in the Overview Model only renders them on the
UI without registering any handler for on-click events.
Overview Engine supports registering the handlers by passing a callback into onEventButtonClick
of eventHandlers prop. When triggered, the callback receives two parameters:
• event — event name of the button being clicked.
• button — configuration of the clicked Button.
NOTE onEventButtonClick is used to register handlers for any buttons no matter if they are
on header, footer or multi-selection panel
Searching
The API for searching in Overview Engine gives you full control over the functionality. To
implement search:
• uiState.searchString is used to specify the current search value.
• eventHandlers.onSearch callback inside the eventHandlers prop is triggered when focusing is on
20

-- 20 of 114 --

search input and Enter key is pressed or search button is clicked. It can also be done via the
OverviewEngineActions.event, more details in the Commands and Events section.
NOTE
Overview Engine performs a plain text string-based full text search using the
Simple Search operator from Data Services. For further information, refer to the
Data Services documentation.
Field-Based Filtering
Overview Engine is equipped with a feature to search for documents based on field values. This
feature can be activated or deactivated in the Overview Model, details in Overview Modelling
documentation - Enable Filter.
Visualization of Filtering Feature
By default, there is a small button appears next to the Search input component: the Filter Button.
Figure 6. Filter Button
Click on this button to open the Filter Selector dialog, which shows the list of fields to configure the
filter parameters. The Apply button inside Filter Selector is responsible for triggering the filtering
process, then the Filter Selector disappears and the Filter Bar is shown to display what is being
filtered.
Figure 7. Visualization of Filtering Feature
Configure the Overview Model to show/hide the Filter Button/Filter Bar, see more details in
Overview Modelling documentation - Filter Configuration.
21

-- 21 of 114 --

Filter Selector
The left side of the dialog is a list of fields from the Document Model, a list of columns, or a custom
list. It is configured by the Filter Mode in the Overview Model. Use Filter Search input to quickly
find a field.
The list of fields can be grouped by sections. In the image below, the Number section contains fields
related to "number". It is easy for end users to find relevant fields to apply filters. Read Overview
Modelling documentation - Section Data to know how to group fields as sections.
Figure 8. Filter selector
There is a checkbox inside each item of the list, which is used to mark the field to apply the filter.
When a field is checked, it becomes an active filter.
22

-- 22 of 114 --

Filter Option
When a field is selected, the right side of the dialog shows its Filter Option. It can be a simple input;
a combination of two inputs, a range of start and end values; or a list of options. The visualization
of the Filter Option is based on field data type, see more details in supported data types. Below is a
Filter Option for data type Number.
Figure 9. Filter option
If a filter option is set, it is automatically recognized as an active filter.
Apply Button
The Apply button is below the Filter Option. It triggers the filtering process for all active filters of
the Filter Selector, not only the displaying Filter Option.
23

-- 23 of 114 --

Filter Bar
The Filter Bar is presented as a group of many small boxes below the Subheader of the Overview
Engine. Each box summarizes which field is being filtered with its parameters, and a delete button
to remove the corresponding filter.
Figure 10. Filter bar
24

-- 24 of 114 --

Filter Option
Click to a box in the Filter Bar will open a Filter Option, which is similar to the Filter Option in the
Filter Selector.
Apply Button
The Apply button in the Filter Option of the Filter Bar is responsible for applying the filter for the
corresponding field only. The other active filters are still kept.
Close the Filter Option without clicking the Apply button will discard the change for the
corresponding filter.
Supported Data Types
String
Filter Normal String Fields
By default, the string filter options are visualized as a simple input for entering the filter value. The
string filter is an object with a string value.
Beside the input, there is a switch to search for Empty values.
25

-- 25 of 114 --

Figure 11. String Filter Option
26

-- 26 of 114 --

There are three behaviors to perform filtering on string fields: Exact match search and Simple
search. The default behavior is exact match search.
• An Exact match search looks for the exact value provided for the current string field, with or
without case sensitivity. Substring or partial matching is not available.
• The Simple search uses a case-insensitive substring match algorithm to search the current
string field.
• The Undefined match looks for any empty value in the current string field.
To enable simple search, find the string field in the Document Model and add the
enable_approximate_match_search annotation with the value true. When filtering, the keyword
will be split into words by whitespace and connected using the and operator. For example, if the
keyword is Tennisball Pack, it will be split into Tennisball and Pack. Below is the operator in the
query constraint that will be sent to the server:
Splitted keyword example
{
"operator": "and",
"operands": [
{
"operator": "simple_search",
"fields": [
"/product/name"
],
"value": "Tennisball"
},
{
"operator": "simple_search",
"fields": [
"/product/name"
],
"value": "Pack"
}
]
}
The query with this operator will look for documents containing both Tennisball and Pack in the
/product/name field.
If the enable_approximate_match_search annotation is not specified or explicitly set to false,
exact match search is applied by default. The default behavior for exact match search is
caseSensitive true. To enable case-insensitive search (caseSensitive = false), add the
enable_case_insensitive_search annotation with the value true into the string element in the
Document Model.
Filter String Fields with Multi-Select
When Filter String Fields with Multi-Select is enabled, the string filter options are shown as a list of
27

-- 27 of 114 --

options like multi-select or enumeration.
A select option is available to search for Empty values, which can be combined with other string
value selections.
28

-- 28 of 114 --

Figure 12. String Filter Option as Multi-Select
29

-- 29 of 114 --

The behavior for filtering String Fields with Multi-Select is based on exact match search with
caseSensitive true.
With Empty option selected, it looks for any empty value in the current string field with the
undefined match operator.
If multiple options are selected, the values are connected using the or operator.
Number
The number filter is an object with a start and an end value, both of type number.
The number filter options are visualized by default as two inputs, one for the start and one for the
end value.
Beside the two inputs, there is a switch to search for Empty values.
30

-- 30 of 114 --

Figure 13. Number Filter Option
31

-- 31 of 114 --

If Empty switch is turned on, the result filter would look for any empty value in the current number
field.
If one of the two values is filled:
• The value is entered into start field: the result filter would be larger than or equal to the value.
• The value is entered into end field: the result filter would be less than or equal to the enter
value.
It is not possible to enter a number range with the start value larger than the end value.
Boolean
The boolean filter is an object with just the value of type boolean.
The boolean filter options are visualized by default as three radio buttons:
• Yes for true
• No for false
• Empty for null
Figure 14. Boolean Filter Option
Confirm
The confirm filter is similar to the boolean filter, but with three radio button options: Yes and
Empty.
32

-- 32 of 114 --

Figure 15. Confirm Filter Option
Enumeration
The enumeration filter is an object with the property selectedValues, which is an array of string
values corresponding to the values of the selected enumeration options.
The enumeration filter options are visualized by default as a checkbox list of the enumeration
options with localized labels.
A select option is included to search for Empty values, which can be combined with other
enumeration value selections.
33

-- 33 of 114 --

Figure 16. Enumeration Filter Option
Multi-Select
The multi-select filter is an object that have two properties:
• selectedValues: contains multi-select value selected by users. It is an array of string values
corresponding to the values of the enumeration field value inside the multi-select group.
• operation: the operator that is supposed to be used with selectedValues. It is of type
FilterOperation enum, which consists of two values AND and OR.
The multi-select filter options are visualized similarly to the enumeration filter, but with a Filter
Operation button to set the operation. By default, the operation is AND.
34

-- 34 of 114 --

A select option is available to search for Empty values, which can be combined with other multi-
select value selections.
Figure 17. Multi-Select Filter Option
Date
The date filter is an object with a start and an end value, both of type Date.
The date filter options are visualized by default as two inputs, one for the start and one for the end
value. The start and end value are similar to the Number Filter Option, but the input is a date
picker, or a combination of select elements.
35

-- 35 of 114 --

Figure 18. Date Filter Option - By Date Range
It is possible to filter by Month & Year Range (skipping the exact day) or only by Year Range
(skipping the exact day and month). Use the select element on top of the Date Filter Option to
choose relevant mode. The default mode is Date Range. An Empty option is also available in this
select dropdown to search for empty values.
36

-- 36 of 114 --

Figure 19. Date Filter Option - By Month & Year Range
37

-- 37 of 114 --

Figure 20. Date Filter Option - By Year Range
The resulting dates or date ranges are set as:
• Start value: first day of the month / year.
• End value: last day of the month / year.
38

-- 38 of 114 --

Figure 21. Date Filter Option - Empty Values
No input is required for this option.
Time
The time filter visualization is similar to the number filter option, but the input is a time picker.
Beside the two inputs, there is a switch on top to search for Empty values.
39

-- 39 of 114 --

Figure 22. Time Filter Option
DateTime
The date time filter is similar to the date filter, with the difference, that the user can specify also the
time here.
40

-- 40 of 114 --

Figure 23. Date Time Filter Option
There are 6 convenient input modes:
• Date Range
• Date & Time Range
• Time Range
• Month & Year Range
• Year Range
• Empty
The Date Range, Month & Year Range, and Year Range are similar to the Date Filter Option. The time
of the start field is set as 00:00:00, and the time of the end field is set as 23:59:59.
41

-- 41 of 114 --

• Date & Time Range: inputs are datetime pickers, both date and time can be set.
• Time Range: similar to Time Filter Option, but the date is set to today.
Hiding Empty Value Options
All filter option views provide an Empty switch or option to match undefined values. If you want to
remove this choice from the UI, pass hideEmptyValueOption to the corresponding filter option view in
the componentMap.
This flag is supported by the built-in views for String, Number, Boolean, Confirm, Enumeration,
Multi-Select, Date, Time, and DateTime filters.
Hide the Empty option in filter views
const componentMap = {
...DefaultComponentMap,
FilterOptionsViews: {
...DefaultComponentMap.FilterOptionsViews,
StringFilterOptionsView: (props) => (
<DefaultComponentMap.FilterOptionsViews.StringFilterOptionsView
{...props}
hideEmptyValueOption
/>
),
DateFilterOptionsView: (props) => (
<DefaultComponentMap.FilterOptionsViews.DateFilterOptionsView
{...props}
hideEmptyValueOption
/>
)
}
};
How Field-Based Filtering Works
After clicking the Apply button, the onFilterChange event is called with a FilterMap containing an
updated active filters. By default, this event will dispatch the
OverviewEngine/EVENT/onFilterChanged action and the activeFilters is kept in uiState.
NOTE
Each entry in the FilterMap can specify a modelId property, which indicates the
model (main document model or a submodel) where the field to be filtered resides.
When filterMode is "custom_list", use the subModel property in FieldConfiguration
to filter a field in a submodel. This will result in the corresponding modelId in the
filter data. If subModel is omitted, filtering applies to the current document model.
This mechanism is related to the heterogeneity feature in A12, allowing filters to
target fields across different models and submodels.
42

-- 42 of 114 --

Then the default data provider (by OverviewEngineFactories) will use the activeFilters to create
and send a search query to the server, which provides the documents displayed in the Overview
Engine.
Controlling The Active Filters
Overview Engine supports preset filters, which are active filters that are set when the engine is
initialized. Depending on requirements, modeler and developer can work together to define
suitable preset filters.
• In Overview Engine, the developer can set whether filters are removable or non-removable.
• In Overview Model, the modeler can set whether the Filter Bar and Filter Button should be
displayed.
• If the Overview Model references a query model, its constraint behaves like a preset filter,
always applies alongside the active filters, is joined with them using the AND operator, and stays
hidden from the Filter Selector or Filter Bar.
Figure 24. Preset Filters with removable and non-removable filters
To build preset filters like that, Overview Engine needs to be initialized with an activeFilters list of
type FilterMap and passed to the uiState of Overview Engine activity slices. This can be done by
registering a custom middleware in a module or in the application setup that intercepts
Activity.PUSH actions and re-dispatches them with an initial filter configuration. For example:
Create the preset filters
const presetFilter: OverviewEngineApi.FilterMap = {
"/product/name": {
filterType: "String",
criteria: {
43

-- 43 of 114 --

value: "board"
},
nonRemovable: true // set nonRemovable = true if you want this filter cannot
be removed
},
"/product/inStock": {
filterType: "Boolean",
criteria: {
value: true
},
nonRemovable: true
},
"/product/number": {
filterType: "Number",
criteria: {
start: 100
}
},
"/product/logistics/weight/weightValue": {
filterType: "Number"
},
"/product/targetGroup": {
filterType: "Enumeration",
criteria: {
selectedValues: ["women", "men"]
}
},
"/product/dateField": {
filterType: "Date",
type: "Date",
criteria: {
end: new Date()
}
}
};
Example Middleware
import { isEqual } from "lodash-es";
import type { Middleware } from "redux";
import { type Activity, ActivityActions } from "@com.mgmtp.a12.client/client-core";
import {
type UiState,
OverviewEngineActions,
type OverviewEngineApi
} from "@com.mgmtp.a12.overviewengine/overviewengine-core";
export const createInitialUiStateMiddleware: (
initialUiState: UiState,
44

-- 44 of 114 --

targetDescriptor: Activity.Descriptor
) => Middleware = (initialUiState, targetDescriptor) => () => (next) => (action) => {
if (ActivityActions.push.match(action)) {
const { activity } = action.payload;
if (!isTargetOverviewActivity(activity, targetDescriptor) ||
!isSliceEmpty(activity)) {
return next(action);
}
return next(
OverviewEngineActions.createActivity(
{
activityId: activity.id,
activityDescriptor: activity.descriptor
},
initialUiState
)
);
}
return next(action);
};
export const createPresetFilterMiddleware = (
presetFilter: OverviewEngineApi.FilterMap,
targetDescriptor: Activity.Descriptor
): Middleware => createInitialUiStateMiddleware({ activeFilters: presetFilter },
targetDescriptor);
function isTargetOverviewActivity(activity: Activity, targetDescriptor:
Activity.Descriptor) {
return isEqual(activity.descriptor, targetDescriptor);
}
function isSliceEmpty(activity: Activity) {
return Object.keys(activity.dataHolders?.[0].slices ?? {}).length === 0;
}
In FilterMap, the key is the string based on ModelPath of the field, for example: "/product/name",
"/product/number",… The value is a Filter.Options with filterType, criteria and nonRemovable. If
nonRemovable is set, this filter cannot be removed from either Filter Bar or Filter Selector.
The middleware example above is only one way to create preset filters at activity creation time. If
you need to update the active filters during normal usage of the activity, dispatch an engine action
instead:
• Use the event Events.onFilterChanged when you want to apply a new activeFilters list through
the standard filtering flow.
• Use the command Commands.setQueryParameters when you want to update activeFilters together
45

-- 45 of 114 --

with other query parameters such as search, pagination, scrolling, or sorting.
Remember to wrap these actions with OverviewEngineActions.event or
OverviewEngineActions.command so the update is scoped to the correct activity. See Dispatch an
Engine Action for the dispatching pattern.
Enumerated String Filtering
Enable Filter String Fields with Multi-Select before using this feature.
This section will only focus on the customization in searching.
Event Handlers
The search enumerated string filter is handled by onSearchEnumeratedStringField in
eventHandlers. By default this event will dispatch the enumeratedStringQueryParametersChanged
action. Then the default sagas provided by OverviewEngineFactories will handle this action to load
list candidates and keep them in uiState.enumeratedStringFilterMap.
A customization could be done by overriding the event onSearchEnumeratedStringField. The
following steps must be followed:
• Create data holders for uiState.enumeratedStringFilterMap correctly by dispatching action
createEnumeratedStringDataHolder. Each field has a separated data holder, which contains the
list of candidates and the search value.
• Dispatch the action setEnumeratedStringCandidates to update the list of candidates into these
data holders.
Customization Example
The below example illustrates how to customize the search enumerated string filter:
export const EnumerationStringFilterExample = (props:
OverviewEngineFactories.ViewComponentProps) => {
const { activityId } = props;
const dispatch = useDispatch();
const eventHandlers: OverviewEngineApi.EventHandlers = React.useMemo(() => {
return {
onSearchEnumeratedStringField(params: { fieldPath: string; keyword?:
string; nextPage?: boolean }) {
const { fieldPath, keyword = "", nextPage = false } = params;
if (fieldPath === "product.externalNumber.system") {
dispatch(
customEnumeratedStringQuery({
activityId,
fieldPath,
keyword,
nextPage
46

-- 46 of 114 --

})
);
} else {
dispatch(
OverviewEngineActions.enumeratedStringQueryParametersChanged({
activityId,
fieldPath,
keyword,
nextPage
})
);
}
}
};
}, [activityId, dispatch]);
return <OverviewEngineFactories.ViewComponent {...props}
eventHandlers={eventHandlers} />;
};
export const customEnumeratedStringQuery =
factory<OverviewEngineActions.EnumeratedStringQueryParametersChangedPayload>(
"CUSTOM_ENUMERATED_STRING_QUERY"
);
The customEnumeratedStringQuery action should be handled by a saga:
function* customEnumeratedStringSearchingSaga(): SagaGenerator<void> {
yield* takeLatest((action: unknown) => {
return customEnumeratedStringQuery.match(action);
}, handleCustomEnumeratedStringSearching);
}
function* handleCustomEnumeratedStringSearching(
action:
Action<OverviewEngineActions.EnumeratedStringQueryParametersChangedPayload>
): SagaGenerator<void> {
const { activityId, fieldPath, keyword, nextPage } = action.payload;
const activity = yield* select(ActivitySelectors.activityById(activityId));
if (!activity) {
throw new Error(`No activity found for id ${activityId}.`);
}
// Find data holder for enumeratedStringFilterMap by current fieldPath
const dataHolder = activity.dataHolders?.find(
(dataHolder) => EnumeratedStringDataHolder.isInstance(dataHolder) &&
dataHolder.descriptor.fieldPath === fieldPath
);
if (!dataHolder) {
47

-- 47 of 114 --

// Create data holder if not exists
yield* put(
OverviewEngineActions.createEnumeratedStringDataHolder({
data: { fieldPath, keyword, candidates: [] },
activityId,
descriptor: EnumeratedStringDataHolder.createDescriptor(fieldPath)
})
);
} else {
// clean data holder before updating, no need to do it if using
enumeratedStringQueryParametersChanged
yield* put(
OverviewEngineActions.setEnumeratedStringCandidates({
activityId,
fieldPath,
fullSize: 0,
candidates: []
})
);
}
// Get list of candidates and ful size by keyword.
const { candidates: newCandidates, fullSize } = yield* call(requestCandidates,
fieldPath, keyword, nextPage);
// Update the candidates and fullSize
yield* put(
OverviewEngineActions.setEnumeratedStringCandidates({
activityId,
fieldPath,
fullSize,
candidates: newCandidates
})
);
}
async function requestCandidates(
fieldPath: string,
keyword: string,
nextPage: boolean
): Promise<{ candidates: string[]; fullSize: number }> {
const fullCandidates = ["barcode", "customized system number"];
const candidatesByKeyword = fullCandidates.filter((candidate) =>
candidate.includes(keyword));
return Promise.resolve({ candidates: !nextPage ? candidatesByKeyword : [],
fullSize: candidatesByKeyword.length });
}
Remember to register this saga for current module.
48

-- 48 of 114 --

Result Page Customization
When there is no data for a given query (including searching and/or filtering), Overview Engine
will show the default message "No results found" for clarification. It is straightforward to customize
by overriding the default TableBody component, e.g:
Example
import { DefaultTableComponentRenderers } from "@com.mgmtp.a12.widgets/widgets-core";
import {
type TableBody,
OverviewEngine,
DefaultComponentMap,
useOverviewEngineContext
} from "@com.mgmtp.a12.overviewengine/overviewengine-core";
const CustomTableBody: React.ComponentType<TableBody.Props> = (props) => {
const data = useOverviewEngineContext((context) => context.data);
if (data.length === 0) {
return <div className="-u-flex -u-justify-center">No search results. Try again
with another query</div>;
}
return <>{DefaultTableComponentRenderers.bodyRenderer(props)}</>;
};
export const CustomOverviewEngineContainer: React.ComponentType<OverviewEngine.Props>
= (engineProps) => {
return <OverviewEngine {...engineProps} componentMap={{ ...DefaultComponentMap,
TableBody: CustomTableBody }} />;
};
Customizing The New Filter
The Filter Selector and Filter Bar described above are part of the new filter. Beyond the model-
driven configuration, the new filter exposes two programmatic customization points on
OverviewEngine: the componentMap.newFilter for replacing UI, and the eventHandlers.newFilter for
intercepting filter interactions.
Component Overrides
Every component the new filter renders is read from componentMap.newFilter, a
NewFilterComponentMap. This works like the rest of the componentMap: override only the keys
you need and fall back to DefaultComponentMap.newFilter for the rest.
The override points are grouped by responsibility:
• Layout / containers — FilterSelector, FilterBar, FilterSelectorTriggerButton,
FilterSelectorFooter, FilterSelectorSearchBar, FilterSelectorSetting, OverviewHeading,
49

-- 49 of 114 --

OverviewSubheaderBox, SubHeader.
• Filter Bar items — FilterBarItem, FilterBarItemDropdown.
• Per-type editors — the input shown when a filter is active: StringFilterEditor,
NumberFilterEditor, BooleanFilterEditor, ConfirmFilterEditor, EnumerationFilterEditor,
MultiSelectFilterEditor, DateFilterEditor, DateFragmentFilterEditor, DateRangeFilterEditor,
DateTimeFilterEditor, TimeFilterEditor, QueryFilterEditor, plus the abstract FilterEditor
wrapper and the RangeFilterEditorTemplate shared by range types.
• Per-type settings — the per-filter settings panel: StringFilterSetting, NumberFilterSetting,
BooleanFilterSetting, ConfirmFilterSetting, EnumerationFilterSetting, MultiSelectFilterSetting,
DateFilterSetting, DateFragmentFilterSetting, DateRangeFilterSetting, DateTimeFilterSetting,
TimeFilterSetting, plus the abstract FilterSetting wrapper.
• Actions / misc — FilterResetButton, FilterSettingButton, EmptyFilter.
See NewFilterComponentMap for the full list and the prop type of each component.
Override a new filter component
const componentMap = {
...DefaultComponentMap,
newFilter: {
...DefaultComponentMap.newFilter,
StringFilterEditor: (props) => (
<DefaultComponentMap.newFilter.StringFilterEditor {...props} />
)
}
};
Event Handlers
The new filter reports each interaction through the newFilter group of
OverviewEngineApi.EventHandlers. Provide handlers under eventHandlers.newFilter to react to, or
override, the default behavior. All handlers are optional.
• onFilterSelectorOptionsChanged — an active filter’s options changed inside the Filter Selector
(before Apply).
• onFilterOptionsChanged — the shared top-level options changed (e.g. joinOperator or invert).
• onFilterSelectorAllApplied — the Apply button in the Filter Selector was pressed (applies all
active filters).
• onFilterItemEditApplied — the Apply button of a single Filter Bar item was pressed.
• onFilterSelectorVisibilityChanged — the Filter Selector was shown or hidden.
• onFilterCollapsedChanged — a filter group was collapsed or expanded.
• onFilterItemOptionsChanged — a single filter item’s options changed.
• onFilterItemReset — a single filter item was reset.
• onFilterSelectorReset / onFilterBarReset — the Filter Selector or Filter Bar was reset.
50

-- 50 of 114 --

• onFilterBarItemsOverflowed — dispatched when the Filter Bar does not have enough width to
render all of its filter items. The handler receives the items that did not fit, and by default the
engine moves these overflowed items into the Filter Selector so that they remain accessible.
• onFilterItemEditStarted / onFilterItemEditCanceled — editing of a Filter Bar item started or was
canceled.
• onFilterItemSettingsOpened / onFilterItemSettingsClosed — a filter item’s settings panel was
opened or closed.
Each handler corresponds to an action in the Events.NewFilter namespace. To react to these events
outside of eventHandlers (for example in a custom middleware or saga), see New Filter Events and
the dispatch / listen patterns.
Sorting
In Overview Engine, sorting is managed through state for consistent and customizable control:
• uiState.sorting: to define the current sorting state. Note that Overview Engine only displays the
sort icon (↑ or ↓) for the first item in sorting array.
• eventHandlers.onColumnClick callback: to subscribe to the change event of sorting state. This
will be triggered when the column header is clicked.
NOTE
• Because the initial sorting state can be defined in Overview Model, the initial
sorting passed to OverviewEngine should be based on what is defined in
Overview Model. OverviewEngineApi.Sorting.getInitialValue is provided to get
the initial sorting state from Overview Model.
• If no default sorting is specified, results are sorted by __meta/createdAt in
descending order.
• Case is ignored during sorting (ignoreCase is set to true).
Multi-Selection
Overview Engine provides a multi-selection feature that allows users to select multiple rows and
perform actions on those selected rows.
The multi-selection area is a dedicated part of each row that users can click to toggle the row’s
selection state. It is configurable through the model and supports the following options:
• Rows and checkboxes (default) — both row clicks and checkboxes can toggle selection.
• Checkboxes only — selection is managed exclusively via checkboxes.
Users can select rows in two ways:
• Single selection: Click on a row’s selection area to toggle its selection.
• Range selection: Hold Shift and click another row’s selection area to select a consecutive range
of rows.
51

-- 51 of 114 --

NOTE Range selection via Shift+Click is not supported when infinite scrolling is
enabled.
To enable it, first it is required to turn on the feature in the model. Please see the SME docs for more
details about modelling it. This section will only focus on the implementation aspect.
API
As mentioned before, one or multiple rows can be marked as selected by using row state and listen
to the event when one or multiple rows is selected by using onRowsSelect.
Besides, there are two other event handlers related to multi-selection:
• onMultiSelectionClear: This will be triggered when the selections are supposed to be cleared.
Specifically, it is called when the filtering or searching changes or the multi-selection panel
collapses while there are still selected rows.
• onOverallMultiSelectionButtonClick: will be triggered when the overall multi-selection is
clicked. It will be called with a parameter which is an object with two elements:
◦ affectedRowIds — document ids of the rows that are affected.
◦ selected — indicate whether these rows are selected or deselected.
Delete Multiple Documents
It is possible to delete multi-selected documents by modeling a button on multi-selection panel, see
SME docs, with the event name of delete_selected. This feature only works with user roles that
have the access right of DOCUMENT_MULTI_DELETE, see Data Services docs.
Infinite Scroll
Overview Engine also supports infinite-scroll behavior. When this feature is enabled, the table will
load more rows as user keeps scrolling down. This is an alternative to pagination in Overview
Engine and can bring better UI/UX in some particular cases.
NOTE The infinite-scroll feature only supports fixed height rows
To enable infinite-scroll in Overview Engine, it should be specified in the model and configured in
OverviewEngine component through public APIs. For more details about modelling, please see the
SME docs. This section only focuses on how to use the APIs.
NOTE
Since the height of the table body is fixed in infinite scrolling tables, the footer is
always stuck at the bottom of the table body, even if the table rows are short. This is
a non-covered edge case.
52

-- 52 of 114 --

Figure 25. Footer at the bottom of the infinite table, leaving a space between it and body rows
API
To enable infinite-scrolling, the props passed to OverviewEngine component should be of the type
OverviewEngine.InfiniteScrollProps. This means that data prop can receive a discontinuous array
and infiniteScrollOptions must be specified.
The infiniteScrollOptions prop is used for controlling the behavior. It receives an object of type
OverviewEngineApi.InfiniteScrollOptions which is similar to InfiniteScrollOptions in Widget.
OverviewEngineApi.InfiniteScrollOptions has the following fields:
• rowCount: receives the total number of rows.
• rowLoadingStatus: receives a callback to identify if a row is unloaded or loading or loaded. The
callback takes the index of a row and should return RowLoadingStatus for the row.The callback
receives the index of a row and should return the corresponding RowLoadingStatus.
• loadData: a callback that loads and updates data by creating a
DataOperation.ListDocuments.Query with the appropriate DataOperation.ListDocuments.Paging
parameter. This callback will be triggered when more data need to be loaded. It receives an
object parameter which contains the following fields:
◦ startPage: indicating the number of page to start loading. This is 0-based.
◦ endPage: indicating the number of page (inclusive) to end loading. This is 0-based.
• threshold: to specify when to pre-fetch data. A threshold X means that the next rows will start
loading when a user scrolls within X last rows
• minimumBatchSize: minimum number of rows to be loaded at a time
• loaderRef: the React reference to the underlying InfiniteLoader from react-virtualized. It has a
useful method resetLoadMoreRowsCache to reset the internal cache. This should be called when all
the rows should be re-fetched.
53

-- 53 of 114 --

• overrideListProps: to override the props of react-virtualized list rendered under the hood.
There are some useful props such as scrollToRow and so on.
Configurations
Overview Engine provides configurations for the infinite scrolling behavior through
OverviewEngineFactories.
These configurations control how infinite scrolling operates in the OverviewEngine:
• pageSize: Defines the number of rows to load per page. A larger pageSize reduces the number of
requests but increases the amount of data loaded at once.
• cachePages: Specifies the number of pages to retain in the cache while scrolling. This optimizes
performance by reducing the need to re-fetch data when scrolling back. The default value is 5.
NOTE
OverviewEngine calculation
The number of documents per one load by pageSize * ( endPage - startPage + 1 ).
The total number of cached rows is calculated as pageSize * cachePages.
Setup the Overview Engine infinite scroll configurations
import { DirtyHandlingFactories } from "@com.mgmtp.a12.client/client-
core/dirtyHandling";
import { OverviewEngineFactories } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
import { ApplicationFactories, type ApplicationSetup, ModuleRegistryProvider } from
"@com.mgmtp.a12.client/client-core";
export function setup(): ApplicationSetup {
ModuleRegistryProvider.getInstance().addModule(
OverviewEngineFactories.createModule({ infiniteScroll: { pageSize: 50,
cachePages: 4 } })
);
return ApplicationFactories.createApplicationSetup({
...otherConfigurations,
overridePlatformSagas: [
...OverviewEngineFactories.createApplicationSagas(),
...DirtyHandlingFactories.createSagas()
]
});
}
Localization
An application using Overview Engine has to provide a LocalizerContext instance from util-
localization-react package.
54

-- 54 of 114 --

The context can be initialized by using DefaultLocalizerContextProvider, or customized completely
by passing three parameters: localizer, locale, and dataFormats into LocalizerContext.
localizer receives an array of Localizables and returns resolved string in regard to current locale.
For more details, see Utils localization documentation.
API
Overview Engine offers two alternative localization approaches:
• In which the React hooks are available, LocalizerHooks namespace with five different hooks
can be used:
◦ useLocalizedResource: to translate Overview Engine resources via public RESOURCE_KEYS.
◦ useLocalizedOverviewElement: to translate localized texts in Overview Model’s elements.
◦ useLocalizedFieldLabel: to translate field labels in the Document Model.
◦ useLocalizedFieldValue: to translate values of localizable fields (BooleanType, ConfirmType,
EnumerationType).
◦ useLocalizedDateFieldFormat: to translate the date format string of a date field in the
Document Model.
• In which the React hooks are not available, LocalizableFactory namespace with alternative
functions can be used to create corresponding localizables.
Resource Keys
Resource keys are used to identify the document elements and UI elements that are localized. There
are two kinds of resource keys: static resource keys and model element keys.
Static Resource Keys
Static resource keys are fixed and not changed. For example, the labels for Confirm and Cancel in
confirmation dialog or input labels in filter option views are identified by static resource keys.
These keys are provided in the constant RESOURCE_KEYS map and exported from services/localization.
Model Element Keys
Model element keys are dynamically generated based on the model being used. The following table
summarizes the key formats:
Element Key
Table header label uiModel.{overview-model-name}.header.label
Column header label uiModel.{overview-model-name}.columns.{column-id}.label
Number column suffix uiModel.{overview-model-name}.columns.{column-id}.suffix
Sub-header action uiModel.{overview-model-name}.subHeaderBox.actions.{button-
event}.(label|title)
55

-- 55 of 114 --

Element Key
Sub-header action
confirmation
uiModel.{overview-model-name}.subHeaderBox.actions.{button-
event}.confirmation.(title|message)
Footer action uiModel.{overview-model-name}.footerBox.actions.{button-
event}.(label|title)
Footer action
confirmation
uiModel.{overview-model-name}.footerBox.actions.{button-
event}.confirmation.(title|message)
Row action uiModel.{overview-model-name}.rowActionGroup.actions.{action-
event}.(label|title)
Context menu action uiModel.{overview-model-name}.contextMenu.actions.{action-
event}.(label|title)
Multi-selection action uiModel.{overview-model-name}.multiSelection.actions.{action-
event}.(label|title)
Multi-selection action
confirmation
uiModel.{overview-model-name}.multiSelection.actions.{button-
event}.confirmation.(title|message)
Multi-selection clear
confirmation
uiModel.{overview-model-
name}.multiSelection.clearConfirmation.(title|message)
Row action
confirmation
uiModel.{overview-model-name}.rowActionGroup.actions.{action-
event}.confirmation.(title|message)
Context action
confirmation
uiModel.{overview-model-name}.contextMenu.actions.{action-
event}.confirmation.(title|message)
Action group title uiModel.{overview-model-name}.contextMenu.groups.{group-name}
Filter selector Other
section label
uiModel.{overview-model-name}.filterSelector.section.other
Customization
The following snippet demonstrates the way to modify a label of row action confirmation dialog in
American English.
Localization Customization
import { LocalizerContext } from "@com.mgmtp.a12.utils/utils-localization-react";
import { OverviewEngine } from "@com.mgmtp.a12.overviewengine/overviewengine-core";
import {
type Locale,
defaultDataFormats,
defaultValueConversion,
defaultLocalizerFactory
} from "@com.mgmtp.a12.utils/utils-localization";
export const Application: React.ComponentType<{ engineProps: OverviewEngine.Props }> =
({ engineProps }) => {
const localizerContextValue = React.useMemo(() => {
const customResourceKeys = { overviewEngine: { rowAction: { confirmation: {
ok: "Confirm" } } } };
56

-- 56 of 114 --

const locale: Locale = { language: "en", country: "US" };
const dataFormats = defaultDataFormats(locale);
const conversion = defaultValueConversion(dataFormats);
const localizer = defaultLocalizerFactory({
locale,
dataFormats,
conversion,
translationSource: { en_US: customResourceKeys }
});
return { locale, dataFormats, localizer, conversion };
}, []);
return (
<LocalizerContext.Provider value={localizerContextValue}>
<OverviewEngine {...engineProps} />
</LocalizerContext.Provider>
);
};
Conversion
The Overview Engine utilizes the Conversion API from the utils-localization package to handle
value conversions between document data and rendered UI values, considering the current locale
and data formats.
The utils-localization ValueConversion interface has two methods:
• parseValue: converts the rendered UI value to the document value.
• formatValue: converts the document value to the rendered UI value.
When rendering a document value, the Overview Engine calls the formatValue method of the
contextual conversion, which implements the ValueConversion interface and is extracted from
LocalizerContext. This method is provided with the value to be converted, along with relevant
information such as the document model name and the model path to the field or group. Depending
on the element type, additional information may be passed, such as minFractionalDigits for
NumberType elements and format for DateType elements.
Similarly, when parsing a value, the parseValue method is called with the similar parameters.
For more details, please refer to the utils-localization documentation.
To customize the conversion logic, you can provide a custom implementation of the Conversion
API. For example, to customize the format of a DateType field named DateOfBirth in the PersonDM
document model:
57

-- 57 of 114 --

Conversion customization
import * as React from "react";
import { format } from "date-fns/format";
import { ModelPath } from "@com.mgmtp.a12.base/base-model-api";
import { convertMomentToDateFnsFormat } from "@com.mgmtp.a12.widgets/widgets-core";
import { LocalizerContext, type LocalizerContextProps } from
"@com.mgmtp.a12.utils/utils-localization-react";
import {
defaultDataFormats,
type ValueConversion,
defaultValueConversion,
defaultLocalizerFactory
} from "@com.mgmtp.a12.utils/utils-localization";
export const LocalizationProvider: React.FC = () => {
const locale = useProjectLocale();
const localizerContextValue: LocalizerContextProps = React.useMemo(() => {
const dataFormats = defaultDataFormats(locale);
const defaultConversion = defaultValueConversion(dataFormats);
const conversion: ValueConversion = {
...defaultConversion,
formatValue(value, outputFormat) {
if (
value instanceof Date &&
outputFormat.modelId === "PersonDM" &&
outputFormat.modelPath &&
ModelPath.equal(outputFormat.modelPath, targetFieldPath)
) {
// below is a date format using moment's format, to use with date-
fns we can use the widgets utility
return format(value, convertMomentToDateFnsFormat("dd MMM YYYY"));
}
return defaultConversion.formatValue(value, outputFormat);
}
};
const localizer = defaultLocalizerFactory({ locale, conversion, dataFormats
});
return { locale, dataFormats, conversion, localizer };
}, [locale]);
return <LocalizerContext.Provider value={localizerContextValue} />;
};
58

-- 58 of 114 --

Display Modes
Apart from the default look, Overview Engine can also be displayed in two other forms: cardView
and embedded.
Card View
Once the card view mode is enabled, each row is displayed as a card. All cells in a row are rendered
vertically instead of horizontally. Therefore, this mode is appropriate for screens or containers that
have limited width.
Card view mode can be turned on by setting cardView prop in OverviewEngine component to true .
Here is an example result:
59

-- 59 of 114 --

Figure 26. Card-view mode
Embedded
Sometimes, there is a need to have an Overview Engine inside another container. However, the
default look is usually not suitable for this case. For example, OverviewEngine renders not only a
table, but also a content-box, which may not look good if the container already renders its own
content-box. Therefore, we provide this mode to help solve that situation.
Embedded mode can be enabled by turning on embedded prop in OverviewEngine component. The
following example demonstrates an embedded one:
60

-- 60 of 114 --

Figure 27. Embedded mode
Disability
Overview Engine can be disabled by turning on the disabled prop in OverviewEngine component:
61

-- 61 of 114 --

Figure 28. Disabled
Component Customization
The components rendered in Overview Engine are retrieved from the componentMap passed to
OverviewEngine. This makes it possible to customize components in Overview Engine. If custom
components are not defined, the default one in DefaultComponentMap will be used.
To be more specific, customizing a component usually involves two steps:
• Create a custom component.
• Register the custom component by putting it into componentMap map in OverviewEngine with the
key corresponding to the component you want to customize.
Show Number of Entries
Open the Overview Model in SME, then check the checkbox Show Number Of Entries to enable
this feature. Please refer to Number of Entries session in SME documentation for more details.
Depending on the Pagination Behavior, the number of rows can be passed to the OverviewEngine via
the rowCount field of the pagination or infiniteScrollOptions prop. Note that rowCount is optional, so
just set it undefined before the server responds the real total number of rows.
WARNING If there is no label, or it is hidden, both label and number of entries will not be
62

-- 62 of 114 --

shown.
Customize Row Styling
Row styling is a callback used by Overview Engine to query the style of a specific row. It receives an
object parameter containing the row that would be rendered and its corresponding row index.
Based on that, the callback should return an object of RowStyles type, which applies to the row.
RowStyles, which belongs to Widgets library, has the following structure:
RowStyles interface
/**
* Collections of style values for the row
*/
export interface RowStyles extends Styleable {
title?: string;
selected?: boolean;
interactive?: boolean;
disabled?: boolean;
highlightVariant?: TableTemplateProps.TableHighlightVariant;
highlighted?: boolean;
disabledRightClickContextMenu?: boolean;
}
export interface Styleable {
/**
* Additional css class names.
*/
readonly className?: string;
/**
* Additional style.
*/
readonly style?: React.CSSProperties;
}
The following example demonstrates how to set interactive status of a specific row.
Code:
<OverviewEngine
{...otherProps}
rowStyling={({ row, rowIndex }) => {
if (ProductDocument.isInstance(row) && rowIndex === 0) {
return { interactive: false };
}
return { interactive: true };
63

-- 63 of 114 --

}}
/>
Engine Id Prefix
At the moment, model name, set in Overview Model header details, is used as id for different
components (e.g. filter options, filter selector, search bar…).
In case there are many engines of the same model on a screen, this leads to incorrect component
identification. Since version 35.2.0, the introduction of uiIdPrefix property in OverviewEngine allows
a flexible way to add a prefix to the engine id, which could eliminate the above issue.
Custom Field Formatting
Since version 35.2.0, Overview Engine introduces an optional property called fieldFormatter to
ExpressionCell and ReferenceCell. This supports the possibility to modify the format of a specific
field.
The property is a callback that receives a parameter as an instance of FieldFormatterParams and
return a string.
The following example demonstrates how to implement fieldFormatter property to customize field
formatting.
Create a component with custom field formatter
import * as React from "react";
import { format } from "date-fns/format";
import { convertMomentToDateFnsFormat } from "@com.mgmtp.a12.widgets/widgets-core";
import {
OverviewEngine,
useFieldFormatter,
type ExpressionCell,
DefaultComponentMap,
type FieldFormatterParams
} from "@com.mgmtp.a12.overviewengine/overviewengine-core";
export const CustomExpressionCell: React.FC<ExpressionCell.Props> = (props) => {
const formatField = useFieldFormatter();
const fieldFormatter = React.useCallback(
(params: FieldFormatterParams) => {
if (props.columnModel.id === "column-cff6a" && params.value instanceof
Date) {
// below is a date format using moment's format, to use with date-fns
we can use the widgets utility
return `${format(params.value, convertMomentToDateFnsFormat("MMM
YYYY"))}`;
64

-- 64 of 114 --

}
return formatField(params);
},
[formatField, props.columnModel.id]
);
return <DefaultComponentMap.ExpressionCell {...props}
fieldFormatter={fieldFormatter} />;
};
Override the corresponding component in component map
return (
<OverviewEngine {...otherProps} componentMap={{ ...DefaultComponentMap,
ExpressionCell: CustomExpressionCell }} />
);
Custom Time Format
Starting with version 39.0.0, the time format (12h or 24h) is configured globally via the
DateTimeContext from @com.mgmtp.a12.widgets/widgets-core. The previous timeMode prop on
OverviewEngine and on the DateTimeFilterOptionsView / TimeFilterOptionsView components has been
removed.
Wrap the application (or a subtree containing the OverviewEngine) in a DateTimeContext.Provider
and set timeMode to "12h" or "24h". All datetime widgets rendered below the provider pick up the
value automatically.
NOTE When no DateTimeContext.Provider is set, widgets fall back to the 12-hour format.
The following example demonstrates how to configure the 24-hour format globally for the
Overview Engine:
Provide time mode via DateTimeContext
import * as React from "react";
import { enUS } from "date-fns/locale";
import { DateTimeContext } from "@com.mgmtp.a12.widgets/widgets-core";
import { OverviewEngine } from "@com.mgmtp.a12.overviewengine/overviewengine-core";
const dateTimeContextValue = { locale: enUS, timeMode: "24h" as const };
export const CustomOverviewEngine: React.ComponentType<OverviewEngine.Props> = (props)
=> {
return (
<DateTimeContext.Provider value={dateTimeContextValue}>
<OverviewEngine {...props} />
65

-- 65 of 114 --

</DateTimeContext.Provider>
);
};
Accessibility Configurations
In case the project has accessibility requirements, accessibilityConfigurations property of
OverviewEngine could be used to set accessibility-related features, including.
• hasFootContent: The Overview table footer only has aria-attributes when this property is set as
true.
Re-rendering Optimization
Re-rendering is part of any React component lifecycle, most of the time, it is good because your
component will always "react" to any change that happens in your application. However, when
rendering a big list, re-rendering can easily cause a bottleneck due to the constantly re-rendering of
every component. Therefore, it is important to detect the bottleneck when performance is top of
project’s priority.
There are multiple approaches to figure out the re-rendering:
1. React devtools: out of the box integration, install the extension then enable highlighting updates
or profiling yourself. However, the tools is lacking of hooks supports nor details comparison on
why the component is rendered, this sometimes lead to confusion on figuring out which
component should be improved.
2. @welldone-software/why-did-you-render: is a library which requires some knowledge about
the build tools. Once integrated properly, the tools can show a lot of useful information on
which props is changed on which hooks… which can help a lot with decision-making on what to
be improved one by one.
What Are the Usual Causes of Re-Rendering in Overview Engine
Customized Component
1. Most if not all Overview Engine component are memoized via React.memo. Your custom
component should also have it.
2. Avoid re-creating component passed into the component map.
BadCustomOverviewEngine.tsx
/// Bad practice, this can lead to lots of mount/unmount with any change on the Redux
store
function CustomOverviewEngine(props) {
const myState = useSelector(mySelector)
66

-- 66 of 114 --

return (
<OverviewEngineFactories.ViewComponent
{...props}
componentMap={{
TableBody: function CustomBody() {
if(myState === true) {
return <MyCustomBody />
}
return <DefaultComponentMap.TableBody />;
}
}}
/>
);
}
CustomOverviewEngine.tsx
/// Best practice:
/// - Preventing CustomBody function from re-initialized
/// - Cherry pick only the needed state
/// - React.memo
function CustomOverviewEngine(props) {
const componentMap = React.useMemo(() => ({ TableBody: CustomBody }), [])
return <OverviewEngineFactories.ViewComponent {...props}
componentMap={componentMap} />;
}
const CustomBody = React.memo(function CustomBody(props) {
const myState = useSelector(mySelector)
if(myState === true) {
return <MyCustomBody />
}
return <DefaultComponentMap.TableBody {...props} />;
})
NOTE
The above examples is a blueprint which mean it can be applied to any generic
React component that share the same component overriding pattern, this includes
Overview Engine’s ComponentMap/WidgetsMap or Table Widget’s componentRenderers.
Selectors
Selector can be a bottleneck for application when not implemented properly. A non-optimized one
will always be triggered when an action is dispatched which will lead to the selector to be executed
again. Refer to reselect documentation if interested.
Others
Reference type a.k.a. objects and functions recreation usually does not tax on the performance.
67

-- 67 of 114 --

However, when those are passed as a property into a component, changing/re-creating the
reference will always lead to re-rendering. This can be solved with React.useMemo and
React.useCallback.
Custom SelectorMap
NOTE This API is marked as experimental. Breaking changes might happen even in minor
releases.
The SelectorMap can be used to customize certain state access of the Overview Engine. The
Overview Engine internally uses a default variant of this map, but you can provide your own
implementation containing your customizations as a prop for Overview Engine View Component.
Then it will be used in place of the default one.
For example, customizing the way the Overview Engine selects attachment thumbnails can look like
this:
import {
DefaultSelectorMap,
type SelectorMap
} from "@com.mgmtp.a12.overviewengine/overviewengine-
core/lib/main/view/configuration/selector-map.js";
/**
* The default selector looks up thumbnails by id
*
* Here, we use the content property of attachments instead.
* If it does not exist, we fall back to the default selector.
*/
const CustomSelectorMap: SelectorMap = {
...DefaultSelectorMap,
attachmentThumbnail: (attachment) => {
return (state) =>
attachment.content?.startsWith("data:image/")
? attachment.content
: DefaultSelectorMap.attachmentThumbnail(attachment)(state);
}
}
Note that it is mandatory to spread the DefaultSelectorMap when customizing (similar to WidgetMap
and ComponentMap).
Overview Engine Data Loader
Overview Engine comes with a default A12 Server Connector that provides the data
fetching/modifying logic based on the A12 standard setup. However, in some cases, where A12
standard setup does not provide sufficient features or extension points, developers would want to
68

-- 68 of 114 --

adapt either server-side or client-side code to fulfill their needs.
Generally, it would be simpler to adapt the server-side implementation. However, in case adaption
cannot be done on the server-side, Overview Engine also offers a possibility to do it on the client-
side.
Use Cases
There are several use cases that customizing the default Overview Engine Data Loader can come in
handy:
• Have a connection to the Data Services instance but some minor adjustments are needed. For
example: Applying custom filters conditions, custom sorting, modify fields projection, etc.
• Have no connection to Data Services instance. For example: In SME (Simple Model Editor)
integration, most connections and data storage are done on the client-side.
• Have a connection to the backend server, but it is a completely different instance of Data
Services.
Default Data Loader
Overview Engine comes with a default data loader that is used to query data from the A12 Data
Services. Any network request coming from the Overview Engine will have to go through this data
loader, unless overridden.
When Overview Engine request for the data, the data loader will be called with the following
parameters:
• activityId: The ID of the Overview Engine activity.
• documentService: The document service instance, used for processing documents after being
fetched (e.g.: parsing dates).
• documentModel & overviewModel used by the engine.
• queries: A list of DataOperation.Query which will be transformed into a Data Services Query API
request.
Query
DataOperation.Query represents a query operation in Overview Engine. It includes the following
types:
• DataOperation.ListDocuments.Query: Lists all documents for the overview table.
• DataOperation.ListStringFilterOptions.Query: Lists candidates for enumerated string filter
options.
• DataOperation.Export.Query: Exports the overview table.
Each query type may have different required/optional properties. Common properties include:
• id: Identifier for the query operation.
69

-- 69 of 114 --

• fields: Optional list of fields to be projected in the query result.
• constraint: Optional constraint operator for the query.
• sorting: Optional sorting criteria for the result.
• paging: Pagination details, including page size and page numbers. Might request for multiple
pages in one go.
• aggregation: Optional aggregation details, such as group-by and aggregate functions.
Aggregation queries use the entries list as the result. With Query API, to avoid conflicts, split
aggregation into a separate request while keeping common properties (e.g., constraint).
WARNING The DataOperation.Query is specific to Overview Engine. While it aims to align
with the Data Services Query API, they are not 100% identical.
Below are several examples of how a query is created by Overview Engine:
List documents query
const listDocumentsQuery: DataOperation.ListDocuments.Query = {
id: "OverviewEngineDataProvider-0",
type: "LIST_DOCUMENTS",
paging: {
pageNumbers: [0],
pageSize: 10
},
sort: [
{
field: "/Person/PersonalData/PlaceOfBirth",
direction: Query.Direction.ASC,
nullHandling: Query.NullHandling.NULLS_LAST,
ignoreCase: true
}
],
constraint: {
operator: "or",
operands: [
{
operator: "exact_match",
field: "/Person/PersonalData/FirstName",
value: "Aaron",
caseSensitive: true
},
{
operator: "exact_match",
field: "/Person/PersonalData/FirstName",
value: "Allen",
caseSensitive: true
}
]
},
fields: [
70

-- 70 of 114 --

"/Person/PersonalData/FirstName",
"/Person/PersonalData/LastName",
"/Person/PersonalData/PlaceOfBirth",
"/Person/PersonalData/Nationality",
"/Person/PersonalData/Salary"
],
aggregation: {
aggregations: [
{
field: "/Person/PersonalData/Salary",
function: "sum"
}
],
group: [
{
field: "/__meta/modelReference",
alias: "model"
}
]
}
};
List String Filter Options query
const enumeratedStringFilterQuery: DataOperation.ListStringFilterOptions.Query = {
id: "EnumeratedStringDataProvider-2",
type: "LIST_STRING_FILTER_OPTIONS",
paging: {
pageNumber: 0,
pageSize: 5
},
constraint: {
operator: "simple_search",
fields: ["/Person/PersonalData/FirstName"],
value: "Allen"
},
aggregation: {
aggregations: [
{
function: "count",
field: "/Person/PersonalData/FirstName"
}
],
group: [
{
field: "/Person/PersonalData/FirstName",
alias: "name"
}
]
}
71

-- 71 of 114 --

};
Export query
const exportQuery: DataOperation.Export.Query = {
id: "export",
type: "EXPORT",
sort: [
{
field: "/businessPartner/name",
direction: Query.Direction.DESC,
nullHandling: Query.NullHandling.NULLS_FIRST,
ignoreCase: true
}
]
};
When a query is triggered by the Overview Engine, it is transformed into a Data Services Query API
request. This transformation maps Overview Engine’s DataOperation.Query types and properties to
the structure expected by the Data Services.
Query Result
After the request is sent and a response is received, the data loader maps the response back into the
Overview Engine’s expected data structure, DataOperation.QueryResult. This includes:
• Matching the response ID to the original query.
• Extracting the response’s entries into a documents list, processing the documents as needed (e.g.,
parsing dates).
• Extracting the aggregationResult, which is usually coming from a separate aggregation Query
API request.
• Extracting thumbnails records as well as other information like fullSize.
• Handling the "Export" query result, which requires returning the location of the exported file
instead of a list.
This transformation layer allows Overview Engine to remain decoupled from backend specifics,
enabling extensibility and customization of query handling logic.
Customize Data Loader
Query API offers the power to control how the user receives the data, but the engine might not yet
be able to cover all possibilities. Therefore, it is crucial to allow developers to customize the default
data loader behavior to fit their needs, the OverviewEngineDataLoader is delivered to solve this
exact problem. Below are a few instances of how to modify the queries, created by the Overview
Engine, with added customizations on top… Then process to re-use the default data loader
implementation to avoid unnecessary code duplication.
72

-- 72 of 114 --

Add Extra Filters
Add extra filter
export const customFilterDataLoader: OverviewEngineDataLoader = {
*buildRequests(params) {
const { queries, documentModel } = params;
const [query, ...otherQueries] = queries;
let updatedQuery = query;
if (DataOperation.ListDocuments.Query.isAssignableFrom(query) &&
documentModel.header.id === "ProductDM") {
// Create a custom constraint to filter products without productType
const customConstraint: Query.Operator = {
operator: Query.OPERATORS.NOT_OPERATOR,
operand: { operator: Query.OPERATORS.UNDEFINED_MATCH_OPERATOR, field:
"/product/productType" }
};
updatedQuery = {
...query,
// Either extends the existing constraint or directly uses it if none
exists
constraint: query.constraint
? { operator: Query.OPERATORS.AND_OPERATOR, operands:
[query.constraint, customConstraint] }
: customConstraint
};
}
return yield* defaultBuildRequests({ ...params, queries: [updatedQuery,
...otherQueries] });
},
*handleResponses(params): SagaGenerator<DataOperation.ResultSet> {
return yield* defaultHandleResponses(params);
}
};
Apply default constraints to enumerated string filter
Apply default constraints to enumerated string filter
export const customEnumeratedStringFiltersDataLoader: OverviewEngineDataLoader = {
*buildRequests(params) {
const { queries, documentModel } = params;
const [query, ...otherQueries] = queries;
let updatedQuery = query;
if (
DataOperation.ListStringFilterOptions.Query.isAssignableFrom(query) &&
documentModel.header.id === "ProductDM"
73

-- 73 of 114 --

) {
// A valid candidate for the enumerated string options must be a person
with an email address
const withEmailFieldConstraint: Query.Operator = {
operator: Query.OPERATORS.NOT_OPERATOR,
operand: { operator: Query.OPERATORS.UNDEFINED_MATCH_OPERATOR, field:
"/product/seller/email" }
};
updatedQuery = {
...query,
// Either extends the existing constraint or directly uses it if none
exists
constraint: query.constraint
? { operator: Query.OPERATORS.AND_OPERATOR, operands:
[query.constraint, withEmailFieldConstraint] }
: withEmailFieldConstraint
};
}
return yield* defaultBuildRequests({ ...params, queries: [updatedQuery,
...otherQueries] });
},
*handleResponses(params): SagaGenerator<DataOperation.ResultSet> {
return yield* defaultHandleResponses(params);
}
};
Enforce Default Sorting
Enforce default sorting
export const customSortingDataLoader: OverviewEngineDataLoader = {
*buildRequests(params) {
const { queries, documentModel } = params;
const [query, ...otherQueries] = queries;
let updatedQuery = query;
if (DataOperation.ListDocuments.Query.isAssignableFrom(query) &&
documentModel.header.id === "PersonDM") {
updatedQuery = {
...query,
// Apply a custom default sorting, this could also be extended to
emulate the multi-column sorting
sort: query.sort ?? [
{
field: "/person/city",
direction: Query.Direction.ASC,
ignoreCase: false,
nullHandling: Query.NullHandling.NULLS_LAST
}
74

-- 74 of 114 --

]
};
}
return yield* defaultBuildRequests({ ...params, queries: [updatedQuery,
...otherQueries] });
},
*handleResponses(params): SagaGenerator<DataOperation.ResultSet> {
return yield* defaultHandleResponses(params);
}
};
Modify Fields Projection
Modify fields projection
export const customFieldsProjectionDataLoader: OverviewEngineDataLoader = {
*buildRequests(params) {
const { queries, documentModel } = params;
const [query, ...otherQueries] = queries;
let updatedQuery = query;
if (DataOperation.ListDocuments.Query.isAssignableFrom(query) &&
documentModel.header.id === "BundleDM") {
updatedQuery = {
...query,
// Extends the default selection of fields projection to include the
"bundleType"
// If not specified, all fields are returned
fields: query.fields ? [...query.fields, "/bundle/bundleType"] :
undefined
};
}
return yield* defaultBuildRequests({ ...params, queries: [updatedQuery,
...otherQueries] });
},
*handleResponses(params): SagaGenerator<DataOperation.ResultSet> {
return yield* defaultHandleResponses(params);
}
};
Default Data Loader Fallback
Default Data Loader Fallback
function* defaultBuildRequests(params:
Parameters<OverviewEngineDataLoader["buildRequests"]>[0]) {
return yield* call(
75

-- 75 of 114 --

maybeAsyncFnWrapper(OverviewEngineFactories.dataLoader.buildRequests.bind(OverviewEngi
neFactories.dataLoader)),
params
);
}
function defaultHandleResponses(
params: Parameters<OverviewEngineDataLoader["handleResponses"]>[0]
): SagaGenerator<DataOperation.ResultSet> {
return call(
maybeAsyncFnWrapper(OverviewEngineFactories.dataLoader.handleResponses.bind(OverviewEn
gineFactories.dataLoader)),
params
);
}
Skip Initial Loading
To avoid triggering expensive and often irrelevant queries on initial view, the Overview Engine
supports an option to defer data loading. This is particularly useful for models with large datasets,
where the default query (e.g. first page with sorting) can cause performance issues due to how the
Data Services processes large result sets. Instead of loading data immediately, the Engine will wait
for explicit user action—such as pressing the search button or applying a filter—before sending a
request.
To enable this feature, set the skipInitialLoad property to true in the Overview Model’s
configuration section:
{
"content": {
"configuration": {
"skipInitialLoad": true
}
}
}
When enabled, the Overview Engine will display a message prompting the user to perform a search
or apply a filter. Once the user triggers a query (e.g. by searching or changing filters), data loading
proceeds as normal.
NOTE
When using skipInitialLoad, the withoutData property in the corresponding
initialActivity of the Application Model should not be set to true. The Overview
Engine handles the deferred loading internally.
76

-- 76 of 114 --

Custom RequestSelectorMap
NOTE This API is marked as experimental. Breaking changes might happen even in minor
releases.
The RequestSelectorMap can be used to customize how the Overview Engine produces Data Services
Query API requests. Overview Engine ships a default variant, but you can provide your own
implementation containing your customizations. Then it will be used in place of the default one.
For example, adding a default constraint for a specific model while delegating back to the default
implementation can look like this:
/**
* Example: customize requests by wrapping the defaults.
*
* Always spread DefaultRequestSelectorMap and only override what you need.
*/
export const customRequestSelectorMap: RequestSelectorMap = {
...DefaultRequestSelectorMap,
// Add a default constraint for a specific model, then delegate back to the
default implementation
loadListDocuments: (config) => {
const { documentModel, query } = config;
if (documentModel.header.id === "ProductDM") {
const mustHaveSku: Query.Operator = {
operator: Query.OPERATORS.NOT_OPERATOR,
operand: { operator: Query.OPERATORS.UNDEFINED_MATCH_OPERATOR, field:
"/product/sku" }
};
const updatedQuery: typeof query = {
...query,
constraint: query.constraint
? { operator: Query.OPERATORS.AND_OPERATOR, operands:
[query.constraint, mustHaveSku] }
: mustHaveSku
};
return DefaultRequestSelectorMap.loadListDocuments({ ...config, query:
updatedQuery });
}
return DefaultRequestSelectorMap.loadListDocuments(config);
}
};
Note that it is mandatory to spread the DefaultRequestSelectorMap when customizing (similar to
77

-- 77 of 114 --

SelectorMap, WidgetMap, and ComponentMap).
TIP See the API reference for RequestSelectorMap and the default implementation
DefaultRequestSelectorMap.
Why does loadListDocuments return a list of requests instead of a single one? Overview Engine
supports requesting multiple pages in a single operation. Each requested page is mapped to one
Query API request, which allows the data loader to dispatch them concurrently and merge the
results in order. Other methods build a single request.
Injecting a custom map
The map can be injected into an Overview Engine instance by using the data provider function or
the generic createModule function
function setup() {
// Either
OverviewEngineFactories.createModule({
requestSelectorMap: customRequestSelectorMap
});
// Or directly configure it per data provider
dataHandlers =
OverviewEngineFactories.createDataProviders(OverviewEngineFactories.dataLoader, {
requestSelectorMap: customRequestSelectorMap
});
// Rest...
}
Request Filters
This customization approach can also be used in combination with the RequestFilter API (described
in the Data Services documentation), for example to use your own operation methods. Customizing
the RequestFilter alone would not be enough when the method replacement needs some context
(e.g. only overriding methods in certain conditions). Using the RequestSelectorMap could then be
used to provide this context down for the filter to use.
Scroll to Row
Overview Engine provides the Events.onScrollToRow event to scroll the table to a specific row
programmatically. This is useful for flows like "scroll to top", "jump to last edited item", or focusing
a row after navigation.
The event accepts either a document reference or a row index. Optionally, autoFocus can be set to
move keyboard focus to the target row after scrolling.
78

-- 78 of 114 --

NOTE When virtual scrolling is enabled, scrolling by docRef is not supported. Use rowIndex
instead.
Payload
Events.onScrollToRow accepts one of the following payloads:
• { rowIndex?: number, autoFocus?: boolean }
• { docRef?: string, autoFocus?: boolean }
Example
The following example adds a "Scroll to top" button that scrolls to the first row:
Dispatch scroll-to-row event
/*
* SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
*
* Copyright (c) 2012-2026 mgm technology partners GmbH
*
* Dual License
* ------------
* This source file is part of the mgm A12 Platform and available under
* a choice of two different licenses:
*
* 1. Open-Source License - EUPL v1.2
* You may redistribute and/or modify this file under the terms of the
* European Union Public License, version 1.2 - see https://eupl.eu/.
*
* 2. Commercial License
* Alternatively, you may obtain a commercial license from
* mgm technology partners GmbH, that permits use of this software
* under different terms (including support and maintenance services).
*
* Please contact a12-license@mgm-tp.com for more information.
*
* You must select and comply with exactly one of the above license options.
*
* Warranty Disclaimer (applies to either option)
* ----------------------------------------------
* THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT WARRANTY OF ANY KIND,
* WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
* LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
*/
import { useDispatch } from "react-redux";
79

-- 79 of 114 --

import { Button } from "@com.mgmtp.a12.widgets/widgets-core";
import { Events, OverviewEngineActions } from
"@com.mgmtp.a12.overviewengine/overviewengine-core";
export const ScrollToTopButton = (props: { activityId: string }) => {
const { activityId } = props;
const dispatch = useDispatch();
const onClick = () => {
dispatch(
OverviewEngineActions.event({
activityId,
engineAction: Events.onScrollToRow({ rowIndex: 0, autoFocus: true })
})
);
};
return <Button label="Scroll to top" onClick={onClick} />;
};
The event is handled by the engine and stored in uiState.scrollToRow until the view processes it.
The view then acknowledges it internally so the request is cleared.
Pagination Behavior
When using pagination, Events.onScrollToRow with a rowIndex only operates on the currently visible
page. It does not change the active page. This is by design — pagination state and scroll position are
independent concerns.
If you need to scroll to a row on a different page, dispatch two actions sequentially:
1. A page-change action (e.g., Events.onPageClicked) to navigate to the target page.
2. Events.onScrollToRow with the desired rowIndex within that page.
NOTE When using docRef, the engine resolves the row from the current page data. If the
referenced document is not on the current page, the scroll request has no effect.
Row Activation
The rowActivation field on OverviewModel.Content controls how rows respond to user interaction.
When omitted, the default behavior applies: rows are interactive and clicking a row invokes the
onRowClick handler.
Two activation types are available:
• event — clicking a row dispatches a named custom event in addition to invoking engine
handlers.
• non_interactive — rows have no pointer cursor, and click events are suppressed entirely.
80

-- 80 of 114 --

Event
Setting rowActivation to { "type": "event", "event": "<eventName>" } causes a named event to be
passed as customEvent in the onRowClick payload whenever a row is clicked. This allows consumers
to distinguish between rows dispatching different events without needing per-row rowStyling
overrides.
Overview Model
{
"content": {
"rowActionGroup": { "actions": [] },
"rowActivation": { "type": "event", "event": "selectEmployee" }
}
}
The customEvent value is available in the onRowClick event handler:
Handling the custom event
<OverviewEngine
eventHandlers={{
onRowClick: ({ documentId, customEvent }) => {
if (customEvent === "selectEmployee") {
dispatch(selectEmployee(documentId));
}
}
}}
/>
Non-Interactive Rows
Setting rowActivation to { "type": "non_interactive" } makes all rows non-interactive at the model
level. Rows rendered with this configuration have no pointer cursor, and clicking them does not
invoke any handler.
Overview Model
{
"content": {
"rowActionGroup": { "actions": [] },
"rowActivation": { "type": "non_interactive" }
}
}
Row action buttons within non-interactive rows still work normally — only the row click itself is
suppressed.
NOTE When multi-selection is active and the selection area includes the row, the row is
81

-- 81 of 114 --

temporarily restored to interactive so the user can still toggle selection by clicking it.
Interaction with rowStyling
The engine-computed interactive state is merged first; a consumer’s rowStyling prop is applied on
top. This means a rowStyling callback can override the model:
• To force a row clickable when the model says non_interactive, return { interactive: true }
from rowStyling for that row.
• To respect the model strictly, omit the interactive key from the rowStyling return value.
Overriding interactive state per row
<OverviewEngine
rowStyling={({ row }) => ({
interactive: row.alwaysClickable ? true : undefined,
})}
/>
TypeScript
The rowActivation field maps to OverviewModel.RowActivation in the TypeScript model.
Link Columns
Link columns display data from a related document reached via an outgoing relationship, rather
than from the row’s own document. They are configured in the Overview Model exactly like
regular columns, with an additional linkReferences array that describes the relationship path to
traverse.
Link columns only work when the Overview Model is used together with a Query Model
(referenced via modelReferences with purpose: "query-model-for-overview"), and the corresponding
links must be declared in that Query Model’s content.links. Linked document data is fetched
exclusively through the query, so a link column whose linkReferences have no matching link in the
Query Model renders the overviewEngine.table.linkNotFound resource ("Link not found.").
Two link column types are available:
• LinkColumn.Reference — displays a field value from the related document (like a regular
reference column, but across a link).
• LinkColumn.Expression — evaluates an expression against the related document’s data.
Configuration
linkReferences
Every link column must include a linkReferences array with at least one entry describing the
relationship to follow.
82

-- 82 of 114 --

Property Type Description
relationship string The model ID of the Relationship Model connecting the
source document to the target.
targetRole string The role name of the target document within that
relationship.
type "CHILD" | "LINK" Relationship cardinality type. CHILD — the target is a child
document (outgoing-to-1). LINK — the target is a linked
document.
WARNING
The engine displays only one link per document (row). Link reference data is
returned as an array, and the engine always uses the first entry.
For this reason a link reference should resolve to a to-1 relationship.
Technically a to-many relationship can also be used, but the order of links is
not guaranteed, so the first entry — and therefore the displayed value — may
change between loads, producing unstable results. The SME only offers to-1
relationships when configuring a link reference.
LinkColumn.Reference
A LinkColumn.Reference has the same properties as a ReferenceColumn (except summary), plus
linkReferences.
Overview Model — Link reference column
{
"id": "column-dept-name",
"elementRef": "field_dept_name",
"width": 1.2,
"linkReferences": [
{
"relationship": "PersonDepartmentRM",
"targetRole": "department",
"type": "CHILD"
}
]
}
LinkColumn.Expression
A LinkColumn.Expression has the same properties as an ExpressionColumn, plus linkReferences. The
expression is evaluated in the context of the related document.
Overview Model — Link expression column
{
"id": "column-weekly-hours",
"name": "weekly_hours_expression",
83

-- 83 of 114 --

"linkReferences": [
{
"relationship": "PersonContractRM",
"targetRole": "contract",
"type": "CHILD"
}
],
"expression": "kontext(Contract) {\n [Weekly_Work_Hours] \" hours\"\n }",
"width": 1.5
}
Sorting
LinkColumn.Reference supports sorting. Add sortable: true and optionally preferredSorting to
enable it:
Overview Model — Link reference column with sorting
{
"id": "column-dept-name",
"elementRef": "field_dept_name",
"width": 1.2,
"linkReferences": [
{
"relationship": "PersonDepartmentRM",
"targetRole": "department",
"type": "CHILD"
}
],
"sortable": true,
"preferredSorting": "ASC"
}
When a linked column is sorted, the path in the Sorting state is a RelationshipField instead of a
plain string:
Narrowing Sorting.path for link columns
import type { RelationshipField } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
overviewState.sorting?.forEach(({ path, order }) => {
if (typeof path === "string") {
// plain column sort
} else {
// RelationshipField — path through a relationship
console.log(path.relationshipModel, path.targetRole, path.sortBy);
}
});
84

-- 84 of 114 --

If your application reads sort state (e.g. in a custom onColumnClick handler), pass
RelationshipModel[] and sub-document DocumentModel[] to getSortingProps so linked sort paths
resolve correctly:
getSortingProps with relationship models
import type { RelationshipModel } from "@com.mgmtp.a12.dataservices/dataservices-
access";
const sorting = getSortingProps(
uiState.sorting,
documentModel,
overviewModel,
relationshipModels, // RelationshipModel[] from data services state
subDocumentModels // DocumentModel[] for the linked target documents
);
NOTE
Sorting is not available in exclude mode. When queryModel.content.exclude is true,
column headers are rendered as non-sortable and clicking them has no effect,
regardless of the sortable flag on the column.
API Documentation
The API documentation can be found here.
Breaking Change Management
For the general definition of breaking and non-breaking changes in the A12 platform, as well as
frontend and backend perspectives, see the A12 Breaking Change Management page.
The following section describes how this general definition is interpreted for the Overview Engine.
Public API Surface
The public API is defined by the exports in index.ts. Any removal or incompatible change to an
exported symbol is a breaking change. Additions to the public API are non-breaking.
Exports annotated @experimental may change in minor releases without being considered breaking.
Exports annotated @internal are not part of the public API and carry no stability guarantee.
Resource Keys
Adding new entries to RESOURCE_KEYS is non-breaking: applications that do not override those keys
continue to receive the default translation. Removing or renaming an existing key is a breaking
change for any application that provides a custom translation for it.
85

-- 85 of 114 --

Redux State Shape and Action Types
The Redux action type strings and the reducer state shape (UiState, ModelsState) are part of the
public contract. Renaming or removing an action type, or restructuring the state in a way that
breaks existing selectors, is a breaking change.
Selectors and sagas that are part of the public API follow the standard public-API rule above.
Composable Factories
The signatures of withOverviewEngine(), withOverviewEngineDataHandlers(),
withOverviewEngineDataReducers(), withOverviewEngineMiddlewares(),
withOverviewEnginePlatformSagas(), withOverviewEngineView(), and withOverviewModelSupport() are
part of the public API. Any change to parameter types or return types that requires call-site updates
is a breaking change. New optional parameters are non-breaking.
Component Map
ComponentMap and DefaultComponentMap are part of the public API. Adding new keys (optional or
required) is non-breaking as long as a default implementation is provided in DefaultComponentMap.
Removing or renaming an existing key, or changing the required props of a component slot, is a
breaking change.
Overview Model Format
Only changes that are incompatible with existing Overview Model files are breaking: for example,
removing a required field or changing the semantics of an existing field.
Adding an optional field is non-breaking even though it triggers a model schema version bump
and a corresponding migration step. When such a change is introduced, the Overview Engine
model migration tool handles the version upgrade automatically. See the [Model Migration Tool]
chapter for usage instructions.
Server Protocol
Changes to the wire format between the client-side data loader and the backend (request/response
shape, query parameters, endpoint URLs) are breaking on both the frontend and backend sides.
Non-Breaking Examples
The following types of changes are consistently treated as non-breaking in the Overview Engine:
• Adding a new optional configuration field to an existing API
• Adding new RESOURCE_KEYS entries
• Introducing a new @experimental export
86

-- 86 of 114 --

• Internal refactoring that does not affect public exports
• Bumping a transitive dependency with no visible API impact
Migration Instructions
2026.06
39.0.0
React peer dependency raised to 19.2
Tooling: none — manual change.
The minimum supported version of react and react-dom in our peerDependency has been raised
from ^19.0.0 to ^19.2.6. Update your package.json accordingly:
{
"dependencies": {
"react": "^19.2.6",
"react-dom": "^19.2.6"
}
}
Minimum supported model version raised to 37.0.0
Tooling: Model migration tool, required — see 39.0.0.
The migration tool no longer supports migration of Overview models before version 37.0.0. If you
need to migrate models from an older version, you need to migrate to version 37.0.0 first using the
migration tool of that version.
Replace defaultRowAction with rowActivation
Tooling: Model migration tool, required — auto-migrates the legacy shape (see 39.0.0); the
TypeScript change below is manual.
The defaultRowAction field on OverviewModel.Content has been replaced by rowActivation. The new
field accepts two shapes:
• { type: "event", event: "…" } — fires the configured event on row click. Auto-migrated from
the legacy { custom: true, event: "…" } shape.
• { type: "non_interactive" } — new for accessibility. Row click is suppressed; the row renders
without pointer cursor, hover, or focus affordance.
Omitting rowActivation keeps the engine’s default behavior (onRowClick fires with customEvent:
undefined).
87

-- 87 of 114 --

TypeScript changes
The type OverviewModel.DefaultRowAction has been removed. Use OverviewModel.RowActivation:
type RowActivation =
| OverviewModel.EventRowActivation // { type: "event"; event: string }
| OverviewModel.NonInteractiveRowActivation; // { type: "non_interactive" }
Sorting for linked reference columns
Tooling: none — manual change.
Linked reference columns (LinkColumn.Reference) now support the sortable and preferredSorting
properties, which were previously not available for linked columns. This allows sorting by fields
from related documents across outgoing-to-1 relationships.
No migration is required for existing models — to opt in, add sortable and optionally
preferredSorting to a linked reference column:
Overview Model — Linked reference column with sorting
{
"id": "column-dept-name",
"elementRef": "field_dept_name",
"width": 1.2,
"linkReferences": [
{
"relationship": "PersonDepartmentRM",
"targetRole": "department",
"type": "CHILD"
}
],
"sortable": true,
"preferredSorting": "ASC"
}
Sorting.path widened to string | RelationshipField
The Sorting interface’s path property has changed from string to string | RelationshipField. A
new RelationshipField interface is now exported to represent a sort path through a relationship
chain.
Before After
Sorting.path: string Sorting.path: string | RelationshipField
(not exported) RelationshipField { relationshipModel: string;
targetRole: string; sortBy: string |
RelationshipField }
Consumers who directly inspect sort state (e.g. custom onSort handlers or code reading
88

-- 88 of 114 --

UiState["sorting"]) must narrow the type:
Before
overviewState.sorting?.forEach(({ path, order }) => {
console.log(path.toUpperCase()); // path was always a string
});
After
import type { RelationshipField } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
overviewState.sorting?.forEach(({ path, order }) => {
if (typeof path === "string") {
console.log(path.toUpperCase());
} else {
// RelationshipField — sort through a relationship chain
console.log(path.relationshipModel, path.targetRole);
}
});
getSortingProps accepts relationship models
getSortingProps has two new optional parameters needed to resolve sort paths for linked reference
columns:
Before After
getSortingProps(sorting, documentModel,
overviewModel)
getSortingProps(sorting, documentModel,
overviewModel, relationshipModels?,
subDocumentModels?)
If linked reference columns are present in the model, pass the available RelationshipModel[] and
sub-document DocumentModel[] so paths can be resolved:
After
import type { RelationshipModel } from "@com.mgmtp.a12.dataservices/dataservices-
access";
const sorting = getSortingProps(
uiState.sorting,
documentModel,
overviewModel,
relationshipModels, // RelationshipModel[] from the data services state
subDocumentModels // DocumentModel[] for linked target documents
);
Calls without the new parameters continue to work as before for models that use only plain
89

-- 89 of 114 --

reference columns.
Split OverviewEngineDataLoader.provideData into buildRequests and handleResponses
Tooling: none — manual change.
The OverviewEngineDataLoader interface has been extended to include two required methods:
buildRequests and handleResponses. The previous provideData method and its associated types
(ProvideDataParams, ProvideDataResults) have been removed.
What changed
Before After
provideData(params: ProvideDataParams):
Promise<DataOperation.ResultSet>
Removed. Split into buildRequests +
handleResponses.
buildRequests(params: BuildRequestsParams):
MaybeAsync<SupportedRequest[]>
Now required.
handleResponses(params:
HandleResponsesParams):
MaybeAsync<DataOperation.ResultSet>
Now required.
ProvideDataParams, ProvideDataResults Removed.
As a result of this change, OverviewEngineDataProvider now issues a single Dispatcher.rpc call for
all "embedded" data holders.
Migration
A custom provideData implementation typically follows the build / dispatch / interpret pattern:
Before
class MyLoader implements OverviewEngineDataLoader {
async provideData(params: ProvideDataParams): Promise<DataOperation.ResultSet> {
const requests = buildMyRequests(params);
const responses = await Dispatcher.rpc(lang, requests);
return interpretMyResponses(params, requests, responses);
}
}
Split it into the two separate methods. The Dispatcher.rpc call is no longer part of the loader — the
engine handles dispatching:
After
class MyLoader implements OverviewEngineDataLoader {
buildRequests(params: BuildRequestsParams): SupportedRequest[] {
return buildMyRequests(params);
}
handleResponses(params: HandleResponsesParams): DataOperation.ResultSet {
90

-- 90 of 114 --

// params.responsesByQueryId — responses keyed by query id
// params.thumbnails — shared thumbnail map, already merged
return handleMyResponses(params);
}
}
Parameter field mapping
• BuildRequestsParams carries the same fields as the old ProvideDataParams.
• HandleResponsesParams replaces the raw response list with responsesByQueryId:
ReadonlyMap<string, QueryJsonRpc2Response> and adds thumbnails: Record<string, string>. The
requestSelectorMap field is not present in HandleResponsesParams (it belongs to the request-
building side only).
Remove the timeMode prop in favor of DateTimeContext
Tooling: none — manual change.
The timeMode prop has been removed from OverviewEngine.Props as well as from the legacy
DateTimeFilterOptionsView and TimeFilterOptionsView components. The time format (12h or 24h) is
now configured globally via the DateTimeContext provided by @com.mgmtp.a12.widgets/widgets-core.
Steps to migrate:
1. Remove the timeMode prop from every <OverviewEngine> render site as well as from any custom
DateTimeFilterOptionsView / TimeFilterOptionsView wrappers.
2. Wrap the application (or any subtree that contains the OverviewEngine) in a
DateTimeContext.Provider and set the desired timeMode:
import { DateTimeContext } from "@com.mgmtp.a12.widgets/widgets-core";
import { enUS } from "date-fns/locale";
<DateTimeContext.Provider value={{ locale: enUS, timeMode: "24h" }}>
<OverviewEngine {...props} />
</DateTimeContext.Provider>
When no DateTimeContext.Provider is set, the underlying widgets fall back to the 12-hour format.
Change the default rendering of the date filter year selector
Tooling: none — manual change.
The year selector in the header of the date pickers used by the Date, Date & Time, and Date Range
filters changed its default rendering. Previously it was always a select drop-down. With the new
Widgets year selector it now resolves to an autocomplete (when a year range is configured) or a
text box (otherwise). This is an intended breaking change of the Widgets year selector.
If you want the old select drop-down back, pass yearSelectorVariant="select" to the affected filter
91

-- 91 of 114 --

option views via the componentMap. The same prop also lets you opt into "autocomplete" or "textbox"
explicitly.
The prop is supported by the DateFilterOptionsView, DateTimeFilterOptionsView, and
DateRangeFilterOptionsView built-in views.
Restore the select year selector in date filters
const componentMap = {
...DefaultComponentMap,
FilterOptionsViews: {
...DefaultComponentMap.FilterOptionsViews,
DateFilterOptionsView: (props) => (
<DefaultComponentMap.FilterOptionsViews.DateFilterOptionsView
{...props}
yearSelectorVariant="select"
/>
),
DateTimeFilterOptionsView: (props) => (
<DefaultComponentMap.FilterOptionsViews.DateTimeFilterOptionsView
{...props}
yearSelectorVariant="select"
/>
),
DateRangeFilterOptionsView: (props) => (
<DefaultComponentMap.FilterOptionsViews.DateRangeFilterOptionsView
{...props}
yearSelectorVariant="select"
/>
)
}
};
Move skip-initial-load to the Overview Model
Tooling: none — manual change.
The experimental loadingState prop has been removed from OverviewEngine.Props. The "skip initial
data loading" feature is now configured via the skipInitialLoad property in the Overview Model’s
configuration section.
Steps to migrate:
1. Remove withoutData: true from the Application Model’s initialActivity.
2. Remove any usage of the loadingState prop on the Overview Engine component.
3. Add "skipInitialLoad": true to the Overview Model’s configuration.
Overview Model — Before
{
92

-- 92 of 114 --

"content": {
"configuration": { }
}
}
Overview Model — After
{
"content": {
"configuration": {
"skipInitialLoad": true
}
}
}
Application Model — Before
{
"initialActivity": {
"descriptor": { "showcase": "person" },
"withoutData": true
}
}
Application Model — After
{
"initialActivity": {
"descriptor": { "showcase": "person" }
}
}
Rename collectFieldsProjection to getProjectedFields
Tooling: none — manual change.
The experimental collectFieldsProjection functions have been renamed:
Before After
collectFieldsProjection getProjectedFields
Before
import { collectFieldsProjection } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
const fields = collectFieldsProjection(overviewModel, documentModel, queryModel);
93

-- 93 of 114 --

After
import { getProjectedFields } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
const fields = getProjectedFields(overviewModel, documentModel, queryModel);
Rename majorElements / minorElements to rightSlot / leftSlot
Tooling: Model migration tool, required — auto-migrates JSON models (see 39.0.0); the TypeScript
rename below is manual.
The majorElements and minorElements properties in OverviewModel.SubHeaderBox and
OverviewModel.FooterBox have been renamed to rightSlot and leftSlot respectively to align with
their actual rendering positions. Accordingly, the rank property of ButtonType has also been changed
from "major" | "minor" to "left" | "right".
This is a model-level rename with no visual change. Existing JSON models are migrated
automatically by the model migration tool.
For TypeScript consumers:
Old property New property
SubHeaderBox.minorElements SubHeaderBox.leftSlot
SubHeaderBox.majorElements SubHeaderBox.rightSlot
FooterBox.minorElements FooterBox.leftSlot
FooterBox.majorElements FooterBox.rightSlot
ButtonType.rank: "major" | "minor" ButtonType.rank: "left" | "right"
Redux 5 upgrade
Tooling: Client codemod (@com.mgmtp.a12.client/client-codemod migrateTypescriptFsaImports),
optional — automates the typescript-fsa import rewrite only (see Migrate typescript-fsa Imports).
The dependency updates, useSelector typing, and middleware changes are manual.
Overview Engine has been upgraded to Redux 5 along with its ecosystem. For a complete list of
breaking changes in Redux 5, refer to the official Redux 5 release notes.
Dependency Updates
The following peerDependency versions have been updated:
Package Old Version New Version
redux ^4.2.1 ^5.0.0
react-redux ^7.2.1 ^9.2.0
redux-saga ^1.1.3 ^1.3.0
94

-- 94 of 114 --

typescript-fsa ^3.0.0 replaced by
@com.mgmtp.a12.client/typescri
pt-fsa-redux-5-compat ^1.0.0
Update your package.json to use the new versions:
{
"dependencies": {
"redux": "^5.0.0",
"react-redux": "^9.2.0",
"redux-saga": "^1.3.0",
"@com.mgmtp.a12.client/typescript-fsa-redux-5-compat": "^1.0.0"
}
}
Remove @types/react-redux from your dependencies, as react-redux now ships with its own
TypeScript types. Additionally, there are no peerDependency warnings between React and React-
Redux anymore, so you can remove any overrides or patches you had.
Migrate typescript-fsa Imports
Replace all imports from typescript-fsa with the compat fork:
Before
import { actionCreatorFactory, type Action } from "typescript-fsa";
After
import { actionCreatorFactory, type Action } from "@com.mgmtp.a12.client/typescript-
fsa-redux-5-compat";
You can run the Client codemod to automate this:
pnpm dlx @com.mgmtp.a12.client/client-codemod migrateTypescriptFsaImports
Define Type of State in useSelector
react-redux now has stricter types by default. The type of state in useSelector is now unknown. You
can fix this by augmenting the type of useSelector in a custom declaration file:
// @types/react-redux.d.ts
import type { UseSelector } from "react-redux";
declare module "react-redux" {
export const useSelector: UseSelector<object>;
95

-- 95 of 114 --

}
Alternatively, use a pre-typed version:
import { useSelector } from "react-redux";
export const useAppSelector = useSelector.withTypes<object>();
Potentially Fix Middlewares
The typing of actions inside middlewares has changed to unknown. Most middlewares should be
unaffected, as the first call is usually a type guard (e.g. actionCreator.match). If you have a generic
middleware that accesses action.type directly, use the isAction() type guard from redux:
import { isAction, type Middleware } from "redux";
const loggingMiddleware: Middleware = api => next => action => {
if (isAction(action)) {
console.log("Action:", action.type);
}
return next(action);
};
Remove support for nested imports
Tooling: Codemod recipe prefer-top-level-imports, required — nested imports are now compile
errors (see prefer-top-level-imports).
The package now uses the exports field in package.json to restrict entry points to the top-level
import only. Nested imports (e.g. from "@com.mgmtp.a12.overviewengine/overviewengine-
core/lib/main/…") that were deprecated in 2025.06-ext4 are now a compile error.
All public API is accessible from the top-level entry point:
import { OverviewModel, OverviewEngineApi } from
"@com.mgmtp.a12.overviewengine/overviewengine-core";
Run the codemod to migrate automatically:
npx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest prefer-top-level-
imports ./tsconfig.json
Remove deprecated top-level OverviewModel type aliases
Tooling: Codemod recipe prefer-top-level-imports, required — see prefer-top-level-imports.
96

-- 96 of 114 --

The deprecated top-level type aliases from overview-model.ts (deprecated in 2025.06-ext4) have
been removed. These types were never reachable through the top-level entry point and were only
accessible via nested imports, which are now a compile error.
Use the OverviewModel namespace instead:
Before
import { Content, Column, FilterMode } from
"@com.mgmtp.a12.overviewengine/overviewengine-core/lib/main/overview-model.js";
const content: Content = { /* ... */ };
const mode: FilterMode = FilterMode.ALL;
After
import { OverviewModel } from "@com.mgmtp.a12.overviewengine/overviewengine-core";
const content: OverviewModel.Content = { /* ... */ };
const mode: OverviewModel.FilterMode = OverviewModel.FilterMode.ALL;
Run the codemod to migrate automatically:
npx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest prefer-top-level-
imports ./tsconfig.json
See the 2025.06-ext4 deprecation notes for the full list of affected types.
Remove deprecated Pagination type
Tooling: Codemod recipe prefer-top-level-imports, required — see prefer-top-level-imports.
The deprecated Pagination type alias (deprecated in 2025.06-ext4) has been removed. Use
PaginationState instead.
Before
import { Pagination } from "@com.mgmtp.a12.overviewengine/overviewengine-core";
const pagination: Pagination = { pageNumber: 0, pageSize: 10 };
After
import { PaginationState } from "@com.mgmtp.a12.overviewengine/overviewengine-core";
const pagination: PaginationState = { pageNumber: 0, pageSize: 10 };
Run the codemod to migrate automatically:
97

-- 97 of 114 --

npx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest prefer-top-level-
imports ./tsconfig.json
NOTE The Pagination namespace (used for the Pagination component props, e.g.
Pagination.PropsType) is unaffected and remains available.
Deprecate RowActionState and the rowActionState prop
Tooling: none — manual change.
The RowActionState interface and the rowActionState prop on OverviewEngine are deprecated in favor
of the rowActionStyling callback. This covers both use cases: rowActions (apply to all rows) and rows
(per-row disambiguation). The id-keyed map in rows is ambiguous when the same document
appears in multiple rows (exclude mode); the callback receives the full row descriptor and can key
on any combination of fields.
RowActionState.IndividualRowActionState is preserved as a deprecated type alias pointing to
RowActionStyling.IndividualRowActionState.
Migration
Per-row (was RowActionState.rows)
Before
const rowActionState: OverviewEngineApi.RowActionState = {
rows: {
[documentId]: { myEvent: { disabled: true } }
}
};
<OverviewEngine rowActionState={rowActionState} ... />
After
const rowActionStylingPerRow: OverviewEngineApi.RowActionStyling = ({ row, button })
=> {
if (row.id === documentId && button.event === "myEvent") {
return { disabled: true };
}
return undefined;
};
export const PerRowMigrationExample: React.ComponentType = () => (
<OverviewEngine {...otherProps} rowActionStyling={rowActionStylingPerRow} />
);
98

-- 98 of 114 --

All rows (was RowActionState.rowActions)
Before
const rowActionState: OverviewEngineApi.RowActionState = {
rowActions: { myEvent: { disabled: true } }
};
<OverviewEngine rowActionState={rowActionState} ... />
After
const rowActionStylingAllRows: OverviewEngineApi.RowActionStyling = ({ button }) => {
if (button.event === "myEvent") {
return { disabled: true };
}
return undefined;
};
export const AllRowsMigrationExample: React.ComponentType = () => (
<OverviewEngine {...otherProps} rowActionStyling={rowActionStylingAllRows} />
);
NOTE Wrap rowActionStyling with useCallback, its reference is included in a dependency
array inside the engine.
If you referenced RowActionState.IndividualRowActionState as a type, update to
RowActionStyling.IndividualRowActionState.
Row Selection
Selection now carries an optional linkId on each entry in RowsSelectedPayload.documentsSelection.
No action is required unless custom selection logic relied on document-id uniqueness.
onOverallMultiSelectionButtonClick signature change
The onOverallMultiSelectionButtonClick callback signature has changed from { affectedRowIds,
selected } to an array of per-row selection entries { documentId, linkId?, selected }[]. This aligns
it with onRowsSelect and can handle duplicated rows keyed by linkId.
Before
onOverallMultiSelectionButtonClick({ affectedRowIds: ["1", "2"], selected: true });
After
onOverallMultiSelectionButtonClick([
{ documentId: "1", linkId: undefined, selected: true },
99

-- 99 of 114 --

{ documentId: "2", linkId: undefined, selected: true }
]);
2025.06-ext4
38.2.0
Data Services configuration properties
Overview Engine now reads Data Services configuration properties from the Data Services slice (for
example mgmtp.a12.dataservices.query.simpleSearch.minSearchableTokenSize). In case the slice is
missing, some UI features (such as search and enumerated string filter options) may behave
incorrectly and a warning is logged.
To enable the configuration lookup:
1. Add the Data Services configuration reducer during app setup.
2. Load the configuration once after the application is initialized.
Reducer map + configuration load
import { DataServicesReducerMap, loadDataServicesConfiguration } from
"@com.mgmtp.a12.dataservices/dataservices-access";
const { store } = ApplicationFactories.createApplicationSetup({
// ...
reducerMap: { ...DataServicesReducerMap }
});
await loadDataServicesConfiguration(store);
If you are using the new composable setup, simply include the withDataServicesConfigurationSlice
feature as a prerequisite of the withOverviewEngine feature:
Composable setup
import { withDataServicesConfigurationSlice } from "@com.mgmtp.a12.client/client-
core/dataServicesAdapter";
import { withOverviewEngine } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
const { store, initialActions } = createA12ApplicationSetup(
combineFeatures(
withDataServicesConfigurationSlice,
withOverviewEngine,
// ...
)(initialConfig)
);
100

-- 100 of 114 --

Deprecation of nested imports
Nested imports are deprecated in favor of top-level imports to avoid unnecessary breaking changes
caused by moving or renaming internal files. This makes the code more resilient to internal
refactoring, provides a single consistent import path, and reduces ongoing maintenance effort.
Run the codemod command below to migrate automatically:
npx @com.mgmtp.a12.overviewengine/overviewengine-codemod prefer-top-level-imports
./client/tsconfig.json
Deprecation of top level OverviewModel entities
All top-level type exports from overview-model.ts are deprecated in favor of namespace-scoped
exports under OverviewModel. The following types and enums should now be accessed via the
OverviewModel namespace:
Deprecated export New export
Content OverviewModel.Content
Configuration OverviewModel.Configuration
ContextMenu OverviewModel.ContextMenu
ActionGroup OverviewModel.ActionGroup
ColumnRef OverviewModel.ColumnRef
Styles OverviewModel.Styles
Width OverviewModel.Width
PinDirection OverviewModel.PinDirection
Column OverviewModel.Column
ReferenceColumn OverviewModel.ReferenceColumn
Summary OverviewModel.Summary
ExpressionColumn OverviewModel.ExpressionColumn
BaseColumn OverviewModel.BaseColumn
SubHeaderBox OverviewModel.SubHeaderBox
FooterBox OverviewModel.FooterBox
Element OverviewModel.Element
BaseElement OverviewModel.BaseElement
ButtonElement OverviewModel.ButtonElement
MultiSelectionElement OverviewModel.MultiSelectionElement
SearchElement OverviewModel.SearchElement
FilterElement OverviewModel.FilterElement
ElementType OverviewModel.ElementType
SectionItem OverviewModel.SectionItem
FilterConfiguration OverviewModel.FilterConfiguration
101

-- 101 of 114 --

Deprecated export New export
FilterMode OverviewModel.FilterMode
EnumeratedStringFilterConfiguration OverviewModel.EnumeratedStringFilterConfigurat
ion
FieldConfiguration OverviewModel.FieldConfiguration
RowActionGroup OverviewModel.RowActionGroup
ConfirmationText OverviewModel.ConfirmationText
Triggerable OverviewModel.Triggerable
ContextMenuItem OverviewModel.ContextMenuItem
Button OverviewModel.Button
Annotated OverviewModel.Annotated
Icon OverviewModel.Icon
ColumnAlignment OverviewModel.ColumnAlignment
ColumnStyles OverviewModel.ColumnStyles
Alignment OverviewModel.Alignment
HorizontalAlignment OverviewModel.HorizontalAlignment
VerticalAlignment OverviewModel.VerticalAlignment
MultiSelection OverviewModel.MultiSelection
AttachmentDisplayMode OverviewModel.AttachmentDisplayMode
MultiSelectDisplayMode OverviewModel.MultiSelectDisplayMode
IconTheme OverviewModel.IconTheme
Migration example:
Before
import { Content, Column, FilterMode, Button } from
"@com.mgmtp.a12.overviewengine/overviewengine-core/lib/main/overview-model.js";
const content: Content = { /* ... */ };
const column: Column = { /* ... */ };
const mode: FilterMode = FilterMode.ALL;
After
import { OverviewModel } from "@com.mgmtp.a12.overviewengine/overviewengine-core";
const content: OverviewModel.Content = { /* ... */ };
const column: OverviewModel.Column = { /* ... */ };
const mode: OverviewModel.FilterMode = OverviewModel.FilterMode.ALL;
Deprecation of Pagination type
The Pagination type is deprecated in favor of PaginationState for better naming clarity and
102

-- 102 of 114 --

consistency.
Deprecated export New export
Pagination PaginationState
Migration example:
Before
import { Pagination } from "@com.mgmtp.a12.overviewengine/overviewengine-
core/lib/main/store/index.js";
const pagination: Pagination = { pageNumber: 0, pageSize: 10 };
After
import { PaginationState } from "@com.mgmtp.a12.overviewengine/overviewengine-core";
const pagination: PaginationState = { pageNumber: 0, pageSize: 10 };
2025.06
38.0.0
CAUTION Please have a look at Migration to latest A12 chapter for an explanation of
general steps.
Migration to ESM
The npm artifacts @com.mgmtp.a12.overviewengine/overviewengine-core and
@com.mgmtp.a12.overviewengine/overviewengine-model-migration were migrated from CommonJS to
ESM. When using Node 22.12+ and modern build tools, there should be no changes necessary to
your bundler setup.
NOTE If your tests depend on mocking/stubbing Overview Engine API, consult the
documentation of your test runner on how to work with ES modules.
Migrating your own application to ESM is not required, but recommended. Consult the
documentation of your bundler for specifics.
Updating to ES2024
The javascript output of the npm artifacts was updated from ES2020 to ES2024 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
103

-- 103 of 114 --

React 19 upgrade
We dropped the support for React 18 and older and now require React 19 as our peerDependency.
This means you have to perform the React 19 migration, which is described in great detail in the
official React 19 Upgrade Guide. They have codemods that should make the transition
straightforward.
Additionally, we have decided to not also include a Redux update, to minimize the migration effort.
The "react-redux" library does not have React 19 as a peerDependency, but still works fine with it.
One solution is to override the dependency in your package.json. You also have to update the
corresponding "@types/react-redux" typings to at least 7.1.34.
{
"overrides": {
"react-redux": {
"react": "^19"
}
},
"devDependencies": {
"@types/react-redux": 7.1.34
}
}
Styled-components v6 upgrade
We dropped the support for styled-components v5 and now require v6 as our peerDependency.
Please refer to the styled-components guide and Widgets migration notes for more information.
Breaking Changes
Change Infinity Scroll APIs
The interface of UiState.scrolling property of the OverviewEngineState has been changed in this
release.
Old Scrolling
export interface Scrolling {
readonly start: number;
readonly numberOfRows: number;
readonly visibleStart: number; // Range of visible rows - start point
readonly visibleEnd: number; // Range of visible rows - end point
}
It has been changed to support the new Query API paging feature.
New Scrolling
export interface Scrolling {
104

-- 104 of 114 --

readonly pageSize: number;
readonly pageNumbers: number[];
readonly visibleStart: number; // Range of visible rows - start point
readonly visibleEnd: number; // Range of visible rows - end point
}
Change in InfiniteScrollOptions
The loadData callback of the infiniteScrollOptions prop in the OverviewEngine component now
receives a new object parameter which contains these following fields: startPage and endPage.
The new behavior is to load data for a specific range of pages, instead of loading data for a specific
range of rows.
Old loadData callback
export interface InfiniteScrollOptions {
// ... other properties
loadData(start: number, numberOfRows: number): Promise<void>;
}
New loadData callback
export interface InfiniteScrollOptions {
// ... other properties
loadData(params: { startPage: number; endPage: number; }): Promise<void>;
}
Overview Engine will calculate the total number of items to load pageSize * (endPage - startPage +
1). More information can be found in the Infinite Scroll.
Query API integration
This version only ships Query API support; interfaces and functions that allow the non-Query API
usage have been removed or replaced, including:
Removed functions Alternative
OverviewEngineFactories.createModule({queryAPI
: true})
OverviewEngineFactories.createModule({})
OverviewEngineFactories.createQueryAPIDataProv
iders
OverviewEngineFactories.createDataProviders
QueryAPIFieldBasedFiltering.toOperators FieldBasedFiltering.toOperators
QueryAPIAggregation.AggregationResolver AggregationResolver
QueryAPIAggregation.createSumAggregation AggregationResolver.create
Fields projection
Follow the Query API integration, fields projection is now enabled by default, allow the engine to
105

-- 105 of 114 --

only load the relevant fields. In case of needing additional fields, follow a look at the
OverviewEngineDataLoader and its customization section to add more fields to the default list.
Remove OverviewEngineActions.updateEnumeratedStringDataHolder
The action OverviewEngineActions.updateEnumeratedStringDataHolder is no longer used and removed
in this release as a consequence.
Use slash instead of dot for FilterMap fieldPath
The fieldPath in FilterMap used dot (.) as the separator, which is not compatible with ModelPath
and Query API. Now the separator is changed to slash (/).
FilterMap is used to define the Preset Filter for the Overview Engine.
Old FilterMap
const presetFilter: OverviewEngineApi.FilterMap = {
"product.name": {
filterType: "String",
criteria: {
value: "board"
},
nonRemovable: true
},
"product.logistics.weight.weightValue": {
filterType: "Number"
}
};
It has to be transformed to use slash (/) to adapt with this change.
New FilterMap
const presetFilter: OverviewEngineApi.FilterMap = {
"/product/name": {
filterType: "String",
criteria: {
value: "board"
},
nonRemovable: true
},
"/product/logistics/weight/weightValue": {
filterType: "Number"
}
};
Embedded Attachments API removed
The embedded attachments related API has been removed, the engine now solely relies on the
106

-- 106 of 114 --

thumbnails slice to render the thumbnail. Unless embedded attachment is used, it is expected to
have no impact to the current setup. However, it case embedded attachment is used, to restore
functionality, the newly introduced selectorMap will be used to remap the new default thumbnail
accessing logic.
import {
DefaultSelectorMap,
type SelectorMap
} from "@com.mgmtp.a12.overviewengine/overviewengine-
core/lib/main/view/configuration/selector-map.js";
const CustomSelectorMap: SelectorMap = {
...DefaultSelectorMap,
attachmentThumbnail: (attachment) => {
return (state) =>
attachment.content?.startsWith("data:image/")
? attachment.content
: DefaultSelectorMap.attachmentThumbnail(attachment)(state);
}
}
By default, the thumbnail selector assumes that thumbnails for attachments can be queried from
the thumbnails slice by their id. However, for embedded attachment implementation, the image
source can be simply accessed by directly read the content prop.
The selectorMap can then be passed into any OverviewEngine view component to override the
default behavior.
Sorting behavior
From 2025.06 version, sorting behavior is changed.
• Case is ignored during sorting (ignoreCase is set to true).
• If no default sorting is specified, results are sorted by __meta/createdAt in descending order.
Drop deprecated APIs
OverviewEngineViewViews
The namespace OverviewEngineViewViews is no longer used and has been removed. This change
includes the removal of the following:
• OverviewEngineViewViews.OverviewEngineView
• OverviewEngineViewViews.OverviewEngineViewProps
• ConnectedOverviewWrapperProps.onDocumentButtonClick
• ConnectedOverviewWrapperProps.onDocumentButtonDoubleClick
More information can be found in the 2024.06’s deprecation section.
107

-- 107 of 114 --

OverviewEngineState
The OverviewEngineState has been changed in this release. The following properties have been
removed:
• sorting
• filter
• rowState
More information can be found in the 2024.06’s deprecation section.
OverviewEngine Props
Several properties of the OverviewEngine have been changed in this release. The following props
have been removed:
• rowState
• disabled
• searchString
• sorting
• pagination
• fieldBasedFiltering.activeFilters
• fieldBasedFiltering.enumeratedStringFilterMap
• fieldBasedFiltering.timeMode
More information can be found in the 2024.06’s deprecation section.
NOTE
For fieldBasedFiltering.timeMode, the time mode customization can be done via the
timeMode property of the OverviewEngine component. For more details, refer to
custom time format.
OverviewEngineActions
The OverviewEngineActions have been changed in this release. The following actions have been
removed:
• OverviewEngineActions.queryParametersChanged
• OverviewEngineActions.documentsSelectionChanged
• OverviewEngineActions.documentMultiSelectionClear
More information can be found in the 2024.06’s deprecation section.
OverviewEngineSelectors
The OverviewEngineSelectors have been changed in this release. The following selectors have been
removed:
108

-- 108 of 114 --

• OverviewEngineSelectors.getState
• OverviewEngineSelectors.getStateWithoutDefaults
More information can be found in the 2024.06’s deprecation section.
Model Changes
Drop migration support for models before version 36.0.0
The migration tool no longer supports migration of Overview models before version 36.0.0. If you
need to migrate models from an older version, you need to migrate to version 36.0.0 first using the
migration tool of that version.
Make showFullTextSearch optional
The field showFullTextSearch of the Overview Model Configuration interface has been changed from
a required boolean to an optional boolean.
Since undefined and false are both falsy, existing checks should continue to work without
modification. If more explicit handling is needed, use the nullish coalescing operator:
const showFullTextSearch = overviewModel.content.configuration.showFullTextSearch ??
false;
if (showFullTextSearch) {
// Logic when full text search is enabled
}
Rename fieldIDs within FilterConfiguration to fields and change its type from an array of
string to an array of object
This change applies to all fieldIDs within FilterConfiguration, including:
• FilterConfiguration["fieldIDs"]
• SectionItem["fieldIDs"]
• EnumeratedStringFilterConfiguration["fieldIDs"]
New Model Interface
export interface SectionItem {
// other properties
readonly fields: ReadonlyArray<FieldConfiguration>;
}
export interface FilterConfiguration {
// other properties
readonly fields?: ReadonlyArray<FieldConfiguration>;
}
export interface EnumeratedStringFilterConfiguration {
109

-- 109 of 114 --

// other properties
readonly fields: ReadonlyArray<FieldConfiguration>;
}
export interface FieldConfiguration {
readonly fieldID: string;
}
Migration Tool
To migrate Overview Model files, first install or update the migration tool with
npm install -g @com.mgmtp.a12.overviewengine/overviewengine-model-migration
Then run the following command to perform the migration
overview-model-migration <path to overview model file or directory> --backup
Examples
# file
overview-model-migration my-overview-model.json --backup
# folder
overview-model-migration . --backup
Note
• The minimum supported model version is 37.0.0. Models older than this must first be migrated
to version 37.0.0 using that version’s migration tool.
• If <path to overview model file or directory> is a directory, the migration tool will recursively
search for Overview Model files to migrate.
• If Overview Model files are not under version control, use --backup (alias -b) flag to create
backups for model files. This flag is optional.
• Use --help (alias -h) flag to show all available options.
Model migrations
This section documents what the tool changes in an Overview Model when migrating to a given
engine version — the change categories it applies, with at least one named example each — so you
can map the resulting diff to the documented intent. Each entry is also referenced from the
matching subsection of the migration instructions.
110

-- 110 of 114 --

39.0.0
Rename model fields. SubHeaderBox/FooterBox majorElements becomes rightSlot and minorElements
becomes leftSlot; ButtonType.rank values change from major/minor to left/right.
// before
{ "majorElements": [/* ... */], "minorElements": [/* ... */] }
// after
{ "rightSlot": [/* ... */], "leftSlot": [/* ... */] }
Rewrite the row-action shape. The legacy default-row-action is rewritten to the new rowActivation
event shape.
// before
"defaultRowAction": { "custom": true, "event": "open" }
// after
"rowActivation": { "type": "event", "event": "open" }
Codemod
Introduction
The Overview Engine Codemod is a command-line tool for running automated code
transformations (codemods) on TypeScript projects. Codemods assist with codebase migrations by
automatically applying breaking changes, deprecations, and API updates—reducing manual effort
and minimizing human error during upgrades.
Installation
The codemod can be executed directly using npx or pnpm dlx without requiring a permanent
installation:
npx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest <recipe-id-or-version>
<tsconfig-path> [options]
pnpm dlx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest <recipe-id-or-
version> <tsconfig-path> [options]
Usage
The codemod supports two modes of operation, plus an interactive variant:
1. Recipe-based execution — run a specific recipe by its identifier: <recipe-id> <tsconfig-path>.
2. Version-based migration — run all recipes whose supported range includes a target version:
111

-- 111 of 114 --

<target-version> <tsconfig-path>. The tool selects the applicable recipes automatically.
3. Interactive mode (--interactive) — pick a recipe or target version through guided prompts.
See Examples for concrete commands and Arguments / Options for the full CLI surface.
Arguments
Argument Description
<recipe-id-or-version> Either the identifier of a specific codemod recipe to execute, or a target
version number (e.g., 1.2.0, 38.0.0) to run all applicable recipes. Use
--list to view available recipes and their supported versions.
<tsconfig-path> Path to a tsconfig.json file or a directory containing one. Accepts both
absolute and relative paths (relative to the current working directory).
Options
Option Default Description
--list, -l false Lists all available codemod recipes along with their
supported version ranges and descriptions.
--interactive, -i false Launches interactive mode, allowing you to select a
recipe or specify a target version through guided
prompts.
--git-check true Verifies that the git working directory is clean before
execution. If uncommitted changes are detected, you
will be prompted to confirm before proceeding.
--help — Displays CLI help information including usage syntax,
available options, and examples.
Examples
List Available Recipes
Display all available codemod recipes with their supported versions:
npx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest --list
Run a Single Recipe
Execute a specific recipe on a project:
npx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest prefer-top-level-
imports ./client/tsconfig.json
112

-- 112 of 114 --

Migrate to a Specific Version
Run all codemods required to migrate to version 38.0.0:
npx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest 38.0.0 ./tsconfig.json
Skip Git Check
Run a codemod without verifying the git working directory state:
npx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest 38.0.0 ./tsconfig.json
--no-git-check
Interactive Mode
Launch the interactive interface for guided execution:
npx @com.mgmtp.a12.overviewengine/overviewengine-codemod@latest -i
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
This section documents what each recipe changes in a consumer project — the categories of
transformation it applies, with at least one named example per category — so you can map the
resulting diff to the documented intent rather than reverse-engineering it. Run --list for the
authoritative set of recipes and their supported version ranges.
prefer-top-level-imports
Supported versions ^39.0.0 (2026.06)
Required / optional Required — nested lib imports and the removed type aliases are compile
errors in 39.0.0.
113

-- 113 of 114 --

Transforms TypeScript source so that all Overview Engine API is consumed through the top-level
package entry point. Change categories:
Rewrite import paths. Nested deep imports are rewritten to the top-level entry.
// before
import { OverviewModel } from "@com.mgmtp.a12.overviewengine/overviewengine-
core/lib/main/overview-model.js";
// after
import { OverviewModel } from "@com.mgmtp.a12.overviewengine/overviewengine-core";
Rewrite removed type aliases. Deprecated top-level OverviewModel aliases (e.g. Content, Column,
FilterMode) are rewritten to the OverviewModel namespace, and Pagination to PaginationState.
// before
const content: Content = { /* ... */ };
const pagination: Pagination = { pageNumber: 0, pageSize: 10 };
// after
const content: OverviewModel.Content = { /* ... */ };
const pagination: PaginationState = { pageNumber: 0, pageSize: 10 };
114

-- 114 of 114 --

