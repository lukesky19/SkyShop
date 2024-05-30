/*
    SkyShop is a simple inventory based shop plugin with page support, error checking, and configuration validation.
    Copyright (C) 2024  lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skyshop.configuration.menu;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.ConfigurationUtility;
import com.github.lukesky19.skyshop.util.enums.TransactionType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

/**
 * This class manages the validation logic for menu configuration (menu.yml).
*/
public class MenuValidator {
    final SkyShop skyShop;
    final ConfigurationUtility configurationUtility;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param configurationUtility A ConfigurationUtility instance.
    */
    public MenuValidator(SkyShop skyShop, ConfigurationUtility configurationUtility) {
        this.skyShop = skyShop;
        this.configurationUtility = configurationUtility;
    }

    /**
     * Checks if menu.yml is valid.
     * @param menuConfiguration The MenuConfiguration to be validated.
     * @return true if valid, false if not.
    */
    public boolean isMenuValid(MenuConfiguration menuConfiguration) {
        ComponentLogger logger = this.skyShop.getComponentLogger();
        // Check if menu.yml was found and properly loaded.
        if(menuConfiguration == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>Unable to load <yellow>menu.yml</yellow> configuration. Does it exist?</red>"));
            return false;
        }

        // Check if at least one page is configured.
        Map<String, MenuConfiguration.MenuPage> pageMap = menuConfiguration.pages();
        if(pageMap == null || pageMap.isEmpty()) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>No pages were found in <yellow>menu.yml</yellow>. Are any configured?</red>"));
            return false;
        }

