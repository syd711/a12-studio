# print_engine print technical documentation

Print Engine
Table of Contents
Introduction . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
Who Should Read This Document? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
APIs . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
Print Engine API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
PrintJob . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
JobManager . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
PrintEngine . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
PdfBoxPrintEngine . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
PrintModelId. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
PrintResult. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 9
PdfPrintResult . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
PrintMessageReport . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 10
StaticImageProvider . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
PdfBoxPrintEngineConfig . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
Print Engine Runtime . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
PrintJobManager . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
PrintJobManagerApi . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
PdfBoxPrintEngine . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
KernelDocumentV2Provider. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
TypesettingModelProvider . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
AttachmentProvider . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 19
PageRangeRestriction. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 20
Print Model API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
PrintModelMarshaller (TypeScript) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
Deserializing a Print Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
Serializing a Print Model. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
Validation Options . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
MarshallerResult. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 23
IPrintModelValidator (Java) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
Validating with a Locale . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
Validating with Options . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
Integration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 25
Dependency. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 25
Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
Direct Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
Creating the PrintJobManagerApi. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
1

-- 1 of 52 --

Creating the ExecutorService. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 26
Creating PrintJobManager and PrintEngine . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
Using the @Bean Annotation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
Usage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
Creating a PrintJob . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
AttachmentProvider. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
PageRangeRestriction . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
Executing a PrintJob . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
Integration Xml PrintEngine . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
Additional Dependency . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Usage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Choosing the correct Xml Engine. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Usage of the relevant Classes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Print Model Editor Light . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 31
Dependency. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
Usage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
Using the PrintModelEditorLight Component . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
Props . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 33
Differences Compared to the Simple Model Editor . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Breaking Change Management . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Definition of Version . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Definition / distinction of different APIs . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Breaking Changes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Migration Instructions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
2026.06 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
General Information. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Removals . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Deep-Level Imports . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Model API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Print Shell . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Engine Api. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Engine Runtime . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Print Model Api Utils . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 38
Print Setting Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 38
Embedded Image Attachments . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 38
Changes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 39
Engine Runtime . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 39
Print Shell . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 39
Model API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 39
Print Model Api Utils . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
Fixes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
2

-- 2 of 52 --

Introduction
The Print Engine is a Java library used to generate fully functional PDF-documents from A12 Print
Models, created with the A12 Print Model Editor.
2025.06-ext5. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
Deprecations. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
Print Model Api Utils . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
Print Model Api . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
2025.06-ext4. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
Deprecations. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
Print Shell . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
Legacy Rendering Mode . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
Java . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
TypeScript . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 42
Limitations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
2025.06-ext2. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Deprecations. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Java . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Migration from PdfPrintEngine to PdfBoxPrintEngine . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
2025.06 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
Breaking Changes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
Input Value Source . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
Move the description to the header . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
Roles Annotations in the SME are now separated by comma instead of semicolon . . . . . . . . 46
Move the Marshaller to model-api-utils package and validating model references . . . . . . . . 47
Move serialization package to model-api-utils module . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
Deprecation. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
Java . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
Migration Tool . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
For Print Model Version 2.1.0 and Later . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
Migrating Embedded Images (since version 4.0.0) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 48
For Print Model Version Earlier Than 2.1.0. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 49
Step 1: Migrate Model to Version 2.1.0 Using Java Based Tool . . . . . . . . . . . . . . . . . . . . . . . . . . . . 49
Step 2: Migrate Model to Current Version Using Node Based Tool . . . . . . . . . . . . . . . . . . . . . . . . 50
For Print Setting Model (upgrading to Print Engine 4.0.0). . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
Manual steps after migrating the Print Setting Model. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 51
Codemod . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 51
How to Use the Codemod. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 51
3

-- 3 of 52 --

Who Should Read This Document?
This document serves as a reference for integrating the Print Engine into an existing Java
application. It is therefore intended for developers who work with a Java application that handles
data from the A12 Platform to generate PDF-documents. This document is not intended as a manual
for the usage of the Print Model Editor.
APIs
This chapter covers the APIs provided by the Print Engine.
Print Engine API
This section covers the Print Engine API, a package that provides the core abstraction and API for
the usage of the Print Engine in a Java application.
PrintJob
A PrintJob is a job description for the Print Engine that executes a specific print operation for a
PrintModel. The properties locale and timezone are needed for the localization of language specific
content and dates.
PrintJob interface
package com.mgmtp.a12.print.engine.api;
import com.mgmtp.a12.print.engine.api.exception.PrintException;
import com.mgmtp.a12.print.engine.api.exception.PrintJobConfigurationException;
import lombok.NonNull;
import java.util.Locale;
import java.util.TimeZone;
import com.mgmtp.a12.model.utils.OnlyForUsage;
/**
* A job description for the PrintEngine to execute the print operation for a
particular {@link com.mgmtp.a12.print.model.api.model.PrintModel}.
*/
@OnlyForUsage
public interface PrintJob {
PrintModelId getPrintModelId();
PrintJob withProvider(@NonNull JobDependencyProvider provider) throws
PrintException;
PrintJob withLocale(@NonNull Locale locale);
4

-- 4 of 52 --

PrintJob withTimeZone(@NonNull TimeZone timeZone);
PrintJob withRestriction(@NonNull JobRestriction restriction);
@NonNull Locale getLocale() throws PrintJobConfigurationException;
@NonNull TimeZone getTimeZone() throws PrintJobConfigurationException;
}
JobManager
The JobManager is an interface for the creation of new PrintJobs for a given Print Model ID and the
preparation of the Print Models. Preparing a Print Model optimizes the Print Model beforehand,
analyzing its Calculations, Conditions and other dynamic elements to optimize the print operations
that use the given Print Model.
It is also possible to get a PrintMessageReport as result, which catches the semanticall issues during
the preparation of the Print Models.
NOTE
The preparation is necessary for the execution of a PrintJob, but needs to be only
done once for each Print Model. If not done manually beforehand, it will be
automatically done before the start of the first PrintJob of a given Print Model. To
optimize the calculation time of a PrintJob, is recommended to prepare all
necessary Print Models manually before the PrintJob is started.
JobManager interface
package com.mgmtp.a12.print.engine.api;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.print.engine.api.exception.impl.PrintCompilerException;
import com.mgmtp.a12.print.engine.api.message.PrintMessageReport;
import com.mgmtp.a12.print.model.api.model.PrintModel;
/**
* Interface for the creation of new {@link PrintJob}s for a given {@link
PrintModelId} and the preparation of {@link PrintModel}s.
*/
@OnlyForUsage
public interface JobManager {
/**
* Prepare needs to be called once when the Print Model was added or changed
during the lifetime of the manager.
* However, doing so every print is not required and will result in server
performance penalties.
* Throws a {@link PrintCompilerException} for issues during compilation.
5

-- 5 of 52 --

*
* @param printModel Print Model content to be printed
*/
PrintModelId prepare(String printModel) throws PrintCompilerException;
/**
* Prepare needs to be called once when the Print Model was added or changed
during the lifetime of the manager.
* However, doing so every print is not required and will result in server
performance penalties.
* <p>
* Issues during compilation are captured as {@link
com.mgmtp.a12.print.engine.api.message.PrintMessage}s
* in the returned {@link PrintMessageReport} rather than being thrown.
*
* @param printModel Print Model content to be printed
*/
PrintMessageReport<PrintModelId> prepareWithReport(String printModel) throws
PrintCompilerException;
/**
* Create a new {@link PrintJob} from the given {@link PrintModelId}.
* Compiles the Print Model if it has not already been compiled.
* Throws a {@link PrintCompilerException} for issues during {@link PrintJob}
creation
*
* @param printModelId Print Model ID
*/
PrintJob createNewJob(PrintModelId printModelId) throws PrintCompilerException;
/**
* Create a new {@link PrintJob} from the given {@link PrintModelId}.
* Compiles the Print Model if it has not already been compiled.
* <p>
* Issues during {@link PrintJob} creation are captured as {@link
com.mgmtp.a12.print.engine.api.message.PrintMessage}s
* in the returned {@link PrintMessageReport} rather than being thrown.
*
* @param printModelId Print Model ID
*/
PrintMessageReport<PrintJob> createNewJobWithReport(PrintModelId printModelId)
throws PrintCompilerException;
}
PrintEngine
The PrintEngine is an interface for the execution of a given PrintJob, that returns a PrintResult.
It is also possible to get a PrintMessageReport as result, which catches the semanticall issues during
the PrintJob execution.
6

-- 6 of 52 --

