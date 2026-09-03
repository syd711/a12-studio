# overall dev_tutorial_query_migration

Task 3 - Migration
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
before continuing here. For this tutorial task, it is necessary to have
knowledge about the Query API and its operations. Therefore, check out the
Tutorials > Query API > Discovering Queries sections about the Query API
operators and features.
You can check out the tag 2025.06-ext5/query/task-3-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/query/task-3-end to see how
your code differs from the solution.
Use-Case
In this task, we will go through each RPC operation that retrieves data from the server and how it
can be migrated to the new Query API. This includes the following operations:
• GET_DOCUMENT
• LIST_DOCUMENTS
• LIST_CANDIDATES
• LIST_LINKS
• LIST_TERMINATING_LINKS
• LOAD_DOCUMENT_GRAPH
• LIST_CDDS
Additionally, we will have a look at the other migration changes introduced by the new Query API
implementation.
End Result
Upon finishing this task, you will know:
1

-- 1 of 114 --

• How to create equivalent Query API requests for each RPC operation of the old API.
• Which additional changes were introduced by the Query API and how to process them.
Step-by-Step Instructions
In the following sections, we are going to check out the effects the new Query API has on existing
projects.
NOTE
After you have checked out the start tag for this task you should import the Bruno
Collection file bruno/task3/collection-task3-start.json in Bruno. If it is the first
time that you are importing the collection, follow Tutorials > Query API > First Steps
> First Query.
WARNING
The Bruno collection contains requests for the old original data retrieval
operations. These cannot be executed in 2025.06 anymore. Therefore, the
request responses will be provided explicitly in the documentation.
Migrating Operations
Data Services offers a variety of different RPC operations to retrieve document related data from
the server. The Query API replaces these, and the old operations are dropped with the 2025.06 A12
release. This means that the old operations have to be migrated to the new API.
For further explanations and more details about the individual RPC operations, have a look at A12
Data Services > JSON-RPC Endpoint > JSON-RPC 2.0 Core Operations.
WARNING
You cannot copy-paste examples below that contain a docRef parameter. The
document reference will be different in your database. Therefore, use a valid
document reference of one of your documents.
GET_DOCUMENT
IMPORTANT This operation will remain in the 2025.06 release.
This operation allows loading one specific document based on the document reference.
Table 1. GET_DOCUMENT Parameters
Parameter Description
docRef* Specifies the document reference of the document to be loaded.
Legend
* = Required parameter
See the following example for the GET_DOCUMENT operation:
2

-- 2 of 114 --

File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original GET_DOCUMENT
{
"jsonrpc":"2.0",
"id":"Original GET_DOCUMENT",
"method":"GET_DOCUMENT",
"params":{
"docRef":"ContactFinal_DM/41e180b2-6420-4643-a420-a2db3d8cca9f"
}
}
In the example above, the single document matching the document reference is loaded. This
document contains all fields and metadata.
Your task:
• Create a valid Query API request which loads the same document as the Original
GET_DOCUMENT request.
• Hints:
◦ Check the metadata fields for a fitting search field.
◦ Use the correct field-aware operator to match the metadata field.
▼ Click to see solution
• The /__meta/docRef Document Model field stores the document reference of a document.
• The exact_match operator allows filtering for one specific field.
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query GET_DOCUMENT
{
"jsonrpc":"2.0",
"id":"Query GET_DOCUMENT",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"exact_match",
"field":"/__meta/docRef",
"value":"ContactFinal_DM/41e180b2-6420-4643-a420-a2db3d8cca9f"
},
"paging":{
"pageNumber":0,
"pageSize":10
}
}
}
3

-- 3 of 114 --

}
NOTE
Even though the GET_DOCUMENT operation will exist in the 2025.06 release, it is still
beneficial to know how to create a request with the same behavior manually. This
way you have more control of the additional data being processed and loaded. For
example, you could provide the fields property that only loads specific Document
Model fields for the corresponding document.
LIST_DOCUMENTS
This operation allowed loading multiple documents of one specific Document Model. By providing a
set of parameters, the results could be adjusted accordingly.
Table 2. LIST_DOCUMENTS Parameters
Parameter Description
documentModelName* Specifies the Document Model name to load documents for.
filter Specifies field-based filters which the documents must satisfy to
match.
sort Specifies the order in which the resulting documents shall be
sorted.
page Specifies the pagination which shall be applied to the search
result.
facets Group documents based on specific constraints. Additionally,
facets allow running statistical calculations on the grouped
document data.
Legend
* = Required parameter
See the following example for the LIST_DOCUMENTS operation:
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original LIST_DOCUMENTS
{
"jsonrpc":"2.0",
"id":"Original LIST_DOCUMENTS",
"method":"LIST_DOCUMENTS",
"params":{
"documentModelName":"Contact_DM",
"filter":{
"filters":[
"Contact.PersonalData.LastName:Baker",
"Contact.ContactType:Final",
"-Contact.PersonalData.Gender:Weiblich"
],
"lang":"de"
4

-- 4 of 114 --

},
"sort":{
"order":"Contact.PersonalData.FirstName ASC"
}
}
}
In the example above, the following constraints and request configurations are defined:
1. Documents of the Contact_DM shall be searched for.
2. Filters shall be applied loading specific documents satisfying the following conditions:
a. The contact must have the last name "Baker" and
b. The contact must be of type "Final" and
c. The contact must not be female.
3. The german locale "de" shall be used for localizing the enumeration fields.
4. The result list shall be ordered by the contact’s first name in ascending direction.
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original LIST_DOCUMENTS",
"result":{
"fullSize":3,
"page":{
"offset":0,
"limit":10
},
"entries":[
{
"docRef":"ContactFinal_DM/981d5b5e-a039-4859-883d-89647cffa0ae",
"documentModelName":"ContactFinal_DM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Alex",
"LastName":"Baker",
"EmailAddress":"alex.baker@gmx.com",
"DateOfBirth":"1983-12-19",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"partner"
},
"Project":{
"Budget":360000
},
"Address":[
5

-- 5 of 114 --

{
"Street":"Waldstrasse",
"Housenumber":"2",
"City":"Cologne",
"Zip":"51145",
"Country":"Germany"
},
{
"Street":"Siemensstrasse",
"Housenumber":"15",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
},
{
"Street":"Voltastrasse",
"Housenumber":"3",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/0024823824",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/981d5b5e-a039-4859-883d-
89647cffa0ae",
"modelVersion":null
}
}
},
{
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-039146727d5f",
"documentModelName":"ContactFinal_DM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Henry",
"LastName":"Baker",
"EmailAddress":"henry.baker@gmx.com",
6

-- 6 of 114 --

"DateOfBirth":"1987-05-21",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"lead"
},
"Project":{
"Budget":150000
},
"Address":[
{
"Street":"Kastanienallee",
"Housenumber":"25",
"City":"Wiesmoor",
"Zip":"26639",
"Country":"Germany"
},
{
"Street":"Storkower Strasse",
"Housenumber":"89",
"City":"Hartenfels",
"Zip":"56244",
"Country":"Germany"
},
{
"Street":"Sonnenallee",
"Housenumber":"97",
"City":"Augsburg",
"Zip":"86172",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/9387913",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f",
"modelVersion":null
7

-- 7 of 114 --

}
}
},
{
"docRef":"ContactFinal_DM/8b78735d-fb19-4809-b436-4dbba1030952",
"documentModelName":"ContactFinal_DM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Maximilian",
"LastName":"Baker",
"EmailAddress":"maximilian.baker@gmx.com",
"DateOfBirth":"1995-06-19",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"lead"
},
"Project":{
"Budget":300000
},
"Address":[
{
"Street":"Rathenausstrasse",
"Housenumber":"50",
"City":"Fürth",
"Zip":"90702",
"Country":"Germany"
},
{
"Street":"Jenaer Strasse",
"Housenumber":"77",
"City":"Duisburg",
"Zip":"47053",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/1328139912",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/188123829",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
8

-- 8 of 114 --

"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/8b78735d-fb19-4809-b436-
4dbba1030952",
"modelVersion":null
}
}
}
]
}
}
Your task:
• Create a valid Query API request which loads the same documents as the Original
LIST_DOCUMENTS request.
• Hints:
◦ You need a logical operator to combine the different conditions.
◦ Both, the simple_search as well as the exact_match operator could be used.
▼ Click to see solution
• To combine the three conditions the and operator has to be used.
• The sort parameter provides more configuration options by using the nullHandling and
ignoreCase properties. Therefore, we have more options at hand to customize the query
results.
• The proper localization of the enumeration values requires an additional HTTP request
header:
◦ key: Accept-Language
◦ value: de
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query LIST_DOCUMENTS
{
"jsonrpc":"2.0",
"id":"Query LIST_DOCUMENTS",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
9

-- 9 of 114 --

"value":"Baker"
},
{
"operator":"exact_match",
"field":"/Contact/ContactType",
"value":"Final"
},
{
"operator":"not",
"operand":{
"operator":"exact_match",
"field":"/Contact/PersonalData/Gender",
"value":"Weiblich"
}
}
]
},
"sort":{
"field":"/Contact/PersonalData/FirstName",
"direction":"ASC",
"nullHandling":"NULLS_LAST",
"ignoreCase":true
},
"paging":{
"pageNumber":0,
"pageSize":10
}
}
}
}
LIST_LINKS
This operation allows loading relationship links and documents.
Table 3. LIST_LINKS Parameters
Parameter Description
source* Specifies the relationship source for the operation that loads all
related documents and link documents.
filter Specifies field-based filters which the documents must satisfy to
match.
sort Specifies the order in which the resulting documents shall be
sorted.
page Specifies the pagination which shall be applied to the search
result.
10

