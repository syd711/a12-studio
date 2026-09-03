# overall modeling_tutorial_relationships

Tutorial: Relationship Modeling
Prerequisites
The target audience for this tutorial are business analysts. Some prior knowledge of the tools is
assumed. Before you start this tutorial, you should have completed the following training course(s).
For more details on what topics are covered in these courses, please follow the links.
• A12 Fundamentals Training
This tutorial focuses on relationships and the relevant documentation is linked below. The
terminology used in this tutorial is explained in the documentation, but we’ve also included a
glossary of terms in case you forget what a specific term means:
• Relationship Model documentation
• Form Modeling - Binding documentation
• Glossary of Relationship Terminology
This tutorial uses the installer which you can download from geta12.com.
NOTE Please ensure your installer version matches this tutorial.
Use-case
I have a database containing employees and teams, and I want to be able to add different
employees to a team.
I want to be able to select the employees from the employee database so that I can reuse them. This
will save me time as I will not have to enter the employee details (name, job title, salary…) each
time I create a team.
I want to be able to specify when each employee joined the team.
There shouldn’t be any limits on how many teams a team member can be used in. This will allow
me to set up multiple different teams which may use the same employees but have different roles
assigned to the employees.
This can be seen in the schematic diagram below where, for example, Tom is in both Team 1 and
Team 2. Tom joined Team 2 in March 2023, but he has been working in Team 1 since 2016.
1

-- 1 of 36 --

Figure 1. Use-case schema
End Result
At the end of this tutorial, you will be able to deploy your models so that you can:
• Add new employees in the employee module
• Add new teams in the team module
◦ Select employees from the list of current employees to add them to a team
◦ Assign a joining Date for the employee in the team using the Additional Link Field
◦ Save the new team
• View the team in the overview, open, edit and then save changes to the team
• Assign employees to multiple teams
The end result will be similar to the Person and Company modules of the basic workspace.
The use-case can therefore be represented in a modeling schema that can be seen below. The
Document Models for team, employee and the joining date in the team are shown by boxes. The
Relationship Model that we are going to model is then represented by a line connecting the
Document Models.
[Relationships schema 2] | Relationships_schema_2.png
Figure 2. Left: Team and Employee documents with the links between them. Right: The Relationship Model
between the Team and Employee Document Models as shown in the Model Graph Diagram/Data Modeling
Perspective.
If you need to check your work as you do the tutorial, please refer to the expandable sections at the
end of each step:
▼ Click here to see what your project should look like by now
You can find a list of models that you created as well as fullscreen pictures of each step to guide
2

-- 2 of 36 --

you.
Essentials of Relationship Modeling
You may have noticed some differences between the use-case and the end-result sections. This is
largely because the end result uses some specific terminology that you should understand when
using Relationship Models. So, before we get started with the step-by-step modeling guide, let’s clear
up any questions you might have about Relationship Models.
What Can I Do With a Relationship Model?
You can use a Relationship Model to create a connection between two existing Document Models.
An instance of a Relationship Model between two documents is referred to as a link. You can see an
example of this in the use-case where, for example, the employee "Sally" is linked to "Team 1".
While a Document Model contains fields and rules, a Relationship Model has roles and link
constraints. The roles show which Document Models are connected by the Relationship Model and
what roles they play in the relationship. This is trivial for the relationship shown in the use-case
where we create a link between a team and an employee. There are two clear roles, team and
employee.
The role becomes more important in a self-referencing relationship, where the same Document
Model is used for both roles. We could use the same employee Document Model but establish two
clear roles, boss and worker.
NOTE Although the roles appear in a list in the Relationship Model Editor, there is no
hierarchy built in, so it does not matter which order the roles appear in.
The link constraints control the maximum number of instances of this role that can be linked to one
instance of the other role. We refer to this as the multiplicity and, just like a rule, a warning will be
shown in the user interface if this multiplicity is exceeded. If there is no limit, the multiplicity is
called unbounded.
[Link and Additional Link Field] | Text_RelMod.png
Figure 3. Link and Additional Link Field
Add Additional Information and Allow Duplicate Links
In addition to creating a link between two specific instances of Document Models, we can maintain
additional information for each link. These are called Additional Link Fields and they are
modeled on a Link Document Model. This third document is then part of the link.
In the use-case mentioned above, the employee "Sally" is linked to "Team 1". If we model an extra
Document Model with a Field for the employee joining date, we can store the information that Sally
joined Team 1 on May 1st, 2021 in this additional link field when the link is created. This
information is then specific for this link. In other words without Sally and Team 1 we cannot have
Sally’s joining date for Team 1.
3

