# transformer transformer documentation

Transformer
Table of Contents
Introduction . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
Who Should Read This Document? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
Terminology . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
Architecture. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
Functionality . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
XsdToA12ModelTransformer . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Description . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Command Line Tool . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
API. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
Set Up a Gradle Task for the XsdToA12ModelTransformer . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 25
Java API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
Example Usage. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
XSD Discovery Feature . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
What Information Can You Discover? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
How to Use Discovery . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
What Can You Do With Discovered Information?. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
Common Use Cases . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 31
Important Notes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 31
A12DocumentToXmlTransformer . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
Description . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 32
File-Based Usage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 33
In-Memory (Runtime) Usage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 34
Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
Common Configuration (DocumentXmlTransformConfig) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 35
File-Based Configuration (FileBasedDocumentXmlTransformConfig) . . . . . . . . . . . . . . . . . . . . . . . . 36
Considering Only Sub-Models for Transformation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
Excluding Model Elements From the XML (_TRANSFORMER_IGNORE_ELEMENT) . . . . . . . . . . . . . . . . . . . 37
XmlToA12Transformer. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Description . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 38
Step 1: Instantiating the IXdXmlDocumentTransform . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
Step 2: Performing the Transformation. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
1

-- 1 of 66 --

XmlSchemaValidator. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Description . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
A12ModelToJsonSchemaTransformer . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
Description . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
Gradle Plugin Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
Step 1: Apply the Plugin. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
Step 2: Configure the Task. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
Step 3: Run the Task . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
Standalone CLI . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
Usage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
Options. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
Exit Codes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 46
Examples . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
Workspace Converter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
Workspace Transformation Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
JSON Schema Mapping. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 48
Field Type Mappings . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 48
Document Structure Mapping . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 51
Example. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 52
Transformer Workspace Converter. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 54
Description . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 54
Workspace Input Contract. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 55
Behavior. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 55
Maven Coordinates . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 55
Deployment & Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 55
Artifacts . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 55
API Documentation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 56
JavaDoc. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 56
Migration Instructions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 56
Model Migration Tool . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 56
2026.06 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 57
Breaking Changes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 57
XSD-to-Model Transformer No Longer supports parameter /content/Cmd/genDocModelName
in the Transformer Config Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 57
Repeatable Code List Groups No Longer Produce a Multi-Select Companion Group in the
Generated Document Model. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 57
Extended xs:choice Support Changes the Generated Document Model Structure . . . . . . . . . . 58
New Config API for Doc-to-XML and XML-to-Doc Transformers. . . . . . . . . . . . . . . . . . . . . . . . . . 58
Shared Types Moved to transformer-common Module . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
PatternErrors: Default UPDATE_MESSAGE Action and XmPatternErrorsLayer Post-Processing . . . 61
2

-- 2 of 66 --


This documentation belongs to an A12 Enterprise Component which is not part of
the Open Source offering (A12 Community Edition). Please feel free to browse the
documentation and learn more about how you can use this A12 component in
your project. Learn more about the benefits from an A12 Enterprise Subscription
on the Editions & Licensing page.
Introduction
The Transformer provides a Java library and a CLI tool to assist in the transformation between
XMLs and A12 documents, as well as a Gradle plugin for generating JSON Schema files from A12
Document Models.
For this purpose, the Transformer offers the following functionalities:
• Transformation of XML Schemas, into A12 Document Models.
• Transformation between XML and A12 Documents based on transformation models.
• EXPERIMENTAL: Transformation of A12 Document Models into JSON Schema files.
Who Should Read This Document?
This document is intended for developers who want to use the Transformer in a Java application. It
describes the use cases of the Transformer, as well as its configuration and utilization.
Terminology
No Term Definition
1 A12DocumentToXmlT
ransformer
A subcomponent of the Transformer that transforms A12 documents
into XML documents.
2 Transformer The entire project and its subcomponents.
3 XmlSchemaValidator A subcomponent of the Transformer that validates XML documents
against XSDs.
4 XmlToA12Transforme
r
A subcomponent of the Transformer that transforms XML documents
into A12 documents.
5 XsdToA12ModelTransf
ormer
A subcomponent of the Transformer that transforms XSDs into
Document Models.
Separation of Internal and Public Api . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 62
Feature Changes. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 65
Support for XSD and XML Without Target Namespace . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 65
New _TRANSFORMER_IGNORE_ELEMENT Annotation for Modeler-Added Elements . . . . . . . . . . . . . . 65
XSD Elements Without a Declared Type or With xs:anyType Now Map to a String Field. . . . 65
XML-to-Doc Transformer Accepts IDocumentModel Instances Directly . . . . . . . . . . . . . . . . . . . . . 66
3

-- 3 of 66 --

No Term Definition
6 A12ModelToJsonSche
maTransformer
A subcomponent of the Transformer that transforms A12 Document
Models into JSON Schema files.
Architecture
The Transformer’s architecture is composed of the following subcomponents, which are described
in more detail in Chapter Functionality.
1. XsdToA12ModelTransformer: A CLI tool that generates a Document Model based on XSD
documents. To provide the generated model to the application, the CLI tool can be executed
during the build time. For further information, refer to section XsdToA12ModelTransformer.
2. A12DocumentToXmlTransformer: A Java library that can be integrated into A12 applications
to generate an XML document based on an A12 document. This library is intended to be used at
runtime. For further information, refer to section A12DocumentToXmlTransformer.
3. XmlToA12Transformer: A Java library that can be integrated into an A12 application to
generate A12 documents from provided XML documents. This library must be executed during
the runtime of the A12 application. For further information, refer to section
XmlToA12Transformer.
4. XmlSchemaValidator: A Java library that can be integrated into an A12 application to validate
whether an XML document conforms to a provided XSD. For further information, refer to
section XmlSchemaValidator.
5. DocModelToJsonSchemaTransformer: Generates JSON Schema files from A12 Document
Models. Available as a Gradle plugin, a standalone CLI, or a workspace converter. For further
information, refer to section A12ModelToJsonSchemaTransformer.
Figure 1 illustrates how the Transformer components are applied and interact with each other.
4

-- 4 of 66 --

XSDToA12Model
Transformer
XSD
A12 Document
Model 	Input	Output
A12ToXMLTransformer
A12
Document 	Input 	XML	Output
corresponds to 	corresponds to
XMLToA12Transformer 	Input	Output
During Compile/
Modelling Time
During Run Time
A12
Document XML
Scenario 2:
Scenario 1:
XMLSchema
Validator
validates
validates
validates against
Schema
Checker Input
Compatibility
Report 	Output
uses
A12ModelToJsonSchema
Transformer
JSON
Schema
A12 Document
Model
A12
Document
Input 	Output
corresponds to 	validates against
Scenario 3:
Figure 1. Transformer Architecture Overview
As shown in the figure, the Transformer can be used in different scenarios:
Scenario 1: An A12 application needs to import an XML and make it available as an A12 document:
• This is achieved by converting the XSD to which the XML conforms into a Document Model using
the XsdToA12ModelTransformer during build time. Afterward, at runtime, the XML can be
transformed into an A12 document that conforms to the generated Document Model.
Scenario 2: An A12 application needs to export data to an XML conforming to a specific XSD.
• First, the target XSD must be converted into a Document Model using the
XsdToA12ModelTransformer. Next, the data to be exported must be made available as an A12
document that corresponds to the converted Document Model. This step is done by the client of
the Transformer. Finally, the A12 document can be transformed into the target XML using the
A12DocumentToXmlTransformer.
Scenario 3: An A12 application needs to provide JSON Schema definitions representing A12
document models.
• The A12 Document Model can be transformed into a JSON Schema using the
5

-- 5 of 66 --

A12ModelToJsonSchemaTransformer, available as a Gradle plugin or standalone CLI for build-
time generation, or as a workspace converter for use inside a WCF pipeline. The generated JSON
Schema can then be used to validate A12 documents that are serialized in JSON format.
Functionality
In this section the functionality of the Transformer components are described.
XsdToA12ModelTransformer
Description

If you are using the A12 Workspace Converter Framework (WCF), you do not
need to use the XsdToA12ModelTransformer CLI directly. The Transformer
Workspace Converter performs the same transformation automatically as part of
the workspace pipeline. The Workspace Converter is the standard mechanism for
model generation in A12 applications.
The XsdToA12ModelTransformer enables the transformation of XSDs into Document Models. You
can use the CLI tool to perform the transformation at build time or call the transformation API from
your Java code at runtime.
Command Line Tool
The available commands of the CLI tool are described below:
• check: This mode is recommended to test if all features of the source XSDs are supported.
Therefore, it creates a report stating which features are not supported. In addition, it will
generate a Document Model based on the supported XSD features which could be seen as a
preview Document Model.
• generate-model: In contrast to check, this mode just generates a Document Model without an
unsupported feature report. It will fail, if features of the source XSDs are not supported.
• get-config-model: This mode generates the transformer configuration model template to help
users prepare their transformation configuration files. The generated model can be used with
SME to create proper configuration files.
API
To transform XSDs into Document Models, the XsdToA12ModelTransformer the corresponding jar
file must be executed using the following command:
check Mode
java -cp <transformer config jar> -jar <transformer cli application jar> \
check <parameter list>
6

-- 6 of 66 --

generate-model Mode
java -cp <transformer config jar> -jar <transformer cli application jar> \
generate-model <parameter list>
get-config-model Mode
java -jar <transformer cli application jar> \
get-config-model \
--output-dir <value>
Where:
• <transformer cli application jar> — the CLI application JAR, e.g. transformer-xsdtomodel-cmd-
3.0.0.jar
• <transformer config jar> — a JAR or directory on the classpath containing the transformation
configuration file (referenced by --transform-config), e.g. my-config.jar
• <parameter list> — one or more CLI parameters as described in Configuration Parameters
Example
java -cp my-config.jar -jar transformer-xsdtomodel-cmd-3.0.0.jar \
generate-model \
--xsd-dir src/main/resources/xsd \
--output-dir build/generated-models \
--main-xsd my-schema.xsd \
--root-element MyRootElement \
--roles admin,editor \
--gen-doc-model-name MyModel \
--transform-config config/transformer-config.json
Configuration
This section describes how to configure the XsdToA12Transformer and a tutorial on how to set up a
Gradle task to utilize the XsdToA12ModelTransformer CLI tool.

The configuration encompasses the following two aspects:
• Configuration parameters: Single value parameters that can be specified
through the CLI as well as in the config file. See setting Cmd described in
section Configuration Settings.
• Configuration settings: Complex possibilities to configure the transformer.
Can only be specified in the config file, not in the CLI. These configuration
settings are NOT applicable to command get-config-model.
7

-- 7 of 66 --

Configuration Parameters
The tables below list the available configuration parameters for each command.
Configuration Parameters for check and generate-model
For each parameter, the table shows the CLI argument, the corresponding specifier to be used in
the config file, a short description, and whether the parameter is required when running the
XsdToA12ModelTransformer or not.
 If a parameter is specified through the CLI as well as in the config file, the CLI
value is used.
CLI Parameter Config File Parameter Description Required
--xsd-dir xsdDir The path to the
directory containing
the input XSDs for the
transformation.
yes
--output-dir outputDir The directory to store
the output Document
Model.
yes
--main-xsd mainXsd Name of the main XSD
file in the XSD
directory.
yes - must be specified
in the config file, but can
be overridden by the CLI
argument!
--root-element rootElement Name of the root
element in the main
XSD.
yes - must be specified
in the config file, but can
be overridden by the CLI
argument
--roles roles Comma-separated list
of roles to be used in
the generated
Document Model (e.g.
admin,editor,reviewer).
yes
--gen-doc-model-name Can not be specified in
the config file!
Name of the generated
Document Model.
only required when
missing the config file
8

-- 8 of 66 --

