# overall modeling_tutorial_workflows

Tutorial: Workflows Modeling
Prerequisites
Before you start this tutorial, you should have completed the following training course(s) and
tutorials. For more details on what topics are covered, please follow the links.
• A12 Fundamentals Training
• Relationship Modeling Tutorial
NOTE
This tutorial describes the integration of BPMN and DMN Models with other A12
Models in your project.
For more information on BPMN and DMN Modeling, check out the links provided in
the Workflows Documentation under Further Resources.
This tutorial focuses on the integration of BPMN models into your application using the Element
Templates provides by Workflows and the relevant documentation is linked below. The
terminology used in this tutorial is explained in the documentation, but we’ve also included a
glossary of terms:
• Workflow Modeling documentation
• Glossary in Workflows Documentation
This tutorial uses the installer which you can download from geta12.com.
NOTE Please ensure your installer version matches this tutorial.
IMPORTANT
This tutorial uses the Camunda Modeler provided in the installer.
Camunda Modelers from other sources may differ or need to be configured.
See Provided Element Templates in the Workflows Documentation for more
information.
Use-case
I want to allow end users to complete a certification quiz. Once they have completed the
certification quiz, the quiz should be marked. After the quiz has been marked, the mark should be
visible with the certification quiz.
As markers sometimes make a mistake, the end user should be able to request that their quiz is re-
marked. When the quiz is re-marked, the marker should only see the certification quiz that the end
user submitted. Any previous marks should be hidden from the marker, so they are not influenced
by them.
1

-- 1 of 69 --

The end user can then complete the certification process by accepting the mark that they have
received.
[Workflow Overview] | Workflow_Overview.png
Figure 1. Use-case schema
End Result
At the end of this tutorial, you will be able to deploy your models so that you can:
• Start a Workflows Process the controls the completion and marking of a Certification Quiz from
an Overview
• Modify Document Models so that Certification and Marking Documents may be created and
modified in the Process
• Modify Form Models so that they can be used to control the Process
• Use Delegates to automatically link Certification and Marking Documents
• Use Delegates to update a Status Field so that Validation Rules can be triggered based on the
context
The end result will be similar to the with-workflows workspace.
If you need to check your work as you do the tutorial, please refer to the expandable sections at the
end of each step:
▼ Click here to see what your project should look like by now
You can find a list of models that you created as well as fullscreen pictures of each step to guide
you.
Essentials of Workflow Modeling
What Can I Do With Workflows?
Workflows provides an integration of Business Process Model and Notation (BPMN) and Decision
Model and Notation (DMN) capabilities into A12, enabling both the graphical modeling and the
execution of server-side workflows.
When the BPMN Models are executed by the process engine the behavior of the application
depends on the modeling choices that you expressed in the BPMN and DMN models.
This can include:
• Displaying the same Document using different Form Models depending on the current step of
the process
• Determining the next step of the process based on Field Values in the Document
• Setting the status of a Document
2

-- 2 of 69 --

• Managing Relationship Links between Documents
BPMN Models
The Camunda Modeler provided with the installer comes with Element Templates for:
• User Tasks
• Service Tasks
• Message Throw Events
These Element Templates simplify modeling so that the integration of BPMN Models with existing
models is achievable without in-depth knowledge of BPMN Modeling with the Camunda Modeler.
User Tasks
User Tasks can be used to load a Document and display that Document in view defined by a Form
Model. This means that modelers can define the Fields, Validation Rules and Computation Rules for
this Document as normal, in the Document Model.
When the Document is loaded in the Application, the UI that you see is defined by the Form Model.
As a result, all the features that you are familiar with in your application will be provided in the
Workflows context per-default.
Button events that are used to move the process forward can also be used to trigger a Validation of
the current data before the process leaves the Task.
Service Tasks
Service Tasks can be used to trigger Delegates. A range of Delegates are provided by Workflows that
allow you to perform different tasks automatically:
• Create and modify Documents
• Create, modify and delete Relationship Links
• Read Field Values and write them into process variables
• Send Emails
These Delegates can be modeled using an Element Template specifically designed for the Delegate
that you choose.
NOTE
Modifying the value of a status Field allows you to create a range of effects.
• This status can be referenced in Validation or Computation Rules that you model
so that Rules can only be triggered when the status Field has a specific value.
• This status can be used for Attribute-Based Access Control (ABAC).
Message Throw Events
An Element Template is provided for Message Throw Events to allow a Message that contains all
3

-- 3 of 69 --

process variables to be quickly modeled.
Exclusive Gateways
As Field Values can be written into process variables, this means that the next step in the process
can depend on Field Values (and other process variables).
DMN Models
DMN Decision Tables provide the logic for evaluating process variables. By writing Field Values into
process variables using a Workflows Delegate, you can effectively reference Field Values from your
Document in DMN Models.
This can be used in a number of different ways, for example:
• The Document status depends on the current status Field Value (and other process variables)
• The next step in the process depends on Field Values (and other process variables)
NOTE Process Variables can be combined from a wide range of sources and services
How Can I Apply Workflows to Different Use-Cases?
Workflows can be applied to any use-case where well-defined business processes should be
executed.
Workflows provide Delegates and Element Templates that cover a range of standard integration
requirements. This means that the majority of business processes can be easily integrated into your
application.
TIP
Using Workflows does not lead to any restriction on the Model Elements can be used
in the BPMN Models.
However, certain combinations of Model Elements need to be used with care as
documented in Current BPMN Limitations.
Workflows currently allows integration of the following A12 Models into a process:
• Document Models
◦ including Heterogeneous Subtypes
• Form Models
• Overview Models
• Relationship Models
• Composed Document Models (CDMs)
◦ including Form and Overview Models that reference CDMs
4

-- 4 of 69 --

What Do Workflows Look Like in the UI?
Workflows provide a TaskList View that can be used in the Application Model. The TaskList allows
Workflows specific Events to be used.
In addition, the Documents shown in an Overview in the TaskList View will be filtered based on the
following criteria:
• Documents must be used in an incomplete process
• The process must be waiting at a User Task
This means that Documents are only shown in an Overview in the TaskList View when the end user
needs to work on them.
The Form Model that will be used to display the Document will be determined by the Process logic
defined in the BPMN Model.
Example
Let’s consider a process where invoices are prepared for customers:
• The billing department knows the customer’s details.
• The workman knows what services were provided.
• A supervisor needs to check the invoice
Three different Documents of the same type are visible in the Overview as they are waiting at
different User Tasks in the process.
Table 1. Documents in the Overview
Document
Model
Document ID User Task Form Model Referenced in
Task
Invoice_DM Invoice_DM/11 Enter Customer Details Invoice_CustomerDetails_FM
Invoice_DM Invoice_DM/22 Enter Services Provided Invoice_Services_FM
Invoice_DM Invoice_DM/33 Review Invoice Invoice_Review_FM
Clicking on each Document caused a different Form Models to be rendered which is specific for the
Task that needs to be completed.
How Do Workflows Compare to Other A12 Models?
In general, Workflows allow the integration of new model types, BPMN and DMN models, into your
application. As such it is not easy to compare Workflows to other A12 Models.
The following table highlights where similar functionality can be found in A12 Models
Table 2. Workflows Functionality in other A12 Models
5

