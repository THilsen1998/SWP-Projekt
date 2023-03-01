package de.unibremen.swp2.persistence;

import de.unibremen.swp2.model.*;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;
import de.unibremen.swp2.persistence.Interceptors.*;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.persistence.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;



/**@Author Martin
 */
@RequestScoped
public class EvaluationDAO implements DAO<Evaluation> {

    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    private static final ReentrantLock reentrantLock = new ReentrantLock();

    public void lock() {
        reentrantLock.lock();
    }

    public void unlock() {
        reentrantLock.unlock();
    }

    /**
     * Inserts the given evaluation.
     *
     * @param evaluation Evaluation to insert.
     * @throws NullPointerException If {@code evaluation} is {@code null}.
     */
    public void insert(final Evaluation evaluation) throws NullPointerException, EntityNotFoundException,
            OutdatedException {
        if (evaluation.getSubmission() != null) {
            try {
                this.getEvaluationBySubmissionAndParticipantStatus(evaluation.getSubmission(),
                        evaluation.getParticipantStatus());
                throw new OutdatedException();
            } catch (NoResultException e) {
                final Submission submission = entityManager.find(Submission.class, evaluation.getSubmission().getId()
                        , LockModeType.PESSIMISTIC_WRITE);
                final ParticipantStatus status = entityManager.find(ParticipantStatus.class,
                        evaluation.getParticipantStatus().getId(), LockModeType.PESSIMISTIC_WRITE);
                if (submission != null && status != null) {
                    entityManager.persist(evaluation);
                } else {
                    throw new EntityNotFoundException();
                }
            }
        } else if (evaluation.getTask() != null) {
            try {
                this.getEvaluationByTaskAndParticipant(evaluation.getTask(), evaluation.getParticipantStatus());
                throw new OutdatedException();
            } catch (NoResultException e) {
                final Task task = entityManager.find(Task.class, evaluation.getTask().getId(),
                        LockModeType.PESSIMISTIC_WRITE);
                final ParticipantStatus status = entityManager.find(ParticipantStatus.class,
                        evaluation.getParticipantStatus().getId(), LockModeType.PESSIMISTIC_WRITE);
                if (task != null && status != null) {
                    entityManager.persist(evaluation);
                } else {
                    throw new EntityNotFoundException();
                }
            }
        } else {
            try {
                this.getEvaluationByExamAndParticipant(evaluation.getExam(), evaluation.getParticipantStatus());
                throw new OutdatedException();
            } catch (NoResultException e) {
                final Exam exam = entityManager.find(Exam.class, evaluation.getExam().getId(),
                        LockModeType.PESSIMISTIC_WRITE);
                final ParticipantStatus status = entityManager.find(ParticipantStatus.class,
                        evaluation.getParticipantStatus().getId(), LockModeType.PESSIMISTIC_WRITE);
                if (exam != null && status != null) {
                    entityManager.persist(evaluation);
                } else {
                    throw new EntityNotFoundException();
                }
            }
        }
    }

    /**
     * Updates the given evaluation.
     *
     * @param evaluation Evaluation to update.
     * @throws NullPointerException If {@code evaluation} is {@code null}.
     */
    public void update(final Evaluation evaluation) throws NullPointerException, EntityNotFoundException,
            OutdatedException {
        final Evaluation currentEvaluation = entityManager.find(Evaluation.class, evaluation.getId(),
                LockModeType.PESSIMISTIC_WRITE);
        if (currentEvaluation == null) {
            throw new EntityNotFoundException();
        } else if (evaluation.getVersion() == null || !evaluation.getVersion().equals(currentEvaluation.getVersion())) {
            throw new OutdatedException();
        } else {
            try {
                entityManager.merge(evaluation);
            } catch (OptimisticLockException e) {
                throw new OutdatedException();
            }
        }
    }

    /**
     * Deletes the given evaluation. Does not fail if {@code evaluation} is {@code null}
     * or unknown.
     *
     * @param evaluation Evaluation to delete.
     */
    public void delete(final Evaluation evaluation) throws EntityNotFoundException {
        final Evaluation currentEvaluation = entityManager.find(Evaluation.class, evaluation.getId(),
                LockModeType.PESSIMISTIC_WRITE);
        if (currentEvaluation != null) {
            entityManager.remove(currentEvaluation);
        } else {
            throw new EntityNotFoundException();
        }
    }