--transform-config Can not be specified in
the config file!
A path to a
configuration file that
includes the
configuration settings.
The value can be either
a path in the local
filesystem, or a relative
path inside the
application’s classpath.
The configuration
settings are further
described in subsection
Configuration Settings.
no
--allow-remote-xsd allowRemoteXsd Boolean flag (specify
without value or set to
true). Allows
downloading
referenced XSDs from
the internet. If this flag
is not enabled, it will
not be possible to
validate or process XSD
files that import or
include other XSDs via
remote URLs.
no
--cl-xmls-dir clXmlsDir The directory
containing XML code
list files (genericode
format). These files
follow the XSD
genericode
specification and are
referenced in the input
XSDs. The transformer
uses them to generate
EnumerationType
fields in the Document
Model. Currently, only
xoev-specific code lists
are supported.
no
9

-- 9 of 66 --

--minimal minimal Boolean flag (specify
without value or set to
true). If set, a model
without extra
annotations is
generated. These
annotations are used
by the runtime
transformers
(A12DocumentToXmlTr
ansformer and
XmlToA12Transformer)
for XML round-
tripping; see
A12DocumentToXmlTr
ansformer and
XmlToA12Transformer.
A minimal model is
smaller but cannot be
used for XML
transformation without
the annotated variant.
no
--external-type-defs externalTypeDefs Boolean flag (specify
without value or set to
true). When set, shared
type definitions (e.g.
EnumerationTypes
derived from code lists)
are extracted into
separate Type
Definition Model files
instead of being inlined
in the Document Model.
This allows multiple
Document Models to
reference the same
type definitions
without duplication.
no
--all-root-elements Can not be specified in
the config file!
Boolean flag. Activates
the Root Element Batch
Transformation.
no
10

-- 10 of 66 --

--skip-consistency
-check
skipConsistencyCheck Boolean flag (specify
without value or set to
true). Skip consistency
check and force
Document Model
creation even when
A12 Kernel consistency
issues are present.
When set, a warning
notification is logged,
and the model is
generated regardless of
consistency errors.
no
--verbose Can not be specified in
the config file!
Boolean flag. Enables
detailed logging output
(DEBUG log level).
no
Root Element Batch Transformation
If the --all-root-elements configuration parameter is specified, all root elements defined in main-
xsd are used for the transformation. For each root element, a separate Document Model is
generated. The models are named using the following pattern: <gen-doc-model-name>_<root-element-
name>.json. In this case parameter --root-element is ignored.
 The term root element refers to all <xs:element> definitions directly one level below
the <xs:schema> element of an XSD.
Configuration Parameters for get-config-model
Parameter Description Required
--output-dir The directory where the
transformer configuration
model template will be
generated. This template can be
used with SME to create proper
transformation configuration
files.
yes
Configuration Settings
The configuration settings are specified in the config file passed through the --transform-config CLI
argument.
The configuration file passed as the transformConfig contains additional information required for
the transformation between XSDs and Document Models.
The configuration file matches the A12 Model format as follows:
11

-- 11 of 66 --

{
"header": {
"id": "<Your chosen model id>",
"modelType": "transformer"
},
"content": {
...
}
}
Header Attributes
Attribute Description Required
id The transformation
configuration model id.
yes
modelType Must be set to "transformer". yes
modelVersion Must be set to the transformer
lib version
yes
locales List of locales to be used in the
transformed Document Models.
no
labels List of labels to be used in the
transformed Document Models.
no
annotations List of annotations to be used in
the transformed Document
Models.
You can configure roles by
adding an annotation with key
roles and a comma-separated
list of roles as the value.
no

The attributes locales, labels, and annotations are copied from the transformer
config header to the header of the transformed Document Model. If you specify the
roles annotation in the transformer config header, it will be overridden by roles
specified via the CLI arguments or in the Cmd section of the content.
Content Attributes
12

-- 12 of 66 --

Setting Description
TypeMapping Maps XSD types to Document Model field types
(e.g., "string" to "StringType", "boolean" to
"BooleanType"). See Supported Type Mappings
for supported mappings.
- For advanced customization of field type
properties beyond simple type mapping, see
Overriding Default Type Mapping Behavior.
CodeLists Configuration for handling code lists in the
XSDs. The following aspects can be configured:
- CodeIdentifiers = List of ColumnIds. Determines
the columns in code list XML files, whose values
are transformed to Enum values.
- ValueIdentifiersDe = List of ColumnIds.
Determines the columns in code list XML files,
whose values are transformed to german Enum
labels.
- ValueIdentifiersEn = List of ColumnIds.
Determines the columns in code list XML files,
whose values are transformed to english Enum
labels.
- ElementNamesInXsd = Name of the XSD
element that contains the actual code value. This
element is contained in the Complex Type that
represents the code type.
- UriVersionList = Map defines which default
version Ids to be used for which code list URIs.
These default Ids are only applied if no version
Id is specified for a code list complex type.
DeletePaths List of Document Model paths that should not be
contained in the transformed Document Model.
The paths can point to either fields or groups.
Configuration Allows to specify
- the configurations version and
- the timeZone used in the generated Document
Models, and
- the supportedCharactersTypeInXsd or
supportedCharacters to define the set of
supported characters for string field
validation.
See Supported Characters Configuration.
13

-- 13 of 66 --

Setting Description
PatternErrors List of entries applied as a post-processing step
by the XmPatternErrorsLayer after the initial XSD
parsing. Each entry matches a StringType field
whose pattern exactly equals the configured
pattern value (the actual regex string).
+ Each entry supports the following fields:
+ - pattern — the exact regex string as it appears
in the generated StringType (required).
- action — controls the modification applied to
the matched field (optional; absent defaults to
UPDATE_MESSAGE):
UPDATE_MESSAGE — keep the original XSD pattern
unchanged; replace the error message with the
localized texts from errors.
REPLACE - replace the pattern with the value in
replacement; set the error message from errors.
SUPPRESS — remove both the pattern and its
error message from the generated Document
Model.
- replacement — the new regex pattern. Only
used when action is REPLACE.
- errors — array of { "locale": "<lang>",
"text": "<message>" } objects. Used by
UPDATE_MESSAGE and REPLACE. If empty, a default
error message formatted with the (replacement)
pattern is used.
+ If you don’t configure the localized error for any
languages, the error label falls back to the default
German error text.
14

-- 14 of 66 --

Setting Description
EnumLabels Configuration to override enumeration labels of
EnumValues instances using locale-based
replacements;
+ Each entry contains:
+ value: the value of an EnumValue instance
whose label must be overridden
+ replacements: an array of objects { "locale":
"<lang>", "text": "<label>" } forming the final
label set.
If you don’t configure the localized label for any
languages, the label would fall back to the value
of the enum.
If you only configure the label for a subset of
languages, the other languages would fall back to
the label value of de.
- typeDefinitionId: (optional) allows to restrict
the label replacement to a specific
EnumerationType by providing its
typeDefinitionId
- enumFieldPath: (optional) allows to restrict the
label replacement to a specific
EnumerationType by providing its
enumFieldPath
`typeDefinitionId` and enumFieldPath can NOT be
specified simultaneously!
When neither typeDefinitionId nor enumFieldPath
is specified for a label replacement entry, the
replacement applies to all matching enumeration
labels.
If multiple EnumLabels entries match the same
enum value, a scoped entry (typeDefinitionId or
enumFieldPath) overrides a global (unscoped)
entry. If the same enum value is targeted by both
a typeDefinitionId-scoped and an enumFieldPath
-scoped entry, the enumFieldPath-scoped entry
takes precedence and overrides the
typeDefinitionId-scoped one for the matching
field, and a warning is emitted.
15

-- 15 of 66 --

Setting Description
Cmd Specify configuration parameters, see section
Configuration Parameters for the available
parameter list.
RenamePaths Allows to rename the name of elements in the
transformed Document Model; A single
renaming entry contains the following
properties:
- OriginalPath = original path to the element
whose name must be renamed
- NewElementName = new name of the element
Note: The original name is added as an
annotation to the renamed element!
The configuration settings are specified as an A12 document, thus, it can be managed using the
SME. Because this file contains significant information for the transformation process, it must be
maintained in collaboration with the Transformer team.

The Document Model has length restriction on their field and group names,
described as follows:
Type Maximum length (characters)
Group name 60
Field name 200
Validation Rule Name 100
If your XSD element name exceeds those limits, the transformer check command
will throw an error. To resolve this issue, you can rename the affected element
using the RenamePaths configuration.
{
"RenamePaths": [
{
"OriginalPath":
"/a12Root/LongXSDElementNameWhichHas60Characters",
"NewElementName": "ShortenedElementName"
}
]
}
An example configuration file is shown below:
{
"header": {
"id": "TransformationConfiguration",
16

-- 16 of 66 --

"modelType": "transformer"
},
"content": {
"TypeMapping": [
{ "xsdType": "anyType", "a12Type": "StringType" }
],
"CodeLists": {
"CodeIdentifiers": [
//corresponds to `<Column Id="enumValues" ...>` defined in the code list xml
{ "value": "enumValues" }
],
"ValueIdentifiersDe": [
//corresponds to `<Column Id="germanEnumLabels" ...>` defined in the code list
xml
{ "value": "germanEnumLabels" }
],
"ValueIdentifiersEn": [
//corresponds to `<Column Id="englishEnumLabels" ...>` defined in the code
list xml
{ "value": "englishEnumLabels" }
],
"ElementNamesInXsd": [
// corresponds to `<xs:element name="codeElementName" .../>` defined in the
code complexType
{ "value": "codeElementName" }
],
"UriVersionList": [
{
/* if in the code complexType the attribute listVersionID is not set for the
given code list URI, then version "2023-12" is used */
"uri": "urn:xoev-de:xnachweis:codeliste:sprachen-iso-639-1",
"version": "2023-12"
}
]
},
"DeletePaths": [
//paths pointing to groups and fields that would be initially generated based
on the input XSDs but are deleted in the final Document Model
{
"path": "/path/to/FieldToBeDeleted"
},
{
"path": "/path/to/GroupToBeDeleted"
}
],
"RenamePaths": [
{
// performs renaming element "/some/path/OriginalElementName" to
"/some/path/SomeNewElementName"
"OriginalPath": "/some/path/OriginalElementName",
"NewElementName": "SomeNewElementName"
17

-- 17 of 66 --

}
],
"Configuration": {
// version of the configuration file
"version": "15.12.2024",
// used as the time zone of the generated Document Models
"timeZone": "UTC",
// Supported Characters Configuration
// Option 1: Reference an XSD simple type that contains a pattern facet
// The transformer will search for this type in the main XSD and all included
schemas
"supportedCharactersTypeInXsd": "datatypeC"
// Option 2: Directly specify a regex pattern (uncomment to use instead of
Option 1)
// "supportedCharacters": "[a-zA-Z0-9\\s]"
// Note: If both are specified, the transformation will fail with the error
message: _"Both **supportedCharacters** and
// **supportedCharactersTypeInXsd** were provided. Exactly one of them must be
set."_
},
"PatternErrors": [
{
// UPDATE_MESSAGE: replace only the error message for a regex-based pattern
"pattern": "\\d{14}",
"action": "UPDATE_MESSAGE",
"errors": [
{ "locale": "de", "text": "Bitte geben Sie einen validen Leika-Schlüssel
ein." },
{ "locale": "en", "text": "Please enter a valid Leika key." }
]
},
{
// REPLACE: change both the pattern and the error message
"pattern": "\\d+",
"action": "REPLACE",
"replacement": "[0-9]{1,10}",
"errors": [
{ "locale": "de", "text": "Bitte geben Sie eine positive ganze Zahl ein." },
{ "locale": "en", "text": "Please enter a positive integer." }
]
},
{
// SUPPRESS: remove pattern and error message entirely
"pattern": ".*[^\\s].*",
"action": "SUPPRESS"
},
{
// Absent action defaults to UPDATE_MESSAGE
18

-- 18 of 66 --

"pattern": "\\d{5}",
"errors": [
{ "locale": "de", "text": "Bitte geben Sie genau 5 Ziffern ein." }
]
}
],
"EnumLabels": [
/*
This results in the following EnumerationType change in the transformed
Document Model
for all EnumerationValues with value "exampleEnumValue":
"EnumerationType": {"values": [{
"value": "exampleEnumValue",
"label": [
{"locale": "de", "text": "Neues Enum Label"},
{"locale": "en", "text": "New Enum Label"}
]
}]}
*/
{
"value": "exampleEnumValue",
"replacements": [
{ "locale": "de", "text": "Neues Enum Label" },
{ "locale": "en", "text": "New Enum Label" }
]
},
// this will only replace the labels for the EnumerationValue
"enumValueRenamedByEnumFieldPath"
// in the EnumerationType located at "/path/to/EnumerationType/enumField"
{
"value": "enumValueRenamedByEnumFieldPath",
"replacements": [
{ "locale": "de", "text": "Neues Enum Label" },
{ "locale": "en", "text": "New Enum Label" }
],
"enumFieldPath": "/path/to/EnumerationType/enumField"
},
// this will only replace the labels for the EnumerationValue
"enumValueRenamedByTypeDefinitionId"
// in the EnumerationType with the typeDefinitionId
"urn_de_fim_codeliste_dokumenttyp_4_0"
{
"value": "enumValueRenamedByTypeDefinitionId",
"replacements": [
{ "locale": "de", "text": "Neues Enum Label" },
{ "locale": "en", "text": "New Enum Label" }
],
"typeDefinitionId": "urn_de_fim_codeliste_dokumenttyp_4_0"
}
],
"Cmd": {
19

-- 19 of 66 --

"xsdDir": "path/to/xsdDir",
"outputDir": "path/to/output",
"allowRemoteXsd": true,
"clXmlsDir": "path/to/clXmlsDir",
"mainXsd": "example_main_xsd",
"rootElement": "ExampleRootElement",
"minimal": true,
"roles": "admin, freigebendestelle, redakteur, pruefer",
"genDocModelName": "example_document_model",
"externalTypeDefs": true
}
}
}
Supported Type Mappings
Supported type mappings between XSD types and Document Model field types for use in
transformation configuration files.
Supported XSD Type Document Model Field Type
xs:date DateType
xs:time TimeType
xs:dateTime DateTimeType
xs:string EnumerationType
xs:decimal, xs:integer, xs:long, xs:int, xs:short,
xs:byte, xs:nonNegativeInteger,
xs:positiveInteger, xs:nonPositiveInteger,
xs:negativeInteger, xs:unsignedLong,
xs:unsignedInt, xs:unsignedShort,
xs:unsignedByte, xs:float, xs:double
NumberType
xs:string, xs:anyType (also when no type is
declared; the field is annotated with
_XSD_ANY_TYPE)
StringType
xs:gYearMonth, xs:gYear, xs:gMonthDay, xs:gDay,
xs:gMonth, xs:anyURI, xs:ID, xs:IDREF,
xs:language
StringWithXsPatternType
xs:boolean BooleanType
No restriction. EnumForBooleanType
Any type with a custom pattern. EnumForStringType
No restriction. ConfirmType
Supported Characters Configuration
Two configuration options are available:
20

