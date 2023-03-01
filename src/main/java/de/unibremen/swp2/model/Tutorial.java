package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * @Author khaled
 * A Tutorial with id, name, date and info. Represents a tutorial (Tutorium) in a meeting {@link Meeting}.
 * References a meeting. Gets referenced by multiple groups {@link TGroup} multiple users {@link User} and
 * multiple participants {@link Participant}.
 * Everything can be changed except the id. Two tutorials are considered equal if their id's are the same.
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tutorial implements Serializable {

    public Tutorial() {
    }

    /**
     * Id of Tutorial.
     */
    @Id
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * Name of the tutorial.
     */
    @NonNull
    @NotBlank
    private String name = "";

    /**
     * Time of the tutorial.
     */
    private Date date;

    @Setter(AccessLevel.PRIVATE)
    @Version
    private Long version;

    /**
     * Info of the tutorial.
     */
    private String info = "";

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Meeting meeting;

    @ManyToMany(mappedBy = "tutorials", fetch = FetchType.LAZY)
    @Setter(AccessLevel.PRIVATE)
    private Set<UserMeetingRole> userMeetingRoles;

    @OneToMany(mappedBy = "tutorial", fetch = FetchType.LAZY)
    @Setter(AccessLevel.PRIVATE)
    private Set<ParticipantStatus> participantStatuses;

    @OneToMany(mappedBy = "tutorial", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Setter(AccessLevel.PRIVATE)
    private Set<TGroup> groups;

}