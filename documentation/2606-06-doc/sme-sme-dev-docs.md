# sme sme dev docs

SME (Module) Development
This documentation gives a brief overview over the SME architecture and
provides a guide on how to use the SME Core to create a new SME module.
Architectural Overview
[Architectural Overview]
• The SME Core provides the basic SME functionality. The core is independent of both SME
Modules and the SME App.
• SME Modules add the functionality to display, model, upload, … one model and its sub
elements. They must implement the SMEModule interface or extend the DefaultSMEModule
class. Modules depend on the Core, but not on the SME App. Furthermore, they can depend on
each other in the same way their models depend on each other. For this, a module may provide
functionality for other modules. Reusable or provided functionality of a module is located in the
api folder of a module. Only functionality in this folder may be reused by other modules, that is,
it is not allowed that a module A uses functionality of a module B that is located in a different
folder than the api folder of B as depicted in the architectural overview. In case a module needs
some functionality of another module and this relation coincides with the relation between the
corresponding models, the reusable functionality should be moved to the api folder. Common
functionality which is neither module-independent nor can be assigned to a single module
should be placed in the Module Commons. All modules are allowed to depend on Module
Commons.
• The SME App configures the core and registers the modules. Thus, the app may depend on both
modules and core.
A12 Extensions
The architecture blocks are reflected in the folder structure of the src folder. Besides the app, core,
and modules folders, there is an additional a12Extension folder. This folder contains code that is not
SME-specific, but serves as an extension to other A12 products (for example, Client, Kernel, etc.).
This code might be moved into those products in the future as it could also be beneficial for other
A12-based projects.
Middleware and Saga Action Flow
[SME Action Flow]
The SME uses the Form Engine to implement most of the UI components. Whenever a UI
component receives user input, it dispatches an action. While handcrafted react UI components can
dispatch the corresponding actions directly, we need an additional translation from form engine
events. This translation as well as reacting on actions that only affect the UI state of the SME is
implemented as part of UI middlewares. The corresponding code is organized according to the
1

-- 1 of 16 --

corresponding UI components.
SME Features are implemented in the Logic component. Asynchronous Actions are handled by SME
Sagas while synchronous actions can be handled by SME Middlewares. All code that involves a
REST API call, Dirty Handling or that triggers display of a progress indicator is regarded as
asynchronous. After processing an asynchronous action, a response action can be dispatched that is
handled by other parts. The corresponding code is organized according to the implemented feature.
The Data component contains only Reducers and middlewares that act solely as a replacement for a
reducer. The latter can be the case if data is stored as part of an activity. The corresponding code is
implemented according to the redux state.
Creating an SME Module
To support a new type of model in the SME, the creation of an own SME module for that model is
required. To implement a new module at least the following parts must be created:
• a module class or object implementing the SMEModule interface (see Implementation)
• an explorer entry (see Explorer Entries)
We encourage the use of A12’s modeling capabilities as much as possible. Thus, using an App
Model, a Meta Model (Document Model), and an Editor Model (Form Model) for the module is
recommended. For starting the development of a new module there are templates for the meta and
editor model located in the resources/models/template folder. To adhere to the project structure the
meta and editor model should be created in a new folder for the module located in
resources/models. The remaining artifacts should be created in a new folder for the module located
in src/modules. After the module is implemented, it must be registered in the SME App.
Implementation
Only the most basic concepts are explained in this guide. There exist already a few modules, which
can be taken as template for the implementation of new modules. The following listing shows an
excerpt of the SMEModule interface.
export interface SMEModule extends Module {
determineFileTypeIfSupported(ioFile: IOFile): string | undefined;
getExplorerEntryPrototypes(): ExplorerEntry[];
createModelExplorerData(parserFiles: ParserFile[]): ModelExplorerData;
stringifyDocument(document: object): string;
...
}
Alternatively, there is also an abstract class called DefaultSMEModule which implements the
SMEModule interface and provides useful defaults.
2

-- 2 of 16 --

