# overall dev_tutorial_intro_modeling

Task 1 - Modeling
WARNING
This tutorial refers to an older version of A12 (2025.06-ext5) and will be
removed with the 2026.06 release. All modeling aspects will be covered in the
modeling tutorials instead.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Intro > Introduction before
continuing here.
Prior to starting, please take a look at Modeling > Model Overview for a first orientation. Then you
can check out the tag 2025.06-ext5/intro/task-1-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/intro/task-1-end to see how
your code and models differs from the solution.
Use-Case
As we are creating a CRM system, we are going to need somewhere in our application to manage
contacts. In this case, managing contacts means creating new contacts, looking through existing
ones, updating contacts with new data and deleting old contacts.
In the following task we will therefore add the ability to create, read, update and delete (CRUD)
contacts in our application. These contacts will have data associated with them such as a name,
email address, phone number etc.
How might you do this in a normal application, what components on the client and endpoints on
the server would you have to create?
On the client you might need to,
• create a form to input the contact details.
• consider,
◦ which fields are mandatory.
◦ how to notify the user of errors.
◦ how to make it accessible.
◦ how the form works on mobile.
On the server you might need to,
1

-- 1 of 54 --

• set up somewhere to store the created contacts on the server e.g. a database.
• create endpoints that will allow the user to CRUD the contact.
• think about security.
Furthermore, you would have to do all the configuration and installation to ensure that the client as
well as the server are communicating properly.
However, it does not have to be this laborious or be done by a developer. Instead, in A12 the
majority of the aspects listed above can be achieved by modeling, which we will take a closer look
at in this task.
End Result
Upon finishing this task you will,
• know how to add new features to your application without adding code.
• know how to use the A12 modeling tool - Simple Model Editor.
• have the ability to CRUD contacts.
At the end, our application should look something like the following:
Step-by-Step Instructions
Our goal in this task is to create the four models necessary for adding the functionality to our
application that lets users manage contacts in the CRM system.
These will be:
2

-- 2 of 54 --

• A Document Model, which is a blueprint for what information we want to save about the
contacts in our system.
• A Form Model, which allows us to describe how the Document Model translates into a usable
form for creating and editing contacts.
• An Overview Model, which enables us to configure a table-like list of the contacts in our system
with features like filtering, pagination, search etc.
• An Application Model, which determines layout and interaction of the form and overview as a
module in the application.
To create and edit these models, we will use the Simple Model Editor (SME) which is included in the
A12 Modeling Tools.
WARNING
For full compatibility and to be able to follow along with the screenshots easily,
please make sure that you are using the version of the A12 Modeling
Environment specified in Tutorials > Intro > Introduction > Prerequisites.
Common Properties
Upon creating each of our models, we will need to specify the following properties.
Name
The naming of the models is up to you. In the tutorial we will adhere to the model naming
conventions in A12, as defined in Modeling > Model Naming Conventions, and we recommend you
do the same.
Roles
Each user logged into the system is associated with a role. In the Project Template, which the
tutorial application is based on, there are three users available and each of them is connected with
a role:
User Role
admin admin
user1 user
user2 user
Generally, users can only access models if at least one of their assigned roles is included in the
model’s role list. For example, if you create a model and just assign the admin role to it, only users
with the admin role will have access. Users with the user role, for instance, will not be able to
access the model.
The exception to this is the MODEL_MANAGE access right. This permission allows users to manage (and
therefore also access) models, regardless of whether their roles are explicitly listed in the model’s
roles. It is recommended to assign this access right to roles intended for modelers. For more
information, see Security > Security Guidelines > Security Features > CRUD operations on models.
3

-- 3 of 54 --

In the Project Template, the admin role has been assigned this MODEL_MANAGE access right. Therefore,
we only need to set the user role when creating our models. Therefore, when creating models, you
only need to explicitly assign the user role. Of course, it’s also possible to allow multiple roles to
access a model by adding as many roles as needed to the model’s role list.
The setting up of users in your application is a UAA topic. However, you can have a look at the
following files to get a first impression for how the authentication and authorization is set up in the
application:
• In the folder import/auth/users, you can find the definition of the users available in the system.
• In import/auth/roles.yaml, you can see the available roles and how they are configured.
• In import/auth/childAuthorizationDefinition.json, you can find an example for custom rules
and permissions associated with creating, viewing and modifying the documents. This is part of
the document ownership concept which the Project Template provides out-of-the-box, see
Project Template > Document Ownership for more information.
Locales
Often applications need to be available in multiple languages. This can be configured in the models
by listing all supported languages in the locales field.
Our final application will be available in both English and German. Therefore, when we create
models, we will need to set the locales property to "en, de".
Whenever we have a modelable element that can be seen by the user, we will be asked to provide a
label for each locale.
For example, we will model an input for the user to enter their contact’s date of birth. When we do
so, we will set the label for this input to be "Date of Birth" and "Geburtsdatum".
A12 then does all the work switching between the two values based on the user’s currently selected
locale.
Workspace
Before we start creating any models, we need to prepare our workspace in the SME. The Project
Template already contains a workspace that we can use as a starting point:
• Open the SME.
• Click the [ Open Workspace ] button and select the import folder.
In this workspace you can find the roles file as well as some basic example models that are
provided out-of-the-box. To this, we will add a folder to store all models related to contact that we
will create:
• Click the [ Add ] button in the workspace explorer and select the option Folder.
• In the dialog window:
◦ Folder: import/models
◦ Name: "contact"
4

-- 4 of 54 --