-- 3 of 36 --

This allows use-cases like the one in this tutorial to be modeled, where the same employee "Tom"
works in two different teams, "Team 1" and "Team 2", but joined them at different dates.
The checkbox "Duplicable" controls, whether it is possible to have two links between the same
documents. This would allow the use-case, that Tom may leave Team 2 at a certain time, but join
Team 2 again at a later time. Note, that the application does not check the content of the Additional
Link Fields.
NOTE
The input for the Link Document Model reference and the checkbox "Duplicable"
are only active if the upper limit for multiplicity is >1 for both related entities in the
Relationship Model.
If you are considering modeling a relationship with additional link fields, you must consider the
multiplicity modeled in the link constraints. There are two possibilities depending on the business
case:
1. If the business case requires that the multiplicity of one entity should be set to 1, then the
additional link field(s) should be added to the Document Model referenced in the entity with a
multiplicity >1.
2. If the business case requires that data be saved in an additional link field, this is an indicator
that the multiplicity constraints should be defined as > 1 for both entities.
Set the Order of Relationship Information in an Application
By default, the links are ordered by their creation time with the newest links displayed at the
bottom of the list in the UI and the oldest links displayed at the top. If you want to change the order
of the links, please ask your developer to enable Orderable. You can then set whether newly
created links are placed on the top or bottom of the respective list.
The order in which the links appear can only be set manually by viewing the links in a tree. After
enabling Orderable for the child node type, the order maintained in the tree will also be displayed
in any Dual Pane Selection or Table List relating to that relationship.
How Can I Apply a Relationship Model to Different
Use-Cases?
The first step in modeling any use-case is planning. When thinking about the relationships
connecting Document Models, we quickly see that the multiplicity is key to setting up different use-
cases.
When the multiplicity is set correctly, the user will only be able to create a plausible number of links
between specific instances of a document. As we assign a multiplicity of 1, any positive integer, or
unbounded to both roles, we have a range of possible combinations open to us.
The most common combinations are listed in the table below:
4

-- 4 of 36 --

Multiplicity of
Role A
Multiplicity of
Role B
Notation in
Documentatio
n
Effect
1 1 1:1 A maximum of 1 document from Role A may be
linked to a single document from Role B.
A maximum of 1 document from Role B may be
linked to a single document from Role A.
1 10
(Example, any
positive integer
possible)
1:n A maximum of 1 document from Role A may be
linked to a single document from Role B.
A maximum of 10 documents from Role B may
be linked to a single document from Role A.
1 unbounded 1:n A maximum of 1 document from Role A may be
linked to a single document from Role B.
There is no limit on the number of documents
from Role B that may be linked to a single
document from Role A.
unbounded unbounded n:n There is no limit on the number of documents
from Role A that may be linked to a single
document from Role B.
There is no limit on the number of documents
from Role B that may be linked to a single
document from Role A.
unbounded unbounded n:n There is no limit on the number of documents or
the number of times any document from Role A
may be linked to a single document from Role B.
There is no limit on the number of documents or
the number of times any document from Role B
may be linked to a single document from Role A.
Duplicable
1:1 Relationship
A 1:1 relationship is a relationship in which both roles have a maximum multiplicity of 1.
If we chose to model this type of relationship for the Employee-Team use-case, then each employee
could only work in one team. In addition, each team could only have one employee. This might
seem strange in the context of employees and teams but is more understandable if we consider a
relationship in which we set a team leader.
A team (1) - employee (1) relationship would allow a single employee to be set as the team leader
with the additional restrictions that each team may only have one team leader and each team
leader must be different. This is useful as the number of links is validated and the user is informed
5

-- 5 of 36 --

