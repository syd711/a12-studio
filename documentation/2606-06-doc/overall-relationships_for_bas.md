# overall relationships_for_bas

Relationship Modeling
This document serves as an introduction to A12 Relationships.
The target audience for this documentation is the business analyst. Some prior knowledge of the
tools is assumed.
In the following chapters, you can find out about the properties maintained in a Relationship
Model, how Relationships are displayed and in what ways they currently differ from repeats.
Frequently used terms can be found in a Glossary. Please also refer to our tutorial for Relationship
Modeling and learn how to link documents and why you might need to use relationships in your
project in a step-by-step guide.
It is also possible to use Heterogeneity in Relationships. For details, refer to the documentation for
Modeling Heterogeneous Data.
If you are a developer wishing to include Relationships in your A12 project, please also refer to the
sections covering Relationships in the Client documentation and the Data Services documentation.
The installer Preview App comes with Relationships already set up but the Project Template does
not. So if your project is based on the Project Template, or if you have not already enabled your
project to include Relationships, you will need to follow the Client and Data Services documentation
linked above.
The Relationship Model and its Properties
In A12 we have the possibility to express the connection between entities with Relationships. An
example could be a company’s organisational structure: There are many persons (employees) and
many teams. The persons may work for one or several different teams in different positions and for
different time spans. One larger team could also contain subteams.
A12 Relationships introduce another model: the Relationship Model, in which a connection
between two Document Models can be specified.
A Relationship Model is a data model and as such, can be compared to a Document Model. While
the Document Model speaks of fields and rules, the Relationship Model speaks of roles and link
constraints. In the example above, there would be the Document Models Team_DM and Person_DM,
a Relationship Model could describe the connection between them.
An instance of a Document Model is called a document. In the example, there could be documents
representing teams T1 and T2, and persons P1, P2 and P3. An instance of a Relationship Model is
called a link. A link in the example above could represent a concrete connection between team T1
and person P1, team T2 and person P2 and team T2 and person P3.
Related Document Models
A Relationship Model always connects two Document Models (entities) with each other. The
connection does not have a direction.
1

-- 1 of 39 --

CAUTION Document Models that should be connected via a Relationship must possess
only one root group each.
Each Document Model takes up a Role in a Relationship. A Document Model in a certain
Relationship is then referred to by its Role.
For each Role, the number of allowed links needs to be specified: the multiplicity, a link constraint.
It is the maximum number of instances of this Role that can be linked to one instance of the other
Role. If there is no limit, the multiplicity is called unbounded.
In the team-person example above: A person can be part of one but also several teams, and a team
can have many people.
• the allowed number of instances of the Role Team to be linked to one instance of the Role
Person is not limited → multiplicity for the Role Team is unbounded → "One person can be a
member of many teams."
• the allowed number of instances of the Role Person that can be linked to one instance of the
Role Team is not limited → multiplicity for the Role Person is unbounded → "One team can
consist of many persons."
• this results in a team:person Relationship of n:n, where n is "unbounded".
Find more examples in the chapter Common Relationship Types.
Link Document Model and Duplicate Links Allowed
In case of an n:n Relationship, it is possible to maintain additional information. This information
can be different for each link. The information is stored in so-called Additional Link Fields which
are modeled in a separate Link Document Model. There are no constraints towards the amount or
types of fields that can be used as Additional Link Fields. In the example mentioned above, the
additional information could be the Position, StartDate and EndDate for each team-person link.
If the checkbox "Duplicable" is checked, the end user can create multiple links between the same
two documents. Additional Link Fields can then be used to distinguish the different links. This
would be used in the example: A person can leave a team at a specific time (EndDate), but rejoin the
same team later (with a different Position). Please be aware that the content of possible Additional
Link Documents is not validated in terms of uniqueness across multiple links.
The Link Document Model reference and the checkbox "Duplicable" are only active for n:n
Relationships. The Relationship Model Editor shows a warning if a 1:n or 1:1 Relationship Model
has a Link Document Model or Duplicable set. There are two possibilities to resolve this issue
depending on the business case:
1. The business case requires that the multiplicity of one role equals 1: The field(s) that were
modeled in the Link Document Model should be transferred to the Document Model of the Role
with multiplicity >1. This can easily be achieved by including or inserting the fields of the Link
Document Model. (To remove the Link Document Model reference in the Relationship Model,
set both multiplicities temporarily to >1.)
2. The business case requires that data is saved in Additional Link Fields: This is an indicator that
2

-- 2 of 39 --

the multiplicity constraints should be defined as >1 for both roles.
Link Order
It needs to be specified if links of one instance of a Role to instances of the other Role can be
ordered by the user. By default, the setting Orderable is disabled and the links are always indexed
in ascending order by their creation time, i.e. the newest links are displayed at the bottom of a list
in the UI and the oldest are displayed at the top. If the setting Orderable is enabled for the role, it
can be set by a developer whether newly created links shall be placed on the top or bottom of the
respective list. Furthermore, if the child role of a relationship that is used in a tree model is set to
Orderable, the order of siblings of the same parent can be changed via drag & drop.
NOTE
Manual ordering of links is only possible if the Relationship is displayed in a Tree
Engine. A Relationship Model with Orderable Roles will not enable manual ordering
in a Dual Pane Selection or Table List in a Binding. However, a user determined
order maintained in a tree will also be displayed in a Dual Pane Selection or Table
List for the same Relationship and Role.
Modeling a Relationship Model
This section will only cover modeling the Relationship Model itself. In order to use a Relationship in
your application, several different models are needed. An overview and step-by-step guide is given
in the Modeling Relationships tutorial.
Relationship Models can be modeled in the Simple Model Editor (SME). If you want to create a new
Relationship Model, you can do so via the "Add" dialogue in the SME:
Then, a modal will be displayed in which you need to add meta information for your Relationship
Model:
3

-- 3 of 39 --

The folder in which the model is stored depends on your project’s settings. If you use the installer
Preview App you can store the Relationship Model in the "models" folder of the workspace.
Model name, locales and roles depend on your project. The model name needs to be unique in your
workspace.
Relationship Model Editor
The newly created Relationship Model is now opened in the Relationship Model Editor. The meta
information given before can be found and changed in the tab "Model Settings".
4

