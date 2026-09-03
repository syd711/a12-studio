# print_engine print modeling documentation

Print Modeling
Table of Contents
Introduction and Concepts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
Installation. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
Migrating Existing Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Print Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Concepts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Positioning & Layout. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Example. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Templates and Settings. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
Dynamic Content . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
Preview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
Committing Changes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
Print Model Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
Overview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
Editor Header . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
Sidebar . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Tab. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Editor Stage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
Stage Mode (Default) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
Layout Mode. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
General Tab . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
PDF Metadata . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
Schema Tab . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
Text Styles Tab . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
Text Style Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
Typography Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 18
Available Default Fonts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 18
Overwrite Default Fonts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 18
Font Support for DIN 91379 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
Custom Fonts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
Segment Tab . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
Print Model References (Templates) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
Create Template. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
Use Template . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
Section Tab . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
Watermark Tab. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
Commit Changes Tab. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 25
1

-- 1 of 83 --

Preview. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
Preview Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
Validation. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
Editors for Model Elements . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
Common Properties of Print Model Elements. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
Detail View . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
Input Source . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
Text Properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 31
Border Properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
Hide Conditions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 33
Example. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 34
Computations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Conditions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Computation Alternatives. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Referencing Document Model Data . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Document Model Metadata Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Annotation Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Listing-specific Computation Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Page Break Behavior. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Default Input Source for New Elements . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 38
Text . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 39
Field . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
Calculation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Page Number & Page Number Total . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
Line . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
Table . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
Columns . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 48
Image . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
Static . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
Picking an existing resource . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
Uploading a new image . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
Dynamic. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 51
Expression . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 52
Listing . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 53
Listing Columns . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 56
Default Computations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 56
Group Computations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 58
Field Computations. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 58
Value Computations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
Additional Computation Fields . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
Property Computation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
2

-- 2 of 83 --

This documentation is intended for a business analyst audience.
Introduction and Concepts
The Print Model Editor is part of the Simple Model Editor and enables domain experts and analysts
Group Property Computation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 61
Hide Group Example . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 61
Attachments . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
Rendering of Attachments . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
Table Layout . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 64
Table Layout Cells . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 65
Charts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 66
Line Chart . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 67
Series Data . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 68
Bar Chart . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 69
Categorized Data . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 70
Pie Chart . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 71
Bounding Box . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
Bounding Box Stage View . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
Area . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 73
Area Stage View . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 74
Overflow Height . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 75
Switch . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 75
Switch Cases . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 76
print-shell . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 77
Prerequisites . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 77
Start print-shell. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 77
Comparison of Two PDF-Documents. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 77
Comparison of All PDF-Documents With the Same Name. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 78
Printing a Specific Print Model. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 78
Printing All Print Models in the Current Workspace. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 78
Attachment File Resolution. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 78
Typesetting. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 79
Typesetting Model Configuration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 79
Prevent Line Break Rule Settings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 80
Character Sequence Rules. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 80
Number Unit Rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 81
Special Pattern Rules . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 81
Orphan and Widow Settings. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 82
Roles . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 83
3

-- 3 of 83 --

to create and modify Print Models. These models are designed to function as templates for
generating PDF-documents from A12 Platform data.
Print Models describe the layout, styling and content of the resulting PDF-documents. The Print
Model Editor differentiates itself from a common text editor by its design around the insertion of
dynamic content from the A12 Platform. Print Models can facilitate simple data insertions as well as
complex dynamic calculations based on the inserted data. Examples range from in-line text
insertions and tables, to element repetition, rule-based element hiding and listings with completely
customizable content and styles.
[universal access] Accessibility Hint The Print Engine will generate PDF documents with
tagged elements for accessibility software like screen readers, which are aligned with the
standards PDF/UA and PDF/A-3a.
However, to ensure that the resulting PDF document is fully compliant with these standards,
the modeller must follow the accessibility hints provided in this documentation. These are
mandatory for compliance; if they are not implemented, the PDF will not meet PDF/UA or
PDF/A‑3a.
Figure 1. The Print Model Editor
Installation
The Print Model Editor is part of the Simple Model Editor. For advice on the installation of the
Simple Model Editor, refer to the "Quick Start Guide" on getA12 under "Modeling".
NOTE To create or edit a Print Model, use the Simple Model Editor (SME) version 11.5.0 or
higher, which is provided with the A12 version 2024.06-ext4 or higher.
4

-- 4 of 83 --

Migrating Existing Models
It might be necessary to migrate existing Print Models in case a new version of the Simple Model
Editor and thus Print Model Editor has been installed.
In most cases, the Simple Model Editor can handle updating versions itself. For more information,
refer to the documentation of the Simple Model Editor.
NOTE
Since Print Model version 2.1.0, the SME handles the migration automatically. If a
workspace in the SME contains old models that need to be migrated, an error will
appear with prompting messages. Click the Resolve All Issues button to
automatically update them.
NOTE Print Model files older than version 2.1.0 must first be migrated using the Java-
based print-shell tool. Follow the print-shell migration instructions.
WARNING
When migrating a Print Model to a new version in the SME, any uncommitted
changes stored in .wal files may become invalid after the migration. Please
commit all pending changes before opening and migrating the model in a
newer SME version to avoid data loss.
Print Model
A Print Model functions as a template for PDF-representations of inserted Documents.
The resulting PDF-document structure is defined by the Segments modeled in the Segment Tab.
The PDF can be further modified by modeling Sections and Watermarks in the Section Tab and the
Watermark Tab.
Text Styles defined in the Text Styles Tab will also affect the final look and feel of the PDF-
document.
When modeling Segments, Sections and Watermarks, Print Model Elements are used to define the
content of the PDF-document. These Elements are added to the Editor Stage and positioned in the
millimeter grid. The individual settings of each element can be modeled as described in Editors for
Model Elements.
[universal access] Accessibility Hint
Please be aware that it is currently not possible to manually define the reading order of
elements for screen readers. However, if you need this ability in your project, please reach out
to the Print Engine team, so that they can consider it for future updates.
Print Model elements can utilize references to data from Document Models, allowing dynamic
content to be displayed in the PDF. For this purpose, you can reference a Document Model in the
Schema Tab.
5

-- 5 of 83 --

Concepts
Positioning & Layout
Print Model Elements are positioned on a millimeter grid. Each element has a user defined width
and an initial height based on its pre-determined content. If an element contains dynamic content,
its height can change, which means the content below the element might be pushed downwards.
NOTE
The Print Model Editor millimeter grid might not have correct measures when
measured on your display. The display measures are dependent on the type of your
display and do not affect the resulting PDF-document.
Based on their relative positions, elements define a so-called bottom margin. A value that defines
the area below an element, that will be kept empty below the element when it comes to
repositioning and element collisions. In some cases also a top margin is necessary.
Example
In the following figures a scenario is displayed, where both margins are necessary to keep the
expected layout stable during the insertion of dynamic content. If the user places the elements A, B,
C and D like in the left figure, the margins displayed in the right figure are created automatically by
the editor.
Each element has a bottom margin that keeps the empty space between the elements stable. A top
margin for element B is created, because element A's bottom margin only extends to element C.
This leaves the empty area between element A and element B partially uncovered. Therefore, a top
margin is created for element B, which covers this area.
6

-- 6 of 83 --

Figure 2. Margin layout example without displayed
margins.
Figure 3. Margin layout example with displayed
margins.
Templates and Settings
You can use Print Model References as Templates in other Print Models. This allows you to define
regions with a set height and static layout, enabling the creation of Print Models that conform to
various standards (e.g. DIN standards).
The available Fonts are provided as workspace resources and can be selected in the Print Model
when configuring Text Styles.
Dynamic Content
Print Model elements can utilize references to data from Document Models, allowing dynamic
content to be displayed in the PDF. For this purpose, you can reference a Document Model in your
Print Model. This allows you to use data from Documents based on this Document Model to add
dynamic content in a variety of different ways:
• Field References
• Computations
• Expressions
The Print Model Elements such as Tables and Listings can be used to display repeatable data. When
rendering the Preview, the repeatable data will cause rows to be automatically created based on the
modeling settings that have been made.
7

-- 7 of 83 --

Preview
The Preview may be used to generate a PDF preview of your model. If dynamic content has been
referenced in the model, you may select a Document to the Preview which will then be used as the
source of the dynamic content.
Committing Changes
When using the PME, the editor works directly on the selected workspace. Changes to the
workspace files are automatically reloaded by the PME. Changes in the PME are saved in temporary
.wal files. These files function as the editors change history which is used by features like Undo and
Redo.
Print Models with saved in a .wal file are marked in the Workspace Explorer of the Simple Model
Editor with and Icon, "The model has uncommitted changes".
To finalize made changes, a user can commit them to the Print Model in the Commit Changes Tab.
This applies the changes to the Print Model, validates the resulting model and, if successful, deletes
the .wal file and overwrites the Print Model in your workspace.
NOTE
This procedure persists even temporary changes in case of system crashes, editor
closing or validation problems. Every change a user makes is reflected directly in
the workspace which is helpful during support requests and bug reports. When
making such inquiries, please make sure to add your Print Model and any relevant
.wal file.
Print Model Editor
This chapter serves as an overview over the basic features and views of the Print Model Editor.
Overview
The Print Model Editor can be divided into 4 main areas:
1. Editor Header
2. Sidebar
3. Tab
4. Editor Stage
Editor Header
The Header of the Print Model Editor contains the following buttons. These can be found in the top
right-hand corner of the editor:
Undo
Reverts the last change in the current view.
8

-- 8 of 83 --

