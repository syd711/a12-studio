# sme sme rum ba docs

Relationship UI Model
This documentation is intended for a business analyst audience. Some prior
knowledge of the tools is assumed.
The Relationship UI Model is the new way to configure relationship UI components introduced in
the 2026.06 for the new Relationship Engine Architecture. It defines the look and feel of a
relationship component as a standalone, reusable model with its own editor. It is based on
individual Query Models, giving the modeler full control of the listed entities on either side.
NOTE
Bindings remain fully supported in the Form Model Editor und at runtime.
For new modeling work, the Relationship UI Model is the recommended option. The
workspace setting Relationship Engine Mode determines which element is created
and shown in the application; see Relationship Engine Mode.
For the legacy Binding configuration, see Form Modeling – Binding.
Choosing Between Bindings and the
Relationship UI Model
Bindings are the legacy way to configure relationship components directly in a Form Model. The
configuration is stored in a hidden Form Model header annotation, and the relationship UI is
backed by generated Document Models.
Table of Contents
Choosing Between Bindings and the Relationship UI Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 1
Relationship Engine Mode . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
The Relationship UI Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
Creating a Relationship UI Model. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
The Relationship UI Model Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Buttons and Row-Action Events . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
The Overview Model Wizard . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Using a Relationship UI Model in a Form Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Relationship UI Components . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
Dropdown Selection . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
Dual Pane Selection . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
Table List . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
Labels in Relationship Views. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
Additional Link Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
Migrating from Bindings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
1

-- 1 of 14 --

The Relationship UI Model is a separate model type that configures the relationship component
independently from the Form Model structure. Instead of the generated Document Models, it is
based on Query Models.
CAUTION Query Modeling must be enabled in the Tool Settings of the Simple Model Editor
before use; see Enabling Model Types.
This allows to
• use Document Models with multiple Root Groups as related entities or Link Fields
• define Filter Definitions (Base Queries) for the selected and available entities lists
• filter the available items based on values of the context document (Query Variables)
• specify reusable Relationship UI configuration
Use Bindings when existing Form Models rely on them, or when you explicitly want to keep the
legacy configuration style. Use the Relationship UI Model for new work and when you want benefit
of the new modeling possibilities. In the future, Bindings will be deprecated and migrated to
Relationship UI Models.
NOTE
The choice applies to both regular Form Models and forms based on a Composed
Document Model. In Legacy (Binding) mode, both use Bindings; in New
(Relationship UI) mode, both use Relationship UI elements. The mode is a single
workspace setting; see Relationship Engine Mode.
CAUTION
Although the Form Model Editor shows both elements in order to support a
transition, the application either show legacy Bindings or Relationship UI
Elements, depending on the setting.
Relationship Engine Mode
The choice between the legacy Binding mode and the new Relationship UI Model mode is a single
workspace-level setting.
Open the settings in the Workspace Explorer, go to the General tab, and set Relationship Engine
Mode. The two values are Legacy (Binding) and New (Relationship UI). The default is Legacy
(Binding).
This applies to both kinds of form:
• In a regular Form Model, dragging a Relationship Model into the Screen View creates a
Relationship UI element.
• In a form based on a Composed Document Model, dropping a relationship group from the
Document Model tree creates a Relationship UI element, as a Custom Screen Element for a non-
repeatable group or as a Detached Repeat for a repeatable group.
In Legacy (Binding) mode, the same actions create a Binding in both kinds of form. The Preview
2

-- 2 of 14 --

Application runs one mode at a time and picks the mode up from this setting.
The Form Model Editor always shows both element types, the Binding and the Relationship UI
element, together with their respective editors, regardless of the selected mode. The Form Modeling
Module always shows both element types, the Binding and the Relationship UI element, together
with their respective editors, regardless of the selected mode. This lets you open and inspect
existing Bindings even while you author new Relationship UI elements.
Figure 1. A Relationship UI Model Referenced from a Form Model
The Relationship UI Model
The Relationship UI Model is a standalone artifact and then referenced from a dedicated
Relationship UI element in the Form Model. The Relationship UI Model is modeled as a standalone
artifact and then referenced from a dedicated Relationship UI element in the Form Model. This
separates the reusable relationship component configuration from the concrete position of the
element in a form.
The model has its own file and its own icon in the Workspace Explorer.
Creating a Relationship UI Model
There are two ways to create a Relationship UI Model.
In the Workspace Explorer, right-click the target folder and select "Add Entry" and "Relationship UI
Model". In the Add dialog, select a Relationship Model. Its Roles and Locales are taken over into the
new model.
Alternatively, you can create a Relationship UI Model directly from the Form Model editor by
3

-- 3 of 14 --

clicking the "Add" (plus) button on a newly added Relationship UI element.
Figure 2. Create a new Relationship UI Reference
4

-- 4 of 14 --

Figure 3. Add a Relationship UI Model
5

-- 5 of 14 --

