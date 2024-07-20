package com.github.lukesky19.skyshop.api;

import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.configuration.locale.FormattedLocale;
import com.github.lukesky19.skyshop.configuration.locale.LocaleManager;
import com.github.lukesky19.skyshop.configuration.menu.MenuConfiguration;
import com.github.lukesky19.skyshop.configuration.menu.MenuManager;
import com.github.lukesky19.skyshop.configuration.shop.ShopConfiguration;
import com.github.lukesky19.skyshop.configuration.shop.ShopManager;
import com.github.lukesky19.skyshop.util.enums.TransactionType;
import com.github.lukesky19.skyshop.util.gui.InventoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SkyShopAPI {
    SkyShop skyShop;
    InventoryManager inventoryManager;
    LocaleManager localeManager;
    MenuManager menuManager;
    ShopManager shopManager;

    public SkyShopAPI(
            SkyShop skyShop,
            InventoryManager inventoryManager,
            LocaleManager localeManager,
            MenuManager menuManager,
            ShopManager shopManager) {
        this.skyShop = skyShop;
        this.inventoryManager = inventoryManager;
        this.localeManager = localeManager;
        this.menuManager = menuManager;
        this.shopManager = shopManager;
    }

    /**
     * Sells all possible items in the inventory if a sell price is configured in the shop.
     * Any remaining items that weren't sold will be left in the inventory or returned to the player's inventory.
     * @param inventory An inventory containing items.
     */
    public void sell(Inventory inventory, Player player) {
        FormattedLocale messages = localeManager.formattedLocale();
        double money = 0.0;

        for (Map.Entry<String, MenuConfiguration.MenuPage> pageConfig : menuManager.getMenuConfiguration().pages().entrySet()) {
            for (Map.Entry<String, MenuConfiguration.MenuEntry> menuEntryEntry : pageConfig.getValue().entries().entrySet()) {
                if (TransactionType.valueOf(menuEntryEntry.getValue().type()).equals(TransactionType.OPEN_SHOP)) {
                    for (Map.Entry<String, ShopConfiguration.ShopPage> shopPageEntry : shopManager.getShopConfig(menuEntryEntry.getValue().shop()).pages().entrySet()) {
                        for (Map.Entry<String, ShopConfiguration.ShopEntry> shopEntryEntry : shopPageEntry.getValue().entries().entrySet()) {
                            for (ItemStack item : inventory.getContents()) {
                                if (item != null
                                        && TransactionType.valueOf(shopEntryEntry.getValue().type()).equals(TransactionType.ITEM)
                                        && item.getType().equals(Material.valueOf(shopEntryEntry.getValue().item().material()))
                                        && shopEntryEntry.getValue().prices().sellPrice() != -1) {
                                    money = money + (shopEntryEntry.getValue().prices().sellPrice() * item.getAmount());
                                    inventory.removeItem(item);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (inventoryManager.isInventoryRegistered(inventory)) {
            if(!inventory.isEmpty()) {
                for(ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        player.sendMessage(messages.prefix().append(messages.sellallUnsellable()));

                        if (player.getInventory().firstEmpty() != -1) {
                            player.getInventory().addItem(item);
                        } else {
                            player.getWorld().dropItem(player.getLocation(), item);
                        }

                        inventory.removeItem(item);
                    }
                }
            }
        }

        if (money != 0.0) {
            skyShop.getEconomy().depositPlayer(player, money);
            Component sellAllSuccess = MiniMessage.miniMessage().deserialize(messages.sellallSuccess(),
                    Placeholder.parsed("price", String.valueOf(money)),
                    Placeholder.parsed("bal", String.valueOf(skyShop.getEconomy().getBalance(player))));
            player.sendMessage(messages.prefix().append(sellAllSuccess));
        }
    }
}
