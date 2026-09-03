# overall dev_tutorial_intro_project_template

Task 2 - Project Template
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Intro > Introduction before
continuing here.
You can check out the tag 2025.06-ext5/intro/task-2-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/intro/task-2-end to see how
your code and models differs from the solution.
Use-Case
In the previous task, we have created the CRUD functionality for the contacts in our CRM system via
modeling. We were also easily able to add these models into the Project Template as the starting
point for our application.
While the Project Template is a good basis, it still needs to be initialized for each specific
application. In this task, we will handle this and get our application ready to be customized further
in the next tutorials.
End Result
In this task we will go through the steps needed to start an application using the Project Template.
Specifically, this task covers:
• How to initialize and rename the Project Template.
• How to register a module.
• How to provide initial data for development testing.
Step-by-Step Instructions
1

-- 1 of 11 --

Starting With the Project Template
As it was mentioned previously, the tutorial application is based on the Project Template. The base
tutorial application, before we have started applying changes from any of the tutorial, is just the
Project Template (specifically its local authentication variant) with only some insignificant changes,
e.g. a different readme.
Therefore, the changes we will be making in this task to adjust the Project Template are also needed
for projects starting with it.
However, we will only cover the aspects for creating a new project with the Project Template that
are relevant within the scope of these tutorials. There are other points that we will not discuss as
they are too specific or advanced, for example deployment or Docker adjustments for external
developers.
But you can find more information about these and additional topics in the Project Template
documentation.
We will start with one of the first steps when initializing the Project Template, which is renaming it.
If we take a look at the current state of the application, we can see that it is currently just called
"Your-Project-Name". This is not suitable for the CRM, so this is what we will take care of first.
Renaming the Application
Renaming the Project Template is actually very easy, as it contains designated placeholders and
provides a convenient Gradle task to automatically replace them with the names of your choosing.
The basis for this task is the setup.json file, which contains a list of all the placeholders with their
current value and the alternative value that should replace them when executing the task.
So to automatically replace the placeholders, you need to do the following:
• Navigate to the root directory in the terminal.
• Go to setup.json and change the values of alternative to the names of your choice, for example:
{
"serverPackage": {
"current": "com.mgmtp.a12.template.server",
"alternative": "com.mgmtp.a12.tutorial.server"
},
"serverApplication": {
"current": "ProjectTemplateServerApplication",
"alternative": "TutorialServerApplication"
},
"initApplication": {
"current": "ProjectTemplateInitApplication",
"alternative": "TutorialInitApplication"
},
"projectProperties": {
"current": "mgmtp.a12.template.server",
2

-- 2 of 11 --

"alternative": "mgmtp.a12.tutorial.server"
},
"projectGroup": {
"current": "com.mgmtp.your-project-name",
"alternative": "com.mgmtp.a12-tutorial-application"
},
"projectName": {
"current": "your-project-name",
"alternative": "a12-tutorial-application"
},
"projectImportsArtifact": {
"current": "project-template-imports",
"alternative": "a12-tutorial-application-imports"
},
"sonarProjectKey": {
"current": "Project-Template",
"alternative": "A12-Tutorial-Application"
},
"projectDisplayName": {
"current": "Project Template",
"alternative": "A12 Tutorial Application"
},
"appModelName": {
"current": "YourAppModel_AM",
"alternative": "Tutorial_AM"
},
"title": {
"current": "Your-Project-Name",
"alternative": "A12 Tutorial Application"
},
//...
"nginxServerBaseUrlVarName": {
"current": "NGINX_PROJECT_TEMPLATE_SERVER_BASE_URL",
"alternative": "NGINX_A12_TUTORIAL_APPLICATION_SERVER_BASE_URL"
}
}
You do not need to do this for all placeholders, if only some are relevant. In our case, we only
need to adapt the ones in the example above.
• Run gradle replacePlaceholders.
If you have any persisted data, you need to remove it. Out-of-the-box the Project Template is
configured with an Postgres in-file database, so you can for example remove all stored data by
deleting the server/postgres folder.
Otherwise, some issues may occur like duplicated modules being displayed when changing the
values of the appModelName property.
All placeholder values in the project should now be replaced with the provided alternative values.
As a result of the package group name change, the files server/app/src/main/java and
3

-- 3 of 11 --

server/init/src/main/java are moved to the newly created directories in the path, corresponding
with the new group name, and the previous directories are removed.
Please be aware, that in the IDE it sometimes takes a bit until these changes are loaded and
displayed. If any issues arise with the IDE not registering the changes correctly after the renaming,
it usually helps to invalidate the caches and restart it.
In the setup.json file, you can now see that the current values have been updated.
This is done to ensure stability, so that you can do the renaming as often as you would like.
IMPORTANT Therefore, you should never change the current field values if you have no
good reason for it, as this can cause issues.
If you restart the server, you will be able to see that the application title has been updated
accordingly:
Model Deployment
Before we continue with initializing the Project Template, we will digress briefly and set up a
helpful functionality for the following steps.
Currently, if we were to make changes in our models, we would always need to restart the server to
see them in the application. Due to the fact that, while the client automatically reloads when a
changed file is saved, any changes outside of it require a manual restart of the server.
For our models, we can avoid this as the SME provides functionality to deploy models during
runtime. This means that after you have changed the models, you can deploy them and directly
view the changes in the application.
4

