# overall dev_tutorial_workflows_script_tasks

Task 1 - Implementing Script Tasks
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Workflows > Introduction
before continuing here.
You can check out the tag 2025.06-ext5/workflows/task-1-start to follow along.
If you get stuck at any point, you can check out the tag 2025.06-ext5/workflows/task-1-end to see
how your code differs from the solution.
Use Case
Implement the "Assign Sales Representative" script task in the Customer Onboarding Process.
This script task will demonstrate how to write JavaScript code that executes within the CIB 7
engine, how to access process variables, interact with A12 APIs, and set workflow variables for
subsequent tasks.
End Result
Upon finishing this task, you will know:
• How to implement JavaScript-based script tasks in BPMN
• How to access and manipulate process variables in scripts
• How to bundle external JavaScript libraries for use in scripts
Step-by-Step Instructions
Understanding Script Tasks
Script tasks in CIB 7 allow you to execute code in a JSR 223-compliant script engine. Such script
engines exist for Python, JavaScript, and Groovy among others.
1

-- 1 of 10 --

NOTE CIB 7 uses GraalJS to execute JavaScript code. GraalJS is a JavaScript
implementation built on top of GraalVM. It does not require GraalVM JDK.
Script tasks can be a good choice for:
• Logic that requires a library only available in the scripting language
• Projects where developers are proficient in the used scripting language
Key characteristics of script tasks:
• Execute synchronously within the engine
• Have access to process variables and execution context
• Require a corresponding JSR 223-compliant script engine dependency on the classpath
◦ CIB 7 then autoconfigures it
Implementing the Sales Rep Assignment Script
The "Assign Sales Rep" task needs to:
1. Use the customer type as input
2. Assign an appropriate sales representative
Data Exchange and Getting Inputs
The Contact_DM has a field called CustomerType which contains the required input data. This field is
annotated with availableInProcessAs with value customerType.
Additionally, the config profile workflows-automatic-sync is applied in the server module. Because of
this, the Workflows Extension synchronizes all such annotated document fields to the process
engine.
So the current value of /Contact/PersonalData/CustomerType is available in the process as
customerType at all times.
We don’t focus on modeling here, so the BPMN model already defines the script task input as
follows:
2

-- 2 of 10 --

This is not strictly required. But it’s good practice to make clear what inputs and outputs a task has.
Inside the script task, you can access the variable simply as customerType.
Possible values of the customer type enum are:
• vip
• partner
• lead
• inactive
• suspended
Running Data Services and the Client
You can run the client (A12 Client) and server (A12 Data Services) via the provided run
configurations for IntelliJ and VS Code.
Running the Workflow Engine
In addition to the services available on the main variant of the tutorial app, there is a workflow-
engine module that contains the CIB 7 engine.
To run the engine, you can do any of the following:
• Use the workflow engine start run configuration in IntelliJ or VS Code.
• Run the class WorkflowEngineTutorialApplication from any IDE. In this case, you must set
workflow-engine as the working directory.
• Run gradle :workflow-engine:bootRun from the project root directory.
TIP
When you modify code or resources in workflow-engine, the engine must restart for
changes to take effect.
For automatic restarts via the Spring devtools, run
3

-- 3 of 10 --

gradle :workflow-engine:build --continuous
from the project root directory. This triggers a rebuild on file change, which in turn
triggers a restart of the engine.
TIP
When you create or modify BPMN models, you can deploy them directly from the
Modeler. So no restart is required.
However, be aware that existing process instances will continue running on the
process definition version they were started on.
Manual Testing
After you start a new process instance via the tutorial app frontend, visit http://localhost:8088/
camunda/ to access the CIB 7 Admin UI. This shows the state of process instances, any incidents that
occurred, which process variables exist and more.
NOTE In the tutorial app, you can log into the Cockpit using admin / admin.
For details, read Modeling > Workflow Modeling > CIB 7 Cockpit.
Script Resolving
The BPMN model expects the script at scripts/assignSalesRep.js. The path is relative to the
engine’s resources directory. Thus, you must place the scripts in workflow-
engine/src/main/resources/scripts.
Documentation
For details regarding scripting in CIB 7, read CIB 7 > Scripting.
Troubleshooting
If you encounter unexpected UAA authentication errors (check developer tools console in your
browser), try manually restarting the server and/or engine.
Your task:
• Create a file scripts/assignSalesRep.js in the workflow-engine resources
• Implement sales rep assignment logic. The variable salesRep should get a value according to the
following cases:
◦ If the customer is a VIP, assign a special sales rep, e.g. vip@acme.org
◦ If the customer is a regular customer, assign a general sales rep, e.g. sales@acme.org
◦ If the customer type is suspended or inactive, assign no sales rep (null)
▼ Click to see solution
4

