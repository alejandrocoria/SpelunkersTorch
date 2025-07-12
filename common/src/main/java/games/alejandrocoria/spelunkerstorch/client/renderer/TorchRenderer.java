package games.alejandrocoria.spelunkerstorch.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import games.alejandrocoria.spelunkerstorch.common.block.WallTorch;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import games.alejandrocoria.spelunkerstorch.common.util.Util;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TorchRenderer implements BlockEntityRenderer<TorchEntity> {
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
    public void render(TorchEntity torchEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay, Vec3 vec3) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Quaternionf rotation = torchEntity.getRotation();
        if (rotation == null) {
            return;
        }

        poseStack.pushPose();
        Optional<Direction> direction = torchEntity.getBlockState().getOptionalValue(WallTorch.FACING).map(Direction::getOpposite);
        direction.ifPresent(value -> {
            poseStack.translate(value.getStepX() * WallTorch.H_OFFSET, WallTorch.V_OFFSET, value.getStepZ() * WallTorch.H_OFFSET);
        });

        renderNeedle(poseStack, rotation, multiBufferSource, light, overlay);
//        renderDebugPath(poseStack, torchEntity.getBlockPos(), torchEntity.getPath(), multiBufferSource, light, overlay);

        poseStack.popPose();
    }

    public static void renderNeedle(PoseStack poseStack, Quaternionf rotation, MultiBufferSource multiBufferSource, int light, int overlay) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.75, 0.5);
        poseStack.rotateAround(rotation, 0, 0, 0);
        poseStack.translate(0, 0, 0.125);

        VertexConsumer buffer = multiBufferSource.getBuffer(NeedleRenderType.getRenderType());
        PoseStack.Pose pose = poseStack.last();

        buffer.addVertex(pose, VERTICES[0]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[0]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[4]).setColor(0xFFFFFFFF).setUv(0.75f, 0.25f).setNormal(pose, NORMALS[0]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[1]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[0]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

        buffer.addVertex(pose, VERTICES[1]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[1]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[4]).setColor(0xFFFFFFFF).setUv(0.75f, 0.25f).setNormal(pose, NORMALS[1]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[2]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[1]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

        buffer.addVertex(pose, VERTICES[2]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[2]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[4]).setColor(0xFFFFFFFF).setUv(0.75f, 0.25f).setNormal(pose, NORMALS[2]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[3]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[2]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

        buffer.addVertex(pose, VERTICES[3]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[3]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[4]).setColor(0xFFFFFFFF).setUv(0.75f, 0.25f).setNormal(pose, NORMALS[3]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[0]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[3]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

        buffer.addVertex(pose, VERTICES[0]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[4]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[1]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[4]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[2]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[4]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

        buffer.addVertex(pose, VERTICES[2]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[5]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[3]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[5]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.addVertex(pose, VERTICES[0]).setColor(0xFFFFFFFF).setUv(0.25f, 0.25f).setNormal(pose, NORMALS[5]).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    public static void renderDebugPath(PoseStack poseStack, BlockPos torchPos, List<BlockPos> path, MultiBufferSource multiBufferSource, int light, int overlay) {
        if (path.isEmpty()) {
            return;
        }

        VertexConsumer buffer = multiBufferSource.getBuffer(NeedleRenderType.getRenderType());
        float halfX = 0.1f;
        float halfY = 0.07f;
        float halfZ = 0.07f;

        for (BlockPos node : path) {
            poseStack.pushPose();

            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.translate(node.getX() - torchPos.getX(), node.getY() - torchPos.getY(), node.getZ() - torchPos.getZ());

            PoseStack.Pose entry = poseStack.last();
            Matrix4f matrixPos = entry.pose();
            buffer.addVertex(matrixPos, -halfX, -halfY, halfZ).setColor(0.7f, 0.7f, 0.7f, 1.f).setLight(light).setOverlay(overlay);
            buffer.addVertex(matrixPos, 0, -halfY, -halfZ).setColor(0.7f, 0.7f, 0.7f, 1.f).setLight(light).setOverlay(overlay);
            buffer.addVertex(matrixPos, halfX, -halfY, halfZ).setColor(0.7f, 0.7f, 0.7f, 1.f).setLight(light).setOverlay(overlay);
            buffer.addVertex(matrixPos, 0, halfY, 0).setColor(0.85f, 0.85f, 0.85f, 1.f).setLight(light).setOverlay(overlay);
            buffer.addVertex(matrixPos, -halfX, -halfY, halfZ).setColor(0.9f, 0.9f, 0.9f, 1.f).setLight(light).setOverlay(overlay);
            buffer.addVertex(matrixPos, 0, -halfY, -halfZ).setColor(1.f, 1.f, 1.f, 1.f).setLight(light).setOverlay(overlay);

            poseStack.popPose();
        }
    }
}