-- 5 of 69 --

Workflows
Functionality
Comparable
A12
Functionality
Model Comparison
Event - startProcess Event - add Overview
Model
"startProcess" starts a new process instance
in the process engine.
When the process contains a Task that uses
"User Task Creating A New Document" or
"Create Document Delegate Template",
"startProcess" works similarly to "add" as a
new Document will be created which can be
displayed in the Overview.
In contrast to "add", the Form will not be
opened per-default.
Event - proceed Event -
event_submit
Form Model Like "event_submit", "proceed" saves the
Document and closes the current Form
Model scene.
"proceed" causes the process to leave the
User Task.
setStatusDelegate Initial Value
or Dependent
Fields
Form Model Like Initial Value or Dependent Fields, the
setStatusDelegate can set the Value of a non-
computed Field.
However, Initial Values are limited to new
Documents and Dependencies need to be
triggered by a Field Value change in the
Form.
The setStatusDelegate changes the Field
Value directly in the Database. As a result,
this value is already set to the desired value
when the Document is loaded into the Form.
Delegates for
Relationship Links
Bindings Form Model Bindings allow Relationship Links to be
manually managed in a Form by the end
user.
The Delegates for Relationship Links allow
Relationship Links to be modified via a
process.
6

-- 6 of 69 --

Workflows
Functionality
Comparable
A12
Functionality
Model Comparison
DMN Decision Tables Computation
Tables
Document
Model
Like Computation Tables, DMN Decision
Tables describe the conditions under which
a certain result should be returned.
In contrast to Computation Rules which can
only reference Fields and Field Values from
the current Document Model, DMN Decision
Tables reference process variables that can
be created from a wide range of sources and
services.
User Task Form keys Match
Conditions
Application
Model
Match Conditions define which (Form)
Model should be used in the current activity.
User Task Form keys allow the Form Model
to be specified for each User Task in the
Process.
As a result, different Form Models can be
loaded for the same Document in the same
activity under the same Match Conditions.
Step-by-Step Instructions
Step 1: Plan Your Workflow
When planning the Workflow that you want to add to your application, the integration of the BPMN
and DMN Models with other Models must be considered.
For example, when you add a User Task to your BPMN diagram, you will need to model:
• A Document Model
◦ Fields for Workflows Metadata
• A Form Model
◦ A Form Model Event to complete the User Task
NOTE
When using the Preview App, the file structure in your workspace is important. All
models should be created in a folder called "models".
BPMN and DMN Models should be added to a subfolder of "models" called
"workflows".
NOTE From Step 2 onwards, BPMN Model is built up step-by-step to allow each new
7

-- 7 of 69 --

feature to be tested in the Preview Application.
The intermediate BPMN Models therefore show fewer model elements than
planned.
See How to Test and Troubleshoot Your Models for more details on how to test your
models.
Step 1a: Certification Process
Figure 2. BPMN Model of the Certification Process
Based on the requirements stated in the Use-case the Certification process contains:
• A User Task to take the Certification Test
• A User Task to check the Marks that were given
• Message Events to communicate with the Step 1c: Marking Process
• Service Tasks to update a Status Field
NOTE This is similar to using a State Machine
◦ Users can see the status of their process
◦ Validation Rules can reference this Status Field
Table 3. Models and Model Elements in the Certification Process
Model Type
and Name
Key Model Elements Notes
BPMN |
Certification
User Task | Take Certification
Test
Create a new document to persist data
User Task | Final Decision Access an existing document
Message Event Pass information on the document that is being
worked on
Call Activity | Set Status Update Field Value, see Step 1b: Status Change
Process
8

-- 8 of 69 --

Model Type
and Name
Key Model Elements Notes
Document
Model |
Certification_D
M
Certification Test Questions Fields need to be added so the user can enter
their answers
Status Field Match Field Values between the Service Task
and Data Type Configuration
Ensure Status Field is visible to the Workflows
process
3 Status Field Values are planned
Workflows Meta Data Technical Fields must be added
Form Model |
Certification_F
M
Controls Controls must be added to enter answers
Event The User Task must be completed
Form Model |
CertificationMa
rked_FM
Binding The data from the linked Marking Document
must be visualized
Event The User Task must be completed
Event A message may be sent to request re-marking
Overview
Model |
CertificationTa
skList_OM
Columns Display Field Values from the Documents lists
Event The Document must be created and the
Workflows process started
Step 1b: Status Change Process
Figure 3. BPMN Model of the Status Change Process
The Status process allows a Field in the Document Model to be updated with a new value. This Field
value can then be displayed to the end user so that they can see whether their certification test is
waiting to be marked, marked or accepted.
As a result, the Status process contains:
• A Service Task to synchronize the process variables with the Field values in the Document
9

-- 9 of 69 --

• A Business Rule Task to determine the new Status Field value
• A Service Task to update a Status Field
NOTE Separating the Step 1b: Status Change Process from the Step 1a: Certification
Process simplifies the Certification process and helps readability.
Table 4. Models and Model Elements in the Certification Process
Model Type
and Name
Key Model Elements Notes
BPMN |
Marking
Service Task | Sync Fields Update process variables value using values
from Fields in the Document Model with the
annotation availableInProcessAs
Business Rule Task | Decide
Status (Optional)
Evaluate the process variables based on a DMN
Table and output the new Status Field value
Service Task | Set Status Update Field Value
DMN
(Optional)
Decision Table | Decision Status Decision Logic to determine the new Status Field
Value
NOTE
The Document Reference will be passed to the Service Process when it is called.
The Document Models and Documents will be created in separate processes.
Step 1c: Marking Process
Figure 4. BPMN Model of the Marking Process
The Marking Process allows the user to view some data from the linked Certification Document and
then give a mark. As a result, the Marking Process contains
• A Service Task to create a new Marking Document
• Service Tasks to link the Marking Document with the Certification Document
• Service Tasks to update a Status Field
• A User Task to give the mark
• Message Events to communicate with the Step 1a: Certification Process
Table 5. Models and Model Elements in the Marking Process
10

-- 10 of 69 --

Model Type
and Name
Key Model Elements Notes
BPMN |
Marking
Service Task | Create Approval
Doc
Create a new document to persist data
Service Task | Create Link to
History (Optional)
1:n Relationship that allows multiple Marking
Documents to be linked with the Certification
Document
Service Task | Link Approval
Doc
1:1 Relationship that allows one Marking
Documents to be linked with the Certification
Document
Service Task | Re-Link Mark Replace the currently linked Marking Document
with a new Document
Service Task | Set Status Update Field Value
User Task | Give a Mark Access an existing document and update it
Message Event Pass information on the document that is being
worked on
Document
Model |
Mark_DM
Marks Fields need to be added so the user can enter the
mark that the Certification Test should receive
Status Field Match Field Values between the Service Task
and Data Type Configuration
Ensure Status Field is visible to the Workflows
process
1 Status Field Value is planned
Validation Rule Ensure that a Mark is given
Workflows Meta Data Technical Fields must be added
Form Model |
Mark_FM
Controls Controls must be added to enter marks
Event The User Task must be completed
Overview
Model |
MarkTaskList_
OM
Columns Display Field Values from the Documents lists
Relationship
Model |
CertificationMa
rk
1:1 Relationship between
Mark_DM and Certification_DM
Link the Mark Document with the respective
Certification Document that was created to the
in the Step 1a: Certification Process
Relationship
Model |
CertificationMa
rk (Optional)
1:n Relationship between
Mark_DM and Certification_DM
Link multiple Mark Documents with the
respective Certification Document that was
created to the in the Step 1a: Certification
Process
11