You should now be looking at something similar to this:
With this, we can now start with creating our models.
NOTE
When opening a workspace in the SME that already contains models, you may
sometimes get a message about some models having minor incompatibility issues.
This is usually caused by a version mismatch. With this message, there should then
be a button called [ Resolve all issues ] which will do a quick migration to the
current version of A12 used by the SME.
For more information, please refer to Modeling > Model Editor (SME) > Model
Version Issues and Migration.
Document Model
Introduction
As a first step, we will model the domain containing all information relevant for the contacts in our
CRM in a Document Model.
Since our focus is on development, we will only go through the basics by creating a simple
Document Model. You can refer to Modeling > Document Modeling > Introduction and Concepts for
more detailed insights into all the features available for modeling domains with the SME.
First, we need to add a new Document Model to our workspace:
• Click [ Add ] in the workspace explorer and choose the option Document Model.
• Fill the dialog with the following:
◦ Folder: import/models/contact
◦ Name: Contact_DM
5

-- 5 of 54 --

◦ Locales: "en, de"
◦ Roles: Add an entry for "user" to the table
After you have confirmed your input, the editor for the Document Model will open to the right. The
Document Model itself is still empty of course, so now we need to add the necessary elements for
modeling the contact information.
Root Group
On the root level of our model, only groups, attachments, multi-selects and includes can be added.
All other elements can only be children of an existing group. You can find more information about
groups in Modeling > Document Modeling > Groups.
In order to add the majority of the elements available to us, we first need to add a root group. Think
of this root group as the box which contains lots of other data associated with our model, e.g. a first
name, last name or email address field.
To add a root group:
• Click the [ Add ] button.
• Select the option Group.
• In the Group Editor:
◦ Use "Contact" for the name.
6

-- 6 of 54 --

◦ Click [ Apply ].
Notice how clicking the more options button [ ⋮ ] or right-clicking on our newly created group, we
get the chance to add many more things to our model, for example:
• Fields
• Validation rules
• Computations
• …
Sub Groups
The following is a preview of the form in which users will create and update contacts:
7

-- 7 of 54 --

The upper part of this form contains personal data about our contact e.g. their name, email address,
date of birth etc.
The lower part contains contact details, in this case it is a list of addresses and a list of phone
numbers. For these lists, we are allowing the user to collect this information a set number of times
via a so-called repeat.
The three sections described above are three more groups we need to add within our root group
"Contact". But we will start with the first group for personal data:
• Click the [ ⋮ ] button on the root group.
• Select Group under the Add section of menu items.
• In the New Group form:
◦ Name it "PersonalData".
◦ Set the Repetitions to 1.
8

-- 8 of 54 --

◦ Click [ Apply ].
TIP The names of groups on the final form, e.g. "Personal Data", are set later when
creating the Form Model.
Fields
Now that we have created our group, we can add fields to it. A field in A12 is a single element of
data that can be used to e.g. store user input or have a fixed value.
Fields always have a data type associated with them and there are multiple types available in A12:
From very general types, like string, to specific A12 concepts, like Type Definitions. You can find a
complete overview of all the different data types available in Modeling > Document Modeling >
Editors for Model Elements.
We will take a look at some of the types in the following sections, while filling the "PersonalData"
group with our personal data fields. These will be:
• String: First name, last name, email address, nationality
• Enumeration: Gender, customer type
• Date: Date of birth
• Attachment: Photo
9

-- 9 of 54 --

Independent of the data type, all fields have some general properties that can or need to be set:
• Name - Sets the name of the element and is usually written in PascalCase, e.g. "FirstName".
• Data Type - Describes what the data is, e.g. string, number, boolean etc.
• Required - Denotes if the field is mandatory or not.
• Label - Offers a default field label for the user interface for each configured locale.
The properties listed above are the ones that we will need for our use case. There are however also
more advanced options available, like the properties Global and Transient, or Annotations.
Strings
Apart from the general properties listed above, in the case of strings you also have these other
options available:
• Setting a min/max length.
• Adding a regex pattern.
• Allowing a multi-line input via "Line breaks permitted".
Such data type specific properties can always be found in the Data Type Configuration section of
the field editor.
Now that you have an overview over the available options, you can start with adding your first
field:
• Click the [ ⋮ ] button on the "PersonalData" group.
• Select Field under the Add section of menu items:
◦ Fill in the following information as in the next image:
▪ Name: "FirstName"
▪ Type: "String"
▪ Required
▪ Labels: "First Name"/"Vorname"
10

-- 10 of 54 --

• Click [ Apply ] towards the bottom right.
You can then repeat the above steps to add the remaining string fields:
• Create a field for the last name.
◦ Name: "LastName"
◦ Type: "String"
◦ Required
◦ Labels: "Last Name"/"Nachname"
• Create a field for the email address.
◦ Name: "EmailAddress"
◦ Type: "String"
◦ Required
◦ Pattern (using regex):
^([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5})$
◦ Error Messages (for the data type configuration): "Please enter a valid email address."/"Bitte
geben Sie eine valide E-Mail-Adresse an."
◦ Labels: "Email Address"/"E-Mail-Adresse"
• Create a field for the nationality.
11

-- 11 of 54 --

◦ Name: "Nationality"
◦ Type: "String"
◦ Labels: "Nationality"/"Nationalität"
Enumerations
With enumeration fields, the user can choose from a list of predefined options. The representation
of the list can have different forms, e.g. a dropdown or an autocomplete. This is later configured in
the Form Model.
In our case, we will use an enumeration for the fields "Gender" and "CustomerType".
We will start with the enumeration for gender:
• Click the [ ⋮ ] button on the "PersonalData" group.
• Select Field under the Add section of menu items.
• In the Field Editor:
◦ Name should be set to "Gender".
◦ Data Type should be set to "Enumeration".
◦ Under the Enumeration Values section, click [ Add ] and enter information for each
dropdown item you want to be presented.
◦ Remember to set an overall label for this field in each locale.
▪ English: "Gender"
▪ German: "Geschlecht"
12

-- 12 of 54 --

