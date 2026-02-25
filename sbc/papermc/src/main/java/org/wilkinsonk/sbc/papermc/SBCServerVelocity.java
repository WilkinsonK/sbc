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
import org.slf4j.Logger;

import org.wilkinsonk.sbc.SoulardiganBackyard;
import org.wilkinsonk.sbc.model.ServerEntry;
import org.wilkinsonk.sbc.payload.Payload;

@Plugin(
    id = SoulardiganBackyard.NAME,
    name = SoulardiganBackyard.NAME_LONG,
    version = SoulardiganBackyard.BUILD_VERSION
)
public class SBCServerVelocity extends SBCServer {

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

        registerChannel(Payload.REQUEST_CONNECT);
        registerChannel(Payload.REQUEST_SERVER_LIST);
        registerChannel(Payload.RESPOND_SERVER_ICON);
        registerChannel(Payload.RESPOND_SERVER_LIST);
        channels.values().forEach((channel) -> {
            Proxy.getChannelRegistrar().register(channel);
        });
    }

    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        byte[] request = new byte[0];
        event.getPlayer().getCurrentServer().ifPresentOrElse(
            conn -> conn.sendPluginMessage(getChannel(Payload.RESPOND_SERVER_ICON), request),
            () -> Logger.warn("No active connection found when requesting icon for '{}'", event.getPlayer().getUsername())
        );
    }

    @Subscribe
    public void onDeclareServerIcon(PluginMessageEvent event) {
        if (!eventIsChannel(event.getIdentifier(), Payload.RESPOND_SERVER_ICON)) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(event.getSource() instanceof ServerConnection serverConnection)) return;

        String serverName = serverConnection.getServerInfo().getName();
        try {
            String json = new String(event.getData());
            String icon = Payload.RESPOND_SERVER_ICON.FromJson(json).getIcon();
            serverIcons.put(serverName, icon);
            Logger.info("Cached icon for '{}': '{}'", serverName, icon);
        } catch (Exception e) {
            Logger.error("Failed to deserialize server icon", e);
        }
    }

    @Subscribe
    public void onConnectToServer(PluginMessageEvent event) {
        if (!(event.getSource() instanceof Player player)) return;
        if (!eventIsChannel(event, Payload.REQUEST_CONNECT)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());
        byte[] data = event.getData();
        if (data == null) return;

        try {
            String json = new String(data);
            Payload.RequestConnect payload = Payload.REQUEST_CONNECT.FromJson(json);
            Proxy.getServer(payload.getServerId()).ifPresentOrElse(
                server -> player.createConnectionRequest(server).connectWithIndication(),
                () -> Logger.warn("Player '{}' requested unknown server '{}'", player.getUsername(), payload.getServerId())
            );
        } catch (Exception e) {
            Logger.error("Failed to deserialize server connect request", e);
        }
    }

    @Subscribe
    public void onRequestServerList(PluginMessageEvent event) {
        if (!(event.getSource() instanceof Player player)) return;
        if (!eventIsChannel(event, Payload.REQUEST_SERVER_LIST)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());
        Logger.info("Received server list request from player '{}'", player.getUsername());
        String currentServerName = player.getCurrentServer()
            .map(sc -> sc.getServerInfo().getName())
            .orElse("");
        GetServerList(currentServerName).thenAccept(servers -> {
            try {
                String json = Payload.RespondServerList
                    .builder()
                    .servers(servers)
                    .build()
                    .IntoJson();
                Logger.info("Sending server list response to player '{}' with {} server(s)", player.getUsername(), servers.size());
                player.sendPluginMessage(getChannel(Payload.RESPOND_SERVER_LIST), json.getBytes());
            } catch (Exception e) {
                Logger.error("Failed to serialize server list response", e);
            }
        });
    }

    private CompletableFuture<List<ServerEntry>> GetServerList(String currentServerName) {
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
