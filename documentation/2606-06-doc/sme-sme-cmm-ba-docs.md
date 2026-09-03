# sme sme cmm ba docs

Creating an A12 Document by Using
a Combination Model
Table of Contents
1. Introduction and Concepts. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
1.1. Example Use Cases . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 2
1.1.1. Use Case: Reuse Business Logic . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
1.1.2. Use Case: Heterogeneity Modeling . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
1.2. Terminology to Describe the Modeling . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
1.3. Comparison of Include, Addition and Decoration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
1.3.1. Is the fragment self-contained and also used stand-alone? . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
1.3.2. Are Validation or Computation Rules in the fragment that need to reach into the
Reference Model? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
1.3.3. Should the fragment be applied multiple times to the same Reference Model? . . . . . . . . . . 8
1.3.4. Should the added elements always have the same absolute path? . . . . . . . . . . . . . . . . . . . . . 8
1.3.5. Business Requirements Determine the Modeling Strategy. . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
2. Editor Functions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
2.1. Combination Model Contents . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
2.1.1. Combination. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
2.1.2. Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
2.1.3. Preview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
2.1.4. Rule Contradictions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
3. Editor for the Additive Document Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
3.1. Addition Mechanism . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
3.1.1. Properties of Overwritten Elements. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
3.1.2. Order of Elements. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
3.2. Sidebar . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 18
3.3. Model Tree . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 18
3.3.1. Element Editors . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
3.3.2. Actions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
Expand and Collapse . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 21
Add an Element . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 21
Copy and Paste. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
Cut and Paste . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
Insert from DM . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
Delete . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
Multi-Selection and Bulk Operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
Move. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
1

-- 1 of 38 --

This documentation is intended for a business analyst audience.
CAUTION
The Combination Modeling features are not activated per default. They must be
enabled in the Simple Model Editor Tool Settings before use; see Enabling
Module Types.
1. Introduction and Concepts
The Combination Model Editor is part of the Simple Model Editor and enables domain experts and
analysts to create and modify Combination Models for business applications. The Combination
Model defines how an existing Document Model, referred to as the Base Model, is altered by adding,
decorating or selecting Document Model Elements to create a new, modified Document Model. The
Combination, in form of its resulting Document Model, can be used like any other Document Model
in the Simple Model Editor: to build a Form or Overview Model, to define a Mapping Model or to be
used as Base Model in another Combination Model. In the editor and this documentation, the
Combination Model and the resulting Combined Document Model are used synonymously. For
Modelers, these are just the two sides of the same coin.
1.1. Example Use Cases
3.3.3. Additive Elements Only . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
3.3.4. Search and Filter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
3.4. Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
3.5. Type Definitions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
3.6. Rule Contradictions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
3.7. Refactoring Across Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
4. Editor for the Selection Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
4.1. Selection Mechanism . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
4.2. Sidebar . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
4.3. Selected Elements . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
4.3.1. Search and Filter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
4.4. Selection Specification. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
4.5. Selection Mechanism . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
4.6. Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 33
4.7. Refactoring Across Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
5. Decoration Mechanism . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
5.1. Decoration For Fields. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
5.2. Decoration For Groups . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
5.3. Validation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
6. Glossary . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 38
2

-- 2 of 38 --

1.1.1. Use Case: Reuse Business Logic
Combination modeling can be used to manage business logic in one place and reuse it in different
domains. It can be compared with Type Definitions and Includes. Take the following example
Document Models:
Figure 1. Example Document Models CustomerIndividual_DM and CustomerCompany_DM
The four address blocks, BillingAddress, ShippingAddress, Offices and ShippingAddress share the
same set of Fields and Rules. Moreover, the Fields Country, BornIn and RegisteredIn should allow
the end user to select a country from a predefined list. If the list of countries is to be modified later,
we want to do this only once in our application. Otherwise, this could lead to inconsistencies, let
alone that it would be tedious. This is why A12 allows you to extract common Type Definitions into
a single model and provide a single source of truth. Equally, entire structures of Fields and Rules
can be reused in the form of Includes.
The pair of Field <Name>Proofed and Validation Rule <Name>NotProofedWarning also follows a
common pattern. This pair is present next to the special Fields Name, TaxID and StreetAndNumber
of the contact addresses. The Validation Rule produces a Warning if the respective Field is filled, but
<Name>Proofed is not set to true. A12 provides also a way to centralize the definition of such
structures and apply them consistently. It is called Decoration.
3

-- 3 of 38 --

Figure 2. Centralizing Business Logic into Type Definitions, Includes, Decorations and Additive Document
Models
On the left-hand side of Figure 2 the common part of CustomerIndividual_DM and
CustomerCompany_DM are extracted into a Type Definition and a Document Model. These can then
be referenced as Includes in the domain models.
On the right-hand side of Figure 2 the common Rule ShippingRestrictions of
CustomerIndividual_DM and CustomerCompany_DM is extracted into an Additive Document Model.
On the bottom of Figure 2 the pair of Field <Name>Proofed and Validation Rule
<Name>NotProofedWarning are extracted into a Document Model together with an Anchor Field
called Field. The special naming conventions and the Decoration Mechanism turn this decoration
fragment into the respective elements shown in Figure 1.
The Document Model for Decoration and the Additive Document Model can then be used in
different Combinations. The Combination can be compared to the canvas-and-layers-approach of
graphic and photo editors (see Figure 3): The Base Model, in this case CustomerIndividual_DM or
CustomerCompany_DM, acts as the canvas. Different Additive Document Models can be placed on
top of this canvas. They have a transparent background, so for the Additive Document Model, the
canvas (the Base Model), is visible and the elements therein can be referenced in the Additive
Document Model. Each of the Combination Steps (Addition, Decoration, Selection) is a Layer in this
stack. So the Decoration, that is applied after the Addition, does not only "see" the elements of the
Base Model, but also the elements added via the previous Addition step.
[introduction 03] | assets/introduction_03.png
4

-- 4 of 38 --

