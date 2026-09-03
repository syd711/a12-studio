# overall dev_tutorial_backend_document_manipulation

Task 2 - Document Manipulation
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Backend > Introduction
before continuing here.
You can check out the tag 2025.06-ext5/backend/task-2-start to follow along.
If you get stuck, you can check out the tag 2025.06-ext5/backend/task-2-intermediate or 2025.06-
ext5/backend/task-2-end (depending on the point you are stuck at) to see how your code differs
from the solution.
Use-Case
For our CRM system, we will implement a change history in this task. We will track information
about when changes were made over the course of one year, by whom, and provide a general
overview of what changes were made to a contact.
As in task 1, we will also hook into the document creation and document update events. When
creating a document from the Document Model Contact_DM, we will extend the data to include the
information created at and created by. With each update of a document from the same Document
Model, we will extend the data in the document to include the information modified at, modified
by and change details. In addition to that, we will remove entries in the change history that are
older than one year. The new data will be returned to Data Services so that it can be persisted.
NOTE
Some of the data, we are dealing with in this task, can be also found in the metadata
of the document which is delivered by Data Services. However, we will not use it
since we want to show how to manipulate data in a document.
End Result
Upon finishing this task, you will know:
• How to manipulate data in a document, especially in repeatable groups.
• How to generate typed accessor classes for a specific Document Model to work with documents
more conveniently.
1

-- 1 of 28 --

• How to use those generated typed accessor classes.
2

-- 2 of 28 --

Step-by-Step Instructions
Extended Model Workspace
If you are interested in the general workspace of the tutorial, then check out Tutorials > Intro >
Modeling.
The model workspace of the tutorial application has been extended for this task.
The following models were extended:
• Contact_DM: The group /Contact/HistoryInfo was added. This new group contains all the fields
needed for this task.
3

-- 3 of 28 --

• Contact_FM: The collapsible section ChangeInfo under Screen1 was added. This extension was
mainly done to display the information added in the backend. Although this is not strictly
necessary for this task, it serves as an effective way to test our changes.
4

-- 4 of 28 --

Working with Documents
As you learned in Tutorials > Backend > Document Access, in A12, Data Service takes care of the
management of documents and provides an API to get access to them. But when working directly
with documents, i.e. reading or modifying the content, we have to refer to the A12 component
Kernel.
In A12, Kernel is a low level component which, from a developer point of view, mainly provides the
functionality around the data validation and computation based on the data. Within its scope,
Kernel provides a so-called Document API to deal with documents. At the moment, there are two
versions of it:
• V1
◦ Mainly identified by the interface IDocument.
◦ The main reason why this version was created was the data validation and the execution of
computations.
◦ Represents the data in a flat hierarchy composed by a collection of entity (field and/or
group) instances which are identified by the corresponding absolute path and the
corresponding repetitions.
◦ The API is mutable.
◦ Due to the difficulty to work with this version, it is deprecated and must not be used
5

-- 5 of 28 --

anymore.
• V2
◦ Mainly identified by the interface DocumentV2.
◦ Represents the data in a tree-like structure with nodes pointing to concrete entity instances
in the document.
◦ The API is immutable.
◦ As the structure between the document and the corresponding Document Model is similar
in this version, the usage is much simpler and more intuitive.
For this task, we will only focus on the Document API V2.
Modification of a Document
As mentioned previously, we will hook into the document creation and document update events
from Data Services. In the Task 1 of the Backend tutorial, a further example regarding
implementing a custom event listener can be found.
Your task
• In the package com.mgmtp.a12.tutorial.server.change.info, create the class
ChangeInfoListener.java with a skeleton structure (no implementation yet) so that we can
implement the event listener for the events DocumentBeforeCreateEvent and
DocumentBeforeUpdateEvent there.
• Have a look at the class ChangeInfoService.java which contains a skeleton structure where we
will implement the logic related to the modification of a document.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoListener.java
package com.mgmtp.a12.tutorial.server.change.info;
//...
/**
* Class that represents the collection of event listeners for managing the change
information of a document.
* The goal of this class is to listen for events before creating or modifying
documents, in order to extend them
* with the necessary information.
*/
@Component
@RequiredArgsConstructor
public class ChangeInfoListener {
/**
* Before the document is created, the creation info will be set from the
6

-- 6 of 28 --

following listener.
* The creation info includes creation time and creator.
*/
@CommonDataServicesEventListener
public void beforeCreateListener(DocumentBeforeCreateEvent event) {
// Put your code here ...
}
/**
* Before the document is updated, the history entries will be cleaned up
(entries older than 1 year will be
* removed), and the modification info will be set from the following listener.
* The modification info includes modification time, modifier and change
details.
*/
@CommonDataServicesEventListener
public void beforeUpdateListener(DocumentBeforeUpdateEvent event) {
// Put your code here ...
}
}
NOTE
For the implementation, there are two ways to leverage Kernel capabilities:
• Generic approach where hard-coded paths from Document Model fields and
groups are used to access the document
• Specific approach tailored for a concrete Document Model, where typed classes
are used to access the document.
The choice of which approach to use is left to the user of the API since it always
depends on the concrete case. In this task, we will examine both approaches.
Common Information
The following aspects will be needed in both approaches:
• The field type of the fields CreatedAt and ModifiedAt is DateTime, so we will need an Instant
object to set their value.
• In the class ChangeInfoUtils, we are going to add methods whose implementation does not differ
between both approaches. The class initializes two beans which we are going to need later on,
and provides the method to retrieve the current user via Spring.
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/utils/ChangeInfoUtils.java
package com.mgmtp.a12.tutorial.server.utils;
//...
7

