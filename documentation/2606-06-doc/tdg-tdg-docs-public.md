# tdg tdg docs public

Test Data Generator
NOTE
This documentation belongs to an A12 Enterprise Component which is not part of
the Open Source offering (A12 Community Edition). Please feel free to browse the
documentation and learn more about how you can use this A12 component in your
project. Learn more about the benefits from an A12 Enterprise Subscription on the
Editions & Licensing page.
Introduction
The Test Data Generator (TDG) generates technical test data based on A12 Kernel Document
Models. Moreover, it can be used for checking for Rule Contradictions in Document Models.
For testing the concrete customer application test data are needed. These test data have to be valid
according to the rules defined by the A12 Validation Language. Manually creating test data can be
very tedious and time-consuming, especially if every field of the Document Model should be tested.
This task becomes increasingly infeasible to perform by hand if the Document Model includes
complex relations and dependencies between fields.
TDG automatically creates such test data. The test data generation only requires the Document
Model and no further functional knowledge. The created test data represent no typical user data,
but provide other qualities, such as testing extreme values, using all allowed characters or
generating other interesting states, taken from the rules in the Document Model.
Your options to benefit from the Test Data Generator are:
• Use TDG as a Command Line Tool.
• Include TDG as a Java-Library in other Java projects.
• Include TDG String generator as a Java-Library in other Java projects.
Quick Start
The easiest way to get started is to use the TDG Command Line Tool:
1. Create a Document Model.
2. Download the Test Data Generator Command Line Tool.
3. Run the Test Data Generator.
Technical Requirements
TDG is developed and tested for:
• Windows 10 (64 bit)
• Ubuntu 24.04 LTS (64 bit)
1

-- 1 of 55 --

• MacOS 14.5 (Sonoma)
Additionally, an installed Java JRE is required (minimal Java 21).
Test Data Generator Usage
There are three ways to use the Test Data Generator:
• SME: A rule contradictions check is directly included in the SME.
• Command Line Tool: A Command Line Tool which can run the rule contradictions check or
create test data locally.
• Java-Library: You can include TDG as a Java library into your own Java application.
In the SME
The check for rule contradictions is directly available in the SME.
NOTE The Document Model has to be without errors, to run the Rule Contradictions
Check.
• To run the rule contradictions check, click on "Rule Contradictions" on the left side of the
Document Model Editor:
• Then click on "GENERATE" on the bottom right of the Document Model Editor:
• You’ll then have to insert a TODAY-value. By default, the TODAY-value is pre-set as the current date.
Click "CONTINUE" to start the check:
2

-- 2 of 55 --

This is the date which is used if Today is used in a rule condition. Further information on Today can
be found here. In the SME, only one TODAY-value is used.
Sometimes, a reported rule contradiction can be solved by setting a different TODAY-value that is
valid.
In chapter Report About Rule Contradictions, the report is explained in detail.
As a Command Line Tool
Download and Start
Download
Download the TDG Command Line Tool tdg-cl-8.1.0.jar:
If you are within the mgm network tdg-cl-8.1.0.jar
(mgm internal only)
If you are outside the mgm network tdg-cl-8.1.0.jar
(mgm external only)
It is an executable JAR file build as a Spring Boot Application, which contains all dependencies
(including the SMT-Solver needed).
In case the repository cannot be accessed, please contact Professional Services in order to retrieve a
copy.
How to Run It
The tdg-cl-8.1.0.jar is an executable jar, so you can run it from the Command Line. The relative
path to the Document Model file is the only required argument:
java -jar tdg-cl-8.1.0.jar DocumentModel.json
Alternatively, you can provide a path to a directory instead of a single file. In that case, all json-files
in the directory are interpreted as Document Model files. The test data production will run for each
of these files.
3

-- 3 of 55 --

java -jar tdg-cl-8.1.0.jar <directory name>
NOTE
The command line tool requires a console that supports UTF-8 encoding. If the
console does not support UTF-8, the output may contain garbled characters.
On Windows 11, one way to enable UTF-8 support is:
Settings > Time & Language > Language & region > Administrative language settings >
Change system locale… > Check the box "Beta: Use Unicode UTF-8 for worldwide
language support" and restart the system.
Options
This section shows the available configuration options for the TDG Command Line Tool. You can
also run
java -jar tdg-cl-8.1.0.jar --help
to gain further information about the available options.
For example, in order to run the Command Line Tool with Pretty Values and with a maximum of
two cores, run:
java -jar tdg-cl-8.1.0.jar --cores=2 -vP DocumentModel.json
In case you want to use a custom YICES2 solver executable stored at C:\tdg\mySolver\yices2.exe,
run:
java -jar tdg-cl-8.1.0.jar --cores=2 -vP -sp="C:\tdg\mySolver\yices2.exe" -sn=YICES2
DocumentModel.json
If your Document Model uses one of the German tax custom field types (Steuernummer,
Bundesfinanzamtsnummer, EinheitswertAktenzeichen or StrafAktenzeichen), pass the German Tax
Data file via -x:
java -jar tdg-cl-8.1.0.jar -x="path/to/finanzaemter.json" DocumentModel.json
The Basic Options
4

-- 4 of 55 --

Option Explanation Default
-d, --dest,
--destination=<dstDir>
Destination directory where the destination files
are located.
If no option is given,
the files will be put in
the folder where the
Document Model is
located.
-g, --datagoal,
--
datagoals=<dataGoals>
Produced data goals.
I Minimal Dataset
X Maximal Dataset
R Root Group Datasets
A Datasets With All Field Coverage
H Datasets With High Test Coverage
N Negative Testdata, which intentionally violates
Rules.
V Datasets, ignoring the validation rules.
IXAH
-h, --help Display Help.
-legal, --legal Print legal notices (license, notice, and third-
party notices).
-r, --maxRep=<maxRep> Set the maxRep Parameter. 3
-no-rcr,
--noRuleContradictionR
eport
If set, suppresses the Report About Rule
Contradictions.
-sp, --solver-
path=<pathToExecutable
>
The path to a custom Solver executable (must be
a custom version of Z3, YICES2 or CVC5), must be
used together with corresponding -sn option
-sn, --solver-
name=<solverName>
Name of custom Solver (must be either Z3,
YICES2 or CVC5), must be used together with
corresponding -sp option
-t, --today=<fixToday> Set a fix TODAY (yyyy-MM-dd).
- TODAY will not be a variable.
- The given today-value will be the only today-
value used.
- TODAY is a variable
- 14 Today-values,
starting from now until
now + 366 days.
-td, --
duration=<duration>
Changes the <duration>, i.e. the time in days, the
test data has to be valid. In the result, there will
be up to 14 today-values, starting with now until
now + duration days.
Not allowed in combination with --today
(setting a fixed TODAY).
366
-nt, --noToday Produces the test data, without the need of
setting today.
Today will not be a variable.
Not allowed in combination with --today
(setting a fixed TODAY).
5

-- 5 of 55 --

Option Explanation Default
-v, --verbose Verbose logging.
-version, --version Show TDG version.
-vP, --valuesPretty Fields are filled with Pretty Values. For details,
see chapter Pretty Values.
Cannot be used together with the "-
valuesSample" option.
Every value is possible.
(i.e. fields may be filled
with extreme values
and all possible
characters)
-w, --workspace Defines the workspace folder where the
document model(s) are stored.
Per default, the
workspace is set to the
parent directory of the
given document model
(or the directory given
as a main parameter).
Advanced Options
Option Explanation Default
-c, --cores=<cores> Number of cores to use. This setting is especially
relevant for big data models, which need a lot of
computing power. TDG runs parallel on more
threads, but is limited to the given number of
cores.
Number of cores minus
one
-l,
--lang,
--language
The language used in the result’s statistics
(Supported languages: en, de).
English (US)
-maxNumDs,
--maximalNumberDataSet
s=<value>
Maximal number of datasets that shall be
produced
no fix value (implicitly
determined by data
production)
-minNumDs,
--minimalNumberDataSet
s=<value>
Minimal number of datasets that shall be
produced
no fix value (implicitly
determined by data
production)
-vS, --valuesSample Fields are filled with "sample values".
Cannot be used together with the "-valuesPretty"
option.
Every value is possible.
(i.e. fields may be filled
with extreme values
and all possible
characters)
-uI,
--uniquenessIdentifier
=<identifier>
Sets a fixed string component used in
uniqueness fields across all generated
documents.
Production start
timestamp.
-uS,
--startDocumentId=<id>
Sets the document id of the first generated
document. Subsequent documents receive
incrementing ids.
1
6

-- 6 of 55 --

Option Explanation Default
-x, --germantax=<file> Json-file with the German Tax Data (IFadat).
Recommended if any of the custom field types
Steuernummer, Bundesfinanzamtsnummer,
EinheitswertAktenzeichen (EWAz) or
StrafAktenzeichen is used.
If omitted, these four CFTs fall back to the
neutral dummy implementation, which
produces formally valid but not realistic tax
numbers. See German Tax Data for the file
format.
Output of the Command Line Tool (Example Execution)
• If we want to execute the Command Line Tool (JAR file) on a specific Document Model, we can
have a working directory that includes both files:
└──myWorkingDirectory
├──tdg-cl-8.1.0.jar
└──DomainImpfcharge.json
• We can then execute the following command in the preferred shell:
java -jar tdg-cl-8.1.0.jar DomainImpfcharge.json -nt
• Once the above command finishes its execution, the working directory should look similar to:
└──myWorkingDirectory
├──tdg-cl-8.1.0.jar
├──DomainImpfcharge.json
├──DomainImpfcharge_<date-of-execution>.log
├──DomainImpfcharge_<date-of-execution>.xlsx
├──DomainImpfcharge_<date-of-execution>_ALL_FIELDS_<number>.json
├──...
├──DomainImpfcharge_<date-of-execution>_HIGH_TEST_COVERAGE_<number>.json
├──...
├──DomainImpfcharge_<date-of-execution>_MAXIMAL_DATASET_<number>.json
├──...
├──DomainImpfcharge_<date-of-execution>_MINIMAL_DATASET_<number>.json
├──DomainImpfcharge_<date-of-execution>_rule_contradictions.html
└──DomainImpfcharge_<date-of-execution>_rule_contradictions.json
• Where <date-of-execution> is set to the current date-time on which the data production
command was initiated and <number> identifies different datasets for the same data goal.
• In detail, the files represent:
7

-- 7 of 55 --

