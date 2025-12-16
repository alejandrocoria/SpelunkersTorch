package games.alejandrocoria.spelunkerstorch.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import games.alejandrocoria.spelunkerstorch.common.block.WallTorch;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import games.alejandrocoria.spelunkerstorch.common.util.Util;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TorchRenderer implements BlockEntityRenderer<TorchEntity, TorchRenderState> {
    private static final float NEEDLE_HALF_WIDTH = 0.12f;
    private static final float NEEDLE_LENGTH = 0.23f;
    private static final Vector3f[] VERTICES = {
            new Vector3f(0, NEEDLE_HALF_WIDTH, 0),
            new Vector3f(NEEDLE_HALF_WIDTH, 0, 0),
            new Vector3f(0, -NEEDLE_HALF_WIDTH, 0),
            new Vector3f(-NEEDLE_HALF_WIDTH, 0, 0),
            new Vector3f(0, 0, NEEDLE_LENGTH),
    };
    private static final Vector3f[] NORMALS = {
            Util.getNormal(VERTICES[0], VERTICES[4], VERTICES[1]),
            Util.getNormal(VERTICES[1], VERTICES[4], VERTICES[2]),
            Util.getNormal(VERTICES[2], VERTICES[4], VERTICES[3]),
            Util.getNormal(VERTICES[3], VERTICES[4], VERTICES[0]),
            Util.getNormal(VERTICES[0], VERTICES[1], VERTICES[2]),
            Util.getNormal(VERTICES[2], VERTICES[3], VERTICES[0]),
    };

    public TorchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public TorchRenderState createRenderState() {
        return new TorchRenderState();
    }

    @Override
    public void extractRenderState(TorchEntity torchEntity, TorchRenderState torchRenderState, float partialTick, Vec3 vec3, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(torchEntity, torchRenderState, crumblingOverlay);
        torchRenderState.rotation = torchEntity.getRotation();
    }

    @Override
    public void submit(TorchRenderState torchRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (torchRenderState.rotation == null) {
            return;
        }

        poseStack.pushPose();
        Optional<Direction> direction = torchRenderState.blockState.getOptionalValue(WallTorch.FACING).map(Direction::getOpposite);
        direction.ifPresent(value -> {
            poseStack.translate(value.getStepX() * WallTorch.H_OFFSET, WallTorch.V_OFFSET, value.getStepZ() * WallTorch.H_OFFSET);
        });

        renderNeedle(poseStack, torchRenderState.rotation, torchRenderState.lightCoords, submitNodeCollector);

        poseStack.popPose();
    }

    public static void renderNeedle(PoseStack poseStack, Quaternionf rotation, int light, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.75, 0.5);
        poseStack.rotateAround(rotation, 0, 0, 0);
        poseStack.translate(0, 0, 0.125);

        submitNodeCollector.submitCustomGeometry(poseStack, NeedleRenderType.getRenderType(), (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, VERTICES[0]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[0]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[4]).setColor(0xFFFFFFFF).setUv(0.75f, 0.25f).setNormal(pose, NORMALS[0]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[1]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[0]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

            vertexConsumer.addVertex(pose, VERTICES[1]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[1]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[4]).setColor(0xFFFFFFFF).setUv(0.75f, 0.25f).setNormal(pose, NORMALS[1]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[2]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[1]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

            vertexConsumer.addVertex(pose, VERTICES[2]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[2]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[4]).setColor(0xFFFFFFFF).setUv(0.75f, 0.25f).setNormal(pose, NORMALS[2]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[3]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[2]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

            vertexConsumer.addVertex(pose, VERTICES[3]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[3]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[4]).setColor(0xFFFFFFFF).setUv(0.75f, 0.25f).setNormal(pose, NORMALS[3]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[0]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[3]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

            vertexConsumer.addVertex(pose, VERTICES[0]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[4]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[1]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[4]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[2]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[4]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

            vertexConsumer.addVertex(pose, VERTICES[2]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[5]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[3]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[5]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
            vertexConsumer.addVertex(pose, VERTICES[0]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[5]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        });

        poseStack.popPose();
    }
}
