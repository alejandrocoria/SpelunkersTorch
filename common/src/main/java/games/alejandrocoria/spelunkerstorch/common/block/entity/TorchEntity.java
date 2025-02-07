package games.alejandrocoria.spelunkerstorch.common.block.entity;

import games.alejandrocoria.spelunkerstorch.Constants;
import games.alejandrocoria.spelunkerstorch.SpelunkersTorch;
import games.alejandrocoria.spelunkerstorch.client.SpelunkersTorchClient;
import games.alejandrocoria.spelunkerstorch.common.block.Torch;
import games.alejandrocoria.spelunkerstorch.common.pathfinding.Path;
import games.alejandrocoria.spelunkerstorch.common.pathfinding.PathFinder;
import games.alejandrocoria.spelunkerstorch.common.util.Util;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ENTITY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public class TorchEntity extends BlockEntity {
    private static final BlockPos TARGET_NOT_CALCULATED = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private long date;
    @Nullable
    private BlockPos target = TARGET_NOT_CALCULATED;
    private List<BlockPos> incoming = new ArrayList<>();
    private List<BlockPos> path = new ArrayList<>();
    @Nullable
    private Quaternionf cachedRotation;

    public TorchEntity(BlockPos blockPos, BlockState state) {
        super(TORCH_ENTITY.get(), blockPos, state);
        this.date = Instant.now().toEpochMilli();
    }

    public long getDate() {
        return this.date;
    }

    @Nullable
    public BlockPos getTarget() {
        return this.target;
    }

    public List<BlockPos> getIncoming() {
        return this.incoming;
    }

    public List<BlockPos> getPath() {
        return this.path;
    }

    @Nullable
    public Quaternionf getRotation() {
        if (this.cachedRotation == null) {
            this.recalculateRotation();
        }
        return this.cachedRotation;
    }

    public void needNewTarget() {
        this.target = TARGET_NOT_CALCULATED;
    }

    public void recalculateTargetIfNeeded() {
        if (this.level == null || !TARGET_NOT_CALCULATED.equals(this.target)) {
            return;
        }

        this.setBlock();

        PathFinder pathFinder = new PathFinder(this.level, this.worldPosition);
        Path result = pathFinder.calculateMinPath();

        if (result != null) {
            this.path = result.positions;
            this.target = this.path.getLast();
            this.setBlock();
            SpelunkersTorch.getTorchEntity(this.level, this.target).ifPresent((t) -> t.addIncoming(this.worldPosition));
        } else {
            this.target = null;
            this.path.clear();
        }
    }

    private void addIncoming(BlockPos torch) {
        if (!this.incoming.contains(torch)) {
            this.incoming.add(torch);
            this.setBlock(true);
        }
    }

    public void removeIncoming(BlockPos torch) {
        if (this.incoming.remove(torch)) {
            this.setBlock(true);
        }
    }

    private void setBlock() {
        this.setBlock(false);
    }

    private void setBlock(boolean forceChange) {
        if (this.level != null && this.level.getBlockState(this.worldPosition) == getBlockState()) {
            if (forceChange) {
                this.level.setBlock(this.worldPosition, getBlockState().setValue(Torch.HAS_TARGET, !this.hasTarget()), Block.UPDATE_CLIENTS);
            }
            this.level.setBlock(this.worldPosition, getBlockState().setValue(Torch.HAS_TARGET, this.hasTarget()), Block.UPDATE_CLIENTS);
        }
    }

    public int distanceComparator(TorchEntity t1, TorchEntity t2) {
        int distance1 = this.worldPosition.distManhattan(t1.worldPosition);
        int distance2 = this.worldPosition.distManhattan(t2.worldPosition);
        if (distance1 == distance2) {
            return Long.compare(t1.date, t2.date);
        }
        return distance1 - distance2;
    }

    public boolean hasTarget() {
        return this.target != null && !TARGET_NOT_CALCULATED.equals(this.target);
    }

    private void recalculateRotation() {
        if (!this.hasTarget()) {
            this.cachedRotation = null;
            return;
        }

        BlockPos toTarget = this.target.subtract(this.worldPosition);
        this.cachedRotation = Util.getRotation(Vec3.atLowerCornerOf(toTarget));
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (this.level != null) {
            if (this.level.isClientSide()) {
                SpelunkersTorchClient.torchEntityAddedOrRemoved(this);
            } else {
                SpelunkersTorch.addSectionAndNeighborsToMonitor((ServerLevel) this.level, SectionPos.of(this.worldPosition));
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null && this.level.isClientSide()) {
            SpelunkersTorchClient.torchEntityAddedOrRemoved(this);
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (this.level != null && this.level.isClientSide()) {
            SpelunkersTorchClient.torchEntityAddedOrRemoved(this);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        try {
            super.saveAdditional(nbt, registries);
            nbt.putLong("Date", this.date);

            if (this.hasTarget()) {
                nbt.putLong("Target", this.target.asLong());
            }

            if (!this.incoming.isEmpty()) {
                nbt.putLongArray("Incoming", this.incoming.stream()
                        .mapToLong(BlockPos::asLong)
                        .toArray());
            }

            if (!this.path.isEmpty()) {
                nbt.putLongArray("Path", this.path.stream()
                        .mapToLong(BlockPos::asLong)
                        .toArray());
            }
        } catch (Exception e) {
            Constants.LOG.error("Error in TorchEntity.saveAdditional", e);
        }
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        try {
            super.loadAdditional(nbt, registries);
            this.date = nbt.getLong("Date");

            this.target = null;
            if (nbt.contains("Target")) {
                this.target = BlockPos.of(nbt.getLong("Target"));
            }

            this.incoming.clear();
            if (nbt.contains("Incoming")) {
                this.incoming = Arrays.stream(nbt.getLongArray("Incoming"))
                        .mapToObj(BlockPos::of)
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            this.path.clear();
            if (nbt.contains("Path")) {
                this.path = Arrays.stream(nbt.getLongArray("Path"))
                        .mapToObj(BlockPos::of)
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            this.cachedRotation = null;
        } catch (Exception e) {
            Constants.LOG.error("Error in TorchEntity.load", e);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    public static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide() && blockEntityType == TORCH_ENTITY.get()) {
            return (l, p, s, blockEntity) -> ((TorchEntity) blockEntity).recalculateTargetIfNeeded();
        }
        return null;
    }

    @Nullable
    public static TorchEntity getFromBlockPos(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity != null && blockEntity.getType() == TORCH_ENTITY.get()) {
            return (TorchEntity) blockEntity;
        }

        return null;
    }

    public static int distanceComparator(BlockPos position, TorchEntity t1, TorchEntity t2) {
        int distance = (int) (position.distSqr(t1.getBlockPos()) - position.distSqr(t2.getBlockPos()));
        if (distance == 0) {
            return Long.compare(t1.getDate(), t2.getDate());
        }
        return distance;
    }
}