-- 11 of 69 --

▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
Figure 5. BPMN Model to Be Used in Final Step
Step 2: Start a Workflow Process
In this Step we will create an executable process so that you can:
• Start a Task
• Create a new Document
• Edit the Document in a User Task
in the Preview App.
Step 2a: Model an Executable Process
12

-- 12 of 69 --

Figure 6. Setting the Process to be "Executable"
NOTE All BPMN and DMN Models are added to a subfolder of "models" called "workflows"
so to ensure that this workspace is compatible with the Preview App.
• Create a new BPMN Model in the Camunda Modeler.
• Add a Pool to the BPMN Model as planned in Step 1, Step 1a: Certification Process.
• Click on the Pool.
◦ Check the "Executable" checkbox.
◦ Set the "Process ID" to a semantic name with no underscores. We will set it to "Certification".
TIP This is documented in the Workflow Modeling documentation under "Start a Process
From an Overview".
Step 2b: Create a New Document in the User Task
Figure 7. Applying the "User Task Creating A New Document" Template to a User Task
• Use the Simple Model Editor to model Certification_DM as planned in Step 1, Step 1a:
Certification Process.
◦ Model at least one Field that represents a certification question.
◦ (Optional) Add more Fields and Validation Rules to make a complex certification quiz.
• Use the Simple Model Editor to model Certification_FM as planned in Step 1, Step 1a:
Certification Process.
◦ (Optional) Use the "Build Screens From Fields" to quickly create a Form Model which
references all Fields in Certification_DM.
• Switch to the Camunda Modeler.
13

-- 13 of 69 --

• Add a User Task to the Flow in the Certification Pool.
◦ Add a Task to the BPMN Model.
◦ Use the settings to change the element to a User Task.
• Add the "User Task Creating A New Document" Template.
◦ Click on the User Task.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "User Task Creating A New Document" Template from the list provided.
• Configure the User Task by adding the Output Document Reference.
◦ Let’s call the Process Variable for the Document Reference, "NewCertification" by entering
"NewCertification" in the field labeled "Process Variable Name For New Document
Reference".
• Reference your Form Model in the "Forms" section.
◦ Select "Embedded or External Task Forms" in the "Type" field.
◦ Copy the Form Model name, "Certification_FM", into the "Form key" field.
• Save your BPMN Model.
NOTE If you cannot assign the template, please check the Tutorial prerequisites.
TIP Copy and Paste references to A12 Models to avoid typos.
TIP This is documented in the Workflow Modeling documentation under "User Task
Creating A New Document".
Step 2c: Add Workflows Metadata Fields to Document Model
Figure 8. Including Workflows Metadata
• Add the Workflows Metadata to your workspace.
◦ Download the Workflows Metadata from the Workflows Documentation.
◦ Move the Workflows Metadata file into the "models" folder of your workspace.
◦ Switch to the Simple Model Editor.
◦ Click on "Reload Workspace" in the "Workspace Explorer" of your Simple Model Editor.
14

-- 14 of 69 --

• Open WorkflowsMetadata_DM and check the "Roles" displayed in "Settings" match those that
you are using in your Workspace
• Include the Workflows Metadata in Certification_DM.
◦ Open Certification_DM in the Simple Model Editor.
◦ Right-click or use the "Open Menu" Button to add an Include to a Group.
◦ Select "WorkflowsMetadata_DM" as the "Document Model".
◦ Apply the changes and Save the Document Model.
NOTE As we are referencing this Document Model in a Relationship Model, the Include
may not be added to the Document Model root.
CAUTION
The include must be called exactly A12WF.
This is the default name when adding the Include. You should not change the
"Name" of the Include.
TIP This is documented in the Workflow Modeling documentation under "Metadata
Include".
Step 2d: Add "proceed" Action to Form Model
Figure 9. Modeling a "proceed" Button Event
• Open Certification_FM in the Simple Model Editor.
• Add a Button to the Footer.
◦ Click on "Settings".
◦ Select the "Subheader and Footer" tab.
◦ Add a Button to the Footer.
• Configure the Button.
◦ Enter a "Name" and select the "Event" in "Button Functions", "Type".
◦ Click on "Button Functions", "Event" and type "proceed" into the Field.
◦ Continue modeling the Button as normal.
◦ Apply the changes and Save the Form Model.
15

-- 15 of 69 --

TIP This is documented in the Workflow Modeling documentation under "Complete a User
Task".
Step 2e: Add "startProcess_<ProcessID>" Action to Overview Model
Figure 10. Modeling a "startProcess" Button Event
• Use the Simple Model Editor to model CertificationTaskList_OM as planned in Step 1, Step 1a:
Certification Process.
◦ Model at least one Column that references a Field Value.
◦ (Recommended) Add Columns that reference Metadata Fields.
▪ Task ID
▪ Creation Date
• Add a Button to the Subheader.
◦ Click on "Custom Actions".
◦ Add a Button to the Subheader.
• Configure the Button.
◦ Select "Button" in "Action Type".
◦ Click on "Button Functions", "Event" and type "startProcess_Certification" into the Field.
◦ Continue modeling the Button as normal.
◦ Apply the changes and Save the Overview Model.
CAUTION
The Button event syntax is startProcess_<PROCESS_ID>.
The <PROCESS_ID> must exactly match the Process ID that you defined in Step 2a:
Model an Executable Process
TIP This is documented in the Workflow Modeling documentation under "Start a Process
From an Overview".
Step 2f: Add Task List to Application Model
16

-- 16 of 69 --

Figure 11. Adding the Module to the Application Model
• Open PreviewApp_AM in the Simple Model Editor.
• Add a Module by clicking "Add" in the "Modules" section.
◦ Enter a "Name" for the Module and the "Menu".
◦ Add the following Activity Descriptor:
{
"view": "TaskList",
"model": "Certification_DM",
"module": "CertificationTaskList"
}
◦ Add Labels for each locale.
• Add a Flow by clicking "Add" in the "Flow" section.
◦ Enter "TaskFlow" as the "Name".
• Add the Overview Scene by clicking "Add" in the "Scenes" section.
◦ Enter a "Name" for the Scene.
◦ Add Match Conditions that match the Activity Descriptor above:
Table 6. Match Conditions for Overview Scene
Key Must Equal Is Set
model Certification_DM
module CertificationTaskList
instance false
• Add two Scene Changes by clicking "Add" in the "On Enter" section.
1. Add a Scene Change and select "REGION_CLEAR" in "Type".
▪ Enter "MasterDetail" in the "Layout" "Name".
2. Add a Scene Change and select "VIEW_ADD" in "Type".
▪ Enter "OverviewEngine" in the "Name".
▪ Click "Add" in the "Models" section.
17

