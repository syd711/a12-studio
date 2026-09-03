# notification_center notificationcenter documentation

Notification Center
NOTE
This documentation belongs to an A12 Enterprise Component which is not part of
the Open Source offering (A12 Community Edition). Please feel free to browse the
documentation and learn more about how you can use this A12 component in your
project. Learn more about the benefits from an A12 Enterprise Subscription on the
Editions & Licensing page.
1. Introduction
The Notification Center is an A12 component which can be integrated by various projects in order
to support the user performing his workflow tasks within A12.
The purpose of the Notification Center is to manage A12 document-related tasks and events
1

-- 1 of 107 --

resulting in different kinds of notifications presented to the user.
The Notification Center supports 2 major types: Web Notifications and Push Notifications.
Web Notifications can be shown to the users as toast messages or browser notifications. They are
collected and managed in the Notification Center which is accessible via the header bar of the A12
application.
Figure 1. Web Notifications in the Notification Center from a birds eye view
Beside Web Notifications, users can also receive task updates via mobile Push Notifications.
Leveraging the native notification system ensures notifications reach users, even when they are not
actively using the app.
2

-- 2 of 107 --

Figure 2. Push Notifications in the Notification Center from a birds eye view
1.1. Technologies
The Notification Center is based solely on A12 technologies. It consists of a client based on the A12
Client library and React, written in TypeScript.
The communication layer uses A12 Data Distribution.
The Notification Center Services is build upon A12 Data Services.
The Push Notification Services is build upon Capacitor Push Notification Plugin, pushy and
Firebase Cloud Messaging.
1.2. Breaking Change Management
1.2.1. Definition Of Version
The Notification Center defines a single version. All artifacts (Java and Javascript) that belong to the
Notification Center share the same version.
We are using semantic versioning for our product releases. All public classes and their public
members, that are not within internal packages, will be considered public APIs. These public APIs
will remain unchanged within a major version release of the product.
3

-- 3 of 107 --

1.2.2. What Is Breaking Change?
A change is deemed breaking if:
• Project compilation encounters failure.
• There is a change in application behavior.
• Code adaptation becomes necessary.
• Manual model adaptation is required.
Only happen in Major releases. Migration instructions will always be provided to facilitate easier
integration.
1.2.3. What Is Non-Breaking Change?
A change is deemed non-breaking if:
• Migration is provided without necessitating any manual steps from customer projects.
• Modifications occur in the internal API. Please note that migration steps are public, they are
considered valid only for the corresponding release line. Therefore, any changes in the next
release line are not considered breaking.
• Deprecation events take place.
These occurrences are confined to minor and patch releases.
1.2.4. Breaking and Non-Breaking Interpretation
Breaking Non-breaking
Public API Client libraries
Incompatible change of API
signatures that cause compile
errors:
• Adding required properties
to NC components
• Removal/Rename of NC
components properties
• Removing exported
definitions
Java libraries
• New config properties with
a default value that changes
behavior
Client libraries
• Adding new exported
definitions
• Changing exported
definitions compatibly:
◦ Adding new optional
properties to
components
4

-- 4 of 107 --

Internal • Changes of application
behaviour which may affect
client projects
• Everything apart from
Public API points is
considered internal
• All Internal APIs are
considered non-breaking
• Any changes in this section
are always considered non-
breaking
• All changes preserving the
behaviour are considered
non-breaking
Configuration • Add mandatory
configuration
• Rename configuration
• Remove configuration
• Change the default value
• Change meaning or
behavior of configuration
• Add optional configurations
keys or values
• Add new value to existing
configuration
Dependencies Client libraries
• Change of TypeScript
version.
• Change of React version.
• Change of any peer
dependency (range).
Java libraries
• Change of Spring major
releases (include all Spring-
related libraries)
Client libraries
• Update of 3rd
devDependencies
Java libraries
• Update of internal
dependencies with minor,
patch versions
1.2.5. How We Mark Deprecation?
A deprecated API will be:
• Removed in the major releases.
• Marked with @Deprecated (java) @deprecated (typescript) annotations.
• Commented properly with new version usage properly described.
• A documentation of Deprecated APIs will be delivered within the release notes.
5

-- 5 of 107 --

2. Glossary
No Term Definition
1 Appointment
(Notification Type)
Appointment is a notification created when the Appointment Time of
the Reminder document is reached
2 Appointment Time A required field inside the base Reminder Document Model. If this
time has expired, an appointment notification will be sent to the user
3 Artifact An artifact is any product, byproduct, or deliverable part of the
software. For example a docker image, a npm, or a jar package/library
4 Autoconfiguration Spring Boot autoconfiguration attempts to automatically configure
your Spring application based on the jar dependencies that you have
added
5 Browser Notification Browser Notification is the pop-up displayed on the system level when
the webpage is in the background
6 Chunk Chunk in the Notification Center is the number to configure the
number of notifications sent in one request to prevent the server
error
8 ComponentMap Component Map (or NotificationCenter Component Map ) is an
interface provided by the Notification Center for UI customizations
9 Custom Notification
Type
Besides default notification types provided by the Notification Center,
you can create your own Notification Type to fit your requirements
10 Data Distribution A12 DataDistribution (A12 DD) is an A12 component that enables data
synchronization between different nodes and clients. In the
Notification Center, Data Distribution is used to create, store, and
synchronize notifications between clients.
11 Data Distribution
Entry
Data Distribution Entry is the main entity in the Data Distribution.
Notifications are created as a Data Distribution Entries
12 EncryptionService EncryptionService is an interface provided by Data Distribution that
provides encryption and decryption methods to secure sensitive data
13 Entry (Notification
Type)
Entry is one of the default types in the Notification Center
14 Garbage Collection In the Notification Center, Garbage Collection takes care of closing
finished Data Distribution entries based on their retention time
15 Helm Helm is a package manager that simplifies the process of packaging,
deploying, and managing applications on Kubernetes clusters
16 JobDataMap JobDataMap is a part of Quartz, and can be used to hold state
information for Job instances
17 Kubernetes Kubernetes, also known as K8s, is an open-source system for
automating the deployment, scaling, and management of
containerized applications
6

-- 6 of 107 --

No Term Definition
18 Liquibase Liquibase is an open-source database-independent library for
tracking, managing, and applying database schema changes
19 Message (Notification
Type)
Message notification can be used to inform the user about system
events or messages
20 Notification Center
Client
The client library which supports integrating projects to simplify the
setup of the A12 Data Distribution and A12 Reminder module in the
A12 Client application
21 Notification Center
Core
The UI components used by the Notification Center which also be
used separately as a UI library
22 Notification Center
Portal (Compact View)
Compact View is the place where the user can quickly access their
latest notifications. The Compact View only shows a limited number
of notifications (10 by default)
23 Notification Center
Portal (Detail View)
Detail View is the place where the user can view all notifications with
additional filter options, and perform actions on these notifications
24 Notification Center
Reminder Extension
The (java) library which is installed inside A12 Data Services to
handle the logic of the reminder document
25 Notification Center
Reminder Job
The (java) library used by the Notification Center Service to handle
the reminder scheduling logic on the server side
26 Notification Center
REST Client
The (java) client library which provides the API to interact with the
Notification Center Services from other services
27 Notification Center
Services
The artifact that provides the backend services for the Notification
Center
29 Notification Status
"Read"
Read is the status of the notification when the user reads the
notification and clicks on the Mark as Read button to change the status
30 Notification Status
"Unread"
Unread is the initial status of a new notification
31 Notification Status
"Deleted"
Deleted is the status of a notification when the user clicks on the
Delete button. The deleted notification is not removed
instantaneously from the database but instead, a finished flag is set
on it (soft-delete)
32 Notification Notification is a piece of information that can be sent to the user
33 NotificationHubModul
e
The Notification Module is the module provided by the Notification
Center to configure the sync notification action
34 Oracle Oracle Database (commonly referred to as Oracle DBMS, Oracle
Autonomous Database, or simply as Oracle) is a proprietary multi-
model database management system produced and marketed by
Oracle Corporation.
35 Postgres PostgreSQL, also known as Postgres, is an open-source object-
relational database management system
7

-- 7 of 107 --

No Term Definition
36 Push Notification Push Notifications are the messages that pop up on a mobile device,
sent by the backend module to the target users. These notifications
are displayed similarly to browser notifications but can appear even
when the application is not actively in use.
37 Quartz Job Quartz is an open-source job scheduling library. It’s used to schedule
and execute jobs e.g. creating time-based notifications in the
Notification Center
38 Reminder DONE
(status)
DONE is the status when the user marks the reminder as done
39 Reminder DUE (status) DUE is the status when all notifications derived from this reminder
have been created and sent to the user
40 Reminder Migration Reminder migration is the process of migrating reminder data from
the older to the newer version based on the Reminder document
model
41 Reminder Time A required field inside the base Reminder Document Model. When the
time has expired, a Reminder notification will be sent to the user
42 Reminder
TO_BE_DONE (status)
TO_BE_DONE is the initial status of the newly created Reminder
43 Reminder
(Notification Type)
A notification is created when the Reminder Time of the Reminder
document is reached
44 Simple Model Editor
(SME)
Simple Model Editor (SME) is the application used to prepare all the
related models (Application, Form, Overview Models, and screens)
45 Spring Spring Framework (Spring) is an open-source software development
framework that provides infrastructure support for building Java-
based applications on any deployment platform
46 Stacked Notification
View
Incoming notifications will be displayed on top of the user’s current
application context. If there is more than one notification, the popup
will automatically turn into the stacked notification view
47 Theme A website theme, often referred to as a website template, is a ready-
made design template for websites that defines the look, layout, and
often also certain functions of a website
48 UAA User Authentication Authorization (UAA) is a library for handling
security aspects of your application. It can be used as a standalone
library or inside the Spring Boot application
3. Basic Ideas
3.1. Web Notifications
8

-- 8 of 107 --

3.1.1. General Description
Web Notifications are pieces of information that can be sent to the user. Web Notifications are
displayed on the web page. They contain text, icons and can perform contextual actions like e.g.
linking to a document, updating the status of the notification or deleting themselves.
Web Notifications are presented to the user of the application in the form of:
• Toast messages in the application: The toasts displayed on the web page when it is in
foreground.
• Browser notifications: The pop-up displayed on the system level when the webpage is in
background.
• Entries in the Notification Center: The notifications displayed when opening the notification
bell.
depending on the settings of the user and the Web Notification type.
3.1.2. Web Notification Types
Default Types
By default, the Notification Center comes with the following Web Notification types:
Web Notification Type Usage
Reminder Based on a Reminder document, a notification of
type Reminder is shown to the recipients, when
the reminderTime is reached.
Appointment Based on a Reminder document, a notification of
type Appointment is shown to the recipients,
when the appointmentTime is reached.
Entry Can be used by workflows to notify the user
about an incoming workflow / application.
Message Can be used to inform the user about a system
event or message.
3.1.3. Web Notification Statuses
The possible Web Notification statuses:
• Read/Unread: Initially, the notification is in Unread status. In the Notification Center, users can
change this status by clicking on the Mark as Read/Unread button (Eye icon).
• Deleted: In the Notification Center, users can delete a notification by clicking on the Delete
button. The deleted notification is not removed instantaneously from database but instead a
finished flag is set on it (soft-delete). It will only be completely deleted from database by a
scheduled clean-up job.
9

-- 9 of 107 --

3.2. Reminders
The Notification Center provides the general user interface for creating, reading, updating and
deleting reminders.
It comes with a base/general Reminder Document Model. It contains the two DateTime fields
reminderTime and appointmentTime.
When a user creates a new Reminder document and the time that is set in one of these fields is
reached, a notification is sent to the recipients set in the Reminder document. If appointmentTime is
set, the notification is rendered as an Appointment, otherwise, it is rendered as a Reminder.
Depending on the project’s use cases, the modelers can create special Document and Form Models,
that are based on the base/general Reminder Document Model but have additional fields. (See Prepare
Reminder Document Model).
Figure 3. Reminder workflow
10

-- 10 of 107 --

3.3. Push Notifications
3.3.1. General Description
Push Notifications are the messages that pop up on a mobile device, sent by the backend module to
the target users. These notifications are displayed similarly to browser notifications but can appear
even when the application is not actively in use.
4. Features
4.1. Web Notifications
4.1.1. Notification Portal Compact View
The user can quickly access their latest notifications. The compact view only shows a limited
number of notifications (by default at 10). By clicking on the Show All button at the footer, users can
open the detail view.
11

-- 11 of 107 --

4.1.2. Notification Portal Detail View
In this view, users can:
• Filter notifications by status and type.
• Perform the action (toggle read/unread, delete) on selected notifications with the Bulk Operation
feature.
• Expand the list of notifications by clicking on the Show more button at the bottom. Initially, there
are only 10 notifications in the table.
12

-- 12 of 107 --

4.1.3. Popup Notifications
Incoming notifications will be displayed on top of the user’s current application context. If there is
more than one notification, the popup will automatically turn into the stacked notification view
(screenshot).
13

-- 13 of 107 --

4.1.4. Browser Notification
The user can be notified about new notifications even when the web application is not actively in
use. For example, the user opens other browser tabs or applications.
4.1.5. Notification Center User Preferences
This feature allows users to configure Notification Center features, including:
• Toggle on/off the browser notification feature.
• Allow/Disallow the popup notification for specific notification types.
14

-- 14 of 107 --

4.1.6. Reminder
Users can use this feature to remind themselves or a group of users to work on specific tasks.
15

-- 15 of 107 --

4.2. Push Notifications
Notifications are displayed on the users mobile device even if the related app is currently not open
or - depending on the users settings on his mobile device - are also shown if the device is locked
optionally combined with audio/vibrating signal. If the user taps on the notification, he is directly
routed to the corresponding application.
16

-- 16 of 107 --

5. How It Works
5.1. Web Notifications
Figure 4. Web Notifications Architectural Overview
5.1.1. Main Components
• Notification Center Services (notificationcenter-service-distribution): The artifact which
provides the backend services for the Notification Center.
• Notification Center REST client (notificationcenter-rest-client): The (java) client library
which provides the APIs for interacting with Notification Center Services from other backend
services.
• Notification Center Client (notificationcenter-client): The client library which support
17

-- 17 of 107 --

integrator to simplify the setup of A12 Data Distribution and A12 Reminder module in A12
Client application.
• Notification Center Reminder Extension (notificationcenter-reminder-extension): The
(java) library which is installed inside A12 Data Services to handle Reminder Document logics.
• Notification Center Reminder Job (notificationcenter-reminder-job): The (java) library
which is installed inside Notification Center Services to handle Reminder Job logics.
• Notification Center Core (notificationcenter-core): The UI components which are used by the
Notification Center and can also be used separately like a UI library.
NOTE The artifact URLs for the above components are described in the Artifacts section.
Besides that, the Notification Center uses the following other components/technologies:
• A12 Data Distribution: We use this component internally to store notifications, user settings
and handle data synchronization between client and server side.
• Data storage: The Notification Center stores data in two places:
◦ A12 Reminder documents will be stored in the A12 Data Services.
◦ The notifications and other data will be stored in the Notification Center database.
• Quartz Jobs: The open source job scheduling library that the Notification Center uses to
schedule jobs to generate the notifications. It also schedules jobs to execute the background
tasks of the A12 Data Distribution.
5.1.2. User Flow
Send Notification From Other Backend Services
As an example, we want to send a Web Notification from A12 Data Services (other backend
service) to a user in web client (frontend) side. The flow is:
Backend
• Call NotificationPublisher.publish method from the Notification Center REST client package
with the corresponding notification data.
• Notification Center Services reads the notification data from the request and stores the data
into database using A12 Data Distribution APIs.
Frontend
• The A12 Data Distribution Client retrieves the latest data from the backend and stores them
into the Redux store.
• The Notification Center Core reflects the changes in Redux store to the UI.
Interact With Notifications From Web Client
User are be able to toggle read/unread and delete their notifications.
18

-- 18 of 107 --

Frontend
• User interacts with notifications via Notification Center Core UI components.
• The accordingly notifications in the A12 Data Distribution Redux store will be changed and
synced automatically to the backend by using the sync request.
Backend
• Notification Center Services reads the sync request’s payload and stores changes into the
database.
Schedule a Reminder
Frontend
• User fills the reminder form (A12 Form Engine) and clicks submit button.
• Notification Center Client sends CRUD requests to A12 Data Services.
Backend
• In A12 Data Services, Notification Center Reminder Extension reads the request data from
frontend side and send necessary information to Notification Center Services to schedule
Reminder jobs.
• In Notification Center Services, Notification Center Reminder Job reads the data from
Notification Center Reminder Extension and schedule Reminder jobs to send notifications at
the designated reminder time/appointment time specified in the form.
• A12 Data Services stores A12 Reminder document into its database.
5.2. Push Notifications
19

