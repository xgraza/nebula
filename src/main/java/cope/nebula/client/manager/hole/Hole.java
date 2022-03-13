package cope.nebula.client.manager.hole;

import cope.nebula.util.Globals;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a hole in the ground.
 *
 * Can be a 1x1, 2x1, 2x2, or a void hole
 *
 * @author aesthetical
 * @since 3/8/22
 */
public class Hole {
    private final Set<BlockPos> positions = new HashSet<>();
    private final HoleType holeType;
    private final boolean safe;

    public Hole(HoleType holeType, boolean safe, BlockPos[] allPositions) {
        this.holeType = holeType;
        this.safe = safe;

        positions.addAll(Arrays.asList(allPositions));
    }

    /**
     * Checks if an entity is in a hole
     * @param entity The entity
     * @return If the entities' entity bounding box intersects with any of the positions
     */
    public boolean intersects(Entity entity) {
        for (BlockPos pos : positions) {
            AxisAlignedBB box = new AxisAlignedBB(pos);
            if (!Globals.mc.world.getCollisionBoxes(entity, box).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public HoleType getHoleType() {
        return holeType;
    }

    public boolean isSafe() {
        return safe;
    }

    public Set<BlockPos> getPositions() {
        return positions;
    }
}
