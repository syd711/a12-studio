# client client documentation bundle

A12 Client
Introduction
With the Client we pursuit a model-driven solution for building the client-side of business
applications.
The Client uses the same model based approach like the Engines, i.e., an Application Model along
with an interpreter that describes the UI structure and the user interaction behavior of the client
application. Therefore, the model introduces high-level concepts that are independent of the
underlying Client technologies.
It can also be seen as an orchestrator for the Engines and Widgets. The A12 team hides a lot of
complexity within the Engines and the Services (like validation/computation), so that the Client
runtime can be focused and lean.
1

-- 1 of 138 --

Further features for the Client are:
• Support for desktop and mobile devices out of the box. Note that while Plasma is not mobile-
first, it ensures usability and consistency across device classes.
• Flexible UI frame according to guidelines by A12 UI/UX team. Client has the concept of frame
regions (in which views live) that improve upon the rigid three-pane layout of former UX
approaches.
• Extensible by various mechanisms like factories and inversion of control.
• Modern and lean technology stack: just TypeScript, React, Redux, Redux-Saga as well as Engines,
Widgets, and Services.
Technologies
Client is a front-end platform based on React libraries, written in TypeScript, and built via Node.js.
2

-- 2 of 138 --

[Not supported by viewer]
A12 Client Core
Public API
Customer Project
A12 Client Extensions
A12 Engines & Widgets
A12 Client Services
TypeScript
[Not supported by viewer]
React-based Architecture
Views	State & Data
Normalizr
ES 2024 / Next / TypeScript
react-router
Relay / Apollo Client
React Nat
React
redux-thunk
redux-react
Data Structures
Immutable.js
Expo
[Not supported by viewer]
Redux
reselect
recompose
immutable-assign
Mobx
mobx-react
or
redux-saga
Side Effects
Immutability Helper
HOC
GraphQL
State Management
The Client architecture invests fully in the React ecosystem. We have chosen the following
technologies:
• TypeScript: It’s static typed (but weak typed) with features that allow for truly scalable (code-
wise) application development. Although React and Redux are JavaScript, we use the available
TypeScript typings, so it becomes even safer to use these libraries.
• React: efficient rendering of the UI component tree ("virtual DOM") without caring about
current DOM state, i.e., no data binding required. React is also at the core of the Engines and
Widgets.
• We have applied best practices like functional rendering, separation of container and
presentational components, and extensions through wrapping with higher order components
(HOCs).
• Redux: easy-to-understand unidirectional data flow. It follows heavily the functional paradigm
instead of object-orientated. Redux enables the Immutable App Architecture, which is the core
of the Client architecture.
• Sagas: make complex asynchronous control flows look like synchronous code. Sagas are elegant
for any kind of background work that you would put into threads in Java.
Please understand the Redux concepts like store, state, reducer, middleware, action, action creator;
and also how connecting React components works via provider and the connect functions of the
"react-redux" library.
3

-- 3 of 138 --

Figure 1. Source
Sagas are, like middleware and specially thunks, a way to introduce impure effects into the pure
functional world of Redux, like asynchronous I/O. However, unlike thunks, sagas express these
asynchronous steps in conventional sequential manner (by utilizing JavaScript generators
combined with async and await).
With Redux-Saga, the situation changes slightly:
4

-- 4 of 138 --

Figure 2. Source
Sagas are background generator functions that typically are triggered by incoming actions and then
start off complex steps.
Note that the take effect (and any variants), that waits for an action in a saga, returns only after the
store changes happened and all middlewares and reducers are already executed for a specific
action. Meaning that a saga can only react but not change or discard actions (unlike middleware).
Principles
The following diagram shows the principal structuring of the Client into modules and their
responsibilities in the architecture.
5

-- 5 of 138 --

Views
<span>Abstract UI Structure</span>
A12 Engines	[Not supported by viewer]
Store<br>
Application Model
Data Hub as Sagas<br>
A12 Plasma Widgets
[Not supported by viewer]
Custo
Data I/O
State Management
Activities
Interpreter
Data Loader<br>
Layouts
Data in/out / Events
FormEngine<br>W
Supporting Sagas
Load/Save
[Not supported by viewer]	Commit
Custom Business Sagas
Business Logic
The Client modules fall in one of these categories:
• State Management
• Application Model
• Store
• Supporting Sagas
• Data I/O
• Data Hub with the DataLoaders/DataEditors
• DataProvider Principles
• Presentation
• Abstract UI Structures
• Views
• Business Logic
Packages
The public API of the client library is provided via the following npm packages:
• @com.mgmtp.a12.client/client-core: Provides the core functionality of the client library
• @com.mgmtp.a12.client/client-application-model-migration: Provides the application model
migration tool
• @com.mgmtp.a12.client/client-data: An experimental data access library for client-side
components. This package is not yet stable and may be moved, renamed or removed without
deprecation in future releases. Use with caution.
6

-- 6 of 138 --

Getting Started
This chapter gives a quick guide to start developing you own application with the Client.
Prerequisites
In order to use the Client library no special software dependencies are necessary.
Useful Tools
A12 Modeling Tools
The A12 SME offers the opportunity to create document and UI models. It provides a way to upload
models to the server. Be aware that the A12 modeling tools version has to fit to the version of the
backend (i.e. Services and Engines).
Please check the A12 Releases page to find out about compatible versions of different A12
components.
React and Redux Developer Tools
The React and Redux extensions are essential to inspect your application’s UI and backend state.
Basic Concepts
This chapter gives an introduction to the basic concepts of the A12 Client.
Application Frame
Elements of the Application Frame
The A12 UI/UX team has defined a specific structure for the Plasma design that is called application
frame. It consists of four distinguished elements:
• Application Header: contains e.g. logo, selected locale, user, and role information
• Navigation: shows the main menu
• Context (Sidebar): displays additional information, context-related actions, and allows for sub
navigation
• Content: shows for example the Engines in specific layouts, e.g., a master-detail layout.
7

-- 7 of 138 --

Views
Arranging UIs
The Application Model is an A12 UI Model for defining the "macroscopic" UI aspects of applications
based on Plasma for Business Applications. This includes
• main menu
• regions
• views
Inner parts of the UI can also be created using A12, but this is done using other A12 components,
e.g. Engines based on Form and Overview Model.
Each Application Model consists of Modules. A Module is a high level building block of the
application and directly accessible via the main menu. It consists mainly of:
• (optional) menu entry
• flows with scenes
Menu defines the main menu entry of the Module and which Activity is started when it is selected.
The scenes define the actual functionality of the Module. Each scene consists of
• match conditions
• enter and leave directives
• cases
8

-- 8 of 138 --

Match Conditions
Match conditions define when the scene applies. They always refer to the Activity Descriptor. The
Application Model supports the following matching strategies:
• mustEqual: a property must equal a (constant) value
• isSet: a property must be set
Directives
If the scene applies, the respective scene is "entered" and the enter directives are executed. If the
scene no longer applies, the respective "leave" directives are executed.
The available directives are
• VIEW_ADD
• REGION_CLEAR
The VIEW_ADD directive has the following parameters:
• Region: Defines to which Region the view should be added. If no region is defined, the
defaultRegion property of the Application Model is used.
• Name: Defines the name of the React/Redux UI component that is responsible for visualizing
the view.
WARNING In addition, make sure that the UI component is provided by the View Factory
configured in the application setup.
• Constraints: Contains configurable constraints for the current layout. There are the following
pre-defined properties:
◦ type: refers to the layout to be configured. Some properties can only be specified for certain
types. Currently the only built-in type is "MasterDetail"
◦ preferredWidth: only available for type "MasterDetail". Determines the preferred width of
the rightmost view. If another view is visible on the left hand side, this other view will take
up the remaining space. Note that this property is only evaluated for "detail" views, because
when having just a single view (the "master"), it will always take up the full width.
• Configuration: Contains configurable constraints for the current view.
• models: an optional array of model descriptor objects. Each of them describes an A12 model to
be loaded. Whenever this array is defined, the runtime automatically triggers model and data
loading for this scene
• loadData: an optional boolean flag. When set to true, the runtime automatically triggers data
loading for this scene. This flag is only required, if you want to load data without having any
A12 models.
NOTE
Setting loadData to false will not prevent data loading, if there are models defined
for the current view (see Data Loading). However you can prevent data loading by
9

-- 9 of 138 --

• not defining models and setting loadData to false/undefined
• manually setting the loadingState of the activity to "without"
The REGION_CLEAR directive has the following parameters:
• Region: Defines which Region should be cleared. If no region is defined, the defaultRegion
property of the Application Model is used.
• Layout: Sets the layout for the Region. If no layout is defined, the layout from its region is used.
Regions
Regions are visual locations inside the page. Currently, there are three regions available: CONTENT,
SIDEBAR and MODAL.
• Views placed in CONTENT will appear in the content area of the application frame widget.
• Views placed in SIDEBAR will appear in the sidebar of the application frame widget.
• Views placed in MODAL will appear in a modal dialog.
Cases
Cases are an advanced feature to switch between different UIs without changing Activities.
Views are Identifiable Content Parts
The A12 UI UX team has analyzed several layouts and UI flows from existing projects. Below are
shown the most common ones.
Master/Detail
The most simple is the master/detail layout. Here’s an illustration:
The master is here denoted as "View A" and is typically a data-browser, i.e., a list of business objects
in rows. A12 calls this an "overview". The controls for filtering, sorting and search criteria are here
part of View A for simplicity, but search criteria can also be a view on its own. Also note the sidebar
with optional information about the overview. By selecting a row you open and edit the details of
the corresponding business object, here represented as "View A.B". For example, this is a form with
all attributes as fields and input controls.
This example uses two content parts, which we will denote "views": the overview in a first view, the
detail view in a second view. Now let’s see some more challenging examples.
10

-- 10 of 138 --

Nested Details
The role of being a master and a detail is not fixed, meaning that a detail can become master and
open a new, nested detail. In the following example:
1. The overview in "View A" is a master, shown fully maximized.
2. Selecting a row in the overview brings up the details of the respective business object in "View
A.B".
◦ Using tabs in the detail view, the information of the business object can be broken down into
multiple forms, here "View A.B" (the default view of the business object) and "View A.C". For
example let’s say that the business object is an insurance policy and "View A.B" is the
general data and "View A.C" is the list of terms&conditions, which is shown again as an
overview.
3. Selecting a row in "View A.C" which is a list of terms&conditions as an overview, brings up the
details of the corresponding term&condition item in "View A.C.D" as a form. Please note that the
term&condition item is seen here as nested (or contained) business object of the policy.
Thinking this way, we can apply our concepts in a recursive manner.
11

-- 11 of 138 --

View Assignment
Let’s break this down into an assignment of views:
• Overview "View A" is assigned the view #1.
• "View A.B" and "View A.C" are different "details" of the selected business object, here a policy
with the details "general data" (B) and "terms&conditions" (C). Both render their content into a
view. Note that switching tabs to change between "View A.B" and "View A.C" does change the
view assignment and layout.
12

-- 12 of 138 --

• "View A.C.D" is the detail of the selected term&condition item and is assigned a new view.
Layout
Now let’s specify the layout with a focus on visibility: For a desktop, the content area can only show
up to two views. That means for our example:
• view #1 appears maximized if alone.
• If view #2 appears, view #1 for the overview moves to the left and shrinks.
• If view #3 appears, view #2 moves to the left and view #1 is hidden and effectively disappears.
The layout rules can be dependent on device class and available screen space. For example, a smart
phone might only show the latest view, or render views from top to bottom.
13

-- 13 of 138 --

Summary
In summary, these points are important:
• Activities open up one (or even multiple) views
• A view can use tabs internally in order to show different aspects about the business object
respective the activity. Another option is to use views and switch between them, as discussed in
the subsection below.
• Layout rules can organize views on screen, like maximizing, minimizing, proportions, max
shown views, etc.
Changing Context in the Sidebar Region
Usually, each activity is tied to something like a result list (overview) or a business object. Each view
of an activity can have its own context information in the sidebar. For an insurance policy this e.g.
is the title, insured sum and other key data, but also an action panel with buttons for printing,
progressing the workflow state, etc., is useful.
Until now we have assigned views to the content region only. But the sidebar region can also
contain views. In general, regions contain views, and the application frame has two standard
regions: the content and sidebar areas.
If we break this down, we can assign views for the example above to the sidebar as follows:
• "View A" has no sidebar content, thus the sidebar is hidden.
• For "View A.B" has specific sidebar content, for example "policy info" in "Info" which is assigned
view #S1 and an action panel "B" which is assigned view #S2a.
14

-- 14 of 138 --

• For "View A.C" (which is another view of the same activity as "View A.B"), the sidebar continues
to show "Info" in view #S1, removes any other view in the sidebar and adds a new view #S2b for
the action panel "C" for terms & conditions.
• For "View A.D" (again another view of the same activity as "View A.B" and "View A.C"), the
sidebar again continues to show view #S1, but again removes any other view in the sidebar and
adds a new view #S2c for the action panel "E" for coverage.
Thinking in View Directives
You might wonder why we focus on the sequence of delta changes when navigating between the
views and not just specify how each view should look. The reason is that activities and view
navigation actually express these delta changes by using directives: "VIEW_ADD" (add view named
X to region Y) and "REGION_CLEAR" (clear region Y, i.e., remove all previously assigned views). We
will learn much more about this in the section about the Application Model.
Summary
In summary, these points are important:
• The application frame has two regions that contain views: content area and sidebar. Others are
possible, but must be added explicitly by the projects.
• Views can be assigned and removed to achieve view-dependent dynamic content in the regions.
Example: Design Study
We now want to apply the ideas to a example.
As can be seen below, the application frame shows a menu in the header. Clicking on the menu
entry opens the overview. This action technically does the following:
• Starts a new overview activity.
• Allocates a view in the content region.
• Renders an Overview Engine with a result list of selectable objects.
15

-- 15 of 138 --

Selecting a specific row opens up the details about the corresponding entry. From the technically
perspective, this again starts a new activity for editing the objects and changes the regions like this:
• Three views are assigned to the sidebar: an information panel, an action panel, and a view
selection menu.
• One view in the content area with a Form Engine.
Clearing a Region
Note that the Form Engine view is shown maximized even though an overview view exists. The
view assignment for the activity specified in the Application Model causes all regions to be cleared.
If a previous activity has assigned views to a certain region, these views are ignored by the layout
manager if the region is cleared on behalf of the current/active activity.
16

-- 16 of 138 --

The screenshot below illustrates the next user interaction, where the user switches between cases
which results in a change in layout:
• The first view is removed and replaced by a new view which in this case is an overview.
• Note that the sidebar stays the same.
The overview shows another list and can be seen as a "master". A click in a row brings up the
details for the selected item (see the blue arrow on the right side of the illustration below). This
starts a new, nested activity.
17

-- 17 of 138 --

The next screenshot shows the result of this activity:
• The view for the overview is kept.
• A new view in the content area region is allocated and shows another overview.
• The sidebar stays the same.
18

-- 18 of 138 --

Activity
In the previous section, we introduced the concept of "activities" in a somewhat vague way.
Activities are a very important client-side Concept and are actually quite well specified.
An activity can be seen as a bracket around user interactions, another term for this bracket is "use
case" (taken from requirement engineering). Typical use cases are:
• Overview: showing a list of business objects as a result of a search. Typical user interactions are:
scrolling/paging, sorting, filtering, refining search criteria.
• Editing a business object like using a form.
The Client has special support for these typical use cases, but any user interactions can be managed
as an activity. The DevApp shows a variety of activities.
In order to drive the use case, an activity holds all the relevant data and state, and provides the UI
component (indirectly though) for interacting with the data.
Activity Data Structure
• Activity Descriptor
• Properties
• Operations on Activities
19

-- 19 of 138 --

• Data Loading
• Linking Activities
Technically, an activity is a data structure in the Redux store that holds all information of what the
runtime and the application UI component need to know about this use case instance.
export interface Activity {
readonly id: string;
readonly descriptor: ActivityDescriptor;
readonly initiatingActivityId?: string;
// ...
}
export interface ActivityDescriptor {
readonly instance?: string; // e.g. „P2"
readonly [key: string]: string | undefined;
}
Activity Descriptor
The Activity Descriptor is a key/value-map that specifies what the activity is doing, e.g., the type of
use case and details like the ID of the business object.
The idea is to separate the intention, i.e., "What should be done" from the interpretation and
implementation of "How should it be done". The "what" is the Activity Descriptor and it is the
responsibility of the activity creator to specify it. The "how" is specified by "use case matches" in the
Application Model.
Pre-defined Keys
There are some keys which are part of activity descriptors emitted by A12 components, see Activity
Descriptors. These keys are used to drive predefined behavior in A12 components.
An example of such pre-defined behavior is in the form engine: if a Form Model has been specified
in the matching scene and the instance is set in the descriptor, e.g., { "instance": "P222" }, this is
interpreted as the use case "Editing data in a form": load the document for the specified business
object and bring up a form. This uses the Document Model which is referenced in the specified
Form Model. If the constant NEW_INSTANCE_IDENTIFIER ("__NEW__") from core/application is used as
the instance, it is interpreted as the use case "Opening a new form": create a new document for the
specified business object and bring up a form.
Properties
Let’s break down the most important properties.
• id: each activity needs a unique identifier. The identifier also allows "weak" references to the
activity, i.e., without using object references, which are problematic w.r.t. the "immutable state"
principle of Redux. You are responsible for assigning unique ids to activities. If you want to
cancel and recreate an activity, a new id must be set to differentiate it from previous activities.
20

-- 20 of 138 --

• initiatingActivityId and sourceActivityId: see details below.
• case: this relates to the UI and scenes. See Cases for details.
• dataHolders: each activity has a list of DataHolder objects. Each of them is responsible for one
data source and has its own data descriptor see details below.
DataHolder
A DataHolder is responsible for the data of one data source within an activity. It consists of a
descriptor to specify what kind of data is being stored as well as other properties to keep track of
the data. The Activity Descriptor is then only relevant for determining a matching scene from the
Application Model.
When an activity is created, it is initialized with one default DataHolder, which gets the activity’s
descriptor as its DataHolder descriptor. More DataHolders can be added to the activity later.
The error property of the DataHolder is intended for tracking "loading" or "saving" errors. For
tracking other errors it is recommended to store them inside the slices property of the DataHolder.
This can be easily achieved by creating a custom action and a custom DataReducers.
NOTE
A technical action (like "setData") to set arbitrary data of a DataHolder is not
recommended, because actions should be named from a business perspective. See
the Data Loading section below for alternatives.
All properties of the DataHolder are described in more details in the API reference.
Example
Here is an example of an activity that has a list with three DataHolders. There is one DataHolder
storing the document data of the activity, and two other DataHolders storing link and candidate
relationship data.
const activity = {
id: "223",
descriptor: {
model: "Product",
instance: "DomainProduct/15"
},
dataHolders: [
{
descriptor: { model: "Product", instance: "DomainProduct/15"},
data: {},
dirty: "false",
loadingState: "missing",
savingState: "not_saved"
},
{
descriptor: { type: "candidate", instance: "1" },
data: { /*...*/ },
dirty: "false",
21

-- 21 of 138 --

loadingState: "loaded",
savingState: "not_saved"
},
{
descriptor: { type: "link", instance: "1" },
data: { /*...*/ },
dirty: "false",
loadingState: "loaded",
savingState: "not_saved"
}
]
};
Operations on Activities
Creating an Activity
There are some pre-defined Redux action factories for working with activities.
Use the create function to create the Redux action PUSH that creates (or "pushes") a new activity:
function create({ activityId, activityDescriptor, initiatingActivityId, data,
loadingState, slices }: CreatePayload): Action<PushPayload> { /*...*/ };
Make sure that activityId uniquely identifies an activity across the lifetime of the user session.
Failure to do so might result in unintended behavior (e.g. skipping of model loading). If you want to
cancel and recreate an activity, a new id must be set to differentiate it from previous activities.
If you want to create a new activity with data already in place, the recommended way would be to
hand in this data via the optional data property of the params object.
If you still want to update the data of the activity directly after the creation, e.g. dispatching the
ActivityActions.setData action directly afterwards, you need to wait with the dispatch, until the
data and models resulting from dispatching create are present in the store. Otherwise, the data you
want to set with setData would be overwritten! This is due to the asynchronous model and data
loading, that happens after dispatching create.
Data Loading
Most activity types require data and also models, e.g., an overview has the list of search results and
the Overview Model; the Form Engine requires the document data for the business object and also
the document and UI models. This information is kept in the data property, which has an internal
structure depending on the activity type.
When an activity is created, like in the examples above, all that is required is the Activity
Descriptor. Passing data is optional. Typically loading data and models is and should be delegated
to the runtime, specifically to the DataHandlers.[1]
The property loadingState tracks the progress and gives information about the outcome:
22

-- 22 of 138 --

• "without": the activity does not require any data, and the data property is undefined.
• "missing": the activity requires data and possibly models. The runtime has to load them. Until
they are loaded, the data property is undefined.
• "loading": the runtime is currently loading data and models.
• "loaded": the loading of data and models was successful.
• "error": there was at least one error during the loading of data and models. This information is
optional.
An additional property savingState is a complementary tracking state for saving data.
Linking Activities
The Initiating Activity
This is where nesting of activities comes into play. As we saw in the previous examples, activities
can be easily created, like a selection in an overview master creates a form detail activity. Creating
an activity is always relative to a parent activity (called the "initiating activity") with a reference
by ID as initiatingActivityId. Note that the very first activity has no parent, so the ID is not set
(undefined).
Linking activities this way creates a tree which is utilized in the Client:
• Canceling an activity will cancel all linked sub-activities.
• Committing an activity will commit all linked sub-activities.
datasource
[Not supported by viewer] initiated by<br>
datasource
[Not supported by viewer] 	initiated by
datasource
intiated by
datasource
[Not supported by viewer]
[Not supported by viewer]
r/w via DataEditor
load/save via DataLoader	load/save via DataLoader [Not supported by viewer]
Show List of Offers 	Edit Offer O2 	Show List of GCs
r/w via DataEdit
The Source Activity
There is a second kind of link between activities that is concerned with where the data comes from.
Data can come from an existing activity or from somewhere else (subject to the selected
DataLoader), typically a server side data service.[2]
An existing activity is either the direct parent of the activity or any parent above this parent. The id
is kept in the datasourceActivityId property. If data comes from somewhere else, this property is
not set (undefined).
Consider the example that is illustrated above.
23

-- 23 of 138 --

Work with Business Data
Every task that the user is working on is represented as an activity. An activity resembles a
(possibly long-running) interaction of the user with the system, e.g. filling out a (possibly large)
form, browsing data, reading a message, etc.
The main properties of an activity are
• descriptor
• data and loading state
• initiating and source activity
Activity Descriptor
The Activity Descriptor is an outline of the activity. It declares what the activity is about. Most
activities will deal with business data. Because of this, the notion of a business object is built into
the descriptor. Therefore, it is possible to define an arbitrary number of string properties to
describe the activity. These can augment the business data or be used in activities that do not deal
with business objects.
WARNING Currently, only the property "instance" is reserved for A12 own use.
Activity Data
Of course, activities can also contain the actual data of the business object. Data that comes from
one data source is typically kept in one DataHolder. The data can be of any structure, however in
most cases it should be an A12 Document. The loading state is also held as a separate property
within this DataHolder.
In addition to the actual data, there are several properties providing additional information about
the activity’s data:
• loading and saving state: tells whether the activity DataHolder contains data at all and if yes, if
the data is missing, loaded or the loading failed. These are usually controlled and used by
DataHandlers and related functionality.
• lock: can be used by business logic to prevent any changes to the DataHolder. Does not have
any effect on its own.
• dirty information: can be used by business logic to prevent an accidental cancellation of the
activity.
WARNING The feature only works if the "cancel requested" event action is used
instead of the "cancel" command action.
• error state: Holds information about a "loading" or "saving" error that is currently present.
The error has always an errorCode that defines the type of error and - depending on the type -
additional, type specific information.
WARNING If you create A12 documents by yourself, you need to make sure that they
24

