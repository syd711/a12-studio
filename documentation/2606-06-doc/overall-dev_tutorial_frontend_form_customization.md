# overall dev_tutorial_frontend_form_customization

Task 3 - Form Customization
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Frontend > Introduction
before continuing here.
You can check out the tag 2025.06-ext5/frontend/task-3-start to follow along this tutorial.
If you get stuck at any point, you can check out the tag 2025.06-ext5/frontend/task-3-end to see
how your code differs from the solution.
Use-Case
In Tutorials > Frontend > Overview Customization, we enhanced our CRM application’s overview
by adding a button to highlight upcoming contact birthdays. This feature helps our customers
remember when a contact’s birthday is today or within the week.
Given the importance of birthday reminders to our customers, we will now extend this
functionality to the contact form.
In the contact form, we will not just highlight an approaching birthday - we will also display how
many days away it is. This ensures that our customers are well-prepared. To achieve this, we will
add an icon next to the date of birth field and a message box at the top of the contact form to
display this information.
End Result
By the end of this task, you will have learned:
• How to navigate and utilize the public API of the Form Engine.
• How to extend and customize the form component provided by A12.
• How to model, access and use annotations within a custom implementation.
Assuming today is July 1st, the final outcome of this task will look as follows:
1

-- 1 of 26 --

Step-by-Step Instructions
Before we begin, let us outline the features we want to add to the contact form using the following
acceptance criteria:
1. Display an icon next to the date of birth field if the contact’s birthday is approaching.
◦ Use one icon if the birthday is today and a different one if it falls within the week.
2. Add a message box at the top of the form to inform the user how many days remain until the
birthday.
Customizing an A12 form to achieve this follows a similar process to the overview customization in
Tutorials > Frontend > Overview Customization.
We will start by creating a new view component for the custom form and integrate it into the
Application Model. Then, we will customize this view component using the available interfaces and
properties to meet the specified requirements.
Creating a Custom Form Component
Let us begin by setting up a view component for the contact form, which we will later customize.
The current view component for the form is defined in the client/src/app/EnginesViewMap.tsx file,
similar to the OverviewEngine component, as shown below:
File: client/src/app/EnginesViewMap.tsx
//...
import type { View } from "@com.mgmtp.a12.client/client-core";
import { CRUDViews } from "@com.mgmtp.a12.crud/crud-core";
2

-- 2 of 26 --

//...
type ViewMap = Record<string, View.ViewComponent | undefined>;
export const enginesViewMap = {
//...
FormEngine(props) {
return <CRUDViews.FormEngineView {...props} />;
},
//...
} satisfies ViewMap;
We will now create our own view component to replace the current one. However, we cannot use
CRUDViews.FormEngineView as the basis for our form customization, as it does not support passing
essential properties such as formModelMap and widgetMap. These properties are necessary for our
customization, but CRUDViews are not intended for such modifications.
In Tutorials > Frontend > Overview Customization > Creating a Custom Overview Component, we
were able to use CRUDViews.OverviewEngineView because it provided the necessary properties for
overview customization. However, this was an exception made to simplify the tutorial and is not
recommended for standard projects.
Since this approach is not feasible for the form, we will instead use the FormEngineViews.FormEngine
component for our custom view component. This component allows us to pass in a custom
formModelMap and widgetMap, providing the flexibility needed for our customization.
For more detailed information about this component and other options for integrating the Form
Engine into the Client of your application, you can refer to Form Engine > Client Integration.
With the information above, you are now ready to create the custom view component and use it in
the Application Model.
Your task:
1. Create the component CustomContactForm that will be displayed in the view component:
◦ Add a new folder called "CustomContactForm" in client/src/modules/contact/components
with a new file named index.tsx.
◦ In this file, create a basic React component with FormEngineViews.FormEngine as the return
value.
2. Extend the custom view provider of the contact module to include this CustomContactForm
component.
3. Use this CustomContactForm for the "ContactForm" scene of the contact module in the Tutorial_AM
Application Model.
▼ Click to see solution
Step 1:
File: client/src/modules/contact/components/CustomContactForm/index.tsx
3

-- 3 of 26 --

