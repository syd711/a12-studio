# Tutorial: Basic Modeling

## Prerequisites

The target audience for this tutorial are business analysts. Some prior knowledge of the tools is not presumed. The tutorial focuses on modeling the most simple local application possible. Whilst this tutorial has no prerequisites, it is designed to complement the A12 Fundamentals Training.

This tutorial uses the installer which you can download from geta12.com.

> **NOTE** Please ensure your installer version matches this tutorial.

## Use-case

I want to create a shopping list with all the products I want to buy. Therefore, I want to be able to add or delete products. I also want to be able to sort and filter my entries. The end application should provide a list of products I can review in an overview and a simple form to create new entries.

*Figure 1. Use-case schema*

## End Result

At the end of this tutorial, you will be able to deploy your models so that you can:

- Add new products to your shopping list
- View the products on your shopping list
- Delete products from your shopping list
- Sort and filter products

If you need to check your work as you do the tutorial, please refer to the expandable sections at the end of each step:

> Click here to see what your project should look like by now
>
> You can find a list of models that you created as well as fullscreen pictures of each step to guide you.

## Essentials of Modeling With A12

The modeling part is done in the Simple Model Editor. Within the Simple Model Editor you can choose between different types of Modeling Editors to create different types of models. The types of models we use in this tutorial are roughly presented in the following schema.

*Figure 2. Interaction of Models*

### What Can I Do With a Simple Application Based on the Standard A12 UX Pattern?

A12 enables business experts to create custom applications, which fit their business requirements exactly. Therefore, complex applications can be modeled without writing a single line of code. The modeling takes part in the Simple Model Editor, which is the main tool of A12. Within the Simple Model Editor you are able to define, which data should be collected, how the forms will look like and how the data is displayed to the administrator. Furthermore, the default A12 Plasma Design System supports you with predefined views and functionality based on the usual requirements of business applications. A12 uses the same information architecture as lots of your office applications. To sum it up: A12 enables you to easily create a user-friendly business applications. To read more about the Plasma Design System, please check out the Plasma Documentation.

### How Can I Apply the Models in This Simple Application to Different Use-Cases?

In this tutorial, we want to create a Shopping List, where the user can enter, view, delete, sort and filter data. In this simplified use-case we define the data, which should be collected, in the Document Model. In the Form Model we define the visual appearance of our form. To review and edit existing entries, the Overview Model is used. The Overview Model also adds the functionality to sort and filter the data in the list. The filter is activated by default and the user can choose which data is sortable. Even if we use this simple use-case, we are able to demonstrate the core functionality for business applications: Entering, deleting, sorting and filtering data.

For now, we create a shopping list for our own wishes, but you would go through the same steps to model more complex applications for your business use-case. The models then simply offer more than our small list requires. The following paragraphs give you a few examples of what is possible. The complete list of features is described in the documentation of the respective editor.

In the Document Model, a string field does not have to be a plain line of text. You can restrict how many characters it accepts or permit line breaks for longer texts. You can also offer the user a fixed set of allowed values as an enumeration, or store an uploaded file, such as a photo or a document, together with the record as an attachment. Enumerations and attachments are configured in the Document Model only; how they are displayed, for example as a dropdown or an autocomplete, is decided later in the Form Model. You can find a complete overview of the available data types and their configuration options in the Editors for Model Elements documentation.

> **NOTE** When you model data that occurs more than once, such as several phone numbers for one contact, you use a Repeatable Group in the Document Model. Repeat is a Form Model term for the Form Model Element which references the Repeatable Group. You can model Inline Repeats, Embedded Repeats and Detached Repeats which lead to different presentations and functionality in your Form.
>
> A group has a default of one repetition, so you do not need to set Repetitions = 1 explicitly. Marking a Field as "Required" inside a Repeatable Group is nuanced, because the requirement is not enforced until the user creates a new row.
>
> Sub-groups are optional, but grouping related fields improves the structure of the stored data, which benefits Validation and Computation Rules, queries, authorization, and generated code.

The Overview Model gives you more than the sortable column of our shopping list as well. A column that displays the value of a single field of the Document Model is called a reference column. Depending on the type of the referenced field, such a column can show a suffix after the value, for example a unit, or summarize all values of a number column in the footer of the table. For attachments and multi-selects, you can additionally decide in which display mode the values are shown. These and all further options are described in the Reference Column documentation.

> **TIP** You can also decide how the table is sorted when it first loads by configuring the Default Sorting. See the Default Sorting documentation for details.

### What Does the Standard A12 UX Pattern Look Like in the UI?

