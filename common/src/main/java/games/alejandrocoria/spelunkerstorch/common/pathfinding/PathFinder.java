package games.alejandrocoria.spelunkerstorch.common.pathfinding;

import games.alejandrocoria.spelunkerstorch.SpelunkersTorch;
import games.alejandrocoria.spelunkerstorch.common.block.entity.TorchEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import static games.alejandrocoria.spelunkerstorch.Registry.TORCH_ENTITY;

public class PathFinder {
    private static final double[] NEIGHBOR_DISTANCE = {0.0, 1.0, Math.sqrt(2.0), Math.sqrt(3.0)};

    private final BlockPos torchPos;
    private List<TorchEntity> targets;
    private final PathFindingSection[] sections = new PathFindingSection[27];

    private final Queue<Node> openSet = new PriorityQueue<>();
    private final Map<BlockPos, Node> allNodes = new HashMap<>();
    private final BlockPos[] neighbors = new BlockPos[28];
    private int neighborCount;

    public PathFinder(Level level, BlockPos torchPos) {
        this.torchPos = torchPos;
        TorchEntity torch = level.getBlockEntity(torchPos, TORCH_ENTITY.get()).orElse(null);
        if (torch == null) {
            return;
        }

        this.targets = SpelunkersTorch.getNearbyTorchEntities(level, torchPos).stream()
                .filter(t -> t.getDate() < torch.getDate())
                .sorted(torch::distanceComparator)
                .toList();
        if (this.targets.isEmpty()) {
            return;
        }

        SectionPos torchSection = SectionPos.of(torchPos);
        Cursor3D cursor = new Cursor3D(
                torchSection.x() - 1,
                torchSection.y() - 1,
                torchSection.z() - 1,
                torchSection.x() + 1,
                torchSection.y() + 1,
                torchSection.z() + 1);
        int index = 0;
        while (cursor.advance()) {
            this.sections[index++] = PathFindingCache.getSection(level, SectionPos.of(cursor.nextX(), cursor.nextY(), cursor.nextZ()));
        }
    }

    @Nullable
    public Path calculateMinPath() {
        Path bestPath = null;
        double bestDistance = 16.0;
        long bestDate = Long.MAX_VALUE;

        for (TorchEntity target : this.targets) {
            BlockPos targetPos = target.getBlockPos();
            if (targetPos.distSqr(torchPos) > bestDistance * bestDistance) {
                continue;
            }

            Path path = calculatePath(targetPos, bestDistance);
            if (path != null && (path.length < bestDistance || (path.length == bestDistance && target.getDate() < bestDate))) {
                bestDistance = path.length;
                bestDate = target.getDate();
                bestPath = path;
            }
        }

        return bestPath;
    }

    // A* Algorithm
    private Path calculatePath(BlockPos target, double maxDistance) {
        this.openSet.clear();
        this.allNodes.clear();

        Node start = new Node(this.torchPos, null, 0, Math.sqrt(this.torchPos.distSqr(target)));
        this.openSet.add(start);
        this.allNodes.put(this.torchPos, start);

        while (!this.openSet.isEmpty()) {
            Node theNode = this.openSet.poll();
            theNode.visited = true;

            if (theNode.pos.equals(target)) {
                return new Path(this.allNodes, theNode);
            }

            if (theNode.routeDistance + Math.sqrt(theNode.pos.distSqr(target)) >= maxDistance) {
                continue;
            }

            this.calculateNeighbors(theNode.pos);
            for (int i = 0; i < this.neighborCount; ++i) {
                BlockPos neighborPos = this.neighbors[i];
                Node next = this.allNodes.computeIfAbsent(neighborPos, Node::new);
                if (next.visited) {
                    continue;
                }
                double newDistance = theNode.routeDistance + neighborsDistance(theNode.pos, neighborPos);
                if (newDistance < next.routeDistance) {
                    next.previous = theNode.pos;
                    next.routeDistance = newDistance;
                    next.estimatedDistance = newDistance + Math.sqrt(neighborPos.distSqr(target));
                    if (!this.openSet.contains(next)) {
                        this.openSet.add(next);
                    }
                }
            }
        }

        return null;
    }

