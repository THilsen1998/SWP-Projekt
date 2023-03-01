package de.unibremen.swp2.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @Author Dennis
 * A Task with id, name, number, date weighting, points and criteria.
 * Represents the tasks in a submission {@link Submission}.
 * A task can reference another task meaning this task would then be a subtask.
 * Everything except the id can be changed.
 * Two task objects are considered equal if their id's are the same.
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task implements Serializable , Comparable<Task> {

    /**
     * Id of the Task (UUID).
     */
    @Id
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

    /**
     * Name of the Task
     */
    @NonNull
    private String name = "";

    /**
     * Number of the task. 1.1 for example if subtask of number 1.
     */
    @NonNull
    private Double number = 0.0;

    /**
     * Submission date of the task.
     */
    private Date date;

    /**
     * Weighting of the task.
     */
    @NonNull
    private BigDecimal weighting = new BigDecimal("1");

    /**
     * Max achievable points of the task.
     */
    @NonNull
    private BigDecimal points = new BigDecimal("0");

    /**
     * Criteria of the task.
     */
    private String criteria = "";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn
    private Submission submission;

    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Task> tasks = new HashSet<>();

    public Task() {

    }

    public Task(Task other) {
        this.id = other.getId();
        this.weighting = new BigDecimal(String.valueOf(other.getWeighting()));
        this.points = new BigDecimal(String.valueOf(other.getPoints()));
        this.number = other.getNumber();
        this.name = other.getName();
        this.date = other.getDate();
        this.criteria = other.getCriteria();
        this.task = other.getTask();
        this.tasks = other.getTasks();
        this.submission = other.getSubmission();
    }

    @Override
    public int compareTo(Task o) {
        return number.compareTo(o.number);
    }
}
