# sme sme fm ba docs

Form Modeling
This documentation is intended for a business analyst audience. The Form
Model Editor is part of the Simple Model Editor (SME).
Introduction and Concepts
The Form Model Editor is a modeling tool for defining how data is presented in the User Interface
(UI) of business applications. It is used to organize complex forms and introduces unique ways of
creating UI for web applications, based on underlying Document Models and validation rules.
This manual is intended for analysts and domain experts as a reference for working with the Form
Model Editor.
Its contents are structured as follows:
Chapter 1
Provides an introduction to Form Modeling. It outlines the most important modeling principles
and highlights points that are common to Document Models as well as Form Models.
Chapter 2
Describes all basic functions of the editor—from menus and toolbars to the role of different
views.
Chapter 3
Includes a comprehensive description of all dialogs involved in editing online forms with Form
Models. After the model’s elements and structure have been introduced, every element and its
corresponding dialogs are described.
User Interface Models
User Interface (UI) models are essentially construction kits for typical parts of business
applications, such as online forms.
The Form Model Editor facilitates the creation of Form Models, which is one of the UI models.
The Form Model is the most extensive UI model. It is used to lay out the structure and contents of
potentially very complex online forms. The model contains some simple elements, such as sections
and grids, which are mainly used for organizing the underlying data. Other elements, such as
repeats, are more complex and offer a wide range of possible configurations.
In business applications, these modeled parts of the UI are activated by JavaScript engines that
interpret these models. In this case, Form Models are interpreted by the Form Engine.
1

-- 1 of 147 --

General Structure
A Form Model basically consists of two parts:
1. A header with a set of global model properties, which is called Meta Data or Configuration. Here
the model settings are defined. This includes, for example, roles and also a reference to the
corresponding Document Model file which has to be set up in the Document Model Editor
beforehand.
2. A content part with the hierarchical list of model elements describes the actual structure of the
interface part that is being modeled. This is by far the most substantial part of a Form Model.
Common Properties
All model elements in the Form Model Editor have a name and a label property which can be set
separately for each locale in the case of multilingual models. The Common Properties for All Model
Elements, for Example New Button Panel Editor shows these properties using the example of a New
Button Panel editor.
Figure 1. Common Properties for All Model Elements, for Example New Button Panel Editor
Name
All elements have a Name property which is mandatory. It is used as a default label in the Screen
view and helps to distinguish the different elements. Elements can be referenced by using their
name, for example, in order to make them conditionally visible or read-only.
2

-- 2 of 147 --

Label / Title
Many elements have either a label or a title. These are multilingual, that is, there is a separate
text for each locale configured within the model (see Model Settings). Expressions can be set as a
label/title for most elements (for further information on expressions see Expressions).
The declaration of a label or title is optional.
For controls and repeat columns, the labels defined in the Document Model for the connected
field can be overwritten in the Form Model. This can be done either for all controls/columns
linked to this field or only for individual controls/columns. If no label is given (neither in the
Document Model nor in Form Model) then no label will be displayed in the UI. The static text
labels are also used to replace the placeholders in validation error messages and in the
validation bar to identify the controls. Labels of type expression are not considered for
placeholders in validation error messages nor for the jump links in the validation bar.
Live Preview
The Form Model Editor provides a preview feature, allowing you to view the results of your model
changes as you make them. Once the preview has been opened, it will update automatically as
changes are made to the Form Model so that you can see the result of your modeling in real time.
The preview provides a range of functionalities via a menu on the left-hand side, which are
documented in Preview.
The preview is a powerful tool for testing your models as it includes form navigation, the layout of
fields, tables, buttons and the complete form interaction as well as the business logic from your
Document Model.
Validation Bar and Correction Screen
A validation bar is displayed in the Preview above the form if there are any error messages after a
full validation of the document. This allows modelers to test the Kernel based validation of the data
entered into the form that is one of the features of the Form Engine.
The validation bar will name the number of errors, warnings, and infos. You can use the quick-
access or jump links, provided in the expanded validation message, to go right away to the issue of
the mentioned error, warning or info.
A correction mode is also available and may be accessed by clicking on the quick-access or a
provided jump link. Validation Bar and Correction Screen describes this feature in more detail and
how the data entered in the Preview may be corrected using this function.
Form Model
The Form Model Editor pursues a rather abstract but very powerful way of modeling user
interfaces. Instead of diving into design specifics like colors, margins, and spacings, the editor
focuses on the organization of UI elements via models. Instead of letting the user put input fields,
radio buttons, or text labels directly on the screen in a WYSIWYG style, it operates with hierarchical
lists of model elements.
3

-- 3 of 147 --

This modeling philosophy enables business experts to focus on their domain and model complete
user interfaces on their own. The following sections summarize the key principles for working with
Form Models. Generally, the Document Model is defined in the Document Model Editor of the SME,
and then later on the user interface is set up in the Form Model Editor.
Reference to Document Model
Each Form Model refers to at least one Document Model. This is a crucial aspect of understanding
the design philosophy of the Form Model Editor. There is no point in creating Form Models without
corresponding Document Models. You can think of Form Models as a kind of wrapper for selected
parts of your Document Models. They provide the frame for user interaction based on a selection of
your data fields.
Due to this connection, the Document Model and its accompanying validation rules are usually
created first. As soon as this has been done, new UI models with a reference to this Document
Model can be created. The reference to the Document Model file is set in the global model
properties of the Form Model (see Model Settings).
Controls
Once a Form Model references a Document Model, its groups and fields are displayed in the Data
Models view of the editor. In the process of creating Form Models, data fields are put into the Form
Model’s hierarchy via Drag & Drop. The references to data fields in the Form Model’s hierarchy
created that way are called Controls.
NOTE
Controls are references to fields. You can say that Control is kind of a synonym
for the field. Controls build the core of each Form Model. The way the user interface
model is created is closely linked to the underlying data definitions. Controls are
crucial elements of every Form Model. They build the bridge between data and
Form Models. There can be any number of Controls by multiple usages of one single
data field. If the value of one Control is changed by the user of the resulting
application, all Controls which are derived from the corresponding data field are
changed as well.
The visual representation of a Control depends on its underlying data type and its position in the
Form Model’s hierarchy. For example, in Form Models, primitive data types like String or Number
are typically represented as input fields. In contrast, the type Enumeration can be represented as a
bullet point list or drop-down menu. Furthermore, you have the chance to create dependencies
within Controls (see Dependent Controls).
Repeats
Repeats are special structures that facilitate the creation of repeatable Sections which are based on
repeatable groups in the Document Model.
NOTE In case there are several repeats bound to the same group, for each of them, an
empty data set will be created whenever a data set is added to any of the repeats.
For further details on repeats, refer to Repeats.
4

-- 4 of 147 --

Form Model Elements
A Form Model is created by arranging a list of model elements. You can create an element by right-
clicking in the Screen view and selecting an element from the context menu. For every Form Model,
there are restrictions regarding the model hierarchy. Some elements can be placed inside other
elements. Others do not, as you can see in Element hierarchy.
Table 1. Element hierarchy
Model Element Allowed Sub-Elements
Screen Section, Control Grid, Button Panel, Detached Repeat, Embedded Repeat,
Inline Repeat, Custom Screen Element
Section Section, Control Grid, Button Panel, Detached Repeat, Embedded Repeat,
Inline Repeat, Custom Screen Element
Multi-Column Section Section, Control Grid, Button Panel, Detached Repeat, Embedded Repeat,
Inline Repeat, Custom Screen Element
Control Grid Row
Row Control, Text Cell, Expression Cell, Custom Cell
Button Panel Button
Detached Repeat Screen
Embedded Repeat Control Grid
Inline Repeat -
Custom Screen Element Custom code from a developer
Control -
Text Cell -
Expression Cell -
Custom Cell Custom code from a developer
Button -
The Screen element functions as the root element. Screens are usually managed in the Screens View,
but there is one exception: The Detached Repeat also includes an additional Screen. Controls, Text
Cells, Expression Cells and Buttons are the innermost elements of the hierarchy. They cannot
contain other elements.
Sections are the only elements that can be nested inside one another. However, it is indirectly
possible to nest Repeats. Since for each Detached Repeat a Screen element is automatically created,
it is possible to add another Repeat to that Screen, producing a stack of nested Repeats.
5

-- 5 of 147 --

Figure 2. Nested Repeats
Controls are typically the lowest-level nodes in the hierarchy. This is why you can think of the
remaining UI elements as a kind of wrapper or frame for Controls and eventually data fields.
Screen
A form can consist of multiple Screens. A Screen is a sub-part of a complex form that is visible at a
given time. Scrolling might be necessary to access all elements of the Screen. When setting up a
Form Model, different navigation styles can be defined. It can be decided whether Screens are
rendered as individual parts, which are connected by navigation buttons mostly in the form of
"Previous / Next" to support a Screen wizard process or whether a tab navigation style should be
applied.
The label that can be defined for a Screen is used as fallback label for Navigation Buttons which
have this Screen as target and do not have a label themselves.
See Screen for more detailed information on the modeling settings.
Section
Sections, also called Groups, help to group several Control Grids and give them a common headline.
This is an optional element between the Screen level and Control Grid level which is useful for
ordering complex forms by describing the context of the Controls elements contained.
The modeling setting for Sections are described in Section.
6

-- 6 of 147 --

Multi-Column Section
A Multi-Column Section is also used to group Control fields but in a horizontal manner. Up to four
forms can be displayed horizontally next to each other.
For more information, see Multi-Column Section.
Control Grid
A Control Grid is the container for Controls and different types of Cells. A Control is never placed
directly on a Screen - it always has to be placed inside a Control Grid to define its position, for
example, in a multi-column grid. A Control Grid is organized by columns and rows. A Control is
always positioned in one cell, defined by column and row, thus all Controls have their exact
positions within a grid and their alignment is controlled automatically.
Control Grids are described in detail in Control Grid.
Row
A row is a helper element for multi-columned Control Grids. Each row has a label of its own which
can be filled out optionally. See Row for more information.
Button Panel
A Button Panel basically is a container for Buttons. While the buttons defined via Settings >
Subheader and Footer are available for all Screens of the form, the Button Panel introduces
additional ways for precisely placing buttons in sections of specific Screens.
Detached Repeat
Represents a repeatable Section that is modeled on a separate Screen. When entering data into a
Detached Repeat, only the single set of data that is currently edited is visible on the Screen. When
entering the data for the Detached Repeat has been finished, the selected content of this additional
Screen can be displayed as an overview within the enclosing Screen in a table-like fashion.
The modeling settings for Detached Repeats are described in Detached Repeat.
Embedded Repeat
Represents a hybrid of both Inline and Detached Repeat. The data sets can all be viewed in an
overview table at the same time. However, when editing a single set of data an editable row is
opened to display all the necessary Controls to edit that specific set.
Embedded Repeats are described in detail in Embedded Repeat.
Inline Repeat
Represents a repeatable Section where all data entries are directly visible within the enclosing
Screen. So multiple sets of data can be seen simultaneously in the Preview.
You can find more information on Inline Repeats in Inline Repeat.
7

-- 7 of 147 --

Custom Screen Element
A Custom Element basically is a container that is used as a placeholder. This is useful when the
modeler comes up against the limits of modeling, since they can still build their model and a
developer can insert a customized element at this point. The Form Model Editor provides a
CustomScreenElement which can be added to Sections and Screens. Refer to the Form Engine
documentation for a detailed instruction on how to create a customized element.
Control
Controls are references to fields of the Document Model. For example, they can be defined in the
form of text input fields or drop-down lists. For more information about Controls in general, see
Controls. A detailed description of Controls in forms can be found here.
Text Cell
Using text elements, additional explanations and notes can be placed in Control Grids. They will be
rendered read-only as described in Text Cell.
Expression Cell
Expression Cells allow static text to be combined with data from data fields. The output may also be
formatted. Expression Cells can be placed in Control Grids and will be rendered read-only.
Expressions are described in more detail in Expressions.
Custom Cell
A CustomCell basically is a container that is used as a placeholder. This is useful when the modeler
comes up against the limits of modeling, since they can still build their model and a developer can
insert a customized element at this point. CustomCells. can be added to Rows inside a ControlGrid.
Refer to the Form Engine documentation for a detailed instruction on how to create a customized
element.
Button
Buttons can be positioned within Button Panels to be shown in the content of a specific Screen.
They also can be placed in the subheader or footer region of a single Screen or via the model
settings for all Screens. They trigger actions and are associated with events.
Buttons accessible in the subheader or the footer of a form can be defined either via Screen >
Subheader and Footer or via Settings > Subheader and Footer.
For more details refer to Button.
Dependencies
Dependencies are configurational elements which are set up in the Data Configuration View. They
are applied to the underlying fields and groups of the Document Model.
• Dependent Field
Based on the value of a master field, the dependent field can be read-only, not relevant or set to
8

-- 8 of 147 --

a specific value.
• Dependent Group
Based on the value of a master field, all fields of a group can be read-only or not relevant.
• Dependent Enumeration
Allows you to filter subsets of an enumeration according to the prefixes of its enumeration
values.
For further details on dependencies, refer to Dependencies.
Plasma Design
The graphical finish of the UI elements that have been modeled is completely decoupled. In order to
provide an attractive and modern design out of the box, A12 makes use of Plasma Design - a
coherent design concept for business applications developed by mgm.
In order to get the most out of the predefined style definitions, we recommend a set of best
practices for every Form Model. Refer to the UI/UX documentation for designing forms in
compliance with Plasma Design.
In addition to the default design, individual customizing can be done by web designers who may
adapt the CSS of the output generated by the Form Model Editor.
Interlocking Between Document Models and Form
Models
Data and validation definitions are modeled in different, separate models from models that
describe the UI. This strict separation provides a high degree of flexibility. Document Models and
Form Models can be created by different people for example. Several Form Models can be created
based on the same Document Model. There are however some points of contact where both
modeling areas - data/validation and UI - interlock with each other.
The most prominent point of contact is the bridge from data fields to UI Controls (see Controls). In
addition to this crucial concept, there are further areas where you need to be aware of connections
between data, validation, and UI definitions.
Text and Labels
Some texts are defined in the Document Model exclusively because they are strongly linked to data
and validation definitions. These include labels of enumeration elements and text messages for
validation errors. Other labels like headlines can only be specified in the Form Model. Some labels
can be defined in both tools for convenience reasons. In that case, labels defined in the Form Model
will override the labels set in the Document Model (see General Structure). The following table
summarizes by which tool each kind of text or label can be specified:
Type of Label Document
Model
Form Model
Field/Control labels [check] [check]
9

-- 9 of 147 --

Type of Label Document
Model
Form Model
Field configuration labels [times] [check]
Internal field description [times] [check]
Hints [check] [check]
Headlines [times] [check]
Enumeration Texts [check] [times]
Validation Error Texts [check] [times]
Helper Texts [check] [times]
NOTE
For field labels and hints, the default text is defined in the Document Model . If you
"drag and drop" a data field into the Screen view of the Form Model Editor you do
not have to specify the label text each time, because the default label text from the
Document Model is being used automatically. You may, however, overwrite field
labels and hints in the Form Model Editor.
NOTE Helper texts are not shown for inputs inside Inline Repeat tables. You can use the
hints as an additional source of information.
Aspects of Multilingualism
All texts and labels can be defined for multiple locales. There is no restriction on the number of
locales. It is not required to configure a locale. One way to set up multilingual user interfaces is to
configure the desired locales configured in the Document Model and the Form Model. In every
model, the locale specification is part of the global model properties also known as the model
header.
Which locales are used by Form Models is configured in the Model Settings.
Refer to the Document Modeling documentation for a description of how to configure multilingual
Document Models.
NOTE
You can easily set every multilingual element. It is convenient to do because all
elements get additional entry fields for each locale except expressions (read
Expressions for more details). In the Preview only English and German are
supported.
10

-- 10 of 147 --

Figure 3. Multilingual Configuration Example for Labels and Buttons
Repeats in Forms
Some features of the Form Model Editor can only be used if the underlying Document Model meets
certain criteria. A prominent example is the use of repeats in forms. You cannot drag & drop any
field from the field view into a repeatable element in the Screen view. This is only possible for fields
that have been created within repeatable groups in the Document Model .
Enumerations for Dependencies
The Form Model Editor enables users to define dependencies between fields and groups. Based on
the values of a master field, dependent fields or groups can be read-only or not relevant for
example. Only enumerations and boolean fields are allowed to be the master fields for
dependencies. The trigger values are defined in the Document Model .
Basic Editor Functions
This chapter describes the layout and basic functionalities of the Form Model Editor. It first
presents How to Add a New Form Model before providing an overview of the editor’s different
views in Editor Overview. Then, it explains all menus and their commands.
Detailed information about dialogs for specific model elements is provided in the next chapter.
Adding a New Form Model
To add a new Form Model, click the Add button at the top of the Workspace Explorer View and select
Form Model. An Add Form Model dialog opens, with features explained in Add Form Model Dialog
Features.
These settings may be changed after the new Form Model is created. The Name, Document Model,
Locales, and Roles can be changed in the Model Settings, and the folder may be changed in the
Workspace Explorer via drag and drop.
11

-- 11 of 147 --

Figure 4. Add Form Model Dialog
Table 2. Add Form Model Dialog Features
Feature Required Description
Folder Yes A list which allows you to select a root directory.
Name Yes The name of the Form Model.
Document Model Yes A list which allows you to specify the referenced Document
Model with the underlying data. See Settings for more
details.
Build Screens From
Fields
No This action creates default Screens for
all fields in the referenced Document Model which then can
be used as a starting point for refining the Form Model.
Locales No See Settings
Roles Yes/No Required if and only if the Workspace contains a role model.
See Settings
Overview
The Form Model Editor is an application for creating, editing, and examining Form Models. The
editor is divided into several parts that are highlighted in A12 Form Model Editor inside the SME:
12

-- 12 of 147 --

1. Workspace Explorer View
Explorer for browsing and selecting a model that is to be edited. The view lists all A12 models
available in the project folder and its subfolders.
2. Form Model View
Main view containing a sidebar, the Screens, Settings, Data Configuration and Problems View.
3. Data Models View
Contains on top a list of Relationship Models related to the underlying Document Model. These
can be dragged into the Screen View to create new Bindings.
Below the content, that is, the groups and fields, of the referenced Document Model are listed.
The Data Models View is automatically collapsed when opening the settings or an element
editor, but can be expanded again if needed, for example, for defining expressions.
4. Sidebar
Sidebar to navigate through the Form Model Editor and to save or cancel editing the opened
Form Model.
5. Screen View
Main editing window for creating and modifying hierarchies of model elements. New elements
are created using the context menu or through drag and drop of elements from the Data Models
View.
6. Problems View
Displays problems that may occur during modeling, for example, if a field is referenced in the
Form Model that no longer exists in the Document Model.
Figure 5. A12 Form Model Editor inside the SME
Form Model View
The Form Model View is the overall view, which by default displays the Screens View after a Form
13

