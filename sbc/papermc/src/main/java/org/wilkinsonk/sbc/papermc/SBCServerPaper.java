package org.wilkinsonk.sbc.papermc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.wilkinsonk.sbc.SoulardiganBackyard;

public class SBCServerPaper extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON);
        getServer().getMessenger().registerIncomingPluginChannel(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON, (channel, player, message) -> {
            String icon = getConfig().getString("icon", "grass_block");
            try {
                String json = ObjectMapper.writeValueAsString(icon);
                player.sendPluginMessage(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON, json.getBytes());
            } catch (Exception e) {
                getLogger().severe("Failed to serialize server icon: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON);
    }
}
