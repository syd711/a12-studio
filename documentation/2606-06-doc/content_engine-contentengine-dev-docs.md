# content_engine contentengine dev docs

Content Engine
Introduction
The Content Engine is designed to transform content models into interactive and visually appealing
user interfaces using A12 Widgets. It converts content models into React components, ensuring
consistent styling and behavior across applications. This engine supports extensive customization
of styles, layouts, and interactions, making it adaptable to various project requirements.
The Content Editor offers a developer-friendly interface for creating and managing content. It
features a WYSIWYG editor with drag-and-drop functionality and inline editing, simplifying the
content creation process. Integrated seamlessly with the Content Engine, the Content Editor ensures
that all modifications are accurately reflected, allowing for consistent and flexible content
management.
Content Engine
Getting Started
Installation
Content Engine is provided as an npm package. The following command installs the necessary npm
packages:
npm install @com.mgmtp.a12.contentengine/contentengine-core
In addition, it is necessary to install other A12 packages as peer dependencies of the Content
Engine, such as Base, Widgets, Client, and others.
Setup
Create Content Model
As usual, to have a Content Engine, you need to create a Content Model. The Content Model is a
JSON object that describes the structure of the content. The Content Model is used to create the
Content Editor, which is used to create and edit content.
NOTE The Content Editor is not a part of the Simple Model Editor (SME) like other UI
models.
Update the Application Model
Assume we have a Content Model with the name welcome-page. To add the Content Model to the
Application Model, follow these steps:
1

-- 1 of 31 --

1. Add a new menu item to the Application Model for the Content Engine view
2. Add a new scene to the Application Model to configure the view
{
"header": {},
"content": {
"region": { },
"defaultRegion": [],
"modules": [
{
"name": "welcome-module",
"menu": {
"name": "welcome-menu",
"initialActivity": {
"withoutData": true,
"descriptor": {
"model": "welcome-page"
}
}
},
"flows": [
{
"name": "welcome-flow",
"scenes": [
{
"name": "welcome-scene",
"matchConditions": [
{
"key": "model",
"mustEqual": "welcome-page"
}
],
"sceneChange": {
"onEnter": [
{
"type": "REGION_CLEAR"
},
{
"type": "VIEW_ADD",
"name": "ContentEngineView",
"models": [
{
"modelType": "content",
"name": "welcome-page"
}
]
}
]
}
}
2

-- 2 of 31 --

]
}
]
}
]
}
}
IMPORTANT
The name field in the VIEW_ADD action (ContentEngineView) must match the
component name registered in your ViewComponentProvider (see next
section).
WARNING
Though the withoutData field in the initialActivity indicates that the view
does not need any data to be loaded initially, after reloading the application, if
the default Client deep linking is enabled, errors will be thrown because the
view cannot find the model to load. This is a known limitation of the Client
deep linking feature.
Register to ViewComponentProvider
The Content Engine view must be registered with the application’s ViewComponentProvider to
make it available for rendering.
Register to ViewComponentProvider
import * as React from "react";
import { type View, FrameFactories } from "@com.mgmtp.a12.client/client-core";
import { DefaultElementLibraryFactories } from
"@com.mgmtp.a12.contentengine/contentengine-default-element-library";
export function createViewProvider() {
const crudViewProvider = createEngineViewProvider();
return function viewProvider(componentName: string): React.ComponentType<View> {
return (
crudViewProvider(componentName) ||
FrameFactories.viewProvider(componentName) ||
(() => <Placeholder componentName={componentName} />)
);
};
}
const Placeholder: React.FC<{ componentName: string }> = ({ componentName }) => {
return <div>ERROR: NO CONTAINER FOUND for {componentName}</div>;
};
function createEngineViewProvider(): (componentName: string) =>
React.ComponentType<View> | undefined {
3

-- 3 of 31 --

const components: { [name: string]: React.ComponentType<View> | undefined } = {
// OverviewCRUD: (props) => <CRUDViews.OverviewEngineView {...props} />,
// FormCRUD: (props) => <CRUDViews.FormEngineView {...props} />,
// The component name here should be matched with the scene Directive
"VIEW_ADD" in the application model
ContentEngineView: (props) => <DefaultElementLibraryFactories.ViewComponent
{...props} />
};
return (name) => components[name];
}
Key points about the registration:
• The component name ContentEngineView in the provider must match the name field used in the
Application Model’s VIEW_ADD action
• The DefaultElementLibraryFactories.ViewComponent provides the default Content Engine
rendering capabilities
• A fallback Placeholder component is provided for unregistered components
Setup Application
The application setup depends on whether you’re using static or dynamic content models:
For static content models: Only the ElementLibraryRegistry registration is required.
For dynamic content models: Both the ElementLibraryRegistry and additional data handling
components (DataProvider and DataReducers) must be registered.
Register the ElementLibraryRegistry and Data Handlers
import { ApplicationFactories, type ApplicationSetup } from
"@com.mgmtp.a12.client/client-core";
import { DefaultElementLibrary } from "@com.mgmtp.a12.contentengine/contentengine-
default-element-library";
import { ContentEngineFactories, ElementLibraryRegistry } from
"@com.mgmtp.a12.contentengine/contentengine-core";
export function setup(): ApplicationSetup {
ElementLibraryRegistry.get().addEntry(DefaultElementLibrary.get());
return ApplicationFactories.createApplicationSetup({
...otherConfigurations,
dataHandlers: [
// other data handlers
ContentEngineFactories.createDataProvider()
],
dataReducers: [
// other data reducers
...ContentEngineFactories.createDataReducers()
4

-- 4 of 31 --

]
});
}
Setup Components Explained
ElementLibraryRegistry
Registers the default element library that provides standard Content Engine components (text,
images, containers, etc.)
DataProvider
Handles data fetching and management for dynamic content models
DataReducers
Manage state changes and data transformations for content operations
TIP For production applications, consider implementing custom element libraries by
extending the default library to meet your specific requirements.
Key Concepts
Content Model
The Content Model provides a blueprint for structuring, storing, and rendering content within
applications. It follows the base A12 model schema, with the primary difference being the top-level
content field. This model defines various types of content elements, enabling the creation of
dynamic and interactive user interfaces.
Elements can be categorized into built-in and custom types, allowing for standardized and project-
specific content. These elements can be nested to create hierarchical structures, which facilitate
well-organized content layouts. For example, a grid element can contain multiple row and column
elements, each holding other content elements like text, images, or videos. This hierarchy ensures
that content is structured logically and can be easily managed and manipulated.
Content Model interface (simplified)
import { type Header as BaseHeader } from "@com.mgmtp.a12.base/base-model-api";
export interface ContentModel {
readonly header: ContentModel.Header;
readonly content: ContentModel.Content;
}
export namespace ContentModel {
export interface Header extends BaseHeader {
readonly modelType: "content";
}
5

-- 5 of 31 --

export interface Content {
readonly root?: Node;
readonly configuration?: Configuration;
}
export interface Node<Props = unknown> extends IdName {
/**
* Node's type
*/
readonly type: string;
/**
* Node's properties
*/
readonly props: Props;
/**
* Node's children
*/
readonly children?: Node[];
/**
* A reverse domain, dot-separated, lowercase string that uniquely identifies
the group of related elements
*/
readonly namespace: string;
}
export interface IdName {
readonly id: string;
readonly name?: string;
}
export interface Configuration {
readonly baseGroupId?: string;
readonly namespaceVersions?: Record<string, string | undefined>;
}
}
Element
An Element is an abstract type-level entity, similar to a class in programming. It defines the
properties that its instances (that we will call element nodes or just nodes) will have. A Content
model can contain multiple nodes of the same element.
Node interface
export interface Node<Props = unknown> extends IdName {
/**
* Node's type
*/
readonly type: string;
/**
6

-- 6 of 31 --

* Node's properties
*/
readonly props: Props;
/**
* Node's children
*/
readonly children?: Node[];
/**
* A reverse domain, dot-separated, lowercase string that uniquely identifies the
group of related elements
*/
readonly namespace: string;
}
export interface IdName {
readonly id: string;
readonly name?: string;
}
For example, an engine team can define a Badge element with properties like variant, count, and
tiny.
JSON representation of a Badge node
{
"id": "badge-a7b8d6e3",
"namespace": "com.mgmtp.a12.my-engine",
"type": "Badge",
"props": {
"variant": "warning",
"count": "3",
"tiny": true
},
"children": []
}
Below is some important information about the element node:
Namespace
The namespace is a reverse domain, dot-separated, lowercase string that uniquely identifies a group
of related elements. It helps prevent naming conflicts between different elements with the same
name, e.g, one project may have a Table element while another project may also have a Table
element, but they are in different namespaces.
TIP The Content Engine use the namespace com.mgmtp.a12.contentengine for its default
elements.
7

