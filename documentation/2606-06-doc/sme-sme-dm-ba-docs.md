# sme sme dm ba docs

Document Modeling
Table of Contents
Introduction and Concepts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
Document Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Model Settings. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Multilingual Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Model Elements . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Fields. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Type Definitions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Groups . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
Validation and Computation Rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
Composition of Models with Includes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
Model Consistency Constraints . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Type Definition Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Model Compatibility and Expert Mode . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Installation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Migrating Existing Models to a New Version of the Tools . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Basic Editor Functions. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Open the Document Model Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Sidebar . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
Model Tree. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
Element Editors . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
Actions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
Keyboard Support . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
Expand and Collapse . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 18
Add an Element . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
Copy and Paste. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
Cut and Paste . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
Insert From DM . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
Delete . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
Multi-Selection and Bulk Operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
Ad Hoc Testing . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 21
Move. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
New Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
Search and Filter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
Search Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
Filter Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
1

-- 1 of 97 --

Model Name . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
Supported Characters . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
Locales . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
Labels . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
Annotations. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
Roles . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
Document Uniqueness Criteria . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
Expert Mode . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Time Zone . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Type Definitions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Undo and Redo Changes. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Changes in the Model Tree, Model Settings, and Type Definitions Overview . . . . . . . . . . . . . . . . 30
Changes in the Element Editors and Type Definition Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 31
Refactoring Support . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 31
Editors for Model Elements . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
Field Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
Name. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 34
ID . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 34
Path . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 34
Data Type. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 34
Required . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 34
Global . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Transient . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Label . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Descriptions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Annotations. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
String Fields. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Number Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
Date Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
Date Time Fields. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 42
Time Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Date Fragment . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Date Range . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
Confirm Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
Boolean Fields. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
Custom Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
Enumeration Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
Enumeration Values . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 49
Error Message . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
Enumeration Categories . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
2

-- 2 of 97 --

Type Definition Fields. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 51
Groups . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 51
Repeatability and Index Field. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 52
Validation Rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 54
Error Code . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 57
Error Field . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 57
Level . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 57
Condition . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 57
Additional Operator Information . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 58
Additional Element Information . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
Additional Parser Error Information . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
Allow Differing Decimal Places . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 61
Format . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 62
Maximize Rule Condition Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 62
Error Message . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 62
Computation Rules. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 64
Additional Element Information . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 67
Attachment . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 67
Multi-Select . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 69
Includes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
Document Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 74
Include Repeatability . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 75
Exclude Rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 75
Include Name . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 75
Copying Includes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 75
Type Definitions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 75
Local Type Definitions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 76
Imported Type Definitions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 78
Type Definition Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 78
Import Type Definition Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 80
Rule Contradictions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 80
Composed Document Model Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 84
Open the CDM Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 84
Add Elements to the CDM. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 86
Add a Root Document Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 86
Add a Relationship . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 87
Add a Relationship Including a Link Document Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 88
Add a Self-Referencing Relationship. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 88
Selectable Document Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 89
Selectable Relationships . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 89
Switching Between Document Model Elements. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 90
3

-- 3 of 97 --

This documentation is intended for a business analyst audience.
Introduction and Concepts
The Document Model Editor is part of the Simple Model Editor (SME) and enables domain experts
and analysts to create and modify domain-specific Document Models for business applications.
Document Models support the definition of complex Validation and Computation Rules using the
Kernel Language.
The core idea of mgm A12 is to encapsulate domain-specific knowledge in models. The Document
Model Editor is used to create Document Models containing fields and computation and Validation
Rules. A model typically aggregates all field and rule definitions that are needed for a particular
domain of an application. The Document Model Editor is complemented by other editors in the
Simple Model Editor which are used to create models of user interface elements such as forms and
overviews for the domain. The strict separation of data modeling and UI modeling is a core
principle of the A12 architecture. It leads to a high level of flexibility during the UI creation process
and promotes the re-usability of models. For example, you can create several different user
interfaces based on a single Document Model.
Rules are defined in their own domain-specific language, the Kernel Language, which is specifically
designed for applications with complex forms. All fields must be defined on predefined field types
(text, number, date, and others). In addition to the default field types, custom Type Definitions can
be specified. Any Document Model that has been created with the Document Model Editor can be
persisted in the file system as a JSON file.
The core of the editor is a hierarchical view of data fields and Validation and Computation Rules:
the DM Model Tree. The screenshot shown in Figure 1 provides a first impression of the user
interface. The details will be gradually explained in the subsequent chapters.
Switching Between Different Model Graph Diagrams . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 90
CDM-Specific Model Tree Elements . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 90
Document Model Element . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 90
Link Document Model Element . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 91
Relationship Element . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 91
Link Relationship element . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 92
Annotations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 93
Document Model Element Selection from Diagram . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 95
Refactoring. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 95
Document Model Specific Refactoring . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 96
Supported Operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 96
Reference Types. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 96
Example: Renaming a Field . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 96
Example: Moving a Field . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 97
4

-- 4 of 97 --

Figure 1. The DM Model Tree
Document Model
A Document Model is based on a defined set of fields. It includes Validation and Computation Rules
which are specified using the Kernel Language.
A Document Model consists of three parts:
1. A set of model settings, also called Header, which are defined as part of the model configuration
2. A set of fields and rules, ordered hierarchically by using groups
3. A set of reusable Type Definitions (optional)
Model Settings
The Document Model Editor defines a list of locales as a model setting that influences the model
behavior in several different ways.
Multilingual Models
A12 supports multilingual applications by allowing the declaration of the locales needed for a
Document Model in its Header part (see Locales).
Applications using this model or artifacts generated from this model can then choose which locale
to display to the user. This is often used for error messages (see Error Message), field labels,
enumeration values, or descriptions (see Descriptions).
Model Elements
The following sections will give a brief introduction of the elements of Document Models that can
5

-- 5 of 97 --

be used to create a Document Model and the Validation Rules on top of it.
Fields
Fields represent controls in an application form that may have quite different appearances. A field
can either be an input field for different data types or output text as well as a drop-down box from
which a value can be chosen.
The standard field types are
• String
• Number
• Boolean
• Date
• Date Time
• Time
• Date Fragment
• Date Range
• Enumeration
• Confirm
• Custom
All field types support multi-linguality: For each locale, an internal description (being displayed in
the Form Model Editor), an external description (being displayed as a hint in the UI) and a label can
be defined.
For more details about fields and field types refer to Field Editor.
Type Definitions
In addition to the field types, A12 supports the creation of your own reusable Type Definitions. They
are project specific "templates" of field data types that can be reused for multiple fields or can be
shared across Document Models. They are described in detail in Type Definitions.
In the Document Model Editor, the available Type Definitions are displayed in a separate menu
entry.
Figure 2. The Type Definition menu
6

-- 6 of 97 --

Groups
Groups are used to structure your Document Model in a logical or functional way. Groups can be
nested and represent a multilevel parent-child-structure. Groups can also have a multilingual
internal and external description.
Groups are the only element type that can have child elements.
As a special attribute, groups can be marked as "repeatable". This way they can handle multiple
instances of their child elements (for example, multiple addresses for a person’s contact data).
See also Groups.
Validation and Computation Rules
A Validation Rule is a condition that refers to one or more fields in the model. Rules are used to
restrict field values or to put them in a specified relation. They can also be used to define
computations where the result of a calculation rule can be bound to a specific field. Some common
scenarios are:
• The value of field A must not be empty.
• The value of field A must be in a certain range or match a specific pattern.
• If field A is not empty, the value of field B must fulfil certain criteria.
• Both fields A and B must either be empty or not empty.
• The value of field A must be equal to the sum of the values of field B and field C.
• The result of adding the values of the fields A and B should be shown in field C.
Due to the power of the Kernel Language the Document Model can be defined on a very fine level
(for example, much more detailed compared to XML Schema).
Rules also support the use of multiple locales. For more details about Validation Rules refer to the
reference Validation Rules and the separate Kernel Language Documentation.
Rules can also be used to perform calculations. This is described in detail in chapter 6 (Calculation
in A12) of the Kernel Language Guide.
Composition of Models with Includes
Apart from fields, groups, and rules the Document Model Editor also allows the specification of so
called include elements at any location of the model hierarchy, even at the top level.
An include is a specification of another (sub) model at a specific location of a (parent) model that is
interpreted as an inclusion of all Type Definitions, fields, groups, rules, and computations of the
(sub) model. Thus, an include offers the possibility to reuse existing configurations of fields, groups,
rules, computations, and Type Definitions for different models without copying. Changes to the
original (sub)model will also become effective in the included (sub)models, which cannot be
changed directly within the (main)model which is using the include. An example is shown in Figure
3: Here, the included model is Addresses_DM, however, its content is contained in a group called
7

-- 7 of 97 --

Addresses.
Figure 3. An Included Model
Included models can again use includes to reuse other models which in the end allows hierarchies
of models and also promotes modularity. The usage of includes is described in Includes.
However, there are some constraints on the use of includes:
• A model cannot include itself directly or indirectly.
• Included models are read only. That is, they cannot be altered in the editor from within the
parent model. It is only possible to view the properties of the included elements.
• Included models can only contain a single top-level group. This includes attachments which are
also groups.
• The top-level group of included models must not be repeatable. This excludes multi-selects from
being used as top-level group.
• The top-level group of included models must not be empty.
The include element is interpreted at runtime while only referencing the included model file at
specification time. That is why changes to the included model will always lead to changes of the
including model. The final interpretation of these includes happens in the Document Model Editor
for the modeler as well as the underlying generator which generates the validation data. See also
section Copy Includes.
8

-- 8 of 97 --

Model Consistency Constraints
There are several constraints on how Document Models can be put together. Most of them are very
specific and will not be discussed here as the editor will display error messages to warn about
problems.
However, there are some very basic constraints that should be kept in mind when working with
models:
• The top level of a model only allows groups, include elements, attachments and multi-selects.
• All elements within a group or on the top level of the model must have different names, e.g. two
sibling fields or fields and rules in a common parent group may not have the same name.
However, the same element name can be used across different groups within the same model.
Type Definition Model
A Type Definition Model is a Document Model which contains no groups, fields and rules but only
Type Definitions. Find more information on how to create and use Type Definition Models in
Imported Type Definitions.
Model Compatibility and Expert Mode
The so called expert mode flags Kernel features that can be handled by A12 but are not available in
the modeling tools. It is not recommended to use any of those features, as they are maintained for
compatibility reasons only. The following expert mode features are supported for Document Model
field types:
• String
◦ no value validation
• Number
◦ min/max length
• Date
◦ partially known dates allowed
◦ check that the date should be after 1900
• Date range
◦ interpretation of year
Installation
The Document Model Editor is part of the Simple Model Editor. For advice on the installation of the
Simple Model Editor, refer to the "Quick Start Guide" on getA12 under "Modeling".
9

-- 9 of 97 --

