# overall dev_tutorial_backend_document_access

Task 1 - Document Access
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Backend > Introduction
before continuing here.
You can check out the tag 2025.06-ext5/backend/task-1-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/backend/task-1-end to see how
your code and models differ from the solution.
Use-Case
As we are extending the CRM system, we are going to add an address validation for our contacts in
this task.
Therefore, we will hook into the document creation and update events to validate the added
addresses. To verify if the address actually exists, we are going to register and call the Geocoding
API, which will take the contacts' addresses as input.
End Result
Upon finishing this task, you will know:
• How to add additional application properties in the server.
• How to add external libraries to a project.
• How to hook into lifecycle events of documents.
• How to access an A12 document and read its content.
• How to add custom logic for handling address validation.
At the end, our application should display errors after the validation of the inputs as follows:
1

-- 1 of 23 --

And it should also display an error like the following in the case that an invalid address was
entered:
2

-- 2 of 23 --

NOTE
The error messages returned by the server for address validation are only available
in English. If you select another locale, the error message box will be empty. This is
because localization of server-side error messages is only possible with static
strings, but the address validation error contains dynamic text messages (the exact
issues with the address).
Step-by-Step Instructions
Understanding Documents
A document is a concrete instance of a Document Model. It contains the actual data that, for
example, a user has entered and saved in a form. In contrast, the Document Model is an abstraction
over all documents, describing one certain object or entity in the business domain.
Example
• We have a Document Model called Person with the fields: First name, last name and date of
birth.
• One specific document for this Document Model could be represented by: Max, Mustermann
3

-- 3 of 23 --

and 20.11.1981.
Accessing a Document
There are different ways to retrieve a document in the backend. But first, we will take a look at the
relevant classes and their relation to each other.
Relevant classes and interfaces:
• IDocumentModel: Defines the document’s structure.
• DocumentV2: Concrete instance of an IDocumentModel.
• DataServicesDocument: A wrapper containing a DocumentV2 instance and a
DataServicesDocumentMetadata instance (with information like document reference, DM
reference, etc.).
• IElement: Abstract type for all hierarchical elements of an IDocumentModel.
• IField: Represents the leaf in the IElement-hierarchy and describes an element containing a
value.
• FieldInstanceV2: Concrete instance of an IField, it contains the value of the field.
• IGroup: Represents a parent node in the IElement-hierarchy and can contain fields (IField) and
other groups (IGroup).
• RepetitionsV2: Collection of the concrete repetitions of an IGroup. If the group is not repeatable,
it contains only one repetition.
• GroupInstanceV2: Concrete instance of an IGroup, this means, it is a single repetition which can
contain instances of fields (see FieldInstanceV2) and instances of its subgroups (see
RepetitionsV2).
• DocumentPointer: Reference to a concrete entity instance in a document. It contains the
information regarding the path and the repetitions of the entity instance.
Relevant services:
• IDmAwareDocService: Provides functionality for DocumentV2 for which the information in the
Document Model is needed, e.g. get entity instances via the id stored in the Document Model.
• IDocumentModelResolver: Provides functionality for retrieving an IDocumentModel by its id.
• DocumentServiceFactory: Provides functionality to create services related to documents, e.g.
serialization, search and creation of documents.
• IDocumentRepository: Provides functionality for loading a DataServicesDocument by using the
document reference. This docRef is a unique identifier of documents, containing the Document
Model name and the document id. Be aware that this access is unsecured.
• And many more that we will take a look at during this task.
Also, have a look at Data Services > Calling Data Services functionality. These cover the loading,
persisting and some utility in the context of documents.
4

-- 4 of 23 --

