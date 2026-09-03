# overall dev_tutorial_frontend_overview_customization

Task 2 - Overview Customization
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Frontend > Introduction
before continuing here.
You can then check out the tag 2025.06-ext5/frontend/task-2-start to follow along this tutorial.
If you get stuck at any point, you can check out the tag 2025.06-ext5/frontend/task-2-end to see
how your code differs from the solution.
Use-Case
At this point, we have a working application that already contains the fundamental functionalities
necessary for a CRM system. In this task, we will extend these basics and provide a feature to make
our users work easier.
As it is very important for our users to remember the birthdays of their contacts, they would like to
have the option to make it visible if the birthday of a contact is coming up.
To achieve this, we will add a button to the contact overview. When pressed, it will highlight
contacts whose birthdays are today or within the next week.
End Result
After finishing this task, you will know:
• How to navigate and use parts of the public API of the Overview Engine and Kernel.
• How to extend and customize the overview component provided by A12.
• How to get values for a specific field.
Below you can find an example of the final result. Assume the current date is July 1st. Therefore,
the contacts whose dates of birth are "07/01/1990" (today) and "07/04/2003" (in 3 days) are
highlighted.
► https://vimeo.com/1097950906 (Vimeo video)
1

-- 1 of 20 --

Step-by-Step Instructions
Before we start with any implementation, we need to clarify what exactly we want to achieve. As
detailed above, we want to add a button that highlights the contacts whose birthdays are within the
week. For this, the button should fulfill the following acceptance criteria:
• The button is added to the header of the current overview.
• It can be toggled "on" and "off".
• When toggled "on":
◦ If a contact in the overview has a birthday today, their date of birth is displayed in bold with
red font color.
◦ If a contact in the overview has a birthday in the next 7 days, their date of birth is displayed
in bold with yellow font color.
• When toggled "off", all entries appear as normal.
This means that we need to add a button to the overview component, which is provided by A12. So
far, we have had no input on what is displayed there, outside of our Overview Model. This is also
such a niche feature that it is something we cannot do via modeling.
However, creating our own overview from scratch, when we just want a button and some extra
styles, would be an unreasonable amount of effort. Fortunately, this is not necessary as we can
extend and customize the current overview component by using extension points provided by A12.
Creating a Custom Overview Component
In the client/src/app/EnginesViewMap.tsx file, we can already see where the current view
component for the overview is defined:
File: client/src/app/EnginesViewMap.tsx
//...
import type { View } from "@com.mgmtp.a12.client/client-core";
import { CRUDViews } from "@com.mgmtp.a12.crud/crud-core";
//...
type ViewMap = Record<string, View.ViewComponent | undefined>;
export const enginesViewMap = {
//...
OverviewEngine(props) {
return <CRUDViews.OverviewEngineView {...props} timeMode="24h" />;
}
} satisfies ViewMap;
This view component is referenced in the "ContactOverview" scene of the contact module within
the Tutorial_AM Application Model, as described in Tutorials > Intro > Modeling > Scene Change.
2

-- 2 of 20 --

To create the custom overview, we will need our own view component. We can use the
OverviewEngine component as a basis for this component, and customize it later. As the
customizations are specific to the contact module, we will add the component into that folder.
Your task:
• Add a new folder in client/src/modules/contact called "components".
• Add a new folder in client/src/modules/contact/components called "HighlightedDateOverview".
• Create a new file named index.tsx in
client/src/modules/contact/components/HighlightedDateOverview and scaffold out a basic React
component.
• Set the same return value as the OverviewEngine component above, so
CRUDViews.OverviewEngineView.
TIP Your component should use props of type View. You can pass these directly onto
CRUDViews.OverviewEngineView to remove any errors.
▼ Click to see solution
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
import { ReactElement } from "react";
import type { View } from "@com.mgmtp.a12.client/client-core";
import { CRUDViews } from "@com.mgmtp.a12.crud/crud-core";
export default function HighlightedDateOverview(props: View): ReactElement {
return <CRUDViews.OverviewEngineView {...props} />;
}
WARNING
The CRUDViews.OverviewEngineView component is not intended for
customization, which is why only limited props are available. As we will not
need any other props for our use case, we will use this component to keep it
simple for the scope of the tutorials.
In a project however OverviewEngineFactories.ViewComponent should be set up
and used instead, as this is actually intended for customization.
Now, you can try using the HighlightedDateOverview component to render our Overview Model
instead of the one we currently see.
Your task:
1. In the Tutorial_AM Application Model, use the HighlightedDateOverview for the
"ContactOverview" scene in the contact module.
◦ You can do this using the "Name" field for the VIEW_ADD directive. Update this from
OverviewEngine to HighlightedDateOverview.
◦ This tells A12 that we want to use another view than the one provided in
client/src/app/EnginesViewMap.tsx.
3

