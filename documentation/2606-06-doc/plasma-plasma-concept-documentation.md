# plasma plasma concept documentation

Plasma – The Design System
Get Started
A12 Plasma is a design system for enterprise applications. These applications, compared to
consumer applications, deal with two major requirements:
While facing these problems, customer and user still expect a consistent, efficient and - something
which became more important over the last years - a nice UI. To help you creating these user
interfaces for your project, we have created guidelines, components and patterns.
1

-- 1 of 81 --

Designing an application with A12 Plasma
If you are not familiar with the possibilities A12 Plasma is offering or want to know how to use A12
Plasma in the most efficient way, please take your time to have a look at the following two sections:
Designing the application structure
One of the main problems is the sheer quantity of information. This guideline supports you in
analyzing your application to define the application structure and its navigation concept. This also
helps you to extend your application with future requirements.
Designing The Application Structure
Business Objects
Information or Business Objects are seldom stand-alone components in enterprise applications.
Usually they are connected within a certain process or workflow. This guideline will show you the
most used workflow in an enterprise application and how you can visualize it with A12 Plasma.
Working with Business Objects
A12 Plasma Solutions
Our solutions are grouped in 2 main categories. Patterns shows how certain components can be
combined to solve requirements in a reusable and most efficient way. Components are like
ingredients which can be used to build applications or fulfill their role in a certain pattern.
Design Principles
Robustness
Enterprise applications are dealing with sensitive data, therefore our solutions will be fault tolerant
and keep the user in control in order to fulfill their tasks in a safe way.
2

-- 2 of 81 --

Efficiency - Fast and precise
Be carefully of a user’s time and respect their workflow. Anticipate ways for people to work better
and faster with their application. A12 Plasma provide the most efficient way for professional users
to do their job.
Accessibility - Usable by everyone
Accessibility in design allows users with disabilities to navigate, understand, and use your
application. It is important that accessibility is included in all phases of a project and a natural part
in the design process.
Guidelines
Our guidelines provide information on how to use A12 Plasma in the most efficient way.
Designing The Application Structure
To design an application it is necessary to analyze the information architecture and the processes of
a project. In this guideline we will walk you through an example to show how we use our patterns
and concepts to refactor an application. The same ideas can be applied when creating an
application from scratch.
Using Information Architecture For Analysis
To be able to define an application structure and its navigation concept we need to analyze the
information structure of our application. In this example we take Outlook 2010 to demonstrate our
approach.
When analyzing an application people might start with the most obvious layout areas and analyze
the UI components used to rebuild the application. For demonstration we have highlighted 3 areas
3

-- 3 of 81 --

in the example above. In this example you might see:
• Horizontal Navigation
• Vertical Icon Navigation
• Tree
• Table
But in order to rebuild an application and create the most efficient user experience you need to
analyze the actual information structure of the application. This knowledge will also help you to
extend the application with future requirements. So how will our example look like if we approach
it with this method?
As expressed by the arrows the information is structured in a certain hierarchy. Based on this we
can assume that we need a navigation concept to fulfill the following generalized view:
With these findings you can consult the following concept and documentation:
Application Frame
This documentation shows you the fundamental layout of our design system and its possibilities for
customization.
4

-- 4 of 81 --

Navigation Concept
How can users navigate to their content? The concept will demonstrate how many levels A12
Plasma supports and how to efficiently create a navigation concept.
The concepts will help us to redesign our example and to create the following screen:
As you can notice we have used different components and regrouped them compared to the
original application. In the next section we will explain these decisions and introduce some laws of
UX which you can apply to your projects to create a better user experience.
Problem-based requirements and laws of UX
As mentioned in the intro, one of the biggest challenges in enterprise applications is the sheer
number of information. More information means more time for users to capture the information,
which leads to less efficiency. Therefore in A12 Plasma we always strive to reduce the information
density to what is really required to fulfill a certain task or requirement. In this chapter we will
explain how you can achieve a better user experience by applying certain UX rules.
Hick’s Law - Task appropriateness
Hick’s law recommends to decrease the number and complexity of choices in order to reduce the
time it takes for a user to make a decision.
In enterprise applications users spend little time in browsing through different main sections of an
application, but rather spend their time in a certain dedicated section to take care of their work.
Therefore, instead of using a tab navigation with bold big icons we will use a rather simple
horizontal menu as our main navigation. The sole important function for a main navigation is to
show which section is currently selected.
5

-- 5 of 81 --

Law of Proximity
Proximity helps users to understand and organize information faster and more efficiently.
This also helps in terms of extensibility of an application. Even when adding a new feature, users
will find it faster because it can be placed in an expected area, instead of forcing them to search for
it on the interface.
In the following example we are combining the law of proximity with Hick’s law:
Fitt’s Law
This law recommends to keep the distance between a user’s task/attention area and the task-related
action as short as possible.
Applying this to the example and the task „Reading an email - Reply“ will result in the following
approach:
6

-- 6 of 81 --

Conclusion
We hope we were able to walk you through our thoughts and concepts in order to help you create
an application with A12 Plasma.
This guideline was only an intro to the full set of enterprise application requirements. Please take
your time to read further here:
Notification
Validation Handling
Applying UX Principles (Checklist)
The A12 framework allows its users to design their applications in multiple ways.
However, if you want to create a consistent usability and a positive user experience, you should
keep in mind the following guidelines.
Design Best Practices
Brevity
Consider shorter expressions to save (head-) space
Clarity
Make elements as self-evident as possible
7

-- 7 of 81 --

• Example: When choosing labels, ask yourself "Will the user understand what I am referring to
or does this wording have another meaning as well?" → e.g. the button "download pdf" is more
self-evident than "download" because it offers more detail on what is to be expected.
Consistency
Similarities between elements, as well as their differences, should be highlighted through
predictable, application-wide structures (e.g. naming structures), rules and conventions.
• Example: In a table, two column headers that both allow the download of a picture should be
named similarly: Either ".png picture download" & ".jpg picture download", or ".png download"
& ".jpg download". Each pair is named consistently because their naming structure highlights
the items' similarities and differences in a predictable pattern.
Correct formatting
Be mindful when formatting text. An element may come with its own automatic formatting
properties.
• Example: Don’t write button text as upper case. The component comes with its own formatting
properties and gets displayed with the intended formatting by default.
Guidance
Provide the user with all the information they may need to do their tasks. When unsure, rather give
more information (especially when deciding whether to add basic information like headers, titles
and labels).
Plasma Rules in each Application Section
8

-- 8 of 81 --

Application Frame
▼ Details
(A) Application header
Guidance
☐ Choose an application name
Structure
☐ Follow the order and structure as described here
(B) Navigation
Self-explanatory menu item names
☐ Wording as simple as possible
☐ Short (if possible)
☐ Unique names, non-interchangeable among each other (no synonyms either)
Structure
☐ Order menu items according to…
• their importance (hierarchy)
• their relatedness to each other (topical grouping)
Overview
▼ Details
9

-- 9 of 81 --

(A) Content Box
Guidance
☐ Use a title
(B) Action bar
Structure
☐ Use the following order for these most-used components (examples):
• Major area (left)
◦ Main action – e.g. "Add new item" button, see below
• Minor area (right)
◦ Search
◦ Filter
☐ Main action: Consider using an icon for the main action (more below)
"Add item" button
See paragraph Inputs & Buttons
(C) Table columns
Structure
☐ Consider pinning the following columns:
• item name column (name should be self-explanatory)
10

-- 10 of 81 --

