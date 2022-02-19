package io.minestack.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;

public class PlayerListener {

    private final ProxyServer server;
    private final Logger logger;

    private final Random random;

    public PlayerListener(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        this.random = new Random();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChooseInitialServerEvent(PlayerChooseInitialServerEvent event) {
        String serverName = null;

        if (event.getInitialServer().isPresent()) {
            serverName = event.getInitialServer().get().getServerInfo().getName();
        }

        Collection<RegisteredServer> servers = server.matchServer("server-group-" + serverName);
        Optional<RegisteredServer> randomServer = servers.stream().skip(this.random.nextInt(servers.size())).findFirst();
        event.setInitialServer(randomServer.orElseThrow());
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerPreConnectEvent(ServerPreConnectEvent event) {
        ServerInfo serverInfo = event.getOriginalServer().getServerInfo();

        if (!serverInfo.getName().startsWith("server-group-")) {
            String groupName = serverInfo.getName();

            Collection<RegisteredServer> servers = server.matchServer("server-group-" + groupName);
            Optional<RegisteredServer> randomServer = servers.stream().skip(this.random.nextInt(servers.size())).findFirst();
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(randomServer.orElseThrow()));
        }
    }

}
