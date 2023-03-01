package de.unibremen.swp2.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author martin
 */
public class EvaluationToExam {

    public EvaluationToExam(Evaluation evaluation, Participant participant, User user) {
        this.evaluation = evaluation;
        this.participant = participant;
        this.user = user;
    }

    @Getter
    @Setter
    private Evaluation evaluation;

    @Getter
    private Participant participant;

    @Setter
    @Getter
    private User user;
}
