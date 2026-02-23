package org.wilkinsonk.sbc.fabric.payload;

import org.wilkinsonk.sbc.SoulardiganBackyard;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestServerList() implements CustomPayload {
    public static final Id<RequestServerList> ID = new CustomPayload.Id<>(Identifier.of(SoulardiganBackyard.NAME, SoulardiganBackyard.CHANNEL_TOPIC.REQUEST_SERVER_LIST));
    public static final PacketCodec<PacketByteBuf, RequestServerList> CODEC = PacketCodec.unit(new RequestServerList());

    @Override
    public Id<RequestServerList> getId() { return ID; }
}
