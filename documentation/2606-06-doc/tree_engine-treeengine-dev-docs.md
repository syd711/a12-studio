# tree_engine treeengine dev docs

Tree Engine
Introduction
Tree Engine includes model driven UI components based on Widgets library. It provides a
convenient way to set up a full-featured tree view through models configuration and programming
interfaces.
This documentation would describe in details about features and the integration of Tree Engine
into an existing product.
Getting Started
Prerequisite
Client
Basic knowledge of using Client is necessary because Tree Engine heavily depends on Client. See
Frontend tutorial for further details.
For technical requirements, Tree Engine shared the same configuration as Client. If Client has been
running, Tree Engine should be able to use.
Please refer to the full-stack-project-template repository for an up-to-date template.
Relationship
Tree Engine only works if there is at least a relationship between entities. Otherwise, Overview
Engine can serve well. Having knowledge about how relationship works in A12 ecosystem is a
must.
Simple Model Editor (SME)
SME is a tool that allows managing all models in A12 ecosystem. See SME documentation for
further details.
Every A12 component will come with an ability to manage business requirements via a model. Tree
Engine is not an exception in which SME is utilized to manage Tree Model.
Installation
Tree Engine is provided as an npm package published as ES Modules (ESM). Run the following
command to install Tree Engine:
1

-- 1 of 81 --

npm install @com.mgmtp.a12.treeengine/treeengine-core
WARNING It is assumed that the project is bootstrapped with Client.
After that, it is possible to start integrating Tree Engine into the application.
Register Tree Engine modules
The registration process of Tree Engine is quite simple, it is only required to register two Tree
Engine modules (TreeEngineFactories and TreeEngineServerConnectorFactories).
src/appsetup.ts
import { TreeEngineFactories, TreeEngineServerConnectorFactories } from
"@com.mgmtp.a12.treeengine/treeengine-core";
export function setup() {
ModuleRegistryProvider.getInstance().addModule(TreeEngineFactories.createModule());
ModuleRegistryProvider.getInstance().addModule(TreeEngineServerConnectorFactories.crea
teModule());
return ApplicationFactories.createApplicationSetup({ ... })
}
CAUTION See Module Registry for further information about registering modules in a
Client application.
Register Tree Engine in non-modular way
However, in case tree engine modules are not a preferable way to setup the application,
TreeEngine’s factories also allow registering in a non-modular way as below:
NOTE Look for "Tree Engine" comment to find out which part of the
createApplicationSetup to register the Tree Engine
WARNING
In general, only Tree Engine’s data providers needed to be placed above Form
Engine’s data provider. For the rest of the registration (data reducers,
middlewares, sagas, view component), the order does not matter.
src/appsetup.ts
export function setup() {
...
const dataHandlers = [
// Tree Engine data providers
TreeEngineFactories.createDataProvider(),
2

-- 2 of 81 --

TreeEngineServerConnectorFactories.createDataProvider(teDataProviderSetting),
...RelationshipEngineFactories.createDataProviders(),
...OverviewEngineFactories.createDataProviders(),
createEmptyDocumentDataProvider(),
platformSingleDocumentDataProvider
];
config = ApplicationFactories.createApplicationSetup({
model: APPLICATION_MODEL_PLACEHOLDER, // not used
dataHandlers,
modelLoader: createPlatformServerModelLoader({ modelProcessors:
[FormModelProcessor] }),
dataReducers: [
// Tree Engine data dataReducers
...TreeEngineFactories.createDataReducers(),
...RelationshipEngineFactories.createDataReducers(),
...OverviewEngineFactories.createDataReducers(),
...formEngineDataReducers
],
overridePlatformSagas: [
...OverviewEngineFactories.createApplicationSagas(),
...DirtyHandlingFactories.createSagas()
],
additionalMiddlewares: [
// Tree Engine middlewares
...TreeEngineFactories.createMiddlewares(),
...RelationshipEngineFactories.createMiddlewares(),
...createFormEngineMiddlewares(),
...OverviewEngineFactories.createMiddlewares(),
CRUDFactories.createCRUDMiddleware()
],
customSagas: [
...appCustomSagas,
// Tree Engine sagas
...TreeEngineFactories.createSagas(sagaSetting),
...formEngineSagas({ attachmentLoader: platformAttachmentLoader }),
...CRUDFactories.createSagas(),
...RelationshipEngineFactories.createSagas(),
...DeepLinkingFactories.createSagas({
applyTriggers: [ModelActions.setModelGraph]
})
],
reducerMap: { ...DataServicesReducerMap },
composeEnhancer: createComposeEnhancer()
});
3

-- 3 of 81 --

return config;
}
Register Container Factory
There are three ways to register the Tree Engine renderer:
Default Module
The default module, shipped by TreeEngineFactories.createModule(), provides the default one,
named TreeEngine. It should work by following registration steps for a standard Client module.
Default ViewComponentProvider
In case of not using TreeEngine’s module, it is possible to include Tree Engine’s view component
provider
src/defaultContainerFactory.tsx
import { TreeEngineFactories } from "@com.mgmtp.a12.treeengine/treeengine-core";
function viewComponentProvider(componentName: string) {
return TreeEngineFactories.viewComponentProvider(componentName);
}
This provider also provides Tree Engine view component named TreeEngine, which will be
available to be used in the Application Model.
Customized ViewComponentProvider
This approach will allow more flexibility, it is possible to not only change container’s name but also
injecting customized configuration props to the view, even able to replace Tree Engine View
component with a different one:
src/customContainerFactory.tsx
import type { View } from "@com.mgmtp.a12.client/client-core";
import { CRUDViews } from "@com.mgmtp.a12.crud/crud-core";
import { TreeEngineFactories, type RowStyleGetter } from
"@com.mgmtp.a12.treeengine/treeengine-core";
export function createViewProvider(): (componentName: string) =>
React.ComponentType<View> | undefined {
const components: { [name: string]: React.ComponentType<View> | undefined } = {
// CRUD views
OverviewCRUD: CRUDViews.OverviewEngineView,
FormCRUD: CRUDViews.FormEngineView,
// Default Tree Engine view component
TreeCRUD: TreeEngineFactories.ViewComponent,
// Customized Tree components
4

-- 4 of 81 --

TeamTree: TeamTree,
NonDnDTree: (props) => <TreeEngineFactories.ViewComponent {...props}
dndConfiguration={false} />
};
return function provider(name) {
return components[name];
};
}
function TeamTree(props: View) {
const rowStyling: RowStyleGetter = React.useCallback(({ row }) => {
return { disabled: row.data.nodeIdentifier.type === "DomainPerson" };
}, []);
return <TreeEngineFactories.ViewComponent {...props} rowStyling={rowStyling} />;
}
Drag and Drop Provider
Since version 7.0.0, ComponentMap no longer supports DndProvider. Therefore, if the Tree Engine
requires any drag & drop functionality (enabled by default via tree model), it is mandatory to have
a DndProvider registered as the highest order component as follow:
src/app/page/index.tsx
import { DndProvider } from "react-dnd";
import { DragAndDropUtils } from "@com.mgmtp.a12.widgets/widgets-core";
export const BasePage = () => (
<DndProvider
backend={DragAndDropUtils.DefaultDndBackend}
options={DragAndDropUtils.DefaultDndBackendOptions}
>
<LocalizerContext.Provider value={{ locale, dataFormats, localizer, conversion }}>
...
</LocalizerContext.Provider>
</DndProvider>
);
Enable JSON-RPC
JSON-RPC is a standard that can be used to replace batch endpoint and expose methods to be called
via network in a more standardized way. JSON-RPC provides a schema for the request, response
and description of standardized error messages.
Tree Engine uses it to establish the connection to A12 Data Services. Therefore, please ensure JSON-
RPC is enabled by having mgmtp.a12.dataservices.jsonRpc.enabled=true property which configured
on A12 Data Services properties.
5

-- 5 of 81 --

Tree Model
Tree Model can be created and modelled by SME. For more details, please refer to SME
documentation for Tree Model.
Application Model
Please use SME Application Model or Client's TypeScript interface to structure the Application
Model. The scope of this documentation only covers SME method.
Tree Engine Scene
Firstly, upload workspace to SME. In this section, it is assumed that the Application Model has been
set up to work with a basic CRUD workflow (with Form Engine, Overview Engine and Relationship
Engine).
Then open the module that needs to integrate Tree Engine and add a menu with this state
configuration.
Figure 1. Tree Engine menu in Application Model
TIP
The key model is optional and does not have to exactly match the model name, it exists
to make sure this menu is pointing to the correct scene which is configured in
6

-- 6 of 81 --

Application Model scenes.
Then go into the module flow and add a new Tree Engine scene, here is an example of how the
scene looks like:
Figure 2. Tree Engine scene in Application Model
TIP The REGION_CLEAR directive is optional.
CAUTION The Match Conditions section is very important. The key model should match the
one defined in menu.
On Scene Change section, add a directive to On Enter event.
7

-- 7 of 81 --

Figure 3. Tree Engine VIEW_ADD directive in Application Model
TIP The VIEW_ADD should point to the registered Tree Engine container, which is TreeCRUD in
this case.
CAUTION The Models section is very important. Tree Model name must be chosen from the
selection box. Please leave Document Model field empty.
The rest is configurable, it is possible to change and customize based on business requirements.
After that, save the Tree Engine scene. The following figure is an example of a flow used in Tree
Engine showcase application:
8

-- 8 of 81 --

Figure 4. Tree Engine flow in Application Model
Connect to Other Engines
Tree Engine exposes a list of sagas via TreeEngineFactories.createSagas() which simplify connection
process of the Tree Engine to other engines.
CAUTION
These sagas heavily depend on the Match Conditions in the Application Model. It
is possible to strictly follow the guideline or unregister unnecessary ones during
application setup then build a custom one.
Overview Engine
WARNING We do not support Overview Engine because there has not been any use case
whether Tree Engine should connect to Overview Engine.
Form Engine
In order to connect to the existing Form Engine scene, its Match Conditions should be:
9

-- 9 of 81 --

Figure 5. Form Engine scene in Application Model
TIP The key model in the Match Conditions section can be removed and defined inside the
Models section of the VIEW_ADD directive (where Form Model mapping is done).
Relationship Engine
Relationship Engine has two types of display:
• Relationship Engine inside Form Engine
• Standalone Relationship Engine
Both types of Relationship Engine share the same configuration with a usual Form Engine.
10

-- 10 of 81 --

However, for the Standalone Relationship Engine, which will be opened via the "event_add_link", it
will come with an extra descriptor which is "engine" ⇒ "relationship". This descriptor can be used
in the match condition configuration to differentiate between 2 variants.
Editing Link Document
If the Relationship Model has a Link Document, it is required to add a scene to handle the Link
Document. The match condition configuration is the same as Form Engine but the model has to
point to the Link Document Model instead of the Document Model.
Example of link Form Engine scene:
Figure 6. Link Form Engine scene in Application Model
11

-- 11 of 81 --

TIP To achieve the same behavior of editing Link Document as Relationship Engine, the
scene should be rendered in MODAL region instead of CONTENT region.
Example of VIEW_ADD directive:
Figure 7. Link Form Engine VIEW ADD in Application Model
Activity Descriptors
Tree Engine provides its own CRUD-like functionality for creating, reading, updating, and deleting
tree nodes and link documents, independently of the CRUD component. When Tree Engine opens a
child activity, it constructs the activity descriptor by spreading the current tree activity descriptor
and adding scenario-specific keys. The Application Model Match Conditions of the target scene must
align with these keys to ensure the correct scene is activated.
NOTE
Current state documentation. Tree Engine implements CRUD-like functionality
(node creation, editing, link management) independently of the CRUD component.
The descriptor conventions described here reflect this independent
implementation. A future Master Detail Component (A12-15815) will unify these
conventions across engines. Until then, this section is the authoritative reference for
12

-- 12 of 81 --

Tree Engine’s current activity descriptor behavior.
NOTE
engine: "tree" in the tree master activity descriptor is optional and was made so in
v9.0.0 (A12TE-280). In the current iteration, projects do not need it for the engine to
work correctly.
Scenario Added / overridden keys Matching tip
View / edit existing
node
model (node’s document model ref),
instance (node’s document ID)
Match on model to route to the correct
Form Engine scene.
Create new node
(child, sibling, or
root)
model (document model ref of the new
node), instance: "NEW"
instance: "NEW" identifies a new,
unsaved document; match on model to
route to the correct Form Engine
scene.
Add link
(event_add_link)
model (target node’s document type),
instance (target node’s ID), engine:
"relationship"
Match on engine: "relationship" to
distinguish the Standalone
Relationship Engine from a plain form
activity.
Edit link
document
model (link document model ref),
instance (link document ID, or NEW for
a new document), linkForm: "true"
Match on linkForm: "true" to
distinguish the link document form
from a regular edit activity.
NOTE
With the new Relationship Engine integration, child and sibling node creation
additionally propagate parentInstance, parentRelationshipName, and
parentRelationshipRole (sibling creation also propagates predecessor) into the
descriptor, to combine create document and link process into one step. These keys
are extra payload, Application Model match conditions do not need to include them.
NOTE
When using a SME-generated Master Details Model, the engine key in activity
descriptors originates from the MDM-generated initial activity descriptor, not from
Tree Engine itself. See CRUD’s Activity Descriptors: With Master Details Model for
the match condition requirements in that context.
TIP See Connect to Other Engines for concrete Application Model match condition
examples for Form Engine, Relationship Engine, and link document scenes.
Error Handling
Error System
To manage the exceptions, Tree Engine uses a semantic error system which is extended from native
Javascript Error and Activity.Error.Base interface. Therefore, besides the typical properties of
Javascript Error, every Tree Engine error, which is an instance of TreeEngineError, also contains
some helpful fields such as:
13

-- 13 of 81 --