Figure 3. Additions Sketched as Stack of Layers
1.1.2. Use Case: Heterogeneity Modeling
Combination Modeling can also be used to create different Document Models from one existing
Base Model. So different Additions are done to the same Base Model. Therefore, Combination
Modeling is especially useful when working with Heterogeneity, as it keeps the Supertype Model
(the Base Model) consistent with all the Subtype models (Base Model + Additions). When modeling
the Subtypes as Combined Document Models with the Supertype as Base Model, then all changes to
the Supertype are automatically forwarded to the Subtypes.
NOTE
Annotations of the Base Model are taken over into the Combined Document Model.
This is done for the Annotations "superTypes" and "subTypes". Thus, the reference
to the Supertype must be set on the Combined Document Models with Annotation
"superTypes" and the name of the Base Model as value.
The Annotation "abstract" is an exception, as it must be set on the Supertype
Document Model. The Combination Model Editor will ignore it and show a warning,
that this Annotation is not taken into the Combined Document Model.
1.2. Terminology to Describe the Modeling
To describe the Combination Modeling, the following terms are used:
Base Model
A Document Model or a different Combined Document Model, that is the basis on which the
Combination Steps act.
Combination Step
Combination Steps can be of type Addition, Selection, Decoration For Fields or Decoration For
Groups. An Addition adds a set of further elements once onto the Reference Model. Selection
allows you to remove elements from the Reference Model. Decoration adds a set of further
elements in a regular manner multiple times to the Reference Model.
Reference Model
The Reference Model shows the result of all Combination Steps up to that point. It is determined
from the Base Model and the Combination Steps defined in the Context Model.
As a result, the Reference Model is the model that you see in the individual subeditors to support
Addition, Selection or Decoration.
1. In the first Combination Step, the Base Model is the Reference Model.
2. In the second Combination Step, the result of applying the first Combination Step to the Base
Model is the Reference Model. And so on.
In the Additive Document Model Editor, the Reference Model is shown as read-only elements.
They can be used in added Validation and Computation Rules without being present in the
Additive Document Model.
5

-- 5 of 38 --

In the Selection Model Editor, the Reference Model is shown as a model tree with checkboxes to
support the filling of the Selection Specifications.
Context
Context is another way of referring to the Reference Model required when editing Additive
Document Models and the Selection Models. As Additive Document Models and the Selection
Models can be referenced in multiple Combination Models, opening their respective Editors can
only happen when the Context is clear.
Opening an Additive Document Model or a Selection Model therefore happens in one of two
ways:
1. The Additive Document Model or a Selection Model opens immediately as the context is
clear.
2. The User must first select the relevant Context.
NOTE
Without the correct Reference Model, the model tree shown in the Selection
Editor would be empty; and Computation or Validation Rules in Additive
Document Models might be shown as invalid.
Fragment
The collection of Groups, Fields, Validation and Computation Rules (or other Document Model
elements) that are to be reused in an Include, Addition or Decoration.
1.3. Comparison of Include, Addition and Decoration
The three different mechanism to reuse business logic can be compared along the following
questions:
1. Is the fragment self-contained and also used stand-alone?
2. Are Validation or Computation Rules in the fragment that need to reach into the Reference
Model?
3. Should the fragment be applied multiple times to the same Reference Model?
4. Should the added elements always have the same absolute path?
1.3.1. Is the fragment self-contained and also used stand-alone?
If so, model a Document Model and Include it as required.
NOTE Includes can be placed in different structural contexts with different Repeatability
into the same hosting Document Model.
Table 1. Is the to-be-reused fragment self-contained and can be used stand-alone?
6

-- 6 of 38 --

Include Addition Decoration
Yes, valid Document Model No, it might be a partial
Document Model
Yes, valid Document Model
Example
from
Figure 2
Address_DM is self-
contained and validated by
the Workspace Validation.
There could be Documents
of Address_DM in the
database.
Rule ShippingRestrictions
references the
IsVIPCustomer and
ShippingAddress/Country
which are not contained in
ShippingRules_AdM.
ShippingRules_AdM is not a
valid Document Model on its
own.
Proofing_DM is self-
contained and validated by
the Workspace Validation.
1.3.2. Are Validation or Computation Rules in the fragment that need to
reach into the Reference Model?
If so, and the fragment is only added once to each Reference Model, model an Additive Document
Model.
NOTE Additive Document Models provide maximum flexibility when referencing Fields
and Groups not contained in the model.
If so, and the fragment needs to be applied multiple times, use a Decoration for Fields or Groups.
NOTE
Rules in the Document Model for Decoration can reference the Anchor Element,
that represent the to-be-decorated element of the Reference Model. Only exactly this
element can be referenced in the fragment.
Table 2. Can Validation or Computation Rules reach into the Reference Model?
Include Addition Decoration
no yes only to the to-be-decorated
element
7

-- 7 of 38 --

Include Addition Decoration
Example
from
Figure 2
Rule CorrectCodePattern
defined in Address_DM can
only reference the Fields of
that Document Model.
Rule ShippingRestrictions
references IsVIPCustomer
and
ShippingAddress/Country
which are not present in
ShippingRules_AdM but
only in the Reference Model.
Consequently, this could not
be moved into an Include, as
the to-be-included
Document Model would not
"see" the needed Fields of
the Host model.
Rule
FieldNotProofedWarning
references the Anchor Field
Field and FieldProofed. Both
are present in the Document
Model used for Decoration.
However, the Anchor Field
acts as a placeholder,
making the Rule effectively
referencing the to-be-
decorated elements of the
Reference Model, namely
Name, TaxID,
BillingAddress/StreetAndNu
mber and
Offices/StreetAndNumber.
1.3.3. Should the fragment be applied multiple times to the same Reference
Model?
If so, use Includes or Decorations depending on the use case.
NOTE Document Models for Decoration can be applied multiple times to the same model
by selecting different Groups or Fields to be decorated.
Table 3. Can the fragment be applied multiple times to the same model?
Include Addition Decoration
yes, also with different
Repeatability.
no yes
Example
from
Figure 2
• Address_DM included
twice each in
CustomerIndividual_DM
and
CustomerCompany_DM
• in
CustomerCompany_DM
included with different
Repeatability
• ShippingRestriction can
never be added more
than once
• Decorating elements
defined in Proofing_DM
added once for each
selected Reference
Model element
• with the respective
Repeatability
1.3.4. Should the added elements always have the same absolute path?
If so, use Additive Document Models.
Table 4. Is the position of the fragment elements conserved?
8

-- 8 of 38 --

