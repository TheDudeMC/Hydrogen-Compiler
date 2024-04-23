# Hydrogen Syntax

**Hydrogen is a concise, compiled programming language using mcfunctions as it's base language.  While Hydrogen adds a lot of functionality common to programming languages to Minecraft Datapacks, it is still a completely cursed language, user discretion is advised.**

___

## Table of Contents
1. [Definitions](#def-definition)
2. [Variables](#var-variable)
3. [Branches](#branches-if-else-if-else-scope)
4. [Loops](#loops-for-while-delay)
5. [Classes](#classes-and-class-functions)

___

## Def (Definition)

**Def is used to define variables of generic types, you can store all numeric and string values as well as full jsons and lists and reference any of it's members**


| Hydrogen line              |  | MCFunction line(s)                                                                                       | Functionality                                                             |
|----------------------------|--|----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| ```def x = {foo: "bar"}``` |~>| data modify storage minecraft:path_to_file_x data set value \{foo: "bar"\}                               | Create a new storage instance with static data                            |
| ```def y = x.foo```        |~>| data modify storage minecraft:path_to_file_y data set from storage minecraft:path_to_file_x data.foo     | Create a new storage instance with data from existing storage             |
| ```x.bar = "foo"```        |~>| data modify storage minecraft:path_to_file_x data.bar set value "foo"                                    | Updates existing storage instance at path data.bar with static data       |
| ```x.foo = y.bar```        |~>| data modify storage minecraft:path_to_file_x data.foo set from storage minecraft:path_to_file_y data.bar | Updates existing storage instance with data from another existing storage |


Definitions alone don't add much to Minecraft Datapacks, this is essentially just a few short hands making it easier to read and write to storages for your datapack.  And while this isn't terribly interesting, comparing the concise syntax of the Hydrogen syntax to the MCFunction syntax, you can see a very basic advantage to switching to Hydrogen.

___


## Var (Variable)

**Var is used to define variables of numeric values, you can run mathmatic functions such add, subtract, multiply and divide on vars**

| Hydrogen line              |  | MCFunction line(s)                                                                                       | Functionality                                                             |
|----------------------------|--|----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| ```var x = 5```            |~>| <ol><li>scoreboard objectives add path_to_file_x dummy "path_to_file_x"</li><li>scoreboard players set @a path_to_file_x 5</li></ol> | Create a new variable instance with static data |
| ```var y = x```            |~>| <ol><li>scoreboard objectives add path_to_file_x dummy "path_to_file_x"</li><li>execute store result score @a var_name run scoreboard players get @p other_var_name</li></ol> | Create a new variable instance with data from existing variable |
| ```var z = w.foo```        |~>| <ol><li>scoreboard objectives add path_to_file_x dummy "path_to_file_x"</li><li>execute store result score @a var_name run data get storage minecraft:path_to_file_w data.foo</li></ol> | Create a new variable instance with data from existing storage |

Variables much like definitions are used to store data for your datapack, above you can see how we initialize the vars.  The main difference is variables can only store integer values which can used to perform math equations.  And that's where the fun begins for Hydrogen. 

![Dumbass meme reference](https://media.giphy.com/media/3se2U9ZAJr7DW/giphy.gif "Dumbass meme reference")

You can now execute full complex math equations within your datapacks, see below for some basic examples and how Hydrogen compiles the code into mcfunctions.


| Hydrogen line              |  | MCFunction line(s)                                                                                       | Functionality                                                             |
|----------------------------|--|----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| ```x = x + 1```            |~>| <ol><li>data modify storage minecraft:rhs_storage data set value [0, 1]</li><li>execute store result storage minecraft:rhs_storage data[0] int 1 run scoreboard players get @p code_dude_reg_x</li><li>execute store result score @a reg_rhs_op_1 run data get storage minecraft:rhs_storage data[0] 1</li><li>execute store result score @a reg_rhs_op_2 run data get storage minecraft:rhs_storage data[1] 1</li><li>scoreboard players operation @a reg_rhs_op_1 += @p reg_rhs_op_2</li><li>execute store result storage minecraft:rhs_storage data[1] int 1 run scoreboard players get @p reg_rhs_op_1</li><li>execute store result score @a reg_rhs run scoreboard players get @p reg_rhs_op_1</li><li>execute store result score @a code_dude_reg_x run scoreboard players get @p reg_rhs</li></ol> | Increments the variable's value up by one |
| ```x = x + y```            |~>| <ol><li>data modify storage minecraft:rhs_storage data set value [0, 0]</li><li>execute store result storage minecraft:rhs_storage data[0] int 1 run scoreboard players get @p code_dude_reg_x</li><li>execute store result storage minecraft:rhs_storage data[1] int 1 run scoreboard players get @p code_dude_reg_y</li><li>execute store result score @a reg_rhs_op_1 run data get storage minecraft:rhs_storage data[0] 1</li><li>execute store result score @a reg_rhs_op_2 run data get storage minecraft:rhs_storage data[1] 1</li><li>scoreboard players operation @a reg_rhs_op_1 += @p reg_rhs_op_2</li><li>execute store result storage minecraft:rhs_storage data[1] int 1 run scoreboard players get @p reg_rhs_op_1</li><li>execute store result score @a reg_rhs run scoreboard players get @p reg_rhs_op_1</li><li>execute store result score @a code_dude_reg_x run scoreboard players get @p reg_rhs</li></ol> | Increments the variable's value up by the value of another variable |

These are very simple examples but since it generates so many lines of code I won't hit you with any more walls of generated code.  The main take away is this compiles regardless of complexity so the expression (3 * x) / ((y + 1) - (2 * z)) will be processed accurately.  Currently we only support addition, subtraction, multiplication and division.  Hydrogen is still currently being supported so if you feel like exponents and boolean logic should be added please leave feedback.  It is possible to support them but due to the requirements Hydrogen solves, evaluating exponents was not needed in it's initial release. 

___


## Fuction Parameters (call / delay call)
**Hydrogen also adds the capability to pass parameters into other functions.  However this requires some syntax changes to hydrogen files.**

___


## Branches (If / Else If / Else / Scope)

**Braches allow for branching logic that is easily readable in your code.  Hydrogen has all the standard branches (if, else if, else) but also introduces a novel branch defined as "scope".**

| Hydrogen line              |  | MCFunction line(s)                                                                                       | Functionality                                                             |
|----------------------------|--|----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| if entity @e[tag=foo]<br/> {<br/> say bar<br/> }        |~>| <ol><li>execute if entity @e[tag=foo] run function minecraft:.code/generated/path_to_file_if_0</li><li>*Generated in file path_to_file_if_0.mcfunction*</li><li>say bar</li></ol> | Checks if an entity exists with the tag "foo" and if success, run the generated mcfunction.|
| if entity @e[tag=foo]<br/> {<br/> say bar<br/> } else if entity @e[tag=foo2] {<br/> say bar2<br/>} |~>| <ol><li>execute if entity @e[tag=foo] run function minecraft:.code/generated/path_to_file_if_0</li><li>execute unless entity @e[tag=foo] run execute if entity @e[tag=foo2] run function minecraft:.code/generated/path_to_file_else_if_0</li><li>*Generated in file path_to_file_if_0.mcfunction*</li><li>say bar</li><li>*Generated in file path_to_file_else_if_0.mcfunction*</li><li>say bar2</li></ol> | Checks if an entity exists with the tag "foo"; if success, run the generated mcfunction and if fail checks if an entity exists with the tag "foo2" and if success, run the generated mcfunction for the block.|
| if entity @e[tag=foo]<br/> {<br/> say bar<br/> } else {<br/> say bar2<br/>} |~>| <ol><li>execute if entity @e[tag=foo] run function minecraft:.code/generated/path_to_file_if_0</li><li>execute unless entity @e[tag=foo] run function minecraft:.code/generated/path_to_file_else0</li><li>*Generated in file path_to_file_if_0.mcfunction*</li><li>say bar</li><li>*Generated in file path_to_file_else_0.mcfunction*</li><li>say bar2</li></ol> | Checks if an entity exists with the tag "foo"; if success, run the generated mcfunction and if failed, run the generated mcfunction for the block.|
| scope @e[type=armor_stand]<br/> {<br/> say stand<br/> } |~>| <ol><li>execute as entity @e[tag=foo] run function minecraft:.code/generated/path_to_file_scope_0</li><li>*Generated in file path_to_file_scope_0.mcfunction*</li><li>say stand</li></ol> | Shifts the scope of the code block to be run as every armor stand that is currently loaded in the Minecraft world and executes the block making every armor stand say "stand". |

While branching logic was already possible with mcfuctions, Hydrogen makes sure branching logic in your code is generated as efficently as possible where the branch check is checked once and if it passes, it runs the code block from a new generated file.  Mcfuctions also have a unique paradigm for branching logic that Hydrogen acknowledges as "scope".  Scope utilizes the Minecraft command execute's parameters as, at, align, achored, facing, in, on, positioned, & rotated to shift the scope of the code to be run based off of these modifiers.

Scope is great to utilize when, for example, updating multiple entities with the same code, like if one player triggers an event in an adventure map, you can easily set the scope to all players, teleport them to a specific location, and apply a potion effect with very minimal boilerplate code.

___


## Loops (for / while / delay)

**Hydrogen also allows for looping syntax within your code.**

| Hydrogen line              |  | MCFunction line(s)                                                                                       | Functionality                                                             |
|----------------------------|--|----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|

While again loops are possible with mcfuctions, the only way to accomplish them is with recurrsion.  And due to mcfunctions limited syntax and lack of being able to define multiple functions within a file and no concept of classes, this syntax adds way too much complexity for basic loops.  This is another area where Hydrogen introduces a unique aspect into it's syntax with the introduction of a new keyword "delay".  Delay can be appended to a loop to incoperate a delay in between each time the loop block is run.  This keyword allows for very concise writing of asynchronous code.

Delay works much like how multithreading used to work for old, single core CPUs.  With a single core, true multithreading was not possible and to mimic multithreading the kernal would pause one process from being able to access the CPU to allow for a separate process to execute code.  

Delay is excellent for breaking your code up to be executed in multiple ticks in game to prevent performance issues such as freezing the game on a current tick as hundreds or thousands of lines of commands try to execute or for creating smooth animations that incriment every tick.

___


## Classes and Class Functions

**If you made it this far, we assume you're pretty familiar with programming already so you're familiar with code classes and class functions.  Hydrogen currently does not support classes due to the fact the complexity of the code written in Hydrogen did not warrent their addition.  We are confident it is technically possible to incorporate classes into Hydrogen but due to the fact mcfunctions provide large overhead to the base game, we opted for a lower level language approach to minimize the amount of code generated and run per function.  Again, if this is a change you would like to see, please leave us feedback and we will work to incorporate classes into Hydrogen.**
