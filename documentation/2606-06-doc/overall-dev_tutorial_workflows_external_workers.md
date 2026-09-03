# overall dev_tutorial_workflows_external_workers

Task 2 - External Worker
Implementation
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Workflows > Introduction
before continuing here.
You can check out the tag 2025.06-ext5/workflows/task-2-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/workflows/task-2-end to see
differences between both implementations.
Use Case
Implement the "Generate Gift Card" external worker for the Customer Onboarding Process.
This external worker will demonstrate how to create asynchronous task handlers that can handle
resource-intensive operations and can be scaled independently of the workflow engine.
End Result
Upon finishing this task, you will know:
• How to implement and test external workers
• Benefits and drawbacks of external workers
◦ Performance considerations and others
Step-by-Step Instructions
Understanding External Workers
External workers allow service tasks that:
1

-- 1 of 9 --

• Run outside the process engine: Better isolation and scalability
• Handle CPU-intensive operations: Don’t block the engine
• Enable horizontal scaling: Multiple worker instances can process tasks
• Polyglot programming: External task client apps can be implemented in any language
(although CIB 7 only provides starters for Java and Spring Boot)
Key Concepts:
• External Task: A service task configured with external implementation
• Worker: Application that polls for and handles external tasks
• Topic: Named queue that connects tasks to workers
• Lock: An external task is locked (for a configurable duration) once fetched by a worker
Implementing the Gift Card Generation Worker
The "Generate Gift Card" worker needs to:
1. Poll for gift card generation tasks
2. Generate a gift card code
3. Store gift card code back to the process
External Tasks
External task clients run outside the engine. CIB 7 provides starters for pure Java, pure Spring, and
Spring Boot. We recommend using the Spring Boot starter to create a service responsible for gift
card generation.
The service task in BPMN is modeled as follows:
The topic name is GENERATE_GIFT_CARD. The external task handler must subscribe to this exact topic.
Admin UI
Remember you can visit http://localhost:8088/camunda/ to observe process execution for
debugging.
2

-- 2 of 9 --

