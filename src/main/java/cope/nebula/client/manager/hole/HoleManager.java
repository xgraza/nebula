package cope.nebula.client.manager.hole;

import cope.nebula.util.Globals;
import cope.nebula.util.world.BlockUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Finds and records holes in the ground
 *
 * @author aesthetical
 * @since 3/8/22
 */
public class HoleManager implements Globals {
    private final Map<HoleType, Set<Hole>> holes = new HashMap<>();

    public void onTick() {
        holes.clear();

        // TODO: double hole finding does not work, but that's not a worry right now
        new Thread(() -> BlockUtil.sphere(mc.player.getPosition(), 10).forEach((pos) -> {
            // if we cannot stand in this block position
            if (!BlockUtil.isReplaceable(pos)) {
                return;
            }

            // if we are in the void
            if (pos.getY() <= 0) {
                add(new Hole(HoleType.VOID, false, new BlockPos[] { pos }));
                return;
            }

            int singleSafety = 0;
            // go through all blocks around it via facing
            for (EnumFacing facing : EnumFacing.values()) {
                // ignore upwards facing blocks
                if (facing.equals(EnumFacing.UP)) {
                    continue;
                }

                // get the neighboring position from our origin
                BlockPos neighbor = pos.offset(facing);

                // if the neighbor block will be removed upon an explosion
                if (BlockUtil.isReplaceable(neighbor)) {
                    int doubleSafety = 0;
                    // check for surrounding blocks
                    for (EnumFacing neighborFacing : EnumFacing.values()) {
                        // ignore upward facing again
                        if (neighborFacing.equals(EnumFacing.UP)) {
                            continue;
                        }

                        BlockPos extendedNeighbor = neighbor.offset(neighborFacing);

                        // if we are checking the same position as our neighbor, we can ignore it
                        if (extendedNeighbor.equals(neighbor)) {
                            continue;
                        }

                        // if we can replace this neighbor, it cannot be a double hole, disregard
                        if (BlockUtil.isReplaceable(extendedNeighbor)) {
                            return;
                        }

                        // if the neighboring block can be exploded, this cant be a hole
                        if (BlockUtil.canExplode(extendedNeighbor)) {
                            return;
                        }

                        if (!BlockUtil.canBreak(extendedNeighbor) || !BlockUtil.canBreak(extendedNeighbor)) {
                            ++doubleSafety;
                        }
                    }

                    // if we made it through the loop, we can assume that this is a double hole
                    // add(new Hole(HoleType.DOUBLE, doubleSafety > 3, new BlockPos[] { pos, neighbor }));

                    return;
                } else {
                    if (!BlockUtil.canExplode(neighbor) || !BlockUtil.canBreak(neighbor)) {
                        ++singleSafety;
                    }
                }
            }

            add(new Hole(HoleType.SINGLE, singleSafety == 5, new BlockPos[] { pos }));
        }), "Hole-Searcher").start();
    }

    /**
     * Helper method for adding holes into the map
     * @param hole The hole to add
     */
    private void add(Hole hole) {
        Set<Hole> existingHoles = holes.computeIfAbsent(hole.getHoleType(), (s) -> new HashSet<>());
        existingHoles.add(hole);
        holes.put(hole.getHoleType(), existingHoles);
    }

    /**
     * Gets the holes for this hole type
     * @param type The type of hole to look for
     * @return a set of all the holes of this type. will be empty if none
     */
    public Set<Hole> getHoles(HoleType type) {
        return holes.getOrDefault(type, new HashSet<>());
    }
}