import { ReactElement } from "react";
import type { View } from "@com.mgmtp.a12.client/client-core";
import { FormEngineViews } from "@com.mgmtp.a12.formengine/formengine-core";
export default function CustomContactForm(props: View): ReactElement {
return <FormEngineViews.FormEngine {...props} />;
}
Step 2:
File: client/src/modules/contact/index.ts
//...
import CustomContactForm from "./components/CustomContactForm";
const VIEWS: { [name: string]: View.ViewComponent | undefined } = {
HighlightedDateOverview,
CustomContactForm
};
//...
Step 3:
Change the VIEW_ADD directive in the "ContactForm" scene of the contact module in Tutorial_AM as
follows:
4

-- 4 of 26 --

IMPORTANT After making any changes to the models, remember to save them before
restarting the server or deploy them in the SME.
To ensure that our application is still functioning correctly, open any contact form. It should display
the standard form, since no customizations have been added yet.
The properties of the FormEngineViews.FormEngine component consist of the standard View props
(such as the activity id) and additional Form Engine Config props. These Config props include
options like cardView, timeMode, and enablements. You can find the complete list of available props for
Config in Form Engine > API Documentation.
For a first customization, we can once again use cardView. This property determines whether the
card view should be used for repeats.
Your task:
Use the SizeContext in our CustomFormComponent to toggle the cardView for repeats based on the
current screen size.
▼ Click to see solution
File: client/src/modules/contact/components/CustomContactForm/index.tsx
import { ReactElement, useContext } from "react";
//...
5

-- 5 of 26 --

import { SizeContext } from "@com.mgmtp.a12.widgets/widgets-core";
export default function CustomContactForm(props: View): ReactElement {
const { currentSize } = useContext(SizeContext);
const isSmallScreenSize = currentSize === "xs" || currentSize === "sm";
return <FormEngineViews.FormEngine {...props} cardView={isSmallScreenSize} />;
}
You can now open one of the forms and observe how the repeats respond to screen size changes.
On smaller screens, inline repeats - such as phone numbers - should be displayed in cardView, as
shown below:
► https://vimeo.com/1097982328 (Vimeo video)
With this, we have done a first customization of the form in the contact module. We now have a
good basis for implementing the actual feature.
Customizing a Control
We will begin by implementing the first part of our feature: Displaying icons next to the
"DateOfBirth" field in the form. To achieve this, we need to identify and access the specific field next
to which the icon should be displayed.
In the overview, we used the column id for this purpose. However, in the form, we can take
advantage of a more convenient feature: annotations.
Annotation
Annotations can be added to the Form Model to identify specific elements. The resulting application
can then access and use those annotations, e.g. to display all fields with a specific annotation in
bold.
To add an annotation to the "DateOfBirth" field in the Contact_FM Form Model, we need to do the
following:
• Select the "DateOfBirth" control in the Contact_FM to open its editor.
• Go to the section Annotations and add the following entry:
◦ Name: contact-dob-field
◦ Value: true
• Click [ Apply ].
6

-- 6 of 26 --

NOTE The field and value of the annotation can be chosen freely depending on the specific
use case. It is only important to reference them correctly in the code.
Now that we have annotated our field, we need to access this annotation within our custom form.
This can be done via the FormModelMap property in FormEngineViews.FormEngine. You will see how this
works in the following section.
IMPORTANT After making any changes to the models, remember to save them before
restarting the server or deploy them in the SME.
Rendering the Form
To understand how we can add Widgets to our form, we first need to understand how the form is
rendered based on the Form Model.
The FormEngineViews.FormEngine component itself consists of the ScrollHandler, ContentBox and the
FormEngineRenderer. Out of these components, the FormEngineRenderer handles the rendering of the
Form Model.
It gets the state, event handlers and the configuration options from the FormEngineViews.FormEngine
component as props. To customize the rendering of the form, these props can be modified.
You can find more details on how Form Model elements are rendered in Form Engine > Rendering.
The configuration options specifically are what we will use for our customization. They are of type
Config, which we have already seen when dealing with the cardView. Two other properties of this
7

-- 7 of 26 --