-- 4 of 11 --

For this it is necessary that the client and server are running. To configure this for a project based
on the Project Template, you need to do the following:
• Click the [ Configure Server Connection ] button at the top right of the SME.
• In the dialog, add the following information:
◦ Authentication Type: "Local"
◦ URL: "http://localhost:8082/api"
◦ Username: "admin"
◦ Password: "A12PT-admintest"
• Click the [ Connect ] button.
Please note, that the instructions above are intended for the local authentication variant of the
Project Template. The standard Project Template uses Keycloak, for which the server connection
configuration is different. You can find more info about this in Project Template > Model
Deployment.
Now you can try changing something in one of the models, e.g. renaming the label of a field. You
can then deploy this model specifically via the [ Deploy ] button (next to the [ Save ] and [ Cancel ]
button) in the editor. Alternatively, you can deploy the entire workspace with the [ Deploy All ]
button in the Workspace Explorer which you can find next to the [ Validate ] button.
NOTE The [ Deploy All ] button will delete all the documents in your database to prevent
any inconsistencies that may arise from changes to the Document Models.
After you have successfully deployed the model(s), which you will be notified of by a notification in
the SME, you can reload the page in the browser and see the changes in the application. In the next
step we will be making some more changes to the Application Model, where this feature will be
very useful.
Extending the Contact Module
In the last task we have already registered our contact module in the application by just providing
the models in the import/models folder. But we have not yet discussed how and why this works.
Being able to have multiple Application Models in the application without any changes in the client
5

-- 5 of 11 --

is possible due to configurations available in the Project Template, which in turn is using the
adapter module from the A12 Client.
The purpose of the adapter module is handling multiple Application Models by collecting and
registering all Application Models in your model graph. You can find more information on model
graphs in Client > Model Graph and Data Services > Model Graph.
To use it, you need to provide a model graph containing all needed Application Models and to
register it like any other Client module in your application. Then you do not need to specify any
Application Models in the setup of the application. However, for typing a placeholder is still
necessary.
All of these aspects are already configured in the Project Template, so for us no further setup is
required. If you would like to know more about the needed steps, please refer to Client > Adapter
Module.
Consequently, with this adapter module it is possible to add a new module to a Project Template
based application by just providing the models. However, this approach has one significant
disadvantage: In case of multiple Application Models, the order of the modules in the application is
not modelable. Instead, it is defined by the order in which the model graph loads them.
So while multiple Application Models are supported by Client and Data Services, the
recommendation is to just have one Application Model containing the different modules instead of
an Application Model for each module.
It is still possible to use multiple Application Models, but then the model graph needs to be
customized to configure the order of the modules in the application.
To avoid this, we will restructure our two Application Models Tutorial_AM and Contact_AM into one.
If we compare the two models, we can see that the region configuration is the same. So, we just
need to add the "ContactModule" into the Tutorial_AM.
Unfortunately, copy and pasting over separate models is not supported in the SME. If you want
more practice in navigating the Application Model editor, feel free to create the "ContactModule"
again manually. Alternatively, there are two shortcuts you can use:
• Option 1: Directly copy and paste the module in the JSON files.
◦ Open the Tutorial_AM.json and Contact_AM.json files directly in your IDE or text editor.
◦ There you can easily copy and paste the "ContactModule".
◦ With the [ Reload Workspace ] button in the upper right corner of the Workspace Explorer
in the SME, you can then reload your models and view these changes.
• Option 2: Copy and rename the person module.
This is possible as person and contact module are both just simple master/detail modules and
therefore have the same structure.
◦ Open the Tutorial_AM in the SME.
◦ In the Modules section, you can then copy the "PersonModule" with the associated row
button.
◦ Rename every reference of person to contact in the module, flow and scenes.
You can then delete the Contact_AM in the SME (by right-clicking or via the more options (⋮) menu).
6

-- 6 of 11 --

Since it is currently not possible to remove models with the model deployment, you will need to
delete the folder server/postgres and restart the server. You should now still see a module each for
person and contact.
You can try switching the order of the modules in the Modules list in Tutorial_AM with the arrow
buttons. If you deploy these changes again, you will see that the order of the modules has changed
accordingly.
Setting Contacts as the Starting Module
We can also see that changing the order of the modules does not impact which module is opened
initially when entering the application. This is currently always the person module, even if it is
ordered after contact.
This is because the order of the modules does not impact the initial module. Instead, this is defined
by setting an initial activity, which we introduced in Tutorials > Intro > Modeling > Initial Activity.
In Tutorial_AM we can see in the section Initial activity that the person module is set.
We now instead want to ensure that our contact module is opened when the application is loaded.
To do this we just need to add the activity descriptor of the "ContactModule" in the overall
Tutorial_AM file as shown below:
This instructs A12 that we want this activity to start. After deploying this change, we can see that
our contact module is opened initially instead of the person module.
Registering a Module
Currently, our modules are registered automatically through the adapter module. This is sufficient
for modules that are entirely modeled and require no additional customization in the client.
However, if a module needs to be customized, we need a way to register these customizations (e.g. a
custom saga) with the Client. This is not possible with the adapter module, so we need to create a
dedicated module.
To create and register the contact module, you just need to do the following:
• Add a new folder in client/src/modules called "contact".
• Create an index.ts file in the new folder with the following content:
import { type Module } from "@com.mgmtp.a12.client/client-core";
7

