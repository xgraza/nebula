package cope.nebula.client.manager.relation;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Manages all the users relationships
 *
 * @author aesthetical
 * @since 3/27/22
 */
public class RelationshipManager {
    private final ArrayList<Relationship> relationships = new ArrayList<>();

    public void add(Relationship relationship) {
        relationships.add(relationship);
    }

    public void delete(UUID uuid) {
        relationships.removeIf((r) -> r.getUuid().equals(uuid));
    }

    public boolean isFriend(UUID uuid) {
        return relationships.stream().anyMatch((r) -> r.getUuid().equals(uuid));
    }

    public ArrayList<Relationship> getRelationships() {
        return relationships;
    }
}
