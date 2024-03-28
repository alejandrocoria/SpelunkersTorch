package games.alejandrocoria.spelunkerstorch.client;

import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.client.renderer.TorchBlockEntityWithoutLevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ITEM;

public class SpelunkersTorchClientForge {
    private static final Map<Item, BlockEntityWithoutLevelRenderer> ITEM_RENDERERS = new HashMap<>();

    public static void clientSetup(FMLClientSetupEvent setupEvent) {
        Constants.LOG.info("Spelunker's Torch client init");

        SpelunkersTorchClient.init();
        ITEM_RENDERERS.put(TORCH_ITEM.get(), new TorchBlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(SpelunkersTorchClientForge::onRegisterReloadListener);

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
