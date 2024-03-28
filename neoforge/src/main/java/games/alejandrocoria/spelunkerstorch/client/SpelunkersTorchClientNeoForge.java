package games.alejandrocoria.spelunkerstorch.client;

import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.client.renderer.TorchBlockEntityWithoutLevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ITEM;

public class SpelunkersTorchClientNeoForge {
    private static final Map<Item, BlockEntityWithoutLevelRenderer> ITEM_RENDERERS = new HashMap<>();

    public static void clientSetup(FMLClientSetupEvent event, IEventBus eventBus) {
        Constants.LOG.info("Spelunker's Torch client init");

        SpelunkersTorchClient.init();
        ITEM_RENDERERS.put(TORCH_ITEM.get(), new TorchBlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));
        eventBus.addListener(SpelunkersTorchClientNeoForge::onRegisterReloadListener);

        Constants.LOG.info("Spelunker's Torch client init done");
    }

    public static BlockEntityWithoutLevelRenderer getItemRenderer(ItemLike item) {
        return ITEM_RENDERERS.get(item.asItem());
    }

    public static void onRegisterReloadListener(RegisterClientReloadListenersEvent event) {
        for (BlockEntityWithoutLevelRenderer renderer : ITEM_RENDERERS.values()) {
            event.registerReloadListener(renderer);
        }
    }
}