Redo
Reverts the last Undo in the current view.
Errors
Shows the number of error messages for the current Print Model.
Warnings
Shows the number of warning messages for the current Print Model.
Print
Opens the Preview for the current Print Model.
Close
Close the Current Model
Sidebar
The Sidebar serves as the main navigation. The Sidebar can be used to open the following tabs:
1. General
2. Schema
3. Text Styles
4. Segment
5. Section
6. Watermark
7. Commit changes
The standard "Save", "Cancel" and "Deploy" buttons are present at the bottom of the Sidebar. In the
Print Model Editor, the "Save" button at the bottom of the sidebar opens the Commit Changes Tab.
Tab
Once an item is selected in the Sidebar, the respective Tab is opened. The Tab may be maximized,
minimized or closed using the buttons in the top right-hand corner of the Tab.
NOTE
Only Tabs with an Editor Stage may be maximized, minimized or closed.
The "General", "Text Styles" and "Commit Changes" Tab therefore do not have these
options.
Editor Stage
The Editor Stage is used for placing and editing Print Model Elements. The Editor Stage can be
accessed from the following tabs:
• Segment Tab
9

-- 9 of 83 --

• Section Tab
• Watermark Tab
CAUTION
The Editor Stage is shown when the Schema Tab is open to give a better
overview of the current model.
Placing and editing of the Print Model Elements should nevertheless be made
through the respective tabs to ensure the correct Segment, Section or
Watermark is being edited.
There are two modes available in the Editor Stage: Stage Mode and Layout Mode. Effortlessly switch
between them using the conveniently located buttons at the top of the Editor Stage.
Figure 4. Switch the Editor mode
Stage Mode (Default)
The Stage Model allows Print Model Elements to be moved, resized and their content edited. Each
Print Model Element is edited with a dedicated Detail Editor as described in Editors for Model
Elements.
10

-- 10 of 83 --

Figure 5. Add element library
New Elements can be added by clicking on the "Element Library" in the top-left of the Editor Stage
and dragging and dropping the desired Element onto the Editor Stage whilst in "Default" mode.
Existing elements can be moved and resized by drag and drop. Alternatively, the position or size
may be precisely defined using the following settings in the Element Stage header:
• Position Top
• Position Left
• Width
• Height
Right-clicking an Element opens a Context Menu with the following options:
• Copy
• Delete
• Group into Area
• Hide Conditions
NOTE
Print Model Elements may be copied or deleted using:
• "Delete selected elements" and "Copy selected elements" buttons in the Element
Stage header
• Keyboard shortcuts, Delete and Ctrl+C
TIP Copied Elements may be pasted by:
11

-- 11 of 83 --

• Right-clicking on the Stage and selecting "Paste" in the context menu
• Keyboard shortcut, Ctrl+V
To open the detail view of an element, you can double-click the element, or select the element and
click the "Open Detail Edit" button in the right-hand corner of the Stage Header.
Figure 6. Editor Stage options
Figure 7. Element context menu
Group into Area
This feature allows users to group multiple selected elements into an Area element, enabling them
to be affected by the Page Break Behavior configuration of the new Area.
To group elements into an Area:
1. Select multiple elements in the Editor Stage.
2. Right-click and select "Group into Area" from the context menu, or click the "Group selected
elements into Area" button in the right-hand corner of the Stage Header.
The selected elements will be grouped into an Area element.
When elements are grouped into an Area, their Page Break Behavior settings are automatically
adjusted:
• Elements whose page break behavior’s source is Default will be changed to Inherited,
inheriting the page break behavior from the new parent Area.
• Elements whose page break behavior’s source is already Inherited will continue inheriting, but
now from the new parent Area instead of their previous parent.
• Elements whose page break behavior’s source is Input will remain unchanged, keeping their
explicitly defined page break behavior.
NOTE
• The "Group into Area" option is disabled when selecting a single Area, Bounding
Box, or Switch element. Since these elements already function as containers,
wrapping a single container in another Area would create redundant nesting
without providing additional functionality.
Layout Mode
The Layout Mode is used to view and modify margins for existing Print Model Elements. This allows
12

-- 12 of 83 --

precise control of the layout as described in Positioning & Layout.
The margin values are displayed at the top and bottom of the element.
The margin value can be adjusted by:
• Drag and drop
• The input fields in the Editor Stage header
NOTE Top and Bottom margins added in the Layout Mode will also be displayed in the
Stage Mode (Default).
Figure 8. Adjust margins
General Tab
In the General tab, the meta information of the model can be edited. The following properties can
be edited in this tab:
Description
The internal description of this Print Model.
Roles
The allowed roles for this Print Model.
13

-- 13 of 83 --

Annotations
The annotations of this Print Model.
You can select roles from the workspace by clicking the btn:[Add] and selecting an option from the
dropdown. Roles can be deleted by clicking the red button in the row and confirming your choice.
PDF Metadata
The PDF Metadata section allows you to configure PDF metadata fields that will be embedded in the
generated document. These metadata fields are essential for PDF/UA accessibility compliance, as
they provide important document information for assistive technologies. All metadata fields are
configured as a list of computation alternatives, allowing for dynamic values based on Document
Model data.
Document Model (optional)
The Document Model to use for metadata field computations. This determines which data paths
are available for the computation operations.
Title
The title of the PDF document.
Description
The description/subject of the PDF document.
Author
The author of the PDF document.
Language
The language of the PDF document.
Each computation alternative consists of:
• Operation: A Kernel Language expression that computes the metadata value. This can be a path
reference to a field in the Document Model, a static value, or any valid Kernel Language
calculation.
• Precondition (optional): A Kernel Language expression that determines whether this
computation alternative should be used. The computation is only applied if the precondition
evaluates to True.
The computation alternatives are evaluated in order, and the first one with a matching
precondition will be used.
NOTE
Metadata Default values are automatically set for new Print Models. It is
recommended to customize these metadata fields with meaningful values specific
to your document to ensure proper accessibility compliance.
CAUTION If all preconditions evaluate to False or produce an empty result, no metadata
value will be set for that field. This will negatively affect PDF accessibility
14

-- 14 of 83 --

compliance.
Figure 9. General tab
Schema Tab
In the Schema tab, the model references of the Print Model can be edited. The Print Model can
reference Document Models to allow for the inclusion of dynamic content. An alias can be set for
Document Model References, that allows for easier distinction of the referenced Models.
Additionally, you can add Print Model References to include template segments, and Typesetting
Model References to manage typography settings.
CAUTION
The referenced Document Model must not contain dots in its name. It is possible
to rename the Document Model or to use an alias without any dot to bypass this
restriction.
NOTE You can add multiple Document Models in the Schema Tab, but currently only one
can be used as reference in the Print Model.
Document Model References
The list of referenced Document Models and their optional alias in this Print Model. The "Open
Setting" button may be used to update the given alias.
Print Model References
The list of referenced Print Model Templates. After selection, the Print Model Editor will check
the selected Print Model for compliance with the Template specification. If this check fails, the
Print Model cannot be used as a reference.
15

-- 15 of 83 --

NOTE
If changes are made to the referenced Document Model, the field references used by
the elements of the Print Model may become invalid. In such cases, you need to
manually update the field references in the Print Model to ensure consistency.
Changes to referenced Templates will be detected when modeling a Segment which
references the updated Template. This change will create updates that need to be
committed.
Typesetting Model References
The list of referenced Typesetting Models will be used for the Text Styles. Typesetting Models can
be created using the Typesetting Model Editor.
Figure 10. Schema tab
Text Styles Tab
In the Text Styles tab, Text Styles can be created, edited and removed. The upper part of the Text
Styles tab displays an example of the current Text Style, for which the properties can be edited
below. A newly created Print Model comes with a default Text Style which cannot be deleted.
The Text Styles that have been modeled are shown in the list on the left-hand side.
The currently selected Text Style is applied to the preview text at the top of the Text Styles view.
This Text Style can be edited using the modeling settings described below.
16

-- 16 of 83 --

Figure 11. Text Styles tab
Click "Add New Text Style" in the bottom right-hand corner to add a new Text Style.
Text Style Settings
General configuration options for Text Styles:
Name
The name of the Text Style. Used to distinguish the Text Styles.
Semantic
The semantic purpose of the Text Style. Possible values are Paragraph, Headline 1, …, Headline 6.
Selecting a Headline semantic, will mark the Text elements that use the Text Style as headings for
accessibility purposes.
[universal access] Accessibility Hint In order to improve navigation and readability it is
important to use the correct semantic structure for your headlines, subheadings and
paragraphs.
If you want to create a heading, select the appropriate Headline semantic based on the heading
level. You can only use Headline 2 as a subheading after Headline 1, Headline 3 after Headline
2, and so on.
If you want to create a new standard paragraph Text Style, select the Paragraph semantic.
The relationship between font size and line height is crucial for readability, especially for
people with visual impairments, dyslexia, or cognitive disabilities. We recommend a line
height of at least 1.5 times the font size, with a minimum recommended font size for body text
of 12 pt.
17

-- 17 of 83 --

Typography Settings
Options that affect the appearance and behavior of text:
Typesetting Model
Specifies the typesetting model applied when this Text Style is used. Available models are added
via the Schema Tab
Static Hyphenation
Applies language-specific hyphenation rules to text. Available options include:
• en_US: English
• de_1996: German (with the hyphenation rules that were introduced with the spelling reform
of 1996)
• fr: French
• cs: Czech
• pt: Portuguese
• vi: Vietnamese
Font
The font family of the Text Style. See Default Fonts for available fonts. Additional fonts can be
provided as workspace resources.
Font Size
The font size of the Text Style in pt.
Line Height
The line height of the Text Style in pt.
NOTE
If a Text Style employs a font that has not been configured in the Print Engine, any
elements applying that Text Style will be displayed and printed with the default
font.
Available Default Fonts
The Print Engine comes with three available default fonts:
• Open Sans
• Noto Sans Mono
• Noto Sans Symbols
Additional fonts can be provided as workspace resources.
Overwrite Default Fonts
To overwrite a default font, add a custom font with the same name as the default font you wish to
replace. Text Styles using that font name will then be rendered with your custom font instead.
18

