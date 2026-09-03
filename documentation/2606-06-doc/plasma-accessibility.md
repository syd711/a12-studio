# plasma accessibility

Accessibility
Introduction to Accessibility
Accessibility (or short A11Y) is the concept of making a product or service accessible to everyone -
including people with disabilities or special needs.
For web accessibility, this means that people with the widest possible range of abilities, operating
within the widest possible range of situations, are able to perceive, understand, navigate and
interact with the web. This includes both "direct access" (without assistance) and "indirect access"
(with the help of assistive technology such as screen readers).
Accessibility in A12 Projects
If an A12 project wants to be accessible, it doesn’t need to develop every accessibility function from
scratch. Many of those functionalities are already provided by A12 and are working out-of-the-box.
However, there is still a wide spectrum of application-specific requirements which cannot be
delivered by A12 and have to be done by the project itself.
With this documentation, we want to support the projects and their members by giving detailed
information on A11Y requirements and on how to meet them. These requirements not only concern
all working areas (design, modeling, development and testing), but also all levels of the application
(from application frame to single content element).
How accessibility is reflected in the working process of a project
A short overview of some of the most important A11Y requirements is given below. For more
detailed and specific information, please consult the sections Basics, Modeling, Developing and
Quality Assurance.
Creating an accessible project starts with some basics, since many functionalities for accessibility
have to be defined individually for the project before they can be modeled or developed and others
have to be kept in mind throughout the whole working process.
The next step towards an accessible project is the design, as
• a consistent structure of the application and its elements
• a responsive behaviour of the components, as well as
• accessible color and contrast themes
are fundamental requirements for accessibility.
On the modeling side, there are requirements such as
• alternative texts for images
• meaningful labels for input fields and
1

-- 1 of 67 --

• marking mandatory fields.
The most extensive part of the work towards an accessible project is of course to be done in the
development process. On a global level, there are requirements such as
• document titles
• multiple means of navigation
• focus behaviour
• heading hierarchy
• correct syntax.
But also on a very granular level, every component needs to be given the necessary semantic
information (e.g. by setting the required properties), so it can be used with keyboard and correctly
interpreted by screen readers.
Last of all, accessibility requirements also need to be integrated in the quality assurance process to
make certain that all components are interacting in such a way that the whole application and
every part of it is accessible. Especially before Go-Live a thorough accessibility test is necessary as a
reference for the Accessibility Statement.
Why Accessibility is Important
Target Groups and their Requirements
In order to have a better understanding of accessibility and the resulting requirements, it is helpful
to bring to mind the different impairments that might affect the users of applications. For our
purposes, these can be roughly summarized in the following groups:
2

-- 2 of 67 --

The Four Principles of Web Accessibility
The international accessibility standards are grouped into 4 basic principles, which represent the
different needs of the users:
Perceivable
• Text alternatives for non-text content (e.g. graphics)
• Alternatives for multimedia and animations
• Clearly structured content
• High color contrasts
• Information not only conveyed by color differences
Operable
• Keyboard Accessible
• No time restrictions
• No blinking or flashing content, animations can be disabled
• Easy navigation and orientation
• Different input modalities possible (e.g. voice recognition)
Understandable
• Readable and understandable text
• Predictable user interfaces
• Assistance to avoid and correct mistakes
Robust
• Compatibility with different user tools (browsers, devices, assistive technologies, …)
• Clean and valid HTML code
• Programmatically determinable content (interactive elements, status messages)
Standards and Regulations
The most important international standard for web accessibility are the Web Content Accessibility
Guidelines (WCAG) published by the World Wide Web Consortium (W3C). They are a set of
recommendations for making web content more accessible and can be found here: WCAG 2.2.
In the EU, the Web Accessibility Directive (EU) 2016/2102 determines that all public web sites and
apps in the EU have to be accessible by making them ‘perceivable, operable, understandable and
robust’. It requires:
3

-- 3 of 67 --

• an accessibility statement for each public website and mobile app
• a feedback mechanism so that users can flag accessibility problems or request access to
inaccessible content
• regular monitoring of public sector websites and apps by Member States.
The harmonized European standard for accessibility is set out in the EN 301 549 (especially chapter
9 for websites) and is based on the WCAG.
In Germany, web accessibility is standardized by the BITV 2.0 (Barrierefreie-Informationstechnik-
Verordnung), whose regulations are also based on the WCAG. It requires that websites and
applications shall
• comply with harmonized standards or parts of these standards
• provide an accessibility statement
• provide explanations in German Sign Language and Easy Language
One evaluation procedure based on the BITV 2.0 and EN 301 549 is the BIK BITV-Test, which
includes 98 comprehensive test steps (only available in German).
When developing accessible web applications, mgm A12 uses the criteria of the WCAG and the BIK
BITV-Test.
Basics
As we said above, many application-specific requirements for accessibility cannot be delivered by
A12 and have to be done by the project itself. Most of them are explained in detail in the respective
chapters Design, Modeling, Development and Testing.
However, in this chapter you will find a couple of features for accessibility which
• have to be defined at a general level for the whole application
• are important throughout the whole working process
• are not relevant for every project
• are fulfilled when using the A12 package but should be checked in the project
General definitions needed
The following features have to be defined in general for the whole application, preferably in the
concept stage, so that they can be modeled or implemented in a correct and consistent way later on:
4

-- 4 of 67 --

Requirement Description Technical Implementation
Document
titles
Every web page / view should have an accurate
and descriptive title. We recommend the
following structure for all document titles:
[ Name of current page ] - [ Name of web
application ]
Hints for defining document titles:
• The definition of "view" should be done in
the project.
◦ Example: In a master detail concept the
master view needs an individual
document title, but for detail view it is
unnecessary.
• The name of the current page can be the
following (depending on the structure of the
application):
◦ the selected main menu entry
◦ the selected sub menu entry or
◦ the main headline.
• The title should be unique, precise and not
too long
• Decorative elements (e.g. ~~, ===) should be
avoided
The specification of how the
document title should be set
needs to be done by the project
itself. The implemenation
should be done by the
developers → an example can
be seen in the Widgets
Showcase
Link text
format
Links to a file should inform about file type and
size within the link text to meet the user
expectations when opening / downloading the
file. Example link text: "Rules (PDF, 2MB)".
Static link texts for files should
be given to the modeler /
developer.
Ever-present requirements
The following general requirements have to be kept in mind throughout the whole modeling and
developing process:
5

-- 5 of 67 --

Requirement Description Technical Implementation
Alternative
texts for non-
text content
Every information that is provided visually,
must be accessible for visually impaired users as
well. This means above all that every non-text
content of an app must have a text alternative
that clarifies its purpose. For example,
information-bearing graphics as well as icons
and icon buttons must have tooltips/title
attributes which describe their function. Also,
information like when an input field is
mandatory should not be given only by a visual
highlight.
• Modeling: see the tables in
the chapter Modeling for
Icons, Inputs with
mandatory fields and other
• Developing: see the
description in General
Widgets
Labels for
inputs
Relations between different elements that
belong together should be clear to the user, for
example input fields shall have a label, so that
the user knows what kind of input one is dealing
with.
• Modeling: see the table in
the chapter Form Model for
Inputs
• Developing: see the
description in Widgets for
Data Entry
Focus For keyboard handling it is essential to have a
well-functioning focus behaviour so that the
users have access to all functionalities and that
they always know where they are.
This means that:
• Every interactive element should receive the
focus by tab key
• Every element that gets the focus by tab key
has a clearly visible focus design
• The focusable components receive focus in a
conclusive and understandable order
(especially when content is created
dynamically)
See chapter Focus for more
detailed information.
Optional application-specific features
The following three requirements are not relevant for every project, so please check if they are
important for your application:
6

-- 6 of 67 --

7

-- 7 of 67 --

Requirement Description Technical Implementation
Localization If a project is using different languages than
English, then these languages need localized
texts. The same goes of course for hidden texts,
which are necessary for screen reader users. For
German hidden texts, most localizations are
already available for the widgets:
▼ Expand to see localized texts in the widgets
Widget English Text German Text
Accordion "Open" /
"Close"
"Aufklappen" /
"Zuklappen"
Autocomplet
e &
Multiselect
"Clear text" "Eingabe
löschen"
Badge - Basic "Info
notifications"
"Warning
notifications"
"Error
notifications"
"Success
notifications"
"Infomeldung
en"
"Warnmeldun
gen"
"Fehlermeldu
ngen"
"Erfolgsmeldu
ngen"
Badge - Tiny "Info
notifications
available"
"Warning
notifications
available"
"Error
notifications
available"
"Success
notifications
available"
"Infomeldung
en verfügbar"
"Warnmeldun
gen
verfügbar"
"Fehlermeldu
ngen
verfügbar"
"Erfolgsmeldu
ngen
verfügbar"
Breadcrumb "Breadcrumb" "Pfadangaben
"
Collapsible
panel
"Open" /
"Close"
"Aufklappen" /
"Zuklappen"
In order to enable localizations
for non-English languages, the
language provider is needed →
see the description in
Developing.
8

-- 8 of 67 --

