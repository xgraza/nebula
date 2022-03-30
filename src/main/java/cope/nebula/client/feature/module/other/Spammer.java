package cope.nebula.client.feature.module.other;

import cope.nebula.client.feature.module.Module;
import cope.nebula.client.feature.module.ModuleCategory;
import cope.nebula.client.value.Value;
import cope.nebula.util.internal.fs.FileUtil;
import cope.nebula.util.internal.timing.Stopwatch;
import cope.nebula.util.internal.timing.TimeFormat;
import net.minecraft.network.play.client.CPacketChatMessage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Spammer extends Module {
    private static final Path SPAMMER_FILE = FileUtil.NEBULA.resolve("spammer.txt");

    public Spammer() {
        super("Spammer", ModuleCategory.OTHER, "Spams shit");
    }

    public static final Value<Mode> mode = new Value<>("Mode", Mode.LINE);
    public static final Value<Double> delay = new Value<>("Delay", 3.5, 0.0, 10.0);
    public static final Value<Boolean> antiKick = new Value<>("AntiKick", false);
    public static final Value<Boolean> arrow = new Value<>("Arrow", false);

    private final Stopwatch stopwatch = new Stopwatch();
    private int index = -1;
    private List<String> lines;

    @Override
    protected void onActivated() {
        if (nullCheck()) {
            disable();
            return;
        }

        if (!Files.exists(SPAMMER_FILE)) {
            FileUtil.touch(SPAMMER_FILE, "");
            sendChatMessage("Spammer file did not exist, so we created it for you.");
            return;
        }

        lines = FileUtil.readFile(SPAMMER_FILE);
        if (lines == null || lines.isEmpty()) {
            sendChatMessage("Spammer file was empty, disabling...");
            return;
        }
    }

    @Override
    protected void onDeactivated() {
        lines = null;
        index = -1;
    }

    @Override
    public void onTick() {
        if (lines == null || lines.isEmpty()) {
            disable();
            return;
        }

        if (stopwatch.hasElapsed((long) (delay.getValue() * 1000.0), true, TimeFormat.MILLISECONDS)) {
            if (mode.getValue().equals(Mode.RANDOM)) {
                index = ThreadLocalRandom.current().nextInt(0, lines.size());
            } else if (mode.getValue().equals(Mode.LINE)) {
                ++index;
            }

            if (index > lines.size()) {
                index = 0;
            }

            String text = "";
            if (arrow.getValue()) {
                text += "> ";
            }

            try {
                text += lines.get(index);
            } catch (IndexOutOfBoundsException e) {
                text += lines.get(0);
                index = 0;
            }

            if (antiKick.getValue()) {
                text += randomCharacters();
            }

            mc.player.connection.sendPacket(new CPacketChatMessage(text));
        }
    }

    private String randomCharacters() {
        // i couldve used characters and shi, but idrc at this point
        String letters = "abcdefghijklmnopqrstuvwxyz1234567890";
        String text = " [";

        int i = 0;
        while (i <= 4) {
            ++i;
            text += letters.charAt(ThreadLocalRandom.current().nextInt(0, letters.length()));
        }

        return text + "]";
    }

    public enum Mode {
        LINE, RANDOM
    }
}