PrintEngine interface
package com.mgmtp.a12.print.engine.api;
import com.mgmtp.a12.print.engine.api.exception.PrintException;
import com.mgmtp.a12.print.engine.api.message.PrintMessageReport;
import com.mgmtp.a12.model.utils.OnlyForUsage;
/**
* Interface for the execution of a given {@link PrintJob}, that returns a {@link
PrintResult}.
*/
@OnlyForUsage
public interface PrintEngine<Result extends PrintResult> {
/**
* Executing the given PrintJob.
* Throws a {@link PrintException} for issues during print.
*/
Result execute(PrintJob printJob) throws PrintException;
/**
* Executing the given PrintJob.
* <p>
* Issues during print are captured as {@link
com.mgmtp.a12.print.engine.api.message.PrintMessage}s
* in the returned {@link PrintMessageReport} rather than being thrown.
*/
PrintMessageReport<Result> executeWithReport(PrintJob printJob) throws
PrintException;
/**
* Get general config of the current job
*/
PdfBoxPrintEngineConfig getConfig();
}
PdfBoxPrintEngine
The PdfBoxPrintEngine is an interface for the execution of a given PrintJob, that returns a
PdfPrintResult.
PdfBoxPrintEngine interface
package com.mgmtp.a12.print.engine.api;
import com.mgmtp.a12.model.utils.OnlyForUsage;
/**
7

-- 7 of 52 --

* Interface for the execution of a given {@link PrintJob}, that returns a {@link
PdfPrintResult}.
*/
@OnlyForUsage
public interface PdfBoxPrintEngine extends PrintEngine<PdfPrintResult> {
}
PrintModelId
PrintModelId interface
package com.mgmtp.a12.print.engine.api;
import com.mgmtp.a12.print.engine.api.exception.PrintException;
import com.mgmtp.a12.model.utils.OnlyForUsage;
/**
* The interface PrintModel Identifier
*/
@OnlyForUsage
public interface PrintModelId {
/**
* Gets the String that is used as id in the PrintModel JSON File
*
* @return the model header id
*/
String getModelHeaderId();
/**
* From string print model id.
*
* @param printModelHeaderId the print model header id
* @return the print model id
* @throws PrintException if the provided String is null empty or blank
*/
static PrintModelId fromString(final String printModelHeaderId) throws
PrintException {
if(printModelHeaderId == null) {
throw new PrintException("The provided printModelHeaderId is null");
}
if(printModelHeaderId.isBlank()) {
throw new PrintException("The provided printModelHeaderId is blank");
}
return new PrintModelId() {
@Override
public String getModelHeaderId() {
return printModelHeaderId;
8

-- 8 of 52 --

}
@Override
public int hashCode() {
return getModelHeaderId().hashCode();
}
@Override
public boolean equals(Object obj) {
if (obj instanceof PrintModelId) {
return getModelHeaderId().equals(
((PrintModelId) obj).getModelHeaderId()
);
} else {
return false;
}
}
@Override
protected Object clone() {
return PrintModelId.fromString(getModelHeaderId());
}
@Override
public String toString() {
return getModelHeaderId();
}
};
}
}
PrintResult
A PrintResult is a general interface for the results of the PrintEngine. It provides the option to copy
the resulting PDF directly into the serverless stream instead of copying the resulting data from
buffer to buffer.
PrintResult interface
package com.mgmtp.a12.print.engine.api;
import java.io.IOException;
import java.io.OutputStream;
import com.mgmtp.a12.model.utils.OnlyForUsage;
/**
* General interface for the results of the {@link PrintEngine}.
*/
@OnlyForUsage
public interface PrintResult {
9

-- 9 of 52 --

/**
* The content type of the resulting PDF
*/
String getContentType();
/**
* Copy the resulting PDF to the provided outputStream.
*
* @param outputStream The target stream
*/
void copyTo(OutputStream outputStream) throws IOException;
}
PdfPrintResult
A PdfPrintResult is an interface for a PDF-based PrintResult.
PdfPrintResult interface
package com.mgmtp.a12.print.engine.api;
import com.mgmtp.a12.model.utils.OnlyForUsage;
/**
* Interface for PDF-based {@link PrintResult}.
*/
@OnlyForUsage
public interface PdfPrintResult extends PrintResult {
static PdfPrintResult empty() {
return e -> {};
};
@Override
default String getContentType() {
return "application/pdf";
}
}
PrintMessageReport
A PrintMessageReport is an interface for a report, which includes a result. The report itself has a list
of messages. A message can be an error or a warning and contains issues that occurred during the
printing or precompilation process.
PrintMessageReport interface
package com.mgmtp.a12.print.engine.api.message;
10

-- 10 of 52 --

import com.mgmtp.a12.print.engine.api.exception.PrintException;
import java.util.List;
import com.mgmtp.a12.model.utils.OnlyForUsage;
/**
* A report of messages collected during a print operation.
*/
@OnlyForUsage
public interface PrintMessageReport<T> {
/**
* @throws PrintException if there are error messages
*/
T getResult() throws PrintException;
/**
* @return true if no {@link PrintMessage.Severity#ERROR} messages were collected.
*/
default boolean noErrorOccurred() {
return getMessages().stream().noneMatch(m -> m.getSeverity() ==
PrintMessage.Severity.ERROR);
}
/**
* @return all collected messages (warnings and errors), empty if none occurred.
*/
List<PrintMessage> getMessages();
}
StaticImageProvider
StaticImageProvider is an interface for resolving raw image bytes for static attachment images
referenced by a Print Model. Implementations are called once per unique filename during the
JobManager prepare (compile) phase and must be thread-safe.
StaticImageProvider interface
package com.mgmtp.a12.print.engine.api;
import com.mgmtp.a12.print.engine.api.exception.impl.PrintCompilerException;
import com.mgmtp.a12.print.engine.api.exception.StaticImageNotFoundException;
import java.io.UncheckedIOException;
/**
* Resolves raw image bytes for static attachment images referenced by a Print Model.
*
* <p>Implementations are called once per unique {@code internalFilename} during
11

-- 11 of 52 --

* {@link JobManager#prepare(String)} (the compile phase). They must be thread-safe.
*
* <p>Throw {@link StaticImageNotFoundException} when a file cannot be located so that
the
* precompiler can collect <em>all</em> missing filenames and surface them in a single
* {@link PrintCompilerException} rather than failing on the first miss.
*/
public interface StaticImageProvider {
/**
* Loads the raw bytes of the static image identified by {@code internalFilename}.
*
* @param internalFilename the resource name of the static image as referenced in
the Print Model
* @return the image bytes
* @throws StaticImageNotFoundException if the image cannot be located
* @throws UncheckedIOException if an I/O error occurs while reading the image
data
*/
byte[] loadStaticImage(String internalFilename) throws
StaticImageNotFoundException;
}
PdfBoxPrintEngineConfig
The PdfBoxPrintEngineConfig provides options for the configuration of the Print Engine. It currently
only allows integrating custom fonts.
PdfBoxPrintEngineConfig interface
package com.mgmtp.a12.print.engine.api;
import lombok.*;
import java.util.Map;
import com.mgmtp.a12.model.utils.OnlyForUsage;
/**
* Configures the behavior of the PrintEngine.
* Allows integrating custom fonts and setting a default font.
*/
@OnlyForUsage
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PdfBoxPrintEngineConfig {
public static final String DEFAULT_FONT_KEY = "default";
public static final String DEFAULT_TEXT_STYLE_FONT_KEY = "Open Sans";
12

-- 12 of 52 --

private static final String DEFAULT_FONT = "classpath:fonts/OpenSans-Regular.ttf";
public static final Map<String, String> DEFAULT_FONTS = Map.of(
DEFAULT_FONT_KEY, DEFAULT_FONT,
DEFAULT_TEXT_STYLE_FONT_KEY, DEFAULT_FONT,
"Noto Sans Mono", "classpath:fonts/NotoSansMono-Regular.ttf",
"Noto Sans Symbols", "classpath:fonts/NotoSansSymbols2-Regular.ttf"
);
public static final PdfBoxPrintEngineConfig DEFAULT = new
PdfBoxPrintEngineConfig(DEFAULT_FONTS);
/**
* Map of available fonts. Each font is registered with a key which can be used in
the print model. As value a valid
* classpath ('classpath:/') resource of external resource ('file:/') has to be
set (e.g.
* <code>classpath:/fonts/arial.ttf</code>)
*/
protected Map<String, String> availableFonts;
}
Print Engine Runtime
This section covers the API for the Print Engine Runtime, a package that provides the core
implementation behind the interfaces of the Print Engine API.
PrintJobManager
The PrintJobManager is an implementation of the JobManager, that facilitates the creation of
PrintJob and the preparation of Print Models. An optional StaticImageProvider can be supplied to
the constructor to resolve static attachment images during the compile phase.
PrintJobManager implementation
/**
* Used to create {@link PrintJob}s and compile {@link
com.mgmtp.a12.print.model.api.model.PrintModel}s
*/
@OnlyForUsage
public class PrintJobManager implements JobManager {
private final PrintModelCompilerRuntime compiler;
public PrintJobManager(
@NonNull ExecutorService executorService,
@NonNull PrintJobManagerApi api,
@NonNull PrintJobConfig printJobConfig
) {
this.compiler = new PrintModelCompilerRuntime(executorService, api,
13

-- 13 of 52 --

printJobConfig);
}
public PrintJobManager(
@NonNull ExecutorService executorService,
@NonNull PrintJobManagerApi api,
@NonNull PrintJobConfig printJobConfig,
@NonNull StaticImageProvider staticImageProvider
) {
this.compiler = new PrintModelCompilerRuntime(executorService, api,
printJobConfig, staticImageProvider);
}
@Override
public PrintMessageReport<PrintModelId> prepareWithReport(@NonNull String
printModel) {
return PrintMessageReportImpl.wrapException(() -> {
PrintModelId id = compiler.compile(printModel).getId();
return new PrintMessageReportImpl<>(id,
PrintMessageCollector.getMessages());
}, PrintCompilerException.class, PrintCompilerException::new);
}
/**
* Compiles a {@link com.mgmtp.a12.print.model.api.model.PrintModel}.
*/
@Override
public PrintModelId prepare(String printModel) throws PrintCompilerException {
final var prepareResult = prepareWithReport(printModel);
if (prepareResult.noErrorOccurred()) {
return prepareResult.getResult();
}
throw new PrintCompilerException(
"The Print Model prepare failed with the following messages: {}",
StringUtils.join(prepareResult.getMessages(), "\n")
);
}
@Override
public PrintMessageReport<PrintJob> createNewJobWithReport(@NonNull PrintModelId
printModelId) {
return PrintMessageReportImpl.wrapException(() -> {
final var printJob = ManagedPrintJob.builder()
.printModelCompilationContext(getCompiledPrintModel(printModelId))
.build();
printJob.withProvider(PrintModelProvider.fromLoader(this::getCompiledPrintModel));
return new PrintMessageReportImpl<>(printJob,
PrintMessageCollector.getMessages());
}, PrintCompilerException.class, PrintCompilerException::new);
}
14

-- 14 of 52 --

@Override
public PrintJob createNewJob(PrintModelId printModelId) throws
PrintCompilerException {
final var result = createNewJobWithReport(printModelId);
if (result.noErrorOccurred()) {
return result.getResult();
}
throw new PrintCompilerException(
"The Print Job creation failed with the following messages: {}",
StringUtils.join(result.getMessages(), "\n")
);
}
private PrintModelCompilationContext getCompiledPrintModel(@NonNull PrintModelId
printModelId) {
final var context = compiler.get(printModelId);
return context.orElseThrow(
() -> new PrintCompilerException("PrintModel {} is not prepared.",
printModelId)
).awaitCompilation();
}
}
PrintJobManagerApi
The PrintJobManagerApi provides interfaces for the loading of Print Models and Document Models
by given their IDs.
PrintJobManagerApi interface
/**
* Provides interfaces that load print and document models by given IDs.
*/
public interface PrintJobManagerApi {
/**
* Load the Print model content by ID
*/
String loadPrintModel(String id);
/**
* Load the document model structure by ID
*
* @param id document model ID
*/
IDocumentModel loadDocumentModel(String id);
15

-- 15 of 52 --

}
PdfBoxPrintEngine
The PdfBoxPrintEngine is an implementation of PdfBoxPrintEngine, that provides the ability to
execute PrintJobs. This engine is able to work with Typesetting Models, which can be provided with
the TypesettingModelProvider.
KernelDocumentV2Provider
KernelDocumentV2Provider is an interface for loading A12 Kernel DocumentV2.
KernelDocumentV2Provider interface
package com.mgmtp.a12.print.engine.runtime;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.print.engine.api.JobDependency;
import com.mgmtp.a12.print.engine.api.JobDependencyProvider;
import com.mgmtp.a12.print.engine.api.a12.DocumentDependencyDescriptor;
import com.mgmtp.a12.print.engine.api.exception.PrintException;
import com.mgmtp.a12.print.engine.runtime.internal.KernelDocumentJobDependency;
import lombok.NonNull;
/**
* Interface for loading A12 kernel {@link DocumentV2}s.
*/
public interface KernelDocumentV2Provider extends JobDependencyProvider {
static KernelDocumentV2Provider fromDocument(final @NonNull DocumentV2 document) {
return new KernelDocumentV2Provider() {
@Override
public boolean supports(DocumentDependencyDescriptor
documentDependencyDescriptor) {
return
documentDependencyDescriptor.getModelReference().getReference().equals(document.getDoc
umentModelId());
}
@Override
public DocumentV2 loadDocument(DocumentDependencyDescriptor descriptor) {
return document;
}
};
}
/**
* @return true, if the {@link com.mgmtp.a12.model.header.ModelReference} is
16

-- 16 of 52 --

supported by the provider.
*/
boolean supports(DocumentDependencyDescriptor documentDependencyDescriptor);
/**
* @return The supported document
*/
DocumentV2 loadDocument(DocumentDependencyDescriptor descriptor);
/**
* @return true, if the {@link com.mgmtp.a12.model.header.ModelReference} needed
by the {@link JobDependency} is supported.
*/
default boolean canProvide(JobDependency dependency) {
if (!(dependency instanceof KernelDocumentJobDependency
kernelDocumentDataDependency)) {
return false;
}
var descriptor = kernelDocumentDataDependency.getDescriptor();
return supports(descriptor);
}
@Override
default void provide(JobDependency dependency) throws PrintException {
var kernelDocumentDataDependency = ((KernelDocumentJobDependency) dependency);
final var documentV2 =
loadDocument(kernelDocumentDataDependency.getDescriptor());
kernelDocumentDataDependency.setDocument(documentV2);
}
}
TypesettingModelProvider
TypesettingModelProvider is an interface for loading Typesetting Models. They can be created within
the SME (Typesetting Model Configuration).
TypesettingModelProvider interface
package com.mgmtp.a12.print.engine.runtime;
import com.mgmtp.a12.print.engine.api.JobDependency;
import com.mgmtp.a12.print.engine.api.JobDependencyProvider;
import com.mgmtp.a12.print.engine.api.a12.TypesettingModelDependencyDescriptor;
import com.mgmtp.a12.print.engine.api.exception.PrintException;
import com.mgmtp.a12.print.engine.runtime.internal.TypesettingModelJobDependency;
import com.mgmtp.a12.print.typesetting.internal.model.TypesettingModel;
import lombok.NonNull;
import java.util.function.Function;
17