Supported Types
Each model is responsible to identify its supported types. Therefore, the method
determineFileTypeIfSupported() must be implemented. These types are used by the core to identify
the relevant modules used for further operations. Note that:
• The type must equal the type of the implemented ExplorerEntry (see Explorer Entries).
• The order of modules matters (see Order of Modules).
Explorer Entries
To make a new supported element appear in the explorer, an own class extending ExplorerEntry
must be implemented. The following listing demonstrates this based on an excerpt of the
AppModelEntry:
export class AppModelEntry extends ExplorerEntry implements Editable {
type = "appModel";
dirty = false;
get isDownloadable(): boolean { return true; }
...
}
Type information such as isDownloadable should be added by overriding the getter while a
property should be used for information that belongs to the instance (for example, dirty). The type
property is an exception as it is used to identify the type when mapping between pure objects that
are stored in the redux store and rich objects that are used for computations. A single object used as
prototype must be registered for each supported in the module (getExplorerEntryPrototypes()).
Import, Export, and Upload
To specify the behavior during import, createModelExplorerData must be implemented.
Different behavior for export and upload can be provided by implementing the
toFileContentFor{Export,Upload} methods.
Registration
Modules can be registered using the ModuleRegistry from the A12 Client.
Alternatively, the ModuleRegistryExtension from the SME core can be used:
import { addOrUpdateModule } from "../../core/module/moduleRegistryExtension";
import { appModelModule } from "./amModule";
addOrUpdateModule(appModelModule);
3

-- 3 of 16 --

module.hot?.accept();
Besides this, the module must be added to the following array in webpack.common.ts:
entry: {
main: [
// the order of registration must reflect the order of dependencies between the
modules
"./src/modules/documentModel/index.ts",
"./src/modules/modelGraphDiagram/index.ts",
...
]
},
This way hot module replacement (HMR) is ensured and changes are integrated immediately
without losing the state. However, there are some limitations to HMR. In case an activity for the app
model of the changed module exists, a reload is triggered and the state is lost.
Order of Modules
The order of modules matters! The first module in the list of registered modules that can support a
file, will be used to determine the file’s type.
References Inside and Between Models
Motivation
Models may depend on other models within a workspace (for example, inside the Tree Editor, you
need to specify the roles that are defined inside a Relationship Model) or depend on information
inside of the model (for example, styles). Currently, every reference is represented by the
Autocomplete Widget, which is essentially a DropDown that lets you select the possible values.
Since it is not possible to define which values should be displayed at compile time, we need to fill
these Autocomplete widgets dynamically.
Architecture
4

-- 4 of 16 --

How to Create a Reference
Inside the Document Model Editor
1. Create following Type Definition if not already done
a. Name: "Reference" (pay attention to upper- and lower-case letters)
b. Data Type: "String"
NOTE
If you are including ../core/ModelHeader.json, the correct Type Definition is already
included. Type Definitions are located in the right column of the tool. This will be
moved out of ../core/ModelHeader.json once the Kernel allows you to include pure
Type Definition Documents.
2. Assign the Type Definition "Reference" as Data Type to every field that should hold dynamic
enumeration values
3. Create a rule for each field FieldFilled(fieldX) AND CustomCondition InvalidReference
a. FieldFilled is necessary because the error field must be present in the error condition
b. Invalid Reference should be specified as error message
4. If it should be possible for you to enter new (valid) values, add the following annotation to the
field
a. ["AutoCompleteProps" : "allowAddingNewItems"] (pay attention to upper- and lower-case
letters)
WARNING We customized the deserialize method when the meta models are loaded. Type
Definitions will be turned into annotations so we can identify relevant fields
5

-- 5 of 16 --

later on. If you create an annotation on any StringType with the key
"typeDefinition" it will probably not function correctly.
Inside the Form Model Editor
1. Drag and Drop it in any section/grid/etc. you like. It will be displayed as a simple String Input
Inside the Source Code
Create an EnumValueProvider
Use the function createEnumValueProvider(canHandle: ModelPath, getValues: EnumCalculator):
EnumValueProvider to create a new EnumValueProvider
Parameter canHandle
You need to specify the ModelPath of your references here. The ModelPath may be defined as an
absolute path or a query. For example given following Document structure in the DataModeler,
where field1 and field2 are References:
• ModelPath.fromString("RootGroup/Group1/field2") is an absolute path and the
EnumValueProvider will only handle field2 inside RootGroup/Group1
• ModelPath.fromString(".field1") is a query (notice the . in front of the name) and the
EnumValueProvider will handle all paths that end with …/field1 (so 3 in this case)
• ModelPath.fromString(".Group1/field1") is also a query and the EnumValueProvider will handle
all paths that end with …/Group1/field1 (2 in this case)
WARNING
The order of EnumValueProviders matters! If you define .field1 in the first
EnumValueProvider and RootGroup/Group1/field1 in the second
EnumValueProvider, the first one matches for all 3 paths and the second one
will never be triggered. Generally you should be mindful if you use queries
and you need to ensure they are unique and are always handled the same way.
Parameter getValues
After a path matches with the canHandle parameter, the function you pass into getValues will be
6