Requirement Description Technical Implementation
Time Limit If an application needs to have a time limit, then
the time counter must be stoppable or
extendable.
However, we recommend to not have a time
limit, as some users need more time to read the
content or to fill in data.
The time limit process needs to
be implemented by the project
itself.
Keyboard
documentatio
n
If the application implements special and
unusual keyboard shortcuts, this should be
communicated to the user, for example as an
accessible document (like PDF) or as an
accessible part / web page of the application.
The documentation must be
generated (written) and
presented to the user
(implemented in the
application) by the project itself.
Alternate
Access
A project with more than two or three web
pages / views, which are not linked sequentially,
should provide at least 2 different ways to locate
a web page within a set of web pages, for
example:
• Menu (main menu in header and sub menu
as sidebar are counted as one)
• Search
• Sitemap
• (Breadcrumb is not counted as an
autonomous way)
Background information: With more than one
way to navigate pages or views of a web
application, people are able to find information
faster. Users with screen magnifiers or screen
readers may find it easier to navigate by using a
search, rather than scrolling through a large
navigation bar. A person with cognitive
disabilities may prefer a table of contents or site
map.
Needs to be done via modeling
or developing by the project
itself.
Requirements fulfilled by the A12 package
There are a few general requirements which are fulfilled when using the A12 package.
Nevertheless they should be checked in the projects:
9

-- 9 of 67 --

Function Requirement Motivation
Mobile use The content and functionality of an application
should be available without limitations because
of the screen orientation (landscape and portrait
mode) or screen size of the user’s device.
For path-based and motion-based operations
(like swiping or shaking the phone) there shall
exist an accessible alternative.
Every interactive element / function shall be
possible to abort (not triggered by down-event).
→ Is provided by A12 within the responsive
design
There is a wide-ranging
multitude of mobile devices
and viewports that should be
covered to the best possible
extent. Also there are users
who are limited to a certain
screen orientation or who are
not able to use the touch as
precisely as others.
Mouse and
Keyboard
All functions and interactions shall be equally
usable with mouse and keyboard only. Every
interactive element should be focusable by
using the tab key. This focus and also the
mouseover effect on interactive elements should
be clearly visible.
→ A12 strives to fulfill mouse and keyboard
functionality for all widgets and features
Users who can’t use the mouse
should be equally able to access
the app with a keyboard.
Structure of
application
The application should be structured into two or
three main parts:
• Header with role=banner
• Main content area with role=main
• Footer (if existing) with role=contentinfo
If the application contains forms, the role=form
should be used.
Each of these roles should only be used once per
view.
→ Is provided by A12
This helps users to better
understand the overall
organization of the application.
People who are blind will be
able to jump the focus from
role to role with keyboard
navigation.
Forms need role=form for
semantic meaning and better
screenreader use.
10

-- 10 of 67 --

Function Requirement Motivation
Structure -
Heading
Hierarchy
A web page / view of a web application should
be well structured and organized into sections
with headings. These headings must have a
correct hierarchy (h1, h2, h3 …). The level of the
headings is automatically set by the Form
Engine, but it should be re-checked after project
deployment. For some widgets it must be set
manually.
→ Is generally provided by A12 but should be
checked by the project
This helps the users to
understand the overall
organization of the page
content more easily. People who
are blind will know when they
have moved from one section to
another and will be able to
jump the focus from heading to
heading with keyboard
navigation.
Syntax The HTML syntax of a web application must be
valid, meaning that it should not include any
structural mistakes such as missing start or end
tags or duplicate IDs. If the used HTML-code is
correct can be checked at
https://validator.w3.org/.
→ The A12 widgets pass an automated syntax
check, but a project should have a final syntax
check by itself
 This requirement was removed in the new
WCAG 2.2, but the European regulation EN 301
549, which is legally binding for public
authorities, is still based on WCAG 2.1 and will
not be updated anytime soon.
A clean code makes it easier for
browsers and screen readers to
interpret correctly a web
application.
Zoomed
application
The web application should not lose content or
functionality when the browser is resized up to
200 percent.
→ Is provided by A12 with a viewport based
responsive design
People with a reduced vision
should be able to zoom in on
the page / view and still have all
information accessible.
Accessibility Statement
The German regulation BITV (Barrierefreie-Informationstechnik-Verordnung) requires that all
websites hosted by public sector bodies must provide an accessibility statement on their website
which:
• shall be published in an accessible format and shall be accessible from the homepage and from
every page of a website
• shall contain comprehensive, detailed and clearly understandable information on the
compatibility of the website with the accessibility requirements under sections 3 and 4 of the
BITV
11

-- 11 of 67 --

