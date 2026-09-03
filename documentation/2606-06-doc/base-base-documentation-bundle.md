# base base documentation bundle

Base
Introduction
This is the documentation for A12 Base.
Base contains basic A12 libraries with APIs and low level functionality. These libraries are
described in the following sections.
Model API
Getting Started
The model API library is provided in TypeScript and Java. In TypeScript you can use the library like
any other npm package and install it as follows:
npm install --save-dev @com.mgmtp.a12.base/base-model-api
The Java library can for example be included in your Maven dependencies like this:
<dependency>
<groupId>com.mgmtp.a12.base</groupId>
<artifactId>base-model-api</artifactId>
<version>BASE_VERSION</version>
</dependency>
General
This library contains basic model interfaces, that are used for all A12 models, independently of the
model type.
Every A12 model consists of a header object, that contains general information like the type, name
or version of the model, and a content object.
The structure of the content fully depends on the model type and is therefore not defined in this
package.
Please refer to the API documentation for further information about the provided model interfaces.
Model Consistency
1

-- 1 of 13 --

Getting Started
The model consistency library can be added to your Maven dependencies as follows:
<dependency>
<groupId>com.mgmtp.a12.base</groupId>
<artifactId>base-model-consistency</artifactId>
<version>BASE_VERSION</version>
</dependency>
General
The library provides interfaces and general functionality to perform consistency checks on A12
models.
The following sections will briefly cover the most important interfaces and classes of the library.
Please refer to the API documentation for further information about the provided interfaces.
Model Resolver
A ModelResolver can be used to resolve models based on their name. To write your own
ModelResolver you need implement a single method getModel:
Optional<Model> getModel(String modelName);
The method takes the model name and returns the model for the given name or empty if the model
could not be resolved. Note, that all models must be identifiable by their name.
Consistency Categories
Consistency categories indicate which types of problems exist in your model. The following
example shows how you can use the ConsistencyCategory interface to implement an enumeration of
error codes.
public enum TestCategory implements ConsistencyCategory {
SOME_ERROR_1("some.error.1"),
SOME_ERROR_2("some.error.2");
private String keyValue;
TestCategory(final String keyValue) {
this.keyValue = keyValue;
}
@Override
2

-- 2 of 13 --

public String getKeyValue() {
return keyValue;
}
@Override
public String getLocalizedMessage(final Locale locale, final String keyValue) {
final String errorMessage = // get localized error message
return errorMessage;
}
}
These error codes can then be used to create consistency problems by using the ConsistencyProblem
class. To create a problem you need to provide the model name, a consistency category, and an
object describing the source of the problem.
Additionally, you can also provide a severity for the problem (Severity.INFO, Severity.WARNING or
Severity.ERROR) as well as additional parameters. The additional parameters could contain
information about the problematic model element to use it in an error message later.
Problem problem = new Problem(
modelName,
TestCategory.SOME_ERROR_1,
"problem source string",
Severity.ERROR,
// additional parameters here
);
Consistency Validator
The abstract ConsistencyValidator class provides a base for performing consistency checks on A12
models. Implementations of this class must provide two methods:
• List<Problem> validate(final Model model): Perform a consistency check on a single model
• List<Problem> validateSet(final Collection<Model> models): Perform a consistency check on a
set of models
When implementing a ConsistencyValidator, the constructor must accept a ModelResolver as an
argument.
Breaking Change Management
All base libraries follow the general A12 breaking change interpretation.
Migration Instructions
3

-- 3 of 13 --

2026.06
Breaking Changes
Codemod recipes exist for some of the breaking changes. To use them, first install or update the
codemod with your preferred package manager, e.g. with pnpm:
pnpm install -g @com.mgmtp.a12.base/base-codemod
Then run the specific recipe:
base-codemod <recipe> <path to directory with tsconfig.json>
// or as a single command
pnpx @com.mgmtp.a12.base/base-codemod <recipe> <path to directory with tsconfig.json>
Removal of Migration packages
The npm package @com.mgmtp.a12.base/base-model-migration-api and the Java artifact base-model-
migration-api (com.mgmtp.a12.model.migration) have been removed. These packages only contained
the interfaces MigrationTool and MigrationResult, which were solely consumed by the A12
Migration Tool itself. Since they served no purpose outside of that context, they have been removed
from A12 Base.
Required
Depending on whether you used the migration API packages in your project.
Migration
Manual, no codemod available.
Result
npm: If you depend on @com.mgmtp.a12.base/base-model-migration-api, replace it with the
@com.mgmtp.a12.migrationtool/migrationtool-core package, which now provides the
MigrationTool and MigrationResult types directly.
Java: The Java artifact base-model-migration-api has been removed without replacement. If you
do depend on com.mgmtp.a12.model.migration.MigrationTool or
com.mgmtp.a12.model.migration.MigrationResult, you will need to copy the interface and the class.
Simplification of Consistency API
The consistency API has been significantly simplified by removing the rule-based architecture and
converting ConsistencyValidator to an abstract base class. This was done to provide implementers
of this API with more freedom when writing a consistency checker.
Removed Classes and Interfaces:
4

