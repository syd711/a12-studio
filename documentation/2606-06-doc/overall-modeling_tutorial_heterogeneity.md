# overall modeling_tutorial_heterogeneity

Tutorial: Heterogeneity Modeling
Prerequisites
The target audience for this tutorial are business analysts. Some prior knowledge of the tools is
assumed. Before you start this tutorial, you should have completed the following training course(s)
and tutorials. For more details on what topics are covered, please follow the links.
• A12 Fundamentals Training
This tutorial focuses on Heterogeneity and the relevant documentation is linked below. Although
the terminology used in this tutorial is explained in the documentation, we’ve included a glossary
of terms to help you if you don’t understand what we are trying to say:
• Modeling Heterogeneous Data documentation
• Heterogeneity Glossary
This tutorial uses the installer which you can download from geta12.com.
NOTE Please ensure your installer version matches this tutorial.
Use-case
I collect different personal information for employees, freelancers and interns working in my
organization. There is, however, some shared information as they are all people. These data fields
include their names and dates of birth.
As all these people work in my organization, I want to be able to group this information and view it
in one overview. However, I want to be able to add type-specific data fields and clearly separate the
different categories so that I view them in three separate overviews if necessary.
Finally, as all the people working in my organization are employees, freelancers or interns you
should not be able to add people without them being either an employee, freelancer or intern.
[Heterogeneity Schema] | Heterogeneity_Schema.png
Figure 1. Use-case schema
End Result
At the end of this tutorial, you will be able to deploy your models so that you can:
• Add new instances of the employee, freelancer and intern Document Models as Subtypes of the
people Supertype
• View employees, freelancers and interns together in the overview for people
• View employees, freelancers and interns in their own separate overviews
1

-- 1 of 24 --

The end result will be similar to the People module of the advanced workspace. If you need to
check your work as you do the tutorial, please refer to the expandable sections at the end of each
step:
▼ Click here to see what your project should look like by now
You can find a list of models that you created as well as fullscreen pictures of each step to guide
you.
Essentials of Heterogeneity
You may have noticed some differences between the use-case and the end-result sections. This is
largely because the end result uses some specific terminology that you should understand when
using Heterogeneity. So, before we get started with the step-by-step modeling guide, let’s clear up
any questions you might have about Heterogeneity.
What Can I Do With Heterogeneity?
Heterogeneity allows data of different types to be grouped and listed together. This group can then
be treated in a unified way, for example, allowing data from the grouped types to be included in a
single list. In the use case, we want to group the different types of people working for our
organization.
In the following, the terms type and Document Model are used synonymously. However, they serve
different purposes. Document Model is the well-known way in which the inner structure of
documents are defined. Type is used here to describe the semantic connections between entities
without caring about their inner structure.
So far, when we create three separate Document Models for employees, freelancers and interns, we
would view this data in three separate overviews. By using heterogeneity, we can view the data
from all three types in a single Heterogeneous List.
The Document Models are grouped together using a Supertype Document Model. The Document
Models that should belong to this group are then added to a list of Subtype Document Models using
an Annotation in the Document Model Settings. This can be done in one of two ways:
• Set the Annotation "superTypes" in the Subtypes Document Models and add the name of the
Supertype Document Model as the annotation value.
• Set the Annotation "subTypes" in the Supertype Document Model and list the Subtype Document
Models in the annotation value.
Data from fields on the Subtype Document Model which have identical paths to fields on the
Supertype Document Models can then be displayed in trees and overviews. In the picture below, the
fields "FirstName", "LastName", "DateOfBirth" and "Type" all have the same path on the Supertype
People_DM and Subtypes PeopleEmployee_DM and PeopleFreelancer_DM.
CAUTION The path includes the name of the root group. That means, that the root groups
of the Subtype and Supertype Document Models should have the same name.
2

-- 2 of 24 --

