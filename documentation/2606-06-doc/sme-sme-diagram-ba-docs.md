# sme sme diagram ba docs

Model Graph Diagrams and the Data
Modeling Perspective
This documentation is intended for a business analyst audience.
Introduction
A Model Graph Diagram (MGD) is a view of a set of Data Models, specifically the Relationship and
Document Models of a Workspace. It allows business analysts to visualize the connection between
Document Models (Relationships and Heterogeneity) or to create these entities in a visual way.
The SME Workspace Explorer does not show the connections (for example, references) between
Document and Relationship Models, as it is presented as a file tree. Since the Workspace contains
numerous models, it is difficult for analysts to maintain an overview of the modeled connections.
This gap is bridged by the Data Modeling Perspective, which provides structured and filtered
views of the Workspace based on MGDs. Persisted changes to the models are directly reflected in
the diagram and vice versa.
In addition to graphically illustrating the connections of the created models, all modifications to the
models can still be made with their respective editors. Thus, the Data Modeling Perspective offers
an alternative modeling method.
Table of Contents
Introduction . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 1
Features and Model Graph Diagram Elements . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
Basic Interaction With the Model Graph Diagram. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
Representation of the Model Graph in Model Graph Diagrams . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
Model Graph Explorer . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
Creating Model Graph Diagrams. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Modeling With Model Graph Diagrams. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
Custom Layouts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
Editing Relationship and Document Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
New Relationship Models. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
Adding Models to a Diagram . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
Remove Models From a Diagram . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
Visual Settings. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
1

-- 1 of 15 --

Features and Model Graph Diagram
Elements
The Data Modeling Perspective provides the following features to analysts:
• Enables Creating Model Graph Diagrams.
• Allows creation and editing of multiple MGDs with a Custom Layout and saving them in the
Workspace.
• Supports editing of existing Relationship and Document Models.
• Supports creation of new Relationship Models.
• Allows Adding Relationship and Document Models to an MGD.
• Allows Removing Relationship and Document Models from an MGD.
Basic Interaction With the Model Graph Diagram
• Select an element: left click
• Select multiple elements by clicking: hold the CTRL (CMD on MacOS) key and left click
• Select multiple elements by dragging: left click on empty area, hold and drag to draw a selection
box
• Move an element or multiple selected elements: left click on a selected element, hold and drag
• Pan the canvas (move the view): right click on empty area, hold and drag
• Create a new Relationship: left click on a Port of a Document Model, hold and drag to a Port of
another Document Model
• Zoom in/out: mouse wheel scroll up/down
• Open editor: double left click on an element
Representation of the Model Graph in Model Graph
Diagrams
In MGDs, the Model Graph is represented by nodes and edges. Each node corresponds to a
Document Model. The text in the node shows the name of the corresponding Document Model. Each
solid edge in the diagram corresponds to a Relationship Model. Nodes and edges are connected via
Ports.
2

-- 2 of 15 --

Figure 1. The Data Modeling Perspective of the SME Displaying a MGD
An edge may have labels displayed, which can be switched on and off by the user (see Visual
Settings). These labels can be used to display more detailed information about a Relationship and
its Entity Characteristics. The name of the Relationship Model is displayed in the middle label. The
labels at the ends of the edge indicate the respective role in the Relationship, the multiplicity, and
the additional properties "orderable" and "duplicable" of the Relationship Model when set. If a Link
Document Model is specified in the Relationship, it is connected to the edge via a dashed line. To
emphasize the special role of the Document Model, the corresponding node is also shown with a
dashed border.
Subtype connections between Document Models in the diagram are visualized by edges with
arrowheads. The arrowhead points to the Supertype. The foundations for Subtype modeling are
provided by the documentation about Heterogeneity.
Every diagram contains all edges that can be shown given the list of Document Models contained in
the diagram. Thus, adding new Relationships or Subtype connections between Document Models
will lead to new edges in the diagram. Conversely, removing Document Models from the diagram
(or deleting them in the Workspace) will remove all connected edges.
NOTE Elements that are not part of the Model Graph are not included in the diagram (for
example, Overview Models).
The MGD is displayed as read-only with grey-colored nodes if a Model Editor is open at the same
time. The other editor is not displayed while the Data Modeling Perspective is maximized;
therefore, the perspective must be minimized again in order to close the other editor and make the
3

