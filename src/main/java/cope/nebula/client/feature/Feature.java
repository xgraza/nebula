package cope.nebula.client.feature;

import cope.nebula.util.Globals;

/**
 * Represents a very basic form of a feature
 *
 * @author aesthetical
 * @since 3/7/22
 */
public class Feature implements Globals {
    private final String name;

    public Feature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
