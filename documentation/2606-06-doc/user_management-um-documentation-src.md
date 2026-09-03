# user_management um documentation src

UAA - User Management
Table of Contents
1. Introduction. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 5
2. User Management Common . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
2.1. Overview. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 6
2.2. Getting Started. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
2.3. Custom User Structure. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 7
3. User Management Service . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
3.1. Overview. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
3.2. User Management Service Features Available. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
3.3. User Management Service Business Document Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 8
3.3.1. Advanced mode (User, Role, and AccessRight with Organization Units). . . . . . . . . . . . . . . . . 8
3.3.2. Basic mode (User and Role only, without Organization Units) . . . . . . . . . . . . . . . . . . . . . . . . 10
3.4. User Management Security Applied. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
3.4.1. Service Authentication . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
3.4.2. Service Authorization . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 11
3.5. User Management APIs . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
3.5.1. Public API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
UMUserService . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
UMRoleService. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
UMAccessRightService. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
UserExtensionConverter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 12
UMUserDocumentEventCustomizer. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
Spring event listeners (post-operation hooks) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 13
Oauth2TenantStorage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
UMTenantRegistrationStorage. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
UserTenantAccessService . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 14
AllowedOriginsStorage . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
SearchOptions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 15
UMTenantSelector. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
IDPUserService . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
IDPRoleService. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
IDPUserConverter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 16
IDPUserExtensionConverter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
IIDPExceptionHandler. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
ExceptionKeys / ExceptionMessages . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
3.5.2. REST API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
UserController . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 17
1

-- 1 of 121 --

RoleController . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 22
AccessRightController . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 24
3.5.3. RPC Operations . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
3.5.4. Events . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
UMUserAfterCreateEvent . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
UMUserAfterUpdateEvent . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
UMUserAfterDeleteEvent . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 27
UMSynchronizationTriggerEvent . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
UMAfterSynchronizationEvent . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
UMTenantRemoveEvent . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 28
3.6. Data Flow Diagrams. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
3.6.1. DataServices RPC Operations. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
3.6.2. User Management REST API Flow. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 29
3.7. Getting Started With Local . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 30
3.8. Getting Started With OAuth2 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 31
3.8.1. Prerequisites . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 31
3.9. Static Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 33
3.9.1. Configuration Properties. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 33
Tenant registration (mgmtp.a12.uaa.user-management.tenant-registration.[{key}].*) . . . . . . 33
User Management general settings (mgmtp.a12.uaa.user-management.um.*) . . . . . . . . . . . . . . . . 34
User document properties (mgmtp.a12.uaa.user-management.um.user-document-
properties.*) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 34
Data multi-tenancy settings (mgmtp.a12.uaa.user-management.um.multi-tenant.*) . . . . . . . . . . 35
3.10. Configuration Profiles . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
3.10.1. um_uaa — UAA defaults . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 36
3.10.2. um_advanced — Organization unit, role, and access-right support . . . . . . . . . . . . . . . . . . 36
3.10.3. um_basic — User and role only. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
3.10.4. um_oauth2 — OAuth2 / JWT authentication . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
3.10.5. um_tenant — Data multi-tenancy. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 37
3.11. Dynamic Authentication Providers . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 38
3.11.1. Token Verification Flow . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 38
3.11.2. Configuration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 39
3.11.3. Seeding Providers from Properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 39
3.11.4. Registering Providers at Runtime. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
3.12. Data Multi-tenancy. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
3.12.1. Accessible tenants vs. active tenant . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 40
3.12.2. Super-admin support. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
3.12.3. Tenant-scoped document fields. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 41
3.12.4. Configuration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 42
3.12.5. Tenant registration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 42
3.13. Cluster Support . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 42
2

-- 2 of 121 --

3.14. Transaction within User Management . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
3.14.1. Transaction from User Management to Keycloak . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Before create user. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
After create user . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Before update user . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
After update user . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 43
Before delete user . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
After delete user . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
3.14.2. Transaction from Keycloak to User Management . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
3.15. Actuator properties . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 44
3.16. How to Customize the User Document Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
3.16.1. Step 1: Update DomainUserManagement.json . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 45
3.16.2. Step 2: Extend UMUserRepresentation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
3.16.3. Step 3: Implement UserExtensionConverter . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 47
3.16.4. Step 4: Register the Supplier bean . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 48
3.17. How to Customize the Default Behavior. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 48
3.17.1. Customize Representation Conversion . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 48
3.17.2. Customize Pre-Write Behavior (UMUserDocumentEventCustomizer) . . . . . . . . . . . . . . . . 49
3.17.3. Customize Document Validation . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 50
3.17.4. Customize IDP Conversion . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 51
3.17.5. Handle IDP Exceptions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 52
3.18. How to migrate current documents to new business models . . . . . . . . . . . . . . . . . . . . . . . . . . . . 52
4. User Management REST Client . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 53
4.1. Overview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 53
4.2. Getting Started. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 53
4.3. Configuration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 54
4.4. Read Caching Support . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 55
4.4.1. Enable . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 55
4.4.2. Flush the cache . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 55
5. User Management Tool . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 56
6. Keycloak . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 57
6.1. Getting Started. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 58
6.2. Keycloak clients configure . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 58
6.2.1. Access to clients. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
6.2.2. Generate the client secret for uaa-auth-client . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
6.2.3. Update URI for user_management_spa_client . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
6.3. Create user-management-service admin user. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 59
6.4. Open Security Issues . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
6.4.1. Host Header Injection . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
What is the Host Header injection? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
How dangerous are Host Header Injection? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
3

-- 3 of 121 --

How to avoid Host Header Injection? . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
6.4.2. SMTP Server . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
7. Bidirectional Synchronization between User Management Service and Keycloak . . . . . . . . . . . . . . 60
7.1. Overview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
7.1.1. Enabling synchronization. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 60
7.1.2. Diagram. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 61
7.2. Technical users . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 62
7.3. How to Deploy the User Management Keycloak Plugin . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
7.3.1. Prepare the plugin JAR file . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
7.3.2. Start Keycloak . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
7.3.3. Enable Events Listener . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
7.3.4. Set Up Data Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
8. User Management Keycloak Plugin . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
8.1. Overview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
8.2. Getting Started. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 63
8.3. Supported Events . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 64
8.4. Environment Variables . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 64
8.5. Plugin Configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 64
8.5.1. File-based configuration (DISK mode) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 65
8.5.2. REST API configuration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 66
9. User Management Module . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 67
9.1. Overview . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 67
9.2. API . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 67
9.2.1. Full Models . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 67
9.2.2. User Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 68
9.2.3. AUTH_KEYS. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 68
9.3. How to integrate with A12 Client . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 68
9.4. How to allow A12 Client works with multiple realms . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 69
10. Other Resources . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 71
11. Migration Instructions . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
11.1. 2026.06 . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
11.1.1. Refactoring Cutover: uaa-user-management → uaa-usermanagement . . . . . . . . . . . . . . . 72
Cutover Model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
What Did Not Change. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
Breaking Changes at a Glance . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 72
Estimated Migration Effort . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 73
Compatibility Matrix . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 74
How This Chapter Is Organized. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 74
Module-Level Migration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 74
Class-Level Migration. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 77
Configuration Properties Migration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 95
4

-- 4 of 121 --


This documentation belongs to an A12 Enterprise Component which is not part of
the Open Source offering (A12 Community Edition). Please feel free to browse the
documentation and learn more about how you can use this A12 component in
your project. Learn more about the benefits from an A12 Enterprise Subscription
on the Editions & Licensing page.
1. Introduction
UAA User Management is a library for managing users.
It can be used as:
• stand-alone (A12 server application): Only models belonging to User Management are stored in
the database.
• embedded (A12 data service): The business and user models are stored in the same database.
It allows bidirectional synchronization of users with Keycloak by default.
It provides an innovative UI to overcome the disadvantages of the IDP (Keycloak) and extends other
user management features.
It is divided into several modules
• uaa-usermanagement-common - Shared DTOs (UMUserRepresentation, UMRoleRepresentation,
UMAccessRightRepresentation), constants, events, TenantContext, and storage interfaces.
• uaa-usermanagement-service - Core service layer: REST controllers, service interfaces,
document validators, storage, and auto-configuration.
• uaa-usermanagement-idp - IDP abstraction layer defining IDPUserService, IDPRoleService, and
converter interfaces that IDP implementations must satisfy.
• uaa-usermanagement-keycloak - Keycloak implementation of the IDP interfaces.
• uaa-usermanagement-keycloak-plugin - Keycloak server-side plugin (SPI) for event-based
bidirectional synchronization.
• uaa-usermanagement-rest-client-api - REST client interface module (UMUserRestClient,
UMRoleRestClient, UMAccessRightRestClient).
• uaa-usermanagement-rest-client - HTTP implementation of the REST client API.
• uaa-user-management-module - A12 JavaScript/TypeScript UI module (unchanged).
REST API Migration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 103
Authorization Migration . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 107
Multi-Tenancy (New Concept) . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 111
Step-by-Step Migration Procedure . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 115
11.1.2. Legacy uaa-user-management (in case it is still available in 2026 release) . . . . . . . . . . . 120
User model . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 120
5

-- 5 of 121 --

 User Management Service extends A12 DataServices and uses the Kernel library
for data validation. Understanding how DataServices works is recommended —
see A12 Kernel Docs.
2. User Management Common
The uaa-usermanagement-common module provides the shared representation POJOs used across all
User Management modules. These classes are used as data transfer objects when working with User
Management APIs (e.g., CRUD operations via UMUserService or the REST clients).
The base representation can be extended per project to carry custom fields.
2.1. Overview
The following class diagram illustrates the representation class hierarchy and their properties.
Figure 1. User class diagram
6

-- 6 of 121 --

2.2. Getting Started
Add the following dependency to your POM:
<dependency>
<groupId>com.mgmtp.a12.uaa</groupId>
<artifactId>uaa-usermanagement-common</artifactId>
<version>10.0.1</version>
</dependency>
 Organization units, roles, and access rights are not supported by default. Enable
them with mgmtp.a12.uaa.user-management.um.user-document-
properties.organization-unit-role-structure.enabled=true.
2.3. Custom User Structure
To add project-specific fields, create a class that extends UMUserRepresentation and register a
Supplier<T> bean for the subtype together with a UserExtensionConverter<T> bean.
public class ProjectUser extends UMUserRepresentation {
private String department;
private String costCenter;
// getters and setters ...
}
Register the subtype supplier and converter in a Spring configuration class:
@Configuration
public class ProjectUserConfiguration {
@Bean
public Supplier<ProjectUser> projectUserSupplier() {
return ProjectUser::new;
}
@Bean
public UserExtensionConverter<ProjectUser> projectUserConverter() {
return new ProjectUserExtensionConverter();
}
}
See How to Customize the User Document Model for a complete walkthrough.
7

-- 7 of 121 --

3. User Management Service
3.1. Overview
uaa-usermanagement-service is the core service layer of UAA User Management. It extends A12
DataServices for the user management domain.
3.2. User Management Service Features Available
• Fast and effective user search.
• Default Document, Form, and Overview models are provided out of the box.
• CRUD operations for User (UMUserService<T>), Role (UMRoleService), and AccessRight
(UMAccessRightService).
• Bidirectional user and role synchronization with Keycloak via IDPUserService/IDPRoleService.
• Export users as a YAML file (GET /user/export).
• Import users from a YAML file (PUT /user/upload).
• Export the role-to-access-right mapping as a YAML file (GET /role/accessRightMapping).
• Import a role-to-access-right mapping from a YAML file (PUT /role/accessRightMapping/upload).
• Clone an existing role.
• Rename an existing role (when IDP data synchronization is disabled).
• Extensible user data model via UserExtensionConverter<T> and UMUserRepresentation subtyping.
• Data multi-tenancy: per-tenant scoping of users, roles, and access rights (see Data Multi-
tenancy).
• Dynamic authentication providers: verifying JWTs from multiple OAuth2 issuers at runtime (see
Dynamic Authentication Providers).
3.3. User Management Service Business Document
Models
User Management supports two different user structures, selected by the organization-unit-role-
structure.enabled property.
3.3.1. Advanced mode (User, Role, and AccessRight with Organization Units)
Enable advanced mode with:
mgmtp.a12.uaa.user-management.um.user-document-properties.organization-unit-role-
structure.enabled: true
• Document Model Structure
8

-- 8 of 121 --

1. User is managed by the document model DomainUserManagement.json.
2. Role is managed by the document model Role-DM.json.
3. AccessRight is managed by the document model AccessRight-DM.json.
4. The Role-to-AccessRight relationship is managed by the relationship model Role-
AccessRight-RM.json.
5. Organization units provide additional organizational context for projects.
6. A role can optionally belong to a Client.
• Prepare your Organization Unit
User Management Service does not introduce any models or documents relating to
Organization Units; you need to manage these documents and models on your side.
To allow User Management Service to display Organization Unit documents, follow these steps:
1. In the header of your document model, provide an annotation uaa_um_orga_unit_type with
value true.
2. Select a field to display in the dropdown element and provide an annotation
uaa_um_orga_unit_label with value name.
• Data Assignment
1. Access right assignment to a role is done by selecting/unselecting elements.
2. Role assignment to a user is done by a dropdown element.
3. Organization unit assignment to a user is done by a dropdown element.
4. Client assignment to a role is done by a dropdown element [OPTIONAL].
• Prepare your Client (Optional):
User Management Service does not introduce any models or documents relating to Client; you
need to manage these documents and models on your side.
To allow User Management Service to display Client documents, follow these steps:
1. In the header of your Client document model, provide an annotation
uaa_um_client_role_document with value true.
2. Select a field to display in the dropdown element and provide an annotation
uaa_um_client_name_label with value name.
See more at User Management Service Configuration.
9

-- 9 of 121 --


1. Access rights are managed by User Management Service, not by the IDP.
2. Due to https://github.com/keycloak/keycloak/issues/23199, assignment of a
bundle role selection is not possible, but this does not affect single role
selection assignment.
3.3.2. Basic mode (User and Role only, without Organization Units)
With mgmtp.a12.uaa.user-management.um.user-document-properties.organization-unit-role-
structure.enabled=false (the default), the model uses a simplified two-entity structure — basic
mode.
• Document Model Structure
User is managed by the document model DomainUserManagement.json.
• Data Assignment
Role assignment to a user is done by a text-box element.
See more at User Management Service Configuration.
10

-- 10 of 121 --

3.4. User Management Security Applied
3.4.1. Service Authentication
User Management Service supports OAuth2 (JWT) as the default authentication type, and a local
authentication mode for development.
Authentication is handled by UAA. Refer to the appropriate section of the UAA documentation.
3.4.2. Service Authorization
User Management ships authorization definition files (in uaa-usermanagement-workspaces) that
secure its REST APIs. Integrate them using:
mgmtp.a12.uaa.authorization.child-authorization-definitions=...
 If you have a custom mgmtp.a12.uaa.authorization.child-authorization-
definitions configuration, ensure the User Management authorization JSON files
are included alongside yours.
The following authorization scopes are enforced by Spring Security @PreAuthorize guards on the
REST controllers:
Scope name Description
Create User Check that the current user has the proper role to create a user.
Read User Check that the current user has the proper role or that the requested user
matches the logged-in user (self-read).
Update User Check that the current user has the proper role to update a user.
Delete User Check that the current user has the proper role to delete a user.
Export User Check that the current user has the proper role to export users as YAML.
Import User Check that the current user has the proper role to import users from YAML.
Create Role Check that the current user has the proper role to create a role.
Read Role Check that the current user has the proper role to read roles.
Update Role Check that the current user has the proper role to update a role.
Delete Role Check that the current user has the proper role to delete a role.
Export
AccessRight
Check that the current user has the proper role to export the access-right
mapping as YAML.
Import
AccessRight
Check that the current user has the proper role to import the access-right
mapping from YAML.
The authorization layer uses data multi-tenancy to filter data by the active tenant.
11

-- 11 of 121 --

3.5. User Management APIs
3.5.1. Public API
UMUserService
com.mgmtp.a12.uaa.usermanagement.service.UMUserService<T extends UMUserRepresentation>
Provides methods to manage users via UMUserRepresentation (or a project-specific subtype):
create(T), get(…), update(T), delete(…), search(QueryRoot), and simpleSearch(SearchOptions).
Two implementations are provided and selected at runtime via the @ServiceType qualifier:
• @ServiceType(BACKEND_AUTHENTICATION) — bypasses user-facing authorization checks; used for
system-to-system calls.
• @ServiceType(NON_BACKEND_AUTHENTICATION) — enforces the full authorization scope checks; used
in the request path.
UMRoleService
com.mgmtp.a12.uaa.usermanagement.service.UMRoleService
Provides methods to manage roles via UMRoleRepresentation: create, get, update, delete,
search(QueryRoot), and simpleSearch(SearchOptions).
UMAccessRightService
com.mgmtp.a12.uaa.usermanagement.service.UMAccessRightService
Provides methods to manage access rights via UMAccessRightRepresentation. Only available when
mgmtp.a12.uaa.user-management.um.user-document-properties.organization-unit-role-
structure.enabled=true.
UserExtensionConverter
com.mgmtp.a12.uaa.usermanagement.converter.UserExtensionConverter<T extends
UMBaseRepresentation>
The single SPI for all project-level customization of the user representation. The interface defines:
• DocumentV2 convert(T representation, DocumentV2 document) — invoked when writing: enriches
the document already converted by the UM Rest Client with extension-specific fields before
persistence or update.
• void convert(DocumentV2 document, T representation) — invoked when reading: populates the
supplied representation (already converted by the UM Service Converter) with extension fields
read from the persistence document.
• default Object toExportData(List<T> users) — optional. Return a custom object to be YAML-
serialized on export. Returning null falls back to the built-in export format.
• default List<T> fromImportData(InputStream inputStream) — optional. Return a list of
representations parsed from the uploaded stream. Returning null falls back to the default YAML
12

-- 12 of 121 --

deserialization.
Implement and register this interface as a Spring bean to carry custom fields through persistence,
IDP sync, and import/export operations. See How to Customize the User Document Model and How
to Customize Representation Conversion for complete examples.
UMUserDocumentEventCustomizer
com.mgmtp.a12.uaa.usermanagement.event.UMUserDocumentEventCustomizer
A Spring bean interface for pre-operation hooks on user documents:
• DocumentV2 customizeBeforeCreateUser(DocumentV2 userDocument) — invoked just before a new
user document is persisted.
• DocumentV2 customizeBeforeUpdateUser(DocumentV2 userDocument) — invoked just before an
existing user document is updated.
• DocumentV2 customizeBeforeDeleteUser(DocumentV2 userDocument) — invoked just before a user
document is deleted.
Register a Spring bean implementing this interface to inject project logic before any write
operation.
Spring event listeners (post-operation hooks)
After a write completes (including IDP synchronization), User Management publishes the following
Spring events. Register a @EventListener or @TransactionalEventListener bean to react to them.
UMUserAfterCreateEvent — published after a user is created.
UMUserAfterUpdateEvent — published after a user is updated.
UMUserAfterDeleteEvent — published after a user is deleted.
All three are Java records in com.mgmtp.a12.uaa.usermanagement.event. Each carries:
• userDataServicesDocument — the persisted DataServicesDocument envelope (carries A12
DataServices metadata in addition to the document payload).
• latestDocument — the latest persisted DocumentV2 payload (i.e., the version that ended up in
DataServices after all pre-write customizers and validators ran).
@Component
public class ProjectUserListener {
@EventListener
public void onUserCreated(UMUserAfterCreateEvent event) {
DocumentV2 latestDocument = event.latestDocument();
// project-specific post-create logic
}
}
13

-- 13 of 121 --

Oauth2TenantStorage
com.mgmtp.a12.uaa.usermanagement.storage.Oauth2TenantStorage
Provides the OAuth2 / JWT configuration for each registered OAuth2 tenant (issuer URI, JWKs URI,
algorithms). Used by DynamicJWTDecoder to verify tokens at runtime without a service restart.

OAuth2 tenant vs. data tenant: the OAuth2 tenant concept managed by this
storage is aligned with multiple authentication providers (one entry per JWT
issuer / realm). It is distinct from the data tenant concept managed by
UMTenantRegistrationStorage, which scopes the User Management business data
(documents, synchronization configuration) per tenant.
Use storeTenant(Tenant) or removeTenant(Tenant) to add or remove an OAuth2 tenant at runtime.
Use getTenantByIssuer(String issuer) to resolve a tenant by JWT issuer URI; use loadAll() to
enumerate all registered tenants.
By default, User Management seeds the storage from the UAA authentication properties:
mgmtp.a12.uaa.authentication.oauth2.resourceserver.tenants[*].*
 The loadAll method is invoked on every incoming request for token verification.
Ensure your implementation returns cached data and does not fetch directly from
the database or file system on each call to avoid performance issues.
UMTenantRegistrationStorage
com.mgmtp.a12.uaa.usermanagement.storage.UMTenantRegistrationStorage
Maps tenant IDs to their runtime configuration (management config and synchronization config —
see UserManagementProperties.UMTenant). Used by the service layer to resolve the active data tenant’s
context.
Use storeTenant(String tenantId, UMTenant tenant) or removeTenant(String tenantId) to add or
remove a tenant registration at runtime. Use loadTenant(String tenantId) to look up a single
registration; use loadAll() to enumerate all registrations; use isEmpty() to check whether any
tenant is registered.
By default, User Management seeds the storage from:
mgmtp.a12.uaa.user-management.tenant-registration.[*].*
UserTenantAccessService
com.mgmtp.a12.uaa.usermanagement.service.UserTenantAccessService
Provides the logic that decides which tenants the currently authenticated user may manage and
whether the user is a super-admin. It is used by TenantContextInterceptor to populate TenantContext
14

-- 14 of 121 --

on every request.
public Set<String> getAccessibleTenantKeys(Authentication authentication);
public boolean isSuperAdmin(Authentication authentication);
• getAccessibleTenantKeys(Authentication) reads the JWT claim configured by
mgmtp.a12.uaa.user-management.um.multi-tenant.tenant-claim-name (default tenants) from the
authenticated principal and returns its values, split on , and trimmed, as the set of accessible
tenant keys. The claim is therefore the option that controls which tenants the user can manage.
If the claim is absent, the set is empty.
• isSuperAdmin(Authentication) returns true when the authenticated username appears in
mgmtp.a12.uaa.user-management.um.multi-tenant.super-admin-usernames, or when any of the
authenticated user’s granted authorities (with the ROLE_ prefix stripped) appears in
mgmtp.a12.uaa.user-management.um.multi-tenant.super-admin-roles.
A project can supply a Spring bean overriding this service to implement custom tenant-access rules
(for example, reading accessibility from an external directory). See Data Multi-tenancy for the
surrounding flow.
AllowedOriginsStorage
com.mgmtp.a12.uaa.usermanagement.storage.AllowedOriginsStorage
Controls which origins are allowed for CORS. Use storeOrigin(String origin) or
removeOrigin(String origin) to allow or disallow requests from a specific origin at runtime; use
loadAll() to enumerate all allowed origins.
By default, allowed origins are seeded from:
mgmtp.a12.uaa.user-management.um.dynamic-cors-allowed-origins-config-
support.enabled=true
mgmtp.a12.uaa.authentication.cors.allowed-origins=...
 If you provide a custom implementation, loadAll should not fetch data directly
from the database or file system in order to avoid performance issues.
SearchOptions
com.mgmtp.a12.uaa.usermanagement.SearchOptions
A simple POJO used by the simple-search endpoints (/user/simple-search, /role/simple-search,
/accessRight/simple-search). It expresses a filter-based search without requiring callers to assemble
a full A12 DataServices QueryRoot.
15

-- 15 of 121 --