-- 4 of 39 --

NOTE The labels which can be maintained for the Relationship Model are currently not
used in the default UI for Relationships.
In the tab "Relationship", the "Related Entities" as well as additional information can be
maintained. Via the "Add" button it is possible to add exactly two entities and define the Document
Model and Role. Furthermore, link constraints as well as labels are maintained here.
The labels will be displayed above the Relationship UI components in Form Model Bindings. They
are optional.
For each Role, the number of allowed links, the multiplicity, can be set in the section "Link
Constraints". The "Upper Limit" specifies the maximum number of instances of this Role that can be
5

-- 5 of 39 --

linked to one instance of the other Role. If there is no limit, the multiplicity is "Unbounded".
In case of an n:n Relationship, a reference to a Link Document Model can be chosen and it can be
selected, whether links between the same two documents are allowed. (See example below.)
Generated Document Models
Relationships require a special set of Document Models, the generated Document Models. They can
be generated in the SME using the button "Generate Document Models" in the footer of the
Relationship Model Editor. When using this button, two Document Models will be created in the
folder you selected, one for each Role of the Relationship. An export is triggered as well. The
generated Document Models abide by the naming convention RelationshipName_Role____generated.
CAUTION Do not change the name or content of the generated Document Models.
The generated Document Models will later be used as reference in the Overview Models intended
for a UI element (Binding) displaying the linked documents in a form. If a Link Document Model is
referenced, it will be included in the generated Document Models.
NOTE
The roles assigned to the generated Document Models are calculated by performing
an intersection over the roles defined in the Relationship Model and all related
entity Document models, including the optional link Document Model.
As a result of this intersection, some roles may be omitted from the generated
Document Models. It can cause runtime issues, as authorization checks may fail
when required roles are missing, therefore please ensure that all related models
define the necessary overlapping roles before generating Document Models.
6

-- 6 of 39 --

Common Relationship Types
When talking about Relationships, you might encounter different types that commonly occur: 1:n
Relationships, n:n Relationships and self-referencing Relationships. You can find examples for these
Relationship types below along with an example of how to use Additional Link Fields.
Linking two different Document Models
1:n Relationship
A 1:n Relationship is a Relationship in which one Role has the upper limit of 1 as multiplicity, while
the other Role has a multiplicity >1 or an unbounded multiplicity.
An example would be a yearly report for a person: Person (1) - Report (n). The report is only
about/from one specific person, but each person can be subject of/create many reports. Properties,
like DateSubmitted, that you might associate with the Relationship, would be modeled into
Report_DM.
[relshtype 1 n person report] | relshtype_1_n_person_report.png
Figure 1. 1:n Relationship
n:n Relationship
An n:n Relationship is a Relationship in which each Role has a multiplicity >1 or an unbounded
multiplicity. An example would be a team-person Relationship: each team can contain several
persons and each person can be part of several teams.
[relshtype n n team person withoutLinkfields] | relshtype_n_n_team_person_withoutLinkfields.png
Figure 2. n:n Relationship
Additional Link Fields
As mentioned above, additional information about the link can be stored in Additional Link
Fields. This is optional. The additional information is modeled as fields in the Link Document
Model. It can contain as many fields as you like. For the example Relationship TeamPerson, the
Additional Link Fields could be Position, StartDate, EndDate.
[relshtype n n team person withLinkfields] | relshtype_n_n_team_person_withLinkfields.png
Figure 3. n:n Relationship with Additional Link Fields
NOTE
In the example above, the last person has two links to the same team. This could
represent different timeframes (StartDate, EndDate) or Positions. Whether two links
between the same documents are allowed or not, is controlled with the "Duplicable"
setting.
7

-- 7 of 39 --

Self-Referencing Relationship
In a self-referencing Relationship, both Roles are taken up by the same Document Model, thus
making it possible to create links between instances of one Document Model. They can be 1:n or n:n
Relationships.
An example for a 1:n self-referencing Relationship would be a Relationship between teams and
subteams: each team can have several subteams, but each subteam has only one parent-team.
[relshtype 1 n selfref team team] | relshtype_1_n_selfref_team_team.png
Figure 4. self-referencing Relationship
Many Relationships in One Domain
One Document Model can be part of several Relationships. Person_DM is part of the Relationships
TeamPerson and PersonReport. Moreover, the same two entities can be connected by multiple
Relationships. For example, instead of one PersonReport Relationship, there could be two:
ReviewerReport and RevieweeReport. Those can have different properties. While there is only one
reviewee per report, there could be many reviewers per report.
Figure 5. Multiple Relationships between same entities
All the example Relationships are shown schematically in the tree below, there are 1:n
Relationships TeamReport, RevieweeReport and ReviewerReport, a self-referencing 1:n
Relationship TeamTeam and an n:n Relationship with Additional Link Fields TeamPerson.
[Types of relationships hierarchical] | Types_of_relationships_hierarchical.png
Figure 6. Many Relationships displayed in a tree
8

-- 8 of 39 --

How we Display Relationships in A12
In A12 there are a number of UI elements called Relationship UI Components available which can
be used to display the links between entities:
• in the Form Engine as Drop Down Selection, Dual Pane Selection or Table List (Bindings)
• in the Tree Engine as an hierarchical display with additional means to edit and create new links
and linked documents
Form Engine
A relationship can be displayed in the Form Engine by a Binding. For more details, please refer to
the Binding Model documentation.
Tree Engine
The Tree Engine can be used to display and edit links as well. A tree is always hierarchical: the two
Roles of a Relationship need to be divided into parent and child. It is possible to display several
Relationships in one tree, as long as they all descend directly or indirectly from the same Document
Model parent. A Role of this Document Model does then take up the root of the tree. For more
details, please refer to the Tree Model documentation.
User Determined Order of Links
Selecting "User Determined Order" for a Role means that links to this Role can be ordered manually
in regards to their siblings.
Ordering links by the user is currently only possible in the Tree Engine. Drag and drop needs to be
enabled in the corresponding Tree Model. However, the user determined order will also be shown
in the Bindings in the Form Engine.
Differences Between a Relationship and a
Repeat
Sometimes it can be difficult to understand why a modeler would choose to use a Repeat to display
their data or whether it would make more sense to model Relationships. The table below describes
the differences between data in Repeats and Relationships.
Table 1. Repeat vs Relationships
9