interface are formModelMap and widgetMap, which can be used to modify the appearance of the form.
The formModelMap maps each Form Model element to a React component. It defines which
components should be rendered for which element. There are three different kinds of Form Model
elements:
• Containers:
◦ Section
◦ Screen
◦ Control Grid
• Repeats:
◦ Detached Repeats
◦ Inline Repeats
◦ Embedded Repeats
• Controls:
◦ Input elements for modifying the field data of the document.
◦ A complete overview is available in Form Engine > Controls.
The widgetMap, on the other hand, maps each Widget, e.g. Button, to a React component. Parts of this
map can then be replaced with custom Widgets.
For more details on the formModelMap and the widgetMap, you can refer to Form Engine > API
Documentation.
Based on this, both the formModelMap and the widgetMap are necessary for our use case. We will need
the formModelMap to access the "DateOfBirth" control, next to which we want to display our custom
icon Widget, and the widgetMap to handle the actual rendering of that Widget.
FormModelMap
As a first step, we will need to access the formModelMap to identify the "DateOfBirth" control based on
its annotation.
Inside the formModelMap, we find the Control property, which contains all relevant information about
controls, including "DateOfBirth". You can find more details on this property in Form Engine > API
Documentation..
To investigate this further for our application, we can use the formModelMap to log the props of this
Control to the console as follows:
File: client/src/modules/contact/components/CustomContactForm/index.tsx
//...
import { DefaultFormModelMap, FormModelMap } from
"@com.mgmtp.a12.formengine/formengine-core";
8

-- 8 of 26 --

export default function CustomContactForm(props: View): ReactElement {
//...
return <FormEngineViews.FormEngine {...props}
formModelMap={ContactFormFormModelMap} cardView={isSmallScreenSize} />;
}
export const ContactFormFormModelMap: FormModelMap = {
...DefaultFormModelMap,
Control: {
component(controlProps) {
console.log(controlProps);
return <DefaultFormModelMap.Control.component {...controlProps} />;
}
}
};
We see a list of multiple control properties, which makes sense since our form contains multiple
inputs. We will focus on the fourth entry, as "DateOfBirth" is the fourth input in the form. This entry
includes the key and value of the annotation we just set in the SME:
Therefore, we now know how we can access the annotation for our custom view component for the
form.
WidgetMap
However, we cannot access the annotations within the props of the elements of the widgetMap,
which is where we actually need them. This presents a challenge because we need to modify the
Widget in the widgetMap that is used to render the "DateOfBirth" input element.
We can solve this by creating a context for the Control element in the formModelMap and then
9

-- 9 of 26 --

consuming that context in the element that we want to modify within the widgetMap.
To do this, we need to first create a new component that extends the Control element of the default
formModelMap.
Create a new file ContactFormControl.tsx in the folder
client/src/modules/contact/components/CustomContactForm and add the following code:
File: client/src/modules/contact/components/CustomContactForm/ContactFormControl.tsx
import { ReactElement } from "react";
import type { FormModel } from "@com.mgmtp.a12.formengine/formengine-core";
import { DefaultFormModelMap, FormModelMap } from
"@com.mgmtp.a12.formengine/formengine-core";
export default function ContactFormControl(
props: FormModelMap.FormModelComponentProps<FormModel.Control>
): ReactElement {
return <DefaultFormModelMap.Control.component {...props} />;
}
We can now use this ContactFormControl component for the Control property in the formModelMap:
File: client/src/modules/contact/components/CustomContactForm/index.tsx
//...
import ContactFormControl from "./ContactFormControl";
export const ContactFormFormModelMap: FormModelMap = {
...DefaultFormModelMap,
Control: {
component(controlProps) {
return <ContactFormControl {...controlProps} />;
}
}
};
//...
Next, we can create a react context and wrap the Control component with it, as shown below:
File: client/src/modules/contact/components/CustomContactForm/ContactFormControl.tsx
import { ReactElement, createContext } from "react";
//...
export const ContactFormControlContext = createContext<
{ modelElement: FormModel.Control } | undefined
>(undefined);
10

-- 10 of 26 --

