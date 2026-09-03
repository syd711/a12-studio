# overall modeling_tutorial_kernel_language

Tutorial: Kernel Language
Prerequisites
The target audience for this tutorial are business analysts. Some prior knowledge of the tools is
assumed. Before you start this tutorial, you should have completed the following training course(s)
and tutorials. For more details on what topics are covered, please follow the links.
• A12 Fundamentals Training
• Tutorial: Basic Modeling
This tutorial focuses on the Kernel Language and the relevant documentation is linked below.
Although the terminology used in this tutorial is explained in the documentation, we’ve included a
glossary of terms to help you if you don’t understand what we are trying to say:
• Kernel Language Documentation
• Kernel Language Glossary
This tutorial uses the installer which you can download from geta12.com.
NOTE Please ensure your installer version matches this tutorial.
Use-case
I want to be able to create and save multiple shopping lists. Therefore, I want to be able to add
multiple products to my shopping list and then save this. The end application should provide a list
of my shopping lists which I can open.
Each shopping list should show the total cost to me. The following calculations will therefore need
to be carried out:
Table 1. Calculations in the Use-case
Calculation Result
Item Price * Quantity Subtotal
Subtotal - Discount Discounted Subtotal
Sum of Subtotals Total Price
Discounts may be applied on a number of levels in the shopping list. Discounts may be applied to
individual items, product groups or all items. The shopping list must show what discounts have
been applied and how much money I have saved. In addition, a validation should be included to
ensure that an item is not discounted individually and as a result of a product group discount.
Finally, bonus points are offered with a point for each Euro that is spent. Multipliers may be added
to my bonus point for product groups or all items. The shopping list should also show the bonus
1

-- 1 of 47 --

points that I receive.
Figure 1. Use-case schema
End Result
At the end of this tutorial, you will have completed all the steps required to meet the requirements
described in the Use-case. In contrast to other tutorials, you will only model a Document Model. The
tutorial adds complexity to this Document Model step-by-step. You can test your models after each
step using the Ad-hoc Test in the Document Model Editor so that you can:
Step 1
• Add multiple items to your shopping list
• Save multiple shopping lists
• Validate that the data in the shopping list form is complete
• Calculate the subtotals and total price
2

-- 2 of 47 --

Step 2
• Add individual discounts to items on your shopping list
• Validate that either a percentage or a monetary discount is applied but not both
• Calculate the subtotals and total price with and without discounts
Step 3
• Check if any item will be discounted by the bulk discount
• Validate that an item is either discounted by a bulk or an individual discounts but not both
• Add bulk discounts that can be applied to all products from a specific producer with a specific
product type
• Calculate the savings made through bulk discounts and total price with discounts
Step 4
• Add bulk points multipliers that can be applied to all products from a specific producer with a
specific product type or to all items
• Validate that points multipliers are not applied to the same product group twice
• Calculate the bonus points awarded to this shopping list
The end result will be similar to the Order section of the Invoice module in the "basic" Workspace
but will also include some more advanced examples of the Kernel Language. Additional examples
of Validation and Computation Rules can be found in the "kernelLanguage" workspace which under
..\workspaces\further_example_and_tutorial_workspaces.
NOTE
If you would like to test your Document Model using the Preview App Control,
please feel free to model a Form Model, Overview Model and Master Detail Model.
These steps are covered in A12 Fundamentals Training and the Tutorial: Basic
Modeling.
If you need to check your work as you do the tutorial, please refer to the expandable sections at the
end of each step:
▼ Click here to see what your project should look like by now
You can find a list of models that you created as well as fullscreen pictures of each step to guide
you.
There are also expandable sections where you can read an in depth explanation of the Validation
Rules and Computation Rules:
▼ Click here to find out more about this Rule
You can read explanations of the syntax and modeling strategy behind the Validation Rule or
Computation Rule that you have just modeled.
3

-- 3 of 47 --

Essentials of the Kernel Language
You may have noticed some differences between the use-case and the end-result sections. This is
largely because the end result uses some specific terminology that you should understand when
using the Kernel Language. So, before we get started with the step-by-step modeling guide, let’s
clear up any questions you might have about the Kernel Language.
What Can I Do With the Kernel Language?
The Kernel Language is the tool that you use to define valid data in your Document Model. You can
model from simple Rule Conditions, like showing an error if a Field is not filled by the end user. As
you become more familiar with the Kernel Language, you will see that you can also model
extremely complex Rule Conditions involving combinations of Fields and Groups and nested
comparisons.
The same Kernel Language is also used to describe the Preconditions for carrying out a
computation and formulate the computation itself. As before, this means that you can model
complex descriptions of when a specific calculation should be carried out.
In other words, the Kernel Language allows you to:
1. Define a Rule Condition
2. Combine this with other Rule Conditions using logical operators
3. Use these Rule Conditions to:
a. Throw an error
b. Trigger a computation
4. In the case of a computation, describe the desired result
Language Constructs are provided by the Kernel Language to define the conditions you want to
model. These help simplify and shorten the statements made using the Kernel Language and
increase the readability of the statements. The readability of the statements is also improved by the
fact that Validation Rules and Computation Rules may be added to Group in a Document Model and
relative paths may be used.
Let’s look at a few simple examples.
Example 1
I want to test if the end user entered a value in a specific Field. In other words, to check if the Field
is filled or not. Let’s look at a small model for addresses with the Fields: StreetAndNumber, City and
ZipCode.
4

-- 4 of 47 --

Figure 2. Address data
There are two Language Constructs we can use to evaluate if a value is in a single Field. Which of
these two rules do you think are true based on the data above?
1. FieldFilled(StreetAndNumber)
2. FieldNotFilled(City)
Let’s start by "translating" these two Rule Conditions into natural language.
1. The Field called StreetAndNumber is filled.
2. The Field called City is not filled.
As you can see, the Rule Conditions in the Kernel Language are very close to the way you would
express this statement in a requirements document. It should also be clear that both of these Rule
Conditions are true.
TIP
There are a number of examples of simple Validation Rules that you can see in the
Workspaces provided with the installer.
Have a look at "Address_DM" or "Headquarter_DM" in the "kernelLanguage"
Workspace to see examples of more address-based Validation Rules.
Example 2
I want to test if a specific value is present in a list. Let’s imagine a Repeatable Group called
AllNames which contains a Field, Name.
5

-- 5 of 47 --

Figure 3. List of Names
We could easily model a number of different conditions using different Language Constructs.
Which of these two rules do you think are true based on the data above?
1. NoFieldFilled(AllNames*/Name Having [AllNames/Name] == "Elvis")
2. AtLeastOneFieldFilled(AllNames*/Name Having [AllNames/Name] == "Paul")
There is a much more going on in these two Rule Conditions so let’s "translate" them into natural
language again.
1. There is NoFieldFilled in the list of Fields, Name, in the Repeatable Group, AllNames, which has
a Field Value that is exactly "Elvis".
2. There is AtLeastOneFieldFilled in the list of Fields, Name, in the Repeatable Group, AllNames,
which has a Field Value that is exactly "Paul".
Once again the conditions modeled using the Kernel Language are easy to read and understand.
This means that you can work out when the condition is true, even though it is complex.
TIP
Once again, there are examples of Validation Rules using Filter Operations in the
Workspaces provided with the installer.
Have a look at "Order_DM" in the "kernelLanguage" Workspace to see examples of
more Validation Rules that use Filters.
If you’re not convinced yet, try creating the conditions described in Example 1 and Example 2 using
a spreadsheet application or a programming language of your choice.
How Can I Apply the Kernel Language to Different Use-
Cases?
The Kernel Language includes:
6

