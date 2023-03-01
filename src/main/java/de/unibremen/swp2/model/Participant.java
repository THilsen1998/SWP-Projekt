package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import javax.swing.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @Author khaled
 * Participant with id, firstName, lastName, matrikelNr, email, status and semester.
 * Not used to log someone in. Represents a participant in a meeting. A participants gets graded participates
 * in meetings and tutorials and can be part of groups.
 * Everything can be changed except the id. Two participants are considered equal if their id's are equal.
 */
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Participant implements Serializable {

    /**
     * Id of participant (UUID).
     */
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @Id
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * First name of the participant.
     */
    @NonNull
    @NotBlank
    private String firstName = "";

    /**
     * Last name of the participant.
     */
    @NonNull
    @NotBlank
    private String lastName = "";

    /**
     * Matrikel number of the participant.
     */
    private Integer matrikelNr;

    /**
     * Email of the participant
     */
    @NonNull
    @Email
    private String email = "";

    /**
     * Status of the participant. (Left uni)
     */
    @NotBlank
    @NonNull
    private String status = "Immatrikuliert";

    /**
     * Current semester of the participant.
     */
    private Integer semester;

    /**
     * Version of the Participant for Optimistic Lock.
     * Managed by Hibernate
     */
    @Version
    @Setter(AccessLevel.PRIVATE)
    private Long version;

    @OneToMany(mappedBy = "participant", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Setter(AccessLevel.PRIVATE)
    private Set<ParticipantStatus> participantStatuses;
}
