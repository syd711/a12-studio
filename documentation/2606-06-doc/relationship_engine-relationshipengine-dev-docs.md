# relationship_engine relationshipengine dev docs

Relationship Engine & CDM
Introduction
The Relationship Engine package provides an extension that allows users to manage relationships
between Document Models in the Client.
A general documentation describing the basic concepts of the Relationship feature can be found in
the mgm A12 overall documentation.
It also provides the Simple Composed Data Model (SCDM) extension which is currently an
experimental feature that builds on the relationships extension and the new documentGraph
extension and provides an optimized UI for editing sets of documents in the context of a certain use
case of the target application.
NOTE
The new architecture is available as the recommended approach and is the
successor to the legacy setup. It is documented in the New Architecture section at
the end of this guide. New applications should use it; existing applications can
continue with the legacy architecture and migrate when ready — see the migration
tool described in that section.
Getting Started
Installation
Relationship Engine & CDM is provided as an ESM npm package. Run the following command to
install:
npm install @com.mgmtp.a12.relationshipengine/relationshipengine-core
Setup
Relationship Engine
Using basic relationships requires adapting your client application setup in the following way,
adding the relationship data provider, sagas and reducers:
import { RelationshipReducers, RelationshipFactories } from
"@com.mgmtp.a12.relationshipengine/relationshipengine-core";
import {
type DataHandler,
ApplicationFactories,
type ApplicationSetup,
1

-- 1 of 43 --

type ApplicationModel
} from "@com.mgmtp.a12.client/client-core";
export function setup(model: ApplicationModel): ApplicationSetup {
const dataHandlers: DataHandler[] = [
RelationshipFactories.createRelationshipDataProvider()
/* ...others */
];
return ApplicationFactories.createApplicationSetup({
// ...other props
model,
modelLoader: createPlatformServerModelLoader(),
dataHandlers,
customSagas: [...RelationshipFactories.createSagas({ dataHandlers }) /*
...others */],
dataReducers: [
...RelationshipReducers.dataReducers
// ...others
]
});
}
To easily enable displaying and editing relationships in the application ui, we recommend using the
CRUDViews.FormEngineView which supports the integration of the Relationship Engine by default.
Your viewProvider could look like the following, providing the CRUD views:
src/main/viewFactory.tsx
import React from "react";
import { CRUDViews } from "@com.mgmtp.a12.crud/crud-core";
import { type View, FrameFactories } from "@com.mgmtp.a12.client/client-core";
export function viewProvider(componentName: string): React.ComponentType<View> {
return FrameFactories.viewProvider(componentName) ||
crudViewProvider(componentName) || Placeholder;
}
const crudViewProvider = createCRUDViewProvider();
function createCRUDViewProvider(): (componentName: string) =>
React.ComponentType<View> | undefined {
const components: { [name: string]: React.ComponentType<View> | undefined } = {
FormCRUD: CRUDViews.FormEngineView,
OverviewCRUD: CRUDViews.OverviewEngineView
};
return function provider(name) {
return components[name];
2

-- 2 of 43 --

};
}
function Placeholder(): React.ReactNode {
return <div>ERROR: NO COMPONENT FOUND</div>;
}
SCDM
SCDM requires some global application setup configuration for the following properties:
• Middlewares / Sagas
• Reducers
• DataProvider
Please see the following code for an example:
import {
cdmSagas,
cddReducers,
dgReducerFactory,
createCdmMiddlewares,
createCddDataProvider,
cddDataHolderReducerExtension
} from "@com.mgmtp.a12.relationshipengine/relationshipengine-core";
export function setup(model: ApplicationModel): ApplicationSetup {
return ApplicationFactories.createApplicationSetup({
// ...other props
model,
modelLoader: createPlatformServerModelLoader(),
dataHandlers: [createCddDataProvider() /*others */],
additionalMiddlewares: [...createCdmMiddlewares() /* others*/],
customSagas: [...cdmSagas() /* others*/],
dataReducers: [
...dgReducerFactory(cddDataHolderReducerExtension),
...cddReducers
// other,
]
});
}
NOTE The scdm feature requires that the model graph is loaded during the initialization
of your client application. Please refer to the Client Documentation for more info.
NOTE
The CDM middlewares replace the ones provided by the Form Engine extension
with createFormEngineMiddlewares(). Make sure that you either include one or the
3

-- 3 of 43 --

other, but not both!
The same is true for the cdmSagas, which already incorporate the functionality of the
formEngineSagas from the form engine package. So again, only include one of them!
Features
Relationship Engine
The Relationship Engine visualizes links and candidates of a relationship according to the
configuration of the Relationship UI configuration.
Relationship UI Configuration
The Relationship UI configuration is the melting pot of all models and UI components used to
display a relationship. A typical model looks like the following:
{
"name": "ProductBrand-UiConfig-1",
"metaInformation": {
"version": "1.0.0"
},
"relationshipName": "ProductBrand",
"targetRole": "Brand",
"components": [
{
"id": "1",
"name": "DropDownSelection",
"models": [
{
"modelType": "overview",
"name": "ProductBrand-Brand-LinkOverview",
"use": "link"
},
{
"modelType": "overview",
"name": "BrandLink-overview",
"use": "candidate"
},
{
"modelType": "form",
"name": "ProductBrand-LinkForm",
"use": "link"
}
]
}
]
4

-- 4 of 43 --

}
It consists of..
• a unique name
• the name of the referenced Relationship model
• the side of the relationship which shall be displayed (called "target role")
• a list of component configurations used to visualize the relationship
Each component configuration contains..
• a unique component ID
• the ID of the View which shall be rendered (see Views)
• a list of models which are required to render the view
• additional properties which may be required by individual views
In general only the first component of the list is rendered by the engine, but individual views allow
a reference to another component (see Table List as an example).
Views
A relationship can be displayed in several variants. Four views are provided by default, but projects
can define their own UI.
Every default view requires an Overview Model for links and an Overview Model for candidates
since the related documents are based on different result Document Models.
Drop Down Selection
A drop down selection allows the selection of a single link. Candidates are displayed in a drop down
list, the selected link is shown in the input field. By default, only the first 10 relevant candidates are
shown, the remaining candidates will be displayed by clicking the "See More" button.
NOTE
• If no default sorting is specified, the candidate list is sorted by __meta/createdAt
in descending order.
• Case is ignored during sorting (ignoreCase is set to true).
To provide a better modeling experience we have decided to reuse Overview Models for this
component. The field value of the first overview column is used to display the link / candidate
document.
5

-- 5 of 43 --

Use "name": "DropDownSelection" in the UI configuration to present your relationship like this. The
view requires the following models:
model
Type
use Description
"overvi
ew"
"link" Overview Model to display link documents (the field value of the first overview
column is used)
"overvi
ew"
"candi
date"
Overview Model to display candidate documents (the field value of the first
overview column is used)
"form" "link" Form Model describing the form used to modify the link document.
The form is shown when adding new links or by pressing the edit button. If no
link Document Model is specified for the relationship, this Form Model is not
required. Please bind this view to a form control when using the Form Engine
integration.
Dual Pane Selection
With the dual pane selection users can manage multiple links for a document. All candidates are
shown on the left, links are shown on the right side. Added links are highlighted green, removed
links red. As soon as the changes are saved, the highlighting will be cleared.
The list of candidates can be filtered, sorted and paginated while the link table can only be
paginated. The sorting behavior for the candidate list is similar to the Drop Down Selection.
Furthermore, it’s not possible to add custom row actions to the overview tables and the content box
cannot be customized using the Overview Model.
Use "name": "DualPaneSelection" in the UI configuration to present your relationship like this. The
6

-- 6 of 43 --

