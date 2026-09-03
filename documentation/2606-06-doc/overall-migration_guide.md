# overall migration_guide

Migrating to Latest A12 Release Line
This documentation will help you to go through the migration process. It
describes one of the ways to successfully migrate your project to the newest A12
version.
CAUTION
These instructions describe only the migration from the previous major release
to the current one. If you need to migrate to an earlier release, consult the
corresponding documentation published for that specific release. More
information can be found in the Migration Guide section.
• In the Overview you will get insights into A12 components and their dependencies.
• The Migration Guide explains the general process of how to upgrade models, server, and client
in an A12 project.
• The Automatic Source Code Refactoring chapter (introduced in 2024.06) describes the usage of
automatic code refactoring tooling to automatize some aspects of A12 migration process.
• The section Migration to 2026.06 highlights the most important changes in 2026.06. It
summarizes the usage of the automatic code refactoring tools and describes the steps, which are
required for a successful migration to this overall version of A12.
• If something looks unclear, there are the FAQs.
• Feel free to discuss at A12 Discourse → Migration Topics for 2026.06.
NOTE Please share with us your experience, so we can improve this guide and make the
migration easier.
Overview
A12 consists of many different components that are managed by different teams. This creates a
rather complex dependency graph. Therefore, a good first step is to have a look at the different
component dependencies. The following graph is based on the Project Template dependencies and
contains only a subset of A12 components.
1

-- 1 of 42 --

Utils Logging & Collections
com.mgmtp.a12.utils
npm
Utils Localization
com.mgmtp.a12.utils
npm
Utils Server Connector
com.mgmtp.a12.utils
npm
Base
com.mgmtp.a12.base
mvn + npm
Widgets
com.mgmtp.a12.widgets
npm
Expression
com.mgmtp.a12.expression
npm
Diagram Editor
com.mgmtp.a12.diagrameditor
npm
Kernel
com.mgmtp.a12.kernel
mvn + npm
Data Services
com.mgmtp.a12.dataservices
mvn + npm
Data Distribution
com.mgmtp.a12.datadistribution
mvn + npm
Workspace Conversion Framework
com.mgmtp.a12.dataservices.wcf
mvn
UAA
com.mgmtp.a12.uaa
mvn + npm
User Management
com.mgmtp.a12.uaa
mvn + npm
Client
com.mgmtp.a12.client
npm
Form Engine
com.mgmtp.a12.formengine
mvn + npm
Overview Engine
com.mgmtp.a12.overviewengine
npm
Tree Engine
com.mgmtp.a12.treeengine
npm
Print Engine
com.mgmtp.a12.print
mvn + npm
Content Engine
com.mgmtp.a12.contentengine
npm
Relationship Engine
com.mgmtp.a12.relationshipengine
npm
CRUD
com.mgmtp.a12.crud
npm
CMS
com.mgmtp.a12.cms
mvn + npm
Workflows
com.mgmtp.a12.workflows
mvn + npm
Notification Center
com.mgmtp.a12.notificationcenter
mvn + npm
Type
● mvn
● npm
● mvn + npm
Figure 1. A12 components dependency overview graph
With this overview you should be able to easily identify the chain of necessary component updates
that may occur by updating version of a single A12 component.
IMPORTANT
Keep in mind that all artifacts of an A12 component need to have the same
version. This especially applies for A12 components that have frontend and
backend artifacts. To identify an A12 component with frontend and backend
2

-- 2 of 42 --

libraries, please have a look at the dependency overview graph.
TIP
Every artifact contains its component affiliation under the scope
(@com.mgmtp.a12.<component_name>) or group id
(com.mgmtp.a12.<component_name>).
Migration Guide
This guide aims to describe, how the overall migration can be done and assumes an upgrade of an
A12 based project with server and client parts.
The migration follows a fixed order. Complete each step before you start the next one:
1. Preparation: choose the target release and review the Migration Instructions of every
component your project uses.
2. Model Migration: migrate all models with the SME (recommended) or the command-line tools.
3. Code Migration: apply automatic source code refactoring (codemods) first, then update the A12
dependencies. You can migrate the Server and Client parts in any order.
4. Compile and Build: resolve the remaining breaking and deprecated changes, then run the
project.
5. Infrastructure and Deployment: update the runtime services, base images, resource settings,
and Helm charts, then deploy the application.
6. Verification: run the checks that confirm the migration succeeded.
This order is independent of the A12 version you migrate to. The version-specific changes are
described in the migration section for your target version.
Preparation
• Check the Release Table
◦ Find the A12 overall release, you want to upgrade to. The latest versions for each component
are listed in the respective release line.
◦ If you have to upgrade multiple A12 overall versions, we recommend upgrading one by one
and not skipping over any major versions (minor and patch versions can be skipped).
For example to upgrade from 2024.06 → 2026.06, it is necessary to migrate these versions:
2025.06 and 2026.06:
▪ Check the release line 2025.06, apply all breaking changes and fix upcoming issues.
▪ Check the release line 2026.06, apply all breaking changes and fix upcoming issues.
CAUTION Migration is only supported from versions that were released before your target
version.
• To get more information about a specific A12 component, you can look into its documentation
3

-- 3 of 42 --

Migration Instructions section. Links to the migration instructions chapters for every
component can be found in the Links section.
• Before you start, check the Known Issues of your target version’s migration section, for
example the Known Issues for 2026.06. When that section lists entries, they collect the behavior
changes and late fixes made during the release cycle, such as reverted breaking changes or
required workarounds, so you can plan for them instead of discovering them during the
upgrade.
• For a successful project migration you need to update the following: models, server, and client.
We recommend to start with model updates. The order of migration for frontend and backend
parts does not matter.
Model Migration
Every A12 model comes with its own model versions. These versions are bound to the overall A12
version. You need to update all models, which are used.
In the SME for 2026.06 you will not be able to open models with version 2025.06 and older. Proper
model versions are listed in the release table mentioned above if you hover over the SME version in
the release line.
TIP
As new features are added and Model versions move forward the Simple Model Editor
may not recognize the Model version and therefore block migration.
Manually rolling back the Model Version is not supported but is possible as long as
new Modeling Features added in your current release are not used.
1. Check the Release Table to find model versions for an older release in your current
release line.
2. Manually change the Model Version.
3. Try to migrate the models using the target Simple Model Editor.
Tools
There are two options:
1. SME - distributed via the A12 Installer.
◦ Install the version of the Modeling Environment which corresponds to the A12 version you
want to upgrade to.
◦ Consider creating a backup of your models before starting the migration.
◦ Open the folder containing your models in the SME.
◦ Error "Some models are not compatible with current SME version" is shown.
◦ After clicking "Resolve all issues" and confirming the migration, all models will be updated.
◦ Additionally, you should know about the Migration Rules when migrating a modeling
environment workspace.
2. Command-Line Tools
4

-- 4 of 42 --

◦ Each component responsible for models provides a command-line tool to upgrade its
corresponding models.
◦ All command-line tools can be downloaded from Artifactory. See the following overview of
available migration tools with links, where you can find more information on how to use
them.
Model CLI Migration Tool
Document Model Data Model Migration Tool
Relationship Model Relationship Model Migration Tool
Form Model Form Model Migration Tool
Overview Model Overview Model Migration Tool
Tree Model Tree Model Migration Tool
Application Model Application Model Migration Tool
Content Model Content Model Migration Tool
Print Model Print Model Migration Tool
Transformer Model (Enterprise) Transformer Model Migration Tool
After Model Migration
With a breaking release it may be possible that some modeling aspects (rules, computation…) have
been changed. You need to check the component release page and migration instructions to review
particular changes. See Migration to latest A12 Overall Version for actual information.
Code Migration
After you have migrated your models, it is necessary to migrate the code base. In 2024.06 we
introduced automatic source code refactoring for applying breaking changes in your code.
These cover changes on client-side as well as on server-side. In 2025.06 we enhanced this idea even
more. See Automatic Source Code Refactoring chapter for more information and detailed
instructions.
Server Migration
In our guide we start with the backend part for the upgrade. For automatic code refactoring of the
backend we use the open source libraries of OpenRewrite.
Apply OpenRewrite Recipes
Available rewrite artifacts and recipes are listed in the section about Automatic Source Code
Refactoring.
Update Dependencies
• Update the A12 dependencies according to the versions in the release table for the overall
5

-- 5 of 42 --

version you want to upgrade to.
• The file where versions are defined depends on the build tool you are using:
◦ Maven - update your pom.xml.
◦ Gradle - update dependencies which are usually located in settings.gradle file.
TIP
For more information please look into Gradle documentation. Next to this
documentation there are separate pages for downgrading and aligning. You can also
check similar topics in the left menu, it is handy to know.
• Check and update A12 dependencies (Base, Kernel, Data Services, Workflows, …).
• Check other third-party dependencies (Spring, Spring Boot, Jackson, …).
Client Migration
For automatic code refactoring of frontend we use Hypermod (formerly known as:
Codemods/Codeshift). See Migration Instructions of Client and how they use codemods based on
Hypermod.
Available codemod artifacts are listed in the section about Automatic Source Code Refactoring.
Update & Install Dependencies
• Update the A12 dependencies in package.json according to the versions in the release table for
the overall version you want to upgrade to.
NOTE Components with multiple artifacts use the same version for all artifacts.
• Run npm install.
• Check peer dependencies issues and update third-party dependencies. Try to fix all issues and
run npm install again.
• Update your code according to the migration instructions.
Compile & Build
• Compile the project.
• If the project has not compiled, follow the errors and fix them. If the issue is caused by an A12
dependency please check the respective documentation.
• After successful compilation, run the project.
• New errors can appear. Please continue with processing of breaking and deprecated changes.
IMPORTANT
If you think that a breaking change is not covered by the components'
migration instructions, please reach out to us via the usual communication
channels (e.g. creating bug tickets, Discourse, Support Portal).
6

