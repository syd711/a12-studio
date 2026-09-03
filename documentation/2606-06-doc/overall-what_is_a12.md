# overall what_is_a12

Introduction
What you can find in this section
In this section of our user documentation, you can find general information which may be of
interest to many different user types.
This document will give you an overview of A12, what it is, and how it works. This is a good place to
start for a first introduction. If you are new to A12 and interested in learning more, keep reading to
understand What is A12?
To see an example of a full stack A12 application in action, you can download the Modeling
Environment installer and open one of the sample workspaces in the Preview Application.
But first, an important question to answer …
What is A12?
A12 is a platform for developing enterprise applications in complex IT landscapes. It relies on
model-driven software engineering (MDSE) and brings the low-code principle to the world of
enterprise software. As an open platform, A12 simplifies the integration of best-of-breed solutions
and the use of AI at all levels. A12‘s Modeling Environment provides tools to create and maintain
parts of an application over the long term as independent business logic modules without
programming experience. A12‘s Runtime Platform provides the flexibility needed to evolve
business-critical applications with professional individual software development, AI support and
system integration into fully integrated enterprise applications.
How A12 Works - From Models to
Applications
A12 is a model-driven approach to business software. It provides a set of concepts, components and
tools for creating modern, web applications.
Decouple Domain-Specific Information From
Development
The core idea of A12 is to encapsulate domain-specific knowledge in models. By using a set of
powerful tools, domain experts and business analysts are able to create and modify these models -
without the need to touch any code. This concept significantly reduces custom development efforts.
Moreover, it enables domain experts to adapt their applications rapidly - which is a competitive
factor in a digitized world with fast changing business requirements.
[Diff Conventional ModelDriven dev] | Diff_Conventional_ModelDriven_dev.png
1

-- 1 of 4 --

Figure 1. The main difference between conventional and model-driven development
Enable Cross-Platform and Cross-Device Applications
In terms of business applications, the client-side has gained the most traction during the last years.
The needs to support mobile end devices like smartphones and tablets and to enable seamless
workflows between different systems and platforms have introduced new levels of complexity. The
A12 framework responds to these challenges by introducing flexible, light-weight UI-Engines
implemented in JavaScript - coupled with a coherent concept for device-independent visual design
and modern look and feel.
Controllable Use of AI
Generative and agent-based artificial intelligence is increasingly finding its way into the software
development process. In an enterprise context, however, approaches such as “vibe coding” should
be treated with extreme caution. A12 and the specially developed tools for quality assurance,
security testing, and build & deployment provide a controllable framework in which generative and
agent-based AI systems can be used effectively and responsibly. mgm has already begun to explore
the possibilities. For example, a prototype integrated into the SME has already been developed that
automatically generates initial models based on provided PDFs. Further forms of AI support are in
the planning stage and will be gradually incorporated into the platform.
How the A12 Platform Is Structured
[A12 Structure] | A12_Structure.png
From a conceptual point of view, A12 can be roughly divided into the following areas:
1. A12 Modeling Environment:
The Modeling Environment provides several tools that can be used to represent high levels of
business complexity for enterprise applications. You can learn more about it in the Modeling-
section of the documentation. If you are a developer you should get at least a basic
understanding of which types of models there are since these are the applications' parts that
can be created and maintained without programming. If you are a business analyst, this is the
core area of A12 which is relevant for you.
2. A12 Design System Plasma:
Enterprise applications are characterized by high information density and great complexity.
Design languages such as Material Design hit their limits quickly. They cannot fully respond to
some challenges, such as how to present complex tables, clearly. Or how to develop the user
interface structure consistently when new information is added. This is why mgm developed
A12’s Plasma design system. Learn more about Plasma in the UI/UX section.
3. A12 Runtime Platform:
The A12 runtime platform consists of a set of modular client and server side components in a
2

-- 2 of 4 --