-- 6 of 47 --

• 95 Language Constructs
• 2 Logical Operations with the option to create nested statements
• 4 Arithmetic Operations with the option to add brackets to the arithmetic expression
• 6 Comparison Operations
• A Filter Operator
• further options to reference repeatable fields by their index or semantic index
• the option to further extend the Kernel Language with Custom Conditions
All this means that there is almost certainly a Language Construct or combination of elements of
the Kernel Language to cover your use-case.
TIP
There are abstract examples of how certain Language Constructs work in the
"kernelLanguage" Workspace.
• Select this Workspace in the Preview App Control using the "Select workspace
from file system" button and start it.
• Open the Abstract Examples module.
• Investigate the Abstract Examples, for example "Predicate Language Constructs
With 'Filled'" for more information on the Language Constructs used in Example 1
and Example 2.
What Does the Kernel Language Look Like in the UI?
Validation Rules
When a Validation Rule is triggered, a message is displayed on the Error Field. The color of the
Error Message depends on the Error Level that you modeled.
Table 2. Validation Rule Error Levels
Error Level Color Meaning
Error red The document is invalid and cannot be saved due to
this error.
Warning yellow with black text This input may not be correct and you should consider
changing it.
Info blue This information is relevant to you based on the input
you made.
Validation Rules can be triggered by a number of different actions in the Form. These include:
• Changing a Value and tabbing or clicking out of the Field
• Leaving a row in an Inline Repeat or Embedded Repeat
• Leaving an Inline Repeat or Embedded Repeat
7

-- 7 of 47 --

• Clicking on the "Commit" action on a Detached Repeat Screen
• Clicking on an action which has the Validation Mode set to either Full Validation or Partial
Validation
NOTE
Validation and Computation Rules can also be triggered in the back-end of your
application. For example, a Full Validation is carried out in the back-end before any
document is saved.
CAUTION
Make sure that you model a Full Validation for save or submit actions in the
Form Model. This will mean that the Validation Rules will be checked and the
end user will see Error Message resulting from the data that they entered.
If you forget to do this, the back-end validation of your document of your
document might fail without you seeing any Error Messages. This could lead to
loss of data.
List of Relevant Fields
The trigger for the validation effects which Fields are added to the List of Relevant Fields. A Full
Validation adds all the Fields in your Document Model to the List of Relevant Fields. Other triggers
lead to a restricted List of Relevant Fields which can effect whether Validation Rules fire or not.
Table 3. Triggers and the Resulting List of Relevant Fields
Trigger List of Relevant Fields Fields Excluded
Changing a Value and clicking
out of the Field
This Field n.a.
Leaving a row in an Inline
Repeat or Embedded Repeat
All Fields in this Row Fields marked as not relevant
by a Dependency
Leaving an Inline Repeat or
Embedded Repeat
All Fields in this Repeat Fields marked as not relevant
by a Dependency
Clicking on the "Commit" action
on a Detached Repeat Screen
All Fields on this Screen Fields marked as not relevant
by a Dependency
Fields in a collapsed Section on
the current Screen
Clicking on an action which has
the Validation Mode set to
Partial Validation
All Fields on this Screen Fields marked as not relevant
by a Dependency
Fields in a collapsed Section on
the current Screen
Fields modeled in a Detached
Repeat
8

-- 8 of 47 --

Trigger List of Relevant Fields Fields Excluded
Clicking on an action which has
the Validation Mode set to Full
Validation
All Fields in the Document
Model
None
NOTE Fields marked with the Global Flag will always be added to the List of Relevant
Fields.
NOTE
Changing a Value in a Field can trigger Computation Rules or Dependencies to fire.
This can lead to other Field Values changing.
If these Fields are visible, they will also be added to the List of Relevant Fields.
NOTE
The List of Relevant Fields will include the following Fields when validating Repeats
and Screens:
• Fields not visible in the Repeat due to pagination
• Fields not visible in the Repeat due to a Filter Expression
Examples of the List of Relevant Fields
Let’s consider a very simple model with four Fields.
Figure 4. Model for List of Relevant Fields Examples
As you can see, two Fields are non-repeatable and two Fields are repeatable and shown in the
Repeat on the right-hand side. There are also two triggers for hiding certain Fields. Trigger1 sets
Field2 to not relevant and Trigger2 sets Field4 to not relevant.
Let’s add the following Validation Rule:
GroupFilled(Repeat)
And FieldsNotCollectivelyFilled(Field1, Field2, Repeat/Field3, Repeat/Field4)
This Validation Rule will fire when a Full Validation is triggered if all the following conditions are
true:
1. There is a row shown in the Repeat (the end user has clicked on "Add" at least once).
9

-- 9 of 47 --

2. One or more of the four Fields is filled.
3. At least one of the four Fields is empty.
The behavior of the Validation Rule for Partial Validations depends on the List of Relevant Fields.
Example 1 - Change a Single Value
Changing a Single Value returns a List of Relevant Fields that contains the Field that you just
changed. As a result, the Validation Rule can never fire as either condition 2 or 3 is not true.
Example 2 - Leaving a row in an Inline Repeat
Leaving a row in an Inline Repeat returns a List of Relevant Fields that contains the Field you can
see in the row. Any Fields that are marked as not relevant by a Dependency are not considered.
As a result, the Validation Rule can never fire if Field4 is not relevant as either condition 2 or 3 is
not true.
If Field4 is visible, the Validation Rule will fire if Field3 is filled but Field4 is empty or vice-versa.
NOTE Field1 and Field2 are part of the List of Relevant Fields as they are not in the Repeat.
NOTE You can achieve similar behavior to marking as not relevant by a Dependency by
removing the Field from the Repeat or Screen.
NOTE The same behavior will be seen when you click "Commit" on a Detached Repeat
Screen.
Example 3 - Clicking on an action which has the Validation Mode set to Partial Validation
The List of Relevant Fields now potentially contains all four Fields. As shown in the table Triggers
and the Resulting List of Relevant Fields, Fields will be excluded if they are: marked as not relevant
by a Dependency; not visible due to a collapsed Section; or modeled on a Detached Repeat.
This leads to a wide range of possible Lists of Relevant Fields which you can test for yourself.
For example, if you model a Detached Repeat and hide Field2, the rule will never fire as either
condition 2 or 3 is not true. This is because the List of Relevant Fields only includes Field 1.
Alternatively, if you model an Inline Repeat and hide Field2 and Filed4, the List of Relevant Fields
only includes Field1 and Field3. This means that:
• if only one of Field1 and Field3 is filled, the Validation Rule will fire.
• if Field1 and Field3 are both filled or both empty, the Validation Rule will not fire.
NOTE You can achieve similar behavior to marking as not relevant by a Dependency by
removing the Field from the Repeat or Screen.
10

