# sme sme qm ba docs

Query Modeling
CAUTION
The Query Model and the editor are an experimental feature. It must be
enabled in the SME Tool Settings before use; see Enabling Experimental Module
Types.
1. Introduction and Concepts
The Query Model Editor is part of the Simple Model Editor (SME) and enables domain experts,
analysts and developers to create and modify Query Models for business applications.
The Query Model defines the data set that shall be used for a given business requirement:
1. Which Documents should be included in the data set? (Filtering)
2. Should all or only certain Fields of these Documents be included?
Table of Contents
1. Introduction and Concepts. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 1
1.1. Example Use Case . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
1.2. Filter Definition. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
2. Installation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
2.1. Migrating Existing Models to a New Version of the Tools. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
3. Editor Functions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
3.1. Opening the Query Model Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
3.2. Query Model Contents . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
3.2.1. Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
3.2.2. Model Tree . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
Add an Element . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
Element Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
Filtering the Model Tree . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
Actions in Model Tree . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
Expand and Collapse . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
Delete All Elements. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
Add Target Document Model Element . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
3.2.3. Post Processing . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
3.2.4. Element Picker. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
3.3. Validation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
3.4. Refactoring Across Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
4. Document Model Element Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
5. Glossary . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 21
1

-- 1 of 22 --

3. Should the Documents be sorted? (Sorting)
4. Which portion of the Documents should be retrieved? (Paging)
5. Should also Linked Documents be loaded? (Projection)
Query Models can be referenced in Overview Models to define the Data Set that is accessible by the
end user. (See Overview Modeling > Base Query.) The Query Model and the resulting data set can
also be used in custom implementations. In this way, a modeler can define the business
requirements for the data set and hand them over to a developer in form of a Query Model.
An example of Query Model usage can be seen in the Advanced Workspace that is available with
the A12 Installer.
1.1. Example Use Case
In an HR Management System, a business requirement is to show a dedicated list of all fulltime
employees (we consider 35 weekly work hours or more as fulltime). Whether a person works
fulltime or not, can be determined by the contract that is linked to them.
Taking the advanced example workspace as a basis, the formalized business requirement would be:
A list that shows the FirstName and LastName of all Person documents that have a Link of the
PersonContract Relationship to a Contract document, on which Field WeeklyWorkhours has a value
greater or equal to 35.
Figure 1. Query Model Yielding Persons Having Contracts With WeeklyWorkhours >= 35
With a custom implementation, we would like to add a symbol in each list row that represents a
person that is a team lead. In order to implement this requirement, the query must additionally
yield all teams, where the person’s position is specified to be a team lead.
Taking the advanced example workspace as a basis, the formalized business requirement would be:
2

-- 2 of 22 --

A list that shows the FirstName and LastName of all Person documents that have a Link of the
PersonContract Relationship to a Contract document, on which Field WeeklyWorkhours has a value
greater or equal to 35. For each of them, additionally load TeamName of all Team documents that
are linked via Relationship TeamPerson, where the Link Field Position exactly equals "Team Lead".
Figure 2. Query Model Yielding Persons Having Contracts With WeeklyWorkhours >= 35 and Their Linked
Teams Where Position Is Team Lead
This Query would yield two lists of entities:
1. All the Person documents, that fulfill the Filter Definition: Has("PersonContract", "Contract",
[/Contract/WeeklyWorkhours] >= 35)
2. For each of those selected documents, all linked Team documents and the respective Link Fields
Document where the Link Fields document fulfills the Filter Definition: [/LinkFields/Position] ==
"Team Lead"
In a custom implementation, a frontend developer can pick up these results and show a respective
icon, if at least one Link for the document shown in the row is present. When hovering above the
icon, a list with all the team names can be shown.
The business requirement, when the symbol is shown, is modeled. If the business requirement
would change to also take "Team Co-Lead" into account, then this can be changed in the Query
Model. No code change would be needed.
The first list represents the Target Document Model of the Query Model. It is always the first entry
of the Model Tree of the Query Model Editor. In A12 Data Services, this list is filled during the
Selection phase of the query execution.
The second list represents the links section of the Query Model. It is determined by all the other
elements of the Model Tree of the Query Model Editor. In A12 Data Services, this list is filled during
3

