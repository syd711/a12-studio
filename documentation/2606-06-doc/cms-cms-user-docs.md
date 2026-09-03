# cms cms user docs

Content Management System for
Content Managers
NOTE
This documentation belongs to an A12 Enterprise Component which is not part of
the Open Source offering (A12 Community Edition). Please feel free to browse the
documentation and learn more about how you can use this A12 component in your
project. Learn more about the benefits from an A12 Enterprise Subscription on the
Editions & Licensing page.
Introduction
The A12 Content Management System (CMS) allows content managers to create, publish, maintain
pages with their corresponding meta-data and content as well as organize pages in a hierarchical
structure. It provides components and API to render these pages and to use the page hierarchy to
render the equivalent navigation for end users.
The CMS is a lightweight application module that can be integrated into A12 applications. Please
refer to CMS developer documentation for detailed integration instruction. The CMS uses the
Content Engine and the Content Model Editor - lower level components, which can also be used for
other purposes, which are documented separately.
Key Concepts
Content Model
The Content Model is an UI model which focuses on presenting a web page or a section of a web
page within A12 application. It shares the same structure as other models in A12 platform with two
root fields: header and content. The header field includes metadata: modelType, id, modelVersion,
locales, labels, and annotations. The modelType for Content Models is set to "content". The content
field is nested and organized hierarchically, allowing for complex structural definition of layout
and web elements. This model ensures the consistency across different applications within the
platform, facilitating the integration and rendering of web content.
Content Engine
The Content Engine is designed to read the Content Model and render the corresponding elements
based on the defined structure and properties. It interprets the hierarchical content configuration,
which mimics the structure of HTML or React elements, and dynamically generates the visual
representation of the page. This allows content managers to create and configure web pages
without writing code, as the engine automatically handles the presentation of configured elements.
1

-- 1 of 9 --

Content Editor
The Content Editor is a web-based application designed to simplify the process of editing Content
Models within the A12 platform. Instead of dealing with raw JSON files, users can visually manage
and configure the structure and properties of their content elements.
It allows users to add, delete, duplicate, and move elements within the structure tree, providing a
dynamic and flexible content organization. The editor also features a real-time preview, enabling
users to see changes instantly.
Additionally, it allows for easy editing of each element’s configuration and properties, making
content customization straightforward. This robust tool integrates seamlessly into the A12 platform,
making it an indispensable asset for content managers aiming to create and maintain high-quality
web content efficiently.
Content Management System
The CMS is a broader concept compared to the Content Editor. It is designed to create, manage and
modify the hierarchical organization of pages and fragments. The CMS is also responsible for
managing the content in each page and fragment by using the integrated Content Editor.
Pages
Within our Content Management System (CMS), Pages are the central entities that define the
structure and content of your website. Pages allow users to manage web content efficiently,
ensuring a seamless and consistent experience. The CMS supports various page types and provides
detailed metadata to control the behavior and appearance of each page, making it a versatile tool
for content managers and developers alike.
Page Types
The CMS supports four distinct types of pages, each tailored to specific needs:
• Content Pages: These pages enable content managers to create and edit the actual content
displayed on a webpage. They are ideal for static content that needs regular updates.
• External Pages: These pages simply link to external websites. They are useful for directing
users to resources outside the main website.
• Custom Pages: These pages offer developers complete control over the page views, allowing for
extensive customization beyond the standard templates and configurations.
• Dynamic Pages: Designed for use within regular A12 applications, these pages support dynamic
content that changes based on user interactions or data inputs. (experimental)
Page Metadata
Each page within the CMS is associated with a set of metadata attributes that help define its identity
and behavior:
• Name: The unique identifier for the page from the user’s perspective. It helps users recognize
2

-- 2 of 9 --

and differentiate between various pages.
• Label: This metadata is displayed in the web browser tab title and the top navigation bar menu,
providing a clear and concise description of the page’s content.
• Path: A string that is concatenated with other paths from root pages down to the current page,
constructing the final URL for the page. This ensures a logical and hierarchical structure for the
website’s navigation.
• Sidebar: A configuration option that allows users to choose whether the sidebar should be
shown or hidden on the page. This is particularly useful for creating a more focused and
streamlined user experience.
Fragments
Besides pages, the CMS includes a powerful feature called Fragments which are reusable
components that can be created and updated similarly to content pages but are designed to be
embedded into other pages. The main purpose of fragments is to promote content consistency,
efficiency, and modularity across the website:
• Consistency: By using fragments, content managers can ensure that specific sections of content
remain consistent across different pages. This is particularly useful for headers, footers, and
other common sections that appear on multiple pages.
• Efficiency: Fragments help avoid content duplication, making it easier to update and manage
content. Instead of editing the same section on multiple pages, content managers can update the
fragment once, and the changes will propagate automatically.
• Modularity: Fragments promote a modular approach to content management, allowing for
more flexible and maintainable website structures. Content managers can mix and match
fragments to build complex pages without redundant content.
Core Functionalities
The CMS offers a range of core functionalities designed to streamline the management of web
content. These functionalities provide users with the necessary tools to create, organize, configure,
and publish pages efficiently. Additionally, the CMS supports all default A12 themes, allowing the
visual appearance of content pages to adapt dynamically based on the selected theme of the end
application.
Managing Pages
Managing pages is a fundamental aspect of our CMS, allowing users to handle all aspects of page
lifecycle management from creation to publication. All page management tasks can be performed
within the Pages menu, ensuring a centralized and intuitive user experience.
3

-- 3 of 9 --