Field Type Defa
ult
Description
filters List<Filt
er>
[] One or more field-path filters that must all match (AND-combined).
Each filter has a path (the document field path to match, e.g.,
/user/firstName) and a value (the value to look for;
substring/contains match).
ignoreCase boolean false When true, value comparison is case-insensitive.
fullTextSe
arch
boolean false When true, the value is treated as a full-text query against the
document; the path of each filter is ignored.
The nested Filter type has two fields:
Fiel
d
Type Description
path Strin
g
The document field path (e.g., /user/firstName, /role/name). For the user resource,
paths configured via UMUserDocumentProperties are supported.
valu
e
Strin
g
The value to match. Comparison rules depend on ignoreCase and fullTextSearch.
UMTenantSelector
com.mgmtp.a12.uaa.usermanagement.storage.UMTenantSelector
Resolves the active data tenant at runtime given the current request context. Implement this
interface to provide a custom tenant-resolution strategy (e.g., from a header, a JWT claim, or a
subdomain).
IDPUserService
com.mgmtp.a12.uaa.usermanagement.service.IDPUserService<T extends UMUserRepresentation, ID>
The abstraction layer for IDP user operations (create, update, delete, and get). The default
implementation is provided by uaa-usermanagement-keycloak.
IDPRoleService
com.mgmtp.a12.uaa.usermanagement.service.IDPRoleService<T extends UMRoleRepresentation, ID>
The abstraction layer for IDP role operations. The default implementation is provided by uaa-
usermanagement-keycloak.
IDPUserConverter
com.mgmtp.a12.uaa.usermanagement.converter.IDPUserConverter<T>
Defines the core mapping from the persisted DocumentV2 user document to the IDP user
representation of type T (for Keycloak: org.keycloak.representations.idm.UserRepresentation). T is
therefore the IDP user type — not the project user POJO.
16

-- 16 of 121 --

public interface IDPUserConverter<T> {
T toIDPUser(DocumentV2 document);
}
The default Keycloak implementation KeycloakUserConverter implements
IDPUserConverter<UserRepresentation> is provided by uaa-usermanagement-keycloak. Projects may
override it.
IDPUserExtensionConverter
com.mgmtp.a12.uaa.usermanagement.converter.extension.IDPUserExtensionConverter<T>
Optional project-level SPI invoked by IDPUserConverter<T> after the core mapping to apply
additional field mappings (e.g., syncing custom extension attributes to Keycloak user attributes). T
matches the IDP user type used by the surrounding IDPUserConverter<T> (typically Keycloak’s
UserRepresentation).
public interface IDPUserExtensionConverter<T> {
void convert(DocumentV2 userDocument, T idpUser);
}
IIDPExceptionHandler
com.mgmtp.a12.uaa.usermanagement.exception.IIDPExceptionHandler
Allows project code to handle IDP-side exceptions. Register a Spring bean implementing this
interface to intercept and react to IDP errors during synchronization.
ExceptionKeys / ExceptionMessages
Standard exception-key constants and message templates for User Management exceptions. Extend
com.mgmtp.a12.dataservices.exception.ExceptionKeys for DataServices-integrated exception
handling.
3.5.2. REST API
User Management exposes REST endpoints for managing users, roles and access rights. In the basic
mode, roles and access rights apis are not available.
UserController
com.mgmtp.a12.uaa.usermanagement.rest.UserController
Provides CRUD and search endpoints for users.
Create user
Name Create a user.
17

-- 17 of 121 --

Description Creates a new user from the supplied representation.
Method POST
Url /user
Headers Content-Type: application/json
Accept: application/json
Request Body
{
"id": "fake-user-id",
"username": "stanford83",
"firstName": "Lizzie",
"lastName": "Abbott",
"email": "dallas_dare@gmail.com",
"enabled": false,
"roles": ["userManagementAdmin"],
"extension": {
"department": "Beauty",
"job_title": "Regional Intranet Officer"
}
}
Parameters
Notes id and username are required. Extension fields are populated only when a
UserExtensionConverter<T> bean is registered.
Read user
Name Read a user.
Description Loads a user by docRef or id. Either parameter is required (if both are supplied,
docRef takes precedence).
Method GET
Url /user?docRef={docRef}
/user?id={id}
Headers Accept: application/json
Parameters docRef: User document reference (e.g., DomainUserManagement/{uuid}) — optional.
id: User document ID — optional.
Notes Either docRef or id must be supplied.
Update user
Name Update a user.
Description Updates an existing user.
Method PUT
Url /user
18

-- 18 of 121 --

Headers Content-Type: application/json
Accept: application/json
Request Body
{
"id": "ce31206d-7aab-4fdc-aa6b-3320936c3e20",
"username": "stanford83",
"firstName": "Brycen",
"lastName": "Rohan",
"email": "angelina52@yahoo.com",
"enabled": true,
"roles": ["userManagementAdmin"],
"extension": {
"department": "Books",
"job_title": "Legacy Research Specialist"
}
}
Parameters
Notes id and username are required.
Delete user
Name Delete a user.
Description Deletes a user by document reference. Users cannot delete themselves.
Method DELETE
Url /user?docRef={docRef}
Headers Accept: */*
Parameters docRef: User document reference (required).
Notes
Search users (QueryRoot)
Name Search users with a A12 DataServices query.
Description Returns users matching the supplied A12 DataServices QueryRoot query DSL.
Method POST
Url /user/search
Headers Content-Type: application/json
Accept: application/json
Request Body A QueryRoot JSON document. Body is optional; an empty/absent body returns all
users.
Parameters
19

-- 19 of 121 --

Notes Use this endpoint when you need the full expressiveness of the A12 DataServices
query DSL (filters, ordering, projections). For simple filter-based searches, prefer
the /user/simple-search endpoint.
Simple-search users (SearchOptions)
Name Search users by field-path filters.
Description Returns users matching the supplied SearchOptions.
Method POST
Url /user/simple-search
Headers Content-Type: application/json
Accept: application/json
Request Body
{
"filters": [
{ "path": "/user/firstName", "value": "Brycen" },
{ "path": "/user/email", "value": "@yahoo.com" }
],
"ignoreCase": true,
"fullTextSearch": false
}
Parameters
Notes See SearchOptions for the field semantics.
Export users as YAML
Name Export users.
Description Downloads all users as a YAML file.
Method GET
Url /user/export
Headers Accept: application/x-yaml
Parameters
Notes Requires the Export User authorization scope.
Import users from YAML
Name Import users.
Description Uploads and imports a YAML file of users.
Method PUT
Url /user/upload
Headers Content-Type: multipart/form-data
20

-- 20 of 121 --

Parameters file: YAML file
Notes Requires the Import User authorization scope.
Example input file:
users:
- username: user_upload_1
firstname: user-management
lastname: tom
email: tom@mgm-tp.com
enabled: true
password: DefaultPassword@Um1
salt: a641c880e9b55316f3b815b0e89ad01f
authorities:
- guest
extension:
companyName: Company Name 5
department: Department 5
job_title: Job Title 5
- username: user_upload_2
firstname: user-management-1
lastname: jerry
email: jerry@mgm-tp.com
enabled: true
roles:
- name: guest
accessrights:
- name: MODEL_READ
extension:
companyName: Company Name 5
- username: user_upload_3
firstname: user-management
lastname: Test
email: test@mgm-tp.com
enabled: true
organizationunits:
- id: Project-DM/6b5cc2ba-14fd-4f15-9fe6-e609032f7724
roles:
- name: admin-1
- id: Project-DM/964e3e55-d99f-4b9e-8cfd-7669f42376ef
roles:
- client: uaa-auth-client
name: Reviewer
accessrights:
- name: MODEL_READ
authorities:
- uaa-auth-client:Reviewer
- admin-2
21

-- 21 of 121 --

RoleController
com.mgmtp.a12.uaa.usermanagement.rest.RoleController
Provides CRUD and search endpoints for roles, plus import/export endpoints for the role-to-access-
right mapping.
Create role
Name Create a role.
Description Creates a new role from the supplied representation.
Method POST
Url /role
Headers Content-Type: application/json
Accept: application/json
Request Body
{
"id": "{uuid}",
"name": "admin",
"description": "Administrator role"
}
Parameters
Notes name is required and must be unique.
Update role
Name Update a role.
Description Updates an existing role.
Method PUT
Url /role
Headers Content-Type: application/json
Accept: application/json
Request Body
{
"id": "{uuid}",
"name": "admin",
"description": "Updated administrator role"
}
Parameters
Notes name is required.
22

-- 22 of 121 --

Read role
Name Read a role.
Description Loads a role by docRef or id. Either parameter is required (if both are supplied,
docRef takes precedence).
Method GET
Url /role?docRef={docRef}
/role?id={id}
Headers Accept: application/json
Parameters docRef: Role document reference — optional.
id: Role document ID — optional.
Notes Either docRef or id must be supplied.
Delete role
Name Delete a role.
Description Deletes a role by document reference.
Method DELETE
Url /role?docRef={docRef}
Headers Accept: */*
Parameters docRef: Role document reference (required).
Notes
Search roles (QueryRoot)
Name Search roles with a A12 DataServices query.
Description Returns roles matching the supplied QueryRoot.
Method POST
Url /role/search
Headers Content-Type: application/json
Accept: application/json
Request Body A QueryRoot JSON document. Body is optional.
Parameters
Notes See /role/simple-search for filter-based searches.
Simple-search roles (SearchOptions)
Name Search roles by field-path filters.
Description Returns roles matching the supplied SearchOptions.
Method POST
23

-- 23 of 121 --

Url /role/simple-search
Headers Content-Type: application/json
Accept: application/json
Request Body
{
"filters": [
{ "path": "/role/name", "value": "admin" }
],
"ignoreCase": true,
"fullTextSearch": false
}
Parameters
Notes
Export role-to-access-right mapping
Name Export the role-to-access-right mapping.
Description Downloads the role-to-access-right mapping as a YAML file.
Method GET
Url /role/accessRightMapping
Headers Accept: application/x-yaml
Parameters
Notes Requires the Export Role AccessRight Mapping authorization scope.
Import role-to-access-right mapping
Name Import the role-to-access-right mapping.
Description Uploads and applies a role-to-access-right mapping YAML file.
Method PUT
Url /role/accessRightMapping/upload
Headers Content-Type: multipart/form-data
Parameters file: YAML mapping file.
Notes Requires the Import Role AccessRight Mapping authorization scope.
AccessRightController
com.mgmtp.a12.uaa.usermanagement.rest.AccessRightController
Provides CRUD and search endpoints for access rights. Only available when mgmtp.a12.uaa.user-
management.um.user-document-properties.organization-unit-role-structure.enabled=true.
24

-- 24 of 121 --

Create access right
Name Create an access right.
Description Creates a new access right from the supplied representation.
Method POST
Url /accessRight
Headers Content-Type: application/json
Accept: application/json
Request Body
{
"id": "{uuid}",
"name": "MODEL_READ",
"description": "Read the data model"
}
Parameters
Notes name is required and must be unique.
Update access right
Name Update an access right.
Description Updates an existing access right.
Method PUT
Url /accessRight
Headers Content-Type: application/json
Accept: application/json
Request Body
{
"id": "{uuid}",
"name": "MODEL_READ",
"description": "Updated description"
}
Parameters
Notes
Read access right
Name Read an access right.
Description Loads an access right by document reference.
Method GET
Url /accessRight?docRef={docRef}
Headers Accept: application/json
25

-- 25 of 121 --

