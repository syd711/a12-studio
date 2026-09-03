# overall dev_tutorial_backend_custom_endpoint

Task 4 - Custom Endpoint
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Backend > Introduction
before continuing here.
You can check out the tag 2025.06-ext5/backend/task-4-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/backend/task-4-end to see how
your code and models differ from the solution.
Use-Case
To enhance reporting capabilities, we are going to add a custom operation to handle an RPC
request, specifically for retrieving a list of contacts based on defined criteria such as customer type,
nationality and locale language.
This operation is essential for reporting purposes, enabling clients to obtain a customized list of
contacts tailored to their needs.
End Result
Upon finishing this task you will know:
• How to add a custom operation in the backend.
• How to process server-side RPC requests.
• How to use Data Services APIs.
• How to use a REST API Client for sending HTTP requests.
In the end, a request sent to the custom operation should yield a response similar to the following.
1

-- 1 of 30 --

Step-by-Step Instructions
How Operations Are Handled
You can find an overview of the existing RPC operations from Data Services in Data Services > JSON-
RPC 2.0 Core Operations. These operations can be extended by providing additional custom
operations to this endpoint.
To achieve this, it is generally only necessary to set up the following in a project:
• Create a Java class with a @RemoteOperation annotation and set its value name to the operation
name you want to use.
• Add a public method called rpc.
• Add the operation name to the list mgmtp.a12.dataservices.jsonRpc.allowedOperations in
server/app/src/main/resources/config/application-shared.properties. This whitelists the
specific operation, allowing it to be accepted by Data Services.
See Data Services > Custom Operations for more details.
Developing Custom Operation
Already Provided Classes, Interfaces and Utilities
Constants
This class provides static string variables for referencing our Document Model name and paths.
These field and group paths will be used to retrieve values from the documents.
It has now been extended to include all Document Model field and group paths required for
2

-- 2 of 30 --