-- 19 of 107 --

Figure 5. Push Notification Architectural Overview
5.2.1. Main Components
• Push Notification Service (notificationcenter-push-notification-service): The artifact which
provides the backend services for the Push Notification Service.
• Push Notification Rest Client (notificationcenter-push-notification-rest-client): The (java)
library that provides the APIs to interact with the Push Notification Service from the other
backend services.
NOTE The artifact URLs for the above components are described in the Artifacts section.
Besides that, the Push Notification uses the following other components/technologies:
• OpenAPI specification: Programming language-agnostic interface description for Push
Notification Service HTTP APIs, which is provided at nc-push-notification-api.json in
notificationcenter-push-notification-api-specification.
• Cloud push providers (Firebase Cloud Messaging / Apple Push Notification service): The
cloud provider for sending Push Notifications to target mobile platforms.
• Capacitor Push Notification Plugin (optional): The plugin for subscribe mobile Push
Notifications.
20

-- 20 of 107 --

5.2.2. User Flows
Register/Deregister for the Push Notifications
Frontend
• The mobile application prompts the user whether they want to receive Push Notifications or
not.
• If the user accepts the permission prompt of the OS, the application sends a request to the Push
Notification Service with the accountId, deviceId and the current platform of the device.
Backend
• The Push Notification Service receives the data from the mobile application and processes the
request to either save or remove data from the database.
Send Push Notifications From A12 Data Services
• After configuring the UAA Rest Client, the integrator can send a Push Notification to a specific
user by calling the notificationApiConnector.createPushNotification method from the Push
Notification Rest Client package.
• Based on the information saved in the database and the targeted platform, the Push
Notification Service connects to the cloud push providers to send notifications to the target
devices.
Send Push Notifications From a Non-A12 Backend Services
• For non-A12 backend services, the integrator can use the UAA Certificate Authentication for
authentication and authorization.
• After configuring the UAA Certificate Authentication in the project, the integrator can send Push
Notifications by using methods from Push Notification Rest Client as described above.
6. Getting Started
6.1. Artifacts
All backend artifacts are Spring Boot applications. As such, they are configurable via standard
Spring Boot means. As we package application.properties files with default settings into our
artifacts, it is mandatory to use a superseding property source if you wish to overwrite these
settings. Check the provided link above for an ordered precedence list.
6.1.1. Backend
21

-- 21 of 107 --

Artifact Artifact ID Description
notificationcenter-service-
distribution
com.mgmtp.a12.notificationcent
er:notificationcenter-service-
distribution
The artifact which provides the
backend services for the
Notification Center Services.
notificationcenter-rest-client com.mgmtp.a12.notificationcent
er:notificationcenter-rest-
client
The (java) client library which
provides the APIs for
interacting with Notification
Center Services from other
backend services.
notificationcenter-reminder-
extension
com.mgmtp.a12.notificationcent
er:notificationcenter-
reminder-extension
The (java) library which is
installed inside A12 Data
Services to handle Reminder
Document logics.
notificationcenter-reminder-
job
com.mgmtp.a12.notificationcent
er:notificationcenter-
reminder-job
The (java) library which is
installed inside Notification
Center Services to handle
Reminder Job logics.
notificationcenter-push-
notification-rest-client
com.mgmtp.a12.notificationcent
er:notificationcenter-push-
notification-rest-client
The (java) library that provides
the APIs to interact with Push
Notification Service from the
other backend services.
notificationcenter-push-
notification-rest-client-spring-
boot-autoconfigure
com.mgmtp.a12.notificationcent
er:notificationcenter-push-
notification-rest-client-
spring-boot-autoconfigure
The (java) library that provides
the APIs to interact with Push
Notification Service from the
other backend services which
use Spring Boot.
notificationcenter-push-
notification-api-specification
com.mgmtp.a12.notificationcent
er:notificationcenter-push-
notification-api-specification
The openapi-spec for the API of
the Push Notification Service.
notificationcenter-push-
notification-service
com.mgmtp.a12.notificationcent
er:notificationcenter-push-
notification-service
The artifact which provides the
backend services for the Push
Notification Service.
6.1.2. Frontend
Artifact Artifact ID Description
notificationcenter-core @com.mgmtp.a12.notificationcen
ter/notificationcenter-core
The UI components which are
used by the Notification Center
and can also be used separately
like a UI library.
notificationcenter-client @com.mgmtp.a12.notificationcen
ter/notificationcenter-client
The client library which can
integrated to your A12 Client
application as a module.
22

-- 22 of 107 --

Artifact Artifact ID Description
notificationcenter-bap-
example
@com.mgmtp.a12.notificationcen
ter/notificationcenter-bap-
example (only in Source Code
Repository, not published)
The example of integrating
Notification Center into A12
web application.
notificationcenter-mobile-
app-example
com.mgmtp.a12.notificationcent
er.mobile.app (only in Source
Code Repository, not published)
The example of integrating
Notification Center into mobile
application.
6.2. Web Notifications
This guide shows how to integrate the core functionalities of Notification Center Web Notifications
into an A12 based project.
For advance configurations, please go to the Notification Center Services section.
NOTE
• On the frontend, the Notification Center is especially designed to run as a
module inside the A12 Client application. It is not designed to run in any other
web / single page application by itself.
• On the backend, the Notification Center Services run in parallel on your
existing application.
6.2.1. Backend
A12 Data Services
Reminder Document Model and its related logics (HTTP API endpoints, events, validators, …) needs to
be available inside A12 Data Services.
Prepare Reminder Document Model
We provide a base Reminder Document Model with required fields under A12Reminder_DM.json.
This model can be used directly in your application as provided. If you need to add more fields to
the reminder document model to meet your specific requirements, you can follow these steps:
• Create your own Reminder Document Model by using the Simple Model Editor (SME).
• Add A12Reminder_DM.json as an include and name it a12Reminder.
IMPORTANT
• The A12Reminder_DM.json include must be called a12Reminder.
• We recommend to not directly modify the base Document Model, as
doing so could potentially break the reminder feature and require more
effort to migrate to new base Document Models of future major releases
of the Notification Center.
• Create a second root group and add the needed fields to it.
23

-- 23 of 107 --

As an example, in our showcase application, we create a ShowCaseReminder-document model with
2 root groups a12Reminder and extraProps.
Load the Reminder Document Model Into A12 Data Services
After creating your own A12 Reminder, you need to load it into A12 Data Services.
Configure Notification Center Reminder Extension Package in A12 Data Services
• Install our Notification Center Reminder Extension package:
implementation "com.mgmtp.a12.notificationcenter:notificationcenter-reminder-
extension:<VERSION>"
• Import NotificationCenterReminderExtensionConfiguration to your DataServiceApplication:
@Import({NotificationCenterReminderExtensionConfiguration.class})
@DataServicesApplication(scanBasePackages =
{DataServicesApplication.DATASERVICES_BASE_PACKAGE, "com.mgmtp.a12.*"})
public class DataServiceApplication {
public static void main(String[] args) {
SpringApplication.run(DataServiceApplication.class, args);
}
}
• Configure application properties as follows:
mgmtp.a12.notificationcenter.reminder.model-name=<YOUR_CUSTOM_A12_REMINDER_NAME>
• Configure UAA:
Our REST endpoints in Notification Center Reminder Extension are secured using UAA, so you
have to assign the appropriate access rights for user roles in your application. Please refer to the
Notification Center Reminder Extension Authorization section for more details.
Configure Notification Center REST Client
If your backend service, such as A12 Data Services, needs to send notifications to users, you can
utilize the Notification Center REST Client package. This package allows a service to
programmatically create and send notifications by calling Notification Center’s REST APIs from the
backend code.
• Install our rest-client package into your service:
implementation "com.mgmtp.a12.notificationcenter:notificationcenter-rest-
client:<VERSION>"
24

-- 24 of 107 --

• Import configurations NotificationCenterClientConfiguration as follows:
@Import({NotificationCenterClientConfiguration.class})
@DataServicesApplication(scanBasePackages =
{DataServicesApplication.DATASERVICES_BASE_PACKAGE, "com.mgmtp.a12.*"})
public class DataServiceApplication {
public static void main(String[] args) {
SpringApplication.run(DataServiceApplication.class, args);
}
}
• Configure UAA Rest Client.
• Configure the Notification Center Services URL:
mgmtp.a12.notificationcenter.client.service-url=http://localhost:8080
Notification Center Services
Configure Notification Center Services
• Create a Spring boot application for Notification Center Services.
• Install the distribution package:
implementation "com.mgmtp.a12.notificationcenter:notificationcenter-service-
distribution:<VERSION>"
implementation "org.postgresql:postgresql:42.5.6"
• Add the annotation @EnableNotificationServer into the main application:
@SpringBootApplication
@EnableNotificationServer
public class NotificationCenterServiceApplication {
public static void main(String[] args) {
SpringApplication.run(NotificationcenterApplication.class, args);
}
}
• Configure application properties as follows:
# Notification center datasource config
spring.datasources.notificationcenter.url=<JDBC_URL>
spring.datasources.notificationcenter.username=<USERNAME>
spring.datasources.notificationcenter.password=<USERNAME>
spring.datasources.notificationcenter.driver-class-name=org.postgresql.Driver
spring.datasources.notificationcenter.jpa.database-
25

-- 25 of 107 --

platform=org.hibernate.dialect.PostgreSQLDialect
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcj
obstore.PostgreSQLDelegate
NOTE The above database configurations are for Postgres database. Please change the
driver and dependency to match your Database engine.
• Configure UAA:
Our REST endpoints in Notification Center Services are secured using UAA, so you have to assign
the appropriate access rights for user roles in your application. Please refer to the Notification
Center Services Security section for more details.
Customize A12 Data Distribution’s Database Configuration
If you would like to separate the configuration properties for A12 Data Distribution, please follow
these steps:
• Override beans with your own properties' prefix (See DDRepositoryConfiguration for the A12
Data Distribution repository beans). Below is an example overriding
"spring.datasources.datadistribution" for ddDatasourceProperties bean.
@ConfigurationProperties("spring.datasources.datadistribution")
@Bean
public DataSourceProperties ddDatasourceProperties() {
return new DataSourceProperties();
}
• Declare the properties at the property file
spring.datasources.datadistribution.url=<JDBC_URL>
spring.datasources.datadistribution.username=<USERNAME>
spring.datasources.datadistribution.password=<PASSWORD>
spring.datasources.datadistribution.liquibase.database-change-log-lock-
table=NC_DATABASECHANGELOGLOCK
spring.datasources.datadistribution.liquibase.database-change-log-
table=NC_DATABASECHANGELOG
WARNING
The configured changelog is notificationcenter_model.xml, which includes A12
Data Distribution's changelog and Reminder Quartz Job's changelog.
To update A12 Data Distribution's database changelog:
• Override the ddLiquibaseProperties.
• Configure the liquibase changelog as follows:
26

-- 26 of 107 --

