# overall a12_workspace

A12 Modeling Workspace And
Workspace Conversion Framework
In order to support A12 Projects during their full lifecycle, a certain folder
structure is expected by the A12 Modeling and Runtime environment. This
allows to:
• open the models in the correct context in the A12 Simple Model Editor (SME)
• preview the models in the A12 Preview Application (in early project stages)
• use them without adoption in the Project Template
• use the Workspace Converter to create the corresponding runtime version of the models. This
enables development and testing of custom code.
Modeling Workspace Folder Structure
An A12 Project consists, amongst other things, of models, custom code and resource files. The
complete A12 Project folder is usually under version control.
Within the A12 Project, the models and the supporting resource files (fonts, themes, XSD files) are
organised in the A12 Modeling Workspace. The SME and the Preview App are working on the
Modeling Workspace.
The following elements are expected to be in the Modeling Workspace folder and have the stated,
reserved meaning.
Item Description
settings A file containing settings that are used by the Workspace Converters, the
Simple Model Editor and the Preview App Control.
NOTE
A warning is shown in the SME, if the settings are not
present in a to-be-opened folder. Modelers are strongly
advised to always open the whole Modeling Workspace
Folder.
Currently, all settings are persisted together in one file.
models Recommended folder to store all A12 models. Sub-folders can be used to
structure the models.
1

-- 1 of 8 --

Item Description
data
with
• data/attachments
• data/documents
• data/links
Workspace Data managed in the SME. The Preview App is initialized with
this data. The documents can be used to preview models within the SME.
Projects can use this data to initialize development or testing
environments.
auth
with
• auth/users.yaml
• auth/roles.yaml
Defines the users and roles that are available for modeling in the SME
and the Preview Application.
scripts A12 Business Scripts.
resources Additional files supporting the modeling or the application. This includes
1. Fonts used by the Print Engine
2. Themes (within 'resources/themes'), to be used in the SME Previews
3. XML Schema Definition files used by the A12-Transformer to create
corresponding Document Models
Workspace Converter
All functions of A12 that convert and expand Combination, Transformer, Type Definition and
Document Models into their runtime version - a self-contained Document Model - are provided as
Workspace Converters within a common framework. This also resolves any references to Type
Definition Models and Includes of those Models. After the model conversion, the Document Model
files are self-contained. The references to the modeling time helper models are resolved and
removed.
Data Services expects the converted runtime models during production initialization and at its
runtime endpoints. Handing un-converted models to Data Services might result in errors.
A12 provides different Workspace Converters with different purposes. Each of them has an order
number. It is possible to provide custom, project-specific Workspace Converters. The Workspace
Conversion Framework will collect and execute them according to the respective order number. By
choosing an appropriate order number, the custom converters are executed between the A12
default converters according to the project’s needs
The whole process can be seamlessly integrated into a build pipeline. Depending on project needs,
the runtime models can be treated as volatile build artifacts or as sources under source control.
2

-- 2 of 8 --

A12 Default Workspace Converters
Order Number Description
10 Deployment Exclusion Converter
Models that are listed in the Deployment Exclusion setting in the
Workspace Settings are removed from the models in this step.
40 Model Transformer Converter
This Workspace Converter creates a Document Model according to the
settings of the Transformer Model and the referenced XML Schema
Definition file (in the resources folder).
It is only available together with the Transformer Component, which is
part of the A12 Enterprise Subscription.
More information about the A12 Transformer can be found here:
Transformer Modeling Documentation Transformer Component
Documentation
CAUTION
The Transformed Document Model will have the
same name and id as the former Transformer Model.
The structure and the model-type of these models
change in this converter.
3

-- 3 of 8 --

Order Number Description
50 Combination and Document Model Converter
This Workspace converter expands all Combination and Document
Models. It resolves the references to Type Definition Models and Includes.
It applies Addition, Selection and Decoration steps of Combination
Models. The references to the respective modeling-time helper models
are removed from the model’s header in this step.
This converter also rewrites the model-type of all Additive Document
Models, that are used as Precomputation Models in Mapping Models, to
"runtime_mapping_precompfragment_document" (instead of
'document'). It then removes all other Additive Document Models, as they
are considered modeling-time helper models.
CAUTION
Selection and Type Definition Models, Additive
Document Models, that are not used in Mapping
Models, as well as Document Models, that were used
as Decoration Models, will be removed from the
Workspace in this step.
CAUTION
The Combined Document Model will have the same
name and id as the former Combination Model. The
structure and the model-type of these models change
in this converter.
60 Meta Data Group Addition Converter
This Workspace Converter adds the '__meta'-Group to Document Models
and their derivatives. The '\__meta'-Group of the Meta Data Model is
added
1. if the model’s id ends with '\__generated': as a subgroup of groups
'target' and 'relationship'.
2. if the model carries Annotation 'cdm.queryRoot': as a subgroup to all
groups that carry Annotation 'cdm.relationship' and their subgroup
'relationship'.
3. all others: as a root group.
Models Removed From the Workspace
The following models will be removed by the Workspace Converters, because they are considered
modeling-time helper models:
1. all Type Definition Models
2. all Selection Models
4