handling the contact report request.
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/utils/Constants.java
package com.mgmtp.a12.tutorial.server.utils;
//...
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
public static final String CONTACT_MODEL_NAME = "Contact_DM";
public static final String CONTACT_PERSONALDATA_EMAIL_PATH =
"/Contact/PersonalData/Email";
public static final String CONTACT_PERSONALDATA_FIRSTNAME_PATH =
"/Contact/PersonalData/FirstName";
public static final String CONTACT_PERSONALDATA_LASTNAME_PATH =
"/Contact/PersonalData/LastName";
public static final String CONTACT_PERSONALDATA_GENDER_PATH =
"/Contact/PersonalData/Gender";
public static final String CONTACT_PERSONALDATA_NATIONALITY_PATH =
"/Contact/PersonalData/Nationality";
public static final String CONTACT_PERSONALDATA_CUSTOMER_TYPE_PATH =
"/Contact/PersonalData/CustomerType";
public static final String CONTACT_PHONES_PATH = "/Contact/Phones";
public static final String CONTACT_PHONES_PHONE_NUMBER_PATH =
"/Contact/Phones/PhoneNumber";
public static final String CONTACT_PHONES_PHONE_TYPE_PATH =
"/Contact/Phones/Type";
public static final String CONTACT_ADDRESS_PATH = "/Contact/Address";
//...
}
Contact
This class will store and represent our contact related data. It is configured as a Builder for easier
instantiation of the contact data for our report. It will be used for mapping the attributes of the
contact entity to the corresponding Document Model fields.
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/Contact.java
package com.mgmtp.a12.tutorial.server.report;
//...
@Data
@Builder
public class Contact {
3

-- 3 of 30 --

private String firstName;
private String lastName;
private String email;
private String gender;
private String nationality;
private String customerType;
@Singular
private List<Phone> phones;
@Data
@Builder
public static class Phone {
private String number;
private String type;
}
}
NOTE
As you might have noticed, the Contact class does not contain a variable for storing
addresses. This omission is intentional, as adding address handling would increase
implementation complexity without offering additional learning benefits.
The approach for handling addresses is quite similar to that of phone numbers. So,
if you wish, you can extend the implementation to include address handling at the
end.
ContactReportOperation
This class will handle our custom RPC operation and forward it to our ContactReportService, which
contains the actual logic for creating the report. It already provides the necessary configuration for
receiving a GET_CONTACT_REPORT request.
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/ContactReportOperation.java
package com.mgmtp.a12.tutorial.server.report;
//...
/**
* This operation is similar to the "LIST_DOCUMENTS" operation, where it collects all
the contact data based on the
* given params.
*/
@Slf4j
@RemoteOperation(name = GET_CONTACT_REPORT)
@Component
@RequiredArgsConstructor
public class ContactReportOperation {
public static final String GET_CONTACT_REPORT = "GET_CONTACT_REPORT";
4

-- 4 of 30 --

private final ContactReportService contactReportService;
/**
* Handles RPC calls to retrieve a list of contacts based on the specified
customer type, nationality,
* and locale language.
*
* @param customerType the type of the customer to filter by (e.g.,
"Individual", "Business").
* @param nationality the nationality of the contacts to filter by (e.g.,
"German", "British").
* @param localeLanguage the language code used for localization; must be either
"en" or "de".
* @return a list of {@link Contact} objects matching the specified criteria.
* @throws IllegalArgumentException if the localeLanguage is not "en" or "de".
*/
public List<Contact> rpc(@NonNull @JsonRpcParam("customerType") String
customerType,
@NonNull @JsonRpcParam("nationality") String nationality,
@NonNull @JsonRpcParam("localeLanguage") String
localeLanguage) {
log.debug("{} called with parameters [customerType={}, nationality={},
localeLanguage={}]",
GET_CONTACT_REPORT,
customerType,
nationality,
localeLanguage
);
// Put your code here ...
return new ArrayList<>();
}
}
ContactReportService
This class will contain the actual logic for processing the contact report request, providing the data
set based on the received customerType and nationality criteria. It will query for the contact
documents and map them to instances of our Contact class.
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/ContactReportService.java
package com.mgmtp.a12.tutorial.server.report;
//...
@Component
@RequiredArgsConstructor
public class ContactReportService {
5

-- 5 of 30 --

private final QueryService queryService;
private final IDocumentRepository documentRepository;
private final DocumentModelServiceFactory documentModelServiceFactory;
private final IModelLoader<IDocumentModel> documentModelLoader;
/**
* Retrieves a list of contacts based on the specified customer type, nationality,
and locale language.
*
* @param customerType the type of the customer to filter by (e.g., "lead",
"vip").
* @param nationality the nationality of the contacts to filter by (e.g.,
"German", "British").
* @param localeLanguage the language code used for localization (e.g., "en",
"de").
* @return a list of {@link Contact} objects matching the specified criteria.
*/
public List<Contact> getContacts(String customerType, String nationality, String
localeLanguage) {
// Put your code here ...
return new ArrayList<>();
}
}
All necessary beans have already been injected for the implementation, including:
• QueryService: Will be used for querying the documents.
• IDocumentRepository: Will be used to retrieve the documents from the persistent store.
• DocumentModelServiceFactory: Will be used to create an IDocumentModelSearchService, which will
allow us to retrieve values from the Contact_DM Document Model.
• IModelLoader<IDocumentModel>: Will be used to load the Document Model, which contains the
localized text values.
Adding the Property
The contact report operation is handled as a new RPC operation within the ContactReportOperation
class, the operation name is defined as GET_CONTACT_REPORT. To enable this operation, we have to
configure the allowed operations on server-side in the properties file.
Therefore, add GET_CONTACT_REPORT at the end of the list of the property
mgmtp.a12.dataservices.jsonRpc.allowedOperations.
File: server/app/src/main/resources/config/application-shared.properties
#...
6

-- 6 of 30 --

# Security
mgmtp.a12.dataservices.jsonRpc.allowedOperations= A12_INTERNAL_OPERATIONS, \
ATTACHMENT_OPERATIONS, \
CDD_OPERATIONS, \
DOCUMENT_OPERATIONS, \
LINK_OPERATIONS, \
GET_CONTACT_REPORT
#...
This allows us to process the RPC request in the backend. If the operation name is not whitelisted,
Data Services would block the request immediately.
NOTE The order of the operations does not have any effect. You could also add new
operation names at the beginning of the list.
Implementing the Contact Report Operation
To be able to process the custom RPC operation GET_CONTACT_REPORT, we have to extend its already
provided class framework. You can refer to the previous section ContactReportOperation for more
information on this framework.
The method accepts three request parameters:
• customerType: Specific enumeration criteria to group customers by their type, which we will use
for filtering. Existing types:
◦ Lead
◦ Inactive
◦ VIP
◦ Suspended
◦ Partner
• nationality: Specific criteria to filter contacts by their nationality. Input is plain text.
• localeLanguage: The language code for localizing the data. Existing locales:
◦ English en
◦ German de
There is just a small addition necessary to finish this class’s implementation.
Your task:
• Throw an IllegalArgumentException with a descriptive error message in the case of the
localeLanguage neither being German nor English.
• Forward the request to contactReportService and return its result.
▼ Click to see solution
File:
server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/ContactReportOperation.java
7

-- 7 of 30 --