NOTE The data from fields in the "UniqueData" groups will not be part of the
Heterogeneous List as these fields are not present in the Supertype.
[Text SharedFields] | Text_SharedFields.png
Figure 2. Paths for Fields on the Supertype People_DM and Subtypes PeopleEmployee_DM and
PeopleFreelancer_DM
abstract Supertypes
An Annotation controls if the Supertype can be instantiated or not. A Supertype that can not be
instantiated is referred to as abstract and the user will not be able to select this type in the
Variant Dialogue when adding new instances. The Heterogeneous List is then only made up
instances of the Subtype Document Models.
Using the Supertype
The Supertype Document Model can be used as reference for the Overview or Tree Models. In
overviews or trees, the data of the Subtype and Supertype documents are treated and displayed in
the same way, as long as the paths to the fields match. When a new document is created, the user
will be able to choose the type of document. In our use-case, this means that the user will be able to
select if a document for an employee, freelancer or intern should be created.
Supertype Document Models can also be used in Relationship Models. This means that any of the
grouped documents may take the respective role when creating a new link. For example,
employees, freelancers and interns could be linked to a team using a single (heterogeneous)
Relationship between the Supertype People and Team Document Model.
Finally, these groups of Document Models can also be again grouped together with another
Supertype Document Model. For example, we could group employees with short-term contracts and
employees with rolling contracts, both having the employee Document Model as the Supertype
(which has in turn the people Document Model as its Supertype).
How Can I Apply Heterogeneity to Different Use-
Cases?
The use-case will determine whether the Supertype should be abstract, and therefore not
instantiated, or not. In the use-case of this tutorial, employees, freelancers and interns are all
Subtypes of the people Supertype. And as all the people working in the organization are either
employees, freelancers or interns, the Supertype will not be instantiated and therefore marked as
abstract.
Other use-cases may require a general category, and the Supertype could be used to store the data
that cannot be assigned to a specific Subtype. The Supertype would then be set to abstract = false.
This difference is represented visually in the picture below. On the left-hand side we see the
Subtypes of an abstract Supertype, there we have only employees, freelancers and interns. On the
right-hand side we see that the Supertype is not abstract, and instances of the people Supertype may
be created.
3

-- 3 of 24 --

[Text Use cases] | Text_Use_cases.png
Figure 3. Effect of the abstract annotation. Left: The Supertype is abstract. Right: The Supertype is not
abstract.
Using Heterogeneity with an Overview Model allows data of different types to be displayed
together. Heterogeneity can also be used in Relationship and Tree Models. If the Supertype
Document Model is referenced in the Relationship, then any instance of the Subtypes can be part of
the links. In Tree Models, the possibility to create a link to a new document can be restricted to
certain types. This is not possible in Form Model Bindings, where documents of all Subtypes are
listed as Available Items. In this example, only one Relationship Model between the person and
team Document Models would be needed to create links between an employee (Subtype) and a team
or a freelancer (Subtype) and a team. This is investigated in more depth in the tutorial, How to Put It
Together.
What Does Heterogeneity Look Like in the UI?
When a Supertype Document Model is referenced in an Overview Model, the information from all
the respective documents is compiled together into a uniform list. The field references for the
columns are selected from the Supertype Document Model and the data of matching fields of the
Subtype documents is then displayed.
WARNING
If the fields referenced in the Overview Model are not present in the Subtype
Document Model (path must exactly match), the respective column for this
document will be blank. This could lead to blank entries in the overview.
The Variant Dialogue
When the user clicks on the modeled "Add" button in the overview, a dialogue opens to allow the
user to choose the type of document that is to be created. The user can then choose any of the
document types in the group. If there are groups within groups, these will be displayed in a tree
structure in the Variant Dialogue.
If the top-level Supertype is abstract, it will not be shown in this dialogue. Further Supertypes that
are grouped by the top-level Supertype will be shown in the tree structure but may only be selected
if they are not abstract. Once the user makes a selection, the relevant form is opened to allow the
user to enter data.
Figure 4. Variant Dialogue
4

-- 4 of 24 --

