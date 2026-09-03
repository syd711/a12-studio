# utils_localization utils localization documentation bundle

Localization
Introduction
The A12 localization library is a utility to provide localization & conversion functionality to A12-
based applications.
The library leverages the possibility to add locale-specific translations in A12 models by providing
integration with the model data structures.
Overview
The A12 localization solution currently consists of two separate libraries:
• @com.mgmtp.a12.utils/utils-localization
• @com.mgmtp.a12.utils/utils-localization-react
Both libraries are available as npm artifacts. The latter one is an adapter that can be used to
provide the localization functionality in a React context.
Core Concepts
Locale
A locale is an object that represents a specific geographical, cultural or political region. It consists of
a language and a country specifier, i.e. en and US for the English language in the United States of
America. The country specifier allows a differentiation between different variants of a language
that depend on the (usually) geographical region.
Furthermore, we derive the concept of the PartialLocale from a locale. It only consists of a
language specifier and is used as a fallback option during localization.
An application should always be configured to run with a "full" locale.
Localizable
A localizable is an object that represents an entity which can be translated according to the locale
of the application.
It contains a unique key for identification.
Furthermore, it can contain default translations, i.e. texts for a number of different locales.
Additionally, it can have LocalizableArgs. Those arguments are objects that are used to resolve
placeholders in a translation.
1

-- 1 of 20 --

Placeholders
Any translation, either as a default text in Localizable object or provided from external source, can
be a template string. I.e. it can contain placeholders that shall be resolved during localization. Those
placeholders must be delimited by the dollar sign $. Note that only pairs of $ are treated as
placeholders.
For instance: "Hello, $userName$!".
If you have text inside $ which you don’t want to treat as a placeholder, you can use $$ instead to
escape them. They will be displayed as a single $.
The aforementioned LocalizableArgs, that a localizable can contain, then represent the mapping of
the placeholder name to the value that shall be inserted in its place. There are different kinds of
placeholders that are handled differently according to the semantics of their type.
Currently, the following types are supported:
• plain: The value for the placeholder will be inserted as it is.
• formattable: The value for the placeholder will be formatted according to the formatting
options that were given with the placeholder object. These types of placeholders will usually be
used with the ValueConversion object that can be passed to the localizer factory.
• localizable: The value of the placeholder will itself be localized. That is why the placeholder
object again contains one or more localizables.
• dataFormat: The placeholder represents a specific data format. This is currently only used for
error messages that should contain the format that is expected for specific inputs.
Localizer
The localizer is the central function of the localization library. It takes one or multiple localizables
as arguments and turns them into the localized string.
Many localized texts that shall be presented in the application UI are only based on a single
localizable. However, in some cases a fallback approach is desired. In this case, a list of localizables
has to be passed.
Localizer Factory
The localization library comes with a defaultLocalizerFactory that provides the localizer function
with the A12-default localization algorithm.
This algorithm roughly works as follows:
• It goes through the localizables in the order they were passed.
• For each of the localizables, it tries to find a translation by going through the list of current
locale and all fallback (partial) locales.
◦ This is so-called locale fallback.
2

-- 2 of 20 --

• For each locale, first any externally provided texts are searched.
• If no externally provided text is found, the default texts of the localizable object itself are
searched.
• Only if no text could be found for any of the localizables in any of the locales, then undefined is
returned.
• If a translation was found, the (optional) placeholders in the translation are replaced using the
LocalizableArgs of the respective localizable.
Locale Fallback
The locale fallback happens when a translation cannot be resolved for the locale of the application.
In this case, a list of fallback (partial) locales can be used to try to find a matching translation.
Usually, a fallback from a specific locale to a partial locale makes sense, e.g. from "en_US" to just
"en".
This fallback approach allows an efficient management of the actual translations for the locales
that an application shall support. I.e. all required translations are specified for partial locales
(languages) and only specific translations are added for locales in those cases where they differ
from the language default.
Localization Tree (Map)
A LocalizationTree is a data structure to hold translations for a number of localizable keys. The
nodes of the tree represent the segments of a localizable key. The leafs of the tree contain the
translations.
A LocalizationTreeMap is a mapping of locales to LocalizationTrees, i.e. a data structure that holds
translations for multiple locales. The LocalizationTreeMap is one means to provide translations to
the defaultLocalizerFactory that can override A12-default translations.
DataFormats
The DataFormats interface specifies the contents of a configuration object that holds the formatting
specifics which should be used during localization and conversion.
This configuration is mostly interesting to be able to customize the formats of number and/or date
(time) values in a UI.
It can also hold the configuration for how to represent the data formats themselves, e.g. for data
format output in error messages.
A DataFormats object can be seen as a configuration for an application. It usually depends on the
locale of the application since the representation of dates or numbers vary for different locales.
3

