# diagram_editor de dev docs

Introduction
The Diagram Editor is a React and Redux-based framework for building interactive diagrams,
where users can:
• Create, delete, move and manipulate nodes and edges
• Connect nodes with edges between ports on nodes
• Pan and zoom the canvas for navigation
Developers can: * customize the appearance of existing elements * extend the diagram by custom
nodes, edges and ports * implement custom behavior for user interactions
Getting Started
Installation
Install the required package:
npm install @com.mgmtp.a12.diagrameditor/diagrameditor
Install the peer dependencies:
npm install @com.mgmtp.a12.widgets/widgets-core @reduxjs/toolkit react styled-
components
The Diagram Editor can be used in a pure React-Redux application as well as in an A12 Client
Application. Please refer to the Redux Toolkit documentation to see how to integrate the
components in a pure React-Redux application.
Usage with A12 client
Load data
import { createDiagramState } from
"@com.mgmtp.a12.diagrameditor/diagrameditor/dist/renderer/store/slice";
// Loading the data via a data provider into an activity is just a suggestion. The
main point is that the diagram state needs to be stored anywhere in the Redux state
export const myDataProvider: DataProvider = {
name: "myDataProvider",
canHandle(config) {
return config.dataHolder.descriptor.x === "x"; // Depends on your activity
},
*provideData(config) {
1

-- 1 of 8 --

const { activityId } = config;
if (config.operation === "load") {
yield* put(ActivityActions.setData({ activityId, data: createDiagramState() }));
}
}
};
Register the reducer
import { DiagramState } from
"@com.mgmtp.a12.diagrameditor/diagrameditor/dist/core/state";
import { createDiagramReducer } from
"@com.mgmtp.a12.diagrameditor/diagrameditor/dist/renderer/store/reducer";
function isMyDataHolder(dh: Activity.DataHolder): dh is Activity.DataHolder {
return dh.descriptor.x === "x"; // Depends on your activity
}
const diagramReducer = createDiagramReducer();
// Connecting the diagram reducer with the activity data reducer of A12 Client
export const myDataReducer: ActivityReducers.DataReducer = {
reduce(dataHolders, action) {
const dataHolder = dataHolders.find(isMyDataHolder);
if (!dataHolder?.data || Object.keys(dataHolder.data).length === 0) {
return dataHolders; // alternatively create the initial diagram state here
instead
}
const newData = diagramReducer(dataHolder.data as DiagramState, action);
return [{ ...dataHolder, data: newData }];
}
};
Create a View
import { View } from "@com.mgmtp.a12.client/client-core/lib/core/view";
import { A12DiagramWidget } from
"@com.mgmtp.a12.diagrameditor/diagrameditor/dist/a12Client/a12DiagramWidget";
import { DiagramState } from
"@com.mgmtp.a12.diagrameditor/diagrameditor/dist/core/state";
export function MyDiagramView(props: View) {
// The Diagram Widget expects a selector to where the data is stored. This enables
you to store the data anywhere within the Redux state
const selectDiagramState = useCallback(
(state: object) => ActivitySelectors.data(props.activityId)(state) as
DiagramState,
2

-- 2 of 8 --

[props.activityId]
);
return <A12DiagramWidget activityId={props.activityId}
selectDiagramState={selectDiagramState} />;
}
Bring everything together in the A12 Client
const setup = ApplicationFactories.createApplicationSetup({
...restSetup,
dataHandlers: [myDataProvider],
dataReducers: [myDataReducer],
});
export function MyApp() {
return (
<Provider store={setup.store}>
<OtherProviders>
<RegionUI
...otherRegionProps,
viewProvider={viewProvider}
/>
</OtherProviders>
</Provider>
);
}
function viewProvider(name: string) {
if (name === "MyDiagramView") {
return MyDiagramView;
}
return FrameFactories.viewProvider(name);
}
Diagram State
The Diagram State contains the diagram and additional UI state information.
interface DiagramState {
diagram: Diagram;
ui: {
zoomLevel: number;
offset: { left: number; top: number };
selectedElements: Record<string, true>;
readonlyElements: Record<string, true>;
showGrid: boolean;
3

-- 3 of 8 --

...otherProps
};
}
Diagram
The Diagram data structure contains nodes and edges. The key of the records is the id of the
element.
interface Diagram {
nodes: Record<string, DiagramNode>;
edges: Record<string, DiagramEdge>;
}
Every DiagramElement has the property customType to allow developers to mark their custom
elements. If the property is set, an own React Component must be registered as well.
Each Node can have multiple Ports, on which edges can be attached to. They are only visible if the
node is hovered or focussed.
interface DiagramNode extends DiagramElement {
id: string;
type: "node";
label: string;
ports: PortMap;
x: number;
y: number;
width: number;
height: number;
customType: "myNodeType"
}
Each middle point of a Port is positioned relative to the top left corner of its parent node.
export interface DiagramPort extends DiagramElement {
id: string;
type: typeof DEFAULT_PORT_TYPE;
width: number;
height: number;
offset: { left: number; top: number; };
customType: "myPortType"
}
Each Edge consists of at least two Anchors, which are the points that describe an edge route. The
default edge is orthogonal.
4

