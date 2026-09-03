# ai ai user docs

A12 Claude Plugin Marketplace
NOTE
This documentation belongs to an A12 Enterprise Component which is not part of
the Open Source offering (A12 Community Edition). Please feel free to browse the
documentation and learn more about how you can use this A12 component in your
project. Learn more about the benefits from an A12 Enterprise Subscription on the
Editions & Licensing page.
Introduction
The A12 Claude Plugin Marketplace teaches Claude Code about A12. Install the plugins you need,
and Claude gains agents, skills, and knowledge for the everyday work of an A12 project: building
models, writing code, generating test data, and looking things up in the official documentation.
If A12 is new to you, it is mgm’s model-driven low-code platform for long-lived enterprise
applications. What makes it distinctive is a clean split between business content (the JSON models
that domain experts edit) and the runtime that executes them (Spring Boot on the server,
React/Redux on the client). The plugins are built around that split. They know the A12 model types,
stick to A12 conventions, and lean on the official documentation instead of guessing.
What you get
This is not one large plugin but a handful of small ones, each handling a slice of A12 work:
• Modeling: agents and skills that write and check A12 models (Document, Form, Overview,
Relationship, Composed Document, Query, Tree, Print, and Application).
• Coding: help building an A12 application end to end: setup, planning, and working through
problems on both the client and the server.
• Knowledge: the official A12 documentation, searchable from inside Claude Code.
• Utilities: the smaller jobs, such as detecting your A12 version, downloading sources, and wiring
up the model checker.
• Project setup: generates CLAUDE.md, .claudeignore, and AGENTS.md so Claude understands your
project, and keeps coding conventions consistent across frontend, backend, testing,
authorization, and CI.
• Migration: moves a project from one A12 release to the next: it surveys what you have, works
out what changed, and writes a migration plan tailored to your code.
• Test data: creates and validates documents, relationships, and attachments for an A12
workspace.
• Feedback: a skill that notes down modeling, framework, and documentation issues so you can
pass them on to the A12 AI subteam.
The next chapter walks through each plugin in detail.
1

-- 1 of 10 --