    /**
     * Returns the Evaluation with given id.
     *
     * @param id Id of the evaluation in question.
     * @return Evaluation  with given id.
     */
    public Evaluation getById(final String id) {
        return entityManager.find(Evaluation.class, id);
    }

    /**
     * Returns the evaluation with given submission and participant.
     *
     * @param submission  Submission of evaluation in question.
     * @param participant Participant of evaluation in question.
     * @return Evaluation with given submission and participant.
     */
    public Evaluation getEvaluationBySubmissionAndParticipantStatus(final Submission submission,
                                                                    final ParticipantStatus participant) throws NoResultException {
        return entityManager.createQuery("SELECT ev FROM Evaluation ev WHERE ev.participantStatus = :participant AND " +
                "ev.submission = :submission", Evaluation.class).setParameter("participant", participant).setParameter("submission", submission).getSingleResult();
    }

    /**
     * Returns the evaluation with given exam and participant.
     *
     * @param exam        Exam of evaluation in question.
     * @param participant Participant of evaluation in question.
     * @return Evaluation with given exam and participant.
     */
    public Evaluation getEvaluationByExamAndParticipant(final Exam exam, ParticipantStatus participant) throws NoResultException {
        return entityManager.createQuery("SELECT ev FROM Evaluation ev WHERE ev.participantStatus = :participant AND "
                + "ev.exam = :exam", Evaluation.class).setParameter("participant", participant).setParameter("exam",
                exam).getSingleResult();
    }

    /**
     * Returns the evaluation with given submission and group.
     *
     * @param submission Submission of evaluation in question.
     * @param group      Group of evaluation in question.
     * @return Evaluation with given submission and group.
     */
    public Evaluation getEvaluationBySubmissionAndGroup(final Submission submission, TGroup group) {
        return null;
    }

    /**
     * Returns the evaluation with given submission and group.
     *
     * @param task task in question.
     * @param participant participant of evaluation in question.
     * @return Evaluation with given task and participant.
     */
    public Evaluation getEvaluationByTaskAndParticipant(final Task task, final ParticipantStatus participant) throws NoResultException {
        return entityManager.createQuery("SELECT e FROM Evaluation e WHERE e.task = :task AND e.participantStatus = " + ":participant", Evaluation.class).setParameter("task", task).setParameter("participant", participant).getSingleResult();
    }

    /**
     * Returns all evaluations with given participant and meeting.
     *
     * @param participant participant in question.
     * @param meeting meeting of evaluation in question.
     * @return Evaluation with given participant and meeting.
     */
    public List<Evaluation> getEvaluationsByParticipantAndMeeting(final Participant participant,
                                                                  final Meeting meeting) {
        return entityManager.createQuery("SELECT e FROM Evaluation e WHERE e.submission.meeting = :meeting AND e" +
                ".participantStatus.participant = :participant", Evaluation.class).setParameter("participant",
                participant).setParameter("meeting", meeting).getResultList();
    }
    /**
     * Returns all Evaluation with given participant and meeting.
     *
     * @param participant participant in question.
     * @param exam meeting of evaluation in question.
     * @return Evaluation with given exam and participant.
     */
    public Evaluation getExamEvaluation(final Exam exam, final Participant participant) throws NoResultException {
        return entityManager.createQuery("SELECT e FROM Evaluation e WHERE e.exam = :exam AND e.participantStatus" +
                ".participant = :participant", Evaluation.class).setParameter("exam", exam).setParameter("participant", participant).getSingleResult();
    }
    /**
     * counts  Evaluation with given meeting
     *
     * @param meeting  in question.
     * @return long number
     */
    public Long countEvaluationsByMeeting(final Meeting meeting) {
        Query q = entityManager.createQuery("SELECT COUNT(e) FROM Evaluation e WHERE e.submission.meeting = :meeting").setParameter("meeting", meeting);
        return (Long) q.getResultList().get(0);
    }
}
