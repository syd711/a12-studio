# data_distribution datadistribution documentation

Data Distribution
Introduction
A12 DataDistribution (A12 DD) is a component which provides capabilities that data between nodes
can be synchronized. A node can be a browser or a server process. This functionality can be used to
store data in the client (browser) so that operations can be done faster (as no round trip to the
server is required), or to work offline, in case all required data is already available in the client. The
solution is already used in production with more than 10.000 clients and 1.000 server nodes.
The A12 DataDistribution component consists of client and server libraries. The component itself is
not providing REST endpoints or a A12 based client. It is only providing functionality, which can be
used in the server or client in order to enable the data synchronization.
The data provided by the application has to be send to the DataDistribution server component in
order to make the data available there. From there, the data is synchronized to the client. Changes
made on the client are synchronized with the server, which relays the changes to the application
through messages. Data can be created or updated on either the client or the server. All changes are
synchronized with the respective other communication partner. The data can be stored locally
encrypted. The client application can still communicate directly with various services in the
backend.
The following key features are available:
• Data Distribution is a technical service used to distribute data and propagate changes across
servers and clients
• It is provided as an A12 Component which can be included in an A12 project as a library
• Focus on scaling and offline capabilities
• Can be used device independent
• DataDistribution is just a transport layer
• Application or clients submit data (payload) for distribution
• Applications / clients receive events about data updates (including the payload)
The power of using synchronization rather than track individual changes is that in case the
synchronization is blocked for a longer time, only the latest version of each changed document is
synchronized. If e. g. a field, like price is updated on a document 10 times in between, only the
latest state (latest price) is synchronized with the client in case the data connection is back. This is
reducing the amount of data to the minimum necessary.
DataDistribution is NOT a distributed database (no query capabilities)
The approach of data synchronization enables several use cases that are helpful beyond just offline
availability:
• Initial Synchronization: When logging in for the first time on a client that hasn’t received any
1

-- 1 of 26 --

data yet, all necessary data for the current user is loaded into the browser’s indexedDB. From
there, the data is periodically compared with the server’s data repository when online. The user
can log in from a different browser and once again load the data with an initial
synchronization.
• Delta synchronization: After an initial synchronization has been performed, the data is
compared using a delta synchronization. It doesn’t matter whether the client is online or the
connection was interrupted for several weeks.
• Backup: In this case, the server can also be seen as a "backup medium." As long as an online
connection exists, the most up-to-date data is always stored on the server. In the event of a
computer crash, the data can be retrieved from another computer via the server, and work can
be resumed.
• Push mechanism: The data synchronization approach allows sending data from the server to
the client (push). This informs the client about new data, and it can react accordingly (e.g.,
through a modal dialog).
• Data exchange between clients: Through the synchronization of data between the client and
the server, communication between two clients is also possible (via a shared server). This
enables a different approach, such as optimistic locks, where a user is informed about activities
of other users.
• Event-driven architectural pattern: This design pattern has the advantage that the client
reacts to data that is changed in the background. For example, this allows for modal dialogs in
the case of changes from "external" sources or updating displayed lists.
• Additional information: In addition to JSON documents, further information can be
"appended." For instance, comments, notes, tags, or log information can be attached to a data
record. These pieces of information are concatenated in the backend across different clients and
are not overwritten individually. For example, notes about a current case can be created by
multiple users. These notes are concatenated together on the server and can be viewed by other
users as well.
• Attachments: Documents can be provided with attachments, such as PDF files or images. These
attachments can then be loaded by the client application from a server for display or stored
locally on the mobile device in encrypted form (storage can be implemented using service
workers).
• Responsiveness: Due to holding the data on the client, no loading times are necessary when
switching views (e.g., from a master view to a detail view). Since the data is already present on
the client, no requests to the server are required. The pages are displayed significantly faster
and more visibly.
• Deferred loading: It’s also possible to load data that was excluded by the specified filter during
synchronization at a later time.
To accurately represent data changes from multiple users or changes in both the client and the
server for the same data record, version vectors are employed. These version vectors enable
conflicts to be automatically resolved or identified as true conflicts that need resolution by a user.
Additionally, in the frontend, finer-grained changes, such as at the field level, can be captured and
merged using version vectors.
By consistently implementing the event-driven architectural pattern, asynchronous processing can
2

-- 2 of 26 --

