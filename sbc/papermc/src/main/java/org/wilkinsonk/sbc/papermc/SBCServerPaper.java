package org.wilkinsonk.sbc.papermc;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.wilkinsonk.sbc.ByteArrayUtil;
import org.wilkinsonk.sbc.SoulardiganBackyard;

public class SBCServerPaper extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON);
        getServer().getMessenger().registerIncomingPluginChannel(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON, (channel, player, message) -> {
            String icon = getConfig().getString("icon", "grass_block");
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            ByteArrayUtil.writeString(out, icon);
            player.sendPluginMessage(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON, out.toByteArray());
        });
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    }
}
