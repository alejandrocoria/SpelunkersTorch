package games.alejandrocoria.spelunkerstorch.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import games.alejandrocoria.spelunkerstorch.Constants;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class NeedleRenderType extends RenderType {
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

    public NeedleRenderType(String name, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable pre, Runnable post) {
        super(name, bufferSize, useDelegate, needsSorting, pre, post);
    }

    private static final RenderType NEEDLE_RENDER_TYPE = create(
            Constants.MOD_ID + "_needle",
            256,
            false,
            false,
            NEEDLE_RENDER_TYPE_PIPELINE,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/needle.png"), false))
                    .setLayeringState(NO_LAYERING)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true)
    );

    public static RenderType getRenderType() {
        return NEEDLE_RENDER_TYPE;
    }

    @Override
    public void draw(MeshData meshData) {

    }

    @Override
    public VertexFormat format() {
        return null;
    }

    @Override
    public VertexFormat.Mode mode() {
        return null;
    }

    @Override
    public RenderPipeline pipeline() {
        return NEEDLE_RENDER_TYPE_PIPELINE;
    }
}
