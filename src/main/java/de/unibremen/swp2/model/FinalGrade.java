package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * @Author martin
 * A final grade with id, overallGrade and passed.
 * Represents the final grade of a user {@link User} in a meeting {@link Meeting}.
 * The id is unique amongst finalGrade objects.
 * Two finalGrade objects are considered the same if the id is the same.
 * Everything can be changed except the id.
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FinalGrade implements Serializable {

    /**
     * Id of finalGrade (UUID).
     */
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @Id
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * The final grade of a User.
     */
    @NonNull
    private BigDecimal overallGrade = new BigDecimal("0");

    /**
     * Shows if the user has passed the meeting.
     */
    private Boolean passed;

    @NonNull
    private BigDecimal submissionGrade = new BigDecimal("0");

    @ManyToOne(fetch = FetchType.LAZY)
    @NonNull
    @JoinColumn
    private Meeting meeting;

    @OneToOne(fetch = FetchType.LAZY)
    @NonNull
    @JoinColumn
    private ParticipantStatus participantStatus;

    @Setter(AccessLevel.PRIVATE)
    @Version
    private Long version;

    public FinalGrade() {

    }
}