The usual way to display data is the Master Detail View. In this view, you've got two slots: An overview ("Master") and a "Detail" slot. You are familiar with this screen layout since it is used by many applications of your daily use for good reasons: With the Master Detail Layout, you are able to get a good overview when working with large sets of business objects. The Master slot can contain a table, a list or a gallery while the detail slot usually contains a form or a table. Buttons for navigating, saving, canceling or other interactions can be added to predefined sections: To the subheader and the footer section. This supports the intuitive usage of your application. You can read more about the UI/UX Design in the A12 UI/UX documentation.

### How Does the Standard A12 UX Pattern Compare to Other A12 Options?

In addition to the Master Detail view, A12 also provides you with other views like a Dashboard or a Gallery view. In these views, you can customize the overview based on the data and the type of data your application is built on. Based on that, you can also navigate to other views and have Master Detail views as well implemented in your application.

> **CAUTION** Please note, that custom layout options will need development support.

You can look at various layout options in the A12 Widget Showcase.

## Step-by-Step Instructions

### Step 1: Plan Your Fields

As A12 follows a data-first modeling philosophy, let's start by looking at what data we need for our shopping list. We need a field where we can enter products to get a list of desired products. Therefore, we plan a single string field to enter text information.

### Step 2: Prepare Your Workspace

Perform the following steps to follow this tutorial.

#### Step 2a: Launch the Preview App Controller

*Figure 3. Preview App Controller*

- Open the folder where you installed the A12 installer.
- Launch the Preview App Controller.
- Click on "New Workspace" at the bottom of the window.
- Select a folder, where you want to store your workspace and the models you will create in this tutorial and call it "Basic Tutorial".

> **TIP** It is recommended to use the default workspace folder which provides the demo workspaces as well.

#### Step 2b: Launch the Simple Model Editor

*Figure 4. Open your Workspace*

Once the Workspace is created:

- Close the Preview App Control.
- Launch the Simple Model Editor.
- Open the Workspace you just created by using the "Open Workspaces" button

When opening a workspace, make sure that the auth folder is visible to the Simple Model Editor. Therefore, always open your Workspace on workspace level. That means, that you have to choose the folder which was named by you. If you see the "auth" folder in your Workspace Explorer, you've done everything correct.

> Click here to see what your project should look like by now.
>
> *Figure 5. Create a new Workspace in the Preview App Control*
>
> *Figure 6. Open Simple Model Editor*
>
> *Figure 7. Select your new Workspace*

### Step 3: Model Your Document Model

We start by adding our first Document Model to the workspace.

#### Step 3a: Create a Document Model

*Figure 8. Product_DM*

- Click on "ADD" in your Workspace Explorer.
- Select "Document Model".

In the dialog window:

- Chose the "Models" folder as the folder for your Document Model.
- Name it "Product_DM".
- Add the locales: "en,de" (these annotations work as a comma-separated list).
- Add the roles "tester" and "reviewer" via the drop down menu.

Like that, you can add English and German labels to your application. To read more about that, please head over to the locales documentation. Also, you would have the opportunity to implement role-based settings later on by adding different roles. To read more about that, please head over to the UAA Documentation for developers.

Finish the initiation of your Document Model:

- Click on "Ok" to initialize the model.

#### Step 3b: Add a Root Group

*Figure 9. Add your root group*

After you initialized a Document Model, it will open right away. In the right half of the screen, you see the structure of your model. To add a field, we have to model a group first. Have a look at the Group documentation for further information.

- Select your Document Model in the Workspace Explorer.
- Click on "+ADD" in the main section of your Document Model.
- Choose "Group".

When initializing a group, the Group Editor opens on the right-hand side.

- Give the group a unique name.

In this case, we create a very simple model, so call this first and only group "Product". We have chosen this name due to our naming convention that the root Group should take a similar name to the Document Model.

- Save your changes to the group by clicking the Apply button on the bottom right-hand side.

#### Step 3c: Add a Field

*Figure 10. Add a new field*

Now we can add the field we planned step 1.

- Right-click on the group you just created
- Select "Field".

Immediately the Field Editor pops up on the right-hand side.

- Enter a name for the new field ("Product").
- Enter a label for the new field ("Product" in English and "Produkt" in German).

> **NOTE** Note, that we have labels for English and German because we've chosen both locales when we created the Document Model. In every following step, you should label elements for the locales you've chosen.

- Once again save your changes to the field by clicking the Apply button on the bottom right-hand side.
- Hit the Save button to save the changes you've done to the model.