-- 3 of 20 --

ValueConversion
The ValueConversion object is a configuration object which holds the two conversion functions that
can convert typed values to strings and vice versa. These two functions are called formatValue and
parseValue accordingly.
The functions can be used individually when parsing or formatting values and they can also be
passed as a configuration to the localizer function in order to allow it to properly format
placeholders in localizables. Multiple A12 components will also rely on this conversion capability,
e.g. when parsing user input in forms or when displaying data in a locale-specific formats.
The A12 localization library comes with a default implementation that covers all conversions that
can be specified using the DataFormats configuration.
Usage Scenario
The localization libraries are focused on providing localization & conversion to A12-based
applications.
The following ideas shall help to achieve this with minimal efforts.
General
Localization of a text is achieved by calling the localizer with one or multiple localizables.
The localizer itself is usually dependent on a specific locale. In most cases, this is the locale that the
application is running in. That means whenever the application locale changes, the localizer
function should also change. Of course, different parts of an application can use different localizer
functions if the localization behavior should differ between these parts.
As a result of above points, the localizer calls do not require to pass the locale as a parameter.
Similarly, value conversions are done by calling the respective conversion functions. These usually
depend on the DataFormats for a given locale. Thus, this conversion object should also be updated
when the application locale and its DataFormats change.
React Integration
Since A12 is based on the React library, the utils/localization-react library offers an additional
LocalizerContext to provide the localizer & conversion functions to React components.
There are two ways to provide this context:
1. The DefaultLocalizerContextProvider is a function that provides the context based on a locale
and, optionally, a DataFormats configuration object function. In the most basic case, only a locale
must be provided and internally the localizer function and value conversion object are then
created and provided with this context.
2. The standard LocalizerContext.Provider that requires the full configuration object consisting of
4

-- 4 of 20 --