• Click [ Apply ].
Now you can try this yourself for the other enumeration that we have to create.
Your task:
• Create an enumeration field "CustomerType" to enable assigning a type to each contact. It
should offer the following options to the user:
◦ "lead" with labels "Lead"/"Leitung"
◦ "inactive" with labels "Inactive"/"Inaktiv"
◦ "vip" with labels "VIP"/"VIP"
◦ "suspended" with labels "Suspended"/"Suspendiert"
◦ "partner" with labels "Partner"/Partner"
• For the labels of the field, you can use "Customer Type"/"Kundentyp".
▼ Click to see solution
General information for the "CustomerType" field:
13

-- 13 of 54 --

Data type configuration and labels for the "CustomerType" field:
14

-- 14 of 54 --

Dates
We also want to get the date of birth of our contacts to remember their birthdays.
To do this, we could use a string field and rely on the users of the system using the correct date
format. Better yet, we can add a field with a Date type. In the final application the user will be
presented with a date picker and the date itself will be formatted based on the current locale.
To add the date of birth field:
• Click the [ ⋮ ] button on the "PersonalData" group.
• Select Field under the Add section of menu items.
• In the Field Editor:
◦ Name should be set to "DateOfBirth".
◦ Data Type should be set to "Date".
15

-- 15 of 54 --

◦ Set a label for each locale e.g. "Date of Birth" and "Geburtstag".
• Click [ Apply ].
Attachments
Our model can include attachments, meaning files that can be uploaded and saved with the contact.
In our model we will use an attachment to include the possibility of adding a profile picture of our
contact.
Attachments appear in the final application as a File Upload Widget.
To add the attachment for the photo:
• Click the [ ⋮ ] button on the "PersonalData" group.
• Select Attachment under the Add section of menu items.
• In the new attachment screen:
◦ Name should be set to "Photo".
16

-- 16 of 54 --

• Click [ Apply ].
TIP The labels for attachments are, just like groups, set later in another tool for creating
our form.
Repeatable Groups
As previously mentioned, we want to have three groups on our form. You should have already
completed the first "PersonalData" group by following the above instructions.
Now we want to add the two remaining groups for addresses and phone numbers. For both of these
cases, a given contact might have multiple that the user wants to store, e.g. a personal and work
phone number.
Therefore, these will be repeatable groups allowing the addition of multiple entries of the
associated fields.
Since the approach is nearly identical to the one we used for "PersonalData" and its fields, you
already have all the knowledge required to complete this step yourself.
Your task:
• Add a new group inside our root "Contact" group called "Phones".
◦ Set the Repetitions input to 5, since we want up to 5 phone numbers.
• Inside the newly created group we want two new fields:
◦ A field called "PhoneNumber" of type string with labels "Phone Number"/"Telefonnummer" .
17

-- 17 of 54 --

◦ A field called "Type" of type enumeration with labels "Type"/"Art".
The following options should be offered to the user:
▪ "MOBILE" with labels "Mobile"/"Mobil".
▪ "WORK" with labels "Work"/"Arbeit".
▼ Click to see solution
Adding the "Phones" repeat:
Adding the "Phones" > "PhoneNumber" field:
18

-- 18 of 54 --

Adding the "Phones" > "Type" enumeration:
19

-- 19 of 54 --

You can now also repeat the same procedure for the "Address" group to enable the user to input up
to five addresses for a contact.
Each address may contain the usual address information as string fields with the following labels:
• "Street"/"Straße"
• "Housenumber"/"Hausnummer"
• "City"/"Stadt"
• "Zip Code"/"Postleitzahl"
• "Country"/"Land"
The resulting repeatable group should then look like the following:
Final Document Model
We have successfully completed our Document Model. The only thing we must remember to do is to
save it and ensure it’s in our model’s directory.
20

-- 20 of 54 --

You can see a preview of your Document Model by clicking the [ ⋮ ] button on the model tree or the
"Contact" root group, and selecting Ad Hoc Testing under the General section of the menu items.
Under the Settings option towards the upper left side of the model, you can update any of the base
model settings such as its name, available locales and the roles.
If you want to compare your model with the solution then you can find the completed version by
checking out the tag 2025.06-ext5/intro/task-1-end.
As you probably noticed while adding the various elements, there are many more options that can
be utilized in Document Models. As you gain more experience with A12 you will start to discover
what they are used for and you can also find more information in the modeling documentation.
Form Model
Introduction
Now that we have our Document Model, we can use it to model the form, in which the end user will
create and edit the contacts.
We will again only go through the basic concepts and create a simple Form Model. More detailed
information about Form Models can be found in Modeling > Form Modeling > Introduction and
Concepts.
21

-- 21 of 54 --

Before we start with modeling, we need to add a new Form Model to our workspace (the same way
as we did for the Document Model):
• Click [ Add ] in the workspace explorer and select the option Form Model.
• Fill the dialog with the following:
◦ Folder: import/models/contact
◦ Name: Contact_FM
◦ Document Model: import/models/contact/Contact_DM
◦ Locales: "en, de" (Automatically added after selecting a Document Model)
◦ Roles: "user" (Automatically added after selecting a Document Model)
Once you confirm the input, the editor for the Form Model will open with our empty Form Model
that we now need to fill. This editor looks similar to the one for the Document Models. However
now we have a panel on the right side with our associated Document Model that we can use to add
fields or groups to our form.
As a first step, we will define a label to be displayed at the top of our form:
• Go to Settings.
• Scroll down to the section Labels.
• Set a label for each locale:
◦ English: "Contact"
◦ German: "Kontakt"
• Click [ Apply ].
In this editor, you can also configure other base model settings for the Form Model, similar to the
Document Model.
Sections
Now we can go back to the Screens editor and add some content to our form. You can see all the
elements that we can add by clicking on the [ ⋮ ] button on the area named "Screen 1".
One of the available options is the Multi Column Section. Sections, as the name suggests, allows us
to split up our form into areas with inputs that e.g. collect similar data.
We will now create such a multi-column section to contain the elements related to the personal
information of the contact. As you might remember, we have already created the group
"PersonalData" for such fields in our Document Model. But there we were not able to provide a
label, which we will do now here.
Besides the labels, we also need to define the layout of the section. This determines how wide the
section will be respective to the screen width. It is based on a 12 column grid system.
If we input 12, it will take up the entire width of the screen (6 would be half, 4 one third and 3 one
quarter).
22

