# workflows dev docs

Workflows
1. Introduction
A12 Workflows (A12WF) provides an integration of Business Process Model and Notation (BPMN)
and Decision Model and Notation (DMN) capabilities into A12, enabling both the graphical
modeling and the execution of server-side workflows.
This allows modeling the business-level behavior of entire A12 applications. The focus is on semi-
automated, long-running workflows that involve multiple roles and complex authorization rules.
Such authorization rules are defined and executed using UAA.
Integrated modeling capabilities include process steps performed by humans and machines, the
connections between them, and business rules triggering different process paths. For automated
process steps, several automations are provided out of the box, e.g., to send an email or to link two
documents.
Custom automations can be implemented against a well-defined API and then used by modelers.
Additionally, custom code can react to provided events such as a task being completed.
All of this is achieved by connecting the third-party CIB 7 process engine to the A12 stack. To this
end, A12 Workflows provides a tailored, A12-aware CIB 7 service as well as extensions for Data
Services and Client.
NOTE For modeling related information, please refer to our Workflow Modeling
Documentation.
1.1. Architecture Overview
The Workflows architecture is based on CIB 7 and an extension of Data Services.
1

-- 1 of 38 --

2. Basic Ideas
• The documents are solely stored in Data Services and not in CIB 7.
◦ CIB 7 stores only references to documents ("docRefs") and knows nothing about the data
format, storage or models.
• Data Services is the entrypoint for all requests from the client.
◦ Workflows is an opt-in extension of Data Services which registers workflows-related JSON-
RPC operations.
3. How It Works
We provide several artifacts but in short:
• Server side: our extension registers new JSON-RPC operations in Data Services for workflows-
specific functionality.
• Client side: our client-side library connects to these JSON-RPC operations.
As examples, consider the following use cases:
3.1. Start Process
1. The client sends a START_PROCESS JSON-RPC request to Data Services.
2. Our extension handles this request.
3. For this operation, no document changes are to be done.
4. The start process request is forwarded to CIB 7 and thus starting a new process of an existing
process model.
3.2. Assign Task
1. The client sends an ASSIGN_TASK JSON-RPC request to Data Services.
2. Our extension handles this request.
3. The assignee field in our metadata on the document is updated
4. The assign request is forwarded to CIB 7.
5. CIB 7 sets the assignee as well.
3.3. Loading a Task List
1. The client sends a LIST_DOCUMENTS JSON-RPC request to Data Services.
a. Under the hood, we add a filter to the query so that only documents currently referenced by
a user task are returned
2. Data Services handles this request as per usual.
2

-- 2 of 38 --

See also our list of workflows-specific operations.
4. Benefits
• Loose coupling between CIB 7 and Data Services:
◦ CIB 7 knows nothing about models or documents (except that it expects workflows
metadata).
◦ No need to deploy Document Models to CIB 7.
• CIB 7 does not store documents, only Document References:
◦ No data duplication.
◦ Simpler data migration.
• Data Services has access to relevant process metadata (everything in
WorkflowsMetadata_DM.json):
◦ Metadata is available in authorization rules.
◦ Metadata is available in events and extension points which provide the document.
• Support for external attachments and relationships.
5. Current Limitations
• No eventual consistency guarantees yet for communication between CIB 7 and Data Services.
• Document Models used for workflows still need metadata as include for now and must be
exactly named A12WF:
◦ A document can only be referenced by a single User Task at a time (because metadata is
populated directly in the document).
◦ Thus, parallel User Tasks cannot reference the same document.
• CIB 7 must be protected in production because it does not integrate UAA authorization.
6. Integration
NOTE For migration of existing projects, see migration instructions.
6.1. Artifacts
6.1.1. Backend
We publish Java libraries for integration into Spring Boot modules.
Workflows Extension
NOTE This is an extension for Data Services instances.
3

-- 3 of 38 --

Coordinates: com.mgmtp.a12.workflows:workflows-extension
• Registers the following JSON-RPC operations:
◦ START_PROCESS(processDefinition: String, businessKey: String, variables: JsonNode?)
◦ COMPLETE_TASK(docRef: DocumentReference)
◦ SEND_MESSAGE(messageName: String, businessKey: String, processInstanceId: String?,
payload: JsonNode?)
◦ ASSIGN_TASK(docRef: DocumentReference, assignee: String)
Since task documents are simply A12 documents with a partly fixed structure (metadata), we reuse
existing Data Services operations where possible:
• LIST_DOCUMENTS to load task lists.
• GET_DOCUMENT to load a task document.
• MODIFY_DOCUMENT to modify a task document.
For details, see Workflows Extension.
Workflows Engine Distribution
Coordinates: com.mgmtp.a12.workflows:workflows-engine-distribution
This Workflows Engine distribution artifact is the basis for your Workflows Engine module.
See CIB 7 for details.
6.1.2. Frontend
Workflows Core
We publish a client library that must be added to your A12 Client module.
• @com.mgmtp.a12.workflows/workflows-core: defines the necessary actions and sagas to
communicate with the backend as well as utility functions to create URLs for Workflows
endpoints + request/response definitions for those endpoints.
Activity Descriptors
The Workflows middlewares, sagas and data provider inspect and mutate the activity descriptors at
runtime. The Workflows activity descriptors have the following fields:
Field Required Meaning
view Yes Must equal "TaskList" (the
WorkflowsConstants.TASK_LIST_VIEW constant). Every
Workflows middleware, the data provider and the reload
logic ignore activities whose view is not "TaskList".
4

-- 4 of 38 --

Field Required Meaning
model Yes Document model name of the task documents. Used to
route data requests and as the model match condition of
the generated form scene.
instance No Distinguishes the two descriptor shapes. If present, it is a
task form descriptor, otherwise it is a task list descriptor.
formKey Added at runtime Not set by the modeler. Injected into the descriptor by the
addFormKeyToPushAction / addFormKeyToReplacementAction
middlewares (see below).
Task Overview
The activity descriptor for a task overview looks as follows:
{
"view": "TaskList",
"model": "<YOUR_DOCUMENT_MODEL_NAME>"
}
This triggers our data provider. You must model your App Model accordingly so that it shows a task
overview scene, see app model.
Task Form
Similarly, the activity descriptor for a task form looks as follows:
{
"view": "TaskList",
"model": "<YOUR_DOCUMENT_MODEL_NAME>",
"instance": "<YOUR_DOCUMENT_REFERENCE>" // Reference of task document to show
}
Example
{
"view": "TaskList",
"model": "SomeDocumentModel",
"instance": "SomeDocumentModel/1"
}
NOTE You do not need to add corresponding scenes to your App Model because we
generate them at runtime (based on the fetched task list).
5

-- 5 of 38 --