• errorCode: TreeEngineErrorCode enum value to describe and identify the error.
• details?.subError: The nested error if exists.
• details?.causeAction: The Redux action causes the error.
They can be categorized into three groups:
• Generic errors: are thrown from missing (NotFoundError) or invalid (TypeError) entities -
activities, data holders, or Tree-specific entities, etc.
• Loading/saving operation errors: are caused from end-user actions such as ExpandNodeError,
CopyNodeError, etc.
• ServerError: are come from the server-side and contains an extra errors field - the list of A12
DataService’s JsonRpc2ResponseError instances.
All relevant classes, instances and typings/type guards are hosted by TreeEngineError namespace
along with the TreeEngineErrorCode enumeration.
Error Catching
In order to handle errors dispatched from Tree Engine activities, registering a saga that listens to
ActivityActions.Error action which is dispatched for every error occurred. Since every public Tree-
specific error also attaches an isInstance type guard, so we can handle distinct errors in different
ways, such as pushing a notification, displaying a message box, etc.
In the following example, notifications will be dispatched whenever Tree Engine errors are caught.
src/sagas/handleErrorSaga.tsx
export function* handleErrorSaga(): SagaGenerator<void> {
yield* takeEvery((action: unknown) => isAction(action) &&
ActivityActions.error.match(action), handle);
}
function* handle(action: Action<ActivityActions.ErrorPayload>): SagaGenerator<void> {
const { activityId, error } = action.payload;
const addNotificationEffects = [];
if (TreeEngineError.ServerError.isInstance(error)) {
const errors = error.errors.slice(0, 1);
errors.forEach((error) => {
const exception = JsonRpc2Response.Exception.isInstance(error.data) ?
error.data : undefined;
const notificationAction = NotificationActions.add({
activityId,
severity: "error",
title: {
key: exception?.description.default ??
SHOWCASE_RESOURCE_KEYS.showcase.error.server.title,
defaults: { en: exception?.title.default }
},
14

-- 14 of 81 --

message: {
key: exception?.description.default ??
SHOWCASE_RESOURCE_KEYS.showcase.error.server.message,
defaults: { en: exception?.description.default }
}
});
addNotificationEffects.push(put(notificationAction));
});
} else if (TreeEngineError.isInstance(error)) {
const notificationAction = NotificationActions.add({
activityId,
severity: "error",
title: {
key: `tree.engine.showcase.error.${error.errorCode}.title`,
defaults: { en: error.errorCode.split("_").map(toCapitalize).join(" ")
}
},
message: {
key: `tree.engine.showcase.error.${error.errorCode}.message`,
defaults: { en: error.message }
}
});
addNotificationEffects.push(put(notificationAction));
}
yield* all(addNotificationEffects);
}
Then, putting it in the customSagas when calling createApplicationSetup in order to register the saga.
src/appsetup.ts
config = ApplicationFactories.createApplicationSetup({
...
customSagas: [
handleErrorSaga, // your error handling saga should be put here.
...
],
...
});
Basic
API Overview
View Component
TreeEngineFactories.ViewComponent is the main component used to render Tree Engine. It is a
15

-- 15 of 81 --

React component with the following props:
TreeEngineFactories.ViewComponent Props
export interface OwnProps {
/**
* A map of components is used to override the components in Tree Engine.
* The components are expected to have rendering logic based on the Tree model,
Tree Engine state and so on.
*/
readonly componentMap?: ComponentMap;
/**
* A map of Widgets components used in Tree Engine.
* These components are expected to focus on the UI.
* Therefore, they are recommended when some UI customizations need to be applied.
*/
readonly widgetMap?: WidgetMap;
/**
* Map of selectors that can be customized.
* Note that this map will only be expanded as needed
*/
readonly selectorMap?: SelectorMap;
/**
* A configurable object that configures the drag and drop feature.
*/
readonly dndConfiguration?: DndConfiguration | false;
/**
* Thumbnails map
*/
readonly thumbnails?: Record<string, string>;
/**
* The callback controls the state (e.g: visibility,...) of node action buttons.
*/
readonly rowActionStateGetter?: RowActionStateGetter;
/**
* The callback controls the style (e.g: interactive,...) of a node.
*/
readonly rowStyling?: RowStyleGetter;
/**
* An array of key combinations and target configurations which supports keyboard
shortcuts for certain common actions.
*/
readonly keyboardShortcuts?: KeyboardShortcut[];
16

-- 16 of 81 --

/**
* A property which defines the window size for Tree Engine renderers.
*/
readonly smallView?: boolean;
/**
* A property which defines the busy state for Tree Engine application.
*/
readonly busy?: boolean;
/**
* A property which defines the `aria-level` attribute for Tree Engine components.
*/
readonly ariaLevel?: number;
/**
* A property which defines the id prefix for Tree Engine component.
*/
readonly uiIdPrefix?: string;
}
Identifier
A Tree Engine is constructed with nodes and links which are A12 documents.To identify and
distinguish these documents, Tree Engine introduces Identifier.
The Identifier of a document consists of two fields:
• id: The unique ID of the document.
• type: The document name.
Node Path
To locate a node, Tree Engine uses node path defined by TreeEngineState.NodePath interface.
A node path is constructed from an array of Identifier. However, there are additional constraints
applied to the array:
• The first entry is the identifier of the root node where the path starts.
• The following identifiers are the link identifiers which indicate the link connecting a node to its
next node on the path.
View Customization
Tree Engine supports a way to override the rendering logic in most components to adapt to
business requirements. It is necessary to understand how Container Factory works in Client to start
with.
Then create a custom React component that returns the default view
17

-- 17 of 81 --

