package games.alejandrocoria.spelunkerstorch.client;

import games.alejandrocoria.spelunkerstorch.Constants;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class SpelunkersTorchClientForge {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent setupEvent) {
        Constants.LOG.info("Spelunker's Torch client init");

        Constants.LOG.info("Spelunker's Torch client init done");
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        SpelunkersTorchClient.registerBlockEntityRenderer();
    }
}
