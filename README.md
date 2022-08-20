# BlossomExperienceBottling

BlossomExperienceBottling is a Minecraft Fabric mod in the Blossom-series mods that provides /bottle command.
This mod was made during the [ModFest: Singularity](https://modfest.net/) event.

## Table of contents

- [Dependencies](#dependencies)
- [Config](#config)
- [Commands & their permissions](#commands--their-permissions)
- [Translation keys](#translation-keys)

## Dependencies

* [BlossomLib](https://github.com/BlossomMods/BlossomLib)
* [fabric-permissions-api](https://github.com/lucko/fabric-permissions-api) / [LuckPerms](https://luckperms.net/) /
  etc. (Optional)

## Config

This mod's config file can be found at `config/BlossomMods/BlossomExperienceBottling.json`, after running the server
with the mod at least once.

`langDescriptionLines`: int - how many lines will the Experience Bottle have in its description  
`itemColors`: [ItemColors](#itemcolors) - colors used for text on the Experience Bottle item  
`xpDropOnDeathMultiplier`: float - value by which to multiply current XP on death, set to a negative value to use
vanilla behaviour  
`bottlingSound`: [Sound](#sound) - the sound played when bottling experience  
`usageSound`: [Sound](#sound) - the sound played when using bottled experience

### ItemColors

`title`: String - the text color used for the Experience Bottle items title  
`description`: String - the text color used for the Experience Bottle items lore / description

### Sound

`identifier`: String - the identifier of the sound to play (must have namespace, i.e. `minecraft:`)  
`volume`: float - the volume of the sound (1.0 is normal volume)  
`pitch`: float - the pitch of the sound (range from 0.0 to 2.0, 1.0 is normal pitch)

## Commands & their permissions

- `/bottle` - alias of `/bottle all`  
  Permission: `blossom.bottle` (default.true)
  - `<amount> levels|points` - alias of `/bottle exactly <amount> levels|points`
  - `exactly <amount> levels|points` - create an XP bottle containing exactly `<amount>` levels or points
  - `all` - bottle all player XP
    - `in-increments-of <incrementAmount> levels|points` - bottle all player XP making bottles containing no more than
      `<incrementAmount>` levels or points in each
    - `to-level <amount>` - bottle XP such that the player is left with exactly `<amount>` levels
      - `in-increments-of <incrementAmount> levels|points` - bottle XP such that the player is left with exactly
        `<amount>` levels and making bottles containing no more than `<incrementAmount>` levels or points in each

## Translation keys

only keys with available arguments are shown, for full list, please see
[`src/main/resources/data/blossom/lang/en_us.json`](src/main/resources/data/blossom/lang/en_us.json)

- `blossom.bottling.bottle.description.line-{n}`: 3 arguments (n is line number >= 1) - stored XP, player XP, sum XP
- `blossom.bottling.error.not-enough`: 2 arguments - required XP, current XP