-- 7 of 28 --

@Component
@RequiredArgsConstructor
public class ChangeInfoUtils {
private final IModelLoader<IDocumentModel> documentModelLoader;
private final DocumentModelServiceFactory docModelServiceFactory;
public String getUserName() {
return ((UserDetails)
SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
}
}
Generic Approach
Let us list the interfaces that will assist us in the implementation with this approach (some of these
interfaces are already known from Tutorials > Backend > Document Access > Accessing a
Document):
• DocumentV2: Represents a specific instance of a Document Model.
• GroupInstanceV2: Represents a specific instance of a group in a Document Model. Note: For a
repeatable group, there can be more than one instance of the same group in the document.
• RepetitionsV2: Represents the repetitions of a group in a Document Model. This interface is for
repeatable groups especially useful.
• FieldInstanceV2: Represents a specific instance of a field in a Document Model. For filled fields,
it contains a typed value.
• DocumentPointer: Represents an identifier for a specific entity (field or group) instance in a
document. This identifier contains information about the entity path and the specific repetition
indices of all its ancestors. Internally, it is defined as List<PathPart>.
• PathPart: Tuple of entity name and corresponding repetition index.
• UpdateAction: Contains the information necessary to make a change in a document. This is
especially useful when multiple changes need to be made to the document at once.
Provided Code
In the class Constants, you will already find the definition of some instances of the interface
DocumentPointer, e.g. Constants.CONTACT_POINTER or Constants.CONTACT_HISTORY_INFO_POINTER. With
this Kernel API, you can also work with paths, but to make more extensive use of the API, we
decided to use DocumentPointer wherever we could.
Some of the constants follow a path-like structure, e.g.
Constants.CONTACT_MODIFIED_AT_POINTER_PATTERN or
Constants.CONTACT_CHANGED_PROPERTY_POINTER_PATTERN. That follows the pattern of the string
representation of a pointer where each argument will be replaced by the specific repetition index
of the corresponding repeatable group.
8

-- 8 of 28 --

File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/utils/Constants.java
package com.mgmtp.a12.tutorial.server.utils;
//...
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
// not complete, since only contains the constants mentioned above
public static final DocumentPointer CONTACT_POINTER =
DocumentPointer.of("Contact");
public static final DocumentPointer CONTACT_HISTORY_INFO_POINTER =
CONTACT_POINTER.withAppended("HistoryInfo", 1);
public static final String CONTACT_MODIFIED_AT_POINTER_PATTERN =
"/Contact/HistoryInfo/ChangeHistory[%s]/ModifiedAt";
public static final String CONTACT_CHANGED_PROPERTY_POINTER_PATTERN =
"/Contact/HistoryInfo/ChangeHistory[%s]/ChangeDetails[%s]/ChangedProperty";
}
Implementation
Your task:
• Implement the method ChangeInfoService#setCreationInfo:
◦ The method should be executed only if we have a document of the Document Model
Contact_DM.
◦ Retrieve the current user and create an Instant object with the current timestamp.
◦ Have a look at the methods provided in the interface UpdateAction.
◦ Create a list of UpdateAction in order to add the following field values:
▪ "/Contact/HistoryInfo/CreatedAt=<Instant object with the current timestamp>"
▪ "/Contact/HistoryInfo/CreatedBy=<current user>"
▪ Think about the constants already defined in Constants.
◦ Apply the previously created list to the document (see method withBatchUpdates).
◦ Return the updated document.
• Call the method ChangeInfoService#setCreationInfo in ChangeInfoListener#beforeCreateListener.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoService.java
9

-- 9 of 28 --

