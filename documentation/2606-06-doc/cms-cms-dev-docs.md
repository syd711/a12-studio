# cms cms dev docs

Content Management System for
Developers
NOTE
This documentation belongs to an A12 Enterprise Component which is not part of
the Open Source offering (A12 Community Edition). Please feel free to browse the
documentation and learn more about how you can use this A12 component in your
project. Learn more about the benefits from an A12 Enterprise Subscription on the
Editions & Licensing page.
Introduction
The A12 Content Management System (CMS) is a lightweight application module that can be
integrated into A12 applications. It enables content managers to create, publish and maintain pages
with meta data and content, and organize them in a hierarchy. And it provides components and API
to render these pages as well as to use the page hierarchy to render navigation for the end users.
The Content Management System uses the Content Engine and the Content Model Editor - lower
level components, which can also be used for other purposes, and which are documented
separately.
Getting Started
Installation
Client side
Content Management System includes multiple npm packages and depends on Content Engine as
well as other A12 components. First, install the Content Engine packages:
npm install @com.mgmtp.a12.contentengine/contentengine-core
@com.mgmtp.a12.contentengine/contentengine-editor
Next, install the Content Management System packages:
npm install @com.mgmtp.a12.cms/cms-management-types @com.mgmtp.a12.cms/cms-server-
connector @com.mgmtp.a12.cms/cms-management @com.mgmtp.a12.cms/cms-viewer
Replace <version> with the specific version of the package you want to use. The latest version is
{revnumber}.
In addition, it is necessary to install other A12 packages as peer dependencies of the Content
Engine, such as Base, Widgets, Client, and others.
1

-- 1 of 17 --

