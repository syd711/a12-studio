# overall dev_tutorial_backend_unit_testing

Task 3 - Unit Testing
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Backend > Introduction
before continuing here.
You can check out the tag 2025.06-ext5/backend/task-3-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/backend/task-3-end to see how
your code and models differ from the solution.
Use-Case
In the process of enhancing the CRM system, we are going to add unit tests for the task 1 in
Tutorials > Backend > Document Access and its address validation.
Therefore, we are going to introduce a general test structure, register the required dependencies,
and implement the actual test cases by using A12 documents as test data.
End Result
Upon finishing this task, you will know:
• How to set up a test environment that can be executed during the build process.
• How to load A12 documents and models into a test environment.
• How to write unit tests against our features and documents.
At the end, our project should be covered by unit tests and fulfill a code coverage of at least 80% for
the address validation classes and utilities.
1

-- 1 of 14 --

Step-by-Step Instructions
Already Provided Test Documents
The server/app/src/test/resources/models folder contains one Document Model Contact_DM to
retrieve model specific data, such as the repeatability of a group.
NOTE
When writing tests that require a Document Model, do not use Document Models in
modelling state, such as composed Document Models or Document Models with
includes. Instead, use runtime Document Models, which you can create using the
corresponding functionality in the SME.
Based on this Document Model, the server/app/src/test/resources/data folder contains three
documents for the different relevant test cases that we want to cover:
• ContactWithAddress.json: Contact containing basic personal information and a valid address.
• ContactWithInvalidAddresses.json: Contact containing multiple invalid addresses.
• ContactWithNonExistingAddress.json: Contact containing a fictive address, which OpenCage will
not have a result for.
The documents and model will be used in a later step as test input for unit testing the address
validation.
Setting up the Test Environment
Folder Structure
For our build tool to detect and execute unit tests, the server/app/ directory needs to be structured
in a specific way. Below is the exact folder structure that you need to create in server/app/:
src/
├── main/
│ └── ...
└── test/
2

-- 2 of 14 --

