# project_template project template documentation

Project Template
Introduction
The Project Template is meant to be used by developers who want to start an A12 project. It shows
one possibility how a project can be structured, and it is always available on the latest A12 version.
Its main goal is to ease the start of A12 based projects.
Out of the box, the Project Template contains Data Services, Client and Keycloak as identity
provider. When you run it, you get a client-server A12 application with access management, one
Document Model, Form Model, Overview Model, and Application Model.
In addition, the configuration of Tree Engine and Relationships & Compose Document Model (CDM)
are integrated into the base Project Template.
Currently, it is maintained by a dedicated development team. Please send us an email if you start a
project using the Project Template. Feel free to contact us as well with questions, hints, suggestions
and feedback by email or via GetA12 Discourse.
Downloads
The following artifacts are available as downloads for the Project Template, depending on your
licensing model:
• Base Project Template (Community Edition)
Provides a minimal version of the project.
• Variants:
◦ Project Template with Workflows integration (Community Edition)
Provides a version, which integrates and uses the Workflows component.
◦ Project Template with Notification Center integration (Enterprise Edition)
Provides a version, which integrates and uses the Notification Center component.
◦ Project Template with local authentication type (Community Edition)
Provides a version, which uses the local authentication type of the UAA component instead
of Keycloak as identity provider.
WARNING
The LOCAL authentication type is only intended for development, demonstration
and training purposes. It is necessary to significantly enhance the security of
user management and use a different authentication type for production
environments.
Environment and Tools Setup
It is necessary to install and configure the following tools before working with the Project Template.
1

-- 1 of 68 --

Artifactories
Most of the tools need to retrieve dependencies and require an additional setup to access them.
IMPORTANT There are two separate Artifactories for internal and external developers,
and the setup differs.
IMPORTANT
Accessing Community Edition artifacts does not require authentication. You
only need to set up the registries or repositories described in the following
sections in your build tools.
External Partners
For externals, there is an Artifactory on geta12.com (mgm external only).
Internal Developers
For mgmies, there is an internal mgm Artifactory specified on mgm wiki "Tools Configuration" page
in "A12" space.
Gradle
Installation
Tool Versi
on
Downloa
d link
Installation
instructions
Note
JDK >=21
⇐25
Downloa
d
Installation For Linux and macOS users, we recommend using
SDKMAN to install and switch between JDK and Gradle
versions, see SDKMAN installation guide.
For Windows, SDKMAN is only available via WSL.
Gra
dle
>=9.0.
x
Downloa
d
Installation See above.
The easiest way to confirm that Gradle is installed correctly is to run gradle -v and check that the
Gradle version is printed.
Configuration
External Partners
Please follow the steps to access A12 Maven artifacts and create your identity token (if you are an
enterprise user) to set up the access to the artifactory.
Internal Developers
Please follow the steps on mgm wiki "Tools Configuration" page in "A12" space to set up access to
2

-- 2 of 68 --

the artifactory.
Verification
You can verify that Gradle was configured correctly by running gradle properties. This should
print an alphabetically sorted list of your Gradle configuration. If you are an enterprise user or
mgmie, it should include:
repository_password: <identity token>
repository_username: <username>
Troubleshooting
Common tips for solving issues with Gradle can be found on the Gradle troubleshooting site.
buildImages Task Failing on "@Input annotation" Usage
If the latest Gradle version is installed, the buildImages task will fail on:
* What went wrong:
A problem was found with the configuration of task ':server:init:bootBuildImage' (type
'BootBuildImage').
- In plugin 'org.springframework.boot' type
'org.springframework.boot.gradle.tasks.bundling.BootBuildImage' property 'archiveFile'
has @Input annotation used on property of type 'RegularFileProperty'.
...
Please use a supported Gradle version.
buildImages Task Failing on "Connection refused"
In case you get the following error, your Docker daemon is not running properly:
* What went wrong:
Execution failed for task ':server:init:buildImages'.
>
com.bmuschko.gradle.docker.shaded.org.apache.hc.client5.http.HttpHostConnectException:
Connect to http://127.0.0.1:2375 [/127.0.0.1] failed: Connection refused: no further
information
Please double-check your chosen Docker tool’s setup and troubleshoot accordingly.
Authentication Failed on Downloading Artifacts
Double-check, that your "repository_username" and "repository_password" in your Gradle
configuration are correct.
3

-- 3 of 68 --

CAUTION "repository_password" should contain an identity token, not a GetA12
password.
If credentials are correct, and your gradle.properties file contains more items, try to put lines with
"repository_username" and "repository_password" to the top of the document.
Task replacePlaceholders Failed on Configuration Cache Issues
Check the gradle.properties file in the Project Template, if the org.gradle.configuration-cache is set
to "false" before running the Gradle task.
If it is set to "true" it will fail the execution of the Gradle task replacePlaceholders, because its
implementation does not support the Gradle configuration cache.
The replacePlaceholders is meant to be executed only once at the beginning of the project
initialization, you can enable the property org.gradle.configuration-cache afterwards, when the
Gradle task ran successfully.
PostgresDB Still Contains Old Data After Deleting Local Database Files
EmbeddedPostgres relies on a shutdown hook for proper cleanup, resource release, and database
stopping. This might not always work, preventing the embedded Postgres database from shutting
down properly. Even if you delete the Postgres data folder, the Postgres process might still be
running in the background, which can lead to residual old data.
Consequently, restarting the application may lead to errors indicating the Postgres instance is
already running, or you may face port binding issues.
If this issue occurs, you must manually stop the Postgres process and delete the temporary
server/postgres folder. See Data Services - Additional Configuration Options for more details.
Node & npm
Installation
Too
l
Versi
on
Downloa
d link
Installation
instructions
Note
No
de
24.x.
x
Downloa
d
Follow installer We recommend using nvm to install and switch between
Node and npm versions, see nvm installation guide.
np
m
>=11.
x.x
Downloa
d
Installation See above.
To check that Node is installed just run node -v in the command line. This should return a version
number.
Similarly, you can see that npm is installed with npm -v, which should also print a version number.
Additionally, you can test your configuration with npm config list, which should print more details
about your configuration.
4

-- 4 of 68 --

By running the checkToolVersions Gradle task, the versions of Node and npm installed on the
system are compared with the recommended versions, and the result is visible in the output, e.g.:
Checking prerequisite tools versions for ... node, npm ...
...
[✓] Node (version: 24.15.0)
[✓] npm (version: 11.12.1)
...
Configuration
External Partners
Please follow the steps to access A12 npm artifacts and create your identity token (if you are an
enterprise user) to set up the access to the artifactory.
Internal Developers
Please follow the steps on mgm wiki "Tools Configuration" page in "A12" space to set up access to
the artifactory.
Verification
You can verify that Node and npm were configured correctly by running npm config ls. This should
print a list of your npm configuration, which should, among others, include:
External Community Edition Users
registry=https://registry.npmjs.org/
@com.mgmtp.a12.base:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.client:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.dataservices:registry=https://artifacts.geta12.com/artifactory/api/npm/
a12-community-npm/
@com.mgmtp.a12.devtools:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.diagrameditor:registry=https://artifacts.geta12.com/artifactory/api/npm
/a12-community-npm/
@com.mgmtp.a12.dml:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.formengine:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-community-npm/
@com.mgmtp.a12.kernel:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.overviewengine:registry=https://artifacts.geta12.com/artifactory/api/np
m/a12-community-npm/
@com.mgmtp.a12.projecttemplate:registry=https://artifacts.geta12.com/artifactory/api/n
pm/a12-community-npm/
5

-- 5 of 68 --

@com.mgmtp.a12.treeengine:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-community-npm/
@com.mgmtp.a12.tutorial:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.uaa:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.utils:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.widgets:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.workflows:registry=https://artifacts.geta12.com/artifactory/api/npm/a12
-community-npm/
@com.mgmtp.a12.expression:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-community-npm/
@com.mgmtp.a12.print:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.contentengine:registry=https://artifacts.geta12.com/artifactory/api/npm
/a12-community-npm/
@com.mgmtp.a12.enablements:registry=https://artifacts.geta12.com/artifactory/api/npm/a
12-community-npm/
@com.mgmtp.a12.datadistribution:registry=https://artifacts.geta12.com/artifactory/api/
npm/a12-community-npm/
@com.mgmtp.a12.notificationcenter:registry=https://artifacts.geta12.com/artifactory/ap
i/npm/a12-community-npm/
@com.mgmtp.a12.migrationtool:registry=https://artifacts.geta12.com/artifactory/api/npm
/a12-community-npm/
@com.mgmtp.a12.relationshipengine:registry=https://artifacts.geta12.com/artifactory/ap
i/npm/a12-community-npm/
@com.mgmtp.a12.crud:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.querymodel:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-community-npm/
External Enterprise Partners
//artifacts.geta12.com/artifactory/api/npm/a12-enterprise-npm/:_authToken = <IDENTITY
TOKEN>
//artifacts.geta12.com/artifactory/api/npm/a12-enterprise-npm/:always-auth = true
//artifacts.geta12.com/artifactory/api/npm/a12-enterprise-npm/:email = <EMAIL>
@com.mgmtp.a12.base:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.client:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.dataservices:registry=https://artifacts.geta12.com/artifactory/api/npm/
a12-enterprise-npm/
@com.mgmtp.a12.devtools:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.diagrameditor:registry=https://artifacts.geta12.com/artifactory/api/npm
/a12-enterprise-npm/
6

-- 6 of 68 --

@com.mgmtp.a12.dml:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.formengine:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-enterprise-npm/
@com.mgmtp.a12.kernel:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.overviewengine:registry=https://artifacts.geta12.com/artifactory/api/np
m/a12-enterprise-npm/
@com.mgmtp.a12.projecttemplate:registry=https://artifacts.geta12.com/artifactory/api/n
pm/a12-enterprise-npm/
@com.mgmtp.a12.treeengine:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-enterprise-npm/
@com.mgmtp.a12.tutorial:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.uaa:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.utils:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.widgets:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.workflows:registry=https://artifacts.geta12.com/artifactory/api/npm/a12
-enterprise-npm/
@com.mgmtp.a12.expression:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-enterprise-npm/
@com.mgmtp.a12.print:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.contentengine:registry=https://artifacts.geta12.com/artifactory/api/npm
/a12-enterprise-npm/
@com.mgmtp.a12.enablements:registry=https://artifacts.geta12.com/artifactory/api/npm/a
12-enterprise-npm/
@com.mgmtp.a12.datadistribution:registry=https://artifacts.geta12.com/artifactory/api/
npm/a12-enterprise-npm/
@com.mgmtp.a12.notificationcenter:registry=https://artifacts.geta12.com/artifactory/ap
i/npm/a12-enterprise-npm/
@com.mgmtp.a12.migrationtool:registry=https://artifacts.geta12.com/artifactory/api/npm
/a12-enterprise-npm/
@com.mgmtp.a12.relationshipengine:registry=https://artifacts.geta12.com/artifactory/ap
i/npm/a12-enterprise-npm/
@com.mgmtp.a12.crud:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.querymodel:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-enterprise-npm/
Internal Developers
//<internal mgm Artifactory>/api/npm/:_authToken = (protected)
always-auth = true
email = "<Your email>"
registry = "https://<internal mgm Artifactory>/api/npm/npm-repos"
7