-- 20 of 66 --

1. supportedCharactersTypeInXsd: Specify the name of an XSD simple type that contains a pattern
facet. The transformer will extract the pattern from this type and generate the list of supported
characters. This type can be defined in the main XSD or in any included XSD schema via
<xs:include>.
2. supportedCharacters: Directly provide a regex pattern string. The transformer will match all
characters in the Basic Multilingual Plane (BMP) against this pattern to generate the list of
supported characters.
 If both are provided, the transformation will fail with the error message: "Both
supportedCharacters and supportedCharactersTypeInXsd were provided.
Exactly one of them must be set."
Usage Notes:
• The specified simple type must contain a <xs:pattern> facet in its restriction.
• The transformer searches recursively through all included schemas to find the specified type.
Example with supportedCharactersTypeInXsd:
{
"Configuration": {
"version": "15.12.2024",
"timeZone": "UTC",
"supportedCharactersTypeInXsd": "datatypeC"
}
}
This will search for a simple type named datatypeC in the main XSD and all included schemas,
extract its pattern, and generate the supported characters list.
Example with supportedCharacters:
{
"Configuration": {
"version": "15.12.2024",
"timeZone": "UTC",
"supportedCharacters": "[a-zA-Z0-9\\s]"
}
}
This will directly use the provided regex pattern to generate the list of supported characters (letters,
digits, and whitespace).
Overriding Default Type Mapping Behavior
Beyond simple type mapping, you can customize the field type properties for each XSD type
mapping. This allows precise control over how XSD types are transformed into Document Model
21

-- 21 of 66 --

field types.
For each Document Model field type, you can specify nested configuration objects with type-specific
properties:
Document Model Field Type Supported Properties
StringType pattern, minLength, maxLength,
lineBreaksPermitted, alphabeticalSorting
NumberType minValue, maxValue, minFractionalDigits,
maxFractionalDigits, maxIntegerDigits,
leadingZerosAllowed, positivesOnly,
zeroNotAllowed, trait
EnumerationType alphabeticalSorting

Multiple Type Mappings With the Same XSD Type
If you define multiple TypeMapping entries with the same xsdType, only the first
matching entry in the list will be applied. Subsequent entries with the same
xsdType will be ignored.
Example:
{
"TypeMapping": [
{
"xsdType": "string",
"a12Type": "StringType",
"StringType": {
"maxLength": 100
}
},
{
"xsdType": "string", // This entry will be IGNORED
"a12Type": "StringType",
"StringType": {
"maxLength": 500
}
}
]
}
In this example, all XSD string types will be transformed with maxLength: 100. The
second mapping is never applied because the first one already matches xsdType:
"string".
Best Practice: Ensure each xsdType appears only once in your TypeMapping list to
avoid confusion.
22

-- 22 of 66 --

Common Use Cases
Use Case 1: Mapping xs:long to StringType With Pattern Validation
When an XSD type like xs:long needs to be mapped as a string with pattern validation (e.g., for
phone numbers or IDs):
{
"xsdType": "long",
"a12Type": "StringType",
"StringType": {
"pattern": "[0-9]{1,19}",
"minLength": 1,
"maxLength": 19
}
}
Use Case 2: Mapping Unsupported XSD Patterns to A12-Supported Patterns
When your XSD uses patterns that are not fully supported by A12, you can override them with
supported equivalents:
{
"xsdType": "CustomDatePattern",
"a12Type": "StringType",
"StringType": {
"pattern": "[0-9]{4}-[0-9]{2}-[0-9]{2}",
"minLength": 10,
"maxLength": 10
}
}
Use Case 3: Percentage Values With Constraints
Define a percentage type with specific decimal precision and value range:
{
"xsdType": "PercentageType",
"a12Type": "NumberType",
"NumberType": {
"minValue": 0,
"maxValue": 100,
"maxFractionalDigits": 2,
"positivesOnly": true
}
}
Additional Examples
Basic StringType Override
23

-- 23 of 66 --

Override line breaks permission for multi-line text types:
{
"xsdType": "TextMitZeilenumbruechen",
"a12Type": "StringType",
"StringType": {
"lineBreaksPermitted": true
}
}
Advanced StringType With Pattern and Length Constraints
Define a custom email type with pattern validation:
{
"xsdType": "EmailType",
"a12Type": "StringType",
"StringType": {
"pattern": "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
"minLength": 5,
"maxLength": 100,
"lineBreaksPermitted": false
}
}
NumberType With Value Constraints
Define a price type with decimal precision and range:
{
"xsdType": "PriceType",
"a12Type": "NumberType",
"NumberType": {
"minValue": 0,
"maxValue": 999999.99,
"minFractionalDigits": 2,
"maxFractionalDigits": 2,
"leadingZerosAllowed": false,
"positivesOnly": true
}
}
EnumerationType With Sorting
Enable alphabetical sorting for enumeration values:
{
"xsdType": "StatusEnum",
"a12Type": "EnumerationType",
24

-- 24 of 66 --

"EnumerationType": {
"alphabeticalSorting": true
}
}
Overriding Built-in XSD Types
You can override how built-in XSD types like gYear or integer are transformed:
{
"xsdType": "gYear",
"a12Type": "StringType",
"StringType": {
"pattern": "[0-9]{4}",
"minLength": 4,
"maxLength": 4
}
}

When using type mapping overrides:
• The configuration object name must match the a12Type value (e.g., if a12Type is
"StringType", use "StringType": { … })
• All properties are optional - only specify the properties you want to customize
• Properties not specified will use the transformer’s default behavior
• Pattern strings must be properly escaped (use \\ for backslash in JSON)
• Numeric values (minValue, maxValue) can be integers or decimals depending on
your requirements
Set Up a Gradle Task for the XsdToA12ModelTransformer
To simplify the use of the XsdToA12ModelTransformer, the CLI tool execution can be implemented
as a Gradle task. A snippet of a build.gradle file configuring such a task is shown below:
configurations {
modelTransformer
configFiles
}
dependencies {
modelTransformer "com.mgmtp.a12.transformer:transformer-xsdtomodel-cmd:<version>"
}
// define the paths where the input XSD files are located
def xzufiXsdPath = "src/test/resources/multilayer/xzufi"
def outputPath = layout.buildDirectory.dir('generated-models')
25

-- 25 of 66 --

tasks.register('transformXzufiXsdToA12DocumentModel', JavaExec) {
group = 'xsd-to-a12-doc-model-transformer'
description = 'This task transforms the provided Xzufi XSDs to a Document Model'
dependsOn(
configurations.named('configFiles'), configurations.named('modelTransformer')
)
inputs.files(
configurations.modelTransformer, configurations.configFiles
)
inputs.dir("${xzufiXsdPath}/xsd")
inputs.dir("${xzufiXsdPath}/genericode")
outputs.dir("${outputPath.get().asFile.getPath()}/xzufi")
classpath(configurations.modelTransformer,
configurations.configFiles)
main "org.springframework.boot.loader.launch.JarLauncher"
args "check",
"--transform-config", "config/xzufi-config.json",
"--xsd-dir", "${xzufiXsdPath}/xsd",
"--cl-xmls-dir", "${xzufiXsdPath}/genericode",
"--output-dir", "${outputPath.get().asFile.getPath()}/xzufi",
"--main-xsd", "xzufi-transfer",
"--root-element", "leistung"
}
tasks.register('getTransformerConfigModel', JavaExec) {
group = 'xsd-to-a12-doc-model-transformer'
description = 'This task generates the transformer configuration model template
for use with SME'
dependsOn(
configurations.named('modelTransformer')
)
inputs.files(
configurations.modelTransformer
)
outputs.dir("${outputPath.get().asFile.getPath()}/config-model")
classpath(configurations.modelTransformer)
main "org.springframework.boot.loader.launch.JarLauncher"
args "get-config-model",
"--output-dir", "${outputPath.get().asFile.getPath()}/config-model"
}
tasks.named('processResources') {
finalizedBy(tasks.named('transformXzufiXsdToA12DocumentModel'))
}
26

-- 26 of 66 --