        // Verify each configured page is valid.
        for(Map.Entry<String, MenuConfiguration.MenuPage> pageEntry : pageMap.entrySet()) {
            String pageId = pageEntry.getKey();
            MenuConfiguration.MenuPage page = pageEntry.getValue();

            // Check if the size of the page is valid.
            if(page.size() == null || page.size() % 9 != 0 || page.size() < 9 || page.size() > 54) {
                logger.error(MiniMessage.miniMessage().deserialize("<red>The page size for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> is invalid.</red>"));
                logger.error(MiniMessage.miniMessage().deserialize("<red>Valid sizes include: <yellow>9</yellow>, <yellow>18</yellow>, <yellow>27</yellow>, <yellow>36</yellow>, <yellow>45</yellow>, and <yellow>54</yellow>.</red>"));
                return false;
            }

            // Verify that a name was set for the page (will be used for the Inventory title)
            if(page.name() == null) {
                logger.error(MiniMessage.miniMessage().deserialize("<red>The name for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> is invalid. Is it a valid string?</red>"));
                return false;
            }

            // Check if a page has at least one entry.
            Map<String, MenuConfiguration.MenuEntry> entryMap = page.entries();
            if(entryMap == null || entryMap.isEmpty()) {
                logger.error(MiniMessage.miniMessage().deserialize("<red>No entries were found for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow>. Are any configured?</red>"));
                return false;
            }

            // Verify that each entry in a page is valid.
            for(Map.Entry<String, MenuConfiguration.MenuEntry> menuEntry : entryMap.entrySet()) {
                String itemId = menuEntry.getKey();
                MenuConfiguration.MenuEntry entry = menuEntry.getValue();
                TransactionType entryType;

                // Verifies the type is a valid and allowed in this context.
                try {
                    entryType = TransactionType.valueOf(entry.type());
                    if(entryType.equals(TransactionType.ITEM) || entryType.equals(TransactionType.COMMAND)) {
                        logger.error(MiniMessage.miniMessage().deserialize("<red>The type for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> is not allowed in this context.</red>"));
                        logger.error(MiniMessage.miniMessage().deserialize("<red>Valid types: <yellow>FILLER</yellow>, <yellow>PREVIOUS_PAGE</yellow>, <yellow>NEXT_PAGE</yellow>, <yellow>RETURN</yellow>, and <yellow>OPEN_SHOP</yellow>.</red>"));
                        return false;
                    }
                } catch (IllegalArgumentException exception) {
                    logger.error(MiniMessage.miniMessage().deserialize("<red>The type for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> is invalid. Is it a valid type?</red>"));
                    logger.error(MiniMessage.miniMessage().deserialize("<red>Valid types: <yellow>FILLER</yellow>, <yellow>PREVIOUS_PAGE</yellow>, <yellow>NEXT_PAGE</yellow>, <yellow>RETURN</yellow>, and <yellow>OPEN_SHOP</yellow>.</red>"));
                    return false;
                }

                // Checks for any out of bounds slots (based on page size).
                // Checks for any entries with overlapping slots.
                switch(entryType) {
                    case RETURN, NEXT_PAGE, PREVIOUS_PAGE, OPEN_SHOP  -> {
                        // Verify the slot is in the bounds of the page size.
                        if(entry.slot() == null || entry.slot() < 0 || entry.slot() > page.size() - 1) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The slot for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> is invalid.</red>"));
                            logger.error(MiniMessage.miniMessage().deserialize("<red>Slot must be greater than or equal to <yellow>0</yellow> or less than or equal to <yellow> " + (page.size() - 1) + "</yellow>.</red>"));
                            return false;
                        }
                        // Check for any entries with overlapping slots
                        for (Map.Entry<String, MenuConfiguration.MenuEntry> otherMenuEntry : page.entries().entrySet()) {
                            MenuConfiguration.MenuEntry compareEntry = otherMenuEntry.getValue();
                            String compareItemId = otherMenuEntry.getKey();
                            if(!entry.equals(compareEntry) && entry.slot().equals(compareEntry.slot())) {
                                logger.error(MiniMessage.miniMessage().deserialize("<red>The slot for entry <yellow>" + itemId + "</yellow> has the same slot as entry <yellow>" + compareItemId + "</yellow> on page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow>.</red>"));
                                logger.error(MiniMessage.miniMessage().deserialize("<red>Two entries cannot share the same slot.</red>"));
                                return false;
                            }
                        }
                    }
                }

                if(entryType.equals(TransactionType.OPEN_SHOP)) {
                    // Get a list of all files in plugins/SkyShop/shops
                    List<String> shopFileNames = new ArrayList<>();
                    try(Stream<Path> walk = Files.walk(Path.of(skyShop.getDataFolder() + File.separator + "shops"))) {
                        List<Path>result = walk.filter(Files::isRegularFile).toList();
                        for(Path path : result) {
                            shopFileNames.add(path.getFileName().toString());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    // Verifies that for entries of type OPEN_SHOP have a matching file in the `shops` folder.
                    if(entry.shop() != null) {
                        if(!shopFileNames.contains(entry.shop() + ".yml")) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>No corresponding shop file found in <yellow>plugins/SkyShop/shops</yellow> for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow>.</red>"));
                            return false;
                        }
                    } else {
                        logger.error(MiniMessage.miniMessage().deserialize("<red>The shop for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> is invalid. Is it a valid String?</red>"));
                        return false;
                    }
                }

                // Checks that the item for each entry is valid.
                switch(entryType) {
                    case FILLER, NEXT_PAGE, PREVIOUS_PAGE, RETURN, OPEN_SHOP -> {
                        MenuConfiguration.Item item = entry.item();
                        if(item.name() == null) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The display-item name for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> is invalid.</red>"));
                            return false;
                        }

                        try {
                            Material.valueOf(item.material());
                        } catch (IllegalArgumentException e) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The display-item material for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> is invalid.</red>"));
                            return false;
                        }
                    }
                }

                // Checks if an entry of type NEXT_PAGE or PREVIOUS_PAGE is configured, but on the last or first page respectively.
                List<Map.Entry<String, MenuConfiguration.MenuPage>> pageList = menuConfiguration.pages().entrySet().stream().toList();
                switch(entryType) {
                    case NEXT_PAGE -> {
                        if(pageList.getLast().getValue().equals(page)) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The entry for <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> has a NEXT_PAGE button, but this is the last page.</red>"));
                            logger.error(MiniMessage.miniMessage().deserialize("<red>Either add another page after <yellow>" + pageId + "</yellow> or remove entry <yellow>" + itemId + "</yellow>.</red>"));
                            return false;
                        }
                    }
                    case PREVIOUS_PAGE -> {
                        if(pageList.getFirst().getValue().equals(page)) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The entry for <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>menu.yml</yellow> has a PREVIOUS_PAGE button, but this is the first page.</red>"));
                            logger.error(MiniMessage.miniMessage().deserialize("<red>Remove entry <yellow>" + itemId + "</yellow> to fix.</red>"));
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