-- 3 of 15 --

MGD editable again. Navigation via the SME Workspace Explorer is possible at any time.
Figure 2. The Data Modeling Perspective in Read-Only Mode
Model Graph Explorer
The Model Graph Explorer in the Data Modeling Perspective provides an overview of the Document
and Relationship Models in the Workspace. It can be opened by clicking the library icon in the top
left corner. The Document and Relationship Models can be filtered for those contained in the
diagram or those not contained in the diagram. Both model types may also be newly created via the
"Add" button in the Model Graph Explorer. Furthermore, it offers a context menu, which is
especially used to add or remove Models from the MGD (see chapters Adding Models to a Diagram
and Remove Models From a Diagram). The Model Graph Explorer can be closed by clicking the 'X' in
the upper right corner.
4

-- 4 of 15 --

Figure 3. Model Graph Explorer and Context Menu for Contained Models
The options "Focus in Diagram" and "Remove from Diagram" are only available for contained
models. They are shown with a grey checkmark.
Creating Model Graph Diagrams
MGDs can be created and edited in the Data Modeling Perspective of the SME. This perspective
can be opened by clicking the graph icon on the left-hand side navigation.
If the Workspace does not contain any MGDs yet, the Data Modeling Perspective shows an empty
screen. By clicking the add button in the center of the perspective, a new diagram can be created.
5

-- 5 of 15 --

Figure 4. Data Modeling Perspective Without Existing Model Graph Diagrams
Alternatively, new MGDs can be created via the Workspace Explorer, in which they are contained in
the list of available model types.
For a new MGD, users must specify the folder in which it will be contained and the name of the
diagram. There is a select in regard to the initial state of the newly created diagram that offers a
selection of:
• All related Document Models, means which are referenced at least once as an entity in a
Relationship Model
• None, means an empty diagram is initialized
• Custom Selection, providing a view on available Document Models from the Workspace for
selection
6

-- 6 of 15 --

Figure 5. Adding a New MGD
Figure 6. Adding a New MGD: Custom Selection of Document Models
Once name and folder have been set, the MGD can be inspected and modified in the Data Modeling
Perspective. As a Workspace may contain multiple MGDs, a new one can be added with the "Add
Diagram" button and one can switch between the MGDs in a drop-down next to it. The possibility to
create multiple MGDs is particularly useful if the data modeling gets more complex and individual
parts of the structure shall be reflected in the Diagram. Such excerpts might be used as a basis for
7

-- 7 of 15 --

Tree Models, for instance.
After the selection of contained Document Models, the diagram opens with an initial layout that is
possibly suboptimal. The layout allows the user to see all models contained in the diagram. The
diagram shows all Relationships between the contained Document Models. If there are Subtype
connections between the contained Document Models, these are also visualized.
The Data Modeling Perspective offers basic functions to "Save" or "Cancel" a diagram in the bottom
right corner. "Save" will save the current user-defined layout in the selected location of the
Workspace. The user can also discard changes since the last saving with the "Cancel" button.
Furthermore, the Data Modeling Perspective can be closed in the top right corner as well as
maximized/minimized.
Modeling With Model Graph Diagrams
Based on the initial layout of an MGD which will be created based on the selected Document
Models, modelers can start their work. Modelers have the freedom to work in the order of their
choice and the following section will guide through the available features (like creation of custom
layouts, create new models in the Data Modeling Perspective; edit models).
Custom Layouts
It is possible to create user-defined layouts. The user can move the diagram elements by dragging
them. To start dragging, the mouse must first be positioned over the element to be dragged. Then,
press and hold down the left mouse button and drag the element to the desired location. The drag
operation ends when the button is released. Multiple elements can be selected by pressing the CTRL
key via dragging onto an empty area on the canvas. If the resulting bounding box of the selection
completely covers a node, it is added to the selection. Then, clicking and dragging a node moves the
whole selection.
8

-- 8 of 15 --