Java API
The XsdToA12ModelTransformer can also be used programmatically via its Java API.
API
The Java interface for the XsdToA12ModelTransformer is defined as follows:
public interface IXmXsdToModelTransformer {
...
/**
* Transforms XSD schema to Document Model using in memory resources.
*
* @param transformationConfig the transformation configuration
* @param inputResources the input resources, including XSD schema, Codelist, etc.
({@link XmResourceType})
* @param notificationConsumer the notification consumer
*
* @return the transformation result
*/
XmResult transform(
TransformerConfigModel transformationConfig,
Collection<XmResource> inputResources,
Consumer<RankedNotification> notificationConsumer
);
/**
* Transforms XSD schema to Document Model using in memory resources.
*
* @param transformationConfig JSON string of transformation config model
* @param inputResources the input resources, including XSD schema, Codelist, etc.
({@link XmResourceType})
* @param notificationConsumer the notification consumer
*
* @return the transformation result
*/
default XmResult transform(
String transformationConfig,
Collection<XmResource> inputResources,
Consumer<RankedNotification> notificationConsumer
) {
...
}
}
Parameters:
• transformationConfig: You can either provide the transformation configuration as a
27

-- 27 of 66 --

TransformerConfigModel instance or as a JSON string. Please find more details about the
configuration in section Configuration Settings.
• inputResources: A collection of input resources, including XSD schemas and code lists files. Each
resource is represented as an XmResource instance, which includes the resource type
(XmResourceType) and the actual content as text.
• notificationConsumer: A consumer for handling notifications during the transformation process.
Return Value:
• XmResult: The result of the transformation, which includes the generated Document Model and
JSON files for Type Definitions (Code List related) if any.
Example Usage
import com.mgmtp.a12.transformer.xsdtodocm.XsdToModelTransformFactory;
public class Sample {
public static void main(String[] args) {
IXmXsdToModelTransformer transformer = new
XsdToModelTransformFactory().createTransformer();
NotificationReceiver notificationReceiver = new NotificationReceiver();
String configJsonContent = """
{
"header": {
"id": "TransformationConfiguration",
"modelType": "transformer"
},
"content": {
"EnumLabels": [],
"Cmd": {
"mainXsd": "sample",
"rootElement": "rootElement",
"genDocModelName": "sample",
"roles": "admin"
}
}
}
""";
String sampleXSDContent = """
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
elementFormDefault="qualified">
<xs:element name="rootElement">
<xs:complexType>
<xs:sequence>
<xs:element name="subElement" type="xs:string"/>
</xs:sequence>
</xs:complexType>
</xs:element>
28

-- 28 of 66 --

</xs:schema>
""";
List<XmResource> resources = List.of(XmResource.builder()
.type(XmResourceType.XSD)
.name("sample")
.content(sampleXSDContent)
.build());
XmResult result = transformer.transform(configJsonContent, resources,
notificationReceiver);
}
}
XSD Discovery Feature
The XSD Discovery feature helps you understand your XSD schemas by extracting useful metadata
about types, elements, and validation rules. Use this feature when you need to explore a new XSD
schema or prepare transformation configurations.
What Information Can You Discover?
The discovery feature extracts:
• Type Information: All simple types defined in your XSD with their names and base types
• Root Elements: All possible root elements you can use for transformation
• Validation Patterns: Regex patterns used for field validation with the fields they apply to
• Element Paths: Complete hierarchy of all elements in the generated model
• Enumeration Values: All enumeration values with their locations in the model
How to Use Discovery
Use the discovering transformer API to extract metadata from your XSD:
import com.mgmtp.a12.transformer.xsdtodocm.XsdToModelTransformFactory;
import com.mgmtp.a12.transformer.xsdtodocm.IXmDiscoveringTransformer;
import com.mgmtp.a12.transformer.common.XmResource;
import com.mgmtp.a12.transformer.common.XmResourceType;
import com.mgmtp.a12.transformer.xsdtodocm.XmDiscoveringResult;
import com.mgmtp.a12.transformer.xsdtodocm.XmConfigInfo;
import java.util.List;
// Create discovering transformer
IXmDiscoveringTransformer transformer = new
XsdToModelTransformFactory().createTransformerWithDiscovery();
// Prepare transformation configuration as JSON string
String transformationConfig = """
{
29

-- 29 of 66 --

"header": {
"id": "MyModelConfig",
"version": "1.0"
},
"content": {
"Cmd": {
"mainXsd": "schema.xsd",
"rootElement": "RootElement",
"roles": "admin",
"genDocModelName": "MyModel"
}
}
}
""";
// Prepare input resources (XSD files)
List<XmResource> inputResources = List.of(
XmResource.builder()
.type(XmResourceType.XSD)
.name("sample")
.content("sampleXSDContent")
.build()
);
// Run discovery
XmDiscoveringResult result = transformer.discover(transformationConfig,
inputResources, notificationConsumer);
XmConfigInfo configInfo = result.getConfigInfo();
// Now you can explore the discovered information
What Can You Do With Discovered Information?
Find Available Root Elements
If you’re not sure which root element to use:
List<String> rootElements = configInfo.getRootElements();
// Shows all valid root elements from your XSD
Explore Type Definitions
See all custom types and their default Document Model field type mappings:
for (var type : configInfo.getSimpleTypes()) {
System.out.println("Type: " + type.getName());
System.out.println(" - Base type: " + type.getXsType());
System.out.println(" - Maps to: " + type.getDefaultMapping());
30

-- 30 of 66 --

}
Find Validation Patterns
See which fields have regex validation and what patterns they use:
for (var entry : configInfo.getPatternFields().entrySet()) {
System.out.println("Pattern: " + entry.getKey());
System.out.println(" - Used by fields: " + entry.getValue());
}
Browse Element Hierarchy
Get the complete list of all element paths in the generated model:
for (String path : configInfo.getElementPaths()) {
System.out.println(path);
}
List Enumeration Values
See all enumeration values and where they’re used:
for (var entry : configInfo.getEnumValues().entrySet()) {
System.out.println("Value: " + entry.getKey());
System.out.println(" - Field path: " + entry.getValue().getFieldPaths());
}
Common Use Cases
Use XSD Discovery to:
• Explore unfamiliar XSD schemas before starting configuration
• Generate configuration file templates with valid type mappings
• Document your schema structure automatically
• Validate configuration files against actual schema structure
• Build schema editors with auto-completion support
Important Notes
• The discover() method is specifically designed to extract configuration metadata from your XSD
schema
• Discovery automatically disables schema validation checks to extract as much metadata as
possible, even from incomplete schemas
31

-- 31 of 66 --

• If you don’t provide a valid root element, you’ll still get discovery results with the available root
elements listed, but other discovery information will be limited
• Discovery is only available through the Java API via IXmDiscoveringTransformer, not through the
CLI tool
• The discovering transformer returns XmDiscoveringResult which extends XmResult and includes
the configInfo field
A12DocumentToXmlTransformer
Description
The A12DocumentToXmlTransformer serves the purpose of transforming A12 documents into XML
documents at runtime by providing a Java library. Besides the transformation of whole A12
documents, it is possible to consider only document parts for the generation of the output XML.
This is further described in section Considering Only Sub-Models for Transformation.
API
The A12DocumentToXmlTransformer supports two modes of operation:
• File-based: Models are loaded from classpath directories at transformer creation time. Use
FileBasedDocumentXmlTransformConfig.
• In-memory (runtime): Models are provided as XmResource instances, enabling integration with
DataServices or other runtime model sources. Use DocumentXmlTransformConfig with a
Collection<XmResource>.
To use the A12DocumentToXmlTransformer API, the following steps must be performed:
1. Instantiate a configuration object (FileBasedDocumentXmlTransformConfig or
DocumentXmlTransformConfig). The members are described in section Configuration.
2. Instantiate an IDocumentXmlTransform object using the DocumentXmlTransformFactory and passing
the configuration.
3. Perform the transformation by calling IDocumentXmlTransform.transform() and passing the input
A12 document using a java.io.Reader object. Additionally, a Consumer<RankedNotification>
instance must be passed to the call, allowing notifications generated by the transformer to be
received and acted upon. The Locale for localizing error messages is configured via the
configuration object (DocumentXmlTransformConfig or FileBasedDocumentXmlTransformConfig). The
call to IDocumentXmlTransform.transform() returns the content of the transformed XML
document as a String.
4. You can provide your own ResourceBundle for localization in any languages by creating your
own properties files. Below is an example of such a properties file:
locale/doc2xml/message_en.properties
# Model reference errors
error.model.not.used=Model ''{0}'' is not used in document model ''{1}''
32

-- 32 of 66 --

error.model.deserialize.failed=Document Model cannot be deserialized: {0}
# State errors
error.state.invalid=Invalid state: {0}
# Transformer errors
error.transformer.secure.processing=The TransformerFactory does not support secure
processing
error.transformer.corrupt.xml=The created XML is corrupt
# Validation errors
error.validation.failed=XML is not valid. See log file for more details: {0}
error.xml.cannot.load=XML ''{0}'' cannot be loaded
# Transformation errors
error.transformation.to.string.representation=Could not transform to String
representation: {0}
# Renderer errors
error.renderer.not.found=Factory cannot find renderer for type: {0}
# Document errors
error.document.unknown.format=Unknown document format: {0}
# Info
info.no.xsd.validation.performed=No xsd validation has been performed.
File-Based Usage
The following code snippet demonstrates how to use the A12DocumentToXmlTransformer with file-
based model loading:
import com.mgmtp.a12.transformer.doctoxml.FileBasedDocumentXmlTransformConfig;
import com.mgmtp.a12.transformer.doctoxml.DocumentXmlTransformFactory;
import com.mgmtp.a12.transformer.doctoxml.IDocumentXmlTransform;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import com.mgmtp.a12.transformer.common.NotificationReceiver;
// Step 1: Setting up the file-based transformer configuration
FileBasedDocumentXmlTransformConfig config =
FileBasedDocumentXmlTransformConfig.builder()
.modelsDir("xflb")
.modelId("")
.refModelId("XFLB-generated")
.schemaPath("xsd/xflb.xsd")
.customRootPath("root.element.path/answer/result")
33

-- 33 of 66 --

.customRootType("xflb:ResultType")
.skipValidation(skipValidation)
.documentFormat(docFormat)
.locale(Locale.GERMAN)
.build();
// Step 2: Constructing an IDocumentXmlTransform instance
IDocumentXmlTransform transformer = new
DocumentXmlTransformFactory().createMultiLayerTransformer(config);
// Step 3: Performing the transformation
Reader a12DocumentReader = new BufferedReader(
new InputStreamReader(
Thread.currentThread().getContextClassLoader().getResourceAsStream(path),
StandardCharsets.UTF_8
)
);
String outputXML = transformer.transform(
a12DocumentReader,
new NotificationReceiver()
);
In-Memory (Runtime) Usage
The following code snippet demonstrates how to use the A12DocumentToXmlTransformer with in-
memory model resources, for example when models are loaded from DataServices at runtime:
import com.mgmtp.a12.transformer.doctoxml.DocumentXmlTransformConfig;
import com.mgmtp.a12.transformer.doctoxml.DocumentXmlTransformFactory;
import com.mgmtp.a12.transformer.doctoxml.IDocumentXmlTransform;
import com.mgmtp.a12.transformer.common.XmResource;
import com.mgmtp.a12.transformer.common.XmResourceType;
import java.util.List;
import java.util.Collection;
// Step 1: Prepare in-memory model resources (e.g., loaded from DataServices)
Collection<XmResource> modelResources = List.of(
XmResource.builder()
.type(XmResourceType.A12_DOCUMENT_MODEL)
.name("XFLB-generated")
.content(modelJsonContent) // JSON string of the serialized document model
.build()
);
// Step 2: Setting up the transformer configuration with in-memory resources
DocumentXmlTransformConfig config = DocumentXmlTransformConfig.builder()
.modelId("")
.refModelId("XFLB-generated")
.documentFormat("json")
34

-- 34 of 66 --