spring.datasources.notificationcenter.liquibase.change-
log=classpath:database/nc_quartz_model.xml
spring.datasources.datadistribution.liquibase.change-
log=classpath:database/nc_datadistribution_model.xml
Configure Notification Center Reminder Job Package in Notification Center Services
• Install our Notification Center Reminder Job package:
implementation "com.mgmtp.a12.notificationcenter:notificationcenter-reminder-
job:<VERSION>"
• Import NotificationCenterReminderJobConfiguration to your
NotificationCenterServiceApplication:
@SpringBootApplication
@EnableNotificationServer
@Import({NotificationCenterReminderJobConfiguration.class})
public class NotificationCenterServiceApplication {
public static void main(String[] args) {
SpringApplication.run(NotificationcenterApplication.class, args);
}
}
• Configure application properties as follows:
mgmtp.a12.notificationcenter.dataservices.base-url=<YOUR_DATA_SERVICES_URL>
mgmtp.a12.notificationcenter.reminder.model-name=<YOUR_CUSTOM_A12_REMINDER_NAME>
• Configure UAA:
Our REST endpoints in Notification Center Reminder Job are secured using UAA, so you have to
assign the appropriate access rights for user roles in your application. Please refer to the
Notification Center Reminder Job Authorization section for more details.
6.2.2. Frontend
Dependencies
Add the below dependencies into your package.json in the A12 Client application:
{
"@com.mgmtp.a12.notificationcenter/notificationcenter-client": "<VERSION>",
"@com.mgmtp.a12.notificationcenter/notificationcenter-core": "<VERSION>",
"@com.mgmtp.a12.datadistribution/data-distribution-client": "^4.0.0"
27

-- 27 of 107 --

}
App Setup
Register the custom sagas and reducers into your appsetup:
{
...NotificationCenterSagas
}
{
NotificationCenterReducers
}
Or using withNotificationCenter in A12 composable application setup:
import { withNotificationCenter } from
"@com.mgmtp.a12.notificationcenter/notificationcenter-core";
const {store, initialActions, Component} = createA12ApplicationSetup(
combineFeatures(
...
withNotificationCenter,
...
)(initialConfig)
Styled Component Configurations
• Add the additional theme configurations:
const newTheme = {
...flatTheme,
notificationCenter: notificationCenterFlatThemeConfig(flatTheme)
}
<ThemeProvider theme={newTheme} />
• Add the Notification Center global styles:
<ThemeProvider theme={newTheme}>
<GlobalStyles />
<NotificationCenterGlobalStyles />
<WidgetsRoot>
<ResizeablePage />
28

-- 28 of 107 --

</WidgetsRoot>
</ThemeProvider>
The Notification Bell Component
Add the HeaderItem into the ApplicationFrameLayout:
<FrameViews.ApplicationFrameLayout
{...props}
additionalHeaderItems={[
{item: <HeaderItem overflowCount={19} />,
orientation: "rightSlots-left"
}]
/>
Popup Notifications
Wrap the application by the NotificationFrame component as follows:
<NotificationFrame>
<AuthenticatedPage/>
</NotificationFrame>
Configure the A12 Modules
Sync Module
Register the NotificationHubModule into the A12 Modules:
const ddModuleInstance = createNotificationHubModule();
const moduleRegistry = ModuleRegistryProvider.getInstance();
moduleRegistry.addModule(ddModuleInstance);
/**
* On login, registers all modules that current user has access to.
*/
const registerModulesOnLoginMiddleware = StoreFactories.createMiddleware((api, next,
action) => {
if (UaaActions.loggedIn.match(action)) {
// Register all other modules except ddModuleInstance
}
return next(action);
});
NOTE By default, we only support UAA Local and OAuth2 authentication types. For other
29

-- 29 of 107 --

types, you need to provide the custom middleware to initialize A12 Data
Distribution's user as follows:
createNotificationHubModule({
initializeMiddleware: (ddConfig, ddContext) =>
StoreFactories.createMiddleware((api, next, action) => {
if (UaaActions.loggedIn.match(action)) {
api.dispatch(DDInitializationActions.initialize(ddConfig));
const ddContextWithUserInfo = merge(
{
session: {
userId, // Your mapped userId
username // Your mapped username
}
},
DEFAULT_DD_CONTEXT
);
api.dispatch(DDInitializationActions.initializeUser(merge(ddContextWithU
serInfo, ddContext)));
}
if (UaaActions.loggedOut.match(action)) {
api.dispatch(DDInitializationActions.deinitializeUser());
}
return next(action);
})
})
A12 Reminder Module
• Prepare the related models (Application, Form, Overview Models and screens) by using SME.
• Register the reminder module to A12 Modules without customization:
export const reminderModule = (): Module => ({
...ReminderFactories.createModule(),
model: () => model as ApplicationModel
});
• Register the reminder module to A12 Modules with customization:
export const reminderModule = (): Module => ({
...ReminderFactories.createModule(),
model: () => model as ApplicationModel,
30

-- 30 of 107 --

sagas: () => [...sagas, ...ReminderFactories.createSagas()],
middlewares: () => [...ReminderFactories.createMiddlewares(), ...middlewares],
views: () => viewProvider,
dataReducers: () => [...reminderFormDataReducer,
...ReminderFactories.createDataReducers()]
});
Configure Webpack
• Forward any requests matching the following patterns to the Notification Center Services:
{
[
{
context: ["/api/v2/sync"],
target: "http://localhost:8089",
secure: false,
changeOrigin: true,
logLevel: "debug"
}
]
}
• Resolve ES module:
[
// ... other modules
{
test: /\.m?js$/,
resolve: {
fullySpecified: false
}
}
]
6.2.3. Showcase Examples
Sending Notification From Backend Services
A12 Data Services on A12 Document Update Event
• Include ShowCaseReminder-document.
• Pushing a notification form A12 Data Services: PersonDocumentListener.java.
In this example, we push the notification on the Person document model events
(DocumentAfterCreateEvent, DocumentAfterUpdateEvent).
31

-- 31 of 107 --

Camunda Event
• Pushing a notification from camunda service: SendReassignmentNotificationListener.java.
Customize the Reminder Job Data Based on Reminder’s Events
• Adding additional data to the notification before a job is created
(ReminderBeforeScheduleJobEvent, ReferenceModelDataShowcase.java). In this example, we
add the docRef into the Notification data. On the A12 Client application, we can get the docRef
from the notification data and opens the corresponding Reminder form.
• Adding additional data to the notification before a job is executed
(ReminderJobDataBeforeExecuteEvent, RecipientsShowCase.java). In this example, before
sending the notification to the user, we add one more technical user into the recipient list.
Please have a look at Working With Reminder Jobs section for list of events.
Custom Notification Types
You can create your own Notification Type based on your requirements.
Backend
Spring properties
Configuration
property
Default value Usage Remark
mgmtp.a12.notification
center.notification.addi
tional-types
- A Spring property
define list of additional
types for notification
The value must be an
array and each element
must be separated by
"," character.
This example shows how you can create the VALIDATION_RESULT and REASSIGNMENT as your
own notification types:
• Provide the list of additional types in the backend:
mgmtp.a12.notificationcenter.notification.additional-
types=VALIDATION_RESULT,REASSIGNMENT
• Set the corresponding notification type when publishing new notification:
customNotification.setType("VALIDATION_RESULT");
Frontend
• Define the basic UI configurations for these new notification types in the A12 Client application
by using the interface NotificationTypeConfig in notificationcenter-
core/src/internal/configuration/types.ts.
32

-- 32 of 107 --

Example:
...
import { NotificationCenterContainer, rebound } from
"@com.mgmtp.a12.notificationcenter/notificationcenter-core";
...
rebound(NotificationCenterContainer.identifier.NotificationTypes, {
...DefaultNotificationTypes,
VALIDATION_RESULT: {
id: CustomNotificationType.VALIDATION_RESULT,
nameI18nKey:
RESOURCE_KEYS.notificationConfig.notificationType.validationResult.name,
pluralNameI18nKey:
RESOURCE_KEYS.notificationConfig.notificationType.validationResult.pluralName,
variant: "warning",
iconName: "warning_amber",
iconTheme: "outlined",
filterOptions:
getDefaultFilterOptionsByType(CustomNotificationType.VALIDATION_RESULT)
},
REASSIGNMENT: {
id: CustomNotificationType.REASSIGNMENT,
nameI18nKey:
RESOURCE_KEYS.notificationConfig.notificationType.reassignment.name,
pluralNameI18nKey:
RESOURCE_KEYS.notificationConfig.notificationType.reassignment.pluralName,
iconName: "info"
}
});
• For further UI customizations (show extra data, custom renderer, etc.) on the notification UI
components, you can do it by register your own UI components into our ComponentMap. All
supported Notification Center components are exposed under
DefaultNotificationComponentMap. For example:
rebound(NotificationCenterContainer.identifier.ComponentMap, {
...DefaultNotificationComponentMap,
NotificationToast: CustomNotificationToast,
NotificationItemCompact: CustomNotificationItemCompact,
NotificationTableContent: CustomNotificationTableContent,
NotificationActionItems: CustomNotificationActionItems
});
In the below example, we want to render the German Id Number in the Notification toast instead
of the created date.
export const CustomNotificationToast = (props: NotificationToastProps) => {
33

-- 33 of 107 --

const localizer = useLocalizer();
switch (props.notification.type) {
case CustomNotificationType.VALIDATION_RESULT:
return <ValidationResultNotificationToast {...props} />;
case CustomNotificationType.REASSIGNMENT:
return (
<DefaultNotificationComponentMap.NotificationToast
{...props}
additionalInfo={<span>{getIdNumberFromExtraData(props.notification)}</span>
|| null}
title={localizer(RESOURCE_KEYS.notificationConfig.notificationType.reassignment.title)
}
/>
);
default:
return <DefaultNotificationComponentMap.NotificationToast {...props} />;
}
};
Sync Notification Configurations
Configure How Frequent of the Sync Request
By default, we will pull the user’s notifications every 60s. You can configure it when creating the
NotificationHub module with the following parameters.
import {
createNotificationHubModule,
NotificationHubConfiguration
} from "@com.mgmtp.a12.notificationcenter/notificationcenter-client";
const exampleDDConfig: NotificationHubConfiguration = {
ddConfig: {
parameter: {
syncInterval: 20000
}
}
};
createNotificationHubModule(exampleDDConfig)
Limit the Number of Notifications to Be Synced to Backend in a Single Sync Request
In case the user marks read/unread/delete a big amount of notifications, it is possible that the user
will receive the HTTP 413 error since the request’s body is too big. To fix the issue, instead sending
all changes at once, we send them by chunks to the backend.
34

-- 34 of 107 --

Depending on your web server configuration, you can adjust the chunk size by using following code
(by default: 300 notifications per chunk).
rebound(NotificationCenterContainer.identifier.NotificationProcessChunkLength, {
delete: 100,
update: 100
})
Change the Browser Tab Title Based on the Number of Unread Notifications
To show the number of unread notifications in the browser tab title, you can add our custom hook
useTabTitleNotifications into your code:
import { useTabTitleNotifications } from
"@com.mgmtp.a12.notificationcenter/notificationcenter-core";
export const AuthenticatedPage = (): React.JSX.Element=> {
useTabTitleNotifications();
return (
<RegionUi ... />
);
};
Send Email Notification After Notification Publication
In some cases, you may want to send an email notification to the user after a notification is
published.
Backend
You can listen to the NotificationAfterPublishEvent event in the backend and trigger the email
sending logic in the event listener. Please refer to EmailNotificationShowCase.java for full code
example.
The list of all available events are described in Notification Publish Events.
Frontend
It is possible to register a custom Email Toggle component inside UserSettings in Notification Center
frontend, so that the user can decide whether to receive email notifications or not.
The approach is to override the NotificationSettingMainContent in DefaultNotificationComponentMap.
import {
DefaultNotificationComponentMap,
NotificationCenterContainer,
rebound
35

-- 35 of 107 --

} from "@com.mgmtp.a12.notificationcenter/notificationcenter-core";
rebound(NotificationCenterContainer.identifier.ComponentMap, {
...DefaultNotificationComponentMap,
NotificationSettingMainContent: CustomNotificationSettingMainContent
});
In the CustomNotificationSettingMainContent component, you can add your custom email toggle
component as below:
export const CustomNotificationSettingMainContent = (props?:
NotificationSettingMainContentProps) => {
const emailEnabled = props?.draftExtraData?.emailEnabled === true;
const handleChangeEmailEnabled = (value: boolean) => {
props?.changeExtraData?.({
...(props?.draftExtraData || {}),
emailEnabled: value
});
};
return (
<>
<SystemNotificationSettings />
<Switch
label="Enable email notifications"
checked={emailEnabled}
onChange={handleChangeEmailEnabled}
/>
<ToastSettings />
</>
);
};
Refer to NotificationSettingMainContent.tsx for the full code example.
6.3. Push Notifications
This guide shows how to integrate the core functionalities of Notification Center Push Notifications
into an A12 based project.
For advance configurations, please go to the Push Notification Services section.
6.3.1. Backend
• Configure UAA:
Our REST endpoints in Push Notification Services are secured using UAA, so you have to assign
36

-- 36 of 107 --

the appropriate access rights for user roles in your application. Please refer to the Push Notification
Authorization section for more details.
Push Notification Services
• Create a new Spring Boot application for Push Notification Services or use your existing
Spring Boot application.
• Install the service package by adding the following dependencies to your build.gradle:
implementation "com.mgmtp.a12.notificationcenter:notificationcenter-push-notification-
service:<VERSION>"
implementation "org.postgresql:postgresql:42.5.6"
• Add the annotation @EnablePushNotificationServer to the spring boot application class:
@SpringBootApplication
@EnablePushNotificationServer
public class NotificationCenterServiceApplication {
public static void main(String[] args) {
SpringApplication.run(NotificationcenterApplication.class, args);
}
}
• Prepare the required credentials for the Apple Push Notification service:
◦ Go to Account > Keys page, click on plus button as belows:
◦ Enter the auth key’s name and check the APNs checkbox.
37

-- 37 of 107 --

◦ Click the Register button.
◦ Download the auth key file and store the information about TeamId and KeyId. Later on,
you will need them to configure the Push Notification service.
38

-- 38 of 107 --

• Prepare the required credentials for Firebase Cloud Messaging.
◦ Access Firebase console at https://console.firebase.google.com and create a new project.
◦ Open the created project in Firebase > Project settings.
◦ Go to Project settings > Service account > Firebase Admin SDK > Generate new private key.
39

-- 39 of 107 --

• Configure the application properties as follows:
# Push Notification datasource configuration
spring.datasources.pushnotification.url=<JDBC_URL>
spring.datasources.pushnotification.username=<USERNAME>
spring.datasources.pushnotification.password=<USERNAME>
spring.datasources.pushnotification.driver-class-name=org.postgresql.Driver
spring.datasources.pushnotification.jpa.database-
platform=org.hibernate.dialect.PostgreSQLDialect
# Push Notification configuration
mgmtp.a12.notificationcenter.pushnotification.apns.bundle-id=<YOUR_APP_BUNDLE_ID>
mgmtp.a12.notificationcenter.pushnotification.apns.pkcs8-file-
path=<THE_PATH_TO_P8_TOKEN>
mgmtp.a12.notificationcenter.pushnotification.apns.team-id=<TEAM_ID>
mgmtp.a12.notificationcenter.pushnotification.apns.key-id=<KEY_ID>
mgmtp.a12.notificationcenter.pushnotification.fcm.service-account-credential-
path=<THE_PATH_TO_SERVICE_ACCOUNT_CREDENTIAL>
Configure Push Notification REST Client
If your backend service, such as A12 Data Services, needs to send notifications to users, you can
utilize the Push Notification Rest Client package. This package allows a service to
programmatically register, deregister the user devices and send Push Notifications by calling Push
Notification service’s REST APIs from the backend code.
• Install our notificationcenter-push-notification-rest-client package into your service by
adding the following dependency to your build.gradle:
40

-- 40 of 107 --

implementation "com.mgmtp.a12.notificationcenter:notificationcenter-push-
notification-rest-client:<VERSION>"
• Import configurations PushNotificationClientConfiguration as follows:
@Import(PushNotificationClientConfiguration.class)
@DataServicesApplication(scanBasePackages =
{DataServicesApplication.DATASERVICES_BASE_PACKAGE, "com.mgmtp.a12.*",
"com.mgmtp.a12.notificationcenter.dataservice"})
public class DataServiceApplication {
public static void main(String[] args) {
SpringApplication.run(DataServiceApplication.class, args);
}
}
• Configure UAA Rest Client.
• Configure the Push Notification Service URL:
mgmtp.a12.notificationcenter.pushnotification.client.base-
url=http://localhost:8089/api/push-notification
• Example of sending a Push Notification on the Person document model event
(DocumentAfterCreateEvent):
@Component
@AllArgsConstructor
public class PersonDocumentListener {
private final NotificationApiConnector notificationApiConnector;
@EventListener
public void listenCreatingPerson(DocumentAfterCreateEvent event) {
DocumentV2 document = event.getDataServicesDocument().getKernelDocument();
if (isPersonDocument(document)) {
CreatePushNotificationRequest request = new
CreatePushNotificationRequest();
PushNotification pushNotification = new PushNotification();
pushNotification.setTitle("Push Notification");
pushNotification.setBody("A new user has been created");
request.setNotification(pushNotification);
request.setAccountId("example-account-id");
pushNotification(createPushNotificationRequest);
}
}
41

-- 41 of 107 --

private void pushNotification(CreatePushNotificationRequest notification) {
notificationApiConnector.createPushNotification(notification);
}
}
Configure Register/Deregister Devices Endpoints
In case the mobile application doesn’t have UAA user context, you can create the wrapper for our
endpoints as below:
@RestController
@RequestMapping("/api/push-notification/sample")
@RequiredArgsConstructor
public class PushNotificationController {
private final PushNotificationService pushNotificationService;
@PostMapping("/devices")
public void registerDevice(@RequestBody RegisterDeviceRequest
registerDeviceRequest) {
pushNotificationService.registerDevice(registerDeviceRequest);
}
@DeleteMapping("/devices/{deviceId}")
public void deregisterDevice(@PathVariable("deviceId") String deviceId) {
pushNotificationService.deregisterDevice(deviceId);
}
}
The endpoint paths might vary, you must ensure the payload contains sufficient data for the
register/deregister process.
With the data received from the frontend side, call the requests to the Push Notification Service:
/**
* Sample service, just a simple service to handle request to Push Notification
features from sample controller
*/
@Service
@RequiredArgsConstructor
public class PushNotificationService {
private final DeviceApiConnector deviceApiConnector;
private final NotificationApiConnector notificationApiConnector;
/**
* Register device to receive Push Notifications from Notification Center services
* @param registerDeviceRequest Request to register device
*/
42

-- 42 of 107 --

public void registerDevice(RegisterDeviceRequest registerDeviceRequest) {
deviceApiConnector.registerDevice(registerDeviceRequest);
}
/**
* Deregister a device. This device will no longer receive Push Notifications from
Notification Center services
* @param deviceId Device id of device to deregister
*/
public void deregisterDevice(String deviceId) {
deviceApiConnector.deregisterDevice(deviceId);
}
}
6.3.2. Frontend
NOTE Pushing notification on the client side could be implemented by various ways. In
this guide, we will use Capacitor as the platform of choice
Dependencies
• Add the Capacitor Push Notifications Plugin into your project and configure it accordingly:
yarn add @capacitor/push-notifications
• To configure the Google Push Service on Android, place the google-services.json to android/app
folder
Configure Webpack Dev Server for Local Development
Forward any requests matching the following patterns to the Push Notification Service:
module.exports = {
devServer: {
proxy: [
{
context: ["/api/push-notification"],
target: "http://localhost:8086",
secure: false,
changeOrigin: true,
logLevel: "debug"
}
]
}
}
43

-- 43 of 107 --

Set up API Requests
Depends on your project’s API Client library, you must set up the authentication method for your
API client instance to request to the exposed backend endpoint in the previous steps.
For example, using axios:
let AxiosInstance: AxiosInstance = axios.create();
export const registerUser = async (config: PushConfig): Promise<void> => {
return AxiosInstance.post("devices", config);
};
export const unregisterUser = async (deviceId: PushConfig["deviceId"]): Promise<void>
=> {
return AxiosInstance.delete(`devices/${deviceId}`);
};
You can also use Utils Server Connector and UAA JavaScript Client:
const uaaLocalClient: UaaLocalClient = UaaFactories.localClientSetup({
timeout: 10000,
serverURL: "/api"
});
uaaLocalClient.initConnector();
then:
import { ConnectorLocator,RestRequestPayload,RestServerConnector } from
"@com.mgmtp.a12.utils/utils-connector";
import { PushConfig } from "./push-config";
export async function fetchData(requestPayload: RestRequestPayload):
Promise<Response> {
return (ConnectorLocator.getInstance().getServerConnector() as
RestServerConnector).fetchData(requestPayload);
}
export const registerUser = async (config: PushConfig): Promise<void> => {
await fetchData({
relativeUrl: "devices",
method: "POST",
body: config
});
};
export const unregisterUser = async (deviceId: PushConfig["deviceId"]): Promise<void>
=> {
44

-- 44 of 107 --

await fetchData({
relativeUrl: `devices/${deviceId}`,
method: "DELETE"
});
};
Register the Device for the Push Notifications
In your React app, use the @capacitor/push-notifications to register the device:
import { PushNotifications, PushNotificationSchema, Token } from '@capacitor/push-
notifications';
useEffect(() => {
// Request permission from the user to receive push notifications
let permStatus = await PushNotifications.checkPermissions();
if (permStatus.receive === 'prompt') {
permStatus = await PushNotifications.requestPermissions();
}
if (permStatus.receive !== 'granted') {
throw new Error('User denied permissions!');
}
await PushNotifications.register();
const onRegistration = (data: Token) => {
// After starting the mobile app, we will receive a registration ID from the push
notification provider
// You can store this registration ID to register/deregister the device to/from
the Push Notification Service
};
const onNotification = (notificationSchema: PushNotificationSchema) => {
// Handle logic when a push notification is received
};
const onError = (data: RegistrationError) => {
// handle logic when an error occurs during registration
};
PushNotifications.addListener("registration", onRegistration);
PushNotifications.addListener("pushNotificationReceived", onNotification);
PushNotifications.addListener("registrationError", onError);
return () => {
PushNotifications.removeAllListeners()
};
}, []);
With the registration data from the plugin, you can now register/deregister the device to the Push
45

-- 45 of 107 --

Notifications:
const enablePushNotification = useCallback(() => {
if (registrationData?.value) {
registerUser({
accountId: userId,
deviceId: registrationData.value,
platform
})
.then(() => {
setPushEnabled(true);
})
.catch(error => {
console.error("Error when enabling Push Notifications", error);
});
}
}, [enablePushNotificationMutate, userId, registrationData?.value]);
const disablePushNotification = useCallback(() => {
if (registrationData?.value) {
unregisterUser(registrationData.value)
.then(() => {
setPushEnabled(false);
})
.catch(error => {
console.error("Error when disabling Push Notifications", error);
});
}
}, [disablePushNotificationMutate, registrationData?.value]);
7. Build and Deployment
7.1. Build and Publish Docker Image
This section describe how to build and publish an application’s docker image which uses A12
Notification Center components.
7.1.1. Customize Gradle Build
In build.gradle:
• Add Spring Boot Gradle Plugin.
id 'org.springframework.boot' version '<VERSION>'
• Set duplicatesStrategy property’s value of task named bootJar.
46

-- 46 of 107 --

tasks.named("bootJar") {
enabled = true
duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
• Customize the task named bootBuildImage (reference Spring Boot Gradle plugin).
tasks.named("bootBuildImage") {
builder = "<BUILER_IMAGE_NAME_TO_USE>"
runImage = "<RUN_IMAGE_TO_USE>"
imageName = "<GENERATED_IMAGE_NAME>"
docker {
builderRegistry {
username = <AUTHENTICATION_BUILDER_REGISTRY_USERNAME>
password = <AUTHENTICATION_BUILDER_REGISTRY_PASSWORD>
url = "${pullRegistry}/v1/"
}
publishRegistry {
username = project.findProperty("username")
password = project.findProperty("password")
url = "${registry}/v1/"
}
}
}
Below is an example:
def registry = project.findProperty("dockerRegistryForPublish")
def pullRegistry = project.findProperty("dockerRegistryForRead")
bootBuildImage {
builder = "${pullRegistry}/paketobuildpacks/builder-jammy-base:latest"
runImage = "${pullRegistry}/paketobuildpacks/run-jammy-base:latest"
imageName = "${fullTag}"
docker {
builderRegistry {
username = project.findProperty("username")
password = project.findProperty("password")
url = "${pullRegistry}/v1/"
}
publishRegistry {
username = project.findProperty("username")
password = project.findProperty("password")
url = "${registry}/v1/"
}
}
47

-- 47 of 107 --

}
We suggest to use tasks for multiple purposes like below:
tasks.register("buildImage") {
dependsOn bootBuildImage
}
tasks.register("pushImage") {
bootBuildImage.publish = true
dependsOn buildImage
}
7.1.2. Execution
• To build image, run:
gradle buildImage
• To publish image, run:
gradle publishImage
7.2. Deploy in Kubernetes Environment
The A12 Notification Center chart supports you in deploying an A12 Notification Center application
in the Kubernetes environment.
7.2.1. Artifact
Artifact Artifact ID Description
a12-notificationcenter-service a12-notificationcenter-service The artifact which provides the
backend for the Notification
Center Services.
7.2.2. Prerequisites
• Kubernetes 1.19+
• Helm 3.5.0+
7.2.3. Usage Examples
The chart could be used as a dependency:
48

-- 48 of 107 --

dependencies:
- name: a12-notificationcenter-service
version: <VERSION>
repository: "@helm-repos"
or deploy directly by using helm command:
export KUBECONFIG=/path/to/your/.kube/config
helm upgrade a12-notificationcenter helm-repos/a12-notificationcenter-service \
-f values.yaml \
-n namespace
In both cases, you need to define the Notification Center Services configuration values under a12-
notificationcenter-service scope.
The example below shows some minimum values you would need to deploy the Notification Center
service:
a12-notificationcenter-service:
image:
repository: $dockerImage
version: $dockerImageVersion
models:
reminder: ShowCaseReminder-document
ingress:
default:
host: "a12-client.{{ .Release.Namespace }}.{{ .Values.global.cluster.domainName
}}"
extraPaths: |
- path: /(api/v2/sync.*)
pathType: ImplementationSpecific
backend:
service:
name: a12-nc-showcase
port:
number: 8080
annotations: |-
kubernetes.io/ingress.class: nginx
nginx.ingress.kubernetes.io/rewrite-target: /\$1
nginx.ingress.kubernetes.io/proxy-body-size: 10m
management:
enabled: true
uaa:
enabled: true
restClient:
enabled: true
49

-- 49 of 107 --

baseUrl: "http://a12-nc-showcase:8080/api"
database:
connectionURI: 'jdbc:postgresql://{{ .Values.global.infrastructure.name }}-
postgresql.{{ .Release.Namespace }}.svc.cluster.local:5432/a12-notificationcenter-
service'
driverClassName: "org.postgresql.Driver"
secret:
enabled: true
pushNotification:
enabled: true
extraEnvVars: |
- name: MGMTP_A12_UAA_AUTHENTICATION_CORS_ALLOWEDORIGINS
value: "*"
- name: MGMTP_A12_UAA_AUTHENTICATION_JWT_HEADERNAME
value: Authorization
- name: MGMTP_A12_UAA_AUTHORIZATION_CHILDAUTHORIZATIONDEFINITIONS
value: "classpath:uaa/additionalAuthorizationDefinition.json"
- name: SPRING_DATASOURCE_DRIVERCLASSNAME
value: "org.postgresql.Driver"
- name: SPRING_JPA_DATABASEPLATFORM
value: "org.hibernate.dialect.PostgreSQL9Dialect"
- name:
MGMTP_A12_UAA_AUTHENTICATION_CLIENT_REST_GENERATEDTOKENEXPIRATIONHEADERNAME
value: "id_token_expiration"
- name: SPRING_APPLICATION_JSON
value: '{ "spring.quartz.properties.org.quartz.jobStore.driverDelegateClass":
"org.quartz.impl.jdbcjobstore.PostgreSQLDelegate" }'
- name: MGMTP_A12_UAA_AUTHENTICATION_USER_ACCESSRIGHTSRESOURCE
value: "classpath:uaa/roles.yaml"
- name: MGMTP_A12_UAA_AUTHORIZATION_AUTHORIZATIONDEFINITION
value: "classpath:uaa/authorizationDefinition.json"
- name: MGMTP_A12_UAA_AUTHENTICATION_USER_LOCALCONFIG_USERRESOURCES
value:
"classpath:users/admin.yaml,classpath:users/guest.yaml,classpath:users/test.yaml,class
path:users/technical.yaml"
WARNING
• In order to work with A12 Data Distribution, the Notification Center
needs an extra path and an annotation in the K8s ingress configuration
values:
extraPaths: |
- path: /(api/v2/sync.*)
pathType: ImplementationSpecific
backend:
service:
50

