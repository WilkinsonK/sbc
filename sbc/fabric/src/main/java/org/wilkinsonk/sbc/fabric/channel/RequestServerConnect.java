package org.wilkinsonk.sbc.fabric.channel;

import org.wilkinsonk.sbc.payload.Channel;
import org.wilkinsonk.sbc.payload.Payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public class RequestServerConnect extends ChannelProcessorBase<Payload.RequestConnect> {
    private static final Channel<Payload.RequestConnect> CHANNEL = Payload.REQUEST_CONNECT;
    public static final Id<RequestServerConnect> ID = buildId(CHANNEL);
    public static final PacketCodec<PacketByteBuf, RequestServerConnect> CODEC = makeCodec(CHANNEL, RequestServerConnect::new);
    private final String serverId;

    public RequestServerConnect(String serverId) {
        this.serverId = serverId;
    }

    RequestServerConnect(Payload.RequestConnect body) {
        this.serverId = body.getServerId();
    }

    public static void register() { registerC2S(ID, CODEC); }

    public String serverId() {
        return serverId;
    }

    @Override
    public Channel<Payload.RequestConnect> getChannel() {
        return CHANNEL;
    }

    @Override
    public Payload.RequestConnect getBody() {
        return Payload.RequestConnect.builder().serverId(serverId).build();
    }

    @Override
    public Id<RequestServerConnect> getId() { return ID; }
}