.skipValidation(false)
.locale(Locale.GERMAN)
.inputResources(modelResources)
.build();
// Step 3: Constructing an IDocumentXmlTransform instance
IDocumentXmlTransform transformer = new DocumentXmlTransformFactory()
.createMultiLayerTransformer(config);
// Step 4: Performing the transformation (returns XML String)
String outputXML = transformer.transform(a12DocumentReader, notificationConsumer);
Configuration
The configuration is split into a base class and a file-based subclass:
• DocumentXmlTransformConfig — base configuration used for both in-memory and file-based
transformer creation.
• FileBasedDocumentXmlTransformConfig — extends the base with modelsDir (classpath or
filesystem) for file-based model loading.
Common Configuration (DocumentXmlTransformConfig)
Member Description
modelId ID of the main model.
refModelId Specifies a sub-model wrapped by the main
Document Model. In the transformation step,
only the elements of this sub-model are used.
This is further described in section Considering
Only Sub-Models for Transformation.
documentFormat Format of the input A12 document (JSON or
XML).
customRootPath XPath in the XSD against which the output XML
is validated.
customRootType Type of the specified customRootPath.
skipValidation Specifies whether the output XML document
should be validated against the provided XSD.
locale Locale used for localization of error messages.
35

-- 35 of 66 --

Member Description
schemaPath Path or name of the main XSD schema used to
validate the output XML document. For file-
based usage, this is a classpath path (e.g.
"xsd/xflb.xsd"). For in-memory usage, this is the
resource name matching XmResource.getName()
(e.g. "xflb.xsd"). If null, no XSD validation is
performed.
inputResources In-memory usage only. Collection of XmResource
instances (models and optionally XSD schemas)
used for runtime transformation.
File-Based Configuration (FileBasedDocumentXmlTransformConfig)
Extends DocumentXmlTransformConfig with the following additional members:
Member Description
modelsDir Classpath or filesystem directory containing the
main Document Model and further referenced
models.
Considering Only Sub-Models for Transformation
Person_DM: {
name: String,
address:
}
Person: {
name = "Max",
}
A12toXmlTransformer
modelId 	refModelId
<address>
<street>Street 1</street>
</address>
Address_DM: {
street: String
}
points to	points to
references
input	input
input 	output
corresponds to
address = {
street = "Street 1"
}
A12 Document 	XML
Main A12 Document Model 	Sub A12 Document Model
Address_DM
Figure 2. Example Transformation Scenario in Pseudocode
Besides transforming entire A12 documents, it is possible to convert only parts of them to XML.
Figure 2 depicts an example scenario containing Document Models and an A12 document shown in
pseudocode. In this scenario, a Main Document Model exists, which references a Sub Document
Model. The transformer’s input is a A12 document corresponding to the Main Document Model.
However, the requirement in this scenario is to generate XML based solely on the address
contained in the document that corresponds to the Sub Document Model. To achieve this, a
36

-- 36 of 66 --

reference to the Sub Document Model is passed as the refModelId to the
A12DocumentToXmlTransformer, together with the Main Document Model as the modelId.
Therefore, the transformer only considers the address element of the input A12 document when
generating the output XML.
Excluding Model Elements From the XML (_TRANSFORMER_IGNORE_ELEMENT)
A generated Document Model mirrors its XSD, so every element maps back to a schema element.
Sometimes, though, modelers add helper fields or groups that have no XSD counterpart e.g. in a
Combination Model.
Mark those helper elements with the annotation _TRANSFORMER_IGNORE_ELEMENT so they stay out of the
schema flow in both directions:
• Document → XML: the element is removed before serialization and never appears in the
output.
• XML → Document: the element is stripped on ingest, so values provided for it in the incoming
XML are discarded.
XmlToA12Transformer
Description
The XmlToA12Transformer component allows the transformation of XML documents into A12
documents at runtime by providing a Java library.

Model elements marked with _TRANSFORMER_IGNORE_ELEMENT are stripped while
reading the XML, so values provided for them in the incoming document are
discarded. See Excluding Model Elements From the XML
(_TRANSFORMER_IGNORE_ELEMENT) for details.
API
The XmlToA12Transformer supports two modes of operation:
• File-based: Models are loaded from classpath directories at transformer creation time. Use
FileBasedXdXmlDocumentTransformConfig.
• In-memory (runtime): Models are provided directly, enabling integration with DataServices or
other runtime model sources. Use XdXmlDocumentTransformConfig with one of the following
approaches:
◦ As XmResource instances — provide serialized JSON model resources via modelResources.
◦ As IDocumentModel instances — provide already-deserialized model objects via
documentModels. This skips JSON deserialization and is the preferred approach when the
model is already available in memory (e.g., loaded from DataServices).
To use the XmlToA12Transformer API, the following steps must be performed:
37

-- 37 of 66 --

• Instantiate a configuration object (FileBasedXdXmlDocumentTransformConfig or
XdXmlDocumentTransformConfig)
• Instantiate an IXdXmlDocumentTransform object using the XdXmlDocumentTransformFactory
• Perform the transformation by calling IXdXmlDocumentTransform.transform()
Configuration
Common Configuration (XdXmlDocumentTransformConfig)
Member Description
modelId ID of the main Document Model.
locale Locale used for localization of info/error
messages. Must be specified; otherwise a
NullPointerException will occur.
modelResources In-memory model resources (with transformer
annotations) for runtime model deployment,
provided as serialized JSON (XmResource
instances). Mutually exclusive with
documentModels.
validationModelResources In-memory validation model resources (without
transformer annotations), provided as serialized
JSON (XmResource instances). If null and
validationDocumentModels is also null, the main
model is used for validation.
documentModels In-memory model instances for runtime model
deployment, provided as IDocumentModel objects.
Use this instead of modelResources when the
model is already available in memory — it
avoids JSON deserialization entirely. Mutually
exclusive with modelResources.
+ Note: If your model contains include
references to other models, those referenced
model instances may be structurally modified
during model expansion. Do not reuse or share
those instances after passing them to this
configuration.
38

-- 38 of 66 --

Member Description
validationDocumentModels In-memory validation model instances, provided
as IDocumentModel objects. Use this instead of
validationModelResources when the validation
model is already available in memory — it
avoids JSON deserialization entirely.
+ Resolution priority: validationDocumentModels >
validationModelResources > fall back to the main
model.
+ Note: If your model contains include
references to other models, those referenced
model instances may be structurally modified
during model expansion. Do not reuse or share
those instances after passing them to this
configuration.
File-Based Configuration (FileBasedXdXmlDocumentTransformConfig)
Extends XdXmlDocumentTransformConfig with the following additional members:
Member Description
modelPath Classpath or filesystem path to the directory
containing the main Document Model and
further referenced Document Models (with
transformer annotations).
minimalModelPath Classpath or filesystem path to a minimal
Document Model directory (without transformer
annotations) used for validation. If null, the
modelPath is used for both transformation and
validation.
You can provide your own ResourceBundle for localization in any languages by creating your own
properties files. Below is an example of such a properties file:
locale/xml2doc/message_en.properties
error.model.deserialize.failed=Document Model cannot be deserialized: {0}
error.unknown.field.type=Encountered unknown field data type '{0}'
error.validation.invalid=Document is not valid
error.field.not.found.cannot.convert=Field '{0}' was not found, therefore the value
cannot be converted.
error.ifield.group.not.found=No iField was found matching the multi/single select
group {0}
error.no.resource.available=No resource available at path: {0}
error.invalid.params=The given parameters are not valid.
error.null.pointer={0} must not be null.
info.validation.not.performed=No document validation has been done
39

-- 39 of 66 --

info.validation.valid=Document is valid
Step 1: Instantiating the IXdXmlDocumentTransform
File-Based Instantiation
import com.mgmtp.a12.transformer.xmltodoc.FileBasedXdXmlDocumentTransformConfig;
import com.mgmtp.a12.transformer.xmltodoc.XdXmlDocumentTransformFactory;
import com.mgmtp.a12.transformer.xmltodoc.IXdXmlDocumentTransform;
import java.util.Locale;
FileBasedXdXmlDocumentTransformConfig config =
FileBasedXdXmlDocumentTransformConfig.builder()
.modelPath("xflb")
.minimalModelPath("xflb_minimal")
.modelId("XFLB_DM")
.locale(Locale.GERMAN)
.build();
IXdXmlDocumentTransform transformer = new
XdXmlDocumentTransformFactory().createTransformer(config);
In-Memory (Runtime) Instantiation
Use this approach when models are loaded from DataServices or another runtime source:
import com.mgmtp.a12.transformer.xmltodoc.XdXmlDocumentTransformConfig;
import com.mgmtp.a12.transformer.xmltodoc.XdXmlDocumentTransformFactory;
import com.mgmtp.a12.transformer.xmltodoc.IXdXmlDocumentTransform;
import com.mgmtp.a12.transformer.common.XmResource;
import com.mgmtp.a12.transformer.common.XmResourceType;
import java.util.List;
import java.util.Collection;
import java.util.Locale;
// Prepare in-memory model resources (e.g., loaded from DataServices)
Collection<XmResource> modelResources = List.of(
XmResource.builder()
.type(XmResourceType.A12_DOCUMENT_MODEL)
.name("XFLB_DM")
.content(modelJsonContent) // JSON string of the serialized document model
.build()
);
XdXmlDocumentTransformConfig config = XdXmlDocumentTransformConfig.builder()
.modelId("XFLB_DM")
.modelResources(modelResources)
.locale(Locale.GERMAN)
.build();
40

-- 40 of 66 --

IXdXmlDocumentTransform transformer = new
XdXmlDocumentTransformFactory().createTransformer(config);
In-Memory Instantiation with IDocumentModel Instances
If your application already holds IDocumentModel objects in memory (for example, models loaded
from DataServices), pass them directly via documentModels to avoid JSON deserialization. Use
validationDocumentModels to supply a separate validation model the same way:
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.transformer.xmltodoc.XdXmlDocumentTransformConfig;
import com.mgmtp.a12.transformer.xmltodoc.XdXmlDocumentTransformFactory;
import com.mgmtp.a12.transformer.xmltodoc.IXdXmlDocumentTransform;
import java.util.Collection;
import java.util.Locale;
// Main model only — validation falls back to the same model
XdXmlDocumentTransformConfig config = XdXmlDocumentTransformConfig.builder()
.modelId("XFLB_DM")
.locale(Locale.GERMAN)
.documentModels(myDocumentModels) // Collection<IDocumentModel>
.build();
// With validation model
XdXmlDocumentTransformConfig configWithValidation =
XdXmlDocumentTransformConfig.builder()
.modelId("XFLB_DM")
.locale(Locale.GERMAN)
.documentModels(myDocumentModels) // IDocumentModel instances for
transformation
.validationDocumentModels(myValidationModels) // IDocumentModel instances for
validation
.build();
IXdXmlDocumentTransform transformer = new
XdXmlDocumentTransformFactory().createTransformer(config);
 modelResources and documentModels are mutually exclusive — set one or the other,
not both. The factory will throw an exception if neither is provided.
Step 2: Performing the Transformation
Perform the transformation by calling IXdXmlDocumentTransform.transform() and passing the
following parameters:
• xmlReader: The input XML document, passed as a java.io.Reader object.
• validate: A flag specifying if the output A12 document should be validated against the
41

-- 41 of 66 --