Parameters docRef: Access-right document reference (required).
Notes
Delete access right
Name Delete an access right.
Description Deletes an access right by document reference.
Method DELETE
Url /accessRight?docRef={docRef}
Headers Accept: */*
Parameters docRef: Access-right document reference (required).
Notes
Search access rights (QueryRoot)
Name Search access rights with a A12 DataServices query.
Description Returns access rights matching the supplied QueryRoot.
Method POST
Url /accessRight/search
Headers Content-Type: application/json
Accept: application/json
Request Body A QueryRoot JSON document. Body is optional.
Parameters
Notes
Simple-search access rights (SearchOptions)
Name Search access rights by field-path filters.
Description Returns access rights matching the supplied SearchOptions.
Method POST
Url /accessRight/simple-search
Headers Content-Type: application/json
Accept: application/json
26

-- 26 of 121 --

Request Body
{
"filters": [
{ "path": "/accessRight/name", "value": "MODEL_READ" }
],
"ignoreCase": true,
"fullTextSearch": false
}
Parameters
Notes
 Organization units are externally owned documents — User Management does not
provide create/update/delete endpoints for them. Assign them to users via the
dropdown in the UI.
3.5.3. RPC Operations
User Management also supports A12 DataServices JSON-RPC operations for user documents:
ADD_USER, DELETE_USER, MODIFY_USER, LIST_USERS.
3.5.4. Events
UMUserAfterCreateEvent
Published after a user is successfully created (and synced to the IDP, if enabled).
event com.mgmtp.a12.uaa.usermanagement.event.UMUserAfterCreateEvent (Java record)
fields userDataServicesDocument (the persisted DataServicesDocument envelope),
latestDocument (the latest persisted DocumentV2)
UMUserAfterUpdateEvent
Published after a user is successfully updated (and synced to the IDP, if enabled).
event com.mgmtp.a12.uaa.usermanagement.event.UMUserAfterUpdateEvent (Java record)
fields userDataServicesDocument (the persisted DataServicesDocument envelope),
latestDocument (the latest persisted DocumentV2)
UMUserAfterDeleteEvent
Published after a user is successfully deleted (and synced to the IDP, if enabled).
event com.mgmtp.a12.uaa.usermanagement.event.UMUserAfterDeleteEvent (Java record)
fields userDataServicesDocument (the persisted DataServicesDocument envelope),
latestDocument (the latest persisted DocumentV2)
27

-- 27 of 121 --

UMSynchronizationTriggerEvent
Published immediately after a user / role document has been persisted by DataServices, to trigger
IDP synchronization (the bridge between the UM-side DocumentAfter*Event and the IDP write).
Consumed inside User Management by the IDP listeners (e.g., KeycloakUserEventListener,
KeycloakRoleEventListener) to actually create / update / delete the corresponding entity on the IDP.
event com.mgmtp.a12.uaa.usermanagement.event.UMSynchronizationTriggerEvent (Java record)
fields • synchronizationType (UMSynchronizationTriggerEvent.SynchronizationType) —
CREATE_USER, UPDATE_USER, DELETE_USER, CREATE_ROLE, DELETE_ROLE.
• dataServicesDocument (DataServicesDocument) — the document being synchronized
(user or role).
Listen to this event to plug in an alternative IDP integration (for example, an IDP other than
Keycloak) without replacing the entire User Management synchronization layer.
UMAfterSynchronizationEvent
Published by the IDP synchronization listeners after the IDP write has completed (e.g., the user was
created in Keycloak and the IDP ID is now known). Consumed inside User Management to write the
IDP-side identifier back onto the persisted document.
event com.mgmtp.a12.uaa.usermanagement.event.UMAfterSynchronizationEvent<T> (generic
Java record; implements ResolvableTypeProvider)
fields • synchronizationType (UMAfterSynchronizationEvent.SynchronizationType) —
CREATE_USER, UPDATE_USER, DELETE_USER, CREAT_ROLE (the enum literal is spelled
CREAT_ROLE in the code).
• dataServicesDocument (DataServicesDocument) — the document that was
synchronized.
• IDPPayload (T) — IDP-specific payload returned by the IDP write (typically the new
IDP entity ID as String for creates, or a Boolean success flag for updates/deletes).
Use this event to react to the result of an IDP write — for example, to record audit information once
the IDP-side identifier is known.
UMTenantRemoveEvent
Published by UMTenantRegistrationStorage (default implementation
SimpleUMTenantRegistrationStorage) when a tenant is removed at runtime. Consumed inside User
Management to release tenant-scoped resources (for example, closing the cached Keycloak admin
client for that tenant).
event com.mgmtp.a12.uaa.usermanagement.event.UMTenantRemoveEvent (Spring
ApplicationEvent)
fields idpKey (String) — the tenant key that was removed.
Listen to this event to clean up project-side caches or per-tenant resources when a tenant
28

-- 28 of 121 --

registration is removed.
3.6. Data Flow Diagrams
3.6.1. DataServices RPC Operations
You can manage user documents via DataServices RPC operations. The following diagram illustrates
the data flow and the available extension points.
Figure 2. RPC Workflow
Extension points available during this flow:
• (1) Pre-write hook: implement UMUserDocumentEventCustomizer and register it as a Spring bean.
See How to Customize the Default Behavior.
• (2) Document validation: register a custom Spring validator bean.
See Customize Document Validation.
• (3) Sync-events: are fired to trigger IDP synchronization process.
See UMSynchronizationTriggerEvent.
• (4) IDP conversion: implement IDPUserConverter<T> (core) and/or IDPUserExtensionConverter<T>
(extension).
See How to Customize the Default Behavior.
• (5) Post-write hook: use @EventListener on UMAfterSynchronizationEvent.
See after synchronization event.
3.6.2. User Management REST API Flow
You can manage user documents via the User Management REST API. The following diagram
illustrates the data flow and the available extension points.
29

-- 29 of 121 --

Figure 3. REST API Workflow
Extension points available during this flow:
• (1) Representation conversion: implement UserExtensionConverter<T>.
See How to Customize the User Document Model.
• (2) Pre-write hook: implement UMUserDocumentEventCustomizer and register it as a Spring bean.
See How to Customize the Default Behavior.
• (3) Document validation: register a custom Spring validator bean.
See Customize Document Validation.
• (4) Sync-events: are fired to trigger IDP synchronization process.
See UMSynchronizationTriggerEvent.
• (5) IDP conversion: implement IDPUserConverter<T> (core) and/or IDPUserExtensionConverter<T>
(extension).
See How to Customize the Default Behavior.
• (6) Post-write hook: use @EventListener on UMAfterSynchronizationEvent.
See after synchronization event.
3.7. Getting Started With Local
Use the local authentication mode to explore the UI models and functions without bidirectional
synchronization to Keycloak.
 LOCAL authentication type is intended for development purposes only. Do not use
it in production.
Follow these steps:
• Add the dependency
<dependency>
<groupId>com.mgmtp.a12.uaa</groupId>
<artifactId>uaa-usermanagement-service</artifactId>
<version>10.0.1</version>
</dependency>
30

-- 30 of 121 --

• Prepare an admin user YAML file
admin.yaml
username: "admin"
password: "{{YOUR_PASSWORD}}"
authorities:
- "userManagementAdmin"
• Prepare application properties
Assuming you have followed the client setup guide:
application.properties
# Spring Boot
server.port=10000
# Authentication
mgmtp.a12.uaa.authentication.types=LOCAL
mgmtp.a12.uaa.authentication.cors.allowed-origins=http://localhost:5000
mgmtp.a12.uaa.authentication.context-path=/api
mgmtp.a12.uaa.authentication.user.local-config.user-
resources={{YOUR_USER_YAML_FILE_RESOURCE}}
mgmtp.a12.uaa.authentication.jwt.secret={{YOUR_JWT_SECRET}}
mgmtp.a12.uaa.authentication.client-selfconfiguration.application-
base.url=http://localhost:8082/api
mgmtp.a12.uaa.authentication.client-selfconfiguration.uaa-
base.url=http://localhost:8082/api
# Data Services
mgmtp.a12.dataservices.server.context-path=/api
• Start the UM service application
java -jar .\uaa-usermanagement-service-10.0.1.jar
--spring.profiles.active=dataservices-uaa,dataservices
-embedded_postgres,dataservices-rpc,um_uaa,um_basic
3.8. Getting Started With OAuth2
OAuth2 authentication enables bidirectional data synchronization between User Management
Service and the IDP (Keycloak).
3.8.1. Prerequisites
31

-- 31 of 121 --

1. Ensure you have a Keycloak instance with the correct realm set up. Refer to Keycloak for
details.
2. Ensure you have a client configured correctly. Refer to the client setup guide for details.
Follow these steps:
• Prepare your application properties
application.properties
# Spring Boot
server.port=10000
# UAA
mgmtp.a12.uaa.authentication.cors.allowed-origins=http://localhost:5000
mgmtp.a12.uaa.authentication.context-path=/api
# Front-end self-configuration
mgmtp.a12.uaa.authentication.client-selfconfiguration.application-
base.url=http://localhost:10000/api
mgmtp.a12.uaa.authentication.client-selfconfiguration.uaa-
base.url=http://localhost:10000/api
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.client-
id=user_management_spa_client
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.realm-
name=user-management
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.idp-
base.url=http://localhost:9090
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.login-
relative.url=
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.login-
redirect-relative.url=
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.logout-
redirect-relative.url=
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.silent-
redirect-relative.url=silent_renew.html
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.token-
exchange-relative.url=token
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.sso-
configuration.username-xpath=//input[@name='username']
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.sso-
configuration.password-xpath=//input[@name='password']
mgmtp.a12.uaa.authentication.client-selfconfiguration.oauth2.public-client.sso-
configuration.login-button-xpath=//input[@name='login']
# Data Services
mgmtp.a12.dataservices.server.context-path=/api
# UM — IDP synchronization (adjust to your setup)
32

-- 32 of 121 --

mgmtp.a12.uaa.user-management.tenant-registration.[user-
management].synchronization.idp-technical-user.username={YOUR_IDP_TECHNICAL_USER}
mgmtp.a12.uaa.user-management.tenant-registration.[user-
management].synchronization.um-technical-user.username={YOUR_UM_TECHNICAL_USER}
mgmtp.a12.uaa.user-management.tenant-registration.[user-
management].synchronization.um-technical-
user.password={YOUR_UM_TECHNICAL_PASSWORD}
• Start the UM service application
java -jar .\uaa-usermanagement-service-10.0.1.jar
--spring.profiles.active=dataservices-uaa,dataservices
-embedded_postgres,dataservices-rpc,um_uaa,um_basic,um_oauth2
 For production deployments, consult your deployment team. Do not use the
embedded database in production.
3.9. Static Configuration
3.9.1. Configuration Properties
All properties below are bound to UserManagementProperties (prefix mgmtp.a12.uaa.user-management).
Tenant registration (mgmtp.a12.uaa.user-management.tenant-registration.[{key}].*)
Maps a tenant key to its management and synchronization configuration. Use one entry per
registered tenant.
Configuration
property
Default Description
.management.tenant-id (tenant key) Identifies the tenant within User Management.
.synchronization.enable
d
true When true, write-through to the IDP runs after
each user/role write. Set to false to disable
synchronization for the tenant.
.synchronization.idp-
url
http://localhost:90
90
Base URL of the IDP server (e.g. Keycloak) used for
synchronization.
.synchronization.realm-
name
user-management IDP realm name used for synchronization.
.synchronization.client
-name
uaa-auth-client IDP client used for synchronization calls.
.synchronization.idp-
technical-user.username
Technical user on the IDP side used to break the
synchronization cycle.
33

-- 33 of 121 --

Configuration
property
Default Description
.synchronization.um-
technical-user.username
Technical user on the UM side used for
synchronization with the IDP.
.synchronization.um-
technical-user.password
Password of the UM technical user.
.synchronization.accoun
t.url
{idp-
url}/realms/{realm-
name}/account
Account-console URL exposed to end users.
User Management general settings (mgmtp.a12.uaa.user-management.um.*)
Configuration property Default Description
.edit-username.enabled false Allow editing the username field. Enable only if your
IDP supports username editing.
.duplicate-email.enabled false Allow duplicate email values across users. Enable
only if your IDP allows duplicate emails.
.cached-storage.enabled false Wrap in-memory storages with Spring cache.
Required in cluster environments where storage
must be replicated. See Cluster Support.
.dynamic-realms-config-
support.enabled
false Enable runtime registration of OAuth2
authentication providers via Oauth2TenantStorage.
See Dynamic Authentication Providers.
.dynamic-cors-allowed-
origins-config-
support.enabled
false Enable runtime CORS-origin management via
AllowedOriginsStorage.
.client-role-
support.enabled
false Scope roles per OAuth2 client (the role-document
carries a client field).
.role-access-right-mapping-
yaml-attachment-file-name
access_rights File name used when exporting the role-to-access-
right mapping as YAML.
.users-yaml-attachment-
file-name
users File name used when exporting users as YAML.
.backend-authentication-
username
UserManagement
SupperAdmin
Username used for system-to-system calls (the
@ServiceType(BACKEND_AUTHENTICATION) variant).
User document properties (mgmtp.a12.uaa.user-management.um.user-document-properties.*)
Bound to UMUserDocumentProperties. These properties describe the structure of the user document
model. Override them when your project supplies a custom user document model — User
Management reads and writes the user document through these paths, so changing them lets you
customize even the default fields without modifying the built-in converters.
34

-- 34 of 121 --

Configuration
property
Default Description
.user-domain-name DomainUserManagement Name of the user document domain model.
.organization-unit-
role-
structure.enabled
false Enable the full organization-unit / role / access-
right structure. When disabled, only users and
roles are available (basic mode).
.user-id-path /user/id Document path for the user ID.
.user-name-path /user/username Document path for the username.
.user-first-name-path /user/firstName Document path for the first name.
.user-last-name-path /user/lastName Document path for the last name.
.user-email-path /user/email Document path for the email.
.user-enabled-path /user/enabled Document path for the enabled flag.
.user-role-path /user/role Document path for the user-role group.
.user-role-role-name-
path
/user/role/name Document path for the role name within a user-
role group.
.user-organization-
unit-path
/user/organizationUnit
Role
Document path for the organization-unit group.
.user-organization-
unit-unit-name-path
/user/organizationUnit
Role/organizationUnit
Document path for the organization-unit name
within an organization-unit group.
.user-organization-
unit-role-name-path
/user/organizationUnit
Role/role
Document path for the role within an
organization-unit group.
.user-extension-path /user/extension Document path for the project-specific extension
group.
.user-tenant-path `` (empty) Document path for the tenant identifier on the
user document. Empty means the user document
is global (not tenant-scoped).

Equivalent path properties exist for the role and access-right documents:
• mgmtp.a12.uaa.user-management.um.role-document-properties.role-tenant-path
(default: empty)
• mgmtp.a12.uaa.user-management.um.access-right-document-properties.access-
right-tenant-path (default: empty)
Data multi-tenancy settings (mgmtp.a12.uaa.user-management.um.multi-tenant.*)
Bound to UserManagementProperties.UM.MultiTenant. These properties control data multi-tenancy
(scoping user/role/access-right documents by tenant via UMTenantRegistrationStorage). See Data
Multi-tenancy.
35

-- 35 of 121 --

Configuration
property
Default Description
.enabled false Enable data multi-tenancy. When true, the active tenant is resolved
from each request and applied to every read / write.
.tenant-claim-
name
tenants JWT claim name from which the tenant identifier is read.
.tenant-header X-
Tenant-
Id
HTTP header name carrying the tenant identifier when no JWT claim
is present.
.super-admin-
roles
[] Roles whose holders bypass per-tenant filtering (cross-tenant
administrators).
.super-admin-
usernames
[] Usernames whose holders bypass per-tenant filtering.
 Dynamic registration of OAuth2 authentication providers (multiple JWT issuers)
is a separate feature controlled by mgmtp.a12.uaa.user-management.um.dynamic-
realms-config-support.enabled; see Dynamic Authentication Providers.
3.10. Configuration Profiles
User Management ships a set of pre-configured Spring profiles that bundle commonly used
properties to simplify setup. The properties listed in each profile come from uaa-usermanagement-
service/src/main/resources/config/application-{profile}.properties and are activated when the
profile is included in spring.profiles.active.
3.10.1. um_uaa — UAA defaults
Activates baseline UAA configuration (default CORS origin and the bundled access-rights file).
Profile name: um_uaa
application-um_uaa.properties
mgmtp.a12.uaa.authentication.cors.allowed-origins=http://localhost:5000
mgmtp.a12.uaa.authentication.principal.access-rights-
resource=classpath:um/common/access_rights.yaml
3.10.2. um_advanced — Organization unit, role, and access-right support
Activates the full organizational model (user → organization unit → role → access right) and
enables the DataServices enumeration REST endpoint. Loads the advanced document models and
authorization definitions on top of the basic ones.
Profile name: um_advanced
36

-- 36 of 121 --

application-um_advanced.properties
mgmtp.a12.dataservices.enumeration.rest-endpoint.enabled=true
mgmtp.a12.dataservices.initialization.import.models.path=classpath:um/advanced/models
mgmtp.a12.uaa.user-management.um.user-document-properties.organization-unit-role-
structure.enabled=true
mgmtp.a12.uaa.authorization.child-authorization-definitions=\
classpath:um/basic/authorization/user-management-authorization.json,\
classpath:um/advanced/authorization/user-management-authorization.json
mgmtp.a12.uaa.authentication.backend.enabled=true
mgmtp.a12.uaa.authentication.backend.grant-super-user-privileges.enabled=true
3.10.3. um_basic — User and role only
Activates the simplified model (user → role; no organization units or access rights). Loads only the
basic document models and authorization definitions.
Profile name: um_basic
application-um_basic.properties
mgmtp.a12.dataservices.initialization.import.models.path=classpath:um/basic/models
mgmtp.a12.uaa.authorization.child-authorization-
definitions=classpath:um/basic/authorization/user-management-authorization.json
3.10.4. um_oauth2 — OAuth2 / JWT authentication
Selects OAuth2 as the authentication chain and configures Keycloak as the JWT issuer.
Profile name: um_oauth2
application-um_oauth2.properties
mgmtp.a12.uaa.authentication.types=OAUTH2
spring.security.oauth2.resource-server.jwt.jwk-set-
uri=http://localhost:9090/realms/user-management/protocol/openid-connect/certs
spring.security.oauth2.resource-server.jwt.issuer-
uri=http://localhost:9090/realms/user-management
3.10.5. um_tenant — Data multi-tenancy
Enables data multi-tenancy: user / role / access-right documents are scoped by tenant via per-
document tenant field paths, and the JWT tenant claim is propagated to the principal. See Data
Multi-tenancy for the full data multi-tenancy model.
Profile name: um_tenant
37

-- 37 of 121 --

application-um_tenant.properties
mgmtp.a12.uaa.user-management.um.multi-tenant.enabled=true
mgmtp.a12.uaa.user-management.um.user-document-properties.user-tenant-
path=/user/tenant
mgmtp.a12.uaa.user-management.um.role-document-properties.role-tenant-
path=/Root/tenant
mgmtp.a12.uaa.user-management.um.access-right-document-properties.access-right-tenant-
path=/Root/tenant
mgmtp.a12.uaa.authentication.principal.additional-properties=tenants
3.11. Dynamic Authentication Providers
User Management can verify JWTs issued by multiple OAuth2 authentication providers
(Keycloak realms, or other compliant issuers) at runtime, without restarting the service. Each
provider is represented by an OAuth2 tenant entry held by Oauth2TenantStorage.

This section describes the authentication-provider perspective — one entry per
JWT issuer / realm. It is unrelated to data multi-tenancy (scoping User
Management business documents by tenant), which is covered in Data Multi-
tenancy.
 This feature requires OAuth2 with JWT tokens. Opaque tokens are not supported.
3.11.1. Token Verification Flow
38

-- 38 of 121 --

Figure 4. Authentication Workflow
3.11.2. Configuration
Property Defa
ult
Description
mgmtp.a12.uaa.user-
management.um.dynamic-realms-config-
support.enabled
fals
e
Enable registering / removing OAuth2
authentication providers at runtime through
Oauth2TenantStorage. When true, DynamicJWTDecoder
verifies tokens using the per-issuer configuration
held in Oauth2TenantStorage.
3.11.3. Seeding Providers from Properties
By default, User Management seeds Oauth2TenantStorage from the UAA authentication properties at
startup:
mgmtp.a12.uaa.authentication.oauth2.resourceserver.tenants[*].*
Each entry under tenants[*] becomes one OAuth2 authentication provider.
39

-- 39 of 121 --

3.11.4. Registering Providers at Runtime
After the service has started, use Oauth2TenantStorage to add or remove a provider dynamically.
Newly registered providers are picked up by DynamicJWTDecoder on the next request — no restart
required:
// Register a new OAuth2 authentication provider (JWT issuer + JWKs URI)
oauth2TenantStorage.storeTenant(oauth2Tenant);
// Remove a provider
oauth2TenantStorage.removeTenant(oauth2Tenant);
See Oauth2TenantStorage for the full API.
3.12. Data Multi-tenancy
User Management supports data multi-tenancy: user, role, and access-right documents can be
scoped by tenant so that each tenant sees only its own data. Each tenant is represented by an entry
in UMTenantRegistrationStorage, which maps a tenant identifier to its management and
synchronization configuration.

Data tenant vs. authentication provider: data tenants (managed by
UMTenantRegistrationStorage) scope the User Management business data per
tenant. They are independent of dynamic authentication providers (managed by
Oauth2TenantStorage), which are covered in Dynamic Authentication Providers.
3.12.1. Accessible tenants vs. active tenant
Two distinct concepts drive tenant filtering on every request - both resolved by
TenantContextInterceptor and exposed through TenantContext (thread-local):
1. Accessible tenants — the set of tenants the currently logged-in user is allowed to manage.
Computed by UserTenantAccessService.getAccessibleTenantKeys(authentication):
a. The JWT claim named by mgmtp.a12.uaa.user-management.um.multi-tenant.tenant-claim-name
(default tenants) is read from the authenticated principal as a comma-separated list.
b. Each value is trimmed and added to the accessible-tenant set.
c. The JWT claim is just the option that controls accessibility — if absent, the user has no
accessible tenants (unless they are super-admin, see below).
2. Active tenant - the tenant the request is targeting. Read from the HTTP header named by
mgmtp.a12.uaa.user-management.um.multi-tenant.tenant-header (default X-Tenant-Id). If the
header is missing and the user has exactly one accessible tenant, that tenant is selected
automatically.
Both values are placed into the TenantContext for the lifetime of the request:
40

-- 40 of 121 --

TenantContext.getActiveTenantKey();
TenantContext.getAccessibleTenantKeys();
TenantContext.isSuperAdmin();
When the service layer looks up a tenant, UMTenantSelector validates the active tenant against the
accessible-tenant set. If the active tenant is not in the accessible tenants (and the caller is not a
super-admin), an InvalidInputException is raised with message Not authorized for tenant
[{tenantKey}] - the request fails.
3.12.2. Super-admin support
Super-admins bypass the accessibility check and may target any registered tenant.
UserTenantAccessService.isSuperAdmin(authentication) returns true when either of the following
matches:
• The authenticated username is listed in mgmtp.a12.uaa.user-management.um.multi-tenant.super-
admin-usernames.
• Any of the authenticated user’s granted authorities (with any ROLE_ prefix stripped) is listed in
mgmtp.a12.uaa.user-management.um.multi-tenant.super-admin-roles.
When the caller is a super-admin, TenantContextInterceptor sets the accessible-tenant set to all
tenants in UMTenantRegistrationStorage, and UMTenantSelector skips the Not authorized for tenant
check.
For programmatic super-admin scoping inside the service layer (for example, in scheduled jobs or
system-to-system flows), use:
TenantContext.runWithSuperAdmin(() -> {
// code executed with super-admin privileges across all tenants
});
3.12.3. Tenant-scoped document fields
The tenant identifier is persisted on each tenant-scoped document at a configurable path. Override
the corresponding *-tenant-path property to record the tenant on the document; leave the property
empty to keep the document global (not tenant-scoped):
mgmtp.a12.uaa.user-management.um.user-document-properties.user-tenant-
path=/user/tenant
mgmtp.a12.uaa.user-management.um.role-document-properties.role-tenant-
path=/Root/tenant
mgmtp.a12.uaa.user-management.um.access-right-document-properties.access-right-tenant-
path=/Root/tenant
These paths are also documented in the user document properties table.
41

-- 41 of 121 --

3.12.4. Configuration
Bound to UserManagementProperties.UM.MultiTenant:
Property Defaul
t
Description
mgmtp.a12.uaa.user-
management.um.multi-tenant.enabled
false Enable data multi-tenancy. When true,
TenantContextInterceptor resolves accessible and
active tenants on every request.
mgmtp.a12.uaa.user-
management.um.multi-tenant.tenant-
claim-name
tenant
s
Name of the JWT claim from which
UserTenantAccessService reads the accessible-tenant
keys (comma-separated).
mgmtp.a12.uaa.user-
management.um.multi-tenant.tenant-
header
X-
Tenant
-Id
HTTP header that carries the active tenant identifier
on each request.
mgmtp.a12.uaa.user-
management.um.multi-tenant.super-
admin-roles
[] Roles whose holders are treated as super-admins
(full cross-tenant access).
mgmtp.a12.uaa.user-
management.um.multi-tenant.super-
admin-usernames
[] Usernames whose holders are treated as super-
admins (full cross-tenant access).
The um_tenant profile (see Configuration Profiles) bundles a typical data multi-tenancy setup.
3.12.5. Tenant registration
UMTenantRegistrationStorage holds one entry per registered tenant. Each entry contains the tenant’s
management and synchronization configuration; the structure and seeding properties are listed in
the tenant registration table (prefix mgmtp.a12.uaa.user-management.tenant-registration.[{key}].*).
See UMTenantRegistrationStorage for the runtime API to register, remove, and look up tenants.
3.13. Cluster Support
To support dynamic configuration, all runtime configuration related to allowed origins, OAuth2
tenant registration, and IDP synchronization is held in simple in-memory storage by default (no
persistent storage).
In a cluster environment, this storage must be replicated across nodes. Enable the cached storage
wrapper and use a replicated cache provider (e.g., Redis or Hazelcast):
mgmtp.a12.uaa.user-management.um.cached-storage.enabled=true
spring.cache.cache-names=umAllowedOriginsConfigCache,umIDPSyncConfigurationCache
User Management uses Spring’s caching abstraction, so any compliant cache manager works.
Configure your preferred cache provider according to the Spring Boot caching documentation.
42

-- 42 of 121 --


1. If you are using a custom cache provider, provide custom implementations of
Oauth2TenantStorage, AllowedOriginsStorage, and UMTenantRegistrationStorage.
2. Cache entries must never expire or be evicted. If these entries are lost, User
Management will be unable to verify tokens or grant access to clients until the
service is restarted.
3.14. Transaction within User Management
3.14.1. Transaction from User Management to Keycloak
User Management Service is built on A12 DataServices and inherits its transactional behavior for
CRUD operations.
User Management listens to DataServices document events and integrates the IDP synchronization
into the same transaction:
Before create user
The service listens to DocumentBeforeCreateEvent.
Document validation, user uniqueness checks, password confirmation, and any registered
UMUserDocumentEventCustomizer.customizeBeforeCreateUser(…) hooks execute before the document
is persisted. Any exception triggers a rollback.
After create user
The service listens to DocumentAfterCreateEvent.
The user is created on the IDP (Keycloak) and the resulting IDP user ID is written back into the user
document. Any error from the IDP API triggers a rollback.
After a successful create, UMUserAfterCreateEvent is published.
Before update user
The service listens to DocumentBeforeUpdateEvent.
Document validation, illegal ID/username-change checks, and any registered
UMUserDocumentEventCustomizer.customizeBeforeUpdateUser(…) hooks run. Any exception triggers a
rollback.
After update user
The service listens to DocumentAfterUpdateEvent.
The user is updated on the IDP. Any IDP error triggers a rollback.
After a successful update, UMUserAfterUpdateEvent is published.
43

-- 43 of 121 --

Before delete user
The service listens to DocumentBeforeDeleteEvent.
Document validation, the self-deletion check (users cannot delete themselves; attempting to do so
throws InvalidInputException), and any registered
UMUserDocumentEventCustomizer.customizeBeforeDeleteUser(…) hooks run. Any error except
NotFoundException triggers a rollback.
After delete user
The service listens to DocumentAfterDeleteEvent.
The user is removed from the IDP. Any error triggers a rollback.
After a successful delete, UMUserAfterDeleteEvent is published.
3.14.2. Transaction from Keycloak to User Management
Transactions initiated by the Keycloak plugin (uaa-usermanagement-keycloak-plugin) depend on User
Management’s response.
The plugin listens to Keycloak events: REGISTER, UPDATE_PROFILE, and all USER and REALM_ROLE_MAPPING
admin events. If User Management returns an error, the plugin rolls back the Keycloak-side
transaction and propagates an exception.
When the plugin is configured with multiple target UM services, the following errors per target are
ignored (non-fatal):
• Connection timeout — the host/port is firewalled or the host is unreachable.
• Connection refused — no service is listening on the target port.
Ensure that all server configurations in the plugin are correct to avoid silent synchronization
failures.
3.15. Actuator properties
• How to enable it.
◦ Adding below dependency with your expected version
<dependency>
<groupId>com.mgmtp.a12.uaa</groupId>
<artifactId>uaa-authorization-web-spring-boot-autoconfigure</artifactId>
<version>10.0.1</version>
</dependency>
• Enable the actuator support
44

-- 44 of 121 --

Configuration property Default
value
Usage
management.endpoints.enabl
ed-by-default
false The actuator’s endpoints enablement to be opt-in (inside
properties) rather than opt-out
management.endpoint.health
.enabled
true Actuator’s health endpoint is available
management.endpoint.info.e
nabled
true Actuator’s info endpoint is available
management.endpoints.web.e
xposure.exclude
Does not exclude any Actuator’s endpoint
 Make sure your login user should be granted a userManagementAdmin role for
accessing.
3.16. How to Customize the User Document Model
The default user document model (DomainUserManagement.json) contains standard fields: id, username,
lastName, firstName, email, enabled, avatar, roles, and a free-form extension group. Those fields are
required for UM’s core functions. However, User Management supports customizing the user
document model in many shapes:
1. Renaming or relocating the default fields by overriding the corresponding
UMUserDocumentProperties attribute (e.g., mgmtp.a12.uaa.user-management.um.user-document-
properties.user-email-path=/user/contact/primaryEmail).
2. Adding project-specific fields in extension group.
3. Adding project-specific fields alongside with UM’s default fields.
3.16.1. Step 1: Update DomainUserManagement.json
Download the default DomainUserManagement.json file (see User Management Service Business
Document Models) and add your custom fields.
{
"header": { ... },
"content": {
{...},
{
"type": "Group",
"id": "group_6804b",
"name": "user",
"Group": {
"repeatability": 1,
"elements": [
{...default fields...},
{
"contact": {
"type": "Group",
45

-- 45 of 121 --

"id": "contact",
"name": "contact",
"Group": {
"repeatability": 1,
"elements": [
{
"type": "String",
"id": "primaryEmail",
"name": "primaryEmail",
...
}
]
}
}
},
{
"type": "String",
"id": "department",
"name": "department",
...
},
{
"type": "String",
"id": "costCenter",
"name": "costCenter",
...
}
]
}
}
}
}
Place the updated model in your project model path and register it in DataServices:
mgmtp.a12.dataservices.initialization.import.models.path=\
classpath:${USER_MANAGEMENT_DEFAULT_MODEL_PATH},\
classpath:${YOUR_PROJECT_MODEL_PATH}
If you also relocate any default field (e.g., move email outside /user), override the corresponding
path on UMUserDocumentProperties:
mgmtp.a12.uaa.user-management.um.user-document-properties.user-email-
path=/user/contact/primaryEmail
46

-- 46 of 121 --

3.16.2. Step 2: Extend UMUserRepresentation
Create a class that extends UMUserRepresentation and adds your custom fields:
public class ProjectUser extends UMUserRepresentation {
private String department;
private String costCenter;
// new Contact pojo for the relocated email field
private Contact contact;
public String getDepartment() { return department; }
public void setDepartment(String department) { this.department = department; }
public String getCostCenter() { return costCenter; }
public void setCostCenter(String costCenter) { this.costCenter = costCenter; }
public String getContact() { return contact; }
public void setContact(Contact contact) { this.contact = contact; }
}
3.16.3. Step 3: Implement UserExtensionConverter
Implement UserExtensionConverter<ProjectUser> to describe how your custom fields map to and
from the DocumentV2 representation. Use document.withFieldValue(…) to write a field value and
document.fieldValue(…) to read one:
@Component
public class ProjectUserExtensionConverter implements UserExtensionConverter
<ProjectUser> {
@Override
public DocumentV2 convert(ProjectUser user, DocumentV2 document) {
// write custom fields into the document's group
return document
.withFieldValue("/user/department", user.getDepartment())
.withFieldValue("/user/costCenter", user.getCostCenter())
.withFieldValue("/user/contact/primaryEmail", user.getContact()
.getPrimaryEmail());
}
@Override
public void convert(DocumentV2 document, ProjectUser user) {
// read custom fields from the document's group
user.setDepartment((String) document.fieldValue("/user/department"));
user.setCostCenter((String) document.fieldValue("/user/costCenter"));
user.setContact(new Contact((String) document.fieldValue(
"/user/contact/primaryEmail")));
user.setEmail(user.getContact().getPrimaryEmail()); // for backward
47

-- 47 of 121 --

compatibility with the default email field
}
}
3.16.4. Step 4: Register the Supplier bean
Register a Supplier<ProjectUser> bean so that User Management knows how to instantiate your
subtype:
@Configuration
public class ProjectUserConfiguration {
@Bean
public Supplier<ProjectUser> projectUserSupplier() {
return ProjectUser::new;
}
}
Once both beans are registered, User Management will use ProjectUser as the representation type
and call your converter for all persistence and YAML import/export operations.

You can also customize YAML import and export by overriding
toExportData(List<T> users) and fromImportData(InputStream inputStream) in your
UserExtensionConverter<T> implementation. Returning null from either method
falls back to the built-in YAML format.
3.17. How to Customize the Default Behavior
This section covers the four extension points available to project code: representation conversion,
pre-write hooks, document validation, and IDP conversion. Refer to the data flow diagram for
where each extension point is invoked.
3.17.1. Customize Representation Conversion
UserExtensionConverter<T extends UMBaseRepresentation> is the single SPI for mapping between
DocumentV2 (the DataServices representation) and your typed representation T, plus for customizing
YAML import/export.
Implement this interface and register it as a Spring @Component. Use document.withFieldValue(…) to
set a field value and document.fieldValue(…) to read one:
@Component
public class ProjectUserExtensionConverter implements UserExtensionConverter
<ProjectUser> {
@Override
public DocumentV2 convert(ProjectUser user, DocumentV2 document) {
48

-- 48 of 121 --

// Enrich the document already converted by the UM Rest Client before
persistence.
return document.withFieldValue("/user/extension/department", user
.getDepartment());
}
@Override
public void convert(DocumentV2 document, ProjectUser user) {
// Populate the representation already converted by the UM Service Converter
when reading.
user.setDepartment((String) document.fieldValue("/user/extension/department")
);
}
// Override toExportData / fromImportData for custom YAML import/export
(optional).
}

User Management automatically picks up a single UserExtensionConverter<T> bean
from the Spring context. Register a Supplier<T> bean alongside it so User
Management can instantiate your subtype. See How to Customize the User
Document Model for the complete setup.
3.17.2. Customize Pre-Write Behavior (UMUserDocumentEventCustomizer)
UMUserDocumentEventCustomizer provides pre-write hooks that run before a user document is
persisted. Each hook returns the (possibly modified) DocumentV2 that will be passed to the next step.
Implement and register it as a Spring bean:
@Component
public class ProjectUserDocumentCustomizer implements UMUserDocumentEventCustomizer {
@Override
public DocumentV2 customizeBeforeCreateUser(DocumentV2 userDocument) {
// add a temporary password, normalize fields, etc.
return userDocument;
}
@Override
public DocumentV2 customizeBeforeUpdateUser(DocumentV2 userDocument) {
// trim whitespace, enforce field invariants, etc.
return userDocument;
}
@Override
public DocumentV2 customizeBeforeDeleteUser(DocumentV2 userDocument) {
// log, audit, or veto the deletion (throw to abort)
return userDocument;
49

-- 49 of 121 --

}
}
For post-write hooks (after the document is persisted and the IDP is synced), use Spring
@EventListener on the published UM user events. Each event carries the persisted
DataServicesDocument envelope and the latest DocumentV2 payload:
@Component
public class ProjectUserEventListener {
@EventListener
public void onUserCreated(UMUserAfterCreateEvent event) {
DocumentV2 latestDocument = event.latestDocument();
// notify downstream systems, send a welcome email, etc.
}
@EventListener
public void onUserUpdated(UMUserAfterUpdateEvent event) {
// react to the update
}
@EventListener
public void onUserDeleted(UMUserAfterDeleteEvent event) {
// clean up related data
}
}
 The id, username, and email fields (when present) are always lowercased by the
built-in customizer before any project customizer runs.
3.17.3. Customize Document Validation
Implement IUserDocumentValidationService (in com.mgmtp.a12.uaa.usermanagement.validator) and
register it as a Spring bean to validate the user document before it is persisted. The interface
exposes one hook per write operation; all methods default to a no-op, so override only the ones you
need:
@Component
public class ProjectUserDocumentValidator implements IUserDocumentValidationService {
@Override
public void validateBeforeCreateUser(DocumentV2 userDocument) {
String email = (String) userDocument.fieldValue("/user/email");
if (!isValidEmail(email)) {
throw new InvalidInputException("validation.email.invalid", "Invalid email
format");
}
}
50

-- 50 of 121 --

@Override
public void validateBeforeUpdateUser(DocumentV2 userDocument) {
// same or different rules for updates
}
@Override
public void validateBeforeDeleteUser(DocumentV2 userDocument) {
// pre-delete validation rules
}
}
 Built-in validations (password confirmation match, email uniqueness) run before
the project validator. Throw InvalidInputException from any hook to abort the
write and roll back the surrounding DataServices transaction.
3.17.4. Customize IDP Conversion
Two SPIs are available for customizing how a user is represented in the IDP (Keycloak by default).
In both, the generic type T is the IDP user representation type (for Keycloak:
org.keycloak.representations.idm.UserRepresentation):
IDPUserConverter<T> — defines the core mapping from the persisted DocumentV2 to the IDP user type
T. The default Keycloak implementation KeycloakUserConverter implements
IDPUserConverter<UserRepresentation> (in uaa-usermanagement-keycloak) covers the standard fields.
IDPUserExtensionConverter<T> — a lighter hook invoked by IDPUserConverter<T> after the core
mapping, to apply additional project-specific attribute mappings (e.g., writing custom extension
fields to Keycloak user attributes).
To add custom attribute mapping without replacing the full converter, implement
IDPUserExtensionConverter<UserRepresentation> so it plugs into the Keycloak IDPUserConverter:
import org.keycloak.representations.idm.UserRepresentation;
@Component
public class ProjectKeycloakUserExtensionConverter
implements IDPUserExtensionConverter<UserRepresentation> {
@Override
public void convert(DocumentV2 userDocument, UserRepresentation idpUser) {
// Read the field straight from the document and copy it onto the Keycloak
user.
String department = (String) userDocument.fieldValue("/user/department");
Map<String, List<String>> attributes = idpUser.getAttributes();
if (attributes == null) {
attributes = new HashMap<>();
idpUser.setAttributes(attributes);
51

-- 51 of 121 --

}
attributes.put("department", List.of(department));
}
}
3.17.5. Handle IDP Exceptions
IIDPExceptionHandler (in com.mgmtp.a12.uaa.usermanagement.exception) lets project code intercept
and translate IDP-side errors:
@Component
public class ProjectIDPExceptionHandler implements IIDPExceptionHandler {
@Override
public RuntimeException handleException(Exception e) {
return new InvalidInputException("idp.error", "IDP operation failed", e);
}
}

Transaction errors that occur during IDP writes (e.g., in IDPUserExtensionConverter)
should be handled via IIDPExceptionHandler. In the case of a duplicate-user error,
an InvalidInputException is thrown directly and is not delegated to
IIDPExceptionHandler.
3.18. How to migrate current documents to new
business models
By the default, User Management Service will import the business models into database when
starting the application for the first time. The models will be overwritten if they are already present
in the database.
In case the document model content has changed (e.g: Upgrade to new A12 version, data model has
changed to adapt new business requirement,…) then it may be required to migrate existing user
data in the repository to accommodate these changes, otherwise, the application may not startup.
• We have 3 below situations:
◦ Import business models from the core models.
◦ Import business models from the outside models.
◦ Import business models from both core and outside models.
For example, the customer projects want to import the business models from their model directory
by setting mgmtp.a12.dataservices.initialization.import.models.path with outside models path.
• Example of directory models structure:
52

-- 52 of 121 --

+- resources
| +- config
| +- models
| | \- DomainUserManagement.json
| | \- User.json
| | \- User_Create.json
| | \- usermanagement.json
| | \- UserManagementOverview.json
• How to implement User data migration:
In the case got an error with existing data and new document models. You need to implement
migration step refer to Data migration support section of geta12.com - Data Services.
Example migration steps:
1. Load the OLD MODEL from classpath to query all current user documents.
2. Query and migrate all existing user documents to accommodate NEW MODEL (the new model
also loads before migrating documents).
3. Validate all user documents after migration.
4. User Management REST Client
4.1. Overview
The REST client modules provide typed Java clients for consuming User Management REST APIs
from other services. They are built on the UAA REST client infrastructure and expose separate
clients per resource type:
• UMUserRestClient<T extends UMUserRepresentation> — user CRUD and search operations (uses
UMUserRepresentation).
• UMRoleRestClient — role CRUD and search operations (uses UMRoleRepresentation).
• UMAccessRightRestClient — access-right CRUD and mapping import/export (uses
UMAccessRightRepresentation).
All three extend UMBaseRestClient<T> which defines the common operations (based on public REST
API): create(T), get(String id), update(T), delete(String id), search(QueryRoot, SearchOptions).
4.2. Getting Started
Add the following dependencies to your POM:
<dependency>
<groupId>com.mgmtp.a12.uaa</groupId>
<artifactId>uaa-usermanagement-rest-client</artifactId>
53

-- 53 of 121 --

<version>10.0.1</version>
</dependency>
The uaa-usermanagement-rest-client module provides Spring Boot auto-configuration. Once it is on
the classpath and the properties below are configured, the UMUserRestClient<T>, UMRoleRestClient,
and UMAccessRightRestClient beans are available for injection.
@Inject
private UMUserRestClient<ProjectUser> userRestClient;
ProjectUser user = userRestClient.get("some-user-id");
4.3. Configuration
In a Spring Boot application, all configuration is handled by the
UserManagementRestClientAutoConfiguration in uaa-usermanagement-rest-client. Configure it via
properties:
Configuration property Default
value
Usage Rem
ark
mgmtp.a12.uaa.user-
management.client.rest.uaa-
base.url
http://loc
alhost:808
0
Base URL for the UAA server.
mgmtp.a12.uaa.user-
management.client.rest.servic
e-base.url
http://loc
alhost:808
0
Base URL for the User Management Service.
mgmtp.a12.uaa.user-
management.client.rest.servic
e-context-path
`` Context path of the User Management Service.
mgmtp.a12.uaa.user-
management.client.rest.cache.
enabled
false Enable read-result caching.
mgmtp.a12.uaa.user-
management.client.rest.authen
tication-type
DELEGATED Authentication mode: DELEGATED forwards the
caller’s token; BACKEND uses a dedicated service
credential.
Because the UM REST client is built on the UAA REST client infrastructure, refer to UAA Java REST
Client for details on how to configure each authentication type.
 mgmtp.a12.uaa.user-management.client.rest.* is the configuration key prefix. The
remaining keys follow the same pattern as the base UAA REST client.
Example — reading a user:
@Inject
private UMUserRestClient<ProjectUser> userRestClient;
54

-- 54 of 121 --

ProjectUser currentUser = userRestClient.get("some-user-id");
4.4. Read Caching Support
UMUserRestClient.get(…) may be called frequently to load user data. Because user records rarely
change, caching the read result improves throughput.
For the complete flow:
Figure 5. Caching Workflow
4.4.1. Enable
Enable caching via the configuration property — see Configuration.
The REST client uses Spring’s caching abstraction. In a cluster environment, configure a replicated
cache provider (e.g., Redis).
mgmtp.a12.uaa.user-management.client.rest.cache.enabled=true
spring.cache.cache-names=umRestClientReadCache
4.4.2. Flush the cache
To evict cached entries, use one of the following options:
• Configure a time-to-live via your cache provider:
spring.cache.redis.time-to-live=10m
• Publish a CacheFlushEvent programmatically after a write:
@Inject
private ApplicationEventPublisher applicationEventPublisher;
@Inject
55

-- 55 of 121 --

private UserManagementRestClientProperties restClientProperties;
applicationEventPublisher.publishEvent(
new CacheFlushEvent(this, restClientProperties.getServiceContextPath() + "/user")
);
5. User Management Tool
This is a Command Line Interface tool that supports:
• Register multiple user management service configure information for User Management
Keycloak Extension.
• Register multiple technical users.
Usage:
java -jar uaa-user-management-tool-cli-10.0.1.jar [OPTIONS] ARGUMENT_FILE_PATH
Options:
Name, shorthand Description
--config, -c register new extension configs for the new systems
--user, -u register new technical users
--help, -h display help
Argument:

Below is our example that is fixed with the keycloak setup here and user
management server setup here.
If you have a different setup (e.g, host, port). Please make the changes accordingly.
argument.json
{
"credentials": [
{
"url": "http://localhost:9090",
"contextPath": "",
"realmName": "master",
"clientId": "admin-cli",
"clientSecret": null,
"username": "admin",
"password": "{{YOUR_ADMIN_PASSWORD}}"
}
],
56

-- 56 of 121 --

"servers": [
{
"url": "http://localhost:10000",
"contextPath": "/api",
"realmName": "user-management",
"clientId": "uaa-auth-client",
"clientSecret": "{{YOUR_CLIENT_SECRET}}",
"tokenType": "Bearer",
"users": [
{
"username": "{{YOUR_USER_MANAGEMENT_SERVICE_USERNAME}}",
"password": "{{YOUR_USER_MANAGEMENT_SERVICE_PASSWORD}}"
},
{
"username": "{{YOUR_KEYCLOAK_EXTENSION_USERNAME}}",
"password": "{{YOUR_KEYCLOAK_EXTENSION_USER_PASSWORD}}"
}
]
}
]
}
1. credentials: The credentials configuration required for the tool to authenticate and authorize
with the Keycloak Extension through the endpoints.
We support a list of user credentials, but we only use one successfully authenticated login.
We also support admin users from the master realm with admin-cli to register for other
realms. If you have users in your realm you are able to authenticate with your client and secret
but note that it contains client roles: manage-realm, manage-users, view-users and view-clients.
2. servers: The servers configuration required for the tool to register multiple servers into
Keycloak Extension. Besides, we support registering batch technical users from each server
configuration for synchronization.
It is important to note that the user of each server must have at least 2 users (This is required of
the Keycloak Extension). We will use the first technical user for User Management Server and
the second for Keycloak Server.
6. Keycloak
Keycloak is Open Source Identity and Access Management For Modern Applications and Services.
Add authentication to applications and secure services with minimum fuss. No need to deal with
storing users or authenticating users. It is all available out of the box.
You will even get advanced features such as User Federation, Identity Brokering and Social Login.
All user management features from Keycloak can be found in https://www.keycloak.org/docs/latest/
server_admin/index.html.
Keycloak with UAA: UAA uses Keycloak as IDP and relies on Keycloak User Management features
57

-- 57 of 121 --

which allows the project to freely define and manage their own users by using Keycloak. UAA will
provide the authentication method which talk with Keycloak via OpenIdConnect/Oauth2 and SAML.
6.1. Getting Started
For general Keycloak getting started documentation can be found in https://www.keycloak.org/
guides#getting-started.
But in scope of this guideline, we choose the Get started with Keycloak on bare metal.
 Please aware that start-dev which uses in this guideline is for development only
should not use for production setup.
• Download and extract the latest Keycloak (https://github.com/keycloak/keycloak/releases/)
• After extracting this file, you should have a directory with a name that starts with keycloak-
26.0.3
• Download our default secure user management realm.
• Copy this default secure realm into directory keycloak-26.0.3\data\import
• From a terminal, open the keycloak-26.0.3\bin directory
• Create initial admin user by using environment variables (below is an example for window):
Set KEYCLOAK_ADMIN={{YOUR_ADMIN_USER}}
Set KEYCLOAK_ADMIN_PASSWORD={{YOUR_ADMIN_PASSWORD}}
• Start the Keycloak
Below is an example command for window:
kc start-dev --http-port=9090 --import-realm

If you want go with production setup. start should be used instated of start-dev.
But before to go with start then make sure you followed this guideline
https://www.keycloak.org/server/configuration-production.
6.2. Keycloak clients configure
User Management Client Front End application and User Management Keycloak Extension library
want to get the Access Token from Keycloak.
You should have proper client configure.
58

-- 58 of 121 --

6.2.1. Access to clients
Refer below steps:
• Go to the Keycloak Admin Console (e.g. http://localhost:9090/admin/). Login by using your
credential.
• Go to "user-management" realm
• Go to "clients"
6.2.2. Generate the client secret for uaa-auth-client
This client will be used by User Management Keycloak Extension library.
• From Client secret click to Generated button
 Only generated in case the client secret is missing, or you want another secret.
6.2.3. Update URI for user_management_spa_client
This client will be used by User Management Client Front End application.
 Below is our example that is fixed with the user management client setup here.
If you have a different setup (e.g, host, port). Please make the changes accordingly.
• From `Valid redirect URIs ` click to add 2 new items
1. http://localhost:8081/
2. http://localhost:8081/silent_renew.html
• From Web origins click to add 1 new item
1. http://localhost:8081
6.3. Create user-management-service admin user
This is an admin user who can access to system.
Refer below steps:
• Go to the Keycloak Admin Console (e.g. http://localhost:9090/admin/). Login by using your
credential.
• Go to "user-management" realm
• Do following https://www.keycloak.org/getting-started/getting-started-zip#_create_a_user to
create users
• After user created, assign userManagementAdmin role for this user.
59

-- 59 of 121 --

6.4. Open Security Issues
6.4.1. Host Header Injection
What is the Host Header injection?
Host header injection is a web attack where the attacker provides a false Host header to the web
application. In an incoming HTTP request, web servers often dispatch the request to the target
virtual host based on the value supplied in the Host header. Without proper validation of the
header value, the attacker can supply invalid input to cause the web server to:
• Cause a redirect to an attacker-controlled domain.
• Perform web cache poisoning.
• Manipulate password reset functionality.
How dangerous are Host Header Injection?
Host header injection can be used for these attack above. An attacker can abuse it to redirect an
attacker-controller domain. Web cache poisoning lets an attacker serve poisoned content to anyone
who requests pages. Using password reset poisoning, the attacker can obtain a password reset
token and reset another user’s password leak to take over the user account.
How to avoid Host Header Injection?
• Refer to the below link for proper keycloak hostname setup:
◦ Configuring the hostname.
6.4.2. SMTP Server
Please aware of SMTP server data exported via Keycloak return plain text if you don’t configure
anything.
Please follow document: https://www.keycloak.org/docs/latest/server_admin/index.html#_vault-
administration.
7. Bidirectional Synchronization between
User Management Service and Keycloak
7.1. Overview
7.1.1. Enabling synchronization
User Management synchronization with Keycloak is opt-in. To enable the UM → Keycloak
direction:
1. Add a dependency on the Keycloak IDP module so the Keycloak implementation of
60

-- 60 of 121 --

IDPUserService / IDPRoleService is on the classpath:
implementation 'com.mgmtp.a12.uaa:uaa-usermanagement-keycloak'
2. Enable synchronization for each registered tenant and configure the IDP connection in your
application properties:
# Enable synchronization for the tenant (default: true; set to false to disable)
mgmtp.a12.uaa.user-management.tenant-
registration.[{tenantId}].synchronization.enabled=true
# Keycloak connection
mgmtp.a12.uaa.user-management.tenant-registration.[{tenantId}].synchronization.idp-
url=http://localhost:9090
mgmtp.a12.uaa.user-management.tenant-
registration.[{tenantId}].synchronization.realm-name=user-management
# Technical users used to break the synchronization cycle
mgmtp.a12.uaa.user-management.tenant-registration.[{tenantId}].synchronization.idp-
technical-user.username={IDP_TECHNICAL_USER}
mgmtp.a12.uaa.user-management.tenant-registration.[{tenantId}].synchronization.um-
technical-user.username={UM_TECHNICAL_USER}
mgmtp.a12.uaa.user-management.tenant-registration.[{tenantId}].synchronization.um-
technical-user.password={UM_TECHNICAL_PASSWORD}
For the Keycloak → UM direction, deploy the Keycloak plugin as described in How to Deploy the
User Management Keycloak Plugin.
7.1.2. Diagram
61

-- 61 of 121 --

7.2. Technical users
Technical user uses for data-synchronization between User Management Service and Keycloak.
Due to round-trip issue, 2 different technical users need to be used.
One is User Management Service and the other is Keycloak plugin
How to create a proper user:
• Go to the Keycloak Admin Console (e.g. http://localhost:9090/admin/). Login by using your
credential.
• Go to "user-management" realm
• Go to "Users"
• Click to "Add user"
• Assign credentials
• Assign following roles:
◦ For user at User Management Service: manage-realm, manage-clients, manage-users
◦ For user at Keycloak extension: userManagementAdmin

1. Make sure userManagementAdmin is created from Realm roles before doing the
role assigning step
2. You can refer https://www.keycloak.org/getting-started/getting-started-zip
for detail
62

-- 62 of 121 --

7.3. How to Deploy the User Management Keycloak
Plugin
 1. If you do not have a Keycloak instance, refer to Keycloak Getting Started.
2. Make sure no Keycloak instance is running before installing the plugin.
7.3.1. Prepare the plugin JAR file
• Download the User Management Keycloak plugin:
uaa-usermanagement-keycloak-plugin-10.0.1.jar.
• Copy the JAR into keycloak-26.0.3\providers.
7.3.2. Start Keycloak
Refer to how to start Keycloak.
7.3.3. Enable Events Listener
Make sure the user-management event listener is enabled:
• Open the Keycloak Admin Console (e.g. http://localhost:9090/admin/master/console/#/user-
management/realm-settings/events).
• Assign user-management as an event listener.
• Click Save.
7.3.4. Set Up Data Configuration
Refer to how to configure the plugin.
8. User Management Keycloak Plugin
8.1. Overview
uaa-usermanagement-keycloak-plugin is a Keycloak SPI (service provider interface) deployed inside
Keycloak. When a Keycloak event occurs, the plugin builds the appropriate payload (user or role)
and forwards it to the registered User Management Service instance(s).
The realm name resolved from the event is used to determine which User Management Service to
call.
8.2. Getting Started
Refer to how to deploy the plugin.
63

-- 63 of 121 --

8.3. Supported Events
Normal user events
1. EventType.REGISTER
2. EventType.UPDATE_PROFILE
Keycloak admin events
1. ResourceType.USER — create, update, delete
2. ResourceType.REALM_ROLE_MAPPING — assign or remove
3. ResourceType.CLIENT_ROLE_MAPPING — assign or remove
4. ResourceType.REALM_ROLE — create, update, delete
5. ResourceType.CLIENT_ROLE — create, update, delete
8.4. Environment Variables
The plugin is configured via environment variables set in the Keycloak host:
Variable Default Description
KEYCLOAK_EXTENSION_NAME user-
managemen
t
Name used to identify this plugin instance.
KEYCLOAK_EXTENSION_DESCRIP
TION
(none) Human-readable description of the plugin.
KEYCLOAK_EXTENSION_CONFIGU
RATION_STORAGE_TYPE
MEMORY Storage type for plugin configuration. Set to DISK to persist
to files.
KEYCLOAK_EXTENSION_DIRECTO
RY
(none) Directory where configuration files are stored when DISK
mode is used.
KEYCLOAK_FILE_ENCRYPTED_PA
SSWORD
(none) Password for AES-GCM encryption of DISK configuration
files. If absent, files are stored as plain JSON.
KEYCLOAK_FILE_ENCRYPTED_SA
LT
(none) Salt for key derivation. Falls back to the password value if
absent.
IS_UM_ORGANIZATION_UNIT_RO
LE_ENABLE
false Set to true if the User Management Service has role
management enabled or there will be no role
synchronization.
LISTENED_USER_EVENTS (all) Comma-separated list of user event types to listen for.
LISTENED_ADMIN_EVENTS (all) Comma-separated list of admin event resource types to
listen for.
8.5. Plugin Configuration
Each plugin configuration entry (a KeycloakPluginConfiguration.ServiceConfig) identifies one User
64

-- 64 of 121 --

Management Service to notify when events occur. It contains:
• name — the Keycloak realm name; used as the lookup key.
• umServer — base URL of the User Management Service.
• contextPath — context path of the User Management Service.
• auth — credentials used to obtain a token from the IDP for calling the User Management Service:
◦ username, password — credentials of the technical user on the Keycloak side.
◦ oauth2.idpServer — base URL of the IDP (typically the same Keycloak instance hosting the
plugin).
◦ oauth2.realmName — realm under which the token is issued.
◦ oauth2.clientId, oauth2.clientSecret — OAuth2 client used to request the token.
◦ oauth2.loginRelative — token endpoint path appended to idpServer (default /token).
• userManagementTechnicalUsername — username of the technical user on the User Management
Service side (used to prevent cyclic synchronization).
• enabled — whether this entry is active.
• reverseSynchronization — opt-out switches for the Keycloak → UM direction:
◦ userEnabled — propagate user events to UM (default true).
◦ roleEnabled — propagate role events to UM (default true).
Configuration can be loaded from the file system or managed via REST API.
8.5.1. File-based configuration (DISK mode)
When KEYCLOAK_EXTENSION_CONFIGURATION_STORAGE_TYPE=DISK, the plugin reads configuration files
from KEYCLOAK_EXTENSION_DIRECTORY. Each file represents one User Management Service registration;
the file name is the realm name suffixed with .config.json.
user-management.config.json
{
"name": "user-management",
"umServer": "http://localhost:10000",
"contextPath": "/api",
"auth": {
"username": "{{YOUR_KEYCLOAK_PLUGIN_USERNAME}}",
"password": "{{YOUR_KEYCLOAK_PLUGIN_USER_PASSWORD}}",
"oauth2": {
"idpServer": "http://localhost:9090",
"realmName": "user-management",
"clientId": "uaa-auth-client",
"clientSecret": "{{YOUR_CLIENT_SECRET}}",
"loginRelative": "/token"
}
},
"userManagementTechnicalUsername": "{{YOUR_USER_MANAGEMENT_SERVICE_USERNAME}}",
65

-- 65 of 121 --

"enabled": true,
"reverseSynchronization": {
"userEnabled": true,
"roleEnabled": true
}
}

1. File names must follow the pattern {realm-name}.config.json — one file per
realm. The realm name in the file name must match the name field inside the
file.
2. Restart Keycloak to reload configuration files added or modified outside of the
REST API.
 When KEYCLOAK_FILE_ENCRYPTED_PASSWORD is not set, credentials are stored as plain
text. Enable AES-GCM encryption in production by supplying
KEYCLOAK_FILE_ENCRYPTED_PASSWORD and KEYCLOAK_FILE_ENCRYPTED_SALT.
8.5.2. REST API configuration
The plugin exposes a REST API for managing configuration entries at runtime. It works in both
MEMORY and DISK modes: in DISK mode, writes are persisted to the configuration directory; in
MEMORY mode, they live only for the lifetime of the Keycloak process.
• Obtain an access token using the Keycloak-side technical user:
curl --request POST 'http://localhost:9090/realms/user-management/protocol/openid-
connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=uaa-auth-client' \
--data-urlencode 'client_secret={{YOUR_CLIENT_SECRET}}' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username={{YOUR_KEYCLOAK_PLUGIN_USERNAME}}' \
--data-urlencode 'password={{YOUR_KEYCLOAK_PLUGIN_USER_PASSWORD}}'
• Register one or more User Management Service configurations (name must equal the target
realm):
curl --request POST 'http://localhost:9090/realms/user-
management/extension/registerConfigs' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}' \
--header 'Content-Type: application/json' \
--data-raw '[
{
"name": "user-management",
"umServer": "http://localhost:10000",
"contextPath": "/api",
"auth": {
66

-- 66 of 121 --

"username": "{{YOUR_KEYCLOAK_PLUGIN_USERNAME}}",
"password": "{{YOUR_KEYCLOAK_PLUGIN_USER_PASSWORD}}",
"oauth2": {
"idpServer": "http://localhost:9090",
"realmName": "user-management",
"clientId": "uaa-auth-client",
"clientSecret": "{{YOUR_CLIENT_SECRET}}",
"loginRelative": "/token"
}
},
"userManagementTechnicalUsername": "{{YOUR_USER_MANAGEMENT_SERVICE_USERNAME}}",
"enabled": true,
"reverseSynchronization": {
"userEnabled": true,
"roleEnabled": true
}
}
]'
If a configuration fails validation, the corresponding entry in the response array is null. To modify
an existing configuration, re-send it with the same name — the existing entry for that realm is
replaced.
• Unregister the configuration for the current realm (the realm in the request URL):
curl --request POST 'http://localhost:9090/realms/user-
management/extension/unregisterConfigs' \
--header 'Authorization: Bearer {{ACCESS_TOKEN}}'
 1. Any user with the manage-realm role can access these endpoints.
2. Check the Keycloak server log for details if a request returns 400 Bad Request.
9. User Management Module
9.1. Overview
This package deliver a list of A12 modules (e.g. User, Role, AccessRight, Localization,
ErrorHandling). Each module include our default id, model, view and saga implementation. You
can easily use it for integrating with A12 Client Application.
9.2. API
9.2.1. Full Models
67

-- 67 of 121 --

import Modules from "@com.mgmtp.a12.uaa/uaa-user-management-module";
9.2.2. User Model
import Modules from "@com.mgmtp.a12.uaa/uaa-user-management-module";
9.2.3. AUTH_KEYS
It’s an interface that supports custom default language.
import merge from "lodash/merge";
import UM_DEFAULT_TRANSLATIONS from "@com.mgmtp.a12.uaa/uaa-user-management-module";
import { UmResourceKeys } from "@com.mgmtp.a12.uaa/uaa-user-management-module";
const vi: UmResourceKeys = { /* Vietnam translations */ };
const YOUR_UM_CUSTOM_TRANSLATIONS: LocalizationTreeMap = { vi };
const localizer = createApplicationLocalizer(
locale,
documentModelMap,
dataFormats,
merge(UM_DEFAULT_TRANSLATIONS, YOUR_UM_CUSTOM_TRANSLATIONS,
YOUR_PROJECT_TRANSLATIONS)
);
9.3. How to integrate with A12 Client
We suggest that you need to clone full-stack-project-template which include the client module
inside. Then modify the client module.
1. Add the user management module dependency
package.json
"dependencies": {
...,
"@com.mgmtp.a12.uaa/uaa-user-management-module": 10.0.1
}
2. Implement Module Registry.
The correct Modules for registry you base on what are default Models that your UM Service are
using. Refer to User Management Default Models
The correct Modules for corresponding Models will be:
68

