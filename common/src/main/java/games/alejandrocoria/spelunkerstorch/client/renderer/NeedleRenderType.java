package games.alejandrocoria.spelunkerstorch.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class NeedleRenderType extends RenderType {
    private static final RenderType NEEDLE_RENDER_TYPE = create(
            "spelunkerstorch_needle",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
            VertexFormat.Mode.TRIANGLE_STRIP,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LEASH_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setLayeringState(NO_LAYERING)
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setCullState(NO_CULL)
                    .createCompositeState(true)
    );

    public NeedleRenderType(String name, VertexFormat vertexFormat, VertexFormat.Mode drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable pre, Runnable post) {
        super(name, vertexFormat, drawMode, bufferSize, useDelegate, needsSorting, pre, post);
    }

    public static RenderType getRenderType() {
        return NEEDLE_RENDER_TYPE;
    }
}
