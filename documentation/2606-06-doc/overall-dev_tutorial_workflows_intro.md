# overall dev_tutorial_workflows_intro

Workflows Tutorial
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
For this A12 Workflows tutorial, prior knowledge of the following technologies is necessary:
• Java
• Spring Boot
• JUnit 5 and Mockito useful
• Gradle
◦ This tutorial has been tested with Gradle versions 8.12.1 and 8.14.2
You will need the following programs:
• IDE of your choice
• A12 Modeling Environment in version 2025.06-ext5
• Docker (for one of the integration tests)
Overview of A12 Workflows
End Result
After reading this page, you will know:
• The main architecture components and how they interact at a high level.
• The most relevant concepts of CIB 7 (the process engine).
• How an end-to-end user interaction works and which A12 services talk with which other
services.
Brief Introduction
A12 Workflows (A12WF) provides an integration of Business Process Model and Notation (BPMN)
and Decision Model and Notation (DMN) capabilities into A12, enabling both the graphical
modeling and the execution of server-side workflows.
This allows modeling the business-level behavior of entire A12 applications. The focus is on semi-
1

-- 1 of 5 --

automated, long-running workflows that involve complex authorization rules. Such
authorization rules are defined and executed using UAA.
Integrated modeling capabilities include process steps performed by humans and machines, the
connections between them, and business rules triggering different process paths. For automated
process steps, several automations are provided out of the box, e.g., to send an email or to link two
documents.
Custom automations can be implemented against a well-defined API and then used by modelers.
Additionally, custom code can react to provided events such as a task being completed.
All of this is achieved by connecting the third-party CIB 7 process engine to the A12 stack. To this
end, A12 Workflows provides a tailored, A12-aware CIB 7 engine as well as extensions for Data
Services and Client.
Architectural Overview
Understanding CIB 7
A12 Workflows leverages CIB 7 (a Camunda Community Edition fork) as its process engine.
Key Concepts:
• BPMN (Business Process Model and Notation): Standard for modeling business processes
• DMN (Decision Model and Notation): Standard for modeling business decisions
• Element Templates: Reusable, selectable templates for BPMN modeling
• Process Engine: Runtime that executes BPMN processes
For detailed information about BPMN and DMN modeling, see the Workflow Modeling
Documentation and the Workflow Modeling Tutorial.
A12 Workflows Architecture
Key Components:
• Workflows Extension: Registers JSON-RPC operations in Data Services
• Workflows Engine: CIB 7 distribution with A12-specific configurations
• Client Library: Frontend components for workflow interaction
For comprehensive architecture details, see A12 Workflows > Architecture Overview.
A user interaction triggers a chain of events:
2

-- 2 of 5 --

• The user clicks on a button, let’s say to start a process from an overview.
• This dispatches a Redux action, in this case an overview engine action.
◦ In our middleware, we transform this to a WorkflowsActions.startProcess action.
• This action triggers a workflows saga which sends a JSON-RPC request to Data Services, in this
case a START_PROCESS request.
◦ Workflows-related JSON-RPC operations are handled by the Workflows Extension.
• The Workflows Extension performs any necessary data changes in Data Services and sends
requests to the Process Engine, in this example to /engine-rest/process-
definition/key/<PROCESS_DEF_KEY_FROM_ACTION>/start.
• For process start, CIB 7 will start a new process instance and, depending on the process model,
reach a user task. In that case, on the next reload, the new user task will appear in the task list.
The application may also do polling to (almost) immediately open the first user task.
The Customer Onboarding Process
For this tutorial, the following draft workflow is prepared as a starting point:
3

-- 3 of 5 --

The Customer Onboarding Process includes:
• User Task: Create a new customer (form-based)
• Script Task: Assign sales representative
• Service Task (External Worker): Generate a gift card / code
• Service Task (Delegate): Send welcome gift card
• Gateway: Take different workflow paths based on customer type
Project Structure Overview
Let’s briefly review the starting point of this tutorial.
Compared to the plain tutorial application, we made the following relevant changes:
• Added a module workflow-engine which contains the CIB 7 Spring Boot app.
• Added the workflows-extension to the server app module.
◦ Adjusted configuration, primarily UAA config for inter-service communication between Data
Services and the Workflow Engine.
• Extended the appsetup.ts with the middleware, sagas and data loader for A12 Workflows.
• Added the WorkflowsMetadata_DM include to the Contact_DM document model.
◦ Added annotations to fields in the DM that shall be synchronized to the workflow engine
because their values are used as inputs for tasks or decisions.
• Added the draft workflow model CustomerOnboardingProcess.bpmn and all required UI models.
NOTE For a detailed integration guide, see the A12 Workflows
Documentation — Integration.
Summary
A12 Workflows provides a bridge between the process engine (CIB 7) and A12’s document-centric
architecture, allowing you to build sophisticated business processes while leveraging A12’s
modeling capabilities.
4

-- 4 of 5 --

Tutorial Overview
This tutorial focuses on the implementation of automated steps, event handling, and encryption in
workflows.
You will implement a simplified Customer Onboarding Process that demonstrates:
• Script tasks (using JavaScript)
• External workers for asynchronous processing
• Java delegates for complex business logic
• Encryption of workflow variables
• Testing workflow logic and service tasks
This tutorial is structured as follows:
• Task 1: Script Tasks
• Task 2: External Workers
• Task 3: Java Delegates
• Task 4: Process Variable Encryption
Note that there are different solutions to tasks and that this tutorial tends to choose simple
solutions for the sake of clarity.
Feedback
After having completed this tutorial, we would appreciate your thoughts and feedback: Survey.
5

-- 5 of 5 --

