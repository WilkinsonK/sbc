package org.wilkinsonk.sbc.fabric.channel;

import java.util.List;

import org.wilkinsonk.sbc.fabric.screen.ServerSelectionGui;
import org.wilkinsonk.sbc.model.ServerEntry;
import org.wilkinsonk.sbc.payload.Channel;
import org.wilkinsonk.sbc.payload.Payload;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public class RespondServerList extends ChannelProcessorBase<Payload.RespondServerList> {
    private static final Channel<Payload.RespondServerList> CHANNEL = Payload.RESPOND_SERVER_LIST;

    public static final Id<RespondServerList> ID = buildId(CHANNEL);
    public static final PacketCodec<PacketByteBuf, RespondServerList> CODEC =
        makeCodec(CHANNEL, RespondServerList::new);

    private final List<ServerEntry> servers;

    public RespondServerList(Payload.RespondServerList body) {
        this.servers = body.getServers();
    }

    @SuppressWarnings("null")
    public static void register() {
        registerS2C(ID, CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new CottonClientScreen(new ServerSelectionGui(payload.servers)));
            });
        });
    }

    public List<ServerEntry> servers() { return servers; }

    @Override
    public Channel<Payload.RespondServerList> getChannel() { return CHANNEL; }

    @Override
    public Payload.RespondServerList getBody() { return Payload.RespondServerList.builder().servers(servers).build(); }

    @Override
    public Id<RespondServerList> getId() { return ID; }
}
