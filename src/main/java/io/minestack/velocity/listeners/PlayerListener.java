package io.minestack.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import io.minestack.velocity.MinestackPlugin;
import io.minestack.velocity.event.player.PlayerChooseInitialServerGroupEvent;
import io.minestack.velocity.event.player.ServerGroupPreConnectEvent;
import io.minestack.velocity.proxy.server.ServerGroup;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PlayerListener {

    private final MinestackPlugin plugin;
    private final ProxyServer server;
    private final Logger logger;

    public PlayerListener(MinestackPlugin plugin) {
        this.plugin = plugin;
        this.server = this.plugin.getServer();
        this.logger = this.plugin.getLogger();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerChooseInitialServerEvent(PlayerChooseInitialServerEvent event) throws ExecutionException, InterruptedException {
        String serverName;

        if (event.getInitialServer().isPresent()) {
            serverName = event.getInitialServer().get().getServerInfo().getName();
        } else {
            this.logger.error("PlayerChooseInitialServerEvent: Received event with null initial server so not doing anything");
            return;
        }

        if (this.server.getServer(serverName).isPresent()) {
            // get group
            Optional<ServerGroup> serverGroup = this.plugin.getServerGroups().getServerGroup(serverName);

            if (serverGroup.isEmpty()) {
                this.logger.error("PlayerChooseInitialServerEvent: Player tried to connect to group {} which doesn't exist", serverName);
                event.setInitialServer(null);
                return;
            }

            // fire PlayerChooseInitialServerGroupEvent event
            PlayerChooseInitialServerGroupEvent playerChooseInitialServerGroupEvent = new PlayerChooseInitialServerGroupEvent(event.getPlayer(), serverGroup.get());
            CompletableFuture<PlayerChooseInitialServerGroupEvent> futureProbe = this.server.getEventManager().fire(playerChooseInitialServerGroupEvent);
            playerChooseInitialServerGroupEvent = futureProbe.get();

            Optional<ServerGroup> initialGroup = playerChooseInitialServerGroupEvent.getInitialGroup();

            if (initialGroup.isEmpty()) {
                this.logger.warn("PlayerChooseInitialServerEvent: Initial Group {} became null after event", serverGroup.get().getName());
                event.setInitialServer(null);
                return;
            }

            // connect
            ConnectionRequestBuilder connectionRequestBuilder = initialGroup.get().createConnectionRequest(playerChooseInitialServerGroupEvent.getPlayer());
            event.setInitialServer(connectionRequestBuilder.getServer());
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onServerPreConnectEvent(ServerPreConnectEvent event) throws ExecutionException, InterruptedException {
        ServerInfo serverInfo = event.getOriginalServer().getServerInfo();

        if (this.server.getServer(serverInfo.getName()).isPresent()) {
            Optional<ServerGroup> serverGroup = this.plugin.getServerGroups().getServerGroup(serverInfo.getName());

            if (serverGroup.isEmpty()) {
                this.logger.error("ServerPreConnectEvent: Player tried to connect to group {} which doesn't exist", serverInfo.getName());
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            ServerGroupPreConnectEvent serverGroupPreConnectEvent = new ServerGroupPreConnectEvent(event.getPlayer(), serverGroup.get());
            CompletableFuture<ServerGroupPreConnectEvent> futureProbe = this.server.getEventManager().fire(serverGroupPreConnectEvent);
            serverGroupPreConnectEvent = futureProbe.get();
            
            if (!serverGroupPreConnectEvent.getResult().isAllowed()) {
                this.logger.warn("ServerPreConnectEvent: Result became denied after event");
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            event.setResult(ServerPreConnectEvent.ServerResult.allowed(serverGroupPreConnectEvent.getResult().getServer().get().createConnectionRequest(serverGroupPreConnectEvent.getPlayer()).getServer()));
        }
    }

}