-- 9 of 39 --

Repeat Relationship
Ownership Data in Repeats is fully
owned by the document that
it appears in. If the document
is deleted, the data in the
Repeat is deleted with all
other data on the form.
Relationship links cannot currently be
owned. The related documents continue to
exist even if a parent document is deleted.
Ownership is planned for future releases of
Relationships and is already in specification.
Modeling Simple modeling - Modeled
as a repeatable group in the
Document Model, selected
fields from the group are
added to the Repeat in the
Form Model Editor of the
SME
Complex modeling – several Document
Models, Form Models, Overview Models and
a Relationship Model are necessary. There is
a complex process which must be followed
to add a Relationship.
Engines Form engine Form Engine, Relationship Engine, Tree
Engine, Overview Engine
Interaction patterns Three types of Repeats are
available in the Form Engine:
• Inline
• Detached
• Embedded
Linked documents can currently be
maintained in a Drop Down Selection (for
single selection/to-1 Relationship), a Dual
Pane Selection or shown in a Table List (for
multiple selection/to-many Relationship).
Creation of children Can create children in the
Repeat
There is a requirement to allow creation of
document and linking in one step but
currently you cannot create a child in the
Bindings. The user can only add a link to an
existing document. The Tree Engine
embedded in a master/detail context with a
Form Engine allows creating and linking in
one step already.
Validation &
Computation
Validations and
computations are possible
within the scope of a single
Document Model, i.e. with
Repeats in a Form Engine.
Validation or computation across
Relationships/links is not possible.
Data persistence Repeats are part of the form Instances of Relationships are links between
documents
Life cycle Repeats are a part of a Form
Model
Relationships can be modeled to link entities
in an application. Related Document Models
can have their own life cycle, their own
workflow, contain unlimited fields including
repeat data, have multiple Relationships and
exist in an application independently of the
Relationships that they are part of.
10

-- 10 of 39 --

Repeat Relationship
Workflows Repeats can be used in
Workflows
Workflows currently do not support
Relationships
Heterogeneity/Abstra
ctness
Repeats do not support
Heterogeneity
Abstract Supertypes are possible in
heterogeneous Relationships. Super/Sub-
Types can exist without Relationships, but
Relationships support Heterogeneity by
allowing Relationships to point to
Supertypes.
Orderability Data in Repeats can be
ordered
Links can be ordered when displayed in a
Tree Engine
Simple Composed Data
Composed Data is a feature which enables the usage of fields of several Document Models in the
same engine. Those Document Models need to be connected via Relationships.
The current implementation is called "Simple" because it provides a limited subset of Composed
Data functionality. It includes Composed Document Models, short CDM, and enables CDM based
forms in which all fields of that CDM can be displayed and edited, although they stem from
different Document Models and are persisted in different documents.
CAUTION Simple Composed Data is an experimental feature.
Simple Composed Data combines known Relationship UI elements (Bindings) like Table List, Dual
Pane Selection and Drop Down Selection with the possibility to display and edit fields from related
documents in one form. Moreover, CDMs can be enriched with rules and computations that can
make use of the whole CDM data context.
The starting point of CDM modeling is its root Document Model, which is determined by the
business use case.
Regarding the company example sketched above, one could formulate the following business
requirements:
1. When adding a person to a team, additional information is collected (Position, StartDate,
EndDate, TimeShare) and it is ensured that there is exactly one person with Position "Team
Lead" in the team.
2. On the team form, it is displayed how much WorkCapacity the team currently holds. This is
calculated as the sum of the contribution of all current members. The individual contribution is
the WeeklyWorkhours of each person times the TimeShare.
In these requirements, the team is the common bracket around all the linked persons and the
corresponding Additional Link Fields, hence, Team_DM would be the root Document Model of the
CDM. It is recommended to use a speaking name for the CDM, stating either the Document Models
involved or the purpose of the CDM (Model Naming Conventions).
11

-- 11 of 39 --

Only Document Models that are connected directly or indirectly via Relationships can be included
into a CDM. There should be a (chain of) Relationships between the root Document Model and the
included child Document Models.
NOTE
The example discussed in this documentation is also modeled in the advanced
workspace of the A12 installer. In People > Persons with Contracts and Teams and
Teams > Teams with Persons you can find modeled examples for complex
Computation and Validation Rules, Child Activities and Heterogeneity on root and
child level.
CAUTION
Please be aware, that the use of Relationship Models with Multiplicity
"unbounded" in combination with CDMs pose a security risk. The Multiplicity in
the Relationship Model should be lowered to a number reasonable in the
project’s context. It is not sufficient to just lower the repeatability of the
Relationship Element in the CDM.
CDMs should NOT be used with Relationship Models with any Role having
Multiplicity "unbounded".
Modeling a Composed Document Model
Example
To demonstrate all current features of CDMs, we add heterogeneity to the example above: persons
in the company can either be an employee or a freelancer. Moreover, a Contract_DM is added,
which holds a StartDate and EndDate as well as the WeeklyWorkhours.
Figure 7. Model Graph Diagram of the example use case
12

-- 12 of 39 --

The goal is to create a Composed Document Model with Team_DM as root, in which fields from
Person_DM and Contract_DM will be displayed and editable. It shall also contain fields of the Link
Documents and check the following business requirements:
1. When saving the form for a team, there is exactly one person with Position "Team Lead" in the
team.
2. When adding a new team member, it is displayed how much WorkCapacity the team currently
holds. This is calculated as the sum of the contribution of all current members. The individual
contribution is the WeeklyWorkhours of each person times the TimeShare.
Creation of the CDM
The SME contains a dedicated CDM Editor. See its documentation here.
▼ The manual creation with the Document Model Editor is described herein. Click to expand the
explanation.
To create the CDM structure manually, follow these steps:
1. Set the CDM Annotation
A CDM is created by adding a new Document Model and assigning the root Document Model
via the following annotation in the CDM’s model settings:
Name Value
cdm.queryRoot {root document model name}
Figure 8. Annotation in the CDM’s Document Model Settings
2. Include root Document Model
The root Document Model needs to be included on root level of the CDM. The name of the
include must exactly match the name of the root group in the included Document Model. In
the CDM context, this include has a special name: Document Model Element.
13

