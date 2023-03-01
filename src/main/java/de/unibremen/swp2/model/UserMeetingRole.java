package de.unibremen.swp2.model;

import lombok.*;

import javax.ejb.Lock;
import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class UserMeetingRole implements Serializable {

    @Setter(AccessLevel.PRIVATE)
    @Id
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    @NonNull
    private Role role = Role.T;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @NonNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @NonNull
    private Meeting meeting;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable
    @Setter(AccessLevel.PRIVATE)
    private Set<Tutorial> tutorials = new HashSet<>();

    @Version
    @Setter(AccessLevel.PRIVATE)
    private Long version;

    public UserMeetingRole() {}
}