-- 17 of 69 --

▪ Select "Overview" as the "Model Type".
▪ Select "CertificationTaskList_OM" as the "Name".
▪ Apply all the changes and Save the Application Model.
NOTE
The Task List will display Documents from running Process Instances where the
Process Instance is waiting at a User Task.
All other Documents that reference the Certification_DM will be filtered out and not
displayed.
TIP This is documented in the Workflow Modeling documentation under "Overview
Scene".
Step 2g: Enable Workflows in the Preview Application
Figure 12. Enabling Workflows in "workspace.json"
• Open your Workspace in your File Explorer.
• Open the File "workspace.json".
TIP
"workspace.json" is created automatically when you create a new Workspace using
the Preview App Control.
"workspace.json" is saved as a sibling of the "models" folder.
• Add "enableWorkflows": true to file so that the contents of "workspace.json" is as follows:
{
"enableWorkflows": true
}
TIP You can save all your models and start testing what you have modeled so far. See How
to Test and Troubleshoot Your Models for more details.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
18

-- 18 of 69 --

BPMN Model, Certification_DM, Certification_FM, CertificationTaskList_OM, PreviewApp_AM
Figure 13. BPMN Model With a Single User Task
Figure 14. Certification Pool in BPMN Model
19

-- 19 of 69 --

Figure 15. "User Task Creating A New Document" Creating the New Document at the Start of the Process
Figure 16. Certification_DM
20

-- 20 of 69 --

Figure 17. Subheader and Footer on Certification_FM
Figure 18. proceed Event in Subheader and Footer on Certification_FM
21

-- 21 of 69 --

Figure 19. CertificationTaskList_OM
Figure 20. Custom Actions on CertificationTaskList_OM
22

-- 22 of 69 --

Figure 21. startProcess Event in Custom Actions on CertificationTaskList_OM
Figure 22. PreviewApp_AM
23

-- 23 of 69 --

Figure 23. Module on PreviewApp_AM
Figure 24. Flow in Module on PreviewApp_AM
24

-- 24 of 69 --

Figure 25. Scene in Flow in Module on PreviewApp_AM
Figure 26. REGION_CLEAR Scene Change in Scene in Flow in Module on PreviewApp_AM
25

-- 25 of 69 --

Figure 27. VIEW_ADD Scene Change in Scene in Flow in Module on PreviewApp_AM
Figure 28. workflows.json in the Workspace Folder
Step 3: Update Status
In this step we will use Service Tasks to update the status of our Document automatically.
This is required if you want to use the Context-Based Validation Rules that we plan to add in Step 6.
26

-- 26 of 69 --

Step 3a: Model a Call Activity for Status Updates
Figure 29. Modeling a Call Activity
• Switch to the Camunda Modeler.
• Model an executable Status Change Process as planned in Step 1b: Status Change Process.
◦ (Optional) Create a new BPMN Model for the Status Change Process.
◦ Add a Pool for the Status Change Process.
◦ Click on the Pool.
▪ Check the "Executable" checkbox.
▪ Set the "Process ID" to a semantic name with no underscores. We will set it to
"StatusChange".
• Add a Call Activity to the Flow in the Certification Pool.
◦ Add a Task to the BPMN Model.
◦ Use the settings to change the element to a Call Activity.
◦ Add the following setting to the "Called element" section of the Properties Panel.
▪ Select "BPMN" as the "Type".
▪ Add the Status Change Process ID, "StatusChange" to the "Called element" field.
▪ Pass the Process Variable "NewCertification" that we created in Step 2b: Create a New
Document in the User Task to the Status Change Process by either selecting "Propagate
all variables" or by using "In Mappings".
• Save your BPMN Model.
Step 3b: Make Fields Available to Process
27

-- 27 of 69 --

Figure 30. Annotating Fields
• Switch to the Simple Model Editor and open Certification_DM.
• Add a Status Field to the Document Model as planned in, Step 1a: Certification Process.
TIP As the Set Status Process will be called three times in the final process, I
recommend modeling an Enumeration Field with at least three different values.
• Add an annotation to the Status Field.
◦ Click "Add" in the "Annotations" section of the Field Editor.
◦ Enter "availableInProcessAs" as the "Name".
◦ Enter "CertificationStatus" as the "Value".
• (Optional) Add Annotations to any other Fields that should be synchronized.
• Apply the changes and Save the Document Model.
NOTE
The Value of the Status Field will now be saved as a Process Variable with the Name
"CertificationStatus" under a range of conditions.
Please check the documentation for more information.
TIP This is documented in the Workflow Modeling documentation under "Synchronize
Field Values".
Step 3c: Sync Field Values to Process Variables
Figure 31. Applying the "Sync Available Fields Delegate Template" to a Service Task
• Add the Service Task to Sync the Field Values as planned in Step 1b: Status Change Process.
28

-- 28 of 69 --

◦ Add a Task to the BPMN Model.
◦ Use the settings to change the element to a Service Task.
• Add the "Sync Available Fields Delegate Template".
◦ Click on the Service Task.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "Sync Available Fields Delegate Template" from the list provided.
• Configure the Service Task by adding our Document Reference.
◦ Reference the Process Variable, NewCertification, as an expression by entering
"${NewCertification}".
TIP This is documented in the Workflow Modeling documentation under "Sync Available
Fields Delegate Template".
Step 3d: Set Status Field Value
Figure 32. Applying the "Set Status Delegate Template" to a Service Task
• Add the Service Task to Set the Status as planned in Step 1b: Status Change Process.
◦ Add a Task to the BPMN Model.
◦ Use the settings to change the element to a Service Task.
• Add the "Set Status Delegate Template".
◦ Click on the Service Task.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "Set Status Delegate Template" from the list provided.
• Configure the Service Task by adding the following information.
◦ Reference the Process Variable, NewCertification, as an expression by entering
"${NewCertification}" to the "Document Reference".
◦ Enter the "New Value" as a String (fixed value) or an Expression (see optional DMN
Modeling).
◦ Copy the Status Field Path from the Document Model and paste it into the "Path to Field".
• (Optional) Model a DMN Model and add a Business Rule Task to determine the Status Field
Value.
29

-- 29 of 69 --

◦ Use the Process Variables you have including the Field Values that you have just
synchronized to determine the new Field Value.
◦ Output a Result Variable to be used in the "New Value" setting of the Service Task.
• Save your BPMN Model.
NOTE Adding the optional Business Rule Task and DMN Model will make Step 6 quicker
and easier.
TIP This is documented in the Workflow Modeling documentation under "Set Status
Delegate Template".
TIP You can save all your models and start testing what you have modeled so far. See How
to Test and Troubleshoot Your Models for more details.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
No new models
Figure 33. BPMN Model With Separate Process for Status Updates
30