-- 6 of 42 --

Infrastructure and Deployment
After the application builds, update the runtime infrastructure to match the target release before
you deploy. Infrastructure changes are a regular part of an upgrade, not an afterthought.
Review the following areas:
• Runtime services: a release may drop or replace a service, such as a database, search engine,
or workflow engine. Check the version-specific migration section for services that were
removed or replaced, and adjust your deployment accordingly.
• Base images: update the base images of your containers to the versions required by the target
release, for example the Java runtime.
• Resources: review the CPU and memory requests and limits, because changed runtime services
can change the resource footprint.
• Helm charts: update the A12 Helm charts. See the Helm Stack Charts Migration Instructions.
The concrete services, image versions, and resource changes for your target release are listed in the
version-specific migration section.
Verification
After deployment, verify that the migration succeeded before you hand the environment back to
the project.
Run at least the following checks:
• The project compiles and the server starts without errors.
• The automated test suites (unit, integration, and end-to-end) pass.
• All models load in the runtime without version errors.
• The client application starts and the main flows work, such as opening, editing, and saving
documents.
• Authentication and authorization work, including login and role-based access.
• Data created before the migration is still readable and consistent.
• Scheduled tasks, background jobs, and asynchronous processing run as before.
• The runtime services and external integrations report healthy.
• The application logs show no new errors or unexpected deprecation warnings after startup.
The version-specific migration section notes any additional checks that apply to a particular
release.
Automatic Source Code Refactoring
7

-- 7 of 42 --

Motivation
A12 upgrades take a significant amount of time and project resources. It results in projects
struggling with upgrades and being afraid of performing them at all.
We recommend projects to upgrade regularly. Therefore, we strive to prepare for upgrades through
understanding breaking changes, upgrading our own projects, reviewing existing documentation,
and writing new documentation and migration guides. To reduce the efforts for projects upgrading,
we evaluated options to save time on code level. It is possible to apply a subset of breaking changes
automatically through the usage of source code refactoring tools.
After research and evaluation we have decided to use OpenRewrite for backend components and
Hypermod (formerly known as: Codemods/Codeshift) for frontend components.
Advantages of Automated Source Code Refactoring
OpenRewrite and Hypermod apply the same transformation rules across your entire codebase in a
single run. This brings a few practical advantages over doing the work by hand:
• Less manual effort: Renaming imports, updating method signatures, and adjusting call sites
across hundreds of files is tedious work that these tools handle in seconds.
• Fewer mistakes: A find-and-replace session across a large project almost always misses an edge
case or introduces a typo. Automated rules do not get tired or overlook files.
• Uniform results: Every module in your project ends up following the same updated patterns,
rather than depending on which developer touched which file.
• Easier reviews: Because the transformations are deterministic, reviewers can focus on the
handful of manual changes instead of sifting through thousands of mechanical edits.
How to Apply It in A12?
Since A12 2024.06 component teams try to cover every suitable breaking change by a
transformation rule and provide these out-of-the-box. The collection of those rules can be
downloaded by a developer and executed by running a single command, which will apply these
changes to the codebase automatically.
These changes could be,
• import paths changed for library specific classes and functions;
• class and method names changed;
• method signatures changed; or
• the way how to use a class or method has changed.
You can run OpenRewrite recipes for backend components and codemod rules for frontend
components, one component at a time.
8

-- 8 of 42 --

Usage of OpenRewrite for Backend Components
OpenRewrite is a library which covers functionalities to refactor large code bases and various file
types. Its focus was on Java code refactoring, but also enables e.g. Maven and YAML automatic code
transformation by writing so-called recipes.
A12 components provide recipes, which contain rules for automatic code refactoring of suitable
breaking changes. These recipes are bundled into artifacts and released. Projects, who are
performing an A12 overall version upgrade then need to use these new artifacts in their
build.gradle (or for Maven: pom.xml) and activate the provided recipes.
NOTE
OpenRewrite support for Kotlin is still in "Work-in-progress" status. Changes related
to import paths, types and properties are applied. The changes related to methods
are not applied for now. See github.com/openrewrite/rewrite-kotlin for more
information.
Add OpenRewrite to Build Files
When upgrading to a newer A12 major release, extend your build.gradle or pom.xml as follows.
build.gradle
//...
plugins {
id("org.openrewrite.rewrite") version("8.71.0")
}
dependencies {
//...
// Change the recipe artifact versions to the values from the release table for
2026.06
rewrite("com.mgmtp.a12.dataservices:dataservices-rewrite:39.0.2")
rewrite("com.mgmtp.a12.formengine:formengine-rewrite:39.0.0")
// Optional: recipe modules for the Spring Boot 4 ecosystem and Jackson 3
rewrite("org.openrewrite.recipe:rewrite-jackson:1.26.0")
rewrite("io.moderne.recipe:rewrite-spring:0.37.0")
}
rewrite {
activeRecipe(
// A12 component recipes (names follow the artifacts in the dependencies block
above)
'com.mgmtp.a12.dataservices.rewrite.upgradeTo39_0_0',
'com.mgmtp.a12.formengine.UpgradeFormEngine_39',
// Optional: recipes for the Spring Boot 4 ecosystem and Jackson 3
'org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0',
'org.openrewrite.java.jackson.UpgradeJackson_2_3',
)
9

-- 9 of 42 --

}
pom.xml
<project>
<dependencies>
<!-- change this to the dependency that need to be migrated -->
<dependency>
<groupId>com.mgmtp.a12.dataservices</groupId>
<artifactId>dataservices-rewrite</artifactId>
<version>39.0.2</version>
</dependency>
</dependencies>
<build>
<plugins>
<plugin>
<groupId>org.openrewrite.maven</groupId>
<artifactId>rewrite-maven-plugin</artifactId>
<version>6.21.0</version>
<configuration>
<activeRecipes>
<!-- Change this to the recipe name from dependency -->
<recipe>com.mgmtp.a12.dataservices.rewrite.upgradeTo39_0_0</recipe>
</activeRecipes>
</configuration>
</plugin>
</plugins>
</build>
</project>
The recipe artifacts and their active recipe names for 2026.06 are:
Component Recipe Artifact Active Recipe
Data Services com.mgmtp.a12.dataservices:datase
rvices-rewrite
com.mgmtp.a12.dataservices.rewrit
e.upgradeTo39_0_0
Form Engine com.mgmtp.a12.formengine:formengi
ne-rewrite
com.mgmtp.a12.formengine.UpgradeF
ormEngine_39
Spring Boot 4 ecosystem
(community)
io.moderne.recipe:rewrite-spring org.openrewrite.java.spring.boot4
.UpgradeSpringBoot_4_0
Jackson 3 (community) org.openrewrite.recipe:rewrite-
jackson
org.openrewrite.java.jackson.Upgr
adeJackson_2_3
NOTE
The recipe list above covers every component that ships an OpenRewrite recipe for
2026.06, not only the components highlighted in the Migration to 2026.06 section.
For the exact changes a given recipe applies, see that component’s Migration
Instructions in the Links section.
10

-- 10 of 42 --

Execute OpenRewrite
Gradle
• gradle rewriteDiscover - Lists all available recipes and their visitors.
• gradle rewriteDryRun - Runs the active refactoring recipes, producing a patch file. No source
files will be changed.
• gradle rewriteRun - Runs the configured recipes and apply the changes locally.
Maven
• mvn rewrite:run - Runs the configured recipes and apply the changes locally.
• mvn rewrite:runNoFork - Runs the configured recipes and applies the changes locally. This
variant does not fork the Maven life cycle and can be a more efficient choice when using
Rewrite within a CI workflow when combined with other Maven goals.
• mvn rewrite:dryRun - Generates warnings to the console for any recipe that would make changes
and generates a diff file in each maven modules' target folder.
• mvn rewrite:discover - Generates a report of available recipes found on the classpath.
Examples
NOTE This example uses the A12 Project Template as a codebase.
gradle :server:app:rewriteDryRun:
Creates the OpenRewrite patch file under full-stack-project-
template/server/app/build/reports/.rewrite/rewrite.patch. Changes can be applied by using the
patch file and the files in server/app are adapted accordingly.
gradle :server:app:rewriteRun:
Directly applies changes. Dependencies are not upgraded to the respective 2026.06 versions,
therefore these have to be updated manually to make the refactored code compilable.
Tips
• Use rewriteDryRun first.
• After you execute rewrite rules, you can remove OpenRewrite dependencies and usage from
your build file.
For more information check out OpenRewrite configuration options.
Usage of Hypermod for Frontend Components
Hypermod is a library which provides functionalities to refactor large codebases. Its focus is on
writing so-called codemods for JavaScript and TypeScript refactoring. Generally, Hypermod can be
used for any file type because of its more abstract structure.
A12 components provide codemods, which contain rules for automatic code refactoring of suitable
11

-- 11 of 42 --

