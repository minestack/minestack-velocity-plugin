package io.minestack.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.javalin.Javalin;
import io.minestack.velocity.api.routes.HealthApi;
import io.minestack.velocity.api.routes.ServerGroupApi;
import io.minestack.velocity.listeners.PlayerListener;
import io.minestack.velocity.proxy.server.ServerGroupMap;
import io.minestack.velocity.utils.GsonJsonMapper;
import org.slf4j.Logger;

@Plugin(id = "minestack", name = "Minestack Velocity Plugin", version = "1.0.0-SNAPSHOT",
        url = "https://github.com/minestack", description = "Minestack Plugin for Velocity", authors = {"rmb938"})
public class MinestackPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final ServerGroupMap serverGroups;

    @Inject
    public MinestackPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.serverGroups = new ServerGroupMap(this.server, this.logger);
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    public ServerGroupMap getServerGroups() {
        return serverGroups;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Starting Minestack Plugin");

        // event listeners
        server.getEventManager().register(this, new PlayerListener(this));

        // HTTP API
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(MinestackPlugin.class.getClassLoader());

        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.showJavalinBanner = false;

            config.jsonMapper(new GsonJsonMapper());
        });

        HealthApi healthApi = new HealthApi(this);
        ServerGroupApi serverGroupApi = new ServerGroupApi(this);

        app.routes(() -> {
            app.get("healthz", healthApi::healthz);
            app.get("readyz", healthApi::readyz);

            app.post("server-groups", serverGroupApi::post);
        });

        logger.info("Starting Minestack Webserver");
        app.start("127.0.0.1", 4567);

        Thread.currentThread().setContextClassLoader(classLoader);
    }
}