Using Event Listeners
It is possible to access a document by reacting to a specific event. The following events are available
for use in the context of documents:
• DocumentBeforeCreateEvent
• DocumentAfterCreateEvent
• DocumentBeforeUpdateEvent
• DocumentAfterUpdateEvent
• DocumentBeforeDeleteEvent
• DocumentAfterDeleteEvent
• DocumentAfterLoadEvent
• DocumentBeforeIndexEvent
• DocumentBeforeRepositorySaveEvent
• DocumentAfterRepositoryLoadEvent
• DocumentAfterControllerLoadEvent
You can find a complete overview of all events provided by Data Services in Data Services > Data
Services Events. In the following code snippet, you can then see how to register a new event listener
for handling such events:
//...
@Component
@RequiredArgsConstructor
public class SampleEventListener {
private SampleService sampleService;
/**
* Before the document is created this code will be executed.
* event.getCreatedDocument() can be changed in place and Data Services will use
the new version of document in further processing.
*/
@CommonDataServicesEventListener
public void beforeCreateListener(DocumentBeforeCreateEvent event) {
// Put your custom logic here
sampleService.doSomething(event.getCreatedDocument());
}
/**
* Before the document is updated this code will be executed.
*/
@CommonDataServicesEventListener
public void beforeUpdateListener(DocumentBeforeUpdateEvent event) {
5

-- 5 of 23 --

// Put your custom logic here
sampleService.doSomething(event.getUpdatedDocument());
}
}
Example Address Validation
First, we will take a look at the current state of the application. It is possible to create contacts and
add up to five different address entries. But, some users may enter addresses like the following:
• Fictional location:
◦ Webfoot Walk 1313, Duckburg 12345, Calisota
• Invalid location:
◦ V0rd3re Cram3ergasse 11, N4rnb3rg 90478910, #Deutschland
To prevent such input from being stored, a useful and meaningful address validation should be in
place. We will accomplish this by first using internal validation in the backend to filter out invalid
locations.
NOTE
There are multiple ways to achieve this internal validation. For example, the A12
validation language could also be used to add rules to our models for the same
behavior. However, for demonstration purposes on how to customize server-side
behavior, we will implement the validation on our own.
If an entry is valid according to our defined rules, we will then send a request to a Geocoding API.
In our case, we will use OpenCage to check whether the address corresponds to a valid location. If it
does, the address will be added to the contact. Otherwise, an error will be displayed, indicating that
the provided address is not valid. The usage of the validation with OpenCage will be configurable
via the application-dev.properties.
Developing Address Validation
In this section, we will develop the address validation. To do this, the following steps are necessary:
• Adding a new property for the address validation.
• Creating a new bean that contains the address validation logic.
• Creating a new bean for event listening.
We will include our backend code changes in the
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation. We will also use the
server/app/src/main/resources folder to extend the application’s configuration.
Already Provided Classes, Configuration, Interfaces and Utilities
Before starting with the implementation for the address validation, we will have a quick look at the
included classes, interfaces and utilities in the start tag.
6

-- 6 of 23 --

OpenCage Dependency
The OpenCage library is provided and added in the settings.gradle and the
server/app/build.gradle. This allows us to use the necessary functionalities of the Geocoding API.
File: settings.gradle
libs {
//...
version('opencage', '2.2.2')
//...
library('jopencage', 'com.opencagedata', 'jopencage').versionRef('opencage')
}
File: server/app/build.gradle
dependencies {
//...
// OpenCage
implementation libs.jopencage
}
Constants
This class provides static string variables for referencing the name and paths of our Document
Model. These field and group paths will be used to retrieve values from the documents.
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/utils/Constants.java
package com.mgmtp.a12.tutorial.server.utils;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
public static final String CONTACT_MODEL_NAME = "Contact_DM";
public static final String CONTACT_PERSONALDATA_EMAIL_PATH =
"/Contact/PersonalData/Email";
public static final String CONTACT_PERSONALDATA_LASTNAME_PATH =
"/Contact/PersonalData/LastName";
public static final String CONTACT_PHONES_PATH = "/Contact/Phones";
public static final String CONTACT_ADDRESS_PATH = "/Contact/Address";
7

-- 7 of 23 --

