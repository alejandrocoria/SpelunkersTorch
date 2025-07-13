package games.alejandrocoria.spelunkerstorch;

import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import games.alejandrocoria.spelunkerstorch.common.pathfinding.PathFindingCache;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ENTITY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpelunkersTorch {
    private static final int WAIT_TIME_TO_UPDATE = 100;

    private static final Object2ObjectMap<ServerLevel, LongSet> sectionsToUpdate = new Object2ObjectAVLTreeMap<>(Comparator.comparingInt(Object::hashCode));
    private static long lastAddedSectionToUpdate = Long.MAX_VALUE - WAIT_TIME_TO_UPDATE;
    private static final Object2ObjectMap<ServerLevel, LongSet> sectionsToMonitor = new Object2ObjectAVLTreeMap<>(Comparator.comparingInt(Object::hashCode));

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

    public static List<TorchEntity> getTorchesInNearbySections(Level level, SectionPos sectionPos) {
        List<ChunkAccess> chunks = new ArrayList<>();
        for (int z = sectionPos.z() - 1; z < sectionPos.z() + 2; ++z) {
            for (int x = sectionPos.x() - 1; x < sectionPos.x() + 2; ++x) {
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
                if (pos.getY() < sectionPos.minBlockY() - 16 || pos.getY() > sectionPos.maxBlockY() + 16) {
                    continue;
                }
                Optional<TorchEntity> blockEntity = chunk.getBlockEntity(pos, TORCH_ENTITY.get());
                blockEntity.ifPresent(torches::add);
            }
        }

        return torches;
    }

    public static List<TorchEntity> getNearbyTorchEntities(Level level, BlockPos blockPos) {
        List<TorchEntity> torches = getTorchesInNearbySections(level, SectionPos.of(blockPos));
        torches.removeIf((t) -> {
            double distance = t.getBlockPos().distSqr(blockPos);
            return distance > 16 * 16 || distance <= 0.5;
        });

        return torches;
    }

    public static Optional<TorchEntity> getTorchEntity(Level level, BlockPos blockPos) {
        return level.getChunkAt(blockPos).getBlockEntity(blockPos, TORCH_ENTITY.get());
    }

    public static int recalculateTorches(Level level) {
        try {
            int count = 0;
            if (level instanceof ServerLevel serverLevel) {
                Iterable<ChunkHolder> chunks = serverLevel.getChunkSource().chunkMap.getChunks();
                for (ChunkHolder chunkHolder : chunks) {
                    count += SpelunkersTorch.recalculateTorches(level, chunkHolder.getPos());
                }
            }
            return count;
        } catch (Exception e) {
            Constants.LOG.error("Error in recalculateTorches", e);
        }

        return 0;
    }

    public static int recalculateTorches(Level level, ChunkPos chunkPos) {
        if (!allNeighborsChunksLoaded(level, chunkPos)) {
            return -1;
        }

        int count = 0;
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
        Set<BlockPos> entityPositions = chunk.getBlockEntitiesPos();
        for (BlockPos pos : entityPositions) {
            Optional<TorchEntity> blockEntity = chunk.getBlockEntity(pos, TORCH_ENTITY.get());
            blockEntity.ifPresent(TorchEntity::needNewTarget);
            if (blockEntity.isPresent()) {
                ++count;
            }
        }
        return count;
    }

    public static int recalculateTorches(Level level, SectionPos sectionPos) {
        if (!allNeighborsChunksLoaded(level, sectionPos.chunk())) {
            return -1;
        }

        int count = 0;
        ChunkAccess chunk = level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.FULL, false);
        Set<BlockPos> entityPositions = chunk.getBlockEntitiesPos();
        for (BlockPos pos : entityPositions) {
            if (pos.getY() / 16 == sectionPos.y()) {
                Optional<TorchEntity> blockEntity = chunk.getBlockEntity(pos, TORCH_ENTITY.get());
                blockEntity.ifPresent(TorchEntity::needNewTarget);
                if (blockEntity.isPresent()) {
                    ++count;
                }
            }
        }
        return count;
    }

    public static boolean recalculateTorch(Level level, BlockPos blockPos) {
        Optional<TorchEntity> blockEntity = level.getBlockEntity(blockPos, TORCH_ENTITY.get());
        blockEntity.ifPresent(TorchEntity::needNewTarget);
        return blockEntity.isPresent();
    }

    @Nullable
    public static BlockPos recalculateClosestTorch(Level level, BlockPos blockPos) {
        List<TorchEntity> nearbyTorches = SpelunkersTorch.getNearbyTorchEntities(level, blockPos);
        Optional<TorchEntity> closestTorch = nearbyTorches.stream()
                .min((t1, t2) -> TorchEntity.distanceComparator(blockPos, t1, t2));
        closestTorch.ifPresent(TorchEntity::needNewTarget);
        return closestTorch.map(TorchEntity::getBlockPos).orElse(null);
    }

    public static boolean allNeighborsChunksLoaded(Level level, ChunkPos chunkPos) {
        Cursor3D cursor = new Cursor3D(
                chunkPos.x - 1,
                0,
                chunkPos.z - 1,
                chunkPos.x + 1,
                0,
                chunkPos.z + 1);
        while (cursor.advance()) {
            if (level.getChunk(cursor.nextX(), cursor.nextZ(), ChunkStatus.FULL, false) == null) {
                return false;
            }
        }

        return true;
    }

    public static void onBlockUpdated(ServerLevel level, BlockPos pos, BlockState blockState) {
        SectionPos sectionPos = SectionPos.of(pos);

        LongSet sections = sectionsToMonitor.get(level);
        if (sections == null || !sections.contains(sectionPos.asLong())) {
            return;
        }

        PathFindingCache.removeIfChanged(pos, blockState);
        addSectionAndNeighborsToMap(level, sectionPos, sectionsToUpdate);
        if (lastAddedSectionToUpdate - WAIT_TIME_TO_UPDATE > System.currentTimeMillis()) {
            lastAddedSectionToUpdate = System.currentTimeMillis();
        }
    }

    public static void serverTick() {
        if (lastAddedSectionToUpdate + WAIT_TIME_TO_UPDATE > System.currentTimeMillis()) {
            return;
        }

        ObjectIterator<Map.Entry<ServerLevel, LongSet>> mapIterator = sectionsToUpdate.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<ServerLevel, LongSet> entry = mapIterator.next();
            LongIterator setIterator = entry.getValue().iterator();
            while (setIterator.hasNext()) {
                int count = recalculateTorches(entry.getKey(), SectionPos.of(setIterator.nextLong()));
                if (count > -1) {
                    setIterator.remove();
                }
            }
            if (entry.getValue().isEmpty()) {
                mapIterator.remove();
            }
        }

        if (sectionsToUpdate.isEmpty()) {
            lastAddedSectionToUpdate = Long.MAX_VALUE - WAIT_TIME_TO_UPDATE;
        } else {
            lastAddedSectionToUpdate = System.currentTimeMillis();
        }
    }

    public static void addSectionAndNeighborsToMonitor(ServerLevel level, SectionPos sectionPos) {
        addSectionAndNeighborsToMap(level, sectionPos, sectionsToMonitor);
    }

    public static void updateSectionAndNeighbors(ServerLevel level, SectionPos sectionPos) {
        addSectionAndNeighborsToMap(level, sectionPos, sectionsToUpdate);
    }

    private static void addSectionAndNeighborsToMap(ServerLevel level, SectionPos sectionPos, Object2ObjectMap<ServerLevel, LongSet> targetMap) {
        LongSet sections = targetMap.computeIfAbsent(level, (l) -> new LongAVLTreeSet());
        Cursor3D cursor = new Cursor3D(
                sectionPos.x() - 1,
                Mth.clamp(sectionPos.y() - 1, level.getMinSectionY(), level.getMaxSectionY()),
                sectionPos.z() - 1,
                sectionPos.x() + 1,
                Mth.clamp(sectionPos.y() + 1, level.getMinSectionY(), level.getMaxSectionY()),
                sectionPos.z() + 1);
        while (cursor.advance()) {
            sections.add(SectionPos.asLong(cursor.nextX(), cursor.nextY(), cursor.nextZ()));
        }
    }
}