breaking changes. These codemods are bundled into artifacts and released.
Execute Codemods
For Kernel and Workflows components, the structure for installing a codemod artifact and
executing it is as follows:
npx '@com.mgmtp.a12.<component>/<component>-codemod@<component-version>' <path-to-
your-frontend-sources> --target-version <component-version>
For other components, the general structure for installing a codemod artifact and executing it is as
follows:
npx '@com.mgmtp.a12.<component>/<component>-codemod@<component-version>' <component-
version> <path-to-your-frontend-tsconfig>
Examples
The two structures above differ only in how the version and target are passed. The following two
examples are representative. Apply the same pattern to every other component codemod, using the
version from the table below.
Run kernel-codemod (Kernel and Workflows style)
npx '@com.mgmtp.a12.kernel/kernel-codemod@31.1.0' client --target-version 31
Run client-codemod (all other components style)
npx '@com.mgmtp.a12.client/client-codemod@17.0.0' 17.0.0 client\tsconfig.json
The following table lists every frontend codemod for 2026.06. Components marked Kernel and
Workflows are run like the kernel-codemod example; all others follow the client-codemod example.
Component npm Package Version Style
Kernel @com.mgmtp.a12.kernel/kernel-codemod 31.1.0 Kernel and
Workflows
Workflows @com.mgmtp.a12.workflows/workflows-
codemod
13.0.0 Kernel and
Workflows
Base @com.mgmtp.a12.base/base-codemod 30.0.1 Other
Client @com.mgmtp.a12.client/client-codemod 17.0.0 Other
Data Services @com.mgmtp.a12.dataservices/dataservices-
codemod
39.0.2 Other
Diagram Editor @com.mgmtp.a12.diagrameditor/diagramedito
r-codemod
4.0.0 Other
12

-- 12 of 42 --

Component npm Package Version Style
Form Engine @com.mgmtp.a12.formengine/formengine-
codemod
39.0.0 Other
Overview Engine @com.mgmtp.a12.overviewengine/overvieweng
ine-codemod
39.0.1 Other
Tree Engine @com.mgmtp.a12.treeengine/treeengine-
codemod
11.0.0 Other
Print Engine @com.mgmtp.a12.print/print-engine-codemod 4.0.0 Other
User Authentication
and Authorization
@com.mgmtp.a12.uaa/uaa-authentication-
client-codemod
10.0.0 Other
Notification Center @com.mgmtp.a12.notificationcenter/notific
ationcenter-codemod
4.0.0 Other
TIP The order in which the codemods are executed matters. Kernel and Workflows shall
be run first. Then the client-codemod will apply the most amount of breaking changes.
Migration to 2026.06
CAUTION
This section is based on the experiences of upgrading TPS projects (Project
Template, Project Information, Load Test Apps) as well as on an analysis of
breaking changes. For details you need to go to the migration instructions of all
components, which you use in your project.
For the automatic source code refactoring tools used in 2026.06 (OpenRewrite for the backend,
including the Spring Boot 4 ecosystem upgrade, and Hypermod for the frontend), see the Automatic
Source Code Refactoring chapter.
Breaking Changes
Every change below uses the same structure so you can scan it quickly and, if it applies to your
project, act on it:
• Description and Explanation: what changed and why. Read the Explanation only if you want
the background.
• Migration Path: the concrete steps to apply the change.
• Examples and Detector: before/after snippets and a ready-to-run command to find the affected
files in your project.
• Automation: which codemod or OpenRewrite recipe, if any, does part of the work for you.
• Effort: a rough size estimate (XS to XL) to help you plan the change. It is planning guidance
based on TPS experience, not a guarantee for your project.
• Risks: pitfalls to watch out for, each with a suggested mitigation.
If you only need to act, read the Migration Path, run the Detector, and check the Risks.
13

-- 13 of 42 --

A12 upgrades to the Spring Boot 4 ecosystem (Spring Framework 7, Spring
Security 7, Hibernate 7, Jackson 3)
Description
The A12 server-side BOMs now bring Spring Boot 4.0.6, Spring Framework 7.0.7, Spring Security
7.0.5, Spring Data 4.0.5, Hibernate ORM 7.2.1.Final, and Jackson 3.1.3. Consumer projects inherit
these versions transitively through the A12 BOMs and must align their own code with the new APIs.
The most impactful surfaces are:
• Jackson packages move from com.fasterxml.jackson.* to tools.jackson.*, and several APIs are
renamed (for example SerializerProvider becomes SerializationContext, JsonParser.getText()
becomes getString(), and JsonParseException becomes StreamReadException).
• Spring Boot 4 relocates SpringImplicitNamingStrategy from
org.springframework.boot.orm.jpa.hibernate to org.springframework.boot.hibernate.
• The build now requires Gradle 8.14 or newer.
Note that jackson-annotations keeps the com.fasterxml.jackson.annotation namespace; only the
core, databind, and datatype packages move.
Explanation
Spring Boot 3.x and Spring Framework 6.x are approaching the end of their open-source support
window. A12 moves to the Spring Boot 4 ecosystem to stay on actively supported, secure framework
versions and to keep its servers building against current APIs. Because these dependencies are
shared across A12 components (Base, Kernel, UAA, Data Services, Workflows, Data Distribution)
through the A12 BOMs, the upgrade is rolled out in a single coordinated step rather than per
component.
Migration Path
Raise your build to Gradle 8.14 or newer first, since Spring Boot 4 requires it. Add the OpenRewrite
plugin (rewriteVersion 8.71.0) to your build and run ./gradlew rewriteRun.
TIP
UpgradeSpringBoot_4_0 and UpgradeJackson_2_3 are not provided by A12.
UpgradeJackson_2_3 ships in org.openrewrite.recipe:rewrite-jackson, and
UpgradeSpringBoot_4_0 ships in io.moderne.recipe:rewrite-spring. For what each recipe
covers, see the UpgradeSpringBoot_4_0 and UpgradeJackson_2_3 recipe catalog
entries.
After the recipes complete, do the manual follow-up the codemod cannot cover:
• Update any custom Jackson serializers, deserializers, and RPC payload handlers to the renamed
tools.jackson.* APIs.
• If you override the JPA implicit naming strategy, change the configured class name to the new
org.springframework.boot.hibernate package.
• Review the remaining Hibernate ORM 7 mapping and DDL changes, since codemod coverage for
14

-- 14 of 42 --

Hibernate 7 is incomplete.
• Run ./gradlew revapiCheck codeCheck spotlessApply build and inspect any spring-boot-
properties-migrator warnings to find application properties that need adjusting.
Because the upgrade is shared across components, see the per-component migration instructions of
Base, Kernel, User Authentication and Authorization, Data Services, and Workflows.
Examples
Before
# Shared dependency baseline (before)
springBootVersion = 3.5.9
springVersion = 6.2.15
springSecurityVersion = 6.5.7
springDataVersion = 3.5.7
hibernateVersion = 6.6.4.Final
jacksonVersion = 2.20.1
# JPA naming strategy
spring.datasources.contentstore.jpa.properties.hibernate.implicit_naming_strategy=org.
springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
After
# Shared dependency baseline (after)
springBootVersion = 4.0.6
springVersion = 7.0.7
springSecurityVersion = 7.0.5
springDataVersion = 4.0.5
hibernateVersion = 7.2.1.Final
jacksonVersion = 3.1.3
# JPA naming strategy (relocated package)
spring.datasources.contentstore.jpa.properties.hibernate.implicit_naming_strategy=org.
springframework.boot.hibernate.SpringImplicitNamingStrategy
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
15

-- 15 of 42 --

Detector
Run this in your project to find affected files:
grep -rE --exclude-dir={node_modules,build,dist,.git,.gradle,target}
'(com\.fasterxml\.jackson\.(core|databind|datatype)|org\.springframework\.boot\.orm\.j
pa\.hibernate)' .
Automation
Parts of this migration can optionally be handled by the OpenRewrite recipes UpgradeSpringBoot_4_0
and UpgradeJackson_2_3 (not provided by A12); the rest is manual.
Run it with:
plugins {
id("org.openrewrite.rewrite") version "8.71.0"
}
dependencies {
// Optional: not provided by A12
rewrite("org.openrewrite.recipe:rewrite-jackson:1.26.0")
rewrite("io.moderne.recipe:rewrite-spring:0.37.0")
}
rewrite {
activeRecipe(
"org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0",
"org.openrewrite.java.jackson.UpgradeJackson_2_3"
)
}
# then run:
./gradlew rewriteRun
Effort
Automation: semi-automated
Estimated effort: S-L
Notes: Most of the work is bumping Gradle, running the two OpenRewrite recipes, and rebuilding,
which is close to the low end for a project with little custom Jackson or Hibernate code. Manual
effort scales with the number of classes that use Jackson serialization APIs directly, with custom JPA
naming strategies, and with any Hibernate ORM 7 mapping changes the recipes cannot rewrite
automatically. Heavy users of these APIs sit at the upper end of the range.
Risks
Warning: Jackson 3 moves the core, databind, and datatype packages to tools.jackson and changes
some default serialization behavior, so existing JSON contracts and custom (de)serializers can
break silently. Mitigation: run the integration tests that exercise your JSON payloads and review
every custom serializer and deserializer before release.
16

-- 16 of 42 --