Migrating Existing Models to a New Version of the
Tools
It might be necessary to migrate existing Document Models in case a new version of the Simple
Model Editor and thus Document Model Editor has been installed.
In most cases, the Simple Model Editor can handle updating versions itself. For more information,
refer to the documentation of the Simple Model Editor.
NOTE
Note that the version number of the migration tool for Document Models is related
to the Kernel version integrated into the Simple Model Editor/Document Model
Editor and not to the Document Model Editor or SME version itself. The associated
Kernel and Document Model versions can be seen on the A12 release page and in
the Model Versions Info of SME.
Basic Editor Functions
Open the Document Model Editor
To access the Document Model Editor, open a Workspace in the Simple Model Editor (SME). If
existing models should be edited or viewed, they need to be in the opened Workspace. After
opening a Workspace, all models that are contained in it are displayed in the Workspace Explorer
of the Simple Model Editor. Document Models will be recognized as such by the Simple Model
Editor, and the respective icon "D" will be displayed next to them.
10

-- 10 of 97 --

Figure 4. Workspace Explorer Containing Document Models, Among Others
11

-- 11 of 97 --

For more information on Workspaces in the Simple Model Editor, refer to the SME documentation.
To open the Document Model Editor for an existing model, click on the model name in the
Workspace Explorer.
To create a new Document Model, use the "Add" button in the header of the Workspace Explorer
and select "Document Model".
Figure 5. Add a New Document Model
A modal will then be displayed to define the most important model settings: target folder, model
name, locale, and roles.
12

-- 12 of 97 --

Figure 6. Enter Initial Model Settings for New Document Model
The model name must be unique in the Workspace.
It is important that the folder selected is actually the target folder for this model. The location is
required to correctly set the path for includes, so changing the folder later might cause issues if
includes are used.
At least one locale must be entered. Note that only locale codes according to ISO 639 alpha-2 or
alpha-3 are allowed. It is possible to add a region code after an underline, such as en_US, de_DE,
de_CH, and so on.
The roles that you can assign to your model are taken from a YAML file in the Workspace. If you
have created your Workspace using the Preview App Control, this file is in the "user" folder and is
called "access-rights.yaml". If no valid YAML file is visible to the Simple Model Editor, then the drop-
down will be blank and the assignment of roles is optional.
Sidebar
In the sidebar of the Document Model Editor, the name of the Document Model is displayed in the
top. The following perspectives can be accessed:
• Model Tree
• Settings
• Type Definitions
• Rule Contradictions
Below the menus, the following buttons are available:
13

-- 13 of 97 --

• Deploy
• Cancel
• Save As
• Save
The menus as well as saving a model will be described in the subsequent chapters.
Model Tree
The Model Tree is the central editor component for the design of Document Models. Here, elements
such as groups, fields and rules can be added, edited, deleted and viewed.
Figure 7. Model Tree and Field Editor of the Document Modeling Module
Since the elements are displayed in a tree, each element represents a node in the tree. An action
that acts on an element is thus a node action. Some nodes can contain other elements, such as group
nodes. When a node contains other elements, it becomes a parent node, and all elements contained
in it are its child nodes.
All model elements are placed below a virtual Model Tree node. The row actions of the virtual
Model Tree node allow to add and paste elements on root level of the model. The virtual Model Tree
node will not be saved to the Document Model file.
14

-- 14 of 97 --

Figure 8. Select a Root Element to Add
Different nodes are displayed using different icons in the Model Tree:
• Groups have a folder icon and their name is displayed in bold font
• Fields are represented by squared icons; two different types are distinguished:
◦ Fields that are not computed are represented by an "F" icon
◦ If a field is a computed field, it is instead represented by a "+ -" icon
• Rules are represented by diamond-shaped icons; four different types are distinguished:
◦ Validation Rules of the level "error" are represented by a "V" icon
◦ Validation Rules of the level "warning" are represented by a "W" icon
◦ Validation Rules of the level "info" are represented by a "i" icon
◦ Computation Rules are represented by a "+ -" icon
Figure 9. Icons Used in the Model Tree
Element Editors
To open a node in the tree, click on it using the left mouse button. Then, its corresponding Element
Editor (the Field Editor for a field node, the Group Editor for a group node and so on) is opened on
the right. See Editors for Model Elements for more documentation on the different Element Editors.
In the bottom right of each Element Editor, buttons to apply the changes to the element ("APPLY") or
15

-- 15 of 97 --

discard the changes to the element ("CANCEL") are displayed.
NOTE
Be aware that "APPLY" only applies the changes to the respective element. It will not
trigger saving the entire model! To save the model, use the "SAVE" button in the
Sidebar.
Actions
Actions in the Model Tree can mainly be found in the context menu of the respective element nodes
which can be accessed via the three dot icon in each row.
Figure 10. Context Menu Containing Node Actions for a Group
In the subheader, multi-select actions are available which can be toggled by the multi-select button
(see Multi-Selection and Bulk Operations).
16

-- 16 of 97 --

Figure 11. Multi-Select Actions, Expanded
Keyboard Support
Keyboard support is available for Windows, Linux, and MacOS. It is available for the following
actions:
Action Windows + Linux macOS
save Ctrl + S Cmd + S
save as Ctrl + Shift + S Cmd + Shift + S
cancel Alt + W Ctrl + W
deploy Ctrl + D Cmd + D
navigate to Model Tree Alt + 1 Cmd + Ctrl + 1
navigate to Settings Alt + 2 Cmd + Ctrl + 2
navigate to Type Definitions Alt + 3 Cmd + Ctrl + 3
navigate to Rule Contradictions Alt + 4 Cmd + Ctrl + 4
undo changes Ctrl + Z Cmd + Z
redo changes Ctrl + Y Cmd + Y
For all context menu actions of the Model Tree, keyboard support is available as well. The target of
a keyboard shortcut is the element on which the focus is placed. You can set the focus on an
element without opening it by clicking on it with the third mouse button/mouse wheel or by using
the arrow keys or tabulator to navigate to the desired element.
Action Windows + Linux macOS
expand all Alt + E Alt + E
collapse all Alt + Shift + E Alt + Shift + E
toggle expand/collapse Ctrl + E Cmd + E
toggle multi-selection mode Ctrl + M Cmd + M
17

-- 17 of 97 --

Action Windows + Linux macOS
toggle multi-selection of all
nodes
Ctrl + Space not yet supported
delete Delete Delete
copy Ctrl + C Cmd + C
cut Ctrl + X Cmd + X
paste Ctrl + V Cmd + V
Ad Hoc Testing Alt + T Alt + T
insert from DM Ctrl + Shift + C Cmd + Shift + C
Adding elements to the Model Tree:
Action Windows + Linux + macOS
add Group Alt + G
add Field Alt + F
add Validation Rule Alt + R
add Computation Rule Alt + C
add Attachment Alt + A
add Multi-Select Alt + M
add Include Alt + I
Keyboard support is available inside Element Editors as well:
Action Windows + Linux macOS
apply changes Ctrl + Enter Cmd + Enter
cancel changes Alt + W Ctrl + W
navigate to first and second tab,
respectively
Alt + 1, Alt + 2 Ctrl + 1, Ctrl + 2
undo and redo changes,
respectively
Ctrl + Z, Ctrl + Y Cmd + Z, Cmd + Y
Expand and Collapse
Single nodes such as groups, multi-selects and attachments, can be expanded and collapsed via the
arrow icon left of the element name.
It is possible to expand or collapse all elements of the model via the virtual Model Tree node.
In addition, it is possible to expand or collapse all elements inside a specific node via the node
actions "Expand All" and "Collapse All" in the node’s context menu.
18

-- 18 of 97 --

Add an Element
When adding an element to the Model Tree, it needs to be distinguished between elements on root
level and elements that are added as children of an existing group. On root level, only groups,
attachments, multi-selects and includes can be added. All other elements can only be children of an
existing group. To add an element on root level, use the context menu of the virtual Model Tree
node.
Figure 12. Select a Root Element to Add
To add an element as child of an existing group, use the group’s context menu. There, you find
separate node actions to add each possible element. Adding an element is only available as node
action of groups, not of fields.
Alternatively, elements can be added below existing elements. Use the context menu of the target
element and choose the element type inside the sub context "Add sibling". If a Validation or
Computation Rule is added below a Field, the error/computed field of the new Rule is prefilled with
the target Field.
Copy and Paste
Copying a single node is possible via its context menu. There are a number of things to consider
when copying elements:
• Copy & Paste of group nodes will also copy all of the child elements of the group
• Copy & Paste of elements inside their original parent group (or on the top level) will result in
renaming as they cannot have the same names as the original elements. The copied elements
will have a _COPY suffix appended
• Renaming also applies when elements are copied into a group that already contains elements
with the same names
• It is possible to copy several elements at once by doing a multi-selection before copying
• Copy & Paste of single rules can quickly result in errors. The fields that the rule refers to in its
Rule Condition must be reachable from the position of the copied rule
• Copy & Paste of includes will only copy the include, there is currently no option to copy the
19

-- 19 of 97 --

content of the include.
The paste action is only active if the target, be it the root action or a node action, is a valid target for
the copied content. For example, it is not possible to paste a field node onto the root level, thus the
"PASTE" button in the subheader will not become active. If a group node has been copied, the
"PASTE" button in the subheader will become active.
Cut and Paste
Cut and Paste works in similar fashion as Copy and Paste. When the "Cut" action is used instead of
the "Copy" action, the selected element(s) will be moved to the target destination instead of a copy
of the selected element(s) being created.
Insert From DM
With this action, it is possible to insert a copy of all elements of another Document Model as
children of the respective group node or on root level when using the action in the virtual Model
Tree node. The group structure of the original model will be preserved. All includes of the source
Document Model will be resolved when inserting.
Delete
Deleting a node is possible via its context menu.
On an attempt to delete a field that is used as an error field in one or more Validation Rules, a
confirmation dialog enables choosing between deleting only the field and deleting the field as well
as the Validation Rule(s) for this field.
Similarly, an attempt to delete a computed field triggers the display of a confirmation dialog that
enables choosing between deleting only the field and deleting the field as well as the Computation
Rule(s) for this field.
Multi-Selection and Bulk Operations
To toggle the multi-selection mode of the Model Tree, use the toggle button in the subheader.
Figure 13. Use This Button to Switch Between Single and Multi-Selection Mode
In the multi-selection mode, an additional column will appear as left-most column in the Model
Tree. It contains checkboxes to select single nodes as well as a checkbox to select all elements in the
column header.
Selecting an element that contains child elements such as groups always selects all children as well.
It is possible to de-select single children of a selected parent node. Then, the state of the checkbox
will switch from "selected" (checkmark) to "indeterminate" (square).
20

-- 20 of 97 --