also be supported in the backend, facilitating the transmission of data from third-party systems
(such as GeCo) to the client. This approach also supports an Offline-First architecture and, in
conjunction with Service Workers, Progressive Web App (PWA) applications.
If offline operation is supported, it’s important to consider that certain validations and state
transitions (e.g., completing tasks) must be feasible while offline. By utilizing A12 validation rules
and calculations, it becomes possible to execute the same logic both in the client and on the server.
This approach enables validation rules and state transitions even in offline scenarios.
The offline data storage in the client’s IndexedDB is often limited by the browser on mobile devices.
If the decision is made to use the A12 DataDistribution libraries, a detailed analysis should
determine whether a limitation of IndexedDB primarily exists on mobile devices, and potentially,
an external form of persistence needs to be considered.
If the prerequisites for exclusive online operation cannot be met or are not achieved quickly
enough, the proposed approach offers a solid solution. Additionally, it brings forth further
advantages and safeguards against situations where internet connectivity might unexpectedly
become unavailable on-site. As part of an architectural decision, we recommend documenting the
functional and non-functional requirements (including data volume estimates) in detail and
defining the specific implementation approach.
You can find a simple example of how the data is synchronized: Example
DD_ENTRY
The main entity for data synchronization is the dd_entry data type.
Get started
Central questions before implementing data distribution:
• What is the maximum amount of data to be synchronized?
3

-- 3 of 26 --

• Which clients need to be serviced, and is the data volume manageable in terms of transmission
and processing on the client side?
• What entities do I have, and how are they related to each other?
• How is visibility established for the data, users, org units, etc.?
Example of data synchronization
In the following scenario it is illustrated how the DataDistribution component can be used. The first
picture shows teh A12 client server communication without DataDistribution. The communication
to the server components is done synchronously with REST:
In order to use the DataDistribution components a client library and server library have to be
integrated. The client library is synchronizing the data with the Redux Store. From there the data
can be used in the client. For the server, the REST end points and JMS integration have to be defined
(see demo application). The data from A12 DataServices has to be imported in the DataDistribution
(e. g. the models and documents which should be handled by the DataDistribution component). The
DataDistribution will take care of synchronizing the data to the client.
4

-- 4 of 26 --

Once the DataDistribution is integrated, it can be used for data synchronization. In the first
scenario a document is created or updated in the client (browser).
The new document is synchronized with the server.
5

-- 5 of 26 --

A document can be also created or updated on the server:
i. it will be synchronized with the client.
Synchronization means that any changes on both sides (client or server) will be synchronized.
6

-- 6 of 26 --

Client
General concepts
The main technologies of the A12 Client are redux, sagas, react and the higher order technologies
A12 Widgets and Form- and Overview Engines. One main concept of the Client is the application
model and using activities to define the state of the application on wich the ui can "react" on.
The Datadispatcher is a central server and client component to handle synchonisation, dispatching
and storing data. By the usage of redux, both concepts work perfectly together on client side. There
are mainly two ways to use the datadistribution client within the A12 Client.
Directly Connected Views
One main feature of A12 datdistribution is displaying "live" data. This was one of its first features,
for example to display different datasets on a dashboard to give the user an overview of business
key figures.
We do not have to use the dataloader or dataprovider concept from A12 Client, but can connect our
data directly to components and engines, wich is the standard redux way. This is because the
dataloaders and dataproviders are not designed to handle real time data that is already in the store.
It is a concept to load data form external sources into activities. An activity is seen as datacontainer
that provides all the data needed in a business use case. If we have multipe views where we need
data of different types to display it in for example a dashboard, this data can automatically be
updated in the background by the datadispatcher.
7

-- 7 of 26 --

DD Connected Dataprovider
Nevertheless the datadispatcher can also be used with A12 Client dataproviders and activities. You
can select data from the in memory db and copy it to the activity data and store it when needed.
This can be done by a dataprovider implementing the A12 standard activity actions.
It is also possible to update activity data if there is new data on the datahub, but this is not the main
idea of activities that are seen as a isolated use case implementation. If you update data in the
background, you have always be aware of the usability. Useres normaly never expect that theri
input is changed in the background without any notification.
8

-- 8 of 26 --

