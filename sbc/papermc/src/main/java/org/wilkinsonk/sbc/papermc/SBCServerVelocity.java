package org.wilkinsonk.sbc.papermc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.io.ByteArrayDataInput;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;


import org.wilkinsonk.sbc.SBCServerPlugin;
import org.wilkinsonk.sbc.SoulardiganBackyard;
import org.wilkinsonk.sbc.SoulardiganBackyard.ServerEntry;

@Plugin(
    id = SoulardiganBackyard.NAME,
    name = SoulardiganBackyard.NAME_LONG,
    version = SoulardiganBackyard.BUILD_VERSION
)
public class SBCServerVelocity implements SBCServerPlugin {
    private static final MinecraftChannelIdentifier CHANNEL_REQUEST_SERVER_LIST = MinecraftChannelIdentifier.from(SoulardiganBackyard.CHANNEL_TOPIC_REQUEST_SERVER_LIST);
    private static final MinecraftChannelIdentifier CHANNEL_RESPOND_SERVER_LIST = MinecraftChannelIdentifier.from(SoulardiganBackyard.CHANNEL_TOPIC_RESPOND_SERVER_LIST);
    private static final MinecraftChannelIdentifier CHANNEL_CONNECT_TO_SERVER   = MinecraftChannelIdentifier.from(SoulardiganBackyard.CHANNEL_TOPIC_CONNECT_TO_SERVER);

    private final ProxyServer Proxy;
    private final Logger Logger;

    @Inject
    public SBCServerVelocity(ProxyServer proxy, Logger logger) {
        Proxy = proxy;
        Logger = logger;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        RegisterChannels();
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!(event.getSource() instanceof Player player)) return;

        if (event.getIdentifier().equals(CHANNEL_CONNECT_TO_SERVER)) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            byte[] data = event.getData();
            if (data == null) return;
            String serverId = readString(ByteStreams.newDataInput(data));
            Proxy.getServer(serverId).ifPresentOrElse(
                server -> player.createConnectionRequest(server).connectWithIndication(),
                () -> Logger.warn("Player '{}' requested unknown server '{}'", player.getUsername(), serverId)
            );
            return;
        }

        if (!event.getIdentifier().equals(CHANNEL_REQUEST_SERVER_LIST)) return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        Logger.info("Received server list request from player '{}'", player.getUsername());

        GetServerList().thenAccept(servers -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            writeVarInt(out, servers.size());
            servers.forEach(server -> {
                writeString(out, server.Id());
                writeString(out, server.Name());
                writeString(out, server.IconMaterial());
                writeBoolean(out, server.IsOnline());
            });
            Logger.info("Sending server list response to player '{}' with {} server(s)", player.getUsername(), servers.size());
            player.sendPluginMessage(CHANNEL_RESPOND_SERVER_LIST, out.toByteArray());
        });
    }

    public void RegisterChannels() {
        Proxy.getChannelRegistrar().register(CHANNEL_REQUEST_SERVER_LIST);
        Proxy.getChannelRegistrar().register(CHANNEL_RESPOND_SERVER_LIST);
        Proxy.getChannelRegistrar().register(CHANNEL_CONNECT_TO_SERVER);
    }

    public CompletableFuture<List<ServerEntry>> GetServerList() {
        List<CompletableFuture<ServerEntry>> futures = Proxy.getAllServers().stream()
            .map(server -> {
                String name = server.getServerInfo().getName();
                return server.ping()
                    .<ServerEntry>thenApply(sp -> new ServerEntry(name, name, "", true))
                    .exceptionally(ex -> new ServerEntry(name, name, "", false));
            })
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    private static int readVarInt(ByteArrayDataInput in) {
        int value = 0;
        int shift = 0;
        byte b;
        do {
            b = in.readByte();
            value |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return value;
    }

    private static String readString(ByteArrayDataInput in) {
        byte[] bytes = new byte[readVarInt(in)];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void writeVarInt(ByteArrayDataOutput out, int value) {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    private static void writeString(ByteArrayDataOutput out, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static void writeBoolean(ByteArrayDataOutput out, Boolean value) {
        writeVarInt(out, Boolean.compare(value, false));
    }
}
