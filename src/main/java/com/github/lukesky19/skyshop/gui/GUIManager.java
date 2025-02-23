package com.github.lukesky19.skyshop.gui;

import com.github.lukesky19.skylib.gui.abstracts.ChestGUI;
import com.github.lukesky19.skyshop.SkyShop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {
    private final SkyShop skyShop;
    private final HashMap<UUID, ChestGUI> openGuis = new HashMap<>();

    public GUIManager(SkyShop skyShop) {
        this.skyShop = skyShop;
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
        for(Map.Entry<UUID, ChestGUI> entry : openGuis.entrySet()) {
            UUID uuid = entry.getKey();
            ChestGUI gui = entry.getValue();
            Player player = Bukkit.getPlayer(uuid);

            if (player != null && player.isConnected() && player.isOnline()) {
                gui.unload(skyShop, player, onDisable);
            }
        }

        openGuis.clear();
    }
}
