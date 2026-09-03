# utils_logging_collections utils logging documentation

Logging
The package @com.mgmtp.a12.utils/utils-logging contains a logging framework
dependency to be used with TypeScript >= 6.
How to Log
To capture log messages just import LoggerFactory to obtain a logger.
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";
In your code obtain a logger and start logging:
const myLogger = LoggerFactory.getLogger("my-namespace");
myLogger.log("I", "can", "log", "something");
NOTE
Logger is obtained for a certain namespace and instance is created the first time
when a logger is obtained. Any subsequent call with the same namespace will
return the same logger instance.
Log Levels
"trace" Most detailed severity. Messages logged at that
level should provide context when investigating
log records with a severity of warn or error.
"log" Messages at that level should provide diagnosis
information helpful to technical staff
"info" Messages at that level should provide
information about regular events happening in
the application and/or library. For error
analysis, those can be taken into account to see
whether normal operations took place or not in
regard to the error circumstance.
"warn" Messages at that level should make aware that
some irregularity is encountered. From an
application level point of view an example
would be the be loss of network access and thus
buffering of data locally until the connection is
usable again.
1

-- 1 of 8 --

"error" Messages at that level should signal that a
certain operation cannot proceed and no
failover is possible.
How to Process Data
By default the library employs the lib/strategy/DiscardStrategy.d.ts which just discards any log.
Examples
Minimal
// Assuming your namespaces are separated by DOT
import { Settings, ConsoleLoggingStrategy } from "@com.mgmtp.a12.utils/utils-logging";
Settings.LogStrategy = new ConsoleLoggingStrategy(console, undefined, {
filterNamespace: (nameSpace: string) => nameSpace.indexOf(".internal.") === -1;
});
// Done!
Advanced
import { Settings, ConsoleLoggingStrategy } from "@com.mgmtp.a12.utils/utils-logging";
import type { LogLevel } from "@com.mgmtp.a12.utils/utils-logging";
const generalLogger = new ConsoleLoggingStrategy(console, undefined, {
filterNamespace: (nameSpace: string) => nameSpace.indexOf(".internal.") === -1;
});
Settings.LogStrategy = {
digest(namespace: string, level: LogLevel, date: Date, ...messages: unknown[]):
void {
// Implement any desired behavior here
switch(namespace)
{
// ....
case "my.namespace":
// Do something special, send to backend, etc.
break;
// ....
default: generalLogger.digest(namespace, level, date, ...messages);
break;
}
}
}
// Done!
2

-- 2 of 8 --

Namespace Specific Configuration
import { Settings, ConsoleLoggingStrategy, MultiLoggingStrategy } from
"@com.mgmtp.a12.utils/utils-logging";
import type { LogLevel, LoggingStrategy } from "@com.mgmtp.a12.utils/utils-logging";
// Config could be stored anywhere
const config = {
"my.cool.namespace": "info",
"my.other.cool.namespace": "trace"
};
const strategies: LoggingStrategy[] = Object.keys(config).reduce(
(a, configKey) => {
// For each custom logLevel namespace, add a console
// logger which has a local logLevel
a.push(new ConsoleLoggingStrategy(console, config[configKey], {
filterNamespace: ns => ns !== configKey
}));
return a;
},
[]
);
Settings.LogStrategy = new MultiLoggingStrategy(
...strategies,
// Add default strategy and filter out all specific namespaces
new ConsoleLoggingStrategy(console, undefined, {
filterNamespace: ns => config.hasOwnProperty(ns)
})
);
// Done!
Determine Whether the Current Settings Are in the Default State
import { Settings, ConsoleLoggingStrategy } from "@com.mgmtp.a12.utils/utils-logging"
const isDefaultStrategy = Settings.DefaultStrategy === Settings.LogStrategy;
const isDefaultLogLevel = Settings.DefaultLogLevel === Settings.LogLevel;
// Done!
Determine if a Specific logLevel Is Currently Active
import { Settings } from "@com.mgmtp.a12.utils/utils-logging";
const isInfoActive = Settings.isActive("info");
// Done!
Out of the box this library is shipping with four distinct logger factories:
3

-- 3 of 8 --

• ConsoleLoggingStrategy which delegates to console if a log message meets or exceeds a certain
local severity or - by default - the globally configured logLevel
• BaseConsoleLoggingStrategy which delegates to console anyways
• MinLogLevelLoggingStrategy which delegates to another logging strategy if a log message meets
or exceeds a certain local severity or - by default - the global configured logLevel
• MultiLoggingStrategy which delegates to multiple instances of logging strategy
With those implementations you can start right away with console. As a more advanced use case
you might want to consider to defer processing of log data to a remote location. For this you can
simply write a logging strategy which takes care of serializing data and handling remote
communication.
Breaking Change Management
The utils-logging package follows the general A12 breaking change interpretation.
Migration Instructions
2026.06
Breaking Changes
Codemod recipes exist for some of the breaking changes. To use them, first install or update the
codemod with your preferred package manager, e.g. with pnpm:
pnpm install -g @com.mgmtp.a12.utils/utils-logging-codemod
Then run the specific recipe:
utils-logging-codemod <recipe> <path to directory with tsconfig.json>
// or as a single command
pnpx @com.mgmtp.a12.utils/utils-logging-codemod <recipe> <path to directory with
tsconfig.json>
Updating to ES2025
The javascript output of the npm artifacts was updated from ES2024 to ES2025 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
Required
Depending on whether support for older browsers is required.
4

