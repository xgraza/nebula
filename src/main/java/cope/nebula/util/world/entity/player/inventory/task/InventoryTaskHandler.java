package cope.nebula.util.world.entity.player.inventory.task;

import cope.nebula.client.events.PacketEvent;
import cope.nebula.util.Globals;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A helpful class to quickly run through inventory tasks
 *
 * When using this, you must register an instance of InventoryTaskHandler to the FML event bus
 * This is so it can confirm actions that were preformed
 *
 * @author aesthetical
 * @since 3/10/22
 */
public class InventoryTaskHandler implements Globals {
    private final Queue<InventoryTask> pendingActions = new ConcurrentLinkedQueue<>();
    private final Stopwatch stopwatch = new Stopwatch();

    private short transactionId = -1;

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        switch (event.getDirection()) {
            case INCOMING: {
                if (event.getPacket() instanceof SPacketConfirmTransaction) {
                    SPacketConfirmTransaction packet = (SPacketConfirmTransaction) event.getPacket();

                    if (packet.wasAccepted() && packet.getActionNumber() == transactionId) {
                        transactionId = -1;
                    }
                }
                break;
            }

            case OUTGOING: {
                if (event.getPacket() instanceof CPacketClickWindow) {
                    transactionId = ((CPacketClickWindow) event.getPacket()).getActionNumber();
                }
                break;
            }
        }
    }

    /**
     * Cleans up the inventory task handler
     */
    public void clean() {
        transactionId = -1;
        pendingActions.clear();
    }

    /**
     * Adds an inventory task
     * @param task the task
     */
    public void addTask(InventoryTask task) {
        pendingActions.add(task);
    }

    /**
     * Executes inventory tasks
     * @param waitTime The time to wait in between sending tasks
     * @param await If to wait for the window click packet to be accepted by the server before doing another task
     * @return if execution of other things should be halted
     */
    public boolean execute(long waitTime, boolean await) {
        if (transactionId == -1 || !await) {
            if (stopwatch.hasElapsed(waitTime, true, TimeFormat.MILLISECONDS)) {
                InventoryTask task = pendingActions.poll();
                if (task == null) {
                    transactionId = -1;
                    return true;
                }

                task.execute();
                return false;
            }
        } else {
            // TODO some sort of handing in case the task was not accepted
        }

        return false;
    }
}
