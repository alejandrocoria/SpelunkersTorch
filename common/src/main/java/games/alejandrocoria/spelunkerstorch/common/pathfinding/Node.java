package games.alejandrocoria.spelunkerstorch.common.pathfinding;

import net.minecraft.core.BlockPos;

public class Node implements Comparable<Node> {
    public final BlockPos pos;
    public BlockPos previous;
    public double routeDistance;
    public double estimatedDistance;
    public boolean visited;

    public Node(BlockPos pos) {
        this(pos, null, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public Node(BlockPos pos, BlockPos previous, double routeDistance, double estimatedDistance) {
        this.pos = pos;
        this.previous = previous;
        this.routeDistance = routeDistance;
        this.estimatedDistance = estimatedDistance;
        this.visited = false;
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.estimatedDistance, other.estimatedDistance);
    }
}