-- 4 of 13 --

• com.mgmtp.a12.model.consistency.ConsistencyStatus - Use List<Problem> instead
• com.mgmtp.a12.model.consistency.rules.ModelConsistencyRule
• com.mgmtp.a12.model.consistency.rules.ModelConsistencyRulesProvider
• com.mgmtp.a12.model.consistency.rules.RuleExecutor
• com.mgmtp.a12.model.consistency.rules.ModelConsistencyCategory
• com.mgmtp.a12.model.consistency.rules.FatalRuleProblemException
• com.mgmtp.a12.model.data.document.consistency.AbstractRuleWithModelResolver
• com.mgmtp.a12.model.data.document.consistency.CheckReferencedModelRule
• com.mgmtp.a12.model.data.document.consistency.DocumentModelCategory
• com.mgmtp.a12.model.consistency.impl.InvalidModelResolverException
Changed Classes:
ConsistencyValidator is now an abstract base class with a generic type parameter instead of a
concrete implementation:
// Old usage
ModelResolver modelResolver = new MyModelResolver();
ConsistencyValidator validator = new ConsistencyValidator(modelResolver);
// validate your loaded model
ConsistencyStatus status = validator.validate(model);
if (status.isValid()) {
// handle success
}
List<Problem> problems = status.problems();
// New usage - extend ConsistencyValidator with type parameter
public class MyConsistencyValidator extends ConsistencyValidator<MyModel> {
public MyConsistencyValidator(ModelResolver modelResolver) {
super(modelResolver);
}
@Override
public List<Problem> validate(MyModel model) {
// Implement your validation logic here
return new ArrayList<>();
}
@Override
public List<Problem> validateSet(Collection<MyModel> models) {
// Implement your validation logic here
return new ArrayList<>();
}
}
5

-- 5 of 13 --

// Then use it
ModelResolver modelResolver = new MyModelResolver();
ConsistencyValidator validator = new MyConsistencyValidator(modelResolver);
// validate your loaded model
List<Problem> problems = validator.validate(model);
if (problems.isEmpty()) {
// handle success
}
Required
Depending on whether you used the consistency API in your project and how you used it.
Migration
Manual, no codemod available.
Result
The old API used ServiceLoader to automatically discover all implementations of
ModelConsistencyRulesProvider and execute their rules. This mechanism has been removed. Your
migration path depends on how you used the API:
A) If you only used the API to combine multiple rule providers (without custom rules):
Instead of relying on the old ConsistencyValidator to automatically discover and execute all rule
providers, you now need to call the specific validators directly. Each component that previously
provided rules via ModelConsistencyRulesProvider should now offer its own ConsistencyValidator
implementation. Check the documentation of the respective components to see if a validator exists.
Example:
// Old: automatic discovery via ServiceLoader
ConsistencyValidator validator = new ConsistencyValidator(modelResolver);
ConsistencyStatus status = validator.validate(model); // executes all discovered rules
// New: call specific validators
MyModelConsistencyValidator docValidator = new
MyModelConsistencyValidator(modelResolver);
List<Problem> docProblems = docValidator.validate(myModel);
// Combine problems from multiple validators if needed
List<Problem> allProblems = new ArrayList<>();
allProblems.addAll(docProblems);
// add problems from other validators...
B) If you used custom rules (with or without combining rule providers):
You need to create your own ConsistencyValidator implementation and move your custom rule
6

-- 6 of 13 --

logic into the validate() and validateSet() methods. If you were combining your custom rules with
other rule providers, you can call the specific validators from within your implementation (see A).
Example:
// Old: adding custom rules
ConsistencyValidator validator = new ConsistencyValidator(modelResolver);
validator.addCustomRule(myCustomRule);
ConsistencyStatus status = validator.validate(model);
// New: implement your own validator with type parameter
public class MyConsistencyValidator extends ConsistencyValidator<MyModel> {
public MyConsistencyValidator(ModelResolver modelResolver) {
super(modelResolver);
}
@Override
public List<Problem> validate(MyModel model) {
List<Problem> problems = new ArrayList<>();
// Add your custom validation logic here
// (previously in ModelConsistencyRule implementations)
// Optionally call other validators if needed
// DocumentConsistencyValidator docValidator = new
DocumentConsistencyValidator(modelResolver);
// problems.addAll(docValidator.validate(model));
return problems;
}
@Override
public List<Problem> validateSet(Collection<MyModel> models) {
// Implement validation for model sets
return new ArrayList<>();
}
}
General API Changes:
• Replace ConsistencyStatus return values with List<Problem>
• Replace calls to status.isValid() with problems.isEmpty()
• Replace calls to status.problems() with the returned list directly
• Remove any implementations of ModelConsistencyRulesProvider (provide ConsistencyValidator
implementations instead)
• Remove any implementations of ModelConsistencyRule (move validation logic into
ConsistencyValidator.validate() and validateSet() methods)
7

