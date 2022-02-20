package io.minestack.velocity.proxy.server;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGroupMap {

    private final ProxyServer server;
    private final Logger logger;
    private final Map<String, ServerGroup> serverGroups = new ConcurrentHashMap<>();


    public ServerGroupMap(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    public Optional<ServerGroup> getServerGroup(String name) {
        Preconditions.checkNotNull(name, "serverGroup");
        String lowerName = name.toLowerCase(Locale.US);
        return Optional.ofNullable(this.serverGroups.get(lowerName));
    }

    public ServerGroup createServerGroup(String name) {
        Preconditions.checkNotNull(name, "serverGroup");
        String lowerName = name.toLowerCase(Locale.US);

        ServerGroup serverGroup = new ServerGroup(lowerName, this.server, this.logger);
        ServerGroup existing = this.serverGroups.putIfAbsent(lowerName, serverGroup);
        if (existing == null) {
            return serverGroup;
        }
        return existing;
    }
}
