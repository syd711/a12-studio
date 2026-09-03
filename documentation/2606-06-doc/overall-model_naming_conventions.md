# overall model_naming_conventions

Model Naming Conventions
This section is about guidelines for modelers on how to name their models and
model elements (e.g. field names, groups names, validation rules, enumeration
values, categories etc.). Consistent naming is beneficial to keep a good
readability for you and others and provides a better understanding of your
project structure.
Introduction
Every (technical) name should be meaningful and describe the element it represents. Try to keep
the names as short as possible yet as long as necessary. For multi-word names, choose a case style
and stay consistent.
NOTE We recommend using Upper Camel Case (UpperCamelCase).
If you have many models in your workspace, we recommend using subfolders to maintain a clear
overview; for example, collect all models belonging to the same module in one folder. Alternatively,
if you use relationships, collect all generated Document Models or Binding Overviews in a separate
subfolder.
Naming of Document Model Elements
We recommend naming field names, group names, computation and validation rules, enumeration
1

-- 1 of 3 --

values, enumeration categories consistently in Upper Camel Case. For field and group names be
aware of the context and avoid redundancy (e.g. do not name a field "EmployeeFirstName", which
is already in the group "Employee"). For validation rules, construct the name by combining the
name of the error field with a description of the valid case (e.g., 'PassengerAgeOver18' for the error
condition 'FieldFilled(PassengerAge) And DifferenceInYears(PassengerAge, Today) < 18'). For
computation rules, append the suffix 'Comp' to the name of the computed field to easily associate
the computation rule with its computed field. For instance, the computed field is named "MyField",
so the computation rule is named "MyFieldComp".
NOTE
We recommend naming fields and rules descriptively. If you find this approach
unsuitable for your model, do not adhere rigidly, but still maintain naming
consistency.
The root group should be named like the Document Model (e.g. in Document Model
"PassengerRequest_DM" name the root group "PassengerRequest").
CAUTION If you use Heterogeneity and your Document Model is a Subtype, the root group
must be named like the root group of its Supertype Document Model.
Naming of Models
Since modeling starts from the Document Model, we recommend using its name for all respective
UI Models if there is only one model type per Document Model (e.g. Document Model
"PassengerRequest_DM" inherits its name to the Form Model "PassengerRequest_FM" etc.). For
Relationship Models construct the name from the name of the two underlying Document Models.
See Table 1. for examples referring to the Document Model "PassengerRequest".
TIP It is beneficial to quickly recognize which model belongs to which Document Model.
Table 1. Model names based on the Document Model "PassengerRequest_DM"
Model Type Standardized Name Example
App Model Name_AM PreviewApp_AM
Composed Document Model Name_CDM PassengerRequest_CDM
Composed Form Model Name_CFM PassengerRequest_CFM
Composed Overview Model Name_COM PassengerRequest_COM
Document Model Name_DM PassengerRequest_DM
Form Model Name_FM PassengerRequest_FM
Overview Model Name_OM PassengerRequest_OM
Master Detail Module Model Name_MDM PassengerRequest_MDM
Model Graph Diagram Name_MGD
Print Model Name_PM PassengerRequest_PM
Relationship Name(DM1)Name(DM2) PassengerRequestAddress
2

-- 2 of 3 --

Model Type Standardized Name Example
Tree Model Name_TM PassengerRequest_TM
Type Definition Model Name_TDM CommonFields_TDM
If you use Relationship Models, Heterogeneity or Bindings, there are some specific models
necessary. See Table 2 for examples for the Relationship Model "PassengerRequestAddress".
CAUTION Do not change the default name of generated Document Models.
Table 2. Names of special model types used for Relationships, Heterogeneity or Bindings (based on the
Document Model "PassengerRequest_DM")
Model Type Standardized Name Example
Additional Fields Form Relationship_LinkFields_FM PassengerRequestAddress_Link
Fields_FM
Binding Overview Model
(Available Items)
PersonAddress_Person_Availabl
eItems_OM
PassengerRequestAddress_Avail
ableItems_OM
Binding Overview Model
(Selected Items)
PersonAddress_Person_Selected
Items_OM
PassengerRequestAddress_Selec
tedItems_OM
Generated Document Model Relationship_Role____generated PassengerRequestAddress_Addr
ess____generated
Link Document Model Relationship_LinkFields_DM PassengerRequestAddress_Link
Fields_DM
Supertype (Document Model) Name_DM Person_DM
Subtype (Document Model) Name(Supertype)Name(Subtyp
e)_DM
PersonIntern_DM,
PersonExtern_DM
3

-- 3 of 3 --

