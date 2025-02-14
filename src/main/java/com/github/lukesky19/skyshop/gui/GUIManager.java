package com.github.lukesky19.skyshop.gui;

import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import com.github.lukesky19.skyshop.SkyShop;
import com.github.lukesky19.skyshop.SkyShopAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {
    private final SkyShop skyShop;
    private final SkyShopAPI skyShopAPI;
    private final HashMap<UUID, ChestGUI> openGuis = new HashMap<>();

    public GUIManager(SkyShop skyShop, SkyShopAPI skyShopAPI) {
        this.skyShop = skyShop;
        this.skyShopAPI = skyShopAPI;
    }

    @Nullable
    public ChestGUI getOpenGUI(@NotNull UUID uuid) {
        return openGuis.get(uuid);
    }

    public void addOpenGUI(@NotNull UUID uuid, @NotNull ChestGUI chestGui) {
        skyShop.getServer().getScheduler().runTaskLater(skyShop, () -> openGuis.put(uuid, chestGui), 1L);
    }

    public void removeOpenGUI(@NotNull UUID uuid) {
        skyShop.getServer().getScheduler().runTaskLater(skyShop, () -> openGuis.remove(uuid), 1L);
    }

    public void closeOpenGUIs(boolean onDisable) {
        if(!onDisable) {
            for(Map.Entry<UUID, ChestGUI> entry : openGuis.entrySet()) {
                UUID uuid = entry.getKey();
                ChestGUI gui = entry.getValue();
                Player player = Bukkit.getPlayer(uuid);

                if (player != null && player.isConnected() && player.isOnline()) {
                    if(gui instanceof SellAllGUI sellAllGUI) {
                        skyShop.getServer().getScheduler().runTaskLater(skyShop, () ->
                                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);

                        // Remove any buttons so that they aren't sold or given to the player.
                        sellAllGUI.clearButtons();

                        // Proceed to sell any items in the inventory
                        skyShopAPI.sellInventoryGUI(sellAllGUI.getInventory(), player, true);
                    } else {
                        skyShop.getServer().getScheduler().runTaskLater(skyShop, () ->
                                player.closeInventory(InventoryCloseEvent.Reason.UNLOADED), 1L);
                    }
                }
            }
        } else {
            for(Map.Entry<UUID, ChestGUI> entry : openGuis.entrySet()) {
                UUID uuid = entry.getKey();
                ChestGUI gui = entry.getValue();
                Player player = Bukkit.getPlayer(uuid);

                if (player != null && player.isConnected() && player.isOnline()) {
                    if(gui instanceof SellAllGUI sellAllGUI) {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);

                        // Remove any buttons so that they aren't sold or given to the player.
                        sellAllGUI.clearButtons();

                        // Proceed to sell any items in the inventory
                        skyShopAPI.sellInventoryGUI(sellAllGUI.getInventory(), player, true);
                    } else {
                        player.closeInventory(InventoryCloseEvent.Reason.UNLOADED);
                    }
                }
            }
        }

        openGuis.clear();
    }
}