-- 68 of 121 --

• User
import Modules from "@com.mgmtp.a12.uaa/uaa-user-management-module";
• User, Role and AccessRight
import Modules from "@com.mgmtp.a12.uaa/uaa-user-management-module";
The full-stack-project-template’s client is supported Oauth2 Authentication by default.
If your user-management-service application uses Local authentication. You need to change it to
Local, see UAA Docs for detail.
3. Start the application Open terminal as client directory.
npm run start
The credential to login into the application
• Click here, if you are using Oauth2 authentication
• CLick here, if you are using local authentication

1. By default, the client from full-stack-project-template will host on port 8081
and proxy to server on port 8082.
2. If you are using Oauth2 authentication click how to configure valid uri from
Keycloak for more detail.
 For production setup, please get advice from the deployment team
9.4. How to allow A12 Client works with multiple
realms
This is only valid for running with Oauth2 authentication type with Keycloak.
User Management Client application might support multiple tenants where tenant users are
managed by corresponding to the tenant’s realm.
By default, the A12 Template application which used uaaClient will automatically download the
configuration setup information from Data Services for setting up (It’s called an online self-
configure). But this feature only support for working with single tenant’s realm.
For this reason, you should use offline self-configure (refer to uaa documentation.) for setting up
your application.
Below is a user-case and configuration example:
69

