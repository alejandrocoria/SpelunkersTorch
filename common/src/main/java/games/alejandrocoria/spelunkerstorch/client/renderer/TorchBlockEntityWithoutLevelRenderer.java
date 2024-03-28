package games.alejandrocoria.spelunkerstorch.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.client.SpelunkersTorchClient;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import games.alejandrocoria.spelunkerstorch.common.util.Util;
import games.alejandrocoria.spelunkerstorch.platform.Services;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_BLOCK;
import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ITEM;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TorchBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TORCH_MODEL = new ModelResourceLocation(Constants.MOD_ID, "torch", "has_target=false");

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

        BakedModel blockModel = Services.PLATFORM.getModel(Minecraft.getInstance().getModelManager(), TORCH_MODEL);
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
            TorchEntity closestTorch = SpelunkersTorchClient.getClosestTorch(Minecraft.getInstance().level, BlockPos.containing(cameraPos));
            if (closestTorch != null) {
                Quaternionf needleRotation = calculateNeedleRotation(player, cameraPos, closestTorch);
                TorchRenderer.renderNeedle(poseStack, needleRotation, multiBufferSource, light, overlay);
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

    private static Quaternionf calculateNeedleRotation(LocalPlayer player, Vec3 cameraPos, TorchEntity closestTorch) {
        Vec3 needleTarget = closestTorch.getBlockPos().getCenter();

        BlockPos torchTargetPos = closestTorch.getTarget();
        if (torchTargetPos != null) {
            BlockPos torchPos = closestTorch.getBlockPos();
            Vec3 torchToPlayer = cameraPos.subtract(torchPos.getCenter());
            Vec3 targetToPlayer = cameraPos.subtract(torchTargetPos.getCenter());
            double distTorchToTarget = Math.sqrt(torchPos.distSqr(torchTargetPos));
            double distTorchToPlayer = torchToPlayer.length();
            double distTargetToPlayer = targetToPlayer.length();

            double distanceFactor1 = distTorchToPlayer / Mth.clamp(10 - distTorchToTarget, 3, 10);
            distanceFactor1 = 1 - Mth.clamp(distanceFactor1, 0, 1);

            double distanceFactor2 = (distTorchToTarget / (distTorchToPlayer + distTargetToPlayer) - 0.5) * 2;
            distanceFactor2 = Mth.clamp(distanceFactor2, 0, 1);

            double mainFactor = distanceFactor1 + distanceFactor2;

            if (!closestTorch.getIncoming().isEmpty()) {
                double dotFactor = closestTorch.getIncoming().stream()
                        .map(p -> p.subtract(torchPos))
                        .map(Vec3::atLowerCornerOf)
                        .map(p -> p.dot(torchToPlayer) / p.lengthSqr())
                        .max(Double::compare)
                        .map(d -> Mth.clamp(d * 2, 0, 1))
                        .orElse(0d);

                mainFactor -= dotFactor;
            }

            needleTarget = torchPos.getCenter().lerp(torchTargetPos.getCenter(), Mth.clamp(mainFactor, 0, 1));
        }

        float rotX = player.getViewXRot(1) * Mth.DEG_TO_RAD;
        float rotY = player.getViewYRot(1) * Mth.DEG_TO_RAD;

        Vec3 toTorch = needleTarget.subtract(cameraPos);
        toTorch = toTorch.yRot(rotY + Mth.PI);
        toTorch = toTorch.xRot(-rotX);

        return Util.getRotation(toTorch);
    }
}