-- 7 of 31 --

Type
The type is a string that identifies the type of the element, e.g, Box, Text, Image, etc.
Id
The id is a unique identifier for a node within a content model.
Properties
The props field contains the properties of the node. The properties can be any type, including
primitive types, objects, or arrays. It is up to the element module to interpret and render these
properties. For example, a Badge element may have properties like variant, count, and tiny.
Children
The children field contains an array of child nodes. This field is used to create a hierarchical
structure of nodes.
Version
Instead of including version in node level, it is declared in the
content.configuration.namespaceVersions map with the key is the namespace and the value is the
version of the namespace, e.g, the following model declares that the com.mgmtp.a12.my-engine
namespace is at version 1.0.0, which means that all elements in this namespace are at version
1.0.0.
A part of a Content model with namespace version
{
"header": {},
"content": {
"root": {},
"configuration": {
"namespaceVersions": {
"com.mgmtp.a12.my-engine": "1.0.0"
}
},
}
}
Element Module
An Element Module (or Module) defines how an element behaves within the Content Engine. It
acts as a blueprint, detailing the rendering and interaction logic for the element. The simplest
interface for an Element Module includes an identifier (see below), a renderer and an optional
version field. The identifier specifies which element the module corresponds to, while the renderer
is a React component receives the modeled properties and returns the corresponding UI
component.
8

-- 8 of 31 --

