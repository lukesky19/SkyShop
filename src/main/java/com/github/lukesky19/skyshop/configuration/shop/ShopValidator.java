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
package com.github.lukesky19.skyshop.configuration.shop;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.util.enums.TransactionType;

import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

/**
 * This class manages the validation logic for shop configurations.
*/
public class ShopValidator {
    final SkyShop plugin;

    /**
     * Constructor
     * @param plugin The plugin's instance.
    */
    public ShopValidator(SkyShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if a shop configuration is valid.
     * @param shopConfiguration The ShopConfiguration to be validated.
     * @param shopId The ID of the shop being validated, used for error messages.
     * @return true if valid, false if not.
    */
    public boolean isShopConfigValid(ShopConfiguration shopConfiguration, String shopId) {
        ComponentLogger logger = this.plugin.getComponentLogger();
        // Check if the shop configuration was found and properly loaded.
        if (shopConfiguration == null) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>Unable to load <yellow>" + shopId + ".yml</yellow> configuration. Does it exist?</red>"));
            return false;
        }

        // Check if the shop has at least one page configured.
        Map<String, ShopConfiguration.ShopPage> pageMap = shopConfiguration.pages();
        if (pageMap == null || pageMap.isEmpty()) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>No pages were found in <yellow>" + shopId + ".yml</yellow>. Are any configured?</red>"));
            return false;
        }