-- 4 of 8 --

Migration
Manual, no codemod available.
Result
Depending on your bundler setup, consult its documentation on how to include polyfills.
Removal of Deep Import Paths
Deep import paths from @com.mgmtp.a12.utils/utils-logging (e.g. @com.mgmtp.a12.utils/utils-
logging/api) have been removed. All exports must now be imported directly from the package root.
For detailed migration instructions, see the deprecation note.
NOTE
The package now uses the exports field instead of main/types. This should be
transparent for most consumers but may require configuration updates for older
bundlers.
Required
Yes.
Migration
utils-logging-codemod preferTopLevel <path to directory with tsconfig.json>
Result
// before
import { LogLevel } from "@com.mgmtp.a12.utils/utils-logging/lib/api.js";
import { ConsoleLoggingStrategy } from "@com.mgmtp.a12.utils/utils-
logging/lib/strategy/ConsoleStrategies.js";
// after
import { LogLevel, ConsoleLoggingStrategy } from "@com.mgmtp.a12.utils/utils-
logging";
LoggerFactory Interface Renamed
The LoggerFactory interface has been renamed to ILoggerFactory to distinguish it from the singleton
instance.
Required
Yes.
Migration
Manual, no codemod available.
Result
// before
5

-- 5 of 8 --

import type { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging/api";
// after, notice the leading "I"
import type { ILoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";
LogLevel Changed from Enum to Union Type
LogLevel is no longer an enum with numeric values, but a union type: "trace" | "log" | "info" |
"warn" | "error". This was done to unblock users from using the erasableSyntaxOnly flag.
Required
Yes.
Migration
utils-logging-codemod logLevelToString <path to directory with tsconfig.json>
Result
// before
import { LogLevel } from "@com.mgmtp.a12.utils/utils-logging";
if (level === LogLevel.INFO) { ... }
const myLevel: LogLevel = LogLevel.WARN;
// after
import type { LogLevel } from "@com.mgmtp.a12.utils/utils-logging";
if (level === "info") { ... }
const myLevel: LogLevel = "warn";
Logger and LoggingStrategy Interface Signatures Changed
The method signatures for Logger and LoggingStrategy have been updated:
• Logger methods changed from (message?: any, …optionalParams: any[]) to (…messages:
unknown[])
• LoggingStrategy.digest changed from (namespace, level, date, message?, …optionalParams) to
(namespace, level, date, …messages)
Custom implementations of these interfaces need to be updated accordingly.
Required
Depending on whether you have custom implementations of Logger or LoggingStrategy.
Migration
Manual, no codemod available.
6

-- 6 of 8 --

Result
// before
import type { LoggingStrategy } from "@com.mgmtp.a12.utils/utils-logging";
export const customStrategy: LoggingStrategy = {
digest(namespace, level, date, message) {
console.log(`Custom Strategy: ${message}`);
}
};
// after
import type { LoggingStrategy } from "@com.mgmtp.a12.utils/utils-logging";
export const customStrategy: LoggingStrategy = {
digest(namespace, level, date, ...messages) {
console.log(`Custom Strategy: ${messages.join(" ")}`);
}
};
2025.06-ext4
Deprecation
Deep import paths of A12 Logging npm package
Nested imports from the npm package @com.mgmtp.a12.utils/utils-logging (e.g.
@com.mgmtp.a12.utils/utils-logging/lib/strategy/ConsoleStrategies.js) are now deprecated in
favor of top-level imports to avoid unnecessary breaking changes and reduce ongoing maintenance
effort.
The ability to use the old-style ("long") imports will be removed in the next breaking release.
To migrate, first install or update the codemod with your preferred package manager, e.g. with
pnpm:
pnpm install -g @com.mgmtp.a12.utils/utils-logging-codemod
Then run the specific recipe:
utils-logging-codemod preferTopLevel <path to directory with tsconfig.json>
// or as a single command
pnpx @com.mgmtp.a12.utils/utils-logging-codemod preferTopLevel <path to directory with
tsconfig.json>
For further details, run the codemod with the --help flag.
7

-- 7 of 8 --

2025.06
Breaking Changes
Migration to ESM
The npm artifact @com.mgmtp.a12.utils/utils-logging was migrated from CommonJS to ESM. When
using Node 22.12+ and modern build tools, there should be no changes necessary to your bundler
setup.
Migrating your own application to ESM is not required, but recommended. Consult the
documentation of your bundler for specifics.
Updating to ES2024
The javascript output of the npm artifact was updated from ES2020 to ES2024 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
Removed Deprecated API
The deprecated API initLoggingSystem was removed. As a replacement, use the Settings singleton to
set your logging strategy directly, as described in the API documentation: Settings.LogStrategy =
<your strategy>;.
Removed Possibility to set Settings in Global Scope
In previous version it was possible to specify Settings by setting a global value named
"@com.mgmtp.a12/logging" on the window object. This is now removed without a replacement.
Please use the Settings singleton to configure any logging settings.
8

-- 8 of 8 --

