package org.wilkinsonk.sbc.fabric.channel;

import org.wilkinsonk.sbc.payload.Body;
import org.wilkinsonk.sbc.payload.Channel;

public interface ChannelProcessor<T extends Body> {
    static void register() {}
    Channel<T> getChannel();
    T getBody();
}
