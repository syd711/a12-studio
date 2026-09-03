# overall breaking_change_management

How We Handle Breaking Changes
Developers, modelers and end users rely on the stability of the A12 platform,
while it evolves, in particular on the level of public programming API,
configuration, modeling, documented functional behavior and appearance
(detailed definition follows below).
Changes in these areas require the attention of project teams when upgrading to a newer A12
release, and might force projects to adapt, e.g. by changing code or configuration followed by
quality assurance, or by explaining a change to their customer. Any change with such an upgrade
impact is called a breaking change.
How We Do It
Justification
Why are we doing this change in a breaking way? Since breaking changes will lead to effort in
other components and projects, we should always have a good and documented reason, e.g.
changes in architecture, alignment of API patterns, new regulation (e.g. for accessibility), better
maintainability.
Management
We make sure that every change is assessed whether it is breaking or not. In the first step we try to
avoid the breaking change by finding an alternative approach that can be realized in a non-
breaking way. If this is not possible or if there are still good reasons to do it in a breaking way, then
the change is planned for a major release (see also A12 Versioning Schema). Minor releases and
patch releases are defined as backward-compatible, i.e. they do not contain breaking changes.
Using deprecation is an approach to grant projects more flexibility in planning their migration.
Explanation
Any breaking change or deprecation requires the following information:
• What has changed?
• Why was it changed this way?
• How to adapt to it?
◦ Is there a strict need to adapt, e.g. because programming API was changed and customer
project code will be broken?
◦ Is there an optional need to adapt, e.g. if behavior or appearance has changed, which could
be accepted?
1

-- 1 of 8 --

Exceptions
No rule without exceptions: there are rare cases, which force us to deliver breaking changes in
minor or patch release. This can happen due to
• necessary security fixes,
• breaking changes introduced by 3rd party dependencies,
• own bugfixing.
If this happens, then we communicate it explicitly along with the release and we provide all other
usual information for breaking changes, e.g. migration instructions.
New components or features are sometimes introduced as experimental and later stabilized and
then productive. Experimental code is marked as such (directly in code structures, in containing
modules and/or in documentation) and is sometimes changed in a breaking way in minor or patch
releases.
General Definition
Breaking Changes
• Compilation fails
• Code adaption is required
• Manual model adaption is required
• Configuration adaption is required
• Infrastructure adaption is required
• Project needs additional effort to restore previous UI, UX (appearance, interaction).
• Only happen in major releases (see also A12 Versioning Schema)
• Migration instructions are provided explaining what, why and how to react
◦ This is also done for breaking change in exceptional cases or experimental code appearing
in minor or patch releases
Non-Breaking Changes
• A12 model migration tool is provided doing the full job without any further interaction by
modelers
• Happen in any release including major, minor and patches releases
• Deprecated code or information will be marked and migration instructions are provided
Public API vs. Internal Code
On code level, we distinguish between documented public API and internal code. Internal code is
2

-- 2 of 8 --

not exported or marked as such by "internal" in naming of artifacts, folders, files, paths, packages,
types, methods or in code level documentation.
Breaking change management is applied to public API only, changes to internal code can happen
any time without notice.
In Detail
Breaking Non-Breaking
Public API
• Remove public API
• Change the signature of public API
incompatibly
◦ Remove parameter
◦ Change sequence of parameter
◦ Change parameter types
◦ Change return type
• Add public signature in an implementable
interface without default implementation
• Add public signature with default
implementation
• Change public signature compatibly
◦ Add optional parameter
• Add public signature in an interface for
usage only
Behavior
• Remove public behavior
• Change public behavior
• Add public behavior
Internal Code
• Change internal code with effect on
documented behavior
• Change internal code without effect on
documented behavior
Configuration
• Add mandatory configuration
• Rename configuration
• Remove configuration
• Change the default value
• Change meaning or behavior of
configuration
• Add new mandatory configuration key with
a default value that maintains existing
behavior
• Add optional configurations keys or values
• Add new value to existing configuration
Dependencies
• Upgrade to new major A12 dependencies
• Upgrade to new major 3rd party
dependencies
• Update to new minor or patch versions of
A12 dependencies
• Update to new minor or patch versions of
3rd party dependencies
• Remove obsolete dependency
3

-- 3 of 8 --

Breaking Non-Breaking
Model and Modeling
The following points are breaking changes, but
they are usually covered fully by automated
model migration scripts, and then they do NOT
count as breaking: * Change the structure of a
model type * Add mandatory elements * Add
mandatory properties * Remove elements *
Remove element properties * Rename elements
* Rename element properties
• Add optional elements
• Add optional properties
• Increase model version covered by model
migration
• Change modeling tools (in a way that does
not impact the resulting models)
Localization
• Add a localization key for a non-optional
feature
• Change a localization key
• Change the usage of a localization key
• Change the localized text in a way that
changes meaning
• Add a localization key for an optional
feature
• Remove a localization key
• Change the localized text slightly
Data Representation
• Change leads to incompatible data
• Change requires data migration
• Change does not impact data compatibility
• Change does not require data migration
Performance
• Significant performance degradation • Significant performance improvements that
are otherwise non-breaking
Frontend Specific Aspects
Frontend specific aspects on top of the general ones above:
Breaking Non-Breaking
Public API
4

-- 4 of 8 --