locale, dataFormats, value conversion object and localizer function.
This LocalizerContext should be wrapped around all parts of the application that require
localization. Usually, it should be one of the outermost React components of the application.
Once the context is set up, all child React components can access the localizer and value conversion
functions; and use them to render localized texts or convert values.
Integration Into Redux Sagas and Middlewares
The localization solution is intended to be used almost exclusively in UI-related code, i.e. in React
components. The reason is that localization is a feature for the UI since it revolves around texts that
will be presented to the user of the application. Other texts (e.g. log output) should normally not
require a localization. That is also why the LocalizerContext is the central asset for integration into
such code.
When (background) business logic produces localized texts, it should be checked whether this could
be adapted to produce localizable objects instead. After all, the localized texts are only really
required when they can finally be displayed to the user.
However, in some rare cases one still might need to use the localizer function in internal business
logic like redux sagas and middlewares.
In such cases, one could create an application-specific localizer factory function that should then be
used to configure the React part of the application as well as the background processing in sagas
and middlewares. This centralization is necessary to avoid a discrepancy between the UI and
internal processing. The implementation of that factory function can, of course, make use of the
defaultLocalizerFactory from the A12 localization library. In most cases, the implementation will
require access to the current locale, the data formats, the application-specific translations and the
value conversion (for data formatting of localizable placeholders).
On the React-level, this factory function can be provided with the required parameters using the
respective redux selectors. Memoization is advisable here to avoid redundant re-renderings.
In sagas and middlewares, the same selectors can be used to create the localizer function with this
central factory function.
Examples
Standard Use Case: Consuming the LocalizerContext
A React component is supposed to render some localized text. In order to achieve this, the localizer
is retrieved from the LocalizerContext and called with the respective Localizable.
export function SampleComponent(props: { /* ... **/ }): JSX.Element {
const localizer = React.useContext(LocalizerContext).localizer;
const myLocalizedText = localizer({
key: "my.localizable.key",
args: {},
5

-- 5 of 20 --

defaults: { en: "My default text in English" }
})
return <div>{myLocalizedText}</div>;
}
Custom Localization With Dedicated Context
A React component can create its own LocalizerContext and use it or pass it onto its child
components. This could be done, for example, to use a different locale in this part of the
application.
export function ComponentWithCustomLocalization(props: { locale: Locale }):
JSX.Element {
return (
<DefaultLocalizerContextProvider
locale={props.locale}
>
<LocalizerContext.Consumer>
{
l10nContext =>
l10nContext.localizer({
key: "abc",
args: {},
defaults: { en: "Button Label" }
})
}
<SomeChildComponent/>
</LocalizerContext.Consumer>
</DefaultLocalizerContextProvider>
);
}
Completely Customized LocalizerContext
The LocalizerContext is setup in completely custom fashion which allows to specify a custom
localizer function for the application.
const customDataFormatsProvider = (locale: Locale): DataFormats => {
return { /** insert some custom implementation */};
}
const customLocalizerFactory = (locale: Locale, dataFormats: DataFormats): Localizer
=> {
/** insert some custom implementation, sample below */
// "debugging" localizer that returns the key of the first localizable and the
locale itself
6

-- 6 of 20 --

// all text elements of the application UI will then show their primary
localizable key
return (localizable: Localizable) => {
return `${localizable.key} (${Locale.toString(locale)})`;
};
}
const customConversion: ValueConversion = {
parseValue: (value, inputFormat) => {
/** insert some custom implementation */
return {
value: undefined
}
},
formatValue: (value, outputFormat) => {
/** insert some custom implementation */
return ""
}
}
export function Application(props: { locale: Locale }): JSX.Element {
const localizer = React.useMemo<Localizer>(
() => customLocalizerFactory(props.locale,
customDataFormatsProvider(props.locale)),
[props.locale]
);
return (
<LocalizerContext.Provider
value={{
locale: props.locale,
dataFormats: customDataFormatsProvider(props.locale),
conversion: customConversion,
localizer
}}
>
<ApplicationContent />
</LocalizerContext.Provider>
);
}
Customization
Customization is possible on several levels: One can implement a completely custom localizer
function or parameterize the creation of the localizer function with the help of the
defaultLocalizerFactory.
7

-- 7 of 20 --

Custom Localizer Function
Of course, a custom localizer function can be implemented and used everywhere in the application.
It just has to be passed to the respective components, e.g. via the LocalizerContext. This approach
could result in a lot of effort since all aspects of localization might have to be covered:
• Where to find the respective translations for a given localizable?
• How to resolve placeholders and formatting of (document) field values placeholders?
• How to achieve the locale fallback?
• How to achieve the fallback for localizables, i.e. when multiple localizables are passed in a
localizer call?
• How to prioritize between translations that come with localizables and translation from
external configuration?
If those aspects (mostly) do not apply for the specific application, the custom localizer function can
be a good solution. I.e. if all localization should be handled by an external service, the localizer
function could just delegate to this service.
Custom defaultLocalizerFactory Parameters
Another option is to customize the localizer function by making use of the various parameters of
the defaultLocalizerFactory.
• Locale fallback resolving can be achieved by passing a list of fallback locales.
• Basic placeholder resolving is supported out-of-the-box by the defaultLocalizerFactory.
However, proper (document) field value formatting can only be achieved by passing a
DataFormats configuration object and a optionally also a ValueConversion object.
• A translation source can be passed to either
◦ specify a LocalizationTreeMap that contains additional, application-specific translations that
will be used with priority. I.e. overriding A12-default translation is achieved by passing a
LocalizationTreeMap that contains custom translations for the same keys that localizables
created by A12 components use.
◦ or specify a TranslationFinder callback function which can implement a custom translation
resolving algorithm.
Utility Functionality
Locales
The localization library provides the following functions to work with Locale and PartialLocale
objects:
• Locale.fromString(): Creates a Locale object from its string representation, e.g. "en_US".
8