File Explanation
DomainImpfcharge_<date-of-execution>.log Log-File
DomainImpfcharge_<date-of-execution>.xlsx Excel-File containing all datasets and statistical
information (only use for manual inspection of
test data or statistics, not standardized, do not
use as input for further data processing). For
details, see Excel File
DomainImpfcharge_<date-of-
execution>_HIGH_TEST_COVERAGE_<number>.j
son
Datasets for data goal "High Test Coverage" (H) -
number of datasets strongly depends on the
Document Model
DomainImpfcharge_<date-of-
execution>_ALL_FIELDS_<number>.json
Datasets for data goal "All Fields" (L)
DomainImpfcharge_<date-of-
execution>_MAXIMAL_DATASET_<number>.json
Datasets for data goal "Maximal Data Set" (X) -
number of datasets mainly depends on settings.
Per default, dataset number 1 refers to strategy
PRIO_NO_WARNINGS_NO_HEUTE_BEFORE_FILL (avoiding
warnings by not filling entities), and dataset
number 2 refers to strategy
PRIO_FILL_BEFORE_NO_WARNINGS_NO_HEUTE
(accepting warning, if entity can be filled)
DomainImpfcharge_<date-of-
execution>_MINIMAL_DATASET_<number>.json
Datasets for data goal "Minimal DataSets" (I) -
number of datasets mainly depends on settings,
per default only one dataset is produced.
DomainImpfcharge_<date-of-
execution>_not_fillable.html
Report about Rule Contradictions (as HTML file
which can be inspected in the browser). A report
is only created, if there are not-fillable entities.
DomainImpfcharge_<date-of-
execution>_not_fillable.json
Same report, but as processable JSON file (A12
Document)
Generally, all generated JSON files represent valid A12 Documents corresponding to the respective
Document Model (in this case "DomainImpfcharge.json"). Therefore, they are processable by the
Kernel API. Only the "<…>_not_fillable.json" file corresponds to a different Document Model (not
publicly available).
NOTE
Because of -nt, TODAY will not be a variable. Therefore, all the created testdata will
have to be valid for at least one year. For the Document Model
"DomainImpfcharge.json" (which is part of the installer samples), setting this option
will lead to three rule contradictions. Therefore, a Report about Rule Contradictions
will be created.
Explanation of the Generated Excel File
The TDG Command Line Tool creates an Excel File, which contains all the created datasets. The data
is exactly the same as in the created JSON Documents. The Excel File also contains sheets with
8

-- 8 of 55 --

related statistical information.
Each Excel File has two sheets that represent the same generated datasets. In both of them, the
groups and fields appear in the same order as they are declared in the Document Model.
The only difference between both sheets is that the one named "Contexts" orders the group fields
by repetition while the "FullName" sheet orders them by their full name, with all the repetitions of
each field being together.
Explanation of Row Headers
Column A - Indicates the type of the field:
Initials Type of the Field
B Boolean Field (YesNo)
C Confirm Field (Yes)
D Date Field
9

-- 9 of 55 --

Initials Type of the Field
E Enumeration Field
N Number Field
NL Number Field (involved in non-linearity)
R DateRange Field
S String Field
U Custom Field (for example IBAN, BIC, or Steuernummer)
NOTE For repeatable groups, column A shows the maximum possible value of repetitions.
Column B - Indicates if the value of a field (not the filling of the field!!) is used in any
validation/calculation rule:
Char(s) Usage of the field value
F Free Field - Its value is not used in any rule (For more information check the
Bound Fields and Free Fields section)
B Bound Field - Its value is used in at least one rule (Bound Fields and Free Fields)
C Calculated Field (Therefore, also a Bound Field)
S+[Number] Bound field of string/enumeration/custom type. These fields are clustered.
[Number] indicates the index of the cluster.
Column D - Full names of the groups/fields (fields in blue font color are index fields):
Explanation of Column Headers
Column-Headers Explanation
documentId The unique documentId.
Today Today-Value which has to be set to make this dataset valid. Only might be filled
if Today is a variable. (For details see Today-Handling)
Warnings Number of warnings which will arise if this dataset is validated.
Restrictions
The Command Line Tool does not support custom plugin implementations. However, the A12 default
custom field types are already included. If your Document Model includes custom plugins, such as
custom field types or custom conditions, consider integrating TDG as a library. The TDG library
provides the option to pass own custom field type and custom condition implementations.
NOTE
If your Document Model includes custom field types or custom conditions which are
not part of the default A12 Tool Set, the CLI mocks those plugins to dummy
implementations. Test data generation may still be possible, however custom field
types and custom conditions may not reflect the desired business logic.
10

-- 10 of 55 --

Not all possible constellations are covered by TDG up to now. Especially, constellations with string
type fields which are combined with string concatenation and regular expressions may result in an
error. A TdgMappingNotImplementedException will be thrown in such cases.
NOTE
If TDG throws a TdgMappingNotImplementedException, please don’t hesitate to contact
the TDG team. If necessary and reasonable, we will implement that case and release
a patch.
As a Library
Within the mgm-network, use the following dependency to include TDG as a library:
• As Maven dependency:
<dependency>
<groupId>com.mgmtp.a12.tdg</groupId>
<artifactId>tdg-lib</artifactId>
<version>8.1.0</version>
</dependency>
• As Gradle dependency:
compile(group: 'com.mgmtp.a12.tdg', name: 'tdg-lib', version: '8.1.0')
The package includes the TestDataGenerator as the entry point. In this example, we focus on both
static methods TestDataGenerator.checkForRuleContradictions(…) and
TestDataGenerator.createTestData(…). The other offered methods of this class reflect a more
general abstraction of the previous two, in order to provide more efficient data production for
more than one data goal. However, they do not provide any extra functionality. TDG can be used in
two distinctive ways:
• Search for rule contradictions in a IDocumentModel (see here for detailed information).
• To create (valid) test data in the form of an IDocumentV2 for a given IDocumentModel.
In correspondence, TestDataGenerator.checkForRuleContradictions(…) is used to perform a check,
which does not create test data, but provides a detailed report about potential inconsistencies in
the Document Model. TestDataGenerator.createTestData(…) is used to create actual datasets which
are valid for the given model.
NOTE
It is only possible to create test data for documents of type IDocumentModel (model
type: documents), since those provide the description of how valid data is structured.
TDG does not handle other model types such as Form Model, Overview Model or other
A12 model types.
NOTE Usage of the example classes that exemplify how to check the model for potential
inconsistencies and how to create test data can be done by including a dependency
11

-- 11 of 55 --

to the tdg-examples module:
<dependency>
<groupId>com.mgmtp.a12.tdg</groupId>
<artifactId>tdg-examples</artifactId>
<version>8.1.0</version>
</dependency>
NOTE
You may also use the tdg-bom in your dependency management in order to align
Kernel and Jackson versions with the desired TDG version. Be aware that the BOM
also manages other third party library versions that may collide with versions
required in your project. However, the aforementioned tdg-lib package is a
sufficient include with minimal dependency management required for TDG.
<dependency>
<groupId>com.mgmtp.a12.tdg</groupId>
<artifactId>tdg-bom</artifactId>
<version>8.1.0</version>
<type>pom</type>
<scope>import</scope>
</dependency>
Checking a Model for Inconsistencies
To check your IDocumentModel for potential inconsistencies call
TestDataGenerator.checkForRuleContradictions(…):
public final class TestDataGenerator {
//...
public static RuleContradictionReport checkForRuleContradictions(
final IDocumentModel documentModel,
final ContentSettings contentSettings,
final TechnicalSettings techSettings)
throws TdgModellingException, TdgInternalException {
// ...
}
// ..
}
The method most importantly requires the IDocumentModel, which shall be checked. Furthermore,
ContentSettings and TechnicalSettings must be passed to provide the necessary configuration. To
obtain a IDocumentModel as well as the two settings instances, the TDG library provides some utility
methods. For example, see the following usage example:
12

-- 12 of 55 --

public class RuleContradictionReportExample {
static void main(final String[] args)
throws TdgModellingException, TdgInternalException {
// Read the DocumentModel
Path pathToDocumentModel = Path.of(args[0]);
IDocumentModel documentModel =
SerializationUtils.readDocumentModelExpanded(pathToDocumentModel);
// Get the settings
ContentSettings contentSettings = new ContentSettingsBuilder().build();
TechnicalSettings techSettings = new TechnicalSettingsBuilder().build();
// Run rule contradiction check, check exceptions
RuleContradictionReport ruleContradictionReport;
try {
ruleContradictionReport = TestDataGenerator.checkForRuleContradictions(
documentModel,
contentSettings,
techSettings
);
} catch (TdgModellingException ex) {
//The error may be connected to specific rules:
if (!ex.getErroneousRules().isEmpty()) {
ex.getErroneousRules().forEach(LOG::error);
}
throw ex;
} catch (TdgInternalException ex) {
// Unexpected error
throw ex;
}
// Write the rule contradiction report as a json-file
Path dstFile =
pathToDocumentModel.getParent().resolve(documentModel.getHeader().getId() +
"_check_model.json");
try (FileWriter fileWriter = new FileWriter(dstFile.toFile(),
StandardCharsets.UTF_8)) {
SerializationUtils.serializeCheckModelReport(ruleContradictionReport.ruleContradiction
Report(),
fileWriter);
} catch (IOException ex) {
throw new RuntimeException(ex);
}
}
}
13

-- 13 of 55 --

In detail, you may use:
• SerializationUtils.readDocumentModelExpanded(Path pathToModel) to create a IDocumentModel
from a file on your disc.
• ContentSettingsBuilder to create a default instance of ContentSettings.
• TechnicalSettingsBuilder to create a default instance of TechnicalSettings.
• SerializationUtils.serializeCheckModelReport(…) to serialize the Report as an A12 Document.
As a start, both settings can be used as provided by default. If needed, use the respective builder to
configure each setting individually. The return value of checkForRuleContradictions(…) is a
RuleContradictionReport, which provides detailed information about rule contradiction or fields
which cannot be filled. For more details on sophisticated exception handling see the Common
Exception section.
It is also possible to get the report as a HTML file.
For that purpose we can use the RuleContradictionsReportToHtml.writeReport(…) method as shown
in the example below:
public class RuleContradictionReportExample {
static void main(final String[] args) throws TdgModellingException,
TdgInternalException {
// (...)
// Write the rule contradiction report as a html-file
RuleContradictionsReportToHtml r =
new
RuleContradictionsReportToHtml(ruleContradictionReport.ruleContradictionReport(),
Locale.ENGLISH, false);
try (FileOutputStream fos = new FileOutputStream(pathToDocumentModel.toString()
.replace(".json", ".html"));
PrintWriter pw = new PrintWriter(fos, true, StandardCharsets.UTF_8)) {
r.writeReport(pw);
pw.flush();
} catch (Exception ex) {
throw new RuntimeException(ex);
}
}
}
NOTE
For the creation of the html-file the tdg-utils module must be integrated.
<dependency>
<groupId>com.mgmtp.a12.tdg</groupId>
<artifactId>tdg-utils</artifactId>
<version>8.1.0</version>
14