Who this is for
This is for A12 developers and modelers who already work in Claude Code and want an assistant
that actually knows the platform. It assumes you have an A12 project to hand and Claude Code
installed.
Getting Started
This chapter walks you through registering the marketplace and installing the plugins you want.
Prerequisites
• A working Claude Code CLI installation.
• Node.js and npm: the plugins are installed as npm packages, and their helper scripts run on
Node (node).
• Java 21 or newer, for model validation. It must be reachable as java on the PATH or via JAVA_HOME.
• Your npm client configured for the @com.mgmtp.a12.ai scope (see Set up npm access).
• A valid A12 license (see the enterprise-component notice in the Introduction).
Set up npm access
The plugins ship as scoped npm packages under @com.mgmtp.a12.ai, and Claude Code pulls them
through npm. Point your npm client at this scope as described in the enterprise npm section of
Accessing A12 artifacts → npm. Skip this step and installs will fail, since npm has no way to find the
@com.mgmtp.a12.ai/* packages.
Add the marketplace and plugins
The marketplace is a single manifest, published at a stable URL:
https://geta12.com/.well-known/ai/claude/marketplace.json
NOTE
Anyone can browse this manifest — it is just the catalog of available plugins. The
plugin artifacts themselves (the @com.mgmtp.a12.ai npm packages) are an A12
Enterprise Component, available only to enterprise customers. Installing a plugin
requires npm access to the @com.mgmtp.a12.ai scope (see Set up npm access), which
in turn requires a valid A12 license.
TIP
Install only the plugins you actually work with. If one plugin needs another (the
modeling plugins depend on a12-utilities, for instance), Claude Code pulls in and
enables that dependency for you.
Pick the install method that suits you — the plugin manager is the simplest.
2

-- 2 of 10 --

Install from the plugin manager
1. Start Claude Code.
2. Open the plugin manager with the /plugin command.
3. Go to Marketplaces and select Add Marketplace.
4. Enter the URL: https://geta12.com/.well-known/ai/claude/marketplace.json
5. Go to Plugins / Discover, then select and install the a12-… plugins you need.
Plugins named a12-{scope}-{version} target one A12 release line, so pick the one matching your
project; plugins named a12-{purpose} work across releases. The Plugin Catalog describes every
plugin.
Install from the command line
Useful for scripted setups or when you prefer the shell.
▼ Show the command-line instructions
Add the marketplace and install plugins by name:
claude plugin marketplace add https://geta12.com/.well-
known/ai/claude/marketplace.json
claude plugin install <name>@a12-plugin-marketplace
For example, a modeler on the A12 2025.06 release line would install:
# tied to an A12 release line — pick the one matching your project
claude plugin install a12-modeling-support-2025.06@a12-plugin-marketplace
# release-independent
claude plugin install a12-utilities@a12-plugin-marketplace
claude plugin install a12-knowledge-support@a12-plugin-marketplace
Install for Claude Desktop
Claude Desktop is not officially supported: its Add Marketplace dialog only accepts git
repositories and rejects the A12 marketplace URL. However, Claude Desktop shares its
configuration with the Claude Code CLI — given a working CLI installation of the plugins (see
above), they are available in the Desktop app as well.
▼ Show installation instructions
Add the marketplace outside the Desktop dialog — Claude Desktop picks it up automatically:
1. Add the marketplace once via the command line (see above), or add it to
~/.claude/settings.json (see Share the marketplace with your team for the
extraKnownMarketplaces snippet — placed in your user settings instead of the project settings).
3

-- 3 of 10 --

2. In Claude Desktop, go to Browse → Plugins → Code → a12-plugin-marketplace to view its
list of plugins and install the ones you need.
For mgm employees
Mgmies please follow the instructions on bitbucket in the claude-plugin-marketplace repository of
the A12 Project.
Work with the plugins
Once a plugin is installed, it reaches your Claude Code session in three ways:
• Slash commands: actions you trigger by name. Type / to see what your installed plugins offer,
for example /download-modelchecker-cli or /validate-a12-models.
• Skills: task-specific know-how that Claude reaches for on its own when your request fits (ask it
to "generate test data for this workspace" and it engages the generate-a12-test-data skill), or
that you can call by name.
• Agents: sub-assistants Claude hands a fitting job to, such as authoring one particular A12 model
type.
In practice you will rarely type these names yourself. Describe what you want in plain language,
such as "validate the models in this workspace" or "look up the A12 widget for a date field", and
Claude reaches for the right skill or agent. Slash commands are there for when you would rather
trigger something directly.
To see what is currently installed and enabled, open the plugin manager with the /plugin
command.
Share the marketplace with your team
Want everyone on a project to get the marketplace automatically? Add it to the project’s
.claude/settings.json.
▼ Show the team-setup snippet
{
"extraKnownMarketplaces": {
"a12-plugin-marketplace": {
"source": {
"source": "url",
"url": "https://geta12.com/.well-known/ai/claude/marketplace.json"
}
}
}
}
This installs the marketplace at project scope, so everyone who checks out the repository gets it.
4

-- 4 of 10 --

Understand installation scopes
Claude Code installs plugins at one of four scopes. This matters only when the same plugin is
installed in more than one place — then the higher-priority scope wins.
▼ Show how the scopes work
Listed here from highest to lowest priority:
1. Managed — installed by an admin via managed settings; read-only, cannot be overridden by
users.
2. Project — stored in .claude/settings.json in the repository; shared with everyone who
checks out the repo (the Share the marketplace with your team approach).
3. Local — stored in your local project settings; applies only to you in this one repo.
4. User (the default) — stored in ~/.claude/settings.json; applies to you across all projects.
To install at a specific scope, use the /plugin UI: Discover → select a plugin → choose the scope.
The CLI (claude plugin install …) defaults to user scope. The Installed tab of /plugin shows all
installed plugins grouped by scope.
Keep plugins up to date
Turn on auto-updates so you are always on the latest plugins: in the Claude Code CLI, go to /plugin
→ Marketplaces → a12-plugin-marketplace → Enable auto-update. Claude Code disables auto-
update by default for third-party marketplaces like this one, so this is an explicit, one-time step.
Updates are driven by plugin versions: a new version reaches you when a plugin’s version is
bumped in the marketplace. Without auto-update, pull updates manually with claude plugin
marketplace update a12-plugin-marketplace or via /plugin → Marketplaces → Update.
Plugin Catalog
Here’s a closer look at each plugin — what it brings to a Claude Code session, the main commands
and skills it adds, and when you’d reach for it. For the install steps, see Getting Started.
The name tells you the scope. A name like a12-{scope}-{version} is tied to one A12 release line, so
install the one that matches your project; a12-{purpose} names work across releases. Grab only
what you need. Whatever you install shows up in two ways:
• Skills and agents — Claude reaches for these on its own when a request fits, and you can also
call a skill by name as a slash command (for example /model-a12-entities).
• Commands — slash commands you trigger explicitly (for example /download-a12-sources).
Solution design
5

-- 5 of 10 --

a12-solution-architect
Reach for this before you start modeling, when the question is what to build rather than how to
author a specific model. It advises on the A12-native architecture for a feature or a whole business
domain — which model types to use, what belongs in a model versus custom code, what runs on
the client versus the server, and how the pieces fit together. It is advisory only: it never writes
models or code, and it works across releases.
• Skills — research-a12-solution (/research-a12-solution) turns a feature or a greenfield business
domain into a concrete A12 design and hands off to /model-a12-entities for the build; review-
a12-architecture (/review-a12-architecture) reviews a described or pasted architecture and
reports its strengths, risks, and concrete improvements.
• Uses the a12-knowledge-support documentation MCP to verify its advice when present, and
works without it.
Modeling
a12-modeling-support-2025.06
This is the workhorse for writing and maintaining A12 models on the 2025.06 release line.
• Agents — one per model type: Application, Composed Document, Content, Document, Form,
Overview, Print, Query, Relationship, and Tree modeling. Claude routes a modeling request to
the agent for the model type in question.
• Skills — model-a12-entities scaffolds new models from a description, form-beautify lays out
and tidies Form Models, and repair-a12-models fixes broken or inconsistent models.
• Depends on a12-utilities for model validation (/validate-a12-models) and model-checker
downloads; Claude Code installs and enables it automatically.
• Uses the a12-knowledge-support documentation MCP when present, and works without it.
a12-modeling-support-2026-06
The same modeling capabilities as above, targeting the A12 2026.06 release line. It ships the same
agents and skills and the same a12-utilities dependency. Install this one instead of the 2025.06
plugin when your project runs on A12 2026.06.
Client coding
a12-widgets-2025-06
Use this when you’re building Plasma widgets for the A12 client (React/Redux) on 2025.06.
• Agent — a12-developer-widgets helps you build and choose widgets, and covers component
architecture, theming, accessibility, and testing.
• Skill — lookup-a12-widget (/lookup-a12-widget) looks up the right Plasma widget for a given
field or use case from the widget inventory.
6

-- 6 of 10 --

a12-form-engine-2025-06
This covers the client-side behaviour of forms at runtime on 2025.06 — how forms act in the
browser, not how you author a Form Model.
• Agent — a12-form-engine-patterns offers TypeScript/redux-saga patterns for the form lifecycle,
switching between readonly and edit mode, and custom form interactions.
• Complements a12-modeling-support-2025.06: its readonly/edit-mode patterns rely on buttons
configured in the Form Model. Neither plugin requires the other.
Project setup and migration
a12-project-setup
This gets an A12 project ready for AI and keeps everyone’s coding conventions in step. It works
with any A12 release.
• Skills — setup-project-for-ai generates CLAUDE.md, .claudeignore, and AGENTS.md for new and
existing projects; coding-practices discovers and enforces A12 coding conventions across the
frontend, backend, testing, authorization, and CI domains.
a12-migration-support
When it’s time to move a project onto a newer A12 release, this is the toolchain for it — whichever
release you’re coming from or heading to.
• Skills — the core flow detects the project’s structure (detect-a12-project-spine), derives what
changed between releases (derive-a12-migration-spine), and produces a project-specific
migration plan (create-a12-migration-plan). Supporting skills apply automated code changes
(run-a12-codemods), validate the result (validate-a12-migration, check-a12-migration-
completeness), and fetch the widgets showcase for a target release (a12-widgets-showcase).
Knowledge and utilities
a12-knowledge-support
This puts the official A12 documentation right inside Claude Code, whatever release you’re on.
• MCP server — the A12 Documentation server lets Claude search the docs and retrieve full
source pages, so answers are grounded in the official documentation. Several other plugins use
it when it is installed and degrade gracefully when it is not.
a12-utilities
A grab-bag of helpers that the other A12 plugins lean on, useful on any release.
• Commands — /download-a12-sources, /download-maven-artifact, /download-npm-artifact,
/download-project-template, and /download-modelchecker-cli.
• Skills — detect-a12-version and lookup-a12-component-versions identify the A12 version in use,
7

-- 7 of 10 --

exploring-a12-sources navigates downloaded A12 source, and validate-a12-models (/validate-
a12-models) runs the A12 model checker.
Test data and feedback
a12-test-data
Generates and checks test data for an A12 workspace, on any release.
• Skill — generate-a12-test-data (/generate-a12-test-data) creates documents, links files and
attachments, registers seed_metadata, and validates the result against the A12 kernel.
a12-feedback-capture
A quick way to capture what tripped you up during a modeling session, on any release.
• Skill — capture-a12-modeling-feedback records plugin, framework, documentation, and
validator issues in a local a12-feedback-<uid>.md file that you can forward to the A12 AI
subteam. It stays out of the way until you ask for it.
Deprecated
a12-coding-support-2025.06
Deprecated. Its capabilities moved to two focused plugins: a12-widgets-2025-06 (widget
development and /lookup-a12-widget) and a12-form-engine-2025-06 (form-engine client patterns).
Install those instead; this plugin will be removed in the next release.
Dependencies between plugins
Some plugins work together:
• The modeling plugins (a12-modeling-support-2025.06 and a12-modeling-support-2026-06) require
a12-utilities for model validation and model-checker downloads. Claude Code installs and
enables this dependency automatically.
• The modeling plugins and a12-widgets-2025-06 use the a12-knowledge-support documentation
MCP when it is installed, and degrade gracefully when it is not.
• a12-form-engine-2025-06 complements a12-modeling-support-2025.06: its readonly/edit-mode
form patterns rely on buttons configured in the Form Model, which the a12-form-modeling agent
in the modeling plugin covers. Neither plugin requires the other.
Versioning
Version-specific plugins (those ending in a release like -2025.06 or -2026-06) are tied to that A12
release line — install the one that matches your project’s A12 version. When two release lines
coexist in the marketplace, pick the plugin whose version matches your project. Version-
independent plugins such as a12-solution-architect, a12-utilities, a12-knowledge-support, a12-
8

-- 8 of 10 --

project-setup, a12-migration-support, a12-test-data, and a12-feedback-capture apply across
releases.
Troubleshooting
Common issues when installing the marketplace or running the plugins, and how to resolve them.
Unknown error while adding the marketplace or
installing a plugin
Run Claude Code with the debug flag to surface the underlying error:
claude --debug
This writes detailed logs to a file in ~/.claude/debug/ (the CLI prints the exact path). The log usually
names the failing step — a network error, an npm resolution failure, or a permissions problem.
Plugin installs go through npm, so also verify your npm client can resolve the plugin packages (see
the npm registry section in Getting Started):
npm view @com.mgmtp.a12.ai/ai-claude-plugin-utilities version
Self-signed certificate / SSL errors
If your organization routes traffic through a TLS-inspecting proxy, npm and Claude Code see your
organization’s certificate instead of the original one and refuse the connection. Install your
organization’s root CA certificate on your machine, or point Node.js at it explicitly with the
NODE_EXTRA_CA_CERTS environment variable:
export NODE_EXTRA_CA_CERTS=/path/to/your-org-root-ca.pem
Ask your IT department for the certificate if you do not have it locally.
Commands fail on Windows although the tool is
installed
On Windows, Claude Code inherits the system environment, not your user session. Tools installed
only for the current user (a user-scoped Git or Node.js installation, for example) are not on the PATH
that Claude Code sees, so their commands fail even though they work in your own terminal.
Install the required tools system-wide (as Administrator) so they register on the system PATH.
9

-- 9 of 10 --

Nothing happens when you start Claude Code
If claude produces no output at all — even with claude --debug or claude --version — the native
installation is likely broken. Reinstall Claude Code, or install it from npm instead:
npm install -g @anthropic-ai/claude-code
10

-- 10 of 10 --