export default function ContactFormControl(
props: FormModelMap.FormModelComponentProps<FormModel.Control>
): ReactElement {
return (
<ContactFormControlContext.Provider value={{modelElement:
props.modelElement}}>
<DefaultFormModelMap.Control.component {...props} />
</ContactFormControlContext.Provider>
);
}
By using this context, we will be able to access the modelElement property of the control, that
includes the annotations, whenever we want to consume it deeper the tree.
Custom Input
We will need the annotations later when customizing the input to ensure that only the
"DateOfBirth" control is modified. But first, we need to create the component for our custom input.
The Form Engine, by default, uses the TextLineStateless Widget to display the input for a simple
text in the form. Therefore, we will need to create a custom component for the custom input that
extends and customizes this Widget. To do this, create a new file called
ContactFormTextLineStateless.tsx in the client/src/modules/contact/components/CustomContactForm
folder add the following code:
File: client/src/modules/contact/components/CustomContactForm/ContactFormTextLineStateless.tsx
import { ReactElement } from "react";
import { DefaultWidgetMap } from "@com.mgmtp.a12.formengine/formengine-core";
import type { TextLineStatelessProps } from "@com.mgmtp.a12.widgets/widgets-core";
export default function ContactFormTextLineStateless(props: TextLineStatelessProps):
ReactElement {
return <DefaultWidgetMap.TextLineStateless {...props} />;
}
We now want to customize the input for "DateOfBirth". To do this, we will need to access the
annotations to identify the corresponding control. We can get the array with the annotations from
the modelElement property by using the previously created context.
To achieve this, we need to extend the ContactFormTextLineStateless component by integrating the
ContactFormControlContext context. Once we make sure that this context exists, we can access the
annotations and check if our defined date of birth annotation name exists for the current control. If
this is the case, we will display a customized component that we will create later on. Otherwise, we
will use the default TextLineStateless component.
You can see how this can be implemented in the following code snippet:
11

-- 11 of 26 --

File: client/src/modules/contact/components/CustomContactForm/ContactFormTextLineStateless.tsx
import { ReactElement, useContext } from "react";
//...
import type { FormModel } from "@com.mgmtp.a12.formengine/formengine-core";
import { ContactFormControlContext } from "./ContactFormControl";
// Define the annotation name that was set in the form model
const AnnotationBirthdayAddon = "contact-dob-field";
export default function ContactFormTextLineStateless(props: TextLineStatelessProps):
ReactElement {
// Get the model element of the current control from custom context
const modelElement = useContext(ContactFormControlContext)?.modelElement;
// Check if Model Element is of type Control and has a birthday annotation
if (
modelElement &&
FormModel.Control.isInstance(modelElement) &&
modelElement.annotations?.some((annot) => annot.name ===
AnnotationBirthdayAddon)
) {
// Return a customized component.
}
return <DefaultWidgetMap.TextLineStateless {...props} />;
}
To render this component, we need to customize the TextLineStateless property in the widgetMap.
Since this process is very similar to how we previously customized the formModelMap, you can try
implementing it yourself.
Your task:
• Define a widgetMap in client/src/modules/contact/components/CustomContactForm/index.tsx.
• Customize its TextLineStateless prop by providing the ContactFormTextLineStateless component
instead.
• Pass the customized widgetMap as a prop to the FormEngineViews.FormEngine component.
▼ Click to see solution
File: client/src/modules/contact/components/CustomContactForm/index.tsx
//...
import {
DefaultFormModelMap,
DefaultWidgetMap,
12

-- 12 of 26 --

FormModelMap,
WidgetMap
} from "@com.mgmtp.a12.formengine/formengine-core";
//...
import ContactFormTextLineStateless from "./ContactFormTextLineStateless";
export default function CustomContactForm(props: View): ReactElement {
//...
return (
<FormEngineViews.FormEngine
{...props}
formModelMap={ContactFormFormModelMap}
widgetMap={ContactFormWidgetMap}
cardView={isSmallScreenSize}
/>
);
}
export const ContactFormFormModelMap: FormModelMap = {
//...
};
export const ContactFormWidgetMap: WidgetMap = {
...DefaultWidgetMap,
TextLineStateless(textLineStatelessProps) {
return <ContactFormTextLineStateless {...textLineStatelessProps} />;
}
};
Now, the only missing part is the custom component that we want to return in
ContactFormTextLineStateless for the "DateOfBirth" input when the annotation criteria is fulfilled.
We will create a new file named BirthdayTextLineStateless.tsx in the
client/src/modules/contact/components/CustomContactForm folder for this component.
File: client/src/modules/contact/components/CustomContactForm/BirthdayTextLineStateless.tsx
import { ReactElement } from "react";
import { DefaultWidgetMap } from "@com.mgmtp.a12.formengine/formengine-core";
import type { TextLineStatelessProps } from "@com.mgmtp.a12.widgets/widgets-core";
export default function BirthdayTextLineStateless(props: TextLineStatelessProps):
ReactElement {
return <DefaultWidgetMap.TextLineStateless {...props} />;
}
13

