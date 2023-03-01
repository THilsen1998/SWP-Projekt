package de.unibremen.swp2.model;

import lombok.*;


/**
 * @Author Martin
 * Participant with Status
 */
public class ParticipantWithStatus extends Participant{

    public ParticipantWithStatus(final @NonNull Participant other, final @NonNull ParticipantStatus status) {
        this.id = other.getId();
        this.setFirstName(other.getFirstName());
        this.setLastName(other.getLastName());
        this.setMatrikelNr(other.getMatrikelNr());
        this.setEmail(other.getEmail());
        this.setStatus(other.getStatus());
        this.setSemester(other.getSemester());
        this.setMeetingStatus(status);
    }

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String id;

    /**
     * Meeting Status
     */
    @Getter
    @Setter
    private ParticipantStatus meetingStatus;
}