-- 10 of 114 --

Legend
* = Required parameter
See the following example for the LIST_LINKS operation:
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original LIST_LINKS 1
{
"jsonrpc":"2.0",
"id":"Original LIST_LINKS 1",
"method":"LIST_LINKS",
"params":{
"source":{
"relationshipModel":"CompanyContact",
"role":"Company",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-b0ceda37edb0"
}
}
}
In the example above, we are searching for all related documents and link documents of one
specific company.
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original LIST_LINKS 1",
"result":{
"fullSize":3,
"page":{
"offset":0,
"limit":10
},
"entries":[
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/ab6e8222-77ca-41fc-
11

-- 11 of 114 --

a85d-3404159d253a"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/4fc930c1-f168-
4fb3-a986-3a6918737f75",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"8"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Olaf",
"LastName":"Schmitz",
"EmailAddress":"olaf.schmitz@gmx.com"
},
"Project":{
"Budget":100000
}
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/ab6e8222-77ca-41fc-a85d-
3404159d253a",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/4fc930c1-f168-4fb3-
a986-3a6918737f75",
"modelVersion":null
}
}
}
12

-- 12 of 114 --

},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/7a47a57b-7db5-44e6-
ba89-c78b36ffa247"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/958a8c35-47e1-
4478-943b-f05f7ab44151",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"9"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Julia",
"LastName":"Limhold",
"EmailAddress":"julia.limhold@gmx.com"
},
"Project":{
"Budget":350000
},
"Address":[
{
"Street":"Friedrich-Hirsch-Strasse",
"Housenumber":"110",
"City":"Cologne",
"Zip":"51145",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/112300421",
"Type":"MOBILE"
13

-- 13 of 114 --

},
{
"PhoneNumber":"+492203/39129301",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/7a47a57b-7db5-44e6-ba89-
c78b36ffa247",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/958a8c35-47e1-4478-
943b-f05f7ab44151",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/386e250d-c6c8-4eaa-
14

-- 14 of 114 --

b44c-7e9aa521aae5"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/5373d947-0ca3-
4ca7-af76-fcd9b2f253f6",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"10"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"James",
"LastName":"Baker",
"EmailAddress":"james.baker@gmx.com"
},
"Project":{
"Budget":400000
},
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/386e250d-c6c8-4eaa-b44c-
7e9aa521aae5",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
15

-- 15 of 114 --

"docRef":"RoleAdditionalField_DM/5373d947-0ca3-4ca7-
af76-fcd9b2f253f6",
"modelVersion":null
}
}
}
}
]
}
}
Your task:
• Create a valid Query API request which loads the same documents as the Original LIST_LINKS
1 request.
• Hints:
◦ You need a logical operator to combine the different conditions.
◦ You have to use the has operator for filtering the root documents.
◦ To add the link documents to the request result it is necessary to provide the links property.
▼ Click to see solution
• By using the exact_match with the document reference we are only selecting the root
document for the specific company.
• The has operator filters all contact documents to only match the ones that are in relationship
with contacts.
• To also include the child as well as the link documents, the links parameter is set. This will
add the related links for the root document accordingly.
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query LIST_LINKS 1
{
"jsonrpc":"2.0",
"id":"Query LIST_LINKS 1",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Company_DM",
"projectionName":"document",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Contact"
},
{
16

-- 16 of 114 --

"operator":"exact_match",
"field":"/__meta/docRef",
"value":"Company_DM/e0aa4090-10f4-49f8-9a04-b0ceda37edb0"
}
]
},
"links":[
{
"relationshipModel":"CompanyContact",
"targetRole":"Contact"
}
],
"paging":{
"pageNumber":0,
"pageSize":10
}
}
}
}
NOTE
The query would yield the same result if we remove the has condition here
completely, because we are already selecting a single document with the
exact_match operator by filtering for the document reference.
You will also notice that the links are not paginated in the result. This can be easily checked by
reducing the pageSize to "1" and observing no change of behavior for the link results.
In case you need the results to be paginated, you can do the following:
• Reverse the query constraint by changing the targetDocumentModel from "Company_DM" to
"Contact_DM".
• Change the targetRole of the has operator.
• Add the exact_match condition for the document reference as a constraint under the has
operator.
• Remove the links specification.
After applying the aforementioned adjustments to our Query LIST_LINKS 1, your request should
look like this:
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query LIST_LINKS 1
Alternative
{
"jsonrpc":"2.0",
"id":"Query LIST_LINKS 1 Alternative",
"method":"QUERY",
"params":{
"query":{
17

-- 17 of 114 --

"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company",
"constraint":{
"operator":"exact_match",
"field":"/__meta/docRef",
"value":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
}
}
]
},
"paging":{
"pageNumber":0,
"pageSize":10
}
}
}
}
You will notice that we retrieve exactly the same results. The difference is that these are now
contained in the result entries instead of the links.
You might have noticed one difference in the query results. The old API does not contain the root
document in the result. Only the child and link documents are added to the response. If you want to
exclude the root documents, you can add the exclude property to the query request.
Now, we will take a look at the other direction of the relationship. Previously, we searched for
related documents of a specific company. Let’s check out the requests for both APIs where we want
to filter documents related to a specific contact.
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original LIST_LINKS 2
{
"jsonrpc":"2.0",
"id":"Original LIST_LINKS 2",
"method":"LIST_LINKS",
"params":{
"source":{
"relationshipModel":"CompanyContact",
"role":"Contact",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-039146727d5f"
}
}
18

-- 18 of 114 --

}
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original LIST_LINKS 2",
"result":{
"fullSize":1,
"page":{
"offset":0,
"limit":10
},
"entries":[
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f"
},
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/24cbe98a-8a9f-
4925-a556-c32205476e98",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"1"
},
"document":{
"target":{
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
19

-- 19 of 114 --

]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"executive"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-
a556-c32205476e98",
"modelVersion":null
}
}
}
}
]
}
}
Your task:
• Create a valid Query API request which loads the same documents as the Original LIST_LINKS
2 request.
• Exclude the root documents from the query results.
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query LIST_LINKS 2
{
"jsonrpc":"2.0",
"id":"Query LIST_LINKS 2",
"method":"QUERY",
"params":{
"query":{
20

-- 20 of 114 --

"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company"
},
{
"operator":"exact_match",
"field":"/__meta/docRef",
"value":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f"
}
]
},
"exclude":true,
"links":[
{
"relationshipModel":"CompanyContact",
"targetRole":"Company"
}
],
"paging":{
"pageNumber":0,
"pageSize":10
}
}
}
}
LIST_CANDIDATES
This operation allows filtering all available relationship target documents for source documents.
Table 4. LIST_CANDIDATES Parameters
Parameter Description
source* Specifies the relationship source for the operation that loads all
related documents and link documents.
filter Specifies field-based filters which the documents must satisfy to
match.
sort Specifies the order in which the resulting documents shall be
sorted.
page Specifies the pagination which shall be applied to the search
result.
21

-- 21 of 114 --

Parameter Description
excludeLinkIdFromResult Specifies if the id of an existing relationship link between the
source document and candidate should be excluded. Default is
false.
Legend
* = Required parameter
See the following example for the LIST_CANDIDATES operation:
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original LIST_CANDIDATES
{
"jsonrpc":"2.0",
"id":"Original LIST_CANDIDATES",
"method":"LIST_CANDIDATES",
"params":{
"source":{
"relationshipModel":"CompanyContact",
"role":"Company"
},
"filter":{
"filters":[
"Contact.PersonalData.LastName:Baker",
"Contact.Project.Budget:[200000 TO ]",
"-Contact.PersonalData.CustomerType:VIP"
],
"lang":"de"
},
"sort":{
"order":"Contact.PersonalData.FirstName ASC"
},
"page":{
"offset":0,
"limit":10
}
}
}
In the example above, we are searching for all available link candidates that could be associated
with companies. Additionally, only contacts shall be loaded with the last name "Baker", a project
budget of at least 200,000 and not being a "VIP" customer. Also, the query results shall be ordered
by the first name in ascending direction.
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original LIST_CANDIDATES",
22

-- 22 of 114 --

