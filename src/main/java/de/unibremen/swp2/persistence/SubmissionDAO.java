package de.unibremen.swp2.persistence;

import de.unibremen.swp2.model.Meeting;
import de.unibremen.swp2.model.Submission;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.HasEvaluationsException;
import de.unibremen.swp2.persistence.Exceptions.MeetingNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/*
@Author Martin
 * Process date from and into the data base
 */
@RequestScoped
public class SubmissionDAO implements DAO<Submission> {

    @PersistenceContext(name = "IGradeBook")
    private EntityManager entityManager;

    private static final ReentrantLock lock = new ReentrantLock();

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    /**
     * Inserts the given submission.
     *
     * @param submission Submission to insert.
     * @throws NullPointerException If {@code submission} is {@code null}.
     */
    public void insert(final Submission submission) throws NullPointerException, MeetingNotFoundException {
        final Meeting meeting = entityManager.find(Meeting.class, submission.getMeeting().getId(),
                LockModeType.PESSIMISTIC_WRITE);
        if (meeting != null) {
            entityManager.persist(submission);
        } else {
            throw new MeetingNotFoundException();
        }
    }

    /**
     * Updates the given submission.
     *
     * @param submission Submission to update.
     * @throws NullPointerException If {@code submission} is {@code null}.
     */
    public void update(final Submission submission) throws NullPointerException, EntityNotFoundException,
            OutdatedException {
        Submission current = entityManager.find(Submission.class, submission.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (current == null) {
            throw new EntityNotFoundException();
        } else if (current.getVersion() == null || !submission.getVersion().equals(current.getVersion())) {
            throw new OutdatedException();
        } else {
            submission.setVersion(submission.getVersion() + 1);
            entityManager.merge(submission);
        }
    }

    /**
     * Deletes the given submission. Does not fail if {@code submission} is {@code null}
     * or unknown.
     *
     * @param submission Submission to delete.
     */
    public void delete(final Submission submission) throws EntityNotFoundException, HasEvaluationsException {
        Query q = entityManager.createQuery("SELECT COUNT(e) FROM Evaluation e WHERE e.submission = :submission").setParameter("submission", submission);
        final Long i = (Long) q.getResultList().get(0);
        if (i == 0) {
            final Submission currentSubmission = entityManager.find(Submission.class, submission.getId(), LockModeType.PESSIMISTIC_WRITE);
            if (currentSubmission != null) {
                entityManager.remove(currentSubmission);
            } else {
                throw new EntityNotFoundException();
            }
        } else {
            throw new HasEvaluationsException();
        }
    }

    /**
     * Returns the submission with given id.
     *
     * @param id Id of the submission in question.
     * @return Submission with given id.
     */
    public Submission getById(final String id) {
        return entityManager.find(Submission.class, id);
    }

    /**
     * Returns the submissions with given meeting.
     *
     * @param meeting Meeting of the submissions in question.
     * @return Submissions with given meeting.
     */
    public List<Submission> getSubmissionByMeeting(final Meeting meeting) {
        return entityManager.createQuery("SELECT s FROM Submission s WHERE s.meeting =:meeting ", Submission.class).
                setParameter("meeting", meeting).getResultList();
    }
    /**
     * counts the submissions with given meeting.
     *
     * @param meeting Meeting of the submissions in question.
     * @return Submissions with given meeting.
     */
    public Long countSubmissionsByMeeting(final Meeting meeting) {
        Query q = entityManager.createQuery("SELECT COUNT(s) FROM Submission s WHERE s.meeting = :meeting").setParameter("meeting", meeting);
        return (Long) q.getResultList().get(0);
    }
}
