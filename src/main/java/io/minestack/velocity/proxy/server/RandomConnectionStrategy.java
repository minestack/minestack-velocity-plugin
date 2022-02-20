package io.minestack.velocity.proxy.server;

import java.util.Collection;
import java.util.Random;

public class RandomConnectionStrategy implements ConnectionStrategy {

    private final Random random;

    public RandomConnectionStrategy() {
        this.random = new Random();
    }

    @Override
    public RegisteredServerGroupServer getServerToConnect(Collection<RegisteredServerGroupServer> servers) {
        return servers.stream().skip(this.random.nextInt(servers.size())).findFirst().orElseThrow();
    }
}