-- 8 of 20 --

• Locale.toString(): Returns the string representation of a (Partial-)Locale object.
• Locale.isLocale(): Type guard function to check whether a given object is a Locale object.
• Locale.isPartialLocale(): Type guard function to check whether a given object is a
PartialLocale object.
Localizable Keys
Localizable keys are strings with a specific structure. The keys can be split into segments that are
separated by the dot sign .. E.g. the key "documentModel.label.PersonModel.root.Person.name" could
refer to the label of a "name" Field in a document model "PersonModel".
This structure is necessary for interoperability with e.g. the LocalizationTreeMap tree data
structures. Within these trees, the segments of the keys refer to the names of the tree nodes.
However, the segments could themselves contain the . character. For instance, when a document
model name is used as a segment and this model name contains this character.
In order to support such use cases, two utility functions can be used to provide (un-)escaping of the
. character (and the backslash character \ which is used as the escape character):
• localizableKeyFromSegments: This functions turns a given list of segment strings into a valid
localizable key with all necessary escaping applied.
• segmentsFromLocalizableKey: This function splits a given localizable key into its segments and un-
escapes all occurrences of escaped . and \ characters.
• addDotEscaping: This functions returns the given string with all occurrences of . and \ escaped.
• removeEscaping: This functions returns the given string with all escaping undone.
Localizable Creation
There are two built-in factory functions to create localizables:
• localizableFromModel: This function creates the localizable object using the translations of the
given LocalizedModelText. I.e. the localizable defaults are based on the translations that were
specified in the respective A12 model element.
• localizableFromLocalizationTreeMap: This functions creates the localizable object based on a
given key and a LocalizationTreeMap. I.e. the localizable defaults are taken from respective path
in the given LocalizationTreeMap.
Of course, localizables can also be created in custom fashion as e.g. inline object literals or using
custom factory functions.
LocalizationTree & LocalizationTreeMap
These interfaces provide the possibility to specify translations in an efficient tree data structure.
Within A12 we often use it in the following way:
9

-- 9 of 20 --

export const RESOURCE_KEYS: {
dialogs: {
confirmation: {
/** Key of the confirmation dialog title */
title: "",
/** Key of the confirmation dialog message */
message: "",
/** Key of the confirmation dialog confirm button label */
confirm: "",
/** Key of the confirmation dialog cancel button label */
cancel: ""
},
genericError: {
/** Key of the generic error dialog title */
title: "",
/** Key of the generic error dialog message */
message: "",
/** Key of the generic error dialog close button label */
close: ""
}
}
}
initializeKeys(RESOURCE_KEYS);
const en: typeof RESOURCE_KEYS = {
dialogs: {
confirmation: {
title: "Confirmation required",
message: "Do you want to submit your changes?",
confirm: "Submit",
cancel: "Discard"
},
genericError: {
title: "An unexpected error occurred",
message: "The following error occurred: $errorMessage$",
close: "Close"
}
}
};
const de: typeof RESOURCE_KEYS = { /* German translations */ };
export const DEFAULT_TRANSLATIONS: LocalizationTreeMap = { en, de };
The function initializeKeys injects the complete localizable keys into the leafs of the RESOURCE_KEYS
10

-- 10 of 20 --