package com.mgmtp.a12.tutorial.server.report;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import static
com.mgmtp.a12.tutorial.server.report.ContactReportOperation.GET_CONTACT_REPORT;
/**
* This operation is similar to the "LIST_DOCUMENTS" operation, where it collects
all the contact data based on the
* given params.
*/
@Slf4j
@RemoteOperation(name = GET_CONTACT_REPORT)
@Component
@RequiredArgsConstructor
public class ContactReportOperation {
public static final String GET_CONTACT_REPORT = "GET_CONTACT_REPORT";
private final ContactReportService contactReportService;
/**
* Handles RPC calls to retrieve a list of contacts based on the specified
customer type, nationality,
* and locale language.
*
* @param customerType the type of the customer to filter by (e.g.,
"Individual", "Business").
* @param nationality the nationality of the contacts to filter by (e.g.,
"German", "British").
* @param localeLanguage the language code used for localization; must be either
"en" or "de".
* @return a list of {@link Contact} objects matching the specified criteria.
* @throws IllegalArgumentException if the localeLanguage is not "en" or "de".
*/
public List<Contact> rpc(@NonNull @JsonRpcParam("customerType") String
customerType,
@NonNull @JsonRpcParam("nationality") String
nationality,
@NonNull @JsonRpcParam("localeLanguage") String
localeLanguage) {
log.debug("{} called with parameters [customerType={}, nationality={},
8

-- 8 of 30 --

localeLanguage={}]",
GET_CONTACT_REPORT,
customerType,
nationality,
localeLanguage
);
if (!localeLanguage.equals("en") && !localeLanguage.equals("de")) {
throw new IllegalArgumentException("localeLanguage must be either 'en'
or 'de'");
}
return contactReportService.getContacts(customerType, nationality,
localeLanguage);
}
}
Implementing the Contact Report Service
To retrieve the list of all contacts, we have to extend the already provided class framework. You can
refer to the previous section ContactReportService for more information on this framework.
Before we start with the implementation, we will have a closer look at the already registered beans
in this class. In this context, only the required methods and functionalities of the used class and
interfaces are highlighted.
The QueryService is defined as follows:
File: com.mgmtp.a12.dataservices.query.QueryService.java
package com.mgmtp.a12.dataservices.query;
//...
public interface QueryService {
//...
<T> QueryPage<T> query(QueryRoot queryRoot, String locale);
}
Its only publicly available method is the query function, which requires the following parameters:
• QueryRoot queryRoot: Contains the specific query parameters, such as
◦ the Document Model name,
◦ the name of the projection,
◦ the specification of the constraints,
◦ pagination settings,
◦ and others.
9

-- 9 of 30 --

• String locale: The locale for which the query is executed, which is used to interpret localized
values in the query itself.
For more details, see Data Services > QUERY JSON-RPC.
The IDocumentRepository is automatically configured by Data Services and can be easily injected
into projects using the default implementation. The primary functionalities of this interface revolve
around CRUD operations, facilitating the processing of documents to and from the persistent
storage. You can see how it is implemented below:
File: com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository.java
package com.mgmtp.a12.dataservices.document.persistence;
//...
public interface IDocumentRepository {
boolean supports(@NonNull DocumentV2 document);
boolean supports(@NonNull String modelName, @NonNull Optional<String> metadata);
Optional<DataServicesDocument> findByDocumentReference(@NonNull DocumentReference
documentReference);
@NonNull List<DocumentReference> findAllDocRefsForModel(@NonNull String modelId);
@NonNull default List<DocumentReference> findAllDocRefsForModel(@NonNull String
modelId, Pageable pageable) {
return findAllDocRefsForModel(modelId);
}
@NonNull List<DataServicesDocument> findDocumentsByDocRefs(@NonNull
List<DocumentReference> docRefs);
void create(@NonNull DataServicesDocument dataServicesDocument);
void update(@NonNull DataServicesDocument dataServicesDocument);
void delete(@NonNull DocumentReference documentReference);
void deleteAll(@NonNull String modelName, @NonNull Collection<DocumentReference>
documentReferences);
}
A project can have multiple implementations of this interface. The supports method then
determines for which documents which IDocumentRepository implementation shall be used.
For more information, refer to Data Services > IDocumentRepository.
10

-- 10 of 30 --