Include Addition Decoration
• Relative paths within the
fragment conserved
• Absolute paths
dependent on the
position of the Include.
• Absolute Paths of the
fragment elements is
conserved
• Relative position within
the fragment conserved
• Absolute paths and
names dependent on
path and name of the
selected Reference
Model elements
Example
from
Figure 2
• StreetAndNumber has
different absolute paths
for the contact address
blocks, because the
name of the Include
differs.
• In both resulting models
there is
/ShippingAddress/StreetA
ndNumber, because the
name and path of the
Include is the same. But
changing the name in
one model does not
automatically refactor it
into the other.
• ShippingRestriction is
always added in the
model at the same
absolute path. Changing
its name or path in the
Additive Document
Model, changes it in all
resulting models.
• Compare TaxIDProofed
and the two instances of
StreetAndNumberProofe
d, the Fields are added
as siblings of the
selected Fields. The
names and paths
correspond to the
selected Fields. The
absolute paths and
repeatability of the three
resulting Fields are all
different.
1.3.5. Business Requirements Determine the Modeling Strategy
Many business requirements can be solved with a range of modeling strategies. Technical
requirements, model structure clearness or modeling preferences might give the final decision.
You could restructure the Document Models in Figure 2 to allow you to encapsulate the needed
Fields together with the Rule ShippingRestriction into one Document Model and then include this
into the two models CustomerIndividual_DM and CustomerCompany_DM. But if the number of
parallel types grows (CustomerCompanySE_DM, CustomerCompanyNonEU_DM,
CustomerCompany_ASEAN_DM, …) and more and differently applied Rules are added, the Additive
Modeling Approach will be much clearer.
If a set of Fields and Rules is only to be added once to a Document Model, it would be possible to
add those as an Include or an Additive Document Model. This could limit modeling flexibility,
because the Fields would always have the same path. If for example the BillingAddress would be
modeled as an Additive Document Model, it would not be possible to rename the structure to
MainOffice in CustomerCompany_DM. However, this loss in flexibility might also be an advantage,
as it would keep the two models CustomerCompany_DM and CustomerIndividual_DM alike.
Modeling mistakes are then less likely.
And of course: It is possible to use Includes in Additive Document Models and to Include Combined
Document Models.
9

-- 9 of 38 --

2. Editor Functions
Selecting an existing Combined Document Model in the Workspace Explorer of the A12 Simple
Model Editor, or creating a new one, opens the Combined Document Model Editor.
To create a new Combined Document Model, use the "ADD" button in the header of the Workspace
Explorer or in the context menu of a folder and select "Combined Document Model".
Figure 4. Add a New Combined Document Model
A modal will then be displayed to define the most important model settings: Folder and Name.
10

-- 10 of 38 --

Figure 5. Enter Initial Model Settings for New Combined Document Model
The Name of the model must be unique in the Workspace.
The same Locales as in the Base Model should be added here. They will be prefilled when selecting
a Base Model from the dropdown.
2.1. Combination Model Contents
The Combination Model Editor can be seen in Figure 6.
11

-- 11 of 38 --

Figure 6. Combination Model Editor - Combination Screen
2.1.1. Combination
The Combination Screen contains the following elements:
Base Document Model
Select a (Combined or Transformed) Document Model from the dropdown list. The first
Combination Step will act on this model. It is used as Reference Model in the editor of the first
Combination Step.
Combination Steps
A list of up to 99 steps determining the composition of the Combined Document Model. The steps
are processed from top to bottom. Use the ADD button to add a new entry. The entries can be
reordered or deleted.
The result of each Combination Step can be validated individually with the "Validate model up to
this step"-button (two checkmarks). It applies all Combination Steps previous and the respective
step to the Base Model and validates the result. This allows you to find modeling errors in
complex modeling scenarios.
Type
Combination Steps can be of one of the following types:
• Addition
• Selection
• Decoration For Fields
• Decoration For Groups.
An Addition adds a set of further elements once onto the Reference Model. Selection allows
12

-- 12 of 38 --

you to remove elements from the Reference Model. Decoration adds a set of further elements
in a regular manner multiple times to the Reference Model.
Model for Addition
A mandatory Field for Combination Steps of type Addition. Select an Additive Document
Model from the dropdown or add a new model to the workspace. Clicking the Pen button
opens the Editor for the Additive Document Model. The Reference Model shown in this editor
is the Base Model with all previous Combination Steps applied.
Model for Selection
A mandatory Field for Combination Steps of type Selection, Decoration For Fields, or
Decoration For Groups. Select a Selection Model from the dropdown or add a new model to
the workspace. Clicking the Pen button opens the Editor for the Selection Model. The
Reference Model shown in this editor is the Base Model with all previous Combination Steps
applied.
Model for Decoration
A mandatory Field for Combination Steps of type Decoration For Fields or Decoration For
Groups. More information about the Decoration process can be found here.
Select a Document Model from the dropdown or add a new model to the workspace. Clicking
the Pen button opens the regular Document Model editor. The following rules are not
enforced within the opened Document Model editor but only on return to the Combination
Model Editor or when selecting a Document Model:
• Decoration For Field: the Document Model must contain exactly one Root Group with a
Field named 'Field' (the Anchor Field). Siblings of this Field must have the string 'Field' in
their name. The Root Group is not allowed to have the string 'Field' in its name.
• Decoration For Group: the Document Model must contain exactly one Root Group with an
empty sub-Group named 'Group' (the Anchor Group). Siblings of this Group must have the
string 'Group' in their name. The Root Group is not allowed to have the string 'Group' in
its name.
NOTE
When clicking Apply or returning from a sub-editor, the Combination is performed
and the Preview is updated. At this time, errors or warnings that stem from the
combination process are shown. This means, that although the sub-model editor
might not show an error, the application of it in a Combination Step might lead to
an error in the Combined Document Model.
2.1.2. Settings
The Settings Screen contains the following elements:
Name
The name of the Combined Document Model. It needs to fulfill certain conventions: Only letters,
digits, hyphens, underscores and periods are allowed. Furthermore, the name of the model must
not start with "xml" and must be at most 100 characters long.
13

-- 13 of 38 --

The model name must be unique within the Workspace and is synchronized with the filename
by the editor.
Model Version
Shows the version of the opened model.
Description
Multiline text to give more information about the Combined Document Model.
Locales
A list of locales supported by the model. Each locale is represented by a row in the table.
At least one locale must be entered. Note that only locale codes according to ISO 639 alpha-2 or
alpha-3 are allowed. It is possible to add a region code after an underline, such as de_DE, de_CH
and so on.
The listed Locales will be the Locales of the Combined Document Model.
Labels
These fields store a list of labels for the model, one for each specified locale.
The labels can be used as a localizable representation of the model itself in a list of different
models. For example, in a model repository. The label will also be used in the variant selection
modal for example, when using the A12 feature "Heterogeneity".
The listed Labels will be the Labels of the Combined Document Model.
Roles
A list of Roles can be maintained in this table. The listed Roles will be the Roles of the Combined
Document Model. Roles are not taken over from the Base or any Additive Document Model.
Annotations
An Annotation is a name-value pair that can be added to the model. The application that uses the
Combined Document Model can access those Annotations and can use them within custom
implementations.
It is possible to add new Annotations, but Annotations that exist after the last Combination Step
cannot be overwritten with a differing value.
NOTE The Annotation "abstract" is not taken over from the Base Model to enable the
modeling of heterogeneous subtypes.
2.1.3. Preview
The Preview screen shows the Document Model that results after applying all Combination Steps to
the Base Model. The elements in the Model Tree can be inspected and Ad-Hoc-Testing is available.
2.1.4. Rule Contradictions
A report regarding unfillable Fields or contradicting Validation Rules can be created in this screen.
It is based on the that is shown in the Preview screen.
14

-- 14 of 38 --

