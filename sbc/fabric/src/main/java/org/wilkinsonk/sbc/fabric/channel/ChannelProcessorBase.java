package org.wilkinsonk.sbc.fabric.channel;

import java.util.function.Function;

import org.wilkinsonk.sbc.payload.Body;
import org.wilkinsonk.sbc.payload.Channel;
import org.wilkinsonk.sbc.topic.Topic;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public abstract class ChannelProcessorBase<T extends Body> implements CustomPayload, ChannelProcessor<T> {
    @SuppressWarnings("null")
    protected static <T extends Body, P extends ChannelProcessorBase<T>> PacketCodec<PacketByteBuf, P> makeCodec(Channel<T> channel, Function<T, P> constructor) {
        return PacketCodec.of(
            (processor, buf) -> {
                try { buf.writeString(processor.getBody().IntoJson()); }
                catch (Exception e) { throw new RuntimeException(e); }
            },
            buf -> {
                try { return constructor.apply(channel.FromJson(buf.readString())); }
                catch (Exception e) { throw new RuntimeException(e); }
            }
        );
    }

    protected static <P extends CustomPayload> CustomPayload.Id<P> buildId(Channel<?> channel) {
        Topic t = channel.getTopic();
        return new CustomPayload.Id<>(Identifier.of(t.GetNamespace(), t.GetName()));
    }

    @SuppressWarnings("null")
    protected static <P extends ChannelProcessorBase<?>> void registerC2S(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec) {
        PayloadTypeRegistry.playC2S().register(id, codec);
    }

    @SuppressWarnings("null")
    protected static <P extends ChannelProcessorBase<?>> void registerS2C(CustomPayload.Id<P> id, PacketCodec<PacketByteBuf, P> codec) {
        PayloadTypeRegistry.playS2C().register(id, codec);
    }
}