public static final String CONTACT_CHANGED_PROPERTY_PATH =
"/Contact/HistoryInfo/ChangeHistory/ChangeDetails/ChangedProperty";
public static final DocumentPointer CONTACT_POINTER =
DocumentPointer.of("Contact");
public static final DocumentPointer CONTACT_HISTORY_INFO_POINTER =
CONTACT_POINTER.withAppended("HistoryInfo", 1);
public static final DocumentPointer CONTACT_CREATED_AT_POINTER =
CONTACT_HISTORY_INFO_POINTER.withAppended("CreatedAt", 1);
public static final DocumentPointer CONTACT_CREATED_BY_POINTER =
CONTACT_HISTORY_INFO_POINTER.withAppended("CreatedBy", 1);
public static final DocumentPointer CONTACT_CHANGE_HISTORY_POINTER =
CONTACT_HISTORY_INFO_POINTER.withAppended("ChangeHistory", 0);
public static final String CONTACT_MODIFIED_AT_FIELD_NAME = "ModifiedAt";
public static final String CONTACT_MODIFIED_AT_POINTER_PATTERN =
"/Contact/HistoryInfo/ChangeHistory[%s]/ModifiedAt";
public static final String CONTACT_MODIFIED_BY_POINTER_PATTERN =
"/Contact/HistoryInfo/ChangeHistory[%s]/ModifiedBy";
public static final String CONTACT_CHANGED_PROPERTY_POINTER_PATTERN =
"/Contact/HistoryInfo/ChangeHistory[%s]/ChangeDetails[%s]/ChangedProperty";
public static final String CONTACT_CHANGE_TYPE_POINTER_PATTERN =
"/Contact/HistoryInfo/ChangeHistory[%s]/ChangeDetails[%s]/ChangeType";
public static final String CONTACT_CHANGE_REPETITION_POINTER_PATTERN =
"/Contact/HistoryInfo/ChangeHistory[%s]/ChangeDetails[%s]/Repetition";
}
Problem
This interface provides the structure for handling upcoming problems in our address validation.
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/utils/Problem.java
package com.mgmtp.a12.tutorial.server.utils;
public interface Problem {
String getMessage();
}
AddressProblem
This class implements the Problem.java interface and will be used to store and handle errors as well
as validation problems during the address validation.
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressProblem.java
8

-- 8 of 23 --

package com.mgmtp.a12.tutorial.server.addressvalidation;
import com.mgmtp.a12.tutorial.server.utils.Problem;
public class AddressProblem implements Problem {
private final String message;
public AddressProblem(String message) {
this.message = message;
}
@Override
public String getMessage() {
return this.message;
}
}
InvalidAddressException
This exception will be used to forward errors during the validation to our application and frontend.
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation/InvalidAddressExceptio
n.java
package com.mgmtp.a12.tutorial.server.addressvalidation;
public class InvalidAddressException extends RuntimeException {
public InvalidAddressException(String message) {
super(message);
}
}
AddressConfiguration
This configuration class defines a singleton bean of type JOpenCageGeocoder, enabling easy injection
of this bean into other services that require geocoding functionality.
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressConfiguration.j
ava
package com.mgmtp.a12.tutorial.server.addressvalidation;
import com.opencagedata.jopencage.JOpenCageGeocoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
9

-- 9 of 23 --

