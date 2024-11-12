# SkyShop
## Description
* A simple inventory based shop plugin.
## Features
* A simple inventory based shop plugin.
* Supports multiple pages.
* Supports buying and selling for items and commands.
* Features a sellall GUI for quick selling of items.
* Features a variety of sell commands for quick selling of items.
## Required Dependencies
* [SkyLib](https://github.com/lukesky19/SkyLib)
* PlaceholderAPI
* Vault
## Commands
- /skyshop - Command to open the shop.
  - Alias: 
    - /shop
- /skyshop help - Displays the help message.
- /skyshop reload - Reloads the plugin.
- /skyshop sellall - Opens the sellall GUI.
- /sell all - Sells all items inside the player's inventory.
  - Aliases: 
    - /sell
    - /sellall 
- /sell hand - Sells the item in the player's hand.
- /sell hand all - Sells all similar items to the one in the player's hand.
## Permisisons
- `skyshop.commands.skyshop` - The permission to access the /skyshop base command.
- `skyshop.commands.skyshop.shop` - The permission to use /skyshop to open the shop.
- `skyshop.commands.reload` - The permission to access /skyshop reload.
- `skyshop.commands.skyshop.help` The permission to access /skyshop help.
- `skyshop.commands.skyshop.sellall` - The permission to access the sellall GUI (/skyshop sellall).
- `skyshop.commands.sell` - The permission to access the /sell command.
- `skyshop.commands.sell.hand` - The permission to access the /sell hand command.
- `skyshop.commands.sell.hand.all` - The permission to access the /sell hand all command.
- `skyshop.commands.sell.all` - The permission to access the /sell all command.
## Issues, Bugs, or Suggestions
* Please create a new [GitHub Issue](https://github.com/lukesky19/SkyShop/issues) with your issue, bug, or suggestion.
* If an issue or bug, please post any relevant logs containing errors related to SkyShop and your configuration files.
* I will attempt to solve any issues or implement features to the best of my ability.
## FAQ
Q: Does this work support Spigot? Paper?

A: This plugin only works on Paper and forks of Paper. There are no plans to support Spigot.

Q: What versions does this plugin support?

A: 1.21.0, 1.21.1, 1.21.2, 1.21.3, 1.21.4

Q: Are there any plans to support any other versions?

A: I will always support latest major version of the game. For example, 1.21.X.

## For Server Admins/Owners
* Download the plugin [SkyLib](https://github.com/lukesky19/SkyLib/releases).
* Download the plugin from the releases tab and add it to your server.

## For Developers
```./gradlew build publishToMavenLocal```

```koitlin
repositories {
  mavenLocal()
}
```

```koitlin
dependencies {
  compileOnly("com.github.lukesky19:SkyShop:2.0.0")
}
```

## How To Access The API
Follow the "For Developers" section above and then add this code to your plugin.
Then follow the code example below:

```java
SkyShopAPI api;

public SkyShopAPI getSkyShopAPI() {
  return api;
}

@Override
public void onEnable() {
  loadSkyShopAPI();
  // The rest of your plugin's onEnable code.
}

public void loadSkyShopAPI() {
  @Nullable RegisteredServiceProvider<SkyShopAPI> rsp = this.getServer().getServicesManager().getRegistration(SkyShopAPI.class);
  if(rsp != null) {
    api = rsp.getProvider();
  }
}
```

## Building
* Go to [SkyLib](https://github.com/lukesky19/SkyLib) and follow the "For Developers" instructions.
* Then run:
```./gradlew build```

## Why AGPL3?
I wanted a license that will keep my code open source. I believe in open source software and in-case this project goes unmaintained by me, I want it to live on through the work of others. And I want that work to remain open source to prevent a time when a fork can never be continued (i.e., closed-sourced and abandoned).