-- 18 of 83 --

Font Support for DIN 91379
DIN 91379 is a standard that ensures all relevant characters for European languages are correctly
displayed.
The default font Noto Sans Mono included in the Print Engine supports all DIN 91379 characters. If
this font does not match the desired style, you can provide a custom font in the workspace
resources that also supports all required characters.
Custom Fonts
Custom fonts can be added by placing font files in the workspace resources folder. Once added,
they will be available in the Print Model Editor and can be used to define Text Styles.
Figure 12. Text Styles tab with custom fonts
NOTE
In the event that any text styles in your print model were previously associated with
fonts not present in the resources folder, a notification toast will promptly alert you
to these inconsistencies.
NOTE To ensure consistency, take a moment to reselect those fonts for affected text styles.
Elements associated with these styles will adopt the default font if not addressed.
19

-- 19 of 83 --

Figure 13. A Text Style that uses un-configured font
Segment Tab
In the Segment Tab, Segments can be created, edited, reordered and deleted.
Model Segments are containers for elements that each result in one or more DIN-A4 pages in the
PDF document. Multiple Segments may be added to the Print Model and each segment starts on a
new page and defines the page orientation of these pages.
The Segments will be added to the final Document in the Order that they are shown in the Segment
Tab. Existing Segments can be modified using the following buttons:
Open Setting
Edit the Name or Repeatability Settings of the selected Segment.
Open Editor
Open the Editor Stage to edit the Segment’s content.
Open Menu
Duplicate
Duplicate the selected Segment.
Delete
Delete the selected Segment.
NOTE Clicking on the Segment in the List will also cause the Editor Stage to open.
A Document Model Group can be selected as a data context in the Segment Settings. If the selected
group is repeatable, then the Segment will be repeated for each instance of the repeatable group in
the provided Document, with each instance creating a new page.
20

-- 20 of 83 --

Figure 14. Repeatable Segment: Select Document
Model
Figure 15. Repeatable Segment: Select repeatable
Group
New Segments may be added at the bottom of the List.
Name
The Name of the new Segment
Orientation
• Portrait
• Landscape
Template Segment (optional)
A Template from one of the referenced Print Models in the Schema Tab.
Once a Name and Orientation have been given the "Add" button must be clicked to add the new
Segment to the list of Segments.
21

-- 21 of 83 --

Figure 16. Segment tab
Print Model References (Templates)
You can use Print Model References as Templates in other Print Models. This allows you to define
regions with a set height and static layout, enabling the creation of Print Models that conform to
various standards (e.g. DIN standards).
Create Template
To create a Template, you need to create a Print Model with a Segment. This Segment can be used to
define the layout of your Template using Bounding Box elements.
CAUTION A Print Model Segment can only be used as a Template Segment if it contains
only Bounding Box elements or nested Bounding Box elements.
NOTE A Template Print Model can include multiple Template Segments.
Use Template
To use a Template, you must add an existing Template Print Model in the Schema Tab to your
current Print Model. The Segments of the Template can then be used as Template Segments in your
current Print Model.
22

-- 22 of 83 --

Figure 17. Select Template Segment
NOTE
If changes have been made to the Template, the Print Model Editor automatically
reloads the Template each time you visit a Segment that uses it. This will create
updates that need to be committed.
Section Tab
In the Section tab, Sections can be created, edited and deleted.
Sections are optional containers for elements that result in headers and footers in the resulting PDF
document.
TIP If you chose to model Sections, it is often easier to model the Section first and then the
Segments.
Which Section is applied to which page is defined by the two central properties of the Section:
Page Orientation
A section is only applied to pages of the selected page orientation.
Section Usage
A section is either applied to only the first page of the PDF document, or to all remaining pages.
This allows the definition of a specific header & footer for a cover page. On first pages, sections
that are specifically marked for the first page are prioritized.
Each combination of Page Orientation and Section Usage properties has a specific slot listed in the
Section tab. Modeling a Section leads to the modeled header and footer areas being grayed-out in
the respective Segment.
Sections may be added and modified with the following buttons:
Add
Add a Section for this Page Orientation and Section Usage.
Open Editor
Open the Editor Stage to edit the Section’s content.
Delete
Delete the selected Section.
NOTE Clicking on the Section in the List will also cause the Editor Stage to open.
23

-- 23 of 83 --

Figure 18. Section tab
Watermark Tab
In the Watermark tab, watermarks can be created, edited, and deleted.
Watermarks are optional containers for elements that result in overlays on their respective
segments. There are two watermarks that can be set according to segment orientation: portrait and
landscape.
Watermarks may be added and modified with the following buttons:
Add
Add a Watermark for this Page Orientation.
Open Setting
Edit the Opacity or the Conditions under which the selected Watermark is shown.
Opacity
Define the opacity of the element of the watermark pages
Condition
Using Kernel Language to define conditions. If any of the conditions are evaluated as true, or
no condition is defined, the watermark will be printed.
Open Editor
Open the Editor Stage to edit the Watermark’s content.
24

-- 24 of 83 --

Delete
Delete the selected Watermark.
NOTE The attachments of Listing Element are not added to the end of the document when
used inside a Watermark.
Figure 19. Watermark tab
Commit Changes Tab
In the Commit Changes tab, the pending changes of the Print Model can be committed and thus
finalized. The top of this tab shows the change history, a list of changes made in the editor. To
commit the changes the user can make a selection in the change list and then click the "COMMIT
SELECTED CHANGES AND TRUNCATE HISTORY" button.
Pre-default, all changes are selected. To de-select a change, click on the Status of the Change to
toggle the Status from "Commit" to "Pending".
On the right-hand side, validation errors related to the selected changes are displayed in an error
tree. By clicking on the "Go to" icon of an error in the tree, the user will be navigated to the place
where the error has occurred.
During the commit process the semantically correctness of the calculations inside the Print Model is
checked. The error messages are visible above the validation errors.
25

-- 25 of 83 --

Figure 20. Commit Changes tab
Preview
When the "Print" button is pressed, the Print Preview window will open for the currently open
Print Model. You can have a preview window open simultaneously for each print model in your
workspace.
This screen displays the resulting PDF preview. The PDF is automatically reloaded when
committing changes to the Print Model or when the preview settings are changed.
Preview Settings
At the top of the Preview, there is a toolbar that provides the following settings for the resulting PDF
Preview:
Test Document
Select a Document (from Workspace Data) based on the Document Model referenced in the Print
Model to use for printing.
Reload Button
Manually regenerate the PDF with the current preview settings.
Localization Dropdown
Change the locale of the resulting PDF. Locale options are obtained from the referenced
Document Model used for printing.
Time Zone Dropdown
Change the time zone of the resulting PDF. The user’s current time zone is selected as the default
26

-- 26 of 83 --

and appears first in the dropdown list.
Fallback Font
Select the font to use when a referenced font is unavailable. The default fallback font is Open
Sans. All fonts available in the workspace resources are selectable as fallback options.
Using Print model with pending changes
Used to generate the preview with pending changes, which will re-render the PDF with each
change made in the editor. When toggled off, only committed changes will be displayed.
Figure 21. Preview window
NOTE The PDF Preview can be downloaded or printed using the standard buttons in the
top-right hand corner of the Preview.
TIP
You can create Test Documents in several ways:
• Using Ad Hoc Testing in the Document Model Editor
• With the Form Model Preview in the Form Model Editor
• From the documents available in Workspace Data
Please refer to the Document or Form Modeling Documentation for more details.
Validation
The Print Model is automatically validated after every user interaction to ensure consistency and
correctness. This includes two layers of validation:
• Internal Validation: Ensures the Print Model structure is valid and complete. This includes
checks for required fields, value ranges, and correct formats.
27

-- 27 of 83 --

• Reference Validation: Checks whether references to Document Models are valid. This includes
all field references (e.g. fields in Text Elements, Tables, …) and computations (formulas that
refer to fields in the Document Model).
CAUTION References inside Expression elements are currently not validated.
Validation errors are displayed directly in the Editor to provide immediate feedback. The modeler
must resolve all validation errors, otherwise it is not possible to print or commit the changes.
Editors for Model Elements
This chapter describes the Print Model Elements that may be added, edited and deleted in the
Editor Stage.
Common Properties of Print Model Elements
Detail View
The Detail View allows the editing of the properties of each specific Element. The modeling options
available in the Detail View is determined by the type of element that has been selected.
The Detail View has no "Save" button, since every change is saved automatically to the commit
history. Simply click on "Close" in the top right-hand corner of the Detail View or select the next
Element in the Editor Stage.
28

-- 28 of 83 --

Figure 22. Element Detail View
Input Source
This feature allows users to explicitly define the source of values, enhancing data clarity and input
control.
By specifying the value source, it resolves ambiguity between unset and default values and clarifies
contexts that affect the value. Users can make informed decisions by understanding the precise
origin of values in their results, preventing confusion or incorrect assumptions.
There are four types of sources applied to fields:
• Default: The default value will be used for the field
• Input: The value is provided by the user
• Unset: No value will be assigned
• Inherited: The value is inherited from its parent
29

