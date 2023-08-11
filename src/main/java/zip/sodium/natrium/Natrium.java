package zip.sodium.natrium;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.vehicle.MinecartEntity;

public class Natrium implements ModInitializer {
    public static final String MOD_ID = "natrium";

    @Override
    public void onInitialize() {
        NatriumBlocks.registerBlocks();
    }
}
