# overall qapi_introduction

Introduction to the Innovated Query
API
The release line 2025.06 innovates the data query capabilities and API in Data
Services with impact and effect also on some other components, which integrate
with Data Services or use its API, namely: UI engines like the Overview Engine,
Tree Engine or Form Engine, and also Workflows and User Management.
Further perspectives:
For DevOps and Infrastructure
Neither Data Services nor any other A12 component will require Solr as an index or search API
anymore. All indexing and data retrieval will be done using Postgres only. With 2025.06, you can
drop Solr from your infrastructure, unless your project is using Solr directly, which is of course still
valid. Please prepare for more focus on Postgres, since all Solr load will shift to Postgres, which has
different processing and scalability characteristics – more resources might be required here.
For Developers
The Project Template in 2025.06 comes with the required configuration. The Query API Tutorial
helps to get started and understand it. There is a chapter in the Data Services developer
documentation describing the new features.
Other components, especially UI engines use the new API instead of the old one but so far taking
only limited advantage of new capabilities - this will be focus of upcoming releases. As developer,
you can already now customize this and provide UI views with data loaded with the new API in a
more sophisticated way, e.g. you could implement a data loader for an Overview with a complex
and/or optimized query.
For Modelers
Please be aware of the chapter Model-ability in the Data Services documentation.
So far, the integration of the new Query API to other components is done with a focus on feature
parity, i.e. not all new power of Data Services is yet used in or exposed by other components, e.g. UI
engines. This means to large extent, that data and UI modeling, as well as the resulting application,
look and work as before – please be aware of the following To-Dos:
1. All document fields are indexed by default - you might want to remove certain fields from the
index, e.g. for performance or security reasons, in this case, please annotate the field with
indexed annotation with value false. All other values for this annotation name are ignored. Only
indexed fields can be used in Search, Filters and Sorting, e.g. in overviews, the DualPane and
1

-- 1 of 2 --

DropDown Relationship UIs.
2. Keep the current annotations enable_approximate_match_search and
enable_case_insensitive_search as they are still used by the Overview Engine. Changing these
annotations does not require re-indexing anymore.
We plan to enable modelers using the new query capabilities in releases after June.
For Users
Our goal was feature parity, but due to differences in technologies, there are some effects on
feature behavior - please see feature parity, and be aware esp. of the following known or expected
differences in behavior:
New Search Behavior In Standard A12 Applications
• Fields that have the annotation indexed = false are NOT considered in the Full Text Search of
Overviews and DualPane Relationship UIs. Moreover, the autosuggestion in the DropDown
Relationship UI also uses the Full Text Search. This means, less documents will be shown.
• Whitespaces in the Full Text Search term are considered as regular character.
◦ Due to this, the order of the words (separated with whitespace) in the search terms make a
difference.
New Filter Behavior In Standard A12 Applications
• Fields that have the annotation indexed = false do NOT work for Filters. Please make sure, that
all Fields shown in the Filter dialog of the overviews are indexed.
• The asterisk * can not be used as a term in Filters to retrieve all the Fields with any value. It is
treated as regular character.
2

-- 2 of 2 --