when the number of links exceeds the multiplicity.
[Text 121] | Text_121.png
Figure 4. 1:1 relationship
1:n Relationship
A 1:n relationship is a relationship in which one role has the upper limit of 1 as multiplicity, while
the other role has a multiplicity >1 or an unbounded multiplicity.
There are two different ways of applying this type of relationship to the employee-team use-case.
The first is sensible, where a team can have "n" employees but each employee may only be in one
team. The inverse is less sensible, where each employee may be in "n" teams but each team may
only have one employee.
As mentioned before, a role’s multiplicity controls the maximum number of instances of this role
that can be linked to one instance of the other role. A sensible example would, therefore, be a team
(1) - employee (n) relationship, which is shown in the picture below.
[Text 12n] | Text_12n.png
Figure 5. 1:n relationship
n:n Relationship
An n:n relationship is a relationship in which each role has a multiplicity >1 or an unbounded
multiplicity.
To put this in the context of our use-case, where a team can have "n" employees and each employee
may be in "n" teams. We have the option to set n to a specific integer value or to set it to be
unbounded. Using unbounded multiplicity, we can model a team (unbounded) - employee
(unbounded) relationship.
Let’s, however, consider our teams for a moment, should there be a limit on the number of
employees in a team? We could set the multiplicity of the employee role to 30, so a maximum of 30
employees could be linked to a single team. Equally, how many teams can they realistically work in
before they become ineffective? We could set the multiplicity of the team role to 5, so that a
maximum of 5 teams may be linked with a single employee. This would lead to a team (5) -
employee (30) relationship which creates an error in the UI when these limits have been exceeded.
[Text n2n] | Text_n2n.png
Figure 6. n:n relationship
NOTE
n:n relationships are the only type of relationships, that we can add a Link
Document Model to.
This is described in the section, Add Additional Information.
6

-- 6 of 36 --

Self-Referencing Relationships
The use-case and examples that we have discussed so far have always referenced a different
Document Model for each role. It is also possible to reference the same Document Model for both
roles in a relationship. This is then referred to as a self-referencing relationship.
Self-referencing relationships may then be used as any other relationship. Please note, as the same
Document Model is used for both roles, the role becomes more important. As we mentioned
previously, we can use the same employee Document Model but establish two clear roles, team
leader and team member.
NOTE
The depth of Self-Referencing Relationships is unlimited.
Modelers can define the depth of that a Self-Referencing Relationship is queried in
Composed Data Models and Tree Models so that the total amount of data being
loaded is not (theoretically) unlimited.
What Does a Relationship Model Look Like in the UI?
There are plenty of different UI components which can be used on forms to display the links
between documents. Links can be displayed and edited via the Drop Down Selection, the Dual Pane
Selection or the Table List.
Alternatively, the Tree Engine can be used to create a hierarchical overview of linked documents.
We can model additional actions to edit the links and create and link a new document in one step,
i.e. add parents, siblings or children within the tree.
Relationship UI components can be added to an existing Form Model and are configured using the
Binding Editor within the Form Model Editor in the SME. For more information regarding Bindings,
please refer to the Form Modeling - Binding.
Displaying Relationships in a Tree
The Tree Engine can be used to display and edit links. A tree is always hierarchical: the two roles of
a relationship need to be divided into parent and child.
It is possible to display several relationships in one tree, as long as they all descend directly or
indirectly from the same Document Model parent. As all of these Document Models are directly or
indirectly descended from the same Document Model, this one is referred to as the root of the tree.
For more details, please refer to the Tree Model documentation and the tutorial, Tree Modeling.
How Does a Relationship Model Compare to Other A12
Models?
If you have completed the Fundamentals training, you will have already modeled a Repeat on a
Document Model. There a certain parallels between a Relationship Model and a Repeat but each
has its own advantages and disadvantages.
7

-- 7 of 36 --

On the one hand, Relationship Models are very useful for reusing documents which you can view in
an overview or linking together in a tree. This means the user can create links by selecting one or
more existing documents in the Binding. Data in a Repeat exists with the document and can’t be
reused, so the user has to manually enter this data for each new document.
On the other hand, a Relationship Model is more complex than a Repeat and requires more effort.
You can easily add more rows to a Repeat and perform calculations on the data within a Repeat.
You can’t create new documents in a relationship, as there are no UI controls in the Binding to add
or edit linked documents. This is only possible using an extra model, either a Tree Model or a
Composed Document Model. Calculations on data across a link are also difficult to achieve with
relationships, but it is possible using Composed Document Models.
The differences between a Relationship Model and a Repeat on a Document Model are summarized
below to help you decide which to use.
Table 1. Repeat vs Relationships
Repeat Relationship
Data Persistence Information that you enter in
a Repeat will be deleted
when you delete the
document.
Links are between individual documents.
You can delete a document and a link
separately. Also, if you delete one document,
the other linked documents are not
automatically deleted.
User Interaction Three types of repeats are
available in the Form Engine:
• Inline
• Detached
• Embedded
Related data can currently be displayed and
edited in:
• Drop Down Selection (for single
selection)
• Dual Pane Selection (for multiple
selections)
• Table List
Create and Edit
Children
You can create children in
the repeat using nested
repeats.
The data in children can then
be edited in the form.
You cannot create or edit children in the
relationship UI component. You can only
add a link to an existing child document.
To create or edit children, you need to create
a Tree Model and model a button to insert a
new document as a child. Alternatively,
linked documents can be created and edited
using the relationship UI components for a
Composed Document Model.
8