"result":{
"fullSize":3,
"page":{
"offset":0,
"limit":10
},
"entries":[
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":null,
"docRef":null
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/981d5b5e-a039-4859-883d-
89647cffa0ae"
}
],
"predecessorLinkRef":null,
"position":"TOP"
},
"id":null
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Alex",
"LastName":"Baker",
"EmailAddress":"alex.baker@gmx.com",
"DateOfBirth":"1983-12-19",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"partner"
},
"Project":{
"Budget":360000
},
"Address":[
{
"Street":"Waldstrasse",
"Housenumber":"2",
"City":"Cologne",
"Zip":"51145",
23

-- 23 of 114 --

"Country":"Germany"
},
{
"Street":"Siemensstrasse",
"Housenumber":"15",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
},
{
"Street":"Voltastrasse",
"Housenumber":"3",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/0024823824",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/981d5b5e-a039-4859-883d-
89647cffa0ae",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":null,
"docRef":null
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/386e250d-c6c8-4eaa-
24

-- 24 of 114 --

b44c-7e9aa521aae5"
}
],
"predecessorLinkRef":null,
"position":"TOP"
},
"id":null
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"James",
"LastName":"Baker",
"EmailAddress":"james.baker@gmx.com"
},
"Project":{
"Budget":400000
},
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/386e250d-c6c8-4eaa-b44c-
7e9aa521aae5",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":null,
"docRef":null
},
{
25

-- 25 of 114 --

"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/8b78735d-fb19-4809-b436-
4dbba1030952"
}
],
"predecessorLinkRef":null,
"position":"TOP"
},
"id":null
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Maximilian",
"LastName":"Baker",
"EmailAddress":"maximilian.baker@gmx.com",
"DateOfBirth":"1995-06-19",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"lead"
},
"Project":{
"Budget":300000
},
"Address":[
{
"Street":"Rathenausstrasse",
"Housenumber":"50",
"City":"Fürth",
"Zip":"90702",
"Country":"Germany"
},
{
"Street":"Jenaer Strasse",
"Housenumber":"77",
"City":"Duisburg",
"Zip":"47053",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/1328139912",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/188123829",
"Type":"WORK"
26

-- 26 of 114 --

}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/8b78735d-fb19-4809-b436-
4dbba1030952",
"modelVersion":null
}
}
}
}
]
}
}
Your task:
• Create a valid Query API request which loads the same documents as the Original
LIST_CANDIDATES request.
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query LIST_CANDIDATES
{
"jsonrpc":"2.0",
"id":"Query LIST_CANDIDATES",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Baker"
},
{
"operator":"double_range",
"field":"/Contact/Project/Budget",
"from":"200000"
},
{
27

-- 27 of 114 --

"operator":"not",
"operand":{
"operator":"exact_match",
"field":"/Contact/PersonalData/CustomerType",
"value":"VIP"
}
}
]
},
"sort":[
{
"direction":"ASC",
"field":"/Contact/PersonalData/FirstName",
"ignoreCase":true,
"nullHandling":"NULLS_LAST"
}
],
"paging":{
"pageNumber":0,
"pageSize":10
}
}
}
}
Similar to what we did with the LIST_LINKS operation, we can also change the direction of the
relationship and search for all contacts that could be linked to a specific company by using the
links parameter.
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original LIST_CANDIDATES
Alternative
{
"jsonrpc":"2.0",
"id":"Query LIST_CANDIDATES Alternative",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Company_DM",
"projectionName":"document",
"exclude":true,
"links":[
{
"relationshipModel":"CompanyContact",
"targetRole":"Contact",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"exact_match",
28

-- 28 of 114 --

"field":"/Contact/PersonalData/LastName",
"value":"Baker"
},
{
"operator":"double_range",
"field":"/Contact/Project/Budget",
"from":"200000"
},
{
"operator":"not",
"operand":{
"operator":"exact_match",
"field":"/Contact/PersonalData/CustomerType",
"value":"VIP"
}
}
]
}
}
],
"sort":[
{
"direction":"ASC",
"field":"/Contact/PersonalData/FirstName",
"ignoreCase":true,
"nullHandling":"NULLS_LAST"
}
],
"paging":{
"pageNumber":0,
"pageSize":10
}
}
}
}
NOTE
The excludeLinkIdFromResult parameter is not relevant anymore in the new Query
API. By not specifying the links parameter in the query request you are able to
achieve the same behavior.
LIST_TERMINATING_LINKS
This operation returns all documents that only participate in one role of the relationship but not
the other. If you imagined the documents and links as a tree, the result of this operation would
represent roots and leaves.
Table 5. LIST_CANDIDATES Parameters
29

-- 29 of 114 --

Parameter Description
relationshipModelName* Specifies the name of the Relationship Model to load the
terminating links for.
terminatingRoleName* Specifies which side of the relationship the terminating links
shall be retrieved from.
loadOrphans Specifies if documents without participating in a relationship
should be loaded. Default value is false.
Legend
* = Required parameter
See the following example for the LIST_TERMINATING_LINKS operation:
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original
LIST_TERMINATING_LINKS 1
{
"jsonrpc":"2.0",
"id":"Original LIST_TERMINATING_LINKS 1",
"method":"LIST_TERMINATING_LINKS",
"params":{
"relationshipModelName":"CompanyContact",
"terminatingRoleName":"Contact"
}
}
In the example above, we are searching for all terminating links for contacts. You might notice that
the result resembles the one from the LIST_LINKS operation. The reason behind this is that all
relationship links in the tutorial workspace are already implicit terminating links.
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original LIST_TERMINATING_LINKS 1",
"result":[
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
},
{
30

-- 30 of 114 --

"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/386e250d-c6c8-4eaa-b44c-
7e9aa521aae5"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/5373d947-0ca3-4ca7-
af76-fcd9b2f253f6",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"10"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"James",
"LastName":"Baker",
"EmailAddress":"james.baker@gmx.com"
},
"Project":{
"Budget":400000
},
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/386e250d-c6c8-4eaa-b44c-
7e9aa521aae5",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
31

-- 31 of 114 --

"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/5373d947-0ca3-4ca7-af76-
fcd9b2f253f6",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/7a47a57b-7db5-44e6-ba89-
c78b36ffa247"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/958a8c35-47e1-4478-
943b-f05f7ab44151",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"9"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Julia",
"LastName":"Limhold",
"EmailAddress":"julia.limhold@gmx.com"
},
"Project":{
"Budget":350000
},
"Address":[
{
"Street":"Friedrich-Hirsch-Strasse",
"Housenumber":"110",
32

-- 32 of 114 --

"City":"Cologne",
"Zip":"51145",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/112300421",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/39129301",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/7a47a57b-7db5-44e6-ba89-
c78b36ffa247",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/958a8c35-47e1-4478-943b-
f05f7ab44151",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
33

-- 33 of 114 --

"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/ab6e8222-77ca-41fc-a85d-
3404159d253a"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/4fc930c1-f168-4fb3-
a986-3a6918737f75",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"8"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Olaf",
"LastName":"Schmitz",
"EmailAddress":"olaf.schmitz@gmx.com"
},
"Project":{
"Budget":100000
}
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/ab6e8222-77ca-41fc-a85d-
3404159d253a",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
34

-- 34 of 114 --

"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/4fc930c1-f168-4fb3-a986-
3a6918737f75",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/5793d501-1c1e-4c41-8fe2-
b8f732cc7305"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/0119190f-9862-40fe-
8fb5-d9f6a0676d62",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"7"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Hans",
"LastName":"Mayer",
"EmailAddress":"hans.mayer@gmx.com"
},
"Project":{
"Budget":250000
},
"Address":[
{
"Street":"Berliner Strasse",
"Housenumber":"33",
35

-- 35 of 114 --

"City":"Bonn",
"Zip":"52304",
"Country":"Germany"
},
{
"Street":"Fasanenweg",
"Housenumber":"2",
"City":"Königswinter",
"Zip":"53420",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/92139129",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/5793d501-1c1e-4c41-8fe2-
b8f732cc7305",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"executive"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/0119190f-9862-40fe-8fb5-
d9f6a0676d62",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
36

-- 36 of 114 --

"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/964a0945-69d3-4fda-ba6e-
f151fd58e069"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/89e3b518-d657-4bb8-
8737-2f952bb1b8b2",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"6"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Pierre",
"LastName":"Miller",
"EmailAddress":"pierre.miller@gmx.com"
},
"Project":{
"Budget":500000
},
"Phones":[
{
"PhoneNumber":"+33176/321939213",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/964a0945-69d3-4fda-ba6e-
f151fd58e069",
"modelVersion":null
}
37

-- 37 of 114 --

},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"other"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/89e3b518-d657-4bb8-8737-
2f952bb1b8b2",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/5232042c-d259-4a8d-80ce-
7570b455323e"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/e093313d-03ad-428b-
acd0-ca6e493e61f1",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"5"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Miriam",
"LastName":"Miller",
38

-- 38 of 114 --

"EmailAddress":"miriam.miller@gmx.com",
"DateOfBirth":"2000-03-02",
"Nationality":"French",
"Gender":"FEMALE",
"CustomerType":"partner"
},
"Project":{
"Budget":1000000
},
"Address":[
{
"Street":"Rue de Paris",
"Housenumber":"1",
"City":"Lille",
"Zip":"59000",
"Country":"France"
}
],
"Phones":[
{
"PhoneNumber":"+33176/931293801",
"Type":"MOBILE"
},
{
"PhoneNumber":"+4933/10031290123",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/5232042c-d259-4a8d-80ce-
7570b455323e",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"managerial"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
39

-- 39 of 114 --

"docRef":"RoleAdditionalField_DM/e093313d-03ad-428b-acd0-
ca6e493e61f1",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/981d5b5e-a039-4859-883d-
89647cffa0ae"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/bda4773a-3049-4d8a-
8941-041c5c835986",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"4"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Alex",
"LastName":"Baker",
"EmailAddress":"alex.baker@gmx.com",
"DateOfBirth":"1983-12-19",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"partner"
},
"Project":{
"Budget":360000
},
"Address":[
{
"Street":"Waldstrasse",
40

-- 40 of 114 --

"Housenumber":"2",
"City":"Cologne",
"Zip":"51145",
"Country":"Germany"
},
{
"Street":"Siemensstrasse",
"Housenumber":"15",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
},
{
"Street":"Voltastrasse",
"Housenumber":"3",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/0024823824",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/981d5b5e-a039-4859-883d-
89647cffa0ae",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/bda4773a-3049-4d8a-8941-
041c5c835986",
41

-- 41 of 114 --

"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/bf34a2d3-9296-46df-a86b-
0995eb4daddf"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/8ccd097e-25f7-4a41-
b57a-2bd32b54e0e1",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"3"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Anna",
"LastName":"Baker",
"EmailAddress":"anna.baker@gmx.com",
"DateOfBirth":"1990-08-06",
"Nationality":"German",
"Gender":"FEMALE",
"CustomerType":"vip"
},
"Project":{
"Budget":950000
},
"Address":[
{
"Street":"Kaiserstrasse",
"Housenumber":"12",
"City":"Berlin",
42

-- 42 of 114 --

"Zip":"12392",
"Country":"Germany"
},
{
"Street":"Adenauerstrasse",
"Housenumber":"90",
"City":"Nuremberg",
"Zip":"90512",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/57292390",
"Type":"MOBILE"
},
{
"PhoneNumber":"+4933/12300412",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/bf34a2d3-9296-46df-a86b-
0995eb4daddf",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"managerial"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/8ccd097e-25f7-4a41-b57a-
2bd32b54e0e1",
"modelVersion":null
}
}
}
},
43

