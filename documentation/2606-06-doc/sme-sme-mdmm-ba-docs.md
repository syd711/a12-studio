# sme sme mdmm ba docs

Master Detail Modeling
This documentation is intended for a business analyst audience. Some prior
knowledge of the tools is assumed.
The Master Detail Layout is a common presentation pattern for data within business applications.
For example, the user may be presented with an overview list or tree of business objects, such as
tasks or process documents. When clicking an entry in the overview, a detail view of the entry with
more information and the means to edit the details is opened to the side of the overview.
In A12, a Master Detail Layout can be created by first defining Document Models that describe the
data, and then creating UI models such as Form Models for the detail views and Overview Models
or Tree Models in the A12 Simple Model Editor.
These models can then be used within an A12 Client-based application in an application module,
for example together with the CRUD Extension, to achieve a Master Detail Layout. Setting this up
usually requires editing the Application Model and extending the application setup.
Shortcut to Master Detail Modules for the
Preview Application
The SME provides a special Module Model type and the corresponding editor to facilitate modeling
Master Detail Layouts. This functionality enables business analysts to quickly create Application
Model modules containing Master Detail Layouts, which can be used within the A12 Preview
Application by referencing them in the corresponding Application Model.
To create a new Master Detail Module Model, select "Master Detail Module" in the drop-down of the
"ADD" button in the Workspace Explorer.
Table of Contents
Shortcut to Master Detail Modules for the Preview Application. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 1
Homogeneous Overview Module . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
Heterogeneous Overview Module . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
Tree Module. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
1

-- 1 of 6 --

Figure 1. Add a New Master Detail Module Model
Select the target folder for the model, assign a meaningful name, and add the locales that should be
supported in the Preview Application. If a Roles file is present in the Workspace, select one or more
roles for this model.
Saving the Master Detail Module Model will also prompt an export to disk. After saving the newly
created model, it will appear in the Workspace Explorer in the target folder.
Currently, homogeneous and heterogeneous overviews, as well as tree modules, are supported by
the Master Detail Module Editor. Find details on modeling these below.
After creating a Master Detail Module Model following the regular procedure, the model must be
added to the model references of the Application Model. "Model Type" must be set to "module-
masterdetail" and "Reference" must be set to the chosen name for the Master Detail Module Model:
2

-- 2 of 6 --

Figure 2. Module Reference
The module defined in the referenced model will be incorporated into the Application Model as a
regular application module when the Application Model is deployed via the SME. An individual
deployment of the Master Detail Module Model is not possible.
WARNING The Application Model must be deployed again after each start of the Data
Services in order to see these referenced modules.
Homogeneous Overview Module
A homogeneous overview module consists of one Document Model, one Form Model, and one
Overview Model, and reflects the "standard case" in the Preview Application.
To create a homogeneous overview module, select "Overview" as the Type for the Master Detail
Module, and then select the desired Overview Model. Please note that Binding Overview Models are
excluded from the list of available references. In the "Form Mapping" section, select the
corresponding Document Models and Form Models.
3

-- 3 of 6 --

Figure 3. Homogeneous Master Detail Module
Heterogeneous Overview Module
In a heterogeneous overview module, documents of different Document Models can be displayed in
the same overview. For details and the models required for a heterogeneous overview, please refer
to the Heterogeneity documentation.
To add a heterogeneous module, select "Overview" as the Type and then select the desired
Overview Model. If the super-type Document Model is abstract, it will not appear in the "Form
Mapping" list. If the super-type is not abstract, select the correct Form Model for the super-type
Document Model. Select the correct Form Model for each subtype Document Model.
NOTE
When the overview references a Composed Document Model (CDM), its root will be
displayed instead. As described above, subtypes of the root will be listed
individually. If the root is abstract, it will not be shown.
4

-- 4 of 6 --

Figure 4. Heterogeneous Master Detail Module with an Abstract Super-type
Tree Module
In a tree module, documents from all nodes of a Tree Model can be displayed. For an overview of
the necessary models, please refer to the Tree Engine documentation. Heterogeneous nodes are
supported as well.
To create a tree module, select "Tree" as the Type for the Master Detail Module, and then select the
desired Tree Model.
In the "Form Mapping" section, a row will be added for each node of the Tree Model represented by
its referenced Document Model. Select the corresponding Form Model for each Document Model.
For a heterogeneous node, one row will appear for each subtype Document Model as well. Abstract
Document Model types will not appear in the "Form Mapping" list.
In the "Relationship Editors" section, a row will be added for each node that has a node action with
the event "event_add_link," represented by its referenced Document Model. For each Document
Model in this section, select a Form Model that contains Relationship Binding views to edit links to
all its child nodes. For a heterogeneous node, one row will appear for each subtype Document
Model as well. Abstract Document Model types will not appear in the "Relationship Editors" list.
In the "Additional Link Fields" section, a row will appear for each relationship in the tree which has
a link Document Model. Select the Form Model for each link Document Model.
5

-- 5 of 6 --

Figure 5. Tree Module
6

-- 6 of 6 --