-- 14 of 55 --

</dependency>
Creating Test Data
To generate test data, i.e. to generate a set of A12 Documents, that satisfy the rules defined in the
IDocumentModel use the TestDataGenerator.createTestData(…) function:
public final class TestDataGenerator {
//...
public static DataGoalResult createTestData(
final IDocumentModel documentModel,
final DataGoal dataGoal,
final ContentSettings contentSettings,
final TechnicalSettings techSettings)
throws TdgModellingException, TdgInternalException {
// ...
}
// ...
}
The usage is very similar to the check model variant. The only difference is that additionally a
DataGoal is required. All other parameters can be obtained as described in the previous example. In
order to decide which data goal to choose, read the data goals section for detailed information on
each data goal. The chosen data goal strongly influences the generation process and output of the
test data.
NOTE Do not use data goal DataGoal.CUSTOM_1, which only serves as a placeholder for
custom data goal implementations!
The created DataGoalResult most importantly contains a list of ResultDocument. The number of Result
Documents greatly depends on the given data goal and Document Model. The ResultDocument itself
contains the actual test data in form of Kernel’s DocumentV2 interface as well as additional
information such as validation or data goal specific information. For instance, you may use
SerializationUtils.writeDocument(…) to serialize the produced Document, as shown in the
following code excerpt:
public class CreateTestdataExample {
static void main(final String[] args) throws TdgModellingException,
TdgInternalException {
// (...)
// as in previous example
// (...)
15

-- 15 of 55 --

// Produce the test data, check exceptions
DataGoalResult testDataResult;
try {
testDataResult = TestDataGenerator.createTestData(
documentModel,
DataGoal.MAXIMAL_DATASET,
contentSettings,
techSettings
);
} catch (TdgModellingException ex) {
// (...) Do Exception handling
} catch (TdgInternalException ex) {
// (...) Do Exception handling
}
// Write the testdata to files
for (ResultDocument resultDocument : testDataResult.resultDocuments()) {
Path dstPath = pathToDocumentModel.getParent().resolve(
documentModel.getHeader().getId() + "_dataset_" +
resultDocument.getId() + ".json");
try (FileWriter fileWriter = new FileWriter(dstFile.toFile(),
StandardCharsets.UTF_8)) {
SerializationUtils.writeDocument(resultDocument.document(),
fileWriter, documentModel);
} catch (IOException ex) {
// (...) Do Exception handling
}
}
}
}
It is also possible to obtain the test data and the related statistics in the format of an Excel File using
the TdgExcelFileProducer class as demonstrated in the example below:
public class CreateTestdataExample {
static void main(final String[] args) throws TdgModellingException,
TdgInternalException {
// (...)
// Write testdata and statistic to Excel file
final TdgExcelFileProducer excelFileProducer = new
TdgExcelFileProducer(documentModel,
new File(pathToDocumentModel.toString().replace(".json", ".xlsx")),
Locale.ENGLISH, testDataResult);
excelFileProducer.updateAndSaveFile();
16

-- 16 of 55 --

excelFileProducer.close();
}
}
NOTE
For the generation of the Excel file the tdg-utils module must be integrated.
<dependency>
<groupId>com.mgmtp.a12.tdg</groupId>
<artifactId>tdg-utils</artifactId>
<version>8.1.0</version>
</dependency>
NOTE
The Excel file should only be a provider of an easy readable view on data and
statistics, do not use it for further processing. The file’s content structure is subject
to unannounced changes and not standardized.
Configuring German Tax CFTs
If the Document Model uses any of the German tax custom field types (Steuernummer,
Bundesfinanzamtsnummer, EinheitswertAktenzeichen, StrafAktenzeichen), provide an IFadat instance
and register the default CFT implementations from the tdg-cft module via
ContentSettingsBuilder.customFieldTypes(…).
Add tdg-cft to your project:
<dependency>
<groupId>com.mgmtp.a12.tdg</groupId>
<artifactId>tdg-cft</artifactId>
<version>8.1.0</version>
</dependency>
Load the IFadat from a German Tax Data JSON file and register all default CFTs:
import com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat;
import com.mgmtp.a12.tdg.cft.CustomFieldPluginFacade;
// (...)
IFadat fadat;
try (Reader reader = Files.newBufferedReader(Path.of("finanzaemter.json"),
StandardCharsets.UTF_8)) {
// Second parameter: testTaxOfficesAllowed — set to true to also enable
// test tax offices in addition to the entries in the file.
fadat = IFadat.deserialize(reader, false);
}
17

-- 17 of 55 --

ContentSettings contentSettings = new ContentSettingsBuilder()
.customFieldTypes(CustomFieldPluginFacade.defaultCustomFieldPlugins(fadat))
.build();
// (...)
If the Document Model does not use any of the German tax CFTs, use
CustomFieldPluginFacade.defaultCustomFieldPlugins() instead — it returns only the CFTs that do not
require German tax data (BIC, BICInternational, IBAN, IBANInternational, IDNr, ISIN) and no IFadat is
needed.
NOTE
If you omit the German tax CFTs but the Document Model uses them, TDG falls back
to the neutral dummy implementation: values will be formally valid (length,
pattern) but not realistic tax numbers (i.e. check digits are not correct, and they do
not correspond to a real tax office). To report this situation in the rule contradiction
report, use ContentSettingsBuilder.reportUnimplementedCustomFields(true).
See German Tax Data for the JSON file format and the semantics of the testTaxOfficesAllowed flag.
Common Exceptions
The TestDataGenerator API throws two basic types of exception:
• TdgModellingException, indicating an anticipated error, which can (potentially) be fixed by the
user.
• TdgInternalException, indication an unsuspected error or bug.
In case a TdgModellingException is thrown, consider inspecting the concrete exception’s subtype as
well as the presented rule names via TdgModellingException.getErroneousRules() to identify the
issue. Potentially, the error can be fixed right away. Commonly used extensions of the
TdgModellingException, which can be checked on demand are:
• TdgRuleParseException: error while parsing the Rules' error condition text, the condition text
may be malformed.
• TdgInconsistentDocumentModelException: the Document Model is inconsistent, try running
TestDataGenerator.checkForRuleContradictions(…) instead to obtain a detailed analysis.
• TdgUnsupportedConstellationException: The Rules include a known feature or constellation of
features, which is not (yet) supported by TDG. This issue may only be solved by re-writing error
conditions (if possible).
In contrast, a TdgInternalException indicates an unexpected internal error, which most likely
cannot be resolved immediately. In such a case, please consider contacting the TDG team or open a
bug ticket in order to find a solution.
Main Concepts
18

-- 18 of 55 --

Overview Over the TDG Workflow
Basically, TDG takes a DocumentModel and maps it to a satisfiability modulo theories (SMT) problem,
expressed in the SMT-LIB-Language. Then, it repetitively calls SMT-Solvers, which find a solution
for the SMT-problem. That solution is mapped back to a DocumentV2.
Input of TDG
• Document Model
• Settings
• Plugins: (Optional), limited support for Command Line Tool.
Output of TDG
• Test data in form of a DocumentV2 (corresponding to the provided Document Model)
• Report about rule contradictions in the Document Model (if present)
SMT Solvers
The Test Data Generator basically maps the A12 Validation Language to the SMT-LIB language and
uses SMT-Solvers to create valid test data.
By default, TDG uses the Z3 SMT-Solver (version 4.13.3), which is licensed under the MIT License.
But, TDG can also be configured to run with Yices 2 (stable, version 2.6.5) or CVC5 (experimental,
version 1.2.0) as Solvers. However, those solvers have to be obtained manually.
In contrast to previous TDG versions (i.e., versions 3.X and lower), the user is no longer required to
provide a "SMT-Solver" directory. If the use of a different SMT-Solver is desired, see the respective
19

-- 19 of 55 --

usage chapter for more details:
• For the Command Line Tool, see here.
• For usage as a library, see here.
Repetitions, MaxRep and MinRep
Groups can be repeatable in A12. They have the Repeatability property, which is the maximum
amount of instances that are allowed to be created for the group. The Repeatability can have fairly
large values.
TDG creates one or more variables in SMT-Lib for every repetition of a group and every repetition
of a field. The SMT-Solvers then decide, which entity is filled in which repetition, and which is not.
For large values of the repeatability of a group, the resulting SMT-Problem would grow
disproportionately, if TDG would map every allowed repetition. Too big SMT-Problems result in big
memory usage and long computing times, which can easily exceed the limits of a normal computer.
Therefore, TDG uses a setting called maxRep, which defines the maximal repetitions of a group that
TDG maps to the SMT-Problem. For higher repetitions, no variables are created. They are treated as
being not filled.
Sometimes, there are groups which have to be filled for a minimum number of times, which is
guarded with a rule respectively.
In this example, group /A always has to be filled at least five times:
NumberOfFilledGroups(/A*) < 5
For this case, TDG uses the annotation A12_TDG_MINREP, where the value has to be an integer with the
minimum of repetitions needed. The value of A12_TDG_MINREP overwrites maxRep for this specific
group.
NOTE
As maxRep is a variable setting for each data production, we recommend to always
use the A12_TDG_MINREP annotation, if a group has to be repeated more than once to
receive valid data.
Overview over settings for repetition:
Property Setting for Defined in
Repeatability specific group Document Model (group property)
A12_TDG_MINREP specific group Document Model (as annotation)
maxRep entire production TDG settings
The actual number of Repetitions for a group is calculated like this:
20

-- 20 of 55 --

Minimum(Repeatability, Maximum(A12_TDG_MINREP, maxRep))
Examples:
Group Repeatability MaxRe
p
Value
of
A12_TDG
_MINREP
Repetitions with
variables in the SMT-
Problem
/A 10 5 - 5
/B 2 5 - 2
/C 10 5 7 7
/D 2 5 7 2
The standard value for maxRep in A12 is three. The maxRep value can be set in the TDG settings. For
smaller Document Models, a higher maxRep may be feasible. Depending on the Document Model, the
need of memory and computing power can increase dramatically, if maxRep is increased.
Bound Fields and Free Fields
TDG divides the fields of the Document Model in bound fields and free fields:
Bound fields are fields whose value is referenced in one of the rules of the Document Model.
For Example:
[/A/field1] > 5
The field /A/field1 is a bound field.
Free fields are fields whose value is never referenced in the rules of the Document Model. As long
as on the filling of a field is referenced, a field remains a free field.
For Example:
FieldIsFilled(/A/field2) And FieldIsFilled(/A/field3)
The fields /A/field2 and /A/field3 are still regarded as free fields (as long as their value is not
referenced in another rule).
Both types of fields are treated differently: While bound fields have a variable for filling and one or
more variables for the value in the SMT-Lib mapping, free fields only have a variable for filling.
Note: Groups never have a value. Therefore, every group has one variable for filling in the SMT-Lib
mapping.
21

