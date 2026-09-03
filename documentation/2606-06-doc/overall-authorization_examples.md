# overall authorization_examples

Authorization Definition Examples
RBAC
Figure 1. Visualization of the authorization flow for the ADD_DOCUMENT method with the RBAC.
The A12 Data Services method ADD_DOCUMENT requires the scopes "Document Create" and "Model
Read". The following two examples explain how they are defined in the
authorizationDefintion.json.
A complete overview about all scopes defined by A12 components can be found at authorization
scopes and access rights of A12 components.
Scope "Document Create":
This example will explain all steps of the authorization flow based on the authorization definition
for RBAC. As in the image above, the ADD_DOCUMENT method is used as example, which checks the
Document Create scope and the Model Read scope. At first, we focus at the Document Create scope.
The Model Read scope validation is explained in the next example, because it is slightly more
complex.
The authorizationDefinition.json contains next to a name and a description the nodes policies and
permissions.
{
"name": "Example Authorization Definition",
"description": "Just an example",
"policies": [...],
"permissions": [...]
}
The permissions node contains a list of permissions. Each with a name and a description, and
technically more important the nodes scopes and policy-refs. The permission with the name
Document Create Permission contains the Document Create scope. Therefore, UAA will validate this
permission for the ADD_DOCUMENT method. The policy-refs node contains a reference to the policies,
which needs to be validated. Here it is only one policy, the User Has DOCUMENT_CREATE Right.
1

-- 1 of 5 --

"permissions": [
...
{
"name": "Document Create Permission",
"description": "Check that the user has access right to create documents.
com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2 is referenced as
#resource.",
"policy-refs": [
"User Has DOCUMENT_CREATE Right"
],
"scopes": [
"Document Create"
]
},
...
]
This policy is found in the policies node of the authorizationDefinition.json and identified by its
name. To grant access to the requested scope, the rules listed here, must evaluate to true. The rules
contain a Spring Expression Language (SpEL) expression. In this example, only one rule gets
evaluated: hasAccessRight('DOCUMENT_CREATE'). This checks if the requesting user, has the access
right DOCUMENT_CREATE in any of their roles.
"policies": [
...
{
"name": "User Has DOCUMENT_CREATE Right",
"description": "Check that current user has access right to create
documents.",
"rules": [
"hasAccessRight('DOCUMENT_CREATE')"
]
},
...
]
At this point, there was no authorization check based on the roles assigned to the models. This will
be done with the Model Read scope, which is explained in the following.
Scope "Model Read":
This example will guide thought the Model Read scope, which is requested for various methods of
A12 Data Services.
In the authorizationDefinition.json of Data Services the permission with the name Model read
permission contains the Model Read scope. The policy-refs node contains a reference to the User
Has MODEL_READ Right policy.
2

-- 2 of 5 --

"permissions": [
...
{
"name": "Model read permission",
"description": "Check that the user has access right to read provided model.
com.mgmtp.a12.model.header.Header is referenced as #resource.",
"policy-refs": [
"User Has MODEL_READ Right"
],
"scopes": [
"Model Read"
]
},
...
]
This policy contains three rules, linked by a logical OR. So one them must evaluate to true. This
policy also contains the optional dataPreload node, which contains an array of expressions used to
prepare data which can be referenced in the rules. Here #matchedRequiredRoles is defined and loads
the roles of the user matching the requested model.
"policies": [
...
{
"name": "User Has MODEL_READ Right",
"description": "Check that user has access right to read models and matching
role with provided model.",
"dataPreload": ["#matchedRequiredRoles =
T(com.mgmtp.a12.dataservices.utils.ModelUtils).getMatchingRoles(#resource)"],
"rules": [
"@dataServicesCoreProperties.getAuthorization().getRoleBased().isEnabled()
== false ||
hasAccessRight('MODEL_MANAGE') ||
hasNestedObjectWithPropertyValue(#matchedRequiredRoles, 'accessRights',
'name', 'MODEL_READ')"
]
},
...
]
The first rule checks whether the role base authorization is disabled. Some projects want to disable
role based authorization when they implement their own authorization checks.
The second rule checks whether the requesting user has the access right MODEL_MANAGE. This access
right is intendet for roles in modeleling environments and not for production environments. As
defined in this policy, roles with the MODEL_MANAGE assess right do not need to be attributed to the
model to gain access.
3

-- 3 of 5 --

In the third rule it is validated whether the preloaded matching roles #matchedRequiredRoles
contains the MODEL_READ access right. This enforces, that only roles that are attributed to the model
are checked for its access rights and not all roles a user is potentially assigned to.
If one of these rules evaluates to true, the access to scope Model Read is granted.
ABAC
Here we assume, that the default authorizationDefinition.json from Data Services remains, and we
add additional controls via a child authorization definitions file included via the property
mgmtp.a12.uaa.authorization.childAuthorizationDefinitions.
We now take the example of querying documents. Here authorization is granted via the Model Read
scope and since the introduction of the Query API, the Query scope, which checks for the access
right QUERY. We now extend to Query scope in the file childAuthorizationDefinition.json (as done in
the A12 Project Template) and add additional attribute based authorization rules.
At first, it requires a permission with the correct scope assigned:
"permissions": [
...
{
"name": "User List Document Permission",
"repository-refs": [
"Document List By User Created"
],
"scopes": [
"Query"
]
},
...
]
This permission references to the repository policy "Document List By User Created", which is
defined as follows:
"repositoryPolicies": [
...
{
"name": "Document List By User Created",
"description": "User is only able to see the documents created by them",
"target": "!containsAnyRole('admin')",
"templates": [
{
"operator": "exact_match",
"field": "/__meta/creator",
"value": "principal.username"
}
4

-- 4 of 5 --

]
},
...
]
This repository policy contains a target and a templates field. If the target expression (SpEL)
evaluates to false, the policy is not further evaluated and considered as passed. If it evaluates to
true, the content of the templates field is added into the query as additional constraint AND
connected to the existing constraints.
In this example, the repository policy adds a constraint to the query, to only returns documents
where the creator of the document equals the requesting user. For users of the admin role, this
constraint is not added and eventually all documents are returned.
NOTE
The permissions and policies of the child authorization definitions add up to the
existing authorization definitions of Data Services. This ensures that authorization
rules are not weakened. This behavior changes when adding the key-value pair
"call-parent-scope": false to the permissions of the child authorization definitions.
Use with caution!
Further examples of template expression can be found at the UAA Repository Authorization
chapter.
5

-- 5 of 5 --