import org.springframework.context.annotation.Configuration;
@Configuration
public class AddressConfiguration {
@Value("${mgmtp.a12.tutorial.server.geocoder.api-key}")
private String geocoderApiKey;
@Bean
public JOpenCageGeocoder getJOpenCageGeocoder() {
return new JOpenCageGeocoder(geocoderApiKey);
}
}
The JOpenCageGeocoder is the service class from OpenCage that handles tasks such as forwarding our
request and providing the response from the Geocoding API.
NOTE This setup would make it also easier to deploy to different environments in the later
steps of a project lifecycle.
AddressValidator
This class will contain the main logic for handling the address validation of our documents. At the
moment, it only contains the base for the implementation, including:
• The required bean for the validation: JOpenCageGeocoder
• The method signature for our entry function validateAddress.
• An inner class Address for storing all address related attributes in one object.
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressValidator.java
package com.mgmtp.a12.tutorial.server.addressvalidation;
//...
@Component
@RequiredArgsConstructor
public class AddressValidator {
private final JOpenCageGeocoder jOpenCageGeocoder;
/**
* Checker for input document that has to determine if document is of `Contact_DM`
model name. In case of true,
* validation should validate address by calling method implementing validation
logic.
* If validation fails custom exception is thrown affecting the response.
* <p>
10

-- 10 of 23 --

* If internal validation goes through, OpenCage Geocoding API is used to search
for the entered location. Then the
* response will be checked. An exception is thrown when there has been no
address.
*/
public void validateAddress(DocumentV2 document) {
// Put your code here ...
}
@Data
@AllArgsConstructor
private static class Address {
private String street;
private String houseNumber;
private String zip;
private String city;
private String country;
@Override
public String toString() {
return "{%s %s, %s %s, %s}".formatted(street, houseNumber, zip, city,
country);
}
}
}
Creating an OpenCage API Key
To use the OpenCage Geocoding API, it is necessary to create an API key. Therefore, you have to
create a new account on OpenCage. Once you have logged in, you will find the API key under the
"Geocoding API" tab.
Adding Properties
Next, we need to ensure that the following properties are defined and configured correctly:
• mgmtp.a12.tutorial.server.geocoder.api-key: Property for configuring the API key. It is already
included in the start tag, and therefore you only need to replace the placeholder YOUR-API-KEY
through your previously generated API key for OpenCage.
• mgmtp.a12.tutorial.server.addressValidation: It is not included in the start tag. It is a new
property to specify whether OpenCage should be used as an additional validation tool in our
project.
File: server/app/src/main/resources/config/application-dev.properties
#...
# JOpenCageGeocoder API key
mgmtp.a12.tutorial.server.geocoder.api-key=YOUR-API-KEY
11

-- 11 of 23 --

# Flag for enabling the address validation
mgmtp.a12.tutorial.server.addressValidation=true
Creating Bean for Address Validation
We will now begin the implementation of the address validation, as the necessary preparation and
configuration are already done.
To accomplish this, we first need to extend the AddressValidator. Afterward, we are going to hook
into the Data Services document lifecycle events to introduce our address validation logic.
Retrieving Data From a Document
We will start by implementing some helper methods to retrieve data from the document.
The first method will be for the retrieval of a string value based on a concrete group instance and a
field name. The method GroupInstanceV2#directField(String) can be used to retrieve a field
instance by its name. Be aware that the method can return null if the field is not present in the
group instance.
Having the field instance, we can then retrieve the value by calling FieldInstanceV2#value(). After
making sure that the value is not null, we can convert it to string. If the value is null, we return an
empty string.
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressValidator.java
package com.mgmtp.a12.tutorial.server.addressvalidation;
//...
@Component
@RequiredArgsConstructor
public class AddressValidator {
//...
/**
* Method for retrieval of the string values in a document.
*/
private String resolveString(GroupInstanceV2 groupInstance, String fieldName) {
return Optional.ofNullable(groupInstance.directField(fieldName))
.map(field -> field.value().toString())
.orElse("");
}
}
12

-- 12 of 23 --