Server side
Next, add the contentengine-core package as a dependency in your build.gradle file. Specify the
correct group, name, and version of the package.
dependencies {
implementation 'com.mgmtp.a12.contentengine:contentengine-core:<version>'
// other dependencies
}
Replace <version> with the specific version of the package you want to use. The latest version is
{revnumber}.
Setup
The following sections provide the necessary information to set up for Management and Viewers
modules in two separate client applications. The project that wants to have both modules in the
same application can follow the instructions for both modules with additional steps to combine
them.
Setup Management module
The following setup assumes that project already has the proper A12 Client setup and the
Management module is installed.
Register element libraries
To use the Management module, register the necessary (editor) element libraries in your
application.
Configure appsetup.ts
import { ModuleRegistryProvider } from "@com.mgmtp.a12.client/client-core";
import { EditorElementLibraryRegistry } from
"@com.mgmtp.a12.contentengine/contentengine-editor";
import {
ContentManagementFactories,
FragmentEditorElementLibrary,
ManagementEditorElementLibrary
} from "@com.mgmtp.a12.cms/cms-management";
export function setup() {
// Add Management modules
[
ContentManagementFactories.createPagesModule(),
ContentManagementFactories.createFragmentsModule(),
ContentManagementFactories.createAssetsModule()
].forEach((module) => ModuleRegistryProvider.getInstance().addModule(module));
2

-- 2 of 17 --

// Register element libraries
EditorElementLibraryRegistry.get()
.addEntry(ManagementEditorElementLibrary.get())
.addEntry(FragmentEditorElementLibrary.get());
// other configurations
}
Register view components
To use the Management module, register the necessary element libraries in your application.
Configure view provider
import * as React from "react";
import { type View, FrameFactories } from "@com.mgmtp.a12.client/client-core";
import { type ViewComponentMap, type ViewComponentProvider } from
"@com.mgmtp.a12.contentengine/contentengine-core";
import {
PagesTree,
DynamicPageForm,
ContentEditorForm,
DYNAMIC_PAGE_FORM_VIEW_NAME,
CONTENT_EDITOR_FORM_VIEW_NAME,
PAGE_MANAGEMENT_TREE_VIEW_NAME,
FRAGMENT_EDITOR_FORM_VIEW_NAME
} from "@com.mgmtp.a12.cms/cms-management";
export function createViewProvider() {
const additionalViewProvider = createAdditionalViewProvider();
return (componentName: string): React.ComponentType<View> =>
additionalViewProvider(componentName) ||
FrameFactories.viewProvider(componentName) ||
(() => <div>ERROR: NO CONTAINER FOUND</div>);
}
function createAdditionalViewProvider(): ViewComponentProvider {
const components: ViewComponentMap = {
[FRAGMENT_EDITOR_FORM_VIEW_NAME]: (props) => <ContentEditorForm {...props}
fragment />,
[CONTENT_EDITOR_FORM_VIEW_NAME]: (props) => <ContentEditorForm {...props} />,
[PAGE_MANAGEMENT_TREE_VIEW_NAME]: (props) => <PagesTree {...props} />,
[DYNAMIC_PAGE_FORM_VIEW_NAME]: (props) => <DynamicPageForm {...props} />
// other view components
};
return (name) => components[name];
3

-- 3 of 17 --

}
Register layout provider
The Management module requires two layouts: CMSApplicationFrame and CMSToolAreaLayout. CMS
provides them by using withCMSLayout to wrap a layout provider:
Configure layout provider
import { withCMSLayout } from "@com.mgmtp.a12.cms/cms-management";
import { FrameFactories, type FrameViews } from "@com.mgmtp.a12.client/client-core";
export const MainPage = (): React.JSX.Element => {
const rootRegionRef = React.useMemo(() => [], []);
const RegionUi = React.useMemo(() =>
FrameFactories.regionUiProvider(rootRegionRef), [rootRegionRef]);
// const layoutProvider = your layoutProvider here...
return <RegionUi {...regionUiProps} layoutProvider={withCMSLayout(layoutProvider)}
/>;
};
In case your ApplicationFrame has custom frameLayoutProps, the customization should be extracted
and passed again into withCMSLayout
Configure layout provider with custom frameLayoutProps
export const MainPageWithCustomFrameLayout = (): React.JSX.Element => {
const rootRegionRef = React.useMemo(() => [], []);
const RegionUi = React.useMemo(() =>
FrameFactories.regionUiProvider(rootRegionRef), [rootRegionRef]);
// const frameLayoutProps = your custom frame layout props here...
const frameLayoutProps = customFrameLayoutProps;
const layoutProviderWithCMS: FrameViews.LayoutProvider = React.useMemo(
() => withCMSLayout(layoutProvider, frameLayoutProps),
[frameLayoutProps]
);
return <RegionUi {...regionUiProps} layoutProvider={layoutProviderWithCMS} />;
};
Setup Viewer module
Register element library
To use the Viewer module, register the necessary element libraries in your application.
4

-- 4 of 17 --