The same Variant Dialogue is used when creating new documents which are linked in a Composed
Data Model, or when creating sibling or child nodes in a tree.
Figure 5. Variant Dialogue in a Tree
The buttons to add new documents may also be modeled separately in a Tree Model so the user
may add one specific type of document. Using the example from the use-case, this would mean that
three separate buttons are modeled called "Add Employee", "Add Freelancer" and "Add Intern". In
this case, the Subtype Document Models are referenced and the Variant Dialogue is not shown.
Heterogeneity in Relationships
When a Supertype is used in one of the roles in a Relationship Model, the user is presented with a
Heterogeneous List in the relationship UI component and may select any of the Subtype documents
as well as the Supertype documents, in the case that the Supertype is not abstract.
How Does Heterogeneity Compare to Other A12
Models?
Heterogeneity is a feature that is controlled using Annotations. In other words, Heterogeneity is not
a model, heterogeneity is a way of grouping data types, which allows data from instances of
different Documents Models to be viewed in the UI in a heterogeneous overview.
CAUTION Once the document type has been set, the user can not change the document
type to a different type.
Step-by-Step Instructions
Step 1: Plan the Shared Data Fields
As already mentioned in "What Can I Do With Heterogeneity?", it’s really important to plan the data
structure of your models. The path to shared data fields on the Supertype and Subtype Document
Models must match exactly. In other words, the shared fields need to have the same names and be
in the same groups with the same structure.
Let’s start with the data structure that we’re planning to use. We’re collecting personal data, so we
can create a root group called "People". We can then create two subgroups called "SharedData" and
5

-- 5 of 24 --

"UniqueData". You don’t have to use this structure, but it will allow us to clearly see what we are
doing as we work through this tutorial. If we use the shared data from the use-case, then we should
plan to model these data fields in the "SharedData" group. We can also add an enumeration that we
can use to show what type of person is being shown in the heterogeneous overview. The planned
structure looks like this:
▪ People (Group)
◦ SharedData (Group)
▪ FirstName (String)
▪ LastName (String)
▪ DateOfBirth (Date)
▪ Type (Enumeration: Employee/Freelancer/Intern)
◦ UniqueData (Group)
▪ to be completed
According to the plan above, fields that are not present on all the variants should be added to the
"UniqueData" group. If you want to be able to view any of these fields in the heterogeneous
overview, you must ensure that these fields are also included in the Supertype Document Model
using the same data structure.
Step 2: Model the Document Model for the Supertype
Figure 6. People_DM
NOTE You will need to spend some time modeling steps 2 and 3.
As you can see from the schema in the use-case, employee, freelancer and intern Document Models
are Subtypes of the people Supertype. We’re going to start by modeling the Document Model for the
Supertype: People_DM. For our use-case, we don’t need to add any UniqueData fields as we only
want to display the Shared Data in the overview (Name and Date of Birth).
We can therefore model People_DM using the following groups and fields that we planned in Step 1:
▪ People (group)
6

-- 6 of 24 --

◦ SharedData (group)
▪ FirstName (string field)
▪ LastName (string field)
▪ DateOfBirth (date field)
▪ Type (enumeration field Employee/Freelancer/Intern)
TIP A simple strategy to ensure consistency in the SharedData group is to use an include.
Let’s follow our own advice and use an include.
• Create a Document Model called SharedData_DM.
• Model the SharedData Group on this model as planned.
• Model a Document Model called People_DM.
• Add the Group, "People", and then include SharedData_DM.
If you’re wondering why we don’t need a Form Model for People_DM, the answer is simple. We
need a Document Model to act as the Supertype, but we don’t want to create Supertype documents
and therefore don’t need a Form Model.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
People_DM, SharedData_DM
Figure 7. People_DM with included SharedData_DM
7

-- 7 of 24 --

Figure 8. SharedData_DM
Step 3: Model the Document and Form Models for the
Subtypes
Figure 9. PeopleEmployee_DM
The next step is to model the Document and Form Models for the three Subtypes that we have in the
use-case. The "Create Copy" feature in the Workspace Explorer is a great help at this point. Not only
do we save time, but we also create models with exactly the same field names and structure. This
ensures that the Supertypes and Subtypes will match exactly in terms of the paths to the fields used
in a heterogeneous overview.
NOTE Copying a Document Model will copy any includes that you used as well.
• Click on the context menu on the right-hand side of People_DM in the Workspace Explorer.
• Select "Create Copy".
8

-- 8 of 24 --