view requires the following models:
model
Type:
use: Description
"overvi
ew"
"link" Overview Model to display link documents
"overvi
ew"
"candi
date"
Overview Model to display candidate documents
"form" "link" Form Model describing the form used to modify the link document.
The form is shown when adding new links or by pressing the edit button. If no
link Document Model is specified for the relationship, this Form Model is not
required. Please bind this view to a form custom screen element when using the
Form Engine integration.
Table List
Some use cases may focus on a plain table listing the links without modifying them directly. The
table list can be used for these scenarios.
Use "name": "TableList" in the UI configuration to present your relationship like this. The view
requires the following models:
model
Type
use Description
"overvi
ew"
"link" Overview Model to display link documents
"overvi
ew"
"candi
date"
Overview Model to display candidate documents
For the table list an additional property "editComponent" can be specified in the "props" section of
the component configuration. If another component configuration in the Relationship UI
configuration exists with the ID given in the properties value, an edit button is displayed. When the
button is clicked, the referenced edit component is displayed.
As an example, the following Relationship UI configuration …
{
"name": "ProductBrand-UiConfig-1",
"metaInformation": {
"version": "1.0.0"
},
"relationshipName": "ProductBrand",
"targetRole": "Product",
"components": [
{
"id": "1",
7

-- 7 of 43 --

"name": "TableList",
"linkPageSize": 2,
"candidatePageSize": 3,
"models": [
{
"modelType": "overview",
"name": "ProductBrand-Product-LinkOverview",
"use": "link"
},
{
"modelType": "overview",
"name": "ProductLink-overview",
"use": "candidate"
}
],
"props": {
"editComponent": "2"
}
},
{
"id": "2",
"name": "DualPaneSelection",
"models": [
{
"modelType": "overview",
"name": "ProductBrand-Product-LinkOverview",
"use": "link"
},
{
"modelType": "overview",
"name": "ProductLink-overview",
"use": "candidate"
},
{
"modelType": "form",
"name": "ProductBrand-LinkForm",
"use": "link"
}
],
"candidatePageSize": 3
}
]
}
… will show a table list with edit button which will open a dual pane selection ..
8

-- 8 of 43 --

Sorting and Filtering the listed links is not supported in the default table list.
Please bind this view to a form custom screen element when using the Form Engine integration.
NOTE
The relationship feature currently does not support multiple different usages of the
same Relationship Model. For example you can not have multiple bindings for the
same Relationship Model and use different Overview Models (with different
settings). There is always only one configuration used for querying the candidates
and the links respectively. This means that if you use two relationship components
together (like shown above), they will always use the paging, sorting & filtering
settings of the first component. If an optional setting is left empty in the first
component, its default value will be used.
Custom UI (Experimental)
In addition to the default views mentioned above, projects can provide their own UI. To do so, they
have to specify an own component provider as Relationship Engine property. The provider receives
a component configuration and returns a React component providing one of the following
9

-- 9 of 43 --

interfaces:
• ListProps - Provides properties required to show links in a readonly list. Views should use this
interface to provide a UI similar to the table list.
• SingleSelectionProps - Provides properties required to render a UI for the selection of a single
link. Views should use this interface to provide a UI similar to the drop down selection.
• MultiSelectionProps - Provides properties required to render a UI for the selection of multiple
links at the same time. Views should use this interface to provide a UI similar to dual pane
selection.
We consider customization of the components to be an experimental feature at the moment.
Custom Localization
All labels rendered by components of the relationship extension can be localized.
The extension comes with a localizer service, that can be created via a factory. It has to be
configured among the application’s localizer services to localize all multilingual texts defined in the
Relationship Model and UI configuration.
If a particular label shall be localized independently of the model definition, the key can be
overwritten by defining an own localizer service. The following keys are available:
• relationship.ui-configuration.<name of the ui configuration>.<id of the custom
component>.dual-pane.available-items.title - Title of the candidate table in Dual Pane
• relationship.ui-configuration.<name of the ui configuration>.<id of the custom
component>.selected-items.title - Title of the link table in Dual Pane
• relationship.relationshipModel.<name of the relationship>.<name of the role>.labels - The
display label of a relationship role
The localization keys of static texts, which are not defined by models, are provided by the constant
RELATIONSHIP_RESOURCE_KEYS which is located in extensions/relationship. This constant provides the
keys as well as the documentation of their usage.
Link Form
A link between entities can have its own properties, e.g. the amount of a product in a bundle or the
production site of a product for a brand.
Those properties need to be defined in a link Document Model and the respective UI requires a link
Form Model.
If the link Form Model is provided in the Relationship UI configuration, then on creating a new link,
a Form Engine will be shown in a modal, where the user can enter values for the link properties.
10

-- 10 of 43 --

The shown Form Engine has some limitations, as it cannot be customized via properties and engine
options.
Form Engine Integration
In most cases the Relationship Engine shall be integrated into a form. To do so, a custom Form
Model map (RelationshipFormModelMap) has to be used by the Form Engine view.
If the map finds a custom screen element having a matching binding configuration of type
"relationship", a relationship UI configuration is expected as configuration. This model is used to
render the Relationship Engine. The CRUD Form Engine view supports this integration by default.
Please note that properties of the bound custom screen element / custom cell defined in the Form
Model (e.g. a custom screen element title) are not considered by the Relationship Engine. If you
intend to use the custom screen element title to structure your screen, please wrap the bound
element with an additional section having the desired title.
Custom Form Engine
If you want to customize your Form Engine view (e.g. via the FormModelMap), you will have to
integrate the Relationship Engine yourself as the CRUD extension does not support customization.
You can achieve this by passing the RelationshipFormModelMap to the Form Engine component.
Take a look at the following code for an example:
Relationship Engine integration
import React from "react";
import { FormEngineViews, DefaultFormModelMap } from
"@com.mgmtp.a12.formengine/formengine-core";
import { RelationshipFormModelMap } from
"@com.mgmtp.a12.relationshipengine/relationshipengine-core";
type RelationshipFormEngineViewProps = Omit<FormEngineViews.FormEngineProps,
"widgetMap" | "formModelMap">;
export function RelationshipFormEngineView(props: RelationshipFormEngineViewProps):
React.ReactNode {
11

-- 11 of 43 --

return (
<FormEngineViews.FormEngine
{...props}
formModelMap={{
...DefaultFormModelMap,
...RelationshipFormModelMap
// add your own customization here
}}
/>
);
}
When adding your own customization to the FormModelMap, be sure to return the correct default
component as shown in the following example:
Customized FormModelMap
import React from "react";
import { RelationshipFormModelMap } from
"@com.mgmtp.a12.relationshipengine/relationshipengine-core";
import { type FormModel, type FormModelMap, DefaultFormModelMap } from
"@com.mgmtp.a12.formengine/formengine-core";
export const CustomFormModelMap: FormModelMap = {
...DefaultFormModelMap,
...RelationshipFormModelMap,
Screen: { component: CustomScreen },
CustomScreenElement: { component: CustomScreenElement }
};
function CustomScreen(props: FormModelMap.FormModelComponentProps<FormModel.Screen>):
React.ReactNode {
// render custom screen conditionally
const condition = props.modelElement.id === "customScreen";
if (condition) {
return <div>Custom screen</div>;
}
// fallback to the Screen from the DefaultFormModelMap
return <DefaultFormModelMap.Screen.component {...props} />;
}
function CustomScreenElement(
props: FormModelMap.FormModelComponentProps<FormModel.CustomScreenElement>
): React.ReactNode {
// render custom screen element conditionally
const condition = props.modelElement.id === "customScreenElement";
12

-- 12 of 43 --

if (condition) {
return <div>Custom screen element</div>;
}
// The RelationshipFormModelMap already provides a custom screen element,
// so it has to be returned (instead of the one from the DefaultFormModelMap)
return <RelationshipFormModelMap.CustomScreenElement.component {...props} />;
}
Model Overview
When modeling relationships a lot of models are involved. This section should provide a short
overview to put you back on track. As example we focus on the Relationship between products and
brands. In a brand form assigned products shall be managed via a dual pane selection.
Before the introduction of the Relationship feature it was possible to define:
• A Document Model "Product" describing the properties of a product
• A Form Model "Product Form" showing a form to enter the product properties
• An Overview Model "Product Overview" showing products in a table
The same applies to brands.
It was not possible to model a connection between these entities.
From now on we can use a Relationship Model "ProductBrand" to define this connection. In our
example the model specifies that multiple products are assigned to a brand and only one brand can
be assigned to a product. If additional fields shall be defined for this Relationship, another
Document Model "ProductBrand" can be referenced. It is called "Link Document Model" in this
context.
13