-- 30 of 69 --

Figure 34. "Sync Available Fields Delegate Template" in Status Change Process
Figure 35. Status Field on Certification_DM
31

-- 31 of 69 --

Figure 36. Optional DMN Model to Determine the Status Field Value Using Two Synchronized Field
Values
Figure 37. Optional Business Rule Task to Output the New Status Field Value as a Process Variable
32

-- 32 of 69 --

Figure 38. "Set Status Delegate Template" Referencing Process Variable from Optional DMN Model
Step 4: Trigger Marking Process
In this step we will use Message Events to automatically trigger a second process when the
Certification reaches a certain point.
NOTE
You have already performed many of the modeling steps needed to complete this
step in Step 2. As a result, the step-by-step guide in this section will focus on the
modeling steps that are new or different.
Step 4a: Model Throw and Catch Events
Figure 39. Applying the "Template For Message Throw Event" to a Message Throw Event
• Add the Message Intermediate Throw Event to Certification as planned in Step 1a: Certification
Process.
◦ Add an Intermediate Event to the BPMN Model.
33

-- 33 of 69 --

◦ Use the settings to change the element to a Message Intermediate Throw Event.
• Add the "Template for Message Throw Event".
◦ Click on the Message Intermediate Throw Event.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "Template for Message Throw Event" from the list provided.
• Configure the Message Throw Event.
◦ Enter a "Message Name to correlate" in the "Inputs" section of the Properties Panel. Let’s
enter "ToMarking" into the "Value" field.
NOTE
This Template throws all the current Process Variables as part of the
Message.
As a result, the Process Variable, "NewCertification", will be present in the
Marking Process. This will be useful in Step5 when we want to link the two
documents.
Adding a Message Intermediate Catch Event will cause the Certification Process to wait until it
receives the message to proceed.
NOTE
Process instances that are waiting at a User Task are visible in the Task Overview
that you modeled in Step 2.
Process instances that are waiting at any other BPMN Model element will not be
displayed in the Task Overview.
• Add the Message Intermediate Catch Event to Certification as planned in Step 1a: Certification
Process.
◦ Add an Intermediate Event to the BPMN Model.
◦ Use the settings to change the element to a Message Intermediate Catch Event.
• Configure the Message Catch Event.
◦ Select "Create new…" as the "Global message reference" in "Message" section of the
Properties Pane.
◦ Let’s enter "FromMarking" into the "Name" field.
We can now repeat these steps for the Message Events planned in Step 1c: Marking Process.
• Model an executable Marking Process.
◦ (Optional) Create a new BPMN Model for the Marking Process.
◦ Add a Pool for the Marking Process.
◦ Click on the Pool.
▪ Check the "Executable" checkbox.
▪ Set the "Process ID" to a semantic name with no underscores. We will set it to "Marking".
34

-- 34 of 69 --

• Model the Message Start Event in the Marking Process.
◦ Add the "ToMarking" as the "Message Name" to match the Message Intermediate Throw
Event from the Certification Process.
• Model the Message Intermediate Throw Event using the "Template for Message Throw Event" in
the Marking Process.
◦ Enter "FromMarking" in the "Message Name to correlate" in the "Inputs" section of the
Properties Panel. This name matches the Message Intermediate Catch Event from the
Certification Process.
TIP This is documented in the Workflow Modeling documentation under "Template For
Message Throw Event".
Step 4b: Add a Task to Create a Document
Figure 40. Applying the "Create Document Delegate Template" to a Service Task
The Document, Form and Overview Model should be modeled as planned in Step 1c: Marking
Process.
• Switch to the Simple Model Editor.
• Model Mark_DM.
• Model Mark_FM.
◦ Add the proceed Action on the Form as you did in Step 2d: Add "proceed" Action to Form
Model.
• Model Mark_OM.
TIP As we are starting the new process instance using a Message Start Event, you do not
need to model the startProcess Event that you did previously.
Now that the preparation is done, we can integrate these models into the Workflow.
• Switch to the Camunda Modeler.
• Add the Service Task to Create the Approval Document as planned in Step 1c: Marking Process.
◦ Add a Task to the BPMN Model.
◦ Use the settings to change the element to a Service Task.
35

-- 35 of 69 --

• Add the "Create Document Delegate Template".
◦ Click on the Service Task.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "Create Document Delegate Template" from the list provided.
• Configure the Service Task by adding the following information.
◦ Reference the Document Model by entering "Mark_DM" as the "Document Model Name".
◦ Let’s call the Process Variable for this new Document, "NewMark" by entering "NewMark" in
the field labeled "Process Variable Name For New Document Reference".
TIP This is documented in the Workflow Modeling documentation under "Create
Document Delegate Template".
Step 4c: Modify the Existing Document in a User Task
Figure 41. Applying the "User Task With Input Document" Template to a User Task
• Add the User Task to Give a Mark.
◦ Add a Task to the BPMN Model.
◦ Use the settings to change the element to a User Task.
• Add the "User Task With Input Document" Template.
◦ Click on the User Task.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "User Task With Input Document" Template from the list provided.
• Configure the User Task by adding the Input Document Reference.
◦ Reference the Process Variable, NewMark, as an expression by entering "${NewMark}".
• Reference your Form Model in the "Forms" section.
◦ Select "Embedded or External Task Forms" in the "Type" field.
◦ Copy the Form Model name, "Mark_FM", into the "Form key" field.
• Save your BPMN Model.
TIP This is documented in the Workflow Modeling documentation under "User Task With
Input Document".
36

-- 36 of 69 --

Step 4d: Add Task List to Application Model
Figure 42. Adding the Module to the Application Model
Adding the Task List for the Marking Process is very similar to the steps that you followed in Step
2f: Add Task List to Application Model.
TIP
Copying the existing Task might save some time as the Modules are so similar. If you
do this, make sure to adapt:
• Module Names and Labels
• Activity Descriptor
• Scene Name and Match Conditions
• Overview Model in the VIEW_ADD Scene Change
• Open PreviewApp_AM in the Simple Model Editor.
• Add a Module by clicking "Add" in the "Modules" section.
◦ Enter a "Name" for the Module and the "Menu".
◦ Add the following Activity Descriptor:
{
"view": "TaskList",
"model": "Mark_DM",
"module": "MarkTaskList"
}
◦ Add Labels for each locale.
• Add a Flow by clicking "Add" in the "Flow" section.
◦ Enter "TaskFlow" as the "Name".
• Add the Overview Scene by clicking "Add" in the "Scenes" section.
◦ Enter a "Name" for the Scene.
◦ Add Match Conditions that match the Activity Descriptor above:
Table 7. Match Conditions for Overview Scene
37

-- 37 of 69 --