-- 13 of 39 --

Figure 9. Root Document Model included on root level in the CDM
3. Build the structure of the CDM
The Relationships, that connect the Document Models of interest, determine the structure of
the CDM. For each Relationship that is traversed, a subgroup is added. This nesting group
structure must match the "path" of the Relationship connection.
The Relationship that is represented by the group and the direction it is traversed, is
determined by the following annotations to the group. The group is called Relationship
Element.
Name Value
cdm.relationship {name of the Relationship that is represented
by the group}
cdm.targetDocumentModel {name of the Document Model that is to be
included}
cdm.targetRole {Role of the Document Model, that is to be
included, within the Relationship}
cdm.sourceRole {the other Role in the Relationship}
14

-- 14 of 39 --

Figure 10. Annotations for Relationship Elements
NOTE
The Document Model Editor automatically creates the required annotations
upon saving the CDM, if the cdm.queryRoot-annotation is set and the name of
the group matches the Relationship’s name as seen in the SME Workspace
Explorer. If a new nested Relationship Element is added after saving, the
annotations of the parent Relationship Element must be removed. The SME
does not traverse groups/Relationship Elements that have annotations
already.
The Relationship Element’s repeatability must match the multiplicity of the Target Role. If
"Unbounded" is selected in the Relationship, choose a reasonably high number. Otherwise,
enter the respective Upper Limit.
15

-- 15 of 39 --

Figure 11. Structure of the CDM and corresponding Relationships
4. Include the Child Document Model
Each Relationship Element should contain the respective Document Model as an include. The
include’s name must match the name of the root group of the included Document Model. In
the CDM context, this include has a special name: Document Model Element.
5. Include the Link Document Model
Add a new subgroup relationship to the Relationship Element and add an include of the Link
Document Model into this new group. Make sure that the name of the include is exactly the
name of the root group in the Link Document Model.
6. Result
16

-- 16 of 39 --

Figure 12. CDM structure as shown in the Document Model Editor to fulfill the example business
requirements
The structure of the CDM for the example above is shown in the following figure. It is based on
Team_DM as common bracket. The Relationship TeamPerson is followed to hold the links to the
selected team members (Person_DM). For each link, Additional Link Fields are stored and can be
accessed via the included TeamPerson_LinkFields_DM. For each person/team member, all linked
contracts are obtained, by following Relationship PersonContract.
17

-- 17 of 39 --

Figure 13. CDM structure as shown in the CDM Editor to fulfill the example business requirements
CAUTION
Once created, the annotations and the repeatability of the Relationship
Elements will not be refactored automatically. If the Relationship’s name or the
Roles or Multiplicities are changed, the annotations must be adapted manually.
NOTE
In the running application, the data of the CDM is handled with respect to the
Document Models of its parts (the root and child documents), so that the parts can
be persisted correctly on the server when saving "the CDM document".
Consequently, all Document Models of those parts must be present at runtime.
The loading of all the needed models is done automatically, if the CDM is build with
the editor using Includes.
But if a Document Model Element is not an Include, then the needed models must
be added to the respective VIEW_ADD directive in the App Model. If the Document
Model Element refers to a heterogeneous SuperType, ensure that all the SubTypes
are listed as well.
Additional fields in the CDM
The purpose of a CDM is the connection of data from different documents. Therefore, it is possible
to add any number of fields, validation rules and computations to the Composed Document Model.
CAUTION
Data changes in a CDM are ONLY persisted if the respective field is within the
root, a child or an Additional Link Fields Document Model. Please make it clear
to the end user, that the changes are done on the corresponding document; the
changes are not specific to the individual CDM context. If the result of a
computation rule should be persisted, the computed field must be within the
18

-- 18 of 39 --

root, a child or an Additional Link Fields Document Model.
Please be aware, that validation and computation rules, are NOT evaluated
when the respective documents are edited outside the CDM based form. So
make sure, that the end user can not change data relevant to the computation
rules outside a CDM with the respective rules. Alternatively, speak to a
developer to handle the CDM re-evaluation in the backend.
NOTE
If data is computed in the CDM context, but should be displayed in the Bindings
(referenced in the Binding Overview Models), the respective field must be modeled
in the corresponding child Document Model. It should be transient.
For example: The team member list should show the individual time contributions
of each member to the team’s WorkCapacity. The computation is modeled as child
of the Relationship Element TeamPerson. The computed field is
WorkCapacityContribution, a transient field modeled in Person_DM. The value of the
field is calculated when opening the CDM form or editing/adding links.
[cdm TableviewWithCalculatedValue] | cdm_TableviewWithCalculatedValue.png
Figure 14. List of team members with individual contribution to the team’s work capacity
How to Determine, Whether a Link Is Established
In order to determine, whether a link has been established and a child document is present in the
respective repetition, a technical field can be used. This field will only be filled when a link is
created and a child document is present. Add a string field with name t_docRef into the Relationship
Element as a sibling to the Document Model Element. As this is a technical field, it should not be
added to UI Models.
CAUTION
This is a Client feature only. Do not use it in Computation Rules for fields that
are executed on server side, e.g. to show on a CDD overview. A Rule
NumberOfTeamMembers = NumberOfFilledFields(/TeamPerson*/t_docRef) will always
show '0' in the CDD overview after (re)calculation on server side.
However, it can be used to ensure that computed data is not written into fields that are not
persisted (no document present). Moreover, fields of to-1 related documents should only be shown
to the user, if a link is established. (Otherwise the data entered is lost.)
NOTE
In order to hide fields, a Dependency must be set in the Form Model. For this, a
suitable Trigger field must be available in the CDM. Use the t_docRef field to
compute an additional transient boolean field which can then act as a trigger field.
Use "FieldFilled(t_docRef)" and "FieldNotFilled(t_docRef)" in the computation.
Heterogeneity on root level
Heterogeneity is supported on root level of CDMs. This means, that the CDM can be based on a
Document Model that is a Supertype. This does not mean, that a CDM itself can be heterogeneous.
19