• action column
Self-evidence/ Wording
Use self-explanatory column header names
☐ which are understandable while being short
☐ which are clearly distinguishable (not used more than once, no synonyms used, not
interchangeable)
• Example: Use "project logo" and "customer logo" vs. "logo" for both
Guidance
☐ Use headers (exceptions might be made for action columns)
(D) Content area: Icon buttons
Choose with the help of the paragraph on Icons
Form
▼ Details
(A) Content box
Guidance
☐ Use title
(B) Content area: Sections
Structure
☐ Read this to design a better information structure for your forms and display them nicely to
11

-- 11 of 81 --

your end-users
Guidance
☐ Use titles (typography widget) whenever they can help to segment information better and
make long forms easier to understand
(C) Content area: Inputs & buttons
See paragraph Inputs & Buttons
(D) Footer
See section on the Footer
Modals
▼ Details
(A) Content box
Guidance
☐ Use header text
(B) Content area
Body text
Guidance
☐ Write a text to explain to the user what the context of the modal is, or what the actions will
trigger.
12

-- 12 of 81 --

Brevity
☐ Keep everything short (while being clear)
Content area: Buttons & inputs
See paragraph on Inputs & Buttons
(C) Footer
See section on the Footer
Footer
▼ Details
Best practice case
Structure
☐ Please use this structure:
• Major area
◦ (1) one main action
◦ (2) one main action’s destructive counterpart
▪ Secondary button type:
▪ destructive
• Minor area
◦ any additional actions (if necessary)
◦ The amount of actions that can be shown explicitly depends on the space available.
▪ If the space is limited, some or all buttons get hidden using the Popup Menu
component.
Read more suggestions on the general button positioning here
Wording
13

-- 13 of 81 --

☐ Use consistent wording for buttons across the app.
• Example: Use "Save" everywhere instead of using both "Save" and "Yes, save". They mean the
same so they should be called the same.
☐ When choosing a wording, consider the following use cases (examples):
Confirm
• confirm process
◦ "«verb»" – e.g. "Save"
• confirm (having read) instructions
◦ If part of a step-by-step process – see Continue below
◦ Instructions – low risk
▪ "Understood"
▪ "Got it"
◦ Instructions – heavier consequences
▪ "Approve"
◦ Terms & conditions – heavier consequences
▪ "Accept (and continue)"
▪ "I + «verb»" – e.g. "I accept"
Continue
• continue step-by-step process
◦ "Continue"
Exit
• close window
◦ "Close"
• exit procedure
◦ when you do not want to continue with the shown procedure
▪ "Cancel"
▪ "Cancel «process»"
Decline
• see 1) and 2), except: if
◦ terms & conditions
▪ "Decline (and exit)"
14

-- 14 of 81 --

Dirty and deletion handling
Unlike in the best practice case, in the use cases of dirty and deletion handling the primary
button can be destructive, e.g. because the users need to confirm that they want to discard their
changes or that they want to delete something.
Structure
☐ Use the structure as described above, except:
• major area
◦ (1) one main action
▪ Primary button type:
▪ destructive
◦ (2) one counter action
▪ Secondary button type:
▪ default
Wording
☐ Read and choose one wording option for the same meaning across the app.
☐ Consider the following use cases:
• delete field
◦ Text: "Deletion cannot be reverted. What do you want to do?"
◦ Button pair:
▪ "Delete" – primary destructive
▪ "Cancel" – secondary
• unsaved changes
◦ Text: "There are some unsaved changes in the editor. They will be lost. What do you want
to do?"
◦ Button pair:
15

-- 15 of 81 --

▪ "Discard changes" as primary destructive button and
▪ "Go back" as secondary button
• warning
◦ Text: "By closing the bulk operation panel, the selection will be cleared. What do you want
to do?"
◦ Button pair:
▪ "Clear selection" as primary destructive and
▪ "Cancel" as secondary
Icons
▼ Details
In the following table you will find the recommended icons for the most common use cases.
When there are two icons for the same meaning:
• use any of the two as long as you use the same one consistently for the same meaning
• if possible, only one should be used per use case (or their meaning and use must be clearly
distinguishable)
Main use case Preview Recommended usage Google identifier
Add • add item
• include
add
add_circle
Attachment • attach (new) item
• (this is an existing)
attachment
attach_file
attachment
Close • cancel, end something
• close/hide
Attention: The icon "close" is
also used for deleting entries
(see "Delete" below) and
therefore only recommended
when "cancel" is no
alternative.
cancel
close
Copy • copy item
Only recommended for this
exact use; for different
meanings (e.g. template), use a
different icon
content_copy
16

-- 16 of 81 --

Main use case Preview Recommended usage Google identifier
Delete delete delete
final deletion (ie. from inside
of bin)
delete_forever
delete entry (e.g. from input) close
Download download file or item download
Edit • edit item
• write something
edit
Folder create new folder create_new_folder
select a folder (to save sth into) folder
delete a folder folder_delete
Home (Navigation) • start, home
• initial view of a web
application
home
Info • (more) information
In A12 context already globally
used in the widgets message
box and global message box.
Can be used for other purposes
as long as it is distinguishable
from these widgets.
info
Link add link/connection add_link
remove link/connection link_off
Menu, More (Navigation) • see more
• open (pop-up) menu
more_vert
more_horiz
Move move, drag or place item open_with
People, Group, Team • people, group
• team
group
groups
add group group_add
remove/delete group group_remove
disable/ block group (or
similar)
group_off
17

-- 17 of 81 --

Main use case Preview Recommended usage Google identifier
Person, User, Account • person, user
• personal account details
• profile details
account_circle
person
account settings &
management
manage_accounts
add person/user person_add
remove user person_remove
disable/ block user (or similar) person_off
Show less, resize view collapse information unfold_less
• minimize view
Use for layout elements
fullscreen_exit
Show more, resize view expand information unfold_more
• maximize view
Use for layout elements
fullscreen
Search (Navigation) search search
Settings (Navigation) • (general) settings
For account-related settings
consider using the icon
"manage_ accounts" instead
(see Person).
settings
Success • success
In A12 context already globally
used in the widgets message
box and global message box to
express success.
If you need a check symbol in
other contexts, we recommend
to use the "done" icon (below).
check_circle
• (getting something) done
• selected, checked
done
18

-- 18 of 81 --

Main use case Preview Recommended usage Google identifier
Warning • warning
In A12 context already globally
used in the widgets message
box and global message box,
but can be used for other
purposes, as long as it is
distinguishable from these
widgets.
warning
Inputs & Buttons
Inputs
☐ Use labels wherever it can help guide the user (read here for more info)
☐ Use placeholder texts if:
• you want to give an example of how to use the field
◦ Example: Field label = e-mail; placeholder text = john@company.com
Buttons
☐ Label: Choose between "text only" / "text + icon" / and "icon only" labels, see here how
☐ Wording consistency: Whenever it makes sense, make sure to use the same expression (and
possibly icon) for the same usage all across the app.
"Add item" button
(1) Use of "Add" Material icon is preferred – wherever it is possible.
19

-- 19 of 81 --

• This makes the function of the button very obvious.
• Default buttons where you can’t add an icon in the Form Model: see here
(2) Use of the "…" (item name) is preferred, unless
• the item name is too long – e.g. several words
• space is not available
Creating Forms
Introduction
Forms are a fundamental part of A12 based applications. They can be found in all kind of situations,
e.g.:
• Product site
• Tax document
• Survey report
This guideline shall support you in creating or adjusting forms of any complexity.
The first part covers how to structure the information of forms independently from their visual
appearance.
The second part takes care of how to display forms to your end users with the help of a good layout.
At the end of this section, you will find usability tips for common problems.
Video Tutorial
In the following video tutorial "From Paper to Digital Form" you can see how to easily transfer a
paper form into a user-friendly digital one in 6 steps.
The covered topics are i.a. select options, layout, help text and navigation.
► https://vimeo.com/680767260 (Vimeo video)
Materials for download:
20

