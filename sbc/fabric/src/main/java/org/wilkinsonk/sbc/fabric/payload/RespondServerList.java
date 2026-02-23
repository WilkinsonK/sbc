package org.wilkinsonk.sbc.fabric.payload;

import java.util.List;

import org.wilkinsonk.sbc.SoulardiganBackyard;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RespondServerList(List<SoulardiganBackyard.ServerEntry> servers) implements CustomPayload {
    private static final PacketCodec<PacketByteBuf, SoulardiganBackyard.ServerEntry> ENTRY_CODEC = PacketCodec.tuple(
        PacketCodecs.STRING,  SoulardiganBackyard.ServerEntry::Id,
        PacketCodecs.STRING,  SoulardiganBackyard.ServerEntry::Name,
        PacketCodecs.STRING,  SoulardiganBackyard.ServerEntry::IconMaterial,
        PacketCodecs.BOOLEAN, SoulardiganBackyard.ServerEntry::IsOnline,
        PacketCodecs.BOOLEAN, SoulardiganBackyard.ServerEntry::IsCurrentPlayerServer,
        SoulardiganBackyard.ServerEntry::new
    );

    public static final Id<RespondServerList> ID = new CustomPayload.Id<>(Identifier.of(SoulardiganBackyard.NAME, SoulardiganBackyard.CHANNEL_TOPIC.RESPOND_SERVER_LIST));
    public static final PacketCodec<PacketByteBuf, RespondServerList> CODEC = PacketCodec.tuple(
        ENTRY_CODEC.collect(PacketCodecs.toList()),
        RespondServerList::servers,
        RespondServerList::new
    );

    @Override
    public Id<RespondServerList> getId() { return ID; }
}