-- 7 of 13 --

• Remove usages of ModelConsistencyCategory (the ConsistencyCategory interface can be used to
implement your custom categories if needed)
Removal of base-parent and base-bundle
After the deprecation in the previous release, the Java artifacts base-parent and base-bundle were
removed now. See the deprecation notice for details.
Please use the new base-bom artifact as Java platform / bom to manage the versions of used base
libraries and configure the specific base libraries you need as direct dependencies.
If you relied on the base-parent pom managing the version of apache.commons.io, you need to
manage this dependency yourself now, since it is not used by any base library and the new base-
bom does not manage it.
Required
Depending on whether you used base-parent and/or base-bundle in your project.
Migration
Manual, no codemod available.
Result
Depending on your usage & dependency declaration setup.
Changed Dependency Scopes
The scopes of some dependencies of the base libraries were changed to better reflect their actual
usage:
base library dependency scope change
base-fs-model-repository base-model-marshalling from compile to runtime
apache.commons.lang3 from compile to test
base-model-api apache.commons.lang3 from compile to runtime
base-model-consistency apache.commons.lang3 from compile to runtime
base-model-marshalling apache.commons.lang3 was not used, dependency is
removed
If you use one of these base libraries, you may have relied on the fact, that the listed dependencies
were made available via base at compile-time. Maybe you use them without declaring them as your
own dependencies. Now, you need to add them directly to your dependency configuration.
Required
Depending on whether you relied on using base dependencies without declaring them directly.
Migration
Manual, no codemod available.
8

-- 8 of 13 --

Result
Depending on your usage & dependency declaration setup.
Drop Java 17 Support
The Base Java libraries are now compiled with and for Java 21. Support for Java 25 is also
guaranteed.
Required
As required by Java 21 Migration Guide.
Migration
Manual, no codemod available.
Result
See Java 21 Migration Guide.
Updating to ES2025
The javascript output of the npm artifacts was updated from ES2024 to ES2025 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
Required
Depending on whether support for older browsers is required.
Migration
Manual, no codemod available.
Result
Depending on your bundler setup, consult its documentation on how to include polyfills.
Removal of Deep Import Paths
Deep import paths from @com.mgmtp.a12.base/model-api-ts (e.g., @com.mgmtp.a12.base/base-model-
api/lib/main/model/index.js) have been removed. All exports must now be imported directly from
the package root. For detailed migration instructions, see the deprecation note.
NOTE
The package now uses the exports field instead of main/types. This should be
transparent for most consumers but may require configuration updates for older
bundlers.
Required
Yes.
Migration
base-codemod preferTopLevel <path to directory with tsconfig.json>
9

-- 9 of 13 --

Result
// before
import { Model } from "@com.mgmtp.a12.base/base-model-api/lib/main/model/index.js"
// after
import { Model } from "@com.mgmtp.a12.base/base-model-api";
Conversion of ModelPath namespace into object
The ModelPath namespace was changed to a const object. This was done to improve treeshaking and
to unblock users from using the erasableSyntaxOnly flag.
Required
No
Migration
Not needed
Result
No changes
Migration to Jackson 3
We migrated to Jackson 3. Please refer to the official Migration Guide for details on required
changes to your code.
To achieve cleaner separation between ObjectMapper configurations, how we handle (de-
)serialization of model headers is now an implementation detail of A12 Base. As a result, the
method JacksonConfiguration#configureMixins does not register mixins for the Header class
anymore. It should only be used if you use Locale, Annotation, Label or ModelReference outside of the
model header.
As a replacement, we now provide a JacksonModule that can be integrated into your ObjectMapper.
import static com.mgmtp.a12.model.header.HeaderModule.HEADER_MODULE;
import tools.jackson.databind.json.JsonMapper;
ObjectMapper mapper = JsonMapper.builder()
.addModule(HEADER_MODULE)
.build();
Required
As required by the official Jackson 3 Migration Guide.
Migration
Manual, no codemod available.
10

-- 10 of 13 --

