package io.minestack.velocity.event.player;

import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.Player;
import io.minestack.velocity.proxy.server.ServerGroup;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

public class PlayerChooseInitialServerGroupEvent {

    private final Player player;
    private @Nullable ServerGroup initialGroup;

    public PlayerChooseInitialServerGroupEvent(Player player, @Nullable ServerGroup initialGroup) {
        this.player = Preconditions.checkNotNull(player, "player");
        this.initialGroup = initialGroup;
    }

    public Player getPlayer() {
        return player;
    }

    public Optional<ServerGroup> getInitialGroup() {
        return Optional.ofNullable(initialGroup);
    }

    public void setInitialGroup(@Nullable ServerGroup group) {
        this.initialGroup = group;
    }

}