-- 6 of 16 --

called. It has the following interface:
(path: DocumentPath, model: {}, explorerData: ModelExplorerData, state?: object) =>
DropDownItem[]
• Parameter path: It contains the DocumentPath of the reference inside the document. The
difference between DocumentPath and ModelPath is, that DocumentPath contains an index for each
path segment. This is important to allow your Reference to be inside a repeatable group.
• Parameter model: It contains the whole Model that is currently loaded or under evaluation
• Parameter explorerData: It contains all models and entries that are currently stored inside the
activity SME_EXPLORER
• Parameter state: Contains the whole redux state
WARNING
We only guarantee that the activity SME_EXPLORER exists in the state. Other
activities may exist depending on the use case, but you cannot assume that
they always do. If your EnumValueProvider depends on those other activities,
you need to implement a fallback solution.
The function should return an array of DropDownItems. It is recommended to use
createDropDownItem(value: string, label = value): DropDownItem from the core. Label should be set
if you want the form to display a different value than what is actually stored inside the model (for
example, to give you a readable value instead of an alphanumerical id).
Register EnumValueProviders in Your SME Module
1. Overwrite the getEnumValues method from the DefaultSMEModule if not already done
2. It is recommended to use following pattern:
getEnumValues(path: DocumentPath, model: {}, explorerData: ModelExplorerData):
DropDownItem[] | undefined {
const provider = resolveEnumValueProvider(path, enumValueProviderArray);
return provider?.getValues(path, model, explorerData);
}
3. Using resolveEnumValueProvider will:
a. automatically convert the DocumentPath to a ModelPath to find the corresponding
EnumValueProvider
b. handle the matching between DocumentPath and the queries defined inside
EnumValueProviderArray
4. the second parameter enumValueProviderArray is an array of type EnumValueProvider that you
have defined in the previous step and need to pass in
7

-- 7 of 16 --

Locale Synchronization
Motivation
Models usually specify supported languages in their Header as Locale (for example, de, en). In their
Content models may offer Localized Elements which you can edit depending on the defined
Locales of the Header (for example, column labels depending on the Locales de, en). Localized
Elements and Locales need to stay synchronized while you are editing the model.
The SME offers a built-in mechanism for this use case under the assumption that:
• the model conforms to the Header API of the model-api package
• the model is edited by a form engine.
Technical Design
If the Locales in the Header are changed (added, edited or deleted) all localized element
occurrences inside the Content need to be synchronized. A Middleware registers the value change
event of the Locales and dispatches ModelEditorActions.synchronizeLocales. The corresponding
Activity.DataReducer will simply query the document searching for registered Locale Interfaces
and apply the new Locale.
If a Detached Repeat is created, Localized Elements need to be created from scratch. A Middleware
registers the new Detached Repeat Event and dispatches ModelEditorActions.addLocalizedElements.
The corresponding Activity.DataReducer traverses the Meta Model looking for registered Locale
Interfaces and creates Localized Elements if necessary.
How to Register for Locale Synchronization
Simply add [EditorFeatures.localize]: true to the activity descriptor of the form engine (default
setting of the DefaultSMEModule). Delete / Leave out the property if the model should not be localized.
Meta models should include the Document Model DomainLocalizedText for every localizable
element. If this is not possible, the fields of DomainLocalizedText need to be recreated. Pay
attention to the annotations of the fields. For the locale field, the annotation {$smeAnnotation$:
locale} must be added. For the text field, the annotation {$smeAnnotation$: localizedText} has to be
present.
Deprecated: Localized Elements need to be modelled as a repeatable group containing two string
fields in the Document Model. The name of the three elements is important, by default the SME
supports following Locale Interface of the model-api package:
{
"groupKey": "labels",
"localeKey": "locale",
"textKey": "text"
8

-- 8 of 16 --

}
So in the Document Model the repeatable group needs to be named labels, the field containing the
locale should be named locale and the field containing the text should have the name text.
If all your Localized Elements follow this interface the synchronization should work out of the
box. Your module may register additional Locale Interfaces by implementing
getLocaleInterfaces(): LocaleInterface[] of the SMEModule in case it is necessary. WARNING: Follow
the default if you can decide on your own. Currently, there is effort to align the localized elements
across all modules. Thus, custom localized elements might not be supported in the future!
Notes on Deleting Empty Localized Elements
If your module decides to delete empty Localized Elements on export it also has to make sure to
add them again on import. Since the SMEAction.addLocalizedElements is also dispatched on
loading of the model and it queries the underlying meta model, it is sufficient to create an empty
object.
Keyboard Shortcuts
We use three mechanisms to register keyboard shortcuts:
• via the onKeyDown/onKeyUp callbacks of React/HTML Elements
• via the keyboardShortcuts API of the tree engine
• via a custom, annotation based solution for form engine components
Modeling Keyboard Shortcuts in Form Models
Keyboard shortcuts are directly mapped to the buttons (event and navigation buttons) that are
modelled in the form model. Our mechanism recognizes annotations directly on buttons that have
the key keyboardShortcut. For example, to trigger a button event that should be dispatched when
Control, Shift and Enter are pressed simultaneously, following annotation must be added via the
Form Model Editor:
{
"key": "keyboardShortcut",
"value": "Control + Shift + Enter"
}
Following limitations apply:
• The modifier keys must match the values defined by keycode-js, so for example Control can not
be abbreviated with Ctrl
• Only Control, Alt and Shift are supported as modifier keys
9