TreeEngineFactories.ViewComponent. As from now, the custom ComponentMap, WidgetMap or
row action state getter can be passed as props to this component.
Initialize UI State
Tree Engine provides a way to initialize the UI state of the engine. This is useful when you want to
restore the UI state from a previous session or set a initial UI state for the engine.
initial-tree-engine-with-customized-ui-state.example.ts
import { TreeEngineActions } from "@com.mgmtp.a12.treeengine/treeengine-core";
const activityPushAction = TreeEngineActions.createActivity(
{ activityDescriptor: {
// The desired activity descriptor
} },
{ readonly: true, initialExpansion: { type: "all_levels" } }
);
store.dispatch(activityPushAction)
Customize Component Map
Component Map is a list of components provided by Tree Engine. Most of these components focus
on the logic of Tree Engine, not the UI logic.
Below is an example of customizing the Component Map.
src/views/custom-a12-team-tree-engine.tsx
function View(props) {
...
const componentMap: ComponentMap = React.useMemo(() => {
return {
...DefaultComponentMap,
DocumentBodyCell: (documentBodyCellProps) => {
const rowState = useTreeEngineRowContext((context) =>
context.rowState);
let className = "";
if (rowState?.node?.identifier.type === "DomainTeam") {
className = addPrefix("-u-font-bold");
}
return <DefaultComponentMap.DocumentBodyCell
{...documentBodyCellProps} className={className} />;
},
BodyCellUIValue: (bodyCellUIValueProps) => {
const { documentModelPath } = bodyCellUIValueProps;
if (ModelPath.equal(documentModelPath,
ModelPath.fromString("Person/PersonalData/FirstName"))) {
return (
18

-- 18 of 81 --

<DefaultComponentMap.BodyCellUIValue
{...bodyCellUIValueProps}
className={addPrefix("-u-background-purple-light")}
/>
);
}
return <DefaultComponentMap.BodyCellUIValue {...bodyCellUIValueProps}
/>;
},
/**
* By default, the {@link HeterogeneousInsertChildNodeDialog} is always
returned when running under Client context.
* Below example show a way to render your own Insert child node dialog
component
*/
InsertChildNodeDialog: (dialogProps) => {
const onDialogConfirmed = useTreeEngineContext((context) =>
context.eventHandlers.onDialogConfirmed);
const { options, insertPosition, button } = dialogProps.dialogState;
return (
<DefaultWidgetMap.ModalOverlay>
<ActionContentbox padding="12px"
headingElements={<ContentBoxElements.Title text="Insert child dialog" />}>
<DefaultWidgetMap.List>
{options.map(({ documentModelId,
childRelationshipConfiguration }) => {
const customDocumentModelId =
documentModelId.startsWith("DomainPerson")
? "DomainPerson"
: documentModelId;
return (
<DefaultWidgetMap.ListItem
key={customDocumentModelId}
onClick={() =>
onDialogConfirmed({
payload: {
type:
TreeEngineState.Dialog.Type.INSERT_CHILD_NODE,
documentModelId:
customDocumentModelId,
insertPosition,
childRelationshipConfiguration,
button
}
})
}
text={customDocumentModelId}
/>
);
19

-- 19 of 81 --

})}
</DefaultWidgetMap.List>
</ActionContentbox>
</DefaultWidgetMap.ModalOverlay>
);
},
InitialViewBody: () => {
const dispatch = useDispatch();
const ContextMenu = useTreeEngineContext((context) =>
context.componentMap.ContextMenu);
const contextMenuModel = useInitialViewContextMenuModel();
return contextMenuModel ? (
<DefaultWidgetMap.Message className={addPrefix("-u-height-full -u
-flex -u-flex-col -u-items-center")}>
<ContextMenu
contextMenuModel={contextMenuModel}
row={RootNodeRow.create()}
triggerElement={
<DefaultWidgetMap.Button
label={"Add a new element"}
icon={<DefaultWidgetMap.Icon>add_circle</DefaultWidgetMap.Icon>}
/>
}
/>
or
<DefaultWidgetMap.Button
label={"Paste from Clipboard"}
onClick={() => {
dispatch({
...NotificationActions.add({
severity: "info",
title: { key:
LOCALE_RESOURCE_KEYS.application.title },
message: { key:
SHOWCASE_RESOURCE_KEYS.showcase.button.pasteFromClipboard }
})
});
}}
/>
</DefaultWidgetMap.Message>
) : null;
}
};
}, []);
...
return <TreeEngineFactories.ViewComponent {...props} componentMap={componentMap}/>
}
20

-- 20 of 81 --

TIP Spread the DefaultComponentMap on the custom ComponentMap to avoid re-write
everything from scratch.
TIP See ComponentMap interface in the Typedoc to find out supported components.
After that, register a new container to Container Factory with a name that Application Model can
later reference.
The above example is applied to Tree Engine showcase application - A12 Team Tree example.
Customize Widget Map
Widget Map is a list of Widgets components that Tree Engine uses (e.g. Content Box, Button Group,
Tree Table…). Most of these components focus on the UI logic.
Customizing Widget Map is similar to Component Map. For example:
src/views/custom-a12-team-tree-engine.tsx
function View(props) {
...
const widgetMap: WidgetMap = React.useMemo(() => {
return {
...DefaultWidgetMap,
TreeTable: (treeTableProps) => {
const componentRenderers:
Partial<TreeTableComponentRenderers<FlattenNodeRow>> = React.useMemo(
() => ({
...treeTableProps.componentRenderers,
headRowRenderer: (params) => {
return
DefaultTreeTableComponentRenderers.headRowRenderer({
...params,
style: { background: "lavender" }
});
}
}),
[treeTableProps.componentRenderers]
);
return <TreeTable {...treeTableProps}
componentRenderers={componentRenderers} />;
}
};
}, []);
...
return <TreeEngineFactories.ViewComponent {...props} widgetMap={widgetMap}/>
}
21

-- 21 of 81 --

TIP Spread the DefaultWidgetMap on the custom WidgetMap to avoid re-write everything
from scratch.
TIP See WidgetMap interface in the Typedoc to find out supported components.
Customize Row Action State Getter
Row Action State Getter is a callback used by Tree Engine to query the state of a specific row action.
It receives an object parameter containing the row action and the row rendering that row action.
Based on that, the callback should return an object of IndividualRowActionState type, which applies
to the row action.
IndividualRowActionState type provides the visibility and disability settings for row actions. For
example:
src/views/custom-a12-team-tree-engine.tsx
function View(props) {
...
const dataStateSelector = React.useMemo(() => {
return TreeEngineSelectors.dataState(props.activityId);
}, [props.activityId]);
const data = useSelector((state: object) => dataStateSelector(state)?.data);
const rowActionStateGetter: RowActionStateGetter = React.useCallback(
({ row, action }) => {
if (!data) {
return {};
}
let hidden = false,
disabled = false;
const node = TreeDataUtils.readNodeData(data, row.data.nodeIdentifier);
if (node?.identifier.type === "DomainPerson") {
const married = get(node.document, "Person.PersonalData.Married");
if (married === true && action.type === "event" && action.event ===
"event_delete_link") {
hidden = true;
}
const confirmed = get(node.document, "Person.PersonalData.Confirm");
if (confirmed === true && action.type === "event" && action.event ===
EDIT_NODE_EVENT) {
disabled = true;
}
} else if (node?.identifier.type === "DomainTeam") {
const teamName = get(node.document, "TeamDetails.TeamName");
if (teamName === "UP" && action.type === "event") {
hidden = action.event === "event_copy";
disabled = action.event === INFO_NODE_EVENT;
22

-- 22 of 81 --

}
if (teamName === "Engines" && action.type === "insert") {
disabled = true;
}
}
return { hidden, disabled };
},
[data]
);
...
return <TreeEngineFactories.ViewComponent {...props}
rowActionStateGetter={rowActionStateGetter}/>
}
Engine Id Prefix
At the moment, model name, set in Tree Model header details, is used as id for different
components (e.g. tree table, button…).
In case there are many engines of the same model on a screen, this leads to incorrect component
identification. Since version 7.2.0, the introduction of uiIdPrefix property allows a flexible way to
add a prefix to the engine id, which could eliminate the above issue.
Customize Row Styling
Row styling is a callback used by Tree Engine to query the style of a specific row. It receives an
object parameter that contains the row that would be rendered. Based on that, the callback should
return an object of TreeTableRowStyles type, which applies to the row.
TreeTableRowStyles, which belongs to Widgets library, has the following structure:
TreeTableRowStyles interface
export interface TreeTableRowStyles extends RowStyles {
/**
* Whether the tree node is collapsed.
*/
collapsed?: boolean;
}
/**
* Collections of style values for the row
*/
export interface RowStyles extends Styleable {
title?: string;
selected?: boolean;
interactive?: boolean;
disabled?: boolean;
23

-- 23 of 81 --

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
The following example demonstrates how to set interactive status of specific rows.
src/views/custom-a12-team-tree-engine.tsx
function View(props) {
...
const rowStyling: RowStyleGetter = React.useCallback(
({ row }) => {
if (!data) {
return {};
}
const node = TreeDataUtils.readNodeData(data, row.data.nodeIdentifier);
if (node?.identifier.type === "DomainPerson") {
const firstName = get(node.document, "Person.PersonalData.FirstName");
if (firstName === "Nicolas" || firstName === "Levi") {
return { interactive: false };
}
}
return {};
},
[data]
);
...
return <TreeEngineFactories.ViewComponent {...props} rowStyling={rowStyling}/>
}
Tips
It is recommended to pass the memorized props that only re-initiate when their dependencies are
changed (useMemo/useCallback is a perfect fit for this case). Since we pass ComponentMap, WidgetMap
24

-- 24 of 81 --

and RowActionStateGetter to every component in Tree Engine, re-initiate a new instance every
render may cause unnecessary re-render. See How to memoize calculations?.
Tree Engine Actions
The engine actions are divided into two types: events and commands.
Events signal that something has happened in the UI, triggered by a user interaction. For example a
click on a button or drag & drop a row. They are handled by middlewares and will never change the
state directly. They can be dispatched by users, for example "Events.reload" to reload a specific
subtree with configurable depth. Developers are also encouraged to listen to them, for example to
"Events.onNodeExpansionChanged" to get notified about a tree node being expanded.
Commands are used to directly modify the Redux state. They are dispatched by other
Events/Commands and are usually implemented in reducers. Users are encouraged to dispatch
them, for example "Commands.setDisabled" to disable the UI, but are NOT encouraged to listen to
them. Which commands are dispatched in what order and by what user interaction is considered
an implementation detail and a change is not considered breaking.
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
each action does and what user interaction leads to the action. For a more details e.g.: action’s
payloads, please refer to the API documentation.
Event Description Dispatched by
onNodeExpansionChanged expand a node a click event on the expansion
arrow
onNodeSelectionChanged select a node a click event on a node row
onInsertChildNodeRequest request to insert a new child for
a node
a click event on a row’s event
button
onInsertSiblingNodeRequest request to insert a new sibling
for a node
a click event on a row’s event
button
25

-- 25 of 81 --

Event Description Dispatched by
onInsertRootNodeRequest request to insert a new root for
the tree
a click event on a
subheader/footer’s event button
onEventButtonClickedRequest request a confirmation dialog if
configured by the button
a click event on a
subheader/footer’s event button
onEventButtonClicked no usage, lets users react to
button clicks
a click event on a
subheader/footer’s event button
onMultiSelectionButtonClicked toggle the multi-selection state
of the tree
a click event on the
collapse/expand multi-selection
section
onMultiSelectionEventButtonCli
ckedRequest
request a confirmation dialog if
configured by the button
a click event on multi-selection
section’s button
onMultiSelectionEventButtonCli
cked
no usage, lets users react to
button clicks
a click event on multi-selection
section’s button
onOverallMultiSelectionClicked select every possible nodes on
the tree for multi-selection
a click event on checkbox in the
multi-selection column header
onNodeMultiSelectionClicked select specific nodes on the tree
for multi-selection (this could
sometimes result a whole
subtree being multi-selected,
depending on the node type)
a click event on multi-selection
checkbox of a row
onNodeRangeSelectionClicked select multiple nodes on the
tree for multi-selection
a click event on multi-selection
checkbox of a row, but with
extra modifier key (e.g.: Shift)
onNodeEventButtonClickedReq
uest
request a confirmation dialog if
configured by the button
a click event on a row’s event
button
onNodeEventButtonClicked no usage, lets users react to
button clicks
a click event on a row’s event
button
onDialogClosed close the dialog a close button of the dialog
onDialogConfirmed confirm the dialog a confirm button of the dialog
onDndStarted start drag and drop operation a drag event on a row
onDndDone finish drag and drop operation a drop event on a row
onBulkDndDone start bulk drag and drop
operation in combination with
multi-selection
a drag event on a row
onDndHover for dynamically expanding a
parent node during a drag and
drop operation
a hover event on a row during
drag and drop operation
onMakeRootNodeRequest request to make a row as root
node
a drag and drop operation
where a node is dropped as root
26

-- 26 of 81 --

Event Description Dispatched by
onRowClicked no usage, lets users react to
button clicks
a click event on a row
onColumnWidthsChanged resize the columns width a resize event on resize handler
between column’s headers
scrollToNode See scroll to node
reload See reloading
reloadAll See reloading
onLoadMore Load more children for a node
(required pagination enabled)
a click event on the load more
button of a row
onLoadAll Load all children for a node
(required pagination enabled)
a click event on the load all
button of a row
revalidateClipboard Revalidate the clipboard to
ensure consistency of engine’s
clipboard
a node is added to or removed
from the tree
Commands
All direct state changes are coming from these actions. However, it is not recommended to listen to
these actions directly. What Commands are dispatched at what time and in what order is
considered an implementation detail and may change any time in a non-breaking way.
It is possible to register custom reducers to take care about how information is stored. But be aware
that the engine components always need the store in a special structure to be able to render!
The following table shows an overview of all Commands. For a detailed description, including the
specific payloads, please refer to the API documentation.
Commands Description State Change
setSelectedNodes set selected nodes state "selectedNodes" in "uiState"
setExpandedMultiSelectionPane
l
set expansion state of multi-
selection panel
"expandedMultiSelectionPanel"
in "uiState"
setMultiSelectionNodes set multi-selection nodes "multiSelectionNodes" in
"uiState"
setExpandedNodes set expanded nodes "expandedNodes" in "uiState"
setScrollToNode set node for the UI to trigger the
scrolling animation
"scrollToNode" in "uiState"
setDialogState set the dialog information "dialog" in "uiState"
setColumnWidths set the width of each table’s
column
"columnWidths" in "uiState"
setCopiedNodes prepare clipboard nodes for the
paste operation
"clipboard" in "uiState"
27

-- 27 of 81 --

Commands Description State Change
setCutNodes prepare clipboard nodes for the
paste operation
"clipboard" in "uiState"
resetClipboard empty the clipboard "clipboard" in "uiState"
setPreloadChildNodes set preload child node behavior "preloadChildNodes" in
"uiState"
setDisabled set disabled state for the engine "disabled" in "uiState"
setReadonly set readonly state for the engine "readonly" in "uiState"
Client Actions
The commands and events are specific to the engine and are not aware of the context where it
belongs to. This can be a problem when multiple instances of an engine are required, resulting in
conflict between instances. Therefore, the TreeEngineActions namespace exists to allow adding
context to the core’s events and commands, e.g. activityId and other Client’s related information.
The following table shows an overview of every action. For a detailed description, including the
specific payloads, please refer to the API documentation.
Actions Description
TreeEngineActions.createActivity Action to create a Tree Engine activity with
option to include initial UI state
TreeEngineActions.event A wrapper action for Events while also includes
the activityId
TreeEngineActions.command A wrapper action for Commands while also
includes the activityId
TreeEngineActions.editLinkDocument Async action which is used for editing the link
document via external form
TreeEngineActions.setDataHolders Action which is used for applying the changes to
the tree data
Dispatch an Engine Action
In an usual A12 application, the engine runtime will NOT be able to react if only a bare event or
command action is dispatched. Those events or commands, which can be seen on Redux Devtool,
are usually nested in a client action. This internally allows some engine’s core features set to be
separated from being dependent on the activity.
Therefore, to control the engine’s runtime behaviors, it is necessary to be aware of this extra layer
before dispatching an action.
dispatching-actions.ts
import { Commands, Events, TreeEngineActions } from
28

-- 28 of 81 --

"@com.mgmtp.a12.treeengine/treeengine-core";
// Middleware
const customMiddleware: Middleware = (api) => (next) => (action) => {
// ...other logics
api.dispatch(TreeEngineActions.event({
activityId: "MY_ACTIVITY_ID",
engineAction: Events.onNodeExpansionChanged({ nodeIdentifier:
targetNodeIdentifier, nodePath: targetNodePath })
}))
}
// Saga
function* customSagaHandler(): SagaGenerator<void> {
// ...other logics
yield put(TreeEngineActions.command({
activityId: "MY_ACTIVITY_ID",
engineAction: Commands.setDisabled({ disabled: true })
}))
}
Handle Custom Events
The Tree Engine button system is configured from several places in the Tree Model:
• Subheader/Footer Box: from content.(subheader|footerBox) parts in the model.
• Node Actions/Context Menu: from content.nodes[*].(actions|contextMenu) parts in the model.
The following figure shows how buttons are placed on the application:
Figure 8. Tree Engine Actions
Tree Engine also provides a built-in right-clicked context menu that helps end users interact with
rows via its actions easily on the row, instead of moving to the end.
29

-- 29 of 81 --

Figure 9. Right-clicked context menu
Each button listens to a specific event, following sections will show how to handle certain use cases.
Handle Custom Subheader/Footer Event
Subheader/Footer actions can be handled by listening to onEventButtonClicked event. In the
following example, a saga is responsible to handle a subheader button which is used to restore the
previous activity state.
handle-back-button-saga.ts
export function* handleBackButtonSaga(): SagaGenerator<void> {
yield* takeLatest((action: unknown) => {
return (
isAction(action) &&
TreeEngineActions.event.match(action) &&
Events.onEventButtonClicked.match(action.payload.engineAction) &&
[BACK_ENGINE_EVENT].includes(action.payload.engineAction.payload.button.event)
);
}, handle);
}
function* handle(action: Action<ActivityActions.ActivityActionPayload>) {
const { activityId } = action.payload;
const suspendingActivity = yield* select((state) => state.suspendingActivity);
if (suspendingActivity) {
yield* put(restoreActivityAction({ activity: suspendingActivity }));
30

-- 30 of 81 --

} else {
yield* put(ActivityActions.cancel({ activityId }));
}
}
There are default CRUD event names that have been supported already, see more in Default Actions
in SME.
Handle Custom Node Event
Node actions can be handled by listening to the onNodeEventButtonClicked event. In the following
example, a saga is responsible to handle a node action button with the edit event.
handle-edit-event-saga.ts
export function* handleEditEventSaga(): SagaGenerator<void> {
yield* takeEvery((action: unknown) => {
return (
isAction(action) &&
TreeEngineActions.event.match(action) &&
Events.onNodeEventButtonClicked.match(action.payload.engineAction) &&
action.payload.engineAction.payload.button.event === EDIT_NODE_EVENT
);
}, handle);
}
function* handle(
action:
Action<TreeEngineActions.EventPayload<Action<Events.NodeEventButtonClickedPayload>>>
): SagaGenerator<void> {
const { engineAction, activityId } = action.payload;
const { nodeIdentifier, nodePath } = engineAction.payload;
yield* put(
TreeEngineActions.event({
activityId,
engineAction: Events.onNodeSelectionChanged({ nodeIdentifier, nodePath,
selected: true })
})
);
}
NodeEventButtonClickedPayload contains nodeIdentifier and nodePath, which can be used to
retrieve information of the current node.
Handle Custom Row Selection Event
The default action when a row is clicked in Tree Engine is to view/edit the selected node. This
default behavior can be changed via the rowActivation property on a node type in the Tree Model.
See Row Activation for the full list of available types and configuration options.
31

-- 31 of 81 --

The following example shows how to handle custom row action:
handle-custom-row-action-saga.ts
export function* handleCustomRowActionSaga(): SagaGenerator<void> {
yield* takeEvery(
(action: unknown) =>
isAction(action) &&
TreeEngineActions.event.match(action) &&
Events.onRowClicked.match(action.payload.engineAction),
handle
);
}
function* handle(
action: Action<TreeEngineActions.EventPayload<Action<Events.RowClickedPayload>>>
): SagaGenerator<void> {
const { engineAction, activityId } = action.payload;
if (engineAction.payload.event === "selectPerson") {
const notificationAction = NotificationActions.add({
activityId,
severity: "info",
title: { key:
SHOWCASE_RESOURCE_KEYS.showcase.a12Teams.customRowAction.selectPerson.title },
message: { key:
SHOWCASE_RESOURCE_KEYS.showcase.a12Teams.customRowAction.selectPerson.message }
});
yield* put(notificationAction);
}
}
In this example, onRowClicked event is listened, then check the condition to handle the custom row
action event selectPerson. Absolutely nodeIdentifier and nodePath, which can be used to retrieve
the information of current node, are provided.
Multi-Selection
Tree Engine provides a multi-selection feature that allows users to select multiple rows and
perform actions on those selected rows.
The multi-selection area is a dedicated part of each row that users can click to toggle the row’s
selection state. It is configurable through the model and supports the following options:
• Rows and checkboxes (default) — both row clicks and checkboxes can toggle selection.
• Checkboxes only — selection is managed exclusively via checkboxes.
Users can select rows in two ways:
• Single selection: Click on a row’s selection area to toggle its selection.
32

-- 32 of 81 --

• Range selection: Hold Shift and click another row’s selection area to select a consecutive range
of rows.
For model configuration, please see SME documentation.
Components
Figure 10. Multi-selection feature
Some multi-selection components need to be explained:
• Multi-selection panel: the gray section including components [1], [2], and [3].
• Multi-selection button [1]: to enable/disable the feature, as well as expand/collapse the multi-
selection panel.
• Multi-selection counter [2]: to indicate the number of selected nodes.
• Multi-selection event buttons [3]: to perform bulk operations. Their modelling configurations
are similar to normal Tree event button.
• Overall checkbox [4]: to indicate the overall multi-selection state of the whole tree.
• Node checkboxes: to indicate the current multi-selection state of a node, including: partly
selected [5], selected [6], or deselected [7].
Performing the Bulk Operation
Firstly, the multi-selection button’s event name should be configured in the Tree Model. Then, the
action could be listened to by watching onMultiSelectionEventButtonClicked event. For example:
src/sagas/watch-nodes-deletion-saga.ts
function* watchNodesDeletionSaga(activityId: string): SagaGenerator<void> {
yield* takeLatest((action: AnyAction) => {
return (
TreeEngineActions.event.match(action) &&
Events.onMultiSelectionEventButtonClicked.match(action.payload.engineAction) &&
action.payload.activityId === activityId &&
33

-- 33 of 81 --

action.payload.engineAction.payload.button.event === "event_delete_nodes"
);
}, handle);
}
function* handle(action:
Action<TreeEngineActions.EventPayload<Action<Events.MultiSelectionEventButtonClickedPa
yload>>>) {
...
}
Localization
An application using Tree Engine has to provide a LocalizerContext instance from utils-
localization-react package. The context can be initialized by using
DefaultLocalizerContextProvider, or customized completely by passing three parameters: localizer,
locale, and dataFormats into LocalizerContext. localizer receives an array of Localizables and
returns resolved string according to the current locale. For more details, see Utils localization
documentation.
NOTE By default, Tree Engine only provides texts for two locales: en and de. For other
locales, they can be defined via models or a customized localizer.
API
Tree Engine offers two alternative localization approaches:
• In which the React hooks are available, LocalizerHooks namespace with five different hooks
can be used:
◦ useLocalizedResource: to translate Tree Engine resources via public RESOURCE_KEYS.
◦ useLocalizedTreeElement: to translate localized texts in Tree Model’s elements.
◦ useLocalizedEnumerationValue: to translate labels of EnumerationType values.
◦ useLocalizedBooleanValue: to translate labels of BooleanType values.
◦ useLocalizedConfirmValue: to translate labels of ConfirmType values.
• In which the React hooks are not available, LocalizableFactory namespace with alternative
functions can be used to create corresponding localizables.
Keys
Every localizable element in Tree Engine is identified by a key. The keys can be categorized into the
static resource keys and model element keys.
Static Resource Keys
Resources such as titles for notifications and confirmation buttons are marked using static keys.
The static keys are defined and well-documented in RESOURCE_KEYS in
34

-- 34 of 81 --

core/services/localization/languages/keys.ts. Please see the file for more details.
Model Element Keys
The keys for elements rendered based on models such as buttons and column header labels are
dynamically generated. The schemas for the keys are as follows:
Element Key
Table header label/Aria
label
uiModel.{tree-model-name}.header.labels
Column header label uiModel.{tree-model-name}.columns.{column-id}
Sub-header button uiModel.{tree-model-name}.subHeaderBox.buttons.(label|title).{button-
id}
Footer button uiModel.{tree-model-name}.footerBox.buttons.(label|title).{button-id}
Node event row action uiModel.{tree-model-name}.nodes.{node-id}.actions.event.{event-
name}.(label|title)
Node insert row action uiModel.{tree-model-name}.nodes.{node-id}.actions.insert.(label|title)
Node event context
menu action
uiModel.{tree-model-name}.nodes.{node-id}.contextMenu.event.{event-
name}.(label|title)
Node insert context
menu action
uiModel.{tree-model-name}.nodes.{node-
id}.contextMenu.insert.(label|title)
Context menu group
title
uiModel.{tree-model-name}.nodes.{node-id}.contextMenu.groups.{group-
name}.title
Confirmation dialog uiModel.{tree-model-name}.nodes.actions.event.confirmation.{event-
name}
Enumeration values documentModel.enumValue.{document-model-name}.{…element-
path}.{enumeration-field-value}
Boolean values documentModel.boolean.{document-model-name}.{…element-
path}.(true|false)
RESOURCE_KEYS.(true|false)
Confirm values documentModel.confirm.{document-model-name}.{…element-
path}.(true|null)
RESOURCE_KEYS.(true|null)
Conversion
The Tree Engine utilizes the Conversion API from the utils-localization package to format value
from document data to rendered UI values, considering the current locale and data formats.
The utils-localization ValueConversion interface has two methods but:
• parseValue (not used by Tree Engine): converts the rendered UI value to the document value.
• formatValue: converts the document value to the rendered UI value.
When formatting a document value, the Tree Engine calls the formatValue method of the contextual
35

-- 35 of 81 --

conversion, which implements the ValueConversion interface and is extracted from LocalizerContext.
This method is provided with the value to be converted, along with relevant information such as
the Document Model name and the model path to the field or group. Depending on the element
type, additional information may be passed, such as minFractionalDigits for NumberType elements
and format for DateType elements.
For more details, please refer to the utils-localization documentation.
To customize the conversion logic, a custom implementation of the Conversion API can be
provided. For example, to customize the format of a DateType field named DateOfBirth in the
PersonDM Document Model:
Conversion customization
import { LocalizerContext, type LocalizerContextProps } from
"@com.mgmtp.a12.utils/utils-localization-react";
import {
defaultDataFormats,
defaultLocalizerFactory,
defaultValueConversion,
type ValueConversion
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
return dayjs(value).format("dd MMM YYYY");
}
return defaultConversion.formatValue(value, outputFormat);
}
};
const localizer = defaultLocalizerFactory({ locale, conversion, dataFormats
});
return { locale, dataFormats, conversion, localizer };
}, [locale]);
36