-- 20 of 81 --

Paper Form (PDF), Digital Form (PDF)
Information Structure
As a first step, it is important to structure your form. Here, we are only taking care of the raw
“data” of the form, not the visual appearance.
A well structured form supports:
• The overall readability
• Users in skim reading
To achieve these goals, you can:
• Arrange content in “Groups”
• Use “Group Titles”
• Nest “Groups” to build “Group Hierarchies”
Groups
When building a form, try to group its content. For example, client-related information like name,
address and phone should be part of one group.
TIP
Do:
IMPORTANT
Don’t:
Group Titles
A group title describes the content of a group. By providing it, users can identify the content of a
form more easily. They do not need to read over the whole form to figure out which part is of
interest to them.
21

-- 21 of 81 --

A group title should be meaningful. Therefore:
• Try to find summarizing titles
• Avoid to just copy the label texts from the group’s fields.
TIP
Do:
IMPORTANT
Don’t:
However, in case, there is just one field inside a group, reusing its label as a group title can be valid.
Consider shortening the group title and/or field’s label to reduce redundancies.
Group Hierarchies
Nesting groups can improve the general effect of grouping even more. However, you should not
overdo it. Try to limit yourself to a maximum of three nesting levels for a good readability.
Otherwise the form creates the impression of being complicated.
TIP
Do:
IMPORTANT
Don’t:
22

-- 22 of 81 --

Keep in mind: Even if the theoretical information structure of your form calls for nesting, you do
not need to necessarily do so. Your end users will most likely not care too much about whether a
certain group should have been part of another one or not. A flat hierarchy is more desired than an
accurate one.
TIP
To flatten group hierarchies consider:
• Restructuring hierarchies
• Dissolving sub groups
• Putting sub groups on a higher hierarchy level
• Outsourcing groups to a new view
Layout
After setting up the form’s information structure, you need to decide on how to display it to your
end users.
The layout of a form defines the base for its visual appearance. The most common layouts are:
There are no rules, when to use which layout. It depends on your form’s content, goal and
complexity as well as the available space and the overall use case. For example, a column-oriented
layout might be a good choice for both:
• A simple form to change a password
• A complex insurance form
When using these layout types, there are some pitfalls to be aware of. The section below lists up
tips to avoid the most common ones.
23

-- 23 of 81 --

Row-Oriented
Keep the number of form elements, like fields or buttons, next to each other small: A PC screen is
typically wider than it is high. So, using the whole width seems to be the best approach to save
space. However, with a certain amount of form elements next to each other, a form gets really hard
to read. To avoid this, consider:
• Using a column-orientated layout approach
• Reducing the width of form in Master-Detail-View
TIP
Do:
IMPORTANT
Don’t:
NOTE
Please note that you are currently limited in modeling the width for Master and
Detail View. The SME only allows to define a desired width for the Detail View. That
means that A12 tries to set the width of the Detail View to the intended size if
possible. The Master View is automatically adjusted.
Column-Oriented
Keep content that belongs together close to each other: Avoid large gaps between related content
inside of one column.
TIP
Do:
IMPORTANT
Don’t:
24

-- 24 of 81 --

Keep the number of columns next to each other small: If a form consists of too many columns, it
gets hard to read.
TIP
Do:
IMPORTANT
Don’t:
Try to keep the height of a column small: Don’t force end users to scroll up and down endlessly in
order to read the content of columns next to each other (as illustrated with the blue line in the
images below).
TIP
Do:
IMPORTANT
Don’t:
25

-- 25 of 81 --

Mix
Keep the amount of nesting small: Nested rows or columns are valid to a certain degree. It helps to
visualize the information structure of your form. However, as mentioned in “Group Hierarchies”,
too many sub rows or columns create the impression of a complicated form and reduce the
readability.
TIP
Do:
IMPORTANT
Don’t:
For Groups with the same hierarchy level, consider putting:
• Group titles at the same height: This can help to identify sections faster
• Groups with a similar amount of content next to each other: This can help to reduce gaps
between sections
26

-- 26 of 81 --

• Groups with similar / comparable content next to each other
TIP
Do:
IMPORTANT
Don’t:
Responsive Behavior
Currently, A12 allows you to model the layout for a large screen. In case of smaller screens, A12
automatically arranges your content.
For example, inside the Form Model you can divide a Control Grid into up to 12 columns. However,
the 12 columns are only applied to a large screen. On a tablet, A12 would transform them to 6
columns, while on a small phone there would only be one column.
Example for Responsive Behavior of rows in a Control-Grid:
Example for Responsive Behavior of columns in a Multi-Column-Grid:
27

-- 27 of 81 --

In conclusion, you cannot completely control the layout of forms via A12 Tools currently. However,
there are already plans to provide this feature.
UI/UX Pattern
When creating forms, there are some frequently asked questions. Below, you can find tips on how
so solve certain issues. As with all guidelines, these are not strict rules. They can be broken if a use
case or requirements demands it.
How to improve readability?
Terms
Subject specific terms can be used to shorten the amount of text in your application. However, such
terms are limited to a certain group of people. In addition, terms can hold another meaning for
different end users. This can result in misunderstandings as different groups think they understand
a term correctly which another party uses.
TIP
To avoid confusion:
• Use terms that your end users understand
• Explain terms if needed
• Distinguish terms with various meanings
• Use terms in a consistent way
Labels
The needed input of a field is typically described by its label. When done well, you can provide good
orientation and guidance for your end users.
A form is much cleaner if you present labels in a consistent way. For example, decide if the labels
start with a capitalized letter or not.
TIP
Do:
28

-- 28 of 81 --

IMPORTANT
Don’t:
Short and concise labels are easier to read and understand. Try to limit yourself to single-line
labels.
TIP
Do:
IMPORTANT
Don’t:
In some cases, it is not easy or even possible to shorten labels without losing crucial pieces of
information. Here are two tips for what you can do in those cases:
TIP
• If you have trouble to shorten a label, consider outsourcing redundant parts to a
“Group Title” (see “Information Structure” above).
• If you cannot shorten a label, consider causing intended line breaks to limit the
29

-- 29 of 81 --

amount of words per line.
Please note that modeling intended line breaks in labels is currently not possible. You can
only try to generate them by reducing layout columns in width.
Proximity
Form elements that belong together should be placed close to each other. For example, consider
positioning a ZIP code and city Field next to each other or below each other.
Size of Form Elements
The size of a field provides pieces of information to your end users about how much content they
are expected to fill in. A very long field conveys the impression that the end user can or must type
much data. A tiny field on the other side creates the impression of limitation.
You can support your end users by resizing fields to an expected input amount. For example, a date
field can be short while a description usually requires more space.
TIP
Do:
IMPORTANT
Don’t:
30

-- 30 of 81 --

