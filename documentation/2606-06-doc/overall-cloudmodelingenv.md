# overall cloudmodelingenv

Cloud Modeling Environment
This documentation is intended for a business analyst audience. Prior
knowledge of the Simple Model Editor and the Preview Application is assumed.
NOTE You must have an account on geta12.com with respective rights to use the Cloud
Modeling Environment.
Introduction & Terminology
Instead of a local installation of the Modeling Environment, we offer a cloud-based solution for
accessing our key modeling tools. This includes both the Simple Model Editor and the Preview
Application. They can be accessed by the Cloud Modeling Control, which is an administrative view
which enables the user to start Cloud Modeling Environment Workspaces. The following is a guide
to setting up and using a Cloud Modeling Environment Workspace, short CME-Workspace.
Although the tools are available in the browser, it is still necessary to have locally stored models to
work with in the SME.
The Sample Workspaces can be used as a starting point for modeling. Please download them from
geta12.com.
Workspaces
For the Cloud Modeling Environment, the term "workspace" is used in two different contexts. A
single CME-Workspace contains both, a SME and a Preview Application. Starting, stopping or
deleting a CME-Workspace, will start, stop or delete the corresponding instances of the SME and the
Preview Application. A Sample Workspace or SME Workspace is a set of files that is considered to be the
content of the Preview Application and contains editable models to work with in the SME. During
modeling, models are not stored in the CME-Workspace. Instead, as mentioned above, the SME
loads and saves model files directly from the user’s local file system.
Use Case
The Cloud Modeling Environment is to be treated as a Demo and is available as Software as a
Service with a corresponding login. Details about this can also be found in the "Terms of use for the
demo environment of the Cloud Modeling Environment".
The goal is to enable mgm’s pre-sales colleagues to prepare and present a fully modeled pre-sales
application. The prepared Cloud Modeling Environment Workspace can be handed over to the
interested party, who can then access the modeling tools and view and modify the models. With the
Cloud Modeling Environment, this can be done without installing anything. This demo is therefore
not limited by the constraints of the IT environment.
NOTE For sharing and versioning of A12 models / SME Workspaces we recommend the use
1

-- 1 of 6 --

of common version control systems such as git.
Get Started
For accessing the Cloud Modeling Control (https://cme.playground.mgm-tp.com/) you will need to
log in with the cloud modeling account and arrive at the following view.
Figure 1. Create New CME-Workspace View
The 5 tiles offer a selection of the possible starting points based on Sample Workspaces available on
getA12. Please select one by clicking on the tile.
2

-- 2 of 6 --

Figure 2. Progress of Starting the new CME-Workspace
As a result, the CME-Workspace will be initialised and when the process is complete, the SME will
open.
NOTE The supported browsers for the SME are Chrome (Windows, MacOS), edge
(Windows) and Chromium (Ubuntu).
At the same time, the Preview App server and client will start and its content is determined by the
selected Sample Workspace. Please be aware that the Preview App takes some time to start up. You
can access the running Preview App client by copying the URL of the Simple Model Editor, which
looks like:
https://cme.playground.mgm-tp.com/userID/CME-WorkspaceID/7000/
And change the port 7000 to 7001:
https://cme.playground.mgm-tp.com/userID/CME-WorkspaceID/7001/
The SME can be connected to the running Preview App server via the same URL with /api added:
https://cme.playground.mgm-tp.com/userID/CME-WorkspaceID/7001/api
The userID is displayed on the top right and the CME-WorkspaceID is listed in the menu item
"Workspaces" or is also to be found in the sidebar of the Cloud Modeling Control mentioned as
"Recent Workspaces". The information is also to be found in the already opened SME URL.
3

-- 3 of 6 --

Figure 3. Examples for User and CME-Workspace IDs
Figure 4. Example Configuration for Server Connection in SME
As soon as the SME is connected to the Preview App Server you deploy the models. The models can
be based on a Sample Workspace or a completely different workspace. Be careful with modifications
that have an effect on documents already present. At the moment the Cloud Modeling Environment
does not offer any database control, hence a deletion of the Preview App database is not possible.
4

-- 4 of 6 --

Figure 5. Working with the SME in the Cloud Modeling Environment
At the moment it is only possible to have one running CME-Workspace. If you decide to initialise
your Cloud Modeling Environment based on a different Sample Workspace, it is necessary to delete
the current one and create a new one with the respective starting point.
For deleting the currently running one return to the overview of CME-Workspaces. The option to
delete is to be found in the three-dot-menu on the very right.
Figure 6. Deleting the Current CME-Workspace.
5

-- 5 of 6 --

The deletion has to be confirmed. The locally stored models are not affected by this.
6

-- 6 of 6 --