-- 13 of 147 --

Model is opened. On the left is a sidebar that can be used to navigate between the different views
and to use features such as opening the live Preview.
Sidebar
In the sidebar of the Form Model Editor, the name of the Form Model is displayed in the top. The
following menus, views and features can be accessed:
• Screens
• Settings
• Data Configuration
• Problems
• Preview
Below the menus, the following buttons are available:
• Deploy
• Cancel
• Save As
• Save
NOTE It is possible to save an invalid Form Model, for example, to save an invalid "work
in progress" state of a model. Use this with care.
NOTE Opening a Form Model with an invalid Document Model Reference will open the
Form Model directly in the Settings View showing validation errors to the user.
Screens View
The Screens View is the area of the Form Model Editor where most of the work on Form Models is
done. Here you can set up all model elements and Controls. Depending on the model type you are
working on, different rows are displayed and different model elements become available.
A model element can be added to the list by opening the context menu with a right-click on the
corresponding row or with a left-click on the three vertical dots (hamburger menu or ellipsis) and
then selecting the desired element. Controls are created by dragging fields from the Document
Model Content shown in the Data Models View into the Screens View. By default, the Control label is
taken over from the Document Model set up in the Document Model Editor.
WARNING
The structure of each Form Model has to meet certain criteria. This structure
determines, in which way model elements can be placed on the Screens View.
The rules and restrictions about building the hierarchy of model elements are
described in Form Model Elements.
14

-- 14 of 147 --

Context Menu
In Context Menu in Screens View we see an example Screen named Screen1 and its corresponding
context menu.
Figure 6. Context Menu in Screens View
The context menu lists the elements of the respective model which is edited. In addition to the
model elements, there are some menu items that are available for every Form Model:
• Cut/Copy: Cuts/Copies a model element. This action is only enabled when you select an element
from the Screen’s view that is allowed to be cut/copied.
• Paste: Pastes a model element from the clipboard into the Screen view.
• Delete : Deletes the selected model element or Control. This cannot be undone.
Cut / Copy & Paste
The Form Model Editor allows you to cut or copy and paste elements within the Screens View. You
can either use the corresponding menu items from the context menu or the well-known keyboard
shortcuts Cmd/Win+X, Cmd/Win+C, and Cmd/Win+V. The Form Model Editor maintains its own clipboard
for that purpose.
Drag & Drop
The Form Model Editor provides a drag-and-drop mechanism for utilizing the fields of the
Document Model within the Form Model. In addition, you can use a drag-and-drop mechanism to
simply restructure the existing model elements within the Screens View itself.
15

-- 15 of 147 --

Search Elements
The Screens View displays all modeled Screens along with their respective elements by default. To
facilitate navigation within large Form Models, you can use the search box above the Form Model
tree to locate specific elements.
When you enter a search term, the Form Model tree automatically filters to show only those
elements that contain the search term as a substring, along with their parent items. By default the
search compares the search term to the names of the elements. However, this can be adjusted by
clicking on the settings icon inside the search bar. This opens up the following dialog, where you
can choose to search by label or ID instead.
Figure 7. Search settings
Note:
• When searching for a label of a field, it is important to keep in mind that the label specified on
the control has precedence over the label of the field configuration. The label of the field
configuration has precedence over the label specified in the Document Model.
• Labels are searched by all of the specified locales
• Expression labels cannot be searched
Debugging with IDs
When an error is reported in an application, for example in the Preview Application or Form Model
Preview, the message often includes an element ID. Ensure Search by ID is enabled in the Form
Model tree and then copy the ID from the error. Paste the ID into the Form Model tree search to
locate the offending element and inspect or fix its configuration. If an element has no meaningful
label or name, enable Show Empty Labels in the Form Model Preview to retrieve its ID and use it
together with the ID search for faster, reliable debugging.
Show Elements
As an alternative method for navigating large Form Models, you can choose to display only specific
elements and hide the rest.
To configure which elements are shown, click the button located next to the search input above the
Form Model tree. This opens a dialog listing all top-level Screens, as well as filterable element types
within the Form Model (see 'Show Elements' Dialog in the Screens View).
16

-- 16 of 147 --

For each element in the list, you can select whether it should be displayed or hidden. For Controls
and Field Columns, it is possible to filter for specific field types.
As soon as Form Model elements are filtered, elements that are not filterable are hidden as well.
Additionally, for Screens, you can quickly bring a selected Screen into view by clicking the button at
the end of its corresponding row.
Figure 8. 'Show Elements' Dialog in the Screens View
Settings View
The Settings View is to configure settings for the current Form Model file. You can enter this view
by clicking the Settings tab from the sidebar inside the Form Model View.
The Settings View contains the tabs and corresponding fields that can be found in the following
sections Settings, Repeat Default Button Labels and Subheader and Footer.
17

-- 17 of 147 --

Figure 9. Model Settings View
Settings
Name
The name of the model.
Model Version
The version of the model.
Description
A description of the model, for example, its purpose, editing history, etc.
18

-- 18 of 147 --

General Settings
Document Model
Name of the Document Model file. The path shown in the drop-down depends on the folders that
are shown in the Workspace Explorer.
Amount Suffix
The global suffix, will be used for Controls and repeat columns based on number fields from the
Document Model, which have the unit 'amount'. This could be used, for example, to set a default
currency suffix. The suffix can be overwritten per referenced number field in the control /
repeat column settings.
• Static: An arbitrary string value may be entered.
• Dynamic: The localized value of the referenced enumeration field will be used.
NOTE This enumeration field must be non-repeatable!
Readonly Presentation
Defines the read-only presentation for Controls inside the model.
You can also define the read-only presentation for a Control Grid or Control, which will override
the read-only presentation defined here.
• Input: Controls that are readonly will be rendered as a read-only input field
• Text Output: Controls that are readonly will be rendered as plain text
NOTE Inline Repeats are not affected as they have a separate setting (see Readonly
Presentation).
Show Asterisks
Depending on the setting, asterisks will be shown on labels of Controls and Field Overview
Columns. You can also define it on individual Controls and Field Overview Columns, in which
case this general setting overrides it, unless kept on default (If required).
• If required: Asterisks will only be shown for required fields.
• Never: Asterisks will never be shown.
• Always: Asterisks will be shown on every label, regardless of the required status of the
referenced field. They will not be shown for readonly, disabled or computed fields.
General Detached Repeat Settings
No data change in Detail Screen
This option defines how the commit button of Detached Repeat Detail Screens should be
displayed if no data changed in the Detail Screen yet.
• Disable Button: Show a disabled commit button as long as the data did not change on the
Screen,
• Hide Button: Hide the commit button.
19

-- 19 of 147 --

• Show enabled Button (default value): The commit button will always be shown and enabled.
If one of these options is selected, the button will be enabled/shown as soon as the user
entered some input in any of the Controls on the Screen and leaves the current Control.
NOTE
Once the button is enabled/shown on this Detail Screen it will not be
disabled/hidden again as long as the user is on this Detail Screen.
Reverting changes by changing the input values back to the previous state
will not disable/hide the button.
General Inline Repeat Settings
Readonly Presentation
Defines the read-only presentation for field overview columns in Inline Repeats. You can also
define the read-only presentation for a specific Inline Repeat or a column, which will overwrite
the read-only presentation defined here.
• Input: Columns that are read-only will be rendered as a read-only input field
• Text Output: Columns that are readonly will be rendered as plain text.
Rule Confirmation Settings
Disable Rule Confirmation Dialog
Configures whether a confirmation dialog is shown for warning and information rules.
• always show confirmation: Default, confirmation is enabled for all messages of level "info"
and "warning"
• info only: confirmation is disabled only for messages of level "info"
• info and warning: confirmation is disabled for all messages of level "info" and "warning"
Show Confirmation Summary
Configures whether the summary inside the confirmation dialog is shown
Figure 10. Confirmation Dialog With Confirmation Summary
Preprocessing Settings
The following options allow influencing the preprocessing behavior when opening documents in
the Form Engine.
20

-- 20 of 147 --

Preprocessing when opening a new document
Configures the preprocessing behavior which is applied to a new document after the initial
values and rows have been filled out.
• no preprocessing: neither computations nor any dependencies with value effects are
evaluated
• evaluate computations (default): only the computations defined in the document model are
executed. The computation results will not trigger dependencies with value effects.
• evaluate computations and dependencies: computations and dependencies are evaluated
together. The evaluation starts with the dependencies triggered by the initial values.
Preprocessing when opening an existing document
Configures the preprocessing behavior which is applied when a preexisting document is opened.
• no preprocessing (default): neither computations nor any dependencies with value effects
are evaluated
• evaluate computations: only the computations defined in the document model are executed.
The computation results will not trigger dependencies with value effects.
• evaluate computations and dependencies: computations and dependencies are evaluated
together.
For existing documents, the evaluation starts with computations. Dependencies are only
evaluated as long as the evaluation starts with a trigger change resulting from a
computation. Be cautious, as this could lead to overwriting dependent values that have been
manually changed before saving.
On the other side, dependencies, which do not have a trigger that is changed due to a
computation, are not evaluated. Such triggers can only be considered unchanged and thus,
dependent values are not rewritten.
Mind that for existing documents the last two options can lead to the form already
containing unsaved changes on opening.
Note that for both preprocessing options the display state effects defined in dependencies (for
example, readonly, hidden since not relevant) are always applied directly, independent from the
respective setting and only based on the values in the document. See also Dependency Evaluation
for reference.
When opening the preview or when clicking the reset button in the preview, the document is
preprocessed according to the 'new document' setting. When importing an existing document from
a workspace data file, it is preprocessed according to the 'existing document' setting.
CDM Specific Preprocessing Defaults
The default behavior when opening existing CDM based documents differs from the default
behavior for regular non-CDM documents: Computations are always evaluated. Thus, for Form
Models based on a Composed Document Model (CDM) the default option in the setting for opening
an existing document is evaluating computations.
21

-- 21 of 147 --

The Form Model Editor Preview doesn’t support CDM based documents. When a default value in a
CDM based Form Model was set, it behaves according to the default behavior of a regular Form
Model.
Locales
This shows the list of locales for locale-specific labels. New locales may be added to the list using the
"Add" button at the bottom of the list or removed using the "Delete" button at the end of each row.
NOTE
Enter "en", "de", "fr" and "nl" to the list if you want to enter texts in English, German,
French, and Dutch language.
If you want to specify texts using more fine-granular locale level, you can
differentiate further by entering for example: "en", "de", "en_GB", "en_US", "de_DE"
and "de_AT".
The editor will offer a separate input field for every given locale whenever multilingual inputs are
possible, for example, for labels, titles etc.
Labels
Heading of the form’s content box. If no label is provided, the heading will be empty.
Subtitle
Subtitle below the heading of the form’s content box. Can also be an expression.
Styles
The Form Model Editor allows you to use custom CSS styles for each model element (with the
exception of the screen element of a Detached Repeat). The styles that you add to this list can then
be added to Form Model elements using the editors in the Screens view. Edit Styles Globally in
Model Settings shows an example for which three different style values are defined.
22

-- 22 of 147 --

Figure 11. Edit Styles Globally in Model Settings
These styles can then be applied to all model elements by open its editor, expanding the Styles
section and select from the drop-down menu. Edit Styles for a Control displays the edit dialog for
applying custom styles to a Control.
WARNING
By default, the label and cursor position of number Controls are right aligned.
If you apply your own style, this default style will not be set. In case you still
want the Control to be right aligned you need to apply the helper class
h_rightAlign as well.
23

-- 23 of 147 --

Figure 12. Edit Styles for a Control
The selected styles for a model element are passed to the corresponding widget through the
className property. The following table lists all model elements, which can have styles assigned to
them, and the respective widgets, that these styles will be applied (see A12 Widgets
Documentation).
Form Model Element Widget
ButtonPanel ButtonGroup
EventButton Button
NavigationButton in
Subheader
MenuItem
other NavigationButton Button
ControlGrid LayoutGrid.Grid
ControlGrid Row LayoutGrid.Row
Control, TextCell,
ExpressionCell
LayoutGrid.Column
24

-- 24 of 147 --

Form Model Element Widget
Section Typography.Section
MultiColumnSection LayoutGrid.Grid
Repeat wrapper div data-
role="repeat-
(detached/embedded/inline)
"
FieldColumn,
ExpressionColumn
Table BodyCell
RowAction Button
WARNING
There is no automated Form Model migration for the mapping of styles to the
respective widgets. If there are breaking changes that affect the used widgets
or helper classes, they will be documented in the corresponding tickets. The
styles in affected models will have to be adapted manually.
Roles
The roles that you can assign to your model are taken from a YAML file in the Workspace.
If you have created your Workspace using the Preview App Control, this file is in the "user" folder
and is called "access-rights.yaml".
If no valid YAML file is visible to the SME, then the drop-down will be blank and the assignment of
roles is optional.
Annotations
You can apply Annotations to the Form Model so that the resulting application can access those
Annotations and can use them within the custom implementation. For example, an Annotation can
be used to show all fields that have this Annotation in boldface.
A list of Annotations as name/value pairs for this form can be modeled.
NOTE Annotations can also be added to Form Model elements in the model element
editors and Document Model elements in the Data Configuration View.
Repeat Default Button Labels
The default button labels may be changed for each Locale for the following buttons:
• Add
• CommitAdd
• Apply
• Edit
25

-- 25 of 147 --

• Remove
• View
• Cancel
• Confirm
• Return
• Up
• Down
• Copy
• Close
• Download
• Skip
• Replace
• Upload as Copy
• Remove Confirmation Title
• Remove Confirmation Message
Subheader and Footer
Global Form Model buttons can be created four different areas by adding them to the following
lists:
• Subheader Minor Buttons
• Subheader Major Buttons
• Footer Minor Buttons
• Footer Major Buttons
Data Configuration View
With the Data Configuration View comes the possibility to set, edit or delete configurational
elements such as dependencies. Accordingly, the fields of the Document Model are displayed in this
view. Fields that are part of a dependency are marked with colored flags (see Data Configuration
View With Trigger and Dependent Flags). A distinction is made between Trigger and Dependent flags.
A field that is both a trigger and a dependent gets both flags. Multiple flags of the same type are
clustered with a counter. Hovering the flags will display a list of all fields that have a dependency
linked to this field.
26

-- 26 of 147 --

Figure 13. Data Configuration View With Trigger and Dependent Flags
Problems View
This view lists all kinds of problems that may occur during the Form Model creation process. For
each problem encountered the following properties are displayed, telling where and when the
problem came up.
NOTE The Problem View only becomes visible when you click on the problems tab in the
sidebar of the Form Model View.
Figure 14. Problems View
There is no context menu for the Problems View.
Preview
Clicking the preview button in the sidebar will open a new window displaying a Form Engine that
renders the current Form Model.
27

-- 27 of 147 --

Figure 15. Live Preview of the Form
Every time you now make a valid change, the preview window will update automatically. Reloading
the page is not required for changes which do not influence the preview document.
NOTE
When the preview updates, the existing UI State is reused as long as all screens are
still available in the Form Model. If this is not the case, the UI state will be reset to
the default. The entered data is kept regardless. Because of this behavior, certain
updates in the Form Model Editor might introduce visual inconsistencies in the
preview, for example, removing the sortable property from a column while the
repeat is currently sorted by this column. In these cases, a manual reload of the
preview (by either reloading the page directly, using the sidebar button or closing
and reopening it via the SME) is required.
Cases in which a reload is required to see the effect:
• changing the number initial rows of an Inline Repeat
• changing the initial value of a Control
The menu on the left-hand side of the preview provides the following functionality.
Name Functionality
Screens Navigating to Screens if there is more than one Screen available.
Validate Checking rules and validation. See Validation Bar and Correction Screen
for more details.
Reset Reset to the initial state of the form (for example, removes entered data,
clears validation messages and navigates back to the initial screen). A
Confirmation Dialog is displayed, giving the user the option to cancel the
action.
28

-- 28 of 147 --

Name Functionality
Width Setting the width of the Form Engine to large, medium, small or extra
small. This allows for convenient modeling of responsive layouts.
Theme Selector Setting one of the available themes of Widgets (default, compact, flat and
flat compact), which is then saved in the local storage. This means that
the selected theme will be remembered even if you close the Form Model
Editor. Additionally, Themes which are available in your workspace
(within the folder resources/themes) can be selected here. A custom
workspace theme though will not be remembered after closing the Form
Model Editor and must be manually selected again.
Locale Changing the displayed locale, which can be defined in the Form Model
Settings. For more details, refer to Settings.
Readonly Displaying elements in read-only mode.
Hide / Show Empty
Labels
Showing or hiding generated labels where no label was assigned in the
Form Model .
Data Save data as Workspace Data files to your workspace via Data > Save. The
current state of the document is saved in the same file it was loaded from.
For a new document, it will be created with a randomly generated
filename within the data/documents directory. If present, the respective
attachments are saved and referenced in the document.
Showing raw data in JSON format in a detail view next to the form via
Data > Show data.
Display data from matching Workspace Data Documents offered via Data
> Documents.
NOW Value Changing/Setting NOW. In some models, Rule Conditions are checked
against NOW or TODAY, for example, to prevent deliveries before certain
deadlines. To test such rules in advance, it must be possible to set NOW to
a value in the future. Note: TODAY is implicitly determined by NOW. To
avoid inconsistencies, only NOW can be specified on the interface.
Custom Conditions Specifying the output for the Custom Conditions. The dialog has a table
with the two columns "Name" and "Enabled?". The names of the Custom
Conditions specified in Validation Rules in the Document Model are listed
in the first column. The column "Enabled?" initially is turned off for all
Custom Conditions. The default setting of off is selected so that Custom
Conditions do not report errors by default. If it is enabled instead, the
Custom Condition is always wrong.
WARNING
The data entered in the preview is lost if the preview is reloaded. For example
when leaving the Form Model Editor, changing the Document Model and then
switching back. There is a risk that the Document Model no longer fits the
document of the entered data. For this reason, the document is reinitialized. At
this point, the preview user is encouraged to use workspace data.
29

-- 29 of 147 --