-- 22 of 54 --

With that in mind, we can create a section to contain the inputs related to personal data:
• Click the [ ⋮ ] button on "Screen 1".
• Select Multi Column Section under the Add section of menu items.
• In the New Multi Column Section form:
◦ Name it "PersonalData".
◦ Specify the layout, so that section spans the entire screen width.
◦ Provide the following labels "Personal Data"/"Persönliche Daten".
• Click [ Apply ].
Control Grids
Any fields we want to add to our form need to go into a so-called control grid. A control grid
represents the column structure of our form in a given section.
Therefore, you need to specify the layout here as well. It is again based on the 12 column grid
system. Here we need to configure how many columns the section should contain and how wide
each column is respective to the screen width.
For example, if we fill "12" into the "Layout lg" field, our section would have one column taking up
the entire width of the screen. With e.g. "6-3-3", we would instead have three columns where the
first one would be half of the screen and the remaining two each one quarter.
23

-- 23 of 54 --

For this section we want the inputs of the form to each take up one third of the screen space,
therefore we will add the following control grid for "PersonalData":
• Click the [ ⋮ ] button on the "PersonalData" section.
• Select Control Grid under the Add section of menu items.
• In the New Control Grid form:
◦ Name it e.g. "CG1".
◦ Specify the layout, so that we have three columns that each take up a third of the screen
width.
• Click [ Apply ].
Rows
The final step before we can add our inputs, is to add some rows to structure and contain them:
• Click the [ ⋮ ] button on the "CG1" control grid.
• Select Row under the Add section of menu items.
• In the New Row form:
◦ Name it e.g. "r1".
• Click [ Apply ].
24

-- 24 of 54 --

• Repeat the steps above to add two more rows to the control grid.
Our Form Model now should look like the following:
Fields
Now that we have created the necessary structure for our inputs, you can add in the fields. To do
this, you need to drag and drop the items from the "Data Models" section on the right into your
newly created rows:
► https://vimeo.com/1097016351 (Vimeo video)
You should now add the fields in the "PersonalData" group from our Document Model to the form.
You can play around with the order to get a better understanding for how the structure works.
To see how your form will then look to the end user, you can use the preview option in the toolbar
on the left-hand side, provided your form has no errors. The preview reloads automatically, if you
change anything in the model.
There are options on the left-hand side of the preview for seeing what your form looks like under
different circumstances e.g. with input errors, or another language.
Once you have added all the fields and ordered them into rows, your Form Model should look
something like this:
25

-- 25 of 54 --

The labels of fields are inherited from the Document Model and do not need to be set again.
The exception to this are attachment fields, such as our "Photo". For those, you have to have labels
set in the Form Model. You can click on "Photo" to open the edit window and add your localized
label in the section Label > Field Configuration > Type, e.g. "Profile Picture"/"Profilbild".
The preview of our form now looks like the following:
26

-- 26 of 54 --

Repeats
Our last piece of data to add is our repeatable groups, so addresses and phone numbers with up to 5
instances.
Repeated groups need to take up the entire width of the screen and so are not added in control
groups. Instead, they can be directly dragged and dropped into a section.
So to add the two repeats for addresses and phone numbers, you can follow these steps for each of
them:
• Click the [ ⋮ ] button on the "Screen1" screen.
• Select Section under the Add section of menu items.
• Provide a name and a label in the New Section form:
◦ "Address" with the labels "Address"/"Adresse".
◦ "Phone" with the labels "Phone"/"Telefon".
• Click [ Apply ].
• Drag and drop the respective group from the Document Model to the newly created section:
► https://vimeo.com/1097019300 (Vimeo video)
After you have done this for both repeats addresses and phone numbers, we will have the following
Form Model:
27

-- 27 of 54 --

The preview of our form now should look like this:
Subheader and Footer
Our form looks good, but it is missing a key element: There is currently nowhere a user can save
28

-- 28 of 54 --

the form.
Save Button
To add a save button to the form:
• Go to Settings > Subheader and Footer.
• Under the section Footer Major Buttons, click [ Add ].
• Add the following details:
◦ Name: "Save"
◦ Type: "Event"
◦ Event: "event_submit"
◦ Validation Mode: "Full Validation"
◦ Priority: "Primary"
◦ Label: "Save"/"Speichern"
• Click [ Commit ].
Event
The event "event_submit" that we have specified, is a built-in A12 event that saves the form. If it is
not listed as a dropdown option, you can type it in yourself.
29

-- 29 of 54 --

Please be aware that such built-in A12 events are intended as a shortcut to be used during
development, as it does not support any customization and is therefore not suitable for most
projects. They are provided as part of the CRUD package. For more details, please take a look at the
CRUD documentation.
Validation Mode
The validation mode controls if and for which scope the validation of the form will be run, so that
e.g. mandatory fields are checked. There are values for:
• No validation: Do not run any checks against the input data.
• Partial validation: Run validation against fields in the current form.
• Full validation: Run validation against all fields.
An example for the difference between partial and full validation would be, that if we were to leave
one of the required fields off our form (e.g. "EmailAddress") and try to save the form, it would result
in an error that the user cannot solve.
Cancel Button
We also want to allow the user to quit the form, so we also add a cancel button.
The steps to add this button are basically the same as for the save button. But we will add it under
the Footer Minor Buttons section. This determines which side of the screen it is shown on. Major
buttons are displayed on the right side, while minor button are on the left.
For this secondary button we will use the "event_cancel" event. The same warnings apply here, as
for "event_submit".
We also do not want to validate our fields, as the data will not be stored anyway. Also, it is usually
recommended setting the flag Destructive under Visual Settings, since the action will remove the
current form from the screen.
Your task:
Add a cancel button to the form using the information above. You can use the labels
"Cancel"/"Abbrechen".
▼ Click to see solution
30