-- 8 of 36 --

Repeat Relationship
Validation &
Computation
Validation and computations
can be performed on data
from a single document
based on a single Document
Model.
Per-default, Validation or
Computation Rules are
evaluated in the frontend
and backend.
Validation or computation across linked
documents is only possible if the relevant
Document Models are included in a
Composed Document Model.
Per-default, Validation or Computation Rules
are evaluated in the frontend only.
Workflows Repeats can be used in
workflows.
Workflows supports Relationships and
Composed Data.
Heterogeneity /
Abstractness
Repeats do not support
heterogeneity.
Abstract supertypes can be used in
heterogeneous relationships. Links can then
be made to super/subtype documents.
Heterogeneity at the root level can be
modeled using a Composed Document
Model for each subtype.
Orderability Data in Repeats can be
ordered.
Links can be ordered when displayed in a
Tree Engine.
Recursion Repeats cannot be recursive. Relationships can be recursive. You can
model a self-referencing relationship.
Performance when
Scaling
An increasing number of
rows in a repeat will cause
the Form Engine to become
slower.
Relationships can deal with large sets of
documents efficiently.
Step-by-Step Instructions
Step 1: Plan Your Relationship
The first step should be thoroughly planning your relationship. As discussed in the Essentials of
Relationship Modeling, a relationship either connects two different Document Models or connects
back to the same Document Model. In our use-case, this means that we will use one role "Team" and
one role "Employee".
As there should not be any limits on how many teams a team member can be assigned to, the
multiplicity of the Team role must be > 1. We also want to be able to have multiple employees in
each team. This means that the multiplicity of the Employee role must be > 1. For simplicity, the
multiplicity for both roles should be unbounded.
The multiplicity of the two roles means that the date on which the employee joined the team can be
stored in the Additional Field, as described in the section, What Can I Do With Relationships.
9

-- 9 of 36 --

TIP
It is generally better to model an explicit upper limit for the Multiplicity rather than
using the "Unbounded" checkbox.
This can help with:
• Performance
• Security (Denial of Service attacks)
as the number of Links cannot exceed this limit.
This is summarized in the table below.
Team
Multiplicity
Employee
Multiplicity
Notation in
Documentatio
n
Effect
unbounded unbounded n:n There is no limit on the number of Team
documents that may be linked to a single
Employee document. This means, each employee
may be in an unlimited number of teams.
There is no limit on the number of Employee
documents that may be linked to a single Team
document. This means, each team may have an
unlimited number of employees.
Step 2: Model the Document and Form Models
Figure 7. Employee_DM
Now that we know what we’re going to do, we need to create the Document and Form Models for
employees and teams. This should be done using the Simple Model Editor using the skills you
learned in the Fundamentals training.
• Model Document Models for the employee and team.
You can add as many fields as you like to the Document Models. The fields that will be used in this
10

-- 10 of 36 --

tutorial are shown in the table below.
Fields on Team_DM Fields on Employee_DM
Name (String) FirstName (String)
Location (String) LastName (String)
JobTitle (String)
StartDate (Date)
• Model a Form Model for each Document Model.
• Model save and cancel buttons at the bottom of the Form.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
Employee_DM, Team_DM, Employee_FM, Team_FM
Figure 8. Employee_DM
11

-- 11 of 36 --

Figure 9. Team_DM
Figure 10. Preview of Employee_FM
12

-- 12 of 36 --

Figure 11. Preview of Team_FM
Step 3: Create the Additional Link Field Models
Figure 12. TeamEmployee_LinkFields_DM
To be able to add the joining date for each employee to each team, we need a further Document
Model and Form Model for this data. In this use-case, we are only going to add one field
JoinedTeamOn, to hold the joining date.
We strongly advise using the Model Naming Conventions. This means that you will be able to easily
find models relating to the relationship.
We are going to call this model for the Additional Link Fields, TeamEmployee_LinkFields_DM. This
is based on the convention Relationship_LinkFields_DM.
• Model the Document Model (TeamEmployee_LinkFields_DM).
13

-- 13 of 36 --

• Model the Form Model (TeamEmployee_LinkFields_FM).
• Once again, please add save and cancel buttons to the bottom of the form.
CAUTION Please ensure that the "Save" button uses the event, "event_submit", so that the
popup window closes after saving the data.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
TeamEmployee_LinkFields_DM, TeamEmployee_LinkFields_FM
Figure 13. TeamEmployee_LinkFields_DM
14

-- 14 of 36 --