-- 13 of 26 --

In BirthdayTextLineStateless, we will then display an Icon Widget after the input. This is done by
using the addonAfter property of the TextLineStateless component.
//...
import { Icon } from "@com.mgmtp.a12.widgets/widgets-core";
//...
export default function BirthdayTextLineStateless(props: TextLineStatelessProps):
ReactElement {
return <DefaultWidgetMap.TextLineStateless {...props}
addonAfter={<Icon>celebration</Icon>} />;
}
Now, we can use this component in ContactFormTextLineStateless for the "DateOfBirth" input.
However, according to our acceptance criteria, the icon should only be displayed if the contact’s
birthday falls within the week. Additionally, different icons should be used to indicate whether the
birthday is today or later in the week.
To achieve this, we need to determine the relevant date information and pass the appropriate icon
to component.
In Tutorials > Frontend > Overview Customization, we performed similar checks for highlighting
values in the overview. To improve modularity and maintain a clear separation of concerns, we will
extract this logic from the TableBodyCell component into a utility function.
This utility function has already been provided in the starting tag 2025.06-ext5/frontend/task-3-
start, along with the necessary refactoring. You can find all the date-checking functionalities in the
client/src/modules/contact/utils/dateUtils.ts file.
Now, we can use the utility functions isBirthdayTodayOrNextWeek and getDateFromString to determine
whether the entered date of birth is today or within the week. We will also use the LocalizerContext
to ensure the date is correctly parsed from the string format.
To obtain a correctly formatted date of birth, we first encapsulate the parsing helpers in a custom
react hook.
import { /*...*/ useContext } from "react";
//...
import { LocalizerContext } from "@com.mgmtp.a12.utils/utils-localization-react";
import { getDateFromString /*...*/ } from "../../utils/dateUtils";
//...
function useBirthdayDate(birthday: string | undefined): Date | undefined {
const language = useContext(LocalizerContext).locale.language;
return birthday ? getDateFromString(birthday, language) : undefined;
}
14

-- 14 of 26 --

Based on a correct birthday Date object and the isBirthdayTodayOrNextWeek util function, we can
now decide how and which icon to display. For better reusability we move the addon icon creation
into a custom react component.
//...
import { Icon } from "@com.mgmtp.a12.widgets/widgets-core";
//...
import { /*...*/ isBirthdayTodayOrNextWeek } from "../../utils/dateUtils";
//...
function BirthdayAddon({ birthday }: { birthday?: Date }) {
if (birthday) {
const { isBirthdayToday, isBirthdayNextWeek } =
isBirthdayTodayOrNextWeek(birthday);
if (isBirthdayToday || isBirthdayNextWeek) {
return (
<Icon size="big" className={`-u-text-${isBirthdayToday ? "green" :
"yellow"}`}>
{isBirthdayToday ? "celebration" : "notification_important"}
</Icon>
);
}
}
return null;
}
//...
Unlike in Tutorials > Frontend > Overview Customization > Getting the Overview Column Value,
retrieving the contact’s date of birth is much simpler here since input values are stored as data
within the activity and can be easily accessed through the TextLineStatelessProps properties.
Put together our BirthdayTextLineStateless now should look like that:
//...
export default function BirthdayTextLineStateless(props: TextLineStatelessProps):
ReactElement {
const birthday = useBirthdayDate(props.value);
return <DefaultWidgetMap.TextLineStateless {...props} addonAfter={<BirthdayAddon
birthday={birthday} />} />;
}
//...
Integrate the BirthdayTextLineStateless into ContactFormTextLineStateless as our customized
component.
15