-- 3 of 20 --

2. Take inspiration from the "Help" module and add a viewComponentProvider to the contact
module, which includes our HighlightedDateOverview component.
▼ Click to see solution
Step 1:
Change the VIEW_ADD directive in the "ContactOverview" scene of the contact module in
Tutorial_AM as follows:
IMPORTANT After making any changes to the models, remember to save them before
restarting the server or deploy them in the SME.
Step 2:
File client/src/modules/contact/index.ts
//...
import type { View } from "@com.mgmtp.a12.client/client-core";
import HighlightedDateOverview from "./components/HighlightedDateOverview";
// A new component provider to return our HighlightedDateOverview:
const VIEWS: { [name: string]: View.ViewComponent | undefined } = {
HighlightedDateOverview
4

-- 4 of 20 --

};
function viewComponentProvider(name: string) {
return VIEWS[name];
}
const module: Module = {
id: "ContactModule",
// Do not forget to add it here:
views: () => viewComponentProvider
};
export default module;
Back in the browser, you should see that the application works the same as before. This is expected,
as we are still using the exact same component to render our overview. If you want to see a
preview of how this can be customized, try adding the following to your HighlightedDateOverview:
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
//...
export default function HighlightedDateOverview(props: View): ReactElement {
// Add the prop cardView:
return <CRUDViews.OverviewEngineView {...props} cardView />;
}
If you now check the contact overview in the browser, you will see that the rows of the table have
been transformed into cards:
5

-- 5 of 20 --

While this does not look great on a larger screen, you can imagine that this view might be preferred
on a mobile device. So we can add this as a feature:
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
import { ReactElement, useContext } from "react";
//...
import { SizeContext } from "@com.mgmtp.a12.widgets/widgets-core";
export default function HighlightedDateOverview(props: View): ReactElement {
// An A12 context that gets the current size of the screen. e.g. desktop: "lg",
mobile: "sm":
const { currentSize } = useContext(SizeContext);
// Check if the current screen size is small or extra small:
const isSmallScreenSize = currentSize === "xs" || currentSize === "sm";
// If the screen size is a smaller device set the card view:
return <CRUDViews.OverviewEngineView {...props} cardView={isSmallScreenSize} />;
}
The contact overview in our application will now respond to screen size changes in the following
way:
► https://vimeo.com/1097963344 (Vimeo video)
With this, we have already done a first customization of the overview for the contact module. As a
next step, we will focus on implementing the requirement for the data highlighting. This however,
will be more complex than just having to enable a prop.
Adding a Button to the Overview Header
As a first step, we need to add a new button to the overview header to trigger the requested
functionality.
While we could wrap our component in a containing div and add a button that way, we want it to
be part of the overview header itself. So how can we achieve this?
You can find an overview of all props that the CRUDViews.OverviewEngineView component accepts, in
Overview Engine > Overview Engine Component.
One of them is the componentMap property, which is a map used to inject customized components by
overriding those provided in the Overview Engine. You can find more details on how to customize
components with the componentMap in Overview Engine > Component Customization.
In client/node_modules/@com.mgmtp.a12.overviewengine/overviewengine-
core/src/main/view/components, you can see all components provided by the Overview Engine that
can be customized using the componentMap. One of them is the Heading component, which we can use
to make changes to the header of the overview.
Below you can find an example of how we can use the componentMap property to replace the Heading
6

-- 6 of 20 --

with some simple example component:
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
//...
import { ComponentMap, DefaultComponentMap } from
"@com.mgmtp.a12.overviewengine/overviewengine-core";
export default function HighlightedDateOverview(props: View): ReactElement {
//...
// Allows us to inject code into parts of the overview.
const componentMap: ComponentMap = {
...DefaultComponentMap,
Heading(headingProps) {
return <h1>Hello</h1>;
}
};
// Make sure to use it here:
return <CRUDViews.OverviewEngineView {...props} componentMap={componentMap}
cardView={isSmallScreenSize} />;
}
So we are telling CRUDViews.OverviewEngineView to utilize our custom header provided through the
componentMap, rather than the default header. This results in the following overview:
However, this is only a simple example to give you an idea of what we can achieve with this
approach. If you take a look at the type definition for componentMap, you can see everything we can
7