Runtime Requirements and Modifications
The Workflows middlewares, sagas and data provider inspect and mutate the activity descriptors at
runtime.
Overview vs. form
Selectors filter task activities by descriptor.view === "TaskList" and then split on
descriptor.instance: the single activity with instance === undefined is the task overview; activities
with a defined instance are task forms. This is how reloadTaskOverviewMiddleware finds the unique
overview to refresh and how closeCurrentFormAfterTaskCompleteMiddleware matches the form to
close (by view + instance).
formKey injection.
When a task form activity is pushed (Activity/PUSH) or replaces another activity
(Activity/CANCEL_REQUESTED), the addFormKey* middlewares look up the opened task document in the
initiating overview activity, read A12WF.TaskDefinition.FormKey (falling back to
A12WF.TaskDefinition.Key) and add it as descriptor.formKey. The same formKey is written onto the
descriptor of the matching data holder. This requires the task form activity to carry an
initiatingActivityId pointing at the overview activity whose data contains the document.
Scene generation
adaptAppModelMiddleware reacts to Activity/SET_DATA on the task overview and dispatches
ADAPT_APP_MODEL; adaptAppModelSaga then generates one App Model scene per distinct formKey in the
task list. The generated scene matches on model (equals the document model), instance (is set) and
formKey (equals the task’s form key) — the same three descriptor fields. This is why no task-form
scenes need to be modeled by hand.
Data provider query augmentation
For descriptors with view === "TaskList", workflowsDataLoader rewrites LIST_DOCUMENTS queries: it
locates the A12WF group in the document model, adds a constraint that …/A12WF/Task/ID is not empty
(so only documents representing a live task appear) and projects …/A12WF/Task/ID and …
/A12WF/TaskDefinition/FormKey. Descriptors with any other view are passed through unchanged.
NOTE These descriptor modifications rely on the A12WF metadata group being present in
the task document model.
Events
The namespace WorkflowsEvents contains the following constants:
• WorkflowsEvents.START_PROCESS
• WorkflowsEvents.CLAIM
• WorkflowsEvents.COMPLETE_TASK
• WorkflowsEvents.SEND_MESSAGE
6

-- 6 of 38 --

• WorkflowsEvents.ASSIGN_TASK
These constants store the event names used in form or overview models.
6.2. Integration Step-by-Step Guide
Navigate to the Releases Overview to catch the latest Workflows(-ext) version.
1. In your Data Services module…
a. Add the dependency:
implementation "com.mgmtp.a12.workflows:workflows-extension:<version>"
b. Add the configuration property to specify where to reach CIB 7:
mgmtp.a12.workflows.engine.client.base-url=http://<YOUR_CIB7_HOST>/engine-rest
i. Make sure to keep /engine-rest as suffix
c. Adjust Data Services configuration to activate the required operations:
mgmtp.a12.dataservices.json-rpc.allowed-
operations=<YOUR_EXISTING_VALUES>,START_PROCESS,COMPLETE_TASK,ASSIGN_TASK,SEND_M
ESSAGE,CREATE_METADATA,REMOVE_METADATA,SET_STATUS
2. In your Client module…
a. Add the dependency:
"@com.mgmtp.a12.workflows/workflows-core": "<version>"
b. Run npm install to fetch these new dependencies.
c. In your appsetup (or a specific Client module), register the following new sagas, middlewares
and data provider:
i. for a modular, default installation:
import {
ApplicationFactories,
ApplicationSetup,
ModuleRegistryProvider
} from "@com.mgmtp.a12.client/client-core/lib/core/application";
import { WorkflowsFactories } from "@com.mgmtp.a12.workflows/workflows-
core";
export function setup(): ApplicationSetup {
7

-- 7 of 38 --

ModuleRegistryProvider.getInstance().addModule(WorkflowsFactories.createModu
le());
return ApplicationFactories.createApplicationSetup({
...otherConfigurations,
overridePlatformSagas: [
otherSagas,
...WorkflowsFactories.createApplicationSagas()
]
});
}
ii. for a non-modular installation (e.g. you want to exclude certain middlewares):
A. Add the imports:
import { workflowsSagas, workflowsMiddlewares, WorkflowsFactories } from
"@com.mgmtp.a12.workflows/workflows-core";
B. The workflowsSagas goes into your overridePlatformSagas.
C. The workflowsMiddlewares goes into your additionalMiddlewares.
D. Note that workflowsMiddlewares has two optional parameters:
I. options: { formComponentName: string }
II. middlewareOptions: { includeAdaptAppModelMiddleware: boolean;
includeAddFormKeyToPushAction: boolean; includeAddFormKeyToReplacementAction:
boolean; includeReloadTaskOverviewAfterCompleteTaskMiddleware: boolean;
includeReloadTaskOverviewAfterAssignTaskMiddleware: boolean;
includeReloadTaskOverviewAfterSendMessageMiddleware: boolean;
includeReloadTaskOverviewAfterStartProcessMiddleware: boolean; }
E. Internally, we generate App Model scenes. To do so, we must know the name of your
form component as defined in your view provider. By default, we assume
"FormEngine". The second parameter middlewareOptions allows you to exclude the
following middlewares, which are included by default:
▪ adaptAppModelMiddleware
▪ addFormKeyToPushAction
▪ addFormKeyToReplacementAction
▪ reloadTaskOverviewAfterTaskCompleteMiddleware
▪ reloadTaskOverviewAfterAssignTaskMiddleware
▪ reloadTaskOverviewAfterSendMessageMiddleware
▪ reloadTaskOverviewAfterStartProcessMiddleware
F. WorkflowsActions contains actions you can dispatch or handle
8

-- 8 of 38 --

G. We provide no React components. WorkflowsFactories module contains
taskOverviewActionsMiddleware, which includes the logic for handling the Overview
Engine Events onRowClicked, onRowButtonClicked and onEventButtonClicked.
▪ If these are the only event handlers you need, no custom task overview
component is necessary anymore.
▪ Therefore your view provider does not need to return
<OverviewEngineFactories.ViewComponent {…props} key="task-overview" /> as
the task overview component.
▪ Your App Model has to be adapted as well: Set OverviewEngine as name in the
VIEW_ADD directive instead of TaskOverview. For more information have a look
at how to model the Overview Scene.
▪ If you don’t want to adapt your App Model, your view provider should return
<OverviewEngineFactories.ViewComponent {…props} key="task-overview" /> as
the task overview component.
▪ If you have custom event handlers in your task overview, you can delete any
default handlers to let our taskOverviewActionsMiddleware handle them. You can…
▪ Either keep a custom React component and register custom event handlers
there. Then your view provider needs to register this custom component and
your App Models must reference its view name for task overviews.
▪ Or use the Overview Engine’s events API as we do and move custom event
handlers to middleware, then remove your custom overview component.
▪ We recommended you to entirely drop your custom task overview component
and the respective registering with the view provider. For any other custom event
handler we recommend you to move them to a dedicated middleware.
iii. for a setup using the Composable Client API
A. We provide withWorkflows to integrate both the sagas and middlewares in one go.
B. Alternatively, you can also integrate the sagas and middlewares separately with
withWorkflowsPlatformSagas and withWorkflowsMiddlewares.
3. In your document models…
a. Add WorkflowsMetadata_DM.json (Right-click → Save as…) as an include to all document
models used in a workflow.
b. The include must be called exactly A12WF but can be on any level.
7. Workflows Extension
7.1. Workflows Extension Configuration
NOTE See also secure configuration suggestions for production.
Since this is an extension for Data Services, please also refer to Data Services configuration.
9