-- 24 of 138 --

fulfill the interface of Activity.Data.Document. Next to your data your
document needs to contain an id and a modelId. For new documents you can
use the constant NEW_INSTANCE_IDENTIFIER as the document id.
Activity Relations
If an activity has been started by another activity, then that activity is available as initiating activity.
If an activity obtained its data from another activity, then that activity is available as (data) source
activity.
DataReducers
The business data held in an activity can be changed by the user working on a task like filling out a
form or by a system executing a technical task. To achieve this, the Client provides the concept of
DataReducers which give fine-grained control over changes to the business data within
DataHolders.
As an example, the UI state of the Form Engine is handled by a DataReducer.
From Activities to Views to Engines
Let’s bring together the concepts we saw so far — views, activities, and also Engines — and show
how they play together to present the UI. We stick to our insurance policy example (here labelled
"offer").
NOTE
For simplicity reasons we will focus only on the default application frame layout.
The behavior for custom layouts depends mainly on the the region and layout
implementation.
When the application starts and no menu item is selected, the application frame is empty or shows
the default content for the sidebar and content.
25

-- 25 of 138 --

Application Header
Navigation
[Not supported by viewer] [Not supported by viewer]
Initial (Empty)
Creating the First Activity (Overview)
In the next step the user clicks on the menu item "Offer" and activity 1 is created with the Activity
Descriptor “{ "model": "offer"}”:
We will study the Application Model only in the next section, but note that this action is declared in
the model.
As a result of creating this activity, scene directives are executed according to the Application
Model. In this case: first clear the sidebar and the content regions, then add a view for showing the
Overview Engine.
26

-- 26 of 138 --

Application Header
Navigation
[Not supported by viewer] [Not supported by viewer]
Initial (Empty) Activity 1<br>Show List of Offers<br>
View
Note that until now the activity is only containing the Activity Descriptor but no data or models,
which is also indicated by the loading state "loading". As a consequence, the view cannot render
much, maybe an animation to indicate this pending state, but not the Overview Engine. Loading of
data and models is done through DataHandlers and orchestrated by the Client runtime.
The next step begins when the data and models are loaded and the loading state is changed to
"loaded". Now the view has all required parameters for the Overview Engine and can instantiate
and render it inside the view.
27

-- 27 of 138 --

[Not supported by viewer]
View
Application Header
Navigation
[Not supported by viewer]
Initial (Empty) Activity 1<br>Show List of Offers<br>
Overview-Engine
Overview Model
Creating the Second Activity (Detail)
The user now interacts with the overview and clicks on a row in order to edit the details of the
offer. Here’s what happens. The view issues a Redux action to create a second activity. This activity
has the first activity as the initiating activity; but not as source activity, since it will get the offer
document data from the server.
As a result of creating this new activity, directives are executed according to the Application Model.
In this case:
• clear the sidebar region (but not the content region),
• then add a view in the sidebar for the info panel
• and another view in the content region for showing the Form Engine
Note again that until now the activity is only containing the Activity Descriptor but no data or
models. As with the first activity, data and models are loaded and the loading state is updated
accordingly.
As soon as the views have all required parameters, the view contents are updated with the actual
information (sidebar) and Form Engine (content region).
28

-- 28 of 138 --

[Not supported by viewer] [Not supported by viewer]
Initial (Empty) Activity 1<br>Show List of Offers<br>[Not supported by viewer]
View
Application Header
Navigation
Overview-Engine
Overview Model
View
Form-Engine
Form Model
View
Info Panel
Changing the Selected Row in the Overview
A typical user interaction in this master/detail situation is the selection of another row in the
master, i.e., overview. One would expect the following to happen in the UI: the form on the right is
cancelled (possibly by asking the user for permission if the form has changed data) and the form
content is replaced with the document data of the newly selected row. This breaks down into an
action sequence and view changes like this:
1. We start with just the activity 1 and showing the overview and the user selects a row in the
overview.
2. Just like before the overview view dispatches an action (create) in order to create a dependent
activity 2.
3. Now the user selects another row in the overview.
4. It needs to be checked if there exists a child activity of the overview activity.
◦ If no child activity exists, a (create) action can be dispatched in order to create a new
dependent activity.
◦ If a child activity exists, this child activity needs to be cancelled by dispatching the
(cancelRequested) action. It is possible to provide an activity to the (cancelRequested) action
that replaces the canceled activity in order to avoid dispatching an additional (create) action
after the cancellation.
Application Model
So far we have focused on the runtime and understand how starting activities creates views in
regions that show the UI elements. One piece of the puzzle is missing so far: how exactly do you
29

-- 29 of 138 --