Figure 14. Additional Link Form
Step 4: Create the Relationship Model
Create the Relationship Model
Figure 15. Create the Relationship Model
• Click on "ADD" in the Workspace Explorer and choose "Relationship Model".
• Complete the Folder, Name, locales and roles and click "OK".
Once again, we’re going to use the naming convention from the Model Naming Conventions so that
the Relationship Model is called TeamEmployee.
Add Related Entities
15

-- 15 of 36 --

Figure 16. Add the Team role to Related Entities
The first step is to add the roles to the "Related Entities" section.
• Click on the "Add" button.
• Enter "Team" in both the "Label" and "Role" fields.
• Select the Team_DM Document Model using the drop-down menu and check the "unbounded"
checkbox to set the multiplicity.
• This role is now complete so click "Add" / "Apply".
• Click "Add" to complete the second role using the Employee_DM Document Model.
NOTE You could also set an explicit upper limit for the Multiplicity as mentioned in Step 1
Add the Link Document Model
Figure 17. Add the Link Document Model
Link Document Models can be added using the drop-down labelled "Link Document Model
Reference". This field is used to select the Document Model that we created in Step 3.
• Select TeamEmployee_LinkFields_DM from the list.
16

-- 16 of 36 --

Generate Document Models
Figure 18. Generate Document Models
We now need to generate some Document Models to allow the relationship to work correctly. The
Simple Model Editor does this for us automatically, but you need to save the Relationship Model
first.
• Save the Relationship Model.
The "Generate Document Models" button in the bottom left-hand corner is now active.
• Click "Generate Document Models" button to create the additional Document Models.
• Select a folder for these models and click "Continue".
Two Document Models will be created with names according to the following convention,
Relationship_Role___generated. In our case, the Document Models are called
TeamEmployee_Team___generated and TeamEmployee_Employee___generated.
CAUTION
If you make any changes to the relationship after you have generated these
Document Models, you should:
• Save the Relationship Model.
• Re-use the "Generate Document Models" button.
This will make sure that the additional Document Models required for the
relationship are up-to-date.
Note on Modeling Multiplicity
Please note, if you are modeling relationships with a different multiplicity, e.g. 1, you should leave
the "unbounded" checkbox blank. Instead, enter the upper limit for the multiplicity in the field to
the left of the checkbox. In the case of a role with multiplicity = 1, you would simply enter 1 in this
field.
It is generally recommended to model an explicit upper limit for the multiplicity as this can
improve performance and prevent security issues. This means modeling, for example, a 10:999
17

-- 17 of 36 --

relationship instead of an unbounded n:n relationship.
The modeling of a Drop Down Selection which is recommended for maintaining links to a role with
multiplicity = 1 is then covered in Step 5c.
CAUTION A Link Document Model can only be used in an n:n relationship
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
TeamEmployee, TeamEmployee_Team___generated, TeamEmployee_Employee___generated
Figure 19. TeamEmployee, Team Role
18

-- 18 of 36 --

Figure 20. TeamEmployee, Employee Role
Figure 21. TeamEmployee, Generate Document Models button is disabled
19

-- 19 of 36 --

Figure 22. TeamEmployee after saving, Generate Document Models button is enabled and has been
clicked to generate TeamEmployee_Team___generated and TeamEmployee_Employee___generated
Step 5: Create the Binding in the Form Model
As discussed in, "What Does a Relationship Model Look Like in the UI", Bindings are used to
configure the relationship UI components to display and maintain the links between Document
Models. Bindings are added and positioned in the Form Model.
There are different UI components that can be used and the best choice depends on:
• The type of relationship you have
• If you want the user to be able to edit the links
• How you want the information presented
The UI components are summarized in the table below.
UI Component Recommended
for Target Role
of Multiplicity
Description Planned Use
Drop Down
Selection
1 Set a link to a single document
from a drop-down list of all
available documents.
Dual Pane
Selection
>1 Set (multiple) links to documents
from a list of all available
documents. Linked documents are
then shown in a second pane.
Use on Employee_FM to
select teams
20

-- 20 of 36 --

