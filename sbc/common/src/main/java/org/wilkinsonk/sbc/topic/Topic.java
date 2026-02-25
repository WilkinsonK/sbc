package org.wilkinsonk.sbc.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Topic {
    REQUEST_CONNECT("request/connect"),
    REQUEST_SERVER_LIST("request/server_list"),
    RESPOND_SERVER_ICON("respond/server_icon"),
    RESPOND_SERVER_LIST("respond/server_list");

    private final Metadata topic;

    Topic(String topic) {
        this.topic = new Metadata(topic);
    }

    @Override
    public String toString() {
        return GetFullyQualifiedName();
    }

    public final String GetFullyQualifiedName() {
        return topic.GetFullyQualifiedName();
    }

    public final String GetName() {
        return topic.getIdentity();
    }

    public final String GetNamespace() {
        return topic.getNamespace();
    }
}