-- 21 of 55 --

Data Goals
Introduction
Different test applications have different requirements for the used test data.
For Example:
• Smoke tests should avoid long runtimes. Therefore, less filled datasets are desired.
• Regression tests before releases should test every detail of the application. Therefore, datasets
with high test coverage are needed.
Consequently, TDG provides different data goals in order to define the desired type of data set
which should be created.
TODAY-Handling and Rules of SeverityType WARNING
Document Models can contain Validation Rules which contain the TODAY-language construct. In
some cases it is not possible to fill all fields without a specific today-value.
The Validation Rules in the Document Model can be of SeverityType ERROR or WARNING. Rules with
SeverityType ERROR are never violated in the resulting datasets (except for the data goal NEGATIVE
which intentionally violates at least one rule). In some Document Models, there are fields which can
only be filled while violating rules of SeverityType WARNING though.
As a result, often there is a tradeoff between filling of fields and the benefit of having no fixed
today-value or no violation of rules of SeverityType WARNING. This trade-off is treated differently for
each data goal.
Non-Linearity
Finally, TDG has to deal with the problem of non-linearity: Non-linearities are multiplications of
more than one field or divisions in which there is at least one field in the divisor.
If non-linearities occur, the SMT-Solvers have to use totally different algorithms which are
significantly slower compared to their linear counterparts. Moreover, the Solvers often do not
provide an answer for non-linear problems (Non-linear integer problems are not decidable).
Because of this, TDG eliminates any non-linearity by a complex pre-definition algorithm. The
algorithm works with heuristics. Therefore, it’s always possible, that the resulting datasets are not
perfect with respect to the test requirements regarding fields, which are used in non-linearities.
22

-- 22 of 55 --

Minimal Dataset
The minimal dataset is a dataset which is minimally filled.
Typical Use Case Minimal datasets are used for smoke tests or manual tests, to get a valid
dataset with as little filling as possible.
Numbers of
Created Datasets
1
Commandline
Capital
I
Today/Warnings Minimal datasets try to avoid the need of today or warnings. Higher filling is
accepted in exchange.
Order The entities are set to "not filled" invers to their order in the Document Model.
Therefore, if only one of several entities has to be filled, only the first one is
filled.
Repetitions The minimal number of repetitions needed.
Maximal Dataset
The maximal dataset is a dataset which is maximally filled.
Typical Use Case Maximal datasets are used for manual tests, to get a well-filled dataset.
Numbers of
Created Datasets
1 or 2
Commandline
Capital
X
Today/Warnings If there are today-critical rules or warnings, TDG produces two maximal
datasets:
PRIO_NO_WARNINGS_NO_HEUTE_BEFORE_FILL: TDG tries to avoid the need of a today
-value and warnings. Less filling is accepted in exchange.
PRIO_FILL_BEFORE_NO_WARNINGS_NO_HEUTE: TDG tries to fill the dataset maximally.
The need of a today-value and warnings are accepted in exchange.
23

-- 23 of 55 --

Order The entities are filled according to their order in the Document Model.
Therefore, if only one of several entities can be filled, only the first one is
filled.
Repetitions Maximal umber of allowed repetitions (up to maxRep).
Root Group Datasets
Definition: The root groups are the groups on the lowest possible level, with more than one element
on that level.
Example 1: The Document Model has the following structure (where A, AA, AB, B, BA and C are
groups, field* are fields):
├──/A
│ ├──/A/AA
│ │ ├──/A/AA/field1
│ │ └──/A/AA/field2
│ └──/A/AB
│ ├──/A/AB/field3
│ └──/A/AB/field4
├──/B
│ ├──/B/BA
│ │ └──/B/BA/field5
│ └──/B/field6
└──/C
└──/C/field7
The root groups are /A, /B and /C.
Example 2: The Document Model has the following structure (where A, AA, AAA, AAAA and AAAB
are groups, field* are fields):
└──/A
└──/A/AA
└──/A/AA/AAA
├──/A/AA/AAA/AAAA
│ ├──/A/AA/AAA/AAAA/field1
│ └──/A/AA/AAA/AAAA/field2
└──/A/AA/AAA/AAAB
├──/A/AA/AAA/AAAA/field3
└──/A/AA/AAA/AAAA/field4
The root groups are /A/AA/AAA/AAAA and /A/AA/AAA/AAAB.
Root group datasets fill one root group maximal, the others minimal.
24

-- 24 of 55 --

Typical Use Case Root group datasets are used for automated tests or manual tests, if special
parts of the data model are tested.
Numbers of
Created Datasets
TDG produces as many datasets per root group as needed for every entity to be
filled at least once. This can result in one or more datasets per root group.
Commandline
Capital
R
Today/Warnings The first dataset for a root group tries to avoid the need of a today-value or
warnings.
The other datasets for a root group accept the need of a today-value or
warnings.
Repetitions Root group, dataset is created for: Maximal possible up to maxRep.
Others: Minimal needed.
All Fields Datasets
All fields datasets create a set of datasets where every entity is filled at least once, if possible.
Typical Use Case All fields datasets are used for automated tests or manual tests, if every field
should be entered at least once and no repetitions are tested. Therefore, this
data goal produces fewer datasets with a lower degree of filling compared to
datasets of high coverage.
For example, these datasets are used by an automated ZAP-test to find
vulnerabilities of a portal.
Numbers of
Created Datasets
As many as needed to have each entity filled at least once.
Commandline
Capital
A
Today/Warnings TDG starts with creating datasets without a today-value and warnings.
If there are entities which could never be filled that way, TDG creates datasets,
which accept a today-value and warnings and fills these entities there.
Repetitions The entities only have to be filled in one repetition. Therefore, normally just
the first repetition is filled. In rare case it’s necessary to fill higher repetitions
in order to fill entities at all.
High Test Coverage Datasets
Introduction
These datasets contain a lot of interesting properties, such as extreme and special values or logical
branches. We call these properties states. If a state can be reached, we call it reachable, otherwise,
we call it unreachable. A state is unreachable, if it’s in conflict with the properties of the fields or
with one of the rules.
Datasets with High Test Coverage are datasets which contain all reachable defined states.
25

-- 25 of 55 --

Overview Over the Defined States
Type Defined states
States regarding filling
of groups and fields
- Entity is filled.
- If repeatable, every number of repetitions up to maxRep.
- Entity is not filled, while its parent is.
States regarding values
of fields
Numbers:
Minimum, Maximum, smallest positive number, biggest negative
number, Zero
Dates:
29th Feb of actual year. In Addition, the filling is very sophisticated for
dates.
Enums:
At least two different values.
Strings (Bound fields):
One value from the known string constants. Another which is not from
the known string constants.
Strings (Free fields):
Minimal length (no spaces), maximal length (no spaces), maximal length
(with spaces), length between min and max (no spaces), length between
min an max (with spaces)
States from logical
branches
If there are different options, to not violate a rule, each of this logical
branches will be used in at least one repetition in at least one dataset.
States from plugins Plugins can define additional states.
Properties of Datasets With High Test Coverage
These datasets contain the Highest Test Coverage TDG offers. Normally, this data goal contains the
highest number of datasets, the single datasets are huge and consequently take the largest
computation time for their creation.
Every defined state will either be considered unreachable or it will be reached in one of the
resulting datasets. TDG tries to put as many states in each dataset as possible. Afterwards the
datasets are filled up by sophisticated filling-values.
P-Constraints
This data goal also provides a very sophisticated handling of non-linearities: States, which are
connected to a non-linearity in any way are called p-constraints. Unfortunately it is not possible to
guarantee that every p-constraint is satisfiable. However, based on sophisticated heuristics, TDG
tries to satisfy as many p-constraints as possible.
Summary
Typical Use Case High test coverage datasets are used for automated tests to achieve the
highest possible test coverage, e.g. to test an application before a release.
26

-- 26 of 55 --

Numbers of
Created Datasets
As many as needed in order to capture every reachable state.
Commandline
Capital
H
Today/Warnings There are two kinds of datasets:
Datasets with no today-value and warnings: All states, which can be placed
here, are placed here.
Dataset, which accept a today-value and warnings: The other states are placed
here.
Repetitions Maximal number of allowed repetitions (up to maxRep).
Negative Datasets
Negative datasets violate one validation rule while being filled as sparsely as possible.
The rule might be violated in any iteration up to maxRep. Rules with non-linearities are ignored here.
If it’s not possible to only violate a rule, no dataset is created for that rule.
Typical Use Case Negative datasets are used for automated tests or manual tests, to test, if
validation errors and warnings are handled in the intended way.
Numbers of
Created Datasets
For each rule of the data model a dataset is created, which only violates that
rule. If that is not possible for a rule, or the rule contains a non-linearity, it’s
ignored.
Commandline
Capital
N
Today/Warnings Negative datasets try to avoid the need of a today-value or warnings, if that is
possible while the intended rule is violated.
Order The entities are set to "not filled" inverse to their order in the Document
Model. Therefore, if only one of several entities has to be filled, only the first
one is filled.
Repetitions The minimal repetition needed.
Datasets Ignoring the Rules
Datasets which ignore the rules just fill up all the fields with values. The datasets are valid regarding
the formal attributes of the fields. They are normally violating a lot of the Validation Rules though.
Typical Use Case This datasets are used to check print layouts. In these scenarios all fields
should be filled and no validation is performed.
Numbers of
Created Datasets
Normally just one dataset is produced. This can be changed by setting the
minimal number of datasets.
Commandline
Capital
V
27

-- 27 of 55 --

Today/Warnings Irrelevant, as rules are ignored.
Order Irrelevant, as rules are ignored.
Repetitions maxRep
Today-Handling
Introduction
The A12 Kernel Validation Language has the Today language construct, which returns the day on
which the data is validated. There is also the Now language construct, which returns the point in
time at which the data is validated. In this chapter, we only refer to Today, but everything said also
applies to Now accordingly.
As the test data generation can take some time, the test data is normally created prior to the
execution of the test. Therefore, at the time of the test data generation, the value of Today is
unknown.
Handling Today turned out to be extremely complicated in concrete projects, as the Validation Rules
can practically define anything. The Today-handling of TDG is the result of experience from several
concrete projects. The TODAY concept does not cover everything that is expressible by A12
Validation Rules, however it covers all the cases that occurred in praxis.
Typical use cases for Today are:
# Use Case Example
1 The data starts to be valid
at a given point of time in
the future.
A form starts to be usable at the following calendar
year, but has to be tested before.
2 Lower limit for a datefield
depends on TODAY.
A Person has to be younger than a special age. The
birthdate is entered in a field. As TDG tries to produce
edge cases, it’s probable, that a dataset produced,
where the person is exactly that age minus one day.
Therefore, the data will be invalid at the next day.
28

