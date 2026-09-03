# sme sme data ba docs

Workspace Data
Workspace Data is initial data for an A12 application that can be used for
demonstration or testing purposes during the application development process.
It is designed for small-scale, non-critical data scenarios and is represented by
Documents, Links, Attachments, as well as user and roles files in the Workspace.
This feature ensures consistency within the Modeling Environment after deployment. It eliminates
old models, data, roles, and users, allowing the modeler to preview the current state of the
Workspace.
Even if the Document Model changes and data is present, the modeler can continue incremental
modeling.
Workspace Data is contained in a Workspace that is opened in the SME. It can be generated and
maintained in an A12 application during runtime, for example with the Preview App. The
generated data can be downloaded as Workspace Data and integrated into the SME Workspace.
Finally, the entire Workspace, containing all its Models and the Workspace Data, can be exchanged
and uploaded to the server of a connected A12 application.
The Workspace Data comprises the following elements, which are displayed in the Workspace
Explorer:
• Documents are the data entities of the Workspace Data. Each Document must correspond to a
Document Model, Combined Document Model or Transformed Document Model that is also
contained in the Workspace.
• Links describe a relationship between two Documents contained in the Workspace Data and
correspond to a Relationship Model that must be contained in the Workspace. If the
Relationship Model defines a Link Document Model Reference, the corresponding Links must
indicate a Link Document Reference that points to a Document in the Workspace.
Table of Contents
Generating and Maintaining Workspace Data . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
Editing Documents . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
Document Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
Document Data . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Validation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Document Import. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
Editing Links . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
Link Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
Link Data . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Uploading a Workspace . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Downloading and Updating Data . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
1

-- 1 of 12 --

• Attachments are files that are currently being used or planned to be used as Attachments
referenced from Documents. Any file can be used as an Attachment in the Workspace;
therefore, it can only be determined to be an Attachment by being placed in the
data/attachments folder. This must be done in the file system.
• The Users file contains all users that should be available in the A12 Application once the
workspace is deployed to a connected server. User files are currently not editable via the SME.
Instead, they can be edited via the User Management service, which can be started in the Expert
Mode of the Preview App Control.
• The Roles file contains all roles and the corresponding access rights assignments of the
Workspace. It can also be edited with the User Management service. In the SME, the roles file
can be opened but not edited.
A workspacedata_items file is maintained by the Simple Model Editor in the file system. It
stores the Data Services document and link identifiers. It is not shown in the Workspace
Explorer but updated if files are added or removed.
Generating and Maintaining Workspace
Data
Workspace Data can be generated and maintained in the application during runtime. The Preview
App, for example, enables creating, editing, and deleting Documents and Links, as well as
uploading and deleting Attachments. This data can be downloaded as Workspace Data and
integrated into the SME Workspace. The benefit here is that even if the Document Model changes
and data is present, the modeler can continue incremental modeling since any inconsistencies are
visible and can be fixed within the SME. The Documents are also available for testing in the Editor
Previews, such as the Form Model Preview.
A new deployment of a Workspace ensures that the application contains only the current state of
the Workspace. Old models, data, roles, and users are removed.
Editing Documents
Each Document contained in a Workspace is stored as an individual file. If existing Documents are
to be edited or viewed, they must be in the opened SME Workspace. After opening a Workspace, all
relevant entries contained in it are displayed in the Workspace Explorer of the SME. Documents
will be recognized as such by the SME, and the respective icon—a database icon with a "D"—will be
displayed next to them.
To open the Document Editor for an existing Document, click on the entry name in the Workspace
Explorer.
Data added in a running A12 application can be downloaded as Workspace Data. Additionally, new
Documents can be created and added to the Workspace either by using the Add button in the header
of the Workspace Explorer or via the context menu on the data/documents folder.
2

-- 2 of 12 --