-- 29 of 83 --

Figure 23. Input Source
Default
Figure 24. Input Source
Input
Figure 25. Input Source
Unset
Figure 26. Input Source
Inherited
You can change the input source by hovering over the currently selected input source icon. The
available options will appear, allowing you to select a different source.
Refer to the table below for fields that apply the input source and their respective source types.
Table 1. Input source fields
Element Field Source
Default Input Unset
Table Max row count ✘ ✔ ✔
Sum row label ✘ ✔ ✔
Column’s label ✔ (value: Column) ✔ ✘
Column’s width ✘ ✔ ✔
Listing Column’s label ✔ (value: Column) ✔ ✔
Column’s width ✘ ✔ ✔
Table Layout Row options:
minimum height
✔ (value: 10mm) ✔ ✘
Column options:
width
✘ ✔ ✔
Line Chart Title ✘ ✔ ✔
Label X-Axis ✘ ✔ ✔
Label Y-Axis ✘ ✔ ✔
Bar Chart Title ✘ ✔ ✔
Label X-Axis ✘ ✔ ✔
Label Y-Axis ✘ ✔ ✔
Pie Chart Title ✘ ✔ ✔
Table 2. Text properties source fields
Text Properties Default Input Inherited
Text Style ✔ (value: Default Text
Style)
✔ ✔
Bold ✔ (value: true for
header properties,
otherwise false)
✔ ✔
Italic ✔ (value: false) ✔ ✔
Underline ✔ (value: false) ✔ ✔
30

-- 30 of 83 --

