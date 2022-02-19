package io.minestack.velocity.api.routes;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.minestack.velocity.event.api.LivenessProbeEvent;
import io.minestack.velocity.event.api.ReadinessProbeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HealthApi {

    private final ProxyServer server;

    public HealthApi(ProxyServer server) {
        this.server = server;
    }

    public void healthz(Context ctx) throws ExecutionException, InterruptedException {
        HttpCode responseCode = HttpCode.OK;

        LivenessProbeEvent event = new LivenessProbeEvent(responseCode);
        CompletableFuture<LivenessProbeEvent> futureProbe = server.getEventManager().fire(event);
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

        for (String joinServer : groupNames) {
            Collection<RegisteredServer> servers = server.matchServer("server-group-" + joinServer);
            if (servers.size() == 0) {
                responseCode = HttpCode.SERVICE_UNAVAILABLE;
                break;
            }
        }

        ReadinessProbeEvent event = new ReadinessProbeEvent(responseCode);
        CompletableFuture<ReadinessProbeEvent> futureProbe = server.getEventManager().fire(event);
        event = futureProbe.get();

        ctx.status(event.getResponseCode());
    }

}