specify which and how many views are associated with an activity? This section will explain that
this association is in the Application Model (also often referred to as just "App Model"), specifically
in the "scene" descriptions.
NOTE
For a code-first alternative to Application Models, see Dynamic Configuration,
which allows defining regions, flows, and menus directly in TypeScript with
optional state-dependent selectors. When building new applications using A12
Client, we recommend using Dynamic Configuration over Application Models.
Overview
The Application Model is all about the structure of the UI in terms of regions and layout, and
behavior - when and how UI components should be placed in the UI. The building blocks of the
model are summarized in this diagram:
[Not supported by viewer]
[Not supported by viewer]
[Not sup
<b>Region</b
+ name
+ Layout
+ Child Region [ ]
Scene<br>
+ name
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
+ Default Case name
+ Main Scene Change
(in addition to Case SC) 	<span>Scene Change</span><br>
[Not supported by viewer]
+ OnExit Directives [ ]
[Not supported by viewer]
+ Region name
+ Id (for numbering)
+ UI Component name<div><br></d
+ Models
[Not supported by viewer]
[Not supported by viewer]
+ name
+ Scenes [ ]
Module
+ Menu Entry
incl. Activity Descriptor
+ Flows [ ]
<span>Application</span>
+ name
+ Regions Configuration
+ Module [ ]
[Not supported by viewer]
+ name
+ Menu Entry
(for sidebar)
+ Scene Change<div><br></div>
Region Hierarchy<br>
Let’s go quickly through these elements:
Application
• Header specifies generic model properties like id, supported locales, annotations etc.
• Specifies the modules
• Regions configuration specifies the application frame and layouts.
Module
• Functional domain of an application
30

-- 30 of 138 --

• Represented as "Main menu entry", that triggers the creation of the flow’s initial activity.
Flow
• UI interaction bracket, i.e. it divides the user interaction into discrete parts. Think of it as a
micro-workflow on the client side.
• In this sense, it is also a closure for a set of connected UI configurations; specifically it breaks
down into multiple steps in the flow that we denote scenes.
Scene
UIs in the Client are modeled using "Scenes", where a scene is "entered" when some configurable
conditions are met. When a scene is entered, its specified scene change (which in turn contains
scene directives) is executed. This, in turn, reconfigures the UI.
• Single UI configuration
• triggered by a change in the activities and activated by an Activity Descriptor match
• linked to a (required) prior scene (except initial scenes of a flow)
• AD match always includes the prior scene implicitly
Case
• Sub-case of an UI arrangement
• triggered by an explicit case change (manually) / activated by a case menu entry
• hence, all cases within a scene operate on the same activity!
Scene Change
• Contains directives that should be executed when the scene/case changes
• onEnter: When the scene/case is entered
• onExit: When the scene is followed by a next one (see below)
Here’s an example and break-down the UI and its changes in terms of flows, scenes and scene
changes:
Flow
Top-Level Layout "<span>ApplicationFrame"</span>
Region "CONTENT"	"SIDEBAR"
Scene 1 	Scene 2 	Sce
[Not supported by viewer]	<b>View S1</b> 	<b>View S1</b> 	<b>View S1</b>
[Not supported by viewer]	[Not supported by viewer]
Configuration of Regions
We have seen how applications can place contents of UI components in regions, like the content,
31

-- 31 of 138 --

sidebar and modal region. However, the Application Model allows arbitrarily named and even
nested regions.
• In the Application Model, regions can have any name and there are no "predefined" regions .
The semantics of the regions are defined by the layout that is assigned to the region.
• The tree of regions is defined (statically) in the Application Model.
• There is one top-level region - the application (or "app") region. If it has the ApplicationFrame
layout then it resembles the classical application frame with the content and sidebar regions.
Here’s the excerpt from the model that sets up the three regions of the previous examples:
{
"region": {
"name": "APP",
"layout": { "name": "ApplicationFrame" },
"subRegions": [
{ "name": "CONTENT", "layout": { "name": "MasterDetail" } },
{ "name": "SIDEBAR", "layout": { "name": "Null" } },
{ "name": "MODAL", "layout": { "name": "Stack" } }
]
},
"defaultRegion": ["CONTENT"]
}
The resulting region configuration then looks like this:
Region "APP"
[Not supported by viewer]	[Not supported by viewer]
Layout:<br> "Application Frame"
[Not supported by viewer]
declares
[Not supported by viewer]
<span>Region "APP"</span>
Layout: "MasterDetail"	[Not supported by viewer]
declares
[Not supported by
declares
[Not supported by viewer]
Example Region Configuration in Application Model
Region Layout
For each region, the layout must be specified by name with optional settings data. The provided
layouts of the Client are shown here:
32

-- 32 of 138 --

Layouts provided By Client 	Cu
[Not supported by viewer]	Layout:<br><b> "Application Frame"</b>
[Not supported by viewer]	[Not supported by viewer]
[Not supported by viewer]	[Not supported by viewer]	[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
Default-LayoutProvider
... SubRegions ...Views
...Views	...Views
... SubRegions 	...Views
...Views 	...Views	...Views	...Views
– xor – ..
Please note that custom layouts can be easily installed and this setup is discussed further in the
customization page.
Layouts for regions can be changed at runtime during a scene change, specifically by the
CLEAR_REGION directive. See the page Scene Changes for further details; this page also shows how to
provide extra layout settings data in the dashboard example.
Locales
The property locales is optional and currently unused. However, it is recommended to provide here
all locales that are used in the Application Model.
Modules
The Application Model is a composition of modules. The modules describe all the model aspects for
a certain domain in your application. For example, an insurance application can be broken down
into modules like partner, submission, offer and policy.
The idea here is that you can reuse modules and supporting code in different applications and put
together modules to build up an application. This notion is expressed in the following diagram:
33

-- 33 of 138 --

Modules contain two pieces of information:
• Module header including a menu entry: each module can provide a menu entry that is shown
automatically in the menu bar in the header.
• Flows: a flow contains usually many scenes that handle activities in the respective domain and
are connection between activities and the view assignments in regions. For example, the offer
module might consist of three scenes: the overview (list), editing an offer and adding/editing
clauses.
Bottom-up Development
The diagram above also shows the supposed bottom-up development process. You can break up the
full application into smaller parts. You begin with single scene, like showing a search filter with
results (an overview in A12 jargon) or a detail form. Scenes can be assigned to teams and developed
and tested in isolation as a single-scene "application". You now can wire single scenes together for a
user interaction scenario and bundle related scenarios (typically of the same business entity like
offer) into a flow of a module e.g. "offer". A module is a mini-application that again can be tested in
isolation, and the modules can then finally combined into the full application.
Starting an Initial Activity Through the Menu
The application menu is automatically built from the Application Model. Each module can specify a
menu entry like this:
{
"menu": {
"name": "offer",
"label": { "en": "Offer" },
"initialActivity": { "descriptor": { "model": "Offer_v3" } }
}
}
34

-- 34 of 138 --

When you click on a menu entry, an activity is started automatically with the Activity Descriptor
content that is specified in the descriptor property. It is equivalent to createPushActivityAction({
descriptor: {model: "Offer_v3"}, …}).
If the activity does not require any data loading, the property withoutData should be set. This is
equivalent to the above push action with a loadingState set to "without".
The label is localizable by providing different texts in a map. The key is hereby the string
representation of the Locale object. Furthermore, it is possible to extend and override these texts
with a localizer by using the corresponding localization key. Using a customized localizer does not
require that a label is provided in the Application Model.
Flows
A flow is a way to group together coupled UI interaction steps, we denote scenes. Examples of multi-
step user interactions that could be modeled as flows are
• wizards that span multiple steps and involve more than one UI form on the Client [3], e.g. mixing
forms, lists/overviews etc.
• drilling down into a hierarchy of information. Take the example from the Design Study: it starts
with an overview of policy instances, then entering a specific policy document, and from here
listing its general clauses, which are related or embedded information, and looking at various
general clause instances of this list.
Starting a Flow
Flows are not directly instantiated and have no direct tie to activities. However, scenes are
associated with activities through scene matches. And so we have a notion of starting a flow by the
fact that an activity will match a first scene in flow. More on this topic in the following section.
Multiple Flows
The built-in main menu component enforces single-flow behavior: clicking a menu entry first
cancels the current top-level activity and its entire activity subtree, then starts the flow of the
selected entry.
Multiple top-level activities — and therefore multiple flows — can exist in parallel, but only if each
flow’s activity subtree triggers directives that operate on separate, non-overlapping regions. Having
multiple flows compete for the same region is not supported and leads to issues.
Scenes
Now we enter the heart of the Application Model. Scenes are the connection between activities and
the view changes in the UI that are driven by them.
The procedure is roughly as follows:
1. Selection criterion as Match Condition that has to match the Activity Descriptor of the given
activity
35

-- 35 of 138 --

2. Layout changes as Scene Changes
The following screenshot shows the UI views that are shown for an activity:
Scene Match
Now we address the question of how to choose a scene in the model for the given activity. All scenes
of the model are candidates and are checked via a scene match as follows.
The match conditions for a scene must evaluate to true. A selector evaluates a single attribute of the
condition and can evaluate it against matches:
Matches Meaning
mustEqual: string The value must match the string
isSet: boolean A value for the given key must exist. The value
can be anything.
Let’s see an example:
{
"matchCondition": [
{ "key": "model", "mustEqual": "Offer" },
{ "key": "instance", "isSet": false }
]
}
These match conditions evaluate to true for an Activity Descriptor like {mode: "Offer" }, but not for
{mode: "Offer", instance: "433" }.
36

-- 36 of 138 --

Required Prior Scene
Actually, there are two conditions that need to be met for a scene to be matched:
1. Required Prior Scene (optional): The name of the scene within the flow scope that has to be
activated "before" (that is, it is the predecessor). It basically just means, that the parent activity
of the activity that matches the scene must match this referenced prior scene.
2. The Match Condition as shown above.
Initial Scene for a Flow
A scene that does not have a required prior scene, can be matched by the match condition alone.
This implies that this scene starts the flow, and thus we can derive the notion of initial scenes of
flows. Note that
• there can be multiple initial scenes,
• and the ordering of scenes in the model is not relevant.
Example
Let’s look at the example from the previous Flows page and walk through the changes that happen:
[Not supported by viewer]
[Not supported by viewer] 	[Not supported by viewer] 	[Not suppo
Scene "Edit C	Scene "Edit Offer"<br>
[Not supported by viewer]
[Not supported by viewer]
[Not suppo
[Not suppo
Scene "Offer List"<br><br><br>
[Not supported by viewer]
Matching Activities to Scenes
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer] 	[Not supported by viewer] [Not supp
[Not supported by viewer] 	[Not supp
Scene reference is scoped to flow
[Not supported by viewer]
UI State 0: No Scene
• Let’s say there is a module with a main menu entry AD = {model: "Offer", action: "list"}.
• The user clicks on this main menu entry.
• A new activity with the above AD is created.
• Note that there is no initiating activity and hence no prior scene. Therefore, only the scene
37

-- 37 of 138 --

match with the AD properties is conducted among all existing scenes in the model.
• As result, the scene "Offer List" is matched and thus triggered. The directives in the scene "Offer
List" are executed and the respective UI is configured. This effect is not shown in the diagram
above but will be illustrated in the following page.
UI State 1: In Offer List
• The user clicks "edit" on the row representing the document "55".
• A new activity with the AD properties: {"Offer", instance: "55", action: "edit"} is created.
• This time, an initiating activity exists and it matches the AD of "Offer List" (this match is a
consequence of the defined required prior scene "Offer List"). Additionally, the newly created
activity matches the AD of "Edit Offer".
• Mixing Different Model Types: Now that we have "prior scene" as an additional scene match
criteria, we can omit the model match criteria. This allows to edit documents of different types
out of a common scene.
• The scene "Edit Offer" is triggered and the directives in the scene "Edit Offer" are executed (not
shown) and the respective UI is configured.
UI State 2: In Edit Offer
• The user selects "edit clause" for clause 5.
• A new activity with the AD model: {"Clause", instance: "5", action: "edit"} is created.
• Again, an initiating activity exists, and it matches the AD of "Edit Offer". Additionally, the newly
created activity matches the AD of "Edit Clause in Offer". Therefore the scene "Edit Clause in
Offer" is triggered.
• The directives in the scene "Edit Clause in Offer" are executed (not shown) and the respective UI
is configured.
Scene Changes
Scene changes consists of scene directives that reconfigure the UI, specifically the contained views
and layout of regions.
Let’s just continue with the example from the Scene page and refine it with the specific scene
changes:
38

-- 38 of 138 --

[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
Scene Directives:<br>
Top-Level Layout "<span>ApplicationFrame"</span>
Region "CONTENT"	"SIDEBAR"
[Not supported by viewer]	<b>View S1</b> 	<b>View S1</b> 	<b>View S1</b>
[Not supported by viewer]	[Not supported by viewer]
[Not supported by viewer]	[Not supp
[Not supported by viewer]
Scene Directives:<br>
––
[Not supported by viewer]	[Not supported by viewer]
Scene Directives:<br>
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
Scene Match Condition
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
Scene Match Condition
[N
[Not supported by viewer]
Scene Directives:<br> 	Scene Directives:<br>
––
[Not supported by viewer]	[Not supported by viewer]
Scene 1 	Scene 2 	Scen
Here’s an excerpt of directives for a scene change:
{
"onEnter": [
{ "type": "REGION_CLEAR", "region": ["CONTENT"] },
{ "type": "VIEW_ADD", "region": ["CONTENT"], "name": "OverviewCRUD" }
],
"onExit": []
}
The directives of a scene change are either in the onEnter or in the onExit scene changes:
• onEnter directives are executed when a scene is selected.
• onExit directives are executed when a scene is followed by a next one, i.e., when a new activity
is created and this causes a new match condition. This group is for preparing a follow-up
situation. Note that the scene with the onExit does not end - there is just another scene (change)
now on top of it (or you might think of a new nested scene).
NOTE
This is also the reason why there is no onExit directive for cases. Since cases are
mutually exclusive and have no relationship to each other (e.g. there cannot be
something like a "child" case), the condition "when a [case] is followed by a next
one" would never be true in the first place.
39

-- 39 of 138 --

Example: if you don’t want to show the overview/list when a user selects an entry in the list, then
you would change the scene example above to the following:
{
"onEnter": [
{ "type": "REGION_CLEAR", "region": ["CONTENT"] },
{ "type": "VIEW_ADD", "region": ["CONTENT"], "name": "OverviewCRUD" }
],
"onExit": [{ "type": "REGION_CLEAR", "region": ["CONTENT"] }]
}
Scene changes can be given on the scene level but also on the case level. Scene changes on the scene
level are always executed additional and before to any selected case.
The available directives are:
Directive Meaning
REGION_CLEAR Changes the layout of a region. It can have
(optional) geometry information which can be
used by the layout to position its placeholders
(into which the views are placed). The structure
of the geometry information is therefore layout
specific.
VIEW_ADD Adds a view to a certain region. You must specify
a name property, which specifies the UI
component by name that is to be shown in the
view. The name is resolved during view
rendering by looking for a match among the
registered UI components. Optionally it is
possible to specify view specific constraints that
effects the layouts.
Here some more sample excerpts:
{
"onEnter": [
{ "type": "REGION_CLEAR", "layout": { "name": "Stack" } },
{
"type": "VIEW_ADD",
"name": "OverviewCRUD",
"models": [{ "modelType":"overview", "name":"DiveLogOverview" }]
}
]
}
1. The REGION_CLEAR directive can change the layout of the regions.
40

-- 40 of 138 --

2. The VIEW_ADD directive can provide specific model document names to use in the engine of the
view. Here the Overview Model has the very specific "DiveLogOverview" ID.
To better understand the assignment of views to regions, it helps to think of a region as a container
of views:
• A view is always added to the end of the view list of its region.
• By clearing a region explicitly with the REGION_CLEAR directive, you remove all previous views
from the region.
• Replaying: The view list of a region is rebuilt if any new activity is created or when an activity is
finished.
The Replay Algorithm
(This section contains very technical information that is optional.)
You might wonder why there is no directive to remove a view or other means for cleaning up the
views that were added. But cleaning up is not required because it is done automatically. This works
like this: if an activity is finished, the view assignment is reconstructed by the following replaying
algorithm:
1. Let’s assume that activity Ax has finished and thus is removed from the list of activities {A1, A2,
…, Ax, …, An}
2. The leftover list {A1, A2, …, An} is replayed
3. Reset all assignments of views to regions, so that they are empty.
4. A_previous := undefined
5. Run a loop over A1 up to An with A_i as the loop variable:
a. If A_previous !== undefined: execute the directives of the onNext group of A_previous
(including the onNext directives of the selected view of A_previous).
b. Find the matching scene for A_i and execute the directives of its onEnter group (including
the onEnter directives of the selected view).
c. A_previous := A_i
The replaying algorithm is performed as a Redux selector called region.
Layout Changes
Even though regions are setup in the Region Configuration part of the model, you can easily
reconfigure the layout of any region in a scene change.
Let’s illustrate this reconfiguration of the region layout by implementing a dashboard. The layout
"Dashboard" is already provided by the Client. We just need to set it up with a specific
columns/rows configuration, so that the dashboard’s tiles can be positioned properly. The layout
setting consists of a list of rows. Each row contains either a number of views or a list of container
columns.
Let’s implement this sample dashboard layout:
41

-- 41 of 138 --

First, we break it down into the rows and columns like this:
Row 1
Row 2
Column 1 Column 1
View (1)
Row 1
[Not supported by viewer]
Row 2
[Not supported by viewer]
Column 1 Column 2
[Not supported by viewer] [Not supported by viewer]	The dashboard should be assigned to a single activity. Thus, we set up the region for the dashboard
with a REGION_CLEAR directive and the following layout setting. Additionally, we add the five views to
the region in the right order. These views will then be placed into dashboard tiles by the dashboard
layout.
{
42

-- 42 of 138 --

"sceneChange": {
"onEnter": [
{
"type": "REGION_CLEAR",
"layout": {
"name": "Dashboard",
"settings": {
"rows": [
{
"columns": [
{ "width": { "sm": 3, "md": 4, "lg": 9 } },
{
"width": { "sm": 1, "md": 2, "lg": 3 },
"rows": [
{ "columns": [ { "width": { "sm": 4, "md":
6, "lg": 12 } } ] },
{ "columns": [ { "width": { "sm": 4, "md":
6, "lg": 12 } } ] }
]
}
]
},
{
"columns": [
{ "width": { "sm": 4, "md": 2, "lg": 4 } },
{ "width": { "sm": 4, "md": 4, "lg": 8 } }
]
}
]
}
}
},
{ "type": "VIEW_ADD", "name": "DashboardIntroTile" },
{ "type": "VIEW_ADD", "name": "DashboardCalendarTile" },
{ "type": "VIEW_ADD", "name": "DashboardNotesTile" },
{ "type": "VIEW_ADD", "name": "DashboardTasksTile" },
{
"type": "VIEW_ADD",
"name": "OverviewCRUD",
"models": [ { "modelType": "overview", "name": "CRUD-overview" } ]
}
]
}
}
Note that the region is not explicitly given in the directives, since the "CONTENT" region is the
default one, as it was specified in the Region Configuration part of the Application Model.
In summary, this is how we modelled the dashboard:
43

-- 43 of 138 --

Add
Clear Region &<br>set Layout
[Not supported by viewer]
<span>Dashboard Layout</span>
View
CONTENT<br>Region<br>
Text
View Calendar
View Notes
View Tasks
ViewCRUD Overview
Add
Add
Add
Add
[Not supported by viewer]
Scene Directives
To follow up on the dashboard idea, if the user clicks a row in the overview in view 5, a form
should come up. This form is of course a new activity, which in turn triggers a new scene. Let’s
assume that the dashboard in the "CONTENT" region should disappear while the overview is
shown, even on desktops with enough width to show both. Well, discarding the dashboard activity
is not an option. There are two obvious options: using an REGION_CLEAR directive in either the onExit
scene change of the dashboard scene or the onEnter scene change of the overview scene. The
former looks like this:
{ "onExit": [{ "type": "REGION_CLEAR", "region": ["CONTENT"], "layout": "MasterDetail"
}] }
Cases
Cases are an optional feature and can be defined within a scene. Think of them as mutual exclusive
user interface components for editing the data. For example, when editing an offer, one case is the
general data form, another case is the terms & conditions and yet another one is the coverages list.
All these details are contained in the same offer document, but the designers broke it down into
different cases.
Specifically, a case has
• A label which is typically shown in the sidebar menu and is localizable by providing different
texts in a map. The key is hereby the string representation of the Locale object. Furthermore, it
is possible to extend and override these texts with a localizer by using the corresponding
localization key. Using a customized localizer does not require that a label is provided in the
Application Model.
• Scene changes with directives: The selected case contains a scene change that is executed after
the scene change on the scene level. One typically places view with Engines here, while the case
agnostic sidebar is setup on the scene level.
Only one case of a scene can be active (thus, mutual exclusive). This is tracked by the activity, which
44

-- 44 of 138 --

has a property case for the current case. If you change this case property, the region is updated (via
the replay algorithm) to update the view assignment to regions.
When model descriptors are given in the onEnter directives of cases, the models will lazily be
loaded once the case is active.
A menu of available cases can be shown by just adding the standard UI "GenericCaseMenu"
component with a sidebar region. The menu then shows the name for each case, and with a click on
a menu entry, the respective case is selected and the directives of the case’s onEnter group are
executed.
Here’s an example scene that offers three cases and uses the GenericCaseMenu UI component:
{
"scenes": [
{
"name": "Edit Offer",
"description": "Specific Offer",
"matchCondition": [
{ "key": "model", "mustEqual": "Offer" },
{ "key": "instance", "isSet": true }
],
"sceneChange": {
"onEnter": [
{ "type": "VIEW_ADD", "region": "SIDEBAR", "name": "OfferActions"
},
{ "type": "VIEW_ADD", "region": "SIDEBAR", "name":
"GenericCaseMenu" }
]
},
"defaultCase": "general",
"cases": [
{
"name": "general",
"label": { "en": "General" },
"sceneChange": { "onEnter": [ { "type": "VIEW_ADD", "name":
"GenericForm" } ] }
},
{
"name": "terms",
"label": { "en": "Terms & Conditions (General Conditions)" },
"sceneChange": { "onEnter": [ { "type": "VIEW_ADD", "name":
"TermsConditions" } ] }
},
{
"name": "coverages",
"label": { "en": "Coverages" },
"sceneChange": { "onEnter": [ { "type": "VIEW_ADD", "name":
"GenericOverview" } ] }
}
45

-- 45 of 138 --

]
}
]
}
The following diagram shows the situation where an activity is matched to this "Offer" scene and
the cases are switched from "general" to "terms":
46

-- 46 of 138 --

[Not supported by viewer]	[Not supported by viewer]
Scene Change
[Not supported by viewer]
[Not supported by viewer]
Case "terms" Case "coverages"
Scene
match
[Not supported by v
Scene Change
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
Scene Change 	Scene Chang
[Not supported by viewer] 	[Not supported by view
View<br>GenericInfoPanel<br>
– xor – 	– xor –
[Not supported by vi
View<br>GenericCaseMenu<br>
View<br>PolicyActions<br>
View<br>GenericForm<br>
1
1
Scene Change
[Not supported by viewer]
[Not supported by viewer]
Case "general" 	Case "coverages"
Scene
match
[Not supported by vi
Scene Change
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
Scene Change 	Scene Chang
[Not supported by viewer] 	[Not supported by view
– xor – 	– xor –
[Not supported by vie
2
[Not supported b	[Not supported by viewer]
View<br>GenericInfoPanel<br>
View<br>GenericCaseMenu<br> View<br>TermCond
2	<font color="#000000"><br></font>
[Not supported by viewer]
If the case "general" is selected, the resulting UI looks like this:
47

-- 47 of 138 --

When the cases are switched from "general" to "terms", the UI adapts like this:
1. Removal of the views specified for the previous case "general", i.e., the case with "GenericForm"
UI component.
2. Addition of the view specified for the new view "terms", i.e., the view with "TermsConditions" UI
component.
Cases Versus Traditional "Tabs"
Some UIs use the "tabs" approach with only one tab being shown at a time. However, this is very
physical. By introducing cases as an abstraction, you are free to come with any physical rendering
you like. And quite easily so, by means of choosing the right layout (for the tabbed UI, you can
easily provide a layout that shows views in exclusive tabs) for the "CONTENT" region.
Data Loading
The Client provides a powerful data loading concept. Whenever a scene gets activated for an
activity the Client evaluates if data loading is needed. This evaluation can be described as the
following:
1. If the matching scene has models defined, e.g. models: [{ modelType: "form", name: "Product"
}], the runtime automatically performs model and data loading in parallel.
2. If the matching scene has no models defined, but the boolean flag loadData: true, the runtime
48

-- 48 of 138 --

automatically performs data loading.
3. In all other cases, no data loading is performed.
You can always, dispatch an ActivityActions.loadData to trigger the data loading for a specific
activity id.
DataHandlers
The Client supports three kinds of DataHandlers: DataLoader, DataEditor and DataProvider. They
are described in more detail in the upcoming sections. Additionally, a union type DataHandler is
provided for convenience, combining the three types.
By using DataLoader or DataEditor, the runtime provides some default behavior for some data
operation.
• save: after saving, it dispatches an ActivityActions.reload of the current activity. If you pass the
updateActivityData flag to the action’s payload of save/commit, the Client updates the activity’s
data with the response data of the save request.
• delete: before executing the delete request, the Client first tries to cancel all child activities of
the current one. After the delete operation, it triggers a reload for the overview activity, if there
is one.
If you do not want to have any of these, you should implement the data operation with a
DataProvider. There is no default behavior for DataProvider. The runtime simply calls the
provideData() function.
Registration and Order of Execution
DataHandlers are registered in the application setup. For each data operation, the runtime tries to
find a matching DataHandler in the following order:
1. DataProviders and DataEditors provided via the Module API (in order of Module registration)
2. DataEditors provided in the application setup (regardless of their position in the array!)
3. DataProviders and DataLoaders provided in the application setup (following their position in
the array)
The runtime first evaluates DataProviders and DataEditors for all registered modules, before
looking at the handlers from the application setup. Note that while the order of the setup is
important, any DataEditors in the array will always be evaluated first. In the following example, the
runtime would first query the DocumentDataEditor, then the DataLoader and afterwards the
DataProvider, if one of them can handle a DataHolder of the current activity. The first matching
DataHandler is going to perform the data operation.
const dataHandlers: DataHandler[] = [
new DocumentDataEditor(),
new DataLoader(),
new DataProvider()
/* ... */
49

-- 49 of 138 --

]
DataLoader and DataEditor
DataLoaders handle the exchange of business data with external systems. DataEditors on the other
hand handle exchange of business data between activities.
DataLoaders support the following actions:
• load
• save
• delete
DataEditors support the following actions:
• read
• write
Any number of DataLoaders and DataEditors can be configured but for every data action, only one
DataLoader or DataEditor will be used. The selection happens as follows:
• Each DataLoader is asked if it can handle the current Activity. The first matching DataLoader is
executed .
• Each DataEditor is checked if it matches the current Activity and an ancestor of it using match
conditions. A DataEditor defines parent and child match conditions. It matches for an activity if
all the child match conditions of the editor match the Activity Descriptor and if all the parent
match conditions match an Activity Descriptor of an ancestor (defined by the initiating activity
id) of this activity. The ancestor activities are checked bottom to top (first the direct parent of
the activity). The first matching DataEditor is executed and the data is read/written from/to the
matched ancestor activity.
Both DataLoaders and DataEditors only handle the data of the activity’s default DataHolder, which
is the one that has the same descriptor as the activity. Handling the data of additional DataHolders
is supported via DataProviders.
DataProvider
A DataProvider can also handle the exchange of business data with external systems as well as the
exchange of data between activities. Since it is a wrapper around a redux-saga function, it can fetch
data, access the Redux store via selectors or dispatch actions via action creators.
DataProviders support the following actions:
• load
• save
• delete
Any number of DataProviders can be registered in the application setup. Every DataProvider is
50

-- 50 of 138 --

asked if it can handle a DataHolder for the current activity. A DataProvider can handle an arbitrary
number of DataHolders, but each DataHolder can only be handled by one DataProvider.
NOTE
Among other properties, the triggering action is passed to the canHandle method of
each Data Provider. This makes it possible to limit the scope of a Data Provider
based on the presence of certain model types in a scene. See the documentation
about DataReducer on how to extract them from the action payload.
After the selection of all DataHolders is finished, each DataProvider gets its list of DataHolders to
load. Additionally, each provider has access to the payload of the action that triggered it via the
details property (see example below).
If a "loading" or "saving" error should be set to a specific non-default DataHolder, then a custom
action and custom DataReducers are required.
When to use a DataProvider compared to a DataLoader
DataLoaders are intended for simple use cases like "loading a single document" or "loading a list of
documents". Since the methods are asynchronous, requests can be made to fetch some data, which
will then be stored in the default DataHolder of the activity. Only if your logic is more complicated
than that, the more low-level API of a DataProvider should be used. Since these methods are
implemented as sagas, they have full access to the redux state of the application, which allows them
for example to handle different DataHolders.
Empty Document DataProvider
In order to enable the creation of empty document by the Client itself, the form-engine extension
provides a DataProvider factory function (createEmptyDocumentDataProvider). This factory returns a
DataProvider that creates an empty document with initial values and pre-computed values if a new
document should be loaded.
WARNING Without this DataProvider another DataHandler has to handle the request for
loading a new document.
Example
The following example shows an implementation of a DataProvider to load documents from an API
and putting them into the current activity’s data property.
const DocumentsDataProvider: DataProvider = {
name: "DocumentsDataProvider",
canHandle({ dataHolder, operation }) {
switch (operation) {
case "load":
return dataHolder.descriptor.instance !== undefined;
case "save":
case "delete":
return false;
default:
51

-- 51 of 138 --

throw new Error(`No support for ${operation}.`);
}
},
*provideData({ activityId, dataHolders, operation, details }) {
switch (operation) {
case "load":
const documents = yield all(
dataHolders.map(({ descriptor }) =>
// getDocumentById fetches data from a external source and
returns a promise
call(getDocumentById, `/documents/${descriptor.instance}`)
));
yield put(ActivityActions.setData({ activityId, data: documents }));
break;
case "save":
case "delete":
default:
throw new Error(`No support for ${operation}.`);
}
}
};
Data Reducers
A DataReducer is a Client-specific extension of the reducer concept from Redux.
NOTE It is recommended to put as much logic as possible in reducers as can be read in the
redux best practice style guide.
Like a standard Redux reducer it is triggered by Redux actions. However, since DataReducers are
only concerned with the data slice of an activity, they can only handle activity-related actions and
then, once triggered, calculate the update of all DataHolders of the respective activity.
The interface of a DataReducer is quite similar to that of a DataHandler, but only consists of the
reduce method. This function changes the given DataHolders in the Redux store.
export interface DataReducer {
/**
* Can be registered as sub-reducers within the activity reducer. It enables
you to perform any kind of dataHolder update, e.g.
* - adding dataHolder(s)
* - changing dataHolder(s)
* - removing dataHolder(s)
*
* @param dataHolders All dataHolders for the activity specified by the id in
the action payload.
* @param action The triggering action
* @param defaultDataHolder The default dataHolder of the activity, `undefined`
if none exists
52

-- 52 of 138 --

* to keep the existing behavior of `isDefaultDataHolder`
*/
reduce(
dataHolders: Activity.DataHolder[],
action: ActivityActions.DataReducerAction,
defaultDataHolder?: Activity.DataHolder
): Activity.DataHolder[];
}
Adding a DataReducer
Below is a sample implementation of a DataReducer that reacts to a specific action and then
handles its respective payload. The exemplary DataReducer is implemented in a way that it will
only update a specific DataHolder of the activity.
const actionCreator = actionCreatorFactory("Example");
/** Example action to add a new Todo item to the list of ToDos */
export const addTodo = actionCreator<AddTodoPayload>("ADD_TODO");
/** The payload of the addTodo action. */
export interface AddTodoPayload extends ActivityActions.ActivityActionPayload {
readonly todo: string;
}
/** Todo list data structure to manage in a DataHolder */
export interface TodoList {
readonly todos: string[];
}
/** Helper function to assert the type of a DataHolder */
export function isTodoDataHolder(dh: Activity.DataHolder): dh is
Activity.DataHolder<TodoList> {
return dh.descriptor.use === "todo-list";
}
export const AddTodoDataReducer: ActivityReducers.DataReducer = {
reduce(dataHolders, action) {
return addTodo.match(action)
? dataHolders.map(dh => {
return isTodoDataHolder(dh)
? {
...dh,
data: {
...dh.data,
todos: [...(dh.data?.todos ?? []),
action.payload.todo]
}
}
53

-- 53 of 138 --

: dh;
})
: dataHolders;
}
};
NOTE
It is important to note that the trigger action should either extend
ActivityActions.ActivityActionPayload or
ActivityActions.AsyncActivityActionPayload. Otherwise, the action payload would
not contain the activity identifier and thus would not link it to any activity in the
Redux store.
The integration of a DataReducer can happen in two ways:
1. The DataReducer can be provided with the ApplicationFactories.Config object as a part of the
general application setup using the ApplicationFactories.createApplicationSetup function.
2. The DataReducer can be provided via the Module API as a module-specific DataReducer.
WARNING
When writing DataReducers, care must be taken in order to avoid unwanted
state changes. Consider the following example (same as above):
// this reducer only looks for a specific action and just returns
the unchanged dataHolders array otherwise
const addTodoReducer: ActivityReducers.DataReducer = {
reduce(dataHolders, action) {
return TodoActions.addTodo.match(action) ?
dataHolders?.map(handleAddTodo(action)) : dataHolders;
}
}
This reducer will NOT change the state if the action type does not match by
returning the same object reference. This allows any activity-related actions
(e.g. "loadData") that need to be handled by Client’s own reducer to actually
"reach" them.
In comparison, consider this reducer:
// this reducer always changes the state, regardless of the action!
DO NOT COPY THIS!
const doNotUseThisReducer: ActivityReducers.DataReducer = {
reduce(dataHolders, action) {
return dataHolders?.map(dh =>
TodoActions.addTodo.match(action) ? handleAddTodo(action)(dh) : dh);
}
}
54

-- 54 of 138 --

Because mapping an array always creates a new object reference, this reducer
will create a state change for ALL activity-related actions, even in cases where
the actual DataHolder content will not be touched at all.
Now, none of the Client-internal actions will ever reach their intended reducer,
because all of them are already handled. Registering reducers using this
pattern effectively breaks Client!
Using the Default DataHolder
The reduce() method of the API contains the defaultDataHolder as a third parameter. When
iterating over the existing DataHolders, you can use this to check whether the current DataHolder
is the default one. For example like this:
const addTodoReducer: ActivityReducers.DataReducer = {
reduce(dataHolders, action, defaultDataHolder) {
return TodoActions.addTodo.match(action) ? dataHolders.map(dh => dh ===
defaultDataHolder ? updateDataHolder(dh, action) : dh) : dataHolders;
}
}
In case your setup contains multiple DataReducers that all want to change the default DataHolder,
checking for its existence as shown above will not work, because the first reducer will create a new
object reference for it. In such cases, you will need to perform the check on a lower level (e.g. by
comparing the descriptors).
NOTE The defaultDataHolder parameter will be undefined if no default DataHolder exists
for the activity.
How to Add New DataHolders
The API is capable of creating DataHolders. Additionally, if your setup requires you to add multiple
data holders at the same time, switching from multiple dispatched actions to a single data reducer
might be a performance gain.
Consider the following example:
function addDataHolders(action: Action<CustomTriggerPayload>, dataHolders:
Activity.DataHolder[]) {
// based on the action payload, create new DHs here
const newDataHolders = []
// add them to the existing ones
return [
...dataHolders,
...newDataHolders
]
55

-- 55 of 138 --

}
const customTriggerReducer: ActivityReducers.DataReducer = {
reduce(dataHolders, action) {
return customTriggerAction.match(action) ? addDataHolders(action,
dataHolders) : dataHolders;
}
}
Because a reducer is just a function without side effects, the code can easily be unit-tested.
In case the creation of a DataHolder from inside your existing saga depends on other data from
state, you can either: . select the data beforehand and pass it as payload when dispatching your
trigger action . select the data inside your reducer factory function. Note that this only works for
module-specific DataReducers (added via the Module API).
Accessing models of the current scene
To work with the referenced models of the current activity scene, A12 Client automatically injects
all models of the scene together with the Model Graph into the payloads of activity actions.
Inside your reducer, the models can then be accessed like this:
export const customReducer: ActivityReducers.DataReducer = {
reduce(dataHolders, action) {
// or use AsyncActivityActionPayload
if (ActivityActions.isActivityActionWithModelsInScenePayload(action.payload))
{
const { modelGraph, modelsInScene } = action.payload;
// use modelsInScene
}
return dataHolders;
}
};
In case the scene does not include any models, modelsInScene will be an empty array. Note that
depending on the loading state, each element in modelsInScene can be one of:
• the loaded model
• a model error, when loading was already attempted and failed
• a model descriptor, when the model was not loaded yet (in this case the Model Graph could be
used, e.g. to find references)
56

-- 56 of 138 --

Model Loading
The complete rendering of views in the Client is often based on the availability of certain A12
models: Document Models, Form Models, Overview Models or Tree Models. Those models describe
the behavior of the view and that is why they need to be referenced in the respective Application
Model fragments, i.e. in "VIEW_ADD" directives.
The Client library will load the required models (once) for all active views and also consider the
dependencies between different models in order to also load all implicitly referenced models.
In order to make sure that those models are only used if they are actually compatible with the
application, the Client Model Loader also checks whether the loaded models are version compatible
to the libraries that those models belong to. I.e. tree models must match the model version that the
Tree Engine expects, Form Models must match the model version that the Form Engine version
expects and so on.
This check requires a model type to model version mapping to be available to the Client library.
This can be achieved by injecting a respective mapping object into the bundled javascript during
the bundling step in the build process. One example solution is part of the webpack configuration
of the A12 project template.
Summary
Let’s summarize what we’ve learned so far. Here are the key concepts:
• Regions: the application frame defines two regions: content area and sidebar. Each region
contains views and uses a layout manager to derive which views are visible and how they are
arranged.
• Activity: a user is always engaged in an activity like an overview or editing a form. Activities can
be nested in a possible recursive master/detail manner. The next section explains activities
further.
• Case: an activity can offer multiple cases, e.g., a complex business objects like an insurance
policy can be broken down into different cases like general data, risks, terms&conditions, etc.
• Views: The views of a regions are derived by the activities and the current selected scene and
case (through a mechanism called condition matching, which is explained in the Application
Model section).
57

-- 57 of 138 --

Features
This chapter describes the features of the A12 Client in detail.
Composable Appsetup
In the regular appsetup, you are responsible for combining all the necessary setup code for each
A12 Component with your own setup code and provide the result to A12 Client. This includes for
example reducers, sagas or DataHandlers. You are also expected to call specific function, dispatch
specific actions or wrap your React components with specific contexts. With the new API, each A12
Component is now responsible for their own setup.
Another advantage is the compile-time validation of the config. Each component may declare other
features, that they depend on or that must not exist. The type of the config will become 'never' if
that happens.
NOTE
The older appsetup is still supported and can even be mixed with the composable
appsetup. While we recommend switching A12 based code to the new setup for
easier integration and reduced maintenance effort, your own setup can continue to
use the old config.
When migrating to the new API, remember to remove A12 specific setup code. In
58

-- 58 of 138 --

addition to the code provided to the old appsetup, remove A12 specific React
contexts and view/layout/regionProvider.
Composable Appsetup
// the composable appsetup is build on top of the old config, which continues to work
// you can either use it or the more functional setup described below and keep it
empty
const clientConfig: BaseConfig = {
// your reducers, middlewares, sagas, data handlers, etc.
additionalMiddlewares: [customMiddleware],
// additionally, view/layout/regionProvider can now be specified here
// remember to remove any A12 specific components,
layouts: layoutProvider
};
const initialConfig: A12ApplicationConfig = {
config: clientConfig,
localization: {
supportedLocales: [
{
country: "US",
language: "en",
name: localizableFromLocalizationTreeMap("languages.english",
localizations)
}
]
}
};
const finalConfig = combineFeatures(
// Engines
combineFeatures(withFormEngine, withTreeEngine),
// Model Loading
combineFeatures(withModel(appModel), withPlatformModelLoader),
// Client Extensions
combineFeatures(withLocalization, withDirtyHandling, withNotifications),
// Custom Setup
combineFeatures(addView("MyView", CustomView), addWrapper(CustomContext))
)(initialConfig);
const { store, initialActions, Component } = createA12ApplicationSetup(finalConfig);
await initialActions();
const root = createRoot(document.getElementById("root") as HTMLDivElement);
root.render(<Provider store={store}>{Component}</Provider>);
Client Extensions
A12 Client itself not only provides the composable API, but provides some extensions that can be
59

-- 59 of 138 --

registered with the API. For a description of a specific feature, see the following paragraphs.
Client Extensions
combineFeatures(
withLocalization,
withAppModelAdapter,
withDataServicesConfiguration,
withDeepLinking,
withDirtyHandling,
withPlatformModelLoader,
withNotifications
);
Root Reducer
In most cases, your own reducers will either work on custom top level slices of the state (using the
reducerMap property of the ApplicationSetup) or on certain activities (using Data Reducer).
However, these APIs will not work when wanting to work on the default slices of Client itself. To
achieve this, it is possible to customize the root reducer as a whole: this lets you intercept actions,
add your own logic to default actions, while still being able to delegate to the default reducer if
necessary.
Consider the following example:
Example of a custom root reducer
import type { Reducer } from "redux";
import { ApplicationFactories } from "@com.mgmtp.a12.client/client-core";
declare const otherConfig: ApplicationFactories.Config;
const customRootReducer: (defaultReducer: Reducer<object>) => Reducer<object> =
defaultReducer => (state, action) => {
switch (action.type) {
case "CUSTOM_ACTION": {
// handle custom action here
return { ...state, custom: true };
}
// fall back to the default root reducer to make sure
// the default Client actions are handled properly
default:
return defaultReducer(state, action);
}
};
ApplicationFactories.createApplicationSetup({ ...otherConfig, rootReducer:
60

-- 60 of 138 --

customRootReducer });
Here we define a custom root reducer that handles a specific action ("CUSTOM_ACTION") and falls back
to the default root reducer for all other actions. Note that because our custom action wants to
update the top level of the Client state, using something like a Data Reducer would not be possible
here (since these only work per Activity). This ensures that your custom logic is applied without
affecting the default Client behavior.
NOTE Instead of using a switch statement, you could also use the .match() method
provided by action creators to distinguish actions.
Lenses
Lenses are a functional programming concept adapted here for state management. In the context of
A12 Client, a lens is a plain object with a getter and a setter, allowing you to read and update a
specific part of the redux state in a type-safe and composable way. This is especially useful when
customizing the root reducer, when default Client behavior should be overridden / changed.
Compared to selectors, which only read state, lenses provide both read and write access.
Lenses are available for all slices of the Client state, namely:
Client state slice lens namespace
activities ActivityLenses
application ApplicationLenses
locale LocaleLenses
models ModelLenses
notifications NotificationLenses
NOTE
There is no specific lens for the ApplicationModel inside the models slice. To
programmatically change flows, regions and menus, implement the corresponding
selectors with the DynamicConfiguration API instead.
Basic Example
Consider this example showing how to use a lens to update the locale slice in your root reducer:
Example of a custom root reducer using the LocaleLenses
import type { Reducer } from "redux";
import { LocaleLenses } from "@com.mgmtp.a12.client/client-core";
import { actionCreatorFactory } from "@com.mgmtp.a12.client/typescript-fsa-redux-5-
compat";
import { Locale } from "@com.mgmtp.a12.utils/utils-localization";
// a custom action to set the locale from a string
61

-- 61 of 138 --

export const customLocaleAction = actionCreatorFactory()<{
readonly localeString: string;
}>("CUSTOM_SET_LOCALE");
export const rootReducerWithLocaleLens: (defaultReducer: Reducer<object>) =>
Reducer<object> =
defaultReducer => (state, action) => {
return customLocaleAction.match(action) && state
? // using the locale lens to perform the state update
LocaleLenses.locale.set(Locale.fromString(action.payload.localeString)
as Locale)(state)
: defaultReducer(state, action);
};
Here, we define a custom action to set a locale from a string. When dispatched, the reducer uses the
LocaleLenses.locale() lens to update the default locale slice in the state. Other actions, including all
the default ones from Client, will go through the default reducer, leaving their behavior unchanged.
Complete Example
The following example demonstrates how you can use multiple lenses to handle both custom
actions and default Client actions:
Example of a custom root reducer using the different Lenses from Client
import type { Reducer } from "redux";
import type { Modifier, Notification } from "@com.mgmtp.a12.client/client-core";
import {
ActivityActions,
ActivityLenses,
modifyIfPresent,
modifyWithLens,
NotificationLenses,
type Activity
} from "@com.mgmtp.a12.client/client-core";
import type { Action } from "@com.mgmtp.a12.client/typescript-fsa-redux-5-compat";
import { actionCreatorFactory } from "@com.mgmtp.a12.client/typescript-fsa-redux-5-
compat";
// a custom action to update a notification's duration
export const updateNotificationDuration = actionCreatorFactory()<{
readonly id: string;
readonly duration: number;
}>("UPDATE_NOTIFICATION_DURATION");
export const rootReducerExample: (defaultReducer: Reducer<object>) => Reducer<object>
=
defaultReducer => (state, action) => {
return updateNotificationDuration.match(action) && state
? handleNotificationUpdate(state, action.payload)
62

-- 62 of 138 --

: ActivityActions.push.match(action) && state
? handlePush(state, action, defaultReducer)
: defaultReducer(state, action);
};
const updateDuration =
(duration: number): Modifier<Notification> =>
n => ({ ...n, duration });
// Use NotificationLenses to update the duration of an existing notification
function handleNotificationUpdate(
state: object,
payload: { id: string; duration: number }
): object {
const notificationLens = NotificationLenses.notificationById(payload.id);
const updateIfNotificationPresent =
modifyIfPresent(updateDuration(payload.duration));
return modifyWithLens(notificationLens)(updateIfNotificationPresent)(state);
}
const updateDescriptor: Modifier<Activity> = a => ({
...a,
descriptor: {
...a.descriptor,
hasChild: "yes"
}
});
// Use ActivityLenses to update an activity after another activity was pushed
function handlePush(
state: object,
action: Action<ActivityActions.PushPayload>,
defaultReducer: Reducer<object>
): object {
// first let the default reducer handle the action
const nextState = defaultReducer(state, action);
const parentActivityId = action.payload.activity.initiatingActivityId ?? "";
// then use lenses to update the state some more
const updateIfActivityPresent = modifyIfPresent(updateDescriptor);
return
modifyWithLens(ActivityLenses.activity(parentActivityId))(updateIfActivityPresent)(
nextState
);
}
As seen in the example, you can choose to override the default behavior or delegate to the default
63

-- 63 of 138 --

reducer and then use lenses to further update the state. This flexibility makes the API very
powerful for advanced use cases.
NOTE
When handling default Client actions in your custom root reducer, be careful not to
break the Client behavior. Always make sure to handle these actions properly, or
fallback to the default reducer.
Model Loader
The modelLoader extension provides the functionality to load different kinds of models from
different locations.
We distinguish between loading models directly from the platform server (Platform Model Loading)
or from other locations, e.g. a different server or your local file system (HTTP Model Loading).
Model loading also varies for different kinds of models, e.g. for a Document Model it is also
necessary to load the corresponding validation code.
The extension provides a single generic loader, which can be used to load all kinds of models. For
Document models, the loader will additionally perform post-processing by default (deserializing the
model and adding the validation code).
This model loader is internally customized with different fetching mechanisms to be used for both
HTTP Model Loading and Platform Model Loading.
ModelLoader API
Both extensions provide an API for parallel model loading. The implementation provides a single
generic loader, which can be used to load all kinds of models. Both the fetching and the post-
processing mechanism can be customized.
HTTP Model Loading
With the public factory function createHttpModelLoader you can retrieve the model loader for
HTTP Model Loading, which uses the fetch API for loading. The base path from which the models
should be loaded and the post-processing of models can be configured by passing it to the factory
function. If no base path is given, the root path "/" will be used.
const modelLoader = createHttpModelLoader({ basePath: "/models/", modelProcessors: []
});
Platform Model Loading
Platform Model Loading reuses the model loader from the HTTP Connectors extension, but sets a
different fetching mechanism, using RPC operations for batch-loading models.
Providing a Custom Server Connector
This loader respects the Services API and uses the ConnectorLocator. Therefore, it is possible to
64

-- 64 of 138 --

provide your implementation following the Simple Example API. This can be used to modify the
authentication filters as well as provide a custom server connector.
Simple Example
// Configure your server connector e.g. using UAA
ConnectorLocator.createInstance(serverConnector);
Note about behavior
The ModelLoader API performs parallel model loading. To achieve this, the Model Graph is used to
find all referenced models before the actual loading is started, allowing the loader to load
everything "at once". Therefore, when using the API make sure to initialize the Model Graph on
application startup. This is required as the new API uses the Model Graph to resolve all references.
See the Model Graph and the A12 Services documentation for more information.
ModelProcessor API
The ModelLoader API is designed to load all models in parallel. To be able to handle models
differently depending on their type, the ModelProcessor API can be used.
Usage of ModelProcessor API
export interface ModelProcessor {
/**
* The type of model this processor can handle
*/
readonly modelType: string;
/**
* How to process the model
*
* @param loadedModel The loaded model, also contains validation code for document
models
* @param referencedModels All referenced models from the header
* @returns A post-processed model or an error
*/
processModel(loadedModel: ModelAPI, referencedModels: ModelMap): ModelAPI |
Model.Error;
}
In case your processing logic needs access to the referenced models, they are included as the
second parameter. See the customization section below for additional information.
Implementing a Custom ModelLoader
Fetching models
The actual loading of models can be customized by passing your own implementation of a
ModelFetcher. Note that by default, Client expects a valid ModelGraph to be able to find all
referenced models. When the ModelFetcher is called, Client will pass all of the resolved model
65

-- 65 of 138 --

descriptors at once, allowing the fetcher to perform parallel loading.
As a consequence, using the default ModelLoader implementation by Client requires a ModelGraph
(without it, model loading will break even for a single ui model, since already the referenced
Document Model would not be found).
However, in case setting up a ModelGraph is not wanted or not possible, the API can still be used,
requiring you to write a custom model fetcher instead. Since the fetcher will only get the models in
scene without any resolved dependencies, the logic might look like this:
1. Load all models that are given (in most cases, this will be a single model: the UI model of the
current scene, for example a form)
2. When loading is done, manually extract referenced models from the header
3. For each found model, perform loading again (starting at 1.)
While this approach would allow circumventing the need for a ModelGraph, please note that it also
hinders performance. In case there are a lot of referenced models, loading them like this will
trigger a waterfall of requests, potentially slowing down your application.
Post-processing models
In addition to customizing the model fetching, it is also possible to customize the post-processing of
loaded models with the ModelProcessor API e.g. like this:
Usage of ModelProcessor API
export const customModelProcessor: ModelProcessor = {
modelType: "overview",
processModel(model, referencedModels) {
// any custom logic for a loaded overview model
// for example, accessing and checking the referenced document model
const documentModelRef = model.header.modelReferences?.find(
ref => ref.modelType === "document"
);
const documentModel = referencedModels[documentModelRef?.reference ?? ""];
if (!documentModel) {
return {
type: "POST_PROCESSING_FAILED",
message: "Document model not found"
};
}
// in any case, return either the processed model OR an error describing
why/how the processing failed
return model;
}
};
Both the http and the platform loader factories allow passing a list of `ModelProcessor`s. Since
66

-- 66 of 138 --

Client already includes post-processing by default for Document Models, the following rules apply:
• There is always only one ModelProcessor applied (if there are multiple passed for the same
model type, the first one wins)
• If there exists a custom ModelProcessor for Document Models, it will be applied instead of the
default.
• If no ModelProcessor is passed, Client will always fall back to the default processing logic for
Document Models
If you need to write your own ModelLoader, the default ModelProcessor from Client for Document
Models is available as a convenience.
Customizing the loader itself
You can also implement a custom ModelLoader yourself. To achieve this you first need to
implement the ModelLoader interface as shown in the example below and afterwards register your
custom model loader in the appsetup of your application.
The ModelLoader interface
export interface ModelLoader {
/**
* A unique name to identify the loader
*/
readonly name: string;
/**
* Loads an A12 model together with all of its referenced models.
*
* @param modelDescriptors List of model descriptors to load
* @param existingModels Map of already existing models, useful if post processing
* needs access to models that were not loaded
*/
load(modelDescriptors: Model.Descriptor[], existingModels: ModelMap):
Promise<ModelMap>;
}
Example of a simple ModelLoader implementation
export class CustomModelLoader implements ModelLoader {
readonly name: string = "CustomModelLoader";
protected basePath: string;
constructor(basePath: string = "/") {
this.basePath = basePath;
}
private async loadSingleModel(
descriptor: Model.Descriptor
): Promise<{ name: string; result: ModelAPI | Model.Error }> {
67

-- 67 of 138 --

try {
const response = await fetch(`${this.basePath}${descriptor.name}.json`);
const jsonContent = await response.json();
if (!isModelInstance(jsonContent)) {
throw new Error("not a valid model");
}
return { name: descriptor.name, result: jsonContent };
} catch (error) {
return {
name: descriptor.name,
result: { type: "MY_MODEL_ERROR", source: error }
};
}
}
async load(descriptors: Model.Descriptor[]): Promise<ModelMap> {
// load models in parallel
const results = await Promise.all(
descriptors.map(descriptor => this.loadSingleModel(descriptor))
);
// apply post processing if needed
return results.reduce((modelMap, { name, result }) => {
return { ...modelMap, [name]: result };
}, {} as ModelMap);
}
}
While the examples above can be used as a starting point, please note that this API is designed to
only provide a single loader. Therefore, if you need to customize the processing of specific model
types, you will still need to implement processing for all of them.
Custom Model Loading
Independent of the default model loading of the Client it is possible to handle model loading
yourself. In order to prevent interference with the default model loading only models should be
handled that are not referenced by the Application Model and their transitive dependencies.
Handling the model loading yourself has some implications, because without the model descriptors
in the Application Model no automatic data loading will happen and Engine views are not able to
resolve their models. Therefore, you have to provide your own DataProviders and views.
In the following example you can see a simple custom model loading saga. For more details about
setting models please have a look the ModelActions.setModels action documentation.
Example of a custom model loading saga
export function* loadModels(): SagaGenerator<void> {
68

-- 68 of 138 --

yield* takeEvery(
[ActivityActions.push, ActivityActions.cancel],
function* handlePushAction(action) {
const activity = ActivityActions.push.match(action)
? action.payload.activity
: action.payload.replacementActivity;
if (
activity === undefined ||
!isActivityWithCustomModelLoading(activity) ||
modelsAreLoaded()
) {
return;
}
const documentModel = customDocumentModelLoader();
const formModel = customFormModelLoader();
yield* put(
ModelActions.setModels({
[formModel.header.id]: formModel,
[documentModel.header.id]: documentModel
})
);
}
);
}
Layouts
Layouts define how views are visually arranged inside a Region. The following layouts are provided
by default:
MasterDetail
Arrangement of views side-by-side, where only one or two views are visible at the same time.
Via the VIEW_ADD directive in the Application Model it is possible to add a constraint for the
preferred width of the right view. The view can take from 1 to 11 of the 12 available layout grid
slots, leaving the remaining space for another view visible on the left. If only one view is visible, it
uses all available space.
For more details, see the Widget Showcase.
Dashboard
All views are arranged in tiles which are positioned in rows and columns. Please consult the
Widgets documentation for more details.
69

-- 69 of 138 --

ApplicationFrame
This layout is only intended to be used for the root region of the application. It renders
• a header showing a logo and a title as well as additional header items which can be defined by
the project.
• a sidebar showing the views assigned to the region "SIDEBAR"
• a content part showing the views assigned to the region "CONTENT"
Header
The application frame contains a header which by default shows the title of the application. It can
be adapted by the following properties:
• logoURL: The URL to the image which should be shown as logo.
• additionalHeaderItems: Additional header items, defined by a react component and an
orientation.
Main Menu
The application frame contains the main menu. In order to customize this aspect a custom main
menu component can be provided to the application frame via its mainMenuComponent property.
Additionally, the main menu button, that is rendered in mobile mode, can be customized by using
the ClientWidgetMap.
NOTE
When customizing generic Widgets like a Button, make sure to wrap your custom
context as "close to its usage" as possible, since all instances of a Widget rendered
by Client will always use the closest parent context. In this example, this would
mean that if you were to wrap the whole application with your custom context,
every Button rendered by Client would be customized (which is probably not what
you want).
import { useContext } from "react";
import { ButtonProps } from "@com.mgmtp.a12.widgets/widgets-
core/lib/button/main/button.api";
import { ClientWidgetMapContext } from "@com.mgmtp.a12.client/client-
core/lib/core/ClientWidgetMap.js";
interface PropsType {
readonly mobileMode: boolean;
readonly expanded: boolean;
onMenuItemClick(): void;
}
function CustomMainMenuComponent(props: PropsType): JSX.Element {
return <React.Fragment />;
}
70

-- 70 of 138 --

function CustomMainMenuButtonComponent(props: ButtonProps): JSX.Element {
return <React.Fragment />;
}
const DefaultClientWidgetMap = useContext(ClientWidgetMapContext);
const widgetMap = {
...DefaultClientWidgetMap,
Button: CustomMainMenuButtonComponent
};
<ClientWidgetMapContext.Provider value={widgetMap}>
<FrameViews.ApplicationFrameLayout
{...layoutProps}
mainMenuComponent={CustomMainMenuComponent}
/>
</ClientWidgetMapContext.Provider>
WARNING In order to insure correct behavior it is necessary to call onMenuItemClick on
every menu entry click if a change should occur!
Mobile Navigation
Mobile Navigation is a build-in feature of the ApplicationFrameLayout that is active by default. It
enhances the user experience on mobile devices by using the available screen space in a more
efficient way.
More details are available in Mobile Navigation.
MainMenuContext
On a mobile device the main menu is shown as a sliding menu. It can be toggled by the user via
touching the "hamburger" button shown in the mobile header.
The ApplicationFrameLayout wraps its content into the provider of the MainMenuContext in order to
allow toggling the menu in a programmatic way. This React context provides props to get and set
the current expansion state of the mobile main menu. This way a custom component can use the
context anywhere within the application via its Consumer to toggle the mobile main menu.
A use case is a button in the mobile application header which starts a new activity and at the same
time closes the mobile menu to prevent that the view of the new activity is hidden from the user.
SidebarContext
The ApplicationFrame wraps a React context around the Sidebar region, with which the current
state of the sidebar (collapsed or expanded, minimized or maximized) can be retrieved and set.
Please refer to the API documentation for more information about FrameViews.SidebarContext.
71

-- 71 of 138 --

Layout Settings
The ApplicationModel contains the following ApplicationFrame settings which can be used to
configure the ApplicationFrame:
• disableCollapsingSub: Used to hide the button for triggering the collapsing and expanding of the
sidebar
• initialSubExpanded: Used to set the initial expansion state of the sidebar.
• initialSubExpandedState: Used to set the initial size of the sidebar.
Please refer to the API documentation for more information about
ApplicationModel.ApplicationFrame.Settings.
Stack
Views are "stacked" on top of each other, i.e. only the latest view is visible. Useful for modals.
Null
All views are rendered without any layout next to each other in the DOM. Use if you want to output
all views without any additional markup.
Custom Layouts
You can define your own layouts by returning them in a custom LayoutProvider. Then you can use
them by referencing them in the Application Model.
Use the same approach to define a custom logo, login image, or version. Example:
import type { JSX } from "react";
import { FrameFactories, FrameViews } from "@com.mgmtp.a12.client/client-core";
export const customLayoutProvider: FrameViews.LayoutProvider = name => {
return name === "ApplicationFrame"
? { component: CustomAppFrameLayout }
: FrameFactories.layoutProvider(name);
};
function CustomAppFrameLayout(props: FrameViews.ApplicationFrameLayoutProps):
JSX.Element {
return <FrameViews.ApplicationFrameLayout {...props} logoURL="/images/my_logo.svg"
/>;
}
You could even implement a fully custom layout component, laying out the view components as you
wish. See the following example:
72

-- 72 of 138 --

import type { JSX } from "react";
import type { FrameViews } from "@com.mgmtp.a12.client/client-core";
import { FrameFactories } from "@com.mgmtp.a12.client/client-core";
import { LayoutGrid } from "@com.mgmtp.a12.widgets/widgets-core";
export const customLayoutProvider: FrameViews.LayoutProvider = name => {
return name === "CustomGridLayout"
? { component: CustomGridLayout }
: FrameFactories.layoutProvider(name);
};
function CustomGridLayout(props: FrameViews.LayoutProps): JSX.Element {
const viewComponents = props.views.map(view => ({
ViewComponent: props.viewProvider(view.name),
view
}));
return (
<LayoutGrid.Grid>
<LayoutGrid.Row>
{viewComponents.map(({ ViewComponent, view }, index) => (
<ViewComponent key={`${view.name}-${view.activityId}-${index}`}
{...view} />
))}
</LayoutGrid.Row>
</LayoutGrid.Grid>
);
}
Progress Indication
Every layout renders a progress indicating React component wrapping each view component. This
component is provided by a Progress Component Provider.
The default provider can be obtained by using FrameFactories.createProgressComponentProvider(). It
delivers a ProgressIndicator widget showing a progress spinner if the flag "busy" of the default
DataHolder of the related activity is true.
If a different behavior is desired, it is always possible to provide your own custom progress
component provider.
Conditionally Hide Progress Indicator
To conditionally show or hide the progress indicator from inside the view component, set
handleProgressIndicator on the view component. If this flag is set to true, the progress component
will be passed to your view component, where you can show or hide it based on your custom
condition. Example:
73

-- 73 of 138 --

For functional components
function ConditionalProgressComponent(props: View): JSX.Element {
const ViewContent = <div>COMPLEX VIEW COMPONENT</div>;
return props.ProgressComponent && shouldRender() ? (
<props.ProgressComponent
activityId={props.activityId}>{ViewContent}</props.ProgressComponent>
) : (
ViewContent
);
}
ConditionalProgressComponent.handleProgressIndicator = true;
For class components
class ConditionalProgressComponentClass extends Component<View> {
static handleProgressIndicator = true;
render(): ReactNode {
const ViewContent = <div>COMPLEX VIEW COMPONENT</div>;
return this.props.ProgressComponent && shouldRender() ? (
<this.props.ProgressComponent activityId={this.props.activityId}>
{ViewContent}
</this.props.ProgressComponent>
) : (
ViewContent
);
}
}
Here, the view component provider will set handleProgressIndicator only for the
ConditionalProgress components, while keeping the default behavior for all other components.
NOTE
In order to achieve this behavior when using your own custom layout, you need
check for this flag and either wrap your ViewComponent inside of the
ProgressComponent (if false or not set) or pass it to the ViewComponent as the prop
ProgressComponent
Error Boundaries
Every layout renders an error boundary component wrapping each view component. Error
boundaries are React components that catch JavaScript errors anywhere in their child component
tree, log those errors, and display a fallback UI instead of the component tree that crashed. Error
boundaries catch errors during rendering, in lifecycle methods, and in constructors of the whole
tree below them.
74

-- 74 of 138 --

The error boundary component is provided by an Error Boundary Provider. The default provider
FrameFactories.errorBoundaryProvider delivers an ErrorBoundary with a default fallback component.
This fallback shows a component with detailed information about the error and a button to cancel
the current activity.
The provider mechanism allows to replace the default error boundary component for specific
views or regions, e.g.:
Using a custom error boundary provider
export function MainComponent(): JSX.Element {
const RegionUi = FrameFactories.regionUiProvider([]);
// other props here...
const regionUiProps = {} as FrameViews.RegionUiProps;
return <RegionUi {...regionUiProps}
errorBoundaryProvider={CustomErrorBoundaryProvider} />;
}
To prevent re-renderings of your view components, it is essential that your provider always
returns a stable function reference like this:
Using a custom error boundary
import type { ComponentType, JSX } from "react";
import { Component } from "react";
import type { ApplicationModel, FrameViews, View } from "@com.mgmtp.a12.client/client-
core";
import { FrameFactories } from "@com.mgmtp.a12.client/client-core";
class SpecificErrorBoundary extends Component<View.ErrorBoundaryProvider.PropsType> {
state: { error: Error | null } = {
error: null
};
componentDidCatch(error: Error): void {
this.setState({ error });
}
render() {
const { error } = this.state;
if (error !== null) {
return <div>Specific error text for ProductView</div>;
}
return this.props.children;
}
}
75

-- 75 of 138 --

function CustomErrorBoundaryProvider(
regionReference: ApplicationModel.Region.Reference,
viewName: string
): ComponentType<View.ErrorBoundaryProvider.PropsType> {
const DefaultErrorBoundary = FrameFactories.errorBoundaryProvider();
return viewName === "ProductView" ? SpecificErrorBoundary : DefaultErrorBoundary;
}
Here, the custom error boundary rendering a fallback text is used only for a specific
ViewComponent. In all other cases, the default error boundary is used. Because the error boundary
is defined outside of the provider, its reference will not change during reacts lifecycle.
Resizing
It is possible to enable resizing for the following built-in layouts via customization:
• ApplicationFrameLayout: horizontal resizing of the sidebar
• MasterDetailRegionLayout: horizontal resizing of the left view
The customization is enabled via the widget map approach:
The Client layout components allow optionally providing alternative layout widget components via
a widget map. The provided custom widgets can be setup to be resizable using the resizing api of
the respective layout widget. Please consult the Widgets documentation for more details.
The following example shows how to achieve this in a Client based application. A custom layout
name is defined in the Application Model. A custom layout provider returns a custom layout
component for that name. In this component, the widgetMap property is defined and provides a
respective custom, resize-enabled layout widget.
import { useContext, type JSX } from "react";
import {
ClientWidgetMapContext,
FrameFactories,
FrameViews
} from "@com.mgmtp.a12.client/client-core";
import {
MasterDetail,
type MasterDetailProps,
type VisibleView
} from "@com.mgmtp.a12.widgets/widgets-core";
export const customLayoutProvider: FrameViews.LayoutProvider = name => {
return name === "MyResizableMasterDetailLayout"
? { component: CustomResizableMasterDetailLayout }
: FrameFactories.layoutProvider(name);
76

-- 76 of 138 --

};
const resizeOptions: VisibleView["resizableOptions"] = {
minWidth: 200,
maxWidth: "75%"
};
function ResizableMasterDetail(props: MasterDetailProps): JSX.Element {
return <MasterDetail {...props} firstViewResizableOptions={resizeOptions} />;
}
export function CustomResizableMasterDetailLayout(props: FrameViews.LayoutProps):
JSX.Element {
const DefaultClientWidgetMap = useContext(ClientWidgetMapContext);
const customWidgetMap = {
...DefaultClientWidgetMap,
MasterDetail: ResizableMasterDetail
};
return (
<ClientWidgetMapContext.Provider value={customWidgetMap}>
<FrameViews.MasterDetailRegionLayout {...props} />
</ClientWidgetMapContext.Provider>
);
}
ClientWidgetMap
The ClientWidgetMap can be used to customize the Widgets that are rendered by A12 Client itself.
Client internally uses a default variant of this map, but you can provide your own implementation
containing your customizations via React Context. Then it will be used in place of the default one.
For example, customizing the way the MasterDetailRegionLayout component renders its
MasterDetail Widget can look like this:
import type { JSX } from "react";
import { useContext } from "react";
import { ClientWidgetMapContext, FrameViews } from "@com.mgmtp.a12.client/client-
core";
import { type MasterDetailProps, MasterDetail } from "@com.mgmtp.a12.widgets/widgets-
core";
function CustomMasterDetail(props: MasterDetailProps): JSX.Element {
return <MasterDetail {...props} /* customization here*/ />;
}
export function CustomResizableMasterDetailLayout(props: FrameViews.LayoutProps):
77

-- 77 of 138 --

JSX.Element {
const DefaultClientWidgetMap = useContext(ClientWidgetMapContext);
const customWidgetMap = {
...DefaultClientWidgetMap,
MasterDetail: CustomMasterDetail
};
return (
<ClientWidgetMapContext.Provider value={customWidgetMap}>
<FrameViews.MasterDetailRegionLayout {...props} />
</ClientWidgetMapContext.Provider>
);
}
Here, we first get the default Widget implementation from the context and then add our own
customization, specifically for the MasterDetail Widget.
Note that this map will only customize A12 Widgets rendered by Client itself, not the ones rendered
by your application (e.g. your own view components).
Localization
Localization in A12 is done by using the Localizer, which is accessible from a LocalizerContext react
context. This react context has to wrap all the react components of the Client application that need
to localize any texts. The Client library does not create this context itself. The application code has
to add it to the react component tree itself. It is suggested to add this context as one of the
outermost react components of the application.
Current Locale
The current locale is part of the application state and as such managed in the Redux store. Defining
an initial locale of the application is done by dispatching the LocaleActions.set action with the
wanted locale during the setup phase. This can be achieved by adding this action to
ApplicationFactories.createApplicationSetup.
Available Locales
A12 Client provides a ready-to-use component LocaleSelect to update the locale. The available
locales need to be set as a property on that component. Note that an initial locale set via application
setup also needs to be part of that list.
Localizer
The localizer is defined by the A12 localization API and represents the core of it. Please refer the
documentation of the @com.mgmtp.a12.utils/utils-localization library for more in-depth
information.
export interface Localizer {
78

-- 78 of 138 --

(...localizable: Localizable[]): string | undefined;
}
All A12-related localizations are covered by the default texts that the Localizable objects can
contain. I.e. any localizable that is created by A12-internal logic will always bring the default
translations for the English and German locale. Additional texts to override or extend the A12-
default texts can be passed to e.g. the defaultLocalizerFactory when creating an application-specific
Localizer function. Please refer to documentation of the @com.mgmtp.a12.utils/utils-localization
library for more details on how this can be achieved.
Localization Keys
The main identification aspect of localizables are the localization keys. Every Localizable that is
provided to a localizer must have a localization key! How the different localization keys are
generated depends on their producer. Therefore, it may be necessary to read the documentation of
other components like Form Engine and Overview Engine.
Static Resources
For static resources the keys are provided by the constant LOCALE_RESOURCE_KEYS which is located in
core/locale. This constant provides the keys as well as the documentation of their usage. For
extensions similar constants exist and are provided by the extensions. Please consult the
documentation of the extensions for more details.
Application Model
Please note that you can also provide localization texts directly in the Application Model. Every
entry is localizable by providing different texts in a map. The key is hereby the string
representation of the Locale object.
Menu Entries
The key of each menu entry has the pattern application-model.{application-model-name}.{module-
name}.{menu-name}\(.{sub-menu-name})* .
Cases
The key of each case has the pattern application-model.{application-model-name}.{module-
name}.{flow-name}.{scene-name}.{case-name}.
Single Locale Setup
In order to setup the Client with a single locale that is not the default en_US, you have to set the
current locale in the Redux store to the desired locale, e.g. fr_FR.
WARNING
The Client only provides default texts and configurations for en and de. For
every other locale it is necessary to provide the necessary texts and
configurations. This can be achieved by providing the texts in the models
(form, overview, document, and application) or by using the A12 localization
79

-- 79 of 138 --

API.
Hidden Accessibility Texts
For the localization of hidden accessibility texts, we rely on the Widgets. In order to provide your
custom localization you have to use the A11YLanguageContext component, which is provided by
Widgets. This component should be used next to the Provider component from React-Redux. Please
see the accessibility section in the Widgets documentation for more details.
Modularization
The ModuleRegistry is an API to enable modularization of Client projects. The API offers extension
points to implement dynamic modules in the Client Redux context. This documentation shows what
a modularized Client project can look like.
Motivation
Modularization is a common requirement in web projects regarding different aspects:
• Architecture
◦ Clear dependency management
◦ Clear relation between frontend and backend code
◦ Loose coupling of BE modules
◦ API for portal / cross-functionality
• Infrastructure
◦ Possibility to separately release a module (not by default)
◦ Possibility to separately deploy a module (not by default)
• Organization
◦ Modules can be developed by another supplier
◦ Modules can be easily switched off or de-activated
Different but related aspects are:
• Keep JS modules in the browser cache for offline usage
• Partial loading of modules - modules which are not used are not loaded
• Delta updates in production in order to limit bandwidth
ModuleRegistry
To be able to build up a single page Client application by modules dynamically, the base or core
application itself is not allowed to know the concrete modules. The modules have to register
themselves to any part of the architecture they want to participate in.
Therefore, the Client architecture provides a central ModuleRegistry to add, remove or retrieve
80

-- 80 of 138 --

modules. More information about the API can be found in the respective part of the API
documentation.
To implement a module the API offers different extension points like reducers, views, sagas, etc.
The Client uses the OSGi whiteboard pattern to integrate the modules: The extension points use a
provider to ask all the modules for specific functions. To enable dynamic implementations of these
interfaces, access to the Redux state is needed.
Modules have to return an unique id. Everything else is optional.
NOTE
Module definitions should not interfere and interact with each other to prevent any
execution order issue because the execution order is not ensured. This especially
applies to fallback solutions for DataProviders, DataEditors and DataReducers
because they prevent the executions of others that are later in the execution chain.
Therefore, it is highly recommended that every fallback logic for preventing fatal
error should be done in the global definition.
The ModuleRegistry can be accessed by the ModuleRegistryProvider object. The
ModuleRegistryProvider also offers the getViewProvider function which can be used in a RegionUi to
set the viewProvider as this an extension point outside the Client core.
Using the API
To register a module we first need to implement the Module interface
export const myModule: Module = {
id: "my-module",
model(/* state: object */): ApplicationModel {
return myModuleAppModel;
},
views(/* state: object */): (componentName: string) => ComponentType<View> |
undefined {
return MyModuleFactories.createRenderer;
},
middlewares(/* state: object */): Middleware[] {
return [MyModuleFactories.createCRUDMiddleware()];
},
reducersMap(/* state: object */): ReducerMap {
return {
slice1: MyModuleReducers.slice1,
slice2: combineReducers({
slice2_1: MyModuleReducers.slice2_1
// ...
})
};
81

-- 81 of 138 --

},
sagas(/* state: object */): (() => SagaGenerator<void>)[] {
return [];
}
};
Then the module can be registered before or after bootstrapping the Client Application by
ApplicationSetup.
// Load the moduleAppModel before
ModuleRegistryProvider.getInstance().addModule(myModule);
const model: ApplicationModel = {
header: {
id: "base-application-model",
locales: [{ code: "en" }],
modelType: "application",
modelVersion: "4.0.0"
},
content: {
region: {
name: "APP",
layout: { name: "ApplicationFrame" },
subRegions: [{ name: "CONTENT", layout: { name: "MasterDetail" } }]
},
defaultRegion: ["CONTENT"],
modules: [
/* Add your flows and scenes here */
]
}
};
export const config: ApplicationSetup = ApplicationFactories.createApplicationSetup({
model,
modelLoader: emptyModelLoader,
dataHandlers: emptyDataHub,
additionalMiddlewares: []
});
As you can see we still need to define the Application Model, the modules model will be added at
runtime. Special care must be taken when removing modules at runtime and it should be avoided,
if possible. If there is an active View in a module, removing the module will lead to an Error.
If you remove a module at runtime you are responsible for consistency. For example you may have
to stop running sagas or middleware that are doing processing in the background, or you have to
remove state from the store.
NOTE Modules are not allowed to set own reducers for the following slices because they
82

-- 82 of 138 --

are handled internally:
• activities
• locale
• models
• application
• notifications
Implementing Custom Extension Points
The given extension points may not fit your needs completely, but with the API it is quite simple to
implement new ones on top of it.
In the example we see a MyAbstractModule which is the base class for every project module that
offers the new extension getModuleSpecificRenderer. The getMyModuleExtensionsRenderer function
provides a renderer to render a central component that includes module specific parts:
export abstract class MyAbstractModule implements Module {
id = "myAbstractModule";
// ...
moduleSpecificRenderer(): (props: ModuleSpecificProps) => JSX.Element | undefined
{
return () => undefined;
}
}
export function getMyModuleExtensionsRenderer(): (props: ModuleSpecificProps) =>
JSX.Element {
return props => {
for (const module of ModuleRegistryProvider.getInstance().getAllModules()) {
if (module instanceof MyAbstractModule) {
const result = module.moduleSpecificRenderer()(props);
if (result) {
return result;
}
}
}
return <div>{/* ... default View */}</div>;
};
}
Modules and the Main Menu
The default main menu of the Client creates one menu item per (Application Model) menu, and the
order of the menu items is the same as the appearance / order of registration in the Application
83

-- 83 of 138 --

Model. When using Modularization, this might not fit your needs. In this case, please integrate a
custom menu component using the App Frame’s mainMenuComponent API.
NOTE When using the adapter module with multiple Application Models (see below), the
menu order is dependent on the order of the models in the Model Graph.
Adapter Module
Client provides an Adapter Module to handle multiple Application Models. This module can be
registered like any other Client module and will collect all Application Models in your model graph,
load them and make them available. When using the adapter module, specifying an Application
Model in your application setup becomes obsolete (because Application Models from the Model
Graph will be collected anyway). Since the typing still requires you to set the property, Client
provides a placeholder that can be used in that case. Please see the snippet below for an example.
import {
AppModelAdapterModule,
ApplicationFactories,
ModuleRegistryProvider,
APPLICATION_MODEL_PLACEHOLDER
} from "@com.mgmtp.a12.client/client-core";
import { createPlatformServerModelLoader } from "@com.mgmtp.a12.client/client-
core/modelLoader";
// register the adapter module like other Client modules
ModuleRegistryProvider.getInstance().addModule(AppModelAdapterModule);
ApplicationFactories.createApplicationSetup({
// this placeholder is just a completely empty application model
model: APPLICATION_MODEL_PLACEHOLDER,
modelLoader: createPlatformServerModelLoader(),
dataHandlers: [],
additionalMiddlewares: []
});
// NOTE: do not forget to setup up your modelGraph
Note that while the adapter allows to provide multiple Application Models, in most use cases a
single Application Model should be sufficient. In this case you can still specify it in the setup
directly. Changing an existing setup by splitting the Application Model into multiple ones is not
recommended and might lead to unwanted effects (see below).
NOTE
Using this adapter module requires a Model Graph that contains all Application
Models you want to load. See the Model Graph and the A12 Services documentation
for more information about it.
84

-- 84 of 138 --

Merging Application Models
When matching scenes, Client will internally consider all Application Models that were loaded this
way and merge them together into one.
NOTE
When merging regions of Application Models, only the subregions are taken into
account. If a Region (identified by name) exists both in source and target model, the
subregions of source are added, while all other properties from the source are
ignored.
Effects of Having Multiple Application Models
• Having more than one Application Model with an initial activity is not supported and will lead
to unexpected results. There should always only be a single initial activity.
• When multiple Application Models contribute menu entries, their order is defined by the Model
Graph (as this is used to find and load the models from the server).
Dirty Handling
This extension consists of a Dirty Handling saga and a VetoDialog, which can be wrapped around
the application.
The saga can be used instead of the default cancelRequestedHandling saga, to handle the
CANCEL_REQUESTED action.
The CANCEL_REQUESTED action contains in its payload an array of activity ids and will cancel
these activities if they are not dirty. For evaluating the dirty state the saga takes all DataHolders of
the activity into account. An activity is dirty if any DataHolder is dirty.
If an activity is dirty, a SET_CANCEL_CONFIRMATION_REQUIRED action with
cancelConfirmationRequired=true is dispatched. The saga then waits until a
RESPONSE_CANCEL_CONFIRMATION action is dispatched. This action contains in its payload the
information about if there was a veto for the cancellation. If there was no veto the activity gets
cancelled by the dirty handling saga. If there was a veto the activity does not get cancelled and the
whole cancelling process gets aborted.
If all activities got cancelled the Dirty Handling saga dispatches in the end the
RESPONSE_CANCEL_REQUESTED action which contains the payload "cancelled=true". If not all
activities got cancelled, because there was a veto, the saga dispatches the
RESPONSE_CANCEL_REQUESTED action which contains the payload "cancelled=false". The caller
can use the ActivitySaga waitForResponseCancelRequested to wait for the response of the
cancelProcess.
Example usage:
const activityIdsWhichShouldBeCancelled = ["A", "B"];
yield put(ActivityActions.cancelRequested({activityIds:
85

-- 85 of 138 --

activityIdsWhichShouldBeCancelled }));
// wait for the process to finish and take the response
const cancelled: boolean = yield call(ActivitySagas.waitForResponseCancelRequested);
if(cancelled){
// all activities where cancelled
} else {
// not all activities where cancelled
}
If the VetoDialog is used, it will listen to any state change and show a dialog, if there is an activity
with "cancelConfirmationRequired=true" . The dialog offers two options: "Discard changes" and
"Abort".
• The first option dispatches a RESPONSE_CANCEL_CONFIRMATION action with veto=false.
• The second option dispatches a RESPONSE_CANCEL_CONFIRMATION action with veto=true.
If you do not use the VetoDialog, you have to take care of dispatching a
RESPONSE_CANCEL_CONFIRMATION action, if there are activities which need a
cancelConfirmation, yourself.
Notifications
The core/notification API can be used to notify the user about arbitrary events.
In order to visualize notifications, it is mandatory to wrap the application’s root component with a
suitable React component to visualize notifications. The core/notification API provides a default
frame component. Without such a component no notification is shown to the user.
Notifications can be created by dispatching the notification action "add" and removed again by the
corresponding "remove" action.
Notifications can be bound to activities by providing the corresponding activity id to the
notification. Activity bounded notifications will be removed automatically if the bounded activity is
removed by either committing or canceling. If no activity id is provided then the notification is
treated as a global notification and will not be removed on removing any activity.
Currently, notifications by default are shown as toast messages following the function and design of
the A12 Toast Widget. They can be customized in their message, position, duration, color, and other
options via the properties of the notification object, that needs to be given to the "add" action as
payload.
It is also possible to create completely custom notifications by providing the notification frame with
a notification renderer, that would return the custom notification view.
Mobile
On mobile the position is not customizable due to the small available space. Therefore, only the
86

-- 86 of 138 --

newest notification is shown on the bottom of the screen.
Deep Linking
The deep linking extension helps to implement URL based (activity) state restoration, e.g. when a
user wants to share a deep link into the application. Whenever a (relevant) state changes, the
encoder is called to update the location part of the URL. When loading the application, the decoder
is called to restore activity state based on the URL location.
To register this extension you need to call DeepLinkingFactories.createSagas and pass the returned
sagas to the customSagas property of the ApplicationFactories.createApplicationSetup function.
Default Codec
The extension includes a default codec, that uses the descriptor of the latest activity. This means
that other state of the activity or even other activities will not be recovered, so the scene might not
look the same as at the time of encoding.
Setup
URL Encoding
Registering the sagas is already enough to activate the encoding of the last started activity into the
URL with the default codec.
Activity Restoration
To restore the activity encoded in the URL when the app is loading, you need to setup an action
which serves as a trigger for the restoration to be started.
First, you have to dispatch this action after the initialization code of the app was executed. This
could be e.g. before the first render of the app or after the user logged in successfully.
Second, the action also needs to be added to the array of apply triggers in the deep linking sagas
configuration. You can hand in the configuration object to the DeepLinkingFactories.createSagas
factory function in your app setup.
Configuration
To further customize this extension you can pass more options to the
DeepLinkingFactories.createSagas function via the configuration object. This configuration
contains:
• updateTriggers: A list of actions for which the deep link should be updated
• applyTriggers: A list of actions for which the deep link should be applied
If no action is given in applyTriggers the deep link will never be applied. If the Activity encoded
in the URL is not found, the default page will be loaded if configured (see Welcome Page).
87

-- 87 of 138 --

• clearTriggers: A list of action for which the deep link should be cleared
• loadingTimeout: The time that is waited for the data of a data source activity to be loaded.
When it expires and the data is not loaded, the to-be-restored activity will not be created.
Customization
There are other properties in the configuration object which can be used to replace default
implementations:
• deepLinkCoder: Users can provide their own implementation of DeepLinkCoder, which decides,
what information is encoded in the URL, and how this is done. The default implementation only
uses information stored in the ActivityDescriptor.
• locationManager: A custom location manager can be provided which allows customizing the
handling of the location part of the url. The default implementation only manages setting the '#'
character before the location part.
Limitations
NOTE This feature is not meant to provide full URL-based navigation through the
application (Routing).
By default, only the latest Activity is restored, regardless of how many Activities were active when
the link was created.
Additionally, the URL changes are not saved in the browser history and therefore it is not possible
to navigate to a previous or later Activity with the browser navigation buttons.
If you want to encode more Activities or even implement a history-based navigation yourself, a
good custom implementation can be found in Discourse.
It is not sufficient to just change the link in the address bar of a web browser. The link needs to be
opened.
Deep links currently do not work when used in combination with the priorScene property of Scenes
(see Arrange UIs).
Welcome Page
To set a default starting page ("welcome page"), you need to set the property initialActivity in
your Application Model. This contains the descriptor of the activity that will open as your
starting/welcome activity. Additionally, you need to configure at least one action which indicates
that this activity should be loaded. To do that, you have two possibilities:
If you already configured the deep-linking extension as described above, the saga that listens to
applyTriggers will load the activity described by the initialActivity property of your App Model,
when one of these actions gets dispatched and when the current url does not contain a deep link.
If the App Model does not contain an initialActivity property and the current url does not contain
a deep link, the saga will do nothing.
88

-- 88 of 138 --

If you don’t want to use the deep-linking functionality, you need to call
DeepLinkingFactories.loadWelcomePage and pass its returned saga to the customSagas property of the
ApplicationFactories.createApplicationSetup function. Then you need to configure it by adding
your action/actions to the applyTriggers list, in the same way as described above.
The saga from DeepLinkingFactories.loadWelcomePage will listen for your configured actions and
create the activity described by the initialActivity property of your App Model.
If no initialActivity is set in the App Model, the saga will do nothing.
Finally, to actually show the view component of your welcome page, you need to define a scene in
your Application Model matching the descriptor of the initialActivity with its match conditions
and which has a VIEW_ADD directive with your welcome page view.
NOTE
When using the default ModelLoader implementation of Client and your initial
activity wants to load models, make sure to use a trigger action that is dispatched
after the Model Graph was set. Since by default Client uses the Model Graph to
resolve model dependencies, triggering model loading before the graph exists will
fail.
Modularization
If no initialActivity is set in the root App Model and you add Application Models using the
ModelActions.addModulesApplicationModels action then the initial activity is taken from the first App
Model defining one which is merged into the root App Model.
Model Graph
The Model Graph provides information about Models during runtime.
The following Client features make use of the Model Graph and therefore require you to load and
set it:
Feature Reason
Heterogeneity lookup of supertype relations
Model loading (when using the default
ModelLoader implementation provided by
Client.[4])
resolving model references
You can load the Model Graph yourself from any place, but most probably you want to use the
respective A12 Services API to load it from an A12 Server. A simple and safe occasion is
immediately after the user logged in. Please refer to the Services documentation for more
information.
You can set the Model Graph using ModelActions.setModelGraph, e.g. like this:
89

-- 89 of 138 --

How to set a ModelGraph
// create your appsetup
const config = ApplicationFactories.createApplicationSetup({
model: model as ApplicationModel,
modelLoader: createPlatformServerModelLoader(),
dataHandlers: []
});
// ...later (e.g. after a user logged in)
configureModelGraph(config);
async function configureModelGraph(config: ApplicationSetup) {
// assuming a configured server connector
const serverConnector =
ConnectorLocator.getInstance().getServerConnector() as RestServerConnector;
// fetch the graph in some way
const modelGraph = await serverConnector.fetchData(ModelGraph.build(true)).then(r
=> r.json());
// set it into state
config.store.dispatch(ModelActions.setModelGraph(modelGraph));
}
Until you set the Model Graph, an empty Model Graph will be assumed.
Heterogeneity
Heterogeneity is an experimental feature in A12 that allows the handling of sets of Documents of
different types.
First, the involved parts are described. Then it is shown how to create a setup using the commerce
sample domain.
In the following, the terms Type and Document Model are used synonymously. However, they serve
different purposes. Document Model is the well-known way in which the inner structure of
Documents are defined. Type is a new way to describe Dependencies between entities without
caring about their inner structure. Types "live" inside the Model Graph.
Subtype Dependency
The Subtype Dependency declares an "is a" relationship from one Document Model to another. The
intention is to instantiate and process Documents which have an "is a" relationship in a uniform
way.
NOTE
The Subtype Dependency only declares the relationship. It does not imply or even
require inheritance of fields/groups/rules. If your business logic requires
inheritance, then you have to ensure it yourself, e.g. by using includes or copying
90

-- 90 of 138 --

fields/groups/rules.
You declare a Subtype Dependency by adding an Annotation with name subTypes to the metadata
section of the supertype. The value of the Annotation is a comma separated list of the names of the
subtypes.
The following concepts are not implemented in the current version:
• Multiple Inheritance
• Metadata on subtypes (e.g. to filter subtypes)
Model Graph Loading
With the introduction of heterogeneity, every Client holds a Model Graph inside its state.
To use heterogeneity, you must load and set the Model Graph. See the Model Graph documentation
for an example.
Heterogeneous Overviews
To list a heterogeneous set of Documents, you can assign the common super type as the Document
Model of the Overview Model. The A12 Server will then take all Documents of that super type and
all sub types into account.
Creating New Documents
In heterogeneous overview, newly created Documents can be of different types.
The CRUD extension detects that the type associated with the Overview Model has sub types. It then
displays a dialog to select the type of the new Document. The offered types are the same as those
used for heterogeneous overviews.
Document Model-Dependent Forms
When editing a Document from a heterogeneous set, you probably want to define a single
Application Model Scene, but still use different forms for Documents of different types.
In order to achieve this, you need to limit the usage of a Form Model to a specific Document type.
This can be done by adding a Document Model constraint to the referenced Form Model in the
respective Scene of the Application Model.
Limitations:
• For each Document type, you must provide a Form Model entry, except if all Document types
should share the same Form Model - then you must only provide exactly one Form Model
without Document Model.
• All declared Form Models are loaded on the first usage of the Scene. This can be slow the first
time if you have many Sub Types.
91

-- 91 of 138 --

Example Setup (Simple CRUD)
The following section describes the necessary changes to make use of heterogeneity in the A12
sample domain "commerce". You can find the full, working setup in the Relationships example of
the Client DevApp.
It is a simple setup that makes use of the CRUD component, which contains pre-configured versions
of the heterogeneity building blocks.
NOTE You can find all models in the source code of the Client DevApp in the folder
core/resources/models/.
Modeling
• In the Document Model DomainProduct, set DomainBundle as subtype.
• Setup a Scene using the Overview Model Product-overview and the view OverviewCRUD.
• Setup a Scene using the Form Models Product and Brand (constrained to the respective type) and
the view RelationshipFormEngine.
Application Setup
• Set the Model Graph. See Model Graph documentation on how to do it.
Example Setup (Detailed)
In order to tailor the heterogeneity functionality to project specific needs, you can combine the
provided building blocks, or replace them with your own implementation.
All modeling in Document or Application Models is done exactly in the same way as described in
the CRUD example above. Therefore, here, only the coding API is explained.
The currently available building blocks are:
• Variant selection mapper: a function that maps given types to a tree of selection options
• Variant selection UI: a component to render an UI to select a type for given selection options
NOTE
You can find the fully working example in the source code of the Client DevApp in
the folder core/src/devApp/src/modules/data_handling/heterogeneity of the Client
repository.
Variant Selection Mapper
The provided default variant selection mapper is a function that accepts a single root type and
creates a tree of selection options from it.
The root type defines, which part of the type hierarchy you want to render.
The resulting tree defines the available options, if they are abstract or concrete, as well as their
nesting, order, and labels.
92

-- 92 of 138 --

You can pass the result of the mapping to the default variant selection React component to render a
selection UI.
The provided callback of the default selection UI will be invoked with the name of the selected type,
which can then be used to e.g. start a new Activity.
WARNING
When starting a new Activity for a heterogeneous Scene containing Engines,
make sure to set the model property in the Descriptor. The Client needs it to
pick the right engine models.
const localizer = useContext(LocalizerContext).localizer;
const myStartingType = modelGraph.documentModels.find(dm => dm.modelId === "MyModel");
if (!myStartingType) {
throw new Error("Model graph incomplete!");
}
const options = defaultVariantSelectionMapper(modelGraph, localizer)(myStartingType);
return <VariantSelection variants={options} onSelect={handleSelection} />;
In order to change the enablement, nesting, order or labels of types, you can replace/compose the
default mapper.
In order to change the rendering, you can replace/compose the default React component.
Accessibility Support
The Client allows creating applications with accessibility support.
To make the application accessible for a user who uses accessibility tools like a screen reader, the
application requires a clear hierarchical structure which also is reflected in the application’s HTML.
The user interface of a Client application is structured using regions which are configured with
layouts to display views and other UI components like menus and headers.
The hierarchical structure within the UI and the underlying HTML is established by using
unambiguous HTML tags like nav for navigation components and by providing ARIA levels and the
role "heading" to the headings and specific roles to other building blocks of those UI components.
ARIA in Built-in Layouts
ApplicationFrameLayout
The ApplicationFrameLayout is typically used in the application’s root region to give the
application its base structure. It provides an application header and a main menu and it allows to
orchestrate the content for a main content region, a sidebar region, and a modal region.
93

-- 93 of 138 --

The following aria configuration is applied:
• the application header has the role "banner"
• the main menu is build with a nav-tag as root HTML element, marking it as a navigation for a
screen reader
• the main content view has the role "main", marking it as the application’s main content
• the side bar usually contains project specific custom content, making it a project responsibility
to define an appropriate aria structure using nav-tags for a navigation, aria-levels for headings
and suitable aria roles.
When the case feature of the Client is used and a navigation to the different cases is displayed in
the side bar, a nav-tag is used.
• The modal region content again requires project specific configuration.
DashboardLayout
The DashboardLayout renders its views as tiles in a grid.
The aria structure is established by
• rendering a hidden title with aria-level 1 and role "heading" above the tiles
• handing aria-level 2 to the views in the tiles, so that the views can use this level for their
headings.
MasterDetailLayout
The MasterDetailLayout allows rendering a list of views, with each view depending on the previous
one which creates a hierarchy of views.
Therefore it hands aria-levels in ascending order starting with 1 to its visible views, so that the
headings of these views can reflect that hierarchy.
StackRegionLayout
The StackRegionLayout can manage a list of views but will always only render the last added one.
This view will receive aria-level 1 for its heading.
ARIA in Built-in Components
Engines
The Engines provide views which render a ContentBox Widget with a title. The title should be set
for good accessibility. It can be defined in the respective UI model. The title will receive the aria-
level determined for the view by the layout.
Further details about accessibility support of the Engines can be found in the respective engine
documentation.
94

-- 94 of 138 --

ErrorBoundary
When an error is thrown during rendering of a view, the built-in layouts render the fallback
component of the provided ErrorBoundary. It receives the same aria-level as the view, so that it can
be set to the fallback component’s title.
ARIA in Custom Layouts and Views
When you are creating custom layouts and views and have to fulfill the requirement of good
accessibility, we have the following recommendations:
Custom Layouts
• The layout should hand aria-levels to its visible views which resemble the hierarchy in which
these views stand to each other. Views having the same level in the application hierarchy
should get the same aria-level. A view depending on a parent view should get a higher aria-level
than that of the parent.
• If the layout wraps its views in ErrorBoundary components, those should get the same aria-level
as the wrapped view.
Custom Views
• The HTML tag of the view’s heading component should have the aria-level which was handed to
the view, and the role "heading". This way the heading will integrate into the application’s aria
heading hierarchy.
• For a good accessibility structure make sure your custom view has a heading. If you want to
make the heading invisible but still accessible for screen readers, you can use the plasma style
helper class "-u-unseenButRead" or style your header accordingly.
• Please do not use aria-level for sub headings inside the view. Only the view’s top heading, e.g.
the content box title, should have an aria-level.
• To structure the content of your view use roles on the HTML containers, e.g. "form" for content
with input fields, "document" for text, "heading" for sub headings. Nesting such containers with
roles makes the structure transparent for screen readers.
• Please do not use h-tags for sub headings, since this interferes with the hierarchy created by the
aria-levels. Instead use styled div tags with the role "heading".
• Use a nav-tag as root tag for navigation components or use the role "navigation".
Focus Handling Between Views
In some situations you might want to change the focus from one view to another. For example, this
could happen when adding or removing views in a layout. How to achieve this focus behavior
depends on the concrete requirement and the specific project setup. The Client DevApp provides an
example in the "Examples" section demonstrating a custom focus handling between an overview
and a form.
95

-- 95 of 138 --

Asynchronous Flows With Redux Saga
Client uses the JavaScript library Redux Saga for code that involves asynchronous execution and
side effects. This code can be often seen as defining application flow of some type, like logging-in
and -out of a user or retrieving documents from a server and storing them in the redux store.
Redux Saga allows writing asynchronous code in a synchronous manner, by using generator
functions, called in this context "sagas", that yield descriptions of what "effects" should occur at any
given point in the flow. Those descriptions are then handled by the saga middleware to perform
tasks, like dispatching actions, waiting for some action to come, calling an asynchronous function (a
function that returns a promise). Effects can be blocking or non-blocking.
Most sagas use the take-effect (or the helper effects takeLatest or takeEvery) to wait for an action
that matches some pattern. It is important to remember, that the saga middleware executes after
reducers - if some reducer changes the store in some way on receiving an action, and some saga
listens for the same action, then by the time yield take returns, the store was already modified by
the reducer. If this is not desired, then a new type of action should be created, that is ignored by
reducers.
For general information about Redux Saga, you may want to visit following links:
• Redux project page
• Tutorial with background information
• Comparison of Thunks and Sagas with background information and links to discussions
• Description of an approach that strictly divides actions into signals (that trigger sagas) and
deltas (that trigger reducers)
Implementation of Sagas in Client
In the standard case, like the one described in the official tutorial (to be found under the first link
above), a saga is a generator function that is directly connected to the saga middleware. It has all
the freedoms allowed by Redux-Saga and behaves as if it was a separate thread. It is completely
independent of any other sagas. Client allows to define sagas like this, but the built-in platform-
sagas are implemented in a different way.
Connecting Sagas Directly to Middleware
Custom sagas can be passed in the customSagas-argument to the factory function
ApplicationFactories.createApplicationSetup. Its platform implementation passes each saga to the
run-function defined by the saga middleware. This function executes each saga right away, so if it
contained some blocking code, it would freeze the application. That’s why possibly long running
code should always be run asynchronous and the sagas should always yield effects. Typical usage is
that a saga waits for some action using the take-effect and then calls some asynchronous code using
the call-effect.
Connecting Sagas to the Dispatcher
This method is used for built-in platform-sagas and it allows to overwrite them. Objects
implementing the interface ApplicationSaga.Descriptor can be passed in the
96

-- 96 of 138 --

overridePlatformSagas-argument to the factory function
ApplicationFactories.createApplicationSetup. A descriptor must define following two functions:
• canHandle: takes an action as argument and returns true, if the saga should execute
• handle: calls a generator function (can be seen as a saga), that handles the action matched by
canHandle and returns its result, that is, a generator
To use the Descriptor, you need following import:
import { ApplicationSaga } from "@com.mgmtp.a12.client/client-
core/lib/core/application";
Those descriptors are then used by the dispatcher, a saga that is directly connected to the
middleware, just like in the standard case described before. The dispatcher receives all actions
dispatched to the store and calls canHandle on every descriptor that it knows. For the first
matching descriptor, the dispatcher yields the generator returned by its handle-function, that is
then processed by the saga middleware. To recapitulate, the dispatcher dispatches actions to
functions, that can handle them.
This mechanism can be used to selectively overwrite built-in sagas - all descriptors in
overridePlatformSagas are checked before built-in sagas, so if some of them can handle the same
action as some built-in saga, then it will be executed instead.
This mechanism has its restrictions - some concurrency patterns, that are possible when using
sagas in a standard way, are currently not directly possible, like takeLatest or blocking take. The
dispatcher uses takeEvery under the hood, so an action is always executed, if any of the registered
descriptors can handle it.
For an explanation of takeEvery and takeLatest you may want to visit the documentation on Redux
Saga helpers.
Providing Own Dispatcher
Users of Client can pass the setup function their own implementation of the dispatcher. It is then
executed by the saga middleware, just like provided dispatcher would be, and receives all
registered descriptors as argument.
Error Handling in Sagas
The circumstance that sagas are implemented as generator functions allows for easy error handling
with the try…catch statement. Inside the catch-block, the ERROR-action should be created, that is
then used by reducers to save the error and error status in the affected activity. Below are the steps,
that comprise the error handling.
1. Inside the catch-block in a saga, the ERROR-action should be dispatched. Its payload contains the
error and the property operationType, which can be set to "loading" or "saving" - it is used by
reducer to set the value of either loadingState or savingState in the activity.
2. If the error is of type ApplicationError, then it is saved by reducer as is. Otherwise, the reducer
97

-- 97 of 138 --

wraps the error in an ApplicationError with the error code of INTERNAL_CLIENT_ERROR.
3. Based on value of operationType, the reducer sets either loadingState or savingState to "error".
4. Components can use this information for example to render some kind of indication, that an
error occurred.
5. The error-property can be reset by dispatching the action CLEAR_ERROR.
Built-in Sagas Which Get Triggered by an Action
Below are listed all actions that are consumed by Client built-in sagas. All actions that trigger them
are in the namespace "Activity", so for example the action named "PUSH" below has the type
"Activity/PUSH". If you want to have a different behavior, you can register your own
implementation in the array overridePlatformSagas in the Client application setup.
Models and Documents
Handle loading of A12 documents and models.
Trigge
ring
action
Description
PUSH Executes after the reducer already created a new activity. Dispatches the LOAD_MODELS- and
LOAD_DATA-action if model and/or data loading is required. It also initializes one
DataHolder if it is not already existing. This DataHolder’s descriptor is identical to the
activity’s descriptor, and we refer to it as the activity DataHolder. If there is an Overview
Model specified in the scene for the current activity, this saga also initializes the list
parameters like pagination, filter and sorting.
LOAD_M
ODELS
Loads all models into the Redux store which are specified in the scene matching the
current activity. All referenced Document Models are automatically loaded, so there is no
need to additionally specify the referenced Document Model. It only loads models which
are not yet in the redux store.
LOAD_D
ATA
It performs the data loading for all DataHolders of the current activity whose loading state
is loading. The loading itself is delegated to the DataHandlers.
If you are using a DataLoader, there is some default behavior implemented within the
Client. After the loading, it dispatches a SET_DATA action which sets the data in the activity.
When a new document is created and the preComputeNewDocuments parameter of the
ApplicationSaga.Configuration object is set to true, computations will be executed before
and the calculated document will be set as data of the activity.
If you are using the DataProvider, you have the full control about the data loading
operation, e.g. you have to write the loaded data back into the activity’s DataHolder.
98

-- 98 of 138 --

Trigge
ring
action
Description
SAVE.S
TARTED
/
COMMIT
.START
ED
If performs the saving of data for the current activity. The saving itself is delegated to the
DataHandlers.
If you are using a DataLoader, it dispatches the SET_DATA-action for the activity after saving
with the data from the save result in the payload. If the activity contains a
datasourceActivityId, the SET_DATA-action is also dispatched for this activity. It also
dispatches a RELOAD action for related overview activity if it exists.
If you are using a DataProvider, there is no additional default behavior. The Client simply
runs your implemented provideData method of the matching DataProvider.
For the COMMIT.STARTED action, the activity is cancelled after the saving operation.
REMOVE
_DATA
Calls the delete function of a matching DataHandler for the current activity. If you are
using a DataLoader, it also dispatches the RELOAD-action for the parent overview-activity
and its dependents after the delete operation.
RELOAD Locks the activity (by dispatching the LOCK-action), dispatches the LOAD_DATA-action and
then unlocks the activity (by dispatching the UNLOCK-action).
Cancellation of Activities
Trigge
ring
action
Description
CANCEL
_REQUE
STED
Dispatches a CANCEL-action for all activities which are contained in the payload of the
action. This saga can be overridden with for example the dirtyHandling-saga which is
contained in the extensions of the Client.
ActivitySagas
acquireActivityLock
This saga can be used to acquire a lock for an activity. The saga tries to lock an activity until it has
the lock or until the activity is not in the store anymore. If the activity is already locked by
somebody else, the saga waits for the activity to be unlocked.
The saga has the following signature:
function acquireActivityLock(activityId: string, owner: string, details: object):
SagaGenerator<string | undefined> { /* ... */ }
The locks get a unique id which later has to be used to unlock them.
If the saga acquired a lock it returns the lock id. If the activity gets deleted from the store in the
process the saga returns "undefined". A caller has to check that the lock id is not undefined before
proceeding.
99

-- 99 of 138 --

A caller also has to release the lock after it is not needed anymore.
Example usage:
const lockId = yield call(ActivitySagas.acquireActivityLock, activityId, owner,
details);
if (lockId === undefined){
// activity with activityId is not in the store anymore, so it makes sense to stop
here..
return "failed";
}
// Statements while having the lock
// When you are done, unlock the activity with the lockId
yield put(ActivityActions.unlock({activityId: activityId, lockId: lockId}));
StoreSagas
waitForStateChange
This saga can be used to wait for a specific state change by passing a selector.
The saga has the following signature:
waitForStateChange<T>: (selector: Selector<{stateChanged: boolean, returnValue: T}>)
=> SagaGenerator<T>;
The function expects as an argument a selector, which returns an object, which contains the
following values:
• stateChange: specifies that the state changed to the desired outcome
• returnValue: a value which gets returned by the selector
The waitForStateChange saga calls the selector on the first call and then after each action which
gets dispatched, until the "stateChanged" value is true. In the end the saga returns the "returnValue"
to the caller.
Example usage in another saga:
const returnValue = yield call(StoreSagas.waitForStateChange, mySelector(myArguments))
waitFor
This saga is a simplified variant of waitForStateChange. Instead of requiring a selector that returns
an object with stateChanged and returnValue, it accepts a selector that returns either a value or
undefined.
100

-- 100 of 138 --

This makes waitFor more convenient when you simply need to wait until some value becomes
available in the store, without needing the explicit stateChanged flag.
The saga has the following signature:
waitFor<T>: (selector: WaitForSelector<T>) => SagaGenerator<T>;
type WaitForSelector<T> = Selector<T | undefined>;
Responsive Application Layout Optimization
There are certain layout optimizations for small window sizes (e.g. an application is opened on a
phone device or the browser window is made smaller).
These optimizations apply to the following components in the Client:
• MasterDetailLayout: In a large window the MasterDetailLayout shows two panels next to each
other, if there is more than one view opened. In a small window the layout shows only one
panel.
• NotificationFrame: If the application window is large the notifications are shown separately.
Otherwise they are stacked.
• ApplicationFrameLayout: If the application window is small the mobile navigation behavior
(Mobile Navigation) is enabled.
Furthermore, A12 Engines and Widgets provide layout optimization for small window sizes for
certain components as well.
In order to enable these layout optimizations for the whole application (including Engines and
Widgets) you need to wrap your application in a React context provider called SizeContext. This
provider makes the current window size available to all underlying React components.
The following code shows an example of how this should look like:
export function ResizableApplication(): JSX.Element {
const { breakPoint } = useWindowSize();
return (
<SizeContext.Provider value={{ currentSize: breakPoint.size }}>
<MyApplication />
</SizeContext.Provider>
);
}
WARNING
If you do not wrap your application with the SizeContext.Provider there will
not be any size detection and a large window will be assumed. There is no
fallback to a device detector for Client components.
101

-- 101 of 138 --

Mobile Navigation
General Overview
To be able to navigate backwards in a consistent way on a small device, the ApplicationFrameLayout
of the Client provides a navigation mechanism to show a back button which either cancels the
activities belonging to the currently visible views or expands the sidebar, if it exists.
The back button will be shown as soon as a nested view (e.g. the detail in a Master Detail Layout) is
shown or a sidebar is collapsed.
The position and rendering of the back button depends on the used layout.
In the case of a layout which shows multiple views in mobile mode (e.g. DashboardLayout) the
ApplicationFrameLayout will render a back button in the navigation bar above the current content.
For layouts only showing one view in mobile mode (e.g. Master Detail), a React context with a
onBackButtonClicked callback will be created if a back navigation is possible.
Each view is responsible of showing a back button if this callback is given.
More details can be found in the Mobile Navigation Project setup.
In a multi view layout you also should provide a title for a better navigation experience. This title
needs to be given to the ApplicationFrameLayout with the prop navigationBarTitle.
Furthermore, this feature hides the header and footer of the ApplicationFrameLayout as soon as a
back-button is shown.
The next section will summarize what you need to do in your project to fully enable mobile
navigation.
NOTE
If you do not want this described behavior, you can disable it by setting the prop
disableMobileNavigation of the ApplicationFrameLayout to true. Then no back-button
will be displayed and the header as well as the footer will be shown the whole time.
Mobile Navigation Project Setup
Application
First of all you need to enable responsive layout optimization for the whole application. See
Responsive Application Layout Optimization for more details.
Views
If you use an ActionContentBox, you need to set the content box prop listenToNavigationContext to
true.
If you do not use an ActionContentBox for your view, you need to take care of rendering the button
yourself when the onBackButtonClicked callback is set.
For this purpose the A12 Widgets library provides a BackButton component.
The following code shows how to create the back button which you can then put in the heading of
your content box:
102

-- 102 of 138 --

import type { JSX } from "react";
import { useContext } from "react";
import {
NavigationContentboxContext,
ContentBoxElements
} from "@com.mgmtp.a12.widgets/widgets-core";
export function BackButton(): JSX.Element | null {
const { onBackButtonClicked } = useContext(NavigationContentboxContext);
return onBackButtonClicked ? (
<ContentBoxElements.BackButton onClick={onBackButtonClicked} />
) : null;
}
Title
If you use a layout which shows more than one view in mobile mode (e.g. Dashboard or Null
layout), you should provide a title for the navigation bar, which is shown above the visible content
in the ApplicationFrameLayout.
You can set the title with the prop navigationBarTitle of the ApplicationFrameLayout.
NOTE You do not need to do this, if you do not use the layout in a nested scenario.
Meaning there is no sidebar or other parent activity before.
Custom Layouts
In your custom layouts you need to adapt the layout provider. The declaration of the layout
depends on the number of visible views in mobile mode.
One View in Mobile Mode
For this case the layout provider needs to return a Layout object, which sets visibleViews to "single"
and getClearedAfterViewIndex to "views.length - 2".
The getClearedAfterViewIndex tells the ApplicationFrameLayout after which view index there is a
clear caused by the layout.
The following code shows an example for such a custom layout declaration
import type { FrameViews } from "@com.mgmtp.a12.client/client-core";
import { CustomLayoutComponent } from "./CustomLayoutComponent.js";
export const layout: FrameViews.Layout = {
component: CustomLayoutComponent,
visibleViews: "single",
getClearedAfterViewIndex(layoutProps: FrameViews.LayoutProps) {
return () => {
if (layoutProps.views.length > 1) {
103

-- 103 of 138 --

return layoutProps.views.length - 2;
}
return undefined;
};
}
};
The usage of the properties visibleViews and getClearedAfterViewIndex is described in more detail
in the API documentation about Layout.
Multiple Views in Mobile Mode
For this case the layout provider needs to return a Layout object, which sets "visibleViews" to
"multiple". Furthermore, if you do not show all available views with your layout, you need to set
getClearedAfterViewIndex as well, to define the view index after which the layout causes a clear.
Dynamic Configuration
Dynamic Configuration is a code-first, selector-based alternative to Application Models for
configuring A12 Client applications. Instead of defining regions, flows, scenes, and menus in static
JSON files, you write them directly in TypeScript — enabling runtime-derived, state-dependent UI
configuration.
NOTE
The Dynamic Configuration API is currently marked as @experimental. While it is
functional and used in production applications, its API surface may still change in
future releases.
Setup
To enable Dynamic Configuration in your composable appsetup:
1. Use withDynamicConfig() in your feature composition.
2. Use APPLICATION_MODEL_PLACEHOLDER as the model (the traditional Application Model is not used,
but the setup API still requires one).
3. Register your DynamicConfiguration modules with the ModuleRegistry.
The root component of your application should use DynamicRegionUi (instead of the traditional
layout/view providers) to render the region tree.
Appsetup with Dynamic Configuration
// 1. Register your DynamicConfiguration modules
const modules: DynamicConfiguration[] = [PersonConfiguration];
modules.forEach(m => ModuleRegistryProvider.getInstance().addModule(m));
// 2. Build the composable appsetup with withDynamicConfig()
const initialConfig: A12ApplicationConfig = {
104

-- 104 of 138 --

config: {},
localization: {
supportedLocales: [{ country: "US", language: "en" }]
}
};
const finalConfig = combineFeatures(
withModel(APPLICATION_MODEL_PLACEHOLDER),
withDynamicConfig(),
withHTTPModelLoader,
withLocalization
// ...others
)(initialConfig);
// 3. Create and start the application
createA12ApplicationSetup(finalConfig);
Defining a DynamicConfiguration
A DynamicConfiguration module extends Module but replaces the JSON-based model and views
properties with three new contributions:
• regions — the region/layout tree
• flows — flows containing scenes with match conditions and directives
• menus — menu entries with actions and i18n labels
TIP
The structure of these objects roughly follows the structure of the Application Model.
This can be helpful when migrating an existing App Model to a Dynamic
Configuration. The main differences are described in the migration guide.
Each of these can be either a plain object (static) or a Redux selector function (state) ⇒ T
(dynamic). See Static vs Dynamic Configuration (Selectors) for details.
A simple static module
export const PersonConfiguration: DynamicConfiguration = {
id: "Person",
regions,
flows,
menus
};
Defining Regions
Regions define the layout tree of your application. Each DynamicRegion has a name, a layout (a React
component wrapped in a LayoutNG object), and optional subRegions.
• The root region by convention uses an empty string ("") as its name.
105

-- 105 of 138 --

• Sub-regions are referenced by absolute paths (e.g. "/CONTENT").
• Multiple modules can contribute regions — regions with the same name are merged
automatically.
Region definition
const regions: DynamicRegion[] = [
{
name: "",
layout: ApplicationFrameLayoutNG,
subRegions: [{ name: "CONTENT", layout: MasterDetailRegionLayoutNG }]
}
];
Built-in layout components include ApplicationFrameLayoutNG, MasterDetailRegionLayoutNG,
NullRegionLayoutNG, StackRegionLayoutNG, and DashboardLayoutComponentNG (same ones as for the
Application Model-based layouts). Custom layout components receive LayoutPropsNG which provides
views, regionRef, and subRegionRefs.
To render a region in a view component, use DynamicRegionUi:
Region rendering
import { DynamicRegionUi } from "@com.mgmtp.a12.client/client-core";
export function Root() {
// Render the root region
return <DynamicRegionUi />;
}
export function SubRegion() {
// Render a specific sub-region
return <DynamicRegionUi regionRef="/SIDEBAR" />;
}
Defining Flows and Scenes
Flows group related scenes. Each DynamicScene defines:
• name — a unique scene identifier
• matches — a DynamicMatchCondition: a function (descriptor: Activity.Descriptor) ⇒ boolean
that determines when this scene is active
• sceneChange — contains onEnter and/or onExit arrays of directives
• priorScene (optional) — links to a preceding scene, establishing scene order within a flow
• defaultCase / cases (optional) — for sub-case handling within a scene
106

-- 106 of 138 --

Flow and scene definition
const DESCRIPTOR = { feature: "person" };
const flows: DynamicFlow[] = [
{
name: "person.flow",
scenes: [
{
name: "person.overview",
matches: descriptor => descriptor.feature === "person" &&
!descriptor.instance,
sceneChange: {
onEnter: [
{
type: "DYNAMIC_ADD_VIEW",
region: "/CONTENT",
component: PersonOverview
}
]
}
},
{
name: "person.detail",
matches: descriptor => descriptor.feature === "person" &&
!!descriptor.instance,
priorScene: "person.overview",
sceneChange: {
onEnter: [
{
type: "DYNAMIC_ADD_VIEW",
region: "/CONTENT",
component: PersonDetail
}
]
}
}
]
}
];
Directives
Scene changes use directives to modify the UI:
DYNAMIC_ADD_VIEW
Adds a view component to a region. The component is referenced directly (not by string name).
Optionally includes models (model descriptors) and constraints.
107

-- 107 of 138 --

DYNAMIC_CLEAR_REGION
Clears all views from a region. Optionally accepts a new layout to apply after clearing.
Unlike the traditional Application Model where views and layouts are referenced by name through
a provider, Dynamic Configuration references React components directly — eliminating the need
for a mapping of string constants to components (e.g., "viewProvider").
Defining Menus
Each DynamicMenu has:
• id — unique menu identifier
• label — a Localizable object (typically an i18n resource key like { key:
"application.menu.person" })
• selected (optional) — whether the menu item is currently active
• action (optional) — a callback (dispatch: Dispatch) ⇒ void that is invoked when the menu is
clicked
• children (optional) — nested sub-menus
Menu definition
const menus: DynamicMenu[] = [
{
id: "person-menu",
label: { key: "application.menu.person" },
action: dispatch =>
dispatch(
ApplicationActions.startMainActivityRequested({
descriptor: {},
action: ActivityActions.create({ activityDescriptor: DESCRIPTOR })
})
)
}
];
Menus typically derive their selected state from ActivitySelectors.activitiesByDescriptor() and
dispatch ApplicationActions.startMainActivityRequested in their action.
Static vs Dynamic Configuration (Selectors)
The regions, flows, and menus properties on DynamicConfiguration accept the type
SelectorOrObject<T>:
type SelectorOrObject<T> = T | ((state: object) => T);
Static (plain object): Use when the configuration is known at build time and does not depend on
108

-- 108 of 138 --

application state. This is the simpler option and has no performance concerns.
Dynamic (selector function): Use when the configuration depends on Redux state — for example,
showing different regions based on active activities, or computing menu selected state from
current activities.
Dynamic module with selectors
const dynamicFlows = (state: object): DynamicFlow[] => {
const activities = Object.values(ActivitySelectors.activities()(state));
const dashboardActivities = activities.flatMap(a =>
a?.descriptor.feature === "dashboard" ? a : []
);
return [
{
name: "dashboard.flow",
scenes: dashboardActivities.map(activity => ({
name: `dashboard.${activity.descriptor.variant}`,
matches: createMemoizedMatcher(activity.descriptor.variant as string),
sceneChange: {
onEnter: [
{
type: "DYNAMIC_ADD_VIEW" as const,
region: "/CONTENT",
component: createMemoizedView(activity.descriptor.variant
as string)
}
]
}
}))
}
];
};
const dynamicMenus = (state: object): DynamicMenu[] => {
const activities = ActivitySelectors.activitiesByDescriptor(DESCRIPTOR)(state);
const isSelected = activities.length > 0;
return createMemoizedMenu(isSelected);
};
Performance and Memoization
When using selector functions, it is critical to ensure referential stability. The framework uses
reference equality (===) to detect changes — if a selector returns a new object or function reference
on every call, it triggers unnecessary re-renders even when the underlying data hasn’t changed.
The Problem
Selector functions run on every Redux state change. Creating new objects, arrays, functions, or
109

-- 109 of 138 --

React components inside a selector produces fresh references each time:
Memoization pattern
{
name: "bad",
matches: () => true, // new function on every state change!
sceneChange: {
onEnter: [
{
type: "DYNAMIC_ADD_VIEW",
region: "/CONTENT",
// new component on every state change!
component: (props: ViewNGProps) => <div>{props.activityId}</div>
}
]
}
}
The Solution
Use memoization to return the same reference when the relevant inputs haven’t changed:
Memoization pattern
const DESCRIPTOR = { feature: "dashboard" };
// Memoize component factories to ensure referential stability
const createMemoizedView = memoize(
(variant: string): ViewNGComponent =>
(props: ViewNGProps) => <DashboardView {...props} variant={variant} />
);
const createMemoizedMatcher = memoize(
(variant: string): DynamicMatchCondition =>
descriptor =>
descriptor.feature === "dashboard" && descriptor.variant === variant
);
const createMemoizedMenu = memoize((isSelected: boolean): DynamicMenu[] => [
{
id: "dashboard-menu",
label: { key: "application.menu.dashboard" },
selected: isSelected,
action: dispatch =>
dispatch(ApplicationActions.startMainActivityRequested({ descriptor:
DESCRIPTOR }))
}
]);
110

-- 110 of 138 --

General strategies:
• Move non-dynamic parts out of the selector — use plain objects for anything that doesn’t
depend on state.
• Use a memoization utility keyed on the values that actually change.
• Ensure stable references for: match condition functions, React components, layout objects, and
menu arrays.
Caveats and Limitations
No initialActivity
Dynamic Configuration has no equivalent of ApplicationModel.initialActivity. If your application
needs to start with a specific activity, you have two options:
• Setup Actions Dispatch the initial activity by putting in the setup actions of the application
setup.
• Deep linking configuration: Combine with the deep-linking feature to derive the initial activity
from the URL.
No Default Wrapping of View Components with Progress Indicators / Error Boundaries
Previously, views added via VIEW_ADD directives were automatically wrapped with a progress
indicator and error boundary by the default A12 Client Layouts (like the MasterDetailLayout). These
could be customized via the corresponding provider.
With Dynamic Configuration, you are responsible for the rendering of the layout / view
components directly, which means that A12 Client by default will not do any wrapping in the
default layouts. To restore this functionality, simply wrap your view components with Progress
Indicator / Error Boundary components as needed (or move the logic in the view components
themselves).
Deep Linking Interaction
The deep-linking system traditionally relies on ApplicationModel.initialActivity to determine the
starting point. When using Dynamic Configuration, ensure you handle the initial navigation
explicitly (see above).
Memoization is Required for Dynamic Selectors
Unlike Application Models (which are structurally compared), Dynamic Configuration uses
reference equality. Forgetting to memoize selector return values can cause excessive re-renders.
See Performance and Memoization for details.
No Direct permission Property on Menus
The ApplicationModel.Menu.permission property is not directly available on DynamicMenu. However,
you can implement permission-based filtering in your selector logic by conditionally including or
excluding menu entries based on the current user’s permissions from the state.
111

-- 111 of 138 --

Module Registration Order
When multiple modules contribute regions, the module that defines the root region structure (the
application frame) should be registered first. Other modules contribute into the region tree
established by the root module. In the same way, contributed menus will be shown in order of their
registration when using the default Menu component. If this is not wanted, provide a custom Menu
component (see the A12 Client Devapp for an example).
Comparison: Application Model vs Dynamic Configuration
The following table maps concepts from the traditional JSON-based Application Model to their
Dynamic Configuration equivalents:
Application Model (JSON) Dynamic Configuration (TypeScript)
JSON ApplicationModel file DynamicConfiguration module (TypeScript object)
Module.model (JSON selector) Removed (never) — configuration is code
Module.views (string → component map) Components referenced directly in
DynamicAddView.component
ApplicationModel.Region + Layout (by name) DynamicRegion + LayoutNG (React component)
Flow / Scene / MatchCondition (JSON) DynamicFlow / DynamicScene /
DynamicMatchCondition (function)
Menu with initialActivity + label[] DynamicMenu with action callback + Localizable
label
Directive.Add (VIEW_ADD by name) DynamicAddView (DYNAMIC_ADD_VIEW, direct
component)
Directive.Clear (REGION_CLEAR) DynamicClearRegion (DYNAMIC_CLEAR_REGION)
ApplicationModel.initialActivity No direct equivalent — dispatch actions instead.
View provider (createViewProviderSelector) Not needed — components are imported directly
Layout provider (by string name) LayoutNG object with component property
Backward Compatibility
The AppmodelToDynamicConfigurationAdapter bridges traditional JSON Application Models into the
Dynamic Configuration system. This allows existing modules using JSON models to coexist with
new DynamicConfiguration modules in the same application.
The adapter is automatically registered when using withAppModelAdapter in your composable
appsetup. It converts the loaded Application Model’s regions, flows, and menus into their dynamic
equivalents at runtime.
NOTE
This adapter is not meant to be used in production! It only serves as way to test
DynamicConfiguration in an existing A12 Client application (using Application
Models).
112

-- 112 of 138 --

Migrating from Application Model
This section provides a step-by-step guide for converting an existing JSON Application Model to
Dynamic Configuration.
Step 1: Enable Dynamic Configuration
In your composable appsetup, add withDynamicConfig() and replace any direct model import with
APPLICATION_MODEL_PLACEHOLDER:
Enable Dynamic Configuration
export const finalConfig = combineFeatures(
withModel(APPLICATION_MODEL_PLACEHOLDER),
withDynamicConfig()
// ... other features
)(initialConfig);
Remove withApplicationFrameLayout if present — the application frame is now defined as a
DynamicRegion with a layout component.
Step 2: Convert Regions
Translate the JSON region tree into DynamicRegion[]. Replace layout names with layout components:
JSON Layout Name LayoutNG Component
"ApplicationFrame" ApplicationFrameLayoutNG
"MasterDetail" MasterDetailRegionLayoutNG
"Null" NullRegionLayoutNG
"Stack" StackRegionLayoutNG
"Dashboard" DashboardLayoutComponentNG
Custom name Custom component implementing LayoutPropsNG
Step 3: Convert Flows and Scenes
For each JSON flow, create a DynamicFlow. For each scene:
• Replace JSON matchConditions with a predicate function:
• Replace VIEW_ADD directives with DYNAMIC_ADD_VIEW, referencing the component directly instead
of by name:
Convert scenes
const matchesPersonOverview: DynamicMatchCondition = descriptor =>
descriptor.feature === "person" && !descriptor.instance;
declare const PersonOverview: ComponentType<ViewNGProps>;
// inside the scenes array...
113

-- 113 of 138 --

{
name: "person.overview",
// JSON: "matchConditions": [
// { "key": "feature", "mustEqual": "person" },
// { "key": "instance", "isSet": false }
// ]
matches: matchesPersonOverview,
sceneChange: {
onEnter: [
{
// JSON: { "type": "VIEW_ADD", "name":
"PersonOverview", "models": [...] }
type: "DYNAMIC_ADD_VIEW",
region: "/CONTENT",
component: PersonOverview,
models: [
/* ... */
]
}
]
}
},
• Note that the region property is now explicit on each directive. With Application Models, a
missing region property would fallback to the default region.
Use the state parameter to make the generation of scenes / flows fully dynamic.
Step 4: Convert Menus
Replace inline locale arrays with i18n resource keys, and replace initialActivity with an action
callback:
Convert menus
menus() {
// JSON: "label": [{ "locale": "en", "text": "Person" }], "initialActivity": {
"descriptor": { "feature": "person" } }
return [
{
id: "person-menu",
label: { key: "application.menu.person" },
action: dispatch =>
dispatch(
ApplicationActions.startMainActivityRequested({ descriptor: {
feature: "person" } })
)
}
];
}
114

-- 114 of 138 --

Same as above, state can be used to dynamically return menu items (e.g., hiding menus based on
user information).
Move the localized label texts to your i18n resource files.
Step 5: Handle Initial Activity
Since Dynamic Configuration has no initialActivity equivalent, add an initialization middleware
that sets the starting activity. This example assumes that deep linking is used and
ModelActions.setModelGraph is used as the trigger action.
Handle Initial Activity
export const initializationMiddleware: Middleware = () => next => action => {
const result = next(action);
if (ModelActions.setModelGraph.match(action) && location.hash.replace(/^#/, "")
=== "") {
location.hash = "feature:person";
}
return result;
};
In case deep linking is not used, the initial activity can also be dispatched via setupAction in the
application setup.
Step 6: Register and Clean Up
Register your new DynamicConfiguration module with the ModuleRegistry:
Registering the module
ModuleRegistryProvider.getInstance().addModule(PersonConfiguration);
Then remove:
• The JSON Application Model file
• Any createViewProviderSelector calls
• The old Module.views and Module.model properties
• Layout/view provider configuration from the appsetup
Architecture
This chapter gives an insight into the A12 Client Architecture.
Walkthrough
This page gives an overview of the key modules and elements of the Client. The focus is on high-
115

-- 115 of 138 --

level dependencies and interactions. The following concept map will guide us:
"views" – React –
"model"<br>Abstract UI Structure<br>DYNAMIC<br>
A12 Engines<br>– React
A12 Client Services
"activities" – RUNTIME State<br>– Redux –<br>
" – Application Model <span style="font-weight: normal"><i>(UI Structure and Behavior)</i></span>
Match scene with activity by AD
"fram
creates
...
"data" – DataHub (I/O)<br>– Redux Saga –
[Not supported by viewer]	Application Model 	[Not supported by viewer]	Module
Region Config<br> 	Menu
0..n	1	Scene 	Scene<br>Dire
Identifies component via name<br>and is provided as render property
0..n	[Not supported by viewer]
uses
[Not supported by viewer]
[Not supported by viewer]
Activity Descriptor<br>(AD)
data as<br>render property
[Not supported by viewer]
FormEngine
...Data Editors
1..n
data
Data Handler
add
[Not supported by viewer]	[Not suppo
Code<br>
reference
1
Case
0..n
...Data Loaders
...Model Loaders
Flow[Not supported by viewer]
LayoutProvi	viewComponent-Provider
[Not supported by viewer]	Legend:
[Not supported by viewer]
[Not supported by viewer]
set
[Not supported by viewer]
[Not supported by viewer
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
A12 Server<br>Connector 	[Not supported by viewer]
Logging 	Login/<br>Authentication 	Localization
Form-Engine 	Overview-Engine
Overview-Engine
Client Architecture<span style="font-weight: normal"> – Key modules and elements with dependencies and
[Not supported by viewer]
[Not supported by viewer]
[Not supported by viewer]
Scene Change<br>(onEnter, onExit)<br>
main
optional;<br>mutual exclusive
[Not supported by viewer]
Derived via "region" Selector from activities and scenes
Read the above diagram like this: modules of the Client are in colored boxes. The box title briefly
summarizes the responsibility of the module and also relevant technologies used. Go back to
Principles in the Introduction to see a collapsed diagram of Clients' modules.
Activity / Scene / View Cascade
Activities are not directly related to the UI. Actually, you can create activities (through Redux
actions) in a headless Client application, e.g., when testing on the command line. Activities relate to
UI only through indirection with the so-called Activity/Scene/View cascade. This page will give a
quick walkthrough of what happens when a new activity is created. The following sections of this
chapter contain other detailed walkthroughs for the specifics that we are omitting for now.
1. Application code dispatches a Redux action to start a new activity. In the diagram locate the
arrow labelled "creates" pointing towards "Activity". For example, when the user clicks on a row
in the overview list, the row-selection handler would dispatch this Redux action.
2. The activity is created. It contains, among other things:
◦ The Activity Descriptor (a key/value map with the two pre-defined keys "model" and
"instance").
▪ The Activity Descriptor is the most relevant part; it denotes the intention of its activity
and is then used to select an Application Model scene that changes the UI.
◦ data (which might be missing at this point and loaded later from server - by DataLoaders -
or from a parent activity - by a DataEditor)
◦ Various tracking information like loading state, saving state, processing state, whether the
116

-- 116 of 138 --

activity is locked or dirty, and also error information.
3. The activity triggers now two actions: data loading (yellow area in the diagram) and UI updates
(the blue and green rest in the diagram).
◦ The data loading is handled by so-called data-loading sagas, which is a loop that watches for
new activities without loaded data. These sagas are registered during application startup.
The default sagas delegate the loading (and saving later on) to registered DataLoaders
(load/save from server) and DataEditors (read/write from data of a parent activity). The
selection is done by inspecting the Activity Descriptor.
4. The region selector (in the lower green "frame" module) is called and finds a matching
Application Model scene for the Activity Descriptor. The scene contains
◦ a match condition that has to be matched against the Activity Descriptor (keys and values to
match)
◦ a scene change with directives to execute, also an optional default case again with scene
changes (not detailed here).
◦ Then the directives of the scene change are interpreted and result in a region specification
containing the layouts for regions and views in these regions. In the diagram, locate the light
blue box "model" just above the green "frame" module.
5. The derived abstract UI structure is now used to render actual UI components via a provider
mechanism, that maps names to actual UI components, e.g., a view with name "CRUDOverview"
would map to a specific React component.
◦ Note that derived abstract UI structure is agnostic to any technology (even does not know
about to React).
6. The layout is a React component for a region that defines the visual structure of this region. The
derived views will be rendered according to its specification and the available placeholders in
the respective layouts. For example, the master-detail layout has two placeholders on the
desktop and one on mobile devices.
◦ Views that are not displayed can be accessed through other means, e.g., a dropdown control
that is shown by the layout.
◦ Applications are free to define their own layouts.
7. The UI component for a view is rendered. There are many predefined views, e.g., for the
Engines. Applications are free to define their own views or extend existing ones.
Recipes
In the following sections we want to share best practices and recipes for developing an A12 Client
Application.
Writing Tests for the Client
React Components
We write tests using React Testing Library. If you were using enzyme before, have a look at this
117

-- 117 of 138 --

migration guide as the approach to testing components is different.
ActionCreators / Reducers
In Redux, action creators are functions which return plain objects. When testing action creators, we
want to test whether the correct action creator was called and also whether the right action was
returned.
A reducer should return the new state after applying the action to the previous state.
Because these functions are pure, they are easy to test without mocking.
Example
The following example shows one test case for an action creator.
import { deepStrictEqual } from "node:assert/strict";
import type { Action } from "@com.mgmtp.a12.client/typescript-fsa-redux-5-compat";
import type { AddTodoPayload } from "./action.js";
import { addTodoRequested } from "./action.js";
describe("@com.mgmtp.a12.client.devapp.features.multiple-data-holders", () => {
describe("ActionCreators", () => {
describe(
"When the ActionCreator addTodoRequested is called with " +
"an activity id list id and a new todo string",
() => {
it("creates an action of this type with the given ids and a todo as
payload", () => {
const payload: AddTodoPayload = {
activityId: "1",
listId: "A",
newTodo: "New todo"
};
const expectedAction: Action<AddTodoPayload> = {
type: addTodoRequested.type,
payload
};
deepStrictEqual(addTodoRequested(payload), expectedAction);
});
}
);
});
});
118

-- 118 of 138 --

DataEditor / DataLoader / ModelLoader
Whenever you have to deal with the retrieval and persistence of data or models in your
application, it is likely that you get in touch with these components.
The features documentation provides more information about the DataLoader and DataEditor. The
ModelLoader works quite the same, but is concerned with the retrieval and persistence of models.
When testing these classes, we recommend to use a mocking library.
Sagas
Our preferred way of testing sagas is to run the whole saga and assert that the expected effects
have occurred.
This approach is also recommended by the official redux-saga docs.
Example
import { deepStrictEqual } from "node:assert/strict";
import { mock } from "node:test";
import type { Action } from "redux";
import { runSaga } from "redux-saga";
import type { ActivityMap } from "@com.mgmtp.a12.client/client-core";
import { ActivityActions, ActivitySagas } from "@com.mgmtp.a12.client/client-core";
import { handleInitializeDataHolders } from "./saga.js";
import { createActivity } from "./testUtils.js";
describe("@com.mgmtp.a12.client.devapp.features.multiple-data-holders", () => {
describe("initializeDataHoldersSaga", () => {
describe("When a push action is dispatched with a given activityId", () => {
const payload = {
activityId: "1",
activityDescriptor: { feature: "MultipleDataHolders" }
};
const pushAction = ActivityActions.create({
...payload
});
const dispatched: object[] = [];
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function setupAndRunSaga(state: any = { activities: {} }): Promise<any> {
return runSaga(
{
dispatch(action: Action) {
dispatched.push(action);
119

-- 119 of 138 --

},
getState() {
return state;
}
},
handleInitializeDataHolders,
pushAction
).toPromise();
}
beforeEach(() => {
dispatched.length = 0;
});
describe("When store contains an empty activity map", () => {
it("dispatches a commit started activity with the given activity id",
async () => {
await setupAndRunSaga();
deepStrictEqual(dispatched, [
{
type: ActivityActions.commit.started.type,
payload: payload
}
]);
});
});
describe(
"When store contains some activities where one of them is dependent "
+
"on the activity for the given activityId",
() => {
it("dispatches a commit started activity with the given activity
id", async () => {
mock.method(ActivitySagas, "waitForResponseCancelRequested",
function* () {
return true;
});
const activities: ActivityMap = {
1: createActivity({ id: "1" }),
2: createActivity({ id: "2", initiatingActivityId: "1" })
};
await setupAndRunSaga({ activities });
deepStrictEqual(dispatched, [
{
type: ActivityActions.cancelRequested.type,
payload: { activityIds: ["2"] }
120

-- 120 of 138 --

},
{
type: ActivityActions.commit.started.type,
payload: payload
}
]);
});
}
);
});
});
});
Frequently Asked Questions
Since Client 1.0, we collect questions that are asked frequently. Please address new questions to
Technical Professional Services - we will answer them here if they are of general interest.
General
How to Define the Initial Locale of the Application?
This is described in the Section Localization > Current Locale.
How to Connect a Different Backend?
Client provides the DataLoader API for this purpose. You can register your own loaders in the setup.
Have a look at the integration of the A12 server into the DevApp - it uses the provided connectors
which themselves are an extension on top of the core.
How to Enable Redux DevTools in the Client?
The Redux DevTools provide a store enhancer which can be passed as parameter when calling the
factory function ApplicationFactories.createApplicationSetup. We recommend using the @redux-
devtools/extension package for this. A function creating the enhancer could look like this:
import { composeWithDevTools } from "@redux-devtools/extension";
import { actionSanitizer, type ComposeEnhancer } from "@com.mgmtp.a12.client/client-
core";
export function enableReduxDevTools(): ComposeEnhancer {
return composeWithDevTools({
name: "My Application",
actionSanitizer
});
121

-- 121 of 138 --

}
TIP
We also recommend using the actionSanitizer to strip properties from the payload
that A12 Client injects by default in each activity actions, as these can be very large
and cause issues when viewing the state in devtools.
Why Isn’t There a URL-Based Navigation (Routing) Solution? / How Can I
Integrate React Router?
We provide the Deep Linking extension, which enables URL-based state restoration. It should cover
basic use cases of providing links into specific parts of the application.
A more generic Routing solution can not be provided, because Routing works best for more or less
static web pages. In contrast, Client applications usually run multiple or even nested Activities,
which handle the state of multiple views on the screen. Storing the whole state of all Activities in
the URL is not feasible for every project.
Is it Possible to Extend a Client Extension?
Extensions are optional features of the Client (opt-in). They offer individual means of customization
based on the need of users. We provide several examples on how to extend and customize different
extensions in the </features,Features>> section.
Is There a Way to Configure View Sizes?
View sizes cannot be be configured individually, as they are defined by Layouts. To find out how to
resize views in our builtin layouts read the Resizing section.
An alternative is writing your own layout provider, this gives you the ability to configure more than
what is exposed on our builtin layouts. To find out more read the Custom Layouts section.
Can you Change Application Models during Runtime?
It is possible to change application models during runtime using module API and dynamic module,
an example of this feature is shown in the devapp.
Depending on your use case, it is likely that Dynamic Configuration is the preferred approach.
Can you Add a Button to Maximize a View?
That is possible. We have a sample implementation in the devapp.
Engines
How to Render a UI Engine?
The following module from the Application Model contains one scene which renders the Overview
Engine for the model CRUD-overview when this scene matches an activity with the specified match
122

-- 122 of 138 --

conditions.
{
"name": "CRUD Module",
"menu": {
"name": "crud",
"label": { "en": "CRUD" },
"initialActivity": { "descriptor": { "model": "CRUD" } }
},
"flows": [
{
"name": "CRUD Flow",
"scenes": [
{
"name": "CRUD Overview Scene",
"matchConditions": [
{ "key": "model", "mustEqual": "CRUD" },
{ "key": "instance", "isSet": false }
],
"sceneChange": {
"onEnter": [
{ "type": "REGION_CLEAR" },
{
"type": "VIEW_ADD",
"name": "OverviewCRUD",
"models": [ { "modelType": "overview", "name": "CRUD-
overview" } ]
}
]
}
}
]
}
]
}
The Client first loads all models that are listed in all VIEW_ADD directives of that scene and puts them
into the Redux store. It also loads the referenced models, if these have not already been loaded. In
this example, it is the CRUD-overview Overview Model as well as the referenced DomainCRUD Document
Model.
As soon as the relevant models for the specified view OverviewCRUD are available, the UI component
itself selects the appropriate models from the store and uses them to initialize and render the
Overview Engine.
Please keep in mind that each scene working with A12 models requires a key model. The value for
this model key must match the model you specify in the models list of the VIEW_ADD directives. Of
course, you may add additional Document Models which will be loaded into the store and can be
used for project specific operations.
123

-- 123 of 138 --

Remark: From the model key, the Client derives the specific model names (for document and UI
models), i.e.
• Document Model: Domain and the value of the key model, e.g. DomainCRUD
• Form Model: just the value of the key model, e.g. CRUD
• Overview Model: the value of the key model and -overview, e.g. CRUD-overview
How to Render 2 UI Engines for Distinct Document Models?
You need to add another scene into your module to render more than one UI engine in the same
content area.
You can find an example in the DevApp under UI Handling > Layouts > Dashboard Layout >
Multiple Activities. The example dashboard contains multiple tiles that rely on different models.
DO NOT Add Another VIEW_ADD Directive for a Different UI Engine
Please be aware that it is not possible to use just one scene for rendering two UI Engines. You could
think that we take the module from above and simply add another VIEW_ADD directive, e.g.
{
"name": "CRUD Overview Scene",
"matchConditions": [
{ "key": "model", "mustEqual": "CRUD" },
{ "key": "instance", "isSet": false }
],
"sceneChange": {
"onEnter": [
{ "type": "REGION_CLEAR" },
{
"type": "VIEW_ADD",
"name": "OverviewCRUD",
"models": [ { "modelType": "overview", "name": "CRUD-overview" } ]
},
{
"type": "VIEW_ADD",
"name": "OverviewCRUD",
"models": [ { "modelType": "overview", "name": "Offer-overview" } ]
}
]
}
}
The second VIEW_ADD directive wants to initialize the Overview Engine for the Offer-overview model
which references the DomainOffer model.
Note that the match conditions for this overall scene only have one model key. Its value is CRUD. The
Client is not able to successfully render this second Overview Engine, because it expects CRUD
models and documents.
124

-- 124 of 138 --

Performance
Common Issues Impacting Performance
Dispatching Many Consecutive Actions
This can be problematic when each action triggers a state change and another action, which in turn
also changes the state. A typical case is a higher-level saga, that listens to actions and dispatches
additional ones. When each action triggers state change, dispatching the first trigger action will
cause a fast cascade of state changes, which might impact the render time of components that are
subscribed on a specific piece of the state.
To resolve these problems, try to reduce the number of state changes by putting as much logic as
possible into reducers and use saga/middlewares only where reducers are not possible (for
example, network requests or other side effects). The amount of "work" done in a reducer does not
matter, since there will always be only one state change at the end of it.
Apart from performance, this approach also has some other advantages:
• reducers are simple functions and can easily be unit tested
• reducers can easily be composed and therefore reused
• stepping through the code while debugging is easy, because there are no additional "redux
effect layers"
• cognitive load is lower for developers reading the code
Subscribing to Too Much State
React components should always only subscribe to state changes they are actually interested in.
This way, unrelated state changes will cause a re-render. Consider the following view component:
Using a too generic selector
export function DisplayActivityLockView(props: View) {
const activityId = props.activityId;
// adding a dataHolder to this activity will cause a re-render!
const activity = useSelector(ActivitySelectors.activityById(activityId));
return <div>{activity?.lock ? "locked" : "unlocked"}</div>;
}
Since the whole activity object is selected from the state, any change to it will trigger a re-render.
However, only the lock property of the activity is actually used. Therefore, the component should
subscribe to changes from this property only:
Using a specific selector
export function DisplayActivityLockView2(props: View) {
125

-- 125 of 138 --

const activityId = props.activityId;
// only changes to the lock property cause a re-render
const lock = useSelector(ActivitySelectors.activityPropById(activityId, a =>
a.lock));
return <div>{lock ? "locked" : "unlocked"}</div>;
}
Now, the component will not re-render when the activity state changes, except for the lock property
(which is exactly what we want here).
Using Unstable References
With React and Redux, its important that references to non primitives (like objects or arrays, but
also functions) stay stable (equal by reference) whenever possible. Therefore reducers and
selectors should only return a new value when something actually changed. With arrays and
objects in particular, this can be difficult to spot:
Unstable selector
interface MyState {
readonly someArray: string[];
readonly someObject: object;
readonly maybeArray?: string[];
}
/**
* Returns a new reference on each call, meaning for the exact same state:
*
* selectArray(state) !== selectArray(state)
*/
export function selectArray(state: MyState): string[] {
return [...state.someArray];
}
/**
* Returns a new reference on each call, meaning for the exact same state:
*
* selectObject(state) !== selectObject(state)
*/
export function selectObject(state: MyState): object {
return { selected: state.someObject };
}
Since the selector will be called after every state change (regardless of what it will select) and only
checks for referential equality by default, a component using it re-renders on every state change.
This happens because the selector returns a new object on every call.
For these cases, either try to return stable values or use the second parameter of useSelector to pass
126

-- 126 of 138 --

a custom equality function.
Stable selector
/**
* Returns the array as it is.
*
* NOTE: the reducer is responsible to only change the reference when needed!
* In that case, for the exact same state:
*
* selectArray2(state) === selectArray2(state)
*/
export function selectArray2(state: MyState): string[] {
return state.someArray;
}
/**
* Returns the object as it is.
*
* NOTE: the reducer is responsible to only change the reference when needed!
* In that case, for the exact same state:
*
* selectObject2(state) === selectObject2(state)
*/
export function selectObject2(state: MyState): object {
return state.someObject;
}
While the example above is only for illustrative purposes, a more common real-word scenario is a
selector that returns an empty array/object. Consider this code:
Real-world example
/**
* Returns an array property as is, but provides a fallback in case it doesn't exist.
*
* Since the empty array is defined inline, the return value will not be stable when
* `maybeArray` does not exist!
*/
export function selectMaybeArray(state: MyState): string[] {
return state.maybeArray ?? [];
}
/**
* Returns an array property as is (it could be undefined).
* Any fallback can be done by the caller.
*
* Since `undefined` is a primitive, the return value is stable.
*/
export function selectMaybeArray2(state: MyState): string[] | undefined {
return state.maybeArray;
127

-- 127 of 138 --

}
Using Context
Every time the value of a React context changes, the children of the context provider will re-render.
Therefore the same principle as above applies here. The context value needs to be stable and only
return a new value when something actually changed.
Also note that useContext on its own does not provide a custom equality function.
How to Analyze
Its recommended to install the React developer tools.
Visualize Renderings
The developer tools extension can be configured to visualizing any rendering by highlighting the
affected component(s). You can find the setting under "General → Highlight updates when
components render"
Note that this displays any rendering, including the valid ones. Therefore this should only be used
in cases where you don’t expect any renderings and want to check the impact of a state change.
Use the Devtools Profiler
The developer tools extension provides a Profiler panel that records all renders when running.
NOTE Make sure to activate "Record why each component rendered while profiling." in
the settings of the Profiler.
While recording, perform the "problematic" state changes to trigger rendering. Afterwards, the
extension will display each render cycle and the components that were affected. In case your
profile contains lots of unrelated/unproblematic renders (very short ones), they can be excluded via
the setting "Hide commits below".
When clicking on a component, the cause of the rendering and its time should be displayed. When
identifying "slow" renders, check whether the rendering is expected (for example, a Modal
component rendering the first time after the user clicked a confirm button). If it is, check the
render logic of the React component to identify any blocking code paths. If not, try optimizing the
component based on the cause (for example with memoizing or by stabilizing any selectors)
Setup Programmatic Profiling
This can be done with the React Profiler directly in the code or by using external libraries like
WhyDidYouRender. These libraries hook into React internals to track any changes, including any
unwanted re-renderings. This approach allows you to set up unit tests for common performance
problems.
NOTE Note that tools like WhyDidYouRender rely on using internal code of React, which
128

-- 128 of 138 --

might break with a new React release. This needs to be taken into account when
upgrading.
Troubleshooting
Initial Activity Does Not Load Correctly
When your Application Model contains an initial activity that performs model loading, please check
whether your Model Graph is setup correctly and whether you are using the correct trigger action.
See the documentation about the Deep Linking Feature for details.
Client-Internal Actions Seem to Have No Effect
If Client-internal behavior does not work the way you are used to (e.g. data loading is not triggered
after "loadData" is dispatched, activities are not canceled after "cancel" is dispatched etc…), please
check if all of your registered Data Reducers (specified via Application Setup or Module API) are
written correctly. Take a look at the documentation about Data Reducers for an example and to
know what to look out for.
API Documentation
The API documentation can be found here.
Breaking Change Management
The client library follows the general A12 breaking change interpretation, but has the following
exceptions:
• Adding new keys to the localization resource key objects is not considered a breaking change.
Otherwise, it would not be possible to provide overridable default translations for texts in
components introduced with new optional features.
Compile errors resulting from this should point consumers to missing translations in locales
without default translations.
Migration Instructions
The following sections contain migration instructions, starting from Client 15 (2024.06) to the latest
version. For the migration from older versions, please refer to the Client documentation of A12
version 2024.06.
NOTE The complete list of changes for the A12 Client can be found in the changelog.
129

-- 129 of 138 --

Application Model Migration Tool
To migrate your Application Models, first install or update the migration tool with your preferred
package manager, e.g., with pnpm:
pnpm install -g @com.mgmtp.a12.client/client-application-model-migration
Then run the following command to perform the migration:
client-application-model-migration <path to appModel files> -b
Alternatively, you can use pnpm dlx to perform the migration directly:
pnpm dlx @com.mgmtp.a12.client/client-application-model-migration <path to appModel
files> -b
Note that if the given path points to a directory instead, it will be searched recursively for
Application Models to migrate. In case you do not have your Application Models under version
control, you can set the optional -b flag to create backups of your models. Use the -h flag to display
all available options.
2026.06
Breaking Changes
Codemod recipes exist for some of the breaking changes. To use them, first install or update the
codemod with your preferred package manager, e.g. with pnpm:
pnpm install -g @com.mgmtp.a12.client/client-codemod@17
Then run the specific recipe:
client-codemod <recipe> <path to directory with tsconfig.json>
// or as a single command
pnpm dlx @com.mgmtp.a12.client/client-codemod@17 <recipe> <path to directory with
tsconfig.json>
Integration of Dependencies
The versions of the A12 dependencies were increased. Please check the documentation of the
respective components for further migration instructions.
130

-- 130 of 138 --

Required
Yes
Migration
Manual, no codemod available.
Result
See component documentation for details.
React Peer Dependency Update
The peer dependency version of react has been increased to ^19.2.6 to be able to use latest React
features. If you are on an older React 19 version, update your react and react-dom dependencies.
Required
Yes
Migration
Manual, no codemod available.
Result
{
"dependencies": {
"react": "^19.2.6",
"react-dom": "^19.2.6"
}
}
Stricter Typing For Sagas
The type SagaIterator from redux-saga has been replaced with SagaGenerator from typed-redux-saga.
This change improves type safety when using yield* with typed-redux-saga effects. Since both types
are structurally compatible at runtime, it should be transparent to consumers.
Required
No
Migration
Not needed
Result
No changes required, but its recommended to update your own saga typings to also use
SagaGenerator for better type safety.
Removal of Widget Customization Via widgetMap Property
Following its deprecation in A12 Client 16.1.0, the ability to customize certain A12 Widgets rendered
131

-- 131 of 138 --

by the A12 Client Components (like Layouts) using the widgetMap property is now removed. Instead,
the ClientWidgetMap can be used.
For detailed migration instructions, see the deprecation note.
The removal of the widgetMap property made some interfaces obsolete, which are therefore
removed as well:
Interface Replacement
ModalRegionUiNGProps RegionUiPropsNG
MasterDetailLayoutPropsNG LayoutPropsNG
ModalRegionUiProps RegionUiProps
FrameViews.MasterDetailLayoutProps FrameViews.LayoutProps
Required
Depending on whether you used the widgetMap property.
Migration
Manual, no codemod available.
Result
Before, when customizing the MasterDetail widget:
const customWidgetMap: FrameViews.MasterDetailLayoutProps["widgetMap"] = {
masterDetail: CustomMasterDetail
};
export function CustomResizableMasterDetailLayout(
props: FrameViews.MasterDetailLayoutProps
) {
return <FrameViews.MasterDetailRegionLayout {...props}
widgetMap={customWidgetMap} />;
}
Now, wrap the outer component with the ClientWidgetMapContext instead and pass your custom
widget there:
import { useContext } from "react";
import { ClientWidgetMapContext } from "@com.mgmtp.a12.client/client-core";
export function CustomResizableMasterDetailLayout(
props: FrameViews.MasterDetailLayoutProps
) {
const DefaultClientWidgetMap = useContext(ClientWidgetMapContext);
const customWidgetMap = {
...DefaultClientWidgetMap,
132

-- 132 of 138 --

MasterDetail: CustomMasterDetail
};
return (
<ClientWidgetMapContext.Provider value={customWidgetMap}>
<FrameViews.MasterDetailRegionLayout {...props} />
</ClientWidgetMapContext.Provider>
);
}
Updating to ES2025
The javascript output of the npm artifacts was updated from ES2024 to ES2025 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
Required
Depending on whether support for older browsers is required.
Migration
Manual, no codemod available.
Result
Depending on your bundler setup, consult its documentation on how to include polyfills.
Removal of Deep Import Paths
Deep import paths from @com.mgmtp.a12.client/client-core and @com.mgmtp.a12.client/client-data
(e.g., @com.mgmtp.a12.client/client-core/lib/core/application/index.js) have been removed. All
exports must now be imported directly from the package root. For detailed migration instructions,
see the deprecation note.
NOTE
The package now uses the exports field instead of main/types. This should be
transparent for most consumers but may require configuration updates for older
bundlers.
Required
Yes.
Migration
Codemod available in 2025.06-ext4.
Result
// before
import { ApplicationModel } from "com.mgmtp.a12.client/client-
core/lib/core/application/index.js"
// after
133

-- 133 of 138 --

import { ApplicationModel } from "com.mgmtp.a12.client/client-core";
Conversion of value-only namespaces into objects
Several namespaces consisting only of value exports were changed to const objects. This was done
to improve tree-shaking support in modern bundlers. This change should be transparent for most
consumers since the usage syntax remains the same (e.g., ActivitySelectors.activities()).
Required
No
Migration
Not needed
Result
No changes
Correction of Return Types
The return type of FrameFactories.viewProvider(componentName: string) has been corrected to
View.ViewComponent | undefined to reflect what the method actually returns. Make sure that every
time the method is used, a static default ViewComponent is chained via ??.
Required
Depending on whether you used FrameFactories.viewProvider and if you handled the case of it
returning undefined before.
Migration
Manual, no codemod available.
Result
Depending on your previous usage, add a fallback for the undefined case.
Stricter ApplicationModel.isInstance type guard
The type guard for Application Models has become stricter. It now checks the model content in
addition to just the model type. Any models passed to A12 Client already had to conform to this and
this only affects the unlikely case you used this on models not passed to A12 Client.
Required
No
Migration
Not needed
Result
No changes required
134

-- 134 of 138 --

Redux 5 Upgrade
A12 Client has been upgraded to Redux 5 along with its ecosystem. For a complete list of breaking
changes in Redux 5, refer to the official Redux 5 release notes.
Required
Yes.
Migration
Partial, some codemods available (see below).
Result
Consists of multiple different changes, see the following sections:
Dependency Updates
The following peerDependency versions have been updated:
Package Old Version New Version
redux ^4.0.5 ^5.0.0
react-redux ^7.2.2 ^9.2.0
redux-saga ^1.1.3 ^1.3.0
Additionally, typescript-fsa actions have diverged from Redux actions and do not work out-of-the-
box anymore. We have provided @com.mgmtp.a12.client/typescript-fsa-redux-5-compat as a
replacement, which is a Redux 5-compatible fork maintained by us.
Update Dependencies
Update your package.json to use the new versions:
{
"dependencies": {
"redux": "^5.0.0",
"react-redux": "^9.2.0",
"redux-saga": "^1.3.0",
}
}
Remove @types/react-redux from your dependencies, as react-redux now ships with its own
TypeScript types. Additionally, there are no peerDependency warning between React and React-
Redux anymore, so you can remove any overrides or patches you had.
Migrate typescript-fsa Imports
Run the migrateTypescriptFsaImports codemod to update all imports from typescript-fsa to the
fork:
135

-- 135 of 138 --

client-codemod migrateTypescriptFsaImports <path to directory with tsconfig.json>
Migrate AnyAction Type References
AnyAction from both redux and typescript-fsa-redux-5-compat are now deprecated. AnyAction from
typescript-fsa-redux-5-compat is not compatible with Redux anymore, as they require action types
to be strings.
AnyAction from redux is not type safe and can be migrated to the new UnknownAction, but its super
type Action is often the cleaner type.
Run the migrateAnyActionType codemod to migrate all usages of AnyAction to Action:
client-codemod migrateAnyActionType <path to directory with tsconfig.json>
Define Type of State in useSelector
react-redux now have stricter types by default. Especially the type of state in useSelector is now
unknown. Selectors of A12 will not work with this type. It is recommended to define the type of the
state for your application yourself, but returning to object is the minimum.
You can fix this by augmenting the type of useSelector in a custom declaration file.
// @types/react-redux.d.ts
import type { UseSelector } from "react-redux";
declare module "react-redux" {
export const useSelector: UseSelector<object>;
}
Include this in your TS compilation (e.g. with the "include" property of the tsconfig.json).
Alternatively, react-redux provides methods to define a pre-typed version of useSelector and
useDispatch.
import { useSelector } from "react-redux";
export const useAppSelector = useSelector.withTypes<object>();
You then can use this useSelector as a direct replacement wherever you previously imported
useSelector from react-redux.
Potentially fix Middlewares
The typing of actions and the result of next inside middlewares have changed to be unknown. Most
middlewares should be safe, as the first call in any middleware is usually a type guard (usually
actionCreator.match).
136

-- 136 of 138 --

If you have a generic middleware that does not care about the specific type of action and accesses
action.type directly, you can now use the isAction() type guard from redux to ensure type safety:
// Before
import { type Middleware } from "redux";
const loggingMiddleware: Middleware = api => next => action => {
console.log("Action:", action.type);
return next(action);
};
// After
import { isAction, type Middleware } from "redux";
const loggingMiddleware: Middleware = api => next => action => {
if (isAction(action)) {
console.log("Action:", action.type);
}
return next(action);
};
Potentially fix Reducers
Same as with middlewares, the type of action in reducers is now a bit type safer and is now
UnknownAction. Most reducers should be fine as a type guard is normally used. If not (e.g. you used
action properties without type guard), you need to add one.
// Before
const myReducer: Reducer = (state, action) => {
if (action.type === "CUSTOM_ACTION") {
return action.payload.value;
}
};
// After
import { actionCreatorFactory } from "@com.mgmtp.a12.client/typescript-fsa-redux-5-
compat";
const factory = actionCreatorFactory("");
const customAction = factory<string>("CUSTOM_ACTION");
const myReducer: Reducer = (state, action) => {
if (customAction.match(action)) {
return action.payload.value;
}
};
137

-- 137 of 138 --

More specific typing in module-specific data reducer selectors
The typing of the dataReducers property in the Module interface and the moduleReducers property in
the ActivityReducers.Config interface have been changed to be more specific.
Previously, the signature of both selectors suggested that they receive the entire state. However,
since data reducers only operate on the activities slice of the state, the selectors only receive the
activity map as an argument. This change allows for better type safety and clarity when
implementing these selectors.
The behavior of these selectors has not changed, so if you were not using the argument before, no
migration is required. If you are using the argument, make sure to update your usage accordingly.
Required
Depending on whether you use the arguments of these selectors.
Migration
Not needed
Result
export interface Module {
// ... other properties
// Before
readonly dataReducers?: Selector<ReadonlyArray<ActivityReducers.DataReducer>>;
// After
readonly dataReducers?: (activityMap: ActivityMap) =>
ReadonlyArray<ActivityReducers.DataReducer>;
}
export namespace ActivityReducers {
// ... other interfaces
export interface Config {
// Before
moduleReducers?(state: object): ReadonlyArray<DataReducer>;
// After
moduleReducers?(activityMap: ActivityMap): ReadonlyArray<DataReducer>;
}
}
[1] Knowing which model(s) to load, is the responsibility of the selected DataLoader; the details will be described later on.
[2] While mixing loading data from parent activities and services is possible, we focus on the most common type here to keep
things simple.
[3] Note that multi step wizards can also be realized with just one form by using one screen per step.
[4] It is technically possible to use the ModelLoader API without a Model Graph, but this requires you to write your own
implementation to resolve model references some other way.
138

-- 138 of 138 --