tree structure. It can then be used in e.g. the rendering code to refer to the localizable keys of the
translations with code completion support.
NOTE
Since the localization tree node names are joined together to form a localizable key,
they may not contain the . or \ characters as explained in the section above. If they
are, however, supposed to contain it to match certain naming conventions, they
must use the escaped formats: \p or \\.
// exemplary section of a render code for a generic error dialog
const titleLocalizable = localizableFromLocalizationTreeMap(
RESOURCE_KEYS.dialogs.genericError.title,
DEFAULT_TRANSLATIONS
);
const messageLocalizable = localizableFromLocalizationTreeMap(
RESOURCE_KEYS.dialogs.genericError.message,
DEFAULT_TRANSLATIONS,
// this specifies the value for the $errorMessage$
placeholder
{ errorMessage: { type: "plain", value:
genericError.message } }
);
const title = localizer(titleLocalizable);
const message = localizer(messageLocalizable);
// ...
Placeholders
The function resolvePlaceholders can be used to replace all placeholder occurrences in a given
string with the values from a given object that maps the placeholder names to their respective
values.
Usage Examples
Overriding the A12-default translations & fallback locale(s)
import { useMemo, type JSX } from "react";
import type {
Localizable,
LocalizationTree,
Localizer,
ValueConversion
} from "@com.mgmtp.a12.utils/utils-localization";
import {
createTextResolverForTreeMap,
11

-- 11 of 20 --

defaultDataFormats,
defaultLocalizerFactory,
defaultTranslationFinderFactory,
defaultValueConversion,
Locale
} from "@com.mgmtp.a12.utils/utils-localization";
import { LocalizerContext } from "@com.mgmtp.a12.utils/utils-localization-react";
interface CustomTranslationsProps {
readonly locale: Locale;
readonly translations: LocalizationTree;
}
export function CustomTranslations(props: CustomTranslationsProps): JSX.Element {
const { locale, translations } = props;
const localizerContext = useMemo(() => {
const customLocalizer = defaultLocalizerFactory({
locale, // fr_CA
fallbackLocales: [{ language: "fr" }], // default: "fr", "en"
translationSource: defaultTranslationFinderFactory(
createTextResolverForTreeMap({
[Locale.toString(locale)]: translations
})
)
});
return {
locale: locale,
dataFormats: defaultDataFormats(locale),
localizer: customLocalizer,
conversion: defaultValueConversion(defaultDataFormats(locale))
};
}, [locale, translations]);
return (
<LocalizerContext.Provider value={localizerContext}>
<LocalizerContext.Consumer>
{context =>
context.localizer({
key: "abc",
args: {},
defaults: {}
})
}
</LocalizerContext.Consumer>
</LocalizerContext.Provider>
);
}
12

-- 12 of 20 --

Implementing a model-dependent localizer
import { useMemo, type JSX } from "react";
import type {
Localizable,
LocalizationTree,
Localizer,
ValueConversion
} from "@com.mgmtp.a12.utils/utils-localization";
import {
createTextResolverForTreeMap,
defaultDataFormats,
defaultLocalizerFactory,
defaultTranslationFinderFactory,
defaultValueConversion,
Locale
} from "@com.mgmtp.a12.utils/utils-localization";
import { LocalizerContext } from "@com.mgmtp.a12.utils/utils-localization-react";
interface ModelDependent {
readonly model: { header: { id: string } };
readonly locale: Locale;
readonly translations: LocalizationTree[];
}
export function ModelDependent(props: ModelDependent): JSX.Element {
const { locale, model } = props;
const localizerContext = useMemo(() => {
const localizer =
model.header.id === "foo"
? // e.g. do something with props.translations, or:
(...localizables: Localizable[]) => localizables[0].key
: defaultLocalizerFactory({ locale });
return {
locale,
dataFormats: defaultDataFormats(locale),
localizer,
conversion: defaultValueConversion(defaultDataFormats(locale))
};
}, [locale, model.header.id]);
return (
<LocalizerContext.Provider value={localizerContext}>
<LocalizerContext.Consumer>
{context =>
context.localizer({
key: "abc",
13

-- 13 of 20 --

args: { fieldValue: { type: "plain", value: 123 } },
defaults: {}
})
}
</LocalizerContext.Consumer>
</LocalizerContext.Provider>
);
}
Customizing everything
import { useMemo, type JSX } from "react";
import type {
Localizable,
LocalizationTree,
Localizer,
ValueConversion
} from "@com.mgmtp.a12.utils/utils-localization";
import {
createTextResolverForTreeMap,
defaultDataFormats,
defaultLocalizerFactory,
defaultTranslationFinderFactory,
defaultValueConversion,
Locale
} from "@com.mgmtp.a12.utils/utils-localization";
import { LocalizerContext } from "@com.mgmtp.a12.utils/utils-localization-react";
const myValueConversion: ValueConversion = {
parseValue: (_value, _inputFormat) => {
// implement your string -> SupportedType parsing logic here
return {
value: undefined,
parseError: undefined
};
},
formatValue: (_value, _outputFormat) => {
// implement your SupportedType -> string formatting logic here
// you can match specific cases by e.g.
// - identifying document model fields based on outputFormat.modelId &
outputFormat.modelPath
// - identifying specific field data types based on outputFormat.type
// - identifying specific values that shall be formatted in a certain way
// - a combination of the above
return "formatted-example-value";
}
};
14