Text Properties Default Input Inherited
Color ✔ (value: #000000) ✔ ✔
Background Color ✔ (value: #ffffff) ✔ ✔
Table 3. Border properties source fields
Border
Properties
Default Input Unset Inherited
Border Style ✘ ✔ ✔ (2) ✔ (1)
Border Width ✔ (value: 1pt) ✔ ✔ ✔ (1)
Border Color ✔ (value: #000000) ✔ ✔ ✔ (1)
(1) The Inherited source is only available for elements that inherit border properties from a parent:
listing columns (inherit from the listing) and text elements inside a table layout cell (inherit from
the table layout).
(2) The Unset source is not available for Border Style on Line elements — a value is always
required. New Line elements default to Solid.
NOTE
Setting Border Style to Unset automatically resets Border Width and Border
Color to their Default source, since these properties have no effect without a
border style.
Inherited Source
The Inherited source resolves the value from a parent element at runtime. The following
inheritance relationships can exist:
Element Can inherit from Properties
Listing column Listing Border properties (border style,
width, color)
Text (inside Table Layout cell) Table Layout Border properties (border style,
width, color)
Expression (inside Table) Table Text properties (all fields)
All Elements (inside container
elements)
Area, Bounding Box Page break behavior
Text Properties
Allows the definition of properties affecting the text portions of the element.
Text Style
Selection of a Text Style.
31

-- 31 of 83 --

Bold, Italic, Underline
Toggles the respective property.
Color
Hexadecimal code for the text color.
Background Color
Hexadecimal code for the background color of the text.
Alignment
Selection of a text alignment.
• Left
• Center
• Right
• Justify
Figure 27. Text Properties detail view
When the source is Default, the default values are applied. See the table in Text Properties Source
Fields for details.
Border Properties
Allows the definition of a border around the element.
Border Width
Sets the width of the border in pt.
Border Style
Sets the style of the border.
• None
32

-- 32 of 83 --

• Solid
• Dashed
• Dotted
Border Color
Hexadecimal code for the border color.
Figure 28. Border Properties detail view
Hide Conditions
Hide Conditions allow the definition of conditions that will hide elements from being displayed in
the resulting PDF document. The Hide Conditions can be edited by right-clicking an element and
selecting the "Hide Conditions" option. The conditions are written as Computations in the Kernel
Language syntax. It is expected that the condition returns a boolean value, where True leads to the
element being hidden.
If an element is hidden from being displayed in the resulting PDF document, the elements below
the hidden element are move upwards as much as possible.
33

-- 33 of 83 --

Figure 29. Hide Conditions detail view
Example
Consider the example displayed below, that was introduced in the Margin Layout example.
• If element A is hidden by a Hide Condition, all elements below are moved upwards a distance
equal to the height of element A, and it’s bottom margin.
• If element B is hidden, no other element is moved. Element D cannot be moved upwards, since
it is blocked by element C, and it’s bottom margin.
• If element C is hidden, no other element is moved. Element D cannot be moved upwards, since
it is blocked by element B, and it’s bottom margin.
• If element B and C are hidden, element D is moved upwards a distance equal to element B's
height and bottom margin.
• If element A, B and C are hidden, element D is moved upwards to the initial position of element
A.
34

-- 34 of 83 --

Figure 30. Margin layout example without displayed
margins.
Figure 31. Margin layout example with displayed
margins.
Computations
Computations in Print Models use the Kernel Language syntax to dynamically calculate values or
evaluate conditions. Depending on the context, computations in the Print Model serve different
purposes:
Conditions
Used for conditionally showing or hiding content. This can be used in:
• Hide Conditions - to conditionally hide elements
• Switch Cases - to conditionally display different content
• Watermarks - to conditionally display watermarks
Computation Alternatives
Used for computing dynamic values. This can be used in:
• Calculation Elements - to compute and display dynamic values
• Listing Elements - to compute cell values and properties
Each Computation Alternative consists of:
35

-- 35 of 83 --

Precondition
A condition that must evaluate to True for the operation to be applied. If no precondition is
specified, the operation is always applied.
Operation
The expression that computes the result value.
The Computation Alternatives are evaluated in order, and the first one with a matching
precondition (or no precondition) will be used.
Referencing Document Model Data
In computations, you can reference fields and groups from the Document Model using their paths:
[CustomerDM/customer/firstName]
Document Model Metadata Fields
In addition to referencing Document Model fields and groups, you can access metadata properties
of Document Model elements using the metadata/… prefix.
metadata/<element-path>/<metadata-field>
For example, to access the label metadata of a field at /customer/firstName:
[metadata/customer/firstName/label]
This would return the localized label of the field, e.g. "First Name".
Available Metadata Fields
The following metadata fields are available:
Table 4. Available Metadata Fields
Metadata Field Applies To Description
required Fields, Groups Whether the element is required. Returns a
boolean value.
externalDescription Fields, Groups The external description of the element.
path Fields, Groups The path of the element as a string with leading
and trailing slashes (e.g. "/customer/addresses/").
label Fields only The label of the field.
repeatability Groups only The repeatability setting of the group.
36

-- 36 of 83 --

Metadata Field Applies To Description
errorMessage Enumeration Fields
only
The error message defined for the enumeration
field.
Annotation Fields
You can also access annotations of Document Model elements using the annotations/… prefix:
annotations/<element-path>/<annotation-name>
For example:
[annotations/customer/firstName/myCustomAnnotation]
Listing-specific Computation Fields
The Listing Element supports additional computation fields for accessing information about the
current row’s entity. See Additional Computation Fields in the Listing documentation for details.
Page Break Behavior
Page break behavior allows users to define how an element interacts with page breaks in the
resulting PDF document. This setting is only available on the Segment Tab. To edit it, double-click
the element in the Layout Mode to open the Detail View. The page break behavior options are
located under the page break settings.
Figure 32. Page Break Behavior detail view
The Input Source is used to define the source for the page break behavior value. There are three
sources available:
• Default: The default value is applied. The default value is Allow.
• Input: The user can select between the two behavior options described below.
• Inherited: The value is inherited from its parent element. This is only applicable for elements
inside container elements (Bounding Box/Area/Switch).
37

-- 37 of 83 --

The page break behavior options are:
Allow
The element can be split across pages.
Avoid
The element will not be split across pages.
• For container elements (Bounding Box/Area/Switch): If the element (including gaps
between child elements and child elements that are also set to 'Avoid') falls on a page break,
the whole element is pushed to the next page, even if it won’t fit completely.
• For standalone elements: If the element falls on a page break, it is pushed to the next page
to avoid splitting. If it won’t fit on the next page at all, it remains in its current position.
NOTE
For repeatable Areas and Switch elements, the page break behavior is not applied
to the entire element, but rather to each repetition/case of the element. This means
that if an Area or Switch is set to Avoid, each repetition/case of that Area or Switch
will try to avoid being split across pages, but different repetitions/cases may end up
on different pages.
If an element is set to Avoid, it is indicated by a small icon in the top right corner of the element in
the Layout Editor Stage.
Figure 33. Page Break Behavior Indicator
Default Input Source for New Elements
• In the Segment stage: When creating a new element, the page break behavior’s source is set to
Default.
• In the stage of container elements (Bounding Box/Area/Switch): When creating a new element,
the page break behavior’s source is set to Inherited and references the page break behavior of
the container element containing it.
38

-- 38 of 83 --

Text
The Text element displays text in the resulting PDF. The content of the Text element can consist of
static text and dynamic inline insertions:
• Field
• Calculation
• Page Number
NOTE Page Number elements are only available on Text elements that are placed inside
Sections.
The Text element content is defined using the rich text editor. The following settings are available
in the header of the rich text editor.
Bold
Sets the formatting of the currently selected text to bold.
Italic
Sets the formatting of the currently selected text to italic.
Underline
Sets the formatting of the currently selected text to underlined.
Color
Sets the color of the currently selected text to the color displayed in the hexadecimal color code
picker immediately to the right.
Background Color
Sets the color of the currently selected text to the color displayed in the hexadecimal color code
picker immediately to the right.
Field
Adds the reference to a Field from a referenced Document Model using the Field Editor.
Calculation
Adds the result of a Calculation based on data from the Document using the Calculation Editor.
Remove font size inline styles
Removes any inline font-size styles from the text. This is useful when pasting content from
external sources that override the standard styling.
Additionally, the Text element is configurable using Border Properties and a subset of the Text
Properties.
If all dynamic inline elements evaluate to an empty value, you have the option to hide the Text
element. To enable this behavior, select the option Hide this element if all nested entities are
empty.
39

-- 39 of 83 --

[universal access] Accessibility Hint
For accessibility purposes, there are some recommendations when modeling Text elements:
• use the correct semantic text style for headings and paragraphs, e.g. Headline 2 can only be
used as a subheading after Headline 1, etc.
• avoid using italics or underlining
• do not use text alignment "Justified"
• ensure sufficient color contrast between text and background if you use custom colors
• do not use text colors alone to convey information (e.g. red for errors), use icons or
additional text instead
• do not refer to other text segments by visual information only (for example, do not use "see
below"). Instead, use links or spell out the headline of the respective paragraph (See
chapter 5.1 of this document).
• copying lists (as in bullet-point list) from other sources into the text editor does not
create the correct semantic accessibility structure for lists in the output PDF. If you have
this requirement in your project, please reach out to Print Engine team.
40

-- 40 of 83 --

Figure 34. Text element detail view
Field
The Field element can insert the content of a Document Model Field instance into the resulting PDF
document. The content of the selected Field instance will be evaluated and returned as a formatted
string. The formatting of the string is determined by the Field Type settings below.
Add a Field element by clicking on the "Field" Button in the rich text editor of a Text element.
Document Model
Select a Document Model referenced in the Schema Tab.
Field
Navigate to the desired field by expanding Groups in the selected Document Model then click on
the Field
41

-- 41 of 83 --

Field Type
The formatting can be modified based on the Field type of the instance, by selecting one of the
following types:
• Text (no selection): Displays the data of a Field instance as plain text without any
formatting.
• HTML: Formats a Field instance containing HTML content. HTML tags are processed and
rendered in the resulting PDF.
The following HTML tags are supported:
<p>, <span>, <strong>, <em>, <u>, <br>
The following CSS properties are supported in style attributes:
◦ color – any valid hex color value (e.g. #ff0000)
◦ background-color – any valid hex color value (e.g. #ff0000)
◦ text-align – left, right, center, justify
CAUTION Unsupported tags and CSS properties are stripped silently during
rendering.
• Date: Formats a Date Field instance into a readable date format based on the locale. The
formatting can be customized by defining a specific pattern for evaluation. For example, the
pattern YYYY.MM.dd evaluates to e.g. 2025.02.17, while the pattern dd’th of' MMMM YYYY 'at'
HH:mm 'o''clock' evaluates to 17th of February 2025 at 13:54 o’clock.
• Date range: Formats a DateRange Field instance into a readable format based on the locale.
The formatting of the start date, end date, and separator can be customized by defining an
evaluation pattern.
NOTE
You can find more thorough information about evaluation patterns for dates
by clicking the Info Icon next to the pattern input field or by reading through
the official Java documentation.
• Checkbox: Formats a Boolean Field instance based on its value. You can specify which value
should be displayed for true and false.
NOTE
To display special characters such as ☐ or ☑ as the display value for the
checkbox type, ensure that the selected Text Style uses a font that supports
these characters. The Print Model Editor provides the font Noto Sans
Symbols, which is compatible with these symbols.
Suffix
Model a suffix for the formatted string data.
Click "Back" to return to the Text Editor.
42

-- 42 of 83 --

Figure 35. Field detail view
Calculation
The Calculation element can insert the result of a Kernel Language into the resulting PDF
document. The Kernel Computation functions analog to computations in Document Models. If the
Computation does not return a String, the result type of the computation must be explicitly defined
by the user to allow the default formatting options to be applied.
Add a Calculation by clicking on the "Calculation" Button in the rich text editor of a Text element.
CAUTION
The Calculation Element is not a replacement for Computations of Business data
and does not provide the same Level of accuracy as Computations within a
Document Model.
Name
The Name of the Calculation
43

-- 43 of 83 --

Document Model
Select a Document Model referenced in the Schema Tab.
Field Type
The result of the Calculation is, by default, assumed to be a String. If the result is of another type,
the Field Type property needs to be set. The following Field Types are available using this
configuration:
• Boolean
• Number
• String
• TypeDefinition
NOTE If TypeDefinition is selected, a TypeDefinition from the selected Document Model
must be specified.
Formatting Type
If a Field Type is specified, a Formatting Type may additionally be set. The options are similar to
the String formatting options offered in the Field.
Suffix
Model a suffix for the formatted string data.
Precondition and Operation
A Computation Table is modeled as described in the Kernel Language Documentation.
Click "Back" to return to the Text Editor.
44

-- 44 of 83 --

Figure 36. Calculation detail view
Page Number & Page Number Total
The Page Number element inserts the current page number into the resulting PDF document, while
the Page Number Total inserts the total number of pages.
Add a Page Number or Page Number Total by clicking on the respective Button in the rich text
editor of a Text element placed inside a Section.
No further modeling is required.
45

-- 45 of 83 --

Figure 37. Page Number and Total Page Number in a Section
Line
The Line element inserts a horizontal line into the resulting PDF document, which functions analog
to the HTML <hr> element. The properties of the Line element are determined by its Border
Properties.
NOTE The Border Style of a Line element is always required — the Unset source is not
available. New Line elements are created with Border Style set to Solid by default.
Table
The Table element inserts a table into the resulting PDF, which points to a selected repeatable
group in a referenced A12 Document Model. Each row of the resulting table corresponds to one
instance of the repeatable group. The Table can have any number of columns, but needs at least
one column to be displayed correctly. Each column can have a title, which will be displayed in the
header and is based on an Element type:
• Field
• Expression
If a column is marked as a sum column, one additional row will be generated at the bottom of the
table, which will contain the sums of each sum column. To indicate this last row as a sum row, you
can define a sum label. This label will be rendered in the first column of a table if at least one sum
column is defined in the other columns of the table.
Document Model
Select a Document Model referenced in the Schema Tab.
46

-- 46 of 83 --

Group
The path to the Group in the Document Model that defines the root of the Table Element. Every
row of the Table Element corresponds to one repetition of this Group.
NOTE Only Groups can be selected, that are either non-repeatable or are repeatable
Groups that are not children of other Repeatable Groups.
Columns
Each Column of the Table Element pertains to a defined nested Element. Click "Add" to add new
columns or click on the Column in the table to edit it. See Columns for more details.
General Properties
Maximum Row Count
Numerical value that specifies the maximum number of rows that will be generated for the
Table Element. Click "Unset Value" to skip this setting. See Input Source for more details on
these settings.
Sum Row Label
A String label that is displayed in the first cell of the row containing the results of the Sum
Columns. If the Sum Row Label is unset, no label will appear. See Input Source for more
details on these settings.
NOTE If the first Column is marked as a Sum Column, the row will not have a label.
Filtering
Filter Expression
A filter expression that applies a filter on the set of rows. Its syntax conforms to the Filter
Expression Syntax used in the Form Model. The row is displayed if the condition is fulfilled.
Additionally, the Table element is configurable using Border Properties and separate configurations
of Text Properties for the header and body of the table.
47

-- 47 of 83 --

Figure 38. Table detail view
Figure 39. Table element detail view (continued)
Columns
Element type
The type of this Element must be selected as it defines the content of the respective Column.
• Field: Each cell of the column is inserted directly from a specific field instance, analog to the
Field element.
• Expression: Each cell is the result of an expression evaluated on the repeatable group
instance, analog to the Expression element.
Label
The label of the Column in the Table Element header. See Input Source for more details on these
settings.
• Default Value
• User Input
Hide label?
Hides the label for this column
48

-- 48 of 83 --

Width
Percentage width of the Column in respect to the Listing Elements width. If no width is set, the
default HTML-table behavior is applied. The value must be an integer between 1 and 100. The
sum of all widths will also be validated to ensure that it is 100 or less.
Sum Column
Marks the Column to be summed up at the bottom of the table.
NOTE A Column can only be marked as a Sum Column, if the Element Type is a Field
that is a Number Field.
Click "Back" to return to the Table Editor.
Figure 40. Table field column detail view
Figure 41. Table expression column detail view
[universal access] Accessibility Hint
For accessibility purposes, a table element should have more than one column and more than
one row.
Do not use the "Hide Label" option for columns and the "Hide Header" option for the table.
49

-- 49 of 83 --

Image
The Image element inserts an image into the resulting PDF.
Image source type
The image data can be inserted from two different sources:
• Static
• Dynamic
Alternative Text
The text that will be accessed by screen readers. This is required for accessibility purposes.
Height
The height in millimeters that the image will be scaled to. If not set, the image height will be
scaled according to its aspect ratio with respect to the provided image width.
Width
The width in millimeters, that the image will be scaled to. This is required for images with
source type Dynamic.
[universal access] Accessibility Hint
For accessibility standards, the alternative text should be short but descriptive of the image
content. For info graphics the description can be more extensive.
Do not use images that contain important textual information (exception for logos), use the
text element instead.
Static
The image is stored as a named file in the resources folder of the workspace and referenced by
name in the Print Model. The original image dimensions are derived from the file at
upload/selection time and used if not overridden by the user. If only the width property is provided
(with height left empty), the image will be scaled while keeping the original aspect ratio.
NOTE The supported image formats are: GIF, BMP, JPEG, JPG, and PNG. When using GIF
images, only the first frame will be displayed in the PDF.
Picking an existing resource
The resource selector lists all image files available in the workspace’s resources folder. Selecting an
entry sets the image reference to that resource.
Uploading a new image
Click the upload button next to the selector to choose a local file. The file is saved to the workspace’s
resources/ folder under a deduplicated name (basename-sizeId.ext, where sizeId is the file size in
50

-- 50 of 83 --

base-36). If a file with that name already exists it is reused without re-uploading. After upload, the
new resource is automatically selected.
Figure 42. Image element detail view using static image
Dynamic
The image content is inserted from an A12 field instance during the printing process, allowing the
image to be dynamically sourced from Document Model data. The content of the field instance must
be a valid base64-encoded string to be correctly rendered in the resulting PDF.
Document Model
Select a Document Model referenced in the Schema Tab.
Field
Navigate to the desired field by expanding Groups in the selected Document Model then click on
the Field.
The following Fields from the Attachment Group should be selected when referencing
Attachments from the Document Model:
• content Field when using Attachments in Embedded mode.
• attachment_id Field when using Attachments in Reference mode.
NOTE Any other Field in a Document Model that contains a valid base64-encoded image
string may also be selected.
CAUTION
Please note that the Print Preview will not work with the attachment_id selected.
This functionality is intended for use in A12 applications that have an
51

-- 51 of 83 --

AttachmentProvider implemented.
Since the image dimensions are not available during the editing, the width must be explicitly set by
the user.
Figure 43. Image element detail view using dynamic image
Expression
The Expression element inserts the result of a Form Engine Expression into the resulting PDF.
Document Model
Select a Document Model referenced in the Schema Tab.
Content
The Expression elements content is generated from the Expression content. For more
information regarding the expected syntax, please refer to the Expression documentation.
52

-- 52 of 83 --

The Expression Element can be hidden automatically if all referenced Fields are evaluated to an
empty value or string. To enable this behavior, select the option Hide this element if all nested
entities are empty.
Additionally, the Expression element is configurable using Border Properties and Text Properties.
Figure 44. Expression element detail view
Listing
The Listing element inserts a tabular representation of a specific subtree of a Document into the
resulting PDF. Each row of the generated table corresponds to one field or group instance of the
selected Document.
NOTE
Due to the complex nature of the customization options, the Listing Element is
recommended for more experienced users.
Consider modeling a Table if the use-case allows it.
The columns of the table are configured on the Listing Element. The cells content and styles are
defined by A12 Kernel Computations set in the respective column configuration. These
computations are executed on the respective field or group instance of the row to determine its
result values.
Document Model
Select a Document Model referenced in the Schema Tab.
Group
The path to the Group in the Document Model that defines the root of the Table Element. This
Group and every Group or Field inside it will be generating a row in the resulting table.
Columns
Each Column of the Table Element pertains to a defined nested Element. Click "Add" to add new
columns or click on the Column in the table to edit it. See Listing Columns for more details.
53

-- 53 of 83 --

Row Property Computations
Property Computations may be modeled that apply to each row of the Listing. See Property
Computation for more details.
Group Property Computations
Group Property Computations may be modeled that apply to the selected Group Instance. See
Group Property Computation for more details.
Additionally, the Listing Element is configurable using Border Properties and separate
configurations of Text Properties for the header and body of the Listing.
54

-- 54 of 83 --

Figure 45. Listing element detail view
55

-- 55 of 83 --

Listing Columns
Column label
The label of the Column in the Table Element header. See Input Source for more details on these
settings.
• Default Value
• User Input
• Unset Value
Column Width
Percentage width of the Column in respect to the Listing Elements width. If no width is set, the
default HTML-table behavior is applied. The value must be an integer between 1 and 100. The
sum of all widths will also be validated to ensure that it is 100 or less.
Sort group content by this column
The resulting table rows will be sorted by the content of this column.
NOTE
The nesting structure of the A12 Document will be kept intact by the sorting. The
contents of any Group will be restructured regarding the sorting, but no instance
within a Group will be moved outside.
Column has custom text properties
Allows full configuration of the Text Properties for the current column. The Text Properties are
shown at the bottom of the Listing Column Editor.
Column has custom border properties
Allows full configuration of the Border Properties for the current column. The Border Properties
are shown at the bottom of the Listing Column Editor.
Default Computations
A set of Value Computations and Property Computations that will be executed if neither Group
Computations nor Field Computations are holding for the rows respective instance.
Value Computations
A Computation Table is modeled to return a specific value under a specific set of conditions. See
Value Computations for more details
Property Computations
Property Computations may be modeled that apply to each row of this Listing Column. See
Property Computation for more details.
56

-- 56 of 83 --

Figure 46. Listing column detail view
57

-- 57 of 83 --

Group Computations
A set of Value Computations and Property Computations that will be executed on any row
corresponding to a Group Instance.
Field Computations
A set of Value Computations and Property Computations that will be executed on any row
corresponding to a Field Instance with a Field Type that matches the Document Field Type.
Select the field type of the results of the value computations
Expected input Field Type for the Field Value Computation.
Computation Output Type
Expected output Field Type for the Field Value Computations. The output of the Value
Computation should match this Field Type.
Field Type
The formatting can be modified based on the Field type of the instance, by selecting one of the
following types:
• HTML: Formats a Field instance containing HTML content. HTML tags are processed and
rendered in the resulting PDF.
The following HTML tags are supported:
<p>, <span>, <strong>, <em>, <u>, <br>
The following CSS properties are supported in style attributes:
◦ color – any valid hex color value (e.g. #ff0000)
◦ background-color – any valid hex color value (e.g. #ff0000)
◦ text-align – left, right, center, justify
CAUTION Unsupported tags and CSS properties are stripped silently during
rendering.
• Date: Formats a Date Field instance into a readable date format based on the locale. The
formatting can be customized by defining a specific pattern for evaluation. For example, the
pattern YYYY.MM.dd evaluates to e.g. 2025.02.17, while the pattern dd’th of' MMMM YYYY 'at'
HH:mm 'o''clock' evaluates to 17th of February 2025 at 13:54 o’clock.
• Date range: Formats a DateRange Field instance into a readable format based on the locale.
The formatting of the start date, end date, and separator can be customized by defining an
evaluation pattern.
NOTE
You can find more thorough information about evaluation patterns for dates
by clicking the Info Icon next to the pattern input field or by reading through
the official Java documentation.
58

-- 58 of 83 --

• Checkbox: Formats a Boolean Field instance based on its value. You can specify which value
should be displayed for true and false.
NOTE
To display special characters such as ☐ or ☑ as the display value for the
checkbox type, ensure that the selected Text Style uses a font that supports
these characters. The Print Model Editor provides the font Noto Sans
Symbols, which is compatible with these symbols.
Suffix
Model a suffix for the formatted string data.
Value Computations
In addition to the Operators and Language Constructs in the Kernel Language and the Fields and
Groups in the referenced Document Model, the Computations used in the Listing Element support
the usage of Additional Computation Fields. The Additional Computation Fields allow the access of
properties relative to the Field or Group instance of the current row.
Additional Computation Fields
The computations inside the complex listing support references to the following Fields to access
information about the entity of the current row:
• value - reference to the value of the current Field. Not filled for Groups.
• path - reference to the path of the current Field or Group.
• name - reference to the name of the current Field or Group.
• depth: reference to the nesting depth of the current Field or Group inside the document model.
Root Groups have [depth] == 0.
• isField: results in True if the current entity is a Field, False otherwise.
• currentRepetition: the repetition index of the current entity.
• repetitions: the repetitions value of the current entity.
• relativeMetadata/…: prefix for accessing the Metadata of the current entity.
• relativeAnnotation/…: prefix for accessing the Annotations of the current entity.
Furthermore, the user can use references to the following Fields to access information of the
current entities' parent Group:
• currentRepetitionOfParent: the repetition index of the current entities' parent Group.
• repetitionsOfParent: the repetitions value of the current entities' parent Group.
• parentName: the name of the current entities' parent Group.
• parentPath: the path of the current entities' parent Group.
Table 5. Simple Examples of Preconditions using Additional Computation Fields
59

-- 59 of 83 --

Preconditions with Additional Field Result
[depth] == 1 Returns True for each child of the
Document Model’s root Group
FieldFilled(relativeAnnotation/annotationA) Returns in True if the current entity
has the annotation annotationA
Table 6. Simple Examples of Operations using Additional Computation Fields
Operations with Additional Field Result
[value] Returns the value of the current Field.
[relativeMetadata/label] Returns the Label of the current entity.
Table 7. Advanced Examples of Preconditions using Additional Computation Fields
Precondition with Additional Fields Result
[parentPath] == "/general/phone/" AND
[repetitionsOfParent] < 2
Returns True for every child of the
Group /general/phone/ if the Group
has less than 2 instances.
[repetitionsOfParent] > 1 AND RoundDown
([currentRepetitionOfParent] / 2, 0) !=
RoundUp([currentRepetitionOfParent] / 2, 0)
Returns True for every 2nd child of a
repeatable Group.
Property Computation
A Property Computation consists of a target Property, and a set of Computation Alternatives that
each consist of a Precondition and an Operation. A wide range of Properties can be set as the target
Property including:
• Text Properties
• Border Properties
• Text Styles
In addition, the following properties may be set:
• Vertical Alignment: Defines the vertical alignment of the cells content.
• Hide Cell: A boolean value, that if set to True, hides the cell completely.
• Hide Value: A boolean value, that if set to True, hides the value of the cell. (Only available for
Property Computations in Listing Columns)
• Column Span: A numeric value, that defines the number of columns that the cell spans. (Only
available or Property Computations in Listing Columns)
The Additional Computational Fields may also be used in the Preconditions and Operations of
Property Computations.
60

-- 60 of 83 --

Group Property Computation
A Group Property Computation applies properties to all Listing rows that correspond to a selected
Group instance. It consists of a target Property, a Group instance, and a set of Computation
Alternatives that each consist of a Precondition and an Operation.
At the moment, only Hide Group can be used as the target Property for Group Property
Computations. If the Computation Alternatives evaluate to True, all rows belonging to the selected
Group instance are hidden.
WARNING The precondition is only allowed to evaluate to true for fields and groups
within the selected group path.
Hide Group Example
The following example demonstrates how to hide a repeating addresses group based on a condition.
61

-- 61 of 83 --

Figure 47. Hide Group Property Computation detail view
Example: Hiding an repeatable address group when it is marked as not public
• Group Path: /customer/addresses
• Precondition: [path] == [metadata/customer/addresses/isPublic/path] And [value] == False
• Operation: True
In this example, all rows belonging to the addresses group will be hidden when the isPublic field’s
value is False. The following images show the rendered PDF output:
62

-- 62 of 83 --

Figure 48. All Groups visible
Figure 49. Non Public Groups hidden
NOTE
You can use the path metadata field in computations to reference an element’s path
as a string value. For example, [metadata/customer/addresses/isPublic/path]
resolves to "/customer/addresses/isPublic/".
Attachments
Attachments in the Listing element are included in the generated PDF if the selected group contains
attachment groups. These attachments are added at the end of the PDF, after all Print Model
content has been rendered. Each attachment starts on a new page.
Rendering of Attachments
Attachments can be resolved in two ways:
• Embedded Attachment: The content of the attachment is embedded in the document data.
• Referenced Attachment: To reference an attachment by setting the attachment ID. The
AttachmentProvider will use this ID to provide the attachment content at render time
Rendering behavior:
• If the attachment content is present, it will be rendered.
• If the attachment ID is present, the referenced attachment will be rendered.
• If both content and ID are specified, the content takes precedence and will be rendered.
To allow referenced attachments, you need to use the AttachmentProvider.
• Attachment Fields: In the rendered listing, all attachment fields are displayed, but the content
field will be shown as a link referencing the rendered attachment.
• Image Size: The image size is scaled to fully fit the page’s width.
• Supported Attachment Types:
63

-- 63 of 83 --

◦ Images: png, jpg
◦ PDFs: pdf
NOTE If the attachment type is not supported, the content field is not shown in the listing
result, and the attachment is not rendered.
Table Layout
The Table Layout element inserts a static table into the resulting PDF document, which provides a
static layout to its contents.
Number of Rows
The number of rows in the resulting table.
Number of Columns
The number of columns in the resulting table.
Row Properties
Index
Number in the range from 1 to the defined number of rows>> that identifies the row targeted
by this set of properties.
Minimal Row Height
Defines the minimal height of a row in millimeters. If unset, the default of 10mm is used. See
Input Source for more details.
Column Properties
Index
Number in the range from 1 to the defined number of columns that identifies the row
targeted by this set of properties.
Column Width
Defines the width of a column in percentage relative to the width of the Table Layout
Element.
Vertical Alignment
Defines the Vertical Alignment of a columns contents. Possible Values are:
• Top
• Middle
• Bottom
The Table Layout element is configurable using Border Properties.
64

-- 64 of 83 --

Figure 50. Table layout element detail view
Table Layout Cells
After modeling the Rows and Columns in the Table Layout, Print Model Elements can be added,
edited and deleted in each cell by hovering over the desired cell in the Table Layout in the Editor
Stage.
To add an element click the "+" button in an empty cell. Clicking the "+" button brings up a popup
that allows to select an element type to be added.
NOTE Currently, the only supported type is the Text element.
To edit an existing cell element, hover over the desired cell in the Table Layout and click the "Edit"
button.
Removing the content of a cell is done by hovering the cell and clicking the "Delete" button.
65

-- 65 of 83 --

Figure 51. Adding an element to a table layout cell
Figure 52. Hovering over a filled table layout cell
Charts
The Chart elements insert a chart representation of repeatable A12 field instances into the resulting
PDF document. The chart will be generated as an image and inserted into the PDF document.
[universal access] Accessibility Hint
This element is currently not recommended, as the Chart element does not generate a
meaningful description of its content for accessibility software like screen readers.
If you have this requirement in your project, please reach out to Print Engine team.
Charts have the following common modeling settings:
Height
The height of the Chart Element in centimeters. One decimal place may be used, for example, 7.5
cm.
Width
The width of the Chart Element in centimeters. One decimal place may be used, for example, 7.5
cm.
NOTE The height and width of Charts can only be edited in the Page Detail View. They
cannot be adjusted by dragging and dropping the Element in the Editor Stage.
Document Model
Select a Document Model referenced in the Schema Tab.
Group
The Diagram element requires selecting a group of a document model to read from. The root
group is selected by default, but it’s possible to select an inner group using the model tree
widget, then the resulted data set only contains the fields of the selected group.
Data
The Data used by the chart is specified below the Group. The way that the Chart Data is specified
depends on the type of Chart.
66

-- 66 of 83 --

Chart Data Type Modeling Settings Multiple Data Sets
allowed
Line Chart Series Field with value Yes
Bar Chart Series Field with value Yes
Categorized • Field with value
• Field for labels
Yes
Pie Chart Categorized • Field with value
• Field for labels
No
Line Chart
Displays multiple continuous lines in a horizontal or vertical orientation, each line corresponding
to a separate set of values. These sets of values are modeled by adding one or more series data sets.
Label X-Axis
The X-Axis Label may be entered or set to undefined.
Label Y-Axis
The Y-Axis Label may be entered or set to undefined.
Orientation
There are two types of orientation to select from:
• Horizontal: The Value will be displayed on the X Axis
• Vertical: The Value will be displayed on the Y Axis
67

-- 67 of 83 --

Figure 53. Line Chart element detail view
Series Data
Series Data is modeled by selecting a repeatable field. The value of this field on the axis defined by
the Orientation. The other axis will count through the repetitions of the selected Field.
Field with value
Specify the Field from the selected Document Model that contains the Series Data.
Name of series
The name of this Series Data in the Print Model.
68

-- 68 of 83 --

Example
The Document Model contains a repeatable Group, Person, with two Number Fields, income and tax.
The following Data is modeled:
Table 8. Series Data Example
Field with value Name of series
root/Person/income DataSet1
root/Person/tax DataSet2
Based on the following data in the Document:
Person[0]:
income: 10
tax: 1
Person[1]:
income: 20
tax: 4
Person[2]:
income: 15
tax: 6
The result will be two Datasets:
DataSet1: [10,20,15]
DataSet2: [1,4,6]
Bar Chart
Displays multiple sets of bars in a horizontal or vertical orientation, each set of bars corresponding
to a separate set of values.
Label X-Axis
The X-Axis Label may be entered or set to undefined.
Label Y-Axis
The Y-Axis Label may be entered or set to undefined.
Orientation
There are two types of orientation to select from:
• Horizontal: The Value will be displayed on the X Axis
• Vertical: The Value will be displayed on the Y Axis
69

-- 69 of 83 --

Bar Charts accept Series Data and Categorized Data. To model Series Data, simply leave the "Field
for labels" blank.
Figure 54. Bar Chart element detail view
Categorized Data
The Category DataSet is created based on two Fields from the Document Model so that each value
has a label.
70

-- 70 of 83 --

Field with value
Specify the Field from the selected Document Model that contains category values.
Field for labels
Select the label Field which is used for the categorization process.
The result is a Dataset which includes each unique Field for labels value. The Field with value data
will be created by summing up the value fields with matching labels.
Example
The Document Model contains a repeatable Group, Person, with a Number Field, income and a String
Field occupation. The following Data is modeled:
Table 9. Categorized Data Example
Field with value Field for labels
root/Person/income root/Person/occupation
Based on the following data in the Document:
Person[0]:
occupation: "A"
income: 1
Person[1]:
occupation: "A"
income: 2
Person[2]:
occupation: "B"
income: 1
The Dataset consists of two data points, one for each unique value of occupation. The Value of the
datapoint is the Sum of all the Field Values with this label:
A : 3
B : 1
Pie Chart
Displays a pie distribution chart based on a single set of Categorized Data.
Use numeration as label
Select "Use numeration as label" If the data shouldn’t be grouped. Each Value will be represented
by its own with a unique incrementing number being its label.
NOTE "Field for labels" may be left blank when "Use numeration as label" is selected.
71

-- 71 of 83 --

Figure 55. Pie Chart element detail view
Bounding Box
The Bounding Box element provides the ability to group elements in a specific region. The
Bounding Box functions analog to the Area, except that it does not grow with its contents and
provides no option to set a data context.
The properties of the Bounding Box element are determined by its Border Properties.
To access the Bounding Box Stage View, hover over the Bounding Box and click the revealed "Edit"
button in the top right-hand corner of the Bounding Box.
Bounding Box Stage View
The Bounding Box Stage View functions similar to the Editor Stage.
The main difference between these Editor Stage Views are:
72

-- 72 of 83 --

• The Bounding Box Stage View has the dimensions of the corresponding Bounding Box.
• The Bounding Box Stage View displays the borders of the corresponding Bounding Box.
Elements inside a Bounding Box are positioned in relation to the Bounding Boxes upper left corner.
Leave the Bounding Box Stage View by clicking on the desired level in the Breadcrumb navigation
displayed in the top left-hand corner.
NOTE
• The positioning of the Elements inside a Bounding Box is unaffected of the
Bounding Box border. Therefore, the Elements and the Bounding Box border
may overlap.
• The height calculation and positioning of the Elements inside the Bounding Box
are done independently of Elements outside the Bounding Box.
Figure 56. Bounding Box Stage View
Area
The Area element provides the ability to group elements in a specific region. Once created, the Area
content can be edited on a separate Area stage.
Layout calculations of the Area content are done separately to the elements outside the Area. This
way, the Area with its content interacts exactly as one single element. If the Area content grows
outside the Area, the Area grows to fit its content.
Repeatability Settings
Document Model
Select a Document Model referenced in the Schema Tab.
Group
A Group can be selected as a data context in the Area configuration. If the selected group is
repeatable, then the Area will be repeated for each instance of the repeatable group in the
provided A12 Document.
73

-- 73 of 83 --

The Area element is also configurable using Border Properties.
Figure 57. Area element detail view
To access the Area Stage View, hover over the Area and click the revealed "Edit" button in the top
right-hand corner of the Area.
Area Stage View
The Area Stage functions similar to the Editor Stage.
The main difference between these Editor Stage Views are:
• The Area Stage has the dimensions of the corresponding Area
• The Area Stage displays the borders of the corresponding Area
• At the lower border of the Area Stage, an extension point is accessible, which can be dragged to
define the Overflow Height of the Area.
Elements inside an Area are positioned in relation to the Areas upper left corner.
74

-- 74 of 83 --

Leave the Area Stage View by clicking on the desired level in the Breadcrumb navigation displayed
in the top left-hand corner.
NOTE
• The positioning of the Elements inside an Area is unaffected of the Area border.
Therefore, the Elements and the Area border may overlap.
• The height calculation and positioning of the Elements inside the Area are done
independently of Elements outside the Area.
Figure 58. Area Stage View
Overflow Height
The height of the Area Stage can be increased by the definition of an Overflow Height. Drag-and-
drop the lower border of the Area to edit the Overflow Height. The Overflow Height allows the user
to place additional Elements inside the Area.
TIP This works well in combination with the usage of Hide Conditions.
NOTE The distance between the lowest Element inside the Area to its lower bounds will
stay always the same.
Switch
The Switch element is a container element similar to Area and BoundingBox. It provides the ability
to group elements in a specific region.
75

-- 75 of 83 --

This is an advanced container that dynamically controls the visibility of enclosed Area elements
based on specified preconditions.
To access the Switch Cases, hover over the Area and click the revealed "Edit" button in the top right-
hand corner of the Area.
CAUTION It is not allowed to set Border Properties on the Switch and a DataContext on the
Area Case.
Figure 59. Switch element properties
Switch Cases
Preconditions and associated Area Elements are added by clicking on "Add". These preconditions
are written as Computations in the Kernel Language syntax.
Leave the Switch Cases View by clicking on the desired level in the Breadcrumb navigation
displayed in the top left-hand corner.
Precondition
When a precondition evaluates to True, the corresponding Area element becomes visible as the
same Switch position.
If the precondition evaluates to False, the Area element is hidden.
If all preconditions evaluate to False, the Switch is hidden as well.
NOTE It is possible that there are multiple cases visible at the same time.
Elements
An Area with the dimensions of the Switch is created for each Precondition. Refer to the Area
Element for more details on how to edit these.
76

-- 76 of 83 --

Figure 60. Switch cases detail
print-shell
This chapter covers the print-shell. This is a command line interface which allows to use some
handy tools of the Print Engine.
Prerequisites
To run the print-shell, Java 21 must be installed and available from the command line.
Start print-shell
The print-shell must be started in the command line with the command:
java -jar print-shell-[PrintEngineVersion].jar
The print-shell is a handy tool, that allows you to use the following commands:
Comparison of Two PDF-Documents.
compare -1 pdf1 -2 pdf2 -p percent
• pdf1: Name of the first PDF-document
• pdf2: Name of the second PDF-document
• percent: The percent of the allowing pixels to be different for comparison. Valid values range
77

-- 77 of 83 --

from 0 to 100. The default value is 0
Comparison of All PDF-Documents With the Same Name.
compare-all -w workspacePath -p percent --equal equal
• workspacePath: Path to the workspace containing the PDF-documents.
• percent: The percent of the allowing pixels to be different for comparison. Valid values range
from 0 to 100. The default value is 0
• equal: This defines how the matched PDF files are found. By default the command is checking
for startWith. If this parameter is set to true the file name of the PDF files need to be completely
equal.
Printing a Specific Print Model.
print -w workspacePath -p printModelId -d documentId -t timeZone --locale localeString
--suffix suffix
• workspacePath: Path to the workspace containing the Print Models.
• printModelId: ID of the Print Model used for the print operation.
• documentId: ID of the A12 Document used for the print operation. Example: For document file
DomainDocument-1 the ID is 1.
• timeZone: Timezone for the print operation. Example: GMT
• localeString: Locale to use for the print operation. Example: de
• suffix: The suffix is attached separated with a '-' on the final PDF file name
Printing All Print Models in the Current Workspace.
print-all -w workspacePath -t timeZone --locale localeString --suffix suffix --filter
filter
• workspacePath: Path to the workspace containing the Print Models.
• timeZone: Timezone for the print operation. Example: GMT
• localeString: Locale to use for the print operation. Example: de
• suffix: The suffix is attached separated with a '-' on the final PDF file name
• filter: With the filter option it is possible to define a Regex to print only Print Models with IDs
that match. If empty, all Print models in the workspace are printed.
Attachment File Resolution
When a Print Model references an attachment, the print-shell resolves it by searching the
78

-- 78 of 83 --

workspace for a file whose base name (filename without extension) matches the attachment ID
defined in the Print Model. Only PDF and image files are supported as attachments.
Example
If the Print Model references attachment ID myReport, place a file named myReport.pdf in the
workspace directory. The file will be loaded and embedded as the attachment during printing.
Typesetting
This chapter instructs how to customize the typesetting of text within the Print Engine and is
intended for modelers familiar with Print Modeling. Typesetting controls the composition and flow
of text when a document is printed.
Typesetting Model Configuration
The Print Engine has a dedicated Typesetting Model that allows you to add customizable rules to
the setting of your text.
Figure 61. Typesetting Editor
You can create a new Typesetting Model in the SME workspace and edit it by selecting it. You can
create, edit, and reference multiple Typesetting Models per workspace.
To affect your text, follow these steps:
1. Create, edit, and save a Typesetting Model.
2. Reference the Typesetting Model in the Schema tab of your Print Model.
79

-- 79 of 83 --

Figure 62. Referencing Typesetting Model
3. Create a Text Style and reference the Typesetting Model.
Figure 63. Text Style with Typesetting Model
4. You can now apply the custom Text Style that you created to your Print Model Elements.
Prevent Line Break Rule Settings
These settings allow you to define exceptions to hyphenation and line wrapping when text is
printed with the Print Engine.
Character Sequence Rules
This option allows you to define specific words or character sequences that should not be
hyphenated.
To add a new character sequence, click the btn:[Add] button. A new row will be added to a table,
where you can enter the desired sequence. Do note that only hyphens (-) and alphabetical
characters are allowed. Other characters like numbers will prompt an error message and prevent
you from saving the model.
To remove a rule press the button on the right of the row and confirm your choice in the dialog.
80

-- 80 of 83 --

Figure 64. Character Sequence Rules
Number Unit Rules
This option allows you to define character strings that will be treated as units of measurement.
They will not be line wrapped if preceded by a number, which means the number and unit will stay
together on the same line.
To add a new unit rule, click the btn:[Add] button. A new row will be added into a table, where you
can enter the desired unit.
To remove a rule press the button on the right of the row and confirm your choice in the dialog.
Figure 65. Number Unit Rules
WARNING
While Number Unit Rules allow usage of a wide selection of possible symbols,
they may not be supported by the font of the selected Text Style. In this case the
Print Model can not be printed and will show an error in the print preview.
Special Pattern Rules
This option allows you to apply a set of curated rules that were designed for special use cases.
Currently, there are two special patterns available:
• §{digit} Abs. {digit} - e.g. §3 Abs. 7 for referencing german legal texts
• {digit}({digit})({letter}) - e.g. 219(1)(a) for referring to document sections
If you require different special patterns that cannot be covered by any of the preceding options,
please contact the Print Engine Team to implement a new Special Pattern Rule.
81

-- 81 of 83 --

Figure 66. Special Pattern Rules
Orphan and Widow Settings
In typesetting, "orphan" and "widow" are terms that define how text at the start or end of a page is
treated in regard to the visual flow of the text. Proper management of widows and orphans results
in professional-looking documents and prevents isolated lines from disrupting the reader’s flow. As
definitions vary, the Typesetting Model defines them as follows:
• Widows: How many isolated lines are allowed at the start of a page (Default: 2)
• Orphans: How many isolated lines are allowed at the end of a page (Default: 2)
These settings allow the modeler to define their custom limits for Orphans and Widows for their
texts.
Figure 67. Widow and Orphan Settings.
Figure 68. Widow and Orphan Example (Default limit).
82

-- 82 of 83 --

Figure 69. Orphan and Widow Example with limit set to 5.
Roles
The roles table allows you to define the allowed roles for the Typesetting Model.
You can select roles from the workspace by clicking the btn:[Add] and selecting an option from the
dropdown. Roles can be deleted by clicking the red button in the row and confirming your choice.
83

-- 83 of 83 --