> Click here to see what your project should look like by now.
>
> These are the models that you created in this step:
>
> **Product_DM**
>
> *Figure 11. Add a Document Model to your Workspace*
>
> *Figure 12. Basic settings to your Document Model*
>
> *Figure 13. Add the Group, "Product", to your Document Model*
>
> *Figure 14. Basic settings for your Root Group*
>
> *Figure 15. Add a field to your Root Group*
>
> *Figure 16. Settings in the Field Editor*

### Step 4: Model Your Form Model

To be able to interact with your Data Model, you need to create a Form Model which defines how the data should be collected.

#### Step 4a: Create a Form Model

*Figure 17. Add a Form Model*

- Click on "ADD" in your Workspace Explorer.
- Select "Form Model".
- Chose the "Models" folder as the folder for your Form Model.
- Fill in the name "Product_FM".
- Select the related Document Model.
- Check the box next to "Build Screens from Fields".

Like that, the Simple Model Editor does the heavy lifting for you and creates Controls for your Fields automatically.

> **NOTE** Every available fields in your Document Model will be taken into account. This includes metadata which we don't need in this tutorial. We're going to delete them from our Form Model in a few steps. This meta data is visible to the Form Model as well as to the Overview Model. So please be aware of the fact that the Simple Model Editor displays meta data fields which you haven't modeled yourself.

- Choose locales and the roles like you did for the Document Model.
- Click on "Ok" to initialize the model.

By clicking "Ok", the Form Model opens and presents its automatically generated content in the middle section, where on the right you see the related Document Model.

Now you see the basic structure of your Form Model.

#### Step 4b: Delete Metadata

*Figure 18. Delete Metadata Screen*

As mentioned in the info box above, the Document Metadata is part of the Form Model Tree since these Fields got read by the Form Engine. They are not needed in this tutorial. Therefore, the respective screen should be deleted as well as the Button Panel to navigate to this screen.

- Right-click on the screen "__meta".
- Select "Delete Screen".
- Confirm the deletion.

After cleaning up the Form Model, you are presented with the final structure of the Form Model Tree.

> **NOTE** By right-clicking on the top level row ("form model content") you can choose "Expand All" or decide to "Collapse All" later on.

#### Step 4c: Add and Delete Buttons

*Figure 19. Subheader and Footer settings of the Form Model*

To store data, you have to model a "Save" button. In case you want to cancel your input, you should also model a "Cancel" button. These buttons can be modeled in the settings tab.

As you also see an error in the settings tab, lets correct this error and add the buttons.

- Click on the "settings" tab of the Form Model.
- Navigate to "Subheader and Footer" in the secondary menu.
- Delete the two "Navigation" buttons, "Product" and "__meta", from the "Subheader Major Buttons".

> **NOTE** These buttons are unnecessary as you only have one screen.
>
> You see an error as the __meta screen does not exist as you deleted it in Step 4b: Delete Metadata.

- In the "Footer Major Buttons" section, click on "ADD" to add cancel and save buttons to your form.

For the Cancel button:

- Give it the name "Cancel".
- Select "event_cancel" as the event.
- Enable "Destructive" to make the button red.
- Use "Cancel" for the English label and "Abbrechen" for the German label.
- Click on Commit and continue with the Save button.

For the Save button:

- Give it the name "Save".
- Select "event_submit" as the event.
- Select the priority "Primary" to make the button more prominent.
- Use "Save" for the English label and "Speichern" for the German label.
- Click on Commit.

Finishing your button modeling:

- Click on Apply once more to commit your changes.

#### Step 4d: Add Labels

*Figure 20. Label your Form Model*

To recognize the Form Model in the end application, we need to label it.

- In the "settings" tab of your Form Model, navigate to "Settings" in the secondary menu.
- Scroll down until you reach the label section.
- Give it the label "Product", as we want the user to enter a desired product in the form.
- Click on Apply to commit your changes.
- Hit the Save button to save the changes you've done to the model.

> Click here to see what your project should look like by now.
>
> These are the models that you created in this step:
>
> **Product_FM**
>
> *Figure 21. Add a new Form Model*
>
> *Figure 22. Basic settings for your Form Model*
>
> *Figure 23. Structure of your generated Form Model*
>
> *Figure 24. Delete Metadata Screen*
>
> *Figure 25. Enter the settings tab of your Form Model, Subheader and Footer tab*
>
> *Figure 26. Add Cancel button to major Footer Buttons section*
>
> *Figure 27. Add Save button to major Footer Buttons section*
>
> *Figure 28. Subheader and Footer settings - Result*
>
> *Figure 29. Form Model Labels*

### Step 5: Model Your Overview Model

The third model that you need to add to the workspace is the Overview Model.

#### Step 5a: Create an Overview Model

*Figure 30. Add Overview Model*