-- 69 of 121 --

1. Your application is served for 2 domains http://mycompany.abc.com/ and
http://yourcompany.abc.com/.
2. Your IDP is provided 2 different realms are mycompany and yourcompany for 2 different domains
Your application will need 2 different self-configure configuration for 2 different domains. It can be
stored as files or simply defined as variables like below.
index.tsx
import { SelfConfigure } from '@com.mgmtp.a12.uaa/uaa-authentication-client';
const myCompanyOfflineSelfConfigure: SelfConfigure = {
"tokens": [
{
"authorizationHeaderName": "Authorization",
"tokenType": "BEARER",
"generatedTokenHeaderName": null,
"generatedTokenExpirationHeaderName": null,
"allowCredentials": null
}
],
"oauth2": {
"tokenType": "BEARER",
"clientId": "uaa-spa-client",
"realmName": "mycompany",
"idpBaseUrl": "http://localhost:9090",
"loginRedirectRelativeUrl": "callback",
"logoutRedirectRelativeUrl": "logout",
"silentRedirectRelativeUrl": "silent_renew.html"
},
"local": {},
"activeDirectoryLdap": {},
"saml": {}
}
const yourCompanyOfflineSelfConfigure: SelfConfigure = {
"tokens": [
{
"authorizationHeaderName": "Authorization",
"tokenType": "BEARER",
"generatedTokenHeaderName": null,
"generatedTokenExpirationHeaderName": null,
"allowCredentials": null
}
],
"oauth2": {
"tokenType": "BEARER",
"clientId": "uaa-spa-client",
"realmName": "yourcompany",
"idpBaseUrl": "http://localhost:9090",
"loginRedirectRelativeUrl": "callback",
70

-- 70 of 121 --

"logoutRedirectRelativeUrl": "logout",
"silentRedirectRelativeUrl": "silent_renew.html"
},
"local": {},
"activeDirectoryLdap": {},
"saml": {}
}
From the step create UaaClientConfiguration, we will dynamic switch the offlineSelfConfigure base
on host name subDomain.
index.tsx
import { UaaClientConfiguration } from '@com.mgmtp.a12.uaa/uaa-authentication-client';
const subDomain = window.location.hostname.split('.')[0];
const uaaClientConfigure: UaaClientConfiguration =
{
serverURL: "http://localhost:10000/",
offlineSelfConfigure: subDomain === "yourCompany" ? yourCompanyOfflineSelfConfigure
: myCompanyOfflineSelfConfigure,
automaticallyLogin: true
}
UaaClient.init(uaaClientConfigure).then(() => {
console.log("The Uaa has initialized.");
});
That’s it,now just pass the above uaaClientConfigure into uaaProvider:
index.tsx
root.render(
<UaaProvider store={store} clientConfigure={uaaClientConfigure}>
<MainAppComponent />
</UaaProvider>,
document.getElementById('root'),
);
10. Other Resources
• JavaDoc
• TypeDoc
71

-- 71 of 121 --

11. Migration Instructions
11.1. 2026.06
11.1.1. Refactoring Cutover: uaa-user-management → uaa-usermanagement
The legacy uaa-user-management-* Java modules have been replaced by the refactored uaa-
usermanagement-* family. This chapter is the migration guide for consuming projects. It is scoped to
the backend Java side only; frontend sources (uaa-user-management-module/, devapps/*-client)
remain unchanged.
Cutover Model
• The legacy modules will not receive another release. They are being replaced entirely by the
refactoring.
• There is no @Deprecated bridge release and no legacy-fallback compatibility layer.
• Projects must migrate as a one-shot cutover: swap dependencies, update code, deploy.
• The uaa-user-management-tool CLI module is not being migrated; projects that used it must
implement their own batch flow (for example, by calling UMUserRestClient).
What Did Not Change
These points of contact remain stable between legacy and refactored modules. No project code
change is needed for them:
• Root property prefix: mgmtp.a12.uaa.user-management.*
• Spring Boot auto-configuration model (registered beans are opted-in / overridable as before)
• A12 Dataservices DocumentV2-based persistence model
• userManagementAdmin role still governs admin-only operations
Breaking Changes at a Glance
Area Change Code change
required
Gradle coordinates :uaa-user-management-* → :uaa-
usermanagement:uaa-usermanagement-*
Yes
Domain POJOs User, Role, AccessRight replaced by
UMUserRepresentation, UMRoleRepresentation,
UMAccessRightRepresentation
Yes
Service interfaces UserService/RoleService/AccessRightService
replaced by UMUserService/UMRoleService
/UMAccessRightService
Yes
72

-- 72 of 121 --

Area Change Code change
required
REST client Monolithic UserManagementServiceRestClient split
into UMUserRestClient / UMRoleRestClient /
UMAccessRightRestClient
Yes
REST endpoints Path-variable lookups (/user/read/{username})
replaced by query-parameter lookups
(/user?id=… or /user?docRef=…); /user-
management/* endpoints removed
Yes (clients)
Authorization scopes Renamed (Create User Data Model → Create
User, Download Users As Yaml File → Export
User, etc.); advanced scopes split into a second
JSON file
Yes
Configuration properties idp-registration[*] renamed to tenant-
registration[*] with restructured sub-keys;
core-model-name moved under user-document-
properties.user-domain-name
Yes
Events UserAfterCreate/Update/DeleteEvent<U> (generic
classes) replaced by
UMUserAfterCreate/Update/DeleteEvent (records)
with payload field renames
Yes (listeners)
Extension SPIs IUserDocumentConversionService,
IUserDocumentCustomizationService,
IUserIDPConversionService,
IUserIDPCustomizationService replaced by
UserExtensionConverter,
UMUserDocumentEventCustomizer, IDPUserConverter,
IDPUserExtensionConverter
Yes (if
implemented)
Multi-tenancy Entirely new subsystem (TenantContext,
UMTenantRegistrationStorage, UMTenantSelector,
JwtTenantExtractor). Opt-in via um.multi-
tenant.enabled=true.
Optional
Estimated Migration Effort
Project profile Typical effort
Small — consumes only
UserManagementServiceRestClient for
user CRUD
0.5 – 1 day (dependency swap + representation rename +
scope rename)
Medium — embeds UM service, uses
default converters
1 – 2 weeks (above + property migration + event listener
updates)
73

-- 73 of 121 --

Project profile Typical effort
Large — custom
IUserDocumentConversionService,
custom customizers, or embedded UM
with bespoke auth JSON
2 – 4 weeks (above + SPI migration + multi-tenant decision
+ integration tests)
Compatibility Matrix
The refactored modules target:
• JDK 21 (was 17 in late-legacy releases)
• Node 22, NPM 8.13.2 (frontend only)
• Gradle 8.7
• Spring Boot 3.x (same major as the final legacy release)
How This Chapter Is Organized
Module-Level Migration
Rename Pattern
Every uaa-user-management-* artifact is replaced by an uaa-usermanagement-* artifact under the uaa-
usermanagement/ umbrella project. The Gradle coordinate changes from:
:uaa-user-management-<suffix>
to:
:uaa-usermanagement:uaa-usermanagement-<suffix>
Maven group / artifact ids follow the same shape.
Legacy → New Module Mapping
Legacy module New module(s) Kind Notes
uaa-user-management-
user
uaa-usermanagement-common Split +
rename
Domain POJOs (User, Role,
AccessRight) replaced by
UM*Representation classes;
shared contracts (events,
constants, TenantContext)
moved here.
uaa-user-management-
service
uaa-usermanagement-service +
uaa-usermanagement-idp + uaa-
usermanagement-keycloak
Split Core UM kept in -service; IDP
abstraction extracted to -idp;
Keycloak implementation split
into -keycloak.
74

-- 74 of 121 --

Legacy module New module(s) Kind Notes
uaa-user-management-
rest-client
uaa-usermanagement-rest-
client-api + uaa-
usermanagement-rest-client
Split Interface contract in -rest
-client-api (depends only on
-common); HTTP implementation
+ Spring Boot auto-config in
-rest-client.
uaa-user-management-
rest-client-test
uaa-usermanagement-rest-
client-test
Rename +
refactor
Reorganized test structure;
better multi-tenant coverage.
uaa-user-management-
keycloak-extension
uaa-usermanagement-keycloak-
plugin (+ -keycloak, -idp)
Split +
rename
The deployable Keycloak plugin
JAR is built from -keycloak
-plugin; Keycloak SPI logic lives
in -keycloak; IDP abstraction in
-idp.
uaa-user-management-
e2e-testing
uaa-usermanagement-e2e-testing Rename Functionally equivalent.
uaa-user-management-
realm
uaa-usermanagement-
workspaces
Removed Keycloak realm template JSON
uaa-user-management-
module
(frontend — out of scope) N/A TypeScript API-extractor
module.
uaa-user-management-
api-testing
replaced by uaa-usermanagement-
rest-client-test
Removed Postman-based tests superseded
by Java integration tests.
uaa-user-management-
codemod
(frontend — out of scope) N/A TypeScript codemod tooling.
New Modules Introduced
Module Purpose
uaa-usermanagement-common DTOs (UM*Representation), events, constants, storage interfaces,
TenantContext, exception types. Depended on by every other UM
module.
uaa-usermanagement-service Core UM service: REST controllers, service layer, converters,
validators, auto-config, authorization wiring. Produces the Spring
Boot service binary.
uaa-usermanagement-idp IDP-neutral abstraction: IDPUserService, IDPRoleService,
IDPUserConverter, IDPRoleConverter, IIDPExceptionHandler,
IDPUserExtensionConverter. Lets projects swap Keycloak for
another IDP.
uaa-usermanagement-keycloak Concrete Keycloak implementation of the IDP abstraction.
Depends on -idp + Keycloak Admin Client.
uaa-usermanagement-keycloak-
plugin
Fat JAR packaged for deploying the Keycloak SPI event listener.
Combines rest-client and Keycloak implementations with
CDI/Spring exclusions.
75

-- 75 of 121 --

Module Purpose
uaa-usermanagement-rest-
client-api
Interface-only REST client contract (UMUserRestClient,
UMRoleRestClient, UMAccessRightRestClient, UMBaseRestClient).
Consumers that only need the contract depend on this.
uaa-usermanagement-rest-client HTTP implementation of the REST client API + Spring Boot auto-
config. Replaces legacy uaa-user-management-rest-client.
uaa-usermanagement-workspaces Ships authorization JSONs (um_basic, um_advanced), rights
definitions, workspace resources consumed by the service at
runtime.
Gradle Dependency Swap
Replace legacy dependencies with the new module coordinates:
dependencies {
// Before
implementation project(':uaa-user-management-service')
implementation project(':uaa-user-management-rest-client')
implementation project(':uaa-user-management-user')
// After
implementation project(':uaa-usermanagement:uaa-usermanagement-service')
implementation project(':uaa-usermanagement:uaa-usermanagement-rest-client')
implementation project(':uaa-usermanagement:uaa-usermanagement-common')
// Alternatively, if the project only needs the REST client contract (no HTTP
impl):
implementation project(':uaa-usermanagement:uaa-usermanagement-rest-client-api')
}
For Maven/Ivy-style coordinates, update the artifact id the same way (uaa-user-management-* → uaa-
usermanagement-*); group id is unchanged.
Removed Modules — Migration Notes
uaa-user-management-tool
This CLI module is not being migrated. Projects that relied on it for batch tech-user registration
should:
1. Use UMUserRestClient.create(…) in a standalone Java program, or
2. Prepare a YAML file and call the controller endpoint PUT /user/upload (import users as YAML)
directly.
uaa-user-management-api-testing
The Postman-based API test collection is replaced by Java integration tests. Projects previously
using the collection should rewrite their tests on top of uaa-usermanagement-rest-client-test
patterns (Spring Boot test harness + the new REST client API).
76

-- 76 of 121 --

Class-Level Migration
Representation POJOs (replace legacy User/Role/AccessRight)
The legacy domain POJOs live in uaa-user-management-user:
• com.mgmtp.a12.uaa.usermanagement.User
• com.mgmtp.a12.uaa.usermanagement.Role
• com.mgmtp.a12.uaa.usermanagement.AccessRight
• com.mgmtp.a12.uaa.usermanagement.ExtendedUser
They are replaced by builder-based *Representation POJOs in uaa-usermanagement-common:
New class Package Legacy equivalent
UMBaseRepresentation com.mgmtp.a12.uaa.usermanagement.repres
entations
(none — new common
base; holds docRef, id,
tenantId)
UMUserRepresentation com.mgmtp.a12.uaa.usermanagement.repres
entations
User / ExtendedUser
UMRoleRepresentation com.mgmtp.a12.uaa.usermanagement.repres
entations
Role
UMAccessRightRepresentatio
n
com.mgmtp.a12.uaa.usermanagement.repres
entations
AccessRight
Typical migration:
// Before
User u = new User();
u.setUsername("alice");
u.setEmail("alice@example.com");
// After
UMUserRepresentation u = UMUserRepresentation.builder()
.username("alice")
.email("alice@example.com")
.build();
Project-specific extensions (previously done by subclassing ExtendedUser) move to the extension
blob and a project-supplied UserExtensionConverter<T> bean (see Extension SPIs a Project Typically
Implements).
Creating Customized Entities via Generics
The service interfaces are generic over UMBaseRepresentation. A project that needs custom user
fields extends UMUserRepresentation and parameterizes the service with the subtype — no override,
subclass, or wrapper service is required.
77

-- 77 of 121 --

Step 1 — Subclass the base representation
public class ExtendedUser extends UMUserRepresentation {
private String password;
private String phoneNumber;
public ExtendedUser() { super(); }
public String getPassword() { return password; }
public void setPassword(String password) { this.password = password; }
public String getPhoneNumber() { return phoneNumber; }
public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}

Step 2 — Tell UM to instantiate the subclass (REQUIRED whenever you extend
UMUserRepresentation)
If the project supplies a UserExtensionConverter<ExtendedUser>, it must also
register a Supplier<ExtendedUser> bean. This pair is not optional and not
independent — UMUserServiceImpl resolves the representation factory at read time
and hands the resulting instance to the converter. Without the supplier, UM falls
back to UMUserRepresentation::new, the converter receives a UMUserRepresentation
instead of ExtendedUser, and the very first field access fails with a
ClassCastException.
Treat the Supplier<T> bean as an integral part of the UserExtensionConverter<T>
contract:
• Project extends UMUserRepresentation → new subtype T
• Project registers UserExtensionConverter<T> → tells UM how to marshal T
• Project registers Supplier<T> → tells UM which type to instantiate
Missing any one of the three silently breaks the other two.
The bean itself is a one-liner — the service-example ships exactly this shape:
@Configuration
public class ExtendedUserConfiguration {
/**
* Tells UMUserServiceImpl to create ExtendedUser instances (instead of plain
UMUserRepresentation)
* when reading users from the database. Required whenever a
UserExtensionConverter<ExtendedUser>
* is present — without it, the converter's convert(DocumentV2, ExtendedUser)
method receives a
* UMUserRepresentation and throws ClassCastException.
78

-- 78 of 121 --

*/
@Bean
public Supplier<ExtendedUser> extendedUserRepresentationFactory() {
return ExtendedUser::new;
}
}
The same rule applies to REST-client consumers that work with an extended user subtype. If the
project receives an ExtendedUser over HTTP via UMUserRestClient<ExtendedUser> and feeds it through
a UserExtensionConverter<ExtendedUser> on a downstream service, the downstream service must
also register a Supplier<ExtendedUser> bean — the rule is about the UM service reading the
document, and every UM service instance needs it.
Step 3 — Register a UserExtensionConverter<ExtendedUser>
See How UserExtensionConverter Fits into UMUserService Lifecycle for how UM calls into it during
CRUD. Recall from Step 2 that this converter and the Supplier<T> bean must ship together.
Step 4 — Inject the parameterized service
The same bean that projects inject as UMUserService<UMUserRepresentation> can be typed to the
subclass. Spring re-binds the generic parameter at injection time:
@Service
public class UserProfileService {
@Inject
@ServiceType(type = ServiceType.Type.NON_BACKEND_AUTHENTICATION)
private UMUserService<ExtendedUser> userService;
public ExtendedUser getExtendedUser(String docRef) {
return userService.get(docRef); // returns an ExtendedUser with
password/phoneNumber populated
}
public void updatePhoneNumber(String docRef, String phoneNumber) {
ExtendedUser user = userService.get(docRef);
user.setPhoneNumber(phoneNumber);
userService.update(user); // round-trips through
UserExtensionConverter
}
}
The service layer does not need to know about ExtendedUser. The Supplier bean controls
instantiation; the UserExtensionConverter<ExtendedUser> bean controls field marshalling; callers
declare their preferred subtype via the generic parameter.
How UserExtensionConverter Fits into UMUserService Lifecycle
UserExtensionConverter<T> has four methods — two for the CRUD round-trip (convert(T,
79

-- 79 of 121 --

DocumentV2) and convert(DocumentV2, T)), and two optional ones for import / export customisation
(toExportData, fromImportData). UM calls them at well-defined points in each service operation.
Create / update flow (UMUserService.create(T) and .update(T))
project code
userService.create(extendedUser)
│
▼
┌─────────────────────────────────────────
───────────────────┐
│ UMUserServiceImpl │
│ │
│ 1. core UM converter maps common fields │
│ (username, email, firstName, roles, ...) │
│ → partial DocumentV2 │
│ │
│ 2. UserExtensionConverter.convert(T, DocumentV2) ◀──── project-supplied
│ writes extension fields │
│ (e.g. password, phoneNumber) │
│ → final DocumentV2 │
│ │
│ 3. UMUserDocumentEventCustomizer │
│ customizeBeforeCreateUser / customizeBeforeUpdateUser│
│ │
│ 4. Dataservices persistence │
│ + UMUser*Event published │
└─────────────────────────────────────────
───────────────────┘
Read flow (.get(docRef), .getById(id), .search(…), .list(), .simpleSearch(…))
┌─────────────────────────────────────────
───────────────────┐
│ UMUserServiceImpl │
│ │
│ 1. load DocumentV2 from persistence │
│ │
│ 2. instantiate T via the Supplier<T> bean ◀──── project-supplied (*)
│ (falls back to UMUserRepresentation::new) │
│ │
│ 3. core UM converter fills the common fields │
│ (username, email, firstName, roles, ...) │
│ │
│ 4. UserExtensionConverter.convert(DocumentV2, T) ◀──── project-supplied
│ reads extension fields into T │
│ │
│ 5. returned to caller as T │
└─────────────────────────────────────────
───────────────────┘
80

-- 80 of 121 --

│
▼
project code
user.getPhoneNumber()

