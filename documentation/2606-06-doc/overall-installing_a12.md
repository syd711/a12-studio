# overall installing_a12

Installing A12
The A12 Modeling Environment Installer is a convenient way for getting all
available A12 tools for modelers of A12 models. It could be seen as single source
of truth for receiving matching versions of A12 tools based on A12 overall
releases.
To simplify usage for non technical users, the A12 Installer takes care of loading tool dependencies
and pre-configurations.
A12 Modeling Environment Installer will … A12 Modeling Environment Installer will not …
• download A12 tools based on overall release
• download necessary dependencies
• wire your installed components
• link to documentation
• add shortcuts for components inside
installation folder
• deep integrate into your operating system
(it’s easy to remove - just delete the folder)
• rewire existing components
Included Tools
The A12 Installer is a utility to get the A12 Modeling Environment onto your local machine.
This includes:
• The Simple Model Editor (SME) for editing e.g. Document Models (with validation and
computation rules), Form Models, Relationship Models, Application Models, Overview Models
and Tree Models
• The Camunda Modeler to work with workflows models (bpmn & dmn)
• The Preview App Control to start a local Preview App and see how the models work in a full stack
application
• Our A12 Sample Workspaces ready to use in the Preview App as showcases for what is possible
by modeling or as a starting point for your own model adventures
• A Link to the documentation
The Installer and certain products are platform specific. Please follow the instructions below and in
the Installer itself.
The Installer will put the components of your choice into an A12 release line specific folder.
Download
Download the A12 Modeling Environment Installer on geta12.com.
1

-- 1 of 4 --

Installation
Once you have downloaded the installer for your operating system, you need to extract the
compressed file (zip/tgz).
Figure 1. Extract all (Windows example)
If you have already extracted an installer to the chosen directory, you will be asked if you want to
replace some files. Please select 'Replace' or cancel and choose a different/new folder for the
installer.
Figure 2. Replace existing files (Windows example)
After extraction, you will see multiple items in the folder, including the A12 Installer Application
and the Release Table. Please run the A12 Installer Application.
▼ Windows - Click here to see more information
On Windows, you might see a blue prompt when you run the application, please click on “More
Info” and then “Run anyway”.
2

-- 2 of 4 --

Figure 3. Warning on Windows
▼ MacOS - Click here to see more information
On Mac, accept the dialog that you want to open an app that was downloaded from the internet.
Figure 4. MacOS
▼ Ubuntu - Click here to see more information
In Ubuntu, you have to tell your system how to handle executable script files. Therefore, to use
the shortcut files in installation root by double click you have to set it explicitly. To do so, open:
"Files > Preferences (could be found in the dropdown of files menu bar) > Behavior > Executable
Text Files", then change to "Run them" or "Ask what to do".
The installer will now start and you can select the folder where you want the Modeling
Environment to be installed. Please be sure to choose a folder where you have Read, Write and
Execute permissions for the current user. Have a look at the section, Permissions for more
information. We recommend using a unique name for each installer version, for example “A12
Tools - 2024.06-ext2” for the ext2 version of the 2024.06 release line. Please note the installer will
overwrite old versions of A12 if you choose an existing folder, and you may lose data.
The installer will now install the tools and sample workspaces. This may take a few minutes so
please be patient. Once the installation has finished, a green notification will be shown.
3

-- 3 of 4 --

Permissions
The user running the A12 Installer needs to have Read, Write and Execute permissions for the
selected target directory. This may mean that you need to speak to your IT Admins to install the A12
Tools for you. After the installation, your user profile needs to have Read, Write and Execute
permissions for the folder which the tools are in.
▼ Windows - Click here to see more information
On Windows, it is normally sufficient to install the tools on the C:\ drive but not in Program Files
as only Admin-Users have the required permissions for the Program Files folder.
▼ MacOS - Click here to see more information
If you are a Mac user you can choose any target directory as your user profile will determine if a
folder has Read, Write and Execute permissions.
▼ Ubuntu - Click here to see more information
If you are running Ubuntu you should simply execute the installer with the necessary
permissions and choose a folder (preferably in your home directory).
What if my tools do not open or function correctly?
If you do not have the necessary permissions discussed in the section, Permissions you may need to
change the permissions for your user for the folder where you installed the tools. If this is not
possible, you may need to re-install the tools in a folder where you do have Read, Write and
Execute permissions.
Check out the FAQ Section for more information.
System Requirements
The recommended system requirements for the installation and operation of the A12 Tools are
listed in the following table.
Requirement Recommended
RAM 8 GB
CPU 4 cores, 2.20 GHz
Disk Space 6 GB
Operating System Windows 10, 64-bit
macOS 11
Ubuntu 18
Monitor Resolution 1280 x 720
4

-- 4 of 4 --

