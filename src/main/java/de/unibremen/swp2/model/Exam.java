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
 * @Author Theo
 * An exam with id, name, location and date. Represents an exam of meeting {@link Meeting}
 * Id is unique amongst exam objects.
 * Two exam objects are considered equal if the id's are equal.
 * Everything except the id can be changed.
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Exam implements Serializable {

    /**
     * Id of Exam (UUID).
     */
    @Id
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * Name of exam.
     */
    @NonNull
    @NotBlank
    private String name = "";

    /**
     * Location of exam.
     */
    private String location;

    /**
     * Date of exam.
     */
    private Date date;

    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal weighting = new BigDecimal("0");

    /**
     * Location of exam.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @NonNull
    private Meeting meeting;

    @Setter(AccessLevel.PRIVATE)
    @Version
    private Long version;


    public Exam() {

    }
}