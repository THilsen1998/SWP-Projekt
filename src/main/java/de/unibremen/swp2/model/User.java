package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * @Author Theo, Martin
 * A user with id, first name, last Name, email, password, status and role.
 * Id and email are unique amongst user objects. Everything except the id can be changed.
 * A User Object is used to log in a user.
 * Two user objectc are considered equal when their id is the same. Two users can't have the same email.
 * The default role is {@link Role#T}.
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements Serializable {

    public User(User user) {
        this.id = user.id;
        this.firstName = user.firstName;
        this.lastName = user.lastName;
    }

    /**
     * Id of user (UUID).
     */
    @Setter(AccessLevel.PRIVATE)
    @Id
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * First name of user.
     */
    @NonNull
    @NotBlank
    private String firstName = "";

    /**
     * Last name of user.
     */
    @NonNull
    @NotBlank
    private String lastName = "";

    /**
     * Email of user.
     */
    @NonNull
    @NotBlank
    @Email
    @Column(unique = true)
    private String email = "";

    /**
     * Password of user.
     */
    @NotBlank
    @NonNull
    private String password = "";

    /**
     * Status of user. (active/not active)
     */
    @NonNull
    private Boolean status = true;

    /**
     * Role of user.
     */
    @NonNull
    private Role role = Role.T;

    @Version
    @Setter(AccessLevel.PRIVATE)
    private Long version;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Setter(AccessLevel.PRIVATE)
    private Set<UserMeetingRole> userMeetingRoles;

    public User() {

    }
}