Key Must Equal Is Set
model Mark_DM
module MarkTaskList
instance false
• Add two Scene Changes by clicking "Add" in the "On Enter" section.
1. Add a Scene Change and select "REGION_CLEAR" in "Type".
▪ Enter "MasterDetail" in the "Layout" "Name".
2. Add a Scene Change and select "VIEW_ADD" in "Type".
▪ Enter "OverviewEngine" in the "Name".
▪ Click "Add" in the "Models" section.
▪ Select "Overview" as the "Model Type".
▪ Select "MarkTaskList_OM" as the "Name".
▪ Apply all the changes and Save the Application Model.
TIP This is documented in the Workflow Modeling documentation under "Overview
Scene".
TIP You can save all your models and start testing what you have modeled so far. See How
to Test and Troubleshoot Your Models for more details.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
Mark_DM, Mark_FM, MarkTaskList_OM
38

-- 38 of 69 --

Figure 43. BPMN Model With Message Events to Trigger the Marking Process
Figure 44. "Template For Message Throw Event" to Trigger Marking Process
39

-- 39 of 69 --

Figure 45. Message Catch Event at the Start of the Marking Process
Figure 46. "Create Document Delegate Template" to Create the Marking Document
40

-- 40 of 69 --

Figure 47. "User Task With Input Document" to Give the Mark
Figure 48. Mark_DM with Workflows Metadata
41

-- 41 of 69 --

Figure 49. Mark_FM with proceed Action
Figure 50. MarkTaskList_OM
42

-- 42 of 69 --

Figure 51. PreviewApp_AM
Figure 52. Module on PreviewApp_AM
43

-- 43 of 69 --

Figure 53. Flow in Module on PreviewApp_AM
Figure 54. Scene in Flow in Module on PreviewApp_AM
44

-- 44 of 69 --

Figure 55. VIEW_ADD Scene Change in Scene in Flow in Module on PreviewApp_AM
Step 5: Link Doc with Relationship
The documents from the two processes should be linked so that the Mark that is given clearly
relates to a specific Certification Quiz.
In this step, you will create the Relationship Model to allow this and use the Workflows Templates
so that the Links are automatically created.
Step 5a: Create the Relationship Model
Figure 56. Modeling "CertificationMark"
• Model the Relationship Models as planned in Step 1c: Marking Process.
• Model CertificationMark.
◦ Set the Model up to be a 1:1 Relationship.
◦ Add the following Roles.
45

-- 45 of 69 --

▪ Certification referencing Certification_DM, Multiplicity = 1.
▪ Mark referencing Mark_DM, Multiplicity = 1.
• Apply the changes, save your Relationship model and generate the Document Models.
Step 5b: Link the Documents
Figure 57. Applying the "Create Relationship Link Delegate Template" to a Service Task
• Switch to the Camunda Modeler.
• Add the Service Task to Link the Approval Document as planned in Step 1c: Marking Process.
◦ Add a Task to the BPMN Model.
◦ Use the settings to change the element to a Service Task.
• Add the "Create Relationship Link Delegate Template".
◦ Click on the Service Task.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "Create Relationship Link Delegate Template" from the list provided.
• Configure the Service Task by adding the following information.
◦ Reference the Relationship Model by entering "CertificationMark" as the "Relationship
Model".
◦ Add the Certification Document Reference by entering the expression with the process
variable, "${NewCertification}", into "Source Document Reference".
◦ Match this Document with the correct role in the Relationship Model by entering
"Certification" into "Source Role".
◦ Add the Mark Document Reference by entering the expression with the process variable,
"${NewMark}", into "Target Document Reference".
◦ Match this Document with the correct role in the Relationship Model by entering "Mark"
into "Target Role".
◦ Let’s call the Process Variable for this new Link "NewMarkLinkID" by entering
"NewMarkLinkID" in the field labeled "Process Variable Name For New Link ID".
• Save your BPMN Model.
NOTE As Relationship Links in are flat and have no direction in A12, the assignment of
Source and Target is not significant.
46

-- 46 of 69 --

However, the Document Reference must match the Document Model for that Role:
• Source Document Reference matches the Document Model used in the Source
Role.
• Target Document Reference matches the Document Model used in the Target
Role.
TIP This is documented in the Workflow Modeling documentation under "Create
Relationship Link Delegate Template".
Step 5c: Model a Binding to View the Linked Data
Figure 58. Modeling a Binding on "Mark_FM"
• Switch to the Simple Model Editor.
• Add a Binding to Mark_FM.
◦ Select the Table List Component.
◦ Set "Has Edit Modal" to "no".
◦ Model the Selected Items Overview Model so that the Marker can see answers to the
Certification Questions in the Table List.
◦ Apply the changes and save your Form Model.
Step 5d: Add an Extra User Task to Check Mark
Figure 59. Applying the "User Task With Input Document" Template to a User Task
• Make a copy of Certification_FM so that we can check the Mark as planned in Step 1a:
Certification Process.
47

-- 47 of 69 --

◦ Copy Certification_FM and name the copy CertificationMarked_FM.
◦ Set the Certification Quiz Controls to be read-only.
◦ Add another Table List Binding as you did in Step 5c: Model a Binding to View the Linked
Data.
• Switch to the Camunda Modeler.
• Add a User Task to check the Mark using the "User Task With Input Document" as you did in
Step 4c: Modify the Existing Document in a User Task
◦ Add a Task to the BPMN Model.
◦ Use the settings to change the element to a User Task.
• Add the "User Task With Input Document" Template.
◦ Click on the User Task.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "User Task With Input Document" Template from the list provided.
• Configure the User Task by adding the Input Document Reference.
◦ Reference the Process Variable, NewCertification, as an expression by entering
"${NewCertification}".
• Reference your Form Model in the "Forms" section.
◦ Select "Embedded or External Task Forms" in the "Type" field.
◦ Add the Form Model name that you just created, "CertificationMarked_FM", into the "Form
key" field.
• Save your BPMN Model.
TIP This is documented in the Workflow Modeling documentation under "User Task With
Input Document".
TIP You can save all your models and start testing what you have modeled so far. See How
to Test and Troubleshoot Your Models for more details.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
CertificationMark, CertificationMarked_FM with the respective \___generated Document Models
and Binding Overview Models
48

-- 48 of 69 --

Figure 60. BPMN Model With Tasks to Create and Check a Relationship Link
Figure 61. CertificationMark Relationship Model
49

-- 49 of 69 --

Figure 62. "Create Relationship Link Delegate Template" to Link the Certification and Mark Documents
Figure 63. "User Task With Input Document" to Display the Mark
50

-- 50 of 69 --

Figure 64. Mark_FM with a Table List Binding
Figure 65. CertificationMarked_FM with a Table List Binding
Step 6: Context-Based Validation
In this step you will update link Validation Rules to the Status Field Value. As you can update the
Status Field Value automatically in the Workflows process, this allows you to integrate a State
Machine into the Workflow.
51

-- 51 of 69 --