Configure appsetup.ts
import { ViewerElementLibrary } from "@com.mgmtp.a12.cms/cms-viewer";
import { ElementLibraryRegistry } from "@com.mgmtp.a12.contentengine/contentengine-
core";
export function setup() {
// Register element libraries
ElementLibraryRegistry.get().addEntry(ViewerElementLibrary.get());
// other configurations
}
Register initial action
The viewer need to call to the server to get the initial pages to render the viewer. It could be done
along with model graph fetching or separately. Here is the example of how to do it after logging in:
Fetch pageGraph and add Page Modules
import { createBrowserHistory } from "history";
import { all, put, call, takeLatest, type SagaGenerator } from "typed-redux-saga";
import { UaaActions } from "@com.mgmtp.a12.uaa/uaa-authentication-client";
import { ModelGraph } from "@com.mgmtp.a12.dataservices/dataservices-access";
import { createPlatformServerModelLoader } from "@com.mgmtp.a12.client/client-
core/modelLoader";
import { ConnectorLocator, type RestServerConnector } from
"@com.mgmtp.a12.utils/utils-connector";
import {
PageGraph,
ContentViewerActions,
PathRegistryProvider,
ContentViewerFactories,
ContentViewerDeepLinkFactories
} from "@com.mgmtp.a12.cms/cms-viewer";
import {
ModelActions,
ApplicationActions,
NotificationActions,
ApplicationFactories,
type ApplicationModel,
ModuleRegistryProvider
} from "@com.mgmtp.a12.client/client-core";
export function setup() {
// other configurations
return ApplicationFactories.createApplicationSetup({
model: ProjectAppModel,
dataHandlers: [
5

-- 5 of 17 --

/** can be empty unless project has specific data loading logic */
],
modelLoader: createPlatformServerModelLoader(),
// other application configurations...
customSagas: [
// other sagas...
watchLoggedInSaga
]
});
}
// export for root application component to synchronize URL history
export const appHistory = createBrowserHistory({ window });
function* watchLoggedInSaga(): SagaGenerator<void> {
yield* takeLatest(UaaActions.loggedIn, function* () {
try {
yield* put(ApplicationActions.setBusy(true));
const serverConnector =
ConnectorLocator.getInstance().getServerConnector() as RestServerConnector;
// Fetching page graph along with model graph
const { modelGraph, pageGraph } = yield* all({
modelGraph: call(() =>
serverConnector.fetchData(ModelGraph.build(true)).then((r) =>
r.json() as Promise<ModelGraph>)
),
pageGraph: call(PageGraph.build)
});
// Initialize PathRegistryProvider
PathRegistryProvider.get().initialize(pageGraph.rootPages);
// Set page graph
yield* put(ContentViewerActions.setPageGraph(pageGraph));
const moduleRegistry = ModuleRegistryProvider.getInstance();
// Add module for each response page
pageGraph.rootPages.forEach((page) => {
moduleRegistry.addModule(ContentViewerFactories.createPageModule(page,
pageGraph.metaAppModels));
});
// Add not found module
moduleRegistry.addModule(ContentViewerFactories.createNotFoundModule());
// Add custom deep link module
moduleRegistry.addModule(
ContentViewerFactories.createDeepLinkModule({
6

-- 6 of 17 --

locationManager:
ContentViewerDeepLinkFactories.createLocationManager(appHistory),
deepLinkCoder: ContentViewerDeepLinkFactories.createDeepLinkCoder(
ContentViewerDeepLinkFactories.createDefaultPageCoder()
),
applyTriggers: [ModelActions.setModelGraph,
ContentViewerActions.setLink]
})
);
// Set regular model graph and release busy state
yield* all([put(ModelActions.setModelGraph(modelGraph)),
put(ApplicationActions.setBusy(false))]);
} catch (e) {
const error = e as Response;
const body = error;
yield* put(
NotificationActions.add({
severity: "error",
title: { key: "server.connection.failed" },
message: { key: "any", defaults: { en: JSON.stringify(body,
undefined, 2) } }
})
);
throw error;
}
});
}
Setup Server side
Import Management models
The Management module exposes the necessary models to be imported in the server side via npm
packages that need to be added into application.properties:
Import Management models
mgmtp.a12.dataservices.initialization.import.models.path=...,file:${<path-to-
management-models>}/models
Please replace <path-to-management-models> with the actual path to the Management models inside
node_modules folder, e.g. <root>/node_modules/@com.mgmtp.a12.cms/cms-management/resources
Add scan packages
Add com.mgmtp.a12.cms to the scanBasePackages array within the @DataServicesApplication
7

-- 7 of 17 --

