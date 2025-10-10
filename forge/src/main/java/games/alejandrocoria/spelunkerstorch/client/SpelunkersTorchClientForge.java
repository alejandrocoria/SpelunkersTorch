package games.alejandrocoria.spelunkerstorch.client;

import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.client.renderer.TorchSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.CreateSpecialBlockRendererEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_BLOCK;

public class SpelunkersTorchClientForge {
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        SpelunkersTorchClient.registerBlockEntityRenderer();
    }

    public static void createSpecialBlockRendererEvent(CreateSpecialBlockRendererEvent event) {
        SpecialModelRenderers.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "torch"), TorchSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(TORCH_BLOCK.get(), new TorchSpecialRenderer.Unbaked());
    }
}