Breaking Non-Breaking
• Change / removal of the ID attribute
generated by UI engines
• Change / removal of the following attributes
in generated HTML:
◦ data-role
◦ label
◦ aria-label
◦ title
◦ placeholder
◦ aria-selected
◦ aria-checked
◦ aria-expanded
◦ aria-sort
◦ contenteditable
◦ hidden-text
• Removal/rename of a theming variable
• Add public behavior
◦ could lead to compatible change of
models
• Change of internal processing
• Change in HTML structure
◦ e.g. changing the relative location of
data-role to interactive elements
▪ i.e. the relative location is internal,
just like the HTML structure
• Adding id, data-role, title, label, aria-label,
placeholder, hidden-text in generated HTML
• Change of CSS classes
Appearance
• Change appearance with impact on behavior • Change appearance without impact on
behavior
Dependencies
• TypeScript: Use of types or language
constructs that are incompatible to the
communicated TypeScript version of the
release line
◦ i.e. usage of new types from newer
TypeScript versions in public API that
lead to compile errors with the
TypeScript version of the release line
• TypeScript: Use of types or language
constructs that are compatible to the
communicated TypeScript version of the
release line
◦ i.e. usage of new functionality from
newer TypeScript versions in public API
that still compiles with the TypeScript
version of the release line
Considerations about HTML and CSS being internal
Some projects raised concerns about HTML and CSS structures being internal.
Since we want to limit the amount of breaking change and manage the necessary change properly,
the evolution of A12 Widgets would significantly slow down and require a lot more effort.
Therefore, we are interested in keeping HTML and CSS internal. With the following agreement, we
acknowledge the needs of the projects.
5

-- 5 of 8 --

Re Automated Testing
Data-role (and other attribution like title, label, aria-label)
In A12-based web applications, we use a custom web resolver to support automated UI testing with
QF-Test. This resolver extracts specific HTML attributes and enables stable component recognition,
resulting in robust and maintainable test automation.
The resolver operates as follows:
1. data-role Attribute
This is the primary attribute used to determine the QF-Test class of a UI element, such as
TextField, Panel, TextArea, Label, etc. It defines the functional type of the element and is critical
for proper mapping.
2. label
If present, the label attribute is used to identify the UI element together with its class. It usually
reflects the user-facing description or name shown next to the component in the UI.
3. Fallback Attributes
If no label is found, the resolver attempts to use the following attributes in order of priority to
help identify the element:
◦ aria-label
◦ title
◦ placeholder
4. Status Attributes
Following attributes are used for status checks f.g. table column sorting, table row selected,
section expanded or collapsed, content editable
◦ aria-checked
◦ aria-selected
◦ aria-expanded
◦ aria-sort
◦ contenteditable
These attributes are essential to retrieve status or descriptive metadata about UI elements and
support unique or at least more accurate identification, especially in complex UIs.
IDs or specific attributes in this sense are in the first place supported by widgets, which provides
methods to set the ID and guarantee that the appearance of the ID in the HTML markup is stable.
Engines use this widget API and set IDs or attributes that are usually bound to their model - they
keep also this stable.
However, based on our experience, relying on HTML IDs for test automation has proven to be
unsustainable. IDs are often fragile, prone to change during development or refactoring, and
require significant maintenance effort to keep test cases stable. Therefore, our long-term goal is to
phase out the use of HTML IDs for component recognition in favor of more robust and semantically
meaningful attributes. This strategy allows us to build more resilient and maintainable automated
6

-- 6 of 8 --

test cases, while maintaining the flexibility required to adapt to frequent UI changes.
Re Styling
Theming
Widgets defines and implement a theming concept based on an structure of variables.
(https://www.mgm-tp.com/a12.htmlshowcase/index.html#/basics/theme/theming) They define a set
of fundamental default and semantic aspects in variables, e.g. base font, color palette or styling for
selection, focus, hover, destruction, etc. These variables are public API and changing values here
enables you to adjust certain aspects of the appearance consistently and easily.
If you miss a feature here - let’s get in touch.
Customizing CSS
Adding custom CSS on top of everything is of course technically possible, and it might be required
sometimes, e.g. to have a temporary solution until A12 has improved theming capabilities.
Since we do not apply breaking change management on HTML and CSS level, there is a risk that an
A12 upgrade breaks your custom styling. Therefore, if you do it, please keep track of such cases, so
that you can do focused regression tests after the upgrade. See https://www.mgm-tp.com/
a12.htmlshowcase/index.html#/get-started/use-and-configure-widgets-style, section Global style
override (NOT RECOMMENDED).
Customizing Markup
Various widgets provide extension points to inject custom render code, e.g. the Table widgets allows
to hook in custom code to render the header.
These extension points are public API and there to enable you in a reliable way. The responsibility
for the extension is of course on project side.
Custom Widgets
A12 widgets are React components - you can combine A12 widgets with your own widgets or with
3rd party React components.
In many cases you might create higher level custom widgets from lower level A12 widgets - this is
an intended approach and if you run into problems, e.g. styling side effects within A12 widgets due
to particular combinations, please let us know.
Theming for Custom Markup and Custom Widgets
To align the styling of custom extensions or own widgets, you might want to hook into the theming
concept. E.g. if your custom widget has a notion of “selection”, then you can use styling aspects,
which are defined in a semantic way with theming variables. More concretely: you can e.g. use the
selection color variable provided there.
Since the theming variables are public API, this is a valid approach. Doing so enables your project
7

-- 7 of 8 --

to evolve your theme easier and makes your custom widget also a hotter candidate to become an
A12 widget.
Backend Specific Aspects
Backend specific aspects on top of the general ones above:
Breaking Non-Breaking
Public API
• Add authentication requirements
Configuration
• Remove Spring profiles
• Add/remove/change configuration keys in a
Spring profile
• Change configuration format/precedence
• Add a Spring profile
Server Runtime Behavior
• Any changes to the initialization sequence,
including event publishing order
• Any incompatible changes in transaction
handling
• Any changes in error codes and exception
types in public API
• Any compatible changes in transaction
handling
• Any changes to textual error messages
• Any change to log messages and log levels
Dependencies
• Add/remove 3rd party dependencies
8

-- 8 of 8 --

