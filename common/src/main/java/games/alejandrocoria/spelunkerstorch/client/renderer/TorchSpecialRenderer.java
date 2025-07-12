package games.alejandrocoria.spelunkerstorch.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import games.alejandrocoria.spelunkerstorch.Registry;
import games.alejandrocoria.spelunkerstorch.client.SpelunkersTorchClient;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import games.alejandrocoria.spelunkerstorch.common.util.Util;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TorchSpecialRenderer implements NoDataSpecialModelRenderer {
    public TorchSpecialRenderer() {
    }

    public void render(ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, boolean hasFoilType) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) {
            return;
        }

        if (displayContext.firstPerson()) {
            poseStack.pushPose();
            poseStack.translate(0, -0.1, 0);
            Quaternionf rotation = new Quaternionf(new AxisAngle4f(-0.35f, 1, 0, 0));
            poseStack.rotateAround(rotation, 0, 0.75f, 0);
        }

        BlockStateModel blockStateModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(Registry.TORCH_BLOCK.get().defaultBlockState());
        ModelBlockRenderer.renderModel(
                poseStack.last(),
                bufferSource.getBuffer(Sheets.cutoutBlockSheet()),
                blockStateModel,
                1f, 1f, 0f,
                light, overlay);

        if (displayContext.firstPerson()) {
            poseStack.popPose();

            Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            List<TorchEntity> nearbyTorches = SpelunkersTorchClient.getTorchesInNearbySections(Minecraft.getInstance().level, SectionPos.of(BlockPos.containing(cameraPos)));
            if (!nearbyTorches.isEmpty()) {
                Quaternionf needleRotation = calculateNeedleRotation(player, cameraPos, nearbyTorches);
                if (needleRotation != null) {
                    TorchRenderer.renderNeedle(poseStack, needleRotation, bufferSource, light, overlay);
                }
            }
        } else {
            poseStack.pushPose();
            poseStack.translate(0, -0.1, 0);

            Quaternionf rotation = new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, 1, 0));
            rotation.mul(new Quaternionf(new AxisAngle4f(-Mth.HALF_PI / 2, 1, 0, 0)));
            TorchRenderer.renderNeedle(poseStack, rotation, bufferSource, light, overlay);

            poseStack.popPose();
        }
    }

    @Nullable
    private static Quaternionf calculateNeedleRotation(LocalPlayer player, Vec3 cameraPos, List<TorchEntity> nearbyTorches) {
        double LIMIT = 256.0;
        Vec3 nodeSum = Vec3.ZERO;
        Vec3 pathSum = Vec3.ZERO;
        double distanceToClosestPath = LIMIT;
        for (TorchEntity torchEntity : nearbyTorches) {
            if (!torchEntity.hasTarget() || torchEntity.getPath().isEmpty()) {
                continue;
            }

            double distanceToClosest = LIMIT;
            Vec3 closest = null;

            List<BlockPos> path = torchEntity.getPath();
            BlockPos prev = path.getFirst();
            for (int i = 1; i < path.size(); ++i) {
                BlockPos node = path.get(i);
                Vec3 closestPoint = getClosestPoint(cameraPos, prev.getCenter(), node.getCenter());
                double distanceToPoint = closestPoint.distanceToSqr(cameraPos);
                if (distanceToPoint < distanceToClosest) {
                    distanceToClosest = distanceToPoint;
                    closest = closestPoint;
                }
                Vec3 toNextNode = Vec3.atLowerCornerOf(node.subtract(prev));
                double factor = toNextNode.length() * Math.max(1.0 / Math.clamp(distanceToPoint, 1.0, LIMIT) - 1.0 / LIMIT, 0.0);
                toNextNode = toNextNode.scale(factor);
                nodeSum = nodeSum.add(toNextNode);
                prev = node;
            }

            if (closest != null) {
                closest = closest.subtract(cameraPos);
                double factor = 1.0 / closest.lengthSqr();
                closest = closest.scale(factor);
                pathSum = pathSum.add(closest);
                if (distanceToClosest < distanceToClosestPath) {
                    distanceToClosestPath = distanceToClosest;
                }
            }
        }

        if (nodeSum.equals(Vec3.ZERO) && pathSum.equals(Vec3.ZERO)) {
            return null;
        }

        nodeSum = nodeSum.normalize();
        pathSum = pathSum.normalize();
        double pathFactor = 1 / (1 + Math.pow(2, -Math.sqrt(distanceToClosestPath)/*-distanceToClosestPath*/ + 4));
        Vec3 averageSum = nodeSum.scale(1.0 - pathFactor).add(pathSum.scale(pathFactor));

        float rotX = player.getViewXRot(1) * Mth.DEG_TO_RAD;
        float rotY = player.getViewYRot(1) * Mth.DEG_TO_RAD;
        averageSum = averageSum.yRot(rotY);
        averageSum = averageSum.xRot(rotX);
        averageSum = averageSum.yRot(-Mth.PI / 2);

        return Util.getRotation(averageSum);
    }

    private static Vec3 getClosestPoint(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        Vec3 ap = p.subtract(a);
        double dotApAb = ap.dot(ab);
        if (dotApAb <= 0.0) {
            return a;
        }

        Vec3 bp = p.subtract(b);
        if (bp.dot(ab) >= 0.0) {
            return b;
        }

        return a.add(ab.scale(dotApAb / ab.lengthSqr()));
    }

    @Override
    public void getExtents(Set<Vector3f> set) {
        set.isEmpty();
    }


    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<TorchSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new TorchSpecialRenderer.Unbaked());

        public MapCodec<TorchSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            return new TorchSpecialRenderer();
        }
    }
}
