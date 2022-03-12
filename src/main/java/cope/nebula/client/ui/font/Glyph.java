package cope.nebula.client.ui.font;

public class Glyph {
    private final float x, y, width, height;
    private final char c;

    public Glyph(float x, float y, float width, float height, char c) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.c = c;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public char getC() {
        return c;
    }
}