However, when adjusting the size of form elements, keep the whole layout in mind. Sometimes it is
better to intentionally reject size adjustments and avoid “ragged alignment” in favor of a cleaner
layout.
TIP
Do:
IMPORTANT
Don’t:
Please note that adjusting the size of Fields via A12’s Modeling Tools is currently limited as
explained in “Layouts > Responsive Behavior” above.
Recurring Forms and Form Parts
Certain forms are often re-used inside an application. They might vary a bit, but their main
elements are the same.
For example, contact forms inside one application may contain fields for name, address, post code
and phone number. In case the form shows data of a real person, the name field might be split into
forename and last name. In case the form is about a company, there might be a field for the
company’s name and department. Both forms may differ a bit, but their overall content is similar.
Try to display such recurring forms in a consistent way. By this, your end users do not have to read
and think about each e.g. contact form as they have already encountered similar looking ones.
TIP Do:
31

-- 31 of 81 --

IMPORTANT
Don’t:
The same can be applied for recurring parts of a form, too. For example if you decide to put
forename and last name next to each other, try to do so in all cases where this combination of fields
appears.
How to deal with dependencies?
In some cases, form elements are dependent on each other. For example, an end user has stated to
be married and therefore needs to fill out additional pieces of information for their partner. But
this is of no interest for a non-married end user.
Below, you can find some tips to guide your end users through such dependencies.
Visibility
TIP
• Only show form elements that are relevant for an end user.
• If it is not possible to hide those form elements, display them as disabled.
Proximity
TIP • Keep dependent form elements close by, for example next to or directly below each
other.
How to prevent false user inputs?
Sometimes, Fields require a certain input format or a checked checkbox demands the upload of an
32

-- 32 of 81 --

additional document.
To support a good usability, such issues should be pointed out to end users initially to prevent false
user input.
TIP
Do:
IMPORTANT
Don’t:
A12 provides several options to achieve this:
• Asterisk after labels of required form elements: “Last Name*”
• Placeholder for input formats: “YYYY-MM-DD”
• Hint Tooltip for subject specific explanations of a form element: “The phone number is needed
to … .”
• Helper Text for input requirements: “Year must not be dated back more than 3 years.”
• Message Box for notes: “Please attach a copy of the driver’s license.”
Please note, that A12 is currently not supporting to model Helper Texts or Message Boxes.
By initally showing end users what to do, you can:
• Increase a positive user experience as they were able to fill out a form with no problems.
• Reduce the effort of an end user to fill out a form as they do not need to correct mistakes.
How to deal with limited space?
The space for a form element might be limited in certain use cases, e.g.:
• On a dashboard
• Inside a repeat
Below, you can find a list of tips for dealing with such situations.
Labels
Keep labels readable. Therefore, consider:
• Enlarging the form element to the required label size
33

-- 33 of 81 --

• Outsourcing label text to a Group Title, plain text, etc. above the form element
• Using synonyms
• Using common or understandable abbreviations
• Using Tooltips
• Truncated label text with “…”: Advanced end users can often guess the meaning of truncated
words out of the context, but you should use this with care
• Using an suitable icon instead of text
Remember to check all translations.
TIP
Do:
IMPORTANT
Don’t:
Please note that adjusting the size of a File Upload (aka “Attachment” inside A12’s Tools) as
shown in the example images above, can currently only be done code-wise.
Validation
Generally, error and warning messages should be visible for end users directly. However, when
dealing with limited space, A12 offers to outsource such messages to Tooltips.
TIP
Do:
IMPORTANT
Don’t:
34

-- 34 of 81 --

Compact Exposition of Data Types
Certain Data Types can be displayed in a compact way. For example:
• “Enumerations” can be displayed as radio buttons or select fields.
• “Strings” can be displayed as a full Text Area or an auto expanding one.
This saves much space, especially in height.
Form Elements for Selection
Choosing the right selection widget for form elements might be tricky sometimes, which is why we
will try to explain the different advantages and disadvantages of the solutions in this section.
First, you need to decide whether the user can select only one of the given options (= Single
Selection) or if multiple options can be selected at the same time (= Multi Selection). In the next
step, you can choose which element fits perfectly to your needs with the help of the tables and use
cases below.
If you need a selection element for Special formats such as times, dates or icons, the Picker widget
is the best choice.
When modelling selection fields with the A12 tools, you might come across different terminology,
which is why we put a short overview in the chapter Terminology in A12 Tools.
Single Selection
Overview
If the user can select only one of the given options, the following elements are possible.
35

-- 35 of 81 --

The questions in the table might help to choose the most suitable component:
Checkbox Switch Radio
Buttons
Select Autocomplet
e
How many options
are available?
one two a few many unlimited
Is it an optional
field, which needs to
be resettable?
[check] ([check])* [check] [check]
Do you have long
option names?
[check] [check] [check] [check]
Do you want the user
to directly see and
select the options?
[check] [check] [check]
* see section Yes/No Decisions as optional fields
If you are still unsure which form element is the best for you, the following use cases and layout
recommendations might be helpful to make a decision.
Use Cases
Yes/No Decisions
For select fields with simple yes/no decisions, we recommend radio buttons or the switch, as they
are
• directly visible and comprehensible
• selectable with one click only.
Radio buttons with vertical alignment
TIP
Use the Radio buttons with vertical alignment if
[check] you want a very good comprehensibility
[check] you need longer option names
36

-- 36 of 81 --

Radio buttons with horizontal alignment
TIP
Use the Radio buttons with horizontal alignment if:
[check] you have only limited vertical space available
Switch
TIP Use the Switch if
[check] you have on/off or true/false choices
[check] you have only limited vertical space available
[check] you want a less formal design
Yes/No Decisions as optional fields:
If the select field is an optional field and needs to be resettable to an empty state, then you can use
• Radio Buttons with a third option (e.g. “No information”)
• a Select with a third option (e.g. "No information"), or
• a Checkbox
Confirmation fields
For confirmation fields (e.g. “Yes, I agree”), the Checkbox is usually the best choice:
TIP
Use the checkbox if:
[check] you want a very good comprehensibility
[check] you need to use longer option names
Many available options
Select
37

-- 37 of 81 --

TIP
Use the Select if:
[check] There are many available options (up to 20 recommended)
[check] The user doesn’t know what terms to search for
Autocomplete
TIP
Use the Autocomplete if:
[check] There is a very high number of available options
[check] The user knows what terms to search for
Layout recommendations
Complex forms with restricted space
If the available space is restricted and you have several available options, we recommend
Autocomplete or Select:
TIP
Do:
38

-- 38 of 81 --

IMPORTANT
Don’t:
Simple forms with sufficient space
If you have enough space and not so many available options, we recommend using the Radio
Buttons:
TIP
Do:
IMPORTANT
Don’t:
Multi Selection
If the user can select more than one of the given options, the following components are available.
The questions in the table and the info boxes below might help to choose the most suitable
component:
39

-- 39 of 81 --

Checkbox Group Tag Input Multiselect
How many options are
available?
a few a few unlimited
Is it an optional field,
which needs to be
resettable?
[check] [check] [check]
Are the users allowed
to create options
themselves?
[check] [check]
Do you want the user to
directly see and select
the available options?
[check]
Checkbox Group
TIP
Use the Checkbox Group if:
[check] the number of options is not too high
[check] you want a good comprehensibility
[check] you need longer option names
The checkboxes can be arranged vertically or horizontally, the advantage of the vertical alignment
being a better comprehensibility, while the horizontal alignment takes up less vertical space.
TIP
Do:
IMPORTANT
Don’t:
Tag Input
40

-- 40 of 81 --

TIP
Use the Tag Input if:
[check] Only a few options will be selected at the same time
[check] The option names are not too long
[check] The user can create options themselves
Multiselect
TIP
Use the Multiselect if:
[check] There is a very high number of available options
[check] The available space is restricted
Special Formats
For special data formats we currently offer four types of selection elements:
In comparison to a simple input, the picker is especially helpful when the available options are
restricted (e.g. only dates in the last month are allowed). In general, the picker is more user friendly,
as the users do not have to struggle with the correct date format or the proper names.
Terminology in A12 Tools
Within the A12 Tools, the terminology for selection fields is sometimes different from the widget
showcase:
Single Selection
41