Figure 1. Pages tree
Organize Pages
The CMS enables users to organize pages hierarchically, ensuring a logical structure for the website.
Users can rearrange pages using a drag-and-drop interface, making it easy to maintain an intuitive
navigation system. This hierarchical organization supports nesting, allowing for complex page
structures that reflect the website’s architecture.
WARNING When a page is dragged and dropped into another page, the parent page MUST
NOT contain a child with the same path to avoid conflicts.
Create Pages
Inside a content page node, users can click the insert button at the end of the parent node or right-
click and choose "Insert Sub Page" from the context menu. This action will open a dialog where the
user can select from different page types (content, external, dynamic, custom) to suit their specific
needs. Specify the properties for the new sub-page, ensuring that new pages can be quickly added
and integrated into the site.
4

-- 4 of 9 --

Figure 2. Right-click page menu
Figure 3. Create page dialog
5

-- 5 of 9 --

Configure Page Metadata
Each page in the CMS can be configured with detailed metadata to control its behavior and
appearance. Metadata fields include:
• Name: The unique identifier for the page from the user’s perspective.
• Label: Displayed in the web browser tab title and the top navigation bar menu.
• Path: Constructs the final URL by concatenating strings from root pages to the current page.
Pages within the same parent must have unique paths to avoid conflicts.
• Hide Sidebar: A option to configure whether the sidebar should be shown or hidden.
These metadata configurations ensure that each page is properly identified and displayed
according to user preferences and site requirements.
Figure 4. Configure page metadata
Publish pages
Publishing pages is a critical step in making content available to the public. The CMS manages the
publication process by providing tools to control the visibility of pages. Users can preview pages
before publishing to ensure everything is correct and make any necessary adjustments.
Within Pages tree, the publication status of each page is shown in the Status column. Users can
select Publish/Unpublish action button in correspondence with each page to change their visibility.
Figure 5. Publication status and publish/unpublish action buttons to change the visibility of pages
6

-- 6 of 9 --

Managing Fragments
All actions mentioned in this guide will be carried out through the Fragment menu. This menu
provides a centralized location for managing various aspects of fragments, ensuring a streamlined
and organized approach to handling fragment-related tasks.
Create Fragments
Users can navigate to the Fragments menu and initiate the creation of a new fragment. The process
involves specifying the fragment’s name, label, and initial content. Fragments can be designed to
include various elements similar to regular content pages, such as text, images, and layout
elements, which can be reused across multiple pages.
Edit Fragments
Editing fragments allows users to update and refine the content of existing fragments. Users can
select a fragment from the Fragments menu and open it in the editor. All editing actions are similar
to those available in the regular Content Editor used for content pages, ensuring a consistent and
intuitive editing experience.
Changes made to a fragment are automatically reflected wherever the fragment is embedded. This
feature is particularly useful for making global updates to commonly used content sections, such as
headers, footers, and promotional banners.
Embed Fragments
Embedding fragments into content pages is a key functionality that enhances content reuse and
management. To embed a fragment, users need to open the target content page in the editor, then
select the target fragment element within the page and choose the desired fragment name from a
dropdown list. This action embeds the selected fragment into the content page, and any updates to
the fragment will be reflected in all instances where it is used.
Manage Content
For more information, refer to the Content Modeling documentation.
Cross-Reference Links
The CMS allows you to create cross-reference links between different pages and/or direct users to
specific headings within the same application.
Creating Cross-Reference Links
To create a cross-reference link to another page:
1. Insert a regular link element in the CMS
2. Choose between two link types using the radio button:
◦ External: Creates a link to an external URL
7

-- 7 of 9 --

◦ Cross-reference: Creates a link to another page within the same application
When you select the Cross-reference option, two dropdown fields will appear:
• Page (required): Select the target page you want to link to
• Anchor (optional): Select a configured heading anchor within the target page to jump to
Figure 6. Cross reference link configuration
In this example, when a user clicks the configured link, they will be directed to the Home page and
automatically scroll to the "Get Started" heading within that page.
Configuring Heading Anchors
To make a heading available as a link target:
1. Open the heading element configuration panel (in the target page)
2. Set the Anchor field with a descriptive name (e.g., "get-started", "overview")
When users click the cross-reference link, the application will navigate to the target page and
automatically scroll to the selected heading anchor, if specified.
Glossary
Term Brief Definition
Content Model A12 UI model that focuses on presenting a page
or a fragment in A12 application.
Content Engine A12 component that renders pages or fragments
in A12 application based on the Content Model.
Content Editor Web-based application that supports building up
the Content Model in a visually convenient way.
Content Management System (CMS) Web-based application that manages pages and
fragments and uses an integrated Content Editor
to manage elements in a specific page or
fragment.
8

-- 8 of 9 --

Term Brief Definition
Pages tree Tree view that used by the CMS to manage the
hierarchical structure of web pages.
Publish/Unpublish Feature of the CMS to show/hide a page from
public.
Page Entity that presents a web page. All pages in the
application are managed in a tree structure by
Pages tree.
Content Page Web page that shows static content.
External Page Web page that links to an external website.
Custom Page Web page that is controlled and customized by
developers.
Dynamic Page (Experimental) Web page that supports
dynamic content related to user interactions or
data changes.
Label Web page metadata that is displayed on browser
tab title and navigation bar menu.
Path Web page metadata that combines with other
pages' paths in the hierarchical structure down
to root page to form the corresponding URL.
Sidebar Navigational sidebar which could be configured
to be shown or hidden from a web page.
Fragment Reusable component that presents a section of a
web page.
Element/Content elements Structural unit that builds up the content of a
web page.
9

-- 9 of 9 --