Figure 1. Add a New Document
If a new Document is to be added via the Workspace Explorer, a modal will be displayed to define
the most important Document settings: folder, Document name, and Document Model.
The folder selection contains a dropdown with all folders that are allowed to contain a Document.
The SME prescribes that Documents may only be contained in the data/documents folder or any
subfolders of this folder.
The Document name must be unique within the Workspace.
The Document Model must point to the Model that the Document should conform to. This
determines the data structure that is considered valid for the data contained in the Document. The
Document Model can be selected via a dropdown containing the names of all Document Models in
the Workspace.
After confirming the dialog, the Document Editor with the new, temporary Document entry opens.
The Document Editor has two tabs for editing the Document: Settings and Document Data.
Document Settings
3

-- 3 of 12 --

Figure 2. Document Settings
In the Settings tab, the Document can be renamed, and the Document Model can be changed. For
the Document, a Locale can be selected that is used as the language for the validation of the
Document. The Locale dropdown lists all locales of the selected Document Model. Changed
Document settings must be confirmed by pressing "Apply" before they become active. Closing the
editor or changing to another editor tab is not possible while unsaved changes are present.
If a Transformed Document Model or a Combined Document Model based on one is selected, then
two additional buttons are shown in the footer of the editor tab:
Export data with Transformer
A XML file is created based on the specifications of the Transformer Model. This could include
the renaming of Document Model elements to XML elements. The export destination can be
selected in the file dialog that is shown.
Import data with Transformer
A file dialog is shown to select one XML file that contains the data that is to be imported. The
XML file should be valid against the respective XSD file, otherwise the import will fail with an
error. In this case, it is recommended to use a specialized external tool to get modeling support
to resolve the issue.
The imported data of the XML file will overwrite existing data in the document.
4

-- 4 of 12 --

Document Data
Figure 3. Document Data
The Document Data tab contains the actual data of the Document alongside the data structure. For
Document Models that include other Document Models, the Document Model is expanded to enable
editing of the included parts of the corresponding Document.
The main component of the Document Data tab is the Field Value Table, which is a tree
representation of the Group and Field instances of the Document. Each row in the table represents
a Group or Field instance. The Field Value Table has three columns. First, the Name column is the
hierarchical column of the tree. In this column, Groups are represented by a folder icon and their
name. For repeatable groups, the name is suffixed by two numbers: The first number is the actual
repetition of the group instance in the current row, and the second number is the maximum
number of repetitions.
Figure 4. Document Data: Repeatable Groups
5

-- 5 of 12 --

Next to the repetition information are buttons for repetition actions. First, the button with the plus
icon can be used to create a new and empty repetition in a new row directly below the row of the
current repetition. Similarly, the second button enables adding a new repetition as a copy of the
Field values of the current repetition. The third button enables deleting the current Group
repetition. The action buttons are only enabled if the current actual and maximum repetitions
allow them to be executed. Therefore, a new repetition can only be added to a repeatable Group if
the maximum number of repetitions has not yet been reached.
In Documents, Attachments and Multiselects are Groups of values that are displayed in a similar
way, but have their respective icons known from the Document Model Editor.
Fields and computed Fields are represented by the same icons as in the Document Model Editor and
their Field name. If the Field is marked as a required Field, an asterisk icon is added as a suffix to
the name.
The Field value column contains inputs to edit the values of the Field. For Groups, the Field value
cells are empty. For computed Fields, the computed value is displayed as readonly. For other Fields,
the appearance of the input depends on the Field type. String and number Fields have a text-line
input field. If the Type Configuration of a String Field allows line breaks, the input is a text area.
Boolean Fields and Enumeration Fields have a dropdown input that enables selecting only valid
values, and Confirm Fields are represented by check boxes. Date-related Field values can be edited
with suitable Date and Time pickers.
If the current value of a Field cannot be displayed in a specific input representation, the SME uses a
normal text-line input to edit/correct it.
The Document Data Editor enables to associate the Document with an Attachment file contained in
the Workspace. To do so, the original_filename Field of the Attachment contains a dropdown with
all Attachment files currently available in the Workspace. If an Attachment file has recently been
added to the file system, it is necessary to reload the Workspace in order for the Attachment to be
displayed in the Workspace Explorer. Updating the value of the original_filename automatically
updates the other Fields of the Attachment: The attachment_id is updated to the id of the newly
selected Attachment file and the values of other Fields are removed, because their values are not
valid anymore and new values for these Fields are only calculated by Data Services once the
Attachment is uploaded.
Validation
If the currently displayed Document is invalid with respect to the Document Model, the validation
column of the Field Value Table displays all validation findings that relate to a concrete Group/Field
instance in the cell of the corresponding row.
The result of the Document validation is also displayed in a validation bar above the Field value
table. The validation bar has an expanded and a compact representation that can be switched in a
button on the right side of the validation bar. In the expanded representation, the validation bar
contains an overview of all validation findings in the Document. Clicking an entry in this overview
focuses the affected row of the Field Value Table. In the compact representation, only one finding is
displayed at a time. The findings can be navigated individually by using the next/previous finding
6