The DocumentModelServiceFactory provides methods to instantiate Document Model specific services,
which is implemented as follows:
File: com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory.java
package com.mgmtp.a12.kernel.md.facade;
//...
public class DocumentModelServiceFactory {
public IDocumentModelSerializer createDocumentModelSerializer() {
return new MDSerializerFactory().createDocumentModelSerializer();
}
public IDocumentModelService createDocumentModelService() {
return new ExternalDocumentModelService();
}
public IDocumentModelSearchService createDocumentModelSearchService(IDocumentModel
documentModel) {
return new DocumentModelSearchServiceImpl(documentModel);
}
public IDocumentModelMigrator createDocumentModelMigrator(ICustomMigrationConfig
customConfig) {
return new DocumentModelMigratorImpl(customConfig);
}
}
In particular, we will use the IDocumentModelSearchService by utilizing the
createDocumentModelSearchService method to create a search service for retrieving specific data
from the Document Model.
The IModelLoader provides the method loadModel to retrieve a model, as you can see below:
File: com.mgmtp.a12.dataservices.model.persistence.IModelLoader.java
package com.mgmtp.a12.dataservices.model.persistence;
import com.mgmtp.a12.model.Model;
import lombok.NonNull;
public interface IModelLoader<T extends Model> {
T loadModel(@NonNull String modelId);
}
In our case, we are interested in the Document Model, which is why the function is parameterized
with IDocumentModel in our ContactReportService.
Now that you have an overview of the registered beans, we can continue with the implementation
11

-- 11 of 30 --

