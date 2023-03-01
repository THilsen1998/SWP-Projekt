package de.unibremen.swp2.model;

import lombok.Getter;

public class UserWithRole extends User {

    public UserWithRole(final User user, final UserMeetingRole role) {
        this.id = user.getId();
        this.setEmail(user.getEmail());
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
        this.meetingRole = role.getRole();
    }

    @Getter
    private final String id;

    @Getter
    private final Role meetingRole;

}
