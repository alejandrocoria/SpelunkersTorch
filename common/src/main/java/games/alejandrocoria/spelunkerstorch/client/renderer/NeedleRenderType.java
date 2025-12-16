package games.alejandrocoria.spelunkerstorch.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.common.mixin.client.RenderTypeInvoker;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class NeedleRenderType {
    private static final RenderPipeline NEEDLE_RENDER_TYPE_PIPELINE = RenderPipeline.builder()
            .withLocation(Constants.MOD_ID + "/pipeline/needle")
            .withVertexShader("core/entity")
            .withFragmentShader("core/entity")
            .withUniform("Lighting", UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("Fog", UniformType.UNIFORM_BUFFER)
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withSampler("Sampler2")
            .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES)
            .build();

    private static final RenderType NEEDLE_RENDER_TYPE = RenderTypeInvoker.invokeCreate(
            Constants.MOD_ID + "_needle",
            RenderSetup.builder(NEEDLE_RENDER_TYPE_PIPELINE)
                    .withTexture("Sampler0", Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/needle.png"))
                    .useLightmap()
                    .useOverlay()
                    .createRenderSetup()
    );

    public static RenderType getRenderType() {
        return NEEDLE_RENDER_TYPE;
    }
}
