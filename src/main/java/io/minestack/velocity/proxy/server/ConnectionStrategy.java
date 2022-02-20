package io.minestack.velocity.proxy.server;

import java.util.Collection;

public interface ConnectionStrategy {

    RegisteredServerGroupServer getServerToConnect(Collection<RegisteredServerGroupServer> servers);

}