-- 41 of 81 --

Date Type Exposition Corresponding Widget
Enumeration Compact Select
Autocomplete Autocomplete
Full Radio basic (with vertical
alignment)
Inline Radio basic (with horizontal
alignment)
Boolean Switch (with or without value) Switch
Checkbox Checkbox
Confirm Checkbox Checkbox
Multiselection
Date Type Exposition Corresponding Widget
Multi-Select Autocomplete Multiselect
Full Radio basic (with vertical
alignment)
Inline Radio basic (with horizontal
alignment)
Enterprise UX Pattern
These patterns provide solutions for requirements which mainly occur in enterprise applications.
Working with Business Objects
What does it mean to work with business objects? In our illustration above we pictured our
42

-- 42 of 81 --

business object as a shopping cart. You might be familiar with the actions which can be performed
on a shopping cart, but in enterprise applications these actions can be applied to any professional
context. Here are some examples of actions users perform on business objects:
• Overview
• Compare
• Create
• Edit/Update
• Analyze
• Create
• Delete
• Change Status
But as illustrated, actions can’t be seen as stand-alone features and in majority stand-alone actions
never can fulfill any requirement. An action often leads to another action or has at least an
influence on other business objects. Here is an example to demonstrate this statement:
As you can see above only the combination of all steps fulfills the whole e-commerce example
above. The whole process above is also described as a userflow in UI/UX design. But seldomly a user
will process a userflow in a straight forward approach. And thus in order to achieve a good user
experience, it is key to also think about the transitions between these actions. We have prepared
patterns which takes these transitions into consideration and provide the best user experience for
your user’s needs and business requirements. These patterns can all be used and combined
simultaneously in an application.
Workflow Patterns
Master Detail Flow
43

-- 43 of 81 --

A master detail pattern is well known in business applications, where the master (left area) is often
represented by a table and the detail (right area) by a form for editing or presenting details.
TIP
Use this pattern if
• Users need to quickly switch between different business objects
• A business object is not complex but still requires a navigation (max. 2 levels)
• Users need to browse the whole object to gather information for carrying out the
task
To achieve this pattern we are providing the Master-Detail layout widget. To get to know the full
feature of this layout please take a look here:
Master Detail Layout
If the detail consists of eg. multiple tabs please consider loading the direct tab needed for the task
instead of showing the first tab and forcing the user to navigate to the information needed.
Alternatively consider using the „Modal Edit“ for certain actions.
Modal Edit
In this pattern the detail or the necessary information is presented in a modal overlay.
TIP
Use this pattern if
• The business object is not complex and the interface can bring the information
directly into the user’s focus
• The business object is complex but the task requires only a subset of information
to be done
To achieve this pattern we are using the following components:
Widget Showcase → Modal Overlay
Client Sample App (mgm internal only) → Button "Start nested activity"
Client Documentation → Regions
44

-- 44 of 81 --

Inline Edit
This is an alternative pattern for modal edit. Although patterns can be combined, we strongly
advice against mixing this pattern with the modal edit pattern in one view.
TIP
Use this pattern if
• The business object is not complex and the task requires the user to compare his
input with other business objects
• The information needed to fulfill the task fits in a small area and does not require
the user to scroll or extend the area significantly
This pattern can be achieved with the following table widget:
Widget Showcase → Table
New Page
This pattern opens the detail in a complete new view with its own navigation.
TIP
Use this pattern if
• The content of the business object is complex and requires more than 2 navigation
levels
• The content is very complex and might require complex patterns like master
detail, dashboard etc. to be visualized
If you want to know how to place the information of the business object or set up the navigation
45

-- 45 of 81 --

please take a look at these concepts:
Application Frame
Navigation
Client Documentation → Scene Change
Validation Handling
Validation is a basic and indispensable concept for every enterprise application. In A12 Plasma we
differentiate between two different types of validation handling which have to be accessibile for
every user, including blind users and usage without mouse. The types are distinct by the kind of
validation and the visual application layer.
• Application Level Validation
• Block Level Validation
◦ Form Validation
▪ Overall Form
▪ Field Specific
◦ Table
Validation Principles
If a problem occurs the user must be informed about it - a problem can be an error or a warning
and it can appear as a general application problem or as a problem of form validation. If it is based
on an input field in a form, this input field is called invalid input. Therefore the following principles
have to be followed:
TIP
• Clear presentation and description of the problem
• Design supports the recognizability and distinction of
◦ errors, as they must be solved
46

-- 46 of 81 --

◦ warnings, as they should be solved
Validation Messages
Validation messages are a crucial part of a validation concept. They should always contain the
following information:
• What is the problem?
• Why did it happen?
• How to solve it.
In enterprise applications errors and warnings are often not bound to a single source of failure but
in fact could be the result of business logic creating dependencies between multiple sources. In this
case provide a list of possible error sources.
To achieve the best user experience also consider the following guidelines:
TIP
• Be specific to the user’s task. Provide a guidance as concise as possible to solve the
problem
• Don’t provide generic error messages. Ask yourself: "How does the error message
sound when you speak it in conversation"
General Design Decisions
Based on the requirements of usability and accessibility guidelines which recommend to use more
then one visual characteristic for distinction, our widgets follow common design principles for
validation states. These states are expressed in:
Error
We use red and and a rectangle with an exclamation mark for displaying errors.
Warning
To express warning we use orange and a triangle with an exclamation mark.
Application Level Validation
Errors and warnings can also describe problems which are application wide, eg.
• Server connection errors
• Expiring user password warnings
We are providing and recommending the following widgets for displaying these issues:
Modal Notification
TIP Use this if
47

-- 47 of 81 --

The problem needs prompt reaction by the user and without closing the modal there
is no other interaction possible with the application - High priority.
Widget Showcase
Client Documentation - Error handling
Global Message Box
TIP
Use this if
The problem needs to be shown to the user and it is important to notify the problem
application-wide, but the application can still be used - High or medium priority.
Widget Showcase
Client Documentation - Error handling
Toasts
TIP
Use this if
The user should be informed about the problem but it is not harming the application
functionality so the toast can be taken care of or not - Low priority.
Widget Showcase
Client Documentation - Notifications
Block Level Validation
A block level validation describes the validation concept on the level of information blocks eg. an
overview or a form.
Form Validation
Overall Form
The overall form validation checks the data against defined business logic rules. These rules can
create dependecies and connections between multiple data fields and should therefore only be
validated if the whole form is submitted. This pattern ensures that the user is not disturbed while
still fulfilling his task. After the submission of the form we are using a validation bar to display a
list of issues. For a live example please visit the link below:
Widget Showcase
Form Engine (mgm internal only)
48

-- 48 of 81 --

Field Validation
The field specific validation is the prompt validation of input data. This should be used for type
specific rules, eg. writing a letter into a number field would produce a field specific problem. The
problem can be shown to the user immediately and directly on the field. When expecting a certain
format, eg. date input, please make use of placeholder texts to indicate which format is expected to
avoid user frustration. The field specific error and warning indications are supported by every data
entry widget.
The indication can either be shown directly to the user as message or in case you don’t have
enough space, we are also providing a tooltip as an indicator:
Widget Showcase
Widget Showcase
Table & Tree Table Validation
For an overall validation of the table you can use the validation bar to offer users a summarized
view. Additionally to this we are providing the following solution to display errors and warning in
tables and tree-tables. This pattern includes solutions for displaying issues on entry and sub-entry
level by differentiating whether the issues is located at the row itself or in a contained row.
Widget Showcase
Login
In order to keep sensitive data from non-authorized people, logins are a must-have to provide basic
websecurity. There are several approaches to logins, e.g. form, certificate or via an external identity
provider. As logins are highly connected to the topic of websecurity, please dicuss questions and
needs also with your websecurity collegues.
49

