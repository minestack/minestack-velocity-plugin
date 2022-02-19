package io.minestack.velocity.api.routes;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.minestack.velocity.api.models.ServerGroup;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class ServerGroupApi {

    private final ProxyServer server;
    private final Logger logger;

    public ServerGroupApi(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    public void post(Context ctx) {
        ServerGroup serverGroup = ctx.bodyStreamAsClass(ServerGroup.class);

        // existing servers
        Collection<RegisteredServer> existingServersInGroup = this.server.matchServer("server-group-" + serverGroup.getName());

        // new servers
        Map<String, ServerInfo> newServers = new HashMap<>();

        for (Map.Entry<String, String> entry : serverGroup.getServers().entrySet()) {
            ServerInfo serverInfo = new ServerInfo("server-group-" + serverGroup.getName() + "-" + entry.getKey(), new InetSocketAddress(entry.getValue(), 25565));
            newServers.put(serverInfo.getName(), serverInfo);
            this.server.registerServer(serverInfo);
        }

        // remove no longer existing servers
        for (RegisteredServer existingServer : existingServersInGroup) {
            // server DNE so remove it
            if (!newServers.containsKey(existingServer.getServerInfo().getName())) {
                // unregister server
                this.server.unregisterServer(existingServer.getServerInfo());

                // kick all players on server back to the first server in "try"
                for (Player player : existingServer.getPlayersConnected()) {
                    player.createConnectionRequest(this.server.getServer(this.server.getConfiguration().getAttemptConnectionOrder().get(0)).orElseThrow()).connectWithIndication();
                }
            }
        }

        ctx.status(HttpCode.OK);
    }

}