-- 7 of 68 --

NOTE
The client:universalizePackageLock Gradle task is specifically designed for the
development of the Project Template. Its purpose is to provide a universal package-
lock.json, independent of the artifactory configuration, with fixed dependency
versions. This ensures consistency and predictability in the project’s dependencies
across different environments and setups.
Troubleshooting
Common tips for solving issues with Node and npm can be found on npm common errors site.
npm Invalid Auth
If you get the following message while trying to run npm commands:
npm ERR! code ERR_INVALID_AUTH
npm ERR! Invalid auth configuration found: `_auth` must be renamed to
`//registry.npmjs.org/:_auth` in user config
npm ERR! Please run `npm config fix` to repair your configuration.`
Either run npm config fix as suggested in the error message, or prefix your "_auth:" line in .npmrc
file with the artifactory URL, for example:
//<npm-artifactory-url>/:_auth="aGFoYW5hbWVuYWRodWh1cGFzcw=="
Unclear Errors After npm Version Change
If you change the npm version after you already built a client with another npm version, your build
can end up with errors that are related to dependencies' versions but do not actually describe a
specific problem.
In such a case, it is good practice to try to update the package-lock.json file by one of the following
options:
• Running the command below:
npm update
• Deleting the node_modules directory and the package-lock.json file in the client directory. It will
be created again with the next build.
Stopping Node Processes on Windows Manually
If the client process doesn’t stop properly, it is possible to stop the underlying Node.js runtime
process manually:
1. Opening a Task Manager (either via "Start" menu or with "Ctrl+Shift+Esc" shortcut).
8

-- 8 of 68 --

2. Ending all tasks named "Node.js JavaScript Runtime".
Docker
Installation
Tool Vers
ion
Download link Installation
instructions
Note
Doc
ker
>=20.
x
UI tool for any
OS Download
UI tool
Installation
Docker Engine
for Linux
Rancher Desktop is linked here as a UI tool, it is
possible to use different tools depending on licenses
and OS.
The Docker engine installation steps include the
necessary packages for the docker-compose-plugin.
Git - Download Follow
installer
After installing your desired Docker daemon provider (Docker Desktop, Rancher Desktop or other),
test the proper installation by running docker version. The result should return both "client" and
"server" versions and properties, for example:
Client:
Version: 24.0.4
API version: 1.42 (downgraded from 1.43)
Go version: go1.20.5
Git commit: 3713ee1
Built: Fri Jul 7 14:52:09 2023
OS/Arch: windows/amd64
Context: default
Server:
Engine:
Version: 23.0.6
API version: 1.42 (minimum version 1.12)
Go version: go1.20.4
Git commit: 9dbdbd4b6d7681bd18c897a6ba0376073c2a72ff
Built: Fri May 12 13:54:36 2023
OS/Arch: linux/amd64
Experimental: false
containerd:
Version: v1.7.2
GitCommit: 0cae528dd6cb557f7201036e9f43420650207b58
runc:
Version: 1.1.7
GitCommit: 860f061b76bb4fc671f0f9e900f7d80ff93d4eb7
docker-init:
Version: 0.19.0
GitCommit:
9

-- 9 of 68 --

We have a Docker Compose setup that requires version ≥ 2.20.3 of Docker Compose. To check your
Docker Compose version, run docker compose version. The output should display the version of
Docker Compose, for example:
$ docker compose version
Docker Compose version 2.23.1
We recommend using the latest versions of Docker and Docker Compose to avoid compatibility
issues.
Configuration
External Partners
Please follow the steps to access A12 Docker artifacts and create your identity token (if you are an
enterprise user) to set up the access to the artifactory.
Internal Developers
Please follow the steps on mgm wiki "Tools Configuration" page in "A12" space to set up access to
the artifactory.
Troubleshooting
Adding User To "docker-users" Group on Windows
If the Docker server is not starting properly, your user was probably not added to the "docker-
users" group during installation. Try to add your user via one of the following options:
• "Computer Management" menu:
1. From the "Start" menu, open "Computer Management" with right-click "Run as
administrator".
2. Expand "Local Users and Groups", and select "Groups".
3. Find the docker-users group, right-click and select "Add to Group…".
4. Add your user account or accounts.
5. Sign out and sign back in again for these changes to take effect.
• Command line:
1. Run cmd with right-click "Run as administrator".
2. Run following command with your domain and username net localgroup docker-users
DOMAIN\username /add.
WSL Upgrade to WSL2 on Windows
Older computers might contain Windows Subsystem for Linux in the first version and/or with an
outdated kernel.
10

-- 10 of 68 --

WSL2 is necessary for Docker to run on Windows. Switch to WSL2 and update the kernel following
this Microsoft manual.
Gradle composeUp Task Fails After Renaming Project With the replacePlaceholders Task
If you first build the project related Docker images and afterward rename the project with the
replacePlaceholders task, composeUp task will fail on a "pulling error" similar to these two:
> Task :compose:composeUp
frontend Pulling
server Error
frontend Error
Error response from daemon: unauthorized
> Task :compose:composeUp FAILED
> Task :compose:composeDownForcedOnFailure
Warning: No resource found to remove for project
"173a24ec4084261355a507f28da08f52_compose_".
> Task :compose:composeUp FAILED
frontend Pulling
server Pulling
server Error
frontend Error
Error response from daemon: Get "https://my-own-dockerregistry.com/v2/": dialing my-
own-dockerregistry.com:443 static system has no HTTPS proxy: resolving host my-own-
dockerregistry.com: lookup my-own-dockerregistry.com: no such host
> Task :compose:composeDownForcedOnFailure
Warning: No resource found to remove for project
"b6e168adff81f019151d07be4954e365_compose_".
To fix it, perform the following steps:
1. If the project-specific Docker containers are still running, shut them down with composeDown
Gradle task.
2. Remove existing Docker images related to the project by using docker rmi command or
cleanImages Gradle task.
3. Rebuild the images with the buildImages Gradle task.
4. Run composeUp Gradle task again.
New setup.json Database Related Values Not Applied On Docker Compose Setup After
Executing replacePlaceholders Task
When changing the initial database related values in the setup.json file, in case you already run
compose-up the Postgres container, they will not take effect because the Postgres database still uses
11

-- 11 of 68 --

a Docker volume with the old setup. This volume persists data and configuration, including initial
credentials, that will make the script compose/postgres/db-init.sh be ignored.
The compose-up might fail with a similar error like the following
2024-08-05 10:44:44,507 WARN [io.agroal.pool] (agroal-11) Datasource '<default>':
FATAL: password authentication failed for user "my-keycloak-db-username"
2024-08-05 10:44:44,510 WARN
[org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator] (JPA Startup Thread)
HHH000342: Could not obtain connection to query metadata:
java.lang.NullPointerException: Cannot throw exception because the return value of
"java.util.function.BiFunction.apply(Object, Object)" is null
To update the database related values, you need to remove the Docker volume and compose-up the
Postgres database again following these steps:
1. Run docker volume ls to list all Docker volumes.
2. Find the volume with the name of the project, for example your-project-name_postgres_data.
3. Remove the volume with docker volume rm <volume-name>.
4. Run gradle postgresComposeUp to start the Postgres database again.
Please ensure that you have a backup of the data before removing the volume.
Server Docker Container Crash on Mac M4 Machine
If you are using Docker Desktop on a Mac M4 machine, you might encounter an issue where the
server Docker container crashes with the following error:
2025-01-31 16:57:28 #
2025-01-31 16:57:28 # A fatal error has been detected by the Java Runtime Environment:
2025-01-31 16:57:28 #
2025-01-31 16:57:28 # SIGILL (0x4) at pc=0x0000ffff70d3fc5c, pid=1, tid=7
2025-01-31 16:57:28 #
2025-01-31 16:57:28 # JRE version: (21.0.6+7) (build )
2025-01-31 16:57:28 # Java VM: OpenJDK 64-Bit Server VM (21.0.6+7-LTS, mixed mode,
sharing, tiered, compressed oops, compressed class ptrs, g1 gc, linux-aarch64)
2025-01-31 16:57:28 # Problematic frame:
2025-01-31 16:57:28 # j java.lang.System.registerNatives()V+0 java.base@21.0.6
Please try updating Docker Desktop to the latest version or switching to a different base image for
the server Docker container.
Postgresql Container Stops Unexpectedly After a Certain Amount of Time
If the postgres container stops unexpectedly after running successfully for a short period of time
and the Keycloak log shows similar error:
12

-- 12 of 68 --

FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':compose:keycloakComposeUp'.
> TCP socket on 172.18.0.1:8083 of 'your-project-name_postgres' is still failing.
Logs:
PostgreSQL Database directory appears to contain a database; Skipping initialization
* Try:
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
* Exception is:
org.gradle.api.tasks.TaskExecutionException: Execution failed for task
':compose:keycloakComposeUp'.
at
org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.lambda$executeIfVal
id$1(ExecuteActionsTaskExecuter.java:130)
at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:293)
Please check your Docker installation vs. operating system network settings and behavior.
You can try to disable the TCP ports check, that is verified to help on the combination of Ubuntu
with Docker Desktop. Add the waitForTcpPorts = false parameter to the dockerCompose {} closure of
your project’s compose\build.gradle, for example:
dockerCompose {
waitForTcpPorts = false //add this line
removeVolumes = false
projectName = "${rootProject.name}"
// …. rest of the code
Docker or Docker Compose Related Errors on Linux
If you encounter Docker or Docker Compose related errors on Linux, please check the following
points:
• Make sure you followed all the steps required for the Docker engine installation.
• To manage Docker as a non-root user, please check the post-installation steps.
• If you did not proceed with full installation previously, please make sure that the Docker
Compose is installed in version 2.
Getting Started With the Project
Once the environment is set up, it is possible to download, inspect and run the project.
13

-- 13 of 68 --

Retrieving the Project
Download Project Template 202606.0.0 (tgz archive), unarchive to a desired directory and rename
the project.
Project Content Structure
The Project Template has two main modules:
• client module is split into the configuration files and folders src, resources and scripts:
◦ resources folder contains the main index.html and the other static resources like images.
◦ scripts folder contains helper JS scripts for the project.
◦ src consists of the custom frontend code. Containing the setup of the application, including
the layout and some custom configurations, like the support of multiple locales. It also
provides sagas, components and the logic to register custom modules.
• server module consists of two submodules, init and app:
◦ app module contains the backend code of the Project Template, including Spring profiles for
properties definitions.
◦ init module takes care of initializing models and documents for the server application when
it runs with multiple instances on a cluster environment. When the server application runs
with only one instance, it can perform the initialization of models and documents itself
without this init module. Migration tasks are also included in this submodule for data
migration purposes.
The Project Template also contains complement modules:
• compose module contains a Docker compose configuration for development purposes,
including a setup for Keycloak and Postgres containers.
• copyright module contains a copyright header template and helper Gradle tasks for verifying
and applying headers to new files. The usage is described in the section Licensing and License
Headers.
• import module contains resources that are imported to the server during initialization, for
example models, JSON-RPC requests or user roles definitions. The content of the import
directory can be modified as described in Enhancement Possibilities. These resources are then
imported to the application as follows:
◦ For local development and manual Spring Boot application start, they are imported from the
import directory. application-dev.properties file already contains necessary properties.
◦ For local development and execution in Docker compose, the import directory is mounted as
a volume to the server (or server-init) container.
◦ For the purpose of cluster deployment, the content of import is zipped and published as an
artifact with the same naming as published images. This artifact is then mounted to the
container as a volume.
CAUTION This approach serves as an example for the Project Template resources, and you
14

-- 14 of 68 --

need to consider changes in the deployment strategy of your project.
If your project requires different publishing cycles for different resources or
finer split of published artifacts, changes in packaging and publishing tasks are
necessary.
• buildSrc module contains custom build logic and helper utilities for the project.
• resources module contains common static resources for the project, for example images.
• quality module contains code quality check configurations.
Besides the modules, the project contains:
• logs directory for the server application. This is created with the first run of the server
application on the path specified in server/app/src/main/resources/logback-prod.xml in file tag.
By default, it is disabled in development to avoid performance impact.
• .run directory contains run configuration profiles for IntelliJ IDEA.
• .vscode directory contains run configuration profiles for Visual Studio Code.
• sbom directory contains Software Bill of Materials in OWASP CycloneDX standard format of the
project: cyclonedx-client.json and cyclonedx-server.json.
Frequently Used Commands
The following list contains a collection of the most important commands to interact with the Project
Template:
Command Description
gradle clean build Build the project.
gradle :server:app:bootRun --args='
--spring.profiles.active=dev-env'
Start the Data Services Server.
cd client
npm start
Start the frontend.
gradle buildImages Build Docker images.
gradle composeUp Start Docker containers.
gradle buildImagesComposeUp Build images and start Docker
containers with one command.
gradle composeDown Stop running Docker
containers.
gradle keycloakComposeUp Starting the Keycloak and
Postgres container. Keycloak is
connected to Postgres to persist
data.
gradle keycloakComposeDown Stopping the Keycloak and
Postgres container.
15

-- 15 of 68 --

Command Description
gradle replacePlaceholders Replacing all placeholder
template names based on
setup.json.
gradle checkToolVersions Compares provided tool
versions to the required ones
based on tool-versions.json.
Ready-to-Use Functionality
The following features are fully integrated into the base Project Template. This means you can start
leveraging these features without any additional configuration.
Tree Engine
You can refer to this Tree Engine for more details on Tree Engine.
NOTE You will need to add your own custom modules and models.
Relationship and Composed Document Model
You can refer to this Modeling Documentation for more details on CDM.
NOTE You will need to add your own custom modules and models.
Content engine
You can refer to this Content Engine for more details on Content Engine.
NOTE You will need to add your own custom modules and models.
Workspace Model Conversion
The Project Template uses the A12 Workspace Conversion Framework (WCF) to convert the
workspace (modeling-time) models into runtime models during the build.
Model Layers
The Project Template distinguishes two layers of models. The following table describes each layer
and its role.
Layer Description
Workspace models The source models that you author and maintain. They are
stored in import/models/ and kept under version control. This is
the modeling-time format.
16

-- 16 of 68 --

Layer Description
Runtime models The self-contained models that the kernel and Data Services
consume at runtime. They are produced by the Workspace
Conversion Framework.
Build-Time Conversion Pipeline
During the build, the workspace models are converted and the result is consumed by the typed
accessor generator and the resource processing of both server applications. The following diagram
illustrates this pipeline.
import/models/ (Workspace models — version-controlled)
|
v :convertModels
| (WCF CLI + RMC converters; output: <OUTPUT_DIR>/data/models/<modelId>.json)
build/wcf-output/data/models/ (Runtime models — build artefact, not under version
control)
|
|---> :server:app:runTypedAccessorGenerator (reads the converted runtime
models)
|
|---> loaded at runtime via file: path
(bootRun: file:../../build/wcf-output/data/models)
(Docker: mounted as volume at /var/lib/a12/data/models)
The convertModels task performs the conversion once. Both consumers, runTypedAccessorGenerator
and processResources, declare a dependency on it, so the conversion is guaranteed to run before
either of them.
NOTE The converted models are build artefacts. They exist only under build/ and are
loaded at runtime directly from the filesystem.
Running the Conversion Manually
To run the conversion on its own, execute the following command.
gradle convertModels
The converted runtime models are written to build/wcf-output/data/models/.
Runtime Configuration
Both server/app and server/init are configured to load the models from the filesystem.
For local development:
mgmtp.a12.dataservices.initialization.import.models.path=file:../../build/wcf-
17

-- 17 of 68 --

output/data/models
For Docker Compose, the host build/wcf-output/data/models/ directory is mounted as a volume into
the containers at /var/lib/a12/data/models, and the environment variable is set accordingly:
MGMTP_A12_DATASERVICES_INITIALIZATION_IMPORT_MODELS_PATH:
file:/var/lib/a12/data/models
volumes:
- ../build/wcf-output/data/models:/var/lib/a12/data/models
IMPORTANT
Before running Docker Compose, ensure the converted models exist on the
host by running gradle convertModels (or gradle buildImages, which triggers
the conversion automatically).
Typed Accessor Classes
The typed accessor classes are generated during the build process of the server application from
the converted runtime models (see Workspace Model Conversion). This ensures accessors are
based on fully expanded, self-contained Document Models. For more details, please refer to Typed
Accessors Documentation.
The runTypedAccessorGenerator task depends on :convertModels and reads from build/wcf-
output/data/models. To adapt it for your project’s package prefix, update the packagePrefix in
server/app/build.gradle:
tasks.register('runTypedAccessorGenerator', JavaExec) {
dependsOn ':convertModels'
...
def modelPath = rootProject.file('build/wcf-output/data/models')
def packagePrefix = 'com.mgmtp.a12.template.server.typings'
...
}
The following is how the generated folder looks like in the project structure:
18

-- 18 of 68 --

File Naming Convention
The Project Template follows a specific file naming convention to ensure consistency and clarity
19

-- 19 of 68 --

across the project. Below are the guidelines for naming files:
File Type Naming Convention Example
JavaScript node script (.js) Use kebab-case kill-port.js
TypeScript (.ts) Use camelCase appsetup.ts
React Components, React Context
(.tsx)
Use PascalCase ThemeChooser.tsx,
ThemeContext.tsx
Java files (.java) Use PascalCase UserService.java,
ServerApplication.java
Shell scripts Use lowercase with hyphens (kebab-
case)
build.sh, run-tests.sh
Preparation of the Project Template for a
New Project
This section covers the changes necessary for using the Project Template in a new project.
Initialization
After unpacking and understanding the project structure, we suggest initializing version control of
your choice for the repository. The following example describes initialization for Git.
In the project folder, execute these commands:
# Init repository
git init
# Create first commit
git add --all
git commit -m "Initial Commit"
Eventually, you can add your remote repository and push the first commit there:
# Add new origin remote
git remote add origin <your-repository-url>
# Push the initial commit
git push -u origin HEAD:main
Renaming
NOTE It is not mandatory to rename the project for initial build or local development. In
20

-- 20 of 68 --

case you plan to publish your project, you should replace these values with your
desired project-specific ones.
CAUTION
Make sure there is no persisted data before you use replacePlaceholders task to
change model’s names, e.g. change the values of appModelName property. We
support Postgres file-based and local Postgres for database configuration, and
both work on persistence mechanism. If there are some issues like displaying
double App Model, you should delete the files in the server/postgres folder.
The Project Template contains placeholders like "your-project-name" for a project name in multiple
places. These can be replaced automatically by a Gradle task following these steps:
1. Make sure you’re in the project root directory and that Gradle is set.
2. Make sure no Docker images for the project exist. If some exist already, remove it by using
docker rmi command or with cleanImages gradle task.
3. All parameters that would be replaced are centralized in setup.json. Put your desired name
values to the alternative fields in the file. It is not needed to change all the alternative fields, if
you know you will not use some of it.
CAUTION To avoid unexpected field changes, please don’t change current field values
if you have no purposeful reason for it.
WARNING
We can not support all characters for alternative values. Single quotes (')
are not supported and may lead to errors when e.g. included in database
password placeholders.
4. Run replacePlaceholders Gradle task
All placeholder values should now be replaced with the alternative values. For the package group
name changes, files in server/../src directory are moved to the newly created directories in the
path corresponding with the new group name.
Docker Related Adjustments for External Developers
IMPORTANT This section is only for external developers. The following changes are also
required for participating in the A12 Technical Introduction Training.
The Docker setup in the Project Template needs the following changes if it relies on an external
registry.
Change Docker Registry Properties
The gradle.properties file in the root directory of the project contains properties that define Docker
registries:
• For publish - registry where your project’s images will be published once it is ready to be
published. It has to be a secured non-public registry or service.
21

-- 21 of 68 --

• For read - registry where the necessary public images like "postgres" will be pulled from.
Both registries are pre-set to docker.io as a default. You may need to change
dockerRegistryForPublish to your own secured registry as shown here:
From
dockerRegistryForPublish=docker.io
dockerRegistryForRead=docker.io
To
dockerRegistryForPublish=your-project-name.<YOUR-DOCKER-REGISTRY>
dockerRegistryForRead=docker.io
NOTE
It is unnecessary to have a Docker registry for publishing and to change
dockerRegistryForPublish for the purpose of the A12 Technical Introduction
Training.
If you set up your own secured dockerRegistryForPublish, also set dockerUseCredentials to true, like
this:
From
dockerUseCredentials=false
To
dockerUseCredentials=true
Set the Keycloak’s Registry
Keycloak does not use "docker.io" as its public registry for images. Its registry is set up in
compose/.env and is pre-set to the public Keycloak registry quay.io. If you need to use your own
registry instead, change it as shown here:
From
KEYCLOAK_REGISTRY='quay.io'
To
KEYCLOAK_REGISTRY='<YOUR-DOCKER-REGISTRY>'
22

-- 22 of 68 --

Docker Host Changes
In case your environment requires changes on Docker host port or address, uncomment and edit
the dockerHost property in the gradle.properties file. This is useful if you are running Docker inside
WSL, for example. You can expose your Docker daemon and use the IP address of your WSL in this
property.
Certificate Authentication
NOTE Certificate authentication type is integrated into Workflows and Notification Center
variants.
Certificate authentication is provided by UAA, it is used for backend jobs that are executed without
user interaction. The certificate is validated by a Root Authority(CA) and contains information like
system’s name and user’s role.
We provide a Gradle task to generate a server and a client certificate:
gradle genCert
This task creates the following files:
• rootCA.crt: Added in DataServices module. This certificate is used by the UAA Server to verify
the client.crt.
• client.crt: Imported into extra modules, such as Workflows or Notification Center.
CAUTION
Users MUST run the gradle genCert task manually to generate certificates first
as a prerequisite, otherwise the application can’t start.
The information in certificates is set by default and serves for development
purposes only. For production usage, or if users want to implement their own
certificates, please refer to the UAA Certificate documentation.
Build
With the project prepared and environment set up, the source is ready to be built from the root
directory of the project.
The build is provided by the Gradle Build Tool.
Project Modules
All modules can be built together with one Gradle task:
gradle build
23

-- 23 of 68 --

Alternatively, it is possible to build modules separately, for example:
gradle :client:build
Docker Images
The Docker images can be built together as well:
gradle buildImages
Or every image can be built separately, e.g.:
gradle :server:app:buildImages
Additionally, we provide an option to build all images and run all containers with one command:
gradle buildImagesComposeUp
For the server, based on this Spring Boot Docker Guide, the images are built with the Spring Boot
layer index approach.
Run
Running With Docker
NOTE Make sure your Docker is set up properly.
We leverage Gradle Docker-compose plugin to simplify the process of composing the project with
Docker-compose. In compose/build.gradle, we provide a set of tasks to start and stop Docker-
compose services. For more details, please refer to the plugin’s documentation.
Command Description Started services
gradle
composeUp
Once project images are built, we can run the entire
project at once.
Postgres, Keycloak, Server,
Frontend
gradle
serverInitComp
oseUp
This is similar to the composeUp task, but it additionally
starts the server-init container, which is typically
used for initializing databases, sending data, or
performing other setup tasks before the main
application services start.
Postgres, Keycloak,
Frontend, Server Init ➡
Server
24

-- 24 of 68 --

Command Description Started services
gradle
noClientCompos
eUp
Run server-side components only. Postgres, Keycloak, Server
gradle
postgresCompos
eUp
Run the Postgres container specifically, serving
development purposes.
Postgres
gradle
keycloakCompos
eUp
Run the Keycloak container specifically, with data
persisted in the Postgres container.
Postgres and Keycloak
Containers should be stopped by the *Down task variant of the same name. For example, you can
stop all containers at once with:
gradle composeDown
Or to stop all containers started by the serverInitComposeUp task, use the corresponding "down" task
variant:
gradle serverInitComposeDown
Running Standalone
For development purposes, it might be more convenient to run backend and frontend services
separately as a batch job.
Standalone Keycloak
The application expects Keycloak to be running (on port 8089 by default), so a standalone Keycloak
container is a prerequisite of the next steps. Run it with:
gradle keycloakComposeUp
WARNING
Project Template’s Keycloak setup is for development purposes only. It is
necessary to significantly enhance the security of a Keycloak instance for
production environments. You can find more details regarding the
configuration in the UAA Keycloak documentation and more details on
security in the Security Guidelines.
Authorization Introspector
Project Template provides a minimal configuration for the UAA Authorization Introspector. The
configuration is placed in server/app/src/main/resources/config/application-shared.properties,
which declares the endpoints to be whitelisted for introspection:
25

-- 25 of 68 --

mgmtp.a12.uaa.authorization.web.introspection.whitelist-endpoints=/error,/api/uaa-
authentication/currentUser,/cs/download/{id},/api/sme/workspace/import,/api/sme/worksp
ace/export,/api/sme/workspace/clearDatabase
For more information about the UAA Authorization Introspector, please refer to the UAA
documentation on this topic.
Backend
There are two options for running the backend. While running it via a Gradle task is more
straightforward, running it via an IDE brings more options for debugging when you are developing.
Both options use Spring profiles for additional configuration possibilities.
The Spring Boot Devtools is enabled by default, the application automatically restarts when
changes are detected in the classpath. This significantly reduces the turnaround time during
development.
Spring Profiles
There are three aggregated Spring profiles in
server/app/src/main/resources/config/application.properties:
• dev-env - It’s the first-choice profile for development.
• local-db-env - Works exactly the same as the aggregated dev-env profile but is configured with
external postgres.
• cluster-safe-env - Aggregated for cluster mode, which means all initialization scripts are
disabled.
You can find more profiles and detailed descriptions in the Configuration section.
There are several options to set your Spring profile (the first two are recommended). The "dev-env"
as the most suitable profile for local development is used in the examples of these options below:
• Use every Gradle bootRun command with an --args='--spring.profiles.active=dev-env'
parameter.
• Add spring.profiles.active=dev-env to the bottom of your
server/app/src/main/resources/config/application.properties file.
• If the parameter approach is not working, Unix users can try to set the environment property,
for example:
# Set the property
export SPRING_PROFILES_ACTIVE=dev-env
# Run the task without parameters
gradle :server:app:bootRun
26

-- 26 of 68 --

Running With Gradle Task
You can run the server application with a Gradle task, in the example already parametrized with
the "dev-env" profile:
gradle :server:app:bootRun --args='--spring.profiles.active=dev-env'
It is a long-running job, and the server application will run under this task until you interrupt it
with Ctrl + C.
Running From IDE
IntelliJ IDEA is set up by a configuration in the .run directory.
If you need to set up the run configuration manually, it is necessary to:
1. Add desired Spring profiles.
2. Specify the working directory as server/app.
3. Remove "Add dependencies with provided scope to classpath" option.
Run the class annotated with @DataServicesApplication in the respective module:
27

-- 27 of 68 --

This allows running each service in debug mode.
Frontend
The frontend application can be run in a long-running task via npm, described in the following
example.
CAUTION For the following steps, make sure you’re inside a client/ directory containing a
package.json file and that npm is set up to use the A12 Artifactory.
"Install" and "compile" steps are included if you run the gradle "build" task, so repeating it is only
necessary after code changes.
Step Command Description
Install npm install Install all npm dependencies.
Compi
le
npm run
compile
Compile the npm project.
Run npm start Once the client application is successfully compiled, start the frontend
application (using Webpack).
It is a long-running job, and the client application will run under this task
until you interrupt it with Ctrl + C.
Accessing the Application
Once running, services are exposed on the following ports by default:
Service Port Note
Frontend :8081 Runs in Docker or standalone.
Project Template Server :8082 Runs in Docker or standalone.
Postgres :8083 Runs in Docker only.
Keycloak :8089 Runs in Docker only.
The frontend is running and accessible on http://localhost:8081.
There are three test users to log in:
28

-- 28 of 68 --

user password role
admin A12PT-admintest Admin
user1 A12PT-user1test User
user2 A12PT-user2test User
For detailed description of users' roles and permissions, please read the Document Ownership
section.
WARNING
Project Template’s login setup is for development purposes only. It is necessary
to significantly enhance the security of logins and user management for
production environments. You can find more details on configuration in the
UAA documentation and more details on security in the Security Guidelines.
Running Init Application
The init application takes care of initializing models and documents for the server application. This
is especially useful when running an application with multiple instances on a cluster environment.
Migration tasks are also included in this submodule for data migration example purposes.
Spring Profiles
In the server/init submodule, the application enables two aggregated Spring profiles, which are
placed in server/init/src/main/resources/config/application.properties:
• dev-env: This is the standard profile for local development.
• local-db-env: Same usage as dev-env, but it is configured with external Postgres instead of
Postgres in file by default.
The application also provides some additional profiles:
• init-data: Contains configuration of initialization scripts, like RPC requests.
• shared: Contains shared properties between profiles for logging and data migration.
Running Commands
Command Description
gradle :server:init:bootRun --args='
--spring.profiles.active=dev-env,init-data'
Starts the Data Services Server,
runs
JSON-RPC requests with data
stored in the import/data
directory and is terminated
after initialization.
gradle serverInitComposeUp Starts the server-init
container.
29

-- 29 of 68 --

Command Description
gradle serverInitComposeDown Stops the server-init container.
Users can also run server/init run configurations from IDE. Currently only IntelliJ IDEA and Visual
Studio Code are supported. They are configured in .run and .vscode:
• init app start (postgres-in-file)
• init app start (local-db)
Best Practices
For production purposes, it is not desired to run initialization phase everytime the server is
started.
Therefore, the init application is used to serve data initialization/migration. These use cases can be
applied into this app:
• Database migration: should be run in case of database changes.
• Model import: In case of models change or for the first time.
• Data migration: Migrate data by MigrationStep extension point.
• Execution of custom JSON-RPC requests: May be used for document import.
Troubleshooting
If you receive the following error when starting the server application:
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating
bean with name 'dsDataSource' defined in class path resource
[com/mgmtp/a12/dataservices/autoconfigure/DSEmbeddedPostgresDatasourceConfiguration.cl
ass]: Failed to instantiate [javax.sql.DataSource]: Factory method 'dsDataSource'
threw exception with message: Process [/tmp/embedded-pg/PG-
eeb889eb8aa39ea3cb783f5a8b3fbe01/bin/initdb, -A, trust, -U, postgres, -D,
../postgres/ds-embedded-postgres, -E, UTF-8, --lc-ctype=en_US.UTF-8] failed
Refer to the Data Services documentation for instructions to solve the issue.
Development Tips
Versioning
The version of the Project Template is derived from a12ReleaseLine in gradle.properties (e.g.,
2026.06).
Gradle build uses this as the default version for all subprojects. If the -PreleaseVersion parameter is
defined with Gradle build, then the version from the parameter is used.
30

-- 30 of 68 --

A12 Dependencies
It is possible to set the same version for different maven artifacts of the same A12 product in
settings.gradle. Keep in mind that this does not work for npm artifacts of A12 products; these need
to be set in client/package.json. The usage can be seen in the build.gradle files as follows:
// Native BOMs
implementation(platform(a12Libs.base.bom))
implementation(platform(a12Libs.kernel.bom))
implementation(platform(a12Libs.uaa.bom))
implementation(platform(a12Libs.dataservices.bom))
Keycloak Realms
Project Template uses the "A12Realm" realm for Keycloak as the default configuration.
To manage the realm configuration, users, roles and more, follow these steps:
1. Make sure your Keycloak container is running.
2. Open "Administration Console", by default at http://localhost:8089.
3. Login with admin/admin credentials (only in dev mode).
WARNING
Project Template’s login setup is for development purposes only. It is
necessary to significantly enhance the security of logins and user
management for production environments. You can find more details on
configuration in the UAA documentation and more details on security in
the Security Guidelines.
4. Select "A12Realm" and modify it according to your needs.
Backend
Getting familiar with the Data Services documentation is the ideal start of the backend
development and server code changes.
Extensions
The Spring Boot distribution artifacts of backend services allow setting up project-specific Spring
Boot applications with custom extensions and configurations. To register your own
implementations or extensions, add your code under src/main/java as with any Spring Boot
application.
You may need to add the @ComponentScan annotation for Spring Boot to include your extensions.
If your project uses a package different from com.mgmtp.a12.template, it is sufficient to add it to
@DataServicesApplication(scanBasePackages…) in ServerApplication, for example:
31

-- 31 of 68 --

@DataServicesApplication(scanBasePackages = {DataServicesApplication
.DATASERVICES_BASE_PACKAGE, "com.mgmtp.a12.template", "another.path.xy"})
Spring Profile Edits
Customising Spring profile properties brings a lot of enhancement possibilities for development. All
properties are described in the Data Services Configuration documentation.
Logging
A12 uses logback with Slf4j for logging and can be configured according to the needs of your
project. The Project Template provides an example logback configuration in
server/app/src/main/resources/logback-dev.xml and server/app/src/main/resources/logback-
prod.xml with security and GDPR in mind.
The configuration files serve the following purposes:
• logback-dev.xml: Is used for the development environment and logs the output in clear format.
• logback-prod.xml: Should be used in a production environment and logs the output in JSON
format.
The ch.qos.logback.core.ConsoleAppender controls logging to STDOUT. Additionally, logs are written
into files with the ch.qos.logback.core.FileAppender and an example log rolling policy.
For both appenders, the log pattern is configured to not include the MDC (Mapping Diagnostic
Context), because this would add sensitive information (e.g. username) into the log messages. Also,
both use the net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder to produce log entries
in the JSON format. The JSON format is recommended, because it prevents log injection and is
easier to handle in log post-processing.
More information about logback configuration can be found at
https://logback.qos.ch/manual/appenders.html.
Frontend
You may implement your own A12 Client application here from scratch or adjust the example
application. Getting familiar with the Client documentation is the ideal start of the fronted
development.
IDE Setup
Both VSCode and IntelliJ IDEA/WebStorm are supported.
Code Formatting and Quality Check
We are using Prettier, ESLint, and Checkstyle to enforce some (but not all) code conventions and
cleanliness.
32

-- 32 of 68 --

ESLint
• IntelliJ IDEA/WebStorm: In Settings → Languages & Frameworks → JavaScript → Code Quality
Tools → ESLint, make sure "Automatic ESLint configuration" is checked.
• VSCode: ESLint is packaged together with VSCode, so no additional configuration is required.
We also recommend enabling the format on a save option in the ESLint settings to keep your files
always formatted.
Alternatively, you can fix ESLint check errors by running:
npm run lint:fix
Prettier
See the instruction links below to set up Prettier in your favorite IDEs:
• IntelliJ IDEA/WebStorm: WebStorm Setup (includes setup for IntelliJ IDEA)
• VSCode: Editor Integration
Alternatively, you can fix Prettier check errors by running:
npm run format:fix
Checkstyle
The Checkstyle plugin performs quality checks on your project’s Java source files using Checkstyle
and generates reports from these checks. Run Checkstyle against the Java source files by this
command:
gradle checkstyleMain
Disabling Code Quality Checks for Builds
WARNING Only for development purposes. The following commands will skip the
validation of ESLint, Prettier and Checkstyle.
tool command
npm npm run skip:lint-format-compile
Gradle gradle build -PskipLintFormat=true
Gradle gradle build -PskipCheckstyle=true
Alternatively, it’s also possible to set the property skipLintFormat or skipCheckstyle to true on
project-level in the gradle.properties file.
33

-- 33 of 68 --

Java Sources Searching With IntelliJ IDEA
• Enable Downloading Java Sources
To ensure that your IDE downloads Java sources for Gradle libraries automatically, the idea plugin
has been configured by default in the root build.gradle.
idea {
module {
downloadSources = true
}
}
All library sources should be indexed, you can now search for A12 API within project and
libraries scope as follows:
• Search Scope for A12 Libraries
To improve the search for Java sources and only include A12 libraries, you can define a custom
scope.
1. Go to Settings/Preferences > Appearance & Behavior > Scopes.
2. Add a new scope, e.g. A12.
3. Define the pattern to include A12 libraries (e.g., lib:com.mgmtp.a12..*).
4. Apply the changes, and now you can use this scope in various IDE features like "Find in Files".
34

-- 34 of 68 --

For more details, please refer to the Defining Scopes in IntelliJ IDEA documentation.
Note for VSCode users, you can use Search For Symbols in VSCode to find definitions across your
workspace. However, VSCode does not support sophisticated searching within library sources as
IntelliJ IDEA does.
React and Redux Developer Tools
As with the IDE, you can also use any browser. We use Chrome for the development of the Project
Template.
Familiarize yourself with the React and Redux browser extensions, they are essential to inspect
your application’s UI and backend state:
• React Extension: React Developer Tools
• Redux Extension: Redux DevTools
Hot Module Replacement (HMR) with Fast Refresh
The Project Template uses Webpack HMR with Fast Refresh to enable instant code updates without
full page reloads during development.
Overview
Hot Module Replacement (HMR) is Webpack’s mechanism for swapping modules at runtime
without a full page reload. However, HMR alone cannot properly update React components because
it doesn’t know how to re-render them or preserve their state.
Fast Refresh builds on HMR to enable editing React components without losing their state.
35

-- 35 of 68 --

Change Type Behavior
React components (.tsx) Instant update, state preserved
Files only imported by React components
(utils, hooks, constants)
Instant update, state preserved (Fast Refresh
propagates through importing components)
Files imported outside the React tree
(appsetup.ts, module index.ts)
Full page refresh
Configuration
Fast Refresh is configured in client/webpack.dev.js as shown below:
const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin");
const ReactRefreshTypeScript = require("react-refresh-typescript");
module.exports = merge(common, {
module: {
rules: [{
test: /\.tsx?$/,
use: [{
loader: "ts-loader",
options: {
getCustomTransformers: () => ({
before: [ReactRefreshTypeScript()]
}),
transpileOnly: true
}
}]
}]
},
plugins: [
new ReactRefreshWebpackPlugin()
]
});
Module and Theme Auto-Discovery
Custom A12 modules and themes are automatically discovered by the generate script
(client/scripts/auto-discover.js), which runs at the start of npm start, npm run compile, and npm
run webpack, and can also be invoked manually via npm run generate. This script scans
client/src/modules/ for subfolders containing an index.ts and client/src/themes/ for .json files,
then generates corresponding TypeScript files (modules.generated.ts and themes.generated.ts). To
add a module, create a folder under client/src/modules/ with an index.ts that exports a default
Module object, then run npm run generate. To add a theme, place a .json file in client/src/themes/
and run npm run generate. See Adding Custom Theme for details.
36

-- 36 of 68 --

Verifying HMR with Fast Refresh
When you modify a React component file, you can verify HMR with Fast Refresh is working by
checking the browser console:
[HMR] Updated modules:
[HMR] - ./src/modules/person/components/PersonOverview.tsx
[HMR] App is up to date.
Connecting to Databases
The Project Template uses PostgreSQL as the default database. It can be run either in a Postgres
Docker container or as an embedded Postgres database in file.
You can connect to the PostgreSQL database using any compatible client tool:
• IntelliJ IDE: Use the built-in Database Tools and SQL plugin (IntelliJ IDEA Ultimate) or DataGrip
• Visual Studio Code: Use the PostgreSQL extension
PostgreSQL Docker Container
The gradle keycloakComposeUp command automatically starts a PostgreSQL container. However, if
you want to start the Postgres container separately, use the gradle postgresComposeUp command.
Connection Details:
• Port: 8083
• Configuration File: server/app/src/main/resources/config/application-local-db.properties
Starting the Server:
gradle :server:app:bootRun --args='--spring.profiles.active=local-db-env'
Alternatively, use the "server start (local-db)" run configuration in your IDE.
IntelliJ IDEA / DataGrip Connection Setup
Configure two separate database connections with the parameters shown in the screenshots below:
37

-- 37 of 68 --

38

-- 38 of 68 --

Visual Studio Code Connection Setup
Step 1: Create a server group to organize your database connections (optional).
39

-- 39 of 68 --

Step 2: Create two database connections with the following parameters:
• Data Services database connection
40

-- 40 of 68 --

• Content Store database connection
41

-- 41 of 68 --

PostgreSQL In-File (Embedded) Database
Starting the Server:
gradle :server:app:bootRun --args='--spring.profiles.active=dev-env'
Alternatively, use the "server start (postgres-in-file)" run configuration in your IDE.
Connection Details:
Parameter Value
Host localhost
Port 5434
User postgres
Password (no password required)
42

-- 42 of 68 --

IntelliJ IDEA / DataGrip Connection Setup
Visual Studio Code Connection Setup
43

-- 43 of 68 --

CI/CD
CI/CD Jenkins Pipelines in Project Template
We provide an example Jenkins pipeline to speed up the project CI/CD setup. The usage of Jenkins
pipelines related to the Project Template is described in detail in the project’s README.
CAUTION Before using the Jenkinsfile in the project, it is necessary to go through all the
//TODO marks and adapt related code to fit your deployment environment needs.
Security
Content Security Policy (CSP)
The Content Security Policy directive allows amongst others reducing the threat of Cross-Site-
Scripting (XSS) vulnerabilities in modern browsers. These directives are set via HTTP header
attributes.
Recommendations:
44

-- 44 of 68 --

• For responses with HTML content, CSP should be set to include at least: default-src 'self';
connect-src: 'self'; base-uri: 'self'. Other directives can be added and expanded according
to actual requirements. Unsafe settings for sources (e.g. script-src) like https: or 'inline-
script' should be avoided.
• For API responses that use the JSON content type, consider setting default-src: 'none'.
Attachment Upload Restrictions
It is recommended to restrict the types of files that can be uploaded as attachments by their MIME
type.
In the Project Template, this restriction is applied in the form comma-separated list of MIME types
in the server/app/src/main/resources/config/application-shared.properties. Adapt the list
according to your project needs:
• Explicitly list all allowed MIME types.
• List allowed MIME type groups by using asterisk, for example image/*.
• Decrease the security level by disabling the MIME type restriction. It is possible to use asterisk
as a value of the property or to delete the property for this.
Reload Authorization Rules
UAA supports the reloading of authorization definition rules during runtime.
The Project Template contains the required configuration and an additional access right
RELOAD_AUTH_RULES. This access right allows users with the role systemAdmin to be authorized to send
the request for reloading the authorization rules.
The implementation of this security feature can be found in
import/auth/childAuthorizationDefinition.json.
NOTE
There is no user out-of-the-box available with the systemAdmin role. In order to use
this functionality, it is necessary to create a dedicated user.
In production environments, the reload of the authorization rules has to be
triggered for every node on the cluster.
For more information about reloading authorization rules, please refer to the UAA documentation
on this topic.
Licensing and License Headers
Every source and configuration file in the Project Template carries a copyright header that
describes A12 Dual licensing usage. The header text is maintained in copyright/copyright-
template.md.
Projects based on the Community Edition are obliged to follow the EUPL License rules for project
45

-- 45 of 68 --

files header usage.
Enterprise Edition users should follow their proprietary license rules for project file headers.
Copyright Headers Gradle Tasks
Two Gradle tasks manage license headers across all project files (excluding build output and
node_modules):
gradle validateCopyrightHeaders
Checks every covered source file for a copyright header that matches the template. It is
automatically wired into gradle check (and therefore gradle build), so a missing, mismatched, or
outdated header will fail the build.
gradle updateCopyrightHeaders
Adds the header to files that are missing it, updates the copyright year and replaces any header that
no longer matches the template. Run this task after adding new source files or at the start of a new
calendar year.
Usage
Adapt the header template according to your project license and needs, then run gradle
updateCopyrightHeaders task to apply the new template to project files.
If you decide to use another way of maintaining the license headers, or your specific enterprise
license agreement does not require it, it is possible to prevent Gradle tasks from checking the
headers by:
1. removing the apply from: 'copyright/copyright.gradle' and
check.dependsOn('validateCopyrightHeaders') lines from the root build.gradle file
2. removing the copyright directory
Enhancement Possibilities
This chapter contains examples of possible enhancements and extensions of the Project Template.
Adding New Custom Module in Code
The following steps describe the import of the models created in the SME:
1. Create a new folder in import/models/<modulename>.
a. Copy models and subdirectories with models from your SME workspace to this folder.
2. Create a new folder in client/src/modules/<modulename>.
46

-- 46 of 68 --

a. Create a new file named index.ts in client/src/modules/<modulename>/.
b. Update the content of client/src/modules/<modulename>/index.ts:
i. The content should be as follows:
import { Module } from "@com.mgmtp.a12.client/client-core";
const module: Module = {
id: "myNewModule"
};
export default module;
ii. Change the id of the module from "myNewModule" to "<modulename>Module".
Changes to React components will benefit from HMR with Fast Refresh - updates
appear instantly without page reload and state is preserved. See Hot Module
Replacement (HMR) with Fast Refresh for more details.
3. Run npm run format:fix in the client directory.
4. Run gradle clean build in the root directory of the project.
5. Start the Data Services server.
6. Start the client.
NOTE
Make sure that the App Models' filenames are meaningful and represent the
respective models in the import/models folder. E.g., "Person_AM" filename for the
"Person-XYZ" models.
NOTE
If you don’t need original files (e.g., a Person module), you can remove them:
1. Delete all unwanted models in import/models/.
2. Delete all unwanted request files in import/data/request.
Adding Custom Theme
In order to add a custom theme to the Project Template, follow these steps:
• Place a new theme JSON file in the client/src/themes folder. The file must have a .json
extension.
• Name the file meaningfully, e.g., my-dark-theme.json. The application will convert the filename
to a display name (e.g., "my-dark-theme.json" will be displayed as "My Dark Theme" in the
theme selector).
• Content of the theme JSON file should follow the interface DefaultThemeType, or you can refer to
Widgets Theming.
• Run npm run generate (from the client/ directory) to regenerate themes.generated.ts, which will
47

-- 47 of 68 --

include the new theme.
NOTE The generate script also runs automatically at the start of npm start, npm run
compile, and npm run webpack. Each theme must be in a separate JSON file.
NOTE
The generate script validates theme JSON files at build time. Each key in the JSON
must be a valid top-level property of DefaultThemeType. An invalid key will cause the
generate step to fail with an error.
NOTE
The theme name base is reserved by the default base theme. Using this name for a
custom theme will override the default base theme instead of adding a new option
to the theme selector.
Working With the SME
Model Deployment
The SME provides functionality to deploy models during runtime.
NOTE It’s necessary that the Project Template application including Keycloak, Client, and
Server are running before models can be deployed.
Configuring a Web SME
To configure this for the Project Template, changes in Keycloak are necessary.
NOTE
The web SME used in the example requires access to the mgm internal
infrastructure (mgm internal only). If your project has a deployed SME instance, set
the respective URl in the following property.
Walk through the following steps to configure your Keycloak instance, to accept deployment
requests from the web SME:
1. Go to your running Keycloak instance under localhost:8089.
2. Log in with "admin/admin" credentials.
3. Select the "A12Realm" in the top-left corner dropdown.
4. Go to Clients → a12-spa-client.
5. Add your web SME URL to Valid redirect URIs and Valid post logout redirect URIs.
6. Click the btn:[Save] button.
Deploying Models
Go to your SME and configure your server connection and set the values as follows:
48

-- 48 of 68 --

• Authentication Type: "OIDC"
• URL: "http://localhost:8082/api/"
• OIDC URL: "http://localhost:8089/realms/A12Realm"
• Client: "a12-spa-client"
Afterward, login with the "admin/A12PT-admintest" user credentials.
After logging in, the Keycloak screen will disappear and a notification about the successful login
will be displayed.
Deploying the Seed Data
The Project Template, if run with the dev properties, is preconfigured to work with the Seed Data.
This feature helps to create initial data in the SME and deploy it to your application server. This
data can be documents, relationship links, attachments, roles and users.
NOTE
Seed Data for Users does not work with the Keycloak, thus it can be only used with
the Local Authentication variant of the Project Template. Other types of Seed Data
work with all variants of the Project Template.
WARNING
The Seed Data related operations, endpoints and data are not intended for
production environment usage; they serve development and testing purposes
only and do not provide the security features required for production use
cases.
Deploying Seed Data relies on the SME workspace import/export/delete REST
endpoints, provided by the dataservices-sme-workspace-support dependency.
This dependency is declared as developmentOnly in server/app/build.gradle, so
it is excluded from the production build and its endpoints stay unreachable in
production.
Importing a Workspace
If you want to start with a new project containing completely new models, clean the project from
current models and data and add new models with the following steps.
CAUTION
Make sure there is no persisted application data. If you already started the
application previously with different models, you have to clean the persisted
data with one of these two options:
1. Clean the persistent storage:
◦ If you ran the application Postgres file-based, delete the server/postgres
folder.
◦ If you ran the application with a local Postgres database configuration,
delete the database and create it again.
49

-- 49 of 68 --

◦ If you ran the application with the Postgres Docker container:
▪ Delete the container and the image, either automatically with gradle
composeDown, or with docker rm command.
▪ Find and delete the related Docker volume with docker volume ls
and docker volume rm commands.
2. Remove only the data from the storage:
◦ Remove models with the help of DELETE Model REST API endpoint, or
with the help of initialization import properties.
◦ Remove documents within the application or with related JSON RPC
operations.
◦ Attachments without a reference will be deleted automatically by the
cleanUpDirtyAttachmentsJob.
1. Remove old models in import/models.
2. Remove old requests in import/data/request.
3. Move all prepared models of your workspace from the SME to import/models and adjust the
following:
a. The roles, which are used in the models, must match the roles that are defined in the Project
Template (and Keycloak). If that is not the case, replace the roles in your models with the
default values used in the Project Template: admin, user.
b. All the View Engines you define in the App Model Scenes must be included and match the
keys in the enginesViewMap constant in client/src/app/EnginesViewMap.tsx, and each must be
registered via a corresponding addView() call in client/src/appsetup.ts.
NOTE
Make sure the App Models' filenames are meaningful and represent the
respective models in the import/models folder. E.g., "Person_AM" filename for
the "Person-XYZ" models.
4. Delete unnecessary
server/init/src/main/java/com/mgmtp/a12/template/server/init/migration/PersonMigration.java.
If you want to keep it as an example for future reference, make sure
mgmtp.a12.dataservices.initialization.migration.enabled in
server/init/src/main/resources/config/application-shared.properties is set to false.
5. If you added View Engines, or modified client code another way, run npm run format:fix in the
client directory.
6. Start the server and the client.
Data Migration Support
Every project will sooner or later face the situation, where they modify their Document Models
(e.g., changing the field type or removing an existing field).
To make this more accessible for new projects, the Project Template provides an example of a
50

-- 50 of 68 --

document migration. Example structure and a small sample migration task for executing data
migration via MigrationStep extension point is placed in the server/init path.
We convert the document to a JSON Node and modify it while performing the data migration
instead of using Kernel functionality.
It is necessary to ensure that the document is valid and deserializable after migration, which has
the following consequences:
• These migration steps are for migrating documents from version A to version B, where version
A is not valid anymore because we have new models compliant with version B.
So in the MigrationStep you must make sure that documents are valid for version B if you want
to migrate them.
• Using the Kernel implementation creates an issue here, because documents are not
deserializable anymore due to the mismatch between the document and the new Document
Model.
The solution is to modify the content of the documents after loading it from the repository to fit
the new models before the documents are being deserialized.
Please refer to the Data Services documentation to get more information.
Instructions
In the Project Template, the example for the data migration is based on the following overall steps:
1. A new package is added to the server/init under
server/init/src/main/java/com/mgmtp/a12/template/server/init/migration.
2. A new file named PersonMigration is created.
3. A MigrationStep and MigrationTask are added to remove the "PlaceOfBirth" field from the
documents of the Person-document model.
NOTE
Data migration is disabled in the Project Template by default. In order to enable it,
set mgmtp.a12.dataservices.initialization.migration.enabled=true in the
server/init/src/main/resources/config/application-shared.properties.
Document Ownership
The document ownership concept defines the rules and permissions associated with creating,
viewing and modifying the documents.
It involves two primary roles, Admin and User. Each role has specific privileges regarding
document ownership, ensuring secure and controlled access to the created documents:
• Admin has full control over all documents.
• User has control only over the documents they created.
This approach provides a secure and organized environment, and this structure ensures data
integrity and confidentiality within the application.
51

-- 51 of 68 --

Implementation
The document ownership is driven by the read and delete permissions in the
import/auth/childAuthorizationDefinition.json.
WARNING
It’s essential to note that this document ownership approach is specifically
tailored for the Project Template example Document Models.
It is crucial to adapt and handle document ownership on your own and based
on the specific requirements of your application needs. If not, your application
could be exploited by attackers.
End-to-End Testing
The Project Template uses Playwright as the testing framework for end-to-end (E2E) tests.
Our objective is to test a few core features of the template application. Please expand it with
additional test suites to match your project’s scope.
The tests and related code are located in the e2e directory.
Playwright
Playwright is a modern end-to-end testing framework developed by Microsoft that enables reliable
testing across all modern browsers (Chromium, Firefox, WebKit). It provides an excellent
experience for writing, debugging and running tests with its powerful API and built-in features like
auto-wait, network interception, parallel test execution, screenshots, videos and trace viewer, etc.
Playwright also provides some powerful tools like:
• Codegen
• Playwright inspector
• Trace Viewer
If you are new to Playwright or are not familiar with it, you can learn more about it by reading its
official documentation here Playwright.
Prerequisites
To run the E2E tests, ensure you have the following prerequisites:
• Your application is running/accessible. You can follow the instructions in the Run section.
• Go to e2e directory:
cd e2e
52

-- 52 of 68 --

• Install all the dependencies:
npm install
npm run e2e:install
File Structures
• tests contains all the test suites.
• fixtures contains all the test fixtures, mock data
• pages contains all the page object models (POMs). It helps to optimize ease of authoring and
maintenance for your test suites.
• types contains all the data test IDs, some test data types.
• utils contains all the helper functions, e.g. files handling.
Configuration Tests
You can update the playwright.config.ts file to point to the base path of your running application.
In most cases you can leave it as the default http://localhost:8081.
Running Tests
You can run the tests via the interactive UI mode:
npm run e2e:test-ui
You can also run the tests in the terminal (default in headless mode)
npm run e2e:test
You can also use cross-env to set the BASE_URL environment variable. This might be useful if you
want to run the tests against a deployed version of the project in a CI context:
npx cross-env BASE_URL=<YOUR-DEPLOYED-APPLICATION-URL> npm run e2e:test
Showing the HTML Report
npm run e2e:report
53

-- 53 of 68 --

Configuration
The following paragraphs contain all configuration points, which the Project Template provides to
projects.
Available Aggregated Profile Supported
This section contains the default configuration of the Project Template server side. The partial
profiles are combined into the Aggregated profile to cover specific Project Template setup.
Aggregated
profile
Included profiles Use Case
dev-env • dataservices-uaa
• dataservices-
embedded_contentsto
re
• dataservices-
embedded_postgres
• contentstore-
embedded_postgres
• dataservices-rpc
• dataservices-
actuators
• shared
• dev
Default choice spring profile to run the Project
Template in the development environment.
⚠️ Used when running
server_start_postgres_in_file.run.xml run
configuration.
local-db-env • dataservices-uaa
• dataservices-rpc
• dataservices-
actuators
• dataservices-
embedded_contentsto
re
• shared
• dev
• dataservices-
external_postgres
• contentstore-
external_postgres
• local-db
Default choice spring profile to run the Project
Template in the development environment with an
external postgres database connected.
⚠️ Used when running server_start_local_db.run.xml
run configuration.
⚠️ Please replace the placeholders with your
database credentials in
server/app/src/main/resources/config/application-
local-db.properties.
54

-- 54 of 68 --

Aggregated
profile
Included profiles Use Case
cluster-safe-env • dataservices-uaa
• dataservices-cluster
• dataservices-
embedded_contentsto
re
• dataservices-rpc
• shared
• prod
Default choice spring profile to run the Project
Template in a cluster environment.
⚠️ All initialization scripts are disabled.
WARNING
Since the aggregated profiles are the composition of Data Services predefined
profiles and customized ones, please make sure you put your customized
profiles after the predefined ones. Otherwise, the predefined profiles will
overwrite your customization. And the name of the aggregated profile must
NOT be duplicated with customized and predefined profiles.
Data Services Pre-configured Profiles Used in Project
Template
Profile name Usage
dataservices-uaa Set default UAA configuration such as:
• The authorization definition file with role base support.
• Enforce authentication.
• Granting the super user privileges.
• Switch off protection for the attachment thumbnails.
dataservices-
embedded_conten
tstore
Configure server to use the content-store database ( storage for the
attachments and thumbnails).
dataservices-
embedded_postgre
s
Use embedded Postgres in-memory database.
contentstore-
embedded_postgre
s
Use embedded Postgres in-memory database.
dataservices-rpc Enable all available RPC operations and usage of SpEL expressions in them.
dataservices-
actuators
Enable Spring Boot Actuators for all endpoints.
55

-- 55 of 68 --

Profile name Usage
dataservices-
external_postgres
Set up the application to use Postgres database.
contentstore-
external_postgres
Set up the content store to use Postgres database.
dataservices-
cluster
Configure the server for cluster mode (used by the cluster-safe-env
aggregated profile).
NOTE Some properties of Data Services default profiles are overridden in Project
Template profiles. Please see the Shipped Configurations for details.
CAUTION These profiles should undergo a security check before being used in non-
development environments.
For more details on profiles, please see the Data Services configuration profiles documentation.
Shipped Configurations
Common Configuration
This contains configuration which is used in the plain Project Template and present in all other
variants.
File Usage
setup.json • Used for script and Gradle task
'replacePlaceholders' to replace Project
Template specifics with actual project
specifics.
import/auth/roles.yaml • Definition and mapping of roles to access
rights.
tool-versions.json • Used for script and Gradle task
'checkToolVersions', which is also checked
on every Gradle build.
• Checks for all dev tool versions to match.
56

-- 56 of 68 --

File Usage
server/app/src/main/resources/config/applicati
on-dev.properties
• Contains Data Services initialization
properties for importing models and loading
the roles file.
• Contains Postgres properties that configure
data storage to a file instead of in-memory,
overriding the default properties of the
dataservices-embedded_postgres and
contentstore-embedded_postgres profile.
• Contains UAA Keycloak configuration
properties.
• Enables seed data related endpoints and
operations.
server/app/src/main/resources/config/applicati
on-local-db.properties,
server/init/src/main/resources/config/applicat
ion-local-db.properties
• Contains default properties for connecting
to an external Postgres database.
• Configuration can be used as it is, when an
external database is using this values.
Otherwise, projects can adjust the values in
the profile.
server/app/src/main/resources/config/applicati
on-shared.properties
• Contains application properties which are
shared between the Spring profiles.
◦ Definition of allowed RPC operations: we
leverage RPC operation group to define
the allowed RPC operations (we should
only include the ones that we actually
need).
◦ Refers to default UAA
authorizationDefinition file.
◦ Contains the endpoints that are
whitelisted for introspection.
◦ List of MIME types allowed for
attachment uploads.
◦ Content store is set up to save content in
the database.
server/app/src/main/resources/config/applicati
on-prod.properties
• Contains application properties for
production environment.
◦ Set up INFO logging.
◦ Management endpoints can be accessed.
Just expose health and info endpoint for
production environment.
57

-- 57 of 68 --

File Usage
server/init/src/main/resources/config/applicat
ion-init-data.properties
• Contains application properties for
execution of Custom JSON-RPC requests
during server initialization phase.
◦ Enable JSON-RPC request can be
executed.
◦ Specifies files that are executed.
server/init/src/main/resources/config/applicat
ion-shared.properties
• Contains application properties for data
migration.
◦ Enable migration of custom tasks (e.g.
Document or Model migration) on
system initialization.
WARNING
• The postgres username is hardcoded and the password is not set. Consider
changing it.
• Adjust the external Postgres configuration to align with the project.
Variants' Configuration Differences
០ = Overrides Common Configuration
Workflows
File Usage
០
server/app/src/main/resources/applica
tion-dev.properties
• UAA Keycloak configuration.
• Camunda Connection configuration.
០
server/app/src/main/resources/applica
tion-shared.properties
• Additional allowed Workflows-related RPC operations.
workflows/camunda/src/main/resources/
application.properties
• Only configuration of the Spring profile.
workflows/camunda/src/main/resources/
application-dev.properties
• Workflows UAA authentication configuration.
• Roles file configuration.
NOTE The project should remove properties that Project Template suggests to be removed
in production.
Local Authentication
File Usage
០ server/app/src/main/resources/application-
dev.properties
• UAA local configuration.
58

-- 58 of 68 --

Localization
The Project Template provides a built-in localization system that supports multiple languages and
locales. It uses the Utils Localization library to manage localization across the application.
Default Locales
The Project Template includes translations for the following locales by default:
Language Language Code Country Code
English en US
German de DE
Users can switch between locales at any time using the locale chooser dropdown in the application
header. The selected locale is immediately applied to all UI text and date/time formatting.
Default Translations
The Project Template includes pre-configured translations for both supported locales. These
translations cover:
• Application labels: Header text, user information labels (e.g., "Logged in as"), and logout button
• Keycloak messages: Processing and error messages displayed during authentication
• Error messages: Security authorization errors, attachment validation errors, and content size
errors
• A12 component translations: Translations from integrated A12 components are automatically
included
Localization Setup
Localization is configured in client/src/appsetup.ts as part of the composable application setup.
The withLocalization feature is applied via combineFeatures, and the localization settings are passed
through initialConfig:
localization: {
supportedLocales,
translationSource: DEFAULT_TRANSLATIONS,
getDateTimeResource
}
• supportedLocales: The list of available locales with their display names (from
client/src/localization/index.ts)
• translationSource: The DEFAULT_TRANSLATIONS map combining all locale resource files
59

-- 59 of 68 --

• getDateTimeResource: A function returning the date-fns locale for date/time formatting based on
the current language
Localization Structure
The localization files are located in the client/src/localization/ directory:
client/src/localization/
├── index.ts # Main localization configuration
├── keys.ts # Key structure definitions for custom labels
└── resources/
├── en_US.ts # English translations
└── de_DE.ts # German translations
Updating a Label
To override a default label from an A12 component, follow these steps. This example changes the
attachment "replace" button label to "Override".
Step 1: Find the Key Path
Identify the correct key path from the component’s localization documentation. Consult the A12
Component Translations section for links to each component’s localization documentation.
For this example, the Form Engine attachment button uses the key path attachment.button.replace.
Step 2: Add the key structure to client/src/localization/keys.ts:
export const RESOURCE_KEYS = {
application: {
header: {
userinfo: {
labels: {
loggedInAs: "",
logoutButton: ""
}
}
},
},
// ... other keys
attachment: {
button: {
replace: ""
}
}
};
60

-- 60 of 68 --

Step 3: Add the translation value in your locale files.
In client/src/localization/resources/en_US.ts:
export const en_US: LocalizationKeyTreeType = {
// ... other translations
attachment: {
button: {
replace: "Override"
}
}
};
In client/src/localization/resources/de_DE.ts:
export const de_DE: LocalizationKeyTreeType = {
// ... other translations
attachment: {
button: {
replace: "Überschreiben"
}
}
};
Adding New Labels
To add new custom labels to the application, follow these steps:
Step 1: Define the Key Structure
Add the new key to the RESOURCE_KEYS object in client/src/localization/keys.ts:
export const RESOURCE_KEYS = {
application: {
header: {
userinfo: {
labels: {
loggedInAs: "",
logoutButton: ""
}
}
},
// Add your new keys here
myComponent: {
title: "",
description: ""
}
},
61

-- 61 of 68 --

// ... other keys
};
Step 2: Add Translations
Add the translations for each supported locale.
In client/src/localization/resources/en_US.ts:
export const en_US: LocalizationKeyTreeType = {
application: {
// ... existing translations
myComponent: {
title: "My Component",
description: "This is my new component"
}
},
// ... other translations
};
In client/src/localization/resources/de_DE.ts:
export const de_DE: LocalizationKeyTreeType = {
application: {
// ... existing translations
myComponent: {
title: "Meine Komponente",
description: "Das ist meine neue Komponente"
}
},
// ... other translations
};
Step 3: Use the New Labels
Access the new labels in your components using the useLocalizer hook:
import { useLocalizer, RESOURCE_KEYS } from "../../localization";
function MyComponent() {
const localizer = useLocalizer();
return (
<div>
{localizer(RESOURCE_KEYS.application.myComponent.title)}
</div>
);
62

-- 62 of 68 --

}
Adding a New Locale
To add support for a new locale (e.g., French), follow these steps:
Step 1: Create the Translation File
Create a new translation file in client/src/localization/resources/, for example fr_FR.ts:
import { LocalizationKeyTreeType } from "../keys";
export const fr_FR: LocalizationKeyTreeType = {
application: {
title: "Your-Project-Name",
header: {
userinfo: {
labels: {
loggedInAs: "Connecté en tant que",
logoutButton: "Déconnexion"
}
}
}
},
// ... other translations
};
Step 2: Register the Translation
Update client/src/localization/index.ts to include the new locale:
import { de, enUS, fr, type Locale as DateLocale } from "date-fns/locale";
// ... other imports
import { fr_FR } from "./resources/fr_FR";
export const DEFAULT_TRANSLATIONS: LocalizationTreeMap = {
en: en_US,
de: de_DE,
// Add new locale
fr: fr_FR
};
export const supportedLocales: LocalizedLocale[] = [
{ language: "en", country: "US", name: { key: RESOURCE_KEYS.locale.en } },
{ language: "de", country: "DE", name: { key: RESOURCE_KEYS.locale.de } },
// Add new locale
{ language: "fr", country: "FR", name: { key: RESOURCE_KEYS.locale.fr } }
];
63

-- 63 of 68 --

const DATE_LOCALES: Record<string, DateLocale> = { en: enUS, de, fr };
NOTE
When adding a locale key like RESOURCE_KEYS.locale.fr, also add the corresponding
entry to the locale object in client/src/localization/keys.ts and provide its
translation in each locale resource file.
A12 Component Translations
The Project Template integrates translations from various A12 components. These resource keys
are imported in keys.ts and their translations are automatically available when the locale is set.
To learn how to customize them, refer to their respective documentation:
• Client Localization
• Form Engine Localization
• Overview Engine Localization
• Tree Engine Localization
• Relationship Engine Localization
Variants
Not only does the Project Template provide a plain starting point for new projects, but it is also
packed with a number of out-of-the-box A12 functionalities and components.
In this chapter, we will introduce different variants of the Project Template and their main
functionalities.
Local Authentication
User Authentication and Authorization (UAA) simplifies and standardizes most common
authentication types.
By default, the Project Template uses "OAUTH2" as a UAA authentication type. It uses OAUTH2 for
authorization, OIDC for authentication and Keycloak as Identity Provider (IDP).
Nevertheless, providing a Project Template using the LOCAL mode benefits development purposes by
significantly reducing setup time.
WARNING
The LOCAL authentication type is only intended for development, demonstration
and training purposes. It is necessary to significantly enhance the security of
user management and use a different authentication type for production
environments.
Description
The LOCAL authentication type is used in the simple scenario where the application also acts as the
64

-- 64 of 68 --

IDP. This means that while UAA provides a login endpoint, which verifies user credentials, the
application is responsible for storing user data and validating users' passwords.
LOCAL is using the standard UAA JWT Token generation and processing.
If the authentication type is set to LOCAL, the application will load user data from a resource file.
Therefore, we have configured additional settings to load the data in the application.
For more information about LOCAL authentication, please refer to the UAA documentation on this
topic.
Download
NOTE We are preparing the Local variant for 2026.06. In the meantime, please find this
variant in the 2025.06-ext5 documentation.
Workflows
The Workflows component introduces an architecture in which Workflow management becomes
an extension of Data Services.
Description
In this variant, we use the Workflows component, which integrates Business Process Model and
Notion (BPMN) modeling capabilities into A12. BPMN enables graphical modeling of server-side
workflows and their execution.
Documents may serve as input and output for a Workflow, and the UI for user tasks can be modeled
using the existing A12 modeling approach.
A new module workflows including a Camunda service is added, which communicates with Data
Services asynchronously.
Models that are involved in this integration are explained in the following subsections.
WARNING This variant requires certificates for modules communication. See the
Certificate Authentication section for details.
Document Models
• OrderProcess_DM is the Document Model containing the domain data for orders. It includes the
Task_DM.
• Task_DM is used to display different processes of making an order.
Form Models
• OrderProcess_FM defines the UI for the Enter Order details task.
• OrderConfirmation_FM defines the UI for the Confirm order task.
65

-- 65 of 68 --

Overview Models
• OrderProcess_OM links to the OrderProcess_DM Document Model that models data of its order and
corresponding process.
BPMN
The Workflows component also requires a BPMN model along with Document Models and UI
models.
• OrderProcessWithoutInput.bpmn:
◦ Defines the business process.
◦ Defines the expected input and output document of the process.
◦ Defines the document name and Document Model name for the process.
◦ Connects tasks to data and UI models.
• The circle with thin border in the top-left is the start node, the entry point into the process.
• First, the process reaches the user task "Enter order details", in which a customer enters their
order (in our context in A12, this is done via a form).
• Once the customer completes the task, the two following steps will be handled:
◦ The customer’s order detail will be canceled and reach the end.
◦ The order details are sent to the next task "Confirm order". Here, the customer can approve
the order.
• Then the process ends.
Please refer to the Workflows documentation to see more details.
Download
NOTE We are preparing the Workflows variant for 2026.06. If you are an enterprise user,
66

-- 66 of 68 --

or mgmie, you can find this variant in older release documentation.
Notification Center
The Notification Center is an A12 component capable of supporting users by keeping track of the
events occurring in the application. The purpose of the Notification Center is to manage A12
document-related tasks and events by multiple kinds of notifications presented to the users.
Description
This variant integrates the add-on provided by the Notification Center and the Data Distribution
dependencies used by the Notification Center under the hood. This integration provides two
features related to notification publishing:
• Display notifications to the user whenever a Person document is successfully created or deleted.
For now, notifications are only displayed for the user, which triggered the events.
• Reminder is a new module that Notification Center provides. "Reminders" can be used by users to
remind themselves or a group of users to work on certain tasks like application, appointments
or any other kind of document.
"Reminders" contain two different datetime fields. Whenever the time set in these fields is
reached, a notification is created automatically and sent to the recipients specified in the
"reminder" document.
WARNING This variant requires certificates for the communication between the modules.
See the Certificate Authentication section for details.
Document Models
• A12Reminder_DM is the Document Model provided by the Notification Center. It includes the data
for the "reminder".
NOTE
Using A12Reminder_DM is mandatory. If you want to customize your "reminder"
document, you must include this model and name it A12Reminder. Also, there
must be a root group called a12Reminder.
Form Models
• A12Reminder_FM defines the UI for displaying the Reminder task.
Overview Models
• A12Reminder_OM is a UI model that displays a list of data. It is built on the A12Reminder_DM model.
Please refer to the Notification Center documentation to see more details.
Download
NOTE We are preparing the Notification Center variant for 2026.06. If you are an
67

-- 67 of 68 --

enterprise user, or mgmie, you can find this variant in older release documentation.
68

-- 68 of 68 --