-- 15 of 26 --

File: client/src/modules/contact/components/CustomContactForm/ContactFormTextLineStateless.tsx
//...
if (
modelElement &&
FormModel.Control.isInstance(modelElement) &&
modelElement.annotations?.some((annot) => annot.name ===
AnnotationBirthdayAddon)
) {
return <BirthdayTextLineStateless {...props} />;
}
//...
If you open the form of a contact whose birthday is soon, an icon should appear next to the
"DateOfBirth" field, as illustrated below.
• If the birthday of the contact is today:
• If the birthday of the contact is within the week:
16

-- 16 of 26 --

This completes the first part of our feature request and demonstrates how to customize a control
within the form.
Customizing a Container
Next, we will focus on implementing the second part of our requirement: displaying a message box
at the top of each form to inform the user of how many days remain until the contact’s next
birthday.
Unlike the previous customization, this information does not need to be associated with a specific
Form Model element. Instead, we will add it to a container - specifically, the outer screen of the
Contact_FM Form Model.
To achieve this, we will create a custom component to extend the Screen component and use it to
customize the formModelMap. Since many of the steps and principles are similar to those in the
previous section, you can try implementing this on your own.
Your task:
1. Create a custom React component called ContactFormScreen in the
client/src/modules/contact/components/CustomContactForm folder.
◦ Use the default screen as the basis by returning DefaultFormModelMap.Screen.component.
2. Use this component to customize the formModelMap in the CustomContactForm component.
3. Add a Global Message Box Widget above the DefaultFormModelMap.Screen.component with a
placeholder text to the component.
▼ Click to see solution
Step 1:
17

-- 17 of 26 --

File: client/src/modules/contact/components/CustomContactForm/ContactFormScreen.tsx
import { ReactElement } from "react";
import type { FormModel } from "@com.mgmtp.a12.formengine/formengine-core";
import { DefaultFormModelMap, FormModelMap } from
"@com.mgmtp.a12.formengine/formengine-core";
export default function ContactFormScreen(
props: FormModelMap.FormModelComponentProps<FormModel.Screen>
): ReactElement {
return <DefaultFormModelMap.Screen.component {...props} />;
}
Step 2:
File: client/src/modules/contact/components/CustomContactForm/index.tsx
//...
import ContactFormScreen from "./ContactFormScreen";
export const ContactFormFormModelMap: FormModelMap = {
//...
Screen: {
component(screenProps) {
return <ContactFormScreen {...screenProps} />;
}
}
}
Step 3:
File: client/src/modules/contact/components/CustomContactForm/ContactFormScreen.tsx
//...
import { GlobalMessageBox } from "@com.mgmtp.a12.widgets/widgets-core";
export default function ContactFormScreen(
props: FormModelMap.FormModelComponentProps<FormModel.Screen>
): ReactElement {
return (
<>
<GlobalMessageBox content="A very meaningful message" />
<DefaultFormModelMap.Screen.component {...props} />
</>
);
}
18

-- 18 of 26 --

Now, when you open the form, you should see the following information displayed:
As a next step, we will replace the placeholder text with an actual meaningful and dynamic
message. To do this, we first will need to set the proper localization resources.
As we need to display dynamic data within the localized text, we will use a template string like
$var$. We can then pass the data for that template string to the localizer, for example, like this:
localizer(RESOURCE_KEYS.your.custom.key, { var: { type: "plain", value: "x" } })
You can now use this approach for the localization resources for the messages that should be
displayed.
Your task:
• Extend the RESOURCE_KEYS to include keys for localizing the two messages that should be
displayed, depending on whether the birthday is today or in the coming days.
• Provide the corresponding resources for the English and German locale. If the birthday is not
today, include the number of days remaining by using a template string.
• Use these localization resources for the content of the GlobalMessageBox in the ContactFormScreen
component.
▼ Click to see solution
File: client/src/localization/keys.ts
export const RESOURCE_KEYS = {
application: {
//...
19

-- 19 of 26 --

},
contact: {
form: {
screen: {
daysUntilBirthday: "",
birthdayToday: ""
}
}
},
//...
};
File: client/src/localization/resources/en_US.ts
export const en_US: LocalizationKeyTreeType = {
application: {
//...
},
contact: {
form: {
screen: {
daysUntilBirthday: "The contact's birthday is in $daysNum$ day(s).",
birthdayToday: "The contact's birthday is today!"
}
}
},
//...
};
File: client/src/localization/resources/de_DE.ts
export const de_DE: LocalizationKeyTreeType = {
application: {
//...
},
contact: {
form: {
screen: {
daysUntilBirthday: "Der Geburtstag des Kontakts ist in $daysNum$
Tag(en).",
birthdayToday: "Der Geburtstag des Kontakts ist heute!"
}
}
},
//...
};
File: client/src/modules/contact/components/CustomContactForm/ContactFormScreen.tsx
20