-- 7 of 20 --

replace or enhance with this method.
For our example, we do not want to completely replace the existing header. Instead, we aim to
extend the structure of the existing overview header, rather than create a new heading from
scratch. To do this, we can access the default implementation of components as follows:
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
//...
export default function HighlightedDateOverview(props: View): ReactElement {
//...
const componentMap: ComponentMap = {
...DefaultComponentMap,
Heading(headingProps) {
return (
<>
<DefaultComponentMap.Heading {...headingProps} />
<h1>Hello</h1>
</>
);
}
};
//...
}
This code change will result in the following:
Consequently, we can have custom code while simultaneously keeping the parts of the default
8

-- 8 of 20 --

implementation that we want to remain. With this approach, we can now extend the existing
header with a button.
For this, we will create a separate component for the customized Heading to have a better separation
of concerns between components.
Your task:
• Create a new file named Heading.tsx in the
client/src/modules/contact/components/HighlightedDateOverview folder and create a React
component for Heading.
• Keep in mind that the headingProps will need to be passed in, similar to the
DefaultComponentMap.Heading.
• Customize this Heading component to include a Button Widget.
• The button should be an icon button. You can use the icon "highlight".
• Replace the previous customization of the heading in the componentMap with this Heading
component.
TIP
We will use the utility classes -u-flex -u-items-center -u-justify-end -u-padding-x-xl
-u-margin-y-sm, to add a few styles to our div, such as flex properties and spacing. You
can find more details about them in the Widgets Utility Classes documentation.
▼ Click to see solution
There is nothing particular to A12 here besides the usage of Widgets, which should be somewhat
familiar if you have ever used libraries such as Material UI.
File: client/src/modules/contact/components/HighlightedDateOverview/Heading.tsx
import { ReactElement, PropsWithChildren } from "react";
import { DefaultComponentMap, type Heading } from
"@com.mgmtp.a12.overviewengine/overviewengine-core";
import { Button, Icon } from "@com.mgmtp.a12.widgets/widgets-core";
export default function Heading({...headingProps}:
PropsWithChildren<Heading.PropsType>): ReactElement {
return (
<>
<DefaultComponentMap.Heading {...headingProps} />
<div className="-u-flex -u-items-center -u-justify-end -u-padding-x-xl
-u-margin-y-sm">
<Button primary icon={<Icon size="big">highlight</Icon>} />
</div>
</>
);
}
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
9

-- 9 of 20 --

//...
import Heading from "./Heading";
export default function HighlightedDateOverview(props: View): ReactElement {
//...
const componentMap: ComponentMap = {
...DefaultComponentMap,
Heading(headingProps) {
return <Heading {...headingProps} />;
}
};
//...
}
You should now see the button in the overview header of your application. However, click handling
is still missing. As the next step, we will add the structure to toggle the icon based on the button
click.
Your task:
• Add a state variable to the HighlightedDateOverview to track if the dates of birth should be
highlighted.
• Pass this state and the corresponding function to set it, into the Heading component.
• Define a click handler for the button in Heading that changes this state when the button is
pressed.
• On clicking the button, toggle the icon of the button between two icons, e.g. "highlight" and
"highlight_off".
▼ Click to see solution
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
import { ReactElement, useState, useContext } from "react";
//...
export default function HighlightedDateOverview(props: View): ReactElement {
const [isHighlighted, setIsHighlighted] = useState<boolean>(false);
//...
const componentMap: ComponentMap = {
...DefaultComponentMap,
Heading(headingProps) {
return <Heading {...headingProps} isHighlighted={isHighlighted}
setIsHighlighted={setIsHighlighted} />;
}
};
//...
}
10

-- 10 of 20 --

