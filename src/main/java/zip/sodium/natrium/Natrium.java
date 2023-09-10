package zip.sodium.natrium;

import net.fabricmc.api.ModInitializer;

public class Natrium implements ModInitializer {
    public static final String MOD_ID = "natrium";

    @Override
    public void onInitialize() {
        NatriumBlocks.registerBlocks();
    }
}