        // Verify each configured page is valid.
        for (Map.Entry<String, ShopConfiguration.ShopPage> pageEntry : pageMap.entrySet()) {
            String pageId = pageEntry.getKey();
            ShopConfiguration.ShopPage page = pageEntry.getValue();

            // Check if the size of the page is valid.
            if (page.size() % 9 != 0 || page.size() < 9 || page.size() > 54) {
                logger.error(MiniMessage.miniMessage().deserialize("<red>The page size for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> is invalid.</red>"));
                logger.error(MiniMessage.miniMessage().deserialize("<red>Valid sizes include: <yellow>9</yellow>, <yellow>18</yellow>, <yellow>27</yellow>, <yellow>36</yellow>, <yellow>45</yellow>, and <yellow>54</yellow>.</red>"));
                return false;
            }

            // Verify that a name was set for the page (will be used for the Inventory title)
            if (page.name() == null) {
                logger.error(MiniMessage.miniMessage().deserialize("<red>The name for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> is invalid. Is it a valid string?</red>"));
                return false;
            }

            // Check if the page has at least one entry
            Map<String, ShopConfiguration.ShopEntry> shopEntryMap = page.entries();
            if (shopEntryMap == null || shopEntryMap.isEmpty()) {
                logger.error(MiniMessage.miniMessage().deserialize("<red>No entries were found for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow>. Are any configured?</red>"));
                return false;
            }

            // Verify that each entry in a page is valid.
            for (Map.Entry<String, ShopConfiguration.ShopEntry> shopEntry : shopEntryMap.entrySet()) {
                String itemId = shopEntry.getKey();
                ShopConfiguration.ShopEntry entry = shopEntry.getValue();
                TransactionType entryType;

                // Verifies the type is valid and allowed in this context.
                try {
                    entryType = TransactionType.valueOf(entry.type());
                    if(entryType.equals(TransactionType.OPEN_SHOP)) {
                        logger.error(MiniMessage.miniMessage().deserialize("<red>The type for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> is not allowed in this context.</red>"));
                        logger.error(MiniMessage.miniMessage().deserialize("<red>Valid types: <yellow>FILLER</yellow>, <yellow>PREVIOUS_PAGE</yellow>, <yellow>NEXT_PAGE</yellow>, <yellow>RETURN</yellow>, <yellow>ITEM</yellow>, and <yellow>COMMAND</yellow>.</red>"));
                        return false;
                    }
                } catch (IllegalArgumentException exception) {
                    logger.error(MiniMessage.miniMessage().deserialize("<red>The type for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> is invalid. Is it a valid type?</red>"));
                    logger.error(MiniMessage.miniMessage().deserialize("<red>Valid types: <yellow>FILLER</yellow>, <yellow>PREVIOUS_PAGE</yellow>, <yellow>NEXT_PAGE</yellow>, <yellow>RETURN</yellow>, <yellow>ITEM</yellow>, and <yellow>COMMAND</yellow>.</red>"));
                    return false;
                }

                // Checks for any out of bounds slots (based on page size).
                // Checks for any entries with overlapping slots.
                switch(entryType) {
                    case RETURN, NEXT_PAGE, PREVIOUS_PAGE, ITEM, COMMAND -> {
                        // Verify the slot is in the bounds of the page size.
                        if (entry.slot() == null || entry.slot() < 0 || entry.slot() > page.size() - 1) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The slot for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> is invalid.</red>"));
                            logger.error(MiniMessage.miniMessage().deserialize("<red>Slot must be greater than or equal to <yellow>0</yellow> or less than or equal to <yellow> " + (page.size() - 1) + "</yellow>.</red>"));
                            return false;
                        }
                        // Check for any entries with overlapping slots
                        for (Map.Entry<String, ShopConfiguration.ShopEntry> compareShopEntry : page.entries().entrySet()) {
                            ShopConfiguration.ShopEntry compareEntry = compareShopEntry.getValue();
                            String compareItemId = compareShopEntry.getKey();
                            if (!entry.equals(compareEntry) && entry.slot().equals(compareEntry.slot())) {
                                logger.error(MiniMessage.miniMessage().deserialize("<red>The slot for entry <yellow>" + itemId + "</yellow> has the same slot as entry <yellow>" + compareItemId + "</yellow> on page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow>.</red>"));
                                logger.error(MiniMessage.miniMessage().deserialize("<red>Two entries cannot share the same slot.</red>"));
                                return false;
                            }
                        }
                    }
                }

                // Checks that the item for each entry is valid.
                switch(entryType) {
                    case FILLER, NEXT_PAGE, PREVIOUS_PAGE, RETURN, ITEM, COMMAND -> {
                        ShopConfiguration.Item item = entry.item();
                        if (item.name() == null) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The item name for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> is invalid.</red>"));
                            return false;
                        }

                        try {
                            Material.valueOf(item.material());
                        } catch (IllegalArgumentException e) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The item material for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> is invalid.</red>"));
                            return false;
                        }
                    }
                }

                // Checks if an entry of type NEXT_PAGE or PREVIOUS_PAGE is configured, but on the last or first page respectively.
                List<Map.Entry<String, ShopConfiguration.ShopPage>> pageList = shopConfiguration.pages().entrySet().stream().toList();
                switch(entryType) {
                    case NEXT_PAGE -> {
                        if (pageList.getLast().getValue().equals(page)) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The entry for <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> has a NEXT_PAGE button, but this is the last page.</red>"));
                            logger.error(MiniMessage.miniMessage().deserialize("<red>Either add another page after <yellow>" + pageId + "</yellow> or remove entry <yellow>" + itemId + "</yellow>.</red>"));
                            return false;
                        }
                    }
                    case PREVIOUS_PAGE -> {
                        if (pageList.getFirst().getValue().equals(page)) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The entry for <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> has a PREVIOUS_PAGE button, but this is the first page.</red>"));
                            logger.error(MiniMessage.miniMessage().deserialize("<red>Remove entry <yellow>" + itemId + "</yellow> to fix.</red>"));
                            return false;
                        }
                    }
                }

                // Verify the buy and sell prices for type ITEM and COMMAND
                switch(entryType) {
                    case ITEM, COMMAND -> {
                        ShopConfiguration.Prices prices = entry.prices();
                        if (prices.buyPrice() == null || (prices.buyPrice() < 0.0 && prices.buyPrice() != -1.0)) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The buy-price for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> is invalid.</red>"));
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The buy-price must be greater than or equal to <yellow>0</yellow> or <yellow>-1</yellow> to disable buying.</red>"));
                            return false;
                        }
                        if (prices.sellPrice() == null || (prices.sellPrice() < 0.0 && prices.sellPrice() != -1.0)) {
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The sell-price for entry <yellow>" + itemId + "</yellow> for page <yellow>" + pageId + "</yellow> in <yellow>" + shopId + ".yml</yellow> is invalid.</red>"));
                            logger.error(MiniMessage.miniMessage().deserialize("<red>The sell-price must be greater than or equal to <yellow>0</yellow> or <yellow>-1</yellow> to disable selling.</red>"));
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