-- 20 of 26 --

//...
import { RESOURCE_KEYS, useLocalizer } from "../../../../localization";
export default function ContactFormScreen(
props: FormModelMap.FormModelComponentProps<FormModel.Screen>
): ReactElement {
const localizer = useLocalizer();
return (
<>
<GlobalMessageBox
content={localizer(RESOURCE_KEYS.contact.form.screen.daysUntilBirthday, { daysNum: {
type: "plain", value: "5" } })}
/>
<DefaultFormModelMap.Screen.component {...props} />
</>
);
}
If you now open a form in the application, you will see that our localized resources are being used:
The downside is that we are currently using a static number in our template string, which is not
accurate.
Instead, we need to retrieve the actual date of birth for the contact. Similar to the previous section,
this information can be found in the activity associated with the current form.
To achieve this, we can use the FormEngineSelectors.dataState method to create a selector that
accesses the data slice of the form for the default data holder of a given activity. Some of these
21

-- 21 of 26 --

terms, such as "data holder," may be unfamiliar at this point, but they will be explained in detail in
Tutorials > Frontend > Data in Activities.
The FormEngineSelectors.dataState method only requires the activityId of the currently opened
form, which is available as react context ViewViews.ActivityContext and can be received inside any
client view as follows:
import { useContext } from "react";
import { ViewViews } from "@com.mgmtp.a12.client/client-core";
// activity context and also activityId can be undefined.
// it is recommended to implement a fallback here
const activityId = useContext(ViewViews.ActivityContext)?.activityId ?? "No view
context with activityId provided";
To have our ContactFormScreen stay clean we move all the message related code parts inside an own
custom component named BirthdayReminderBox.
File: client/src/modules/contact/components/CustomContactForm/BirthdayReminderBox.tsx
import { GlobalMessageBox } from "@com.mgmtp.a12.widgets/widgets-core";
import { RESOURCE_KEYS, useLocalizer } from "../../../../localization";
export function BirthdayReminderBox() {
const localizer = useLocalizer();
return (
<GlobalMessageBox
content={localizer(RESOURCE_KEYS.contact.form.screen.daysUntilBirthday, {
daysNum: { type: "plain", value: "5" } })}
/>
);
}
File: client/src/modules/contact/components/CustomContactForm/ContactFormScreen.tsx
//...
import { BirthdayReminderBox } from "./BirthdayReminderBox";
//...
return (
<>
<BirthdayReminderBox />
<DefaultFormModelMap.Screen.component {...props} />
</>
);
22

-- 22 of 26 --