-- 10 of 47 --

Computation Rules
You must select a Computed Field when modeling a Computation Rule. The Computed Field is the
Field where the result of the Computation Rule is stored. This Field is shown as a read-only Field in
the UI. The read-only presentation can be changed in the Model Settings or in the Control Editor in
the Form Model.
When working in the UI, changes to Field Values may trigger Computation Rules. In contrast to
Validation Rules, these Computation Rules will always be evaluated based on all the Fields in the
Document Model.
How Does the Kernel Language Compare to Other A12
Models?
The Kernel Language is a tool which you use to define Computation and Validation Rules. These
rules can then be interpreted and evaluated by the Kernel component.
As Validation and Computation Rules are essential for the definition of our Data Model, the Kernel
Language is a key aspect of Document Modeling or Composed Document Modeling.
Step-by-Step Instructions
Step 1: Add Simple Validation and Computation Rules
Figure 5. Simple Data
Let’s get started by adding some Fields to our Document Model. These Fields will be used for data
input or as the Computed Field in a Computation Rule. We will also reference these Fields in the
Validation and Computation Rules that we will model.
Don’t forget, you can use the Ad-Hoc Test at any point to test your rules. Simply follow the
instructions in How to Test and Troubleshoot Your Model. At the end of this step, the resulting Ad-
Hoc Test should look like the image above.
• Create a new Document Model, ShoppingList_DM.
• Model the Groups and Fields as shown below.
11

-- 11 of 47 --

Figure 6. Document Model Structure for Step 1
Table 4. List of Fields for Step 1
Field Name Data Type Data Type Configuration
ProductName String Required
ProductType String Required
Producer String Required
ProducerProductType String
Price Number Required
Decimal Places = 2
Quantity Number Required
Decimal Places not allowed
Subtotal Number Decimal Places = 2
TotalPrice Number Decimal Places = 2
NOTE A Type Definition has been used in the screenshots for all Number Fields which are
limited to 2 Decimal Places.
You have already completed the Validation Rules for Step 1. By selecting the Required Flag, we
ensure that the end user completes these Fields. If the Fields are left blank and the Form is
validated, the end user will be shown a default Error Message.
Step 1a: Add a String Concatenation
12

-- 12 of 47 --

Figure 7. Simple String Concatenation
The Field ProducerProductType will be used to identify a unique combination of Producer and
Product Type. To achieve this, let’s start by creating a single data point which has both of the inputs
from the ProductType and Producer Fields. We will do this modeling a Computation Rule.
NOTE The first important step has already completed as the Field Types of Producer,
Product and ProducerProductType are the same.
• Right-click on the ProducerProductType Field to open the context menu.
• Select "Computation Rule".
• Complete the Name. Let’s use the following naming convention: NameOfFieldComp. This means
the Computation Rule is named, ProducerProductTypeComp.
• Click on the "Computation" tab.
NOTE
The Computed Field has been pre-completed with the path to the Field
ProducerProductType as we used a right-click on the Field ProducerProductType to
create the Computation Rule.
If you did not right-click on the Field, select the Computed Field now.
• Click "Add" to add a new entry to the Computation Table.
• Enter the Precondition: AllFieldsFilled(ProductType,Producer)
• Add the Calculation: [ProductType] + " " + [Producer]
▼ Click here to find out more about this Rule
Calculation
Square Brackets are used to reference the Field Value.
[ProductType] [Producer] [ProductType]+[Producer]
Software mgm technology partners Softwaremgm technology
partners
String Constants may be added in Quotation Marks. A space is added using:
13

-- 13 of 47 --

" "
We add this to the Calculation to make:
[ProductType] + " " + [Producer]
[ProductType] [ProductType] [ProductType] + " " +
[Producer]
Software mgm technology partners Software mgm technology
partners
WARNING
Without a Precondition, this rule can return the following results:
[ProductType] [ProductType] [ProductType] + " " +
[Producer]
Software Software (single tailing
space )
mgm technology
partners
(single leading space)
mgm technology
partners
(single space)
All of these results are invalid and cannot be saved.
Precondition
The Precondition AllFieldsFilled(ProductType,Producer) ensures that both Fields contain Values
before the Calculation is carried out. This means that any whitespace in the computed string
appears between two string values. In other words, the results shown in the warning above are
not produced and the Field is blank.
As any leading or trailing whitespace is trimmed from the Field Value when the end user enters
their data, this ensures that the result of this Computation Rule is valid.
NOTE We will show you how multiple Precondition Calculation pairs may be used to
model alternative valid results in a later step of this tutorial.
Step 1b: Product of Two Number Fields
14

-- 14 of 47 --

Figure 8. Multiply the Price with the Quantity
The Field Subtotal will be used to store the total price of this row. In other words, product of the
Price and the Quantity shown in this row of the Shopping List.
NOTE Once again, the Field Types of Price, Quantity and Subtotal are the same.
• Right-click on the Subtotal Field to open the context menu.
• Select "Computation Rule".
• Complete the Name following your naming convention. This means that, in this tutorial, the
Computation Rule is named, SubtotalComp.
• Click on the "Computation" tab.
• Click "Add" to add a new entry to the Computation Table.
• Enter the Precondition:
• Add the Calculation: [Price] * [Quantity]
▼ Click here to find out more about this Rule
Calculation
As before, Square Brackets are used to reference the Field Value.
[Price] [Quantity] [Price] * [Quantity]
2.50 4 10.00
The Data Type Configuration is important for Calculations involving multiplication or division
with Number Fields that allow decimal places.
The Fields Price and Subtotal are both modeled with two decimal places. As the Quantity Field
does not allow decimal places, the number of decimal places does not change as a result of the
calculation.
NOTE We will show you how Rounding Operators may be used to control the number of
decimal places in the calculation in a later step of this tutorial.
15

-- 15 of 47 --

Precondition
The Precondition may be left blank as empty Number Fields are assumed to have the value 0.
As a result, this computation always returns a valid result. A Precondition may be used if there
are conditions under which no result should be stored.
Step 1c: Sum of a List of Number Fields
Figure 9. Build the Sum of the Subtotals
The Field TotalPrice will be used to store the total of all the items on our Shopping List.
NOTE
The Field Types of Subtotal and TotalPrice are the same and importantly, as you can
read in the explanation to Step 1b, the number of allowed decimal places is the
same.
• Right-click on the TotalPrice Field to open the context menu.
• Select "Computation Rule".
• Complete the Name following your naming convention. This means that, in this tutorial, the
Computation Rule is named, TotalPriceComp.
• Click on the "Computation" tab.
• Click "Add" to add a new entry to the Computation Table.
• Enter the Precondition:
• Add the Calculation: Sum(Products*/Subtotal)
▼ Click here to find out more about this Rule
Calculation
The Arithmetic Language Construct, Sum(FieldList) will add up all the Field Values of the Fields
referenced in the FieldList.
A Field List may be modeled in a number of ways. In this rule, the Asterisk Operator is used to
combine all instances of a repeatable field or repeatable group into a list.
NOTE If you need to reference a Field List that include all the Fields in a Repeatable
16