- Click on "ADD" in your Workspace Explorer.
- Choose "Overview Model".
- Chose the "Models" folder as the folder for your Overview Model.
- Name it "Product_OM".
- Add the same locales you used in the models before.
- Add the same roles you used in the models before.
- Click on "Ok" to initialize the model.

#### Step 5b: Add Columns

*Figure 31. Model Columns in your Overview*

Decide on what should be displayed in the overview:

- Select the related Document Model.
- Add the Columns, you want to see in your overview by clicking the "Add" button.
- Select "Product/Product" under "Element Reference" as we want the different values which have been entered in this field to be displayed in the overview.
- Check the box next to "Sortable" so you can sort your shopping list in the end application.
- Click on "Add" at the bottom to add that column to the overview.

#### Step 5c: Add an Add Button

*Figure 32. Minor Subheader of the Overview Model*

To be able to add products to our shopping list, we have to add an "Add" button.

- Go to the "Custom Actions" tab.
- Scroll down to Subheader section
- Under "Minor", click on "Add".
- Select "Button" as the Action Type.
- Select "add" as the Event.
- Label it with "Add" in English and "Neu" in German.
- If you like, you can choose a suitable icon as well.
- Click on "Add" at the bottom to add the button to your overview.

#### Step 5d: Add a Delete Button

*Figure 33. Delete Button in the Row Action Group*

Now that we created an option to add items to the list, we also want to have the ability to remove them. To do so, we need to create a "Delete" button.

- Stay in the "Custom Actions" tab.
- Scroll up to the Row Action Group.
- Click on "Add" under "Row Action".
- Select "delete" as the event.
- Set the Button to be "Destructive"
- Pick an appropriate icon in the "Visual Settings" area by start typing "delete" in the search field. Then, click in the desired icon to choose it.
- Label the button with "Delete" for the English label and with "Löschen" for the German one.
- Click on "Add" to add the delete button to Overview Model. Now, each row in your overview will have a delete button.

> **TIP** Please label your buttons due to accessibility reasons - even when this doesn't match your personal taste. In that case, you can toggle the "Hide Label" option.

#### Step 5e: Add Labels

*Figure 34. Label the Overview*

Before we're done with the Overview Model, we need to label it.

- Go to the "Model Settings" tab.
- Add "Shopping List" as the English label and "Einkaufsliste" as the German one.

This label will then be shown in the header of the list in the end application.

- Click on "Save" at the bottom right-hand side to save all the changes you made in the Overview Model.

> Click here to see what your project should look like by now.
>
> These are the models that you created in this step:
>
> **Product_OM**
>
> *Figure 35. Add a new Overview Model*
>
> *Figure 36. Basic settings to your Overview Model*
>
> *Figure 37. Add the Product column*
>
> *Figure 38. Settings in your Overview Model - Result*
>
> *Figure 39. Add Button configuration*
>
> *Figure 40. Delete Button configuration*
>
> *Figure 41. Custom Actions tab — Result after adding Buttons*
>
> *Figure 42. Model Settings tab — Result after adding Labels*

### Step 6: Model the Master Detail Module Model

The easiest way to integrate our Overview and Form Models into our end application is to create a Master Detail Module Model.

#### Step 6a: Create a Master Detail Module Model

*Figure 43. Add a Master Detail Module Model*

- Click on "ADD" in your Workspace Explorer.
- Choose "Master Detail Module Model".
- Chose the "Models" folder as the folder for your Master Detail Module Model.
- Name it "Product_MDM".
- Add the same locales you used in the models before.
- Add the same roles you used in the models before.
- Click on "Ok" to initialize the model.

#### Step 6b: Reference Models

*Figure 44. Reference Document, Form and Overview Models in Master Detail Module Model*

Reference the models you want to display in the Master-Detail-View.

- Select the related Overview Model you created in step 5
- Complete the form mapping by selecting the correct Form Model you modeled in step 4.

#### Step 6c: Add Labels

*Figure 45. Label your Master Detail Module Model*

- Go to the Model Settings tab.
- Fill in the name, which should be displayed for this module in the end application ("My Shopping List").
- Click on "Save" at the bottom right-hand side.

> Click here to see what your project should look like by now.
>
> These are the models that you created in this step:
>
> **Product_MDM**
>
> *Figure 46. Add a new Master Detail Module Model*
>
> *Figure 47. Basic settings to your Master Detail Module Model*
>
> *Figure 48. Master Detail tab in your Master Detail Module Model*
>
> *Figure 49. Model Settings tab your Master Detail Module Model*

### Step 7: Reference the Master Detail Module Model in "PreviewApp_AM"

