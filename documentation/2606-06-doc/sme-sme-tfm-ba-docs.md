# sme sme tfm ba docs

Transformer Modeling
CAUTION
The Transformer Model and the editor are an experimental feature. It must be
enabled in the Tool Settings of the Simple Model Editor before use; see Enabling
Model Types.
1. Introduction and Concepts
The Transformer Model Editor is part of the Simple Model Editor and enables domain experts,
analysts and developers to create and modify Transformer Models for business applications.
The Transformer Model determines how an XML Schema Definition (XSD) file is translated into an
A12 Document Model. The resulting Transformed Document Model can be used like any other
Document Model in the Simple Model Editor: to build a Form or Overview Model, to define a
Mapping Model or to be used as Base Model in a Combined Document Model. In the Simple Model
Editor and this documentation, the Transformer Model and the resulting Transformed Document
Model are used synonymously. For Modelers, these are the two sides of the same coin.
The A12 Transformer and its corresponding model is used to bring data from an external source
into an A12 application or send it back. Currently, the Transformer supports XML as interchange
format. In the full data chain, one can distinguish three different transformation processes (and
three different Transformer components):
1. Transforming the XML Schema Definition (XSD) into an A12 Document Model (at modeling
time)
Table of Contents
1. Introduction and Concepts. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 1
1.1. Example Use Case . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
2. Editor Functions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
2.1. Open the Transformed Document Model Editor. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
2.2. Transformer Model Contents. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
2.2.1. Transformation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
2.2.2. Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
2.2.3. Element Selection . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Rename Paths. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Delete Paths . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
2.2.4. Custom Texts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
String Pattern Error Messages . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
Enumeration Display Texts. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
2.2.5. Preview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
3. Glossary . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
1

-- 1 of 14 --

Both, XSDs and A12 Document Models describe how a valid dataset is structured. They
determine which values are allowed for which properties (XML elements or attributes, and A12
Fields, respectively) and if they are mandatory.
The XSD>DocumentModel-Transformer takes an XSD file and a Transformer Model as input and
creates an A12 Document Model for it. This Transformed Document Model can then be used to
represent the external data source in the A12 Application. It contains the necessary information
for the runtime Transformers in the form of Annotations. Those Annotations are needed and
should not be changed or deleted. Because A12 comes with preconfigured Data Types, it is
possible to specify which XSD Data Type ("restriction") shall be represented by which A12 Data
Type in the resulting Transformed Document Model.
The following questions are answered by the Transformer Model:
◦ Which XSD elements are transformed into Document Model Groups and Fields?
◦ How are XSD restrictions transformed into A12 Data Types?
◦ Which localized Display Texts should be available for end users for String Patterns and
Enumeration Values?
2. Transforming an XML file into an A12 Document (at runtime)
The XML file (or object at runtime) is an instance of the schema defined in the XSD, just like the
A12 Document is an instance of the A12 Document Model.
The XML>Document Transformer takes an XML file and a Transformed Document Model as
input and creates an A12 Document for it.
It uses the Annotations in the Transformed Document Model to find the data in the XML file that
is to be transformed into the A12 Document.
3. Transforming an A12 Document into an XML file (at runtime)
The Document>XML-Transformer takes an A12 Document and a Transformed Document Model
as input and creates an XML file for it.
It uses the Annotations in the Transformed Document Model to place the data of the A12
Document into the correct position in the XML file.
CAUTION The runtime Transformers need the Annotations written into the Transformed
Document Model. Those Annotations should not be changed.
NOTE
The purpose of the Transformer is to connect an A12 Application to an external data
interface. The Transformed Document Model serves as a representation of this
external interface inside the A12 modeling ecosystem. The external interface has
priority and determines the A12 representation. Because of this clear directionality,
the Transformed Document Model is "volatile" and not persisted as such. It can not
be altered directly but only by means of the Transformer Model. This approach
avoids inconsistencies between the XSD and the Document Model.
2

-- 2 of 14 --

