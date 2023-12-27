# Discount Service Documentation
This service enables creation and management of discount


## Enums
### DiscountPlace
This enum defines where the discount will be applied.
Options are:
- ROOM_DISCOUNT
- BOOM_DISCOUNT

Consider the boom is selected. In this case the discount will be applied to the all of the 
rooms in the boom. In case room is selected, then discount will be applied only to the rooms
with input room_name in the target boom.

### DiscountType
This enum defines various types of discount. Currently these are the
developed discounts:
- GENERAL
- LAST_MINUTE
- CODE

## Discount execution
All discount types can be executed percent-wise or amount-wise.
If the discount is percent-wise then the discount will be based
on the input percentage. Otherwise if the discount is amount-wise
then the discount will be based on the input amount.

Here are the enum values:
- PERCENTAGE
- AMOUNT

## Discounts

### General discount
By applying this discount the Boom owner can apply an offer like:
_If you purchase at least minimumRequiredPurchase then you will get 
amount/percent off, at most discountThreshold_.

**minimumRequiredPurchase** and **discountThreshold** can be null,
which they will be ignored in process of calculating discount.

The fields are:
- discountExecution
- percent: The percent which will be applied to the total cost
- amount:  The amount which will be subtracted from the total cost
- minimumRequiredPurchase: Minimum amount of total cost, which is required
to activate the discount
- discountThreshold: If discount execution is PERCENTAGE, then this field is
the maximum amount of applied discount
- lifeTimeStart, lifeTimeEnd: These fields define the start and end of the
discounts life time
- targetDateStart, targetDateEnd: these fields define the target dates, which 
discount can be applied for

### Last minute discount
This discount is applied for only the first day of residence.

The fields are:
- discountExecution
- percent: The percent which will be applied to the total cost
- amount:  The amount which will be subtracted from the total cost
- targetDate: The date, for which the discount will be applied for
- lifeTimeStart: The date, from which discount life time starts

### Code Discount
The Boom owner can define a unique code for discount. The discount
can be applied percentage-wise or amount-wise. Also, the code hase
limited number of usage.

The fields are:  
- discountExecution
- percent: The percent which will be applied to the total cost
- amount:  The amount which will be subtracted from the total cost
- code: Unique code for discount
- usageCount: Number of times that the code can be applied
- lifeTimeStart, lifeTimeEnd: These fields define the start and end of the
  discounts life time
- targetDateStart, targetDateEnd: these fields define the target dates, which
  discount can be applied for


