# sme sme binding ba docs

Form Modeling – Binding
This documentation is intended for a business analyst audience. Some prior
knowledge of the tools is assumed.
NOTE
This page describes the legacy Binding configuration.
Bindings remain fully supported in 2026.06. For new modeling work, the
Relationship UI Model is the recommended option.
Which relationship element is created is controlled by the workspace setting
Relationship Engine Mode (Workspace Settings → General). In Legacy (Binding)
mode, both regular Form Models and forms based on a Composed Document Model
use Bindings, with the symbols and editors described here. In New (Relationship
UI) mode, both kinds of form use Relationship UI elements instead. The default is
Legacy (Binding). The Form Modeling Module shows both element types and their
editors regardless of the selected mode.
Bindings and Form Models
The UI configuration for displaying Relationship UI components within a form requires adding
special elements called "Bindings" to an existing Form Model. Adding these elements is done within
Table of Contents
Bindings and Form Models. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 1
Adding Bindings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
Via Context Menu . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
Via Drag and Drop . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
Editing Binding Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
Overview Models and Link Form Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Saving the Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Relationship UI Components . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Dropdown Selection . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Dual Pane Selection . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
Table List . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
Labels in Relationship Views. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
Dropdown Selection . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
Dual Pane Selection . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
Table List . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
Section Labels for Relationship Views . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
Additional Link Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
1

-- 1 of 20 --

the Form Modeling Module (FMM) of the Simple Model Editor (SME).
The Bindings are shown in the Screen view of the FMM with their name and special icons that
already hint to the configured relationship UI component. They can also be copied, pasted, moved
to a different position, and deleted like other screen elements.
Adding Bindings
Bindings can be added to Screens of the Form Model at the same places where Sections can be
added.
There are two ways to add Bindings:
• via the context menu of existing elements in the Form Model Tree
• via drag and drop of a Relationship Model from the list in the Data Models panel on the right
side.
Via Context Menu
Figure 1. Adding a New Binding via the Context Menu
The Form Model to which you want to add a Binding needs to reference a Document Model that
takes up a role in the Relationship. If the Relationship uses a heterogeneous Supertype Document
Model, the Form Model can also reference one of its Subtypes instead.
It will then be possible to create a link to the other role, the Target Role, in the Binding.
2

-- 2 of 20 --

Figure 2. Select the Relationship and Target Role
Via Drag and Drop
When a Binding is added via drag and drop, the Relationship is prefilled based on the dragged
Relationship Model.
The target role is prefilled with the role of the side in the Relationship Model that is not using the
referenced Document Model of the open Form Model or one of its Supertypes. When both sides of
the Relationship use such a Document Model, the target role needs to be filled by the user.
3

-- 3 of 20 --

Figure 3. Adding a New Binding via Drag & Drop
NOTE
One current shortcoming is that when dragging an element from the Data Models
View to the Screen View, container elements are not automatically expanded when
you hover over them with the drag item.
This means the drag target currently needs to be already visible in the Screen View
before starting the drag, which might require expanding container elements
manually beforehand.
Editing Binding Configuration
There are 3 productive component types available. They are the Dropdown Selection, the Dual Pane
Selection, and the Table List.
Figure 4. Component Types
The fields that are displayed in a Relationship UI can be determined during modeling.
In a Person-Company relationship, you might have a Form Model for Person and one for
4

-- 4 of 20 --

Company. If you add a Binding to the Person form, you can maintain links to a Company there.
Hence, the role Company is the target role of the Binding in the Person form. The same also works
the other way around.
A relationship UI component requires a special set of Overview Models. One Overview Model
supports the display of the available items, also called candidates. Another Overview Model
supports the display of the selected items, also called links. The Overview Model for available items
needs to reference the Document Model of the target role of the Relationship. The Overview Model
for selected items or links needs to reference the generated Document Model for the target role of
the Relationship.
NOTE
Sorting is supported for available items only. It is not supported for selected items.
• If no default sorting is specified, the candidate list is sorted by __meta/createdAt
in descending order.
• Case is ignored during sorting (ignoreCase is set to true).
The Available Items Page Size and the Selected Items Page Size are the page sizes of the
available items and the selected items respectively.
• The Drop-Down needs only Available Items Page Size.
• The Table List needs only Selected Items Page Size.
• The Dual Pane needs both Available Items Page Size and Selected Items Page Size.
If a page size is not set, the Binding view displays 10 items per page for the respective list.
NOTE
• Page size must not exceed the query.pageRequest.pageSizeLimit from Data
Services' Query Configuration. Otherwise, Data Services returns an error. To
resolve this, reduce the configured page size or increase Data Services'
query.pageRequest.pageSizeLimit.
• The same problem occurs when the page number is larger than the
query.pageRequest.pageNumberLimit from Data Services' Query Configuration.
Increase the Data Services configuration to resolve it.
Overview Models and Link Form Models
You can create those Overview Models like any other Overview Model in the Simple Model Editor
and then select them in the Binding Editor. The Binding Editor lets you select only an Overview
Model that references the correct Document Models.
5

