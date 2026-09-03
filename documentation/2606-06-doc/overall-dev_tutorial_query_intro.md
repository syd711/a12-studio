# overall dev_tutorial_query_intro

Query API Tutorial
WARNING
This tutorial refers to an older version of A12 (2025.06-ext5) and is being
reworked for the 2026.06 release. Its structure and focus will change
substantially, so the current form will no longer be available.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
For this Query API tutorial, prior knowledge of the following technologies is necessary:
• Data Services
You also need to have the following programs available:
• Text editor / IDE
• A12 Modeling Environment in version 2025.06-ext5
• Bruno in version 2.7.0 or newer
NOTE
Using an older version of Bruno could cause problems when opening the provided
Bruno collections. Therefore, make sure that you are using Bruno in a version
specified above.
Introduction
Query API
With the 2025.06 release, Data Services introduced a new query mode to retrieve document related
data from the server.
NOTE
If you want to get familiar with the Query API in more detail, you can have a look at
the Data Services > Query API documentation.
This tutorial will cover all feature aspects of the new API, but it will not represent
its reference documentation, and not go into details about how the queries are
processed internally.
We will start with the first question that you probably have now:
Why is there a new API for querying document data?
There are multiple reasons and implications, which led to the implementation of the new Query
API:
1

-- 1 of 6 --

• Granular field selection: It allows you to specify which specific fields from the Document
Model should be included in the query results. This eliminates the need to load entire
documents with irrelevant data for a given request.
• Enhanced relationship and CDM query support: The new API structure enables efficient
retrieval of linked data and composed document data, improving flexibility and accuracy when
working with complex relationships.
• Custom document graphs: It supports the creation of custom document graphs, empowering
you to query and structure data, including root, child and link documents.
• Improved performance: Querying and indexing performance are enhanced. Data Services
have transitioned from Solr to PostgreSQL, leveraging its advanced features to improve query
capabilities and efficiency.
• Aggregations: Compute document data by using a set of functions, and group the computed
document results.
• Feature parity with the old API: Despite these enhancements, the new Query API maintains
feature parity with the previous data retrieval API, ensuring consistency in functionality and
authorization. It was not technically possible to achieve in every aspect, but Data Services
introduced new opportunities to create complex queries (see 2025.06 Migration Instructions).
Extended Model Workspace
If you are interested in the general workspace of the tutorial, then check out Tutorials > Intro >
Modeling.
The model workspace of the tutorial application has been extended in this Query API tutorial to
cover features like heterogeneity, relationships and CDMs. For this, the following models have been
added:
2

-- 2 of 6 --

Table 1. Workspace Models
Type Name Description
General ContactPotential_DM Subtype of Contact_DM, which represents a
potential customer with limited data fields.
General ContactPotential_FM Corresponding Form Model to display the
ContactPotential_DM Document Model.
General ContactFinal_DM Subtype of Contact_DM, which represents a
customer contact with more data fields.
General ContactFinal_FM Corresponding Form Model to display the
ContactFinal_DM Document Model.
General Company_* Similar structure as the contact models but
represent company entities.
Relationship
related
CompanyContact Relationship Model between Company_DM and
Contact_DM. This specifies for which company the
customer contact is working for. One contact can
only be employed at one company. One company
can have multiple different contacts employed.
3

-- 3 of 6 --