-- 30 of 54 --

Navigation
Multiple Screens
Our form currently only has one screen, where we have put all our fields thus far. However, it is
possible to have multiple screens in a form. By adding more screens we can make large forms more
easily digestible for users.
For example, we could have one screen for personal details, one for addresses and one for phone
numbers. Since our form is rather small, we will not do this here. But if you are interested, you can
try this out yourself. You can find an example of what this could look like below:
► https://vimeo.com/1053468989 (Vimeo video)
Current Screen
Take a look at the following image, where a new button/tab has been added to the top of the form,
which lets the user know which screen they are on:
31

-- 31 of 54 --

To add this yourself:
• Go to Settings > Subheader and Footer.
• Under the section Subheader Major Buttons, click [ Add ].
• Add the following details:
◦ Name: "Details"
◦ Type: "Navigation"
◦ Screen: "Screen 1"
◦ Label: "Details"/"Details"
32

-- 32 of 54 --

With the field Screen we can specify which screen the navigation button should take us to. Since
we currently just have one, this is what we need to select.
For the Validation Mode, we can stick with the default of "No Validation" as we do not want to
trigger the validation when a user is trying to get to another part of our form.
Final Form Model
The preview of your finished form should now look like the following:
33

-- 33 of 54 --

When saving without any data entered, you can see an error bar allowing you to focus on any fields
with issues. Filling all required fields and pressing [ Save ] should open a toast notification saying
"event_submit" is triggered. The same should happen for cancel.
If everything works, we have finished creating our Form Model. Now we just need to save it and
then can continue with the next step.
Overview Model
Introduction
Now that we have modeled the domain and the form for our CRM, we want to create an overview
of our contacts. In A12 this is done by creating an Overview Model.
We will again only create a simple Overview Model to demonstrate the core concepts. To learn
more about all the features available for Overview Models, you can check out Modeling > Overview
Modeling > The Overview Model.
As a first step, we will create a new Overview Model in our workspace:
• Click [ Add ] in the workspace explorer and select the option Overview Model.
• Fill the dialog with the following:
◦ Folder: import/models/contact
◦ Name: Contact_OM
◦ Document Model: import/models/contact/Contact_DM
◦ Locales: "en, de" (Automatically added after selecting a Document Model)
34

-- 34 of 54 --

◦ Roles: "user" (Automatically added after selecting a Document Model)
The editor for the Overview Model will again open on the right-hand side, once you confirm your
input. Now we can start with configuring this empty Overview Model.
Model Settings
Let us start in the Model Settings tab, where we can provide some general information about our
model. You can see that the locales and roles are already pre-filled, based on the information we
provided when creating the Overview Model.
Besides that, we also want to specify a label here. This will be displayed in a header at the top of the
table and is used as the aria-label for accessibility.
We will use "Overview" for the English label and "Überblick" for the German one, but you can also
use whatever label you prefer or even provide subtitles.
This is everything we want to add to our model settings, which now should look similar to the
following:
Now that we have finished the general model settings, we should consider how we want our
overview to look like. An overview in A12 contains a tabular list displaying different documents
which are defined by the specified Document Model. In this table, each row represents one
document with the columns being different fields for which the information should be displayed.
35

-- 35 of 54 --

In the end, we want our contact overview to look like this:
As we will not be needing any multi-selection in our overview, we can disable the pre-configured
settings for it by doing the following:
• Uncheck the Enable Multi-Selection checkbox in the Overview tab under Features > Multi-
Selection.
• Delete the "Multi-Selection" action in the Custom Actions tab under Subheader > Minor.
Columns
You can see that the columns in the overview define which information of the document is shown.
They are an integral part of the Overview Model and are based on the fields specified in the
Document Model, same as the inputs for the form.
There are two column types available: Reference column and expression column.
A reference column defines what field from the Document Model is displayed via an element
reference. Only non-repeatable fields can be used as the element reference, as the data needs to be
unique.
The following special configuration options are available:
Option Description Limitation
Sortable Determines if the column
should be sortable.
Only for non-multi-select types.
Display mode Configures how the attachment
or multi-select should be
displayed.
Only for attachment or multi-
select groups.
Suffix Displays a suffix after the entry. Only for number fields.
36

-- 36 of 54 --

Option Description Limitation
Summary Enables a summary row at the
footer of the table.
Only for number fields.
For more details on these settings, please take a look at Modeling > Overview Modeling > Reference
Column.
An expression column is a more advanced concept. It is used to merge multiple field values into a
single cell or to add formatting instructions. For example, you could combine first and last name
into one column for the full name. If you want to see how to do this or learn more about expression
columns, you can check out Modeling > Overview Modeling > Expression Column.
Independent of the column type, there are some common settings available, e.g. width. For more
information about these settings, you can refer to Modeling > Overview Modeling > Common
Settings.
For our Overview Model we will stick with the simple reference columns, which can be added by:
• Go to the Overview tab.
• Under the section Columns, click [ Add ].
• For Element Reference, select the field from the Document Model which the column should
reference.
• Check the Sortable checkbox, if you want the user to be able to sort the column.
• Click [ Add ].
Please note that the labels are inherited from the Document Model as well, but if you would like
you can adapt them for the overview. The exception here is again attachment fields, for which you
need to manually set the labels when creating the column.
You can now do the steps above for all the non-repeatable fields you would like to display in your
overview. You should then end up with a list of columns similar to this:
37