UI Component Recommended
for Target Role
of Multiplicity
Description Planned Use
Table List A read only view of the linked
document(s).
Table List with
Dual Pane
Selection
>1 Adding a Dual Pane Selection to a
Table List adds an Edit button at
the bottom of the Table List. This
button opens a Dual Pane Selection
to allow the user to maintain the
links.
Use on Team_FM to view
and select employees
As our relationship is an n:n relationship, we will model the two UI components for multiplicity > 1.
We will model a Dual Pane Selection for the Employee_FM Form Model and a Table List with Dual
Pane Selection for the Team_FM Form Model.
Step 5a: Binding on Employee_FM
Create Binding
Figure 23. Create TeamEmployee_Binding
Bindings are added in the Simple Model Editor within a Form Model by drag-and-drop or by
selecting a Binding in the context menu. If the Document Model, on which the current Form Model
is based, is referenced in a Relationship Model, this Relationship Model will be listed in the
"Relationships" section on the right-hand side of the Simple Model Editor.
• Open the Employee_FM Form Model.
• Decide which screen or section the relationship UI component should appear on.
• Expand the "Relationships" section of "Data Models" on the right-hand side of the Simple Model
Editor.
• Drag and drop the Relationship "TeamEmployee" into the screen or section of your choice.
A Binding is added to the Form Model with the default name, TeamEmployee_Binding. If you open
the Binding Editor by clicking on TeamEmployee_Binding, you will see that the Relationship and
21

-- 21 of 36 --

Target Role are automatically completed. In this case, the Relationship, "TeamEmployee" and Target
Role, "Team" are selected.
The Binding Editor will now open on the right-hand side of the Simple Model Editor.
Add Component
We want to use a Dual Pane Selection on Employee_FM to allow teams to be selected.
• Select Dual Pane Selection from the drop-down list "Component Type".
Add Additional Link Fields Form
Figure 24. Add TeamEmployee_LinkField_FM as the Additional Link Fields Form
The next step is to select the additional Form Model from Step 3.
• Select TeamEmployee_LinkFields_FM in the drop-down menu, "Additional Link Fields Form"
Create Available Items Overview
Figure 25. Create Available Items Overview Model
We now need to create simplified overviews that we can use to view and select the items.
• Click the "+Add" button on the right of the Available Items Overview. (This button is a "+" button
in the new Binding Editor.)
22

-- 22 of 36 --

This opens a specialized version of the Overview Model Editor, which we will use to add columns,
just like columns are added to any overview. The Document Model, "Team_DM", is pre-selected
based on the "Target Role".
• Add both the Name and Location fields to the columns by clicking on "Add" and selecting the
correct "element reference".
• Switch to the Models Settings tab.
• Ensure that the roles we’ve chosen for the Form Model are added to this overview as well.
You can model various other settings that will be applied to the Dual Pane Selection later on, but for
now, we will leave these settings.
• Click "Save" to save this Available Items Overview and close the editor.
This creates a new overview model called TeamEmployee_Team_AvailableItems_OM.
CAUTION
Overview Models used in Bindings have a round icon and are decorated with a
small "Re" in the Workspace Explorer.
These Overview Models will always have restricted functionality and therefore
should not be re-used in other contexts.
Create Selected Items Overview
Figure 26. Create Selected Items Overview Model
We can now repeat this process for the Selected Items Overview. The
"Relationship_Role___generated" Document Models that we generated in Step 4 are referenced in
the Selected Items Overview.
As *_*___generated Document Models combine fields from the individual roles with the Link Fields,
fields from both target role (Team_DM) and the Link Document Model
(TeamEmployee_LinkFields_DM) can be referenced in the Selected Items Overview.
NOTE The *_*___generated Document Models are not used in the backend and simply
support modeling Bindings with data from Link Documents.
23

-- 23 of 36 --

• Click the "+Add" button on the right of the Selected Items Overview. ("+" button in the new
Binding Editor.)
• Add the same 2 columns that we used in the Available Items Overview (Name and Location).
• Switch to the Models Settings tab.
• Ensure that the roles we’ve chosen for the Form Model are added to this overview as well.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
TeamEmployee_Team_AvailableItems_OM, TeamEmployee_Team_SelectedItems_OM,
Figure 27. Modeling TeamEmployee_Team_AvailableItems_OM
24

-- 24 of 36 --

Figure 28. Employee Form Model with TeamEmployee_Binding
Step 5b: Binding on Team_FM
Create Binding
Figure 29. Create TeamEmployee_Binding
We now need to add a Binding to Team_FM. Once again, we will add this Binding by drag-and-drop.
But this time, we will add a Table List with Dual Pane Selection as the relationship UI element. In
the Binding Editor we will select Table List as component type and then set the switch "Has Edit
Modal" to "yes".
• Open the Team_FM Form Model.
• Decide which screen or section the relationship UI component should appear on.
• Expand the "Relationships" section of "Data Models" on the right-hand side of the Simple Model
Editor.
25

-- 25 of 36 --

