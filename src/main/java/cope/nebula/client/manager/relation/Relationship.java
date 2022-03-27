package cope.nebula.client.manager.relation;

import java.util.UUID;

/**
 * Represents a relationship with a certain user
 *
 * @author aesthetical
 * @since 3/27/22
 */
public class Relationship {
    private final UUID uuid;
    private final String alias;

    public Relationship(UUID uuid, String alias) {
        this.uuid = uuid;
        this.alias = alias;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getAlias() {
        return alias;
    }
}