package com.mgmtp.a12.tutorial.server.change.info;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoService {
public DocumentV2 setCreationInfo(DocumentV2 document) {
if (!CONTACT_MODEL_NAME.equals(document.getDocumentModelId())) {
return document;
}
Instant createdAtInstant = Instant.now();
String userName = changeInfoUtils.getUserName();
List<UpdateAction> updates = List.of(
UpdateAction.putFieldValue(CONTACT_CREATED_AT_POINTER,
createdAtInstant),
UpdateAction.putFieldValue(CONTACT_CREATED_BY_POINTER, userName)
);
return document.withBatchUpdates(updates);
}
// ...
}
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoListener.java
package com.mgmtp.a12.tutorial.server.change.info;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoListener {
private final ChangeInfoService changeInfoService;
@CommonDataServicesEventListener
public void beforeCreateListener(DocumentBeforeCreateEvent event) {
DocumentV2 newDocument =
changeInfoService.setCreationInfo(event.getCreatedDocument());
event.setCreatedDocument(newDocument);
}
//...
10

-- 10 of 28 --

}
NOTE
When reviewing the methods provided in the DocumentV2 interface, we could use the
method withFieldValue. However, based on the implementation, each update would
produce a new DocumentV2-object which could affect the performance of the
application. That’s why it is recommended to gather all changes and apply them all
simultaneously as we did before.
While updating a document (the next step), there are mainly two actions which should be
implemented:
• Remove entries from the change history older than one year.
• Update the modification information if there are relevant changes in the document.
Your task:
• Implement the utility method ChangeInfoService#getDocWithEntriesUpToDate(DocumentV2):
◦ Iterate over all the repetitions of the group /Contact/HistoryInfo/ChangeHistory.
◦ Based on the field ModifiedAt, filter the entries that are older than 1 year.
◦ Return a document with up-to-date entries.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoService.java
package com.mgmtp.a12.tutorial.server.change.info;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoService {
// ...
/**
* Removes all entries from the contact change history that are older than 1
year.
*/
private DocumentV2 getDocWithEntriesUpToDate(DocumentV2 document) {
Instant oneYearAgo = ZonedDateTime.now().minusYears(1).toInstant();
List<GroupInstanceV2> upToDateEntries = new ArrayList<>();
boolean withOutdatedEntries = false;
for (GroupInstanceV2 entry :
document.groupAllRepetitions(CONTACT_CHANGE_HISTORY_POINTER)) {
boolean youngerThanOneYear =
Optional.ofNullable(entry.directField(CONTACT_MODIFIED_AT_FIELD_NAME))
11

-- 11 of 28 --

.map(FieldInstanceV2::value)
.map(Instant.class::cast)
.map(instant -> instant.isAfter(oneYearAgo))
.orElse(false);
if (youngerThanOneYear) {
upToDateEntries.add(entry);
} else {
withOutdatedEntries = true;
}
}
if (withOutdatedEntries) {
return document.withGroupAllRepetitions(CONTACT_CHANGE_HISTORY_POINTER,
RepetitionsV2.of(upToDateEntries));
} else {
return document;
}
}
}
The implementation of the following method does not differ between the two approaches and
therefore, it should be implemented in ChangeInfoUtils.
Your task:
• Implement the utility method ChangeInfoUtils#determineRelevantFieldChanges(DocumentV2
document, DocumentV2 reference) which returns a list with changes in relevant fields:
◦ With the method DocumentV2Utils.compare from the Kernel API, determine the changes in the
document.
◦ If there are no changes, an empty list is returned.
◦ If there are changes, determine the relevant ones: The enumeration values of the field
/Contact/HistoryInfo/ChangeHistory/ChangeDetails/ChangedProperty contain the relevant
field names.
◦ Getting the entity name of a DocumentPointer is something which should be provided as a
separate small utility method.
◦ Return the relevant field changes.
▼ Click to see solution
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/utils/ChangeInfoUtils.java
package com.mgmtp.a12.tutorial.server.utils;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoUtils {
12

-- 12 of 28 --

// ...
public String getEntityName(DocumentPointer pointer) {
return pointer.getPathParts().getLast().name();
}
/**
* Determines the relevant field changes between the given document and its
reference.
* Relevant field changes are those that correspond to the enumeration values of
the field
* <code>ChangedProperty</code> in the contact model.
*
* @param document the document containing the changes.
* @param reference the reference document to compare against.
* @return a list of relevant field changes.
*/
public List<Change<FieldInstanceV2>> determineRelevantFieldChanges(DocumentV2
document, DocumentV2 reference) {
DocumentV2Utils.CompareConfig config =
DocumentV2Utils.CompareConfig.builder().build();
DocumentChanges documentChanges = DocumentV2Utils.compare(reference,
document, config);
if (documentChanges.noChanges()) {
return List.of();
}
Set<String> relevantFieldNames = getRelevantFieldNames(document);
List<Change<FieldInstanceV2>> relevantFieldChanges = new ArrayList<>();
for (Change<FieldInstanceV2> change :
documentChanges.expandedFieldChanges()) {
DocumentPointer pointer = change.pointer();
if (!relevantFieldNames.contains(getEntityName(pointer))) {
continue;
}
relevantFieldChanges.add(change);
}
return relevantFieldChanges;
}
/**
* The enumeration values of the field <code>ChangedProperty</code> correspond
to the field names that are relevant
* for the change history of a contact.
*
* @return the set of field names that are relevant for the change history.
*/
public Set<String> getRelevantFieldNames(DocumentV2 document) {
IDocumentModel docModel =
documentModelLoader.loadModel(document.getDocumentModelId());
IDocumentModelSearchService docModelSearchService =
13

-- 13 of 28 --

docModelServiceFactory.createDocumentModelSearchService(docModel);
return docModelSearchService.getByPath(CONTACT_CHANGED_PROPERTY_PATH)
.map(IField.class::cast)
.map(IField::getFieldType)
.map(IEnumerationType.class::cast)
.orElseThrow(
() -> new IllegalArgumentException(
"Field '%s' does not exist in the Document Model."
.formatted(CONTACT_CHANGED_PROPERTY_PATH))
)
.getValues().stream()
.map(IEnumerationType.IEnumValue::getValue)
.collect(Collectors.toSet());
}
}
Your task:
• Implement the method ChangeInfoService#updateModificationInfo:
◦ The method should be executed only if we have a document of the Document Model
Contact_DM.
◦ Call the method getDocWithEntriesUpToDate and from now on, the returned document will be
your "docWithUpToDateEntries".
◦ Determine the relevant field changes calling the method determineRelevantFieldChanges.
◦ If no changes are present, return the docWithUpToDateEntries.
◦ If there are relevant changes, update the change history:
▪ Retrieve the current user and create an Instant object with the current timestamp.
▪ Determine the next available repetition for the group
/Contact/HistoryInfo/ChangeHistory.
▪ With this information, you can now create a corresponding UpdateAction for setting the
values for the fields ModifiedAt and ModifiedBy considering the next available repetition.
▪ Based on the relevant changes, you can iterate over them to create the corresponding
UpdateAction for creating the following fields:
▪ ChangedProperty - set the name of the field which was changed as value.
▪ ChangeType - map the returned values of the methods Change#isAdd and
Change#isDelete to a valid enumeration value for the field.
▪ Repetition - set the repetition of the parent of the field which was changed as value.
The method for extracting this information should be implemented in
ChangeInfoUtils.
▪ Apply all the gathered UpdateAction instances to the document and return the resulting
one.
• Call the method ChangeInfoService#updateModificationInfo in
ChangeInfoListener#beforeUpdateListener.
14