-- 16 of 47 --

Group, the easiest way to do this is to "look down" at the Fields in the Repeatable
Group in the Document Model Tree.
We are modeling this Computation Rule with the Rule Group ShoppingList. This
means that the rule is a sibling of the Products Group.
As a result, the relative path, Products*/Subtotal, is suggested by the
autocomplete function of the Computation Rule Editor. In other words, the
relative path "looks down" into the Repeatable Group Products.
If you want to model this rule with the Rule Group Products, the relative path
must reflect this. The relative path, ../Products*/Subtotal, moves up one level
and then "looks down" at the Fields in the Repeatable Group. This is not
recommended as the Computation Rule and Computed Field are no-longer
siblings in the Document Model Tree.
Precondition
The Precondition may be left blank as empty Number Fields are assumed to have the value 0.
As a result, this computation always returns a valid result. A Precondition may be used if there
are conditions under which no result should be stored.
You can now test your model and the Validation and Computation Rules that you have added in this
step. If you are unsure how to test your model, please review the How to Test and Troubleshoot
Your Models section.
▼ Click here to see what your project should look like by now.
These are the Validation Rules and Computation Rules that you modeled in this step:
ProducerProductTypeComp, SubtotalComp and TotalPriceComp
17

-- 17 of 47 --

Figure 10. ProducerProductTypeComp
Figure 11. SubtotalComp
18

-- 18 of 47 --

Figure 12. TotalPriceComp
Step 2: Multiple Partial Conditions or Preconditions
Figure 13. Simple Data with Individual Discounts
In this step, the Document Model will be extended to include Fields for Discounts. These Discounts
will then be considered when calculating the Subtotal and Total.
Don’t forget, you can use the Ad-Hoc Test at any point to test your rules. Simply follow the
instructions in How to Test and Troubleshoot Your Model. At the end of this step, the resulting Ad-
Hoc Test should look like the image above.
19

-- 19 of 47 --

Figure 14. Document Model Structure for Step 2
Table 5. List of Fields added for Step 2
Field Name Data Type Data Type Configuration
DiscountPercent Number Min Value = 0
Max Value = 100
DiscountEuros Number Decimal Places = 2
TotalPriceWithoutDiscounts Number Decimal Places = 2
Step 2a: Sum of Products of Two Lists of Number Fields
Figure 15. Use SumOfProducts to Calculate the Total
20

-- 20 of 47 --

As Discounts will be taken into account when calculating the Subtotal in Step 2b let’s start this step
by modeling a second Computation Rule that shows the Total Price without any Discounts.
• Right-click on the TotalPriceWithoutDiscounts Field to open the context menu.
• Select "Computation Rule".
• Complete the Name following your naming convention. This means that, in this tutorial, the
Computation Rule is named, TotalPriceWithoutDiscountsComp.
• Click on the "Computation" tab.
• Click "Add" to add a new entry to the Computation Table.
• Enter the Precondition:
• Add the Calculation: SumOfProducts(Products*/Price,Products*/Quantity)
▼ Click here to find out more about this Rule
Calculation
The Arithmetic Language Construct, SumOfProducts(FieldList1, FieldList2) calculates the
product of each iteration for two repeatable fields and sums these.
The number of Decimal Places allowed for TotalPriceWithoutDiscounts matches the number of
Decimal Places from the Calculation so no rounding operator is required.
NOTE
This could be modeled using a second SubtotalWithoutDiscounts Field.
A Sum of these SubtotalWithoutDiscounts Fields could then be modeled as in Step
1c
Precondition
The Precondition may be left blank as empty Number Fields are assumed to have the value 0.
As a result, this computation always returns a valid result. A Precondition may be used if there
are conditions under which no result should be stored.
Step 2b: Multiple Precondition Calculation Pairs
Figure 16. Apply the Discount to the Subtotal
21

-- 21 of 47 --

Let’s now add the Discounts to the Computation Rule SubtotalComp. This means that when a
Discount is present, then the calculation will take this into account. However, if no Discount is
present, the result of the Computation Rule should be as in Step 1b.
• Click on the Computation Rule SubtotalComp that you modeled in Step 1b.
• Click on the "Computation" tab.
• Edit the Precondition Calculation pair that you added in Step 1b.
◦ Enter the Precondition: NoFieldFilled(DiscountEuros,DiscountPercent)
◦ The Calculation remains: [Price]*[Quantity]
• Click "Add" to add a second Precondition Calculation pair.
◦ Enter the Precondition: FieldFilled(DiscountEuros)
◦ Add the Calculation: [Price]*[Quantity] - [DiscountEuros]*[Quantity]
• Click "Add" to add a third Precondition Calculation pair.
◦ Enter the Precondition: FieldFilled(DiscountPercent)
◦ Add the Calculation: RoundAccounting([Price] * [Quantity] * {{100 - [DiscountPercent]} /
100}, 2)
▼ Click here to find out more about this Rule
Calculations
As before, Square Brackets are used to reference the Field Value. When a mixture of Arithmetic
Operations are used, it is sometimes necessary to add brackets. Per default, division and
multiplication are carried out first and then addition and subtraction operations are carried out.
1. The Calculation [Price]*[Quantity] is the same as that used in Step 1b.
2. The Calculation [Price]*[Quantity] - [DiscountEuros]*[Quantity] is used when a Discount in
Euros is applied. The Discount in Euros is multiplied by the Quantity and then subtracted
from the Subtotal.
NOTE The multiplication operations are carried out first, before the subtraction
operation. As a result, brackets are not required.
3. The Calculation RoundAccounting([Price] * [Quantity] * {{100 - [DiscountPercent]} / 100},
2) is used when a Percentage Discount is applied.
a. The Percentage Discount is converted into the Percentage of the Total Price: {100 -
[DiscountPercent]}
b. The Percentage of the Total Price is converted from a Percentage to a Decimal: {{100 -
[DiscountPercent]} / 100}
c. The Subtotal is then multiplied by this Decimal: [Price] * [Quantity] * {{100 -
[DiscountPercent]} / 100}
d. As the result of this multiplication can have any number of decimal places the Arithmetic
Language Construct RoundAccounting(Value, DecimalPlaces) is used to set the number of
decimal places to two. This result then matches the Data Type Configuration of the Field
22

-- 22 of 47 --