3. Editor for the Additive Document Model
The Additive Document Model Editor needs Context to support the editing. Combination and
Mapping Models provide the Context needed to generate the Reference Document Model.
The Additive Document Model Editor can be opened in one of 3 ways:
1. Clicking the Edit button next to the Additive Document Model dropdown in a Combination
Model.
2. Clicking the Edit button next to the Precomputation Model dropdown in a Mapping Model.
3. Clicking on the Additive Document Model in the Workspace Explorer and selecting the desired
Context.
NOTE The Context only needs to be selected if the Additive Document Model is referenced
more than once in your workspace.
The Additive Document Model will then be shown in the selected context. The Model Tree in the
Additive Document Model Editor will contain both the Reference Document Model and the Additive
Document Model Elements.
3.1. Addition Mechanism
The Addition process starts with the Reference Document Model. Its elements and model settings
are the basis for the resulting Document Model. The Additive Document Model Editor allows:
• New Document Model Elements like Fields, Groups and Rules to be freely added.
• Existing Elements of the Reference Document Model to be overwritten.
The Reference Document Model Structure affects whether Addition overwrites existing elements or
creates new elements. It is therefore possible to model as follows:
1. Create an Additive Document Model
2. Use the Additive Document Model on Reference Document Model A
a. GroupA exists in Reference Document Model A, the Repeatability of the Group is taken from
the Reference Model
3. Use the same Additive Document Model on Reference Document Model B where GroupA is not
modeled.
a. GroupA is added to Reference Document Model B with the Repeatability defined in the
Additive Document Model
CAUTION
If the same Additive Document Model is used in different contexts, then one
should take extra care when changing the Repeatability of Groups in the
Reference Document Model.
15

-- 15 of 38 --

3.1.1. Properties of Overwritten Elements
In order to guarantee that Documents of the Reference Document Model are formally valid also in
the resulting Combined Document Model, it is not possible to change:
• the existence and properties of Groups
• the Repeatability of Groups
• the existence and properties of Fields
• the values of existing Annotations on Groups and Fields
However, it is possible to change:
• Validation Rules
• Computation Rules
• Add Annotations to Fields and Groups
Table 5. Provenance of Properties in the Combined Document Model Element
Element Type Property Taken From
Group Repeatability Reference Model, can not be
overwritten
Group Annotations Joined from Reference and Additive
Model
Group Other Properties Reference Model, can not be
overwritten
Field Data Type Configuration Reference Model, can not be
overwritten
Field Annotations Joined from Reference and Additive
Model
Field Other Properties Reference Model, can not be
overwritten
Validation Rule Rule Condition Additive Model
Validation Rule Rule Error Field Additive Model
Validation Rule Rule Message Additive Model
Validation Rule Rule Condition Additive Model
Validation Rule Annotations Additive Model
Computation Rule Computed Field Additive Model
Computation Rule Computation Alternatives Additive Model
Computation Rule Annotations Additive Model
16

-- 16 of 38 --

3.1.2. Order of Elements
By default, additive elements are added at the end of the parent Group.
If the additive elements need to be within the list of siblings in the resulting model, then add an
existing Reference Model element to the Additive Document Model and drag&drop the additive
elements above it. The overwritten elements serve as an Anchor for other elements.
There is modeling support in the Simple Model Editor to add Groups as Anchor elements to the
Additive Document Model. But due to the different overwriting semantics (see above), no such
support exists for Fields or Rules.
▼ Details about the Ordering Mechanism
The Addition mechanism takes the Reference Model as the basis and adds the additive elements
to it. The mechanism goes through the list of Root-Groups/children of the Group in the Reference
Model and searches for an element with the same name in the Additive Document Model. If an
element is found, all previous additive elements are added in their order directly above the
overwritten element. The mechanism then searches for the next matching elements and adds all
intermittent elements directly above the overwritten element.
The mechanism conserves the order of the elements of the Reference Model. Additive Elements
that were above another additive element in the Additive Document Model, will also be above
this element in the resulting model. The order of elements in the Additive Document Model, that
overwrite an element in the Reference Model will take the order from the Reference Model.
Their order might not be as defined in the Additive Document Model.
Table 6. Example illustrating the Order Mechanism
Additive Document Model Base Model 1 and Addition Base Model 2 and Addition
17

-- 17 of 38 --

3.2. Sidebar
In the sidebar of the Additive Document Model Editor, the name of the Additive Document Model is
displayed at the top. Next to it, the icon for the Context Model is shown. On hover, the name of the
Context Model is shown.
Four menus can be accessed:
• Model Tree
• Settings
• Type Definitions
• Rule Contradictions
Below the menus, the following buttons are available:
• Cancel
• Save
The menus as well as saving a model will be described in the subsequent chapters.
3.3. Model Tree
The Model Tree is the central editor component for the design of Additive Document Models. Here,
the altered or additional elements such as Groups, Fields and Rules can be added, edited, deleted
and viewed.
18

-- 18 of 38 --

Figure 7. Model Tree and Validation Rule Editor
The elements show the following background colors:
• gray: Element of the Reference Document Model (Info on mouse over reads "reference").
• green: Element that exists in the Reference Document Model and the Additive Document Model.
Some of its properties can be overwritten with the Additive Document Model (Info on mouse
over reads "overwritten").
• none: Element that is defined in the Additive Document Model only. (Info on mouse over reads
"additive").
Each element represents a node in the Model Tree. They are all placed below a virtual Model Tree
node. The node actions of the virtual Model Tree node allow you to add and paste elements on root
level of the model. The virtual Model Tree node will not be saved to the model file.
19

-- 19 of 38 --

Figure 8. Virtual Model Tree Node and Actions Available on Model Root Level
3.3.1. Element Editors
To open a node in the tree, click on it using the left mouse button. Then, its corresponding Element
Editor is opened on the right. Documentation for them can be found in the Document Model
documentation.
In the bottom right of each Element Editor, buttons to apply the changes to the element ("APPLY") or
discard the changes to the element ("CANCEL") are displayed.
3.3.2. Actions
Actions in the Model Tree can mainly be found in the context menu of the respective element nodes
which can be accessed via the three dot icon or a right click on each row.
20

-- 20 of 38 --

Figure 9. Context Menu Containing Node Actions for a Group
In the subheader, multi-select actions are available which can be toggled by the multi-select button.
Expand and Collapse
Single nodes such as groups, multi-selects and attachments, can be expanded and collapsed via the
arrow icon left of the element name.
It is possible to expand or collapse all elements of the model via the virtual Model Tree node.
In addition, it is possible to expand or collapse all elements inside a specific node via the node
actions "Expand All" and "Collapse All" in the node’s context menu.
Add an Element
All additions or modifications made in the editor are persisted in the Additive Document Model.
The Reference Document Model is not changed. In order to modify an element of the Reference
Document Model, it must first be added to Additive Document Model. When the Additive Document
Model is joined with the Reference Model, the original element in the Reference Document Model is
overwritten. The Model Tree shows the resulting joined Document Model. This leads to two
different ways of adding elements to the Additive Document Model tree:
1. "Add to ADM" or "Add to ADM with children" in the context menu of Reference Document
Model Elements (see Figure 9).
This copies the respective element and the parent groups to the Additive Document Model. "Add
21