-- 14 of 28 --

▼ Click to see solution
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/utils/ChangeInfoUtils.java
package com.mgmtp.a12.tutorial.server.utils;
// ...
@Component
@RequiredArgsConstructor
public class ChangeInfoUtils {
// ...
public BigDecimal getParentRepetition(DocumentPointer pointer) {
return BigDecimal.valueOf(pointer.parent().repetitionIndexes().getLast());
}
// ...
}
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoService.java
package com.mgmtp.a12.tutorial.server.change.info;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoService {
// ...
public DocumentV2 updateModificationInfo(DocumentV2 updatedDocument, DocumentV2
persistedDocument) {
if (!CONTACT_MODEL_NAME.equals(updatedDocument.getDocumentModelId())) {
return updatedDocument;
}
DocumentV2 docWithUpToDateEntries =
getDocWithEntriesUpToDate(updatedDocument);
List<Change<FieldInstanceV2>> relevantFieldChanges =
changeInfoUtils.determineRelevantFieldChanges(docWithUpToDateEntries,
persistedDocument);
if (relevantFieldChanges.isEmpty()) {
return docWithUpToDateEntries;
15

-- 15 of 28 --

}
Instant modifiedAtInstant = Instant.now();
String userName = changeInfoUtils.getUserName();
RepetitionsV2 repetitions =
docWithUpToDateEntries.groupAllRepetitions(CONTACT_CHANGE_HISTORY_POINTER);
int nextAvailableRepetitionIndex = repetitions.size() + 1;
List<UpdateAction> updates = new ArrayList<>();
updates.add(UpdateAction.putFieldValue(
CONTACT_MODIFIED_AT_POINTER_PATTERN.formatted(nextAvailableRepetitionIndex),
modifiedAtInstant));
updates.add(UpdateAction.putFieldValue(
CONTACT_MODIFIED_BY_POINTER_PATTERN.formatted(nextAvailableRepetitionIndex),
userName));
for (int i = 0; i < relevantFieldChanges.size(); i++) {
Change<FieldInstanceV2> change = relevantFieldChanges.get(i);
DocumentPointer pointer = change.pointer();
updates.add(UpdateAction.putFieldValue(
CONTACT_CHANGED_PROPERTY_POINTER_PATTERN.formatted(nextAvailableRepetitionIndex, i +
1),
changeInfoUtils.getEntityName(pointer)));
updates.add(UpdateAction.putFieldValue(
CONTACT_CHANGE_TYPE_POINTER_PATTERN.formatted(nextAvailableRepetitionIndex, i + 1),
mapChangeType(change)));
updates.add(UpdateAction.putFieldValue(
CONTACT_CHANGE_REPETITION_POINTER_PATTERN.formatted(nextAvailableRepetitionIndex, i
+ 1),
changeInfoUtils.getParentRepetition(pointer)));
}
return docWithUpToDateEntries.withBatchUpdates(updates);
}
// the returned values should correspond to the enumeration values of the field
`ChangeType`
private String mapChangeType(Change<FieldInstanceV2> change) {
if (change.isAdd()) {
return "added";
}
if (change.isDelete()) {
return "deleted";
} else {
return "updated";
}
}
16

-- 16 of 28 --

}
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoListener.java
package com.mgmtp.a12.tutorial.server.change.info;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoListener {
//...
@CommonDataServicesEventListener
public void beforeUpdateListener(DocumentBeforeUpdateEvent event) {
DocumentV2 newDocument = changeInfoService.updateModificationInfo(
event.getUpdatedDocument(),
event.getPersistedDocument());
event.setUpdatedDocument(newDocument);
}
}
The implementation is now complete. If you got stuck, you can check out the tag 2025.06-
ext5/backend/task-2-intermediate to see how your code differs from the solution for the generic
approach.
Testing
You can (re)start the server, see also Tutorials > General Information > Setup > Running the Code,
and play around in the application:
• Create a new contact and check the corresponding information.
• Update some properties of a contact and check the modification info.
• If you want to check the implementation regarding outdated modification info, you can import
the data in import/data/request/ContactsWithOldModificationInfoRequest.json, see also Tutorials
> Intro > Project Template > Creating New Contacts on Start-up.
◦ Open the contact Max Mustermann (you will see outdated modification info).
◦ Save it.
◦ Reopen the contact and check that the outdated information is not there anymore.
17