-- 19 of 39 --

The concept and the modeling are similar to regular Document Models. It is recommended to read
the Heterogeneity BA documentation first.
Consider the following business requirement: For HR, an overview of all persons is needed, that
shows First Name and Last Name (Person_DM), the StartDate (Contract_DM) of the latest contract
and the number of current team assignments (using StartDate / EndDate) and the sum of the
currently assigned TimeShare (TeamPerson_LinkFields_DM) for each person. When clicking on an
entry, all fields of the respective Subtype (PersonEmployee_DM or PersonFreelancer_DM) should be
shown together with the contract information.
To achieve an overview of all persons (employees and freelancers combined), the Overview Model
must be based on the Supertype Person_DM. Since data of the linked contract document(s) should
be shown as well, a CDM is needed. Its root is Person_DM. There is only one Relationship between
the Supertype Person_DM and Contract_DM. There must not be a Relationship between
PersonEmployee_DM and Contract_DM / PersonFreelancer_DM and Contract_DM!
In order to show all fields of the Subtypes, two distinct forms are needed. If contract data should be
shown as well, a CDM is needed. These CDMs have the Subtypes as root Document Model:
• PersonEmployeeWithTeamsAndContracts_CDM is based on PersonEmployee_DM; a form based
on this CDM is opened when an employee is selected in the overview
• PersonFreelancerWithTeamsAndContracts_CDM is based on PersonFreelancer_DM; a form
based on this CDM is opened when a freelancer is selected in the overview
The computations to determine the latest contract’s StartDate and the number and sum of
TimeShare of the current team assignments must be modeled in all three CDMs and maintained
manually.
Heterogeneity on child level
The linked child documents can be heterogeneous. As there is currently no way to restrict the
selection to or filter for specific subtypes, the CDM Document Model Elements must always include
the respective Supertype.
For example in TeamWithPersonsAndContracts_CDM, the Document Model Element, that is placed
within Relationship Element TeamPerson, should always include Person_DM and not
PersonEmployee_DM or PersonFreelancer_DM.
As a consequence, only the fields of the Supertype can be used in CDM computations or validation
rules. The respective DetailScreen is also modeled only on the Supertype. Use Child Activity to
model a different form for each Subtype.
Modeling a Form Model for a CDM
Fields of the root Document Model
The fields of the root Document Model can be treated in the Form Model Editor of the SME as any
other field in a "normal" Document Model.
20

-- 20 of 39 --

Linking and Modifying child documents
The link to child documents is established in UI elements called Binding. For general information
about Bindings see the Binding Model documentation.
CAUTION
CDM-Bindings can only be created in the Form Model Editor by dragging
dropping a Relationship Model Element onto a Screen or Section. It can not be
created from the context menu.
Depending on the multiplicity of the Target Role (the possible number of child documents), the
modeling slightly differs:
To-1 Binding Component
If the multiplicity of the Target Role is 1, only one document can be linked. This link should be
maintained in the Binding component Drop Down Selection.
NOTE
The modeler must ensure that all links have a display text. In the Drop Down
Selection a placeholer "NO LABEL" is shown to assist with debugging and to indicate
a modeling issue.
There are two options to model input fields for the linked document:
• Only allow selection and de-selection of an existing document via the dropdown.
All fields of the child Document Model can be used in the Form Model as usual. Make a clear
distinction for the user, which fields belong to which data context/document.
CAUTION
The fields should not be available for input, if no document is selected/no
link is established. In order to hide those fields, use the Dependent Control
feature together with a trigger field based on t_docRef.
• Enable Add and Edit by ticking the "Use CDM child activities" checkbox in the Binding Editor.
This will activate the CDM Child Activity feature that is explained in the following section. The
user is then presented with a new form, in which the created/linked document can be edited.
CAUTION
When launching the child activity, the original CDM context is left. While the
new form shown to the user can again be based on a CDM, the rules and
computations of the parent CDM will NOT be applied in the child activity.
They will be applied, when the user leaves the child activity and returns into
the parent activity.
To-many Binding Component
If the multiplicity of the Target Role is > 1, many documents can be linked to the root/parent
document. These links should be maintained in a Dual Pane Selection. But to have a better overview
of existing links and to enable the user to see and edit the data of the child documents, a Table List
must be used. Both UI components can be combined.
21

-- 21 of 39 --

In Table Lists it is possible to create and link a new child document in one step. The respective
button is shown to the user when "Repeat Entries > Add" is checked in the Binding Editor.
CAUTION
Additional Link Fields can currently not be entered in a modal right away when
linking a document. Hence, there is no selection for an Additional Link Fields
Form Model in the CDM Binding Editor.
There are two options to model input fields for the linked documents. The respective action is
triggered if the user clicks on a row in the Table List.
• DetailScreen of the Binding
When adding a Binding in the Form Model Editor via Drag&Drop, a DetailScreen is
automatically created. All fields that are modeled within the corresponding Relationship
Element can be used on that screen. This includes Additional Link Fields and fields defined in
the CDM. Make sure the user can easily distinguish which fields belong into which data context.
NOTE
The "bread crumbs" of the DetailScreen are determined by the label set in the
Binding Editor. The label above the Relationship UI element is determined by
the label set for the Relationship Target Role.
It is also possible to nest another Binding onto that screen and traverse the next Relationship
included in the CDM.
• CDM Child Activity
Tick the "Use CDM child activities" checkbox in the Binding Editor. In this case, the DetailScreen
is ignored and a Child Activity is launched. The modeling of a Child Activity is explained below.
This option will display the linked document in a dependant activity within its own form. This
allows to edit fields defined in heterogeneous Subtypes, as there can be one form for each
Subtype.
NOTE
To edit the Additional Link Fields, create a CDM for the child Document Model
and include the Relationship and the Additional Link Fields Document Model
there.
Example:
If the end user opens a form for Team_CDM and clicks on a row in the Table List
for TeamPerson, a new activity is launched and the user is presented with a
form for the selected person. This second form can be based on a CDM (e.g.
TeamMember_CDM) that includes the Relationship TeamPerson. During
runtime, this Relationship Element of TeamMember_CDM will always be
populated with only one link: the one to the root document of the CDM of the
parent activity. In this example, the team edited in the original CDM form. Other
teams, this person is linked to, will not be shown/available!
Due to this feature, the repeatability of the Relationship Element TeamPerson in
TeamMember_CDM can be set to 1 in the Details Editor of the CDM Editor,
regardless of the Multiplicity set in the TeamPerson Relationship. This makes the
22

