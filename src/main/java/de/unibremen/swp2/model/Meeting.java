package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * @Author Tommy
 * A meeting (Veranstaltung) with id, name, visible parameter, wiSo and year.
 * Represents a meeting where a participant is part of, with tutorials etc.
 * Everything can be changed except the id.
 * Two meeting objects are considered equal if their id's are equal.
 */
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Meeting implements Serializable {

    /**
     * Id of meeting (UUID).
     */
    @Id
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * Name of meeting.
     */
    @NonNull
    @NotBlank
    private String name = "";

    /**
     * If the meeting is visible or not.
     */
    @NonNull
    private Boolean visible = true;

    /**
     * Winter or summer semester.
     */
    @NonNull
    private Boolean wiSo = true;

    /**
     * Year in which the meeting takes place.
     */
    private Date year;

    @NonNull
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal submissionWeighting = new BigDecimal("0");

    /*
   Notifys if a Participant musst be in a Group
   true == participant can only join a new Group
   flase == participant can not be in any Group
   */
    @NonNull
    private Boolean onlyGroupSplit = true;

    @Setter(AccessLevel.PRIVATE)
    @Version
    private Long version;

    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Setter(AccessLevel.PRIVATE)
    private Set<UserMeetingRole> userMeetingRoles;

    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Setter(AccessLevel.PRIVATE)
    private Set<ParticipantStatus> participantStatuses;

    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Setter(AccessLevel.PRIVATE)
    private Set<Tutorial> tutorials;

    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Setter(AccessLevel.PRIVATE)
    private Set<Submission> submissions;

    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Setter(AccessLevel.PRIVATE)
    private Set<Exam> exams;

}