-- 13 of 43 --

The dual pane selection requires two Overview Models - one to define the candidate table (based on
the "Product" Document Model) and one to define the link table. Link result documents are a
combination of fields defined in the "Product" Document Model and fields defined in the Link
Document Model. To reference this combination in the Overview Model another Document Model
is required: The Link Result Document Model. It can be generated from the platform server.
If a Link Document Model is referenced in the Relationship Model, an additional Form Model is
required for a form to modify the link document. It is called "ProductBrand Form" in the overview
above.
The Relationship UI configuration references the view, the used overview / Form Models and the
Relationship Model to show. To integrate the relationship directly in the form, the Relationship UI
configuration is defined as a binding configuration in the "Brand Form" Form Model.
Fields projection
Fields projection are partially supported by the relationship bindings, including the loading of
candidates and links table. However, the link document fields shall not be projected and the
binding will request a full document in all cases. Customization of projected fields can be done via
the Server Connector middlewares and should be done in a cautious approach to avoid removing
the mandatory fields. Please consult Data Services documentation for more details.
Simple Composed Data Model
WARNING Customizing of Form Model elements (by way of the FormModelMap) does not
work in combination with CDM. Trying to do so might result in errors.
Preparation
The modeling of SCDM is based on
• Multiple Document Models: Used for the individual entities (domain models) and the aggregate
(CDM)
• A single Form Model: Used to model the form UI
• Multiple Bindings: Configuration of the relationship UI components and placement inside the
form
• Multiple Overview Models: Configuration of the links and candidate columns
All models are regular A12 models. The CDM and the Form Model use annotations and existing
features in a slightly extended way to allow the modeling of a CDM (form).
Please refer to the Business Analyst documentation for a detailed description on how to create
CDMs.
View component
The SCDM connected view via Form Engine is offered by default via the CRUDViews.FormEngineView
14

-- 14 of 43 --

component. However, in case CRUD is not used, a manual registration of FormModelMap and
cddAdapter should be taken care.
Registration
export function FormEngineWithRelationship(props: View): React.JSX.Element {
const stateProps = useSelector((state: object) => {
const activity = ActivitySelectors.activityById(props.activityId)(state);
if (!activity) {
return;
}
const adaptedState = cddActivityStateAdapter(activity)(state);
return FormEngineStateAdapter.mapStateToProps(adaptedState, { ...props,
formModelMap: CustomFormModelMap });
});
const dispatch = useDispatch();
const dispatchProps = FormEngineActions.mapDispatchToProps(dispatch, props);
return <FormEngineViews.FormEngineTpl {...props} {...stateProps}
{...dispatchProps} />;
}
const CustomFormModelMap: FormModelMap = {
...DefaultFormModelMap,
...RelationshipFormModelMap
};
View Customization
The SCDM exposes a componentProvider API as a way to customizing its components, e.g.:
DualPaneSelection, TableList. In order to make a customized component, instead of using
RelationshipFormModelMap, the createRelationshipFormModelMap shall be called as follows
const CustomFormModelMap: FormModelMap = {
...DefaultFormModelMap,
...createRelationshipFormModelMap({
componentProvider: config => {
if (config.name === "DualPaneSelection") {
return { type: "MultiSelection", component: CustomDualPane };
} else if (config.name === "TableList") {
return { type: "List", component: CustomTableList };
}
return undefined;
}
})
15

-- 15 of 43 --

};
function CustomDualPane(props: RelationshipViews.MultiSelectionProps) {
const assignments = React.useMemo(() => {
if (props.assignments.loadingState !== "loaded") {
return props.assignments;
}
return { ...props.assignments, data: [...props.assignments.data].reverse() };
}, [props.assignments]);
return <DualPaneSelection {...props} assignments={assignments} />;
}
function CustomTableList(props: RelationshipViews.ListProps) {
const items = React.useMemo(() => {
if (!props.items || props.items.loadingState !== "loaded") {
return props.items;
}
return { ...props.items, data: [...props.items.data].reverse() };
}, [props.items]);
return <TableList {...props} items={items} />;
}
WARNING
SCDM APIs are under experimental stage and subject to be changed even in a
minor release. Therefore, customizations to the view components should be
done in a cautious approach, e.g.: in the CustomDualPane example, a change to
ordering of the assignments array are safe, but removing/adding an item
from/to assignments are not recommended and should always be avoided.
Fields projection
Fields projection are not supported by CDM bindings. However, the candidates table which is re-
used from the normal binding does have support for the fields projection hence can be customized.
Relationship Engine & CDM Actions
For the current stage, Relationship Engine & CDM actions are internal, it is not encouraged for
developers have access to these actions.
Custom RequestSelectorMap
NOTE This API is marked as experimental. Breaking changes might happen even in minor
releases.
The RequestSelectorMap can be used to customize how the Relationship Engine & CDM produces
Data Services Query API requests. A DefaultRequestSelectorMap is shipped, but it is possible to inject
a custom map to tweak queries and mutations without involving internal logic.
16

-- 16 of 43 --

