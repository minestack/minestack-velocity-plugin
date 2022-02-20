package io.minestack.velocity.proxy.server;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.util.Collection;

public class RegisteredServerGroupServer {

    private final RegisteredServer registeredServer;

    public RegisteredServerGroupServer(RegisteredServer registeredServer) {
        this.registeredServer = registeredServer;
    }

    public RegisteredServer getRegisteredServer() {
        return registeredServer;
    }

    public ServerInfo getServerInfo() {
        return registeredServer.getServerInfo();
    }

    public Collection<Player> getPlayersConnected() {
        return registeredServer.getPlayersConnected();
    }
}
