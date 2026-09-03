# utils_server_connector server connector documentation src

Server Connector
Server connector is a generic component for request/response server
communication.
1. Server Connector API
API for server connector which contains Interfaces for main connector components.
• ServerConnector
• ServerRequest
• ServerResponse
2. JAVA REST Server connector
REST Server Connector is a connector implementation which uses HTTP REST as communication
protocol. The implementation uses Spring’s RestTemplte and it’s interceptor mechanism.
See class diagram for key components:
The REST Server Connector provides implementation for most commons HTTP methods:
Table of Contents
1. Server Connector API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 1
2. JAVA REST Server connector . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 1
2.1. Extensions. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
2.1.1. Interceptors . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
2.1.2. ErrorHandler . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
2.1.3. MessageConverter. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
2.2. Initialization. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 3
2.3. SpringBoot . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
3. JS Connector Client. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 4
4. Other Resources . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
5. Migration Instructions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
5.1. 2026.06. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
5.1.1. utils-rest-server-connector . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Dependency Updates . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
Breaking Changes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
5.1.2. utils-connector-client . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
Breaking Changes . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
1

-- 1 of 7 --

• RestGetConnector
• RestPostConnector
• RestPutConnector
• RestDeleteConnector
• RestOptionsConnector
• RestHeadConnector
Internally it’s implemented with single GenericRestConnector which is not made public and above
classes are type wrappers.
 If there is need to create implementations for connector(s) it has to be
implemented by an implementor.
The REST Server Connector behaviour can be extended from outside. All components are illustrated
in the following Class Diagram.
Figure 1. RestTemplate Class Diagram
REST Server Connector internal architecture and flow are illustrated in the following diagram:
Figure 2. Rest Server Connector Architecture
2

-- 2 of 7 --

2.1. Extensions
REST Server Connector can be extended in many aspects from outside. All extensions can be
supplied to the implementation during initialization time.
2.1.1. Interceptors
Before an HTTP/HTTPS request is performed the interceptors are called. This allows request
customization. Most common use-case is header modification. See in following example.
public class AcceptHeaderInterceptor implements ClientHttpRequestInterceptor {
@Override
public ClientHttpResponse intercept(HttpRequest request, byte[] body,
ClientHttpRequestExecution execution) throws IOException {
HttpHeaders headers = request.getHeaders();
List<MediaType> accept = headers.getAccept();
if (accept.isEmpty()) {
headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
}
return execution.execute(request, body);
}
}
2.1.2. ErrorHandler
Error handler is and implementation of the ResponseErrorHandler. It can customize behaviour how
the connector detects an error and what data are extracted from the server response. REST Server
Connector itself doesn’t extend standard spring behaviour and uses spring
DefaultResponseErrorHandler implementation.
2.1.3. MessageConverter
Message converter is used to convert HTTP request/response to an Object and vice versa.
Implementation is picked based on content type. REST Server Connector uses default message
converters supplied by Spring implementation.
2.2. Initialization
In order to use the REST Server Connector it needs to be initialized. There is
RestServerConnectorFactoryBuilder which builds RestServerConnectorFactory with all necessary
extensions. After the RestServerConnectorFactory is created it contains factory method for all
available connectors. After a connector is obtained from the factory it’s initialized and ready to use.
The connector is intended to live for all application lifetime and there is no need to call factory
multiple times.
3

-- 3 of 7 --

2.3. SpringBoot
REST Server Connector provide SpringBoot module which introduce property based configuration.
After initialization it provides initialized connectors as spring beans to the application context. If
there is a need for extending the implementation it’s enough to provide all extensions as spring
beans and the module will pick them up.
See the expected extension beans injects into the spring boot configuration:
@Inject
private Optional<List<ClientHttpRequestInterceptor>> interceptors;
@Inject
private Optional<List<ResponseErrorHandler>> errorHandlers;
@Inject
private Optional<List<HttpMessageConverter<?>>> messageConvertes;
3. JS Connector Client
Connector Client is a javascript library which helps you to setup, communicate to Rest Server
Connector in the backend. The library provides the setup classes and filters.
• ServerConnector is an interface which allows you to provide your own implementation for
connect and fetch.
• RestServerConnector is out of the box implementation ServerConnector which help you to
connect to the backend using REST. During the construction of connector it’s default (you can’t
change) that add some request and response filters. You can add more additional request and
response filters by your own.
const additionalRequestFilter: RequestFilter[] = [...];
const responseFilters: ResponseFilter[] = [...];
const serverConnector = new RestServerConnector(serverURL, requestFilters,
responseFilters);
• ConnectorLocator will help you holds and provides access to a single instance of a
ServerConnector
ConnectorLocator.createInstance(serverConnector);
const baseUrl = (ConnectorLocator.getInstance().getServerConnector() as
RestServerConnector).getBaseUrl();
• Filters are the convenient ways for you to intercept the Request and Response according to
your needs.
4

