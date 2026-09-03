# overall comprehensive_features_rbac

Modeling Support for Role Based
Access Control
The following chapter guides through the process of "Role Based Access Control"
(RBAC) modeling by modifying the included role mapping within the Modeling
Environment. In summary, to modify the roles present in the sample
workspaces (or in newly created workspaces using the Preview App Control)
and the corresponding modeling support in the SME (see SME Documentation),
the modeler has to:
• Start the User Management Application (Expert Mode of Preview App Control)
Figure 1. Expert Mode of Preview App Control with User Management Application started
• Login as "usermanager" with the password "a12"
• Upload the "roles.yaml" of the workspace in which a change of RBAC is to be done to the "Roles
Management" module
1

-- 1 of 4 --

Figure 2. Roles Management: Upload Role Mapping
• Adapt the roles in the user management module Roles
• Export the Roles/AccessRight mapping file ("roles.yaml")
• Store it in the corresponding workspace and replace the existing one in "\auth\roles.yaml"
• Refresh/reopen the workspace in SME to get modeling support for the changed Roles
• To see the changes in the Preview App the workspace needs to be restarted
Users can be added/modified in a similar manner:
• Login as usermanager
• Upload the "roles.yaml" and "users.yaml" of the workspace to be changed
• Adapt the users in the module Users
• Export the users file ("users.yaml")
• Store it in the corresponding workspace and replace the existing one in "\auth\users.yaml"
User Management Application
Let’s take a look into the modules of the A12 User Management which is included in the User
Management. After starting up the User Management from the Expert Mode of the Preview App
Control, one can log in as "usermanager" with the password "a12" by default. Three modules are
available:
• User Management
2

-- 2 of 4 --

Figure 3. User Management Module
• Role Management
Figure 4. Roles Management Module
• Access Right Management
3

-- 3 of 4 --

Figure 5. Access Rights Management Module
In those three modules only the "usermanager" with the role "userManagementAdmin" can modify
the present data. New Roles making use of the existing Access Rights and according Users can be
created.
TIP The usermanager can not be deleted.
To make use of changes during modeling in the SME, the modeler needs to export the new Access
Rights mapping of the created or modified Roles and make it available during modeling by
replacing the "roles.yaml" within the file system/the workspace. For the installer sample
workspaces the file is located in the "auth" folder, e.g. basic\auth\roles.yaml.
NOTE For newly created users the password to log into the preview app is "a12", like it is
for already existing users.
All Sample Workspaces come with an roles.yaml and a users.yaml and thus a predefined setup for
Users, Roles and Access Rights. Please bear in mind that this is only a basic example and is not
intended to be an actual setup to be used for production purposes.
4

-- 4 of 4 --