previously configured Document Model.
• notificationConsumer: A java.util.function.Consumer instance to handle notifications that occur
during the transformation process.
The following code snippet demonstrates a complete file-based example:
import java.util.ArrayList;
import com.mgmtp.a12.transformer.xmltodoc.FileBasedXdXmlDocumentTransformConfig;
import com.mgmtp.a12.transformer.xmltodoc.IXdXmlDocumentTransform;
import com.mgmtp.a12.transformer.xmltodoc.XdXmlDocumentTransformFactory;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import com.mgmtp.a12.model.notification.RankedNotification;
import java.util.Collection;
import java.util.function.Consumer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.notification.Severity;
// Step 1: Constructing the IXdXmlDocumentTransform instance
FileBasedXdXmlDocumentTransformConfig config =
FileBasedXdXmlDocumentTransformConfig.builder()
.modelPath("xflb")
.minimalModelPath("xflb_minimal")
.modelId("XFLB_DM")
.locale(Locale.GERMAN)
.build();
IXdXmlDocumentTransform transformer = new
XdXmlDocumentTransformFactory().createTransformer(config);
// Step 2: Performing the transformation
Reader inputXmlReader = new BufferedReader(
new InputStreamReader(
new FileInputStream("xflb.xml"),
StandardCharsets.UTF_8
)
);
Collection<RankedNotification> transformationNotifications = new ArrayList<>();
Consumer<RankedNotification> notificationConsumer = transformationNotifications::add;
DocumentV2 doc = transformer.transform(
inputXmlReader,
true,
notificationConsumer
);
if (transformationNotifications.stream()
.anyMatch(notification -> Severity.ERROR.equals(notification.getSeverity()))) {
// react to transformation errors
42

-- 42 of 66 --

}
XmlSchemaValidator
Description
The XmlSchemaValidator allows validation of an XML document against a provided XSD.
API
To use the XmlSchemaValidator API, the following steps must be performed:
1. Using the XmlValidatorFactory, instantiate an IXmlValidator object. A path to the XSD used for
validation is passed as a parameter during instantiation.
2. Perform the validation of an XML document by executing IXmlValidator.validate() and passing
the XML document, as a Reader or InputStream, to be validated. A specific XML path can be
specified to restrict validation to a subset of the XML content, and a Locale to localize error
messages to the specified language.
If you don’t provide a Locale, Locale.ENGLISH is used default.
You can provide your own ResourceBundle for localization in any languages by create your own
properties files. Below is an example of such a properties file:
resources/xmlschemavalidator/locale/message_en.properties
error.root.path.missing=For partial validation the path must be present in the XML:
{0}
error.sax.secure.processing=SAXParserFactory implementation does not support secure
processing
error.path.invalid=Invalid path ''{0}''
The call to IXmlValidator.validate() returns an XmlValidationResult object, which contains data
about the validation result. For example, the method XmlValidationResult.isOK() returns whether
the provided XML document conforms to the XSD.
The following code snippet demonstrates the utilization of the XmlSchemaValidator:
Classpath-based usage
import com.mgmtp.a12.transformer.schemavalidator.IXmlValidator;
import com.mgmtp.a12.transformer.schemavalidator.XmlValidatorFactory;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import com.mgmtp.a12.transformer.schemavalidator.XmlValidationResult;
43

-- 43 of 66 --

// Step 1: Constructing a validator instance and passing the XSD used for validation
String xsdPath = "path/to/example.xsd";
IXmlValidator iXmlValidator = XmlValidatorFactory.getValidator(xsdPath);
// Step 2: Reading and validating an XML document
Reader xmlInputReader = new InputStreamReader(
new FileInputStream("path/to/input.xml"),
StandardCharsets.UTF_8
);
XmlValidationResult xmlValidationResult = iXmlValidator.validate(xmlInputReader,
Locale.GERMAN);
if (xmlValidationResult.isOK()) {
// Continue if the XML is valid
}
In-memory usage
import com.mgmtp.a12.transformer.schemavalidator.IXmlValidator;
import com.mgmtp.a12.transformer.schemavalidator.XmlValidatorFactory;
import com.mgmtp.a12.transformer.common.XmResource;
import com.mgmtp.a12.transformer.common.XmResourceType;
import com.mgmtp.a12.transformer.common.XmResourceUtils;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import com.mgmtp.a12.transformer.schemavalidator.XmlValidationResult;
// Step 1: Load XSD resources from a directory and create a validator
Collection<XmResource> xsdResources =
XmResourceUtils.getResourcesFromPath("path/to/xsd", XmResourceType.XSD);
IXmlValidator iXmlValidator = XmlValidatorFactory.getValidator(xsdResources,
"example.xsd");
// Step 2: Reading and validating an XML document
Reader xmlInputReader = new InputStreamReader(
new FileInputStream("path/to/input.xml"),
StandardCharsets.UTF_8
);
XmlValidationResult xmlValidationResult = iXmlValidator.validate(xmlInputReader,
Locale.GERMAN);
if (xmlValidationResult.isOK()) {
// Continue if the XML is valid
}
44

-- 44 of 66 --

A12ModelToJsonSchemaTransformer

Experimental Component — This feature is under active development and not
yet recommended for production use. Functionality may be incomplete, and you
may encounter unexpected behaviour or errors. Use it for evaluation purposes
and report any issues.
Description
The A12ModelToJsonSchemaTransformer enables the transformation of A12 Document Models into
JSON Schema files. A12 Documents validate against the JSON Schemas transformed from their
corresponding A12 Document Models.
Three integration paths are available:
• A Gradle Plugin for automated, build-time generation inside a Gradle project
• A standalone CLI JAR (transformer-model-jsonschema-cmd) for direct command-line use without
a Gradle project
• A Workspace Converter for use inside an A12 Workspace Converter Framework (WCF)
pipeline
Gradle Plugin Configuration
To use the A12ModelToJsonSchemaTransformer Gradle plugin, follow these steps:
Step 1: Apply the Plugin
Add the plugin to your build.gradle file:
plugins {
id 'com.mgmtp.a12.transformer.plugins.modeltojsonschema' version '<version>'
}
Step 2: Configure the Task
Configure the generateJsonSchema task by specifying the input A12 Document Model file:
tasks.named('generateJsonSchema') {
inputFile.set(file('src/test/resources/document_models/Example_DM.json'))
}
The generateJsonSchema task accepts the following configuration:
• inputFile: A file property that specifies the path to the A12 Document Model JSON file to be
transformed. This parameter is required.
45

-- 45 of 66 --

Step 3: Run the Task
Execute the task using Gradle:
./gradlew generateJsonSchema
The generated JSON Schema file will be written to the build/generated/jsonschema directory. The
output file name is derived from the input file name, replacing the .json extension with
.schema.json.
For example, if the input file is Example_DM.json, the output will be Example_DM.schema.json.
Standalone CLI
The transformer-model-jsonschema-cmd-<version>.jar is a self-contained executable JAR that can be
run directly with the JRE, without a Gradle project.
Usage
java -jar transformer-model-jsonschema-cmd-<version>.jar [OPTIONS] <inputFile>
Options
Option Description
<inputFile> (Required) Path to the A12 document model
JSON file.
-o, --output <outputDir> Output directory for the generated JSON
Schema. Defaults to build/generated/jsonschema
(relative to the working directory).
-g, --group <groupId> ID of a group in the document model to
transform. When specified, only this group is
transformed into a JSON Schema. The group can
be at any nesting level in the document model.
When omitted, the first root group is used
(default behaviour).
-h, --help Print usage help and exit.
-V, --version Print version information and exit.
Exit Codes
Code Meaning
0 Transformation completed successfully.
1 Invalid arguments (usage error).
46

-- 46 of 66 --

Code Meaning
2 Execution error (e.g. input file not found,
transformation failed, specified group ID not
found).
Examples
Transform the whole document model (default — uses the first root group):
java -jar transformer-model-jsonschema-cmd-<version>.jar \
path/to/MyModel_DM.json \
--output path/to/output
Transform only a specific group by its ID:
java -jar transformer-model-jsonschema-cmd-<version>.jar \
path/to/MyModel_DM.json \
--group group_c3181 \
--output path/to/output
The --group option is useful when the document model contains multiple groups and you only need
to generate a JSON Schema for a particular one. The group ID corresponds to the id field of the
group element in the A12 Document Model JSON (e.g. "id": "group_c3181").
Workspace Converter
The Workspace Converter does the following:
1. scans the workspace for a Workspace Transformation Model (a workspace may contain at most
one transformation model; if more than one is present, an error is reported and nothing is
transformed),
2. transforms the document models it points to into JSON Schemas,
3. writes the generated schemas back into the workspace under output path json-schemas, and
4. removes the consumed transformation model.
Output files are named <model name>.schema.json for whole-model transformations and
<groupId>.schema.json when a specific group is targeted.
Workspace Transformation Model
The JSON-Schema-Transformation-Model is an A12 model of type json_schema_transformer. Its
content.transformationType property selects one of the supported modes:
• ALL_MODELS — transform every document model in the workspace:
47

-- 47 of 66 --

{
"header": {
"id": "json_schema_transformation_model",
"modelType": "json_schema_transformer"
},
"content": {
"transformationType": "ALL_MODELS"
}
}
• SELECTIVE — transform only the document models (or specific groups within them) listed under
documentModelSelections:
{
"header": {
"id": "json_schema_transformation_model",
"modelType": "json_schema_transformer"
},
"content": {
"transformationType": "SELECTIVE",
"documentModelSelections": [
{ "name": "Example_DM" },
{ "name": "MultiGroup_DM", "groupId": "group_nested" }
]
}
}
Each entry in documentModelSelections accepts:
◦ name (required) — the id of the document model in the workspace (matched against the
header id). Selections whose name does not resolve to a document model are skipped with a
warning.
◦ groupId (optional) — the id attribute of a group within that model, with the same semantics
as the standalone CLI’s --group option. When omitted, the model is transformed as a whole
(i.e. the first root group is used). The group can be at any nesting level.
A missing or unknown transformationType is treated as a no-op — the workspace is left unchanged
and a warning is logged.
JSON Schema Mapping
This section describes how different aspects of an A12 Document Model are represented in the
generated JSON Schema.
Field Type Mappings
The following table shows how A12 field types and their attributes are mapped to JSON Schema:
48

-- 48 of 66 --

A12 Field Type JSON Schema Type A12 Field Type Attribute
Mapping
StringType string • minLength → minLength
• maxLength → maxLength
• pattern → pattern
• errorMessage → not mapped
• lineBreaksPermitted → not
mapped
• alphabeticalSorting → not
mapped
• hintList → not mapped
NumberType number • minValue → minimum
• maxValue → maximum
• maxFractionalDigits →
multipleOf (calculated as
10^-maxFractionalDigits)
• minFractionalDigits → not
mapped
• leadingZerosAllowed → not
mapped
• trait → not mapped
DateType string format: "date" (fixed, no
properties to map)
DateTimeType string format: "date-time" (fixed, no
properties to map)
49

-- 49 of 66 --

A12 Field Type JSON Schema Type A12 Field Type Attribute
Mapping
DateRangeType string • format, rangeSeparator →
pattern: "^\\d{4}-\\d{2}-
\\d{2}/\\d{4}-\\d{2}-
\\d{2}$" (fixed pattern for
date range validation)