ElementModule interface (simplified)
export interface ElementModule<Node extends ContentModel.Node = any> {
/**
* The unique identifier of the module.
*/
readonly id: ElementModule.Identifier;
/**
* The version of the module.
*/
readonly version?: string;
/**
* The renderer for the node type.
*/
readonly renderer: React.ComponentType<NodeRendererProps<Node>>;
}
Identifier
The Element Module Identifier is a concentrated string between the namespace and the type of the
element, i.e: <namespace>:<type>.
For example, the identifier for a Badge element in the com.mgmtp.a12.my-engine namespace is
"com.mgmtp.a12.my-engine:Badge". In runtime, to find the responsible module for a node, the Content
Engine will join the namespace and the type of the node to get the identifier, then use it to find the
corresponding module.
Version
The version field is an optional string that specifies the version of the module. It should match
identically the version of the element in the content model, which is declared in the
content.configuration.namespaceVersions map.
Renderer
The renderer is a React component that receives the modeled properties and returns the
corresponding UI component.
Element Library
An Element Library is a collection of element modules managed together. It facilitates the
organization and usage of various modules within the Content Engine. Each library has an
identifier and methods to retrieve modules, making it easier to manage and utilize a consistent set
of modules across different parts of the application.
The Element Library serves several key purposes. It centralizes management by grouping element
modules, simplifying updates and maintenance. It ensures consistency by using the same set of
modules throughout the application. Libraries promote reusability, reducing redundancy and
9

-- 9 of 31 --

improving development efficiency. Additionally, they allow for customization by combining built-in
and custom elements tailored to specific project needs, enabling easy adaptation and extension of
the Content Engine’s capabilities.
Element Library interface
export type ElementLibrary = Library<ElementModule>;
export interface Library<Module extends Identifiable = Identifiable, Configuration =
unknown> extends Identifiable {
/**
* The modules in the library.
*/
readonly modules: Module[];
/**
* The configuration of the library.
*/
readonly configuration: Configuration;
}
A library (e.g., ElementLibrary) can be created by constructing a plain object that implements the
Library interface.
To create a modified version of an existing library—such as adding or removing
modules—standard JavaScript techniques can be used to produce a new object based on the
original, ensuring immutability is preserved where needed.
Features
Content Engine Actions
The EngineAction interface defines a standardized representation of user interaction events within
the Engine. By encapsulating essential details, such as the target node and the contextual
information in which the event occurs, it enables a structured, consistent approach to event
handling.
EngineAction interface
import { type Action } from "@com.mgmtp.a12.client/typescript-fsa-redux-5-compat";
export type EngineAction = Action<EngineAction.Payload>;
export namespace EngineAction {
export interface Payload {
/**
* The node that triggered the event.
*/
readonly target: Target;
/**
10

-- 10 of 31 --

* The path to the group instance that triggered the event.
*/
readonly groupInstancePath: string;
}
export interface Target extends Pick<ContentModel.Node, "namespace" | "type" |
"id"> {}
}
The EngineAction is specific to the Engine and does not have awareness of the context to which it
belongs. This can be problematic when multiple instances of the Engine are required, potentially
leading to conflicts between instances.
To address this, the ContentEngineAction.EventPayload interface is introduced, enabling the
addition of context to core EngineAction events. This is primarily achieved by including the
activityId, which identifies the activity to which the event belongs.
ContentEngineAction.EventPayload interface
export namespace ContentEngineAction {
export interface EventPayload {
readonly activityId: string;
readonly engineAction: EngineAction;
}
}
NOTE Content Engine does not have Command action concept like other Engines or the
Content Editor.
This action is defined inside the Element Module, and the Content Engine will dispatch it when an
event is triggered by a user interaction through elements.
Handling Node Events
Developers need to listen for these actions to handle node events accordingly. Node events are used
to react to changes or actions performed on nodes within the system. Please refer to the Event
section for how to configure the events at the element level. You can implement event handlers in
multiple ways:
Using Reducers
The most direct approach is to handle events in reducers, which provides immediate response to
user interactions:
Handle Node Event with Activity Reducer
import { type UnknownAction } from "redux";
import { ContentEngineAction } from "@com.mgmtp.a12.contentengine/contentengine-core";
11

-- 11 of 31 --

import { type Activity, type ActivityReducers } from "@com.mgmtp.a12.client/client-
core";
import { ButtonNode } from "@com.mgmtp.a12.contentengine/contentengine-default-
element-library";
export const contentEngineEventDataReducer: ActivityReducers.DataReducer = {
reduce(
dataHolders: Activity.DataHolder[],
action: UnknownAction,
_defaultDataHolder?: Activity.DataHolder
): Activity.DataHolder[] {
if (ContentEngineAction.event.match(action)) {
if (ButtonNode.onClick.match(action.payload.engineAction)) {
// handle the button click event
// perform synchronous data transformations
return updateDataHolders(dataHolders, action.payload);
}
}
return dataHolders;
}
};
Using Sagas
For complex asynchronous workflows and side effects, implement event handlers as sagas:
Handle Node Event with Sagas
import { takeLatest, type SagaGenerator } from "typed-redux-saga";
import { ContentEngineAction } from "@com.mgmtp.a12.contentengine/contentengine-core";
import { ButtonNode } from "@com.mgmtp.a12.contentengine/contentengine-default-
element-library";
export function* handleContentEngineEventSaga(): SagaGenerator<void> {
yield* takeLatest(ContentEngineAction.event.match, (action) => {
const { engineAction } = action.payload;
if (ButtonNode.onClick.match(engineAction) && engineAction.payload.eventName)
{
alert(
`Button clicked with event name "${engineAction.payload.eventName}" in
context "${engineAction.payload.groupInstancePath}"`
);
}
});
}
Sagas are ideal for handling complex asynchronous operations, such as:
12

