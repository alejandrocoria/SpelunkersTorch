package games.alejandrocoria.spelunkerstorch.common.mixin.client;

import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.client.renderer.TorchSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_BLOCK;

@Mixin(SpecialModelRenderers.class)
public class SpecialModelRenderersMixin {
    @ModifyVariable(method="createBlockRenderers", at=@At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;"))
    private static Map<Block, SpecialModelRenderer.Unbaked> spelunkerstorch$createBlockRenderers(Map<Block, SpecialModelRenderer.Unbaked> map) {
        SpecialModelRenderers.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "torch"), TorchSpecialRenderer.Unbaked.MAP_CODEC);
        map.put(TORCH_BLOCK.get(), new TorchSpecialRenderer.Unbaked());
        return map;
    }
}
