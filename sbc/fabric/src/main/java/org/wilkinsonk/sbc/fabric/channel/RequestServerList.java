package org.wilkinsonk.sbc.fabric.channel;

import org.wilkinsonk.sbc.payload.Channel;
import org.wilkinsonk.sbc.payload.Payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public class RequestServerList extends ChannelProcessorBase<Payload.RequestServerList> {
    private static final Channel<Payload.RequestServerList> CHANNEL = Payload.REQUEST_SERVER_LIST;

    public static final Id<RequestServerList> ID = buildId(CHANNEL);
    public static final PacketCodec<PacketByteBuf, RequestServerList> CODEC =
        makeCodec(CHANNEL, body -> new RequestServerList());

    @Override
    public Channel<Payload.RequestServerList> getChannel() { return CHANNEL; }

    @Override
    public Payload.RequestServerList getBody() { return Payload.RequestServerList.builder().build(); }

    public static void register() { registerC2S(ID, CODEC); }

    @Override
    public Id<RequestServerList> getId() { return ID; }
}