    private void calculateNeighbors(BlockPos pos) {
        this.neighborCount = 0;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean n = this.maybeAddNeighbor(x    , y    , z - 1);
        boolean s = this.maybeAddNeighbor(x    , y    , z + 1);
        boolean e = this.maybeAddNeighbor(x + 1, y    , z    );
        boolean w = this.maybeAddNeighbor(x - 1, y    , z    );
        boolean a = this.maybeAddNeighbor(x    , y + 1, z    );
        boolean b = this.maybeAddNeighbor(x    , y - 1, z    );

        boolean ne = (n || e) && this.maybeAddNeighbor(x + 1, y    , z - 1);
        boolean nw = (n || w) && this.maybeAddNeighbor(x - 1, y    , z - 1);
        boolean se = (s || e) && this.maybeAddNeighbor(x + 1, y    , z + 1);
        boolean sw = (s || w) && this.maybeAddNeighbor(x - 1, y    , z + 1);
        boolean na = (n || a) && this.maybeAddNeighbor(x    , y + 1, z - 1);
        boolean nb = (n || b) && this.maybeAddNeighbor(x    , y - 1, z - 1);
        boolean sa = (s || a) && this.maybeAddNeighbor(x    , y + 1, z + 1);
        boolean sb = (s || b) && this.maybeAddNeighbor(x    , y - 1, z + 1);
        boolean ea = (e || a) && this.maybeAddNeighbor(x + 1, y + 1, z    );
        boolean eb = (e || b) && this.maybeAddNeighbor(x + 1, y - 1, z    );
        boolean wa = (w || a) && this.maybeAddNeighbor(x - 1, y + 1, z    );
        boolean wb = (w || b) && this.maybeAddNeighbor(x - 1, y - 1, z    );

        if (ne || na || ea) this.maybeAddNeighbor(x + 1, y + 1, z - 1);
        if (nw || na || wa) this.maybeAddNeighbor(x - 1, y + 1, z - 1);
        if (ne || nb || eb) this.maybeAddNeighbor(x + 1, y - 1, z - 1);
        if (nw || nb || wb) this.maybeAddNeighbor(x - 1, y - 1, z - 1);
        if (se || sa || ea) this.maybeAddNeighbor(x + 1, y + 1, z + 1);
        if (sw || sa || wa) this.maybeAddNeighbor(x - 1, y + 1, z + 1);
        if (se || sb || eb) this.maybeAddNeighbor(x + 1, y - 1, z + 1);
        if (sw || sb || wb) this.maybeAddNeighbor(x - 1, y - 1, z + 1);
    }

    private boolean maybeAddNeighbor(int x, int y, int z) {
        int sectionX = (x >> 4) - this.sections[0].getPos().x();
        int sectionY = (y >> 4) - this.sections[0].getPos().y();
        int sectionZ = (z >> 4) - this.sections[0].getPos().z();

        assert(sectionX >= 0);
        assert(sectionY >= 0);
        assert(sectionZ >= 0);
        assert(sectionX <= 2);
        assert(sectionY <= 2);
        assert(sectionZ <= 2);

        PathFindingSection section = this.sections[sectionX + sectionY * 3 + sectionZ * 9];
        if (!section.getBlock(x, y, z)) {
            this.neighbors[this.neighborCount++] = new BlockPos(x, y, z);
            return true;
        }

        return false;
    }

    private static double neighborsDistance(BlockPos p1, BlockPos p2) {
        int deltaX = Math.abs(p1.getX() - p2.getX());
        int deltaY = Math.abs(p1.getY() - p2.getY());
        int deltaZ = Math.abs(p1.getZ() - p2.getZ());
        int delta = deltaX + deltaY + deltaZ;

        assert(delta > 0 && delta <= 3);

        return NEIGHBOR_DISTANCE[delta];
    }
}