• shall be based on an assessment of the compliance of the website with the requirements
specified in § 3 BITV, which can be carried out by a third party or the public body itself
• shall offer contact information so that users can give feedback
• shall be updated annually and whenever significant changes are made to the website
Template
We recommend to provide a static page (usually located in the footer) for which you might use the
text of the following template:
▼ Expand to see German version
Erklärung zur Barrierefreiheit
[Name] legt großen Wert darauf, seine/ihre Website[s] im Einklang mit den Bestimmungen des
Behindertengleichstellungsgesetzes des Bundes (BGG) sowie der Barrierefreien-
Informationstechnik-Verordnung (BITV 2.0) zur Umsetzung der Richtlinie (EU) 2016/2102
barrierefrei zugänglich zu machen.
Stand der Vereinbarkeit mit den Anforderungen
Die Überprüfung der Einhaltung der Anforderungen beruht auf
• einer am [Datum] / im Zeitraum von [Datum] bis [Datum] durchgeführten Selbstbewertung
anhand der Kriterien der BIK BITV-Selbstbewertung / EN 301 549 / WCAG 2.1.
• einer von [Name der Prüfinstitution ergänzen] am [Datum] / im Zeitraum von [Datum] bis
[Datum] vorgenommenen Bewertung durch [genauere Beschreibung des
Bewertungsverfahrens und Link zum Bewertungsbericht ergänzen].
Aufgrund der Überprüfung ist die Website mit den zuvor genannten Anforderungen
a) vollständig vereinbar [nur zutreffend, wenn alle Anforderungen der Normen oder
technischen Spezifikationen vollständig und ausnahmslos erfüllt sind].
b) wegen der folgenden [Unvereinbarkeiten] [und/oder] [Ausnahmen] teilweise vereinbar [nur
zutreffend, wenn die meisten Anforderungen der Normen oder technischen Spezifikationen mit
einigen wenigen Ausnahmen erfüllt sind].
Nicht barrierefreie Inhalte
Die nachstehend aufgeführten Inhalte sind aus folgenden Gründen nicht barrierefrei:
1. Der [nicht barrierefreie Abschnitte/Inhalte/Funktionen] ist mit dem Erfolgskriterium
[Erfolgskriterium aus der EN 301 549 mit Bezug auf die referenzierte WCAG 2.1 und Mangel
in nicht allzu technischer Form] nicht vereinbar.
(Beispiel: "Das Log-in-Formular der Anwendung für den Dokumentenaustausch ist per Tastatur
nicht vollständig nutzbar im Sinne der Anforderung XX nach XX")
12

-- 12 of 67 --

Optional:
Folgende Maßnahmen zur Erreichung der Barrierefreiheit sind geplant:
[Geplante Verbesserung und geplanter Zeitpunkt]
Datum der Erstellung bzw. der letzten Aktualisierung der Erklärung
Diese Erklärung wurde am [Datum] erstellt und zuletzt am [Datum] aktualisiert.
Barrieren melden sowie Informationen zur Barrerefreiheit dieser
Website
Sie möchten uns bestehende Barrieren mitteilen oder Informationen zur Umsetzung der
Barrierefreiheit erfragen? Für Ihr Feedback sowie alle weiteren Informationen sprechen Sie
unsere verantwortlichen Kontaktpersonen an:
Ansprechpartner für die barrierefreie Zugänglichkeit
[Behördenname
Anschrift
E-Mail:
Telefon:]
Durchsetzungsverfahren
Im Rahmen eines Durchsetzungsverfahrens haben Sie die Möglichkeit, bei der
Durchsetzungsstelle online einen Antrag auf Prüfung der Einhaltung der Anforderungen an die
Barrierefreiheit zu stellen.
Kontaktdaten der Durchsetzungsstelle
[Behördenname
Anschrift
E-Mail:
Telefon:]
Schlichtungsverfahren
(optional)
Wenn auch nach Ihrem Feedback an den oben genannten Kontakt keine zufriedenstellende
Lösung gefunden wurde, können Sie sich an die Schlichtungsstelle nach § 16
Behindertengleichstellungsgesetz (BGG) wenden. Die Schlichtungsstelle BGG hat die Aufgabe, bei
Konflikten zum Thema Barrierefreiheit zwischen Menschen mit Behinderungen und öffentlichen
Stellen des Bundes eine außergerichtliche Streitbeilegung zu unterstützen. Das
Schlichtungsverfahren ist kostenlos. Es muss kein Rechtsbeistand eingeschaltet werden.
Weitere Informationen zum Schlichtungsverfahren und den Möglichkeiten der Antragstellung
erhalten Sie unter: www.schlichtungsstelle-bgg.de.
13

-- 13 of 67 --

Direkt kontaktieren können Sie die Schlichtungsstelle BGG unter info@schlichtungsstelle-bgg.de.
▼ Expand to see English version
Accessibility Statement
[Name] places a high value on making its website[s] accessible in accordance with the provisions
of the German Act on Equality for Persons with Disabilities (BGG) and the German regulation for
accessible information technology (BITV) in accordance with Directive (EU) 2016/2102.
Status of compliance with the requirements
The verification of compliance with the requirements is based on
• a self-assessment conducted on [date] / in the period from [date] to [date] based on the
criteria of the BIK BITV self test / EN 301 549 / WCAG 2.1.
• an assessment conducted by [add name of assessment institution] on [date] / during the
period from [date] to [date] by [add more detailed description of assessment process and link
to test report].
Based on the review, the website is, with regard to the previously stated requirements,
a) fully compliant [only applicable if all requirements of the standards or technical specifications
are fully met without exception].
b) partially compliant due to the following [incompatibilities] [and/or] [exceptions] [only
applicable if most requirements of the standards or technical specifications are met with a few
exceptions].
Non-Accessible Content
The content listed below is not accessible for the following reasons:
1. the [non-accessible sections/content/features] is not compliant with the success criterion
[success criterion from EN 301 549 with reference to the referenced WCAG 2.1 and deficiency
in not overly technical terms].
(Example: "The log-in form of the document exchange application is not fully usable by keyboard
in terms of requirement XX of XX".)
Optional:
The following measures are planned to improve accessibility:
[Planned improvement and planned timeline]
Date of creation or most recent update of this statement
This statement was issued on [Datum] and last updated on [Datum].
14

-- 14 of 67 --

Report barriers: Feedback Contacts
Would you like to report existing barriers or request information on the implementation of
accessibility? For your feedback as well as all further information, please contact our responsible
contact persons:
Contact information for accessibility feedback
[Authority name
Address
E-mail:
Phone:]
Enforcement Procedure
As part of an enforcement proceeding, you have the option of submitting an online request to the
Enforcement Office for a review of compliance with accessibility requirements.
Contact information of the enforcement office
[Authority name
Address
Email:
Phone:]
Arbitration Procedure
(optional)
If no satisfactory solution could be found even after you submitted feedback to the above-
mentioned contact, you can turn to the Arbitration Service pursuant to Section 16 of the German
Act on Equal Opportunities for Persons with Disabilities (Behindertengleichstellungsgesetz -
BGG). The aim of the BGG Arbitration Service is to find out-of-court solution for conflicts on the
topic of accessibility between people with disabilities and federal public authorities. The
arbitration procedure is free of charge. No legal counsel needs to be involved.
For more information on the conciliation process and how to submit a request, visit:
www.schlichtungsstelle-bgg.de.
You can contact the BGG conciliation body directly at info@schlichtungsstelle-bgg.de.
These templates can be adapted to your project with regard to possible support descriptions for
special implemented functionalities (e.g. keyboard shortcuts).
Design
The visual styling of a website is a very important element of accessibility as it helps to support
people with color blindness, low vision and limited motor abilities. Besides, it is also relevant for
15

-- 15 of 67 --

people using the High Contrast Mode in Windows and user defined browser styles.
These are the core accessibility requirements for design that should always be fulfilled:
Color
The content of a page must be entirely perceivable without the need of colors. In addition, there
shall be no referring to color or other sensory characteristics such as shape, size or visual
location.
Therefore colors must not be used as the only distinguishing feature to:
• convey information (e.g. a form that uses colors to indicate required fields)
• indicate an action
• prompt a response
• highlight a visual element
This doesn’t mean that colors should not be used on a page, but that structural distinguishing
features should be added as well.
Examples for such supplementary features are:
• Icons or symbols
• Additional text
• Contour Style (e.g. solid, dotted, hard contour or soft/blurred contour)
• Contour Size (e.g. border, outline, box-shadow)
• Font Styles (bold, semi-bold, italic, underline, etc.)
• (Background-)Pattern
• Shape
• Adding a colored background, e.g. in case there is none in a default state of a component, but
one when hovering (this changes the shape).
• Other non-color attributes: E.g. an additional arrow shaped marker

Do:
 Don’t:
16

-- 16 of 67 --

Color Check Tools
Tools for testing a website by simulating color blindness are:
• the Windows setting "Color filters" (Start > Settings > Ease of Access)
• the Firefox AddOn "Let’s get color blind"
Contrast
The visual presentation of texts, graphics and interactive elements should fulfill a minimum
contrast ratio between background color and foreground color.
The minimal contrast ratio of 4.5:1 is required for:
• Text or images of text with a font size <24 px (or <18,7 px for bold text), including placeholders
The minimal contrast ratio of 3:1 is required for:
• Large-scale text or images of text with a font size of at least 24 px
• Interactive components without text, such as
◦ Icons of icon buttons
◦ Background color or border of inputs
◦ Background color or border of elements without text
◦ Check mark of checkboxes
• All different states of interactive components without text, such as
◦ Icons of selected or activated icon buttons
• Other elements which are required to understand content, e.g. icons without explaining text
No contrast ratio is required for:
• Inactive elements, except they provide information which is needed to understand the context,
etc.
• Decorative elements, e.g.
◦ Borders, in case the contrast ratio of the element’s background-color fulfills contrast of 3:1
• Text, which is
17

-- 17 of 67 --

◦ part of an inactive element
◦ purely decorative
◦ part of a logo or brand name
◦ part of an image with significant other visual content
◦ is not visible to anyone

Do:

Don’t:
Contrast Check Tools
The contrast ratio of two colors can be checked with the help of the following tools:
• Software (recommended): Colour Contrast Analyser
• Online contrast checkers, e.g.
◦ https://webaim.org/resources/contrastchecker/
◦ https://contrast-finder.tanaguru.com/
◦ http://colorsafe.co/
• Browser AddOns, e.g. WCAG Contrast Checker
Consistency
Components that have the same functionality or semantical meaning should be identified
consistently, for example the same icons should always be used for the same function.

Do:
18

-- 18 of 67 --


Don’t:
Other General Requirements
The following points are also requirements of BITV and WCAG that should be kept in mind in the
process of design specifications:
• Resizeable Text: User should be able to zoom up to 200%. Therefore avoid designs which might
result in issues like cutting off text.
• Text as Graphics: Important texts such as headlines, list entries or buttons must not be bitmap
images (e.g. jpg, png, gif). This is because users cannot adjust bitmap images in size and color
according to their needs.
• Reflows: Content and functions should adjust for a width of 320px (e.g. small devices) or when
zooming, so that the need of scrolling both vertically and horizontal can be avoided.
• Text Spacing: Text spacing needs to be adjustable for users. Therefore avoid specs, which might
result in issues like cutting off text.
• Flashing/blinking: Animations in which content flashes up more than 3 times within 1 second
should be avoided, as they are problematic for users with e.g. epilepsy.

Do:

Don’t:
19

-- 19 of 67 --

Interactive Elements
Interactive Elements such as buttons or links can have different states which need to be perceivable
by every user. There are the following requirements:
• Interactive Elements should be recognizable as interactive
• Readonly and Disabled:
◦ should be recognizable as inactive
◦ they can have the same design, but readonly texts that are necessary for
orientation/operation must fulfill a contrast ratio of 4,5:1
• For state changes and different states a contrast ratio of 3:1 is required, when the difference
is only made by color
• Hover and Focus:
◦ The focus shall always be visible
◦ Hover and focus can have the same design and must fulfill the contrast ratio of 3:1
(element:background)
• Checked: The state "checked" should have a structural distinguishing feature (e.g. a check mark
for checkboxes, a bullet for radio buttons)
• Links need a structural distinguishing feature (e.g. an underline)
• The pseudo-class :active is not recommended, since it can be confusing when the style of an
element is changing when you click on it

Do:

Don’t:
Modeling
For application-wide general requirements, please consult the chapter Basics. For specific
requirements within the modeling tools, see the following chapters:
General Requirements
20

-- 20 of 67 --

Feature Requirement
Content box
header
For every content box (= overall frame for the content area), there is a header
which can have a label. This label should always be set as visible.
It is not recommended to have an empty label, because this would lead to
syntax and semantic problems.
Technical background: The label always has role=heading and an aria-level
for correct semantic. An empty label induces an empty div-container with
role=heading and an aria-level.
Example in Form Engine:
Confirmation
texts
The confirmation dialogue should always have the label and the title.
Graphics and
Icons
All information-bearing graphics and icons (non-interactive) must have
tooltips which describe their function. This is set by the label with the option
"Hide Label" activated, which sets the title attribute in the HTML-code.
Example in Form Engine:
21

-- 21 of 67 --

Feature Requirement
Icon Buttons
(e.g. Row Actions,
buttons in
subheader and
footer, …)
If a button has only an icon and no label, then a title must be set for the button
which describes its function (e.g. "Delete" for destructive button).
Form Model
Requirements for Inputs
Feature Requirement
Labels All inputs shall have a label. If a visible label is not wanted by design, then a
hidden label should be used by setting the label and activating the checkbox
"Hide Label".
Mandatory fields If a field is mandatory (can be set in the Data Model), this information needs to
be given to the user by the label (e.g. asterisk) and by an explanation above
the form.
• When mandatory fields are marked: The sentence above the form informs
the user that the named mark is used for mandatory fields.
• When mandatory fields are not visibly marked: The sentence above the
form informs the user that ALL fields are mandatory.
Requirements for Repeats
22

-- 22 of 67 --

Feature Requirement
Icon Buttons for
Row actions
All interactive icon buttons shall have tooltips (title attribute) which describe
their function.
The five standard icon buttons in repeats already have default texts
("Edit"/"Bearbeiten", "Delete"/"Löschen", "Copy"/"Kopieren", "Up" and
"Down"/"Nach oben" and "Nach unten").
If you want to change the text of a repeat icon button, you can do it:
• for all buttons of one view which have the same function: Settings →
Repeat Default Button Labels
• just for the icon buttons for a particular repeat:
23

-- 23 of 67 --

Feature Requirement
Interactive Rows The interactive rows for repeats shall have tooltips (title attribute) which
describe their function. The text for the default row action Edit/View is
automatically the same as the text for the edit button. It is set as
"Edit"/"Bearbeiten" per default, but can be changed as described above for
icon buttons.
Columns resize This option is not recommended, as it might result in unnecessary horizontal
scrolling with e.g. browser zoom.
Column settings Fixed width:
It is not recommended to set a fixed width, as it might result in unnecessary
horizontal scrolling with e.g. browser zoom.
Pin direction:
Not recommended, because with browser zoom or on smaller viewports
important columns might be hidden (see example Overview Model)
Multiple repeats
in one view
If the view contains more than one table, then every table needs an individual
hidden label (aria-label). This is done by setting a label for the repeat and
selecting the option "Hide title".
Infinite Scrolling It is not possible for now to have an accessible table with infinite scrolling
because of the use of a third product library.
Specific Feature Requirements
Feature Requirement
Annotation If there is an element used in annotations which affects the frontend (HTML-
code) or layout of a view, then the output should be checked for accessibility
before going online.
24

-- 24 of 67 --

Feature Requirement
Attachment (File
Upload)
• Set label (otherwise there would be an input without label causing A11Y-
issue)
• Also, when used in repeats the label will be displayed as column header →
option "Hide Label" can be used:
◦ without icon: will set the label as hidden text in column header
◦ with icon: will set the label as title attribute and hidden text for the
icon in column header
Dependencies Modelers have to ensure, that dependencies follow the "reading order" → the
master field must be located (somewhere) in front of the changing content.
The field label or value of a master field should be self-explaining to
understand the dependency.
This is also true for dependent required fields.
Dynamic
Subtitles
We don’t recommend using content box subtitles which are added
dynamically after the user entered data, because users with visual
impairments might not notice the change.
Expressions /
Text
Beware of using HTML-code here, like <h1>, <img>, etc. as there are certain
things to keep in mind for accessibility. Tags like <font> are usually ok. In case
of uncertainty, ask an accessibility expert for advice. Examples of known
issues:
• heading hierarchy,
• title attribute for links leaving the page,
• title attribute for e-mail links (set as “(mailto:address@example.com 'E-
Mail title')”),
• HTML-syntax if <br>-tags are generated,
• mandatory label if used in repeats and
• mandatory alt attribute for images.
 The generated HTML-code should always be checked for accessibility
before going online.
Switch (Boolean) If there is no visible label, there shall be a hidden label (set label and activate
the checkbox "Hide Label").
Overview Model
Overview Tab
25

-- 25 of 67 --

Section Requirement
Columns Label The label for the column headers must always be set (usually it’s
already set in the Document Model).
If you only want to display an icon without text, then you have to
activate the checkbox “Hide Label”.
Pin Direction Please be careful with using Pin Directions for columns, as this might
lead to problems on smaller views or mobile devices.
You should only pin columns that are the most important and that are
rather slim.
Also, you should always check whether your table is still usable on
smaller viewports and doesn’t have the problems shown in the
example below.
Table with pinned columns on desktop view:
Same table with pinned columns on mobile view:
In the example, you can see that the pinning results in the following
problems:
• The first column is not sufficient for the identification of the entry
• The pinned columns take the entire available space so that
◦ the action column is not visible anymore and
◦ the other not-pinned columns are not reachable
Width It is not recommended to set a fixed width, as it might result in
unnecessary horizontal scrolling with e.g. browser zoom
26

-- 26 of 67 --

Section Requirement
Columns Resize The checkbox “Enable Columns Resize” should not be selected,
because when it is checked, the columns get a fixed width and the
responsive features of the columns are disabled.
Custom Actions Tab
Section Requirement
Row Action Set either a label or an icon and a title.
Context Menu The buttons of the context menu always need a label.
Subheader + Footer Minor + Major Buttons. Set either a label or an icon and a title.
Title for Interactive
Rows
Interactive rows should have a title, so that the user gets an information
about the row action on mouse-over on the row.
Model Settings Tab
Section Requirement
Labels The label must be set so that the main container box has a heading. An
empty label leads to syntax and semantic problems. It is possible to hide
the heading with the checkbox "Hide Label", but it is not recommended to
not have a visible heading.
Example in Overview Engine Showcase:
Non-interactive overview tables
If the overview table is not supposed to be interactive, meaning that no action is triggered by
clicking on a row, then this needs to be set in the code.
The non-interactive overview can be created by implementing a custom overview component
based on the client provided ViewViews.OverviewEngineView component, in which the
27

-- 27 of 67 --

onDocumentClick callback is not defined.
An example of the implementation can be seen in the Overview Engine (mgm internal only).
Tree Model
Tree Tab
Section Requirement
Columns Label The label for the column headers must always be set (usually it’s
already set in the Document Model).
If you only want to display an icon without text, then you have to
activate the checkbox “Hide Label”.
28

-- 28 of 67 --

Section Requirement
Node Types Row Actions Interactive rows should have a title, so that the user gets an
information about the row action on mouse-over on the row. You can
set this title in the settings of each node type in the section Title for
Interactive Rows.
Context
Menu
Actions
Actions in the context menu should not have only an icon, but also a
label. If the label is not wanted, then you must at least set a title. This
text will be used for mouse-over on the icon and as hidden text for
screen readers.
Print Model
The print engine generates documents in the accessible PDF/A format. This format supports the
"tagging" of all elements in the PDF document in order to make them available for assistive
technology such as screen readers.
The tagging is automatically done by the Print Engine so that the majority of the accessibility
requirements are already fulfilled by default.
However, there are a few rules that you need to comply when creating content.
Text Styles
Feature Requirement
Headings When you define the text styles for your headings, you need to select the
correct semantic information for them.
The main heading should have the semantic "Headline 1". All subsequent
headings should have a coherent structure (e.g. "Headline 3" as subheading of
"Headline 2").
Once you have defined these text styles, you can use them for adding headings
in the Segment or Section tab.
29

-- 29 of 67 --

Feature Requirement
Body Text When you define the text styles for body text, you need to select the semantic
"Paragraph".
The font size for standard text should be at least 12 pt so that the text is easily
readable.
Element Library
Element Requirement
Text Headings Use the correct text style for headings (see Text Styles).
Text
highlighting
Use as little italic, underline and color highlighting as possible,
because this makes texts harder to understand for people with
cognitive deficits and for screen reader users.
Alignment Don’t use text alignment "Justified", because it makes texts harder to
understand for people with cognitive deficits.
Color • When using other font colors than black (+ white background),
make sure that the contrast ratio is at least 4,5:1 between text
color and background.
• Don’t use only color to convey information (e.g. green for success),
because this information is not accessible to people with color
blindness and it will get lost in black and white print.
Lists Please be aware that semantically correct lists cannot be created yet
with the Print Engine.
Image • Use short, precise alternative texts which convey the important
information of the image.
• Info graphics need more extensive alternative texts which
describe all the important information.
• Don’t use images that contain important text (exception: logos), as
the text might not be perceivable for users with visual
impairments. Use the text element instead.
Table • Use the table only for real table use cases, not for layout purposes.
• Don’t select the checkboxes for "Hide Label" or "Hide Header",
because the column headers are important for screen reader
users.
• Make sure that the columns are wide enough so that the words
have enough space and do not break onto the next line in the
middle of the word.
30

-- 30 of 67 --

Element Requirement
Table Layout It’s not recommended to use this element.
Instead, you can use several text elements and arrange and style them
as you wish.
Charts The charts are not accessible yet.
Developing
General Requirements
There are some general accessibility requirements which need to be done by every project. The
following are the most important ones in the developing process:
Function Requirement Motivation
Alternative
Image Texts
Every information-bearing image shall have a
good alternative text. Usually this text should
not contain more than 80 characters, only
images with a high level of information (e.g.
charts) should be described more detailed.
Decorative images which do not contain any
relevant information, should have an empty alt
tag (alt="").
→ Needs to be done and customized by the project
itself
Blind and visually impaired
users want to know about the
content and information of an
image in the same way as
sighted users.
With an empty alt tag, the
image is ignored by the screen
reader instead of reading the
image name, so that the user is
not disturbed by irrelevant
information such as the file
name of the image.
Text
Formatting
If a certain text or word should be highlighted in
layout (bold, italic), then use
• <strong> tag for bold text
• <em> tag for italic / emphasized text
When the author of a text
intends to highlight things by
using bold or italic layout, blind
users should be able to get this
information as well as sighted
users with the help of screen
readers.
31

-- 31 of 67 --

Function Requirement Motivation
Document
titles
Every web page / view should have an accurate
and descriptive title. We recommend the
following structure:
[Name of current page] - [Name of the web
application]
For more information on how to define
document titles see chapter Basics.
→ This needs to be implemented by the project
itself, an example can be seen in the Widget
Showcase
Every web page / view should
have an individual, accurate
and descriptive title, so that
different pages can be easily
distinguished (e.g. in browser
tabs, Google search results, for
bookmarks).
Localization For non-English languages the localization shall
be used also for accessibility hidden texts. These
are important to provide necessary information
for screen reader users and are implemented
firmly in the widgets.
For all German and English default hidden texts
no customization is needed: For specific
invisible texts, widgets provide the React
component "HiddenText". Other texts are used
in title attributes or as aria-label to provide a
name for an element so it can be read by screen
readers.
→ A project needs to use the language provider
given by A12, see Widgets Showcase →
Accessibility
When a project has other
screen languages than English,
then the hidden texts for blind
users shall be localized, too.
32

-- 32 of 67 --

Function Requirement Motivation
Main
Language
The main language is specified in the HTML
DOM for the whole application and it changes
accordingly if the application provides a
function to switch the language.
How it is done in the A12 client:
config.store.subscribe(() => {
const htmlElement =
document.querySelector("html");
const { language } =
LocaleSelectors.locale()(config.store.ge
tState());
if (htmlElement !== null &&
htmlElement.getAttribute("lang") !==
language) {
htmlElement.setAttribute("lang",
language);
}
});
→ This needs to be implemented by the project
itself, an example can be seen here: Client Sample
Application (mgm internal only)
Focus
Visible and Invisible Focus
For keyboard handling it is essential to have a visible focus indicator, so that the users always know
where they are. This means that every element that gets the focus by tab key must have a clearly
visible focus design. The most common focus indicator is a visible border displayed around the
focused user interface.
However, there is also a second type of focus which is not set by the user, but by the application.
This happens for example when a modal opens or a detail view is opened next to master view. The
focus is needed there as well, so that every user will recognize what is happening. But in contrast to
the focus set by tab key, the focus set by the app can be invisible - depending on the concrete use
case:
• If the focus is set on an interactive element (e.g. input field, button) → the focus has to be visible
• If the focus is working like a jump mark to an HTML element (mostly the title-element or the
outer container of a widget) → the focus can be invisible
The invisible focus is especially helpful for screen reader users, because when content (e.g. a
message box) appears after an interaction, the focus will be set in the box (more precisely on the
33

-- 33 of 67 --

div-element or the headline of the box) and the screen reader will start reading the content of the
box. The next tab key will then set a visible focus on the next interactive element within the content
container.
Technical hint: In Chrome the invisible focus gets a visible outline by the browser specific style
itself. This can be prevented by setting the outline=none via CSS. This was also done for the
example of Master Detail Pattern, see Focus Concept for Certain Patterns.
Examples
Invisible Focus Visible Focus
• Pop Up Menu when the List is opened
(same for Multilingual Field)
• Menu when the sub-menu is opened
• Modal Notification
• Modal Overlay
• Toasts
• Tooltip
• Filter Selector → Focus is set into first
input field, input field has focus design
Focus Order
When keyboard users navigate sequentially through web content, the focusable components should
receive focus in a conclusive and understandable order. This is especially important when content
is created dynamically.
Also, usually only interactive elements should receive focus by tab key.
Linearization
The visible order of elements on a page or view should be represented in the HTML DOM. There
may be different orders that reflect logical relationships in the content (like a table that can be
navigated cell by cell or row by row), but in general the tab order should follow the language
reading order left to right. This means for example that when there are two boxes floating side by
side, the left one should get the focus first, then the right one and after that elements below the
boxes.
Focus Concept for Certain Patterns
The general rule for the focus change after any event of trigger action is that the focus should go to
the content which is newly displayed, so that screen reader users will notice the change. If this
content is closed again, the focus should go back to the initial trigger element. A more detailed focus
concept is provided for the following patterns:
34

-- 34 of 67 --

Engine to Engine: Master Detail
Rule of Master Detail Pattern:
There is a button or another interactive element to open the next pane. After this action is
triggered, the new pane opens and the focus is set on top of this new content in order to start
reading and tab navigation from there. When the new pane is closed, the focus is set back to the
trigger element and has a visible focus to show the user position.
Example: Master Detail Managed in Widgets Showcase
Each view should have an id to get the focus behavior.
You can see the usage in the Widgets Showcase: Master Detail Managed
For the different trigger actions, the focus concept is the following:
Trigger Action Focus Behaviour
Add The focus will be on the added view. The id of the trigger open element should
be provided onNext, so that this trigger element will be focused when
removing the added view.
Remove The focus will be on the trigger open element if its id is defined, otherwise, the
last visible view will be focused.
Example: Master Detail Pattern used in the A12 Sample Application
The Master Detail Pattern is used between Overview Engine and Form Engine.
The interactive rows and / or edit buttons of the master can be focused and get the visible focus
indicator. When a row is selected (Overview Engine) the detail view opens (Form Engine).
An example implementation of this concept can be seen in the A12 Sample Application: Client
Sample Application (mgm internal only)
For the different trigger actions, the focus concept is the following:
Trigger Action Focus Behaviour
Edit With Enter on a row or on an edit button in the Master view, the focus should
be on the start of the newly opened content (= Detail view).
When the Detail is closed (e.g. with cancel or submit button), the focus should
get back on the row respectively edit button in the master that was selected.
(Exception: When the Master view has a pagination and the edited or newly
added entry is not shown in the actual view because it was placed on another
page, the focus should be set onto the Master table instead.)
35

-- 35 of 67 --

Trigger Action Focus Behaviour
New When a new entry is added, the focus also goes to the opened Detail view.
• When hitting Cancel, the focus goes back to the Add button.
• When hitting Submit/Apply, the focus goes to the newly added row in the
master. (Exception: see Edit)
Overview Engine: Delete
Trigger Action Focus Behaviour
Delete When an entry in the master is deleted, the focus is set to the next row (or to
the next interactive element if the deleted row was the last entry).
If the deletion process is canceled, the focus gets back to the trigger element
(delete button).
Form Engine: Repeat
The focus behavior is working automatically when the A12 feature Repeat is used. If there are
similar use cases without the usage of Repeat this pattern must be applied by the project itself.
Inline Repeat
Trigger Action Focus Behaviour
Add / New The focus shall be set to the new row. The focus itself shall not be visible, but
the row should have a highlight design. The next focus (by tab key or arrow
down key) goes to the input field.
Edit (Nothing special here)
Delete After deleting an entry, the focus is set on the table. When the deletion process
is canceled, the focus is set back to the trigger element / delete button and is
visible.
Cancel When an action is cancelled, the focus is set back to the trigger element.
Detached Repeat
36

-- 36 of 67 --

Trigger Action Focus Behaviour
Add / New For all of these actions, the editing form is shown in a new view (or page),
where the focus should start on top of the form.
Save:
• After the editing form is closed, the focus shall be set to the new / edited
element respectively row
• If the row is non-interactive it has highlight design, if it is interactive it has
focus design
• The next TAB would go to next interactive element (e.g. button in the row
or after table) → normal tab order
Cancel:
• After the editing form is closed, the focus is set back to the trigger element,
(e.g. the ADD / NEW button, the EDIT button or the interactive row) →
normal tab order from this element on
Delete See Inline Repeat above
Cancel See Inline Repeat above
Pagination
After a new page is selected from the pagination, the focus should go to the content which is newly
displayed. The first TAB should set the focus on the first interactive element on the new content. If
there are no interactive elements on the new content, the focus should follow the normal tab order
that includes interactive elements before the pagination and the pagination itself.
This is supposed to work out-of-the-box for certain pattern like Repeat using pagination, but it
should definitely be checked when content and pagination are implemented - especially if the
pagination is not used with a table. If the focus is not set automatically as described above, the
focus shall be set on the wrapper by the project. See an example with focus on table wrapper while
using pagination: Table Expandable in Widgets Showcase
The pagination tab order should be as following:
• Focus starts on the First Page button
• TAB → Focus is on the Previous Page button
• TAB → Focus is on the input select
◦ Works as normal input select
◦ After choosing a page: select page and set focus on the top of the new page (or new content)
• TAB → Focus is on the Next Page button
37

-- 37 of 67 --

• TAB → Focus is on the Last Page button
Widgets
Most of the A12 widgets provide a good accessibility automatically. But sometimes the use of certain
properties and adjustment is needed. For some widgets there is an accessibility version provided in
the Widgets Showcase → always use this version. Otherwise, the basic version is accessible.
 It is recommended to use the A12 widgets instead of manually developed
components. If a component must be developed manually, it should get checked
for accessibility by the A11Y experts.
For the following widgets, there are properties provided to make them accessible. The values of
some properties will need further customization depending on the function of the widget.
General Widgets
38

-- 38 of 67 --

39

-- 39 of 67 --

Widget Property /
Adjustment
Description
Button
40

-- 40 of 67 --

Widget Property /
Adjustment
Description
Content opening in a pop up → use A12 widget Pop-Up
Menu
Content collapsing / de-collapsing → use A12 widget
Collapsible Panel
Icon Button: title For reading and mouseover of an Icon Button it needs a
good name similar to the Icon widget. This is done by the
title attribute. An additional requirement is to have also
an aria-label so that the Icon Button can be used by voice
control software. Therefore, the title attribute and aria-
label are mandatory for accessibility and shall always have
the same value. To set it to the Icon Button use the title
property and it will be used as title attribute and aria-
label.
Special: If the Icon Button is used to open another content
connected to the button, then the name needs to reflect this
and change depending on whether the content is open or
not. See also widget Button.
Examples of widgets where the name must be given by the
title property and may be changed depending on
functionality:
• Table → Expandable → button to open / close the
editable content of table row
• Charts → Hideable Legend → button to open / close the
legend
• Split View → Button to split / not split the contentbox
• Modal Notification → Close button
• Callout → Close button
• Resize and Drag Container → Draggable Button Element
• Simple Toggle Button
41

-- 41 of 67 --

Widget Property /
Adjustment
Description
Icon title For blind users using the screen reader and as a hint while
mouseover of an icon it needs a good name. The name is
then displayed as a tooltip by the title attribute and used
as non-visible text in the HTML-code. The value can be set
by the property title.
Link Open / Expand
content:
aria-haspopup, aria-
expanded, role
See "Button → Open / Expand content": If a link opens a pop
up menu or expands content, it shall follow the same rules.
Pop Up Menu triggerButtonTitle When the pop up menu is used more than once per view /
page they should have individual names for mouseover
effect and for screen readers. Therefore, a customized title
for the origin trigger button is needed. It can be changed
with the property triggerButtonTitle and should be localized
if needed.
As default, it will be "Open menu" in English, and "Menü
öffnen" in German.
HeaderTrigger If you use the HeaderTrigger widget, you need to decide if
the hidden screen reader text "selected" is suitable in your
use case or not. If you use the widget e.g. for a language
switch, then you can keep the default of the hideHiddenText
property. If you use it e.g. for a user menu, then you should
set the property to true, so that the hidden text will be
removed.
See Widget Showcase Popup menu for examples.
Widgets for Layout
42

-- 42 of 67 --

Widget Property /
Adjustment
Description
Collapsible
Panel

Deprecated,
please use
widget
Typography
instead.
role and ariaLevel If the collapsible panel is used as section header it should be
also a header on a semantic meaning. Therefore, it needs
the role=heading, which is provided by the use of the
property role. In addition, the property ariaLevel is needed
to get the heading in the correct hierarchy by setting a level.
Content Box ariaLabel Sometimes it is needed to name a certain content box for a
better screen reader reading, e.g. for the master detail use
case. To write a special name which is read to the content
box, use the property ariaLabel.
ariaLevel The heading hierarchy of an application is very important.
The content box headline has the role=heading by default,
but needs to have a logical heading level. It can be set by the
property ariaLevel.
Attention: The form engine is calculating the heading level
automatically and there is no need to manually change the
property ariaLevel.
headingTitle and
ariaLevel in
footerProps
If the footer of a content box contains content (like
interactive elements such as an submit button), it needs a
heading as well in order to provide a good
programmatically determined structure. By default it has
the invisible heading "Action Section" (in German
"Aktionsbereich") which is read by screen readers.
But if more than one content box footer is used on one view
/ page, then it is necessary to have individual invisible
headlines for them. You can customize the footer headline
by using the property headingTitle from footerProp.
The hierarchy of the footer headline should be set
automatically by the engines, like for the contentbox
header. But it might be necessary to set the level of the
hierarchy manually. This can be done with the property
ariaLevel from footerProps.
43

-- 43 of 67 --

Widget Property /
Adjustment
Description
Layout Grid
used as
Gallery
role It is recommended to use the property role for the layout
grid widget when it is used as a gallery. With this property,
the layout grid / gallery will be set as list (semantic meaning
of list, no design change).
For further info see typedoc instruction in the widget
showcase for LayoutGridTemplateProps and RowProps.
Responsive
Image
Container
alt To provide alternative texts for images, fill the alt attribute
by using the property alt.
Card widget
The card widget is using the responsive image container
widget and therefore should also use the property alt to set
a good alternative text.
Widgets for Navigation
Widget Property /
Adjustment
Description
Breadcrumb currentPage The active page is marked as "current" by using the
property currentPage on the last breadcrumb element
which generates aria-current=page as attribute on the last
breadcrumb item.
Menu -
Flyout Menu,
Sliding Menu
mainContainerLabel Use the property mainContainerLabel for the main
navigation only to give it a semantic name. This is
recommended for blind users using the screen reader. Set
the aria-label with text in all languages you need, for
example:
• English: "Main navigation"
• German: "Hauptnavigation"
title If a menu item contains only an icon and no text, it needs a
name (set by title attribute and aria-label) to be read by
screen readers. This can be done by the title property.
44

-- 44 of 67 --

Widget Property /
Adjustment
Description
Menu -
Sliding Menu
id Each menu item needs an id so that the keyboard focus will
get back on the parent item when collapsing its sub-menu.
These ids will automatically be generated when you give the
Sliding menu an id.
However, if you customize the menu items in a way that
their labels are not plain text anymore, you need to
manually give each menu item an id.
If you are unsure, let an accessibility expert test it.
onTabOut When the Sliding Menu is expanded, it shall have a tab
circle between the end of the menu and the button to
collapse/expand the menu.
• The function set by property onTabOut will trigger when
the user presses the tab key to move the focus out of the
last menu item. After the last menu item, the next tab
goes to the sliding menu button to collapse/expand the
menu.
• The project itself must handle it to move the focus on
the button to collapse/expand the menu when onTabOut
was fired.
Tab Panel id, title The Tab Panel needs an id and each tab needs its own title.
This way, the title of the tab panel will be also read when
navigating into the content of the selected tab panel.
Badge When you use a badge, you need to set the id of the badge as
value of the ariadescribedby property, so that screen readers
can read the badge’s information.
Widgets for Data Entry
45

-- 45 of 67 --

46

-- 46 of 67 --

Widget Property /
Adjustment
Description
All inputs
47

-- 47 of 67 --

Widget Property /
Adjustment
Description
• This is for text affixes or counter or other affixes as
additional information and NOT for interactive affixes
like buttons. Buttons, e.g. date picker or "clear all"
button, shall not be linked to the input.
keyboard use If the input has a function besides normal typing or
selecting an option, this function must be accessible by
mouse and keyboard likewise. For example the input like
text line can have a functionality triggered by a double click
with the mouse. It is important that this functionality is also
available by keyboard. This is fulfilled:
• when the double click is only a shortened way for
functionality which is available anyway (e.g. by a menu)
or
• when there is a keyboard short cut which is to be
defined by project.
errors and warnings Error and warning states of an input can have a different
color. For accessibility it is required that they need to be
indicated not only by color, but also by a validation message
or icon or anything else to indicate the invalid field.
Autocomplet
e
ariaDescribedby The Autocomplete widget is using a special dropdown list
functionality. Therefore, it is needed to link the label and all
other information belonging to the field onto the dropdown
list. This way, the label and other information are read by
screen readers when the focus is in the input, has value and
the list is opened. The linking is done with the property
ariaDescribedby which needs the IDs of the following
elements:
• <label> → premise: set property id
• validation message (error and warning) → premise: set
property id
• validation tooltip (error and warning) → premise: set
property id
• tooltip which is not validation (information etc.) → ID
has to be set manually, since it is an addon passed in via
the property addonAfter
See Widget Showcase for more information and example:
Autocomplete
48

-- 48 of 67 --

Widget Property /
Adjustment
Description
Checkbox title When the label of a checkbox is not visible on the screen the
title property can be used to have a visible tooltip for
mouseover effect on the checkbox.
ariaControls for
indeterminated
checkbox
To get a connection between the parent checkbox and the
belonging child checkboxes it is needed to link the IDs of the
child elements to the parent element with the attribute
aria-controls. This connection is programmatically
determined and can be read by screen readers. The value of
ariaControls has to contain the list of controlled checkbox
IDs.
Custom
Select -
Horizontal
Mode
hidden text or
placeholder
When used in horizontal mode, the custom select should
have a hidden text or placeholder which instruct the user to
use the arrow keys left/right/up/down.
File Upload placeholderIconTitle With the property placeholderIcon a certain preview icon
can be set for the file upload widget. This preview icon shall
get a name which is read by screen readers, too. The name
can be set by using the property placeholderIconTitle.
id If the widget FileUpload is used customized instead of the
widget DefaultFileUpload, then it is necessary to use the id
property, which will create IDs for all elements inside the
file upload (e.g. label, tooltip) and link them to aria-
labelledby and aria-describedby attributes. See also Widget
Showcase.
Plugin Editor adding customized
buttons:
createButtonGroup
and own properties
(including title)
The default buttons in the Plugin Editor already have
hidden texts and localization. If a project wants to
customize button groups or use other than default buttons
(e.g. Mark Text button), it needs to use createButtonGroup or
createBlockButton and pass its own properties (including
title).
Radio Button groupDOMProps If the radio button group is set as mandatory this must be
given also in a way to be programmatically determined, for
example to be read by screen readers. With the property
groupDOMProps the attribute aria-required=true must be
passed into the radio group wrapper.
49

-- 49 of 67 --

Widget Property /
Adjustment
Description
Tag Input keys The keys to create a tag is set by the property keys. The
default is to press "," (comma key) for creating a tag. For
accessibility it is recommended to also set ENTER as a key to
create a tag to meet user expectations for keyboard
handling.
ariaLabelledby The Autocomplete and Tag Input widgets are using a special
dropdown list functionality (see table entry Autocomplete
above for details).
The only additional requirement for the Tag Input is to also
link the ID of the helper text (premise: set property id).
See also Widget Showcase Tag Input for further information
and example.
Widgets for Data Display
Widget Property /
Adjustment
Description
Badge title There are default invisible texts and texts shown on
mouseover. The invisible texts are necessary to detect the
meaning of the badge element read by screen readers.
These texts can be customized with the property title.
Badge used on Icon Button
The icon button widget uses the attribute aria-label for
reading by screen readers. Because only the aria-label is
read (the invisible text of the badge widget is ignored) the
aria-label must contain the information of the badge. This is
done by using the property title for the icon button widget
as well as for the badge widget.
It is recommended to set the same title for both widgets to
show the same text on mouseover.
Tiny Badge - contrast The A12 default colors for widget Tiny Badge don’t fulfill
contrast requirements (for information-bearing icons the
contrast ratio has to fulfill 3:1.). Therefore it is not
recommended to use them with A12 default colors.
50

-- 50 of 67 --

Widget Property /
Adjustment
Description
Card with interactive
elements
Depending on the number of interactive elements and the
desired hover effect, different card variants should be used.
See following decision tree:
Advanced Linked
Card: useLinkRole
If the Advanced Linked Card widget is used:
• The property useLinkRole for the ActionArea should be
set to "false" (in order to remove role="link")
◦ For the main target of the Linked Card widget, the
link widget should be used inside the first or main
card content container (depending on project
priorities)
◦ This link widget shall repeat the same link-target of
the Linked Card widget (CSS might be customized so
that there is no underline of the link text)
• All further interactive elements are added with Link
widget or Button widget in the card content container
Counter with Tooltip / with
Title
We recommend to use the counter with tooltip.
With Title: It is not recommended to use the counter widget
with title since the title attribute is mostly not accessible by
screen readers for elements which contain text. The title
attribute should be used only for graphical elements like
icon buttons.
hiddenDescription This property can be used to add a hidden description text
to counters, e.g. in the dual pane when using different types
of counters. This will be read by screen readers before the
counter value (e.g. "Deleted: [4 entries]"/"Gelöscht: [4
Einträge]"). We also recommend to use different icons for
different types of counters.
51

-- 51 of 67 --

Widget Property /
Adjustment
Description
List selected If the list has interactive list items which represent a
selection of options the selected option will be marked
visually with a checkmark icon. To differentiate
programmatically between the normal buttons (they have
aria-pressed=false as default) and the pressed button, the
attribute aria-pressed=true is needed. It can be set to true
with the property selected.
interactive list item
with button → not
recommended
For semantic reasons it is not recommended to use the list
item as interactive wrapper with other interactive elements
inside. Instead, the list item itself should not be interactive
and the main action should be made available via button or
icon button.
Pagination id The pagination is using a select field and therefore this must
be linked to the invisible label with the for-id-linking, see
chapter Widgets for Data Entry for more information. To
have this linking, the ID is to set with property id.
Status title If you are using only the icon without text, you need to set
the title property in the Icon widget.
color In flat theme, the warning variant is not accessible, because
the white text has not sufficient contrast to the orange
background.
Table & Tree
Table - Basic
readable text All content of a table shall be readable - by visible text or by
invisible text for screen readers. If a table contains any
graphical content, e.g. icon buttons, or content without
visible text, e.g. inputs, they must follow the general
accessibility requirements. For special types and use cases
of these widgets, see the table "Table and Tree Table" below.
Tooltip id If the tooltip belongs to an input it must be connected to it.
This is done by giving the tooltip widget an ID by property
id. See chapter Widgets for Data Entry and properties id and
ariaDescribedby for more information.
Table and Tree Table
The widgets Table and Tree Table are using the same API. Although not all use cases are provided
for Tree Table in Widgets Showcase, all properties and adjustments available for Table could be
used for Tree Table as well.
When the Table widget is rendered by the engines, it uses the Textoutput widget for the cell
content. There is no accessibility difference for using or not using Textoutput widget, both work the
same.
52

-- 52 of 67 --

Type or Use
Case
Property /
Adjustment
Description
Column
Header
without
visible text
hidden text If a table header cell doesn’t contain visible text it must
contain invisible text which can be read by screen readers.
This is especially important for tables with a column for
actions and no visible text or just icon buttons in the table
header. The column header text should be a generic word
like "Status" or "Action" if the column contains only status
icons or buttons.
First cell
contains
interactive
element
hidden text Button or other interactive element in 1st cell of a row:
If the first cell of a row contains only an interactive element
like a button, invisible text is recommended in front of the
button for proper focus and reading by screen readers. If
the row is interactive the differentiation between row
function and button function can be difficult. The invisible
text has to be modelled or implemented manually.
Interactive
rows
title If rows are interactive and trigger an action (e.g. edit, open
detail view, open modal, …), they need a title specific to the
interaction. This title is then shown by mouseover and read
by screen readers. To localize the title, see chapter
Developing → General Requirements → Localization.
For example:
• Table Select and Sort, see Table in Widgets Showcase:
The title should be "Selectable" and after selection
"Selected"
• Tree Table Selectable, see Tree Table in Widgets
Showcase: The title should be "Selectable" and after
selection "Selected"
• Master Detail pattern → table in the Overview: The title
should be "Open detail view" (example for German
would be "Detailansicht öffnen")
53

-- 53 of 67 --

Type or Use
Case
Property /
Adjustment
Description
Marked rows secondaryCellTitle,
cellStyling
A row can be marked, e.g. as secondary. This can not only
mean a secondary relevance but also a selection, pre-
selection or highlight. The marked row can be interactive or
not. This information which is seen by design shall be given
to the blind users as well with an invisible text inside the
first cell of a row.
With the property secondaryCellTitle the hidden text can be
set at the first secondary cell of the row with a default
localized content, see example in Widgets Showcase in
Table with Customizations.
For other use cases there are also default hidden texts, for
example the highlighted row. When a row is highlighted by
the property rowStyling the property highlightVariant with
value "success" will set a default localized hidden content,
see example in Widgets Showcase in Table with
Customizations and Tree Table Highlight:
• English: "Success"
• German: "Erfolg"
Customized hidden text
The default content can be overwritten with the property
cellStyling (the same implementation as useSecondaryColor).
Pro-tip: If the first cell contains also visible content, the
hidden text should be written with a comma to have a
breathing space of reading by screen readers, e.g.
"Success,".
54

-- 54 of 67 --

Type or Use
Case
Property /
Adjustment
Description
Name ariaLabel / hidden
text
To differentiate between different tables / tree tables or to
give them a name for a better understanding of their
meaning, it is necessary to set a name with the property
ariaLabel (for tables) or set a hidden text (for tree tables).
The default name for the Table widget is "Table" (en) /
"Tabelle" (de).
This is especially important when the table switches to the
card view and is then not a table-element in HTML-code
anymore → A good name should be given or the aria-label
should change.
Interactive rows
Tables and tree tables with interactive rows should have an
invisible information which informs blind users about how
to trigger the function, since rows are usually not
interactive.
For tables, this information can be added by using the
property ariaLabel. Example sentence:
• English: "You can trigger the row action with Enter on a
cell."
• German "Zeilenfunktionen können mit der Eingabetaste
in einer Zelle aktiviert werden."
For tree tables, you need to set the property id, which will
be used to create a default hidden text (identical to example
sentences above) and will be linked to the ariaLabelledby
attribute. It is also possible to link the id of another hidden
text or content with the arialabelledby property.
If you want to replace the default text by your own
customized text, you can do it by wrapping the
A11YResourcesContext (see Widget Showcase).
Row groups -
Widget Table
only
ariaLabel The title of the group shall be passed also as aria-label for
the group by property ariaLabel. Screen readers can then
recognize and read it as grouping.
55

-- 55 of 67 --

Type or Use
Case
Property /
Adjustment
Description
Sortable
column
header
with another
interactive
element
contentWrapperRole If the sortable column header is used together with another
interactive element such as a tooltip, the property
contentWrapperRole must be set to false. In addition, a
custom role should be passed to the other interactive
element via this property (e.g. button for the tooltip widget).
Background: The sortable header cell has role="button" by
default so that screen readers can recognize it as an
interactive element. But if there is another interactive
element within the cell, this leads to syntax errors. With the
property contentWrapperRole the role="button" " is
removed from the cell itself and instead the header name
gets the role=button and the other second interactive
element can get a role as well.
Table footer hasFootContent If the table footer is used to display content, the property
hasFootContent needs to be set to true so that the necessary
aria attributes are rendered as well.
With context
menu
Keyboard shortcut When the context menu is used for the rows, the buttons of
the menu should be the same for the right-click menu as for
the three-dot menu.
If this is not possible, then there needs to be a shortcut
implemented which opens the right-click menu (plus
documentation for this shortcut).
We don’t recommend to use the context menu for the
column header. If you really need to use it, then you must
provide a shortcut which opens the menu (plus
documentation for this shortcut) and you should let an
accessibility expert check it.
With Icon
Button inside
table body
title When the widget Icon Button is used inside the table, the
title attribute and aria-label must be filled just like it must
be done for alone standing icon buttons, see widget Icon
Button in chapter General Widgets.
buttonAttributes For the type of Table widget where an icon button is used to
expand editable row content, e.g. Table Expandable in
Widgets Showcase, the icon button needs aria-expanded
attribute set by property buttonAttributes. Also, the title
attribute should be changed depending on status. See
widget Icon Button in chapter General Widgets.
56

-- 56 of 67 --

Type or Use
Case
Property /
Adjustment
Description
With inputs hidden label If an input e.g. text line, checkbox or indeterminated
checkbox (used for multiselection function) is used inside a
table it needs a label as it is default requirement for all
inputs. See chapter Widgets for Data Entry.
For tables, most times the label is not visible and therefore
needs to be set as hidden label with text in all project-
languages.
Example for multiselection in a table with indeterminated
checkbox in table header:
• English: "De/Select all"
• German: "Alle auswählen / abwählen"
title Input in table header:
If a table contains an input without visible label in the table
header, e.g. indeterminate checkbox for multiselection, a
title to show on mouseover is needed. Use property title for
this → title shall be filled then with text of hidden label.
57

-- 57 of 67 --

Type or Use
Case
Property /
Adjustment
Description
Not
accessible
types
no recommendation Unfortunately there are types of Table and Tree Table
widgets which are not accessible for screen reader users. It
is NOT recommended to use them for projects that need to
support accessibility.
Column Group Table and Tree Table
Because of the complicated HTML structure of grouped
columns, the screen reader is not mapping table header and
cell content correct. At the moment there is no solution
known to provide accessible groups for column headers.
Editable Row Table
The use of the opened editable row is causing problems for
the screen reader NVDA so that the column names are
mapped incorrect to the cells. At the moment there is no
solution known for this problem.
Virtualized Table
Because of the complicated HTML structure of the table
body, the screen reader NVDA on Firefox couldn’t detect
columns and rows correctly. At the moment there is no
solution known for this problem.
Widgets for Feedback & Utils
58

-- 58 of 67 --

Widget Property /
Adjustment
Description
Global
Message Box
ariaLevel (default
value: "level 2"),
role (default value:
"heading")
By default, the global message box is used as a heading,
which is done by giving it the property role=heading. To
place it correctly in the heading hierarchy, it has also the
property ariaLevel, which is set on level 2 by default.
If these settings don’t match with the structure of a web
application, they can be customized:
• By changing the level of the heading → set a different
aria-level (>0)
• With no heading at all → without role and aria-level
• By changing the role (not recommended) → Please note
that this role must be an element which allows using
aria-level, see WAI ARIA - aria-level. Such a change
should be approved by the A11Y experts.
focusOnMount
(default value: "true")
The default use case for global message box is to be shown
as global information on a web application and when it
appears first it should get invisible focus. Users will then get
to know the message when the viewport is scrolled to the
message and screen reader starts reading there.
This functionality is set as default behavior by property
focusOnMount. If the global message box is used in a
different use case or the focus should not be shifted to it, the
focus behavior can be turned off by setting the property to
"false".
In case of multiple global message boxes, the focus should
be set only on the first box.
color In flat theme, the warning variant is not accessible, because
the white text has not sufficient contrast to the orange
background.
59

-- 59 of 67 --

Widget Property /
Adjustment
Description
Toast Header It is strongly recommended to always use a header for the
toast.
Connected Toast:
focusOnOpen
(default value: "true")
For the default use case (connected toast shown on a button
without changing the view), the focus is set automatically to
the opened content.
If you have a use case where the focus should NOT get into
the opened content, set the property focusOnOpen to "false".
Example: The connected toast appears on an input which
includes the content of the toast also in the placeholder text
of the input.
Toast Group:
type, activityID
Only the toast type "permanent" is recommended for A11Y,
because it gives the users enough time to read and use the
content (exception: when used in combination with the
notification center).
If you want the notification to disappear after a certain
activity (e.g. cancel, commit, or when leaving the view), it is
necessary to always provide an activity id for the activity to
which the notification should be bound.
Toast Group
stackable
When the stackable toast group appears, it doesn’t get the
keyboard focus, but it is still read by the screen reader due
to the property role="status".
For better accessibility, you should implement a keyboard
shortcut so that users can quickly reach the toasts. This
shortcut should be documented in the accessibility
declaration.
tabIndex for main
content
Only the permanent Toast Group is recommended for
accessibility, because it gives the users enough time to read
and use the content. When the toast is closed, the focus goes
back to the element that triggered the action. If there is no
such element, the focus will be set back to the main content
(with role=main). Therefore it is important that the main
content has a tabindex.
Typography ariaLevel It is necessary to use the property ariaLevel so that the
screen reader can give the information that this is a heading
and the level of the heading. These levels should have a
meaningful hierarchy and can be independent from the
level used for styling purpose.
60

-- 60 of 67 --

Widgets for Business Case
Widget Property /
Adjustment
Description
Chat alt for avatar If the avatar of the chat partner is a real image (and not
plasma icon) it needs a good alternative text. The alt
attribute should be used for
• telling the meaning of image or
• empty alt attribute for not reading image if it is not
meaningful.
The alternative text is to set with property alt which is part
of the AvatarProps.
Login Layout Logo The logo can have an image as child which needs a good
alternative text. This is done by the alt attribute which can
be empty if the image shall not be read and is only used for
decoration.
ariaLevel for
Headline
The heading hierarchy must be correct also for the login
page. By default, the aria-level is 1 for the headline. It can
be changed by using the property ariaLevel.
Message Box focusOnMessage
(default value: "true")
There are different use cases to show a message box and
sometimes it needs focus and sometimes not. If the message
box is generated by the web application it should get focus
so all users will be aware of this information. Otherwise
they might miss this information.
The focus is set as default behavior by property
focusOnMessage. If the message box is used in a different
use case or the focus should not be shifted to it, the focus
behavior can be turned off by setting the property to "false".
In case of multiple message boxes, the focus should be set
only on the first box.
61

-- 61 of 67 --

Widget Property /
Adjustment
Description
Validation
Bar
autoFocus (default
value: "true")
The validation bar shall get focus when appearing so that all
users will get the information about validation result. For
blind users the reading by screen readers starts there and
for all other users the viewport is scrolled up to the
validation bar to present it in an accessible way. This is
required for accessibility and is done as default focus
behavior. Any ways it can be turned off by setting the
property autoFocus to "false".
Mobile Use:
a11yTitleSupport
On the mobile view of validation bar there are shown only
the numbers and icons of errors or warnings. For reading
by screen readers on mobile devices the property
a11yTitleSupport must be set to "true" to have default
localized invisible texts for the graphics. This property is
recommended for only the Overview because the supported
texts let users know they should click on the validation bar
to see the Validation List or Detail.
For the Summary View, there are set default localized
invisible texts on each issue dependent on the validation
type. No property needed to have them.
Wizard title For the steps of the wizard, the title property should be
used, so that they are readable and get a tooltip when they
are truncated.
A11Y in Progress
Some widgets are still in the process of accessibility improvement. They are not recommended for
projects that need to support accessibility or they need a manual A11Y-proofed implementation.
Widget Status Quo
Charts The charts are not yet tested for accessibility. But it is already known that they
should have a good alternative text and should be presented with an invisible
table including the same information for blind users.
Comment The Comment widget with the types Single Comment, List Comment and
Comment Container is not yet tested for accessibility.
Quality Assurance
Quality assurance is a core element of achieving accessibility, which is why accessibility tests
should be integrated in the regular quality assurance process. The goal is to make sure that the
application covers as many user needs as possible and fulfills the requirements of legal regulations
(see chapter Standards and Regulations). And although the complexity of the accessibility tests
might seem very challenging at first, many of these tests can easily be executed by the project QA
62

-- 62 of 67 --

and don’t necessarily need an A11Y expert.
Testing levels
Every project is of course different, but on a general basis we recommend the following three
testing levels for quality assurance:
Ticket tests
The best way to ensure accessibility from the start is to include A11Y tests in the verification of a
ticket. Depending on the kind of change that was implemented in the ticket, the following should be
tested:
• Every frontend change → check if the functionalities are available with keyboard as well
• Every change of the UI → check if all works well with browser zoom of 200%
• If there are new icons/graphics → check if there are alternative texts
• If there are new sections or pages → check if the heading hierarchy is correct
• If there are new components or fundamental changes → check the application on mobile
devices
System tests / Regression tests
These tests are needed for a constant quality assurance of accessibility and should address as many
accessibility requirements as possible.
We recommend using the accessibility test suite in TMT which includes all standard accessibility
test cases and can be found here. If you are already using TMT in your project, then you just need to
request reading rights for the test suite.
63

-- 63 of 67 --

High-level tests
These complex and extensive tests are aligned with the criteria of the WCAG / BITV requirements
and should be executed by the A12 accessibility experts.
Usually, they are based on the BIK BITV self test, which consists of 98 test steps (only available in
German). These have been created by the BIK for their BITV test and are based on the EN 301 549.
The high-level accessibility tests are especially important
• before Go-Live, as reference for the accessibility statement
• as preparation for external tests (e.g. BIK BITV test)
Testing tools
Screen reader
A screen reader is a program which reads out the content of a website to the user.
The output of the screen reader is not based on the visible content, but on the content of the HTML.
The following screen readers are currently the most common worldwide:
• NVDA for Windows
• JAWS for Windows
• VoiceOver for MacOS and iOS
• TalkBack for Android
For our tests, the open source screen reader NVDA is the standard tool, as it is the most popular
screen reader and is also used by official BIK BITV testers. It can be downloaded here.
Navigation
For desktop, there are basically two ways to navigate a website:
• Use the down arrow key to navigate to each element on the page in turn (up arrow for back).
• Tab key to jump from one interactive element to the next (shift-tab for back)
On mobile, you can either swipe left to get to the next element (right for back) or you can touch the
element that you want to hear. If you want to trigger an action (e.g. click on a button) you need to
set the reading focus on the element and double tap. For scrolling down, you need to use two
fingers (TalkBack) or three fingers (VoiceOver).
How to use NVDA
Operation modes
When using NVDA, you have two modes of operation:
Browse mode: For normal navigation on a page — within this mode you can navigate normally
64

-- 64 of 67 --

with arrow keys and tab key, as well as use short cuts, but you can NOT type in data.
Focus mode: For fields where data can be entered — within this mode you can type in data, but you
can NOT navigate with the arrow keys, only with Tab, and also the short cuts usually don’t work.
Usually, NVDA automatically switches between the two modes, but you can also change them
manually by pressing Insert + Space.
Shortcuts
For faster navigation through a page, there are many keyboard shortcuts available, such as:
Function Shortcut
Next heading H
Next form field (Input,
…)
F
Next link K
Next table T
Next list L
Next list item I
Stop current reading Control
Stop speech mode Control + S
Exit NVDA Insert + Q
Settings and Tools
With Insert + N you can open the menu of NVDA.
In the settings you can change the reading speed and activate the visual highlighting of the
currently read out object.
In the tools the most important feature is the Braille Viewer, which displays in written form the
information that is currently being read out (and sometimes even additional information). On the
braille display, abbreviations and symbols are used for certain semantic information like "selected
radio button". An overview of these can be found here.
65

-- 65 of 67 --

Color contrasts
For checking the contrast ratio between background and foreground colors, you can either use the
tool Color Contrast Analyser (https://www.tpgi.com/color-contrast-checker/) where you can select
the color on the screen via pipette.
Or you can use one of several websites where you put in the color codes of both colors, for example
here: https://www.w3docs.com/tools/color-analyzer.
Another possibility is the browser extension WCAG color contrast checker, which lists all
contrasts of the page and also indicates where a contrast might not be sufficient.
If you are unsure about the contrast requirements, you can look them up in chapter Contrast.
Headings
The most efficient tool for testing headings and their levels is the browser extension headingsMap.
You can directly see all headings of a page and their hierarchy. The tool also indicates by red color
which headings are not in correct order (e.g. headings don’t start with level 1, incorrect order of
heading levels).
66

-- 66 of 67 --

67

-- 67 of 67 --