Step 2 is where the Supplier<T> requirement bites. When a
UserExtensionConverter<ExtendedUser> is registered but no Supplier<ExtendedUser>
bean is present, step 2 falls back to UMUserRepresentation::new, and step 4 hands a
UMUserRepresentation to a converter expecting ExtendedUser — producing a
ClassCastException on the very first field access. See Step 2 — Tell UM to instantiate
the subclass (REQUIRED whenever you extend UMUserRepresentation).
Export flow (UserImportExportService)
┌─────────────────────────────────────────
───────────────────┐
│ UserImportExportService │
│ │
│ 1. read all users via UMUserService.list() │
│ (each is a T with extension fields populated) │
│ │
│ 2. UserExtensionConverter.toExportData(users) ◀──── project-supplied
│ - returns non-null → custom YAML shape │
│ - returns null → default UserImportExport shape │
│ │
│ 3. serialize via YAMLMapper │
└─────────────────────────────────────────
───────────────────┘
Import flow
┌─────────────────────────────────────────
───────────────────┐
│ UserImportExportService │
│ │
│ 1. UserExtensionConverter.fromImportData(stream) ◀──── project-supplied
│ - returns non-null → list of T │
│ - returns null → default YAML → UserImportExport │
│ → UMUserRepresentation list │
│ │
│ 2. for each user: UMUserService.importUsers(list) │
│ which upserts via create() or update() │
│ (invokes the create/update flow above) │
└─────────────────────────────────────────
───────────────────┘
The toExportData / fromImportData hooks return null by default — projects override them only when
the legacy YAML wrapping shape differs from the built-in UserImportExport POJO.
81

-- 81 of 121 --

How IDPUserConverter and IDPRoleConverter Fit into IDP Synchronization
The IDP converter layer (in uaa-usermanagement-idp) converts between DocumentV2 and IDP-specific
representations (for example, Keycloak UserRepresentation / role strings). The Keycloak
implementation in uaa-usermanagement-keycloak provides the concrete converters; projects can
replace them with custom IDP adapters.
IDP user sync — create / update flow
┌─────────────────────────────────────────
───────────────────┐
│ UserEventListener (after DataServices create/update) │
│ │
│ 1. load user DocumentV2 from the event │
│ │
│ 2. IDPUserConverter.toIDPUser(DocumentV2) ◀──── IDP implementation
│ maps common fields (username, email, firstName, │
│ lastName, enabled, roles) into IDP user object │
│ │
│ 3. IDPUserExtensionConverter.convert( ◀──── project-supplied
(optional)
│ DocumentV2, IDPUser) │
│ customises IDP user with extra fields │
│ │
│ 4. IDPUserService.createUser / updateUser │
│ calls the IDP (e.g. Keycloak Admin REST API) │
│ │
│ 5. publish UMAfterSynchronizationEvent<T> │
│ (synchronizationType, dataServicesDocument, │
│ IDPPayload) │
└─────────────────────────────────────────
───────────────────┘
IDP user sync — delete flow
┌─────────────────────────────────────────
───────────────────┐
│ UserEventListener (after DataServices delete) │
│ │
│ 1. load user DocumentV2 from the event │
│ │
│ 2. IDPUserService.deleteUser(userId) │
│ removes the user from the IDP │
│ │
│ 3. publish UMAfterSynchronizationEvent<T> │
│ (DELETE_USER, dataServicesDocument, IDPPayload) │
└─────────────────────────────────────────
───────────────────┘
82

-- 82 of 121 --

IDP role sync flow
┌─────────────────────────────────────────
───────────────────┐
│ RoleEventListener (after DataServices create/delete) │
│ │
│ 1. load role DocumentV2 from the event │
│ │
│ 2. IDPRoleConverter.toIDPRole(DocumentV2) ◀──── IDP implementation
│ maps role name to IDP role string │
│ │
│ 3. IDPRoleService.createRole / deleteRole │
│ calls the IDP │
│ │
│ 4. publish UMAfterSynchronizationEvent<T> │
│ (CREAT_ROLE / DELETE_ROLE, dataServicesDocument, │
│ IDPPayload) │
└─────────────────────────────────────────
───────────────────┘
Replacing IUserIDPCustomizationService with UMAfterSynchronizationEvent
The legacy IUserIDPCustomizationService let projects hook into the sync lifecycle by implementing
an interface. In the refactored module, post-synchronization customisation is event-driven:
// Before — legacy interface implementation
@Component
public class MyIDPCustomizer implements IUserIDPCustomizationService<ExtendedUser,
UserRepresentation> {
@Override
public void customizeAfterCreateUser(ExtendedUser user, UserRepresentation
idpUser) {
// custom logic after IDP create
}
}
// After — Spring event listener
@Component
public class MyIDPSyncListener {
@EventListener
public void afterSynchronizationCreateUser(UMAfterSynchronizationEvent<String>
event) {
if (event.synchronizationType() == UMAfterSynchronizationEvent
.SynchronizationType.CREATE_USER) {
DataServicesDocument dsDoc = event.dataServicesDocument();
String userIdpId = event.IDPPayload();
// custom logic after IDP create
}
83

-- 83 of 121 --

}
}
The SynchronizationType enum values are: CREATE_USER, UPDATE_USER, DELETE_USER, CREAT_ROLE,
DELETE_ROLE.
UMUserDocumentEventCustomizer — Removed customizeAfter* Hooks
The legacy IUserDocumentCustomizationService provided six hooks — three customizeBefore* and
three customizeAfter*:
• customizeBeforeCreateUser / customizeAfterCreateUser
• customizeBeforeUpdateUser / customizeAfterUpdateUser
• customizeBeforeDeleteUser / customizeAfterDeleteUser
The refactored UMUserDocumentEventCustomizer retains only the three customizeBefore* hooks:
• customizeBeforeCreateUser(DocumentV2)
• customizeBeforeUpdateUser(DocumentV2)
• customizeBeforeDeleteUser(DocumentV2)
The customizeAfter* hooks are removed. Post-operation logic formerly placed in
customizeAfterCreateUser / customizeAfterUpdateUser / customizeAfterDeleteUser should move to
Spring @EventListener methods listening on UMUserAfterCreateEvent, UMUserAfterUpdateEvent, or
UMUserAfterDeleteEvent:
// Before — legacy interface with after-hooks
@Component
public class MyCustomizer implements IUserDocumentCustomizationService {
@Override
public DocumentV2 customizeAfterCreateUser(DocumentV2 userDocument) {
// post-create logic (e.g. clear password fields)
return userDocument.withFieldValue("/user/password", null);
}
// ... other hooks ...
}
// After — event listener for post-operation logic
@Component
public class MyPostOperationListener {
@EventListener
public void onUserCreated(UMUserAfterCreateEvent event) {
DocumentV2 doc = event.latestDocument();
// post-create logic
}
84

-- 84 of 121 --

}
// After — UMUserDocumentEventCustomizer for pre-operation logic only
@Component
public class MyCustomizer implements UMUserDocumentEventCustomizer {
@Override
public DocumentV2 customizeBeforeCreateUser(DocumentV2 userDocument) {
// pre-create logic
return userDocument;
}
@Override
public DocumentV2 customizeBeforeUpdateUser(DocumentV2 userDocument) {
return userDocument;
}
@Override
public DocumentV2 customizeBeforeDeleteUser(DocumentV2 userDocument) {
return userDocument;
}
}
Removed IJwtDecoderConfiguration — Use Oauth2TenantStorage
The legacy IJwtDecoderConfiguration interface allowed projects to customise how the
DynamicJWTDecoder resolved JWK set URIs from JWT issuers. A typical project implementation
translated an issuer URL to a JWK set endpoint (for example, appending /protocol/openid-
connect/certs).
This interface is removed in the refactored module. Instead, register all OAuth2 tenants through
Oauth2TenantStorage. Each Tenant object contains the issuer URI and the JWK set URI. The refactored
DynamicJWTDecoder looks up the tenant by issuer from Oauth2TenantStorage and builds the JWT
decoder automatically — no project-side decoder-configuration interface is needed.
// Before — project implemented IJwtDecoderConfiguration
@Component
public class ProjectJwtDecoderConfiguration implements IJwtDecoderConfiguration {
@Override
public String getJwkSetUri(String issuer) {
return issuer + "/protocol/openid-connect/certs";
}
}
// After — register tenants via Oauth2TenantStorage (auto-configured or custom bean)
// No IJwtDecoderConfiguration implementation needed.
// DynamicJWTDecoder resolves issuers from Oauth2TenantStorage automatically.
For static deployments, the Oauth2TenantStorage is populated from tenant-
85

-- 85 of 121 --

registration[*].synchronization.* properties at startup (via UMOauth2AutoConfiguration). For
dynamic deployments, implement the Oauth2TenantStorage SPI and call storeTenant() /
removeTenant() at runtime.
Removed MultipleRealmsConfiguration — Use UMTenantSelector and Document Properties
The legacy MultipleRealmsConfiguration interface served two purposes:
1. Resolve the current IDP registration key (which Keycloak realm to sync with).
2. Provide the document field paths that carried the tenant/IDP identifier on user, role, and access-
right documents.
Both purposes are now handled by dedicated, finer-grained mechanisms:
Legacy method Replacement
getIdpRegistrationKey() UMTenantSelector.getActiveTenant() — resolves the active
tenant key from TenantContext. See Multi-Tenancy (New
Concept).
getUserDocumentIdpIdentifierField() …um.user-document-properties.user-tenant-path (property,
default empty)
getRoleDocumentIdpIdentifierField() …um.role-document-properties.role-tenant-path (property,
default empty)
getAccessRightDocumentIdpIdentifierFi
eld()
…um.access-right-document-properties.access-right-
tenant-path (property, default empty)
Projects that previously extended MultipleRealmsConfiguration (for example,
ProjectMultipleRealmsConfiguration) should:
1. Remove the implementation class.
2. Implement UMTenantSelector if custom tenant-resolution logic is needed (otherwise the default
DefaultUmTenantSelector suffices).
3. Set the tenant-path properties in application.yml / application.properties to the document field
paths that carried the IDP identifier.
// Before — legacy MultipleRealmsConfiguration
@Component
public class ProjectMultipleRealmsConfiguration implements MultipleRealmsConfiguration
{
@Override
public String getIdpRegistrationKey() { return "myRealm"; }
@Override
public String getUserDocumentIdpIdentifierField() { return "/user/tenant"; }
@Override
public String getRoleDocumentIdpIdentifierField() { return "/role/tenant"; }
@Override
public String getAccessRightDocumentIdpIdentifierField() { return
"/accessRight/tenant"; }
86

-- 86 of 121 --

}
// After — configure via properties + optional UMTenantSelector bean
// application.yml:
// mgmtp.a12.uaa.user-management.um.user-document-properties.user-tenant-
path=/user/tenant
// mgmtp.a12.uaa.user-management.um.role-document-properties.role-tenant-
path=/role/tenant
// mgmtp.a12.uaa.user-management.um.access-right-document-properties.access-right-
tenant-path=/accessRight/tenant
Injecting UMUserService with BACKEND_AUTHENTICATION vs NON_BACKEND_AUTHENTICATION
UM registers two beans for each of UMUserService, UMRoleService, and UMAccessRightService. They
differ only in whether their method invocations run inside the UAA "backend authentication"
context.
Variant Purpose
NON_BACKEND_AUTHENTICATION (default) Uses the principal from the incoming request.
Authorization, tenant filtering, and the username self-
delete policy all apply. Inject this variant in controllers and
in application services that act on behalf of a human
caller.
BACKEND_AUTHENTICATION Executes with a synthetic backend-admin principal (…
um.backend-authentication-username, default
UserManagementSupperAdmin). Bypasses non-admin filters.
Inject this variant in system-to-system flows: event
listeners triggered by IDP synchronization, Keycloak
plugin operations, scheduled jobs, the
assignUpdatedRoleForUsers cross-entity propagation.
Bean registration (from UMAutoConfiguration)
Each service is registered twice with the @ServiceType qualifier:
@Bean
@ServiceType(type = ServiceType.Type.BACKEND_AUTHENTICATION)
public UMUserService createUMBackendAuthUserService() { ... }
@Bean
@ServiceType(type = ServiceType.Type.NON_BACKEND_AUTHENTICATION)
public UMUserService createUMUserService() { ... }
How backend-authentication works internally
UM uses Spring AOP via BackendAuthenticationAspect. The aspect wraps every public method of a
bean qualified with @ServiceType(BACKEND_AUTHENTICATION), pushing the backend-admin principal
onto the UAA security context, running the method, and restoring the original principal on exit.
87

-- 87 of 121 --

Projects do not need to configure this aspect — it is registered automatically.
Project-side injection — pick the qualifier at the injection point
@Service
public class MyProjectService {
// Acts on behalf of the incoming HTTP user
@Inject
@ServiceType(type = ServiceType.Type.NON_BACKEND_AUTHENTICATION)
private UMUserService<ExtendedUser> userService;
// Internal scheduled job — run as backend admin
@Inject
@ServiceType(type = ServiceType.Type.BACKEND_AUTHENTICATION)
private UMUserService<ExtendedUser> backendUserService;
@Inject
@ServiceType(type = ServiceType.Type.BACKEND_AUTHENTICATION)
private UMRoleService backendRoleService;
@Inject
@ServiceType(type = ServiceType.Type.BACKEND_AUTHENTICATION)
private UMAccessRightService backendAccessRightService;
}
Injecting UMUserService (or UMRoleService, UMAccessRightService) without a @ServiceType qualifier
fails — Spring sees two matching beans and cannot decide. Always annotate the field, method
parameter, or constructor parameter with the variant the caller needs.
Table 1. When to pick which variant
Caller Variant to inject
REST controller NON_BACKEND_AUTHENTICATION — honours the request user’s
permissions.
Application service invoked
from a controller
NON_BACKEND_AUTHENTICATION — propagates the HTTP user’s auth.
UM-internal event listener
(RoleEventListener, IDP sync
handlers)
BACKEND_AUTHENTICATION — propagates role updates across all
users regardless of the triggering user’s permissions.
Keycloak plugin, scheduled job,
startup data loader
BACKEND_AUTHENTICATION — no HTTP request context exists;
backend admin is required.
Custom validator / customizer
that needs to read sibling
documents
BACKEND_AUTHENTICATION when the read must not be restricted by
the request user’s tenant or filter policies; otherwise
NON_BACKEND_AUTHENTICATION.
88

-- 88 of 121 --

Same Name, New Package
The following classes keep their name but moved package. IDE import updates are required.
Class / interface Legacy FQN New FQN
UserManagementProperti
es
com.mgmtp.a12.uaa.usermanagement.u
m.UserManagementProperties
com.mgmtp.a12.uaa.usermanagement.c
onfig.UserManagementProperties
UserManagementRestClie
ntProperties
com.mgmtp.a12.uaa.um.client.rest.U
serManagementRestClientProperties
com.mgmtp.a12.uaa.usermanagement.r
est.UserManagementRestClientProper
ties
IIDPExceptionHandler com.mgmtp.a12.uaa.usermanagement.i
dp.IIDPExceptionHandler
com.mgmtp.a12.uaa.usermanagement.e
xception.IIDPExceptionHandler
UserManagementProperties has also been restructured internally (nested groups UM, UMTenant,
MultiTenant, UMUserDocumentProperties, RoleDocumentProperties, AccessRightDocumentProperties) —
see Configuration Properties Migration.
Removed Public Java APIs and Their Replacements
Legacy API Replacement Notes
UserService
(…um.a12internal.service)
UMUserService<T> (public) Generics-based; now in public
API surface.
RoleService UMRoleService —
AccessRightService UMAccessRightService —
UserManagementServiceRestClien
t (monolithic)
UMUserRestClient<T> +
UMRoleRestClient +
UMAccessRightRestClient
Split by entity.
UserManagementRestClientConfig
uration (auto-config)
UserManagementRestClientAutoCo
nfiguration
Registered via
AutoConfiguration.imports
instead of spring.factories.
UserManagementEndPoint
(constants)
(internal — no longer public) Endpoint paths live inside the
REST client implementation.
IUserDocumentConversionService
<U>
UserExtensionConverter<T> Single extension point.
IUserDocumentCustomizationServ
ice<U>
UMUserDocumentEventCustomizer Pre-operation hooks (create /
update / delete).
IUserIDPConversionService<U,
I>
IDPUserConverter<T> +
IDPUserExtensionConverter<T>
Split into core converter +
extension SPI.
IUserIDPCustomizationService<U
, I>
IDPUserExtensionConverter<T> Merged into the extension SPI.
89

-- 89 of 121 --

Legacy API Replacement Notes
IDPTrustedIssuersStorage Oauth2TenantStorage Trusted-issuer management
now handled by registering
OAuth2 tenants through
Oauth2TenantStorage. Projects no
longer store individual issuer
strings; they store Tenant objects
that include issuer URI, JWK set
URI, and audience.
IDPSynchronisationConfiguratio
nStorage
UMTenantRegistrationStorage IDP sync configuration is now
part of the per-tenant
registration
(UMTenant.Synchronization).
Projects that dynamically
managed sync configs must
implement
UMTenantRegistrationStorage
and use the tenant-
registration[*].synchronizatio
n.* property structure.
IJwtDecoderConfiguration Oauth2TenantStorage +
DynamicJWTDecoder
Removed entirely. The legacy
IJwtDecoderConfiguration
interface let projects control
how JWK set URIs were
resolved from issuers. In the
refactored module, register all
OAuth2 tenants through
Oauth2TenantStorage instead —
DynamicJWTDecoder looks up the
matching tenant by issuer and
builds the JwtDecoder
automatically. No project-side
interface implementation is
needed.
90

-- 90 of 121 --

Legacy API Replacement Notes
MultipleRealmsConfiguration UMTenantSelector + document-
property paths
Removed entirely. The legacy
MultipleRealmsConfiguration
provided the IDP registration
key and the document field
paths for tenant-id stamping. In
the refactored module: (1) use
UMTenantSelector to resolve the
active tenant (see Multi-
Tenancy (New Concept)), and
(2) configure tenant-path fields
via …um.user-document-
properties.user-tenant-path, …
um.role-document-
properties.role-tenant-path,
and …um.access-right-
document-properties.access-
right-tenant-path.
IUserIDPCustomizationService<U
, I> (post-sync hooks)
UMAfterSynchronizationEvent<T> The legacy
IUserIDPCustomizationService
allowed projects to run custom
logic after IDP synchronization
operations. This is replaced by
listening to the
UMAfterSynchronizationEvent<T>
Spring event, which carries
synchronizationType (enum:
CREATE_USER, UPDATE_USER,
DELETE_USER, CREAT_ROLE,
DELETE_ROLE),
dataServicesDocument, and the
IDP payload. Register an
@EventListener method instead
of implementing the interface.
IIDPService / IIDPRoleService IDPUserService<T, ID> /
IDPRoleService<T, ID>
Generic signatures; now
defined in uaa-usermanagement-
idp.
ExtendedUserDocumentConversion
Service
UserExtensionConverter<T> Same extension point as
IUserDocumentConversionService.
UserManagementCustomizer
(internal)
UMUserEventCustomizer (internal)
+ UMUserDocumentEventCustomizer
(public SPI)
Post-operation behavior moves
to Spring events (see Public
Events — New, Removed,
Replaced).
91

-- 91 of 121 --

New Public Java APIs
Service layer:
API Purpose
UMBaseService<T extends
UMBaseRepresentation>
Generic CRUD contract: create, search(QueryRoot), list,
simpleSearch(SearchOptions), getById, get, update, delete.
UMUserService<T extends
UMUserRepresentation>
Extends UMBaseService<T>. Adds
assignUpdatedRoleForUsers(…), importUsers(List<T>).
UMRoleService Extends UMBaseService<UMRoleRepresentation>. Adds
searchByNameAndClient(String, String),
importRoles(List<UMRoleRepresentation>).
UMAccessRightService Extends UMBaseService<UMAccessRightRepresentation>. Adds
isAssignedToAnyRole(String).
ServiceType (annotation qualifier) BACKEND_AUTHENTICATION vs NON_BACKEND_AUTHENTICATION —
selects which service-type variant a consumer is asking
for.
IDP layer (in uaa-usermanagement-idp):
API Purpose
IDPUserService<T, ID>,
IDPRoleService<T, ID>
IDP-agnostic user/role service contract.
IDPUserConverter<T>,
IDPRoleConverter<T>
Convert DocumentV2 ↔ IDP representation.
IDPUserExtensionConverter<T> Public SPI for customising IDP user objects after the
default conversion.
IIDPExceptionHandler Translate IDP-specific exceptions into UM runtime
exceptions.
Extension SPIs a Project Typically Implements
These are the Spring beans that downstream projects most commonly provide.
SPI When to implement
UserExtensionConverter<T extends
UMUserRepresentation>
When the project extends UMUserRepresentation with
custom fields or needs custom user import/export formats.
Replaces IUserDocumentConversionService and
AdditionalUserConvertor. Companion requirement:
always ship a Supplier<T> bean alongside — see Step 2 —
Tell UM to instantiate the subclass (REQUIRED whenever
you extend UMUserRepresentation).
92

-- 92 of 121 --

SPI When to implement
UMUserDocumentEventCustomizer When the project needs to mutate the user DocumentV2
before create/update/delete (for example, stamping
auditing fields). Replaces
IUserDocumentCustomizationService.
IDPUserExtensionConverter<T> When the project needs to customise the IDP user object
after the default IDPUserConverter ran. Replaces
IUserIDPCustomizationService.
UMTenantRegistrationStorage When the project needs runtime tenant CRUD (backed by
DB, Redis, etc.) instead of static application.yml tenants.
UMTenantSelector When the project needs custom logic to resolve the active
tenant or the user’s home tenant.
Oauth2TenantStorage When the project needs dynamic OAuth2 issuer
registration (for example, Keycloak realms provisioned at
runtime).
AllowedOriginsStorage When the project needs dynamic CORS allowed-origins
configuration.
Cacheable variants exist for each storage SPI: CacheableUMTenantRegistrationStorage,
CacheableAllowedOriginsStorage, CacheableOauth2TenantStorage. They are selected automatically
when mgmtp.a12.uaa.user-management.um.cached-storage.enabled=true and a CacheManager bean is
present.
Public Events — New, Removed, Replaced
All UM events in the new module carry the UM prefix. The legacy generic event classes
(UserAfterCreateEvent<U>, UserAfterUpdateEvent<U>, UserAfterDeleteEvent<U>) are replaced by non-
generic Java record types. Listeners must update their method signatures and payload field names.
Legacy event New event Change
UserAfterCreateEvent<U> (class) UMUserAfterCreateEvent (record) Non-generic; payload
fields:
userDataServicesDocume
nt, latestDocument (was
createdDocument).
UserAfterUpdateEvent<U> UMUserAfterUpdateEvent (record) Payload fields:
userDataServicesDocume
nt, latestDocument (was
updatedDocument).
UserAfterDeleteEvent<U> UMUserAfterDeleteEvent (record) Payload fields:
userDataServicesDocume
nt, latestDocument (was
deletedDocument).
93

-- 93 of 121 --

Legacy event New event Change
IDPAdminClientServiceRemoveEvent UMTenantRemoveEvent (extends
ApplicationEvent)
Renamed and tenant-
centric; payload:
idpKey. Implementers
of
UMTenantRegistrationSt
orage.removeTenant()
must fire this event
from their
implementation.
(none — internal sync was implicit) UMSynchronizationTriggerEvent
(record)
Explicit IDP-sync
trigger. Payload:
synchronizationType
(enum: CREATE_USER,
UPDATE_USER,
DELETE_USER,
CREATE_ROLE,
DELETE_ROLE),
dataServicesDocument.
(none) UMAfterSynchronizationEvent<T>
(record)
Post-sync hook with
generic IDP payload
type. Payload:
synchronizationType,
dataServicesDocument,
IDPPayload.
Listener migration example:
// Before
@EventListener
public void onUserCreated(UserAfterCreateEvent<ExtendedUser> event) {
DocumentV2 doc = event.getCreatedDocument();
ExtendedUser user = event.getUser();
...
}
// After
@EventListener
public void onUserCreated(UMUserAfterCreateEvent event) {
DocumentV2 doc = event.latestDocument();
DataServicesDocument ds = event.userDataServicesDocument();
...
}
Customizer Interfaces
The customizer SPI surface in the new module:
94

-- 94 of 121 --