Subtotal: RoundAccounting([Price] * [Quantity] * {{100 - [DiscountPercent]} / 100}, 2)
NOTE
Curly Brackets are used to bracket Arithmetic Operations. These brackets are
then evaluated from the innermost bracket to the outermost bracket. This can be
seen in point 3 of the above note.
The use of curly brackets therefore ensures that the Calculation is evaluated in
the order described above.
Precondition
When more than one row is present in the Computation Table, Preconditions must be modeled
for each Precondition Calculation pair.
1. The Precondition NoFieldFilled(DiscountEuros,DiscountPercent) ensures that this Calculation
is only carried out when no Discount is present.
2. The Precondition FieldFilled(DiscountEuros) ensures that this Calculation is only carried out
when a Discount in Euros is present.
3. The Precondition FieldFilled(DiscountPercent) ensures that this Calculation is only carried
out when a Percentage Discount is present.
WARNING
The Preconditions used in a Document Model must be exclusive.
If more than one Precondition is true, the default Error Message, "error text
for computation of NameOfComputationRule", will be shown when the
Document is validated.
In this case, if the end user completed both the DiscountEuros and
DiscountPercent Fields, then the following default Error Message would be
shown:
"error text for computation of SubtotalComp"
NOTE
As the Preconditions used in a Document Model must be exclusive this also
means that an Error Message will be shown if two different Computation Rules
with the same Computed Field have Preconditions that are true at the same time.
TIP
Non-exclusive preconditions can sometimes be detected using the "Rule
Contradictions" tab of the Document Model Editor. The rules which always have
non-exclusive preconditions will be listed in the report. Rules which have non-
exclusive preconditions under certain conditions will not be listed in the report.
Step 2c: Validate That Only One Field Is Filled
23

-- 23 of 47 --

Figure 17. Ensure that the End-User
The end user should only complete one of the Discount Fields but not both of them. A Validation
Rule allows you to model an Error Message that will be shown when a particular Error Condition is
true.
In this case, the Error Condition is that the end user has entered values into both Discount Fields.
The Error Message will guide the end user by describing the desired combination of data, that only
one of these Fields may be filled.
• Right-click on the DiscountPercent Field to open the context menu.
• Select "Validation Rule".
• Complete the Name. Let’s use the following naming convention that the Name describes the
valid state. This means the Validation Rule is named, EitherPercentOrEuros.
• Click on the "Condition" tab.
NOTE
The Error Field has been pre-completed with the path to the Field DiscountPercent
as we used a right-click on the Field DiscountPercent to create the Validation Rule.
If you did not right-click on the Field, select the Error Field now.
• Add the Error Condition: GroupFilled(RuleGroup) and
MoreThanOneFieldFilled(DiscountEuros,DiscountPercent)
• Add the Error Message: "Please complete either a $DiscountPercent$ or a $DiscountEuros$ but
not both"
NOTE
This Validation Rule was copied.
The Error Field was set to DiscountEuro in the copied Validation Rule.
This ensured that the Error Message was shown on both Discount Fields.
▼ Click here to find out more about this Rule
24

-- 24 of 47 --

Error Condition
The Rule Group for this Validation Rule is the repeatable Group, Products. This Validation Rule
should only be triggered for rows of the Repeat that have been already added. This is achieved by
the partial Error Condition: GroupFilled(RuleGroup)
NOTE
It is not strictly necessary to add the partial Error Condition
GroupFilled(RuleGroup) to this Validation Rule.
GroupFilled(RuleGroup) has been added for demonstration purposes.
GroupFilled(RuleGroup) must be added when using negative Language Constructs,
for example: NotAllFieldsFilled(DiscountEuros,DiscountPercent) or
NoFieldFilled(DiscountEuros,DiscountPercent)
The Predicate Language Construct MoreThanOneFieldFilled(FieldList) is true if two or more
Fields in the FieldList are filled. The FieldList was modeled using a comma separated list of
Fields.
NOTE The Predicate Language Construct AllFieldsFilled(FieldList) could also have
been used.
Error Message
The Error Message was modeled using text and references to the Field Labels. These references
to the Field Labels were made using dollar symbols: $DiscountPercent$ and $DiscountEuros$.
NOTE References to Field Labels and Field Values may only be made to Fields
referenced in the Error Condition.
▼ Click here to see what your project should look like by now.
These are the Validation Rules and Computation Rules that you modeled in this step:
TotalPriceWithoutDiscountsComp, SubtotalComp, EitherPercentOrEuros_Percent and
EitherPercentOrEuros_Euro
25

-- 25 of 47 --

Figure 18. TotalPriceWithoutDiscountsComp
Figure 19. SubtotalComp
26

-- 26 of 47 --

Figure 20. EitherPercentOrEuros_Percent "Condition" tab
Step 3: Comparing Values and Filtering in FieldLists
Figure 21. Data with Individual Discounts and Bulk Discounts
In this step, the Document Model will be extended to include Fields for Bulk Discounts. The savings
made through these Discounts will then be calculated and a new Total Price will be calculated that
takes all savings into account.
Don’t forget, you can use the Ad-Hoc Test at any point to test your rules. Simply follow the
instructions in How to Test and Troubleshoot Your Model. At the end of this step, the resulting Ad-
Hoc Test should look like the image above.
27

-- 27 of 47 --

Figure 22. Document Model Structure for Step 3
Table 6. List of Fields added for Step 3
Field Name Data Type Data Type Configuration
TotalPriceWithBulkDiscounts Number Decimal Places = 2
In addition to the TotalPriceWithBulkDiscounts Field, you need to add the new repeatable Group
shown in the Document Model structure above.
• Right-click on the Group Products and select "Copy".
• Right-click on the Group ShoppingList and select "Paste".
• Rename the Group "BulkDiscounts".
• Delete the following Fields and Computation Rule from the Group BulkDiscounts using right-
click.
◦ ProductName
◦ Price
◦ Quantity
28

-- 28 of 47 --

◦ SubtotalComp
• Rename the Field Subtotal, "SavingsSubtotal".
NOTE You may also use the Multi-Select function to copy and paste the Group without the
Price and Quantity Fields
Step 3a: Comparing Values
Figure 23. Compare the ProducerProductType between the two Repeatable Groups
Simple comparisons between Constants or Field Values can easily be achieved using Comparison
Operations as long as the Data Type is compatible. For example:
Table 7. Comparison Examples
Data Types Comparison
String Field and String Constant [Producer] == "mgm technology partners"
Two Number Fields [TotalPrice] < [TotalPriceWithoutDiscounts]
Date Constant and correctly formatted String
Constant
Today != "25.12.2024"
NOTE In these examples, the Fields referenced are not repeatable.
A number of comparisons which relate to FieldLists can be modeled using one of the Predicate
Language Constructs. We will need to compare the values in two FieldLists as we need to apply a
Discount if the identifier, ProducerProductType matches.
In other words, we need to check if any value of ProducerProductType in Products matches a value
of ProducerProductType in BulkDiscounts. This check then needs to be repeated for each value of
ProducerProductType in BulkDiscounts.
Let’s start by modeling a warning that is displayed if the end user enters a BulkDiscount and there
is no matching ProducerProductType in Products.
• Right-click on the ProducerProductType Field in the BulkDiscounts Group to open the context
menu.
29

-- 29 of 47 --