-- 17 of 28 --

Specific Approach
With this approach, the implemented solution works for a specific Document Model, or for
Document Models that share a part of the model, i.e. the paths which you want to deal with can be
found in all the corresponding models. Out of the specific Document Model, Java classes are
generated and reflect the group-field structure defined in the model. For the Document Model itself
and for each group, the following sources are generated:
• Class P<name>.java: where name is the Document Model id or the group name. This class is
categorized as "pointer class" and represents a document pointer from a parametrized root to
the entity represented by the class.
• Class <name>.java: This class is categorized as "view class", allows the access to the data in the
document and the creation of document pointers rooted at the entity represented by the class.
• Package(s) _<name>: In plural since the two class categories, just mentioned above, are organized
in the packages views and pointers, and you might find such package in each of them, especially
when having nested groups. Later on, we will have a look at the structure generated out of our
Document Model Contact_DM in order to better understand what is actually produced.
Some of the advantages of this approach are:
• You get specific type checking at compile time:
◦ While implementing the solution, you know exactly which type you should use.
◦ Structural changes in the corresponding Document Model are recognized in an earlier stage.
• You can use the auto-completion in your IDE while implementing the solution.
From the interfaces listed in the previous approach, UpdateAction will also assist us in this
approach. Additionally, we should have a look at the following methods which are available in the
view classes:
• _pointer(): This method is available in view classes and allows the creation of typed document
pointers rooted at the entity represented by the class.
• TypedGroupView#_at(pointer): It provides access to the specific part of the document identified
by the pointer.
• TypedGroupView#_with(pointer, object): It creates an instance of the view class with the
specified object set at the document part identified by the given pointer.
• _unwrap: This method is available in the typed pointer classes and returns the untyped
corresponding object, e.g. DocumentPointer.
Generation of Typed Accessor Classes
The generation of the typed accessor classes can be done manually via a CLI-tool provided by
Kernel, see Kernel > Document API V2 (Java) > Manual Code-Generation via CLI. For this task, we
decided to configure Gradle in order to automatically generate the classes at build time. The Gradle
scripts which are used for this task are already adapted and we will now have a look at the
configuration.
18