CIB 7 vs Camunda
Being a fork of Camunda 7, CIB 7 still has relics of that naming. For instance, the configuration
properties you will need still use camunda as prefix. In the URL above, /camunda is intentional because
it leads to the legacy Camunda Admin UI.
Your task:
• Set up a new worker app using Spring Boot and the CIB 7 External Task Client starter.
◦ Call the new module generate-gift-card-external-worker.
◦ Implement an external task handler.
▪ Subscribe to the topic GENERATE_GIFT_CARD.
▪ The gift gard code should have the format VIP-<some-uuid>.
• Unit test the task handler logic in your new worker app.
• Bonus: Test the service task setup in the BPMN model via the CIB 7 Testing API.
◦ This is done in the workflow-engine module.
• Manually integration-test via the Admin UI.
Use the following resources to complete this task:
• To implement the External Worker app:
◦ Spring Initializr to initialize a new Spring Boot app
◦ CIB 7 > External Task Client Spring Boot
▪ You only need the dependency in the intro, the brief topic subscription section, and the
basic client configuration.
• To test the service task setup:
◦ CIB 7 > Testing API for External Tasks
◦ Camunda > SimpleTestCase.java template (there is also externalTask(…) equivalent to
task(…))
TIP
In the external task client configuration, you will need the following property to
conform to the A12 datetime format: camunda.bpm.client.date-format=yyyy-MM-
dd'T'HH:mm.
▼ Hints
Dependencies
Your External Task Client app will need dependencies along the lines of:
dependencies {
implementation libs.cibseven.external.task.client
implementation libs.jaxb.api // jaxb-api:jakarta.xml.bind:4.0.1
developmentOnly libs.spring.boot.devtools
3

-- 3 of 9 --

annotationProcessor libs.spring.boot.configuration.processor
testImplementation testCatalogLibs.spring.boot.starter.test
}
Testing
The task handler test is a regular unit test and doesn’t need the CIB 7 Testing API. You can use
mocking frameworks like Mockito to mock the ExternalTask and ExternalTaskService interfaces
and verify on them:
var mockExternalTaskService = mock(ExternalTaskService.class);
var mockExternalTask = mock(ExternalTask.class);
To use JUnit 5, make sure to configure the test task accordingly in build.gradle:
tasks.named('test') {
useJUnitPlatform()
}
▼ Click to see solution
External Worker Spring Boot App
File: settings.gradle
Include the new module:
include 'generate-gift-card-external-worker'
Add required dependencies to the Gradle version catalog:
// def springBootVersion = '3.4.5'...
def cibsevenVersion = '2.0.0'
// ... libs {
version('cibseven', cibsevenVersion)
version('jaxb','4.0.1')
library('cibseven-external-task-client', 'org.cibseven.bpm.springboot', 'cibseven-
bpm-spring-boot-starter-external-task-client').versionRef('cibseven')
library('jaxb-api', 'jakarta.xml.bind', 'jakarta.xml.bind-api').versionRef('jaxb')
// ... testCatalogLibs {
version('cibseven', cibsevenVersion)
library('cibseven-bpm-assert', 'org.cibseven.bpm', 'cibseven-bpm-
4

-- 4 of 9 --

assert').versionRef('cibseven')
File: generate-gift-card-external-worker/build.gradle
plugins {
id 'java'
id 'application'
alias(gradlePluginLibs.plugins.spring.boot)
}
springBoot {
mainClass =
'com.mgmtp.a12.workflows.tutorial.worker.generategiftcard.GenerateGiftCardExternalWo
rkerApplication'
}
dependencies {
implementation libs.cibseven.external.task.client
implementation libs.jaxb.api
developmentOnly libs.spring.boot.devtools
annotationProcessor libs.spring.boot.configuration.processor
testImplementation testCatalogLibs.spring.boot.starter.test
}
tasks.named('test') {
useJUnitPlatform()
}
File: generate-gift-card-external-
worker/src/main/java/com/mgmtp/a12/workflows/tutorial/worker/generategiftcard/GenerateGiftCa
rdExternalWorkerApplication.java
This file is unchanged from start.spring.io:
package com.mgmtp.a12.workflows.tutorial.worker.generategiftcard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class GenerateGiftCardExternalWorkerApplication {
public static void main(String[] args) {
SpringApplication.run(GenerateGiftCardExternalWorkerApplication.class,
args);
}
5

-- 5 of 9 --

}
File: generate-gift-card-external-worker/src/main/resources/application.properties
Here, external task client configuration is placed as described in the CIB 7 docs.
camunda.bpm.client.base-url=http://localhost:8088/engine-rest
camunda.bpm.client.worker-id=generate-gift-card-worker
camunda.bpm.client.basic-auth.username=admin
camunda.bpm.client.basic-auth.password=admin
# Required: A12 date format used by A12 Workflows Engine
camunda.bpm.client.date-format=yyyy-MM-dd'T'HH:mm
File: workflow-engine/src/main/resources/application-dev.properties
For simplicity, we extend UAA’s unsecured URLs to allow unauthenticated access to /engine-
rest/external-task/** which will be used by external workers:
# UAA
# ...
mgmtp.a12.uaa.authentication.unsecured.urls=<...>,/${spring.jersey.application-
path}/external-task/**
File: generate-gift-card-external-
worker/src/main/java/com/mgmtp/a12/workflows/tutorial/worker/generategiftcard/GenerateGiftCa
rdHandler.java
The gift card generator simply creates random gift codes of the form VIP-<some-uuid>.
package com.mgmtp.a12.workflows.tutorial.worker.generategiftcard;
import org.cibseven.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.cibseven.bpm.client.task.ExternalTask;
import org.cibseven.bpm.client.task.ExternalTaskHandler;
import org.cibseven.bpm.client.task.ExternalTaskService;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import java.util.Collections;
import java.util.UUID;
@Configuration
@ExternalTaskSubscription("GENERATE_GIFT_CARD")
public class GenerateGiftCardHandler implements ExternalTaskHandler {
@Override
public void execute(ExternalTask externalTask, ExternalTaskService
externalTaskService) {
6

-- 6 of 9 --

String giftCardCode = "VIP-" + UUID.randomUUID();
externalTaskService.complete(externalTask,
Collections.singletonMap("giftCardCode", giftCardCode));
LoggerFactory.getLogger(GenerateGiftCardHandler.class).info("Generated gift
card: {}", giftCardCode);
}
}
The handler completes the external task via externalTaskService.complete and passes back the
gift card code which will be stored as a new process variable named giftCardCode.
NOTE
At this point, you can run the new external worker app via its main class
GenerateGiftCardExternalWorkerApplication and it will start generating gift codes.
There may be a noticeable delay between reaching the task and it being
processed by the worker.
NOTE
Note that the process will fail because of the missing Java delegate in the
subsequent service task. So the following incident in the Cockpit is expected for
now:
Next is the unit test for the gift card handler.
This unit test mocks the ExternalTask and ExternalTaskService interfaces to verify that the
handler behaves as expected, i.e. it calls externalTaskService.complete with a giftCardCode
variable.
File: generate-gift-card-external-
worker/src/test/java/com/mgmtp/a12/workflows/tutorial/worker/generategiftcard/GenerateGiftCa
rdHandlerTest.java
package com.mgmtp.a12.workflows.tutorial.worker.generategiftcard;
import org.cibseven.bpm.client.task.ExternalTask;
7

-- 7 of 9 --

import org.cibseven.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
class GenerateGiftCardHandlerTest {
GenerateGiftCardHandler generateGiftCardHandler = new GenerateGiftCardHandler();
@Test
public void shouldGenerateValidGiftCardCode() {
// given
var mockExternalTaskService = mock(ExternalTaskService.class);
var mockExternalTask = mock(ExternalTask.class);
ArgumentCaptor<Map<String, Object>> variablesCaptor =
ArgumentCaptor.captor();
doNothing().when(mockExternalTaskService).complete(eq(mockExternalTask),
any());
// when
generateGiftCardHandler.execute(mockExternalTask, mockExternalTaskService);
// then
verify(mockExternalTaskService).complete(eq(mockExternalTask),
variablesCaptor.capture());
String capturedCode = (String)
variablesCaptor.getValue().get("giftCardCode");
assertNotNull(capturedCode);
assertTrue(capturedCode.startsWith("VIP-"));
}
}
NOTE
In real scenarios, we recommend adding higher-level tests that verify the inter-
service interaction. For instance, we did not test that the worker actually
subscribes to the correct topic name (GENERATE_GIFT_CARD). This requires running
the workflow engine and the external worker app (and Data Services if not
mocked) via a tool like Testcontainers. You will implement such a test in a later
task for Java Delegates.
8

-- 8 of 9 --

Recommendations
Consider the following aspects before using external workers.
• Noticeable Latency: Both the polling interval and network cause noticeable latency.
◦ Finetuning configuration can improve this to some extent, but external worker performance
in CIB 7 (and Camunda 7) is not great.
◦ On the other hand, external workers can be scaled independently to handle very high loads.
For instance, if you have millions of resource-intensive computations to run and need many
replicas of the worker.
◦ Thus, we currently recommend using external workers judiciously (if you must move load
off the workflow engine) and prefer Java delegates or script tasks.
• Testing: Keep in mind that integration testing external tasks is more complex as it requires
running multiple services.
Conclusion
You have implemented and tested a simple external worker that handles asynchronous gift card
generation.
Key takeaways:
• External workers enable asynchronous processing outside the process engine.
• External workers can be scaled horizontally for high-throughput scenarios.
• Consider network latency and use external workers judiciously.
If something does not seem right, or you got stuck at any point, you can just check out 2025.06-
ext5/workflows/task-2-end to see differences between both implementations.
« Task 1: Script Tasks Task 3: Java Delegates »
9

-- 9 of 9 --

