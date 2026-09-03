# overall dev_tutorial_frontend_unit_testing

Task 6 - Unit Testing
WARNING This tutorial refers to an older version of A12 (2025.06-ext5). An updated
version is currently in progress and will be available as soon as possible.
NOTE This tutorial uses A12 version 2025.06-ext5 and is based on the Project Template
version 202506.5.1.
Prerequisites
IMPORTANT
If you are new to the development tutorials, make sure to first go through
Tutorials > General Information and Tutorials > Frontend > Introduction
before continuing here.
IMPORTANT
We are using Vitest as the testing tool for this tutorial. If you are new to
Vitest or are not familiar with it, please take a moment to read through the
official Vitest documentation before continuing.
You can check out the tag 2025.06-ext5/frontend/task-6-start to follow along this tutorial.
If you get stuck at any point, you can check out the tag 2025.06-ext5/frontend/task-6-end to see
how your code differs from the solution.
Use-Case
In Tutorials > Frontend > Application Frame, we enhanced our CRM application by customizing the
application frame with a logo, footer, and localized title. The footer now includes navigation links to
Help and FAQ pages, improving the overall application structure.
Additionally, in Tutorials > Frontend > Overview Customization, we extended the customer
functionality by implementing a birthday highlighting feature for our contact management page.
Beyond component testing, understanding how to test A12’s core functionality is crucial for
building robust applications. In Tutorials > Frontend > Application Frame > Modules, we learned
about modules, and when working with frontend applications, we should also understand data
reducers. These are both key concepts that manage how features are loaded and how data flows
through your application’s state management system. These components also require thorough
testing to ensure your application behaves correctly under different user scenarios and data states,
making them essential areas to cover in our testing tutorial.
Given the importance of unit testing to ensure code quality and reliability, we will now extend our
application’s development workflow to include thorough testing coverage for these key
components.
In this tutorial, we will write unit tests for the footer, some components in the Contact module, and
core A12 functionality, including middleware and data reducers. To achieve this, we will set up
1

-- 1 of 30 --

Vitest as our testing framework, write comprehensive test cases covering both UI components and
business logic, and finally analyze our test coverage percentage to ensure we have adequate testing
in place.
End Result
By the end of this task, you will have learned:
• How to set up a test environment that works with A12 applications.
• How to write comprehensive and maintainable test cases for your components.
• How to generate and analyze test coverage reports.
At the end, our project should be covered by unit tests and achieve a code coverage of at least 80%
for code branches and functions covered for the targeted components within the tutorial.
Step-by-Step Instructions
Before we begin, let us outline why we are choosing Vitest as the testing tool for our frontend
environment:
Vitest is a modern testing framework built on top of Vite, but it also works perfectly fine even if
your project is not using Vite. Vitest can still be integrated smoothly, offering a consistent test
environment without the need for complex configuration.
For background reading, see Vitest > Guide and Vitest > Features.
With the release of A12 version 2025.06, many of the core packages were migrated to the new ESM
(ECMAScript Module) syntax structure along with their internal dependencies. Traditional testing
tools such as Jest still struggle with ESM support, often requiring experimental flags or complicated
setups. In contrast, Vitest was designed with ESM as a first-class concern, making it especially
suitable for this transition.
Beyond compatibility, Vitest provides excellent performance, leveraging modern build techniques
to deliver very fast test runs, instant re-runs, and efficient developer feedback loops.
It is also highly compatible with Jest, so existing Jest-style test cases can often be reused with little
or no modification, making the migration process straightforward and the learning curve gentle for
developers already familiar with Jest.
At the same time, Vitest’s ecosystem is still newer and not yet as broad as Jest’s. This is something
you should be aware of when choosing it as the main testing tool for your project. Please consider
2

-- 2 of 30 --

the project timeline and scale carefully to ensure you are selecting the most suitable tool.
By understanding these points, we can see why Vitest is suitable for our frontend testing tutorial.
Already Provided Test Utilities
The client/src/tests folder contains one setup file setup.ts that provides the basic test setup and
includes mocks for browser features needed by A12 components.
In the same folder, there is also a utils.tsx file that provides some utility functions for our tests,
such as rendering components with the necessary context providers.
Setting up the Test Environment
Folder Structure
For this tutorial, to keep your tests organized and easy to maintain, it is recommended to structure
the client/src/tests directory in the following way.
src/
├── components/
├── modules/
│ └── ...
└── tests/
├── components/
│ └── Footer.test.tsx
├── reducers/
│ └── TodoDataReducer.ts
├── constants.ts
├── setup.ts
└── utils.tsx
As you can see, the tests folder is separated into:
• The components folder, which contains our component test files.
• The reducers folder, which contains our reducer implementation and test file.
• Inside the tests folder, there are some files which include basic setup, utilities, and mock data
for our tests.
Dependencies
For our testing, we will use Vitest as the test framework along with React Testing Library for
rendering and interacting with React components. We will also use Vitest’s built-in mocking
capabilities to mock API calls and other dependencies.
For test coverage analysis, we will use @vitest/coverage-v8 which provides fast and accurate
coverage reports.
3

-- 3 of 30 --

We will start by updating our project’s dependencies. Therefore, the following libraries have been
added to our package.json file.
File: client/package.json
{
//...
"devDependencies": {
"vitest": "^4.0.3",
"jsdom": "^26.1.0",
"@testing-library/react": "^16.3.0",
"@testing-library/jest-dom": "^6.9.1",
"@testing-library/user-event": "^14.6.1",
"@vitest/coverage-v8": "^4.0.3",
"@vitest/ui": "^4.0.3",
//...
}
//...
}
Run npm install to install these dependencies.
Additionally, test scripts have been added to our package.json to run our tests more easily:
{
//...
"scripts": {
//...
"test": "vitest",
"test:ui": "vitest --ui",
"test:coverage": "vitest run src/tests/components/ --coverage"
}
//...
}
These scripts provide different ways to run your tests:
• npm run test: Runs tests in watch mode for development.
• npm run test:ui: Opens Vitest’s UI interface for interactive testing.
• npm run test:coverage: Generates test coverage reports by running only component tests to
focus on the HighlightedDateOverview module within the scope of this tutorial, avoiding
coverage dilution from unrelated tests.
Configuration
Next, a Vitest configuration file has been added to set up our test environment properly.
File: client/vitest.config.ts
4