The JSON
Schema
transformatio
n for
DateRangeType
currently
supports only
the standard
date format
(yyyy-MM-dd)
with the /
separator.
While A12
Document
Models allow
custom format
and
rangeSeparator
attributes (e.g.,
yyyy-MM-
dd’T’HH:mm:ss
or custom
separators),
these are not
reflected in the
generated
JSON Schema
pattern. The
fixed pattern
validates date
ranges in the
format YYYY-
MM-DD/YYYY-MM-
DD (e.g., "2023-
01-15/2023-12-
31").
50

-- 50 of 66 --

A12 Field Type JSON Schema Type A12 Field Type Attribute
Mapping
DateFragmentType string • formatOfFragment → pattern:
"^\\d{4}-(0[1-9]|1[0-2])$"
(fixed pattern for year-
month validation)

The JSON
Schema
transformatio
n for
DateFragmentTy
pe currently
supports only
the yyyy-MM
format
pattern.
BooleanType boolean no properties to map
ConfirmType boolean no properties to map
EnumerationType string • values → enum array
(contains all enumeration
values)
• categories → not mapped
• alphabeticalSorting → not
mapped
• errorMessage → not mapped
TypeDefType $ref Reference to
#/$defs/<typeDefinitionId>
(resolved from the type
definition’s field type)
Document Structure Mapping
The A12 Document Model structure is mapped to JSON Schema as follows:
• Root Schema Object: The generated JSON Schema is an object with type: "object" and contains
two required properties:
◦ id: A string property for the document ID
◦ The root group from the A12 Document Model
• Groups: Each group in the A12 Document Model is represented as an object in JSON Schema:
◦ If the group has repeatability = 1 (single occurrence), it is mapped to a JSON object with
type: "object"
◦ If the group has repeatability > 1 (multiple occurrences), it is mapped to a JSON array with
51

-- 51 of 66 --

type: "array", and the group structure is defined in the items property. The array
constraints minItems: 1 and maxItems: <repeatability> are set accordingly.
• Nested Groups: Groups can contain other groups, creating nested object structures in the JSON
Schema. Each nested group follows the same mapping rules.
• Properties: Each group contains a properties object that defines all fields and nested groups
within that group.
• Required Fields: Fields marked as required in the A12 Document Model are added to the
required array in the corresponding JSON Schema object.
• Additional Properties: All generated objects have additionalProperties: false to ensure strict
validation against the schema.
• Type Definitions: Type definitions (TypeDefType) from the A12 Document Model are collected
and placed in the $defs section at the root of the JSON Schema. These definitions can be
referenced from fields using $ref.
• Include Groups: A group that includes another document model (via includeConfig.reference)
is mapped to a $ref pointing at the referenced model’s schema as a whole, identified by that
model’s $id URN (urn:schemas:a12:<referenced model id>:document). The include group is not
expanded inline; the reference resolves against the referenced model’s generated schema. For
example, an include group named includedGroup referencing To_be_included becomes
"includedGroup": { "$ref": "urn:schemas:a12:To_be_included:document" }.
Example
Given an A12 Document Model with the following structure:
{
"header": {
"id": "Example_DM"
},
"content": {
"typeDefinitions": [
{
"id": "typedef_cca33e87-6f84-4e80-af8d-82dc8e54708c",
"name": "exampleTypeDefinition",
"fieldType": {
"type": "StringType"
}
}
],
"modelRoot": {
"rootGroups": [{
"name": "rootGroup",
"elements": [
{
"type": "Field",
"id": "field_256b5",
"name": "firstName",
"fieldType": {
52

-- 52 of 66 --

"type": "StringType",
"StringType": {
"maxLength": 50
}
},
"requirednessConfig": { }
},
{
"type": "Field",
"id": "field_256b6",
"name": "age",
"fieldType": {
"type": "NumberType",
"NumberType": {
"minValue": 0,
"maxValue": 150
}
}
},
{
"type": "Field",
"id": "field_256b7",
"name": "exampleTypeDefField",
"fieldType": {
"type": "TypeDefType",
"TypeDefType": {
"typeDefinitionId": "typedef_cca33e87-6f84-4e80-af8d-82dc8e54708c"
}
}
}
]
}]
}
}
}
The generated JSON Schema would include:
{
"$schema": "https://json-schema.org/draft/2020-12/schema",
"$id": "urn:schemas:a12:Example_DM:document",
"title": "Example_DM document",
"$defs": {
"typedef_cca33e87-6f84-4e80-af8d-82dc8e54708c": {
"type": "string"
}
},
"type": "object",
"additionalProperties": false,
"properties": {
53

-- 53 of 66 --

"id": {
"type": "string"
},
"rootGroup": {
"type": "object",
"additionalProperties": false,
"properties": {
"firstName": {
"type": "string",
"maxLength": 50
},
"age": {
"type": "number",
"minimum": 0,
"maximum": 150
},
"exampleTypeDefField": {
"$ref": "#/$defs/typedef_cca33e87-6f84-4e80-af8d-82dc8e54708c"
}
},
"required": ["firstName"]
}
},
"required": ["id", "rootGroup"]
}
This example demonstrates how:
• The root group rootGroup becomes an object property
• String and Number field types are mapped with their constraints
• Required fields appear in the required array
• Type definitions from the A12 Document Model are placed in the $defs section with the type
definition’s ID as the key
• Fields using TypeDefType reference the type definition using $ref:
"#/$defs/<typeDefinitionId>"
• The type definition itself is resolved to its underlying field type (in this case, a simple StringType
without additional constraints)
• The document structure maintains the hierarchy from the A12 Document Model
Transformer Workspace Converter
Description
XmTransformerModelConverter integrates the XSD-to-A12 document model transformation into the
A12 Workspace Converter Framework (WCF). For every transformer model in a Workspace, it runs
the same transformation as XsdToModelTransformer, then writes the generated runtime document
54

-- 54 of 66 --

models back into the workspace.
The converter is annotated with @WcfConverter(order = 40) and discovered automatically by the
WCF runtime, so application code does not instantiate it directly. It is functionally equivalent to the
generate-model CLI command, repackaged for use inside a workspace pipeline.
Workspace Input Contract
For the converter to do useful work, the caller must populate the workspace with:
• one or more ModelTuple entries in workspace.getModels() whose header modelType is transformer
and whose content is a JSON TransformerConfigModel — the same JSON shape the generate-model
CLI accepts via --transform-config;
• one entry per XSD file in workspace.getFiles(), keyed by the file name (e.g. xflb_dummy.xsd);
• workspace.getInputDir() pointing at the directory where those XSD files live on disk.
Only file names ending in .xsd are picked up. The converter reads each XSD’s bytes from
inputDir/<file name> on disk; it does not read FileTuple.getContent().
Behavior
For each call to convert(Workspace):
1. Filter workspace.getModels() for tuples whose header modelType equals transformer. If none are
found, the workspace is returned unchanged.
2. Load all .xsd files listed in workspace.getFiles() from workspace.getInputDir() as XmResource
instances.
3. Transform every transformer model with IXmXsdToModelTransformer. Notifications with
Severity.ERROR are collected and surfaced as an IllegalStateException if the result is not
successful.
4. For each generated document model add a DefaultModelTuple to workspace.getModels() keyed by
the model id (typically the value of Cmd.genDocModelName in the transformer config). The original
transformer model entries are left untouched.
Maven Coordinates
implementation 'com.mgmtp.a12.transformer:transformer-xsd-model-workspace-converter'
Deployment & Configuration
Artifacts
To utilize the Transformer components, the following Gradle dependency statements must be
added to the application’s build.gradle file:
55

-- 55 of 66 --

dependencies {
// XsdToA12ModelTransformer dependency
implementation("com.mgmtp.a12.transformer:transformer-xsdtomodel-cmd:<version>")
// A12DocumentToXmlTransformer dependency
implementation("com.mgmtp.a12.transformer:transformer-doctoxml:<version>")
// XmlToA12Transformer dependency
implementation("com.mgmtp.a12.transformer:transformer-xmltodoc:<version>")
// Tranformer common
implementation("com.mgmtp.a12.transformer:transformer-common:<version>")
}
API Documentation
JavaDoc
• Transformer javadoc
Migration Instructions
 Please have a look at Migration to latest A12 chapter for an explanation of general
steps on how to upgrade before starting with the component migration.
Model Migration Tool
To migrate your Transformer models, first install or update the migration tool with
npm install -g @com.mgmtp.a12.transformer/transformer-model-migration
Then run the following command to perform the migration:
transformer-model-migration <path to transformer model file>
Note that if the given path points to a directory instead, it will be searched recursively for
Transformer models to migrate. In case you do not have your Transformer models under version
control, you can set the optional -b flag to create backups of your models. Use the -h flag to display
all available options.
 It is essential that only valid models are migrated, e.g. models that do not contain
consistency problems. Migrating models with problems might lead to undefined
56

-- 56 of 66 --

behavior.
2026.06
Version: 3.0.0
Breaking Changes
XSD-to-Model Transformer No Longer supports parameter /content/Cmd/genDocModelName in the
Transformer Config Model
Transformer parameter /content/Cmd/genDocModelName has been removed from the Transformer
Config Model (configuration file). It is only available in the command-line tool as an argument to
specify the name and the /header/id of the generated Document Model.
Required
Depending on your usage of genDocModelName in your Transformer Config Model.
Migration
Automatic by using transformer-model-migration.
Result
The /content/Cmd/genDocModelName should be removed from the Transformer config file.
Migration Steps
1. Remove the genDocModelName parameter from your transformation configuration file.
2. When using the command-line tool, specify the generated Document Model name using the
--gen-doc-model-name argument when the configuration file does not present.
3. The generated Document Model’s file name and the /header/id field in the generated Document
Model will be set to the value provided in the /header/id field of the transformation
configuration file (or the --gen-doc-model-name argument).
Repeatable Code List Groups No Longer Produce a Multi-Select Companion Group in the
Generated Document Model
Previously, the XSD to model transformer generated an additional _ms (multi-select) for a repeatable
code list group, alongside the original code list group. Now, the code list group itself is directly
repeatable and the _ms group no longer exists.
Required
Yes, if your XSD contains repeatable code list groups.
Migration
Manual.
Result
Regenerated models no longer contain _ms-suffixed groups; code referencing them must be
57

-- 57 of 66 --

updated.
Before
Foerdernehmer (Group, repeatability: 1, annotations: [_XSD_CODE_LIST_GROUP,
_XM_REPLACEMENT_PATH=Value_ms])
└── optionCode (Field)
└── listURI (Field)
└── listVersionID (Field)
Value_ms (Group, repeatability: 19, usageType: multi-select)
└── value (Field, TypeDefType)
After
Foerdernehmer (Group, repeatability: 1999999999, annotations: [_XSD_CODE_LIST_GROUP])
└── optionCode (Field)
└── listURI (Field)
└── listVersionID (Field)
Migration Steps
1. Regenerate your document models from XSD using the updated transformer.
2. Update any code or configuration that references _ms-suffixed group names derived from code
list groups — these groups no longer exist in the model.
3. Expect the code list group itself to carry the repetition, instead of a separate _ms group.
4. The Document-to-XML transformer is also affected: all existing documents that match the older
document model must be migrated. Please contact the transformer team for support with
migrating old documents.
Extended xs:choice Support Changes the Generated Document Model Structure
The transformer now handles more xs:choice patterns: multiple sibling choices in the same parent,
sequences inside a choice, and nested choices. As a result, the generated document model structure
for these patterns will differ from what was produced before.
Required
Yes, if your XSD uses multiple sibling choices, sequences inside a choice, or nested choices.
Migration
Manual. Regenerate models and review structural changes.
Result
Generated document model structure differs for affected xs:choice patterns.
New Config API for Doc-to-XML and XML-to-Doc Transformers
Each transformer config has been split into a base class (for in-memory resources) and a file-
based subclass (for classpath/filesystem loading). This also adds in-memory model loading support
58

-- 58 of 66 --

via Collection<XmResource>.
Required
Yes, if you use DocumentXmlTransformConfig or
XdXmlDocumentTransformFactory.createTransformer(String, String, String, Locale).
Migration
Manual. Renames builder classes and rewrites factory method calls.
Result
Replace DocumentXmlTransformConfig with FileBasedDocumentXmlTransformConfig; replace
positional createTransformer calls with config-object overloads.
Migration Steps
1. Replace DocumentXmlTransformConfig.builder() with
FileBasedDocumentXmlTransformConfig.builder(). The schemaPath field remains the same.
Before
DocumentXmlTransformConfig config = DocumentXmlTransformConfig.builder()
.modelsDir("xflb")
.modelId("...")
.refModelId("...")
.schemaPath("path/to/schema.xsd")
.documentFormat("json")
.build();
IDocumentXmlTransform transformer = factory.createMultiLayerTransformer(config);
After
FileBasedDocumentXmlTransformConfig config =
FileBasedDocumentXmlTransformConfig.builder()
.modelsDir("xflb")
.modelId("...")
.refModelId("...")
.schemaPath("path/to/schema.xsd")
.documentFormat("json")
.build();
IDocumentXmlTransform transformer = factory.createMultiLayerTransformer(config);
2. Replace XdXmlDocumentTransformFactory.createTransformer(String, String, String, Locale)
with createTransformer(FileBasedXdXmlDocumentTransformConfig).
Before
IXdXmlDocumentTransform transformer = factory.createTransformer(modelPath,
minimalModelPath, modelId, locale);
59

-- 59 of 66 --

After
FileBasedXdXmlDocumentTransformConfig config =
FileBasedXdXmlDocumentTransformConfig.builder()
.modelPath("xflb")
.minimalModelPath("xflb_minimal")
.modelId("XFLB_DM")
.locale(Locale.GERMAN)
.build();
IXdXmlDocumentTransform transformer = factory.createTransformer(config);
3. Update import statements — see Shared Types Moved to transformer-common Module.
4. In-memory model support is a new optional feature. Your existing file-based code continues to
work after applying the steps above.
For complete documentation, see the A12DocumentToXmlTransformer, XmlToA12Transformer, and
XmlSchemaValidator sections.
Shared Types Moved to transformer-common Module
The shared types XmResource, XmResourceType, and XmResult have been moved from the
com.mgmtp.a12.transformer.xsdtodocm package to the com.mgmtp.a12.transformer.common package in
the transformer-common module. This enables these types to be used across all transformer
directions (XSD-to-Model, Doc-to-XML, XML-to-Doc, and XML Schema Validator).
Required
Yes, if you import XmResource, XmResourceType, or XmResult from
com.mgmtp.a12.transformer.xsdtodocm.
Migration
Manual. Rewrites import paths.
Result
Update imports from xsdtodocm.* to common.* (e.g., com.mgmtp.a12.transformer.common.XmResource).
Migration Steps
1. Update all import statements:
Old import New import
com.mgmtp.a12.transformer.xsdtodocm.XmResour
ce
com.mgmtp.a12.transformer.common.XmResource
com.mgmtp.a12.transformer.xsdtodocm.XmResour
ceType
com.mgmtp.a12.transformer.common.XmResourceT
ype
com.mgmtp.a12.transformer.xsdtodocm.XmResult com.mgmtp.a12.transformer.common.XmResult
2. Update your Gradle/Maven dependencies to include transformer-common if not already present.
60

-- 60 of 66 --

PatternErrors: Default UPDATE_MESSAGE Action and XmPatternErrorsLayer Post-Processing
The PatternErrors configuration has been reworked. Error message customization for patterns is
now handled exclusively by the new XmPatternErrorsLayer post-processing layer, which runs after
the initial XSD parsing.
1. action field is optional. If action is absent, the transformer uses UPDATE_MESSAGE by default. This
preserves the original XSD pattern and updates only the error message from errors.
2. Pattern matching is by exact regex string only. The pattern value in each entry must be the
exact regex as it appears in the generated StringType — the same string that comes from the
XSD <xs:pattern> facet or is assigned by the transformer for built-in types. Symbolic names (e.g.,
XS_ANY_URI_PATTERN, XS_G_YEAR) are no longer recognized and must be replaced with the
corresponding actual regex.
3. New action values are available:
◦ UPDATE_MESSAGE — keep the original pattern, replace error message from errors.
◦ REPLACE — replace pattern with replacement, set error message from errors.
◦ SUPPRESS — remove both pattern and error message.
Required
Depending on your usage of symbolic pattern names (e.g., XS_ANY_URI_PATTERN) in
PatternErrors configuration.
Migration
Manual. Replaces symbolic pattern names with actual regex strings.
Result
Replace symbolic names with exact regex values from XmXsPatterns; action field now
defaults to UPDATE_MESSAGE if omitted.
Migration Steps
1. action can be omitted when the intended behavior is UPDATE_MESSAGE.
2. Replace any pattern values that used symbolic names (e.g., "XS_ANY_URI_PATTERN") with the actual
regex string stored in the generated Document Model for that type. You can find the exact value
by inspecting the generated JSON model for the affected field’s StringType.pattern. The
following listing shows all built-in patterns defined in XmXsPatterns and the exact regex strings
that appear in the generated Document Model:
Built-in patterns from XmXsPatterns
XS_NC_NAME = ^[_A-Za-z][_A-Za-z0-9.-]*$
XS_G_YEAR_MONTH = -?([0-9]{4,})-(0[1-9]|1[0-2])(Z|[+-][0-9]{2}:[0-9]{2})?
XS_G_YEAR = -?([0-9]{4,})(Z|[+-][0-9]{2}:[0-9]{2})?
XS_G_MONTH_DAY = --(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])(Z|[+-][0-9]{2}:[0
-9]{2})?
XS_G_DAY = ---(0[1-9]|1[0-9]|2[0-9]|3[0-1])(Z|[+-][0-9]{2}:[0-9]{2})?
XS_G_MONTH = --(0[1-9]|1[0-2])(Z|[+-][0-9]{2}:[0-9]{2})?
61