annotation. This ensures that Spring will scan the specified package for components,
configurations, and services.
Add scan package
import org.springframework.boot.SpringApplication;
import com.mgmtp.a12.dataservices.DataServicesApplication;
@DataServicesApplication(scanBasePackages = { "com.mgmtp.a12.dataservices",
"com.mgmtp.a12.contentengine", "com.mgmtp.a12.cms" })
public class ProjectServerApplication {
public static void main(String[] args) {
new SpringApplication(ProjectServerApplication.class).run(args).start();
}
}
Features
WARNING This section is under development.
RequestSelectorMap
NOTE This API is marked as experimental. Breaking changes might happen even in minor
releases.
The RequestSelectorMap can be used to customize the requests of the default data providers of the
Management modules. The modules internally uses a default variant of this map, but you can
provide your own implementation containing your customizations. Then it will be used in place of
the default one.
For example, customizing the way the Data Provider loads a single document can look like this:
/**
* Here, requests for loading documents are customized with another parameter.
* Instead of reusing the default, you could also create your own.
*/
export const CustomRequestSelectorMap: RequestSelectorMap = {
...DefaultRequestSelectorMap,
loadDocument(config) {
return (state) => {
const defaultRequest =
DefaultRequestSelectorMap.loadDocument(config)(state);
return {
...defaultRequest,
params: {
8

-- 8 of 17 --

...defaultRequest.params,
additionalParam: "customValue"
}
};
};
}
};
/**
* Then you can now use the `CustomRequestSelectorMap` when creating the
Pages/Fragments modules for the CMS.
* <project>/client/src/modules/index.ts
*/
export const ALL_MODULES = [
// other modules...
ContentManagementFactories.createPagesModule({ requestSelectorMap:
CustomRequestSelectorMap }),
ContentManagementFactories.createFragmentsModule({ requestSelectorMap:
CustomRequestSelectorMap })
];
Note that it is mandatory to spread the DefaultRequestSelectorMap when customizing.
This customization approach can also be used in combination with the RequestFilter API (described
in the Data Services documentation), for example to use your own operation methods. Customizing
the RequestFilter alone would not be enough when the method replacement needs some context
(e.g. only overriding methods in certain conditions). Using the RequestSelectorMap could then be
used to provide this context down for the filter to use.
Injecting model roles
Data Services requires every persisted model to carry a roles annotation in its header
(header.annotations with name: "roles"); without it the server refuses to persist documents for that
model.
Most CMS models already ship with their roles annotation. The document models, however, are
shipped without one. Importing them into a server as-is causes Data Services to reject writes to
them, so the roles annotation must be added to the document models before the server imports
them.
Required steps
1. Add a workspace-conversion step to the server build that runs a WorkspaceConverter over
the CMS models (see Injecting roles with a WorkspaceConverter).
2. Set the model import path to the converted output instead of the raw node_modules models.
3. Use the role mapping in Roles expected on the CMS models as the starting point for the
converter.
9

-- 9 of 17 --