of the ContactReportService. At first, we will create a set of smaller helper methods. Afterward, we
are going to put everything together.
Creating the Constraint
To use the queryService instance for querying contact documents, we first need to prepare the
corresponding constraint of type ILogicOperator. This constraint will be created by combining the
given parameters, such as customerType and nationality. By specifying these parameters, we ensure
that the query results are limited to contacts that exact match the specific customerType and
nationality values.
Such constraint can be represented in JSON as follows:
{
"constraint": {
"operator": "and",
"operands": [
{
"operator": "exact_match",
"field": "/Contact/PersonalData/CustomerType",
"value": "lead"
},
{
"operator": "exact_match",
"field": "/Contact/PersonalData/Nationality",
"value": "German"
}
]
}
}
As you can see, we are combining two exact_match operators with an and operator. In the next code
snippets below, you can see how this JSON structure could be mapped to the corresponding Java
objects:
File: com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator.java
package com.mgmtp.a12.dataservices.query.constraint.matching;
//...
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true) @ToString(callSuper =
true) @SuperBuilder
@QueryOperator("exact_match") public class ExactMatchOperator<T> extends
FieldAwareOperator implements CaseSensitive, ValueAware<T> {
private T value;
@Builder.Default private boolean caseSensitive = true;
12

-- 12 of 30 --

}
File: com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator.java
package com.mgmtp.a12.dataservices.query.constraint.logical;
//...
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true) @ToString(callSuper =
true) @SuperBuilder
@QueryOperator("and") public class AndOperator extends VariadicOperator {
}
Your task:
• Write a method createExactMatchOperator that takes fieldPath and fieldValue as inputs and
returns an instance of ExactMatchOperator.
• Write a method createAndOperator that takes customerType and nationality as inputs and returns
an instance of AndOperator which combines the 2 ExactMatchOperator instances created out of the
inputs.
▼ Click to see solution
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/ContactReportService.java
package com.mgmtp.a12.tutorial.server.report;
//...
@Component
@RequiredArgsConstructor
public class ContactReportService {
//...
/**
* Creates an and-operator for querying contacts based on customer type and
nationality.
*
* @param customerType the type of the customer to match.
* @param nationality the nationality of the contacts to match.
* @return a {@link AndOperator} object configured with the specified
information.
*/
private AndOperator createAndOperator(String customerType, String nationality) {
return AndOperator.builder()
.operands(Set.of(
13

-- 13 of 30 --

createExactMatchOperator(CONTACT_PERSONALDATA_CUSTOMER_TYPE_PATH, customerType),
createExactMatchOperator(CONTACT_PERSONALDATA_NATIONALITY_PATH, nationality)
))
.build();
}
/**
* Creates an exact_match-operator for a given field path and field value.
*
* @param fieldPath the path of the field to match.
* @param fieldValue the value of the field to match.
* @return the operator with the given parameters.
*/
private ExactMatchOperator<Object> createExactMatchOperator(String fieldPath,
String fieldValue) {
return ExactMatchOperator.builder()
.field(fieldPath)
.value(fieldValue)
.build();
}
}
Preparing a Map for Localized Texts
A DocumentV2 allows us to retrieve the values of enumeration fields. Each has a specific localized
text, which is configured in the Document Model. We will use these texts and create a mapping for
each field value and its corresponding localized text.
We can then use this mapping whenever a correct translation for a given field is necessary.
Especially when the mapping between the DocumentV2 and our Contact data object will be done.
Therefore, add the following to the ContactReportService.java:
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/ContactReportService.java
package com.mgmtp.a12.tutorial.server.report;
//...
@Component
@RequiredArgsConstructor
public class ContactReportService {
//...
/**
* Builds a map of field values to localized text for a specific field path.
*
* @param searchService the search service used to look up field values.
* @param fieldPath the path of the field to look up.
14

-- 14 of 30 --

* @param localeLanguage the language code used for localization.
* @return a map of field values to their localized text.
*/
private Map<String, String> buildEnumValueToLocalizedTextMap(
IDocumentModelSearchService searchService,
String fieldPath,
String localeLanguage
) {
Locale locale = Locale.of(localeLanguage);
return getEnumValuesFromFieldPath(searchService, fieldPath).stream()
.collect(toMap(IEnumerationType.IEnumValue::getValue, enumVal ->
enumVal.getLabel().get(locale)));
}
/**
* Retrieves the enumeration values for a specified field path.
*
* @param searchService the search service used to look up field values.
* @param fieldPath the path of the field to look up.
* @return a list of {@link IEnumerationType.IEnumValue} representing the
enumeration values for the field.
* @throws NoSuchElementException if the field element is not found at the
specified path.
*/
private List<IEnumerationType.IEnumValue> getEnumValuesFromFieldPath(
IDocumentModelSearchService searchService,
String fieldPath
) {
final IField element = (IField) searchService.getByPath(fieldPath)
.orElseThrow(() -> new NoSuchElementException("Element not found for
path: " + fieldPath));
final IFieldType fieldType = element.getFieldType();
if (fieldType instanceof ITypeDefType typeDefType) {
return typeDefType.getTypeDefinition()
.map(IFieldTypeDefinition::getFieldType)
.filter(IEnumerationType.class::isInstance)
.map(IEnumerationType.class::cast)
.map(IEnumerationType::getValues)
.orElse(emptyList());
} else if (fieldType instanceof IEnumerationType enumerationType) {
return enumerationType.getValues();
}
return emptyList();
}
}
When having a closer look at the searchService, we can observe the following structure for the
15

-- 15 of 30 --

enumeration fields:
As you can see, the localized labels for English and German are defined inside the enumeration
field type of our Document Model.
Getting String Field Values
In the previous step, we prepared the retrieval of texts from enumeration fields and their
respective localized labels. For the string based values however, we can just access the DocumentV2
directly.
For this, we will use the approach with the generated typed classes which was described in
Tutorials > Backend > Document Manipulation > Modification of a Document > Specific Approach.
While retrieving field values in this way, we have to consider that null can be returned, but in that
case, we want to use the empty string.
Therefore, add the following to the ContactReportService.java:
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/ContactReportService.java
package com.mgmtp.a12.tutorial.server.report;
//...
@Component
@RequiredArgsConstructor
public class ContactReportService {
//...
/**
* If the value is null, returns an empty string; otherwise, returns the value as
a string.
*/
private String getFieldValueAsString(String value) {
return Optional.ofNullable(value)
16

-- 16 of 30 --

.orElse("");
}
/**
* If the value is null, returns an empty string; otherwise, returns the enum name
as a string.
*/
private String getFieldValueAsString(Enum<?> value) {
return Optional.ofNullable(value)
.map(Enum::name)
.orElse("");
}
}
Mapping Contact Document to Contact Object
Before we start with the mapping between documents and contact objects, we are going to
implement the last helper method. We are going to add another method, which will take care of
mapping the Phones group of a document. This group contains the nested fields PhoneNumber
and Type, so we need to map these phone fields from the document to our Java Contact object.
Your task:
• With the generated typed classes, get the phones defined in the document and iterate over
them.
• Extract the PhoneNumber and Type values from the document.
• Use the phoneTypeToLocalizedTextMap to get the localized text for the phone type.
• Create a Contact.Phone object for each phone number and type combination.
• Use the following method framework for your implementation:
/**
* Maps the phone numbers and types from an {@link Contact_DM} to a list of {@link
Contact.Phone} objects.
*
* @param contactDoc the contact document containing phone information.
* @param phoneTypeToLocalizedTextMap the mapping of phone types to their
localized text.
* @return a list of {@link Contact.Phone} objects populated with phone numbers
and types from the document.
*/
private List<Contact.Phone> mapToContactPhones(
Contact_DM contactDoc,
Map<String, String> phoneTypeToLocalizedTextMap
) {
return contactDoc.contact().phones().stream()
// Put your code here ...
.toList();
}
17

-- 17 of 30 --

▼ Click to see solution
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/ContactReportService.java
package com.mgmtp.a12.tutorial.server.report;
//...
@Component
@RequiredArgsConstructor
public class ContactReportService {
//...
/**
* Maps the phone numbers and types from an {@link Contact_DM} to a list of
{@link Contact.Phone} objects.
*
* @param contactDoc the contact document containing phone information.
* @param phoneTypeToLocalizedTextMap the mapping of phone types to their
localized text.
* @return a list of {@link Contact.Phone} objects populated with phone numbers
and types from the document.
*/
private List<Contact.Phone> mapToContactPhones(
Contact_DM contactDoc,
Map<String, String> phoneTypeToLocalizedTextMap
) {
return contactDoc.contact().phones().stream()
.map(phone -> Contact.Phone.builder()
.number(getFieldValueAsString(phone.phoneNumber()))
.type(phoneTypeToLocalizedTextMap.get(getFieldValueAsString(phone.type())))
.build()
)
.toList();
}
}
All required helper methods are now written, which allows us to implement the method
responsible for mapping the entire contact documents and Contact Java objects.
Your task:
• Implement the mapToContact method, which retrieves the following values from the document
and creates a Contact object:
◦ firstName
◦ lastName
◦ email
◦ gender
18

-- 18 of 30 --

◦ nationality
◦ customerType
◦ phones
• Use the following method framework for your implementation:
/**
* Maps an {@link Contact_DM} to a {@link Contact} object using the document field
values and the given localized
* text maps.
*
* @param contactDoc the document representing the contact.
* @param genderToLocalizedTextMap the map of gender values to localized
text.
* @param customerTypeToLocalizedTextMap the map of customer type values to
localized text.
* @param phoneTypeToLocalizedTextMap the map of phone type values to localized
text.
* @return a {@link Contact} object populated with data from the document.
*/
private Contact mapToContact(
Contact_DM contactDoc,
Map<String, String> genderToLocalizedTextMap,
Map<String, String> customerTypeToLocalizedTextMap,
Map<String, String> phoneTypeToLocalizedTextMap
) {
// Put your code here ...
}
▼ Click to see solution
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/ContactReportService.java
package com.mgmtp.a12.tutorial.server.report;
//...
@Component
@RequiredArgsConstructor
public class ContactReportService {
//...
/**
* Maps an {@link Contact_DM} to a {@link Contact} object using the document
field values and the given localized
* text maps.
*
* @param contactDoc the document representing the contact.
* @param genderToLocalizedTextMap the map of gender values to localized
19

-- 19 of 30 --

text.
* @param customerTypeToLocalizedTextMap the map of customer type values to
localized text.
* @param phoneTypeToLocalizedTextMap the map of phone type values to
localized text.
* @return a {@link Contact} object populated with data from the document.
*/
private Contact mapToContact(
Contact_DM contactDoc,
Map<String, String> genderToLocalizedTextMap,
Map<String, String> customerTypeToLocalizedTextMap,
Map<String, String> phoneTypeToLocalizedTextMap
) {
PersonalData personalData = contactDoc.contact().personalData();
String firstName = getFieldValueAsString(personalData.firstName());
String lastName = getFieldValueAsString(personalData.lastName());
String email = getFieldValueAsString(personalData.emailAddress());
String gender = getFieldValueAsString(personalData.gender());
String nationality = getFieldValueAsString(personalData.nationality());
String customerType = getFieldValueAsString(personalData.customerType());
List<Contact.Phone> phones = mapToContactPhones(contactDoc,
phoneTypeToLocalizedTextMap);
return Contact.builder()
.firstName(firstName)
.lastName(lastName)
.email(email)
.gender(genderToLocalizedTextMap.get(gender))
.nationality(nationality)
.customerType(customerTypeToLocalizedTextMap.get(customerType))
.phones(phones)
.build();
}
}
Putting Everything Together
Now that everything is prepared, we can proceed with implementing the entry function getContacts
of our ContactReportService class. This means we can finally put together all the helper methods we
created previously and finish this task.
Your task:
• Create a QueryRoot object with the corresponding builder. Since we are searching for
documents, the projection name should be document. Add a paging of new Paging(0, 10) to limit
the result to 10 items.
• Use the queryService and the just created QueryRoot instance to query for the contact documents,
which will yield a RootQueryPage<DocumentTreeResult>. Since we are expecting a not localized
value for customerType, we can pass null as the locale.
• Use the documentRepository to retrieve all DataServicesDocument instances for the document
20

-- 20 of 30 --

references.
• Use the documentModelLoader to retrieve the Document Model.
• Create an instance of IDocumentModelSearchService.
• Use the previously implemented helper methods to map all documents to Contact Java objects.
▼ Click to see solution
File: server/app/src/main/java/com/mgmtp/a12/tutorial/server/report/ContactReportService.java
package com.mgmtp.a12.tutorial.server.report;
//...
@Component
@RequiredArgsConstructor
public class ContactReportService {
private final QueryService queryService;
private final IDocumentRepository documentRepository;
private final DocumentModelServiceFactory documentModelServiceFactory;
private final IModelLoader<IDocumentModel> documentModelLoader;
/**
* Retrieves a list of contacts based on the specified customer type,
nationality, and locale language.
*
* @param customerType the type of the customer to filter by (e.g., "lead",
"vip").
* @param nationality the nationality of the contacts to filter by (e.g.,
"German", "British").
* @param localeLanguage the language code used for localization (e.g., "en",
"de").
* @return a list of {@link Contact} objects matching the specified criteria.
* @throws NoSuchElementException if the document field elements required are
not found.
*/
public List<Contact> getContacts(String customerType, String nationality, String
localeLanguage) {
QueryRoot queryRoot = QueryRoot.builder()
.targetDocumentModel(CONTACT_MODEL_NAME)
.projectionName("document")
.constraint(createAndOperator(customerType, nationality))
.paging(new Paging(0, 10))
.build();
List<DocumentReference> documentReferences = queryService.query(queryRoot,
null).getContent().stream()
.map(DocumentTreeResult.class::cast)
.map(DocumentTreeResult::getDocRef)
.toList();
List<DataServicesDocument> contactDocuments =
21

-- 21 of 30 --

documentRepository.findDocumentsByDocRefs(documentReferences);
IDocumentModel documentModel =
documentModelLoader.loadModel(CONTACT_MODEL_NAME);
final IDocumentModelSearchService searchService =
documentModelServiceFactory.createDocumentModelSearchService(documentModel);
Map<String, String> genderToLocalizedTextMap =
buildEnumValueToLocalizedTextMap(searchService,
CONTACT_PERSONALDATA_GENDER_PATH, localeLanguage);
Map<String, String> customerTypeToLocalizedTextMap =
buildEnumValueToLocalizedTextMap(searchService,
CONTACT_PERSONALDATA_CUSTOMER_TYPE_PATH,
localeLanguage);
Map<String, String> phoneTypeToLocalizedTextMap =
buildEnumValueToLocalizedTextMap(searchService,
CONTACT_PHONES_PHONE_TYPE_PATH, localeLanguage);
return contactDocuments.stream()
.map(DataServicesDocument::getKernelDocument)
.map(Contact_DM::_viewOf)
.map(document -> mapToContact(
document,
genderToLocalizedTextMap,
customerTypeToLocalizedTextMap,
phoneTypeToLocalizedTextMap
))
.toList();
}
// Rest of class
}
All classes required for the reporting functionality have now been implemented. In the next step,
we can start the application and send a request to our new GET_CONTACT_REPORT RPC endpoint.
Running the Application
Now that everything is set up, we can start our application and test the changes in action. To test
our new endpoint, first run the application. Then, add a few contact documents with nationality
and customer type set.
You can find instructions on how to build and start the application in Tutorials > General
Information > Setup. Since we only modified the server/app module, you do not have to build the
entire application; you can simply execute the following command:
gradle :server:app:build
22

-- 22 of 30 --

TIP
You might run into Checkstyle compile errors. If that is the case, you can either run
the build with gradle build -P skipCheckstyle=true, set the skipCheckstyle property
in gradle.properties to true, or fix the Checkstyle errors.
If you would like to load some initial data, you can also run the init app before starting the server:
gradle :server:init:bootrun --args='--spring.profiles.active=dev-env,init-data'
Access Token
Before using the custom operation GET_CONTACT_REPORT, we need to retrieve the access token, which
needs to be set in the header of the RPC request.
Explanation
An access token is a security credential used in the authentication and authorization processes in
web and mobile applications. It verifies a user’s identity and permissions, allowing access to
protected resources.
Background
• Issuance: Access tokens are generated by an authorization server after a user successfully
authenticates and authorizes to an application.
• Format: They can be in different formats such as opaque strings or JSON Web Tokens (JWTs).
JWTs are commonly used because they are self-contained and can carry claims or user
information.
• Scope: Access tokens include scopes, which define what resources or actions the token holder
can access.
• Expiration: Access tokens have a limited lifespan and expire after a certain period of time,
enhancing security by reducing the risk if a token is compromised.
• Validation: Servers validate access tokens before allowing access to protected resources. For
JWTs, this involves checking the signature and ensuring the token has not expired.
The Access Token is generated after a successful authentication process, called login flow. The client
initiating the login flow stores the token and sends it with each request as an authorization header.
Our backend is using an A12 library called User Authorization Authentication (UAA) for
authentication and authorization. We are using local authentication in the tutorial application,
which means:
• UAA acts as Identity Provider (IDP), providing the login endpoint.
• The implementor is responsible for credential validation.
• UAA generates the access token.
So, the authorization header format for the request sent to our app would be: Authorization:
23

-- 23 of 30 --

UAABearer <Token>
Receiving an Access Token
The token can be easily retrieved via either of the following methods:
• Using the Browser’s Network tab:
Go to the Network tab in your browser’s developer tools. Click on one of the rpc request. If
none are available, refresh the tutorial application. Go to the request Headers and copy the
Authorization value.
NOTE Some browsers (e.g. Firefox) may truncate the value in the Network tab (indicated
by "…"). To avoid this, make sure you copy the unformatted value.
• Using the Redux DevTools:
Go to the Redux tab in your browser’s developer tools (if not available, you will have to install
the corresponding extension). Open one of the UAA-actions (e.g. UAA/UPDATE_ACCESS_TOKEN) and
open the uaa slice. Copy the access_token value. When sending the request with the token, it is
necessary to prefix it with UAABearer <Token>.
24

-- 24 of 30 --

To verify that the token you copied is valid, you can use jwt.io. Paste your token there—you should
be able to view its decoded header and payload if it is valid.
Sending a Request
As you might have already noticed, we did not add a button or functionality in the frontend to send
a request to our new endpoint.
To test it, we will instead use a tool called "Bruno", which allows us to easily create and send
multiple HTTP requests to an endpoint.
TIP
You could also use other tools or send a request directly from the command line.
However, Bruno is a convenient choice, especially since it’s a generally useful tool for
development.
Getting Bruno
Bruno offers for most platforms so-called "Portable" versions, which do not require any installation.
We recommend using these versions if possible.
Download: Bruno Tool
25

-- 25 of 30 --

When the download was successful, start the application.
NOTE
The screenshots below were taken using Bruno version 1.13.1. Please install the
latest version and just keep in mind that the user interface may look slightly
different.
Configuring Bruno
To be able to send requests via Bruno, we have to do a few configurations.
1. Create a new collection:
Click on the more options menu (⋯) and create a new collection with the name "Tutorial".
2. Create a new request:
Click on the more options menu (⋯) next to the previously created collection and add a new
POST request with the name "GET_CONTACT_REPORT" and the URL
"http://localhost:8081/api/v2/rpc".
26

-- 26 of 30 --

3. Adding an authorization header:
Click on the Headers tab, add a new header called "Authorization" and set its value to the access
token.
27

-- 27 of 30 --

4. Adding a request body:
Click on the Body tab, select JSON from the dropdown on the right and add a request body, for
example:
{
"jsonrpc": "2.0",
"id": "Get Contact Report 1",
"method": "GET_CONTACT_REPORT",
"params": {
"customerType": "lead",
"nationality": "British",
"localeLanguage": "de"
}
}
28

-- 28 of 30 --

5. Sending the request:
Click on the arrow icon (→, on the top right corner of the tool) or press CTRL + ENTER . If
everything is set up correctly, you should see a result similar to the following:
NOTE
If you receive an "…Invalid character in header content \["Authorization"]" error
when sending the request, make sure to copy the whole access token. For example,
the formatted access token from Firefox’s Network tab contains the …-character
which means that the value has been shortened.
NOTE
If you receive a "401 Unauthorized" error when sending the request, you most likely
have to copy the access token again. Access tokens are refreshed regularly, and an
old token may have been invalidated.
You have now implemented your first custom endpoint and successfully sent a request to it. Take a
moment to explore the response and experiment a bit. Try modifying the request, adjusting the
29

-- 29 of 30 --

endpoint’s behavior, or adding additional parameters to see how the endpoint reacts. This is a great
way to deepen your understanding of how everything works together.
Conclusion
In this task, you learned how to
• register your own RPC operation,
• use Data Services API for querying documents and loading Document Models,
• map field values of DocumentV2 instances to Java objects,
• retrieve an access token and send an HTTP request via Bruno.
If something does not seem right, or you got stuck at any point, just check out 2025.06-
ext5/backend/task-4-end to see the differences between both implementations.
Now that you have completed the backend tutorial, we would also really appreciate your feedback.
If you have any ideas or wishes for additional tasks or tutorials, you can let us know there.
You can also take a look at Tutorials > General Information > Structure if you are interested in
learning about other A12 topics, e.g. frontend customization.
« Task 3: Unit Testing
30

-- 30 of 30 --

