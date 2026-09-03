# overall dev_tutorial_query_first_steps

Task 1 - First Steps
WARNING
This tutorial refers to an older version of A12 (2025.06-ext5) and is being
reworked for the 2026.06 release. Its structure and focus will change
substantially, so the current form will no longer be available.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Query API > Introduction
before continuing here.
You can check out the tag 2025.06-ext5/query/task-1-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/query/task-1-end to see how
your requests differ from the solution.
Use-Case
At first, you will receive an explanation of the Query API’s purpose and how projects will benefit
from using it.
End Result
Upon finishing this task, you will know:
• How you can benefit from using the new Query API.
• How to configure an A12 application to enable the Query API mode on server-side.
• How to modify the Document Models to be able to query for specific fields.
• How to create a first request using the new Query API.
Step-by-Step Instructions
We will start by having a look at how to enable the query mode in a running application, how to
mark specific Document Model fields to be relevant for querying, and also send our first query with
the new API. Afterward, we will go through the different aspects of the API and how a general
request is structured.
1

-- 1 of 13 --

Indexing
The search index of Data Services is an internal structure which enables performant querying for
data. The respective data fields are stored in the document_fields database table. Only fields which
are present in this table can be used in querying parameters.
By default, all Document Model fields are indexed. To prevent a field from being indexed, explicitly
add the annotation indexed with the value "false" to the corresponding data field.
Initializing Contact Data
Before we can really start discovering the new Query API with our own queries, we need some
testing data. You can already find the predefined data under
import/data/request/initial_data.json. This contains the definition of multiple documents for all
Document Models in our workspace. These RPC operations will be executed during the start-up of
the Init Application.
Start the application in following order to initialize the documents:
1. Run the gradle build command to build the project.
2. Run the gradle :server:init:bootrun --args='--spring.profiles.active=dev-env,init-data'
command to initialize the documents from our initial_data.json.
3. Run the gradle :server:app:bootrun --args='--spring.profiles.active=dev-env' command
to start the Data Services server.
4. Open a new terminal and go to the client directory cd client and start the frontend with
the npm start command.
NOTE If you already started the server application before, you have to stop it; otherwise
the init application can not be started.
NOTE
The gradle :server:app:bootrun as well as the npm start are long-running-tasks.
They will not terminate without being forced to. More details can be found in
Tutorials > General Information > Running the Code.
If you access the application under localhost:8081 and log in with the credentials:
• Username: "admin"
2

-- 2 of 13 --

• Password: "A12PT-admintest"
You should see documents for the Contact, Company and Employment module.
First Query
Before we can send a request, we have to import the already prepared Bruno Collection for the first
task and configure the access token properly.
1. Go to Bruno and click on the more options button in the top-left corner next to the logo [ ⋯ ].
2. Select Collections > Import Collection and use the Bruno collection file
bruno/task1/collection-task1-start.json in our project repository.
3. Open the Query API Tutorial (TUT) collection.
4. Click the [ ⋯ ] next to the newly imported collection and create a new folder named "Task 1 -
First Steps" with the option "New Folder".
5. Click the "REQUEST TEMPLATE" request.
The access token will be used to authorize our HTTP requests for the Data Services server. You can
read more about the access token and its purpose under Tutorials > Backend > Custom Endpoint >
Access Token.
1. Go to the CRM application and receive your access token by following the instructions in
Tutorials > Backend > Custom Endpoint > Receiving an Access Token.
2. Go to Bruno and click on the dropdown in the top-right corner [ No Environment ↓ ].
3. Click on "Configure".
4. Select "A12" environment on the left.
5. Clear the Value field for the Authorization variable, paste your access token without the
3

-- 3 of 13 --

