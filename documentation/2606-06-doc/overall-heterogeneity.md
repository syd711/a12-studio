# overall heterogeneity

Modeling Heterogeneous Data
Business objects that share a set of properties can be grouped together by using
a common Supertype. The Supertype Document Model can be referenced in UI
Models, such as Overview Models, and also in Relationship Models. This allows
users to work with Subtypes Documents of the referenced Supertype Document
Model as if they were all Documents of the same type. Thus, the same overview,
form or content page can show documents of different Document Models.
Likewise, Links to any of the different Subtype Documents can be created and
shown in a single Binding.
In addition to loading and linking Subtype Documents "as equals", Subtype-specific models can be
shown when adding or editing a document. This can be specified in the Master Detail Module
Model or the Application Model.
Important To Note
CAUTION Fields that are used in a Supertype’s UI model must have the exact same path in
all of the Subtype Document Models.
Below are the three Document Models that make up our Supertype and Subtypes. While other
Fields may exist in the different Subtype Document Models, all Fields that we intend to display with
the Supertype UI model must have the exact same path and matching data types in all Document
Models.
1

-- 1 of 8 --

Figure 1. Document Models Showing Field Structure
2

-- 2 of 8 --

If Fields do not exist in one of the Subtype Document Models, then they are treated as they are
empty in the respective documents.
Sample Heterogeneous Data
The A12 installer contains an example for heterogeneity modeling in the Advanced workspace.
Figure 2. Sample Application Heterogeneous Data Example
In this example we have two entities described by the models Team_DM and Person_DM, there is
an n - n relationship between these two entities, TeamPerson meaning that a team can contain
many people and a person can belong to multiple teams.
Teams are made up of people who are either Employees or Freelancers. Both Employees and
Freelancers are separate entities described by different Document Models, PersonEmployee_DM
and PersonFreelancer_DM. They also use different Form Models to allow their unique Fields to be
completed.
When I see a list of people in the end application, I want it to include both Employees and
Freelancers. This is achieved by using Heterogeneity. In this example, Person_DM is the
Supertype and PersonEmployee_DM and PersonFreelancer_DM are Subtypes.
In the examples below we can see an overview for the People module that is showing mixed data.
We can tell which items are Freelancers because they have a Contract end date listed. You can see
that when an Employee is selected, the Employee Form is displayed and when a Freelancer is
selected in the same overview the Freelancer Form is displayed.
3

-- 3 of 8 --

Figure 3. Employee Form
Figure 4. Freelancer Form
Supertype Entity
The Supertype is currently a concrete entity (or may also be a set of models) in the application. An
overview displaying a heterogeneous list of data can be modeled by creating an Overview Model
based on the Supertype. Per-default, this leads to all Supertype Documents as well as its Subtype
Documents being loaded. A generic form or content page may also be modeled to display both
Supertype and Subtype Documents.
4

-- 4 of 8 --

Sometimes not all entities in such a hierarchy shall actually be instantiated. In the given Sample
Heterogeneous Data you have a Supertype People with Subtypes Employees and Freelancer. Then
you might want all documents to be either one of the Subtypes, but never the Supertype People. For
this reason you can mark the Supertype Document Model as abstract. This means, when creating a
new document in a heterogeneous context, such an abstract Supertype will not be offered.
Editing the Supertype or SubType Document Model
Linking the Supertype with its Subtypes is done in the Document Model by adding an Annotation.
There are two different Annotations that you can use, subTypes and superTypes. You only need to
use one Annotation to connect the Supertype with its Subtypes and you can choose which
Document Models you add the Annotation on. It is best to decide early on which Annotation you
want to use, so you should not combine them during modeling.
NOTE During runtime all superTypes Annotations will be transformed into subTypes
because this is what UI needs.
If you add the annotation to the Supertype model, in this example Person_DM, open the
Supertype Document Model in the SME and open the Model Settings by clicking on Settings and
you will see the section for Annotations. Enter an Annotation with the name subTypes and for the
value, add a comma delimited list of Subtype Document Models. It is important not to have any
spaces in this list, the Subtypes are separated by a comma only, no spaces.
Figure 5. Adding an Annotation to the Supertype Document Model
Alternatively, it is also possible to add annotation superTypes with value Person_DM to
PersonEmployee_DM and PersonFreelancer_DM to make them Subtypes of the Person_DM
model. The annotation superTypes also accepts comma delimited list of Supertype references
(Document Model names).
5

-- 5 of 8 --