NOTE
Mind that forms with data represent documents. All entered data is validated. But
in some specific error cases, this can prevent storing the data in the document.
Formally incorrect data in certain fields, for example, letters in a number field or a
wrong date format, will not get stored.
In addition to the width menu functionality, the preview itself behaves responsively. This means
when shrinking the browser window, using the responsive dev tools, or working with mobile
devices the form will adapt, and scroll bars and a button to open or close the side menu will appear.
Figure 16. Minimized Preview
The model including a preview can easily be shared cross-computer because you do not need any
further project template files.
The preview includes form navigation, the layout of fields, tables and buttons and the complete
form interaction as well as business logic.
Validation Bar and Correction Screen
The Validate button from the sidebar of the Preview will execute a full validation of all rules
existing in the model. A full validation for customer (live) models is triggered if a button with
validation mode full will be clicked.
A validation bar is displayed above the form if there are any error messages after a full validation
of the document. If only parts of the document are validated, for example, via a button with
validation mode partial, the validation bar will not be triggered.
The validation bar will name the number of errors, warnings, and infos. You can use the quick-
access or jump links, provided in the expanded validation message, to go right away to the issue of
the mentioned error, warning or info.
You will enter the correction mode if you jump to one of the issues using the quick-access or a
30

-- 30 of 147 --

provided jump link. The following limitations exist while the form is in correction mode:
• You cannot add or delete a row of a repeat.
• You cannot use the navigation buttons.
Figure 17. Errors, Warnings, and Infos in Form
The Exit correction Mode button will close the correction mode and goes back to the form.
The Validate button will execute a full validation again.
Or you can use a menu from the validation bar with other options.
31

-- 31 of 147 --

Figure 18. Validation Bar Collapsed With Menu
Errors, warnings and infos are displayed in different colors and with different icons. Errors are red,
warnings are orange and infos are blue.
Figure 19. Validation Bar Expanded
"Show All Issues" will list all errors, warnings and infos together within the Correction Screen.
Figure 20. List With References to All Fields
The list provides links to all issues. These links will redirect you to the fields in the form where you
can make corrections.
Clicking Back in the header will close the entire Correction Screen and your form will be shown
again.
32

-- 32 of 147 --

Data Models View
The Data Models View shown in Data Models View has two parts. In the upper section, all
Relationship Models related to the referenced Document Model are listed. The lower section shows
all groups and fields of the referenced Document Model.
The elements from both sections can be dropped to the Screens of the Form Model in order to
create new Form Model elements as explained in Drag & Drop.
Figure 21. Data Models View
33

-- 33 of 147 --

Refactoring Support
For a general description of the refactoring feature, see the Refactoring Support documentation.
The following tables show what action triggers a refactoring and what properties might be affected.
Within the Model
Action Affected Properties
Deletion of a Screen Target Screen in Navigation Buttons
Deletion of a Repeat Column Initial Sorting Column in the same Repeat
Deletion of a Screen Element Trigger Element for Dependent Controls
Deletion of a Control Focused Element For Initial Rendering in a
Screen
Deletion of a Style Styles in all Elements
Renaming/Deletion of a Row Action Default Row Action in the same Repeat
Across Models
Action Affected Properties
Deletion of a Field Trigger Element for Dependent Fields and
Dependent Groups
Deletion of a Enum Field Dynamic Amount Suffix in the Model Settings,
Trigger Element for Dependent Enumeration
Renaming a Document Model Document Model in the Model Settings
NOTE Deleting a referenced Document Model (or one of their Includes) is deemed
unrecoverable and is not covered by the Refactoring Support.
NOTE
Field references in Expressions (Filter Expressions, Expression Labels, Expressions
Cells) cannot always safely be refactored. To avoid changing those occurrences in
an unintended way and thus breaking business logic, they are not refactored at all.
Editors for Model Elements
Forms are the heart of document-oriented business applications. The Form Model provides the
means to create and manage complex forms with up to thousands of fields, dependencies, and
repeatable sections.
The following sections describe the model’s elements, structure, and modeling settings.
34

-- 34 of 147 --

Common Features
Several settings are available on nearly all Model Element Editors in the Form Model Editor. For
example:
Name
You must assign a name to all elements in the Form Model except for rows. The names of sibling
elements must be unique.
NOTE
Rows that do not have a name are displayed as "<Row>" in the Form Model Tree.
This applies to Rows that are:
• Automatically generated when Controls are added to a Control Grid.
• Manually created and unnamed.
Label
Type
• Text: A list showing the different locales is displayed under the drop-down to allow
multilingual labels to be modeled.
• Expression: An expression may be added in the editor below this drop-down. See
Expressions for more details.
NOTE Button labels are displayed in capital letters.
Hide Condition
The Hide Condition section available in the editor of most Form Model elements allows you to
configure the element to be conditionally hidden based on the values of a selected field. See section
Conditionally Hidden Elements for more details.
Styles
The property Style allows you to customize model elements. Before you can set a Style for a button,
you must define Styles in the Model Settings. Refer to Styles in the Settings View.
To add a style to a model element:
1. Expand the Styles section.
2. Click the Add button.
3. Select one of the available styles defined in the model settings using the drop-down menu.
WARNING
Be careful when combining Styling and Styles for buttons. It is possible to set
a Styling Primary and a Style, for example, button__secondary, to the same
button. For some combinations, this might cause erroneous behavior. When
migrating, the set Style will be used.
35

-- 35 of 147 --

Annotations
To add Annotations to Form Model elements, click any element in the Screens View. Annotations
can then be added in the bottommost section.
NOTE To add Annotations to field entries, click any Document Model element in the Data
Configuration View. In the editor form, choose the tab "Annotations".
Figure 22. Edit Annotations
Screen
A Screen refers to a distinct section or page within a form and serves as a logical grouping of
related fields and information, called Screen Elements.
Screen Element Reference
Section Section
Multi-Column Section Multi-Column Section
Control Grid Control Grid
Button Panel Button Panel
Binding
Custom Screen Element
A Form Model can consist of multiple Screens, between which navigation buttons (see Button
Details) allow switching back and forth. Only the first Screen in a form can be assigned a specific
Control within that Screen, which will be focused when the form is initially rendered. The following
image shows the editor of a Screen and its drop-down menu, listing all paths of focusable elements.
36

-- 36 of 147 --

Figure 23. Screen Editor
NOTE Controls that are set to read-only are not considered in the selection for an initially
focused Control.
NOTE
Controls to be initially focused should be placed near the top of the form, since
focus is called together with scrollToTop, which cannot be disabled. Due to the
master-detail layout animation, proper scrolling behavior cannot be ensured. The
control will still be focused, even if it is not visible due to incorrect scrolling.
Section
Sections allow the grouping of different Screen elements by giving them a common headline. A
Section is an optional element. It creates a hierarchical layer between the Screen and the Control
Grid which is useful for ordering complex forms. With the collapsing feature, you can optimize the
clarity of large forms.
Figure 24. Collapsible Sections in Preview
The same control element can be part of multiple Sections. The number of occurrences of a field
are shown in the Document Model section of the Data Models View next to the field’s name in
brackets, for example, FieldName (3) for a field being used three times in the Form Model. The most
recently set value will be propagated to all other occurrences of the field.
Sections are essential for realizing nested structures. You can place Sections into Sections and
create any level of depth. Sections can only be placed in Screens and Sections and not in any other
37

-- 37 of 147 --

kind of element. To add fields to Sections you need to create a Control Grid inside a Section.
You can edit the properties by clicking on a Section. Name, multilingual labels, and the collapsing
functionality can be set in the upcoming view.
Figure 25. Section Settings
Multi-Column Section
A Multi-Column Section is a special Section type that allows grouping Screen elements in a
horizontal manner. Up to twelve Screen elements can be displayed horizontally next to each other.
38

-- 38 of 147 --

Figure 26. New Multi-Column Section
Within a Multi-Column Section, other Sections as well as other Screen elements like Control Grids,
Button Panels, and Repeats can be added. It is also possible to put a Multi-Column Section within
another Multi-Column Section.
You can define a layout for a large horizontal space and if required also the responsive layouts md
and sm for narrower space situations.
NOTE
There is also a layout xs for extra small horizontal space. This layout is always
present independent of the presence of the other layouts. It cannot be edited in the
Form Model Editor. Its behavior is to show one element per row, ignoring span and
offset.
The layouts define the widths of the columns within the Multi-Column Section. Each column is
represented by a number from 1 to 12 and the columns are separated by dashes.
For example, you can set it to 3-3-6, which means that there are 3 columns where the first two
columns each take up 1/4 of the total grid width and the third column 1/2.
39

-- 39 of 147 --

For the large layout, the values for all columns can sum up to 12 at most. For the responsive layouts
md and sm sums larger than 12 are allowed. This enables creating layouts with wrapping behavior
since columns exceeding the sum of 12 will be shown wrapped to the next row. The number of
columns has to be equal among all layouts. Responsive layouts can only be provided when also a
large layout is provided.
When there are no responsive layouts provided the large layout will be used for all space situations
(except extra small, see Note above). When the responsive layouts are only provided partially, there
is a fallback mechanism in place: The next wider layout available is used if, for the current width,
no layout is present. For example, when the available space is medium wide but only layout lg and
sm are present, the layout lg will be used.
By default, the layout lg is set to 12 when creating a new Multi-Column Section that contains no
column at all. The number of Screen elements allowed within a Multi-Column Section is determined
by the number of layout columns. You can only add so many elements via copy & paste, drag &
drop, or the context menu.
NOTE
The responsive layout of a screen element is based on its own width and not on the
width of the entire form. Therefore, screen elements of different widths will switch
between layouts at different times.
For example: A Multi-Column Section might still use layout lg, while a Control Grid
inside this Multi-Column Section already switched to layout md.
Refer to the A12 Widgets documentation for more details.
Control Grid
A Control Grid is the container for Controls and read-only texts. Such elements are never placed
directly on a Screen - it is always placed inside a Control Grid to define its position (for example, in
a multi-column-grid).
40

-- 40 of 147 --

Figure 27. Edit Control Grid
41

-- 41 of 147 --

A Control Grid is organized by columns and rows. A Control is always positioned in one column and
one row thus all Controls have their exact positions in a grid and their alignment can be
automatically controlled.
You can edit the properties of the Control Grid by clicking on it. The Form Model Editor displays a
view as seen in Edit Control Grid. This view has the following fields:
Name
Name of the Control Grid, used for example in setting up dependencies.
Layout lg
If you want to specify more than one column in your grid, you have to set a value for the layout
lg property. Each column is represented by a number from 1 to 12 and the columns are
separated by dashes. For each column, a value has to be specified. So in the case of three
columns, a possible layout lg value would be 3-3-6, that is, the first two columns each take up 1/4
of the total grid width and the third column 1/2. The sum of the layout numbers can be 12 at
most.
Layout md / sm
Similar to the Multi-Column Section, you can define the responsive layouts md and sm for
narrower horizontal space situations. The same features and rules as in the Multi-Column
Section apply.
When a responsive layout is removed, the offset and span configuration for the respective layout
will be removed from all Controls, Text Cells, and Expression Cells within the Control Grid. This
has to be confirmed by the user.
Readonly
Control Grids can be set to readonly. In this case, attempts to edit a value in the Controls inside
the Control Grid are blocked. For date fields, the date picker icon will not be displayed. For
required fields inside the Control Grid, the asterisk will not be shown.
Readonly presentation
Defines the read-only presentation for the Controls inside the Control Grid.
• Default: Uses the read-only presentation defined in the model. If no presentation is defined
in the model it renders the Controls as read-only Inputs if they are readonly
• Input: Controls that are readonly will be rendered as a read-only input field
• Text Output: Controls that are readonly will be rendered as plain text
Figure 28. Preview of Readonly Presentation
Vertical Alignment
Configuration for the vertical alignment of the Controls inside the Control Grid.
42

-- 42 of 147 --

NOTE
The Form Model Editor allows adding more elements than allowed. The user then
gets the error and can fix the arrangement.
Corresponding to adding this new behavior the automatic rearrangement of rows is
dropped. Therefore, in the Form Model Editor for a row holding too many elements
for a new layout, no "spillover" rows are generated anymore.
Row
A row is used to structure Controls inside a grid in case of a multi-column layout. Thus, each Control
can have its exact position within a column per row. Add New Row shows the dialog for adding a
new row.
Figure 29. Add New Row
Controls
Controls are input fields for the web form and must be initially defined as data fields in the
Document Model. A Control is the actual UI representation of a field on the web form. The term
Control is used to differentiate between the field in the Document Model and its representation in
the user interface. After a field has been dragged into the Screens View to create a Control, you can
open the Edit Control dialog, as shown in Edit Control, by clicking on the respective Control.
43

-- 43 of 147 --

Within the Edit Control dialog, several attributes are displayed, and most of them can be set.
44

-- 44 of 147 --

Figure 30. Edit Control
45

-- 45 of 147 --

Common Settings
Internal Field Description (Document Model)
This section displays the internal field description that has been provided in the Document Model
for the underlying field of the Control. This description is shown either in English, if present, or in
the first locale of the Document Model. This section is read-only.
Label
The label displayed in a form can have three origins:
Document Model
The Document Model section displays the various multilingual labels that have been set up for the
Control within the Document Model. This section is read-only.
Field Configuration
With the Field Configuration, it is possible to (optionally) set a label for all controls that reference
the same field used by this control.
NOTE When you define an Expression as the configuration label of a field, it will only be
used as a fallback for Controls, but not for Field Overview Columns.
Control
The Control label can deviate from the other two labels and is only considered for the single,
specific Control in the form.
Label Hierarchy
This leads to the following hierarchy of labels, and the form engine will display:
• the Control label if present
• the Field Configuration label if present in this Form Model for the referenced field
• the label from the Document Model
Hint
As with Label, the Hint shown at a Control in the form can have three origins. If the field is
configured with a Description (External) in the Document Model, its content will be displayed as a
Hint.
The Field Configuration Hint and Control Hint attributes allow you to provide context-help
information in several locales for the form if no Description (External) has been configured.
46

-- 46 of 147 --

Figure 31. Edit Control Section for Hints
The hint attribute is an additional (possibly longer) text displayed when hovering over the "info"
icon. It will appear next to your input or, in the case of enumeration expositions, next to the label of
the Control.
Figure 32. Preview of Several Field / Control Types and Their Hints
Hint Hierarchy
This leads to the following hierarchy of hint texts, and the form engine will display:
• the Control Hint if present
• the Field Configuration Hint if present in this Form Model for the referenced field
• the Description (External) from the Document Model
Placeholder
With the Placeholder Label (Multilingual) attribute, a placeholder can be defined that will be
displayed in a text or selection input if no value is entered yet.
47

-- 47 of 147 --