-- 3 of 22 --

the Projection phase of the query execution.
▼ The corresponding Query Model content and its query result are shown herein. Click to expand.
4

-- 4 of 22 --

5

-- 5 of 22 --

Query Data Services Result
{
"header": {
"id":
"PersonsWithFulltimeContract_QeM",
"modelType": "query",
"modelVersion": "1.0.0",
"locales": [
{
"code": "en"
},
{
"code": "de"
}
],
"annotations": [],
"modelReferences": []
},
"content": {
"projectionName": "document",
"paging": {
"pageSize": 50,
"pageNumber": 0
},
"targetDocumentModel": "Person_DM",
"constraint": {
"operator": "has",
"relationshipModel":
"PersonContract",
"targetRole": "Contract",
"constraint": {
"operator": "double_range",
"field":
"/Contract/WeeklyWorkhours",
"from": 35
}
},
"fields": [
"/Person/FirstName",
"/Person/LastName"
],
"links": [
{
"relationshipModel":
"TeamPerson",
"targetRole": "Team",
"linkDocumentFields": [],
"linkDocumentConstraint": {
"operator": "exact_match",
"field":
"/LinkFields/Position",
{
"jsonrpc": "2.0",
"id": "Example_Query",
"result": {
"fullSize": 2,
"page": {
"pageNumber": 0,
"pageSize": 50
},
"entries": [
{
"docRef":
"PersonEmployee_DM/6_Joshua",
"document": {
"Person": {
"LastName": "Weimann",
"FirstName": "Joshua"
}
},
"type": "ROOT",
"depth": 0,
"documentModelName":
"PersonEmployee_DM"
},
{
"docRef":
"PersonEmployee_DM/2_Viola",
"document": {
"Person": {
"LastName": "Ward",
"FirstName": "Viola"
}
},
"type": "ROOT",
"depth": 0,
"documentModelName":
"PersonEmployee_DM"
}
],
"links": [
{
"docRef":
"Team_DM/1_Management-Board",
"relationshipModel":
"TeamPerson",
"sourceRole": "Person",
"sourceDocRef":
"PersonEmployee_DM/2_Viola",
"targetRole": "Team",
"targetDocRef":
6

-- 6 of 22 --

1.2. Filter Definition
Filter Definitions are the central modeling feature of Query Models. They define which documents
and links will be added to the Query Result Set.
They are shown in the Model Tree and can be edited in the Document Model Element Editor after
clicking on a Document Model Element.
The following language constructs are available:
TIP Use [Ctrl]+[Space] within the Filter Definition editor to show suggestions.
To get more information about the language constructs and to see usage examples, please enter the
term in the Filter Definition editor and hover above it. An info box with more information will be
shown.
Operator Description
[/path/to/field] The Field Value Operator can be used to reference a Field of the current
Document Model. The current Document Model is shown in the right
bottom corner of the editor. In the first place this is the Document Model
of the Document Model Element. When using the Has-Operator this could
also be the Link Fields Document Model or the linked Document Model.
Fields that have the Annotation Indexed = False cannot be used in Filter
Definitions.
Not, And, Or Logical Operators to concatenate Filter Statements logically. Round
brackets must be used together with Not or if And and Or are used
together.
==, ~, !=, !~, >= , <= Unitary Value Comparison Operators compare a Field specified by a Field
Value Operator with a constant value. The constant value must match the
data type of the Field. This is ensured by modeling support in the editor.
It can be specified, whether the value of the Field must exactly match the
constant value (==) or a case-insensitive substring search (~) should be
done.
By default, at least three characters must be given for the ~ and !~
Operator; this can be changed with Data Service configuration.
Null can be used together with == and !=
Date, Time, DateTime,
DateFragment,
DateRange
Language constructs to create date and time constants for comparison by
unitary value comparison operators or InRange operator.
7

-- 7 of 22 --

Match One or many Field Value Operators can be given together with at least
one Constant Values to the Match Operator. It is fulfilled, if any of the
given constant values is a case-insensitive sub-string of the data in any of
the given Fields. If no Field is given, all Fields of the Document Model
Element, that do not have Annotation indexed = false, are taken into
account.
InRange The InRange Operator takes a Field Value Operator and two constant
values.
InRange([/Group/Field],3,9)
is logically equal to
[/Group/Field] >= 3 And [/Group/Field] <= 9
but the InRange construct is much faster.
Has The Has Operator expects the following arguments:
1. Relationship Model: Name of a Relationship Model in which the
current Document Model is referenced in any Role.
2. Role: Name of the other Role in the Relationship Model. In case of a
self-referencing Relationship this is needed; in all other cases it is
explicitly specified to mitigate modeling mistakes.
3. Filter Definition for the linked Document Model:
It can be specified that the linked document must fulfill the given
Filter Definition. The Filter Definition editor will switch the current
Document Model and suggest Field references according to the
Document Model of the respective Role.
Can be Null, if no Filter Definition should be applied.
Must be set, if a Filter Definition for the Link Fields Document Model
shall be added.
4. Filter Definition for the Link Fields Document Model: It can be
specified that the Link Fields document of the link must fulfill the
given Filter Definition. The Filter Definition editor will switch the
current Document Model and suggest Field references according to
the Link Fields Document Model of the Relationship Model.
Can be Null, if no Filter Definition should be applied.
CAUTION The Has operator is fulfilled, if any link exists, where
both Filter Definitions are met for the same link.
8

-- 8 of 22 --

Null • Placeholder in the Has Operator, if no Filter Definition for the
respective Document Model is needed.
• Representation of an empty database Field when used with == or != .
TIP
[/Group/Field] != Null
matches all documents, where Field /Group/Field has any
value. Documents where Field /Group/Field is empty are not
included in the Result.
There is Data Service configuration to specify if an empty
Field of Type Number is matched by 0 (as it would be
treated by Kernel in a Computation) or by Null.
2. Installation
The Query Model Editor is part of the Simple Model Editor. For advice on the installation of the
Simple Model Editor, refer to the "Quick Start Guide" on getA12 under "Modeling".
2.1. Migrating Existing Models to a New Version of the
Tools
It might be necessary to migrate existing Query Models in case a new version of the Simple Model
Editor and thus Query Model Editor has been installed.
In most cases, the Simple Model Editor can handle updating versions itself. For more information,
refer to the documentation of the Simple Model Editor.
3. Editor Functions
3.1. Opening the Query Model Editor
To access the Query Model Editor, open a Workspace in the Simple Model Editor (SME). All models
contained in the Workspace are displayed in the SME Workspace Explorer. Query Models are
recognized by the SME and are indicated with the "Qe" icon
9

-- 9 of 22 --

Figure 3. SME Workspace Explorer Containing a Query Model
10

-- 10 of 22 --

To open the Query Model Editor for an existing model, click the entry in the Workspace Explorer.
To create a new Query Model, use the "ADD" button in the header of the Workspace Explorer or the
context menu of a folder, and select "Query Model".
Figure 4. Add a New Query Model
11

-- 11 of 22 --

A modal will then be displayed to define the mandatory model settings: Folder, Name and Target
Document Model.
Figure 5. Enter Initial Model Settings for New Query Model
The Name of the model must be unique within the Workspace.
3.2. Query Model Contents
3.2.1. Settings
The Settings Screen contains the following elements:
Name
The name of the Query Model. It must use only letters, digits, hyphens, underscores, and periods.
The name must not start with "xml" and must be at most 100 characters long.
The model name must be unique within the Workspace and is synchronized with the filename
by the editor.
Model Version
Shows the version of the opened model.
Description
Multiline text providing additional information about the query specified by the Query Model.
12

-- 12 of 22 --

Locales
A list of locales supported by the model. Each locale is represented by a row in the table.
At least one locale must be entered. Only locale codes according to ISO 639 alpha-2 or alpha-3 are
allowed. A region code may be added after an underscore, such as de_DE or de_CH.
NOTE Locales are currently only used to specify Query Model Labels.
Labels
These fields store a list of labels for the model, one for each specified locale.
The labels can be used as a localizable representation of the model itself in a list of different
models. E.g. in a model repository.
Roles
A list of Roles can be maintained in this table.
Annotations
An Annotation is a name-value pair that can be added to the model. Applications using the
Query Model can access these Annotations and use them in custom implementations.
3.2.2. Model Tree
The Model Tree Screen can be seen in Figure 6.
Figure 6. Query Model Editor - Model Tree Screen With Model Tree, Document Model Element Editor, and
Element Picker
The Model Tree represents the quality and quantity of the Result Set the query will yield. It consists
of Document Model Elements, Relationship Model Elements and the content of the respective
13

-- 13 of 22 --

Document Models (Groups, Fields, …).
Name
The hierarchical column of the Model Tree shows the nested structure of the Query Model and
lists
Target Document Model Element
The first Document Model Element of the Model Tree represents the Target Document Model.
It has some special properties that can be changed in the editor for the Document Model
Element.
Relationship Model Element
Starting from the Target Document Model Element, any number of Relationships can be
followed and the respective linked documents are added to the Result Set. The Relationship
Model Element represents a followed Relationship. They can be nested inside other
Relationship Model Elements.
The Document Model, that is referenced in the other Role of the Relationship Model, is shown
nested under the Relationship Model Element. If a Link Fields Document Model is specified in
the Relationship Model, then this will be present as well.
Document Model Element
Document Model Elements represent the Document Models for which data will be retrieved.
For each of those, a Filter Definition and a list of Fields can be maintained in the editor.
Only for the Document Model Elements a Detail Editor is opened.
In Result
The checkboxes in the rows of the Model Tree that represent Fields indicate, whether the data of
this Field will be present in the query’s Result Set or not. If a Field has the Annotation indexed =
false in the Document Model, then the checkbox will be disabled. Fields with this Annotation
can only be added to the Result Set, if All Fields of Document Model is selected in the Document
Model Element Editor of the respective Document Model Element. The value of the checkbox
corresponds to the section "Fields included in Result Set" of the Document Model Element Editor.
CAUTION
If All Fields of Document Model was selected for a Document Model Element
and any checkbox for a Field nested within this Document Model Element is
deselected, then all checkboxes for Fields, that have Annotation indexed =
false, are deselected as well. This reflects that the method of retrieving the
result will be changed from the slower document-from-database to the faster
fields-from-index approach.
When clicking the checkbox within the row of a Group, then the checkbox of all subelements of
this Group will be changed accordingly.
If a Relationship Model Element refers to a self-referencing Relationship, then an input box will
be shown, that specifies how often the Relationship will be followed.
14

-- 14 of 22 --

Filter Definition
This column of the Model Tree shows a simplified version of the Filter Definition that will be
applied to the respective Document Model Element. It can be changed in the Document Model
Element Editor, where more information about the functional An overview about the language
constructs is given here.
For Relationship Model Elements it states an info text which Filter Definitions will be applied
when adding the respective Relationship Links to the Result Set. The concrete Filter Definitions
must be changed on the Document Model Elements.
CAUTION
The Filter Definitions will only act locally. A Filter Definition specified on the
Target Document Model will not be taken into account when adding
Relationship Links to the Result Set.
If the Filter Definition on a Person Target Document Model would state
Has("TeamPerson", "Team", Null, [/LinkFields/Position] == "Team
Lead")
and no Filter Definition is specified within the TeamPerson Relationship
Model Element, then the query yields all persons, that are linked with
position "Team Lead" and all their linked teams. If only those teams, where
the person is linked as team lead should be added to the Result Set, then the
Filter Definition must be repeated on the Document Model Element, that
represent the Relationship Link Fields.
Add an Element
The first element of the Model Tree is the Target Document Model Element, it is usually added while
creating the Query Model using the Add Query Model Modal.
When selecting a Document Model Element in the Model Tree then the respective Document Model
node is highlighted (light blue background) in the Element Picker and all Relationship Model nodes
that are connected to it are active (dark blue border).
When clicking on an active Relationship Model node in the Element Picker, the Relationship Model
Element, its respective Document Model Element and (if specified) a Link Fields Document Model
Element are added to the Query Model.
If the Relationship Model has the same Document Model referenced in both Roles, a Modal is shown
to select the Role for which the nested Document Model Element shall be added.
Element Editor
Only the Document Model Elements can be edited. Clicking on a Document Model Element in the
tree opens the Document Model Element Editor and highlights the respective element in the
Element Picker.
15

-- 15 of 22 --

In the bottom right of the Detail Editor, buttons to apply the changes to the element ("APPLY") or
discard the changes to the element ("CANCEL") are displayed.
Filtering the Model Tree
Initially, the Model Tree shows all Fields of the respective Document Models but hides the metadata.
This can be changed by clicking the Filter Icon in the header of the Model Tree:
Figure 7. Filter Options for Fields in the Document Model Elements
Actions in Model Tree
Actions in the Model Tree can mainly be found in the context menu of the virtual root node and the
model element nodes via the three dot icon or a right click on each row.
Figure 8. Context Menu Containing Node Actions for a Relationship Model Element
Delete
It is possible to delete Relationship Model Elements from the Model Tree. All sub elements and also
nested Relationship Model Elements are deleted from the Query Model as well.
Expand and Collapse
Single nodes such as Document and Relationship Model elements, Groups, Includes, Multi-Selects,
and Attachments can be expanded and collapsed using the arrow icon left of the element name.
It is possible to expand or collapse all elements of the model using the virtual Model Tree node.
16

-- 16 of 22 --

In addition, it is possible to expand or collapse all elements inside a specific node using the node
actions "Expand All" and "Collapse All" in the node’s context menu.
Delete All Elements
Delete all nodes of the current Query Model.
Add Target Document Model Element
If no Document Model Element is present in the Model Tree, either because no Target Document
was selected in the Add Query Model Dialog or because all elements were deleted, then a Target
Document Model Element can be added.
3.2.3. Post Processing
The Post Processing Screen can be seen in Figure 9.
Figure 9. Query Model Editor - Post Processing Screen
The Post Processing Screen contains the following elements:
Sorting
A list of sort fields can be maintained here. The sorting will be applied to the documents of the
Target Document Model.
Sorting supports:
17

-- 17 of 22 --

• non-repeatable Fields of the Target Document Model, that do not have the Annotation indexed =
false
• fields of documents reached via to-1 Relationship traversals
Nested traversals are supported (for example: root document → related document → related
document), as long as each traversal step follows a to-1 Relationship. Relationship-based sorting is
exported in the Query definition as a relationshipField object (Data Services RelationshipOrder).
NOTE
Modeling options are restricted by Data Services configuration:
• mgmtp.a12.dataservices.query.relationshipOrder.maxNestingDepth (default: 5)
• mgmtp.a12.dataservices.query.relationshipOrder.maxCount (default: 5)
Relationship-based sorting also requires explicit null handling and does not allow
fields from repeatable groups.
For more information, please see Data Services > Sorting
Paging
Defines what portion (page) of the results will be present in the Result Set. If Exclude All Root
Documents is set to true in the Target Document Model Element Editor, then this specifies the
number of sorted documents of the Target Document Model (elements in query result "entries").
Otherwise, it specifies the number of links (n linked documents and n Link Fields documents in
query result "links").
Page Number
The index of the portion (page) of the result. Starting from 0.
Page Size
If Exclude All Root Documents = false, then this specifies how many documents of the Target
Document Model are included. If Exclude All Root Documents = true, then this specifies how
many links should be included. The query results "links" section will contain n linked
documents and n Link Fields documents if the size is set to n.
Aggregation
If not the documents of the Target Document Model Elements themselves, but just statistical
information about them is of interest in the Query’s use case, the Aggregate Results switch can
be turned on.
Results can only be aggregated, if the Query Model contains only a Target Document Model
Element (no Relationship Model Element) and Structure of Result Set (Projection) = document.
The settings for Sorting and Paging are ignored.
Aggregate Results
If yes, then the query will not result documents or links but only the Aggregation Result. The
following options are available for this setting:
18

-- 18 of 22 --

Group Documents in Result By
A list of non-repeatable Fields of the Target Document Model, that do not have Annotation
indexed = false. An entry for each combination of the values of the listed Fields will be
created in the Aggregation results.
Aggregations For Grouped Results
For each of the entries defined by the Grouping above, the result of the Aggregation will be
returned.
3.2.4. Element Picker
If the Model Tree is open in the editor, the Element Picker is shown on the left side. It allows to add
new elements to the Model Tree by clicking on active Relationship Model nodes (dark blue border)
with the left mouse button.
All Relationship Model nodes that have the currently highlighted Document Model (light blue
background) referenced in one of its Relationship Roles are shown as active.
When selecting a Document Model Element in the Model Tree, the respective Document Model
node is highlighted in the Element Picker (if it exists in the Model Graph Diagram). Select a different
Model Graph Diagram if the elements you want to add to the Query Model are not present in the
initially selected one. More information about creating a Model Graph Diagram and editing it can
be found in the Model Graph Diagram Documentation.
TIP Click onto the diagram canvas with the right mouse button and hold it to move the
canvas. It is also possible to change the width of Element Picker.
Select the Document Model Element in the Model Tree, for which linked documents should be
retrieved in the Query. Then click on the respective Relationship Model node in the Element Picker
to add the Relationship Model Element to the tree.
If the Relationship Model has the same Document Model referenced in both Roles, a Modal is shown
to select the Role for which the nested Document Model Element shall be added.
3.3. Validation
Upon opening or changing a Query Model, a consistency check is done. Among other things, it
validates that:
• The Fields selected in the In Result column are present in the respective Document Model and
do not have Annotation indexed = false.
• The Filter Definitions only reference Fields that are present in the respective Document Model
and do not have Annotation indexed = false.
• Sorting and Aggregation is only done on non-repeatable Fields that are present in the Target
Document Model and do not have Annotation indexed = false.
• Relationship-based sorting only uses to-1 Relationship traversals, includes explicit null
handling, and stays within the configured limits for traversal depth and number of relationship
19

-- 19 of 22 --

sort orders.
3.4. Refactoring Across Models
Currently, only renaming of the referenced Document Models are refactored in the Query Models.
Changes within the referenced Document and Relationship Models, such as Field name or Role
changes, will not automatically be reflected in the Query Model.
4. Document Model Element Editor
The Document Model Element Editor opens, when a Document Model Element in the Model Tree is
clicked.
Document Model
Name of the Document Model, that is referenced by the Document Model Element. It cannot be
changed.
NOTE
If the Query Model should reference a different Target Document Model, create a
new Query Model and select the correct Document Model in the Add Query
Model Modal.
Structure of Result Set (Projection)
Only shown for the Target Document Model Element.
Currently only "document" can be selected. In this case, the results for the Target Document
Model are in the structure of a regular document.
Exclude All Root Documents
Only shown for the Target Document Model Element.
If not the Target Documents but only the Links retrieved by further Relationship Model Elements
are needed in the Query Result, then this setting can be checked to optimize performance.
If it is set to true, the pagination settings are applied on the linked documents that are retrieved.
Filter Definition
A Filter Definition describes the properties a document must fulfill in order to be included in the
Result Set of the query. See Filter Definition for an overview of available language constructs. It
is translated by the editor into the technical Query Constraints interpreted by Data Services.
NOTE
The Query Model editor creates the Filter Definition from the technical
Constraints that are persisted in the Query Model. This conversion is done every
time the Filter Definition editor is left. Additional line breaks or tabs will be lost.
It is not possible to retain any custom formatting of the Filter Definition.
The editor will display the Filter Definition in its most readable form. Therefore,
when entering
20

-- 20 of 22 --

Match([/Group/Field],"Test")
the editor will convert it into
[/Group/Field] ~ "Test"
Fields included in Result Set
By default, All Fields of Document Model is set to false, so a list of Fields is shown. Any Field, that
does not have Annotation indexed = false, can be selected. Only the respective data for these
Fields will be included in the Result Set.
If All Fields of Document Model is selected, then the complete Document will be retrieved from
the database. To optimize performance, only the needed Fields should be included in the Result
Set. The data for these Fields will then be taken from the Index and not from the database, which
is much faster. Moreover, less data has to be transferred to the Client, which improves the
loading performance for the end user.
The list of Fields represents the state of the checkboxes in the In Result column of the Model
Tree.
5. Glossary
Term Description
Query The specification for a function call to a database service. In A12 Data
Services provides methods to run Queries and return the result. The
result of a Query is a Result Set. Depending on the Result Set, it can be
used directly in A12 Engines or in custom implementations.
The Query defines the quality (which elements are selected from the
database) and quantity (how many of them; what portion of their data) of
the Result Set.
Query Model A Query Model specifies a Query. Usually it is enriched with runtime
context data. For example in an overview, the original Query is enriched
with the page and sorting information the end user sets.
Filter Definition Which documents are retrieved from the database is defined by the Filter
Definition. Only documents that fulfill the Filter Definition will be added
to the Result Set.
The Filter Definition is the modelers representation of the technical
Constraints persisted in the Query Model.
Constraint Filter Definitions are persisted in the Query Model in the form of
Constraints. Constraints reflect the technical implementation of querying
in Data Services. See Data Services > Selection for more technical details.
21

-- 21 of 22 --

Term Description
Document Model
Element
A kind of element that is shown in the Query Model Tree. It represents a
Document Model, so a type of documents that can be found in the Result
Set of the query.
This can also be a Link Field Document Model used in a Relationship
Model.
On these elements, the Filter Definition for the respective documents and
the Fields that are to be included in the Result Set can be specified.
Target Document
Model Element
The first element of the Query Model Tree is always the Target Document
Model Element. It specifies the main result of the Query.
The target documents are selected from the database during the Selection
phase of Data Service’s query execution. In the following phase, the
Projection phase, Data Services will go through the Relationship Model
Elements and add the respective linked documents to the results, if the
specified Filter Conditions on the Relationships Target Role’s Document
Model Element and the Filter Condition of the Link Fields Document
Model Element are met.
The Document Model Element Editor for the Target Document Model
Element shows the query properties Structure of Result Set
(Projection) and Exclude All Root Documents.
Relationship Model
Element
Additionally to the documents of the Target Document Model Element,
further documents can be requested in the query. They will be added to
the Result Set in the Projection phase after the target documents are
selected from the database during the Selection phase of Data Service’s
query execution. Data Services will go through the Relationship Model
Elements and add the respective linked documents to the results, if the
specified Filter Conditions on the Relationships Target Role’s Document
Model Element and the Filter Condition of the Link Fields Document
Model Element are met.
22

-- 22 of 22 --

