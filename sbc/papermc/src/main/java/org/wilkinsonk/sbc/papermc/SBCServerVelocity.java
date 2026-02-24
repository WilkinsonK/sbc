package org.wilkinsonk.sbc.papermc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import org.wilkinsonk.sbc.SoulardiganBackyard;
import org.wilkinsonk.sbc.SoulardiganBackyard.ServerEntry;
import org.wilkinsonk.sbc.ByteArrayUtil;

@Plugin(
    id = SoulardiganBackyard.NAME,
    name = SoulardiganBackyard.NAME_LONG,
    version = SoulardiganBackyard.BUILD_VERSION
)
public class SBCServerVelocity extends SBCServer {
    private static final MinecraftChannelIdentifier CHANNEL_REQUEST_SERVER_LIST  = MinecraftChannelIdentifier.from(SoulardiganBackyard.CHANNEL_TOPIC_REQUEST_SERVER_LIST);
    private static final MinecraftChannelIdentifier CHANNEL_RESPOND_SERVER_LIST  = MinecraftChannelIdentifier.from(SoulardiganBackyard.CHANNEL_TOPIC_RESPOND_SERVER_LIST);
    private static final MinecraftChannelIdentifier CHANNEL_CONNECT_TO_SERVER    = MinecraftChannelIdentifier.from(SoulardiganBackyard.CHANNEL_TOPIC_CONNECT_TO_SERVER);
    private static final MinecraftChannelIdentifier CHANNEL_DECLARE_SERVER_ICON  = MinecraftChannelIdentifier.from(SoulardiganBackyard.CHANNEL_TOPIC_DECLARE_SERVER_ICON);

    private final ProxyServer Proxy;
    private final Logger Logger;
    private final Path DataDirectory;
    private final Map<String, String> serverIcons = new ConcurrentHashMap<>();
    private String defaultIcon = ConfigProperty.ICON.DefaultValue;

    @Inject
    public SBCServerVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        Proxy = proxy;
        Logger = logger;
        DataDirectory = dataDirectory;
    }

    private void loadConfig() {
        Path configFile = DataDirectory.resolve("config.properties");
        Properties props = new Properties();
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(DataDirectory);
                ConfigProperty.ICON.Proxy.Apply(props);
                try (OutputStream out = Files.newOutputStream(configFile)) {
                    props.store(out, "SBC Velocity Configuration");
                }
            } catch (IOException e) {
                Logger.error("Failed to write default config, using built-in defaults", e);
                return;
            }
        }
        try (InputStream in = Files.newInputStream(configFile)) {
            props.load(in);
        } catch (IOException e) {
            Logger.error("Failed to load config, using built-in defaults", e);
            return;
        }
        defaultIcon = ConfigProperty.ICON.Proxy.Get(props);
        Logger.info("Loaded config: default-icon='{}'", defaultIcon);
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        loadConfig();
        Proxy.getChannelRegistrar().register(CHANNEL_REQUEST_SERVER_LIST);
        Proxy.getChannelRegistrar().register(CHANNEL_RESPOND_SERVER_LIST);
        Proxy.getChannelRegistrar().register(CHANNEL_CONNECT_TO_SERVER);
        Proxy.getChannelRegistrar().register(CHANNEL_DECLARE_SERVER_ICON);
    }

    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        byte[] request = new byte[0];
        event.getPlayer().getCurrentServer().ifPresentOrElse(
            conn -> conn.sendPluginMessage(CHANNEL_DECLARE_SERVER_ICON, request),
            () -> Logger.warn("No active connection found when requesting icon for '{}'", event.getPlayer().getUsername())
        );
    }

    @Subscribe
    @SuppressWarnings("null")
    public void onDeclareServerIcon(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(CHANNEL_DECLARE_SERVER_ICON)) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(event.getSource() instanceof ServerConnection serverConnection)) return;

        String serverName = serverConnection.getServerInfo().getName();
        String icon = ByteArrayUtil.readString(ByteStreams.newDataInput(event.getData()));
        serverIcons.put(serverName, icon);
        Logger.info("Cached icon for '{}': '{}'", serverName, icon);
    }

    @Subscribe
    public void onConnectToServer(PluginMessageEvent event) {
        if (!(event.getSource() instanceof Player player)) return;
        if (!event.getIdentifier().equals(CHANNEL_CONNECT_TO_SERVER)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());
        byte[] data = event.getData();
        if (data == null) return;
        String serverId = ByteArrayUtil.readString(ByteStreams.newDataInput(data));
        Proxy.getServer(serverId).ifPresentOrElse(
            server -> player.createConnectionRequest(server).connectWithIndication(),
            () -> Logger.warn("Player '{}' requested unknown server '{}'", player.getUsername(), serverId)
        );
    }

    @Subscribe
    public void onRequestServerList(PluginMessageEvent event) {
        if (!(event.getSource() instanceof Player player)) return;
        if (!event.getIdentifier().equals(CHANNEL_REQUEST_SERVER_LIST)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());
        Logger.info("Received server list request from player '{}'", player.getUsername());
        String currentServerName = player.getCurrentServer()
            .map(sc -> sc.getServerInfo().getName())
            .orElse("");
        GetServerList(currentServerName).thenAccept(servers -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            ByteArrayUtil.writeVarInt(out, servers.size());
            servers.forEach(server -> {
                ByteArrayUtil.writeString(out, server.Id());
                ByteArrayUtil.writeString(out, server.Name());
                ByteArrayUtil.writeString(out, server.IconMaterial());
                ByteArrayUtil.writeBoolean(out, server.IsOnline());
                ByteArrayUtil.writeBoolean(out, server.IsCurrentPlayerServer());
            });
            Logger.info("Sending server list response to player '{}' with {} server(s)", player.getUsername(), servers.size());
            player.sendPluginMessage(CHANNEL_RESPOND_SERVER_LIST, out.toByteArray());
        });
        return;
    }

    public CompletableFuture<List<ServerEntry>> GetServerList(String currentServerName) {
        List<CompletableFuture<ServerEntry>> futures = Proxy.getAllServers().stream()
            .map(server -> {
                String name = server.getServerInfo().getName();
                boolean isCurrent = name.equals(currentServerName);
                String icon = serverIcons.getOrDefault(name, defaultIcon);
                return server.ping()
                    .<ServerEntry>thenApply(sp -> new ServerEntry(name, name, icon, true, isCurrent))
                    .exceptionally(ex -> new ServerEntry(name, name, icon, false, isCurrent));
            })
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
}
