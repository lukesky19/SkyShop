/*
    SkyShop is a simple inventory based shop plugin with page support, sell commands, and error checking.
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
package com.github.lukesky19.skyshop.gui;

import com.github.lukesky19.skylib.format.FormatUtil;
import com.github.lukesky19.skylib.gui.GUIButton;
import com.github.lukesky19.skylib.gui.GUIType;
import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import com.github.lukesky19.skyshop.configuration.manager.LocaleManager;
import com.github.lukesky19.skyshop.configuration.manager.SellAllManager;
import com.github.lukesky19.skyshop.configuration.record.GUI;
import com.github.lukesky19.skyshop.configuration.record.Locale;
import com.github.lukesky19.skyshop.enums.ActionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class is called to create a sellall inventory for a player to sell items.
*/
public class SellAllGUI extends ChestGUI {
    private final SkyShop skyShop;
    private final LocaleManager localeManager;
    private final SkyShopAPI skyShopAPI;
    private final GUIManager guiManager;
    private final Player player;
    private final GUI sellAllGuiConfig;

    /**
     * Constructor
     * @param skyShop The plugin's instance.
     * @param skyShopAPI The SkyShopAPI
     * @param player The player who opened the GUI.
     */
    public SellAllGUI(
            SkyShop skyShop,
            LocaleManager localeManager, GUIManager guiManager,
            SellAllManager sellAllManager,
            SkyShopAPI skyShopAPI,
            Player player) {
        this.skyShop = skyShop;
        this.localeManager = localeManager;
        this.guiManager = guiManager;
        this.skyShopAPI = skyShopAPI;
        this.player = player;
        sellAllGuiConfig = sellAllManager.getSellAllGuiConfig();

        GUIType guiType = GUIType.getType(sellAllGuiConfig.gui().guiType());
        if(guiType == null) {
            throw new RuntimeException("Invalid GUIType");
        }

        String guiName = "";
        if(sellAllGuiConfig.gui().name() != null) guiName = sellAllGuiConfig.gui().name();

        createInventory(player, guiType, guiName, null);

        update();
    }

    /**
     * A method to create all the buttons in the inventory GUI.
    */
    @Override
    public void update() {
        final ComponentLogger logger = skyShop.getComponentLogger();
        final Locale locale = localeManager.getLocale();

        // Clear the GUI of buttons
        clearButtons();

        // Create the placeholders list for errors with the config file associated with this GUI
        List<TagResolver.Single> errorPlaceholders = new ArrayList<>(List.of(Placeholder.parsed("file", "sellall.yml")));

        Map<Integer, GUI.Page> pages = sellAllGuiConfig.gui().pages();
        if(pages == null || pages.isEmpty()) {
            logger.error(FormatUtil.format(locale.noPagesFound(), errorPlaceholders));
            return;
        }

        GUI.Page page = sellAllGuiConfig.gui().pages().firstEntry().getValue();

        // Add the page number to the placeholders.
        errorPlaceholders.add(Placeholder.parsed("page", "0"));

        // Check if at least 1 entry is configured.
        Map<Integer, GUI.Entry> entries  = page.entries();
        if(entries == null || entries.isEmpty()) {
            logger.error(FormatUtil.format(locale.noEntriesFound(), errorPlaceholders));
            return;
        }

        for(Map.Entry<Integer, GUI.Entry> itemEntry : page.entries().entrySet()) {
            final int entryNum = itemEntry.getKey();
            final GUI.Entry entryConfig = itemEntry.getValue();
            final ActionType type = ActionType.getActionType(entryConfig.type());
            final GUI.Item itemConfig = entryConfig.item();

            List<TagResolver.Single> placeholders = List.of(
                    Placeholder.parsed("entry", String.valueOf(entryNum)),
                    Placeholder.parsed("page", String.valueOf(0)),
                    Placeholder.parsed("file", "sellall.yml"));

            List<Component> loreList = itemConfig.lore().stream()
                    .map(loreLine -> FormatUtil.format(player, loreLine))
                    .toList();

            switch(type) {
                case RETURN -> {
                    Material material = Material.getMaterial(itemConfig.material());
                    if(material != null) {
                        GUIButton.Builder builder = new GUIButton.Builder();

                        ItemStack itemStack = ItemStack.of(material);
                        ItemMeta meta = itemStack.getItemMeta();

                        if(itemConfig.name() != null) {
                            meta.displayName(FormatUtil.format(itemConfig.name()));
                        }

                        meta.lore(loreList);

                        itemStack.setItemMeta(meta);

                        builder.setItemStack(itemStack);

                        builder.setAction(event -> Bukkit.getScheduler()
                                        .runTaskLater(skyShop, () -> closeInventory(skyShop, player), 1L));

                        setButton(entryConfig.slot(), builder.build());
                    } else {
                        logger.error(FormatUtil.format(locale.skippingEntryInvalidMaterial(), placeholders));
                    }
                }

                case null -> logger.warn(FormatUtil.format(locale.skippingEntryInvalidType(), placeholders));

                default -> logger.warn(FormatUtil.format(locale.skippingEntryTypeNotAllowed(), placeholders));
            }
        }

        super.update();
    }

    @Override
    public void handleTopDrag(@NotNull InventoryDragEvent inventoryDragEvent) {}

    @Override
    public void handleTopClick(@NotNull InventoryClickEvent event) {
        int slot = event.getSlot();
        GUIButton button = this.getButtonMapping().get(slot);
        if (button != null) {
            event.setCancelled(true);

            button.action().accept(event);
        }
    }

    @Override
    public void openInventory(@NotNull Plugin plugin, @NotNull Player player) {
        super.openInventory(plugin, player);

        guiManager.addOpenGUI(player.getUniqueId(), this);
    }

    @Override
    public void closeInventory(@NotNull Plugin plugin, @NotNull Player player) {
        UUID uuid = player.getUniqueId();

        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);

        // Remove any buttons so that they aren't sold or given to the player.
        clearButtons();

        // Proceed to sell any items in the inventory
        skyShopAPI.sellInventoryGUI(getInventory(), player, true);

        // Stop tracking the GUI as being open
        guiManager.removeOpenGUI(uuid);
    }

    /**
     * Handles the closing of the inventory GUI.
     * Also handles selling of items and returning any un-sellable items.
     * @param inventoryCloseEvent The InventoryCloseEvent
     */
    @Override
    public void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent) {
        if(inventoryCloseEvent.getReason().equals(InventoryCloseEvent.Reason.UNLOADED)) return;

        Player player = (Player) inventoryCloseEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        // Remove any buttons so that they aren't sold or given to the player.
        clearButtons();

        // Proceed to sell any items in the inventory
        skyShopAPI.sellInventoryGUI(inventoryCloseEvent.getInventory(), player, true);

        // Stop tracking the GUI as being open
        guiManager.removeOpenGUI(uuid);
    }
}