-- 5 of 20 --

Figure 5. Select/Edit Overview Models or Create New Ones
The Binding Editor can also support you in creating those specific Overview Models. If you select
"Add" (plus icon) for the available or selected items overview, a special Overview Model Editor
opens in a modal.
6

-- 6 of 20 --

Figure 6. New Overview Model as a Modal in the Binding Editor
The Document Model reference is prefilled depending on the Overview Model you intend to create.
A model name is suggested that follows the optional naming convention
RelationshipName_TargetRoleName_AvailableItemsOverview or
RelationshipName_TargetRoleName_SelectedItemsOverview, respectively. Roles and locales are
prefilled according to the referenced Document Model.
The settings you can make in this special Overview Model Editor are limited to the settings that are
used for the display of the Binding and are therefore available in the Relationship UI.
NOTE Not all settings that can be made are used in each Binding view type.
You need to add at least one column to those Overview Models to save them. The target folder for
saving is the folder in which the Form Model is located. The Overview Model has an identifier in
the Workspace Explorer, and the limited Overview Model Editor also opens if you open it from
there. Information is shown about which forms and Bindings reference the respective Overview
Model.
7

-- 7 of 20 --

Figure 7. Binding Overview Model information toast
After the creation of a new Binding Overview Model in the Binding Editor for either the selected or
available items overview, it is automatically selected as a reference.
If you selected an additional fields Document Model for your Relationship, you need a Form Model
in which those additional link fields can be maintained. This Form Model has to reference the
additional fields Document Model and must contain at least a button with some event to save.
Again, the Binding Editor lets you select only Form Models that reference the correct Document
Model.
NOTE You can add columns of the additional link fields for the Selected Items Overview
since the __generated Document Model includes them.
To edit the referenced Overview Model for available or selected items, click the "Edit" button
(pencil icon).
8

-- 8 of 20 --

Figure 8. Edit Selected Overview Model for Binding
NOTE The Edit button is disabled when the Overview Model is empty or invalid.
Saving the Configuration
If you finished the settings for your desired Relationship UI, you can save the Binding Detail Editor
and then save the Form Model that contains your Bindings.
NOTE
In the exported Form Model, the configuration details of all Bindings are stored
together in a hidden model header Annotation called bindingConfiguration.
In the Form Model Screens Custom Screen Elements are inserted at the positions of
the Binding elements. In the Form Model Editor Preview, these elements are
displayed as placeholder boxes.
For Bindings, these placeholders don’t reflect the height configured for the
Binding’s main component. Use the Preview Application to check the heights with
the actual Relationship UI components.
The Form Model Editor does not support creating Bindings in a Control Grid.
9

-- 9 of 20 --

Relationship UI Components
Bindings and the Relationship UI Model support the same built-in component patterns. The
difference is where you configure them. Bindings are configured in the Binding Detail Editor.
Dropdown Selection
If the target role has a link constraint with an upper limit of 1, a drop-down can be used to maintain
and display a link or selected item.
10

-- 10 of 20 --

Figure 9. Binding Detail Editor for DropDown Selection
The Dropdown Selection appears as a Control in the form with an autocomplete widget. In the
Control, the currently linked document is displayed. In the drop-down, all available documents that
can be linked are displayed. Selecting a different document from the drop-down removes a prior
existing link because only one document can be linked in this case. Additional link fields can be
displayed when clicking the "Additional Properties" icon next to the arrow in the drop-down field.
11

-- 11 of 20 --

Figure 10. Dropdown Selection
Only the first column of the Available Items Overview or Selected Items Overview Models is
displayed. It can be either a reference field or an expression column. The Overview Models for
available and selected items do not need to match. Be aware that this leads to different information
being displayed in the selection list and in the field after a selection is made.
If no document is linked yet, all documents are listed as candidates in the drop-down list. If the user
starts typing into the Control, a Simple Search is performed to filter the number of displayed
documents. This has benefits because it can match not just by the displayed text but by all available
fields of the document. It also has drawbacks because currently not all field types are searchable.
In the example pictured below, which is taken from the e-Commerce sample Workspace for the
Preview Application, a drop-down is available to assign a Brand to the Product that is displayed in
the form.
12

-- 12 of 20 --

Figure 11. A Drop-Down Embedded in a Form
When the Drop-Down Selection should be displayed next to other form inputs, a Multi-Column
Section can be used.
Dual Pane Selection
If the target role has a link constraint with an upper limit greater than 1 or is unbounded, a dual
pane can be used to maintain and display links. On the left side, all available documents of the
target role are displayed. A link can be created by clicking the "+" icon in the row of the respective
document. Documents that have already been linked to the document that is currently open are
displayed with a gray background.
13

-- 13 of 20 --