-- 43 of 114 --

{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/8b78735d-fb19-4809-b436-
4dbba1030952"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/eb8bfa4b-57db-42e8-
964a-e9a0bdbe9a97",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"2"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Maximilian",
"LastName":"Baker",
"EmailAddress":"maximilian.baker@gmx.com",
"DateOfBirth":"1995-06-19",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"lead"
},
"Project":{
"Budget":300000
},
"Address":[
{
"Street":"Rathenausstrasse",
"Housenumber":"50",
"City":"Fürth",
"Zip":"90702",
"Country":"Germany"
},
{
"Street":"Jenaer Strasse",
44

-- 44 of 114 --

"Housenumber":"77",
"City":"Duisburg",
"Zip":"47053",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/1328139912",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/188123829",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/8b78735d-fb19-4809-b436-
4dbba1030952",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"executive"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/eb8bfa4b-57db-42e8-964a-
e9a0bdbe9a97",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
45

-- 45 of 114 --

{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-
a556-c32205476e98",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"1"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Henry",
"LastName":"Baker",
"EmailAddress":"henry.baker@gmx.com",
"DateOfBirth":"1987-05-21",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"lead"
},
"Project":{
"Budget":150000
},
"Address":[
{
"Street":"Kastanienallee",
"Housenumber":"25",
"City":"Wiesmoor",
"Zip":"26639",
"Country":"Germany"
},
{
"Street":"Storkower Strasse",
"Housenumber":"89",
"City":"Hartenfels",
"Zip":"56244",
"Country":"Germany"
},
46

-- 46 of 114 --

{
"Street":"Sonnenallee",
"Housenumber":"97",
"City":"Augsburg",
"Zip":"86172",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/9387913",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"executive"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-a556-
c32205476e98",
"modelVersion":null
}
}
}
}
]
}
47

-- 47 of 114 --

Your task:
• Create a valid Query API request which loads the same documents as the Original
LIST_TERMINATING_LINKS 1 request.
• Additionally, include the child and link documents to the request response.
• Hints:
◦ Use the has operator to specify the relationship.
◦ Negate one of the has constraints.
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query
LIST_TERMINATING_LINKS 1
{
"jsonrpc":"2.0",
"id":"Query LIST_TERMINATING_LINKS 1",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company"
},
{
"operator":"not",
"operand":{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Contact"
}
}
]
},
"links":{
"relationshipModel":"CompanyContact",
"targetRole":"Company"
},
"paging":{
"pageNumber":0,
"pageSize":100
}
}
}
48

-- 48 of 114 --

}
NOTE By removing the links parameter in the request you can keep the result smaller.
Now, let’s check out another LIST_TERMINATING_LINKS example with the addition of loadOrphans.
Therefore, create another contact document without linking it to a company.
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original
LIST_TERMINATING_LINKS 2
{
"jsonrpc":"2.0",
"id":"Original LIST_TERMINATING_LINKS 2",
"method":"LIST_TERMINATING_LINKS",
"params":{
"relationshipModelName":"CompanyContact",
"terminatingRoleName":"Contact",
"loadOrphans":true
}
}
If you create a new contact document without linking it to a company, you will notice the effect the
loadOrphans option has.
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original LIST_TERMINATING_LINKS 2",
"result":[
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/386e250d-c6c8-4eaa-b44c-
7e9aa521aae5"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/5373d947-0ca3-4ca7-
49

-- 49 of 114 --

af76-fcd9b2f253f6",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"10"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"James",
"LastName":"Baker",
"EmailAddress":"james.baker@gmx.com"
},
"Project":{
"Budget":400000
},
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/386e250d-c6c8-4eaa-b44c-
7e9aa521aae5",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/5373d947-0ca3-4ca7-af76-
fcd9b2f253f6",
"modelVersion":null
}
50

-- 50 of 114 --

}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/7a47a57b-7db5-44e6-ba89-
c78b36ffa247"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/958a8c35-47e1-4478-
943b-f05f7ab44151",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"9"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Julia",
"LastName":"Limhold",
"EmailAddress":"julia.limhold@gmx.com"
},
"Project":{
"Budget":350000
},
"Address":[
{
"Street":"Friedrich-Hirsch-Strasse",
"Housenumber":"110",
"City":"Cologne",
"Zip":"51145",
"Country":"Germany"
}
],
"Phones":[
{
51

-- 51 of 114 --

"PhoneNumber":"+49176/112300421",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/39129301",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/7a47a57b-7db5-44e6-ba89-
c78b36ffa247",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/958a8c35-47e1-4478-943b-
f05f7ab44151",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0"
},
{
"role":"Contact",
52

-- 52 of 114 --

"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/ab6e8222-77ca-41fc-a85d-
3404159d253a"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/4fc930c1-f168-4fb3-
a986-3a6918737f75",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"8"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Olaf",
"LastName":"Schmitz",
"EmailAddress":"olaf.schmitz@gmx.com"
},
"Project":{
"Budget":100000
}
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/ab6e8222-77ca-41fc-a85d-
3404159d253a",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/4fc930c1-f168-4fb3-a986-
3a6918737f75",
"modelVersion":null
}
53

-- 53 of 114 --

}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/5793d501-1c1e-4c41-8fe2-
b8f732cc7305"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/0119190f-9862-40fe-
8fb5-d9f6a0676d62",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"7"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Hans",
"LastName":"Mayer",
"EmailAddress":"hans.mayer@gmx.com"
},
"Project":{
"Budget":250000
},
"Address":[
{
"Street":"Berliner Strasse",
"Housenumber":"33",
"City":"Bonn",
"Zip":"52304",
"Country":"Germany"
},
{
"Street":"Fasanenweg",
"Housenumber":"2",
54

-- 54 of 114 --

"City":"Königswinter",
"Zip":"53420",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/92139129",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/5793d501-1c1e-4c41-8fe2-
b8f732cc7305",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"executive"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/0119190f-9862-40fe-8fb5-
d9f6a0676d62",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
55

-- 55 of 114 --

},
{
"role":"Contact",
"modelName":"ContactPotential_DM",
"docRef":"ContactPotential_DM/964a0945-69d3-4fda-ba6e-
f151fd58e069"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/89e3b518-d657-4bb8-
8737-2f952bb1b8b2",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"6"
},
"document":{
"target":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Pierre",
"LastName":"Miller",
"EmailAddress":"pierre.miller@gmx.com"
},
"Project":{
"Budget":500000
},
"Phones":[
{
"PhoneNumber":"+33176/321939213",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactPotential_DM",
"docRef":"ContactPotential_DM/964a0945-69d3-4fda-ba6e-
f151fd58e069",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"other"
},
"__meta":{
56

-- 56 of 114 --

"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/89e3b518-d657-4bb8-8737-
2f952bb1b8b2",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/5232042c-d259-4a8d-80ce-
7570b455323e"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/e093313d-03ad-428b-
acd0-ca6e493e61f1",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"5"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Miriam",
"LastName":"Miller",
"EmailAddress":"miriam.miller@gmx.com",
"DateOfBirth":"2000-03-02",
"Nationality":"French",
"Gender":"FEMALE",
"CustomerType":"partner"
},
"Project":{
57

-- 57 of 114 --

"Budget":1000000
},
"Address":[
{
"Street":"Rue de Paris",
"Housenumber":"1",
"City":"Lille",
"Zip":"59000",
"Country":"France"
}
],
"Phones":[
{
"PhoneNumber":"+33176/931293801",
"Type":"MOBILE"
},
{
"PhoneNumber":"+4933/10031290123",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/5232042c-d259-4a8d-80ce-
7570b455323e",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"managerial"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/e093313d-03ad-428b-acd0-
ca6e493e61f1",
"modelVersion":null
}
}
}
},
58

-- 58 of 114 --