-- 28 of 55 --

# Use Case Example
3 Automatic calculation of a
field, depending on TODAY
This rule would make the test data valid for only one
day:
[/A/dateField] != Today
This leads to the case in which test data created with a
TODAY=01.01.2021 is only valid at this date. When the
same test data is reused on the next day (i.e. 02.01.2021)
it already became invalid.
4 Filling of fields depends on
the month of TODAY
Fields can only be filled during a specific time of the
year.
5 Possible values depend on
TODAY
A field contains a tax rate. Only defined tax rates can be
entered. Some rates have a validity date, for example
the tax on sales was reduced in Germany from 1st of
July until 31st of December 2020.
Definitions and Assumptions
• The test for which the test data is created, might set a today-value, which simulates that the test
is performed on that day.
• It’s preferable not to have the need to set that today-value, if possible.
• It’s preferable to generate test data, which are valid as long as possible, beginning with the day
of the test data generation.
• If it’s required to have a today-value set to have some fields filled or to reach special cases in the
High Test Coverage, it’s preferable to generate some testcases without a today-value and some
with a today-value. In the latter case, the problematic fields are filled or the problematic states
are reached.
• Time passes only in direction towards the future. Therefore, no test data are produced for the
past.
NOTE
We opened the interface to use LocalDateTime instead of LocalDate as "Today" values
in order to allow correct representation of the "Now" language construct in the
future. However, this is not yet fully supported and there are known issues in
certain constellations with "Today"-values at a time not equal to midnight. We
highly recomment to use dates dd-MM-YYYY’T'00:00:00 as today values when there is
a need to specify them manually. For example, see LocalDate#startOfDay().
Today-Handling in the Command Line Tool
You basically have three options regarding Today in the Command Line Tool:
1. You allow the creation of datasets with today-value (standard). If you realize, that all or too
many datasets have a today-value, you can try to reduce the duration with the --duration
-parameter. This might reduce the datasets with today-value.
2. You forbid the creation of datasets with today-value by setting --noToday. Use this setting if the
29

-- 29 of 55 --

rules allow the data to be valid from the production day on for a period of time or if your test-
tool does not support the setting of a today-value. If the test data generation is not possible
without a today-value (Csp is UNSAT, report about rule contradictions), you should reduce the
duration with the --duration-parameter. This might make the test data generation possible
again. Be aware, that there are cases (like use case 1) where you always need a today-value.
3. You set a fixed today-value with the --today-parameter. TDG then only guarantees that the
produced test data is valid on that day.
Today-Values in the Command Line Tool
It’s not possible to create test data which are valid for a period of time. Only points of time can be
validated. Therefore, the Command Line Tool will create a set of dates, starting with day of
production, adding CEIL(duration / 13) days, until duration is reached.
The created data will be valid on those dates. It’s assumed that the data are also valid between
those dates too. If that is not the case the responsible rules are called weird.
Properties of the Produced Data:
• If a resulting dataset has no today-value, it will be valid from the production day on for duration
days (assuming that there are no weird Today-rules).
• If a resulting dataset has a today-value, it will be valid on that day.
Dive Deeper: What TDG Does
The Test Data Generator takes two settings for handling Today:
Setting Type ContentSetting
s
Command Line Tool
Today is a
variable
boolean todayIsVariabl
e()
Standard: true
--noToday, --today: false
Today-Values Set<LocalDateT
ime>
todayValuesRul
es()
Standard: 14 Today-values, starting from now
until now + 366 days.
--duration: Up to 14 Today-Values, starting from
now until now + duration days
The main setting is todayIsVariable(), which will lead to two different modes:
Today-Mode 1: Today Is NOT a Variable
This mode is suitable if the rules allow the test data to be valid for a given period starting from the
day of production.
TDG will copy all rules containing Today for every value in the Today-Values and add the resulting
constraints to the Csp. The result is that the produced test data are forced to be valid for all given
Today-Values.
Example:
30

-- 30 of 55 --

The test data are produced by the Command Line Tool on January 1st 2021 with the --noToday
-option. TDG will copy all rules with Today for the following Today-Values:
2021-01-01 2022-07-23
2021-01-30 2021-08-21
2021-02-28 2021-09-19
2021-03-29 2021-10-18
2021-04-27 2021-11-16
2021-05-26 2021-12-15
2021-06-24 2022-01-02
The Command Line Tool creates 14 Today-Values, starting at the day of production and adding
CEIL(366 / 13) = 29 days until the day of production plus 366 days is reached. By dividing by 13
every month in that year is hit and the day of the month also changes. The purpose of that
procedure is to hit as many thinkable cases as possible.
Let’s apply this to Use Case 2: A person must be younger than 18 years old. TDG ensures that they
are also younger than 18 on Jan 2nd 2022; therefore they have to be born on Jan 3rd 2004 or later.
The resulting datasets will have the following qualities:
• No dataset will have a today-value set.
• The resulting test data will be valid for all the 14 Today-Values. It’s assumed that it’s also valid at
every day during this year.
Today-Mode 2: Today Is a Variable
This mode is suitable for models with more complicated today-rules, for example use cases 3, 4 and
5. TDG will try to avoid the today-value, if it’s possible:
TDG will translate Today as a variable to the Csp and will also create so called Nice-To-Have-
Constraints: The first Nice-To-Have-Constraint is always setting the Today-variable to the day of
production. The others are copies of the Today-rules for every Today-Value given in the settings.
TDG will try to put the Nice-To-Have-Constraints into the Csp, if possible. Depending on the data
goal, this might happen before or after the other constraints.
The resulting datasets will have the following qualities:
• Test Data Generation is always possible. If it’s not possible to create a dataset without a today-
value, all resulting datasets will have one.
• The use of a today-value is avoided though, if possible. If all desired states can be reached
without a today-value, none of the resulting dataset will have one. If it’s possible to create test
data without a today-value, but some desired states require a today-value, some of the resulting
datasets will have a today-value set, some won’t.
• If a resulting dataset has no today-value set, it will be valid for all of the Today-Values, given in
31

-- 31 of 55 --

the settings.
• If a resulting dataset has a today-value set, it will be valid on that day.
Uniqueness
Basic Behaviour of TDG
Kernel’s document model allows defining uniqueness criteria, each of which is a set of fields. The
values of these fields together must be unique for each data set.
TDG defines the documentId and the uniquenessIdentifier to ensure document uniqueness:
• The documentId is a sequential number assigned to each produced document. The documentId is
also used in log messages and is put in the header of each document of the Excel File.
• The uniquenessIdentifier is a string embedded in string fields to distinguish productions; by
default it equals the start date and time of the production run.
TDG puts the following values in fields that are part of a uniqueness criterion:
Field Type Value used
Number The documentId.
String (no number field in
uniqueness criterion)
documentId-uniquenessIdentifier
By default this has the form n-yyyy-MM-dd’T’HH-mm-ss, where n is
the documentId and yyyy-MM-dd’T’HH-mm-ss is the start of the
production.
String (with number field in
uniqueness criterion)
uniquenessIdentifier
By default this is the start of the production in the form yyyy-MM-
dd’T’HH-mm-ss.
Enumeration Permuting all allowed combinations of values
Date Start time of production + documentId in lowest allowed time
unit
Boolean, Confirm Not used by TDG
Restrictions
TDG is only able to create unique test data if the following restrictions apply to the fields that are
part of a uniqueness criterion:
• Number fields must not be restricted with maximum or minimum values such that TDG cannot
set the documentId.
• String fields must not be restricted with a pattern, a maximum or minimum length such that
TDG cannot set the documentId or the uniquenessIdentifier as described above.
32

-- 32 of 55 --

• Field values must not be restricted by rules in a way that prevents TDG from setting the
described values.
• Values of fields that are part of a uniqueness criterion must not be used in nonlinear rule parts.
Uniqueness Across Different Data Productions
If a string field or a date field with time is part of the uniqueness criterion, TDG embeds the
production start time in the corresponding field, making that production unique compared to other
productions with a different start time. If this is not the case (example: only a single number field in
the uniqueness criterion), TDG cannot guarantee uniqueness across productions, as it has no
knowledge of other runs. In that case the user must ensure uniqueness manually, by setting the
startDocumentId in ContentSettings. It is also possible to set the uniquenessIdentifier in
ContentSettings, which is then embedded in string fields instead of the production start date and
time.
When using the Command Line Tool, both settings are available via the -uI (--
uniquenessIdentifier) and -uS (--startDocumentId) options.
Report About Rule Contradictions
Introduction
The Rule Contradictions Report
A Document Model contains rule contradictions if the model is in conflict per se, or if a group or a
field can never be filled without causing a validation error. We call these groups or fields
"Unfillable Entities". Assuming that a document does not intentionally include unfillable entities,
this provides an indication for erroneous rule conditions. It might also be an indicator for the need
of a different TODAY-setting.
TDG cannot resolve the contradictions leading to an unfillable entity on its own, as this may require
the knowledge of a business expert. However, the "Rule Contradiction Report" provides as much
assistance as possible in order to solve the problem.
33

-- 33 of 55 --

In the SME, the rule contradictions check is directly included as a feature. Both the Command Line
Tool and the tdg-library also provide the report as a html-file and in a json-format, respectively.
NOTE The screenshots in this chapter are taken from SME. The output of the Command
Line Tool has a different UI, but the content is the same.
Components of the Contradiction
A rule contradiction is found if an unfillable entity is found or if the Document Model is in conflict
per se. An entity is unfillable if the assumption that the entity is filled leads to a rule contradiction.
The contradiction consists of contradiction components. There are four types of contradiction
components:
• The assumption that an entity is filled.
• Rules from the Document Model.
• Calcfillings: Preconditions of calculations, which implicitly decide, if a calculated field is filled or
not.
• Edge conditions: Implicit properties of the A12 Kernel Rule Language.
Example of a Rule Contradiction Report:
34

-- 34 of 55 --

Explanation:
# Caption Further Explanation
1 The full name of the entity. The Unfillable Entity.
2 Precondition which leads to the conflict. The precondition, that the entity is filled.
3 Rules involved in the conflict. The validation- and calculation rules from
the Document Model.
4 Filling of automatically calculated fields
involved in the conflict.
Preconditions of calculations. They decide
about the filling of the calculated field. See
Conflict/ConflictCalcFillings.
5 Edge conditions involved in the conflict. Implicit properties of the A12 rule language.
See Conflict/Edge conditions.
6 Subsequent unfillable fields and groups. Entities, which are unfillable subsequently
to the reported Unfillable Entity. See
SubsequentUnfillableEntities.
35

-- 35 of 55 --