-- 22 of 39 --

modeling of the respective form straightforward.
But take care to not use TeamMember_CDM in any other context. (If it was used
together with an overview on Person_DM in a Master/Detail Module, and any
person be selected, the team context would not be present. The application
would fetch all links and try to list all teams, the person is linked to. This would
lead to an error.)
CAUTION
When launching the Child Activity, the original CDM context is left. While
the form shown to the user can again be based on a CDM, the rules and
computations of the parent CDM will NOT be applied in the child activity.
They will be applied, when the user leaves the child activity and returns into
the parent activity.
CDM Child Activities
If "Use CDM Child Activity" is activated in the Binding configuration, the linked document will be
created or edited in a dependent activity with an own Form and Document Model. That means, a
separate form will be opened next to the CDM form, in which the created/linked child document
can be edited. An additional Scene in the App Model is required. It must match the state of this new,
separate activity as described in the following section.
In case of a Drop Down Selection, the "Use CDM Child Activity" is activated by default to allow
adding new entities. The Add / Edit button label can be configured. (If no link is established yet, the
Add button is shown, otherwise the Edit button is shown, but never both.)
In case of a Table List, an Add button below the table can be configured. (Edit is triggered by
clicking on the respective row.)
If the user clicks any of these buttons, the new activity will be created and the respective UI for that
activity will be shown next to the original CDM form.
In case the CDM child is a Supertype, the Variant Dialogue is shown in order to determine the
concrete type of the to be created document.
This new activity can again be based on a CDM, which essentially allows nesting of CDM activities
of arbitrary depth. Furthermore, different Document Models or CDMs can be used for the Child
Activity in case of Heterogeneity. This would then require different Form Models for each of the
relevant Subtypes.
The Client provides a default data handling between the parent and Child Activity. This must be
enabled by setting the scene matching to derive activity descriptor from parent scene in the
Binding editor. Otherwise, the handling of the resulting data of the Child Activity has to be provided
by custom implementation.
If derive activity descriptor from parent scene is chosen, the scene for the Child Activity can
reuse some of the match conditions of the parent scene. Only model and instance will be adapted
according to the Document Model and id of the document.
23

-- 23 of 39 --

CAUTION
To ensure that data, that might have been changed in the parent activity, is also
present in the Child Activity (and vice versa), the Child Activity works on a
portion of the exact same data as the parent activity. That means, that if derive
activity descriptor from parent scene is selected, no new data is loaded for the
Child Activity. This applies also to links and linked documents.
Moreover, Bindings are only supported, if the Document Model that is the basis
for the Child Activity is also a CDM. If no Bindings are needed, the Child Activity
can use a regular Document and Form Model.
Consequently, only data that fits into the data structure of the CDM in the parent
scene will be saved.
NOTE
The feature described above can be used to hand down data, that was computed in
the parent activity. For example in the team-person-contract context, the current
WeeklyWorkhours of each team member would be computed in the parent
TeamWithPersonsAndContracts_CDM. This value can then also be used in the Child
Activity, even if there is no respective computation in TeamMember_CDM. Neither
must it include the PersonContract Relationship.
Use this, to keep copies of complex computations low, but make sure that the values
are used only as read-only.
Initial Values and Rows
It is possible to define initial values and rows in Form Models based on Composed Document
Models as with usual Form Models.
But there is one exception, where initial values cannot be set:
• In the chain of Relationships that connects the root Document Model to the Document Model
that contains the field in question, the Target Roles must not contain Heterogeneity.
When providing initial values for fields of to-1 related Document Models, this leads to the automatic
creation of new sub documents and links when creating a new document or when entering a
respective detail screen. It is not possible to create new links to existing documents via initial
values.
Dependencies using 'Not Relevant'
Field and Group Dependencies with Not Relevant are supported in CDM based Form Models and
CDM Child Activities. It is also possible to remove links on save, when a Relationship Model Element
was marked as Not Relevant by a dependency.
However, at the moment there is the limitation that the relationship UI components (e.g. Table List)
do not hide entities that are marked as Not Relevant. In such case, the whole Binding should be
hidden by using the Dependent Control setting in the Form Model.
24

-- 24 of 39 --

Controls with Index
Using Controls with index is currently not supported in Form Models based on Composed
Document Models.
Modeling an Overview Model for a CDM
CDMs can be used as basis for Overview Models. Any Field defined within the CDM can be used.
Regarding Computed Fields, please see the section below.
Most aspects of Overview Modeling with CDMs are exactly the same as "normal" Overview
Modeling. Refer to the Overview Model documentation for further details.
There is a difference when it comes to optimizing the loading performance of overviews: In case of
the "normal" overview, per default, only the data that is needed for the overview is transmitted to
the client. No extra modeling is required. This optimization is not done for the CDM overview.
In order to improve the loading performance of CDM overviews, modelers can do the following:
1. Create a dedicated CDM for the overview
The CDM that is used for the overview and the CDM that is used for the corresponding Form
Scene in a Master/Detail setting can be different. Just reference them correctly in the Overview
and Form Model, no extra App Model settings are needed. It is possible to use a Master Detail
Module Model.
Reduce the amount of data that is loaded by only adding those Relationship Elements to the
CDM that are needed for the overview.
2. Set the Annotation indexed = false in the Document Model on those Fields that are not shown in
an overview
Set the Annotation indexed = false on Fields that are not shown in an overview (as reference
column or in an Expression). The Annotation must be set in the respective root or child
Document Model, not in the CDM.
Be careful with Fields used in Computations, please see the section below.
Computed Fields in CDM Overviews
For "normal" Document Models, Computations are only executed
1. on saving a document
2. on loading a document in a form, if specified in the Form Model.
Computations are not automatically executed for the overview.
In contrast, it is possible to automatically execute all Computations of the CDM again when opening
the overview. A respective Data Services configuration flag must be set to activate this feature.
25

