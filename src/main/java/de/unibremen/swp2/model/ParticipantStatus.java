package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.UUID;

/**
 * @Author Martin
 */
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParticipantStatus implements Serializable {

    @Id
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    @NonNull
    private ParticipantMeetingStatus meetingStatus = ParticipantMeetingStatus.Anwesend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @NonNull
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @NonNull
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Tutorial tutorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private TGroup group;

    @Version
    @Setter(AccessLevel.PRIVATE)
    private Long version;

    public ParticipantStatus() {
    }
}