package de.unibremen.swp2.service;


import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.InvalidGradeException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Exceptions.PersistenceException;
import de.unibremen.swp2.persistence.Interceptors.*;
import org.primefaces.model.TreeNode;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.swing.*;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @Author Tommy, Martin
 * Allows to edit,update,delete,creat and other operations on an Evaluation
 * (stored in {@link #evaluationDAO}).
 */
@Transactional(rollbackOn = PersistenceException.class)
@RequestScoped
public class EvaluationService implements Serializable {


    @Inject
    private EvaluationDAO evaluationDAO;

    @Inject
    private SubmissionDAO submissionDAO;

    @Inject
    private ParticipantDAO participantDAO;

    @Inject
    private GroupDAO groupDAO;

    @Inject
    private UserDAO userDAO;

    /**
     * create the given evaluation.
     *
     * @param evaluation Evaluation to create.
     */
    @LockEvaluationDAO
    @LockSubmissionDAO
    @LockParticipantDAO
    @LockTaskDAO
    @LockMeetingDAO
    public void createParticipantEvaluation(Evaluation evaluation, TreeNode node) throws OutdatedException,
            EntityNotFoundException {
        if (evaluation.getVersion() != null) {
            evaluationDAO.update(evaluation);
        } else {
            evaluationDAO.insert(evaluation);
        }
        createEvaluationHelper(node.getChildren());
    }

    /**
     * Creates the evaluation to a group.
     * @param evaluation Evaluation to the submission.
     * @param node root of a Tree
     * @param group Group to evaluate
     * @throws OutdatedException
     * @throws EntityNotFoundException
     */
    @LockMeetingDAO
    @LockEvaluationDAO
    @LockParticipantDAO
    @LockSubmissionDAO
    @LockTaskDAO
    public void createGroupEvaluation(Evaluation evaluation, TreeNode node, TGroup group) throws OutdatedException,
            EntityNotFoundException {

        List<Participant> participantsInGroup = participantDAO.getParticipantsByGroup(group);

        Evaluation newEvaluation;

        for (Participant p : participantsInGroup) {

            ParticipantStatus participantStatus;
            try {
                participantStatus = participantDAO.getParticipantStatusByParticipantAndGroup(p, group);

            } catch (NoResultException e) {
                throw new EntityNotFoundException();
            }


            try {
                Evaluation currentEvaluation = evaluationDAO.getEvaluationBySubmissionAndParticipantStatus(evaluation.getSubmission(),
                        participantStatus);

                currentEvaluation.setComment(evaluation.getComment());
                currentEvaluation.setPoints(evaluation.getPoints());

                evaluationDAO.update(currentEvaluation);

                createEvaluationHelperForGroup(node.getChildren(), participantStatus);

            } catch (NoResultException e) {
                newEvaluation = new Evaluation();

                newEvaluation.setSubmission(evaluation.getSubmission());
                newEvaluation.setPoints(evaluation.getPoints());
                newEvaluation.setComment(evaluation.getComment());
                newEvaluation.setDate(evaluation.getDate());
                newEvaluation.setParticipantStatus(participantStatus);


                evaluationDAO.insert(newEvaluation);


                createEvaluationHelperForGroup(node.getChildren(), participantStatus);
            }

        }

    }

    /**
     * Helper to create evaluations to a group.
     * @param nodes nodes of a tree
     * @param participantStatus status of participant regarding the meeting.
     * @throws OutdatedException
     * @throws EntityNotFoundException
     */
    private void createEvaluationHelperForGroup(List<TreeNode> nodes, ParticipantStatus participantStatus) throws OutdatedException, EntityNotFoundException {

        Evaluation newEvaluation;

        for (TreeNode n : nodes) {

            try {
                Evaluation evaluation = ((EvaluationToTask) n.getData()).getEvaluation();

                Evaluation currentEvaluation = evaluationDAO.getEvaluationByTaskAndParticipant(evaluation.getTask(),
                        participantStatus);

                currentEvaluation.setComment(evaluation.getComment());
                currentEvaluation.setPoints(evaluation.getPoints());

                evaluationDAO.update(currentEvaluation);

                if (n.getChildCount() != 0) {
                    createEvaluationHelperForGroup(n.getChildren(), participantStatus);
                }


            } catch (NoResultException e) {
                Evaluation evaluation = ((EvaluationToTask) n.getData()).getEvaluation();

                newEvaluation = new Evaluation();

                newEvaluation.setSubmission(evaluation.getSubmission());
                newEvaluation.setPoints(evaluation.getPoints());
                newEvaluation.setTask(evaluation.getTask());
                newEvaluation.setComment(evaluation.getComment());

                newEvaluation.setParticipantStatus(participantStatus);

                evaluationDAO.insert(newEvaluation);


                if (n.getChildCount() != 0) {
                    createEvaluationHelperForGroup(n.getChildren(), participantStatus);
                }
            }

        }

    }

    /**
     * Helper to create a evaluation of a participant.
     * @param nodes
     * @throws OutdatedException
     * @throws EntityNotFoundException
     */
    private void createEvaluationHelper(List<TreeNode> nodes) throws OutdatedException, EntityNotFoundException {
        for (TreeNode n : nodes) {
            Evaluation evaluation = ((EvaluationToTask) n.getData()).getEvaluation();
            if (evaluation.getVersion() != null) {
                evaluationDAO.update(evaluation);
            } else {
                evaluationDAO.insert(evaluation);
            }
            if (n.getChildCount() != 0) {
                createEvaluationHelper(n.getChildren());
            }
        }
    }

    /**
     * creates the evaluation to an exam of a participant.
     * @param evaluation the evaluation of the exam.
     * @throws OutdatedException
     * @throws EntityNotFoundException
     * @throws InvalidGradeException
     */
    @LockMeetingDAO
    @LockParticipantDAO
    @LockExamDAO
    @LockEvaluationDAO
    public void createExamEvaluation(final Evaluation evaluation) throws OutdatedException, EntityNotFoundException, InvalidGradeException {
        if (evaluation.getGrade().compareTo(new BigDecimal("5")) <= 0 && evaluation.getGrade().compareTo(BigDecimal.ZERO) > 0) {
            if (evaluation.getVersion() == null) {
                evaluationDAO.insert(evaluation);
            } else {
                evaluationDAO.update(evaluation);
            }
        } else {
            throw new InvalidGradeException();
        }
    }

    /**
     * Updates the given evaluation.
     *
     * @param evaluation Evaluation to update.
     * @throws NullPointerException If {@code evaluation} is {@code null}.
     */
    public void update(Evaluation evaluation) {
    }

    /**
     * Deletes the given evaluation. Does not fail if {@code evaluation} is {@code null}
     * or unknown.
     *
     * @param evaluation Evaluation to delete.
     */
    @LockMeetingDAO
    @LockEvaluationDAO
    public void delete(final Evaluation evaluation) throws EntityNotFoundException {
        evaluationDAO.delete(evaluation);
    }

    /**
     * Returns the Evaluation with given id.
     *
     * @param id Id of the evaluation in question.
     * @return Evaluation  with given id.
     */
    public Evaluation getById(final String id) {
        return evaluationDAO.getById(id);
    }

    /**
     * Returns the evaluation with given submission and participant.
     *
     * @param submission Submission of evaluation in question.
     * @return Evaluation with given submission and participant.
     */
    public Evaluation getEvaluationBySubmissionsAndParticipant(final Submission submission, final ParticipantStatus participantStatus) {
        try {
            return evaluationDAO.getEvaluationBySubmissionAndParticipantStatus(submission, participantStatus);
        } catch (NoResultException e) {
            Evaluation evaluation = new Evaluation();
            evaluation.setParticipantStatus(participantStatus);
            evaluation.setSubmission(submission);
            return evaluation;
        }
    }

    /**
     * Returns the evaluation with given exam and participant.
     *
     * @param exam Exam of evaluation in question.
     *             Participant of evaluation in question.
     * @return Evaluation with given exam and participant.
     */
    public List<EvaluationToExam> getEvaluationsByExamAndParticipants(Exam exam, List<ParticipantStatus> participantStatuses) {
        final List<EvaluationToExam> list = new ArrayList<>();
        for (ParticipantStatus p : participantStatuses) {
            try {
                Evaluation evaluation = evaluationDAO.getEvaluationByExamAndParticipant(exam, p);
                Participant participant = participantDAO.getById(p.getParticipant().getId());
                User user = userDAO.getById(evaluation.getUser().getId());
                EvaluationToExam evaluationToExam = new EvaluationToExam(evaluation, participant, user);
                list.add(evaluationToExam);
            } catch (NoResultException e) {
                Evaluation evaluation = new Evaluation();
                evaluation.setParticipantStatus(p);
                evaluation.setExam(exam);
                Participant participant = participantDAO.getById(p.getParticipant().getId());
                EvaluationToExam evaluationToExam = new EvaluationToExam(evaluation, participant, null);
                list.add(evaluationToExam);
            }
        }
        return list;
    }

    /**
     * Creates a new Evaluation to an exam of a participant.
     * @param exam the given exam.
     * @param status the status of the participant.
     * @return Evaluation of exam.
     */
    public Evaluation createNewEvaluationByExamAndParticipants(final Exam exam, final ParticipantStatus status) {
        Evaluation evaluation = new Evaluation();
        evaluation.setExam(exam);
        evaluation.setParticipantStatus(status);
        return evaluation;
    }

    /**
     * Gets the list of evaluations linked to tasks of a participant of a submission.
     * @param submission submission which holds the tasks
     * @param participant participant who gets evaluated
     * @return List of evaluations linked to tasks
     * @throws EntityNotFoundException
     */
    public List<EvaluationToTask> getEvaluationToTaskBySubmissionAndParticipantStatus(final Submission submission, final ParticipantStatus participant) throws EntityNotFoundException {
        final List<EvaluationToTask> list = new ArrayList<>();
        Submission currentSubmission = submissionDAO.getById(submission.getId());
        if (currentSubmission != null) {
            currentSubmission.getTasks().size();
            createEvaluationToTaskList(list, currentSubmission.getTasks(), participant, null);
        } else {
            throw new EntityNotFoundException();
        }
        return list;
    }

    /**
     * Helper to create the evaluations linked to tasks
     * @param list list to be edited
     * @param tasks All tasks of a submission.
     * @param participant participant to evaluate
     * @param parent holds the parentTask
     */
    private void createEvaluationToTaskList(final List<EvaluationToTask> list, final Set<Task> tasks, final ParticipantStatus participant, final EvaluationToTask parent) {
        List<Task> sortedTasks = new ArrayList<>(tasks);
        Collections.sort(sortedTasks);
        EvaluationToTask evaluationToTask;
        for (Task t : sortedTasks) {
            try {
                Evaluation evaluation = evaluationDAO.getEvaluationByTaskAndParticipant(t, participant);
                evaluationToTask = new EvaluationToTask(t, evaluation);
                if (t.getSubmission() != null) {
                    evaluationToTask.setSubmission(t.getSubmission());
                } else {
                    evaluationToTask.setEvaluationToTask(parent);
                }
                list.add(evaluationToTask);
            } catch (NoResultException e) {
                Evaluation evaluation = new Evaluation();
                evaluation.setParticipantStatus(participant);
                evaluation.setTask(t);
                evaluationToTask = new EvaluationToTask(t, evaluation);
                if (t.getSubmission() != null) {
                    evaluationToTask.setSubmission(t.getSubmission());
                } else {
                    evaluationToTask.setEvaluationToTask(parent);
                }
                list.add(evaluationToTask);
            }
            if (t.getTasks().size() != 0) {
                createEvaluationToTaskList(evaluationToTask.getEvaluationToTasks(), t.getTasks(), participant, evaluationToTask);
            }
        }
    }

    /**
     * Gets all evaluations by a participant.
     * @param participant the participant.
     * @param meeting the meeting.
     * @return list of evaluations.
     */
    public List<Evaluation> getEvaluationsByParticipantAndMeeting(final Participant participant, final Meeting meeting) {
        return evaluationDAO.getEvaluationsByParticipantAndMeeting(participant, meeting);
    }

    /**
     * Gets the evaluation of an exam to a participant by the participantStatus.
     * @param exam the exam
     * @param status the participant
     * @return The evaluation to the exam.
     * @throws NoResultException
     */
    public Evaluation getEvaluationByExamAndParticipant(final Exam exam, ParticipantStatus status) throws NoResultException {
        return evaluationDAO.getEvaluationByExamAndParticipant(exam, status);
    }

    /**
     * Gets the evaluation to an exam to a participant by the participant.
     * @param exam the exam
     * @param participant the participant
     * @return The evaluation to the exam.
     * @throws NoResultException
     */
    public Evaluation getExamEvaluation(final Exam exam, final Participant participant) throws NoResultException {
        return evaluationDAO.getExamEvaluation(exam, participant);
    }

    /**
     * Deletes all evaluations to a submission of a participant.
     * @param submission the submission
     * @param status the status of the participant to a meeting.
     * @throws OutdatedException
     * @throws EntityNotFoundException
     */
    @LockMeetingDAO
    @LockEvaluationDAO
    public void deleteAllEvaluationsOfParticipantOfSubmission(final Submission submission,final ParticipantStatus status) throws OutdatedException, EntityNotFoundException {
        final Submission current = submissionDAO.getById(submission.getId());
        if (current == null) {
            throw new OutdatedException();
        }
        current.getTasks().size();
        try {
            Evaluation evaluation = evaluationDAO.getEvaluationBySubmissionAndParticipantStatus(submission, status);
            evaluationDAO.delete(evaluation);
        } catch (NoResultException ignored) {}
        for (Task t : current.getTasks()) {
            try {
                Evaluation evaluation = evaluationDAO.getEvaluationByTaskAndParticipant(t, status);
                evaluationDAO.delete(evaluation);
            } catch (NoResultException ignored) {}
        }
    }

    /**
     * Deletes the evaluation to a given task of a complete group.
     * @param task the given task.
     * @param group the group.
     * @throws OutdatedException
     */
    @LockMeetingDAO
    @LockEvaluationDAO
    public void deleteEvaluationToTaskOfGroup(final Task task, final TGroup group) throws OutdatedException {
        final TGroup current = groupDAO.getById(group.getId());
        if (current == null) {
            throw new OutdatedException();
        }
        current.getParticipants().size();
        for (ParticipantStatus s : current.getParticipants()) {
            try {
                Evaluation evaluation = evaluationDAO.getEvaluationByTaskAndParticipant(task, s);
                evaluationDAO.delete(evaluation);
            } catch (NoResultException | EntityNotFoundException ignored) {}
        }
    }

    /**
     * Deletes all evaluations to a submission of a group.
     * @param group the group
     * @param submission the submission
     * @throws EntityNotFoundException
     * @throws OutdatedException
     */
    public void deleteAllEvaluationsOfGroupOfSubmission(TGroup group, Submission submission) throws EntityNotFoundException, OutdatedException {
        final TGroup currentGroup = groupDAO.getById(group.getId());
        if (currentGroup == null) {
            throw new EntityNotFoundException();
        }
        currentGroup.getParticipants().size();
        for (ParticipantStatus s : currentGroup.getParticipants()) {
            this.deleteAllEvaluationsOfParticipantOfSubmission(submission, s);
        }
    }
}