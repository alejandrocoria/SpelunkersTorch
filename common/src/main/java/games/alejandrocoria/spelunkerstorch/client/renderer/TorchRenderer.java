package games.alejandrocoria.spelunkerstorch.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import games.alejandrocoria.spelunkerstorch.common.block.WallTorch;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TorchRenderer implements BlockEntityRenderer<TorchEntity> {
    private static final float NEEDLE_HALF_WIDTH = 0.12f;
    private static final float NEEDLE_LENGTH = 0.23f;

    public TorchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TorchEntity torchEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
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
        PoseStack.Pose entry = poseStack.last();
        Matrix4f matrixPos = entry.pose();
        buffer.addVertex(matrixPos, -NEEDLE_HALF_WIDTH, 0, 0).setColor(1.f, 1.f, 1.f, 1.f).setLight(light).setOverlay(overlay);
        buffer.addVertex(matrixPos, NEEDLE_HALF_WIDTH, 0, 0).setColor(1.f, 1.f, 1.f, 1.f).setLight(light).setOverlay(overlay);
        buffer.addVertex(matrixPos, 0, -NEEDLE_HALF_WIDTH, 0).setColor(1.f, 1.f, 1.f, 1.f).setLight(light).setOverlay(overlay);
        buffer.addVertex(matrixPos, 0, 0, NEEDLE_LENGTH).setColor(0.7f, 0.7f, 0.7f, 1.f).setLight(light).setOverlay(overlay);
        buffer.addVertex(matrixPos, -NEEDLE_HALF_WIDTH, 0, 0).setColor(0.8f, 0.8f, 0.8f, 1.f).setLight(light).setOverlay(overlay);
        buffer.addVertex(matrixPos, 0, NEEDLE_HALF_WIDTH, 0).setColor(0.9f, 0.9f, 0.9f, 1.f).setLight(light).setOverlay(overlay);
        buffer.addVertex(matrixPos, NEEDLE_HALF_WIDTH, 0, 0).setColor(1.f, 1.f, 1.f, 1.f).setLight(light).setOverlay(overlay);
        buffer.addVertex(matrixPos, 0, 0, NEEDLE_LENGTH).setColor(0.85f, 0.85f, 0.85f, 1.f).setLight(light).setOverlay(overlay);

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