-- 18 of 28 --

From the Project Template version 202506.2.0 onwards, the necessary Gradle configuration for
generating typed accessor classes is already included in the template. Therefore, if you started with
this or a later version of the Project Template, you just need to adapt the arguments for your
project.
The following configuration is already included in the base code and shown here for reference. The
only adaptation you need to make is to update the packagePrefix to match your project’s package
name.
File: server/app/build.gradle
//...
// the java-plugin at the beginning of the file is important for the generation and
compilation of the typed classes
plugins {
id 'java'
//...
}
//...
// new configuration to generate the typed classes
configurations {
runTypedAccessorGeneratorConfig
}
// directory where the typed classes will be generated
def generatedTypingsOutputDir =
layout.buildDirectory.dir("generatedTypings").get().asFile
sourceSets {
generatedTypings {
java {
// the classes in this directory are marked as source to be compiled
srcDir(generatedTypingsOutputDir)
}
//...
}
main {
java {
// we add the compiled classes to the classpath
compileClasspath += sourceSets.generatedTypings.output
runtimeClasspath += sourceSets.generatedTypings.output
}
}
}
dependencies {
//...
// needed to use the generated typed classes
generatedTypingsImplementation group: 'com.mgmtp.a12.kernel', name: 'kernel-md-
document-v2', version: a12Libs.versions.kernel.get()
runTypedAccessorGeneratorConfig group: 'com.mgmtp.a12.kernel', name: 'kernel-md-
typed-accessor-gen', version: a12Libs.versions.kernel.get(), classifier:
'TypedAccessorGenerator-CLI'
19

-- 19 of 28 --

}
// task to generate the classes; scans all Document Model files in import/models
tasks.register('runTypedAccessorGenerator', JavaExec) {
classpath = configurations.runTypedAccessorGeneratorConfig
mainClass =
'com.mgmtp.a12.kernel.md.document.typed_accessor_gen.TypedAccessorGenerator'
def modelPath = "${rootProject.projectDir.path}/import/models"
// adapt this to your project's package name
def packagePrefix = "com.mgmtp.a12.tutorial.server.typings"
def documentModels = fileTree(dir: "${modelPath}", include: ['**/*_DM.json',
'**/*_CDM.json'])
if (!documentModels || documentModels.isEmpty()) {
throw new GradleException("No model definition files found in ${modelPath}")
}
def argsList = []
argsList += ['--document-model-file', documentModels.first().absolutePath]
argsList += ['--package-prefix', packagePrefix]
argsList += ['--output-dir', generatedTypingsOutputDir]
documentModels.each { File jsonFile ->
argsList += ['-i', jsonFile.absolutePath]
}
args argsList
}
// compile step
tasks.compileGeneratedTypingsJava.dependsOn(runTypedAccessorGenerator)
If you run respective Gradle task by gradle compileGeneratedTypingsJava, the typed classes will be
generated and compiled. After doing this, those classes will be available in the classpath.
You will find the generated classes in the directory server/app/build/generatedTypings with the
following structure:
• pointers
20

-- 20 of 28 --

• views
21

-- 21 of 28 --

Now, in your IDE, type "CDM" in an existing class, and you should be able to see
com.mgmtp.a12.tutorial.server.typings.views.Contact_DM in the list of suggestions displayed in your
IDE.
Extension of the Implementation
We should now point out that the main differences between the two approaches is the way how to
create the document pointer. In some parts in the first approach, we used instances of the interface
DocumentPointer, and in other parts hard-coded patterns of the string representation of
DocumentPointer pointing to the entity we wanted to deal with. In this approach, the document
pointer can be obtained from the generated view classes as shown below:
• non-repeatable entities
PContact<Contact_DM> contactPointer = Contact_DM._pointer().contact(); // corresponds
to "/Contact[1]"
PHistoryInfo<Contact_DM> historyInfoPointer = contactPointer.historyInfo(); //
corresponds "/Contact[1]/HistoryInfo[1]"
• repeatable entities
TypedRepetitionsPointer<Contact_DM, Phones, PPhones<Contact_DM>> phonesPointerAll =
contactPointer.phones(); // corresponds to "/Contact[1]/Phones[0]" -> all repetitions
of Phones
PPhones<Contact_DM> phonesPointer1 = contactPointer.phones(1); // corresponds to
"/Contact[1]/Phones[1]"
Once the generated classes are available in the classpath, and we know how to create the document
pointers, we can proceed with the implementation of the change history with the typed classes.
Your task:
Define corresponding constants of typed pointers for CONTACT_POINTER,
CONTACT_HISTORY_INFO_POINTER, CONTACT_CREATED_AT_POINTER, CONTACT_CREATED_BY_POINTER and
CONTACT_CHANGE_HISTORY_POINTER.
▼ Click to see solution
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/utils/Constants.java
package com.mgmtp.a12.tutorial.server.utils;
// ...
@NoArgsConstructor(access = AccessLevel.PRIVATE)
22

