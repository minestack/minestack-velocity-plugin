package io.minestack.velocity.api.routes;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.minestack.velocity.MinestackPlugin;
import io.minestack.velocity.proxy.server.RegisteredServerGroupServer;
import io.minestack.velocity.proxy.server.ServerGroup;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class ServerGroupApi {

    private final MinestackPlugin plugin;
    private final ProxyServer server;
    private final Logger logger;

    public ServerGroupApi(MinestackPlugin plugin) {
        this.plugin = plugin;
        this.server = this.plugin.getServer();
        this.logger = this.plugin.getLogger();
    }

    public void post(Context ctx) {
        io.minestack.velocity.api.models.ServerGroup serverGroupModel = ctx.bodyStreamAsClass(io.minestack.velocity.api.models.ServerGroup.class);

        ServerGroup serverGroup = this.plugin.getServerGroups().createServerGroup(serverGroupModel.getName());

        // register group in master list
        this.server.registerServer(new ServerInfo(serverGroup.getName(), new InetSocketAddress("127.0.0.1", 25565)));

        // existing servers
        Collection<RegisteredServerGroupServer> existingServersInGroup = serverGroup.getAllServers();

        // new servers
        Map<String, ServerInfo> newServers = new HashMap<>();

        for (Map.Entry<String, String> entry : serverGroupModel.getServers().entrySet()) {
            ServerInfo serverInfo = new ServerInfo(entry.getKey(), new InetSocketAddress(entry.getValue(), 25565));
            this.logger.info("ServerGroupApi: Registering server {}/{}", serverGroup.getName(), serverInfo);
            newServers.put(serverInfo.getName(), serverInfo);
            serverGroup.register(serverInfo);
        }

        // remove no longer existing servers
        for (RegisteredServerGroupServer existingServer : existingServersInGroup) {
            // server DNE so remove it
            if (!newServers.containsKey(existingServer.getServerInfo().getName())) {
                // unregister server
                this.logger.info("ServerGroupApi: Unregistering server {}/{}", serverGroup.getName(), existingServer.getServerInfo());
                serverGroup.unregister(existingServer.getServerInfo());

                // move all players on server back to the first group in "try"
                for (Player player : existingServer.getPlayersConnected()) {
                    this.plugin.getServerGroups().getServerGroup(this.server.getConfiguration().getAttemptConnectionOrder().get(0)).orElseThrow().createConnectionRequest(player).connectWithIndication();
                }
            }
        }

        ctx.status(HttpCode.OK);
    }

}