-- 50 of 107 --

name: a12-nc-showcase
port:
number: 8080
annotations: |-
nginx.ingress.kubernetes.io/rewrite-target: /\$1
The purpose is to forward all requests coming with path /(api/v2/sync.*) to
the Notification Center Services component to perform the sync request.
Use Fullname for K8s Objects
By default, we set the value for fullnameOverride in our Helm chart value file as follows:
fullnameOverride: a12-notificationcenter-service
If you want to use fullname (format as Release.Name-Chart.Name) for all K8s objects, you must
override the fullnameOverride, commonLabels and podLabels as follows:
fullnameOverride: ""
commonLabels:
app.kubernetes.io/name: '{{ include "common.names.fullname" . }}-nc'
podLabels:
app.kubernetes.io/name: '{{ include "common.names.fullname" . }}-nc'
Working with ingress in TPI Cluster
In the latest ingress controller running on the TPI Cluster, the validation enforces character
restrictions for paths with the Exact or Prefix types.
To include a rewrite configuration in the ingress path, the path type must be set to
ImplementationSpecific.
...
- path: /(api/v2/sync.*)
pathType: ImplementationSpecific
...
Secret
The A12 Notification Center requires some Kubernetes Secrets objects that reference credentials in
the system, such as database passwords and private keys, etc. There are two main ways to handle
secrets:
• Manually create Kubernetes Secrets You can manually create Kubernetes Secrets and configure
the A12 Notification Center Helm chart to reference the created objects using secretRef.
• Use the Kubernetes Secrets template from the A12 Notification Center chart:
51

-- 51 of 107 --

◦ Step 1: Enable Secrets creation for each feature By default, Secrets creation is disabled for
all features in the chart. You must explicitly enable it for each feature if needed. This
configures the chart to create Secret objects for the Push Notification feature.
secret:
enabled: true
pushNotification:
enabled: true
• Step 2: Specify raw credentials in the secret values file For example, you can specify the Pkcs8
token for Push Notification feature as follows:
a12-notificationcenter-service:
secret:
pushNotification:
apns:
pkcs8Base64: Sample_base64
fcm:
serviceAccountCredentialBase64: Sample_base64
8. Notification Center Services
8.1. Web Notifications
8.1.1. Database Configuration
Due to the limitations in A12 Data Distribution, our dependency, so we are only able to support
two specific database types:
• Postgres: PostgreSQLDialect.
• Oracle: OracleDialect, Oracle9iDialect, Oracle10gDialect.
Embedded Postgres
The Notification Center service supports an embedded Postgres instance for local development
and testing without requiring an external database server. This is powered by
io.zonky.test:embedded-postgres.
Activate the notificationcenter-embedded_postgres Spring profile, or set the property directly:
spring.datasources.notificationcenter.embedded-postgres.enabled=true
All embedded Postgres configuration properties:
52

-- 52 of 107 --

Property Default value Description
spring.datasources.notificatio
ncenter.embedded-
postgres.enabled
false Enables the embedded Postgres
instance.
spring.datasources.notificatio
ncenter.embedded-postgres.port
5434 TCP port for the embedded
instance.
spring.datasources.notificatio
ncenter.embedded-postgres.path
null (system temp dir) Path to a directory for
persisting data across restarts.
When null, data is stored in a
temporary directory.
spring.datasources.notificatio
ncenter.embedded-
postgres.clean-data-directory
false Deletes the data directory on
startup. Set to true in tests for
isolation.
WARNING: Never set this to
true in production — all data
will be deleted on startup.
spring.datasources.notificatio
ncenter.embedded-
postgres.locale-c-type
en_US.UTF-8 Locale option set during initdb.
spring.datasources.notificatio
ncenter.embedded-
postgres.connect-config.*
- JDBC/driver connection
parameters.
spring.datasources.notificatio
ncenter.embedded-
postgres.postgres-config.*
- Options passed to the pg_ctl
command.
spring.datasources.notificatio
ncenter.embedded-
postgres.override-working-
directory
null Override directory for extracted
Postgres binaries. Useful for
persisting binaries across runs.
8.1.2. Spring Configuration
Database Configurations
Property Default value Required Description
spring.datasources.not
ificationcenter.url
- true Database connection
string.
spring.datasources.not
ificationcenter.userna
me
- true Database username.
spring.datasources.not
ificationcenter.passwo
rd
- true Database password.
53

-- 53 of 107 --

Property Default value Required Description
spring.datasources.not
ificationcenter.driver
-class-name
- true The database driver
classname. For
example:
org.postgresql.Driver.
spring.datasources.not
ificationcenter.jpa.da
tabase-platform
- true The database platform.
For example:
org.hibernate.dialect.
PostgreSQLDialect.
spring.datasources.not
ificationcenter.jpa.hi
bernate.ddl-auto
none false Disable auto ddl auto
generation of Spring
JPA.
spring.datasources.not
ificationcenter.jpa.op
en-in-view
false false disable default
transaction of Spring
JPA.
spring.datasources.not
ificationcenter.liquib
ase.change-log
classpath:database/noti
ficationcenter_model.x
ml
false liquibase change log
file path.
spring.quartz.properti
es.org.quartz.jobStore
.driverDelegateClass
Spring managed true Spring target should be
auto-detected, but not
working for Postgres,
so there is more secure
to configure DB type
for Quartz manually.
Should be class of type
org.quartz.impl.jdbcjob
store.StdJDBCDelegate
(one of
org.quartz.impl.jdbcjob
store.StdJDBCDelegate,
org.quartz.impl.jdbcjob
store.PostgreSQLDelega
te,
org.quartz.impl.jdbcjob
store.oracle.OracleDele
gate).
Notes:
• There are additional tuning properties depending on the connection pool implementation. By
default, Spring uses com.zaxxer.hikari.HikariDataSource.
#Timeout 30 seconds. See https://github.com/brettwooldridge/HikariCP#frequently-used
spring.datasources.notificationcenter.hikari.connection-timeout=30000
54

-- 54 of 107 --

• If you want to change the Quartz job database related configurations (Liquibase), you can set
the following properties:
spring.quartz.properties.org.quartz.jobStore.tablePrefix=nc_qrtz_
spring.datasources.notificationcenter.liquibase.parameters.table_prefix=NC_QRTZ_
spring.datasources.notificationcenter.liquibase.database-change-log-lock-
table=NC_DATABASECHANGELOGLOCK
spring.datasources.notificationcenter.liquibase.database-change-log-
table=NC_DATABASECHANGELOG
Connection Pool Configuration
The Notification Center service uses HikariCP as the database connection pool implementation. You
can tune the connection pool settings based on your application’s requirements:
# Maximum number of connections in the pool (default: 10)
spring.datasources.notificationcenter.hikari.maximum-pool-size=5
# Minimum number of idle connections maintained by HikariCP (default: same as maximum-
pool-size)
spring.datasources.notificationcenter.hikari.minimum-idle=2
# Maximum time (in milliseconds) to wait for a connection from the pool (default:
30000)
spring.datasources.notificationcenter.hikari.connection-timeout=30000
# Maximum time (in milliseconds) that a connection can sit idle in the pool (default:
600000)
spring.datasources.notificationcenter.hikari.idle-timeout=600000
# Maximum lifetime (in milliseconds) of a connection in the pool (default: 1800000)
spring.datasources.notificationcenter.hikari.max-lifetime=1800000
TIP
For optimal performance, consider the following guidelines:
• maximum-pool-size: Set based on your database’s connection limit and
application load. The default value of 10 may be higher than necessary for low-
traffic applications. Consider reducing it to 5 or lower if your application handles
fewer concurrent requests.
• minimum-idle: Setting this lower than maximum-pool-size allows the pool to shrink
during periods of low activity, freeing up resources. A common practice is to set it
to about 20-50% of the maximum pool size.
• For more information on HikariCP configuration, refer to the HikariCP
Configuration Documentation.
55

-- 55 of 107 --

Transaction Manager
Specify the transactionManager's value for @Transactional.
• By default, the @Transactional annotation uses the default ncTransactionManager, to make it
explicit:
◦ For logic related to Reminder, specify @Transactional("ncTransactionManager").
◦ For logic related to A12 Data Distribution , specify @Transactional("ddTransactionManager").
Notification Related Configurations
Property Default value Required Description
mgmtp.a12.notification
center.notification.ad
ditional-types
- false Additional notification
types. The value must
be an array and each
element must be
separated by ","
character.
mgmtp.a12.notification
center.notification.ex
piration.expiration-
date
- false The notification will be
expired (deleted) in
how many days. If it’s
empty, the notifications
will never be deleted.
mgmtp.a12.notification
center.notification.ex
piration.enable-clean-
up-job
false false Enable/disable the
cleanup job, when the
job enabled, you must
provide the expression
for scheduler the clean
up job.
mgmtp.a12.notification
center.notification.ex
piration.clean-up-
notification-
scheduler-expression
- false The Quartz expression
for the execution time
of the notification
garbage collection job.
See http://www.quartz-
scheduler.org/
documentation/quartz-
2.3.0/tutorials/
crontrigger.html.
56

-- 56 of 107 --