-- 49 of 81 --

Structure
A login usually consists of:
Application Identifier
To identify an application. This can be done by displaying the application’s name, logo or both.
Credentials
Data required for the login, e.g. username and password.
Login Button
To submit user inputs to the application.
A12 provides the possibility to adjust a login to your needs. For example, you can use another
approach for identification instead of username and password. It is also possible to add more input
fields, include captchas or offer a "password forgotten" feature.
50

-- 50 of 81 --

An example login can be found here:
Widget Showcase
Use Cases
When dealing with logins, there are several use cases to consider:
• Successful Login
• Failed Login
• Change Password
• Login via External Identity Provider (IDP)
Successful Login
After a successful login, users are redirected to the starting page of the application. No additional
notification is required in general.
However, in case of very sensitive data it is common to inform users about successful logins to
improve websecurity, e.g.:
• E-mail about login on different device
• E-mail about login from another country
Failed Login
A login can fail for different reasons:
• A user typed in invalid credentials by accident
• The server connection was lost while processing a login
• A hacker tried to inflitrate the application
Users should be informed about failed logins:
• Notification with error message
• Notification about the amount of failed logins
• E-mail about failed logins, e.g. login attempts via another device or a different country
TIP • Use summarizing, vague error messages about credentials like "incorrect user
51

-- 51 of 81 --

name and/or password" to support websecurity
• Provide specific information about suspicious issues, e.g. another device or
country
Based on websecurity guidelines, errors should be shown in a general way and not be bound to a
specific field.
Change Password
We are differentiating between two use cases where users might want to change their password.
• Inside the application
• Before login
Inside the application
Inside the application, "Changing Password" is a user feature. Therefore it should be displayed near
other user features like "Logout".
Widget Showcase
Before login
There are situations in which users desire to set a new password before they have logged in, e.g.:
• A user forgot their password.
• A password expired and a user cannot login.
The common user flow is as follows:
52

-- 52 of 81 --

Request New Password
An email is sent to the user with further information.
Receive Link/Token
The mail consists of information with a link and/or token. Links are utile if a fast access to the
"change password page" is desired. They can be interactive or not. Tokens are useful if high
websecurity is desired. For logins, tokens are most commonly strings, similiar to serial keys for
software. You can also provide both to support users with different mindsets. For example, there
are users who don’t trust links.
Verification by Link/Token
A valid link opens a page to change a password. A valid token needs to be typed in an input field
and submitted to achieve the same.
Change password
Commonly, the new password requires to be typed in twice. This helps to confirm an intended
password input. Password conditions or tips are often displayed in this step, too. This supports
users when choosing a new password.
Widget Showcase
Redirected to Login
If the new password is valid, the user can try to log in again.
TIP
• Provide this feature if users are allowed to change their password themselves
• Provide access to this feature on the login page
• Provide each step of the userflow in seperated screens to guide the user through
the steps
Login with External Identity Provider (IDP)
53

-- 53 of 81 --

Projects may outsource the user management to a third party sytem. By this, the login itself is not
provided by A12 but by another system, a so called external "Identity Provider" (IDP).
We do not have influence on how a login page of an IDP looks like, what kind of input data it
requires or which features it supports. However, we recommend embedding the IDP login and
providing some information, e.g.
You can login into "Application name" with "IDP name".
The text above is just a simple example for the concept of providing information about the context
and purpose to ensure the users that they really log in into their desired application.
Buttons
Introduction
Buttons can be used to trigger different functionalities like print and export. As there are several
use cases for buttons, A12 offers you a great variety to choose from. We will take a look at them in
the following documentation.
The first part shall help you to find the right button for your specific use case. You will learn about:
• Button types
• Button hierarchy
• How to deal with buttons, that delete data or abort an user flow
The second part introduces UI/UX concepts about how to integrate your button in your application.
This part covers how to:
• Position buttons
• Order buttons
54

-- 54 of 81 --

Button Type
A12 offers to display buttons with:
How to use?
When working with buttons, consider the following general usability tips:
TIP
Use:
• Short and concise button labels. Try to limit yourself to max. 2 words if possible.
• Fitting, self-explaining and common icons.
• Consistent terms and icons.
When to use?
When choosing a certain button type, keep your use case in mind. Below you can find some tips
that shall help you to find the right button for your requirements.
Text
Use a Text Button if words describe an action better than an icon alone.
TIP
Do:
IMPORTANT
Don’t:
Text & Icon
You can add an icon to your Text Button to support skim reading.
55

-- 55 of 81 --

Icon Only
You should solely use an icon only:
• For commonly known actions, e.g. the "centered text" icon.
• If available space is limited and the usage is clear. Otherwise consider using a context menu.
TIP
Do:
IMPORTANT
Don’t:
Button Hierarchy
Sometimes, certain buttons are more often used by end users than others. To highlight their
importance, you can define a “hierarchy” for buttons.
Buttons with “text” or “text with icon” can be Primary or Secondary Buttons.
Buttons with “icon only” have an additional third hierarchy level, since they can be repeated/shown
in a high number, e.g. as row actions in a table. Therefore, A12 provides a third, less prominent
hierarchy level for those “icon only” buttons.
When to use?
A12 buttons come with a default hierarchy level. Buttons with “text” or “text with icon” are
Secondary Buttons. Buttons with “icon only” uses their third hierarchy level.
If you want to highlight the importance of certain buttons, you can use their higher hierarchy level.
This helps your end-user to find those buttons faster.
How to use?
Primary Button
Primary Buttons are meant to be used for the most important action in a usage context, e.g. “save”
in a form screen. Therefore, only one Primary Button should be used in one user flow.
TIP
Do:
IMPORTANT
Don’t:
56

-- 56 of 81 --

Secondary Button
A Secondary Button is often used in combination with a Primary Button, e.g. “cancel” when
combined with “save”. While the Primary Button marks the most important action, Secondary
Buttons can be used for every other action. Therefore, they can appear multiple times in one user
flow.
Destructive Buttons
There are use cases in which a button:
• Aborts a user-flow, e.g. cancel a saving process.
• Causes losing data, e.g. delete a document.
Plasma summarizes these behaviors with the term “destructive”.
When to use?
You can hint the use cases from above to your end users by displaying the responsible button as a
destructive one.
How to use?
When working with destructive buttons, keep the following usability tips in mind.
Multiple Destructive Buttons
Avoid to use multiple destructive buttons in one user flow. It can be quite confusing for your end
user.
For example, take a look a the image below on the right-hand side. There are two destructive
buttons. As a result, the user has to think about which buttons causes loosing data and which one
does not.
TIP
Do:
57

-- 57 of 81 --

IMPORTANT
Don’t:
Equal Choices or Actions
In cases of equal actions or choice options, avoid destructive buttons. Else a destructive button
looks more important than a non-destructive one.
TIP
Do:
IMPORTANT
Don’t:
This can also be applied to Icon Buttons inside the last column of a repeat. As a result, avoid using
destructive buttons here as well.
TIP
Do:
IMPORTANT
Don’t:
Button Labels
As it is confusing to read the word "black" while it is displayed in white font color, it is also
irritating to see a positive label with a color that causes attention. Based on this, avoid using labels
like “Yes” for a destructive button. Change the label to fix the problem.
TIP
Do:
IMPORTANT Don’t:
58

