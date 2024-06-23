# SkyShop
## Description
* A simple inventory based shop plugin.
## Features
* A simple inventory based shop plugin.
* Supports multiple pages.
* Supports buying and selling for items and commands.
* Features error checking and configuration validation. We're only human right?
* Features a sellall GUI for quick selling of items.
## Required Dependencies
* PlaceholderAPI
* Vault
## Commands
- /skyshop - Command to open the shop.
  - Alias: /shop
- /skyshop help - Displays the help message.
- /skyshop reload - Reloads the plugin.
- /skyshop sellall - Opens the sellall GUI.
## Permisisons
- `skyshop.commands.shop` - The permission to access the shop.
- `skyshop.commands.sellall` - The permission to access the sellall GUI.
- `skyshop.commands.reload` - The permission to reload the plugin.
## Issues, Bugs, or Suggestions
* Please create a new [Github Issue](https://github.com/lukesky19/SkyShop/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to SkyShop and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.
## FAQ
Q: What versions does this plugin support?

A: 1.19 through 1.21.

Q: Are there any plans to support any other versions?

A: I will always support newer versions of the game. I have no plans to support any version older than 1.19 as I make use of API features added in 1.19. I may drop older versions if it becomes difficult to support or hinders supporting newer versions.

Q: Does this work on Spigot and Paper?

A: This plugin only works with Paper, it makes use of many newer API features that don't exist in the Spigot API. There are no plans to support Spigot.

Q: Is Folia supported?

A: There is no Folia support at this time. I may look into it in the future though.

## Building
```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