• Choose the folder and, as we’re going to start with the Document Model for employees, change
the name to PeopleEmployee_DM.
• Model the "UniqueData" group.
• Add the extra fields shown in the table below to this group.
We also need to model a computation rule that sets the enumeration field to the correct value.
NOTE The Model Reference is saved in the metadata but using a Computation Rule allows
you to model a localized value of your choice.
• Model the computation in the "UniqueData" group.
• Select the computed field to be "Type" using the drop-down menu.
• Leave the precondition empty and set the calculation to "Employee".
Keep creating copies of the People_DM model and adding the unique fields until you’ve completed
the PeopleEmployee_DM, PeopleFreelancer_DM and PeopleIntern_DM Document Models. You
should add a label under Model Settings to each Document Model as this will be used later to select
the Subtype in the Variant Dialogue.
Document Model Field in
UniqueData
Group
Field Type Label Other
Information
PeopleEmployee_
DM
StartDate Date Start date
JobTitle String Job title
ProbationaryPerio
d
Confirm Probationary
period complete
Salary Number Salary Decimal places
min and max = 2
Min Value = 0
Unit = Amount
PeopleFreelancer_
DM
Company String Company
DailyRate Number Daily rate Decimal places
min and max = 2
Min Value = 0
Unit = Amount
PeopleIntern_DM StartDate Date Start date
EndDate Date End date
9

-- 9 of 24 --

Document Model Field in
UniqueData
Group
Field Type Label Other
Information
Supervisor String Supervisor
• Create the Form Models for these Document Models as normal.
• Don’t forget to set the amount suffix to "€" and to add save and cancel buttons at the bottom!
As mentioned in Step 1, if we want to be able to view any of these fields in the heterogeneous
overview, then they also need to be added to the Supertype Document Model using the same data
structure. If, for example, the start date of employees should be shown in the overview, then the
field "StartDate" should also be added to the "UniqueData" group on the Supertype Document Model.
The StartDate of interns would be shown in this column as well, but it would be always blank for
freelancers.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
PeopleEmployee_DM, PeopleFreelancer_DM, PeopleIntern_DM PeopleEmployee_FM,
PeopleFreelancer_FM and PeopleIntern_FM
Figure 10. PeopleEmployee_DM
10

-- 10 of 24 --

Figure 11. PeopleFreelancer_DM
Figure 12. PeopleIntern_DM
11

-- 11 of 24 --

Figure 13. Employee Form
Figure 14. Freelancer Form
12

-- 12 of 24 --

Figure 15. Intern Form
Step 4: Add the Annotations to the Supertype
Document Model
Figure 16. Annotations in Model Settings of People_DM
To link the Supertype with its Subtypes we need to add an annotation to the People_DM Document
Model. We also want to ensure that no documents of the People_DM Document Model can be
created. To ensure this we need to set the Annotation "abstract" = "true" for People_DM.
• Go to the Simple Model Editor and select People_DM.
• Use the sidebar on the left to open the Model Settings.
• Click on "Add" under the Annotations section.
• Add the Annotations shown in the table below.
13

-- 13 of 24 --

Name Value
subTypes PeopleEmployee_DM,PeopleFreelancer_DM,Peop
leIntern_DM
abstract true
WARNING It is important that you do not add any spaces in this list of Subtypes. The
Subtypes must be separated by a comma only, no spaces.
• Click on "Apply" in the bottom right-hand corner.
• Save your model.
TIP If you want to create Supertype documents, set the abstract annotation to "false". Don’t
forget that you’ll need to create a Form Model for the Supertype Document Model.
As discussed in "What Can I Do With Heterogeneity?", the Annotation "superTypes" may be used to
group Document Models by adding a list of Supertype Document Models in the Document Model
Settings of the respective Subtype Document Model. If you prefer to follow this methodology,
remove the "subTypes" Annotation from People_DM and add the "superTypes" Annotation to
PeopleEmployee_DM, PeopleFreelancer_DM and PeopleIntern_DM instead. The value you need to
enter is "People_DM". It is also possible to mix these Annotations and the same result may still be
achieved.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
No new models were created. Annotations were added to People_DM.
Figure 17. Annotations on People_DM
14