-- 4 of 8 --

export interface DiagramEdge extends DiagramElement {
id: string;
type: "edge";
anchors: { id: string; x: number; y: number }[];
sourceNodeId: string;
sourcePortId: string;
targetNodeId: string;
targetPortId: string;
customType: "myEdgeType"
}
Event Flow
The event flow in the diagram editor is designed to be unidirectional, following a straight path from
user interactions to state updates. Here’s a high-level overview of the event flow:
1. User Interaction: The user interacts with the diagram (e.g., dragging a node, connecting ports).
2. Event Dispatching: These interactions are dispatched as Redux actions via the action creators
of a12DiagramActions.
3. Enablement: It is checked if the current action is allowed based on the ui state and/or the
enablement rules defined in the diagramReducer. If the action is denied and a backup exists,
the backup will be restored.
4. State Update: If the action is enabled, the corresponding reducers process these actions and
update the diagram state in the Redux store.
Recipes
Modifying the Diagram State
Modifying the Diagram State should be done by dispatching actions from a12DiagramActions. For
more complex or project related scenarios, you can also create custom actions and reducers to
handle specific state changes.
The recommended way to create a custom reducer is to pass it into the createDiagramReducer
function. Since the diagram state can be changed in an arbitrary way, you need to be careful here
what is updated. E.g. if a node is dragged and your custom reducer deletes said node, an exception
will occur. This way we offer maximum flexibility at the cost of the developer experience.
import { defaultDiagramReducer } from
"@com.mgmtp.a12.diagrameditor/diagrameditor/dist/renderer/store/slice";
function myCustomReducer(state: DiagramState, action: Action): DiagramState {
if (a12DiagramActions.nodeAdded.match(action)) {
const additionalNode = createNode();
5

-- 5 of 8 --

const updatedState = defaultDiagramReducer(state, action); // let the default
reducer process the action first
// add an additional node every time a node is added
return {
...updatedState,
diagram: {
...updatedState.diagram,
nodes: {
...updatedState.diagram.nodes,
[additionalNode.id]: additionalNode
}
};
}
return defaultDiagramReducer(state, action);
}
const diagramReducer = createDiagramReducer({ customReducer: myCustomReducer });
Adding Custom Elements
To add custom elements to the diagram, you need to use the customType property in the Diagram
Data Structure and register a React component in the A12DiagramWidget.
The following example showcases how to register a custom node. The same pattern can be used for
edges, ports and dialogs (explained in the next section).
interface MyNode extends DiagramNode {
customType: "myNode";
customProperty: any;
}
// Nodes are positioned automatically
function MyNodeWidget(props: NodeWidgetProps) {
const { node } = props
return <div style={{ backgroundColor: 'lightblue', height: node.height, width:
node.width }} />;
}
const myNode: MyNode = { id: "1", type: "node", customType: "myNode", customProperty:
"someValue", ...restNode };
const customDiagramData: Diagram = { nodes: { [myNode.id]: myNode }, edges: {} };
function selectDiagramState() {
return createDiagramState(uiState, customDiagramData)
}
function CustomDiagram() {
return <A12DiagramWidget
6

-- 6 of 8 --

activityId={props.activityId}
selectDiagramState={selectDiagramState}
nodeWidgetMap={{ myNode: MyNodeWidget }}
// below are not implemented in this example
edgeWidgetMap={{ myEdge: MyEdgeWidget }}
portWidgetMap={{ myPort: MyPortWidget }}
dialogWidgetMap={{ myDialog: MyDialogWidget }}
/>;
}
Disabling events / Showing dialogs
Per default the user is allowed to interact with the diagram without restrictions. The enablements
customization point of the diagramReducer allows disabling specific events or requesting dialogs
that the user needs to confirm. Dialogs are not restricted to confirmation alone. You could also
implement e.g. a form, to request additional data, before an edge is connected to a node. The
default dialog that we offer is a confirmation dialog that always contains a cancel and confirm
button, which can be modified. For everything else you need to add a custom dialog.
const customEnablements: DiagramEnablementMap = {
canMoveNode: (node, vector, state) => {
if (node.id === "x") {
return false; // Prevent moving this specific node
} else {
return true;
}
},
canRemoveElements: (elements, state) => {
// return a dialog object to let the user confirm the removal
return {
dialog: {
title: "Remove Elements",
message: "Are you sure you want to remove the selected elements?",
type: "dialog",
severity: "warning",
confirmButton: { label: "Remove", primary: true, destructive: true },
cancelButton: { label: "Cancel", primary: false, destructive: false }
}
}
}
const diagramReducer = createDiagramReducer({ enablements: customEnablements });
Hiding continuous actions from Redux Devtools
Since we have implemented everything with redux actions, the devtools will be crowded by
dragging actions (e.g. panning the canvas, moving nodes). We have collected these actions in
7

-- 7 of 8 --

diagramBlacklistedActions, which can be hidden by the devtools.
import { devToolsEnhancer } from "@redux-devtools/extension";
import { diagramBlacklistedActions } from
"@com.mgmtp.a12.diagrameditor/diagrameditor/dist/reduxDevTools/blacklistedActions";
return ApplicationFactories.createApplicationSetup({
...setup,
composeEnhancer(enhancers) {
return compose(enhancers, devToolsEnhancer({ actionsDenylist:
diagramBlacklistedActions }));
}
});
E2E Testing with Cypress
describe('My Test Suite', () => {
it('My Test', () => {
// How to pan the canvas
cy.get("[data-type='canvas']")
.trigger('mousedown', { button: 2, clientX: 100, clientY: 100 })
.trigger('mousemove', { clientX: 200, clientY: 200 })
.trigger('mouseup', { button: 2 });
// How to move a node
cy.root().find(`#nodeId [data-type='node']`)
.trigger("mousedown", { button: 0 })
.trigger("mousemove", { clientX: 0, clientY: 0 }) // a first initializing
mouse move is necessary
.trigger("mousemove", { clientX: 1, clientY: 1 }) // node is moved by 1
pixel in x and y direction
.trigger("mouseup");
// How to create an edge between two ports
cy.root().find(`#node1 [data-type='node']`).trigger("mouseover"); // hover over
node to make ports visible
cy.root().find(`#port1 [data-type='port']`).trigger("mousedown", { button: 0 })
.trigger("mousemove", { clientX: 0, clientY: 0 })
cy.root().find(`#node2 [data-type='node']`).trigger("mouseover");
cy.root().find(`#port2 [data-type='port']`).trigger("mouseup", { button: 0 });
});
});
8

-- 8 of 8 --