Figure 14. Multi-Selection in the Model Tree
As soon as at least one node has been selected in the Model Tree, the bulk operations become
active, while all non-bulk operations become inactive. The only exception to this is the paste action
on root level as well as on node level.
As bulk operations, copy, cut, delete, and Ad Hoc Testing are available.
Using the toggle button again will hide the multi-selection column. If nodes are selected, a
confirmation modal will appear, since hiding the multi-selection column will lose the multi-
selection as well.
Ad Hoc Testing
Ad Hoc Testing is a feature of the Document Model Editor that creates and shows a preview form
containing form elements for a set of selected Document Model elements. This is especially helpful
for early experiments regarding proper validation of the selected elements.
It can be triggered in two ways:
• As bulk operation, by clicking the lightbulb icon in the bulk operation bar after performing a
multi-selection
• As operation on a single model element, by clicking the lightbulb icon in the context menu of
the Model Tree entry
In the latter case, the Ad Hoc Testing of groups, includes, and multi-selects incorporates all model
21

-- 21 of 97 --

elements these contain.
If a rule is selected for Ad Hoc Testing, all Document Model elements that the rule relies upon are
automatically included in the preview form. With a bulk operation it is possible to test multiple
rules and computations at once.
When Ad Hoc Testing is triggered for a single field, only the validation of the Data Type, Data Type
Configuration and Required (if Parent Group is Filled = no) are activated. If further validation
should be activated in the preview form, the corresponding rule(s) must be selected with a bulk
operation. Consequently, if a user manually selects several fields but not the rules that should be
tested, their validation is not applied in the preview form.
As a best practice, select only the rules that should be tested and not the model elements that the
rule relies upon.
Additionally, ad hoc testing provides automatic synchronization, allowing the user to modify
selected elements, renaming them, updating Type Definitions or field types. This ensures a
harmonious and uninterrupted user experience, since the user does not have to reload the preview
window to see the changes their actions caused.
Figure 15. Ad Hoc Testing of a Multi-Selection of Document Model Elements
22

-- 22 of 97 --

The Ad Hoc Testing for the multi-selection of elements as depicted in Figure 14 opens the preview
depicted in Figure 15. The validation is triggered by clicking the checkmark icon in the sidebar
menu.
Move
Elements like fields and groups can be moved from one group to another by dragging selected
elements and dropping them on the target group.
Multiple elements can be moved if they are selected via the multi-selection.
When moving a field that is used in at least one validation or Computation Rule, the SME displays a
dialog with the option to perform a refactoring operation that renames the field references in these
rules. Similarly, when moving a rule, a dialog offers the option to adapt the paths of field references
in this rule according to its new location.
New Model
In a new Document Model, the Model Tree is initially empty and an "ADD" button is displayed.
Search and Filter
In the header of the Model Tree, it is possible to filter the Model Tree for element types and search
for elements.
Figure 16. Filter and Full Text Search
Search Settings
Elements can be searched by either their name (default), label or ID. This can be configured by
clicking on the settings icon and choosing another option.
Figure 17. Search settings
23

-- 23 of 97 --

Filter Settings
To toggle the display of fields, Validation Rules and Computation Rules, use the filter button. A pop-
up will be displayed to restrict the display of fields, Validation Rules and Computation Rules in the
Model Tree.
24

-- 24 of 97 --

Figure 18. Filter for Model Elements
Initially, all elements are displayed. De-select element types to filter the Model Tree. An indicator on
the filter icon will show if the view is currently filtered.
25

-- 25 of 97 --

To search for elements by their name, use the search field. Only elements which (partially) match
the input will be displayed. To reset the view, clear the search field.
The reset button next to the filter button will reset the view by removing any filter for elements and
any search.
Settings
In the Settings menu, model-wide settings can be made.
Figure 19. The Model Settings Editor
Model Name
The name of the Document Model. It needs to fulfill certain conventions: Only letters, digits,
hyphens, underscores and periods are allowed. Furthermore, the name of the model must not start
with "xml" and must be at most 100 characters long.
The model name must be unique in the Workspace and it must match its filename.
Configuration
Supported Characters
It is possible to maintain character sets for the documents of a Document Model. A character set is
26

-- 26 of 97 --