The following sections explain why the annotation cannot be committed into the document model
source, and walk through each step.
Why roles are not part of the document model source
Why only the document models? Data Services builds the runtime document models by joining the
page document models along their inheritance hierarchy (BasePageDM → AbstractPageDM →
ContentPageDM, …). The relationship and UI (form / overview / tree) models are not joined this way,
so they keep their roles annotation in source as usual — only the joined document models are
affected.
The join step refuses to merge a roles annotation and aborts the whole conversion if it finds one in
the source of a joined model:
Error while joining document models:
Roles (annotations with name 'roles') will not be joined. (CM_AbstractPageDM)
That is why the roles annotation cannot live in the source of a document model, and has to be
added after the join has produced the runtime models.
NOTE
This is a current A12 platform limitation, not a permanent contract. The kernel
deliberately does not join the roles annotation today; it will remain this way until
roles becomes a first-class model-header structure instead of an annotation
(tracked upstream, not yet scheduled). Until then, the converter-based injection
described below is the supported approach.
Injecting roles with a WorkspaceConverter
A WorkspaceConverter is a Java class that the workspace conversion runs over the models. Because
the conversion runs it after the model-join, it can safely add the roles annotation that could not be
committed to the source.
The example below is the converter the CMS showcase ships. Two things are meant to be adapted
per project: the @WcfConverter(order = ...) value (kept high, e.g. 9000, so the converter runs after
the join) and the role mapping — the MANAGER_ONLY_IDS set and the default in convert() (see Roles
expected on the CMS models):
// The order must be high enough to run AFTER the workspace conversion has joined
// the document models. Injecting roles into the raw source models instead would
// make the model-join step fail with "Roles (annotations with name 'roles') will
// not be joined".
@WcfConverter(
order = 9000,
name = "addCmsRoles",
description = "Inject CMS role annotations on all runtime models")
public class AddCmsRolesConverter implements WorkspaceConverter {
private static final String ROLES = "roles";
10

-- 10 of 17 --

private static final String CMS_MANAGER = "CMS_contentManager";
private static final String CMS_MANAGER_READER =
"CMS_contentManager,CMS_contentReader";
// Models that only the content manager may read; everything else is also
// readable by the content reader role.
private static final Set<String> MANAGER_ONLY_IDS = Set.of(
"AssetDM",
"PageAssetRM_Asset____generated",
"PageAssetRM_Page____generated"
);
private final JsonMapper mapper = new JsonMapper();
@Override
public Workspace convert(Workspace workspace) {
for (ModelTuple tuple : workspace.getModels().values()) {
ObjectNode root = (ObjectNode) mapper.readTree(tuple.getContent());
ObjectNode header = (ObjectNode) root.get("header");
if (header == null) {
continue;
}
String id = header.path("id").asText();
String rolesValue = MANAGER_ONLY_IDS.contains(id) ? CMS_MANAGER :
CMS_MANAGER_READER;
ArrayNode annotations = header.has("annotations")
? (ArrayNode) header.get("annotations")
: header.putArray("annotations");
boolean hasRoles = false;
for (JsonNode node : annotations) {
if (ROLES.equals(node.path("name").asText())) {
hasRoles = true;
break;
}
}
if (!hasRoles) {
annotations.addObject().put("name", ROLES).put("value", rolesValue);
}
tuple.setContent(mapper.writeValueAsString(root));
}
return workspace;
}
}
The class is compiled into a jar (custom-converters.jar) with dataservices-wcf-api and base-model-
api on the compile classpath.
The workspace-conversion CLI (com.mgmtp.a12.dataservices.wcf:dataservices-wcf-cli) then runs
with the converter jar passed via --converters (-c). <wcf-cli-classpath> is the CLI jar plus its
11

-- 11 of 17 --

dependencies (the showcase resolves this through a Gradle wcfCli configuration — see the TIP
below). The CLI takes the models shipped in node_modules as input and writes the converted models
to a server-owned directory:
java -cp <wcf-cli-classpath> com.mgmtp.a12.dataservices.wcf.WcfCli \
node_modules/@com.mgmtp.a12.cms/cms-management/resources/models \
<output-dir> \
-c <custom-converters>.jar
The converted models are then imported instead of the raw node_modules path — set
mgmtp.a12.dataservices.initialization.import.models.path to <output-dir> (see the Import
Management models setup). The converted models carry the roles annotation and can be persisted
by Data Services.
TIP The CMS showcase wires this exact step into its server Gradle build (stageRawModels →
convertModels → copyConvertedModels), which serves as a working reference.
Roles expected on the CMS models
Only the document models are shipped without a roles annotation and therefore need the
converter. The relationship and UI (form / overview / tree) models already carry their roles in
source and are listed here only for completeness. The converter shipped with the showcase applies
exactly this mapping:
Model Type roles
AssetDM document CMS_contentManager
PageAssetRM_Asset____generated document CMS_contentManager
PageAssetRM_Page____generated document CMS_contentManager
AbstractPageDM, BasePageDM,
ContentPageDM, CustomPageDM,
DynamicPageDM, ExternalPageDM,
FragmentDM, ProjectDM
document CMS_contentManager,CMS_contentReader
PageAssetRM relationship CMS_contentManager
PageAbstractPageRM,
ProjectAbstractPageRM
relationship CMS_contentManager,CMS_contentReader
all form, overview and tree models form / overview /
tree
CMS_managementApplicationUser
The roles map to CMS authorization as follows: CMS_contentManager may read and write content,
CMS_contentReader may only read it, and CMS_managementApplicationUser is required to use the
Management UI models. These values are meant to be adapted to the consuming project’s
authorization model.
12