UAABEARER prefix in the field, click the [ Save ] button and afterward the [ Activate ] button.
IMPORTANT Everytime you import a collection from a new task tag it is necessary to set
the Authorization variable in the environment configuration.
Now everything is set up and we can extend the provided Bruno collections to create HTTP requests
for the QUERY operation.
Use the "REQUEST TEMPLATE" request as a starting point and clone it by using the more options
button [ ⋯ ] next to it and select "Clone". Set the name for the new request to "All Contact
Documents" and click [ Clone ]. Move the cloned request to the folder "Task 1 - First Steps" by Drag
& Drop. The request body looks as follows:
File: bruno/task1/collection-task1-start.json > Query API Tutorial (TUT) > Task 1 - First Steps
> All Contact Documents
{
"id": "REQUEST TEMPLATE",
"jsonrpc": "2.0",
"method": "QUERY",
"params": {
"query": {
"targetDocumentModel": "<DOCUMENT MODEL NAME>",
"projectionName": "document OR cdd OR document-graph",
"paging": {
"pageSize": 10,
"pageNumber": 0
}
}
}
}
We will fill out the placeholder values with proper ones, considering our model workspace.
Therefore, insert the following data:
• id: "All Contact Documents".
This is just a descriptive id for the request, it has no impact on the actual query.
• targetDocumentModel: "Contact_DM".
This defines for which Document Model we want to query documents.
• projectionName: "document".
This specifies which kind of data we want to query. Queries are processed and structured
differently for simple document data compared to CDD or document graphs.
Your request should then look like this:
File: bruno/task1/collection-task1-end.json > Query API Tutorial (TUT) > Task 1 - First Steps >
All Contact Documents
4

-- 4 of 13 --

{
"id": "All Contact Documents",
"jsonrpc": "2.0",
"method": "QUERY",
"params": {
"query": {
"targetDocumentModel": "Contact_DM",
"projectionName": "document",
"paging": {
"pageSize": 10,
"pageNumber": 0
}
}
}
}
You can either press [ Enter ] or use the [ → ] button on right side of the URL text field to send the
request. The request result should contain a list of ten entries.
This is the simplest query that we can create, which retrieves all contact documents stored in the
database. We will explore all usable querying features in the Discovering Queries tutorial step.
Besides our document JSON content you can also see additional attributes for each result entry:
• docRef: The document reference, which identifies the document based on its Document Model
name and its database UUID.
5

-- 5 of 13 --

• type: Type of the result entry document. Values are "ROOT", "CHILD" or "LINK".
• documentModelName: The inferred Document Model of the result entry document. This might
differ from the targetDocumentModel in case of heterogeneity or relationships being used.
The New Query API
The newly introduced API handles data retrieval by specifying so-called "Selection" and "Projection"
aspects in a query.
Selection
The query selection defines restrictions on which document data should be loaded. In database-
terms: Which rows should be selected from a table full of entries. When no selection is applied, all
documents for a specific Document Model are returned.
Key functionalities for the selection are as follows:
• Creating queries by concatenating logic and field-aware operators.
• Creating queries by using a simplified search operator.
• Creating queries whilst considering relationships using the has operator.
Projection
The query projection defines restrictions on which fields should be returned based on the root
document. In database-terms: Which columns should be selected from a specific data set.
Key functionalities for the projection are as follows:
• Specifies the data that should be loaded additionally, after the selection is resolved.
• Retrieving only specific fields in the query response decreases the amount of unnecessary data
processed and loaded. In the past, a query response would yield the whole document data with
all respective fields, even if they were not needed.
• Retrieving additional linked document data which enriches the root documents.
• Retrieving partial link data by combining projection properties.
Request Structure
The QUERY RPC operation accepts structured requests with a specific format. These requests offer
multiple optional properties that can be configured to create customized queries. Each query
request contains a single parameter, query, which can be further defined using various properties.
In this section, we will examine the different parameters in detail to provide a comprehensive
understanding. This knowledge will serve as a foundation for creating advanced custom requests in
subsequent tutorial tasks Tutorials > Query API > Discovering Queries and Tutorials > Query API >
Migration.
You can either use the REQUEST TEMPLATE from before or check out the tag 2025.06-
6

-- 6 of 13 --