Property Default value Required Description
mgmtp.a12.notification
center.notification.ex
piration.garbage-
collection-scheduler-
expression
- false The Quartz expression
for the cleanup
notification scheduler,
which specifies the
execution time for the
notification cleanup
cron job See
http://www.quartz-
scheduler.org/
documentation/quartz-
2.3.0/tutorials/
crontrigger.html.
mgmtp.a12.dd.server.ar
chive.retention_time `
30 false How many days should
deleted-entries remain
in the database before
they will be archived.
mgmtp.a12.dd.server.ar
chive.processing.limit
10000 false Specifies the maximum
number of entries to be
archived in a single
run. The value < 0
stands for no limit.
mgmtp.a12.dd.server.ar
chive.schema.name
NO_ARCHIVE_SCHEMA false Name of the archive
database schema. By
default, we don’t create
the archive schema.
8.1.3. Spring Profiles
We offer the following configuration profiles for the Notification Center Service.
A12 Data Distribution Configuration
• Profile name:
◦ notificationcenter-datadistribution
• Profile contents:
mgmtp.a12.dd.server.archive.retention_time=30
mgmtp.a12.dd.server.archive.processing.limit=10000
mgmtp.a12.dd.server.archive.schema.name=NO_ARCHIVE_SCHEMA
Datasources Configuration
• Profile name:
57

-- 57 of 107 --

◦ notificationcenter-datasources
• Profile contents:
spring.datasources.notificationcenter.jpa.open-in-view=false
spring.datasources.notificationcenter.jpa.hibernate.ddl-auto=none
spring.datasources.notificationcenter.liquibase.change-
log=classpath:database/notificationcenter_model.xml
Embedded Postgres Configuration
• Profile name:
◦ notificationcenter-embedded_postgres
• Profile contents:
spring.datasources.notificationcenter.embedded-postgres.enabled=true
spring.datasources.notificationcenter.driver-class-name=org.postgresql.Driver
spring.datasources.notificationcenter.jpa.database-
platform=org.hibernate.dialect.PostgreSQLDialect
NOTE Activate this profile together with notificationcenter-datasources for local
development or tests without an external database. ===== Quartz Configuration
• Profile name:
◦ notificationcenter-quartz
• Profile contents:
spring.quartz.job-store-type=jdbc
spring.quartz.properties.org.quartz.scheduler.instanceId=AUTO
spring.quartz.jdbc.initialize-schema=never
spring.quartz.overwrite-existing-jobs=true
UAA Configuration
• Profile name:
◦ notificationcenter-uaa
• Profile contents:
mgmtp.a12.uaa.authorization.authorization-
definition=classpath:uaa/authorizationDefinition.json
58

-- 58 of 107 --

8.1.4. Security
Authentication
Authentication is completely handled by UAA, refer to their documentation for more information.
Authorization
Notification Center Services introduces a new authorizationDefinition.json file for securing our
APIs. It uses mgmtp.a12.uaa.authorization.authorizationDefinition for integrating with UAA.
IMPORTANT
Do not replace our authorizationDefinition.json, otherwise you disable our
authorization completely, meaning that there is no guarantee that your code
will work as expected. Instead, use
mgmtp.a12.uaa.authorization.childAuthorizationDefinitions to introduce
your own authorization rules on top of the already existing ones.
You are free to define your own (additional) permissions and policies, and organize them as you
want. You may define permission for following scopes:
Scope name Description
Push Notification Check that the user has access right to push a
notification.
Endpoint You can control endpoint security inside this
scope. Each endpoint is defined by its class
name/method name. Returns always true.
Authorization scopes used in the code:
Method name and arguments Scope name Description
NotificationController.create
notification
com.mgmtp.a12.notificationcen
ter.shared.notification.data.Not
ification
PreAuthorize
PushNotification
Check that the user
has access right to
push a notification.
8.1.5. Encryption
A12 Data Distribution Entry Encryption
In the Notification Center, we also provided the ability to encrypting the sensitive data in the
notification or either user setting entry:
public interface EncryptionService {
EncryptedContent encrypt(String var1);
String decrypt(EncryptedContent var1) throws UnsupportedEncryptionMethodException;
59

-- 59 of 107 --

...
}
There is an EncryptionService interface which provides encrypt and decrypt methods. Here you
could implement your own way to secure the sensitive data.
The interface EncryptionServiceProvider is used to register the EncryptionService:
public interface EncryptionServiceProvider {
EncryptionService getEncryptionService();
boolean isEncryptionEnabled();
}
JobDataMap Encryption
The notification content is also stored in JobDataMap - which is used to generate the notification.
In order to encrypt that data, you can use the ReminderJobDataBeforeScheduleEvent - the events
will be published before scheduling the job:
/**
* Event published before a reminder job data is used to schedule a job
*/
@Getter
@AllArgsConstructor
public class ReminderJobDataBeforeScheduleEvent {
private JobDataMap jobDataMap;
}
To decrypt the data in the JobDataMap, you can use the event
ReminderJobDataBeforeExecuteEvent - which will be published before job execution:
/**
* Event published before a reminder job data is used to execute
*/
@Getter
@AllArgsConstructor
public class ReminderJobDataBeforeExecuteEvent {
private JobDataMap jobDataMap;
}
8.1.6. Notification Lifecycle
The Notification Center provides a property to configure how long the notification is active:
60

-- 60 of 107 --

mgmtp.a12.notificationcenter.notification.expiration.expiration-date
• If the property is not specified, the notifications will never be deleted.
• If the property is specified, the value will apply for all notifications published by the
Notification Center. Each notification will have an expiration date - count by the current time
plus the number provided in days. This date is set during the creation of the notification
based on the configured calculation at that time. When the date is reached, the notification will
be marked as finished and a finished date will be set. Finished notifications will not be visible in
the client application.
In the life cycle of notification, we have introduced another configuration retention-time-in-days
with default value is 30 days. The number represents for how long we want to keep the expired
notifications. The value can be adjusted by using the property from the A12 Data Distribution.
mgmtp.a12.dd.server.archive.retention_time
Publish Events
The Notification Center publishes lifecycle events around
NotificationPublisher.pushNotification(…).
Event Trigger time Usage
NotificationBeforePublishEvent Before the dispatch request is
sent to Data Distribution
Validate or enrich integration-
specific processing before
publication
NotificationAfterPublishEvent After the dispatch request is
successfully sent to Data
Distribution
Trigger post-publish
integrations such as email
notification hooks
Example listener:
@Configuration
public class EmailNotificationShowCase {
@EventListener
public void listenNotificationAfterPublishEvent(NotificationAfterPublishEvent
event) {
// Integration-specific handling
}
}
WARNING
The NotificationPublisher.pushNotification is called within a transaction, and
the notification is persisted when the transaction commits. Therefore, if you
want to query related information from database such as User Settings in the
61

-- 61 of 107 --

listener, you would need to use @TransactionalEventListener instead of
@EventListener.
Please have a look at the showcase example section for more details.
Garbage Collection
GarbageCollectionScheduler triggers the garbage collection process. You can change the trigger
time by below configuration:
mgmtp.a12.notificationcenter.notification.expiration.garbage-collection-scheduler-
expression
When the GarbageCollectionScheduler runs, it will:
• mark all notifications whose expiration-date is in the past as finished and set the finished date
date to the current timestamp.
• mark all Notifications whose finished date is further in the past as the current date minus the
retention-time-in-days as deleted.
We will remove these deleted notifications completely later by using
CleanUpNotificationScheduler.
Completed Clean up the Notification
The Garbage Collection only marks notifications as Deleted. They still remain on the database. You
can use the CleanUpNotificationScheduler to remove them completely.
In order to delete the notifications, the schema name must be set to NO_ARCHIVE_SCHEMA, it
means deleted without archive the notifications into another table. The default value of this
property is NO_ARCHIVE_SCHEMA as follow:
mgmtp.a12.dd.server.archive.schema.name=NO_ARCHIVE_SCHEMA
The CleanUpNotificationScheduler is disabled by default. So if you want to completed clean up
the notification, you must enable the job with the property:
mgmtp.a12.notificationcenter.notification.expiration.enable-clean-up-job
The limit number of entries to be cleaned up in a single run can be adjusted by the property
provided by A12 Data Distribution (The default value is 10000). If the value ⇐0, it means cleaning
up notifications without limit.
mgmtp.a12.dd.server.archive.processing.limit
62

-- 62 of 107 --

NOTE
• If you configure the expiration-date property, you must provide the spring
expression for scheduling the garbage collection and cleanup notification job,
otherwise, the application will not able to be start up.
• Only the notification has the expiration time, while the user setting doesn’t.
8.1.7. Timezone
The default timezone of the Notification Center Services is Europe/Berlin, but you can override
this by implement the interface TimezoneResolver.
See the default implementation DefaultTimezoneResolver.
8.1.8. Notification Reminder Job
The (java) library, which needs to be installed inside Notification Center Services to handle
Reminder Job logics.
Artifact
Artifact Artifact ID Description
notificationcenter-reminder-
job
com.mgmtp.a12.notificationcent
er:notificationcenter-
reminder-job
The (java) library, which is
installed inside Notification
Center Services to handle
Reminder Job logics.
Configurations
Autoconfiguration
@Import({NotificationCenterReminderJobConfiguration.class}) // Use this annotation to
enable autoconfiguration
public class NotificationCenterServiceApplication {
public static void main(String[] args) {
final SpringApplication application = new
SpringApplication(NotificationCenterServiceApplication.class);
application.run(args).start();
}
}
Spring properties
Property Default value Required Description
mgmtp.a12.notification
center.reminder.model-
name
- true Your custom A12
Reminder Model name.
63

-- 63 of 107 --

Property Default value Required Description
mgmtp.a12.notification
center.dataservices.ba
se-url
- true Base A12 Data
Services URL.
mgmtp.a12.notification
center.reminder.job.ma
rk-due.enabled
false false Enable/disable the
reminder mark DUE
cron job.
mgmtp.a12.notification
center.reminder.job.ma
rk-due.scheduler-
expression
- false The Quartz expression
for reminder mark due
job, to define the
execution time for the
garbage collection job.
See: http://www.quartz-
scheduler.org/
documentation/quartz-
2.3.0/tutorials/
crontrigger.html.
mgmtp.a12.notification
center.reminder.job.ma
rk-due.limit
100 false Specifies the maximum
number of reminders
to be processed in a
single run.
Spring Profiles
We offer the following configuration profile for the Reminder Job package.
• UAA configuration:
◦ Profile name:
▪ notificationcenter-reminderjob-uaa
◦ Profile contents:
mgmtp.a12.uaa.authorization.child-authorization-
definitions=classpath:uaa/ncReminderJobAuthorizationDefinition.json
Authorization
Notification Center Reminder Job introduces a new ncReminderJobAuthorizationDefinition.json file
for securing our APIs. It uses mgmtp.a12.uaa.authorization.child-authorization-definitions for
integrating with UAA.
IMPORTANT
If you have a custom configuration for the key
mgmtp.a12.uaa.authorization.child-authorization-definitions then make
sure our file ncReminderJobAuthorizationDefinition.json should be added as
well.
64

-- 64 of 107 --

You are free to define your own (additional) permissions and policies, and organize them as you
want. You may define permission for following scopes:
Scope name Description
Schedule Reminder Job Check that the user has access right to schedule
a reminder job.
Reschedule Reminder Job Check that the user has access right to
reschedule a reminder job.
Delete Reminder Job Check that the user has access right to delete a
reminder job.
Authorization scopes used in the code:
Method name and arguments Scope name Description
ReminderJobService.scheduleJob
document
com.mgmtp.a12.kernel.md.doc
ument.apiV2.immutable.Docu
mentV2
PreAuthorize
ScheduleReminderJob
Check that the user has
access right to schedule
a reminder job.
ReminderJobService.rescheduleJob
document
com.mgmtp.a12.kernel.md.doc
ument.apiV2.immutable.Docu
mentV2
PreAuthorize
RescheduleReminderJob
Check that the user has
access right to
reschedule a reminder
job.
ReminderJobService.deleteJob
documentId
java.lang.String
PreAuthorize
DeleteReminderJob
Check that the user has
access right to delete a
reminder job.
Working With Reminder Jobs
Events
List of events supported by Notification Center Reminder Job:
No Event function name Policy Description
1 ReminderBeforeScheduleJobE
vent
The event is published before
the computation of a new cron
job to trigger notification.
2 ReminderJobDataBeforeSche
duleEvent
The event is published before
scheduling a cron job.
3 ReminderJobDataBeforeExec
uteEvent
The event is published before
executing a cron job.
65

-- 65 of 107 --

WARNING
The DocumentV2 is immutable. Therefore, if you want to modify the
document included in the event, you must use the setter method provided in
the event to make changes.
Examples
How to set fields of the reminder document before a cron job is created from the reminder
document:
@EventListener
public void listenReminderBeforeScheduleJobEvent(ReminderBeforeScheduleJobEvent
reminderBeforeScheduleJobEvent) {
DocumentV2 document =
reminderBeforeScheduleJobEvent.getReminderDocumentBeforeScheduleJob();
// Modify some field of reminder document before it is mapped to notification
// Call setter methods to modify the included document in the event
reminderBeforeScheduleJobEvent.setReminderDocumentBeforeScheduleJob(document);
}
Reminder Job Scheduler
We use the Quartz as the scheduler to schedule the reminder jobs which will then push the
corresponding notification to the client when the time comes. The default configurations
service/src/main/resources/autoconfig/core.properties for quartz are defined as below:
spring.quartz.job-store-type=jdbc
spring.quartz.properties.org.quartz.scheduler.instanceId=AUTO
spring.quartz.jdbc.initialize-schema=never
spring.quartz.overwrite-existing-jobs=true
We save the jobs to the database to avoid losing them due to server failure. Because Quartz doesn’t
recognize the DB type correctly for PostgresDB, you must define the DB type as described in the
configurations section.
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass
For testing purpose, you can save jobs to memory by configuring spring.quartz.job-store-
type=memory.
Reminder Mark DUE Cron Job
To keep the expired reminders having the correct status, we already have a job to move the status
to DUE immediately after the notification was published but the job is not robust.
If a problem happens when the Notification Center marks an entry as DUE, then this entry will be
the status TO_BE_DONE forever or until a user marks it as DONE. The scheduler will not recheck the
reminder if an error happens.
66

-- 66 of 107 --

Therefore, to make sure all expired reminders have the correct status. We introduced a new cron
job which could be executed in a period.
The job is not enabled by default, you can enable it by the following property:
mgmtp.a12.notificationcenter.reminder.job.mark-due.enabled
The trigger time can be configured by the property. The value must be a Quartz scheduler
expression.
mgmtp.a12.notificationcenter.reminder.job.mark-due.scheduler-expression
The limited number of reminders performed at 1 execution time could be specified by the below
property. The default value is 100 reminders.
mgmtp.a12.notificationcenter.reminder.job.mark-due.limit
Reminder Job HTTP API
List of Contents
• Schedule Reminder Job
• Reschedule Reminder Job
• Delete Reminder Job
Schedule Reminder Job
Name Schedule a reminder job.
Description Endpoint allows scheduling reminder job.
Method POST
URL /api/reminder-jobs/schedule
Headers Content-type
application/json
Parameters documentJson
Reminder document with JSON format, which is used to schedule a reminder job.
Authorizati
on Scopes
Schedule Reminder Job
Success
response
200 OK
A reminder job has been scheduled.
Reschedule Reminder Job
67

-- 67 of 107 --

Name Reschedule a reminder job.
Description Endpoint allows rescheduling a reminder job.
Method POST
URL /api/reminder-jobs/reschedule
Headers Content-type
application/json
Parameters documentJson
Reminder document with JSON format, which is used to reschedule a reminder job.
Authorizati
on Scopes
Reschedule Reminder Job
Success
response
200 OK
A reminder job has been rescheduled.
Delete Reminder Job
Name Delete a reminder job.
Description Endpoint allows deleting a reminder job by id of a document, which was used to
schedule a reminder job.
Method DELETE
URL /api/reminder-jobs/{documentId}
Headers Content-type
application/json
Parameters documentId
Document id, which was used to schedule a reminder job.
Authorizati
on Scopes
Delete Reminder Job
Success
response
200 OK
A reminder job with the provided reminder document id has been deleted.
8.1.9. Notification REST Client
The (java) client library which provides the APIs for interacting with Notification Center Services
from other backend services.
Artifact
Artifact Artifact ID Description
notificationcenter-rest-client com.mgmtp.a12.notificationcent
er:notificationcenter-rest-
client
The (java) client library which
provides the APIs for
interacting with Notification
Center Services from other
backend services.
68

-- 68 of 107 --

Configurations
Autoconfiguration
@Import(NotificationCenterClientConfiguration.class) // Use this annotation to enable
autoconfiguration
class ProjectTemplateServerApplication {
public static void main(String[] args) {
SpringApplication.run(ProjectTemplateServerApplication.class, args);
}
}
Spring properties
Property Default value Required Description
mgmtp.a12.notification
center.client.service-
url
- true Base Notification
Center Service URL
WARNING Since the Notification Center Services uses UAA for authentication, all
properties of the UAA Rest Client must be configured.
8.1.10. Web Notifications HTTP API
Web Notifications in Notification Center Services is reachable via the following endpoints:
Notification API
List of Contents
• Create Notification
Create Notification
Name Create Notification.
Description Endpoint allows creating a notification.
Method POST
URL /api/notifications
Headers Content-type
application/json
Parameters A JSON structure represents for the notification
com.mgmtp.a12.notificationcenter.shared.notification.data.Notification.
Authorizati
on Scopes
Push Notification
69

-- 69 of 107 --

Success
response
200 OK
A notification has been created.
Sync Notification API
List of Contents
• Sync Notification
Sync Notification
Name Sync Notification.
Description Long polling A12 Data Distribution entries endpoints. This endpoint can be used in
both case:
- Update changed entries from frontend to database.
- Retrieve new entries created in backend.
Method POST
URL /api/v2/sync
Headers Content-type
application/json
Parameters An object that presents the request to sync entries
com.mgmtp.a12.datadistribution.dto.sync.SyncRequestTO
Success
response
200 OK
A object that holds all data that gets sent to client of sync request
com.mgmtp.a12.datadistribution.dto.sync.SyncResponseTO
8.1.11. Spring Actuator
Spring Actuator contains many endpoints which expose information about the running application
like health metrics, configuration information, etc…
By default, all actuator endpoints (/actuator/*) are secured. If needed, you have the possibility to
explicitly open certain endpoints for public usage.
Configuration of Actuator Endpoint
The configuration actuator provides information about the currently applied configuration on the
running Notification Center Services server. The actuator is accessible via GET request to the
/actuator/configuration resource.
This actuator gives information on configuration changes and on warnings concerning the
configuration of the Notification Center Services server.
To enable this endpoint, provide the properties below:
management.endpoints.web.exposure.include="configuration"
70

-- 70 of 107 --

management.endpoint.configuration.access=read_only
Example response from 'GET /actuator/configuration'
{
"changes": {
"mgmtp.a12.notificationcenter.notification.expiration.garbageCollectionSchedulerExpres
sion": {
"default": "null",
"current": "0 0 * * * ?"
},
"mgmtp.a12.notificationcenter.notification.additionalTypes": {
"default": "null",
"current": "VALIDATION_RESULT,REASSIGNMENT,FORWARD_FOR_SIGNATURE"
},
"mgmtp.a12.notificationcenter.notification.expiration.cleanUpNotificationSchedulerExpr
ession": {
"default": "null",
"current": "0 0 * * * ?"
},
"mgmtp.a12.notificationcenter.reminder.job.mark-due.enabled": {
"default": "false",
"current": "true"
},
"mgmtp.a12.notificationcenter.reminder.job.mark-due.schedulerExpression": {
"default": "null",
"current": "0 0 0 * * ?"
},
"mgmtp.a12.notificationcenter.reminder.job.mark-due.limit": {
"default": "100",
"current": "50"
},
"mgmtp.a12.notificationcenter.notification.expiration.enableCleanUpJob": {
"default": "false",
"current": "true"
},
"mgmtp.a12.notificationcenter.reminder.modelName": {
"default": "null",
"current": "ShowCaseReminder-document"
},
"mgmtp.a12.notificationcenter.dataservices.baseUrl": {
"default": "null",
"current": "http://localhost:9090/api"
},
"mgmtp.a12.notificationcenter.notification.expiration.expirationDate": {
"default": "null",
"current": "1"
}
71

-- 71 of 107 --

}
}
8.2. Push Notifications
8.2.1. Supported Databases
Supported type of database: All relational database types are supported.
Embedded Postgres
The Push Notification service supports an embedded Postgres instance for local development and
testing without requiring an external database server. This is powered by
io.zonky.test:embedded-postgres.
Activate the notificationcenter-pushnotification-embedded_postgres Spring profile, or set the
property directly:
spring.datasources.pushnotification.embedded-postgres.enabled=true
All embedded Postgres configuration properties:
Property Default value Description
spring.datasources.pushnotific
ation.embedded-
postgres.enabled
false Enables the embedded Postgres
instance.
spring.datasources.pushnotific
ation.embedded-postgres.port
5435 TCP port for the embedded
instance.
spring.datasources.pushnotific
ation.embedded-postgres.path
null (system temp dir) Path to a directory for
persisting data across restarts.
When null, data is stored in a
temporary directory.
spring.datasources.pushnotific
ation.embedded-postgres.clean-
data-directory
false Deletes the data directory on
startup. Set to true in tests for
isolation.
WARNING: Never set this to
true in production — all data
will be deleted on startup.
spring.datasources.pushnotific
ation.embedded-
postgres.locale-c-type
en_US.UTF-8 Locale option set during initdb.
spring.datasources.pushnotific
ation.embedded-
postgres.connect-config.*
- JDBC/driver connection
parameters.
72

-- 72 of 107 --

Property Default value Description
spring.datasources.pushnotific
ation.embedded-
postgres.postgres-config.*
- Options passed to the pg_ctl
command.
spring.datasources.pushnotific
ation.embedded-
postgres.override-working-
directory
null Override directory for extracted
Postgres binaries. Useful for
persisting binaries across runs.
8.2.2. Spring Configuration
Database Configuration
Property Default value Required Description
spring.datasources.push
notification.url
- true Database connection
string
spring.datasources.push
notification.username
- true Database username
spring.datasources.push
notification.password
- true Database password
spring.datasources.push
notification.driver-class-
name
- true The database driver
classname. For
example:
org.postgresql.Driver
spring.datasources.push
notification.liquibase.ch
ange-log
classpath:database/db.c
hangelog-push-
notification.xml
false Liquibase change log
file path
• If you want to change database related configurations (Liquibase), you can set the following
properties
spring.datasources.pushnotification.liquibase.database-change-log-lock-
table=PN_DATABASECHANGELOGLOCK
spring.datasources.pushnotification.liquibase.database-change-log-
table=PN_DATABASECHANGELOG
Connection Pool Configuration
The Push Notification service uses HikariCP as the database connection pool implementation. You
can tune the connection pool settings based on your application’s requirements:
# Maximum number of connections in the pool (default: 10)
spring.datasources.pushnotification.hikari.maximum-pool-size=5
# Minimum number of idle connections maintained by HikariCP (default: same as maximum-
73

-- 73 of 107 --

pool-size)
spring.datasources.pushnotification.hikari.minimum-idle=2
# Maximum time (in milliseconds) to wait for a connection from the pool (default:
30000)
spring.datasources.pushnotification.hikari.connection-timeout=30000
# Maximum time (in milliseconds) that a connection can sit idle in the pool (default:
600000)
spring.datasources.pushnotification.hikari.idle-timeout=600000
# Maximum lifetime (in milliseconds) of a connection in the pool (default: 1800000)
spring.datasources.pushnotification.hikari.max-lifetime=1800000
TIP
For optimal performance, consider the following guidelines:
• maximum-pool-size: Set based on your database’s connection limit and
application load. The default value of 10 may be higher than necessary for low-
traffic applications. Consider reducing it to 5 or lower if your application handles
fewer concurrent requests.
• minimum-idle: Setting this lower than maximum-pool-size allows the pool to shrink
during periods of low activity, freeing up resources. A common practice is to set it
to about 20-50% of the maximum pool size.
• For more information on HikariCP configuration, refer to the HikariCP
Configuration Documentation.
Push Provider Configurations
In order to let the app interacts with the push providers like APNs for FCM, you must configure
those properties:
Property Required Default Description
mgmtp.a12.notificationc
enter.pushnotification.a
pns.bundle-id
true - iOS app bundle id.
mgmtp.a12.notificationc
enter.pushnotification.a
pns.pkcs8-file-path
true - APNs p8 token file path.
mgmtp.a12.notificationc
enter.pushnotification.a
pns.team-id
true - The Apple team Id.
_mgmtp.a12.notificatio
ncenter.pushnotificatio
n.apns.key-id
true - The Apple key Id.
74

-- 74 of 107 --

Property Required Default Description
mgmtp.a12.notificationc
enter.pushnotification.a
pns.production
false false If the value is true,
ApnsClient will connect
to the APNs production
server.
mgmtp.a12.notificationc
enter.pushnotification.a
pns.alternative-server-
port
false false Should use the
alternative server port
(2129) or not. By
default, the ApnsClient
will connect the APNs
server under port 443
mgmtp.a12.notificationc
enter.pushnotification.a
pns.connection-timeout
false 30s Connection timeout in
second.
mgmtp.a12.notificationc
enter.pushnotification.fc
m.service-account-
credential-path
true - The Google service
account credential file
absolute path.
mgmtp.a12.notificationc
enter.pushnotification.fc
m.connection-timeout
false 30s Connection timeout in
second.
mgmtp.a12.notificationc
enter.pushnotification.
mobile-platform
false cordova The target mobile
platform. This option is
required to handle
specific logic for the
target platform. The
available options:
cordova, capacitor,
native.
Transaction Manager
Push Notification’s repository configuration is declared particularly in PNRepsitoryConfiguration:
• TransactionManager bean:
@Bean
public PlatformTransactionManager pnTransactionManager(
@Qualifier("pnEntityManagerFactory") LocalContainerEntityManagerFactoryBean
pnEntityManagerFactory){
return new
JpaTransactionManager(Objects.requireNonNull(pnEntityManagerFactory.getObject()));
}
• DataSource bean:
75

-- 75 of 107 --

@ConfigurationProperties(PN_DATASOURCE_HIKARI_PROPERTY_BASE)
@Bean
public HikariDataSource pnDataSource(
@Qualifier("pnDatasourceProperties") DataSourceProperties pnDatasourceProperties)
{
HikariDataSource dataSource= pnDatasourceProperties.initializeDataSourceBuilder()
.type(HikariDataSource.class)
.build();
if(StringUtils.hasText(pnDatasourceProperties.getName())){
dataSource.setPoolName(pnDatasourceProperties.getName());
}
return dataSource;
}
WARNING
Above configurations are default, to specify explicitly Push Notification’s
transaction context for your logic, use
@Transactional("pnTransactionManager"). For example:
@Transactional("pnTransactionManager")
@PreAuthorize(AuthConstants.UAA_PUSH_NOTIFICATION_REGISTER_DEVICE_PE
RMISSION)
public ResponseEntity<Void> registerDevice(RegisterDeviceRequest
registerDeviceRequest) {
try {
// Logic of Push Notification component
logger.info("Registered new device successfully, request:
[{}]", registerDeviceRequest);
return ResponseEntity.status(HttpStatus.OK).build();
} catch (Exception e) {
logger.info("Fail to register new device, request: [{}],
exception message: [{}]", registerDeviceRequest, e.getMessage());
throw e;
}
}
Test Mode Configuration
Property Default value Required Description
mgmtp.a12.notificationc
enter.pushnotification.te
st-mode
false false Flag to enable/disable
the test mode the Push
Notification API
Secure Logger Configuration
The Push Notification service uses a secure logger to log sensitive information to comply with GDPR
regulations, including AccountId, DeviceId and other relevant information. By default, we use
76

-- 76 of 107 --

DEBUG level for logging sensitive information. You can customize the secure logger by defining a
bean of type SecureLoggerService as below:
public class CustomSecureLoggerServiceImpl implements SecureLoggerService {
/**
* Change the default log level to INFO.
*/
@Override
public void log(Logger logger, LogCategory category, String message, Object... var)
{
logger.info(message, var);
}
/**
* Hash sensitive information using SHA-256 algorithm.
*/
@Override
public String calculateHash(String content) {
return HashingUtil.calculateHashSha256(content);
}
}
Then, register the custom secure logger service as a bean in the Spring context:
@Configuration
public class ShowcaseLoggerConfiguration {
@Bean
public SecureLoggerService secureLoggerService() {
return new CustomSecureLoggerServiceImpl();
}
}
8.2.3. Spring Profiles
We offer the following configuration profiles for the Push Notification Service.
UAA Configuration
• Profile name:
◦ notificationcenter-pushnotification-uaa
• Profile contents:
mgmtp.a12.uaa.authorization.child-authorization-
definitions=classpath:uaa/ncPushNotificationAuthorizationDefinition.json
77

-- 77 of 107 --

Datasources Configuration
• Profile name:
◦ notificationcenter-pushnotification-datasources
• Profile contents:
spring.datasources.pushnotification.liquibase.database-change-log-lock-
table=PN_DATABASECHANGELOGLOCK
spring.datasources.pushnotification.liquibase.database-change-log-
table=PN_DATABASECHANGELOG
spring.datasources.pushnotification.liquibase.change-
log=classpath:database/db.changelog-push-notification.xml
Embedded Postgres Configuration
• Profile name:
◦ notificationcenter-pushnotification-embedded_postgres
• Profile contents:
spring.datasources.pushnotification.embedded-postgres.enabled=true
spring.datasources.pushnotification.driver-class-name=org.postgresql.Driver
spring.datasources.pushnotification.jpa.database-
platform=org.hibernate.dialect.PostgreSQLDialect
NOTE Activate this profile together with notificationcenter-pushnotification-datasources
for local development or tests without an external database.
8.2.4. Authorization
Push Notification Service introduces a new ncPushNotificationAuthorizationDefinition.json file for
securing our APIs. It uses mgmtp.a12.uaa.authorization.child-authorization-definitions for
integrating with UAA.
IMPORTANT
If you have a custom configuration for the key
mgmtp.a12.uaa.authorization.child-authorization-definitions then make
sure our file ncPushNotificationAuthorizationDefinition.json should be
added as well.
You are free to define your own (additional) permissions and policies, and organize them as you
want. You may define permission for following scopes:
Scope name Description
Push Notification Register Device Check that the user has access right to register a
device for Push Notification.
78

-- 78 of 107 --

Scope name Description
Push Notification Deregister Device Check that the user has access right to deregister
a device for Push Notification.
Push Notification Create Notification Check that the user has access right to create
Push Notification for their devices.
Push Notification Delete Account Check that the user has access right to delete an
account and its related data.
Push Notification Delete Test Accounts Check that the user has access right to delete test
accounts and their related data.
Authorization scopes used in the code:
Method name and arguments Scope name Description
DeviceApiDelegate.registerDevice
registerDeviceRequest
com.mgmtp.a12.notification
center.pushnotification.mod
el.RegisterDeviceRequest
PreAuthorize
PushNotificationRegisterDevic
e
Check that the user has
access right to register a
device for Push Notification.
DeviceApiDelegate.deregisterDevi
ce
id
java.lang.String
deviceId
java.lang.String
PreAuthorize
PushNotificationDeregisterDev
ice
Check that the user has
access right to deregister a
device for Push Notification.
PushNotificationService.createNo
tification
createPushNotificationRequ
est
com.mgmtp.a12.notification
center.pushnotification.mod
el.CreatePushNotificationRe
quest
PreAuthorize
PushNotificationCreateNotific
ation
Check that the user has
access right to create Push
Notification for their
devices.
AccountApiDelegate.deleteAccount
accountId
java.lang.String
PreAuthorize
PushNotificationDeleteAccount
Check that the user has
access right to delete an
account and its related data.
AccountApiDelegate.deleteTestAcc
ounts
timestamp
java.lang.String
PreAuthorize
PushNotificationDeleteTestAcc
ounts
Check that the user has
access right to delete test
accounts and their related
data.
79

-- 79 of 107 --

8.2.5. Events
List of events supported by Push Notification service:
No Event function name Description
1 PushProviderApnsBeforeSend
Event
Event published before a
notification is sent to APNs.
2 PushProviderFcmBeforeSend
Event
Event published before a
notification is sent to FCM.
You are able to update the APNs/FCM properties before sending notification to the cloud provider.
For example, you can change the color of notification by using the AndroidNotificationBuilder as
follows:
@Component
public class FcmListener {
@EventListener
void useGreenColorForAllPushNotifications(PushProviderFcmBeforeSendEvent event) {
event.getAndroidNotificationBuilder().setColor("#94c502");
}
}
8.2.6. Push Notification REST Client
Working With Spring Application
Artifact
Artifact Artifact ID Description
notificationcenter-push-
notification-rest-client
com.mgmtp.a12.notificationcent
er:notificationcenter-push-
notification-rest-client
The (java) library that provides
the APIs to interact with Push
Notification Service from the
other backend services.
Configurations
Example of configuration
@Configuration
public class UaaRestClientConfiguration {
private UAARestClientFactory uaaRestClientFactory;
private static final String PUSH_NOTIFICATION_BASE_URL =
"http://localhost:8089/api/push-notification";
80

-- 80 of 107 --

@PostConstruct
void initialize() {
UrlProperty uaaBaseUrlProperty = new UrlProperty("http://localhost:8089/api");
UAARestClientProperties restClientProperties = new UAARestClientProperties();
restClientProperties.setAuthorizationHeaderName("Authorization");
restClientProperties.setUaaBase(uaaBaseUrlProperty);
restClientProperties.setAuthenticationType(AuthenticationType.CERTIFICATE);
restClientProperties.getAuthenticationConfiguration().setCertificate(certificateProper
ties);
uaaRestClientFactory = UAARestClientFactoryBuilder
.withConfiguration(restClientProperties)
.withOkHttpClient(new OkHttpClient().newBuilder()
.connectTimeout(5000, TimeUnit.MILLISECONDS)
.readTimeout(5000, TimeUnit.MILLISECONDS)
.writeTimeout(5000, TimeUnit.MILLISECONDS)
.build())
.build();
}
@Bean
public AuthenticationRestClient authenticationRestClient() {
return uaaRestClientFactory.getAuthenticationRestClient();
}
@Bean
public AuthorizationRestClient authorizationRestClient() {
return uaaRestClientFactory.getAuthorizationRestClient();
}
@Bean
public DeviceApiConnector deviceApiConnector() {
return new DeviceApiConnector(PUSH_NOTIFICATION_BASE_URL,
uaaRestClientFactory.getPostConnector(),
uaaRestClientFactory.getDeleteConnector());
}
@Bean
public NotificationApiConnector notificationApiConnector() {
return new NotificationApiConnector(PUSH_NOTIFICATION_BASE_URL,
uaaRestClientFactory.getPostConnector());
}
@Bean
public AccountApiConnector accountApiConnector() {
return new AccountApiConnector(PUSH_NOTIFICATION_BASE_URL,
uaaRestClientFactory.getDeleteConnector());
}
@Bean
public HealthApiConnector healthApiConnector() {
81

-- 81 of 107 --

return new HealthApiConnector(PUSH_NOTIFICATION_BASE_URL,
uaaRestClientFactory.getGetConnector());
}
}
WARNING Since the Push Notification Service uses UAA for authentication, all
properties of the UAA Rest Client must be configured.
Working With Spring Boot Application
Artifact
Artifact Artifact ID Description
notificationcenter-push-
notification-rest-client-spring-
boot-autoconfigure
com.mgmtp.a12.notificationcent
er:notificationcenter-push-
notification-rest-client-
spring-boot-autoconfigure
The (java) library that provides
the APIs to interact with Push
Notification Service from the
other backend services which
use Spring Boot
Configurations
Autoconfiguration
@Import(PushNotificationClientConfiguration.class)
@SpringBootApplication
public class SampleBackendServiceApplication {
public static void main(String[] args) {
final SpringApplication application = new
SpringApplication(SampleBackendServiceApplication.class);
application.run(args).start();
}
}
Spring properties
Property Default value Required Description
mgmtp.a12.notification
center.pushnotificatio
n.client.base-url
- true Base Push Notification
Service URL
WARNING Since the Push Notification Service uses UAA for authentication, all
properties of the UAA Rest Client must be configured.
8.2.7. Push Notifications HTTP API
We provide an OpenAPI specification at nc-push-notification-api.json.
82

-- 82 of 107 --

Push Notifications in Notification Center Services is reachable via the following endpoints:
Account
List of Contents
• Delete Account
• Delete Test Accounts
• Schema Definition
◦ DeleteAccountRequest
Delete Account
Name Delete account
Description Delete an account and their associated device from system.
Method DELETE
URL /api/push-notification/accounts/{accountId}
Parameters accountId (string): The account ID
DeleteAccountRequest
Authorizati
on Scopes
PushNotificationDeleteAccount
Success
response
200 OK
Delete account successfully.
Error
response
404 NOT_FOUND
The account does not exist.
500 INTERNAL_SERVER_ERROR
Internal server error happens.
403 FORBIDDEN
The user doesn’t have permission to access the API.
Test Marker NOT_FOUND
INTERNAL_SERVER_ERROR
Delete Test Accounts
Name Delete test accounts
Description Delete test accounts and their associated devices from system
Method DELETE
URL /api/push-notification/test-accounts/{timestamp}
Parameters timestamp (long): The timestamp specifies the cutoff for deleting pre-existing
accounts in milliseconds, example: 1638485634567
Authorizati
on Scopes
PushNotificationDeleteTestAccounts
83

-- 83 of 107 --

Success
response
200 OK
Delete test accounts successfully.
Error
response
500 INTERNAL_SERVER_ERROR
Internal server error happens.
403 FORBIDDEN
The user doesn’t have permission to access the API.
Device
List of Contents
• Register Device
• Deregister Device
• Schema Definition
◦ RegisterDeviceRequest
◦ DeregisterDeviceRequest
Register Device
Name Register device.
Description Register device to receive Push Notifications from Notification Center services.
If the device already exists, it will be overwritten
Method POST
URL /api/push-notification/devices
Headers Content-type
application/json
Parameters RegisterDeviceRequest
Authorizati
on Scopes
PushNotificationRegisterDevice
Success
response
200 OK
Device is registered successfully.
304 NOT_MODIFIED
Device is not modified.
Error
response
403 FORBIDDEN
The user doesn’t have permission to access the API.
500 INTERNAL_SERVER_ERROR
Internal server error happens.
Test Marker INTERNAL_SERVER_ERROR
Deregister Device
Name Deregister device
84

-- 84 of 107 --

Description Deregister a device. This device will no longer receive Push Notifications from
Notification Center services.
Method DELETE
URL /api/push-notification/devices/{deviceId}
Parameters deviceId (string): The device ID
Authorizati
on Scopes
PushNotificationDeregisterDevice
Success
response
200 OK
Device is deregistered successfully.
Error
response
403 FORBIDDEN
The user doesn’t have permission to access the API.
404 NOT_FOUND
The device is not registered.
500 INTERNAL_SERVER_ERROR
Internal server error happens.
Test Marker NOT_FOUND
INTERNAL_SERVER_ERROR
Push
List of Contents
• Push a New Notification
• Schema Definition
◦ CreatePushNotificationRequest
◦ PushNotification
◦ PlatformSpecific
Push a New Notification
Name Push a New Notification
Description Create a Push Notification for devices of a specific account.
Method POST
URL /api/push-notification/notifications
Headers Content-type
application/json
Parameters CreatePushNotificationRequest
Authorizati
on Scopes
PushNotificationCreateNotification
Success
response
200 OK
Notification is sent successfully
85

-- 85 of 107 --

Error
response
403 FORBIDDEN
The user doesn’t have permission to access the API.
404 NOT_FOUND
The account is not registered, or the account doesn’t have any registered devices.
500 INTERNAL_SERVER_ERROR
Internal server error happens.
502 BAD_GATEWAY
All the push notification requests failed.
Test Marker NOT_FOUND
INTERNAL_SERVER_ERROR
PROVIDER_ERROR
Health
List of Contents
• Health Check
Health Check
Name Health check
Description Health check status for Push Notification including status of services and UAA
integration.
Method GET
URL /api/push-notification/health
Parameters None
Authorizati
on Scopes
-
Success
response
200 OK
The service is healthy and the UAA integration is working properly.
Error
response
401 UNAUTHORIZED
Authentication failed or missing scope.
500 INTERNAL_SERVER_ERROR
Internal server error happens.
Test Marker Not available for this endpoint.
Schema Definition
DeleteAccountRequest
Name Type Description
testMark
er
string The test marker value. "NOT_FOUND" or
"INTERNAL_SERVER_ERROR"
86

-- 86 of 107 --

RegisterDeviceRequest
Name Type Description
accountI
d*
string The account ID of the user ID.
deviceId
*
string The device ID.
platform
*
Enum: "ANDROID" or "IOS". The device platform.
testMark
er
string The test marker value. "INTERNAL_SERVER_ERROR"
isTestDe
vice
boolean When set to true, the account will be marked as a test
account. The default value is false.
DeregisterDeviceRequest
Name Type Description
testMark
er
string The test marker value. "NOT_FOUND" or
"INTERNAL_SERVER_ERROR"
CreatePushNotificationRequest
Name Type Description
accountI
d*
string The account ID of the user ID.
notifica
tion*
PushNotification The notification data.
platform Enum: "ANDROID" or "IOS" The device platform. Please use "null" or without the key
if you want to push for both device platforms.
testMark
er
string The test marker value. "NOT_FOUND" or
"INTERNAL_SERVER_ERROR" or "BAD_GATEWAY".
PushNotification
Name Type Description
title string The notification title.
body string The notification body.
customDa
ta
object This data will be included in the notification payload to
the mobile application.
platform
Specific
PlatformSpecific The data which is used to customize the notification in
different platforms.
PlatformSpecific
87

-- 87 of 107 --

Name Type Description
ios PlatformSpecificIOS Additional notification configurations for iOS.
android PlatformSpecificAndroid Additional notification configurations for Android.
PlatformSpecificIOS
Name Type Description
notifica
tionSubt
itle
string The notification subtitle.
badgeNum
ber
integer The mobile application badge number.
delivery
Priority
Enum: "IMMEDIATE" or
"CONSERVE_POWER"
The priority of the notification. By default, APNs sets the
notification priority to 10 (IMMEDIATELY).
collapse
Id
string An identifier you use to merge multiple notifications into a
single notification for the user. Typically, each notification
request displays a new notification on the user’s device.
When sending the same notification more than once, use
the same value in this header to merge the requests. The
value of this key must not exceed 64 bytes.
PlatformSpecificAndroid
Name Type Description
notifica
tionImag
eUrl
string The image Url in the notification.
notifica
tionCoun
t
integer The number of notification counts will be added to the
mobile application’s badge number.
sticky boolean When set to true, the notification persists even when the
user clicks it.
delivery
Priority
Enum: "NORMAL" or "HIGH" The priority of the notification. By default, FCM uses
NORMAL priority.
collapse
Id
string The collapse key serves as an identifier for a group of
messages that can be collapsed, so that only the last
message gets sent when delivery can be resumed. A
maximum of 4 different collapse keys may be active at any
given time.
TestMarker
88

-- 88 of 107 --

Na
me
Type Description Values
Test
Mark
erEn
um
Enum The test marker value NOT_FOUND
INTERNAL_SERVER_ERROR
PROVIDER_ERROR
8.2.8. Spring Actuator
Spring Actuator contains many endpoints which expose information about the running application
like health metrics, configuration information, etc…
By default, all actuator endpoints (/actuator/*) are secured. If needed, you have the possibility to
explicitly open certain endpoints for public usage.
Configuration of Actuator Endpoint
The configuration actuator provides information about the currently applied configuration on the
running Push Notification Services server. The actuator is accessible via GET request to the
/actuator/pushNotificationConfiguration resource.
This actuator gives information on configuration changes and on warnings concerning the
configuration of the Push Notification Services server.
To enable this endpoint, provide the properties below:
management.endpoints.web.exposure.include="pushNotificationConfiguration"
management.endpoint.pushNotificationConfiguration.access=read_only
Example response from 'GET /actuator/pushNotificationConfiguration'
{
"changes": {
"mgmtp.a12.notificationcenter.pushnotification.apns.bundleId": {
"current": "com.mgmtp.a12.notificationcenter.mobile.sample",
"default": "null"
}
}
}
8.2.9. Test Mode
When the test mode is enabled, a test marker value can be added to the request to simulate the
different use/error cases that can occur during usage of the API.
The supported test marker value is different for each API. You can find these values under Push
Notification API sections.
89

-- 89 of 107 --

This is an example request for registering device API with test marker value is
INTERNAL_SERVER_ERROR:
POST /api/push-notification/devices
{
"accountId" "",
"deviceId": "",
"platform": "",
"testMarker": "INTERNAL_SERVER_ERROR"
}
NOTE
If an unsupported or invalid test marker value is used, the request will return a
NOT_IMPLEMENTED error.
If the test marker is omitted. The request will be delegated to the controller and
process normally.
9. Notification Reminder Extension
The (java) library, which needs to be installed inside A12 Data Services to handle Reminder
Document logics.
9.1. Artifact
Artifact Artifact ID Description
notificationcenter-reminder-
extension
com.mgmtp.a12.notificationcent
er:notificationcenter-
reminder-extension
The (java) library which is
installed inside A12 Data
Services to handle Reminder
Document logics.
9.2. Configurations
Since this is an extension for A12 Data Services, please also refer to A12 Data Services
configuration.
Autoconfiguration
@Import(NotificationCenterReminderExtensionConfiguration.class) // Use this annotation
to enable autoconfiguration
class DataServiceApplication extends ServerApplication {
public static void main(String[] args) {
SpringApplication.run(DataServiceApplication.class);
}
90

-- 90 of 107 --

}
Spring properties
Property Default value Required Description
mgmtp.a12.notification
center.reminder.model-
name
- true Your custom A12
Reminder Model name.
Spring Profiles
We offer the following configuration profile for the Reminder Extension package.
• UAA configuration:
◦ Profile name:
▪ notificationcenter-reminderextension-uaa
◦ Profile contents:
mgmtp.a12.uaa.authorization.child-authorization-
definitions=classpath:uaa/ncExtensionAuthorizationDefinition.json
9.3. Authorization
Notification Center Reminder Extension introduces a new
ncReminderExtensionAuthorizationDefinition.json file for securing our APIs. It uses
mgmtp.a12.uaa.authorization.child-authorization-definitions for integrating with UAA.
IMPORTANT
If you have a custom configuration for the key
mgmtp.a12.uaa.authorization.child-authorization-definitions then make
sure our file ncReminderExtensionAuthorizationDefinition.json should be
added as well.
You are free to define your own (additional) permissions and policies, and organize them as you
want. You may define permission for following scopes:
Scope name Description
Create Reminder Check that the user has access right to create a
Reminder.
Update Reminder Check that the user has access right to update a
Reminder.
Get Reminder Check that the user has access right to get a
Reminder.
Query Reminder Check that the user has access right to query
Reminders.
91

-- 91 of 107 --

Scope name Description
Delete Reminder Check that the user has access right to delete a
Reminder.
Mark Done Reminder Check that the user has access right to mark a
Reminder as done.
Mark Due Reminder Check that the user has access right to mark a
Reminder as due.
Authorization scopes used in the code:
Method name and arguments Scope name Description
ReminderDocumentService.createRemi
nder
reminderRequest
com.mgmtp.a12.notificationce
nter.extension.document.Remi
nderRequest
PreAuthorize
CreateReminder
Check that the user has
access right to create a
Reminder.
ReminderDocumentService.updateRemi
nder
id
java.lang.String
reminderRequest
com.mgmtp.a12.notificationce
nter.extension.document.Remi
nderRequest
PreAuthorize
UpdateReminder
Check that the user has
access right to update a
Reminder.
ReminderDocumentService.getReminde
r
id
java.lang.String
PreAuthorize
GetReminder
Check that the user has
access right to get a
Reminder.
92

-- 92 of 107 --

Method name and arguments Scope name Description
ReminderDocumentService.queryRemin
ders
filterSpec
com.mgmtp.a12.dataservices.r
pc.query.FilterSpec
sortSpecs
java.util.List<com.mgmtp.a12.
dataservices.rpc.query.SortSp
ec>
pageSpec
com.mgmtp.a12.dataservices.r
pc.query.PageSpec
PreAuthorize
QueryReminders
Check that the user has
access right to query
Reminders.
ReminderDocumentService.deleteRemi
nder
id
java.lang.String
PreAuthorize
DeleteReminder
Check that the user has
access right to delete a
Reminder.
ReminderDocumentService.markDone
id
java.lang.String
PreAuthorize
MarkDone
Check that the user has
access right to mark a
Reminder as done.
ReminderDocumentService.markDue
id
java.lang.String
PreAuthorize
MarkDue
Check that the user has
access right to mark a
Reminder as due.
9.4. Working With Reminders
9.4.1. Events
List of events supported by Notification Center Reminder Extension:
No Event function name Policy Description
1 ReminderBeforeCreateEvent The event is published before
the computation and validation
of a newly created reminder
document.
2 ReminderBeforeUpdateEvent The event is published before
validation and computation of
an updated reminder
document.
WARNING The DocumentV2 is immutable. Therefore, if you want to modify the
93

-- 93 of 107 --

document included in the event, you must use the setter method provided in
the event to make changes.
9.4.2. Examples
How to set fields of the reminder document before the reminder document is created:
@EventListener
public void listenReminderBeforeCreatedEvent(ReminderBeforeCreateEvent
reminderBeforeCreateEvent) {
DocumentV2 document = reminderBeforeCreateEvent.getCreatedReminderDocument();
// Modify some field of reminder document
reminderBeforeCreateEvent.setCreatedReminderDocument(document);
}
9.4.3. Reminder Validation
In the A12 Data Services, you can implement your own way of validation before a reminder is
updated:
/**
* Interface for validation when updating reminder document
*/
public interface BeforeUpdateReminderValidator {
/**
* Validate reminder document
*
* @param updatedDocument the updated reminder document to be validated
* @param persistedDocument the persisted reminder document to be validated
* @throws ReminderValidationException exception including error message
*/
void validate(DocumentV2 updatedDocument, DocumentV2 persistedDocument) throws
ReminderValidationException;
}
Table 1. Enabled validators
Validator Classpath
ReminderCreatorInformationValidator extension/src/main/java/com/mgmtp/a12/notifica
tioncenter/extension/validator/internal/Remind
erCreatorInformationValidator.java
ReminderExpirationValidator extension/src/main/java/com/mgmtp/a12/notifica
tioncenter/extension/validator/internal/Remind
erExpirationValidator.java
ReminderStatusValidator extension/src/main/java/com/mgmtp/a12/notifica
tioncenter/extension/validator/internal/Remind
erStatusValidator.java
94

-- 94 of 107 --

NOTE The ReminderValidationException contains the localisation key, it will be used by
the client.
9.4.4. Search Capabilities
In Notification Center A12Reminder_DM, we enable the case-insensitive and approximate match
search in filters for the following fields:
• title
• description
• updatedBy
9.4.5. Reminder Migration
• Add the migration package:
dependencies {
implementation "com.mgmtp.a12.notificationcenter:notificationcenter-data-
migration:<VERSION>"
// other dependencies
}
• Import a migration script that matches with your model version and call migrate with your
document content that contains our A12Reminder_DM in XML format:
import
com.mgmtp.a12.notificationcenter.migration.versions.A12ReminderModelMigration_0_5_0;
@MigrationStep(version = "0.5.0", name = "Migrate A12Reminder documents")
@RequiredArgsConstructor
public class ShowCaseReminderDocumentMigration_0_5_0 {
private final MigrationScript a12ReminderMigrationScript = new
A12ReminderModelMigration_0_5_0();
@Transactional
@MigrationTask(name = "Migrate A12Reminder document fields")
public void migrateA12ReminderDocuments() {
String migratedDocument = loadDocumentXml();
// Migrate A12Reminder document fields
a12ReminderMigrationScript.migrate(migratedDocument);
}
}
95

-- 95 of 107 --

9.5. Reminder Extension HTTP API
List of Contents
• Get Reminder
• Create Reminder
• Update Reminder
• Delete Reminder
• Query Reminders
• Mark Done
• Mark Due
9.5.1. Get Reminder
Name Get a reminder by id.
Description Endpoint allows fetching a reminder document by id.
Method GET
URL /api/reminders/{id}
Headers Content-type
application/json
Parameters id
Reminder document id.
Authorizati
on Scopes
- Get Reminder
Success
response
200 OK
Loaded reminder document.
9.5.2. Create Reminder
Name Create a reminder.
Description Endpoint allows creating a reminder document.
Method POST
URL /api/reminders
Headers Content-type
application/json
Parameters reminderRequest
Reminder request contains content with validations and computations of the
document.
com.mgmtp.a12.notificationcenter.extension.document.ReminderRequest
96

-- 96 of 107 --

Authorizati
on Scopes
- Create Reminder
Success
response
200 OK
DocumentReference of the created reminder document.
Note The ReminderBeforeCreateEvent will be published before the document is created.
9.5.3. Update Reminder
Name Update a reminder.
Description Endpoint allows updating a reminder document.
Method PUT
URL /api/reminders/{id}
Headers Content-type
application/json
Parameters reminderRequest
Reminder request contains content with validations and computations of the
document
com.mgmtp.a12.notificationcenter.service.web.requestbody.ReminderRequest
id
Id of requested reminder document.
Authorizati
on Scopes
- Update Reminder
Success
response
200 OK
Reminder document has been updated.
Note The ReminderBeforeUpdateEvent will be published before the document is updated.
9.5.4. Delete Reminder
Name Delete a reminder by id.
Description Endpoint allows deleting a reminder document by id.
Method DELETE
URL /api/reminders/{id}
Headers Content-type
application/json
Parameters id
Reminder document id.
Authorizati
on Scopes
- Delete Reminder
97

-- 97 of 107 --

Success
response
200 OK
The reminder document has been deleted.
Note If the document does not exist, the action will silently finish without errors.
9.5.5. Query Reminders
Name Query reminders.
Description Endpoint allows fetching a list of reminder documents with partition configuration
(filtering, sorting, paging).
Method POST
URL /api/reminders/query
Headers Content-type
application/json
Parameters filter com.mgmtp.a12.dataservices.rpc.query.FilterSpec
A filter definition.
sort
A list of sort definitions.
page com.mgmtp.a12.dataservices.rpc.query.PageSpec
Pagination specification.
Authorizati
on Scopes
- Query Reminders
Success
response
200 OK
A list of reminder documents.
9.5.6. Mark Done
Name Mark a reminder as done.
Description Endpoint allows changing status of a reminder document to DONE.
Method PUT
URL /api/reminders/{id}/done
Headers Content-type
application/json
Parameters id
Reminder document id.
Authorizati
on Scopes
- Mark Done
Success
response
200 OK
Status of the reminder document has been updated to DONE.
98

-- 98 of 107 --

9.5.7. Mark Due
Name Mark a reminder as due.
Description Endpoint allows changing status of a reminder document to DUE.
Method PUT
URL /api/reminders/{id}/due
Headers Content-type
application/json
Parameters id
Reminder document id.
Authorizati
on Scopes
- Mark Due
Success
response
200 OK
Status of the reminder document has been updated to DUE.
10. Automatic Migration
Since 2024.06, A12 components should provide consuming projects with artifacts for automatic
migration where possible, to make migrations faster and less error-prone.
10.1. Migrating Client with Codemod
This codemod CLI helps automate repetitive code transformations required during Notification
Center version upgrades. It uses AST-based transformations to safely and accurately modify your
TypeScript codebase.
10.1.1. How To Use
Run command:
npx @com.mgmtp.a12.notificationcenter/notificationcenter-codemod@latest <recipe-id>
<source-directory-containing-tsconfig-file>
Or via pnpm:
pnpm dlx @com.mgmtp.a12.notificationcenter/notificationcenter-codemod@latest <recipe-
id> <source-directory-containing-tsconfig-file>
Example
Run the prefer-top-level-imports recipe on your project:
99

-- 99 of 107 --

npx @com.mgmtp.a12.notificationcenter/notificationcenter-codemod prefer-top-level-
imports ./client
10.1.2. Available Recipes
prefer-top-level-imports
Supported versions: ^3.2.0
Migrates deep path imports from @com.mgmtp.a12.notificationcenter/notificationcenter-* to top-
level imports.
Before
import {
NotificationFrame
} from "@com.mgmtp.a12.notificationcenter/notificationcenter-
core/lib/internal/components/NotificationFrame/NotificationFrame.js";
import {
NotificationCenterGlobalStyles
} from "@com.mgmtp.a12.notificationcenter/notificationcenter-
core/lib/internal/theme/global-styles.js";
After
import {
NotificationCenterGlobalStyles,
NotificationFrame
} from "@com.mgmtp.a12.notificationcenter/notificationcenter-core";
11. Infrastructure Dependencies
In the table below, the infrastructure dependencies required by Notification Center are listed with
their purpose, supported versions, resource recommendations, and configuration links.
100

-- 100 of 107 --

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
PostgreS
QL
Stores persistent
data (notifications,
reminder
documents, etc.)
15,16,17 Please use the links
for the Web
Notification
connection
configurations and
the Push Notification
connection
configurations
- An
embedde
d
Postgres
option is
available
for local
developm
ent and
tests —
see Web
Notificati
on and
Push
Notificati
on
embedde
d
Postgres
sections.
Due to the limitations of A12 Data Services and A12 Data Distribution, the PostgreSQL database is
required to support all features of Notification Center, especially the Reminder feature.
Currently, all Notification Center functional tests are executed against PostgreSQL version 17.
NOTE
For local development and testing without a running database server, both the
Notification Center service and the Push Notification service support an embedded
Postgres instance via the notificationcenter-embedded_postgres and
notificationcenter-pushnotification-embedded_postgres Spring profiles respectively.
See Web Notification Embedded Postgres and Push Notification Embedded Postgres
for details.
12. Migration Instructions
CAUTION Please have a look at Migration to latest A12 chapter for an explanation of
general steps on how to upgrade before starting with the component migration.
12.1. 2025.06-ext5
Version: 3.3.0
101

-- 101 of 107 --

12.1.1. Notification Publish Lifecycle Events
The Notification Center service now provides publish lifecycle events for web notifications:
• NotificationBeforePublishEvent
• NotificationAfterPublishEvent
These events are emitted around NotificationPublisher.pushNotification(…) and are intended as
integration hooks.
If your project currently overrides the Notification Publisher bean only to attach custom logic (for
example, email integration), migrate to Spring event listeners instead.
@Configuration
public class EmailIntegrationListener {
@EventListener
public void onAfterPublish(NotificationAfterPublishEvent event) {
// Integration-specific logic
}
}
12.1.2. Embedded Postgres Support for Notification Center and Push
Notification Services
Both the Notification Center service and the Push Notification service now support running an
embedded Postgres instance for local development and tests without requiring an external
database server. This is powered by io.zonky.test:embedded-postgres.
What’s New
• New Spring profile notificationcenter-embedded_postgres for the Notification Center service.
• New Spring profile notificationcenter-pushnotification-embedded_postgres for the Push
Notification service.
• New configuration for activating embedded Postgres:
# Notification Center service
spring.datasources.notificationcenter.embedded-postgres.enabled=true=true
# Push Notification service
spring.datasources.pushnotification.embedded-postgres.enabled=true=true
For tests that should run in isolation (clean database on each startup), add:
# Notification Center service
spring.datasources.notificationcenter.embedded-postgres.clean-data-directory=true
102

-- 102 of 107 --

# Push Notification service
spring.datasources.pushnotification.embedded-postgres.clean-data-directory=true
For full configuration reference, see the Web Notification Embedded Postgres and Push Notification
Embedded Postgres sections.
12.1.3. Discontinuation of the Notificationcenter-Rewrite Package
As of version 3.3.0, the notificationcenter-rewrite package has been discontinued due to licensing
issues. This package automated the migration of Notification Center to newer versions in backend
applications.
If you are on version 2.x, you can still use notificationcenter-rewrite 3.2.1 for automating the
migration to version 3.2.1. For any future migrations beyond that, you will need to apply the
necessary code changes manually.
12.2. 2025.06-ext4
Version: 3.2.1
12.2.1. Migrate Push Notification Mobile Showcase to Capacitor Platform
We have migrated the Push Notification Mobile Showcase from Cordova to Capacitor platform to
simplify the development and testing of Push Notification features on mobile devices. By doing so,
we switch to use Capacitor Push Notification plugin for handling push notifications and updated
the related code accordingly.
12.2.2. Support Composable A12 Client Application
We have migrated the showcase application to use the new composable A12 Client application API.
In Notification Center Core package, we have provided the withNotificationCenter function to
simplify the integration of Notification Center in A12 Client application setup.
12.2.3. Configurable Http Client for Push Notification REST Client
It is now possible to configure OkHttpClient for Push Notification REST client by configuring the
UaaRestClientFactory in the consuming application. You can set the desired configurations such as
connection timeout, read timeout, and other settings for the HTTP client used by the Push
Notification REST client.
For more details, please refer to the Push Notification REST Client documentation.
12.2.4. Configurable Hikari Connection Pool Configuration
It is now possible to configure Hikari connection pool settings for Notification Center service and
Push Notification service.
103

-- 103 of 107 --

For more details, please refer to the Web Notification Connection Pool for Web Notification or Push
Notification Connection Pool for Push Notification documentation.
12.2.5. Introduce Codemod for Frontend Migrations
We have introduced a Codemod CLI tool to help automate repetitive code transformations required
during Notification Center version upgrades.
• Recipes:
Prefer Top Level Import
Nested imports from the npm package @com.mgmtp.a12.notificationcenter/notificationcenter-core
(e.g. @com.mgmtp.a12.notificationcenter/notificationcenter-core/lib/internal/applicationFactory.js)
are deprecated in favor of top-level imports to avoid unnecessary breaking changes and reduce
ongoing maintenance effort.
Please refer to the Migrating Client with Codemod section for more details on how to use the
Codemod tool and the available recipes.
12.2.6. Push Notification Service Health Check Endpoint
We have added a new health check endpoint to the Push Notification service to provide better
visibility into the health status of the service. This endpoint can be used to verify that the service is
healthy and that UAA integration is working properly.
The endpoint is accessible at /api/push-notification/health.
12.2.7. Push Notification Service Secure Logger
We have implemented a secure logger for the Push Notification service to enhance security and
compliance when logging sensitive information.
For further details on how to customize the secure logger for different use cases, please refer to the
Push Notification Secure Logger documentation.
12.3. 2025.06-ext2
Version: 3.1.0
12.3.1. Push Notification on Capacitor Platform
We have introduced a new property to enable push notification support on the Capacitor mobile
platform:
mgmtp.a12.notificationcenter.pushnotification.mobile-platform=cordova|capacitor|native
The default value of this property is cordova.
104

-- 104 of 107 --

12.4. 2025.06
Version: 3.0.0
12.4.1. Replace the IDocument With the DocumentV2
We have replaced the IDocument interface with the new DocumentV2 interface from Kernel for access
and manipulation of data in the documents.
The overall information about the DocumentV2 can be found at Kernel document at DocumentV2.
12.4.2. Actuator Endpoint Migration
In Spring boot 3.0 and newer, the management.endpoint.<endpoint>.enabled property has been
deprecated and replaced with a new model using management.endpoint.<endpoint>.access.
The access property uses a more structured access control model with the following values:
• none – The endpoint is disabled for all access.
• read_only – The endpoint is accessible only for read operations (typically used for safe
operations like GET).
• unrestricted – The endpoint is fully accessible (subject to global actuator security settings).
12.4.3. Removal of the Deprecated Appendix ID
We have dropped support for notifications with appendix ID Notification. From now on, only
appendices with the ID metaData (the default implementation) are supported.
If you are manipulating the notification metadata (read/unread/delete) manually, you must migrate
your code to use NotificationDDAppendix.APPENDIX_ID instead of
NotificationDDAppendix.DEPRECATED_APPENDIX_ID.
12.4.4. Removal of Query API Switch
The Query API is now the default mechanism for querying Reminder documents, consistent with
other A12 components.
Backend
All related Spring properties and profiles have been removed.
• Affected Spring property: mgmtp.a12.notificationcenter.reminder.queryapi.enabled
• Affected Spring profiles: notificationcenter-reminderextension-queryapi, notificationcenter-
reminderjob-queryapi
NOTE
We have provided an OpenRewrite recipe to help remove the affected Spring
properties. However, due to the complexity of the Spring profiles structure, we
were unable to automate the removal of affected Spring profiles. As a result, you
105

-- 105 of 107 --

will need to remove them manually.
Frontend
In Notification Client package, we have removed the Query API switch from the
ReminderFactories.createModule method. You will need to update your code accordingly by
removing the { queryAPI: true } parameter, as shown below:
export const reminderModule = (): Module => ({
...ReminderFactories.createModule(),
model: () => model as ApplicationModel
});
12.4.5. Removal of Outdated Reminder Migration Code
According to the supported A12 release lines, we have removed the outdated migration code
including:
• Migration for A12 Reminder documents in version 0.5.0 (A12ReminderModelMigration_0_5_0)
• OpenRewrite recipe for version 2.0.0
(com.mgmtp.a12.notificationcenter.UpgradeNotificationCenter_2_0_0)
12.4.6. Deployment
Migrate the Ingress Path Type to ImplementationSpecific
In the latest Ingress controller running on the TPI Cluster, the validation enforces character
restrictions for paths with the Exact or Prefix types.
To include a rewrite configuration in the ingress path, the path type must be set to
ImplementationSpecific.
...
- path: /(api/v2/sync.*)
pathType: ImplementationSpecific
...
13. References
13.1. JavaDoc
• Notification Center javadoc
106

-- 106 of 107 --

13.2. TypeDoc
• notificationcenter-core
• notificationcenter-client
107

-- 107 of 107 --