-- 6 of 12 --

buttons.
The validation is triggered after opening and before saving a Document, upon editing a Field value
and after changing the Document Model of the current Document.
For example, the Document seen in Figure 3 had its respective Document Model edited and saved,
resulting in a field path being changed. When the Document is opened again, the following will be
reported in the data view:
Figure 5. Document Data Validation Error
The old value and field path are kept, and the user can decide how to resolve the error.
Document Import
The folder data/documents as well as each Document Model offer a context menu action to import
Documents from JSON files. The action opens a file selection dialog that enables selecting one or
more JSON files from the file system. The selected files are imported as new Documents conforming
to the selected Document Model in the data/documents folder. The SME creates subfolders for each
Document Model referenced by the imported Documents if they do not yet exist.
Editing Links
Each Link contained in a Workspace is stored as an individual file. After opening a Workspace, all
relevant entries that are contained in it are displayed in the Workspace Explorer of the SME. Links
will be recognized as such by the SME, the respective icon, a database icon with an "L" will be
displayed next to them.
To open the Link Editor for an existing Document, click on the entry name in the Workspace
Explorer.
Like for Documents, Links created in a running A12 application can be downloaded as Workspace
Data. Additionally new Links can be created and added to the Workspace either by using the Add
button in the header of the Workspace Explorer or via the context menu on the data/links folder.
7

-- 7 of 12 --

Figure 6. Add a new Link
If a new Link is to be added via the Workspace Explorer, a modal will be displayed to define the
most important settings: Folder, Link name and Relationship Model.
The folder selection contains a dropdown with all folders that are allowed to contain a Link. The
SME prescribes that Links may only be contained in the data/links folder or any subfolders of this
folder.
The Link name must be unique in the Workspace.
The Relationship Model must point to the Model that the Link should conform to. This determines
the entities and the Model for the Link Document of the Link. The Relationship Model can be
selected via a Dropdown containing the names of all Relationship Models in the Workspace.
Link Settings
The Link Settings tab enables renaming the Link and changing the Relationship Model. The latter
also affects the entity data displayed in the Link Data tab and may invalidate the Link. Changed
Link Settings must be confirmed by pressing "Apply" before they become active. The SME prevents
closing the editor or changing to another editor tab if the settings tab has unsaved changes.
8

-- 8 of 12 --

Figure 7. Link Settings
Link Data
The Link Data tab enables editing the two Link entities and, if the Relationship Model defines a Link
Document Model Reference also the Link Document, in individual sections.
The two Link Entity sections have the name of the respective entity as defined in the Relationship
Model in their section titles. On the left side, the sections contain a control for the Document
Reference and on the right side, for information purposes, a readonly input field with the
Document Model of the entity. The Document Reference can be edited with a dropdown that
contains options with the names of all Documents in the Workspace Explorer that conform to the
Document Model of the entity. Internally, however, the stored Link file contains the Document
Reference of the selected Document instead of its name.
The Link Document section is only displayed if required based on the Relationship Model of the
Link. Similar to the entity sections, this section contains an input for editing the Link Document and
a readonly control displaying the Link Document Model.
9