-- 7 of 11 --

const module: Module = {
id: "ContactModule",
};
export default module;
The Project Template includes an auto-discovery script that scans all subfolders of
client/src/modules for an index.ts file. Each such file is expected to export a default Module object.
This script runs automatically as part of the build process, so no manual registration is required. If
you want to update the discovered modules without triggering a full build, you can also run the
script manually with npm run generate in the client folder.
This process of adding a new module is explained in more detail in Tutorials > Frontend >
Application Frame > Module Registration.
Creating New Contacts on Start-up
Next we want to add some test data for our contact module. By providing some test data that is
automatically loaded on initialization of the server, we can avoid the extra effort of having to
manually create documents during development.
The Project Template provides all the necessary setup and configuration to enable this loading of
initial data. For this, an init-data Spring profile is configured in the init module, which you can
find in server/init/src/main/resources/config/application-init-data.properties.
To create documents automatically at the start of the application, you just need to create a JSON file
with a number of requests which are executed upon start-up of the server. To add this initial data,
you need to do the following:
• Create a file called ContactRequest.json in import/data/request.
• Copy the JSON below into this new file:
[
{
"jsonrpc": "2.0",
"method": "ADD_DOCUMENT",
"id": "AddContact1",
"params": {
"document": {
"Contact": {
"PersonalData": {
"FirstName":"Johny",
"LastName":"Smith",
"EmailAddress":"johny.smith@mgm-tp.com",
"Nationality":"British",
"Gender":"MALE",
"CustomerType":"lead",
"DateOfBirth":"1987-10-11"
8

-- 8 of 11 --

},
"Phones": [
{
"PhoneNumber":"123456",
"Type":"WORK"
},
{
"PhoneNumber":"7891011",
"Type":"MOBILE"
}
],
"Address": [
{
"Street":"Vordere Cramergasse",
"Housenumber":"11",
"City":"Nürnberg",
"Zip":"90478",
"Country":"Germany"
}
]
}
},
"documentModelName": "Contact_DM",
"locale": "en"
}
}
]
• Stop the server, if you have it running.
• Execute the init app with the command below. You can also find alternatives to this in Project
Template > Running Init Application, it is just important that the Spring profiles dev-env and
init-data are used.
gradle :server:init:bootRun --args='--spring.profiles.active=dev-env,init-data'
• Restart the server using the Spring profile dev-env.
What we are doing here, is to ask Data Services to execute this ADD_DOCUMENT command on startup.
In the params we see details of our Document Model and the data for the actual document that will
be created. Attachments, e.g. the profile picture, however can not be provided as this is currently
not supported for the initial data.
Please note, that if the field names, group names or structure of your Document Model is different
from our example, this might not work. A good way to get the correct structure is to look at the
network tab in the browser when creating a new contact document, as the exact same type of
request is sent there. Below, you can see an example of this in Chrome:
9

-- 9 of 11 --

You can try to extend the above file to add a second contact, this means you will add a second
element to the array with another document. Now you can test out the overview features such as
filtering, sorting and searching.
NOTE
This approach uses the Data Services init application. Alternatively, Seed Data can
be used, which can be created and maintained in the SME and deployed along with
the models. The Project Template supports this out of the box. For more details,
please refer to Project Template > Deploying the Seed Data.
Removing the Example Models and Module
As a last step, we will now just remove the person module from our application as this is not
needed for our CRM use-case.
To do this, you just need to delete the following:
• "PersonModule" from the Modules list in Tutorial_AM (Remember to save this change).
• Folder import/models/person with its contained models.
10

-- 10 of 11 --

• File import/data/request/PersonRequest.json.
• Folder server/init/src/main/java/com/mgmtp/a12/tutorial/server/init/migration.
• Optional: Folder server/postgres to delete any data related to Person in the database (e.g.
documents, models).
When you then restart the server, only the contact module should be visible in our application:
Conclusion
With this, we have sufficiently prepared and configured the Project Template into our own
application. We now have a good starting point to start with the actual development tutorial, you
can find the complete overview over all available ones in Tutorials > General Information >
Structure.
If something does not seem right, or you got stuck at any point, you can just check out 2025.06-
ext5/intro/task-2-end to see differences between both implementations and models.
Now that you have completed the intro tutorial, we would also really appreciate your feedback. If
you have any ideas or wishes for additional tasks or tutorials, you can let us know there.
« Task 1: Modeling
11

-- 11 of 11 --