Because of this directionality there is no DocumentModel>XSD-Transformer.
NOTE
It is possible to test the Document>XML and XML>Document-Transformation by
using Workspace Data in the Simple Model Editor. See the Workspace Data >
Document > Setting Documentation for more information.
1.1. Example Use Case
In an HR Management System, a business requirement is to automatically retrieve and link
enrollment information to Employees. The Employees are managed in the A12 application. The
enrollment data is provided from an external source in form of XML data. For example, as an
Attachment-upload by the Applicant during the recruitment phase.
Taking the advanced example workspace as a basis, the formalized business requirement would be:
Within the A12 modeling ecosystem, there is a representation of the enrollment information, so
that a modeler can use this information to create a Relationship between Enrollment Certificates
and Employees. The enrollment information is read by the running application, transformed into
an A12 Document and linked to the Employee. A table shows all the linked enrollment information
on the Employee form.
The XML Schema Definition and the corresponding Document Model are shown side-by-side below.
Figure 1. XML Schema Definition of Enrollment
Certificate
Figure 2. Corresponding Document Model
One can see clear parallels between the XML Schema Definition and the Document Model:
Property of the XML Schema Definition Property of the Document Model
The Root Element of the XML is named
"EnrollmentCertificate"
The Root Group of the Document Model is
named "EnrollmentCertificate"
3

-- 3 of 14 --

Property of the XML Schema Definition Property of the Document Model
The Root Element is of a Complex Type.
It has the following attributes: IdNr,
CreationDate, TermIssued
It has the following nested elements: University,
Fee, Enrollment, CurrentProgramProgress
Within the Root Group there are Fields and
nested Groups.
It has the following Fields: IdNr, CreationDate,
TermIssued
It has the following nested Groups: University,
Fee, Enrollment, CurrentProgramProgress
The Enrollment element has the property
maxOccurs="10".
The Enrollment Group has the Repeatability set
to 10.
The Enrollment element has the property
minOccurs="1".
A helper Field
'errorFieldMinOccurs_Fee_EnrollmentCertificate
_RootGroup_MinOccurs' is created as a sibling of
the Enrollment Group. It is the Error Field for a
Validation Rule with the following Error
Condition:
(FieldFilled(errorFieldMinOccurs_Fee) OR
FieldNotFilled(errorFieldMinOccurs_Fee)) AND
NumberOfFilledGroups(Fee*) < 1
2. Editor Functions
2.1. Open the Transformed Document Model Editor
To access the Transformed Document Model Editor, open a respective model in the Workspace
Explorer; they are marked by the icon "Tf".
4

-- 4 of 14 --

Figure 3. Workspace Explorer containing a Transformed Document Model and the respective XSD file as
Resource, among others
5

-- 5 of 14 --

To open the Transformed Document Model Editor for an existing model, click on the entry in the
Workspace Explorer.
To create a new Transformed Document Model, use the "ADD" button in the header of the
Workspace Explorer or in the context menu of a folder and select "Transformed Document Model".
Figure 4. Add a New Transformed Document Model
A modal will then be displayed to define the most important model settings: Folder and Name.
It is also possible to specify the XSD file from the Workspace Resources and Locales as well as Roles.
6

-- 6 of 14 --

Figure 5. Enter Initial Model Settings for New Transformed Document Model
The Name of the model must be unique in the Workspace.
The Name, the Locales and the Roles will be the respective properties of the Transformed
Document Model.
2.2. Transformer Model Contents
The Transformed Document Model Editor can be seen in Figure 6.
7

-- 7 of 14 --

Figure 6. Transformed Document Model Editor - Transformation Screen
2.2.1. Transformation
The Transformation Screen contains the following elements:
Main XSD File
Select an XML Schema Definition (XSD) file from the dropdown list. The list contains all XSD files
present in the Workspace Resources.
This XSD file will be used to create the Transformed Document Model according to the settings
specified in this editor. The XSD file may contain references to other files. Those must be present
in the Workspace Resources according to the relative paths used in the selected XSD file.
CAUTION
Please be aware, that there is currently no refactoring or automatic detection
of name changes of the selected XSD file or any therein referenced file. For
your convenience, the Simple Model Editor offers the Rename action on all
Workspace Explorer elements. It is recommended to reload the Workspace
after changing the name of XSD or therein referenced files.
Root Element
Select the name of the XSD element that will be used as root group in the A12 Document Model.
The first element with a matching name in the resolved XSD will be used, if multiple elements
have the same name.
CAUTION The element must be non-repeatable (maxOccurs = 1).
8

