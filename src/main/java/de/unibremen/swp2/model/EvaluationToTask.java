package de.unibremen.swp2.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author martin
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EvaluationToTask {

    public EvaluationToTask(Task task, Evaluation evaluation) {
        this.task = task;
        this.evaluation = evaluation;
    }

    public EvaluationToTask() {

    }

    @Getter
    private Task task;

    @Getter
    @Setter
    @EqualsAndHashCode.Include
    private Evaluation evaluation;

    @Getter
    private List<EvaluationToTask> evaluationToTasks = new ArrayList<>();

    @Getter
    @Setter
    private EvaluationToTask evaluationToTask;

    @Getter
    @Setter
    private Submission submission;
}