-- 12 of 31 --

• Making API calls in response to node changes
• Orchestrating multi-step workflows
• Managing long-running processes
• Coordinating between multiple systems
Using Middlewares
For simpler synchronous processing and transformations, middlewares provide a clean approach:
Handle Node Event with Middlewares
import type { Middleware } from "redux";
import { ContentEngineAction } from "@com.mgmtp.a12.contentengine/contentengine-core";
import { InteractiveTileNode } from "@com.mgmtp.a12.contentengine/contentengine-
default-element-library";
export function createContentEngineEventHandlerMiddleware(): Middleware {
return () => (next) => (action) => {
if (ContentEngineAction.event.match(action)) {
const { engineAction } = action.payload;
if (InteractiveTileNode.onClick.match(engineAction) &&
engineAction.payload.eventName === "my-event") {
alert(
`Interactive tile clicked with event name
"${engineAction.payload.eventName}" in context
"${engineAction.payload.groupInstancePath}"`
);
}
}
return next(action);
};
}
Middlewares are suitable for:
• Data validation and transformation
• Logging and auditing
• Simple synchronous business logic
• Cross-cutting concerns like authentication checks
Registration
You can register your event handlers in two locations:
• Content Engine Module: Register handlers specific to your module’s functionality
13

-- 13 of 31 --

• Central AppSetup: Register global handlers that affect the entire application
This flexibility allows you to organize your event handling logic based on scope and responsibility.
RequestSelectorMap
NOTE This API is marked as experimental. Breaking changes might happen even in minor
releases.
The RequestSelectorMap can be used to customize the requests of the default data provider of the
Content Engine. The Content Engine internally uses a default variant of this map, but you can
provide your own implementation containing your customizations. Then it will be used in place of
the default one.
For example, customizing the way the Data Provider loads a single document can look like this:
import {
type Module,
type DataProvider,
type ActivityReducers,
type ApplicationSetup
} from "@com.mgmtp.a12.client/client-core";
import {
ContentEngineFactories,
type RequestSelectorMap,
DefaultRequestSelectorMap
} from "@com.mgmtp.a12.contentengine/contentengine-core";
/**
* Here, requests for loading documents are customized with another parameter.
* Instead of reusing the default, you could also create your own.
*/
export const CustomRequestSelectorMap: RequestSelectorMap = {
...DefaultRequestSelectorMap,
load(config) {
return (state) => {
const defaultRequest = DefaultRequestSelectorMap.load(config)(state);
return {
...defaultRequest,
params: {
...defaultRequest.params,
additionalParam: "customValue"
}
};
};
}
};
/**
14

-- 14 of 31 --

* Depending on how you set up your application, you can now use the
`CustomRequestSelectorMap`
* when creating the data provider for the Content Engine.
*/
/**
* Via a single Content Engine Client Module:
* <project>/client/src/modules/index.ts
*/
export const ALL_MODULES: Module[] = [
// other modules...
ContentEngineFactories.createModule({ requestSelectorMap: CustomRequestSelectorMap
})
];
/**
* Or via application setup with separate module components:
* <project>/client/src/appsetup.ts
*/
export function setup(): ApplicationSetup {
const dataHandlers: DataProvider[] = [
// other data handlers...
ContentEngineFactories.createDataProvider({ requestSelectorMap:
CustomRequestSelectorMap })
];
return ApplicationFactories.createApplicationSetup({
dataHandlers,
dataReducers: [
// other data reducers...
...ContentEngineFactories.createDataReducers()
]
// other configurations...
});
}
Note that it is mandatory to spread the DefaultRequestSelectorMap when customizing.
This customization approach can also be used in combination with the RequestFilter API (described
in the Data Services documentation), for example to use your own operation methods. Customizing
the RequestFilter alone would not be enough when the method replacement needs some context
(e.g. only overriding methods in certain conditions). Using the RequestSelectorMap could then be
used to provide this context down for the filter to use.
Screen Reader Column
NOTE This API is marked as experimental. Breaking changes might happen even in minor
15

-- 15 of 31 --

releases.
The Screen Reader Column feature improves accessibility for tables that contain interactive
elements such as buttons, interactive tiles, or checkboxes in action columns. When a table has
multiple rows with identically labeled actions (e.g. several "Delete" buttons), screen reader users
cannot distinguish which button belongs to which row. This feature connects those interactive
elements with a descriptive column’s content via aria-labelledby, so a screen reader announces
both the action label and the row context (e.g. "Delete, Product ABC").
How It Works
The table node accepts an optional screenReaderColumnRef property that references a column by its
ID. When configured:
1. For each body row, the table resolves the cell in the referenced column and makes its DOM ID
available via a React context.
2. Interactive elements (Button, InteractiveTile) in action columns read this cell ID using the
useTableScreenReaderContext hook.
3. They set aria-labelledby on themselves, referencing both their own ID and the screen reader
cell’s ID.
4. The browser and screen reader natively resolve the combined label from both referenced
elements.
This approach is robust because it delegates text resolution to the browser — no manual text
extraction is needed.
Configuration
To enable the feature, set the screenReaderColumnRef property on the Table node in your Content
Model to the ID of the column whose content should provide row context:
Content Model excerpt
{
"type": "Table",
"namespace": "com.mgmtp.a12.contentengine",
"props": {
"columns": [
{ "id": "name-col", "width": "1fr" },
{ "id": "action-col", "width": "auto", "actionColumn": true }
],
"screenReaderColumnRef": "name-col"
}
}
Requirements
• The referenced column (screenReaderColumnRef) must contain descriptive text that identifies the
16