-- 61 of 66 --

XS_LANGUAGE = [a-zA-Z]{2,8}(-[a-zA-Z0-9]{2,8})*
XS_ANY_URI_PATTERN = (?!.*<|.*%(?![0-9a-fA-F]{2})|([^#]*#[^#]*){2,})(?=[a-zA-
Z][0-9a-zA-Z+\-.]*:[^#]|/{1,2}[\s]|([^:/?#]+)?($|/|\?|#)).*
XFLB_ANY_URI_PATTERN = (?!.*<|.*%(?![0-9a-fA-F]{2})|([^#]*#[^#]*){2,})([a-zA-Z][0-
9a-zA-Z+\-.]*://[^\s/.?#$]+|[^\s?#$:]+)\.[^\s]*|[uU][rR][nN]:([a-zA-Z0-9][a-zA-Z0-
9-]{0,31}):([a-zA-Z0-9-()+,.:=@;$_!*'&~/]|\%[0-9a-fA-F]{2})+
XML_SPY = (([a-zA-Z][0-9a-zA-Z+\-.]*:)?/{0,2}[0-9a-zA-Z;/?:@&=+$.\-
_!~*'()%]+)?(#[0-9a-zA-Z;/?:@&=+$.\-_!~*'()%]+)?
 These patterns are defined in the enum
com.mgmtp.a12.transformer.common.XmXsPatterns. Use the exact regex string
(right side of =) in your PatternErrors configuration pattern field.
Example
Old (still valid — defaults to UPDATE_MESSAGE)
{
"PatternErrors": [
{
"pattern": "\\d{14}",
"errors": [
{ "locale": "de", "text": "Bitte geben Sie einen validen Leika-Schlüssel ein."
}
]
}
]
}
New (explicit action is optional, but recommended for readability)
{
"PatternErrors": [
{
"pattern": "\\d{14}",
"action": "UPDATE_MESSAGE",
"errors": [
{ "locale": "de", "text": "Bitte geben Sie einen validen Leika-Schlüssel ein."
}
]
}
]
}
Separation of Internal and Public Api
Many classes that were previously accessible in public packages have been moved to .internal.
packages. Classes in internal packages are not part of the public API and may change without
notice. At the same time, several configuration-related classes that were previously hidden inside
62

-- 62 of 66 --

internal have been promoted to public packages.
Required
Yes, if you use any of the classes listed below that were moved to internal packages or promoted
to public packages.
Migration
Manual. Rewrites import paths, removes usages of internal classes, and updates config class
imports.
Result
Remove direct usage of internal classes; update imports for promoted config classes (e.g.,
xsdtodocm.internal.config → xsdtodocm.config).
Classes moved to internal
transformer-common:
• common.IXmDocumentModelCompareService
• common.IXmDocumentValidationService
• common.XmElementChange
• common.XmRankedNotificationImpl
• common.XmTransformationMessageFormatter
• common.XmXsAttributeAttributes
• common.HashingNamingHelper
• common.ResourceBundleUtil
• common.XmAnnotationHelper
• common.XmRootElementResolver
• common.layer.IModelTransformerLayer
• common.utils.*
• common.document.*
transform-xsd-model:
• xsdtodocm.IXmSimpleTypeMapperService
• xsdtodocm.XmConstants
• xsdtodocm.XmSimpleTypeMapper
• xsdtodocm.service.XmConfigImpl
transform-document-xml:
• doctoxml.groupinformation.GroupInformation
• doctoxml.groupinformation.GroupInformationManager
63

-- 63 of 66 --

xml-schema-validator:
• schemavalidator.ClassLoaderResolver
• schemavalidator.IXvResourceResolver
• schemavalidator.InMemoryResourceResolver
• schemavalidator.Input
• schemavalidator.XmlValidator
transform-xml-document:
• xmltodoc.IXdData
• xmltodoc.IXdXmlReader
xoev-codelist-converter:
• codelistconverter.XmCodeInfoFilter
• codelistconverter.XmConverterImpl
cmd:
• modeltransformer.* except modeltransformer.ModelTransformerApplication
Classes promoted to public (update your imports)
Table 1. transform-xsd-model
Old import New import
xsdtodocm.internal.config.IXmConfigReader xsdtodocm.config.IXmConfigReader
xsdtodocm.internal.config.XmConfiguration xsdtodocm.config.XmConfiguration
xsdtodocm.internal.config.model.* xsdtodocm.config.model.*
xsdtodocm.internal.config.model.serialization.
*
xsdtodocm.config.model.serialization.*
Removed unused/deprecated classes
• xsdtodocm.IXsdToModelConfigKeys
• xsdtodocm.InputValidator
Migration Steps
1. Remove any direct usage of classes listed under moved to internal — these were not part of the
public API.
2. Update imports for the promoted config classes: replace xsdtodocm.internal.config with
xsdtodocm.config.
64

-- 64 of 66 --

Feature Changes
Support for XSD and XML Without Target Namespace
The Document-to-XML transformer now handles XSD schemas that omit targetNamespace and
produces valid XML without namespace declarations in that case.
Previously, attempting to transform a document whose model was generated from a no-namespace
XSD caused:
The value of the attribute * is invalid. Prefixed namespace bindings may not be empty.
The generated XML now omits xsi:schemaLocation and namespace prefix bindings when the XSD
has no targetNamespace, which matches what xsi:noNamespaceSchemaLocation-based XML files expect.
Required
No
Migration
Not needed
Result
No changes required. No-namespace XSD schemas now produce valid XML output automatically.
New _TRANSFORMER_IGNORE_ELEMENT Annotation for Modeler-Added Elements
Modelers can now mark Document Model fields and groups with _TRANSFORMER_IGNORE_ELEMENT to
keep them out of the XML schema flow.
For details and an example, see Excluding Model Elements From the XML.
Required
No
Migration
Not needed
Result
No changes required. New opt-in annotation for excluding model elements from XML output.
XSD Elements Without a Declared Type or With xs:anyType Now Map to a String Field
XSD elements with no explicit type (i.e. implicit xs:anyType, e.g. <xs:element name="foo"/>) and
elements explicitly declared as xs:anyType (e.g. <xs:element name="foo" type="xs:anyType"/>) were
previously skipped or caused issues. They are now mapped to a StringType field annotated with
_XSD_ANY_TYPE in the generated document model.
65

-- 65 of 66 --

Required
No
Migration
Not needed
Result
No changes required. Previously skipped elements now appear as StringType fields annotated
with _XSD_ANY_TYPE.
XML-to-Doc Transformer Accepts IDocumentModel Instances Directly
XdXmlDocumentTransformConfig now accepts IDocumentModel objects directly via documentModels (main
model) and validationDocumentModels (validation model), skipping JSON deserialization when
models are already in memory.
Required
No
Migration
Not needed
Result
This is an add-on. Existing code using modelResources, validationModelResources continues to
work without modification.
66

-- 66 of 66 --