-- 37 of 54 --

The order of the columns in the list also determines the order of the columns in the overview. You
can use drag and drop, or the arrows on the right side in each row to adjust this.
You can also set how the table’s content should be sorted at the initial load of the Overview Engine
by utilizing the Default Sorting. Please refer to Modeling > Overview Modeling > Default Sorting,
for more details on how this works.
Features
There are many more configuration options available in the Overview tab. We will just use the
default settings. But you can take a look and experiment with those later if you are interested. You
can also find out more about all these available features in Modeling > Overview Modeling >
Features.
Custom Actions
Now that we have configured the structure and content of the overview by defining the columns,
we are only missing the necessary interaction points. The purpose of the overview is not just to
display the documents, but also to provide users with a way to navigate this data by creating new or
deleting existing documents, filtering, searching etc. So as a last step, we will create these actions.
In the Custom Actions tab, we have the following configuration options:
• Different options regarding rows: Row action group, default row action, title for interactive
rows.
• Minor and major buttons for the subheader and footer.
• Position for multi-selection, search and filters.
We will only discuss some of these options in this section, but you can learn more about all the
possible configuration in Modeling > Overview Modeling > Custom Actions.
38

-- 38 of 54 --

There are already some actions set out-of-the-box: Multi-selection, search and filter. You can of
course remove them or categorize them differently to display them in a different part of the
overview. The default setting is based on the recommendation by the Plasma design system, so we
will keep it as it is.
Besides these functionalities, we also want to enable the user to create a new contact or to delete an
existing one. For those two scenarios, we will now add the necessary buttons. To do that, we first
need to consider where the button should be placed. This determines as which type of action, the
button will be created.
Row Actions
To delete a document, it makes sense to have such a button available for each document. Therefore,
we will use a row action for this purpose, which means the button is displayed for each row in the
overview.
To add the action for deleting a document to the model:
• Go to Row Action Group.
• Under the section Row Action, click [ Add ].
• Add the following details:
◦ For Event, select "delete".
◦ Add a Confirmation Text for both locales to display a confirmation dialog.
◦ Select an Icon from the icon picker, e.g. "delete".
39

-- 39 of 54 --

• Click [ Add ].
Providing "delete" as the event is the only setting that we need to specify. The rest is up to you,
depending on what you want the button to look like. The settings above are just an example for
what is often used in this case.
For full details on the different settings, see Modeling > Overview Modeling > Row Action.
Subheader and Footer
Creating a new document is not associated with an existing document, so we want the button to be
outside of the table. In that case, we can put the button in the subheader or in the footer of the
overview. The options minor and major then decide, if the button is displayed on the left or the
right side respectively.
The Plasma recommendation for such add buttons is to place them in the subheader as a minor
button, so this is what we will do.
To create the add button, we now need to:
• Go to Subheader.
• Under the section Minor, click [ Add ].
• Select "Button" as the Action Type.
• Add the following details:
◦ For Event, select "add".
40

-- 40 of 54 --

◦ Select an Icon from the icon picker, e.g. "add".
◦ Add a Label for both locales ("Add"/"Hinzufügen").
Once you select "Button" as the Action Type, the button configuration is very similar to the row
action. Therefore, the event name is here again the only mandatory field and the other fields can be
filled out or not depending on your use case.
Final Overview Model
We have now finished modeling our overview and you can save it.
If you want to compare your model with the solution then you can find the completed version by
checking out the tag 2025.06-ext5/intro/task-1-end.
You might now ask yourself, how you can preview your Overview Model. Unfortunately this is
currently not possible in the SME, unlike for the Document and Form Model.
Business analysts usually use the Preview App in combination with a Master Detail Module Model
to see and test their models. If you would like to try this, you can find instructions in Modeling >
Master Detail Modeling > Homogeneous Overview Module. However, as this feature is not very
relevant for developers working with A12, we will not focus on this.
Instead, we will directly create an Application Model, with which we can add our models as a
module to our application. Using the model deployment feature from the SME, we can then easily
deploy any model changes to our application and preview them there. This will be explained in
41

-- 41 of 54 --

more detail in Tutorials > Intro > Project Template > Model Deployment.
Application Model
Introduction
By now, we have modeled,
• the fundamental content and structure of our data in the Document Model,
• the form to create and edit data in the Form Model
• and an overview displaying the existing data in the Overview Model.
With that, we have all the fundamental building blocks for our CRM, but we still need to connect
the UI models (overview and form) with each other and integrate it into our application.
This is where the Application (App) Model comes in to play. The purpose of this model is to define
the regions, layout and behaviour of the application.
For example, when you click the [ Add ] button or on a row in the overview, the associated form
appears to create or edit the document. This interaction pattern is referred to as Master/Detail and
is the most common one in A12. You have actually seen this yourself in the SME, which is based on
A12, when opening up a model, field etc. This is one of the aspects that can be modeled in the
Application Model.
The specific principles and concepts behind the Application Model are introduced and discussed in
more detail in Client > Application Model. You can also find some more context regarding the
technical aspects behind Application Models in Tutorials > Frontend > Application Frame. Here we
will stick with the modeling aspect of the Application Model and how to create one.
For that, we first need to create a new Application Model in our workspace:
• Click [ Add ] in the workspace explorer and choose the option App Model.
• Fill the dialog with the following:
◦ Folder: import/models/contact
◦ Name: Contact_AM
◦ Locales: "en, de"
◦ Roles: "user"
After you have confirmed your input, the editor for the Application Model will open to the right
with our empty Application Model.
Region
As a first step, we need to define where the UI components will be shown in the application. This is
done by configuring the regions, which are structured hierarchically.
There is one top-level region, the app region, which can have different subregions. This tree of
regions is static and defined in the Application Model. The names of the regions are completely up
42