{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/981d5b5e-a039-4859-883d-
89647cffa0ae"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/bda4773a-3049-4d8a-
8941-041c5c835986",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"4"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Alex",
"LastName":"Baker",
"EmailAddress":"alex.baker@gmx.com",
"DateOfBirth":"1983-12-19",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"partner"
},
"Project":{
"Budget":360000
},
"Address":[
{
"Street":"Waldstrasse",
"Housenumber":"2",
"City":"Cologne",
"Zip":"51145",
"Country":"Germany"
},
{
"Street":"Siemensstrasse",
59

-- 59 of 114 --

"Housenumber":"15",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
},
{
"Street":"Voltastrasse",
"Housenumber":"3",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/0024823824",
"Type":"MOBILE"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/981d5b5e-a039-4859-883d-
89647cffa0ae",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"operational"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/bda4773a-3049-4d8a-8941-
041c5c835986",
"modelVersion":null
}
}
}
},
{
"linkRef":{
60

-- 60 of 114 --

"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/bf34a2d3-9296-46df-a86b-
0995eb4daddf"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/8ccd097e-25f7-4a41-
b57a-2bd32b54e0e1",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"3"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Anna",
"LastName":"Baker",
"EmailAddress":"anna.baker@gmx.com",
"DateOfBirth":"1990-08-06",
"Nationality":"German",
"Gender":"FEMALE",
"CustomerType":"vip"
},
"Project":{
"Budget":950000
},
"Address":[
{
"Street":"Kaiserstrasse",
"Housenumber":"12",
"City":"Berlin",
"Zip":"12392",
"Country":"Germany"
},
{
"Street":"Adenauerstrasse",
"Housenumber":"90",
"City":"Nuremberg",
61

-- 61 of 114 --

"Zip":"90512",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/57292390",
"Type":"MOBILE"
},
{
"PhoneNumber":"+4933/12300412",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/bf34a2d3-9296-46df-a86b-
0995eb4daddf",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"managerial"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/8ccd097e-25f7-4a41-b57a-
2bd32b54e0e1",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
62

-- 62 of 114 --

"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/8b78735d-fb19-4809-b436-
4dbba1030952"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/eb8bfa4b-57db-42e8-
964a-e9a0bdbe9a97",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"2"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Maximilian",
"LastName":"Baker",
"EmailAddress":"maximilian.baker@gmx.com",
"DateOfBirth":"1995-06-19",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"lead"
},
"Project":{
"Budget":300000
},
"Address":[
{
"Street":"Rathenausstrasse",
"Housenumber":"50",
"City":"Fürth",
"Zip":"90702",
"Country":"Germany"
},
{
"Street":"Jenaer Strasse",
"Housenumber":"77",
"City":"Duisburg",
"Zip":"47053",
"Country":"Germany"
}
],
"Phones":[
63

-- 63 of 114 --

{
"PhoneNumber":"+49176/1328139912",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/188123829",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/8b78735d-fb19-4809-b436-
4dbba1030952",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"executive"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/eb8bfa4b-57db-42e8-964a-
e9a0bdbe9a97",
"modelVersion":null
}
}
}
},
{
"linkRef":{
"linkDescriptor":{
"relationshipModel":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
64

-- 64 of 114 --

"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f"
}
],
"linkDocumentDocRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-
a556-c32205476e98",
"predecessorLinkRef":null,
"position":"TOP"
},
"id":"1"
},
"document":{
"target":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Henry",
"LastName":"Baker",
"EmailAddress":"henry.baker@gmx.com",
"DateOfBirth":"1987-05-21",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"lead"
},
"Project":{
"Budget":150000
},
"Address":[
{
"Street":"Kastanienallee",
"Housenumber":"25",
"City":"Wiesmoor",
"Zip":"26639",
"Country":"Germany"
},
{
"Street":"Storkower Strasse",
"Housenumber":"89",
"City":"Hartenfels",
"Zip":"56244",
"Country":"Germany"
},
{
"Street":"Sonnenallee",
"Housenumber":"97",
"City":"Augsburg",
"Zip":"86172",
"Country":"Germany"
}
65

-- 65 of 114 --

],
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/9387913",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f",
"modelVersion":null
}
},
"relationship":{
"id":"__NEW__",
"Root":{
"Role":"executive"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-a556-
c32205476e98",
"modelVersion":null
}
}
}
}
]
}
Your task:
• Create a valid Query API request which loads the same documents as the Original
LIST_TERMINATING_LINKS 2 request.
• Additionally, include the child and link documents to the request response.
• Hints:
66

-- 66 of 114 --

◦ Orphans are documents that are not part of any relationship. Therefore, you can at first
construct a query that retrieves all contact documents that are not assigned to a company.
◦ Extend the previous query request by using an or.
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query
LIST_TERMINATING_LINKS 2
{
"jsonrpc":"2.0",
"id":"Query LIST_TERMINATING_LINKS 2",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"or",
"operands":[
{
"operator":"not",
"operand":{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company"
}
},
{
"operator":"and",
"operands":[
{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company"
},
{
"operator":"not",
"operand":{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Contact"
}
}
]
}
]
},
"links":{
"relationshipModel":"CompanyContact",
"targetRole":"Company"
67

-- 67 of 114 --

},
"paging":{
"pageNumber":0,
"pageSize":100
}
}
}
}
NOTE By removing the links parameter in the request you can keep the result smaller.
LOAD_DOCUMENT_GRAPH
This operation allows constructing document graphs based on a CDM.
Table 6. LOAD_DOCUMENT_GRAPH Parameters
Parameter Description
cdm* Specifies the name of the CDM to construct a document graph.
docRef* Specifies the starting point of the document graph.
path Specifies the starting point of the document graph inside the
CDM.
Legend
* = Required parameter
See the following example for the LOAD_DOCUMENT_GRAPH operation:
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original
LOAD_DOCUMENT_GRAPH
{
"jsonrpc":"2.0",
"id":"Original LOAD_DOCUMENT_GRAPH",
"method":"LOAD_DOCUMENT_GRAPH",
"params":{
"cdm":"Employment_CDM",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-039146727d5f"
}
}
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original LOAD_DOCUMENT_GRAPH",
"result":{
"links":[
68

-- 68 of 114 --

{
"linkId":"1",
"linkDescriptor":{
"relationshipModelName":"CompanyContact",
"entities":[
{
"role":"Company",
"modelName":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd"
},
{
"role":"Contact",
"modelName":"ContactFinal_DM",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f"
}
]
},
"linkDocRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-a556-
c32205476e98"
}
],
"documents":[
{
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-039146727d5f",
"documentModelName":"ContactFinal_DM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Henry",
"LastName":"Baker",
"EmailAddress":"henry.baker@gmx.com",
"DateOfBirth":"1987-05-21",
"Nationality":"German",
"Gender":"MALE",
"CustomerType":"lead"
},
"Project":{
"Budget":150000
},
"Address":[
{
"Street":"Kastanienallee",
"Housenumber":"25",
"City":"Wiesmoor",
"Zip":"26639",
"Country":"Germany"
},
{
69

-- 69 of 114 --

"Street":"Storkower Strasse",
"Housenumber":"89",
"City":"Hartenfels",
"Zip":"56244",
"Country":"Germany"
},
{
"Street":"Sonnenallee",
"Housenumber":"97",
"City":"Augsburg",
"Zip":"86172",
"Country":"Germany"
}
],
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/9387913",
"Type":"WORK"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f",
"modelVersion":null
}
}
},
{
"docRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-a556-
c32205476e98",
"documentModelName":"RoleAdditionalField_DM",
"document":{
"id":"__NEW__",
"Root":{
"Role":"executive"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifiedAt":"2025-07-16T17:50:39",
70

-- 70 of 114 --

"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-a556-
c32205476e98",
"modelVersion":null
}
}
},
{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-8ca9bcc1f0dd",
"documentModelName":"Company_DM",
"document":{
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifiedAt":"2025-07-16T17:50:38",
"modelReference":"Company_DM",
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-8ca9bcc1f0dd",
"modelVersion":null
}
}
}
]
}
}
Your task:
• Create a valid Query API request which loads the same documents as the Original
LOAD_DOCUMENT_GRAPH request.
• Hint: Use the document-graph projection.
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query LOAD_DOCUMENT_GRAPH
{
"jsonrpc":"2.0",
"id":"Query LOAD_DOCUMENT_GRAPH",
"method":"QUERY",
71

-- 71 of 114 --

"params":{
"query":{
"targetDocumentModel":"Employment_CDM",
"projectionName":"document-graph",
"constraint":{
"operator":"exact_match",
"field":"/__meta/docRef",
"value":"ContactFinal_DM/41e180b2-6420-4643-a420-a2db3d8cca9f"
},
"paging":{
"pageNumber":0,
"pageSize":10
}
}
}
}
NOTE
Generally the document graph is a technical construct required by the
Relationship Engine. The recommended approach for retrieving a root document
with corresponding links would be to the use document projection and specifying
the links property.
The document graph can be used to construct CDDs. In the next section, we will check how to map
the corresponding request for CDD related documents and data retrieval.
LIST_CDDS
This operation allows retrieving CDD data.
Table 7. LIST_CDDS Parameters
Parameter Description
cdm* Specifies the CDM name to load the CDDs for.
filter Specifies field-based filters which the documents must satisfy to
match.
sort Specifies the order in which the resulting documents shall be
sorted.
page Specifies the pagination which shall be applied to the search
result.
facets Group documents based on specific constraints. Additionally,
facets allow running statistical calculations on the grouped
document data.
Legend
* = Required parameter
See the following example for the LIST_CDDS operation:
72

-- 72 of 114 --

