package org.wilkinsonk.sbc.papermc;

import java.util.Properties;

public abstract class SBCServer {
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