Miscellaneous
Allow Fields to be Unfillable
Sometimes there are fields and/or groups which are allowed to be unfillable. In that case, we do not
want to have those fields/groups checked and reported by the rule contradictions report, so we can
mark them with the annotation A12_TDG_UNFILLABLE_OK. The value of the annotation is evaluated in
the following way:
Value of
A12_TDG_UNFILLABLE_OK
Explanation Example
The value is empty The annotation always
applies
"" or null: Annotation always applies.
Comma-separated list of
variants
Annotation only applies for
given variants.
"var1,var2": Annotation applies for
variants "var1" and "var2".
NOT comma-separated list
of variants
Annotation applies for all
variants except the given
ones.
"NOT var1, var2": Annotation applies for
all variants except "var1" and "var2".
Fields or groups which have the annotation A12_TDG_UNFILLABLE_OK are never checked or reported by
the rule contradiction report.
Reported Contradictions
For every unfillable entity, one contradiction is reported:
• There might be other contradictions caused by the assumption that an entity is filled.
• TDG will only report one contradiction.
NOTE
We recommend to solve contradictions incrementally: If TDG reports more than one
Rule Contradictions, we recommend to solve one conflict at a time. Recreate the rule
contradictions report afterward. Repeat this until all problems have been resolved.
The reported conflict is minimal:
• The reported conflict is a minimal conflict in the sense, that all the reported Contradiction
Components are involved in the conflict.
• In other words, all Contradiction Components are necessary in order to produce the
contradiction. None of the reported contradiction components can be removed without
removing the contradiction.
Repetitions
Normally, if an entity is unfillable, all of its repetitions are unfillable. Only in rare cases, single
repetitions are unfillable. Therefore, only the first repetition of Unfillable Entities is reported.
36

-- 36 of 55 --

Subsequent Unfillables
If an entity /A/B is unfillable, and an entity /A/C can only be filled if /A/B is also filled, /A/C is listed
in section "Subsequent Unfillable" of /A/B and is not reported separately.
Emphasized Rule Parts
Parts of rule conditions which are relevant for the contradiction are emphasized. Not emphasized
parts of the rule conditions have no influence on the contradiction.
Details About Conflict Components
Calcfillings
The preconditions of the calculations decide if that field is filled or not:
• A calculated field is filled if one of the preconditions for its calculation is fulfilled.
• If none of the preconditions for its calculation is fulfilled, it will not be filled.
NOTE
Please keep in mind that the preconditions of the calculations decide about the
filling of the calculated fields. This fact is easily overlooked. A lot of conflicts, which
occurred in real live applications were caused by ignoring this connection.
Calculation Rules can occur in the Rules section as well as in the Calculation Fillings section:
• They will be listed in the Rules section if the calculation itself is part of the contradiction.
• They will be listed in the Calculation Fillings section if the filling of the calculated field, decided
by the preconditions, is part of the contradiction.
Edge Conditions
There are several types of edge conditions:
Type Explanation
Filling of groups caused
by filling of child.
A group (parent entity) is automatically filled, if one of its child entities is
filled. This connection is part of the conflict.
Allowed values for a
field
A formally invalid value for a field is enforced. This normally refers to
field properties, such as minimal or maximal value, allowed values for
enumerations etc.
String specialties Special behaviour of strings: This normally refers to string patterns,
which cannot match each other.
Mandatory fields and
groups
A field or a group was marked mandatory.
Plugins A rule or attribute, injected by a plugin, is involved in the conflict.
37

-- 37 of 55 --

When solving the conflict, the edge conditions should always be considered. As all reported
Contradiction Components are relevant, each of the reported edge conditions is relevant for the
contradiction.
How to Solve the Contradiction
Basic Procedure
Approach I: Check Reported Contradiction Rules and Calculation Fillings
The first approach for the solution of the contradiction is to simply check the reported rules. It’s
very likely that there is an unintended error in the logic or phrasing of the rules, which causes the
contradiction.
• Check the preconditions of the calculations in the Calculation Fillings section. Keep in mind that
these preconditions decide if the calculated field is filled or not.
• Please have in mind that A12 Kernel Rules define the error situation. This is easily overlooked.
• Check if the and and or are correct. Check if braces are missing.
If you found an error, correct it and recreate the rule contradictions report. If the conflict is solved,
you’re done.
If you do not find the error by this approach, you’ll have to go on to the second approach.
Approach II: Understanding the Contradiction
The second approach is to understand the contradiction. This can be very difficult, especially if
more and complicated rules are involved.
• We recommend to start with the precondition which enforces the Unfillable Entity to be filled.
It’s helpful to think "Suppose group/field … is filled".
In a contradiction component, the groups and fields which are relevant for the contradiction are
emphasized. The single contradiction components are being connected by relevant fields and
groups they have in common.
• After you evaluated a contradiction component (i.e. the precondition at the beginning), search
for the contradiction components which have common emphasized groups or fields.
◦ Keep going through all contradiction components, guided by the emphasized groups or
fields. From there, you can try to understand how the contradiction components evaluate,
given the previously mentioned assumption that the Unfillable Entity "is filled".
• At a certain point, you will find a contradiction.
NOTE Example 1 provides a deeper explanation and visualisation of how to try to
understand a contradiction.
38

-- 38 of 55 --

Example I
Let’s go through the SME rule contradictions report already mentioned:
In our example, the field /A/fieldSum is unfillable. The best way to understand the conflict is to start
with the precondition "Suppose, /A/fieldSum is filled".
Step A
• You start with the precondition "Suppose, /A/fieldSum is filled."
• Then, you look for another rule where /A/fieldSum is emphasized or for the automatic
calculation of that field. In our case, the field is automatically calculated:
39

-- 39 of 55 --

As the calculation is not shown in the Rules section, only the filling of the calculated field is
involved in the conflict and not the calculation itself.
As the precondition of the calculation decides about the filling of /A/fieldSum, and the precondition
is
FieldFilled(B/fieldB) and FieldFilled(C/fieldC)
we know that /A/B/FieldB and /A/C/FieldC both have to be filled.
Step B
• In the calculation rule /A/addBAndC, FieldFilled(C/fieldC) is emphasized. The other parts of the
rule are not involved in the contradiction. Therefore, keep looking for other rules in which
C/fieldC is referenced. We see that C/fieldC is also referenced in
/A/fieldMandatoryAndFieldsCDNotTogether:
• The rule basically says that /A/B/fieldMandatory and /A/C/fieldC cannot be filled together. As
/A/C/fieldC has to be filled (Step A), we know that /A/B/fieldMandatory has to be not filled.
Note: /A/D/fieldD is not relevant for the conflict. Therefore, it’s not emphasized in the rule
condition text.
Step C
As /A/B/fieldMandatory is a required field, there are edge conditions, basically saying, that if
/A/B/fieldMandatory is not filled, all of it’s parent-groups cannot be filled.
This is a contradiction to /A/fieldSum being filled.
40

-- 40 of 55 --

Solution
The solution of this conflict is a functional decision, which has to be made by the user in accordance
to the correct business logic. Technically, the solution could be to change any of the conflict
components:
• Change the preconditions of the calculation
• Allow /A/C/fieldC and /A/B/fieldMandatory to be filled together.
• Make /A/B/fieldMandatory not mandatory.
Example II: TODAY
In our example, we have a field1, which cannot be filled after the year 2021. At the same time, we
have a rule /A/field1NotFilled, enforcing the filling of field1.
We start the report with the TODAY value 20/10/2022:
No Valid Document at All
There is no valid dataset for this Document Model with these Today-settings at all. Therefore, the
report contains no Unfillable Entity. Instead, you see the message "The Document Model is
completely in conflict!" (A).
This may happen independently of Today-issues.
Today-Values Involved in Conflicts
If Today is involved in one of the conflicts, and it’s emphasized, consider to rerun the report with
another TODAY value (B).
You can see the today-value used by opening the top section of the report ©. In the SME a TODAY
-value is always used.
41

-- 41 of 55 --

Solution
The solution is a functional decision:
• If the report is also valid in 2022, maybe somebody forgot to adjust the year in rule
/A/field1After2021 to 2022.
• If the report is not valid in 2022, restart the report with a TODAY-value from 2021 or before.
Details About the Rule Contradictions json-file
The rule contradictions json-file is created by the Command Line Tool.
Text With Semantic Information
Some texts in the report are delivered with semantic and formatting information. This information
consists of the following fields:
Field Explanation
text The blank text.
markdownText The text with Markdown-markups.
SemanticInfo Semantic information about the text.
SemanticInfo/beginInfo The index in text, where the semantic
information starts (inclusive).
SemanticInfo/endInfo The index in text, where the semantic
information ends (exclusive).
SemanticInfo/semanticType The type of the semantic information.
Semantic types:
semanticType Explanation Formatting in markdown
RULE_PART_RELEVANT Relevant part in the rule
condition.
bold
FULLNAME_GROUP A fullname of a group. monospaced, bold
FULLNAME_FIELD A fullname of a field. monospaced, bold
TEXT_EMPHASIZED Emphasized part of the text. bold
TEXT_TECHNICAL A technical string. monospaced
Relevant Rule Parts
The report contains the rule conditions enriched with the information of which parts of the rule
condition are relevant for the conflict.
• Parts of the rule conditions which are relevant for the conflict are marked as
RULE_PART_RELEVANT. If one of them is left out, the conflict disappears.
42

-- 42 of 55 --

• Parts of the rule conditions which are not relevant for the conflict are not marked. If those are
left out, the conflict does not disappear. These parts can only occur in or-operations or in or-
like-operations like AtLeastOneFieldFilled.
• In the rule conditions of the Calcfillings section, the relevant parts of the preconditions are
marked as RULE_PART_RELEVANT, not the calculated field itself or the calculation.
Referenced Entities
Entities referenced in conflict components are called relevant if the conflict disappears when they
are left out.
• In the A12 Document, the referenced entities are reported. They have a relevant-flag which
indicates their relevance.
◦ They are reported for every conflict component along with the information of whether they
are relevant for that conflict component or not.
◦ The group /RuleContradictions/Conflict/EntitiesReferenced contains all the entities which
are referenced in any conflict component. They are marked relevant if they are relevant in
any conflict component.
• In the html-report, one can see the relevant entities along with their parents on the right side in
a tree-view.
• If you move the mouse over a conflict component,
◦ the entities which are relevant in this conflict component are being highlighted in the tree-
view.
◦ the caption of other conflict components which have relevant entities in common with the
currently hovered conflict component are highlighted as well.
Pretty Values
Introduction
By default, TDG tries to fill the created datasets with technical interesting values. In particular, TDG
creates extreme and special values or random values. For string fields, TDG tries to use every
allowed character.
A typical TDG default output looks like this:
NOTE
If you use TDG for automized tests, we highly recommend to use the values TDG
produces by default. In real life applications, we found a lot of errors in our
applications by the use of extreme and special values and by using every allowed
43