-- 16 of 31 --

row (e.g. a name or title).
• The column with interactive elements must have actionColumn: true.
• The screen reader column and the action column must not be the same column.
Supported Elements
The following built-in elements automatically use the screen reader context when placed inside an
action column cell:
• Button — sets aria-labelledby combining the button ID and the screen reader cell ID
• InteractiveTile — sets aria-labelledby via htmlAttributes
Custom Element Integration
Custom element modules can consume the screen reader context using the
useTableScreenReaderContext hook exported from @com.mgmtp.a12.contentengine/contentengine-core.
The hook returns undefined for screenReaderCellId when the element is not inside an action
column, so it is safe to use unconditionally.
Custom element using screen reader context
import * as React from "react";
import { type ContentModel, useTableScreenReaderContext } from
"@com.mgmtp.a12.contentengine/contentengine-core";
export const CustomActionModule =
moduleFactory<CustomActionNode.Props>("CustomAction", ({ node }) => {
const { id, props } = node;
// Read the screen reader cell ID from the table context.
// Returns undefined when the element is not inside an action column.
const screenReaderCellId = useTableScreenReaderContext((context) =>
context.screenReaderCellId);
return (
<button
id={id}
aria-label={props.label}
onClick={() => {
/* handle click */
}}
aria-labelledby={screenReaderCellId ? `${id} ${screenReaderCellId}` : id}>
{props.label}
</button>
);
});
export type CustomActionNode = ContentModel.Node<CustomActionNode.Props>;
17

-- 17 of 31 --

export namespace CustomActionNode {
export interface Props {
readonly label?: string;
}
}
The key pattern is:
• Read screenReaderCellId from the context.
• If present, set aria-labelledby to "${ownId} ${screenReaderCellId}" — this tells the screen reader
to announce both the element’s own label and the referenced cell’s content.
• Keep aria-label as a fallback for when the element is used outside a table.
WARNING This section is under development.
Content Editor
Content Editor Actions
The editor actions are divided into two types: Events and Commands.
Events signal that something has happened in the UI, triggered by a modeler interaction. For
example select an element in Content Editor. They are handled by middlewares and will never
change the state directly. They can be dispatched by users, for example Events.onSelectNode to
select specific Element in Content Engine. Developers are also encouraged to listen to them, for
example to Events.onDeleteNode to get notified about an element being deleted from the Engine.
Commands are used to directly modify the Redux state. They are dispatched by other
Events/Commands and are usually implemented in reducers. Users are encouraged to dispatch
them, for example Commands.setSelectedNode to disable the UI, but are NOT encouraged to listen to
them. Which commands are dispatched in what order and by what user interaction is considered
an implementation detail and a change is not considered breaking.
Why are actions separated into events and commands?
This helps to understand the runtime behavior better, because you can rely on the effect of actions
only creating other actions or changing the state. This makes maintenance, customizing, and
debugging easier.
It also serves as a reminder not to listen to commands when you want to react to some user
interaction. Which event is dispatched by what user interaction is fixed. Which commands are
dispatched as a result might change and should not be relied on.
Events
All UI-Events trigger the dispatching of a Redux action. The behavior, which gets triggered by these
events, can be changed by registering custom middlewares.
18

-- 18 of 31 --

The following table describes what each action does and what user interaction leads to the action.
For a more details e.g.: action’s payloads, please refer to the API documentation.
Event Description Dispatched by
onChangeConfiguration change the Content Editor
configuration
the settings button in the
toolbar
onChangeSetting change an element’s properties
via its setting UIs
the settings panel
newOnChangeSetting change an element’s properties
via Redux
the settings panel
onSelectNode select an element a click event on an element in
the canvas or tree panel
onToggleConditionalNodeVisibil
ity
toggle the visibility of the
Conditional node element on
the canvas
the preview button on the
Conditional node tree element
onInsertNode insert an element the insert button in the context
menu
onMoveNode move an element the move buttons in the context
menu and the toolbar
onDeleteNode delete an element the delete button in the context
menu and the toolbar
onCopyNode copy an element the copy button in the context
menu and the toolbar
onCutNode cut an element the cut button in the context
menu and the toolbar
onPasteNode paste an element the paste button in the context
menu and the toolbar
onDuplicateNode duplicate an element the duplicate button in the
context menu
onInlineEditNode allow raw editing of an
unknown element inside the
Content Model
the inline edit button in the
settings panel
onConfirmDialog confirm a dialog action the confirm button in the dialog
onCancelDialog cancel a dialog action the cancel button in the dialog
onClickSettingPanelButton open or close the settings panel the settings panel toggle button
in the toolbar
onClickComponentPanelButton open or close the component
panel
the component panel toggle
button in the toolbar
19

-- 19 of 31 --