-- 4 of 8 --

3. Document Models that were used as Decoration Models
4. Additive Document Models that are not referenced in Mapping Models as Precomputation
Models
Integration of the Workspace Conversion
Into Build Pipelines
The A12 Project Template comes with the required configuration. It integrates the Default A12
Converters in its gradle build pipeline and treats the created runtime models as volatile build
artefacts not under version control.
Workspace Conversion Framework
The Workspace Conversion Framework (WCF) is a lightweight, extensible Spring Boot based
pipeline that converts an A12 Workspace into its runtime form via ordered converter steps.
Modules
Module Description
dataservices-wcf-api Public SPI: domain interfaces, WorkspaceConverter,
WorkspaceFactory, @WcfConverter.
dataservices-wcf-core Default Spring components (supplier, conversion service,
consumer) + internal POJOs.
dataservices-wcf-cli Command line interface (fat JAR) adding an external converters
JAR to the classpath at runtime.
Core Concepts
Workspace
The WCF exposes the read A12 Workspace in the following form:
• Models (header + parsed JSON)
• Files (binary or pass-through)
• Input directory reference (for read-only access by converters)
Tuples
The Converter can access the Workspace items via one of the following:
• ModelTuple: Header + JSON content string
• FileTuple: target output path + optional byte[] content (null ⇒ copy original)
5

-- 5 of 8 --

Converters
The Workspace Converters (standard and custom) must implement WorkspaceConverter and are
discovered via annotation @WcfConverter(order=…, description=…).
Responsibilities of the Converter may include:
• Model enrichment / normalization
• Synthetic file generation
• Validation / pruning
Pipeline
1. DefaultWorkspaceSupplier scans input recursively:
◦ Parses *.json with HeaderParser
◦ Falls back to file tuple on parse failure
2. Ordered converters mutate or replace the Workspace
1. DefaultWorkspaceConsumer writes:
◦ Models → data/models/<modelId>.json
◦ Files → declared output paths (under seed data root)
Autoconfiguration
dataservices-wcf-core provides:
• DefaultWorkspaceSupplier
• DefaultWorkspaceConsumer
• DefaultWorkspaceConversionService
All annotated with @Component (ensure package com.mgmtp.a12 is scanned).
Injection:
@Autowired
WorkspaceConversionService service;
Adding Custom Converter
The code snippet below shows a Custom Converter that generates a README file.
@WcfConverter(order = 50, description = "Add generated README")
public class AddReadme implements WorkspaceConverter {
private final WorkspaceFactory f = WorkspaceFactory.getInstance();
6

-- 6 of 8 --

@Override
public Workspace convert(Workspace ws) {
ws.getFiles().put(
"synthetic:readme",
f.createFileTuple("generated/README.txt", "Hello Workspace".getBytes())
);
return ws;
}
}
Package into a JAR and pass with -c. Please note, that currently only one JAR file can be handed
over to the Workspace Converter Framework JAR.
Thread Safety Guidelines
• Converters should be stateless or internally synchronized
• Avoid mutable static fields
• Use thread-safe caches if needed
CLI Usage
Build CLI:
./gradlew :dataservices-wcf-cli:bootJar
Run:
java -jar dataservices-wcf-cli/build/libs/dataservices-wcf-cli-<version>.jar \
<WORKSPACE_DIR> <SEED_DATA_DIR> \
-c path/to/converters.jar
Notes:
• Relative paths resolved against current working directory (CWD unchanged by launcher).
• Converters JAR is appended to classpath (not replaced).
WorkspaceFactory
Thread-safe lazy singleton (WorkspaceFactory.getInstance()). Creates plain POJOs — no heavy state.
Error Handling
Case Behavior
Missing input dir CLI parameter error
7

-- 7 of 8 --

Case Behavior
Converter returns null Fails fast
Duplicate output path OutputConflictException
Header parse failure Stored as file tuple (copy-through)
8

-- 8 of 8 --