-- 25 of 39 --

NOTE This setting has been made in the Preview App for all models.
CAUTION
All Computations seen in the CDM are executed, not just those defined in the
CDM. Use the Exclude Computation Rules setting in the Document Model Element
Editor of the CDM to exclude the Computation Rules of the root or child
Document Models.
The Computations are executed at the time when Data Services composes the result that is sent to
the client and shown in the overview. This happens after the respective documents are gathered
according to the Search term or Filters applied in the overview.
In order to prevent inconsistencies, take the following steps:
1. Remove the possibility for the enduser to Search or Filter on Fields that are computed by
Computations defined in the CDM.
If a Field of the root or a child document is computed by a CDM Computation and not shown in
an overview or tree, then
a. set the Annotation indexed = false on the Field in its Document Model to exclude it from the
Search.
b. Remove those computed Fields from the Filter list of the Overview Model for the CDM.
CAUTION
If the enduser would filter on a computed Field of the CDM, they would
never find an item, as this Field is always still empty when the Filter is
evaluated.
If the enduser would filter on a Field of the root or child document, that
is computed by a Computation of the CDM, the Filter would work on the
value that was present when the document was saved. If the
Computation then changes the result afterwards, this could lead to a
difference between the value shown in the overview and the value
defined in the Filter. It would seem as if the Filter would not work
correctly.
2. Columns that reference computed Fields should not be marked as sortable.
3. None of the Fields that are used in a Computation, which result is shown in the overview,
should have Annotation indexed = false.
This applies for all Fields referenced in the Preconditions and Calculations.
CAUTION
Fields with indexed = false appear empty to Computations, so the results
shown in the overview and in the form (which has all data present) might
differ without any error or warning.
There is modeling support that reports an error if a Field with indexed =
false is used in an overview, but no support regarding the use in
Computations.
26

-- 26 of 39 --

TIP
You can set the Annotation indexed = false on Fields in the CDM. This has no
consequence on the performance, but you can benefit from subsequent modeling
support and prevent "accidental" usage of a computed Field in an overview.
CDMs and Regular Overviews or Trees
CDM based overviews and forms can be combined seamlessly with regular tree, overviews or
forms. It is possible to show a regular form for an entry of a CDM based overview in a Master/Detail
layout or to show a CDM based form for an entry in a tree.
CDMs and Master Detail Module Model
CDM based Overview Models can be selected in the Master Detail Module Editor.
Moreover, CDM based forms can be used in the Detail activity. The Client will detect, if a form is
based on a CDM and will activate the special CDM data handling automatically.
CAUTION The current implementation of the Master Detail Module Model does not
support CDM Child Activities. It can only be used with DetailScreens.
Setup in the App Model
To use CDMs in an application, certain settings have to be made by a developer. If this is covered, it
is possible for a modeler to add a CDM to the application. CDMs can be used in the Installer Preview
App right away.
CAUTION
Scene Matching for CDMs is always done on the respective root Document
Model. For example use model : Team_DM instead of
"TeamWithPersonsAndContracts_CDM". There should never be a Match
Condition "model":"[Name of Composed Document Model]".
Setup of Master Detail Module With Heterogeneous Child Activity
The following section describes, how a Master Detail Module is created in the App Model Editor of
the SME. It uses the following Document Models (see example above):
• TeamWithPersonsAndContracts_CDM (root Document Model: Team_DM)
• TeamMemberWithContracts_CDM (root Document Model: Person_DM)
• Contract_DM
with the respective UI Models:
• TeamWithPersonsAndContracts_COM (Overview Model) for
TeamWithPersonsAndContracts_CDM
• TeamWithPersonsAndContracts_CFM (Form Model) for TeamWithPersonsAndContracts_CDM
27

-- 27 of 39 --

• TeamMemberWithContracts_CFM (Form Model) for TeamMemberWithContracts_CDM
• Contract_FM (Form Model) for Contract_DM
There should be two Bindings with a TableList and "Use CDM Child Activity" set up. The first is
modeled on TeamWithPersonsAndContracts_CFM showing the links of Relationship TeamPerson to
documents of Person_DM. The second is modeled on TeamMemberWithContracts_CFM showing the
links of Relationship PersonContract to documents of Contract_DM.
The ViewProvider/UI Component names used here are valid for the Preview App and Project
Template.
1. Add new Module to App Model
Open the App Model Editor in the SME and add a new entry to the Modules list. You may give
any attributes to the Activity Descriptor.
Figure 15. New Module for CDM
2. Add new Flow to the Module
Add a new Flow to the list of Flows of the Module. Give a speaking name.
The following picture shows the Scenes that will be created in the next steps:
28

-- 28 of 39 --

Figure 16. New Flow of the CDM Module
3. Add Scene 1: Overview
Add a new Scene to the list of Scenes of the Flow and give it a speaking name. You may add a
description.
It is important to set the Match Conditions according to the Activity Descriptor of the Menu
Entry (see step 1). Add the Match Condition instance isSet false, to activate the overview. In
the next step, we add the Scene Change directives as seen in the picture:
29

-- 29 of 39 --

Figure 17. Attributes of the CDM overview Scene
Add the following two On Enter directives:
Figure 18. Attributes of the Region_Clear directive
30

-- 30 of 39 --

Figure 19. Attributes of the View_Add directive
4. Add Scene 2: Detail Form Scene
Add a new Scene to the list of Scenes of the Flow and give it a speaking name. You may add a
description. It is important to set the Match Conditions according to the Activity Descriptor of
the Menu Entry (see step 1). Add the Match Condition instance isSet true, to activate this Scene
only when a document is selected in the overview.
Add the Match Condition model and enter the name of the respective root Document Model. So
if you want to display TeamWithPersonsAndContracts_CFM (Form Model) for
TeamWithPersonsAndContracts_CDM, you enter "Team_DM" here, as it is the root Document
Model of TeamWithPersonsAndContracts_CDM.
Do not add a Region_Clear Directive, but only a View_Add Directive to the On Enter list. This
time, enter FormEngine as the UI Component name and add the respective Form Model
(TeamWithPersonsAndContracts_CFM) to the Models list.
31

