package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @Author Martin
 * A Submission with id, name and a date. Represents a submission a user with role {@link Role#D} in the
 * respective meeting can create. Further data is saved in the Tasks {@link Task}.
 * The tasks have a relation to a submission.
 * Everything except the id can be changed.
 * Two submission objects are considered the same if their id's are equal.
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Submission implements Serializable {

    @Getter
    @Setter(AccessLevel.PRIVATE)
    @Id
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * Name of the submission
     */
    @NotBlank
    @NonNull
    private String name = "";

    /**
     * Submission date.
     */
    private Date date;

    @NonNull
    @DecimalMin("0")
    private BigDecimal weighting = new BigDecimal("1");

    @NonNull
    private Boolean groupWork = true;

    /**
     * Submission's max grade.
     */
    @NonNull
    private BigDecimal maxGrade = new BigDecimal("0");

    private Long version = 0L;

    @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @Setter(AccessLevel.PRIVATE)
    private Set<Task> tasks = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @NonNull
    private Meeting meeting;

    public Submission()
    {

    }

}
