package games.alejandrocoria.spelunkerstorch.common.pathfinding;

import games.alejandrocoria.spelunkerstorch.Constants;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class PathFindingSection {
    private SectionPos pos;
    private final boolean[] blocks = new boolean[4096];

    public PathFindingSection(Level level, SectionPos pos) {
        this.pos = pos;

        try {
            ChunkAccess chunk = level.getChunk(pos.x(), pos.z(), ChunkStatus.FULL, false);
            if (chunk == null) {
                return;
            }
            if (pos.y() < chunk.getMinSection() || pos.y() > chunk.getMaxSection()) {
                return;
            }
            if (chunk.isSectionEmpty(pos.y())) {
                return;
            }

            LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(pos.getY()));
            Cursor3D cursor = new Cursor3D(0, 0, 0, 15, 15, 15);
            while (cursor.advance()) {
                int x = cursor.nextX();
                int y = cursor.nextY();
                int z = cursor.nextZ();
                this.blocks[toIndex(x, y, z)] = isBlocked(section, x, y, z);
            }
        } catch (Exception e) {
            Constants.LOG.error("Error in PathFindingSection", e);
        }
    }

    public SectionPos getPos() {
        return this.pos;
    }

    public boolean getBlock(int x, int y, int z) {
        return this.blocks[toIndex(x, y, z)];
    }

    public boolean equalToBlockState(int x, int y, int z, BlockState blockState) {
        return this.blocks[toIndex(x, y, z)] == isBlocked(blockState);
    }


    private static boolean isBlocked(LevelChunkSection section, int x, int y, int z) {
        return isBlocked(section.getBlockState(x, y, z));
    }

    private static boolean isBlocked(BlockState blockState) {
        return !blockState.isPathfindable(PathComputationType.AIR);
    }

    private static int toIndex(int x, int y, int z) {
        return (x & 15) + (y & 15) * 16 + (z & 15) * 256;
    }
}