basically a set of (unicode)-characters, which are allowed to be used inside String fields. If users
attempt to save a document that uses a String value in a String field that contains a character which
is not in the character set, a formal error is reported. This does not only hold for fields that are
directly of type String, but also restricts fields whose type evaluates to a String, such as fields that
have a custom Type Definition as data type and enumeration values. Furthermore, it also applies to
fields whose value is computed. Character sets should not be confused with encoding. Encoding is
the way the text is encoded, not the set of allowed characters.
By default, no supported characters are set explicitly and Document Models assume Unicode-Plane-
0 as character set.
To indicate supported characters that deviate from the default, the allowed characters can be
entered by editing the Supported Characters field. Within the modal the supported character set can
be specified and will be imported.
NOTE
During import, the character set is simplified as follows:
• Areas and individual characters whose Unicode code points (e.g. “0x0043”) are
adjacent to or overlap other areas or individual characters are merged.
• All duplicates are removed from the combined characters.
• The result is the list of all sorted ranges and individual characters, followed by
the sorted combined characters.
• Sorting is in ascending order by Unicode code point value. Combined characters
are also sorted in ascending order by string length.
Each allowed character has to be enclosed by quotation marks and allowed characters must be
separated by commas. In its entirety, the content describes a character array in JSON format. The
input has to be surrounded by square brackets, such that it yields an input like this:
[ "\n", "\r", " ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-",
"a", "ß", ... ].
Within the brackets, there are the following options for defining the character set:
• Individual characters, e.g. [ "a", "b", "c", … ].
• Ranges of single characters, e.g. [ "a-z", "A-Z", "0-9", …]. The ranges can also be defined
using the corresponding Unicode code points, e.g. [ "\u0061-\u007A", "\u0041-\u005A", "\u0030-
\u0039", …].
• Combined characters that consist of a maximum of three individual characters, e.g. [ "C̆",
"C̨̆", … ] .
• Concatenated combined characters in which a maximum of three individual characters occur in
total, e.g. [ "K͟H", "K͟h", "k͟h", … ].
It can be used to define a character set that conforms to the DIN 91379 standard.
If one or more characters are indicated explicitly in the supported characters field, the default is
27

-- 27 of 97 --

overridden, that is, only the characters indicated explicitly are allowed and all other characters are
forbidden. Note that it is only possible to allow characters of Unicode-Plane-0. Other characters,
such as emojis, cannot be added.
The content of a valid input for the standard character set is as follows:
["\n", "\r", " ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-",
".", "\/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">",
"?", "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\", "]", "^", "_", "`",
"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
"r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "|", "}", "~", "¡", "¢", "£", "¥",
"§", "©", "ª", "«", "¬", "®", "¯", "°", "±", "²", "³", "μ", "¶", "·", "¹", "º", "»",
"¿", "À", "Á", "Â", "Ã", "Ä", "Å", "Æ", "Ç", "È", "É", "Ê", "Ë", "Ì", "Í", "Î", "Ï",
"Ð", "Ñ", "Ò", "Ó", "Ô", "Õ", "Ö", "×", "Ø", "Ù", "Ú", "Û", "Ü", "Ý", "Þ", "ß", "à",
"á", "â", "ã", "ä", "å", "æ", "ç", "è", "é", "ê", "ë", "ì", "í", "î", "ï", "ð", "ñ",
"ò", "ó", "ô", "õ", "ö", "÷", "ø", "ù", "ú", "û", "ü", "ý", "þ", "ÿ", "Œ", "œ", "Š",
"š", "Ÿ", "Ž", "ž", "€"]
CAUTION
The String data used in Document Metadata will be validated to ensure that the
characters used are included in the Supported Characters defined in the
Document Model.
Modeling a restricted set of Supported Characters can lead to formal validation
errors when adding or updating the Document Metadata which prevent the
Document from being saved.
Locales
This field stores a list of locales for the model. Each locale is represented by a row in the table.
At least one locale must be entered. Note that only locale codes according to ISO 639 alpha-2 or
alpha-3 are allowed. It is possible to add a region code after an underline, such as de_DE, de_CH and
so on.
NOTE
If you want to test your models in the Preview App, model the locales en and de.
Note that locales like de and de_DE (or en and en_US) are formally different locales. de
is not treated as a fallback for de_DE (and en is not a fallback for en_US).
The editor will show a separate input field for every given country code where multilingual inputs
are possible, for example for error messages, labels, and descriptions.
If a locale is deleted from the Document Model by deleting its row from the locale table, a warning
will be shown mentioning that all texts that have been set up for that locale will also be deleted.
For more information about locales see Multilingual Models.
28

-- 28 of 97 --

Labels
These fields store a list of labels for the model, one for each specified locale.
The labels can be used as a localizable representation of the model itself in a list of different
models. For example, in a model repository. The label of the Document Model will also be used in
the variant selection modal for example when using the A12 feature "Heterogeneity".
Annotations
An Annotation is a name-value pair that can be added to the model in the model settings and all
model elements. The application that uses the Document Model can access those Annotations and
can use them within custom implementation, for example, to show all fields that have an
Annotation in boldface.
In the model settings, at least one Annotation is set which contains the roles for this model.
Roles
Roles can be indicated in the Document Model when it is created. Afterwards, the roles can be
inspected in, added to, and removed from the list of available roles in the Settings menu.
Document Uniqueness Criteria
Document Uniqueness Criteria specify that a given set of Fields must contain a unique combination
of values across all existing documents. They are evaluated when a document is saved. If all
contributing Fields hold the same values as an already existing document, the save is rejected and a
localized error message is shown to the end user.
Name
Name of the Document Uniqueness Criterion. This name must be unique within the Document
Model.
Fields
List of Fields that contribute to the Document Uniqueness Criterion. Only Fields that meet all of
the following prerequisites can be selected:
• marked as Required, with Only if Parent Group Filled set to false
• not repeatable
• not transient
An error is shown if a selected Field does not fulfill one of these prerequisites (any more).
Error Messages
An error message per Locale can be defined. It is possible to specify Label- and Value-
placeholders to the contributing Fields with the $-Operator.
29

-- 29 of 97 --

Expert Mode
Time Zone
The time zone is used for date, date time and time fields to correctly display their respective values
in the UI. Currently, time zones "UTC" and "Europe/Berlin" are available. For new Document Models
it is set automatically to "UTC". If a Workspace contains Document Models with differing time
zones, the models which use time zone "UTC" will be marked as invalid.
Type Definitions
In the Type Definitions menu, Type Definitions can be viewed, added, edited and deleted. Type
definitions can either be defined in the edited model or be introduced via an include or import.
Only Type Definitions that have been defined in the edited model can be edited or deleted.
For information on what Type Definitions are and how they work, refer to the Type Definitions.
Undo and Redo Changes
It is possible to undo or redo changes in the Model Tree as well as in an Element Editor, the Model
Settings and the Type Definitions Overview and Editor. Undo/redo buttons are displayed in the top
right corner of the overviews/editors.
Figure 20. Undo/Redo Buttons in Model Tree and Field Editor
It is possible to use the shortcuts Ctrl+Z for undo and Ctrl+Y for redo.
There are two levels of changes that can be undone separately:
Changes in the Model Tree, Model Settings, and Type Definitions Overview
Changes in the Model Tree include:
• changes to an element done in its editor (this summarizes all individual changes in the Element
Editor into one change in the Model Tree).
• reordering/moving of elements.
30

-- 30 of 97 --

• adding/deleting elements.
Changes in the Model Settings include:
• all changes to fields.
• adding/deleting rows.
Changes in the Type Definitions overview include:
• changes to a Type Definition done in its editor (this summarizes all individual changes in the
Type Definition Editor into one change in the Type Definition overview).
• adding/deleting a Type Definition.
• adding/removing an import of a Type Definition Model.
Changes in the Model Tree, Model Settings and Type Definition overview can be undone/redone on
the same level, that is, if a change is made to the Model Tree and then to the Model Settings, an
undo action will first revert the change to the Model Settings, a second will lead the view back to
the Model Tree and a third one will undo the change to the Model Tree.
The element’s icon will wobble as visual feedback to the modeler if a change is being
undone/redone to an element.
Changes in the Element Editors and Type Definition Editor
The Element Editors and Type Definition Editor will be considered separately when it comes to
undo and redo. While an Element Editor or the Type Definition Editor are open, the undo/redo
buttons will be inactive in the Model Tree or Type Definition overview, respectively.
All changes done in an Editor can be undone/redone. Once an Editor is closed via Cancel or Apply,
there is no possibility to undo/redo anymore.
Refactoring Support
The Document Model Editor supports automatic refactoring for both within-model and cross-model
references. When you rename, move, or delete elements that are referenced elsewhere, the tool
identifies affected references and presents options for updating them.
For general information about the refactoring feature, workflow, and actions, see the Refactoring
Support documentation.
For detailed information specific to Document Model refactoring, including supported operations,
reference types, and examples, see the Refactoring chapter.
NOTE
Within-model refactoring is an experimental feature that can be enabled or
disabled in the SME Tool Settings. See Enabling Within-Model Refactoring for
configuration instructions.
31

-- 31 of 97 --

Editors for Model Elements
This chapter describes all of the various Element Editors for the different model elements that can
be edited in the Document Model Editor.
Field Editor
A field is a single element of data in a Document Model. The field can be used to store user input or
have a fixed value, for example.
Fields have a data type that determines what kind of values can be assigned to them. Depending on
the data type also a Data Type Configuration can be defined which can put restrictions on the values
allowed to be entered. Data Type Configurations may also extend the field or provide additional
formal aspects related to formatting.
Fields also have a few general properties which are independent of the data type. Those general
properties are shown in the top of the Field Editor (see Figure 21). The properties specific to a data
type can be set below.
32

-- 32 of 97 --

Figure 21. Properties Common to All Field Types
33

-- 33 of 97 --

Name
The name of the element.
Constraints: Names always must start with a letter and may not contain any whitespace or special
characters except for underscores. They must not start with "xml". Siblings and rules within the
same group must have different names. Furthermore, the length is limited to 200 characters.
ID
IDs are auto-generated by the Document Model Editor and thus are read only.
Path
The path of the field in the Document Model is displayed.
Data Type
The Data Type property determines the data type of the field. There are twelve options to choose
from:
• String
• Number
• Date
• Date Time
• Time
• Date Fragment
• Date Range
• Confirm
• Boolean
• Custom
• Enumeration
• Type Definition
Those will be explained in the following sections in more detail.
Required
The Required property declares whether or not a Field is mandatory. If a document is validated
without a value given for a mandatory Field Instance, an error will be returned. The setting "Only if
Parent Group is filled" may be used in combination with the Required property to modify the
conditions when a field is mandatory. In addition, repeatability is a further condition:
A Field is repeatable if it is within a repeatable Group - either as a direct child or nested within
other subgroups. For this repeatable Field there will be Field Instances according to the created
34

-- 34 of 97 --

repetitions (see also Kernel Language Documentation). These Repetitions might either be created by
the user or by setting Initial Rows in the corresponding Form Model.
If "Only if Parent Group is filled" is set to false (default setting), all Field Instances are mandatory.
Therefore:
• A field with a repeatability of 1 is mandatory and must be filled.
• A field with a repeatability greater than 1 is mandatory each time an instance of this field is
present.
If "Only if Parent Group is filled" is set to true, only those Field Instances are mandatory, which are
in a Parent Group that contains data. This is given, whenever the Kernel Language construct
"GroupFilled(RuleGroup)" evaluates to true for a Validation Rule in the same Parent Group. With
this said, the property extends the semantics of "FieldNotFilled(Field)" to "GroupFilled(RuleGroup)
and FieldNotFilled(Field)".
NOTE To test this property, at least one field in the parent group must be selected along
with the field in the Ad Hoc Test.
If "Use Default Error Messages" is selected, a localized text like "This field is required." will be
shown. Customized texts per Field can be given, if "Use Default Error Messages" is deselected.
NOTE The default message can be changed via the localization API.
Required Fields are decorated with a symbol according to their settings:
Figure 22. Only if Parent Group is Filled is Set to False
Figure 23. Only if Parent Group is Filled is Set to True
Global
The property Global works as a tag and identifies fields of fundamental relevance. In case of a
partial validation, all global fields of a model are added to the set of relevant fields (see also Kernel
Language Documentation). This property can be selected via a checkbox.
Transient
If the Transient property is selected, the field will not be persisted. This can for example be used for
interim results of a complex calculation. It can be selected via a checkbox.
NOTE By default, Data Services will not persist fields marked with the respective property.
This can be changed via configuration.
35

-- 35 of 97 --

Label
This property can be used as a default field label in a user interface. It can be entered for each
language that has been set up for the model (see Multilingual Models and Locales).
NOTE
The contents of this field will be displayed read-only in the Document Model Label
(Multilingual) field within the Edit Control dialog of the Form Model in the Form
Model Editor as well as in the Overview Model Editor. Unless another label is
defined in those Editors, this label will be used by the respective engine.
Descriptions
There are two types of descriptions: internal and external description. Both of them can be defined
individually for each language for which the model has been set up.
Figure 24. Descriptions in the Field Editor
• Description (Internal): These descriptions are used for technical documentation purposes, for
example as a technical description of a field or Validation Rule. The contents of this field will be
displayed read-only in the Internal Field Description (Document Model) field within the Edit
Control dialog of Form Models referring to this Document Model.
• Description (External): The external description is a way to add a hint to this field in the UI.
More information on hints on different levels and their display in the UI can be found in the
Form Modeling Documentation.
36

-- 36 of 97 --

• Helper Text: A helper text is shown below the input of this field in the UI. Helper texts cannot
be defined for Confirm data types or Type Definitions.
Annotations
An Annotation is a name-value pair that can be added to the model in the model settings and all
model elements. The application that uses the Document Model can access those Annotations and
can use them within custom implementation, for example, to show all fields that have an
Annotation in boldface.
Annotations can be maintained in each Element Editor. An "A" icon is displayed behind an
element’s name in the Model Tree if it contains Annotations.
String Fields
Strings are field types that can contain arbitrary text which might be limited by setting up regular
expressions. The data type configuration for string fields is shown in Figure 25.
Figure 25. String Data Type Configuration
NOTE The option "Trailing/Leading Blanks Permitted" does not exist anymore.
As part of the DataType Configuration string fields can be restricted by:
Min. Length
The minimum number of characters of the text. This must be a valid integer number.
Max. Length
The maximum number of characters of the text. This must be a valid integer number.
Pattern
A user-defined regular expression in Java/Perl syntax that restricts possible values of this field in
37

-- 37 of 97 --

documents. For example, the pattern [a-zA-Z]+ only allows strings that have at least one letter,
either in lowercase or in uppercase, while other characters are not allowed. The character limit
for regular expressions is 1000 characters. For some more details on that syntax, refer to the
Kernel Language Documentation.
NOTE
When using the pattern property, additionally an error message has to be
entered which will be displayed in case the rules defined for the pattern are
violated.
There is a built-in check if the input in the field "Pattern" results in a valid regular expression.
Line Breaks Permitted
To enable multi-line input. Defaults to single-line input.
Alphabetical Sorting
To enable the alphabetical sorting of the localized texts during runtime. It only makes sense to
enable this option for a string field, if the field is used in an enumerable context (for example,
external enumerations).
Suggestions
This property provides end users with multiple suggestions (hint list) for data entry into a string
field. The user can either choose from the predefined string values of the suggestions or add a
custom value. The values of the suggestions are maintained as localized and are validated
against other field restrictions, such as defined patterns. The resulting document does not
distinguish whether the value is from the hint list or a user-defined entry.
38

-- 38 of 97 --

Figure 26. String Data Type Configuration with Suggestions
39

-- 39 of 97 --

Figure 27. List of Suggestions Maintained per Locale
Number Fields
The data type configuration for number fields is shown in the Figure 28.
Figure 28. Number Data Type Configuration
The number field type is suitable for all data fields that might be involved in arithmetic calculations
or comparisons.
It should not be used for fields that can only contain digits but are not used for calculations, such as
postal codes or numeric identifiers.
The following restrictions can be set for number fields:
Decimal Places
Defines the number of decimal places allowed. If less than the maximum number of decimal
places are entered, the remaining decimal places will be padded with zeroes in the Form Engine.
There are three options:
40

-- 40 of 97 --

• NONE (no decimal places) - no subfields displayed.
• FIXED (fixed number of decimal places) - when choosing that option, the subfield Decimal
Places will be displayed for setting the number of decimal places. When entering a value
with less than the fixed number of decimal places into a number field with a fixed number of
decimal places, the remaining decimal places will be filled with zeros.
• RANGE (a range with minimum and maximum number of decimal places) -for that option the
subfields Min. Decimal Places and Max. DecimalPlaces are displayed to allow to specify the
minimum and maximum number of decimal places. When entering a value with less than
the minimum number of decimal places into a number field with a range of decimal places,
the minimum number of decimal places will be filled with zeros.
Zero Allowed
To allow zero values for the field. If not allowed and a zero value is entered, an error message
will be displayed (Invalid Value: The provided value must not be equal to 0).
Leading Zeros Allowed
This setting will allow to enter leading zeros in a number field.
Positives Only
This setting will allow only positive values in a number field. Note: This setting does not affect
whether zero is a valid value. This is controlled by the Zero Allowed setting.
Min. Value
The minimum value of the user input. This may be a floating-point number if decimal places
were defined.
Max. Value
The maximum value of the user input. This may be a floating-point number if decimal places
were defined.
Max. Integer Digits
Defines the maximum number of digits allowed before the decimal separator in a number field.
Unit
This option can be used to give the field a special meaning, such as Amount for monetary values
or Percent and Permille. It does have an effect on the data type configuration options, and only 0
or 2 decimal places are allowed when choosing the amount unit. Other than that, the validation
of the field is not changed, except for different default error messages for amount number fields.
Date Fields
The A12 Document Model supports the definition of dates: Figure 29 depicts the dialog for setting
the data type configuration of date fields. No further configuration options are available for this
data type.
41

-- 41 of 97 --

Figure 29. Date Data Type Configuration
Figure 30. Date Picker in the Preview Mode
NOTE
Note that the dates will be stored within the documents always in the YYYY-MM-DD
format, for example 2023-05-28. When selecting a date using the date picker, the
date format for displaying the date depends on the localization selected. For
example, for the English/United States localization it would be shown as 05/28/2023
whereas for the German/Germany localization 28.05.2023 would be displayed.
Date Time Fields
In addition to the use of date fields the Document Model Editor supports the definition of date time.
The data type does not have further configuration options (similar to Date and Boolean).
42

-- 42 of 97 --

Figure 31. Date Time Data Type Configuration
NOTE Note that in A12 internally the ISO standard format for date, time and date time will
be allowed.
Time Fields
There is also the option to define a field with the data type time within Document Model Editor. In
this case no further configuration options are available for this data type, it therefore corresponds
to the options of the Date or Boolean type.
NOTE Refer to the Kernel Language Documentation for further information about the
validation of the data types Date, DateTime and Time.
Date Fragment
The date fragment data type is intended to use for date specifications that are not known or needed
as a complete date. Four different formats can be specified:
• MM
• YYYY
• YYYY-MM
• MM-DD
43

-- 43 of 97 --

Figure 32. Date Fragment Data Type Configuration
Date Range
Date ranges can be specified using five different formats:
• YYYY/YYYY
• YYYY-MM-DD/YYYY-MM-DD
• MM/MM
• YYYY-MM/YYYY-MM
• MM-DD/MM-DD
44

-- 44 of 97 --

Figure 33. Date Range Data Type Configuration
Confirm Fields
A Confirm field is either True or not set (null).
No further configuration options are available for this data type.
Figure 34. Confirm Data Type Configuration
Boolean Fields
The Boolean field type can contain True or False as value or is not set (null).
No further configuration options are available for this data type.
45

-- 45 of 97 --

Figure 35. Boolean Data Type Configuration
NOTE If a Boolean is required it can be True or False, so either is a valid value, but if a
Confirm type is required the only valid value is True.
Custom Fields
Custom fields are a way to define special fields with project-specific validation logic, that cannot be
adequately expressed using regular expressions. The Kernel provides the implementations for
some Custom Field Types. For information on how to create your own Custom Field Type refer to
the Kernel Developer Documentation.
The data type configuration for custom fields is shown in Figure 36.
Figure 36. Custom Data Type Configuration
For custom fields the following configuration options are available:
Name
The name of the Custom Field Type to be used.
46

-- 46 of 97 --

Min. Length
The minimum number of characters.
Max. Length
The maximum number of characters.
Enumeration Fields
Enumeration fields can be used for selecting values out of a predefined set of alternatives. In Figure
37 the dialog for setting the data type configuration for enumeration fields can be seen.
47

-- 47 of 97 --

Figure 37. Enumeration Data Type Configuration
Enumeration fields offer a list of predefined values for the user to choose from. Such fields will
48

-- 48 of 97 --

usually be presented in the form of drop-down lists or radio buttons. In the column Value of the
Enumeration properties the value which will be stored for the different selections is defined. In the
other columns the value displayed in the user interface can be set for each language the model is
set up for.
Alphabetical Sorting
If the property is set to true the list of enumeration values will be sorted by their localized text in
the UI.
Enumeration Values
The declaration of an enumeration field type requires at least two statements for each enumeration
value:
• Value This property defines the value of the selection as it will be stored in the Document
Model. No blanks are allowed here.
• Text (multilingual) This property defines the label of the selection option for the graphical user
interface. This property is multilingual and thus can (and should!) be defined for every
language configured for the model.
• Furthermore, there is one column for each defined Category. The category values can be
written directly in the cell. In addition a Drop-Down Selection is available, which contains all
already inserted enumeration values for this category. For each enumeration value a category
value has to be set. If this is not the case, then an error message will be shown when trying to
apply the changes.
To add new enumeration values, use the ADD button underneath the enumeration value table. This
will add an empty line at the bottom of the table. The position of the entries can be changed via the
arrow buttons in each row.
Figure 38. Gaps in Enumeration Values
When creating enumeration values, it may occur that there are gaps in the labels of values. In the
enumeration value table, such gaps are denoted by the cell value - empty -. All rows that have a
gap in one of their values are annotated with an error icon on the left side. The 'Fill Empty Texts'
49

-- 49 of 97 --

button above the table can be used to auto-fill all gaps with the corresponding enumeration value.
For example, assume that the enumeration value Postal has the English label Postal address and an
empty German label. After pressing the "Fill Empty Texts" button, the German label of this
enumeration value will be set to Postal.
Empty labels for locales are only allowed if none of them are filled. The default behavior at
runtime of A12 is to use the enumeration value for any locale with empty texts.
It is possible to remove entries via the delete button in each row. All entries can be removed at once
with the 'Clear Values' button above the enumeration value table.
Error Message
An error message is displayed when the user selects an invalid enumeration value. Invalid
enumeration values may occur due to customizations by the application or by modifying the value
with the keyboard.
The error message can be defined in the section below the enumeration value table (see Figure 39).
As a standard feature, default error messages are used. Alternatively, the checkbox Use Default
Error Message can be unchecked. Then, custom error messages can be specified for each of the
languages the model supports. The error message property is mandatory and must be entered for
every language defined for the model.
Figure 39. Enumeration Data Type Configuration - Error Messages
Enumeration Categories
Categories for an enumeration can be used to define a grouping for the enumeration values. In a
rule, enumeration keys can be queried by their category. Refer to the Kernel Language
Documentation for detailed information.
Categories can be defined in the table above the enumeration value table. For each category a
unique name has to be provided. Furthermore, an optional description can be set. For each
available category a column will be shown in the enumeration value table. Each enumeration value
requires one value per category.
Add a new category to the table via the ADD button underneath the category table. Delete a
category via the delete button in each row.
50

-- 50 of 97 --

Type Definition Fields
Type Definition fields are applied for reusable custom enumerations and restrictions. Setting up
Type Definitions is described in Type Definitions. The usage of already existing Type Definitions is
shown in Figure 40. If the "Type Definition" checkbox is selected, a drop-down is displayed next to it
and the field "Data Type" becomes read-only. If a selection has been made in the drop-down, the
data type of the field is set to the Type Definition’s data type.
Figure 40. Type Definition Data Type Configuration
The Type Definition is actually not a field type itself. It rather is a reference to a predefined
definition of a field type that can be reused for the field under consideration. For more details refer
to section Type Definitions.
The Field Editor offers the list of Type Definitions that were defined for the current model within
the Type Definition view or introduced via including another Document Model or importing a Type
Definition Model. The name of Type Definitions which have been defined in another model consists
of the origin model name, an underscore and the name of the Type Definition in the original model.
A "TD" icon is displayed next to a field’s name in the Model Tree if it is a Type Definition field.
Figure 41. A Type Definition field in the Model Tree
Groups
The very first element of each Document Model must be a so called top-level group. Without at least
one top-level group no fields can be created.
Groups are used for hierarchically organizing the Document Models; they are mainly used to
51

-- 51 of 97 --

structure the Document Model and combine their child elements (fields, rules, and other groups)
into groups of elements. Every group must have a name and an ID. The ID is auto-generated and
read-only. The name of a group is limited to 60 characters and must be unique among sibling
groups.
Labels, internal and external descriptions, and Annotations can be set in the same way as they can
for Field Editors.
Repeatability and Index Field
The Repeatability property sets the maximum amount of instances that are allowed to be created
for the group. The maximum number of repeatability is 999,999. For repeatable groups the Index
Field can be used to identify a concrete repetition. Such an Index Field is always a mandatory field
and is marked with a # icon in the Model Tree. The two following rules are generated automatically
for it:
• check that the field is mandatory for the group with (GroupFilled(RuleGroup) and
FieldNotFilled(indexField))
• check that the values are unique within the repeatable group RepetitionNotUnique(indexField)
Figure 42. An Index Field in the Model Tree
The definition of an Index Field allows the usage of the Validation features Semantic Index and
Parallel Iteration, refer to the Kernel Language Documentation for detailed information. An
example for setting up the properties of a group can be seen in Figure 43.
When an instance of the model is filled with data, the group together with its child elements may be
repeated as many times as the repeatability property for the group states.
52

-- 52 of 97 --

Figure 43. Configuration of Group Elements
NOTE It is also possible to define an Index Field for a non-repeatable group.
53

-- 53 of 97 --

Validation Rules
Validation Rules are used to define constraints on the Document Model.
The most important property for Validation Rules is the rule condition. This condition puts one or
more fields in relation with each other and can also include constants to impose restrictions for
values of specific fields.
It is very important to notice that conditions specify the case in which the rule is triggered and not
valid behavior. For example:
• There are two fields A and B. Field B should be mandatory when A is filled.
• The correct rule condition would be: FieldFilled(A) and FieldNotFilled(B), because it is an error if
field A is filled and field B is not.
Besides the general information (name and ID), the internal and external description (for each
language in multilingual models) and the possibility to add Annotations, there are some more
properties that can be defined for rules. These are described in the following sections.
The Validation Rule Editor consists of two tabs. In the first tab "Basic Data", information is given
around the Validation Rule. The Validation Rule needs a unique name.
54

-- 54 of 97 --

Figure 44. The Validation Rule Editor, tab Basic Data
In the second tab, "Condition", the Validation Rule properties are defined as well as the Rule
Condition and resulting Error Messages for the end user.
55

-- 55 of 97 --

Figure 45. The Validation Rule Editor, tab Condition
In the following sections, the settings maintained in the Validation Rule Editor will be covered.
56

-- 56 of 97 --

NOTE It is possible to save invalid Validation Rules. This will result in an invalid model,
but gives the modeler the opportunity to work on their rules in steps.
Error Code
The error code property can be used as an error identifier for the application or the user. It is
generated automatically but is editable.
Error Field
The error field is the field that will be used to display the error message if this rule reported an
error during validation. The error field must be referenced in the rule condition.
There is an auto-completion mode available to help select the error field.
Level
There are three severity levels for Validation Rules:
• Error: The severity level Error is used to represent an invalid application/form state. Messages
of this severity level are marked in red and disable saving of or navigation through the
application/form until the error is eliminated.
• Warning: The severity level Warning signals a possibly unwanted application/form state.
Messages of this severity level are marked in orange. Contrary to error messages, warnings do
neither disable saving of nor navigation through the application/form.
• Info: The severity level Info can be used to display additional information to the user if the rule
fires. Messages of this severity level are marked in blue. Similar to warning messages, info
messages do neither disable saving of nor navigation through the application/form.
Condition
The rule condition, as explained above, contains the specification of an incorrect state in an
instance of a Document Model. The condition must be specified in terms of the Kernel Language. By
default, the SME supports the English Kernel Language. Models can also use the German Kernel
Language, but the language must be changed in the corresponding Document Model(s) with an
external tool. The rule condition can also contain calculations, for example there might be an error
if the sum of two field values does not match the value of a third field.
Within this field, an auto-completion is available. This means that by entering CTRL+SPACE or the
letter(s) of a field name or a Kernel Language construct, a list of available proposals will
automatically be displayed. Entering additional letters will further reduce the list of proposals.
Elements within that list can be chosen by selecting them (using either the arrow keys or the
mouse) and then hitting ENTER. The auto-completion mode can be left by hitting the <ESC> key.
If the name of a field is the same as a terminal in the rule language, the Rule Condition fails. As a
solution, you may surround the field name with single quotes (’). This applies for Groups,
CustomConditions or Categories as well. For instance, MinValue is a terminal in the rule language
and if it is used as a field name, the following rule condition is valid:
57

-- 57 of 97 --

FieldFilled(Group/’MinValue’) AND [Group/’MinValue’]==1
NOTE
For computations - as well as for numerical comparisons - multiple numerical
constants can be used, as for example the constants 2, 43 and 999 in the following
condition. 2 * [NumberA] + 43 <= 999
Rule conditions can be defined which will lead to an error if one or more fields within a group are
not set (e.g.FieldNotFilled(<Field>)). If such a condition is defined for a repeatable group, an error
will even occur if a validation is done on an empty group. This situation might result for example
from creating a new data set for a repeatable group (using the Add button), entering no data, and
then trying to save the complete data set that includes the repeatable group.
For more information about this domain specific language refer to the Kernel Language
Documentation.
Additional Operator Information
When hovering with the mouse over an operator (for example FieldFilled) additional information
about the operator is displayed. It also includes a link to the documentation of the operator.
58

-- 58 of 97 --

Figure 46. Additional Information is Displayed When Hovering Over an Operator
Additional Element Information
When hovering with the mouse over an element that is currently used in the Rule Condition editor,
additional information about the element is displayed.
59

-- 59 of 97 --

Figure 47. Additional Element Information is Displayed When Hovering Over an Element
Additional Parser Error Information
The rule condition is validated by the rule parser in the background, which may result in a reported
error. Hovering the mouse over a highlighted error in a rule condition will display additional
information about the error from the rule parser. It also includes a link to the rule parser error
documentation.
60

-- 60 of 97 --

Figure 48. Additional Information is Displayed When Hovering Over a Problem
Allow Differing Decimal Places
Clicking the button will add the text @SuppressWarning(MVK_INVALID_COMPARE_DEC_PLACES) at the
beginning of the condition text.
This allows the comparison of numbers with an unequal number of decimal places (fractional
digits) within the rule condition statement.
By default this is flagged as an error, since the comparison of numbers and the assignment of other
number field values should be restricted to only numbers with equal amount of decimal places and
would lead to an error otherwise. In certain situations, this may be undesirable and ticking the box
will suppress the error.
The suppression text must be deleted to return to the default behavior.
61

-- 61 of 97 --

Format
A standard formatting of the Rule Condition is available via the button "FORMAT". The editor will
introduce line breaks and spaces to increase readability of the Rule Condition. While formatting a
Rule Condition text, following changes are done:
• Unnecessary spaces are removed (for example double spaces).
• Missing spaces are added (for example, a space is always inserted after a comma in a parameter
list).
• Indentation is normalized.
• Syntax of And and Or is capitalized.
• Unnecessary brackets are removed.
• Leading zeroes in numbers are removed.
• AtLeastOneFieldFilled and NoFieldFilled are replaced by FieldFilled and FieldNotFilled
respectively, if only one parameter without star is defined.
• AtLeastOneGroupFilled and NoGroupFilled are replaced by GroupFilled and GroupNotFilled
respectively, if only one parameter without star is defined.
• If the rule group (which can also be a root group) is referenced with its absolute path, it will be
replaced during formatting by the language construct RuleGroup (only if no star was specified).
Maximize Rule Condition Editor
Via the "maximize" button above the Rule Condition Editor, the editor is maximized to take up the
entire space of the SME. Leave the maximized Rule Condition Editor again via the "X" button.
Error Message
The error message property is used to define the content of the messages that will be displayed if
the rule applies. It is mandatory to define the message text for all languages for which the model
has been set up. There are some restrictions that the error message has to meet:
• The error message must not contain any line breaks.
• If you need to use the Dollar-sign, you have to put $$ into the error message.
• The text of the error message may only contain characters from the ISO-8859-15 character set.
References to fields that are part of the rule condition can be used within error messages. There is
an autocomplete feature available to facilitate referencing fields that becomes available after
typing $. Some aspects have to be kept in mind:
• References to field labels can be defined by embracing the field name with Dollar signs. For
example $MyFieldName$ would display its localized field label.
• A reference to the current value of a field can be defined by concatenating the field name with
.value and putting the resulting string between Dollar signs. For example $MyFieldName.value$
would display the value of the field MyFieldName.
• Only fields that are part of the condition may be referenced and the fields have to be referenced
62

-- 62 of 97 --

exactly like in the condition.
A reference to a specific repetition within a repeatable group is also possible using the
$#<GroupName>$-syntax. For example if you want to check the uniqueness of the two fields
MyStringRep and MyNumberRep across multiple repetitions of the group MyRepeatableGroup, you
could reference its instances in the error message of a rule with the following text for the error
message:
The combination of $MyRepeatableGroup/MyStringRep$ and
$MyRepeatableGroup/MyNumberRep$ has to be unique (iteration nr. $#MyRepeatableGroup$)
The resulting error messages shown when entering non-unique data can be seen here.
Figure 49. Validation Error Messages Displayed After Entering Non-Unique Data
NOTE
In case the text of the error message takes more space than the space provided by
the width of the column in which the message will be displayed, automatic word
wrapping will take place, i.e. the error message will be split across multiple lines.
However, if there is a single word part of the error message which takes more space
than provided by the corresponding column, there will be no line break within that
word and thus the word will reach into the next column. Especially if there is also
an error message displayed in that adjacent column making use of the very same
line, the error messages will overlap and will be hardly readable. To avoid that
misbehavior, line breaks should be added (using the dash character ’-’) to the long
words manually, like e.g. in ThisIsAVery-LongWord.
63

-- 63 of 97 --

Computation Rules
Computation Rules are used to check and display the result of computations.
The Computation Rule Editor consists of two tabs. In the first tab "Basic Data", information is given
around the Computation Rule.
Figure 50. The Computation Rule Editor, Tab Basic Data
In the second tab, "Computation", the computed field is set as well as the sets of precondition and
calculation.
64

-- 64 of 97 --

Figure 51. The Computation Rule Editor, Tab Condition
NOTE It is possible to save invalid Computation Rules. This will result in an invalid model,
65

-- 65 of 97 --

but gives the modeler the opportunity to work on their rules in steps.
In the two tabs of the Computation Rule Editor, the following settings can be done:
• Name: The name of the computation which will be part of the Document Model.
• ID (read-only): An automatically provided unique identifier for the computation.
• Internal and External Description: Those are general elements which are described in
Descriptions.
• Computed Field: This defines the field that will be used to hold the results of the computation.
If the computed field is repeatable the precondition must be filled and it must ensure that an
instance of the parent group of the computed field exists.
• Allow differing decimal places: Checking this allows the comparison of numbers with an
unequal number of decimal places (fractional digits) within the precondition and calculation
statements or returning as computation result the value of a number field with fewer decimal
places than the computed field specifies.
By default this is flagged as an error, since the comparison of numbers and the assignment of
other number field values should be restricted to only numbers with equal amount of decimal
places and would lead to an error otherwise. This might be undesired in some cases and
checking the box will suppress the error.
• Common Precondition: Computations have a list of alternatives, where each alternative is a
pair of a precondition and a calculation (see below). The common precondition box can be used
to specify preconditions that must be satisfied in each alternative of a computation. In order for
the user to be able to edit the common precondition of a newly created rule, they have to select
the corresponding "showCommonPrecondition" option in the editor.
• Precondition: Here, preconditions for the computations can be defined. There are several
restrictions on them which are described in the Kernel Language Documentation.
• Calculation: Here, the calculation itself can be defined which is applied if the preconditions are
satisfied.
NOTE
Add pairs of "Precondition" and "Calculation" via the ADD button below the table. In
case of multiple preconditions, a very important constraint is that those
preconditions have to be mutually exclusive.
Edit "Precondition" and "Calculation" by clicking the row or using the "Edit" button.
To delete precondition/calculation pairs, delete the rows from the table. Pairs can
also be copied and reordered via the respective row actions.
• Format: A standard formatting of the precondition and calculation is available via the button
"FORMAT". The editor will introduce line breaks and spaces to increase readability of the
conditions (refer to Format for further information).
• Maximize precondition and calculation editors: Via the "maximize" button above the
precondition and calculation editor, the editor is maximized to take up the entire space of the
66

-- 66 of 97 --

SME. Leave the maximized editor again via the "X" button.
Once a newly created computation has been saved, the icon of the computed field changes to
indicate that this field is now a computed field (see a comparison in Figure 52).
Figure 52. Highlighting of Computations in the Model Grid
Rule based computations can be moved within the group hierarchy by making use of the drag-and-
drop mechanism. Immediately after moving a computation, it is likely that error messages will
appear because the references to the fields used in the precondition and the calculation itself might
be no longer valid. In that case the field references within the computation have to be corrected
using the Computation Rule Editor.
The details for the usage of computations are described in the Kernel Language Documentation.
Additional Element Information
When hovering with the mouse over an element that is currently used in the Precondition or
Calculation, additional information about the element is displayed.
Figure 53. Additional Element Information is Displayed When Hovering Over an Element
Attachment
Attachment elements are used to attach files to a document via file upload to a server.
67

-- 67 of 97 --

Figure 54. The Attachment Editor
The attachment element is added to the Document Model as a group containing eight read-only
fields and four read-only rules by default (see Figure 55). Elements belonging to the attachment are
highlighted with a light pink background.
Fields:
• original_filename: Field of data type string (optional)
• internal_filename: Field of data type string (indexed, required)
• content: Field of data type string (required if attachment_id is empty)
• attachment_id: Field of data type string (required if content is empty)
• size: Field of data type number (required if content is filled)
• mime_type: Field of data type string (required)
• category: Field of data type string (optional)
• description: Field of data type string (optional)
Rules:
• AttachmentInternalFilenameRequired: Rule to flag internal_filename as required.
• AttachmentMimeTypeRequired: Rule to flag mime_type as required.
• AttachmentIdOrContentFilled: Rule to flag either attachment_id or content as required.
• SizeOfContentFilled: Rule to flag size as required if content is filled.
68

-- 68 of 97 --

Figure 55. The Attachment Group and its Elements
NOTE Multiple attachments for the same model attribute are not supported. Therefore,
only single-select and single-upload of files is enabled.
Multi-Select
Multi-Select elements are used to select multiple enumeration values.
69

-- 69 of 97 --

Figure 56. The Multi-Select Editor
The multi-select element is added to the Document Model as a group containing a single partially
read-only field (see Figure 57). The repeatability of this group is automatically set to 999,999 and
cannot be configured. The field type may be set to Enumeration or String (for external
enumerations).
Figure 57. The Multi-Select Group and its Element
This field’s name is set as value , but can be altered afterwards, and contains the selectable
enumeration value. Here you can select whether to use an enumeration type or a Type Definition of
type enumeration. In addition it is possible to set the Data Type to "String" for configuring an
external enumeration.
NOTE For consistency reasons it is not possible to use imported Type Definitions or Type
Definitions from includes.
The dialog for editing the multi-select values is shown in Figure 58.
70

-- 70 of 97 --

Figure 58. Editing the 'Value' Enumeration of a Multi-Select
A Multi-Select element is technically implemented as a repeatable group with one field. As a
consequence, it is possible to apply the same Validation Rules as for usual repeatable groups and
fields.
71

-- 71 of 97 --

The Multi-Select group and field can also be used in the precondition of a computation. One
exception is that the Multi-Select field cannot be a computed field itself.
Includes
Include elements are used to insert other Document Models at a specific location of a model,
potentially multiple times. The dialog for adding includes to a Document Model is shown in Figure
59.
72

-- 72 of 97 --

Figure 59. The Add Include Dialog
73

-- 73 of 97 --

This allows a composition of models with emphasis on modularity. Using includes also avoids
copying reusable parts of models.
Included models can be altered and all dependent models will automatically be updated with the
changes of the included model during the next generation.
NOTE
An included model must contain at least the same languages as the main model,
otherwise an error will be thrown. However, it is possible that the included model
contains additional languages which are not used by the main model.
Include elements are read-only. They are highlighted with a light grey background in the Model
Tree. It is possible to add an Annotation to an include.
Figure 60. Display of an Include in the Model Tree
Document Model
Select a Document Model from the Workspace to include. When a Document Model is selected
which cannot be included, an error message will be displayed. Document Models with the following
properties cannot be included:
• they contain more than one root group
• the repeatability of the root group is > 1
• they do not contain all locales the currently edited model contains
• they include (nestedly) the currently edited model (no circular references)
• they have a differing Type Definition mode from the currently edited model
74

-- 74 of 97 --

Include Repeatability
This property defines the repeatability of the group containing the content of the included model.
Exclude Rules
By checking these properties, the inclusion of validation and calculation rules can be skipped. Rules
of the selected type in the included model (and its potential submodels) will not be included into
the current model.
Include Name
This is the name of the group that represents the included model. The content of the included
model’s single top-level group will be expanded into this group. Thus, the name of the top-level
group of an included model has no effect on the including model. It is overridden by the include
name.
If rules or computations in the included model use paths through the top level group of the
included model, the name of the include group in the including model must match the name of the
top level group of the included model. For convenience, the Document Model Editor provides this
information as a default name, which can be overwritten.
For example if a rule in the included model uses a path "..topLevelGroupName/fieldName" but the
include name in the including model is "includeName" instead of "topLevelGroupName" this would
lead to a consistency error in the including model.
See the Kernel Language Documentation for more information about the ".."-notation.
Copying Includes
The "Copy" action copies the reference to the included Document Model. To achieve that the content
of another Document Model is available in an editable way, use the action Insert from DM.
Type Definitions
Type definitions can be used to create reusable field types: The values and restrictions defined in
the data type configuration, which can get quite complex, can be reused at different places when
they are defined as a Type Definition.
Examples:
• A Type Definition for lists of genders or countries (based on Enumerations with corresponding
values)
• A Type Definition for Email address input fields (based on Strings, restricted by regular
expressions)
Type definitions are always based on one of the standard field types (string, number, enumeration
etc.) and are mainly used for the following reasons:
75

-- 75 of 97 --

• They are reusable in multiple fields which is particularly useful when dealing with
enumerations.
• In addition they can be used to refine the corresponding standard type for example by
restricting the length or pattern of the field value.
There are two different ways to introduce Type Definitions into a Document Model: As local and
included Type Definitions or imported Type Definitions.
NOTE It is not possible to mix local/included and imported Type Definitions in one model.
Local Type Definitions
Local Type Definitions are maintained in the model directly. If an included model contains local
Type Definitions, they will also become available as included Type Definitions.
Type definitions can be added, edited, and viewed in a separate menu accessible via the sidebar
(see Figure 2 and Type Definitions). Use the "ADD" button in the table header to add a new local
Type Definition. Type definitions from included models are displayed with a grey background
color. The column "Source" either displays "local" if the Type Definition has been maintained
directly in this model, or "included", followed by the name of the model the Type Definition was
defined in as well as the path and name of the include, for included Type Definitions.
As soon as a local Type Definition is present (directly or via an include), the button to import a Type
Definition Model becomes disabled.
Figure 61. Local Type Definitions
Type definitions are composed of a name, a data type and its corresponding data type
configuration. The data type configuration is identical to the configuration that can be done when
choosing the field type for a specific field.
76

-- 76 of 97 --

Figure 62. The Type Definition Editor
After creating a Type Definition, this new type can be chosen when selecting the type of a specific
field of the model (see Type Definition Fields).
77

-- 77 of 97 --

A local Type Definition can be deleted via the delete button in its row.
Included Type Definitions are read-only and cannot be deleted. They will no longer be available if
the origin model is no longer included.
NOTE
There is no check if a Type Definition is used in a field definition before deleting it
and thus no confirmation dialogue. The field definition becomes invalid if a Type
Definition in use is removed.
Imported Type Definitions
Type Definition Model
It is possible to create a Document Model which only contains Type Definitions, but no groups,
fields and rules: A Type Definition Model. This offers a way to maintain all Type Definitions that are
needed in the different Document Models of a project in a central place. It is possible to import
Type Definition Models in a Document Model, which makes the Type Definitions available to use.
To add a Type Definition Model, use the "ADD" button in the header of the Workspace Explorer and
select "Type Definition Model".
78

-- 78 of 97 --

Figure 63. Create a new Type Definition Model
After entering the basic model settings in a modal, the Type Definition Model Editor is opened. This
is a variation of the Document Model Editor with the difference that there is no Model Tree to add
groups, fields and rules, but only the possibility to add Type Definitions.
Figure 64. Type Definition Model Editor
Type definitions can be added directly to the model like it is described in Local Type Definitions. It
is also possible to import another Type Definition Model. Type definitions which originate in a Type
Definition Model that has been imported into another Type Definition Model will become available
79

-- 79 of 97 --

in a Document Model when importing that other Type Definition Model.
Import Type Definition Model
To make Type Definitions from a Type Definition Model available, the Type Definition Model has to
be imported into the Document Model. To import a Type Definition Model, use the "IMPORT" button
in the header of the Type Definitions table. Select the Type Definition Model that should be
imported from the drop-down. As soon as a Type Definition Model has been imported, the button to
add local Type Definitions is disabled.
Imported Type Definitions are displayed in the Type Definitions table. The column source mentions
"imported", followed by the name of the Type Definition Model the Type Definition originates.
Figure 65. Imported Type Definitions
If an included model uses imported Type Definitions, they will appear in the Type Definitions table
as well. The source then mentions "included imported" and the Type Definitions are displayed with
a gray background.
NOTE
Included imported Type Definitions are not available to use in the currently edited
Document Model. To make use of imported Type Definitions, the Type Definition
Model has to be imported explicitly. The included imported Type Definitions are
merely displayed to explain their content since they can be used in the included
Document Model.
To remove the import of a Type Definition, use the "REMOVE IMPORT" button in the subheader of
the Type Definition table. Select the model from the drop-down which you would like to remove.
NOTE
There is no check if a Type Definition is used in a field definition before removing
the import and thus no confirmation dialogue. The field definition becomes invalid
if a Type Definition Model in use is removed. Importing the Type Definition Model
again results in a valid field definition again.
Rule Contradictions
While each rule and computation is checked for validity during editing it is possible that multiple
rules contradict each other. For instance, a rule may require that a certain field is filled and another
rule may prohibit that it is filled. To check whether a model has such contradictions, the model
80

-- 80 of 97 --

editor provides the option to check for rule contradictions within the Rule Contradictions tab.
Figure 66. Viewing Rule Contradictions
When selecting the tab for the first time the report for Rule Contradictions is empty. As calculating
rule contradictions might take a while for models with many rules and fields, rule contradictions
are not calculated when changing the model. Instead, the calculation is explicitly initiated by the
modeler.
To create a new rule contradiction report the "GENERATE" button in the lower right corner is used.
A modal will then be displayed that requests a value for "today". This value is preset with the
current day but can also be set to an arbitrary day to test rules that restrict only at certain time
periods.
Figure 67. Modal to Generate Rule Contradiction Report
The "CANCEL" button just returns to the previously displayed report view while pressing the
"CONTINUE" button calculates a new report based on the given today value. In case no
contradictions are found, the report is empty, otherwise an overview over current rule
contradictions is shown and the number of problems is displayed in an orange circle on the tab:
81

-- 81 of 97 --

Figure 68. Viewing Rule Contradictions
In this example two fields are reported to be unfillable. A detail view can be opened by clicking on
one of the entries in the overview. A detail view provides more insight in which rules and fields are
involved in the contradictions:
82

-- 82 of 97 --

Figure 69. Details for a Rule Contradiction
It is possible to return to the overview using the "RETURN" button.
To solve contradictions, the involved rules need to be adapted and the report must be generated
anew as calculating the contradictions report is initiated by the modeler only. This allows the
modeler to view the old report again as well as selecting a today value before generating the report.
In case the model has changed since the report was created, a warning icon is shown on the tab.
When hovering this icon the text states that the report might be outdated.
Figure 70. Warning Icon is Shown if the Report is Outdated
Models with rule contradictions are not considered invalid. Thus, a model with contradictions can
be saved normally and can even be deployed.
It is possible that rule contradictions cannot be calculated. This is the case for invalid models and in
83

-- 83 of 97 --

rare cases if the model contains elements that are not yet supported by the calculation. In both
cases a warning notification is shown.
Figure 71. Warning if Rule Contradictions Cannot be Calculated as the Model is Invalid
Figure 72. Warning if Rule Contradictions Cannot be Calculated Due to Not Supported Elements
NOTE Rule Contradictions are currently not supported on MacOS. Generating the report
will always state that unsupported elements were used.
Composed Document Model Editor
The Composed Document Model Editor is a special variant of the Document Model Editor. It is used
to create and edit Composed Document Models (CDMs). CDMs are Document Models with the
additional option to establish links to other Document Models via Relationship Models in their
Model Tree.
Open the CDM Editor
To add a CDM, use the "ADD" button in the header of the Workspace Explorer and select "Composed
Document Model".
84

-- 84 of 97 --

Figure 73. Create a New Composed Document Model
After entering the basic model settings in a modal, the CDM Editor is opened that contains the
Model Tree on the left side. However, in contrast to the Document Model Editor, the Element Picker
is displayed on the right-hand side.
The Element Picker is used to select Document Models and Relationships that should be included in
the CDM. It consists of two tabs, the Diagram and the Tree Tab.
Initially, the Diagram Tab is open. If only one diagram exists in the Workspace, it is selected
automatically. If there is more than one diagram, the one suitable for the CDM creation can be
selected from the drop-down menu. In the Diagram Tab, Document Model and Relationship
elements can be added to the Model Tree using diagrams that have been previously created in the
Workspace.
85

-- 85 of 97 --

Figure 74. New Composed Document Model
In the Tree Tab, Document Model and Relationship elements can be added to the Model Tree using
a list of all possible Document Models and Relationships from the Workspace.
Figure 75. Element Picker Tree Tab
Add Elements to the CDM
Regular Document Model elements are added in the same way as in the Document Model Editor.
CDM-specific elements are added by selecting them from the Element Picker.
Add a Root Document Model
One of the root elements of a CDM must be a Document Model element (see Document Model
Element). In the Element Picker’s Diagram Tab, the Document Models that can be selected as the
root element are marked in blue. A click on a selectable node will add the corresponding element to
the Model Tree.
In the Tree Tab, the Document Models that qualify as root elements are shown. A click on a
Document Model adds it to the CDM.
The name of the added element is the name of the Document Model’s root Group. The new element
is automatically selected in the tree and marked in the Element Picker.
86

-- 86 of 97 --

Figure 76. CDM Editor After Selection of the Root Node
The Root Document Model can also be selected during creation of a new CDM within the add dialog.
Add a Relationship
The Document Model element that is selected in the Model Tree is also marked in the Element
Picker Diagram Tab if the opened Model Graph Diagram contains the Document Model. To connect
the selected Document Model with another Document Model, click on the respective Relationship
that connects them.
The Element Picker’s Tree Tab shows all possible Relationships from the Workspace that can be
connected to the selected Document Model element. A click on a Relationship adds it to the CDM.
Figure 77. The Element Picker Tree Tab Shows All Possible Relationships
The respective Relationship element (see Relationship Element) and the connected Document
Model element are added to the Model Tree. The Relationship element is added as a sibling node
and the connected Document Model element as a child node of the Relationship element. The newly
added Document Model element is automatically selected in the Model Tree and marked in the
Model Graph Diagram.
87

-- 87 of 97 --

Figure 78. CDM Editor After Adding a Connected Document Model Via a Relationship
Add a Relationship Including a Link Document Model
Adding a Relationship that has a Link Document Model works the same way as described before for
a Relationship without a Link Document Model. In the Model Tree, the Link Document Model
element (see Link Document Model Element) is added via a special Link Relationship element (see
Link Relationship element) with name "relationship" below the connected Document Model
element.
Figure 79. CDM Editor After Adding a Relationship With a Link Document
Add a Self-Referencing Relationship
When a self-referencing Relationship is selected, the user is asked to select the target role. This is
the role of the Relationship that is assigned to the Document Model element that will be added to
the Model Tree as a child of the Relationship element.
88

-- 88 of 97 --

Figure 80. Role Selection for Adding a Self-Referencing Relationship
The name of the self-referencing Relationship element in the Model Tree is the name of the
Relationship and the role name, separated by an underscore.
Figure 81. CDM Editor After Adding a Self-Referencing Relationship
Selectable Document Models
Any Document Model that has a single Group as the only root element is selectable. The user must
take care to only add 'includable' Document Models to the tree (refer to Document Model).
Selectable Relationships
A Relationship is not selectable from a Document Model if:
• The Relationship Model is invalid and only contains one Document Model entity
• The target Document Model does not fulfill the 'selectable' requirements (see Selectable
Document Models)
89

-- 89 of 97 --

• The currently selected Document Model has the same parent Relationship with the source role’s
upper limit multiplicity of 1
• The currently selected Document Model already has the same Relationship as a child
Relationship (sibling in the tree)
For self-referencing Relationships, the following rules apply:
• If the currently selected Document Model has the same parent Relationship with the source
role’s upper limit multiplicity of 1, only the same role is selectable as source role.
• If the currently selected Document Model has the same Relationship as a child Relationship
(sibling in the tree), the child Relationship’s target role is not selectable as target role.
Switching Between Document Model Elements
It is possible to freely select Document Model elements inside the Model Tree. Clicking on a
Document Model element will select it and mark the respective node in the Model Graph Diagram
as well as the selectable adjacent Relationships.
Switching Between Different Model Graph Diagrams
The Model Graph Diagram in the Element Picker can be changed to another Model Graph Diagram
of the Workspace using the drop-down menu at the top of the Element Picker. The selected
Document Model from the Model Tree and the selectable adjacent Relationships will be
dynamically marked in the selected Model Graph Diagram.
CDM-Specific Model Tree Elements
There are four CDM-specific Model Tree elements: Relationship element, Document Model element,
Link Relationship element, Link Document Model element. The root of a CDM is always a Document
Model element and all other CDM-specific Model Tree elements are connected to it via a chain of
Relationship elements.
Document Model Element
This element represents a Document Model. Clicking on it will select it in the tree and
simultaneously mark it in the Model Graph Diagram. Its context menu allows to add other elements
and to open its Details Editor. It cannot be deleted directly but only by deleting the parent
Relationship element. The root Document Model can only be deleted by selecting the 'Delete all
elements' option from the context menu of the first default row in the Model Tree called 'Model
Tree'.
90

-- 90 of 97 --

Figure 82. Context Menu of Document Model Element
Link Document Model Element
This element represents a Document Model that is used as a Link Document Model. It is always a
child node of a Link Relationship Element (see Link Relationship element). Clicking on it will have
no effect. Its context menu allows to open its Details Editor. It cannot be deleted directly but only by
deleting the parent Link Relationship element.
Figure 83. Context Menu of a Link Document Model Element
Relationship Element
This element represents a Relationship Model. It connects two Document Model elements. Its
91

-- 91 of 97 --

Repetitions value is the maximum multiplicity of its target Document Model element and is
automatically inferred from the corresponding Relationship Model on creation.
If it is unbounded, a value of 999,999,999 is used. However, using an unbounded multiplicity is not
recommended in a CDM context for security reasons.
The repetitions and the multiplicity have to match, with one exception: For a higher multiplicity the
repetitions can also be set to 1. This is allowed for the use case of having a CDM child activity, in
which information of one concrete related parent entity of an 'n to m' relationship needs to be
displayed.
Clicking on the element will have no effect. Its context menu allows to add elements, to open the
Details Editor and to delete it.
Figure 84. Context Menu of a Relationship Element
Link Relationship element
This element is a helper element and purely used to model the connection between a Relationship
element and a Link Document Model element. Clicking on it has no effect. Its context menu allows
adding elements, opening the Details Editor, and deleting it.
92

-- 92 of 97 --

Figure 85. Context Menu of a Link Relationship Element
Annotations
Composed Document Models need an Annotation to identify as such and separate it from a regular
Document Model. This Annotation is set in the Settings tab. The required Annotation has the name
'cdm.queryRoot' and its value is the name of the root Document Model. This Annotation is set
automatically when the root Document Model is selected and must not be changed manually.
93

-- 93 of 97 --

Figure 86. Automatically Set Annotation 'cdm.queryRoot'
Additionally, the Relationship elements require some Annotations that are managed automatically
as well:
• 'cdm.relationship': name of the Relationship Model
• 'cdm.sourceRole': role of the source (parent) Document Model
• 'cdm.targetRole': role of the target (child) Document Model
• 'cdm.targetDocumentModel': role of the target Document Model
These Annotations must not be changed manually.
94

-- 94 of 97 --

Figure 87. Annotations for a Relationship Element A_B
Document Model Element Selection from Diagram
When a Document Model Node in the Diagram is clicked, the corresponding Document Model
Element is automatically selected in the Model Tree. If there is more than one tree element of the
clicked Document Model, a modal is opened from which the path to the desired tree element can be
selected.
Figure 88. Tree element selection modal after clicking on the diagram node "A"
Refactoring
The Simple Model Editor supports automatic refactoring for Document Models. When you rename,
move, or delete elements that are referenced elsewhere, the tool identifies affected references and
presents options for updating them.
95

-- 95 of 97 --

For detailed information about refactoring capabilities, workflow, and actions, refer to the
Refactoring Support section in the SME Tool Documentation.
Document Model Specific Refactoring
Document Model refactoring handles references within and between Document Models. The
following elements and reference types are supported:
Supported Operations
Document Model refactoring supports the following operations:
• Renaming fields, groups, or includes
• Moving fields or groups to a different parent group
• Deleting fields, groups, includes, or type definitions
Reference Types
The following reference types are tracked and updated during Document Model refactoring:
Reference Type Description
Rule Error Conditions Conditions that reference fields, for example [field1] == "".
Rule Error Messages Localized error messages that mention field names.
Computation
Preconditions
Common precondition that reference fields.
Computation
Operations
Calculations that reference fields.
Index Field References Group index field assignments.
Type Definition
References
References to type definitions used by fields.
Example: Renaming a Field
Consider a Document Model with:
• A field named orderDate
• A rule with error condition FieldNotFilled(orderDate)
• A rule with error message "Please enter $orderDate"
When you rename orderDate to purchaseDate:
1. Save the Document Model.
2. The refactoring dialog displays the affected references:
◦ Error condition: FieldNotFilled(orderDate) changes to FieldNotFilled(purchaseDate)
96

-- 96 of 97 --

◦ Error message: "Please enter $orderDate" changes to "Please enter $purchaseDate"
3. Select Commit for each reference to apply the updates.
4. The model saves with all references updated.
Example: Moving a Field
Consider a Document Model with:
• A field named field1 at the top level
• A computation referencing field1
When you move field1 into a group named otherGroup:
1. Save the Document Model.
2. The refactoring dialog displays the affected computation reference.
3. The proposed update changes the path from field1 to otherGroup/field1.
4. Select Commit to apply the update, or Edit to select a different target.
5. The model saves with your selected changes applied.
For information about enabling or disabling Document Model refactoring, see Enabling Within-
Model Refactoring in the SME Tool Documentation.
97

-- 97 of 97 --

