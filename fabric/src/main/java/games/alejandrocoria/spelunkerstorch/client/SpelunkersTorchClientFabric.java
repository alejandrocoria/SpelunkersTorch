package games.alejandrocoria.spelunkerstorch.client;

import games.alejandrocoria.spelunkerstorch.Constants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_BLOCK;
import static games.alejandrocoria.spelunkerstorch.Registry.WALL_TORCH_BLOCK;

public class SpelunkersTorchClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Constants.LOG.info("Spelunker's Torch client init");

        SpelunkersTorchClient.registerBlockEntityRenderer();

        BlockRenderLayerMap.putBlock(TORCH_BLOCK.get(), ChunkSectionLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(WALL_TORCH_BLOCK.get(), ChunkSectionLayer.CUTOUT);

        Constants.LOG.info("Spelunker's Torch client init done");
    }
}
