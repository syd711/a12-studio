# overall types_of_models

Types of Models
Several different types of model are available in A12:
• Document Model
• Relationship Model
• Form Model
• Content Model
• Overview Model
• Tree Model
• App Model
• Print Model & Print Setting Model
Below you can find some very high-level explanations of these models. Please note that this list is
not complete. Refer to further documentation in this section for more details about the different
model types.
Document Model
The Document Model forms the foundation of the entire application. The domain expert maps out
the entire business domain within it, so the Document Model describes the entities belonging to
your business application. An example of a business entity might be a company or an employee.
The Document Model for each of these entities would contain all fields associated with that entity.
Those fields can be organised hierarchically in groups. In more technical terms, Document Models
contain specifications of fields, data types, computation and validation rules.
1

-- 1 of 8 --

Figure 1. Document Model
The Kernel Language can be used to express computations and validation rules. This validation
may be as simple as setting a field to mandatory or as complex as applying intricate patterns and
conditions across multiple fields.
A12 provides different approaches to reuse existing business logic: Type Definitions can be used to
specify common data types, Includes allow to reuse complete sets of groups, fields and rules and
Combination Modeling allows to add a set of fields and rules to different Document Models in a
systematic way.
Document Models are built in the Document Model Editor in the SME and saved as JSON files. The
Document Model Editor facilitates the definition of data structures, validation rules, and
computations. For conveniently adding and modifying validation criteria, the editor features
"autocomplete" and "syntax highlighting" for the Kernel Language.
Relationship Model
The Relationship Model describes the relationship between two entities, hence Document Models.
You can describe the relationship, for example whether it is of type 1:n, n:n etc. You can add
captions that will be used when the relationship data is displayed and also specify Document
Models that contain additional link fields that will be associated with that relationship link between
particular documents of the entities.
The Relationship Model is created and edited in the Simple Model Editor and saved as a JSON file.
The SME also offers the feature of creating a Model Graph Diagram in the so-called Data Modeling
Perspective, which visualizes the relationships within the workspace.
2

-- 2 of 8 --

Figure 2. Model Graph Diagram and Relationship Model
Form Model
The Form Model is one of the UI models available in A12, and it can be used to display details or
fields from one instance of an entity. The Form Model Editor is used to create Form Models, and
they are stored as JSON files.
Figure 3. Form Model in the Form Model Editor
The Form Model Editor pursues a rather abstract but very powerful way of modeling user
interfaces. Instead of diving into design specifics like colors, margins, and spacing, the editor
focuses on the organization of UI elements. Instead of letting the user put input fields, radio
3

-- 3 of 8 --

buttons, or text labels directly on screen in a WYSIWYG style, it operates with hierarchical lists of
model elements. This modeling philosophy enables business experts to focus on their domain and
model complete user interfaces on their own. The graphical presentation is completely decoupled
and is not specified within the Form Model Editor. In order to provide an attractive and modern
design out of the box, A12 makes use of Plasma Design—a coherent design concept for business
applications developed by mgm that is capable of applying various themes.
Figure 4. Form Model in an application
Each Form Model refers to at least one Document Model. This is a crucial aspect for understanding
the design philosophy of the Form Model Editor—and the main difference from classical GUI
builders. There is no point in creating UI models without corresponding Document Models. You can
think of UI models as a kind of wrapper around selected parts of your Document Models. They
provide the frame for user interaction based on a selection of your data fields. Due to this
connection, the Document Model and its accompanying validation rules are usually created first. As
soon as this has been done, new UI models with a reference to this Document Model can be created.
Data and validation definitions take center stage in A12.
Content Model
With the Content Model static pages can be created and embedded in your application, e.g. in the
sample workspaces as welcome page. It’s editor offers both, a model tree displaying the nested
model elements as well as a canvas with a visual representation of the model elements and
WYSIWYG editing options for the modeler.
4

-- 4 of 8 --

Like the other model types, the Simple Model Editor stores this as a JSON file.
Overview Model
The Overview Model is another example of a UI model that is available. Like the Form Model, it is
based on at least one Document Model, it is created in the Simple Model Editor and is stored as a
JSON file.
Figure 5. Overview Model in an application
The Overview Model is used to display lists of entity instances or documents. The Overview Model
can be modeled to allow filtering, sorting, column pinning, row actions, and context menus.
By referencing a Query Model, modelers can limit the documents that are listed. This can take the
5

-- 5 of 8 --

data of the respective document, existing links to other documents or their data into account.
Tree Model
The Tree Model is a UI model which focuses on showing lists of data in a hierarchical structure
based on relationships between the entities. Within a tree these entities are called node types.
While forms are mainly used to acquire data from users, business applications often contain some
sort of hierarchical table for presenting selected aspects of datasets in an orderly fashion like
shown in the image below. This is exactly what the Tree Model is intended for.
Figure 6. Tree Model in an application
The Tree Model can be modeled to allow node type actions and context menus.
Application Model
The Application Model is used to model the application that will contain your forms, trees and
overviews. In the Application Model you can set up the layout of your application, e.g. Master/Detail
- showing a list of documents in an overview or tree, when one is selected its details are displayed
in a form on the right side of the screen.
Using the Application Model, you can add new modules to your application referencing the data
and UI models that you created. The Application Model is edited using the Simple Model Editor and
is saved as a JSON file.
Master Detail Module Model
In the Modeling Environment modelers can use a shortcut to create a Master Detail Module using
this model type. With it you specify the respective models to be used, e.g. the overview you want to
show in the module and the associated document model and form model will be picked up. The
Master Detail Module Model has to be referenced in the PreviewApp’s App Model.
6

-- 6 of 8 --

Figure 7. Master Detail Module
Find details about this special model type in the comprehensive features chapter: Master Detail
Module Model.
Print Model and Print Setting Model
Print Model
Print Models offer a way to generate PDF documents from data processed by applications built on
the A12 platform. Print Models describe the layout, styling, and content of the resulting PDF
document(s).
You can use the Print Model Editor within the SME to create Print Models, and save them as JSON
files within your workspace. You can reference a Document Model in your Print Model and make
use of its fields and groups in order to create a blueprint of the PDF files you want to generate. Key
features of a Print Model include:
1. Content Definition
Includes elements like text, images, tables, lists, headers, and footers. Supports both static
content and dynamic data bindings from the A12 platform.
2. Styling and Layout
Allows customization of fonts, colors, alignment, borders, and spacing. Defines page properties
such as size, orientation, and margins.
3. Dynamic Behavior
Supports advanced features like conditional visibility of specific elements, element repetition,
and calculations. Enables rule-based content generation (by making use of the A12 Kernel
Language) and dynamic layouts.
7

-- 7 of 8 --

4. Standards Compliance
Ensures generated PDFs conform to standards like PDF/A-3a (archival) and PDF/UA
(accessibility).
Figure 8. Print Model Editor in the SME
The Print Model acts as a reusable blueprint, enabling developers and domain experts to create
consistent, professional, and standards-compliant PDF documents efficiently.
Check out our Tutorial about Print Modeling.
Print Setting Model
In the Print Setting Model, you can determine some global settings, such as fonts, which are then
applicable to all of your Print Models within the workspace. Therefore, there can only be one Print
Settings Model per workspace.
8

-- 8 of 8 --