-- 8 of 14 --

Type Mappings
A list of the selected XSD-SimpleTypes and their corresponding A12 Data Type with the Data
Type Configuration.
XSD Type Name
Select the name of a SimpleType available in the XSD.
Type
Select the corresponding A12 Data Type. For each Data Type, the specific configurations that
are supported by the Transformer are available. For more information regarding the
individual settings, please refer to the Document Model documentation.
In general, the Transformer selects the matching A12 Type based on the XSD type definitions. Use
the Preview of the Transformed Document Model to inspect the final Data Type of the Fields. It is
recommended to only specify a different Data Type here if needed.
2.2.2. Settings
The Settings Screen contains the following elements:
Name
The name of the Transformed Document Model. It needs to fulfill certain conventions: Only
letters, digits, hyphens, underscores and periods are allowed. Furthermore, the name of the
model must not start with "xml" and must be at most 100 characters long.
The model name must be unique within the Workspace and is synchronized with the filename
by the editor.
Model Version
Shows the version of the opened model.
Description
Multiline text to give more information about the Transformed Document Model.
Locales
A list of locales supported by the model. Each locale is represented by a row in the table.
At least one locale must be entered. Please note that only locale codes according to ISO 639
alpha-2 or alpha-3 are allowed. It is possible to add a region code after an underscore, such as
de_DE, de_CH and so on.
Labels
These fields store a list of labels for the Transformed Document Model, one for each specified
Locale.
They can be used as a localizable representation of the model itself in a list of different models. For
example, in a model repository. The label of the Transformed Document Model will also be used in
the variant selection modal for example when using the A12 feature "Heterogeneity".
9

-- 9 of 14 --

Roles
A list of Roles can be maintained in this table. The listed Roles will be the Roles of the Combined
Document Model.
Annotations
An Annotation is a name-value pair that can be added to the model. The application that uses the
Transformed Document Model can access those Annotations and can use them within custom
implementation.
2.2.3. Element Selection
Figure 7. Transformed Document Model Editor - Element Selection Screen
The Element Selection Screen, as shown in Figure 7, contains the following elements:
Rename Paths
It is possible to change the name of XSD elements during the transformation. The connection to the
original element will be persisted as Annotation '_XM_ORIGINAL_ELEMENT_NAME' on the
respective element in the Transformed Document Model.
Original Path
Full path of the element starting from the specified Root Element
This can be used to change the name of XSD elements that do not conform to the pattern for A12
element names.
New Element Name
The new name for the element.
10

-- 10 of 14 --

CAUTION
In further modeling steps, like 'Custom Texts', the element must be referenced
by its new name. Changing the name here might break the specifications of the
further modeling steps.
Delete Paths
It is possible to neglect XSD elements during the transformation.
Path in Model
Full path of the element which should be neglected. Path must start with the specified Root
Element. Renamings are not taken into account.
2.2.4. Custom Texts
Figure 8. Transformed Document Model Editor - Custom Texts Screen
The Custom Texts Screen, as shown in Figure 8, contains the following elements:
String Pattern Error Messages
The list defined in this screen specifies Error Messages in a general manner for all String Fields
with the respective pattern.
The following properties are available for each entry in the detail screen:
Pattern
Select a Pattern that is defined as a restriction in the XSD.
Action
Select one of the following actions:
11

-- 11 of 14 --

