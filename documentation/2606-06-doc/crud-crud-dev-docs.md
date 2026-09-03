# crud crud dev docs

CRUD
Introduction
The CRUD package contains a client extension which provides components to specifically resemble
a CRUD UI.
It is useful for analysts / modelers during development phase. It is totally OK to use it in production
as long as it is used as a drop-in component without any customizing.
Getting Started
Installation
The CRUD extension is provided as an ESM npm package. Run the following command to install:
npm install @com.mgmtp.a12.crud/crud-core
Setup
Setup application
To use the CRUD extension in you client application, you only need to register a middleware and
some sagas in the application setup as shown below. They can be obtained via the CRUDFactories.
Setting up the CRUD extension in a client application
import type { Middleware } from "redux";
import type { SagaGenerator } from "typed-redux-saga";
import { CRUDFactories } from "@com.mgmtp.a12.crud/crud-core";
import { ApplicationFactories, type ApplicationSetup } from
"@com.mgmtp.a12.client/client-core";
export function setup(): ApplicationSetup {
return ApplicationFactories.createApplicationSetup({
...otherConfigurations,
additionalMiddlewares: [...otherMiddlewares,
CRUDFactories.createCRUDMiddleware()],
customSagas: [...otherPlatformSagas, ...CRUDFactories.createSagas()]
});
}
To make the CRUD Views available, adapt the viewProvider of your application. Then you can refer
1

-- 1 of 10 --

to the view component names, e.g. "OverviewCRUD", in the VIEW_ADD directives of your
application model.
Providing CRUD views
import React from "react";
import { CRUDFactories } from "@com.mgmtp.a12.crud/crud-core";
import { FormEngineViews } from "@com.mgmtp.a12.formengine/formengine-core";
import { type View, FrameFactories } from "@com.mgmtp.a12.client/client-core";
export function createViewProvider() {
const formEngineViewProvider = createFormEngineViewProvider();
return function viewProvider(componentName: string): React.ComponentType<View> {
return (
FrameFactories.viewProvider(componentName) ||
// Provides view component named "OverviewCRUD"
CRUDFactories.createCRUDRenderer(componentName) ||
// Provides view component named "FormEngine"
formEngineViewProvider(componentName) ||
Placeholder
);
};
}
function createFormEngineViewProvider(): (componentName: string) =>
React.ComponentType<View> | undefined {
const components: { [name: string]: React.ComponentType<View> | undefined } = {
FormEngine(props) {
return <FormEngineViews.FormEngine {...props} />;
}
};
return function formEngineProvider(name) {
return components[name];
};
}
function Placeholder(): React.ReactNode {
return <div>ERROR: NO CONTAINER FOUND</div>;
}
Features
Components
The components which are needed to resemble the CRUD UI are:
2

-- 2 of 10 --

• The Overview Engine CRUD view from this CRUD extension
• The Form Engine view from the client extension of the form engine package.
• CRUD Sagas and a CRUD Middleware from this CRUD extension
Overview Component
The Overview Engine view, available via CRUDFactories.createCRUDRenderer with the name
"OverviewCRUD", is already connected to the store and is pre-configured for the CRUD
functionality. It needs the CRUD Sagas to be registered in the application setup, which provide CRUD
specific Redux backend functionality.
The OverviewCRUD view handles via the CRUD sagas the following CRUD events from the Overview
Model:
• add: Initiate an Activity to add a new document
• delete: Delete the selected document, which will try to cancel the current detail activity if it has
the same instance with the selected document
Clicking on an overview row triggers the default row action of selecting and opening the document
in the Form Engine view. Custom default row actions are not supported by the CRUD extension.
Form Component
The Form Engine view is also provided as a connected component. It requires the CRUD
middleware to be registered in the application setup in order to provide the CRUD functionality.
For the basic Form Engine functionality, a middleware, a saga and special reducers need to be
registered in the application setup. Please consult the Form Engine Documentation for more details.
The Form Engine view handles via the CRUD middleware the following CRUD events from the Form
Model:
• CRUD::SAVE: Saves the current data of the Activity
• event_submit / event_save: Commit the current Activity
• event_cancel: Cancel the current Activity
The submit/save events will also trigger a full validation in the A12 Form Engine. Note that the
further behavior depends on the configured DataProviders, DataLoaders and DataEditors. See
Client Documentation.
The CRUD extension via the CRUDViews namespace also provides the FormEngineView, a basic drop-in
Form Engine view which has built-in support for rendering relationship ui components.
The CRUD Engines must be used together in order to work correctly.
Customizing Not Supported
This extension combines a set of features to provide a functionality which is mostly useful for
3