Figure 7. The Data Modeling Perspective Provides Alignment Guidance for Document Model Elements
Relationship edges can be adjusted by dragging their segments. In addition, the starting point of an
edge at a node (called Port) can be adjusted. To do this, hover over the start or end point and
interact with the Port that appears. The interaction pattern is similar to moving an element. The
edge can be detached from a Port by dragging it. The modeler can drag it to another Port at the
same node and drop it there to finish the relocation. If the Port is already being used by another
edge, or if the modeler releases the edge at another location than a Port, it jumps back to its original
Port.
Figure 8. Available Ports on Document Model Elements Displayed on Hover
9

-- 9 of 15 --

Figure 9. Draggable Starting Point (Port) of a Relationship at a Document Model Displayed on Hover
Editing Relationship and Document Models
The editor for each model type can be opened by double-clicking on its respective Element. For
Relationship Models, it is possible to target the edge as well as any of its corresponding labels.
Figure 10. Editing an Existing Relationship Model in the Diagram
The Document Model Editor opens in the same manner.
To quickly modify the related Document Models of a Relationship Model, it is also feasible to
reattach an existing Relationship Edge to a different node to easily modify the related Document
Models of a Relationship Model. After reallocating the Relationship Edge, a dialog will be displayed,
prompting the user to adjust the Entity Characteristics before confirming the modification.
10

-- 10 of 15 --

Figure 11. Entity Characteristic Editor
In the dialog, the new Document Model name as well as the previous Document Model name of the
entity are displayed read-only. The remaining properties can be freely edited, and the Relationship
Model will be saved when the dialog is confirmed. If the checkbox for (re)generating Document
Models is checked, the workflow will be triggered afterwards.
Similarly, a Subtype edge can be reallocated, triggering the display of a confirmation box. If the user
confirms the reallocation, the related Document Models are updated. In addition, warning
messages notify the user if a cycle is generated with the most recent reallocation or if a Subtype
connection already exists.
Figure 12. Warning About Reconnecting Edge to Be Confirmed
New Relationship Models
To create a new Relationship Model between two Document Models, the node from which the
11

-- 11 of 15 --

Relationship should originate must first be hovered. Then the mouse must be moved over the Port
from which the edge should start. By pressing and dragging, a new edge can be created, which can
be released at a free Port at the destination node. Then, the familiar modal for creating a new
Relationship Model opens. In addition, the diagram in the background visually indicates that a new
model is being created. When the modal is confirmed, the temporary Relationship Model is
displayed in the diagram. Furthermore, the Relationship Model Editor for editing opens on the right
side. In contrast to creating a new Relationship Model via the Workspace Explorer, it reduces the
modeling workload for business analysts by pre-filling the related entities with the referenced
Document Models.
NOTE A newly created Relationship Model is temporary until it is saved.
Figure 13. Prefilled Document Model References in New Relationship Model
Adding Models to a Diagram
It is possible to add Document Models from the Workspace manually to the diagram, for example,
to create new Relationships to them. This functionality is provided by the Model Graph Explorer.
An unused Document Model that is present in the Workspace can be added to the diagram by drag
& drop from the table to the diagram.
12

-- 12 of 15 --

Figure 14. Drag & Drop Document Model to Add to Diagram
Models already included in the diagram cannot be added again. Relationship Models that have been
created outside the diagram after the diagram has been created can also be dragged into the
diagram.
NOTE If a Relationship Model has less than two Related Entities present in the diagram, it
cannot be added (no dangling edges).
Remove Models From a Diagram
Document and Relationship Models can be removed from the diagram. This can be done without
deleting the corresponding models. This feature is available from the context menu of the models
in the Model Graph Explorer.
13

-- 13 of 15 --

Figure 15. Removing a Document Model From the Diagram
Visual Settings
To allow for the creation of clean diagrams, it is possible to adjust the label visibility. The setting can
be applied as default to each Relationship. The default label visibility settings are adjustable via a
button in the upper right corner of the canvas.
Figure 16. Default Settings of the Labels of Relationships
Any settings for individual Relationships can override these defaults. This way it is possible to apply
separate settings for a specific Relationship. The settings option for an individual Relationship can
be changed when a Relationship Edge is selected via a single click.
14

-- 14 of 15 --

Figure 17. Display Settings for an Individual Relationship
NOTE The settings panel will switch depending on the selection context in the diagram.
There is an option to reset individual settings and apply the default visibility settings again.
15

-- 15 of 15 --