• Select "Validation Rule".
• Complete the Name following your naming convention. This means that, in this tutorial, the
Validation Rule is named, TypeIsInShoppingList.
• Click on the "Condition" tab.
• Set the Level to "Warning"
• Add the Error Condition: GroupFilled(RuleGroup) and
NoFieldValueIncludedInValueList(ProducerProductType In ../Products*/ProducerProductType)
• Add the Error Message: "Please check your products, this type has not been added to the list"
▼ Click here to find out more about this Rule
Error Condition
The Rule Group for this Validation Rule is the repeatable Group, BulkDiscounts. As before, the
partial Error Condition GroupFilled(RuleGroup) ensures that this Validation Rule can only be
triggered for rows of the Repeat that have been already added.
NOTE
As the negative Language Constructs, NoFieldValueIncludedInValueList(FieldList
IN ValueList) is used, the partial Error Condition GroupFilled(RuleGroup) must be
added to this Validation Rule.
The Predicate Language Construct NoFieldValueIncludedInValueList(FieldList IN ValueList) is
true if none of the Values from the FieldList are in the ValueList. The ValueList may be either a
FieldList or a list of String Constants.
In this case, we use the Asterisk Operator to reference the list of ProducerProductType Fields in
the repeatable Group Products.
NOTE
The Predicate Language Construct FieldValueNotIncludedInValueList (Field,
ValueList) may not be used as the ValueList must consist of either a list of field
values (such as [Field1], [Field2], [Field3]) or a list of string or number constants.
Error Message
A simple Error Message was modeled with no references to Field Labels or Values.
Error Level
This Validation Rule has been modeled as a Warning Level Rule. This means that the end user
may still save the document after acknowledging the Warning.
Step 3b: Filtering a List Using Having
30

-- 30 of 47 --

Figure 24. Placeholder
A further Validation Rule needs to be modeled to ensure that a particular item is not discounted
twice. Currently, it is possible to add an individual discount and a Bulk Discount to the same item.
We have already seen how we can compare values in Step 3a. We now need to filter the FieldList
for those Products which have an individual Discount.
Let’s model a Validation Rule that will fire if the Field Values of ProducerProductType in
BulkDiscounts match this filtered FieldList.
• Right-click on the ProducerProductType Field in the BulkDiscounts Group to open the context
menu.
• Select "Validation Rule".
• Complete the Name following your naming convention. This means that, in this tutorial, the
Validation Rule is named, ProductNotAlreadyDiscounted.
• Click on the "Condition" tab.
• Add the Error Condition: GroupFilled(RuleGroup) and
AtLeastOneFieldValueIncludedInValueList(ProducerProductType In
../Products*/ProducerProductType
Having FieldFilled(../Products/DiscountEuros) or FieldFilled(../Products/DiscountPercent))
• Add the Error Message: "Please check your products, this type has already been individually
discounted"
▼ Click here to find out more about this Rule
Error Condition
The Rule Group for this Validation Rule is the repeatable Group, BulkDiscounts. As before, the
partial Error Condition GroupFilled(RuleGroup) ensures that this Validation Rule can only be
triggered for rows of the Repeat that have been already added.
The Predicate Language Construct AtLeastOneFieldValueIncludedInValueList(FieldList IN
ValueList) is true if one or more of the Values from the FieldList are in the ValueList. As with the
Predicate Language Construct NoFieldValueIncludedInValueList(FieldList IN ValueList), the
ValueList may be either a FieldList or a list of String Constants.
31

-- 31 of 47 --

Once again, we use the Asterisk Operator to reference the list of ProducerProductType Fields in
the repeatable Group Products. This FieldList is then filtered by using Having and a filter
condition. The filter condition FieldFilled(../Products/DiscountEuros) or
FieldFilled(../Products/DiscountPercent) checks if an individual discount has been entered.
NOTE
In this example, the Logical Operator Or is used.
If a combination of Or and And are required, these must be grouped in round
brackets to clarify how the combination of Or and And is to be understood.
NOTE
The filter condition FieldFilled(../Products/DiscountEuros) or
FieldFilled(../Products/DiscountPercent) could also have been modeled using
AtLeastOneFieldFilled(../Products/DiscountEuros,../Products/DiscountPercent)
Error Message
A simple Error Message was modeled with no references to Field Labels or Values.
Step 3c: Comparing a Value with a Field List with Having
Figure 25. Placeholder
It’s now time to compute the savings made as a result of the Bulk Discount. This means that we
need to sum up either the Quantity or the Subtotal of products with a specific ProducerProductType
so that either a Discount in Euros or a Percentage Discount may be applied.
Based on what you have already learnt in this tutorial you might already know that we will
combine a number of topics in a single Computation Rule:
1. Sum up a FieldList (Step 1c)
2. Multiple Preconditions (Step 2b)
3. Comparing Values (Step 3a)
4. Filtered Lists (Step 3b)
Let’s get started!
• Right-click on the SavingsSubtotal Field to open the context menu.
32

-- 32 of 47 --

• Select "Computation Rule".
• Complete the Name following your naming convention. This means that, in this tutorial, the
Computation Rule is named, SavingsSubtotalComp.
• Click on the "Computation" tab.
• (Optional) - Check the "Common Precondition" checkbox.
• (Optional) - Add the Common Precondition:
AtLeastOneFieldFilled(DiscountEuros,DiscountPercent)
• Click "Add" to add a Precondition Calculation pair.
◦ Enter the Precondition: FieldFilled(DiscountEuros)
◦ Add the Calculation: [DiscountEuros] * Sum(../Products*/Quantity
Having [../Products/ProducerProductType] == [$ProducerProductType])
• Click "Add" to add a second Precondition Calculation pair.
◦ Enter the Precondition: FieldFilled(DiscountPercent)
◦ Add the Calculation: RoundAccounting(Sum(../Products*/Subtotal
Having [$ProducerProductType] == [../Products/ProducerProductType])
* {[DiscountPercent] / 100}, 2)
▼ Click here to find out more about this Rule
Calculations
As before, Square Brackets are used to reference the Field Value for the Discount added in
BulkDiscounts. The Savings are then calculated in a similar way to Step 2b. As the Savings are
calculated we need to perform one of two Calculations:
1. The Calculation [DiscountEuros]*[Quantity] is used when a Discount in Euros is applied.
2. The Calculation Sum of Filtered Items * {[DiscountPercent] / 100} is used when a
Percentage Discount is applied.
a. As before, the result of this multiplication can have any number of decimal places and the
Arithmetic Language Construct RoundAccounting(Value, DecimalPlaces) is used to set the
number of decimal places to two. This result then matches the Data Type Configuration of
the Field SavingsSubtotal
The filtered FieldList is modeled in the same way in both Calculations:
• Sum(FieldList Having [../Products/ProducerProductType] == [$ProducerProductType])
a. Square Brackets are used to reference the Field Values of Products/ProducerProductType
and BulkDiscounts/ProducerProductType.
As the Rule Group is Bulk Discounts, the Field BulkDiscounts/ProducerProductType is
referenced with the relative path ProducerProductType.
b. As we are working in two different repeatable contexts, the $-Operator is used to specify
iteration in the filter condition.
33

