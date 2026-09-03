# overall access_artifacts

Accessing A12 Artifacts
CAUTION
This setup is only for mgm partners who do not have access to the mgm
artifactory. The setup for mgmies can be found in the documentation of the
Project Template.
IMPORTANT The audience of this chapter is developers.
This chapter describes the necessary setup for getting access to our A12 artifacts. The following
sections describe the two scenarios for this. If you already operate your own proxy for artifacts,
then please read on with Access via Proxy. Otherwise, please continue with Direct Access.
Dual Licensing and Related Authentication
A12 is offered in two editions - Community and Enterprise. Access to the artifacts differs between
the two editions, and details are described in the Direct Access section.
Access via Proxy
npm
Set up your own npm registry to proxy https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/, and https://artifacts.geta12.com/artifactory/api/npm/a12-enterprise-npm/ if you
are an enterprise user.
Please refer to the documentation of the used registry for more details on how to set up a proxy.
Maven
Set up your own Maven repository to proxy https://artifacts.geta12.com/artifactory/a12-
community-maven/, and https://artifacts.geta12.com/artifactory/a12-enterprise-maven/ if you are
an enterprise user.
Additional required repositories:
• https://repository.jboss.org/nexus/content/groups/public
• https://build.shibboleth.net/nexus/content/repositories/releases
Please refer to the documentation of the used repository for more details on how to set up a proxy.
Docker
Set up your own Docker registry to proxy https://artifacts.geta12.com/artifactory/a12-community-
docker/, and https://artifacts.geta12.com/artifactory/a12-enterprise-docker/ if you are an
1

-- 1 of 9 --

enterprise user.
Please refer to the documentation of the used registry for more details on how to set up a proxy.
Helm
Set up your own Helm registry to proxy https://artifacts.geta12.com/artifactory/a12-community-
helm/, and https://artifacts.geta12.com/artifactory/a12-enterprise-helm/ if you are an enterprise
user.
Please refer to the documentation of the used registry for more details on how to set up a proxy.
Direct Access
Community Users
Community artifacts are accessible without authentication. You only need to set up the following
registries in your build tools.
npm
In order to get access to the community npm artifacts of A12, it is required to extend your .npmrc.
(Please read the npm documentation for more details.)
NOTE
The '~' (tilde) symbol marks a relative path to the user’s home directory. It is mostly
used for Unix-like systems, but works in Windows as well via PowerShell.
It would typically be /home/username, /Users/username or C:\Users\username,
depending on the used OS.
For the .npmrc, you can use the following template:
~/.npmrc
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
2

-- 2 of 9 --

@com.mgmtp.a12.kernel:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.overviewengine:registry=https://artifacts.geta12.com/artifactory/api/np
m/a12-community-npm/
@com.mgmtp.a12.projecttemplate:registry=https://artifacts.geta12.com/artifactory/api/n
pm/a12-community-npm/
@com.mgmtp.a12.treeengine:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-community-npm/
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
@com.mgmtp.a12.migrationtool:registry=https://artifacts.geta12.com/artifactory/api/npm
/a12-community-npm/
@com.mgmtp.a12.relationshipengine:registry=https://artifacts.geta12.com/artifactory/ap
i/npm/a12-community-npm/
@com.mgmtp.a12.crud:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
community-npm/
@com.mgmtp.a12.querymodel:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-community-npm/
Maven
Add https://artifacts.geta12.com/artifactory/a12-community-maven/ to your Maven or Gradle
settings. Please read the Maven documentation or Gradle documentation for more details.
Additional required repositories:
• https://repository.jboss.org/nexus/content/groups/public
• https://build.shibboleth.net/nexus/content/repositories/releases
In the end, your repositories.gradle file should look like the following:
~/.gradle/init.d/repositories.gradle
settingsEvaluated { settings ->
settings.pluginManagement {
repositories {
exclusiveContent {
forRepository {
3

-- 3 of 9 --

maven {
name "mgmart"
url 'https://artifacts.geta12.com/artifactory/a12-community-
maven/'
}
}
filter {
includeGroupByRegex "com\\.mgmtp\\.a12.*"
}
}
maven {
url "https://plugins.gradle.org/m2/"
}
}
}
}
allprojects {
repositories {
exclusiveContent {
forRepository {
maven {
name "mgmart"
url 'https://artifacts.geta12.com/artifactory/a12-community-
maven/'
}
}
filter {
includeGroupByRegex "com\\.mgmtp\\.a12.*"
}
}
mavenCentral()
maven {
url "https://build.shibboleth.net/nexus/content/repositories/releases/"
}
}
}
NOTE If the init.d folder doesn’t exist, it has to be created.
Docker
The community Docker registry is accessible right away. You can test access by pulling the images
by following:
docker pull <image>:<tag>
4

-- 4 of 9 --