File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original LIST_CDDS 1
{
"jsonrpc":"2.0",
"id":"Original LIST_CDDS 1",
"method":"LIST_CDDS",
"params":{
"cdm":"Employment_CDM"
}
}
In the example above, every CDD for the Employment_CDM is retrieved from the server. This includes
all employments for the contacts and companies. Each entry contained the complete data for the
corresponding contact, company and role.
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original LIST_CDDS 1",
"result":{
"fullSize":10,
"page":{
"offset":0,
"limit":10
},
"entries":[
{
"docRef":"Employment_CDM/ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Henry",
"LastName":"Baker",
"Gender":"MALE",
"DateOfBirth":"1987-05-21",
"Nationality":"German",
"EmailAddress":"henry.baker@gmx.com",
"CustomerType":"lead"
},
"Project":{
"Budget":150000
},
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
73

-- 73 of 114 --

},
{
"PhoneNumber":"+492203/9387913",
"Type":"WORK"
}
],
"Address":[
{
"Street":"Kastanienallee",
"Housenumber":"25",
"City":"Wiesmoor",
"Zip":"26639",
"Country":"Germany"
},
{
"Street":"Storkower Strasse",
"Housenumber":"89",
"City":"Hartenfels",
"Zip":"56244",
"Country":"Germany"
},
{
"Street":"Sonnenallee",
"Housenumber":"97",
"City":"Augsburg",
"Zip":"86172",
"Country":"Germany"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"executive"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-
a556-c32205476e98",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
74

-- 74 of 114 --

"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactFinal_DM/28b4d32c-6e52-40a9-
8ef4-039146727d5f",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactFinal_DM/5232042c-d259-4a8d-80ce-
7570b455323e",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Miriam",
"LastName":"Miller",
"Gender":"FEMALE",
"DateOfBirth":"2000-03-02",
"Nationality":"French",
"EmailAddress":"miriam.miller@gmx.com",
"CustomerType":"partner"
},
"Project":{
"Budget":1000000
},
"Phones":[
{
"PhoneNumber":"+33176/931293801",
75

-- 75 of 114 --

"Type":"MOBILE"
},
{
"PhoneNumber":"+4933/10031290123",
"Type":"WORK"
}
],
"Address":[
{
"Street":"Rue de Paris",
"Housenumber":"1",
"City":"Lille",
"Zip":"59000",
"Country":"France"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"managerial"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/e093313d-03ad-428b-
acd0-ca6e493e61f1",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
76

-- 76 of 114 --

"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactFinal_DM/5232042c-d259-4a8d-
80ce-7570b455323e",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactFinal_DM/8b78735d-fb19-4809-b436-
4dbba1030952",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Maximilian",
"LastName":"Baker",
"Gender":"MALE",
"DateOfBirth":"1995-06-19",
"Nationality":"German",
"EmailAddress":"maximilian.baker@gmx.com",
"CustomerType":"lead"
},
"Project":{
"Budget":300000
},
"Phones":[
{
"PhoneNumber":"+49176/1328139912",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/188123829",
"Type":"WORK"
}
],
"Address":[
{
"Street":"Rathenausstrasse",
"Housenumber":"50",
"City":"Fürth",
"Zip":"90702",
77

-- 77 of 114 --

"Country":"Germany"
},
{
"Street":"Jenaer Strasse",
"Housenumber":"77",
"City":"Duisburg",
"Zip":"47053",
"Country":"Germany"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"executive"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/eb8bfa4b-57db-42e8-
964a-e9a0bdbe9a97",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactFinal_DM/8b78735d-fb19-4809-
78

-- 78 of 114 --

b436-4dbba1030952",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactFinal_DM/981d5b5e-a039-4859-883d-
89647cffa0ae",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Alex",
"LastName":"Baker",
"Gender":"MALE",
"DateOfBirth":"1983-12-19",
"Nationality":"German",
"EmailAddress":"alex.baker@gmx.com",
"CustomerType":"partner"
},
"Project":{
"Budget":360000
},
"Phones":[
{
"PhoneNumber":"+49176/0024823824",
"Type":"MOBILE"
}
],
"Address":[
{
"Street":"Waldstrasse",
"Housenumber":"2",
"City":"Cologne",
"Zip":"51145",
"Country":"Germany"
},
{
"Street":"Siemensstrasse",
"Housenumber":"15",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
},
{
79

-- 79 of 114 --

"Street":"Voltastrasse",
"Housenumber":"3",
"City":"Cologne",
"Zip":"50667",
"Country":"Germany"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"operational"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/bda4773a-3049-4d8a-
8941-041c5c835986",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactFinal_DM/981d5b5e-a039-4859-
883d-89647cffa0ae",
"modelReference":"Employment_CDM",
"modelVersion":null,
80

-- 80 of 114 --

"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactFinal_DM/bf34a2d3-9296-46df-a86b-
0995eb4daddf",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Anna",
"LastName":"Baker",
"Gender":"FEMALE",
"DateOfBirth":"1990-08-06",
"Nationality":"German",
"EmailAddress":"anna.baker@gmx.com",
"CustomerType":"vip"
},
"Project":{
"Budget":950000
},
"Phones":[
{
"PhoneNumber":"+49176/57292390",
"Type":"MOBILE"
},
{
"PhoneNumber":"+4933/12300412",
"Type":"WORK"
}
],
"Address":[
{
"Street":"Kaiserstrasse",
"Housenumber":"12",
"City":"Berlin",
"Zip":"12392",
"Country":"Germany"
},
{
"Street":"Adenauerstrasse",
"Housenumber":"90",
"City":"Nuremberg",
"Zip":"90512",
"Country":"Germany"
}
81

-- 81 of 114 --

]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"managerial"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/8ccd097e-25f7-4a41-
b57a-2bd32b54e0e1",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactFinal_DM/bf34a2d3-9296-46df-
a86b-0995eb4daddf",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
82

-- 82 of 114 --

},
{
"docRef":"Employment_CDM/ContactPotential_DM/386e250d-c6c8-4eaa-
b44c-7e9aa521aae5",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"James",
"LastName":"Baker",
"EmailAddress":"james.baker@gmx.com"
},
"Project":{
"Budget":400000
},
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"operational"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/5373d947-0ca3-4ca7-
af76-fcd9b2f253f6",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Servicing Good GmbH",
"Website":"servicing-good.com"
},
"Phones":[
{
"PhoneNumber":"+4930/0210301"
}
]
},
"__meta":{
83

-- 83 of 114 --

"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactPotential_DM/386e250d-c6c8-
4eaa-b44c-7e9aa521aae5",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactPotential_DM/5793d501-1c1e-4c41-
8fe2-b8f732cc7305",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Hans",
"LastName":"Mayer",
"EmailAddress":"hans.mayer@gmx.com"
},
"Project":{
"Budget":250000
},
"Phones":[
{
"PhoneNumber":"+49176/92139129",
"Type":"MOBILE"
}
],
"Address":[
{
"Street":"Berliner Strasse",
"Housenumber":"33",
"City":"Bonn",
"Zip":"52304",
"Country":"Germany"
},
84

-- 84 of 114 --

{
"Street":"Fasanenweg",
"Housenumber":"2",
"City":"Königswinter",
"Zip":"53420",
"Country":"Germany"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"executive"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/0119190f-9862-40fe-
8fb5-d9f6a0676d62",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactPotential_DM/5793d501-1c1e-
4c41-8fe2-b8f732cc7305",
"modelReference":"Employment_CDM",
85

-- 85 of 114 --

"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactPotential_DM/7a47a57b-7db5-44e6-
ba89-c78b36ffa247",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Julia",
"LastName":"Limhold",
"EmailAddress":"julia.limhold@gmx.com"
},
"Project":{
"Budget":350000
},
"Phones":[
{
"PhoneNumber":"+49176/112300421",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/39129301",
"Type":"WORK"
}
],
"Address":[
{
"Street":"Friedrich-Hirsch-Strasse",
"Housenumber":"110",
"City":"Cologne",
"Zip":"51145",
"Country":"Germany"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"operational"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/958a8c35-47e1-4478-
943b-f05f7ab44151",
86

-- 86 of 114 --

"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Servicing Good GmbH",
"Website":"servicing-good.com"
},
"Phones":[
{
"PhoneNumber":"+4930/0210301"
}
]
},
"__meta":{
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactPotential_DM/7a47a57b-7db5-
44e6-ba89-c78b36ffa247",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactPotential_DM/964a0945-69d3-4fda-
ba6e-f151fd58e069",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Pierre",
87

-- 87 of 114 --

"LastName":"Miller",
"EmailAddress":"pierre.miller@gmx.com"
},
"Project":{
"Budget":500000
},
"Phones":[
{
"PhoneNumber":"+33176/321939213",
"Type":"WORK"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"other"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/89e3b518-d657-4bb8-
8737-2f952bb1b8b2",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
88

-- 88 of 114 --

"__meta":{
"docRef":"Employment_CDM/ContactPotential_DM/964a0945-69d3-
4fda-ba6e-f151fd58e069",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactPotential_DM/ab6e8222-77ca-41fc-
a85d-3404159d253a",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Olaf",
"LastName":"Schmitz",
"EmailAddress":"olaf.schmitz@gmx.com"
},
"Project":{
"Budget":100000
}
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"operational"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/4fc930c1-f168-4fb3-
a986-3a6918737f75",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Servicing Good GmbH",
"Website":"servicing-good.com"
},
"Phones":[
{
89

-- 89 of 114 --

"PhoneNumber":"+4930/0210301"
}
]
},
"__meta":{
"docRef":"Company_DM/e0aa4090-10f4-49f8-9a04-
b0ceda37edb0",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactPotential_DM/ab6e8222-77ca-
41fc-a85d-3404159d253a",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
}
]
}
}
Your task:
• Create a valid Query API request which loads the same documents as the Original LIST_CDDS 1
request.
• Hint: Use the cdd projection.
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query LIST_CDDS 1
{
"jsonrpc":"2.0",
"id":"Query LIST_CDDS 1",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Employment_CDM",
"projectionName":"cdd",
"paging":{
"pageNumber":0,
90

-- 90 of 114 --

"pageSize":10
}
}
}
}
Let’s say we are interested in all employments that are referring to a contact with the "executive"
role. The following example contains an additional filter for the CDD.
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original LIST_CDDS 2
{
"jsonrpc":"2.0",
"id":"Original LIST_CDDS 2",
"method":"LIST_CDDS",
"params":{
"cdm":"Employment_CDM",
"filter":{
"filters":[
"CompanyContact.relationship.Root.Role:executive"
]
}
}
}
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original LIST_CDDS 2",
"result":{
"fullSize":3,
"page":{
"offset":0,
"limit":10
},
"entries":[
{
"docRef":"Employment_CDM/ContactFinal_DM/28b4d32c-6e52-40a9-8ef4-
039146727d5f",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Henry",
"LastName":"Baker",
"Gender":"MALE",
"DateOfBirth":"1987-05-21",
91

-- 91 of 114 --

"Nationality":"German",
"EmailAddress":"henry.baker@gmx.com",
"CustomerType":"lead"
},
"Project":{
"Budget":150000
},
"Phones":[
{
"PhoneNumber":"+49176/8821390213",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/9387913",
"Type":"WORK"
}
],
"Address":[
{
"Street":"Kastanienallee",
"Housenumber":"25",
"City":"Wiesmoor",
"Zip":"26639",
"Country":"Germany"
},
{
"Street":"Storkower Strasse",
"Housenumber":"89",
"City":"Hartenfels",
"Zip":"56244",
"Country":"Germany"
},
{
"Street":"Sonnenallee",
"Housenumber":"97",
"City":"Augsburg",
"Zip":"86172",
"Country":"Germany"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"executive"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/24cbe98a-8a9f-4925-
a556-c32205476e98",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
92

-- 92 of 114 --

"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactFinal_DM/28b4d32c-6e52-40a9-
8ef4-039146727d5f",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactFinal_DM/8b78735d-fb19-4809-b436-
4dbba1030952",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"FINAL",
"PersonalData":{
"FirstName":"Maximilian",
"LastName":"Baker",
"Gender":"MALE",
93

-- 93 of 114 --

"DateOfBirth":"1995-06-19",
"Nationality":"German",
"EmailAddress":"maximilian.baker@gmx.com",
"CustomerType":"lead"
},
"Project":{
"Budget":300000
},
"Phones":[
{
"PhoneNumber":"+49176/1328139912",
"Type":"MOBILE"
},
{
"PhoneNumber":"+492203/188123829",
"Type":"WORK"
}
],
"Address":[
{
"Street":"Rathenausstrasse",
"Housenumber":"50",
"City":"Fürth",
"Zip":"90702",
"Country":"Germany"
},
{
"Street":"Jenaer Strasse",
"Housenumber":"77",
"City":"Duisburg",
"Zip":"47053",
"Country":"Germany"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"executive"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/eb8bfa4b-57db-42e8-
964a-e9a0bdbe9a97",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
94

-- 94 of 114 --

"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactFinal_DM/8b78735d-fb19-4809-
b436-4dbba1030952",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
},
{
"docRef":"Employment_CDM/ContactPotential_DM/5793d501-1c1e-4c41-
8fe2-b8f732cc7305",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"ContactType":"POTENTIAL",
"PersonalData":{
"FirstName":"Hans",
"LastName":"Mayer",
"EmailAddress":"hans.mayer@gmx.com"
},
"Project":{
"Budget":250000
},
"Phones":[
{
95

-- 95 of 114 --

"PhoneNumber":"+49176/92139129",
"Type":"MOBILE"
}
],
"Address":[
{
"Street":"Berliner Strasse",
"Housenumber":"33",
"City":"Bonn",
"Zip":"52304",
"Country":"Germany"
},
{
"Street":"Fasanenweg",
"Housenumber":"2",
"City":"Königswinter",
"Zip":"53420",
"Country":"Germany"
}
]
},
"CompanyContact":{
"relationship":{
"Root":{
"Role":"executive"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/0119190f-9862-40fe-
8fb5-d9f6a0676d62",
"modelReference":"RoleAdditionalField_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:39",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:39"
}
},
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Website":"trading-good.com"
},
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"__meta":{
"docRef":"Company_DM/2c60bdca-69f9-40e0-b1a8-
8ca9bcc1f0dd",
96

-- 96 of 114 --

"modelReference":"Company_DM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
},
"__meta":{
"docRef":"Employment_CDM/ContactPotential_DM/5793d501-1c1e-
4c41-8fe2-b8f732cc7305",
"modelReference":"Employment_CDM",
"modelVersion":null,
"creator":"superUser",
"createdAt":"2025-07-16T17:50:38",
"modifier":"superUser",
"modifiedAt":"2025-07-16T17:50:38"
}
}
}
]
}
}
Your task:
• Create a valid Query API request which loads the same documents as the Original LIST_CDDS 2
request.
• Hints:
◦ Use the cdd projection.
◦ Use the has operator with the linkDocumentConstraint parameter.
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query LIST_CDDS 2
{
"jsonrpc":"2.0",
"id":"Query LIST_CDDS 2",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Employment_CDM",
"projectionName":"cdd",
"constraint":{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company",
"linkDocumentConstraint":{
"operator":"exact_match",
97

-- 97 of 114 --

"field":"/Root/Role",
"value":"executive"
}
},
"paging":{
"pageNumber":0,
"pageSize":10
}
}
}
}
Aggregations
Aggregations, also called faceted search, allow data grouping and processing by using dedicated
functions.
The following search facets were available in the old API:
Table 8. Search Facets
Facet Description
term Allows grouping search results by a string Document Model field. Usually used
to group by specific categories.
range Allows grouping search results by a number Document Model field. This can
be used for creating e.g. histograms.
statistics Allows processing all documents to retrieve aggregated values. Available
functions are: max, min, sum and avg.
Let’s have a look at the different kinds of aggregations and how we can map them to the new Query
API.
Term Facet
A term facet allows to group document data by specifying a Document Model field. This field will
define how to group the documents. The response will represent these groups as buckets. Each
bucket contains the value of the corresponding Document Model field and the count.
See the following example for the term facet:
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original Aggregation term 1
{
"id":"Original Aggregation term 1",
"jsonrpc":"2.0",
"method":"LIST_DOCUMENTS",
"params":{
"documentModelName":"Contact_DM",
"facets":[
98

-- 98 of 114 --

{
"id":"term_nationality",
"type":"term",
"field":"Contact.PersonalData.Nationality"
}
]
}
}
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original Aggregation term 1",
"result":{
"fullSize":10,
"page":{
"offset":0,
"limit":10
},
"entries":[...],
"facets":{
"term_nationality":{
"buckets":[
{
"value":"German",
"count":4
},
{
"value":"French",
"count":1
}
],
"offset":0,
"limit":10,
"fullSize":2
}
}
}
}
Our term_nationality facet contains two buckets for each nationality, including their count.
Your task:
• Create a valid Query API request which loads the same data as the Original Aggregation term
request.
• Hint: Use the http://localhost:8081/api/aggregation endpoint.
▼ Click to see solution
99