-- 3 of 10 --

analysts / modelers.
In case you need a different look or behavior, please use the components provided by the overview
engine and form engine that are meant for customization.
The CRUD extension exports the namespace EventNames, which contains constants for event names
of the typical crud events described above and we recommend to use those instead of hard coding
the crud event name strings into your custom component.
Activity Descriptors
Activity descriptors are key/value maps that identify the active scene. The two base keys are model
(the document model reference) and instance (the document identifier or NEW for unsaved
documents). An additional engine key appears in specific scenarios documented below.
NOTE
Current state documentation. The activity descriptor conventions documented
here reflect the current architecture of the A12 platform as of 2026.06. The CRUD
component (Overview Engine + Form Engine) and Tree Engine define their own
descriptor patterns independently, which produces overlapping conventions when
combined with a SME-generated Master Details Model. This document is the
authoritative reference for current behavior.
Default Behavior
When using the CRUD component without a SME-generated Master Details Model, no engine key is
required. CRUD activities are created by spreading the parent activity’s descriptor and overriding
only model and instance:
Opening a new document:
{
...activity.descriptor, // Everything from the parent activity's descriptor
// The following keys replace the values from the parent activity
model: action.payload.model, // The model the new document is based on
instance: NEW_INSTANCE_IDENTIFIER // "__NEW__"
}
Editing an existing document:
{
...activity.descriptor, // Everything from the parent activity's descriptor
// The following keys replace the values from the parent activity
model: modelIdToUse, // The model of the edited document
instance: instanceId // The ID of the edited document
}
4

-- 4 of 10 --

Projects are free to define any additional keys in their initial activity descriptor and Application
Model match conditions, CRUD does not enforce any descriptor structure beyond model and
instance.
NOTE
Tree Engine has its own CRUD-like functionality for managing tree nodes and link
documents, independently of this CRUD component. It follows the same spread-and-
override pattern without enforcing an engine key. See Tree Engine’s Activity
Descriptors for the descriptor shapes specific to Tree Engine.
NOTE
One exception is the Relationship Engine editor opened via the event_add_link
action, which always carries engine: "relationship" in its descriptor. This key is
required to distinguish the relationship editor scene from a plain form activity in
Application Model match conditions. Future Relationship Engine modeling
capabilities will provide a more flexible alternative for this use case.
With Master Details Model
When the Application Model uses a Master Details Model (MDM) generated by SME, the initial
activity descriptor is structured as:
{
module: id + "Module", // The name of the Module describing the initial activity
engine: md.type // The engine used for the activity, e.g. "tree" or "overview"
}
Since every CRUD child activity spreads and forwards the parent descriptor, all child activities also
carry the engine key. The MDM-generated match conditions rely on this forwarded value:
Scene Condition
Overview Scene engine mustEqual tree or overview
module mustEqual <DocumentModel id> +
"Module"
instance isSet false
Detail Scene engine mustEqual tree or overview
instance isSet true
linkForm isSet false
model mustEqual <DocumentModel>
LinkDocumentEditor Scene engine mustEqual tree
instance isSet true
linkForm isSet true
model mustEqual <DocumentModel>
5

-- 5 of 10 --