Commands
All direct state changes are coming from these actions. However, it is not recommended to listen to
these actions directly. What Commands are dispatched at what time and in what order is
considered an implementation detail and may change any time in a non-breaking way.
It is possible to register custom reducers to take care about how information is stored. But be aware
that the engine components always need the store in a special structure to be able to render!
The following table shows an overview of all Commands.
For a detailed description, including the specific payloads, please refer to the API documentation.
Commands Description State Change
setModel set Content Model contentModel in ModelState
setSelectedNode set selected node state selectedNode in UIState
setSettingScreens set list of setting screens settingScreens in UIState
setInsertPanel set insert panel’s information insertPanel in UIState
setShowSettingPanel set setting panel’s visibility showSettingPanel in UIState
setShowComponentPanel set component panel’s visibility showComponentPanel in UIState
setDialog set dialog information dialog in UIState
setVisibleConditionalNodes set map of visible Conditional
nodes
visibleConditionalNodes in
UIState
updateSettingsState update a specific Content
Model’s element following the
selected node
a specific node of contentModel
in ModelState
resetSettingsState reset setting state from selected
node
settingsState of the selected
node in UIState
rescueSettingsState revert to the latest valid setting
state
settingsState of the selected
node in UIState
Client Actions
The commands and events are specific to the engine and doesn’t aware of the context where it
belongs to. This can be a problem when multiple instances of an engine are required thus can
generate conflict between instances. Therefore, the ContentEditorClientActions namespace is
introduced to allow adding context to the core events and commands.
The main point of this namespace is to add extra layer of information, like including the activityId
and other Client’s related information. The following table shows an overview of every action.
For a detailed description, including the specific payloads, please refer to the API documentation.
20

-- 20 of 31 --

Actions Description
ContentEditorClientActions.event A wrapper action for Editor Events while also
includes the activityId
ContentEditorClientActions.command A wrapper action for Editor Commands while
also includes the activityId
WARNING This section is under development.
Content Model Migration
Installation
npm install -D @com.mgmtp.a12.contentengine/contentengine-model-migration
CLI Usage
npx content-model-migration <path>
Where <path> is the relative or absolute path to the content model file, or the directory containing
the files.
For other options, run npx content-model-migration --help.
If the project is using other elements from 3rd-party packages and they deliver their own Migrator
(see below), it may need to have a configuration file to configure the migration process.
First, create a file named content-model-migration.config.ts in the same level with package.json file
where declare the above package. Then, import the Migrator from the 3rd-party package and add it
to the migrators array in the configuration file, e.g:
content-model-migration.config.ts
import { defineConfig, DefaultMigrator } from
"@com.mgmtp.a12.contentengine/contentengine-model-migration";
import { OtherPackageMigrator } from "other-package";
export default defineConfig({
migrators: [DefaultMigrator, OtherPackageMigrator]
});
The tool will automatically detect the configuration file and use the migrators array to run the
migration process.
21

-- 21 of 31 --

TIP
The file name can be changed to any name, with the extension .ts or .js (CommonJS
format). In that case, the --config option can be used to specify the path to the
configuration file.
API Usage
For element module developers, the Migrator interface is provided to create a migration script for
their elements. They can freely decide where to store the migration logic, but to reduce the bundled
size of the final application, it is recommended to create a separate package for the migration logic.
Assume that the Badge element needs to be migrated to make the size field required. In that
package’s src folder, create a folder named v0.0.0 with a file named badge.ts inside. Copy the
current interface of the Badge element, e.g:
v0.0.0/badge.ts
import type { ContentModel } from "@com.mgmtp.a12.contentengine/contentengine-core";
export type BadgeNode = ContentModel.Node<BadgeNode.Props>;
export namespace BadgeNode {
export interface Props {
readonly size?: "sm" | "md" | "lg";
}
}
Then, create another folder named v1.0.0 with the same file named badge.ts inside. Update the
interface of the Badge element, and provide the transform function to convert the old node to the
new node, e.g:
v1.0.0/badge.ts
import type { ContentModel } from "@com.mgmtp.a12.contentengine/contentengine-core";
import type { BadgeNode as OldBadgeNode } from "../v0.0.0/badge";
export type BadgeNode = ContentModel.Node<BadgeNode.Props>;
export namespace BadgeNode {
export interface Props {
readonly size: "sm" | "md" | "lg"; // It is required now
}
}
export function transformBadge(oldNode: OldBadgeNode): BadgeNode {
return {
...oldNode,
props: {
...oldNode.props,
size: oldNode.props.size ?? "md"
}
22

-- 22 of 31 --

};
}
Finally, create a file named index.ts in the root of the package to export the Migrator object, so
other projects can import it in their configuration file, e.g:
index.ts
import { Transform, type Migrator } from "@com.mgmtp.a12.contentengine/contentengine-
model-migration";
import { transformBadge } from "./steps/v1.0.0/badge";
export const MyEngineMigrator: Migrator = {
namespace: "com.mgmtp.a12.my-engine",
steps: [
{
version: "0.0.0"
},
{
version: "1.0.0",
transform: Transform.combineWithMap({ Badge: transformBadge })
}
]
};
API Documentation
Content Engine & Content Editor API documentation can be found:
• Content Engine API documentation
• Content Default Element Library API documentation
• Content Editor API documentation
• Content Model Migration Tool API documentation
Breaking Change Management
For the general definition of breaking and non-breaking changes in the A12 platform, as well as the
frontend and backend perspectives, see the A12 Breaking Change Management page.
The following section describes how this general definition is interpreted for the Content Engine.
Public API Surface
Each Content Engine package (contentengine-core, contentengine-editor, contentengine-element-
library, contentengine-model-migration) defines its public API through the exports in its index.ts.
23