-- 36 of 81 --

return <LocalizerContext.Provider value={localizerContextValue} />;
};
Keyboard Shortcuts
Tree Engine supports keyboard shortcuts for certain common actions (e.g. expand the whole tree,
copy/paste nodes).
src/view.ts
import * as KeyCode from "keycode-js";
export const CustomViewComponent: React.FC<TreeEngineFactories.ViewComponentProps> =
(props) => {
const keyboardShortcuts: KeyboardShortcut[] = [
{
keyCombinations: [{ eventCode: KeyCode.CODE_E }],
target: {
type: KeyboardShortcut.TargetType.ENGINE_BUILTIN_ACTION,
action: KeyboardShortcut.EngineBuiltinAction.EXPAND_WHOLE_TREE
}
},
{
keyCombinations: [{ modifierKeys: [KeyboardShortcut.ModifierKey.Ctrl],
eventCode: KeyCode.CODE_C }],
target: {
type: KeyboardShortcut.TargetType.NODE_EVENT_ACTION,
event: "event_copy_node"
}
}
];
return <TreeEngineFactories.ViewComponent {...props}
keyboardShortcuts={keyboardShortcuts} />;
};
API
KeyboardShortcut interface has the following properties which are used to control what and how
shortcuts are handled:
KeyboardShortcut interface
export interface KeyboardShortcut {
/**
* The action will be executed
*/
readonly target: KeyboardShortcut.Target;
37

-- 37 of 81 --

/**
* A list of key combinations will trigger the corresponding target action if
possible
*/
readonly keyCombinations: KeyboardShortcut.KeyCombination[];
/**
* Used to display next to the action in the context menu. If not specified, the
default one will be used.
*/
readonly label?: string;
/**
* The appended text for the action's title. If not specified, the default one
will be used.
*/
readonly title?: string;
/**
* Used to check if a warning message, which is related to the failure in handling
keyboard shortcuts, should be shown to users.
*/
readonly stopIfUnavailable?:
| true
| Localizable
| ((params: { node?: TreeEngineState.Node; link?: TreeEngineState.Link }) =>
Localizable);
}
Key Combination
KeyboardShortcut.KeyCombination interface, which is a simple form of Web API KeyboardEvent,
contains two fields:
• eventCode: identical to KeyboardEvent’s code property. It represents a physical key on the
keyboard as string, and is not affected by the modifier keys such as Shift, Alt,… The Engine will
compare it to the listened event’s code to determine the equality. The keycode-js package can be
used here for type safety.
NOTE
The code of number keys in a standard keyboard have different values, e.g. Digit4
and Numpad4. It is also true for operational keys in the number keyboard block and
each pair of modifier keys (e.g. ShiftLeft versus ShiftRight)
• modifierKeys (optional): a list of unique modifier keys: Shift, Ctrl, Alt and Meta (on Mac
keyboards, the Command ⌘ key; on Windows keyboards, the Windows ⊞ key). They will be compared
to the corresponding properties: shiftKey, ctrlKey,… of the listened KeyboardEvent.
38

-- 38 of 81 --

Target
In general, a target action can be triggered by keyboard shortcuts if users can trigger it by mouse.
So the following situations will not work:
• Register shortcuts for non-modeling buttons (in subheader/footer bar or nodes).
• Trigger shortcuts for hidden/disabled row actions (e.g. through RowActionState API).
• Trigger node multi-selection shortcut when the multi-selection mode is disabled.
There are six types of target which belongs to two scopes:
• Engine scope:
◦ EngineBuiltinActionTarget: built-in actions such as expand/collapse multi-selection panel
or the whole tree,…
◦ EngineEventActionTarget: subheader, footer or multi-selection action buttons.
◦ EngineInsertActionTarget: the insert actions belong to the virtual root.
• Node scope:
◦ NodeBuiltinActionTarget: built-in actions such as toggle multi-selection state, expand or
collapse,…
◦ NodeEventActionTarget: event node actions.
◦ NodeInsertActionTarget: insert node actions.
Two BuiltinActionTarget can be declared through the corresponding enums: EngineBuiltinAction or
NodeBuiltinAction. Similarly, EventActionTarget uses the event field of the action as its identifier. In
case of InsertActionTarget, the identifier is documentModelRef field.
Engine scope will run whenever a Tree element is focused. If that element is a row, the Node scope
will be executed first. The keyboard event will stop right there if any node action is triggered.
Otherwise, (e.g., the action is hidden or disabled), the event will be bubbled up to the Engine scope.
Handling Keyboard Shortcut Events
When a keyboard shortcut event is triggered from a row, the Node scope is considered to handle it
before the Engine scope.
This event is used to extract targets (see Keyboard shortcut target) from the configured keyboard
shortcuts.
These targets are subsequently used to check if the corresponding actions are modeled for current
node.
• In case the modeled action for current node is not hidden or disabled. The handler is executed
and event propagation stops.
• In case the modeled action for current node is hidden or disabled. Keyboard shortcuts with the
same target are checked if stopIfUnavailable property is defined.
◦ If stopIfUnavailable is defined: A warning toast is shown to user and event propagation
39

-- 39 of 81 --

stops.
◦ If stopIfUnavailable is not defined: The event is bubbled to engine scope.
• In case there are not any modeled action for current node, the event is bubbled to Engine scope.
Engine scope would continue to handle the bubbled event, that node scope could not solve, using
similar logical steps.
The process of handling keyboard shortcut events is summarized as the following figure.
40

-- 40 of 81 --

Figure 11. Keyboard shortcuts handling flow
Scrolling to a Node
Tree Engine supports scrolling to a node through the action Events.scrollToNode. The action
payload contains node path of the scroll target, and corresponding identifiers of nodes in that path:
41

-- 41 of 81 --

ScrollToNodePayload
interface ScrollToNodePayload {
nodePath: TreeEngineState.NodePath;
nodesFromNodePath?: Identifier[];
autoFocus?: boolean;
}
NOTE For further information, see Node Path and Identifier sections.
If the node is not visible because it is not loaded yet or nested in collapsed nodes, Tree Engine will
try to reveal the node. To do that, it loads and expands all the nodes along the way to the specified
node based on the provided node path, then scroll to it.
By default, if only nodePath is provided, Tree Engine will try to expand level by level until the path is
finished, loading this way might lead to generating too many network requests if the nodePath is too
long. By providing nodesFromNodePath, Tree Engine will be able to generate only one single batch
request to load all required nodes along the path then attempt to scroll to the target node.
NOTE
The nodesFromNodePath can be simply retrieved by using the
DataSelector.nodesFromNodePath. This selector only works when the full path to
the node is revealed/loaded, otherwise, undefined shall be returned.
NodesFromNodePath
import { DataSelector } from "@com.mgmtp.a12.treeengine/treeengine-core";
const { nodePath } = action.payload;
const nodesFromNodePath = DataSelector.nodesFromNodePath(nodePath)(engineState);
Reloading
Tree Engine enables reloading subtrees by providing the action Events.reload. By dispatching this
action, multiple subtrees can be reloaded at once.
Events.reload and Events.reloadAll payload interface are as follows:
Reload action payload
interface ReloadPayload {
/**
* Identifiers of source nodes whose subtree to be reloaded
*/
sources: Identifier[];
/**
* Define a limit on how many levels to be reloaded
* @default Infinity travel until reaching the leaf nodes
*/
42

-- 42 of 81 --

level?: number;
}
interface ReloadAllPayload {
/**
* Define a limit on how many levels to be reloaded
* @default Infinity travel until reaching the leaf nodes
*/
level?: number;
}
An action payload of this type contains a list of sources, each source represents the root of each
subtree. An empty array sources will result no reloading. However, in case sources are not provided
hence a reloadAll action, the engine will attempt to reload the whole tree starting from the root
nodes.
By default, the action will recursively travel and reload until every leaf node of each subtree is
reached. Defining a level shall limit how many levels to be reloaded, potentially prevent a
resource-intensive reloading action:
• 0: reload only the sources, resulting in GET_DOCUMENT operations to be requested. Providing 0 will
only reload the provided sources, which expects only the source document to be updated. If the
source document is deleted or removed from the subtree beforehand, the subtrees will not be
updated properly. 0 is not a valid value for a reloadAll action.
• 1: reload the direct children of every provided source, this always result in LIST_LINKS
operations to be requested.
• For every increased number of level, the traveling depth is also increased. The Engine will
automatically stop when the leaf is reached regardless of the pre-defined level.
• The GET_DOCUMENT operation will be used only when 0 is provided, otherwise it is always the
LIST_LINKS operation.
INFO
The operations listed above assume a setup using the default data loader. For a custom
implementation of TreeEngineDataLoader, Tree Engine’s GetDocument query handler should
also be included.
Tree Engine Data Loader
Tree Engine comes with a default A12 Server Connector that provides the data fetching/modifying
logic based on the A12 standard setup. However, in some cases, where A12 standard setup does not
provide sufficient features or extension points, developers would want to adapt either server-side
or client-side code to fulfill their needs.
Generally, it would be simpler to adapt the server-side implementation. However, in case adaption
cannot be done on the server-side, Tree Engine also offers a possibility to do it on the client-side.
43

