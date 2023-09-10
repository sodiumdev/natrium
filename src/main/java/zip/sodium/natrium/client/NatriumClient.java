package zip.sodium.natrium.client;

import net.fabricmc.api.ClientModInitializer;
import zip.sodium.natrium.NatriumEntities;

public class NatriumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NatriumEntities.registerEntityRenderers();
    }
}
