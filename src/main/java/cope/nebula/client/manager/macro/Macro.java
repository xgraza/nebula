package cope.nebula.client.manager.macro;

/**
 * Represents a macro object
 *
 * @author aesthetical
 * @since 3/8/22
 */
public class Macro {
    private final String text;
    private final int keyCode;

    public Macro(String text, int keyCode) {
        this.text = text;
        this.keyCode = keyCode;
    }

    public String getText() {
        return text;
    }

    public int getKeyCode() {
        return keyCode;
    }
}