-- 17 of 52 --

/**
* Interface for loading {@link TypesettingModel}s.
*/
public interface TypesettingModelProvider extends JobDependencyProvider {
static TypesettingModelProvider fromLoader(final @NonNull Function<String,
TypesettingModel> loading) {
return new TypesettingModelProvider() {
@Override
public boolean supports(TypesettingModelDependencyDescriptor
typesettingModelDependencyDescriptor) {
return true;
}
@Override
public TypesettingModel
loadTypesettingModel(TypesettingModelDependencyDescriptor descriptor) {
return loading.apply(descriptor.getTypesettingModelId());
}
};
}
/**
* @return true, if the {@link TypesettingModel} referenced by the ID is supported
by the provider.
*/
boolean supports(TypesettingModelDependencyDescriptor
typesettingModelDependencyDescriptor);
TypesettingModel loadTypesettingModel(TypesettingModelDependencyDescriptor
descriptor);
/**
* @return true, if the {@link TypesettingModel} needed by the {@link
JobDependency}.
*/
default boolean canProvide(JobDependency dependency) {
if (!(dependency instanceof TypesettingModelJobDependency)) {
return false;
}
var typesettingModelJobDependency = (TypesettingModelJobDependency)
dependency;
var descriptor = typesettingModelJobDependency.getDescriptor();
return supports(descriptor);
}
@Override
default void provide(JobDependency dependency) throws PrintException {
var typesettingModelJobDependency = ((TypesettingModelJobDependency)
dependency);
18

-- 18 of 52 --

typesettingModelJobDependency.setTypesettingModel(loadTypesettingModel(typesettingMode
lJobDependency.getDescriptor()));
}
}
AttachmentProvider
AttachmentProvider is an interface for loading Attachments as ByteArrayInputStream.
AttachmentProvider interface
package com.mgmtp.a12.print.engine.runtime;
import com.mgmtp.a12.print.engine.api.JobDependency;
import com.mgmtp.a12.print.engine.api.JobDependencyProvider;
import com.mgmtp.a12.print.engine.api.a12.AttachmentDependencyDescriptor;
import com.mgmtp.a12.print.engine.api.exception.PrintException;
import com.mgmtp.a12.print.engine.runtime.internal.AttachmentJobDependency;
import lombok.NonNull;
import java.io.ByteArrayInputStream;
import java.util.function.Function;
/**
* Interface for loading A12 Attachments.
*/
public interface AttachmentProvider extends JobDependencyProvider {
static AttachmentProvider fromLoader(final @NonNull Function<String,
ByteArrayInputStream> loading) {
return new AttachmentProvider() {
@Override
public boolean supports(AttachmentDependencyDescriptor
attachmentDependencyDescriptor) {
return true;
}
@Override
public ByteArrayInputStream loadAttachment(AttachmentDependencyDescriptor
descriptor) {
return loading.apply(descriptor.getAttachmentId());
}
};
}
/**
* @return true, if the Attachment referenced by the ID is supported by the
provider.
*/
19

-- 19 of 52 --

boolean supports(AttachmentDependencyDescriptor attachmentDependencyDescriptor);
ByteArrayInputStream loadAttachment(AttachmentDependencyDescriptor descriptor);
/**
* @return true, if the Attachment needed by the {@link JobDependency}.
*/
default boolean canProvide(JobDependency dependency) {
if (!(dependency instanceof AttachmentJobDependency attachmentJobDependency))
{
return false;
}
final var descriptor = attachmentJobDependency.getDescriptor();
return supports(descriptor);
}
@Override
default void provide(JobDependency dependency) throws PrintException {
final var attachmentJobDependency = ((AttachmentJobDependency) dependency);
attachmentJobDependency.setAttachment(loadAttachment(attachmentJobDependency.getDescri
ptor()));
}
}
PageRangeRestriction
PageRangeRestriction is a JobRestriction, which is able to restrict the resulting pages in the PDF. The
user is able to use one of the auxiliary methods or the Builder to create a PageRangeRestriction.
A starting index must be defined. The end index can be defined and is then to be understood as
exclusive. If no end index is defined, all pages after the starting index are in the output PDF. The
indices are zero-based.
It is only possible to define one PageRangeRestriction
PageRangeRestriction
package com.mgmtp.a12.print.engine.api.restriction;
import com.mgmtp.a12.print.engine.api.JobRestriction;
import com.mgmtp.a12.print.engine.api.JobRestrictionContext;
import com.mgmtp.a12.print.engine.api.exception.PrintJobRestrictionException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.mgmtp.a12.model.utils.OnlyForUsage;
@OnlyForUsage
@Builder
20