-- 22 of 28 --

public final class Constants {
// ...
// typed pointer
public static final PContact<Contact_DM> CONTACT_TYPED_POINTER =
Contact_DM._pointer().contact();
public static final PHistoryInfo<Contact_DM> CONTACT_HISTORY_INFO_TYPED_POINTER
=
CONTACT_TYPED_POINTER.historyInfo();
public static final TypedPointer<Contact_DM, Instant>
CONTACT_CREATED_AT_TYPED_POINTER =
CONTACT_HISTORY_INFO_TYPED_POINTER.createdAt();
public static final TypedPointer<Contact_DM, String>
CONTACT_CREATED_BY_TYPED_POINTER =
CONTACT_HISTORY_INFO_TYPED_POINTER.createdBy();
public static final TypedRepetitionsPointer<Contact_DM, ChangeHistory,
PChangeHistory<Contact_DM>>
CONTACT_CHANGE_HISTORY_TYPED_POINTER =
CONTACT_HISTORY_INFO_TYPED_POINTER.changeHistory();
}
Your task:
Expand the implementation with this approach:
• Create the new class com.mgmtp.a12.tutorial.server.change.info.ChangeInfoServiceTyped and
add the method setCreationInfo(DocumentV2).
• Reimplement the method setCreationInfo, the logic is the same as in ChangeInfoService but the
implementation should be with the typed classes.
• Change the type of the class field ChangeInfoListener#changeInfoService to
ChangeInfoServiceTyped.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoServiceTyped.ja
va
package com.mgmtp.a12.tutorial.server.change.info;
//...
/**
* This is a version of the {@link ChangeInfoService}, which uses the generated
pointers and views for type safety.
*/
@Component
@RequiredArgsConstructor
public class ChangeInfoServiceTyped {
23

-- 23 of 28 --

private final ChangeInfoUtils changeInfoUtils;
/**
* Like {@link ChangeInfoService#setCreationInfo(DocumentV2)}, but uses the
generated accessor classes.
*/
public DocumentV2 setCreationInfo(DocumentV2 document) {
if (!CONTACT_MODEL_NAME.equals(document.getDocumentModelId())) {
return document;
}
Instant createdAtInstant = Instant.now();
String userName = changeInfoUtils.getUserName();
List<UpdateAction> updates = List.of(
UpdateAction.putFieldValue(CONTACT_CREATED_AT_TYPED_POINTER._unwrap(),
createdAtInstant),
UpdateAction.putFieldValue(CONTACT_CREATED_BY_TYPED_POINTER._unwrap(), userName)
);
return document.withBatchUpdates(updates);
}
// ...
}
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoListener.java
package com.mgmtp.a12.tutorial.server.change.info;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoListener {
private final ChangeInfoServiceTyped changeInfoService;
@CommonDataServicesEventListener
public void beforeCreateListener(DocumentBeforeCreateEvent event) {
DocumentV2 newDocument =
changeInfoService.setCreationInfo(event.getCreatedDocument());
event.setCreatedDocument(newDocument);
}
//...
24

-- 24 of 28 --

}
• Add and reimplement the method getDocWithEntriesUpToDate but using Contact_DM as parameter
type instead of DocumentV2.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoServiceTyped.ja
va
package com.mgmtp.a12.tutorial.server.change.info;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoServiceTyped {
// ...
/**
* Like {@link ChangeInfoService#getDocWithEntriesUpToDate(DocumentV2)}, but
uses the generated
* accessor classes.
*/
private Contact_DM getDocWithEntriesUpToDate(Contact_DM document) {
Instant oneYearAgo = ZonedDateTime.now().minusYears(1).toInstant();
List<ChangeHistory> upToDateEntries = new ArrayList<>();
boolean withOutdatedEntries = false;
for (ChangeHistory entry : document.contact().historyInfo().changeHistory())
{
if (entry.modifiedAt().isAfter(oneYearAgo)) {
upToDateEntries.add(entry);
} else {
withOutdatedEntries = true;
}
}
if (withOutdatedEntries) {
return document._with(CONTACT_CHANGE_HISTORY_TYPED_POINTER,
upToDateEntries);
} else {
return document;
}
}
}
• Add and reimplement the method mapChangeType but in this case the returned type should be
25

