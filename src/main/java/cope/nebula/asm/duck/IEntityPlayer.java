package cope.nebula.asm.duck;

public interface IEntityPlayer {
    /**
     * @return if this player is already a friend or not
     */
    boolean isFriend();

    /**
     * Friends or unfriends this player
     * @return true if they were added, or false if they were unadded
     */
    boolean friend();
}