File: client/src/modules/contact/components/HighlightedDateOverview/Heading.tsx
import { ReactElement, PropsWithChildren, Dispatch, SetStateAction } from "react";
//...
interface HeadingProps extends PropsWithChildren<Heading.PropsType> {
isHighlighted: boolean;
setIsHighlighted: Dispatch<SetStateAction<boolean>>;
}
export default function Heading({
isHighlighted,
setIsHighlighted,
...headingProps
}: HeadingProps): ReactElement {
const handleClick = () => {
setIsHighlighted((prevIsHighlighted) => !prevIsHighlighted);
};
return (
<>
<DefaultComponentMap.Heading {...headingProps} />
<div className="-u-flex -u-items-center -u-justify-end -u-padding-x-xl
-u-margin-y-sm">
<Button
onClick={handleClick}
primary
icon={<Icon size="big">{!isHighlighted ? "highlight" :
"highlight_off"}</Icon>}
/>
</div>
</>
);
}
With this, we can now toggle the button "on" and "off", and see the current state based on its icon:
► https://vimeo.com/1097967238 (Vimeo video)
Now, we are only missing the actual highlighting of the relevant dates of birth in the contact
overview.
Highlighting the Date
In the same way that we added custom code for the Heading, we can customize the values in the
overview. In the list of overview components in
client/node_modules/@com.mgmtp.a12.overviewengine/overviewengine-core/src/main/view/components,
we can find the TableBodyCell which is suitable for this as it corresponds with each cell in the
overview displaying a value.
11

-- 11 of 20 --

Since you have already seen how to customize the header, you can now try customizing the
TableBodyCell yourself.
Your task:
• Return another component instead of the default TableBodyCell, e.g. something showing the
string "Hello World".
• Make it conditional on your custom button being clicked.
▼ Click to see solution
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
//...
export default function HighlightedDateOverview(props: View): ReactElement {
//...
const componentMap: ComponentMap = {
//...
TableBodyCell(tableBodyCellProps) {
if (isHighlighted) {
return <span>Hello World</span>;
}
return <DefaultComponentMap.TableBodyCell {...tableBodyCellProps} />;
}
};
//...
}
Now, when you click the button, the overview should display something like this:
12

-- 12 of 20 --

For our final highlighting, we want to apply changes to only one column. Therefore, we will extend
this "Hello World" feature to target a specific column.
To do this, we need to identify the column that we want to customize. For this we can use the
column id which was generated when we created the column in the Overview Model.
You can find the id of the column via the Simple Model Editor. Alternatively, you can look directly
at the JSON file and find the id of your column in the columns array, based on the order they are
shown on screen.
The column id is "column-a7455" for the Overview Model of the modeling solution. Please note that
your column id will likely differ if you are using your own models, so you will need to adjust it
accordingly in the code snippet below.
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
//...
export default function HighlightedDateOverview(props: View): ReactElement {
//...
const componentMap: ComponentMap = {
//...
TableBodyCell(tableBodyCellProps) {
const { columnModel } = tableBodyCellProps;
// Replace "column-a7455" with your column id!
if (columnModel.id === "column-a7455" && isHighlighted) {
return <span>Hello World</span>;
}
return <DefaultComponentMap.TableBodyCell {...tableBodyCellProps} />;
}
};
//...
}
You can now see in the application that only the date of birth column changes:
13

-- 13 of 20 --

Using the generated column id is not ideal. To improve this, you can update the value in the
Contact_OM.json file for this column to something more meaningful, such as "column-highlight-
date":
File: import/models/contact/Contact_OM.json
//...
{
"id": "column-highlight-date",
"width": 1,
"styles": {},
"elementRef": "F7",
"sortable": false
},
//...
IMPORTANT After making any changes to the models, remember to save them before
restarting the server or deploy them in the SME.
We can then use this in our column checks instead:
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
//...
export default function HighlightedDateOverview(props: View): ReactElement {
//...
const componentMap: ComponentMap = {
14

-- 14 of 20 --

//...
TableBodyCell(tableBodyCellProps) {
const { columnModel } = tableBodyCellProps;
// Our newly defined id:
if (columnModel.id === "column-highlight-date" && isHighlighted) {
return <span>Hello World</span>;
}
return <DefaultComponentMap.TableBodyCell {...tableBodyCellProps} />;
}
};
//...
}
Now that we are targeting the right column, we just need to do the following:
• Get the value of the cell rendered by the TableBodyCell, rather than "Hello World".
• Do our date check to see if the contact’s birthday is today or within the week.
• If it is, wrap the default TableBodyCell in a div with some styles, e.g. bold and color.
• If not, return the default TableBodyCell.
Getting the Overview Column Value
Getting the value of a cell to display in the TableBodyCell takes a little work and some help from the
Kernel library. Specifically, we will use the kernel-md-facade module of the Kernel TypeScript API,
which provides access to all services regarding Document Models and documents. For more
information about this API, you can refer to Kernel > TypeScript/JavaScript Artifacts.
The entry point for all model and document related services in this API is the
DocumentServiceFactory class, from which instances of the following three interfaces can be
retrieved:
• DocumentModelSearchService: Contains search services for Document Models.
• DocumentModelSerializer: Deserializes Document Models from a string representation.
• DocumentService: Provides different functions for transforming documents in JSON-format.
We will now use the DocumentModelSearchService to search our Document Model for the field
associated with the column and the DocumentService to access the value for the cell in that column.
But first, we will need to get the Document Model referenced in the Overview Model for our
overview. This can be accessed for the overview via context:
const documentModel = useOverviewEngineContext((context) => context.documentModel);
Using this Document Model, we can obtain a DocumentModelSearchService to search it. We can
retrieve this service from an instance of the DocumentServiceFactory class:
15