-- 14 of 24 --

Step 5: Create the Overview Models
Before we can finish, we need to create an Overview Model to show the heterogeneous data of
employees, freelancers and interns together. We also wanted to see the data separately, so we’re
going to add an overview for the employees too. If you want to, please feel free to add overviews for
the freelancers and interns too!
Create People Overview
Figure 18. People_OM
• Click on "ADD" in the Workspace Explorer and choose "Overview Model".
• Complete the Folder, Name, Locales and Roles and click "OK".
• Select People_DM from the Document Model drop-down.
• Add the columns that reference the shared data that we chose in Step 1.
• Click on the add button in the columns section and choose an element reference from the drop-
down menu.
• Enter a name and label for the column, click apply and move on to the next column.
Column Label (automatically taken from
Field label)
Element Reference
First Name /People/SharedData/FirstName
Last Name /People/SharedData/LastName
Type /People/SharedData/Type
If you have chosen to include an extra field that is only present on one (or more) Subtype_
Document Models you can add this now. In Step 3, we discussed the option to add the "StartDate"
field to the "UniqueData" group on the Supertype Document Model. If you did this, you just need to
create an extra "Start Date" column and choose the correct element reference
(/People/UniqueData/StartDate).
NOTE You can only select fields that have been modeled on the Supertype Document
Model in the "Element Reference" drop-down menu.
15

-- 15 of 24 --

Once you have added the columns we need to add buttons to the overview.
• Switch to the Custom Actions tab.
• Add a delete row action.
• Add button to the minor subheader section.
• Save the Overview Model.
Create Employee Overview
Figure 19. PeopleEmployee_OM
To be able to see the data from the employee Subtype separately, we need to repeat the process for
the employee overview. There are no differences between setting up the standard overview for the
employees and the heterogeneous overview for the people.
TIP
If you prefer to use separate buttons to add the individual variants, then you need to
ask your developer to help you. Once these buttons have been set up, they can be
modeled in the subheader section of the people overview, too.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
People_OM, PeopleEmployee_OM
16

-- 16 of 24 --

Figure 20. People_OM
Figure 21. PeopleEmployee_OM
Step 6: Use Master Detail Module Models to Create the
Modules
17

-- 17 of 24 --

Figure 22. People_MDM
You’re now ready to add each Overview Model to a new module.
• Click on "ADD" in the Workspace Explorer and choose "Master Detail Module Model".
• Complete the Folder, Name, Locales and Roles and click "OK".
• Use the drop-down menu to select the Overview Model we just created in Step 5.
• The Document Models are already added to the Form Mapping section. Simply select the correct
Form Model from the drop-down menu for each Document Model.
• You also need to navigate to the Model Settings tab and enter a label in the Main Menu Label
section.
Apart from the additional form mapping options, there are no differences between setting up the
models for employees and people. The Master Detail Module Models can now be added to the App
Model.
• Click on the PreviewApp_AM and navigate to the Model Settings tab.
• Click on the Add button in the Model References section at the bottom.
• Select "module-masterdetail" and the People_MDM model that we just created using the two
drop down menus.
• Click on the add button again to add the PeopleEmployee_MDM.
• Save the model and you’re finished!
You can now test your models to make sure that everything is working perfectly.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
People_MDM, PeopleEmployee_MDM
18

-- 18 of 24 --

Figure 23. People_MDM
Figure 24. PeopleEmployee_MDM
19

-- 19 of 24 --

Figure 25. Reference MDMs in PreviewApp_AM
How to Test and Troubleshoot Your Models
• Start your workspace using the Preview App Control and log in (admin:a12).
• Connect the Simple Model Editor to the server using the "Configure Server Connection" button
in the top right-hand corner of the Simple Model Editor (admin:a12).
• Deploy your workspace and refresh your browser tab.
Now you can navigate to the People module that you added in Step 6. The model will have the label
that you added to the Main Menu Label section of the Model Settings Tab in the Master Detail
Module Model. In this module you can add documents of the three Subtypes by clicking on the add
button that you added to the custom actions of the Overview Model.
However, with the add button in the employee module, only documents of the Subtype employee
(PeopleEmployee_DM) can be created.
NOTE
All the employee documents are listed in the employee overview that you added in
Step 6. Freelancer and intern documents will not be shown in the employee
overview.
NOTE
If you want to retain this test data, you can save it in your Workspace by clicking on
the "Replace Workspace Data" button in the Workspace Explorer of the Simple
Model Editor.
Check the Simple Model Editor documentation for more information.
20

