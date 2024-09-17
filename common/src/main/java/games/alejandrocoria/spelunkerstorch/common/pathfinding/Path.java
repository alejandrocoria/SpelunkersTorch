package games.alejandrocoria.spelunkerstorch.common.pathfinding;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Path {
    public final List<BlockPos> positions = new ArrayList<>();
    public double length;

    public Path(Map<BlockPos, Node> nodeMap, Node node) {
        this.length = node.routeDistance;

        List<BlockPos> positionsReversed = new ArrayList<>();
        BlockPos pos = node.pos;
        do {
            positionsReversed.add(pos);
            pos = nodeMap.get(pos).previous;
        } while (pos != null);

        BlockPos prev = positionsReversed.getLast();
        BlockPos delta = BlockPos.ZERO;
        for (int i = positionsReversed.size() - 2; i >= 0; --i) {
            pos = positionsReversed.get(i);
            BlockPos newDelta = pos.subtract(prev);
            if (!newDelta.equals(delta)) {
                this.positions.add(prev);
                delta = newDelta;
            }
            prev = pos;
        }

        this.positions.add(positionsReversed.getFirst());
    }
}