-- 21 of 38 --

to ADM with children" is available for Groups, Includes, Attachments and Multi-Selects and adds
all child elements to the Additive Document Model as well.
All the added elements will overwrite the respective elements in the Reference Document
Model. Some of their attributes can be modified in the Detail Editor.
They are shown with a green background.
2. "Add" or "Add sibling" section of the context menu of Additive Document Model Elements
(including the virtual Model Tree root element; see Figure 8)
This adds a new element to the Additive Document Model, which has no counterpart in the
current Reference Document Model. On root level, only Groups, Attachments and Multi-Selects
can be added. Contrary to regular Document Models, Includes cannot be added on root level. All
other elements can only be children of an existing Group.
To add an element on root level, use the context menu of the virtual Model Tree node. To add an
element as child of an existing Group, use the Group’s context menu. Alternatively, elements
can be added below existing elements. Use the context menu of the target element and choose
the element type inside the "Add sibling" section. If a Validation or Computation Rule is added
below a Field, the Error/Computed Field of the new Rule is prefilled with the target Field.
Copy and Paste
Copying a single node of the Additive Document Model is possible via its context menu. Pasting is
only possible into a Group that is part of the Additive Document Model (either a Group or the
virtual Root Node). There are a number of things to consider when pasting elements:
• Copy & Paste of Group nodes will also copy all the child elements of the Group.
• Copy & Paste of elements inside their original parent Group (or on the top level) will result in
renaming as they cannot have the same names as the original elements. The copied elements
will have a _COPY suffix appended.
• Renaming also applies when elements are copied into a group that already contains elements
with the same names.
• It is possible to copy several elements at once by doing a multi-selection before copying.
• Copy & Paste of single Rules can quickly result in errors. The Fields that the Rule refers to must
be reachable from the position of the copied Rule.
• Copy & Paste of Includes will only copy the Include, to copy the content of an included model
use the "Insert from DM" action.
The Paste action is only active if the target, be it the virtual root or any other node, is a valid target
for the copied content. For example, it is not possible to paste a Field onto the root level.
Cut and Paste
Cut and Paste works in similar fashion as Copy and Paste. When the Cut action is used instead of the
Copy action, the selected element(s) will be moved to the target destination instead of a copy of the
selected element(s) being created. Consequently, this action is only available for elements that do
not occur in the Reference Document Model.
22

-- 22 of 38 --

Insert from DM
With this action, it is possible to insert a copy of all elements of another Document Model as
children to the respective group node or on root level when using the action in the virtual Model
Tree node. The group structure of the original model will be preserved. All includes of the source
Document Model will be resolved when inserting.
Delete
Deleting an element of the Additive Document Model is possible via its context menu. Elements
that have a corresponding element in the Reference Document Model (overwritten elements) have
the action "Remove from ADM" while elements that are only present in the Additive Document
Model have the action "Delete".
On an attempt to delete a Field that is used as an Error Field in one or more Validation Rules, a
confirmation dialog enables choosing between deleting only the field and deleting the field as well
as the Validation Rule(s) for this field.
Similarly, an attempt to delete a Computed Field triggers the display of a confirmation dialog that
enables choosing between deleting only the Field and deleting the Field as well as the Computation
Rule(s) for this Field.
All elements of the Additive Document Model can be removed by using the "Delete all additive
elements" action in the context menu of the virtual root node.
Multi-Selection and Bulk Operations
To toggle the multi-selection mode of the Model Tree, use the toggle button in the subheader.
In the multi-selection mode, an additional column will appear in the Model Tree. It contains
checkboxes to select single nodes as well as a checkbox to select all elements in the column header.
Selecting an element that contains child elements such as groups always selects all children as well.
It is possible to de-select single children of a selected parent node. Then, the state of the checkbox
will switch from "selected" (checkmark) to "indeterminate" (square).
As soon as at least one node has been selected in the Model Tree, the bulk operations become
active, while all non-bulk operations become inactive. The only exception to this is the paste action
on root level as well as on node level.
Depending on the selected elements (whether they are elements in the Reference Document
Model, the Additive Document Model or both), the following bulk operations are available: Copy,
Cut, Delete, and Ad Hoc Testing.
Using the toggle button again will hide the multi-selection column. If nodes are selected, a
confirmation modal will appear, since hiding the multi-selection column will remove the multi-
selection as well.
Move
Elements like Fields and Groups can be moved from one Group to another by dragging selected
23

-- 23 of 38 --

elements and dropping them on the target group. The selected elements must be elements of the
Additive Document Model. The target must be a Group or the virtual root node of the Additive
Document Model or a Group that was added from the Reference Document Model. Elements that
only exist in the Reference Document Model can neither be moved nor be targets.
Multiple elements can be moved if they are selected via the multi-selection.
When moving a Field that is used in at least one Validation or Computation Rule, the Simple Model
Editor displays a dialog with the option to perform a refactoring operation that renames the Field
references in these Rules. Similarly, when moving a Rule, a dialog offers the option to adapt the
paths of Field references in this Rule according to its new location.
3.3.3. Additive Elements Only
In the header of the Model Tree, the switch "Additive Elements Only" is present. If it is activated,
only the elements of the Additive Document Model are shown. This could be elements that are
uniquely added to the Additive Document Model or added from the Reference Document Model.
3.3.4. Search and Filter
In the header of the Model Tree, it is possible to filter the Model Tree by element types and do a
search on element names.
Initially, all elements are displayed. De-select element types to filter the Model Tree. An indicator on
the filter icon will show if the view is currently filtered.
To search for elements by their name, use the search field. Only elements which (partially) match
the input will be displayed. To reset the view, clear the search field.
The reset button next to the filter button will reset the view by removing any filter for elements and
any search.
3.4. Settings
In the Settings menu, model-wide settings can be made.
24

-- 24 of 38 --

Figure 10. Model Settings Screen
Name
The name of the Additive Document Model. The name needs to fulfill certain conventions: Only
letters, digits, hyphens, underscores and periods are allowed. Furthermore, the name must not
start with "xml" and must be at most 100 characters long.
The model name must be unique within the Workspace and is synchronized with the filename
by the editor.
CAUTION
The ids of the model elements in the resulting Combined Document Model
are dependent on the name of the Additive Document Model. Renaming the
Additive Document Model thus changes the ids of the added elements. This
can break consecutive models or custom code, for example Form and
Overview Models.
Model Version
Shows the version of the opened model.
Description
Multiline text to give more information about the Additive Document Model.
Reference Model
Description of how the current Reference Document Model was built. In the context of a
25