-- 4 of 30 --

import { defineConfig } from "vitest/config";
export default defineConfig({
test: {
environment: "jsdom",
globals: true,
clearMocks: true,
setupFiles: ["./src/tests/setup.ts"],
include: ["src/tests/**/*.test.{ts,tsx}"],
coverage: {
reporter: ["text"],
include: ["src/modules/contact/components/HighlightedDateOverview/**"]
}
}
});
WARNING
We will only generate coverage reports for the HighlightedDateOverview folder
in the scope of this tutorial. That is why we specify the include option in the
coverage configuration. In a real-world scenario, you would typically want to
include all relevant source files in your coverage reports.
TIP
To keep this tutorial simple, we configure the coverage reporter to use text mode only.
You may extend this configuration by adding additional reporter modes such as json
or html to generate different formats of coverage reports for your project needs.
Additionally, the TypeScript configuration has been updated to exclude test files from the
compilation process. This ensures that Vitest can run tests independently with its own TypeScript
processing, while keeping test files out of the production build where they don’t belong.
File: client/tsconfig.json
{
"compilerOptions": {
// ... other options
},
"include": [
"./src/**/*"
],
"exclude": [
"./src/tests"
]
}
To ensure that your IDE (for example IntelliJ IDEA) provides proper autocomplete for Vitest globals
and jest-dom matchers in test files, a separate tsconfig.test.json has been created that extends the
base tsconfig.json. This configuration includes the src/tests directory and references the Vitest
and jest-dom types.
5

-- 5 of 30 --

File: client/tsconfig.test.json
{
"extends": "./tsconfig.json",
"compilerOptions": {
"types": ["vitest/globals", "@testing-library/jest-dom"]
},
"include": [
"./src/tests"
]
}
Then reference it from your vitest.config.ts by adding the typecheck.tsconfig option:
File: client/vitest.config.ts
export default defineConfig({
test: {
//... existing options
typecheck: {
tsconfig: "./tsconfig.test.json"
}
}
});
Implementing Test Files
Footer
A test file for the Footer component has already been created at
client/src/tests/components/Footer.test.tsx. It contains our first test case, which checks for the
presence of the "Help" and "FAQ" links in the footer component:
File: client/src/tests/components/Footer.test.tsx
describe("Footer", () => {
it("renders the footer with links", () => {
renderWithProviders(<Footer />); ①
expect(screen.getByText("Help")).toBeInTheDocument(); ②
expect(screen.getByText("FAQ")).toBeInTheDocument(); ②
});
});
① Our custom render function that wraps the component with necessary providers.
② Assertions to check if the links are present in the document.
6

-- 6 of 30 --