-- 9 of 38 --

For the Workflows Extension itself, we offer the following configuration options:
Configuration Property Default Value Remarks
mgmtp.a12.workflows.automatic-
synchronization.enabled
false Synchronize A12 document
fields to CIB 7 automatically.
Alternatively, you may disable
this and use the
syncAvailableFieldsDelegate to
control when data is synced
(e.g. for performance reasons).
7.1.1. Object Mapper Configuration
The Workflows Extension requires a com.fasterxml.jackson.databind.ObjectMapper containing the
com.fasterxml.jackson.module.kotlin.KotlinModule. It registers this automatically.
WARNING Please avoid overriding the object mapper bean. If you do have to, remember
to add the Kotlin module. Otherwise, metadata serialization may fail.
7.2. Configuration Profiles
We offer the following configuration profiles for the Workflows extension.
7.2.1. Enable Automatic Synchronization
Enables automatic synchronization of fields annotation with availableInProcessAs from the
Workflows extension to the process engine.
Profile Name
workflows-automatic-sync
Profile Contents
mgmtp.a12.workflows.automatic-synchronization.enabled=true
7.3. Events
We publish the following Spring events to hook into our RPC operations.
7.3.1. TaskBeforeAssignEvent
This event is fired before a Task is assigned to a user and before the corresponding task document
is even loaded.
10

-- 10 of 38 --

The event is fired with the following parameters:
TaskBeforeAssignEvent(DocumentReference docRef, String assignee)
You can handle the event as follows:
@EventListener
public void handleTaskBeforeAssignEvent(TaskBeforeAssignEvent event) {
// do something
}
Use cases
• Prevent assignment of the Task.
• Run custom code before Task assign.
• Monitoring, tracing, logging
7.3.2. TaskAfterAssignEvent
This event is fired after a Task has been successfully assigned to a user (in sync mode).
The event is fired with the following parameters:
TaskAfterAssignEvent(DocumentReference docRef, String assignee)
You can handle the event as follows:
@EventListener
public void handleTaskAfterAssignEvent(TaskAfterAssignEvent event) {
// do something
}
Use cases
• Run custom code only after successful Task assign.
• Monitoring, tracing, logging (for only succeeded operations)
7.3.3. TaskBeforeCompleteEvent
This event is fired before a Task is completed and before the corresponding task document is even
loaded.
The event is fired with the following parameters:
11

-- 11 of 38 --

TaskBeforeCompleteEvent(DocumentReference docRef)
You can handle the event as follows:
@EventListener
public void handleTaskBeforeCompleteEvent(TaskBeforeCompleteEvent event) {
// do something
}
Use cases
• Prevent completion of the Task.
• Run custom code before Task completion.
• Monitoring, tracing, logging
7.3.4. TaskAfterCompleteEvent
This event is fired after a task has been successfully completed (in sync mode).
The event is fired with the following parameters:
TaskAfterCompleteEvent(DocumentReference docRef)
You can handle the event as follows:
@EventListener
public void handleTaskAfterCompleteEvent(TaskAfterCompleteEvent event) {
// do something
}
Use cases
• Run custom code only after successful Task completion.
• Monitoring, tracing, logging (for only succeeded operations)
7.3.5. ProcessBeforeStartEvent
This event is fired before a process is started, i.e., before the request is forwarded to CIB 7 (in sync
mode).
The event is fired with the following parameters:
ProcessBeforeStartEvent(String processDefinitionKey, String businessKey, JsonNode
12

-- 12 of 38 --

variables)
You can handle the event as follows:
@EventListener
public void handleProcessBeforeStartEvent(ProcessBeforeStartEvent event) {
// do something
}
Use cases
• Prevent process start (e.g. run custom validations).
• Run custom code before process start.
• Monitoring, tracing, logging
7.3.6. ProcessAfterStartEvent
This event is fired after a process has been started, i.e. after the process has been started in CIB 7 (in
sync mode).
The event is fired with the following parameters:
ProcessAfterStartEvent(String processDefinitionKey, String businessKey, JsonNode
variables)
You can handle the event as follows:
@EventListener
public void handleProcessAfterStartEvent(ProcessAfterStartEvent event) {
// do something
}
Use cases
• Run custom code only after successful process start.
• Monitoring, tracing, logging (for only succeeded operations)
7.3.7. ProcessBeforeMessageSendEvent
This event is fired before the Message is sent to the process instance.
The event is fired with the following parameters:
ProcessBeforeMessageSendEvent(String messageName, String businessKey, String
13

-- 13 of 38 --

processInstanceId, JsonNode payload)
You can handle the event as follows:
@EventListener
public void handleProcessBeforeMessageSendEvent(ProcessBeforeMessageSendEvent event) {
// do something
}
Use cases
• Prevent sending of the message (e.g. custom validation).
• Run custom code before message send.
• Monitoring, tracing, logging
7.3.8. ProcessAfterMessageSendEvent
This event is fired after a Message has been sent to a process instance (in sync mode).
The event is fired with the following parameters:
ProcessAfterMessageSendEvent(String messageName, String businessKey, String
processInstanceId, JsonNode payload)
You can handle the event as follows:
@EventListener
public void handleProcessAfterMessageSendEvent(ProcessAfterMessageSendEvent event) {
// do something
}
Use cases
• Run custom code only after successful message send (or successful persistence of the outbox
command to send the message).
• Monitoring, tracing, logging (for only succeeded operations)
7.4. Java Client
We provide a tiny artifact workflows-extension-client which provides constants for our RPC method
names via WorkflowsOperationConstants.
It can be used in conjunction with Data Services' RpcOperationsClient and RequestBuilderFactory to
invoke our RPC operations:
14

-- 14 of 38 --