For example, when the Status Field Value is "A", Field A must be filled.
CAUTION
The "Set Status Delegate Template" does not trigger a Full Validation of the
Document.
By referencing the Status Field Value in Validation Rules and then automatically
setting the Status Field Value you will probably create an invalid Document.
This is by design so that the Validation Rules can be triggered in the next User
Task.
Invalid Documents should be resolved as soon as possible. This normally means
modeling a User Task directly after the Service Task.
Step 6a: Add Additional Status Changes in Certification Process
Figure 66. Modeling Additional Status Changes
If you chose to add the optional Business Rule Task and DMN Model in Step 3 and have planned for
three different status values in your Document Model and DMN Model you can simply copy and
paste the Call Activity.
NOTE
If not, please ensure that you can set three different values. You could also achieve
this using:
• Inputs on the Call Activity.
• "Set Status Delegate Template" in Service Tasks instead of the Call Activity. This
is described in more detail in the next step.
• Add Tasks to perform the three Status Field Value changes planned in Step 1a: Certification
Process.
◦ Copy and Paste the Set Status Call Activity that you have already modeled and let the
Business Rule Task and DMN Model work their magic.
Step 6b: Add Status Change to Marking Process
52

-- 52 of 69 --

Figure 67. Applying the "Set Status Delegate Template" to a Service Task
• Add the Service Task to Set the Status as planned in Step 1c: Marking Process.
◦ Add a Task to the BPMN Model.
◦ Use the settings to change the element to a Service Task.
• Add the "Set Status Delegate Template".
◦ Click on the Service Task.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "Set Status Delegate Template" from the list provided.
• Configure the Service Task by adding the following information.
◦ Reference the Process Variable, NewMark, as an expression by entering "${NewMark}" to
the "Document Reference".
◦ Enter a "New Value" as a String (fixed value). Let’s set the Value to be "ToMark".
◦ Copy the Status Field Path from the Document Model and paste it into the "Path to Field".
• Click "Async before" in the "Async Properties".
• Save your BPMN Model.
NOTE
Setting the Asynchronous Continuations to before creates a "savepoint" in the
process.
Whilst the process will roll-back to this point in the case of an error, changes to
Documents will not be reverted.
Step 6c: Model Context-Based Validation Rules
53

-- 53 of 69 --

Figure 68. Modeling a Validation Rule
As planned in Step 1c: Marking Process, we need to model a Validation Rule to ensure that a mark is
given.
• Switch to the Simple Model Editor.
• Open Mark_DM and model the Validation Rule.
◦ Set the Mark Field as the Error Field.
◦ Add the Condition to ensure that the Mark is filled.
[Status/Status] == "ToMark" And FieldNotFilled(Mark)
◦ Enter an Error Message for each locale.
◦ Apply the changes and save your Document Model.
TIP This is documented in the Workflow Modeling documentation under "Context-Based
Validation Rules".
TIP You can save all your models and start testing what you have modeled so far. See How
to Test and Troubleshoot Your Models for more details.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
54

-- 54 of 69 --

Figure 69. BPMN Model With Status Changes for Context-Based Validation
Figure 70. Copy of Set Status Call Activity
55

-- 55 of 69 --

Figure 71. "Set Status Delegate Template" used to set the Status Field Value to "ToMark"
Figure 72. Validation Rule on Mark_DM
Step 7: Loops and Errors
So far the tutorial has shown you how to model a "happy path". The Certification Quiz is complete,
the Mark is given and with that all the processes come to an end.
This step is concerned with a deviation from the "happy path". This means modeling the
56

-- 56 of 69 --

functionality to allow a user to request that their Certification Quiz is remarked.
There are a number of different ways to create a loop back. For example, you could choose to:
1. Send a Message from the User Task.
2. Use Process Variables to set the Path at an Exclusive Gateway.
You have already Synchronised Field Value to Process Variables in Step 3 so that you could use
them in the Process. In addition, the Field Values that are available to the Process are also
synchronised when saving a User Task. As a result, let’s focus on option 1.
Step 7a: Send a Message to Request Re-Marking
Figure 73. Adding a Message Boundary Event
• Switch to the Camunda Modeler.
• Add a Message Boundary Event to the Final Decision Task as planned in Step 1a: Certification
Process.
◦ Add an Intermediate Event to the BPMN Model.
◦ Use the settings to change the element to a Message Boundary Event.
• Configure the Message Catch Event.
◦ Select "Create new…" as the "Global message reference" in "Message" section of the
Properties Panel.
◦ Let’s enter "Remark" into the "Name" field.
• Model the Gateway and Flow so that the Process Loops back.
◦ Add an Exclusive Gateway after the first User Task, Take Certification Test.
◦ Add a Flow that connects the Message Boundary Event to the Gateway. This will mean that
the Marking Process is triggered again.
This Message Boundary Event can catch a message that we send from the User Task. This means
you need to add an Action to the Form Model referenced in this User Task that sends the message.
• Switch to the Simple Model Editor and open CertificationMarked_FM.
• Model a Button to send the message.
◦ Add a Button to the Model.
• Configure the Button.
57

-- 57 of 69 --

◦ Enter a "Name" and select the "Event" in "Button Functions", "Type".
◦ Click on "Button Functions", "Event" and type "sendMessage" into the Field.
◦ Continue modeling the Button as normal.
◦ Expand the Annotations section at the bottom of the Button Editor and click "Add".
▪ Enter "messageName" into the "Name" Field.
▪ Enter the message that you modeled in the Boundary Event, "Remark" as the "Value".
◦ Apply the changes and Save the Form Model.
CAUTION
Sending a Message from the Form does not lead to the Form being saved or
validated.
This modeling solution should not be used when the end user is expected to
change a Field Value.
NOTE
The Caution above is the reason why we haven’t modeled a Context-Based
Validation Rule for the Certification Process.
If the Document was invalid when the Remark message was sent, an incident would
be raised the next time the Workflows Metadata were updated.
TIP This is documented in the Workflow Modeling documentation under "Send a Message
From a Form".
Step 7b: Add an Error Event
Figure 74. Adding an Error Boundary Event
As the Marking Process is triggered again, a second Marking Document will be linked with the
Certification Document. This will cause an incident as the Relationship Model specifies a 1:1
Relationship.
Rather than change the Relationship Model, let’s raise an Error, catch it and then change the Linked
Documents using the "Re-link Document Delegate Template".
• Switch to the Camunda Modeler.
◦ Add an Error Boundary Event to the Link Approval Doc Task as planned in Step 1c: Marking
Process.
58

-- 58 of 69 --

◦ Add an Intermediate Event to the BPMN Model.
◦ Use the settings to change the element to an Error Boundary Event.
• Configure the Error Boundary Event.
◦ Select "Create new…" as the "Global error reference" in "Error" section of the Properties
Panel.
◦ Let’s enter "Link Limit Reached" into the "Name" field.
◦ Enter "LinkLimitReachedError" in the "Code" field.
NOTE This Error Code is generated by Workflows when the Link Limit is reached and
requires no extra coding.
TIP This is documented in the Workflow Modeling documentation under "BPMN Errors".
Step 7c: Re-Link Documents
Figure 75. Applying the "Re-link Document Delegate Template" to a Service Task
We need the existing Link ID to be able to re-Link the Documents. By the time the Error is raised,
the Service Task has already overwritten the existing value of NewMarkLinkID, the Process
Variable we defined in Step 5b. As a result, we need to copy it before that happens.
• Add an Output to copy the Process Variable.
◦ Click on "Final Decision".
◦ Add an Output and enter "ExistingMarkLinkID" as the "Process variable name".
◦ Reference the NewMarkLinkID using the expression "${NewMarkLinkID}" in the "Value"
field.
You can now model the Service Task.
• Add the Service Task to Re-Link the Approval Document as planned in Step 1c: Marking Process.
◦ Add a Task to the BPMN Model.
◦ Connect the Task to the Error Boundary Event and join the Flows with an Exclusive
Gateway.
◦ Use the settings to change the element to a Service Task.
• Add the "Re-link Document Delegate Template".
59