-- 99 of 114 --

File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query Aggregation term 1
{
"targetDocumentModel":"Contact_DM",
"aggregation":{
"aggregations":[
{
"function":"count",
"field":"/Contact/PersonalData/EmailAddress"
}
],
"group":[
{
"field":"/Contact/PersonalData/Nationality"
}
]
}
}
This response contains one additional null group that groups all documents which do not have
the nationality field set. In our case, reflecting all potential contact documents that do not have a
nationality field.
NOTE
The old API does allow defining multiple facets and providing the result for them in
one request, as shown in the following example.
It is not possible with the Query API to achieve the same behavior in only one
request. Multiple aggregation requests have to be created to receive the same
structured data.
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original Aggregation term 2
{
"id":"Original Aggregation term 2",
"jsonrpc":"2.0",
"method":"LIST_DOCUMENTS",
"params":{
"documentModelName":"Contact_DM",
"facets":[
{
"id":"term_nationality",
"type":"term",
"field":"Contact.PersonalData.Nationality"
},
{
"id":"term_family",
"type":"term",
"field":"Contact.PersonalData.LastName"
},
100

-- 100 of 114 --

{
"id":"avg_project_budget",
"type":"avg",
"field":"Contact.Project.Budget"
}
]
}
}
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original Aggregation term 2",
"result":{
"fullSize":10,
"page":{
"offset":0,
"limit":10
},
"entries":[...],
"facets":{
"term_nationality":{
"buckets":[
{
"value":"German",
"count":4
},
{
"value":"French",
"count":1
}
],
"offset":0,
"limit":10,
"fullSize":2
},
"avg_project_budget":436000,
"term_family":{
"buckets":[
{
"value":"Baker",
"count":5
},
{
"value":"Miller",
"count":2
},
{
"value":"Limhold",
101

-- 101 of 114 --

"count":1
},
{
"value":"Mayer",
"count":1
},
{
"value":"Schmitz",
"count":1
}
],
"offset":0,
"limit":10,
"fullSize":5
}
}
}
}
Range Facet
The range facet allows to group data based on specific values of a number field. It is possible to
create histograms with defined block sizes.
See the following example for the range facet:
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original Aggregation range
{
"id":"Original Aggregation range",
"jsonrpc":"2.0",
"method":"LIST_DOCUMENTS",
"params":{
"documentModelName":"Contact_DM",
"facets":[
{
"id":"range_project_budget",
"type":"range",
"field":"Contact.Project.Budget",
"start":200000,
"end":1000000,
"gap":100000
}
]
}
}
In the example above, the document data is grouped by the project budget field. All values that are
greater than 200,000 and less than 1,000,000 are included in the aggregation result. The documents
102

-- 102 of 114 --

are grouped in buckets with the size of each being 100,000.
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original Aggregation range",
"result":{
"fullSize":10,
"page":{
"offset":0,
"limit":10
},
"entries":[...],
"facets":{
"range_project_budget":{
"buckets":[
{
"value":200000,
"count":1
},
{
"value":300000,
"count":3
},
{
"value":400000,
"count":1
},
{
"value":500000,
"count":1
},
{
"value":900000,
"count":1
}
],
"offset":-1,
"limit":10,
"fullSize":-1
}
}
}
}
Five buckets are provided as a result of our facet request. Three contacts have a project budget that
exceeds 300,000 and is less than 400,000. Therefore, the corresponding bucket contains the "count":
3.
103

-- 103 of 114 --