ext5/query/task-1-end to follow along with the upcoming examples for the various query
parameters.
Query Parameters
Table 1. Query Parameters
Parameter Description
targetDocumentModel* Document Model name for which the query should be
performed.
sort Specifies the sorting of the query result documents. Data Services
does not apply any sorting per default.
paging* Specifies the pagination of the query result.
constraint Restricts the query result based on operators (e.g. logic operators
on Document Model fields).
fields Specifies which Document Model fields should be included in the
query result. If not explicitly provided, all fields are included.
links Specifies which link documents should be included in the query
result.
projectionName* Specifies which projection type should be used for the query. Pre-
defined are "document", "cdd", and "document-graph" as
projection names.
aggregation Specifies a function which can be applied to the query result.
Supported functions are "avg", "count", "min", "max", and "sum".
exclude Specifies whether root documents should be excluded from the
query result. This is useful when only link documents are
required.
Legend
* = Required parameter
Each of these parameters consist of additional properties, which we will examine in the next
sections. You will also be able to practice and use all of these in the following tutorial tasks.
sort Parameter
Defines the sorting behavior for the query result. It is possible to define the sorting based on
multiple fields.
Table 2. sort Parameters
Parameter Description
field* Specifies the Document Model field path for which the query
result should be sorted.
7

-- 7 of 13 --

Parameter Description
direction* Specifies the order for the sorting of the query result. Options are
"ASC" for ascending and "DESC" for descending.
nullHandling* Specifies how null values should be handled for the sorting.
Options are "NULLS_FIRST" and "NULLS_LAST".
ignoreCase* Specifies if the sorting should be case-insensitive. Options are
true or false.
Legend
* = Required parameter
See the following examples for the sort properties:
"sort":[
{
"field":"/Contact/PersonalData/LastName",
"direction":"DESC",
"nullHandling":"NULLS_LAST",
"ignoreCase":false
}
]
In the example above, the query result entries will be sorted by the contact last name in descending
order.
"sort":[
{
"field":"/Contact/PersonalData/LastName",
"direction":"DESC",
"nullHandling":"NULLS_LAST",
"ignoreCase":false
},
{
"field":"/Contact/PersonalData/Nationality",
"direction":"ASC",
"nullHandling":"NULLS_FIRST",
"ignoreCase":true
}
]
In the example above, the query result entries will be first sorted by the contact last names in
descending order and in case of documents having the same last name, the second sort filter will
take effect. This one will sort the entries based on the nationality in ascending order with null
values being first in the order.
NOTE It is recommended to use the "NULLS_LAST" option because it is more efficient in
8

-- 8 of 13 --

terms of performance.
paging Parameter
Table 3. paging Parameters
Parameter Description
pageNumber* Specifies the page number to retrieve document data of the
query result.
pageSize* Specifies the amount of entries for each query result page.
Legend
* = Required parameter
NOTE Pagination in Data Services starts counting from the index 0. This means the first
query result entries are available on the page with the number 0.
See the following example for the paging properties:
"paging":{
"pageSize":5,
"pageNumber":1
}
In the example above, the query result entries are paginated containing five entries per page. The
page with index "1" is selected, meaning the entries on the second page are contained in the query
result, while each page contains five entries.
constraint Parameter
Table 4. constraint Parameters
Parameter Description
operator* Specifies the kind of operator to be used. Options are:
• Field-aware operators: exact_match, simple_search,
undefined_match, double_range, date_range, datefragment_range.
• Logic operators: and, or, not.
• Has operator: has.
operands Specifies an entity as an input for a search term. Some operators
allow for combining multiple operands. It is possible to define
any nested conditional structures.
Legend
* = Required parameter
9

-- 9 of 13 --

NOTE
The different kinds of operators are complex and provide different optional
configuration for refining them. We will go into detail of each operator in the
Tutorials > Query API > Discovering Queries task.
See the following example for the constraint properties:
"constraint":{
"operator":"and",
"operands":[
{
"operator":"exact_match",
"field":"/Contact/PersonalData/Nationality",
"value":"German"
},
{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Baker"
},
{
"operator":"not",
"operand":{
"operator":"exact_match",
"field":"/Contact/PersonalData/Gender",
"value":"MALE"
}
}
]
}
Simplified logic:
Contact.Nationality == "German" ∧ Contact.LastName == "Baker" ∧ ¬Contact.Gender ==
"MALE"
In the example above, the query result entries are filtered by a combination of field-aware as well
as logic operators. Only those contact documents are selected, which are "German", with last name
"Baker", and not male.
fields Parameter
The fields parameter is part of the projection for the query result. It allows to specify the data
fields which should be returned. All other Document Model fields will be ignored. Providing this
parameter improves performance and reduces the result payload.
See the following example for the fields parameter:
10

