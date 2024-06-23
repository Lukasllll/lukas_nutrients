**Please note that this mod isn't officially released yet and may have some bugs as well as features missing!**

# Lukas' Nutrients
Lukas' Nutrients is a highly customizable mod that aims to make Minecraft's food mechanics more engaging by adding nutrients to food. By default the mod provides five food groups (fruits, grains, proteins, vegetables, and sugars).

## Overview

This mod incentivises players to secure multiple different food sources to keep their nutrients balanced.

<img src="https://github.com/Nutrient-Mod/lukas_nutrients/assets/145078665/73362caf-b40f-40f2-a2fc-464c4b48b91a" width="600">

### Nutrients
Nutrients can be added to a few base foods via configs. The mod then calculates the nutrients for all other foods based on their crafting recipes.

Nutrients are added to the player when eating food and decay with the player gaining hunger.

<img src="https://github.com/Nutrient-Mod/lukas_nutrients/assets/145078665/3b10c5d0-46fb-473f-9e38-182bd5d29f1f" width="400">
<img src="https://github.com/Nutrient-Mod/lukas_nutrients/assets/145078665/964f9ed5-73c0-4775-b8f4-6cf9357b97e8" width="400">

Nutrient amounts are then assigned a score between 0 and 2.

<img src="https://github.com/Nutrient-Mod/lukas_nutrients/assets/145078665/06c0c708-80dd-4d71-9608-982259283760" width="600">


### Nutrient Effects
If the current nutrient values fall into certain ranges, effects are applied to the player.

<img src="https://github.com/Nutrient-Mod/lukas_nutrients/assets/145078665/d3e2f913-0d75-4344-a92e-66229b0092b6" width="600">

By default, all nutrient scores are summed together into a value called "Diet". If this total diet score exceeds 6, the player gains maximum health, and depending on the score, further benefits like additional attack damage and movement speed. 
If it falls below 4, the player instead loses maximum health.

All of these effects, as well as their conditions, can be changed via configs.

The mod not only provides support for sums, but also other operators such as multiplication, min, max, exponentials, and logarithms, as well as chaining these operators together.

<img src="https://github.com/Nutrient-Mod/lukas_nutrients/assets/145078665/094b293a-2aec-43a7-b300-a3f819dcba54" width="400">
<img src="https://github.com/Nutrient-Mod/lukas_nutrients/assets/145078665/6fec0611-5b74-4830-bb03-89da63a86fd9" width="400">

### Commands
The mod also provides a few commands for utility.
* `/nutrients list`
* `/nutrients get <player> <id>(optional)`
* `/nutrients set <player> <id> <value>`
* `/nutrients reload`

### Download
**Please note that this mod isn't officially released yet and may have some bugs as well as features missing!**

Check out the current releases at [Nutrient Mod Releases](https://github.com/Nutrient-Mod/lukas_nutrients/releases). You can find a downloadable `.jar` file under "Assets".

### Supported Mods

At the moment, Lukas' Nutrients has full inbuilt support for the following mods:
* Create
* Farmer's Delight
* Nether's Delight
* Autumnity
* Upgrade Aquatic
* Duckling
* Ecologics
* Missing Wilds

As well as for all food crafted with vanilla ingredients.