-- 23 of 31 --

Any removal of, or incompatible change to, an exported symbol is a breaking change. Additions to
the public API are non-breaking.
Exports annotated @experimental may change in minor releases without being considered breaking.
Exports annotated @internal are not part of the public API and carry no stability guarantee. The
public API surface is tracked per package via API Extractor (index.api.md); an unintended diff there
signals an API change that must be classified.
Content Model Format
The persisted content model is a versioned format owned by the Content Engine. Only changes that
are incompatible with existing content models are breaking: for example, removing a required
field or changing the semantics of an existing field.
Adding an optional field is non-breaking even though it triggers a content model version bump
and a corresponding migration step. When such a change is introduced, the
@com.mgmtp.a12.contentengine/contentengine-model-migration tool handles the version upgrade
automatically — see the Content Model Migration chapter. Every model-format change ships with
its migration step, and the affected release carries a matching entry in the Migration Instructions
chapter.
Element Library Contract
The element module contract — the runtime moduleFactory API, the editor element factory() API,
and the registration shapes consumed by DefaultElementModules / DefaultEditorElementModules — is
part of the public API. Changes that require updates in a consumer’s custom element modules are
breaking changes.
Third-party element packages may ship their own Migrator for the content model migration tool.
Changing the Migrator interface, or the defineConfig configuration contract that registers them, is a
breaking change.
Resource Keys
Adding new entries to the editor resource keys (RESOURCE_KEYS) is non-breaking: applications that
do not override those keys continue to receive the default translation. Removing or renaming an
existing key is a breaking change for any application that provides a custom translation for it.
Redux State Shape and Action Types
The editor store’s Redux action type strings and reducer state shape are part of the public contract.
Renaming or removing an action type, or restructuring the state in a way that breaks existing
selectors, is a breaking change.
Selectors and sagas that are part of the public API follow the standard public-API rule above.
A concrete example of a past breaking change: the Redux 4 → 5 upgrade required consumers to
update peer dependencies and replace the typescript-fsa import path with the compat shim. See
24

-- 24 of 31 --

the Migration Instructions chapter for the migration steps.
Non-Breaking Examples
The following types of changes are consistently treated as non-breaking in the Content Engine:
• Adding a new optional configuration field to an existing API
• Adding new RESOURCE_KEYS entries
• Adding a new element module to the element library
• Adding an optional field to the content model (a migration step still ships, but existing models
keep working)
• Introducing a new @experimental export
• Internal refactoring under src/internal/ that does not affect public exports
• Bumping a transitive dependency with no visible API impact
Migration Instructions
2026.06
0.12.0
Minimum supported model version raised to 0.7.0
Tooling: Content model migration tool, required for affected models — see Content Model
Migration.
The migration tool no longer supports migration of content models before version 0.7.0.
If you need to migrate a content model from an older version, you must first migrate it to version
0.7.0 using the migration tool of that version.
Redux 5 upgrade
Tooling: Client codemod (@com.mgmtp.a12.client/client-codemod migrateTypescriptFsaImports),
optional — automates the typescript-fsa import rewrite only (see Migrate typescript-fsa Imports).
The dependency updates, useSelector typing, and middleware changes are manual.
Content Engine has been upgraded to Redux 5 along with its ecosystem. For a complete list of
breaking changes in Redux 5, refer to the official Redux 5 release notes.
Dependency Updates
The following peerDependency versions have been updated:
Package Old Version New Version
25

-- 25 of 31 --

redux ^4.2.1 ^5.0.0
react-redux ^7.2.8 ^9.2.0
redux-saga ^1.3.0 ^1.3.0
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
This is the single change category the optional Client codemod automates: it rewrites typescript-
fsa import paths to the compat shim. Replace all imports from typescript-fsa with the compat
fork:
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
26

-- 26 of 31 --

