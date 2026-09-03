# overall dev_tutorial_general_information

General Information
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
Introduction
With A12, large parts of an application can be created and maintained by business analysts via
modeling. Depending on the specific use cases however often further customizations are required,
for which a developer is needed. These tutorials are intended as a starting point for development
with A12.
In these tutorials you will learn how to navigate an A12 project, gain a better understanding of how
A12 works and how to interact with the API of different A12 components, to enable you to transfer
this knowledge then to the specific use cases in your project.
Use-Case
These aspects will be shown based on the use case of a small customer relationship management
(CRM) system, to track customer contacts. Of course, the goal is not to have a production ready
application, but to give you an introduction into some core concepts of an A12 application.
Update Policy
The tutorials are available for every supported release line of A12. The specific A12 and Project
Template version is documented at the start of each tutorial.
You can switch to a different version using the version selector in the top left corner of the
documentation page. Note that tutorials are not necessarily available for every ext version listed in
the dropdown.
These minor ext releases of A12 represent different states of the same release line and are therefore
compatible with each other. Since usually the changes within them do not affect the tutorials, they
are generally not updated for every ext release. Whether a tutorial is updated for such a minor or
patch release is decided on a case-by-case basis.
Feedback
Please keep in mind that the development tutorials are a work in progress and are being
continuously updated and extended with new tasks and additional tutorials. If your topic of interest
is not part of a tutorial yet, it might be included in the future.
If you have wishes for future content, find something that does not work as described, or you get
stuck, do not hesitate to let us know as the tutorials get better with your feedback.
For details on reaching our team, please visit our support page on GetA12 or use the feedback
button at the top of the documentation.
1

-- 1 of 6 --

Setup
Prerequisites
The tutorials do not require any previous knowledge of A12. If you are completely new to A12, we
recommend you to take a look at Overall > What Is A12 to get an understanding about basic
concepts and the way it works.
The target audience for these tutorials are developers. Therefore, some prior basic knowledge in
certain technologies is assumed. The specific technologies differ a bit depending on the tutorial and
its focus on frontend and/or backend. You can find a specific list in the "Prerequisites" section of
each tutorial.
Generally, the tutorials also assume you have the following programs available:
• Text editor / IDE
• A12 Modeling Environment in version 2025.06-ext5
Download
The code for the tutorial application is available on GitHub, where you can clone the repository to
have it available locally:
A12 Tutorial Application
IMPORTANT
To follow along with the tutorials, you must use the git tags provided in each
tutorial to check out the correct state of the code. Using the Project Template
on its own is not sufficient, as the tutorial application includes additional
configurations, models, and helper files that are specific to the tutorials and
not part of the Project Template.
Setting up and Configuring the Application
After cloning the repository of the tutorial application, the environment and tools need to be set up
and configured so that you can locally start the application.
As the tutorial application is based on the Project Template, the setup and configuration work the
same.
Therefore, you just need to follow Project Template > Environment and Tools Setup.
You can skip the section about the installation and configuration of Docker, as it is not needed for
the tutorial application.
NOTE
If you already have the Project Template in a compatible version successfully
running, the tutorial application most likely will work out-of-the-box. If you run into
any problems, please go through the instructions linked above again.
2

-- 2 of 6 --

Running the Code
After following the instructions linked above, you can try starting the application by executing the
following steps.
Firstly, you need to navigate to the root directory of the application and build all modules with:
gradle build
NOTE
The Project Template has some code formatting and quality check tools configured
and enabled out-of-the-box, specifically ESLint, Prettier and Checkstyle. These can
cause the build to fail, if you made any changes that are not compliant, e.g. wrong
order of the inputs.
Please see Project Template > Code Formatting and Quality Check, for more
information on how to fix the findings or disable them for development purposes.
You can then run the backend with the following Gradle task:
gradle :server:app:bootrun --args='--spring.profiles.active=dev-env'
There are also other options available to set the Spring profile, and it is possible to run the backend
from the IDE. For more information on this you can take a look at Project Template > Running
Standalone > Backend. All options work for the tutorial application, you just need to make sure to
use the dev-env Spring profile.
Secondly, you need to start the frontend. For that, navigate to the client directory and start it via the
following commands:
cd client
npm start
Please note that the server and client are both long-running jobs, and they will run under their
respective task until you interrupt them with Ctrl + C .
Once they are both running, you can access the application on localhost:8081 with the following
credentials:
• username: admin
• password: A12PT-admintest
For an overview over all ports and available test users, please refer to Project Template > Accessing
the Application.
NOTE
In the Project Template documentation you might have seen some mentions of
Keycloak. However, this is not relevant for the tutorial application as we are using
3

-- 3 of 6 --

the local authentication variant of the Project Template.
Navigation
Structure
The local authentication variant of the Project Template is used as a starting point for the
development tutorials.
In the intro tutorial we will then model the basis of our CRM system and initialize the Project
Template with it. Each further tutorial uses this result as a basis to add new features for additional
CRM functionalities.
We currently have the following tutorials available:
• Intro
• Frontend
• Backend
• Query API
• Workflows
The link between these tutorials is illustrated below:
Navigating the Tutorials and Their Tasks
Each of the tutorials is split into multiple tasks covering various aspects of A12.
The different tutorials can be done independently of each other and every tutorial is structured in a
way that you are able to complete each task individually. For example, if you are only interested in
4

-- 4 of 6 --

one of the later tasks, you can start there.
This is achieved by utilizing the git tag system. For that, the repository of the tutorial application
includes tags corresponding to the starting point and final result of every task in each of the
tutorials for all supported versions of A12.
To begin working on a specific task, you can just check out the corresponding start tag. Similarly,
you can see the solution, e.g. to see where your code differs, by checking out the end tag for that
task.
So by using the provided tags, you can easily choose which tasks you want to work on, depending
on what you already know or are interested in. You can also always go back to a previous task by
checking out the necessary tag.
The syntax of the tag names is always:
<A12_VERSION>/<TUTORIAL_NAME>/task-<NUMBER>-[start/end]
But you can also find the specific tag names for the start and end of each task in its corresponding
documentation.
By cloning the repository, you will already have access to all the tags we provide and you can then
utilize them with the following commands:
• To get a list of all available tags:
git tag
• To view the versions of files a tag is pointing to:
git checkout <tag-name>
◦ To start e.g. the first task in the intro tutorial:
git checkout 2025.06-ext5/intro/task-1-start
◦ To view the end result for e.g. that task:
git checkout 2025.06-ext5/intro/task-1-end
Please be aware that checking out a tag, will put your repository in a "detached HEAD" state, which
means that "HEAD" is now pointing to a commit instead of a branch.
It is intended that after completing a task you check out the next task’s starting tag to continue,
rather than carry forward your own work.
This is necessary as occasionally helper files are provided and also due to the nature of A12, small
5

-- 5 of 6 --

differences in the project files might mean upcoming tasks of the tutorial do not work for you.
You can utilize git stash or if you use IntelliJ IDEA its shelf functionality to switch between different
tags without losing your local changes.
Code Snippets
Code examples are given at various times during the tutorial. These are only snippets and not
indicative of all the code in a given file.
At times a comment such as // … is used to indicate missing lines in order to keep the examples
shorter.
Additionally, the imports and package names are left out of the code snippets by default for better
readability.
In case of any ambiguity, you can take a look at the source code on GitHub to see the full solution.
6

-- 6 of 6 --