Result
See official Jackson 3 Migration Guide.
2025.06-ext4
Deprecation
Deep import paths of A12 Base npm packages
Nested imports from the npm packages @com.mgmtp.a12.base/base-model-api and
@com.mgmtp.a12.base/base-model-migration-api (e.g. @com.mgmtp.a12.base/base-model-
api/lib/main/model/index.js) are now deprecated in favor of top-level imports to avoid
unnecessary breaking changes and reduce ongoing maintenance effort.
The ability to use the old-style ("long") imports will be removed in the next breaking release.
To migrate, first install or update the codemod with your preferred package manager, e.g. with
pnpm:
pnpm install -g @com.mgmtp.a12.base/base-codemod
Then run the specific recipe:
base-codemod preferTopLevel <path to directory with tsconfig.json>
// or as a single command
pnpx @com.mgmtp.a12.base/base-codemod preferTopLevel <path to directory with
tsconfig.json>
For further details, run the codemod with the --help flag.
Deprecation of base-parent
The base-parent Java artifact has been deprecated. Please use the new artifact base-bom as Gradle
Java platform / Maven bom, when you want to manage the versions of used base libraries.
In the new base-bom, the versions for apache.commons.lang3 and apache.commons.io are no longer
managed compared to base-parent.
This was done because apache.commons.lang3 is actually only a runtime dependency of the base
libraries, that is currently provided with compile-time scope only by accident. The
apache.commons.io library is not used by any base library at all anymore.
If you use these apache libraries in your project, please add them as direct dependencies with the
versions you need.
11

-- 11 of 13 --

Deprecation of base-bundle
The base-bundle Java artifact has been deprecated without replacement.
If you used it as Java-platform / bom in your build setup, please also use the new base-bom now.
If you have used it to get access to the bundled base libraries, now please configure the specific
base libraries you need as direct dependency.
Future Scope Changes of Base Library Dependencies
As a heads-up, in a future breaking release we plan to correct the scope of the following
dependencies of the base libraries to better reflect their actual usage:
base library dependency scope change
base-fs-model-repository base-model-marshalling compile-time → runtime
apache.commons.lang3 compile-time → test-time
base-model-api apache.commons.lang3 compile-time → runtime
base-model-consistency apache.commons.lang3 compile-time → runtime
base-model-marshalling apache.commons.lang3 compile-time → dependency
will be removed, since it isn’t
used here.
If you use one of these base libraries, you might rely on the fact, that the listed dependencies are
currently made available via base at compile-time. Maybe you use them without declaring them as
your own dependencies. Please consider adding them directly to your dependency configuration
now.
2025.06
Breaking Changes
Migration to ESM
The npm artifacts @com.mgmtp.a12.base/base-model-api and @com.mgmtp.a12.base/base-model-
migration-api were migrated from CommonJS to ESM. When using Node 22.12+ and modern build
tools, there should be no changes necessary to your bundler setup.
Migrating your own application to ESM is not required, but recommended. Consult the
documentation of your bundler for specifics.
Updating to ES2024
The JavaScript output of the npm artifacts was updated from ES2020 to ES2024 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
12

-- 12 of 13 --

Drop Java 17 Support
The Base Java libraries are now compiled with and for Java 21.
Removed Severity
The deprecated class com.mgmtp.a12.model.consistency.Severity has been removed now and can be
replaced with com.mgmtp.a12.model.notification.Severity.
Problem Code Restructuring
The class com.mgmtp.a12.model.consistency.Problem was renamed to
com.mgmtp.a12.model.consistency.ConsistencyProblem.
This was done to allow for a new interface com.mgmtp.a12.model.consistency.Problem, which has the
ConsistencyProblem class as its default implementation.
This interface, resembling the functionality of the former Problem class, now extends the interface
com.mgmtp.a12.model.notification.RankedNotification.
With this, classes implementing the new com.mgmtp.a12.model.consistency.Problem interface now
have the new property source of type Object, which allows to describe the source of the Problem.
Also, for the severity property, the type com.mgmtp.a12.model.notification.Severity is used now.
The constructors of ConsistencyProblem(former class Problem) have been adapted and now require
the source as third parameter.
If you referred to Problem as type in your code, nothing should change.
If you however extended the former class Problem or created new Problem instances, you now could
use ConsistencyProblem and provide the problem source as third constructor parameter.
There is no code mod provided, since we assume there is no wide usage of this code.
ConsistencyStatus Converted to Record
The class com.mgmtp.a12.model.consistency.ConsistencyStatus was converted to a Java Record.
With this, the getter for the problems property is changed from getProblems() to just problems().
There is no code mod provided, since we assume internal usage only.
13

-- 13 of 13 --