-- 43 of 55 --

character.
Pretty Values Option
For manual usage, datasets with more readable values might be preferred. Therefore, TDG has the
option --valuesPretty or -vP. If you set this option, TDG tries to create pretty, readable values for all
types of fields.
Example of a TDG output with the --valuesPretty set:
TDG tries to use Pretty Values for Dates and Numbers. For Strings, TDG inserts either a single word
or a fake sentence, consisting of real words.
Use Test Data Hints
TDG is capable of creating more realistic data for names, addresses, phone numbers and email
addresses. This is achieved by adding the annotation A12_TDG_TDH to the relevant fields. The value of
the annotation contains the information about which kind of data the field contains:
Value of annotation
A12_TDG_TDH
explanation
FIRSTNAME firstname of a person
LASTNAME lastname of a person
BIRTHDAY birthday of a person
SALUTATION salutation like 'Mr.' or 'Ms.'
TITLE academic title
STREET name of a street without the street number
STREET_DE German street name without the street number
STREET_INT international street name without the street number
STREET_NUMBER street number
STREET_NUMBER_ADDITION addition to the street number
STREET_AND_NUMBER street name and number
STREET_AND_NUMBER_DE German street name and number
STREET_AND_NUMBER_INT international street name and number
ADDRESS_ADDITION additional address information
ZIPCODE zip code
ZIPCODE_DE German zip code
44

-- 44 of 55 --

Value of annotation
A12_TDG_TDH
explanation
ZIPCODE_INT international zip code
CITY name of city
CITY_DE name of German city
CITY_INT name of foreign city
COUNTRY name of a country
PHONE complete phone number
PHONE_INT international part of phone number
PHONE_AREA area part of phone number
PHONE_LOCAL local part of phone number
EMAIL EMail address
WEB_ADDRESS web address
COMPANY_NAME name of company
COMPANY_NAME_DE name of German company
COMPANY_NAME_INT name of foreign company
TAX_OFFICE A tax office
TAX_NUMBER A tax number
(Only for String fields - Do not use this Annotation for Custom
field type "Steuernummer"!)
Example output with Test Data hints:
In this example, the names of the fields are the same as the value of their A12_TDG_TDH annotation.
This does not have to be the case. The fieldnames are not interpreted by TDG. Only the value of the
A12_TDG_TDH annotation is relevant in order to create data with test data hints.
For String fields, you can also put more than one of the constants above in the value of the
A12_TDG_TDH annotation. They have to be separated by ' ' or by ', '. Examples:
45

-- 45 of 55 --

Value of annotation
A12_TDG_TDH
Example Value
FIRSTNAME LASTNAME Bob Miller
STREET_AND_NUMBER_DE,
ZIPCODE_DE CITY_DE
Musterstrasse 42, 55555 Musterstadt
Counters
Sometimes, the test data should contain a field whose value is a counter. For this case, annotate the
field with the annotation A12_TDG_COUNTER. If the annotation has a value, that value is used as a
prefix of the counter.
Please notice:
• TDG only evaluates the A12_TDG_COUNTER annotation for fields which are free fields. For bound
fields, the annotation is ignored.
• TDG only evaluates the A12_TDG_COUNTER annotation for fields of type String or Number.
• The annotation overwrites the evaluation of the A12_TDG_TDH annotation.
Limitations
The creation of Pretty Values is a "weak" requirement in TDG. TDG might not be able to use Pretty
Values if
• The formal properties of the field forbid the Pretty Values TDG tries to fill in. This might be the
case if a number field has atypical minimal or maximal values or if a string field has a pattern.
• The field is a bound field. For bound fields, the values of the fields are chosen by the SMT-
Solver. TDG still tries to create Pretty Values, but the rules might not allow this.
Plugins
There are three types of plugins (custom extensions) available for TDG:
• Custom Field Types (CFT)
• Custom Conditions (CC), also called "Application Conditions"
• Specialities
Custom Field Types as well as Custom Conditions are standard A12 extensions. Specialities, however,
are TDG-specific plugins that allow you to add additional rules or to manipulate fields of a
Document Model. Please contact the TDG team if assistance regarding Specialities is required, as no
further information will be presented here.
Generally, plugins introduce custom logic which cannot be automatically reproduced by the TDG.
For instance, a Custom Field Type may correspond to an internal order number, which has a very
strict structure. On the one hand, without providing custom logic TDG cannot generate correct
46

-- 46 of 55 --

values according to an unknown rule set. On the other hand, test data production shall not fail
because of a missing plugin implementation. As a consequence, missing plugin implementations
are mocked internally by a neutral dummy implementation:
• For CFTs: Missing implementations are replaced by Dummy-CFT implementations that generate
values according to the CFT-field’s properties (min/max length), example values may be given
by annotations.
• For CCs: Missing implementations are replaced by a neutral Dummy-CC implementation which
never causes a validation error and has no affect in the rules it is used in.
Providing CFT Example Values
Instead of providing an actual CFT implementation, it is possible to only provide (valid) example
values that shall be used during test data production. In the Document Model, annotate CFT fields
with the annotation A12_TDG_CUSTOM_FIELDTYPE_TESTDATA (as a comma separated list of example
values) in order to enforce certain values. A few remarks regarding this approach:
• Annotations are type-specific, i.e. if there are multiple field instances of the same CFT, all
annotation values are used for all fields of this type.
• Annotation values must be valid with respect to the CFT field’s formal restrictions (min/max
length), otherwise they are not used.
• If an actual CFT implementation is provided, the annotation values are ignored, so it is possible
to annotate fields first, if no implementation is ready yet. Annotations do not need to be
removed once the implementation is ready.
Providing Plugin Implementations
Providing custom implementations is only possible when using TDG as a library. For the Command
Line Tool, unknown plugins are automatically mocked by respective dummy implementations (but
annotations are applied). Custom plugin implementations can be passed via ContentSettings. Note
that TDG's plugin interfaces are different to Kernel’s plugin interfaces as TDG requires additional
information to generate values (instead if only validating values). For custom field types, implement
the CustomFieldPlugin interface. For custom conditions, implement the CustomConditionPlugin
interface. For further information and assistance please contact the TDG team.
NOTE
For default A12 CFTs a CustomFieldPlugin implementation can be accessed by
including the tdg-cft package.
<dependency>
<groupId>com.mgmtp.a12.tdg</groupId>
<artifactId>tdg-cft</artifactId>
<version>8.1.0</version>
</dependency>
The package includes implementations for the following A12 default custom field
types:
47

-- 47 of 55 --

CFT name (as used
in the Document
Model)
Description Requires German
Tax Data
BIC Business Identifier Code (SEPA
countries)
no
BICInternational Business Identifier Code (international) no
IBAN International Bank Account Number
(SEPA countries)
no
IBANinternational International Bank Account Number
(international)
no
IDNr German tax identification number
(Identifikationsnummer)
no
ISIN International Securities Identification
Number
no
Bundesfinanzamtsnum
mer
German federal tax office number (4
digits)
yes
Steuernummer German tax number yes
EinheitswertAktenze
ichen
Property valuation reference number
(EWAz)
yes
StrafAktenzeichen Criminal case reference number yes
Use the class CustomFieldPluginFacade from the tdg-cft module to obtain the default
implementations and register them via ContentSettingsBuilder.customFieldTypes(…
):
// Without German Tax Data (the four German-tax CFTs are not registered,
// TDG falls back to the neutral dummy implementation for them):
myContentSettingsBuilder.customFieldTypes(
CustomFieldPluginFacade.defaultCustomFieldPlugins());
// With German Tax Data (all eleven default CFTs are registered):
IFadat fadat = IFadat.deserialize(reader, /*testTaxOfficesAllowed*/
false);
myContentSettingsBuilder.customFieldTypes(
CustomFieldPluginFacade.defaultCustomFieldPlugins(fadat));
See German Tax Data for details on the IFadat file format and the
testTaxOfficesAllowed flag.
Reporting Missing Plugin Implementations
If you use TDG as a library and want to be informed about missing custom field type
implementations, you can set ContentSettingsBuilder.reportUnimplementedCustomFields(true).
48

-- 48 of 55 --

Fields with missing custom-field-type implementations and fields used in the Invalid() or Valid()
language constructs are than not filled and are reported in the rule contradiction report instead.
Note: Fields used in the Valid() language construct whose CFT has no implementation are never
filled, regardless of this setting. The reason is that the validator considers all custom field values
valid in this case, which leads to an error for the language construct Valid().
German Tax Data (IFadat)
Four of the default CFTs bundled in tdg-cft implement concrete check-digit, pattern and
Bundesfinanzamt rules of the German tax system and therefore need a list of valid
Bundesfinanzamtsnummern (BUFA) at runtime:
• Bundesfinanzamtsnummer
• Steuernummer
• EinheitswertAktenzeichen (EWAz)
• StrafAktenzeichen
This information is passed in as an IFadat instance (Finanzamts-Daten). Both the Command Line
Tool (via the -x option) and the Library (via ContentSettings) use the same JSON file format.
File Format
The file is a JSON document with a top-level array bufaNrs, each entry containing a 4-digit federal
tax office number:
{
"bufaNrs": [
{ "bufaNr": "2210" },
{ "bufaNr": "2244" },
{ "bufaNr": "3065" }
]
}
The first two digits of a BUFA identify the federal state (see FederalState for the mapping, e.g. 22 =
Hamburg, 30 = Brandenburg). The file must list at least one BUFA per federal state that the
generated test data should cover.
The testTaxOfficesAllowed Flag
IFadat.deserialize(Reader, boolean testTaxOfficesAllowed) accepts a second parameter that
controls whether the dedicated Test-Finanzämter are accepted in addition to the BUFAs listed in the
file. Pass true to also enable the hard-coded test tax offices, false to restrict the data to the entries in
the file. The Command Line Tool currently passes false and provides no switch to change this.
49

-- 49 of 55 --

Behavior when the File is Missing
If the German tax CFTs are used in a Document Model but no IFadat is provided:
• On the Command Line Tool (no -x option): only the seven non-tax CFTs are registered; the four
German-tax CFTs fall back to the neutral dummy implementation, which generates values that
are formally valid (length, pattern) but not realistic tax numbers.
• When using the library: The same fallback applies if neither a real nor an annotation-based
implementation is provided. Consider using ContentSettings.reportUnimplementedCustomFields()
to have this situation reported by the rule contradiction report instead.
Not Implemented Features
Kernel Language Constructs
Currently, all kernel language constructs are supported by TDG. However, single constellations may
be not implemented.
Kernel Language Constellations
A kernel language constellation is the usage of a kernel language construct in a special way. A
typical example would be a kernel language construct, used with a filter condition or a semantic
index.
We try to close all gaps of not implemented constellations step by step. However, it’s possible, that
there are still some gaps in rare constellations. In these cases, TDG will throw a
TdgMappingNotImplementedException, which contains the rules, in which the not implemented
constellation was found.
If a TdgMappingNotImplementedException is thrown, please do not hesitate to in inform the TDG-Team.
These language constructs are the most probable to be used in an unsupported constellation:
• String concatenations are extremely crucial for TDG. Most missing constellations are
connected with complicated string concatenations. Some of them are not representable in the
way, TDG currently maps strings to SMT-Lib. We are working on a new solution, which
completely supports all constellations of string concatenations.
• Categories are not supported in some language constructs. This is especially the case, if
categories are concatenated with other strings.
• Type conversions between Numbers, Strings and Dates might not be implemented.
• Filter conditions or the usage of a semantic index might not be implemented in rare
situations.
50

