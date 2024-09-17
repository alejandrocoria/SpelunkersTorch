package games.alejandrocoria.spelunkerstorch.common.pathfinding;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PathFindingCache {
    private static final Long2ObjectMap<PathFindingSection> cache = new Long2ObjectOpenHashMap<>();

    public static PathFindingSection getSection(Level level, SectionPos pos) {
        return cache.computeIfAbsent(pos.asLong(), (l) -> new PathFindingSection(level, pos));
    }

    public static void removeIfChanged(BlockPos pos, BlockState blockState) {
        SectionPos sectionPos = SectionPos.of(pos);
        PathFindingSection section = cache.get(sectionPos.asLong());
        if (section != null) {
            if (!section.equalToBlockState(pos.getX(), pos.getY(), pos.getZ(), blockState)) {
                cache.remove(sectionPos.asLong());
            }
        }
    }
}
