package wtf.nebula.asm.hooks;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import wtf.nebula.client.Nebula;
import wtf.nebula.client.event.world.ClickBlockEvent;

public class PlayerControllerMPHook {
    public static boolean clickBlock(BlockPos pos, EnumFacing facing) {
        ClickBlockEvent event = new ClickBlockEvent(pos, facing);
        Nebula.Companion.getBUS().post(event);
        return event.getCancelled();
    }
}