-- 43 of 81 --

Use Cases
There are several use cases that customizing the default Tree Engine Data Loader can come in
handy:
• Have a connection to the Data Services instance but some minor adjustments are needed. For
example: Filter the root node(s), fetch from a different endpoint for a specific query…
• Have no connection to Data Services instance. For example: In SME (Simple Model Editor), most
of the connections and data storage are done on the client-side.
• Have a connection to the backend server, but it is a completely different instance of Data
Services.
Default Data Loader
Figure 12. Tree Engine Data Loader flow
From the above figure, A12DataServiceDataLoader is responsible for sending/receiving the
request/response from Data Services instance. When being triggered, the data loader will be given
44

-- 44 of 81 --

with a single or a list of transformed Tree Engine operation(s) which consist of:
• A list of queries.
• A list of mutations.
After transforming queries & mutations into HTTP request(s), a response is expected to be returned
from the server. A12DataServiceDataLoader will process the response into the compatible data
structure for further manipulation in Tree Engine.
Query
Any data loading/fetching in Tree Engine always resolves into a list of queries. A query can be in
different types, below are some of the most important query types that Tree Engine supports:
• ListRootNodes: Fetches the root nodes of the tree.
• ListChildNodes: Fetches the child nodes of a specific parent node.
• TreeNodes: Fetches a list of nodes for multi-level loading strategy.
Other than that, a query also includes:
• ID.
• Relationship Model.
• Computed roles (parent, child).
• Target Document Model name.
• An optional parent source.
• An optional list of fields to be retrieved (field projection).
NOTE More information can be found in DataOperation.Query API documentation.
Below are several examples of how a query is created by Tree Engine:
ListRootNodes without a parent source
const listRootNodes: DataOperation.Query.ListRootNodes.Query = {
id: "list-root-nodes",
type: "LIST_ROOT_NODES",
relationshipModel: "TeamTeam",
roles: {
parent: "Parent",
child: "Child"
},
targetDocumentModel: "DomainTeam"
};
ListRootNodes with a parent source or ListChildNodes
const listChildNodes: DataOperation.Query.ListChildNodes.Query = {
45

-- 45 of 81 --

id: "list-child-nodes",
type: "LIST_CHILD_NODES",
relationshipModel: "DirectoryFile",
source: "DomainDirectory/9bd9169a-a8c9-44be-85af-d65ed6ed8e72",
roles: {
parent: "Directory",
child: "File"
},
targetDocumentModel: "DomainFile"
};
TreeNodes
const treeNodes: DataOperation.Query.TreeNodes.Query = {
id: "tree-nodes",
type: "TREE_NODES",
entry: { targetDocumentModel: "DomainCategory", relationshipModel:
"CategoryCategory", parentRole: "Parent" },
links: [
{
relationshipModel: "CategoryCategory",
roles: { parent: "Parent", child: "Child" },
targetDocumentModel: "DomainCategory",
fields: ["/Category/Name", "/Category/Description"],
maxDepth: 4,
childNodes: [
{
relationshipModel: "ProductCategory",
roles: { parent: "Category", child: "Product" },
targetDocumentModel: "DomainProduct",
maxDepth: 2,
childNodes: [
{
relationshipModel: "BundleProduct",
roles: { parent: "Bundle", child: "Product" },
targetDocumentModel: "DomainProduct",
maxDepth: 1
}
]
}
]
},
{
relationshipModel: "ProductCategory",
roles: { parent: "Category", child: "Product" },
targetDocumentModel: "DomainProduct",
maxDepth: 2,
childNodes: [
{
relationshipModel: "BundleProduct",
46

-- 46 of 81 --

roles: { parent: "Bundle", child: "Product" },
targetDocumentModel: "DomainProduct",
maxDepth: 1
}
]
}
]
};
SubTreeNodes
const subTreeNodes: DataOperation.Query.TreeNodes.Query = {
id: "sub-tree-nodes",
type: "TREE_NODES",
entry: {
targetDocumentModel: "DomainCategory",
source: "DomainCategory/18da8433-e39e-4ef6-903e-b38fa7bbdf0d"
},
links: [
{
relationshipModel: "CategoryCategory",
roles: { parent: "Parent", child: "Child" },
targetDocumentModel: "DomainCategory",
fields: ["/Category/Name", "/Category/Description"],
maxDepth: 4,
childNodes: [
{
relationshipModel: "ProductCategory",
roles: { parent: "Category", child: "Product" },
targetDocumentModel: "DomainProduct",
maxDepth: 2,
childNodes: [
{
relationshipModel: "BundleProduct",
roles: { parent: "Bundle", child: "Product" },
targetDocumentModel: "DomainProduct",
maxDepth: 1
}
]
}
]
},
{
relationshipModel: "ProductCategory",
roles: { parent: "Category", child: "Product" },
targetDocumentModel: "DomainProduct",
maxDepth: 2,
childNodes: [
{
relationshipModel: "BundleProduct",
47

-- 47 of 81 --

roles: { parent: "Bundle", child: "Product" },
targetDocumentModel: "DomainProduct",
maxDepth: 1
}
]
}
]
};
When a query is triggered by the Tree Engine, it is transformed into a Data Services Query API
request. This transformation maps Tree Engine’s DataOperation.Query types and properties to the
structure expected by the Data Services.
Query Result
After the request is sent and a response is received from Data Services, the data loader maps the
response back into Tree Engine’s expected data structures. This includes:
• Matching the response ID to the original query.
• Extracting entries and links, and processing documents as needed.
• Handling paging and filtering to ensure only the relevant results are returned to the Tree
Engine.
A query result is represented by the DataOperation.QueryResult type, which contains:
• ID: Matches the original query ID, allowing Tree Engine to identify the corresponding result.
• Entries: An list of relational entries of type Relationship.LinkWithDocument, each containing:
◦ linkRef: A descriptor for the relationship link.
◦ document: The node’s document, including all required fields for that node.
NOTE More information can be found in DataOperation.QueryResult API documentation.
Fields Projection
Tree Engine supports field projection by default, Tree Engine only retrieves the fields that are
needed based on the columns that are displayed in the tree view. This is done by passing a list of
fields to the query, which will be used to filter the fields that are returned from the Data Services.
This is particularly useful for performance optimization, as it reduces the amount of data
transferred over the network and processed by the client.
The fields are specified in the query as an array of strings, where each string represents a field
path. For example:
Fields Projection in Query
const listChildNodesWithFieldsProjection = {
id: "list-child-nodes",
type: "LIST_CHILD_NODES",
48

-- 48 of 81 --

relationshipModel: "DirectoryFile",
source: "DomainDirectory/9bd9169a-a8c9-44be-85af-d65ed6ed8e72",
roles: {
parent: "Directory",
child: "File"
},
targetDocumentModel: "DomainFile",
// list of fields to be retrieved
fields: [
"/File/Basic/Name",
"/File/Basic/Owner",
"/File/Basic/Group",
"/File/FileType",
"/File/Readonly",
"/File/Size"
]
};
Mutation
Mutation represents for data modification that can be happened on the Tree Engine. A mutation
could be:
• Add link operation
• Delete node operation
• Delete link operation
• Move node operation
• Copy node operation
• And more to come…
The data loader can be triggered with a list of mutations, which will happen when the user tries to
perform a bulk operation via multi-selection.
Each mutation is filled with all needed information (e.g. Delete node mutation includes document
ID & the link ID). It also expects a result which can be either successful or failed.
Depending on the mutation type, the result can be different.
See TreeEngineOperation.Mutation for more detail.
Mutations Result
After the mutations are executed, the data loader will return a list of results for each mutation.
Each result contains:
• ID: Matches the original mutation ID, allowing Tree Engine to identify the corresponding result.
• Type: The type of the mutation (e.g., ADD_LINK, DELETE_NODE).
49

-- 49 of 81 --

• Payload: The result of the mutation, it can be either a success or a failure.
◦ For successful mutations, it contains different information depending on the mutation type.
◦ For failed mutations, it contains an error message or code.
NOTE More information can be found in TreeEngineOperation.Done and
TreeEngineOperation.Failed API documentation.
Combine Queries and Mutations
In general, when being triggered, a data loader will be provided with both queries and mutations.
Most of the actions triggered by user always result in a list of queries which need to be loaded.
Every mutation also requires some queries to be reloaded because of outdated information needed
to be updated after a mutation is resolved.
It is advised to execute all mutations before executing any queries otherwise the unexpected
behavior might be popped up.
Document Processor
The data loader provides a DocumentProcessor instance, this is a utility helper which facilitates the
serialize/deserialize process of object returned from Data Services. As the current state, it is mainly
used to convert all the date/time fields of a document model from serialized string into a Date
object and the other way around. See DocumentProcessor interface for more detail.
Customize Data Loader
Customizing/Implementing Tree Engine data loader is pretty simple and straightforward as long as
the expected result and the order of executing mutations and queries are ensured.
src/appsetup.ts
function setup() {
const settings: TreeEngineServerConnectorFactories.ModuleSettings = {
dataLoader: CustomDataLoaderFieldsProjection
};
ModuleRegistryProvider.getInstance().addModule(TreeEngineServerConnectorFactories.crea
teModule(settings));
}
const defaultTreeEngineDataLoader = createA12DataServiceDataLoader({});
const customTreeEngineDataLoader: TreeEngineDataLoader = {
*provideData(params) {
const { queries, mutations, activityId, documentProcessors } = params;
//
// Customization go here
//
50

-- 50 of 81 --

return yield*
call(maybeAsyncFnWrapper(defaultTreeEngineDataLoader.provideData), params);
}
};
NOTE maybeAsyncFnWrapper is optional and only required to be used when typed-
redux-saga is installed. Otherwise, a simple call effect would be enough.
WARNING
Other settings for the A12 Server Connector module might not be preserved
and passed into the customized data loader. In case other settings are set,
please have a more in-depth look into every Tree Engine function that is called
within the customized data loader to make sure that the additional parameters
are not missed.
Custom fields projection
If the default fields projection does not meet your requirements, you can customize it by
implementing your own TreeEngineDataLoader.
src/customDataLoaderFieldsProjection.ts
/**
* Custom data loader for TreeEngine that modifies the behavior of the default data
loader.
* This loader adds an extra field to the queries when a move operation is performed.
* It is used to ensure that the extra data is loaded when moving nodes in the tree
engine.
*/
export const CustomDataLoaderFieldsProjection: TreeEngineDataLoader = {
*provideData(params) {
const { queries, mutations } = params;
const isMoveNodeOperation = mutations?.some((mutation) => mutation.type ===
"MOVE_NODE");
const newQueries = queries.map((query) => {
if (
DataOperation.Query.ListChildNodes.Query.isAssignableFrom(query) &&
query.targetDocumentModel === "DomainProduct" &&
isMoveNodeOperation // This flag indicate whether a custom query logic
shall be applied
) {
return {
...query,
// Extends the default fields to include the ProductType.
// Can be undefined, which shall make the Query request to return
every field available.
fields: query.fields && [...query.fields, "/Product/ProductType"],
linkDocumentFields: query.linkDocumentFields &&
51

-- 51 of 81 --

[...query.linkDocumentFields, "/AdditionalDetails/Quantity"]
};
}
return query;
});
return yield* call(maybeAsyncFnWrapper(defaultDataLoader.provideData), {
...params,
queries: newQueries
});
}
};
Limitation of Link Document Model
When drag and drop a single node, if the relationship between this one and its targeted parent has
a Link Document Model, a modal is shown to update additional information of the link as the
following example from Tree Engine showcase.
Figure 13. Modal to update additional information when drag a person into a team
However, there is no appropriate UI/UX solution for updating Link Document of multiple links, for
now. Therefore, if the Tree Model has a Link Document Model in any relationship, drag & drop for
multi-selected nodes and copy/cut & paste functionalities are not provided intentionally.
Custom SelectorMap
NOTE This API is marked as experimental. Breaking changes might happen even in minor
releases.
The SelectorMap can be used to customize certain state access of the Tree Engine. The Tree Engine
internally uses a default variant of this map, but you can provide your own implementation
containing your customizations as a prop for Tree Engine View Component. Then it will be used in
place of the default one.
52

-- 52 of 81 --

For example, customizing the way the Tree Engine selects attachment thumbnails can look like this:
import { DefaultSelectorMap, type SelectorMap } from
"@com.mgmtp.a12.treeengine/treeengine-core";
export const CustomSelectorMap: SelectorMap = {
...DefaultSelectorMap,
attachmentThumbnail: (attachment) => {
return (state) =>
attachment.content?.startsWith("data:image/")
? attachment.content
: DefaultSelectorMap.attachmentThumbnail(attachment)(state);
}
};
Note that it is mandatory to spread the DefaultSelectorMap when customizing (similar to
ComponentMap and WidgetMap).
Custom RequestSelectorMap
NOTE This API is marked as experimental. Breaking changes might happen even in minor
releases.
The RequestSelectorMap lets you customize how Tree Engine produces Data Services JSON-RPC
requests. A DefaultRequestSelectorMap is provided; you can inject your own to tweak queries and
mutations without involving internal logic.
What it controls
The map exposes selector factories that return JSON-RPC request objects. Tree Engine uses these
exclusively for server calls.
• Queries
◦ loadListRootNodes - list root entries (optionally "orphaned" roots using NOT HAS)
◦ loadListChildNodes - list children for a given parent
◦ loadDocument - load a single document by docRef
◦ loadTreeNodes - load a tree slice (root or parent entry plus nested link specs)
• Mutations
◦ addLink, modifyLink, deleteLink, relink
◦ copyDocument, deleteDocument
All methods return selectors, so you can derive values from app state inside the selector body (e.g.,
locale, activity). Every selector receives an activityId in its config to resolve contextual state.
53

