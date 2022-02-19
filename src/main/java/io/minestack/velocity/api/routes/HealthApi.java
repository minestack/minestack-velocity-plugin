package io.minestack.velocity.api.routes;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.minestack.velocity.event.api.LivenessProbeEvent;
import io.minestack.velocity.event.api.ReadinessProbeEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HealthApi {

    private final ProxyServer server;
    private final Logger logger;

    public HealthApi(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
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
        String envPrefix = "MINESTACK_SERVER_GROUP_";

        for (String envName : System.getenv().keySet()) {
            if (!envName.startsWith(envPrefix)) {
                continue;
            }
            groupNames.add(envName.substring(envPrefix.length()));
        }

        for (String groupName : groupNames) {
            String matchServerName = "server-group-" + groupName;
            Collection<RegisteredServer> servers = this.server.matchServer(matchServerName);
            if (servers.size() == 0) {
                this.logger.warn("Cannot find any registered servers for " + matchServerName);
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