requestBuilderFactory
.newJsonRpc2RequestBuilder()
.addMethodCall(WorkflowsOperationConstants.START_PROCESS_OPERATION)
.id(...)
.putParameter(...)
.putParameter(...)
.build()
8. CIB 7
CIB 7 is a third-party process engine we tailor and use to execute BPMN process models.
We also provide a tailored version of its model editor, the Camunda Modeler.
See CIB 7 Docs: Introduction for more info.
8.1. CIB 7 Configuration
Configuration options for CIB 7 are inherited from different levels as broken down below.
8.1.1. Configurations from Spring
CIB 7 is a Spring Boot service. So you can use regular Spring Boot configuration, such as:
• spring.*
• logging.*
• server.*
8.1.2. Configurations from CIB 7
For all configuration options and defaults of CIB 7, please refer to the Camunda documentation.
We set the following defaults to override CIB 7 defaults:
Configuration Property Default Value Remarks
camunda.bpm.history-level AUDIT Camunda history level
camunda.bpm.history-
level.default
AUDIT Camunda default history level if
camunda.bpm.history-level is
set to AUTO and a level can not
be determined
spring.jackson.date-format yyyy-MM-dd’T’HH:mm:ss To conform to A12 date format
camunda.bpm.generic-
properties.properties.enforceH
istoryTimeToLive
false Makes Camunda’s mandatory
time-to-live property optional
15

-- 15 of 38 --

NOTE The CIB 7 Spring properties still contain the word camunda.
8.1.3. Configurations from UAA
Authentication
A12 CIB 7 integrates UAA authentication.
Please refer to the UAA documentation for all configuration options.
For our suggestions, see secure configuration for CIB 7.
Authorization
A12 CIB 7 currently does not integrate UAA authorization.
8.1.4. Configurations for A12WF Delegates
For our provided delegates, we offer the following configuration properties:
Configuration Property Default Value Remarks
mgmtp.a12.workflows.engine.del
egates.email.host
- The host address of your email
server
mgmtp.a12.workflows.engine.del
egates.email.port
- The email server port to use
mgmtp.a12.workflows.engine.del
egates.email.user
- Email server username
mgmtp.a12.workflows.engine.del
egates.email.password
- Email server password
mgmtp.a12.workflows.engine.del
egates.email.from
- 'From' address of the email
mgmtp.a12.workflows.engine.del
egates.email.start-tls.enabled
- Use TLS? Must be true or false.
mgmtp.a12.workflows.engine.del
egates.email.login.enabled
- Attempt login? Must be true or
false.
mgmtp.a12.workflows.engine.del
egates.email.keep-alive-
timeout
- Set the 'Keep-Alive' timeout in
seconds. During this time,
connection to the email server
is not closed.
8.1.5. Configurations for A12WF-Specific Features
Data Encryption
CIB 7 can be configured to publish events which allow hooking in logic to encrypt Process Variables.
To enable these events to implement encryption, apply the workflows-variable-encryption profile.
16

-- 16 of 38 --

• Encryption must be done during serialization of Process Variables before they are persisted in
the database.
• Decryption must be done on deserialization.
See also serialization events.
8.1.6. Configuration Profiles
We offer the following configuration profiles for CIB 7:
UAA Configuration
UAA defaults for CIB 7
Profile Name
workflows-uaa
Profile Contents
mgmtp.a12.uaa.authentication.context-path=/engine-rest
Enable Variable Encryption
Enables event publication on variable serialization and deserialization to enable injecting
encryption logic. See Process Variable Serialization Events.
Profile Name
workflows-variable-encryption
Profile Contents
mgmtp.a12.workflows.engine.variables.encryption.enabled=true
Enable Actuator Endpoint
Enable actuator endpoint in CIB 7
Profile Name
workflows-actuators
17

-- 17 of 38 --