-- 53 of 81 --

Injecting a custom map
Inject your map via the Tree Engine data provider. If none is provided, the
DefaultRequestSelectorMap is used.
export const provider: DataProvider =
TreeEngineServerConnectorFactories.createDataProvider({
requestSelectorMap: customRequestSelectorMap
});
You can also pass the map directly when constructing a data loader if you manage loaders
manually (createA12DataServiceDataLoader(dataServicesSetting, requestSelectorMap)).
Example: add an extra constraint for child listing
This example injects an additional EXACT_MATCH constraint (/category/status = Active) into
loadListChildNodes, while delegating everything else to the default implementation.
export const customRequestSelectorMap: RequestSelectorMap = {
...DefaultRequestSelectorMap,
// Example: add a custom constraint to child listing using the spread operator.
// This merges an extra EXACT_MATCH constraint with whatever the default builder
produces.
loadListChildNodes: (config:
Parameters<RequestSelectorMap["loadListChildNodes"]>[0]) => (state) => {
const baseRequest =
DefaultRequestSelectorMap.loadListChildNodes(config)(state);
const extraConstraint: Query.Operator = {
operator: "exact_match",
field: "/category/status",
value: "Active"
};
const mergedConstraint: Query.Operator = baseRequest.params.query.constraint
? {
operator: "and",
operands: [baseRequest.params.query.constraint, extraConstraint]
}
: extraConstraint;
// Narrow the type to the request's expected constraint
return {
...baseRequest,
params: {
...baseRequest.params,
query: {
...baseRequest.params.query,
54

-- 54 of 81 --

constraint: mergedConstraint
}
}
};
}
};
Hidden root computations in loadTreeNodes
When using a hidden root (i.e., a root entry with a relationshipModel and no explicit source), the
default implementation computes the reversed role needed to traverse from the visible parent to its
hidden counterpart. If you customize loadTreeNodes, be mindful to preserve this behavior or
provide your own role resolution.
Mutations and multi-step operations
Some user actions in the tree translate to multiple underlying mutations. For example, a drag-and-
drop move can result in DELETE_LINK followed by ADD_LINK.
• The RequestSelectorMap gives you control over how each individual RPC request is constructed
(addLink, relinkDocument, etc.).
• If you wish a deeper customization, consider the TreeEngineDataLoader.
Request Filters
This customization approach can also be used in combination with the RequestFilter API (described
in the Data Services documentation), for example to use your own operation methods. Customizing
the RequestFilter alone would not be enough when the method replacement needs some context
(e.g. only overriding methods in certain conditions). Using the RequestSelectorMap could then be
used to provide this context down for the filter to use.
Row Activation
The rowActivation field on a TreeNode controls how rows of that node type respond to a click. When
omitted, the default behavior applies: clicking a row dispatches Events.onNodeSelectionChanged,
selecting the node for viewing or editing.
Three activation types are available:
• event — row click dispatches Events.onNodeEventButtonClicked for built-in event names, or
Events.onRowClicked for custom event names.
• insert — row click triggers an insert action.
• non_interactive — rows have no click behavior and are excluded from keyboard tab order.
Event
Setting rowActivation to { "type": "event", "event": "<eventName>" } dispatches one of two events
55

-- 55 of 81 --

depending on whether the event name is built-in:
• Built-in event names — row click behaves identically to clicking a row action button:
Events.onNodeEventButtonClicked is dispatched with the configured event name.
The full list of built-in event names is exported as TreeModel.BUILT_IN_EVENT_NAMES:
event_add_link, event_delete_node, event_delete_link, event_expand_sub_tree,
event_collapse_sub_tree, event_copy_node, event_copy_node_and_children, event_cut_node,
event_paste, event_paste_above, event_paste_below, event_open_node, event_toggle_expansion.
• Custom event name — row click dispatches Events.onRowClicked with the configured event
name in the payload. Use this to react to row clicks in a saga with your own logic.
Tree Model — dispatch a custom event on row click
{
"rowActivation": {
"type": "event",
"event": "selectPerson"
}
}
Listen to Events.onRowClicked in a saga to react to the event:
handle-custom-row-action-saga.ts
export function* handleCustomRowActionSaga(): SagaGenerator<void> {
yield* takeEvery(
(action: unknown) =>
isAction(action) &&
TreeEngineActions.event.match(action) &&
Events.onRowClicked.match(action.payload.engineAction),
handle
);
}
function* handle(
action: Action<TreeEngineActions.EventPayload<Action<Events.RowClickedPayload>>>
): SagaGenerator<void> {
const { engineAction, activityId } = action.payload;
if (engineAction.payload.event === "selectPerson") {
const notificationAction = NotificationActions.add({
activityId,
severity: "info",
title: { key:
SHOWCASE_RESOURCE_KEYS.showcase.a12Teams.customRowAction.selectPerson.title },
message: { key:
SHOWCASE_RESOURCE_KEYS.showcase.a12Teams.customRowAction.selectPerson.message }
});
yield* put(notificationAction);
}
56

-- 56 of 81 --

}
Events.RowClickedPayload provides nodeIdentifier, nodePath, and event — the configured event
name — so a single saga can branch on multiple custom row actions.
Insert
Setting rowActivation to { "type": "insert" } causes a row click to trigger an insert action,
equivalent to clicking a row insert button.
The optional position field controls where the new node is placed:
Value Behavior
as_child (default) New node is inserted as a child of the clicked node.
above New node is inserted as a sibling above the clicked node.
below New node is inserted as a sibling below the clicked node.
The optional documentModelRef field specifies the document model of the new node. When omitted
the engine uses the same resolution logic as a row insert button without a documentModelRef.
Tree Model — insert a child node on row click
{
"rowActivation": {
"type": "insert",
"position": "as_child",
"documentModelRef": "MyDocument"
}
}
Non-Interactive Rows
Setting rowActivation to { "type": "non_interactive" } makes rows of that node type non-
interactive. Clicking the row produces no action, and the row is excluded from the keyboard tab
order (tabindex="-1").
Tree Model — non-interactive row
{
"rowActivation": {
"type": "non_interactive"
}
}
Row action buttons within non-interactive rows still work normally — only the row click itself is
suppressed.
57

-- 57 of 81 --

Inheritance
rowActivation supports inheritance via configuration.inherit.rowActivation: true.
TypeScript
TreeModel.RowActivation is a discriminated union of the three activation types. Branch at runtime
using a switch on the type discriminant.
Advanced
In this section, we will focus on some more advanced customizations available in Tree Engine.
Hidden Root Node
Tree Engine offers the possibility to customize the root node. This feature can be useful for the case
in which user wants to see/open the sub-tree of a document (from an Overview Engine or Tree
Engine) in another engine.
For example: In Tree Engine Showcase, we have a file explorer example which can open a
Document Model file.
Figure 14. Data modeller showcase
To get started with this feature, it is required to be:
58

-- 58 of 81 --

• Familiar with Client’s activity concept.
• Knowledgeable of writing custom Redux saga.
When Tree Engine is rendered, it will initially send a batch request for LIST_TERMINATING_LINKS
operation based on your Tree Model configuration. However, in the use case where the Document
Model DomainPerson.json is opened, the LIST_TERMINATING_LINKS operation may return the root
groups from the other Document Models. These mixed groups of different documents can be very
confused and not manageable.
To solve this case, Tree Engine provides an API to specify the starting root node by creating a Tree
Engine activity with these descriptors:
• rootInstance: the id of your root document, e.g. "DomainFile/1"
• rootRelationshipName: the relationship link between the root instance and Tree Model’s root
node, e.g. DocumentModelFileGroup
• rootRelationshipRole: the role of the root instance in the Relationship Model, e.g. File
Below is a custom saga that catches the custom action from the left Tree Engine then create another
Tree Engine activity with the custom descriptors:
src/model-editor/sagas/handle-open-document-model-button-saga.ts
function* handle(action: EventAction): SagaGenerator<void> {
const { activityId, engineAction } = action.payload;
const { nodeIdentifier } = engineAction.payload;
const activityDescriptor = createActivityDescriptor(nodeIdentifier.id);
if (action.payload.engineAction.payload.button.event === OPEN_DM_NODE_EVENT) {
yield* put(
TreeEngineActions.createActivity(
{ activityDescriptor, initiatingActivityId: activityId },
)
);
}
}
function createActivityDescriptor(instance: string):
TreeEngineActivity.Descriptor.HiddenRootInstance {
return {
/**
* User-defined descriptors
*/
model: "data-modeler-tree",
engine: "tree", // Since 9.0.0, "engine" is no longer a mandatory descriptor
for a Tree Engine activity
/**
* Mandatory descriptors to enable hidden root node on the tree
*/
rootInstance: instance,
rootRelationshipName: "DocumentModelFileGroup",
rootRelationshipRole: "File"
59

-- 59 of 81 --

};
}
Disable Link Creation in Tree Engine
By default, the CRUD module of Tree Engine provides the ability to link the created document in the
Form Engine into the parent node when user uses the Add Child Node event.
However, in some specific use cases, the user would like to link the created document to the Tree
directly in the Form Engine. This would give more benefit because of the transactional nature of a
batch request. Any error happens while linking will roll back the CREATE_DOCUMENT operation.
To achieve it, developers can choose either to:
• Use Relationship Engine inside the Form to perform the linking directly when user creates the
document. If the Tree Engine sees any link between the created document to the parent node, it
will automatically ignore the linking step in the Tree Engine after the document creation.
• Use customized logic to perform the linking using DataProvider API. However, Tree Engine will
not know this customization, so it still links the created document. In order to make Tree Engine
ignore the linking step, developer should tell it whenever the link creation should be
enabled/disabled by providing a setting.
src/appsetup.ts
export function setup() {
const treeEngineModuleSetting: TreeEngineFactories.ModuleSettings = {
sagaConfig: { linkCreationSetting: { isLinkAddedByDetailActivity } }
};
ModuleRegistryProvider.getInstance().addModule(TreeEngineFactories.createModule(treeEn
gineModuleSetting));
...
}
export function* isLinkAddedByDetailActivity(
params: TreeEngineSaga.IsLinkAddedByDetailActivityParams
): SagaGenerator<boolean | undefined> {
const { activityId } = params;
const activity = yield* select(ActivitySelectors.activityById(activityId));
assert(activity, "Activity not found");
return activity.descriptor["view"] === "CustomA12TeamTreeEngine" ? true :
undefined;
}
The parameters of the function isLinkAddedByDetailActivity will provide:
• activityId: The activity ID of Tree Engine.
60

-- 60 of 81 --

• detailActivityId: The activity ID of the detail activity (usually a Form Engine activity).
• relationshipModelRef: The relationship name is useful when the link creation is
disabled/enabled per relationship.
Custom New Link Position
By default, A12 Data Services controls the order of the newly added nodes. However, developers
can change this behavior by providing the following setting to the Server Connector module.
src/appsetup.ts
import { Relationship } from "@com.mgmtp.a12.dataservices/dataservices-access/lib";
function setup() {
const settings: TreeEngineServerConnectorFactories.ModuleSettings = {
addLinkSetting: { newLinkPosition: Relationship.LinkPosition.BOTTOM }
};
ModuleRegistryProvider.getInstance().addModule(TreeEngineServerConnectorFactories.crea
teModule(settings));
}
newLinkPosition field accepts any value of Relationship.LinkPosition enumeration, undefined or a
function which returns those values. The function receives following parameters:
• activityId: The current Tree Engine activity ID.
• relationshipName: The relationship name of the new link is about to be created.
By returning undefined, the Engine will fall back to the default behavior.
NOTE This setting will not affect the relationship models which do not have ordering
enabled.
Child Nodes Preloading
By default, Tree Engine always shows the arrow buttons besides the nodes which can be parental.
This behavior could cause the end users expand the leaf nodes unnecessarily. With this feature, the
arrow buttons will only be shown if their corresponding nodes really have one or more children.
In the Server Connector module’s setting, preloadChildNodes field can accept boolean values, or
undefined, or a function that returns those values.
WARNING
In order to determine whether the target node has any children or not, Tree
Engine needs to fetch all data of child nodes including their documents, instead
of the number of children merely. This limitation of Data Services could affect
the bandwidths requests/responses to/from the server. Therefore, this feature
61

-- 61 of 81 --