-- 33 of 47 --

i. The condition Sum(FieldList
Having [../Products/ProducerProductType] == "Software mgm technology partners")
iterates through each Value of ProducerProductType in the repeatable Group
Products.
These values are then compared to the String Constant "Software mgm technology
partners" one-by-one.
ii. We need to filter based on a Field Value instead of a constant, so we reference this
with square brackets.
NOTE
If the Field was not repeatable, we could stop here. For example:
Sum(FieldList
Having [../Products/ProducerProductType] == [NonRepeatableField)]
iii. As we also need to iterate through all values of ProducerProductType in the
repeatable Group BulkDiscounts. As this is a different repeatable context, we need to
specify this with the $-Operator which results in the following filter condition: Having
[../Products/ProducerProductType] == [$ProducerProductType])
NOTE
This filter condition may also be modeled using the Predicate Language Construct
shown in Step 3b with the paths and $-Operator as described above:
AtLeastOneFieldValueIncludedInValueList(../Products/ProducerProductType In
$ProducerProductType))
This results in the Calculation: [DiscountEuros] * Sum(../Products*/Quantity
Having
AtLeastOneFieldValueIncludedInValueList(../Products/ProducerProductType In
$ProducerProductType))
Precondition
As more than one row is present in the Computation Table a Preconditions was modeled for each
Precondition Calculation pair.
1. The Precondition FieldFilled(DiscountEuros) ensures that this Calculation is only carried out
when a Discount in Euros is present.
2. The Precondition FieldFilled(DiscountPercent) ensures that this Calculation is only carried
out when a Percentage Discount is present.
An optional Common Precondition was modeled for demonstration purposes.
Step 3d: Update the Total Price
34

-- 34 of 47 --

Figure 26. Subtracting the Savings from the Total Price
As you have made it this far through the tutorial, you will have no problems subtracting these
Savings from the Total Price.
• Right-click on the TotalPriceWithBulkDiscounts Field to open the context menu.
• Select "Computation Rule".
• Complete the Name following your naming convention. This means that, in this tutorial, the
Computation Rule is named, TotalPriceWithBulkDiscountsComp.
• Click on the "Computation" tab.
• Click "Add" to add a Precondition Calculation pair.
◦ Enter the Precondition:
◦ Add the Calculation: [TotalPrice] - Sum(BulkDiscounts*/SavingsSubtotal)
TIP
An alternative way to apply discounts is shown in "OrderItems_DM" in the
"kernelLanguage" Workspace.
In this example, the discount applied depends on the number of different products
and a single special offer product.
▼ Click here to find out more about this Rule
Calculation
We simply sum the Savings as we did in Step 1c and subtract these from the Value in the
TotalPrice Field.
Precondition
No Precondition is necessary
▼ Click here to see what your project should look like by now.
These are the Validation Rules and Computation Rules that you modeled in this step:
TypeIsInShoppingList, ProductNotAlreadyDiscounted, SavingsSubtotalComp and
TotalPriceWithBulkDiscountsComp
35

-- 35 of 47 --

Figure 27. TypeIsInShoppingList
Figure 28. ProductNotAlreadyDiscounted
36

-- 36 of 47 --

Figure 29. SavingsSubtotalComp
Figure 30. TotalPriceWithBulkDiscountsComp
Note on Modeling Strategy
The Validation and Computation Rules that you modeled in this step increased in complexity and
included rules that iterated through two different repeatable contexts.
This strategy was necessary as:
37

-- 37 of 47 --

1. Multiple Products may have the same identifier in the Field ProducerProductType. For example,
there are multiple Software Products from mgm technology partners in your Shopping List.
2. The identifier ProducerProductType is a Computed Field.
An alternative strategy is available if the requirements were different:
1. The identifier in the Field ProducerProductType must be unique in the repeatable Group
Products.
2. The identifier ProducerProductType is not a Computed Field.
Alternative Modeling Strategy
In this alternative Modeling Strategy, we will use Index Fields and Parallel Iteration to achieve a
similar result.
TIP You can test how parallel iteration works using "ParallelIteration_DM" in the
"kernelLanguage" Workspace.
• Set both identifier Fields to be Index Fields.
◦ Click on the Group Products.
◦ Select the identifier Field in the drop-down "Index Field".
◦ Click on the Group BulkDiscounts.
◦ Select the identifier Field in the drop-down "Index Field".
NOTE
The Groups can now be compared based on the Values in the Index Fields.
If the Index Fields have the same Value, they will be "matched" with each other.
CAUTION
The Asterisk Operator should not be used when using Parallel Iteration as:
1. The Fields are matched based on the Value in the Index Field
2. The Value in the Index Field must be unique
This means that we always reference a single Field with a specific Value in the
Index Field
• Remodel the Validation Rule ProductNotAlreadyDiscounted.
◦ Click on the "Condition" tab.
◦ Change the Error Condition: MoreThanOneFieldFilled(DiscountEuros,DiscountPercent,
../Products/DiscountEuros,../Products/DiscountPercent)
◦ Change the Error Field to any of the Discount Fields
NOTE The Fields DiscountEuros and DiscountPercent in the Groups BulkDiscount and
Products will be "matched" with each other if they have the same Value in the Index
38

-- 38 of 47 --

Field.
If the end user applies more than one Discount with the same Index Field Value, an
error will be shown.
The Calculation of the Savings made can also be simplified. However, the changes made to the
requirements mean that only one Product may have the same Value in the Index Field. As a result,
there is no need to sum the Values from a List of Fields.
• Remodel the Computation Rule SavingsSubtotalComp.
◦ Click on the "Computation" tab
◦ Update the Precondition Computation Pair for Euro Discounts
▪ Precondition: FieldFilled(DiscountEuros)
▪ Calculation: [DiscountEuros] * [../Products/Quantity]
◦ Update the Precondition Computation Pair for Percentage Discounts
▪ Precondition: FieldFilled(DiscountPercent)
▪ Calculation: RoundAccounting([../Products/Subtotal] * {[DiscountPercent] / 100}, 2)
NOTE
As before, the Fields DiscountEuros,DiscountPercent in the Group BulkDiscount are
matched with the Subtotal and Quantity Fields from the Group Products if they
have the same Value in the Index Field.
Step 4: Repeat and Review
Figure 31. Bonus Points and Points Multipliers
The Document Model will be extended again to include Fields for Points Multipliers. The extra
points that are earned through these Points Multipliers will then be calculated and the sum of
Points will be shown.
Don’t forget, you can use the Ad-Hoc Test at any point to test your rules. Simply follow the
instructions in How to Test and Troubleshoot Your Model. At the end of this step, the resulting Ad-
Hoc Test should look like the image above.
39

-- 39 of 47 --

Figure 32. Document Model Structure for Step 4
Table 8. List of Fields added for Step 4
Field Name Data Type Data Type Configuration
AllItems Confirm
Multiplier Number Min Value = 2
Points Number
Once again, the simplest way to add the PointsMultipliers Group is to create a copy of an existing
Group.
• Right-click on the Group BulkDiscounts and select "Copy".
• Right-click on the Group ShoppingList and select "Paste".
• Rename the Group "PointsMultipliers".
40