Warning: Hibernate 7 can change generated DDL and schema-mapping defaults, so an automatic
schema update may alter the database. Mitigation: review the generated DDL, test the migration
against a copy of production data, and never run auto-DDL against production.
A12 frontend toolchain upgrade: TypeScript 6 (ES2025), React 19.2.6, and
Redux 5
Description
A12 advances three core frontend dependencies together. The supported TypeScript version moves
from 5.8.x to 6.0.x and the tsconfig target and lib move from ES2024 to ES2025, so the JavaScript
emitted by A12 npm artifacts can now contain ES2025 syntax. The react and react-dom peer
dependency moves from 19.0.0 to 19.2.6. redux, react-redux, and typed-redux-saga are aligned to
single versions (redux 5, react-redux 9), and support for older redux and react-redux versions is
dropped. In middlewares and sagas the action parameter is now typed as unknown instead of relying
on the previous Redux 4 defaults.
Explanation
Keeping TypeScript, React, and Redux current lets A12 use newer language and library features
such as ES2025 APIs and the Redux 5 end-to-end type-safety model that extends across state,
dispatch, and selector types. Redux 5 also requires Redux Toolkit, which the latest tooling builds on.
Aligning every A12 component on a single redux, react-redux, and typed-redux-saga version
removes the peer-dependency warnings caused by components depending on divergent redux
versions. Staying close to the latest TypeScript follows the TypeScript team’s own guidance that
consumers keep their toolchain up to date.
Migration Path
Update the react and react-dom peer dependency to 19.2.6. For Redux 5, run the
@com.mgmtp.a12.client/client-codemod package against your tsconfig.json: its
migrateTypescriptFsaImports recipe rewrites typescript-fsa imports to the A12 fork
@com.mgmtp.a12.client/typescript-fsa-redux-5-compat, and its migrateAnyActionType recipe replaces
AnyAction (imported from typescript-fsa or redux) with Action from redux. Then declare your own
State and Dispatch types instead of relying on the restored Redux 4 defaults. In middlewares and
sagas the action is now typed unknown; the common first call actionCreator.match(action) keeps
working, but any other action usage needs a manual isAction() type guard. Finally, align redux,
react-redux, and typed-redux-saga to the A12 versions defined in the Common Dependencies
Definition.
For the per-component details, see the Redux 5, React 19.2, and TypeScript migration instructions of
Diagram Editor, User Authentication and Authorization, Data Services, Overview Engine,
Relationship Engine, CRUD, Content Engine, Content Management System, and Expression.
Examples
Before
TypeScript 5.8.x
17

-- 17 of 42 --

tsconfig target / lib ES2024
react / react-dom (peer) 19.0.0
redux / react-redux mixed, pre-5 / pre-9
After
TypeScript 6.0.x
tsconfig target / lib ES2025
react / react-dom (peer) 19.2.6
redux 5.x
react-redux 9.x
typed-redux-saga aligned to the A12 version
Detector
npm ls typescript react react-dom redux react-redux typed-redux-saga
Run this in your project to list the currently resolved versions and confirm whether any sit below
the new baselines (TypeScript 6, react 19.2.6, redux 5, react-redux 9).
Automation
Parts of this migration can be handled by the @com.mgmtp.a12.client/client-codemod package; the
rest is manual.
Run every recipe that applies to the 17.0.0 target version with:
npx '@com.mgmtp.a12.client/client-codemod' 17.0.0 <path-to-your-frontend-tsconfig>
Or run a single recipe by its ID instead of a target version:
npx '@com.mgmtp.a12.client/client-codemod' migrateTypescriptFsaImports <path-to-your-
frontend-tsconfig>
npx '@com.mgmtp.a12.client/client-codemod' migrateAnyActionType <path-to-your-
frontend-tsconfig>
Effort
Automation: semi-automated
Estimated effort: S-M
Notes: TypeScript and React are usually low effort: most projects already run a TypeScript close to
6.0, and the react change is a peer-dependency bump. The Redux 5 alignment drives the spread.
Running the import codemod is quick, but declaring your own State and Dispatch types and adding
isAction() guards where the codemod cannot help scales with how much custom middleware and
saga code the project contains. The A12 effort estimates put the Client and Workflows changes at
18

-- 18 of 42 --

roughly five developer-days each, so heavy consumers should plan toward the upper end of this
range.
Risks
Warning: A12 artifacts now emit ES2025 JavaScript instead of ES2024, which can break execution
on older browsers that lack ES2025 features. Mitigation: if you must support older browsers, ensure
your build down-levels the output or includes the appropriate polyfills.
Caution: After the Redux 5 migration the action type in middlewares and sagas is unknown, so only
actionCreator.match(action) keeps working automatically and other action usages compile-fail until
fixed. Mitigation: provide your own State and Dispatch types and add isAction() type guards where
the codemod cannot rewrite the usage.
Data Services Public API Changes
Description
The 2026.06 release removes, renames, and re-signs a broad set of Data Services public Java and
TypeScript APIs. The most impactful changes are:
• Model bulk import classes removed: ModelBulkImporter, BulkImportProblemReporter,
ModelBulkImportException, and ModelBulkImportExceptionMapper.
• Model import client methods renamed: ModelsClient.importModelBulk(InputStream) and
RestModelsClient.importModelBulk(InputStream) become importRuntimeModels(InputStream).
• Document classes removed: Document, DocumentDetail, and DocumentGraphService are replaced by
DocumentSpec and the Query API documentGraph projection.
• Validation class merged:
com.mgmtp.a12.dataservices.document.operation.validate.DocumentValidationError is replaced
by com.mgmtp.a12.dataservices.document.DocumentValidationResult.
• Query API: Query.Order renamed to Query.DirectFieldOrder; FilterSpec removed in favor of
QueryRoot; ExactMatchOperator.lang removed.
• @Transactional removed from the ModelService, IModelRepository, and RelationshipModelLoader
interface declarations, which moved to the new dataservices-modelgraph-api module.
• New mandatory interface methods on ModelService (findAllHeaders()) and ModelTypeService
(findRootModelName, findModelNameAndAllSubtypes, isSubtype).
• Relationship model classes removed: RelationshipMigration and the Java/TypeScript
CandidateConstraints are migrated via the dataservices-relationship-model-migration npm tool.
• TypeScript dataservices-access: deep import paths removed in favor of root-level imports;
LinkEntitySpec.docRef is now mandatory; requires Redux ^5 and TypeScript ^6.0.2.
• Content Store artifacts move from the com.mgmtp.a12.dataservices group to
com.mgmtp.a12.dataservices.contentstore.
19

-- 19 of 42 --

Explanation
Data Services consolidated its public API surface as part of the 2026.06 major release to remove
long-standing duplicate and legacy types, align model import and document access on the
DocumentSpec and Query API abstractions, and decouple the model interfaces from Spring by
relocating them to the new dataservices-modelgraph-api module. Many parameters that previously
had undefined behavior on null are now annotated @NonNull, making contract violations fail fast
instead of producing silent or inconsistent results.
Migration Path
Recompile your project against the 2026.06 Data Services artifacts and resolve the compilation
errors group by group.
• Replace removed bulk-import types: drop all usages of ModelBulkImporter.doImport,
BulkImportProblemReporter, and the related exception and resolver classes, and route models
through the Workspace Converter Framework pipeline before import (see Data Services No
Longer Expands Models at Runtime).
• Rename the model import client calls from importModelBulk(InputStream) to
importRuntimeModels(InputStream) (behavior is unchanged but only runtime models should be
imported, see Data Services No Longer Expands Models at Runtime).
• Replace Document, DocumentDetail, and DocumentGraphService with DocumentSpec and the Query API
documentGraph projection, and replace DocumentValidationError with DocumentValidationResult.
• Update query code: replace Query.Order with Query.DirectFieldOrder, replace FilterSpec with
QueryRoot, and remove ExactMatchOperator.lang usages.
• If you implement ModelService, IModelRepository, or RelationshipModelLoader and rely on
transactional behavior, add @Transactional to your own implementation methods, and add
implementations for the new mandatory ModelService and ModelTypeService methods.
• Run the dataservices-relationship-model-migration npm tool on your TypeScript and frontend
relationship model files to migrate CandidateConstraints and related removed types.
• In TypeScript projects, replace deep dataservices-access import paths with root-level imports,
provide a docRef for every LinkEntitySpec, and upgrade Redux to ^5 and TypeScript to ^6.0.2.
• Update Content Store dependency coordinates to the new
com.mgmtp.a12.dataservices.contentstore group.
• Review call sites of newly @NonNull-annotated constructors and methods (for example
AsterixAnonymizer, ModelGraphGenerator) to ensure no null values are passed;
ModelGraphGenerator now uses a 2-parameter constructor.
For the full component-level details, see the Data Services > Migration Instructions.
Examples
Before
// Java: removed/renamed APIs
modelsClient.importModelBulk(inputStream);
20

-- 20 of 42 --