-- 25 of 38 --

Combination Model, it reads "<Combination Model Name> Combination Up To Step <n>". In the
context of a Mapping Model, it reads "Joined Target and Source Models of <Mapping Model
Name>".
Locales
This field stores a list of locales for the Additive Document Model. Each locale is represented by
a row in the table. At least one locale must be entered. All locales of the Reference Document
Model must be present.
Note that only locale codes according to ISO 639 alpha-2 or alpha-3 are allowed. It is possible to
add a region code after an underscore, such as de_DE, de_CH and so on.
The editor will show a separate input field for every given country code where multilingual
inputs are possible, for example, for error messages, labels, and descriptions.
If a locale is deleted from this list, a warning will be shown mentioning that all texts that have
been set up for that locale will also be deleted.
For more information about locales see the Languages section in the Document Model
documentation.
Annotations
An Annotation is a name-value pair that can be added to the model in the model settings and all
model elements. The application that uses the Additive Document Model can access those
Annotations and can use them within custom implementations.
The editor shows the list of the Annotations of the Reference Model on the top and the list of the
to-be-added Annotations below.
Document Uniqueness Criteria
You can add Document Uniqueness Criteria in the Additive Document Model. For more
information about their modeling see the Document Uniqueness Criteria section in the
Document Model documentation.
All Criteria defined here will be added to the existing ones in the Reference Model. There is no
overwriting mechanism, so if a Document Uniqueness Criterion is specified again with the same
name, an error will be shown.
It is possible to reference Fields from both the Reference and the Additive Document Model.
3.5. Type Definitions
In the Type Definitions menu, Type Definitions can be viewed, added, edited and deleted. The Type
Definitions are either defined in the Additive Document Model or introduced via an include,
import or the Reference Model.
Locally defined Type Definitions can be edited and deleted. The others have the action to "Add to
ADM" (and then "Remove from ADM") in their context menu. This actions will add or remove the
Type Definition also to the Additive Document Model. This modeling support is helpful when the
26

-- 26 of 38 --

same Additive Document Model shall be used for many different Reference Models in which not all
have the respective Type Definition. But generally, this is not needed and should be avoided.
For information on what Type Definitions are and how they work, refer to the Type Definitions
section in the Document Model documentation.
3.6. Rule Contradictions
While each Rule and Computation is checked for validity during editing it is possible that multiple
Rules or Formal Validation settings contradict each other. The Rule Contradiction Report takes not
just the Additive Document Model but the Joined Model into account.
For more information, refer to the Rule Contradiction section in the Document Model
documentation.
3.7. Refactoring Across Models
Currently, no refactoring support is provided for Additive Document Models. This means, that
changes in the Reference Document model might break the Additive Document Model, or changes
to the Additive Document Model might break subsequent models (Form or Overview Models,
Mapping Models or subsequent Combination Steps).
4. Editor for the Selection Model
The Selection Model Editor needs Context to support the editing. Combination Models provide the
Context needed to generate the Reference Document Model.
The Selection Model Editor can be opened in one of 2 ways:
1. Clicking the Edit button next to the Selection Model dropdown in a Combination Model.
2. Clicking on the Selection Model in the Workspace Explorer and selecting the desired Context.
NOTE The Context only needs to be selected if the Selection Model is referenced more than
once in your workspace.
The Selection Model will then be shown in the selected context.
4.1. Selection Mechanism
The Selection Model specifies, which elements of the Reference Model are taken into account for
Decoration (if the Selection Model is referenced in a Decoration step) or for subsequent modeling (if
the Selection Model is referenced in a Selection step). Elements, that are not selected, are not
decorated or will not be present in the resulting Combined Document Model, respectively.
The specification of which elements are selected can be done and reviewed in detail in the Selection
Specification screen.
27

-- 27 of 38 --

But for most use-cases, the Selected Elements screen is sufficient and gives an easier entry point to
Selection Modeling. However, this screen is just an easier view onto the result; the persisted
information is that shown in Selection Specification.
If a Group is empty after the Selection process, then it will be removed in the resulting model. If
elements of an Include are selected out, then the Include is converted into a regular Group in the
resulting model.
If a Field is unselected but was the Index Field of a Group, then this property will be removed from
the Group. Other adaptations are not done. Rules are not adapted or automatically removed if their
Computed or Error Field is removed.
Note that the Selection Model can specify (even with a full absolute path) elements that are not
present in the current Reference Model. A warning will be shown for those elements in the
Combination Model editor.
CAUTION
In this version of A12 it is possible to select out Fields and Validation Rules from
Attachments, breaking their usability. This is considered a bug and will be fixed
in an upcoming A12 release.
4.2. Sidebar
In the sidebar of the Selection Model Editor, the name of the Selection Model is displayed at the top.
Next to it, the icon for the Context Model is shown. On hover, the name of the Context Model is
shown.
Three menus can be accessed:
• Selected Elements
• Selection Specification
• Settings
Below the menus, the following buttons are available:
• Cancel
• Save As
• Save
The menus as well as saving a model will be described in the subsequent chapters.
4.3. Selected Elements
28

-- 28 of 38 --

Figure 11. Model Tree Showing the Selection State of the Reference Model Elements
This screens shows the Model Tree of the Reference Model. The status of the checkboxes of the
Fields, Validation or Computation Rules indicates, whether the element will be part of the resulting
Combined Document Model or not.
Checking a checkbox adds the element to the Selected list of the respective element type. If the
element’s full path was specified in the Unselected list, then this specification will be removed.
Unchecking a checkbox adds the element to the Unselected list of the respective element type. If the
element’s full path was specified in the Selected list, then this specification will be removed.
Be aware that there is no clean-up mechanism for wildcards or Path Specifications that identify
Groups. It is recommended to start modeling in this screen and adjust the Path Specifications if
needed afterward in the Selection Specification screen.
This screen serves as a Preview as it shows all elements that will be in the resulting Combined
Document Model with a checked checkbox. Moreover, the switch "Selected Elements Only" can be
used, to filter the model tree to elements that are selected only. The checked state might stem from
the Default, the use of a wildcard, the specification of a full path or from checking the respective
box in this screen.
CAUTION
Although checkboxes are used to represent the Selection state of elements, they
do not behave like the selection for Bulk Operations. [Shift] or [Control] based
interaction patterns are not supported and might lead to unintended Selection
Specifications.
29

-- 29 of 38 --