Interface Role
UMUserDocumentEventCustomizer (public
SPI)
Project-side customisation of the user DocumentV2 before
create / update / delete. Three hooks:
customizeBeforeCreateUser, customizeBeforeUpdateUser,
customizeBeforeDeleteUser.
UMUserEventCustomizer (internal
default)
Built-in default customiser that applies tenant stamping,
UUID generation, lowercasing of IDP fields. Delegates to
the optional project UMUserDocumentEventCustomizer.
UMRoleEventCustomizer (internal
default)
Default role customiser: sets default client, applies tenant
stamping, updates user roles when a role changes.
UMAccessRightEventCustomizer (internal
default)
Default access-right customiser: applies tenant stamping.
Post-operation behaviour that the legacy UserManagementCustomizer handled via customizeAfterXxx()
hooks now arrives as Spring @EventListener`s on the `UMUser*Event records.
Exception Types
The new module currently relies on RuntimeException / IllegalArgumentException /
NoSuchElementException raised by service and validator code; UMExceptionsHandler maps them to
HTTP responses. Symbolic keys live in com.mgmtp.a12.uaa.usermanagement.constant.UMExceptionKeys
and UMExceptionMessages. The IIDPExceptionHandler interface (now under
…usermanagement.exception) provides project-side translation of IDP-specific exceptions.
A typed domain-exception hierarchy (UMException + subtypes) is planned as a future enhancement.
Until then, projects that need to differentiate error classes should inspect UMExceptionKeys constants
or the HTTP status code.
Configuration Properties Migration
Property-Prefix Overview
The root prefix is unchanged: mgmtp.a12.uaa.user-management.*. All legacy properties that remain
live under the same root. Many have been reorganized into nested groups:
• mgmtp.a12.uaa.user-management.um.* — core UM settings (unchanged root)
• mgmtp.a12.uaa.user-management.um.multi-tenant.* — new, multi-tenancy settings
• mgmtp.a12.uaa.user-management.um.user-document-properties.* — new, configurable user
document paths
• mgmtp.a12.uaa.user-management.um.role-document-properties.* — new
• mgmtp.a12.uaa.user-management.um.access-right-document-properties.* — new
• mgmtp.a12.uaa.user-management.tenant-registration[*].* — renamed from idp-
registration[*].*
• mgmtp.a12.uaa.user-management.client.rest.* — REST client settings (unchanged)
95

-- 95 of 121 --

Legacy → New Property Mapping
Legacy property New property Change
…um.url …um.url Unchanged
…um.core-model-name …um.user-document-properties.user-
domain-name
Renamed + moved
under user-
document-
properties. Default
is still
DomainUserManageme
nt.
…um.extension-model-name …um.extension-model-name Unchanged
…um.role-access-right-mapping-yaml-
attachment-file-name
…um.role-access-right-mapping-yaml-
attachment-file-name
Unchanged
…um.users-yaml-attachment-file-name …um.users-yaml-attachment-file-name Unchanged
…um.edit-username.enabled …um.edit-username.enabled Unchanged
…um.duplicate-email.enabled …um.duplicate-email.enabled Unchanged
…um.organization-unit-role-
structure.enabled
…um.user-document-
properties.organization-unit-role-
structure.enabled
Moved under
user-document-
properties.
…um.cached-storage.enabled …um.cached-storage.enabled Unchanged
…um.dynamic-realms-config-
support.enabled
…um.dynamic-realms-config-
support.enabled
Unchanged
…um.dynamic-cors-allowed-origins-
config-support.enabled
…um.dynamic-cors-allowed-origins-
config-support.enabled
Unchanged
…um.client-role-support.enabled …um.client-role-support.enabled Unchanged
…idp-registration[key].url …tenant-
registration[key].synchronization.idp
-url
Renamed — IDP-
centric → tenant-
centric.
…idp-registration[key].realm-name …tenant-
registration[key].synchronization.rea
lm-name
Same move.
…idp-registration[key].documents-
idp-identifier
…tenant-
registration[key].management.tenant-
id
Semantic shift —
tenantId replaces
the IDP identifier.
…idp-
registration[key].synchronize.enabled
…tenant-
registration[key].synchronization.ena
bled
Renamed
(synchronize →
synchronization).
…idp-registration[key].idp-
extension-technical.username/password
…tenant-
registration[key].synchronization.idp
-technical-user.username/password
Renamed.
…idp-registration[key].user-
management-
technical.username/password
…tenant-
registration[key].synchronization.um-
technical-user.username/password
Renamed.
96

-- 96 of 121 --

Legacy property New property Change
…idp-registration[key].account.url …tenant-
registration[key].synchronization.acc
ount.url
Moved inside
synchronization.
…client.rest.service-base …client.rest.service-base Unchanged
…client.rest.service-context-path …client.rest.service-context-path Unchanged
Any legacy property not listed here and still referenced by a project should be removed — the
legacy UserManagementProperties class is gone. If in doubt, grep for the property key in the new
com.mgmtp.a12.uaa.usermanagement.config.UserManagementProperties class.
New Property Groups
======= Multi-Tenant
Entirely new; opt-in, defaults are safe for single-tenant deployments.
Property Default Controls
…um.multi-tenant.enabled false Master switch. When true, the
TenantContextInterceptor is installed and
tenant-filter policies in the authorization
JSON apply.
…um.multi-tenant.tenant-header X-Tenant-Id HTTP request header carrying the active
tenant id.
…um.multi-tenant.tenant-claim-name tenants JWT claim name containing the list of
tenants the user can access.
…um.multi-tenant.super-admin-roles [] Roles that grant cross-tenant access
(bypass tenant restrictions).
…um.multi-tenant.super-admin-usernames [] Usernames that grant cross-tenant access.
======= User Document Paths
New in the refactoring — previously hard-coded in Constant.java. Projects with non-default
document models can now adapt without forking.
Property Default Purpose
…um.user-document-
properties.user-id-path
/user/id Document field path for user id.
…um.user-document-
properties.user-name-path
/user/name Username / login name.
…um.user-document-
properties.user-email-path
/user/email Email address.
…um.user-document-
properties.user-first-name-path
/user/firstName Given name.
…um.user-document-
properties.user-last-name-path
/user/lastName Family name.
97

-- 97 of 121 --

Property Default Purpose
…um.user-document-
properties.user-enabled-path
/user/enabled Boolean active flag.
…um.user-document-
properties.user-extension-path
/user/extension Container path for custom
extension fields.
…um.user-document-
properties.user-tenant-path
`` (empty) Tenant-id path. Leave empty to
disable tenant filtering on users; set
to enable.
…um.user-document-
properties.user-role-path
/user/roles Array of assigned roles.
…um.user-document-
properties.user-role-role-name-
path
/user/roles/name Role name within the role array
element.
…um.user-document-
properties.user-organization-unit-
path
/user/orgUnits Array of organization-unit role
assignments.
…um.user-document-
properties.user-organization-unit-
unit-name-path
/user/orgUnits/unitNam
e
Org-unit name.
…um.user-document-
properties.user-organization-unit-
role-name-path
/user/orgUnits/roleNam
e
Role name within the org-unit entry.
…um.user-document-
properties.user-domain-name
DomainUserManagement Document-model domain name
(replaces legacy core-model-name).
…um.user-document-
properties.organization-unit-role-
structure.enabled
false Enables the org-unit / role hierarchy
feature and the
EnumerationController endpoints.
======= Role / AccessRight Document Paths
Property Default Purpose
…um.role-document-
properties.role-tenant-path
`` (empty) Tenant-id path on role documents.
Leave empty for global roles; set to
enable tenant filtering of roles.
…um.access-right-document-
properties.access-right-tenant-
path
`` (empty) Tenant-id path on access-right
documents. Leave empty for global
access rights.
======= Backend-Auth Super Admin
Property Default Purpose
…um.backend-authentication-
username
UserManagementSupperAd
min
Principal name used for internal
service-to-service (backend-
authenticated) calls.
98

-- 98 of 121 --

