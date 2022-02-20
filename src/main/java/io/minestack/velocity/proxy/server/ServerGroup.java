package io.minestack.velocity.proxy.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGroup {

    private final String name;
    private final ProxyServer server;
    private final Logger logger;
    private final Map<String, RegisteredServerGroupServer> servers = new ConcurrentHashMap<>();
    private final ConnectionStrategy randomConnectionStrategy;

    public ServerGroup(String name, ProxyServer server, Logger logger) {
        this.name = name;
        this.server = server;
        this.logger = logger;

        this.randomConnectionStrategy = new RandomConnectionStrategy();
    }

    public String getName() {
        return name;
    }

    public ConnectionRequestBuilder createConnectionRequest(Player player) {
        return this.createConnectionRequest(player, this.randomConnectionStrategy);
    }

    public ConnectionRequestBuilder createConnectionRequest(Player player, ConnectionStrategy connectionStrategy) {
        return player.createConnectionRequest(connectionStrategy.getServerToConnect(this.getAllServers()).getRegisteredServer());
    }

    public Optional<RegisteredServerGroupServer> getServer(String name) {
        Preconditions.checkNotNull(name, "server");
        String lowerName = name.toLowerCase(Locale.US);
        return Optional.ofNullable(this.servers.get(lowerName));
    }

    public Collection<RegisteredServerGroupServer> getAllServers() {
        return ImmutableList.copyOf(this.servers.values());
    }

    public RegisteredServerGroupServer register(ServerInfo serverInfo) {
        Preconditions.checkNotNull(serverInfo, "serverInfo");
        String lowerName = serverInfo.getName().toLowerCase(Locale.US);
        RegisteredServerGroupServer rs = new RegisteredServerGroupServer(this.server.createRawRegisteredServer(serverInfo));

        RegisteredServerGroupServer existing = this.servers.putIfAbsent(lowerName, rs);
        if (existing != null && !existing.getServerInfo().equals(serverInfo)) {
            throw new IllegalArgumentException(
                    "Server with name " + serverInfo.getName() + " already registered");
        } else {
            return Objects.requireNonNullElse(existing, rs);
        }
    }

    public void unregister(ServerInfo serverInfo) {
        Preconditions.checkNotNull(serverInfo, "serverInfo");
        String lowerName = serverInfo.getName().toLowerCase(Locale.US);
        RegisteredServerGroupServer rs = servers.get(lowerName);
        if (rs == null) {
            throw new IllegalArgumentException(
                    "Server with name " + serverInfo.getName() + " is not registered!");
        }
        Preconditions.checkArgument(rs.getServerInfo().equals(serverInfo),
                "Trying to remove server %s with differing information", serverInfo.getName());
        Preconditions.checkState(servers.remove(lowerName, rs),
                "Server with name %s replaced whilst unregistering", serverInfo.getName());
    }
}
