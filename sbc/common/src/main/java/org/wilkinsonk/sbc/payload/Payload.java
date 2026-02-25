package org.wilkinsonk.sbc.payload;

import java.util.List;

import org.wilkinsonk.sbc.model.ServerEntry;
import org.wilkinsonk.sbc.topic.Topic;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@SuppressWarnings("null")
public final class Payload {
    public static final Channel<RequestConnect>    REQUEST_CONNECT     = new Channel<>(Topic.REQUEST_CONNECT, RequestConnect.class);
    public static final Channel<RequestServerList> REQUEST_SERVER_LIST = new Channel<>(Topic.REQUEST_SERVER_LIST, RequestServerList.class);
    public static final Channel<RespondServerIcon> RESPOND_SERVER_ICON = new Channel<>(Topic.RESPOND_SERVER_ICON, RespondServerIcon.class);
    public static final Channel<RespondServerList> RESPOND_SERVER_LIST = new Channel<>(Topic.RESPOND_SERVER_LIST, RespondServerList.class);

    /**
     * Listener topic to subscribe to for when a player
     * requests to connect to a specific server.
    */
    @Jacksonized @Builder @Getter
    public static class RequestConnect extends BodyBase implements Body {
        @JsonProperty("serverId") private final String serverId;
    }

    /**
     * Listener topic to subscribe to for when a server list
     * is requested.
    */
    @Jacksonized @Builder @Getter
    public static class RequestServerList extends BodyBase implements Body {}

    /**
     * Listener topic to subscribe to for when a backend provides
     * an icon.
     */
    @Jacksonized @Builder @Getter
    public static class RespondServerIcon extends BodyBase implements Body {
        @JsonProperty("serverId") private final String serverId;
        @JsonProperty("icon")     private final String icon;
    }

    /**
     * Listener topic to subscribe to for when a server list
     * is returned by the requestee.
    */
    @Jacksonized @Builder @Getter
    public static class RespondServerList extends BodyBase implements Body {
        @JsonProperty("servers") private final List<ServerEntry> servers;
    }
}