-- 14 of 20 --

function myComplexLocalizerFactory(_locale: Locale, _valueConversion:
ValueConversion): Localizer {
// implement custom localizer approach
// you might need to use your custom conversion logic as well in here in case the
localizables contain formattable placeholders
return (..._localizables) => "foo";
}
export function MyExampleComponent2(props: { locale: Locale }): JSX.Element {
const { locale } = props;
const localizerContext = useMemo(() => {
const localizer = myComplexLocalizerFactory(locale, myValueConversion);
return {
locale,
dataFormats: defaultDataFormats(locale),
localizer,
conversion: myValueConversion
};
}, [locale]);
return (
<LocalizerContext.Provider value={localizerContext}>
<LocalizerContext.Consumer>
{context =>
context.localizer({
key: "abc $replaceMe$",
args: {
replaceMe: {
type: "formattable",
value: "example-value",
properties: {
formattingConfig: {
type: "StringType",
modelId: "sampleModel",
modelPath: [{ elementName: "rootGroupName" },
{ elementName: "fieldName" }]
}
}
}
},
defaults: {}
})
}
</LocalizerContext.Consumer>
</LocalizerContext.Provider>
);
}
15

-- 15 of 20 --

API Documentation
The API documentation for the @com.mgmtp.a12.utils/utils-localization can be found by following
this link.
The API documentation for the @com.mgmtp.a12.utils/utils-localization-react can be found by
following this link.
Breaking Change Management
For the localization libraries the general A12 breaking change interpretation applies.
Migration Instructions
2026.06
Breaking Changes
Codemod recipes exist for some of the breaking changes. To use them, first install or update the
codemod with your preferred package manager, e.g. with pnpm:
pnpm install -g @com.mgmtp.a12.utils/utils-localization-codemod
Then run the specific recipe:
utils-localization-codemod <recipe> <path to directory with tsconfig.json>
// or as a single command
pnpx @com.mgmtp.a12.utils/utils-localization-codemod <recipe> <path to directory with
tsconfig.json>
Updating to ES2025
The javascript output of the npm artifacts was updated from ES2024 to ES2025 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
Required
Depending on whether support for older browsers is required.
Migration
Manual, no codemod available.
16

-- 16 of 20 --

Result
Depending on your bundler setup, consult its documentation on how to include polyfills.
Removal of Deep Import Paths
Deep import paths from @com.mgmtp.a12.utils/utils-localization and @com.mgmtp.a12.utils/utils-
localization-react (e.g. @com.mgmtp.a12.utils/utils-localization/lib/main/index.js) have been
removed. All exports must now be imported directly from the package root. For detailed migration
instructions, see the deprecation note.
NOTE
The package now uses the exports field instead of main/types. This should be
transparent for most consumers but may require configuration updates for older
bundlers.
Required
Yes.
Migration
utils-localization-codemod preferTopLevel <path to directory with tsconfig.json>
Result
// before
import { ValueConversion } from "@com.mgmtp.a12.utils/utils-
localization/lib/main/index.js"
// after
import { ValueConversion } from "@com.mgmtp.a12.utils/utils-localization";
Conversion of Enums and Namespaces to Union Types
All enums and namespaces have been converted to union types and const objects. This was done to
improve treeshaking and to unblock users from using the erasableSyntaxOnly flag.
Before After
enum DatePrecision Union type "FULL" | "DAY_OPTIONAL" |
"MONTH_OPTIONAL" | "YEAR_OPTIONAL"
enum InterpretationOfYear Union type "FROM" | "TO"
namespace SupportedType Const object with same name
namespace Locale Const object with same name
namespace ValueConversion Interface ValueConversionParseError
namespace LocalizerContext Interface LocalizerContextProps
namespace DefaultLocalizerContextProvider Interface DefaultLocalizerContextProviderProps
17