-- 9 of 16 --

• Modifier keys must be provided first
• The last key may not be one of the modifier keys
• Keys have to be separated by +
• Capitalization and spaces between keys are ignored
Implementation
The Form Engine View must be wrapped within FormEngineKeyboardWrapper unless you already use
EnhancedFormEngine from core.
export function FormEngineWithKeyboardShortcuts(props:
FormEngineViews.FormEngineProps) {
return (
<FormEngineKeyboardWrapper {...props}>
<FormEngineViews.FormEngine {...props} />
</FormEngineKeyboardWrapper>
);
}
Testing (End-to-End)
End-to-End tests are realized using Cypress.
Test files should be placed in cypress\integration, fixed data in cypress\testData, and support
commands in cypress\support\commands.ts with type definitions in cypress\support\commands.d.ts.
Example test using a custom command:
describe("Test basic functionality", () => {
it("should load the basic workspace with its installer-appmodel file", () => {
cy.visit("");
cy.loadWorkspaceExplorerDataFromJson(workspaceBasicExplorerData);
cy.contains(".treeWidget__nodeName a", "installer-appmodel").should("exist");
});
});
Electron
The SME uses electron to develop a cross-platform desktop app with web technologies (TypeScript,
HTML, CSS, etc.). Electron is based on Chromium and NodeJS and abstracts OS- and desktop-specific
parts from the actual app implementation. Therefore, the SME app is wrapped in Electron to create
the desktop app.
We make use of the electron-builder to package and build distribution-ready desktop apps.
10

-- 10 of 16 --

Currently, the SME is offered as Windows, Linux, and MacOS application and the result of building
the app with electron is an OS-specific executable.
Architecture of the SME Electron App
The SME Electron app is composed from three components:
• The loading screen appears when the app is started. It shows a spinner while in the
background, the server is started. When this is finished, the app shows the main window.
• The main screen shows the actual SME app in the same way it would be shown in a browser
window.
• The server runs the SME backend in a separated Java process. To achieve this, the correct path
to the Java installation must be provided in the settings file.
The app is accompanied by
• The settings.json file that contains the path to the Java call. By default this is "java", that is, it is
assumed that the java path is set in the system environment variables.
• This is also used by the installer to point to the shipped JDK
• If the app is installed manually and the path has not been set correctly, typical symptoms are
that the loading screen will remain permanently or an error dialog with "Error: spawn […] java
[…] at ChildProcess" appears. In any case, the backend log can give further information.
• sme-backend.jar contains the SME backend
• The smtsolver directory contains the solver(s) used in the backend, for example, to calculate
rule contradictions in document models.
How to build the SME App?
Building the SME app is fully integrated in gradle. Developers can call gradle package{Win, Linux,
Mac}ElectronApp for the OS that the build is triggered on. Builds for foreign OSs are currently not
supported. Instead, virtual machines or remote access to other machines, for example, via CI nodes,
must be used.
In short, the build comprises the following activities:
• Frontend and backend are built
• Loading screen, main window, and server are built
• Electron app is built
• Additional resources are integrated/copied
◦ settings.json, sme-backend.jar, smtsolver directory
◦ intermediate result located in electron/build/electron
• result is packaged: sme-app-{win, linux}.zip, sme-app-mac.tgz
◦ located in electron/build/packaged
11