Figure 4. Create a Relationship UI Model
This opens the Relationship UI Model editor as an editor-in-editor. It is also possible to edit existing
models by using the "Edit" (pen) button.
Moreover, Relationship UI Elements can be created by dragging and dropping a Relationship from
the Data Models pane of the Form Model Editor. In this case, the relationship and target role are
prefilled. If the relationship is self-referencing, you are asked to choose the target role. If you
cancel, the temporary model is discarded and nothing is created.
The Relationship UI Model Editor
The editor defines the relationship UI component and with this the respective UI and data needs in
the Component Configuration.
6

-- 6 of 14 --

Figure 5. The Relationship UI Model Editor
Select the component type: Dropdown, Dual Pane, or Table List. Select the component type:
Dropdown Selection, Dual Pane Selection, or Table List. Select the Available Items overview, which
lists the candidates, and the Selected Items overview, which lists the existing links. For a Table List
with an edit modal, configure the edit-modal component and its dimensions.
7

-- 7 of 14 --

Figure 6. Editing a Relationship UI Model
NOTE
Sorting is supported for available items only. It is not supported for selected items.
• If no default sorting is specified, the candidate list is sorted by __meta/createdAt
in descending order.
• Case is ignored during sorting (ignoreCase is set to true).
The Available Items Page Size and the Selected Items Page Size are the page sizes of the
available items and the selected items respectively.
• The Dropdown Selection needs only the Available Items Page Size.
• The Table List needs only the Selected Items Page Size.
• The Dual Pane Selection needs both the Available Items Page Size and the Selected Items
Page Size.
If a page size is not set, the view displays 10 items per page for the respective list.
NOTE
• A page size must not exceed the query.pageRequest.pageSizeLimit from Data
Services' Query Configuration. Otherwise, Data Services returns an error. To
resolve this, reduce the configured page size or increase Data Services'
query.pageRequest.pageSizeLimit.
• The same problem occurs when the page number is larger than the
query.pageRequest.pageNumberLimit from Data Services' Query Configuration.
8

-- 8 of 14 --

Increase the Data Services configuration to resolve it.
Buttons and Row-Action Events
Buttons are configured with the standard button editor. Row actions are defined as a fixed list of
events rather than free-form custom actions. You cannot add custom actions; you can only edit the
events that are present. Clicking a row in the list opens the standard button editor for that event,
where the event itself is read-only and you set the icon, labels, and confirmation message.
The default row-action events are created automatically, both for new models and during
migration:
Pane Event Purpose
Available Items event_add_link Adds the selected candidate as a link
Selected Items event_delete_link Removes the link for the row
Selected Items event_restore_link Re-adds a locally removed link (hidden by default;
controlled at runtime)
Selected Items event_edit_link_do
cument
Opens the link document edit form (only when a link Form
Model is configured)
The Overview Model Wizard
A relationship UI component requires a special set of Overview Models. One Overview Model
supports the display of the available items, also called candidates. Another Overview Model
supports the display of the selected items, also called links. Both Overview Models reference the
Document Model of the target role of the Relationship. They differ in their backing query: the
available items list shows all linkable candidates, while the selected items list is bound to the
current document through a context-bound Query Model so that it shows only the existing links.
Fields from the link document are shown in the selected items list as link columns.
You can create those Overview Models like any other Overview Model in the Simple Model Editor
and then select them in the editor. The editor lets you select only an Overview Model that
references the correct Document Models.
Beside an Overview Model reference field in the editor, the "Add" (plus) button opens a wizard that
generates the necessary models in one step. The plus button is disabled until the Relationship and
the target role are set, because the wizard needs them to determine the target Document Model and
the Additional-Link-Fields Document Model.
The wizard always creates an Overview Model and a backing Query Model.
The Overview Model name is prefilled from the Relationship name, the role, and the _OM suffix. The
Query Model name is derived from the Overview Model name: if the name ends in OM it becomes
QeM, otherwise _QeM is appended. Both models are created without being opened. The default row-
action buttons and their default labels are created on the Overview Models that the wizard
produces.
9

-- 9 of 14 --

The Query Model field opens a nested Query Model editor-in-editor. The Document Model reference
is prefilled depending on the Overview Model you intend to create. Roles and locales are prefilled
according to the referenced Document Model.
NOTE You can add columns for the additional link fields to the Selected Items overview,
because the selected items query exposes the link document fields as link columns.
Using a Relationship UI Model in a Form Model
In the Form Model Editor, add a Relationship UI element through the context menu of a Screen or
Section, or by dragging a Relationship Model from the Data Models panel.
Figure 7. Adding a Relationship UI Element to a Form
Instead of the in-place Binding Editor, you select an existing Relationship UI Model from a
dropdown, or create one with the "Add" (plus) button. The dropdown lists the Relationship UI
Models whose target role is compatible with the Document Model referenced by the Form Model, or
one of its supertypes. The element shows the referenced Relationship UI Model name in the tree,
like a control. You can place more than one element that references the same model, for example to
apply different hide conditions.
Unlike a Binding, the configuration is not stored in a bindingConfiguration annotation on the Form
Model. Instead, the reference is stored on the placeholder element and resolved through the Form
Model header.
• A Custom Screen Element carries an optional reference property that holds the model id.
• A Detached Repeat carries the model id in a feature-specific built-in annotation.
The header modelReferences then contain only the Document Model and the referenced
10