• Drag and Drop the relationship "TeamEmployee" into the screen or section of your choice.
A Binding is added to the Form Model with the default name, TeamEmployee_Binding. If you open
the Binding Editor by clicking on TeamEmployee_Binding, you will see that the Relationship and
Target Role are automatically completed. In this case, the Relationship, "TeamEmployee" and Target
Role, "Employee" are selected.
Add Component
Figure 30. Add Table List component with edit modal
This Binding is now modeled by adding the Table List component and then setting the switch, "Has
Edit Modal" to "yes".
The updated layout of the editor means that you will now have the option to add all 3 overviews
that you require.
NOTE
The Overviews and Additional Field Form Model selection are displayed in the
following order in the Binding Editor
• Selected Items Overview (for the Table List)
• Additional Link Fields Form (editable in the Dual Pane)
• Available Items Overview (for the Dual Pane)
• Selected Items Overview (for the Dual Pane)
• Model a new Selected Items Overview using the "+" button on the right-hand side.
• Add the FirstName, LastName and JoinedTeamOn fields as columns to this overview as well so
that we can see these 3 fields in this Table List.
• Click "Save".
• Select the additional Form Model from Step 3 (TeamEmployee_LinkFields_FM).
• Create a new Available Items Overview for the Dual Pane Selection using the "+" button on the
right-hand side.
• Add the FirstName and LastName fields to this overview.
• Click "Save".
26

-- 26 of 36 --

• Re-use the Selected Items Overview that you just created by selecting this in the drop-down.
• Click "Apply" to finish editing the Binding.
• Save the Form Model.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
TeamEmployee_Employee_AvailableItems_OM, TeamEmployee_Employee_SelectedItems_OM
Figure 31. Modeling TeamEmployee_Team_SelectedItems_OM
27

-- 27 of 36 --

Figure 32. Team Form with TeamEmployee_Binding
Step 5c: Bindings with a Drop Down Selection
In Steps 5a and 5b, we modeled the Dual Pane Selection and the Table List with a Dual Pane
Selection. If we had a different use-case, then it may be more appropriate to model a Drop Down
Selection. As you can see from the Table at the start of Step 5, this is the recommended relationship
UI component when the target role has a multiplicity of one.
The process of modeling a Drop Down Selection on a Form Model is very similar to the process
explained in Step 5a.
Add Drop Down Selection
• Create the Binding and select the relationship and target role from the drop-down menus.
Please consider the multiplicity of the target role, as we’re modeling a Drop Down Selection, we
should choose a target role with a multiplicity of one.
• Click on the "Add" button in the "Components" section and then choose "Drop Down Selection"
from the drop-down menu.
The subsequent steps are then as described in Step 5a.
NOTE Drop Down Selections will only show one column (the first column) from the
Available Items and Selected Items Overviews.
NOTE An expression column combining multiple field values may be modeled.
CAUTION Take care to test expression columns carefully as the search functionality of the
28

-- 28 of 36 --

autocomplete widget is based on data in the document and does not use the
output of the expression.
Step 6: Create the Overview Models
Create Team Overview
Figure 33. Create Team overview
Before we finish, we need to create Overview Models to add new and list existing employees and
teams.
NOTE
Overview Models used in Bindings have a round icon and are decorated with a
small "Re" in the Workspace Explorer.
These Overview Models will always have restricted functionality and therefore
should not be re-used in other contexts.
• Click on "Add" in the Workspace Explorer and choose "Overview Model".
• Complete the Folder, Name, Locales and Roles and click "OK".
• Select Team_DM from the Document Model drop-down.
• Add the columns to the overview. Let’s create two columns from fields we modeled in Step 2,
Name and Location.
• Switch to the Custom Actions tab and add a delete row action and an add button to the minor
subheader section.
• Save the Overview.
Create Employee Overview
29

-- 29 of 36 --

Figure 34. Create Employee overview Model
• Repeat the process for the Employee overview.
• Let’s add 3 columns First Name, Last Name and Job Title.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
Team_OM, Employee_OM
Figure 35. Team_OM
30

-- 30 of 36 --

Figure 36. Employee_OM
Step 7: Use Master Detail Module Models to Create the
Modules
Figure 37. Create Employee Master Detail Module Model
You’re now ready to add each Overview Model to a new module.
• Click on Add in the Workspace Explorer and choose "Master Detail Module Model".
• Complete the Folder, Name, Locales and Roles and click "OK".
• Use the drop-down menu to select the Overview Model we just created in Step 6.
• The Document Models are already added to the Form Mapping section. You simply need to
select the correct Form Model from the drop-down menu for each Document Model.
• You also need to navigate to the Model Settings tab and enter a label in the Main Menu Label
31