Figure 33. Placeholder Example
Layout
The layout of the Control can be specified by Offset `lg` and Span `lg. Within the whole grid layout
of the containing Control Grid, the offset defines the number of empty cells (starting from the left)
and the span defines the number of columns the Control is spanned.
The following pictures show an example:
Figure 34. Configuration of Span and Offset in Control Settings
Figure 35. Preview in Browser of Configured Example
If the model contains a responsive layout anywhere, offset and span can be defined for the layouts
lg, md and sm individually. But offset and span md and sm are only editable, when the parent Control
Grid has a value for the respective layout.
The offset configuration of a Control in a responsive layout is only valid if the total width of the
layout columns taken up by the Control's offset is not larger than 12. The same applies to the span
configuration. The total width of the layout columns spanned by the Control must not be larger
than 12.
Mind that it is not possible to create an empty row by using an offset over the full grid width since
the invisible element creating the offset has no height.
48

-- 48 of 147 --

Figure 36. Configuration of Offset and Span for Responsive Layouts
Additional Settings
The contents of the Additional Settings section of the "Edit Control" view can vary depending on
the data type of the 'Control'. This will be described in detail in the subsequent paragraphs.
Initial Value
The initial value can be set to a value according to the data type of the underlying field of the
Control. This value will be used when editing an "empty" form or when adding a new row in a
repeat.
When modeling initial values, for Controls of certain field data types just an input is shown. In this
case the following data formats need to be used for the value:
Data Type Format Example
String no special format my initial string value
Number a number potentially using a "."
as decimal separator
1234.5
Date yyyy-MM-dd 2024-05-16
Time HH:mm:ss 15:30:00
DateTime yyyy-MM-dd’T’HH:mm:ss
The 'T' means the actual letter
T, but without quotes
2024-05-16T15:30:00
49

-- 49 of 147 --

Data Type Format Example
DateFragment MM
yyyy
MM-dd
yyyy-MM
depending on the
DateFragment format of the
field
05
2024
05-16
2024-05
DateRange yyyy/yyyy
yyyy-MM-dd/yyyy-MM-dd
MM/MM
yyyy-MM/yyyy-MM
MM-dd/MM-dd
depending on the DateRange
format of the field
2024/2028
2024-05-16/2028-10-17
05/10
2024-05/2028-10
05-16/10-17
Exposition
Controls can be rendered in different styles. These styles are called Expositions. The Expositions
offered depend on the Data Type and are briefly described in the respective sections. Default
expositions will be marked with the suffix "(default)". They are not persisted in the Form Model and
may change in future (breaking) releases.
Position of Hint and Validation Message
Positioning of the hint and the validation messages on the Control can be configured to fit different
spacing conditions.
When 'Default' is selected, validation messages will be rendered above the Control and the hint icon
will be rendered behind.
Figure 37. Tooltip and Messages in Default Position
Selecting 'Beside Control' will result in the validation messages being rendered as tooltips beside
50

-- 50 of 147 --

the Control, which might be required when the form has limited height.
Figure 38. Tooltip and Messages Beside Control
Selecting 'Above Control' will result in the validation messages being rendered as tooltips together
with the Control tooltip above the Control. This might help in forms with limited width.
Figure 39. Tooltip and Messages Above Control
Readonly
Controls can be set to read-only. In this case, attempts to edit a value in the Control are blocked. For
date fields, the date picker icon will not be displayed. For required fields, the asterisk will not be
shown.
Readonly Presentation
Defines the read-only presentation for the Control.
• Default: Uses the read-only presentation of the parent Control Grid or model. If no presentation
is defined for the parent Control Grid or the model it renders a Control, which is read-only as a
read-only Input.
• Input: Renders a Control, which is read-only, as a read-only Input
• Text Output: Renders the Control, which is read-only, as plain text
Figure 40. Preview of Readonly Presentation
Show Asterisk
Depending on the setting, an asterisk will be shown on the label of this control.
• If required: An asterisk will only be shown if the referenced field is required.
• Never: An asterisk will never be shown.
• Always: An asterisk will be shown on the label, regardless of the required status of the
referenced field. It will not be shown, if the field is readonly, disabled or computed.
51

-- 51 of 147 --

Control Index
This section is only visible if a Control is placed outside its nested repeatable group. It is possible to
configure the condition for selecting the right repeatable group instance. This is easily done by
defining a numeric (number of rows) or semantic index (value of index field of the row) to identify
the repeatable group instance.
Figure 41. Edit Control Index
This is limited to only one level of repeatability between the context of the Control Grid and the
used field.
Currently, no checks are applied to the value field. Double-check your values.
In the Form Engine, the nested repeatable group will be created if the value of a corresponding
Control changes and no suitable repeatable group can be found. Furthermore, if a Control of a
nested repeat exists and an initial value is defined, then the Form Engine will create the
corresponding entry of the nested repeatable group with the creation of the parent context.
Accessibility
Controls support accessibility by having their label programmatically connected to their input. This,
52

-- 52 of 147 --

for example, allows screen readers to read the label for the input or allows that the input gets the
focus when clicking on the label. The accessibility section provides configuration options for this.
Hide Label
If the design of the web form requires that the label of Control shouldn’t be visible, it can be hidden
with the 'Hide Label' option. This will only visually hide the defined label, but still keep it available
for accessibility tools.
String Type
NOTE The settings Exposition and Secret are not available for a field of type String that
has Suggestions since they are not useful in this case.
Exposition
• text line (default): renders the Control as a text line
• Area: renders the Control as a text box that enables a multi-line input and breaks
automatically. If you did not set Line Breaks Permitted for strings in the Document Model ,
you will get an error message when doing a manual line break via enter.
When the exposition is set to Area you can set the option Auto Expand. In this case, the
multi-line input will initially be shown as a single-line input and then expand automatically
depending on the space needed by the inserted text. Auto Expand Example shows how the
different options are rendered.
Figure 42. Auto Expand Example
Secret
The Secret attribute is only valid for fields of type String that do not have the exposition ’Area’
set and whose underlying Document Model fields do not have the property ’Line Breaks
Permitted’ set to true. When a Secret is set, it does not reveal the content of the field while
editing (for example, for passwords). The entered value is hidden and represented by asterisks.
WARNING
Setting a Control to secret will only visually hide its content. The setting will
not affect other Controls for the same field and has no additional security
aspects.
Autocomplete
Since Controls are input fields, the autoComplete attribute can be provided to them in order to
automatically fill out the field. You can specify which value should be passed to the autoComplete
HTML attribute through a selection field. The values are sourced from here. The only exception
is the value "chrome-off." The reason for this is that the autocomplete="off" instruction is not
supported in the Chrome browser. To make it work in Chrome, you need to use a value other
53

-- 53 of 147 --

than "off." The designation of chrome-off also serves to inform the developer about the approach.
NOTE The autoComplete attribute can also be set on Columns that are an input field.
External Enumeration
External enumerations are a means to provide enumeration values from external sources. The
corresponding fields must be modeled as a string within the Document Model, not as enumerations.
The following steps have to be performed when applying an external enumeration.
1. Navigate to the Data Configuration View
2. Click on the favoured field of type string
3. From the upcoming Edit Field Configuration Entry, click on External enumeration > Set
External Enumeration
4. Within the view shown in View for Selecting an External Enumeration, specify a Source URL
which delivers the enumeration value
5. Select an Exposition (see Enumerations)
Figure 43. View for Selecting an External Enumeration
When the exposition autocomplete was chosen, it is possible to configure more properties of the
external enumeration:
Allow custom values
Enables entering a custom value independent of the values provided by the external
enumeration source.
Case sensitive
To determine, if the entered value is a custom value or one of the provided values, the values are
compared, ignoring the case by default. When this option is checked, the comparison happens in
a case-sensitive way. For example, the entered value "cat" would be recognized as distinct from
the provided value "Cat" and added as a custom value.
54

-- 54 of 147 --

Differences Between Enumeration, External Enumeration and String Field with Suggestions
Enumeration External Enumeration String Field with
Suggestions
Values are
defined in
Field Editor Data Configuration View
(as external source)
Field Editor
Exposition Selection of different
options (see Enumerations)
Selection of different
options (see Enumerations)
Autocomplete
Custom
value
No Can be set when exposition
is set to autocomplete
Yes
Case
sensitive
No Can be set when exposition
is set to autocomplete and
custom values are allowed
Yes
Localization Each value is localized Each value is localized Suggestions can be defined
for each locale
Number Type
Suffix
With the Suffix (Multilingual) attribute a Suffix can be defined for Controls based on number
fields, for example, to display a currency or physical unit. You need to define a Suffix for each
locale of your model. If it is the same for all of your locales you can use the "Fill All" button to fill
the Suffix for all locales.
When creating a Control based on a number field that has the unit 'percent' or 'permille', the
Suffix will be prefilled with the respective symbol (%, ‰).
A global Amount Suffix can be defined in the Model Settings and will be used for Controls based
on number fields with unit 'amount'. It can be either static or dynamic. When static, the given
value is simply used in all applicable places. When dynamic, the localized value of the
referenced enumeration field is used instead. The global Suffix can be overwritten per number
field by setting a Suffix in the Control settings.
The number of decimal places to render is defined on the referenced number field in the
Document Model.
When a Suffix is set on the Control, it will take precedence over the number of suffixes set in the
model.
Truncate suffix
In Controls based on number fields, checking this option will display long suffixes of number
Controls truncated, for example, when the horizontal space is limited. The full suffix is visible
when hovering over the snippet.
Mind: If "Text Output" is set as a read-only presentation and the Control is read-only this option
will not take effect.
55

-- 55 of 147 --

Figure 44. Suffix Configuration
Exposition
No exposition so far.
Boolean Type
Exposition
• Boolean Select (default): renders the Control as a drop down menu
• Checkbox: renders the Control as a checkbox
• Switch: renders the Control as a switch
• Switch-With-Values: renders the Control as a switch with labels for both options, for
example, "yes" and "no" for English localization
• Full: renders the Control as a vertical radio button list
56

-- 56 of 147 --

• Inline: renders the Control as a horizontal radio button list
NOTE
The only exposition that allows a user to select all three values is Boolean Select. All
other expositions show no initial selection and then switch between the true and
false values.
If the Exposition is set to Switch, there are two additional fields shown to configure the Control
even further, first Label Placement which allows setting the label to be above the control, left of the
control or right of the control, second Switch Icon, which allows using any Icon as the icon shown
for a checked switch. For Switch with Values, only the Switch Icon is configurable.
Date Type
Exposition
• Data Type Date No exposition so far. Nevertheless, you can customize some values. The
range of years can be configured as Relative or Absolute. The default value is -7/+7 from the
current year. With Preview you can check how it will be displayed in the Form Engine.
Figure 45. Date Configuration
In the Form Engine, you see an icon in the date field. When you click it a date picker appears.
57

-- 57 of 147 --

Figure 46. Date Picker
There is no picker support for partially known dates, you need to enter them in the input
field. If you entered a partially known date and open the date picker, then the current date
(taking the preselection year into account) will be preselected in the date picker.
Date Time Type
Exposition
No exposition so far. In the Form Engine, you see an icon next to the date time field. When you
click it, a date time picker appears showing a date selector.
58

-- 58 of 147 --

Figure 47. Date Time Picker
After clicking EDIT TIME a time picker will appear showing a time selector. The time selector
respective to its format depends on the set locale and therefore will be displayed as 12 hours
(with a.m./p.m.) or 24 hours circle.
59

-- 59 of 147 --

Figure 48. Date Time Picker 12 Hours Format
60

-- 60 of 147 --

Figure 49. Date Time Picker 24 Hours Format
Time Type
Exposition
No exposition so far. In the Form Engine, you see an icon next to the time field. Clicking the icon
will trigger a time picker. This provides the same functionality as the time selector of the date
time picker. It will be displayed in a 12 or 24 hours time format depending on the set locale.
Date Fragment Type
Exposition
No exposition so far. In the Form Engine, you see a text input for this data type. There is
currently no date picker support for date fragments.
Date Range Type
Exposition
No exposition so far. In the Form Engine, you see a text input with an icon button for this data
61

-- 61 of 147 --

type. Clicking the icon button will trigger a date range picker.
Figure 50. Date Range Picker
The picker is only supported for date ranges of fully defined dates (year, month, and day). For
ranges of date fragments a text input without the button is shown.
Enumeration Type
Exposition
• compact (default): renders the Control as a drop-down list. If the field is not required or no
initial value is selected, an empty option is implicitly added to the list of enumeration
options.
• autocomplete: renders the Control as a drop-down list and provides autocompletion
• full: renders the Control as a vertical radio button list
• inline: renders the Control as a horizontal radio button list
Different Expositions for an Enumeration Control shows the drop-down box for the
exposition field of an enumerations field.
62

-- 62 of 147 --

Figure 51. Different Expositions for an Enumeration Control
Figure 52. Preview of All Expositions for Enumerations in a Standard Screen
WARNING
Mind that Expositions are defined per Control and therefore all settings
are provided for each reference of a field. This means if you are using
the same field within Inline and Detached Repeat the set Expositions is
the same but will be displayed differently. Within a Screen, also for the
Detached Repeats Details Screen, all four Expositions can be displayed.
In an Inline Repeat table structure all Expositions other than
"autocomplete" are shown as a "compact" drop-down list within the row.
Confirm Type
Exposition
• Checkbox (default): renders the Control as a checkbox
• Switch: renders the Control as a switch
• Switch-With-Values: renders the Control as a switch with a label for the truthy option, for
example, "yes" for English localization. For the no value option no label is rendered by
default, but could be provided via customization using the localization API.
If the Exposition is set to Switch, there are two additional fields shown to configure the Control
even further, first Label Placement which allows setting the label to be above the control, left of the
control or right of the control, second Switch Icon, which allows using any Icon as the icon shown
for a checked switch. For Switch with Values, only the Switch Icon is configurable.
63

-- 63 of 147 --

Attachment Type
Attachments are groups from a data point of view. However, they can be used in two ways:
1. Standard Use Case
In most cases, the attachment group should be treated like a field and be used to create a
Control or repeat column. It will then be displayed as a File Picker. When you click it, the
native file select dialog will pop up and you can select the file you like to attach.
Mind that the default behavior is to save the data into the document and it is highly
recommended to use the custom AttachmentHandler for bigger data to store the attachment
outside the document.
2. Special Use Case
The fields of an attachment group can also be used individually to create Controls or columns.
However, changing values like the content or id of an attachment might lead to undesired
behavior. Therefore, such Controls and columns will be automatically set to read-only with the
read-only presentation "Text Output", when they are created. These settings can be adapted
manually after creation. However, except for the category and description of an attachment, it
is strongly recommended to keep these fields read-only.
Figure 53. File Picker
Images will be displayed directly within the model if the exposition is not set to compact. All
other files can just be downloaded.
NOTE
Limitation: The tiff image format is not supported by browsers. Therefore, only
the image placeholder icon will be shown when uploading images with this file
type.
Figure 54. Image Attachment
In a popup menu, you find the Replace, Download and Delete option.
64

-- 64 of 147 --

Figure 55. Image Attachment With Popup Menu
Exposition
NOTE
Note that the exposition for attachment inputs has to be set individually for
every Control/column (in contrast to other data types, where the same exposition
is used for all inputs that reference the same Form Model element).
You can pick one of the following options:
• file preview (default): Renders the attachment as a File Picker, which will show a
thumbnail for uploaded image files. If the uploaded attachment is not an image or no valid
thumbnail could be retrieved by the AttachmentHandler, an icon will be shown. This icon
will be determined by the MIME type of attachment.
• compact: Renders a compact version of the File Picker, which only shows the name of the
uploaded file and an icon for the corresponding MIME type. No placeholder or preview
images will be displayed.
For attachment columns there is an additional exposition option:
• thumbnail or icon: Renders a thumbnail for uploaded image files. If the uploaded
attachment is not an image or no valid thumbnail could be retrieved by the
AttachmentHandler, an icon will be shown. This icon will be determined by the MIME type of
attachment.
NOTE This exposition is only used for the display of uploaded files. If no file has been
uploaded yet, nothing will be shown.
Attachment Settings
These settings are exclusive to attachments and cannot be edited for other data types. They are
saved in the field configuration entry for the underlying attachment group. Therefore, these
settings affect every input for the corresponding attachment.
65

-- 65 of 147 --

Figure 56. Attachment Settings
Placeholder Icon
The placeholder icon that is shown for an attachment input, if no file is uploaded yet. The
available options for placeholder icons are
• Default (This option will be used if there is no placeholder icon set in the model)
• Image
• Text
• Spreadsheet
• PDF
• Video
• Sound
• None
Default Action
The default action that will be executed when clicking on an attachment input after a file has
been uploaded. The available options are
• Download: Clicking on the attachment upload will automatically trigger a download of the
given file.
• Replace: Clicking on the attachment upload will open a dialog to upload a new file. This is
the default behavior.
NOTE
The default action Replace will only be executed if the input is editable.
When the input is readonly, this setting will be overridden and a download
will be triggered instead, because it is the only possible action in this case.
This will happen in any of the following cases:
◦ The Control/column is set to readonly.
◦ A parent element of the Control/column is set to readonly.
◦ The attachment is shown in a column of a Detached or Embedded Repeat
66

-- 66 of 147 --

overview table.
Accepted MIME Types
With the accept property you can provide a comma-separated list of MIME types that should be
accepted by inputs for this attachment group (for example, image/jpeg, video/*,
application/msword, …).
NOTE
It is important to note that this will not actually restrict the file types that can be
uploaded by the user but only affects the file types that are initially suggested to
the user when opening the file upload dialog.
Multi-Select Type
Exposition
The Multi-Select supports the following expositions:
• autocomplete (default): renders the Control as an auto-complete in which multiple entries
can be selected
• full: renders the Control as a vertical checkbox group
• inline: renders the Control as a horizontal check-box list
Figure 57. Different Expositions for a Multi-Select
When the exposition is set to full or inline you can set the Enable select all option. In this
case, the checkbox group will show a checkbox for selecting and de-selecting all options at
once.
If a hint should be defined for the Multi-Select, it can be done in the usual way in the Form
Model Editor global or per Control.
If the hint or a helper text should already be defined in the Document Model, it can be edited
in the Document Model Editor on the 'value' field of the Multi-Select.
WARNING For columns of data type multi select all Expositions other than
"autocomplete" are shown as a "compact" drop-down list within the row.
67

-- 67 of 147 --

Show as comma-separated list
This setting is only available for Multi-Selects as repeat columns. It will show the multi-select
values as a comma-separated list instead of bullet points. For Inline Repeats, it is also required to
set the "Readonly presentation" to "Text Output".
External Enumeration for Multi-Select
It is also possible to add an external enumeration to a multi-select group if its value field is of type
String. In this case, as described in External Enumeration, the value field can be clicked in the Data
Configuration View to apply the external enumeration. Note that when applying the external
enumeration, only the Source URL can be specified. This is because the exposition is set by the multi-
select itself.
NOTE
If such a multi-select with a value field of type String is used somewhere in the
form, specifying an external enumeration for it is mandatory. Without it, a
consistency problem will be reported.
Custom Type
Exposition
No exposition so far.
Text Cell
The Form Model Editor provides text cells that allow the user to add custom text displayed read-
only in the web interface. Text cells are organized in Control Grids. They can be added using the
menu of Rows. The dialog is shown in Add a New Text.
68

-- 68 of 147 --

Figure 58. Add a New Text
Decoration
A display variant for the text can be chosen in the drop-down.
• None
69

-- 69 of 147 --

• Info
• Warning
• Success
• Error
Text
The text message for each locale in case of multilingual web interfaces can be entered. They
allow entering a multi-line text which can be formatted using standard HTML syntax.
NOTE Accessibility test recommended! For further information look into the Get A12
Documentation for Accessibility.
Button
Buttons allow the user to trigger certain actions.
They can be displayed in different places:
• within a form Screen: defined as the children of a Button Panel
• in the subheader or footer of a specific Screen: defined in the Screen editor.
• visible in the subheader or footer of every Screen: created in the Settings.
The global buttons are always shown on the left of the Screen specific buttons.
70

-- 70 of 147 --

Figure 59. Subheader and Footer View
Subheader buttons can be modeled in two lists - one for major buttons and one for minor buttons.
In the resulting form, the navigation buttons of both lists are mixed together in a tabbed
navigation bar above the form content. For more information, see the Form Engine documentation.
The event buttons are shown in an action bar below the tabbed navigation bar. The event buttons
from the major list are shown in a left-aligned group, the buttons from the minor list in a right-
aligned group.
Footer buttons can be modeled in a minor and major list as well. In the footer, navigation buttons
and event buttons are shown together and not separated into different "bars". Here, the minor
buttons are left-aligned and the major buttons right-aligned.
Furthermore, the buttons in the subheader and footer area will be collapsed into popup menus if
there is not enough space available.
To create a button visible on all form Screens:
• go to Settings
• open the Subheader and Footer tab
• click Add under the respective list
71

-- 71 of 147 --

• fill out the details for the new button in the following dialog and click Commit
72

-- 72 of 147 --

Figure 60. Edit Button
73

-- 73 of 147 --

To create a button visible on one Screen:
• click on the Screen to open its editor
• open the Subheader and Footer tab
• click Add under the respective list
• fill out the details for the new button in the following dialog and click Commit
The created form buttons and Screen buttons can be reordered inside their lists. This is possible
using the Move Down and Move Up buttons. There is also an option using the three dotted menu to
move buttons between the lists.
Button Details
The following properties can be configured for a button:
Name
Enter a name for the button. It must be unique within its button panel or header/footer.
Button Functions
Buttons have different parameters depending on the button type.
Button Type
In the Form Model we differentiate two types of buttons:
• Event: Event buttons fire a (custom) event that can be handled in the application embedding
this form. The parameter Event Name is used in the (custom) code that handles this event.
There are three event names available by default. These are automatically handled by the
Client CRUD extension. For more information on the semantics of these events, refer to the
chapter about the CRUD extension within the Client documentation.
• Navigation: Navigation buttons allow navigating to other Screens. The "target" parameter
defines the Screen to navigate to. This can either be the previous or next Screen or it can be a
specific Screen that has been set up within the Screens View of the Form Model Editor (see
Screens View).
74

-- 74 of 147 --

Figure 61. Navigation Button
Validation-Mode
Buttons can specify different validation behaviors.
• No Validation: No validation is triggered on a button press.
• Partial Validation: A validation of relevant elements on the current Screen is executed with
a button press. If the validation result contains at least one parsing error or validation error,
the default button behavior is not triggered.
• Full Validation: A validation of the entire document is executed with a button press. If the
validation result contains at least one parsing error or validation error, the default button
behavior is not triggered and a validation bar will be displayed.
NOTE
Validation rules may also produce warnings or info messages rather than
errors. These do not block the button behavior directly. Instead, a
confirmation dialog may be shown to the user, allowing them to
acknowledge the messages and proceed or cancel the action. This
confirmation behavior is controlled by the Rule Confirmation Settings in the
Model Settings. See the Disable Rule Confirmation Dialog property for details
on suppressing the confirmation dialog for warning and info level messages.
NOTE
The different validation modes only consider data, that was not marked as
non-relevant via dependencies.
While a full validation executes all Validation Rules, that were defined in the
Document Model, a partial validation only executes a subset of these rules.
This subset depends on the set of fields, that is considered as relevant for the
current Screen.
Usually, a field is considered as relevant for the Screen if one of the following
conditions is met for every referenced field of a rule:
◦ There is a visible Control for the field on the current Screen
75

-- 75 of 147 --

◦ There is a visible Inline Repeat column for the field on the current Screen
◦ The field is set to global in the Document Model
For a more detailed description of the validation behavior and how the
relevant fields are determined, refer to the Form Engine documentation.
Scope
Buttons can be set to be hidden or disabled, depending on the read-only state of the form. By
default, buttons will always be enabled, but will only be shown, if the form is not set to read-
only. Therefore Hide Button in Readonly Mode will be preselected, when creating a new button.
• Always: The button will always be shown and enabled.
• Disable Button in Edit Mode: The button will be disabled if the form is in edit mode, that is,
not set to read-only.
• Disable Button in Readonly Mode: The button will be disabled if the form is in read-only
mode.
• Hide Button in Edit Mode: The button will be hidden if the form is in edit mode.
• Hide Button in Readonly Mode: The button will be hidden if the form is in read-only mode.
No data change
(Only available for buttons of type Event)
This option defines how the button should be displayed if no data changed in the form yet.
• Disable Button: The button will be disabled button as long as the data did not change.
• Hide Button: The button will be hidden as long as the data did not change.
• Show enabled Button (default value): The button will always be shown and enabled.
If one of these options is selected, the button will be enabled/shown as soon as the user
entered some input in any of the Controls in this form and leaves the current Control. Once
the button is enabled/shown it will not be disabled/hidden again as long as this form is
opened.
Visual Settings
All buttons are displayed as Secondary and non-Destructive per default and will appear as a blue
text link. This default value can be changed in the Visual Settings
Priority
Sets the style of the button to either Secondary or Primary.
Destructive
Sets the color of the button. If destructive is ticked then the corresponding button is displayed in
red. This might be useful to amplify the button’s action, for example, a red Cancel button.
76

-- 76 of 147 --

Figure 62. Button in Preview
Icon
Sets the icon which is shown next to the button label. An icon picker allows selecting the
available icons. Different icon themes are available. Consult the A12 Widgets documentation.
Figure 63. Button With Icon
Hide Label
The label of the button can be hidden with the hide label option. This will only visually hide the
label, but it is still available for accessibility tools.
For accessibility reasons and in cases where due to missing space the button is moved into a
popup menu, you should always provide a label for the button.
Furthermore, an icon needs to be given when hide label is selected. Otherwise, the button would
be invisible in the form.
NOTE
When the label is hidden with hide label and no description is given, the label is
also used in the button tooltip on hover.
For Navigation Buttons, when no label is given, neither in the model nor through
the localization API, and the target Screen has a label, then this is used as fallback
label for the Button.
Description
A multilingual description of the button. If set, it is used in a tooltip when hovering over the button.
It is also available for accessibility tools, for example, contained in the text read by screen readers.
Button Panel
Button panels are a container element for buttons. They can be added to Screens, Sections and
Multi-Column Sections. The Edit Button Panel shows the dialog for editing the button panel
"Navigation" which contains three buttons: Validate, Save, and Submit.
77

-- 77 of 147 --

Figure 64. Edit Button Panel
Buttons within a button panel will be left aligned per default. If you like to change the alignment it
is not done via minor or major button lists. This can be done by using Styles, for example,
h_floatRight (also see Styles).
Expressions
Expressions are a solution for displaying field values as read-only information. In addition to single
values, such as read-only table cells in repeats, they can also be used to create complex composites.
For more details about this topic, refer to the Expression Documentation.
There are different variants of Expressions:
78

-- 78 of 147 --

• An "Expression Cell" can be placed inside Control Grids next to Controls and text cells.
• An "Expression Column" can be defined on Repeats as a repeat column, similar to field-based
columns.
• An "Expression Label" can be set for most model elements:
◦ Screens
◦ Sections
◦ Multi-Column Sections
◦ Control Grids
◦ Rows
◦ Controls
◦ Expression Cells
◦ Button Panels
◦ Buttons
◦ Repeats
◦ Row Actions
◦ Field Overview Columns
◦ Expression Overview Columns
• Furthermore, an Expression can be used to set a permanent filter for repeats (see Filter
Expressions).
NOTE
When defining an Expression, the following two scenarios must be distinguished:
1. The expression is defined as an expression cell in a Control Grid or as an
expression label, which is not placed inside a repeat. In this case, when
referencing fields, the absolute reference to a field must be set using kontext
statements.
2. The expression is defined as an expression column on a repeat, as an expression
cell in a Control Grid inside a repeat, or as an expression label inside a repeat,
such as in a Detached Repeat Detail Screen. In this case, only relative field
references to fields of the repeat’s referenced repeatable group or further
nested child groups are supported. The repeat’s group is used automatically as
the scope of the expression; therefore, absolute field references are not valid.
WARNING Expression labels should not contain line breaks or paragraphs, because they
will not be displayed correctly if the expression is rendered as a label or title.
Creating a new Expression:
• On a Control Grid row:
◦ Right-click on the row and click on Expression from the context menu or use the keyboard
shortcut Alt+X.
79

-- 79 of 147 --

• On a Repeat:
◦ Right-click on the repeat and click on Expression Column or use the keyboard shortcut Alt+O.
Figure 65. Add Expression From Context Menu
This will bring up the following dialogs:
For Expression on Controls:
80

-- 80 of 147 --

Figure 66. Add an Expression for a Control
For Expression on Columns:
81

-- 81 of 147 --

Figure 67. Add an Expression for a Repeat From Context Menu
82

-- 82 of 147 --

Figure 68. Add an Expression for a Repeat Column
NOTE Accessibility testing is recommended. For further information, refer to the Get A12
Documentation for Accessibility.
83

-- 83 of 147 --

Formatting of Expressions
The formatting of Expressions can be done according to the Markdown syntax. All formatting
instructions must be inside quoted text. Detailed information can be found in the Expression
Documentation.
WARNING Using formatted Expressions for any titles and/or labels will break accessibility!
NOTE
Markdown behaves differently if empty strings are wrapped with the formatting
types mentioned above. Thus, empty field values should be excluded with a case
statement. E.g.:
case[stringField] != "" {"**" [stringField] "**"}
Examples
"for " [packagingAmount] " " [packagingMaterial] " " [packagingType] " = "
"**"[shippingCosts]"**"
Then it looks like this:
Now let us have a look at a more comprehensive example of an Expression.
The Form Model could look like this:
84

-- 84 of 147 --

Figure 69. Example of a Form Model using various Expressions
The definition of the example Expressions are:
"controlGridExpressionString"
kontext(top){[string]}
"2ndControlGridexpressionEnum+Datetime"
kontext(top) {
kontext(sub) {
case [enum] = "sun" {
kontext(subsub) {
[datetime]
}
}
}
}
85

-- 85 of 147 --

"repeatExpressionString"
(en:"value: ",de:"Wert: ")[string]
Figure 70. Result of the Expression in the Preview
Sometimes referencing to a group might get tricky, so here is a complex Expression with several
conditions and usage of nested repeatable groups.
Mind that referencing only works downwards, not upwards.
Figure 71. Model with Nested Repeatable Groups
86

-- 86 of 147 --

Example for Expression:
kontext(subsub1) {
case [enum1] = "1" {
kontext(subsubsub1) {[enum3]}
}
case [enum1] = "2" {[enum2]}
case [enum1] = "3" {"-"}
case [enum1] = "4" {
kontext(subsub2) {
kontext (subsubsub2) {[number1]}
}
}
}
Figure 72. Preview of Expression with Several Cases
Depending on the selected enumeration value from "enum1" a different resulting Expression value
will be shown. This displayed value might be a constant or a value coming from another field.
Repeats
Based on a repeatable group data structure, which can be defined at the group level within the
Document Model, the Form Model Editor makes it possible to easily generate complex user
interfaces that allow you to create, edit, and delete repeatable data sets, such as one or multiple
addresses. These user interfaces are called Repeats. There are three different types of Repeats: the
Inline Repeat, the Detached Repeat, and the Embedded Repeat. Their differences are explained in the
following chapters.
An overview table is displayed for all repeatable items. Depending on the configuration of the
Repeat columns, all or some of the data of the repeatable data structure will be displayed with
multilingual headers. The values in the overview table can be provided in several ways. They can
be shown using a direct field reference or by using an Expression (see Expressions). For empty
repeats or tables, a default message There are no entries yet will be displayed.
Furthermore, the available row actions will be displayed in an additional action column at the end
of each Repeat row. These row actions are also available in a context menu that can be accessed
87

-- 87 of 147 --

with a right-click on the corresponding row.
NOTE The Row Action context menu will not be opened by right-clicking an input Control
or a Row Action button. The browser context menu will be opened in these cases.
Before repeats can be created in the Form Model, the respective repeatable groups must be defined
in the Document Model, as shown in Repeatable Groups in the Document Model Editor. In square
brackets behind the group name, you can see the maximum number of instances of the data sets
that can be created.
For all repeat types, it is required that all referenced fields are located in the same repeatable
group, including subgrouping below the repeatable group level (see also nested repeats in Nested
Repeats).
Figure 73. Repeatable Groups in the Document Model Editor
More details on how to model repeatable structures are provided in the Document Modeling
documentation. Once the repeatable group is present in the Document Model, you can follow the
instructions in Add/Edit a Repeat.
Inline Repeat
With Inline Repeats, all data is edited directly inside the overview table, that is, without the need to
click edit in the user interface. An example of the UI is shown in User Interface for Inline Repeats.
In that example, the Repeat has the label All Persons.
Figure 74. User Interface for Inline Repeats
Screen View With an Inline Repeat shows the Screen Editor after an Inline Repeat has been created
and four fields have been added to it.
88

-- 88 of 147 --

Figure 75. Screen View With an Inline Repeat
NOTE
With a growing number of columns or rows, Inline Repeats can significantly slow
down the entire form, especially on weaker devices such as smartphones. To
mitigate the slowdown:
• Keep the number of columns low
• Use pagination with a small page size
• Activate infinite scrolling
Detached Repeat
As mentioned above, for modeling Detached Repeats in the Form Model, the required repeatable
groups must have been defined in the Document Model first. For Detached Repeats, all data can
only be edited inside a Detail Screen, which results in a separate form. Committing the changes in
this form will trigger a partial validation of its content.
NOTE Note that some Validation Rules might not be executed when committing the
changes on a Detail Screen. See Validation Mode for more information.
Screen View With a Detached Repeat shows an example of a Detached Repeat with a Detail Screen
that contains a Control Grid and several Controls.
89

-- 89 of 147 --

Figure 76. Screen View With a Detached Repeat
Detail Screen of a Detached Repeat in the User Interface (Preview) shows what the Detail Screen of
the above example Detached Repeat looks like in the preview web application.
Figure 77. Detail Screen of a Detached Repeat in the User Interface (Preview)
Detached Repeat in the User Interface (Preview) shows the overview table of a Detached Repeat.
Because values cannot be changed on this overview, they are displayed with their read-only text
output presentation.
90

-- 90 of 147 --

Figure 78. Detached Repeat in the User Interface (Preview)
Detached Repeat Detail Screen
The Detached Repeat Detail Screen can contain the same elements as a top level Screen. The
following is just an example.
1. Add a Control Grid to the Detail Screen by using the context menu and selecting, for example,
Control Grid or using the keyboard shortcut.
2. Now Controls can be dragged from the Document Model section of the Data Models View onto the
Control Grid that has been created in the previous step. Mind that it is not possible to add
Controls to a Detached Repeat Detail Screen without a Control Grid.
3. Finally, the Columns that should be visible in the Detached Repeat overview have to be added to
the Detached Repeat element:
◦ either by dragging the respective fields from the Document Model in the Data Models View
onto the repeat. They will be displayed in the Screens View with a numbered prefix like "#2:"
for the second element.
◦ or by creating an Expression Column using the repeat’s context menu (see Expressions)
Embedded Repeat
Embedded Repeats can be seen as a hybrid of Inline and Detached Repeats. They display repeatable
data in the overview table in a read-only fashion just like Detached Repeats.
Embedded Repeat in the User Interface (Preview) shows the overview table of an Embedded Repeat.
Figure 79. Embedded Repeat in the User Interface (Preview)
However, when the user clicks to edit a set of data an editable row is shown which allows
modification of the values of that data set. The editable row offers a Close button to close the row
again, as well as all Row Action buttons for this Repeat.
91

-- 91 of 147 --

Figure 80. Open Editable Row of an Embedded Repeat in the User Interface (Preview)
The content of the editable row is a Control Grid that is automatically created together with the
Embedded Repeat and which cannot be removed. This means that nesting is not possible with this
type of Repeats since Control Grids cannot contain Screens, Sections, or further Repeats.
Figure 81. Screen View With an Embedded Repeat
Add and Edit a Repeat
• Add: Initially you have to create an Inline Repeat. Detached Repeats and Embedded Repeats
92

-- 92 of 147 --

cannot be created on their own.
◦ Just drag & drop a repeatable group from the Document Model in the Data Models View onto
the Screens View. An Inline Repeat named {NameOfGroup}_Repeat is shown in the Screens
View. This name can be changed.
• Edit: Once you have created a Repeat, it appears in the Screens View. By clicking on it, a view
with all properties appears. Now you can edit your Repeat.
◦ To convert the Repeat type the user has to open the context menu of the Repeat and select
the corresponding action inside the Convert section. The Repeat will automatically be
converted to the selected type.
◦ For Detached Repeats, a Detail Screen will be created automatically.
◦ For Embedded Repeats, the Control Grid of the editable row will be created automatically.
◦ When converting from Embedded or Detached Repeats to Inline Repeats, only the columns will
be reused. The remaining content is discarded, after a confirmation warning was shown.
◦ When converting from Detached to Embedded Repeats, the cells of all Control Grids on the
details screen will be reused in the Control Grid of the Embedded Repeat. The remaining
content, including the Control Grids and their rows, are discarded, after a confirmation
warning was shown.
◦ Columns that refer to a field from an attachment group, will be automatically set to read-
only with the read-only presentation "Text Output" when converting to an Inline Repeat.
93

-- 93 of 147 --

Figure 82. Repeat Properties
94

-- 94 of 147 --

Repeat Settings
Type
Type of the repeat (Detached, Embedded or Inline). This can be changed in the context menu as
described in Add and Edit a Repeat
Repeatable Group Path
Path of the repeatable group from the Document Model. This is not editable.
Name
Name of the repeat in Screens View.
Enable Columns Resize
Enable the resizing of Repeat columns. If this feature is checked, then the width of columns in
this Repeat will not be stretched, except for the last column in the non-pinned area, which will
take the remaining space.
Initial Sorting
This property specifies the sorting of the repeat columns. More about the Sorting and Filtering of
columns is explained in Column Settings, Filtering and Sorting in Repeats
Initial Rows
When editing an Inline Repeat you can define how many initial rows (None, 1-10) should be
displayed for this Repeat when it is initially created. These initial rows will be filled with the
initial values of the Repeat columns. Therefore, all rows will be initialized with the same values.
If initial rows are defined for a nested Inline Repeat, these rows will be created as soon as a new
row is added to the parent Repeat. They will not be created upon document creation.
The modeler needs to make sure that the initial rows of a Repeat are aligned with the validation
of the model - for example, the number of initial rows should not exceed the maximum
repeatability of the underlying group.
NOTE
The number of initial rows can only be defined for Inline Repeats and will be
saved for the underlying group of the Repeat. Therefore, this setting affects all
Repeats referencing this group. If a number of initial rows are set for a group, it
is not allowed to have a Detached/Embedded Repeat that references the same
group.
Converting an Inline Repeat to a Detached/Embedded Repeat will automatically
remove the number of initial rows from the group configuration as long as no
other Inline Repeat references the same group.
Read-only
Sets a repeat to read-only. All data cannot be edited or added because the "Add"-button will be
hidden.
Readonly Presentation
Defines the read-only presentation for field columns in an Inline Repeat. This setting can be
95

-- 95 of 147 --

overwritten for individual columns in the corresponding column dialog.
• Default: Uses the read-only presentation defined in the model. If no presentation is defined
in the model, it renders read-only inputs for the columns that are read-only.
• Input: Renders a read-only input field for columns, which are readonly.
• Text Output: Renders plain text for columns, which are readonly.
The read-only functionality for Repeats is just to display repeatable data structures for
information purposes. Additionally, a "view" icon for Detached Repeats replaced the "edit"
icon. The "view" icon is representing a square with an arrow pointing to the top right and
can be seen in Read-Only Repeat With "View" Icon. This view button opens a non-editable
Detail Screen with the existing data entries.
Figure 83. Read-Only Repeat With "View" Icon
Figure 84. Details Screen of Detached Repeat After Clicking the "View" Icon
96

-- 96 of 147 --

Repeat Entries
Here you will find some row actions that come predefined for general actions:
• Add
• Remove
• Reorder
• Copy
Checked row actions will be displayed in the Form Engine.
Selecting "Reorder" for a repeat will also enable the drag-and-drop feature, allowing you to move
rows inside the repeat by dragging them to a new place.
NOTE You can only use Copy when Add is also selected.
Figure 85. Buttons for a Row in a Repeatable Structure
NOTE
When using a filter expression the reorder button is hidden, since the shown data is
only a subset of the real data in the document. Reordering (using the buttons or by
dragging) does not work with filter expression! The same is true when using normal
filters or sorting.
Row Actions
In addition to the Repeat Entries, you can specify customized Row Actions, that is, actions affecting
the data displayed in the corresponding Repeat row, which are not covered by the entries Add,
Remove, Reorder, Copy.
A new custom Row Action with a customized icon can be created by clicking on the Add button
within the Row Actions section.
97

-- 97 of 147 --

Figure 86. Add Row Action for Repeat
98

-- 98 of 147 --

Button Functions
In this section the functionality of the Row Action button can be defined.
Event
Name of the event that is triggered by the button.
Scope
Row Action buttons can be set to be hidden or disabled, depending on the read-only state of the
form. By default, Row Action buttons will always be shown and enabled. Therefore Always will
be pre-selected when creating a new Row Action.
• Always: The button will always be shown and enabled.
• Disable Button in Edit Mode: The button will be disabled if the form is in edit mode, that is,
not set to read-only.
• Disable Button in Readonly Mode: The button will be disabled if the form is in read-only
mode.
• Hide Button in Edit Mode: The button will be hidden if the form is in edit mode.
• Hide Button in Readonly Mode: The button will be hidden if the form is in read-only mode.
Confirmation title (Multilingual)
When the Row Action requires a confirmation by the user, the title of the confirmation dialog
can be defined here.
Confirmation message (Multilingual)
Message shown in the confirmation dialog.
Figure 87. Customized Row Action in Repeat
Visual Settings
All Row Action buttons are displayed as Secondary and non-Destructive per default and will appear
as a blue text link. This default value can be changed in the Visual Settings section.
Priority
Sets the style of the button to either Secondary or Primary.
Destructive
Sets the color of the Row Action button. Destructive buttons are displayed in red.
Icon
The icon representation is the recommended way for Row Action buttons in order to save space.
An icon picker allows selecting the available icons. Different icon themes are available. Consult
the A12 Widgets documentation.
99

-- 99 of 147 --

Hide Label
The label of the button can be hidden with the hide label option.
This will only visually hide the defined label, but the label is still available for accessibility tools.
For accessibility reasons and since it is used in the context menu of the repeat row, you should
always provide a label for the button.
NOTE If you want to hide the label of a Row Action button, you need to provide an icon.
Label
For accessibility reasons and also since it is shown in the context menu entry, you should always
provide a label for the Row Action.
Description
A multilingual description of the Row Action. If set, it is used in a tooltip when hovering over the
Row Action button. It is also available for accessibility tools, for example, contained in the text read
by screen readers.
Default Row Action
Event
You can specify which row action should be executed when a user clicks on a Detached or
Embedded Repeat row by selecting an action from the drop-down. The drop-down will show an
entry for:
• "Edit/View"
• "Download" if multi-file upload is enabled for the repeat (see Multi File Upload)
• All row actions you created which do not specify a confirmation message
"Edit/View" will open the row in an editable or read-only mode depending on the Repeat and
the engine state. "Download" will automatically trigger a download of the attachment
associated with the Repeat row.
NOTE
The default Row Action event will not be triggered if the user clicks on an
interactive element in the corresponding row such as Row Action buttons or
attachment inputs. A click on such elements will trigger the action of that
element instead.
Hide Button
You can decide whether the corresponding Row Action button should be hidden in the action
column and in the context menu of the Repeat.
NOTE
It is recommended to hide the additional Row Action button to make your
application accessible. Otherwise, screen readers will read the same action twice
when using tab navigation.
100

-- 100 of 147 --

Table Size
You can specify whether pagination or infinite scrolling should be used to display a repeat table.
Furthermore, additional settings can be made to configure the size of the rows and the Action
column:
Pagination
If no infinite scrolling should be used, but your Repeat list is getting too large, you can provide a
page size to enable pagination.
Page Size
The Page Size value defines after how many entries the page break is triggered. This is even
provided for Repeats that are set to read-only. The pagination will be adapted when filtering and
sorting.
Figure 88. Repeats With Pagination
For detailed information about the Detached Repeat or the Inline Repeat read chapters Detached
Repeats and Inline Repeats
Infinite scrolling
As an alternative to pagination you can also use infinite scrolling by enabling this check box.
With infinite scrolling enabled, the content of the Repeat appears as a list that can be scrolled
from the first to the last entry.
Figure 89. Infinite Scrolling Properties
When infinite scrolling is enabled, you also need to set the height of the table to a fixed value:
101

-- 101 of 147 --

Table Height in px
The height of the Repeat table in pixels. This setting must be set if you use infinite scrolling,
otherwise, it is optional.
Row Height in px
The height of the Repeat table row in pixels. This setting must be set if you use infinite scrolling,
otherwise, it is optional. Infinite scrolling can only be used without issues if the Repeat rows
have a fixed height.
You need to define this height to fit best to the type of cells you have. As an example, Expressions
with multiline output or string fields with the area exposition need more space than a single line
input Control.
WARNING
Setting the row height to a value that is smaller than the minimum required
value may lead to rendering issues. It is recommended to check this setting
in the production environment to avoid these issues.
NOTE
The following values show the minimal required height for single line inputs
based on the used Widgets theme:
• Default: 33px
• Compact: 29px
• Flat: 49px
• Flat Compact: 29px
Action column width
Defines the width of the action column in which the row actions (for example, delete, copy, or
custom actions) are shown. You only need to set the property if you use infinite scrolling and the
number of row actions differs between the rows (for example, because there are dynamically
hidden or shown). With infinite scrolling, the repeat table does not access all rows at once and
thus cannot calculate the maximum width of the action cell to avoid a size change of the column
while scrolling. The width can be defined by setting a value of at least 0.3. Steps of 0.1 can be
used. When using the default plasma styling, 1.0 is equivalent to 150 pixels. Accordingly, a width
of 0.5 is equivalent to 75 pixels and 2.5 is equivalent to 375 pixels.
Card Height in px
The height of a card (when configuring the Form Engine for card view) in pixels. This setting is
only applied if you enable the card view in the configuration of the Form Engine and will
overwrite the row height in that case.
CAUTION
Infinite scrolling can only be used without issues if the Repeat rows have a
fixed height. Therefore, infinite scrolling is not allowed for Embedded Repeats,
because its expandable rows can dynamically change their height.
Furthermore, with infinite scrolling enabled, you should use "Validation
messages as tooltips" for your columns to assure a fixed row height.
102

-- 102 of 147 --

Multi File Upload
Multi-file upload can be enabled for Embedded Repeats and Inline Repeats if the underlying
repeatable group contains exactly one attachment group, which has to be non-repeatable and is not
nested in an additional repeatable group. If the multi-file upload is enabled for a Repeat, a file
upload area will be rendered above the repeat table, where users can upload multiple files at once.
Similar to other attachment inputs, files can either be picked in a file picker dialog after clicking the
upload area or they can be added via drag and drop.
For every successfully uploaded file a Repeat row, containing the respective attachment, will be
automatically added. When a user tries to upload a file with a name that already exists, a dialog
will be shown to decide if the file should be skipped, replaced, or uploaded as a copy with a
modified name.
Figure 90. Repeat With Multi File Upload Enabled
NOTE An attempt to upload more files, than the repeatability allows, will result in an
error message.
The following additional settings are available if the multi-file upload is enabled:
103

-- 103 of 147 --

Figure 91. Multi File Upload Settings
Attachment Group
The path to the attachment group in the Document Model. This path will be automatically set in
the Form Model Editor and cannot be changed.
104

-- 104 of 147 --

Show Download Button in Row Action
Shows a download Row Action button. Just like any other Row Action, this button will be
rendered in the action column and in the context menu of the Repeat. Clicking the button
triggers the download of the attachment that is associated with the current Repeat row.
Description
A localized text that will be shown inside the upload area.
Hide Description
If the description should not be shown, it can be visually hidden via this setting. It will still be
available for accessibility tools.
Button Text
The localized label of the upload button.
Hide Button Text
If the button text should not be shown, it can be visually hidden via this setting. It will still be
available for accessibility tools.
Helper Text
A localized helper text that will be shown below the upload area.
Accepted MIME Types
With the accept property you can provide a comma-separated list of MIME types that should be
accepted by inputs for this attachment group (for example, image/jpeg, video/*,
application/msword, …).
NOTE
It is important to note that this will not actually restrict the file types that can be
uploaded by the user but only affects the file types that are initially suggested to
the user when opening the file upload dialog.
Filter Expression
All repeats support a filtering of the rows that are displayed. By doing this, different views on the
same underlying repeatable group can be provided. For example, when editing a repeatable group
containing both foreign and domestic addresses, one Repeat could filter for foreign and another for
domestic address rows.
Since Filter Expressions could be seen as a subset of Expressions the syntax for them is similar.
• In order to specify a Filter Expression for a Repeat, first open its detail view (click on the model
element in the Screens View). Then fill in a filter rule in the field Filter Expression. A Repeat can
only be filtered by a single field’s value.
◦ Example for a string field with the name "Country". This will list only the data entries in
which the field Country equals DE. [Country] = "DE"
◦ Example for group scope for an Enumeration field with the name "NumberEnum" defined in
the group "MyFieldGroup": kontext(MyFieldGroup) { [NumberEnum] } = "THREE"
105

-- 105 of 147 --

◦ Example for the nested group: kontext(TopGroup){ kontext(SubGroup) {[Date]}} = "2018-05-
25"
Accessibility
Repeats support accessibility by having their label programmatically connected to their table. This,
for example, allows screen readers to read the label of the table.
Hide Label
If the design of the web form requires that the label of a Repeat shouldn’t be visible, it can be
hidden with the 'Hide Label' option. This will only visually hide the defined label but still keep it
available for accessibility tools.
Screen Reader Column
By default, row action labels in repeat rows will only contain their event name (like "delete" or
"edit"). To allow screen readers to provide context about a specific row, this property can be
used. If set, the value of the cell (defined by the current row and the given column) will be
available for screen readers.
NOTE
The intention of this feature is that screen readers can read a "unique" text for row
actions that would otherwise have a generic one that is not distinguishable between
different rows (for example, the delete action). Since the uniqueness is achieved by
reading the cell value in addition to the action text, it is therefore not recommended
to use columns of type Expression (as these might contain HTML markup) or types
that have a very limited set of values (like a Boolean column)
Column Settings, Filtering and Sorting in Repeats
For all Repeat types, you can change some column properties like its icon, width, filtering, and
sorting.
1. Double click on a repeatable field
2. Go to Column Settings
106

-- 106 of 147 --

Figure 92. Column Settings of a Repeatable Field
An icon can be specified in the Column Settings of each field. This icon will be shown in the Repeat
table head, together with the column label. Additionally, Hide Label can be selected. In this case,
only the icon - if specified - will be displayed in the Repeat table head. Note that hiding the label and
not specifying a label text are not the same. The hidden label will still be part of the form to provide
accessibility.
Furthermore, the Column Width can be defined by setting a value of at least 0.3. Steps of 0.1 can be
used. The default setting for the column width is 1.0. When using the default plasma styling, 1.0 is
equivalent to 150 pixels. Accordingly, a width of 0.5 is equivalent to 75 pixels and 2.5 is equivalent
to 375 pixels.
For non-pinned columns, the value defines a minimum width if the option Fixed Width is not set. If
there is more space available, all non-pinned columns will grow proportionally according to their
width ratio. If the option Fixed Width is set, then the column will not stretch.
For pinned columns, the value defines a fixed width. Pinned columns will not grow if more space is
available. For more details, consult the Table widget documentation of the Widgets team.
Furthermore, the Filterable and Sortable options can be configured within this field setting:
Filterable
Filterable is available for all data types except attachments. If there is at least one filterable
column in a repeat, a button is going to be available to open/close the filter section. A color
change of the button indicates an active filter.
107

-- 107 of 147 --

Figure 93. Filtering in Preview
The type of input displayed in the filter section depends on the data type of the column.
Filter Exposition
If filterable has been activated for an Enumeration or an External Enumeration, the exposition
used to display the filter input can be adjusted via the Filter Exposition property. The default
exposition for an Enumeration is FULL, meaning that all available options are going to be
displayed as a checkbox group. For an External Enumeration the default exposition is STRING,
meaning that a text input is going to be displayed where you can enter a filter value.
Sortable
Sortable is available for all data types except attachments and multi-selects. It supports
ascending and descending, and is triggered by clicking on a sortable column header. The chosen
column header shows the sorting state with a solid black triangle icon.
Preferred sorting
Specifies the initial sorting direction after sorting is triggered for this column. Sorting the
column in the other direction can still be achieved by clicking the column header again.
Figure 94. Sorting in Preview
NOTE
The rows of a Repeat will be immediately filtered and sorted again when a value
in one of the rows is changed. In Inline/Embedded Repeats this might lead to
unexpected behavior if filtering or sorting is active while editing a row:
• Filtering: The row might be filtered out and will therefore disappear from the
Screen.
• Sorting: If pagination is used for the Inline/Embedded Repeat, the row might
be sorted to a different page and will therefore disappear from the Screen.
While adding a new row, this row is excluded from filtering/sorting and will
therefore not be affected by the described behavior.
108

-- 108 of 147 --

NOTE
There is some special behavior when filtering and sorting lists of dates that
contain partially known dates. In this case the partially known dates will be
converted to a full date where the missing parts of the date are taken from "1970-
01-01". This is only done for the UI - no data will be altered. Examples:
• "2021-08-00" becomes "2021-08-01"
• "2021-00-00" becomes "2021-01-01"
• "0000-00-00" becomes "1970-01-01"
NOTE
When filtering fields of type DateRange, for which the property
interpretationOfYear is defined, it is not possible to use open ranges. For such
fields, the start and end values are required, because the year of the start date
depends on the end date or vice versa.
NOTE
Leading zeros will be ignored when sorting or filtering numbers with them (for
example, 0042).
Example for sorting: Having three rows with the following number values:
• Row 1: 042
• Row 2: 12
• Row 3: 0099
Sorting them in ascending order will result in the following order:
• Row 2: 12
• Row 1: 042
• Row 3: 0099
Furthermore, when entering a number with leading zeros into a number filter
field the leading zeros will be stripped from the input.
Pin Direction
defines if a certain column will be pinned to the left or right side of the table. Scrolling will
happen between pinned columns.
The column order will be changed in the editor automatically. A Column that gets pinned to the left
will be moved to the top and right-pinned columns to the bottom of the list. Unpinning does not
change the order. Every pinned column gets marked with a black triangle specifying the pin
direction.
Alignment
Alignments can be set independently for header and content cells of a Repeat column.
109

-- 109 of 147 --

Figure 95. Alignment of Columns
For horizontal alignment the following options are available:
• Default
• Left
• Center
• Right
For vertical alignment the available options are
• Default
• Top
• Middle
• Bottom
When setting the alignment to Default, the default alignment will be used. The default alignment is
determined according to the data type of the corresponding Document Model element, the repeat
type, the cell type (Header/Content) and the presence of an Expression column in this Repeat:
Default Condition
Horizontal Right All columns with data type Number
Left In all other cases
110

-- 110 of 147 --

Default Condition
Vertical Top For content cells of Inline Repeats that contain no expression
column
Middle In all other cases
NOTE When a specific horizontal alignment is set only for the body, the head alignment
will receive it by default as well.
Additional Settings
Summary Row
For all columns of type number, a summary row can be modeled by clicking the checkbox "Show
summary". The summary row will be displayed at the bottom of the repeat table if there is at
least one valid input in the column. If a suffix is modeled, it will be shown after the sum.
NOTE
Because the sum is formatted via Kernel, it is limited to 15 (significant) digits. If
the sum exceeds this limit because of fractional digits, it will be rounded. If the
sum exceeds the upper/lower bound, a placeholder will be shown instead. Refer
to the Kernel documentation for more information about the number type.
Figure 96. Summary Row for Number Column
Expressions for Repeats
A short introduction on how to create an expression in a repeat. For detailed information, see
Expressions.
• right click on a Repeat
• select the menu item Expression from the context menu
The Expressions will be displayed at the end of the Repeat list/overview and has the suffix (Expr) to
its name.
Similar to field columns, an icon can be specified for each Expression Column. This icon will be
shown in the Repeat table head, together with the column label. Additionally, Hide Label can be
selected. In this case, only the icon - if specified - will be displayed in the repeat table head. Note
111

-- 111 of 147 --

that hiding the label and not specifying a label text are not the same. The hidden label will still be
part of the form to provide accessibility.
Button Labels
You can edit the multilingual labels and confirmation texts of all Repeat buttons according to the
locales used in the current Form Model. The title and message of the modal shown when removing
a Repeat row can be configured in the "Remove Confirmation" section from Edit Repeat > Button
Labels.
112

-- 112 of 147 --

Figure 97. View for Customizing Button Texts in Repeats
113

-- 113 of 147 --

Button
Label
Description Shown for Example
Add Used by: Detached,
Embedded, Inline
Defines the label the
user has to click in
order to be able to fill
out a new entry. The
default value for the
label is Add (De:
Hinzufügen).
Repeats that have the
Add-Repeat-Entry
enabled and are not in
read only mode.
Commi
tAdd
Used by: Detached
Defines the label the
user has to click in
order to create a new
entry with the entered
data. The default value
for the label is Commit
(De: Hinzufügen
bestätigen). This
triggers a full
validation of the dialog
and if no errors are
found, is going to close
it.
Only for Detached-
Repeats that have the
Add-Repeat-Entry
enabled and are not in
read only mode. Visible
on the screen once the
user has clicked on the
Add-button and the
input dialog of the
detached repeat has
opened.
Apply Used by: Detached
Defines the label the
user has to click in
order to apply any
changes, while editing
the contents of an
existing entry. The
default value for the
label is Apply (De:
Anwenden). This
triggers a validation of
the dialog and if no
errors are found, is
going to close it.
Only for Detached-
Repeats that have at
least one entry and are
not in read only mode.
Only visible for the edit
mode of the input
dialog of the detached
repeat.
114

-- 114 of 147 --

Button
Label
Description Shown for Example
Edit Used by: Detached,
Embedded, Inline
Defines the tooltip of
the row and edit icon
the user can click in
order to be able to edit
an entry. The default
value for the tooltip is
Edit (De: Bearbeiten).
Each row of a Repeat
that is not in read only
mode.
Remov
e
Used by: Detached,
Embedded, Inline
Defines the tooltip of
the delete icon the user
has to click in order to
be able to delete an
entry and also defines
the label of the remove
button in the
confirmation dialog.
The default value for
the tooltip is Remove
(De: Löschen).
Each row of a Repeat
that is not in read only
mode.
View Used by: Detached,
Embedded, Inline
Defines the tooltip of
the row itself and of
the open icon the user
can click in order to be
able to view an entry.
The default value for
the tooltip is Open (De:
Öffnen).
Each row of a Repeat
that is in read only
mode.
115

-- 115 of 147 --

Button
Label
Description Shown for Example
Cancel Used by: Detached
Defines the label of the
button the user has to
click in order to cancel
the creation, editing or
deletion of an entry.
The default value for
the label is Cancel (De:
Abbrechen).
Only for Detached-
Repeats not in read
only mode.
Confir
m
Used by: Detached,
Embedded, Inline
Defines the label of the
button the user has to
click in order to
confirm a custom row
action. The default
value for the label is
Confirm (De:
Bestätigen).
Repeats that have a
custom row action
defined. Whether or
not the button is shown
in read only mode etc.
depends on how the
individual custom row
action is modelled.
Return Used by: Detached
Defines the label of the
button the user has to
click in order to close
the view dialog of an
entry and return to the
form. The default value
for the label is Return
(De: Zurück).
Only for Detached-
Repeats in read only
mode.
116

-- 116 of 147 --

Button
Label
Description Shown for Example
Up Used by: Detached,
Embedded, Inline
Defines the tooltip of
the button the user has
to click in order to
move an entry
upwards in the repeat.
The default value for
the tooltip is Up (De:
Nach oben).
Only for Repeats not in
read only mode.
Down Used by: Detached,
Embedded, Inline
Defines the tooltip of
the button the user has
to click in order to
move an entry
downwards in the
repeat. The default
value for the tooltip is
Down (De: Nach unten).
Only for Repeats not in
read only mode.
Copy Used by: Detached,
Embedded, Inline
Defines the tooltip of
the button the user has
to click in order to
create a copy of an
entry. The default value
for the tooltip is Copy
(De: Kopieren).
Only for Repeats not in
read only mode and
that have the Copy-
Repeat-Entry set. Note:
In order for the Copy-
Row-Action to be
enabled, the Add-
Repeat-Entry needs to
be selected as well.
Close Used by: Embedded
Defines the label of the
button the user has to
click in order to close
the view of an entry.
The default value for
the label is Close (De:
Schliessen).
Only for Embedded-
Repeats in read only
mode with at least one
entry.
117

-- 117 of 147 --

Button
Label
Description Shown for Example
Downl
oad
Used by: Detached,
Embedded, Inline
Defines the tooltip for
the row action button
the user has to click in
order to download the
view of an entry. The
default value for the
label is Download (De:
Herunterladen). Note:
This does not change
the label of the
Download-Button
inside the More
Options Menu, when
editing an entry with
an uploaded
Attachment.
For any repeat that
contains a column of
type attachment and
the MultiFileUpload
setting as well as the
Show Download Button
enabled. Shown for
both read only and edit
mode.
Skip Used by: Embedded,
Inline
Defines the label of the
button the user has to
click in order to skip
the creation of a new
entry, when uploading
a duplicate attachment
for a MultiFileUpload-
Repeat. The default
value for the label is
Skip (De:
Überspringen).
Only for Inline- and
Embedded-Repeats
with MultiFileUpload
enabled and not in read
only mode.
118

-- 118 of 147 --

Button
Label
Description Shown for Example
Replac
e
Used by: Embedded,
Inline
Defines the label of the
button the user has to
click in order to
replace the existing
entry, when uploading
a duplicate attachment
for a MultiFileUpload-
Repeat. The default
value for the label is
Replace (De: Ersetzen).
Note: This does not
change the label of the
Replace-Button inside
More Options Menu,
when editing an entry
with an uploaded
Attachment.
Only for Inline- and
Embedded-Repeats
with MultiFileUpload
enabled and not in read
only mode.
Upload
as
Copy
Used by: Embedded,
Inline
Defines the label of the
button the user has to
click in order to create
a new entry, when
uploading a duplicate
attachment for a
MultiFileUpload-
Repeat. The default
value for the label is
Upload as Copy (De: Als
Kopie hochladen).
Only for Inline- and
Embedded-Repeats
with MultiFileUpload
enabled and not in read
only mode.
Remov
e
Confir
mation
Title
Used by: Detached,
Embedded, Inline
Defines the text of the
title for the deletion
dialog of an entry. The
default value for the
title is Delete Row (De:
Zeile löschen).
Repeats not in read
only mode.
119

-- 119 of 147 --

Button
Label
Description Shown for Example
Remov
e
Confir
mation
Messag
e
Used by: Detached,
Embedded, Inline
Defines the text of the
message for the
deletion dialog of an
entry. The default value
for the message is
Deleting this Row
cannot be reverted.
Are you sure you want
to delete it? (De: Das
Löschen der Zeile kann
nicht rückgängig
gemacht werden. Sind
Sie sicher, dass Sie die
Zeile löschen wollen?).
Repeats not in read
only mode.
Dependencies
The Form Model Editor supports different model elements for advanced specifications concerning
dependencies and configurational elements that can be applied to Controls, Fields, or Groups. This
chapter takes a closer look at each type of Dependency.
Dependency Editing
To create, edit, or delete a dependency, click the "Data Configuration" tab in the sidebar. There, the
tree of the Document Model elements is shown for which you can apply dependencies. Existing
dependencies will be visualized in the tree with icons behind the elements: A "D" symbolizes an
element that is dependent on another, while a "T" symbolizes that the element is used as the master
in a dependency.
Dependent Field
The Dependent Field feature allows setting a field’s property based on another field’s value. The
field that is being controlled is called the Dependent Field. The controlling field is called the Master
Field.
A Field Dependency can be defined for all fields except value fields of Multi-Select groups.
The dependent field configuration can be created/removed as follows:
1. Click on the field to be made dependent in the Data Configuration View.
2. In the newly opened form under the tab "Dependencies", click the button "Set Dependent Field"
to create a new dependency.
120

-- 120 of 147 --

3. A table displaying all available trigger values will be visible, and the master field can be
changed by using the drop-down menu above the table.
4. Removing an existing dependency can be done by clicking the "Remove Dependent Field"
button below the table.
Clicking the Apply button in the bottom right will save your changes in the Form Model and close
the editor form.
Figure 98. Dialog to Edit a Dependent Field
NOTE
The Dependent Field feature is always applied to the underlying data field. For
example, if you use the same field both on an edit and on a summary Screen, the
Dependency will be effective on both Screens.
Dialog to Edit a Dependent Field shows the dialog for editing field-wise dependencies. In that
example, whenever a selection has been made on the ContactType master field, the Dependent Field
121

-- 121 of 147 --

will be cleared by setting an empty value. For each selected trigger value, the following options are
available:
Display Column:
• Default: Does not change the Dependent Field.
• Not Relevant: Hides the Dependent Field from the form view and ignores it in form validation
and computation. The field value will not be saved.
• Read Only: Sets the Dependent Field to read-only. If this field is required, the asterisk will not be
shown.
Value Column:
• Unchanged: Does not change the Dependent Field.
• Value: Writes the given value into the Dependent Field. This value can only be set once the
checkbox in the trigger value column has been activated. For dependent fields of certain data
types, the value can be entered into a plain input. In this case, the same data formats as for
initial values apply. See Initial Value for more details. For computed dependent fields, a value
cannot be set since it would conflict with the computation.
• Field Value: Takes the value of the selected field and writes it in the Dependent Field. All
available fields are listed in a drop-down menu. This value can only be set once the checkbox in
the trigger value column has been activated.
WARNING
A field can only be selected for the field value if it has the same data type as the
Dependent Field. However, it is important to note that the fields are not
checked for compatibility based on Data Type Configuration or Validation
Rules within the Document Model.
WARNING
The value effects of a Dependency are only evaluated and applied when the
value of the trigger field changes as a result of a manual change of a form
input or of adding or removing a repeat row. See Dependency Evaluation for
more details.
For example, the Dependency in Example of a Dependent Field means that the
value of PrimaryContact is set to the value of the field TelephoneNumber if, in
the input of the trigger field ContactType, the entry "Telephone Number" is
chosen. The value of PrimaryContact will not be updated when the value of the
field TelephoneNumber changes, as long as the trigger value is not deselected
and selected again.
122

-- 122 of 147 --

Figure 99. Example of a Dependent Field
WARNING
If the Dependent Field is located inside a repeatable group, then the following
restrictions apply:
The master field must be located in the same repeatable group or in a group on
a higher level above it. This is due to the fact that one master field data
instance can control read-only and not relevant for many Dependent Field data
instances but one Dependent Field data instance must be uniquely controlled
by one master field data instance. Otherwise, configurations would be
ambiguous.
WARNING
Combining multiple dependencies (for example, Dependent Field and Dependent
Enumeration) can result in unexpected behavior. See Combining Multiple
Dependencies for details.
123

-- 123 of 147 --

Default Values for Dependent Fields
There is one feature worth mentioning here regarding the interplay of Dependent Fields with their
default values. Consider the situation shown in Dependent Field "contactMisc".
The field labeled Miscellaneous defaults to phone number known when Telephone Number is selected
as a contactType. If in the next step Twitter Account is selected as contactType, the field
Miscellaneous will be hidden due to being marked as Not Relevant. Finally, making no selection on
the contactType makes the field Miscellaneous visible again. It will still be filled with the text phone
number known from before.
Figure 100. Dependent Field "contactMisc"
So no resetting of the field takes place when it is set to Not Relevant. It is good practice to set up
default values for Dependent Fields in the way just described. This works more elaborately
compared to just defining an initial value for the corresponding field.
124

-- 124 of 147 --

Dependent Group
Dependent Groups are similar to Dependent Fields, but they work on a group level. They can be used
to hide (via not relevant) or show all fields placed in a group and the group itself based on the
value of a master field for example.
To create/remove a Dependent Group:
1. Click any group in the Data Configuration View.
2. You can set a new dependency by clicking "Set Dependent Group".
3. Select a Master Field. Only Enumeration, Boolean and Confirmation fields are allowed as master
fields.
4. Select the trigger values for which you want to define the dependent group’s state. In case of
Boolean master fields, there are three trigger values: -No Selection-, false and true. In case of
Enumerations, all predefined Enumeration values and -No Selection- are listed as possible trigger
values.
5. Define the desired state for each activated trigger value.
6. Removing an existing dependency can be done by clicking the "Remove Dependent Group"
button below the table.
125

-- 125 of 147 --

Figure 101. View to Edit a Dependent Group
The View to Edit a Dependent Group shows an example of the dialog for editing group-wise
dependencies. For each selected trigger value, the following options are available:
Display Column:
• Not Relevant: Hides the Dependent Group and all of its fields from the form view and ignores
them in form validation and computation. The Dependent Group will not be saved.
• R/O: Sets all fields of the Dependent Group to read-only. This condition is also set for all members
of the Detached Repeat Detail Screen. As a consequence, if your trigger field is part of the
Detached Repeat Detail Screen, setting it to read-only inside the Screen will disable the
possibility to change back. Likewise, if you enter the Screen with read-only enabled, you cannot
disable it from inside the Screen.
NOTE
The described behavior of the read-only status to affect all members of the Detached
Repeat Detail Screen does not happen for the not relevant status of the Dependent
126

-- 126 of 147 --

Group.
NOTE
The Dependent Group feature has been introduced mainly for comfort. Instead of
defining the same dependency for each field in a group, you can define a
dependency for all fields in a group directly on group-level.
NOTE The groups of attachment and multi-select elements can be Dependent Groups.
Dependent Enumeration
Dependent Enumerations allow you to filter Enumeration subsets depending on the value of another
Enumeration which is called the master enumeration.
NOTE
Note that the "Set Dependent Enumeration" button will only be visible if the clicked
field is enumeration-like (so either an Enumeration field or a field with a Type
Definition based on Enumeration). Furthermore, Dependent Enumerations are not
supported for the value field of Multi-Selects.
The following listing shows how to set up this element.
1. Click on the field in the Data Configuration View which is supposed to become a Dependent
Enumeration.
2. In the newly opened form under the tab "Dependencies", click on the button "Set Dependent
Enumeration" to create a new dependency.
3. A table displaying all available trigger values will be visible and the master field can be changed
by using the drop-down menu above the table
4. In the example shown, the enumeration MasterEnumeration is set as the trigger value. Note that
only Controls for fields of type enumeration are listed in the drop-down menu.
5. Removing an existing Dependent Enumeration can be done by clicking the "Remove Dependent
Enumeration" button below the table.
In the example shown in Editor to Define a Dependent Enumeration, the following behavior of the
field DependentField is defined:
• Trigger value (not its text) MASTER_1 selected: DEPENDENT_1, DEPENDENT_4, and
DEPENDENT_5 and the empty selection will be selectable in the dependent enumeration. The
dependent enumeration field is set to DEPENDENT_1 on the master field change.
• Trigger value MASTER_2 selected: DEPENDENT_2, DEPENDENT_4, and DEPENDENT_5 and the
empty selection will be selectable in the dependent enumeration. The dependent enumeration
field is cleared on the master field change.
• Trigger value MASTER_3 selected: DEPENDENT_3, DEPENDENT_4, and DEPENDENT_5 and the
empty selection will be selectable in the dependent enumeration. The dependent enumeration
field is set to DEPENDENT_5 on the master field change.
WARNING • If there is an empty selection done on the master enumeration, then all
127

-- 127 of 147 --

values of the Dependent Enumeration become selectable. Furthermore, the
empty selection is always possible in the Dependent Field.
WARNING
Combining multiple dependencies (for example, Dependent Field and Dependent
Enumeration) can result in unexpected behavior. See Combining Multiple
Dependencies for details.
Figure 102. Editor to Define a Dependent Enumeration
Dependent Controls
Dependent Controls only serve to hide or show entire blocks (Screen elements), that is, Control
Grids, Sections, Multi-Column Sections or Custom Screen Elements. Everything concerning fields
should be done via Dependent Field. In contrast to the other Dependencies, Dependent Controls are
added from the Screens View. Other than that, the user flow is the same.
• Add a Dependent Control:
When clicking a Form Model Control of type Boolean, Confirm or Enumeration, the opened editor
form will include a dependency tab, where a Dependent Control can be added, edited or
removed. To add dependent elements to a specific trigger value, click it and choose elements
128

-- 128 of 147 --

from the tree modal. Note that you can resize the modal by dragging its borders.
Figure 103. Dependent Control Settings
129

-- 129 of 147 --

Figure 104. Select a Control From the Dialog
After completion of the Dependent Control the elements in the Screens View gets marked.
130

-- 130 of 147 --

Figure 105. Marked Dependent Controls With Mouse Over Hint
The trigger control is marked with a colored T flag behind its name. Dependent elements are mark
with a colored D flag.
• To edit an existing Dependent Control configuration:
1. click on the Control in the Screens View
2. navigate to Dependencies > Dependent Control
3. click on the trigger value whose Controls shall be changed
• To remove an existing Dependent Control configuration:
1. follow the same steps 1 and 2 from above on how to edit an existing Dependent Control
2. click on Remove Dependent Controls button
NOTE
A dependent Screen element must be controlled by exactly one instance of the
trigger field. Therefore, it is not allowed to select Screen elements, that are placed in
a data context outside of the context of the trigger field. Consider the following
example:
1. Document Model with a non-repeatable field F0 and a repeatable group G1 with
a field F1.
2. Form Model with a Detached Repeat for group G1 and a Control for F1 on its
Detail Screen.
Then it would not be allowed to define a Control dependency on the Control for F1,
that references a Screen element outside of the Detached Repeat Detail Screen,
because it is not uniquely defined which instance of F1 should be used to evaluate
the dependency.
In contrast, a Control dependency from a Control for field F0 to a Screen element on
the Detached Repeat Detail Screen would be valid.
131

-- 131 of 147 --

WARNING
Control dependencies on indexed Controls cannot be defined across
repeatability levels. This means, that an indexed Control may only reference
Screen elements on the same (Detail) Screen, that are not further nested in an
Embedded Repeat or Detached Repeat.
Dependency Evaluation
Dependencies can have effects on the display state of form content on the one hand, hidden (via not
relevant) and readonly, and on the values of form fields on the other hand.
In the editors of the Dependent Field and Dependent Group dependencies, these two effect
categories are represented by the two columns in the dependency tables.
Dependent Enumeration dependencies only have a value effect or an effect on the available
enumeration options.
Dependent Control dependencies only have a display state effect.
These different kinds of effects are evaluated and applied to the form at different points in time.
Display state effects are evaluated and applied in the form during initialization, that is, right from
the start, and when a trigger value of a dependency is changed in any way.
Value effects are only evaluated after the form was initialized. This means they are not evaluated
during the initial computation and also not for initial values. Therefore, defining an initial value for
a trigger field control doesn’t lead to the dependent field having the derived value initially.
Their evaluation only happens when the trigger value changes as a result of a user interaction with
the form. This could simply be a direct change of the trigger input value. But the trigger could also
be a computed field or a dependent field itself or even part of a chain of dependencies and
computations. In this case, evaluation happens when an input of the computation / a transitive
dependency trigger is changed manually. This can also happen by adding or removing repeat rows.
This default behavior can also be changed via the options described in Preprocessing Settings.
Combining Multiple Dependencies
Combining multiple dependencies can result in unwanted behavior for multiple reasons.
1. Dependencies are evaluated after each other and can therefore override each other.
That is why a combination of Dependent Field and Dependent Enumeration on the same field
should not be used.
2. Dependencies, which change the value of one or more fields, might trigger other dependencies
and can therefore lead to cycles. Furthermore, such dependencies might trigger a re-calculation
of computations that have been defined in the Document Model (for further information, refer
to the Document Modeling documentation). Consequently, combining dependencies with
computations can also lead to cycles.
132

-- 132 of 147 --

If a cyclic reference between any number of fields is detected, a consistency problem will be
reported, which provides information about the affected fields, dependencies, and
computations.
NOTE
Even if your Form Model contains multiple cycles, the consistency problem will
only contain information about the first detected cycle. Once this cycle has been
resolved, the message will be updated accordingly and show information about
the next remaining cycle.
Generally, the following combinations can lead to cycles:
◦ Dependent Field for a field F1 and a Dependent Field/Enumeration, which uses F1 as trigger
element
◦ Dependent Field for a field F1 and a Computation, which uses F1 in its precondition or
calculation
◦ Dependent Enumeration for a field F1 and a Dependent Field/Enumeration, which uses F1 as
trigger element
◦ Dependent Enumeration for a field F1 and a Computation, which uses F1 in its precondition or
calculation
◦ Computation for a field F1 and a Dependent Field/Enumeration, which uses F1 as trigger
element
◦ Computation for a field F1 and a Computation, which uses F1 in its precondition or calculation
NOTE
The cycle detection will only consider if the value of a field depends on some other
field, but not which specific value is set by the dependency or computation. In some
cases this will detect cycles, which do not cause problems in the running
application. Example:
• Dependent Field for field F1: If field F3 is set to 1, set field F1 to 1.
• Dependent Field for field F2: If field F1 is set to 1, set field F2 to 1.
• Dependent Field for field F3: If field F2 is set to 1, set field F3 to 2.
There is a cyclic reference between all three fields. However, the dependency for F3
will not trigger the dependency for F1.
Conditionally Hidden Elements
In addition to the functionality provided by Dependencies, most Form Model elements can be
conditionally hidden based on the value of some field instance in the document.
Unlike Dependencies, that allow marking fields as "not relevant" and remove their values,
conditionally hidden elements preserve their data in the document. It is purely a visual hiding
mechanism that does not affect the underlying document data. Each form element evaluates its
hide condition independently, and the hiding takes effect immediately when the form is displayed.
The field from the Document Model used in the Hide Condition can be of type Boolean, Confirm or
133

-- 133 of 147 --

Enumeration. Since computed fields are supported as well, the condition indirectly supports
complex logic and the expressiveness of the Kernel Language.
One or more values of the Hide Condition Field, including the empty value, can be selected and
when either of these values is present in the document, the element will be hidden.
This feature is similar to Dependent Controls, but more elements are supported and it is not
necessary to add a Control for the Hide Condition Field to the Screen.
Modeling Conditionally Hidden Elements
To configure elements to be conditionally hidden, in the Form Model Editor:
1. Select the to be hidden Form Model element from the model tree and open its element editor.
2. In the element editor, locate and open the section "Hide Condition".
3. Choose a Hide Condition Field from the dropdown list. Only supported fields are available for
selection. See Supported Hide Condition Fields for details.
4. Select the values of the Hide Condition Field, for which the element should be hidden. The
empty value, "(no value)", is supported as well.
134

-- 134 of 147 --

Figure 106. Hide Condition Section in the Element Editor
To remove the Hide Condition from an element, simply clear the Hide Condition Field selection in
the element editor. This removes the values as well.
Limitations and Considerations
When using Hide Conditions on Repeat Columns, only the displayed data or the input are hidden,
135

-- 135 of 147 --

but not the entire column. This means that the column header remains visible and the column still
takes space.
The same is true for Controls within Control Grids. When the Control is hidden, the displayed data
or the input is hidden, including the label, but the hidden Control itself still takes space in the grid.
The following elements are currently excluded from having a Hide Condition:
• Screens
• Event and Navigation Buttons
• Repeat Row Actions and Buttons
• Embedded Repeat Control Grids - since these are an integral part of the Embedded Repeat
• Detached Repeat Detail Screens - since these are an integral part of the Detached Repeat
Note, that Event and Navigation Buttons as well as Repeat Row Actions can be hidden or disabled
dynamically via the Form Engine’s Enablement programming API. See the Form Engine
documentation for details.
WARNING
Conditionally hidden elements are still considered during the form validation
and for computations. Validation rules need to take the visibility into account,
so that the user is not blocked from saving the document due to invalid hidden
elements.
WARNING
The Form Model Editor cannot prevent a model structure, where the last
remaining Control for influencing the hidden state of a container element is
placed somewhere within that container itself. This could lead to situations,
where the user can hide but not un-hide the container element anymore.
Be cautious to avoid such structures in your Form Model.
Influence on Validation and Computations
Since hiding an element via a Hide Condition has no influence on the underlying document data,
this data is still considered in both computations and validation rules.
In contrast, when using DependentField and DependentGroup with "not relevant", the data is
filtered out before executing validations. However, computations always include all data,
regardless of whether elements are conditionally hidden or marked as "not relevant".
Whether a hidden element is considered in validation depends on the validation mode:
• Full or field validation: Hidden elements are validated, including formal validations. If a
hidden element contains invalid data, the user will be blocked from saving.
• Partial validation: Only visible elements are validated. Hidden elements are excluded from
these validation modes.
136

-- 136 of 147 --

Recommendations
To prevent saving issues caused by invalid hidden elements, you should design your validation
rules to take the visibility of elements into account. For example, you could use an inversion of the
Hide Condition in the validation rule’s condition, so that the rule only applies when the element is
visible.
If the hidden element’s data can be discarded on save, prefer using the "not relevant" feature of
Dependencies instead of Hide Conditions, since the data is filtered out before validation then.
Be extra careful with computation rules, since here data from elements hidden via any hiding
mechanism is still included. If you do not want to compute values of hidden elements, you need to
include the visibility condition in the computation rule’s condition.
For a more detailed description of the form validation behavior, refer to the Form Engine
documentation.
Supported Hide Condition Fields
A field from the Document Model can serve as a Hide Condition Field when it meets both of the
following criteria: the field type and its position in the Document Model structure.
Field Type
The Hide Condition Field must be one of the following types:
• Boolean
• Confirm
• Enumeration
• Type Definition based on Boolean, Confirm, or Enumeration
TIP Since computed fields are also supported, you can use Kernel Language expressions to
implement complex visibility logic.
Document Structure Position
The Hide Condition Field instance must be uniquely identifiable in the context of the element to be
hidden. The following rules determine which fields are available:
For elements outside any Repeat (at the Form Model root level):
• Fields at the root level of the Document Model are available, that is, from all non-repeatable
root groups and their non-repeatable descendant groups.
For elements inside a Repeat:
• Fields from the same repeatable Group that the Repeat references, and from deeper nested non-
repeatable Groups within that repeatable Group, are available
• Fields from any ancestor Group in the path from the Document Model root to the Repeat’s
137

-- 137 of 147 --

Group are available
• Fields at the root level of the Document Model are available
NOTE
The Form Model Editor’s dropdown only displays fields that are valid for the
selected element’s position. If an expected field does not appear in the dropdown,
verify that the field meets the type and position requirements described above.
Overview in the Form Model Editor
Conditionally hidden elements are marked with a green indicator containing an "H" in the Form
Model Editor’s model tree, so that they can be easily identified.
Figure 107. Hide Condition Indicator in the Form Model Editor element tree
When hovering over the indicator, a tooltip gives a summary of the Hide Condition.
138

-- 138 of 147 --

Figure 108. Hide Condition Tooltip
Copying the Hide Condition
It is possible to copy the Hide Condition configuration from one element to another using the
context menu actions "Copy Hide Condition" and "Paste Hide Condition" available on elements that
support conditional hiding.
When copying, both the selected "Hide Condition" field and the selected values are copied. When
pasting, an existing configuration of the target element is replaced only after confirmation. A
currently open element editor will first be closed with dirty handling before pasting the Hide
Condition.
A once copied Hide Condition can be pasted multiple times to different target elements.
It is not checked whether the Hide Condition Field is compatible for the target element. If it isn’t,
the target element will become invalid and the error needs to be resolved manually.
Following keyboard shortcuts are available for the copy and paste actions:
• Copy Hide Condition: Ctrl+H / Cmd+H
• Paste Hide Condition: Ctrl+B / Cmd+B
Refactoring Support
When the experimental Form Model Refactoring feature is enabled in the Simple Model Editor,
deleting the Hide Condition field from the Document Model will trigger a refactoring dialog that,
next to changing to a different field, offers the option to remove the Hide Condition from all
elements that reference the deleted field.
Refactoring for field type changes of the Hide Condition field is not supported. If the field type is
changed to an unsupported type, the elements referencing that field in their Hide Condition will
become invalid and must be corrected manually.
When the Hide Condition Field type is changed to a different supported type (for example, from
139

-- 139 of 147 --

Enumeration to Boolean), the Hide Condition configuration is preserved but may become invalid.
The previously selected values might no longer be valid for the new field type. In such cases, all
elements referencing this field must be manually reviewed and their Hide Condition values
updated accordingly. Invalid values will be marked as such and there is an option to clear all
invalid values at once.
Comparison of Dependency Mechanisms
This chapter provides a comparative overview of the four mechanisms available in the Form Model
for controlling visibility, editability, and values of form elements based on document field values.
Overview
The Form Model Editor supports four distinct mechanisms for reactive form behavior:
• Dependent Field - Controls a single field’s display state and value based on a master field.
• Dependent Group - Controls all fields within a group based on a master field.
• Dependent Control - Hides or shows entire screen-level blocks based on a master control.
• Conditionally Hidden - Visually hides most form elements based on a master field value, without
affecting data.
Comparison of Mechanisms
Aspect Dependent Field Dependent Group Dependent
Control
Conditionally
Hidden
Purpose Set a single field’s
display and data
relevancy state
and/or static value
or another field’s
value based on a
master field’s
value.
Set the display and
data relevancy
state of all fields in
a group and the
group itself based
on a master field’s
value.
Hide or show
entire screen-level
blocks (structural
elements) based
on a master
control’s value.
Visually hides
most form
elements based on
a master field
value, without
affecting data.
Target Elements Individual fields
(for example,
String, Boolean,
Enumeration).
A group and all
contained
elements
(including fields
and nested
groups).
Structural
elements: Control
Grids, Sections,
Multi-Column
Sections, Custom
Screen Elements
Most form
elements.
Excluded: Screens,
Event and
Navigation
Buttons, Repeat
Row Actions and
Buttons,
Embedded Repeat
Control Grids, and
Detached Repeat
Detail Screens.
140

-- 140 of 147 --

Aspect Dependent Field Dependent Group Dependent
Control
Conditionally
Hidden
Supported
Master Field
Types
Enumeration,
Boolean,
Confirmation,
Type definitions
based on these
types.
Same as
Dependent Field.
Same as
Dependent Field,
but a master
Control must be
placed on the
same screen as the
dependent
structural element
and it cannot be
contained in the
same structural
element.
Same as
Dependent Field.
Trigger Values For Enumeration:
all predefined
values + the empty
value ("-No
Selection-")
For Boolean: false,
true + the empty
value
For Confirmation:
true + the empty
value
Same as
Dependent Field.
Same as
Dependent Field.
Same as
Dependent Field.
Display Effects Default (no
change), Not
Relevant (hidden,
not validated, not
saved), Read Only
Not Relevant
(hidden, not
validated, not
saved), Read Only
hides element and
all its content
(validated and
saved as usual)
hides element and
all its content
(validated and
saved as usual)
Data Effects Unchanged, Value
(set a specific
value), Field Value
(copy from
another field).
no data effects no data effects no data effects
Data
Preservation
Not Relevant: field
value is not saved.
Read Only and
Default: data
preserved. Value
effects actively
modify data.
Not Relevant:
group data is not
saved. Read Only:
data preserved.
Data is always
preserved. Hidden
elements are
removed from the
DOM but their
underlying data
remains intact.
Data is always
preserved. Hidden
elements are
removed from the
DOM but their
underlying data
remains intact.
141

-- 141 of 147 --

Aspect Dependent Field Dependent Group Dependent
Control
Conditionally
Hidden
Configuration
Location
Data
Configuration
View ⇒ On the
dependent field ⇒
"Dependencies"
tab
Data
Configuration
View ⇒ On the
dependent group
Screens View ⇒
Control of type
Boolean, Confirm,
or Enumeration ⇒
"Dependencies"
tab
Screens View ⇒
Editor of the target
element ⇒ "Hide
Condition" section
Operates At Data level.
Effective across
the entire form
where the field is
referenced and
also for document
data not
referenced in the
form
Data level.
Effective across
the entire form
where the group
and the fields of
the group are
referenced. Also
effective for
document data not
referenced in the
form.
UI level. Effective
only for the
specific elements
controlled by the
master control on
the same screen.
UI level. Effective
only for the
specific elements
controlled by the
master field
instance.
Requires Control
on Screen
No. The master
field does not need
a Control on the
Screen.
No. The master
field does not need
a Control on the
Screen.
Yes. The master
field must have a
Control placed on
the Screen in the
Screens View.
No. The master
field does not need
a Control on the
Screen.
Validation of
Hidden Elements
Not Relevant
elements are
excluded from
validation, but not
from computation.
Not Relevant
elements are
excluded from
validation, but not
from computation.
Hidden elements
are excluded from
partial but not
from full and field
validation. They
are still included
in computations.
Hidden elements
are excluded from
partial but not
from full and field
validation. They
are still included
in computations.
Time of
Evaluation
Display effects:
evaluated during
form initialization
and on master
value change. Data
effects: on master
value change,
potentially also on
form initialization.
See Pre-processing
settings for details.
Display effects:
evaluated during
form initialization
and on master
value change.
Display effects:
evaluated during
form initialization
and on master
value change.
Display effects:
evaluated during
form initialization
and on master
value change.
142

-- 142 of 147 --

Aspect Dependent Field Dependent Group Dependent
Control
Conditionally
Hidden
Visual Indicator
in the Form
Model Editor
"D" icon on
dependent
element, "T" icon
on master field
(trigger) in the
Data
Configuration
View.
"D" icon on
dependent
element, "T" icon
on master field
(trigger) in the
Data
Configuration
View.
"D" icon on
dependent
element, "T" icon
on master Control
(trigger) in the
Screens View.
"H" icon on
conditionally
hidden element in
the Screens View.
Copy/Paste
Support
No dedicated
copy/paste for the
dependency
configuration.
No dedicated
copy/paste for the
dependency
configuration.
No dedicated
copy/paste for the
dependency
configuration.
Yes. Context menu
actions "Copy Hide
Condition" / "Paste
Hide Condition".
Keyboard
shortcuts: Ctrl+H /
Cmd+H (copy),
Ctrl+B / Cmd+B
(paste).
Cycle Risk Yes. Can cause
cycles when
combined with
other
dependencies or
computations on /
leading back to the
same field.
Yes. Inherits cycle
risk from the
fields within the
group if combined
with other
dependencies or
computations.
No direct cycle
risk (no value
effects). Can be
part of a chain if
the master is a
computed or
dependent field.
No direct cycle
risk (no value
effects). Can be
part of a chain if
the master is a
computed or
dependent field.
Common Aspects
All four mechanisms share the following characteristics:
• They use fields of type Boolean, Confirmation, or Enumeration as their master field.
• They support the empty value as a master condition.
• Display state effects are evaluated during form initialization and re-evaluated when the master
value changes.
• They are configured within the Form Model Editor, not the Document Model Editor.
• Existing configurations are visually indicated in the editor tree views with dedicated icons
Unique Aspects
Dependent Field
• The only mechanism that can actively set or change field values (via the Value column: Value or
Field Value).
143

-- 143 of 147 --

• Provides three display states: Default, Not Relevant, and Read Only.
• Value effects by default are not evaluated during initial form loading - only after user
interaction. But this can be changed via pre-processing settings.
• Applied at the data level, meaning the dependency is effective on all Screens where the
dependent field is used and also for fields not used in the form.
Dependent Group
• Operates on the group level, controlling the group and all child fields at once - introduced
mainly for convenience to avoid defining the same dependency on each individual field.
• The Read Only state propagates into Detached Repeat Detail Screens, which can lock users out of
editing if the trigger field is inside the same Screen.
• Can be applied to attachment and multi-select element groups.
Dependent Control
• The only Dependency mechanism that is configured from the Screens View rather than the Data
Configuration View or element editor.
• Requires the master field to have a Control placed on a Screen.
• Targets screen-level structural elements (Control Grids, Sections, Multi-Column Sections, Custom
Screen Elements) rather than data fields or groups.
• Purely a display state mechanism with no impact on data values.
Conditionally Hidden
• Supports the broadest range of target elements (most Form Model elements).
• Does not require the condition field to have a Control on the Screen.
• Purely visual - hidden elements are still validated during a full validation and still participate in
computations, values are not discarded on save, unlike "not relevant" dependencies.
• Provides copy/paste functionality with keyboard shortcuts (Ctrl+H / Cmd+H, Ctrl+B / Cmd+B).
• Uses a green "H" indicator in the editor tree, distinct from the "D"/"T" icons used by
dependencies.
Choosing the Right Mechanism
Use Case Recommended Mechanism
Hide a Control, because its value is not needed
anymore in the document, and you want to
exclude it from validation and saving
Dependent Field (with display effect Not
Relevant)
Make a single Control read-only based on a
master field value
Dependent Field (with display effect Read Only)
Set a field’s value to another field’s value when a
master field value is changed
Dependent Field (with value effect Field Value)
144

-- 144 of 147 --

Use Case Recommended Mechanism
Make all Controls referencing fields of a certain
group read-only at once
Dependent Group
Hide a Repeat including all its rows in the form,
as it is not relevant any longer and you want to
exclude all repeating group instances from
validation and saving in the document
Dependent Group (with display effect Not
Relevant)
Visually hide a structural screen element
(Section, Control Grid) without affecting data
Conditionally Hidden
Visually hide a Control while preserving its data
and keeping validation active
Conditionally Hidden
Visually hide a Section on a Screen based on a
Control value, while showing another Section
based on a different Control referencing the
same Field as the first Control
Dependent Control
Using Multiple Mechanisms Together
Modeling multiple mechanisms for the same Element does not lead to an error but modelers must
take care when setting the condition.
An Element will be hidden when either a Hide Condition matches or it is not relevant according to a
Field or Group Dependency or a Dependent Control condition does not match.
This means that if the conditions of multiple mechanisms are combined on the same element, they
are evaluated with an OR logic.
Refactoring
The Simple Model Editor supports automatic refactoring for Form Models. When you rename,
move, or delete elements that are referenced elsewhere, the tool identifies affected references and
presents options for updating them.
For detailed information about refactoring capabilities, workflow, and actions, refer to the
Refactoring Support section in the SME Tool Documentation.
Form Model Specific Refactoring
Form Model refactoring handles references within Form Models and references from Form Models
to Document Models. The following elements and reference types are supported:
Supported Operations
Form Model refactoring supports the following operations:
• Renaming or deleting screens
145

-- 145 of 147 --

• Renaming or deleting controls and button panels
• Modifying navigation button targets
• Deleting master fields in referenced Document Models
Reference Types
The following reference types are tracked and updated during Form Model refactoring:
Reference Type Description
Navigation Button
Targets
References from buttons to their target screens.
Dependent Controls References between controls that depend on each other.
Initially Focused
Element
References from screens to their initially focused control.
Initial Sorting Column References from repeats to their default sorting column.
Master Field
References
References from Form Model fields to Document Model fields.
Style References References from controls to style definitions.
Sortable Columns References from repeats to sortable column elements.
Custom Row Actions References from repeats to Document Model actions.
Cross-Model Refactoring
Form Models reference Document Models for their data structure. When you modify a Document
Model that a Form Model references, the refactoring system detects affected Form Model
references.
For example, deleting a field in a Document Model that serves as a master field for Form Model
controls triggers the refactoring dialog. You can then choose to update the reference, select a
different target, or delete the affected control.
Example: Deleting a Screen
Consider a Form Model with:
• A screen named detailsScreen
• A button with navigation target pointing to detailsScreen
When you delete detailsScreen:
1. Save the Form Model.
2. The refactoring dialog displays the affected button reference.
3. Choose one of the following actions:
146

-- 146 of 147 --

◦ Commit: Clear the navigation target (leaving an empty reference).
◦ Edit: Select a different screen as the navigation target.
◦ Delete: Remove the button from the Form Model.
4. The model saves with your selected changes applied.
For information about enabling or disabling Form Model refactoring, see Enabling Within-Model
Refactoring in the SME Tool Documentation.
147

-- 147 of 147 --