-- 15 of 20 --

const documentSearchService = new
DocumentServiceFactory().getDocumentModelSearchService(documentModel);
Before we do any searching, we need to define what exactly we are looking for. Since we want to
find the field referenced in the column, we first need to access this column.
As we are doing this in the context of the TableBodyCell component, we can get the column (and the
row which we will need later to identify the cell) from its props. We then just need to ensure that
the column is a reference column and actually pointing to a field.
const { columnModel, row } = props;
if (!OverviewModel.ReferenceColumn.isAssignableFrom(columnModel)) {
throw new Error("Column not a reference Column");
}
Now that we have the column, we also have the elementRef of the field it is referencing. With this id,
we can then get the corresponding path to the field in the Document Model using the
DocumentModelSearchService. This tells us the location of the field in the Document Model.
If the model path does not exist, it means the field cannot be found, and we need to throw an error.
const modelPath = documentSearchService.getPathById(columnModel.elementRef);
if (!modelPath) {
throw new Error(`Cannot find model path with id ${columnModel.elementRef}!`);
}
Using the model path, we can then retrieve the corresponding field element from the Document
Model via the DocumentModelSearchService. This provides us with more detailed information about
the field, which will be necessary later for styling the cell. If this field element does not exist, we
need to throw an error, indicating that the field cannot be found.
const field = documentSearchService.getByPath(modelPath);
// There is no field
if (!field) {
throw new Error(`Cannot find field with id ${columnModel.elementRef}!`);
}
Once we have the model path and the field, we can use the DocumentService to access the value of
that field for a specific document. But first, we need to retrieve it from the DocumentServiceFactory:
const documentService = new DocumentServiceFactory().getDocumentService();
Since this function call has no dependencies, it will be defined outside of the component to avoid
being recreated every time the component re-renders.
We can then use the DocumentService to get the value for the cell. A cell in the overview is defined by
16

-- 16 of 20 --

its column, which corresponds to a field in the Document Model, and its row, which represents a
document as an instance of that Document Model.
To retrieve its value, we can use the getAssignedObject method from the DocumentService, which
returns the value for a given field and document. Therefore, we can pass in the row and the
previously determined model path to get it. If the value exists, we can render it as required.
const value = documentService.getAssignedObject(
row as GroupInstance,
modelPath.map((x) => ({ ...x, index: 1 }))
);
// If there is a value
if (value) {
// Render something custom
}
To see how the steps above look like after putting them together, you can take a look at the helper-
files/task2/TableBodyCell.tsx file. In there you can actually find all the code needed for
customizing the TableBodyCell component. It consists of the following parts:
• Get the value for the column as described above.
• Check if there is a value and that the column type is DateType. If that is the case, then we can be
sure that the value is a JavaScript date.
• Check if the date is today or in the next 7 days.
• If the above condition is met, add a wrapping div with styles and return it.
• Return a fallback for the default.
Copy TableBodyCell.tsx into the client/src/modules/contact/components/HighlightedDateOverview
folder to use it. There are two to-do tasks in the copied file regarding a separate component to
handle the styling of the cell.
Your task:
• Create a new React component called HighlightedDateCell in
client/src/modules/contact/components/HighlightedDateOverview.
• This component will return the DefaultComponentMap.TableBodyCell wrapped in a div.
• The wrapping div will include a class to change the font color and weight.
◦ Create your own class or use -u-text-red,-u-text-orange and -u-font-semibold from the
Widgets utility classes.
• The color can be passed as props, e.g. textColor="red" or textColor="orange".
• With this, you can finish the two to-do tasks in
client/src/modules/contact/components/HighlightedDateOverview/TableBodyCell.tsx.
▼ Click to see solution
File: client/src/modules/contact/components/HighlightedDateOverview/HighlightedDateCell.tsx
17

-- 17 of 20 --

