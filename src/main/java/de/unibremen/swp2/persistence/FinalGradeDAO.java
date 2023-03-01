package de.unibremen.swp2.persistence;

import de.unibremen.swp2.model.FinalGrade;
import de.unibremen.swp2.model.Meeting;
import de.unibremen.swp2.model.Participant;
import de.unibremen.swp2.model.ParticipantStatus;
import de.unibremen.swp2.persistence.Exceptions.EntityNotFoundException;
import de.unibremen.swp2.persistence.Exceptions.OutdatedException;

import javax.persistence.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**@Author Martin
 */
public class FinalGradeDAO implements DAO<FinalGrade> {

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
     * Inserts the given finalGrade.
     *
     * @param finalGrade FinalGrade to insert.
     * @throws NullPointerException If {@code finalGrade} is {@code null}.
     */
    public void insert(final FinalGrade finalGrade) throws NullPointerException, OutdatedException {
        try {
            entityManager.createQuery("SELECT f FROM FinalGrade f WHERE f.participantStatus = :status AND f.meeting =" +
                    " :meeting").setParameter("status", finalGrade.getParticipantStatus()).setParameter("meeting",
                    finalGrade.getMeeting()).getSingleResult();
            throw new OutdatedException();
        } catch (NoResultException e) {
            final ParticipantStatus status = entityManager.find(ParticipantStatus.class, finalGrade.getParticipantStatus().getId(), LockModeType.PESSIMISTIC_WRITE);
            final Meeting meeting = entityManager.find(Meeting.class, finalGrade.getMeeting().getId(), LockModeType.PESSIMISTIC_WRITE);
            if (status != null && meeting != null) {
                entityManager.persist(finalGrade);
            }
        }
    }

    /**
     * Updates the given finalGrade.
     *
     * @param finalGrade
     *      FinalGrade to update.
     * @throws NullPointerException
     *      If {@code finalGrade} is {@code null}.
     */
    public void update(final FinalGrade finalGrade) throws NullPointerException, EntityNotFoundException, OutdatedException {
        final FinalGrade currentGrade = entityManager.find(FinalGrade.class, finalGrade.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (currentGrade == null) {
            throw new EntityNotFoundException();
        } else if (currentGrade.getVersion() == null || !currentGrade.getVersion().equals(finalGrade.getVersion())) {
            throw new OutdatedException();
        }
        try {
            entityManager.merge(finalGrade);
        } catch (OptimisticLockException e) {
            throw new OutdatedException();
        }
    }

    /**
     * Deletes the given object. Does not fail if {@code finalGrade} is {@code null}
     * or unknown.
     *
     * @param finalGrade
     *      FinalGrade to delete.
     */
    public void delete(final FinalGrade finalGrade){}

    /**
     * Returns the finalGrade with given id.
     *
     * @param id
     *      Id of the finalGrade in question.
     * @return
     *      FinalGrade with given id.
     */
    public FinalGrade getById(final String id){return null;}

    /**
     * Returns the finalGrade with given participant and meeting.
     *
     * @param participant Participant of finalGrade in question.
     * @param meeting     Meeting of finalGrade in question.
     * @return FinalGrade with given participant and meeting.
     */
    public FinalGrade getFinalGradeByParticipantAndMeeting(final ParticipantStatus participant,
                                                           final Meeting meeting) throws NoResultException {
        return entityManager.createQuery("SELECT f FROM FinalGrade f WHERE f.meeting = :meeting AND f" +
                ".participantStatus = :participant", FinalGrade.class).setParameter("meeting", meeting).setParameter(
                        "participant", participant).getSingleResult();
    }

    public FinalGrade getFinalGrade(final Participant participant, final Meeting meeting) throws NoResultException {
        return entityManager.createQuery("SELECT f FROM FinalGrade f WHERE f.participantStatus.participant = " +
                ":participant AND f.meeting = :meeting", FinalGrade.class).setParameter("participant", participant).setParameter("meeting", meeting).getSingleResult();
    }

    /**
     * Returns the finalGrades with given meeting.
     *
     * @param meeting Meeting of finalGrades in question.
     * @return FinalGrades with given meeting.
     */
    public List<FinalGrade> getFinalGradesByMeeting(final Meeting meeting) {
        return null;
    }

    /**
     * Returns the finalGrades with given meeting.
     *
     * @param meeting Meeting of finalGrades in question.
     * @return FinalGrades with given meeting.
     */
    public Long countFinalGradesByMeeting(final Meeting meeting) {
        Query q = entityManager.createQuery("SELECT COUNT(f) FROM FinalGrade f WHERE f.meeting = :meeting").setParameter("meeting", meeting);
        return (Long) q.getResultList().get(0);
    }
}
