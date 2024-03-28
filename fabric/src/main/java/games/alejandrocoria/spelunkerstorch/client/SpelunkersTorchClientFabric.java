package games.alejandrocoria.spelunkerstorch.client;

import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.client.renderer.TorchBlockEntityWithoutLevelRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_BLOCK;
import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ITEM;
import static games.alejandrocoria.spelunkerstorch.Registry.WALL_TORCH_BLOCK;

public class SpelunkersTorchClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Constants.LOG.info("Spelunker's Torch client init");

        SpelunkersTorchClient.init();

        BlockRenderLayerMap.INSTANCE.putBlock(TORCH_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(WALL_TORCH_BLOCK.get(), RenderType.cutout());

        BuiltinItemRendererRegistry.INSTANCE.register(TORCH_ITEM.get(), new TorchBlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels())::renderByItem);

        Constants.LOG.info("Spelunker's Torch client init done");
    }
}