should be used wisely and in concern.
src/appsetup.ts
import { ModuleRegistryProvider } from "@com.mgmtp.a12.client/client-
core/lib/core/application";
import { TreeEngineServerConnectorFactories } from
"@com.mgmtp.a12.treeengine/treeengine-core";
function setup() {
const settings: TreeEngineServerConnectorFactories.ModuleSettings = {
preloadChildNodes: true
};
ModuleRegistryProvider.getInstance().addModule(TreeEngineServerConnectorFactories.crea
teModule(settings));
}
Unregister Default Engine Sagas
Tree Engine module is bundled with a set of saga handlers that support different type of events
dispatched by the core module. Generally, these sagas follow the standard workflow of an A12
based application. However, in some cases where application does not want to follow the default
standard, Tree Engine also offers an option to dynamically register/unregister specific saga
handler(s).
src/appsetup.ts
export function setup() {
const treeEngineModuleSetting: TreeEngineFactories.ModuleSettings = {
sagaSetting: { sagaRegistrations: createCustomSagasRegistration() }
};
ModuleRegistryProvider.getInstance().addModule(TreeEngineFactories.createModule(treeEn
gineModuleSetting));
...
}
export function createCustomSagaRegistrations(): TreeEngineSaga.SagaRegistrationsMap {
return {
watchChildLinksCreation: dontAllowCustomA12TeamTree,
watchNodeDeletion: dontAllowCustomA12TeamTree,
watchLinkDeletion: dontAllowCustomA12TeamTree
};
function* dontAllowCustomA12TeamTree(activityId: string): SagaGenerator<boolean> {
const activity = yield* select(ActivitySelectors.activityById(activityId));
62

-- 62 of 81 --

return activity?.descriptor["view"] !== "CustomA12TeamTreeEngine";
}
}
NOTE
Each property of the registration map accepts a boolean value or a function that also
returns a boolean value. Returning false will unregister respective saga and the
other way around.
After defining the saga registration map, the associated action from the Tree Engine will no longer
be handled by the saga. Developers will need to provide proper handling of the unregistered saga
handlers.
Register Custom Actions for Saga Initialization
By default, Tree Engine matches one of these actions to trigger Tree Engine’s initialization of its
sagas: ActivityActions.push, ActivityActions.cancel and ActivityActions.commit.done. These sagas
are used in every interaction in the engine. Therefore, in case of having customization built on top
of Client that does not use one of these actions to create/re-create an activity, the specification of
action to TreeEngineFactories.SagaSetting is necessary as below:
src/appsetup.ts
export function setup() {
const treeEngineModuleSetting: TreeEngineFactories.ModuleSettings = {
sagaSetting: { sagaInitializationMatchers: [yourActionCreator] }
};
ModuleRegistryProvider.getInstance().addModule(TreeEngineFactories.createModule(treeEn
gineModuleSetting));
...
}
Maximum Number of Nodes
Query API request from Data Services comes with a default maximum page size limit of 100, Tree
Engine respects the limit and use it as the default maximum page size limit per Query request.
For the level by level expansion strategy, this maximum number will affect the amount of root
nodes, or the amount of child nodes from a parent node.
For the tree expansion strategy, this maximum number will only affect the amount of root nodes,
the maximum amount of child nodes will remain unlimited.
In case of raising the limit, developer will not only have to set the
mgmtp.a12.dataservices.query.page-request.page-size-limit but also required to update the default
maximum page limit in the Tree Engine configuration.
63

-- 63 of 81 --

TreeEngineServerConnector
TreeEngineServerConnectorFactories.createModule({
...otherSettings,
dataServicesSetting: {
maximumPageSize: 200, // the value should match the one set in Data Services
},
});
Custom Initial Expansion
Tree Model offers an option to configure the initial expansion behavior of the Tree Engine when it
is initialized. This is a fixed behavior that will apply to every Tree Engine instance no matter the
context. Furthermore, this can potentially cause the performance issue because always expanding
to certain levels can consume a lot of time. Therefore, Tree Engine offers an ability to dynamically
set initial expansion based on your application business logic.
openTreeEngineMiddleware.example.ts
const activityPushAction = TreeEngineActions.createActivity(
{ activityDescriptor: { engine: "tree" } },
{ initialExpansion: { type: "all_levels" } }
);
store.dispatch(activityPushAction)
By creating the Tree Engine activity this way, the engine will automatically use passed
configuration instead of the one from Tree Model. The property initialExpansion accepts one of
these value:
• TreeModel.InitialExpansion: of two types all_levels and level_limit.
• false: completely disable the initial expansion behavior.
• undefined: skip the configuration via API and only use the configuration from Tree Model.
Let’s have a look at Tree Engine Showcase’s model editor example, for this one, it is necessary to
restrict the initial expansion behavior on Document Model or Form Model which file’s size is too
large.
handle-open-model-saga.ts
import { DataSelector, TreeEngineState, TreeEngineActions, TreeEngineSelectors } from
"@com.mgmtp.a12.treeengine/treeengine-core";
import { SagaGenerator, put, select } from "typed-redux-saga";
function* handle(action): SagaGenerator<void> {
const { activityId } = action.payload;
64

-- 64 of 81 --

const data = yield* select(TreeEngineSelectors.dataState(activityId));
const node = data && DataSelector.node(nodeIdentifier)(data);
const initialExpansion = node && File.isInstance(node.document) &&
getInitialExpansionConfig(node.document);
const activityPushAction = TreeEngineActions.createActivity(
{ activityDescriptor: { engine: "tree" } },
{ initialExpansion }
);
yield* put(activityPushAction)
}
export function getInitialExpansionConfig(doc: File) {
let initialExpansion: TreeEngineState["initialExpansion"] = undefined;
if (doc.File.Size <= 5000) {
initialExpansion = { type: "all_levels" };
} else if (doc.File.Size > 5000 && doc.File.Size < 10000) {
initialExpansion = { type: "level_limit", level: 2, affectedNodeRefs: ["node-
54bbd"] };
} else if (doc.File.Size >= 10000) {
initialExpansion = false;
}
return initialExpansion;
}
Re-Rendering Optimization
Re-rendering is part of any React component lifecycle, most of the time, it is good because your
component will always "react" to any change happen in your application. However, when
rendering a big list/tree, re-rendering can easily cause a bottleneck due to the constantly re-
rendering of every component.
Tree Engine includes optimized re-rendering by default, minimizing unnecessary re-rendering.
However, this only applies when Tree Engine component is used without any customization on top.
In case of customization, learning how to be able to detect the bottleneck is the most important part
when performance is top of project’s priority.
There are multiple approaches to figure out the re-rendering:
1. React devtools: out of the box integration, install the extension then enable highlighting updates
or profiling yourself. However, the tools is lacking of hooks supports nor details comparison on
why the component is rendered, this sometimes lead to confusion on figuring out which
component should be improved.
2. @welldone-software/why-did-you-render: is a library which requires some knowledge about
the build tools. Once integrated properly, the tools can show a lot of useful information on
which props is changed on which hooks… which can help a lot with decision-making on what to
be improved one by one.
65

-- 65 of 81 --

What Are the Usual Causes of Re-Rendering in Tree Engine
Customized Component
1. Most if not all Tree Engine component are memoized via React.memo. Your custom component
should also have it.
2. Avoid re-creating component passed into the component map.
BadCustomTreeEngine.tsx
/// Bad practice, this can lead to lots of mount/unmount with any change on the Redux
store
function CustomTreeEngine(props) {
const myState = useSelector(mySelector)
return (
<TreeEngineFactories.ViewComponent
{...props}
componentMap={{
Body: function CustomBody() {
if(myState === true) {
return <MyCustomBody />
}
return <DefaultComponentMap.Body />;
}
}}
/>
);
}
CustomTreeEngine.tsx
/// Best practice:
/// - Preventing CustomBody function from re-initialized
/// - Cherry pick only the needed state
/// - React.memo
function CustomTreeEngine(props) {
const componentMap = React.useMemo(() => ({ Body: CustomBody }), [])
return <TreeEngineFactories.ViewComponent {...props} componentMap={componentMap}
/>;
}
const CustomBody = React.memo(function CustomBody(props) {
const myState = useSelector(mySelector)
if(myState === true) {
return <MyCustomBody />
}
return <DefaultComponentMap.Body {...props} />;
66

-- 66 of 81 --

})
NOTE
The above examples is a blueprint which mean it can be applied to any generic
React component that share the same component overriding pattern, this includes
Tree Engine’s ComponentMap/WidgetsMap or TreeTable/Table Widget’s
componentRenderers.
Selectors
1. Selector can be a bottleneck for application when not implemented properly. A non-optimized
one will always be triggered when an action is dispatched which will lead to the selector to be
executed again. Refer to reselect documentation if interested.
Others
1. Reference type a.k.a. objects and functions recreation usually does not tax on the performance.
However, when those are passed as a prop into other component, changing/re-creating the
reference will always lead to a re-rendering. This can be solved with React.useMemo and
React.useCallback.
API Documentation
The API documentation can be found here.
Breaking Change Management
For the general definition of breaking and non-breaking changes in the A12 platform, as well as
frontend and backend perspectives, see the A12 Breaking Change Management page.
The following section describes how this general definition is interpreted for the Tree Engine.
Public API Surface
The public API is defined by the exports in index.ts. Any removal or incompatible change to an
exported symbol is a breaking change. Additions to the public API are non-breaking.
Exports annotated @experimental may change in minor releases without being considered breaking.
Exports annotated @internal are not part of the public API and carry no stability guarantee.
Resource Keys
Adding new entries to RESOURCE_KEYS is non-breaking: applications that do not override those keys
continue to receive the default translation. Removing or renaming an existing key is a breaking
change for any application that provides a custom translation for it.
67

-- 67 of 81 --

Redux State Shape and Action Types
The Redux action type strings and the reducer state shape (DataState, UiState, ModelsState) are part
of the public contract. Renaming or removing an action type, or restructuring the state in a way
that breaks existing selectors, is a breaking change.
Selectors and sagas that are part of the public API follow the standard public-API rule above.
Composable Factories
The signatures of withTreeEngine(), withConfiguredTreeEngine(), withTreeEngineDataHandlers(),
withTreeEngineDataReducers(), withTreeEngineMiddlewares(), withTreeEngineSagas(),
withTreeEngineView(), and withTreeModelSupport() are part of the public API. Any change to
parameter types or return types that requires call-site updates is a breaking change. New optional
parameters are non-breaking.
Component Map
ComponentMap and DefaultComponentMap are part of the public API. Adding new keys (optional or
required) is non-breaking as long as a default implementation is provided in DefaultComponentMap.
Removing or renaming an existing key, or changing the required props of a component slot, is a
breaking change.
Tree Model Format
Only changes that are incompatible with existing Tree Model files are breaking: for example,
removing a required field or changing the semantics of an existing field.
Adding an optional field is non-breaking even though it triggers a model schema version bump
and a corresponding migration step. When such a change is introduced, the Tree Engine model
migration tool handles the version upgrade automatically. See the Model Migration Tool chapter for
usage instructions.
Server Protocol
Changes to the wire format between the client-side data loader and the backend (request/response
shape, query parameters, endpoint URLs) are breaking on both the frontend and backend sides.
Non-Breaking Examples
The following types of changes are consistently treated as non-breaking in the Tree Engine:
• Adding a new optional configuration field to an existing API
• Adding new RESOURCE_KEYS entries
• Introducing a new @experimental export
68

-- 68 of 81 --

• Internal refactoring that does not affect public exports
• Bumping a transitive dependency with no visible API impact
Migration Instructions
CAUTION Please have a look at Migration to latest A12 chapter for an explanation of
general steps.
2026.06
11.0.0
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
Minimum supported model version raised to 9.0.0
Tooling: Model migration tool (treeengine-model-migration), required for affected models — see
11.0.0.
The migration tool no longer supports migration of Tree models before version 9.0.0. If you need to
migrate models from an older version, you need to migrate to version 9.0.0 first using the migration
tool of that version.
Replace defaultRowAction with rowActivation
Tooling: Model migration tool (treeengine-model-migration), required — auto-migrates the legacy
shape (see 11.0.0); the TypeScript change below is manual.
The defaultRowAction field on TreeNode has been replaced by rowActivation. The new field accepts
three shapes:
• { type: "event", event: "…" } — row click behaviour depends on the event name:
◦ Built-in event names dispatch onNodeEventButtonClicked, identical to clicking a row action
button.
69

-- 69 of 81 --

◦ Custom event name dispatches onRowClicked carrying the event name in the payload. Use
this to handle the row click in a saga with your own logic. Auto-migrated from the legacy {
custom: true, event: "…" } shape.
• { type: "insert", position?, documentModelRef? } — row click triggers an insert action.
• { type: "non_interactive" } — new for accessibility. Row click is suppressed; the row has no
pointer cursor and is skipped in keyboard tab order.
The inherit flag has also been renamed: configuration.inherit.defaultRowAction is now
configuration.inherit.rowActivation.
Omitting rowActivation keeps the engine’s default behavior — clicking a row dispatches
onNodeSelectionChanged.
TypeScript changes
The type TreeModel.DefaultRowAction has been removed. Use TreeModel.RowActivation:
type RowActivation =
| TreeModel.EventRowActivation // { type: "event"; event: string }
| TreeModel.InsertRowActivation // { type: "insert"; position?:
InsertPosition; documentModelRef?: string }
| TreeModel.NonInteractiveRowActivation // { type: "non_interactive" }
See Row Activation for details on the available activation types.
Enforce top-level package exports
Tooling: Codemod recipe enforce-top-level-exports, required — deep lib/ imports now fail at
runtime (see enforce-top-level-exports).
Starting with v11.0.0, the exports field in package.json is used to enforce top-level imports. Deep
imports into lib/ subpaths (e.g. @com.mgmtp.a12.treeengine/treeengine-core/lib/extensions/client)
are no longer resolved at runtime and will fail with ERR_PACKAGE_PATH_NOT_EXPORTED.
All public APIs remain available from the package root:
// Before (no longer works)
import { TreeEngineFactories } from "@com.mgmtp.a12.treeengine/treeengine-
core/lib/extensions/client";
import { DataSelector } from "@com.mgmtp.a12.treeengine/treeengine-
core/lib/core/store";
// After
import { TreeEngineFactories, DataSelector } from
"@com.mgmtp.a12.treeengine/treeengine-core";
Run the codemod to migrate automatically:
70

-- 70 of 81 --

npx @com.mgmtp.a12.treeengine/treeengine-codemod enforce-top-level-exports
./client/tsconfig.json
Composable API registration order
Tooling: none — manual change.
When using withTreeEngine together with either variant of Relationship Engine, the Tree Engine
composable must now be registered after Relationship Engine in the combineFeatures chain.
Old Relationship Engine New Relationship Engine
combineFeatures(
withTreeEngine,
withRelationshipFormEngine,
)
combineFeatures(
withRelationshipFormEngine,
withTreeEngine,
)
combineFeatures(
withTreeEngine,
withRelationshipEngine,
)
combineFeatures(
withRelationshipEngine,
withTreeEngine,
)
This is enforced at compile time. TypeScript will report a type error if withTreeEngine appears
before the Relationship Engine composable in the registration order.
Rename majorElements / minorElements to rightSlot / leftSlot
Tooling: Model migration tool (treeengine-model-migration), required — auto-migrates JSON
models (see 11.0.0); the TypeScript rename below is manual.
The majorElements and minorElements properties in TreeModel.SubHeaderBox and TreeModel.FooterBox
have been renamed to rightSlot and leftSlot respectively to align with their actual rendering
positions.
This is a model-level rename with no visual change. Existing JSON models are migrated
automatically by the model migration tool.
For TypeScript consumers:
Old property New property
SubHeaderBox.minorElements SubHeaderBox.leftSlot
SubHeaderBox.majorElements SubHeaderBox.rightSlot
FooterBox.minorElements FooterBox.leftSlot
FooterBox.majorElements FooterBox.rightSlot
71

-- 71 of 81 --

2025.06-ext4
10.2.0
Deprecate nested imports
Tooling: Codemod recipe prefer-top-level-imports, optional — nested imports still work in this
release but are deprecated (see prefer-top-level-imports).
Nested imports are deprecated in favor of top-level imports to avoid unnecessary breaking changes
caused by moving or renaming internal files. This makes the code more resilient to internal
refactoring, provides a single consistent import path, and reduces ongoing maintenance effort.
Run the codemod command below to migrate automatically:
npx @com.mgmtp.a12.treeengine/treeengine-codemod prefer-top-level-imports
./client/tsconfig.json
2025.06
10.0.0
Migrate to ESM
The npm artifact @com.mgmtp.a12.treeengine/treeengine-core was migrated from CommonJS to ESM.
When using Node 22.12+ and modern build tools, there should be no changes necessary to your
bundler setup.
NOTE If your tests depend on mocking/stubbing Tree Engine API, consult the
documentation of your test runner on how to work with ES modules.
Migrating your own application to ESM is not required, but recommended. Consult the
documentation of your bundler for specifics.
Update JavaScript output to ES2024
The javascript output of the npm artifacts was updated from ES2020 to ES2024 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
React 19 upgrade
We dropped the support for React 18 and older and now require React 19 as our peerDependency.
This means you have to perform the React 19 migration, which is described in great detail in the
official React 19 Upgrade Guide. They have codemods that should make the transition
straightforward.
72

-- 72 of 81 --

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
"@types/react-redux": "^7.1.34"
}
}
Styled-components v6 upgrade
We dropped the support for styled-components v5 and now require v6 as our peerDependency.
Please refer to the styled-components guide and Widgets migration notes for more information.
Model Changes
• Tree Model’s initialExpansion (and its respective namespace InitialExpansion) and pageSize has
been moved into the expansionStrategy, with type "level_by_level", it is done in preparation for
the new tree expansion strategy mentioned in later section.
Multi-level tree loading strategy
Multi-level tree loading strategy is introduced to reduce amount of rpc requests sent to Data
Services to 1, for most engine’s interactions. The new strategy results in several changes to the API:
1. New expansionStrategy property has been introduced to the TreeModel to allow switching
between the new and the old strategy.
2. New Tree Engine data holder descriptors, created by new loading strategy, will no longer has
relationshipName or relationshipRole.
3. New Query.TreeNodes interface has been added to the TreeEngineDataLoader, allow customizing
several aspects related to Query API (e.g.: fields projection).
In the tree mode, initial expansion will be configured with a maximum depth value per
relationship model instead of a single maximum depth value for the whole tree previously. Let’s
have a look at this example:
Relationship Model Max Depth
CategoryCategory 5
ProductCategory 1
73