-- 42 of 54 --

to you as they have no semantic effect, this is determined by the layout assigned to the region.
The following layouts are provided out-of-the-box by the Client component:
• ApplicationFrame: Intended only for the root region of the application, as it renders,
◦ a header with a logo, title and additional, configurable header items (e.g. logout button).
◦ a sidebar with the views assigned to the region "SIDEBAR".
◦ a content part with the views assigned to the region "CONTENT".
◦ a modal with the views assigned to the region "MODAL".
• Null: Views are rendered next to each other without any layout.
• MasterDetail: Views are arranged side-by-side with only one or two visible at once.
• Stack: Views are "stacked" on top of each other, so that only the latest view is visible.
• Dashboard: Views are arranged in tiles as a grid.
It is also possible to provide and use custom layouts instead. You can find more information about
the provided layouts as well as how to create a custom one in Client > Layouts.
Since we just want the standard layout for our application, we will use the provided
ApplicationFrame layout. As mentioned above, this layout already declares three subregions
("CONTENT", "MODAL" and "SIDEBAR") that can be configured via their assigned layout.
The usual layouts for these subregions are:
• "CONTENT": MasterDetail
• "SIDEBAR": Null
• "MODAL": Stack
You can find this region configuration for the the ApplicationFrame layout illustrated in Client >
Configuration of Regions. We will now configure the regions for our Application Model with the
same approach. We can do this in the Region section of the editor.
The name for the top-level application region is arbitrary, we can e.g. use the suggested "APP". It is
just important that we specify the name of the layout as ApplicationFrame and that we add the three
subregions "CONTENT", "MODAL" and "SIDEBAR" with their associated layouts as listed above.
The default region defines to which region a view is added, if no region is specified. We will use the
"CONTENT" region for this purpose, as this is the core of our application.
With that in mind, you can now try configuring the region in our Application Model as described
above. It should then look like this:
43

-- 43 of 54 --

With this, we have set up the framework for our application. Now we need to specify what we
actually want to display and where, this is done in large parts using modules.
Modules
The Application Model is mainly a composition of modules, which contain and connect all the
models associated with a specific domain in the application.
For example, in our CRM we could have multiple modules like contacts, products, contracts, offers
and so on, depending on the specific use cases. These different modules are then combined to build
the application, with the benefit being that they can be reused. But for this task, we will just focus
on our contact module.
As we will just focus on the modeling, you can learn more about modules in Client > Modules.
To create a new module for contact:
• Under the section Modules, click [ Add ].
• Set the name for the module, e.g. "ContactModule".
• Under the section Menu:
◦ Add a name, e.g. "Contact", so that a menu entry is displayed in the header.
◦ Set the activity descriptor to define the initial state. This initial state is the activity that is
44

-- 44 of 54 --

created on click of the menu item.
▪ It needs to be formatted as a valid JSON.
▪ The content does not need to fulfill any specific criteria, it just needs to be unique.
▪ You can use the following, for example:
{
"module": "Contact"
}
This will then result in the following activity, when the menu item is clicked:
◦ Add a label that will be displayed in the header menu for each locale, e.g.
"Contact"/"Kontakt".
When following the steps above, your module configuration should look similar to this:
45

-- 45 of 54 --

In the instructions above we have introduced the term activity, which is central concept of the
Client component. It can be seen as a use case with specified user interactions. It is implemented as
a data structure in the Redux store containing all necessary information for this use case. The
concepts of activities is explained in detail in Client > Activity. You can also find additional
resources on such Client concepts in Tutorials > Frontend > Application Frame > Application Model.
Flows
Having created the module and configured how it is displayed in the menu, we now need to add a
flow to it. With a flow you can group together connected UI interaction steps, which are called
scenes. For example, in the contact module we have two scenes, the overview and the form, that
will be contained in one flow.
To create this flow for the "ContactModule":
• Under the section Flows, click [ Add ].
• Set the name, e.g. "ContactFlow".
46

-- 46 of 54 --

Scenes
As a next step, we need to add the scenes to it which connect activities with the resulting view
changes in the UI.
We will start with the scene for the overview, for which an empty scene can be created in the
"ContactFlow" with the following steps:
• Under the section Scenes, click [ Add ].
• Set the name, e.g. "ContactOverview".
• Optional: Provide a description for documentation purposes.
Now, to define this scene you need to specify the activity that it should react on, this is done with a
match condition, and which resulting view changes should be triggered, these are called scene
changes.
Match Conditions
The match condition is the selection criteria for the activity, that we want to be connected with a
view change by the scene. This selection is determined by checking if the activity descriptor of the
activity fulfills the match conditions of the scene.
For every action that is dispatched in the application, the Application Model is checked for a scene
with match conditions that are fulfilled by its activity descriptor. The first scene with fitting match
conditions is then chosen and the defined scene change is initiated.
A scene can contain multiple match conditions. Each match condition is made up by a key and a
condition for its value. The possible conditions are:
• Is Set (boolean): For the given key an arbitrary value must exist or not exists, depending on the
boolean.
• Must Equal (string): The value for the given key must match the provided string.
For the scene handling the overview, we want the overview to be displayed when the menu item of
the contact module is clicked. Therefore, the match conditions need to fit with the activity
descriptor we have defined for the menu, which was:
47

-- 47 of 54 --

{
"module": "Contact"
}
To define such match conditions, we need to add the following configuration to the
"ContactOverview" scene:
• Under the section Match Conditions, click [ Add ].
• Add the following details:
◦ Key: "module"
◦ Must Equal: "Contact"
Now that we have defined the match condition, we need to determine what scene change this
should trigger.
Scene Change
A scene change consists of scene directives that reconfigure the UI, in particular the contained
views and layout of regions. These directives can be categorized into two types:
• onEnter: They are executed when a scene is selected.
• onExit: They are executed when a scene is followed up by the next scene.
48