-- 40 of 47 --

• Delete the following Fields and the Rules associated with them from the Group
"PointsMultipliers" using right-click.
◦ DiscountPercent
◦ DiscountEuro
• Model the Fields AllItems and Multiplier as described in the table.
• Rename the Field SavingsSubtotal, "PointsSubtotal", and delete its related Computation Rule
"SubtotalComp". As before, you will add a new Computation Rule in a moment.
NOTE Don’t forget to change the Data Type Configuration of these copied Fields to ensure
they are appropriate.
Step 4: Requirements
You have all the knowledge that you need to complete this final section. As a result, a step-by-step
set of instructions will not be offered.
As always, the results are shown in the screenshots at the end of this section.
Your mission, should you choose to accept it, is to model Validation and Computation Rules
required to fulfill the following requirements:
1. The end user may not enter an identifier in the Field ProducerProductType and select the
checkbox AllItems in the same row. See Step 2c for a reminder.
2. Points are awarded based on the original Price.
a. You earn one Point for every Euro.
NOTE
Due to changes that you made in Step 2b, you can only calculate the original
price of all the items. To calculate the original price of items with a specific
ProducerProductType you need to add a "helper" Field.
BaseSubtotal is suggested in the Document Model Structure for Step 4.
Consider modeling this as a Transient Field.
▼ Click here to find out more about Transient Fields
"Helper" Fields can increase the amount of data that you store. For Fields that only serve
as an intermediate step in a calculation chain, this storage is unnecessary.
Selecting the Transient property prevents a Field value from being persisted as these
values are never sent to the server. Transient values of Computed Fields only exist after
the Computation Rule has been evaluated. Ensure that Computation Rules have been
evaluated before reaching any process that needs the result. For example, when using a
Transient Computed Field to trigger a Form Model Dependency, Form Model
Preprocessing must be used to recompute existing Documents when they are loaded.
Use Transient Fields when:
▪ The Field is only used as input to other Computation Rules and does not need to be
41

-- 41 of 47 --

displayed or stored independently.
▪ You want to keep the stored Document compact by excluding intermediate results.
▪ The value can be reliably recomputed by your Computation Rules from other
persisted Fields.
BaseSubtotal is a typical example: it is purely intermediate input for PointsSubtotal and
can always be recomputed from the persisted Fields Price and Quantity.
b. You should round up so that if the original Price was €2.01 you earn 3 Points.
c. The PointSubTotal must show the extra points earned. See Step 2b for a reminder.
NOTE
The extra points are calculated to make the calculation of the total points
easier.
You should use: {[Multiplier]-1}
i. If the AllItems checkbox is set, calculate the extra points based on the
TotalPriceWithoutDiscounts.
ii. If an identifier is entered in the Field ProducerProductType, calculate the extra points
based on the TotalPriceWithoutDiscounts for these items. See Step 1c and Step 3c for a
reminder.
3. The Total Points earned should be shown (base Points plus extra Points). See Step 3c for a
reminder.
4. (Optional) The AllItems checkbox may only be used once.
▼ Click here to see what your project should look like by now.
These are the Validation Rules and Computation Rules that you modeled in this step:
42

-- 42 of 47 --

Figure 33. Requirement 1 - EitherAllItemsOrType
Figure 34. Requirement 2 - BaseSubtotalComp
43

-- 43 of 47 --

Figure 35. Requirement 2 - PointsSubtotalComp
Figure 36. Requirement 3 - PointComp
44

-- 44 of 47 --

Figure 37. Requirement 4 - AllItemsOnlyOnce
How to Test and Troubleshoot Your Model
Ad Hoc Testing can be used to test the Validation or Computation Rules in your Document Model.
All the Fields referenced in the selected Rule or Rules will be displayed in a new SME window. You
can then:
• Switch to the new SME window that opens.
• Enter your test data.
◦ Check the results of Computation Rules
• Use the "Validate" button in the sidebar to:
◦ See if your Validation Rule fires.
◦ Ensure that your Preconditions are exclusive.
Ad Hoc Testing can be started in a number of ways as shown below.
How to Test a Single Validation or Computation Rule
• Right-click on the Validation or Computation Rule.
• Select "Ad Hoc Testing".
How to Test Multiple Validation or Computation Rules
• Click on the Bulk Operations button above the Document Model Tree.
• Select the Validation or Computation Rules you want to test
45

-- 45 of 47 --

• Click on the "Ad Hoc Testing" button in the Bulk Operations above the Document Model Tree.
How to Test All Validation and Computation Rules in a Group
• Right-click on the Group you want to test.
• Select "Ad Hoc Testing".
How to Test All Validation and Computation Rules in your Model
• Right-click on the virtual Node "Model Tree" of the top of the Document Model Tree.
• Select "Ad Hoc Testing".
Glossary
Term Description
Calculation This describes the value that will be written in the Computed Field.
Common Precondition A common precondition is a condition of the validation language which
determines whether the computation is carried out at all.
Computation Table Computation tables specify the computed value and consist of three
columns: Common Precondition, Precondition and Calculation.
Computed Field This is the Field where the result of a Computation Rule will be written.
Error Field The error field of a rule is the field on which the error is usually
displayed on the A12 web interface.
Error Level The Error Level defines how the occurrence of a rule is classified and
should be interpreted. If Error Rules are triggered, the input is invalid. If
Rules of other Levels are triggered, a warming or information will be
shown in the Form although the input is valid.
Error Message The Error Message or Error Text is the output in the event of an Error.
This will be shown next to the Error Field.
Full Validation All Validation Rules are evaluated with the List of Relevant Fields
including all Fields in the Document Model.
Index Field For every Group, one Field can be specified as the Index Field. The Index
Field is a required Field and must have a unique value.
Iteration Iteration is the process of evaluating all the repeatably referenced Fields
one-by-one.
Language Construct Language Constructs evaluate their arguments (fields, field lists, groups,
group lists, values, and value lists) according to a clearly defined set of
rules. The arguments that a Language Construct accepts are defined in
the Kernel Language documentation for each Language Construct.
46

-- 46 of 47 --

Term Description
List of Relevant Fields This is the List of Fields that are used by the Kernel when evaluating a
Validation Rule. The Fields that are included in this list depend on the
type of Validation and the specific interaction that the end user makes
with the Form.
Parallel Iteration Parallel Iteration allows Validation and Computation Rules to be
evaluated based on the Values in the Index Fields of the Groups
referenced in the Rules. The Rules iterate through the repeatable Groups
and Fields with match Indexes are evaluated in the same iteration.
Partial Validation Validation Rules that reference Fields in the List of Relevant Fields are
evaluated using the List of Relevant Fields generated by the user
interaction that triggered the Partial Validation.
Precondition A Precondition defines a case stating under which circumstances a
computation is carried out.
Rule Condition The Rule Condition defines when the Validation Rule should fire and the
Error Message be shown.
47

-- 47 of 47 --