• Update Message: Overwrite the default error message with a custom error message per
Locale.
• Replace: Define a different Pattern. This can be used, if the original pattern contains
elements that are not supported by Kernel.
• Suppress: Ignore the Pattern. The XSD elements referring to this pattern will be transformed
to String Fields without a pattern.
Replacement
Mandatory Field for Action 'Replace'. Specify the valid A12 Pattern that should be used instead of
the XSD Pattern. (See Kernel Language Documentation for more information.)
Error Messages
Can be given for Action 'Replace' and must be given for Action 'Update Message'.
NOTE It is possible to use Placeholders '$field$' and '$field.value$' in the Error Text.
CAUTION A Text must be given for each Locale. It is not possible to specify only Texts
for one Locale.
Enumeration Display Texts
The list defined in this screen specifies Display Texts for Enumeration values in a general manner
for all Enumeration Fields.
For each created entry the following settings can be specified in the detail screen:
Value
The Enumeration Value specified in the XSD for which the Display Texts should be applied.
Only For Field
Select the Enumeration Field in which the given value should have the listed Display Texts.
If no Field is given, the Display Texts are used for all Fields in which the Enumeration Value
occurs.
Text
The Display Text that is shown to the end user in the respective Locale.
An error will be shown in the Transformed Document Model Preview, if the Display Texts are
not unique within the set of values of an Enumeration Field or Type Definition.
NOTE
The replacements are processed from top to bottom. It is possible to specify a set
of Display Texts only for a specific Field/Type Definition first and a set of Display
Texts without a restriction for all (remaining) Fields afterward.
CAUTION A Display Text for each Locale must be given. It is not possible to specify only
the Display Text for one Locale.
12

-- 12 of 14 --

2.2.5. Preview
Figure 9. Transformed Document Model Editor - Preview Screen
The Preview screen, as shown in Figure 9, displays the Transformed Document Model. It is the
result of the transformation according to the specifications of the previous screens. This model will
be used as basis for subsequent modeling, for example for Form or Overview Models.
General features like Search and Filter by Document Model element type are available. All model
elements can be inspected in detail.
The Ad-Hoc-Testing feature can be used to test the generated Validation Rules.
NOTE The preview of the Transformed Document Model is read-only. In order to change
elements, the other editor screens must be used.
NOTE
It is possible to test the XML>Document- and Document>XML-Transformation by
using Workspace Data in the Simple Model Editor. See the Workspace Data >
Document > Settings for more information.
13

-- 13 of 14 --

3. Glossary
Term Description
XML XML is a format to exchange structured data. Like the A12 Document
Model describes - and restricts - the data present in an A12 Document,
XML Schema Definitions describe and restrict the data present in
corresponding XML files.
XSD XML Schema Definition (XSD) is a format to define the structure of a
valid XML. XSD and XML relate like A12 Document Model and A12
Document. The XSD file is the basis for modeling in the Transformer
Model editor.
Transformer Model The Transformer Model describes how an XML Schema Definition (XSD)
is translated into an A12 Document Model. The resulting Document
Model is called Transformed Document Model.
Transformer The following three Transformers are available in A12:
1. XSD to Document Model Transformer at modeling time (using a
Transformer Model)
2. XML to A12 Document Transformer at runtime (using a Transformed
Document Model)
3. A12 Document to XML Transformer at runtime (using a Transformed
Document Model)
XML Element XML structures data into elements and attributes. While attributes have
the granularity of an A12 Field, elements can either nest other elements
or contain simple data. Depending on whether subelements are present,
the Transformer translates the element into an A12 Field or a Group.
SimpleType XSDs can contain information about which data is allowed to be present
in an element or attribute. Those sets of Restrictions can be specified and
named in the XSD. The named simpleTypes can be used to systematically
translate them into A12 Data Types.
Restriction A Restriction is a definition of what data is allowed to be present in an
element or attribute. This could be the length of a string, the maximum of
a number, a RegEx Pattern for a string and so on.
These Restrictions can be bundled within the XSD to simpleTypes. Those
are transformed into A12 Data Types.
The Restrictions can also be present directly at the element or attribute
definition. Those are transformed to Validation Rules or added to the
respective Field’s Data Configuration, depending on the Restriction.
14

-- 14 of 14 --

