# overall dev_tutorial_frontend_application_frame

Task 1 - Application Frame
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Frontend > Introduction
before continuing here.
You can check out the tag 2025.06-ext5/frontend/task-1-start to follow along this tutorial.
If you get stuck at any point, you can check out the tag 2025.06-ext5/frontend/task-1-end to see
how your code differs from the solution.
Use-Case
In this task we will start by extending the CRM system, that we have created in the intro tutorial. To
prepare for the more advanced customizations in the following tasks, we will mainly focus on
understanding the structure of the client of our A12 application.
For this, we will customize the application frame with a logo, footer and localized title. Additionally,
we will create some static pages for help and FAQ, which we will add as modules to our application.
End Result
Upon finishing this section, you will know:
• How to navigate the client of a standard A12 application.
• How to customize the application frame to your needs.
• How to localize static resources.
• How to add a static module to your A12 application.
In the end, our application will look like the following:
1

-- 1 of 32 --

Step-by-Step Instructions
Initial Client Module
When setting up our application with the Project Template in the intro tutorial, you might have
been surprised by the many files and folders in the client module. However, like many other
frameworks, A12 requires a reasonable amount of boilerplate to get its applications to run.
The files included at the starting point in the Project Template are standard to most A12 projects
and handle the initial setup of the application for us. We will adjust this setup later, based on our
specific needs and requirements. However, for the most part we will add pre-existing setup options
available from a range of A12 components rather than writing the code ourselves.
One thing that should be familiar to you is the client/src/index.tsx file, where you can see how our
application is rendered via ReactDOM:
File: client/src/index.tsx
//...
const mountPoint = document.getElementById("root");
if (mountPoint) {
const root = createRoot(mountPoint);
const available = await waitForServerAvailable();
const { store, initialActions, Component } = setup();
if (available) {
await initialActions();
2

-- 2 of 32 --

}
root.render(
<Provider store={store}> ①
<StyledPage>{available ? Component : <ServerUnavailableNotification
/>}</StyledPage> ②
</Provider>
);
}
① Comes from Redux.
② A React component that wraps the application with global styles and theming. If the server is
unavailable, a fallback notification is shown instead.
In the following sections we will discuss central aspects regarding the user interface of a typical
A12 application: Login screen, localization, application frame and modules.
Login Page
Launching the application for the first time in the browser will lead you to a login page:
This LoginPage is provided by the User Authentication and Authorization (UAA) component as
follows:
File: client/node_modules/@com.mgmtp.a12.uaa/uaa-authentication-
client/src/internal/components/LoginPage.tsx
//...
export interface LoginPageProps {
3

-- 3 of 32 --

readonly logoURL?: string;
readonly imageURL?: string;
readonly uaaClient?: UaaLocalClient | UaaLdapClient;
readonly additionalFormItems?: React.ReactNode[];
readonly additionalFooterItems?: React.ReactNode;
}
//...
/**
*
* @param loginPageProps
*/
export const LoginPage = (loginPageProps: LoginPageProps) => {
As you can see, UAA offers some configuration options for this page, e.g. setting the background
image with imageURL or providing additional items to be displayed in the footer with
additionalFooterItems.
The LoginPage component is then used within client/src/app/AuthBarrier.tsx. You can see below
where and when it is being shown to the user:
File: client/src/app/AuthBarrier.tsx
//...
import { AuthenticationState, UaaSelectors, LoginPage, UaaClient } from
"@com.mgmtp.a12.uaa/uaa-authentication-client";
export const AuthBarrier = ({ children }: PropsWithChildren): ReactNode => {
// Get the authentication state from the Redux store via a selector.
const authenticatedState = useSelector(UaaSelectors.state);
// Check if this value matches our definition of being logged in.
const isAuthenticated = authenticatedState === AuthenticationState.AUTHENTICATED;
// If we are not authenticated, show the login page.
if (!isAuthenticated) {
return <LoginPage imageURL={"/images/login_bg.jpg"}
uaaClient={UaaClient.getLocalClient()} />;
}
// Otherwise, show our application.
return children;
};
The AuthBarrier is registered as a wrapper of the application in client/src/appsetup.ts via the
addWrapper extension point. This ensures that any component rendered by the application is only
shown to authenticated users.
This LoginPage is only available for the English locale out-of-the-box, as it is mainly intended for
development purposes. You can find information on how to localize it, in UAA > Localization.
4

-- 4 of 32 --

On this page the user can now log in to access the application with the credentials "admin"/"A12PT-
admintest". In our case, we will see the following:
Localization
In the intro tutorial we have already been able to localize parts of our application by defining labels
for each locale in the models. However, there are texts in our application that are not set via
modeling, e.g. the title. Such static resources need to be localized in the client module.
The Project Template already comes with all the necessary configurations to support multiple
locales. Out-of-the-box the supported locales are English and German, however this can be adjusted
if necessary.
In this section, we will introduce you to the different aspects necessary to enable localization in
your A12 application as well as how to localize static resources.
Locale Chooser
An important part of localizing an application is the selection of a locale by the user. For this
purpose, the Project Template contains a locale chooser in the upper right corner of the application:
5

-- 5 of 32 --

This locale chooser is provided by the A12 Client component as part of the withLocalization feature.
It automatically generates a drop-down list of all supported locales, based on the configuration
passed to that feature in client/src/appsetup.ts.
File: client/src/appsetup.ts
//...
import { withLocalization } from "@com.mgmtp.a12.client/client-core/localization";
import { DEFAULT_TRANSLATIONS, supportedLocales, getDateTimeResource } from
"./localization";
//...
export function setup() {
const initialConfig: A12ApplicationConfig = {
//...
localization: {
supportedLocales,
translationSource: DEFAULT_TRANSLATIONS,
getDateTimeResource
}
//...
};
//...
const configured = combineFeatures(
withLocalization,
//...
)(initialConfig);
//...
}
When the user clicks on one of the entries in the drop-down list of supported locales, the locale of
the application is changed. The current locale is part of the application state and therefore
managed in the Redux store. You can use the Redux DevTools extension to see this.
6

-- 6 of 32 --

In your own components you can access the current locale via the LocaleSelectors and change it
via the LocaleActions, both provided by the Client component. A typical usage looks like this:
//...
import { LocaleActions, LocaleSelectors } from "@com.mgmtp.a12.client/client-core";
//...
const locale = useSelector(LocaleSelectors.locale());
const dispatch = useDispatch();
const setLocale = (selectedLocale: Locale): void => {
dispatch(LocaleActions.set(selectedLocale));
};
With the framework-provided locale chooser, the user can select and set the locale of the
application out of the box. Now we will take a closer look at how the texts and labels are localized
in the application to accommodate this.
Supported Locales
In the Project Template the main set up for the localization of the application is done in
client/src/localization/index.ts.
There, the list of supported locales supportedLocales is defined, including the language code,
country code, and a localization key for the display name. Additionally, the getDateTimeResource
function provides the corresponding date-fns locale objects used for date and time formatting.
Based on supportedLocales the framework-provided locale chooser generates the drop-down list.
File: client/src/localization/index.ts
//...
export const supportedLocales: LocalizedLocale[] = [
7

-- 7 of 32 --

{ language: "en", country: "US", name: { key: RESOURCE_KEYS.locale.en } },
{ language: "de", country: "DE", name: { key: RESOURCE_KEYS.locale.de } }
];
const DATE_LOCALES: Record<string, DateLocale> = { en: enUS, de: de };
export function getDateTimeResource(locale: Locale): DateTimeContextType {
return { locale: DATE_LOCALES[locale.language] ?? enUS };
}
Localizer
The localization of a text in an A12 application is achieved by using a localizer, which is a function
taking one or multiple localizable objects and turning them into localized strings. This Localizer
function is provided by the A12 Localization library and can be accessed via the LocalizerContext
React context. Please refer to the Localization component documentation for more details.
This context needs to be wrapped around all the React components requiring localization. In the
Project Template setup this is done by the withLocalization feature, which is composed into the
application in client/src/appsetup.ts:
File: client/src/appsetup.ts
//...
import { withLocalization } from "@com.mgmtp.a12.client/client-core/localization";
import { DEFAULT_TRANSLATIONS, supportedLocales, getDateTimeResource } from
"./localization";
//...
export function setup() {
const initialConfig: A12ApplicationConfig = {
//...
localization: {
supportedLocales,
translationSource: DEFAULT_TRANSLATIONS,
getDateTimeResource
}
//...
};
//...
const configured = combineFeatures(
withLocalization,
//...
)(initialConfig);
//...
}
In addition to setting up the LocalizerContext, the withLocalization feature also configures the
8

-- 8 of 32 --

DateTimeContext from Widgets via the getDateTimeResource function. This enables the localization of
the Date Picker and Date Time Picker Widgets in the application, e.g. for the date of birth field.
The LocalizerContext is then utilized to access a function of type Localizer in
client/src/localization/index.ts. In this file a useLocalizer hook is provided that gets this localizer
function from the LocalizerContext. In this hook, the application-specific default translations are
applied and a localizer, that only expects the localization key, is returned.
File: client/src/localization/index.ts
//...
export const applyDefaultTranslations = (localizer: Localizer) => {
return (key: string, args?: LocalizableArgs) =>
localizer(localizableFromLocalizationTreeMap(key, DEFAULT_TRANSLATIONS, args))
?? "";
};
export const useLocalizer = () => {
const { localizer } = useContext(LocalizerContext);
return applyDefaultTranslations(localizer);
};
//...
The last aspect that is now missing from the localization setup are the default translations, which
need to be provided for each locale.
Static Resources
For the static resources, translations need to be provided in a Localizable object along with a key to
identify the text.
These resources are provided by the different A12 components with default translations for the
English and German locale. In client/src/localization/keys.ts, you can find the imports of all the
A12 resource keys that are used by default in the Project Template.
These A12-default texts can be overridden or extended for other locales. Also, additional
application-specific texts, e.g. labels of custom buttons, can be provided.
In the Project Template, these customized static resources are collected for each locale here:
File: client/src/localization/index.ts
//...
export const DEFAULT_TRANSLATIONS: LocalizationTreeMap = {
en: en_US,
de: de_DE
};
//...
9

-- 9 of 32 --

These DEFAULT_TRANSLATIONS are then passed to the withLocalization feature in
client/src/appsetup.ts to create the application-specific localizer via the LocalizerContext, as you
already saw in the previous section.
For each of these custom labels and texts, a key is defined in client/src/localization/keys.ts to
specify the key structure for the translation for each locale. The values for these key-value pairs can
be then found in the client/src/localization/resources folder for each locale.
As an example, we can now try localizing the title of our application which currently is "A12
Tutorial Application". Let us now replace this with a more fitting title for a CRM system.
To update this, we need to look into the file client/src/localization/resources/en_US.ts. This is a
file containing the localized text for many parts of the A12 application in the en_US locale. There is
also a file client/src/localization/resources/de_DE.ts for the German translations next to it. These
files correspond with the two locales currently set up for the application.
In the resource files for our supported locales (client/src/localization/resources/en_US.ts and
client/src/localization/resources/de_DE.ts), we can see the string "A12 Tutorial Application" that
shows in our application. For example:
File: client/src/localization/resources/en_US.ts
export const en_US: LocalizationKeyTreeType = {
application: {
title: "A12 Tutorial Application",
//...
}
//...
}
Therefore, to update the title of our application we just need to change these strings to something
more appropriate. For example, "Customer Relationship Management" and
"Kundenbeziehungsmanagement" for English and German respectively.
Here the naming of the key with "application.title" is important as we are overriding a resource key
defined by the Client in client/node_modules/@com.mgmtp.a12.client/client-
core/src/core/locale/internal/languages/keys.ts.
If you reload the application in the browser, you should now see our new title and how it changes
when you switch the locales. When English is selected, it should look as follows:
10

-- 10 of 32 --

Later on in this task, we will also see how these static resources can be expanded when adding new
custom texts or labels to the application.
Application Frame
The base structure of our application is defined by the layout of its root region, which is defined in
the Application Model. We have already touched on this in Tutorials > Intro > Modeling >
Application Model.
There we have learned that the Client component provides a layout, called ApplicationFrame, out-of-
the-box that is designed and intended for this root region. It is a pre-defined skeleton on which we
can hang our applications features. This is usually used in projects and is also what we have used
for our application.
This application frame is based on the Plasma design, for more details see UI/UX > Plasma – A12
Design System > Application Frame. Client then implements and provides it based on this
specification.
It consists of four elements: Application header, navigation, context (sidebar) and content. Please
refer to Client > Elements of the Application Frame to see how these elements form the application
structure.
The application frame of our application contains three of those elements, as illustrated below:
11

-- 11 of 32 --

This standard application frame from Client can be customized, which is what the Project Template
does by default to add a theme chooser and user information component to the right side of the
application header.
This custom configuration can be found in client/src/app/LayoutProvider.tsx. There you can find a
component named CustomApplicationFrameLayout which returns an ApplicationFrameLayout with
some props:
File: client/src/app/LayoutProvider.tsx
//...
export function CustomApplicationFrameLayout(props:
FrameViews.ApplicationFrameLayoutProps): ReactElement {
const localizer = useLocalizer();
const roles = useSelector(UaaSelectors.roles)?.map((role) => role.name);
return (
<FrameViews.ApplicationFrameLayout
{...props}
permissions={roles}
additionalHeaderItems={[
...(props.additionalHeaderItems ?? []),
{
item: <ThemeChooser />,
orientation: "rightSlots-left"
},
{
item: (
<UserInfoHeader
mobileMode={false}
loggedInAsLabel={localizer(RESOURCE_KEYS.application.header.userinfo.labels.loggedInAs
12

-- 12 of 32 --

)}
logoutButtonLabel={localizer(RESOURCE_KEYS.application.header.userinfo.labels.logoutBu
tton)}
/>
),
orientation: "rightSlots-left"
}
]}
/>
);
}
This custom layout is then made available to the Client by registering it for a region name via the
addLayout extension point. This enables you to reference the layout in the Application Model with
that name.
In the Project Template this is done in client/src/appsetup.ts by overriding the region name
ApplicationFrame from the Client:
File: client/src/appsetup.ts
//...
import { CustomApplicationFrameLayout } from "./app/LayoutProvider";
//...
const viewAndLayoutFeatures = combineFeatures(
//...
addLayout("ApplicationFrame", { component: CustomApplicationFrameLayout })
);
If you are interested, you can find more details on custom layouts in Client > Custom Layouts. We
will now customize the CustomApplicationFrameLayout a bit further by adding a logo and a footer to
it.
Logo
We will start with adding our own logo in the header for some branding.
To do this, we will set the property logo in the ApplicationFrameLayout to a React component
containing the logo.
As a first step, we will create this new component HeaderLogo. Create a new file named
HeaderLogo.tsx in the folder client/src/components and add the following code snippet:
File: client/src/components/HeaderLogo.tsx
import type { ReactElement } from "react";
export default function HeaderLogo(): ReactElement {
13

-- 13 of 32 --

return <img style={{ height: 40 }} src="/images/mgm-Logo.svg" alt="mgm technology
partners logo" />;
}
Feel free to customize it as you wish. The image has already been included for you in the project.
Next, we will make the logo visible in our application by providing this component to the
application frame. For this, set the logo property in the FrameViews.ApplicationFrameLayout
component to be the HeaderLogo component:
File: client/src/app/LayoutProvider.tsx
//...
import HeaderLogo from "../components/HeaderLogo";
function CustomApplicationFrameLayout(props: FrameViews.ApplicationFrameLayoutProps):
ReactElement {
//...
return (
<FrameViews.ApplicationFrameLayout
{...props}
// Set the logo to the HeaderLogo component.
logo={<HeaderLogo />}
//...
/>
);
}
You should now see your logo in the application header.
Footer
Now that we have a logo, we will also add a footer bar with some links to our application.
In the helper-files/task1 directory you should find a file called Footer.tsx which includes a Footer
component. Copy this into the client/src/components folder.
Your task:
Add this component to our application frame, similarly to the logo before. Once this is done, you
should see it in the application as a white bar across the bottom of the page.
▼ Click to see solution
File: client/src/app/LayoutProvider.tsx
//...
import Footer from "../components/Footer";
function CustomApplicationFrameLayout(props:
FrameViews.ApplicationFrameLayoutProps): ReactElement {
14

-- 14 of 32 --

//...
return (
<FrameViews.ApplicationFrameLayout
{...props}
//...
// Footer added here:
footer={<Footer />}
/>
);
}
For now, our footer is just an empty bar at the bottom of the page. Let us add one link to it to flesh it
out, even if it will not point anywhere just yet.
Your task:
• Read about the Link Widget.
• Import the Link component from the Widgets library.
• Add a new Link between the opening and closing StyledFooter styled component in the Footer.
• The links text should just be "Help" for now.
▼ Click to see solution
File: client/src/components/Footer.tsx
//...
import { Link } from "@com.mgmtp.a12.widgets/widgets-core";
//...
export default function Footer(): ReactElement {
return (
<StyledFooter>
<Link>Help</Link>
</StyledFooter>
);
}
That seems good, but the hard-coded string "Help" can be improved by localizing the label. Here we
can now finally see the localizer, that we introduced in the step Localizer, in action.
Your task:
• Add a new key to the RESOURCE_KEYS object in client/src/localization/keys.ts. It can go under
application.
• Update the two locale files within the resources folder de_DE.ts and en_US.ts with the localized
values for these keys, e.g. "Help" (en) and "Hilfe" (de).
• Take a look at the CustomApplicationFrameLayout component (
client/src/app/LayoutProvider.tsx), its first line gives you an idea how to get the localizer via a
custom hook. Then a resource key can be passed to it to localize the text.
15

-- 15 of 32 --

• Back in our footer, replace "Help" with the localized value.
▼ Click to see solution
NOTE Changes to the keys in en_US and de_DE also require the same changes to the
typing of the root object.
File: client/src/localization/keys.ts
export const RESOURCE_KEYS = {
application: {
header: {
//...
},
footer: {
help: ""
}
}
//...
}
File: client/src/localization/resources/en_US.ts
export const en_US: LocalizationKeyTreeType = {
application: {
//...
footer: {
help: "Help"
}
}
//...
}
File: client/src/localization/resources/de_DE.ts
export const de_DE: LocalizationKeyTreeType = {
application: {
//...
footer: {
help: "Hilfe"
}
}
//...
}
File: client/src/components/Footer.tsx
16

-- 16 of 32 --

//...
import { useLocalizer } from "../localization";
//...
export default function Footer(): ReactElement {
const localizer = useLocalizer();
return (
<StyledFooter>
<Link>{localizer("application.footer.help")}</Link>
</StyledFooter>
);
}
Now we have a link with a localized label, but it is not pointing anywhere yet as we are still missing
a page for the help content. This we will take care of in the next step by adding it as a module to our
application.
Modules
As we have experienced in the intro tutorial with the contact module, modules are the building
blocks of A12 applications. Each module usually contains some code and models behind-the-scenes
that enable users to use our application.
In the following, we will discuss the steps of creating a module for the example of a static module.
To assist the users of our CRM application, we will now add a help module containing multiple
static pages.
You will likely recognize many of the following steps from creating and registering the contact
module in Tutorials > Intro > Modeling > Application Model and Tutorials > Intro > Project Template
> Manually Registering a Module. We will therefore not go into too much detail on these points, but
instead focus on some new concepts.
Module Registration
We will now start with creating the help module by registering it:
• Add a new folder in client/src/modules called "help".
• Create an index.ts file with the following content:
import type { Module } from "@com.mgmtp.a12.client/client-core";
const module: Module = {
id: "HelpModule"
};
export default module;
This Module interface is provided by the Client and offers different extension points to implement a
17

-- 17 of 32 --

module, e.g. views, sagas, reducers. However, they only have to return a unique id, the rest is
optional. You will see some examples for these extension points in the following sections and tasks.
Now that we have created the module, we need to register it with the Client. The build process
automatically discovers any module placed in a subfolder of client/src/modules/ and generates
client/src/modules/modules.generated.ts, which exports the list of your own modules. This
generated array is then combined in client/src/modules/index.ts with A12 built-in modules (such
as the AppModelAdapterModule) into a single ALL_MODULES list, so no further action is required from
you.
All modules in ALL_MODULES are registered via the ModuleRegistry, which is provided by the Client
component to add, remove or retrieve modules.
Below you can see how this API is used in the Project Template to initialize the module registry on
the setModelGraph action and unregister the modules on logout.
File: client/src/modules/index.ts
//...
const moduleRegistry = ModuleRegistryProvider.getInstance();
// Get all modules.
export const getAllModules = (): Module[] => {
return ALL_MODULES;
};
// Initializes module registry on `setModelGraph` action.
export const registerModulesOnSetModelGraphMiddleware =
StoreFactories.createMiddleware((api, next, action) => {
if (ModelActions.setModelGraph.match(action)) {
const registeredModules = moduleRegistry.getAllModules();
if (registeredModules.length > 0) {
logger.error(
"Module registry already has modules registered with the following
ids:",
registeredModules.map((module) => module.id)
);
} else {
getAllModules().forEach((module) => moduleRegistry.addModule(module));
}
}
return next(action);
});
// On logout, unregisters all modules.
export const unregisterModulesOnLogoutMiddleware =
StoreFactories.createMiddleware((api, next, action) => {
if (UaaActions.loggedOut.match(action)) {
// The logout action has to be processed first so that any existing activities
18

-- 18 of 32 --

are removed first
const result = next(action);
const moduleIds = moduleRegistry.getAllModules().map(({ id }) => id);
moduleIds.forEach((id) => moduleRegistry.removeModuleById(id));
return result;
}
return next(action);
});
//...
You can learn more about the modularization concept in Client as well as how the API can be used
in Client > Modularization.
We have now successfully created and registered an A12 module. However, there is nothing visible
in our application as we have not yet defined any UI aspects for our module. This is what we will do
next by expanding our Application Model.
Application Model
Before we start with adding our help module to the Application Model, we will shortly go through
the main concepts behind the Application Model again.
Theory
Application (App) Models are UI models that instruct A12 behind-the-scenes which components to
display or to remove from the screen. They consist of modules and define the higher-level UI
aspects based on the Plasma design system.
In the first task of the intro tutorial, you have already created an Application Model for the contact
module with the standard master/detail layout. During this, you were also introduced to its central
terms and concepts. Please make sure to go through Tutorials > Intro > Modeling > Application
Model, as we are building on this knowledge here.
The concept behind the Application Model is quite complex and can take some time to understand.
To make sense of it you can also try to think of it in the following terms:
• Scenes, like in movies, are things taking place on the screen.
• Match conditions start a scene but only when one of these conditions are met. Imagine a film
director yelling "Action" but only when the actors are ready.
• Scene changes describe what happens once the director has yelled "Action".
• "On Enter" refers to what the application should do when it hears the imaginary director yell
"Action".
Getting more practice with creating Application Models can also help with understanding these
concepts, which is why we are recommending to use the opportunity for this in the following
section as well as in Tutorials > Frontend > Data in Activities > Dashboard Module.
19

-- 19 of 32 --

In Client > Arranging UIs, you can also find a short overview over the main terminology and
functionality from a more technical perspective. Some of these aspects will not be covered in the
scope of this tutorial. If you are interested in more details, you can read Client > Application Model.
Please make sure to use the resources provided above to get familiar with the concept and
terminology behind Application Models, as this is a central aspect of the client in A12 applications.
Modeling
Now that you have an overview of the main concepts behind the Application Model, we can start
applying them to model the help module.
Most of the steps and principles are the same as for the contact module in Tutorials > Intro >
Modeling > Application Model. So you can now try this for yourself.
As explained in Tutorials > Intro > Project Template > Extending the Contact Module, since we are
using the adapter module to automatically register our Application Models, it is easiest to just have
one central Application Model for the application. Therefore, we will add the help module to the
existing Tutorial_AM.
Your task:
• Create the new help module in the Tutorial_AM Application Model, analogous to how we did it
for contact in Tutorials > Intro > Modeling > Modules.
• Create a flow in this help module, again in the same way as with contact in Tutorials > Intro >
Modeling > Flows.
• Add a scene to this flow, set a name and specify the match conditions of the help module. You
can find how this was done for contact in Tutorials > Intro > Modeling > Scenes and Tutorials >
Intro > Modeling > Match Conditions.
• You can skip defining a scene change for now, as we will do this next.
▼ Click to see solution
20

-- 20 of 32 --

Now the last aspect that is missing from the help module is the scene change(s), so what we want to
happen when entering or exiting our module.
In the contact module, we added a view of the Form Engine or Overview Engine component to the
content region to display our respective models in a master/detail layout as described in Tutorials >
Intro > Modeling > Scene Change. This is the standard approach for modules that should show an
Overview and Form Model.
In the help module however, we want to display static content. This means that we just want to
display a custom (static) view in the content region without any special layout, which requires
different scene changes.
Since the default layout of the content region is MasterDetail, we first need to change this before we
can add any views.
To do this, we can, when entering the module, use the REGION_CLEAR directive to change the layout of
a region and remove currently displayed views. As we want our views to be displayed without any
additional markup, we will use the layout Null.
To model this scene change, we need to do the following:
• Go to the section Scene Change at HelpModule > HelpFlow > HelpScene.
• Under the section On Enter, click [ Add ].
• Add the following details:
21

-- 21 of 32 --

◦ Type: Select REGION_CLEAR.
◦ Region: Since we have set CONTENT as the default region, we do not need to set it here.
◦ Name: Set Null.
• Click [ Commit ].
After we have changed the layout of the content region with this directive, we can add our custom
view:
• Go to the section Scene Change.
• Under the section On Enter, click [ Add ].
• Add the following details:
◦ Type: Select VIEW_ADD.
◦ Region: Since we have set CONTENT as the default region, we do not need to set it here.
◦ Name: Set HelpPage.
• Click [ Commit ].
As we just want to display some static content, we do not need to specify any models. The static
content will instead be provided through a custom view component, which we have referenced as
HelpPage.
If you now save the changes to the Application Model, and restart the server or deploy the model
changes, you can see that we have a menu item "Help". When clicking it however, we see the
message "No view renderer found: `HelpPage`".
This makes sense of course, as we have referenced the custom view component HelpPage in our
Application Model, but have not yet defined or registered it anywhere. We will take care of this in
the next step.
Custom View Component
We now need to tell A12 what it should show for our HelpPage. This will just be a simple div like
<div>This is my help page</div> at first, but we will improve this in the following section.
But how and where can we register this HelpPage component with the client?
In client/src/app/EnginesViewMap.tsx, you can see how the Project Template defines and registers
the view components that we have used for the Overview and Form Engine in the client module.
We now need to do something similar for the HelpPage.
However, as we do not need to use this component in the whole application, it is enough to just
register it for the help module. For this, we can use an extension point of the Module interface with
which we have defined our help module in client/src/modules/help/index.ts.
Take a look at the following code snippet to see how this works and adjust your
client/src/modules/help/index.ts file accordingly.
WARNING Update the file extension from .ts to .tsx in client/src/modules/help/index.ts.
22

-- 22 of 32 --

This allows us to add JSX directly in the file.
File: client/src/modules/help/index.tsx
//...
// View is used for typing.
import type { View } from "@com.mgmtp.a12.client/client-core";
// Add an object that instructs A12 which component to show for each view name.
// HelpPage matches the name specified in the Application Model.
// The value of HelpPage is a React component.
const VIEWS: { [name: string]: View.ViewComponent | undefined } = {
HelpPage: () => <div>This is my help page</div>
};
// A function which returns a component given a view name.
function viewComponentProvider(name: string) {
return VIEWS[name];
}
const module: Module = {
id: "HelpModule",
// Allows our module to access the new views.
views: () => viewComponentProvider
};
export default module;
Back in the browser, clicking the "Help" menu item now opens a page corresponding to our defined
component for HelpPage, which we will now improve.
Improvements
Currently, our help page works but is still missing some actual content. We actually want to have a
few help pages for users to get acquainted with the application. They should fulfill the following
requirements:
• There should be an overall help page and a FAQ page available.
• Both of them need to be available in all supported locales of the application, so English and
German.
• The content of the pages should be easy to update. Therefore, it should not be hardcoded in
React components, but instead we want to use Markdown (md) to quickly achieve this.
Content
In the helper-files/task1 folder, you will find a file called pages.ts. This exports an object that
contains the content we want to display for the help and the FAQ page in English and German. You
can imagine that the content of these pages in a real application could come from anywhere, e.g. a
23

-- 23 of 32 --

CMS.
But for the scope of these tutorials we will use this approach. For that we will copy this pages.ts file
into the folder client/src/modules/help for the help module.
With our pages defined, we now just need a component for the rendering. This is defined in
MarkdownPage.tsx, which is also provided for you in the helper-files/task1 directory.
Create a new folder called "components" in client/src/modules/help and copy the MarkdownPage.tsx
file into it.
WARNING
MarkdownPage.tsx imports the react-markdown package. This package has already
been added to client/package.json. However, if you have not run gradle build
since checking out the start tag, please do so now or run npm install in the
client folder.
Your task:
There is one to-do task for you in this file regarding getting the current locale, give it a try.
▼ Click to see solution
File: client/src/modules/help/components/MarkdownPage.tsx
//...
const locale = useSelector(LocaleSelectors.locale());
//...
Your task:
Back in our client/src/modules/help/index.tsx file, we now need to tell A12 to use this component
for the HelpPage instead of the inline component we added earlier. Can you work out how to do
that?
▼ Click to see solution
NOTE
We can now change the file extension back from .tsx to .ts in
client/src/modules/help/index.tsx as we are no longer adding JSX directly in the
file.
File: client/src/modules/help/index.ts
//...
import MarkdownPage from "./components/MarkdownPage";
const VIEWS: { [name: string]: View.ViewComponent | undefined } = {
HelpPage: MarkdownPage
};
//...
24

-- 24 of 32 --

With that our help page should now show the improved content we provided. When changing the
locale, the text should also be localized accordingly.
With this, we have fulfilled two of our requirements. We are just missing the FAQ page, but we will
come back to this later. First we need to fix the link in our footer, as this does not yet lead to the
help page.
Footer Link
We have already seen how our help page can be triggered by clicking the "Help" item in the menu
bar. This is defined in the Application Model, as we have already discussed when modeling it for
contact in Tutorials > Intro > Modeling > Match Conditions. But we will shortly summarize again
how this works in this section.
When creating a module in an Application Model, we are defining the name (and optionally labels)
of the menu item for this module and the descriptor of the activity. This descriptor specifies which
activity gets started when the menu entry is selected.
For the help module for example, we have defined the following activity descriptor:
{
"module": "Help"
}
NOTE The activity descriptor can be set based on your preferences, it just needs to be
unique and in valid JSON format.
The activity descriptor is then used to define which scenes should be displayed when the activity is
dispatched. This is achieved by referencing the descriptor in the match condition of the scenes to be
shown.
So once the menu item is clicked and the corresponding activity is dispatched, the application will
look for the first scene with match conditions that correspond to the activity descriptor and display
it.
The match condition that fits the activity descriptor of the help menu item, defines that the key or
the left side of the above state ("module"), has to be the same as (mustEqual) "Help". In the JSON of
the Application Model, this looks like the following:
File: import/models/Tutorial_AM.json
"initialActivity": {
"descriptor": {
"module": "Help"
}
}
As we just have one potential scene for the help module, this match condition is specific enough.
For the contact module on the other hand, we needed to add additional checks as there were two
scenes related to the module. Please refer to Tutorials > Intro > Modeling > Combining Views, to
25

-- 25 of 32 --

learn more about how this works.
In terms of our earlier analogy, the stage is now set. Our actors are in position (due to an activity
descriptor match) and the director yells "Action", our view is added to the page making use of the
view HelpPage we added previously.
This is what we also want to happen when clicking the link in the footer. Therefore, we will now try
to achieve the same without the Application Model for our link.
Activities
Anything happening in our application will have an activity behind it. Activities are the core
concept of the Client, so it is important that you understand what they are and how they work. You
can find more information on activities in A12 in Client > Activity.
There is also a short course available, that gives a short recap on the lower parts of the A12 Client
technology stack and deep dives into the central Client concepts: Activities, modules and
Application Models (with a focus on scenes, directives, regions and layouts). We recommend
watching this for a better understanding of how the client in A12 applications works. You can find it
on the internal and external e-learning platforms.
When we click our "Help" menu item this creates an activity which tells A12 to show our help page.
Consequently, if we want to do the same for the footer link we need to create an activity ourselves.
Like in the localization section, where we saw how a locale was set by dispatching an action, we
will do the same here for requesting an activity to be started.
Your task:
• Add an onClick handler to our help link in the footer component.
• In the handler create an activity making use of ActivityActions. Some hints for this:
◦ You will need to import the following:
import { ActivityActions } from "@com.mgmtp.a12.client/client-core";
◦ Also, you will need this payload:
activityDescriptor: {
module: "Help"
}
• Dispatch the action.
▼ Click to see solution
File: client/src/components/Footer.tsx
//...
import { useDispatch } from "react-redux";
//...
26

-- 26 of 32 --

import { ActivityActions } from "@com.mgmtp.a12.client/client-core";
//...
export default function Footer(): ReactElement {
const dispatch = useDispatch();
const localizer = useLocalizer();
const onFooterItemClick = () => {
// Send it to Redux:
dispatch(
// Create a new activity with a payload that describes our desired
activity:
ActivityActions.create({
activityDescriptor: {
module: "Help"
}
})
);
};
return (
<StyledFooter>
{/* Add onClick here: */}
<Link
onClick={onFooterItemClick}>{localizer("application.footer.help")}</Link>
</StyledFooter>
);
}
Now when you are in the Contact module and try clicking the link in the footer, it should open the
help page. But you will see that both menu items are shown as selected, which we will need to fix.
Linking
To get our link to only show the corresponding menu item as selected, we will need to change our
action in order to more closely resemble what happens when we would click the menu item.
You can see in the Redux DevTools that when clicking the help menu item, we see an activity called
Application/START_MAIN_ACTIVITY_REQUESTED being dispatched:
27

-- 27 of 32 --

We can do the same in our onFooterItemClick function.
Your task:
• Import the namespace ApplicationActions instead of ActivityActions.
• In the onFooterItemClick handler, dispatch a new action from ApplicationActions.
• This time the action payload will be called descriptor instead of activityDescriptor.
▼ Click to see solution
File: client/src/components/Footer.tsx
Replace ActivityActions with the following:
import { ApplicationActions } from "@com.mgmtp.a12.client/client-core";
Replace ActivityActions.create with ApplicationActions.startMainActivityRequested and adjust
the payload:
const onFooterItemClick = () => {
dispatch(ApplicationActions.startMainActivityRequested({
descriptor: {
module: "Help"
}
}));
};
When we now click our link, only the "Help" menu item is displayed as selected. This is because
when using ApplicationActions.startMainActivityRequested, we also instruct A12 to cancel any
28

-- 28 of 32 --

other running activities that might be happening. Thus, only one is shown as selected.
FAQ Module
Previously we added a pages.ts file to our help module which included the content for two pages,
an overall help page and an FAQ page. We have the overall help page showing, so now we just need
to extend our code slightly to also display the FAQ.
The first thing we will need is another localized link in our footer called "FAQ". See if you can add it
yourself, by doing the following.
Your task:
• Create a mapping between the available pages and the Link Widget in the footer component.
• Expand the static resources to include a localized label for the FAQ page.
▼ Click to see solution
The following code gives us the ability to add new help pages to our footer by simply adding a
new entry of a page in our pages.ts file and creating a corresponding resource for the label.
Both links will currently go to the same page, but we will solve this next.
File: client/src/components/Footer.tsx
//...
import { pages } from "../modules/help/pages";
//...
export default function Footer(): ReactElement {
//...
const footerPages = Object.keys(pages);
//...
return (
<StyledFooter>
{footerPages.map((page) => (
<Link key={page} onClick={onFooterItemClick}>
{localizer(`application.footer.${page}`)}
</Link>
))}
</StyledFooter>
);
}
Remember to add a new key to your client/src/localization/keys.ts file and a localized value
for each locale.
File: client/src/localization/keys.ts
export const RESOURCE_KEYS = {
application: {
29

-- 29 of 32 --

//...
footer: {
help: "",
faq: ""
}
}
//...
}
File: client/src/localization/resources/en_US.ts
export const en_US: LocalizationKeyTreeType = {
application: {
//...
footer: {
help: "Help",
faq: "FAQ"
}
}
//...
}
File: client/src/localization/resources/de_DE.ts
export const de_DE: LocalizationKeyTreeType = {
application: {
//...
footer: {
help: "Hilfe",
faq: "FAQ"
}
}
//...
}
Now we just need to let our MarkdownPage component know which page it should render depending
on which link is clicked. To do this, we can pass an extra parameter when clicking our footer links
for the corresponding page. Then in the MarkdownPage, we can use that to get the correct page’s
content.
For this, we now need to add another value to our payload for the clicked page. This payload is
known as an activity descriptor which, as we have already learned, can help us determine what the
activity needs to do. In our case, we want to let the activity know which page should be displayed:
File: client/src/components/Footer.tsx
//...
30

-- 30 of 32 --

const onFooterItemClick = (page: string) => {
dispatch(
ApplicationActions.startMainActivityRequested({
descriptor: {
module: "Help",
// Only set a page property to the descriptor if the page requested is
different from default page (this ensures that the descriptor will match the active
menu item condition for default)
...(page !== defaultPage ? { page } : {})
}
})
);
};
//...
We will also need to pass the clicked page as a parameter to our click handler:
File: client/src/components/Footer.tsx
onClick={() => onFooterItemClick(page)}
Your task:
In our MarkdownPage component you can now try updating the page variable to get the value being
passed.
This value is contained in the activity, you can try logging it to find it. To access specific activity
properties, retrieve the activityId from the props and use the ActivitySelectors.activityPropById
function, which is provided by the Client component.
▼ Click to see solution
File: client/src/modules/help/components/MarkdownPage.tsx
//...
import { ActivitySelectors } from "@com.mgmtp.a12.client/client-core";
import { defaultPage, pages } from "../pages";
//...
export default function MarkdownPage(props: View): ReactElement | null {
const locale = useSelector(LocaleSelectors.locale());
const { activityId } = props;
// Get the page we passed earlier from the activity descriptor.
// If one is not there fall back to the overall default page.
const page = useSelector(ActivitySelectors.activityPropById(activityId, (a) =>
a.descriptor.page)) ?? defaultPage;
return (
<ContentPage>
31

-- 31 of 32 --

<ReactMarkdown>{pages[page]?.[locale.language]}</ReactMarkdown>
</ContentPage>
);
}
You can now click each link, and it will show the requested page, either "Help" or "FAQ". Both pages
are localized and available in English and German. Extra pages can also be added to the pages.ts
file, and they will be shown in the application (once the resources for the link label are added).
Therefore, we have fulfilled all three requirements for our help module.
Conclusion
In this task you have gained some experience in navigating the client module, and the concepts of
application frame and localization have been introduced. Additionally, we discussed modules and
Application Models again, this time for a static page and from a more technical perspective.
If you got stuck at any point, you can check out the tag 2025.06-ext5/frontend/task-1-end, in order
to see how your code differs from the solution.
Task 2: Overview Customization »
32

-- 32 of 32 --