-- 73 of 81 --

Relationship Model Max Depth
ProductBundle 1
With the given example above, the engine can initially fetch and display a tree with up to 7 levels
in 1 single request.
Remove LIST_* operation support
Since the Data Services Query API is integrated by default, all related interfaces/functions have
been removed/renamed, including:
Removed functions Alternative
TreeEngineServerConnectorFactories.createModul
e({queryAPI: true})
TreeEngineServerConnectorFactories.createModul
e({})
TreeEngineServerConnectorFactories.createDataP
rovider({queryAPI: true})
TreeEngineServerConnectorFactories.createDataP
rovider({})
For better integration/migration path, see the 2024.06’s Query API section for an early adoption of
the new API.
Remove the Embedded Attachments API
The embedded attachments related API has been removed, the engine now solely relies on the
thumbnails slice to render the thumbnail. Unless embedded attachment is used, it is expected to
have no impact to the current setup. However, it case embedded attachment is used, to restore
functionality, the newly introduced selectorMap will be used to remap the new default thumbnail
accessing logic.
src/customTreeEngineSelectorMap.ts
import { DefaultSelectorMap, type SelectorMap } from
"@com.mgmtp.a12.treeengine/treeengine-core";
export const CustomSelectorMap: SelectorMap = {
...DefaultSelectorMap,
attachmentThumbnail: (attachment) => {
return (state) =>
attachment.content?.startsWith("data:image/")
? attachment.content
: DefaultSelectorMap.attachmentThumbnail(attachment)(state);
}
};
By default, the thumbnail selector assumes that thumbnails for attachments can be queried from
the thumbnails slice by their id. However, for embedded attachment implementation, the image
source can be simply accessed by directly read the content prop.
The selectorMap can then be passed into any TreeEngine view component to override the default
behavior.
74

-- 74 of 81 --

Remove UIStateSelector.latestMultiSelectionNode
The deprecated UIStateSelector.latestMultiSelectionNode selector has been removed. It was used
to get the latest node selected in multi-selection mode. The
UIStateSelector.lastMultiSelectionAction selector should be used instead. It returns the last action
that was performed in multi-selection mode, which can be used to determine the latest node
selected.
Old code with UIStateSelector.latestMultiSelectionNode
const treeEngineState = yield* select(TreeEngineSelectors.engineState(activityId));
let multiSelectionNodes = UIStateSelector.multiSelectionNodes()(treeEngineState);
const latestMultiSelectionNode =
UIStateSelector.latestMultiSelectionNode()(treeEngineState);
if(latestMultiSelectionNode) {
const engineAction = TreeEngineActions.setMultiSelectionNodes({
multiSelectionNodes: updateMultiSelectionNodes(multiSelectionNodes,
latestMultiSelectionNode),
});
yield* put(TreeEngineActions.event({ activityId, engineAction }));
}
function updateMultiSelectionNodes(multiSelectionNodes:
TreeEngineState.MultiSelectionNodes, latestMultiSelectionNode:
TreeEngineState.NodePath) {
// do something with multiSelectionNodes
}
Replace it with the following code:
New code with UIStateSelector.lastMultiSelectionAction
const treeEngineState = yield* select(TreeEngineSelectors.engineState(activityId));
let multiSelectionNodes = UIStateSelector.multiSelectionNodes()(treeEngineState);
const lastMultiSelectionAction =
UIStateSelector.lastMultiSelectionAction()(treeEngineState);
if(lastMultiSelectionAction) {
const engineAction = TreeEngineActions.setMultiSelectionNodes({
multiSelectionNodes: updateMultiSelectionNodes(multiSelectionNodes,
lastMultiSelectionAction.payload.nodePath),
});
yield* put(TreeEngineActions.event({ activityId, engineAction }));
}
function updateMultiSelectionNodes(multiSelectionNodes:
75

-- 75 of 81 --

TreeEngineState.MultiSelectionNodes, latestMultiSelectionNode:
TreeEngineState.NodePath) {
// do something with multiSelectionNodes
}
Minimum supported model version raised to 8.0.0
Tooling: Model migration tool (treeengine-model-migration), required for affected models.
The migration tool no longer supports migration of Tree models before version 8.0.0. If you need to
migrate models from an older version, you need to migrate to version 8.0.0 first using the migration
tool of that version.
Remove unused Actions
Actions in the following list are no longer used and removed as a consequence.
• Commands.setRoot
• Commands.setData
• Commands.setMatchedNodes
• Commands.setQuery
• Events.onQueryChanged
• Events.onQueryReset
Selectors
Most selectors has been reworked to migrate to WeakMap caching mechanism with Reselect v5.
This migration allows better usability of selectors while maintaining good performance. Most
selectors function call signature remains the same, but some of them have been changed to
accommodate the new caching mechanism.
• ModelSelector.relationshipBetweenDocumentModels has changed its parameter into 2 list of
strings.
Tree Activity Descriptors
Tree Activity Descriptors behavior has been updated to always forward its descriptors to the child
activities when possible. This will add more flexibilities to the match conditions modeling of the
child activities in the app model. However, this change may break the existing descriptors modeling
which relies on specific descriptor, e.g.: "engine": "tree". Fortunately, this "engine" descriptor is no
longer required by Tree Engine since 9.0 release, so these could already be adapted accordingly.
Maximum Page Size Limit
Query API request from Data Services comes with a default maximum page size limit of 100, Tree
Engine respects the limit and use it as the default maximum page size limit per Query request.
However, in case of raising the limit, project will not only have to set the
mgmtp.a12.dataservices.query.page-request.page-size-limit but also required to update the default
76

-- 76 of 81 --

maximum page limit in the Tree Engine configuration.
Tree Engine Configuration
TreeEngineServerConnectorFactories.createModule({
...otherSettings,
dataServicesSetting: {
maximumPageSize: 200, // the value should match the one set in Data Services
},
});
Model Migration Tool
To migrate Tree Model files, first install or update the migration tool with
npm install -g @com.mgmtp.a12.treeengine/treeengine-model-migration
Then run the following command to perform the migration
tree-model-migration <path to tree model file or directory> --backup
Example
# file
tree-model-migration my-tree-model.json --backup
# folder
tree-model-migration . --backup
Note
• The minimum supported model version is 9.0.0. Models older than this must first be migrated to
version 9.0.0 using that version’s migration tool.
• If <path to tree model file or directory> is a directory, the migration tool will recursively
search for Tree Model files to migrate.
• If Tree Model files are not under version control, use --backup (alias -b) flag to create backups
for model files. This flag is optional.
• Use --help (alias -h) flag to show all available options.
Model migrations
This section documents what the tool changes in a Tree Model when migrating to a given engine
version — the change categories it applies, with at least one named example each — so you can
map the resulting diff to the documented intent. Each entry is also referenced from the matching
77

-- 77 of 81 --

subsection of the migration instructions.
11.0.0
Rename model fields. SubHeaderBox/FooterBox majorElements becomes rightSlot and minorElements
becomes leftSlot.
// before
{ "majorElements": [/* ... */], "minorElements": [/* ... */] }
// after
{ "rightSlot": [/* ... */], "leftSlot": [/* ... */] }
Rewrite the row-action shape. The legacy defaultRowAction custom-event shape is rewritten to the
new rowActivation shape, and the configuration.inherit.defaultRowAction flag is renamed to
configuration.inherit.rowActivation.
// before
"defaultRowAction": { "custom": true, "event": "open" }
// after
"rowActivation": { "type": "event", "event": "open" }
Codemod
Introduction
The Tree Engine Codemod is a command-line tool for running automated code transformations
(codemods) on TypeScript projects. Codemods assist with codebase migrations by automatically
applying breaking changes, deprecations, and API updates—reducing manual effort and
minimizing human error during upgrades.
Installation
The codemod can be executed directly using npx or pnpm dlx without requiring a permanent
installation:
npx @com.mgmtp.a12.treeengine/treeengine-codemod@latest <recipe-id-or-version>
<tsconfig-path> [options]
pnpm dlx @com.mgmtp.a12.treeengine/treeengine-codemod@latest <recipe-id-or-version>
<tsconfig-path> [options]
Usage
The codemod supports two modes of operation, plus an interactive variant:
78

-- 78 of 81 --

1. Recipe-based execution — run a specific recipe by its identifier: <recipe-id> <tsconfig-path>.
2. Version-based migration — run all recipes whose supported range includes a target version:
<target-version> <tsconfig-path>. The tool selects the applicable recipes automatically.
3. Interactive mode (--interactive) — pick a recipe or target version through guided prompts.
See Examples for concrete commands and Arguments / Options for the full CLI surface.
Arguments
Argument Description
<recipe-id-or-version> Either the identifier of a specific codemod recipe to execute, or a target
version number (e.g., 1.2.0, 11.0.0) to run all applicable recipes. Use
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
npx @com.mgmtp.a12.treeengine/treeengine-codemod@latest --list
Run a Single Recipe
Execute a specific recipe on a project:
npx @com.mgmtp.a12.treeengine/treeengine-codemod@latest prefer-top-level-imports
79

-- 79 of 81 --

./client/tsconfig.json
Migrate to a Specific Version
Run all codemods required to migrate to version 11.0.0:
npx @com.mgmtp.a12.treeengine/treeengine-codemod@latest 11.0.0 ./tsconfig.json
Skip Git Check
Run a codemod without verifying the git working directory state:
npx @com.mgmtp.a12.treeengine/treeengine-codemod@latest 11.0.0 ./tsconfig.json --no
-git-check
Interactive Mode
Launch the interactive interface for guided execution:
npx @com.mgmtp.a12.treeengine/treeengine-codemod@latest -i
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
resulting diff to the documented intent. Run --list for the authoritative set of recipes and their
supported version ranges.
enforce-top-level-exports
Supported versions ^11.0.0 (2026.06)
80

-- 80 of 81 --

Required / optional Required — from v11.0.0 the package.json exports field blocks deep lib/
imports at runtime.
Rewrite import paths. Deep lib/ imports are rewritten to the top-level package entry.
// before
import { TreeEngineFactories } from "@com.mgmtp.a12.treeengine/treeengine-
core/lib/extensions/client";
// after
import { TreeEngineFactories } from "@com.mgmtp.a12.treeengine/treeengine-core";
prefer-top-level-imports
Supported versions ^10.2.0 (2025.06-ext4)
Required / optional Optional — nested imports are deprecated but still resolve in this
release.
Rewrite import paths. Deep import paths are rewritten to their top-level names.
// before
import { DataSelector } from "@com.mgmtp.a12.treeengine/treeengine-
core/lib/core/store";
// after
import { DataSelector } from "@com.mgmtp.a12.treeengine/treeengine-core";
81

-- 81 of 81 --

