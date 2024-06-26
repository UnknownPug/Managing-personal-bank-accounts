package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

/**
 * This enum represents the roles a user can have in the system.
 * It contains the roles: ROLE_USER, ROLE_ADMIN, and ROLE_MODERATOR.
 */
public enum UserRole {
    /**
     * Represents the role of a regular user.
     */
    ROLE_USER("ROLE_USER"),

    /**
     * Represents the role of an admin user.
     */
    ROLE_ADMIN("ROLE_ADMIN"),

    /**
     * Represents the role of a moderator user.
     */
    ROLE_MODERATOR("ROLE_MODERATOR");

    /**
     * The role of the user.
     */
    private final String role;

    /**
     * Constructor for the UserRole enum.
     *
     * @param role The role of the user.
     */
    UserRole(String role) {
        this.role = role;
    }

    /**
     * Returns the role of the user as a string.
     *
     * @return The role of the user.
     */
    @Override
    public String toString() {
        return role;
    }
}