-- 4 of 10 --

Create the JavaScript script file:
File: workflow-engine/src/main/resources/scripts/assignSalesRep.js
Here’s one way to implement a solution in pure JavaScript:
console.log("Customer type: " + customerType);
let salesRep = undefined;
switch (customerType) {
case "vip": {
salesRep = "vip@acme.org";
break;
}
case "suspended":
case "inactive": {
salesRep = null;
break;
}
default: {
salesRep = "sales@acme.org"
}
}
execution.setVariable("salesRep", salesRep);
Note that this task could be solved in a number of different ways in A12. For instance, via DMN
(Decision Model and Notation) or Kernel computations. Here, we choose this solution to
demonstrate the use of script tasks.
Using TypeScript
If you want to use TypeScript, you must tell the compiler that the execution context provides the
used objects and variables:
declare const customerType: string;
declare const execution: ExecutionEntity;
export declare interface ExecutionEntity {
getVariable(variableName: string): any;
setVariable(variableName: string, value: any): void
// If you want to declare more, see
org.cibseven.bpm.engine.impl.persistence.entity.ExecutionEntity
}
// Script task logic as above...
5

-- 5 of 10 --

Next, you must bundle the TypeScript to a standalone executable JavaScript file without any
imports.
For simplicity, in this task, you can use bun and run the following in the scripts directory:
npx bun@1.2.22 build --entrypoint assignSalesRep.ts --outdir . --sourcemap=linked
This generates assignSalesRep.js and assignSalesRep.js.map. The former can be referenced for
execution in the workflow engine. The latter provides mappings between TypeScript and JavaScript
for debugging.
WARNING This does not mean we recommend using bun for productive use cases. We
have not analyzed it thoroughly.
NOTE Alternatively, you can use a more mature tool like esbuild or webpack which require
a build config file.
After implementing the script task, when you trigger an onboarding process, you should see that
the corresponding process instance now moves on to the "Generate new gift card" task that you will
implement in the next task:
NOTE
In this picture, two process instances are started. One is still in the form-based user
task while one has already completed the script task and chosen the "VIP path"
through the process.
Testing Script Tasks
For the Camunda Testing API, refer to the following resources:
• CIB 7 > Testing
• CIB 7 > Start a Process Instance at Any Set of Activities
• Camunda > SimpleTestCase.java template (CIB 7 API is equivalent)
Your task:
• Implement integration tests using the CIB 7 Testing API
• Optionally, create unit tests for your isolated script logic (in your favorite JavaScript or
TypeScript test library)
6

-- 6 of 10 --

▼ Hints
Initializing a Process Engine
For JUnit 5, you can create an in-memory process engine for testing as described at the end of the
JUnit 5 section in the CIB 7 Testing documentation:
public class MyBusinessProcessTest {
public ProcessEngine myProcessEngine = ProcessEngineConfiguration
.createStandaloneInMemProcessEngineConfiguration()
.setJdbcUrl("jdbc:h2:mem:camunda;DB_CLOSE_DELAY=-1")
.buildProcessEngine();
@RegisterExtension
ProcessEngineExtension extension = ProcessEngineExtension.builder()
.useProcessEngine(myProcessEngine)
.build();
}
With this, you can inject a ProcessEngine object into your test methods:
@Test
public void testSomething(ProcessEngine processEngine) {
// Use the process engine for testing
}
Starting a Process Instance
To start an instance of the CustomerOnboardingProcess, you can use the ProcessEngine API:
ProcessInstanceWithVariables processInstance = processEngine.getRuntimeService()
.createProcessInstanceByKey("CustomerOnboardingProcess")
.startBeforeActivity("assignSalesRepActivity")
.setVariableLocal("customerType", customerType)
.executeWithVariablesInReturn();
Using startBeforeActivity, the CIB 7 Testing API allows skipping other process steps and directly
start the activity under test.
Expected process variables at that point can be set via setVariable or setVariableLocal,
depending on whether the variable should be local to the task or a global process instance
variable.
For details, read CIB 7 > Variable Scopes and Variable Visibility.
Use executeWithVariablesInReturn instead of execute to assert on the variable you expect to be set
7

