package cope.nebula.client.feature.module;

/**
 * Modules can be categorized for organization
 *
 * @author aesthetical
 * @since 3/7/22
 */
public enum ModuleCategory {
    COMBAT("Combat"),
    EXPLOIT("Exploit"),
    MOVEMENT("Movement"),
    OTHER("Other"),
    RENDER("Render"),
    WORLD("World");

    private final String displayName;

    ModuleCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return A human-readable version of this enum constant
     */
    public String getDisplayName() {
        return displayName;
    }
}