-- 11 of 16 --

macOS-Specific Build Parts
Notarization
Apple has a GateKeeper that checks apps not downloaded via the AppStore. If apps are not properly
signed and notarized, MacOS complicates their execution. These apps can still be executed, for
example, via right-click/open or by deactivating GateKeeper. Apps can be notarized to avoid this.
To apply successful notarization, developers need an Apple production certificate in the keychain,
an Apple ID, and Xcode. All this is already configured in the Jenkins Mac nodes the SME uses.
Notarization is performed via electron-notarize during the electron build. It executes a
notarization script to sign the code and indicate entitlements. This is realized in /electron/build-
mac.
Translocation
Apple copies apps to mounted read-only disk images and executes them there. This is problematic,
for example, if external resources are used. To overcome this "translocation", the SME performs an
"untranslocation": On start, the SME checks whether it is translocated. If so, it removes its
quarantine attribute, and restarts itself. This is realized in /electron/mainWindow/translocation.ts.
Build Process in the CI and Manual Build Testing
Electron build & publication to Artifactory triggered when building a tag or for nightly builds of
master or release branch.
Hence, for builds of feature branches or pull requests, the electron build is not triggered by default.
To test OS-specific or Electron-related implementations, the jenkinsfile is parametrizable with the
option "ArchiveElectronBuild". The value for the option can be set in the "Build with parameters"
tab of Jenkins. Developers can choose between archiving no builds, the build for macOS, the build
for linux, or both. Currently it is assumed that the Windows build can be triggered locally, hence
this option is not selectable. On success, the artifacts of the build are archived in Jenkins and can be
downloaded from the Jenkins build page. After a while, the files are deleted together with all other
information about the build.
View the Log Files
All log messages are printed to log files. The location of these files is a folder 'log' next to the
executable of the SME Electron build.
Currently, we distinguish between three different log files - backend.log contains the log messages
from the backend - frontend.log contains the log messages from the frontend. Currently, this log
file is only prototypical. - electron.log contains all remaining log messages that mainly arise in the
configuration of electron or the management of frontend and backend as child processes
New log messages are added at the end of the log files. All files have a maximum size of 1 mb. When
a log file reaches its maximum size, its content is moved to an archived version of the file that,
again, has a maximum size of 1 mb.
12

-- 12 of 16 --