-- 58 of 81 --

Accessibility
NOTE
Color-blind end users:
For color-blind end users consider using a “Text with Icon” instead of a “Text”
destructive button.
This is not a must-have as the text label also hints the destructive nature of a button.
However, it provides the benefit of the regular destructive button for color-blind
users, as well.
Button Position & Order
The right position and order for buttons can improve the usability, consistency and readability of
an application. This section summarizes the most important A12 patterns to achieve these goals.
General Suggestions
Put buttons that belong together close to each other.
TIP
Do:
IMPORTANT
Don’t:
Avoid mixed orders. Instead, order buttons:
• By button type.
TIP
Do:
59

-- 59 of 81 --

IMPORTANT
Don’t:
• By button hierarchy.
TIP
Do:
IMPORTANT
Don’t:
• Based on being destructive or not.
TIP
Do:
IMPORTANT
Don’t:
Use a consistent way to:
• Position buttons.
• Order buttons.
Action Bar & Footer
The Action Bar (aka “Subheading” inside A12 Tools) is located in the top area of the Content Box,
while the Footer can be found at the very bottom of it. Both wrap the content area of the Content
Box, which could contain a large table or a form.
60

-- 60 of 81 --

Overall buttons of e.g. an Overview or a Form Model should be positioned inside the Action Bar or
Footer. Common example are “add”, “save” and “cancel”.
When to use?
TIP
Use the Action Bar:
• If end users shall notice a button fast AND
• …if your end users do not need to consider the content below the Action Bar
before using a button.
TIP
Use the Footer:
• If your end users need to consider the content above the Footer before using a
button.
Major and Minor Area
Action Bar and Footer consists of two areas: Major and Minor Area. When putting buttons in
either the Action Bar or Footer, you also have to choose in which of those areas a button shall be
positioned.
61

-- 61 of 81 --

When to use?
Major and Minor Area differ for Action Bar and Footer due to the western reading direction. When
skim reading, end-user will notice buttons inside the Major Areas sooner than the ones in the Minor
Area.
The responsive behavior of each area was defined based on the reading direction, too: When the
screen gets smaller, buttons inside the Minor Area are collapsed first. After this, buttons in the
Major Area get collapsed from the inside to the outside step by step.
Therefore consider the following usability tips:
TIP
Use the Major Area:
• For the most important actions, e.g. “save” and “cancel”.
TIP
Use the Minor Area:
• For less important actions.
How to use?
When considering either the Major or Minor Area, also keep the following A12 patterns for Primary
Buttons in mind:
TIP
Put the Primary Button in the Major Area
• In case of the Action Bar, put Primary Button on the very left-hand side.
• In case of the Footer, put Primary Button on the very right-hand side.
62

-- 62 of 81 --

Technical Implementation & Live Examples for Buttons
Buttons
Button Group Container
Master-Detail
Application UX Pattern
These patterns provide solutions for general application requirements.
Navigation
Navigation Principles
To create a navigation concept it is important to consider the cultural way how information is
processed. In the western culture these are the main principles:
63

-- 63 of 81 --

Navigation Areas
The main purpose of navigation in enterprise applications is to guide a user through the application
in order to find information for fulfilling a task. Based on this we are mainly differentiating
conceptually-wise between:
• Structural Navigation
• Task Content
To enable a more fine-grained navigation concept we are extending this concept by providing 4
areas:
• Structural Navigation
◦ Main Navigation
◦ Sub Navigation
• Task Content
◦ Content Navigation
◦ Content
64

-- 64 of 81 --

Main Navigation
The main navigation offers the entry to the major modules of your application. We have chosen a
horizontal and unobtrusive space, although it might not be able to show many entries at first sight,
due to the following reasons:
NOTE
• Enterprise applications generally don’t have a lot of major modules. The
complexity lies within the subsections and connections between the major
sections.
• Business users usually don’t switch often between the major sections. It’s more
common for them to work/start with one major section. Therefore we don’t
want the main menu to be prominent on the page.
Sub Navigation
As stated above we can expect a high complexity in a major section. Therefore we have chosen a
vertical navigation for representing this due to the following reasons:
NOTE
• Vertical navigations can easily be extended because each entry takes up less
space from the whole screen. It is also easier for users to scan the whole
navigation when having a big number of entries.
• Even if the menu exceeds the displaying area the vertical scroll is the fastest
user interaction, so that users can quickly go through the navigation.
Content Navigation
The content navigation and content should mainly be used to present information in order to fulfill
a certain task. By reducing/restricting the information to what is really needed you can achieve a
65

-- 65 of 81 --

good information density and therefore raise the user’s efficiency. Therefore you should:
NOTE
• Limit the navigation entries as much as possible.
• Keep it simple and the navigation flat, only one information level should be used
Content
To help you structure your content we are providing you the following guidelines and components:
Plasma documentation
Information Structure
Form Engine Documentation
Repeats
Sections
Widget Showcase
Collapsible Panel
Heading Styles
Layout and Navigation Components
The navigation areas provide a basic navigation structure to deliver a consistent user experience
and are realized by using the „Application Frame“ widget, which also provides other layout
specifications apart of the navigation. For more information, take a look here:
Application Frame
Each area can consist of various navigation components. Per default we are using the Flyout Menu.
As demonstrated in the widget showcase the menus can represent more than one information level
and therefore allow the application to represent more than four information levels. Apart from a
possible higher number of levels we are also recommending to group menu entries to achieve a
better usability. Due to general usability research this is already recommended if a menu consists of
more then 7 entries.
Alternatively to the default navigation components we are also providing the following components
to raise the efficiency and usability in certain user scenarios.
Accordion
TIP
Use this widget if
• The user needs to see which entry is selected but doesn’t need to move quickly
between the menu entries
66

-- 66 of 81 --

• The area for navigation is limited and you want to avoid an extra scroll bar, either
vertical and horizontal
Accordion
Tree
TIP
Use this widget if
• The user needs to see which entry is selected and needs to move quickly between
the menu entries
• The information structure is very complex and it is necessary for the user to
quickly recognize the hierarchy
Tree
Sliding Menu
TIP
Use this widget if
• The sub level of a level consists of many entries, which needs to be displayed
instantly without any user interaction
• The user is focused on one navigation at a time
Sliding Menu
Notification
Introduction to Notifications
Notifications are messages directed to the user of an application, e.g. "File was saved.". In general,
67

-- 67 of 81 --

they are meant to inform a user about the state or outcome of their action. In some cases,
notifications require user interaction, e.g. confirm or cancel deleting a file.
Notifications are defined by:
• Notification Level
• Notification Type
• Notification Widget
Notification Levels
Notification Levels are a conceptual tool defined by Plasma. They can be used to find the most
suitable Notification Widget for your use case. Notification Levels are based on the questions:
• What part of your application triggers the notification?
• Where in your application should the notification be visible?
Derived from those questions, Plasma has defined three Notification Levels:
Application Level
An application-wide notification, e.g. about lost server connection.
Block Level
A notification about a part of the application, e.g. a validation summary of a whole form.
Element Level
A notification related to a single element, e.g. a validation message of one input field.
Notification Types
68

-- 68 of 81 --