-- 48 of 54 --

Independent of the type, there are two directives available:
• REGION_CLEAR: Changes the layout of a region and removes currently displayed views.
• VIEW_ADD: Adds a view to a certain region.
In our case, we want the overview to be displayed when the associated activity is triggered by
clicking the menu item of the module, so we need our directive to be executed onEnter.
We then want our overview to be displayed in the "CONTENT" region, which already has the
MasterDetail layout. Therefore, we just need to add the view for the overview to that region with a
VIEW_ADD directive.
For the VIEW_ADD directive, we then just need to specify the view that should be displayed. This is
done by setting the name of the UI component that should be shown in the view. This component is
defined and registered in the application.
The Project Template provides three of these components out-of-the-box that we can use:
"FormEngine", "OverviewEngine" and "TreeEngine". They handle rendering the content for
standard forms, overviews and trees via a mapping to the React components of the respective
engines.
They are defined in client/src/app/viewProvider.tsx, if you are curious. You will also learn more
about this, including how to create a custom component, in Tutorials > Frontend > Application
Frame > Custom View Component.
To now create the scene change for the overview, we need to make the following adjustments to the
"ContactOverview" scene:
• Go to the section Scene Change.
• Under the section On Enter, click [ Add ].
• Add the following details:
◦ Type: Select VIEW_ADD.
◦ Region: Since we have set "CONTENT" as the default region, we do not need to set it here.
◦ Name: Set "OverviewEngine".
◦ Under the section Models, click [ Add ] and specify the following:
▪ Model Type: Select "Overview".
▪ Name: Set Contact_OM.
49

-- 49 of 54 --

Now you can commit our changes for the scene change and scene, until you have reached the
"ContactFlow" again. With this, you have created a scene for the overview.
Combining Views
Now, we also want our form to be displayed as a detail view. For this, we need to define a new scene
for the form in the "ContactFlow".
Here, we might notice an issue: The match condition that we would use for this scene would be
identical to the one we used for the overview scene, as they are both part of the same module.
As the application will always display the first scene with a fitting match conditions for an activity,
this would result in problems. Therefore, for such cases you need to make sure that the match
condition is specific enough.
To achieve this for the overview and form, we can use the Is Set condition with the key "instance"
by setting it as true for the form and as false for the overview. This works, as the activity for forms
always contains an instance, while the overview activity does not.
Apart from that, the steps are the same as for the "ContactOverview" scene, so you can try this
yourself.
Your task:
• Create a new scene called "ContactForm" in the "ContactFlow".
• For the match conditions, you need to keep in mind:
50

-- 50 of 54 --

◦ We now need two conditions, one reflecting the activity descriptor of the module and one
checking if "instance" is set.
◦ The match condition needs to be adjusted for both the overview and the form scenes.
• The scene change follows the same principle as the one for the overview, you just need to
reference a different container and model.
▼ Click to see solution
The "ContactForm" scene should look like this:
With the following scene change:
51

-- 51 of 54 --

An Is Set match condition has been added to the "ContactOverview" scene:
Once we commit and save everything, we will have successfully modeled the module for contact.
Which, for now, is the only module we need. Additional modules will be added in the frontend
tutorial.
52

-- 52 of 54 --

Initial Activity
In case of multiple modules, you can define which module is displayed when initially opening the
application or logging in. For that, the activity descriptor of the given module needs to be provided
as the initial activity descriptor. This can be done in the Initial activity section under the
Application tab.
However, you need to be careful if you have multiple Application Models in your application. Only
one should have an initial activity set, as they are conflicting with each other otherwise.
If you take a look at the YourAppModel_AM Application Model that is also in our workspace, which we
will take a closer look at in the next task, you will see that there already is an initial activity
descriptor defined. Therefore, we will not add one to Contact_AM.
Other Customizations
Beyond the aspects we have just covered, there is much more that can be modeled or even
customized with the Application Model. For example:
• Submenu
• Dashboard
• Sidebar menu
• Custom views
• Custom layouts
We will not go into any detail here, you can find all the information on this in Client > Application
Model.
Final Application Model
We now have completed our Application Model and only need to ensure that all changes are
committed and applied, and that we have saved it.
If you want to compare your Application Model with the solution then you can find the completed
version by checking out the tag 2025.06-ext5/intro/task-1-end.
Conclusion
In the previous chapters, we have created a Document, Form, Overview and Application Model for
the contact functionality of our CRM. Now, we just need to take those models and integrate them
into our application.
As the starting point for our application we have used the Project Template, which makes this very
easy. You just need to make sure that your necessary models are under the import/models folder.
You can either directly use the import folder as the workspace in the SME, which is the approach we
used. Alternatively, you can import an existing workspace from the SME, which is explained in
Project Template > Importing a Workspace.
53

-- 53 of 54 --

Beyond that, there are no extra steps necessary. We can just start the server and the client, as
described in Tutorials > General Information > Running the Code.
If you have the server already running, it is necessary to restart it to load the new models. A restart
of the client is not necessary though.
After this, you can access the application at localhost:8081 with the credentials "admin"/"A12PT-
admintest". If you then open the "Contact" tab in the header, you can see the module we have just
created via modeling:
Feel free to take some time to test the module by creating, editing or deleting contacts, switching
locales, filtering and so on. All of these functionalities, we have created without a single line of
code.
Of course, there is still some aspects of our application that need to be customized, like the title of
the application for example. This is not possible with modeling, but we will learn how to do it in the
next task.
Task 2: Project Template »
54

-- 54 of 54 --