-- 12 of 17 --

API Documentation
API documentations can be found here:
• Management Type Definitions API documentation
• Utility Server Connector API documentation
• Content Management API documentation
• Content Viewer API documentation
Breaking Change Management
For the general definition of breaking and non-breaking changes in the A12 platform, as well as the
frontend and backend perspectives, see the A12 Breaking Change Management page.
The following section describes how this general definition is interpreted for the Content
Management System (CMS).
Public API Surface
Each CMS package (cms-management, cms-management-types, cms-viewer, cms-server-connector, cms-
services-utils, cms-model-migration) defines its public API through the exports in its index.ts. Any
removal of, or incompatible change to, an exported symbol is a breaking change. Additions to the
public API are non-breaking.
Exports annotated @experimental may change in minor releases without being considered breaking.
Exports annotated @internal are not part of the public API and carry no stability guarantee. The
public API surface is tracked per package via API Extractor (index.api.md); an unintended diff there
signals an API change that must be classified.
Content Model Format
CMS content models are versioned. Only changes that are incompatible with existing models are
breaking: for example, removing a required field or changing the semantics of an existing field.
Adding an optional field is non-breaking.
The cms-model-migration tool upgrades stored content models. It applies to the persisted-model
format only; it does not cover code-level breaking changes in the client packages. When the model
format changes, the affected release carries a matching entry in the Migration Instructions chapter,
and the upgrade is performed via the Migration Tool.
Content Engine Integration
CMS builds on the Content Engine. Changes that require updates in a consumer’s Content Engine
element modules, custom elements, or content model configuration follow the Content Engine
breaking-change rules and are breaking for CMS consumers when they surface through the CMS
13

-- 13 of 17 --