DocumentDetail detail = documentService.getDetail(docRef);
DocumentValidationError error = result.getError();
Query.Order order = new Query.Order("/Contract/Name", ASC);
// TypeScript: deep imports, optional docRef
import { DocumentSpec } from "@com.mgmtp.a12.dataservices/dataservices-
access/lib/document";
const link: LinkEntitySpec = { /* docRef optional */ };
After
// Java: replacement APIs
modelsClient.importRuntimeModels(inputStream);
DocumentSpec spec = queryService.query(/* documentGraph projection */);
DocumentValidationResult result = operation.validate(...);
Query.DirectFieldOrder order = new Query.DirectFieldOrder("/Contract/Name", ASC);
// TypeScript: root-level imports, mandatory docRef
import { DocumentSpec } from "@com.mgmtp.a12.dataservices/dataservices-access";
const link: LinkEntitySpec = { docRef: "Contract/7005c91f" };
Detector
Run this in your project to find affected files:
grep -rE --exclude-dir={node_modules,build,dist,.git,.gradle,target}
'com\.mgmtp\.a12\.dataservices\.(model\.bulkload\.(ModelBulkImporter|BulkImportProblem
Reporter|ModelBulkImportException)|document\.(Document|DocumentDetail|DocumentGraphSer
vice|operation\.validate\.DocumentValidationError)|rpc\.query\.FilterSpec)|importModel
Bulk|Query\.Order([^[:alnum:]_]|$)' .
Automation
Two tools cover parts of this change; the rest is manual.
• The dataservices-rewrite OpenRewrite recipe
(com.mgmtp.a12.dataservices.rewrite.upgradeTo39_0_0) automates the mechanical Java package,
import, and type renames. Add it to your build and run it as shown in the Automatic Source
Code Refactoring chapter.
• The dataservices-relationship-model-migration npm tool migrates the relationship model
files (CandidateConstraints and the related removed types), as well as the SME.
Run the npm tool with:
21

-- 21 of 42 --

npx @com.mgmtp.a12.dataservices/dataservices-relationship-model-migration <path-to-
model-files>
Adopting the DocumentSpec and Query API replacements, implementing the new mandatory
interface methods, reviewing the newly @NonNull call sites, and the TypeScript dataservices-access
changes remain manual.
Effort
Automation: semi-automated
Estimated effort: M-L
Notes: Light users of Data Services that only consume documents and queries sit at the lower end
(about one to two days). Projects with custom ModelService/ModelTypeService implementations, bulk
model import code, custom relationship models, and a TypeScript frontend on dataservices-access
sit at the upper end (about a week). The dataservices-rewrite OpenRewrite recipe automates the
mechanical Java package, import, and type renames, and the dataservices-relationship-model-
migration npm tool automates the relationship model migration. The remaining Java and
TypeScript source changes (the DocumentSpec/Query API adoption, the new mandatory interface
methods, and the dataservices-access import changes) are manual but mostly mechanical, driven
by compiler errors.
Risks
Caution: This is a wide compile-breaking change set: a project that uses several of the affected APIs
will not build against 2026.06 until every group above is addressed. Plan the upgrade with a full
clean build rather than incremental edits.
Caution: Query field paths must now begin with a leading forward slash (for example
/ContractRoot/ChangeLog/Changes/Status). Paths without a leading slash now raise a
QueryInvalidInputException (HTTP 400) instead of silently returning an empty result, so previously
tolerated queries will start failing.
Data Services No Longer Expands Models at Runtime
Description
With the 2026.06 release, the Document Model expansion and meta-data group addition is moved
from Data Services into dedicated artifacts called Workspace Converters. A Workspace Converter
Framework is provided and can be integrated into build pipelines and model preparation at
runtime. More information about the A12 Workspace and the Converters can be found here.
• Workspace Converters resolve Document Model dependencies (TypeDefinitions, Includes,
Additive Document Model) and add __meta-Groups, so the resulting model is the runtime
format
• The runtime format of the models can be used for importRuntimeModels(InputStream)
• The Workspace Converter Framework can be used as a CLI tool in build pipelines
22

-- 22 of 42 --