This test follows a concise approach that directly renders the Footer component and verifies the
presence of the "Help" and "FAQ" texts. However, to properly render the Footer component, we
must wrap it with the necessary context providers required by A12 components. This is
accomplished using the renderWithProviders function from our utils.tsx file. Let’s examine the
implementation in the utils file:
File: client/src/tests/utils.tsx
//... other imports
/** Shared Redux store */
const reducer = (state = {}) => state;
const store = createStore(reducer);
/** Mock dispatch */
const mockDispatch = vi.fn();
/** Replace store's dispatch with mock dispatch */
store.dispatch = mockDispatch;
/** Shared localizer context */
const locale = { language: "en", country: "US" };
const dataFormats = defaultDataFormats(locale);
const conversion = defaultValueConversion(dataFormats);
const localizer = defaultLocalizerFactory({
locale,
conversion,
dataFormats,
translationSource: DEFAULT_TRANSLATIONS
});
export function renderWithProviders(ui: React.ReactElement, options = {}) {
const result = render(
<Provider store={store}>
<LocalizerContext.Provider value={{ locale, conversion, dataFormats,
localizer }}>
<ThemeProvider theme={flatTheme}>
<SizeContext.Provider value={{ currentSize: "lg"
}}>{ui}</SizeContext.Provider>
</ThemeProvider>
</LocalizerContext.Provider>
</Provider>,
options
);
return {
...result,
mockDispatch
};
7

-- 7 of 30 --

}
This utility file sets up a shared Redux store and localizer context that can be reused across
multiple tests. The renderWithProviders function wraps the provided UI component with the
necessary providers, ensuring that it has access to the Redux store, localization, theming, and size
context.
Now we will enhance our testing by adding another test case to verify the Footer component’s
interactive functionality. This test will ensure that the links are properly rendered as clickable
elements and that the correct Redux actions are dispatched when users interact with them.
Your task:
• Inside the Footer.test.tsx add another test case 'should verify footer functionalities'.
• Import userEvent from @testing-library/user-event and create a UserEvent instance by calling
userEvent.setup().
• Extract mockDispatch from the renderWithProviders return value.
• Get the footer links using screen.getByText().
• Verify that the links render as <a> tags using .tagName.toLowerCase().
• For each of the footer links, do the following:
◦ Use await user.click() to simulate clicking the link. The await is important because user
events are asynchronous.
◦ Assert that mockDispatch was called with the correct Redux action type and payload for the
respective link.
• Run npm run test to verify all tests are passing.
▼ Click to see solution
File: client/src/tests/components/Footer.test.tsx
//...
import userEvent from "@testing-library/user-event";
//...
it("should verify footer functionalities", async () => {
const user = userEvent.setup();
const { mockDispatch } = renderWithProviders(<Footer />);
const helpLink = screen.getByText("Help");
const faqLink = screen.getByText("FAQ");
// Verify that the links are clickable elements (should render as anchor
tags)
expect(helpLink.tagName.toLowerCase()).toBe("a");
expect(faqLink.tagName.toLowerCase()).toBe("a");
// Test clicking Help link and verify Redux action is dispatched
8

-- 8 of 30 --

await user.click(helpLink);
expect(mockDispatch).toHaveBeenCalledWith({
type: "Application/START_MAIN_ACTIVITY_REQUESTED",
payload: {
descriptor: {
module: "Help"
}
}
});
// Test clicking FAQ link and verify Redux action is dispatched
await user.click(faqLink);
expect(mockDispatch).toHaveBeenCalledWith({
type: "Application/START_MAIN_ACTIVITY_REQUESTED",
payload: {
descriptor: {
module: "Help",
page: "faq"
}
}
});
});
Table Cell
Now that we have successfully added tests for the Footer component, let’s proceed to add tests for
other components inside the HighlightedDateOverview folder.
In Tutorials > Frontend > Overview Customization > Highlighting the Date, we implemented logic to
highlight contact birthdays when they occur today or within a week by customizing a table cell.
Now we need to add tests to verify that this highlighting functionality works as expected.
Let’s first review the HighlightedDateCell component to understand how it works and what we
need to test.
File: client/src/modules/contact/components/HighlightedDateOverview/HighlightedDateCell.tsx
import type { ReactElement, PropsWithChildren } from "react";
import { DefaultComponentMap, type TableBodyCell } from
"@com.mgmtp.a12.overviewengine/overviewengine-core";
interface HighlightedDateCellProps extends PropsWithChildren<TableBodyCell.Props> {
textColor: "red" | "orange" | "green";
}
export default function HighlightedDateCell({
textColor,
...tableBodyCellProps
}: HighlightedDateCellProps): ReactElement {
9

-- 9 of 30 --

return (
<div className={`-u-text-${textColor} -u-font-semibold`}>
<DefaultComponentMap.TableBodyCell {...tableBodyCellProps} />
</div>
);
}
The HighlightedDateCell component is a simple wrapper around the TableBodyCell component that
adds a colored text style based on the textColor prop. The text color can be "red", "orange", or
"green", indicating different levels of urgency for the contact’s birthday.
The main thing we need to be aware of here is that it imports TableBodyCell from
@com.mgmtp.a12.overviewengine/overviewengine-core package. This means that in order to properly
render the HighlightedDateCell component in our tests, we need to mock the
@com.mgmtp.a12.overviewengine/overviewengine-core package to provide a mock implementation of
the TableBodyCell component.
Testing these components in isolation from the complete A12 component tree allows us to focus
specifically on their core logic and behavior. By mocking them as simple div elements, we create
predictable outputs that remain independent of external dependencies or side effects. This
approach ensures clean, isolated unit tests that verify only the functionality we intend to test.
Let’s do that in our setup.ts file. We will use Vitest’s mocking capabilities to mock the module.
Inside the setup.ts file, add the following code:
File: client/src/tests/setup.ts
//... other imports
import React from "react";
//... existing code
vi.mock("@com.mgmtp.a12.overviewengine/overviewengine-core", async (importOriginal) =>
{
const actual = await importOriginal<typeof
import("@com.mgmtp.a12.overviewengine/overviewengine-core")>();
return {
...actual,
DefaultComponentMap: {
TableBodyCell: vi.fn(({ children }) =>
React.createElement("div", { "data-testid": "default-table-body-cell"
}, children)
)
}
};
});
This mock implementation provides simple div elements with data-testid attributes for the Heading
and TableBodyCell components. This allows us to verify that these components are being rendered
correctly in our tests.
10

-- 10 of 30 --

Now we can proceed to write tests for the HighlightedDateCell component. You should create a new
test file named HighlightedDateCell.test.tsx inside the client/src/tests/components folder and add
the following code:
import { describe } from "vitest";
import type { TableBodyCell } from "@com.mgmtp.a12.overviewengine/overviewengine-
core";
describe("HighlightedDateCell", () => {
//Mock Column Data
const mockColumnModel = {
id: "dateOfBirth",
elementRef: "PersonalData.DateOfBirth",
width: 150,
visible: true,
sortable: true,
editable: false
};
//Mock Row data
const mockRow = {
id: "test-row-1",
data: {
dateOfBirth: new Date("1990-01-15")
}
};
const mockProps: Pick<TableBodyCell.Props, "columnModel" | "row"> = {
columnModel: mockColumnModel,
row: mockRow
};
});
This sets up the basic structure for our tests. We define a mock column model and a mock row to
simulate the data that would be passed to the HighlightedDateCell component.
Now we can add our test cases to verify that the HighlightedDateCell component styling renders
correctly with different text colors. Make sure you import the HighlightedDateCell component as
well as render and screen from @testing-library/react at the top of your test file. Then add the
following test cases inside the describe block:
it("renders if birthday is today", () => {
renderWithProviders(
<HighlightedDateCell {...mockProps} textColor="red">
Birthday Today
</HighlightedDateCell>
); ①
11

-- 11 of 30 --

const container = screen.getByText("Birthday Today").parentElement; ②
expect(container).toHaveClass("-u-text-red"); ②
expect(container).toHaveClass("-u-font-semibold"); ②
});
① Render the HighlightedDateCell component with our mock props and "red" text color.
② Verify that the container has the correct classes for red text color and semibold font.
TIP
To know which utility classes we can use for modifying the component style, such as
-u-text-red -u-text-orange -u-text-green -u-font-semibold, you can refer to the
Widgets Utility Classes documentation. These utility classes are part of the A12
framework and provide a convenient way to apply common styles to components
without writing custom CSS.
There is also a case in which the birthday is within a week, we should test that as well. The
implementation is similar to the previous test case, but we will use "orange" as the text color and
also modify the display text.
Your task:
• Inside the HighlightedDateCell.test.tsx add another test case 'renders if birthday is within a
week'.
• Render the HighlightedDateCell component with our mock props and "orange" text color.
• Use screen.getByText() to get the text element and then access its parent element.
• Verify that the container has the correct classes for orange text color and semibold font by using
toHaveClass().
▼ Click to see solution
File: client/src/tests/components/HighlightedDateCell.test.tsx
it("renders if birthday is next week", () => {
renderWithProviders(
<HighlightedDateCell {...mockProps} textColor="orange">
Birthday Next Week
</HighlightedDateCell>
);
const container = screen.getByText("Birthday Next Week").parentElement;
expect(container).toHaveClass("-u-text-orange");
expect(container).toHaveClass("-u-font-semibold");
});
TIP
Instead of running all tests at once, you can run individual test files by using npm run
test HighlightedDateCell or npm run
test — src/tests/components/HighlightedDateCell.test.tsx.
12

