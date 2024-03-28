package games.alejandrocoria.spelunkerstorch.client;

import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.SpelunkersTorch;
import games.alejandrocoria.spelunkerstorch.client.renderer.TorchRenderer;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ENTITY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public class SpelunkersTorchClient {
    private static final BlockPos INVALID_PLAYER_POSITION = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private static BlockPos lastPlayerPosition = INVALID_PLAYER_POSITION;
    @Nullable
    private static TorchEntity closestTorch = null;

    public static void init() {
        BlockEntityRenderers.register(TORCH_ENTITY.get(), TorchRenderer::new);
    }

    @Nullable
    public static TorchEntity getClosestTorch(ClientLevel level, BlockPos playerPosition) {
        if (!level.isClientSide()) {
            Constants.LOG.error("SpelunkersTorchClient.getClosestTorch should be called only in the client.");
            return null;
        }

        if (lastPlayerPosition.equals(playerPosition)) {
            return closestTorch;
        }

        List<TorchEntity> nearbyTorches = SpelunkersTorch.getNearbyTorchEntities(level, playerPosition);
        closestTorch = nearbyTorches.stream()
                .min((t1, t2) -> distanceComparator(playerPosition, t1, t2))
                .orElse(null);

        lastPlayerPosition = playerPosition;
        return closestTorch;
    }

    public static void torchEntityAddOrRemoved(TorchEntity torchEntity) {
        if (torchEntity.getLevel() != null && !torchEntity.getLevel().isClientSide()) {
            Constants.LOG.error("SpelunkersTorchClient.torchEntityAddOrRemoved should be called only in the client.");
            return;
        }
        lastPlayerPosition = INVALID_PLAYER_POSITION;
    }

    private static int distanceComparator(BlockPos position, TorchEntity t1, TorchEntity t2) {
        int distance = (int) (position.distSqr(t1.getBlockPos()) - position.distSqr(t2.getBlockPos()));
        if (distance == 0) {
            return Long.compare(t1.getDate(), t2.getDate());
        }
        return distance;
    }
}