├── java/
│ └── com/mgmtp/a12/tutorial/server/
│ ├── BaseTest.java
│ ├── addressvalidation/
│ └── utils/
└── resources/
├── data/
└── models/
As you can see, the test folder is separated into:
• The java folder, which contains our test classes.
• The resources folder, which includes the documents and models used for testing.
As described in the previous section, the resources folder has already been created for you and
prefilled with the Document Model and corresponding documents.
Dependencies
For our testing, we will use JUnit5 as the test framework and Mockito to mock certain classes and
functionalities to decouple our implementation from used services. We will start by updating our
project’s dependencies. Therefore, we have to add the following libraries to our Gradle files.
File: settings.gradle
libs {
//...
version('junit', '6.0.1')
version('mockito', '5.20.0')
//...
library('junit-api', 'org.junit.jupiter', 'junit-jupiter-api').versionRef('junit')
library('junit-engine', 'org.junit.jupiter', 'junit-jupiter-
engine').versionRef('junit')
library('junit-platform-launcher', 'org.junit.platform', 'junit-platform-
launcher').versionRef('junit')
library('mockito', 'org.mockito', 'mockito-core').versionRef('mockito')
library('mockito-junit', 'org.mockito', 'mockito-junit-
jupiter').versionRef('mockito')
}
File: server/app/build.gradle
dependencies {
//...
// Test
testImplementation libs.junit.api
3

-- 3 of 14 --

testRuntimeOnly libs.junit.engine
testRuntimeOnly libs.junit.platform.launcher
testImplementation libs.mockito
testImplementation libs.mockito.junit
testImplementation libs.lombok
testAnnotationProcessor libs.lombok
}
//...
test {
useJUnitPlatform()
}
Implementing Test Classes
Adding a Document Model Resolver
We will use some services and classes from Kernel and Data Services to reduce the effort of writing
our test cases. To achieve this, we will work with the concrete models and documents that were
introduced in the previous section.
As part of this, we will implement an IDocumentModelResolver, an interface from Kernel, which
makes our Document Model Contact_DM available for the other services during test execution.
Let us take a look at the interface’s definition:
File: com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver.java
package com.mgmtp.a12.kernel.md.model.api.services;
import java.util.Optional;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.header.Header;
import lombok.NonNull;
/**
* Provides resolution of already expanded {@link IDocumentModel}s.
* <p>
* <b>Note:</b> The caller has to provide an implementation of this interface.
*/
public interface IDocumentModelResolver {
/**
* This method is used to retrieve {@link IDocumentModel}s by {@link
Header#getId()}.
*
* @param id
4

-- 4 of 14 --

* the {@link IDocumentModel} id. Never {@code null} or blank.
* @return the already expanded {@link IDocumentModel} with the given id.
*/
IDocumentModel getDocumentModelById(@NonNull String id);
/**
* This method is used to retrieve {@link IDocumentModelSearchService}s by
* {@link Header#getId()}.
* Providing an implementation of this method that returns an cached instance
leads to
* significantly
* better performance especially when deserializing more documents for the
model.<br>
* An instance of {@link IDocumentModelSearchService} can be created using
* {@code
DocumentModelServiceFactory#createDocumentModelSearchService(IDocumentModel
documentModel)}.
*
* @param documentModelId
* the {@link IDocumentModel} id. Never {@code null} or blank.
* @return the {@link IDocumentModelSearchService} for the given id. The default
implementation
* returns {@link Optional#empty()}, the instance is then created e.g. for
each document
* deserialization. Never returns {@code null}.
*/
default @NonNull Optional<IDocumentModelSearchService>
getDocumentModelSearchService(@NonNull String documentModelId) {
return Optional.empty();
}
}
You can find more information about the IDocumentModelResolver interface via the API
documentation in Kernel > Java Artifacts.
We will now implement the IDocumentModelResolver interface, which we will call
TestDocumentModelResolver.
The TestDocumentModelResolver is handled as a Singleton, since this shall be the single source of
truth handling all Document Models. It is a simple implementation containing a hash map
documentModels, which maps String Document Model ids to IDocumentModel instances. We also have
to provide two methods, one for retrieval of Document Models by ids and one to add Document
Models.
To add this implementation, you first need to create a new folder named utils in
test/java/com/mgmtp/a12/tutorial/server. You can then add the implementation of the
TestDocumentModelResolver to this folder, which you can find below:
▼ Click to see code
File:
server/app/src/test/java/com/mgmtp/a12/tutorial/server/utils/TestDocumentModelResolver.java
5

-- 5 of 14 --

package com.mgmtp.a12.tutorial.server.utils;
//...
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDocumentModelResolver implements IDocumentModelResolver {
private static TestDocumentModelResolver instance;
private Map<String, IDocumentModel> documentModels = new HashMap<>();
public static TestDocumentModelResolver getInstance() {
if (instance == null) {
instance = new TestDocumentModelResolver();
}
return instance;
}
@Override
public IDocumentModel getDocumentModelById(String id) {
IDocumentModel documentModel = documentModels.get(id);
if (documentModel == null) {
throw new IllegalArgumentException("Document Model with the id '" + id +
"' has not been registered yet.");
}
return documentModel;
}
public void addDocumentModel(IDocumentModel documentModel) {
documentModels.put(documentModel.getHeader().getId(), documentModel);
}
}
Creating a Base Test Class
There might be more new features coming, for which we will need to add new unit tests in the
future. Therefore, we will need to have a solid test structure that is easy to extend. To achieve this,
we will create a base class from which our specific unit tests will inherit.
To that end, we will implement BaseTest.java, which will handle the conversion and loading of the
aforementioned documents and models, as well as providing utility functions that can be used
across multiple test classes.
This BaseTest class will offer the following methods:
• protected void setUp(String testDocumentModelPath) will set an IDocumentModel instance, so that
we can access it in our specific test classes.
• private String readFileAsString(String pathToFile) will retrieve the content of a file at a
specified path and return it as a string.
6

-- 6 of 14 --

• protected DocumentV2 convertJsonToDocument(String pathToJson) will retrieve a path to a JSON
file and return a DocumentV2 instance.
• protected DocumentDeserializationConfig createJsonReaderConfig() will create the configuration
for document deserialization (necessary for document conversion).
• protected Object getFieldValue(DocumentV2 document, DocumentPointer pointer) will retrieve
the field value for a given DocumentPointer.
You can now create BaseTest.java in server/app/src/test/java/com/mgmtp/a12/tutorial/server/, and
add the following code:
▼ Click to see code
File: server/app/src/test/java/com/mgmtp/a12/tutorial/server/BaseTest.java
package com.mgmtp.a12.tutorial.server;
//...
@Slf4j
public class BaseTest {
protected IDocumentModel documentModel;
protected IDocumentModelSerializer documentModelSerializer =
new DocumentModelServiceFactory().createDocumentModelSerializer();
protected TestDocumentModelResolver documentModelResolver =
TestDocumentModelResolver.getInstance();
protected DocumentServiceFactory documentServiceFactory = new
DocumentServiceFactory(documentModelResolver);
protected IDocumentV2Serializer documentSerializer =
documentServiceFactory.createDocumentV2Serializer();
protected IDocumentModelSearchService documentModelSearchService;
protected final String basePath = "src/test/resources/data/";
protected void setUp(String testDocumentModelPath) throws IOException {
try (StringReader stringReader = new
StringReader(readFileAsString(testDocumentModelPath))) {
log.info("Deserializing Document Model: {}", testDocumentModelPath);
documentModel = documentModelSerializer.deserialize(stringReader);
} catch (IOException e) {
throw e;
}
documentModelResolver.addDocumentModel(documentModel);
7

-- 7 of 14 --

documentModelSearchService = new
DocumentModelServiceFactory().createDocumentModelSearchService(documentModel);
}
private String readFileAsString(String pathToFile) throws IOException {
try {
log.info("Reading file: {}", pathToFile);
return new String(Files.readAllBytes(Paths.get(pathToFile)));
} catch (IOException e) {
throw e;
}
}
protected DocumentV2 convertJsonToDocument(String pathToJson) {
try (StringReader stringReader = new
StringReader(readFileAsString(pathToJson))) {
log.info("Deserializing document: {}", pathToJson);
return documentSerializer.deserializeV2(stringReader,
CONTACT_MODEL_NAME,
createJsonReaderConfig(),
rankedNotification -> {
});
} catch (IOException e) {
throw new UncheckedIOException(e);
}
}
protected DocumentDeserializationConfig createJsonReaderConfig() {
return
DocumentDeserializationConfig.builder().format(DocumentSerializationConfig.Format.JS
ON).build();
}
protected Object getFieldValue(DocumentV2 document, DocumentPointer pointer) {
return Optional.ofNullable(document.field(pointer).value()).orElse("");
}
}
NOTE
As mentioned in previous sections, there are several ways to implement features
such as setting up our test environment and writing test cases. The approach
outlined here is just one example of how to create tests for A12 document-based
services.
Below you can find an explanation of the variables and services used in the previous code snippet:
• IDocumentModel documentModel is used in our test classes to access model specifics.
• IDocumentModelSerializer documentModelSerializer is used to deserialize the JSON model to an
8

-- 8 of 14 --

IDocumentModel.
• TestDocumentModelResolver documentModelResolver is necessary to retrieve the corresponding
Document Model for the services in our test environment.
• DocumentServiceFactory documentServiceFactory is used to create the IDocumentV2Serializer
instance.
• IDocumentV2Serializer documentSerializer is used to deserialize the JSON document to a
DocumentV2.
• IDocumentModelSearchService documentModelSearchService is used to search for model specifics
like repeatability.
• String basePath defines the location of the JSON documents in our test environment.
We also introduced logging to easier track the process of each test later. Especially in the case of a
test failing, this will help us to more easily navigate and identify issues.
With this setup, we have established a basic test structure with some utility functions. In the future,
we are able to use the BaseTest class to derive test classes for many document-based functionalities,
allowing us to expand the test environment.
Each test class will simply extend the BaseTest class and implement its own testing logic.
Creating Test for Address Validation
Everything is now set up for you to start implementing a test for the address validation. To do so,
create a new class AddressValidatorTest.java in
server/app/src/test/java/com/mgmtp/a12/tutorial/server/addressvalidation and add the following
code to it:
File:
server/app/src/test/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressValidatorTest.j
ava
package com.mgmtp.a12.tutorial.server.addressvalidation;
//...
@ExtendWith(MockitoExtension.class)
public class AddressValidatorTest extends BaseTest {
private AddressValidator addressValidator;
private DocumentV2 documentWithAddress;
@Mock
private JOpenCageGeocoder jOpenCageGeocoder;
@SneakyThrows
@BeforeEach
public void setUp() {
super.setUp("src/test/resources/models/" + CONTACT_MODEL_NAME + ".json");
9

-- 9 of 14 --

documentWithAddress = convertJsonToDocument(basePath +
"ContactWithAddress.json");
addressValidator = new AddressValidator(jOpenCageGeocoder);
}
}
Now, you can implement the rest of this unit test class.
Your task:
• Implement a method for each of the following scenarios to test whether the validateAddress
method correctly throws an InvalidAddressException:
◦ An address that does not exist.
◦ A set of multiple addresses where one is invalid.
◦ An address containing a country with a digit.
◦ An address containing a city with a digit.
◦ An address containing a country starting with a special character.
◦ An address containing a street starting with a special character.
◦ An address containing an invalid zip code.
• Write a unit test for a valid address.
• Hints:
◦ To implement these unit tests, use the variables and methods we have defined above.
◦ In this project we are working with JUnit5, keep that in mind when writing the test methods.
◦ Use the documents that are provided in test/resources/data.
▼ Click to see solution
File:
server/app/src/test/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressValidatorTes
t.java
package com.mgmtp.a12.tutorial.server.addressvalidation;
//...
@ExtendWith(MockitoExtension.class)
public class AddressValidatorTest extends BaseTest {
private AddressValidator addressValidator;
private DocumentV2 documentWithAddress;
@Mock
private JOpenCageGeocoder jOpenCageGeocoder;
10

-- 10 of 14 --

@SneakyThrows
@BeforeEach
public void setUp() {
super.setUp("src/test/resources/models/" + CONTACT_MODEL_NAME + ".json");
documentWithAddress = convertJsonToDocument(basePath +
"ContactWithAddress.json");
addressValidator = new AddressValidator(jOpenCageGeocoder);
}
@Test
public void throwExceptionForNonExistingAddress() throws IOException {
DocumentV2 documentWithInvalidAddress = convertJsonToDocument(basePath +
"ContactWithNonExistingAddress.json");
JOpenCageResponse jOpenCageResponse = mock(JOpenCageResponse.class);
when(jOpenCageResponse.getResults()).thenReturn(Collections.emptyList());
when(jOpenCageGeocoder.forward(any())).thenReturn(jOpenCageResponse);
assertThrows(InvalidAddressException.class, () -> {
addressValidator.validateAddress(documentWithInvalidAddress);
});
}
@Test
public void throwExceptionForMultipleAddressesAndOneInvalid() throws IOException
{
DocumentV2 documentWithInvalidAddress = convertJsonToDocument(basePath +
"ContactWithInvalidAddresses.json");
assertThrows(InvalidAddressException.class, () -> {
addressValidator.validateAddress(documentWithInvalidAddress);
});
}
@Test
public void throwExceptionForNumberInCountry() {
DocumentPointer pointer = DocumentPointer.of(CONTACT_ADDRESS_PATH +
"/Country");
DocumentV2 newDocumentWithAddress = documentWithAddress.withFieldValue(
pointer,
getFieldValue(documentWithAddress, pointer) + "1"
);
assertThrows(InvalidAddressException.class, () -> {
addressValidator.validateAddress(newDocumentWithAddress);
});
}
@Test
11

-- 11 of 14 --

public void throwExceptionForNumberInCity() {
DocumentPointer pointer = DocumentPointer.of(CONTACT_ADDRESS_PATH +
"/City");
DocumentV2 newDocumentWithAddress = documentWithAddress.withFieldValue(
pointer,
getFieldValue(documentWithAddress, pointer) + "1"
);
assertThrows(InvalidAddressException.class, () -> {
addressValidator.validateAddress(newDocumentWithAddress);
});
}
@Test
public void throwExceptionForInvalidStartingLetterInCountry() {
DocumentPointer pointer = DocumentPointer.of(CONTACT_ADDRESS_PATH +
"/Country");
DocumentV2 newDocumentWithAddress = documentWithAddress.withFieldValue(
pointer,
"#" + getFieldValue(documentWithAddress, pointer)
);
assertThrows(InvalidAddressException.class, () -> {
addressValidator.validateAddress(newDocumentWithAddress);
});
}
@Test
public void throwExceptionForInvalidStartingLetterInStreet() {
DocumentPointer pointer = DocumentPointer.of(CONTACT_ADDRESS_PATH +
"/Street");
DocumentV2 newDocumentWithAddress = documentWithAddress.withFieldValue(
pointer,
"#" + getFieldValue(documentWithAddress, pointer)
);
assertThrows(InvalidAddressException.class, () -> {
addressValidator.validateAddress(newDocumentWithAddress);
});
}
@Test
public void throwExceptionForInvalidZip() {
DocumentPointer pointer = DocumentPointer.of(CONTACT_ADDRESS_PATH + "/Zip");
DocumentV2 newDocumentWithAddress =
documentWithAddress.withFieldValue(pointer, "1234567");
assertThrows(InvalidAddressException.class, () -> {
addressValidator.validateAddress(newDocumentWithAddress);
});
}
@Test
public void validAddress() {
JOpenCageResponse jOpenCageResponse = mock(JOpenCageResponse.class);
12

-- 12 of 14 --

JOpenCageResult jOpenCageResult = mock(JOpenCageResult.class);
when(jOpenCageResponse.getResults()).thenReturn(List.of(jOpenCageResult));
when(jOpenCageGeocoder.forward(any())).thenReturn(jOpenCageResponse);
addressValidator.validateAddress(documentWithAddress);
}
}
Running the Unit Tests
NOTE In this tutorial, we aim to achieve at least 80% line coverage after having run all
implemented tests.
If you are using IntelliJ, you can execute the built-in code coverage tool in the following way:
After running the code coverage tool, your result should look similar to this:
We even surpassed the goal of 80% by reaching 93% line coverage.
TIP
If you run into problems regarding outdated gradle.lockfile, you can execute either
gradle :server:app:dependencies --write-locks or gradle build --write-locks to
upgrade the affected files.
13

-- 13 of 14 --

TIP
You might run into Checkstyle compile errors. If that is the case, you can either run
the build with gradle build -P skipCheckstyle=true, set the skipCheckstyle property
in gradle.properties to true, or fix the Checkstyle errors.
Conclusion
In this task, you learned how to set up a general test structure for implementing unit tests for A12-
based services. You also saw how to provide complex input documents to ensure that all cases of
our custom code are covered during testing.
If something does not seem right or you got stuck at any point, you can check out 2025.06-
ext5/backend/task-3-end to see the differences between both implementations.
« Task 2: Document Manipulation Task 4: Custom Endpoint »
14

-- 14 of 14 --