-- 9 of 12 --

Figure 8. Link Data
Uploading a Workspace
The SME enables uploading an entire Workspace containing all its Models, the Documents, Links,
and Attachment files constituting the Workspace Data, as well as a Users file and a Roles file, and a
metadata file to the server of a connected A12 application that has the Workspace Data endpoints
enabled.
As a prerequisite, the SME must be connected to the server of an A12 application. This can be done
in the "Configure Server Connection" button in the top right corner of the SME. In the modal that
opens, the URL of an A12 application server can be set. The "Test URL" button next to the URL
enables testing whether the URL is valid. The preview app URL is pre-filled. For uploading an entire
Workspace, it is not required to be logged in to the A12 application that should be connected. For
deploying individual models, however, it is required to log in by entering a valid combination of
username and password. The login can be attempted by pressing the "Connect" button in the
bottom of the dialog. Alternatively, the dialog can be confirmed by pressing "Save URL" in case no
credentials are needed.
The Upload Workspace functionality is triggered by pressing the "Deploy" button in the header of
the Workspace Explorer. The button is enabled once a URL has been saved.
On success, the Upload Workspace functionality reports with a success notification that indicates
the number of files that have been uploaded. Otherwise, an error is reported as a notification.
Internally, the functionality performs a sequence of individual actions:
10

-- 10 of 12 --

1. As a first step, all Workspace files that are uploaded are transformed in a representation that
Data Services can handle and are bundled as a tar file.
2. Afterwards, the SME removes all existing data from the connected A12 Server.
3. Finally, the SME uploads the bundled Workspace to the Server and Data Services updates
models, data, users, and roles in the application accordingly.
Downloading and Updating Data
If a Server Connection from the SME to an A12 application, such as the Preview App, is established,
the current data of this application can be downloaded and integrated into the SME. To this effect,
the SME contains an option in the Detail menu of the Deploy button in the header of the Workspace
Explorer. The menu can be opened by clicking the button with the arrow icon next to the deploy
button.
Figure 9. Deploy Workspace and Data Download
The option "Data Download" downloads a Data Snapshot from a connected A12 application and
directly integrates it to the current Workspace. Using this feature requires that the SME is
connected to an A12 application that has the Workspace Data endpoints enabled.
The Data Download has the following content:
• Documents
• Links
• Attachments
• Users
• Roles
• Seed Metadata
Models are explicitly not contained in the Data Download.
The SME creates subfolders in the data/documents and data/links folders for each Document Model
and Relationship Model referenced by the downloaded data if they do not yet exist.
If Workspace Data already exists and differences between an existing and the downloaded
Document are detected, the SME displays a modal that summarizes the differences and enables
selecting which parts of the downloaded data should be integrated into the Workspace.
11

-- 11 of 12 --

Figure 10. Configure Data Download Modal
If a document with the same name already exists in the Workspace, the following options are
available for each Document:
• Keep existing Document: The existing Document in the Workspace is kept, and the downloaded
Document is ignored.
• Replace with downloaded Document: The existing Document in the Workspace is replaced by
the downloaded Document.
• Keep both Documents: The existing Document in the Workspace is kept, and the downloaded
Document is added to the Workspace with a modified name. The SME adds a suffix "_copyX" to
the name of the downloaded Document, where X is a number that is incremented until a unique
name is found.
Links are always replaced and updated, hence they are only listed in the summary without
selection options. Please be aware that keeping existing Documents may lead to inconsistencies if
Links reference Documents that are not replaced or added.
12

-- 12 of 12 --