-- 31 of 36 --

section.
• Repeat the process for the second Overview Model.
The Master Detail Module models can now be added to the App Model.
• Click on the PreviewApp_AM and navigate to the Model Settings tab.
• Click on the Add button in the Model References section at the bottom.
• Select "module-masterdetail" and the Team_MDM model that we just created using the two drop
down menus.
• Click on the add button again to add the Employee_MDM.
• Save the model and you’re finished!
You can now test your models to make sure that everything is working perfectly. Remember, that
Master Detail Module Models are only added to the Preview App, if the App Model or the whole
workspaces is deployed from the Simple Model Editor.
▼ Click here to see what your project should look like by now.
These are the models that you created in this step:
Team_MDM, Employee_MDM
Figure 38. Team_MDM
32

-- 32 of 36 --

Figure 39. Employee_MDM
How to Test and Troubleshoot Your Models
• Start your workspace using the Preview App Control and log in (admin:a12).
• Connect the Simple Model Editor to the server using the "Configure Server Connection" tab in
the top right-hand corner (admin:a12).
• Deploy your workspace and refresh your browser tab.
• Click on the Teams module in your application and add some teams.
• Once you have created Team 1 and Team 2 in the Teams module, you can create employees and
add them to teams using the Dual Pane Selection in the Employees detail view.
We can then enter the information we need to match the schema in the use-case. This is shown in
the three screenshots below.
NOTE
If you want to retain this test data, you can save it in your Workspace by clicking on
the "Replace Workspace Data" button in the Workspace Explorer of the Simple
Model Editor.
Check the Simple Model Editor documentation for more information.
33

-- 33 of 36 --

Figure 40. Testing the Employee overview
Figure 41. Testing the Team overview: Team 1
34

-- 34 of 36 --

Figure 42. Testing the Team overview: Team 2
Glossary
Term Description
1:n relationship A 1:n relationship is a relationship in which one role has the upper limit
of 1 as multiplicity, while the other role has a multiplicity >1 or an
unbounded multiplicity.
n:n relationship An n:n relationship is a relationship in which both roles have a
multiplicity >1 or an unbounded multiplicity.
Binding Used to integrate relationship UI components into the form engine. The
binding allows a UI for linking documents to a single "main" document to
be modeled.
Candidate Refers to the set of instances to which a link could be created. As of now,
it is "all" the instances of the target role’s Document Model to which no
link exists yet. Synonym to "Available Items".
Drop Down Selection A relationship UI component, the best suitable if the target role has a
multiplicity with an upper limit of 1.
Dual Pane Selection A relationship UI component, the best suitable if the target role has
multiplicity with an upper limit ≥ 2 or unbounded.
Duplicable When Duplicable is selected, multiple links of the same type between the
same two documents are allowed.
Generated Document
Model
Relationship-specific Document Models that need to be generated using
the Relationship Model Editor. They are used in the Overview Models for
relationship UI components for the Selected Items Overview.
35

-- 35 of 36 --

Term Description
Link An instance of a Relationship Model; specifies the connection between an
instance of one role of the relationship and an instance of the other role
of the relationship.
Link Constraints Properties of a relationship role; currently limited to multiplicity.
Link Order The order in which links are displayed in the tree engine or relationship
UI elements.
Multiplicity Specifies the maximum number of instances of this role that can be
linked to one instance of the other role; can be a number (upper limit) or
unbounded.
Orderable When Orderable is selected, links that are displayed in a tree engine can
be reordered manually in the UI. The links are shown in the same order
in other relationship UI components.
Relationship Model Determines which Document Models are connected due to a business
requirement. Their roles and multiplicity as well as the reference to a
Link Document Model containing Additional Link Fields are maintained
in the Relationship Model as well.
Role The role is used to identify the part that a document plays in the link to
another document.
Self-Referencing
Relationship
Both roles are taken up by the same Document Model, thus making it
possible to create links between instances of one Document Model.
Table List A relationship UI element in which links are displayed; read-only.
Target Role A setting for the relationship UI elements maintained in the Binding
Editor that determines which role of the links is shown and edited.
Unbounded The multiplicity of a role is unbounded if the maximum number of
instances of this role that can be linked to one instance of the other role is
unlimited.
Upper Limit Specifies the maximum number of instances of this role that can be
linked to one instance of the other role.
Relationship UI
Component
UI element in which relationship links can be edited and/or displayed:
Drop Down Selection, Dual Pane Selection and Table List
36

-- 36 of 36 --