import { ReactElement, PropsWithChildren } from "react";
import { DefaultComponentMap, type TableBodyCell } from
"@com.mgmtp.a12.overviewengine/overviewengine-core";
interface HighlightedDateCellProps extends PropsWithChildren<TableBodyCell.Props> {
textColor: "red" | "orange" | "green";
}
export default function HighlightedDateCell({ textColor, ...tableBodyCellProps }:
HighlightedDateCellProps): ReactElement {
return (
<div className={`-u-text-${textColor} -u-font-semibold`}>
<DefaultComponentMap.TableBodyCell {...tableBodyCellProps} />
</div>
);
}
File: client/src/modules/contact/components/HighlightedDateOverview/TableBodyCell.tsx
//...
import HighlightedDateCell from "./HighlightedDateCell";
//...
export default function TableBodyCell(props:
PropsWithChildren<TableBodyCell.Props>): ReactElement {
//...
if (value && field.type === "Field" && field.fieldType.type === "DateType") {
const { isBirthdayToday, isBirthdayNextWeek } =
isBirthdayTodayOrNextWeek(value as Date);
if (isBirthdayToday) {
return <HighlightedDateCell {...props} textColor="red" />;
} else if (isBirthdayNextWeek) {
return <HighlightedDateCell {...props} textColor="orange" />;
}
}
//...
}
Now back in the HighlightedDateOverview/index.tsx file, you can replace your "Hello World" span
with the custom component that we are providing:
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
//...
import TableBodyCell from "./TableBodyCell";
18

-- 18 of 20 --

export default function HighlightedDateOverview(props: View): ReactElement {
//...
const componentMap: ComponentMap = {
//...
TableBodyCell(tableBodyCellProps) {
const { columnModel } = tableBodyCellProps;
if (columnModel.id === "column-highlight-date" && isHighlighted) {
return <TableBodyCell {...tableBodyCellProps} />;
}
return <DefaultComponentMap.TableBodyCell {...tableBodyCellProps} />;
}
};
//...
}
With this, the highlighting of the dates of birth should work as expected. However, we can still
improve the performance of our application as currently we are re-rendering the overview even
when isHighlighted is not changed.
To optimize this, we can utilize memoization, which is an optimization technique that caches the
result of a function instead of recalculating it unnecessarily. In React, this can be done with the
useMemo hook.
To implement this, we can wrap our componentMap in this hook, with a dependency array consisting
of the isHighlighted flag. This will ensure that we do not make any unnecessary renders when
isHighlighted is not changed.
Since React components should not be nested, we will separate the componentMap out of the
HighlightedDateOverview component into its own function and memoize the resulting object. This
will look as follows:
File: client/src/modules/contact/components/HighlightedDateOverview/index.tsx
import { ReactElement, useState, useContext, useMemo, Dispatch, SetStateAction } from
"react";
//...
export default function HighlightedDateOverview(props: View): ReactElement {
//...
const componentMap: ComponentMap = useMemo(
() => createComponentMap(isHighlighted, setIsHighlighted),
[isHighlighted, setIsHighlighted]
);
//...
}
function createComponentMap(
isHighlighted: boolean,
setIsHighlighted: Dispatch<SetStateAction<boolean>>
): ComponentMap {
19

-- 19 of 20 --

return {
...DefaultComponentMap,
Heading(headingProps) {
return <Heading {...headingProps} isHighlighted={isHighlighted}
setIsHighlighted={setIsHighlighted} />;
},
TableBodyCell(tableBodyCellProps) {
const { columnModel } = tableBodyCellProps;
if (columnModel.id === "column-highlight-date" && isHighlighted) {
return <TableBodyCell {...tableBodyCellProps} />;
}
return <DefaultComponentMap.TableBodyCell {...tableBodyCellProps} />;
}
};
}
You can try out the overview with and without this change to see the impact of the memoization,
particularly when clicking a row in the overview.
With this, we have fulfilled all the acceptance criteria that we have defined at the start. Assuming
today’s date is July 1st, the final application for this task should now behave as follows:
► https://vimeo.com/1097950906 (Vimeo video)
Conclusion
In this task, you have seen how custom features can be added to the A12 overview and gained some
initial experience working with the Overview Engine API.
This is of course also possible for other A12 libraries, such as the Form Engine. You can explore this
in Tutorials > Frontend > Form Customization, where the birthday reminder will be extended to the
contact form.
If you got stuck at any point, you can check out 2025.06-ext5/frontend/task-2-end to see how your
code differs from the solution.
« Task 1: Application Frame Task 3: Form Customization »
20

-- 20 of 20 --