-- 10 of 14 --

Relationship UI Model. The Overview Models and Query Models that the Relationship UI Model uses
are no longer referenced directly by the Form Model; they are loaded transitively through the
Relationship UI Model.
Relationship UI Components
Bindings and the Relationship UI Model support the same three built-in component patterns: the
Dropdown Selection, the Dual Pane Selection, and the Table List. The difference is where you
configure them. The runtime appearance and behavior of each component are identical for both
paths.
For the full visual walkthrough of each component, including the embedded examples from the e-
Commerce sample workspace, see Built-in Relationship UI Components in the Binding
documentation. This section describes only what you configure for each component in the
Relationship UI Model.
Dropdown Selection
Use a Dropdown Selection when the target role has a link constraint with an upper limit of 1.
The Dropdown Selection appears as a control with an autocomplete widget. The currently linked
document is shown in the control, and the available documents are shown in the drop-down.
Selecting a different document replaces the existing link, because only one document can be linked.
Additional link fields can be displayed through the "Additional Properties" icon next to the arrow in
the drop-down field.
Only the first column of the Available Items or Selected Items Overview Model is displayed. It can
be a reference field or an expression column. If no document is linked yet, all documents are listed
as candidates. When the user types into the control, a Simple Search filters the candidates.
When the Dropdown Selection should appear next to other form inputs, use a Multi-Column
Section.
Dual Pane Selection
Use a Dual Pane Selection when the target role has a link constraint with an upper limit greater
than 1 or unbounded. The left side lists the available documents of the target role, and the right side
lists the existing links of the currently open document. A link is created by clicking the "+" icon and
removed by clicking the "-" icon. Documents that are already linked are shown with a gray
background on the left side.
The columns and several Overview Model settings are taken from the referenced Overview Models,
including all column settings, the filter configuration, styles, and the placement of sub-header
elements.
There are a few settings for the Dual Pane Selection:
• Height: Sets the height of the Dual Pane Selection in the form, without its labels.
11

-- 11 of 14 --

◦ Optional. Any valid CSS length value, for example "200px", "20vh", "30%", "15rem", or "auto".
◦ If it is not set, the component sizes to its content.
• Available Items Page Size: Determines the number of rows per page of the available items.
• Selected Items Page Size: Determines the number of rows per page of the selected items.
When used as the main component, the Dual Pane Selection takes up all available width in a form.
To scale the width, use a Multi-Column Section around the element.
NOTE Filtering is not available for the selected items.
Table List
A Table List is a read-only way to display links that have already been created. The columns are
determined by the columns of the Selected Items Overview Model.
To get a Table List with an "Edit" button, enable Has Edit Modal. The Edit Modal Component
section then appears with a Dual Pane Selection as the component type and the related settings.
The Edit Modal dimensions are configured under Edit Modal Properties:
• Edit Modal Width: The width of the Dual Pane Selection in the Edit Modal.
• Edit Modal Max Width
• Edit Modal Max Height
These options are an optional way to limit the size of the edit modal on large screens, while still
allowing it to shrink on smaller screens with a CSS length value. If it is not set, the dialog sizes to its
content.
When Has Edit Modal is not selected, a read-only Table List without an Edit button is embedded in
the form.
Labels in Relationship Views
The Relationship UI Model does not define labels. Labels are split between the other involved model
layers:
• The target role label is maintained in the Relationship Model and is displayed above the
relationship view.
• The labels above the available items and selected items lists are taken from the referenced
Overview Models.
• Section labels can additionally be displayed by wrapping the Relationship UI element in a
Section and using the label of the wrapping Section.
Buttons carry their own labels, which are set in the standard button editor.
For label examples per component type, including the Section-label variants, see Labels in
12

-- 12 of 14 --

Relationship Views in the Binding documentation.
Additional Link Fields
If the Relationship contains additional link fields, those values are maintained through a link Form
Model.
The Relationship UI Model references this Form Model through its link reference. The Form Model
has to reference the additional-link-fields Document Model and must contain at least a button with
an event to save.
When adding a link, the modal to maintain the additional link fields appears automatically. To edit
additional link fields for existing links, use the "Additional Properties" icon in the dual pane or
drop-down. Additional link fields can also be displayed as a column in the Selected Items overview
of a dual pane or table view.
Figure 8. Relationship UI Model Additional Link Fields
Migrating from Bindings
A dedicated migration tool converts an existing workspace folder from Bindings to Relationship UI
Models. This workspace should be migrated with SME before the use of the provided CLI tool.
The tool creates the Relationship UI Models and the required Query and Overview Models, named
13

-- 13 of 14 --

as siblings of the Form Model. It avoids creating exact duplicates and references an existing model
where one already fits. It removes the obsolete Form Model header references and binding
annotations. Migration is automatic in all cases, although it may create more Relationship UI
Models than strictly necessary.
Find more information in our Relationship Engine Developer Documentation.
NOTE
Switch to the new Relationship Engine mode in your workspace settings. This is not
touched by the migration tool, but it is required to use the new Relationship UI
Models. The Preview Application needs a restart if the setting was changed while it
was running.
14

-- 14 of 14 --