-- 59 of 69 --

◦ Click on the Service Task.
◦ Click "Select" in the "Template" section of the Properties Panel.
◦ Select the "Re-link Document Delegate Template" from the list provided.
• Configure the Service Task by adding the following information.
◦ Add the existing Link ID by entering the expression with the process variable,
"${ExistingMarkLinkID}", into "Link ID".
◦ Reference the Relationship Model by entering "CertificationMark" as the "Relationship
Model".
◦ Add the Certification Document Reference by entering the expression with the process
variable, "${NewCertification}", into "Source Document Reference".
◦ Match this Document with the correct role in the Relationship Model by entering
"Certification" into "Source Role".
◦ Add the Mark Document Reference by entering the expression with the process variable,
"${NewMark}", into "Target Document Reference".
◦ Match this Document with the correct role in the Relationship Model by entering "Mark"
into "Target Role".
◦ Let’s use the same output variable for our Link and call the Process Variable for this new
Link "NewMarkLinkID" by entering "NewMarkLinkID" in the field labeled "Process Variable
Name For New Link ID".
• Save your BPMN Model.
TIP This is documented in the Workflow Modeling documentation under "Re-link
Document Delegate Template".
Step 7d: Model a History of Marks (optional)
Figure 76. Applying the "Create Relationship Link Delegate Template" to a Service Task
If you want to see a history of all the Marks that have been given, model a 1:n Relationship Model
as planned in Step 1c: Marking Process.
Now you can follow the steps in Step 5: Link Doc with Relationship to:
• Add the "Create Relationship Link Delegate Template" to a Service Task.
• Add Bindings to the Form Model.
60

-- 60 of 69 --

▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
Figure 77. BPMN Model With Re-Marking Loop and Delegate Error
Figure 78. Message Boundary Event with the Name "Remark"
61

-- 61 of 69 --

Figure 79. sendMessage Event on CertificationMarked_FM
Figure 80. Error Boundary Event when the Link Limit is Reached
62

-- 62 of 69 --

Figure 81. Output to Copy the Link ID Process Variable
Figure 82. "Re-link Document Delegate Template" on Service Task
63

-- 63 of 69 --

Figure 83. Optional Relationship Model CertificationMarkHistory
Figure 84. Optional "Create Relationship Link Delegate Template" on Service Task
64

-- 64 of 69 --

Figure 85. Optional Binding on CertificationMarked_FM
How to Test and Troubleshoot Your Models
You can new test your models to make sure that everything is working perfectly.
• Open the Preview App Control and select your workspace in the drop-down menu.
NOTE
Process Instances are not persisted when you stop and restart the Preview App.
Documents are also cleared from the database each time the Preview App is
restarted. If you want to retain any test data, you can save it in your Workspace by
clicking on the "Replace Workspace Data" button in the Workspace Explorer of the
Simple Model Editor.
This allows you to preserve documents, attachments and links. Process Instances
cannot be preserved in the Workspace in this manner.
Check the Simple Model Editor documentation for more information.
• Start your workspace using the Preview App Control and log in (admin:a12).
TIP
If the Preview App does not start, check your BPMN Model for errors.
Sources of errors include:
◦ The Process cannot be completed.
▪ Flows are not attached.
▪ Conditions after an Exclusive Gateway have not been modeled.
65

-- 65 of 69 --

◦ Service Tasks do not have a Template.
▪ As a result, the Service task does not call a Delegate.
◦ Message Events do not have a Message.
TIP
If the process engine does not start, check that you have adapted the
"workspace.json" as described in Step 2g: Enable Workflows in the Preview
Application.
You can use the expert mode to check what the Preview App Control is starting.
• (If necessary) Connect the Simple Model Editor to the server using the "Configure Server
Connection" tab in the top right-hand corner (admin:a12).
• (If necessary) Deploy your workspace and refresh your browser tab.
NOTE
As the Modules have been added to the Application Model, we only need to re-
deploy models from the Simple Model Editor in the case that we have made
changes.
Changes to the BPMN and DMN Models can be deployed directly from the
Camunda Modeler.
• (Optional) Open another browser tab and log into the Camunda Cockpit.
TIP You can check the status of Process Instances using the Camunda Cockpit. Please
refer to the Workflows Documentation for more details.
• Click on the Certification module in your application and apply for certification by clicking on
the startProcess Action that you added in Step 2: Start a Workflow Process.
NOTE
There is a short delay when starting a process for the first time. When you click
on the startProcess button in the Overview for a second time, the process
instance will be created significantly faster.
◦ Click on the new row that has been added to the Overview.
◦ Enter your data and Click on the proceed Action that you added in Step 2: Start a Workflow
Process.
• Switch to Marking module and check that new row has been added to the Overview due to the
Events and Tasks that you added in Step 4: Trigger Marking Process.
◦ Click on the new row to open the Form Model that you created in Step 4: Trigger Marking
Process.
◦ Check the Certification Quiz data in the Binding that you added to the Form Model in Step 5:
Link Doc with Relationship.
◦ Use the proceed Action to submit the mark.
• Switch back to the Certification module.
66

-- 66 of 69 --

◦ Click on the Document to open the Form that you added in Step 5: Link Doc with
Relationship and use the Binding to check the mark.
◦ Check that the Certification Status has been updated as modeled in Step 6: Context-Based
Validation.
◦ Click on the proceed Action to end the Process Instance.
• Repeat the Testing Process.
◦ Try and submit the Marking Form without adding a mark. Ensure that the Context-Based
Validation Rule tht you modeled in Step 6: Context-Based Validation is triggered.
◦ Loop back using the sendMessage Action that you added in Step 7: Loops and Errors. Ensure
that the Relationship Link is updated.
• (Optional) Use a Master Detail Module Model to quickly add a "normal" Overview for the
Certification and Marking Documents.
NOTE The TaskList Overviews that you added to the Application Model are filtered.
◦ Check that the Status is updated correctly at all stages of the process.
▼ Click here to see what your project should look like by now.
Figure 86. Three running processes in the Camunda Cockpit
67

-- 67 of 69 --

Figure 87. Certification TaskList
Figure 88. Entering Certification Data
68

-- 68 of 69 --

Figure 89. Giving a Mark
Figure 90. Checking the Mark in the Certification Process
Glossary
Please check the Glossary in the Workflows Documentation for more information.
69

-- 69 of 69 --