-- 20 of 52 --

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class PageRangeRestriction implements JobRestriction {
private final Integer inclusiveStart;
private final Integer exclusiveEnd;
public static PageRangeRestriction of(final int inclusiveStart, final int
exclusiveEnd) {
if(inclusiveStart >= exclusiveEnd) {
throw new PrintJobRestrictionException("Range may not be empty");
}
if(exclusiveEnd < 1) {
throw new PrintJobRestrictionException("End must be greater than zero");
}
return new PageRangeRestriction(inclusiveStart, exclusiveEnd);
}
public static PageRangeRestriction skip(final int pageCount) {
if(pageCount < 1) {
throw new PrintJobRestrictionException("Page count must be greater than
zero");
}
return new PageRangeRestriction(pageCount, null);
}
public static PageRangeRestriction take(final int pageCount) {
if(pageCount < 1) {
throw new PrintJobRestrictionException("Page count must be greater than
zero");
}
return new PageRangeRestriction(0, pageCount);
}
public static PageRangeRestriction page(final int pageIndex) {
return new PageRangeRestriction(pageIndex, pageIndex + 1);
}
@Override
public void restrict(JobRestrictionContext context) {
if (context instanceof SinglePageRangeRestrictionContext) {
((SinglePageRangeRestrictionContext) context).setPageRange(inclusiveStart,
exclusiveEnd);
} else {
throw new PrintJobRestrictionException("The given context does not
implement SinglePageRangeRestrictionContext");
}
}
}
21

-- 21 of 52 --

Print Model API
This section covers the APIs for validating and marshalling Print Models.
PrintModelMarshaller (TypeScript)
The PrintModelMarshaller transforms a PrintModel between its TypeScript API representation and its
JSON-based DTO representation. Validation is performed automatically during serialization and
deserialization.
Import the marshaller from the @com.mgmtp.a12.print/print-model-api-utils package:
import { PrintModelMarshaller, PrintModelValidatorOptions } from
"@com.mgmtp.a12.print/print-model-api-utils/marshaller";
Deserializing a Print Model
Call deserialize to parse a raw JSON string into a PrintModel. The method returns a
MarshallerResult containing the deserialized model and a validation report.
const marshaller = new PrintModelMarshaller();
const { result, report } = marshaller.deserialize(rawJson);
if (report.noErrorOccurred) {
const printModel = result; // PrintModel
}
Serializing a Print Model
Call serialize to convert a PrintModel back to its JSON DTO representation.
const { result, report } = marshaller.serialize(printModel);
if (report.noErrorOccurred) {
const json = JSON.stringify(result); // JSON string
}
Validation Options
Both serialize and deserialize accept an optional PrintModelValidatorOptions object to control
validation behaviour.
interface PrintModelValidatorOptions {
/** Document Models to validate field and calculation references against. */
references?: {
documentModels?: readonly DocumentModel[];
22

-- 22 of 52 --

};
/** When true, HTML content in Text elements is validated. Default: true. */
html?: boolean;
/** Restricts validation to a subset of the model by entity instance paths. */
partial?: {
relevantPaths?: EntityInstancePath[];
};
}
Reference Validation
Pass the document models that the Print Model references to validate field paths and calculations:
const { result, report } = marshaller.deserialize(rawJson, {
references: { documentModels: [myDocumentModel] }
});
HTML Validation
Disable HTML validation if you want to skip checking HTML content:
const { result, report } = marshaller.deserialize(rawJson, { html: false });
const htmlMessages = report.errorMap[ErrorSeverity.ERROR]
.filter(e => e.origin === ErrorOrigin.HTML);
Errors indicate unsupported constructs that will not render correctly (e.g., malformed HTML,
invalid CSS color values). Warnings indicate constructs that are ignored by the Print Engine (e.g.,
unsupported tags or CSS properties).
Partial Validation
Restrict validation to a subset of the model by providing the entity instance paths of the changed
elements. This is useful for incremental validation in editors:
const { result, report } = marshaller.serialize(printModel, {
partial: { relevantPaths: [changedPath] }
});
MarshallerResult
The result object returned by both serialize and deserialize:
interface MarshallerResult<TModel, TDTO> {
/** The transformed model, or undefined if errors occurred. */
result?: TModel | TDTO;
23

-- 23 of 52 --

report: {
/** True if no errors occurred during serialization and validation. */
noErrorOccurred: boolean;
/** Validation messages grouped by severity (ERROR, WARNING, INFO). */
errorMap: DeepPartialErrorMap<TModel>;
/** The relevant paths used for partial validation. */
relevantPaths: EntityInstancePath[];
};
}
IPrintModelValidator (Java)
The IPrintModelValidator interface validates a raw JSON string representation of a PrintModel
against the current version of the model API. The default implementation is PrintModelValidator.
IPrintModelValidator validator = new PrintModelValidator();
Validating with a Locale
IPrintModelIntegrityReport report = validator.validate(rawJson, Locale.GERMAN);
if (report.noErrorOccurred()) {
// model is valid
}
for (IPrintModelIntegrityMessage message : report.getMessages()) {
System.out.println(message.getSeverityType() + ": " + message.getText());
}
Validating with Options
Use PrintModelValidatorOptions to configure validation features. HTML validation is enabled by
default:
PrintModelValidatorOptions options = new PrintModelValidatorOptions(Locale.GERMAN); //
html=true by default
IPrintModelIntegrityReport report = validator.validate(rawJson, options);
To disable HTML validation:
PrintModelValidatorOptions options = new PrintModelValidatorOptions(Locale.GERMAN,
false /* html */);
IPrintModelIntegrityReport report = validator.validate(rawJson, options);
When html is true, every Text element’s HTML content is validated. Errors are reported for
24

-- 24 of 52 --

malformed HTML or invalid CSS values (e.g., non-hex color values). Warnings are reported for
HTML tags or CSS properties that the Print Engine does not support.
HTML validation is enabled by default.
Integration
This chapter describes the integration of the Print Engine into a Java application.
Dependency
This section covers the dependencies necessary for integrating the Print Engine into a Maven or
Gradle application.
For the usage with Maven, the following dependencies need to be added in the applications POM-
file:
Maven plugin
<dependency>
<groupId>com.mgmtp.a12.print</groupId>
<artifactId>print-engine-api</artifactId>
<version>{printEngineVersion}</version>
</dependency>
<dependency>
<groupId>com.mgmtp.a12.print</groupId>
<artifactId>print-engine-runtime</artifactId>
<version>{printEngineVersion}</version>
</dependency>
For the usage with Gradle, the following dependencies need to be added in the applications
build.gradle file:
Gradle plugin
...
dependencies {
...
implementation("com.mgmtp.a12.print:print-engine-api:${printEngineVersion}")
implementation("com.mgmtp.a12.print:print-engine-runtime:${printEngineVersion}")
...
}
25

-- 25 of 52 --

Configuration
This section covers the core configurations of the Print Engine, necessary for printing valid PDF-
documents.
You can either create these configurations directly or if you are using Spring you can do so by using
the @Bean annotation in the Configuration class.
Direct Configuration
Creating the PrintJobManagerApi
First of all, we need to define the PrintJobManagerApi.
NOTE the PrintJobManagerApi may be called by any thread from the ExecutorService.
These threads will not carry a SecurityContext.
final var printJobManagerApi = new PrintJobManager.PrintJobManagerApi() {
@Override
public String loadPrintModel(String id) {
return findPrintModelContent(id); ①
}
@Override
public IDocumentModel loadDocumentModel(String id) {
return findDocumentModelById(id); ②
}
};
① This should be changed by your local service accordingly.
② This should be changed by your local service accordingly.
Creating the ExecutorService
The Print Engine Runtime provides a default implementation for the ExecutorService:
ExecutorServiceFactory.getInstance(Runtime.getRuntime().availableProcessors(), "print-
pool");
Alternatively, you can declare your own ExecutorService:
return new ForkJoinPool( ①
Runtime.getRuntime().availableProcessors(),
p -> {
final ForkJoinWorkerThread worker =
ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
worker.setName("print-pool-" + worker.getPoolIndex());
26

-- 26 of 52 --

return worker;
},
null,
true
);
Creating PrintJobManager and PrintEngine
Then we can create PrintJobManager with StaticImageProvider and PdfBoxPrintEngine.
final var printJobManager = new PrintJobManager(
printThreadPool,
printJobManagerApi,
PrintJobConfig.DEFAULT,
internalFilename -> {
byte[] imageBytes = loadStaticImageBytes(internalFilename);
if (imageBytes == null) {
throw new StaticImageNotFoundException(internalFilename);
}
return imageBytes;
});
final var printEngine = new PdfBoxPrintEngine(
printThreadPool,
new PdfBoxPrintEngineConfig(availableFonts)
);
The possibilities of the configs are described in PdfBoxPrintEngineConfig.
Using the @Bean Annotation
Example
Configuration class
public class PrintConfiguration {
public static final String PRINT_THREAD_POOL = "printThreadPool";
@Bean
@Qualifier(PRINT_THREAD_POOL)
public ExecutorService printThreadPool() {
return ExecutorServiceFactory.getInstance();
}
@Bean
public PrintJobManager.PrintJobManagerApi printJobManagerApi() {
return new PrintJobManager.PrintJobManagerApi() {
@Override
public String loadPrintModel(String id) {
// load print model content with the provided id ①
27

-- 27 of 52 --

}
@Override
public IDocumentModel loadDocumentModel(String id) {
// load document model with the provided id ②
}
};
}
@Bean
public PdfBoxPrintEngine pdfPrintEngine(@lombok.NonNull
@Qualifier(PRINT_THREAD_POOL) ExecutorService printThreadPool) {
return new com.mgmtp.a12.print.engine.runtime.pdfBox.PdfBoxPrintEngine(
printThreadPool,
PdfBoxPrintEngineConfig.DEFAULT.toBuilder()
.availableFonts(PrintEngineConfig.DEFAULT_FONTS) ③
.build()
);
}
}
① Load Print Model content. The implementation should be changed by your local service
accordingly.
② Load IDocumentModel. The implementation should be changed by your local service
accordingly.
③ Customization fonts could be set for the PdfBoxPrintEngineConfig.
Usage
Creating a PrintJob
Next step is the creation of a PrintJob, where document provider, locale, timezone are configured.
final var printJob = printJobManager.createNewJob(printModelPrepareId);
Locale parsedLocale = LocaleUtils.toLocale(locale);
printJob.withLocale(parsedLocale);
printJob.withTimeZone(
StringUtils.isBlank(timeZone)
? TimeZone.getDefault()
: TimeZone.getTimeZone(ZoneId.of(timeZone))
);
if (documentToPrint != null) {
printJob.withProvider(KernelDocumentV2Provider.fromDocument(documentToPrint)); ①
}
① This is the integration point for your own document provider. === TypesettingModelProvider
Within the TypesettingModelProvider it is possible to provide Typesetting Models to the
28

-- 28 of 52 --

PdfBoxPrintEngine.
printJob.withProvider(TypesettingModelProvider.fromLoader(id -> {
String rawContent = loadTypesettingModelContent(id);
return validateAndMarshallTypesettingDto(rawContent);
}));
AttachmentProvider
The usage of the AttachmentProvider is similar to the KernelDocumentV2Provider, but in the
AttachmentProvider the attachments needs to be provided as ByteArrayInputStream.
printJob.withProvider(AttachmentProvider.fromLoader(attachmentId -> {
byte[] data = loadAttachmentBytes(attachmentId);
if (data == null) {
throw new IllegalArgumentException("No attachment found for id: " +
attachmentId);
}
return new ByteArrayInputStream(data);
}));
PageRangeRestriction
Next to the Providers it is possible to define a PageRangeRestriction.
printJob.withRestriction(
PageRangeRestriction.builder()
.inclusiveStart(startIndex)
.exclusiveEnd(endIndex)
.build()
);
Executing a PrintJob
Once everything is set up. You can print the provided Print Model by calling the execute method of
the PdfBoxPrintEngine.
Example
PdfPrintResult pdfPrintResult = pdfBoxPrintEngine.execute(printJob);
Integration Xml PrintEngine
This chapter describes the additional integration of the XmlPrintEngine and
PdfWithXmlPrintEngine into a Java application.
29

-- 29 of 52 --

Additional Dependency
This section covers the additional dependencies necessary for integrating the XML PrintEngines
into a Maven or Gradle application. In both cases it is required to first follow the Instructions from
the previous Chapter.
For the usage with Maven, the following additional dependencies need to be added in the
applications POM-file:
Maven plugin
<dependency>
<groupId>com.mgmtp.a12.print</groupId>
<artifactId>print-engine-runtime-xml</artifactId>
<version>{printEngineVersion}</version>
</dependency>
For the usage with Gradle, the following additional dependencies need to be added in the
applications build.gradle file:
Gradle plugin
...
dependencies {
...
implementation("com.mgmtp.a12.print:print-engine-runtime-
xml:${printEngineVersion}")
...
}
Usage
Choosing the correct Xml Engine
There are two options available for generating Xml
• only the Xml String
• a Pdf where the Xml is embedded into the Pdf as a PdfAttachment
Usage of the relevant Classes
Import the package:
package com.mgmtp.a12.print.engine.runtime.xml;
30

-- 30 of 52 --

/**
* Provides the ability to execute {@link PrintJob}s.
*/
@OnlyForUsage
public class PdfWithXmlPrintEngine extends PrintEngine<PdfPrintResult> implements
com.mgmtp.a12.print.engine.api.PdfBoxPrintEngine {
/**
* @param service the ExecutorService that is used for the execution of concurrent
processes.
* @param config the relevant {@link PdfBoxPrintEngineConfig}
* @param resultType the relevant {@link ResultType}
*/
public PdfWithXmlPrintEngine(
@NonNull ExecutorService service,
@NonNull PdfBoxPrintEngineConfig config,
@NonNull ResultType resultType
) {
super(config);
this.service = service;
this.resultType = resultType;
}
/**
* Provides the ability to execute {@link PrintJob}s.
*/
@OnlyForUsage
public class XmlPrintEngine extends PrintEngine<XmlPrintResult> implements
com.mgmtp.a12.print.engine.api.XmlPrintEngine {
/**
* @param service the ExecutorService that is used for the execution of concurrent
processes.
* @param config the relevant {@link PdfBoxPrintEngineConfig}
*/
public XmlPrintEngine(
@NonNull ExecutorService service,
@NonNull PdfBoxPrintEngineConfig config
) {
super(config);
this.service = service;
}
Print Model Editor Light
This chapter describes the integration of the PrintModelEditorLight component into a React
Frontend application. It allows the creation of client applications where users can edit PrintModels.
CAUTION The PrintModelEditorLight is an experimental simplified version of the Print
31

-- 31 of 52 --

Model Editor, which itself is part of the standalone Simple Model Editor (SME)
application. For most use cases, we strongly recommend using the SME as the
standard tool for managing PrintModels. Integration of the
PrintModelEditorLight into your own frontend application is generally not
advised.
If you want to integrate this component or if your project has other requirements, feel free to
contact the A12 Print Engine team (Discourse)
Dependency
This section outlines the dependencies required to integrate the PrintModelEditorLight component
into a React application. The following module is required:
• print-model-editor-component
To install this module, use the following command:
pnpm install @com.mgmtp.a12.print/print-model-editor-component
Note: This package depends on the peer dependencies listed in package.json. Ensure your project
uses compatible versions.
Usage
Using the PrintModelEditorLight Component
The PrintModelEditorLight component provides a simplified API for editing PrintModels. The
example below shows how to integrate the component into a React application.
Please note that a Localization Context is required to provide localization for the component.
import { PrintModelEditorLight } from "@com.mgmtp.a12.print/print-model-editor-
component";
import { Locale, DefaultLocalizerContextProvider } from "@com.mgmtp.a12.utils/utils-
localization-react";
const App = () => {
const locale: Locale = { language: "en", country: "US" };
return (
<DefaultLocalizerContextProvider locale={locale}>
<PrintModelEditorLight
printModel={printModel}
documentModel={documentModel}
customFonts={customFonts}
staticImageProvider={staticImageProvider}
32

-- 32 of 52 --

onChange={(updatedModel, dirty) => {
console.log("Model updated:", updatedModel, dirty);
}}
/>
</DefaultLocalizerContextProvider>
);
};
Props
The PrintModelEditorLight component accepts the following props:
printModel (required)
The PrintModel to be edited. This must be a deserialized PrintModel instance. Utils for
deserialisation can be found in the module @com.mgmtp.a12.print/print-model-api-utils.
documentModel (required)
The associated DocumentModel. This must also be a deserialized DocumentModel instance. Utils
for deserialisation can be found in the module @com.mgmtp.a12.kernel/kernel-md-facade.
onChange (required)
A callback function triggered when the PrintModel changes. The function receives two
parameters:
• printModel: The updated, deserialized PrintModel instance.
• dirty: A boolean indicating whether there are unsaved changes. In practice, this means dirty
is false when the user has clicked the save button. In this case you need to handle saving
these changes on your side. dirty is true when the user has just made a change in the model.
You can use this to handle temporary changes, for example, saving them temporarily.
staticImageProvider (required)
An implementation of the StaticImageProvider interface for managing static images referenced
in the Print Model.
StaticImageProvider
/*
* StaticImageProvider defines the interface for managing static images for Print
Model Editor.
*
* listStaticImages: Returns a list of static image names available in the system.
* loadStaticImage: Given a image name, returns the corresponding StaticImageData if
it exists.
* uploadStaticImage: Takes a StaticImageData object and uploads it to the system,
returning a SaveStaticImageResponse.
*/
export interface StaticImageProvider {
listStaticImages: () => Promise<string[]>;
loadStaticImage: (name: string) => Promise<StaticImageData | undefined>;
33

-- 33 of 52 --

uploadStaticImage: (imageData: StaticImageData) => Promise<SaveStaticImageResponse
| undefined>;
}
/*
* The content of a image is interpreted by the Print Model Editor as a base64-encoded
string.
*/
export interface StaticImageData {
readonly name: string;
readonly internal_filename: string;
readonly mime_type: string;
readonly content: string;
readonly size: number;
}
export interface SaveStaticImageResponse {
readonly name: string;
}
customFonts (optional)
A map of custom fonts. The map should follow the FontResourceMap format. Each entry in the
map consists of:
• Key: The name of the font.
• Value: An object with the following properties:
◦ src: Specifies the font source. This can either be:
▪ A URL pointing to an accessible font file in your application.
▪ A Data URI that encodes the font file inline.
◦ format: The format of the font. Currently, only ttf (TrueType Font) is supported.
Custom Fonts
const customFonts: FontResourceMap = {
"CustomFont1": {
src: "./assets/fonts/custom-font-1.ttf",
format: "ttf",
},
"CustomFont2": {
src: "https://my-cdn.com/fonts/custom-font-2.ttf",
format: "ttf",
},
"CustomFont3": {
src: "data:application/octet-stream;base64,...",
format: "ttf",
},
};
34

-- 34 of 52 --

To override default fonts, provide a key matching the specific default font name. You find the
available default fonts in our modelling documentation: Available Default Fonts.
Override Default Fonts
const customFonts: FontResourceMap = {
"Open Sans": {
src: "./assets/fonts/custom-open-sans.ttf",
format: "ttf",
},
};
Differences Compared to the Simple Model Editor
The PrintModelEditorLight component differs from the full Print Model Editor which is integrated
in the SME in the following ways:
• Schema Tab: The PrintModelEditorLight does not include a Schema tab. This is because it does
not allow selecting different Document Models. Instead, a fixed Document Model is passed via
the documentModel prop.
• Commit Changes Tab: The PrintModelEditorLight does not support partial commits of selected
changes. Users can only choose to save all changes.
Breaking Change Management
Here, we define the A12 Print Engine public API and define which changes to this API we consider
breaking and non-breaking.
Definition of Version
The Print Engine component defines a single version. All artifacts (Java and Javascript) that belong
to the Print Engine share the same version.
Definition / distinction of different APIs
Public API internal
- All sources that are not internal.
- Everything that is mentioned in the
documentation.
- All sources in folders named a12internal
/internal or sub-folders thereof, or for Java
sources in packages named internal or sub-
packages thereof.
- Also all sources in modules whose artifactId
contains internal.
Code changes which do not affect the Public API type surface and change broken features (i.e.
features that did not work like expected) which are fixed are not regarded as Breaking Changes.
35

-- 35 of 52 --

Breaking Changes
Area Breaking Non-breaking
PDF - Layout changes in the output
PDF.
- Changes that require manual
user migration.
- Adding new features / new
components.
- Fully automated migration
that does not result in layout
changes.
libraries - Adding new base interfaces.
- Changing function signature.
- Adding a new interface.
- Adding an optional function
definition / signature.
Migration Instructions
2026.06
General Information
• All Print Java Artifacts now support Java 21 and Java 25.
Removals
Deep-Level Imports
Deep-level imports have been removed in favor of top-level imports for TypeScript projects.
Migration: Codemod (optional)
Result: The codemod automatically updates all imports.
// before
import { Area } from "@com.mgmtp.a12.print/print-model-
api/lib/model/elements/type/area.js";
// after
import { Area } from "@com.mgmtp.a12.print/print-model-api/model";
Model API
Java
• com.mgmtp.a12.print.model.api.model.element.structure.General.getTitle() has been removed.
Use com.mgmtp.a12.print.model.api.model.metadata.Metadata.getTitleComputation() instead.
36

-- 36 of 52 --

• com.mgmtp.a12.print.model.api.model.element.structure.General.getDetails() has been
removed. Use com.mgmtp.a12.print.model.api.model.element.structure.General.getMetadata()
instead.
• com.mgmtp.a12.print.model.api.model.element.structure.Details interface has been removed.
Use com.mgmtp.a12.print.model.api.model.metadata.Metadata with getAuthorComputation() and
getLanguageComputation() instead.
Migration: Manual
TypeScript
• PrintModelContentGeneral.title property has been removed. Use Metadata.titleComputation
instead.
• PrintModelContentGeneral.details property has been removed. Use
PrintModelContentGeneral.metadata to access metadata fields.
• Details interface has been removed. Use authorComputation and languageComputation from
Metadata instead.
• Language enum has been removed. Use any string to represent a language.
Migration: Manual
Print Shell
• The migration command has been removed from the print-shell. Please use the new typescript
based migration tool to migrate Print Model files. See Migration Tool for details.
• The --useExperimentalRendering flag has been removed from the print-shell. The new rendering
mode is now the default and only supported rendering mode.
Migration: Manual
Engine Api
• The PdfPrintEngine and PrintEngineConfig is no longer available. Please use the
PdfBoxPrintEngine and PdfBoxPrintEngineConfig instead.
Migration: Manual
Engine Runtime
• The PdfPrintEngine is no longer available. Please use the PdfBoxPrintEngine instead. The
following behaviors are specific to the PdfPrintEngine and will change once migrated to the
PdfBoxPrintEngine:
1. Table Rows are spread over a page break instead of being moved completely to the next
page if possible
2. Image Elements that are larger than a page will not be scaled down to fit the page size
3. Watermarks are also applied to attachment pages
4. Attachments are printed, whether or not they are linked inside the main document body
37

-- 37 of 52 --

5. If a text can not be printed with the selected font or the defined fallback font, it will instead
be printed with Open Sans
6. If Fields or Calculations are marked as anything but HTML, the HTML tags inside them will
be printed as plain text
7. Expression elements background color will apply to the whole element area, not just the text
area
8. Listing Property Computations can use other color descriptions beside hex color codes (e.g.
"red", "blue", etc.)
• The MarkupCombiner is no longer available. There is no replacement needed.
• The KernelDocumentProvider is no longer available. Please use the KernelDocumentV2Provider
instead.
Migration: Manual
Print Model Api Utils
• The ElementDefinitionMarshaller has been removed. Marshalling is only available at the Print
Model level via PrintModelMarshaller.
• The PrintElementValidator has been removed. Validation is only available at the Print Model
level via PrintModelValidation.
Migration: Manual
Print Setting Model
The Print Setting Model has been removed. Fonts are no longer configured manually — they are
loaded automatically from the workspace resources folder. When opening a workspace, the SME
migration automatically extracts the fonts and removes any existing Print Setting Model files. For
migrating outside of the A12 SME, use the print-setting-migration tool. See For Print Setting Model
(upgrading to Print Engine 4.0.0) for details.
Migration: SME or Migration Tool (required)
Result: Fonts are stored in the workspace resources folder.
Embedded Image Attachments
Embedded image attachments (base64-encoded image data stored directly in the Print Model) are
no longer supported. Images must be stored as files in the workspace resources folder and
referenced by name. The SME automatically migrates existing Print Models with embedded images
when the workspace is opened. For migrating outside of the SME, use the migration tool with the
--resources option. See migrate model with Node for details.
Projects that use static images must now implement a StaticImageProvider:
• Java: Implement the Java StaticImageProvider interface and pass it to the PrintJobManager
constructor to resolve image bytes during PDF generation. See StaticImageProvider for details.
38

-- 38 of 52 --

• PrintModelEditorLight: Pass a StaticImageProvider implementation via the
staticImageProvider prop to enable image selection and upload in the editor. See the
PrintModelEditorLight usage documentation for details.
Migration: SME or Migration Tool (required)
Result: Images are stored as files in the workspace resources folder.
Changes
Engine Runtime
PDFBox has been upgraded to version 3, which improves the memory usage of the generated PDFs.
Migration: Not needed
If a field value is requested inside a repeatable context that is not defined, the Print Engine will
now throw an error instead of returning null.
Migration: Manual
Print Shell
Print-shell flags are now enabled by passing only the flag without true or false.
Migration: Manual
Model API
Java
• New class com.mgmtp.a12.print.model.api.validation.PrintModelValidatorOptions — an options
record for IPrintModelValidator.validate that carries the locale and an html flag for HTML
validation.
• New method IPrintModelValidator.validate(String rawPrintModel, PrintModelValidatorOptions
options) — validates the print model with the supplied options. When options.html() is true, all
Text element HTML fields are validated in addition to the structural model validation. HTML
validation is enabled by default. In previous versions, HTML content in Text elements was not
validated at all. If your existing Print Models contain HTML that does not conform to the
supported subset of tags, attributes, and CSS properties, validation will now report errors or
warnings. To restore the previous behavior, explicitly disable HTML validation:
PrintModelValidatorOptions options = new PrintModelValidatorOptions(locale, false);
IPrintModelIntegrityReport report = validator.validate(rawPrintModel, options);
Migration: Manual
39

-- 39 of 52 --

Print Model Api Utils
TypeScript
• PrintModelMarshaller.serialize and PrintModelMarshaller.deserialize now accept a unified
PrintModelValidatorOptions object instead of separate positional parameters. The options
consolidate reference validation (references.documentModels), HTML validation (html), and
partial validation (partial.relevantPaths) in a single argument.
• HTML validation is now available via PrintModelValidatorOptions.html (default: true). When
enabled, the HTML content of Text elements is validated against the set of tags, attributes, and
CSS properties supported by the Print Engine. Errors are reported for constructs that cause
rendering failures; warnings are reported for unsupported but non-fatal constructs. In previous
versions, HTML validation did not exist — marshalling may now produce validation errors that
did not occur before. To restore the previous behavior, pass html: false:
const result = marshaller.deserialize(rawJson, { html: false });
const result = marshaller.serialize(printModel, { html: false });
• PrintModelValidatorOptions is exported from @com.mgmtp.a12.print/print-model-api-
utils/marshaller.
• See PrintModelMarshaller (TypeScript) for full usage examples.
Migration: Manual
Fixes
• The repetition and repetitionOfParent parameters in the listing calculations were
inconsistently evaluated, when using it inside of nested repeatable contexts. This has been fixed
and now the parameters are evaluated correctly in all contexts.
2025.06-ext5
Deprecations
Print Model Api Utils
• The ElementDefinitionMarshaller is deprecated and will be removed within the 2026.06 release.
• The PrintElementValidator is deprecated and will be removed within the 2026.06 release.
Print Model Api
• com.mgmtp.a12.print.model.api.model.element.base.DisplayOptions.DisplayType.HTML is no
longer deprecated and working as intended again.
40

-- 40 of 52 --

2025.06-ext4
Deprecations
Print Shell
• The --useExperimentalRendering flag ("-x") is deprecated and will be removed within the 2026.06
release. Currently, the default behavior of the print shell will default to the Legacy Rendering
Mode. With the removal of this flag, the print shell will use the current experimental rendering
mode as the default rendering mode.
Legacy Rendering Mode
The Legacy Rendering Mode is deprecated and will be removed within the 2026.06 release.
Noticeable Differences in the Legacy Rendering Mode
The following behaviors are specific to the Legacy Rendering Mode and will change once migrated
to the new rendering mode:
1. Table Rows are spread over a page break instead of being moved completely to the next page if
possible
2. Image Elements that are larger than a page will not be scaled down to fit the page size
3. Watermarks are also applied to attachment pages
4. Attachments are printed, whether or not they are linked inside the main document body
5. If a text can not be printed with the selected font or the defined fallback font, it will instead be
printed with Open Sans
6. If Fields or Calculations are marked as anything but HTML, the HTML tags inside them will be
printed as plain text
7. Expression elements background color will apply to the whole element area, not just the text
area
8. Listing Property Computations can use other color descriptions beside hex color codes (e.g.
"red", "blue", etc.)
Java
• The com.mgmtp.a12.print.model.api.model.element.base.DisplayOptions.DisplayType.HTML is
deprecated and will be removed within the 2026.06 release. This removal has no effect because
all content is evaluated as HTML now.
• The com.mgmtp.a12.print.model.api.model.element.structure.General.getTitle() method is
deprecated and will be removed within the 2026.06 release. Use
com.mgmtp.a12.print.model.api.model.metadata.Metadata.getTitleComputation() instead.
• The com.mgmtp.a12.print.model.api.model.element.structure.General.getDetails() method is
deprecated and will be removed within the 2026.06 release. Use
com.mgmtp.a12.print.model.api.model.element.structure.General.getMetadata() instead.
41

-- 41 of 52 --

• The com.mgmtp.a12.print.model.api.model.element.structure.Details interface is deprecated and
will be removed within the 2026.06 release. Use
com.mgmtp.a12.print.model.api.model.metadata.Metadata with getAuthorComputation() and
getLanguageComputation() instead.
TypeScript
• The PrintModelContentGeneral.title property deprecated and will be removed within the
2026.06 release. Use Metadata.titleComputation instead.
• The PrintModelContentGeneral.details property deprecated and will be removed within the
2026.06 release. Use PrintModelContentGeneral.metadata to access metadata fields.
• The Details interface deprecated and will be removed within the 2026.06 release. Use
authorComputation and languageComputation from Metadata instead.
• The Details.author property deprecated and will be removed within the 2026.06 release. Use
Metadata.authorComputation instead.
• The Details.language property deprecated and will be removed within the 2026.06 release. Use
Metadata.languageComputation instead.
• The Language enum deprecated and will be removed within the 2026.06 release. Use any string
to represent a language.
Deep Level Imports
Deep-level imports for the public API in print packages will be deprecated in this release and
removed within the 2026.06 release. Instead we will enforce top-level imports only.
Top-level imports are favorable, because they obscure the package internals, which makes them
less prone to breaking changes. Additionally they also minimize the amount of import statements
needed and allows us to precisely control the public API of our packages.
As an example, take the following deep-level imports:
import { PossibleInputSource } from "@com.mgmtp.a12.print/print-model-api/lib/input-
source/input-source.js";
import { InputSourceGenerator } from "@com.mgmtp.a12.print/print-model-api/lib/input-
source/input-source-generator.js";
import { InputValueSourceResolver } from "@com.mgmtp.a12.print/print-engine-
core/lib/input-source/input-source-resolver.js";
import { DeepPartialErrorMap } from "@com.mgmtp.a12.print/print-model-
api/lib/errors/deep-partial-error-map.js";
import { PrintModelMarshaller } from "@com.mgmtp.a12.print/print-model-api-
utils/lib/marshaller/model-marshaller.js";
import { ElementDefinitionMarshaller } from "@com.mgmtp.a12.print/print-model-api-
utils/lib/marshaller/element-definition-marshaller.js";
Following the deprecation, these imports must be replaced with top-level imports:
42

-- 42 of 52 --

import {
PossibleInputSource,
InputValueSourceResolver,
InputSourceGenerator
} from "@com.mgmtp.a12.print/print-model-api/input-source";
import { DeepPartialErrorMap } from "@com.mgmtp.a12.print/print-model-api/errors";
import {
PrintModelMarshaller,
ElementDefinitionMarshaller
} from "@com.mgmtp.a12.print/print-model-api-utils/marshaller";
For this migration, we provide a codemod tool that automatically replaces deep-level imports with
top-level imports. Please refer to the Codemod chapter for instructions on how to use the codemod
tool.
Limitations
The deprecated APIs getTitle(), getDetails() remain functional only if the metadata contains
exactly one computation alternative with a string literal operation. Using multiple computation
alternatives or non-literal operations will cause the deprecated APIs to throw an
IllegalStateException.
The new Metadata fields titleComputation, authorComputation, languageComputation,
descriptionComputation use ComputationAlternative arrays instead of simple strings. Existing Print
Models are automatically migrated by wrapping the old string values in a single computation
alternative with a quoted string literal as operation.
2025.06-ext2
Deprecations
Java
• com.mgmtp.a12.print.engine.api.PdfPrintEngine is deprecated and will be removed within the
2026.06 release. Please use com.mgmtp.a12.print.engine.api.PdfBoxPrintEngine instead.
• com.mgmtp.a12.print.engine.api.PrintEngineConfig is deprecated and will be removed within
the 2026.06 release. Please use com.mgmtp.a12.print.engine.api.PdfBoxPrintEngineConfig instead.
In the new config, it is only possible to configure custom fonts.
• com.mgmtp.a12.print.engine.runtime.pdf.PdfPrintEngine is deprecated and will be removed
within the 2026.06 release. Please use
com.mgmtp.a12.print.engine.runtime.pdfBox.PdfBoxPrintEngine instead. The new engine is not
able to provide HTML anymore and the PDF output differs.
• com.mgmtp.a12.print.engine.runtime.pdf.MarkupCombiner is deprecated and will be removed
within the 2026.06 release. It is not needed anymore, because the new
com.mgmtp.a12.print.engine.runtime.pdfBox.PdfBoxPrintEngine does not output HTML anymore.
43

-- 43 of 52 --

Migration from PdfPrintEngine to PdfBoxPrintEngine
CAUTION
We strongly recommend to migrate from PdfPrintEngine to PdfBoxPrintEngine as
soon as possible, because the old engine is going to be removed within the
2026.06 release. The experimental flag of PdfBoxPrintEngine is going to be
removed with the 2025.06-ext4 release. It only allows us to react to project
feedback with smaller breaking changes until then. The feature itself is
completely stable and ready for production use.
Feel free to contact the A12 Print Engine team (Discourse) in case of problems or
unexpected behavior changes.
Usage of PdfPrintEngine
PrintJobManager printJobManager = new PrintJobManager(pool, printJobManagerApi,
PrintJobConfig.DEFAULT);
PdfPrintEngine pdfPrintEngine = new PdfPrintEngine(pool, PrintEngineConfig.DEFAULT);
Usage of PdfBoxPrintEngine
PrintJobManager printJobManager = new PrintJobManager(pool, printJobManagerApi,
PrintJobConfig.DEFAULT, true);
PdfBoxPrintEngine pdfBoxPrintEngine = new PdfBoxPrintEngine(pool,
PdfBoxPrintEngineConfig.DEFAULT);
2025.06
Breaking Changes
Input Value Source
Due to the introduction of the Input Value Source for certain fields, these fields now represent
breaking changes at the API level. The value of each field depends on its source. To retrieve the
value, please use the Input Value Source’s API
Java usage
// Old model api
String title = chart.getChartProperties().getTitle();
// New model api
Optional<String> title =
InputValueSourceResolver.getInputValue(chart.getChartProperties().getTitle())
Typescript usage
44

-- 44 of 52 --

// Old model api
const title = lineChart.title;
// New model api
const title = InputValueSourceResolver.getSourceInputValue(lineChart.title, lineChart,
"lineChart.title")
Table 1. Fields that have an input source applied
Element Field
Table maxRowCount
sumLabel
columns.label
columns.width
Listing columns.label
columns.width
Table Layout rowProperties.minHeight
columnProperties.width
Line Chart title
labelX
labelY
Bar Chart title
labelX
labelY
Pie Chart title
The following elements contain text properties that need to be adapted to the new API:
• Text: Text properties
• Expression: Text properties
• Table: Text properties, header text properties
• Listing: Text properties, header text properties, column’s text properties
Table 2. Text Properties – Input Source Fields
45

-- 45 of 52 --

Element Field
Text Properties textStyleId
color
backgroundColor
bold
italic
underlined
alignment
Move the description to the header
The description field used to belong to content.general is moved to header of the model.
{
"header": {
"id": "print-model",
"modelType": "print",
"modelVersion": "3.0.0",
"description": "Description"
},
"content": { ... }
}
Roles Annotations in the SME are now separated by comma instead of semicolon
In versions prior to 3.0.0 creating a Print Model with roles in the SME would generate an
annotation with each role being separated with a semicolon. Now a comma-separated list is created
instead, bringing it in line with the rest of the A12 ecosystem.
{
"header": {
"id": "print-model",
"modelType": "print",
"modelVersion": "3.0.0",
"description": "Description",
"annotations": [
{
"name": "roles",
"value": "admin,modeler,tester,reviewer"
}
]
},
"content": { ... }
}
46

-- 46 of 52 --

Move the Marshaller to model-api-utils package and validating model references
In versions prior to 3.0.0, the Marshaller lived in the`@com.mgmtp.a12.print/print-model-api`, in
version 3.0.0, it has been moved to package @com.mgmtp.a12.print/print-model-api-utils.
To use the Marshaller, import it from the new package and pass document models when calling
serialize/deserialize to ensure that referenced document models can be validated correctly.
import { PrintModelMarshaller } from "@com.mgmtp.a12.print/print-model-api-
utils/lib/marshaller/index.js";
const printModelMarshaller = new PrintModelMarshaller();
const deserializedResult = printModelMarshaller.deserialize(printModel,
documentModels);
To skip reference validation, pass the SKIP_REFERENCES mode to the serialize/deserialize function
const deserializedResult = printModelMarshaller.deserialize(printModel, [],
PrintValidationMode.SKIP_REFERENCES);
Move serialization package to model-api-utils module
In version 3.0.0, the model-impl module has been removed. The serialization package was moved to
the model-api-utils module.
To use the serialization package, update all references and imports accordingly.
- com.mgmtp.a12.print.model.impl.serialization
+ com.mgmtp.a12.print.model.api.utils.serialization
Deprecation
Java
• Since the migration to Document V2, the Document V1 provider KernelDocumentProvider has
been deprecated. Please use KernelDocumentV2Provider instead.
• The migration command of the print-shell tool is deprecated since version 3.0.0. Use the Node
Based Tool from this version onwards.
Migration Tool
For Print Model Version 2.1.0 and Later
To migrate Print Model files, first install the latest version of the migration tool
47

-- 47 of 52 --

pnpm install -g @com.mgmtp.a12.print/print-model-migration
Then run the following command to perform the migration
print-model-migration <path to print model file or directory> --backup
Examples
# file
print-model-migration my-print-model.json --backup
# current folder
print-model-migration . --backup
Note
• If <path to print model file or directory> is a directory, the migration tool will recursively
search for Print Model files to migrate.
• If Print Model files are not under version control, use --backup (alias -b) flag to create backups
for model files. This flag is optional.
• Use --help (alias -h) flag to show all available options.
WARNING
When migrating a Print Model to a new version, any uncommitted changes
stored in .wal files may become invalid after the migration. Please commit all
pending changes in the Simple Model Editor before running the migration
tool.
Migrating Embedded Images (since version 4.0.0)
When migrating Print Models from a version earlier than 4.0.0, embedded image attachments on
image elements are detached from the model and saved as separate resource files in the
workspace. Because of this, the migration tool needs to know where to write the extracted image
files.
Use the --resources option when migrating models that contain embedded images:
--resources <path>
Path to the resources directory used for model lookup and where extracted image files are
saved.
Examples
# migrate a single file, writing extracted images to a resources folder
print-model-migration my-print-model.json --backup --resources ./my-
workspace/resources
48

-- 48 of 52 --

# migrate a directory and write images to a resources folder
print-model-migration . --backup --resources ./my-workspace/resources
NOTE
If a model contains embedded images and --resources is not provided, the
migration will fail with an error. Models without embedded images do not require
this option.
Additionally, when upgrading to Print Engine 4.0.0, any existing Print Setting Models must also be
migrated using the print-setting-migration tool. See For Print Setting Model (upgrading to Print
Engine 4.0.0) for details.
For Print Model Version Earlier Than 2.1.0
Step 1: Migrate Model to Version 2.1.0 Using Java Based Tool
To migrate Print Model, first download the Java based tool
CAUTION
Since with release 2.1.0 we introduce a new typescript based migration tool to
align with overall A12 standards. The print-shell migration will no longer
validate and pretty print starting from version 3.0.0. To ensure the same output
as prior versions, you need to run the print-shell migration and then the
typescript migration in order to migrate to the latest version. In releases after
2.1.0, only the typescript migration will be relevant.
Then migrate Print Model by executing the downloaded tool as follows
java -jar print-shell-2.1.3.jar migrate [OPTIONS]
Examples
# relative path
java -jar print-shell-2.1.3.jar migrate --workspace foldername --overwrite false
# absolute path
java -jar print-shell-2.1.3.jar migrate --workspace C:\Test\check\foldername
--overwrite false
Note
• Use migrate --help (alias -h) flag to show all available options.
• If Print Model files are not under version control, use --overwrite false (alias -b) flag to keep
the original model files. This flag is optional.
• You can use the print-shell in interactive mode by just running the jar file and entering
commands after startup. You can find more about the features of the print-shell in the modeling
49

-- 49 of 52 --

documentation of Print Engine.
Step 2: Migrate Model to Current Version Using Node Based Tool
See migrate model with Node.
For Print Setting Model (upgrading to Print Engine 4.0.0)
When upgrading to Print Engine 4.0.0, the Print Setting Model is removed. Fonts are no longer
configured per-model — they are always loaded from the workspace resources folder instead. Any
Print Setting Model at version 3.2.0 or earlier must be migrated.
To migrate existing Print Setting Model files, first install the latest version of the migration tool
pnpm install -g @com.mgmtp.a12.print/print-model-migration
Then run the following command to perform the migration
print-setting-migration <path to print setting file or directory> --resources <path to
resources directory>
The migration tool handles two font types:
Path fonts (type: "path")
The tool searches the workspace resources for a file matching the font path. If found, the file is
renamed to the sanitized configured font name and placed in the resources folder. If not found,
a warning is logged and the font must be added to the resources folder manually.
Embedded fonts (type: "attachment")
Base64-encoded font data is decoded and saved as a file in the workspace resources folder,
named after the sanitized configured font name.
In both cases, the font file is named after the font’s configured name in the Print Setting Model. For
example, a font configured as "My Custom Font" is saved as MyCustomFont.ttf.
After processing the fonts, the Print Setting Model file itself is deleted, as it is no longer needed.
--resources <path>
Path to the resources directory used for font lookup and where extracted font files are saved.
Examples
# migrate a single file, writing extracted fonts to a resources folder
print-setting-migration my-print-setting.json --resources ./my-workspace/resources
# migrate a directory and write fonts to a resources folder
print-setting-migration . --resources ./my-workspace/resources
50

-- 50 of 52 --

NOTE
The --resources option is required for this migration, since fonts must be written to
or looked up from the resources directory. The migration will fail if this option is
not provided.
Manual steps after migrating the Print Setting Model
After the migration tool has run, the following manual steps may be required:
Reselect sanitized font names
Font names containing whitespace or characters other than letters, numbers, -, _, and . are no
longer valid, because the SME file system does not allow them. The migration tool automatically
renames fonts stored in the workspace resources folder to comply with this restriction.
However, the font references in the Print Model are not updated automatically. After migration,
you must reselect the affected fonts in the Print Editor so the model references the sanitized file
names.
Relocate Fonts outside the resources folder
The migration tool can only process fonts stored inside the workspace resources folder. Fonts
stored elsewhere cannot be accessed by the SME workspace and must be handled manually:
1. Move the font files into the workspace resources folder.
2. Make sure the file names contain only letters, numbers, -, _, and .
3. Reselect the fonts in the Print Editor so the model references their new location.
Fallback font
The fallback font configured in the Print Setting Model is not automatically preserved during
migration. After migrating, the fallback font must be selected in the SME in the Print Preview.
Codemod
Codemods are automated scripts that help you update your codebase to accommodate breaking
changes introduced in new versions of a library or framework. They can save you significant time
and effort by automating repetitive tasks involved in code migration.
We provide a codemod tool to assist you in migrating your Print Engine related code.
At the moment, we only provide a recipe to replace deep-level imports with top-level imports in
TypeScript projects.
How to Use the Codemod
The codemod package is available via npx, so you don’t need to install it globally. You’ll need access
to the a12 npm registry to use it.
npx @com.mgmtp.a12.print/print-engine-codemod [recipe-id-or-version] [tsconfig-path]
If you run the command without any parameters, it will guide you through an interactive prompt to
51

-- 51 of 52 --

select a recipe and the tsconfig path.
Optional Parameters
• recipe-id-or-version: The ID of a specific recipe to run, or a target version to run all matching
recipes. To see a list of available recipes, run npx @com.mgmtp.a12.print/print-engine-
codemod@latest --list.
• tsconfig-path: The path to your tsconfig.json file or a folder containing one. The settings of the
tsconfig will be used to determine which files to migrate.
For example, to run the codemod for replacing deep-level imports with top-level imports, you
would execute:
npx @com.mgmtp.a12.print/print-engine-codemod prefer-top-level-imports ./tsconfig.json
52

-- 52 of 52 --