-- 10 of 13 --

"fields":[
"/Contact/PersonalData/FirstName",
"/Contact/PersonalData/LastName"
]
In the example above, the query results are limited to include only the first and last name fields
from the documents. All additional fields defined in the Document Model are systematically
excluded from the returned data set.
NOTE
This way also the metadata fields under the root group __meta will be excluded from
the query result. If these are required for the response, the specific ones can be
added to the fields array.
links Parameter
The links parameter allows selecting additionally related documents in a relationship next to the
query root documents. It is possible to define constraints for the link documents, which allows the
filtering of such.
Table 5. links Parameters
Parameter Description
relationshipModel* Specifies the Relationship Model, which document data shall be
retrieved for.
targetRole* Specifies the target role in the relationship that will be linked to
the root document. Data Services differentiates for document
types "ROOT", "CHILD" and "LINK".
constraint Defines a condition on the target document of the relationship,
which allows for filtering of the link documents.
linkDocumentConstraint Defines a condition on the link document, which allows for
filtering the list of links. This is only allowed when a Link
Document Model is defined for the relationship.
fields Specifies which fields of the "CHILD" documents should be
included in the query result. If not explicitly provided, all fields
are included.
linkDocumentFields Specifies which fields of the "LINK" documents should be
included in the query result. If not explicitly provided, all fields
are included.
links Allows for linking additional documents to the link documents. It
is possible to define either recursive or nested structures.
11

-- 11 of 13 --

Parameter Description
maxDepth Relevant for self-referencing relationships. It is possible to query
for such relationships. In these cases the maxDepth parameter has
to be provided. This specifies how many of these references will
be resolved. This parameter is required when a self-referencing
relationship is detected in a link during the query validation.
Legend
* = Required parameter
See the following example for the links parameter:
"links":{
"relationshipModel":"CompanyContact",
"targetRole":"Company",
"fields":[
"/Company/GeneralInformation/CompanyName",
"/__meta/docRef"
],
"constraint":{
"operator":"exact_match",
"field":"/Company/GeneralInformation/CompanyName",
"value":"Trading Good Ltd."
},
"linkDocumentFields":[
"/Root/Role",
"/__meta/docRef"
],
"linkDocumentConstraint":{
"operator":"exact_match",
"field":"/Root/Role",
"value":"executive"
}
}
In the example above, the link documents for the relationship CompanyContact are selected and
filtered by the following criteria:
• The target document company name must be "Trading Good Ltd.".
• The "LINK" document role field must be "executive".
• The fields for the "CHILD" documents are limited to the company name and the document
reference.
• The fields for the "LINK" documents are limited to the role and the document reference.
These documents will be added to the query result.
12

-- 12 of 13 --

Aggregation Parameter
Table 6. aggregation Parameters
Parameter Description
aggregations* List of different aggregation operations.
function* Specifies the type of operation, which should be processed.
Options are "avg", "min", "max", "sum" and "count".
field* Specifies the Document Model field for which the aggregation
should be processed or grouped.
group* Collect the aggregation results and group them based on a field.
Legend
* = Required parameter
See the following example for the aggregation parameter:
"aggregation":{
"aggregations":[
{
"function":"count",
"field":"/Contact/PersonalData/FirstName"
}
],
"group":[
{
"field":"/Contact/PersonalData/LastName"
}
]
}
In the example above, the contacts are counted and then grouped by the last name.
Conclusion
In this chapter, you have been introduced to the foundational concepts of the new Query API,
including its core advantages and processing the first request. The subsequent step will provide a
comprehensive exploration of advanced query functionalities, delving deeper into the API’s
sophisticated capabilities and operational mechanisms.
If something does not seem right, or you got stuck at any point, you can just check out 2025.06-
ext5/query/task-1-end to see differences between both implementations.
Task 2: Discovering Queries »
13

-- 13 of 13 --

