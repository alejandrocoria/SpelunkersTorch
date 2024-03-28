package games.alejandrocoria.spelunkerstorch;

import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ENTITY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpelunkersTorch {
    public static void init() {
    }

    public static void updateNearbyOnRemove(Level level, BlockPos blockPos) {
        List<TorchEntity> nearbyTorches = SpelunkersTorch.getNearbyTorchEntities(level, blockPos);
        nearbyTorches.stream()
                .filter(t -> blockPos.equals(t.getTarget()))
                .forEach(TorchEntity::needNewTarget);

        TorchEntity torch = TorchEntity.getFromBlockPos(level, blockPos);
        if (torch != null && torch.hasTarget()) {
            TorchEntity torchTarget = TorchEntity.getFromBlockPos(level, torch.getTarget());
            if (torchTarget != null) {
                torchTarget.removeIncoming(blockPos);
            }
        }
    }

    public static List<TorchEntity> getNearbyTorchEntities(Level level, BlockPos blockPos) {
        SectionPos torchSection = SectionPos.of(blockPos);
        List<ChunkAccess> chunks = new ArrayList<>();
        for (int z = torchSection.z() - 1; z < torchSection.z() + 2; ++z) {
            for (int x = torchSection.x() - 1; x < torchSection.x() + 2; ++x) {
                ChunkAccess chunk = level.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk != null) {
                    chunks.add(chunk);
                }
            }
        }

        List<TorchEntity> torches = new ArrayList<>();

        for (ChunkAccess chunk : chunks) {
            Set<BlockPos> entityPositions = chunk.getBlockEntitiesPos();
            for (BlockPos pos : entityPositions) {
                int distance = pos.distManhattan(blockPos);
                if (distance > 15 || distance == 0) {
                    continue;
                }
                Optional<TorchEntity> blockEntity = chunk.getBlockEntity(pos, TORCH_ENTITY.get());
                blockEntity.ifPresent(torches::add);
            }
        }

        return torches;
    }
}
