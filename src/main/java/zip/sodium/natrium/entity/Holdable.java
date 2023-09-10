package zip.sodium.natrium.entity;

import dev.lazurite.rayon.api.EntityPhysicsElement;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface Holdable extends EntityPhysicsElement {
    @Nullable
    PlayerEntity getHolder();
    void toggleHolder(PlayerEntity holder);
    void setHolder(PlayerEntity holder);

    boolean isBeingHeld();
}