modern enterprise architecture. It provides robust components for typical enterprise
application requirements. At the same time, it gives the development team full control through
fine-grained entry points to plug in their own code and implement individual project
requirements. The development section contains everything you need to know as a developer –
pointers for getting started, tutorials, detailed documentation of each component and
instructions for migrating to the newest A12 Release Line.
4. Containerization:
mgm relies on a series of standards for A12 projects that enable fast setup and automate
essential build & deployment processes. These primarily include pre-built Helm charts and
Jenkins pipelines. Learn more about it in the Build & Deployment section. It is mainly intended
for DevOps users. In addition, there are cross-functional areas that extend across several of
these areas. These primarily include quality assurance and security.
Data First - The Modeling Philosophy of A12
A12 is all about models. But what exactly can you model as a business expert? What are the
benefits of having different models for data and the user interface? And how does A12 differ
from other modeling approaches like GUI builders?
The A12 platform enables experts and analysts to create and adapt business applications. It aims at
building applications much faster than usual and allows for business-driven adjustments on the fly.
This is accomplished in large parts thanks to a model-based approach. The platform provides tools
for creating models. And it provides technical environments which interpret these models and use
them to control what the corresponding applications are about. The first step of gaining a deeper
understanding of this approach is to take a closer look at the models.
A guiding principle of the platform is to separate different kinds of models: data models and UI
models. We decided to separate data and UI information from each other in order to introduce a
higher level of flexibility and re-usability. You can create several different user interfaces, for
example, which refer to the same data model.
The Bedrock of Business Applications: Data and
Validation Criteria
The first step of creating applications with the A12 platform involves the creation of so called
Document Models. If you want to build an application for stock management, for example, you
need a structural outline for articles among others. An article has a name, represented as a string. It
has a serial number. It has a price. There has to be numbers which describe how many items of
each article are in stock. Additionally, there are rules for the validity of data. The price cannot be
negative. The serial number has to satisfy a certain format. All these organizational structures and
criteria, which relate to the business perspective, are defined in an A12 Document Model. In more
technical terms, Document Models contain specifications of fields, data types, type definitions, and
validation rules.
Document Modeling facilitates both the definition of data structures and validation rules. Data
3

-- 3 of 4 --

fields can be ordered in groups. For conveniently adding and modifying validation criteria, the
editor features "autocomplete" and "syntax highlighting" for the Kernel Language. In large
applications, there might be large sets of data definitions and validation rules. That is why we
introduced so-called "Includes": You can include Document Models in other Document Models. If
you have a basic Document Model for articles, for example, you can reuse it in quite different
contexts.
Content-Related User Interface Design
The Simple Model Editor (SME) is the control center for modeling in A12. The tool enables business
analysts and domain experts to design and test key parts of business applications themselves
without any programming knowledge. It features visual perspectives as well as live previews and
pursues a rather abstract but very powerful way of modeling user interfaces. Instead of diving too
early into design-specifics like colors, margins and spacing, the editor focuses on the organization
of UI elements via models. Instead of letting the user put input fields, radio buttons or text labels
directly on screen in a WYSIWYG-style, it operates with hierarchical lists of model elements. This
modeling philosophy enables business experts to focus on their domain and model complete user
interfaces on their own. The graphical finish is completely decoupled and not specified in UI
models like the Form Model. In order to provide an attractive and modern design out of the box,
A12 makes use of Plasma Design - a coherent design concept for business applications developed by
mgm.
Each A12 UI model refers to at least one A12 Document Model. This is a crucial aspect for
understanding the design philosophy of the SME - and the main difference to classical GUI builders.
Except from static content pages with the A12 CMS there is no point in creating UI models without
corresponding Document Models. You can think of UI models as a kind of wrapper for selected
parts of your Document Models. They provide the frame for user interaction based on a selection of
your data fields. Due to this connection, the Document Model and its accompanying validation rules
are usually created first. As soon as this has been done, new UI models with a reference to this
Document Model can be created. Data and validation definitions really take the center stage in A12.
It takes a little getting used to, but once you get the idea of this unique approach, you will surely
appreciate how fast you can build robust and productive software with it.
Learn More About A12
There are a growing number of introductory videos available in our training catalogue which give
a slightly deeper introduction to A12 as a low-code enterprise platform, an introduction for
Modelers and Business Analysts and an introduction to Plasma, the A12 design system. You can find
these videos in our training catalogue here.
4

-- 4 of 4 --