-- 12 of 30 --

Heading
In our actual implementation, to display the birthday highlighting feature, we also customized the
page heading to include a toggle button that enables users to switch this highlighting feature on or
off. This was accomplished by creating a custom Heading component, as detailed in Tutorials >
Frontend > Overview Customization > Adding A Button To The Overview Header.
Let’s first examine the Heading component to understand its structure and functionality so we can
add tests for this component as well.
File: client/src/modules/contact/components/HighlightedDateOverview/Heading.tsx
import type { ReactElement, PropsWithChildren, Dispatch, SetStateAction } from
"react";
import { DefaultComponentMap, type Heading } from
"@com.mgmtp.a12.overviewengine/overviewengine-core";
import { Button, Icon } from "@com.mgmtp.a12.widgets/widgets-core";
interface HeadingProps extends PropsWithChildren<Heading.PropsType> {
isHighlighted: boolean;
setIsHighlighted: Dispatch<SetStateAction<boolean>>;
}
export default function Heading({ isHighlighted, setIsHighlighted, ...headingProps }:
HeadingProps): ReactElement {
const handleClick = () => {
setIsHighlighted((prevIsHighlighted) => !prevIsHighlighted);
};
return (
<>
<DefaultComponentMap.Heading {...headingProps} />
<div className="-u-flex -u-items-center -u-justify-end -u-padding-x-xl -u
-margin-y-sm">
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
The Heading component is a wrapper around the default Heading component from the A12
OverviewEngine. It adds a button that toggles the isHighlighted state when clicked. The button’s
icon changes based on the current state of isHighlighted.
13

-- 13 of 30 --

To properly render the Heading component in our tests, we need to ensure that the
DefaultComponentMap.Heading from @com.mgmtp.a12.overviewengine/overviewengine-core package is
mocked correctly, which we need to do in our setup.ts file.
Your task:
• In the existing setup.ts, a mock for the Overview Engine’s DefaultComponentMap has already been
added.
• Extend this mock by adding a Heading component in the same way that the TableBodyCell
component was defined, assigning it the attribute data-testid: 'default-heading'.
▼ Click to see solution
File: client/src/tests/setup.ts
//... existing code
vi.mock("@com.mgmtp.a12.overviewengine/overviewengine-core", async (importOriginal)
=> {
const actual = await importOriginal<typeof
import("@com.mgmtp.a12.overviewengine/overviewengine-core")>();
return {
...actual,
DefaultComponentMap: {
TableBodyCell: vi.fn(({ children }) => React.createElement("div", {
"data-testid": "default-table-body-cell" }, children)),
Heading: vi.fn(({ children }) => React.createElement("div", { "data-
testid": "default-heading" }, children))
}
};
});
Now we can proceed to write tests for the Heading component. You should create a new test file
named Heading.test.tsx inside the client/src/tests/components folder and add the following code:
File: client/src/tests/components/Heading.test.tsx
import { describe, vi } from "vitest";
import Heading from
"../../modules/contact/components/HighlightedDateOverview/Heading";
describe("Heading", () => {
const mockSetIsHighlighted = vi.fn();
const mockHeadingProps: React.ComponentProps<typeof Heading> = {
title: "Contact Overview",
subtitle: "Manage your contacts",
buttons: [],
isHighlighted: false,
setIsHighlighted: mockSetIsHighlighted
14

-- 14 of 30 --

};
});
This sets up the basic structure for our tests. We define a mock heading props to simulate the data
that would be passed to the Heading component.
Now we can add our test cases to verify that the Heading component renders correctly and that the
toggle button functions as expected. First, we will verify that the Heading component renders the
button correctly.
Your task:
• Inside the Heading.test.tsx add a test case 'renders heading with toggle button'.
• Render the Heading component with our mock props.
• Use screen.getByRole('button') to get the button element.
• Verify that the button is in the document using toBeInTheDocument().
▼ Click to see solution
File: client/src/tests/components/Heading.test.tsx
describe("Heading", () => {
//... existing code
it("renders heading with toggle button", () => {
renderWithProviders(<Heading {...mockHeadingProps} />);
// Verify that the custom button is present
const button = screen.getByRole("button");
expect(button).toBeInTheDocument();
});
});
TIP
In our Heading component, we have added a prop primary to the Button component.
Instead of toBeInTheDocument(), we could also use toHaveClass('button—primary') to
verify that the button has the primary styling applied. This would ensure that our
button not only exists in the document but also has the correct visual appearance as
intended.
WARNING
You might see a warning about 'non-boolean attribute primary' when
running the test. This is coming from the Button component which we could
mock if needed. However, for the purpose of this tutorial, we can ignore this
warning as it does not affect the functionality we are testing.
Next, we should test the button’s icon to ensure it displays correctly based on the isHighlighted
state.
Your task:
• Inside the Heading.test.tsx add another test case 'renders icon when button is toggled'.
15

-- 15 of 30 --

• Render the Heading component with our mock props and isHighlighted set to true.
• Use screen.getByText('highlight_off') to get the icon element.
• Verify that the icon is in the document using toBeInTheDocument().
▼ Click to see solution
File: client/src/tests/components/Heading.test.tsx
describe("Heading", () => {
//... existing code
it("renders icon when button is toggled", () => {
renderWithProviders(<Heading {...mockHeadingProps} isHighlighted={true} />);
// Verify highlight_off icon text is present
const highlightOffIcon = screen.getByText("highlight_off");
expect(highlightOffIcon).toBeInTheDocument();
});
});
You could get inspired by the above task and add another test case to check when the isHighlighted
is false and verify that the icon should be highlight.
TIP For other types of icons, you can refer to the Material Icons documentation to see the
full list of available icons.
Next, we should test the button container styling to ensure it has the correct classes applied.
Your task:
• Inside the Heading.test.tsx add another test case 'renders button with correct styling'.
• Render the Heading component with our mock props.
• Get the button’s parent container by accessing .parentElement on the button element you
already know how to retrieve from the previous test.
• Verify that the button container has the correct styling classes using toHaveClass.
▼ Click to see solution
File: client/src/tests/components/Heading.test.tsx
describe("Heading", () => {
//... existing code
it("renders button with correct styling", () => {
renderWithProviders(<Heading {...mockHeadingProps} />);
// Find the button container div
const buttonContainer = screen.getByRole("button").parentElement;
// Verify correct styling classes are applied
expect(buttonContainer).toHaveClass("-u-flex");
16

-- 16 of 30 --

expect(buttonContainer).toHaveClass("-u-items-center");
expect(buttonContainer).toHaveClass("-u-justify-end");
expect(buttonContainer).toHaveClass("-u-padding-x-xl");
expect(buttonContainer).toHaveClass("-u-margin-y-sm");
});
});
Finally, after verifying the rendering and styling of the button, we should test the button’s
functionality to ensure it works as expected. We will simulate a click event on the button and verify
that the setIsHighlighted function is called with the correct argument.
Your task:
• Inside the Heading.test.tsx add another test case 'should verify heading functionalities'.
• Import userEvent from @testing-library/user-event.
• Render the Heading component with our mock props and isHighlighted set to false.
• Use screen.getByRole('button') to get the button element.
• Use await user.click() to simulate clicking the button.
• Assert that mockSetIsHighlighted was called with a function argument using
expect.any(Function).
• Extract the toggle function from mockSetIsHighlighted.mock.calls and verify its logic by testing
that it toggles false to true and true to false.
▼ Click to see solution
File: client/src/tests/components/Heading.test.tsx
//...other imports
import userEvent from "@testing-library/user-event";
describe("Heading", () => {
//... eixsting code
it("should verify heading functionalities", async () => {
const user = userEvent.setup();
renderWithProviders(<Heading {...mockHeadingProps} />);
// Test initial state
expect(screen.getByText("highlight")).toBeInTheDocument();
expect(screen.queryByText("highlight_off")).not.toBeInTheDocument();
// Simulate user clicking the button
const button = screen.getByRole("button");
await user.click(button);
// Verify the toggle function behavior
expect(mockSetIsHighlighted).toHaveBeenCalledWith(expect.any(Function)); ①
// Test the actual toggle function logic
17

-- 17 of 30 --

const toggleFunction = mockSetIsHighlighted.mock.calls[0][0]; ②
expect(toggleFunction(false)).toBe(true);
expect(toggleFunction(true)).toBe(false);
});
});
① Verify that the mock function was called with a function argument.
② Extract the actual toggle function from the mock calls and test its logic.
Custom Middleware
Having successfully implemented tests for the Heading and HighlightedDateCell components, we
should now extend our testing scope to cover critical A12-related Redux implementations beyond
just UI component testing.
While we previously discussed Modules in Tutorials > Frontend > Application Frame > Modules, we
will now explore this topic in greater depth. In our client/src/modules/index.ts file, we have two
key middleware functions:
• registerModulesOnSetModelGraphMiddleware - Creates middleware to register the full module set
as soon as the model graph is loaded
• unregisterModulesOnLogoutMiddleware - Creates middleware to unregister modules when users
log out
These middleware functions are essential for managing the application’s state and ensuring that
modules are properly registered and unregistered based on user actions.
File: client/src/modules/index.ts
//... existing code
/**
* Initializes module registry on `setModelGraph` action.
*/
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
18

-- 18 of 30 --

return next(action);
});
/**
* On logout, unregisters all modules.
*/
export const unregisterModulesOnLogoutMiddleware =
StoreFactories.createMiddleware((api, next, action) => {
if (UaaActions.loggedOut.match(action)) {
// The logout action has to be processed first so that any existing activities
are removed first
const result = next(action);
const moduleIds = moduleRegistry.getAllModules().map(({ id }) => id);
moduleIds.forEach((id) => moduleRegistry.removeModuleById(id));
return result;
}
return next(action);
});
Before writing the middleware tests, we need to add additional mocks to our setup.ts file. The test
file will import the middleware functions directly from client/src/modules/index.ts. Since
modules/index.ts in turn imports from @com.mgmtp.a12.formengine/formengine-content-elements and
@com.mgmtp.a12.contentengine/contentengine-default-element-library, these packages must be
mocked to avoid dependency resolution failures in the jsdom test environment.
File: client/src/tests/setup.ts
//... existing code
import React from "react";
/** Mock A12 FormEngine content elements - avoids SVG import chain from widgets-core
*/
vi.mock("@com.mgmtp.a12.formengine/formengine-content-elements", () => ({
FormElementsLibrary: { modules: [] }
}));
/** Mock A12 ContentEngine default element library - avoids SVG import chain from
widgets-core */
vi.mock("@com.mgmtp.a12.contentengine/contentengine-default-element-library", () => ({
DefaultElementLibrary: { get: () => ({ modules: [] }) },
DefaultElementLibraryFactories: {
createModule: () => ({ id: "DefaultElementLibraryModule" })
}
}));
These mocks provide minimal implementations of the libraries that modules/index.ts depends on,
preventing the test environment from attempting to resolve their full dependency trees.
19

-- 19 of 30 --

We will now write comprehensive tests to verify the functionality of these middleware. You should
first create a new folder named "modules" inside the client/src/tests folder and then create a new
test file named Modules.test.tsx inside the client/src/tests/modules folder and add the following
code:
import { describe, beforeEach, vi, expect, it } from "vitest";
import type { MiddlewareAPI } from "redux";
import { ModuleRegistryProvider, ModelActions } from "@com.mgmtp.a12.client/client-
core";
import { UaaActions } from "@com.mgmtp.a12.uaa/uaa-authentication-client";
import {
getAllModules,
registerModulesOnSetModelGraphMiddleware,
unregisterModulesOnLogoutMiddleware
} from "../../modules";
import { testModules } from "../constants";
describe("Modules", () => {
const moduleRegistry = ModuleRegistryProvider.getInstance();
const api = {
getState: vi.fn(),
dispatch: vi.fn()
} as unknown as MiddlewareAPI;
const next = vi.fn((action) => action);
});
This sets up the basic structure for our tests. We import the middleware functions directly from
modules/index.ts, and inside the describe block we initialize the module registry and create a mock
API for testing.
Since we know that we will write tests for different cases of registering and unregistering modules,
it is a good practice to reset the module registry and mock functions before each test to ensure that
tests do not interfere with each other. We can do this by adding a beforeEach block inside the
describe block:
File: client/src/tests/modules/Modules.test.tsx
describe("Modules", () => {
//... existing code
beforeEach(() => {
// Clear all modules from registry
const moduleIds = moduleRegistry.getAllModules().map(({ id }) => id);
moduleIds.forEach((id) => moduleRegistry.removeModuleById(id));
});
});
20

-- 20 of 30 --

Now we can proceed with writing our test cases. We will begin by testing the module registration
middleware to ensure it properly loads modules when the model graph is established.
For this test, we will create a mock setModelGraph action and configure the necessary spies to
monitor the middleware behavior. The test will verify that modules are registered correctly in the
registry, that each module is added exactly once, and that the action continues through the
middleware chain without modification.
Your task:
• Inside Modules.test.tsx, add a test case 'should register modules when model graph is set and
continue with action'.
• Create a plain action object with type "MODEL_SET_MODEL_GRAPH".
• Call the local getAllModules() function and store the returned modules so they can be used as
the expected list in assertions.
• Use vi.spyOn to spy on ModelActions.setModelGraph.match and mock it to return true for the
action.
• Use vi.spyOn on the module registry’s addModule method to observe each registration.
• Execute registerModulesOnSetModelGraphMiddleware with the mocked API, next function, and
action.
• Verify that ModelActions.setModelGraph.match was called with the action.
• Verify the total call count of addModule matches the expected list length using
toHaveBeenCalledTimes, then verify that addModule was called with each module from the
expected list using toHaveBeenCalledWith.
• Verify that the registry now contains the full module list using toHaveLength on
moduleRegistry.getAllModules().
• Verify that the next function was called with the original action, ensuring the middleware
passes the action through the chain.
• Clean up the spies after the assertions so later tests see the original implementations.
▼ Click to see solution
File: client/src/tests/modules/Modules.test.tsx
//... imports are already defined at the top of the file
describe("Modules", () => {
//... existing code
it("should register modules when model graph is set and continue with action", ()
=> {
const action = {
type: "MODEL_SET_MODEL_GRAPH"
};
// Get expected modules and set up spies to monitor the middleware behavior
const modulesToRegister = getAllModules();
const matchSpy = vi.spyOn(ModelActions.setModelGraph,
21

-- 21 of 30 --

"match").mockReturnValue(true);
const addModuleSpy = vi.spyOn(moduleRegistry, "addModule");
// Execute the register modules middleware
registerModulesOnSetModelGraphMiddleware(api)(next)(action);
// Middleware should recognise the action and register every module exactly
once
expect(matchSpy).toHaveBeenCalledWith(action);
expect(addModuleSpy).toHaveBeenCalledTimes(modulesToRegister.length);
modulesToRegister.forEach((module) => {
expect(addModuleSpy).toHaveBeenCalledWith(module);
});
// Registry should hold the full list and the action should keep flowing
expect(moduleRegistry.getAllModules()).toHaveLength(modulesToRegister.length);
expect(next).toHaveBeenCalledWith(action);
// Clean up spies
matchSpy.mockRestore();
addModuleSpy.mockRestore();
});
});
Now that we have confirmed the modules register properly when the model graph is loaded, we
will move on to testing the unregister flow when a user logs out.
Your task:
• Inside the Modules.test.tsx add another test case 'should unregister modules after logout and
continue with action'.
• Import testModules from client/src/tests/constants.ts and pre-register them into the module
registry. Verify there are 4 modules.
• Create a logout action using UaaActions.loggedOut().
• Create a spy on UaaActions.loggedOut.match using vi.spyOn and mock its return value to true.
• Create a spy on moduleRegistry.removeModuleById method.
• Call the unregisterModulesOnLogoutMiddleware with the mock API, next function, and action.
• Verify that UaaActions.loggedOut.match was called with the action.
• Verify that the next function was called with the action.
• Assert that removeModuleById was called 4 times.
• Assert that the module registry is empty after the logout action.
▼ Click to see solution
File: client/src/tests/modules/Modules.test.tsx
22

-- 22 of 30 --

//... existing imports
import { testModules } from "../constants";
//... existing imports and setup are already defined at the top of the file
describe("Modules", () => {
//... existing code
it("should unregister modules after logout and continue with action", () => {
// Seed the registry with modules
testModules.forEach((module) => moduleRegistry.addModule(module));
expect(moduleRegistry.getAllModules().length).toBe(4);
// Create logout action
const action = UaaActions.loggedOut();
// Set up spies to monitor the middleware behavior
const matchSpy = vi.spyOn(UaaActions.loggedOut,
"match").mockReturnValue(true);
const removeModuleSpy = vi.spyOn(moduleRegistry, "removeModuleById");
// Execute the logout middleware
unregisterModulesOnLogoutMiddleware(api)(next)(action);
// Verify all modules were properly unregistered
expect(matchSpy).toHaveBeenCalledWith(action);
expect(next).toHaveBeenCalledWith(action);
expect(removeModuleSpy).toHaveBeenCalledTimes(4);
expect(moduleRegistry.getAllModules().length).toBe(0);
// Clean up spies
matchSpy.mockRestore();
removeModuleSpy.mockRestore();
});
});
We have successfully implemented test cases for both middleware functions. This is one of the most
important parts that we should cover when writing tests for A12 applications, as it ensures that our
application behaves correctly based on user actions and permissions.
Custom Activity Data Reducer
One of the most common use cases when working with A12 applications is implementing a custom
activity data reducer. In this final section on unit testing, you will learn how to test a custom
activity data reducer effectively.
TIP
Data Reducer is a pattern for managing and transforming application state in React by
defining pure functions that specify how the state should change in response to
dispatched actions. You can learn more about data reducers in A12 in Client > Data
Reducers.
23

-- 23 of 30 --

Many scenarios exist where implementing a custom activity data reducer is beneficial. For instance,
you might need to filter or transform data before displaying it in an activity, or combine data from
multiple sources into a single activity. For this tutorial, we will focus on a straightforward example
of a custom activity data reducer from the data reducers documentation: a to-do data reducer.
Let’s examine the implementation of this to-do reducer to understand its functionality and
determine what aspects require testing. The implementation can be found in the
client/src/tests/reducers/TodoDataReducer.ts file.
File: client/src/tests/reducers/TodoDataReducer.ts
import type { Activity, ActivityActions, ActivityReducers } from
"@com.mgmtp.a12.client/client-core";
/** Action type constant */
const ADD_TODO = "Tutorial/ADD_TODO";
/** The payload of the addTodo action. */
export interface AddTodoPayload extends ActivityActions.ActivityActionPayload {
readonly todo: string;
}
/** Action interface */
interface AddTodoAction {
type: typeof ADD_TODO;
payload: AddTodoPayload;
}
/** Todo list data structure to manage in a DataHolder */
export interface TodoList {
readonly todos: string[];
}
/** Action creator to add a new Todo item */
export const addTodo = (payload: AddTodoPayload): AddTodoAction => ({
type: ADD_TODO,
payload
});
/** Helper function to check if action is addTodo */
function isAddTodoAction(action: unknown): action is AddTodoAction {
return (action as AddTodoAction).type === ADD_TODO;
}
/** Helper function to assert the type of DataHolder */
export function isTodoDataHolder(dh: Activity.DataHolder): dh is
Activity.DataHolder<TodoList> {
return dh.descriptor.use === "todo-list";
}
24

-- 24 of 30 --

export const AddTodoDataReducer: ActivityReducers.DataReducer = {
reduce(dataHolders, action) {
return isAddTodoAction(action)
? dataHolders.map((dh: Activity.DataHolder) => {
return isTodoDataHolder(dh)
? {
...dh,
data: {
...dh.data,
todos: [...(dh.data?.todos ?? []),
action.payload.todo]
}
}
: dh;
})
: dataHolders;
}
};
Looking at the implementation, we can see that this reducer listens for the ADD_TODO action and adds
a new to-do item to the list of to-dos in the data holder. The reducer checks if the data holder is of
type "todo-list" using the isTodoDataHolder helper function before modifying its data.
With this understanding, we can get some ideas about what we need to test. Common scenarios to
test for this reducer include:
• Verifying that the action is defined correctly.
• Ensuring that the reducer only modifies matching data holders.
• Verifying the immutability of the state.
• Verifying action type matching.
• How the reducer handles empty or undefined data holders.
• Verifying descriptor and metadata integrity.
There will be more cases that you can come up with, but within this tutorial we will only cover
some of them. You should create a new test file named TodoDataReducer.test.tsx inside the
client/src/tests/reducers folder and add the following code:
import { describe, it, expect } from "vitest";
import type { Activity, ActivityActions } from "@com.mgmtp.a12.client/client-core";
import { addTodo, TodoList, AddTodoDataReducer } from "./TodoDataReducer";
describe("TodoDataReducer", () => {
// Setup mock data holders for testing
const mockDataHolder: Activity.DataHolder = {
descriptor: {
25

-- 25 of 30 --

use: "todo-list",
id: "todo-holder-1"
},
data: {
todos: ["Learn A12", "Write tests"]
},
slices: {},
dirty: false,
loadingState: "loaded",
savingState: "not_saved"
};
const nonTodoDataHolder: Activity.DataHolder = {
descriptor: { use: "user-profile", id: "user-1" },
data: { name: "John" },
slices: {},
dirty: false,
loadingState: "loaded",
savingState: "not_saved"
};
});
This sets up the basic structure for our tests by defining mock data holders for testing. We have a
mockDataHolder of type "todo-list" and a nonTodoDataHolder of a different type which we will use for
our test scenarios. Now that everything has been set up, we can proceed with writing our test cases.
Your task:
• Inside the TodoDataReducer.test.tsx add a test case 'should verify to-do reducer
functionalities'.
• Create an addTodo action and verify action.type and action.payload properties using toBe().
• Create a mixed array of both mockDataHolder and nonTodoDataHolder, call
AddTodoDataReducer.reduce() with the action, and verify the result length using toHaveLength.
Find the updated todo data holder via .find(), check it is defined with toBeDefined(), and verify
its todos array content using toEqual().
• Find the unchanged data holder in the result by its descriptor.use and verify it is the exact same
object reference as the original using toBe().
• Verify immutability by checking that the original mockDataHolder.data still contains its original
todos using toEqual().
• Create an action with an unrelated type, pass it along with the data holders to
AddTodoDataReducer.reduce(), and verify that the returned array is the same reference as the
original using toBe().
▼ Click to see solution
1. Create the test case inside the describe block with the specified name:
File: client/src/tests/reducers/TodoDataReducer.test.tsx
26

-- 26 of 30 --

it("should verify to-do reducer functionalities", () => {
// Test implementation goes here
});
2. Start by creating and verifying the addTodo action with proper A12 payload structure:
// Test 1: Verify action creation with required A12 activity payload
const action = addTodo({
activityId: "test-activity", // Required by A12 ActivityActionPayload
todo: "Create tutorial"
});
expect(action.type).toBe("Tutorial/ADD_TODO");
expect(action.payload.activityId).toBe("test-activity");
expect(action.payload.todo).toBe("Create tutorial");
3. Test the reducer with mixed data holders to verify selective processing:
// Test 2: Verify reducer only processes matching data holders
const mixedDataHolders = [mockDataHolder, nonTodoDataHolder];
const result = AddTodoDataReducer.reduce(mixedDataHolders, action) as
Activity.DataHolder[];
// Verify no data holders were added or removed
expect(result).toHaveLength(2);
// Find and verify the todo data holder was updated
const updatedTodoHolder = result.find((dh) => dh.descriptor.use === "todo-list");
expect(updatedTodoHolder).toBeDefined();
expect((updatedTodoHolder?.data as TodoList).todos).toEqual(["Learn A12", "Write
tests", "Create tutorial"]);
4. Verify object reference equality for unchanged data holders:
// Find and verify the non-todo data holder remained unchanged
const unchangedUserHolder = result.find((dh) => dh.descriptor.use === "user-
profile");
expect(unchangedUserHolder).toBe(nonTodoDataHolder); // Same object reference
5. Test immutability by checking original data remains unchanged:
// Test 4: Verify immutability
expect((mockDataHolder.data as TodoList).todos).toEqual(["Learn A12", "Write
tests"]);
27

-- 27 of 30 --

6. Test action filtering with unrelated actions to ensure performance optimization:
// Test 5: Verify action type filtering
const unrelatedAction = {
type: "UNRELATED_ACTION",
payload: { activityId: "test-activity" }
} as ActivityActions.DataReducerAction;
const originalDataHolders = [mockDataHolder];
const unchangedResult = AddTodoDataReducer.reduce(originalDataHolders,
unrelatedAction);
expect(unchangedResult).toBe(originalDataHolders); // Same reference returned
With that, we have successfully completed our comprehensive testing implementation. This final
section demonstrates several important concepts when working with A12 Data Reducers.
When implementing custom data reducers in A12 applications, there are several key considerations
to keep in mind. Your actions must extend ActivityActions.ActivityActionPayload to ensure proper
integration with A12’s activity system. The reducer should return the same object reference for
non-matching actions - this prevents breaking A12’s internal reducer chain and maintains optimal
performance.
Type guards like isTodoDataHolder() are essential for safely processing only the relevant data
holders while leaving others untouched. This selective processing ensures your reducer doesn’t
interfere with unrelated data.
For testing data reducers, you should cover both successful scenarios (when actions match and data
is processed correctly) and edge cases (empty arrays, non-matching data holders, and unrelated
actions). Testing immutability is crucial - use expect().toEqual() to verify data content changes and
expect().toBe() to confirm object reference equality for unchanged items.
Pay particular attention to preserving A12 metadata during processing. Properties like descriptor,
loadingState, savingState, dirty, and slices should remain intact after your reducer processes the
data.
The key principles for A12 Data Reducers are selectivity (process only what matches), immutability
(never modify existing objects), and performance (avoid unnecessary processing). Following these
patterns ensures your reducers integrate smoothly with the A12 framework while maintaining the
reliability needed for productive applications.
Running Tests and Analyzing Coverage
Now that we have written tests for different parts of our application code, we can run our tests and
analyze the coverage.
Instead of running each test file individually, we can use the test scripts we added to our
package.json to run all tests at once.
Vitest also provides a UI interface that allows us to run and monitor our tests interactively. To start
the Vitest UI, run the following command: npm run test:ui
28

-- 28 of 30 --

This will open the Vitest UI in your default web browser. From here, you can see the list of test files,
their status (passed/failed), and detailed information about each test case.
You can click on individual test files to see the test cases and their results. You can also re-run tests,
filter tests, and view error messages for failed tests.
To generate a test coverage report, you can run the following command: npm run test:coverage
This will run all tests and generate a coverage report. You can then view the coverage table report
directly in the terminal.
The coverage report provides detailed information about the coverage of your code, including the
percentage of lines, functions, branches, and statements that are covered by tests.
Based on the coverage report, we achieved 100% coverage across all four criteria for the
components (Heading.tsx and HighlightedDateCell.tsx) tested within the HighlightedDateOverview
module, demonstrating thorough and comprehensive testing practices.
29

-- 29 of 30 --

Conclusion
Having completed this task, you have learned how to set up a unit testing environment and write
tests for your components with test coverage analysis.
If you got stuck at any point, you can check out the tag 2025.06-ext5/frontend/task-6-end, in order
to see how your code differs from the solution.
« Task 5: Middleware and Saga
30

-- 30 of 30 --