Helm
Add https://artifacts.geta12.com/artifactory/a12-community-helm/ to your Helm repositories:
helm repo add <repo-name> https://artifacts.geta12.com/artifactory/a12-community-helm/
helm search repo <repo-name>
Enterprise Users
Access to the enterprise artifacts is restricted by authentication.
GetA12 Artifactory Account
Your GetA12 credentials, used for accessing this documentation, should also be valid for the GetA12
Artifactory. If you do not have access, please reach out to your project lead or the Partner
Management Team to request the access.
Technical User
CAUTION
To retrieve the enterprise artifacts using automation tools or CI/CD pipelines,
you must use a technical user along with a valid identity token generated
from GetA12 Artifactory.
If you are an enterprise user and your project does not have a technical user account yet, please
request it from the Partner Management Team.
Identity Token
You will need to create an identity token for build tools that require Artifactory access by following
these steps:
1. Login to Artifactory (mgm partners only) with your getA12 credentials, or project technical
user’s credentials.
2. Open your profile in the top-right corner, and click Edit Profile.
3. In Authentication Settings click on Generate an Identity Token.
4. Add a Description, e.g., "Build Tools".
5. Copy the token and use it for the tool configuration.
CAUTION The token is not stored in the Artifactory, so make sure you copied it before
closing the window with the token string.
npm
In order to get access to the enterprise npm artifacts of A12, it is required to extend your .npmrc.
(Please read the npm documentation for more details.)
5

-- 5 of 9 --

NOTE
The '~' (tilde) symbol marks a relative path to the user’s home directory. It is mostly
used for Unix-like systems, but works in Windows as well via PowerShell.
It would typically be /home/username, /Users/username or C:\Users\username,
depending on the used OS.
CAUTION Make sure to use your identity token, not the GetA12 password. Using the
password will lead to a locked account after a few trials.
Once you have created the identity token, you need to add it to your .npmrc along with your email
and Artifactory username. For the .npmrc, you can use the following template:
~/.npmrc
registry=https://registry.npmjs.org/
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
@com.mgmtp.a12.uaa:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.utils:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.widgets:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.workflows:registry=https://artifacts.geta12.com/artifactory/api/npm/a12
-enterprise-npm/
6

-- 6 of 9 --

@com.mgmtp.a12.expression:registry=https://artifacts.geta12.com/artifactory/api/npm/a1
2-enterprise-npm/
@com.mgmtp.a12.print:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.contentengine:registry=https://artifacts.geta12.com/artifactory/api/npm
/a12-enterprise-npm/
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
@com.mgmtp.a12.ai:registry=https://artifacts.geta12.com/artifactory/api/npm/a12-
enterprise-npm/
@com.mgmtp.a12.modelchecker:registry=https://artifacts.geta12.com/artifactory/api/npm/
a12-enterprise-npm/
Maven
You have to define the two following properties in your ~/.gradle/gradle.properties:
~/.gradle/gradle.properties
repository_username: <This is your email>
repository_password: <This is your identity token>
CAUTION Make sure to use your identity token, not the GetA12 password. Using the
password will lead to a locked account after a few trials.
Add https://artifacts.geta12.com/artifactory/a12-enterprise-maven/ to your Maven or Gradle
settings. Please read the Maven documentation or Gradle documentation for more details.
Additional required repositories:
• https://repository.jboss.org/nexus/content/groups/public
• https://build.shibboleth.net/nexus/content/repositories/releases
In the end, your repositories.gradle file should look like the following:
~/.gradle/init.d/repositories.gradle
settingsEvaluated { settings ->
settings.pluginManagement {
7

-- 7 of 9 --

repositories {
exclusiveContent {
forRepository {
maven {
name "mgmart"
url 'https://artifacts.geta12.com/artifactory/a12-enterprise-
maven/'
credentials {
username = "${settings.repository_username}"
password = "${settings.repository_password}"
}
}
}
filter {
includeGroupByRegex "com\\.mgmtp\\.a12.*"
}
}
maven {
url "https://plugins.gradle.org/m2/"
}
}
}
}
allprojects {
repositories {
exclusiveContent {
forRepository {
maven {
name "mgmart"
url 'https://artifacts.geta12.com/artifactory/a12-enterprise-
maven/'
credentials {
username = "${settings.repository_username}"
password = "${settings.repository_password}"
}
}
}
filter {
includeGroupByRegex "com\\.mgmtp\\.a12.*"
}
}
mavenCentral()
maven {
url "https://build.shibboleth.net/nexus/content/repositories/releases/"
}
}
}
NOTE If the init.d folder doesn’t exist, it has to be created.
8

-- 8 of 9 --

Docker
Authenticate to https://artifacts.geta12.com/artifactory/a12-enterprise-docker/ by using:
docker login artifacts.geta12.com/artifactory/a12-enterprise-docker/ -u <username>
If the docker login asks for a password, you have to enter your identity token.
CAUTION Make sure to use your identity token, not the GetA12 password. Using the
password will lead to a locked account after a few trials.
Then you can pull the images by following:
docker pull <image>:<tag>
Helm
Add https://artifacts.geta12.com/artifactory/a12-enterprise-helm/ to your Helm repositories:
helm repo add <repo-name> https://artifacts.geta12.com/artifactory/a12-enterprise-
helm/ --username <username>
helm search repo <repo-name>
If the helm add repo asks for a password, you have to enter your identity token.
CAUTION Make sure to use your identity token, not the GetA12 password. Using the
password will lead to a locked account after a few trials.
9

-- 9 of 9 --

