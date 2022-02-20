package io.minestack.velocity.event.player;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;
import io.minestack.velocity.proxy.server.ServerGroup;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

public class ServerGroupPreConnectEvent implements
        ResultedEvent<ServerGroupPreConnectEvent.ServerGroupResult> {

    private final Player player;
    private final ServerGroup originalGroup;
    private ServerGroupPreConnectEvent.ServerGroupResult result;

    public ServerGroupPreConnectEvent(Player player, ServerGroup originalGroup) {
        this.player = Preconditions.checkNotNull(player, "player");
        this.originalGroup = Preconditions.checkNotNull(originalGroup, "originalGroup");
        this.result = ServerGroupPreConnectEvent.ServerGroupResult.allowed(originalGroup);
    }

    /**
     * Returns the player connecting to the server.
     *
     * @return the player connecting to the server
     */
    public Player getPlayer() {
        return player;
    }

    public ServerGroupPreConnectEvent.ServerGroupResult getResult() {
        return result;
    }

    public void setResult(ServerGroupPreConnectEvent.ServerGroupResult result) {
        this.result = Preconditions.checkNotNull(result, "result");
    }

    public ServerGroup getOriginalGroup() {
        return originalGroup;
    }

    public static class ServerGroupResult implements ResultedEvent.Result {

        private static final ServerGroupPreConnectEvent.ServerGroupResult DENIED = new ServerGroupPreConnectEvent.ServerGroupResult(null);

        private final @Nullable ServerGroup group;

        private ServerGroupResult(@Nullable ServerGroup group) {
            this.group = group;
        }

        @Override
        public boolean isAllowed() {
            return group != null;
        }

        public Optional<ServerGroup> getServer() {
            return Optional.ofNullable(group);
        }

        @Override
        public String toString() {
            if (group != null) {
                return "allowed: connect to " + group.getName();
            }
            return "denied";
        }

        public static ServerGroupPreConnectEvent.ServerGroupResult denied() {
            return DENIED;
        }

        public static ServerGroupPreConnectEvent.ServerGroupResult allowed(ServerGroup group) {
            Preconditions.checkNotNull(group, "group");
            return new ServerGroupPreConnectEvent.ServerGroupResult(group);
        }
    }
}