After you’ve added subTypes or superTypes to the Document Model you can add another
annotation if the Supertype shall be abstract. If your Supertype shall not be instantiated, add an
Annotation with name abstract and value true. Please be aware of the fact that, like explained, an
abstract type can never have a document instance. It will be checked and refused by the server if a
document is created for an abstract type. For that reason it makes only sense to have an abstract
Document Model in a heterogeneous context with at least one subtype for the moment.
NOTE
You can use Combination Modeling to specify the Subtypes by adding Fields and
Rules to the Supertype Document Model. But be aware, that neither the Annotation
subTypes nor the Annotation abstract can be set on the Supertype Document Model,
as the Annotations would be added to the Combined Document Models as well.
You can create a dedicated Combined Document Model for the Supertype and set
the Annotation subTypes or abstract in the Model Settings for the Combined
Document Model.
It is also possible to set the Annotation superTypes in the Model Settings for the
Combined Document Models of the Subtypes.
Heterogeneity in A12 Engines
Overview Engine
To use heterogeneity in an overview, select the Supertype Document Model as Document Model
reference in the Overview Model or in the selected Query Model. Then, documents of all Subtypes
will be displayed in this overview. The columns will be filled with data from the respective fields in
the Subtype documents.
When the enduser clicks the add-button, the Variant Dialogue opens and shows all (nested)
Subtypes of the Supertype referenced in the overview. The enduser can select any Document Model
that is not marked as abstract and a document of the respective type will be created.
Figure 6. Adding an Item to a Heterogeneous List
The localized labels of the Document Models are used in this dialogue. If the top-level Supertype
Document Model is abstract, it will not be displayed in this dialogue, however, intermediate-level
abstract types will be displayed.
6

-- 6 of 8 --

It is also possible to have buttons that immediately add a specific Subtype document, skipping the
dialogue, or only allow a limited selection of Subtypes to be added. The button can be modelled, but
the specific event has to be created by a developer.
Relationships
Heterogeneity can be used in relationships as well. To achieve that, use the Supertype as reference
for a Role in the Relationship Model. This Supertype Document Model has to be used as reference in
all models required to display the relationship links for this role.
In a binding with a heterogeneous target role, the documents of all Subtypes will be offered to
create links to.
Tree Engine
Heterogeneous relationships can also be rendered by the tree engine. All aspects of a tree work
with heterogeneous relationships as they do with non-heterogeneous relationships.
When inserting a child, the same Variant Dialogue is displayed as in heterogeneous overviews,
using the label of the Document Models as well. Adding links makes use of bindings, which work
the same as in any heterogeneous relationship. The order of heterogeneous siblings in a tree is not
divided by their Subtype. This can be seen in the Advanced workspace where Employees and
Freelancers linked to a Team are displayed in any order.
NOTE
Per-default, the Child documents are grouped by the Document Model reference.
Heterogeneity allows the Documents to be mixed as the Documents are grouped by
the SuperType Document Model.
Figure 7. Example of a Heterogenous Tree: Employees and Freelancers Are Heterogeneous Children of
Teams
It is possible to have row actions that immediately add a specific Subtype document, skipping the
Variant Dialogue. Those buttons and actions are fully model-able. It is also possible to add a button
to only allow a limited selection of Subtypes to be added. The button can be modelled, but the
specific row action has to be created by a developer.
To make use of Heterogeneity in the Tree Model, the following models are required:
7

-- 7 of 8 --

• Document Models for the Supertype and Subtypes
• Form Models for the Subtypes
• Form Model for the Supertype, if it is not annotated as abstract
• Relationship Model referencing the Supertype Document Model in one of its Roles
There are three different ways to include heterogeneous nodes in the Tree Model:
Add the Supertype as Node
If the Subtypes should be shown uniformly with the same icons, data in columns and row actions,
then the Supertype should be added as Document Model Reference of a node.
Only one node for all heterogeneous Subtypes needs to be maintained.
Be aware that for any child relationship configurations, only relationships in which the Supertype
represents the parent role can be selected.
Add the Subtypes as Nodes
Add one node per Subtype, with each respective Subtype Document Model as Document Model
reference.
The node settings of all Subtypes can differ. It is also possible that Subtype nodes inherit selected
settings from the Supertype’s node. This can be specified individually on the Subtype node.
There can also be different child relationship configuration to show links that only spawn from one
heterogeneous Subtype.
A Mix of Supertype and Subtypes as Nodes
It is also possible to have a mix of both options above. If a node for the Supertype as well as a
Subtype exist, the Subtype node is considered first by the tree engine regarding the node settings.
Model this way if you have several Subtypes to one Supertype, where a portion of the Subtypes
have the same node settings, another portion having differing node settings.
Adding a Heterogeneous Module to a
Preview App Workspace
To add a heterogeneous module to a Preview App workspace, use a Master Detail Module Model. It
fully supports heterogeneity in overviews and trees.
8

-- 8 of 8 --

