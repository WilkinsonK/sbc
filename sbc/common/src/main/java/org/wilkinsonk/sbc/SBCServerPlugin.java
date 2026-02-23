package org.wilkinsonk.sbc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SBCServerPlugin {
    void RegisterChannels();
    CompletableFuture<List<SoulardiganBackyard.ServerEntry>> GetServerList(String currentServerName);
}