The datadistribution flow in redux
The Datadispatcher (DD) is a central component that hosts and synchronizes data between client
and backend where the backend can have two stages. The client part of the datadispatcher is an in
memory database that is kept in sync with the connected server and stores data locally to provide
offline usage to the application.
The following diagram illustrates the DD-implementation in redux. A main requirement of the DD
is the optional persistence. The DD should be fully working without local persistence, keeping all
information in memory. The optional persistence layer is used for buffering not synced outbound
data and initialization of the datahub to avoid initial synchronisation.
The in memory data is stored as normalized data tables in the redux store, organized by type and id
as suggested in the redux documentation (https://redux.js.org/usage/structuring-reducers/
9

-- 9 of 26 --

normalizing-state-shape#designing-a-normalized-state).
Action Description
Sync Call (simplified)
1 DDSync/EXECUTE_SYNC Executes a new sync cycle if no sync call is
running. Is called by a sheduler or manually.
2 Sync Rest Call The sync Rest-Call is executed against the
current sync partner. Set the sync call state to
RUNNING via DDSync/SET_SYNC_STATE
3 DDHub/RECEIVE_DATA Puts the received data of a successful sync into
the inbound buffer, executes version updates
and deletes on the data hub, stores inbound data
to the indexedDB, updates the sync meta
information like timestamp, seen timestamps,
last sync.
4 DDHub/PROCESS_INBOUND_DATA Triggered by the sync saga to proccess the data
in inbound,. In inbound the payload of
DDEntries contains the transferable and
persistable format which might be converted for
the usage in the ui.
5 Execute DDReducer Execute type specific reducers that write the
data to the data hub and remove it from
inbound. DefaultReducers are available.
10

-- 10 of 26 --

Action Description
6 DDHub/PUT_DATA Type specific sagas might be called to process
inbound data which use PUT_DATA to put the
data into the Hub which is the in memory
represantation of DDEntries that are used by
applications.
7 Use Data in View The Data of the Datadispatcher might be directly
connected in views to implement live updates. It
can of cource also be used in activities and
updated if needed.
8 DDSync/FINISH_SYNC Finish the sync, clear outbound and reset the
call state.
Storing Data
1 DDHub/STORE_DATA Called by applications to store data into the
datadispatcher. The payload of the DDEntries
has to be in the transferable format.
2 DDHub/WRITE_TO_CHANNELS Writes the dirty data to the outbound and
inbound buffer and stored in the indexedDB. We
do optimistic writing because we do not know
when the data is transferred to the backend.
Therefore and because the backend might only
deliver new timestamps and versions we need to
write the data to inbound.If the data is rejected
ore changed by another sync partner the data
might be changed later. Managing concurrent
data is not a direct feature of the dd but there
are several concepts build on top of it.
3 DDHub/PROCESS_INBOUND_DATA For the data that is stored by the STORE_DATA
action PROCESS_INBOUND_DATA is called to
update the data in the data hub in the correct
format.
Initialize User
1 Init User Call the initialization of data in the user
initialization process
2 DDHub/RECEIVE_DATA Call receive data with the persisted inbound
data
3 DDHub/STORE_DATA Call store data with the persisted outbound data
to restore not synchronized data.
Collect Garbage
1 DDGC/EXEC_GARBAGE_COLLECTION Executes a new gc cycle if no gc or sync is
running. Is called by a sheduler or manually.
11

-- 11 of 26 --

Action Description
2 Read data from Hub Iterates over the data in the hub to detect
outdated entries.
3 DDGC/PROCESS_GARBAGE The outdated data is removed from persistence
Integration
The integration and use of the data distribution client can vary greatly depending on the project.
There are various requirements that can be implemented through customization. However, the
basic functionality is quite simple. This documentation describes the necessary steps for
configuration and the use of the client. To integrate the client into your client project you nedd to
call "npm install @com.mgmtp.a12.datadistribution/data-distribution-client"
Minimal Integration
The following steps need to be taken to integrate the data distribution client into the a12 client. This
is an minmalistic integration that makes sence to test if the datadistribution communikation to the
backend is up and running. To implement a client solution based on datadistribution see module
integration.
Configure middleware and datareducers
simple appsetup
Unresolved directive in asciidocs/client/integration.adoc -
include::../../assets/source/client/integration/simple/appsetup.ts[]
This will configure the synchronization of the data type "my-data-type"
Initialize
Optional dispatch DDInitializationActions.initialize if there is anything to configure, for example
the syncInterval (Default is 60000ms) or the backend urls.
Then inititalize the User on Login and deinititalize on logout.
initialization
Unresolved directive in asciidocs/client/integration.adoc -
include::../../assets/source/client/integration/simple/initUser.ts[]
where my-sync-client-id has to replaces by anything to identify the sync_client.
Module Integration
The recommended way to integrate the datadistribution client into the A12 client is to make use of
the A12 client modules.
12

-- 12 of 26 --

First we need a datadistribution specific module
initialization
Unresolved directive in asciidocs/client/integration.adoc -
include::../../assets/source/client/integration/module/ddModule.ts[]
Then we create a module for the integration of the datadisribution client, which has to be loaded on
client bootstrap.
initialization
Unresolved directive in asciidocs/client/integration.adoc -
include::../../assets/source/client/integration/module/init/index.ts[]
This code can be copied and reused, maybe you want to implement another id for client
identification, this is project dependend. The next step is to implement a middleware that handles
the user initialization. Here is an example based on uaa:
initialization
Unresolved directive in asciidocs/client/integration.adoc -
include::../../assets/source/client/integration/module/init/middleware.ts[]
Now we can add business modules to our application that are base on one or more data types
initialization
Unresolved directive in asciidocs/client/integration.adoc -
include::../../assets/source/client/integration/module/person/index.ts[]
This kind of integration is also used in the datadistribution demo app.
Check Integration
To check if your dd integration was successful, you can do the following:
• Check if the redux dd state is created in the store and that there are data tables for each
configured type under dd.hub.data
• Check the following values in dd.sync
◦ initializationState === "FINISHED"
◦ state === "RUNNING"
◦ syncFailed === false
◦ erros === []
• Check if the sync call is answered with HTTP 200
13

-- 13 of 26 --

Usage
The Datahub is the in-memory database of the datadistribution client.
For a trivial integration, only two things are actually necessary: reading DDEntries from the
Datahub using selectors and writing DDEntries using the StoreAction.
Transfer Format
When writing data into the Datahub, it is important to note that the payload of the entries must be
in the transfer format.
In the Concepts chapter, it is described how the datadistribution client writes data into the hub.
Before data is written into the hub, it is always in the inbound state, and before being persisted or
synchronized to the backend, it is in the outbound state. Inbound and outbound data are always in
the transfer format, meaning they are serialized in this format. The data in the Datahub doesn’t
necessarily have to be in the same format. Writing data into the hub is done through a reducer
created using createDDDefaultReducer, but it can also be implemented manually. In the payload of
StoreData, the data needs to be converted from its in-memory format back into the transfer format.
API
Client API
Reading Data
Data can be easily read from the DataHub using various selectors. However, when reading directly
from the DataHub in a view, it should be noted that the data could be altered due to
synchronization.
initialization
Unresolved directive in asciidocs/client/usage.adoc -
include::../../assets/source/client/usage/view.tsx[]
Writing Data
Modify
When modifying data, usually only the payload of an entry is changed, and the record is saved
again in the transfer format.
initialization
Unresolved directive in asciidocs/client/usage.adoc -
include::../../assets/source/client/usage/modify.tsx[]
14

-- 14 of 26 --

Create
There is a helper function available for creating an empty DDEntry. However, some attributes still
need to be set depending on the domain expertise.
initialization
Unresolved directive in asciidocs/client/usage.adoc -
include::../../assets/source/client/usage/create.tsx[]
Backend
Integration
The Data Distribution (DD) service is presently distributed as a Java library (bean archive), which
can be integrated into an existing or dedicated web application that implements the DD interfaces.
Spring Boot or Jakarta EE-capable containers are supported as runtime environments.
Artifacts
Data Distribution Service
Coordinates: com.mgmtp.a12.datadistribution:data-distribution-service
• This must be added as dependency to your application
• Provides a Java API for all DataDistributionServer operations, see DataDistributionServer
• Provides a Java API for DataDistributionClient operations, see DataDistributionClient
The embedding application must implement DD server REST endpoints required by DD clients
(using the Java API).
Data Distribution DTO
Coordinates: com.mgmtp.a12.datadistribution:data-distribution-dto
• Contains DD DTO (data transfer objects) that can be used for data de-/serialization.
• Relevant only to backend applications using the DD JMS API, those applications embedding the
Data Distribution service library can utilize the Java API directly.
Data Distribution Server Example App
Coordinates: com.mgmtp.a12.datadistribution:server-example-app
• Provides a reference implementation of an web application which integrates Data Distribution
service.
• Based on the "Project Template".
15

-- 15 of 26 --

Data Distribution Server
A web application that integrates the Data Distribution service must provide a configuration of the
infrastructure components required by this service. Different configurations are required for
different containers (Spring Boot, Jakarta EE). The following description applies to Spring Boot.
First, it needs to be ensured that DD components provided in the library are detected through
component scanning. Use the @ComponentScan annotation or include the DD base package in the
@DataServicesApplication if your application is based on Data Services. Scheduling and
asynchronous processing must also be enabled as shown in the following example.
SpringApplication example
Unresolved directive in asciidocs/server/integration.adoc -
include::../../assets/source/server/app/com/mgmtp/a12/template/server/ProjectTemplateS
erverApplication.java[lines=10..]
Repository Configuration
The repository configuration activates the JPA repositories contained in the bean archive and
provides settings for the EntityManager, TransactionManager, the DataSource and Liquibase. You can
copy the implementation provided within the example server application. Since DD service
depends on these settings, it is strongly recommended not to change it. The example configuration
sets NoOpEncryptionServiceImpl implementation which disables payload encryption in the database.
If encryption is required in your project, a custom implementation of EncryptionService can be set.
DataDistributionRepositoryConfiguration
Unresolved directive in asciidocs/server/integration.adoc -
include::../../assets/source/server/app/com/mgmtp/a12/template/server/datadistribution
/config/DataDistributionRepositoryConfiguration.java[lines=35..]
Additionally, this class must be plugged into the Spring Boot auto configuration by creating the file
org.springframework.boot.autoconfigure.AutoConfiguration.imports in the META-INF/spring resource
folder. Put the class name of your configuration class as a new line:
com.mgmtp.a12.template.server.datadistribution.config.DataDistributionRepositoryConfiguration
Scheduler
The DD module implements processes, like garbage collection, that need to be started by the
application via a scheduler. Simply copy the
com.mgmtp.a12.datadistribution.scheduler.DataDistributionScheduler class from the sample
application into your application to activate these tasks.
Scheduler configuration
Unresolved directive in asciidocs/server/integration.adoc -
include::../../assets/source/server/app/com/mgmtp/a12/template/server/datadistribution
16

-- 16 of 26 --

/scheduler/DataDistributionScheduler.java[lines=12..]
Data Distribution Endpoints
The application must implement REST endpoints required by the DD client to synchronize with the
server. This approach allows the application to implement various aspects such as authorization,
validation, logging, etc. in accordance with the project requirements. The following example
illustrates a possible implementation of a RestController that uses A12 UAA for user authorization.
The specified context paths also need to be passed to the DD client, either as constants in the client
code or through a service registry if one is used.
Data Distribution REST endpoints
Unresolved directive in asciidocs/server/integration.adoc -
include::../../assets/source/server/app/com/mgmtp/a12/template/server/datadistribution
/syncserver/DataDistributionController.java[lines=15..]
Data Distribution Server Facade
The actual request processing takes place in a server facade. Here, the DD Java API is used, and
additional validations are implemented that can be customized to meet the project requirements.
The facade marks the transactional boundaries of the single DD operations. The
@Transactional("ddTransactionManager") annotation is necessary to start a Java transaction
managed by the "ddTransactionManager" defined in the DataDistributionRepositoryConfiguration
 Currently, only one DD operation must be executed within a database transaction.
If multiple DD operations are to be executed within a client request, these must be
transactionally decoupled.
DD Server Facade Example
Unresolved directive in asciidocs/server/integration.adoc -
include::../../assets/source/server/app/com/mgmtp/a12/template/server/datadistribution
/syncserver/DataDistributionServerFacade.java[lines=33..]
Data Distribution exceptions and client handling
There are a few common exceptions that can occur in Data Distribution, which extend
AbstractDataDistributionCodifiedException. These are:
• ChangeNumberInconsistencyException
• SyncStateInconsistencyException
• FaultException
Each of these exception classes has a specific error code associated to it,
AbstractDataDistributionCodifiedException#errorCode#code. The frontend library offers a
customized error handling for these exceptions, provided the Response returned to them contains
17

-- 17 of 26 --

that errorCode.
To use the frontend library’s exception handling, the Response can use the
DataDistributionCodifiedExceptionClientDetails class, or extend it. The other attributes (timestamp,
description, title) are optional, but used in logs. For SpringBoot, an example of how to create a
ResponseEntity that will trigger the DD frontend error handling is shown in
com.mgmtp.a12.template.server.datadistribution.syncserver.DataDistributionController.
JMS configuration
The integrating application has the option to implement the DD JMS API, enabling other backend
applications to access the DD service. Aligned with the DD Java API, the DD JMS API adheres to the
pub-sub model and includes a DISPATCH_MESSAGE_QUEUE JMS queue for transmitting data to the DD
service, along with a DISPATCH_EVENT_MESSAGE_TOPIC JMS topic that conveys messages related to data
updates.

For implementing the JMS API, you will need a message broker. For simplicity, the
reference implementation uses the embedded message broker integrated into
Spring Boot, which is not suitable for production scenarios. We recommend using
an external message broker such as Apache ActiveMQ, which must be installed
and configured according to the project requirements. In a production setup, the
configuration for both the queue and topic should support durable subscriptions.
This enables listeners to process messages at a later time, even if they are
temporarily unavailable or disconnected from the message broker.
Implementing the JMS API requires additional configuration of the application, which is
exemplified in the following illustration. To begin, we will activate the embedded message broker
and enable the pub-sub messaging domain in the application.properties file. Additionally, we
initialize the DISPATCH_MESSAGE_QUEUE and the DISPATCH_EVENT_MESSAGE_TOPIC.
# Message broker configuration
spring.jms.pub-sub-domain=true
spring.artemis.mode=embedded
spring.artemis.embedded.queues=DISPATCH_MESSAGE_QUEUE
spring.artemis.embedded.topics=DISPATCH_EVENT_MESSAGE_TOPIC
Next, a JMS listener is needed to receive incoming data from the DISPATCH_MESSAGE_QUEUE and
forward the decoded DispatchMessage to the DataDistributionServer for further processing.
DispatchMessageListener
Unresolved directive in asciidocs/server/integration.adoc -
include::../../assets/source/server/app/com/mgmtp/a12/template/server/datadistribution
/syncserver/jms/DispatchMessageListener.java[lines=17..]
Finally, events generated by the DataDistributionServer need to be sent to the
DISPATCH_EVENT_MESSAGE_TOPIC by a JMS publisher. To enable message filtering based on the data
18

-- 18 of 26 --

type on the receiver side, we set the JMS property 'type' as metadata of the message. If routing rules
need to be implemented in the message broker, the JMSType message header should also be set.
DispatchEventMessagePublisher
Unresolved directive in asciidocs/server/integration.adoc -
include::../../assets/source/server/app/com/mgmtp/a12/template/server/datadistribution
/syncserver/jms/DispatchEventMessagePublisher.java[lines=14..]
This basic configuration is adequate to demonstrate the utilization of the DD JMS API in JMS API.
Configuration properties
The Data Distribution service offers configuration options that can be set using the
application.properties of your application in Spring or by system properties in Jakarta EE.
Alternatively a custom com.mgmtp.a12.datadistribution.config.ConfigurationProvider can be
implemented to provide settings required by the module. Most of the Data Distribution
configuration keys start with the prefix mgmtp.a12.dd. See ConfigurationProperties for all available
properties.
Datasource configuration
The Data Distribution service requires a datasource that is configured using the following keys. At
the current time, only Oracle(19c) and PostgreSQL databases are supported.
spring.datasources.datadistribution.liquibase.change-log =
classpath:com/mgmtp/a12/datadistribution/liquibase/db.changelog-datadistribution.xml
Liquibase change log configuration.
DD depends on this property not being changed therefore it is strongly recommended not to
change it.
spring.datasources.datadistribution.jpa.database-platform =
org.hibernate.dialect.PostgreSQLDialect
JPA Dialect
spring.datasources.datadistribution.driver-class-name = org.postgresql.Driver
Driver must be on the classpath.
spring.datasources.datadistribution.jpa.database = POSTGRESQL
The database system in use.
spring.datasources.datadistribution.url = jdbc:postgresql://localhost:8083/datadistribution
Connection string to the database.
spring.datasources.datadistribution.username = postgres
19

-- 19 of 26 --

The username for the connection.
spring.datasources.datadistribution.password=secret
The password for the connection.
spring.jpa.hibernate.ddlAuto = validate
Validates the db schema created by Liquibase against the JPA entity model.
should not be changed
Server configuration
mgmtp.a12.dd.server.appendix.gc_retention_time.days = 30
Specifies the retention period for deleted DD entry appendices before it will be physically removed
by server. Within the retention period deleted appendices are kept in the appendix array with
deleted=true flag.
Archive configuration
The archiving process can be configured with the following properties.
mgmtp.a12.dd.server.archive.mode = ERASE
Specifies the archiving mode [BACKUP, ERASE]. If BACKUP is set, the finalized DD entries, previously
marked as deleted by the GC process, will be moved (archived) into a separate archive schema. If
ERASE is set, the deleted DD entries will be irrevocably erased from the main database schema.
mgmtp.a12.dd.server.archive.schema.name = archive
The name of the archive database schema.
Only required if BACKUP mode is set.
mgmtp.a12.dd.server.archive.retention_time = 90
The number of days that deleted entries remain in the database before they are archived. Values < 1
will be ignored.
mgmtp.a12.dd.server.archive.processing.limit = 10000
The maximum number of entries that can be archived in a process execution.
mgmtp.a12.dd.server.archive.parallel_query = 0
Specifies the maximum number of parallel database queries to be used by the archiving process. If
the value is < 0, no parallel queries are used. If the value is 0, the DB selects the optimum number. If
the value is > 0, the given number of parallel queries are used.
20

-- 20 of 26 --

Usage
This section contains examples that can be realised using the Data Distribution Server Java API. The
source code can be found in the Server Example Application in the Data Distribution Project.
Java API
SYNC, FILL-IN, GC, ARCHIVE
The SYNC, FILL-IN, GC and ARCHIVE are technical processes implemented in DataDistributionServer
and DataDistributionClient. The execution of those operations on the server side has already been
explained in section Data Distribution Server as part of the application setup; no additional
application code is required.
DISPATCH
In contrast to a DD client, which can bidirectionally synchronize all its data with the server via the
SYNC operation, application backends typically do not synchronize with a DD server. Instead a
publisher/subscriber pattern is used. The following example demonstrates, how data entries can be
created and published by the application using the Java API.
Dispatch Example
Unresolved directive in asciidocs/server/usage.adoc -
include::../../assets/source/server/examples/com/mgmtp/a12/template/server/datadistrib
ution/DispatchExampleIT.java[lines=19..]
SUBSCRIBE
An application can subscribe to DispatchEventMessage events to be informed about data updates that
occur as a result of the SYNC or DISPATCH operation on the server. The application can react on
these events and trigger internal processing tasks. The following implementation example shows
how to subscribe and to process DispatchEventMessage in the application code.
Subscribe Example
Unresolved directive in asciidocs/server/usage.adoc -
include::../../assets/source/server/app/com/mgmtp/a12/template/server/datadistribution
/event/DataDistributionEventListener.java[lines=11..]
 The processing of the DispatchEventMessage must be carried out within a separate
transaction to prevent exceptions thrown during processing from rolling back the
underlying DD transaction.
A straightforward way to achieve this is through asynchronous event processing. Since Spring
transactions are thread-bound, the @Async annotation ensures that the listener code is executed
outside of the underlying DD transaction. Asynchronous processing must be enabled with the
@EnableAsync annotation in the application configuration.
21

-- 21 of 26 --

Conditional event listeners can be used for selecting relevant data, filtering messages based on the
DDEntry type.
JMS API
PUBLISH / SUBSCRIBE
To illustrate the usage of the JMS API, let’s define a test class that sends data to the
DISPATCH_MESSAGE_QUEUE and listens for update events from the DISPATCH_EVENT_MESSAGE_TOPIC created
in JMS configuration. This test class will utilize a custom Notification object as the payload, defined
within the test case. Below is an example of how you can create such a test class. The source code
can be found in the Server Example Application in the Data Distribution Project.
Publish-Subscribe Example
Unresolved directive in asciidocs/server/usage.adoc -
include::../../assets/source/server/examples/com/mgmtp/a12/template/server/datadistrib
ution/JmsApiExamplesIT.java[lines=1..186]
In this example, data transfer objects from the dto.messaging.* package, which are part of the data-
distribution-dto library, are employed. This choice is made because the application lacks direct
access to the Java API offered by the data-distribution-service. These DTOs are straightforward
POJOs designed for the purpose of message encoding and decoding. The main entity is the DDEntry
object, which can be instantiated using a builder that validates the entry for completeness and, if
necessary, generates an InvalidEntryException. After the data entry has been created and validated,
it is encapsulated within a DispatchMessage and sent to the DISPATCH_MESSAGE_QUEUE using Spring’s
JmsTemplate.
A DispatchMessage is not limited to a single record; you can add as many DDEntries as needed. These
entries are then processed on the server side within a single DISPATCH operation. To limit the scope
of the operation, a chunking approach with multiple messages can be used, especially when a very
large number of entries needs to be transmitted.
In the second part of this test, a JmsListener subscribes to the update events generated and sent by
the DD server to the DISPATCH_EVENT_MESSAGE_TOPIC. DispatchEventMessage can contain one or more
DDEntry instances of the same data type, which were processed during a DISPATCH or SYNC operation.
The server generates DispatchEventMessage as a TextMessage, and to deserialize it, a tool of choice
such as Jackson can be used.

It is imperative to ensure the graceful handling of unknown attributes during
deserialization using the DTO library. This practice helps maintain the stability of
the application code, even when facing significant server changes, as new
attributes may be introduced to the DispatchEventMessage in subsequent releases.
For a business application, it’s generally impractical, and often a security concern, to receive and
process all server event messages. To filter the data, you can use a JMS selector that allows only
messages containing a specific data type to pass through. In the above example, the JmsListener is
configured to receive and process only messages with the data type 'notification'. Another approach
22

-- 22 of 26 --

can be implemented on the message broker side by setting up an individual topic for each
application and routing only messages containing types relevant to that application to it. To enable
such routing, event messages are assigned a JMSType header, which is set based on the contained
entry data type.
Often, an application must discern between updates initiated by clients, typically stemming from
user interactions, and those originating from the backend system itself. This distinction can be
established using the changeUser attribute, which the application should manually configure for
backend updates and which will be automatically populated by clients with the userId of the
logged-in user.
SCRIPT UPDATE
In the previous section, we demonstrated how a DDEntry can be created or updated using the JMS
API. In this example, the complete entry is transferred from an application’s backend to the DD
Server. While this method is suitable when the application intends to completely replace the entry’s
content, it may not always be the most practical choice for two primary reasons. Firstly, the
application’s backend frequently lacks real-time knowledge of the entry’s current state since clients
can also perform updates, and the backend often does not maintain a log of these modifications.
This can lead to discrepancies in data accuracy. Secondly, when dealing with large data payloads,
transmitting the entire content incurs additional overhead, even if only a small portion of the
payload has undergone changes. This inefficiency can impact performance and resource
utilization.
The DD JMS API offers an alternative approach to update an entry without requiring prior
knowledge of its current content, known as SCRIPT UPDATE. In this method, the application’s backend
transmits a JavaScript update function to the DD Server, along with entry identifiers. This function
is subsequently executed on the found entries, enabling the selective modification of specific
payload content.
Let’s build upon the previous pub-sub test case, where we created and sent a notification. In this
upgraded version, we will introduce a script update message that leverages JavaScript to modify
the text of the notification after its initial creation.
Script Update Example
Unresolved directive in asciidocs/server/usage.adoc -
include::../../assets/source/server/examples/com/mgmtp/a12/template/server/datadistrib
ution/JmsApiExamplesIT.java[lines=187..238]
The DispatchMessage.UpdateSpecification is initialized with three parameters: a correlationId
(which may be null), a list of entry ids and the JS update function. The ids serve as identifiers for
the entries targeted for update by the script. In situations where the backend lacks information
about the IDs of all existing entries, a correlationId can be employed in place of individual IDs.
Similar to the id, the correlationId is an attribute of the DDEntry that can be employed to associate
(correlate) multiple related entries with a single key, such as a well-known business case identifier.
The IDs and the correlationId are applied additively using the OR operator.
The update script is a JavaScript function that is invoked with a found DDEntry object and can make
23

-- 23 of 26 --

modifications to it. In the given example, the payload of the entry is parsed as JSON object, and the
'text' attribute is set to the value 'Greetings!'. Naturally, the logic can be more intricate, including the
use of conditional statements to assess the state of the existing entry and execute calculations on
the attributes of the DDEntry and its payload. The function may return a boolean value indicating
whether the script made modifications to the entry or not. This allows the DD server to receive
advice on whether to update the entry in its persistence. If no boolean value is returned, the server
will automatically update the entry following the successful execution of the update function.

While the script has the capability to access all DDEntry header attributes and
make modifications, it’s important to note that the server may reject the resulting
entry from being updated if update rules are enforced on the server side. It’s
advisable to monitor the server log for any potential error messages during the
testing of your function.
REDELIVERY
The REDELIVERY mechanism provides a way to request the re-delivery of previously published
data from the DD Server. The DispatchMessage includes a QuerySpecification, which, similar to the
UpdateSpecification, includes a correlationId and a list of entry id as parameters. The difference
lies in the fact that no data update occurs on the server. Instead, the server is called upon to
generate events for the requested data, thereby providing an application with the opportunity to
determine the current state of the entries. We are extending our test to demonstrate this
functionality.
Redelivery Example
Unresolved directive in asciidocs/server/usage.adoc -
include::../../assets/source/server/examples/com/mgmtp/a12/template/server/datadistrib
ution/JmsApiExamplesIT.java[lines=239..]
Please note that the entry was not updated during the redelivery operation, as confirmed in our
test.

As A12 Data Distribution is not a database in the traditional sense but rather a data
transport mechanism, it does not provide a Query API for data retrieval. The
REDELIVERY mechanism should not be understood or massively used as a Query
API, especially because it does not meet the corresponding performance
requirements.
API documentation
The full java documentation can be found here.
Migration Instructions
The following documentation contains all migration instructions and hints needed to update Data
24

-- 24 of 26 --

Distribution versions.
Migration to version 2.0.0
Update of com.faster.xml library version
Data Distribution now requires version 2.16.2 or above.
Replacing java.util.Date by java.time.Instant
Existing attributes of type java.util.Date in the DTO objects and services, have been converted into
java.time.Instant. No DB migration is needed as for the moment, since the internal attributes are
still using the java.util.Date type. This might change in a future release.
Migration steps
Marshalling/Unmarshalling DDEntries
Our recommended way to transfer DD objects is to use Jackson ObjectMapper. In order to work
with the Instant type correctly, you will need to enable the module 'jackson-datatype-jsr310` with
ObjectMapper configuraton
objectMapper.registerModule(new JavaTimeModule());
objectMapper.setTimeZone(TimeZone.getDefault()); // adjust accordingly to your needs
DDEntry.builder
The main entrypoint for creating DDEntries,
com.mgmtp.a12.datadistribution.dto.messaging.DDEntry.builder, has been updated. The methods
withChangeDateAtClient, withChangeDateAtServer, withFinishedDate and withExpirationDate,
now take an Instant as parameter, instead of Date. === Migration to version 2.1.0
DDEntry appendix array cleanup
Appendix objects flagged as deleted by the client will be automatically removed from the appendix
array by the server after a configurable retention period. During this retention period, deleted
appendices remain in the appendix array marked with the deleted=true flag.
Configuration changes
A new application property specifies the retention period (in days) for deleted DD entry appendices
before it will be physically removed by server. Default = 30 days.
mgmtp.a12.dd.server.appendix.gc_retention_time.days = 30
25

-- 25 of 26 --

Migration to version 4.0.0
SyncScope ORGA_UNIT has been removed
The syncScope property has been removed from the SYNC request. By default, the regular sync scope
now includes all DD-Entries addressed to the user’s organizational unit, but only those specifically
assigned to the user via userId or user roles. Previously, it was possible to bypass user identity
verification by setting syncScope=ORGA_UNIT within the SYNC request, but this is no longer supported.
Configuration changes
Following application properties are no longer supported:
mgmtp.a12.dd.server.sync.scope.orgaunit.enabled
26

-- 26 of 26 --

