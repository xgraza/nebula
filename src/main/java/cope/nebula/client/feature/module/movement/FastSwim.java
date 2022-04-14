package cope.nebula.client.feature.module.movement;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.math.Vec2d;
import cope.nebula.util.world.entity.player.MotionUtil;

public class FastSwim extends Module {
    public FastSwim() {
        super("FastSwim", ModuleCategory.MOVEMENT, "move fast liquid");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.MOTION);
    public static final Value<Double> speed = new Value<>("Speed", 0.2, 0.1, 2.0);

    @Override
    public void onTick() {
       if (mc.player.isInWater() || mc.player.isInLava()) {
           double motionSpeed = speed.getValue();
           if (mode.getValue().equals(Mode.STRICT)) {
               // doesnt work lol TODO
               motionSpeed = MotionUtil.getBaseNCPSpeed();
           }

           Vec2d vec = MotionUtil.strafe(motionSpeed);

           mc.player.motionX = vec.getX();
           mc.player.motionZ = vec.getZ();

           if (mode.getValue().equals(Mode.MOTION)) {
               if (mc.gameSettings.keyBindJump.isKeyDown()) {
                   mc.player.motionY = speed.getValue();
               } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                   mc.player.motionY = -speed.getValue();
               }
           }
       }
    }

    public enum Mode {
        /**
         * Uses a custom speed
         */
        MOTION,

        /**
         * Uses the maximum speed you can go on NCP-Updated
         */
        STRICT
    }
}