Type Name Description
Relationship
related
RoleAdditionalField_DM Specifies the role a contact has in the company.
Options are "Executive", "Managerial",
"Operational" and "Other".
Relationship
related
RoleAdditionalField_FM Corresponding Form Model to display the
RoleAdditionalField_DM Document Model.
CDM related Employment_CDM Composed Document Model for merging
Contact_DM and Company_DM via the relationship
CompanyContact.
CDM related EmploymentFinal_CDM Composed Document Model for merging
ContactFinal_DM and Company_DM via the
relationship CompanyContact. This is used in an
"Employment" module in the application.
CDM related EmploymentFinal_CFM Corresponding Form Model to display the
EmploymentFinal_CDM Document Model.
CDM related EmploymentPotential_CDM Composed Document Model for merging
ContactPotential_DM and Company_DM via the
relationship CompanyContact. This is used in an
"Employment" module in the application.
CDM related EmploymentPotential_CFM Corresponding Form Model to display the
EmploymentPotential_CDM Document Model.
CDM related Employment_COM Corresponding Overview Model to display
EmploymentFinal_CDM and
EmploymentPotential_CDM documents in one
overview.
NOTE
To follow the tutorial, it is only necessary to understand which Document Models
are present in the workspace and how they are structured. This knowledge is
required to be able to understand and create queries in the dedicated tasks.
Simplified Logical Notation
Queries and their constraints can become quite complex when combining multiple logical
operators. Therefore, we will be using a simplified notation to represent such complex logical
structures:
Table 2. Notation Elements
Element Description
∧ Combines conditions with a logical "AND". Meaning that both conditions have to be met
to satisfy the constraint.
∨ Combines conditions with a logical "OR". Meaning that at least one condition has to be
met to satisfy the constraint.
4

-- 4 of 6 --

Element Description
¬ Negates a condition.
has Specifies the relationship related condition with the following syntax:
`has(<RELATIONSHIP_MODEL>, <TARGET_ROLE>, <CONSTRAINT>)
== Constraint that represents the exact_match field-aware operator. Meaning that a value
has to fully match to satisfy the condition. Using == in combination with null represents
the undefined_match operator.
<= Constraint that represents the double_range field-aware operator. Meaning that a value
has to be equal or less than the specified number to satisfy the condition.
>= Constraint that represents the double_range field-aware operator. Meaning that a value
has to be equal or greater than the specified number to satisfy the condition.
See the following example:
(Contact.LastName == "Miller" ∨ Contact.LastName == "Baker") ∧ ¬Contact.Gender ==
"MALE" ∧ has("CompanyContact", "Company", Company.CompanyName == "Servicing Good
Ltd.")
Here, all contacts will be returned, which
• have the last name "Miller" OR "Baker", AND
• are not male, AND
• are employed at the company "Servicing Good Ltd.".
Request Collections
In this tutorial, we will use Bruno for creating and executing HTTP requests. You will find the Bruno
collections in the tutorial project in the bruno/*.json files. These contain the examples and solutions
for each hands-on task. The following collections are provided:
• bruno/task1/collection-task1-start.json
• bruno/task1/collection-task1-end.json
• bruno/task2/collection-task2-start.json
• bruno/task2/collection-task2-end.json
• bruno/task3/collection-task3-start.json
• bruno/task3/collection-task3-end.json
This means, if you are interested in the solution and need an additional hint, you can check out the
respective -end tag at any time and import the corresponding collection in Bruno. Instructions on
how to work with the requests and set up your Bruno environment are provided in the dedicated
tutorial chapters.
5

-- 5 of 6 --

Overview
This tutorial serves as an introduction into the Query API, which Data Services introduced for
retrieving data from the server. This will replace the existing Data Services RPC operations for
receiving document related data with the 2025.06 A12 release.
You will receive an explanation about what the Query API is, its feature set, and how to migrate
existing queries to the new structure. The tutorial focuses on the server-side Query API changes and
how to work with the new query layers.
The following aspects will be covered in these tasks:
• Task 1: First Steps
Explanation of the general Query API structure, instructions to enable the query mode and
sending a first request.
• Task 2: Discovering Queries
Deep dive into all Query API features and discovering all querying features in detail.
• Task 3: Migration
Listing and explaining how different aspects of the 2024.06 Data Services API can be migrated to
the new Query API.
Feedback
After having completed this tutorial, we would appreciate your thoughts and feedback: Survey.
6

-- 6 of 6 --