// @types/react-redux.d.ts
import type { UseSelector } from "react-redux";
declare module "react-redux" {
export const useSelector: UseSelector<object>;
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
Replace SagaIterator with SagaGenerator
Tooling: none — manual change.
All saga return types have been changed from SagaIterator (from redux-saga) to SagaGenerator
(from typed-redux-saga). This affects the public API of the Content Engine sagas, including the
client-extension data provider’s provideData and the editor’s
ContentEditorClientFactories.createSagas().
If you have custom sagas that explicitly type their return type as SagaIterator, update them to use
SagaGenerator instead:
// Before
import { SagaIterator } from "redux-saga";
function* mySaga(): SagaIterator<void> { /* ... */ }
// After
import { SagaGenerator } from "typed-redux-saga";
27

-- 27 of 31 --

function* mySaga(): SagaGenerator<void> { /* ... */ }
typed-redux-saga is already a transitive dependency via @com.mgmtp.a12.client/client-core.
React peer dependency raised to 19.2
Tooling: none — manual change.
The minimum supported version of react and react-dom in our peer dependency has been raised
from ^19.0.0 to ^19.2.6. Update your package.json accordingly:
{
"dependencies": {
"react": "^19.2.6",
"react-dom": "^19.2.6"
}
}
Raise TypeScript / ES baseline to 6.x / ES2025
Tooling: none — manual change.
The Content Engine npm artifacts are now built with TypeScript 6.x and target ES2025. Consumers
should:
• Use TypeScript 6.x or later; older compilers will reject the emitted .d.ts.
• Ensure their bundler targets browsers with ES2025 support, or include the relevant polyfills.
2025.06-ext2
0.9.0
Split DocumentContext selectors into document / model / events fields
The selectors/callbacks of the DocumentContext have been moved to separate fields to improve
autocompletion and usability:
• Document-related selectors are now available under the document field of the context
• Model-related selectors are now available under the model field of the context
• Event callbacks are now available under the events field of the context
Change ImageNode src to a static/dynamic record
Tooling: Content model migration tool, required for affected models — see Content Model
Migration.
28

-- 28 of 31 --

The src field of the ImageNode.Props interface has changed from string | undefined to
Partial<Record<"static" | "dynamic", string | undefined>>.
This change allows for more flexible image source configuration, supporting both static URLs and
dynamic field references.
2025.06
Please refer to the Migration to latest A12 chapter for an explanation of general steps.
0.8.0
Migrate to ESM
All npm artifacts have been migrated from CommonJS to ESM. When using Node.js 22.12+ and
modern build tools, no changes should be necessary to your bundler setup.
NOTE If your tests depend on mocking/stubbing Content Engine API, consult the
documentation of your test runner on how to work with ES modules.
Migrating your own application to ESM is not required, but recommended. Consult the
documentation of your bundler for specifics.
Update JavaScript output to ES2024
The JavaScript output of the npm artifacts has been updated from ES2020 to ES2024 to enable the use
of latest language features. When using supported browsers, no changes are necessary. If support
for older browsers is required, make sure to include necessary polyfills.
React 19 upgrade
We have dropped support for React 18 and older versions and now require React 19 as our
peerDependency. This means you must perform the React 19 migration, which is described in detail
in the official React 19 Upgrade Guide. They provide codemods that should make the transition
straightforward.
Additionally, we have decided not to include a Redux update to minimize the migration effort. The
"react-redux" library does not have React 19 as a peerDependency, but still works fine with it. One
solution is to override the dependency in your package.json. You also need to update the
corresponding "@types/react-redux" typings to at least version 7.1.34.
{
"overrides": {
"react-redux": {
"react": "^19"
}
},
"devDependencies": {
"@types/react-redux": "7.1.34"
29

-- 29 of 31 --

}
}
Styled-components v6 upgrade
We have dropped support for styled-components v5 and now require v6 as our peerDependency.
Please refer to the styled-components migration guide and Widgets migration notes for more
information.
Rework SettingsTemplate components
In the SettingsTemplate namespace, components have been restructured for improved consistency
and usability:
• SettingsTemplate.SettingBody has been removed without a replacement
• SettingsTemplate.SettingHeading has been renamed to SettingsTemplate.Setting
• The previous SettingsTemplate.Setting has been removed
Rework the data binding API
The data binding APIs have been rewritten to provide a more consistent and powerful foundation
for working with content models.
Interface Renaming
The DataContext interface has been renamed to DocumentContext to better reflect its purpose. While
its core callback signatures remain mostly unchanged, the first argument is now a string-form
version of the EntityInstancePath (e.g., product[1]/addresses[2]/streets[1]), referred to as
DocumentPath, instead of a field or group ID. Correspondingly, the useDataContext hook has been
renamed to useDocumentContext.
New DocumentPathContext
To access path-related information in a structured way, a new context called DocumentPathContext is
now available. It provides the current groupPath, along with two utility methods:
• resolveChildGroupPath - resolves the full path to a child group
• resolveFieldPath - resolves the full path to a specific field
These changes help make path resolution more robust and aligned with a model-driven
architecture.
Removed Components
As part of the cleanup, the following interfaces have been removed:
• The DocumentService interface and its default implementation DefaultDocumentService
• The DocumentProvider interface (was not actively used in the codebase)
30

-- 30 of 31 --

These removals simplify the data binding system and remove unnecessary layers of indirection.
Direct DocumentContext Integration
All default elements are now wired to interact directly with the DocumentContext, both for reading
values and writing changes. This direct interaction ensures a consistent data flow across the
application.
Simplified ContentEngine Setup
Setting up data binding in the ContentEngine component is now much simpler. Previously, it
required supplying a DocumentService and a DocumentProvider. Going forward, there are two
supported approaches:
Option 1: Data Binding Configuration
Pass a dataBindingConfiguration object to the dataBindingConfiguration prop of the ContentEngine
component. This object must include document and documentModel. Optionally, it can also include:
• baseGroupPath to define an inner group path as the data binding base
• onValueChanged callback to handle value updates
When this configuration is used, ContentEngine will internally create a DocumentContext and
configure data binding accordingly.
Option 2: Custom DocumentContextProvider
Alternatively, wrap the ContentEngine component with a custom DocumentContextProvider, which
provides a manually created DocumentContext instance. This approach is suitable for cases requiring
external control over the document context setup.
Minimum supported model version raised to 0.5.0
The migration tool no longer supports migration of content models before version 0.5.0.
If you need to migrate models from an older version, you must first migrate to version 0.5.0 using
the migration tool of that version.
31

-- 31 of 31 --