-- 7 of 10 --

by the script task.
▼ Click to see solution
We leave the unit testing in the script language to the reader, this tutorial focuses on the CIB 7
Testing API.
In this solution, we use JUnit 5 and AssertJ.
We already extract the basic setup into an abstract base class:
File: workflow-
engine/src/test/java/com/mgmtp/a12/tutorial/workflow/engine/AbstractProcessEngineTest.java
package com.mgmtp.a12.tutorial.workflow.engine;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.ProcessEngineConfiguration;
import org.cibseven.bpm.engine.test.junit5.ProcessEngineExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
public class AbstractProcessEngineTest {
private final ProcessEngine inMemProcessEngine = ProcessEngineConfiguration
.createStandaloneInMemProcessEngineConfiguration()
.setJdbcUrl("jdbc:h2:mem:cibseven;DB_CLOSE_DELAY=-1")
.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE) //
prevent re-create
.buildProcessEngine();
@RegisterExtension
ProcessEngineExtension extension = ProcessEngineExtension.builder()
.useProcessEngine(inMemProcessEngine)
.build();
}
File: workflow-
engine/src/test/java/com/mgmtp/a12/tutorial/workflow/engine/CustomerOnboardingProcessScriptT
askTest.java
package com.mgmtp.a12.tutorial.workflow.engine;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.cibseven.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
8

-- 8 of 10 --

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
@Deployment(resources = "bpmn/CustomerOnboardingProcess.bpmn")
public class CustomerOnboardingProcessScriptTaskTest extends
AbstractProcessEngineTest {
private static ProcessInstanceWithVariables
executeAssignSalesRepScriptTask(ProcessEngine processEngine, String customerType) {
return processEngine.getRuntimeService()
.createProcessInstanceByKey("CustomerOnboardingProcess")
.startBeforeActivity("assignSalesRepActivity")
.setVariableLocal("customerType", customerType)
.executeWithVariablesInReturn();
}
@Test
void givenVip_shouldAssignVipSalesRep(ProcessEngine processEngine) {
// given
String customerType = "vip";
// when
var processInstance = executeAssignSalesRepScriptTask(processEngine,
customerType);
// then
assertThat(processInstance.getVariables()).contains(entry("salesRep",
"vip@acme.org"));
}
@ParameterizedTest
@ValueSource(strings = { "partner", "lead" })
void givenNonVip_shouldAssignRegularSalesRep(String customerType, ProcessEngine
processEngine) {
// when
var processInstance = executeAssignSalesRepScriptTask(processEngine,
customerType);
// then
assertThat(processInstance.getVariables()).contains(entry("salesRep",
"sales@acme.org"));
}
@ParameterizedTest
@ValueSource(strings = { "inactive", "suspended" })
void givenInactiveOrSuspended_shouldAssignNoSalesRep(String customerType,
ProcessEngine processEngine) {
// when
var processInstance = executeAssignSalesRepScriptTask(processEngine,
customerType);
9

-- 9 of 10 --

// then
assertThat(processInstance.getVariables()).contains(entry("salesRep",
null));
}
}
Recommendations
For script tasks, we recommend the following practices:
• Keep scripts lightweight
◦ Avoid long-running CPU-intensive operations (use external workers instead)
• Test the isolated script logic in a script language test framework of your choice (e.g. vitest for JS,
pytest for Python)
• Test the workflow (incl. script tasks) using CIB 7 testing API in Java
• Do not overuse scripts on listeners. These are hidden in the BPMN model. Instead, use explicit
script tasks where possible.
• Avoid inline scripts (even for simple scripts). These are untested and hard-coupled to the BPMN
model without proper tooling.
Conclusion
You have implemented a simple JavaScript script task that handles sales representative assignment.
Key takeaways:
• Script tasks are a valid choice for fast-running business logic
• Script tasks allow polyglot programming to some extent
◦ However, we recommend minimizing the number of used technologies
• Be sure to test script tasks thoroughly, covering both normal operation and potential error
scenarios
• Keep scripts focused and avoid CPU-intensive operations
If something does not seem right, or you got stuck at any point, you can just check out 2025.06-
ext5/workflows/task-1-end to see differences between both implementations.
« Introduction Task 2: External Workers »
10

-- 10 of 10 --

