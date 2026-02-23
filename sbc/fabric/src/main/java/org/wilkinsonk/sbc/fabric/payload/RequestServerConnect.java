package org.wilkinsonk.sbc.fabric.payload;

import org.wilkinsonk.sbc.SoulardiganBackyard;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RequestServerConnect(String serverId) implements CustomPayload {
    public static final Id<RequestServerConnect> ID = new CustomPayload.Id<>(Identifier.of(SoulardiganBackyard.NAME, SoulardiganBackyard.CHANNEL_TOPIC.CONNECT_TO_SERVER));
    public static final PacketCodec<PacketByteBuf, RequestServerConnect> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, RequestServerConnect::serverId,
        RequestServerConnect::new
    );

    @Override
    public Id<RequestServerConnect> getId() { return ID; }
}
