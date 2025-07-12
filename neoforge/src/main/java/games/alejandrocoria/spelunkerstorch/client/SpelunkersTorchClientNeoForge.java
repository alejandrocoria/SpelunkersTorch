package games.alejandrocoria.spelunkerstorch.client;

import games.alejandrocoria.spelunkerstorch.Constants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class SpelunkersTorchClientNeoForge {
    public static void clientSetup(FMLClientSetupEvent event, IEventBus eventBus) {
        Constants.LOG.info("Spelunker's Torch client init");

        SpelunkersTorchClient.registerBlockEntityRenderer();

        Constants.LOG.info("Spelunker's Torch client init done");
    }
}