NOTE
It is not allowed to specify the Default "Selected" and then giving only Specifications
for Selected Elements without also giving Specifications for Unselected Elements –
as this does not make sense. The same applies vice versa if the Default is
"Unselected".
If the modeler clicks a checkbox of a Group and the editor would produce such a
state, a warning is shown and no entry for Specification for Selected/Unselected
Elements is added. The result is likely correct (as the Default takes over), the
warning is merely produced to make the modeler aware.
4.3.1. Search and Filter
In the header of the Model Tree, it is possible to filter the Model Tree by element types and do a
search on element names.
Initially, all elements are displayed. De-select element types to filter the Model Tree. An indicator on
the filter icon will show if the view is currently filtered.
To search for elements by their name, use the search field. Only elements which (partially) match
the input will be displayed. To reset the view, clear the search field.
The reset button next to the filter button will reset the view by removing any filter for elements and
any search.
4.4. Selection Specification
30

-- 30 of 38 --

Figure 12. Selection Specifications Screen with detailed Selection Specifications
For each element category, Fields, Validation Rules and Computation Rules, it can be specified if
those elements should all be Selected or Unselected per Default. Next, Path Specifications can be
given to Select or Unselect elements. The Path Specifications can contain wildcards:
1. for the Group the element resides in
For example "/*/MyElement" will find all elements with name MyElement in any Group.
To state only "/*/" is not allowed, since this is the purpose of the Default.
It is not supported yet to give partial Group paths like "/Customer/*/FirstName".
2. for the prefix of the name
For example "/*/*Date" would match all elements with names ending on "Date", like
"/Person/BirthDate" and "/Person/Addresses/MoveInDate".
3. for the suffix of the name
For example "/*/Date*" would match all elements with names beginning with "Date", like
"/Person/DateOfBirth" and "/Person/Addresses/DateOfRegistration".
NOTE
No wildcard is needed to specify all elements of a Group. Simply state the Group’s
name followed by a "/": "/Person/Addresses/" is the correct way to state that all
elements (of the respective category) of the Group Addresses shall be selected. The
specification "/Person/Addresses/*" is invalid.
The corresponding Group structure is automatically added to the result. However, if a Group is
empty after the Selection process is finished, then it is removed. If elements of an Include are
selected out, then the Include is converted into a regular Group in the resulting model. If a Field is
31

-- 31 of 38 --

unselected but was the Index Field of a Group, then this property will be removed from the Group.
4.5. Selection Mechanism
The Selection process consists of the following Selection Steps that are done for each category
(Fields, Validation Rules and Computation Rules):
• if default = Selected:
1. all elements of the respective category are added to the result (matching specificity 0)
2. all elements of the respective category that match a Path Specification for Unselected
Entities are removed from the result
3. all elements of the respective category that match a Path Specification for Selected Entities
are added again to the result
• if default = Unselected:
1. all elements of the respective category that match a Path Specification for Selected Entities
are added the result
2. all elements of the respective category that match a Path Specification for Unselected
Entities are removed again from the result
An element matches a Path Specification if one of the following conditions is met. The number
indicates the matching specificity (see below). The conditions are listed from low to high specificity:
1. the path of an ancestral Group is identical
"/RG/G1/" (with trailing /) matches e.g. elements with path "/RG/G1/F3", "/RG/G1/F33", "/RG/G1/R3",
"/RG/G1/G2/F3" or "/RG/G1/G2/G4/F3" (all subelements of /RG/G1)
2. the path of the parent Group is identical
"/RG/G1/" (with trailing /) matches e.g. elements with path "/RG/G1/F3", "/RG/G1/F33", "/RG/G1/R3",
but not "/RG/G1/G2/F3" or "/RG/G1/G2/G4/F3" (all children of /RG/G1)
3. the beginning of the element’s name is identical
"/*/F*" matches e.g. elements with path "/RG/G1/G2/F3", "/RG/G1/F33", "/RG/G1/F3" or
"/RG/G1/G2/G4/F3" but not "/RG/G1/R3"
4. the beginning of the given group’s child’s name is identical
"/RG/G1/F*" matches e.g. elements with path "/RG/G1/F3", "/RG/G1/F33" but not "/RG/G1/R3",
"/RG/G1/G2/F3" or "/RG/G1/G2/G4/F3"
5. the end of the element’s name is identical
"/*/F*" matches e.g. elements with path "/RG/G1/G2/F3", "/RG/G1/F33", "/RG/G1/R3", "/RG/G1/F3" or
"/RG/G1/G2/G4/F3"
6. the end of the given group’s child’s name is identical
"/RG/G1/\*3" matches e.g. elements with path "/RG/G1/F3", "/RG/G1/F33" or "/RG/G1/R3", but not
"/RG/G1/G2/F3" or "/RG/G1/G2/G4/F3"
7. the elements name is identical
"/*/F3" matches e.g. elements with path "/RG/G1/G2/F3", "/RG/G1/F3", "/RG/G1/G2/G4/F3"
8. the path is identical
32

-- 32 of 38 --

"/RG/G1/G2/F3" matches only an element with path "/RG/G1/G2/F3"
However, the match is neglected, if the matching specificity is lower than the specificity of the
previous selection step. This means, that a concrete Path Specification in the previous Selection Step
beats a fuzzy Path Specification in the next Selection Step. Examples can be seen in the Table below
Table 7. Example Selection Specification Illustrating the Matching Specificity
Default Path Specification for
Selected Entities
Path Specification for
Unselected Entities
Result
Unselected "/RG/G1/F3" (specificity 8) "/RG/G1/" (specificity 1) "/RG/G1/F3" is present
Unselected "/RG/G1/" (specificity 1) "/RG/G1/F3" (specificity 8) "/RG/G1/F3" is missing
Unselected "/RG/G1/*3" (specificity 6) "/RG/G1/F*" (specificity 4) "/RG/G1/F3" is present
Unselected "/RG/G1/F*" (specificity 4) "/RG/G1/*3" (specificity 6) "/RG/G1/F3" is missing
Selected
(specificity
0)
"/RG/G1/*3" (specificity 6) "/RG/G1/F*" (specificity 4) "/RG/G1/F3" is present
Selected
(specificity
0)
"/RG/G1/F*" (specificity 4) "/RG/G1/*3" (specificity 6) "/RG/G1/F3" is missing
In Selection Models that are used for Decorations For Groups, the element specification work also
for Groups; in other selections this would result in an empty Group that will be removed.
4.6. Settings
In the Settings menu, model-wide settings can be made.
33

-- 33 of 38 --

Figure 13. Model Settings Screen
Name
The name of the Selection Model. The name needs to fulfill certain conventions: Only letters,
digits, hyphens, underscores and periods are allowed. Furthermore, the name must not start
with "xml" and must be at most 100 characters long.
The model name must be unique within the Workspace and is synchronized with the filename
by the editor.
Model Version
Shows the version of the opened model.
Description
Multiline text to give more information about the Selection Model.
Reference Model
Description of how the current Reference Document Model was built. In the context of a
Combination Model, it reads "<Combination Model Name> Combination Up To Step <n>".
Annotations
An annotation is a name-value pair that can be added to the model.
34