-- 50 of 55 --

Complicated regular expressions
Complicated regular expressions, might not be implemented:
• Lookarounds
• Regular expressions used together with PartAs() constructs or string concatenations, if TDG
cannot split up the regular expression accordingly.
Complicated Non-Linearities
TDG can handle simple non-linearities. It uses an own algorithm to eliminate such non-linearities.
More complicated non-linearities like calculations of 3rd grade or higher will possibly lead to
timeouts and to no solution. The same is true for irresolvable calculations.
Complicated or Huge Document Models
The calculation time to find valid data sets increases with the size and the complexity of the
document model. It does not increase linearly. One single, complicated rule might blow up the
runtime totally. If the test data production leads to a lot of timeouts, it might help to reduce maxRep
or to increase the timeout.
Tables
TDG Annotations
Annotation
name
Meaning Link in documentation
A12_TDG_UNFI
LLABLE_OK
Entities marked with this annotation are
allowed to be unfillable. These entities are
ignored in the RULE_CONTRADICTIONS and
statistics.
4.2.1. Allow Fields to be
Unfillable
A12_TDG_NOT_
ALL_REPS_FIL
LABLE_OK
If at least one repetition of an entity marked
with this annotation can be filled, this entity is
not reported by the RULE_CONTRADICTIONS.
----------------------------------------
A12_TDG_MIN
REP
Indicates the minimal repetition of a group for
test data generation.
A12_TDG_MINREP Usage
A12_TDG_CUST
OM_FIELDTYP
E_TESTDATA
The value of this annotation contains field
values, which are used. This annotation can be
used in case the custom field type is not
programmatically implemented.
6.1. Providing CFT Example
Values
51

-- 51 of 55 --

A12_TDG_COU
NTER
Indicates a string field that is used as a counter
for data sets.
NOTE: Has to be a free field of type string or
number.
5.4. Counters
A12_TDG_TDH Serves as a hint for creating pretty strings: The
value of this annotation tells TDG what
information to put into the fields value.
5.3. Use Test Data Hints
_A12_TDG_IGN
ORE_CUSTOM_
CONDITIONS
Ignores the custom conditions for rules marked
with this annotation.
----------------------------------------
Migration Instructions
NOTE
If used as a library, only migrate the TDG version in accordance with the Kernel
version used in your project. TDG and Kernel version have to be aligned, i.e. they
should match the same -ext release in order to avoid inconsistencies. Within a
release line, different TDG and Kernel ext-Release are compatible, however it is not
recommended due to potential inconsistencies in third party library dependencies.
2025.06
Breaking Changes
• [A12TDG-476] - A12TDG is compiled with Java 21 - Compatibility with java 17 is dropped.
• [A12TDG-451] - TdgAnnotations was renamed to TdgAnnotation.
Breaking Change A12 internal
• [A12TDG-478] - Use ICondition in TDG plugins: The A12 internal interfaces ITdgCustomCondition
and ITdgSpecialty are changed from a CSP interface to an A12 interface. Conditions have to be
provided as an A12 ICondition from now on. For ITdgCustomCondition, the interface is
completely redone, for ITdgSpecialty, the old functions are deprecated. The new interfaces
provide an IConditionBuilder, which helps to create an ICondition.
2026.06
Breaking Changes
• [A12TDG-577] - In ContentSettings (2025.06: ITdgContentSettings) and ContentSettingsBuilder
(2025.06: TdgContentSettingsBuilder), suppressAutomaticCalculation() is renamed to
suppressCalculations().
• [A12TDG-577] - In ModelAnalysis (2025.06: ITdgModelAnalysis), the FieldValueUsage keys were
renamed.
52

-- 52 of 55 --

• [A12TDG-577] - In TechnicalSettings (2025.06: ITdgTechSettings), stringStrategy() was removed,
as it’s not needed any more.
• [A12TDG-577] - Removed the A12 internal interface DataDocumentBuilder.
• [A12TDG-183] - Renamed ITdgCheckModelResult to RuleContradictionReport and ProductionGoal
CHECK_FILLING to RULE_CONTRADICTIONS.
• [A12TDG-478] - In the A12 internal interface SpecialtyPlugin (2025.06: ITdgSpecialty), the
deprecated functions were removed.
• [A12TDG-612] - We removed the TDG and I-Prefix from the names of most of our classes and
Interfaces and did some other refactorings:
2025.06 Name 2026.06 Name
package com.mgmtp.a12.tdg.api.a12internal
ITdgCustomFieldTypeInternal deleted
ITdgEc TdgElementContainerNoRules
ITdgEc.IModelInfo TdgElementContainerNoRules.ModelInfo
ITdgElementContainer TdgElementContainer
ITdgReloadablePlugin deleted
ITdgSpecialty SpecialtyPlugin
ITdgSpecialty.IRuleCreator SpecialtyPlugin.RuleCreator
ITdgSpecialty.ITargetConstraintCreator SpecialtyPlugin.TargetConstraintCreator
ITdgValidator ValidationService
TdgPluginsResourceProvider deleted
TdgResultInternal ResultDocumentInternal
package com.mgmtp.a12.tdg.api.a12internal.plugins
ITdgCustomCondition CustomConditionPlugin
ITdgCustomCondition.INegativeTestCaseCreator CustomConditionPlugin.NegativeTestCaseCreator
ITdgCustomCondition.ITargetConstraintCreatorRu
leContext
CustomConditionPlugin.TargetConstraintCreatorR
uleContext
package com.mgmtp.a12.tdg.api.a12internal.plugins.provided
IConditionBuilder ConditionBuilder
IEntityIdBuilder EntityIdBuilder
IFieldManipulator FieldManipulator
package com.mgmtp.a12.tdg.api.plugins
ITdgCustomFieldType CustomFieldPlugin
package com.mgmtp.a12.tdg.api.plugins.provided
IPluginHelper PluginHelper
package com.mgmtp.a12.tdg.api.result
ITdgCheckModelResult RuleContradictionReport
ITdgModelAnalysis ModelAnalysis
53

-- 53 of 55 --

2025.06 Name 2026.06 Name
ITdgModelAnalysis.ITdgFieldAnalysis ModelAnalysis.FieldAnalysis
ITdgProductionResult ProductionResult
ITdgResult ResultDocument
ITdgTestDataResult DataGoalResult
package com.mgmtp.a12.tdg.api.result.dgs
IDgsInfo DataGoalSpecificInfo
IDgsInfoAllFields DgsInfoAllFields
IDgsInfoAnnotationDriven DgsInfoAnnotationDriven
IDgsInfoHtc DgsInfoHtc
IDgsInfoIgnoreRules DgsInfoIgnoreRules
IDgsInfoMaxi DgsInfoMaxi
IDgsInfoMini DgsInfoMini
IDgsInfoNegative DgsInfoNegative
IDgsInfoRootGroup DgsInfoRootGroup
package com.mgmtp.a12.tdg.api.result.validation
IMessageToday MessageWithToday
IRuleId RuleId
ITdgValidationInfo ValidationInfo
package com.mgmtp.a12.tdg.api.settings
ITdgContentSettings
ITdgContentSettings.customFieldTypes()
ContentSettings
ContentSettings.customFieldPlugins()
ITdgPrecondition Precondition
ITdgPreconditions Preconditions
ITdgTechSettings TechnicalSettings
TdgDataGoal DataGoal
package com.mgmtp.a12.tdg.cft.a12internal
TdgCustomFieldTypeFacade CustomFieldPluginsFacade
package com.mgmtp.a12.tdg.lib
ITdgCallbacks ProductionCallbacks
package com.mgmtp.a12.tdg.lib.util
TdgContentSettingsBuilder
TdgContentSettingsBuilder.customFieldTypes()
TdgContentSettingsBuilder.addCustomFieldType()
ContentSettingsBuilder
ContentSettingsBuilder.customFieldPlugins()
ContentSettingsBuilder.addCustomFieldPlugin()
TdgPreconditionsBuilder PreconditionsBuilder
TdgTechSettingsBuilder TechnicalSettingsBuilder
54

-- 54 of 55 --

2025.06 Name 2026.06 Name
package com.mgmtp.a12.tdg.statistic.api
ITdgStatisticCollector Statistic (split into Statistic and
StatisticCollector)
ITdgStatisticId StatisticId
ITdgStatisticId.ITdgStatisticGroup StatisticId.StatisticGroup
ITdgStatisticId.TdgStatisticImportance StatisticId.StatisticImportance
package com.mgmtp.a12.tdg.strings.api
IStringGenerator StringGenerator
ITdgRegEx InvertiblePattern
• [A12TDG-613] - Remove TdgFieldType: Use a switch over the IFieldType instead.
• [A12TDG-616] - Changes in DataGoalSpecificInfo: We removed type() - use a switch over the
DataGoalSpecificType instead.
• [A12TDG-614] - Combine PluginHelper and CustomFieldTypeHelper: Use PluginHelper instead of
CustomFieldTypeHelper
• [A12TDG-617] - Use LocalDateTime in ContentSettings.todayValuesRules() and
PluginHelper.todayValuesRules()
• [A12TDG-224] - Use LocalDateTime instead of LocalDate in all interfaces describing "today"-
values, specifically in ResultDocument and within ValidationInfo
• [A12TDG-632] - The command-line option -rcr, --ruleContradictionReport was renamed to -no
-rcr, --noRuleContradictionReport with inverted semantics. The rule contradiction report is still
generated by default; set -no-rcr to suppress it.
• [A12TDG-632] - The command-line short flag -tn was renamed to -nt. The long form --noToday is
unchanged.
• [A12TDG-634] - The annotation A12_TDG_COUNTER is replaced by the usage of
IDocumentUniquenessCriterion.
• [A12TDG-410] - In ContentSettings and ContentSettingsBuilder,
throwIfCustomFieldTypeNotImplemented() was removed. You can use the new setting
reportUnimplementedCustomFields() instead, which sets unimplemented custom field types to
unfilled and reports them in the rule contradiction report.
55

-- 55 of 55 --