RelationshipEditor Scene engine mustEqual relationship
instance isSet true
model mustEqual <DocumentModel>
If you want to use this default MDM behavior, your activity descriptors must follow these
conditions.
NOTE
The SME generator sets engine: md.type on the initial activity automatically. For
Tree Engine specifically: engine: "tree" is optional in the standalone case (since
Tree Engine v9.0.0), but the MDM-generated LinkDocumentEditor match condition
requires it, which the SME generator satisfies automatically. See Tree Engine’s
Activity Descriptors for details.
API Documentation
The API documentation can be found here.
Breaking Change Management
For the general definition of breaking and non-breaking changes in the A12 platform, as well as
frontend and backend perspectives, see the A12 Breaking Change Management page.
The following section describes how this general definition is interpreted for the CRUD package.
Public API Surface
The public API is defined by the exports in index.ts. Any removal or incompatible change to an
exported symbol is a breaking change. Additions to the public API are non-breaking.
Exports annotated @experimental may change in minor releases without being considered breaking.
Exports annotated @internal are not part of the public API and carry no stability guarantee.
Resource Keys
Adding new entries to CRUD_RESOURCE_KEYS is non-breaking: applications that do not override those
keys continue to receive the default translation. Removing or renaming an existing key is a
breaking change for any application that provides a custom translation for it.
Redux State Shape and Action Types
The Redux action type strings and the reducer state shape are part of the public contract. Renaming
or removing an action type, or restructuring the state in a way that breaks existing selectors, is a
breaking change.
Selectors and sagas that are part of the public API follow the standard public-API rule above.
6

-- 6 of 10 --

Composable Factory and Public Exports
The signatures of withCRUD(), CRUDFactories, CRUDViews, and EventNames are part of the public API.
Any change to parameter types or return types that requires call-site updates is a breaking change.
New optional parameters are non-breaking.
A concrete example of a past breaking change: the Redux 4 → 5 upgrade required consumers to
update peer dependencies and replace AnyAction with UnknownAction. See the Migration Instructions
chapter for the migration steps.
Non-Breaking Examples
The following types of changes are consistently treated as non-breaking in the CRUD package:
• Adding a new optional configuration field to an existing API
• Adding new CRUD_RESOURCE_KEYS entries
• Introducing a new @experimental export
• Internal refactoring that does not affect public exports
• Bumping a transitive dependency with no visible API impact
Migration Instructions
2026.06
Please have a look at Migration to latest A12 chapter for an explanation of general steps.
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
7

-- 7 of 10 --

optional — automates the typescript-fsa import rewrite only (see Codemod:
migrateTypescriptFsaImports). The dependency updates are manual.
The crud-core package now requires Redux 5 (peer dependency redux@^5.0.0). This is a breaking
change for consumers still on Redux 4.
Recommended steps for consumers:
1. Update redux, react-redux, and redux-saga to versions compatible with Redux 5 in your host
application.
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
The codemod does not touch dependency versions — those remain manual.
Replace SagaIterator with SagaGenerator
Tooling: none — manual change.
All saga return types have been changed from SagaIterator (from redux-saga) to SagaGenerator
(from typed-redux-saga). This affects the public API of CRUDFactories.createSagas().
If you have custom sagas that explicitly type their return type as SagaIterator, update them to use
SagaGenerator instead:
// Before
import { SagaIterator } from "redux-saga";
function* mySaga(): SagaIterator<void> { /* ... */ }
// After
8

-- 8 of 10 --

import { SagaGenerator } from "typed-redux-saga";
function* mySaga(): SagaGenerator<void> { /* ... */ }
typed-redux-saga is already a transitive dependency via @com.mgmtp.a12.client/client-core.
2025.06
Please have a look at Migration to latest A12 chapter for an explanation of general steps.
2.0.0
Migrate to ESM
The npm artifact @com.mgmtp.a12.crud/crud-core was migrated from CommonJS to ESM. When using
Node 22.12+ and modern build tools, there should be no changes necessary to your bundler setup.
NOTE If your tests depend on mocking/stubbing CRUD API, consult the documentation of
your test runner on how to work with ES modules.
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
9

-- 9 of 10 --

"@types/react-redux": "^7.1.34"
}
}
Remove onDocumentClick and onDocumentDoubleClick callbacks for OverviewCRUD
CRUDViews.OverviewCRUD is now switched to the new OverviewEngine’s ViewComponent, this
results in the 2 callbacks onDocumentClick and onDocumentDoubleClick no longer available. Refer to
OverviewEngine’s migration instruction for more details.
10

-- 10 of 10 --