-- 4 of 7 --

4. Other Resources
• JavaDoc
• TypeDoc
5. Migration Instructions
 Please have a look at Migration to latest A12 chapter for an explanation of general
steps on how to upgrade before starting with the component migration.
 UAA provides Hypermod (utils-connector-codemod) for automatic migration of
breaking changes of utils-connector-client. Please read how to apply Hypermod
recipes in A12 frontend components.
5.1. 2026.06
5.1.1. utils-rest-server-connector
Dependency Updates
• Upgraded to Spring Boot 4.0.1
• Upgraded to Spring Framework 7.0.2
• Upgraded to Jackson 2.21.0
• Supports Java 25
• Replaced OkHttp with Apache HttpClient 5 (org.apache.httpcomponents.client5:httpclient5)
Breaking Changes
HttpClient
• Replaced OkHttpClient with Apache HttpClient from
org.apache.hc.client5.http.classic.HttpClient with default implementation
CloseableHttpClient
◦ Default configurations:
▪ Using PoolingHttpClientConnectionManager as connection manager
▪ Most of the timeouts (connection, socket, connection request) are set to 5 minutes
instead of infinite as previously with OkHttpClient
• RestServerConnectorFactoryBuilder.withHttpClient() now accepts HttpClient instead of
OkHttpClient
Required: Yes if your code calls RestServerConnectorFactoryBuilder.withHttpClient() or relies on
the previous infinite-timeout defaults; otherwise the new defaults apply automatically.
Migration: Manual.
5

-- 5 of 7 --

Result: The connector uses Apache HttpClient 5 (CloseableHttpClient with
PoolingHttpClientConnectionManager) and 5-minute default timeouts.
RestTemplate to RestClient
• RestTemplate usage has been replaced with RestClient (Spring Framework 7 preferred API)
Required: Yes if your code depended on RestTemplate-specific behavior or extension points of the
connector.
Migration: Manual.
Result: Requests are issued through Spring RestClient.
ResponseErrorHandler
• Spring Framework 7 removed org.springframework.web.client.ResponseErrorHandler interface
• A new com.mgmtp.a12.connector.rest.ResponseErrorHandler interface is introduced to continue
supporting global error handling
◦ hasError(HttpStatusCode httpStatusCode): indicates whether the given response status is an
error
◦ handleError(HttpRequest request, ClientHttpResponse response): handles the error response
if hasError returned true
Required: Yes if your code implemented org.springframework.web.client.ResponseErrorHandler for
global error handling.
Migration: Manual.
Result: Global error handling implemented against
com.mgmtp.a12.connector.rest.ResponseErrorHandler.
5.1.2. utils-connector-client
Breaking Changes
Top-Level Package Exports
• utils-connector now exposes only its top-level package entry. The package.json field was
changed from main to exports, so deep import paths into the package internals (e.g.
@com.mgmtp.a12.utils/utils-connector/lib/…) are no longer resolvable.
• All public API must be imported from the package root.
• Example:
- import { RestServerConnector } from "@com.mgmtp.a12.utils/utils-
connector/lib/main/internal/connector";
+ import { RestServerConnector } from "@com.mgmtp.a12.utils/utils-connector";
Required: Yes if your code imports from deep paths (@com.mgmtp.a12.utils/utils-connector/lib/…).
Migration: Automatic by utils-connector-codemod (top-level-imports recipe).
Result: All public API is imported from the package root @com.mgmtp.a12.utils/utils-connector.
6

-- 6 of 7 --

Custom Headers Merging
• Custom headers provided via customHeaders in RestRequestPayload are now merged with the
default headers (Content-Type and Accept) instead of replacing them.
• If a custom header has the same key as a default header (e.g., a custom Content-Type), the
custom value takes precedence.
• Custom headers with undefined values (e.g., ["headerName"] without a second element) now act
as removal signals — the header will be excluded from the final request, even if it was set by a
default header.
Required: Yes if your code added Content-Type: application/json;charset=utf8 or Accept:
application/json to customHeaders to work around the previous replacement behavior, those entries
can now be removed. Or if you want to remove UT’s default headers by adding neutral header like
Accept: *, you now need to add [Content-Type], [Accept] into customHeaders
Migration: Manual.
Result: Default headers are provided automatically and merged with custom headers; matching
custom keys override defaults, and undefined values remove a header.
7

-- 7 of 7 --

