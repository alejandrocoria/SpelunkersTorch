package games.alejandrocoria.spelunkerstorch.client;

import com.mojang.logging.annotations.FieldsAreNonnullByDefault;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.SpelunkersTorch;
import games.alejandrocoria.spelunkerstorch.client.renderer.TorchRenderer;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.SectionPos;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ENTITY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public class SpelunkersTorchClient {
    private static final SectionPos INVALID_PLAYER_POSITION = SectionPos.of(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private static SectionPos lastPlayerPosition = INVALID_PLAYER_POSITION;
    private static List<TorchEntity> closestTorches = new ArrayList<>();

    public static void registerBlockEntityRenderer() {
        BlockEntityRenderers.register(TORCH_ENTITY.get(), TorchRenderer::new);
    }

    public static List<TorchEntity> getTorchesInNearbySections(ClientLevel level, SectionPos playerPosition) {
        if (!level.isClientSide()) {
            Constants.LOG.error("SpelunkersTorchClient.getTorchesInNearbySections should be called only in the client.");
            return new ArrayList<>();
        }

        if (lastPlayerPosition.equals(playerPosition)) {
            return closestTorches;
        }

        closestTorches = SpelunkersTorch.getTorchesInNearbySections(level, playerPosition);
        lastPlayerPosition = playerPosition;
        return closestTorches;
    }

    public static void torchEntityAddedOrRemoved(TorchEntity torchEntity) {
        if (torchEntity.getLevel() != null && !torchEntity.getLevel().isClientSide()) {
            Constants.LOG.error("SpelunkersTorchClient.torchEntityAddedOrRemoved should be called only in the client.");
            return;
        }
        lastPlayerPosition = INVALID_PLAYER_POSITION;
    }
}