-- 31 of 39 --

Figure 20. Attributes of the Detail CDM Form Scene
32

-- 32 of 39 --

Figure 21. Attributes of the View_Add directive of the Form Scene
5. Add Scene 3: First Child Activity Scene
Add a new Scene to the list of Scenes of the Flow and give it a speaking name. You may add a
description. It is important to set the Match Conditions according to the Activity Descriptor of
the Menu Entry (see step 1). Add the Match Condition instance isSet true, to activate this Scene
only when a document is selected.
Usually, one would set the model Match Condition here. If so, do not match for the CDM but for
the respective root Document Model. Since the same form should be shown for both Subtypes
(PersonFreelancer_DM and PersonEmployee_DM), do not set the model Match Condition. This
scene would now open for any document. In order to bind it to the Parent Scene (the one that
launched the Child Activity), set Prior Scene to be the name of the Detail Form Scene (see above).
Do not add a Region_Clear Directive, but only a View_Add Directive to the On Enter list. Again,
enter FormEngine as the UI Component name and add the respective Form Model
(TeamMemberWithContracts_CFM) to the Models list.
33

-- 33 of 39 --

Figure 22. Attributes of the First Child Activity Scene
34

-- 34 of 39 --

Figure 23. Attributes of the View_Add directive of the First Child Activity Scene
6. Add Scene 4: Second Child Activity Scene
Add a new Scene to the list of Scenes of the Flow and give it a speaking name. You may add a
description.
It is important to set the Match Conditions according to the Activity Descriptor of the Menu
Entry (see step 1). Add the Match Condition instance isSet true, to activate this Scene only,
when a document is selected.
You can limit the activation of this Scene by either setting Prior Scene to the name of the First
Child Activity Scene (see step before) or by setting the model Match Condition. Let’s decide for
the latter here and add model mustEqual Contract_DM. This Scene is now only shown, when an
instance of Contract_DM is to be displayed.
Do not add a Region_Clear Directive, but only a View_Add Directive to the On Enter list. Again,
enter FormEngine as the UI Component name and add the respective Form Model (Contract_FM)
to the Models list.
35

-- 35 of 39 --

Figure 24. Attributes of the Second Child Activity Scene
36

-- 36 of 39 --

Figure 25. Attributes of the View_Add directive of the Second Child Activity Scene
7. Save, deploy, try
Apply all changes and save the App Model. Deploy the workspace to a running application and
refresh the browser window.
Glossary
Term Description
1:n relationship A 1:n relationship is a relationship in which one role has the and upper
limit of 1 as multiplicity, while the other role has a multiplicity >1 or an
unbounded multiplicity
n:n relationship A n:n relationship is a relationship in which each role has a multiplicity
>1 or an unbounded multiplicity
Binding Element to configure relationship ui components to be displayed within a
form. Can be defined within a form model. Holds the configuration of a ui
component to maintain links to a target role and/or a table list to display
links to a target role
37

-- 37 of 39 --

Term Description
Candidate Refers to the set of instances to which a link could be created. As of now,
it is "all" the instances of the other side to which a link does not yet exist.
Drop Down Selection A relationship ui component, the best suitable if the target role has a
multiplicity with an upper limit of 1
Dual Pane Selection A relationship ui component, the best suitable if the target role has
multiplicity with an upper limit ≥ 2 or unbounded
Generated Document
Model
Relationship specific document models that need to be generated using
the Relationship Model Editor. They are used in the overview models
which can be configured in bindings for the Selected Items Overview.
Link An instance of a relationship model; specifies the connection between an
instance of one role of the relationship and an instance of the other role
of the relationship
Link Constraints Properties of a relationship role; currently limited to Multiplicity
Link Order The order in which links are displayed in the tree engine
Multiplicity Specifies the maximum number of instances of this role that can be
linked to one instance of the other role; can be a number (upper limit) or
unbounded
Relationship Model Determines which document models are linked, a long with some other
parameters
Role A relationship establishes a link between two roles. A role is taken up by
a document model. An instance of a role is an instance of the respective
document model in the context of the respective relationship.
Self-Referencing
Relationship
Both roles are taken up by the same document model, thus making it
possible to create links between instances of one document model
Table List UI element in which links are displayed; read-only
Target role Links to this role can be edited and viewed in a relationship ui
component.
Unbounded The multiplicity is unbounded if the maximum number of instances of
this role that can be linked to one instance of the other role is unlimited
Upper Limit Specified the maximum number of instances of this role that can be
linked to one instance of the other role
User Determined
Order
When user determined order is selected, links that are displayed in a tree
engine can be reordered manually in the UI
Relationship UI
Component
UI element in which relationship links can be edited and/or displayed:
Drop Down Selection, Dual Pane Selection and Table List
Relationship Model in A12 model landscape
38

-- 38 of 39 --

Area Type Meta Model M2 Model M1 Instance M0
Data Document Document Meta Model
Describes how
Document Models look
like, i.e. speaks of
elements, groups, fields
and rules
Document Model
Describes how a
concrete business
document looks like,
i.e. describes the
structure and certain
behavior of a concrete
business domain.
Examples: Team,
Employee
Document Represents
a concrete business
data instance.
Examples: Team T1,
Team T2, Employee E1,
Employee E2, Employee
E3
Data Relationship Relationship Meta
Model Describes what
relationships are,
i.e. speaks of source
and target, cardinality,
direction, etc.
Relationship (Model)
Describes a concrete
relationship between
concrete entities
(represented as
documents). Examples:
Team ↔ Employee
(Relationship) Link
Represents a concrete
relationship
link/connection
between two concrete
entity instances
(documents). Examples:
Team T1 ↔ Employee E1
; Team T2 ↔ Employees
E2, E3
UI Application Application Meta
Model
Application Model –
UI Overview Overview Meta Model Overview Model –
UI Form Form Meta Model Form Model –
39

-- 39 of 39 --

