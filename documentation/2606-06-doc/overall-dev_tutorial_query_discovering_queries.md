# overall dev_tutorial_query_discovering_queries

Task 2 - Discovering Queries
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
You can check out the tag 2025.06-ext5/query/task-2-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/query/task-2-end to see how
your requests differ from the solution.
Use-Case
In this task, we will go through each Query API feature and create various query requests. These
requests will cover the different parameters and operators.
This task will showcase how to retrieve specific datasets and leverage the new API to exactly get the
data we need.
End Result
Upon finishing this task, you will know:
• How to create your own query requests by using
◦ all query operators, with
◦ different options to receive fine-grained results,
◦ and only receive the data that you are interested in.
Step-by-Step Instructions
In the following sections, we are going to discover all kinds of query operators and the different
options to tinker with them.
1

-- 1 of 54 --

NOTE
After you have checked out the start tag for this task you should import the Bruno
Collection file bruno/task2/collection-task2-start.json in Bruno. If it is the first
time that you are importing the collection, follow Tutorials > Query API > First Steps
> First Query.
Field-Aware Operators
The field-aware operators are used to create queries based on Document Model fields. If a
constraint with such an operator is satisfied, the respective document will be added to the result
entries.
exact_match
The exact_match operator allows filtering documents based on a specific Document Model field,
whose value has to match fully.
Table 1. exact_match Parameters
Parameter Description
field* Specifies the Document Model field by its path, which the query
should be filtered for.
value* Specifies the value, which the document data must fully match.
caseSensitive Specifies whether the textual matching shall be case-sensitive or
not. Options are true and false, the default is true.
Legend
* = Required parameter
See the following example for the exact_match operator:
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > exact_match
Example 1
{
"id":"exact_match Example 1",
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
"paging":{
"pageSize":10,
2

-- 2 of 54 --

"pageNumber":0
}
}
}
}
In the example above, the contact documents of the Contact_DM Document Model are filtered by the
field "/Contact/PersonalData/LastName" with the value "Baker". When sending such a query in a
request with our existing customer contact documents we will receive five documents as a result.
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"exact_match Example 1",
"result":{
"fullSize":5,
"page":{
"pageNumber":0,
"pageSize":10
},
"entries":[
{
"docRef":"ContactPotential_DM/e58ddd56-b749-426f-96f7-d81453f8a872",
"document":{
"Contact":{
"ContactType":"POTENTIAL",
"Project":{
"Budget":400000
},
"PersonalData":{
"FirstName":"James",
"LastName":"Baker",
"EmailAddress":"james.baker@gmx.com"
},
"Phones":[
{
"Type":"MOBILE",
"PhoneNumber":"+49176/8821390213"
}
]
},
"__meta":{
"createdAt":"2025-06-14T09:42:05",
"creator":"superUser",
"docRef":"ContactPotential_DM/e58ddd56-b749-426f-96f7-
d81453f8a872",
"modelReference":"ContactPotential_DM",
"modelVersion":null,
"modifiedAt":"2025-06-14T09:42:05",
3

-- 3 of 54 --

"modifier":"superUser"
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactPotential_DM"
},
{
"docRef":"ContactFinal_DM/2235b54f-ee90-43cd-858e-957f9f783891",
"document":{
"Contact":{
"ContactType":"FINAL",
"Project":{
"Budget":950000
},
"PersonalData":{
"DateOfBirth":"1990-08-06",
"FirstName":"Anna",
"Gender":"FEMALE",
"LastName":"Baker",
"CustomerType":"vip",
"EmailAddress":"anna.baker@gmx.com",
"Nationality":"German"
},
"Phones":[
{
"Type":"MOBILE",
"PhoneNumber":"+49176/57292390"
},
{
"Type":"WORK",
"PhoneNumber":"+4933/12300412"
}
],
"Address":[
{
"Zip":"12392",
"Country":"Germany",
"Street":"Kaiserstrasse",
"City":"Berlin",
"Housenumber":"12"
},
{
"Zip":"90512",
"Country":"Germany",
"Street":"Adenauerstrasse",
"City":"Nuremberg",
"Housenumber":"90"
}
]
},
4

-- 4 of 54 --

"__meta":{
"createdAt":"2025-06-14T09:42:05",
"creator":"superUser",
"docRef":"ContactFinal_DM/2235b54f-ee90-43cd-858e-
957f9f783891",
"modelReference":"ContactFinal_DM",
"modelVersion":null,
"modifiedAt":"2025-06-14T09:42:05",
"modifier":"superUser"
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
},
{
"docRef":"ContactFinal_DM/568e6abd-31d6-411e-95c6-a581a0d156ab",
"document":{
"Contact":{
"ContactType":"FINAL",
"Project":{
"Budget":360000
},
"PersonalData":{
"DateOfBirth":"1983-12-19",
"FirstName":"Alex",
"Gender":"MALE",
"LastName":"Baker",
"CustomerType":"partner",
"EmailAddress":"alex.baker@gmx.com",
"Nationality":"German"
},
"Phones":[
{
"Type":"MOBILE",
"PhoneNumber":"+49176/0024823824"
}
],
"Address":[
{
"Zip":"51145",
"Country":"Germany",
"Street":"Waldstrasse",
"City":"Cologne",
"Housenumber":"2"
},
{
"Zip":"50667",
"Country":"Germany",
"Street":"Siemensstrasse",
"City":"Cologne",
5

-- 5 of 54 --

"Housenumber":"15"
},
{
"Zip":"50667",
"Country":"Germany",
"Street":"Voltastrasse",
"City":"Cologne",
"Housenumber":"3"
}
]
},
"__meta":{
"createdAt":"2025-06-14T09:42:05",
"creator":"superUser",
"docRef":"ContactFinal_DM/568e6abd-31d6-411e-95c6-
a581a0d156ab",
"modelReference":"ContactFinal_DM",
"modelVersion":null,
"modifiedAt":"2025-06-14T09:42:05",
"modifier":"superUser"
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
},
{
"docRef":"ContactFinal_DM/699867c7-ce9e-475e-9386-3c4d690168d8",
"document":{
"Contact":{
"ContactType":"FINAL",
"Project":{
"Budget":300000
},
"PersonalData":{
"DateOfBirth":"1995-06-19",
"FirstName":"Maximilian",
"Gender":"MALE",
"LastName":"Baker",
"CustomerType":"lead",
"EmailAddress":"maximilian.baker@gmx.com",
"Nationality":"German"
},
"Phones":[
{
"Type":"MOBILE",
"PhoneNumber":"+49176/1328139912"
},
{
"Type":"WORK",
"PhoneNumber":"+492203/188123829"
6

-- 6 of 54 --

}
],
"Address":[
{
"Zip":"90702",
"Country":"Germany",
"Street":"Rathenausstrasse",
"City":"Fürth",
"Housenumber":"50"
},
{
"Zip":"47053",
"Country":"Germany",
"Street":"Jenaer Strasse",
"City":"Duisburg",
"Housenumber":"77"
}
]
},
"__meta":{
"createdAt":"2025-06-14T09:42:05",
"creator":"superUser",
"docRef":"ContactFinal_DM/699867c7-ce9e-475e-9386-
3c4d690168d8",
"modelReference":"ContactFinal_DM",
"modelVersion":null,
"modifiedAt":"2025-06-14T09:42:05",
"modifier":"superUser"
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
},
{
"docRef":"ContactFinal_DM/b30e870c-7b5f-45a3-9d76-566e7ead9fe9",
"document":{
"Contact":{
"ContactType":"FINAL",
"Project":{
"Budget":150000
},
"PersonalData":{
"DateOfBirth":"1987-05-21",
"FirstName":"Henry",
"Gender":"MALE",
"LastName":"Baker",
"CustomerType":"lead",
"EmailAddress":"henry.baker@gmx.com",
"Nationality":"German"
},
7

-- 7 of 54 --

"Phones":[
{
"Type":"MOBILE",
"PhoneNumber":"+49176/8821390213"
},
{
"Type":"WORK",
"PhoneNumber":"+492203/9387913"
}
],
"Address":[
{
"Zip":"26639",
"Country":"Germany",
"Street":"Kastanienallee",
"City":"Wiesmoor",
"Housenumber":"25"
},
{
"Zip":"56244",
"Country":"Germany",
"Street":"Storkower Strasse",
"City":"Hartenfels",
"Housenumber":"89"
},
{
"Zip":"86172",
"Country":"Germany",
"Street":"Sonnenallee",
"City":"Augsburg",
"Housenumber":"97"
}
]
},
"__meta":{
"createdAt":"2025-06-14T09:42:04",
"creator":"superUser",
"docRef":"ContactFinal_DM/b30e870c-7b5f-45a3-9d76-
566e7ead9fe9",
"modelReference":"ContactFinal_DM",
"modelVersion":null,
"modifiedAt":"2025-06-14T09:42:04",
"modifier":"superUser"
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
}
],
"links":[
8

-- 8 of 54 --

],
"otherResults":{
}
}
}
The query result contains a lot of data, especially for each document entry. To reduce the amount of
loaded data, we can use the query parameter fields to project only the data we are interested in.
Let’s say we want to only receive the contact data for the first name, last name and the email
address.
See the following example for the exact_match operator modified with the fields parameter:
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > exact_match
Example 2
{
"id":"exact_match Example 2",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"fields":[
"/Contact/PersonalData/FirstName",
"/Contact/PersonalData/LastName",
"/Contact/PersonalData/EmailAddress"
],
"constraint":{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Baker"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
▼ Click to see request result
{
"jsonrpc":"2.0",
"id":"exact_match Example 2",
9

-- 9 of 54 --

"result":{
"fullSize":5,
"page":{
"pageNumber":0,
"pageSize":10
},
"entries":[
{
"docRef":"ContactPotential_DM/e58ddd56-b749-426f-96f7-d81453f8a872",
"document":{
"Contact":{
"PersonalData":{
"LastName":"Baker",
"FirstName":"James",
"EmailAddress":"james.baker@gmx.com"
}
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactPotential_DM"
},
{
"docRef":"ContactFinal_DM/2235b54f-ee90-43cd-858e-957f9f783891",
"document":{
"Contact":{
"PersonalData":{
"LastName":"Baker",
"FirstName":"Anna",
"EmailAddress":"anna.baker@gmx.com"
}
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
},
{
"docRef":"ContactFinal_DM/568e6abd-31d6-411e-95c6-a581a0d156ab",
"document":{
"Contact":{
"PersonalData":{
"LastName":"Baker",
"FirstName":"Alex",
"EmailAddress":"alex.baker@gmx.com"
}
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
10

-- 10 of 54 --

},
{
"docRef":"ContactFinal_DM/699867c7-ce9e-475e-9386-3c4d690168d8",
"document":{
"Contact":{
"PersonalData":{
"LastName":"Baker",
"FirstName":"Maximilian",
"EmailAddress":"maximilian.baker@gmx.com"
}
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
},
{
"docRef":"ContactFinal_DM/b30e870c-7b5f-45a3-9d76-566e7ead9fe9",
"document":{
"Contact":{
"PersonalData":{
"LastName":"Baker",
"FirstName":"Henry",
"EmailAddress":"henry.baker@gmx.com"
}
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
}
],
"links":[
],
"otherResults":{
}
}
}
You can see that the result entries for the documents only contain the specified field values which
we included in the fields list and omit the rest of the Document Model fields.
Now that you are aware of the exact_match operator with the fields option, you can go ahead and
create your own filter.
Your task:
• Filter for "German" contacts.
• Only the following fields should be included in the query result entries:
11

-- 11 of 54 --

◦ First name
◦ Last name
◦ Email address
◦ Phone numbers
◦ Creator (metadata)
◦ Created at (metadata)
• Hint: You should receive the following number of result entries: Four ROOT documents.
TIP
You can check the necessary Document Model paths in the SME by opening the
tutorial workspace under import/models. The available metadata fields can be found in
e.g. the request result of the "exact_match Example 1" query.
▼ Click to see solution
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > exact_match Task
{
"id":"exact_match Task",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"fields":[
"/Contact/PersonalData/FirstName",
"/Contact/PersonalData/LastName",
"/Contact/PersonalData/EmailAddress",
"/Contact/Phones/PhoneNumber",
"/__meta/creator",
"/__meta/createdAt"
],
"constraint":{
"operator":"exact_match",
"field":"/Contact/PersonalData/Nationality",
"value":"German"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
12

-- 12 of 54 --

double_range
The double_range operator allows filtering document data based on a number field. Only values that
satisfy the range constraint are matched.
Table 2. double_range Parameters
Parameter Description
field* Specifies the number field to create a range filter for.
from Specifies the lower bound for the number range.
to Specifies the upper bound for the number range.
Either the from and/or to have to be specified.
Legend
* = Required parameter
Your task:
• Filter for customer contacts, which have a planned project budget between 500,000 and
1,000,000.
▼ Click to see solution
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > double_range Task
{
"id":"double_range Task",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"double_range",
"field":"/Contact/Project/Budget",
"from":500000,
"to":1000000
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
▼ Click to see request result
13

-- 13 of 54 --

{
"jsonrpc":"2.0",
"id":"double_range Task",
"result":{
"fullSize":3,
"page":{
"pageNumber":0,
"pageSize":10
},
"entries":[
{
"docRef":"ContactFinal_DM/d7b3cbd0-ee04-4a18-aa69-915fe7463b49",
"document":{
"Contact":{
"ContactType":"FINAL",
"Project":{
"Budget":1000000
},
"PersonalData":{
"DateOfBirth":"2000-03-02",
"FirstName":"Miriam",
"Gender":"FEMALE",
"LastName":"Miller",
"CustomerType":"partner",
"EmailAddress":"miriam.miller@gmx.com",
"Nationality":"French"
},
"Phones":[
{
"Type":"MOBILE",
"PhoneNumber":"+33176/931293801"
},
{
"Type":"WORK",
"PhoneNumber":"+4933/10031290123"
}
],
"Address":[
{
"Zip":"59000",
"Country":"France",
"Street":"Rue de Paris",
"City":"Lille",
"Housenumber":"1"
}
]
},
"__meta":{
"createdAt":"2025-06-14T09:42:05",
"creator":"superUser",
14

-- 14 of 54 --

"docRef":"ContactFinal_DM/d7b3cbd0-ee04-4a18-aa69-
915fe7463b49",
"modelReference":"ContactFinal_DM",
"modelVersion":null,
"modifiedAt":"2025-06-14T09:42:05",
"modifier":"superUser"
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
},
{
"docRef":"ContactPotential_DM/bc739965-deaf-4e3c-a49c-757e8c8b5b1f",
"document":{
"Contact":{
"ContactType":"POTENTIAL",
"Project":{
"Budget":500000
},
"PersonalData":{
"FirstName":"Pierre",
"LastName":"Miller",
"EmailAddress":"pierre.miller@gmx.com"
},
"Phones":[
{
"Type":"WORK",
"PhoneNumber":"+33176/321939213"
}
]
},
"__meta":{
"createdAt":"2025-06-14T09:42:05",
"creator":"superUser",
"docRef":"ContactPotential_DM/bc739965-deaf-4e3c-a49c-
757e8c8b5b1f",
"modelReference":"ContactPotential_DM",
"modelVersion":null,
"modifiedAt":"2025-06-14T09:42:05",
"modifier":"superUser"
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactPotential_DM"
},
{
"docRef":"ContactFinal_DM/2235b54f-ee90-43cd-858e-957f9f783891",
"document":{
"Contact":{
15

-- 15 of 54 --

"ContactType":"FINAL",
"Project":{
"Budget":950000
},
"PersonalData":{
"DateOfBirth":"1990-08-06",
"FirstName":"Anna",
"Gender":"FEMALE",
"LastName":"Baker",
"CustomerType":"vip",
"EmailAddress":"anna.baker@gmx.com",
"Nationality":"German"
},
"Phones":[
{
"Type":"MOBILE",
"PhoneNumber":"+49176/57292390"
},
{
"Type":"WORK",
"PhoneNumber":"+4933/12300412"
}
],
"Address":[
{
"Zip":"12392",
"Country":"Germany",
"Street":"Kaiserstrasse",
"City":"Berlin",
"Housenumber":"12"
},
{
"Zip":"90512",
"Country":"Germany",
"Street":"Adenauerstrasse",
"City":"Nuremberg",
"Housenumber":"90"
}
]
},
"__meta":{
"createdAt":"2025-06-14T09:42:05",
"creator":"superUser",
"docRef":"ContactFinal_DM/2235b54f-ee90-43cd-858e-
957f9f783891",
"modelReference":"ContactFinal_DM",
"modelVersion":null,
"modifiedAt":"2025-06-14T09:42:05",
"modifier":"superUser"
}
},
16

-- 16 of 54 --

"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
}
],
"links":[
],
"otherResults":{
}
}
}
We can see that the results are not sorted yet. The first result is: "Miriam Miller", "Pierre Miller",
and then "Anna Baker". If we want to sort these, we could enhance the query by providing the sort
parameter.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > double_range
Example
{
"id":"double_range Example",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"double_range",
"field":"/Contact/Project/Budget",
"from":500000,
"to":1000000
},
"sort":[
{
"field":"/Contact/PersonalData/LastName",
"direction":"DESC",
"nullHandling":"NULLS_LAST",
"ignoreCase":false
},
{
"field":"/Contact/PersonalData/FirstName",
"direction":"DESC",
"nullHandling":"NULLS_LAST",
"ignoreCase":false
}
],
"paging":{
17

-- 17 of 54 --

"pageSize":10,
"pageNumber":0
}
}
}
}
When processing this query, you will see that the query result is now sorted in the order: "Pierre
Miller", "Miriam Miller", and then "Anna Baker". Multiple sort parameters allow us to create a
specific order of the result entries.
You can experiment with the from and to parameters to observe the effects of specifying only one
boundary or both.
date_range
With the date_range operator, we can create a filter that selects document data based on a date
field. Only those that satisfy the provided range constraint are selected.
Table 3. date_range Parameters
Parameter Description
from Specifies the lower bound for the date range.
to Specifies the upper bound for the date range.
value Specifies a Kernel-formatted date range, which contains the full
range. It is exclusive with the from and to parameters. This
parameter only works for the DateRange field type.
Either the from and/or to have to be specified when the value parameter is not used.
Your task:
• Filter for customer contacts, which were born on or after the 1st of January 1990.
• Hint: You should receive the following number of result entries: Three ROOT documents.
▼ Click to see solution
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > date_range Task
{
"id":"date_range Task",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"date_range",
"field":"/Contact/PersonalData/DateOfBirth",
18

-- 18 of 54 --

"from":"1990-01-01"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
undefined_match
The undefined_match operator can be used to specifically filter for null or empty values.
Table 4. undefined_match Parameters
Parameter Description
field* Specifies the Document Model field by its path, which the query
should filter for.
Legend
* = Required parameter
See the following example for the undefined_match operator:
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > undefined_match
Example
{
"id":"undefined_match Example",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"undefined_match",
"field":"/Contact/PersonalData/CustomerType"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
In the example above, we filter for all contact documents which do not have the customer type set.
19

-- 19 of 54 --

This could be either because it only consists of an empty string or it is null.
Your task:
• Filter for contacts who do not have their nationality set.
• Hint: You should receive the following number of result entries: Five ROOT documents.
▼ Click to see solution
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > undefined_match
Task
{
"id":"undefined_match Task",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"undefined_match",
"field":"/Contact/PersonalData/Nationality"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
simple_search
The simple_search operator allows filtering documents by specifying a search term without
knowing the exact Document Model field. It enables full-text search support and capabilities of
PostgreSQL.
Table 5. simple_search Parameters
Parameter Description
fields Specifies the Document Model fields by their paths, which the
query should consider for matching the terms. If not specified, all
indexed fields of the target Document Model are considered for
matching.
20

-- 20 of 54 --

Parameter Description
value Specifies the search term, which the query should search for.
This is a mandatory field if values is not provided.
Search terms can contain multiple words. Per-default,
whitespaces are interpreted as logical AND operator. Therefore,
the search term "Bak Mil" would be interpreted as "Bak AND
Mil", and search for documents satisfying both constraints. E.g.
"Maximilian Baker" would satisfy the constraint, because both
search terms are found in the email address.
values A list of search terms to be queried. This is a mandatory field if
value is not provided.
Specifies a list of search terms, which the query should search
for. Each term in the list is interpreted as logical OR operator.
This is more performant than concatenating multiple simple
search queries with the OR logical operator. (Performance)
Legend
* = Required parameter
NOTE
All terms of the value parameter have to be satisfied within a single document field.
It is not possible to specify a value like "German Baker", and expect to match all
contacts with the name "Baker" and nationality "German".
Your task:
• Filter for contacts with the document data containing information regarding the term
"German".
▼ Click to see solution
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > simple_search
Task
{
"id":"simple_search Task",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"simple_search",
"value":"German"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
21

-- 21 of 54 --

}
}
}
You will notice that we not only receive matches for the nationality "German", but also for
addresses that contain the substring "German". These matches occur because the country is set to
"Germany".
By providing the fields parameter, we can filter specifically for the nationality field.
{
"id":"simple_search Task",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"simple_search",
"fields":[
"/Contact/PersonalData/Nationality"
],
"value":"German"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
With this configuration, the total results are reduced from six documents to four, because now
we are only receiving the matches for nationality being "German".
NOTE
Without specifying the fields parameter, partial matches can cause unintended
search results for the user as all possible matches in all indexed Document Model
fields will be returned.
Logical Operators
We already discovered the field-aware operators and their different characteristics. These only
covered the search for one specific field at a time.
The Query API enables the combination of multiple operators within a single query. This is
achieved by using logical operators:
22

-- 22 of 54 --

• and: Requires all conditions to be met for a document to satisfy the query.
• or: Requires at least one condition to be met for a document to satisfy the query.
• not: Negates a condition, excluding documents that match the specified constraint from the
results.
These operators can be used to create complex queries by combining them with each other.
Table 6. Logical Operators Parameters
Parameter Description
operands* Specifies the conditions which should be combined to match.
This parameter can be used with the and, and or operator.
operand* Specifies the condition which should be matched. This parameter
can be used with the not operator.
Legend
* = Required parameter Either the operands or operand have to be specified.
NOTE
To represent complex constraints, which combine multiple operators and
conditions, we will use a simplified logic notation for demonstration and
explanation purposes. Check out the explanation under the Tutorials > Query API >
Introduction > Simplified Logical Notation section.
The and as well as the or operator expects the specification of an operands list. In contrast, the not
operator only expects a single operand element.
Let’s have a look at small examples for logical operators.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > Logical Operators
Example 1
{
"id":"logical operators Example 1",
"jsonrpc":"2.0",
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
"field":"/Contact/PersonalData/Gender",
"value":"FEMALE"
},
{
23

-- 23 of 54 --

"operator":"double_range",
"field":"/Contact/Project/Budget",
"from":300000
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
Simplified logic:
Contact.Gender == "FEMALE" ∧ Contact.Budget >= 300000
If you execute this query request you will notice, that all result entries satisfy both conditions. Only
customer documents are returned, which are female and have a project budget equal or bigger
than 300,000.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > Logical Operators
Example 2
{
"id":"Logical Operators Example 2",
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
"field":"/Contact/PersonalData/LastName",
"value":"Miller"
},
{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Baker"
}
]
},
"paging":{
24

-- 24 of 54 --

"pageSize":10,
"pageNumber":0
}
}
}
}
Simplified logic:
Contact.LastName == "Miller" ∨ Contact.Budget == "Baker"
Here we retrieve all contacts, which have a last name "Miller" or "Baker".
As mentioned earlier, it is possible to create more complex queries that leverage the different
logical as well as field-aware operators. In this regard, operators can be deeply nested, as shown in
the following example:
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > Logical Operators
Example 3
{
"id":"Logical Operators Example 3",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"or",
"operands":[
{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Miller"
},
{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Baker"
}
]
},
{
"operator":"exact_match",
"field":"/Contact/PersonalData/Gender",
25

-- 25 of 54 --

"value":"FEMALE"
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
Simplified logic:
(Contact.LastName == "Miller" ∨ Contact.Budget == "Baker") ∧ Contact.Gender == "FEMALE"
In the example above, we are first selecting all contacts with the last name "Miller" or "Baker" and
they must be female to satisfy the query.
You will now explore how to use logical operators such as and, or, and not to refine query results.
You will create complex queries, which will also use the previously introduced operators and
parameters.
Your task:
• Filter for contacts with the following constraints:
◦ Having at least one address located in "Germany", and
◦ not being a customer of type "vip".
• Restrictions for the query:
◦ Use the simple_search at least once.
◦ The result entries should only include the following fields:
▪ The address country.
▪ The customer type.
▪ The first name.
▪ The last name.
• Hint: You should receive the following number of result entries: Five ROOT documents.
▼ Click to see solution
• We are using the fields projection to specify which Document Model fields should be
included. All other Document Model fields will be excluded.
• The simple_search operator is used with the specification of the country field.
• We negate the customer type condition.
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > Logical Operators
26

-- 26 of 54 --

Task 1
{
"id":"Logical Operators Task 1",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"fields":[
"/Contact/Address/Country",
"/Contact/PersonalData/CustomerType",
"/Contact/PersonalData/FirstName",
"/Contact/PersonalData/LastName"
],
"constraint":{
"operator":"and",
"operands":[
{
"operator":"simple_search",
"fields":[
"/Contact/Address/Country"
],
"value":"Germany"
},
{
"operator":"not",
"operand":{
"operator":"exact_match",
"field":"/Contact/PersonalData/CustomerType",
"value":"vip"
}
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
Simplified logic:
Contact.Country == "Germany" ∧ ¬Contact.CustomerType == "vip"
Your task:
27

-- 27 of 54 --

• Filter for contacts with the following constraints:
◦ Having no address set in their contact profile, and
◦ having a project budget equal or lower than 400,000, and
◦ having a phone number set in their contact profile.
• Hint: You should receive the following number of result entries: One ROOT document.
▼ Click to see solution
• We are using the undefined_match operator to determine if a field is set or not.
• To check if a repeatable group is set, it would be sufficient to only match at least one required
field. In our case, none of the Document Model fields of the groups "Address" nor "Phones"
are required. Therefore, we have to check on each field, because otherwise it could be
possible that just one address field has been set.
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > Logical Operators
Task 2
{
"id":"Logical Operators Task 2",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"undefined_match",
"field":"/Contact/Address/Street"
},
{
"operator":"undefined_match",
"field":"/Contact/Address/Housenumber"
},
{
"operator":"undefined_match",
"field":"/Contact/Address/City"
},
{
"operator":"undefined_match",
"field":"/Contact/Address/Zip"
},
{
"operator":"undefined_match",
"field":"/Contact/Address/Country"
},
{
28

-- 28 of 54 --

"operator":"double_range",
"field":"/Contact/Project/Budget",
"to":"400000"
},
{
"operator":"not",
"operand":{
"operator":"undefined_match",
"field":"/Contact/Phones/PhoneNumber"
}
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
Simplified logic:
Contact.Street == null ∧ Contact.Housenumber == null ∧ Contact.City == null ∧
Contact.Zip == null ∧ Contact.Country == null ∧ ¬Contact.PhoneNumber == null
has Operator
Until now, we had a look at the field-aware as well as the logical operators. In this section we are
going to delve into the has operator which allows to query for specific links between documents
based on the defined Relationship Models.
Table 7. has Parameters
Parameter Description
relationshipModel* Specifies the Relationship Model reference, which the query
should match.
targetRole* Specifies the role, which will point to the target Document Model.
This defines which side of the relationship will be queried.
constraint Specifies the constraint for the target document, based on the
target Document Model.
linkDocumentConstraint Specifies the constraint to filter for any link documents, which
might be present in the relationship between documents.
Legend
* = Required parameter
29

-- 29 of 54 --

In our tutorial workspace, we have the CompanyContact Relationship Model with the roles
"Company" and "Contact". This Relationship Model defines the RoleAdditionalField_DM as the link
Document Model, which stores information about the role a contact has within their company. For
more information about the setup, check out Tutorials > Query API > Introduction > Extended
Model Workspace.
Let’s have a look at the simplest query we can create with the has operator: Receiving all contacts,
which are associated in the CompanyContact relationship.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > has Example 1
{
"id":"has Example 1",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
Simplified logic:
has("CompanyContact", "Company")
If we switch the targetDocumentModel and targetRole to "Company_DM" and "Contact" respectively,
we will receive all companies associated with the CompanyContact relationship.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > has Example 2
{
"id":"has Example 2",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Company_DM",
30

-- 30 of 54 --

"projectionName":"document",
"constraint":{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Contact"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
Simplified logic:
has("CompanyContact", "Contact")
In both cases, all contact or company documents are returned by the query. This is because all
documents are already linked to each other by relationships in our test data. If new person or
company documents would be created, they would only show up in the query result if they would
be linked to each other via the relationship binding.
If you check out the Employment Module in the tutorial application, you will see a CDM based
overview of all contacts and companies.
Your task:
• Filter for all customer contacts, which are
31

-- 31 of 54 --

◦ working at the company "Trading Good Ltd.", and
◦ of "German" nationality.
• Hint: You should receive the following number of result entries: Four ROOT documents.
▼ Click to see solution
We can specify fields to query for in the target Document Model. The has operator allows to
specify these constraints via the constraint parameter. It is possible to use any logical, field-
aware or has operators on the target Document Model. Here we want to specifically filter for the
company name.
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > has Task 1
{
"id":"has Task 1",
"jsonrpc":"2.0",
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
"field":"/Contact/PersonalData/Nationality",
"value":"German"
},
{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company",
"constraint":{
"operator":"exact_match",
"field":"/Company/GeneralInformation/CompanyName",
"value":"Trading Good Ltd."
}
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
32

-- 32 of 54 --

Simplified logic:
Contact.Nationality == "German" ∧ has("CompanyContact", "Company",
Company.CompanyName == "Trading Good Ltd.")
links
You might have already noticed that the query results only contain the contact root documents, but
no company documents or linked documents.
To also include the linked documents, we can use the links property in our query. This allows us to
specify which relationship and role we want to select.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > has Example 3
{
"id":"has Example 3",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company"
},
"exclude":false,
"links":[
{
"relationshipModel":"CompanyContact",
"targetRole":"Company"
}
],
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
The following response is manually shortened to only include the "ROOT", "CHILD", and "LINK"
document relating to the contact "Anna Baker".
• "ROOT": The actual contact document of the model ContactFinal_DM, which is Anna Baker.
• "CHILD": The linked document of the target Document Model Company_DM in the relationship
33

-- 33 of 54 --

CompanyContact, which is the company named "Trading Good Ltd.".
• "LINK": The link document of the Document Model RoleAdditionalField_DM, which is created
when a contact is assigned to a company. For the relationship between "Anna Baker" and
"Trading Good Ltd." it is the "managerial" role.
▼ Click to see request result (shortened)
{
"jsonrpc":"2.0",
"id":"has Example 3",
"result":{
"fullSize":10,
"page":{
"pageNumber":0,
"pageSize":10
},
"entries":[
{
"docRef":"ContactFinal_DM/486fdd86-1f74-4b9a-a610-cbf4c272ccdb",
"document":{
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
"Zip":"90512",
"Country":"Germany"
}
],
34

-- 34 of 54 --

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
"createdAt":"2025-02-06T09:40:16",
"modifiedAt":"2025-02-06T09:40:16",
"modelReference":"ContactFinal_DM",
"docRef":"ContactFinal_DM/486fdd86-1f74-4b9a-a610-
cbf4c272ccdb",
"modelVersion":null
}
},
"type":"ROOT",
"depth":0,
"documentModelName":"ContactFinal_DM"
}
],
"links":[
{
"docRef":"Company_DM/de275a1c-921e-4697-addf-4763f7f68493",
"relationshipModel":"CompanyContact",
"sourceRole":"Contact",
"sourceDocRef":"ContactFinal_DM/486fdd86-1f74-4b9a-a610-
cbf4c272ccdb",
"targetRole":"Company",
"targetDocRef":"Company_DM/de275a1c-921e-4697-addf-4763f7f68493",
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
35

-- 35 of 54 --

"createdAt":"2025-02-06T09:40:16",
"modifiedAt":"2025-02-06T09:40:16",
"modelReference":"Company_DM",
"docRef":"Company_DM/de275a1c-921e-4697-addf-4763f7f68493",
"modelVersion":null
}
},
"type":"CHILD",
"linkId":3,
"depth":0,
"documentModelName":"Company_DM"
},
{
"docRef":"RoleAdditionalField_DM/a70609ba-c4aa-4144-82c7-
2fc2e6219eba",
"relationshipModel":"CompanyContact",
"sourceRole":"Contact",
"sourceDocRef":"ContactFinal_DM/486fdd86-1f74-4b9a-a610-
cbf4c272ccdb",
"targetRole":"Company",
"targetDocRef":"RoleAdditionalField_DM/a70609ba-c4aa-4144-82c7-
2fc2e6219eba",
"document":{
"id":"__NEW__",
"Root":{
"Role":"managerial"
},
"__meta":{
"creator":"superUser",
"modifier":"superUser",
"createdAt":"2025-02-06T09:40:17",
"modifiedAt":"2025-02-06T09:40:17",
"modelReference":"RoleAdditionalField_DM",
"docRef":"RoleAdditionalField_DM/a70609ba-c4aa-4144-82c7-
2fc2e6219eba",
"modelVersion":null
}
},
"type":"LINK",
"linkId":3,
"depth":0,
"documentModelName":"RoleAdditionalField_DM"
}
]
}
}
Providing the links property enhances the query response by adding all "CHILD" and "LINK"
documents. To reduce the amount of data processed for the response, we can exclude all "ROOT"
documents from the result. When switching the exclude property from "false" to "true", you will
36

-- 36 of 54 --

notice that the entries list in the response is empty and only the links list contains results.
Every linked document of Company_DM is of type "CHILD" and every linked document of
RoleAdditionalField_DM is of type "LINK". Additionally, each sourceDocRef and targetDocRef
represents the actual documents, identified by their document references, that are associated in the
relationship.
linkDocumentConstraint
By using the linkDocumentConstraint parameter, we can additionally filter the links to be included in
our query result. This parameter follows the usual constraint structure of the respective operator.
We can also define more complex queries on these "LINK" documents.
Your task:
• Filter for all link documents of companies and roles that match the role "executive".
• The root documents are Contact_DM, but shall not be included in the query result.
• Hints:
◦ Use the links and linkDocumentConstraint parameters to achieve this.
◦ You should receive the following number of result entries: Three CHILD documents and
three LINK documents.
▼ Click to see solution
We use the linkDocumentConstraint in the scope of the links to specifically filter the "CHILD" and
"LINK" documents based on the specified role, which has to be satisfied.
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > has Task 2
{
"id":"has Task 2",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"exclude":true,
"links":[
{
"relationshipModel":"CompanyContact",
"targetRole":"Company",
"linkDocumentConstraint":{
"operator":"exact_match",
"field":"/Root/Role",
"value":"executive"
}
}
],
"paging":{
37

-- 37 of 54 --

"pageSize":10,
"pageNumber":0
}
}
}
}
We excluded the "ROOT" documents from the query result. If we want to include them in the
response, we can remove the "exclude":true from the request. The response will then include all
contact documents as entries and the filtered "CHILD" and "LINK" documents as links.
Now, we want to retrieve all contacts who are working as executives in any company. Let’s have a
look at the following example to see how we can achieve this.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > has Example 4
{
"id":"has Example 4",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"constraint":{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company",
"linkDocumentConstraint":{
"operator":"exact_match",
"field":"/Root/Role",
"value":"executive"
}
},
"links":[
{
"relationshipModel":"CompanyContact",
"targetRole":"Company"
}
],
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
In the example above, we are using the has operator on the CompanyContact relationship with a
38

-- 38 of 54 --

linkDocumentConstraint to match only those contact documents that have the "executive" role set in
the RoleAdditionalField_DM Document Model. The links remain the same, filtered by the same
criteria to include only those documents.
NOTE
It is only necessary to specify the linkDocumentConstraint under the constraint for
the root documents. The Query API applies this automatically to the links for the
child and link documents as well. Therefore, we do not have to define the
linkDocumentConstraint under the links section for retrieving the correct links.
Document Graph Projection
This projection allows to construct custom document graphs based on constraints and return them
to the client. It can be applied by setting the projectionName property to "document-graph".
Constraints can be defined to create a document graph. The targetDocumentModel has to be specified
to a Composed Document Model (CDM). It will not work with regular Document Models, because
Data Services resolves the existing links between the documents based on the CDM.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > document-graph
Example
{
"id":"document-graph Example",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Employment_CDM",
"projectionName":"document-graph",
"constraint":{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Miller"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
In the example above, we have a query similar to the previous ones, but this time we are specifying
a CDM as query target and using the "document-graph" projection. This query will return the
following result entries:
• Two ROOT documents of our contacts.
• Two CHILD documents of the related companies.
39

-- 39 of 54 --

• Two LINK documents, each including the respective roles of the contacts within the company.
As you can see, with our simple query, we were able to create complete document graphs for all
contacts with the last name "Miller". The document graph contains the respective contact root
documents in the entries list and all company and role links in the links list of the query response.
Composed Data Document (CDD)
The cdd projection allows to construct CDDs based on a CDM. This is in contrast to the previous
document graph projection.
To create CDDs for all of our contact root documents, we can simply create and process the
following query.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > cdd Example 1
{
"id":"cdd Example 1",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Employment_CDM",
"projectionName":"cdd",
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
Let’s have a look at one CDD entry for the Employment_CDM in the entries list. This way you can check
on the structure of the CDD we are receiving from Data Services.
▼ Click to see request result (shortened)
{
"jsonrpc":"2.0",
"id":"cdd Example 1",
"result":{
"fullSize":10,
"page":{
"pageNumber":0,
"pageSize":10
},
"entries":[
{
"docRef":"Employment_CDM/ContactPotential_DM/133b40e5-963b-4690-
40

-- 40 of 54 --

bcec-9f81fc72f3bf",
"documentModelName":"Employment_CDM",
"document":{
"Contact":{
"PersonalData":{
"FirstName":"Pierre",
"LastName":"Miller",
"EmailAddress":"pierre.miller@gmx.com",
"Photo":{
}
},
"Phones":[
{
"PhoneNumber":"+33176/321939213",
"Type":"WORK"
}
],
"Address":[
],
"Project":{
"Budget":500000
},
"ContactType":"POTENTIAL"
},
"CompanyContact":{
"Company":{
"GeneralInformation":{
"CompanyName":"Trading Good Ltd.",
"Logo":{
},
"Website":"trading-good.com"
},
"Address":[
],
"Phones":[
{
"PhoneNumber":"+49221/9213898"
}
]
},
"relationship":{
"Root":{
"Role":"other"
},
"__meta":{
"docRef":"RoleAdditionalField_DM/3a564c54-125d-4267-
aca0-63f76d2ff214",
41

-- 41 of 54 --

"modelReference":"RoleAdditionalField_DM",
"creator":"superUser",
"createdAt":"2025-06-14T09:42:06",
"modifier":"superUser",
"modifiedAt":"2025-06-14T09:42:06",
"extensions":{
}
}
},
"__meta":{
"docRef":"Company_DM/3799a0b2-05c9-4b9e-9a1b-
769915f696c1",
"modelReference":"Company_DM",
"creator":"superUser",
"createdAt":"2025-06-14T09:42:05",
"modifier":"superUser",
"modifiedAt":"2025-06-14T09:42:05",
"extensions":{
}
}
},
"__meta":{
"docRef":"ContactPotential_DM/bc739965-deaf-4e3c-a49c-
757e8c8b5b1f",
"modelReference":"ContactPotential_DM",
"creator":"superUser",
"createdAt":"2025-06-14T09:42:05",
"modifier":"superUser",
"modifiedAt":"2025-06-14T09:42:05",
"extensions":{
}
}
}
}
],
"links":[
]
}
}
We can see in the shortened request result, that the document data consists of the contact data as
well as the company data. The CDD structure is the following for our models:
• document: Contains the whole CDD.
◦ Contact: Contains the data for the Contact_DM Document Model. This also functions as the
root Document Model for the CDM.
42

-- 42 of 54 --

◦ __meta: Metadata of the CDD.
◦ CompanyContact: The relationship, which defines the CDM.
▪ Company: Contains the data for the Company_DM Document Model.
▪ relationship: Contains the link document for the RoleAdditionalField_DM Link Document
Model, which is used to store the role of the contact within the company.
▪ __meta: Metadata of the company document. Here you can see that the metadata
actually represents the timestamp when the documents were created by the
initialization application.
To filter the CDD, we can use the constraint parameter with logical, field-aware, and has operators.
Your task:
• Construct CDDs for employments with the following constraint:
◦ Filter contacts with the last name "Baker".
• Hints:
◦ Define a constraint on the root Document Model of the CDM.
◦ You should receive the following number of result entries: Five constructed CDDs of
Employment_CDM.
▼ Click to see solution
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > cdd Task 1
{
"id":"cdd Task 1",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Employment_CDM",
"projectionName":"cdd",
"constraint":{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Baker"
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
WARNING The simple_search operator does not work on CDDs directly, only on root
documents, because the Query API is not aware of the CDM fields. Therefore, it
43

-- 43 of 54 --

has to be combined with the has operator, to be able to resolve the field path
for the simple_search operator.
Let’s check out the following example to see how the simple_search operator could be used in this
scenario:
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > cdd Example 2
{
"id":"cdd Example 2",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Employment_CDM",
"projectionName":"cdd",
"constraint":{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company",
"constraint":{
"operator":"simple_search",
"value":"Servicing Good GmbH"
}
},
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
In the example above, the has operator is used to create a constraint on the CompanyContact
relationship. This constraint is field-aware of the target Document Model, allowing us to use the
simple_search operator. In this case, we specified the Company as targetRole, which means this
constraint is resolved against the Company_DM Document Model.
Your task:
• Construct a CDD for employments with the following constraint:
◦ The last name of the contact should match "Baker", and
◦ the company that Baker is working for should be "Servicing Good GmbH".
• Restrictions for the query:
◦ Use the simple_search for filtering the companies.
• Hint: You should receive the following number of result entries: One constructed CDD of
Employment_CDM.
44

-- 44 of 54 --

▼ Click to see solution
• We are not able to use the simple_search directly on the CDD, except for the root document. In
our case, the root document is Contact_DM. This is configured in the Employment_CDM model
annotations.
• To be precise, we should add the fields property to the simple_search operator. But it would
also work without it, because there is not any other document with this value matching.
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > cdd Task 2
{
"id":"cdd Task 2",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Employment_CDM",
"projectionName":"cdd",
"constraint":{
"operator":"and",
"operands":[
{
"operator":"exact_match",
"field":"/Contact/PersonalData/LastName",
"value":"Baker"
},
{
"operator":"has",
"relationshipModel":"CompanyContact",
"targetRole":"Company",
"constraint":{
"operator":"simple_search",
"fields":[
"/Company/GeneralInformation/CompanyName"
],
"value":"Servicing Good GmbH"
}
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
45

-- 45 of 54 --

Heterogeneity
The Query API supports selecting documents for heterogeneous Document Models. All model
references will be processed into a single logical structure, which allows to filter document data in
a unified way, without having to consider heterogeneity explicitly in the actual query request.
We will have a look at how heterogeneity is affected when a query is processed.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > Heterogeneity
Example 1
{
"id":"Heterogeneity Example 1",
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
"paging":{
"pageSize":10,
"pageNumber":0
}
}
}
}
This simple query will return all documents of German contacts. You will also notice that all of
them are of the ContactFinal_DM Document Model. The reason behind this is that the
ContactPotential_DM Document Model does not have the nationality field, so none of these
documents will match in general. Let’s say we adjust the query to add an or condition, checking if a
contact has an address in Germany.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > Heterogeneity
Example 2
{
"id":"Heterogeneity Example 2",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
46

-- 46 of 54 --

"constraint":{
"operator":"or",
"operands":[
{
"operator":"exact_match",
"field":"/Contact/PersonalData/Nationality",
"value":"German"
},
{
"operator":"exact_match",
"field":"/Contact/Address/Country",
"value":"Germany"
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
Now you will notice, that we receive two additional documents of potential customer contacts. The
address field is present in both ContactFinal_DM and ContactPotential_DM, which allows these
documents to match on the additionally introduced condition.
This concludes that if a Document Model field is not present in a heterogeneous Document Model, it
will be handled as if it was just empty.
Aggregation
The Query API offers a set of functions to process data and group the values based on specific
Document Model fields.
Table 8. aggregation Parameters
Parameter Description
aggregations* Specifies the functions and respective fields that should be
processed. Multiple aggregations can be processed in a single
query.
group* Specifies the group in which the processed document data shall
be concluded.
Legend
* = Required parameter
The following functions are available:
47

-- 47 of 54 --

• sum: Computes a sum of field values.
• min: Computes the lowest value for the specified field.
• max: Computes the highest value for the specified field.
• avg: Computes the average value for the specified field.
• count: Computes how many times the specified field is present.
In the subsequent request, we will use an aggregation to count how many members each family
has, checking on all contact documents.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > Aggregation
Example 1
{
"id":"Aggregation Example 1",
"jsonrpc":"2.0",
"method":"QUERY",
"params":{
"query":{
"targetDocumentModel":"Contact_DM",
"projectionName":"document",
"aggregation":{
"aggregations":[
{
"function":"count",
"field":"/Contact/PersonalData/EmailAddress"
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
"pageNumber":0
}
}
}
}
To achieve this, we use the count function to count the unique occurrences by using the email
address. We are interested in how many members each family has, therefore the last name field is
used as the group.
▼ Click to see request result
{
48

-- 48 of 54 --

"jsonrpc":"2.0",
"id":"Aggregation Example 1",
"result":{
"fullSize":5,
"page":{
"pageNumber":0,
"pageSize":10
},
"entries":[
{
"docRef":"_generated_aggregation/55197260592144",
"document":[
"Baker",
5
],
"type":"ROOT",
"depth":0,
"documentModelName":"_generated_aggregation"
},
{
"docRef":"_generated_aggregation/34374572339729",
"document":[
"Miller",
2
],
"type":"ROOT",
"depth":0,
"documentModelName":"_generated_aggregation"
},
{
"docRef":"_generated_aggregation/29315788455648",
"document":[
"Limhold",
1
],
"type":"ROOT",
"depth":0,
"documentModelName":"_generated_aggregation"
},
{
"docRef":"_generated_aggregation/99944018106695",
"document":[
"Mayer",
1
],
"type":"ROOT",
"depth":0,
"documentModelName":"_generated_aggregation"
},
{
"docRef":"_generated_aggregation/86859512253448",
49

-- 49 of 54 --

"document":[
"Schmitz",
1
],
"type":"ROOT",
"depth":0,
"documentModelName":"_generated_aggregation"
}
],
"links":[
],
"otherResults":{
}
}
}
In the example above, the request result looks different from what we are used to from the
previous queries. We do not have a real A12 document at hand that belongs to a specific Document
Model; instead, it is a generated document. But in the document section of each entry, we can see
the group as the first element followed by the actual aggregation (in our example, the actual count).
We receive followings results:
• "Baker": 5
• "Miller": 2
• "Limhold": 1
• "Mayer": 1
• "Schmitz": 1
In total, all ten contact documents were computed for the aggregation. You can also verify in the
running application if the aggregation result matches the contact documents.
Data Services also provides a dedicated POST endpoint for processing aggregations, which is more
performant for handling aggregations specifically. This avoids complexities like JSON-RPC wrapping
and complicated transaction handling. The endpoint is available at:
http://localhost:8081/api/aggregation.
The following example represents the same query, but with a smaller request body and fewer data
being necessary to load.
File: bruno/task2/collection-task2-start.json > Task 2 - Discovering Queries > Aggregation
Example 2
{
"id":"Aggregation Example 2",
"targetDocumentModel":"Contact_DM",
"aggregation":{
50

-- 50 of 54 --

"aggregations":[
{
"function":"count",
"field":"/Contact/PersonalData/EmailAddress"
}
],
"group":[
{
"field":"/Contact/PersonalData/LastName"
}
]
}
}
▼ Click to see request result
[
[
"Baker",
5
],
[
"Limhold",
1
],
[
"Mayer",
1
],
[
"Miller",
2
],
[
"Schmitz",
1
]
]
Based on the query response, you will notice that only the data related to the aggregation is
returned. No additional processed data is returned as we saw in the first example. The first array
element in each entry represents the aggregation group field, and the second entry is the computed
value.
Your task:
• Create an aggregation to retrieve the average project budget grouped by the contact type
("POTENTIAL" or "FINAL").
• Hint: As the request URL, use the aggregation endpoint available at:
http://localhost:8081/api/aggregation.
51

-- 51 of 54 --

▼ Click to see solution
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > Aggregation Task
1
{
"id":"Aggregation Task 1",
"targetDocumentModel":"Contact_DM",
"aggregation":{
"aggregations":[
{
"function":"avg",
"field":"/Contact/Project/Budget"
}
],
"group":[
{
"field":"/Contact/ContactType"
}
]
}
}
We can process multiple aggregations in one request by adding more of them under aggregations.
Your task:
• Enhance the previous aggregation by adding the amount of documents in each group.
• Hints:
◦ You can achieve it by adding one more aggregation under aggregations.
◦ Use a unique required field of our Contact_DM Document Model.
TIP Check out the aggregation result you should receive for the correct request.
▼ Click to see request result
[
[
"FINAL",
5,
2760000
],
[
"POTENTIAL",
5,
1600000
]
]
52

-- 52 of 54 --

▼ Click to see solution
File: bruno/task2/collection-task2-end.json > Task 2 - Discovering Queries > Aggregation Task
2
{
"id":"Aggregation Task 2",
"targetDocumentModel":"Contact_DM",
"aggregation":{
"aggregations":[
{
"function":"count",
"field":"/Contact/PersonalData/EmailAddress"
},
{
"function":"sum",
"field":"/Contact/Project/Budget"
}
],
"group":[
{
"field":"/Contact/ContactType"
}
]
}
}
We are also able to group by metadata fields of the document, enabling us to create interesting
aggregations for reporting purposes. For example, the following questions could be easily answered
using aggregations:
• How many documents did each user create?
• How many documents exist for one Document Model?
• When were all documents created? This could be interesting to analyze in the scope of spikes of
documents being created.
Conclusion
In this task, we discovered the various operators, their parameters and how we can leverage them
to create customized queries. These ranged from simple one-field matches to complex constraints
with CDD, relationships and nested conditions.
We checked out aggregations, which can be used for reporting purposes and to compute values
based on document data, by defining aggregation requests.
If something does not seem right, or you got stuck at any point, you can just check out 2025.06-
ext5/query/task-2-end to see differences between both implementations.
53

-- 53 of 54 --

« Task 1: First Steps Task 3: Migration »
54

-- 54 of 54 --

