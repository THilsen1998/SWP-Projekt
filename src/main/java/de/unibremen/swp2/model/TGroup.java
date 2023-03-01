package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @Author Navy
 * A group with id, name, number and maxPa. Represents a group of participants {@link Participant}
 * in a Tutorial {@link Tutorial}.
 * Everything can be changed except the id.
 * Two group objects are considered equal if the id's are equal.
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TGroup implements Serializable {

    /**
     * Id of group (UUID).
     */
    @Id
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * Name of group.
     */
    @NotBlank
    @NonNull
    private String name = "";

    /**
     * Number of group.
     */
    @NonNull
    private Integer number = 0;

    /**
     * Maximum number of participants {@link Participant} in a group.
     */
    @NonNull
    private Integer maxPa = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Tutorial tutorial;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    @Setter(AccessLevel.PRIVATE)
    private Set<ParticipantStatus> participants;

    @Setter(AccessLevel.PRIVATE)
    @Version
    private Long version;

    public TGroup() {

    }
}