Support for Finding Bugs
Proper debugging of Electron apps with IDE debuggers is difficult, because different processes are
involved (for example, frontend, backend, electron). Therefore, we introduced logging to inspect
errors in the log files and support developers in creating (temporary) debug output that can be
inspected in the log files, too.
Moreover, developers can enable the Chromium devtools for electron by calling
win.webContents.openDevTools(). These enable the usual means for finding bugs, for example, by
inspecting the console, setting breakpoints, etc.
Enable Redux Dev Tools and Load Source Maps in
Electron
Step 1: Ensure that the Chrome DevTools are enabled, if required also in the packaged app (via
win.webContents.openDevTools() in main.ts). Alternatively, comment out the removal of the window
menu (win.removeMenu()), which will then offer the option to start the DevTools.
Step 2: Configure the Redux DevTool extension as described here. In case of Windows, the result
may be adding the following lines to main.ts:
const reactDevToolsPath = path.join( process.env.APPDATA!,
"../Local/Google/Chrome/User
Data/Default/Extensions/lmhkpmbekcpmknklioeibfkpmmfibljd/3.1.3_0" );
void app
.whenReady()
.then(async () => { await session.defaultSession.loadExtension(reactDevToolsPath)
})
.then(loadingScreen)
.then(smeWindow);
Step 3: Make the copyDist and the copySettings tasks in the electron gradle build depend on
packageDistDirQA instead of packageDistDirWebapp. Additionally, the source directory in copyDist has
to be changed to from("${parent?.buildDir}/qa/dist")
Guidelines
Focus Handling
After opening a detail activity from a master activity (model from workspace explorer; element
from document model tree) the focus should be automatically set to an element of the detail view.
13

-- 13 of 16 --

Guideline
• If the detail is implemented using the form engine:
◦ The focus is in the first focusable input element
• If the detail is implemented using the tree engine (with virtual root node):
◦ The focus is on the virtual root node of the tree if the model has elements
◦ the focus is on the ADD button if the model has no elements
Invalid Existing References
In [References inside and between models], the definition of references was described. This
approach handles invalid existing references in the same way as non-existing references: A generic
error message is shown. To provide a better user experience, the two cases should be handled
differently.
Guideline
Invalid existing references should be displayed with a specific error message describing why the
reference is invalid.
NOTE This can be achieved using custom conditions.
Non-Editable Models
In some cases models loaded by the SME cannot be edited. Currently there are following types of
problems:
• incompatible major version
• newer minor version
• older minor version (can be migrated by the SME)
• models with a critical level of invalidity (for example, error during the loading process)
• models where the id does not match the file name
Guideline
Disable the complete workspace if such issues exist and display hints listing those models and a
strategy to proceed.
Meta Model Version
The SME should be usable to model the Meta Models of the SME itself.
14

-- 14 of 16 --

Guideline
Meta Models should always be editable by at least one SME version without the need for any
migration. Moreover, no validation errors should exist after performing the workspace validation.
Build Process
Guideline: Reproducible Build Results
Executing the build several times should produce the same results.
Exception: Docker images which are provided as official artifacts should always use up-to-date base
images! Thus, the latest image can be chosen automatically.
Guideline: Do Not Modify Source Files During Build
All files that are automatically created or adapted during the build process need to be excluded
from version control.
Reason: The version for published artifacts is calculated based on the git status.
Guideline: Incremental Build
Already executed task of the build process should not be executed again, if no relevant changes
have been applied to their input files since the last execution.
Exception: If the time saving is not relevant (up-to-date check takes time too).
Use of Kernel API
The external api from kernel is designed to be used for all applications that use the kernel. Changes
to the api are documented in the changelog. Artifacts of the external api are usually contained in a
package that has api as a segment.
The a12 internal api from kernel is designed specifically for internal A12 components and should
be used wherever possible in the SME backend. It offers more functionality than the external api,
but changes are not necessarily documented in the changelog. Artifacts of the a12 internal api are
usually contained in a package that has a12internal as a segment.
The internal kernel artifacts are designed to be used within the kernel. Internal artifacts are
usually contained in a package that has internal as a segment.
Guideline
• Use the a12 internal api whenever possible. Otherwise, use the external api. Usage of kernel’s
internal artifacts should be avoided.
• If it is necessary to use an internal artifact (because the functionality is not provided via an api
yet), notify the kernel team.
15

-- 15 of 16 --

• As a rule of thumb the preferred package should always be kernel.core. If something does not
exist there, the package should always include a12internal (for example,
kernel.md.model.a12internal) or api (for example, kernel.md.model.api)
Creating Plugins
NOTE
This feature is in an experimental state and currently only custom elements for the
Content Model Editor can be registered. There is no dedicated marketplace to
retrieve plugins yet, instead projects need to build and share them manually.
Install our node CLI Tool:
npm install '@com.mgmtp.a12.sme/plugin-builder'
Use the following command to create a plugin:
npx sme-plugin-builder --entry src/index.ts --outDir lib --id my-plugin --name \"My
Plugin\" --pluginVersion 1.0.0 --type content-model-plugin --targetSmeVersion 12.2.0
For further explanations to the arguments, use npx sme-plugin-builder --help for more
information.
In your code, you must call the registering function with the code you want to register. Example for
the entry file:
import { MyLibrary } from "/.MyLibrary.js";
import { registerContentModelElementLibrary } from "@com.mgmtp.a12.sme/plugin-
builder/lib/api.js";
registerContentModelElementLibrary(MyLibrary); // Currently untyped, but must match
the interface of an ElementLibrary of the Content Engine
16

-- 16 of 16 --