-- 20 of 24 --

Figure 26. Add an employee, freelancer or intern in the People module
Figure 27. Employees, freelancers an interns in the People module
21

-- 21 of 24 --

Figure 28. Employees in the Employee module
Troubleshooting
1. If you don’t see any labels in the "Please select variant" dialogue, you need to update your
Document Models:
a. Open the Subtype Document Models.
b. Navigate to the settings in the sidebar on the left-hand side.
c. Enter a label for each locale that you’ve used.
d. Connect to the server and redeploy your models.
e. Refresh the preview page.
2. If you see the Supertype variant:
a. Open the Supertype Document Model.
b. Navigate to the settings in the column on the left-hand side.
c. Set the "abstract" annotation to "true".
d. Connect to the server and redeploy your models.
e. Refresh the preview page.
Glossary
22

-- 22 of 24 --

Term Description
Abstract An abstract Document Model cannot be instantiated and so the user
cannot create new instances of this Document Model. This is controlled
using annotations which can be set in the model settings of the supertype
Document Model. This annotation can take the value true or false. When
creating a new document in a heterogeneous context, such an abstract
supertype (abstract = true) will not be offered in the variant dialogue.
When no annotation is added the default behavior is that the supertype
will be offered (abstract = false).
Annotations An annotation is a name-value pair that can be added to the model in the
model settings and all model elements. The application that uses the
Document Model can access those annotations and can use them within
custom implementation, e.g. to show all fields that have an annotation in
bold face.
In the model settings, at least one annotation is set which contains the
roles for this model. The relevant annotations for heterogeneity are
subTypes, superTypes and abstract.
Heterogeneity This is the principle that allows data of different types to be used in a
unified way. This allows, for example, data of different types to be shown
in the same list. In A12, this means that it is possible to model overviews
that contain lists of heterogeneous data.
Heterogeneous
Overview
This is the type of overview used to display different types to be shown in
the same list. When the user chooses to add a new item to a
heterogeneous overview, they must select which type of item they wish to
add and should then be presented with the appropriate form to complete.
For all fields that we intend to display in a heterogeneous overview, it is
necessary that the path to those fields in the Document Models is
identical.
Path The path shows the data structure and provides part of the identity.
In A12 we model paths when we create groups in the Document Model. If
we create a root group "root" and another group within this group,
"subgroup", then the path to a field, "string_field" in "subgroup" would be:
/root/subgroup/string_field.
23

-- 23 of 24 --

Term Description
Subtype A Subtype is one of the different data types which are grouped by the
Supertype. In A12, it is an additional Document Model which is linked to a
Supertype by an Annotation. The data from Subtype models can then be
used as part of a heterogeneous overview. When creating a new
document in a heterogeneous context a choice of all the Subtypes (and
possibly the Supertype) will be offered.
The Subtype Document Models contain some or all of the fields that can
be found in the Supertype Document Model. Please ensure that the path to
those fields in the Document Model is identical. Other fields may exist in
the Subtype Document Models but these will not be displayed in the
heterogeneous overview.
Supertype The Supertype is used to group types of data and use it in a unified way. In
A12 the Supertype is a Document Model (or sometimes a set of models)
and the Annotation "subTypes" is used to show which Document Models
should be grouped under the Supertype. The Supertype can then, for
example, be referenced in an Overview Model so that the heterogeneous
list of data can be viewed. If a Supertype is marked as abstract it will not
be instantiated and as a result will not be offered when creating a new
document in a heterogeneous context.
Variant A variant is a Supertype or Subtype which is offered to the user when
creating documents in a heterogeneous overview. For example, if a
Supertype (abstract = false) has two Subtypes, then three variants will be
offered.
24

-- 24 of 24 --