Tenant Registration — Per-Tenant Configuration
Static multi-tenant configuration uses map-keyed properties. Each key is a tenant id.
Property Purpose
…tenant-
registration[<key>].management.tenant-id
Tenant identifier (typically same as <key>).
…tenant-
registration[<key>].synchronization.idp-url
IDP base URL (default http://localhost:9090).
…tenant-
registration[<key>].synchronization.realm-name
IDP realm name (default user-management).
…tenant-
registration[<key>].synchronization.client-
name
IDP client name for the UM service.
…tenant-
registration[<key>].synchronization.enabled
Enable IDP synchronization for this tenant
(default true).
…tenant-
registration[<key>].synchronization.idp-
technical-user.username/password
IDP-side technical account credentials.
…tenant-
registration[<key>].synchronization.um-
technical-user.username/password
UM-side technical account credentials.
…tenant-
registration[<key>].synchronization.account.ur
l
IDP account-management URL (derived by
default).
For runtime (dynamic) tenant registration instead of static properties, implement
UMTenantRegistrationStorage and enable …um.dynamic-realms-config-support.enabled=true.
Profiles Shipped
The service module ships these Spring profiles under uaa-usermanagement-
service/src/main/resources/config/:
Profile Purpose
um_basic Minimal user-management mode: users only. Loads the
basic authorization JSON and user document model.
um_advanced Layered on top of basic: adds roles, access rights, and
organization-unit / role structure. Loads both basic +
advanced authorization JSONs; enables organization-unit-
role-structure; enables the enumeration REST endpoints.
um_oauth2 OAuth2 resource-server configuration (JWK set URI, issuer
URI) for Keycloak or similar IDPs.
um_tenant Enables multi-tenancy (…multi-tenant.enabled=true) and
default tenant paths (…user-tenant-path=/user/tenant,
etc.).
um_uaa UAA integration (CORS, access-rights resource).
99

-- 99 of 121 --

These profiles compose: a typical tenanted OAuth2 deployment activates
um_advanced,um_oauth2,um_tenant.
Auto-Configuration Registration
The new module ships its auto-configs in META-
INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports (Spring Boot ≥ 2.7
convention). The legacy META-INF/spring.factories is not used.
New auto-config classes:
Class Purpose
UMAutoConfiguration Core beans — document model resolvers, user / role /
access-right services, tenant storage (cacheable or simple),
allowed-origins storage, TenantContextInterceptor.
UMOauth2AutoConfiguration OAuth2 beans — DynamicJWTDecoder, JwtTenantExtractor,
Oauth2TenantStorage, CORS configurers.
UMLocalAutoConfiguration Local-auth beans for development — file-based user
manager wired before LocalUserAutoConfiguration.
IDPAutoConfiguration (in uaa-
usermanagement-idp)
Default IIDPExceptionHandler.
UserManagementRestClientAutoConfigura
tion (in uaa-usermanagement-rest-
client)
User / role / access-right REST client beans.
Projects that previously used @EnableAutoConfiguration(exclude =
UserManagementAutoConfiguration.class) or extended the legacy auto-configs must update the class
references to the new ones.
Removed Properties
Any legacy-only properties not present in the new UserManagementProperties must be removed from
application.yml / application.properties. Notable removals:
• ...idp-registration[*].* — replaced by tenant-registration[*].*.
• …um.core-model-name — replaced by …um.user-document-properties.user-domain-name.
• …um.organization-unit-role-structure.enabled (top-level) — moved under …um.user-document-
properties.organization-unit-role-structure.enabled.
Spring Boot will log an Unknown property warning for stale keys in strict mode, which is a useful first
signal during migration.
Removed Auto-Configuration Beans
The following beans registered by the legacy auto-configuration classes are not present in the
refactored module:
100

-- 100 of 121 --

Removed bean Replacement
IJwtDecoderConfiguration (registered via
UMOauth2AutoConfiguration.createUserManagement
RuntimeJwtDecoderConfiguration())
No replacement interface. Register OAuth2
tenants through Oauth2TenantStorage instead;
DynamicJWTDecoder resolves JWK set URIs from
the stored tenants.
MultipleRealmsConfiguration (registered via
UserManagementAutoConfiguration.multipleRealms
Configuration())
UMTenantSelector for tenant resolution + …user-
document-properties.user-tenant-path / …role-
document-properties.role-tenant-path / …
access-right-document-properties.access-right-
tenant-path properties for document field paths.
IDPTrustedIssuersStorage (registered via
UMOauth2AutoConfiguration.createTrustedIssuers
Storage())
Oauth2TenantStorage. Trusted issuers are derived
from the registered OAuth2 tenants.
IDPSynchronisationConfigurationStorage
(registered via
UserManagementAutoConfiguration.createIDPConfi
gStorage())
UMTenantRegistrationStorage. IDP sync configs
are part of tenant registrations
(UMTenant.Synchronization).
Projects that injected or overrode these beans via @ConditionalOnMissingBean must update their
configuration classes.
Updating the User Document Model (One Model, Not Two)
The refactoring changes how project-specific user fields live in the document model.
Legacy — two separate domain models merged at startup
The legacy service required two document-model JSONs:
• DomainUserManagement.json — the core UM model (id, username, email, roles, …).
• DomainUserExtensionExample.json — a separate project-owned model containing the extension
fields (for example, department, job_title), typically rooted under an extend group.
The legacy service merged the two models at startup, exposing the combined schema to the UI and
to the persistence layer.
New — a single, self-contained DomainUserManagement.json
The refactored service expects one DomainUserManagement.json. Project-specific fields are no longer a
separate model; they live as a nested extension group inside the user root group of the same model
file:
{
"modelRoot": {
"rootGroups": [
{
"type": "Group",
"name": "user",
"Group": {
101

-- 101 of 121 --

"elements": [
{ "type": "Field", "name": "id", "Field": { "fieldType": { "type":
"StringType" } } },
{ "type": "Field", "name": "name", "Field": { "fieldType": { "type":
"StringType" } } },
{ "type": "Field", "name": "email", "Field": { "fieldType": { "type":
"StringType" } } },
"...",
{
"type": "Group",
"name": "extension",
"Group": {
"elements": [
{ "type": "Field", "name": "department", "Field": { "fieldType": {
"type": "StringType" } } },
{ "type": "Field", "name": "job_title", "Field": { "fieldType": {
"type": "StringType" } } }
]
}
}
]
}
}
]
}
}
The path to the extension subtree is controlled by …um.user-document-properties.user-extension-
path (default /user/extension). The UserExtensionConverter (see Extension SPIs a Project Typically
Implements) reads from / writes to this subtree when marshalling project-specific fields to and
from the document.
Migration recipe
1. Open the legacy DomainUserExtensionExample.json and identify every field declared under its
root group (typically extend).
2. Open the legacy DomainUserManagement.json and locate the user root group.
3. Inside the user group, add a new Group element with "name": "extension".
4. Copy every field from the legacy extension model into the new extension group.
5. Delete the legacy extension JSON file — it is no longer loaded.
6. If your extension model root used a name other than extend (for example, customAttributes),
rename it to extension in the new location so it matches the default user-extension-path. If you
cannot rename (for compatibility reasons), change …um.user-document-properties.user-
extension-path to point at the actual path (for example, /user/customAttributes).
7. Update the document model file location to match the workspace the service loads at startup
(for the devapps example, the files live under models/basic/ and models/advanced/).
Ready-to-copy reference
102

-- 102 of 121 --

The merged-model shape is demonstrated end-to-end in:
• uaa-usermanagement/uaa-usermanagement-
workspaces/src/main/resources/um/basic/models/DomainUserManagement.json (core schema
shipped by UM)
• uaa-usermanagement/devapps/uaa-usermanagement-service-
example/src/main/resources/models/basic/DomainUserManagement.json (same schema with
department / job_title added as extension fields)
Use these as the template when merging your project’s legacy extension model into a single file.
REST API Migration
Base-Path and Verb Changes
The refactored controllers follow a cleaner REST convention:
• Path-variable lookups (/user/read/{username}, /role/read/{roleName}) replaced by query-
parameter lookups (?docRef=… or ?id=…) on the entity root.
• Bespoke /create, /update, /delete path segments replaced by proper HTTP verbs (POST, PUT,
DELETE) on the entity root.
• Search moved from URL query params to POST body (QueryRoot or SearchOptions).
• /user-management/* document-metadata endpoints removed entirely — document-model
metadata is now derived from UMUserDocumentProperties at runtime and not exposed over HTTP.
UserController Endpoint Mapping
Base path: #{dataServicesCoreProperties.server.contextPath}/user.
Legacy endpoint New endpoint Notes
POST /user/create (body: JSON) POST /user (body: Object →
converted to UMUserRepresentation
via project-supplied Supplier bean)
Scope: Create User (was
Create User Data
Model).
GET /user/read/{username} GET /user?id={id} or GET
/user?docRef={docRef}
Lookup key changed
from username to id or
docRef. Scope: Read
User.
PUT /user/update PUT /user Scope: Update User.
PUT /user/partial-update (removed) Use full PUT /user. The
partial-update path was
internal-only (Keycloak
sync).
DELETE /user/delete/{username} DELETE /user?docRef={docRef} Lookup key changed.
Scope: Delete User.
103

-- 103 of 121 --

Legacy endpoint New endpoint Notes
GET /user/query?path=&value= POST /user/simple-search (body:
SearchOptions) or POST /user/search
(body: QueryRoot)
Query semantics
redesigned:
SearchOptions for
single-field filters,
QueryRoot for complex
logic operators. Scope:
Read User.
GET /user/export GET /user/export Path unchanged; scope
renamed to Export User
(was Download Users As
Yaml File).
PUT /user/upload PUT /user/upload Path unchanged; scope
renamed to Import User
(was Upload Users Yaml
File). Method renamed
from uploadUsers to
importUsers.
RoleController Endpoint Mapping
Base path: #{dataServicesCoreProperties.server.contextPath}/role.
Legacy endpoint New endpoint Notes
POST /role/create POST /role Scope: Create Role (was
Create Role Document).
GET /role/read/{roleName} GET /role?id={id} or GET
/role?docRef={docRef}
Scope: Read Role.
PUT /role/update PUT /role Scope: Update Role.
POST /role/delete (body: Role) DELETE /role?docRef={docRef} HTTP verb changed
from POST to DELETE;
payload changed from
body to query param.
Scope: Delete Role.
GET /role/accessRightMapping GET /role/accessRightMapping Path unchanged; scope
renamed to Export Role
AccessRight Mapping.
Method renamed from
roleAccessRightMapping
to
exportRoleAccessRightM
apping.
104

-- 104 of 121 --

Legacy endpoint New endpoint Notes
PUT
/role/accessRightMapping/upload
PUT
/role/accessRightMapping/upload
Path unchanged; scope
renamed to Import Role
AccessRight Mapping.
Method renamed from
uploadAccessRightMappi
ng to
importRoleAccessRightM
apping.
AccessRightController — New Endpoints
Access-right management has no legacy HTTP equivalent. Base path:
#{dataServicesCoreProperties.server.contextPath}/accessRight.
Method Path Scope
POST /accessRight (body:
UMAccessRightRepresentation)
Create AccessRight
POST /accessRight/search (body: QueryRoot) Read AccessRight
POST /accessRight/simple-search (body:
SearchOptions)
Read AccessRight
GET /accessRight?docRef={docRef} Read AccessRight
PUT /accessRight (body:
UMAccessRightRepresentation)
Update AccessRight
DELETE /accessRight?docRef={docRef} Delete AccessRight
EnumerationController (Conditional)
Active only when mgmtp.a12.uaa.user-management.um.user-document-properties.organization-unit-
role-structure.enabled=true (enabled by the um_advanced profile). No authentication required;
returns reference data for UI dropdowns.
Method Path Response
GET /enum/ext/organizationUnit List<ExternalEnumResource>
GET /enum/ext/idp/realm/clients List<ExternalEnumResource>
GET /enum/ext/Role-DM List<ExternalEnumeration>
Removed Endpoints
The legacy UserDocumentController (/user-management/*) is entirely removed:
• GET /user-management/loadUserDocument/{id}
• GET /user-management/getChangePasswordUrl
• GET /user-management/getDocumentModel
• GET /user-management/getValidationCode
105

-- 105 of 121 --

• GET /user-management/downloadDocumentModel
Document-model metadata is now resolved internally from the UMUserDocumentProperties bean. If a
project relied on these endpoints (for example, a custom admin UI), it must instead consume
UMUserDocumentProperties directly or invoke the IDP account URL from the tenant registration.
Request / Response Body Shape
• Request bodies migrate from legacy User / Role / AccessRight POJOs to UMUserRepresentation /
UMRoleRepresentation / UMAccessRightRepresentation.
• The UserController create(Object body) and update(Object body) accept raw JSON — it is
converted to the project-specific UMUserRepresentation subclass if a
Supplier<UMUserRepresentation> bean is provided. This preserves the legacy behaviour of
accepting extended user types.
• Export responses keep Content-Type: application/x-yaml.
• Search no longer uses URL query parameters; the body is QueryRoot (POST /search) or
SearchOptions (POST /simple-search).
Error Response Shape
Error responses are produced by UMExceptionsHandler. The current shape is consistent with the
legacy module: HTTP status + JSON body containing a message and (where available) a key from
UMExceptionKeys. A typed domain-exception hierarchy is planned for a future release; projects
should continue to distinguish error cases by HTTP status code and the message key.
REST Client Migration
The monolithic UserManagementServiceRestClient is replaced by three typed clients that mirror the
service layer:
• UMUserRestClient<T extends UMUserRepresentation> — in uaa-usermanagement-rest-client-api
• UMRoleRestClient — in uaa-usermanagement-rest-client-api
• UMAccessRightRestClient — in uaa-usermanagement-rest-client-api
All three extend UMBaseRestClient<T> and share the same CRUD contract: create, search(QueryRoot),
list, simpleSearch(SearchOptions), getById, get(docRef), update, delete(docRef).
Method migration:
Legacy UserManagementServiceRestClient New typed client
createUser(Object) UMUserRestClient.create(T)
readUser(String username) UMUserRestClient.getById(String) or
UMUserRestClient.get(String docRef)
updateUser(Object) UMUserRestClient.update(T)
deleteUser(String) UMUserRestClient.delete(String docRef)
logout() (removed — handled by the authentication
layer)
106

-- 106 of 121 --

Bean injection (Spring):
@Inject
private UMUserRestClient<UMUserRepresentation> userRestClient;
@Inject
private UMRoleRestClient roleRestClient;
@Inject
private UMAccessRightRestClient accessRightRestClient;
REST Client — Export / Import Not Exposed
UMUserRestClient and UMRoleRestClient deliberately do not expose exportUsers, importUsers,
exportRoleAccessRightMapping, or importRoleAccessRightMapping. The REST client is scoped to
representation-level CRUD. Projects that need export / import over HTTP must call the controller
endpoints directly with their own RestTemplate or WebClient:
// Export users as YAML (example using RestTemplate)
ResponseEntity<byte[]> response = restTemplate.exchange(
umBaseUrl + "/user/export",
HttpMethod.GET,
new HttpEntity<>(authHeaders),
byte[].class);
// Import users from YAML file
MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
body.add("file", new FileSystemResource(usersYamlFile));
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.MULTIPART_FORM_DATA);
restTemplate.exchange(
umBaseUrl + "/user/upload",
HttpMethod.PUT,
new HttpEntity<>(body, headers),
Void.class);
Authorization Migration
JSON File Location Change
The legacy module shipped a single authorization JSON at:
uaa-user-management-service/src/main/resources/user-management-authorization.json
The new module ships two files from the uaa-usermanagement-workspaces module, loaded by profile:
107

-- 107 of 121 --

Profile Files loaded
um_basic classpath:um/basic/authorization/user-management-authorization.json
um_advanced Both classpath:um/basic/authorization/user-management-authorization.json
and classpath:um/advanced/authorization/user-management-authorization.json
The um_advanced JSON references policies defined in the um_basic JSON (for example, User
Management Admin Role, Current Login User Is Not IDP Technical User) so they must be loaded
together when advanced features are used.
Projects that register authorization JSONs via mgmtp.a12.uaa.authorization.child-authorization-
definitions must update the classpath references.
Scope Rename Table
Every @PreAuthorize("hasUAAPermission('<scope>')") reference in project code must be checked
against this table. Scope strings change from verbose, inconsistent names to a uniform <Verb>
<Entity> pattern.
Legacy scope New scope Kind
Create User Data Model Create User Renamed
Read User Data Model Read User Renamed
Update User Data Model Update User Renamed
Delete User Data Model Delete User Renamed
Query User Data Model Read User Merged
into read
(new uses
POST
/search)
Download Users As Yaml File Export User Renamed
Upload Users Yaml File Import User Renamed
Create Role Document Create Role Renamed
Read Role Document Read Role Renamed
Update Role Document Update Role Renamed
Delete Role Document Delete Role Renamed
Download Role AccessRight Mapping As Yaml File Export Role AccessRight Mapping Renamed
Upload Role AccessRight Mapping Yaml File Import Role AccessRight Mapping Renamed
(no legacy equivalent) Create AccessRight New
(no legacy equivalent) Read AccessRight New
(no legacy equivalent) Update AccessRight New
(no legacy equivalent) Delete AccessRight New
108

-- 108 of 121 --

Legacy scope New scope Kind
afterCreateUser / afterUpdateUser /
afterDeleteUser
afterCreateUser / afterUpdateUser /
afterDeleteUser
Unchanged
(IDP-sync
scopes)
afterCreateRole afterCreateRole Unchanged
afterDeleteRole afterDeleteRole Unchanged
beforeUpdateRole, afterUpdateRole (removed) Update-
time role
sync no
longer
needed
Query Query Unchanged
— used by
document
read
repository-
refs
Get User Document Model (removed) /user-
management
/getDocume
ntModel
endpoint
removed
Get User Model Validation Code (removed) /user-
management
/getValida
tionCode
endpoint
removed
Download User Document Model (removed) /user-
management
/downloadD
ocumentMod
el
endpoint
removed
Policies Preserved
These policies are defined in the new basic JSON and referenced by permissions:
Policy Purpose
User Management Admin Role Grants access only to users with the userManagementAdmin
role.
109

-- 109 of 121 --

Policy Purpose
Current Login User Is Not IDP
Technical User
Prevents IDP sync loops when the IDP technical account
makes changes (#idpTechnicalUserName !=
principal.username).
User Document Username Filter Policy
(repository policy)
Users without userManagementAdmin see only their own user
document ('user.username:' + principal.username).
User Document Tenant Filter Policy
(repository policy)
Applies
@repositoryTemplateFilterService.generateUserDocumentFil
ter() to queries on DomainUserManagement.
Role Document Tenant Filter Policy
(repository policy, advanced JSON)
Applies tenant filter to Role-DM queries.
AccessRight Document Tenant Filter
Policy (repository policy, advanced
JSON)
Applies tenant filter to AccessRight-DM queries.
Policies Removed
Endpoint-specific policies from the legacy JSON no longer exist; scope-based permissions replace
them:
• User Reading Endpoint Policy
• User Document Reading Policy
• User Pojo Reading Policy
• The legacy empty policy (which always evaluated to true) has been cleaned up and is not present
in the new authorization JSON.
Permission Names in the New JSONs
For reference, the permissions defined in each new JSON (permission name → scope):
Basic JSON (um/basic/authorization/user-management-authorization.json):
• User Document Read Permission → Query
• Create User Permission → Create User
• Read User Permission → Read User
• Update User Permission → Update User
• Delete User Permission → Delete User
• Export User Permission → Export User
• Import User Permission → Import User
• IDP Sync - After User Created → afterCreateUser
• IDP Sync - After User Updated → afterUpdateUser
• IDP Sync - After User Deleted → afterDeleteUser
110

-- 110 of 121 --

Advanced JSON (um/advanced/authorization/user-management-authorization.json):
• User/Role/AccessRight Document Read Permission → Query
• Create Role Permission / Read Role Permission / Update Role Permission / Delete Role Permission
→ Create Role / Read Role / Update Role / Delete Role
• Create AccessRight Permission / Read AccessRight Permission / Update AccessRight Permission /
Delete AccessRight Permission → corresponding AccessRight scopes
• Export Role AccessRight Mapping Permission → Export Role AccessRight Mapping
• Import Role AccessRight Mapping Permission → Import Role AccessRight Mapping
• IDP Sync - After Role Created → afterCreateRole
• IDP Sync - After Role Deleted → afterDeleteRole
Role-Name Conventions
The role string userManagementAdmin is unchanged. Projects keep the same role mapping in their IDP
realm and Spring Security configuration.
Multi-Tenancy (New Concept)
Multi-tenancy is an entirely new feature of the refactored UM service. The legacy module had no
multi-tenant support. This section is longer than the others because it introduces concepts a reader
coming from the legacy module has not seen before.
Concept
A single deployment of the UM service can serve multiple tenants. Each tenant has:
• A distinct tenant id (carried per request — header-provided or JWT-claim-provided)
• Its own IDP registration (Keycloak realm / client / technical user credentials)
• Its own document partition (filtered by a configurable tenant-path on each document)
• Optionally, its own allowed CORS origins
Three orthogonal concepts to keep in mind:
Active tenant
The tenant the current request is operating on — for example, X-Tenant-Id: alpha in the request
header.
User-login tenant
The tenant the authenticated user’s principal belongs to. Used to prevent cross-tenant self-
deletion and similar integrity rules.
Super admin
A role or username that grants access across all tenants, bypassing tenant restrictions.
Configured via …multi-tenant.super-admin-roles / super-admin-usernames.
Multi-tenancy is opt-in. When …multi-tenant.enabled=false (the default) the service behaves as a
111

-- 111 of 121 --

single-tenant deployment and legacy consumers see no difference.
Components
Component Kind Role
TenantContext ThreadLocal holder
(com.mgmtp.a12.uaa.user
management.context)
Stores (activeTenantKey, superAdmin,
accessibleTenantKeys) for the current request
thread. Accessors: getActiveTenantKey(),
isSuperAdmin(), getAccessibleTenantKeys(),
isSet(). Utility: runWithSuperAdmin(Supplier<T>)
executes a block with super-admin privileges.
Must be cleared after the request.
TenantContextIntercept
or
OncePerRequestFilter
(internal)
Reads the tenant header, resolves the active
tenant, sets TenantContext, and clears it in
finally. No-op when …multi-
tenant.enabled=false.
JwtTenantExtractor PropertyExtractor<Jwt>
(internal)
Pulls tenant(s) from the JWT claim (default claim
name: tenants). Falls back to mapping the JWT
issuer’s last path segment to a tenant-
registration key if the claim is absent.
UMTenantRegistrationSt
orage
SPI (in uaa-
usermanagement-
common.storage)
Runtime tenant CRUD: storeTenant(String,
UMTenant), loadTenant(String),
removeTenant(String), loadAll(), isEmpty().
Projects implement this if they want dynamic
tenant registration instead of static properties.
UMTenantSelector SPI Resolves the active tenant and the user’s home
tenant: getActiveTenant(),
getUserLoggedInTenant(). Default returns empty.
UMTenantRegistration /
UMTenant
Data classes (in uaa-
usermanagement-
common.config)
Per-tenant configuration: management.tenantId +
synchronization.* (IDP URL, realm, client,
technical users).
Oauth2TenantStorage SPI Dynamic OAuth2 issuer registration. Methods:
storeTenant(Tenant), removeTenant(Tenant),
getTenantByIssuer(String), loadAll().
AllowedOriginsStorage SPI Dynamic CORS allowed-origins management.
Methods: storeOrigin(String),
removeOrigin(String), loadAll().
DynamicJWTDecoder JWT decoder (internal,
in
…service.config.inter
nal.oauth2)
Resolves the JWK set dynamically from
Oauth2TenantStorage. Enabled when …dynamic-
realms-config-support.enabled=true; registered
as the primary JWT decoder.
UserTenantAccessServic
e
Service Computes the super-admin flag and the set of
accessible tenant keys for the current principal.
Used by TenantContextInterceptor.
112

-- 112 of 121 --

Component Kind Role
Tenant filter policies Authorization JSON
(uaa-usermanagement-
workspaces)
At query time, inject a per-tenant Solr filter via
@repositoryTemplateFilterService.generateUserD
ocumentFilter() / generateRoleDocumentFilter() /
generateAccessRightsDocumentFilter().
End-to-End Request Flow
Client UM Service
| |
| Authorization: Bearer <JWT> |
| X-Tenant-Id: alpha |
|----------------------------------->|
| |
| DynamicJWTDecoder
| | - resolve issuer from JWT
| | - look up in Oauth2TenantStorage
| | - fetch JWK set, decode JWT
| |
| JwtTenantExtractor
| | - read 'tenants' claim
| | - build accessible tenant list
| |
| TenantContextInterceptor
| | - check multi-tenant.enabled
| | - isSuperAdmin? get accessible keys
| | - resolve active tenant from header
| | - TenantContext.set(...)
| |
| Spring Security @PreAuthorize
| | - hasUAAPermission('Read User')
| | - repository tenant-filter policy
| | injects tenant filter into query
| |
| Controller → Service → DataServices
| | - UMUserEventCustomizer stamps
| | tenant id onto the document
| |
|<-----------------------------------| response
| |
| TenantContextInterceptor (finally)
| | - TenantContext.clear()
Project-Side Configuration Checklist
1. Enable the feature:
mgmtp.a12.uaa.user-management.um.multi-tenant.enabled=true
113

-- 113 of 121 --

2. Configure super admin (optional):
mgmtp.a12.uaa.user-management.um.multi-tenant.super-admin-roles=superAdmin
mgmtp.a12.uaa.user-management.um.multi-tenant.super-admin-usernames=system
3. Pick tenant resolution. Header-based is the default; JWT-claim-based is used when the JWT
carries the claim:
mgmtp.a12.uaa.user-management.um.multi-tenant.tenant-header=X-Tenant-Id
mgmtp.a12.uaa.user-management.um.multi-tenant.tenant-claim-name=tenants
4. Set tenant document paths — these tell the filter policies where the tenant id lives in each
document model:
mgmtp.a12.uaa.user-management.um.user-document-properties.user-tenant-
path=/user/tenant
mgmtp.a12.uaa.user-management.um.role-document-properties.role-tenant-
path=/role/tenant
mgmtp.a12.uaa.user-management.um.access-right-document-properties.access-right-
tenant-path=/accessRight/tenant
5. Activate the right authorization profile. um_tenant enables multi-tenancy and applies the default
tenant paths; compose it with um_basic or um_advanced:
spring.profiles.active=um_advanced,um_oauth2,um_tenant
6. Register tenants — choose one of:
Static — declare tenants in application.yml / .properties:
mgmtp.a12.uaa.user-management.tenant-registration[alpha].management.tenant-id=alpha
mgmtp.a12.uaa.user-management.tenant-registration[alpha].synchronization.idp-
url=https://idp.example.com
mgmtp.a12.uaa.user-management.tenant-registration[alpha].synchronization.realm-
name=alpha
mgmtp.a12.uaa.user-management.tenant-registration[alpha].synchronization.client-
name=um-client
mgmtp.a12.uaa.user-management.tenant-
registration[alpha].synchronization.enabled=true
mgmtp.a12.uaa.user-management.tenant-registration[alpha].synchronization.idp-
technical-user.username=idp-tech
mgmtp.a12.uaa.user-management.tenant-registration[alpha].synchronization.idp-
technical-user.password=<secret>
Dynamic — implement UMTenantRegistrationStorage, enable dynamic-realms support, and
114

-- 114 of 121 --

expose a project-owned REST endpoint that calls storeTenant() / removeTenant():
mgmtp.a12.uaa.user-management.um.dynamic-realms-config-support.enabled=true
Remember to fire UMTenantRemoveEvent from the removeTenant implementation.
7. Dynamic CORS (optional): enable and provide an AllowedOriginsStorage bean.
mgmtp.a12.uaa.user-management.um.dynamic-cors-allowed-origins-config-
support.enabled=true
8. Dynamic Keycloak realms (optional): provide an Oauth2TenantStorage bean and set …dynamic-
realms-config-support.enabled=true. DynamicJWTDecoder takes over JWT decoding once enabled.
9. Caching (optional): enable …cached-storage.enabled=true and ensure a CacheManager bean is
present. Cache regions used: umAllowedOriginsConfigCache, umIDPSyncConfigurationCache.
Single-Tenant Deployments
Projects that do not need multi-tenancy keep …multi-tenant.enabled=false and leave the *-tenant-
path properties empty. The repository filter policies are no-ops when the tenant path is empty, so
the legacy single-tenant behaviour is preserved end-to-end. No project code change is required
beyond the other migration sections.
Legacy Comparison
The legacy uaa-user-management-service module had no multi-tenancy mechanism:
• No TenantContext or request-scoped tenant holder.
• No TenantContextInterceptor.
• No UMTenantRegistrationStorage or UMTenantSelector SPIs.
• No tenant extraction from JWT claims.
• The legacy authorization JSON contained tenant filter policies, but they were dormant — there
was no runtime wiring to set the active tenant.
The refactored module activates the filter policies and adds the full request-path machinery.
Projects that were single-tenant in the legacy module can stay single-tenant in the new module by
leaving multi-tenancy disabled; opt-in is a separate migration step.
Step-by-Step Migration Procedure
A chronological playbook for a downstream team. The phases are sequential — do not start a later
phase before the earlier one compiles cleanly.
Phase 1 — Preparation
1. Freeze unrelated UM-related work in your project for the duration of the migration.
115

-- 115 of 121 --

2. Record the legacy surface the project touches. These greps give a starting inventory:
# Authorization scope usage
grep -rn "hasUAAPermission(" src/
# UM property usage
grep -rn "mgmtp.a12.uaa.user-management" src/ application*.properties
application*.yml
# Legacy class imports
grep -rn "com.mgmtp.a12.uaa.um.client.rest" src/
grep -rn "com.mgmtp.a12.uaa.usermanagement.um" src/
grep -rn "com.mgmtp.a12.uaa.usermanagement.idp" src/
# Extension SPI implementations
grep -rln
"IUserDocumentConversionService\|IUserDocumentCustomizationService\|IUserIDPConvers
ionService\|IUserIDPCustomizationService\|IIDPExceptionHandler" src/
# Event listeners
grep -rn
"UserAfterCreateEvent\|UserAfterUpdateEvent\|UserAfterDeleteEvent\|IDPAdminClientSe
rviceRemoveEvent" src/
3. Capture the output — it is the migration punch list.
Phase 2 — Dependency Swap
1. Update Gradle / Maven coordinates to the new artifacts (see Module-Level Migration).
2. Run a build. Expect compile errors everywhere the legacy types were referenced; that is normal
and tells you the punch list is complete.
Phase 3 — Import Updates
1. Bulk-rename the following packages across your Java sources:
Legacy package New package
com.mgmtp.a12.uaa.usermanagement.um.* com.mgmtp.a12.uaa.usermanagement.service.*
(service internals) or
com.mgmtp.a12.uaa.usermanagement.config.*
(properties) — depends on the class
com.mgmtp.a12.uaa.um.client.rest.* com.mgmtp.a12.uaa.usermanagement.rest.client
.* (typed clients) or
com.mgmtp.a12.uaa.usermanagement.rest.*
(REST client properties)
com.mgmtp.a12.uaa.usermanagement.idp.IIDPExc
eptionHandler
com.mgmtp.a12.uaa.usermanagement.exception.I
IDPExceptionHandler
116

-- 116 of 121 --

2. Replace domain-POJO imports:
Legacy New
com.mgmtp.a12.uaa.usermanagement.User com.mgmtp.a12.uaa.usermanagement.representat
ions.UMUserRepresentation
com.mgmtp.a12.uaa.usermanagement.Role com.mgmtp.a12.uaa.usermanagement.representat
ions.UMRoleRepresentation
com.mgmtp.a12.uaa.usermanagement.AccessRight com.mgmtp.a12.uaa.usermanagement.representat
ions.UMAccessRightRepresentation
com.mgmtp.a12.uaa.usermanagement.ExtendedUse
r
Custom subclass of UMUserRepresentation +
UserExtensionConverter<T> bean
Phase 4 — Representation Adoption
1. Replace new User(…) / setter chains with UMUserRepresentation.builder()….build().
2. If the project had an ExtendedUser subclass with custom fields:
a. Create a new subclass of UMUserRepresentation holding those fields.
b. Implement UserExtensionConverter<YourUserType>; register it as a Spring bean.
c. Register a Supplier<YourUserType> bean alongside the converter. This is a hard requirement
— without it, the converter receives UMUserRepresentation instances and throws
ClassCastException on the first read. See the IMPORTANT callout in the classes section
(refactoring-supplier-requirement anchor).
d. Remove the legacy ExtendedUser.
3. Repeat for Role → UMRoleRepresentation and AccessRight → UMAccessRightRepresentation (these
are usually unextended).
Phase 5 — Extension SPI Migration
Legacy SPI (if implemented) Migrate to
IUserDocumentConversionService<U> UserExtensionConverter<T>
IUserDocumentCustomizationService<U> UMUserDocumentEventCustomizer (pre-operation
hooks). Move customizeAfterXxx logic to
@EventListener methods on the UMUser*Event
records.
IUserIDPConversionService<U, I> IDPUserConverter<T> (for core conversion) and /
or IDPUserExtensionConverter<T> (for post-
conversion customisation)
IUserIDPCustomizationService<U, I> IDPUserExtensionConverter<T>
IIDPExceptionHandler (legacy package) IIDPExceptionHandler (same interface, package
com.mgmtp.a12.uaa.usermanagement.exception) —
only the import changes
Register each implementation as a Spring bean; UM picks them up via Spring DI.
117

-- 117 of 121 --

Phase 6 — Event Listener Updates
1. Change listener method signatures to accept the new record-typed events:
Legacy event type New event type
UserAfterCreateEvent<U> UMUserAfterCreateEvent
UserAfterUpdateEvent<U> UMUserAfterUpdateEvent
UserAfterDeleteEvent<U> UMUserAfterDeleteEvent
IDPAdminClientServiceRemoveEvent UMTenantRemoveEvent
2. Rename payload field accesses:
Legacy getter New accessor
event.getCreatedDocument() event.latestDocument()
event.getUpdatedDocument() event.latestDocument()
event.getDeletedDocument() event.latestDocument()
event.getUser() (removed — derive from
event.latestDocument() or
event.userDataServicesDocument())
3. If the project fires events from custom UMTenantRegistrationStorage.removeTenant(), publish
UMTenantRemoveEvent from that implementation.
Phase 7 — Configuration Migration
1. Apply the property rename table (see Configuration Properties Migration).
2. Add any needed profile activations (um_basic or um_advanced at minimum; plus um_oauth2 for
OAuth2; plus um_tenant for multi-tenancy).
3. Spring Boot logs Unknown property warnings for stale keys — use them as a final check.
Phase 8 — Authorization JSON Migration
1. If the project registers its own authorization JSONs via mgmtp.a12.uaa.authorization.child-
authorization-definitions, update the classpath references to:
mgmtp.a12.uaa.authorization.child-authorization-definitions=\
classpath:um/basic/authorization/user-management-authorization.json,\
classpath:um/advanced/authorization/user-management-authorization.json
2. Apply the scope rename table (see Authorization Migration) across every @PreAuthorize in
project code.
3. Remove any references to deleted endpoint-specific policies (User Reading Endpoint Policy, User
Document Reading Policy, User Pojo Reading Policy, the legacy empty policy).
118

-- 118 of 121 --

Phase 9 — REST Endpoint Consumer Updates
If the project has external clients that call UM REST endpoints directly (not via the REST client),
update them:
1. GET /user/read/{username} → GET /user?id={id} (or ?docRef={docRef})
2. POST /user/create → POST /user
3. DELETE /user/delete/{username} → DELETE /user?docRef={docRef}
4. GET /user/query?path=&value= → POST /user/simple-search with SearchOptions body
5. POST /role/delete → DELETE /role?docRef={docRef}
6. Remove any calls to /user-management/* endpoints (all removed).
Phase 10 — Multi-Tenant Decision
1. If the project stays single-tenant: no action. Leave …multi-tenant.enabled=false.
2. If the project opts into multi-tenancy: follow the configuration checklist in Multi-Tenancy (New
Concept). Implement UMTenantRegistrationStorage only if you need runtime tenant registration
— otherwise use the static tenant-registration[*] properties.
Phase 11 — Build and Test
1. ./gradlew build must pass cleanly.
2. Run the project’s integration test suite against a local instance of the new UM service (uaa-
usermanagement-service-example is a ready-to-run reference).
3. Smoke-check every controller route the project exercises; verify authorization scopes pass end-
to-end.
Phase 12 — Deployment
1. Deploy the upgraded service side-by-side with the legacy one if infrastructure permits
(blue/green).
2. Run the smoke test suite against the new deployment before cutting traffic.
3. Monitor error rates and authorization-denied metrics after cutover — scope rename mistakes
tend to surface as HTTP 403 spikes.
Validation Checklist
Use this to confirm the migration is done before declaring release-ready.
☐ No references to the legacy packages remain (com.mgmtp.a12.uaa.usermanagement.um.*,
com.mgmtp.a12.uaa.um.client.rest.*).
☐ All imports resolved without legacy domain POJOs (User, Role, AccessRight, ExtendedUser).
☐ Every hasUAAPermission('…') scope string matches the new scope names.
☐ application.yml / .properties has no legacy-only property keys (idp-registration[*], core-
model-name, top-level organization-unit-role-structure).
119

-- 119 of 121 --

☐ All extension SPI beans use the new interface names.
☐ Every project UserExtensionConverter<T> has a matching Supplier<T> bean registered in the
same Spring context.
☐ Event listeners compile against UMUser*Event record types; all payload field accesses use
latestDocument().
☐ Service module is brought up on the right profile combination (um_basic or um_advanced, plus
um_oauth2 / um_tenant as needed).
☐ Authorization JSONs — project-owned or UM-provided — are reachable from the classpath and
load without parse errors at startup.
☐ The project’s integration tests pass against the new UM service.
☐ (Multi-tenant only) X-Tenant-Id header routing verified for every tenanted endpoint; super-
admin bypass verified for super-admin roles.
Reference: Example Project
uaa-usermanagement/devapps/uaa-usermanagement-service-example/ is a ready-to-run Spring Boot
application using the new module. It demonstrates:
• Project-level UserExtensionConverter and IDPUserExtensionConverter implementations.
• Custom UMUserDocumentEventCustomizer.
• Multi-tenant configuration via the project_basic_tenant and project_advanced_tenant profiles.
• Dynamic configuration via the project_dynamic_configure profile.
Treat this example as the canonical reference when the documentation leaves a question
unanswered.
11.1.2. Legacy uaa-user-management (in case it is still available in 2026
release)
User model
The legacy module by default supported a split user model where DomainUserManagement.json
declared the extension as an include placeholder:
"modelReferences": [
{
"alias": "DomainUserExtensionExample",
"modelType": "document",
"purpose": "include",
"reference": "DomainUserExtensionExample"
}
],
...
"Group": {
"repeatability": 1,
"modelAlias": "DomainUserExtensionExample"
120

-- 120 of 121 --

}
Change
Inline the extension fields into DomainUserManagement.json and remove the include declaration. The
user model becomes self-contained:
"Group": {
"repeatability": 1,
"elements": [
{ "type": "Field", "id": "field_9ad1f", "name": "department", "Field": { ... } },
{ "type": "Field", "id": "field_300ad", "name": "job_title", "Field": { ... } },
{ "type": "Field", "id": "field_e2760", "name": "branchName", "Field": { ... } },
{ "type": "Field", "id": "field_0bee7", "name": "companyName", "Field": { ... } },
{ "type": "Field", "id": "field_0be37", "name": "rights", "Field": { ... } },
{ "type": "Field", "id": "field_64278", "name": "lastLogin", "Field": { ... } }
]
}
In the model header, drop the corresponding modelReferences entry that pointed at
DomainUserExtensionExample.
After the model merging, the property is no longer needed: mgmtp.a12.uaa.user-
management.um.extension-model-name=DomainUserExtensionExample
121

-- 121 of 121 --