Your task:
• Create a valid Query API request which loads the same data as the Original Aggregation range
request.
• Hints:
◦ You have to define a proper constraint to filter the corresponding range values. Therefore,
create a regular constraint with the double_range operator.
◦ There is no equivalent for the gaps property of the old facet API. Each of your buckets should
have the count of one item.
◦ Use the http://localhost:8081/api/v2/rpc endpoint. The http://localhost:8081/api/
aggregation endpoint does not allow defining constraints.
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query Aggregation range
{
"id":"Query Aggregation range",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"double_range",
"field":"/Contact/Project/Budget",
"from":200000,
"to":999999
},
"aggregation":{
"aggregations":[
{
"function":"count",
"field":"/Contact/PersonalData/EmailAddress"
}
],
"group":[
{
"field":"/Contact/Project/Budget"
}
]
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
104

-- 104 of 114 --

}
Statistical Facet: Max
The max function of the statistical facet allows processing all documents to retrieve the maximum
value.
See the following example for the max function.
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original Aggregation max
{
"id":"Original Aggregation max",
"jsonrpc":"2.0",
"method":"LIST_DOCUMENTS",
"params":{
"documentModelName":"Contact_DM",
"filter":{
"filters":[
"Contact.PersonalData.LastName:Baker"
]
},
"facets":[
{
"id":"max_project_budget_baker",
"type":"max",
"field":"Contact.Project.Budget"
}
]
}
}
In the example above, we are receiving the maximum project budget for contacts with last name
"Baker".
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original Aggregation max",
"result":{
"fullSize":5,
"page":{
"offset":0,
"limit":10
},
"entries":[...],
"facets":{
"max_project_budget_baker":950000
105

-- 105 of 114 --

}
}
}
Your task:
• Create a valid Query API request which loads the same data as the Original Aggregation max
request.
• Hints:
◦ You have to define a proper constraint to filter the corresponding last names. Therefore,
create a regular constraint with the exact_match operator. Alternatively, you could also use
the simple_search operator with the specific last name field, which is however less
performant.
◦ Use the http://localhost:8081/api/v2/rpc endpoint. The http://localhost:8081/api/
aggregation endpoint does not allow defining constraints.
◦ Group the data by the last name field.
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query Aggregation max
{
"id":"Query Aggregation max",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Baker"
},
"aggregation":{
"aggregations":[
{
"function":"max",
"field":"/Contact/Project/Budget"
}
],
"group":[
{
"field":"/Contact/PersonalData/LastName"
}
]
},
"paging":{
"pageSize":10,
106

-- 106 of 114 --

"pageNumber":0
}
}
}
}
Statistical Facet: Min
The min function of the statistical facet allows processing all documents to retrieve the minimum
value.
See the following example for the min function.
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original Aggregation min
{
"id":"Original Aggregation min",
"jsonrpc":"2.0",
"method":"LIST_DOCUMENTS",
"params":{
"documentModelName":"Contact_DM",
"filter":{
"filters":[
"-Contact.PersonalData.LastName:Baker"
]
},
"facets":[
{
"id":"min_project_budget_baker",
"type":"min",
"field":"Contact.Project.Budget"
}
]
}
}
In the example above, we are receiving the minimum project budget for contacts with last name
not being "Baker".
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original Aggregation min",
"result":{
"fullSize":5,
"page":{
"offset":0,
"limit":10
107

-- 107 of 114 --

},
"entries":[...],
"facets":{
"min_project_budget_baker":100000
}
}
}
Your task:
• Create a valid Query API request which loads the same data as the Original Aggregation min
request.
• Hints:
◦ You have to define a proper constraint to filter the corresponding last names. Therefore,
create a regular constraint with the exact_match operator.
◦ Use the http://localhost:8081/api/v2/rpc endpoint. The http://localhost:8081/api/
aggregation endpoint does not allow defining constraints.
◦ Group the data by the metadata field "modelReference".
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query Aggregation min
{
"id":"Query Aggregation min",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"not",
"operand":{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Baker"
}
},
"aggregation":{
"aggregations":[
{
"function":"min",
"field":"/Contact/Project/Budget"
}
],
"group":[
{
"field":"/__meta/modelReference"
108

-- 108 of 114 --

}
]
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
Grouping by the /__meta/modelReference field calculates the result in two field values for
"ContactFinal_DM" and "ContactPotential_DM". You can check the change in behavior by using
the /Contact/PersonalData/LastName field instead. By applying this, you will receive the minimum
value for each last name.
Statistical Facet: Sum
The sum function of the statistical facet allows processing all documents to retrieve a cumulative
value.
See the following example for the sum function.
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original Aggregation sum
{
"id":"Original Aggregation sum",
"jsonrpc":"2.0",
"method":"LIST_DOCUMENTS",
"params":{
"documentModelName":"Contact_DM",
"filter":{
"filters":[
"Contact.PersonalData.Nationality:German"
]
},
"facets":[
{
"id":"total_project_budget",
"type":"sum",
"field":"Contact.Project.Budget"
}
]
}
}
In the example above, we are receiving the cumulative project budget for contacts with German
nationality.
109

-- 109 of 114 --

▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original Aggregation sum",
"result":{
"fullSize":4,
"page":{
"offset":0,
"limit":10
},
"entries":[...],
"facets":{
"total_project_budget":1760000
}
}
}
Your task:
• Create a valid Query API request which loads the same data as the Original Aggregation sum
request.
• Hints:
◦ You have to define a proper constraint to filter the corresponding nationality. Therefore,
create a regular constraint with the exact_match operator. Alternatively, you could also use
the simple_search operator with the specific nationality field.
◦ Use the http://localhost:8081/api/v2/rpc endpoint. The http://localhost:8081/api/
aggregation endpoint does not allow defining constraints.
◦ Group the data by the metadata field "modelReference".
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query Aggregation sum
{
"id":"Query Aggregation sum",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"exact_match",
"field":"/Contact/PersonalData/Nationality",
"value":"German"
},
"aggregation":{
"aggregations":[
110

-- 110 of 114 --

{
"function":"sum",
"field":"/Contact/Project/Budget"
}
],
"group":[
{
"field":"/__meta/modelReference"
}
]
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
Statistical Facet: Avg
The avg function of the statistical facet allows processing all documents to retrieve an average
value.
See the following example for the avg function.
File: bruno/task3/collection-task3-start.json > Task 3 - Migration > Original Aggregation avg
{
"id":"Original Aggregation avg",
"jsonrpc":"2.0",
"method":"LIST_DOCUMENTS",
"params":{
"documentModelName":"Contact_DM",
"filter":{
"filters":[
"Contact.PersonalData.LastName:(Baker OR Miller)"
]
},
"facets":[
{
"id":"avg_project_budget",
"type":"avg",
"field":"Contact.Project.Budget"
}
]
}
}
111

-- 111 of 114 --

In the example above, we are receiving the cumulative project budget for contacts with the last
name "Baker" or "Miller".
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"Original Aggregation avg",
"result":{
"fullSize":7,
"page":{
"offset":0,
"limit":10
},
"entries":[...],
"facets":{
"avg_project_budget":522857.14
}
}
}
Your task:
• Create a valid Query API request which loads the same data as the Original Aggregation avg
request.
• Hints:
◦ You have to define a proper constraint to filter the corresponding last names. Therefore,
create a regular constraint with the logical or and field-aware exact_match operators.
◦ Use the http://localhost:8081/api/v2/rpc endpoint. The http://localhost:8081/api/
aggregation endpoint does not allow defining constraints.
◦ Group the data by the metadata field "modelReference".
▼ Click to see solution
File: bruno/task3/collection-task3-end.json > Task 3 - Migration > Query Aggregation avg
{
"id":"Query Aggregation avg",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"or",
"operands":[
{
"operator":"exact_match",
112

-- 112 of 114 --

"field":"/Contact/PersonalData/LastName",
"value":"Baker"
},
{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Miller"
}
]
},
"aggregation":{
"aggregations":[
{
"function":"avg",
"field":"/Contact/Project/Budget"
}
],
"group":[
{
"field":"/__meta/modelReference"
}
]
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
Model Changes
If you are interested in the modeling-related changes and effects, then check out Data Services >
Query API > Model-ability.
Authorization
Authorization is automatically applied when using the Query API.
If you are interested in the current state of the authorization in the Query API, check out Data
Services > Query API > Authorization.
The following applies for the authorization of the Query API:
• The Query authorization scope is used for all data read functionalities. This scope can be used to
define complex authorization definition rules to restrict access to specific document data.
• The Document List and Document Read scopes are replaced by the new Query scope.
113

-- 113 of 114 --

• The roles property of Document Models is used for resolving user permission to document data.
Dropped Services and Technologies
Data Services uses PostgreSQL features to implement performant and efficient search mechanisms.
The original API for document data retrieval used several tools to achieve searching, filtering and
sorting. These are no longer required because Data Services implemented those mechanics directly
via PostgreSQL.
The following technologies were removed from the technology stack of Data Services:
Table 9. Dropped Technologies
Technology Explanation
Solr Data Services implemented indexing and search mechanisms directly and Solr
is no longer required. Therefore, this can be completely removed from the
project infrastructure as long as it is not specifically necessary for any other
functionality.
JMS (e.g. Artemis,
ActiveMQ)
If the A12 Workflows component is not used, any JMS implementation can be
removed from the project infrastructure. Data Services does not require JMS
anymore, but Workflows still does.
Database (Oracle,
H2)
Oracle as well as H2 databases are not supported anymore. Data Services
provides embedded in-memory and in-file PostgreSQL configurations for local
development.
Conclusion
In this task, we migrated RPC requests for all data retrieval-related operations of the old API. These
were mapped to their Query API counterparts together with checking out additional possibilities.
If something does not seem right, or you got stuck at any point, you can just check out 2025.06-
ext5/query/task-3-end to see differences between both implementations.
Now that you have completed the Query API tutorial, we would also really appreciate your
feedback. If you have any ideas or wishes for additional tasks or tutorials, you can let us know
there.
« Task 2: Discovering Queries
114

-- 114 of 114 --