Explanation
The expansion of Document Models, Combination and Transformer Models with TypeDefinition
import, Include resolution, Addition, Selection and Decoration steps, are now available as a
dedicated artifact. Projects can provide their custom model adoptions as a Converter and the
Workspace Converter Framework will pick it up. In this way, a comprehensive pipeline can be built
that leads to the models in a fully prepared state as they will be at runtime. There are no further
adoptions to those models anymore in Data Services at runtime. This enables projects to use these
models to generate Typed Accessor Classes or use them in feature tests.
Migration Path
Recompile your project against the 2026.06 Data Services artifacts and resolve the compilation
errors group by group.
• All Document, Combination and Transformer Models must be passed through the Workspace
Converter Framework pipeline before import.
• Replace bulk-import: drop all usages of ModelBulkImporter.doImport, BulkImportProblemReporter,
and the related exception and resolver classes.
• Rename the model import client calls from importModelBulk(InputStream) to
importRuntimeModels(InputStream) and a call of the Workspaces Converters.
For the full component-level details, see the Data Services > Migration Instructions.
Examples
Before
You imported your models directly into Data Services. Data Services then expanded those models: it
imported the referenced TypeDefinitions, resolved Includes, applied Additions, Selection, and
Decoration, and added the meta-data groups. The conversion happened inside Data Services on
every import, and the fully expanded models existed only at runtime.
After
You pass your models through the Workspace Converter Framework first, which performs the same
expansion and meta-data group addition and produces the runtime format ahead of time. Data
Services no longer adopts models at runtime; it expects them to already be in the runtime format
and imports them as-is. Because the models are now available in their runtime format, you
generate Typed Accessor Classes that mirror the runtime state of the models and thus the
documents in your application. You can also use them to develop feature tests.
One of the options to automate the conversion is the approach that Project Template uses. In this
approach the runtime models are created as build artifacts and are not under version control.
1. Add the dataservices-wcf-cli to the dependencies of your build tool, example in
settings.gradle for Gradle project:
a12Libs {
23

-- 23 of 42 --

...
version('wcf', '1.1.1')
...
library('dataservices-wcf-cli', 'com.mgmtp.a12.dataservices.wcf',
'dataservices-wcf-cli').versionRef('wcf')
2. Add the related task to convert your models from a desired directory, for example in
build.gradle:
dependencies {
wcfCli a12Libs.dataservices.wcf.cli
wcfCli a12Libs.rmc.conversion
}
def importModelsDir = file('import/models')
def wcfOutputDir = layout.buildDirectory.dir('wcf-output').get().asFile
tasks.register('convertModels', Exec) {
description = 'Convert WCF source models in import/models to RMC in build/wcf-
output/data/models'
group = 'model'
doFirst {
wcfOutputDir.deleteDir()
wcfOutputDir.mkdirs()
def cp = configurations.wcfCli.files.collect { it.absolutePath
}.join(File.pathSeparator)
def convertersJar = configurations.wcfCli.files.find {
it.name.startsWith('conversion-') }
if (convertersJar == null) {
throw new GradleException("Could not find RMC conversion jar in wcfCli
configuration. " +
"Available: ${configurations.wcfCli.files*.name}")
}
commandLine 'java', '-cp', cp, 'com.mgmtp.a12.dataservices.wcf.WcfCli',
importModelsDir.absolutePath, wcfOutputDir.absolutePath,
'-c', convertersJar.absolutePath
}
inputs.dir importModelsDir
outputs.dir file("${wcfOutputDir}/data/models")
}
3. Create hooks with build tasks that need converted models, for example:
tasks.register('importsPackage', Zip) {
group 'Publishing'
description 'Packages importable resources of Project Template to zip.'
dependsOn ':convertModels'
24

-- 24 of 42 --

....
Detector
All projects with Document Models using Data Services need to adapt.
Effort
Automation: manual
Estimated effort: S-M
Notes: If you do not have custom code that handles model imports at runtime, you only need to add
the conversion to your build pipeline. If you do have custom code that handles model imports at
runtime, you must integrate the conversion into that custom handling.
Risks
Caution: Models not passed through the Workspace Converter Framework pipeline before import
can be imported but will fail at runtime.
Caution: If the models are not accessible during development because they are provided
dynamically at runtime, it is advised to store a copy of the source models. It is not guaranteed that
model migration will work on runtime models in the future. If you have any concerns about your
solution, feel free to contact us via Discourse or a support ticket.
Replace Hazelcast With Infinispan
Description
Data Services has replaced the Hazelcast cache provider with Infinispan. Hazelcast is no longer a
dependency of any A12 component, and the Hibernate Second Level Cache is disabled entirely. The
Hazelcast-specific configuration properties (spring.cache.type=hazelcast, spring.hazelcast.config,
spring.hazelcast.instance.name, the hibernate.cache.region.factory_class and
hibernate.cache.hazelcast.instance_name Hibernate properties, and the hz.network.port.port,
hz.clusterName, and hz.instanceName properties) are removed. The hazelcast.xml and hazelcast-
caches.xml configuration files are no longer read.
Explanation
Hazelcast follows a commercial (BSL) license trajectory that conflicts with the A12 permissive-
license requirement of Apache 2.0 or MIT only, and the hazelcast-spring module is permanently
incompatible with Spring Boot 4 with no upstream fix available. Keeping Hazelcast would require
maintaining custom bridge classes indefinitely. Infinispan is Apache 2.0 licensed, integrates
natively with Spring Boot 4 through the embedded starter, and enables cross-pod cache
invalidation that the previous Hazelcast setup did not provide.
Migration Path
Most projects need no action: a project that uses Data Services with the default cache configuration
and no Hazelcast customization migrates transparently. Projects that override hz.* or
25

-- 25 of 42 --

spring.hazelcast.* properties must remove those properties, which is a configuration-only change.
Projects that ship a custom hazelcast.xml, or supply it through the hazelcast.customConfiguration
Helm value, must port their cache map definitions, time-to-live values, and entry limits to an
infinispan.xml. Projects that inject HazelcastInstance directly, for distributed maps, distributed
locks, or a custom HazelcastConfigCustomizer, must reimplement that logic against the Infinispan or
JGroups APIs.
The configuration-only path is as follows:
1. Remove hazelcast.xml, and hazelcast-caches.xml if present, from your application resources.
2. Replace the Hazelcast dependencies in your build.gradle, and any version entries in
gradle/libs.versions.toml, with org.infinispan:infinispan-spring-boot4-starter-
embedded:16.1.2.
3. Copy the sample infinispan.xml from examples-extending-
server/src/main/resources/infinispan.xml into your src/main/resources directory.
4. Add spring.cache.type=infinispan and infinispan.embedded.configXml=infinispan.xml to your
application properties, and remove the Hazelcast-specific properties listed under Description.
5. Remove any Hibernate @Cache and @Cacheable entity annotations from your domain classes,
since the Hibernate Second Level Cache is now disabled.
For multi-pod Kubernetes deployments, expose JGroups port 7800 over TCP on the Data Services
container and create a headless Service so that DNS_PING can discover cluster peers. Single-pod
deployments and local development environments require no JGroups configuration, because
Infinispan forms a cluster of size one without errors. The Hazelcast Helm chart is removed from
the A12 infrastructure Helm chart in the 2026.06 release line.
For the per-component details, see the migration instructions of Data Services and Helm Stack
Charts.
Examples
Before
spring.cache.type=hazelcast
spring.hazelcast.config=classpath:hazelcast.xml
spring.hazelcast.instance.name=A12S
spring.datasources.dataservices.jpa.properties.hibernate.cache.region.factory_class=co
m.hazelcast.hibernate.HazelcastCacheRegionFactory
spring.datasources.dataservices.jpa.properties.hibernate.cache.hazelcast.instance_name
=A12S
hz.network.port.port=5701
hz.clusterName=dev
hz.instanceName=A12S
implementation 'com.hazelcast:hazelcast:5.5.0'
implementation 'com.hazelcast:hazelcast-hibernate53:5.2.0'
26

-- 26 of 42 --

After
spring.cache.type=infinispan
infinispan.embedded.configXml=infinispan.xml
implementation 'org.infinispan:infinispan-spring-boot4-starter-embedded:16.1.2'
For multi-pod deployments, add a JGroups headless Service and expose port 7800:
apiVersion: v1
kind: Service
metadata:
name: dataservices-jgroups
spec:
clusterIP: None
selector:
app: dataservices
ports:
- name: jgroups
port: 7800
protocol: TCP
Detector
Run this in your project to find affected files:
grep -rE --exclude-dir={node_modules,build,dist,.git,.gradle,target}
'spring\.hazelcast|hazelcast\.(xml|instance_name|clusterName)|com\.hazelcast|Hazelcast
CacheRegionFactory|hz\.(network|clusterName|instanceName)' .
Effort
Automation: fully-manual
Estimated effort: XS-S
Notes: Projects on the default cache configuration with no Hazelcast customization migrate
transparently, requiring only a few minutes to confirm and no code change. Configuration-only
projects that override hz.* or spring.hazelcast.* properties sit at the low end, up to half a day.
Projects with a custom hazelcast.xml, a custom JGroups or cluster-discovery setup, or direct
HazelcastInstance injection scale beyond this band and should plan for medium-to-high effort,
because cache map definitions and any distributed-data-structure usage must be reimplemented
against Infinispan and JGroups.
27

-- 27 of 42 --

Expanded Document Models No Longer Keep modelReferences to Modeling-
Time Helper Models
Description
The Kernel expansion mechanism for Document Models and Composed Document Models now
strips entries from header.modelReferences in the expanded (runtime) model. All modeling-time
helper references with "purpose": "include", "purpose": "transitiveInclude", or "purpose":
"typeDefinitions" are resolved during expansion and removed from the runtime model. The
unified expansion now handles regular Document Models and Combination Models alike.
purpose=include and purpose=typeDefinitions are now reserved for the expand mechanism: if such
a reference is present, Kernel expects an expandable model (modelType=document or
modelType=combination) and fails the expansion otherwise.
Explanation
Modeling-time helper models such as Includes, Type Definitions, Additive, Selection, and
Decoration models are only needed while modeling. After expansion the runtime Document Model
is self-contained and already carries all of their information. Keeping the references caused this
content to be loaded multiple times at runtime, which reduced performance, and in the case of
Additive Document Models the referenced models are not even valid standalone Document Models,
which could cause runtime errors. A workaround had previously been applied only in the
Workspace Converter, so the handling of modelReferences was inconsistent across Document
Models. The expansion now removes these helper references consistently for every Document
Model.
Migration Path
For regular Document Models no action is required: the new expansion removes the helper
references automatically. Review any project code that reads header.modelReferences from an
expanded runtime Document Model, whether directly from the model JSON or through the Kernel
API (documentModel.getHeader().getModelReferences()), and no longer relies on purpose=include,
purpose=transitiveInclude, or purpose=typeDefinitions being present. Ensure that references
declared with purpose=include or purpose=typeDefinitions always point to an expandable model
(modelType=document or modelType=combination); otherwise the expansion now fails with an error. For
Composed Document Models the runtime still needs the referenced models, so run the provided
model migration: for every Document Model carrying the cdm.queryRoot annotation, each
header.modelReferences entry with "purpose": "include" is duplicated to a matching entry with
"purpose": "composedData", restoring the previous runtime behavior. Going forward the Simple
Model Editor writes the composedData references on save, so projects can later optimize by removing
unneeded references manually.
For the full component-level details, see the Kernel > Migration Instructions.
Examples
Before
{
28

-- 28 of 42 --

"header": {
"modelId": "contact",
"modelReferences": [
{ "reference": "address.dm.json", "purpose": "include" },
{ "reference": "common-types.dm.json", "purpose": "typeDefinitions" }
]
}
}
After
{
"header": {
"modelId": "contact",
"modelReferences": []
}
}
Detector
Run this in your project to find affected files:
grep -rE --exclude-dir={node_modules,build,dist,.git,.gradle,target}
'"purpose"[[:space:]]*:[[:space:]]*"(include|transitiveInclude|typeDefinitions)"' .
NOTE
A match alone does not mean action is required. Regular Document Models migrate
automatically through the new expansion, so most matches need nothing. Act only
for Composed Document Models (those carrying the cdm.queryRoot annotation) and
for project code that reads header.modelReferences from an expanded runtime
model.
Automation
A codemod is available: Document Model Migration Tool (Kernel).
Run it with:
java -jar a12-model-migration-cli.jar --input <model-dir> --output <model-dir>
--target-dm-version <latest>
Effort
Automation: automated-via-codemod
Estimated effort: S-M
Notes: Regular Document Models migrate automatically through the expansion and require no
manual work. Composed Document Models need the provided model migration run once over the
29

-- 29 of 42 --

model directory. Manual effort scales with how much project code inspects header.modelReferences
of runtime models and with the number of references that incorrectly use the reserved purposes.
Risks
Caution: Project code that reads header.modelReferences from an expanded runtime Document
Model, either from the model JSON or via the Kernel API
(documentModel.getHeader().getModelReferences()), and expects purpose=include,
purpose=transitiveInclude, or purpose=typeDefinitions entries to be present will no longer find
them and must be adapted.
Caution: If a purpose=include or purpose=typeDefinitions reference points to a non-expandable or
corrupt model, expansion now fails with an error instead of silently keeping the reference.
Removal of widgetMap Props for Widget Customization
Description
The component-level widgetMap property has been removed from all widget prop typings. It
previously allowed projects to override individual sub-widgets directly on a component instance.
The property existed on the layout components (ApplicationFrameLayout and MasterDetail), the
Region component (for customizing the ModalOverlay), and the LocaleSelect component (for
customizing its inner Select). Passing widgetMap to any of these components is now a type error and
has no effect at runtime.
Explanation
A unified ClientWidgetMap API for customizing widgets was introduced, making the per-component
widgetMap property obsolete. The property was deprecated in 2025.06-ext2 and is now removed. The
ClientWidgetMap approach centralizes widget overrides in one place and follows the same mental
model that projects already know from other A12 engines, so maintaining two parallel
customization mechanisms was no longer justified.
Migration Path
Replace each component-level widgetMap prop with an entry in the application’s central
ClientWidgetMap. Identify the widgets you previously overrode (for example the ApplicationFrame,
the MasterDetail, the ModalOverlay of a Region, or the inner Select of a LocaleSelect) and register
your custom implementations in the ClientWidgetMap instead. The override behavior is unchanged,
so your existing custom components can be reused without modification. This API only existed
since 2025.06, so the change typically affects projects that requested one of these specific
customizations in the first place. For the detailed override mechanism, see the deprecation notice in
the Client documentation that introduced the replacement.
For the full component-level details, see the Widgets > Migration Instructions.
Examples
30

-- 30 of 42 --

Before
function CustomAppFrameLayout(props: FrameViews.ApplicationFrameLayoutProps):
JSX.Element {
return (
<FrameViews.ApplicationFrameLayout
{...props}
widgetMap={{ applicationFrame: CustomAppFrameComponentWithResizing }}
/>
);
}
After
// Register the override once in the central ClientWidgetMap instead of
// passing widgetMap on the component.
const clientWidgetMap: ClientWidgetMap = {
applicationFrame: CustomAppFrameComponentWithResizing
};
function CustomAppFrameLayout(props: FrameViews.ApplicationFrameLayoutProps):
JSX.Element {
return <FrameViews.ApplicationFrameLayout {...props} />;
}
Detector
Run this in your project to find affected files:
grep -rE --exclude-dir={node_modules,build,dist,.git,.gradle,target}
'widgetMap[[:space:]]*=\{' .
Effort
Automation: fully-manual
Estimated effort: S
Notes: Effort scales with the number of customized components. Most projects override at most
one or two of the affected widgets, so the change is usually a quick, mechanical relocation of
existing override components into the ClientWidgetMap. Projects that never used widgetMap are not
affected.
Risks
Caution: The removal is irreversible and the failure is silent at runtime. A widgetMap prop that is
left in place no longer applies the override, so a customized widget quietly falls back to its default
rendering. Run the detector across the project and migrate every match into the ClientWidgetMap
before upgrading, then verify each previously customized component renders as expected after the
31

-- 31 of 42 --

change.
Rich Text Editor Requires the Latest Lexical Packages
Description
The Rich Text Editor (@com.mgmtp.a12.widgets/widgets-core/lib/rich-text-editor) is built on the
Lexical framework, which it consumes as a peer dependency. Lexical and the related packages
(lexical, @lexical/react, @lexical/link) have been updated to their latest version. Consumer
projects install these packages themselves, so the editor now expects the updated Lexical version to
be present.
Explanation
Lexical evolves quickly, and the editor was pinned to an older release. Adopting the latest Lexical
brings performance improvements, bug fixes, and the upstream change tracked in Lexical pull
request 8127, which unblocks a related Rich Text Editor defect. Keeping the peer dependency
current also lets the editor rely on APIs that are only available in recent Lexical versions.
Migration Path
Update the Lexical packages in your project to the latest version so they match what the Rich Text
Editor now expects. Bump lexical, @lexical/react, and @lexical/link (and any other @lexical/*
packages you use) in your package.json, then reinstall. After updating, rebuild your client and
smoke-test every screen that embeds a Rich Text Editor or one of its plugins (static toolbar, mention
plugin, link plugin) to confirm rendering, editing, and serialization still behave as expected.
For the full component-level details, see the Widgets > Migration Instructions.
Examples
Before
The project depends on an older, pinned Lexical version.
"lexical": "0.20.0"
After
npm i lexical@latest @lexical/react@latest @lexical/link@latest
Detector
Run this in your project to find affected files:
grep -rE --exclude-dir={node_modules,build,dist,.git,.gradle,target}
'"lexical"[[:space:]]*:' .
32

-- 32 of 42 --

Effort
Automation: fully-manual
Estimated effort: S
Notes: Casual users with a single Rich Text Editor instance and no custom plugins sit at the low end.
Projects with custom Lexical nodes, custom plugins, or heavy editor usage should budget extra time
to retest those integrations against the new Lexical APIs.
Print Engine: Embedded Image Attachments Replaced by Workspace
Resource References
Description
Image elements in Print Models no longer store image content embedded inside the model file.
Previously the Print Model Editor uploaded an image as an attachment and persisted its binary
content as static, base64-encoded data inside the model JSON. From release 2026.06 onward, image
content is held as a Workspace resource and the Print Model references it by attachment. The Print
Model Editor now offers a drop-down of the allowed Workspace resources (for example image-type
resources) instead of accepting an inline upload, and the editor resolves the image data through the
StaticImageProvider.
Explanation
Embedded image content bloats the Print Model JSON with large base64 blobs that are not part of
the model’s logical structure. This caused problems for the model-checker tooling and for agentic AI
consumers, where processing the embedded binaries consumed many tokens without adding
modeling value. Moving image content into Workspace resources keeps Print Models small,
diffable, and free of binary payloads, while letting several models share and deduplicate the same
resource.
Migration Path
Migrate your Print Model files so that embedded image content is extracted into Workspace
resources and replaced by references. In the Simple Model Editor (SME) the migration runs
automatically: when a workspace contains models that still embed images, an error appears with
prompting messages, and clicking the Resolve All Issues button extracts the embedded
attachments into Workspace resources and rewrites the references with no further work for the
modeler. For Print Model files outside the SME, run the Node-based migration tool, which
recursively extracts every embedded image into a Workspace resource and deduplicates identical
images. After migration, verify that each image element points at the expected Workspace resource
via the resource drop-down in the Print Model Editor. For printing and Print Preview, the backend
must resolve the referenced image data: implement an AttachmentProvider so that attachments
referenced by attachment_id can be loaded at render time, since the Print Preview does not render
unresolved attachment_id references on its own.
For the full component-level details, see the Print Engine > Migration Instructions.
33

-- 33 of 42 --

Examples
Before
{
"type": "image",
"source": "attachment",
"attachment": {
"content": "iVBORw0KGgoAAAANSUhEUgAA...large base64 blob..."
}
}
After
{
"type": "image",
"source": "attachment",
"attachment": {
"attachment_id": "company-logo.png"
}
}
The image content now lives as a Workspace resource and is resolved at render time through the
StaticImageProvider in the editor and preview, and through an AttachmentProvider in the print
runtime.
Detector
Run this in your project to find affected files:
grep -rE --exclude-dir={node_modules,build,dist,.git,.gradle,target}
'"content"[[:space:]]*:[[:space:]]*"(iVBOR|/9j/|R0lGOD|Qk[0-9])' .
Automation
A codemod is available: print-model-migration.
Run it with:
print-model-migration <path to print model file or directory> --backup
Effort
Automation: automated-via-codemod
Estimated effort: M-L
Notes: The extraction itself is automated by the SME Resolve All Issues action or the Node-based
34

-- 34 of 42 --

migration tool, so the model side is low effort. The spread toward the higher band comes from the
backend work: projects that print outside the SME must implement an AttachmentProvider to
resolve referenced images at render time, and must confirm that Print Preview and PDF output still
render every image after the references change. Projects with many Print Models that embed
images, or with custom print integrations, sit at the upper end.
User Management Re-Architecture: uaa-user-management to uaa-
usermanagement
Description
The legacy backend Java module family uaa-user-management-* is replaced entirely by the refactored
uaa-usermanagement-* family under the uaa-usermanagement/ umbrella project. Gradle coordinates
change from :uaa-user-management-<suffix> to :uaa-usermanagement:uaa-usermanagement-<suffix>.
Domain POJOs User, Role, and AccessRight are replaced by UMUserRepresentation,
UMRoleRepresentation, and UMAccessRightRepresentation. Service interfaces UserService/RoleService
/AccessRightService become UMUserService/UMRoleService/UMAccessRightService. The monolithic
UserManagementServiceRestClient is split into UMUserRestClient/UMRoleRestClient
/UMAccessRightRestClient. REST endpoints move from path-variable lookups (/user/read/{username})
to query-parameter lookups (/user?id=…), and /user-management/* endpoints are removed.
Extension SPIs (IUserDocumentConversionService, IUserDocumentCustomizationService,
IUserIDPConversionService, IUserIDPCustomizationService) are replaced by UserExtensionConverter,
UMUserDocumentEventCustomizer, IDPUserConverter, and IDPUserExtensionConverter. Event classes
UserAfterCreate/Update/DeleteEvent<U> become the record types
UMUserAfterCreate/Update/DeleteEvent. The uaa-user-management-tool CLI module is not migrated.
The frontend module uaa-user-management-module is out of scope and unchanged.
Explanation
The User Management backend was re-architected into a modular, layered module family (-common,
-service, -idp, -keycloak, -keycloak-plugin, -rest-client-api, -rest-client) to make it more flexible,
extensible, stable, and clean, and to significantly reduce the effort of future maintenance and
feature work. Keycloak and IDP concerns were separated from the core service so that other
identity providers can be supported, and the REST client contract was split from its HTTP
implementation. End-user-facing behavior (user, role, and access-right CRUD, search,
import/export, Keycloak synchronization) is unchanged, and no database migration is required.
Migration Path
Migrate as a one-shot cutover: there is no @Deprecated bridge release and no legacy-fallback
compatibility layer. No codemod or OpenRewrite recipe is provided for this change; perform the
following steps manually: Gradle/Maven coordinate updates, package and import rewrites (for
example com.mgmtp.a12.uaa.um.client.rest.* and the legacy usermanagement.um.* packages), and the
domain-POJO type renames to the UM*Representation classes. Then complete the remaining steps by
hand: adopt the UMUserRepresentation.builder() API, move any ExtendedUser subclass to a
UMUserRepresentation subtype with a UserExtensionConverter<T> and a matching Supplier<T> bean,
migrate the extension SPIs to their replacements, update event-listener signatures to the new
record events and accessors (for example getCreatedDocument() becomes latestDocument()), and
35

-- 35 of 42 --

apply the property rename table (notably idp-registration[*] to tenant-registration[*] and core-
model-name to user-document-properties.user-domain-name). Update the authorization scope names
(for example Create User Data Model to Create User, Download Users As Yaml File to Export User)
across every @PreAuthorize, and update any direct REST consumers to the new query-parameter
routes. Merge any legacy extension document model into the single DomainUserManagement.json
under the user/extension group. Multi-tenancy is a new opt-in subsystem (um.multi-
tenant.enabled=true); single-tenant projects need no action. The full per-phase procedure and
rename tables are documented in the User Management > Migration Instructions.
Examples
Before
// Legacy domain POJO and service
import com.mgmtp.a12.uaa.usermanagement.User;
import com.mgmtp.a12.uaa.usermanagement.UserService;
User user = new User();
user.setUsername("jdoe");
userService.create(user);
// Legacy event listener
@EventListener
public void onCreate(UserAfterCreateEvent<User> event) {
var doc = event.getCreatedDocument();
}
mgmtp.a12.uaa.user-management.idp-registration[default].url=https://idp.example.com
mgmtp.a12.uaa.user-management.um.core-model-name=DomainUserManagement
After
// Refactored representation and service
import com.mgmtp.a12.uaa.usermanagement.representations.UMUserRepresentation;
import com.mgmtp.a12.uaa.usermanagement.service.UMUserService;
UMUserRepresentation user = UMUserRepresentation.builder()
.username("jdoe")
.build();
umUserService.create(user);
// Refactored event listener (record-typed event, new accessor)
@EventListener
public void onCreate(UMUserAfterCreateEvent event) {
var doc = event.latestDocument();
}
36

-- 36 of 42 --

mgmtp.a12.uaa.user-management.tenant-registration[default].url=https://idp.example.com
mgmtp.a12.uaa.user-management.um.user-document-properties.user-domain-
name=DomainUserManagement
Detector
Run this in your project to find affected files:
grep -rE --exclude-dir={node_modules,build,dist,.git,.gradle,target} '(uaa-user-
management-
|com\.mgmtp\.a12\.uaa\.um\.client\.rest|UserManagementServiceRestClient|IUserDocumentC
onversionService|UserAfterCreateEvent|idp-registration\[)' .
Automation
No codemod or OpenRewrite recipe is provided for this migration; every step is manual.
Effort
Automation: fully-manual
Estimated effort: L
Notes: Effort scales with how deeply the project integrates with User Management. A small project
that only consumes the REST client for user CRUD sits near the low end (dependency swap,
representation rename, scope rename: roughly half a day to a day). A medium project that embeds
the service with the default converters lands around one to two weeks (property migration plus
event-listener updates). A large project with custom conversion/customization SPIs, bespoke
authorization JSON, or an embedded UM service trends toward the high end (two to four weeks,
including SPI migration, the multi-tenancy decision, and integration tests). Since no codemod
automates any part of the rename, mechanical package/import/type renames add to the manual
effort at every level.
Risks
Warning: This is an irreversible one-shot cutover: there is no @Deprecated bridge release and no
legacy-fallback compatibility layer, so the legacy and refactored modules cannot run side by side in
the same application. Mitigate by deploying the upgraded service alongside the legacy one
(blue/green) where infrastructure permits and running the smoke-test suite against the new
deployment before cutting traffic.
Caution: If a project subclasses UMUserRepresentation for custom fields but forgets to register a
matching Supplier<T> bean, the UserExtensionConverter receives plain UMUserRepresentation
instances and throws ClassCastException on the first read. Always register the Supplier<T> bean
together with the converter.
Information
37

-- 37 of 42 --

Unified OnlyForUsage Convention for Usage-Only Interfaces
Description
A12 server-side components now mark usage-only public API with the @OnlyForUsage annotation. Do
not implement an annotated interface or extend an annotated class in your project. This replaces
the former Data Services convention where interfaces without an I prefix were usage only; type
names do not change.
Explanation
Previously, A12 server-side components used two different conventions to distinguish interfaces
meant for implementation from interfaces meant for usage only. Kernel used the annotation in Java
code, while Data Services relied on the I-name-prefix convention. With more than one convention
in play, a developer had to read the documentation of each component to learn which signal
applied where. Unifying on a single annotation lets a developer recognize usage-only API
consistently across all server-side components without consulting per-component documentation.
It also clarifies that such API may be extended in non-breaking releases, so projects should not
implement or extend it unless they accept that added members may require adapting their own
code.
Migration Path
No build-time action is required: type and interface names do not change, and existing code
continues to compile. Review how your project decides whether an A12 server-side interface or
class is safe to implement or extend. Stop relying on the Data Services I-name-prefix rule, because
Data Services no longer follows it and its documentation no longer describes it. Instead, treat any
type annotated with @com.mgmtp.a12.model.utils.OnlyForUsage as usage only, across all server-side
components. If your project implements or extends a now-annotated type, for example to create a
mock in tests, this remains technically possible. Be aware, however, that members added to such a
type in a future non-breaking release will require you to adapt your implementation, because you
deliberately implemented a usage-only contract.
For the per-component details, see the migration instructions of Base, Data Services, and Kernel.
Examples
Before
// Data Services signaled implementability through the type name.
// An interface WITHOUT the `I` prefix was for usage only:
package com.mgmtp.a12.dataservices.api;
public interface DocumentStore { /* usage only */ }
// An interface WITH the `I` prefix was meant to be implemented:
public interface IDocumentConverter { /* implement in your project */ }
38

-- 38 of 42 --

After
// The unified convention uses the annotation from base-model-utils.
// Usage-only API is marked explicitly; the name is unchanged:
package com.mgmtp.a12.dataservices.api;
import com.mgmtp.a12.model.utils.OnlyForUsage;
@OnlyForUsage
public interface DocumentStore { /* do not implement */ }
// API without the annotation is the one you may implement or extend:
public interface DocumentConverter { /* implement in your project */ }
Detector
The @OnlyForUsage annotation lives in the A12 library code (base-model-utils), not in your project, so
grepping your own sources for @OnlyForUsage returns nothing. What you can detect in your own
code is where it references or implements the affected I-prefixed Data Services types.
Run this in your project to find those references:
grep -rEn --exclude-dir={node_modules,build,dist,.git,.gradle,target}
'com\.mgmtp\.a12\.dataservices\.[A-Za-z0-9_.]+\.I[A-Z][A-Za-z0-9_]*' .
NOTE
This is an informational change and most matches need no action. A match only
flags a place that references a Data Services interface; act only where your project
actually implements or extends such a type, because the I-prefix rule that
previously signalled implementability no longer applies. To confirm whether a
specific A12 type you implement is now marked usage-only, do not grep your own
tree: look the type up in that component’s API documentation, or in the A12 sources,
and check for the @OnlyForUsage annotation there.
Effort
Automation: fully-manual
Estimated effort: XS-L
Notes: For most projects the effort is XS: nothing breaks, and there is no required code change. The
upper end of the band applies only to projects that programmatically implement or extend Data
Services interfaces previously selected by the I-prefix naming convention. Such projects may need
to audit those implementations, confirm whether each target is now annotated @OnlyForUsage, and
decide whether to keep the implementation knowing future non-breaking releases may add
members. No automated codemod or OpenRewrite recipe is provided. The option was investigated,
but because the change adds annotations and removes a naming convention rather than rewriting
call sites, a reliable automatic refactoring was not warranted.
39

-- 39 of 42 --

Known Issues
NOTE
No known issues have been reported for the migration to 2026.06 so far. Any in-
cycle behavior changes or required workarounds discovered during the release
cycle will be added here.
Verification
Run the general verification checks after migrating to 2026.06.
The general checklist covers this release. Pay particular attention to the areas changed by 2026.06:
the Spring Boot 4 server startup, the Infinispan cache (single-pod formation or the JGroups cluster
for multi-pod deployments), and the client build on TypeScript 6, React 19.2.6, and Redux 5.
FAQs
Is It Better Migrate to the Next Release or to the
Newest Version?
Upgrading to the newest version will give you the guarantee that you are done with upgrading until
a new A12 version is released, and the following upgrade will be easier. Migrating only to the next
version is always easier – you need to process less changes. A12 TPS team recommends upgrading
to the newest A12 version and then upgrading regularly.
From version 2024.06 on, A12 will only support the migration from the previous breaking release.
More information can be found in the Migration Guide section.
Where to Find Examples and Help?
• For questions and answers specific to this release, browse the A12 Discourse → Migration Topics
for 2026.06.
• Every A12 component documentation has a section Migration Instructions, where you can
find what you should update for the respective component.
• In case you can not find a solution for your issue in the documentation, feel free to ask TPS.
Will TPS Support Me?
Yes. In the past TPS has been doing the upgrades of the projects very closely with project
developers. Nowadays TPS acts rather as consultants. If you have any issues, please first try to find
it in available sources.
If you cannot find it, we are ready to help. For details on reaching our team, please visit our support
page on GetA12.
40

-- 40 of 42 --

Is It Better to Migrate Models With SME or With
Command Line Tools?
Both approaches have advantages and disadvantages. The SME gives you the option to migrate all
types of models at once. The command line tools give you the option to call the model migration
steps separately with scripts and easily see the logs in case of problems.
Links to the Components Migration
Instructions
The following list links the Migration Instructions section of every A12 component that ships
migration content. Check the entry for each component your project uses.
• Logging
• Collections
• Localization
• Server Connector
• Base
• Widgets
• Diagram Editor
• Kernel
• Test Data Generator
• User Management
• User Authentication and Authorization
• Data Services
• Data Distribution
• Form Engine
• Transformer
• Client
• Overview Engine
• Relationship Engine
• Tree Engine
• CRUD
• Workflows
• Notification Center
• Content Engine
• Content Management System
41

-- 41 of 42 --

• Print Engine
• Expression
• Helm Stack Charts
42

-- 42 of 42 --

