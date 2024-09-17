package games.alejandrocoria.spelunkerstorch.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.client.SpelunkersTorchClient;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import games.alejandrocoria.spelunkerstorch.common.util.Util;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_BLOCK;
import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ITEM;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TorchBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ModelResourceLocation TORCH_MODEL = new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "torch"), "has_target=false");

    public TorchBlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher renderDispatcher, EntityModelSet entityModelSet) {
        super(renderDispatcher, entityModelSet);
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext type, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        Item item = itemStack.getItem();
        if (item != TORCH_ITEM.get()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) {
            return;
        }

        poseStack.pushPose();

        boolean inHand = type.firstPerson() || type == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || type == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (type.firstPerson()) {
            double xOffset = 0.35;
            if (type == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                xOffset *= -1;
            }
            poseStack.translate(xOffset, 0, -0.25);
        } else if (type == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || type == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            poseStack.translate(0, 0.2, 0);
        }

        poseStack.pushPose();
        if (type.firstPerson()) {
            poseStack.translate(0, -0.1, 0);
            Quaternionf rotation = new Quaternionf(new AxisAngle4f(-0.35f, 1, 0, 0));
            poseStack.rotateAround(rotation, 0, 0.75f, 0);
        }

        BakedModel blockModel = Minecraft.getInstance().getModelManager().getModel(TORCH_MODEL);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                multiBufferSource.getBuffer(Sheets.cutoutBlockSheet()),
                TORCH_BLOCK.get().defaultBlockState(),
                blockModel,
                1f, 1f, 0f,
                light, overlay);

        poseStack.popPose();

        if (inHand) {
            Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            List<TorchEntity> nearbyTorches = SpelunkersTorchClient.getTorchesInNearbySections(Minecraft.getInstance().level, SectionPos.of(BlockPos.containing(cameraPos)));
            if (!nearbyTorches.isEmpty()) {
                Quaternionf needleRotation = calculateNeedleRotation(player, cameraPos, nearbyTorches);
                if (needleRotation != null) {
                    TorchRenderer.renderNeedle(poseStack, needleRotation, multiBufferSource, light, overlay);
                }
            }
        } else {
            poseStack.pushPose();
            poseStack.translate(0, -0.1, 0);

            Quaternionf rotation = new Quaternionf(new AxisAngle4f(Mth.HALF_PI, 0, 1, 0));
            rotation.mul(new Quaternionf(new AxisAngle4f(-Mth.HALF_PI / 2, 1, 0, 0)));
            TorchRenderer.renderNeedle(poseStack, rotation, multiBufferSource, light, overlay);

            poseStack.popPose();
        }

        poseStack.popPose();
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
                closest.scale(factor);
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
        averageSum = averageSum.yRot(rotY + Mth.PI);
        averageSum = averageSum.xRot(-rotX);

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
}