Getting All Addresses in Our Document
In the next step, we will write a function that takes the DocumentV2 instance and returns all
addresses linked to it as a list. An instance of DocumentV2 reflects the tree-like structure of the
Document Model, and we can have access to the concrete entity instances via the corresponding
path. Having this in mind and knowing the path of the address group, we can retrieve all addresses
in the document.
Having the addresses, we iterate over them, and we can then use our helper method for resolving a
string to get the various field values that are present in each instance of the address group.
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressValidator.java
package com.mgmtp.a12.tutorial.server.addressvalidation;
//...
@Component
@RequiredArgsConstructor
public class AddressValidator {
//...
/**
* A helper method to retrieve all addresses of a document and returns these as a
list.
*/
private List<Address> getAddressesForDocument(DocumentV2 document) {
List<Address> addresses = new ArrayList<>();
RepetitionsV2 addressGroupInstances =
document.groupAllRepetitions(CONTACT_ADDRESS_PATH + "[0]");
addressGroupInstances.stream()
.forEach(addressGroupInstance -> {
String street = resolveString(addressGroupInstance, "Street");
String houseNumber = resolveString(addressGroupInstance,
"Housenumber");
String zip = resolveString(addressGroupInstance, "Zip");
String city = resolveString(addressGroupInstance, "City");
String country = resolveString(addressGroupInstance, "Country");
addresses.add(new Address(street, houseNumber, zip, city,
country));
});
return addresses;
}
}
13

-- 13 of 23 --