-- 34 of 38 --

4.7. Refactoring Across Models
Currently, no refactoring support is provided for Selection Models. This means, that changes in the
Reference Document Model are not reflected in the Selection Specifications.
5. Decoration Mechanism
With the Decoration mechanism, a fixed set of Groups, Fields, Computation and Validation Rules is
added to a Reference Model. The Decoration Model, which contains the to-be-added elements, acts
like a stamp, that imprints the same structure multiple times onto the Reference Model.
Combination Steps of Type Decoration must specify a Selection Model and a Document Model for
Decoration. The Decoration Model defines which elements are to be added, the Selection Model
specifies around which Reference Model elements those should be added.
Note that if elements are added to a Group that was an Include in the Reference Model, then this
connection will be removed and turned into a regular Group.
It is possible to decorate Fields or Groups.
5.1. Decoration For Fields
Combination Steps of Type 'Decoration For Fields' allow you to add the elements of the Document
Model for Decoration systematically around the Fields, that are specified by the Selection Model.
NOTE
Only Fields, that are not marked as Transient are taken into account.
The Document Model for Decoration must contain exactly one Root Group with a Field named
'Field'. This Field is called the Anchor Field. Siblings of this Field must have the string 'Field' in their
name. All other elements, including the Root Group, of the Document Model can have custom
names. The Root Group must not contain the string 'Field'.
An example for a suitable Document Model can be seen in the following figure.
Figure 14. Document Model For Decoration For Fields
35

-- 35 of 38 --

For each non-transient Field selected by the Selection Model, the following steps are done:
1. The names of the elements of the Document Model for Decoration are adopted according to the
name of the selected Field.
If the Field 'Name' is to be decorated, then the Rule in Figure 14 would be called
'NameNotProofedWarning'.
If this new name duplicates the name of an existing sibling, an error is shown in the
Combination Model editor.
2. The paths are adopted according to this new name in Validation and Computation Rules (Error
Field, Error Messages, Error Condition, Computed Field, Preconditions, Calculations). The path
must be relative.
3. If any Field of the Document Model for Decoration has the Annotation
_take_reference_configuration, then the Data Type and the Type Configuration is copied from
the selected Reference Model Field to this Field. Other properties like Labels, Annotations or
Descriptions are not transferred.
This can be used to formulate Type-dependent Rules. An error is shown in the Combination
Model editor, if a Field is decorated with a Rule, that does not match the Fields Data Type.
After this preparation step, the elements are added to the Reference Model around the selected
Field. The internal structure of the Root Group of the Document Model for Decoration is conserved
regarding the Anchor Field: Any element, that was a sibling above the Anchor Field, will be a
sibling above the selected Field.
Annotations of the Anchor Field will be copied to the selected Field. If both have an Annotation
with the same name, those must have the same value. Otherwise, an error is shown.
5.2. Decoration For Groups
Combination Steps of Type 'Decoration For Groups' allow you to add the elements of the Document
Model for Decoration systematically around the Groups, that are specified by the Selection Model.
NOTE
In the Selection Model the Path Specification "/RG/Group1/" is synonym to
"/RG/Group1/*" and means "decorate all subgroups of /RG/Group1". Path
Specification "/RG/Group1" (without the trailing /) means "decorate group
/RG/Group1". Empty Groups can be selected and decorated.
Modelers are advised to select the to-be-decorated Groups first in the Selected
Elements screen and then adopt the Group Paths if needed in the Selection
Specification screen.
The Document Model for Decoration must contain exactly one Root Group with a Group named
'Group'. This Group is called the Anchor Group. It must be empty. Siblings of this Group must have
the string 'Group' in their name. All other elements, including the Root Group, of the Document
Model can have custom names. The Root Group must not contain the string 'Group'.
36

-- 36 of 38 --

An example for a suitable Document Model can be seen in the following figure.
Figure 15. Document Model For Decoration For Groups
For each Group selected by the Selection Model, the following steps are done:
1. The names of the elements of the Document Model for Decoration are adopted according to the
name of the selected Group.
If the Group 'PersonInformation' is to be decorated, then the first Field in Figure 15 would be
called 'UIHelper_PersonInformationIsGDPRProtected'.
If this new name duplicates the name of an existing sibling, an error is shown in the
Combination Model editor.
2. The paths are adopted according to this new name in Validation and Computation Rules (Error
Field, Error Messages, Error Condition, Computed Field, Preconditions, Calculations). The path
must be relative.
After this preparation step, the elements are added to the Reference Model around the selected
Group. The internal structure of the Root Group of the Document Model for Decoration is conserved
regarding the Anchor Group: Any element, that was a sibling above the Anchor Group, will be a
sibling above the selected Group.
Annotations of the Anchor Group will be copied to the selected Group. If both have an Annotation
with the same name, those must have the same value. Otherwise, an error is shown.
5.3. Validation
Note that the structure and the naming rules are not validated in the Document Model for
Decoration itself, but only once it is referenced in a Combination Model. Any modeling
inconsistencies, like naming collisions, mismatching Data Types or duplicated Annotation names,
that arise from this usage, will be shown in the Combination Model editor.
37

-- 37 of 38 --

6. Glossary
Term Description
Anchor Element • In Additive Document Models, Anchor elements are used to order the
elements of the resulting Document Model. The Anchor Element is an
overwritten element of the Reference Model. All siblings of the
Anchor Element, that are above it in the Additive Document Model,
will also be above it in the resulting Document Model.
• In Document Models that are used for Decorations, the Anchor
Element has name 'Field' (in case of Decoration for Fields) or 'Group'
(in case of Decoration for Groups). The Anchor element is the
placeholder for the to-be-decorated element of the Reference Model.
Base Model The starting point of a Combined Document Model.
Combination The process of applying the Combination Steps to the Base Model. The
result is the Combined Document Model.
Combination Step A step in the Combination process can be of one of the following types
• Addition (see Addition Mechanism)
• Selection (see Selection Mechanism)
• Decoration for Fields (see Decoration Mechanism)
• Decoration for Groups (see Decoration Mechanism)
The first step is applied to the Base Model, the second step to the result of
the first step and so on. The result of this process is called Combined
Document Model.
Reference Model The model that is shown read-only in the Additive Document Model
editor or the Selection Model editor, to facilitate the modeling. Depending
on the Context in which the respective model was opened, it is build by
applying the previous Combination Steps to the Base Model (context of
Combination Model) or by joining the Target and Source Models (context
of Mapping Model).
38

-- 38 of 38 --