-- 17 of 20 --

Required
Yes.
Migration
// to migrate enums to unions of string literals
utils-localization-codemod enumToUnion <path to directory with tsconfig.json>
// to migrate namespaces to interface
utils-localization-codemod namespaceToInterface <path to directory with
tsconfig.json>
Result
• Replace enum member access with string literals:
// Before
if (datePrecision === DatePrecision.FULL) { ... }
// After
if (datePrecision === "FULL") { ... }
• Replace enum default values with string literals:
// Before
const precision: DatePrecision = DatePrecision.FULL;
// After
const precision: DatePrecision = "FULL";
• Update type imports if using ValueConversion.ParseError:
// Before
import { ValueConversion } from "@com.mgmtp.a12.utils/utils-localization";
const error: ValueConversion.ParseError = ...;
// After
import { ValueConversionParseError } from "@com.mgmtp.a12.utils/utils-
localization";
const error: ValueConversionParseError = ...;
2025.06-ext4
Deprecation
18

-- 18 of 20 --

Deep import paths of A12 Localization npm packages
Nested imports from the npm packages @com.mgmtp.a12.utils/utils-localization and
@com.mgmtp.a12.utils/utils-localization-react (e.g. @com.mgmtp.a12.utils/utils-
localization/lib/main/index.js) are now deprecated in favor of top-level imports to avoid
unnecessary breaking changes and reduce ongoing maintenance effort.
The ability to use the old-style ("long") imports will be removed in the next breaking release.
To migrate, first install or update the codemod with your preferred package manager, e.g. with
pnpm:
pnpm install -g @com.mgmtp.a12.utils/utils-localization-codemod
Then run the specific recipe:
utils-localization-codemod preferTopLevel <path to directory with tsconfig.json>
// or as a single command
pnpx @com.mgmtp.a12.utils/utils-localization-codemod preferTopLevel <path to directory
with tsconfig.json>
For further details, run the codemod with the --help flag.
2025.06-ext2
NOTE
Please be aware that due to changes in A12 internal api, you should not use this
version of localization without Kernel version 30.3.0 which is also part of the
2025.06-ext2 coordinated and end-to-end tested release
2025.06
Breaking Changes
singleDateAllowed removed
Deprecated property DateRangeConversionConfig.singleDateAllowed was removed and
DateRangeConversionConfig.singleDate is now required.
Forbid false as confirm type value
The formatValue function of ValueConversion used to allow false as a valid value for confirm types
(it was treated analogous to null). This special handling was removed and formatValue will now
throw an error in this case.
19

-- 19 of 20 --

Default FallbackLocales Implementation Changed
In the defaultLocalizerFactory, the default implementation for looking up fallback locales has been
changed to not return Locales or PartialLocales of the language "en", if the given locale is of a
different language.
Now, by default only PartialLocales of the same language are returned.
This was changed since it was an unusual default fallback behavior which could lead to a mix of
different languages in the application ui.
If your application uses the defaultLocalizerFactory and relied on this fallback to english, it is easy
to restore the old behavior by providing custom fallbackLocales when you configure your localizer:
const localizer = defaultLocalizerFactory({
locale,
fallbackLocales: createFallbackLocales(locale)
});
function createFallbackLocales(locale: PartialLocale): PartialLocale[] {
if (Locale.isLocale(locale)) {
if (locale.language === "en") {
return [{ language: "en" }];
} else {
return [{ language: locale.language }, { language: "en" }];
}
}
if (locale.language !== "en") {
return [{ language: "en" }];
}
return [];
}
Migration to ESM
The npm artifacts @com.mgmtp.a12.utils/utils-localization and @com.mgmtp.a12.utils/utils-
localization-react were migrated from CommonJS to ESM. When using Node 22.12+ and modern
build tools, there should be no changes necessary to your bundler setup.
Migrating your own application to ESM is not required, but recommended. Consult the
documentation of your bundler for specifics.
Updating to ES2024
The javascript output of the npm artifacts was updated from ES2020 to ES2024 to be able to use latest
language features. When using supported browsers, there is no change necessary. If support for
older browsers is required, make sure to include necessary polyfills.
20

-- 20 of 20 --