//...
Now, we can use the previously mentioned FormEngineSelectors.dataState method to obtain the
required selector. With this selector, we can retrieve the dataState for the currently open form.
File: client/src/modules/contact/components/CustomContactForm/BirthdayReminderBox.tsx
import { useContext } from "react";
import { useSelector } from "react-redux";
//...
import { FormEngineSelectors } from "@com.mgmtp.a12.formengine/formengine-core";
import { ViewViews } from "@com.mgmtp.a12.client/client-core";
//..
export function BirthdayReminderBox() {
const activityId = useContext(ViewViews.ActivityContext)?.activityId ?? "No view
context with activityId provided";
const dataState = useSelector(FormEngineSelectors.dataState(activityId));
//...
}
However, we still need to extract the value for the "DateOfBirth" field from it. To access the data for
a specific field from the dataState, you will need to typecast it to a type that reflects the path to the
desired field in the Document Model. The necessary type for this is already provided in
client/src/modules/contact/types/contact.ts and is structured as follows:
File: client/src/modules/contact/types/contact.ts
export type Contact = {
PersonalData: {
DateOfBirth?: Date | null;
};
};
As you can see, the Contact type currently only includes one field. This is because it is not necessary
to replicate the entire Document Model structure, only the fields that need to be accessed. Please
note that since "DateOfBirth" is not a required field, it might be set to null.
It is essential to ensure that the Contact type remains aligned with the Document Model.
Consequently, any modifications to the structure of Contact_DM should be updated in the Contact
type accordingly.
With this, we have all the necessary parts to get the current value for the "DateOfBirth" field. To
optimize resolving of the "DateOfBirth" we should move its selection into a custom redux selector.
In react-redux the return value of a useSelector call will trigger a rerender every time it is changed.
Since the BirthdayReminderBox is only necessary to rerender when date of birth changes but not
when any other document data changes, this will help to avoid multiple rerender.
23

-- 23 of 26 --

File: client/src/modules/contact/components/CustomContactForm/BirthdayReminderBox.tsx
//...
import { DefaultRootState /*...*/ } from "react-redux";
//...
import { FormEngineSelectors } from "@com.mgmtp.a12.formengine/formengine-core";
//...
import { Contact } from "../../types/contact";
//...
export function BirthdayReminderBox() {
//...
}
function dateOfBirthSelector(activityId: string) {
return (state: DefaultRootState) => {
// Get the data state from current activity
const dataState = FormEngineSelectors.dataState(activityId)(state);
// The state holds contact data in the shape of our Contact_DM
const contact = (dataState.document as { Contact?: Contact }).Contact;
return contact?.PersonalData.DateOfBirth;
};
}
You can now try completing the BirthdayReminderBox component yourself.
Your task:
• Calculate the number of days remaining until the birthday using the "DateOfBirth" value and
the daysUntil method from client/src/modules/contact/utils/dateUtils.ts.
• Display the dynamic message in the GlobalMessageBox, indicating the actual number of days left
until the birthday.
• Conditionally render the GlobalMessageBox only if the "DateOfBirth" value is provided. This
prevents the message box from appearing when the birthday input is not filled.
▼ Click to see solution
File: client/src/modules/contact/components/CustomContactForm/BirthdayReminderBox.tsx
//...
import { daysUntil, setDateYearToNextOccurrence } from "../../utils/dateUtils";
//...
export function BirthdayReminderBox() {
//...
// Use our custom selector to get the date of birth from current activity
const dob = useSelector(dateOfBirthSelector(activityId));
24

-- 24 of 26 --

if (!dob) {
return null;
}
const daysUntilBirthday = daysUntil(setDateYearToNextOccurrence(dob));
return (
<GlobalMessageBox
content={
daysUntilBirthday
? localizer(RESOURCE_KEYS.contact.form.screen.daysUntilBirthday,
{
daysNum: { type: "plain", value: daysUntilBirthday }
})
: localizer(RESOURCE_KEYS.contact.form.screen.birthdayToday)
}
/>
);
}
function dateOfBirthSelector(activityId: string) {
return (state: DefaultRootState) => {
// Get the data state from current activity
const dataState = FormEngineSelectors.dataState(activityId)(state);
// The state holds contact data in the shape of our Contact_DM
const contact = (dataState.document as { Contact?: Contact }).Contact;
return contact?.PersonalData.DateOfBirth;
};
}
When you now open a form in the application, you will see a message box at the top displaying how
many days remain until the contact’s birthday:
25

-- 25 of 26 --

With this, we have completed the form customization and successfully fulfilled the second part of
the feature request.
Conclusion
Having completed this task, you have learned how to add custom features to a form in A12 and
gained your first experiences with the Form Engine API.
If you got stuck at any point, you can check out the tag 2025.06-ext5/frontend/task-3-end, in order
to see how your code differs from the solution.
« Task 2: Overview Customization Task 4: Data in Activities »
26

-- 26 of 26 --