Information
Notification to highlight certain pieces of information.
Success
Notification about a successfully finished application process.
Warning
Notification about a non-critical issue, which can be canceled or ignored by a user.
Error
Notification about a critical issue, which needs to be repeated or corrected by a user.
Notification Widgets
A12 provides several widgets for notifications. Below you can find them sorted by common usages
by Notification Level and Notification Type.
Common Usage for Notification Level
Application Level Block Level Element Level
• Global Message Box
• Modal Overlay
• Toast (Permanent)
• Toast (Temporary)
• Badge
• Modal Overlay
• Local Message Box
• Toast (Permanent)
• Toast (Temporary)
• Validation Bar
• Badge
• Modal Overlay
• Input Message Box
• Toast (Connected)
• Tooltip
69

-- 69 of 81 --

Common Usage for Notification Type
Information Success Warning & Error
• Badge
• Global Message Box
• Modal Overlay
• Toast (Permanent)
• Toast (Temporary)
• Badge
• Toast (Temporary)
• Toast (Connected)
• Badge
• Global Message Box
• Input Message Box
• Modal Overlay
• Local Message Box
• Toast (Permanent)
• Tooltip
• Validation Bar
Below there is a more detailed list of the A12 Notification Widgets mentioned above. It includes
other usage tips within the context of notification as well as links to the A12 Widget Show Case.
Badge
TIP
Use this widget
• For permanently visible notifications.
• To represent notifications within a navigation entry.
Badge
Global Message Box
TIP
Use this widget
• For permanently visible notifications.
• If other content should not be overlayed.
Global Message Box
Input Message Box
TIP
Use this widget
• For single input fields.
• For permanently visible notifications.
Input Message Box
Local Message Box
TIP Use this widget
70

-- 70 of 81 --

• For groups of input fields.
• For permanently visible notifications.
Message Box
Modal Notification
TIP
Use this widget
• If user reaction is required immediately.
Modal Overlay
Toast (Permanent)
TIP
Use this widget
• For permanently visible notifications.
• For short notification messages.
Toast and Toast Group
Toast (Temporary)
TIP
Use this widget
• For temporarily visible notifications.
• For short notification messages.
Toast Group
Toast (Connected)
TIP
Use this widget
• For temporarily visible notifications.
• For short notification messages.
Connected Toast
Tooltip
TIP
Use this widget
• Mostly for input fields.
• For initially hidden notifications.
• If the available space is limited.
71

-- 71 of 81 --

Tooltip
Validation Bar
TIP
Use this widget
• For a summary of the validation issues of whole the form.
Validation Bar
Related Topic
Validation
Layout Widgets
Application Frame
When analyzing enterprise applications we observed that apart from individual professional
requirements, you have recurring features like:
• Global search
• User features eg. Logout
• Navigation
• Content
72

-- 72 of 81 --

• Context Navigation & Actions
To deliver a consistent user experience and to help you get started, our application frame provides
layout specifications for placing these features.
Main Area
Application Header
The application header is intended to be used for global application-wide functionalities and
typically consists of the following features:
The header is not restricted to the shown features but in fact provides four slots which can be used
for placing information or functionalities. In case you want to place more then four features we
recommend grouping these functionalities to keep the number of options at four. This is especially
important for mobile applications considering the recommended touch size for guaranteeing the
best usability.
Navigation Area
This area is preserved for the main navigation of the application. For a complete view on our
navigation concept take a look here:
Navigation
Sub Area & Content
Visually the sub area and the content are separated, but from a conceptual and professional view
73

-- 73 of 81 --

these areas are strongly connected to each other. In our concept we consider both areas combined
as the whole view of a complex business object or a module with multiple screens. Each application
can consist of several of these views. To demonstrate this concept we have illustrated some views of
outlook using the described combination.
Sub Area
This area is optional and can consist of multiple functionalities:
Content
Depending on the actual content and the user scenario it is required to display the data in different
ways. Therefore the content area can consists of multiple layouts, e.g.:
74

-- 74 of 81 --

These examples only show possible layouts. For further information and layout options, please take
a look here:
Master Detail Layout
Dashboard
Technical Implementation & Live Examples
For implementation details and examples please visit the following links:
Client Developer Documentation
Application Frame
Widget Showcase
Showcase Layout
Master Detail Layout
The Master Detail Layout is a specific UI solution to enable users getting an overview and working
with a large set of business objects. Additionally to the master detail we are also providing other UI
solutions which are documented here:
Working with Business Objects
Usage
Our Master detail layout provides slots which can consists of various components. The most
common usage of the master detail consists of a table representing the master and a detail
represented by a form. But there are also further usages possible.
75

-- 75 of 81 --

Layout
Slot Number
Typically a master detail consist of only 2 slots as shown above, but in enterprise applications
information structure can be very complex, therefore our master detail allows not only 2 levels but
provides the possibility to use as much slots as needed. Depending on your screen resolution and
use case you can define how many slots are visible at the same time.
NOTE We recommend to use a maximum of 4 slots. In case you have a deeper information
structure, we recommend to use our navigation concept:
76

-- 76 of 81 --

Navigation
Slot Size
Depending on the use case and your content it is necessary to define a certain size for a slot. To
guarantee a harmonious user experience, our layout widgets uses a 12-column grid system,
therefore you can set the slot width from 1-12 columns eg.:
The size definition is only valid for desktop devices and tablets. On the smartphone each slot will
use the full width of the device.
Navigating Between Slots
As described above, the layout itself only provides layout slots. However, it is still necessary to
consider the following navigation concept to guarantee a consistent user interaction. Our concept
provides solutions for the following use cases:
• Opening the next slot
• Closing a slot
• Going to a certain slot
Opening the next slot
To open the next slot, any interactive element in the current slot can serve as a trigger, e.g. a table
row or a certain row action. This is strongly based on your requirement.
77

-- 77 of 81 --

Closing a slot
Our design system recommends to use the following interactions for closing a slot.
Back
This solution should be used if there is only one slot visible at a time.
Close
The "Close" pattern should be used if there are 2 or more slots visible at a time.
Going to a certain slot
In case you are using more then 2 slots, it might be necessary for users to quickly go back to a
certain slot. For this use case, Plasma provides the interaction pattern using a select enabling the
quick return to e.g. slot 1.
78

-- 78 of 81 --

Technical Implementation & Live Examples
Client Documentation
Widget Showcase
Widgets
Table
79

-- 79 of 81 --

Usage
Tables are used to display Business Objects in an ordered structure to help users get a quick
overview.
TIP
Use the table if:
• The business objects have many properties
• You need a full overview over the business object properties
• Users need to quickly scan all the data entries
IMPORTANT
Don’t use the table if:
• The data only consists of a smaller set of properties (<6). To display these
objects we recommend using Cards for a better user focus
• The data is image-heavy. For this purpose we recommend using the
Gallery.
Use Cases
Tables are often used as part of common enterprise UX patterns, e.g:
We provide different UI solutions and approaches for the shown user flow above. For further
information please take a look here:
Working with Business Objects
Currently Supported Features
Feature Widgets Engine (mgm internal only)
Pinning Example Overview-Engine
Paging Example
Filtering Example
Multiselection & Bulk operation Example
Inline actions Example Form Engine
Expandable Row Example Form Engine
80

-- 80 of 81 --

Feature Widgets Engine (mgm internal only)
Simple Grouping Example Not supported yet
Row Highlighting Example
Drag&Drop Example
Multiline Header Example
Column grouping Example
Features in progress
The table is a central widget in enterprise applications. We are aware that we are not supporting
every needed feature yet but currently we are already working on the following features:
• Merged Columns
• Complex grouping
81

-- 81 of 81 --