public API.
Non-Breaking Examples
The following types of changes are consistently treated as non-breaking in the CMS:
• Adding a new optional configuration field to an existing API
• Adding an optional field to the content model (a migration step still ships, but existing models
keep working)
• Introducing a new @experimental export
• Internal refactoring under src/internal/ that does not affect public exports
• Bumping a transitive dependency with no visible API impact
Migration Instructions
2026.06
0.12.0
React peer dependency raised to 19.2
Tooling: none — manual change.
The minimum supported version of react and react-dom in the peerDependency has been raised
from ^19.0.0 to ^19.2.6. The consuming package.json must be updated accordingly:
{
"dependencies": {
"react": "^19.2.6",
"react-dom": "^19.2.6"
}
}
Redux 5 upgrade
Tooling: none — manual change.
The CMS client artifacts were upgraded to Redux 5. An application that embeds the CMS
management or viewer and shares a Redux store with it must align its dependencies accordingly:
{
"dependencies": {
"redux": "^5.0.1",
"react-redux": "^9.2.0",
"redux-saga": "^1.4.2"
14

-- 14 of 17 --

}
}
The official Redux 2.0 / Redux 5 migration guide covers the store and middleware changes. Action
creators built with typescript-fsa now interoperate with Redux 5 through
@com.mgmtp.a12.client/typescript-fsa-redux-5-compat, so no action-creator changes are required on
the consuming side.
Management document models are shipped without roles
Tooling: none — manual change to the server setup.
The document models shipped under node_modules/@com.mgmtp.a12.cms/cms-
management/resources/models no longer carry a roles header annotation. Data Services requires that
annotation to persist documents, so importing these models directly (see Import Management
models) causes the server to reject writes to the CMS document models.
The roles annotation must be added to the models before Data Services loads them, by running a
small WorkspaceConverter over the imported models as part of the server build. The Injecting model
roles chapter shows the exact converter the CMS showcase uses and how to invoke it, and describes
setting mgmtp.a12.dataservices.initialization.import.models.path to the converted output instead
of the raw node_modules path.
2025.06-ext2
0.9.0
Remove the /pagegraph endpoint
Thanks to the new Data Services Query API, the old /pagegraph endpoint has been removed along
with the related com.mgmtp.a12.cms/cms-core Java package. On the client side, the only change
required is to replace the server connector call with a PageGraph.build directly, as follows:
// Previous
import { PageGraph } from "@com.mgmtp.a12.cms/cms-server-connector";
const { modelGraph, pageGraph } = yield* all({
modelGraph: call(() => serverConnector.fetchData(ModelGraph.build(true)).then((r)
=> r.json() as Promise<ModelGraph>)),
pageGraph: call(() => serverConnector.fetchData(PageGraph.build()).then((r) =>
r.json()) as Promise<PageGraph>)
});
// Now
import { PageGraph } from "@com.mgmtp.a12.cms/cms-viewer";
const { modelGraph, pageGraph } = yield* all({
15

-- 15 of 17 --

modelGraph: call(() => serverConnector.fetchData(ModelGraph.build(true)).then((r)
=> r.json() as Promise<ModelGraph>)),
pageGraph: call(PageGraph.build)
});
2025.06
CAUTION Please have a look at Migration to latest A12 chapter for an explanation of
general steps.
0.8.0
Migrate to ESM
The npm artifact @com.mgmtp.a12.cms/cms-viewer, @com.mgmtp.a12.cms/cms-server-connector,
@com.mgmtp.a12.cms/cms-management, @com.mgmtp.a12.cms/cms-management-types were migrated from
CommonJS to ESM. When using Node 22.12+ and modern build tools, no bundler-setup changes
should be necessary.
NOTE When tests depend on mocking/stubbing the CMS or CE API, consult the
documentation of the respective test runner on how to work with ES modules.
Migrating the consuming application to ESM is not required, but recommended. Consult the
documentation of the respective bundler for specifics.
Update JavaScript output to ES2024
The javascript output of the npm artifacts was updated from ES2020 to ES2024 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
React 19 upgrade
Support for React 18 and older was dropped; React 19 is now required as a peerDependency. This
requires performing the React 19 migration, which is described in great detail in the official React
19 Upgrade Guide. Its codemods should make the transition straightforward.
Additionally, a Redux update was intentionally left out to minimize the migration effort. The "react-
redux" library does not declare React 19 as a peerDependency, but still works fine with it. One
solution is to override the dependency in the consuming package.json, and to update the
corresponding "@types/react-redux" typings to at least 7.1.34.
{
"overrides": {
"react-redux": {
"react": "^19"
}
16

-- 16 of 17 --

},
"devDependencies": {
"@types/react-redux": "^7.1.34"
}
}
Styled-components v6 upgrade
Support for styled-components v5 was dropped; v6 is now required as a peerDependency. Refer to
the styled-components guide and Widgets migration notes for more information.
Migration Tool
Unlike other engine model migrations, the models in CMS are stored inside the running server
instead of being persisted in the project repository. Therefore, migrating these models requires a
different approach.
To migrate those models, first install or update the CMS migration tool:
npm install --save-dev @com.mgmtp.a12.cms/cms-model-migration
Then run the following command to perform the migration
npx cms-model-migration --api-endpoint <api-endpoint> --auth-method <method>
--username <username> --password <password>
Where:
• <api-endpoint> is your server’s API endpoint (usually ends with /api).
• <method> can be basic or header.
If using the basic method, provide the <username> and <password> of a user with at least MODEL_READ
and MODEL_UPDATE permissions.
If using the header method, omit --username and --password, and instead specify --auth-header with
the value of the Authorization request header to be used in subsequent requests.
Use the --help (alias -h) flag to view all available options.
Examples
npx cms-model-migration --api-endpoint http://localhost:9090/api --auth-method basic
--username admin --password admin
17

-- 17 of 17 --