Implementing an Address Check
Now that we have everything in place, we can start implementing the internal validation. This
method will run checks on the provided addresses before the request is sent to the OpenCage API.
By doing this, we can avoid sending requests for addresses that we already know are invalid.
NOTE
For proper error handling during validation, we will use a List<AddressProblem>
instance. Each method will take this list, and if an error occurs, a new
AddressProblem will be added to it.
If errors have been added to the list during processing, an exception with all the
problem messages will be thrown and displayed in the form. We will check the
List<AddressProblem> instance at the end of the processing to ensure that all errors
linked to invalid addresses are collected.
Your task:
Add the following methods to the AddressValidator.java class to provide helper methods that we
will use in the next step.
1. checkForNumber which takes a string value and the list of problems. It returns whether the value
contains a number.
2. validateStartingLetter which takes a string value and the list of problems. It returns whether
the value starts with a letter.
3. validateZip which takes a string value and the list of problems. It returns whether the value is a
valid zip code.
4. checkAddress which takes an Address and the list of problems. It uses the aforementioned
methods and returns a boolean depending on the result.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressValidator.ja
va
package com.mgmtp.a12.tutorial.server.addressvalidation;
//...
@Component
@RequiredArgsConstructor
public class AddressValidator {
//...
/**
* Method for executing address validation.
*/
private boolean checkAddress(Address address, List<AddressProblem> problems) {
return checkForNumber(address.getCity(), problems)
& checkForNumber(address.getCountry(), problems)
& validateStartingLetter("Street", address.getStreet(), problems)
14

-- 14 of 23 --

& validateStartingLetter("Country", address.getCountry(), problems)
& validateZip(address.getZip(), problems);
}
/**
* Validation to prevent user intentionally putting number in city or country
field
* <p>
* Valid case: "London", "Macedonia"
* Invalid case: "L0nd0n", "Mac3d0nia"
*/
private boolean checkForNumber(String value, List<AddressProblem> problems) {
if (Pattern.compile("\\d").matcher(value).find()) {
problems.add(new AddressProblem("Location: '%s' contains a forbidden
number!".formatted(value)));
return false;
}
return true;
}
/**
* Validation to prevent user using words starting with special character
* <p>
* Valid case: "Poland", "Czechia"
* Invalid case: "-Belgium", "%England"
*/
private boolean validateStartingLetter(String fieldName, String value,
List<AddressProblem> problems) {
if (!value.isEmpty() && !Character.isLetter(value.charAt(0))) {
problems.add(
new AddressProblem(
"%s: '%s' starts with forbidden letter:
'%s'!".formatted(fieldName, value, value.charAt(0)))
);
return false;
}
return true;
}
/**
* Validation for correct ZIP code format
* <p>
* Valid case: "123 456", "123-456", "123456"
* Invalid case: "1234 4567", "Eng1and"
*/
private boolean validateZip(String value, List<AddressProblem> problems) {
if (!Pattern.compile("^\\w{2,3}(-|\\s)?(\\w{2,3})?$").matcher(value).find())
{
problems.add(new AddressProblem("Invalid ZIP code: '%s'
provided!".formatted(value)));
return false;
15

-- 15 of 23 --

}
return true;
}
}
Putting Everything Together
All of our helper methods are ready, so we can start implementing our main method
validateAddress and put everything together.
Your task:
This method shall perform the following steps:
1. Check if the input document is a Contact_DM model.
2. Create a list of AddressProblem in which we will store all errors that occur during the validation.
3. Iterate over the list of all addresses in the document and call the checkAddress function for each
one.
4. For each address, create a JOpenCageForwardRequest with the query being in this format: <street
houseNumber, zip, city, country>.
5. Configure the request to use a minConfidence of 10, set noDedupe to false and use the
JOpenCageGeocoder instance to forward the request.
◦ The confidence relates to how accurate the location response is from OpenCage.
◦ noDedupe means that if set to false, redundant data will be eliminated from the results list;
hence, the term of deduplication is used.
6. Forward the request with the help of JOpenCageGeocoder and store it in a JOpenCageResponse
instance.
7. If the response is not null and the result is empty, add a new AddressProblem to our list of
problems.
8. Check if the problems list contains any entries. If so, throw an InvalidAddressException
containing all the error messages.
NOTE You can find more information on the API and a quick start guide in the OpenCage
documentation.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressValidator.ja
va
package com.mgmtp.a12.tutorial.server.addressvalidation;
//...
@Component
@RequiredArgsConstructor
16

-- 16 of 23 --

public class AddressValidator {
//...
/**
* Checker for input document that has to determine if document is of
`Contact_DM` model name. In case of true,
* validation should validate address by calling method implementing validation
logic.
* If validation fails custom exception is thrown affecting the response.
* <p>
* If internal validation goes through, OpenCage Geocoding API is used to search
for the entered location. Then the
* response will be checked. An exception is thrown when there has been no
address.
*/
public void validateAddress(DocumentV2 document) {
if (!CONTACT_MODEL_NAME.equals(document.getDocumentModelId())) {
return;
}
List<AddressProblem> problems = new ArrayList<>();
List<Address> addresses = getAddressesForDocument(document);
addresses.stream()
.filter(address -> checkAddress(address, problems))
.filter(address -> {
JOpenCageForwardRequest request =
createJOpenCageForwardRequest(address);
JOpenCageResponse response = jOpenCageGeocoder.forward(request);
return response != null && response.getResults().isEmpty();
})
.forEach(address -> problems.add(
new AddressProblem("There is no result for the provided
address: '%s'".formatted(address))
));
// Check for problems
if (!problems.isEmpty()) {
String errorMsg = problems.stream()
.map(AddressProblem::getMessage)
.collect(Collectors.joining("] --- [", "[", "]"));
throw new InvalidAddressException(errorMsg);
}
}
/**
* Creates a JOpenCageForwardRequest using the given address details.
*
* @param address the Address object containing the details needed for the
forward request
17

-- 17 of 23 --

* @return a configured JOpenCageForwardRequest object
*/
private JOpenCageForwardRequest createJOpenCageForwardRequest(Address address) {
JOpenCageForwardRequest request = new JOpenCageForwardRequest(
address.getStreet() + " " + address.getHouseNumber(),
address.getZip(),
address.getCity(),
address.getCountry());
// Confidence represents how accurate the return results should be regarding
bounding box size:
// 10 = < 0.25km distance between southwest and northeast side of box
// 1 = >= 25km distance between southwest and northeast side of box
request.setMinConfidence(10);
request.setNoDedupe(false);
return request;
}
//...
}
Creating Bean for Event Listening
To be able to react on a specific event, it is necessary for us to implement some event listener
methods. To do this, we will create a new bean to call our address validation which listens on the
following events:
• DocumentBeforeCreateEvent
• DocumentBeforeUpdateEvent
Your task:
1. Create a new bean called AddressValidationListener that uses the AddressValidator in
addressvalidation:
2. Use the @ConditionalOnProperty annotation with our new property
mgmtp.a12.tutorial.server.addressValidation.
3. Implement two event listeners, one for DocumentBeforeCreateEvent and one for
DocumentBeforeUpdateEvent.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/addressvalidation/AddressValidationLi
stener.java
package com.mgmtp.a12.tutorial.server.addressvalidation;
//...
/**
* Class that represents the collection of event listeners utilizing mocked address
18

-- 18 of 23 --

validators
* or 3rd party address validators.
* The goal of this class is to listen for event before creating or modifying
documents of model type ContactModel.
* In case of invalid document, e.g. missing field, custom exception will be thrown.
* <p>
* Used Document's events:
* - DocumentBeforeCreateEvent
* - DocumentBeforeUpdateEvent
* <p>
* For more information about published events by Data Services have a look at the
* <a href="https://docs.geta12.com">getA12 documentation</a> ("Development >
Components > Data Services", section
* "Java API > Extending the Server > Events > Data Services Events").
*/
@Component
@ConditionalOnProperty(prefix = "mgmtp.a12.tutorial.server", value =
"addressValidation", havingValue = "true")
@RequiredArgsConstructor
public class AddressValidationListener {
private final AddressValidator addressValidator;
/**
* Before the document is created the address validation will be executed from
the following listener
*/
@CommonDataServicesEventListener
public void beforeCreateListener(DocumentBeforeCreateEvent event) {
addressValidator.validateAddress(event.getCreatedDocument());
}
/**
* Before the document is updated the address validation will be executed from
the following listener
*/
@CommonDataServicesEventListener
public void beforeUpdateListener(DocumentBeforeUpdateEvent event) {
addressValidator.validateAddress(event.getUpdatedDocument());
}
}
NOTE In the Data Services repo, you can also find a similar example for address
validation, see AddressValidator.java.
Running the Application
Now that everything is set up, we can run our application. Next, you can test the changes that we
19

-- 19 of 23 --

did in action. Try creating a contact with some valid addresses and then check how the application
behaves if you enter an address that does not fulfill the requirements of our address validation
rules.
You can find instructions on how to build and start the application in Tutorials > General
Information > Setup. Since we only modified the server/app module, you do not have to build the
entire application; you can simply execute the following command:
gradle :server:app:build
TIP
You might run into Checkstyle compile errors. If that is the case, you can either run
the build with gradle build -P skipCheckstyle=true, set the skipCheckstyle property
in gradle.properties to true, or fix the Checkstyle errors.
If you would like to load some initial data, you can also run the init app before starting the server:
gradle :server:init:bootrun --args='--spring.profiles.active=dev-env,init-data'
When you create a new document and enter an invalid address that conflicts with our specified
validation rules, your form should return an error similar to the following:
20

-- 20 of 23 --

Alternatively, if the address meets the validation rules but does not actually exist, the form should
display the following error:
21

-- 21 of 23 --

Otherwise, if you enter a valid address for a contact (e.g. "Platz der Republik 1, 11011 Berlin,
Deutschland"), the document should be successfully created.
WARNING
It is possible that even if you enter a valid and existing address, that OpenCage
does not find it with a minConfidence of 10. You can try setting another value,
e.g. lowering it.
Conclusion
In this task, you learned how to
• access data in documents,
• introduce validation for invalid data, and
• hook into lifecycle events of a document for injecting custom logic.
If something does not seem right, or you got stuck at any point, just check out 2025.06-
ext5/backend/task-1-end to see the differences between both implementations.
TIP Remember that it is necessary to provide an API key for OpenCage. Therefore, refer to
22

-- 22 of 23 --

the chapter Creating an OpenCage API key for instructions on creating the key, and
replace the placeholder YOUR-API-KEY in
server/app/src/main/resources/config/application-dev.properties.
WARNING
When you test your implementation, keep in mind that server-side error
messages for address validation are only shown in English. If you select
another locale, the error message box will be empty. This is because
localization of server-side error messages is only possible with static strings,
but the address validation error contains dynamic text messages (the exact
issues with the address).
Task 2: Document Manipulation »
23

-- 23 of 23 --