*Figure 50. Reference your Master Detail Module Model*

The App Model needs to know what is has to display. This is handled by referencing the Master Detail Module Model, we just created in the previous step.

- Select "PreviewApp_AM" in your Workspace Explorer.
- Navigate to the Settings tab.
- Scroll down to the bottom and click on "Add" under "Model References".
- In the created row, select "module-masterdetail" as the Model Type and "Product_MDM" as the Reference.
- Save your changes to the "PreviewApp_AM".

> Click here to see what your project should look like by now.
>
> These are the models that you created in this step:
>
> No new models.
>
> *Figure 51. Reference Master Detail Module Model*

### Step 8: Start Your Application in a Local Preview

To display a local preview of the entire application, we need to perform the following steps:

#### Step 8a: Start your Preview App

*Figure 52. Start your Preview App*

- Start the Preview App Controller once again.
- Select the workspace you just modeled
- Click on "Start".

Shortly, your browser will open and shows you a login dialog field.

- Log in to the Application
- Enter the username "admin".
- Enter the password "a12".

#### Step 8b: Deploy your Models

*Figure 53. Deploy your Workspace*

The models you created have to be deployed in order to be displayed. Therefore, perform the following steps:

- Return to the Simple Model Editor.
- Click on the "Configure Server Connection" button which can be found in the top right-hand corner of the Simple Model Editor.
- Enter the username "admin".
- Enter the password "a12".
- Click on "connect"

Once the Simple Model Editor is connected to the local server, the models can be deployed.

- Click on the "Deploy" button in the subheader region of your Workspace Explorer.

Once the deployment was successful, you can return to your browser and open your application.

- Refresh your browser tab.
- Select the "My Shopping List" tab.

You will need to follow this workflow each time you restart the Preview App Control.

> Click here to see what your project should look like by now.
>
> These are the models that you created in this step:
>
> No new models.
>
> *Figure 54. Start a Preview of your Workspace within the Preview App Control*
>
> *Figure 55. Enter credentials in the browser window*
>
> *Figure 56. Connect your Simple Model Editor to the Preview App*
>
> *Figure 57. Deploy your Workspace*

### Step 9: Test Your Shopping List Application

*Figure 58. Test your application*

You can now test your shopping list. Try adding or deleting items using the buttons and form that you modeled in the application.

> **NOTE** If you want to retain this test data, you can save it in your Workspace by clicking on the "Replace Workspace Data" button in the Workspace Explorer of the Simple Model Editor.
>
> Check the Simple Model Editor documentation for more information.

> Click here to see what your project should look like by now.
>
> These are the models that you created in this step:
>
> No new models.
>
> *Figure 59. My Shopping List tab of your application*
>
> *Figure 60. Adding product to your shopping list*
>
> *Figure 61. Master (= Shopping list) — Detail (= item of your list) view of your application*
>
> *Figure 62. Workspace Data after using the "Replace Workspace Data" button in the Workspace Explorer*

If you are not able to add or delete items, please perform the step-by-step instructions once again and check your modeling steps against the screenshots provided.

## Glossary

| Term | Description |
| --- | --- |
| Control | A control in the Form Model is the interface for the end user to add data to a field of our Document Model. |
| Document Model | In the Document Model you define, which data should be collected. Validation and Computation Rules are also part of the Document Model. |
| Field | We use fields in the Document Model to define a column in our set of data we want to collect. |
| Field Editor | The Field Editor opens on the right-hand side when a new field is initialized or an existing field gets selected. In the Field Editor, you can inspect and edit the field properties. |
| Form Model | In the Form Model you define the layout for your forms. Please have the UI/UX Guidelines in mind when you model your forms. |
| Master Detail Model | In the Master Detail Model you connect your Overview Model and your Form Model. This Model is only used to test your models. The integration of your models in the end application will be done in the Application Model by a developer. |
| Overview Model | In the Overview Model you customize the displayed overview and define, which actions are available in the overview. |
| Preview App Control | With the Preview App Control you can start a local server to test your application. New workspaces are created with the Preview App Control as well. |
| PreviewApp_AM | The PreviewApp_AM is initialized automatically when a new workspace is created. Do not make any changes to this model or delete it - that may render your workspace useless. The only change you can make to that model is to reference your Master Detail Models in there. |
| Simple Model Editor | The Simple Model Editor is the modeling tool to create and edit models. Most of your modeling work can be done just with that single tool. |
| Workspace | Your Workspace is displayed in the Simple Model Editor as a bundle of your models. Beyond that your Workspace folder on your computer contains the whole set of data which is needed to run your application. A new Workspace is created in the Preview App Controller. |
