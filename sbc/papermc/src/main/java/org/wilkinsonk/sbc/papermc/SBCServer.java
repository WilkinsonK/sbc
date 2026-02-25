package org.wilkinsonk.sbc.papermc;

import java.util.HashMap;
import java.util.Properties;
import java.util.Map;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import org.wilkinsonk.sbc.payload.Body;
import org.wilkinsonk.sbc.payload.Channel;
import org.wilkinsonk.sbc.topic.Topic;

public abstract class SBCServer {
    protected final Map<String, MinecraftChannelIdentifier> channels = new HashMap<>();

    protected <T extends Body> boolean eventIsChannel(PluginMessageEvent event, Channel<T> channel) {
        return eventIsChannel(event.getIdentifier(), channel);
    }

    protected <T extends Body> boolean eventIsChannel(ChannelIdentifier event, Channel<T> channel) {
        return eventIsChannel(event, channel.getTopic().GetName());
    }

    protected boolean eventIsChannel(ChannelIdentifier event, String channel) {
        return eventIsChannel(event, channels.get(channel));
    }

    protected boolean eventIsChannel(ChannelIdentifier event, MinecraftChannelIdentifier channel) {
        return event.equals(channel);
    }

    protected <T extends Body> MinecraftChannelIdentifier getChannel(Channel<T> channel) {
        return getChannel(channel.getTopic().GetName());
    }

    protected MinecraftChannelIdentifier getChannel(String name) {
        return channels.get(name);
    }

    protected <T extends Body> MinecraftChannelIdentifier registerChannel(Channel<T> channel) {
        return registerChannel(channel.getTopic());
    }

    protected MinecraftChannelIdentifier registerChannel(Topic topic) {
        return registerChannel(topic.GetName(), topic.GetFullyQualifiedName());
    }

    protected MinecraftChannelIdentifier registerChannel(String name, String fqn) {
        return registerChannel(name, MinecraftChannelIdentifier.from(fqn));
    }

    protected MinecraftChannelIdentifier registerChannel(String name, MinecraftChannelIdentifier ident) {
        channels.put(name, ident);
        return ident;
    }

    public enum ConfigProperty {
        ICON("default-icon", "icon", "grass_block");

        public final String RootLabel;
        public final String ServerLabel;
        public final String DefaultValue;

        public final ProxyProperty Proxy;
        public final ServerProperty Server;

        ConfigProperty(String rootLabel, String defaultValue) {
            RootLabel    = rootLabel;
            ServerLabel  = rootLabel;
            DefaultValue = defaultValue;

            Proxy  = new ProxyProperty(this);
            Server = new ServerProperty(this);
        }

        ConfigProperty(String rootLabel, String serverLabel, String defaultValue) {
            RootLabel    = rootLabel;
            ServerLabel  = serverLabel;
            DefaultValue = defaultValue;

            Proxy  = new ProxyProperty(this);
            Server = new ServerProperty(this);
        }

        class ProxyProperty {
            private final ConfigProperty Parent;

            ProxyProperty(ConfigProperty parent) {
                Parent = parent;
            }

            void Apply(Properties props) {
                props.setProperty(Parent.RootLabel, Parent.DefaultValue);
            }

            String Get(Properties props) {
                return props.getProperty(Parent.RootLabel, Parent.DefaultValue);
            }
        }

        class ServerProperty {
            private final ConfigProperty Parent;

            ServerProperty(ConfigProperty parent) {
                Parent = parent;
            }

            void Apply(Properties props) {
                props.setProperty(Parent.ServerLabel, Parent.DefaultValue);
            }

            String Get(Properties props) {
                return props.getProperty(Parent.ServerLabel, Parent.DefaultValue);
            }
        }
    }
}