Figure 12. Dual Pane Selection
On the right side of the dual pane, all existing links of the currently open document are displayed.
The additional linked fields, if present, can be changed via the "Additional Properties" icon in the
row of the respective document. Clicking the "-" icon removes the link to this document.
Figure 13. A Dual Pane Selection Embedded in a Form
There are a few settings for the Dual Pane Selection:
• Height: Determines the height of the Dual Pane Selection in the form (without its labels).
◦ A string that can be a number interpreted as pixels, some other CSS height definition incl.
relative units, e.g. "20vh", "30%", "15rem", etc. or "auto" to automatically adjust the height to
the content.
14

-- 14 of 20 --

◦ default: auto
• Available Items Page Size: Determines the amount of rows per page of the available items.
• Selected Items Page Size: Determines the amount of rows per page of the selected items.
• Labels: Multilingual labels that are displayed above the available items or selected items table
◦ The available locales are the ones from the Form Model settings.
◦ If not modeled, the values for the labels are taken from the localizer resource bundles.
Figure 14. Binding Editor for Dual Pane Selection
When used as the main component, the width of the Dual Pane Selection dynamically takes up all
available space in a form. To scale the width of the Dual Pane Selection, you can use a Multi-Column
Section in the FMM around the dual pane section.
The columns which are displayed in a Dual Pane Selection are determined by the columns of the
Overview Models for the Available Items Overview or Selected Items Overview, respectively.
Besides the columns, the following settings of the Overview Model will be used in a Dual Pane
Selection:
• all column settings
• show or hide filter and filter configuration
• styles
• placement of sub-header elements
15

-- 15 of 20 --

NOTE Filtering is not available for the selected items or links, which is why the respective
setting is not supported in the Binding Overview Model Editor.
Table List
A Table List is a read-only way to display links. It displays Relationship links that have already been
created.
Figure 15. Table List - Left: With Edit Button - Right: Without Edit Button
To have a Table List with an "Edit" button, select Has Edit Modal. Then the Edit Modal
Component section appears with a Dual Pane Selection as component type and the related settings.
16

-- 16 of 20 --

Figure 16. Binding Editor for Table List With Edit Button
To customize this modal, there are a few settings for the TableList view under the section Edit
Modal Properties:
• Height: Determines the height of the Dual Pane Selection when used in the Edit Modal.
◦ A string that can be a number interpreted as pixels, some other CSS height definition incl.
relative units, e.g. "20vh", "30%", "15rem", etc. or "auto" to automatically adjust the height to
the content.
◦ default: auto
• Edit Modal Width: Determines the width of the Dual Pane Selection, when shown in the Edit
Modal.
◦ same definition as in Height applies.
17

-- 17 of 20 --

◦ default: auto
• Edit Modal Max Width: Used to define a maximum width of the Dual Pane Selection, when
shown in the Edit Modal.
◦ same definition as in Height applies.
◦ default: auto
• Edit Modal Max Height: Used to define a maximum height of the Dual Pane Selection, when
shown in the Edit Modal.
◦ same definition as in Height applies.
◦ default: auto
• Labels: Multilingual labels for the title and the cancel and close buttons of the Edit Dialog
◦ The available locales are the ones from the Form Model settings.
◦ If not modeled, default values are used for the labels, which are taken from the localizer
resource bundles.
When Has Edit Modal is not selected, a Table List without an Edit button is embedded in a form.
Figure 17. A Table List Embedded in a Form
The columns that are displayed in the Table List are determined by the columns of the Overview
Model for the Selected Items Overview.
18

-- 18 of 20 --

Labels in Relationship Views
In the Relationship Model as well as the Binding, different labels can be maintained that are
displayed above or in Relationship views. All labels are optional.
Dropdown Selection
The label of the target role, which is maintained in the Relationship Model, will be displayed above
the drop-down:
Dual Pane Selection
The label of the target role, which is maintained in the Relationship Model, will be displayed above
the dual pane. The labels maintained in the Binding will be displayed above the respective item list.
Table List
The label of the target role, which is maintained in the Relationship Model, will be displayed above
the Table List.
Section Labels for Relationship Views
Instead of or in addition to the labels that are displayed above Relationship views, it is also possible
to display Section labels. To achieve that, the Binding needs to be wrapped in another Section. The
label of the wrapping Section can then be used to display another type of label above the
Relationship view.
Examples for the Section label on top of the target role label are displayed below. If the target role
label (maintained in the Relationship Model) is empty, only the Section label is displayed.
19

-- 19 of 20 --

Figure 18. Dropdown Selection with target role label and Section label
Figure 19. Dual Pane Selection with target role label and Section label
Figure 20. Table List with target role label and Section label
Additional Link Fields
To add or edit additional link fields, a modal is displayed. The content of the modal is determined
by the Form Model that refers to the additional link fields' Document Model. If additional link fields
are part of the Relationship, the respective Form Model has to be referenced in the Binding.
When adding a link, the modal to maintain the additional link fields automatically appears. To edit
additional link fields for existing links, you can use the "Additional Properties" icon in the dual pane
or drop-down. Additional link fields can be displayed in a selected items overview column in a dual
pane or table view.
20

-- 20 of 20 --

