package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * @Author Martin
 * An evaluation with id, grade, comment and date. Represents the grade of a submission, a single task or an exam.
 * An evaluation can be in relation to a submission {@link Submission}, a task {@link Task} or an exam {@link Exam}
 * but never to multiple of these at the same time. Used to grade either of the above mentioned.
 * Additionally always in relation with a participant {@link Participant}. Can be in relation with
 * a user {@link User} (when in relation with submission or exam).
 * Two evaluations are considered equal, if their id is the same. Everything except the id can be changed.
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Evaluation implements Serializable {

    public Evaluation() {}

    public Evaluation(Evaluation evaluation) {
        this.id = evaluation.id;
        this.task = evaluation.task;
        this.comment = evaluation.comment;
        this.date = evaluation.date;
        this.points = evaluation.points;
        this.grade = evaluation.getGrade();
        this.participantStatus = evaluation.participantStatus;
        this.submission = evaluation.submission;
        this.version = evaluation.version;
        this.user = evaluation.getUser();
        this.exam = evaluation.getExam();
    }

    /**
     * Id of evaluation (UUID).
     */
    @Setter(AccessLevel.PRIVATE)
    @Id
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * Grade of the evaluation.
     */
    @NonNull
    private BigDecimal points = new BigDecimal("0");

    @NonNull
    @DecimalMin("0")
    @DecimalMax("5")
    private BigDecimal grade = new BigDecimal("0");

    /**
     * Comment of the evaluation.
     */
    @NonNull
    private String comment = "";

    /**
     * Date of the evaluation.
     */
    private Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticipantStatus participantStatus;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn
    private Submission submission;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Exam exam;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Task task;

    @Version
    @Setter(AccessLevel.PRIVATE)
    private Long version;

}