Profile Contents
management.endpoints.enabled-by-default=true
management.endpoints.web.exposure.include=*
management.endpoints.web.exposure.exclude=
management.endpoint.health.showDetails=always
management.health.defaults.enabled=true
management.health.ldap.enabled=false
Allow Unsafe Deployment in CIB 7
Disables UAA for a number of endpoints to allow deployment of BPMN models to CIB 7 from the
Modeler. This should only be used for local development.
Profile Name
workflows-allow-unsafe-deployment
Profile Contents
mgmtp.a12.uaa.authentication.unsecured.urls=/${spring.jersey.application-
path}/deployment,\
/${spring.jersey.application-path}/engine/*/deployment,\
/${spring.jersey.application-path}/deployment/create,\
/${spring.jersey.application-path}/engine/*/deployment/create,\
/${spring.jersey.application-path}/engine/*/version,\
/${spring.jersey.application-path}/version
8.2. CIB 7 Events
CIB 7 publishes events that can be listened to by projects. The following sections provide a list of
available events and how to use them.
8.2.1. Process Variable Serialization
Event Name Event Parameters Description
ProcessVariableBeforeSerializa
tionEvent
variable: String Event is published before
variable serialization and
before variable is stored in the
database.
ProcessVariableAfterDeserializ
ationEvent
variable: String Event is published after
variable deserialization and
after variable is retrieved from
the database.
18

-- 18 of 38 --

Example
Example below demonstrates how projects can listen to ProcessVariableBeforeSerializationEvent
and ProcessVariableAfterDeserializationEvent using @EventListener annotation.
@Component
class VariableEncryptionEventListener {
@EventListener
fun handleEncryption(event: ProcessVariableBeforeSerializationEvent) {
//handle encryption
}
@EventListener
fun handleDecryption(event: ProcessVariableAfterDeserializationEvent) {
//handle decryption
}
}
9. Good Practices
9.1. Minimize Dependencies
In your Workflows Engine module, try to minimize your A12 dependencies (particularly to Data
Services).
For instance, it must not include a dependency to dataservices-core as it can cause Spring bean
configuration clashes.
You should carefully evaluate which dependencies are really necessary.
9.2. Event Handlers for Cross-Sectional Task Listeners
If you find yourself adding the same task listener on every User Task to run code on some task
lifecycle event (create, update, complete, assign, delete), you may want to make it a backend event
handler.
We publish Spring events in our JSON-RPC operations in workflows-extension that you use for this.
For all events and code snippets, see Workflows Extension - Events.
10. Deployment
NOTE For deployment, please be aware of the Helm A12 Stack.
19

-- 19 of 38 --

10.1. Artifacts
Please see our list of artifacts.
10.2. Workflows Extension
10.2.1. Secure Configuration
See secure configuration in Security section.
10.2.2. Clustering
The workflows-extension should not affect the clustering setup of Data Services. It primarily adds
stateless RPC operations.
10.3. CIB 7
10.3.1. Requirements
• CIB 7 must not be accessible from the outside (see Security)
◦ Only Data Services must be able to access CIB 7
10.3.2. Secure Configuration
Not supported, see secure configuration in Security section.
10.3.3. Clustering
CIB 7 Process Engine
CIB 7 allows clustering by distributing the process engine across multiple nodes which access a
shared database.
NOTE For details, please refer to the Camunda documentation on performance and
20

-- 20 of 38 --

scalability, in particular the section titled "Clustering".
CIB 7 Job Executor
In CIB 7, the Job Executor is responsible for executing any asynchronous continuations which are
triggered during process execution. Asynchronous continuations can be triggered e.g. by
transaction boundaries or timer events.
NOTE For details about the Job Executor, please refer to CIB 7’s official documentation.
CIB 7 differentiates between homogenous and heterogeneous deployments. Homogenous means that
the same process applications are deployed on all nodes.
NOTE For details about these variants, please refer to cluster setups for the Job Executor.
In short, in case of heterogeneous cluster, you must set the following Spring config:
camunda.bpm.generic-properties.properties.jobExecutorDeploymentAware=true
NOTE
The above configuration is equal to setting <property
name="jobExecutorDeploymentAware">true</property> in XML as shown in the
Camunda documentation.
10.4. Monitoring
Both CIB 7 and the Workflows Extension (Data Services) provide Micrometer for monitoring.
NOTE CIB 7 provides additional monitoring capabilities, see Camunda monitoring
documentation.
10.5. Recovery
When crashed or restarted, CIB 7 will automatically continue execution from the last state
("savepoint") committed to the database.
Savepoints include wait states such as user tasks or timer events. Additionally, savepoints can be
modeled in BPMN via asynchronous continuations.
NOTE For details, please refer to transactions in CIB 7.
10.5.1. Backups
Please refer to Camunda’s recommendations regarding backups.
21

-- 21 of 38 --

10.6. Auditing
Regarding logging, please refer to secure logging configuration for CIB 7.
11. Security
11.1. General
For an overview of recommended security measures for A12-based projects, please refer to the
overall security documentation.
11.1.1. Terminology
Regarding terminology, we refer to two layers where security may be configured:
• Service/product level: Spring Boot configuration, authorization rules, user setup, …
• Infrastructure/network level: firewalls, proxies, ingress configuration on Kubernetes, access-
or block-listing, …
11.2. GDPR
• The fields initiator and assignee are stored in Data Services, in our Document Metadata, and
transferred to CIB 7 on process start.
• The field initiator is stored in the CIB 7 database. Projects have the option to encrypt data in CIB
7.
• Projects can decide which data can be synchronized between A12 and CIB 7. Data Encryption in
this case is also possible.
11.3. Workflows Extension
11.3.1. Authorization Scopes
Each JSON-RPC operation requires certain user permissions.
We define the following scopes in workflowsAuthorizationDefinition.json:
TaskAssign Check that the user has access right to assign
tasks. #resource is "AssignTaskOperation.rpc",
docRef and assignee are available in the UAA
decision context.
TaskComplete Check that the user has access right to
complete tasks. #resource is
"CompleteTaskOperation.rpc", docRef is
available in the UAA decision context.
22

-- 22 of 38 --

SendMessage Check that the user has access right to send
messages. #resource is
"SendMessageOperation.rpc", messageName,
processInstanceId, payload, and businessKey
are available in the UAA decision context.
StartProcess Check that the user has access right to start
processes. #resource is
"StartProcessOperation.rpc",
processDefinition, businessKey, and variables
are available in the UAA decision context.
GetProcessVariableUpdates (used by
syncAvailableFieldsDelegate
Check that CIB 7 has access right to get
process variables manually by a Service Task.
#resource is "GetProcessVariableUpdates.rpc",
documentReferences is available in the UAA
decision context.
CreateMetadata (used internally) Check that CIB 7 has access right to create
Workflows metadata in a document.
#resource is "CreateMetadataOperation.rpc",
docRef and metadata are available in the UAA
decision context.
RemoveMetadata (used internally) Check that CIB 7 has access right ro remove
Workflows Metadata in a document.
#resource is "RemoveMetadataOperation.rpc",
docRef is available in the UAA decision
context.
By default we look for the following access rights on the user for authorization:
• TASK_ASSIGN
• TASK_COMPLETE
• SEND_MESSAGE
• START_PROCESS
• GET_PROCESS_VARIABLE_UPDATES (only CIB 7 should have this right)
• CREATE_METADATA (only CIB 7 should have this right)
• REMOVE_METADATA (only CIB 7 should have this right)
WARNING
If you set child-authorization-definitions in Data Services, include
classpath:/uaa/workflowsAuthorizationDefinition.json or make sure that you
define proper policies for all our scopes. See UAA docs: Permission Hierarchy
for more information about child-authorization-definitions and UAA docs:
Authorization Configuration on how to to register child-authorization-
definitions files.
23

-- 23 of 38 --

11.3.2. Secure Configuration
Basics
First, read and apply secure configuration for Data Services.
Authentication
For the technical communication between Data Services and CIB 7, we recommend:
• CERTIFICATE mode because certificates are a secure way to represent technical users (i.e., Data
Services and CIB 7).
• Alternatively, you may use your IDP as a secure way to store technical users. In this case,
consider creating a separate Realm for technical users (e.g. to enforce different password
policies than for human users).
For details, please refer to UAA authentication.
Authorization
1. In your mgmtp.a12.uaa.authorization.child-authorization-definitions, please include our child
authorization definition:
mgmtp.a12.uaa.authorization.child-authorization-
definitions=classpath:uaa/workflowsAuthorizationDefinition.json,
<YOUR_ADDITIONAL_AUTHORIZATION_DEFINITION>
2. Set proper authorization rules for all our UAA scopes.
For details, please refer to UAA authorization.
11.4. CIB 7
11.4.1. Secure Configuration
CIB 7 is currently not secure. Therefore, it must be protected from any external access on
infrastructure level.
Authentication
For authentication, the same recommendations apply as for the Workflows Extension.
Authorization
WARNING Currently, CIB 7 does not integrate UAA authorization.
For all configuration options, see CIB 7 configuration.
24

-- 24 of 38 --

11.4.2. Endpoints
CIB 7 has an extensive REST API as well as an admin UI called CIB 7 Cockpit.
Exposing these unsecured endpoints in production would be a huge security issue.
WARNING Therefore, CIB 7 must be protected from any external access on
infrastructure level.
Only Data Services needs access to CIB 7.
If you don’t use CIB 7 Cockpit for development, you may also remove it completely by excluding the
transitive dependency to org.cibseven.bpm.springboot:cibseven-bpm-spring-boot-starter-webapp-
core.
11.4.3. Logging
CIB 7 logs on the INFO level are GDPR compliant, with the exceptions mentioned in GDPR.
11.4.4. Upgrading & Patching
A12WF major releases integrate the latest CIB 7 release.
Projects must align their CIB 7 version to the version used in their A12WF release.
11.4.5. Cockpit
If you do not need the CIB 7 Cockpit in production, disable it by removing the cibseven-bpm-spring-
boot-starter-webapp dependency from your deployment. If you do need the CIB 7 Cockpit, make
sure to do one of the following:
• Define a default admin user in your configuration files.
• Use an identity management system and configure it to be used by the Cockpit.
11.5. Summary
• Protect all endpoints from any outside access.
• Use INFO log level in CIB 7 or anonymize your logging
• Disable the cockpit or configure a Cockpit admin user/identity management system.
12. Process Migration
12.1. Overview
CIB 7 offers three approaches to migrate processes:
• Java API
25

-- 25 of 38 --

• REST API
12.1.1. Avoiding Process Migration
Not every change to a BPMN model (i.e. process definition) requires process migration.
How to Avoid Migration
Instead, it is also possible to use different versions of a process definition in parallel. Of course, this
requires that the underlying application is able to handle both versions.
Process instances running under the old definition will just keep running on their version, whereas
new process instances automatically use the latest process definition.
When to Avoid Migration
Running different process definitions in parallel is advised if there are legal reasons or the
migration is too complicated and the effort outweighs the benefits.
When to Migrate
On the other hand, process migration is the preferred approach if the new process definition
contains only patches or bugfixes, or when the operational complexity of running parallel process
definitions is to be avoided.
12.2. Migration Steps
A process migration can be divided into two steps:
• Mapping tokens ("active User Tasks") to the new model
• Migrating the data
As basis for the below sections we will use the following process:
26

-- 26 of 38 --

12.2.1. Mapping Tokens
A migration plan defines the mappings between tokens of the source and the target process
definition.
This migration plan can be executed on a given set of process instances. Options include whether
custom listeners and input/output mappings of Tasks should be executed during the migration.
In the example below we deployed a second version of our base process, removing the User Task
"Enter String B" and adding a User Task "Enter String C". We now want to migrate all tokens from
Enter String B to Enter String C.
• Process Migration Example
NOTE For specific mapping rules of different BPMN elements, check out the CIB 7 docs. Be
aware of the validation rules for these mappings.
12.3. Migrating Data
For scenarios that require data migration, there are several approaches.
12.3.1. Migrating Data Automatically
If process data can be migrated without user input, you can automate the entire migration.
You must provide your migration logic via custom Java code using the CIB 7 API.
Below you see an example for a migration that does not operate on different process definitions,
but instead simply adjusts some variables in the Task scope. This could e.g. happen if a small bugfix
has to be deployed, where variables have to be renamed or added. Of course, this migration can be
combined with the above example to have both a token and data migration.
• Data Migration Example
27

-- 27 of 38 --

12.3.2. Migrating Data With User Input
If you require user input for your data migration, the situation is more complex.In this case, you
can use the migration island approach.
Migration Island
In the migration island approach, the updated process definition is extended by a non-reachable
User Task and a data manipulating Service Task. This Service Task executes your Java delegate that
is responsible for adapting the data as necessary. After that, it leads into a User Task outside of the
migration island.
On migration, tokens are mapped from the source Task(s) onto the non-reachable User Task. This
can be done in batches with CIB 7’s migration plan API.
Pros
• Simplicity
• User Task "Migration Task" could be used for non-automated migration scenarios that require
user input.
Cons
• The new process is dirtied by unrelated BPMN elements, and you may require multiple
migration islands to migrate multiple User Tasks.
28

-- 28 of 38 --

12.4. Migration examples
12.4.1. After Removing a User Task
Let’s suppose we have WorkAndReviewProcess:1, which contains two User Tasks: DoWork and
ReviewWork. Our business requirements have changed and we would like to remove User Task
ReviewWork. Then, we need to create WorkAndReviewProcess:2 without the second User Task.
The migration plan for this use case will have following structure:
val migrationPlan = runtimeService
.createMigrationPlan(sourceProcessDefinition.id, targetProcessDefinition.id)
// Maps tokens of 'Review work' to 'Do work' _Task_
.mapActivities("ReviewWork", "DoWork")
.build()
val processInstanceIds = runtimeService
.createProcessInstanceQuery()
.processDefinitionKey("WorkAndReviewProcess")
.list()
.map { process -> process.processInstanceId }
runtimeService
.newMigration(migrationPlan)
.processInstanceIds(processInstanceIds)
.execute()
12.4.2. After Changing Gateway Condition
Let’s suppose our model includes a Gateway with specific conditions. Our business requirements
have changed and we would like to change conditions of our Gateway.
This change is covered by CIB 7’s mapEqualActivities:
29

-- 29 of 38 --

MigrationPlan migrationPlan = processEngine
.getRuntimeService()
.createMigrationPlan(sourceProcessDefinition.id, targetProcessDefinition.id)
.mapEqualActivities()
.build()
// Execute migration...
For more details please check out the CIB 7 docs.
12.4.3. After Changing BPMN events
For this case, please refer to the CIB 7 docs.
12.5. Upgrading Your A12 Version
Upgrading to a new breaking A12 release line may require BPMN model changes and therefore
migration.
NOTE Please be aware of our BPMN migration tool.
Please refer to the corresponding release pages for upgrade instructions as usual.
12.6. Versioning Delegates
When migrating BPMN models, it might become necessary to run multiple versions of delegates in
parallel.
As CIB 7 picks up delegates by name, it is necessary to differentiate between the different delegate
versions by name as well. For this, you could add a version suffix to your delegate names.
12.7. Further Resources
• CIB 7 docs:
◦ Process instance migration
◦ Process instance modification
• Camunda docs:
◦ Versioning process definitions & Understanding process migration
13. Glossary
30

-- 30 of 38 --

Term Description
Decision table (from
DMN)
See CIB 7 Docs: DMN Table for more info.
Delegate Delegates are pieces of code you can call from Service Tasks to be
executed at a certain point in the process, passing process variables as
input.
Delegate Expression Delegate expressions are used for identifying the specific Delegate to be
invoked (an expression for the delegate name).
Document Reference
(docRef)
Unique identifier for a document, e.g. Contract/143be3e9-32bc-4d57-a155-
ba1f89eba93a.
Element Template Element templates are a way to extend the Camunda Modeler with
domain-specific diagram elements, such as service and user tasks.
Applying an element template configures the diagram element with pre-
defined values for BPMN properties, input/output mappings, and
extension properties.
Event Events in BPMN represent things that happen. A process can react to
events (catching event) as well as emit events (throwing event).
For example, a catching message event makes the token continue as soon
as a message is received.
Gateway See CIB 7 Docs: Gateways for more info.
Payload All non-metadata fields in a Document Model.
Task Tasks are the basic elements of BPMN processes. These are atomic units
of work composed to create a meaningful result.
Task document A document which has task-level metadata filled in (/A12WF/Task and
/A12WF/TaskDefinition).
Process A process defines steps which should happen in a specific order or flow.
The steps which make up a process are Tasks, Gateways, Events and
Subprocesses.
Process document A document which has process-level metadata filled in (/A12WF/Process
and /A12WF/ProcessDefinition).
Subprocess Subprocesses are element containers that allow defining common
functionality. This allows you to create a "process within a process" that
can be called multiple times.
Workflows metadata
(or metadata)
All fields contained in our WorkflowsMetadata_DM.json model.
14. Automatic Migration
Since 2024.06, A12 components provide artifacts for automatic code migration where possible, to
31

-- 31 of 38 --

make migrations faster and less error-prone.
Since we provide backend as well as frontend modules, we are using Codemod for frontend
migration.
14.1. Migrating with Codemod/Hypermod
For our frontend module, we provide Hypermod transformers via the artifact
com.mgmtp.a12.workflows/workflows-codemod.
You can run the transforms of this artifact with the following command (might be different
depending on your CLI implementation):
npx @com.mgmtp.a12.workflows/workflows-codemod@<workflows-version> <path-to-your-frontend-
sources> --targetVersion <version>
For 2025.06, <workflows-version> is 12.0.0 and the corresponding <version> is 12.
14.1.1. Transforms
A12 Workflows 12.0.0
The workflows-codemod@12.0.0 will do the following changes:
• Removes TaskOverviewDataProvider defined as new TaskOverviewDataProvider() or
taskOverviewDataProvider and inserts the Workflows specific Data Loader as argument into
Overview Engine’s Data provider.
◦ Adjusts the imports respectively.
◦ TaskOverviewDataProvider does not exist any more and Workflows Data Loader in
combination with Overview Engine’s Data provider enables showing tasks on the Workflows
specific TaskList.
A12 Workflows 11.0.0
The workflows-codemod@11.0.0 will do the following changes:
• Replacing all imports for the workflows-access library with imports for workflows-core
◦ The workflows-core library now contains all contents from the workflows-access library, see
also A12 Workflows frontend integration
14.2. BPMN Model Migration Tool
For BPMN migration, we provide an artifact which can be run via CLI.
It is necessary to specify BPMN file or directory to migrate, and also provide at least the current
model version you are using.
32

-- 32 of 38 --

14.2.1. Options
There are a couple of options to use with the migration tool:
• -b or --backup: Creates a backup (.bak file) before migrating a model
• -d or --debug : Enables debug mode, which prints migration infos of steps
• -h or --help: Prints the command line options
• -n or --no-validation: Skips validation of the model against schema definitions after migration
(not recommended)
• -o or --optional: Enables optional migration steps, enable by listing the names of the steps
• -t or --test: Prints migrated model to stdout instead of to a file
• -smv or --source-model-version: Version to start migration from [mandatory]
• -tmv or --target-model-version: Version until migration shall be run including the specified
version. [optional]
14.2.2. How to Run
The following will migrate SetDocumentFieldDelegate.bpmn which is located in the directory in which
the command is executed, creates a backup and performs migration from model version 10.0.0
until the latest model version available:
java -jar workflows-model-migration-tool-<VERSION>-jar-with-dependencies.jar
SetDocumentFieldDelegate.bpmn -b -smv 10.0.0
NOTE
You can also migrate multiple BPMN files. In this case, please provide the path to a
folder containing those files.
java -jar workflows-model-migration-tool-<VERSION>-jar-with-
dependencies.jar your/bpmn/file/path -b -smv 10.0.0
15. Infrastructure Dependencies
In the table below, the infrastructure dependencies required by workflows are listed.
Depende
ncy
Purpose Supporte
d
Versions
Configuration
Reference
Minimum Resource
Recommendation
Notes
CIB7 Process
orchestration and
execution
2.2.0 Configuration and
secure setup.
None Includes
updates
from
Camunda
7.23.0 CE
33

-- 33 of 38 --

16. Breaking Change Management
A12 Workflows follows the A12 Breaking Change Management process, which is described in detail
in Breaking Change Management.
17. Migration Instructions
CAUTION
Please have a look at Migration to latest A12 chapter for an explanation of
general steps on how to upgrade before starting with the component migration
to the latest release line.
17.1. 2026.06
17.1.1. Breaking Changes
For some breaking changes, we offer OpenRewrite recipes, Codemod transforms, and workflows
model migration tool. Feel free to check them out as they might help you to upgrade Workflows
easier (see Automatic Migration).
CIB 7 / Process Engine
• CIB 7 has been upgraded from 2.0.0 to 2.2.0.
• You must follow the official CIB 7 migration guides for each version step, in order. This is
required in all cases, as each step includes mandatory database updates:
◦ CIB 7 update guide 2.0 to 2.1
◦ CIB 7 update guide 2.1 to 2.2
• The property cibseven.webclient.authentication.jwtSecret is required now and needs to be set
to a secret with a minimum length of 155 characters, base64 encoded.
Workflows Extension
• The property mgmtp.a12.workflows.automaticSynchronization.enabled has been renamed to
mgmtp.a12.workflows.extension.automaticSynchronization.enabled.
17.2. 2025.06
17.2.1. Breaking Changes
For some breaking changes, we offer OpenRewrite recipes, Codemod transforms, and workflows
model migration tool. Feel free to check them out as they might help you to upgrade Workflows
easier (see Automatic Migration).
34

-- 34 of 38 --

Overall Changes
• Camunda was replaced by the CIB 7 fork of Camunda 7 (see CIB 7 website) as Camunda is
dropping the support for the Camunda 7 Community Edition
◦ The module workflows-camunda has been renamed to workflows-engine to be vendor
unrelated.
◦ The process engine is basically still the same, and all BPMN and DMN models should work as
before.
◦ All Camunda dependencies were replaced by their CIB 7 equivalents.
◦ If you customize the Camunda engine in your projects with code, you will need to migrate to
the corresponding CIB 7 packages.
◦ See CIB 7 migration instructions from Camunda 7.23 to CIB 7 2.0.
▪ Note that you must first do the minor upgrade from Camunda 7.21 to Camunda 7.23 as
mentioned on the linked page.
▪ Migration to CIB 7 webclient is not necessary, because we currently deactivated the CIB 7
UI and only expose the old Camunda UI.
• Support for Java 17 was dropped. Workflows now only supports Java 21.
• The module workflows-shared was dropped, as it did not fulfill its original purpose anymore. Its
contents have been integrated into workflows-engine and moved to internal packages. To
migrate, you will have to replace the functionality provided by the module. As the affected code
is now internal, changes may not be communicated in migration notes in the future.
◦ Affected classes:
▪ com.mgmtp.a12.workflows.service.rpc.data.JsonRpcRequestIds
▪ com.mgmtp.a12.workflows.service.rpc.data.JsonRpcRequestParams
▪ com.mgmtp.a12.workflows.service.rpc.DataServicesRpcFacade
▪ com.mgmtp.a12.workflows.service.rpc.DataServicesRpcUtil
▪ com.mgmtp.a12.workflows.service.rpc.JsonRpcRequestBuilder
▪ com.mgmtp.a12.workflows.service.rpc.JsonRpcRequestBuilderFactory
• The properties WorkflowsInternalDocumentStatus and DueDate in the data classes
com.mgmtp.a12.workflows.shared.dtos.ProcessMetadata and
com.mgmtp.a12.workflows.shared.dtos.Task are deprecated. These properties and their respective
fields in our WorkflowsMetadata_DM.json will be removed in the next breaking release.
• The property InputDocumentModels in the data class
com.mgmtp.a12.workflows.shared.dtos.ProcessDefinition has been removed, since it is not used
anymore in our new Workflows architecture. Our WorkflowsMetadata_DM itself has not been
changed.
• The property CreationDate in the data class com.mgmtp.a12.workflows.shared.dtos.Task is not
nullable anymore, because it is always set in our CreateMetadataOperation class. Our
WorkflowsMetadata_DM itself has not been changed.
• We moved from CommonJS to ESM for our frontend libraries workflows-core and workflows-
35

-- 35 of 38 --

codemod as part of an overall A12 effort.
• Version upgrades (A12 wide)
◦ Spring Boot 3.4.5
◦ Spring Framework 6.2.6
◦ Spring Security 6.4.5
◦ Typescript 5.8.2
Workflows Engine (previously Camunda)
• The executable main class CamundaApplication has been renamed to ProcessEngineApplication.
• The event ProcessVariableBeforeDeserializationEvent has been renamed to
ProcessVariableAfterDeserializationEvent, as the Java deserialization has already been
performed when the event is published.
• The CreateDocumentDelegate does not validate the created document anymore.
• JS scripting is not supported natively in CIB 7. We removed the respective dependencies from
workflows-engine.
◦ Groovy scripting remains supported.
◦ If you need JS scripting, please see the CIB 7 Documentation.
• The path / redirects to the new CIB 7 webapp which is not yet supported by A12 Workflows. To
access the supported legacy Camunda Cockpit, use the path /camunda.
Configuration Changes
The following configuration properties have been renamed to be vendor unrelated:
Old property key New property key
mgmtp.a12.workflows.camunda.delegates.email.
host
mgmtp.a12.workflows.engine.delegates.email.ho
st
mgmtp.a12.workflows.camunda.delegates.email.
user
mgmtp.a12.workflows.engine.delegates.email.us
er
mgmtp.a12.workflows.camunda.delegates.email.
password
mgmtp.a12.workflows.engine.delegates.email.pa
ssword
mgmtp.a12.workflows.camunda.delegates.email.
from
mgmtp.a12.workflows.engine.delegates.email.fr
om
mgmtp.a12.workflows.camunda.delegates.email.
startTls.enabled
mgmtp.a12.workflows.engine.delegates.email.st
artTls.enabled
mgmtp.a12.workflows.camunda.delegates.email.
login.enabled
mgmtp.a12.workflows.engine.delegates.email.lo
gin.enabled
mgmtp.a12.workflows.camunda.delegates.email.
port
mgmtp.a12.workflows.engine.delegates.email.po
rt
36

-- 36 of 38 --

Old property key New property key
mgmtp.a12.workflows.camunda.delegates.email.
keepAliveTimeout
mgmtp.a12.workflows.engine.delegates.email.ke
epAliveTimeout
mgmtp.a12.workflows.camunda.client.baseUrl mgmtp.a12.workflows.engine.client.baseUrl
mgmtp.a12.workflows.camunda.variables.encry
ption
mgmtp.a12.workflows.engine.variables.encrypti
on
Workflows Engine Modeler Extension
• The Element Templates have been updated to use Groovy instead of JS.
◦ To migrate, update all Element Templates to the latest version in your process models.
NOTE The Camunda Modeler enables you to update the applied Element Template when
selecting the respective BPMN element in the process model.
Workflows Extension
• The property mgmtp.a12.workflows.extension.set-status-event-publishing.enabled has been
removed.
◦ The SetStatusOperation used in the setStatusDelegate cannot be configured to publish the
Data Services events DocumentBeforeUpdateEvent, DocumentAfterRepositoryUpdateEvent, and
DocumentAfterUpdateEvent anymore.
• Data Services removed the spring profile dataservices-embedded_jms. Workflows now offers the
spring profile workflows-embedded_jms to enable embedded Artemis service.
• AssignTaskOperation, CompleteTaskOperation and RemoveMetadataOperation are now
using Data Services' service-level Java API to avoid nested transactions and potentially
unexpected rollback behaviour
◦ Logging is different now.
◦ No DataServicesDocument is added to the OperationContextHolder any more.
• The locale parameter for AssignTaskOperation and CompleteTaskOperation has been
removed, as it was only used in dispatched events. To migrate, adjust your calls to these RPC
operations accordingly, and adjust your event listeners to not rely on the locale parameter.
• RemoveMetadataOperation no longer triggers validation after removing metadata and saving
the document.
• Error handling for all existing JSON-RPC operations has been improved.
◦ The root cause is now visible.
◦ Debug log now explicitly states if a JSON-RPC operation succeeded or failed.
Workflows Extension Client
• The operation constant GET_PROCESS_VARIABLE_UPDATES_OPERATION has been moved to internal
package.
37

-- 37 of 38 --

◦ This constant was marked for internal use only, so ideally there is no migration effort on
your part. It is still mentioned here, as it was not officially internal.
Workflows Core
• To consolidate with the A12 Project Template, the expected form component name in
workflowsMiddlewares has been changed from "FromCRUD" to "FormEngine" (as project template
defines it).
• To simplify code and remove the need for a custom task overview component,
WorkflowsFactories module now contains taskOverviewActionsMiddleware, which includes the
logic for handling the Overview Engine Events onRowClicked, onRowButtonClicked and
onEventButtonClicked.
◦ Have a look at our Integration Step-by-Step Guide, on how to drop your custom task
overview component.
• TaskOverviewDataProvider has been dropped. Instead, use WorkflowsFactories.createDataLoader()
as input when creating Overview Engine’s Data Provider.
◦ This Data Loader is able to differentiate between Workflows specific TaskList and standard
overview, which shows documents not including Workflows Metadata.
◦ Covered by automatic migration
• Actions, Sagas, and middlewares that relate to AssignTaskOperation and
CompleteTaskOperation had the locale parameter removed, see the Workflows Extension
migration notes. To migrate, adjust any calls correspondingly.
38

-- 38 of 38 --

