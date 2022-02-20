package io.minestack.velocity.api.routes;

import com.velocitypowered.api.proxy.ProxyServer;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.minestack.velocity.MinestackPlugin;
import io.minestack.velocity.event.api.LivenessProbeEvent;
import io.minestack.velocity.event.api.ReadinessProbeEvent;
import io.minestack.velocity.proxy.server.ServerGroup;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HealthApi {

    private final MinestackPlugin plugin;
    private final ProxyServer server;
    private final Logger logger;

    public HealthApi(MinestackPlugin plugin) {
        this.plugin = plugin;
        this.server = this.plugin.getServer();
        this.logger = this.plugin.getLogger();
    }

    public void healthz(Context ctx) throws ExecutionException, InterruptedException {
        HttpCode responseCode = HttpCode.OK;

        LivenessProbeEvent event = new LivenessProbeEvent(responseCode);
        CompletableFuture<LivenessProbeEvent> futureProbe = this.server.getEventManager().fire(event);
        event = futureProbe.get();

        ctx.status(event.getResponseCode());
    }

    public void readyz(Context ctx) throws ExecutionException, InterruptedException {
        HttpCode responseCode = HttpCode.OK;

        List<String> groupNames = new ArrayList<>();
        List<String> tryGroupNames = this.server.getConfiguration().getAttemptConnectionOrder();
        String envPrefix = "MINESTACK_SERVER_GROUP_";

        // add groups from env
        for (String envName : System.getenv().keySet()) {
            if (!envName.startsWith(envPrefix)) {
                continue;
            }
            groupNames.add(envName.substring(envPrefix.length()));
        }

        // groups from env
        for (String groupName : groupNames) {
            if (this.plugin.getServerGroups().getServerGroup(groupName).isEmpty()) {
                this.logger.warn("HealthApi: Cannot find any registered group for {} in the environment", groupName);
                responseCode = HttpCode.SERVICE_UNAVAILABLE;
                break;
            }
        }

        // groups from try
        // we require at least 1 server present from each try
        for (String groupName : tryGroupNames) {
            Optional<ServerGroup> serverGroup = this.plugin.getServerGroups().getServerGroup(groupName);
            if (serverGroup.isEmpty()) {
                this.logger.warn("HealthApi: Cannot find any registered group for {} in the try list", groupName);
                responseCode = HttpCode.SERVICE_UNAVAILABLE;
                break;
            }

            if (serverGroup.get().getAllServers().size() == 0) {
                this.logger.warn("HealthApi: Cannot find any registered servers for group {} in the try list", groupName);
                responseCode = HttpCode.SERVICE_UNAVAILABLE;
                break;
            }
        }

        ReadinessProbeEvent event = new ReadinessProbeEvent(responseCode);
        CompletableFuture<ReadinessProbeEvent> futureProbe = this.server.getEventManager().fire(event);
        event = futureProbe.get();

        ctx.status(event.getResponseCode());
    }

}