-- 25 of 28 --

ChangeType (from the generated typed classes).
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoServiceTyped.ja
va
package com.mgmtp.a12.tutorial.server.change.info;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoServiceTyped {
// ...
private ChangeType mapChangeType(Change<FieldInstanceV2> change) {
if (change.isAdd()) {
return ChangeType.added;
}
if (change.isDelete()) {
return ChangeType.deleted;
} else {
return ChangeType.updated;
}
}
}
• Add and reimplement the method updateModificationInfo, the logic is the same as in
ChangeInfoService but the implementation should be with the typed classes.
• Since ChangeInfoListener was changed previously, this class does not need to be adapted
anymore.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/change/info/ChangeInfoServiceTyped.ja
va
package com.mgmtp.a12.tutorial.server.change.info;
//...
@Component
@RequiredArgsConstructor
public class ChangeInfoServiceTyped {
// ...
26

-- 26 of 28 --

/**
* Like {@link ChangeInfoService#updateModificationInfo(DocumentV2,
DocumentV2)}, but uses the generated
* accessor classes.
*/
public DocumentV2 updateModificationInfo(DocumentV2 updatedDocument, DocumentV2
persistedDocument) {
if (!CONTACT_MODEL_NAME.equals(updatedDocument.getDocumentModelId())) {
return updatedDocument;
}
Contact_DM updatedTypedDoc = Contact_DM._viewOf(updatedDocument);
Contact_DM typedDocWithUpToDateEntries =
getDocWithEntriesUpToDate(updatedTypedDoc);
List<Change<FieldInstanceV2>> fieldChanges =
changeInfoUtils.determineRelevantFieldChanges(typedDocWithUpToDateEntries._unwrap(),
persistedDocument);
if (fieldChanges.isEmpty()) {
return typedDocWithUpToDateEntries._unwrap();
}
Instant modifiedAtInstant = Instant.now();
String userName = changeInfoUtils.getUserName();
PChangeHistory<ChangeHistory> changeHistoryPointer =
ChangeHistory._pointer();
PChangeDetails<ChangeDetails> changeDetailsPointer =
ChangeDetails._pointer();
List<ChangeDetails> changeDetails = new ArrayList<>();
for (Change<FieldInstanceV2> change : fieldChanges) {
ChangeDetails changeDetail = ChangeDetails._empty()
._with(
changeDetailsPointer.changedProperty(),
ChangedProperty.valueOf(changeInfoUtils.getEntityName(change.pointer()))
)
._with(changeDetailsPointer.changeType(), mapChangeType(change))
._with(changeDetailsPointer.repetition(),
changeInfoUtils.getParentRepetition(change.pointer()));
changeDetails.add(changeDetail);
}
ChangeHistory currentChangeEntry = ChangeHistory._empty()
._with(changeHistoryPointer.modifiedAt(), modifiedAtInstant)
._with(changeHistoryPointer.modifiedBy(), userName)
._with(changeHistoryPointer.changeDetails(), changeDetails);
return typedDocWithUpToDateEntries
._withAppendedRepetition(CONTACT_CHANGE_HISTORY_TYPED_POINTER,
27

-- 27 of 28 --

currentChangeEntry)._unwrap();
}
}
The implementation is now complete. If you got stuck, you can check out the tag 2025.06-
ext5/backend/task-2-end to see how your code differs from the solution for the generic approach.
Testing
To make sure that the implementation with this approach works as expected, you can check it as
you did it for the first approach, see these instructions/suggestions. Since Max Mustermann might
no longer contain outdated modification information, you can use the contact Maria Perez Ortiz to
check the correspoding part of the implementation. If you want, you can compare the classes
ChangeInfoService and ChangeInfoServiceTyped to each other to identify the differences between
both approaches.
Conclusion
In this task you learned that in order to work with documents, it is necessary to have the following
information at hand:
• entity path for read and write actions, this is the information with which a non-repeatable
entity can be identified in a document.
• repetitions (or repetition indices) for read and write actions, absolutely necessary for
repeatable entities. Together with the entity path, a repeatable entity can be identified in a
document.
• value for read and write actions on fields.
You also learned the following:
• There are two approaches while working with documents.
• The use of the one approach does not exclude the use of the other.
• The typed accessor classes immensely improve the developer experience and offer an early
detection strategy of structural changes in the involved Document Models.
If you got stuck, you can just check out 2025.06-ext5/backend/task-2-intermediate or 2025.06-
ext5/backend/task-2-end (depending on the point you are stuck at) to see differences between both
implementations.
« Task 1: Document Access Task 3: Unit Testing »
28

-- 28 of 28 --