What it controls
The map exposes selector factories that return JSON-RPC request objects. Relationship & CDM use
these exclusively for server calls:
• Queries
◦ loadCandidates – load candidate documents for a relationship
◦ loadLinks – load link documents for a relationship
◦ loadDocumentGraph – load the document graph for the CDM
• Mutations
◦ addDocument, modifyDocument, deleteDocument – document CRUD
◦ addLink, modifyLink, deleteLink – link CRUD
All mutation builders return selectors, so it is possible to access app state within each selector (e.g.:
the locale can be derived via `LocaleSelectors.locale()(state)).
Injecting a custom map
You can inject a custom implementation into both providers:
// Provide the custom map to the Relationship data provider
RelationshipFactories.createRelationshipDataProvider({ requestSelectorMap:
CustomRequestSelectorMap });
// Provide the same custom map to the CDM data provider
createCddDataProvider({ requestSelectorMap: CustomRequestSelectorMap });
Example: add a default sort for queries
The following example adds a default sort to loadCandidates/loadLinks when none is provided,
while delegating all other behavior to the default implementation.
import { Query } from "@com.mgmtp.a12.dataservices/dataservices-access";
import {
createCddDataProvider,
RelationshipFactories,
type RequestSelectorMap,
DefaultRequestSelectorMap
} from "@com.mgmtp.a12.relationshipengine/relationshipengine-core";
/**
* Extend the default RequestSelectorMap with small tweaks.
*
* - Add a default sort to loadCandidates/loadLinks when none is provided.
* - Keep all other behaviors (including locale resolution for document mutations)
17

-- 17 of 43 --

intact by
* spreading the DefaultRequestSelectorMap.
*/
export const CustomRequestSelectorMap: RequestSelectorMap = {
...DefaultRequestSelectorMap,
loadCandidates: (config) => {
const customSort: Query.Order[] = config.sort?.length
? config.sort
: [
{
field: "/product/sku",
direction: Query.Direction.DESC,
ignoreCase: false,
nullHandling: Query.NullHandling.NULLS_LAST
}
];
return DefaultRequestSelectorMap.loadCandidates({ ...config, sort: customSort
});
},
loadLinks: (config) => {
let constraint = config.constraint;
if (config.targetDocumentModel === "Contract-document") {
const mustBeActive: Query.ExactMatchOperator = {
operator: Query.OPERATORS.EXACT_MATCH_OPERATOR,
field: "/contract/status",
value: "ACTIVE"
};
constraint = constraint
? { operator: Query.OPERATORS.AND_OPERATOR, operands: [constraint,
mustBeActive] }
: mustBeActive;
}
return DefaultRequestSelectorMap.loadLinks({ ...config, constraint });
}
};
TIP Always spread DefaultRequestSelectorMap when customizing.
Request Filters
This customization approach can also be used in combination with the RequestFilter API (described
in the Data Services documentation), for example to use your own operation methods. Customizing
the RequestFilter alone would not be enough when the method replacement needs some context
(e.g. only overriding methods in certain conditions). Using the RequestSelectorMap could then be
used to provide this context down for the filter to use.
18

-- 18 of 43 --

New Architecture
NOTE
This section documents the new composable Relationship Engine architecture
(combineFeatures(withFormEngine, withOverviewEngine, withRelationshipEngine),
standalone Relationship UI Models, changelog-based state management).
The new architecture is the recommended approach for new applications and the
successor to the legacy architecture documented above. Existing applications can
migrate at their own pace using the migration tooling described in the Migration
chapter at the end of this section.
Getting Started
The minimum integration is three steps: combine the engine composables when constructing the
application, register a view that knows how to render a form together with its Relationship UI
Models, and configure attachment support for Composed Document Model activities.
Combine the composables
withRelationshipEngine is added to the application setup alongside Form Engine, Overview Engine
and the application’s other features. The composable’s type signature requires Form Engine and
Overview Engine to be configured first, and rejects double application.
import { combineFeatures, createA12ApplicationSetup, type A12ApplicationConfig } from
"@com.mgmtp.a12.client/client-core";
import { withFormEngine } from "@com.mgmtp.a12.formengine/formengine-core";
import { withOverviewEngine } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
import { withRelationshipEngine, RelationshipEngineFactories } from
"@com.mgmtp.a12.relationshipengine/relationshipengine-core";
import { withCRUD } from "@com.mgmtp.a12.crud/crud-core";
const initialConfig: A12ApplicationConfig = {
formEngine: { sagas: RelationshipEngineFactories.createFormEngineSagaOptions() }
};
const { store, Component } = createA12ApplicationSetup(
combineFeatures(
withFormEngine,
withOverviewEngine,
withRelationshipEngine,
withCRUD
)(initialConfig)
);
NOTE The formEngine.sagas configuration is only required for attachment support on
Composed Document Model (CDM) activities. Without it, attachment upload,
19

-- 19 of 43 --

download, and delete operations might fail at runtime.
RelationshipEngineFactories.createFormEngineSagaOptions() pre-wires a
documentDescriptorSelector that resolves document descriptors from the RE
document graph.
Render forms with Relationship UI Models
The recommended path is to also include withCRUD and to register the CRUD-provided form view,
which already integrates the Relationship Engine view layer:
import { CRUDViews } from "@com.mgmtp.a12.crud/crud-core";
addView("MyCustomRelationshipFormEngine",
CRUDViews.FormEngineWithRelationshipEngineView);
Without withCRUD
Applications that integrate the Form Engine view directly, without withCRUD, must extend their
FormEngineView with the form-model map produced by
RelationshipEngineFactories.createFormModelMap. This is what teaches the Form Engine view how to
resolve Relationship UI Models to their renderers.
import { FormEngineViews } from "@com.mgmtp.a12.formengine/formengine-core";
import { DefaultFormModelMap } from "@com.mgmtp.a12.formengine/formengine-core";
import { RelationshipEngineFactories } from
"@com.mgmtp.a12.relationshipengine/relationshipengine-core";
const formModelMap = {
...DefaultFormModelMap,
...RelationshipEngineFactories.createFormModelMap()
};
function MyFormEngineView(props: View) {
return <FormEngineViews.FormEngine {...props} formModelMap={formModelMap} />;
}
addView("MyFormEngine", MyFormEngineView);
The remainder of the integration, including how the Relationship Engine relates to Form Engine
and Overview Engine and how data is loaded for a Relationship UI Model, is described in
Composable Setup.
Architecture Overview
20

-- 20 of 43 --

Summary
The Relationship Engine is structured around four ideas. None of them are large in isolation; the
architecture is the way they fit together.
Composable feature
The Relationship Engine is registered alongside Form Engine and Overview Engine through a
dedicated A12 composable. It never mutates Form Engine or Overview Engine state. It observes
their actions, keeps its own state, and delegates form rendering to Form Engine and table
rendering to Overview Engine. The setup is type-safe: the composable will not type-check unless
Form Engine and Overview Engine are configured first, and it cannot be applied twice.
Changelog as the single source of truth for changes
Every user-visible change, whether adding or removing a link, editing a link document, drafting
a new child document, or editing a field on an existing Composed Document Model document, is
recorded as an entry in an append-only changelog. The changelog is held in a dedicated data
holder per activity. Selectors derive everything else (effective changes for save, drafting rows,
lifecycle badges, link indicator counts) from this single log.
Document Graph for Composed Document Model
Composed Document Model activities additionally maintain an immutable Document Graph: the
tree of documents and links rooted at the Composed Document Model root. Each changelog
entry is folded into a fresh Document Graph instance. Activities outside Composed Document
Model do not have a Document Graph; their Relationship UI Models work directly against
Overview Engine data holders, with the changelog tracking only link additions and deletions.
Standalone Relationship UI Model, not annotation
Relationship configuration lives in standalone A12 models, namely a Relationship UI Model per
relationship UI (Dual Pane, Table List, or Dropdown), a Query Model per data fetch and (for
pane-based Relationship UI Models) an Overview Model per pane. Components, queries and
panes reference these models by name. The previous approach of stringified JSON inside Form
Engine annotations is no longer used.
Layers
The runtime decomposes into three layers:
1. Application setup, which registers the Relationship Engine artefacts next to those of Form
Engine and Overview Engine. Each registration is additive.
2. Redux store, which holds the changelog, the optional Document Graph, the selected items and
available items data holders (which are Overview Engine compatible), and the dropdown
selection state. Selectors expose a stable read interface to the view layer.
3. View layer, a context-driven ComponentMap and WidgetMap. Default implementations of DualPane,
TableList, DropDownSelection, the Overview Engine wrapper and supporting widgets ship with
the package and can be replaced individually.
The flow of a change through these layers is consistent: a UI event is observed by a middleware, the
middleware (or a saga it kicks off) appends a changelog entry, the entry is folded into the Document
21

-- 21 of 43 --

Graph in Composed Document Model activities, and selectors recompute so React re-renders only
the affected components. On save, a save handler reads the deduplicated effective changes from the
changelog and routes them either as a merge into a parent activity’s changelog or as a persist call to
DataServices.
Changelog based state management
Figure 1. Architecture comparison: legacy dual-path implementation versus the new unified changelog
pipeline. The legacy side is shown for reference; the new architecture collapses both CDM and non-CDM
paths into a single Changelog and DocumentGraph.
The same machinery serves both Composed Document Model and non-Composed Document Model
activities. The differences are limited to:
• whether a Document Graph data holder is present,
• which changelog entry kinds can occur (activities outside Composed Document Model only ever
produce link-add and link-delete entries; Composed Document Model activities additionally
produce document-add, document-change, and the cached transient entries used by SCDM and
sub-document-graph loading),
• whether a child activity’s changelog is seeded from a parent activity (a Composed Document
Model feature for editing a drafting sub CDM tree).
Application setup, view customisation and the modelling story are the same in both modes.
A change worth noting against the current architecture: in the new architecture a changelog is
always present, regardless of whether the activity is Composed Document Model or not. Every
Relationship Engine feature, including the link lifecycle indicators, the drafting rows in the selected
items pane, the Parent Link Descriptor protocol and the save routing, derives from this single
22

-- 22 of 43 --

structure.
Reading order
The remaining chapters take this overview from setup to extension:
• Composable Setup, how the Relationship Engine is wired into an A12 application alongside
Form Engine and Overview Engine.
• Modeling, the Relationship UI Model, Query Model and Overview Model, and how they describe
a relationship UI end-to-end.
• State Management, what the changelog, Document Graph, Overview Engine, Dropdown data
holders contribute, plus the Parent Link Descriptor protocol for parents that are not
Relationship Engine activities.
• View Customization, the ComponentMap, WidgetMap, and the supported event hooks.
• Migration, the migration tool for converting existing bindings to Relationship UI Models.
Composable Setup
This chapter describes how the Relationship Engine plugs into an A12 client application, what it
expects from Form Engine and Overview Engine, and what it contributes to data loading.
The composable
withRelationshipEngine is the public entry point. It is a standard A12 feature composable and is
meant to be combined with the other engine composables:
• withFormEngine, which must be applied before withRelationshipEngine.
• withOverviewEngine, which must be applied before withRelationshipEngine.
• withRelationshipEngine, applied once. The composable’s type signature rejects configurations
where Form Engine or Overview Engine is missing, and rejects double application.
Beyond ordering and uniqueness, no special configuration object is required for the Relationship
Engine itself: the engine reads everything it needs from the application’s models, from the
descriptors of the activities the application opens, and from the data services configuration
provided by withDataServicesConfiguration.
NOTE
When the application uses Composed Document Model (CDM) activities with
attachment support, initialConfig.formEngine.sagas must be configured with
RelationshipEngineFactories.createFormEngineSagaOptions() before the composable
chain runs. See Getting Started for the full setup.
Relationship with Form Engine
The Relationship Engine delegates anything form-shaped to Form Engine and observes Form
Engine actions to keep its own state in sync.
23

-- 23 of 43 --

• Fields are controlled by Form Engine form components. A dedicated data provider produces
Form Engine compatible data holders so this is a regular Form Engine activity scope from Form
Engine’s point of view.
• Field changes inside a form engine are emitted by Form Engine as field-value changes; a
Relationship Engine middleware observes them and appends a docChanged entry to the
changelog. Form Engine continues processing normally; the Relationship Engine neither blocks
nor rewrites the action.
• CustomScreenElement referencing to a Relationship UI Models will be rendered by
RelationshipEngine. Applications that integrates the Form Engine view directly, without
withCRUD, must pass this custom FormModelMap to their FormEngineView (see Getting Started).
Relationship with Overview Engine
The Relationship Engine uses Overview Engine as the rendering delegate for the selected items
pane (in DualPane and TableList) and for the available items pane (in DualPane).
• Selected items and available items data holders are Overview Engine data holders with
Relationship Engine specific metadata attached. Overview Engine renders rows from them; the
Relationship Engine owns their loading and lifecycle.
• Row clicks inside the selected items pane are observed by Relationship Engine middlewares,
which decide whether to open a child activity (in Composed Document Model), open the link
document editor, or emit a signal for the host application.
• Pending but unsaved targets are reflected in the Overview Engine query: in the standard
"selecting" mode the Relationship Engine extends the result set so the new target appears in the
next reload. In the alternative "drafting" modes the query is left untouched and a synthetic row
is prepended to the Overview Engine result instead. See State Management.
Data loading
The data provider loads in three phases on the standard activity init path:
1. Document Graph (Composed Document Model only). The Composed Document Model tree is
fetched from DataServices and populates the Document Graph data holder. Relationship UI
Models are identified from the form model.
2. Source-entity resolution. For each Relationship UI Model, the Relationship Engine walks the
Composed Document Model relationship chain to determine which document acts as the source
for that model’s queries.
3. Overview pane and dropdown data. The data holders for the selected items and available
items panes are loaded; dropdown candidate sets are loaded via their Query Models in a single
RPC request.
A separate phase fetches sub-document-graph slices when a link is added in Composed Document
Model mode and the Relationship UI Model declares a groupPath.
A separate phase fetches the overview engine data when user trigger a search, filter or pagination
request. Per default, Relationship Engine respects every loading behavior configured in the
24

-- 24 of 43 --

Overview Model.
A separate phase fetches the dropdown data when user opens the dropdown, triggers a search or
loads more request. By default, dropdown only load the selected item, available items are only
loaded on demand. This helps avoid loading large chunk of available items unnecessary during
initiation phase.
Modeling
The Relationship Engine is configured by A12 models, not by code or annotation strings. Each
relationship UI is described by a Relationship UI Model. Wherever a Relationship UI Model fetches
data, it does so through a Query Model. Pane-based Relationship UI Models additionally compose
with an Overview Model per pane. This chapter explains the responsibilities of each model and
how they react to one another.
Relationship UI Model
The Relationship UI Model defines a single relationship UI, that is one Dual Pane, one Table List, or
one Dropdown. It is a standard A12 base model, stored as its own model file under the application’s
model resources. Multiple relationship UIs on the same form are multiple UI models; they are no
longer collapsed into a single annotation on the Form Engine model.
A UI model captures:
Component shape
The kind of relationship UI (Dual Pane, Table List, Dropdown) and the configuration shared by
every variant of that kind, including labels, action buttons, the relationship name and roles it
represents, whether duplicates are allowed, and so on.
Model references
Named references to the dependent models (Query Models, Overview Models, link-document
Form Models). The Relationship UI Model itself does not embed these; it points at them by name
and version, the same way other A12 features reference base models. This is what makes
versioning, schema validation and tooling possible.
Behavior switches
Per-model choices that influence runtime behavior: whether a link document is required, which
roles are involved, whether a groupPath enables Composed Document Model sub-document-
graph loading, and similar.
Because the UI model is a regular A12 model, it is loaded with the model graph at application start,
validated against its schema, and resolved by name when an activity is opened.
Query Model
A Query Model is the single mechanism through which a Relationship UI Model fetches documents.
It encapsulates a query definition (constraint, projection, ordering, paging) and is referenced by
name from the Relationship UI Model.
25

-- 25 of 43 --

Each Relationship UI Model variant references Query Models in slightly different shapes:
Dropdown
References Query Models directly in the UI model: one for the candidate set (what the user can
pick) and one for the selected item (what is currently chosen). The dropdown does not go
through an Overview Model.
Dual Pane
References Query Models via the Overview Model of each pane. The selected items pane and
the available items pane each have their own Overview Model, and each Overview Model
carries the Query Model that drives its data.
Table List
References Query Models via the Overview Model of the list. If the Table List allows editing
through a dialog with selection, the dialog references additional Query Models the same way a
Dual Pane would.
Query variables placeholders
A query alone rarely makes sense in isolation: the candidates for "products in this category" depend
on which category the user is currently editing. Query Models therefore allow variables
placeholders inside their constraints, that the engine substitutes at runtime with the actual
document reference of the surrounding activity.
This indirection is deliberate. It keeps Query Models reusable across activities, keeps the constraint
declarative, and isolates the runtime substitution to a single layer of the engine.
Overview Model
For pane-based Relationship UI Models (Dual Pane, Table List), each pane is rendered by Overview
Engine. The Overview Model defines that pane’s table, including its columns, sorting and paging
defaults, and the Query Model it draws from, exactly as it would for a stand-alone Overview Engine
activity.
This is what allows the Relationship Engine to delegate rendering to Overview Engine without
forking it: a Relationship Engine pane is, structurally, an "embedded" Overview Engine view
component fed by an "embedded" Overview Engine data holder. Customizations applied to
Overview Engine columns and cell widgets propagate into the Relationship Engine without further
work; see View Customization.
Composed Document Model
Composed Document Model is an opt-in modelling layer for use cases where one root document
needs to be edited together with a tree of related child documents in a single activity. It does not
introduce new Relationship UI Models; instead, it requires the application’s data model to declare a
Composed Document Model, that is the shape of the tree, and it changes the activity setup so that:
• the activity loads the entire Composed Document Model tree at init time as a Document Graph,
• Relationship UI Models whose relationships are part of the tree resolve their source document
26

-- 26 of 43 --

by walking the relationship chain rather than from a single form document,
• drafting entries (e.g.: documents created locally before being persisted) become possible.
How the models react together
Figure 2. Modeling hierarchy: RelationshipUiModel is the root model. Its ComponentConfiguration branches
into DualPane/TableList bindings (via OverviewModels and QueryModels) and DropDown bindings (via
QueryModels directly). The optional linkFormModel activates a DynamicConfiguration for Form Engine
rendering.
A typical Dual Pane wires up like this at runtime:
1. The activity is opened. The model graph supplies the activity’s Form Engine model and every
Relationship UI Model referenced from it.
2. The Relationship Engine creates one set of data holders per Relationship UI Model (selected
27

-- 27 of 43 --

items instance, available items instance, optional dropdown selection) using the UI model’s
relationship metadata.
3. The available items pane’s Overview Model resolves its Query Model; query variables are
substituted from the activity descriptor or, for Composed Document Model activities, or from
the resolved source document on the Document Graph.
4. The selected items pane’s Overview Model is similarly resolved. The Relationship Engine
additionally extends the selected items pane query at runtime when there are pending but
unsaved targets, so the selected pane can accurately reflect the changelog together with the
persisted data.
5. User actions in either pane produce changelog entries; selectors recompute drafting rows,
lifecycle badges and effective changes for the next save.
The Dropdown variant skips steps that involve Overview Models, going directly from the UI Model
to the Query Model. The Table List variant uses one pane plus an optional edit dialog whose data
fetching follows the Dual Pane pattern.
The shared rule is that a Relationship UI Model’s runtime behavior is fully determined by its
referenced models.
State Management
The runtime state of the Relationship Engine is split across a small number of data holders. For the
current stage, most of them are internal, only a few extension points are allowed. However, it is
expected that more will be opened up in the official release.
Data holders
Per activity the Relationship Engine creates the following data holders:
Changelog data holder
Append-only log of changes the activity has accumulated since it was opened, plus a checkpoint
stack used by detached repeat groups and similar transactional flows. This is the single source of
truth for "what has changed".
Document Graph data holder (Composed Document Model only)
Immutable snapshot of the Composed Document Model tree, that is the documents and links
reachable from the Composed Document Model root, plus link documents. Each new changelog
entry is folded into a new Document Graph instance via a pure transformation. Activities outside
Composed Document Model do not have a Document Graph.
Selected items instance and available items instance data holders
One per Relationship UI Model. They hold the Overview Engine shaped data the panes render.
The Relationship Engine owns their loading; Overview Engine renders from them.
Dropdown selection data holder
One per Dropdown Relationship UI Model. Holds candidate sets and the current selection.
28

-- 28 of 43 --

Selectors expose this state to the view layer. The important ones to know about by name are the
effective changes selector (deduplicated changes used by the save path), the drafting document
and drafting link selectors (see below), the link target lifecycle selector (drives badges and the
inherited-row enforcement), and the selecting document entries selector (drives the runtime
extension of the selected items pane query).
The changelog as a single source of truth
29

-- 29 of 43 --

Figure 3. Redux action flow: Form Engine and Overview Engine dispatch native events. RE middlewares
intercept actions for owned activities and produce typed Changelog entries. The Changelog feeds the
DocumentGraph, CDM computation, and ultimately the React UI.
Every change starts as a changelog entry. The change kinds are:
• Link added and link deleted, the structural relationship changes. Available both in Composed
30

-- 30 of 43 --

Document Model activities and outside them.
• Link document changed, a field-level update or a wholesale replacement of a link document.
• Document added and document changed, that is drafting a new child document and editing a
field on an existing Composed Document Model document. Available in Composed Document
Model activities only.
• CDM root computed, a transient cache of the most recent SCDM computation result.
• Sub-document-graph added, a transient cache of a sub-document-graph slice fetched for a
freshly-added link.
The two transient kinds are excluded from the deduplicated effective-changes view used at save
time, but they participate in changelog seeding so that downstream replays remain synchronous.
Each entry carries an optional inherited flag. Inherited entries appear in a child activity that was
seeded from a parent, or via the Parent Link Descriptor protocol described below; they apply to the
Document Graph normally so the child sees the link or document, but they are filtered out of the
merge path back into the parent. The single exception is the Parent Link Descriptor’s injected
linkAdded, which is kept because it represents the child’s own parent-facing relationship and must
be persisted.
Document Graph
The Document Graph is an immutable structure: documents indexed by their document reference,
links indexed by their link id, and a per-document index of which links touch each document. A
monotonically increasing index records how many changelog entries have been applied to produce
the current snapshot.
Each application of a changelog entry returns a fresh Document Graph instance. This is what makes
selectors cheap: structural sharing keeps unchanged sub-trees identical by reference, so React re-
renders are scoped to what actually changed.
Reflecting the changelog in the selected items pane
The selected items pane is rendered by an Overview Engine instance. Its rows would normally
come exclusively from a Query result, which only knows about persisted state. The Relationship
Engine bridges this gap with three strategies. The strategy used for a given Relationship UI Model is
determined entirely by the model’s query shape; there is no per-call flag to set.
Selecting documents
For relationships where duplicates are not allowed, the engine concatenates the pending target
document references onto the result set served by the Overview Engine query and triggers a
reload. Because the extension is performed at the result-set level rather than by mutating the
query semantics, Overview Engine features such as search, filter and paging continue to work
without being aware of the client state.
Drafting links
For relationships where duplicates are allowed, the Overview Engine query is generated with a
per-link response shape that is needed to display duplicates. The result-set extension used in the
31

-- 31 of 43 --

standard mode is not applicable in that shape. Instead, when the user adds a candidate, the
Relationship Engine captures a snapshot of the target document at add-time and attaches it to
the changelog entry. A drafting-link row is prepended to the selected items pane, identified by
link id (so duplicate rows do not collide), without contacting the server.
Drafting documents (Composed Document Model)
A new child document in Composed Document Model has not been persisted on the server yet.
The Relationship Engine records a docAdded entry, which both adds a node to the Document
Graph and causes a drafting-document row to be prepended to the selected items pane. Clicking
the row opens a child activity pre-seeded from the parent’s changelog, where the user can fill in
the new document; on save it is merged back up into the parent.
The three strategies are mutually consistent: the selecting document entries selector returns an
empty set whenever the Relationship UI Model’s query is in the per-link response shape, which
automatically suppresses both the result-set extension and the post-add reload. Render order in the
selected items pane is: drafting documents, then drafting links, then server rows.
Save routing
When an activity saves, the save handler reads the deduplicated effective changes and decides
between three outcomes:
• No effective changes, a no-op.
• Merge: the parent activity has its own Relationship Engine changelog; the child’s local changes
are folded into the parent’s changelog. SCDM and the Document Graph are recomputed
synchronously by replaying the merged changes.
• Persist: there is no Relationship Engine parent (or no parent at all); the changes are translated
into DataServices RPCs (addDocument, addLink, updateDocument and similar) and dispatched in a
single batched call.
If any pending linkAdded requires a link document but does not yet have one, the save handler
prompts the user to fill it in before continuing. Cancelling the prompt aborts the save without losing
the changelog.
Parent Link Descriptor
There are situations where a master or list-based engine, such as Tree Engine or Overview Engine,
opens a child activity that should already display a link to a parent document. For example, adding
a person under a company tree. From the Relationship Engine’s point of view there is no parent
activity, no parent changelog, nothing to seed from, and yet the child form must show the parent
link the moment it opens.
The protocol for this is the Parent Link Descriptor. It is a structural subtype of the standard A12
activity descriptor: any descriptor that carries the three fields
• parentInstance,
• parentRelationshipName,
• parentRelationshipRole
32

-- 32 of 43 --

is recognized by the Relationship Engine at activity init time. The engine looks up the relationship
model, derives the child’s role from it, then prepares a linkAdded entry on the child’s changelog with
the inherited flag set, and applies it to the initial Document Graph (if present). From that point on
the link is a regular changelog entry: visible in the form, included in lifecycle calculations, persisted
as part of the save.
Inherited links are read-only in the UI. Any link modification actions are disabled regardless the
component type. This is enforced through the inherited flag from the changelog entry.
Limitations worth noting:
• The descriptor is read once at init. Changing its fields later does not retroactively update the
child; the activity has to be re-opened.
• Only one inherited parent link can be declared per child. Multiple parent links would require
an extension to both the descriptor shape and the init handling.
• The injection succeeds whenever the relationship model exists in the model graph. If the child’s
Relationship UI Model does not actually reference that relationship, the injected entry is silently
inert.
View Customization
The Relationship Engine view layer follows the same pattern as Form Engine, Overview Engine and
Tree Engine: a ComponentMap for replacing top-level components, a WidgetMap for replacing individual
widgets inside them, and a context provider that merges application-supplied overrides on top of
the defaults.
ComponentMap
33

-- 33 of 43 --

Figure 4. View layer architecture: the Relationship Engine overrides three FormModelMap slots (Screen,
CustomScreenElement, DetachRepeat) and dispatches to component-specific implementations. DualPane
and TableList render via Overview Engine internally; link forms are spawned as Form Engine activities in a
modal region.
The ComponentMap exposes the top-level Relationship Engine components. Each entry has a default
implementation shipped with the package and can be replaced individually:
• DualPane, the two-pane selection UI with the selected items pane on one side and the available
items pane on the other.
• TableList, a single-pane list with inline add, edit and delete actions.
• DropDown, an autocomplete-style single-select dropdown.
• OverviewEngine, the wrapper used internally by DualPane and TableList to render the Overview
Engine backed table for a pane. Replacing this entry lets an application customize Overview
Engine rendering for every Relationship Engine pane in one place; the wrapper receives a
paneType of "link" or "candidate" so the override can branch per pane.
• Button, a renderer for a single button element from the UI model.
Application-supplied entries are merged on top of the defaults: any entry left out keeps its default.
The merging is memoised so replacing one component does not invalidate the others.
34

-- 34 of 43 --

WidgetMap
The WidgetMap extends the Overview Engine WidgetMap with the widgets the Relationship Engine
itself needs. Because the Relationship Engine renders Overview Engine view components inside its
panes and forwards its WidgetMap to them, any Overview Engine cell-widget customization defined
in the Relationship Engine WidgetMap propagates automatically into Relationship Engine rendered
tables.
The Form Engine widget map is a separate concern: the Relationship Engine delegates link-
document forms to Form Engine, and the Form Engine WidgetMap continues to govern field
rendering inside those forms.
Context provider
The context provider accepts partial ComponentMap and WidgetMap overrides and merges them with
the defaults. Inside Relationship Engine components (and inside any custom replacement) the
merged maps are read through a selector hook, so a re-render is triggered only for consumers that
read the specific entry that changed.
The provider is meant to wrap the part of the React tree that contains Relationship Engine rendered
components. In a typical A12 client setup it is mounted near the top of the application alongside the
other engine providers.
Inherited links
Custom replacements of DualPane, TableList and DropDown are expected to honor the inherited-row
protocol described in State Management. The link target lifecycle selector exposes an inherited set;
rows whose target reference is in that set must not expose the delete, restore or edit-link-document
actions. The default implementations enforce this through their internal row-action state hook;
custom components reusing the defaults inherit the behavior automatically and only need to
handle it explicitly when reimplementing the row from scratch.
Hooking into engine events
Customizing the rendered components covers most integrations. When the end-to-end application
also needs to react to user actions outside the panes, such as trigger an analytic, navigation or side
panels, both the Relationship Engine and Overview Engine expose dedicated public events that can
be observed from a custom middleware or saga registered as part of the application setup.
An end-to-end application can attach to either set of events by registering an additional middleware
in the application setup; middlewares compose additively with the ones contributed by the engines.
Migration
To convert a model workspace from existing nested-JSON binding annotations into standalone
Relationship UI Models, an NPM CLI tool is provided. It reads the existing models in place and
writes to the necessary models alongside them, ready to be committed.
35

-- 35 of 43 --

Running the tool
Point the tool at the root of the application’s model workspace (the directory containing the model
files). No installation is required; npx fetches the latest published version on demand:
npx @com.mgmtp.a12.relationshipengine/relationshipengine-binding-extraction@latest
<path-to-workspace>
The tool inspects each Form model in the workspace, extracts every binding annotation, and emits
a Relationship UI Model file per binding annotation next to the original model. Form models are
updated to reference the new Relationship UI Models by name; the previous annotation strings are
removed. Query Models needed by the new Relationship UI Models are generated alongside, case by
case.
When the input is a directory, the tool automatically uses that directory as the workspace root for
model and resource lookup. No additional flags are needed.
Custom workspace path
If the target files do not reside at the workspace root, for example, when pointing the tool at a
single Form model filem use --workspace (or -w) to specify the workspace root explicitly:
npx @com.mgmtp.a12.relationshipengine/relationshipengine-binding-extraction@latest
<path-to-form-model> --workspace <path-to-workspace>
The workspace directory is scanned for all relevant models that the extraction pipeline may need to
resolve cross-model references (overviews, query models, and so on). Generated models are written
relative to the workspace root.
Recommended workflow
1. Migrate workspace models to the current release line. The tool only supports extraction from
the binding annotation format to the RuM based models, and is not responsible for other
migration tasks.
2. Run the tool on a clean working tree so the resulting git diff shows precisely which models
were rewritten and which Relationship UI Models and Query Models were generated.
3. Optionally, use SME and preview app to review the generated models and verify the results. The
tool offer a gateway to transform between Binding Model and the new Relationship UI Model,
and tends to have a bias towards generating more models rather than fewer, so some manual
cleanup/adjustment might be desired.
4. Once models are fully prepared, follow the Getting Started to setup the runtime environment.
After this point the application runs against the new Relationship Engine architecture and the
current stack can be removed.
36

-- 36 of 43 --

API Documentation
The full type-level API reference for the Relationship Engine packages is generated from JSDoc by
TypeDoc and published alongside this guide. @internal exports are excluded from the generated
reference; what remains is the supported public surface.
The two packages of interest are:
• @com.mgmtp.a12.relationshipengine/relationshipengine-core, the engine setup, models, public
events and view-customisation entry points.
• @com.mgmtp.a12.crud/crud-core, the CRUD components built on top of relationshipengine-core.
When consulting the generated API, treat the chapters in this guide as the conceptual map:
anything described here has a public type behind it, and the TypeDoc page is the authoritative
reference for the exact signatures.
The generated reference can be found here.
Breaking Change Management
For the general definition of breaking and non-breaking changes in the A12 platform, as well as
frontend and backend perspectives, see the A12 Breaking Change Management page.
The following section describes how this general definition is interpreted for the Relationship
Engine.
Public API Surface
The public API is defined by the exports in index.ts. Any removal or incompatible change to an
exported symbol is a breaking change. Additions to the public API are non-breaking.
Exports annotated @experimental may change in minor releases without being considered breaking.
Exports annotated @internal are not part of the public API and carry no stability guarantee.
Resource Keys
Adding new entries to RELATIONSHIP_RESOURCE_KEYS or CDM_RESOURCE_KEYS is non-breaking:
applications that do not override those keys continue to receive the default translation. Removing
or renaming an existing key is a breaking change for any application that provides a custom
translation for it.
Redux State Shape and Action Types
The Redux action type strings and the reducer state shape are part of the public contract. Renaming
or removing an action type, or restructuring the state in a way that breaks existing selectors, is a
breaking change.
37

-- 37 of 43 --

Selectors and sagas that are part of the public API follow the standard public-API rule above.
Composable Factory
The signature of withRelationshipEngine() is public API. Any change to its parameter types or
return type that requires call-site updates is a breaking change. New optional parameters are non-
breaking.
UI Model Format
Only changes that are incompatible with existing Relationship UI Models or CDM models are
breaking: for example, removing a required field or changing the semantics of an existing field.
Adding an optional field is non-breaking even though it triggers a model schema version bump
and a corresponding migration step. When such a change is introduced, the
@com.mgmtp.a12.relationshipengine/relationshipengine-ui-model-migration CLI tool handles the
version upgrade automatically.
Server Protocol
Changes to the wire format between the client-side server-connectors and the Java backend
(request/response shape, query parameters, endpoint URLs) are breaking on both the frontend and
backend sides.
Form Engine Integration
The custom widget contract between the Relationship Engine and @com.mgmtp.a12.formengine is part
of the public API. Changes that require updates in the Form Engine integration layer are breaking
changes.
Non-Breaking Examples
The following types of changes are consistently treated as non-breaking in the Relationship
Engine:
• Adding a new optional configuration field to an existing API
• Adding new RELATIONSHIP_RESOURCE_KEYS or CDM_RESOURCE_KEYS entries
• Introducing a new @experimental export
• Internal refactoring under core/src/internal/ that does not affect public exports
• Bumping a transitive dependency with no visible API impact
Migration Instructions
TIP For migrating from the current architecture to the new architecture, see the Migration
38

-- 38 of 43 --

chapter in the New Architecture section.
2026.06
CAUTION Please have a look at Migration to latest A12 chapter for an explanation of
general steps.
3.0.0
React peer dependency raised to 19.2
Tooling: none — manual change.
The minimum supported React version has been raised from ^19.0.0 to ^19.2.6. Ensure your
project’s package.json meets this minimum:
{
"dependencies": {
"react": "^19.2.6",
"react-dom": "^19.2.6"
}
}
Redux 5 upgrade
Tooling: Client codemod (@com.mgmtp.a12.client/client-codemod migrateTypescriptFsaImports),
optional — automates the typescript-fsa import rewrite only (see Codemod:
migrateTypescriptFsaImports). The dependency updates and AnyAction → UnknownAction changes are
manual.
The relationshipengine-core package now requires Redux 5 (peer dependency redux@^5.0.0). This is
a breaking change for consumers still on Redux 4.
Affected public types in relationshipengine-core:
• typescript-fsa types (Action, ActionCreator) are now re-exported from
@com.mgmtp.a12.client/typescript-fsa-redux-5-compat instead of typescript-fsa directly.
• AnyAction is replaced by UnknownAction from redux. For example, cddDataHolderReducerExtension
now accepts UnknownAction instead of AnyAction.
Recommended steps for consumers:
1. Update redux, react-redux, and redux-saga to versions compatible with Redux 5 in your host
application.
2. Replace imports of typescript-fsa types with the @com.mgmtp.a12.client/typescript-fsa-redux-
5-compat shim.
3. Replace AnyAction usages with UnknownAction from redux.
39

-- 39 of 43 --

Codemod: migrateTypescriptFsaImports
The optional Client codemod automates a single change category:
• Rewrites typescript-fsa import paths — every import … from "typescript-fsa" is repointed
to the compat shim @com.mgmtp.a12.client/typescript-fsa-redux-5-compat. Example:
Before
import { actionCreatorFactory, type Action } from "typescript-fsa";
After
import { actionCreatorFactory, type Action } from
"@com.mgmtp.a12.client/typescript-fsa-redux-5-compat";
Run it from your consuming project:
pnpm dlx @com.mgmtp.a12.client/client-codemod migrateTypescriptFsaImports
The codemod does not touch dependency versions or AnyAction → UnknownAction usages — those
remain manual.
Replace SagaIterator with SagaGenerator
Tooling: none — manual change.
All saga return types have been changed from SagaIterator (from redux-saga) to SagaGenerator
(from typed-redux-saga). This affects the public API of cdmSagas() and
RelationshipFactories.createSagas().
If you have custom sagas that explicitly type their return type as SagaIterator, update them to use
SagaGenerator instead:
// Before
import { SagaIterator } from "redux-saga";
function* mySaga(): SagaIterator<void> { /* ... */ }
// After
import { SagaGenerator } from "typed-redux-saga";
function* mySaga(): SagaGenerator<void> { /* ... */ }
typed-redux-saga is already a transitive dependency via @com.mgmtp.a12.client/client-core.
40

-- 40 of 43 --

2025.06
CAUTION Please have a look at Migration to latest A12 chapter for an explanation of
general steps.
2.0.0
Migrate to ESM
The npm artifact @com.mgmtp.a12.relationshipengine/relationshipengine-core was migrated from
CommonJS to ESM. When using Node 22.12+ and modern build tools, there should be no changes
necessary to your bundler setup.
NOTE If your tests depend on mocking/stubbing Relationship Engine API, consult the
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
41

-- 41 of 43 --

Remove the option to enable Query API
Since the Data Services Query API is integrated by default, the option to enable Query API have
been removed.
Register Relationship Engine 2.0.0
import { RelationshipFactories, createCddDataProvider } from
"@com.mgmtp.a12.relationshipengine/relationshipengine-core";
// appsetup.ts
const dataHandlers: DataHandler[] = [
...otherDataHandlers,
createCddDataProvider(),
RelationshipFactories.createRelationshipDataProvider(),
];
Replace cddDataProvider with createCddDataProvider()
The cddDataProvider has been moved to internal. Use createCddDataProvider() instead.
Remove the Quadro Pane
In 2025.06, the Quadro Pane is completely removed. For more details, refer to 2024.06
documentation.
Customize the progress indicator after Client Container API removal
Since the Client Container API has been removed in this release, customizing the progress
indicators requires wrapping the desired React component with the
ProgressIndicatorContextProvider, as shown below:
Customizing the Progress Indicator After Client Container API Removal
// Before
import { Container } from "@com.mgmtp.a12.client/client-
core/lib/core/configuration/index.js";
Container.config.rebind(Container.identifier.UISettings).toConstantValue({
progressIndicatorDelay: 500,
progressIndicatorDisableFastAppear: true,
progressIndicatorLabelKey: "a12.progressIndicator.label"
});
// After
import { type Container } from "@com.mgmtp.a12.widgets/widgets-
core/lib/common/main/base-props.js";
import { ProgressIndicatorContextProvider } from
"@com.mgmtp.a12.relationshipengine/relationshipengine-core";
export const RelationshipComponent: React.FC<Container> = (props) => {
42

-- 42 of 43 --

return (
<ProgressIndicatorContextProvider
progressIndicatorDelay={500}
progressIndicatorDisableFastAppear={true}
progressIndicatorLabelKey={"a12.progressIndicator.label"}>
{props.children}
</ProgressIndicatorContextProvider>
);
};
Sorting behavior
From 2025.06 version, sorting behavior for the candidate list is changed.
• Case is ignored during sorting (ignoreCase is set to true).
• If no default sorting is specified, the candidate list is sorted by __meta/createdAt in descending
order.
43

-- 43 of 43 --

